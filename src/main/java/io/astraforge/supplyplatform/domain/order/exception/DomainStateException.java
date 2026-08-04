package io.astraforge.supplyplatform.domain.order.exception;

/**
 * Signals that an order operation is invalid for the aggregate's current
 * lifecycle state.
 */
public class DomainStateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DomainStateException(String message) {
        super(message);
    }
}
