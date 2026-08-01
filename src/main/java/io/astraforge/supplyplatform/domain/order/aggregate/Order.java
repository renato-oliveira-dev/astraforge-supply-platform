package io.astraforge.supplyplatform.domain.order.aggregate;

import io.astraforge.supplyplatform.domain.order.entity.OrderItem;
import io.astraforge.supplyplatform.domain.order.event.DomainEvent;
import io.astraforge.supplyplatform.domain.order.event.OrderApprovalStarted;
import io.astraforge.supplyplatform.domain.order.event.OrderApproved;
import io.astraforge.supplyplatform.domain.order.event.OrderCancelled;
import io.astraforge.supplyplatform.domain.order.event.OrderCreated;
import io.astraforge.supplyplatform.domain.order.event.OrderItemAdded;
import io.astraforge.supplyplatform.domain.order.event.OrderItemQuantityChanged;
import io.astraforge.supplyplatform.domain.order.event.OrderItemRemoved;
import io.astraforge.supplyplatform.domain.order.event.OrderItemPriced;
import io.astraforge.supplyplatform.domain.order.event.OrderItemPricingInvalidated;
import io.astraforge.supplyplatform.domain.order.event.OrderInventoryReservationFailed;
import io.astraforge.supplyplatform.domain.order.event.OrderInventoryReservationRequested;
import io.astraforge.supplyplatform.domain.order.event.OrderInventoryReserved;
import io.astraforge.supplyplatform.domain.order.event.OrderProcessingStarted;
import io.astraforge.supplyplatform.domain.order.event.OrderRejected;
import io.astraforge.supplyplatform.domain.order.event.OrderReviewRequested;
import io.astraforge.supplyplatform.domain.order.event.OrderRevisionStarted;
import io.astraforge.supplyplatform.domain.order.event.OrderSubmitted;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateOrderItemException;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateProductException;
import io.astraforge.supplyplatform.domain.order.exception.OrderInventoryResultNotAllowedException;
import io.astraforge.supplyplatform.domain.order.exception.OrderItemNotFoundException;
import io.astraforge.supplyplatform.domain.order.exception.OrderNotEditableException;
import io.astraforge.supplyplatform.domain.order.exception.OrderProcessingNotAllowedException;
import io.astraforge.supplyplatform.domain.order.exception.OrderApprovalNotAllowedException;
import io.astraforge.supplyplatform.domain.order.exception.OrderCancellationNotAllowedException;
import io.astraforge.supplyplatform.domain.order.exception.OrderCurrencyMismatchException;
import io.astraforge.supplyplatform.domain.order.exception.OrderPricingIncompleteException;
import io.astraforge.supplyplatform.domain.order.exception.OrderRevisionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.exception.OrderSubmissionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.ApprovalComment;
import io.astraforge.supplyplatform.domain.order.valueobject.CancellationReason;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.InventoryFailureReason;
import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.Money;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Order {

    private static final long INITIAL_VERSION = 0L;
    private static final EnumSet<OrderStatus> CANCELLABLE_STATUSES = EnumSet.of(
            OrderStatus.DRAFT,
            OrderStatus.SUBMITTED,
            OrderStatus.PENDING_APPROVAL,
            OrderStatus.REVIEW_REQUESTED,
            OrderStatus.APPROVED);

    private final OrderId id;
    private final CustomerReference customerReference;
    private final UserId createdBy;
    private final Instant createdAt;
    private final List<OrderItem> items;
    private final List<DomainEvent> domainEvents;
    private OrderStatus status;
    private Instant updatedAt;
    private UserId submittedBy;
    private Instant submittedAt;
    private UserId decisionBy;
    private Instant decisionAt;
    private ApprovalComment decisionComment;
    private UserId cancelledBy;
    private Instant cancelledAt;
    private CancellationReason cancellationReason;
    private UserId processingStartedBy;
    private Instant processingStartedAt;
    private UserId inventoryRequestedBy;
    private Instant inventoryRequestedAt;
    private UserId inventoryResultRecordedBy;
    private Instant inventoryResultRecordedAt;
    private InventoryFailureReason inventoryFailureReason;
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
        boolean pricingInvalidated = item.invalidatePricing();
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
        if (pricingInvalidated) {
            registerEvent(new OrderItemPricingInvalidated(
                    UUID.randomUUID(),
                    id,
                    item.id(),
                    version,
                    changedBy,
                    changedAt,
                    correlationId));
        }
    }

    public void applyItemPricing(
            OrderItemId orderItemId,
            ItemPricing pricing,
            UserId pricedBy,
            Instant pricedAt,
            CorrelationId correlationId
    ) {
        requireEditable();
        Objects.requireNonNull(orderItemId, "Order item ID must not be null");
        Objects.requireNonNull(pricing, "Item pricing must not be null");
        Objects.requireNonNull(pricedBy, "Priced by must not be null");
        Objects.requireNonNull(pricedAt, "Priced at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");

        OrderItem item = findItem(orderItemId);
        requireCompatibleCurrency(pricing);
        if (item.pricing().filter(pricing::equals).isPresent()) {
            return;
        }

        item.applyPricing(pricing);
        touch(pricedAt);
        registerEvent(new OrderItemPriced(
                UUID.randomUUID(),
                id,
                item.id(),
                pricing,
                version,
                pricedBy,
                pricedAt,
                correlationId));
    }

    public boolean pricingComplete() {
        return !items.isEmpty() && items.stream().allMatch(item -> item.pricing().isPresent());
    }

    public OrderTotals totals() {
        if (!pricingComplete()) {
            throw new OrderPricingIncompleteException(
                    "Order totals require pricing for every order item");
        }

        Currency currency = items.getFirst().pricing().orElseThrow().unitPrice().currency();
        Money subtotal = Money.zero(currency);
        Money discount = Money.zero(currency);
        Money tax = Money.zero(currency);

        for (OrderItem item : items) {
            ItemPricing pricing = item.pricing().orElseThrow();
            subtotal = subtotal.add(pricing.subtotal(item.quantity()));
            discount = discount.add(pricing.discount(item.quantity()));
            tax = tax.add(pricing.tax(item.quantity()));
        }

        return new OrderTotals(subtotal, discount, tax, subtotal.subtract(discount).add(tax));
    }


    public void submit(
            UserId submittedBy,
            Instant submittedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(submittedBy, "Submitted by must not be null");
        Objects.requireNonNull(submittedAt, "Submitted at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireDraftForSubmission();
        requireSubmissionReady();

        OrderTotals orderTotals = totals();
        this.status = OrderStatus.SUBMITTED;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;
        touch(submittedAt);
        registerEvent(new OrderSubmitted(
                UUID.randomUUID(),
                id,
                customerReference.customerId(),
                items.size(),
                orderTotals,
                version,
                submittedBy,
                submittedAt,
                correlationId));
    }


    public void startApproval(
            UserId startedBy,
            Instant startedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(startedBy, "Started by must not be null");
        Objects.requireNonNull(startedAt, "Started at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireStatus(
                OrderStatus.SUBMITTED,
                "Only a SUBMITTED order can start approval");

        status = OrderStatus.PENDING_APPROVAL;
        touch(startedAt);
        registerEvent(new OrderApprovalStarted(
                UUID.randomUUID(),
                id,
                version,
                startedBy,
                startedAt,
                correlationId));
    }

    public void approve(
            UserId approvedBy,
            Instant approvedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(approvedBy, "Approved by must not be null");
        Objects.requireNonNull(approvedAt, "Approved at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requirePendingApproval();

        OrderTotals orderTotals = totals();
        status = OrderStatus.APPROVED;
        recordDecision(approvedBy, approvedAt, null);
        touch(approvedAt);
        registerEvent(new OrderApproved(
                UUID.randomUUID(),
                id,
                orderTotals,
                version,
                approvedBy,
                approvedAt,
                correlationId));
    }

    public void reject(
            ApprovalComment comment,
            UserId rejectedBy,
            Instant rejectedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(comment, "Rejection comment must not be null");
        Objects.requireNonNull(rejectedBy, "Rejected by must not be null");
        Objects.requireNonNull(rejectedAt, "Rejected at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requirePendingApproval();

        status = OrderStatus.REJECTED;
        recordDecision(rejectedBy, rejectedAt, comment);
        touch(rejectedAt);
        registerEvent(new OrderRejected(
                UUID.randomUUID(),
                id,
                comment,
                version,
                rejectedBy,
                rejectedAt,
                correlationId));
    }

    public void requestReview(
            ApprovalComment comment,
            UserId requestedBy,
            Instant requestedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(comment, "Review comment must not be null");
        Objects.requireNonNull(requestedBy, "Requested by must not be null");
        Objects.requireNonNull(requestedAt, "Requested at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requirePendingApproval();

        status = OrderStatus.REVIEW_REQUESTED;
        recordDecision(requestedBy, requestedAt, comment);
        touch(requestedAt);
        registerEvent(new OrderReviewRequested(
                UUID.randomUUID(),
                id,
                comment,
                version,
                requestedBy,
                requestedAt,
                correlationId));
    }


    public void reopenForRevision(
            UserId reopenedBy,
            Instant reopenedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(reopenedBy, "Reopened by must not be null");
        Objects.requireNonNull(reopenedAt, "Reopened at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireReviewRequested();

        ApprovalComment requestedChanges = decisionComment;
        status = OrderStatus.DRAFT;
        clearDecision();
        touch(reopenedAt);
        registerEvent(new OrderRevisionStarted(
                UUID.randomUUID(),
                id,
                requestedChanges,
                version,
                reopenedBy,
                reopenedAt,
                correlationId));
    }


    public void cancel(
            CancellationReason reason,
            UserId cancelledBy,
            Instant cancelledAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(reason, "Cancellation reason must not be null");
        Objects.requireNonNull(cancelledBy, "Cancelled by must not be null");
        Objects.requireNonNull(cancelledAt, "Cancelled at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireCancellable();

        OrderStatus previousStatus = status;
        status = OrderStatus.CANCELLED;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = reason;
        touch(cancelledAt);
        registerEvent(new OrderCancelled(
                UUID.randomUUID(),
                id,
                previousStatus,
                reason,
                version,
                cancelledBy,
                cancelledAt,
                correlationId));
    }


    public void startProcessing(
            UserId startedBy,
            Instant startedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(startedBy, "Started by must not be null");
        Objects.requireNonNull(startedAt, "Started at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireProcessingStatus(
                OrderStatus.APPROVED,
                "Only an APPROVED order can start processing");

        status = OrderStatus.PROCESSING;
        processingStartedBy = startedBy;
        processingStartedAt = startedAt;
        touch(startedAt);
        registerEvent(new OrderProcessingStarted(
                UUID.randomUUID(),
                id,
                version,
                startedBy,
                startedAt,
                correlationId));
    }

    public void requestInventoryReservation(
            UserId requestedBy,
            Instant requestedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(requestedBy, "Requested by must not be null");
        Objects.requireNonNull(requestedAt, "Requested at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireProcessingStatus(
                OrderStatus.PROCESSING,
                "Only a PROCESSING order can request inventory reservation");

        status = OrderStatus.INVENTORY_PENDING;
        inventoryRequestedBy = requestedBy;
        inventoryRequestedAt = requestedAt;
        touch(requestedAt);
        registerEvent(new OrderInventoryReservationRequested(
                UUID.randomUUID(),
                id,
                items.size(),
                version,
                requestedBy,
                requestedAt,
                correlationId));
    }


    public void confirmInventoryReservation(
            UserId recordedBy,
            Instant recordedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(recordedBy, "Recorded by must not be null");
        Objects.requireNonNull(recordedAt, "Recorded at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireInventoryPending();

        status = OrderStatus.INVENTORY_RESERVED;
        inventoryResultRecordedBy = recordedBy;
        inventoryResultRecordedAt = recordedAt;
        inventoryFailureReason = null;
        touch(recordedAt);
        registerEvent(new OrderInventoryReserved(
                UUID.randomUUID(),
                id,
                items.size(),
                version,
                recordedBy,
                recordedAt,
                correlationId));
    }

    public void failInventoryReservation(
            InventoryFailureReason reason,
            UserId recordedBy,
            Instant recordedAt,
            CorrelationId correlationId
    ) {
        Objects.requireNonNull(reason, "Inventory failure reason must not be null");
        Objects.requireNonNull(recordedBy, "Recorded by must not be null");
        Objects.requireNonNull(recordedAt, "Recorded at must not be null");
        Objects.requireNonNull(correlationId, "Correlation ID must not be null");
        requireInventoryPending();

        status = OrderStatus.INVENTORY_FAILED;
        inventoryResultRecordedBy = recordedBy;
        inventoryResultRecordedAt = recordedAt;
        inventoryFailureReason = reason;
        touch(recordedAt);
        registerEvent(new OrderInventoryReservationFailed(
                UUID.randomUUID(),
                id,
                reason,
                version,
                recordedBy,
                recordedAt,
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

    public Optional<UserId> submittedBy() {
        return Optional.ofNullable(submittedBy);
    }

    public Optional<Instant> submittedAt() {
        return Optional.ofNullable(submittedAt);
    }

    public Optional<UserId> decisionBy() {
        return Optional.ofNullable(decisionBy);
    }

    public Optional<Instant> decisionAt() {
        return Optional.ofNullable(decisionAt);
    }

    public Optional<ApprovalComment> decisionComment() {
        return Optional.ofNullable(decisionComment);
    }

    public Optional<UserId> cancelledBy() {
        return Optional.ofNullable(cancelledBy);
    }

    public Optional<Instant> cancelledAt() {
        return Optional.ofNullable(cancelledAt);
    }

    public Optional<CancellationReason> cancellationReason() {
        return Optional.ofNullable(cancellationReason);
    }

    public Optional<UserId> processingStartedBy() {
        return Optional.ofNullable(processingStartedBy);
    }

    public Optional<Instant> processingStartedAt() {
        return Optional.ofNullable(processingStartedAt);
    }

    public Optional<UserId> inventoryRequestedBy() {
        return Optional.ofNullable(inventoryRequestedBy);
    }

    public Optional<Instant> inventoryRequestedAt() {
        return Optional.ofNullable(inventoryRequestedAt);
    }

    public Optional<UserId> inventoryResultRecordedBy() {
        return Optional.ofNullable(inventoryResultRecordedBy);
    }

    public Optional<Instant> inventoryResultRecordedAt() {
        return Optional.ofNullable(inventoryResultRecordedAt);
    }

    public Optional<InventoryFailureReason> inventoryFailureReason() {
        return Optional.ofNullable(inventoryFailureReason);
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







    private void requireInventoryPending() {
        if (status != OrderStatus.INVENTORY_PENDING) {
            throw new OrderInventoryResultNotAllowedException(
                    "Inventory result can be recorded only while the order is IN INVENTORY_PENDING status");
        }
    }

    private void requireProcessingStatus(
            OrderStatus requiredStatus,
            String message
    ) {
        if (status != requiredStatus) {
            throw new OrderProcessingNotAllowedException(message);
        }
    }

    private void requireCancellable() {
        if (!CANCELLABLE_STATUSES.contains(status)) {
            throw new OrderCancellationNotAllowedException(
                    "Order cannot be cancelled from status " + status);
        }
    }

    private void requireReviewRequested() {
        if (status != OrderStatus.REVIEW_REQUESTED) {
            throw new OrderRevisionNotAllowedException(
                    "Only a REVIEW_REQUESTED order can be reopened for revision");
        }
    }

    private void clearDecision() {
        decisionBy = null;
        decisionAt = null;
        decisionComment = null;
    }

    private void requirePendingApproval() {
        requireStatus(
                OrderStatus.PENDING_APPROVAL,
                "Only a PENDING_APPROVAL order can receive an approval decision");
    }

    private void requireStatus(OrderStatus requiredStatus, String message) {
        if (status != requiredStatus) {
            throw new OrderApprovalNotAllowedException(message);
        }
    }

    private void recordDecision(
            UserId decidedBy,
            Instant decidedAt,
            ApprovalComment comment
    ) {
        decisionBy = decidedBy;
        decisionAt = decidedAt;
        decisionComment = comment;
    }

    private void requireDraftForSubmission() {
        if (status != OrderStatus.DRAFT) {
            throw new OrderSubmissionNotAllowedException(
                    "Only a DRAFT order can be submitted");
        }
    }

    private void requireSubmissionReady() {
        if (items.isEmpty()) {
            throw new OrderSubmissionNotAllowedException(
                    "An order must contain at least one item before submission");
        }
        if (!pricingComplete()) {
            throw new OrderSubmissionNotAllowedException(
                    "Every order item must be priced before submission");
        }
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

    private void requireCompatibleCurrency(ItemPricing pricing) {
        items.stream()
                .flatMap(item -> item.pricing().stream())
                .map(existingPricing -> existingPricing.unitPrice().currency())
                .findFirst()
                .filter(existingCurrency -> !existingCurrency.equals(pricing.unitPrice().currency()))
                .ifPresent(existingCurrency -> {
                    throw new OrderCurrencyMismatchException(
                            "All order items must use the same currency");
                });
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
