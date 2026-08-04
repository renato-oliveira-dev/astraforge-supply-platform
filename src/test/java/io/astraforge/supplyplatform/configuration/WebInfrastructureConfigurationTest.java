package io.astraforge.supplyplatform.configuration;

import io.astraforge.supplyplatform.infrastructure.order.web.CorrelationIdFilter;
import io.astraforge.supplyplatform.infrastructure.order.web.OrderApiObservationFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class WebInfrastructureConfigurationTest {

    @Test
    void testConfigurationShouldExposeCorrelationIdFilter() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {
            context.registerBean(
                    SimpleMeterRegistry.class,
                    SimpleMeterRegistry::new);
            context.register(WebInfrastructureConfiguration.class);
            context.refresh();

            assertThat(context.getBean(CorrelationIdFilter.class))
                    .as("configured correlation ID filter")
                    .isNotNull();
            assertThat(context.getBean(OrderApiObservationFilter.class))
                    .as("configured order API observation filter")
                    .isNotNull();
        }
    }
}
