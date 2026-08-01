package io.astraforge.supplyplatform.domain.order.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class ItemPricingTest {

    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testCalculateShouldApplyDiscountBeforeTax() {
        ItemPricing pricing = new ItemPricing(
                money("100.00"),
                percentage("10.0000"),
                percentage("20.0000"));
        Quantity quantity = new Quantity(new BigDecimal("2.000"));

        assertThat(pricing.subtotal(quantity).amount())
                .as("item subtotal before discount and tax")
                .isEqualByComparingTo("200.00");
        assertThat(pricing.discount(quantity).amount())
                .as("item discount calculated from subtotal")
                .isEqualByComparingTo("20.00");
        assertThat(pricing.tax(quantity).amount())
                .as("item tax calculated after discount")
                .isEqualByComparingTo("36.00");
        assertThat(pricing.total(quantity).amount())
                .as("item final total")
                .isEqualByComparingTo("216.00");
    }

    private static Money money(String value) {
        return new Money(new BigDecimal(value), BRL);
    }

    private static Percentage percentage(String value) {
        return new Percentage(new BigDecimal(value));
    }
}
