package io.astraforge.supplyplatform.domain.order.exception;

/**
 * Signals that a domain object or operation would violate a business invariant.
 */
public final class DomainValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public DomainValidationException(String message) {
        super(message);
    }
}
