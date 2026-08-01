package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.port.out.OrderIdGenerator;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-31T20:00:00Z");
    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COLLIDING_ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Test
    void testCreateShouldPersistDraftOrderAndReturnResult() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        CreateOrderService service = new CreateOrderService(
                repository,
                new QueueOrderIdGenerator(ORDER_ID),
                fixedClock());

        CreateOrderResult result = service.create(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("created order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("created order status")
                .isEqualTo(OrderStatus.DRAFT);
        assertThat(result.version())
                .as("created order version")
                .isZero();
        assertThat(result.createdAt())
                .as("created order timestamp")
                .isEqualTo(CREATED_AT);
        assertThat(savedOrder.customerReference().customerId().value())
                .as("persisted order customer")
                .isEqualTo(CUSTOMER_ID);
        assertThat(savedOrder.createdBy().value())
                .as("persisted order creator")
                .isEqualTo(USER_ID);
        assertThat(savedOrder.domainEvents())
                .as("created order pending domain event")
                .hasSize(1);
    }

    @Test
    void testCreateShouldRetryWhenGeneratedIdentifierAlreadyExists() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        repository.markExisting(new OrderId(COLLIDING_ORDER_ID));
        CreateOrderService service = new CreateOrderService(
                repository,
                new QueueOrderIdGenerator(COLLIDING_ORDER_ID, ORDER_ID),
                fixedClock());

        CreateOrderResult result = service.create(command());

        assertThat(result.orderId())
                .as("order identifier after collision retry")
                .isEqualTo(ORDER_ID);
        assertThat(repository.existsById(new OrderId(ORDER_ID)))
                .as("order persisted with the available identifier")
                .isTrue();
    }

    @Test
    void testCreateShouldFailAfterIdentifierGenerationLimit() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        repository.markExisting(new OrderId(COLLIDING_ORDER_ID));
        CreateOrderService service = new CreateOrderService(
                repository,
                new QueueOrderIdGenerator(
                        COLLIDING_ORDER_ID,
                        COLLIDING_ORDER_ID,
                        COLLIDING_ORDER_ID,
                        COLLIDING_ORDER_ID,
                        COLLIDING_ORDER_ID),
                fixedClock());

        assertThatThrownBy(() -> service.create(command()))
                .as("identifier collision retry limit")
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Unable to generate a unique order ID after 5 attempts");
    }

    @Test
    void testCreateShouldRejectNullCommand() {
        CreateOrderService service = new CreateOrderService(
                new InMemoryOrderRepository(),
                new QueueOrderIdGenerator(ORDER_ID),
                fixedClock());

        assertThatThrownBy(() -> service.create(null))
                .as("null create order command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Create order command must not be null");
    }

    private static CreateOrderCommand command() {
        return new CreateOrderCommand(
                CUSTOMER_ID,
                USER_ID,
                " correlation-create-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(CREATED_AT, ZoneOffset.UTC);
    }

    private static final class QueueOrderIdGenerator
            implements OrderIdGenerator {

        private final Deque<OrderId> identifiers;

        private QueueOrderIdGenerator(UUID... identifiers) {
            this.identifiers = new ArrayDeque<>();
            for (UUID identifier : identifiers) {
                this.identifiers.addLast(new OrderId(identifier));
            }
        }

        @Override
        public OrderId nextId() {
            return identifiers.removeFirst();
        }
    }

    private static final class InMemoryOrderRepository
            implements OrderRepository {

        private final Map<OrderId, Order> orders = new LinkedHashMap<>();

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

        private void markExisting(OrderId orderId) {
            orders.put(orderId, null);
        }
    }
}
