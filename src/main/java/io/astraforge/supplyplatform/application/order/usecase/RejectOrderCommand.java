package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record RejectOrderCommand(
        UUID orderId,
        String comment,
        UUID rejectedBy,
        String correlationId
) {

    public RejectOrderCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        comment = normalizeRequired(comment, "Rejection comment");
        Objects.requireNonNull(rejectedBy, "Rejected by must not be null");
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
