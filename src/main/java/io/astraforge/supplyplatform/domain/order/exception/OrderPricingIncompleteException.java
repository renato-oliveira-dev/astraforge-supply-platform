package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderPricingIncompleteException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderPricingIncompleteException(String message) {
        super(message);
    }
}
