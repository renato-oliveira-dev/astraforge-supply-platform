package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.exception.OrderItemNotFoundException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductReference;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoveOrderItemServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-31T20:00:00Z");
    private static final Instant REMOVED_AT =
            Instant.parse("2026-07-31T20:10:00Z");
    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID UNKNOWN_ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000099");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Test
    void testRemoveItemShouldPersistAggregateAndReturnResult() {
        InMemoryOrderRepository repository = repositoryWithItem();
        RemoveOrderItemService service = new RemoveOrderItemService(
                repository,
                fixedClock());

        RemoveOrderItemResult result =
                service.removeItem(command(ITEM_ID));

        assertThat(result.orderId())
                .as("updated order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.removedOrderItemId())
                .as("removed order item identifier")
                .isEqualTo(ITEM_ID);
        assertThat(result.itemCount())
                .as("remaining order item count")
                .isZero();
        assertThat(result.version())
                .as("order version after item removal")
                .isEqualTo(2L);
        assertThat(result.updatedAt())
                .as("item removal timestamp")
                .isEqualTo(REMOVED_AT);
        assertThat(repository.findById(new OrderId(ORDER_ID))
                .orElseThrow()
                .items())
                .as("persisted items after removal")
                .isEmpty();
    }

    @Test
    void testRemoveItemShouldRejectUnknownOrder() {
        RemoveOrderItemService service = new RemoveOrderItemService(
                new InMemoryOrderRepository(),
                fixedClock());

        var sonarArgument1Value1 = command(ITEM_ID);
        assertThatThrownBy(() -> service.removeItem(sonarArgument1Value1))
                .as("item removal for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testRemoveItemShouldRejectUnknownItem() {
        RemoveOrderItemService service = new RemoveOrderItemService(
                repositoryWithItem(),
                fixedClock());

        var sonarArgument2Value1 = command(UNKNOWN_ITEM_ID);
        assertThatThrownBy(() -> service.removeItem(sonarArgument2Value1))
                .as("unknown order item removal")
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessage("Order item was not found in the order");
    }

    @Test
    void testRemoveItemShouldRejectNullCommand() {
        RemoveOrderItemService service = new RemoveOrderItemService(
                repositoryWithItem(),
                fixedClock());

        assertThatThrownBy(() -> service.removeItem(null))
                .as("null remove order item command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Remove order item command must not be null");
    }

    private static RemoveOrderItemCommand command(UUID itemId) {
        return new RemoveOrderItemCommand(
                ORDER_ID,
                itemId,
                USER_ID,
                "correlation-remove-item-001");
    }

    private static Clock fixedClock() {
        return Clock.fixed(REMOVED_AT, ZoneOffset.UTC);
    }

    private static InMemoryOrderRepository repositoryWithItem() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        Order order = Order.create(
                new OrderId(ORDER_ID),
                new CustomerReference(new CustomerId(CUSTOMER_ID)),
                new UserId(USER_ID),
                CREATED_AT,
                new CorrelationId("correlation-create-001"));
        order.addItem(
                new OrderItemId(ITEM_ID),
                new ProductSnapshot(
                        new ProductReference(
                                new ProductId(PRODUCT_ID)),
                        "SAFE-HELMET-001",
                        "Industrial Safety Helmet",
                        "UNIT"),
                new Quantity(new BigDecimal("2.000")),
                new UserId(USER_ID),
                Instant.parse("2026-07-31T20:05:00Z"),
                new CorrelationId("correlation-add-item-001"));
        repository.save(order);
        return repository;
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
