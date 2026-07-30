# Order Aggregate

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Order Aggregate |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

## 1. Purpose

This document defines the initial tactical Domain-Driven Design model for the Order aggregate in AstraForge Supply Platform.

Its purpose is to establish:

- The aggregate boundary
- The aggregate root
- Internal entities
- Value objects
- Business invariants
- Domain behaviors
- Lifecycle transitions
- Domain events
- Concurrency rules
- Persistence considerations
- Integration boundaries
- Initial implementation scope

The Order aggregate is the primary consistency boundary of the Order Management bounded context.

This document describes the business model independently from transport protocols, persistence frameworks, messaging technologies, and user-interface concerns.

---

## 2. Aggregate Overview

The Order aggregate represents a corporate customer's intention to acquire one or more products under defined commercial conditions.

The aggregate protects all business rules that must remain consistent within a single local transaction.

The initial aggregate structure is:

```text
Order
├── OrderId
├── OrderNumber
├── CustomerReference
├── CustomerSnapshot
├── OrderStatus
├── OrderItems
│   └── OrderItem
│       ├── OrderItemId
│       ├── ProductReference
│       ├── ProductSnapshot
│       ├── Quantity
│       └── ItemPricing
├── PricingSnapshot
├── SubmissionDetails
├── CancellationDetails
├── OrderStatusHistory
├── AggregateVersion
├── CreatedAt
├── UpdatedAt
└── DomainEvents
```

The aggregate root is `Order`.

All changes to aggregate state must occur through explicit Order behavior.

---

## 3. Aggregate Root

## 3.1 Order

`Order` is the aggregate root.

It is responsible for:

- Protecting aggregate invariants
- Managing Order Items
- Controlling lifecycle transitions
- Applying accepted Pricing results
- Preserving snapshots
- Recording submission information
- Recording cancellation information
- Recording status history
- Producing Domain Events
- Preventing invalid state changes
- Coordinating internal aggregate consistency

External components must not modify internal fields directly.

The aggregate should expose behavior rather than public setters.

Preferred operations include:

```text
Order.create(...)
order.addItem(...)
order.updateItemQuantity(...)
order.removeItem(...)
order.applyPricing(...)
order.submit(...)
order.markApprovalPending(...)
order.approve(...)
order.reject(...)
order.requestReview(...)
order.markInventoryPending(...)
order.confirmInventoryReservation(...)
order.failInventoryReservation(...)
order.markReadyForFulfillment(...)
order.startFulfillment(...)
order.complete(...)
order.cancel(...)
```

---

## 4. Aggregate Boundary

The Order aggregate initially contains:

- Order
- Order Item
- Customer Reference
- Customer Snapshot
- Product Reference
- Product Snapshot
- Item Pricing
- Pricing Snapshot
- Submission Details
- Cancellation Details
- Order Status History entries
- Aggregate Version
- Pending Domain Events

The aggregate does not contain:

- Customer master data
- Product master data
- Pricing policy definitions
- Approval Process
- Inventory Reservation
- Payment
- Shipment
- Notification delivery
- Audit Record storage
- Identity provider information
- External integration DTOs
- Persistence entities from other bounded contexts

The boundary is designed to keep the aggregate behaviorally complete without making it excessively large.

---

## 5. Aggregate Ownership

Order Management owns:

- The Order aggregate
- Order lifecycle rules
- Order Status
- Order Items
- Historical Order snapshots
- Submission state
- Cancellation state
- Order Status History
- Order-related Domain Events

Other bounded contexts own:

| Concept | Owning Context |
|---|---|
| Current Customer data | Customer |
| Current Product data | Product Catalog |
| Pricing policies | Pricing |
| Approval Process | Approval |
| Inventory Reservation | Inventory |
| Payment lifecycle | Payment |
| Shipment lifecycle | Fulfillment |
| User authentication | Identity and Access |
| Notification delivery | Notification |
| Audit Record storage | Audit |

The Order aggregate stores references or snapshots when historical consistency requires them.

---

## 6. Order Identity

## 6.1 OrderId

`OrderId` is the internal technical identifier of an Order.

Characteristics:

- Globally unique
- Immutable
- Generated once
- Used by persistence and internal contracts
- Not intended to replace a business-facing Order Number

Possible representation:

```text
OrderId(UUID value)
```

Rules:

- Must not be null
- Must not change after creation
- Equality is based on the identifier value

---

## 6.2 OrderNumber

`OrderNumber` is the business-facing identifier of an Order.

Characteristics:

- Human-readable
- Immutable after assignment
- Unique within its defined business scope
- Suitable for customer service, operations, and external references

Possible representation:

```text
OrderNumber(String value)
```

Open decision:

- Whether the number is generated when the Order is created
- Whether the number is generated only when the Order is submitted

Until this decision is finalized, the aggregate model should allow an Order Number to be assigned through an explicit domain operation.

---

## 7. Internal Entity

## 7.1 OrderItem

`OrderItem` is an entity inside the Order aggregate.

It has its own identity but does not exist independently from its parent Order.

Responsibilities:

- Preserve Product Reference
- Preserve Product Snapshot
- Maintain Quantity
- Maintain accepted item-level Pricing
- Protect item-specific invariants
- Recalculate item totals when allowed

Possible structure:

```text
OrderItem
├── OrderItemId
├── ProductReference
├── ProductSnapshot
├── Quantity
└── ItemPricing
```

Rules:

- An Order Item belongs to exactly one Order.
- It must not be loaded or modified independently from the Order aggregate.
- It must not expose public setters.
- It must not publish integration events directly.
- Relevant changes should be represented through Order aggregate behavior.

---

## 8. Value Objects

## 8.1 CustomerReference

Represents the Customer identifier used by Order Management.

Possible structure:

```text
CustomerReference(CustomerId customerId)
```

Rules:

- Must not be null
- Must identify one Customer
- Must remain stable after Order submission

---

## 8.2 CustomerSnapshot

Represents immutable Customer information accepted for the Order.

Possible structure:

```text
CustomerSnapshot
├── customerId
├── legalName
├── tradeName
├── documentNumber
├── customerType
├── customerClassification
├── customerSegment
├── contactInformation
└── deliveryInformation
```

