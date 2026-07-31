package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderItemNotFoundException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public OrderItemNotFoundException(String message) {
        super(message);
    }
}
