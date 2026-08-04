package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentResult;
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

class PrepareOrderForFulfillmentServiceTest {

    private static final Instant PREPARED_AT =
            Instant.parse("2026-08-02T20:45:00Z");

    @Test
    void testPrepareShouldPersistReadyForFulfillmentOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReservedInventoryOrder();
        PrepareOrderForFulfillmentService service =
                new PrepareOrderForFulfillmentService(
                        repository,
                        fixedClock());

        PrepareOrderForFulfillmentResult result =
                service.prepare(command());

        assertThat(result.orderId())
                .as("prepared order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after fulfillment preparation")
                .isEqualTo(OrderStatus.READY_FOR_FULFILLMENT);
        assertThat(result.preparedBy())
                .as("fulfillment preparation actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.preparedAt())
                .as("fulfillment preparation timestamp")
                .isEqualTo(PREPARED_AT);
        assertThat(result.itemCount())
                .as("fulfillment preparation item count")
                .isEqualTo(1);
        assertThat(result.version())
                .as("order version after fulfillment preparation")
                .isEqualTo(9L);
    }

    @Test
    void testPrepareShouldRejectUnknownOrder() {
        PrepareOrderForFulfillmentService service =
                new PrepareOrderForFulfillmentService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.prepare(sonarArgument1Value1))
                .as("fulfillment preparation for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testPrepareShouldRejectInventoryPendingOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithInventoryPendingOrder();
        PrepareOrderForFulfillmentService service =
                new PrepareOrderForFulfillmentService(
                        repository,
                        fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.prepare(sonarArgument2Value1))
                .as("fulfillment preparation requires reserved inventory")
                .isInstanceOf(OrderFulfillmentNotAllowedException.class)
                .hasMessage(
                        "Only an INVENTORY_RESERVED order can be prepared for fulfillment");
    }

    private static PrepareOrderForFulfillmentCommand command() {
        return new PrepareOrderForFulfillmentCommand(
                ORDER_ID,
                APPROVER_ID,
                "correlation-prepare-fulfillment-001");
    }

    private static Clock fixedClock() {
        return Clock.fixed(PREPARED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithReservedInventoryOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithInventoryPendingOrder();
        Order order = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        order.confirmInventoryReservation(
                new UserId(APPROVER_ID),
                Instant.parse("2026-08-02T20:40:00Z"),
                new CorrelationId("correlation-confirm-inventory-001"));
        repository.save(order);
        return repository;
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithInventoryPendingOrder() {
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
        repository.save(order);
        return repository;
    }
}
