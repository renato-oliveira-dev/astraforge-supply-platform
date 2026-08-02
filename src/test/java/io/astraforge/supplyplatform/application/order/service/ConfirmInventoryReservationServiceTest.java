package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.ConfirmInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderInventoryResultNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
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

class ConfirmInventoryReservationServiceTest {

    private static final Instant RECORDED_AT =
            Instant.parse("2026-08-01T20:40:00Z");

    @Test
    void testConfirmShouldPersistInventoryReservedOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithInventoryPendingOrder();
        ConfirmInventoryReservationService service =
                new ConfirmInventoryReservationService(
                        repository,
                        fixedClock());

        InventoryReservationOutcomeResult result =
                service.confirm(command());

        assertThat(result.orderId())
                .as("inventory outcome order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after inventory confirmation")
                .isEqualTo(OrderStatus.INVENTORY_RESERVED);
        assertThat(result.recordedBy())
                .as("inventory confirmation actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.recordedAt())
                .as("inventory confirmation timestamp")
                .isEqualTo(RECORDED_AT);
        assertThat(result.failureReason())
                .as("successful inventory outcome failure reason")
                .isEmpty();
        assertThat(result.version())
                .as("order version after inventory confirmation")
                .isEqualTo(8L);
    }

    @Test
    void testConfirmShouldRejectUnknownOrder() {
        ConfirmInventoryReservationService service =
                new ConfirmInventoryReservationService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        assertThatThrownBy(() -> service.confirm(command()))
                .as("inventory confirmation for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testConfirmShouldRejectOrderOutsideInventoryPending() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        repository.save(pendingOrder());
        ConfirmInventoryReservationService service =
                new ConfirmInventoryReservationService(
                        repository,
                        fixedClock());

        assertThatThrownBy(() -> service.confirm(command()))
                .as("inventory confirmation requires pending status")
                .isInstanceOf(
                        OrderInventoryResultNotAllowedException.class)
                .hasMessage(
                        "Inventory result can be recorded only while the order is IN INVENTORY_PENDING status");
    }

    private static ConfirmInventoryReservationCommand command() {
        return new ConfirmInventoryReservationCommand(
                ORDER_ID,
                APPROVER_ID,
                "correlation-confirm-inventory-001");
    }

    private static Clock fixedClock() {
        return Clock.fixed(RECORDED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithInventoryPendingOrder() {
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
        repository.save(order);
        return repository;
    }
}
