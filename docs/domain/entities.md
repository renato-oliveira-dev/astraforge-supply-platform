# Entities

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Domain Entities |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Entities used by the Enterprise Order Platform.

It establishes:

- Entity responsibilities
- Identity
- Lifecycle
- Ownership
- Relationships
- Aggregate membership
- Behavioral responsibilities
- Persistence boundaries
- Java implementation recommendations

Entities represent domain concepts that possess identity and evolve over time.

Unlike Value Objects, Entities are distinguished by their identity rather than by their values.

---

# 2. Entity Characteristics

Every Entity has:

- Business identity
- Mutable lifecycle
- Stable identity
- Business behavior
- Equality based on identity
- Explicit ownership

Entities are responsible for protecting their own invariants.

---

# 3. Initial Entity Catalog

The initial domain contains the following Entities:

| Entity | Aggregate |
|---------|-----------|
| Order | Order |
| OrderItem | Order |
| OrderStatusHistoryEntry | Order |
| ApprovalProcess | Approval |
| ApprovalStep | Approval |
| InventoryReservation | Inventory |
| Shipment | Fulfillment |
| Payment | Payment |

Only Order and OrderItem are initially implemented inside the Order Management bounded context.

The remaining entities belong to their respective bounded contexts.

---

# 4. Aggregate Root

The aggregate root is always an Entity.

Initial aggregate roots:

| Aggregate | Root Entity |
|-----------|-------------|
| Order | Order |
| Approval | ApprovalProcess |
| Inventory | InventoryReservation |
| Fulfillment | Shipment |
| Payment | Payment |

Only the aggregate root may be referenced directly from outside the aggregate.

---

# 5. Order Entity

The Order Entity is the aggregate root.

Responsibilities:

- Protect aggregate invariants
- Control lifecycle
- Manage Order Items
- Record Domain Events
- Produce Status History
- Preserve Pricing Snapshot
- Preserve Customer Snapshot
- Coordinate state transitions

Order owns every object inside the Order aggregate.

---

# 6. Order Identity

Identity:

```
OrderId
```

Business identifier:

```
OrderNumber
```

OrderId never changes.

OrderNumber may be assigned after creation depending on business policy.

---

# 7. Order Lifecycle

Possible lifecycle:

```
DRAFT

↓

PROCESSING

↓

PENDING_APPROVAL

↓

APPROVED

↓

INVENTORY_PENDING

↓

READY_FOR_FULFILLMENT

↓

FULFILLMENT_IN_PROGRESS

↓

COMPLETED
```

Alternative paths:

```
REJECTED

REVIEW_REQUESTED

CANCELLED
```

---

# 8. Order Responsibilities

Order is responsible for:

- addItem
- removeItem
- updateQuantity
- applyPricing
- submit
- approve
- reject
- requestReview
- cancel
- complete

Order never delegates lifecycle validation to infrastructure.

---

# 9. Order Internal Structure

```
Order

OrderId

OrderNumber

CustomerReference

CustomerSnapshot

OrderStatus

OrderItems

PricingSnapshot

SubmissionDetails

CancellationDetails

StatusHistory

Version
```

---

# 10. OrderItem Entity

OrderItem belongs exclusively to Order.

It has identity because it changes independently inside the aggregate.

Identity

```
OrderItemId
```

---

Responsibilities

- maintain quantity
- preserve Product Snapshot
- preserve Item Pricing
- validate Quantity
- calculate totals

---

# 11. OrderItem Structure

```
OrderItem

OrderItemId

ProductReference

ProductSnapshot

Quantity

ItemPricing
```

OrderItem cannot exist without Order.

---

# 12. OrderItem Lifecycle

```
Created

↓

Updated

↓

Removed
```

Removal deletes the entity from the aggregate.

---

# 13. Status History Entity

OrderStatusHistoryEntry records one transition.

Identity is optional.

Possible structure

```
HistoryEntryId

PreviousStatus

NewStatus

ChangedAt

ChangedBy

Reason
```

