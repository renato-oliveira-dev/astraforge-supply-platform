package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderCompleted(
        UUID eventId,
        OrderId orderId,
        int itemCount,
        OrderTotals totals,
        long aggregateVersion,
        UserId completedBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderCompleted {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        if (itemCount <= 0) {
            throw new IllegalArgumentException(
                    "Item count must be greater than zero");
        }
        Objects.requireNonNull(totals, "Order totals must not be null");
        if (aggregateVersion <= 0) {
            throw new IllegalArgumentException(
                    "Aggregate version must be greater than zero");
        }
        Objects.requireNonNull(completedBy, "Completed by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
