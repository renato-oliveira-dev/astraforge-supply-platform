package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Quantity(BigDecimal value) implements Comparable<Quantity> {

    public static final int SCALE = 3;

    public Quantity {
        Objects.requireNonNull(value, "Quantity must not be null");
        value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
        if (value.signum() <= 0) {
            throw new DomainValidationException("Quantity must be greater than zero");
        }
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "Quantity to add must not be null");
        return new Quantity(value.add(other.value));
    }

    @Override
    public int compareTo(Quantity other) {
        Objects.requireNonNull(other, "Quantity to compare must not be null");
        return value.compareTo(other.value);
    }
}
