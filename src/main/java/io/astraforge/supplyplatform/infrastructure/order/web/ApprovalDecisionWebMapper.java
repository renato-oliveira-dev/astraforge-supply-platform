package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.ApproveOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RejectOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestOrderReviewCommand;

import java.util.UUID;

final class ApprovalDecisionWebMapper {

    private ApprovalDecisionWebMapper() {
    }

    static ApproveOrderCommand toApproveCommand(
            UUID orderId,
            ApprovalDecisionRequest request
    ) {
        return new ApproveOrderCommand(
                orderId,
                request.decidedBy(),
                request.correlationId());
    }

    static RejectOrderCommand toRejectCommand(
            UUID orderId,
            ApprovalCommentDecisionRequest request
    ) {
        return new RejectOrderCommand(
                orderId,
                request.comment(),
                request.decidedBy(),
                request.correlationId());
    }

    static RequestOrderReviewCommand toReviewCommand(
            UUID orderId,
            ApprovalCommentDecisionRequest request
    ) {
        return new RequestOrderReviewCommand(
                orderId,
                request.comment(),
                request.decidedBy(),
                request.correlationId());
    }

    static ApprovalDecisionResponse toResponse(
            ApprovalDecisionResult result
    ) {
        return new ApprovalDecisionResponse(
                result.orderId(),
                result.status(),
                result.decidedBy(),
                result.decidedAt(),
                result.comment(),
                result.version());
    }
}
