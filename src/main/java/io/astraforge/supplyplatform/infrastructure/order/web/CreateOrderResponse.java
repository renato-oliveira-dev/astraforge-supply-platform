package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        OrderStatus status,
        long version,
        Instant createdAt
) {
    public CreateOrderResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (version < 0) {
            throw new IllegalArgumentException("Order version must not be negative");
        }
        Objects.requireNonNull(createdAt, "Created at must not be null");
    }
}
