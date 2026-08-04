package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    void testCreateShouldRejectZero() {
        assertThatThrownBy(() -> new Quantity(BigDecimal.ZERO))
                .as("quantity must be positive")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    void testAddShouldReturnCombinedQuantity() {
        Quantity first = new Quantity(new BigDecimal("1.250"));
        Quantity second = new Quantity(new BigDecimal("2.750"));

        Quantity result = first.add(second);

        assertThat(result.value())
                .as("combined quantity")
                .isEqualByComparingTo("4.000");
    }

    @Test
    void testCreateShouldRejectUnsupportedPrecision() {
        var sonarArgument1Value1 = new BigDecimal("1.0001");
        assertThatThrownBy(() -> new Quantity(sonarArgument1Value1))
                .as("quantity scale must remain explicit")
                .isInstanceOf(ArithmeticException.class);
    }
}
