# ADR-005: Use PostgreSQL as the Primary Database

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-005 |
| Title | Use PostgreSQL as the Primary Database |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Data Architecture |
| Related Work Items | Initial platform architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform requires a transactional database capable of supporting complex business operations while providing strong consistency, reliability and operational maturity.

The platform stores information related to:

- orders
- order items
- approvals
- customers
- pricing
- audit records
- workflow state
- integration events
- transactional outbox
- operational metadata

The database is the system of record for critical business information.

It must provide:

- ACID transactions
- concurrency control
- predictable performance
- high availability
- mature backup and recovery
- strong tooling
- cloud compatibility
- SQL standards support
- long-term vendor stability

The selected database must integrate naturally with Java 21, Spring Boot and Kubernetes deployments.

---

# 2. Problem Statement

The platform requires a primary database that:

- guarantees transactional consistency
- supports complex relational models
- scales predictably
- integrates with enterprise tooling
- supports optimistic locking
- supports JSON when appropriate
- provides advanced indexing
- supports Flyway migrations
- supports Testcontainers
- is cloud friendly
- has a mature ecosystem
- minimizes operational risk

---

# 3. Decision Drivers

Primary decision drivers include:

1. transactional integrity
2. SQL standards compliance
3. operational maturity
4. ecosystem compatibility
5. performance
6. concurrency control
7. indexing capabilities
8. replication support
9. backup and recovery
10. Kubernetes compatibility
11. tooling ecosystem
12. Java compatibility
13. licensing
14. long-term maintainability
15. community adoption

---

# 4. Constraints

The decision considers:

- Java 21
- Spring Boot
- Hibernate
- Flyway
- Docker
- Kubernetes
- Testcontainers
- Clean Architecture
- Domain-Driven Design
- event-driven integration
- cloud deployment
- immutable migrations
- transactional outbox

---

# 5. Considered Options

## 5.1 PostgreSQL

Advantages:

- ACID compliant
- excellent SQL support
- advanced indexing
- JSONB support
- MVCC concurrency model
- mature optimizer
- large ecosystem
- excellent Spring support
- Testcontainers support
- Flyway compatibility
- open source
- cloud-native support

Disadvantages:

- operational expertise required
- tuning needed for high-load workloads
- horizontal scaling requires architectural planning

---

## 5.2 MySQL

Advantages:

- popular
- mature
- broad hosting support

Disadvantages:

- fewer advanced SQL capabilities
- weaker JSON capabilities
- fewer enterprise features for the target architecture
- less attractive for advanced analytical workloads

---

## 5.3 Microsoft SQL Server

Advantages:

- enterprise tooling
- mature optimizer
- strong BI ecosystem

Disadvantages:

- licensing cost
- vendor dependence
- unnecessary cost for the platform requirements

---

## 5.4 Oracle Database

Advantages:

- enterprise maturity
- scalability
- advanced capabilities

Disadvantages:

- licensing
- operational cost
- unnecessary complexity for this platform
- stronger vendor lock-in

---

## 5.5 NoSQL Database

Examples:

- MongoDB
- Cassandra
- DynamoDB

Advantages:

- flexible schema
- horizontal scaling

Disadvantages:

- weaker transactional consistency
- unsuitable for aggregate consistency
- relational queries become harder
- transactional outbox becomes more complex

---

# 6. Decision

The AstraForge Supply Platform adopts PostgreSQL as its primary transactional database.

PostgreSQL will be used for:

- aggregate persistence
- transactional outbox
- workflow state
- audit metadata
- configuration data
- business reporting support
- optimistic locking
- transactional consistency

---

# 7. Rationale

PostgreSQL provides the best balance of:

- transactional consistency
- SQL maturity
- ecosystem compatibility
- operational reliability
- cloud readiness
- Java integration
- modern database capabilities

Its MVCC implementation supports concurrent workloads without excessive locking while maintaining strong consistency guarantees.

The platform benefits from PostgreSQL features such as:

- JSONB
- partial indexes
- expression indexes
- window functions
- common table expressions
- advanced execution planner
- strong constraint enforcement

---

# 8. Data Ownership

Each bounded context owns its own schema and tables.

Examples:

```text
Orders Context
    orders
    order_items
    outbox_events

Customers Context
    customers

Inventory Context
    inventory_snapshot
```

Cross-context ownership through shared tables is prohibited.

---

# 9. Transaction Model

Transactions must remain short.

Transactions should include:

- aggregate loading
- domain behavior
- persistence
- outbox persistence

Transactions must avoid:

- HTTP calls
- SQS publication
- user interaction
- long-running processing
- report generation

---

# 10. Isolation Level

The default isolation level is:

```text
READ COMMITTED
```

Higher isolation levels require explicit justification.

Serializable transactions should be reserved for exceptional business requirements.

---

# 11. Concurrency

Concurrency is controlled through:

- optimistic locking
- transactional consistency
- aggregate boundaries

Application code must not depend on database locking as the primary business coordination mechanism.

---

# 12. Optimistic Locking

Aggregates requiring concurrent updates must use optimistic locking.

Example:

```java
@Version
private Long version;
```

Conflicts should be translated into business-friendly exceptions.

---

# 13. Schema Design

Schema design follows DDD ownership.

Rules include:

- normalized transactional model
- explicit foreign keys where appropriate
- immutable identifiers
- meaningful constraints
- consistent naming
- explicit indexes

---

# 14. Primary Keys

