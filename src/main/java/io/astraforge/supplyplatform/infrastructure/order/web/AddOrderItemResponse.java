package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AddOrderItemResponse(
        UUID orderId,
        UUID orderItemId,
        UUID productId,
        OrderStatus status,
        int itemCount,
        long version,
        Instant updatedAt
) {

    public AddOrderItemResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                orderItemId,
                "Order item ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (itemCount <= 0) {
            throw new IllegalArgumentException(
                    "Item count must be greater than zero");
        }
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
        Objects.requireNonNull(updatedAt, "Updated at must not be null");
    }
}
