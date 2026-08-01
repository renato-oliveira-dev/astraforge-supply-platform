package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderItemPriced(
        UUID eventId,
        OrderId orderId,
        OrderItemId orderItemId,
        ItemPricing pricing,
        long aggregateVersion,
        UserId pricedBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderItemPriced {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(orderItemId, "Order item ID must not be null");
        Objects.requireNonNull(pricing, "Item pricing must not be null");
        Objects.requireNonNull(pricedBy, "Priced by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
