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
        var sonarArgument1Value1 = money("100.00", BRL);
        var sonarArgument1Value2 = money("10.00", BRL);
        var sonarArgument1Value3 = money("18.00", BRL);
        var sonarArgument1Value4 = money("109.00", BRL);
        assertThatThrownBy(() -> new OrderTotals(sonarArgument1Value1, sonarArgument1Value2, sonarArgument1Value3, sonarArgument1Value4))
                .as("order total must match its monetary components")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order total must equal subtotal minus discount plus tax");
    }

    @Test
    void testCreateShouldRejectDifferentCurrencies() {
        var sonarArgument2Value1 = money("100.00", BRL);
        var sonarArgument2Value2 = money("10.00", BRL);
        var sonarArgument2Value3 = money("18.00", USD);
        var sonarArgument2Value4 = money("108.00", BRL);
        assertThatThrownBy(() -> new OrderTotals(sonarArgument2Value1, sonarArgument2Value2, sonarArgument2Value3, sonarArgument2Value4))
                .as("order total components require one currency")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order totals require identical currencies");
    }

    private static Money money(String value, Currency currency) {
        return new Money(new BigDecimal(value), currency);
    }
}
