# Repository Contracts

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Repository Contracts |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Repository contracts of the Enterprise Order Platform.

It establishes:

- Repository responsibilities
- Aggregate persistence rules
- Loading strategies
- Optimistic concurrency
- Transaction boundaries
- Repository contracts
- Query separation
- Java implementation guidelines

Repositories provide access to Aggregate Roots.

Repositories are not generic DAOs.

---

# 2. Repository Definition

A Repository is responsible for:

- loading aggregates
- saving aggregates
- removing aggregates
- finding aggregates

Repositories abstract persistence mechanisms.

The Domain does not know whether persistence uses:

- PostgreSQL
- MongoDB
- Event Store
- JPA
- JDBC

---

# 3. Repository Principles

Repositories:

- persist Aggregate Roots
- never expose persistence details
- never contain business rules
- expose domain language
- hide ORM details

---

# 4. Aggregate Ownership

Each Aggregate Root owns one Repository.

| Aggregate | Repository |
|-----------|------------|
| Order | OrderRepository |
| Approval | ApprovalRepository |
| Inventory | InventoryReservationRepository |
| Shipment | ShipmentRepository |
| Payment | PaymentRepository |

---

# 5. OrderRepository

Primary repository of the Order Context.

Example

```java
public interface OrderRepository {

    Optional<Order> findById(
            OrderId id
    );

    void save(
            Order order
    );

    void delete(
            OrderId id
    );

}
```

---

# 6. Repository Responsibilities

OrderRepository is responsible for:

- loading complete Order aggregates
- saving aggregate changes
- optimistic locking
- aggregate reconstruction

It is not responsible for:

- validation
- pricing
- approval
- business decisions

---

# 7. Loading Aggregates

Command use cases require complete aggregates.

Correct

```
Order Aggregate

↓

Order

↓

Items

↓

Snapshots

↓

History
```

Incorrect

```
Partial aggregate
```

---

# 8. Saving Aggregates

Repositories save only Aggregate Roots.

Example

```
repository.save(order)
```

Never

```
repository.save(item)
```

OrderItem belongs to Order.

---

# 9. Optimistic Locking

Repositories support optimistic concurrency.

Example

```
Version

↓

compare

↓

update

↓

increment
```

Concurrent modifications fail.

---

# 10. Aggregate Reconstruction

Repositories rebuild aggregates.

Responsibilities include:

- loading entities
- rebuilding Value Objects
- restoring invariants

---

# 11. Repository Queries

Repositories expose only domain-relevant operations.

Examples

```java
findById()

exists()

save()

delete()
```

Avoid arbitrary SQL-like methods.

---

# 12. Search Operations

Searches belong to Query Services.

Incorrect

```java
findOrdersByTwentyFilters(...)
```

Correct

```
SearchOrdersQueryService
```

---

# 13. Read Model Separation

Commands use Aggregate Repositories.

Queries use dedicated projections.

```
Commands

↓

Repositories

Queries

↓

Read Models
```

---

# 14. Specifications

Complex retrieval may use Specifications.

Example

```java
OrdersAwaitingApprovalSpecification
```

Specifications remain in the Domain.

---

# 15. Pagination

Repositories should not expose pagination for aggregates.

Large result sets belong to Query Services.

---

# 16. Transactions

Repositories do not open transactions.

Application Services own transactions.

---

# 17. Lazy Loading

Avoid lazy loading inside aggregates.

Prefer complete aggregate loading.

---

# 18. Caching

Caching is an infrastructure concern.

Repositories expose no cache APIs.

---

# 19. Event Sourcing Compatibility

Repository contracts remain compatible with Event Sourcing.

Possible implementations:

- Snapshot Repository
- Event Store
- Hybrid

---

# 20. Error Handling

Repositories throw technical exceptions only.

Business exceptions belong to the Domain.

---

# 21. Java Guidelines

Repositories should be interfaces.

Example

```java
public interface OrderRepository {

}
```

Infrastructure provides implementations.

---

# 22. Testing

Repository contract tests should verify:

- save
- load
- delete
- optimistic locking
- reconstruction

Infrastructure tests validate persistence mappings.

---

# 23. Architecture Rules

Repositories:

- belong to the Application Layer
- expose Aggregate Roots
- hide persistence
- contain no business logic
- remain technology independent

---

# 24. Decision Summary

The platform adopts:

- one Repository per Aggregate Root
- aggregate persistence only
- optimistic locking
- CQRS-compatible repositories
- infrastructure-independent contracts

---

# 25. Next Documentation Step

Next document

```
docs/application/cqrs.md
```

It will define:

- Commands
- Queries
- Command Handlers
- Query Handlers
- Read Models
- Write Models
- Separation principles
