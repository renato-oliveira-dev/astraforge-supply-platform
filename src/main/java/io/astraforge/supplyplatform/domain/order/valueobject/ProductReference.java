package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;

public record ProductReference(ProductId productId) {

    public ProductReference {
        Objects.requireNonNull(productId, "Product reference must contain a product ID");
    }
}
