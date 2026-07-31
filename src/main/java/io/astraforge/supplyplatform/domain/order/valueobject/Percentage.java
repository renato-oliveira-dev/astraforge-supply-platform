package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Percentage represented in human form: 10.0000 means ten percent. */
public record Percentage(BigDecimal value) {

    public static final int SCALE = 4;
    private static final BigDecimal MAX_VALUE = new BigDecimal("100.0000");

    public Percentage {
        Objects.requireNonNull(value, "Percentage must not be null");
        value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
        if (value.signum() < 0 || value.compareTo(MAX_VALUE) > 0) {
            throw new DomainValidationException("Percentage must be between 0 and 100");
        }
    }

    public BigDecimal asMultiplier() {
        return value.movePointLeft(2);
    }
}
