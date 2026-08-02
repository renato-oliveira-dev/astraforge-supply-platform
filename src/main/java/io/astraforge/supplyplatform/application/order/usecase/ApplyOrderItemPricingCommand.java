package io.astraforge.supplyplatform.application.order.usecase;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record ApplyOrderItemPricingCommand(
        UUID orderId,
        UUID orderItemId,
        BigDecimal unitPrice,
        Currency currency,
        BigDecimal discountPercentage,
        BigDecimal taxPercentage,
        UUID pricedBy,
        String correlationId
) {

    public ApplyOrderItemPricingCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                orderItemId,
                "Order item ID must not be null");
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        Objects.requireNonNull(
                discountPercentage,
                "Discount percentage must not be null");
        Objects.requireNonNull(
                taxPercentage,
                "Tax percentage must not be null");
        Objects.requireNonNull(pricedBy, "Priced by must not be null");
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException(
                    "Correlation ID must not be blank");
        }
        correlationId = correlationId.trim();
    }
}
