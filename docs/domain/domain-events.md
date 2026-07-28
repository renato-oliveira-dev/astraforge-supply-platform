# Domain Events

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Domain Events |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

## 1. Purpose

This document defines the Domain Event model used by Enterprise Order Platform.

It establishes:

- Domain Event responsibilities
- Event naming conventions
- Standard metadata
- Event payload principles
- Event ownership
- Event creation rules
- Event handling rules
- Mapping from Domain Events to Integration Events
- Transactional Outbox integration
- Idempotency requirements
- Ordering rules
- Versioning rules
- Security and privacy requirements
- Testing expectations
- Initial Order Domain Event catalog

Domain Events represent relevant business facts that occurred inside a bounded context.

They are immutable statements about completed domain behavior.

---

## 2. Domain Event Definition

A Domain Event represents something meaningful that occurred within a domain model.

Examples:

```text
OrderCreated
OrderItemAdded
OrderSubmitted
OrderApproved
InventoryReservationRequested
OrderCancelled
```

A Domain Event:

- Describes a completed business fact
- Uses past-tense naming
- Is created by domain behavior
- Is immutable
- Belongs to one bounded context
- May trigger additional local behavior
- May become an Integration Event
- Must not contain infrastructure implementation details

A Domain Event is not:

- A command
- A request for future execution
- A database entity
- A REST request
- An SQS/AWS-specific object
- A log entry
- An audit record
- A direct replacement for aggregate state

---

## 3. Domain Events and Commands

Commands express intent.

Domain Events express completed facts.

Example:

```text
Command:
SubmitOrder

Domain Event:
OrderSubmitted
```

Another example:

```text
Command:
CancelOrder

Domain Event:
OrderCancelled
```

Commands may be rejected.

Domain Events describe behavior that already succeeded.

Incorrect event naming:

```text
SubmitOrder
CancelOrder
ReserveInventory
ApproveOrder
```

Correct event naming:

```text
OrderSubmitted
OrderCancelled
InventoryReservationRequested
OrderApproved
```

---

## 4. Domain Events and Integration Events

Domain Events and Integration Events are distinct concepts.

### 4.1 Domain Event

A Domain Event:

- Belongs to one bounded context
- Is created by domain behavior
- May contain context-specific terminology
- May remain internal
- Is handled inside the local application boundary
- Does not require an external contract

### 4.2 Integration Event

An Integration Event:

- Is intended for another bounded context or external system
- Uses a stable Published Language
- Has an explicit schema version
- Must preserve backward compatibility
- Must avoid internal implementation details
- Is published through reliable infrastructure
- Must be safe for external consumption

### 4.3 Mapping Rule

Not every Domain Event becomes an Integration Event.

Example:

```text
Domain Event:
OrderPricingInvalidated
```

This may remain internal because external consumers may not need to know that Draft Pricing became stale.

Example:

```text
Domain Event:
OrderSubmitted
```

This may become:

```text
Integration Event:
order.submitted.v1
```

The application layer or a dedicated event mapper is responsible for this transformation.

The aggregate must not create SQS message payloads directly.

---

## 5. Domain Event Ownership

Each bounded context owns its Domain Events.

Examples:

| Domain Event | Owning Context |
|---|---|
| OrderCreated | Order Management |
| OrderSubmitted | Order Management |
| OrderApproved | Order Management |
| ApprovalProcessStarted | Approval |
| ApprovalDecisionRecorded | Approval |
| InventoryReserved | Inventory |
| InventoryReservationFailed | Inventory |
| PaymentConfirmed | Payment |
| ShipmentCreated | Fulfillment |
| NotificationSent | Notification |

A context must not create another context's Domain Event.

Order Management may react to `InventoryReserved`, but Inventory owns the event describing that Reservation fact.

Order Management may then create its own event describing the resulting Order transition.

Example:

```text
Inventory Domain Event:
InventoryReserved

Order Domain Event:
OrderInventoryReservationConfirmed
```

The terminology must reflect context ownership.

---

## 6. Event Naming Convention

Domain Event names must:

- Use past tense
- Describe one completed business fact
- Use the ubiquitous language
- Avoid technical terminology
- Avoid generic names
- Avoid transport-specific names
- Remain explicit

Preferred:

```text
OrderCreated
OrderItemAdded
OrderItemQuantityChanged
OrderPriced
OrderSubmitted
OrderApproved
OrderRejected
OrderCancelled
```

Avoid:

```text
OrderEvent
OrderUpdated
DataChanged
ProcessCompleted
MessageReceived
EntitySaved
StatusChanged
KafkaOrderMessage
```

`StatusChanged` is usually too generic.

A more explicit event should describe the business transition:

```text
OrderApproved
OrderRejected
OrderCancelled
OrderCompleted
```

---

## 7. Standard Event Metadata

Every Domain Event should contain common metadata.

Conceptual structure:

```text
DomainEvent
├── eventId
├── aggregateId
├── aggregateType
├── aggregateVersion
├── occurredAt
├── correlationId
├── causationId
└── eventType
```

The business payload remains specific to each event.

---

## 8. Event Identifier

### 8.1 EventId

`EventId` uniquely identifies one Domain Event instance.

Possible representation:

```text
EventId(UUID value)
```

Rules:

- Must be globally unique
- Must not be null
- Must remain stable
- Must be preserved when mapped to an Integration Event where appropriate
- Must not be regenerated during technical publication retries

A retry of event publication must reuse the original Event ID.

Creating a new Event ID for every retry would represent multiple business events incorrectly.

---

## 9. Aggregate Identifier

### 9.1 AggregateId

The Aggregate ID identifies the aggregate that produced the event.

For Order events:

```text
aggregateType = ORDER
aggregateId = OrderId
```

Rules:

- Must identify the originating aggregate
- Must not be null
- Must use a stable representation
- Must not expose persistence-specific composite objects

---

## 10. Aggregate Type

`AggregateType` identifies the aggregate category.

Examples:

```text
ORDER
APPROVAL_PROCESS
INVENTORY_RESERVATION
PAYMENT
SHIPMENT
```

Rules:

- Must be stable
- Must use business language
- Must not use Java class names as the public representation
- Must not contain package names
- Must not depend on ORM entity names

---

## 11. Aggregate Version

`AggregateVersion` represents the aggregate version associated with the business fact.

Example:

```text
aggregateVersion = 7
```

Rules:

- Should represent the version after the state change
- Must increase monotonically for one aggregate
- May support stale-event detection
- May support event ordering within one aggregate stream
- Must not be treated as global event order

The final implementation may obtain the version from:

- Domain-managed version
- Persistence optimistic-lock version
- Explicit application sequence

The chosen approach must be consistent.

---

## 12. Occurrence Time

`OccurredAt` represents when the business fact occurred.

Possible representation:

```text
Instant
```

Rules:

- Must use UTC
- Must not depend on the consumer's local timezone
- Must be supplied through a Clock or explicit parameter
- Must not be replaced by publication time
- Must remain unchanged during retries

The following timestamps are different:

```text
occurredAt
persistedAt
publishedAt
processedAt
```

They must not be treated as interchangeable.

---

## 13. Correlation Identifier

`CorrelationId` groups operations that belong to the same business flow.

Example:

```text
HTTP request
    |
    v
Submit Order
    |
    v
OrderSubmitted
    |
    v
ApprovalProcessRequested
    |
    v
OrderApproved
```

All steps may preserve the same Correlation ID.

Rules:

- Must be propagated when available
- Must remain stable across the business flow
- Must not contain secrets
- Must not be generated again at every layer
- Must not replace Event ID

---

## 14. Causation Identifier

`CausationId` identifies the command or event that caused another event.

Example:

```text
OrderSubmitted event ID
        |
        v
ApprovalProcessRequested causation ID
```

