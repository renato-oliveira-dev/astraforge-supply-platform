package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;

public record ItemPricing(
        Money unitPrice,
        Percentage discountPercentage,
        Percentage taxPercentage
) {

    public ItemPricing {
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        Objects.requireNonNull(discountPercentage, "Discount percentage must not be null");
        Objects.requireNonNull(taxPercentage, "Tax percentage must not be null");
    }

    public Money subtotal(Quantity quantity) {
        return unitPrice.multiply(Objects.requireNonNull(quantity, "Quantity must not be null"));
    }

    public Money discount(Quantity quantity) {
        return subtotal(quantity).percentageOf(discountPercentage);
    }

    public Money tax(Quantity quantity) {
        Money taxableAmount = subtotal(quantity).subtract(discount(quantity));
        return taxableAmount.percentageOf(taxPercentage);
    }

    public Money total(Quantity quantity) {
        Money taxableAmount = subtotal(quantity).subtract(discount(quantity));
        return taxableAmount.add(tax(quantity));
    }
}
