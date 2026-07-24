# ADR-031: Adopt Database Performance and Data Access Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-031 |
| Title | Adopt Database Performance and Data Access Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | PostgreSQL, Spring Data JPA, Hibernate, Transactions, Performance |
| Related Work Items | JPA, Hibernate, SQL, Indexing, Pagination, Locking, HikariCP, Performance |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform uses PostgreSQL as its primary transactional database and Spring Data JPA/Hibernate as its principal Java persistence abstraction.

The application stack includes:

```text
Java 21

↓

Spring Boot

↓

Spring Data JPA

↓

Hibernate

↓

JDBC

↓

HikariCP

↓

PostgreSQL
```

JPA provides substantial developer productivity, but it does not eliminate database behavior.

Application performance remains fundamentally affected by:

- generated SQL
- query count
- query shape
- indexes
- execution plans
- cardinality
- transaction duration
- lock duration
- connection usage
- fetch strategies
- pagination strategy
- database statistics
- database maintenance
- concurrency

Therefore database access must be designed intentionally rather than treated as an implementation detail hidden behind repositories.

---

# 2. Problem Statement

The platform requires standards defining:

- transaction boundaries
- transaction propagation
- remote I/O inside transactions
- connection-pool usage
- query design
- query-count control
- N+1 prevention
- fetch strategies
- projections
- EntityGraph
- JOIN FETCH
- batch fetching
- pagination
- keyset pagination
- sorting
- indexes
- execution plans
- batching
- bulk operations
- locking
- optimistic concurrency
- pessimistic concurrency
- deadlocks
- transaction isolation
- query timeouts
- slow queries
- PostgreSQL statistics
- VACUUM
- autovacuum
- table/index bloat
- observability
- performance testing

---

# 3. Decision Drivers

Primary drivers are:

1. predictable latency
2. database stability
3. scalability
4. transactional correctness
5. efficient connection usage
6. reduced lock contention
7. predictable SQL
8. controlled memory consumption
9. maintainability
10. observability
11. production diagnosability
12. efficient pagination
13. concurrency safety
14. reduced infrastructure cost
15. independent service scalability

---

# 4. Decision

The Enterprise Order Platform adopts database access based on:

```text
INTENTIONAL QUERY

+

SMALL TRANSACTION

+

APPROPRIATE INDEX

+

BOUNDED RESULT SET

+

CONTROLLED CONCURRENCY

+

MEASURED PERFORMANCE
```

JPA/Hibernate is the persistence abstraction.

PostgreSQL behavior remains the performance reality.

---

# 5. Fundamental Principle

The platform adopts:

```text
JPA hides boilerplate.

JPA does not hide database cost.
```

Every repository operation eventually becomes:

```text
SQL

+

Connections

+

Transactions

+

Locks

+

CPU

+

Memory

+

Disk I/O
```

---

# 6. Persistence Abstraction

Spring Data JPA is preferred for ordinary transactional persistence.

---

# 7. Abstraction Escape

When a query requires explicit SQL control for correctness or measurable performance, the architecture may use:

- JPQL
- Criteria API
- native SQL
- JdbcTemplate

according to the use case.

---

# 8. Native SQL

Native SQL is not inherently a failure of architecture.

It is appropriate when PostgreSQL-specific capabilities or query control provide meaningful value.

---

# 9. Premature Native SQL

Native SQL must not replace simple JPA operations without a concrete reason.

---

# 10. Repository Responsibility

Repositories provide persistence access.

They must not become:

- business orchestration services
- HTTP clients
- Kafka publishers
- generic utility containers

---

# 11. Persistence Entity

JPA entities represent persistence models.

They must not automatically become:

- API contracts
- Kafka contracts
- external DTOs

---

# 12. Transaction Boundary

Transactions should represent the smallest coherent atomic business operation.

Preferred:

```text
BEGIN

Read required state

Validate local invariant

Modify state

Persist

COMMIT
```

---

# 13. Small Transactions

Prefer:

```text
Small deterministic transaction
```

over:

```text
Large transaction containing unrelated work
```

---

# 14. Why Transaction Duration Matters

An open transaction may retain:

- database connection
- row locks
- tuple versions
- transaction snapshots
- database resources

---

# 15. Remote I/O Inside Transaction

External I/O must not remain inside a database transaction unless explicitly required by the architecture.

Examples include:

```text
HTTP

Kafka

S3

SMTP

External API

Remote Service
```

---

# 16. External Latency Amplification

This flow is dangerous:

```text
BEGIN TRANSACTION
       |
       v
UPDATE ORDER
       |
       v
CALL EXTERNAL API
       |
       | 5 seconds
       v
RETURN
       |
       v
COMMIT
```

During the remote call the application may retain:

```text
DB Connection

+

Transaction

+

Locks
```

---

# 17. Failure Cascade

A slow dependency can become a database-capacity incident:

```text
External API Slow
       ↓
Transactions Remain Open
       ↓
Connections Remain Borrowed
       ↓
Hikari Pool Exhaustion
       ↓
Requests Wait for Connections
       ↓
Timeouts Increase
       ↓
Retries Increase
       ↓
Database Pressure Increases
       ↓
Service Degradation
```

---

# 18. Preferred Flow

Where business semantics permit:

```text
Remote Validation / Data Retrieval

↓

BEGIN TRANSACTION

↓

Local State Mutation

↓

COMMIT
```

or:

```text
BEGIN TRANSACTION

↓

Business State + Outbox

↓

COMMIT

↓

Asynchronous External Processing
```

---

# 19. Transactional Outbox

Kafka publication requiring atomic consistency with database state follows ADR-010 and ADR-030.

---

# 20. HTTP Atomicity

A PostgreSQL transaction cannot provide atomic commit with an arbitrary external HTTP service.

Do not create an illusion of distributed atomicity by keeping the local transaction open.

---

# 21. Compensation

Cross-system consistency should use appropriate:

- idempotency
- saga
- process manager
- compensation
- reconciliation

instead of excessively long database transactions.

---

# 22. `@Transactional`

`@Transactional` must be applied intentionally.

---

# 23. Service Boundary