Possible values:

- Command ID
- Previous Event ID
- Request ID

Rules:

- May be absent for the first event in a flow
- Should be propagated when event chains are relevant
- Must remain distinct from Correlation ID
- Must not be used as the Event ID

---

## 15. Event Type

The internal event type should remain stable and explicit.

Examples:

```text
OrderCreated
OrderSubmitted
OrderCancelled
```

The external event type may use a separate naming format:

```text
order.created.v1
order.submitted.v1
order.cancelled.v1
```

Internal Java class names must not automatically become public event contract names.

---

## 16. Base Domain Event Contract

A conceptual Java contract may be:

```java
public interface DomainEvent {

    EventId eventId();

    AggregateId aggregateId();

    AggregateType aggregateType();

    long aggregateVersion();

    Instant occurredAt();

    CorrelationId correlationId();

    CausationId causationId();
}
```

The final implementation may use a generic identifier strategy.

The interface must remain independent from:

- Spring
- JPA
- Amazon SQS
- Jackson
- Messaging framework annotations

---

## 17. Event Immutability

Domain Events must be immutable.

Recommended Java implementation:

- Records
- Final classes
- Immutable value objects
- Defensive copies for collections

Example:

```java
public record OrderCreated(
        EventId eventId,
        OrderId aggregateId,
        long aggregateVersion,
        CustomerId customerId,
        ActorId createdBy,
        Instant occurredAt,
        CorrelationId correlationId,
        CausationId causationId
) implements OrderDomainEvent {
}
```

Rules:

- No setters
- No mutable collections
- No mutable date types
- No entity references
- No lazy-loaded objects
- No persistence proxies
- No framework-specific fields

---

## 18. Domain Event Collection

The aggregate may maintain an internal collection of uncommitted Domain Events.

Conceptual structure:

```java
private final List<DomainEvent> domainEvents;
```

Possible operations:

```java
public List<DomainEvent> domainEvents() {
    return List.copyOf(domainEvents);
}

public void clearDomainEvents() {
    domainEvents.clear();
}

private void recordEvent(DomainEvent event) {
    domainEvents.add(Objects.requireNonNull(event));
}
```

Rules:

- The returned collection must be immutable
- External code must not add events directly
- Events must be recorded only after successful state changes
- Events must not be cleared before reliable application processing
- Persistence infrastructure must not fabricate missing Domain Events
- Domain Events must not be published directly from the aggregate

---

## 19. Event Creation Timing

An event must be created only after the aggregate successfully applies the corresponding state change.

Correct:

```text
1. Validate transition
2. Change aggregate state
3. Append status history
4. Record Domain Event
```

Incorrect:

```text
1. Record event
2. Attempt validation
3. Validation fails
```

A rejected operation must not produce a success event.

Example:

```text
Rejected cancellation attempt
```

must not create:

```text
OrderCancelled
```

---

## 20. Event Payload Principles

A Domain Event payload should contain the information required to describe the fact.

It should not contain every aggregate field.

Include:

- Stable identifiers
- Relevant business values
- Transition information
- Actor reference
- Business timestamp
- Correlation metadata
- Relevant reason or outcome

Avoid:

- Entire aggregate serialization
- Persistence entities
- Secrets
- Access tokens
- Passwords
- Unnecessary personal data
- Framework objects
- Repository references
- Mutable objects
- Large binary content
- Internal caches

---

## 21. Minimal Event Payload

Events should be minimal but useful.

Example:

```text
OrderItemAdded
├── orderId
├── orderItemId
├── productId
├── quantity
├── occurredAt
└── actorId
```

The event does not necessarily require:

- Customer complete address
- Full Product description
- Entire Pricing Snapshot
- Full Order Item collection
- User authentication token

Payload design should reflect actual event consumers.

---

## 22. Event Facts and Snapshots

Events may use one of two payload strategies.

### 22.1 Reference-Based Event

Contains identifiers only.

Example:

```text
OrderSubmitted
├── orderId
├── customerId
└── grandTotal
```

Advantages:

- Small payload
- Lower duplication
- Reduced sensitive data

Disadvantages:

- Consumers may need additional queries
- Consumer result may reflect newer state
- Provider availability may be required

### 22.2 Snapshot-Based Event

Contains required historical values.

Example:

```text
OrderReadyForFulfillment
├── orderId
├── customerDeliverySnapshot
└── itemSnapshots
```

Advantages:

- Consumer autonomy
- Historical consistency
- Fewer synchronous dependencies

Disadvantages:

- Larger payload
- Schema evolution complexity
- Greater privacy considerations

The choice must be explicit for every Integration Event.

Domain Events may remain smaller than their mapped Integration Events.

---

## 23. Order Domain Event Hierarchy

A conceptual hierarchy may be:

```text
DomainEvent
    |
    +-- OrderDomainEvent
            |
            +-- OrderCreated
            +-- OrderNumberAssigned
            +-- OrderItemAdded
            +-- OrderItemQuantityChanged
            +-- OrderItemRemoved
            +-- OrderPricingInvalidated
            +-- OrderPriced
            +-- OrderSubmitted
            +-- OrderApprovalRequired
            +-- OrderApproved
            +-- OrderRejected
            +-- OrderReviewRequested
            +-- OrderProcessingStarted
            +-- InventoryReservationRequested
            +-- OrderInventoryReservationConfirmed
            +-- OrderInventoryReservationFailed
            +-- OrderReadyForFulfillment
            +-- OrderFulfillmentStarted
            +-- OrderCancelled
            +-- InventoryReleaseRequested
            +-- OrderCompleted
```

The exact class names may be refined to preserve context ownership.

---

## 24. OrderCreated

### 24.1 Meaning

An Order aggregate was successfully created.

### 24.2 Trigger

```text
Order.create(...)
```

### 24.3 Preconditions

- Order ID is valid
- Customer Reference is valid
- Actor is valid
- Creation timestamp is valid

### 24.4 Payload

```text
OrderCreated
├── eventId
├── orderId
├── aggregateVersion
├── customerId
├── createdBy
├── occurredAt
├── correlationId
└── causationId
```

### 24.5 Local Consumers

Possible local consumers:

- Audit fact mapper
- Metrics listener
- Order Number assignment workflow

### 24.6 Integration Mapping

Possible external event:

```text
order.created.v1
```

Publishing this event externally is optional.

Draft Order creation may not be relevant to every external consumer.

---

## 25. OrderNumberAssigned

### 25.1 Meaning

A business-facing Order Number was assigned.

### 25.2 Trigger

```text
order.assignOrderNumber(...)
```

### 25.3 Preconditions

- Order Number is valid
- Order Number is not already assigned
- Current lifecycle state allows assignment

### 25.4 Payload

```text
OrderNumberAssigned
├── eventId
├── orderId
├── orderNumber
├── aggregateVersion
├── assignedAt
├── assignedBy
├── correlationId
└── causationId
```

### 25.5 Integration Mapping

Possible external event:

```text
order.number-assigned.v1
```

This should only be published when consumers require the assignment as an independent fact.

---

## 26. OrderItemAdded

### 26.1 Meaning

A Product was added to a Draft Order as a new Order Item.

### 26.2 Trigger

```text
order.addItem(...)
```

### 26.3 Preconditions

- Order is editable
- Product Reference is valid
- Product Snapshot is valid
- Quantity is valid
- Order Item ID is unique
- Duplicate Product policy is satisfied

### 26.4 Payload

```text
OrderItemAdded
├── eventId
├── orderId
├── orderItemId
├── productId
├── quantity
├── aggregateVersion
├── addedBy
├── occurredAt
├── correlationId
└── causationId
```

### 26.5 Side Effects

The operation may also record:

```text
OrderPricingInvalidated
```

