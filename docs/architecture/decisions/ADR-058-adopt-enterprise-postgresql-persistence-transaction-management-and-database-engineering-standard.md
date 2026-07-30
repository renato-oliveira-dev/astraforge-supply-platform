# ADR-058: Adopt Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-058 |
| Title | Adopt Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | PostgreSQL, JPA, Hibernate, Transactions, Flyway, Persistence Engineering |
| Related Work Items | Persistence, Transactions, Database Performance, Schema Evolution, Flyway |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

PostgreSQL is a critical persistence boundary for enterprise services.

A persistence layer must provide more than CRUD.

It must preserve:

```text
Business Consistency

Transaction Atomicity

Concurrency Safety

Data Integrity

Performance

Schema Compatibility

Operational Recoverability
```

A database defect can manifest as:

```text
Lost Update

Duplicate Business Data

Deadlock

Lock Contention

Connection Exhaustion

N+1 Queries

Slow SQL

Migration Failure

Deployment Failure

Data Corruption
```

Database engineering is therefore an architectural discipline.

---

# 2. Problem Statement

The organization requires standards covering:

- PostgreSQL
- JPA
- Hibernate
- entities
- aggregates
- repositories
- transaction boundaries
- propagation
- isolation
- optimistic locking
- pessimistic locking
- deadlocks
- indexes
- constraints
- query plans
- N+1
- projections
- pagination
- batching
- UUID
- BigDecimal
- JSONB
- auditing
- soft delete
- connection pooling
- Flyway
- schema evolution
- zero-downtime migration
- rollback strategy
- Testcontainers

---

# 3. Decision Drivers

Primary drivers are:

1. data integrity
2. transactional correctness
3. concurrency safety
4. predictable performance
5. maintainability
6. safe deployment
7. schema compatibility
8. observability
9. recoverability
10. testability

---

# 4. Decision

PostgreSQL persistence MUST be designed around explicit transaction, aggregate, concurrency and schema-evolution boundaries.

The canonical model is:

```text
HTTP / EVENT
      |
      v
APPLICATION SERVICE
      |
      v
TRANSACTION BOUNDARY
      |
      v
DOMAIN / AGGREGATE
      |
      v
REPOSITORY
      |
      v
JPA / HIBERNATE
      |
      v
POSTGRESQL
```

---

# 5. Fundamental Principle

The governing principle is:

```text
The database is the final guardian
of persistent data integrity.

Application validation complements
database constraints.

It does not replace them.
```

---

# 6. Persistence Model

Persistence models MUST reflect deliberate data ownership.

---

# 7. Aggregate Boundary

DDD aggregate boundaries SHOULD influence transactional persistence where domain complexity justifies aggregates.

---

# 8. Aggregate Root

External application operations SHOULD normally manipulate an aggregate through its root.

---

# 9. Aggregate Size

Aggregates SHOULD remain as small as business invariants permit.

---

# 10. Large Aggregate

Large aggregates increase:

```text
Loading Cost

Lock Scope

Transaction Duration

Contention

Memory Usage
```

---

# 11. Entity Exposure

JPA entities MUST NOT be exposed directly through public REST APIs.

---

# 12. Persistence Coupling

External API contracts MUST remain independent from persistence implementation.

---

# 13. Entity Responsibility

Entities SHOULD model persistent state and relevant domain behavior without accumulating unrelated application concerns.

---

# 14. Entity Constructor

Entities SHOULD preserve valid initialization states where practical.

---

# 15. Setter Proliferation

Unrestricted setters SHOULD be avoided when they allow invalid domain transitions.

---

# 16. Identifier

Identifiers MUST have explicit generation and lifecycle semantics.

---

# 17. UUID

UUID SHOULD be used when distributed identifier generation is valuable.

---

# 18. UUID Generation

Identifier generation SHOULD occur at a deliberate layer.

Do not scatter:

```java
UUID.randomUUID()
```

through business code where deterministic identity or testability matters.

---

# 19. PostgreSQL UUID

Native PostgreSQL:

```text
uuid
```

SHOULD be preferred over textual UUID storage.

---

# 20. UUID String Storage

Avoid:

```text
varchar(36)
```

for UUIDs unless interoperability requirements explicitly justify it.

---

# 21. Sequential IDs

Database-generated numeric identifiers MAY remain appropriate for internal/high-volume relational structures.

---

# 22. Business Identifier

Technical primary key and business identifier MAY be different concepts.

---

# 23. Money

Financial values MUST use decimal-safe storage.

---

# 24. Java Money Type

Use:

```java
BigDecimal
```

for decimal monetary calculations.

---

# 25. PostgreSQL Money Type

Prefer:

```text
numeric(p,s)
```

with explicit precision and scale.

---

# 26. Floating Point

Do not use:

```text
float

double

real

double precision
```

for exact monetary values.

---

# 27. Monetary Rounding

Rounding mode MUST be explicit when calculations require rounding.

---

# 28. String Length

Text columns SHOULD have limits where the domain has meaningful maximum size.

---

# 29. varchar vs text

PostgreSQL `text` is acceptable when no meaningful business length restriction exists.

Length restrictions SHOULD represent business rules rather than arbitrary database habits.

---

# 30. Timestamp

Timestamp semantics MUST be explicit.

---

# 31. Absolute Instant

For globally meaningful timestamps, prefer storage semantics capable of preserving an absolute point in time.

---

# 32. PostgreSQL timestamptz

PostgreSQL:

```text
timestamp with time zone
```

SHOULD normally be used for absolute timestamps.

---

# 33. Local Date

Business date-only values SHOULD use:

```text
date
```

---

# 34. Java Time

Java persistence SHOULD use appropriate `java.time` types.

Examples:

```text
LocalDate

OffsetDateTime

Instant
```

