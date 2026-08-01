package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderInventoryReservationRequested(
        UUID eventId,
        OrderId orderId,
        int itemCount,
        long aggregateVersion,
        UserId requestedBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderInventoryReservationRequested {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        if (itemCount <= 0) {
            throw new IllegalArgumentException(
                    "Item count must be greater than zero");
        }
        if (aggregateVersion <= 0) {
            throw new IllegalArgumentException(
                    "Aggregate version must be greater than zero");
        }
        Objects.requireNonNull(requestedBy, "Requested by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
