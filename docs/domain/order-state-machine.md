# Order State Machine

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Order State Machine |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the official lifecycle of an Order inside the Order Management bounded context.

It establishes:

- Valid states
- State transitions
- Transition triggers
- Preconditions
- Side effects
- Domain events
- Invalid transitions
- Terminal states
- Cancellation policy
- Eventual consistency states

The Order aggregate is the only component allowed to change its lifecycle state.

No infrastructure component, controller, repository, scheduler, or integration adapter may update Order Status directly.

---

# 2. State Machine Overview

```text
                         +----------------+
                         |     DRAFT      |
                         +----------------+
                                 |
                              Submit
                                 |
                +----------------+----------------+
                |                                 |
         Approval Required                  No Approval
                |                                 |
                v                                 v
      +--------------------+            +------------------+
      | PENDING_APPROVAL   |            |    PROCESSING    |
      +--------------------+            +------------------+
         |        |      |                       |
         |        |      |                       |
 Approve | Reject |Review|                       |
         |        |      |                       |
         v        v      v                       |
   APPROVED   REJECTED REVIEW_REQUESTED          |
         |               |                       |
         +-------+-------+                       |
                 |                               |
                 +-------------------------------+
                                 |
                                 v
                      +----------------------+
                      | INVENTORY_PENDING    |
                      +----------------------+
                                 |
                    +------------+------------+
                    |                         |
                    |                         |
             Reservation OK           Reservation Failed
                    |                         |
                    v                         v
        +-----------------------+   +----------------------+
        | INVENTORY_RESERVED    |   | INVENTORY_FAILED     |
        +-----------------------+   +----------------------+
                    |
                    v
      +------------------------------+
      | READY_FOR_FULFILLMENT         |
      +------------------------------+
                    |
                    v
      +------------------------------+
      | FULFILLMENT_IN_PROGRESS       |
      +------------------------------+
                    |
                    v
            +----------------+
            |   COMPLETED    |
            +----------------+
```

---

# 3. State Definitions

## DRAFT

### Meaning

The Order is editable.

The customer is still preparing the Order.

### Allowed Operations

- Add Item
- Remove Item
- Update Quantity
- Change Delivery Information
- Apply Pricing
- Recalculate Pricing
- Submit
- Cancel

### Forbidden Operations

- Approve
- Reserve Inventory
- Fulfill
- Complete

---

## PENDING_APPROVAL

### Meaning

The Order was submitted and is waiting for approval.

### Allowed Operations

- Approve
- Reject
- Request Review
- Cancel

### Forbidden Operations

- Add Item
- Remove Item
- Update Quantity
- Recalculate Pricing

---

## APPROVED

### Meaning

Approval has completed successfully.

Normally this is a transient business state.

It may immediately transition to PROCESSING.

---

## REJECTED

### Meaning

Approval rejected the Order.

The Order cannot continue.

Possible future actions depend on business policy.

---

## REVIEW_REQUESTED

### Meaning

The approver requested corrections.

Possible next state:

```text
REVIEW_REQUESTED
      |
      v
DRAFT
```

---

## PROCESSING

### Meaning

Order Management is preparing downstream operations.

Typical actions include:

- Generate Reservation Request
- Publish Integration Events
- Validate workflow continuation

---

## INVENTORY_PENDING

### Meaning

Inventory Reservation has been requested.

The Order is waiting for Inventory outcome.

---

## INVENTORY_RESERVED

### Meaning

Inventory Reservation completed successfully.

---

## INVENTORY_FAILED

### Meaning

Reservation failed.

Possible actions:

- Retry
- Cancel
- User Review

---

## READY_FOR_FULFILLMENT

### Meaning

The Order completed every prerequisite.

---

## FULFILLMENT_IN_PROGRESS

### Meaning

Shipment preparation has started.

---

## COMPLETED

### Meaning

Business lifecycle completed.

Terminal State.

---

## CANCELLED

### Meaning

Order cancelled.

Terminal State.

---

# 4. Terminal States

Terminal states are:

```text
COMPLETED

CANCELLED

REJECTED
```

Once a terminal state is reached:

- no further processing
- no inventory request
- no approval
- no fulfillment

---

# 5. Transition Matrix

