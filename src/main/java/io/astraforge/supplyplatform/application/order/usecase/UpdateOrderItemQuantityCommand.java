package io.astraforge.supplyplatform.application.order.usecase;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record UpdateOrderItemQuantityCommand(
        UUID orderId,
        UUID orderItemId,
        BigDecimal quantity,
        UUID changedBy,
        String correlationId
) {

    public UpdateOrderItemQuantityCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                orderItemId,
                "Order item ID must not be null");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        Objects.requireNonNull(changedBy, "Changed by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
