package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record RequestOrderReviewCommand(
        UUID orderId,
        String comment,
        UUID requestedBy,
        String correlationId
) {

    public RequestOrderReviewCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        comment = normalizeRequired(comment, "Review comment");
        Objects.requireNonNull(requestedBy, "Requested by must not be null");
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