when previously calculated Pricing becomes stale.

### 26.6 Integration Mapping

This event is expected to remain internal initially.

Draft item modifications usually do not require external publication.

---

## 27. OrderItemQuantityChanged

### 27.1 Meaning

The Quantity of an existing Order Item changed.

### 27.2 Trigger

```text
order.updateItemQuantity(...)
```

### 27.3 Preconditions

- Order is editable
- Order Item exists
- New Quantity is valid
- New Quantity differs from the current value

### 27.4 Payload

```text
OrderItemQuantityChanged
├── eventId
├── orderId
├── orderItemId
├── productId
├── previousQuantity
├── newQuantity
├── aggregateVersion
├── changedBy
├── occurredAt
├── correlationId
└── causationId
```

### 27.5 Side Effects

Pricing becomes invalid when the Quantity affects commercial values.

### 27.6 Integration Mapping

Expected to remain internal initially.

---

## 28. OrderItemRemoved

### 28.1 Meaning

An Order Item was removed from a Draft Order.

### 28.2 Trigger

```text
order.removeItem(...)
```

### 28.3 Preconditions

- Order is editable
- Order Item exists

### 28.4 Payload

```text
OrderItemRemoved
├── eventId
├── orderId
├── orderItemId
├── productId
├── previousQuantity
├── aggregateVersion
├── removedBy
├── occurredAt
├── correlationId
└── causationId
```

### 28.5 Side Effects

Pricing becomes invalid when applicable.

### 28.6 Integration Mapping

Expected to remain internal initially.

---

## 29. OrderPricingInvalidated

### 29.1 Meaning

Previously calculated Pricing is no longer valid because Pricing-relevant Order data changed.

### 29.2 Triggers

Possible triggers:

- Item added
- Item removed
- Quantity changed
- Customer changed
- Delivery information changed
- Payment conditions changed
- Discount request changed

### 29.3 Payload

```text
OrderPricingInvalidated
├── eventId
├── orderId
├── previousPricingVersion
├── invalidationReason
├── aggregateVersion
├── occurredAt
├── correlationId
└── causationId
```

### 29.4 Integration Mapping

This event remains internal unless an external Pricing workflow requires it.

### 29.5 Rules

- Must not be emitted when no accepted Pricing exists
- Must describe the cause
- Must not contain the full previous Pricing result
- Must not imply that external repricing already occurred

---

## 30. OrderPriced

### 30.1 Meaning

A complete and accepted Pricing result was applied to the Order.

### 30.2 Trigger

```text
order.applyPricing(...)
```

### 30.3 Preconditions

- Order is in a repricable state
- Every Order Item has a corresponding Pricing result
- Currency is consistent
- Totals are valid
- Pricing result is complete

### 30.4 Payload

```text
OrderPriced
├── eventId
├── orderId
├── currency
├── subtotal
├── discountTotal
├── taxTotal
├── feeTotal
├── freightTotal
├── grandTotal
├── pricingPolicyVersion
├── calculatedAt
├── aggregateVersion
├── appliedBy
├── occurredAt
├── correlationId
└── causationId
```

### 30.5 Integration Mapping

Possible external event:

```text
order.priced.v1
```

This should only be published if another context requires Pricing acceptance as an independent fact.

Draft repricing may remain internal.

---

## 31. OrderSubmitted

### 31.1 Meaning

A valid Draft Order was submitted for processing.

### 31.2 Trigger

```text
order.submit(...)
```

### 31.3 Preconditions

- Order is in `DRAFT`
- Order contains at least one item
- Customer Snapshot is complete
- Product Snapshots are complete
- Pricing is present and current
- Totals are consistent
- Submission has not occurred previously

### 31.4 Payload

```text
OrderSubmitted
├── eventId
├── orderId
├── orderNumber
├── customerId
├── itemCount
├── totalQuantity
├── currency
├── grandTotal
├── approvalRequired
├── submittedBy
├── submittedAt
├── aggregateVersion
├── correlationId
└── causationId
```

### 31.5 Local Consumers

Possible local consumers:

- Approval request mapper
- Inventory workflow coordinator
- Audit fact mapper
- Notification request mapper
- Integration Event mapper

### 31.6 Integration Mapping

External event:

```text
order.submitted.v1
```

### 31.7 Security

The event must not expose:

- Access tokens
- Complete Customer confidential data
- Unnecessary addresses
- Full internal aggregate state
- Internal approval calculations

---

## 32. OrderApprovalRequired

### 32.1 Meaning

The submitted Order requires an Approval Process.

### 32.2 Trigger

Submission with an Approval requirement.

### 32.3 Preconditions

- Order was successfully submitted
- Approval evaluation determined that Approval is required
- Current Order status is `PENDING_APPROVAL`

### 32.4 Payload

```text
OrderApprovalRequired
├── eventId
├── orderId
├── orderNumber
├── approvalRequestId
├── approvalPolicyReference
├── requiredLevels
├── grandTotal
├── currency
├── aggregateVersion
├── occurredAt
├── correlationId
└── causationId
```

### 32.5 Integration Mapping

External command-style message:

```text
approval.process-requested.v1
```

This is not necessarily an Integration Event describing a completed Approval fact.

It may be modeled as an asynchronous command.

The distinction must be explicit.

### 32.6 Rules

- The Approval Request ID must remain stable during retries
- Duplicate publication must not create duplicate Approval Processes
- Approval facts must be immutable for the workflow instance
- Approval must not receive the complete Order aggregate

---

## 33. OrderApproved

### 33.1 Meaning

Order Management accepted a valid Approval outcome and transitioned the Order accordingly.

### 33.2 Trigger

Application processing of a valid Approval outcome.

### 33.3 Preconditions

- Current status is `PENDING_APPROVAL`
- Approval Process ID matches
- Approval outcome is valid
- Event has not been processed previously

### 33.4 Payload

```text
OrderApproved
├── eventId
├── orderId
├── orderNumber
├── approvalProcessId
├── approvedBy
├── approvedAt
├── comments
├── aggregateVersion
├── correlationId
└── causationId
```

### 33.5 Integration Mapping

External event:

```text
order.approved.v1
```

### 33.6 Rules

- Comments may be omitted
- Sensitive user details must not be included
- Duplicate Approval outcomes must not create duplicate OrderApproved events
- Approval history remains owned by Approval Context

---

## 34. OrderRejected

### 34.1 Meaning

Order Management accepted a valid Approval rejection outcome.

### 34.2 Trigger

Application processing of an Approval rejection.

### 34.3 Preconditions

- Current status is `PENDING_APPROVAL`
- Approval Process ID matches
- Rejection Reason is present
- Event has not been processed previously

### 34.4 Payload

```text
OrderRejected
├── eventId
├── orderId
├── orderNumber
├── approvalProcessId
├── rejectedBy
├── rejectedAt
├── rejectionReason
├── aggregateVersion
├── correlationId
└── causationId
```

### 34.5 Integration Mapping

External event:

```text
order.rejected.v1
```

### 34.6 Rules

- Rejection Reason must use stable business terminology
- Provider-specific workflow details must not leak
- Rejection must not be represented only as a generic status update

---

## 35. OrderReviewRequested

### 35.1 Meaning

An approver requested changes before the Order can continue.

### 35.2 Trigger

Application processing of a valid Review Request outcome.

### 35.3 Preconditions

- Current status is `PENDING_APPROVAL`
- Approval Process ID matches
- Review Reason is present

### 35.4 Payload

```text
OrderReviewRequested
├── eventId
├── orderId
├── orderNumber
├── approvalProcessId
├── requestedBy
├── requestedAt
├── reviewReason
├── aggregateVersion
├── correlationId
└── causationId
```

### 35.5 Integration Mapping

External event:

```text
order.review-requested.v1
```

