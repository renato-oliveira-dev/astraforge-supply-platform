package io.astraforge.supplyplatform.domain.order.exception;

public final class DuplicateProductException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public DuplicateProductException(String message) {
        super(message);
    }
}