according to semantics.

---

# 35. Boolean

Boolean state SHOULD use native:

```text
boolean
```

rather than arbitrary character conventions unless integrating with legacy schemas.

---

# 36. JSONB

PostgreSQL JSONB MAY be used when data is legitimately semi-structured.

---

# 37. JSONB Is Not Default Modeling

JSONB MUST NOT replace normal relational modeling merely to avoid schema design.

---

# 38. Good JSONB Cases

Examples:

```text
External Provider Metadata

Flexible Configuration

Historical Event Payload

Extensible Attributes
```

---

# 39. Poor JSONB Cases

Avoid JSONB for heavily queried relational fields that require:

```text
Foreign Keys

Joins

Strong Constraints

Frequent Individual Updates
```

unless justified.

---

# 40. JSONB Index

Frequently queried JSONB paths SHOULD be evaluated for appropriate indexing.

---

# 41. Constraints

Database constraints MUST enforce critical persistent invariants where possible.

---

# 42. NOT NULL

Required persistent fields SHOULD use:

```text
NOT NULL
```

where compatible with deployment strategy.

---

# 43. UNIQUE

Business uniqueness SHOULD be protected with:

```text
UNIQUE
```

constraints/indexes when appropriate.

---

# 44. Foreign Key

Relational integrity SHOULD use foreign keys when the data model and service ownership allow it.

---

# 45. Cross-Service Foreign Key

Foreign keys MUST NOT couple independently owned service databases.

---

# 46. CHECK Constraint

Stable database-level invariants MAY use:

```text
CHECK
```

constraints.

---

# 47. Application Validation

Application validation provides:

```text
Better User Feedback

Earlier Failure

Business Context
```

---

# 48. Database Constraint

Database constraints provide:

```text
Final Integrity Protection

Concurrency Safety

Protection Against Alternative Writers
```

---

# 49. Duplicate Check Anti-Pattern

This alone is insufficient:

```text
SELECT COUNT(*)
      |
      v
IF ZERO
      |
      v
INSERT
```

because concurrent transactions can race.

---

# 50. Correct Uniqueness

Use:

```text
Application Validation
        +
Database UNIQUE Constraint
```

---

# 51. Repository Pattern

Repositories SHOULD express persistence operations meaningful to the application/domain.

---

# 52. Repository Leakage

Application services SHOULD NOT depend unnecessarily on Hibernate implementation details.

---

# 53. Spring Data

Spring Data JPA MAY be used for standard repository operations.

---

# 54. Complex Query

Complex/high-performance queries MAY use:

```text
JPQL

Criteria

Specifications

Native SQL

JdbcTemplate
```

when justified.

---

# 55. Tool Selection

The simplest mechanism that produces maintainable and efficient SQL SHOULD be preferred.

---

# 56. Native SQL

Native SQL is acceptable when PostgreSQL-specific capabilities or performance requirements justify it.

---

# 57. Native SQL Documentation

Non-trivial native SQL SHOULD explain why ORM-level querying is insufficient.

---

# 58. Transaction Boundary

Transaction boundaries SHOULD normally reside in the application/service layer.

---

# 59. @Transactional

`@Transactional` SHOULD represent a meaningful business/application unit of work.

---

# 60. Controller Transaction

Controllers SHOULD NOT normally own database transactions.

---

# 61. Repository Transaction

Repositories SHOULD NOT independently define business transaction boundaries unless a specific persistence operation requires it.

---

# 62. Transaction Scope

Transactions SHOULD remain as short as practical.

---

# 63. Long Transaction

Long transactions increase:

```text
Lock Duration

Connection Occupancy

MVCC Bloat

Deadlock Probability

Failure Cost
```

---

# 64. Remote Call in Transaction

Avoid:

```text
BEGIN TRANSACTION

UPDATE DATABASE

CALL REMOTE HTTP API

WAIT

COMMIT
```

unless explicitly required.

---

# 65. Preferred Pattern

Prefer separating:

```text
DATABASE TRANSACTION
        |
        v
COMMIT
        |
        v
ASYNC / CONTROLLED INTEGRATION
```

when business semantics permit.

---

# 66. Transactional Outbox

When database state and event publication must be coordinated, use ADR-057 Transactional Outbox.

---

# 67. Transaction Propagation

Spring transaction propagation MUST be chosen deliberately.

---

# 68. REQUIRED

Default:

```text
Propagation.REQUIRED
```

is appropriate for most business transactions.

---

# 69. REQUIRES_NEW

`REQUIRES_NEW` MUST be used carefully because it creates an independent transaction.

---

# 70. REQUIRES_NEW Risk

It can create behavior such as:

```text
Outer Transaction Fails

Inner REQUIRES_NEW Commits
```

which may violate expected atomicity.

---

# 71. Self Invocation

Spring proxy-based transaction behavior MUST be understood.

Calling a transactional method through:

```text
this.someTransactionalMethod()
```

does not necessarily activate proxy interception.

---

# 72. Checked Exceptions

Rollback semantics for checked exceptions MUST be understood and explicitly configured where needed.

---

# 73. Exception Suppression

A transaction MUST NOT silently commit because a persistence exception was caught and ignored.

---

# 74. Read-Only Transaction

Read operations MAY use:

```java
@Transactional(readOnly = true)
```

where useful.

---

# 75. Read-Only Is Not Security

`readOnly=true` is an optimization/semantic hint, not a security boundary.

---

# 76. Isolation Level

Transaction isolation MUST reflect actual concurrency requirements.

---

# 77. PostgreSQL Default

PostgreSQL commonly uses:

```text
READ COMMITTED
```

as the default isolation level.

---

# 78. READ COMMITTED

READ COMMITTED is appropriate for many application transactions.

---

