package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CorrelationIdFilterTest {

    private static final UUID GENERATED_ID =
            UUID.fromString("50000000-0000-0000-0000-000000000001");

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void testFilterShouldReuseNormalizedRequestHeader() throws Exception {
        CorrelationIdFilter filter =
                new CorrelationIdFilter(() -> GENERATED_ID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                " correlation-client-001 ");
        MockHttpServletResponse response =
                new MockHttpServletResponse();
        AtomicReference<String> capturedMdc = new AtomicReference<>();

        filter.doFilter(
                request,
                response,
                capturingChain(capturedMdc));

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .as("correlation response header")
                .isEqualTo("correlation-client-001");
        assertThat(capturedMdc.get())
                .as("correlation value available during request")
                .isEqualTo("correlation-client-001");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY))
                .as("correlation MDC value after request")
                .isNull();
    }

    @Test
    void testFilterShouldGenerateCorrelationIdWhenHeaderIsAbsent()
            throws Exception {
        CorrelationIdFilter filter =
                new CorrelationIdFilter(() -> GENERATED_ID);
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                new MockHttpServletRequest(),
                response,
                capturingChain(new AtomicReference<>()));

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .as("generated correlation response header")
                .isEqualTo(GENERATED_ID.toString());
    }

    @Test
    void testFilterShouldLimitOversizedCorrelationId() throws Exception {
        CorrelationIdFilter filter =
                new CorrelationIdFilter(() -> GENERATED_ID);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                CorrelationIdFilter.HEADER_NAME,
                "x".repeat(120));
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                capturingChain(new AtomicReference<>()));

        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME))
                .as("limited correlation response header")
                .hasSize(100);
    }

    @Test
    void testConstructorShouldRejectNullSupplier() {
        assertThatThrownBy(() -> new CorrelationIdFilter(null))
                .as("null correlation UUID supplier")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UUID supplier must not be null");
    }

    @Test
    void testFilterShouldRejectNullGeneratedUuid() {
        CorrelationIdFilter filter =
                new CorrelationIdFilter(() -> null);

        assertThatThrownBy(() -> filter.doFilter(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                mock(FilterChain.class)))
                .as("null generated correlation UUID")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UUID supplier must not return null");
    }

    private static FilterChain capturingChain(
            AtomicReference<String> capturedMdc
    ) {
        return (request, response) -> capturedMdc.set(
                MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}