Transactional boundaries normally belong at application/service use-case boundaries rather than controller methods.

---

# 24. Controller Transaction

Controllers should not normally own database transactions.

---

# 25. Repository Transaction

Repository-level transaction behavior must not accidentally fragment an operation that requires a single application transaction.

---

# 26. Self Invocation

Developers must understand Spring proxy semantics.

Calling a transactional method through `this` may bypass proxy-based transactional interception.

---

# 27. Private Transactional Method

`@Transactional` on private methods must not be assumed to provide ordinary proxy-based transactional behavior.

---

# 28. Transaction Propagation

Propagation must be selected deliberately.

Default:

```text
REQUIRED
```

is appropriate for most application transactions.

---

# 29. `REQUIRES_NEW`

`REQUIRES_NEW` must not be used merely to suppress transaction problems.

It creates an independent transaction and may consume another database connection.

---

# 30. Nested Capacity

Excessive `REQUIRES_NEW` can increase pool pressure.

---

# 31. Read-Only Transaction

Read-only operations may use:

```java
@Transactional(readOnly = true)
```

where appropriate.

---

# 32. Read-Only Semantics

`readOnly = true` is an optimization/hint and design signal.

It is not a security mechanism.

---

# 33. Transaction Isolation

Isolation levels must remain at the database/application default unless a concrete concurrency requirement justifies a change.

---

# 34. Isolation Escalation

Higher isolation is not automatically safer.

It may increase:

- contention
- retries
- serialization failures

---

# 35. Long Transaction

Long-running transactions are strongly discouraged.

---

# 36. User Think Time

Never keep a database transaction open while waiting for:

- user input
- browser interaction
- approval outside the process
- asynchronous workflow completion

---

# 37. Transaction Timeout

Critical or potentially expensive transactional operations should have bounded timeouts where appropriate.

---

# 38. Query Timeout

Potentially expensive queries should use bounded query execution time where practical.

---

# 39. Connection Pool

Database connections are a finite resource.

---

# 40. HikariCP

HikariCP is the standard connection pool.

---

# 41. Pool Sizing

Connection-pool size must be based on:

- database capacity
- service replicas
- workload
- transaction duration
- query latency
- concurrency

not arbitrary large numbers.

---

# 42. Replica Multiplication

If:

```text
20 connections per pod
```

and:

```text
15 pods
```

the service can request:

```text
300 database connections
```

before considering other services.

---

# 43. Database Capacity

Pool sizing must be evaluated globally:

```text
SUM(
  replicas × maxPoolSize
)
```

across database clients.

---

# 44. Bigger Pool

A larger pool does not automatically improve performance.

---

# 45. Excessive Connections

Too many active database connections can increase:

- context switching
- memory usage
- lock contention
- database CPU pressure

---

# 46. Pool Exhaustion

Pool exhaustion is usually a symptom requiring investigation.

Potential causes include:

- slow SQL
- long transactions
- external I/O in transactions
- leaked connections
- excessive concurrency
- database saturation

---

# 47. Pool Timeout

Connection acquisition must have a bounded timeout.

---

# 48. Connection Leak Detection

Leak detection may be enabled diagnostically where justified.

It should not substitute for proper transaction/resource management.

---

# 49. Connection Lifetime

Connection lifetime and idle settings must align with:

- PostgreSQL
- proxies
- load balancers
- cloud database configuration

---

# 50. Query Design

Every high-volume query must be evaluated as SQL, not only as repository code.

---

# 51. Generated SQL

Developers must be able to inspect SQL generated by Hibernate when diagnosing performance.

---

# 52. SQL Logging

Full SQL logging must not remain indiscriminately enabled in production.

---

# 53. Parameter Logging

Sensitive bind parameters must not be exposed in logs.

---

# 54. Query Count

Performance analysis must consider both:

```text
How expensive is each query?
```

and:

```text
How many queries are executed?
```

---

# 55. N+1

N+1 query behavior is a major persistence anti-pattern.

Example:

```text
SELECT orders
```

followed by:

```text
for each order:

SELECT customer

SELECT items

SELECT status
```

can result in:

```text
1 + N + N + N
```

queries.

---

# 56. N+1 Detection

N+1 must be identified through:

- query-count tests
- SQL inspection
- tracing
- profiling
- production telemetry

---

# 57. N+1 Solutions

Appropriate solutions include:

- projection
- JOIN FETCH
- EntityGraph
- batch fetching
- dedicated query DTO

depending on required data shape.

---

# 58. Global EAGER

Changing associations globally to:

```java
FetchType.EAGER
```

is not the default solution to N+1.

---

# 59. Why EAGER Is Dangerous

Global eager loading can create:

- unnecessary joins
- excessive data loading
- cartesian products
- unpredictable queries
- memory pressure

---

# 60. Default Fetch Strategy

Associations should generally remain lazy unless domain/query behavior clearly requires otherwise.

---

# 61. Explicit Query Shape

Fetch exactly the graph required by the use case.

---

# 62. JOIN FETCH

`JOIN FETCH` is appropriate when a query requires associated data in the same database round trip.

---

# 63. Multiple Collections

Fetching multiple collection associations in one query requires care because joins may multiply rows dramatically.

---

# 64. Cartesian Explosion

Example:

```text
Order
  |
  +-- 20 Items
  |
  +-- 10 Attachments
```

joining both collections may produce:

```text
20 × 10 = 200
```

rows for one order.

---

# 65. EntityGraph

EntityGraph may express use-case-specific fetch requirements without changing global entity mapping.

---

# 66. Projection

Projection is preferred when a read use case requires only a subset of entity fields.

---

# 67. Example

Instead of loading:

```text
Order Entity
+
Customer
+
Items
+
Audit
+
Addresses
```

for a list page requiring:

```text
id
orderNumber
status
createdAt
total
```

use an appropriate projection.

---

# 68. DTO Projection

DTO projections are useful for query-oriented read models.

---

# 69. Interface Projection

Spring Data interface projections may be used when they produce predictable SQL and remain maintainable.

---

# 70. Projection Verification

Do not assume a projection is efficient merely because fewer Java getters are used.

Inspect generated SQL.

---

