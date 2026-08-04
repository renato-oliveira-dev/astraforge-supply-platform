package io.astraforge.supplyplatform.domain.order.exception;

public final class OrderApprovalNotAllowedException
        extends DomainStateException {

    private static final long serialVersionUID = 1L;

    public OrderApprovalNotAllowedException(String message) {
        super(message);
    }
}
