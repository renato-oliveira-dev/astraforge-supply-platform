package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderInventoryResultNotAllowedException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderInventoryResultNotAllowedException(String message) {
        super(message);
    }
}
