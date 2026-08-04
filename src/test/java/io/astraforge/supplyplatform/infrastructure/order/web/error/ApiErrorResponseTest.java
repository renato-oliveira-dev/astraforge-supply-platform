package io.astraforge.supplyplatform.infrastructure.order.web.error;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiErrorResponseTest {

    private static final Instant NOW =
            Instant.parse("2026-08-03T22:40:00Z");

    @Test
    void testConstructorShouldDefensivelyCopyFieldErrors() {
        List<ApiFieldError> mutableErrors = new ArrayList<>();
        mutableErrors.add(new ApiFieldError(
                "customerId",
                "must not be null"));

        ApiErrorResponse response = new ApiErrorResponse(
                NOW,
                400,
                "Bad Request",
                "Invalid request",
                "/api/v1/orders",
                "correlation-error-001",
                mutableErrors);
        mutableErrors.clear();

        assertThat(response.fieldErrors())
                .as("defensively copied field errors")
                .hasSize(1);
        assertThat(response.hasFieldErrors())
                .as("field error state")
                .isTrue();
        assertThatThrownBy(() -> response.fieldErrors().clear())
                .as("immutable field errors")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testConstructorShouldRejectSuccessfulHttpStatus() {
        assertThatThrownBy(() -> new ApiErrorResponse(
                NOW,
                200,
                "OK",
                "Invalid request",
                "/api/v1/orders",
                "correlation-error-001",
                List.of()))
                .as("successful status in API error response")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "HTTP status must be between 400 and 599");
    }
}