# 71. Batch Fetching

Hibernate batch fetching may reduce repeated association queries.

---

# 72. Batch Fetch Limitation

Batch fetching reduces N+1 impact but may not be as efficient as a purpose-built projection for list endpoints.

---

# 73. Open Session in View

Open Session in View should be disabled for service APIs unless a specific architectural decision requires it.

---

# 74. Why

Persistence access should not occur unexpectedly during response serialization.

---

# 75. LazyInitializationException

A `LazyInitializationException` should normally be solved by correcting the query/use-case boundary, not by keeping persistence sessions open across the web layer.

---

# 76. API Serialization

Jackson serialization must not accidentally trigger uncontrolled persistence traversal.

---

# 77. Bidirectional Associations

Bidirectional entity relationships require careful serialization handling and should not be exposed directly.

---

# 78. Pagination

Unbounded collection queries are prohibited for high-volume datasets.

---

# 79. Pageable

Spring Data `Pageable` is appropriate for conventional pagination.

---

# 80. Page

`Page<T>` normally requires:

```text
Data Query

+

Count Query
```

---

# 81. Count Cost

On large or complex datasets, the count query may be expensive.

---

# 82. Slice

Use:

```text
Slice<T>
```

when the client needs only:

```text
Has next page?
```

and does not require total count.

---

# 83. Offset Pagination

Traditional pagination:

```sql
LIMIT 20 OFFSET 100000
```

may become increasingly expensive for deep pages.

---

# 84. Keyset Pagination

For large ordered datasets, keyset/cursor pagination should be considered.

Example:

```sql
WHERE created_at < :lastCreatedAt
ORDER BY created_at DESC
LIMIT 20
```

---

# 85. Stable Sort

Keyset pagination requires deterministic ordering.

---

# 86. Tie Breaker

If `created_at` is not unique:

```text
ORDER BY created_at DESC, id DESC
```

may provide deterministic continuation.

---

# 87. Cursor

Cursor values must correspond to indexed ordering columns where practical.

---

# 88. Arbitrary Sort

Public APIs must not allow unrestricted sorting by arbitrary persistence properties.

---

# 89. Sort Whitelist

Allowed sort fields should be explicitly mapped.

---

# 90. Sort Security

Sort parameters must never be concatenated unsafely into SQL.

---

# 91. Indexes

Indexes are workload-driven data structures.

---

# 92. Index Purpose

Indexes should support:

- filtering
- joins
- ordering
- uniqueness
- foreign-key access patterns

where appropriate.

---

# 93. Index Everything

Creating indexes on every column is prohibited as a strategy.

---

# 94. Index Cost

Indexes increase:

- storage
- INSERT cost
- UPDATE cost
- DELETE cost
- VACUUM work

---

# 95. Composite Index

Composite indexes must reflect actual query predicates and ordering.

---

# 96. Column Order

Column order matters.

An index:

```sql
(a, b)
```

is not equivalent to:

```sql
(b, a)
```

---

# 97. Query-Driven Index

Indexes should be designed from actual access patterns.

---

# 98. Foreign Keys

Frequently accessed foreign-key columns should be evaluated for indexing.

---

# 99. PostgreSQL Foreign Key Index

PostgreSQL does not automatically create an index on every referencing foreign-key column.

---

# 100. Unique Constraint

Business uniqueness should be enforced by database constraints where appropriate.

---

# 101. Application Check

This is insufficient for concurrency:

```text
if (!repository.exists(...)) {
    repository.save(...)
}
```

without a database uniqueness guarantee when uniqueness is a business invariant.

---

# 102. Partial Index

PostgreSQL partial indexes may be useful when queries consistently target a subset.

Example conceptually:

```sql
WHERE status = 'PENDING'
```

---

# 103. Functional Index

Functional indexes may be used for query patterns involving deterministic expressions.

---

# 104. Expression Match

The query expression must align with the index expression for PostgreSQL to use it effectively.

---

# 105. Index Selectivity

Low-selectivity columns may provide little value as standalone indexes.

---

# 106. Covering Index

`INCLUDE` columns may reduce heap access for appropriate read-heavy queries.

---

# 107. Over-Indexing

Index proliferation must be avoided.

---

# 108. Unused Indexes

Unused indexes should be periodically reviewed before removal.

---

# 109. Index Removal

Index removal requires workload evidence and safe deployment planning.

---

# 110. Flyway

All schema/index changes follow the Flyway migration strategy.

---

# 111. Applied Migration

Applied migrations are immutable.

---

# 112. Index Adjustment

If an existing index must change:

```text
DO NOT EDIT OLD MIGRATION
```

Create:

```text
NEW FLYWAY MIGRATION
```

---

# 113. Production Index Creation

Large index creation must consider locking and production impact.

---

# 114. `CREATE INDEX CONCURRENTLY`

PostgreSQL concurrent index creation should be considered for large production tables where operational constraints require it.

---

# 115. Transaction Constraint

PostgreSQL `CREATE INDEX CONCURRENTLY` has transaction restrictions that must be considered when designing Flyway migrations.

---

# 116. Execution Plan

Complex or slow SQL must be analyzed with PostgreSQL execution plans.

---

# 117. EXPLAIN

Use:

```sql
EXPLAIN
```

to inspect the planned execution strategy.

---

# 118. EXPLAIN ANALYZE

Use:

```sql
EXPLAIN ANALYZE
```

to measure actual execution behavior in an appropriate safe environment.

---

# 119. Production Caution

`EXPLAIN ANALYZE` actually executes the statement.

It must be used carefully in production.

---

# 120. Plan Analysis

Relevant indicators include:

- sequential scans
- index scans
- bitmap scans
- estimated rows
- actual rows
- loops
- sort operations
- memory
- disk spill
- execution time

---

# 121. Estimate Error

Large differences between:

```text
estimated rows
```

and:

```text
actual rows
```

may indicate statistics/cardinality problems.

---

# 122. Sequential Scan

A sequential scan is not automatically bad.

For small tables or low-selectivity queries it may be optimal.

---

# 123. Index Scan

An index scan is not automatically better.

Optimization must be workload based.

---

# 124. Query Rewrite

