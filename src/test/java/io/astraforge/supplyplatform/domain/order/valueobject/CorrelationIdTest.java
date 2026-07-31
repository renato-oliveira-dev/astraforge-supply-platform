package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdTest {

    @Test
    void testCreateShouldTrimValue() {
        CorrelationId correlationId = new CorrelationId("  flow-123  ");

        assertThat(correlationId.value())
                .as("normalized correlation identifier")
                .isEqualTo("flow-123");
    }

    @Test
    void testCreateShouldRejectBlankValue() {
        assertThatThrownBy(() -> new CorrelationId(" "))
                .as("blank correlation identifier")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Correlation ID must not be blank");
    }
}
