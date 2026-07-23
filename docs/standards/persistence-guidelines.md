# Persistence Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Persistence Guidelines |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the persistence standards adopted by the Enterprise Order Platform.

It establishes rules for:

- PostgreSQL
- JPA/Hibernate
- Spring Data JPA
- Flyway
- transactions
- locking
- concurrency
- indexes
- query design
- JSONB usage
- auditing
- soft delete
- performance
- schema evolution
- data consistency

Persistence is considered an implementation detail of the infrastructure layer and must never dictate domain design.

---

# 2. Core Principles

Persistence must be:

- transparent to the domain
- deterministic
- performant
- scalable
- observable
- migration-friendly
- vendor-aware
- consistent
- testable

The database is the system of record.

Business rules belong in the domain, not in the database, except when enforcing data integrity.

---

# 3. Architectural Position

Persistence belongs exclusively to the Infrastructure layer.

Dependency direction:

```text
Domain

↑

Application

↑

Infrastructure (Persistence)
```

The domain must not depend on:

- JPA
- Hibernate
- Spring Data
- SQL
- PostgreSQL
- Flyway

---

# 4. Repository Pattern

Repositories expose domain-oriented contracts.

Example:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    void save(Order order);

    void delete(OrderId orderId);
}
```

Infrastructure provides the implementation.

Example:

```java
JpaOrderRepository
```

---

# 5. Persistence Model

Persistence entities are infrastructure models.

They are not domain entities.

Recommended flow:

```text
REST DTO

↓

Application

↓

Domain Aggregate

↓

Persistence Mapper

↓

JPA Entity
```

Never expose persistence entities outside infrastructure.

---

# 6. JPA Entity Naming

Persistence entities use explicit suffixes.

Examples:

```java
OrderJpaEntity

CustomerJpaEntity

OutboxEventJpaEntity
```

Avoid:

```java
OrderEntity
```

when the project also contains domain entities.

---

# 7. Mapping Responsibility

Mapping belongs to infrastructure.

Example:

```text
Domain Aggregate

↓

Persistence Mapper

↓

JpaEntity
```

Avoid embedding persistence mapping logic inside aggregates.

---

# 8. Aggregate Persistence

One aggregate maps to one aggregate root table.

Child entities should be persisted through the aggregate.

Example:

```text
order_header

↓

order_item
```

Repositories should persist the aggregate as one consistency boundary.

---

# 9. Table Naming

Use lowercase snake_case.

Examples:

```sql
order_header

order_item

inventory_reservation

payment_authorization

outbox_event
```

The project adopts singular table names.

---

# 10. Column Naming

Columns use lowercase snake_case.

Examples:

```sql
order_id

customer_id

created_at

updated_at

approved_by

approval_status
```

---

# 11. Primary Keys

Primary keys should use UUID.

Example:

```sql
order_id UUID PRIMARY KEY
```

Avoid surrogate numeric identifiers unless justified by integration or storage constraints.

---

# 12. Business Identifiers

Business identifiers should be stored separately.

Example:

```sql
order_number

invoice_number

external_reference
```

Business identifiers must never replace technical identifiers.

---

# 13. Foreign Keys

Always define foreign keys unless a documented architecture decision justifies otherwise.

Example:

```sql
order_item.order_id

REFERENCES order_header(order_id)
```

Foreign keys preserve integrity.

---

# 14. Constraints

Database constraints complement domain validation.

Typical constraints:

- NOT NULL
- UNIQUE
- CHECK
- FOREIGN KEY

Do not rely solely on application validation.

---

# 15. Check Constraints

Use CHECK constraints for invariant protection.

Example:

```sql
CHECK (quantity > 0)

CHECK (total_amount >= 0)
```

Keep database rules aligned with domain rules.

---

# 16. Unique Constraints

Examples:

```sql
external_reference

customer_number

event_id
```

Constraint names:

```text
uk_<table>_<field>
```

---

# 17. Index Naming

Index names follow:

```text
idx_<table>_<columns>
```

Examples:

```text
idx_order_header_customer_id

idx_order_header_status