# 79. REPEATABLE READ

REPEATABLE READ MAY be required when a transaction needs a stable snapshot.

---

# 80. SERIALIZABLE

SERIALIZABLE MAY be required for strict concurrency invariants.

---

# 81. Serializable Retry

Serialization failures MAY require bounded transaction retry.

---

# 82. Isolation Escalation

Higher isolation MUST NOT be used automatically as a substitute for understanding concurrency.

---

# 83. Optimistic Locking

Optimistic locking SHOULD be preferred when conflicts are relatively uncommon.

---

# 84. @Version

JPA optimistic locking SHOULD use:

```java
@Version
```

where applicable.

---

# 85. Lost Update

Optimistic locking helps prevent:

```text
Transaction A reads version 1

Transaction B reads version 1

A writes version 2

B overwrites A
```

---

# 86. Conflict Handling

Optimistic-lock conflicts MUST be translated into appropriate application/API semantics.

---

# 87. Blind Retry

Optimistic-lock failure MUST NOT always be blindly retried.

The business operation may require the caller to reconsider updated state.

---

# 88. Pessimistic Locking

Pessimistic locking MAY be used when concurrent access must be serialized.

---

# 89. SELECT FOR UPDATE

PostgreSQL:

```sql
SELECT ... FOR UPDATE
```

MAY be appropriate for explicit row locking.

---

# 90. Pessimistic Lock Cost

Pessimistic locks increase:

```text
Blocking

Deadlock Risk

Latency

Contention
```

---

# 91. Lock Scope

Lock the smallest practical dataset.

---

# 92. Lock Order

Code paths acquiring multiple locks SHOULD use consistent lock ordering.

---

# 93. Deadlock

Deadlocks are possible even in correctly functioning relational systems.

---

# 94. Deadlock Handling

The application SHOULD:

```text
Detect

Rollback

Classify

Retry selectively when safe
```

---

# 95. Deadlock Retry

Deadlock retry MUST be:

```text
Bounded

Idempotency-Aware

Backoff-Aware
```

---

# 96. Database Timeout

Database operations MUST have bounded timeout behavior.

---

# 97. Lock Timeout

Lock wait SHOULD be bounded where prolonged waiting would violate service objectives.

---

# 98. Statement Timeout

Critical environments SHOULD consider appropriate PostgreSQL statement timeout policies.

---

# 99. Connection Pool

Database access MUST use a bounded connection pool.

---

# 100. HikariCP

HikariCP is the preferred connection pool for Spring Boot applications unless platform standards specify otherwise.

---

# 101. Pool Configuration

Critical services SHOULD explicitly configure/understand:

```text
maximumPoolSize

minimumIdle

connectionTimeout

idleTimeout

maxLifetime
```

---

# 102. Pool Size

Pool size MUST reflect:

```text
Database Capacity

Replica Count

Workload

Transaction Duration

Concurrency
```

---

# 103. Virtual Threads

Virtual Threads MUST NOT be used to justify extremely large database connection pools.

---

# 104. Capacity Relationship

Example:

```text
5,000 Virtual Threads
        |
        v
50 DB Connections
        |
        v
PostgreSQL
```

is a valid architecture.

---

# 105. Pool Saturation

Monitor:

```text
Active

Idle

Pending

Acquisition Time

Timeouts
```

---

# 106. Connection Leak

Connection leaks MUST be treated as production defects.

---

# 107. Connection Lifetime

Connection maximum lifetime SHOULD account for infrastructure/database connection lifetime behavior.

---

# 108. Index

Indexes MUST support measured access patterns.

---

# 109. Index Every Column

Indexing every column is prohibited as a generic strategy.

---

# 110. Index Cost

Every index has cost:

```text
Storage

INSERT

UPDATE

DELETE

Vacuum

Maintenance
```

---

# 111. Primary Key Index

Primary keys naturally require efficient indexed access.

---

# 112. Foreign Key Index

Frequently joined/filtering foreign-key columns SHOULD be evaluated for indexes.

PostgreSQL does not automatically create an index for every foreign key.

---

# 113. Composite Index

Composite index order MUST reflect query access patterns.

---

# 114. Leftmost Prefix

Composite index usefulness depends on leading-column access patterns.

---

# 115. Partial Index

PostgreSQL partial indexes SHOULD be considered when queries consistently target a selective subset.

Example:

```sql
CREATE INDEX ...
ON orders (...)
WHERE status = 'PENDING';
```

---

# 116. Covering Index

`INCLUDE` MAY be used when index-only access provides measured value.

---

# 117. Expression Index

Expression indexes MAY support stable expression-based predicates.

---

# 118. Duplicate Index

Redundant indexes SHOULD be removed after verifying workload impact.

---

# 119. Unused Index

Unused-index metrics SHOULD be reviewed before removal.

---

# 120. Query Plan

Performance-critical SQL MUST be evaluated using PostgreSQL execution plans.

---

# 121. EXPLAIN

Use:

```sql
EXPLAIN
```

to inspect the planned execution.

---

# 122. EXPLAIN ANALYZE

Use:

```sql
EXPLAIN ANALYZE
```

to execute and measure the query.

---

# 123. Production Safety

`EXPLAIN ANALYZE` MUST be used carefully because the statement actually executes.

---

# 124. Query Plan Analysis

Evaluate:

```text
Scan Type

Join Strategy

Estimated Rows

Actual Rows

Loops

Sort

Buffers

Execution Time
```

---

# 125. Estimate Error

Large differences between:

```text
Estimated Rows

Actual Rows
```

may indicate statistics/cardinality problems.

---

# 126. Statistics

PostgreSQL statistics MUST remain healthy for optimizer decisions.

---

# 127. ANALYZE

Database maintenance SHOULD ensure appropriate statistics collection.

