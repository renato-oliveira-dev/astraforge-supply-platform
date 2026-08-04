package io.astraforge.supplyplatform.infrastructure.order.web.error;

public record ApiFieldError(
        String field,
        String message
) {

    public ApiFieldError {
        field = normalizeRequired(field, "Field");
        message = normalizeRequired(message, "Message");
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
