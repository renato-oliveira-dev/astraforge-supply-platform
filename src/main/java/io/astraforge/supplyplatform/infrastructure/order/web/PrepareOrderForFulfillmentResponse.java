package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PrepareOrderForFulfillmentResponse(
        UUID orderId,
        OrderStatus status,
        UUID preparedBy,
        Instant preparedAt,
        int itemCount,
        long version
) {

    public PrepareOrderForFulfillmentResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(preparedBy, "Prepared by must not be null");
        Objects.requireNonNull(preparedAt, "Prepared at must not be null");
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