---

# 128. N+1

Material N+1 behavior is a persistence defect.

---

# 129. N+1 Example

Avoid:

```text
SELECT orders

for each order:
    SELECT customer
```

---

# 130. N+1 Detection

N+1 SHOULD be identified through:

```text
SQL Logging in Controlled Environments

Integration Tests

Profiling

APM

Query Metrics
```

---

# 131. EAGER

Changing every relationship to:

```java
FetchType.EAGER
```

is not an acceptable generic N+1 solution.

---

# 132. Fetch Join

Fetch joins MAY solve specific object-graph retrieval requirements.

---

# 133. EntityGraph

JPA EntityGraph MAY be used for query-specific fetch plans.

---

# 134. Projection

Projection SHOULD be preferred when only selected fields are required.

---

# 135. List/Search Endpoint

Search/list endpoints SHOULD normally use compact DTO/projection models rather than loading complete aggregates.

---

# 136. Lazy Loading

Lazy loading SHOULD remain within deliberate persistence boundaries.

---

# 137. Open Session in View

Open Session in View SHOULD NOT be relied upon to hide uncontrolled lazy loading in REST applications.

---

# 138. OSIV

Spring Boot services SHOULD normally disable:

```text
spring.jpa.open-in-view
```

unless explicitly justified.

---

# 139. Pagination

Large queries MUST be bounded.

---

# 140. Offset Pagination

Offset pagination is acceptable for moderate datasets.

---

# 141. Large Offset

This:

```sql
OFFSET 1000000
```

can become expensive.

---

# 142. Keyset Pagination

Keyset pagination SHOULD be considered for high-volume sequential navigation.

---

# 143. Stable Ordering

Pagination MUST use deterministic ordering.

---

# 144. Tie Breaker

When sorting by a non-unique field, include a stable unique tie-breaker.

Example:

```text
created_at, id
```

---

# 145. Pagination Index

Pagination sort/filter fields SHOULD be supported by appropriate indexes when workload requires it.

---

# 146. Count Query

Expensive:

```text
COUNT(*)
```

for every search SHOULD be avoided when the consumer does not require total counts.

---

# 147. Batching

Batch operations SHOULD be used when they materially reduce round trips.

---

# 148. Hibernate Batch

Hibernate JDBC batching MAY be enabled for suitable write workloads.

---

# 149. Batch Size

Batch size MUST be bounded and measured.

---

# 150. Identity Generation

Identifier generation strategy can affect Hibernate insert batching and MUST be considered.

---

# 151. Batch Flush

Large write operations SHOULD periodically flush/clear persistence context when required to control memory.

---

# 152. Bulk Update

Bulk SQL/JPQL updates MAY be more efficient than loading thousands of entities individually.

---

# 153. Persistence Context

Bulk operations bypass normal entity state synchronization and MUST account for stale persistence-context state.

---

# 154. Delete

Physical and logical deletion semantics MUST be explicit.

---

# 155. Soft Delete

Soft delete MAY be used when business/audit requirements require retained records.

---

# 156. Soft Delete Cost

Soft delete adds complexity to:

```text
Queries

Indexes

Uniqueness

Foreign Keys

Data Retention
```

---

# 157. Soft Delete Filter

Every relevant query MUST correctly account for deleted state.

---

# 158. Partial Unique Index

Soft-delete uniqueness MAY require a PostgreSQL partial unique index.

Conceptually:

```sql
CREATE UNIQUE INDEX ...
ON customer(code)
WHERE deleted_at IS NULL;
```

---

# 159. Soft Delete Is Not Retention

Soft delete MUST NOT replace formal data-retention/purge policies.

---

# 160. Audit Columns

Persistent business entities SHOULD consider standard audit metadata.

Examples:

```text
created_at

created_by

updated_at

updated_by
```

---

# 161. Auditing

Spring Data auditing MAY be used when it provides consistent behavior.

---

# 162. Audit Trail

Simple audit columns do not replace a full immutable audit trail where regulatory/business requirements demand one.

---

# 163. Database Naming

Database objects SHOULD follow a consistent naming convention.

---

# 164. Identifier Case

PostgreSQL object names SHOULD normally use lowercase unquoted identifiers.

---

# 165. Quoted Identifier

Avoid unnecessary:

```sql
"OrderHeader"
```

style identifiers.

---

# 166. Snake Case

Database objects SHOULD normally use:

```text
snake_case
```

---

# 167. Reserved Words

Database object names MUST avoid PostgreSQL reserved words where practical.

---

# 168. Schema

Database schemas MAY provide logical organization when ownership and operational requirements justify them.

---

# 169. Database per Service

Microservices SHOULD own their persistence data.

---

# 170. Shared Database

Independent services MUST NOT casually share and directly modify the same persistence tables.

---

# 171. Cross-Service Query

A service SHOULD NOT directly query another service's private database schema as an integration mechanism.

---

# 172. Integration Boundary

Use:

```text
REST

SQS

Approved Data Integration
```

instead.

---

# 173. Flyway

Flyway is the standard schema migration mechanism for applicable PostgreSQL services.

---

# 174. Migration Immutability

A Flyway migration that has already been applied MUST NEVER be modified.

---

# 175. Schema Correction

Any database correction MUST be introduced through:

```text
A NEW migration
with
A NEW version
```

---

# 176. Example

If:

```text
V27__create_outbox_event.sql
```

has already been applied and requires correction, do NOT modify V27.

Create:

```text
V28__adjust_outbox_event.sql
```

or the next available project version.

---

# 177. Migration History

Migration history is immutable deployment history.

---

# 178. Flyway Repair

`flyway repair` MUST NOT be used as a routine mechanism to legitimize unauthorized modification of previously applied migrations.

---

# 179. Migration Naming

