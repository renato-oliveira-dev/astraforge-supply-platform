package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record RemoveOrderItemCommand(
        UUID orderId,
        UUID orderItemId,
        UUID removedBy,
        String correlationId
) {

    public RemoveOrderItemCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                orderItemId,
                "Order item ID must not be null");
        Objects.requireNonNull(removedBy, "Removed by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
