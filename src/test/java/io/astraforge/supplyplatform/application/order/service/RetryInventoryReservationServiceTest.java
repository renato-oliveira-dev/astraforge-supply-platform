package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderInventoryRetryNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.InventoryFailureReason;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.pendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryInventoryReservationServiceTest {

    private static final Instant RETRIED_AT =
            Instant.parse("2026-08-01T20:45:00Z");

    @Test
    void testRetryShouldPersistInventoryPendingOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithInventoryFailedOrder();
        RetryInventoryReservationService service =
                new RetryInventoryReservationService(
                        repository,
                        fixedClock());

        RetryInventoryReservationResult result =
                service.retry(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("retried inventory order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after inventory retry")
                .isEqualTo(OrderStatus.INVENTORY_PENDING);
        assertThat(result.requestedBy())
                .as("inventory retry actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.requestedAt())
                .as("inventory retry timestamp")
                .isEqualTo(RETRIED_AT);
        assertThat(result.version())
                .as("order version after inventory retry")
                .isEqualTo(9L);
        assertThat(savedOrder.inventoryFailureReason())
                .as("active failure reason after retry")
                .isEmpty();
        assertThat(savedOrder.inventoryResultRecordedBy())
                .as("previous result actor after retry")
                .isEmpty();
        assertThat(savedOrder.inventoryResultRecordedAt())
                .as("previous result timestamp after retry")
                .isEmpty();
    }

    @Test
    void testRetryShouldRejectUnknownOrder() {
        RetryInventoryReservationService service =
                new RetryInventoryReservationService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.retry(sonarArgument1Value1))
                .as("inventory retry for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testRetryShouldRejectOrderOutsideInventoryFailed() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        repository.save(pendingOrder());
        RetryInventoryReservationService service =
                new RetryInventoryReservationService(
                        repository,
                        fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.retry(sonarArgument2Value1))
                .as("inventory retry requires failed status")
                .isInstanceOf(
                        OrderInventoryRetryNotAllowedException.class)
                .hasMessage(
                        "Inventory reservation can be retried only while the order is in INVENTORY_FAILED status");
    }

    @Test
    void testRetryShouldRejectNullCommand() {
        RetryInventoryReservationService service =
                new RetryInventoryReservationService(
                        repositoryWithInventoryFailedOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.retry(null))
                .as("null inventory retry command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Retry inventory reservation command must not be null");
    }

    private static RetryInventoryReservationCommand command() {
        return new RetryInventoryReservationCommand(
                ORDER_ID,
                APPROVER_ID,
                " correlation-retry-inventory-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(RETRIED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithInventoryFailedOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        Order order = pendingOrder();
        order.approve(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:25:00Z"),
                new CorrelationId("correlation-approve-001"));
        order.startProcessing(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:30:00Z"),
                new CorrelationId("correlation-processing-001"));
        order.requestInventoryReservation(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:35:00Z"),
                new CorrelationId("correlation-inventory-001"));
        order.failInventoryReservation(
                new InventoryFailureReason(
                        "Insufficient stock at eligible facilities."),
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:40:00Z"),
                new CorrelationId("correlation-fail-inventory-001"));
        repository.save(order);
        return repository;
    }
}
