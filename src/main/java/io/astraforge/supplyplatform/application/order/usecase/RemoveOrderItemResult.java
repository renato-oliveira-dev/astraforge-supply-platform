package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RemoveOrderItemResult(
        UUID orderId,
        UUID removedOrderItemId,
        OrderStatus status,
        int itemCount,
        long version,
        Instant updatedAt
) {

    public RemoveOrderItemResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                removedOrderItemId,
                "Removed order item ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (itemCount < 0) {
            throw new IllegalArgumentException(
                    "Item count must not be negative");
        }
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
        Objects.requireNonNull(updatedAt, "Updated at must not be null");
    }
}
