package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.GetOrderDetailsQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderItemDetailsResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
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
import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetOrderDetailsServiceTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-02T20:00:00Z");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testGetDetailsShouldReturnPricedOrderSnapshot() {
        InMemoryOrderRepository repository =
                repositoryWithPricedOrder();
        GetOrderDetailsService service =
                new GetOrderDetailsService(repository);

        OrderDetailsResult result =
                service.getDetails(new GetOrderDetailsQuery(ORDER_ID));

        OrderItemDetailsResult item = result.items().getFirst();
        assertThat(result.orderId())
                .as("queried order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.customerId())
                .as("queried order customer")
                .isEqualTo(CUSTOMER_ID);
        assertThat(result.status())
                .as("queried order status")
                .isEqualTo(OrderStatus.DRAFT);
        assertThat(result.items())
                .as("queried order items")
                .hasSize(1);
        assertThat(result.pricingComplete())
                .as("queried order pricing completeness")
                .isTrue();
        assertThat(result.subtotal())
                .as("queried order subtotal")
                .contains(new BigDecimal("200.00"));
        assertThat(result.discount())
                .as("queried order discount")
                .contains(new BigDecimal("20.00"));
        assertThat(result.tax())
                .as("queried order tax")
                .contains(new BigDecimal("36.00"));
        assertThat(result.total())
                .as("queried order total")
                .contains(new BigDecimal("216.00"));
        assertThat(result.currency())
                .as("queried order currency")
                .contains(BRL);
        assertThat(item.priced())
                .as("queried item pricing state")
                .isTrue();
        assertThat(item.unitPrice())
                .as("queried item unit price")
                .contains(new BigDecimal("100.00"));
        assertThat(item.total())
                .as("queried item total")
                .contains(new BigDecimal("216.00"));
    }

    @Test
    void testGetDetailsShouldReturnEmptyTotalsForUnpricedOrder() {
        GetOrderDetailsService service =
                new GetOrderDetailsService(repositoryWithUnpricedOrder());

        OrderDetailsResult result =
                service.getDetails(new GetOrderDetailsQuery(ORDER_ID));

        assertThat(result.pricingComplete())
                .as("unpriced order pricing completeness")
                .isFalse();
        assertThat(result.total())
                .as("unpriced order total")
                .isEmpty();
        assertThat(result.currency())
                .as("unpriced order currency")
                .isEmpty();
        assertThat(result.items().getFirst().priced())
                .as("unpriced item pricing state")
                .isFalse();
    }

    @Test
    void testGetDetailsShouldRejectUnknownOrder() {
        GetOrderDetailsService service =
                new GetOrderDetailsService(
                        new InMemoryOrderRepository());

        assertThatThrownBy(() -> service.getDetails(
                new GetOrderDetailsQuery(ORDER_ID)))
                .as("details query for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testGetDetailsShouldRejectNullQuery() {
        GetOrderDetailsService service =
                new GetOrderDetailsService(
                        repositoryWithPricedOrder());

        assertThatThrownBy(() -> service.getDetails(null))
                .as("null order details query")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Get order details query must not be null");
    }

    private static InMemoryOrderRepository repositoryWithPricedOrder() {
        InMemoryOrderRepository repository =
                repositoryWithUnpricedOrder();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.applyItemPricing(
                new OrderItemId(ITEM_ID),
                new ItemPricing(
                        new Money(new BigDecimal("100.00"), BRL),
                        new Percentage(new BigDecimal("10.0000")),
                        new Percentage(new BigDecimal("20.0000"))),
                new UserId(USER_ID),
                Instant.parse("2026-08-02T20:10:00Z"),
                new CorrelationId("correlation-price-001"));
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
                Instant.parse("2026-08-02T20:05:00Z"),
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
