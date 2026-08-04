package io.astraforge.supplyplatform.configuration;

import io.astraforge.supplyplatform.infrastructure.order.web.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class WebInfrastructureConfigurationTest {

    @Test
    void testConfigurationShouldExposeCorrelationIdFilter() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             WebInfrastructureConfiguration.class)) {

            assertThat(context.getBean(CorrelationIdFilter.class))
                    .as("configured correlation ID filter")
                    .isNotNull();
        }
    }
}