idx_outbox_event_pending
```

---

# 18. Composite Indexes

Composite indexes should reflect query patterns.

Example:

```sql
(status, created_at)
```

Order matters.

Do not create indexes based only on speculation.

---

# 19. Index Review

Every new index should answer:

- Which query benefits?
- What is the expected selectivity?
- What is the write cost?
- Is another index already sufficient?

Indexes have maintenance cost.

---

# 20. Transactions

Transaction boundaries belong to the Application layer.

Controllers must not define transactions.

Repositories should not start transactions.

---

# 21. Transaction Scope

Keep transactions short.

Inside a transaction:

- validation
- aggregate persistence
- outbox persistence

Avoid:

- HTTP calls
- Kafka publishing
- email sending
- long calculations

---

# 22. Transaction Isolation

Default:

```text
READ COMMITTED
```

Increase isolation only when justified.

Higher isolation reduces concurrency.

---

# 23. Optimistic Locking

Use optimistic locking for mutable aggregates.

Example:

```java
@Version
private Long version;
```

Suitable for:

- orders
- customers
- approvals

---

# 24. Optimistic Lock Tests

Integration tests must validate:

- successful update
- concurrent update rejection
- version increment
- HTTP conflict mapping

---

# 25. Pessimistic Locking

Use only when business requires exclusive access.

Examples:

- inventory allocation
- financial settlement

Document every pessimistic lock.

---

# 26. Deadlock Prevention

Acquire locks in consistent order.

Avoid:

```text
Order A

↓

Inventory

```

and elsewhere

```text
Inventory

↓

Order A
```

Consistent ordering reduces deadlocks.

---

# 27. Lazy Loading

Default:

```java
FetchType.LAZY
```

Use eager loading only when justified.

---

# 28. N+1 Prevention

Prevent N+1 using:

- fetch joins
- projections
- entity graphs
- batch loading

Never solve N+1 by switching everything to EAGER.

---

# 29. Entity Graphs

Prefer EntityGraph when different read scenarios require different fetch plans.

This keeps repositories explicit.

---

# 30. Read Models

Read-heavy endpoints should use projections.

Avoid loading complete aggregates for search screens.

Example:

```java
OrderSummaryProjection
```

---

# 31. DTO Queries

For search APIs prefer:

```text
SQL

↓

Projection

↓

Response
```

instead of:

```text
Entity

↓

Mapper

↓

DTO
```

when aggregate behavior is unnecessary.

---

# 32. Pagination

Always paginate potentially large datasets.

Never expose:

```java
List<Order> findAll();
```

for production APIs.

---

# 33. Sorting

Whitelist sortable fields.

Never concatenate client-provided field names into JPQL.

---

# 34. JPQL

Prefer JPQL for domain-oriented queries.

Native SQL only when:

- PostgreSQL feature required
- performance proven
- recursive queries
- window functions
- JSONB operators

---

# 35. Native SQL

Native SQL must be documented.

Explain:

- why JPQL is insufficient
- expected performance benefit
- PostgreSQL dependency

---

# 36. PostgreSQL Features

The platform may use:

- JSONB
- window functions
- CTEs
- generated columns
- partial indexes
- GIN indexes
- BRIN indexes
- SKIP LOCKED

when they provide measurable value.

---

# 37. JSONB

Use JSONB only for:

- flexible metadata
- external payload snapshots
- evolving schemas
- audit payloads

Do not replace relational modeling.

---

# 38. JSONB Queries

Create GIN indexes when JSONB fields are queried frequently.

Example:

```sql
CREATE INDEX idx_order_metadata
ON order_header
USING GIN(metadata);
```

---

# 39. Audit Columns

Standard columns:

```sql
created_at

created_by

updated_at

updated_by
```

Optional:

```sql
version
```

---

# 40. Soft Delete

Soft delete should be used only when business requires logical retention.

Columns:

```sql
deleted

deleted_at

deleted_by
```

Avoid mixing physical and logical deletion for the same entity.

---

# 41. Physical Delete

Use physical deletion when data has no business retention requirement.

Document retention policy.

---

# 42. Flyway

Flyway is the standard migration tool.

Migration pattern:

```text
V1__create_schema.sql

