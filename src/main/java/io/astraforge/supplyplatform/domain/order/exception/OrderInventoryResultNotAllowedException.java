package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderInventoryResultNotAllowedException
        extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderInventoryResultNotAllowedException(String message) {
        super(message);
    }
}
