package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderProcessingNotAllowedException
        extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderProcessingNotAllowedException(String message) {
        super(message);
    }
}