Migrations SHOULD use descriptive names.

Example:

```text
V31__add_order_external_status_index.sql
```

---

# 180. One Purpose

A migration SHOULD have a coherent purpose.

---

# 181. Forward Migration

Production schema evolution SHOULD primarily use forward migrations.

---

# 182. Rollback

Database rollback is not equivalent to application rollback.

---

# 183. Destructive Rollback

Automatic destructive down migrations SHOULD NOT be assumed safe.

---

# 184. Recovery Strategy

Migration recovery MAY require:

```text
Forward Fix

Application Rollback

Data Restore

Feature Disablement
```

depending on failure.

---

# 185. Zero-Downtime Migration

Production migrations SHOULD support rolling/zero-downtime deployment where platform availability requires it.

---

# 186. Expand and Contract

The preferred pattern is:

```text
EXPAND
   |
   v
DEPLOY COMPATIBLE APPLICATION
   |
   v
MIGRATE / BACKFILL DATA
   |
   v
SWITCH USAGE
   |
   v
CONTRACT
```

---

# 187. Add Column

Adding a nullable column is typically safer than immediately adding a mandatory column with incompatible semantics.

---

# 188. New Mandatory Column

Prefer:

```text
1. Add nullable column

2. Deploy code capable of old/new state

3. Backfill

4. Validate

5. Add NOT NULL in later migration
```

where required for zero downtime.

---

# 189. Rename Column

Direct column rename can break old application instances during rolling deployment.

---

# 190. Compatible Rename

Prefer:

```text
Add New Column

Dual Read/Write if required

Backfill

Migrate Consumers

Remove Old Column Later
```

---

# 191. Drop Column

Dropping a column MUST occur only after all deployed application versions no longer depend on it.

---

# 192. Type Change

Potentially incompatible type changes SHOULD use staged migration.

---

# 193. Large Table Migration

Large-table DDL MUST be evaluated for:

```text
Table Lock

Rewrite

WAL Volume

Replication Lag

Execution Time
```

---

# 194. Index Creation

Large production indexes SHOULD consider PostgreSQL:

```sql
CREATE INDEX CONCURRENTLY
```

when appropriate.

---

# 195. Concurrent Index Limitation

`CREATE INDEX CONCURRENTLY` has transactional restrictions and migration tooling implications that MUST be understood.

---

# 196. DDL Lock

Migration review MUST consider PostgreSQL lock behavior.

---

# 197. Migration Timeout

Production migrations SHOULD have controlled timeout/operational procedures rather than waiting indefinitely on locks.

---

# 198. Backfill

Large data backfills SHOULD normally be:

```text
Batched

Observable

Restartable

Idempotent
```

---

# 199. Backfill Transaction

Do not necessarily update millions of rows in one enormous transaction.

---

# 200. Migration and Application Compatibility

During rolling deployment:

```text
OLD APP
   +
NEW SCHEMA
```

and:

```text
NEW APP
   +
TRANSITIONAL SCHEMA
```

MUST both remain valid during the migration window.

---

# 201. Destructive Migration

Destructive changes require explicit review.

---

# 202. Data Loss

A migration capable of permanent data loss MUST have an approved backup/recovery strategy.

---

# 203. Migration Validation

CI SHOULD validate migrations from an empty database.

---

# 204. Upgrade Validation

Critical services SHOULD also validate migration from representative previous schema state.

---

# 205. Testcontainers

Persistence integration tests SHOULD use real PostgreSQL through Testcontainers where database semantics matter.

---

# 206. H2 Replacement

H2 MUST NOT be assumed to accurately reproduce PostgreSQL behavior.

---

# 207. PostgreSQL-Specific Features

Tests using:

```text
JSONB

UUID

Partial Index

Native SQL

Locking

PostgreSQL Functions
```

SHOULD execute against PostgreSQL.

---

# 208. Repository Integration Test

Repository tests SHOULD verify:

```text
Mapping

Constraints

Queries

Pagination

Sorting

Locking
```

as applicable.

---

# 209. Migration Integration Test

Application startup tests SHOULD verify Flyway migration success against PostgreSQL.

---

# 210. Constraint Test

Critical database constraints SHOULD have integration tests.

---

# 211. Concurrency Test

Critical concurrency invariants SHOULD be tested with real concurrent transactions.

---

# 212. Optimistic Lock Test

Tests SHOULD demonstrate that concurrent stale updates cannot silently overwrite newer state.

---

# 213. Query Performance Test

Performance-critical repository methods SHOULD have query-plan/performance validation where justified.

---

# 214. Production Observability

Database behavior MUST be observable.

---

# 215. Application Metrics

Monitor:

```text
Connection Pool

Query Latency

Transaction Latency

Errors

Timeouts
```

---

# 216. PostgreSQL Metrics

Operational monitoring SHOULD include applicable:

```text
Connections

Locks

Deadlocks

Long Transactions

Cache Hit Ratio

Replication Lag

Table/Index Size

Vacuum Activity
```

---

# 217. pg_stat_statements

`pg_stat_statements` SHOULD be used where platform policy permits it for query-performance analysis.

---

# 218. Slow Queries

Slow-query investigation SHOULD prioritize cumulative workload impact rather than only the single slowest query.

---

# 219. Query Fingerprint

Normalized query fingerprints SHOULD be used for aggregate analysis where tooling supports them.

---

# 220. Transaction Observability

Long-running transactions SHOULD be detectable operationally.

---

# 221. Deadlock Monitoring

Deadlocks MUST generate useful operational signals.

---

# 222. Lock Monitoring

Persistent lock waits SHOULD be observable.

---

# 223. Database Logging

Application SQL logging MUST be controlled.

---

# 224. Production SQL Logging

