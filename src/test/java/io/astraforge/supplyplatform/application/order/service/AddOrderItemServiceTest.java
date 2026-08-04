package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderItemIdGenerator;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateProductException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddOrderItemServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-31T20:00:00Z");
    private static final Instant ADDED_AT =
            Instant.parse("2026-07-31T20:05:00Z");
    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000002");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final CorrelationId CORRELATION_ID =
            new CorrelationId("correlation-create-001");

    @Test
    void testAddItemShouldPersistAggregateAndReturnResult() {
        InMemoryOrderRepository repository =
                repositoryWithDraftOrder();
        AddOrderItemService service = new AddOrderItemService(
                repository,
                generator(List.of(ITEM_ID)),
                fixedClock());

        AddOrderItemResult result = service.addItem(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("updated order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.orderItemId())
                .as("created order item identifier")
                .isEqualTo(ITEM_ID);
        assertThat(result.productId())
                .as("added product identifier")
                .isEqualTo(PRODUCT_ID);
        assertThat(result.status())
                .as("order status after item addition")
                .isEqualTo(OrderStatus.DRAFT);
        assertThat(result.itemCount())
                .as("order item count after addition")
                .isEqualTo(1);
        assertThat(result.version())
                .as("order version after item addition")
                .isEqualTo(1L);
        assertThat(result.updatedAt())
                .as("order update timestamp")
                .isEqualTo(ADDED_AT);
        assertThat(savedOrder.items().getFirst().productSnapshot().sku())
                .as("persisted product SKU snapshot")
                .isEqualTo("SAFE-HELMET-001");
        assertThat(savedOrder.items().getFirst().quantity().value())
                .as("persisted item quantity")
                .isEqualByComparingTo("2.000");
    }

    @Test
    void testAddItemShouldRejectUnknownOrder() {
        AddOrderItemService service = new AddOrderItemService(
                new InMemoryOrderRepository(),
                generator(List.of(ITEM_ID)),
                fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.addItem(sonarArgument1Value1))
                .as("unknown order item addition")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testAddItemShouldPreserveDuplicateProductInvariant() {
        InMemoryOrderRepository repository =
                repositoryWithDraftOrder();
        AddOrderItemService service = new AddOrderItemService(
                repository,
                generator(List.of(ITEM_ID, SECOND_ITEM_ID)),
                fixedClock());
        service.addItem(command());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.addItem(sonarArgument2Value1))
                .as("same product cannot be added twice")
                .isInstanceOf(DuplicateProductException.class)
                .hasMessage("Product already exists in the order");
    }

    @Test
    void testAddItemShouldRejectNullCommand() {
        AddOrderItemService service = new AddOrderItemService(
                repositoryWithDraftOrder(),
                generator(List.of(ITEM_ID)),
                fixedClock());

        assertThatThrownBy(() -> service.addItem(null))
                .as("null add order item command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Add order item command must not be null");
    }

    private static AddOrderItemCommand command() {
        return new AddOrderItemCommand(
                ORDER_ID,
                PRODUCT_ID,
                " SAFE-HELMET-001 ",
                " Industrial Safety Helmet ",
                " UNIT ",
                new BigDecimal("2.000"),
                USER_ID,
                " correlation-add-item-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(ADDED_AT, ZoneOffset.UTC);
    }

    private static QueueOrderItemIdGenerator generator(
            List<UUID> identifiers
    ) {
        return new QueueOrderItemIdGenerator(identifiers);
    }

    private static InMemoryOrderRepository repositoryWithDraftOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        Order order = Order.create(
                new OrderId(ORDER_ID),
                new CustomerReference(new CustomerId(CUSTOMER_ID)),
                new UserId(USER_ID),
                CREATED_AT,
                CORRELATION_ID);
        repository.save(order);
        return repository;
    }

    private static final class QueueOrderItemIdGenerator
            implements OrderItemIdGenerator {

        private final Deque<OrderItemId> identifiers;

        private QueueOrderItemIdGenerator(List<UUID> identifiers) {
            this.identifiers = new ArrayDeque<>();
            identifiers.forEach(identifier ->
                    this.identifiers.addLast(
                            new OrderItemId(identifier)));
        }

        @Override
        public OrderItemId nextId() {
            return identifiers.removeFirst();
        }
    }

    private static final class InMemoryOrderRepository
            implements OrderRepository {

        private final Map<OrderId, Order> orders =
                new LinkedHashMap<>();

        @Override
        public Order save(Order order) {
            orders.put(order.id(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(OrderId orderId) {
            return Optional.ofNullable(orders.get(orderId));
        }

        @Override
        public boolean existsById(OrderId orderId) {
            return orders.containsKey(orderId);
        }
    }
}