### 35.6 Rules

- The event does not automatically imply that the Order is editable
- Reopening the Order may be a separate domain operation
- The business reason must be preserved

---

## 36. OrderProcessingStarted

### 36.1 Meaning

The Order entered the processing phase after submission or Approval.

### 36.2 Trigger

Possible triggers:

- Submission without required Approval
- Continuation after Approval

### 36.3 Payload

```text
OrderProcessingStarted
├── eventId
├── orderId
├── orderNumber
├── previousStatus
├── aggregateVersion
├── startedAt
├── correlationId
└── causationId
```

### 36.4 Integration Mapping

Possible external event:

```text
order.processing-started.v1
```

This event should be published only when downstream consumers require this lifecycle fact.

---

## 37. InventoryReservationRequested

### 37.1 Meaning

Order Management requested Inventory Reservation for the Order.

### 37.2 Trigger

```text
order.requestInventoryReservation(...)
```

### 37.3 Preconditions

- Order is in an Inventory-eligible status
- Required Approval is complete
- Order Items are valid
- Reservation Request ID is present
- No equivalent active request already exists

### 37.4 Payload

```text
InventoryReservationRequested
├── eventId
├── orderId
├── orderNumber
├── reservationRequestId
├── requestedAt
├── expiresAt
├── aggregateVersion
├── correlationId
├── causationId
└── items
    ├── productId
    └── requestedQuantity
```

### 37.5 Integration Mapping

Asynchronous request:

```text
inventory.reservation-requested.v1
```

### 37.6 Rules

- Reservation Request ID must be idempotent
- Item data must be immutable
- The event must not contain Product Catalog entities
- Publication retries must reuse the Event ID and Reservation Request ID
- Large Order payload limits must be considered

---

## 38. OrderInventoryReservationConfirmed

### 38.1 Meaning

Order Management accepted a successful Inventory Reservation outcome.

### 38.2 Trigger

Processing of an Inventory Reservation success message.

### 38.3 Preconditions

- Current status is `INVENTORY_PENDING`
- Reservation Request ID matches
- Reservation ID is valid
- Reserved quantities satisfy the configured policy
- Outcome has not been processed previously

### 38.4 Payload

```text
OrderInventoryReservationConfirmed
├── eventId
├── orderId
├── orderNumber
├── reservationRequestId
├── reservationId
├── reservedAt
├── aggregateVersion
├── correlationId
└── causationId
```

### 38.5 Integration Mapping

Possible external event:

```text
order.inventory-reserved.v1
```

The Inventory context may separately publish:

```text
inventory.reserved.v1
```

These are not the same event.

One describes Inventory's Reservation fact.

The other describes the resulting Order lifecycle fact.

---

## 39. OrderInventoryReservationFailed

### 39.1 Meaning

Order Management accepted an Inventory Reservation failure outcome.

### 39.2 Trigger

Processing of a failed Reservation result.

### 39.3 Preconditions

- Current status is `INVENTORY_PENDING`
- Reservation Request ID matches
- Failure Reason is present
- Outcome has not been processed previously

### 39.4 Payload

```text
OrderInventoryReservationFailed
├── eventId
├── orderId
├── orderNumber
├── reservationRequestId
├── failureCode
├── failureReason
├── failedAt
├── aggregateVersion
├── correlationId
└── causationId
```

### 39.5 Integration Mapping

Possible external event:

```text
order.inventory-reservation-failed.v1
```

### 39.6 Rules

- Technical provider errors must be translated
- Failure Code should be stable
- Sensitive internal warehouse details must not be exposed
- Retryable and non-retryable outcomes should remain distinguishable

---

## 40. OrderReadyForFulfillment

### 40.1 Meaning

The Order satisfied all prerequisites required to begin Fulfillment.

### 40.2 Trigger

```text
order.markReadyForFulfillment(...)
```

### 40.3 Preconditions

- Inventory is successfully reserved
- Required Approval is complete
- Required Payment condition is satisfied when applicable
- Order is not cancelled
- Order is not completed

### 40.4 Payload

```text
OrderReadyForFulfillment
├── eventId
├── orderId
├── orderNumber
├── customerId
├── reservationId
├── deliverySnapshotReference
├── aggregateVersion
├── occurredAt
├── correlationId
└── causationId
```

### 40.5 Integration Mapping

Asynchronous request:

```text
fulfillment.requested.v1
```

or lifecycle event:

```text
order.ready-for-fulfillment.v1
```

The project must distinguish the business fact from the downstream command.

### 40.6 Rules

- Fulfillment payload may require Product and delivery snapshots
- Snapshot content must be explicitly versioned
- Sensitive Customer data must be minimized
- Duplicate requests must not create duplicate Shipments

---

## 41. OrderFulfillmentStarted

### 41.1 Meaning

Order Management accepted that Fulfillment processing started.

### 41.2 Trigger

Processing of a Fulfillment start outcome.

### 41.3 Preconditions

- Current status is `READY_FOR_FULFILLMENT`
- Fulfillment Request ID matches
- Fulfillment ID is valid
- Outcome has not been processed previously

### 41.4 Payload

```text
OrderFulfillmentStarted
├── eventId
├── orderId
├── orderNumber
├── fulfillmentRequestId
├── fulfillmentId
├── startedAt
├── aggregateVersion
├── correlationId
└── causationId
```

### 41.5 Integration Mapping

External event:

```text
order.fulfillment-started.v1
```

---

## 42. OrderCancelled

### 42.1 Meaning

The Order was successfully cancelled.

### 42.2 Trigger

```text
order.cancel(...)
```

### 42.3 Preconditions

- Current status is cancellable
- Cancellation Reason is present
- Actor is present
- Order is not completed
- Order is not already cancelled

### 42.4 Payload

```text
OrderCancelled
├── eventId
├── orderId
├── orderNumber
├── previousStatus
├── cancellationCode
├── cancellationReason
├── cancelledBy
├── cancelledAt
├── inventoryReservationId
├── paymentReference
├── fulfillmentReference
├── aggregateVersion
├── correlationId
└── causationId
```

Optional process references should only be included when needed.

### 42.5 Local Consumers

Possible local consumers:

- Inventory Release mapper
- Payment compensation mapper
- Fulfillment cancellation mapper
- Notification request mapper
- Audit fact mapper
- Integration Event mapper

### 42.6 Integration Mapping

External event:

```text
order.cancelled.v1
```

### 42.7 Rules

- Cancellation is not deletion
- The event must preserve the previous status
- Compensation requests must be separate messages
- The event must not claim that compensation already completed
- Duplicate cancellation commands must not produce duplicate events

---

## 43. InventoryReleaseRequested

### 43.1 Meaning

Order Management requested release of Inventory reserved for a cancelled or invalidated Order.

### 43.2 Trigger

Cancellation or another explicit business operation requiring release.

### 43.3 Preconditions

- A Reservation exists
- Release is required by policy
- No equivalent Release request is already active

### 43.4 Payload

```text
InventoryReleaseRequested
├── eventId
├── orderId
├── orderNumber
├── releaseRequestId
├── reservationId
├── releaseReason
├── requestedAt
├── aggregateVersion
├── correlationId
└── causationId
```

### 43.5 Integration Mapping

Asynchronous request:

```text
inventory.release-requested.v1
```

### 43.6 Rules

- Release Request ID must be idempotent
- The message describes a request, not a completed Release
- Inventory owns the resulting `InventoryReleased` fact
- Publication retries must preserve identifiers

---

## 44. OrderCompleted

### 44.1 Meaning

The Order business lifecycle completed successfully.

### 44.2 Trigger

```text
order.complete(...)
```

### 44.3 Preconditions

- Fulfillment completed
- Required Payment conditions are satisfied
- Order is not cancelled
- Order is not already completed
- Current state permits completion