Rules:

- Must be immutable
- Must contain only information required by Order Management
- Must not reference an external Customer entity
- Must remain historically stable after submission
- Must not be automatically refreshed after submission

---

## 8.3 ProductReference

Represents a Product identifier used by Order Management.

Possible structure:

```text
ProductReference(ProductId productId)
```

Rules:

- Must not be null
- Must identify one Product
- Must remain stable within the Order Item
- Must not expose Product Catalog persistence details

---

## 8.4 ProductSnapshot

Represents immutable Product information preserved in an Order Item.

Possible structure:

```text
ProductSnapshot
├── productId
├── sku
├── name
├── description
├── category
└── unitOfMeasure
```

Rules:

- Must be immutable
- Must contain only Order-relevant information
- Must not change when Product Catalog data changes
- Must be complete before Order submission

---

## 8.5 Quantity

Represents the requested number of units.

Possible structure:

```text
Quantity(BigDecimal value)
```

Rules:

- Must be greater than zero
- Must use the precision required by the Unit of Measure
- Must not be null
- Must not use floating-point types
- May require whole-number validation for unit-based Products
- Must be immutable

Possible operations:

```text
quantity.add(other)
quantity.subtract(other)
quantity.isGreaterThanZero()
```

---

## 8.6 Money

Represents a monetary amount and Currency.

Possible structure:

```text
Money
├── amount
└── currency
```

Rules:

- Amount must use decimal arithmetic
- Currency must not be null
- Operations require compatible currencies
- Precision and scale must be explicit
- Rounding mode must be explicit
- Floating-point types are prohibited
- Money must be immutable

Possible operations:

```text
money.add(other)
money.subtract(other)
money.multiply(quantity)
money.isNegative()
money.isZero()
```

---

## 8.7 Percentage

Represents a proportional rate.

Possible structure:

```text
Percentage(BigDecimal value)
```

Rules:

- Must define whether the internal value is stored as `10` or `0.10`
- Must define allowed minimum and maximum
- Must define scale and rounding
- Must be immutable

---

## 8.8 ItemPricing

Represents the accepted commercial values for one Order Item.

Possible structure:

```text
ItemPricing
├── unitPrice
├── effectivePrice
├── discountAmount
├── discountPercentage
├── taxAmount
├── feeAmount
├── subtotal
└── total
```

Rules:

- All Money values must use the same Currency
- Values must not be negative unless explicitly permitted
- Total must be mathematically consistent
- Item Pricing becomes immutable after submission
- Pricing must be replaced atomically, not field by field

---

## 8.9 PricingSnapshot

Represents the complete accepted Pricing result for the Order.

Possible structure:

```text
PricingSnapshot
├── currency
├── subtotal
├── discountTotal
├── taxTotal
├── feeTotal
├── freightTotal
├── grandTotal
├── policyVersion
└── calculatedAt
```

Rules:

- Must be immutable
- Must be consistent with item-level Pricing
- Must use one Currency
- Must become immutable after Order submission
- Must preserve the Pricing policy reference when required
- Must not be automatically recalculated after submission

---

## 8.10 SubmissionDetails

Represents information recorded when an Order is submitted.

Possible structure:

```text
SubmissionDetails
├── submittedBy
├── submittedAt
└── correlationId
```

Rules:

- Must be absent before submission
- Must be present after successful submission
- Must be immutable
- Submitted At must use UTC
- Submitted By must identify a User or System Actor

---

## 8.11 CancellationDetails

Represents information recorded when an Order is cancelled.

Possible structure:

```text
CancellationDetails
├── reason
├── cancelledBy
├── cancelledAt
└── correlationId
```

Rules:

- Must be absent before cancellation
- Must be present after cancellation
- Cancellation Reason must not be blank
- Must be immutable
- Cancelled At must use UTC

---

## 8.12 OrderStatusHistoryEntry

Represents one lifecycle transition.

Possible structure:

```text
OrderStatusHistoryEntry
├── previousStatus
├── newStatus
├── changedBy
├── changedAt
├── reason
└── correlationId
```

Rules:

- Must be immutable
- History is append-only
- The new status must match the aggregate state after the transition
- Transition time must use UTC
- The responsible actor must be recorded when available

---

## 8.13 CorrelationId

Represents a business-flow identifier used across operations and integrations.

Possible structure:

```text
CorrelationId(String value)
```

Rules:

- Must not be blank
- Must be immutable
- Must be propagated to Domain Events and Integration Events
- Must not contain credentials or sensitive information

---

## 9. Order Status

The initial Order Status enumeration is:

```text
DRAFT
SUBMITTED
PENDING_APPROVAL
APPROVED
REJECTED
REVIEW_REQUESTED
PROCESSING
INVENTORY_PENDING
INVENTORY_RESERVED
INVENTORY_FAILED
READY_FOR_FULFILLMENT
FULFILLMENT_IN_PROGRESS
COMPLETED
CANCELLED
```

This list remains subject to refinement.

The aggregate must not expose a generic status setter.

Status changes must occur only through explicit business operations.

---

## 10. Initial State Machine

The initial lifecycle is:

```text
DRAFT
  |
  v
SUBMITTED
  |
  +----------------------------+
  |                            |
  v                            v
PENDING_APPROVAL           PROCESSING
  |                            |
  +-----------+                |
  |           |                |
  v           v                |
APPROVED   REJECTED             |
  |                             |
  v                             |
PROCESSING <--------------------+
  |
  v
INVENTORY_PENDING
  |
  +----------------------------+
  |                            |
  v                            v
INVENTORY_RESERVED       INVENTORY_FAILED
  |
  v
READY_FOR_FULFILLMENT
  |
  v
FULFILLMENT_IN_PROGRESS
  |
  v
COMPLETED
```

Review flow:

```text
PENDING_APPROVAL
      |
      v
REVIEW_REQUESTED
      |
      v
DRAFT or dedicated review state
```

Cancellation is allowed only from explicitly configured states.

---

## 11. Allowed Transition Matrix

The initial transition matrix is:

