# PostgreSQL Architecture

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | PostgreSQL Architecture |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the persistence architecture adopted by the Enterprise Order Platform.

It establishes:

- database organization
- schema strategy
- indexing
- optimistic locking
- migrations
- auditing
- partitioning
- backup
- recovery
- operational recommendations

The platform adopts PostgreSQL as the primary transactional database.

---

# 2. Goals

The persistence layer must provide:

- consistency
- durability
- scalability
- high availability
- maintainability
- operational simplicity
- strong transactional guarantees

---

# 3. Database Organization

One database per bounded context.

Example

```
orders_db

inventory_db

payment_db

shipment_db

customer_db
```

No service shares tables with another service.

---

# 4. Schema Organization

Within one service:

```
public

audit

integration

reporting
```

Recommended responsibilities

| Schema | Purpose |
|---------|---------|
| public | Business tables |
| audit | Audit information |
| integration | Outbox and integration support |
| reporting | Materialized views |

---

# 5. Aggregate Mapping

Each Aggregate Root owns its persistence.

Example

```
Order

↓

orders

↓

order_items

↓

order_status_history
```

No Aggregate spans multiple databases.

---

# 6. Primary Keys

Primary keys use UUID.

Example

```
UUID

Version 7 (preferred)

or

UUID Version 4
```

Never use sequential identifiers as business identifiers.

---

# 7. Business Identifiers

Business identifiers are independent.

Example

```
OrderId

(UUID)

+

OrderNumber

(Business Identifier)
```

---

# 8. Optimistic Locking

Every Aggregate Root contains:

```
version
```

Example

```java
@Version

Long version;
```

Concurrent modifications fail immediately.

---

# 9. Transactions

Application Services define transaction boundaries.

Repositories never manage transactions.

Transactions should remain short.

---

# 10. Table Organization

Example

```
orders

order_items

order_status_history

outbox_event

processed_event
```

Business tables remain separated from infrastructure tables.

---

# 11. Indexing Strategy

Every table should define:

Primary Key

Unique Constraints

Foreign Keys

Business Search Indexes

Examples

```
order_number

customer_id

status

created_at

correlation_id
```

Indexes must be driven by query patterns.

---

# 12. Composite Indexes

Prefer:

```
(status, created_at)

(customer_id, status)

(next_attempt_at, status)
```

Avoid unnecessary indexes.

Every index increases write cost.

---

# 13. Foreign Keys

Foreign Keys exist only inside the same bounded context.

Never create cross-service foreign keys.

Inter-service consistency is achieved through events.

---

# 14. Constraints

Use database constraints whenever possible.

Examples

- NOT NULL
- CHECK
- UNIQUE
- FK
- DEFAULT

Business rules remain in the Domain Layer.

---

# 15. JSON Columns

Use JSONB only for:

- event payloads
- flexible metadata
- audit details

Never replace relational modeling with JSON.

---

# 16. Auditing

Recommended fields

```
created_at

created_by

updated_at

updated_by
```

Deletion should be explicit.

Prefer logical deletion only when required by business rules.

---

# 17. Soft Delete

Default policy

```
No Soft Delete
```

Use explicit business statuses instead.

When regulatory requirements exist, prefer archival tables over hidden rows.

---

# 18. Partitioning

Partition only when justified.

Typical candidates

```
audit

outbox_event

processed_event

large history tables
```

Partition by:

- month
- business date
- creation date

---

# 19. Archiving

Operational tables remain small.

Archive

- completed events
- audit history
- historical reports

Use scheduled archival jobs.

---

# 20. Flyway

All schema evolution uses Flyway.

Rules

- never modify applied migrations
- always create a new migration
- version every change
- keep migrations deterministic

---

# 21. Naming Convention

Tables

```
snake_case
```

Columns

```
snake_case
```

Primary Key

```
id
```

Foreign Key

```
order_id

customer_id
```

Indexes

```
idx_orders_customer

idx_orders_status

idx_outbox_ready
```

---

# 22. Performance

Recommendations

- avoid SELECT *
- paginate queries
- use projections
- analyze execution plans
- monitor slow queries
- batch updates when appropriate

---

# 23. Backup

Backups should include:

- full backup
- incremental backup (if applicable)
- WAL archiving
- recovery validation

Backups must be tested regularly.

---

# 24. Disaster Recovery

Recovery objectives

Example

| Metric | Target |
|---------|--------|
| RPO | ≤ 5 minutes |
| RTO | ≤ 30 minutes |

Recovery procedures should be documented and rehearsed.

---

# 25. Observability

Expose metrics

- active connections
- transaction rate
- slow queries
- lock waits
- deadlocks
- replication lag
- table growth
- index usage

---

# 26. Security

Use:

- TLS
- encrypted backups
- least-privilege database roles
- credential rotation
- audit logging

Applications should never connect using superuser accounts.

---

# 27. Testing

Persistence tests should verify:

- optimistic locking
- constraints
- migrations
- indexes
- transaction rollback
- repository mappings
- Flyway compatibility

---

# 28. Architecture Rules

The persistence layer:

- stores Aggregate Roots
- isolates bounded contexts
- supports transactional consistency
- uses UUID identifiers
- uses optimistic locking
- evolves exclusively through Flyway

---

# 29. Decision Summary

The platform adopts:

- PostgreSQL
- one database per bounded context
- UUID primary keys
- optimistic locking
- Flyway migrations
- relational modeling
- JSONB for flexible metadata
- event-driven consistency
- audited persistence
- operational monitoring

---

# 30. Next Documentation Step

Next document

```
docs/infrastructure/flyway-migration-strategy.md
```

It will define:

- migration lifecycle
- versioning rules
- naming conventions
- rollback philosophy
- repeatable migrations
- data migrations
- production deployment strategy
