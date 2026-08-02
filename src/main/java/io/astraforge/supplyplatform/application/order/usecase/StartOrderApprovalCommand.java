package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record StartOrderApprovalCommand(
        UUID orderId,
        UUID startedBy,
        String correlationId
) {

    public StartOrderApprovalCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                startedBy,
                "Started by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