| Current Status | Operation | Next Status |
|---|---|---|
| DRAFT | Submit without Approval | PROCESSING |
| DRAFT | Submit requiring Approval | PENDING_APPROVAL |
| PENDING_APPROVAL | Approve | APPROVED |
| APPROVED | Continue Processing | PROCESSING |
| PENDING_APPROVAL | Reject | REJECTED |
| PENDING_APPROVAL | Request Review | REVIEW_REQUESTED |
| REVIEW_REQUESTED | Reopen for Editing | DRAFT |
| PROCESSING | Request Inventory | INVENTORY_PENDING |
| INVENTORY_PENDING | Confirm Reservation | INVENTORY_RESERVED |
| INVENTORY_PENDING | Fail Reservation | INVENTORY_FAILED |
| INVENTORY_RESERVED | Prepare Fulfillment | READY_FOR_FULFILLMENT |
| READY_FOR_FULFILLMENT | Start Fulfillment | FULFILLMENT_IN_PROGRESS |
| FULFILLMENT_IN_PROGRESS | Complete | COMPLETED |
| Eligible Status | Cancel | CANCELLED |

The final transition matrix will be refined in a dedicated Order State Machine document.

---

## 12. Creation Behavior

## 12.1 Create Order

Factory operation:

```text
Order.create(
    orderId,
    customerReference,
    createdBy,
    createdAt,
    correlationId
)
```

Creation rules:

- Order ID must be valid.
- Customer Reference must be present.
- Created By must be present.
- Created At must be present.
- Initial status is `DRAFT`.
- The Order begins with no items.
- Pricing Snapshot is absent.
- Submission Details are absent.
- Cancellation Details are absent.
- Aggregate version starts at its initial value.
- An `OrderCreated` Domain Event is recorded.

Possible result:

```text
OrderCreated
```

---

## 13. Item Management Behavior

## 13.1 Add Item

Operation:

```text
order.addItem(
    orderItemId,
    productReference,
    productSnapshot,
    quantity,
    actor,
    occurredAt,
    correlationId
)
```

Rules:

- Order must be editable.
- Product Reference must be valid.
- Product Snapshot must be valid.
- Quantity must be greater than zero.
- Order Item ID must be unique within the Order.
- Duplicate Product handling must follow an explicit policy.
- Existing Pricing Snapshot becomes invalid after an item change.
- Updated At must change.
- An `OrderItemAdded` Domain Event is recorded.

Open decision:

- Whether the same Product may appear more than once
- Whether adding the same Product merges quantities
- Whether different commercial conditions allow duplicate Product lines

---

## 13.2 Update Item Quantity

Operation:

```text
order.updateItemQuantity(
    orderItemId,
    newQuantity,
    actor,
    occurredAt,
    correlationId
)
```

Rules:

- Order must be editable.
- Order Item must exist.
- New Quantity must be greater than zero.
- Existing Pricing Snapshot becomes invalid.
- Updated At must change.
- An `OrderItemQuantityChanged` Domain Event is recorded.

Changing Quantity to zero is not equivalent to removing the item.

Removal must use an explicit operation.

---

## 13.3 Remove Item

Operation:

```text
order.removeItem(
    orderItemId,
    actor,
    occurredAt,
    correlationId
)
```

Rules:

- Order must be editable.
- Order Item must exist.
- Existing Pricing Snapshot becomes invalid.
- Updated At must change.
- An `OrderItemRemoved` Domain Event is recorded.

A Draft Order may temporarily contain no items.

Submission must reject an empty Order.

---

## 13.4 Replace Product Snapshot

Replacing a Product Snapshot may be allowed only while the Order is in `DRAFT`.

Rules:

- The Product Reference must remain consistent.
- The replacement must not occur after submission.
- Pricing may become invalid.
- The operation must be explicit.
- The change may produce a Domain Event if operationally relevant.

---

## 14. Pricing Behavior

## 14.1 Apply Pricing

Operation:

```text
order.applyPricing(
    itemPricingResults,
    pricingSnapshot,
    actor,
    occurredAt,
    correlationId
)
```

Rules:

- Order must be editable or in an explicitly repricable state.
- Every current Order Item must have one Pricing result.
- No Pricing result may exist for an unknown Order Item.
- Currency must be consistent.
- Item totals must be mathematically valid.
- Aggregate totals must match item totals and Order-level charges.
- Pricing policy reference must be preserved when required.
- Existing Pricing is replaced atomically.
- Updated At must change.
- An `OrderPriced` Domain Event is recorded.

---

## 14.2 Invalidate Pricing

Pricing becomes invalid when any pricing-relevant information changes.

Examples:

- Order Item added
- Order Item removed
- Quantity changed
- Customer changed
- Delivery information changed
- Requested discount changed
- Payment conditions changed

The aggregate may represent this through:

- Absence of Pricing Snapshot
- An explicit Pricing State
- A Pricing version comparison

The final design should avoid a vague boolean when a richer model is required.

---

## 14.3 Pricing Consistency

Before submission:

- All items must have accepted Pricing.
- Pricing Snapshot must be present.
- Pricing Snapshot Currency must match all item values.
- Grand Total must be consistent.
- Pricing must not be stale relative to aggregate content.
- Pricing calculation time must be preserved.

---

## 15. Submission Behavior

## 15.1 Submit Order

Operation:

```text
order.submit(
    customerSnapshot,
    productSnapshots,
    acceptedPricing,
    approvalRequirement,
    submittedBy,
    submittedAt,
    correlationId
)
```

The exact signature may be refined to avoid passing duplicate state already held by the aggregate.

Submission rules:

- Order must be in `DRAFT`.
- Order must contain at least one Order Item.
- Customer Reference must be valid.
- Customer Snapshot must be complete.
- Every item must have a Product Snapshot.
- Accepted Pricing must be present.
- Pricing must be current.
- Order totals must be consistent.
- Submitted By must be present.
- Submitted At must be present.
- Cancellation Details must be absent.
- Submission must occur only once.

Submission effects:

- Customer Snapshot becomes immutable.
- Product Snapshots become immutable.
- Pricing Snapshot becomes immutable.
- Submission Details are recorded.
- Unrestricted editing is disabled.
- Status changes according to Approval requirements.
- Status History is appended.
- `OrderSubmitted` is recorded.
- `OrderApprovalRequired` may be recorded.
- `OrderProcessingStarted` may be recorded when no Approval is required.

