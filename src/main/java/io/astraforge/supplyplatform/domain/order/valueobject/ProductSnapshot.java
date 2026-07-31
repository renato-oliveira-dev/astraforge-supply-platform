package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;

import java.util.Objects;

public record ProductSnapshot(
        ProductReference productReference,
        String sku,
        String name,
        String unitOfMeasure
) {

    private static final int SKU_MAX_LENGTH = 80;
    private static final int NAME_MAX_LENGTH = 200;
    private static final int UNIT_OF_MEASURE_MAX_LENGTH = 30;

    public ProductSnapshot {
        Objects.requireNonNull(productReference, "Product reference must not be null");
        sku = normalizeRequired(sku, "Product SKU", SKU_MAX_LENGTH);
        name = normalizeRequired(name, "Product name", NAME_MAX_LENGTH);
        unitOfMeasure = normalizeRequired(
                unitOfMeasure,
                "Product unit of measure",
                UNIT_OF_MEASURE_MAX_LENGTH);
    }

    public ProductId productId() {
        return productReference.productId();
    }

    private static String normalizeRequired(String value, String fieldName, int maximumLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(fieldName + " must not be blank");
        }

        String normalized = value.trim();
        if (normalized.length() > maximumLength) {
            throw new DomainValidationException(
                    fieldName + " must not exceed " + maximumLength + " characters");
        }
        return normalized;
    }
}
