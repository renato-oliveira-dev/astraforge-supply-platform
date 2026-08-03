package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record CompleteOrderFulfillmentResult(
        UUID orderId,
        OrderStatus status,
        UUID completedBy,
        Instant completedAt,
        int itemCount,
        BigDecimal total,
        Currency currency,
        long version
) {

    public CompleteOrderFulfillmentResult {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(
                completedBy,
                "Completed by must not be null");
        Objects.requireNonNull(
                completedAt,
                "Completed at must not be null");
        if (itemCount <= 0) {
            throw new IllegalArgumentException(
                    "Item count must be greater than zero");
        }
        Objects.requireNonNull(total, "Order total must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
