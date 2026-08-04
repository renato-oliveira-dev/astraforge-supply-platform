package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderCancellationNotAllowedException
        extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderCancellationNotAllowedException(String message) {
        super(message);
    }
}
