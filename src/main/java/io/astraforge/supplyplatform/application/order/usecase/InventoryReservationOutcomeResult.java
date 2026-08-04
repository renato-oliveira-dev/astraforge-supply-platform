package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record InventoryReservationOutcomeResult(
        UUID orderId,
        OrderStatus status,
        UUID recordedBy,
        Instant recordedAt,
        Optional<String> failureReason,
        long version
) {

    public InventoryReservationOutcomeResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(
                recordedBy,
                "Recorded by must not be null");
        Objects.requireNonNull(
                recordedAt,
                "Recorded at must not be null");
        Objects.requireNonNull(
                failureReason,
                "Failure reason must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
