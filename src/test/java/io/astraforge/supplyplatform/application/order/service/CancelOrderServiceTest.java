package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderCancellationNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancelOrderServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-01T20:00:00Z");
    private static final Instant CANCELLED_AT =
            Instant.parse("2026-08-01T20:10:00Z");
    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Test
    void testCancelShouldPersistCancelledOrderAndReturnResult() {
        InMemoryOrderRepository repository = repositoryWithDraftOrder();
        CancelOrderService service = new CancelOrderService(
                repository,
                fixedClock());

        CancelOrderResult result = service.cancel(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("cancelled order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("cancelled order status")
                .isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.reason())
                .as("cancelled order reason")
                .isEqualTo("Request is no longer required.");
        assertThat(result.cancelledBy())
                .as("cancellation actor")
                .isEqualTo(USER_ID);
        assertThat(result.cancelledAt())
                .as("cancellation timestamp")
                .isEqualTo(CANCELLED_AT);
        assertThat(result.version())
                .as("cancelled order version")
                .isEqualTo(1L);
        assertThat(savedOrder.cancellationReason())
                .as("persisted cancellation reason")
                .isPresent();
    }

    @Test
    void testCancelShouldRejectUnknownOrder() {
        CancelOrderService service = new CancelOrderService(
                new InMemoryOrderRepository(),
                fixedClock());

        assertThatThrownBy(() -> service.cancel(command()))
                .as("cancellation for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testCancelShouldRejectSecondCancellation() {
        InMemoryOrderRepository repository = repositoryWithDraftOrder();
        CancelOrderService service = new CancelOrderService(
                repository,
                fixedClock());
        service.cancel(command());

        assertThatThrownBy(() -> service.cancel(command()))
                .as("order cannot be cancelled twice")
                .isInstanceOf(OrderCancellationNotAllowedException.class)
                .hasMessage(
                        "Order cannot be cancelled from status CANCELLED");
    }

    @Test
    void testCancelShouldRejectNullCommand() {
        CancelOrderService service = new CancelOrderService(
                repositoryWithDraftOrder(),
                fixedClock());

        assertThatThrownBy(() -> service.cancel(null))
                .as("null cancel order command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Cancel order command must not be null");
    }

    @Test
    void testCancelCommandShouldRejectBlankReason() {
        assertThatThrownBy(() -> new CancelOrderCommand(
                ORDER_ID,
                " ",
                USER_ID,
                "correlation-cancel-001"))
                .as("blank cancellation reason")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cancellation reason must not be blank");
    }

    private static CancelOrderCommand command() {
        return new CancelOrderCommand(
                ORDER_ID,
                " Request is no longer required. ",
                USER_ID,
                " correlation-cancel-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(CANCELLED_AT, ZoneOffset.UTC);
    }

    private static InMemoryOrderRepository repositoryWithDraftOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        repository.save(Order.create(
                new OrderId(ORDER_ID),
                new CustomerReference(new CustomerId(CUSTOMER_ID)),
                new UserId(USER_ID),
                CREATED_AT,
                new CorrelationId("correlation-create-001")));
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
