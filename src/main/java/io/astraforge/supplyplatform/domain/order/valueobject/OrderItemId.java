package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;
import java.util.UUID;

public record OrderItemId(UUID value) {

    public OrderItemId {
        Objects.requireNonNull(value, "Order item ID must not be null");
    }
}
