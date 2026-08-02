package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.usecase.FailInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailInventoryReservationServiceTest {

    private static final Instant RECORDED_AT =
            Instant.parse("2026-08-01T20:40:00Z");

    @Test
    void testFailShouldPersistInventoryFailedOrderAndReason() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                inventoryPendingRepository();
        FailInventoryReservationService service =
                new FailInventoryReservationService(
                        repository,
                        fixedClock());

        InventoryReservationOutcomeResult result =
                service.fail(command());

        assertThat(result.status())
                .as("order status after inventory failure")
                .isEqualTo(OrderStatus.INVENTORY_FAILED);
        assertThat(result.failureReason())
                .as("inventory failure reason")
                .contains(
                        "Insufficient stock at eligible facilities.");
        assertThat(result.recordedBy())
                .as("inventory failure actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.version())
                .as("order version after inventory failure")
                .isEqualTo(8L);
    }

    @Test
    void testFailCommandShouldRejectBlankReason() {
        assertThatThrownBy(() -> new FailInventoryReservationCommand(
                ORDER_ID,
                " ",
                APPROVER_ID,
                "correlation-fail-inventory-001"))
                .as("blank inventory failure reason")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Inventory failure reason must not be blank");
    }

    @Test
    void testFailShouldRejectNullCommand() {
        FailInventoryReservationService service =
                new FailInventoryReservationService(
                        inventoryPendingRepository(),
                        fixedClock());

        assertThatThrownBy(() -> service.fail(null))
                .as("null fail inventory command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Fail inventory reservation command must not be null");
    }

    private static FailInventoryReservationCommand command() {
        return new FailInventoryReservationCommand(
                ORDER_ID,
                " Insufficient stock at eligible facilities. ",
                APPROVER_ID,
                "correlation-fail-inventory-001");
    }

    private static Clock fixedClock() {
        return Clock.fixed(RECORDED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            inventoryPendingRepository() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        Order order = ApprovalDecisionTestFixture.pendingOrder();
        order.approve(
                new io.astraforge.supplyplatform.domain.order.valueobject.UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:25:00Z"),
                new io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId(
                        "correlation-approve-001"));
        order.startProcessing(
                new io.astraforge.supplyplatform.domain.order.valueobject.UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:30:00Z"),
                new io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId(
                        "correlation-processing-001"));
        order.requestInventoryReservation(
                new io.astraforge.supplyplatform.domain.order.valueobject.UserId(APPROVER_ID),
                Instant.parse("2026-08-01T20:35:00Z"),
                new io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId(
                        "correlation-inventory-001"));
        repository.save(order);
        return repository;
    }
}