Possible transitions:

```text
DRAFT -> PENDING_APPROVAL
```

or:

```text
DRAFT -> PROCESSING
```

The separate `SUBMITTED` status may be retained only if it represents a meaningful persistent business state.

---

## 16. Approval-Related Behavior

## 16.1 Mark Approval Pending

Operation:

```text
order.markApprovalPending(...)
```

Rules:

- Order must have been submitted.
- Approval must be required.
- Order must not already be waiting for Approval.
- The transition must be recorded.
- `OrderApprovalRequired` is recorded.

---

## 16.2 Approve Order

Operation:

```text
order.approve(
    approvalProcessId,
    approvedBy,
    approvedAt,
    comments,
    correlationId
)
```

Rules:

- Current status must be `PENDING_APPROVAL`.
- Approval Process ID must match the expected process.
- Approved By must be present.
- Approved At must be present.
- Duplicate Approval outcomes must not reapply the transition.
- Approval authorization is validated by Approval Context.
- Order records the business outcome, not the complete Approval Process.

Effects:

- Status becomes `APPROVED`.
- Status History is appended.
- `OrderApproved` is recorded.

The application layer may immediately invoke a subsequent transition to `PROCESSING`.

---

## 16.3 Reject Order

Operation:

```text
order.reject(
    approvalProcessId,
    rejectedBy,
    rejectedAt,
    rejectionReason,
    correlationId
)
```

Rules:

- Current status must be `PENDING_APPROVAL`.
- Rejection Reason is mandatory.
- Approval Process ID must match.
- Duplicate outcomes must be idempotent.

Effects:

- Status becomes `REJECTED`.
- Status History is appended.
- `OrderRejected` is recorded.

---

## 16.4 Request Review

Operation:

```text
order.requestReview(
    approvalProcessId,
    requestedBy,
    requestedAt,
    reviewReason,
    correlationId
)
```

Rules:

- Current status must be `PENDING_APPROVAL`.
- Review Reason must be present.
- Approval Process ID must match.

Effects:

- Status becomes `REVIEW_REQUESTED`.
- Status History is appended.
- `OrderReviewRequested` is recorded.

A separate operation may reopen the Order for editing.

---

## 17. Inventory-Related Behavior

## 17.1 Request Inventory Reservation

Operation:

```text
order.requestInventoryReservation(
    reservationRequestId,
    requestedAt,
    correlationId
)
```

Rules:

- Current status must allow Inventory Reservation.
- Required Approval must already be completed.
- Order must contain valid items and quantities.
- Reservation Request ID must be present.
- Duplicate requests must not create duplicate effects.

Effects:

- Status becomes `INVENTORY_PENDING`.
- Status History is appended.
- `InventoryReservationRequested` is recorded.

The aggregate stores only the Reservation Request reference required to correlate future outcomes.

---

## 17.2 Confirm Inventory Reservation

Operation:

```text
order.confirmInventoryReservation(
    reservationRequestId,
    reservationId,
    reservedAt,
    correlationId
)
```

Rules:

- Current status must be `INVENTORY_PENDING`.
- Reservation Request ID must match the pending request.
- Reserved quantities must satisfy the configured policy.
- Duplicate confirmation must be idempotent.
- Stale outcomes must be rejected or ignored safely.

Effects:

- Status becomes `INVENTORY_RESERVED`.
- Reservation reference is preserved.
- Status History is appended.
- `InventoryReserved` is recorded.

---

## 17.3 Fail Inventory Reservation

Operation:

```text
order.failInventoryReservation(
    reservationRequestId,
    failedAt,
    failureReason,
    correlationId
)
```

Rules:

- Current status must be `INVENTORY_PENDING`.
- Reservation Request ID must match.
- Failure Reason must be present.
- Duplicate failure must not duplicate effects.

Effects:

- Status becomes `INVENTORY_FAILED`.
- Status History is appended.
- `InventoryReservationFailed` is recorded.

---

## 17.4 Handle Partial Reservation

Partial Reservation remains an open domain decision.

Possible policies:

- Reject the entire Reservation
- Accept partial quantity
- Keep the Order pending
- Split the Order
- Request user review
- Backorder missing quantities

The aggregate must not implement an implicit partial-reservation behavior before the policy is defined.

---

## 18. Fulfillment-Related Behavior

## 18.1 Mark Ready for Fulfillment

Operation:

```text
order.markReadyForFulfillment(...)
```

Rules:

- Current status must be `INVENTORY_RESERVED`.
- All required preconditions must be complete.
- Required Payment conditions must be satisfied when applicable.

Effects:

- Status becomes `READY_FOR_FULFILLMENT`.
- Status History is appended.
- `OrderReadyForFulfillment` is recorded.

---

## 18.2 Start Fulfillment

Operation:

```text
order.startFulfillment(
    fulfillmentRequestId,
    startedAt,
    correlationId
)
```

Rules:

- Current status must be `READY_FOR_FULFILLMENT`.
- Fulfillment Request ID must be present.
- Duplicate invocation must be idempotent.

Effects:

- Status becomes `FULFILLMENT_IN_PROGRESS`.
- Status History is appended.
- `OrderFulfillmentStarted` is recorded.

---

## 18.3 Complete Order

Operation:

```text
order.complete(
    completedAt,
    correlationId
)
```

Rules:

- Current status must permit completion.
- Required Fulfillment completion must have occurred.
- Required Payment conditions must be satisfied.
- Completed At must be present.
- Completion must occur only once.

Effects:

- Status becomes `COMPLETED`.
- Status History is appended.
- `OrderCompleted` is recorded.

Completed Orders are immutable except for explicitly allowed administrative metadata.

---

## 19. Cancellation Behavior

## 19.1 Cancel Order

Operation:

```text
order.cancel(
    cancellationReason,
    cancelledBy,
    cancelledAt,
    correlationId
)
```

Rules:

- Current status must be cancellable.
- Cancellation Reason must not be blank.
- Cancelled By must be present.
- Cancelled At must be present.
- A completed Order cannot be cancelled.
- A previously cancelled Order cannot be cancelled again.
- Cancellation must not silently delete the Order.

Effects:

