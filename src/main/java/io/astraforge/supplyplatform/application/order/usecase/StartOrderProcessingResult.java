package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StartOrderProcessingResult(
        UUID orderId,
        OrderStatus status,
        UUID startedBy,
        Instant startedAt,
        long version
) {

    public StartOrderProcessingResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(
                startedBy,
                "Started by must not be null");
        Objects.requireNonNull(
                startedAt,
                "Started at must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
