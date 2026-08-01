package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderInventoryRetryNotAllowedException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderInventoryRetryNotAllowedException(String message) {
        super(message);
    }
}