- Status becomes `CANCELLED`.
- Cancellation Details are recorded.
- Status History is appended.
- `OrderCancelled` is recorded.
- `InventoryReleaseRequested` may be recorded when Inventory was reserved.
- Payment or Fulfillment compensation may be requested when applicable.

---

## 19.2 Initial Cancellable Statuses

Initial candidates:

```text
DRAFT
PENDING_APPROVAL
REJECTED
REVIEW_REQUESTED
PROCESSING
INVENTORY_PENDING
INVENTORY_FAILED
INVENTORY_RESERVED
READY_FOR_FULFILLMENT
```

Potentially restricted:

```text
FULFILLMENT_IN_PROGRESS
```

Not cancellable:

```text
COMPLETED
CANCELLED
```

The final policy will be documented in the Order State Machine and Business Rules Catalog.

---

## 20. Editability Rules

An Order is editable only in explicitly allowed statuses.

Initial editable status:

```text
DRAFT
```

Possible future editable status:

```text
REVIEW_REQUESTED
```

Editing includes:

- Adding items
- Removing items
- Changing quantities
- Replacing Product Snapshots
- Changing delivery data
- Repricing

An Order is not editable after submission unless a dedicated review operation explicitly reopens it.

The aggregate should expose:

```text
order.isEditable()
```

This method represents domain behavior and must not replace explicit validation inside state-changing operations.

---

## 21. Core Invariants

The Order aggregate must always preserve the following invariants.

### 21.1 Identity Invariants

- Order ID must exist.
- Order ID must never change.
- Order Number must be unique in its defined scope.
- Order Item IDs must be unique within the Order.

### 21.2 Customer Invariants

- An Order must reference one Customer.
- Customer Reference must not be null.
- Submitted Orders must contain a Customer Snapshot.
- Customer Snapshot must not change after submission.

### 21.3 Item Invariants

- A submitted Order must contain at least one Order Item.
- Every Order Item must have a valid Product Reference.
- Every Order Item must have a Quantity greater than zero.
- Every submitted Order Item must contain a Product Snapshot.
- Order Items must not be changed outside the aggregate.

### 21.4 Pricing Invariants

- Submitted Orders must contain accepted Pricing.
- Every item must have item-level Pricing.
- All monetary values must use compatible Currency.
- Grand Total must be consistent.
- Pricing must not be stale at submission.
- Accepted Pricing must not change after submission.
- Monetary values must not use floating-point arithmetic.

### 21.5 Lifecycle Invariants

- Status transitions must follow explicit rules.
- Status must not be assigned directly.
- Submission must occur only once.
- Completion must occur only once.
- Cancellation must occur only once.
- Completed Orders cannot be cancelled.
- Cancelled Orders cannot continue processing.
- Rejected Orders cannot continue without an explicit new business operation.

### 21.6 History Invariants

- Every relevant status transition must append history.
- History must be append-only.
- History must match the aggregate's lifecycle.
- Transition timestamps must not move backward within normal processing.

### 21.7 Event Invariants

- Relevant business state changes must record Domain Events.
- Events must describe completed facts.
- Events must contain stable identifiers.
- Events must not expose mutable aggregate internals.
- Integration publication must not occur directly from the Domain Layer.

---

## 22. Domain Events

The initial Order Domain Events are:

```text
OrderCreated
OrderNumberAssigned
OrderItemAdded
OrderItemQuantityChanged
OrderItemRemoved
OrderPricingInvalidated
OrderPriced
OrderSubmitted
OrderApprovalRequired
OrderApproved
OrderRejected
OrderReviewRequested
OrderProcessingStarted
InventoryReservationRequested
InventoryReserved
InventoryReservationFailed
OrderReadyForFulfillment
OrderFulfillmentStarted
OrderCancelled
InventoryReleaseRequested
OrderCompleted
```

Not every Domain Event becomes an Integration Event.

---

## 23. Domain Event Structure

A Domain Event may contain:

```text
DomainEvent
├── eventId
├── aggregateId
├── aggregateVersion
├── occurredAt
├── correlationId
├── causationId
└── businessPayload
```

Rules:

- Event ID must be unique.
- Aggregate ID must identify the Order.
- Aggregate Version should represent the version after the change.
- Occurred At must represent when the business fact occurred.
- Correlation ID must be propagated when available.
- Event instances must be immutable.
- Event names use past tense.

---

## 24. Domain Event Examples

## 24.1 OrderCreated

```text
OrderCreated
├── eventId
├── orderId
├── customerId
├── createdBy
├── occurredAt
└── correlationId
```

---

## 24.2 OrderItemAdded

```text
OrderItemAdded
├── eventId
├── orderId
├── orderItemId
├── productId
├── quantity
├── occurredAt
└── correlationId
```

---

## 24.3 OrderSubmitted

```text
OrderSubmitted
├── eventId
├── orderId
├── orderNumber
├── customerId
├── grandTotal
├── currency
├── submittedBy
├── occurredAt
└── correlationId
```

Sensitive or unnecessary snapshot data should not be included automatically.

---

## 24.4 OrderCancelled

```text
OrderCancelled
├── eventId
├── orderId
├── previousStatus
├── cancellationReason
├── cancelledBy
├── occurredAt
└── correlationId
```

---

## 25. Domain Event Collection

The aggregate may maintain an internal collection of uncommitted Domain Events.

Possible behavior:

```text
order.domainEvents()
order.clearDomainEvents()
```

Rules:

- The collection must not be publicly mutable.
- The caller receives an immutable copy.
- Events are recorded during successful domain operations.
- Events are cleared only after the application layer safely handles them.
- Persistence adapters must not invent Domain Events.
- Event publication must occur after the business transaction is safely committed or through a Transactional Outbox.

---

## 26. Business Exceptions

The Domain Layer should use business-specific exceptions or result types.

Initial candidates:

```text
OrderNotEditableException
OrderAlreadySubmittedException
OrderWithoutItemsException
OrderItemNotFoundException
DuplicateOrderItemException
InvalidOrderQuantityException
OrderPricingMissingException
OrderPricingStaleException
OrderPricingInconsistentException
InvalidOrderStatusTransitionException
OrderApprovalMismatchException
InventoryReservationMismatchException
OrderNotCancellableException
OrderAlreadyCancelledException
CompletedOrderCannotBeCancelledException
OrderAlreadyCompletedException
```

