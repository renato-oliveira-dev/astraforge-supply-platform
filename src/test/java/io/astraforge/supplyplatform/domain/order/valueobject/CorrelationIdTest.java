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
    void testCreateShouldRejectOversizedValue() {
        var sonarArgument1Value1 = "x".repeat(101);
        assertThatThrownBy(() -> new CorrelationId(sonarArgument1Value1))
                .as("oversized correlation identifier")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(
                        "Correlation ID must not exceed 100 characters");
    }

    @Test
    void testCreateShouldRejectControlCharacters() {
        assertThatThrownBy(() -> new CorrelationId("flow-123\nforged"))
                .as("correlation identifier with control characters")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(
                        "Correlation ID must not contain control characters");
    }

    @Test
    void testCreateShouldRejectBlankValue() {
        assertThatThrownBy(() -> new CorrelationId(" "))
                .as("blank correlation identifier")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Correlation ID must not be blank");
    }
}