Full SQL/bind-value logging MUST NOT be indiscriminately enabled in production.

---

# 225. Sensitive Parameters

SQL diagnostics MUST NOT expose sensitive data.

---

# 226. Query Comments

Query tagging/comments MAY be used to associate SQL with application operations when supported safely.

---

# 227. Database Security

Applications MUST use least-privilege database credentials.

---

# 228. Application User

Application database users SHOULD NOT normally own unrestricted administrative privileges.

---

# 229. Migration User

Migration credentials MAY have broader DDL privileges than runtime application credentials.

---

# 230. Credential Separation

Runtime and migration credentials SHOULD be separated where platform security supports it.

---

# 231. Public Schema

Unnecessary privileges on PostgreSQL schemas MUST be removed according to security standards.

---

# 232. Secrets

Database passwords MUST NOT be committed to source control.

---

# 233. Encryption

Database transport SHOULD use approved encryption.

---

# 234. Backup

Critical databases MUST have tested backup procedures.

---

# 235. Restore

A backup strategy is incomplete until restore is tested.

---

# 236. RPO and RTO

Critical persistence systems MUST have defined:

```text
RPO

RTO
```

---

# 237. PITR

Point-in-Time Recovery SHOULD be available where business recovery requirements demand it.

---

# 238. High Availability

Production PostgreSQL topology SHOULD meet platform availability requirements.

---

# 239. Read Replica

Read replicas MAY be used for appropriate read workloads.

---

# 240. Replica Lag

Applications using replicas MUST account for replication lag.

---

# 241. Read-After-Write

Operations requiring immediate read-after-write consistency SHOULD NOT blindly read from an asynchronous replica.

---

# 242. Database Capacity

Capacity planning MUST consider:

```text
CPU

Memory

Storage

IOPS

Connections

Data Growth

Index Growth

WAL

Replication
```

---

# 243. Table Growth

High-growth tables MUST have retention/archive strategy.

---

# 244. Outbox Growth

Outbox tables MUST have cleanup/retention strategy after successful publication.

---

# 245. Audit Growth

Audit tables MUST have lifecycle management.

---

# 246. Vacuum

PostgreSQL MVCC requires appropriate vacuum/autovacuum behavior.

---

# 247. Autovacuum

Autovacuum MUST NOT be disabled globally as a generic performance optimization.

---

# 248. Bloat

Table/index bloat SHOULD be monitored for high-churn workloads.

---

# 249. Long Transaction and Vacuum

Long-running transactions can prevent cleanup of old row versions and MUST be controlled.

---

# 250. Data Archiving

Historical data SHOULD be archived when operational tables no longer require immediate access.

---

# 251. Partitioned Tables

PostgreSQL table partitioning MAY be used for sufficiently large datasets with clear access/retention patterns.

---

# 252. Partitioning Is Not Default

Table partitioning introduces complexity and MUST be justified by measured need.

---

# 253. Partition Key

Partition key selection MUST align with:

```text
Query Pattern

Retention

Data Distribution
```

---

# 254. Database Review Checklist

A material persistence change SHOULD evaluate:

```text
[ ] Is the aggregate boundary correct?

[ ] Is transaction scope minimal?

[ ] Are remote calls outside the transaction where possible?

[ ] Are database constraints present?

[ ] Is uniqueness concurrency-safe?

[ ] Is optimistic locking required?

[ ] Is pessimistic locking required?

[ ] Can deadlocks occur?

[ ] Are queries bounded?

[ ] Is there N+1 risk?

[ ] Are projections appropriate?

[ ] Are indexes aligned with queries?

[ ] Has EXPLAIN been evaluated?

[ ] Is connection-pool demand understood?

[ ] Is schema evolution backward compatible?

[ ] Is this a new Flyway migration?

[ ] Does the migration support rolling deployment?

[ ] Is a backfill required?

[ ] Could DDL lock a large table?

[ ] Are PostgreSQL integration tests present?
```

---

# 255. Database Fitness Functions

Stable invariants SHOULD be automated where practical.

Examples:

```text
[ ] Flyway validates successfully

[ ] Previously applied migration files remain unchanged

[ ] New schema changes use new migration versions

[ ] spring.jpa.open-in-view is disabled

[ ] No JPA entities are exposed through controllers

[ ] Repository pagination is bounded

[ ] Critical entities use optimistic locking where required

[ ] Critical uniqueness has DB constraints

[ ] Testcontainers PostgreSQL integration tests pass

[ ] No unbounded database executor exists

[ ] Connection-pool limits are explicit
```

---

# 256. Enterprise Database Gate

A persistence change is not considered compliant when applicable conditions include:

```text
[ ] Applied Flyway migration modified

[ ] Schema changed without migration

[ ] Public API exposes JPA entity

[ ] Critical uniqueness exists only in Java

[ ] Money stored as floating point

[ ] Unbounded query

[ ] Known material N+1

[ ] Remote call unnecessarily held inside transaction

[ ] Connection pool unbounded/misconfigured

[ ] Lock behavior ignored

[ ] Destructive migration lacks compatibility plan

[ ] Large migration has no lock/runtime analysis

[ ] PostgreSQL-specific behavior tested only with H2

[ ] Critical concurrency behavior untested
```

---

# 257. Anti-Patterns

The following are prohibited or strongly discouraged:

