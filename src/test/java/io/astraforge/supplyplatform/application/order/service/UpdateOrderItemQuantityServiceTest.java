package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityCommand;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityResult;
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

class UpdateOrderItemQuantityServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-31T20:00:00Z");
    private static final Instant CHANGED_AT =
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
    void testUpdateQuantityShouldPersistAggregateAndReturnResult() {
        InMemoryOrderRepository repository = repositoryWithItem();
        UpdateOrderItemQuantityService service =
                new UpdateOrderItemQuantityService(
                        repository,
                        fixedClock());

        UpdateOrderItemQuantityResult result =
                service.updateQuantity(command(ITEM_ID, "5.000"));

        assertThat(result.orderId())
                .as("updated order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.orderItemId())
                .as("updated order item identifier")
                .isEqualTo(ITEM_ID);
        assertThat(result.quantity())
                .as("updated quantity")
                .isEqualByComparingTo("5.000");
        assertThat(result.version())
                .as("order version after quantity update")
                .isEqualTo(2L);
        assertThat(result.updatedAt())
                .as("quantity update timestamp")
                .isEqualTo(CHANGED_AT);
    }

    @Test
    void testUpdateQuantityShouldKeepVersionForSameQuantity() {
        InMemoryOrderRepository repository = repositoryWithItem();
        UpdateOrderItemQuantityService service =
                new UpdateOrderItemQuantityService(
                        repository,
                        fixedClock());

        UpdateOrderItemQuantityResult result =
                service.updateQuantity(command(ITEM_ID, "2.000"));

        assertThat(result.version())
                .as("order version after idempotent quantity update")
                .isEqualTo(1L);
        assertThat(result.updatedAt())
                .as("order timestamp after idempotent quantity update")
                .isEqualTo(Instant.parse("2026-07-31T20:05:00Z"));
    }

    @Test
    void testUpdateQuantityShouldRejectUnknownOrder() {
        UpdateOrderItemQuantityService service =
                new UpdateOrderItemQuantityService(
                        new InMemoryOrderRepository(),
                        fixedClock());

        assertThatThrownBy(() -> service.updateQuantity(
                command(ITEM_ID, "5.000")))
                .as("quantity update for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testUpdateQuantityShouldRejectUnknownItem() {
        UpdateOrderItemQuantityService service =
                new UpdateOrderItemQuantityService(
                        repositoryWithItem(),
                        fixedClock());

        assertThatThrownBy(() -> service.updateQuantity(
                command(UNKNOWN_ITEM_ID, "5.000")))
                .as("quantity update for unknown item")
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessage("Order item was not found in the order");
    }

    private static UpdateOrderItemQuantityCommand command(
            UUID itemId,
            String quantity
    ) {
        return new UpdateOrderItemQuantityCommand(
                ORDER_ID,
                itemId,
                new BigDecimal(quantity),
                USER_ID,
                "correlation-update-item-001");
    }

    private static Clock fixedClock() {
        return Clock.fixed(CHANGED_AT, ZoneOffset.UTC);
    }

    private static InMemoryOrderRepository repositoryWithItem() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        Order order = orderWithItem();
        repository.save(order);
        return repository;
    }

    private static Order orderWithItem() {
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
        return order;
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