Sometimes rewriting a query is more effective than adding an index.

---

# 125. `SELECT *`

Avoid `SELECT *` in performance-sensitive queries where only a subset is required.

---

# 126. Large Columns

Avoid loading large:

- TEXT
- JSON
- BYTEA

columns when not needed.

---

# 127. JSONB

PostgreSQL JSONB may be used for genuinely flexible document-like attributes.

---

# 128. JSONB Is Not Escape Hatch

JSONB must not replace relational modeling merely to avoid schema design.

---

# 129. JSONB Index

JSONB query patterns require appropriate indexing such as GIN where justified.

---

# 130. Query Parameterization

Queries must use bind parameters.

---

# 131. SQL Injection

Dynamic values must not be concatenated into SQL.

---

# 132. Dynamic Query

Dynamic filters should use safe query-building mechanisms.

---

# 133. Bulk Insert

Large insert workloads should use batching.

---

# 134. Hibernate JDBC Batching

Hibernate JDBC batching should be enabled/tuned where workloads benefit.

---

# 135. Batch Size

Batch size must be measured rather than maximized blindly.

---

# 136. Identity Generation

Identifier-generation strategy can affect insert batching.

---

# 137. Sequence

PostgreSQL sequence-based ID generation generally works better with batching than database identity strategies requiring immediate generated values.

---

# 138. UUID

UUID identifiers may be used according to domain/platform standards.

---

# 139. Random UUID Index Locality

Random UUID insertion can reduce B-tree locality compared with more sequential identifiers.

This should be considered for very high-volume tables.

---

# 140. UUID Optimization

Identifier strategy must not be changed solely for theoretical micro-optimization without workload evidence.

---

# 141. Bulk Update

Updating thousands of rows by loading every entity individually is discouraged.

---

# 142. Bulk DML

Bulk JPQL/native SQL may be preferable for true set-based operations.

---

# 143. Persistence Context

Bulk DML bypasses ordinary persistence-context synchronization.

---

# 144. Clear Context

After bulk DML, persistence-context state must be handled explicitly where necessary.

---

# 145. Entity Batch Processing

Large entity-processing loops should periodically flush/clear where appropriate to avoid persistence-context memory growth.

---

# 146. Loading Entire Table

This is prohibited for large tables:

```java
repository.findAll()
```

followed by in-memory filtering.

---

# 147. Filter at Database

Push appropriate filtering, ordering and aggregation to PostgreSQL.

---

# 148. In-Memory Processing

In-memory processing is appropriate only when the bounded dataset and operation justify it.

---

# 149. Aggregation

Database aggregation should generally be used when only aggregate values are required.

---

# 150. `COUNT`

Do not load rows into Java merely to count them.

---

# 151. `EXISTS`

When only existence matters, prefer an existence-oriented query.

---

# 152. `findFirst`

Do not fetch a complete collection when only the first matching record is required.

---

# 153. Locking

Concurrency correctness must be explicit.

---

# 154. Optimistic Locking

Optimistic locking is preferred for many ordinary concurrent-update scenarios.

---

# 155. `@Version`

JPA `@Version` may be used to detect conflicting concurrent updates.

---

# 156. Optimistic Conflict

A version conflict should result in an explicit application/business outcome.

---

# 157. Blind Retry

Optimistic-lock failures must not always be retried blindly.

The business operation may need re-evaluation against new state.

---

# 158. Pessimistic Locking

Pessimistic locking may be used when business correctness requires serialized access to specific rows.

---

# 159. Pessimistic Cost

Pessimistic locks reduce concurrency and increase deadlock risk.

---

# 160. Lock Scope

Lock the smallest necessary dataset.

---

# 161. Lock Duration

Keep lock duration short.

---

# 162. External I/O Under Lock

External calls while holding pessimistic database locks are strongly discouraged.

---

# 163. Lock Timeout

Pessimistic locking should use bounded lock waits where appropriate.

---

# 164. `SKIP LOCKED`

`FOR UPDATE SKIP LOCKED` may be appropriate for database-backed worker queues/outbox dispatch.

---

# 165. `SKIP LOCKED` Semantics

`SKIP LOCKED` changes processing semantics and must not be applied indiscriminately to ordinary business queries.

---

# 166. Deadlock

Deadlocks are possible even with correct transactional code.

---

# 167. Deadlock Prevention

Reduce deadlock probability through:

- consistent lock ordering
- short transactions
- narrow lock scope
- appropriate indexes

---

# 168. Deadlock Recovery

PostgreSQL may abort one transaction to resolve a deadlock.

Applications must handle the resulting failure safely.

---

# 169. Deadlock Retry

A bounded retry may be appropriate for operations known to be safely repeatable.

---

# 170. Retry Safety

Database retry requires the same idempotency awareness as other distributed retries.

---

# 171. Lost Update

Concurrent read-modify-write operations must be evaluated for lost-update risk.

---

# 172. Atomic Update

Where appropriate, prefer atomic SQL:

```sql
UPDATE ...
SET quantity = quantity + :delta
WHERE ...
```

instead of unsafe application-level read/modify/write sequences.

---

# 173. Constraint as Concurrency Control

Database constraints are valuable concurrency-control mechanisms.

---

# 174. Check Constraint

Business invariants representable locally in the database should use `CHECK` constraints where appropriate.

---

# 175. Not Null

Required persistence fields should use database `NOT NULL` constraints where appropriate.

---

# 176. Application Validation

Application validation complements database constraints.

It does not replace critical persistence integrity constraints.

---

# 177. Referential Integrity

Foreign keys should enforce important relational integrity where appropriate.

---

# 178. Cascade

Database/JPA cascade semantics must be selected intentionally.

---

# 179. `CascadeType.ALL`

`CascadeType.ALL` must not be used automatically on every association.

---

# 180. Orphan Removal

`orphanRemoval = true` has destructive persistence semantics and requires explicit domain justification.

---

# 181. Delete Cascade

Large cascaded deletes can create:

- long transactions
- locks
- WAL volume
- replication impact

---

# 182. Retention Deletion

Large lifecycle deletions follow ADR-029 and use bounded processing.

---

