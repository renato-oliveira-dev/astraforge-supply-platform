package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record CompleteOrderFulfillmentCommand(
        UUID orderId,
        UUID completedBy,
        String correlationId
) {

    public CompleteOrderFulfillmentCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                completedBy,
                "Completed by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