Exceptions must represent domain meaning.

Avoid:

```text
IllegalStateException
RuntimeException
GenericBusinessException
ValidationException
```

Generic technical exceptions may still be used outside the Domain Layer where appropriate.

---

## 27. Validation Responsibilities

Validation must be divided correctly.

### 27.1 Transport Validation

Owned by inbound adapters:

- Required JSON fields
- Field length
- Format
- JSON types
- Supported enum representation
- Basic syntax

### 27.2 Application Validation

Owned by application services:

- External Customer existence
- Customer eligibility
- Product existence
- Product orderability
- Permission validation
- Idempotency-key lookup
- Loading the correct aggregate
- Calling external ports
- Coordinating transactions

### 27.3 Domain Validation

Owned by the Order aggregate:

- Whether the current status allows the operation
- Whether Quantity is valid
- Whether the Order contains items
- Whether Pricing is consistent
- Whether submission is allowed
- Whether cancellation is allowed
- Whether an outcome matches the pending process
- Whether invariants remain preserved

---

## 28. Application Service Responsibilities

Application services coordinate use cases but do not replace aggregate behavior.

Example submission flow:

```text
SubmitOrderUseCase
    |
    +--> Load Order
    |
    +--> Validate User permission
    |
    +--> Load Customer data
    |
    +--> Validate Customer eligibility
    |
    +--> Load Products
    |
    +--> Validate Product orderability
    |
    +--> Calculate Pricing
    |
    +--> Evaluate Approval requirement
    |
    +--> order.applyPricing(...)
    |
    +--> order.submit(...)
    |
    +--> Save Order
    |
    +--> Save Outbox Records
    |
    +--> Commit Transaction
```

The application service decides orchestration order.

The aggregate decides whether the business state transition is valid.

---

## 29. Repository Contract

The Domain or Application Layer may define an Order repository port.

Possible interface:

```text
OrderRepository
├── save(Order order)
├── findById(OrderId orderId)
├── findByOrderNumber(OrderNumber orderNumber)
└── existsByOrderNumber(OrderNumber orderNumber)
```

Search operations may use separate query models rather than loading aggregates.

Rules:

- Repository interfaces use domain types.
- JPA entities must not leak through the interface.
- The repository returns complete aggregates when behavior is required.
- Partial aggregates must not be used for state-changing operations.
- Search projections should remain outside the aggregate model.

---

## 30. Persistence Considerations

The persistence model may use JPA, but JPA must not define domain behavior.

Possible persistence structure:

```text
orders
order_items
order_status_history
order_domain_event_outbox
```

Possible Order columns:

```text
id
order_number
customer_id
status
currency
subtotal
discount_total
tax_total
fee_total
freight_total
grand_total
submitted_by
submitted_at
cancelled_by
cancelled_at
cancellation_reason
version
created_at
updated_at
```

Possible Order Item columns:

```text
id
order_id
product_id
sku
product_name
quantity
unit_price
effective_price
discount_amount
tax_amount
fee_amount
subtotal
total
```

Snapshot fields may be:

- Embedded
- Serialized into structured JSON
- Stored in dedicated columns
- Stored in dedicated snapshot tables

The final choice must consider:

- Query requirements
- Auditability
- Schema evolution
- Indexing
- Data volume
- Database portability
- Reporting requirements

---

## 31. Persistence Mapping Rules

- Persistence mapping must preserve aggregate invariants.
- Collections must not become publicly mutable because of ORM requirements.
- JPA no-argument constructors should have restricted visibility.
- Domain constructors should remain explicit.
- Public setters should not be added for JPA convenience.
- Lazy-loading proxies must not escape the transaction unexpectedly.
- Cascade operations must reflect aggregate ownership.
- Orphan removal may be used for internal Order Items when appropriate.
- External context entities must not be mapped as aggregate relationships.
- Customer ID and Product ID are references, not JPA cross-context associations.

---

## 32. Optimistic Concurrency Control

The Order aggregate must use optimistic concurrency control.

Possible field:

```text
version
```

Possible implementation:

```java
@Version
private Long version;
```

Business meaning:

- Two concurrent commands must not silently overwrite each other.
- A stale aggregate update must fail.
- The application layer must translate the conflict into a stable error.
- Automatic retries must be used carefully.
- State-changing requests may require client-visible version information.

Possible error code:

```text
ORDER_CONCURRENT_MODIFICATION
```

---

## 33. Concurrency Scenarios

### 33.1 Concurrent Item Updates

Two users update the same Draft Order.

Expected behavior:

- One transaction succeeds.
- The second detects a version conflict.
- No item update is silently lost.

---

### 33.2 Submission and Item Update

One request submits the Order while another changes Quantity.

Expected behavior:

- Only one consistent version succeeds.
- The losing operation receives a conflict.
- The Order must not be submitted with unpriced or stale item data.

---

### 33.3 Duplicate Approval Outcome

The same Approval event is delivered more than once.

Expected behavior:

- The first valid outcome applies the transition.
- Duplicate delivery does not append duplicate history.
- Duplicate delivery does not publish duplicate business effects.

---

### 33.4 Cancellation and Inventory Confirmation

Cancellation and Inventory confirmation arrive concurrently.

Expected behavior:

- Optimistic locking prevents silent state corruption.
- The application layer reloads or rejects stale processing.
- Compensation rules determine whether Inventory Release is required.

---

## 34. Idempotency

Aggregate concurrency and application idempotency are related but distinct.

Optimistic locking prevents lost updates.

Idempotency prevents repeated commands or events from duplicating business effects.

Idempotency may use:

- Command ID
- API Idempotency Key
- Event ID
- Approval Process ID
- Reservation Request ID
- Fulfillment Request ID

Rules:

- A retry must preserve the original identifier.
- The application layer may store processed request identifiers.
- The aggregate may preserve business process references required to detect duplicates.
- Duplicate operations should return the existing result when practical.
- Idempotency retention must be defined.

---

## 35. Aggregate Size

The Order aggregate may contain multiple Order Items.

