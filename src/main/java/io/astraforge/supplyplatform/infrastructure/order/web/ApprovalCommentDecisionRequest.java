package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ApprovalCommentDecisionRequest(
        @NotBlank @Size(max = 500) String comment,
        @NotNull UUID decidedBy,
        @NotBlank @Size(max = 100) String correlationId
) {
}
