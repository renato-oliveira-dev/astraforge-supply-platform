package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ApprovalDecisionResponse(
        UUID orderId,
        OrderStatus status,
        UUID decidedBy,
        Instant decidedAt,
        Optional<String> comment,
        long version
) {

    public ApprovalDecisionResponse {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(status, "Order status must not be null");
        Objects.requireNonNull(decidedBy, "Decided by must not be null");
        Objects.requireNonNull(decidedAt, "Decided at must not be null");
        Objects.requireNonNull(
                comment,
                "Decision comment must not be null");
        if (version <= 0) {
            throw new IllegalArgumentException(
                    "Order version must be greater than zero");
        }
    }
}
