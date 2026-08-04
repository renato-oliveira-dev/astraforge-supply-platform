package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderProcessingNotAllowedException;
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

class RequestInventoryReservationServiceTest {

    private static final Instant APPROVED_AT =
            Instant.parse("2026-08-01T20:25:00Z");
    private static final Instant PROCESSING_STARTED_AT =
            Instant.parse("2026-08-01T20:30:00Z");
    private static final Instant REQUESTED_AT =
            Instant.parse("2026-08-01T20:35:00Z");

    @Test
    void testRequestReservationShouldPersistInventoryPendingOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithProcessingOrder();
        RequestInventoryReservationService service =
                new RequestInventoryReservationService(
                        repository,
                        fixedClock());

        RequestInventoryReservationResult result =
                service.requestReservation(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("inventory reservation order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after inventory reservation request")
                .isEqualTo(OrderStatus.INVENTORY_PENDING);
        assertThat(result.requestedBy())
                .as("inventory reservation request actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.requestedAt())
                .as("inventory reservation request timestamp")
                .isEqualTo(REQUESTED_AT);
        assertThat(result.itemCount())
                .as("inventory reservation item count")
                .isEqualTo(1);
        assertThat(result.version())
                .as("order version after inventory reservation request")
                .isEqualTo(7L);
        assertThat(savedOrder.inventoryRequestedAt())
                .as("persisted inventory reservation timestamp")
                .contains(REQUESTED_AT);
    }

    @Test
    void testRequestReservationShouldRejectUnknownOrder() {
        RequestInventoryReservationService service =
                new RequestInventoryReservationService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.requestReservation(sonarArgument1Value1))
                .as("inventory reservation for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testRequestReservationShouldRejectApprovedOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithApprovedOrder();
        RequestInventoryReservationService service =
                new RequestInventoryReservationService(
                        repository,
                        fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.requestReservation(sonarArgument2Value1))
                .as("inventory reservation requires processing status")
                .isInstanceOf(OrderProcessingNotAllowedException.class)
                .hasMessage(
                        "Only a PROCESSING order can request inventory reservation");
    }

    @Test
    void testRequestReservationShouldRejectSecondRequest() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithProcessingOrder();
        RequestInventoryReservationService service =
                new RequestInventoryReservationService(
                        repository,
                        fixedClock());
        service.requestReservation(command());

        var sonarArgument3Value1 = command();
        assertThatThrownBy(() -> service.requestReservation(sonarArgument3Value1))
                .as("inventory reservation cannot be requested twice")
                .isInstanceOf(OrderProcessingNotAllowedException.class)
                .hasMessage(
                        "Only a PROCESSING order can request inventory reservation");
    }

    @Test
    void testRequestReservationShouldRejectNullCommand() {
        RequestInventoryReservationService service =
                new RequestInventoryReservationService(
                        repositoryWithProcessingOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.requestReservation(null))
                .as("null inventory reservation command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Request inventory reservation command must not be null");
    }

    private static RequestInventoryReservationCommand command() {
        return new RequestInventoryReservationCommand(
                ORDER_ID,
                APPROVER_ID,
                " correlation-request-inventory-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(REQUESTED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithProcessingOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithApprovedOrder();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.startProcessing(
                new UserId(APPROVER_ID),
                PROCESSING_STARTED_AT,
                new CorrelationId(
                        "correlation-start-processing-001"));
        repository.save(order);
        return repository;
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithApprovedOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        Order order = pendingOrder();
        order.approve(
                new UserId(APPROVER_ID),
                APPROVED_AT,
                new CorrelationId("correlation-approve-001"));
        repository.save(order);
        return repository;
    }
}