# 183. PostgreSQL MVCC

PostgreSQL uses MVCC.

Updates/deletes create obsolete tuple versions that require cleanup.

---

# 184. VACUUM

VACUUM is fundamental PostgreSQL maintenance.

---

# 185. Autovacuum

Autovacuum must remain enabled and monitored.

---

# 186. Disable Autovacuum

Disabling autovacuum globally is prohibited.

---

# 187. Dead Tuples

High dead-tuple accumulation may indicate:

- heavy updates/deletes
- insufficient vacuum
- long transactions

---

# 188. Long Transaction Impact

Long-running transactions can prevent PostgreSQL from reclaiming old row versions.

---

# 189. Table Bloat

Table bloat must be monitored for high-churn tables.

---

# 190. Index Bloat

Indexes can also become bloated.

---

# 191. Maintenance

Maintenance actions must be based on observed database behavior.

---

# 192. `VACUUM FULL`

`VACUUM FULL` requires substantial locking and must not be treated as routine production maintenance.

---

# 193. ANALYZE

PostgreSQL statistics must remain current enough for effective planning.

---

# 194. Statistics

Poor statistics can produce poor execution plans even when suitable indexes exist.

---

# 195. Extended Statistics

PostgreSQL extended statistics may be useful for correlated columns when planner estimates are consistently inaccurate.

---

# 196. Statistics Target

Statistics targets may be adjusted for specific problematic columns when evidence supports it.

---

# 197. Slow Query

Slow-query detection must be available in production.

---

# 198. `pg_stat_statements`

Where infrastructure permits, `pg_stat_statements` should be enabled for query-performance analysis.

---

# 199. Query Fingerprint

Queries should be analyzed by normalized fingerprint rather than individual parameter values.

---

# 200. Slow Query Threshold

Slow-query thresholds must reflect service SLOs and workload characteristics.

---

# 201. Database Observability

Monitor at least:

- connection usage
- connection wait
- query latency
- transaction duration
- slow queries
- locks
- deadlocks
- database CPU
- disk I/O
- cache hit behavior
- replication health where applicable

---

# 202. Application Pool Metrics

HikariCP metrics should expose:

- active
- idle
- pending
- max
- acquisition time

---

# 203. Pending Connections

Persistent pending connection requests are an important saturation signal.

---

# 204. Transaction Duration Metric

Transaction duration is often more actionable than query duration alone.

---

# 205. Query Tracing

Critical database spans may be captured through approved observability instrumentation.

---

# 206. SQL Privacy

Database telemetry must follow ADR-029.

---

# 207. Parameter Values

Personal/sensitive parameter values must not become unrestricted telemetry attributes.

---

# 208. High Cardinality

SQL parameters and entity IDs must not become metric labels.

---

# 209. Alerting

Alerts should focus on actionable database symptoms.

Examples:

- pool near exhaustion
- connection acquisition timeout
- abnormal deadlocks
- abnormal query latency
- replication problems
- database capacity saturation

---

# 210. Connection Alert

A pool at 100% active for a few milliseconds is not necessarily an incident.

Sustained saturation with pending requests is more meaningful.

---

# 211. Database SLO

Database behavior should support application SLOs rather than exist as an unrelated monitoring domain.

---

# 212. Performance Testing

Critical query paths require performance testing with representative data volume.

---

# 213. Small Dataset Illusion

A query performing well with:

```text
100 rows
```

does not prove acceptable performance with:

```text
100 million rows
```

---

# 214. Representative Cardinality

Performance tests must approximate realistic:

- table sizes
- distribution
- selectivity
- relationships

---

# 215. Query Regression

Critical query performance should be monitored for regression across releases.

---

# 216. Index Regression

Schema migrations may change query plans and must be validated where risk is high.

---

# 217. Load Test

Load testing must include database capacity.

---

# 218. Application-Only Load Test

A load test is incomplete if the database dataset is unrealistically small.

---

# 219. Concurrency Test

Concurrency-sensitive persistence logic requires dedicated tests.

---

# 220. Optimistic Lock Test

Critical `@Version` behavior should have integration tests.

---

# 221. Unique Constraint Test

Concurrency-sensitive uniqueness must be validated at database level.

---

# 222. Integration Tests

Repository behavior requiring PostgreSQL-specific semantics should be tested against PostgreSQL rather than relying solely on an in-memory substitute.

---

# 223. Testcontainers

Testcontainers PostgreSQL is preferred where realistic database integration behavior is required and platform constraints permit.

---

# 224. H2 Limitation

H2 must not be assumed to reproduce PostgreSQL behavior exactly.

---

# 225. Migration Test

Flyway migrations must be validated against the supported PostgreSQL version.

---

# 226. Query Count Test

Critical list/detail operations should use query-count assertions where N+1 regression risk is significant.

---

# 227. Repository Test

Repository tests should validate:

- filtering
- sorting
- pagination
- constraints
- locking
- query semantics

where relevant.

---

# 228. Production Diagnostics

When investigating database latency:

```text
Application
    |
    +--> Pool wait?
    |
    +--> Transaction duration?
    |
    +--> Query count?
    |
    +--> Slow SQL?
    |
    +--> Lock wait?
    |
    +--> Database CPU/I/O?
    |
    +--> Bad execution plan?
```

---

# 229. Diagnosis Order

Do not automatically conclude:

```text
Database is slow.
```

The application may be:

- holding connections too long
- generating excessive queries
- performing N+1
- opening excessive concurrency
- waiting on locks

---

# 230. Repository Method Name

A convenient Spring Data repository method can still generate an expensive query.

---

# 231. Derived Query

Complex derived-query method names should be replaced by clearer explicit queries/specifications when readability or SQL control suffers.

---

# 232. Specification

JPA Specifications may support dynamic filters.

---

# 233. Specification Complexity

Large generic Specification frameworks can become difficult to optimize and understand.

---

# 234. Search Architecture

Complex search endpoints may require dedicated query infrastructure rather than forcing every use case through entity repositories.

---

# 235. CQRS

Separate read models may be considered for materially different read/write workloads.

---

# 236. CQRS Is Not Default

