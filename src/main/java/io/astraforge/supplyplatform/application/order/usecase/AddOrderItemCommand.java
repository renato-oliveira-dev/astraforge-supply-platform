package io.astraforge.supplyplatform.application.order.usecase;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record AddOrderItemCommand(
        UUID orderId,
        UUID productId,
        String sku,
        String productName,
        String unitOfMeasure,
        BigDecimal quantity,
        UUID addedBy,
        String correlationId
) {

    public AddOrderItemCommand {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        sku = normalizeRequired(sku, "Product SKU");
        productName = normalizeRequired(productName, "Product name");
        unitOfMeasure = normalizeRequired(
                unitOfMeasure,
                "Unit of measure");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        Objects.requireNonNull(addedBy, "Added by must not be null");
        correlationId = normalizeRequired(
                correlationId,
                "Correlation ID");
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
}