History is append-only.

---

# 14. ApprovalProcess

Belongs to Approval Context.

Represents an approval workflow.

Responsibilities

- maintain workflow
- manage approval levels
- validate approvers
- publish outcomes

Order never owns ApprovalProcess.

---

# 15. ApprovalStep

Internal entity inside ApprovalProcess.

Represents one approval level.

Example

```
Manager

Supervisor

Director
```

---

# 16. InventoryReservation

Aggregate root of Inventory Context.

Responsibilities

- reserve stock
- release stock
- expire reservation
- publish reservation outcome

Order references only ReservationId.

---

# 17. Shipment

Aggregate root of Fulfillment.

Responsibilities

- shipping
- tracking
- delivery

Order stores only ShipmentId.

---

# 18. Payment

Aggregate root of Payment.

Responsibilities

- authorize
- capture
- refund
- cancel

Order stores only PaymentId.

---

# 19. Entity Relationships

```
Order

│

├── OrderItem

├── StatusHistory

├── CustomerSnapshot

├── PricingSnapshot

└── SubmissionDetails
```

OrderItem references

```
ProductReference
```

never Product Entity.

---

# 20. Ownership Rules

Order owns

- OrderItem
- StatusHistory
- PricingSnapshot
- CustomerSnapshot

Approval owns

- ApprovalProcess

Inventory owns

- InventoryReservation

Payment owns

- Payment

Fulfillment owns

- Shipment

---

# 21. Identity Rules

Identity never changes.

Example

```
OrderId
```

may never be replaced.

Equality uses only identity.

---

# 22. Equality

Correct

```
OrderId == OrderId
```

Incorrect

```
every field equal
```

Entities compare identities.

---

# 23. Encapsulation

Entities expose behavior.

Avoid

```java
setStatus()

setQuantity()

setPricing()
```

Prefer

```java
submit()

approve()

cancel()

updateQuantity()
```

---

# 24. Invariants

Order

- customer mandatory
- pricing mandatory before submit
- item quantity > 0
- immutable snapshots after submit
- valid transitions

OrderItem

- quantity > 0
- product mandatory
- pricing consistent

---

# 25. Persistence

Order

```
orders
```

OrderItem

```
order_items
```

History

```
order_status_history
```

---

# 26. Java Design

Prefer

```java
public class Order
```

Mutable entity.

Prefer

```java
record
```

only for Value Objects.

---

# 27. Constructors

Constructors must produce valid entities.

Never create partially initialized entities.

---

# 28. Factory Methods

Prefer

```java
Order.create(...)
```

instead of exposing constructors.

---

# 29. Domain Events

Entities record Domain Events.

Examples

```
OrderCreated

OrderSubmitted

OrderCancelled
```

---

# 30. Optimistic Locking

Entities support optimistic locking.

Example

```java
@Version

Long version;
```

---

# 31. Entity References

Never reference another aggregate by object.

Correct

```
CustomerId
```

Incorrect

```
Customer customer;
```

---

# 32. Entity Size

Keep entities cohesive.

If behavior becomes unrelated:

- extract Value Object
- extract Domain Service
- create new Aggregate

Do not split aggregates only because they become "large."

---

# 33. Testing

Every Entity should test

- creation
- lifecycle
- invariants
- invalid transitions
- equality
- optimistic locking
- domain events

---

# 34. Architecture Rules

Entities:

- do not depend on Spring
- do not depend on JPA
- do not depend on Kafka
- do not depend on REST

Entities expose business behavior only.

---

# 35. Decision Summary

The platform adopts:

- identity-based Entities
- behavior-rich entities
- explicit aggregate ownership
- lifecycle protection
- encapsulation
- optimistic concurrency
- aggregate-root access only

---

# 36. Next Documentation Step

Next document:

```
docs/domain/domain-services.md
```

It will define:

- Domain Services
- Policies
- Specifications
- Business calculations
- Aggregate collaboration
- Domain algorithms