Large enterprise Orders may make aggregate size significant.

The design must consider:

- Maximum number of Order Items
- Load time
- Memory consumption
- Persistence cost
- Lock duration
- Event payload size
- API payload size

Possible approaches for very large Orders:

- Explicit maximum item count
- Batch commands
- Separate pricing calculation outside the transaction
- Query projections
- Chunked external validation
- Dedicated large-order workflow
- Aggregate redesign if business requirements exceed practical limits

Prematurely splitting Order Items into independent aggregates would weaken consistency and should not be done without evidence.

---

## 36. Aggregate Transaction Scope

A state-changing Order operation should normally execute within one local transaction.

The transaction may include:

- Loading the Order
- Executing one aggregate operation
- Saving the Order
- Saving Order Status History
- Saving idempotency data
- Saving Outbox Records

The transaction must not wait for:

- Notification delivery
- Inventory Reservation completion
- Fulfillment completion
- Payment confirmation
- SQS publication acknowledgement outside the Outbox mechanism
- Long-running external processing

External reads required before the transaction should be coordinated carefully to avoid stale decisions.

---

## 37. External Validation Timing

External validation may occur:

### Before Loading the Aggregate

Suitable when:

- Validation is independent from Order state
- The result does not require aggregate information

### After Loading the Aggregate but Before Mutation

Suitable when:

- Validation depends on current Order data
- The aggregate must remain unchanged until all required information is available

### During Submission Preparation

Typical sequence:

1. Load Order.
2. Confirm it is a Draft candidate.
3. Obtain Customer data.
4. Obtain Product data.
5. Calculate Pricing.
6. Evaluate Approval requirements.
7. Invoke aggregate behavior.
8. Persist aggregate and Outbox Records.

The aggregate must revalidate all invariants that depend on its own state.

---

## 38. Domain Service Candidates

A Domain Service should exist only when a business rule does not naturally belong to one entity or value object.

Possible candidates:

### OrderPricingConsistencyPolicy

Responsibility:

- Validate that Pricing Result corresponds to current Order Items
- Validate totals
- Validate Currency
- Validate policy version requirements

This may also be implemented within the Order aggregate if it remains cohesive.

### OrderCancellationPolicy

Responsibility:

- Determine whether cancellation is allowed
- Determine required compensating actions

This may be useful if cancellation rules become complex and configurable.

### OrderSubmissionPolicy

Responsibility:

- Evaluate aggregate-independent submission rules

External Customer and Product validation still belongs in application orchestration or their owning contexts.

Avoid creating generic domain services that merely move behavior out of the aggregate.

---

## 39. Factory Candidates

Possible factory:

```text
OrderFactory
```

Responsibilities:

- Generate or receive Order ID
- Generate or receive Order Number
- Create initial Order state
- Apply creation policies
- Record initial Domain Event

A factory is justified only when creation becomes too complex for a clear static factory method.

Preferred initial approach:

```text
Order.create(...)
```

---

## 40. Query Model Separation

Order queries do not always require loading the aggregate.

Possible query models:

```text
OrderSummary
OrderDetails
OrderItemView
OrderStatusHistoryView
OrderSearchResult
```

Rules:

- Query projections are not Domain entities.
- Query models may be optimized for read use cases.
- Query models must not be used to execute aggregate behavior.
- Search APIs should not load full aggregates unnecessarily.
- CQRS may be applied pragmatically without requiring separate infrastructure initially.

---

## 41. Serialization Boundaries

The Order aggregate must not be serialized directly as:

- REST Response
- SQS integration-event payload
- Audit payload
- Cache contract
- External API contract

Adapters must map the aggregate to explicit models.

Reasons:

- Prevent accidental data exposure
- Preserve contract stability
- Avoid lazy-loading problems
- Avoid exposing internal structure
- Allow context-specific representation
- Protect invariant implementation

---

## 42. Security Considerations

The aggregate must not contain:

- Access tokens
- Refresh tokens
- Passwords
- Authentication sessions
- Provider credentials
- Raw sensitive payment data

Actor references should use stable identifiers.

Authorization is coordinated by the application layer.

The aggregate still enforces business eligibility based on the operation's supplied business context.

Example:

- The application validates that the User has permission to approve.
- The aggregate validates that the Order is currently pending Approval.

---

## 43. Audit Considerations

Order Status History is part of the business model.

Audit Records belong to Audit Context.

The aggregate may produce facts required for Audit.

Examples:

- Order created
- Order submitted
- Order cancelled
- Order approved
- Order rejected

Audit payloads must be safe and minimal.

Status History and Audit Trail must not be treated as the same structure.

---

## 44. Testing Strategy

The Order aggregate should be tested primarily through unit tests without Spring.

Tests should cover:

- Successful creation
- Invalid creation
- Item addition
- Item update
- Item removal
- Pricing application
- Pricing inconsistency
- Submission without items
- Submission without Pricing
- Successful submission
- Approval required flow
- Approval outcome
- Rejection
- Review request
- Inventory request
- Inventory confirmation
- Inventory failure
- Readiness for Fulfillment
- Completion
- Cancellation
- Invalid transitions
- Duplicate outcomes
- Domain Event generation
- Status History
- Immutability
- Defensive copies

Test names should describe observable business behavior.

Example:

```java
@Test
void testSubmitShouldRejectOrderWithoutItems() {
    Order order = OrderTestFixture.draftOrderWithoutItems();

    assertThatThrownBy(() -> order.submit(
            CUSTOMER_SNAPSHOT,
            PRICING_SNAPSHOT,
            APPROVAL_NOT_REQUIRED,
            USER_ID,
            SUBMITTED_AT,
            CORRELATION_ID
    ))
            .as("Submission of an order without items")
            .isInstanceOf(OrderWithoutItemsException.class);
}
```

Assertions must include AssertJ descriptions.

Example:

```java
assertThat(order.status())
        .as("Order status after successful submission")
        .isEqualTo(OrderStatus.PROCESSING);
```

---

## 45. Test Fixture Principles

Test fixtures should:

- Use fixed identifiers
- Use fixed timestamps
- Avoid random UUID generation
- Avoid current system time
- Avoid shared mutable instances
- Use valid defaults
- Allow explicit override of relevant fields
- Keep business intent visible

