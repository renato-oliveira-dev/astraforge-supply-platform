package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderCurrencyMismatchException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public OrderCurrencyMismatchException(String message) {
        super(message);
    }
}
