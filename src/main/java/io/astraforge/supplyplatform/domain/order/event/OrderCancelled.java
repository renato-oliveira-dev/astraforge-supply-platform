package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.valueobject.CancellationReason;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderCancelled(
        UUID eventId,
        OrderId orderId,
        OrderStatus previousStatus,
        CancellationReason reason,
        long aggregateVersion,
        UserId cancelledBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderCancelled {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                previousStatus,
                "Previous status must not be null");
        Objects.requireNonNull(reason, "Cancellation reason must not be null");
        if (aggregateVersion <= 0) {
            throw new IllegalArgumentException(
                    "Aggregate version must be greater than zero");
        }
        Objects.requireNonNull(cancelledBy, "Cancelled by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
