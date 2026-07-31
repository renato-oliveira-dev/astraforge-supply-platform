package io.astraforge.supplyplatform.domain.order.aggregate;

import io.astraforge.supplyplatform.domain.order.event.DomainEvent;
import io.astraforge.supplyplatform.domain.order.event.OrderCreated;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Order {

    private static final long INITIAL_VERSION = 0L;

    private final OrderId id;
    private final CustomerReference customerReference;
    private final UserId createdBy;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents;
    private OrderStatus status;
    private Instant updatedAt;
    private long version;

    private Order(
            OrderId id,
            CustomerReference customerReference,
            UserId createdBy,
            Instant createdAt,
            CorrelationId correlationId
    ) {
        this.id = id;
        this.customerReference = customerReference;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.status = OrderStatus.DRAFT;
        this.version = INITIAL_VERSION;
        this.domainEvents = new ArrayList<>();
        registerEvent(new OrderCreated(
                UUID.randomUUID(),
                id,
                customerReference.customerId(),
                createdBy,
                createdAt,
                correlationId));
    }

    public static Order create(
            OrderId id,
            CustomerReference customerReference,
            UserId createdBy,
            Instant createdAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(id, "Order ID must not be null");
        Objects.requireNonNull(customerReference, "Customer reference must not be null");
        Objects.requireNonNull(createdBy, "Created by must not be null");
        Objects.requireNonNull(createdAt, "Created at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        return new Order(id, customerReference, createdBy, createdAt, correlationId);
    }

    public OrderId id() {
        return id;
    }

    public CustomerReference customerReference() {
        return customerReference;
    }

    public UserId createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public OrderStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public List<DomainEvent> domainEvents() {
        return List.copyOf(domainEvents);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> pendingEvents = List.copyOf(domainEvents);
        domainEvents.clear();
        return pendingEvents;
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(Objects.requireNonNull(event, "Domain event must not be null"));
    }
}
