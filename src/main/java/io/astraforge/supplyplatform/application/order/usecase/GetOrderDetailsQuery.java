package io.astraforge.supplyplatform.application.order.usecase;

import java.util.Objects;
import java.util.UUID;

public record GetOrderDetailsQuery(UUID orderId) {

    public GetOrderDetailsQuery {
        Objects.requireNonNull(orderId, "Order ID must not be null");
    }
}