### 44.4 Payload

```text
OrderCompleted
├── eventId
├── orderId
├── orderNumber
├── customerId
├── completedAt
├── finalGrandTotal
├── currency
├── aggregateVersion
├── correlationId
└── causationId
```

### 44.5 Integration Mapping

External event:

```text
order.completed.v1
```

### 44.6 Rules

- Completion occurs only once
- Completed Orders are terminal
- Duplicate completion outcomes must not generate duplicate events
- Final totals must reflect the accepted Order snapshot
- The event must not include unnecessary full snapshots

---

## 45. Internal Domain Event Handling

Internal handlers may react to Domain Events within the same bounded context.

Examples:

```text
OrderSubmitted
    |
    +--> Create Audit Fact
    |
    +--> Map Integration Event
    |
    +--> Prepare Approval Request
```

Rules:

- Handlers must not bypass aggregate behavior
- Handlers must not mutate aggregate state after persistence without a new transaction
- Handler execution order must not be implicit
- Required transactional handlers must be explicit
- Failure behavior must be defined
- Handlers must remain idempotent where execution may repeat

---

## 46. Transactional Event Handling

Domain Events may be processed before transaction commit to create local records such as:

- Outbox Records
- Audit Fact records
- Idempotency records
- Context-owned projections

These operations may participate in the same database transaction.

Example:

```text
Application Service
    |
    +--> Load Order
    |
    +--> Execute aggregate behavior
    |
    +--> Save Order
    |
    +--> Read Domain Events
    |
    +--> Create Outbox Records
    |
    +--> Commit
```

After commit:

```text
Outbox Publisher
    |
    +--> Publish Integration Event
```

The system must not publish an external event before the related business transaction commits.

---

## 47. Transactional Outbox

The Transactional Outbox Pattern ensures that business data and event publication intent are stored atomically.

### 47.1 Outbox Flow

```text
1. Execute business operation
2. Persist aggregate state
3. Persist Outbox Record
4. Commit local transaction
5. Outbox Publisher reads pending record
6. Publish message
7. Mark Outbox Record as published
```

### 47.2 Failure Scenario

Without Outbox:

```text
Database commit succeeds
SQS publication fails
```

Result:

```text
Business state changed
External consumers never receive the event
```

With Outbox:

```text
Business state and publication intent are persisted together
Publication may be retried safely
```

---

## 48. Outbox Record Structure

Conceptual structure:

```text
OutboxRecord
├── id
├── aggregateType
├── aggregateId
├── eventType
├── eventVersion
├── destination
├── payload
├── status
├── attempts
├── nextAttemptAt
├── lastError
├── correlationId
├── causationId
├── occurredAt
├── createdAt
└── publishedAt
```

Possible statuses:

```text
PENDING
PROCESSING
PUBLISHED
FAILED
DEAD
```

The final status model must avoid unnecessary complexity.

---

## 49. Outbox Atomicity

The following must occur in one local transaction:

```text
Order update
Order Status History update
Idempotency record
Outbox Record creation
```

When required by the use case.

The Outbox Publisher must not participate in the original business transaction.

---

## 50. Outbox Publication Rules

The publisher must:

- Read pending records in bounded batches
- Avoid publishing records that are not committed
- Preserve Event ID
- Preserve Event Version
- Preserve Correlation ID
- Apply retry rules
- Record publication attempts
- Avoid exposing sensitive payloads in logs
- Mark successful publication
- Handle concurrent publishers
- Recover records left in processing state
- Support operational monitoring

---

## 51. Outbox Concurrency

Multiple Outbox Publisher instances may run concurrently.

Possible controls:

- Database row locking
- `FOR UPDATE SKIP LOCKED`
- Optimistic status update
- Lease-based processing
- Database row claiming / lease ownership

The chosen strategy must prevent uncontrolled duplicate parallel publication.

At-least-once delivery may still produce duplicates.

Consumers must remain idempotent.

---

## 52. Outbox Retry Policy

Retries should apply only to transient failures.

Examples:

- Broker temporarily unavailable
- Network timeout
- Temporary authentication infrastructure failure
- Temporary destination throttling

Permanent failures may include:

- Invalid schema
- Unsupported event version
- Invalid destination
- Payload serialization failure caused by code defect
- Authorization permanently denied

Retry policy must define:

- Maximum attempts
- Initial delay
- Backoff strategy
- Maximum delay
- Jitter
- Dead-letter or terminal state
- Alert threshold

---

## 53. Integration Event Envelope

A stable event envelope may be:

```text
IntegrationEventEnvelope
├── eventId
├── eventType
├── eventVersion
├── aggregateType
├── aggregateId
├── aggregateVersion
├── occurredAt
├── publishedAt
├── producer
├── correlationId
├── causationId
├── contentType
└── payload
```

Example:

```json
{
  "eventId": "a6dbf9a6-f245-4af7-a228-8f1590d4cc81",
  "eventType": "order.submitted",
  "eventVersion": 1,
  "aggregateType": "ORDER",
  "aggregateId": "fdcc8622-4ec2-4598-b80f-4cc22608ca18",
  "aggregateVersion": 5,
  "occurredAt": "2026-07-21T20:15:30Z",
  "publishedAt": "2026-07-21T20:15:31Z",
  "producer": "enterprise-order-platform",
  "correlationId": "a2a3d4e5-f607-48ac-9ac7-3b78a61b0896",
  "causationId": "1dfabf8f-c381-4879-a86e-8cead547fb9a",
  "contentType": "application/json",
  "payload": {
    "orderId": "fdcc8622-4ec2-4598-b80f-4cc22608ca18",
    "orderNumber": "ORD-2026-000001",
    "customerId": "d9fe585d-594b-42e2-b6c2-a76914f9d1ac",
    "currency": "BRL",
    "grandTotal": 12500.00,
    "submittedAt": "2026-07-21T20:15:30Z"
  }
}
```

---

## 54. Integration Event Naming

Preferred event type format:

```text
<context>.<business-fact>
```

Version stored separately:

```text
eventType = order.submitted
eventVersion = 1
```

The event version is kept separately from the semantic event type.

Recommended:

```text
eventType = order.submitted
eventVersion = 1
```

Examples:

```text
order.created
order.submitted
order.approved
order.rejected
order.cancelled
order.completed
inventory.reservation-requested
inventory.release-requested
fulfillment.requested
```

---

## 55. Event Versioning

Every external event contract must define an explicit version.

### 55.1 Backward-Compatible Changes

Usually compatible:

- Adding optional fields
- Adding optional metadata
- Adding new event types
- Expanding documented non-exclusive values carefully

### 55.2 Breaking Changes

Usually breaking:

- Removing a field
- Renaming a field
- Changing a field type
- Changing field meaning
- Making an optional field required
- Changing identifier semantics
- Changing Currency representation
- Changing timestamp semantics
- Reusing enum values with new meaning

Breaking changes require a new event version.

---

## 56. Event Schema Governance

Every Integration Event should have:

- Owner
- Business purpose
- Event type
- Version
- Producer
- Consumers
- Required fields
- Optional fields
- Validation rules
- Example payload
- Compatibility policy
- Deprecation policy
- Security classification
- Retention policy
- Idempotency requirements
- Ordering requirements

Possible schema formats:

- JSON Schema
- AsyncAPI
- Avro
- Protobuf
- OpenAPI components for shared documentation

The technology choice must not replace semantic governance.

---

## 57. Consumer Idempotency

Consumers must assume at-least-once delivery.

Each consumer must detect duplicate processing.

Possible strategy:

```text
processed_event
├── consumer
├── event_id
├── processed_at
└── result_reference
```

Unique constraint:

```text
consumer + event_id
```

Processing flow:

