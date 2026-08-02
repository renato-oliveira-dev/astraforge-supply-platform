package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record ConfirmInventoryReservationCommand(
        UUID orderId,
        UUID recordedBy,
        String correlationId
) {

    public ConfirmInventoryReservationCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                recordedBy,
                "Recorded by must not be null");
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
