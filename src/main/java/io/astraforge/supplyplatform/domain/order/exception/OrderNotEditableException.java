package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderNotEditableException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderNotEditableException(String message) {
        super(message);
    }
}
