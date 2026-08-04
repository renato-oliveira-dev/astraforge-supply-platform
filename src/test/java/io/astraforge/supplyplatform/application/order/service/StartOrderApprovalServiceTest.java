package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderApprovalCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderApprovalResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderApprovalNotAllowedException;
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

class StartOrderApprovalServiceTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-08-01T20:00:00Z");
    private static final Instant APPROVAL_STARTED_AT =
            Instant.parse("2026-08-01T20:20:00Z");
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
    private static final UUID APPROVER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testStartApprovalShouldPersistPendingApprovalOrder() {
        InMemoryOrderRepository repository =
                repositoryWithSubmittedOrder();
        StartOrderApprovalService service =
                new StartOrderApprovalService(
                        repository,
                        fixedClock());

        StartOrderApprovalResult result =
                service.startApproval(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("approval order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after approval starts")
                .isEqualTo(OrderStatus.PENDING_APPROVAL);
        assertThat(result.version())
                .as("order version after approval starts")
                .isEqualTo(4L);
        assertThat(result.startedAt())
                .as("approval start timestamp")
                .isEqualTo(APPROVAL_STARTED_AT);
        assertThat(savedOrder.status())
                .as("persisted approval status")
                .isEqualTo(OrderStatus.PENDING_APPROVAL);
    }

    @Test
    void testStartApprovalShouldRejectUnknownOrder() {
        StartOrderApprovalService service =
                new StartOrderApprovalService(
                        new InMemoryOrderRepository(),
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.startApproval(sonarArgument1Value1))
                .as("approval start for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testStartApprovalShouldRejectDraftOrder() {
        StartOrderApprovalService service =
                new StartOrderApprovalService(
                        repositoryWithDraftOrder(),
                        fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.startApproval(sonarArgument2Value1))
                .as("approval start requires submitted order")
                .isInstanceOf(OrderApprovalNotAllowedException.class)
                .hasMessage(
                        "Only a SUBMITTED order can start approval");
    }

    @Test
    void testStartApprovalShouldRejectSecondStart() {
        InMemoryOrderRepository repository =
                repositoryWithSubmittedOrder();
        StartOrderApprovalService service =
                new StartOrderApprovalService(
                        repository,
                        fixedClock());
        service.startApproval(command());

        var sonarArgument3Value1 = command();
        assertThatThrownBy(() -> service.startApproval(sonarArgument3Value1))
                .as("approval cannot be started twice")
                .isInstanceOf(OrderApprovalNotAllowedException.class)
                .hasMessage(
                        "Only a SUBMITTED order can start approval");
    }

    @Test
    void testStartApprovalShouldRejectNullCommand() {
        StartOrderApprovalService service =
                new StartOrderApprovalService(
                        repositoryWithSubmittedOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.startApproval(null))
                .as("null start approval command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Start order approval command must not be null");
    }

    private static StartOrderApprovalCommand command() {
        return new StartOrderApprovalCommand(
                ORDER_ID,
                APPROVER_ID,
                " correlation-start-approval-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(
                APPROVAL_STARTED_AT,
                ZoneOffset.UTC);
    }

    private static InMemoryOrderRepository repositoryWithDraftOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        repository.save(createOrder());
        return repository;
    }

    private static InMemoryOrderRepository repositoryWithSubmittedOrder() {
        InMemoryOrderRepository repository =
                new InMemoryOrderRepository();
        Order order = createOrder();
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
        order.applyItemPricing(
                new OrderItemId(ITEM_ID),
                pricing(),
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:10:00Z"),
                new CorrelationId("correlation-price-item-001"));
        order.submit(
                new UserId(USER_ID),
                Instant.parse("2026-08-01T20:15:00Z"),
                new CorrelationId("correlation-submit-order-001"));
        repository.save(order);
        return repository;
    }

    private static Order createOrder() {
        return Order.create(
                new OrderId(ORDER_ID),
                new CustomerReference(new CustomerId(CUSTOMER_ID)),
                new UserId(USER_ID),
                CREATED_AT,
                new CorrelationId("correlation-create-001"));
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
