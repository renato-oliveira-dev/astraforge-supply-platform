package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RetryInventoryReservationResponse(
        UUID orderId,
        OrderStatus status,
        UUID requestedBy,
        Instant requestedAt,
        long version
) {

    public RetryInventoryReservationResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(
                requestedBy,
                "Requested by must not be null");
        Objects.requireNonNull(
                requestedAt,
                "Requested at must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