- modifying an already applied Flyway migration
- using `flyway repair` to hide migration modification
- schema changes performed manually without version-controlled migration
- direct JPA entity exposure
- floating-point money
- UUID stored as text without reason
- arbitrary JSONB replacing relational design
- Java-only uniqueness enforcement
- `SELECT` then `INSERT` as sole duplicate protection
- transactions in controllers
- unnecessarily long transactions
- remote HTTP calls inside database transactions
- indiscriminate `REQUIRES_NEW`
- ignoring Spring transactional proxy semantics
- swallowing persistence exceptions
- blindly increasing isolation level
- blind optimistic-lock retry
- broad pessimistic locking
- indexing every column
- ignoring index write cost
- using EAGER as universal N+1 fix
- relying on OSIV to hide persistence problems
- loading full entities for lightweight searches
- unbounded pagination
- huge single-transaction backfills
- direct incompatible column rename during rolling deployment
- dropping columns while old application versions still depend on them
- assuming application rollback automatically rolls back schema
- testing PostgreSQL-specific behavior only with H2
- excessive production SQL logging
- application runtime credentials with unnecessary DDL/admin privileges

---

# 258. Positive Consequences

The decision provides:

- stronger data integrity
- safer concurrent updates
- clearer transaction boundaries
- fewer N+1 defects
- predictable query performance
- controlled connection usage
- safer schema evolution
- immutable migration history
- improved rolling deployments
- stronger PostgreSQL integration testing
- improved database observability
- better disaster recovery posture

---

# 259. Negative Consequences

The decision introduces:

- additional migration discipline
- database integration tests
- query-plan analysis
- explicit concurrency design
- staged schema changes
- operational database monitoring
- more careful deployment sequencing

These costs are accepted because persistent data corruption and incompatible schema deployment are among the most expensive failure classes in enterprise systems.

---

# 260. Neutral Consequences

The decision also means:

- not every query requires native SQL
- not every table requires partitioning
- not every entity requires pessimistic locking
- not every transaction requires SERIALIZABLE
- not every field requires an index
- JSONB remains valid for appropriate workloads
- some zero-downtime migrations require multiple releases
- application and database deployments may need coordinated sequencing

---

# 261. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Data corruption | Critical | Low/Medium | DB constraints + transactions |
| Lost update | High | Medium | Optimistic locking |
| Deadlock | High | Medium | Lock discipline + bounded retry |
| N+1 | High | High | Query/fetch testing |
| Pool exhaustion | Critical | Medium | Bounded HikariCP |
| Slow query | High | Medium | EXPLAIN + metrics |
| Migration outage | Critical | Medium | Expand/contract |
| Flyway checksum failure | High | Medium | Immutable migrations |
| Data loss | Critical | Low | Backup + staged migration |
| Large-table lock | Critical | Medium | DDL analysis |
| Replica inconsistency | High | Medium | Read consistency policy |
| Database bloat | High | Medium | Vacuum + lifecycle management |

---

# 262. Implementation Guidance

The following rules are mandatory:

1. PostgreSQL is the final enforcement boundary for critical persistent integrity.
2. JPA entities must not be exposed directly through REST APIs.
3. Persistence identifiers must use deliberate generation strategies.
4. Native PostgreSQL UUID should be preferred for UUID values.
5. Financial values must use BigDecimal/numeric semantics.
6. Timestamp semantics must be explicit.
7. JSONB must be used only for justified semi-structured data.
8. Critical persistent invariants must use database constraints where possible.
9. Business uniqueness must not rely solely on pre-insert Java queries.
10. Transactions should be defined around meaningful application units of work.
11. Transactions must remain short.
12. Remote calls should remain outside database transactions where business semantics permit.
13. Transaction propagation must be deliberate.
14. Optimistic locking should protect applicable concurrent update scenarios.
15. Pessimistic locking must be used selectively.
16. Deadlock retry must be bounded and safe.
17. Database and lock waits must be bounded.
18. Connection pools must have explicit limits.
19. Virtual Threads must not cause uncontrolled database concurrency.
20. Indexes must correspond to measured access patterns.
21. Performance-critical SQL must be evaluated using PostgreSQL query plans where required.
22. Material N+1 behavior must be corrected.
23. EAGER must not be used as a generic N+1 solution.
24. OSIV should normally be disabled for REST services.
25. Large queries must use bounded pagination.
26. High-volume sequential pagination should consider keyset pagination.
27. Batch operations must remain bounded.
28. Soft delete requires explicit query, uniqueness and retention semantics.
29. Database object naming must remain consistent.
30. Independently owned services must not directly manipulate each other's databases.
31. Flyway must be used for version-controlled schema evolution.
32. An already applied Flyway migration must never be altered.
33. Every database correction must use a new migration with a new version.
34. `flyway repair` must not be used to normalize unauthorized migration modifications.
35. Production schema evolution should use forward-compatible expand/contract patterns.
36. Destructive changes must occur only after consumers no longer depend on old schema.
37. Large backfills must be bounded, observable and restartable.
38. Large-table DDL must receive lock/runtime analysis.
39. PostgreSQL-specific behavior should be tested using PostgreSQL Testcontainers.
40. Critical concurrency and database constraints must have integration tests.
41. Database pool, locks, transactions and query performance must be observable.
42. Runtime database credentials must follow least privilege.
43. Critical databases must have tested backup and restore procedures.
44. Data growth, vacuum, retention and capacity must be actively managed.

---

# 263. Validation

This ADR will be validated through:

- PostgreSQL
- Spring Data JPA
- Hibernate
- HikariCP
- Flyway
- Testcontainers PostgreSQL
- JUnit 5
- AssertJ
- integration tests
- concurrency tests
- migration tests
- EXPLAIN
- EXPLAIN ANALYZE
- pg_stat_statements
- database metrics
- SonarQube
- SAST
- CI/CD architecture gates

---

# 264. Success Criteria

The decision is successful when:

- applied migrations remain immutable
- database changes deploy without schema drift
- rolling deployments remain backward compatible
- duplicate business records decrease
- lost updates are prevented
- transaction duration remains controlled
- connection-pool exhaustion decreases
- N+1 regressions are detected before production
- query performance is measurable
- destructive schema changes follow staged migration
- PostgreSQL-specific behavior is tested against PostgreSQL
- database recovery procedures are proven
- persistence incidents become easier to diagnose

