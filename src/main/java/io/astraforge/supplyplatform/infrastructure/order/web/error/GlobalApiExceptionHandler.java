package io.astraforge.supplyplatform.infrastructure.order.web.error;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.domain.order.exception.DomainStateException;
import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import io.astraforge.supplyplatform.infrastructure.order.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public final class GlobalApiExceptionHandler {

    private static final String INVALID_REQUEST = "Invalid request";
    private static final String UNEXPECTED_ERROR = "Unexpected error";

    private final Clock clock;

    public GlobalApiExceptionHandler(Clock clock) {
        this.clock = Objects.requireNonNull(
                clock,
                "Clock must not be null");
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            OrderNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler({
            DomainValidationException.class,
            DomainStateException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessValidation(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage(),
                request,
                List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ApiFieldError> fieldErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(GlobalApiExceptionHandler::toFieldError)
                .toList();

        return response(
                HttpStatus.BAD_REQUEST,
                INVALID_REQUEST,
                request,
                fieldErrors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                INVALID_REQUEST,
                request,
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                UNEXPECTED_ERROR,
                request,
                List.of());
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ApiFieldError> fieldErrors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                clock.instant(),
                status.value(),
                status.getReasonPhrase(),
                safeMessage(message),
                request.getRequestURI(),
                resolveCorrelationId(request),
                fieldErrors);

        return ResponseEntity.status(status).body(body);
    }

    private static String resolveCorrelationId(
            HttpServletRequest request
    ) {
        Object requestAttribute = request.getAttribute(
                CorrelationIdFilter.REQUEST_ATTRIBUTE);
        if (requestAttribute instanceof String correlationId
                && !correlationId.isBlank()) {
            return correlationId.trim();
        }

        String requestHeader = request.getHeader(
                CorrelationIdFilter.HEADER_NAME);
        if (requestHeader != null && !requestHeader.isBlank()) {
            return requestHeader.trim();
        }

        return "unavailable";
    }

    private static ApiFieldError toFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        if (message == null || message.isBlank()) {
            message = INVALID_REQUEST;
        }
        return new ApiFieldError(fieldError.getField(), message);
    }

    private static String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return UNEXPECTED_ERROR;
        }
        return message.trim();
    }
}
