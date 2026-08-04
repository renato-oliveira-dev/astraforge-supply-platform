package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID orderId,
        UUID customerId,
        OrderStatus status,
        int itemCount,
        boolean pricingComplete,
        Optional<BigDecimal> total,
        Optional<Currency> currency,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    public OrderSummaryResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        if (itemCount < 0) {
            throw new IllegalArgumentException(
                    "Item count must not be negative");
        }
        Objects.requireNonNull(
                total,
                "Order total must not be null");
        Objects.requireNonNull(
                currency,
                "Order currency must not be null");
        if (version < 0) {
            throw new IllegalArgumentException(
                    "Order version must not be negative");
        }
        Objects.requireNonNull(createdAt, "Created at must not be null");
        Objects.requireNonNull(updatedAt, "Updated at must not be null");
        if (total.isPresent() != currency.isPresent()) {
            throw new IllegalArgumentException(
                    "Order total and currency must be provided together");
        }
    }
}
