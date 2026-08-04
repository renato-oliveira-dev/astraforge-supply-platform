package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderCompletionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.pendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompleteOrderFulfillmentServiceTest {

    private static final Instant COMPLETED_AT =
            Instant.parse("2026-08-02T21:00:00Z");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testCompleteShouldPersistCompletedOrderAndReturnTotals() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithFulfillmentInProgressOrder();
        CompleteOrderFulfillmentService service =
                new CompleteOrderFulfillmentService(
                        repository,
                        fixedClock());

        CompleteOrderFulfillmentResult result =
                service.complete(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("completed order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("completed order status")
                .isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.completedBy())
                .as("completion actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.completedAt())
                .as("completion timestamp")
                .isEqualTo(COMPLETED_AT);
        assertThat(result.itemCount())
                .as("completed order item count")
                .isEqualTo(1);
        assertThat(result.total())
                .as("completed order total")
                .isEqualByComparingTo("216.00");
        assertThat(result.currency())
                .as("completed order currency")
                .isEqualTo(BRL);
        assertThat(result.version())
                .as("completed order version")
                .isEqualTo(11L);
        assertThat(savedOrder.completedAt())
                .as("persisted completion timestamp")
                .contains(COMPLETED_AT);
    }

    @Test
    void testCompleteShouldRejectUnknownOrder() {
        CompleteOrderFulfillmentService service =
                new CompleteOrderFulfillmentService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.complete(sonarArgument1Value1))
                .as("completion for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testCompleteShouldRejectReadyForFulfillmentOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReadyForFulfillmentOrder();
        CompleteOrderFulfillmentService service =
                new CompleteOrderFulfillmentService(
                        repository,
                        fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.complete(sonarArgument2Value1))
                .as("completion requires fulfillment in progress")
                .isInstanceOf(OrderCompletionNotAllowedException.class)
                .hasMessage(
                        "Only a FULFILLMENT_IN_PROGRESS order can be completed");
    }

    @Test
    void testCompleteShouldRejectSecondCompletion() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithFulfillmentInProgressOrder();
        CompleteOrderFulfillmentService service =
                new CompleteOrderFulfillmentService(
                        repository,
                        fixedClock());
        service.complete(command());

        var sonarArgument3Value1 = command();
        assertThatThrownBy(() -> service.complete(sonarArgument3Value1))
                .as("order cannot be completed twice")
                .isInstanceOf(OrderCompletionNotAllowedException.class)
                .hasMessage(
                        "Only a FULFILLMENT_IN_PROGRESS order can be completed");
    }

    @Test
    void testCompleteShouldRejectNullCommand() {
        CompleteOrderFulfillmentService service =
                new CompleteOrderFulfillmentService(
                        repositoryWithFulfillmentInProgressOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.complete(null))
                .as("null completion command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Complete order fulfillment command must not be null");
    }

    private static CompleteOrderFulfillmentCommand command() {
        return new CompleteOrderFulfillmentCommand(
                ORDER_ID,
                APPROVER_ID,
                " correlation-complete-fulfillment-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(COMPLETED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithFulfillmentInProgressOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReadyForFulfillmentOrder();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.startFulfillment(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:55:00Z"),
                new CorrelationId(
                        "correlation-start-fulfillment-001"));
        repository.save(order);
        return repository;
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithReadyForFulfillmentOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        Order order = pendingOrder();
        order.approve(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:25:00Z"),
                new CorrelationId("correlation-approve-001"));
        order.startProcessing(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:30:00Z"),
                new CorrelationId("correlation-processing-001"));
        order.requestInventoryReservation(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:35:00Z"),
                new CorrelationId("correlation-inventory-001"));
        order.confirmInventoryReservation(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:40:00Z"),
                new CorrelationId(
                        "correlation-confirm-inventory-001"));
        order.prepareForFulfillment(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:45:00Z"),
                new CorrelationId(
                        "correlation-prepare-fulfillment-001"));
        repository.save(order);
        return repository;
    }
}
