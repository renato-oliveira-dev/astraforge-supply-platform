package io.astraforge.supplyplatform.infrastructure.order.persistence.memory;

import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryOrderRepositoryAdapterTest {

    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_CUSTOMER_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000002");
    private static final UUID USER_ID =
            UUID.fromString(
                    "30000000-0000-0000-0000-000000000001");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testSaveShouldSupportFindExistsSizeAndClear() {
        InMemoryOrderRepositoryAdapter repository =
                new InMemoryOrderRepositoryAdapter();
        Order order = order(
                "10000000-0000-0000-0000-000000000001",
                CUSTOMER_ID,
                "2026-08-03T10:00:00Z");

        Order savedOrder = repository.save(order);

        assertThat(savedOrder)
                .as("saved order")
                .isSameAs(order);
        assertThat(repository.findById(order.id()))
                .as("stored order lookup")
                .contains(order);
        assertThat(repository.existsById(order.id()))
                .as("stored order existence")
                .isTrue();
        assertThat(repository.size())
                .as("stored order count")
                .isEqualTo(1);

        repository.clear();

        assertThat(repository.size())
                .as("stored order count after clear")
                .isZero();
    }

    @Test
    void testSearchShouldFilterSortAndPaginateOrders() {
        InMemoryOrderRepositoryAdapter repository =
                populatedRepository();

        OrderPageResult result = repository.search(
                new ListOrdersQuery(
                        Optional.of(CUSTOMER_ID),
                        Optional.of(OrderStatus.DRAFT),
                        0,
                        1));

        assertThat(result.content())
                .as("filtered first order page")
                .hasSize(1);
        assertThat(result.content().getFirst().orderId())
                .as("newest matching order")
                .isEqualTo(UUID.fromString(
                        "10000000-0000-0000-0000-000000000003"));
        assertThat(result.totalElements())
                .as("filtered order count")
                .isEqualTo(2L);
        assertThat(result.totalPages())
                .as("filtered order page count")
                .isEqualTo(2);
    }

    @Test
    void testSearchShouldReturnPricedSummaryWithTotals() {
        InMemoryOrderRepositoryAdapter repository =
                new InMemoryOrderRepositoryAdapter();
        Order order = pricedOrder();
        repository.save(order);

        OrderPageResult result = repository.search(
                new ListOrdersQuery(
                        Optional.empty(),
                        Optional.empty(),
                        0,
                        20));

        assertThat(result.content().getFirst().pricingComplete())
                .as("priced order summary state")
                .isTrue();
        assertThat(result.content().getFirst().total())
                .as("priced order summary total")
                .contains(new BigDecimal("216.00"));
        assertThat(result.content().getFirst().currency())
                .as("priced order summary currency")
                .contains(BRL);
    }

    @Test
    void testSearchShouldReturnEmptyPageWhenPageIsOutOfRange() {
        InMemoryOrderRepositoryAdapter repository =
                populatedRepository();

        OrderPageResult result = repository.search(
                new ListOrdersQuery(
                        Optional.empty(),
                        Optional.empty(),
                        10,
                        20));

        assertThat(result.content())
                .as("out-of-range page content")
                .isEmpty();
        assertThat(result.totalElements())
                .as("out-of-range page total elements")
                .isEqualTo(3L);
    }

    @Test
    void testRepositoryShouldRejectNullArguments() {
        InMemoryOrderRepositoryAdapter repository =
                new InMemoryOrderRepositoryAdapter();

        assertThatThrownBy(() -> repository.save(null))
                .as("null order save")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Order must not be null");
        assertThatThrownBy(() -> repository.findById(null))
                .as("null order identifier lookup")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Order ID must not be null");
        assertThatThrownBy(() -> repository.search(null))
                .as("null order search query")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("List orders query must not be null");
    }

    private static InMemoryOrderRepositoryAdapter
            populatedRepository() {
        InMemoryOrderRepositoryAdapter repository =
                new InMemoryOrderRepositoryAdapter();
        repository.save(order(
                "10000000-0000-0000-0000-000000000001",
                CUSTOMER_ID,
                "2026-08-03T10:00:00Z"));
        repository.save(order(
                "10000000-0000-0000-0000-000000000002",
                OTHER_CUSTOMER_ID,
                "2026-08-03T10:05:00Z"));
        repository.save(order(
                "10000000-0000-0000-0000-000000000003",
                CUSTOMER_ID,
                "2026-08-03T10:10:00Z"));
        return repository;
    }

    private static Order pricedOrder() {
        Order order = order(
                "10000000-0000-0000-0000-000000000010",
                CUSTOMER_ID,
                "2026-08-03T10:00:00Z");
        OrderItemId itemId = new OrderItemId(UUID.fromString(
                "11000000-0000-0000-0000-000000000010"));
        order.addItem(
                itemId,
                new ProductSnapshot(
                        new ProductReference(new ProductId(
                                UUID.fromString(
                                        "40000000-0000-0000-0000-000000000010"))),
                        "SAFE-HELMET-001",
                        "Industrial Safety Helmet",
                        "UNIT"),
                new Quantity(new BigDecimal("2.000")),
                new UserId(USER_ID),
                Instant.parse("2026-08-03T10:05:00Z"),
                new CorrelationId("correlation-add-001"));
        order.applyItemPricing(
                itemId,
                new ItemPricing(
                        new Money(new BigDecimal("100.00"), BRL),
                        new Percentage(new BigDecimal("10.0000")),
                        new Percentage(new BigDecimal("20.0000"))),
                new UserId(USER_ID),
                Instant.parse("2026-08-03T10:10:00Z"),
                new CorrelationId("correlation-price-001"));
        return order;
    }

    private static Order order(
            String orderId,
            UUID customerId,
            String createdAt
    ) {
        return Order.create(
                new OrderId(UUID.fromString(orderId)),
                new CustomerReference(new CustomerId(customerId)),
                new UserId(USER_ID),
                Instant.parse(createdAt),
                new CorrelationId(
                        "correlation-" + orderId.substring(0, 8)));
    }
}
