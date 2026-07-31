package io.astraforge.supplyplatform.domain.order.exception;

public final class DuplicateOrderItemException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public DuplicateOrderItemException(String message) {
        super(message);
    }
}