UUIDs are the standard identifier.

Advantages:

- distributed generation
- reduced coordination
- service independence

Business identifiers remain separate when required.

---

# 15. Foreign Keys

Foreign keys should enforce integrity inside a bounded context.

Foreign keys between bounded contexts should be avoided when ownership is separated.

Integration occurs through APIs or events rather than shared relational coupling.

---

# 16. Indexing Strategy

Indexes should support:

- aggregate lookup
- business search
- workflow queries
- outbox dispatch
- reporting projections

Every index should have measurable value.

Unused indexes increase write cost.

---

# 17. JSON Usage

JSONB may be used for:

- flexible metadata
- integration payload snapshots
- configuration documents

Business-critical relational data should remain relational.

JSON must not replace proper aggregate modeling.

---

# 18. Constraints

Use database constraints for:

- uniqueness
- referential integrity
- mandatory values
- check constraints where appropriate

Business rules remain primarily enforced by the Domain layer.

Database constraints provide defense in depth.

---

# 19. Flyway

Database evolution is managed exclusively through Flyway.

Mandatory rule:

> Applied migrations are immutable.

Every change requires a new migration version.

---

# 20. Backup and Recovery

The production environment must define:

- automated backups
- retention
- recovery testing
- point-in-time recovery where supported
- disaster recovery procedures

Recovery procedures must be tested periodically.

---

# 21. Performance

Performance should be validated through:

- execution plans
- index analysis
- slow-query review
- connection-pool metrics
- lock analysis
- vacuum monitoring

Optimization must be evidence-based.

---

# 22. Observability

Database observability should include:

- connection usage
- active sessions
- transaction duration
- lock waits
- slow queries
- replication status
- storage growth
- deadlocks
- vacuum statistics

---

# 23. Security

The database must enforce:

- least-privilege users
- encrypted connections
- credential rotation
- audit where required
- restricted administrative access

Applications must not connect using administrative accounts.

---

# 24. Test Strategy

Integration tests must use PostgreSQL through Testcontainers.

H2 or other in-memory databases must not replace PostgreSQL for persistence validation.

This ensures production-compatible SQL behavior.

---

# 25. Positive Consequences

The decision provides:

- strong transactional guarantees
- mature SQL engine
- modern indexing
- reliable concurrency
- excellent ecosystem support
- cloud readiness
- strong Java integration
- operational maturity
- scalable architecture
- predictable behavior

---

# 26. Negative Consequences

The decision introduces:

- operational management
- backup responsibility
- tuning requirements
- migration governance
- database monitoring
- storage maintenance

These costs are acceptable given the platform's requirements.

---

# 27. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Poor indexing | High | Medium | Execution-plan review |
| Long transactions | High | Medium | Keep application transactions short |
| Lock contention | Medium | Low | Prefer optimistic locking |
| Schema drift | High | Low | Immutable Flyway migrations |
| Connection exhaustion | High | Medium | Proper pool sizing |
| Slow queries | Medium | Medium | Continuous monitoring |
| Storage growth | Medium | Medium | Capacity planning |

---

# 28. Implementation Guidance

Mandatory rules:

1. PostgreSQL is the system of record.
2. Flyway manages all schema changes.
3. Applied migrations are immutable.
4. Optimistic locking protects concurrent updates.
5. Transactions remain short.
6. Cross-context joins are avoided.
7. JSONB complements relational modeling rather than replacing it.
8. Integration tests use PostgreSQL.
9. Performance tuning is evidence-based.
10. Database security follows least privilege.

---

# 29. Validation

The decision will be validated through:

- Flyway migration execution
- integration tests
- Testcontainers
- performance benchmarks
- execution-plan analysis
- backup recovery testing
- production monitoring
- schema review
- security review

---

# 30. Success Criteria

The decision is successful when:

- all services use PostgreSQL
- Flyway manages schema evolution
- integration tests run against PostgreSQL
- optimistic locking prevents conflicting updates
- backups are validated
- performance targets are met
- database growth remains predictable
- operational monitoring is available

---

# 31. Alternatives Rejected

## MySQL

Rejected because PostgreSQL provides stronger SQL capabilities and better support for the platform's long-term architectural requirements.

## SQL Server

Rejected because licensing costs provide no proportional benefit.

## Oracle Database

Rejected because enterprise capabilities exceed platform requirements while introducing unnecessary operational and licensing costs.

## NoSQL

Rejected because aggregate consistency and transactional guarantees are core requirements.

---

# 32. Related Decisions

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt Transactional Outbox
- ADR-013: Use Testcontainers for Integration Testing

---

# 33. References

- PostgreSQL Documentation
- PostgreSQL Performance Tuning Guide
- PostgreSQL MVCC Documentation
- Spring Data JPA Documentation
- Hibernate Documentation
- Flyway Documentation
- Testcontainers PostgreSQL Module
- AstraForge Supply Platform Persistence Guidelines

---

# 34. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved | Initial database architecture baseline |

---

# 35. Decision Summary

The AstraForge Supply Platform adopts PostgreSQL as its primary transactional database.

PostgreSQL provides:

- ACID-compliant transactions
- MVCC concurrency
- advanced SQL capabilities
- JSONB support
- strong indexing
- mature tooling
- excellent integration with Spring Boot, Flyway and Testcontainers

The database serves as the system of record for all transactional business data while remaining aligned with Clean Architecture, Domain-Driven Design and the platform's operational standards.
