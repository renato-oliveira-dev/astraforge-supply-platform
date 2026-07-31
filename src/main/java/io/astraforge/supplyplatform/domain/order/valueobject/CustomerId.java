package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId {
        Objects.requireNonNull(value, "Customer ID must not be null");
    }
}
