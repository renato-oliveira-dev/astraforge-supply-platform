package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ReopenOrderForRevisionResult(
        UUID orderId,
        OrderStatus status,
        long version,
        Instant reopenedAt
) {

    public ReopenOrderForRevisionResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
        Objects.requireNonNull(
                reopenedAt,
                "Reopened at must not be null");
    }
}
