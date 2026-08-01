package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderRevisionNotAllowedException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderRevisionNotAllowedException(String message) {
        super(message);
    }
}
