package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CancelOrderResult(
        UUID orderId,
        OrderStatus status,
        String reason,
        UUID cancelledBy,
        Instant cancelledAt,
        long version
) {

    public CancelOrderResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason must not be blank");
        }
        reason = reason.trim();
        Objects.requireNonNull(
                cancelledBy,
                "Cancelled by must not be null");
        Objects.requireNonNull(
                cancelledAt,
                "Cancelled at must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
