package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderSubmissionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.Money;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.Percentage;
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
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitOrderServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-01T20:00:00Z");
    private static final Instant SUBMITTED_AT =
            Instant.parse("2026-08-01T20:15:00Z");
    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testSubmitShouldPersistSubmittedOrderAndReturnTotals() {
        InMemoryOrderRepository repository =
                repositoryWithPricedOrder();
        SubmitOrderService service = new SubmitOrderService(
                repository,
                fixedClock());

        SubmitOrderResult result = service.submit(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("submitted order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("submitted order status")
                .isEqualTo(OrderStatus.SUBMITTED);
        assertThat(result.itemCount())
                .as("submitted order item count")
                .isEqualTo(1);
        assertThat(result.total())
                .as("submitted order total")
                .isEqualByComparingTo("216.00");
        assertThat(result.currency())
                .as("submitted order currency")
                .isEqualTo(BRL);
        assertThat(result.version())
                .as("submitted order version")
                .isEqualTo(3L);
        assertThat(result.submittedAt())
                .as("submitted order timestamp")
                .isEqualTo(SUBMITTED_AT);
        assertThat(savedOrder.submittedBy())
                .as("persisted submission actor")
                .contains(new UserId(USER_ID));
    }

    @Test
    void testSubmitShouldRejectUnknownOrder() {
        SubmitOrderService service = new SubmitOrderService(
                new InMemoryOrderRepository(),
                fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.submit(sonarArgument1Value1))
                .as("submission for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testSubmitShouldPreserveIncompletePricingInvariant() {
        SubmitOrderService service = new SubmitOrderService(
                repositoryWithUnpricedOrder(),
                fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.submit(sonarArgument2Value1))
                .as("submission requires complete pricing")
                .isInstanceOf(OrderSubmissionNotAllowedException.class)
                .hasMessage(
                        "Every order item must be priced before submission");
    }

    @Test
    void testSubmitShouldRejectSecondSubmission() {
        InMemoryOrderRepository repository =
                repositoryWithPricedOrder();
        SubmitOrderService service = new SubmitOrderService(
                repository,
                fixedClock());
        service.submit(command());

        var sonarArgument3Value1 = command();
        assertThatThrownBy(() -> service.submit(sonarArgument3Value1))
                .as("order cannot be submitted twice")
                .isInstanceOf(OrderSubmissionNotAllowedException.class)
                .hasMessage("Only a DRAFT order can be submitted");
    }

    @Test
    void testSubmitShouldRejectNullCommand() {
        SubmitOrderService service = new SubmitOrderService(
                repositoryWithPricedOrder(),
                fixedClock());

        assertThatThrownBy(() -> service.submit(null))
                .as("null submit order command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Submit order command must not be null");
    }

    private static SubmitOrderCommand command() {
        return new SubmitOrderCommand(
                ORDER_ID,
                USER_ID,
                " correlation-submit-order-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(SUBMITTED_AT, ZoneOffset.UTC);
    }

    private static InMemoryOrderRepository repositoryWithPricedOrder() {
        InMemoryOrderRepository repository =
                repositoryWithUnpricedOrder();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.applyItemPricing(
                new OrderItemId(ITEM_ID),
                pricing(),
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:10:00Z"),
                new CorrelationId("correlation-price-item-001"));
        repository.save(order);
        return repository;
    }

    private static InMemoryOrderRepository repositoryWithUnpricedOrder() {
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
                Instant.parse("2026-08-01T20:05:00Z"),
                new CorrelationId("correlation-add-item-001"));
        repository.save(order);
        return repository;
    }

    private static ItemPricing pricing() {
        return new ItemPricing(
                new Money(new BigDecimal("100.00"), BRL),
                new Percentage(new BigDecimal("10.0000")),
                new Percentage(new BigDecimal("20.0000")));
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
