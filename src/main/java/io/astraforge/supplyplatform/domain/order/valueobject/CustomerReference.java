package io.astraforge.supplyplatform.domain.order.valueobject;

import java.util.Objects;

public record CustomerReference(CustomerId customerId) {

    public CustomerReference {
        Objects.requireNonNull(customerId, "Customer reference must contain a customer ID");
    }
}
