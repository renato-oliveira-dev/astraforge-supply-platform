package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderSubmissionNotAllowedException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public OrderSubmissionNotAllowedException(String message) {
        super(message);
    }
}
