package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void testCreateShouldNormalizeScaleAndRounding() {
        Money money = new Money(new BigDecimal("10.126"), BRL);

        assertThat(money.amount())
                .as("money amount normalized to the configured scale")
                .isEqualByComparingTo("10.13");
    }

    @Test
    void testAddShouldReturnNewMoneyWithSameCurrency() {
        Money first = new Money(new BigDecimal("10.00"), BRL);
        Money second = new Money(new BigDecimal("2.50"), BRL);

        Money result = first.add(second);

        assertThat(result)
                .as("sum of compatible monetary values")
                .isEqualTo(new Money(new BigDecimal("12.50"), BRL));
        assertThat(first.amount())
                .as("original money remains unchanged")
                .isEqualByComparingTo("10.00");
    }

    @Test
    void testSubtractShouldRejectNegativeResult() {
        Money lower = new Money(new BigDecimal("2.00"), BRL);
        Money higher = new Money(new BigDecimal("3.00"), BRL);

        assertThatThrownBy(() -> lower.subtract(higher))
                .as("negative monetary results are forbidden")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Money subtraction must not result in a negative amount");
    }

    @Test
    void testOperationShouldRejectDifferentCurrencies() {
        Money brl = new Money(BigDecimal.ONE, BRL);
        Money usd = new Money(BigDecimal.ONE, USD);

        assertThatThrownBy(() -> brl.add(usd))
                .as("money arithmetic requires identical currencies")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Money operations require identical currencies");
    }

    @Test
    void testMultiplyShouldUseQuantityValue() {
        Money unitPrice = new Money(new BigDecimal("12.35"), BRL);
        Quantity quantity = new Quantity(new BigDecimal("2.000"));

        Money total = unitPrice.multiply(quantity);

        assertThat(total.amount())
                .as("money multiplied by quantity")
                .isEqualByComparingTo("24.70");
    }

    @Test
    void testPercentageOfShouldCalculatePercentageUsingMoneyRounding() {
        Money base = new Money(new BigDecimal("99.99"), BRL);
        Percentage percentage = new Percentage(new BigDecimal("12.5000"));

        Money result = base.percentageOf(percentage);

        assertThat(result.amount())
                .as("percentage calculated from monetary value")
                .isEqualByComparingTo("12.50");
    }
}
