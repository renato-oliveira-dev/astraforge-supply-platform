package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record CreateOrderCommand(
        UUID customerId,
        UUID createdBy,
        String correlationId
) {

    public CreateOrderCommand {
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(createdBy, "Created by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