CQRS is not required for ordinary CRUD/read scenarios.

---

# 237. Read Replica

Read replicas may be considered for appropriate read-heavy workloads.

---

# 238. Replica Lag

Read replicas introduce replication lag.

---

# 239. Read-After-Write

Use cases requiring immediate read-after-write consistency must not blindly route reads to asynchronous replicas.

---

# 240. Replica Routing

Replica use requires explicit consistency semantics.

---

# 241. Database Cache

PostgreSQL and the operating system already provide substantial caching.

Application caching must not be introduced merely because a query exists.

---

# 242. Cache Decision

Application caching belongs to ADR-032.

---

# 243. Query First

Before caching a slow query:

```text
Validate query

↓

Validate index

↓

Validate plan

↓

Validate data volume

↓

Then consider cache
```

---

# 244. Cache Is Not Query Fix

Caching must not hide structurally inefficient SQL without investigation.

---

# 245. Schema Design

Normalized relational modeling is preferred for transactional integrity.

---

# 246. Denormalization

Denormalization may be justified for measured read-performance requirements.

---

# 247. Denormalization Cost

Denormalization introduces:

- duplication
- synchronization
- consistency complexity

---

# 248. Materialized View

PostgreSQL materialized views may be considered for expensive read models with acceptable refresh semantics.

---

# 249. Materialized View Freshness

Freshness requirements must be explicit.

---

# 250. Sequence Queries

Database-generated sequence values must not be assumed gapless.

---

# 251. Gapless Business Number

If a legally/business-required sequence must be gapless, it requires dedicated domain design rather than ordinary PostgreSQL sequence assumptions.

---

# 252. Timestamp

Database/application timestamp semantics must be standardized.

---

# 253. UTC

Persist absolute timestamps using UTC-compatible semantics.

---

# 254. `timestamptz`

PostgreSQL `timestamptz` is generally appropriate for absolute instants.

---

# 255. Local Business Date

A business date without time-zone semantics should remain a date concept rather than an artificial timestamp.

---

# 256. Monetary Data

Monetary values must use exact decimal types.

---

# 257. Floating Point Money

`float` and `double` must not represent financial values requiring decimal precision.

---

# 258. BigDecimal

Java financial values should use `BigDecimal` with explicit scale/rounding rules where required.

---

# 259. PostgreSQL Numeric

PostgreSQL `NUMERIC/DECIMAL` should use appropriate precision and scale.

---

# 260. Nullability

Nullability must reflect domain semantics.

---

# 261. Magic Empty String

Empty strings must not be used as universal substitutes for absent database values.

---

# 262. Enum Persistence

Persisted enums require stable database representation.

---

# 263. Enum Ordinal

JPA enum ordinal persistence is strongly discouraged.

---

# 264. Enum String

String/code-based enum persistence is preferred because enum declaration order may change.

---

# 265. Enum Rename

Renaming a persisted enum value is a data migration, not merely Java refactoring.

---

# 266. Large Object

LOB usage requires explicit performance consideration.

---

# 267. Blob Retrieval

Large binary objects should not be loaded as part of ordinary list queries.

---

# 268. File Storage

Large files should generally use appropriate object storage rather than transactional database rows unless domain requirements justify database storage.

---

# 269. Audit Columns

Common audit metadata may include:

```text
created_at

created_by

updated_at

updated_by
```

where required.

---

# 270. Audit vs Business History

Audit columns do not replace domain history/audit requirements.

---

# 271. Database Trigger

Triggers may be used when database-level semantics genuinely require them.

---

# 272. Hidden Business Logic

Complex application business workflows should not be hidden in database triggers by default.

---

# 273. Stored Procedure

Stored procedures may be appropriate for database-intensive operations when justified.

---

# 274. Mixed Architecture

Use of JPA does not prohibit specialized SQL/procedure solutions where measured requirements justify them.

---

# 275. Data Ownership

Each microservice owns its persistence model.

---

# 276. Cross-Service Database Access

One service must not directly query another service's private database schema as a normal integration mechanism.

---

# 277. Shared Database Coupling

Shared mutable tables between independently deployed microservices are strongly discouraged.

---

# 278. Integration Boundary

Cross-service integration should use governed APIs/events.

---

# 279. Database Migration Ownership

The owning service owns Flyway migrations for its schema.

---

# 280. Backward-Compatible Migration

Schema migrations should support rolling deployments where required.

---

# 281. Expand/Contract

Breaking schema changes should use:

```text
EXPAND

↓

MIGRATE

↓

SWITCH APPLICATION

↓

CONTRACT
```

---

# 282. Example

Instead of immediately renaming a column:

```text
old_column
```

to:

```text
new_column
```

a rolling migration may:

```text
1. Add new_column

2. Deploy compatible application

3. Backfill

4. Switch reads/writes

5. Remove old_column in later migration
```

---

# 283. Destructive Migration

Dropping columns/tables requires explicit verification that no deployed version still depends on them.

---

# 284. Large Backfill

Large data backfills should not automatically run as one enormous Flyway transaction.

---

# 285. Backfill Strategy

Large backfills may require:

- bounded batches
- dedicated migration job
- throttling
- resumability

---

# 286. DDL Lock

DDL can acquire locks affecting production traffic.

---

# 287. Migration Review

High-impact DDL requires operational review.

---

# 288. Database Deployment

A successful Flyway migration does not prove application query performance remains acceptable.

---

# 289. Anti-Patterns

The following are prohibited or strongly discouraged:

