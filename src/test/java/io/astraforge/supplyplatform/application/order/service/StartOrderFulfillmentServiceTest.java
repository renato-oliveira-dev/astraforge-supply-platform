package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderFulfillmentNotAllowedException;
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

class StartOrderFulfillmentServiceTest {

    private static final Instant STARTED_AT =
            Instant.parse("2026-08-02T20:50:00Z");

    @Test
    void testStartShouldPersistFulfillmentInProgressOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReadyOrder();
        StartOrderFulfillmentService service =
                new StartOrderFulfillmentService(
                        repository,
                        fixedClock());

        StartOrderFulfillmentResult result = service.start(command());

        assertThat(result.orderId())
                .as("fulfillment order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after fulfillment starts")
                .isEqualTo(OrderStatus.FULFILLMENT_IN_PROGRESS);
        assertThat(result.startedBy())
                .as("fulfillment start actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.startedAt())
                .as("fulfillment start timestamp")
                .isEqualTo(STARTED_AT);
        assertThat(result.version())
                .as("order version after fulfillment starts")
                .isEqualTo(10L);
    }

    @Test
    void testStartShouldRejectReservedInventoryOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReservedInventoryOrder();
        StartOrderFulfillmentService service =
                new StartOrderFulfillmentService(
                        repository,
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.start(sonarArgument1Value1))
                .as("fulfillment start requires ready status")
                .isInstanceOf(OrderFulfillmentNotAllowedException.class)
                .hasMessage(
                        "Only a READY_FOR_FULFILLMENT order can start fulfillment");
    }

    @Test
    void testStartShouldRejectNullCommand() {
        StartOrderFulfillmentService service =
                new StartOrderFulfillmentService(
                        repositoryWithReadyOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.start(null))
                .as("null start fulfillment command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Start order fulfillment command must not be null");
    }

    private static StartOrderFulfillmentCommand command() {
        return new StartOrderFulfillmentCommand(
                ORDER_ID,
                APPROVER_ID,
                "correlation-start-fulfillment-001");
    }

    private static Clock fixedClock() {
        return Clock.fixed(STARTED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithReadyOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReservedInventoryOrder();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.prepareForFulfillment(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:45:00Z"),
                new CorrelationId("correlation-prepare-fulfillment-001"));
        repository.save(order);
        return repository;
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithReservedInventoryOrder() {
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
                new CorrelationId("correlation-confirm-inventory-001"));
        repository.save(order);
        return repository;
    }
}
