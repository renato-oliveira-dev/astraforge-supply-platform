package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RequestInventoryReservationResult(
        UUID orderId,
        OrderStatus status,
        UUID requestedBy,
        Instant requestedAt,
        int itemCount,
        long version
) {

    public RequestInventoryReservationResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(
                requestedBy,
                "Requested by must not be null");
        Objects.requireNonNull(
                requestedAt,
                "Requested at must not be null");
        if (itemCount <= 0) {
            throw new IllegalArgumentException(
                    "Item count must be greater than zero");
        }
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
