package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record CancelOrderCommand(
        UUID orderId,
        String reason,
        UUID cancelledBy,
        String correlationId
) {

    public CancelOrderCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        reason = normalizeRequired(reason, "Cancellation reason");
        Objects.requireNonNull(
                cancelledBy,
                "Cancelled by must not be null");
        correlationId = normalizeRequired(
                correlationId,
                "Correlation ID");
    }

    private static String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank");
        }
        return value.trim();
    }
}
