package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.exception.OrderCurrencyMismatchException;
import io.astraforge.supplyplatform.domain.order.exception.OrderItemNotFoundException;
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

class ApplyOrderItemPricingServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-01T20:00:00Z");
    private static final Instant PRICED_AT =
            Instant.parse("2026-08-01T20:10:00Z");
    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000002");
    private static final UUID UNKNOWN_ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000099");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000002");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void testApplyPricingShouldPersistPricingAndReturnItemTotal() {
        InMemoryOrderRepository repository = repositoryWithOneItem();
        ApplyOrderItemPricingService service =
                new ApplyOrderItemPricingService(
                        repository,
                        fixedClock());

        ApplyOrderItemPricingResult result =
                service.applyPricing(command(ITEM_ID, BRL));

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("priced order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.orderItemId())
                .as("priced item identifier")
                .isEqualTo(ITEM_ID);
        assertThat(result.itemTotal())
                .as("item total after discount and tax")
                .isEqualByComparingTo("216.00");
        assertThat(result.currency())
                .as("pricing currency")
                .isEqualTo(BRL);
        assertThat(result.pricingComplete())
                .as("single-item order pricing completeness")
                .isTrue();
        assertThat(result.version())
                .as("order version after pricing")
                .isEqualTo(2L);
        assertThat(result.updatedAt())
                .as("pricing timestamp")
                .isEqualTo(PRICED_AT);
        assertThat(savedOrder.items().getFirst().pricing())
                .as("persisted item pricing")
                .isPresent();
    }

    @Test
    void testApplyPricingShouldBeIdempotentForIdenticalPricing() {
        InMemoryOrderRepository repository = repositoryWithOneItem();
        ApplyOrderItemPricingService service =
                new ApplyOrderItemPricingService(
                        repository,
                        fixedClock());
        service.applyPricing(command(ITEM_ID, BRL));

        ApplyOrderItemPricingResult result =
                service.applyPricing(command(ITEM_ID, BRL));

        assertThat(result.version())
                .as("order version after identical pricing")
                .isEqualTo(2L);
        assertThat(result.updatedAt())
                .as("order timestamp after identical pricing")
                .isEqualTo(PRICED_AT);
    }

    @Test
    void testApplyPricingShouldRejectUnknownOrder() {
        ApplyOrderItemPricingService service =
                new ApplyOrderItemPricingService(
                        new InMemoryOrderRepository(),
                        fixedClock());

        assertThatThrownBy(() -> service.applyPricing(
                command(ITEM_ID, BRL)))
                .as("pricing for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testApplyPricingShouldRejectUnknownItem() {
        ApplyOrderItemPricingService service =
                new ApplyOrderItemPricingService(
                        repositoryWithOneItem(),
                        fixedClock());

        assertThatThrownBy(() -> service.applyPricing(
                command(UNKNOWN_ITEM_ID, BRL)))
                .as("pricing for unknown order item")
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessage("Order item was not found in the order");
    }

    @Test
    void testApplyPricingShouldPreserveOrderCurrencyInvariant() {
        InMemoryOrderRepository repository = repositoryWithTwoItems();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.applyItemPricing(
                new OrderItemId(ITEM_ID),
                pricing(BRL),
                new UserId(USER_ID),
                PRICED_AT,
                new CorrelationId("correlation-price-first"));
        repository.save(order);
        ApplyOrderItemPricingService service =
                new ApplyOrderItemPricingService(
                        repository,
                        fixedClock());

        assertThatThrownBy(() -> service.applyPricing(
                command(SECOND_ITEM_ID, USD)))
                .as("mixed currencies in one order")
                .isInstanceOf(OrderCurrencyMismatchException.class)
                .hasMessage(
                        "All order items must use the same currency");
    }

    @Test
    void testApplyPricingShouldRejectNullCommand() {
        ApplyOrderItemPricingService service =
                new ApplyOrderItemPricingService(
                        repositoryWithOneItem(),
                        fixedClock());

        assertThatThrownBy(() -> service.applyPricing(null))
                .as("null pricing command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Apply order item pricing command must not be null");
    }

    private static ApplyOrderItemPricingCommand command(
            UUID itemId,
            Currency currency
    ) {
        return new ApplyOrderItemPricingCommand(
                ORDER_ID,
                itemId,
                new BigDecimal("100.00"),
                currency,
                new BigDecimal("10.0000"),
                new BigDecimal("20.0000"),
                USER_ID,
                " correlation-price-item-001 ");
    }

    private static ItemPricing pricing(Currency currency) {
        return new ItemPricing(
                new Money(new BigDecimal("100.00"), currency),
                new Percentage(new BigDecimal("10.0000")),
                new Percentage(new BigDecimal("20.0000")));
    }

    private static Clock fixedClock() {
        return Clock.fixed(PRICED_AT, ZoneOffset.UTC);
    }

    private static InMemoryOrderRepository repositoryWithOneItem() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        Order order = order();
        addItem(order, ITEM_ID, PRODUCT_ID, "SAFE-HELMET-001");
        repository.save(order);
        return repository;
    }

    private static InMemoryOrderRepository repositoryWithTwoItems() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        Order order = order();
        addItem(order, ITEM_ID, PRODUCT_ID, "SAFE-HELMET-001");
        addItem(
                order,
                SECOND_ITEM_ID,
                SECOND_PRODUCT_ID,
                "SAFETY-GLOVE-001");
        repository.save(order);
        return repository;
    }

    private static Order order() {
        return Order.create(
                new OrderId(ORDER_ID),
                new CustomerReference(new CustomerId(CUSTOMER_ID)),
                new UserId(USER_ID),
                CREATED_AT,
                new CorrelationId("correlation-create-001"));
    }

    private static void addItem(
            Order order,
            UUID itemId,
            UUID productId,
            String sku
    ) {
        order.addItem(
                new OrderItemId(itemId),
                new ProductSnapshot(
                        new ProductReference(
                                new ProductId(productId)),
                        sku,
                        "Industrial Safety Item",
                        "UNIT"),
                new Quantity(new BigDecimal("2.000")),
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:05:00Z"),
                new CorrelationId("correlation-add-item"));
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
