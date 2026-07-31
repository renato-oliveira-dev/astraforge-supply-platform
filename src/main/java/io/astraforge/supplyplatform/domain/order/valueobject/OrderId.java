package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;
import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId {
        Objects.requireNonNull(value, "Order ID must not be null");
    }
}