```text
1. Receive event
2. Validate schema
3. Check Event ID
4. Begin transaction
5. Apply business effect
6. Store processed Event ID
7. Commit
8. Acknowledge message
```

The business effect and idempotency record should be persisted atomically where possible.

---

## 58. Duplicate Event Behavior

Duplicate delivery must not:

- Repeat Approval
- Reserve Inventory twice
- Release Inventory twice
- Create duplicate Shipment
- Confirm Payment twice
- Append duplicate Order History
- Send uncontrolled duplicate Notifications
- Produce duplicate Audit records

Possible response:

- Ignore duplicate safely
- Return the previously produced result
- Acknowledge the message
- Record a duplicate-processing metric

Duplicate delivery is expected behavior in an at-least-once system.

It must not be treated automatically as a critical error.

---

## 59. Event Ordering

Global ordering is not guaranteed.

Ordering may be required for events from the same aggregate.

Possible sequence:

```text
OrderSubmitted
OrderApproved
InventoryReservationRequested
OrderInventoryReservationConfirmed
OrderReadyForFulfillment
OrderCompleted
```

Controls may include:

- Aggregate Version
- Sequence Number
- FIFO MessageGroupId or aggregate sequence
- Expected current state
- Optimistic locking
- Stale-event rejection

A consumer must not apply an event that is incompatible with its current business state.

---

## 60. Stale Event Handling

An event may be stale when:

- Its Aggregate Version is older than an already processed event
- Its process identifier does not match the active process
- The aggregate is already in a terminal state
- A newer workflow instance replaced the original one
- The event arrived after a compensation

Possible handling:

- Ignore safely
- Record as stale
- Send to operational review
- Reject and dead-letter
- Trigger reconciliation

The correct behavior depends on the event type.

---

## 61. Event Replay

Event replay may be required for:

- Rebuilding projections
- Recovering consumers
- Reprocessing corrected handlers
- Migrating reporting models

Replay must not automatically repeat irreversible business actions.

Consumers must declare whether they are:

- Replay-safe
- Idempotent
- Projection-only
- Side-effecting
- Reconciliation-dependent

Notification, Payment, Shipment, and external command consumers require particular care.

---

## 62. Event Retention

Retention depends on event purpose.

Possible categories:

| Event Category | Retention |
|---|---|
| Operational messaging | Short or medium |
| Audit facts | Long-term |
| Analytics events | Policy-defined |
| Outbox published records | Limited operational period |
| Failed events | Until resolution and policy expiry |
| Idempotency records | At least maximum replay window |

Retention must comply with:

- Business requirements
- Privacy requirements
- Regulatory requirements
- Storage cost
- Replay requirements
- Operational investigation needs

---

## 63. Error Handling

Event processing failures must distinguish:

### Transient Failures

Examples:

- Temporary database outage
- Broker connectivity issue
- Temporary downstream unavailability
- Lock timeout

Possible action:

- Retry with backoff

### Permanent Failures

Examples:

- Unsupported schema version
- Missing required field
- Invalid business identifier
- Irrecoverable state conflict
- Serialization incompatibility

Possible action:

- Dead-letter
- Operational alert
- Manual reconciliation
- Contract correction

A permanent business rejection must not be retried indefinitely.

---

## 64. Dead-Letter Handling

Dead-letter processing must preserve:

- Original message
- Event ID
- Event Type
- Event Version
- Destination
- Failure reason
- Stack trace reference, when safe
- Attempt count
- First failure time
- Last failure time
- Correlation ID

Dead-letter storage must not expose secrets.

Operational procedures must define:

- Investigation ownership
- Reprocessing method
- Payload correction policy
- Maximum retention
- Alert severity
- Manual approval requirements

---

## 65. Event Security

Events must follow least-privilege data sharing.

Do not include:

- Passwords
- Access tokens
- Refresh tokens
- API keys
- Session identifiers
- Full payment card data
- Sensitive provider credentials
- Unnecessary personal information
- Internal security claims
- Stack traces
- Raw exception messages from external systems

Include only the information required by declared consumers.

---

## 66. Personal Data

Events containing personal data must define:

- Business purpose
- Data owner
- Legal or policy basis
- Consumers
- Retention period
- Encryption requirements
- Masking rules
- Deletion or anonymization strategy

Customer snapshots must not be added to every event by convenience.

Prefer references when consumers do not require historical data.

---

## 67. Logging Rules

When logging event processing, include:

- Event ID
- Event Type
- Event Version
- Aggregate ID
- Correlation ID
- Consumer
- Processing outcome
- Processing duration
- Retry attempt

Example:

```text
Processed order.submitted event
eventId=a6dbf9a6-f245-4af7-a228-8f1590d4cc81
aggregateId=fdcc8622-4ec2-4598-b80f-4cc22608ca18
consumer=approval-request-handler
outcome=SUCCESS
elapsedMs=42
```

Do not log complete event payloads by default.

---

## 68. Metrics

Recommended metrics include:

```text
domain_events_created_total
outbox_records_created_total
outbox_publication_success_total
outbox_publication_failure_total
outbox_publication_latency
outbox_pending_records
outbox_oldest_pending_age
integration_events_consumed_total
integration_event_processing_failure_total
integration_event_duplicate_total
integration_event_stale_total
dead_letter_events_total
```

Metrics should include controlled labels such as:

- Event Type
- Consumer
- Outcome

Avoid high-cardinality labels such as:

- Event ID
- Order ID
- Customer ID
- Correlation ID

---

## 69. Tracing

Trace propagation may include:

- Trace ID
- Span ID
- Correlation ID
- Causation ID

Tracing metadata must remain separate from business payload where practical.

Domain Events should not depend directly on OpenTelemetry types.

Infrastructure adapters may translate metadata into tracing frameworks.

---

## 70. Serialization

Domain Events must not depend on serialization libraries.

Integration Event serialization belongs to infrastructure.

Recommended separation:

```text
OrderSubmitted
        |
        v
OrderSubmittedIntegrationEventMapper
        |
        v
OrderSubmittedV1
        |
        v
JSON Serializer
```

Benefits:

- Domain remains framework-independent
- Public contract remains stable
- Internal model may evolve independently
- Serialization annotations remain outside the Domain Layer

---

## 71. Serialization Rules

External event serialization must define:

- Property naming
- Date format
- Decimal format
- Null handling
- Unknown-field handling
- Enum representation
- Character encoding
- Maximum payload size

Recommended defaults:

```text
Property names: camelCase
Timestamp: ISO-8601 UTC
Currency: ISO 4217 code
Decimal: JSON number or documented string strategy
Encoding: UTF-8
```

Money must not be represented using binary floating-point calculations.

---

## 72. Event Queues and Destinations

Conceptual destinations may include:

```text
enterprise.orders.events
enterprise.inventory.commands
enterprise.fulfillment.commands
enterprise.notifications.commands
enterprise.audit.events
```

Queue/destination design must consider:

- Ownership
- Access control
- Retention
- Ordering
- Consumer isolation
- Event volume
- Schema governance
- Operational responsibility

The Domain Layer must not know SQS queue names.

---

## 73. SQS Ordering Scope

When strict per-Order ordering is required, SQS FIFO MAY use:

```text
MessageGroupId = orderId
```

Benefits:

- Events for one Order are serialized within one message group
- Relative order can be preserved for that group

Trade-offs:

- Hot aggregate/message-group risk
- Per-group serialization
- Consumer parallelism limits

FIFO ordering does not remove the need for idempotency and stale-state validation.

---

## 74. Event Publishing Responsibility

The aggregate:

- Records Domain Events

The application layer:

- Collects Domain Events
- Maps selected events
- Creates Outbox Records
- Coordinates local transaction

The infrastructure layer:

