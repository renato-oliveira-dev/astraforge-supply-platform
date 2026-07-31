package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        Objects.requireNonNull(value, "Product ID must not be null");
    }
}