---

# 265. Alternatives Rejected

## 265.1 Application-Only Integrity

Rejected because concurrent and alternative writers can bypass application checks.

---

## 265.2 Modify Existing Flyway Migration

Rejected because applied migration history is immutable.

---

## 265.3 H2 as PostgreSQL Equivalent

Rejected because database-specific behavior differs materially.

---

## 265.4 EAGER Loading Everywhere

Rejected because it can replace N+1 with over-fetching and large joins.

---

## 265.5 Maximum Database Connection Pool

Rejected because excessive connections can reduce database throughput.

---

## 265.6 Schema and Application Changed Atomically by Assumption

Rejected because rolling distributed deployments temporarily run multiple application versions.

---

# 266. Related Decisions

This ADR extends and implements:

- ADR-007: Transactional Outbox
- ADR-008: At-Least-Once Delivery
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Observability and Production Diagnostics Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-045: Disaster Recovery and Regional Resilience Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard

---

# 267. References

- PostgreSQL Documentation
- Hibernate ORM Documentation
- Spring Data JPA Documentation
- Spring Framework Transaction Management
- HikariCP
- Flyway Documentation
- Testcontainers
- Jakarta Persistence
- Domain-Driven Design
- Designing Data-Intensive Applications
- PostgreSQL MVCC Documentation
- PostgreSQL Explicit Locking Documentation
- PostgreSQL Query Planning Documentation

---

# 268. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial PostgreSQL persistence and database engineering baseline |

---

# 269. Decision Summary

The persistence architecture becomes:

```text
APPLICATION
     |
     v
TRANSACTION
     |
     v
DOMAIN
     |
     v
REPOSITORY
     |
     v
JPA / HIBERNATE
     |
     v
POSTGRESQL
     |
     v
CONSTRAINTS
```

For integrity:

```text
APPLICATION VALIDATION
        +
DATABASE CONSTRAINT
        =
SAFE PERSISTENT INVARIANT
```

For concurrency:

```text
READ ENTITY
    |
    v
VERSION = 4
    |
    +----------------+
    |                |
    v                v
TRANSACTION A    TRANSACTION B
    |                |
    v                v
UPDATE V5        UPDATE V5
    |                |
    v                v
 SUCCESS          CONFLICT
```

rather than silent lost update.

For transactions:

```text
BEGIN
  |
  +--> READ
  |
  +--> VALIDATE
  |
  +--> UPDATE
  |
COMMIT
```

and avoid:

```text
BEGIN
  |
  +--> DATABASE
  |
  +--> HTTP CALL
  |
  +--> WAIT
  |
  +--> HTTP CALL
  |
COMMIT
```

For Virtual Threads:

```text
5,000 REQUESTS
      |
      v
VIRTUAL THREADS
      |
      v
BOUNDED HIKARI POOL
      |
      v
POSTGRESQL
```

For query optimization:

```text
APPLICATION QUERY
       |
       v
GENERATED SQL
       |
       v
EXPLAIN ANALYZE
       |
       v
ACTUAL PLAN
       |
       v
BOTTLENECK
       |
       v
QUERY / INDEX CHANGE
       |
       v
MEASURE AGAIN
```

For N+1:

```text
100 ORDERS
    |
    +--> SELECT ORDERS
    |
    +--> 100 SELECT CUSTOMER

          BAD
```

Prefer, according to the use case:

```text
PROJECTION

FETCH JOIN

ENTITY GRAPH

BATCH FETCH

BATCH QUERY
```

For Flyway:

```text
V27 APPLIED
    |
    v
PROBLEM FOUND
    |
    X
DO NOT MODIFY V27
    |
    v
CREATE V28
    |
    v
APPLY FORWARD FIX
```

For zero-downtime schema evolution:

```text
EXPAND
  |
  v
ADD COMPATIBLE SCHEMA
  |
  v
DEPLOY APPLICATION
  |
  v
BACKFILL
  |
  v
SWITCH
  |
  v
VERIFY
  |
  v
CONTRACT
```

For migration compatibility:

```text
OLD APPLICATION
       +
TRANSITIONAL DATABASE
       +
NEW APPLICATION
       =
ROLLING DEPLOYMENT SAFETY
```

For testing:

```text
REPOSITORY
    |
    v
TESTCONTAINERS
    |
    v
REAL POSTGRESQL
    |
    +--> Constraints
    |
    +--> JSONB
    |
    +--> UUID
    |
    +--> Locks
    |
    +--> Native SQL
    |
    +--> Flyway
```

The complete persistence equation is:

```text
EXPLICIT TRANSACTIONS
        +
DATABASE CONSTRAINTS
        +
CONCURRENCY CONTROL
        +
BOUNDED CONNECTIONS
        +
EFFICIENT SQL
        +
PURPOSEFUL INDEXES
        +
SAFE PAGINATION
        +
IMMUTABLE MIGRATIONS
        +
EXPAND / CONTRACT
        +
REAL POSTGRESQL TESTING
        +
OBSERVABILITY
        =
RELIABLE ENTERPRISE PERSISTENCE
```

The governing principle is:

```text
Persistent data deserves stronger
guarantees than application convention.

Validate in the application.

Enforce critical integrity
in the database.

Keep transactions short.

Understand generated SQL.

Measure before adding indexes.

Do not use ORM abstractions
as an excuse to ignore PostgreSQL.

Do not solve concurrency only
with application-level checks.

Do not increase connection pools
because more threads exist.

And never rewrite database history
by modifying an already applied
Flyway migration.

If an applied migration needs
correction, create a new migration.

Always move the schema forward.
```