| From | Operation | To |
|-------|-----------|----|
| DRAFT | Submit | PROCESSING |
| DRAFT | Submit | PENDING_APPROVAL |
| DRAFT | Cancel | CANCELLED |
| PENDING_APPROVAL | Approve | APPROVED |
| PENDING_APPROVAL | Reject | REJECTED |
| PENDING_APPROVAL | Request Review | REVIEW_REQUESTED |
| REVIEW_REQUESTED | Reopen | DRAFT |
| APPROVED | Continue | PROCESSING |
| PROCESSING | Request Inventory | INVENTORY_PENDING |
| INVENTORY_PENDING | Reservation Success | INVENTORY_RESERVED |
| INVENTORY_PENDING | Reservation Failure | INVENTORY_FAILED |
| INVENTORY_RESERVED | Prepare Fulfillment | READY_FOR_FULFILLMENT |
| READY_FOR_FULFILLMENT | Start Fulfillment | FULFILLMENT_IN_PROGRESS |
| FULFILLMENT_IN_PROGRESS | Complete | COMPLETED |

---

# 6. Invalid Transitions

The aggregate must reject:

```text
COMPLETED -> DRAFT

COMPLETED -> PROCESSING

CANCELLED -> PROCESSING

REJECTED -> INVENTORY_PENDING

DRAFT -> INVENTORY_RESERVED

DRAFT -> COMPLETED

PENDING_APPROVAL -> COMPLETED

INVENTORY_PENDING -> APPROVED
```

Attempting an invalid transition must raise:

```text
InvalidOrderStatusTransitionException
```

---

# 7. Cancellation Policy

Cancellation is initially allowed from:

- DRAFT
- PENDING_APPROVAL
- REVIEW_REQUESTED
- PROCESSING
- INVENTORY_PENDING
- INVENTORY_RESERVED
- INVENTORY_FAILED
- READY_FOR_FULFILLMENT

Not allowed from:

- COMPLETED
- CANCELLED

Fulfillment cancellation remains an open decision.

---

# 8. Transition Side Effects

Every transition may produce:

- Status History
- Domain Events
- Audit Facts
- Integration Events
- Outbox Records

Example:

```text
PROCESSING
      |
Inventory Requested
      |
      v

InventoryReservationRequested

↓

Outbox Record

↓

Kafka Event
```

---

# 9. Domain Events by State

| Transition | Event |
|------------|-------|
| Create | OrderCreated |
| Submit | OrderSubmitted |
| Approval Required | OrderApprovalRequired |
| Approve | OrderApproved |
| Reject | OrderRejected |
| Review | OrderReviewRequested |
| Inventory Request | InventoryReservationRequested |
| Inventory Reserved | InventoryReserved |
| Inventory Failed | InventoryReservationFailed |
| Ready | OrderReadyForFulfillment |
| Fulfillment Started | OrderFulfillmentStarted |
| Complete | OrderCompleted |
| Cancel | OrderCancelled |

---

# 10. Concurrency Rules

Optimistic locking protects state transitions.

Duplicate events must be ignored safely.

Examples:

- Duplicate Approval
- Duplicate Reservation
- Duplicate Cancellation
- Duplicate Completion

Each asynchronous message must contain:

- EventId
- CorrelationId
- AggregateVersion

---

# 11. Eventual Consistency

The following states exist because of asynchronous processing:

- PENDING_APPROVAL
- INVENTORY_PENDING
- READY_FOR_FULFILLMENT

The UI should communicate that processing is still occurring.

---

# 12. Aggregate Responsibilities

The Order aggregate is responsible for:

- validating transitions
- recording history
- producing Domain Events
- preserving invariants

Application services are responsible for:

- loading the aggregate
- calling external services
- publishing Outbox records
- committing transactions

---

# 13. Future Evolution

Possible future states include:

- PAYMENT_PENDING
- PAYMENT_CONFIRMED
- BACKORDER
- PARTIALLY_RESERVED
- PARTIALLY_FULFILLED
- RETURNED
- CLOSED

These states should only be introduced if required by business rules.

---

# 14. Decision Summary

The Order lifecycle is:

- Explicit
- Deterministic
- Auditable
- Event-driven
- Optimistically concurrent
- Protected by the aggregate
- Independent from infrastructure implementation

Status transitions represent business decisions, not technical workflow execution.

---

# 15. Next Documentation Step

The next tactical DDD document is:

```text
docs/domain/domain-events.md
```

It will formally specify:

- every Domain Event
- payloads
- event naming
- metadata
- versioning
- publication rules
- mapping to Integration Events
- Outbox integration
