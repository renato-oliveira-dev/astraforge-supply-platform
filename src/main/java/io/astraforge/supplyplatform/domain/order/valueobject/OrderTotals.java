package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;

public record OrderTotals(
        Money subtotal,
        Money discount,
        Money tax,
        Money total
) {

    public OrderTotals {
        Objects.requireNonNull(subtotal, "Order subtotal must not be null");
        Objects.requireNonNull(discount, "Order discount must not be null");
        Objects.requireNonNull(tax, "Order tax must not be null");
        Objects.requireNonNull(total, "Order total must not be null");
        ensureSameCurrency(subtotal, discount, tax, total);
        Money expectedTotal = subtotal.subtract(discount).add(tax);
        if (!expectedTotal.equals(total)) {
            throw new IllegalArgumentException("Order total must equal subtotal minus discount plus tax");
        }
    }

    private static void ensureSameCurrency(Money first, Money... remaining) {
        for (Money money : remaining) {
            if (!first.currency().equals(money.currency())) {
                throw new IllegalArgumentException("Order totals require identical currencies");
            }
        }
    }
}
