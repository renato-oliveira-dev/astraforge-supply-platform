package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingResult;
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
import java.util.UUID;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.pendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartOrderProcessingServiceTest {

    private static final Instant APPROVED_AT =
            Instant.parse("2026-08-01T20:25:00Z");
    private static final Instant PROCESSING_STARTED_AT =
            Instant.parse("2026-08-01T20:30:00Z");

    @Test
    void testStartProcessingShouldPersistProcessingOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithApprovedOrder();
        StartOrderProcessingService service =
                new StartOrderProcessingService(
                        repository,
                        fixedClock());

        StartOrderProcessingResult result =
                service.startProcessing(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("processing order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("order status after processing starts")
                .isEqualTo(OrderStatus.PROCESSING);
        assertThat(result.startedBy())
                .as("processing start actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.startedAt())
                .as("processing start timestamp")
                .isEqualTo(PROCESSING_STARTED_AT);
        assertThat(result.version())
                .as("processing order version")
                .isEqualTo(6L);
        assertThat(savedOrder.processingStartedAt())
                .as("persisted processing start timestamp")
                .contains(PROCESSING_STARTED_AT);
    }

    @Test
    void testStartProcessingShouldRejectUnknownOrder() {
        StartOrderProcessingService service =
                new StartOrderProcessingService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        assertThatThrownBy(() -> service.startProcessing(command()))
                .as("processing start for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testStartProcessingShouldRejectPendingApprovalOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        repository.save(pendingOrder());
        StartOrderProcessingService service =
                new StartOrderProcessingService(
                        repository,
                        fixedClock());

        assertThatThrownBy(() -> service.startProcessing(command()))
                .as("processing start requires approved order")
                .isInstanceOf(OrderProcessingNotAllowedException.class)
                .hasMessage(
                        "Only an APPROVED order can start processing");
    }

    @Test
    void testStartProcessingShouldRejectSecondStart() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithApprovedOrder();
        StartOrderProcessingService service =
                new StartOrderProcessingService(
                        repository,
                        fixedClock());
        service.startProcessing(command());

        assertThatThrownBy(() -> service.startProcessing(command()))
                .as("processing cannot be started twice")
                .isInstanceOf(OrderProcessingNotAllowedException.class)
                .hasMessage(
                        "Only an APPROVED order can start processing");
    }

    @Test
    void testStartProcessingShouldRejectNullCommand() {
        StartOrderProcessingService service =
                new StartOrderProcessingService(
                        repositoryWithApprovedOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.startProcessing(null))
                .as("null start processing command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Start order processing command must not be null");
    }

    private static StartOrderProcessingCommand command() {
        return new StartOrderProcessingCommand(
                ORDER_ID,
                APPROVER_ID,
                " correlation-start-processing-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(
                PROCESSING_STARTED_AT,
                ZoneOffset.UTC);
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
