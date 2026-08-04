package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class CorrelationIdFilter extends OncePerRequestFilter
        implements Ordered {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    public static final String REQUEST_ATTRIBUTE =
            CorrelationIdFilter.class.getName() + ".correlationId";


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
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(
                request.getHeader(HEADER_NAME));

        request.setAttribute(REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        MDC.put(MDC_KEY, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String resolveCorrelationId(String headerValue) {
        if (headerValue != null && !headerValue.isBlank()) {
            try {
                return new CorrelationId(headerValue).value();
            } catch (DomainValidationException exception) {
                // Invalid external identifiers are replaced instead of
                // propagating unsafe values to headers, logs, or MDC.
            }
        }

        UUID generatedValue = Objects.requireNonNull(
                uuidSupplier.get(),
                "UUID supplier must not return null");
        return generatedValue.toString();
    }
}
