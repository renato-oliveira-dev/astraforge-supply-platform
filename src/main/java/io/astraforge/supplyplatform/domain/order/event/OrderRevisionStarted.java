package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.valueobject.ApprovalComment;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderRevisionStarted(
        UUID eventId,
        OrderId orderId,
        ApprovalComment requestedChanges,
        long aggregateVersion,
        UserId reopenedBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderRevisionStarted {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                requestedChanges,
                "Requested changes must not be null");
        if (aggregateVersion <= 0) {
            throw new IllegalArgumentException(
                    "Aggregate version must be greater than zero");
        }
        Objects.requireNonNull(reopenedBy, "Reopened by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
