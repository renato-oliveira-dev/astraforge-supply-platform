package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionCommand;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderRevisionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.ApprovalComment;
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

class ReopenOrderForRevisionServiceTest {

    private static final UUID REQUESTER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant REVIEW_REQUESTED_AT =
            Instant.parse("2026-08-01T20:25:00Z");
    private static final Instant REOPENED_AT =
            Instant.parse("2026-08-01T20:30:00Z");

    @Test
    void testReopenShouldPersistDraftOrderAndClearActiveDecision() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithReviewRequestedOrder();
        ReopenOrderForRevisionService service =
                new ReopenOrderForRevisionService(
                        repository,
                        fixedClock());

        ReopenOrderForRevisionResult result =
                service.reopen(command());

        Order savedOrder = repository.findById(new OrderId(ORDER_ID))
                .orElseThrow();
        assertThat(result.orderId())
                .as("reopened order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.status())
                .as("reopened order status")
                .isEqualTo(OrderStatus.DRAFT);
        assertThat(result.version())
                .as("reopened order version")
                .isEqualTo(6L);
        assertThat(result.reopenedAt())
                .as("revision reopening timestamp")
                .isEqualTo(REOPENED_AT);
        assertThat(savedOrder.decisionBy())
                .as("active decision actor after reopening")
                .isEmpty();
        assertThat(savedOrder.decisionAt())
                .as("active decision timestamp after reopening")
                .isEmpty();
        assertThat(savedOrder.decisionComment())
                .as("active decision comment after reopening")
                .isEmpty();
    }

    @Test
    void testReopenShouldRejectUnknownOrder() {
        ReopenOrderForRevisionService service =
                new ReopenOrderForRevisionService(
                        new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                        fixedClock());

        var sonarArgument1Value1 = command();
        assertThatThrownBy(() -> service.reopen(sonarArgument1Value1))
                .as("revision reopening for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testReopenShouldRejectOrderOutsideReviewRequestedStatus() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        repository.save(pendingOrder());
        ReopenOrderForRevisionService service =
                new ReopenOrderForRevisionService(
                        repository,
                        fixedClock());

        var sonarArgument2Value1 = command();
        assertThatThrownBy(() -> service.reopen(sonarArgument2Value1))
                .as("revision reopening requires review-requested status")
                .isInstanceOf(OrderRevisionNotAllowedException.class)
                .hasMessage(
                        "Only a REVIEW_REQUESTED order can be reopened for revision");
    }

    @Test
    void testReopenShouldRejectNullCommand() {
        ReopenOrderForRevisionService service =
                new ReopenOrderForRevisionService(
                        repositoryWithReviewRequestedOrder(),
                        fixedClock());

        assertThatThrownBy(() -> service.reopen(null))
                .as("null revision reopening command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Reopen order for revision command must not be null");
    }

    private static ReopenOrderForRevisionCommand command() {
        return new ReopenOrderForRevisionCommand(
                ORDER_ID,
                REQUESTER_ID,
                " correlation-reopen-revision-001 ");
    }

    private static Clock fixedClock() {
        return Clock.fixed(REOPENED_AT, ZoneOffset.UTC);
    }

    private static ApprovalDecisionTestFixture.InMemoryOrderRepository
            repositoryWithReviewRequestedOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                new ApprovalDecisionTestFixture.InMemoryOrderRepository();
        Order order = pendingOrder();
        order.requestReview(
                new ApprovalComment(
                        "Confirm the requested quantity."),
                new UserId(APPROVER_ID),
                REVIEW_REQUESTED_AT,
                new CorrelationId(
                        "correlation-request-review-001"));
        repository.save(order);
        return repository;
    }
}
