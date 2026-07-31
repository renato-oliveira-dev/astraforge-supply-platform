package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageTest {

    @Test
    void testAsMultiplierShouldConvertHumanPercentage() {
        Percentage percentage = new Percentage(new BigDecimal("12.5000"));

        assertThat(percentage.asMultiplier())
                .as("human percentage converted to decimal multiplier")
                .isEqualByComparingTo("0.125000");
    }

    @Test
    void testCreateShouldRejectValueAboveOneHundred() {
        assertThatThrownBy(() -> new Percentage(new BigDecimal("100.0001")))
                .as("percentage upper boundary")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Percentage must be between 0 and 100");
    }
}
