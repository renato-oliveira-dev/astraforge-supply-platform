package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private static final int MAX_CORRELATION_ID_LENGTH = 100;

    private final Supplier<UUID> uuidSupplier;

    public CorrelationIdFilter() {
        this(UUID::randomUUID);
    }

    CorrelationIdFilter(Supplier<UUID> uuidSupplier) {
        this.uuidSupplier = Objects.requireNonNull(
                uuidSupplier,
                "UUID supplier must not be null");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(
                request.getHeader(HEADER_NAME));

        response.setHeader(HEADER_NAME, correlationId);
        MDC.put(MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String resolveCorrelationId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            UUID generatedValue = Objects.requireNonNull(
                    uuidSupplier.get(),
                    "UUID supplier must not return null");
            return generatedValue.toString();
        }

        String normalized = headerValue.trim();
        if (normalized.length() > MAX_CORRELATION_ID_LENGTH) {
            return normalized.substring(0, MAX_CORRELATION_ID_LENGTH);
        }
        return normalized;
    }
}
