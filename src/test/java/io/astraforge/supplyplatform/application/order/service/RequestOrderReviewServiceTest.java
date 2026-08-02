package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RequestOrderReviewCommand;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.DECIDED_AT;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.repositoryWithPendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestOrderReviewServiceTest {

    @Test
    void testRequestReviewShouldPersistReviewRequestedOrder() {
        RequestOrderReviewService service =
                new RequestOrderReviewService(
                        repositoryWithPendingOrder(),
                        Clock.fixed(DECIDED_AT, ZoneOffset.UTC));

        ApprovalDecisionResult result =
                service.requestReview(command());

        assertThat(result.status())
                .as("review-requested order status")
                .isEqualTo(OrderStatus.REVIEW_REQUESTED);
        assertThat(result.comment())
                .as("review request comment")
                .contains("Confirm the requested quantity.");
        assertThat(result.decidedBy())
                .as("review request actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.version())
                .as("review-requested order version")
                .isEqualTo(5L);
    }

    @Test
    void testRequestReviewCommandShouldRejectBlankComment() {
        assertThatThrownBy(() -> new RequestOrderReviewCommand(
                ORDER_ID,
                " ",
                APPROVER_ID,
                "correlation-review-001"))
                .as("blank review request comment")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Review comment must not be blank");
    }

    private static RequestOrderReviewCommand command() {
        return new RequestOrderReviewCommand(
                ORDER_ID,
                " Confirm the requested quantity. ",
                APPROVER_ID,
                "correlation-review-001");
    }
}
