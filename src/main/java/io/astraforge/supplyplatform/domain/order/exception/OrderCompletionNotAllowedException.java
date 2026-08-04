package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderCompletionNotAllowedException
        extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderCompletionNotAllowedException(String message) {
        super(message);
    }
}