- Serializes Integration Events
- Publishes messages
- Applies technical retry
- Records publication result
- Exposes operational metrics

This separation must remain explicit.

---

## 75. Domain Event Handler Responsibility

A local Domain Event handler may:

- Create an Outbox Record
- Update a projection
- Create an Audit Fact
- Initiate another local application process
- Collect metrics

A Domain Event handler must not:

- Modify the originating aggregate secretly
- Bypass application authorization
- Publish directly before commit
- Depend on handler execution order without explicit coordination
- Swallow mandatory failures silently
- Contain unrelated business orchestration

---

## 76. Eventual Consistency

Events enable collaboration without distributed transactions.

Examples:

```text
OrderSubmitted
    |
    v
Approval Process begins later
```

```text
InventoryReservationRequested
    |
    v
InventoryReserved arrives later
```

Temporary business states must be explicit:

```text
PENDING_APPROVAL
INVENTORY_PENDING
FULFILLMENT_IN_PROGRESS
```

Eventual consistency must not be hidden behind a generic `PROCESSING` state when a more precise state is required.

---

## 77. Compensating Events and Commands

A compensation is a new business action.

Example:

```text
OrderCancelled
    |
    v
InventoryReleaseRequested
```

This is not a database rollback.

Possible compensation messages:

```text
InventoryReleaseRequested
PaymentCancellationRequested
FulfillmentCancellationRequested
CorrectiveNotificationRequested
```

Compensation messages must be:

- Explicit
- Idempotent
- Auditable
- Observable
- Governed by business rules

---

## 78. Event Contract Testing

Integration Event contracts should be tested through:

- JSON schema validation
- Serialization tests
- Deserialization tests
- Required-field tests
- Optional-field compatibility tests
- Unknown-field tolerance tests
- Event version tests
- Consumer-driven contract tests
- Example-payload tests

Domain Event tests remain independent from serialization.

---

## 79. Domain Event Unit Testing

Aggregate unit tests must verify:

- Event type
- Event count
- Relevant payload values
- Aggregate Version
- Occurrence timestamp
- Correlation ID
- Causation ID
- No event on failed behavior
- No duplicate event on idempotent behavior
- Immutable event collections

Example:

```java
@Test
void testSubmitShouldRecordOrderSubmittedEvent() {
    Order order = OrderTestFixture.pricedDraftOrder();

    order.submit(
            CUSTOMER_SNAPSHOT,
            APPROVAL_NOT_REQUIRED,
            USER_ID,
            SUBMITTED_AT,
            CORRELATION_ID
    );

    assertThat(order.domainEvents())
            .as("Domain events recorded after Order submission")
            .hasSize(1)
            .first()
            .isInstanceOf(OrderSubmitted.class);
}
```

Example payload assertion:

```java
OrderSubmitted event = (OrderSubmitted) order.domainEvents().getFirst();

assertThat(event.orderId())
        .as("Order identifier in OrderSubmitted event")
        .isEqualTo(ORDER_ID);

assertThat(event.occurredAt())
        .as("Occurrence time in OrderSubmitted event")
        .isEqualTo(SUBMITTED_AT);

assertThat(event.correlationId())
        .as("Correlation identifier in OrderSubmitted event")
        .isEqualTo(CORRELATION_ID);
```

---

## 80. Failed Operation Testing

A failed domain operation must not record a success event.

Example:

```java
@Test
void testSubmitShouldNotRecordEventWhenOrderHasNoItems() {
    Order order = OrderTestFixture.draftOrderWithoutItems();

    assertThatThrownBy(() -> order.submit(
            CUSTOMER_SNAPSHOT,
            APPROVAL_NOT_REQUIRED,
            USER_ID,
            SUBMITTED_AT,
            CORRELATION_ID
    ))
            .as("Submission attempt for an Order without items")
            .isInstanceOf(OrderWithoutItemsException.class);

    assertThat(order.domainEvents())
            .as("Domain events after rejected submission")
            .isEmpty();
}
```

---

## 81. Idempotency Testing

Example:

```java
@Test
void testConfirmInventoryReservationShouldIgnoreDuplicateOutcome() {
    Order order = OrderTestFixture.inventoryPendingOrder();

    order.confirmInventoryReservation(
            RESERVATION_REQUEST_ID,
            RESERVATION_ID,
            RESERVED_AT,
            CORRELATION_ID
    );

    int eventsAfterFirstOutcome = order.domainEvents().size();

    order.confirmInventoryReservation(
            RESERVATION_REQUEST_ID,
            RESERVATION_ID,
            RESERVED_AT,
            CORRELATION_ID
    );

    assertThat(order.domainEvents())
            .as("Domain events after duplicate Inventory outcome")
            .hasSize(eventsAfterFirstOutcome);
}
```

The final duplicate behavior may be:

- No operation
- Return existing result
- Raise a specific duplicate outcome exception

The chosen behavior must be documented.

---

## 82. Outbox Testing

Outbox tests should verify:

- Business state and Outbox Record commit atomically
- Failed transaction creates no Outbox Record
- Event ID is preserved
- Event Version is preserved
- Payload matches the contract
- Retry preserves identifiers
- Published records are not selected again
- Concurrent publishers do not process the same record simultaneously
- Failed records respect retry schedule
- Permanent failure reaches terminal status
- Sensitive values are not logged

---

## 83. Consumer Testing

Consumer tests should verify:

- Valid event processing
- Duplicate event handling
- Unsupported version handling
- Unknown optional field tolerance
- Missing required field rejection
- Transient failure retry
- Permanent failure routing
- Stale event handling
- Atomic idempotency record
- Business effect consistency
- Correlation metadata propagation

---

## 84. Architecture Tests

Planned architecture rules:

```text
Domain Events must not depend on Spring.
```

```text
Domain Events must not depend on Amazon SQS or the AWS SDK.
```

```text
Domain Events must not depend on Jackson.
```

```text
Integration Event DTOs must not be located in the Domain package.
```

```text
SQS publishers/AWS SDK clients must not be called from aggregates.
```

```text
Outbox persistence must remain in Infrastructure.
```

```text
External event contracts must not expose JPA entities.
```

---

## 85. Possible Package Structure

Conceptual structure:

```text
order
├── domain
│   ├── event
│   │   ├── OrderDomainEvent
│   │   ├── OrderCreated
│   │   ├── OrderSubmitted
│   │   ├── OrderApproved
│   │   ├── OrderRejected
│   │   ├── OrderCancelled
│   │   └── OrderCompleted
│   └── model
├── application
│   ├── event
│   │   ├── DomainEventCollector
│   │   ├── DomainEventProcessor
│   │   └── IntegrationEventMapper
│   └── port
│       └── OutboxPort
├── infrastructure
│   ├── outbox
│   │   ├── OutboxEntity
│   │   ├── OutboxRepository
│   │   ├── OutboxPublisher
│   │   └── OutboxSerializer
│   └── messaging
│       ├── contract
│       ├── producer
│       └── consumer
└── api
```

Generic event primitives may reside in a minimal shared technical kernel.

---

## 86. Initial Integration Event Catalog

