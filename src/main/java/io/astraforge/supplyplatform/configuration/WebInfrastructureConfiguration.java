package io.astraforge.supplyplatform.configuration;

import io.astraforge.supplyplatform.infrastructure.order.web.CorrelationIdFilter;
import io.astraforge.supplyplatform.infrastructure.order.web.OrderApiObservationFilter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WebInfrastructureConfiguration {

    @Bean
    CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    OrderApiObservationFilter orderApiObservationFilter(
            MeterRegistry meterRegistry
    ) {
        return new OrderApiObservationFilter(meterRegistry);
    }
}
