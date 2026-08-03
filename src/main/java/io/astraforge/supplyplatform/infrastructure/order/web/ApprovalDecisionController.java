package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.ApproveOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RejectOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RequestOrderReviewUseCase;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/approval-decisions")
public final class ApprovalDecisionController {

    private final ApproveOrderUseCase approveOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final RequestOrderReviewUseCase requestOrderReviewUseCase;

    public ApprovalDecisionController(
            ApproveOrderUseCase approveOrderUseCase,
            RejectOrderUseCase rejectOrderUseCase,
            RequestOrderReviewUseCase requestOrderReviewUseCase
    ) {
        this.approveOrderUseCase = Objects.requireNonNull(
                approveOrderUseCase,
                "Approve order use case must not be null");
        this.rejectOrderUseCase = Objects.requireNonNull(
                rejectOrderUseCase,
                "Reject order use case must not be null");
        this.requestOrderReviewUseCase = Objects.requireNonNull(
                requestOrderReviewUseCase,
                "Request order review use case must not be null");
    }

    @PostMapping("/approval")
    public ResponseEntity<ApprovalDecisionResponse> approve(
            @PathVariable UUID orderId,
            @Valid @RequestBody ApprovalDecisionRequest request
    ) {
        ApprovalDecisionResult result = approveOrderUseCase.approve(
                ApprovalDecisionWebMapper.toApproveCommand(
                        orderId,
                        request));

        return ResponseEntity.ok(
                ApprovalDecisionWebMapper.toResponse(result));
    }

    @PostMapping("/rejection")
    public ResponseEntity<ApprovalDecisionResponse> reject(
            @PathVariable UUID orderId,
            @Valid @RequestBody ApprovalCommentDecisionRequest request
    ) {
        ApprovalDecisionResult result = rejectOrderUseCase.reject(
                ApprovalDecisionWebMapper.toRejectCommand(
                        orderId,
                        request));

        return ResponseEntity.ok(
                ApprovalDecisionWebMapper.toResponse(result));
    }

    @PostMapping("/review-request")
    public ResponseEntity<ApprovalDecisionResponse> requestReview(
            @PathVariable UUID orderId,
            @Valid @RequestBody ApprovalCommentDecisionRequest request
    ) {
        ApprovalDecisionResult result =
                requestOrderReviewUseCase.requestReview(
                        ApprovalDecisionWebMapper.toReviewCommand(
                                orderId,
                                request));

        return ResponseEntity.ok(
                ApprovalDecisionWebMapper.toResponse(result));
    }
}
