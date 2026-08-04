package io.astraforge.supplyplatform.infrastructure.order.web.error;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ApiFieldError> fieldErrors
) {

    public ApiErrorResponse {
        Objects.requireNonNull(timestamp, "Timestamp must not be null");
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException(
                    "HTTP status must be between 400 and 599");
        }
        error = normalizeRequired(error, "Error");
        message = normalizeRequired(message, "Message");
        path = normalizeRequired(path, "Path");
        fieldErrors = List.copyOf(
                Objects.requireNonNull(
                        fieldErrors,
                        "Field errors must not be null"));
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
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