Possible fixtures:

```text
OrderTestFixture
CustomerSnapshotTestFixture
ProductSnapshotTestFixture
PricingSnapshotTestFixture
```

Avoid oversized fixture builders that hide the scenario being tested.

---

## 46. Initial Java Design Sketch

The following sketch is illustrative and not final implementation code:

```java
public final class Order {

    private final OrderId id;
    private OrderNumber orderNumber;
    private final CustomerReference customerReference;
    private CustomerSnapshot customerSnapshot;
    private OrderStatus status;
    private final List<OrderItem> items;
    private PricingSnapshot pricingSnapshot;
    private SubmissionDetails submissionDetails;
    private CancellationDetails cancellationDetails;
    private final List<OrderStatusHistoryEntry> statusHistory;
    private final List<DomainEvent> domainEvents;
    private long version;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(
            OrderId id,
            CustomerReference customerReference,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.customerReference = Objects.requireNonNull(customerReference);
        this.status = OrderStatus.DRAFT;
        this.items = new ArrayList<>();
        this.statusHistory = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = createdAt;
    }

    public static Order create(
            OrderId id,
            CustomerReference customerReference,
            ActorId createdBy,
            Instant createdAt,
            CorrelationId correlationId
    ) {
        Order order = new Order(id, customerReference, createdAt);
        order.recordEvent(new OrderCreated(
                EventId.generate(),
                id,
                customerReference.customerId(),
                createdBy,
                createdAt,
                correlationId
        ));
        return order;
    }
}
```

The final implementation may use records for immutable value objects.

Domain code must remain independent from Spring and persistence frameworks.

---

## 47. Initial Package Design

Possible conceptual package structure:

```text
order
├── domain
│   ├── model
│   │   ├── Order
│   │   ├── OrderItem
│   │   ├── OrderStatus
│   │   ├── CustomerSnapshot
│   │   ├── ProductSnapshot
│   │   ├── PricingSnapshot
│   │   └── valueobject
│   ├── event
│   ├── exception
│   ├── policy
│   └── repository
├── application
│   ├── command
│   ├── query
│   ├── port
│   └── service
├── infrastructure
│   ├── persistence
│   ├── messaging
│   └── integration
└── api
    ├── request
    ├── response
    └── controller
```

The exact package structure will be finalized before implementation.

---

## 48. Architecture Rules

The following rules apply to the Order aggregate:

- The Domain Layer must not depend on Spring.
- The Domain Layer must not depend on JPA.
- The Domain Layer must not depend on Amazon SQS or the AWS SDK.
- The Domain Layer must not depend on REST.
- The Order aggregate must not depend on another bounded context.
- The Order aggregate must not call external services.
- Controllers must not modify aggregate state directly.
- Repositories must not contain business rules.
- Persistence entities must not be public integration contracts.
- Status changes must occur through explicit behavior.
- Internal collections must not be publicly mutable.
- Time-dependent behavior must receive a timestamp or Clock abstraction.
- Monetary calculations must not use floating-point types.
- Domain Events must be immutable.

---

## 49. Open Aggregate Decisions

The following questions remain open:

1. Is Order Number generated at creation or submission?
2. Is `SUBMITTED` a persistent state or only a business event?
3. Is `APPROVED` a persistent state or an immediate transition to `PROCESSING`?
4. Does `REVIEW_REQUESTED` reopen the Order automatically?
5. May the same Product appear more than once?
6. Does adding the same Product merge Quantity?
7. Can an Order contain multiple delivery destinations?
8. Does every item use the same Currency?
9. Can Pricing be partially recalculated?
10. How is Pricing staleness detected?
11. Is Customer Snapshot created at Order creation or submission?
12. Are Product Snapshots refreshed while the Order remains in Draft?
13. Does Inventory Reservation occur for the entire Order or per item?
14. Is partial Inventory Reservation allowed?
15. Does Reservation expiration return the Order to `INVENTORY_FAILED`?
16. Which statuses are cancellable?
17. Can an Order be cancelled after Fulfillment starts?
18. Is Payment required before `READY_FOR_FULFILLMENT`?
19. Which external process references must be stored in the aggregate?
20. What is the maximum number of Order Items?
21. Should Status History remain inside the aggregate or be persisted as a related append-only model?
22. Which Domain Events remain internal?
23. Which Domain Events become Integration Events?
24. Should the aggregate preserve aggregate version in Domain Events?
25. How should stale asynchronous outcomes be handled?
26. Does a rejected Order terminate permanently or allow resubmission?
27. Which operations require API Idempotency Keys?
28. Should cancellation policies be configurable?
29. Should the aggregate use a dedicated Pricing State?
30. Should a dedicated `OrderProcessingStatus` replace generic `PROCESSING`?

---

## 50. Decision Summary

The initial Order aggregate design establishes that:

- Order is the aggregate root.
- Order Item is an internal entity.
- Order owns Customer, Product, and Pricing snapshots.
- Customer and Product master data remain outside the aggregate.
- Pricing policies remain outside the aggregate.
- Approval Process and Inventory Reservation remain separate aggregates.
- All Order state changes occur through explicit behavior.
- Public setters are prohibited.
- Status transitions are explicitly validated.
- Submission freezes accepted snapshots and Pricing.
- Cancellation is an explicit business action, not deletion.
- Domain Events describe completed business facts.
- Integration Events are produced outside the Domain Layer.
- The aggregate uses optimistic concurrency control.
- Idempotency is required for duplicate commands and asynchronous outcomes.
- The aggregate is persisted atomically with history, idempotency data, and Outbox Records when required.
- Query models remain separate from the behavioral aggregate.
- Domain code remains independent from frameworks.
- Aggregate boundaries will be protected by unit tests and architecture tests.

---

## 51. Next Documentation Step

The next document will define the Order lifecycle in greater detail:

```text
docs/domain/order-state-machine.md
```

It will establish:

- Official Order statuses
- Allowed transitions
- Transition triggers
- Preconditions
- Side effects
- Rejected transitions
- Terminal states
- Cancellation rules
- Review flow
- Approval flow
- Inventory flow
- Fulfillment flow
- Concurrency and stale-event behavior