V2__create_order_tables.sql
```

---

# 43. Immutable Migrations

Applied migrations must never be modified.

Corrections require:

```text
V43__fix_order_index.sql
```

This rule is mandatory.

---

# 44. Repeatable Migrations

Repeatable migrations:

```text
R__create_reporting_views.sql
```

Use only for:

- views
- functions
- procedures

---

# 45. Migration Design

Each migration should:

- solve one concern
- be reversible where practical
- be idempotent within Flyway semantics
- include indexes
- include constraints

---

# 46. Large Migrations

Large data migrations should be split.

Avoid:

- locking entire tables
- long transactions
- hours-long deployments

---

# 47. Zero-Downtime

Schema evolution should follow:

Expand

↓

Deploy

↓

Migrate

↓

Contract

Never break running applications during deployment.

---

# 48. Expand-Contract Example

Phase 1:

```sql
ADD COLUMN new_status
```

Phase 2:

Application writes both columns.

Phase 3:

Backfill.

Phase 4:

Application reads new column.

Phase 5:

Drop old column.

---

# 49. Batch Updates

Large updates should be processed in batches.

Avoid:

```sql
UPDATE huge_table ...
```

inside one massive transaction.

---

# 50. Repository Responsibilities

Repositories:

- persist aggregates
- execute queries
- translate persistence exceptions

Repositories must not:

- call REST
- publish Kafka
- send emails
- execute business workflows

---

# 51. Persistence Exceptions

Translate:

```text
HibernateException

↓

OrderPersistenceException
```

Do not leak Hibernate exceptions.

---

# 52. SQL Logging

Disable SQL logging in production.

Enable only temporarily for diagnostics.

---

# 53. Performance Monitoring

Monitor:

- slow queries
- lock waits
- deadlocks
- index usage
- sequential scans
- transaction duration

---

# 54. Explain Plans

Every critical query should be analyzed using:

```sql
EXPLAIN ANALYZE
```

Do not optimize based on assumptions.

---

# 55. Connection Pool

Use HikariCP.

Tune:

- maximum pool size
- minimum idle
- timeout
- lifetime

Configuration must be environment-specific.

---

# 56. Batch Inserts

Enable JDBC batching for large insert operations.

Batch size should be validated through benchmarks.

---

# 57. Fetch Size

Large reads should configure fetch size when supported.

Avoid loading huge result sets into memory.

---

# 58. Streaming Queries

Use streaming only for:

- exports
- reports
- ETL
- batch processing

Close streams properly.

---

# 59. Large Objects

Store large binary files outside PostgreSQL whenever practical.

Persist only:

- metadata
- checksum
- storage reference

---

# 60. Timezones

Store timestamps in UTC.

Convert to user timezone only at presentation.

---

# 61. Database Testing

Repository behavior must be validated using PostgreSQL Testcontainers.

Do not rely exclusively on H2.

---

# 62. Migration Testing

CI must validate:

- clean installation
- upgrade path
- schema compatibility
- Flyway history

---

# 63. Architecture Rules

Persistence must:

- remain isolated
- preserve aggregate boundaries
- avoid leaking infrastructure
- optimize based on evidence
- keep migrations immutable
- maintain referential integrity
- support observability
- remain vendor-aware without contaminating the domain

---

# 64. Anti-Patterns

Avoid:

- exposing JPA entities
- EAGER everywhere
- SELECT *
- repository business logic
- modified Flyway migrations
- missing indexes
- N+1 queries
- huge transactions
- random native SQL
- soft delete without justification
- database triggers implementing business workflows

---

# 65. Decision Summary

The platform adopts:

- PostgreSQL
- Spring Data JPA
- Hibernate
- UUID primary keys
- aggregate-oriented repositories
- immutable Flyway migrations
- optimistic locking by default
- PostgreSQL Testcontainers
- projections for read models
- JSONB only when justified
- Expand-Contract schema evolution
- deterministic transaction boundaries
- performance-driven indexing
