package io.astraforge.supplyplatform.domain.order.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTotalsTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void testCreateShouldRejectInconsistentTotal() {
        assertThatThrownBy(() -> new OrderTotals(
                money("100.00", BRL),
                money("10.00", BRL),
                money("18.00", BRL),
                money("109.00", BRL)))
                .as("order total must match its monetary components")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order total must equal subtotal minus discount plus tax");
    }

    @Test
    void testCreateShouldRejectDifferentCurrencies() {
        assertThatThrownBy(() -> new OrderTotals(
                money("100.00", BRL),
                money("10.00", BRL),
                money("18.00", USD),
                money("108.00", BRL)))
                .as("order total components require one currency")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order totals require identical currencies");
    }

    private static Money money(String value, Currency currency) {
        return new Money(new BigDecimal(value), currency);
    }
}
