package io.astraforge.supplyplatform.domain.order.valueobject;

public record InventoryFailureReason(String value) {

    private static final int MAX_LENGTH = 500;

    public InventoryFailureReason {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Inventory failure reason must not be blank");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Inventory failure reason must not exceed 500 characters");
        }
    }
}