- treating JPA as if SQL does not matter
- business orchestration inside repositories
- exposing JPA entities as API/event contracts
- large transaction boundaries
- remote HTTP calls inside transactions without explicit justification
- Kafka publication as an unsafe dual write
- transaction around user think time
- indiscriminate `REQUIRES_NEW`
- arbitrarily oversized Hikari pools
- diagnosing pool exhaustion by simply increasing pool size
- N+1 queries
- solving N+1 by globally changing everything to EAGER
- uncontrolled lazy loading during API serialization
- Open Session in View for ordinary service APIs
- loading complete entities for small list projections
- unbounded queries
- deep OFFSET pagination on massive datasets without evaluation
- arbitrary unrestricted sort fields
- indexing every column
- ignoring composite-index column order
- application-only uniqueness checks
- modifying already-applied Flyway migrations
- assuming sequential scans are always bad
- assuming index scans are always good
- using `SELECT *` unnecessarily
- using JSONB to avoid proper schema design
- SQL string concatenation
- loading an entire table to filter in Java
- loading rows merely to count them
- indiscriminate pessimistic locking
- remote calls while holding database locks
- ignoring optimistic-lock conflicts
- disabling autovacuum
- routine uncontrolled `VACUUM FULL`
- ignoring PostgreSQL statistics
- exposing SQL parameters containing sensitive data in telemetry
- performance testing only with tiny datasets
- assuming H2 equals PostgreSQL
- adding cache before investigating SQL
- using floating-point values for money
- persisting enums by ordinal
- hiding major application business logic in triggers
- cross-service direct database access
- editing historical Flyway migrations
- large unbounded production backfills

---

# 290. Positive Consequences

The decision provides:

- predictable transaction boundaries
- lower connection-pool pressure
- reduced lock duration
- improved query efficiency
- fewer N+1 regressions
- safer pagination
- better index discipline
- improved concurrency correctness
- stronger database observability
- safer schema evolution
- better PostgreSQL utilization
- improved production diagnostics
- reduced cascading failures

---

# 291. Negative Consequences

The decision introduces:

- SQL awareness requirements
- execution-plan analysis
- query-count testing
- index review
- more integration testing
- additional database observability
- more careful migration planning

These costs are accepted because database performance is a fundamental part of application performance.

---

# 292. Neutral Consequences

The decision also means:

- not every query should use JPA entities
- some workloads require native SQL
- some list endpoints should use projections
- some pagination should use cursors instead of page numbers
- some concurrency conflicts must be exposed to application logic
- increasing application concurrency may require database-capacity changes
- application caching cannot compensate for every database design problem

---

# 293. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Connection pool exhaustion | Critical | Medium | Short transactions and pool monitoring |
| N+1 regression | High | High | Query-count tests and explicit fetch plans |
| Missing index | High | Medium | Execution-plan review |
| Excessive indexes | Medium | Medium | Usage review |
| Deadlock | High | Medium | Lock ordering and short transactions |
| Lost update | High | Medium | Optimistic locking/atomic SQL |
| Slow deep pagination | High | Medium | Keyset pagination |
| Long transaction causes bloat | High | Medium | Transaction monitoring |
| Bad planner estimate | High | Medium | ANALYZE/statistics review |
| Migration locks production | Critical | Low | DDL review and safe migration |
| Large backfill overloads DB | High | Medium | Bounded/resumable processing |
| Excessive concurrency saturates DB | Critical | Medium | Capacity budget |
| Remote API holds DB connections | Critical | Medium | Separate remote I/O from transaction |
| Sensitive SQL telemetry exposure | High | Medium | Redaction/minimization |

---

# 294. Implementation Guidance

The following rules are mandatory:

1. Transaction boundaries must represent small atomic business operations.
2. External I/O must not remain inside database transactions without explicit justification.
3. Controllers must not normally own transactions.
4. Transaction propagation must be deliberate.
5. Connection pools must be capacity planned globally.
6. Pool exhaustion must not be solved automatically by increasing pool size.
7. Generated SQL must be inspectable.
8. High-volume operations must consider query count.
9. N+1 must be prevented.
10. Global EAGER loading must not be used as a generic N+1 solution.
11. Use-case-specific projections/fetch plans are preferred.
12. Open Session in View should remain disabled for service APIs.
13. High-volume collection endpoints must be paginated.
14. Deep pagination must evaluate keyset pagination.
15. Public sort fields must be whitelisted.
16. Indexes must correspond to actual access patterns.
17. Database constraints must enforce critical persistence invariants.
18. Applied Flyway migrations must never be modified.
19. Index/schema corrections require new migrations.
20. Slow SQL must be analyzed using execution plans.
21. Bulk operations should use batching/set-based processing.
22. Large tables must not be loaded entirely into application memory.
23. Optimistic locking is preferred where suitable.
24. Pessimistic locking requires explicit justification.
25. Lock duration must remain short.
26. External calls while holding database locks are strongly discouraged.
27. Autovacuum must remain enabled.
28. PostgreSQL statistics must be monitored.
29. Slow-query visibility must exist in production.
30. Hikari pool metrics must be monitored.
31. Database telemetry must not expose sensitive parameters.
32. Performance tests require representative cardinality.
33. PostgreSQL-specific behavior should be tested against PostgreSQL.
34. Query-count tests should protect critical paths against N+1.
35. Cache must not be the first response to inefficient SQL.
36. Financial values must use exact decimal representations.
37. Persisted enums must not rely on Java ordinal position.
38. Cross-service database access is prohibited as normal integration.
39. Schema migrations must support rolling deployments where required.
40. Large data migrations must be bounded and operationally safe.

---

# 295. Database Production Readiness Gate

A critical persistence flow is not production ready until:

```text
[ ] Transaction boundary reviewed

[ ] External I/O outside transaction where possible

[ ] Query count reviewed

[ ] N+1 risk reviewed

[ ] Fetch strategy reviewed

[ ] Projection strategy reviewed

[ ] Pagination defined

[ ] Sort fields controlled

[ ] Required indexes defined

[ ] Execution plan reviewed for critical queries

[ ] Constraints defined

[ ] Concurrency behavior reviewed

[ ] Optimistic/pessimistic locking strategy reviewed

[ ] Connection-pool capacity reviewed

[ ] Query timeout reviewed

[ ] Transaction timeout reviewed

[ ] Slow-query monitoring available

[ ] Pool metrics available

[ ] Lock/deadlock monitoring available

[ ] PostgreSQL maintenance reviewed

[ ] Representative-volume tests completed

[ ] Flyway migrations validated

[ ] Rolling-deployment compatibility reviewed

[ ] Privacy impact reviewed
```

---

# 296. Validation

This ADR will be validated through:

