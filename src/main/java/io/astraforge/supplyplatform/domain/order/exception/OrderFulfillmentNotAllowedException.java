package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderFulfillmentNotAllowedException
        extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderFulfillmentNotAllowedException(String message) {
        super(message);
    }
}
