package io.astraforge.supplyplatform.infrastructure.order.web;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class OrderApiObservationFilter extends OncePerRequestFilter
        implements Ordered {

    static final String REQUEST_TIMER = "astraforge.order.api.requests";
    static final String ERROR_COUNTER = "astraforge.order.api.errors";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OrderApiObservationFilter.class);
    private static final String ORDER_API_PREFIX = "/api/v1/orders";

    private final MeterRegistry meterRegistry;

    public OrderApiObservationFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(
                meterRegistry,
                "Meter registry must not be null");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri == null
                || !requestUri.startsWith(ORDER_API_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            recordRequest(
                    request,
                    response,
                    System.nanoTime() - startedAt);
        }
    }

    private void recordRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            long elapsedNanos
    ) {
        String method = normalizeMethod(request.getMethod());
        String outcome = outcome(response.getStatus());
        String correlationId = correlationId(request);

        Timer.builder(REQUEST_TIMER)
                .description("Order API request execution time")
                .tag("method", method)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);

        if (response.getStatus() >= HttpServletResponse.SC_BAD_REQUEST) {
            Counter.builder(ERROR_COUNTER)
                    .description("Order API error responses")
                    .tag("method", method)
                    .tag("outcome", outcome)
                    .register(meterRegistry)
                    .increment();
        }

        logRequest(
                method,
                request.getRequestURI(),
                response.getStatus(),
                outcome,
                elapsedNanos,
                correlationId);
    }

    private static void logRequest(
            String method,
            String requestUri,
            int status,
            String outcome,
            long elapsedNanos,
            String correlationId
    ) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

        if (status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            LOGGER.error(
                    "Order API request failed: method={}, path={}, "
                            + "status={}, outcome={}, elapsedMs={}, "
                            + "correlationId={}",
                    method,
                    requestUri,
                    status,
                    outcome,
                    elapsedMs,
                    correlationId);
            return;
        }

        if (status >= HttpServletResponse.SC_BAD_REQUEST) {
            LOGGER.warn(
                    "Order API request rejected: method={}, path={}, "
                            + "status={}, outcome={}, elapsedMs={}, "
                            + "correlationId={}",
                    method,
                    requestUri,
                    status,
                    outcome,
                    elapsedMs,
                    correlationId);
            return;
        }

        LOGGER.info(
                "Order API request completed: method={}, path={}, "
                        + "status={}, outcome={}, elapsedMs={}, "
                        + "correlationId={}",
                method,
                requestUri,
                status,
                outcome,
                elapsedMs,
                correlationId);
    }

    private static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "UNKNOWN";
        }
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private static String outcome(int status) {
        if (status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            return "SERVER_ERROR";
        }
        if (status >= HttpServletResponse.SC_BAD_REQUEST) {
            return "CLIENT_ERROR";
        }
        if (status >= HttpServletResponse.SC_MULTIPLE_CHOICES) {
            return "REDIRECTION";
        }
        return "SUCCESS";
    }

    private static String correlationId(HttpServletRequest request) {
        Object attribute = request.getAttribute(
                CorrelationIdFilter.REQUEST_ATTRIBUTE);
        if (attribute instanceof String value && !value.isBlank()) {
            return value.trim();
        }

        String header = request.getHeader(CorrelationIdFilter.HEADER_NAME);
        if (header != null && !header.isBlank()) {
            return header.trim();
        }

        return "unavailable";
    }
}
