package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class ApprovalDecisionTestFixture {

    static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    static final UUID APPROVER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000002");
    static final Instant DECIDED_AT =
            Instant.parse("2026-08-01T20:25:00Z");

    private static final UUID ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");

    private ApprovalDecisionTestFixture() {
    }

    static InMemoryOrderRepository repositoryWithPendingOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        repository.save(pendingOrder());
        return repository;
    }

    static Order pendingOrder() {
        Order order = Order.create(
                new OrderId(ORDER_ID),
                new CustomerReference(new CustomerId(CUSTOMER_ID)),
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:00:00Z"),
                new CorrelationId("correlation-create-001"));
        order.addItem(
                new OrderItemId(ITEM_ID),
                new ProductSnapshot(
                        new ProductReference(new ProductId(PRODUCT_ID)),
                        "SAFE-HELMET-001",
                        "Industrial Safety Helmet",
                        "UNIT"),
                new Quantity(new BigDecimal("2.000")),
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:05:00Z"),
                new CorrelationId("correlation-add-item-001"));
        order.applyItemPricing(
                new OrderItemId(ITEM_ID),
                new ItemPricing(
                        new Money(
                                new BigDecimal("100.00"),
                                Currency.getInstance("BRL")),
                        new Percentage(new BigDecimal("10.0000")),
                        new Percentage(new BigDecimal("20.0000"))),
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:10:00Z"),
                new CorrelationId("correlation-price-item-001"));
        order.submit(
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:15:00Z"),
                new CorrelationId("correlation-submit-001"));
        order.startApproval(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:20:00Z"),
                new CorrelationId("correlation-start-approval-001"));
        return order;
    }

    static final class InMemoryOrderRepository
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
