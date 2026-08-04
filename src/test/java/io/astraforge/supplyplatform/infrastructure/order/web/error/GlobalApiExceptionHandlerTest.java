package io.astraforge.supplyplatform.infrastructure.order.web.error;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import io.astraforge.supplyplatform.domain.order.exception.OrderProcessingNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.infrastructure.order.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalApiExceptionHandlerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final Instant NOW =
            Instant.parse("2026-08-03T22:40:00Z");
    private static final String PATH =
            "/api/v1/orders/" + ORDER_ID;
    private static final String CORRELATION_ID =
            "correlation-error-001";

    @Test
    void testHandleNotFoundShouldReturnStandardizedResponse() {
        GlobalApiExceptionHandler handler = handler();
        HttpServletRequest request = request();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFound(
                        new OrderNotFoundException(
                                new OrderId(ORDER_ID)),
                        request);

        assertThat(response.getStatusCode())
                .as("not found HTTP status")
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .as("not found response body")
                .isEqualTo(new ApiErrorResponse(
                        NOW,
                        404,
                        "Not Found",
                        "Order not found: " + ORDER_ID,
                        PATH,
                        CORRELATION_ID,
                        java.util.List.of()));
    }

    @Test
    void testHandleBusinessValidationShouldReturnUnprocessableEntity() {
        GlobalApiExceptionHandler handler = handler();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBusinessValidation(
                        new DomainValidationException(
                                "Quantity must be greater than zero"),
                        request());

        assertThat(response.getStatusCode())
                .as("business validation HTTP status")
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody().message())
                .as("business validation message")
                .isEqualTo("Quantity must be greater than zero");
        assertThat(response.getBody().hasFieldErrors())
                .as("business validation field error state")
                .isFalse();
    }

    @Test
    void testHandleStateViolationShouldReturnUnprocessableEntity() {
        GlobalApiExceptionHandler handler = handler();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleBusinessValidation(
                        new OrderProcessingNotAllowedException(
                                "Only an APPROVED order can start processing"),
                        request());

        assertThat(response.getStatusCode())
                .as("domain state violation HTTP status")
                .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody().message())
                .as("domain state violation message")
                .isEqualTo(
                        "Only an APPROVED order can start processing");
        assertThat(response.getBody().hasFieldErrors())
                .as("domain state violation field error state")
                .isFalse();
    }

    @Test
    void testHandleUnexpectedShouldHideExceptionDetails() {
        GlobalApiExceptionHandler handler = handler();

        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpected(
                        new IllegalStateException(
                                "Database password=secret"),
                        request());

        assertThat(response.getStatusCode())
                .as("unexpected error HTTP status")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message())
                .as("unexpected error safe message")
                .isEqualTo("Unexpected error");
    }

    @Test
    void testConstructorShouldRejectNullClock() {
        assertThatThrownBy(() ->
                new GlobalApiExceptionHandler(null))
                .as("null API error clock")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Clock must not be null");
    }

    private static GlobalApiExceptionHandler handler() {
        return new GlobalApiExceptionHandler(
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(PATH);
        when(request.getAttribute(
                CorrelationIdFilter.REQUEST_ATTRIBUTE))
                .thenReturn(CORRELATION_ID);
        return request;
    }
}
