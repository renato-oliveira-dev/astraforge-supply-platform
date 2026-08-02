package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record SubmitOrderCommand(
        UUID orderId,
        UUID submittedBy,
        String correlationId
) {

    public SubmitOrderCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                submittedBy,
                "Submitted by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
