package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public Money {
        Objects.requireNonNull(amount, "Money amount must not be null");
        Objects.requireNonNull(currency, "Money currency must not be null");
        amount = amount.setScale(SCALE, ROUNDING_MODE);
        if (amount.signum() < 0) {
            throw new DomainValidationException("Money amount must not be negative");
        }
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        BigDecimal result = amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new DomainValidationException("Money subtraction must not result in a negative amount");
        }
        return new Money(result, currency);
    }

    public Money multiply(Quantity quantity) {
        Objects.requireNonNull(quantity, "Quantity must not be null");
        return new Money(amount.multiply(quantity.value()), currency);
    }

    public Money percentageOf(Percentage percentage) {
        Objects.requireNonNull(percentage, "Percentage must not be null");
        return new Money(amount.multiply(percentage.asMultiplier()), currency);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "Money operand must not be null");
        if (!currency.equals(other.currency)) {
            throw new DomainValidationException("Money operations require identical currencies");
        }
    }
}
