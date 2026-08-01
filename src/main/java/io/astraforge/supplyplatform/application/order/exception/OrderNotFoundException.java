package io.astraforge.supplyplatform.application.order.exception;

import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;

public final class OrderNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(OrderId orderId) {
        super("Order not found: " + orderId.value());
    }
}