- architecture review
- repository review
- generated-SQL inspection
- query-count tests
- PostgreSQL integration tests
- Testcontainers
- EXPLAIN/EXPLAIN ANALYZE
- index review
- Hikari metrics
- slow-query monitoring
- lock/deadlock monitoring
- concurrency tests
- load testing
- migration testing
- production query telemetry
- periodic database health review

---

# 297. Success Criteria

The decision is successful when:

- database latency remains predictable under load
- connection pools do not routinely saturate
- remote dependency latency does not unnecessarily consume database connections
- critical list operations do not exhibit N+1 behavior
- large datasets remain paginated
- critical queries use appropriate indexes
- slow queries can be diagnosed from production telemetry
- concurrency conflicts preserve business correctness
- deadlocks remain rare and recoverable
- migrations do not unexpectedly block production traffic
- PostgreSQL maintenance remains healthy
- application scaling does not accidentally exceed database connection capacity

---

# 298. Alternatives Rejected

## 298.1 Let Hibernate Handle Everything Automatically

Rejected because ORM abstractions cannot independently determine application-specific performance and transaction requirements.

---

## 298.2 Use EAGER Everywhere

Rejected because it creates uncontrolled data loading and query complexity.

---

## 298.3 Increase Connection Pool Whenever Latency Appears

Rejected because it can amplify database saturation.

---

## 298.4 Keep Transactions Open Across External Calls

Rejected because external latency becomes database resource retention.

---

## 298.5 Add Indexes to Every Column

Rejected because indexes have write/storage/maintenance costs.

---

## 298.6 Cache Every Slow Query

Rejected because cache can hide rather than solve inefficient data access.

---

## 298.7 Use Offset Pagination Everywhere

Rejected because deep offsets may scale poorly.

---

## 298.8 Use H2 as Proof of PostgreSQL Compatibility

Rejected because database semantics and SQL behavior differ.

---

## 298.9 Modify Existing Flyway Migrations

Rejected because applied migration history is immutable.

---

# 299. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-034: Adopt Concurrency and Parallelism Standards

---

# 300. References

- PostgreSQL Documentation
- Hibernate ORM Documentation
- Spring Data JPA Documentation
- Spring Framework Transaction Management Documentation
- HikariCP Documentation
- Flyway Documentation
- Testcontainers Documentation
- Java 21 Documentation
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards

---

# 301. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial database performance and data-access baseline |

---

# 302. Decision Summary

The Enterprise Order Platform treats database performance as an application architecture responsibility.

The primary execution model is:

```text
REQUEST / EVENT
       |
       v
APPLICATION USE CASE
       |
       v
REMOTE DATA REQUIRED?
       |
   +---+---+
   |       |
  YES      NO
   |       |
   v       |
REMOTE I/O |
   |       |
   +---+---+
       |
       v
BEGIN SHORT TRANSACTION
       |
       v
READ REQUIRED DATA
       |
       v
VALIDATE LOCAL STATE
       |
       v
MODIFY
       |
       v
PERSIST
       |
       v
COMMIT
```

Avoid:

```text
BEGIN TRANSACTION
       |
       v
READ DATABASE
       |
       v
CALL REMOTE API
       |
       v
WAIT
       |
       v
CALL ANOTHER API
       |
       v
WAIT
       |
       v
UPDATE DATABASE
       |
       v
COMMIT
```

because:

```text
External Latency

↓

Long Transaction

↓

Connection Held

↓

Lock Held

↓

Pool Pressure

↓

Request Queue

↓

Timeout

↓

Retry

↓

More Pressure
```

For queries:

```text
USE CASE

↓

WHAT DATA IS ACTUALLY REQUIRED?

       |
       +--------------------+
       |                    |
       v                    v
FULL AGGREGATE        SMALL READ MODEL
       |                    |
       v                    v
ENTITY/FETCH PLAN       PROJECTION
```

N+1 must be treated explicitly:

```text
BAD

SELECT orders

↓

20 Orders

↓

20 SELECT customer

↓

20 SELECT items

↓

20 SELECT status

=

61 Queries
```

Instead, choose the correct query shape:

```text
Projection

or

JOIN FETCH

or

EntityGraph

or

Batch Fetch
```

according to the use case.

Pagination follows:

```text
SMALL / NORMAL DATASET
        |
        v
OFFSET / PAGE
```

versus:

```text
VERY LARGE DATASET
+
DEEP NAVIGATION
+
STABLE ORDER
        |
        v
KEYSET / CURSOR PAGINATION
```

Index design follows:

```text
REAL QUERY

↓

WHERE / JOIN / ORDER BY

↓

CARDINALITY

↓

EXPLAIN

↓

INDEX DESIGN

↓

MEASURE
```

not:

```text
Column exists

↓

Create index
```

Connection capacity follows:

```text
DB CONNECTION DEMAND

=

SUM(
    service replicas
    ×
    pool size
)
```

Therefore horizontal application scaling must always consider database capacity.

Concurrency correctness follows:

```text
Concurrent Update
       |
       v
Can conflicts occur?
       |
   +---+---+
   |       |
  YES      NO
   |
   v
Can optimistic concurrency work?
   |
 +---+---+
 |       |
YES      NO
 |       |
 v       v
@Version  Carefully scoped
          pessimistic lock
```

Database diagnosis follows:

```text
SLOW REQUEST
      |
      v
POOL WAIT?
      |
      v
LONG TRANSACTION?
      |
      v
TOO MANY QUERIES?
      |
      v
SLOW QUERY?
      |
      v
LOCK WAIT?
      |
      v
BAD PLAN?
      |
      v
DATABASE SATURATION?
```

And Flyway remains governed by the immutable migration rule:

```text
Migration already applied?
          |
      +---+---+
      |       |
     YES      NO
      |
      v
DO NOT MODIFY
      |
      v
CREATE NEW VERSIONED MIGRATION
```

The definitive principle is:

```text
Hibernate is an abstraction over SQL.

It is not an abstraction over performance.
```

Therefore:

```text
Correct Data Access

=

Correct SQL
+
Correct Transaction Boundary
+
Correct Index
+
Correct Fetch Strategy
+
Correct Concurrency
+
Correct Connection Budget
+
Production Observability
```
