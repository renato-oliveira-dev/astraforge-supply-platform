package io.astraforge.supplyplatform.infrastructure.order.web;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderApiObservationFilterTest {

    private static final String CORRELATION_ID =
            "correlation-observation-001";

    @Test
    void testFilterShouldRecordSuccessfulOrderRequest() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OrderApiObservationFilter filter =
                new OrderApiObservationFilter(registry);
        MockHttpServletRequest request = orderRequest("POST");
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                successfulChain());

        Timer timer = registry.get(
                        OrderApiObservationFilter.REQUEST_TIMER)
                .tag("method", "POST")
                .tag("outcome", "SUCCESS")
                .timer();

        assertThat(timer.count())
                .as("successful order API timer count")
                .isEqualTo(1L);
        assertThat(registry.find(
                        OrderApiObservationFilter.ERROR_COUNTER)
                .counter())
                .as("successful request error counter")
                .isNull();
    }

    @Test
    void testFilterShouldRecordClientError() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OrderApiObservationFilter filter =
                new OrderApiObservationFilter(registry);
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                orderRequest("PATCH"),
                response,
                clientErrorChain());

        Counter counter = registry.get(
                        OrderApiObservationFilter.ERROR_COUNTER)
                .tag("method", "PATCH")
                .tag("outcome", "CLIENT_ERROR")
                .counter();

        assertThat(counter.count())
                .as("client error counter value")
                .isEqualTo(1.0);
        assertThat(registry.get(
                        OrderApiObservationFilter.REQUEST_TIMER)
                .tag("method", "PATCH")
                .tag("outcome", "CLIENT_ERROR")
                .timer()
                .count())
                .as("client error timer count")
                .isEqualTo(1L);
    }

    @Test
    void testFilterShouldIgnoreNonOrderEndpoint() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OrderApiObservationFilter filter =
                new OrderApiObservationFilter(registry);
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/actuator/health");

        filter.doFilter(
                request,
                new MockHttpServletResponse(),
                successfulChain());

        assertThat(registry.getMeters())
                .as("meters for ignored endpoint")
                .isEmpty();
    }

    @Test
    void testFilterShouldRunAfterCorrelationFilter() {
        OrderApiObservationFilter observationFilter =
                new OrderApiObservationFilter(
                        new SimpleMeterRegistry());
        CorrelationIdFilter correlationFilter =
                new CorrelationIdFilter();

        assertThat(observationFilter.getOrder())
                .as("observation filter order")
                .isGreaterThan(correlationFilter.getOrder());
    }

    @Test
    void testConstructorShouldRejectNullRegistry() {
        assertThatThrownBy(() ->
                new OrderApiObservationFilter(null))
                .as("null observation meter registry")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Meter registry must not be null");
    }

    private static MockHttpServletRequest orderRequest(String method) {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        method,
                        "/api/v1/orders");
        request.setAttribute(
                CorrelationIdFilter.REQUEST_ATTRIBUTE,
                CORRELATION_ID);
        return request;
    }

    private static FilterChain successfulChain() {
        return (request, response) -> {
            MockHttpServletResponse httpResponse =
                    (MockHttpServletResponse) response;
            httpResponse.setStatus(200);
        };
    }

    private static FilterChain clientErrorChain() {
        return (request, response) -> {
            MockHttpServletResponse httpResponse =
                    (MockHttpServletResponse) response;
            httpResponse.setStatus(422);
        };
    }
}
