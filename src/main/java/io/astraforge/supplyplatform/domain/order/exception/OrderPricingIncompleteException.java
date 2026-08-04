package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderPricingIncompleteException extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderPricingIncompleteException(String message) {
        super(message);
    }
}
