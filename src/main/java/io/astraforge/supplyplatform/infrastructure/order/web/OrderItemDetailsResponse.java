package io.astraforge.supplyplatform.infrastructure.order.web;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record OrderItemDetailsResponse(
        UUID orderItemId,
        UUID productId,
        String sku,
        String productName,
        String unitOfMeasure,
        BigDecimal quantity,
        Optional<BigDecimal> unitPrice,
        Optional<BigDecimal> discountPercentage,
        Optional<BigDecimal> taxPercentage,
        Optional<BigDecimal> total,
        Optional<Currency> currency
) {

    public OrderItemDetailsResponse {
        Objects.requireNonNull(
                orderItemId,
                "Order item ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        sku = normalizeRequired(sku, "Product SKU");
        productName = normalizeRequired(productName, "Product name");
        unitOfMeasure = normalizeRequired(
                unitOfMeasure,
                "Unit of measure");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        requireOptional(unitPrice, "Unit price");
        requireOptional(
                discountPercentage,
                "Discount percentage");
        requireOptional(
                taxPercentage,
                "Tax percentage");
        requireOptional(total, "Item total");
        requireOptional(currency, "Currency");
    }

    public boolean priced() {
        return unitPrice.isPresent();
    }

    private static String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank");
        }
        return value.trim();
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
