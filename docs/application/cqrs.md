# CQRS Architecture

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | CQRS |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines how Command Query Responsibility Segregation (CQRS) is applied within the Enterprise Order Platform.

The platform adopts **logical CQRS**, separating write and read models while sharing the same transactional database.

CQRS improves:

- scalability
- maintainability
- performance
- domain isolation
- API clarity

The platform does **not** implement Event Sourcing.

---

# 2. CQRS Overview

```
                    Client
                      │
          ┌───────────┴───────────┐
          │                       │
      Commands                Queries
          │                       │
          ▼                       ▼
  Command Handlers        Query Handlers
          │                       │
          ▼                       ▼
      Aggregates            Read Models
          │                       │
          ▼                       ▼
      Repository           Projection Repository
```

---

# 3. Commands

Commands express intent.

A command requests the system to perform an action.

Examples

```
CreateOrder

SubmitOrder

CancelOrder

ApproveOrder

RejectOrder

AddOrderItem

RemoveOrderItem

UpdateOrderQuantity
```

Commands never return aggregates.

---

# 4. Command Characteristics

Commands are:

- immutable
- validated
- intention revealing
- side-effect producing
- processed exactly once whenever possible

Example

```java
public record SubmitOrderCommand(

        OrderId orderId,

        UserId userId,

        CorrelationId correlationId

) {
}
```

---

# 5. Command Handlers

Each command has exactly one handler.

Example

```
SubmitOrderCommand

↓

SubmitOrderService
```

Responsibilities

- load aggregate
- execute domain behavior
- save aggregate
- collect events
- persist outbox

---

# 6. Command Flow

```
HTTP Request

↓

Controller

↓

Command

↓

Application Service

↓

Aggregate

↓

Repository

↓

Outbox

↓

Commit
```

---

# 7. Queries

Queries retrieve information.

Queries never modify state.

Examples

```
FindOrder

SearchOrders

OrderHistory

CustomerOrders

PendingApprovals
```

---

# 8. Query Characteristics

Queries are:

- read only
- side-effect free
- optimized for retrieval
- projection based
- cache friendly

---

# 9. Query Handlers

Every query has one handler.

Example

```
SearchOrdersQuery

↓

SearchOrdersQueryService
```

Responsibilities

- retrieve projections
- map DTOs
- apply pagination
- apply sorting
- apply filtering

---

# 10. Read Models

Read Models are optimized for querying.

They are not Aggregates.

Example

```
OrderSummaryView

OrderDetailView

CustomerOrdersView

ApprovalQueueView
```

---

# 11. Read Model Example

```
OrderSummaryView

OrderId

OrderNumber

CustomerName

Status

CreatedAt

GrandTotal
```

Only fields required by the query are returned.

---

# 12. Write Model

The Write Model consists of:

- Aggregates
- Entities
- Value Objects
- Domain Services

Only the Write Model enforces business rules.

---

# 13. Read Model

The Read Model contains:

- DTOs
- projections
- database views
- optimized SQL

No domain behavior exists here.

---

# 14. Repository Separation

Write side

```
OrderRepository
```

Read side

```
OrderProjectionRepository
```

Never mix responsibilities.

---

# 15. Projection Repository Example

```java
public interface OrderProjectionRepository {

    Page<OrderSummaryView> search(
            OrderSearchCriteria criteria
    );

}
```

---

# 16. DTO Mapping

Read Models map directly to API responses.

Avoid:

```
Projection

↓

Aggregate

↓

DTO
```

Prefer:

```
Projection

↓

DTO
```

---

# 17. Pagination

Queries support:

- page
- size
- sorting

Commands never paginate.

---

# 18. Filtering

Filtering belongs exclusively to Query Handlers.

Examples

- status
- customer
- region
- order type
- creation date
- approval state

---

# 19. Sorting

Sorting belongs to Query Services.

Examples

```
CreatedAt

OrderNumber

Status

Customer

Total
```

---

# 20. Caching

Read Models may be cached.

Examples

```
Redis

In-memory

CDN
```

Write Models must never be cached.

---

# 21. Transactions

Commands execute inside transactions.

Queries generally execute without transactions.

---

# 22. Performance

Read Models may:

- use joins
- use native SQL
- use views
- use materialized views
- use denormalized structures

Write Models prioritize consistency.

---

# 23. Validation

Commands

- validate intent
- execute business rules

Queries

- validate search parameters only

---

# 24. Event Integration

Successful commands generate Domain Events.

```
Command

↓

Aggregate

↓

Domain Event

↓

Integration Event

↓

Outbox
```

Queries never generate events.

---

# 25. Error Handling

Commands return:

- business errors
- validation errors

Queries return:

- empty results
- projections

---

# 26. Testing

Command tests verify:

- business behavior
- state transitions
- event generation

Query tests verify:

- filtering
- sorting
- pagination
- projection correctness

---

# 27. Architecture Rules

Commands:

- mutate state
- use aggregates
- use repositories
- publish events

Queries:

- never mutate state
- never load aggregates
- use projections
- optimize performance

---

# 28. Decision Summary

The platform adopts:

- logical CQRS
- separate write/read models
- immutable commands
- dedicated query services
- projection repositories
- event publication from commands only

---

# 29. Next Documentation Step

Next document:

```
docs/infrastructure/transactional-outbox.md
```

It will define:

- Transactional Outbox Pattern
- Event persistence
- Event publication
- Retry strategy
- Dead Letter Queue
- Ordering guarantees
- Idempotency
- Failure recovery
