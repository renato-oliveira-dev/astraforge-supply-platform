package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record ApproveOrderCommand(
        UUID orderId,
        UUID approvedBy,
        String correlationId
) {

    public ApproveOrderCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(approvedBy, "Approved by must not be null");
        correlationId = normalizeCorrelationId(correlationId);
    }

    private static String normalizeCorrelationId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        return value.trim();
    }
}
