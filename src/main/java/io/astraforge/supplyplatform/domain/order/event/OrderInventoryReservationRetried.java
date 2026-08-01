package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.InventoryFailureReason;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderInventoryReservationRetried(
        UUID eventId,
        OrderId orderId,
        InventoryFailureReason previousFailureReason,
        long aggregateVersion,
        UserId requestedBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderInventoryReservationRetried {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(
                previousFailureReason,
                "Previous failure reason must not be null");
        if (aggregateVersion <= 0) {
            throw new IllegalArgumentException(
                    "Aggregate version must be greater than zero");
        }
        Objects.requireNonNull(requestedBy, "Requested by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
