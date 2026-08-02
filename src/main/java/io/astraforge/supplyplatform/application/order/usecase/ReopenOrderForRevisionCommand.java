package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record ReopenOrderForRevisionCommand(
        UUID orderId,
        UUID reopenedBy,
        String correlationId
) {

    public ReopenOrderForRevisionCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                reopenedBy,
                "Reopened by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
