# Saga Pattern

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Saga Pattern |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines how distributed business transactions are coordinated within the AstraForge Supply Platform.

The platform adopts the Saga Pattern to provide:

- eventual consistency
- autonomous services
- distributed transaction management
- compensation
- failure recovery
- scalability
- resilience

The platform never uses Two-Phase Commit (2PC).

---

# 2. Why Sagas?

A single business operation spans multiple bounded contexts.

Example

```
Order

↓

Inventory

↓

Payment

↓

Shipping

↓

Notification
```

Each service owns its own database.

Distributed ACID transactions are therefore not possible.

---

# 3. Architecture Decision

The platform adopts:

```
Choreography
```

as the default strategy.

A lightweight Process Manager may be introduced only when business workflows become highly complex.

---

# 4. Choreography

Each service reacts to Integration Events.

Example

```
Order Submitted

↓

Inventory reserves stock

↓

Inventory publishes ReservationConfirmed

↓

Payment authorizes payment

↓

Payment publishes PaymentAuthorized

↓

Shipping creates shipment

↓

Notification sends email
```

No central coordinator exists.

---

# 5. Benefits

Advantages

- low coupling
- independent deployment
- scalability
- simple infrastructure
- autonomous bounded contexts

Trade-offs

- harder tracing
- event proliferation
- more observability required

---

# 6. Typical Business Flow

```
Customer

↓

Submit Order

↓

Order Context

↓

OrderSubmitted
```

Inventory

```
ReservationRequested

↓

Reserve Stock

↓

ReservationConfirmed
```

Payment

```
PaymentRequested

↓

Authorize

↓

PaymentAuthorized
```

Shipping

```
ShipmentRequested

↓

Create Shipment

↓

ShipmentCreated
```

Notification

```
OrderCompleted

↓

Email

↓

SMS

↓

Webhook
```

---

# 7. Successful Saga

```
SubmitOrder

↓

OrderSubmitted

↓

InventoryReserved

↓

PaymentAuthorized

↓

ShipmentCreated

↓

OrderCompleted
```

---

# 8. Compensation

Failures trigger compensating actions.

Example

```
OrderSubmitted

↓

InventoryReserved

↓

PaymentFailed
```

Compensation

```
ReleaseInventory

↓

InventoryReleased

↓

CancelOrder
```

Business consistency is restored.

---

# 9. Compensation Principles

Compensation is:

- explicit
- asynchronous
- idempotent
- reversible whenever possible

Compensation is **not** database rollback.

---

# 10. Compensation Catalog

| Failure | Compensation |
|---------|--------------|
| PaymentFailed | ReleaseInventory |
| InventoryFailed | CancelOrder |
| ShipmentFailed | RefundPayment |
| ApprovalRejected | CancelOrder |
| Timeout | CancelOrder |

---

# 11. Long Running Transactions

A Saga may last:

- seconds
- minutes
- hours
- days

The system must tolerate incomplete workflows.

---

# 12. Timeouts

Every business step defines a timeout.

Example

Inventory Reservation

```
5 minutes
```

Payment

```
10 minutes
```

Shipping

```
30 minutes
```

Expired steps trigger compensation.

---

# 13. Retry Policy

Retries are allowed only for transient failures.

Examples

- network timeout
- broker unavailable
- temporary external API failure

Business failures are never retried automatically.

---

# 14. Event Sequence

```
OrderSubmitted

↓

InventoryReservationRequested

↓

InventoryReserved

↓

PaymentRequested

↓

PaymentAuthorized

↓

ShipmentRequested

↓

ShipmentCreated

↓

OrderCompleted
```

---

# 15. Failure Sequence

```
OrderSubmitted

↓

InventoryReserved

↓

PaymentFailed

↓

ReleaseInventory

↓

InventoryReleased

↓

OrderCancelled
```

---

# 16. Saga State

Every Saga instance has a state.

Example

```
STARTED

WAITING_INVENTORY

WAITING_PAYMENT

WAITING_SHIPPING

COMPLETED

FAILED

COMPENSATING

CANCELLED
```

---

# 17. Correlation

Every event contains:

```
CorrelationId
```

All events belonging to the same Saga share the same CorrelationId.

---

# 18. Causation

Every event contains:

```
CausationId
```

This identifies the event that originated the current event.

---

# 19. Observability

Tracing should reconstruct the entire Saga.

Visualization

```
Order

↓

Inventory

↓

Payment

↓

Shipment

↓

Notification
```

using:

- CorrelationId
- TraceId
- EventId

---

# 20. Idempotency

Every Saga participant must be idempotent.

Duplicate events must not change business state.

---

# 21. Process Manager (Optional)

For highly complex workflows, a Process Manager may coordinate execution.

Responsibilities

- timeout management
- manual intervention
- workflow visualization
- escalation
- audit

It must not contain domain business rules.

---

# 22. Error Handling

Business errors

↓

Compensation

Technical errors

↓

Retry

Unknown errors

↓

Manual intervention

---

# 23. Manual Recovery

Operators may:

- retry
- compensate
- cancel
- continue

Every manual action must be audited.

---

# 24. Monitoring

Expose metrics

- active sagas
- completed sagas
- compensations
- retries
- failures
- timeout count
- average duration

---

# 25. Logging

Log

- saga start
- each step
- compensation
- completion
- timeout
- failures

Always include:

- CorrelationId
- TraceId
- SagaId

---

# 26. Testing

Verify

- successful flow
- inventory failure
- payment failure
- shipment failure
- duplicate events
- retries
- compensation
- timeout
- replay

---

# 27. Architecture Rules

The platform adopts:

- choreography first
- eventual consistency
- asynchronous compensation
- idempotent participants
- explicit business events
- observable workflows

---

# 28. Decision Summary

The AstraForge Supply Platform uses:

- Saga Pattern
- Choreography
- Event-Driven Architecture
- Transactional Outbox
- Idempotent Consumers
- Compensation Events
- CorrelationId
- TraceId

---

# 29. Next Documentation Step

Next document

```
docs/infrastructure/sqs-architecture.md
```

It will define:

- Standard versus FIFO queue selection
- Queue naming and ownership
- Producer configuration
- Consumer configuration
- MessageGroupId strategy
- Visibility timeout and redelivery
- Dead Letter Queue
- Consumer scaling
- IAM and security
- Performance and backlog monitoring
