package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderCompletionNotAllowedException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderCompletionNotAllowedException(String message) {
        super(message);
    }
}
