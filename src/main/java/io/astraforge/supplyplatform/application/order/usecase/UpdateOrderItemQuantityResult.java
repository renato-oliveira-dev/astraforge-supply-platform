package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record UpdateOrderItemQuantityResult(
        UUID orderId,
        UUID orderItemId,
        BigDecimal quantity,
        OrderStatus status,
        long version,
        Instant updatedAt
) {

    public UpdateOrderItemQuantityResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                orderItemId,
                "Order item ID must not be null");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
        Objects.requireNonNull(updatedAt, "Updated at must not be null");
    }
}
