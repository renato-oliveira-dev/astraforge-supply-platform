package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.ApproveOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RejectOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RequestOrderReviewUseCase;
import io.astraforge.supplyplatform.application.order.usecase.ApproveOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RejectOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestOrderReviewCommand;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalDecisionControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID APPROVER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000002");
    private static final Instant DECIDED_AT =
            Instant.parse("2026-08-03T14:00:00Z");

    @Test
    void testApproveShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<ApprovalDecisionResponse> response =
                fixture.controller().approve(
                        ORDER_ID,
                        new ApprovalDecisionRequest(
                                APPROVER_ID,
                                " correlation-approve-rest-001 "));

        assertThat(fixture.approveUseCase().command())
                .as("approve command delegated by controller")
                .isEqualTo(new ApproveOrderCommand(
                        ORDER_ID,
                        APPROVER_ID,
                        "correlation-approve-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("approve HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("approve response")
                .isEqualTo(new ApprovalDecisionResponse(
                        ORDER_ID,
                        OrderStatus.APPROVED,
                        APPROVER_ID,
                        DECIDED_AT,
                        Optional.empty(),
                        5));
    }

    @Test
    void testRejectShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<ApprovalDecisionResponse> response =
                fixture.controller().reject(
                        ORDER_ID,
                        new ApprovalCommentDecisionRequest(
                                " Budget unavailable. ",
                                APPROVER_ID,
                                " correlation-reject-rest-001 "));

        assertThat(fixture.rejectUseCase().command())
                .as("reject command delegated by controller")
                .isEqualTo(new RejectOrderCommand(
                        ORDER_ID,
                        "Budget unavailable.",
                        APPROVER_ID,
                        "correlation-reject-rest-001"));
        assertThat(response.getBody())
                .as("reject response")
                .isEqualTo(new ApprovalDecisionResponse(
                        ORDER_ID,
                        OrderStatus.REJECTED,
                        APPROVER_ID,
                        DECIDED_AT,
                        Optional.of("Budget unavailable."),
                        5));
    }

    @Test
    void testRequestReviewShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<ApprovalDecisionResponse> response =
                fixture.controller().requestReview(
                        ORDER_ID,
                        new ApprovalCommentDecisionRequest(
                                " Confirm requested quantity. ",
                                APPROVER_ID,
                                " correlation-review-rest-001 "));

        assertThat(fixture.reviewUseCase().command())
                .as("review command delegated by controller")
                .isEqualTo(new RequestOrderReviewCommand(
                        ORDER_ID,
                        "Confirm requested quantity.",
                        APPROVER_ID,
                        "correlation-review-rest-001"));
        assertThat(response.getBody())
                .as("review response")
                .isEqualTo(new ApprovalDecisionResponse(
                        ORDER_ID,
                        OrderStatus.REVIEW_REQUESTED,
                        APPROVER_ID,
                        DECIDED_AT,
                        Optional.of("Confirm requested quantity."),
                        5));
    }

    @Test
    void testConstructorShouldRejectNullDependencies() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> new ApprovalDecisionController(
                null,
                fixture.rejectUseCase(),
                fixture.reviewUseCase()))
                .as("null approve order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Approve order use case must not be null");
        assertThatThrownBy(() -> new ApprovalDecisionController(
                fixture.approveUseCase(),
                null,
                fixture.reviewUseCase()))
                .as("null reject order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Reject order use case must not be null");
        assertThatThrownBy(() -> new ApprovalDecisionController(
                fixture.approveUseCase(),
                fixture.rejectUseCase(),
                null))
                .as("null request review use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Request order review use case must not be null");
    }

    private record Fixture(
            CapturingApproveUseCase approveUseCase,
            CapturingRejectUseCase rejectUseCase,
            CapturingReviewUseCase reviewUseCase,
            ApprovalDecisionController controller
    ) {

        private Fixture() {
            this(
                    new CapturingApproveUseCase(),
                    new CapturingRejectUseCase(),
                    new CapturingReviewUseCase());
        }

        private Fixture(
                CapturingApproveUseCase approveUseCase,
                CapturingRejectUseCase rejectUseCase,
                CapturingReviewUseCase reviewUseCase
        ) {
            this(
                    approveUseCase,
                    rejectUseCase,
                    reviewUseCase,
                    new ApprovalDecisionController(
                            approveUseCase,
                            rejectUseCase,
                            reviewUseCase));
        }
    }

    private static final class CapturingApproveUseCase
            implements ApproveOrderUseCase {

        private final AtomicReference<ApproveOrderCommand> command =
                new AtomicReference<>();

        @Override
        public ApprovalDecisionResult approve(ApproveOrderCommand command) {
            this.command.set(command);
            return new ApprovalDecisionResult(
                    ORDER_ID,
                    OrderStatus.APPROVED,
                    APPROVER_ID,
                    DECIDED_AT,
                    Optional.empty(),
                    5);
        }

        private ApproveOrderCommand command() {
            return command.get();
        }
    }

    private static final class CapturingRejectUseCase
            implements RejectOrderUseCase {

        private final AtomicReference<RejectOrderCommand> command =
                new AtomicReference<>();

        @Override
        public ApprovalDecisionResult reject(RejectOrderCommand command) {
            this.command.set(command);
            return new ApprovalDecisionResult(
                    ORDER_ID,
                    OrderStatus.REJECTED,
                    APPROVER_ID,
                    DECIDED_AT,
                    Optional.of("Budget unavailable."),
                    5);
        }

        private RejectOrderCommand command() {
            return command.get();
        }
    }

    private static final class CapturingReviewUseCase
            implements RequestOrderReviewUseCase {

        private final AtomicReference<RequestOrderReviewCommand> command =
                new AtomicReference<>();

        @Override
        public ApprovalDecisionResult requestReview(
                RequestOrderReviewCommand command
        ) {
            this.command.set(command);
            return new ApprovalDecisionResult(
                    ORDER_ID,
                    OrderStatus.REVIEW_REQUESTED,
                    APPROVER_ID,
                    DECIDED_AT,
                    Optional.of("Confirm requested quantity."),
                    5);
        }

        private RequestOrderReviewCommand command() {
            return command.get();
        }
    }
}