| Domain Event | Integration Contract | Initial Publication |
|---|---|---|
| OrderCreated | order.created.v1 | Optional |
| OrderNumberAssigned | order.number-assigned.v1 | Optional |
| OrderItemAdded | None | Internal |
| OrderItemQuantityChanged | None | Internal |
| OrderItemRemoved | None | Internal |
| OrderPricingInvalidated | None | Internal |
| OrderPriced | order.priced.v1 | Optional |
| OrderSubmitted | order.submitted.v1 | Required |
| OrderApprovalRequired | approval.process-requested.v1 | Required when applicable |
| OrderApproved | order.approved.v1 | Required |
| OrderRejected | order.rejected.v1 | Required |
| OrderReviewRequested | order.review-requested.v1 | Required |
| OrderProcessingStarted | order.processing-started.v1 | Optional |
| InventoryReservationRequested | inventory.reservation-requested.v1 | Required |
| OrderInventoryReservationConfirmed | order.inventory-reserved.v1 | Required |
| OrderInventoryReservationFailed | order.inventory-reservation-failed.v1 | Required |
| OrderReadyForFulfillment | fulfillment.requested.v1 | Required when Fulfillment is enabled |
| OrderFulfillmentStarted | order.fulfillment-started.v1 | Optional |
| OrderCancelled | order.cancelled.v1 | Required |
| InventoryReleaseRequested | inventory.release-requested.v1 | Required when applicable |
| OrderCompleted | order.completed.v1 | Required |

---

## 87. Initial Event Flow: Submission Without Approval

```text
SubmitOrder
    |
    v
Order.submit(...)
    |
    +--> OrderSubmitted
    |
    +--> OrderProcessingStarted
    |
    v
Application Event Mapper
    |
    +--> order.submitted.v1
    |
    +--> inventory.reservation-requested.v1
    |
    v
Outbox Records
    |
    v
Commit
    |
    v
Publisher
```

The final design may create the Inventory request in a separate application workflow rather than directly from the submission event.

---

## 88. Initial Event Flow: Submission With Approval

```text
SubmitOrder
    |
    v
Order.submit(...)
    |
    +--> OrderSubmitted
    |
    +--> OrderApprovalRequired
    |
    v
Outbox Records
    |
    +--> order.submitted.v1
    |
    +--> approval.process-requested.v1
    |
    v
Approval Context
    |
    +--> approval.completed.v1
    |
    v
Order Approval Consumer
    |
    +--> order.approve(...)
    |
    +--> OrderApproved
    |
    +--> OrderProcessingStarted
```

---

## 89. Initial Event Flow: Inventory Reservation

```text
OrderProcessingStarted
    |
    v
Request Inventory Reservation
    |
    v
InventoryReservationRequested
    |
    v
inventory.reservation-requested.v1
    |
    v
Inventory Context
    |
    +--> inventory.reserved.v1
    |
    or
    |
    +--> inventory.reservation-failed.v1
    |
    v
Order Inventory Consumer
    |
    +--> confirmInventoryReservation(...)
    |
    or
    |
    +--> failInventoryReservation(...)
```

---

## 90. Initial Event Flow: Cancellation

```text
CancelOrder
    |
    v
order.cancel(...)
    |
    +--> OrderCancelled
    |
    +--> InventoryReleaseRequested
    |
    +--> PaymentCancellationRequested
    |
    +--> FulfillmentCancellationRequested
    |
    v
Outbox Records
    |
    v
Asynchronous compensation
```

Only compensation messages applicable to the current Order state should be created.

---

## 91. Initial Event Flow: Completion

```text
Fulfillment completed
    |
    v
Fulfillment outcome consumed
    |
    v
order.complete(...)
    |
    +--> OrderCompleted
    |
    v
Outbox
    |
    +--> order.completed.v1
    |
    +--> Notification request
    |
    +--> Audit fact
```

---

## 92. Event Design Review Checklist

Before approving a new Domain Event, verify:

- Does it represent a completed business fact?
- Is the name in past tense?
- Is the event owned by the correct bounded context?
- Is the event required?
- Is the payload minimal?
- Are identifiers stable?
- Is the timestamp correct?
- Is Correlation ID included where required?
- Is Causation ID included where required?
- Is the event immutable?
- Does it avoid infrastructure details?
- Does it avoid sensitive data?
- Does it require external publication?
- Does the Integration Event require versioning?
- Is the consumer idempotent?
- Are ordering requirements documented?
- Is retry behavior documented?
- Is dead-letter handling documented?
- Are tests defined?
- Is observability defined?

---

## 93. Event Introduction Process

A new event should be introduced through:

1. Domain requirement identification
2. Ubiquitous Language review
3. Context ownership confirmation
4. Domain behavior definition
5. Payload definition
6. Consumer identification
7. Security classification
8. Integration mapping decision
9. Schema definition
10. Compatibility review
11. Test implementation
12. Documentation update
13. Architecture review
14. Deployment and monitoring plan

Events must not be introduced only because a framework listener is convenient.

---

## 94. Event Deprecation

An Integration Event version may be deprecated when:

- All consumers migrated
- Replacement contract is stable
- Retention window passed
- Replay requirements were evaluated
- Operational dependencies were removed

Deprecation process should include:

- Announcement
- Migration documentation
- Parallel publication period when required
- Consumer monitoring
- Removal deadline
- Final contract removal

Internal Domain Events may evolve more freely but still require code and behavior review.

---

## 95. Open Decisions

The following decisions remain open:

1. Will Event ID use UUID v4, UUID v7, or another identifier?
2. Is Aggregate Version domain-managed or persistence-managed?
3. Is Causation ID mandatory for all asynchronous events?
4. Will event versions be embedded in Event Type or stored separately?
5. Will JSON Schema, Avro, or Protobuf be used?
6. Will AsyncAPI document asynchronous contracts?
7. Which consumer-owned SQS queues/fan-out paths are required for each integration event?
8. Which events require FIFO and what is the MessageGroupId strategy?
9. What is the maximum event payload size?
10. Which Order events are public?
11. Is `OrderCreated` externally relevant?
12. Is `OrderPriced` externally relevant?
13. Should `OrderProcessingStarted` remain internal?
14. Will Audit consume Domain Events or dedicated Audit Facts?
15. Will Notification consume Order events directly or dedicated requests?
16. Are Approval and Inventory interactions commands, events, or both?
17. Which records require Causation ID?
18. How long are Outbox Records retained after publication?
19. What is the maximum Outbox retry count?
20. What is the dead-letter recovery process?
21. How long are consumer idempotency records retained?
22. How are event schemas validated in CI?
23. Will consumer-driven contract testing be required?
24. How are stale events monitored?
25. Can event replay trigger external side effects?
26. Which events contain personal data?
27. Which event fields require encryption?
28. Which events require strict per-aggregate ordering?
29. Will Outbox publication use polling or change-data capture?
30. How will schema compatibility be enforced?

---

## 96. Decision Summary

The initial Domain Event model establishes that:

- Domain Events represent completed business facts.
- Commands and Domain Events remain distinct.
- Domain Events and Integration Events remain distinct.
- Events use past-tense business names.
- Events are immutable.
- Events are created by domain behavior.
- Failed operations do not create success events.
- Aggregates maintain uncommitted Domain Events.
- The application layer maps selected Domain Events to Integration Events.
- Infrastructure serializes and publishes Integration Events.
- The Transactional Outbox Pattern protects reliable publication.
- Event publication occurs only after the business transaction is safely committed.
- External event contracts are versioned.
- Consumers assume at-least-once delivery.
- Consumers must be idempotent.
- Global ordering is not assumed.
- Aggregate Version may support per-aggregate ordering.
- Stale events must be detected and handled explicitly.
- Event payloads must remain minimal and secure.
- Internal entities and framework objects must not be exposed.
- Compensations are explicit business operations.
- Event contracts require tests, observability, and governance.

---

## 97. Next Documentation Step

The next document will define the Value Objects used by the Order Management bounded context:

```text
docs/domain/value-objects.md
```

It will establish:

- Identifier Value Objects
- Money
- Currency
- Quantity
- Percentage
- Customer Reference
- Product Reference
- Customer Snapshot
- Product Snapshot
- Pricing Snapshot
- Submission Details
- Cancellation Details
- Correlation Identifier
- Equality rules
- Immutability rules
- Validation rules
- Persistence mapping considerations
- Serialization boundaries
