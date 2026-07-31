package io.astraforge.supplyplatform.domain.order.aggregate;

import io.astraforge.supplyplatform.domain.order.entity.OrderItem;
import io.astraforge.supplyplatform.domain.order.event.DomainEvent;
import io.astraforge.supplyplatform.domain.order.event.OrderCreated;
import io.astraforge.supplyplatform.domain.order.event.OrderItemAdded;
import io.astraforge.supplyplatform.domain.order.event.OrderItemQuantityChanged;
import io.astraforge.supplyplatform.domain.order.event.OrderItemRemoved;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateOrderItemException;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateProductException;
import io.astraforge.supplyplatform.domain.order.exception.OrderItemNotFoundException;
import io.astraforge.supplyplatform.domain.order.exception.OrderNotEditableException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
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
    private final List<OrderItem> items;
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
        this.items = new ArrayList<>();
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

    public void addItem(
            OrderItemId orderItemId,
            ProductSnapshot productSnapshot,
            Quantity quantity,
            UserId addedBy,
            Instant addedAt,
            CorrelationId correlationId
    ) {
        requireEditable();
        Objects.requireNonNull(orderItemId, "Order item ID must not be null");
        Objects.requireNonNull(productSnapshot, "Product snapshot must not be null");
        Objects.requireNonNull(quantity, "Quantity must not be null");
        Objects.requireNonNull(addedBy, "Added by must not be null");
        Objects.requireNonNull(addedAt, "Added at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireUniqueItemId(orderItemId);
        requireUniqueProduct(productSnapshot);

        OrderItem item = OrderItem.create(orderItemId, productSnapshot, quantity);
        items.add(item);
        touch(addedAt);
        registerEvent(new OrderItemAdded(
                UUID.randomUUID(),
                id,
                orderItemId,
                item.productId(),
                quantity,
                version,
                addedBy,
                addedAt,
                correlationId));
    }

    public void updateItemQuantity(
            OrderItemId orderItemId,
            Quantity newQuantity,
            UserId changedBy,
            Instant changedAt,
            CorrelationId correlationId
    ) {
        requireEditable();
        Objects.requireNonNull(orderItemId, "Order item ID must not be null");
        Objects.requireNonNull(newQuantity, "New quantity must not be null");
        Objects.requireNonNull(changedBy, "Changed by must not be null");
        Objects.requireNonNull(changedAt, "Changed at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");

        OrderItem item = findItem(orderItemId);
        if (item.quantity().equals(newQuantity)) {
            return;
        }

        Quantity previousQuantity = item.changeQuantity(newQuantity);
        touch(changedAt);
        registerEvent(new OrderItemQuantityChanged(
                UUID.randomUUID(),
                id,
                item.id(),
                item.productId(),
                previousQuantity,
                newQuantity,
                version,
                changedBy,
                changedAt,
                correlationId));
    }

    public void removeItem(
            OrderItemId orderItemId,
            UserId removedBy,
            Instant removedAt,
            CorrelationId correlationId
    ) {
        requireEditable();
        Objects.requireNonNull(orderItemId, "Order item ID must not be null");
        Objects.requireNonNull(removedBy, "Removed by must not be null");
        Objects.requireNonNull(removedAt, "Removed at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");

        OrderItem item = findItem(orderItemId);
        items.remove(item);
        touch(removedAt);
        registerEvent(new OrderItemRemoved(
                UUID.randomUUID(),
                id,
                item.id(),
                item.productId(),
                item.quantity(),
                version,
                removedBy,
                removedAt,
                correlationId));
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

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    public List<DomainEvent> domainEvents() {
        return List.copyOf(domainEvents);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> pendingEvents = List.copyOf(domainEvents);
        domainEvents.clear();
        return pendingEvents;
    }

    private void requireEditable() {
        if (status != OrderStatus.DRAFT) {
            throw new OrderNotEditableException(
                    "Order items can be changed only while the order is in DRAFT status");
        }
    }

    private void requireUniqueItemId(OrderItemId orderItemId) {
        boolean duplicated = items.stream().anyMatch(item -> item.id().equals(orderItemId));
        if (duplicated) {
            throw new DuplicateOrderItemException("Order item ID already exists in the order");
        }
    }

    private void requireUniqueProduct(ProductSnapshot productSnapshot) {
        boolean duplicated = items.stream()
                .anyMatch(item -> item.productId().equals(productSnapshot.productId()));
        if (duplicated) {
            throw new DuplicateProductException("Product already exists in the order");
        }
    }

    private OrderItem findItem(OrderItemId orderItemId) {
        return items.stream()
                .filter(item -> item.id().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(
                        "Order item was not found in the order"));
    }

    private void touch(Instant changedAt) {
        updatedAt = changedAt;
        version++;
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(Objects.requireNonNull(event, "Domain event must not be null"));
    }
}
