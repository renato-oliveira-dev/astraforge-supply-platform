package io.astraforge.supplyplatform.domain.order.event;

import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderItemQuantityChanged(
        UUID eventId,
        OrderId orderId,
        OrderItemId orderItemId,
        ProductId productId,
        Quantity previousQuantity,
        Quantity newQuantity,
        long aggregateVersion,
        UserId changedBy,
        Instant occurredAt,
        CorrelationId correlationId
) implements DomainEvent {

    public OrderItemQuantityChanged {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(orderItemId, "Order item ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(previousQuantity, "Previous quantity must not be null");
        Objects.requireNonNull(newQuantity, "New quantity must not be null");
        Objects.requireNonNull(changedBy, "Changed by must not be null");
        Objects.requireNonNull(occurredAt, "Occurred at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
    }
}
