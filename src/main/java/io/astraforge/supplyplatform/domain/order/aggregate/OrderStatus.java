package io.astraforge.supplyplatform.domain.order.aggregate;

public enum OrderStatus {
    DRAFT,
    SUBMITTED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    REVIEW_REQUESTED,
    PROCESSING,
    INVENTORY_PENDING,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    READY_FOR_FULFILLMENT,
    FULFILLMENT_IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
