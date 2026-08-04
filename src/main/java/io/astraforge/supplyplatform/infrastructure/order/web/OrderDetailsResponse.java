package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record OrderDetailsResponse(
        UUID orderId,
        UUID customerId,
        OrderStatus status,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        long version,
        List<OrderItemDetailsResponse> items,
        boolean pricingComplete,
        Optional<BigDecimal> subtotal,
        Optional<BigDecimal> discount,
        Optional<BigDecimal> tax,
        Optional<BigDecimal> total,
        Optional<Currency> currency
) {

    public OrderDetailsResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(createdBy, "Created by must not be null");
        Objects.requireNonNull(createdAt, "Created at must not be null");
        Objects.requireNonNull(updatedAt, "Updated at must not be null");
        if (version < 0) {
            throw new IllegalArgumentException(
                    "Order version must not be negative");
        }
        items = List.copyOf(
                Objects.requireNonNull(
                        items,
                        "Order items must not be null"));
        requireOptional(subtotal, "Subtotal");
        requireOptional(discount, "Discount");
        requireOptional(tax, "Tax");
        requireOptional(total, "Total");
        requireOptional(currency, "Currency");
    }

    private static <T> Optional<T> requireOptional(
            Optional<T> value,
            String fieldName
    ) {
        return Objects.requireNonNull(
                value,
                fieldName + " must not be null");
    }
}
