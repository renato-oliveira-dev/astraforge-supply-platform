package io.astraforge.supplyplatform.domain.order.valueobject;

public record CancellationReason(String value) {

    private static final int MAX_LENGTH = 500;

    public CancellationReason {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Cancellation reason must not be blank");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Cancellation reason must not exceed 500 characters");
        }
    }
}
