# ADR-066: Adopt Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-066 |
| Title | Adopt Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | API Performance, Queries, Pagination, Filtering, Sorting, Bulk Processing |
| Related Work Items | Java 21, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, REST APIs |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise APIs frequently retrieve increasingly large datasets.

A simple endpoint can evolve from:

```text
GET /orders
```

into:

```text
GET /orders
    ?customerId=...
    &segment=...
    &status=...
    &type=...
    &createdFrom=...
    &createdTo=...
    &page=0
    &size=20
    &sort=createdAt,desc
```

The underlying operation may then involve:

```text
HTTP REQUEST
     |
     v
FILTER PARSING
     |
     v
DATABASE QUERY
     |
     +--> JOINS
     +--> SORTING
     +--> COUNT
     +--> PAGINATION
     |
     v
ENTITY LOADING
     |
     +--> LAZY RELATIONSHIPS
     +--> N+1
     |
     v
REMOTE ENRICHMENT
     |
     +--> PRODUCTS
     +--> CUSTOMERS
     +--> USERS
     |
     v
MAPPING
     |
     v
LARGE JSON RESPONSE
```

Without explicit standards, apparently simple search endpoints can become among the most expensive operations in a service.

---

# 2. Problem Statement

The organization requires standards covering:

- pagination
- `Page`
- `Slice`
- offset pagination
- cursor pagination
- keyset pagination
- maximum page size
- filtering
- dynamic queries
- JPA Specification
- Criteria API
- sorting
- safe sort fields
- projections
- DTO projections
- entity loading
- fetch joins
- EntityGraph
- N+1
- batch fetching
- query count
- count-query optimization
- native SQL
- JDBC
- database indexes
- bulk APIs
- batch processing
- payload limits
- exports
- streaming
- asynchronous processing
- Virtual Threads
- remote fan-out
- concurrency limits
- backpressure
- query timeout
- expensive-query protection

---

# 3. Decision Drivers

Primary drivers are:

1. predictable latency
2. database efficiency
3. bounded resource consumption
4. API scalability
5. memory safety
6. query correctness
7. stable pagination
8. network efficiency
9. operational resilience
10. developer productivity
11. observability
12. maintainability

---

# 4. Decision

Collection endpoints MUST use bounded data-retrieval strategies.

Unbounded retrieval is prohibited for datasets that can grow materially.

Canonical flow:

```text
REQUEST
   |
   v
VALIDATE
FILTERS
   |
   v
VALIDATE
PAGINATION
   |
   v
VALIDATE
SORT
   |
   v
OPTIMIZED QUERY
   |
   v
BOUNDED RESULT
   |
   v
BATCH ENRICHMENT
   |
   v
BOUNDED RESPONSE
```

---

# 5. Fundamental Principle

```text
Do not retrieve
what the caller
does not need.

Do not load
what the response
does not expose.

Do not call remotely
once per row.

And never allow
an API consumer
to choose an
unbounded workload.
```

---

# 6. Pagination

Collection endpoints returning potentially large datasets MUST be paginated.

---

# 7. Unbounded Find-All

Endpoints equivalent to:

```text
SELECT *
FROM orders
```

with no effective result bound are prohibited for growing transactional tables.

---

# 8. Default Page Size

Paginated endpoints MUST define a default page size.

Example:

```text
20
```

or another value appropriate to the resource.

---

# 9. Maximum Page Size

Every paginated endpoint MUST enforce a maximum page size.

Example:

```text
default = 20

maximum = 100
```

The exact values MUST reflect payload and query cost.

---

# 10. Caller Page Size

A request such as:

```text
size=1000000
```

MUST NOT result in one million rows being loaded.

---

# 11. Invalid Page Size

Negative, zero or excessive sizes MUST be rejected or normalized according to the API contract.

Silent unlimited behavior is prohibited.

---

# 12. Offset Pagination

Traditional pagination commonly uses:

```sql
LIMIT :size
OFFSET :offset
```

This is acceptable for ordinary bounded interactive searches.

---

# 13. Offset Cost

Large offsets can become increasingly expensive.

Conceptually:

```text
OFFSET 0
    |
    v
CHEAP

OFFSET 100
    |
    v
USUALLY ACCEPTABLE

OFFSET 1,000,000
    |
    v
POTENTIALLY EXPENSIVE
```

---

# 14. Deep Pagination

Endpoints requiring deep traversal SHOULD evaluate cursor/keyset pagination.

---

# 15. Keyset Pagination

Keyset pagination SHOULD be considered when:

```text
Dataset is large

Ordering is stable

Deep navigation is common

Low latency is important
```

---

# 16. Keyset Example

Instead of:

```sql
ORDER BY created_at DESC
OFFSET 500000
LIMIT 20
```

use conceptually:

```sql
WHERE created_at < :lastCreatedAt
ORDER BY created_at DESC
LIMIT 20
```

---

# 17. Stable Keyset

Keyset pagination MUST use deterministic ordering.

---

# 18. Tie Breaker

A non-unique sort column MUST include a deterministic tie breaker.

Prefer:

```text
ORDER BY created_at DESC, id DESC
```

rather than only:

```text
ORDER BY created_at DESC
```

---

# 19. Cursor

Public cursor APIs SHOULD expose opaque cursors rather than forcing clients to understand internal database keys.

---

# 20. Cursor Integrity

Cursor content MUST be validated and MUST NOT permit arbitrary query manipulation.

---

# 21. Cursor Version

Long-lived public cursor formats SHOULD support evolution/versioning when necessary.

---

# 22. Page

Spring Data `Page<T>` provides:

```text
Content

Page Number

Page Size

Total Elements

Total Pages
```

---

# 23. Page Count Query

`Page<T>` commonly requires an additional:

```sql
COUNT(...)
```

query.

This cost MUST be considered.

---

# 24. Slice

`Slice<T>` SHOULD be considered when the caller only requires:

```text
Current Content

Has Next
```

and does not require exact totals.

---

# 25. Page vs Slice

Decision:

```text
NEED EXACT TOTAL?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
 PAGE   SLICE
```

---

# 26. Expensive Count

Complex searches with expensive joins SHOULD avoid exact count queries unless the API genuinely requires totals.

---

# 27. Count Optimization

Where `Page` is required, count queries SHOULD be independently optimized when generated queries are inefficient.

---

# 28. Count Join

Count queries SHOULD NOT retain unnecessary joins used only for response projection.

---

# 29. Pagination Consistency

Offset pagination over rapidly changing datasets MAY produce duplicates or omissions between pages.

---

# 30. Stable Traversal

Workloads requiring stable traversal SHOULD use a strategy appropriate to consistency requirements, commonly keyset pagination over immutable/stable ordering.

---

# 31. Filtering

Search APIs MUST define which fields are filterable.

---

# 32. Arbitrary Filtering

Callers MUST NOT be allowed to dynamically query arbitrary entity properties merely because those properties exist.

---

# 33. Filter DTO

Complex search endpoints SHOULD use an explicit filter model.

Example:

```java
public record OrderSearchFilter(
        UUID customerId,
        String segment,
        OrderStatus status,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo
) {
}
```

---

# 34. Filter Validation

Filters MUST be validated before query execution.

---

# 35. Date Range

Date-range filters SHOULD define maximum acceptable ranges when unrestricted historical searches are expensive.

---

# 36. Invalid Range

This MUST be rejected:

```text
createdFrom > createdTo
```

---

# 37. Empty Filter

Whether an empty filter is permitted MUST be explicit.

---

# 38. Full Table Search

An empty filter MUST NOT accidentally enable an expensive full-table search when the endpoint requires narrowing criteria.

---

# 39. Wildcard Search

Leading wildcard searches such as:

```sql
LIKE '%ABC'
```

can prevent ordinary index usage and SHOULD be evaluated carefully.

---

# 40. Contains Search

Unrestricted:

```sql
LIKE '%ABC%'
```

on large transactional datasets SHOULD require an appropriate indexing/search strategy.

---

# 41. Case-Insensitive Search

Patterns such as:

```sql
LOWER(column) = LOWER(:value)
```

MAY require functional indexes or alternative normalized storage.

---

# 42. Search Engine

Full-text or highly flexible search requirements MAY justify dedicated search infrastructure rather than increasingly complex transactional SQL.

---

# 43. JPA Specification

Spring Data JPA `Specification` MAY be used for composable dynamic predicates.

---

# 44. Specification Scope

Specifications SHOULD focus on query predicates rather than becoming general business-rule containers.

---

# 45. Specification Complexity

A very large Specification builder with many unrelated responsibilities SHOULD be decomposed.

---

# 46. Criteria API

Criteria API MAY be used where dynamic query composition requires it.

---

# 47. Criteria Readability

Criteria-based queries MUST remain maintainable and testable.

---

# 48. QueryDSL

A type-safe query DSL MAY be adopted when dynamic-query complexity justifies the additional dependency and standardization.

---

# 49. Native Query

Native SQL MAY be used when it provides material value for:

```text
Performance

Database-Specific Features

Complex Reporting

Window Functions

CTEs

Optimized Projections
```

---

# 50. Native SQL Is Not Failure

Using native SQL for a database-intensive operation is not an architectural failure.

The correct abstraction MUST match the workload.

---

# 51. Native Query Cost

Native SQL introduces stronger database coupling and therefore SHOULD be isolated and tested.

---

# 52. Sorting

Sort options exposed to callers MUST be controlled.

---

# 53. Arbitrary Sort

Raw caller values MUST NOT be directly concatenated into SQL `ORDER BY`.

---

# 54. Sort Allowlist

Public sort fields SHOULD map through an allowlist.

Example:

```text
API FIELD        DATABASE/ENTITY PATH

createdAt   ->   createdAt

status      ->   status

customer    ->   customer.name
```

---

# 55. Sort Resolver

Complex services SHOULD centralize safe sort-path resolution.

---

# 56. Unknown Sort

Unknown sort fields MUST be rejected or handled according to the documented contract.

---

# 57. Default Sort

Paginated endpoints SHOULD define deterministic default sorting.

---

# 58. Stable Sort

Pagination MUST NOT rely on unstable ordering.

---

# 59. Sort Index

Frequently used sort/filter combinations SHOULD be considered during database index design.

---

# 60. Dynamic Sort Explosion

Supporting arbitrary sorting over dozens of columns can make indexing impractical.

Only useful business sorts SHOULD be exposed.

---

# 61. Entity Loading

JPA entities SHOULD be loaded when domain behavior or persistence lifecycle requires them.

---

# 62. Read-Only Search

Read-only search endpoints SHOULD NOT automatically load complete aggregate graphs.

---

# 63. Projection

Projections SHOULD be considered when the response needs only a subset of persistent data.

---

# 64. DTO Projection

DTO projections SHOULD be preferred for large read-heavy endpoints when they materially reduce:

```text
Columns Read

Entity Instantiation

Dirty Tracking

Memory Usage

Mapping Work
```

---

# 65. Header Search

A search endpoint returning only order headers SHOULD query only the fields required for the header response.

---

# 66. Do Not Load Details

If the API returns:

```text
Order Header
```

the query SHOULD NOT load:

```text
All Order Items

Product Details

Audit History

Attachments
```

unless required.

---

# 67. Interface Projection

Spring Data interface projections MAY be used for simple read models.

---

# 68. Constructor Projection

Constructor/record DTO projections MAY be used for explicit read models.

---

# 69. Record Projection

Java records are appropriate for immutable query projections where supported.

---

# 70. Projection Ownership

Projection types SHOULD belong to the read/query contract rather than persistence entities.

---

# 71. N+1

N+1 query patterns MUST be actively prevented.

---

# 72. Classic N+1

Example:

```text
1 query:
SELECT orders

then

N queries:
SELECT items WHERE order_id = ?
```

---

# 73. N+1 Detection

SQL/query-count tests SHOULD be considered for performance-sensitive repository operations.

---

# 74. Fetch Join

Fetch joins MAY eliminate selected N+1 problems.

---

# 75. Fetch Join Pagination

Collection fetch joins combined with pagination require special care because they can produce:

```text
Duplicate Rows

Incorrect Pagination

In-Memory Pagination

Large Result Sets
```

---

# 76. EntityGraph

`EntityGraph` MAY explicitly control relationship fetching for appropriate use cases.

---

# 77. EAGER

Changing relationships globally to:

```java
FetchType.EAGER
```

MUST NOT be the default solution for N+1.

---

# 78. LAZY

Associations SHOULD generally remain lazy unless domain/query requirements justify eager loading.

---

# 79. Batch Fetching

Hibernate batch fetching MAY reduce N+1 when appropriate.

---

# 80. Batch Size

Batch-fetch size MUST be tuned based on actual workload rather than arbitrarily maximized.

---

# 81. Two-Step Query

For paginated parent/child retrieval, a two-step strategy MAY be preferable:

```text
1. QUERY PAGE OF PARENT IDS

2. QUERY REQUIRED DATA
   FOR THOSE IDS
```

---

# 82. Query Count

Performance-sensitive endpoints SHOULD have a known approximate database query count.

---

# 83. Accidental Query Growth

A code change that changes:

```text
3 queries
```

into:

```text
203 queries
```

for the same page SHOULD be detectable.

---

# 84. Open Session in View

APIs SHOULD NOT rely on accidental lazy loading during response serialization.

---

# 85. Serialization Query

JSON serialization MUST NOT unexpectedly trigger database queries.

---

# 86. Database Index

Indexes MUST be designed around actual query patterns.

---

# 87. Index Every Column

Creating an index for every filterable column is not an acceptable default strategy.

---

# 88. Composite Index

Composite indexes SHOULD reflect common predicate and ordering patterns.

---

# 89. Index Order

Column order within composite indexes matters and MUST be evaluated against actual queries.

---

# 90. Explain Plan

Slow critical queries SHOULD be analyzed with database execution plans.

---

# 91. Statistics

Database statistics MUST remain healthy enough for the optimizer to make appropriate decisions.

---

# 92. Query Timeout

Potentially expensive queries MUST have bounded execution time.

---

# 93. HTTP Timeout Is Not Enough

A client disconnect or HTTP timeout MUST NOT be the only mechanism limiting database query duration.

---

# 94. Transaction Timeout

Applicable long-running operations SHOULD define appropriate transaction/query timeouts.

---

# 95. Query Cancellation

Where supported, timeout/cancellation SHOULD release database work rather than allowing abandoned work to continue indefinitely.

---

# 96. Expensive Query Protection

Search APIs SHOULD protect against combinations known to create excessive workload.

---

# 97. Query Complexity Budget

Complex public search APIs MAY define a query-complexity budget.

For example:

```text
Wide Date Range
        +
Contains Search
        +
Multiple Joins
        +
Large Page
        =
REJECT OR ASYNC PROCESS
```

---

# 98. API Cost Is Multiplicative

Query cost often behaves conceptually as:

```text
ROWS
 ×
JOINS
 ×
SORT COST
 ×
ENRICHMENT
 ×
PAGE SIZE
```

rather than as one isolated parameter.

---

# 99. Response Payload

Response payload size MUST remain bounded.

---

# 100. Payload Measurement

Performance-sensitive endpoints SHOULD measure actual serialized payload size.

---

# 101. Excessive DTO

Returning hundreds of fields because the entity contains them is prohibited as a design rationale.

---

# 102. Compression

HTTP compression MAY reduce transfer size for sufficiently large compressible responses.

---

# 103. Compression Cost

Compression consumes CPU and MUST be evaluated against payload size and throughput.

---

# 104. Bulk API

Operations involving multiple resources SHOULD consider explicit bulk endpoints when repeated single-resource calls create unnecessary overhead.

---

# 105. Bulk Example

Instead of:

```text
POST /orders/1/approve
POST /orders/2/approve
POST /orders/3/approve
...
```

consider:

```text
POST /orders/approve/bulk
```

when business semantics permit.

---

# 106. Bulk Bound

Every bulk operation MUST define a maximum number of items.

---

# 107. Unlimited Bulk

An unbounded:

```json
{
  "orders": [ ... ]
}
```

payload is prohibited.

---

# 108. Bulk Validation

Bulk APIs MUST validate:

```text
Maximum Item Count

Duplicate IDs

Required Fields

Payload Size

Authorization

Business Rules
```

---

# 109. Duplicate Bulk Item

Duplicate resource identifiers SHOULD be rejected or normalized according to explicit contract semantics.

---

# 110. Bulk Atomicity

Bulk APIs MUST define transaction semantics.

Possible models:

```text
ALL OR NOTHING

PARTIAL SUCCESS

INDEPENDENT ITEM PROCESSING
```

---

# 111. Atomic Bulk

All-or-nothing behavior SHOULD be used only when business semantics genuinely require one transaction.

---

# 112. Large Transaction

Large bulk operations MUST NOT create unbounded transactions.

---

# 113. Partial Result

Partial-success APIs MUST return item-level outcomes in a stable contract.

---

# 114. Retry Bulk

Bulk retry semantics MUST prevent already-successful operations from being duplicated.

---

# 115. Idempotency

Bulk mutation APIs SHOULD follow idempotency requirements defined by ADR-056.

---

# 116. Batch Database Access

Bulk operations SHOULD prefer batch retrieval.

Instead of:

```text
FOR EACH ID
    repository.findById(id)
```

prefer:

```text
repository.findAllById(ids)
```

where semantics permit.

---

# 117. Batch Remote Access

External integrations SHOULD use batch endpoints when available.

---

# 118. Remote N+1

This is prohibited when avoidable:

```text
FOR EACH ORDER
    usersClient.getUser(...)
```

---

# 119. Batch Loader

A dedicated batch loader MAY coordinate remote batch retrieval and mapping.

---

# 120. Batch Loader Bound

Batch loaders MUST enforce bounded input and concurrency.

---

# 121. Fan-Out

A request that triggers many downstream calls creates fan-out.

---

# 122. Fan-Out Cost

Conceptually:

```text
1 API REQUEST
      |
      +--> 100 PRODUCT CALLS
      +--> 100 USER CALLS
      +--> 100 CUSTOMER CALLS
```

is effectively:

```text
1 REQUEST
   =
300 DOWNSTREAM REQUESTS
```

---

# 123. Fan-Out Budget

Endpoints SHOULD have a bounded downstream fan-out budget.

---

# 124. Batch Before Parallel

The preferred optimization order is:

```text
1. REMOVE UNNECESSARY CALLS

2. BATCH CALLS

3. CACHE WHEN VALID

4. PARALLELIZE REMAINING
   INDEPENDENT CALLS
```

---

# 125. Parallelism

Independent I/O MAY execute concurrently.

---

# 126. Virtual Threads

Java 21 Virtual Threads SHOULD be considered for blocking I/O orchestration where they simplify concurrency.

---

# 127. Virtual Threads Do Not Remove Limits

Virtual Threads make blocking cheaper.

They do NOT make downstream capacity unlimited.

---

# 128. Bounded Concurrency

Remote parallelism MUST remain bounded.

---

# 129. Concurrency Limit

A concurrency limit SHOULD consider:

```text
Downstream Capacity

HTTP Connection Pool

Database Pool

Request Deadline

Expected Fan-Out
```

---

# 130. Semaphore

Semaphores or equivalent mechanisms MAY bound concurrent remote operations.

---

# 131. Executor Bound

Executor configuration MUST NOT create uncontrolled resource amplification.

---

# 132. Structured Concurrency

Structured concurrency MAY be considered when available and approved for the Java/runtime baseline.

---

# 133. Deadline

Parallel child operations SHOULD respect the parent request deadline.

---

# 134. Slowest Child

Parallelism reduces additive latency but total response time is still constrained by slow required dependencies.

Conceptually:

```text
SEQUENTIAL:

100ms + 200ms + 300ms
=
600ms

PARALLEL:

max(100ms, 200ms, 300ms)
≈
300ms
```

excluding overhead.

---

# 135. Parallel Failure

Failure semantics for concurrent operations MUST be explicit.

---

# 136. Fail Fast

If one mandatory dependency failure makes the response impossible, sibling work SHOULD be cancelled where practical.

---

# 137. Partial Enrichment

Optional enrichment MAY permit partial degradation if explicitly supported by the API contract.

---

# 138. Backpressure

Systems MUST prevent producers from generating work faster than downstream resources can safely consume it.

---

# 139. Backpressure Mechanisms

Applicable mechanisms include:

```text
Bounded Queue

Concurrency Limit

Batch Size

Rate Limit

Consumer Poll Size

Connection Pool

Admission Control
```

---

# 140. Unbounded Queue

Unbounded in-memory work queues are prohibited for production request/batch processing.

---

# 141. Memory Is Not Backpressure

Allowing memory usage to grow until the JVM fails is not a backpressure strategy.

---

# 142. Streaming

Streaming MAY be appropriate for large sequential result processing.

---

# 143. Streaming Is Not Pagination Replacement

Streaming large results through HTTP does not automatically make an unbounded query safe.

---

# 144. Database Streaming

Database streaming MUST account for:

```text
Open Connection Duration

Transaction Duration

Fetch Size

Client Consumption Speed

Cancellation
```

---

# 145. Long-Lived Connection

Long-running exports SHOULD NOT unnecessarily monopolize transactional connection pools.

---

# 146. Export

Large exports SHOULD generally use asynchronous job processing.

---

# 147. Export Threshold

Services SHOULD define when an interactive request becomes an asynchronous export.

---

# 148. Asynchronous Export

Canonical flow:

```text
POST /exports
      |
      v
202 ACCEPTED
      |
      v
BACKGROUND JOB
      |
      v
GENERATE FILE
      |
      v
OBJECT STORAGE
      |
      v
DOWNLOAD REFERENCE
```

---

# 149. Export Request

Export creation SHOULD capture immutable filter criteria.

---

# 150. Export Status

An asynchronous export SHOULD expose status such as:

```text
PENDING

PROCESSING

COMPLETED

FAILED

EXPIRED
```

---

# 151. Export Idempotency

Repeated equivalent export creation MAY use idempotency semantics when appropriate.

---

# 152. Export Storage

Generated files SHOULD use appropriate object/file storage rather than remaining indefinitely in application memory.

---

# 153. Export Expiration

Export artifacts SHOULD have a retention/expiration policy.

---

# 154. Export Authorization

Downloading an export MUST enforce authorization.

Possession of an export identifier alone MUST NOT grant access.

---

# 155. Sensitive Export

Exports containing sensitive data MUST follow data-governance and encryption requirements.

---

# 156. CSV/Excel Generation

Large document generation SHOULD use streaming/write-efficient techniques rather than constructing the entire file representation in memory where avoidable.

---

# 157. Memory Bound

Bulk/export processing MUST have a bounded memory model.

---

# 158. Chunk Processing

Large background workloads SHOULD process data in chunks.

Conceptually:

```text
READ 500
   |
PROCESS
   |
WRITE
   |
READ NEXT 500
```

rather than:

```text
LOAD 5,000,000
INTO MEMORY
```

---

# 159. Chunk Size

Chunk size MUST be configurable within validated bounds when operational tuning is required.

---

# 160. Batch Job Restart

Long-running batch operations SHOULD support restart/recovery where business requirements justify it.

---

# 161. Transaction per Chunk

Batch processing SHOULD use transaction boundaries appropriate to chunk size and recovery requirements.

---

# 162. JDBC

JDBC SHOULD be considered when:

```text
High-Volume Read/Write

Simple Tabular Mapping

Batch Operations

JPA Overhead Is Material
```

---

# 163. JPA vs JDBC

Decision:

```text
NEED DOMAIN ENTITY
LIFECYCLE / ORM?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
 JPA   READ/WRITE
       VOLUME HIGH?
          |
       +--+--+
       |     |
      YES    NO
       |     |
       v     v
     JDBC   DTO
            PROJECTION
```

---

# 164. JPA

JPA SHOULD remain the default for ordinary aggregate persistence where ORM semantics add value.

---

# 165. Projection

DTO projection SHOULD be the first optimization considered for read-only JPA queries that do not need complete entities.

---

# 166. Native SQL

Native SQL SHOULD be considered when ORM-generated SQL cannot efficiently express the required operation.

---

# 167. JDBC Batch

JDBC batching SHOULD be considered for high-volume writes where ORM lifecycle features are unnecessary.

---

# 168. Bulk JPA Mutation

Bulk JPQL/native updates bypass normal persistence-context entity lifecycle semantics.

Their use MUST account for stale managed entities.

---

# 169. Persistence Context

After bulk update/delete, the persistence context MUST be cleared/refreshed appropriately where stale state is possible.

---

# 170. Save in Loop

This pattern SHOULD be reviewed for large workloads:

```text
FOR EACH ITEM
    repository.save(item)
```

---

# 171. Batch Write

Batch writes SHOULD use appropriate Hibernate/JDBC batching where large write volumes justify it.

---

# 172. Flush

Large ORM write loops SHOULD control flush/clear behavior to avoid unbounded persistence-context growth.

---

# 173. Performance Measurement

Optimization MUST be evidence-driven.

---

# 174. Baseline

Critical endpoint optimization SHOULD begin with a measurable baseline.

---

# 175. Metrics

Performance-sensitive APIs SHOULD observe applicable:

```text
Request Latency

Database Latency

Query Count

Rows Returned

Payload Size

Downstream Calls

Downstream Latency

Timeouts

Pool Saturation
```

---

# 176. Percentiles

Latency SHOULD be evaluated using percentiles such as:

```text
p50

p95

p99
```

rather than averages alone.

---

# 177. Average Latency

Average latency can hide tail problems and MUST NOT be the only performance indicator.

---

# 178. Query Logging

Slow-query logging SHOULD be enabled according to environment and operational requirements.

---

# 179. SQL Logging

Full SQL logging in production MUST be carefully controlled because of volume and sensitive-data risk.

---

# 180. Query Tagging

Database queries MAY include safe application/request context for diagnostics where supported.

---

# 181. Load Testing

Critical high-volume endpoints SHOULD receive representative load tests.

---

# 182. Representative Dataset

Performance tests MUST use data volumes sufficiently representative of production behavior.

---

# 183. Small Dataset Fallacy

A query performing well with:

```text
100 rows
```

does not prove acceptable behavior with:

```text
100,000,000 rows
```

---

# 184. Performance Regression

Material performance behavior SHOULD be regression-tested where feasible.

---

# 185. Query Count Test

Tests MAY assert bounded query counts for known N+1-sensitive scenarios.

---

# 186. Pagination Test

Pagination tests SHOULD verify:

```text
Default Size

Maximum Size

Stable Sort

Next Page

Empty Page

Invalid Parameters
```

---

# 187. Sort Security Test

Tests MUST verify unsupported sort fields cannot manipulate query construction.

---

# 188. Filter Test

Dynamic filters SHOULD test combinations relevant to business behavior.

---

# 189. Bulk Limit Test

Bulk endpoints MUST test maximum item limits.

---

# 190. Duplicate Bulk Test

Bulk APIs SHOULD test duplicate identifiers.

---

# 191. Bulk Authorization Test

Authorization MUST be validated for every affected resource where required.

---

# 192. Batch Remote Test

Batch enrichment SHOULD test that one remote call can replace N individual calls where the integration supports batching.

---

# 193. Concurrency Test

Parallel loaders SHOULD test bounded concurrency.

---

# 194. No Thread.sleep

Concurrency tests SHOULD avoid `Thread.sleep` as a synchronization strategy.

---

# 195. Deterministic Concurrency Test

Latches, barriers, controlled futures or equivalent deterministic mechanisms SHOULD be preferred.

---

# 196. AssertJ

Java tests MUST follow established project quality conventions, including meaningful:

```java
.as("...")
```

descriptions before applicable AssertJ assertions.

---

# 197. Test Constants

Stable test identifiers SHOULD use centralized test constants where project conventions require them rather than random UUID generation.

---

# 198. Performance Architecture Review

Material data-retrieval changes SHOULD evaluate:

```text
[ ] Is pagination required?

[ ] Is page size bounded?

[ ] Is Page really needed?

[ ] Could Slice remove count cost?

[ ] Is deep pagination expected?

[ ] Should keyset pagination be used?

[ ] Is sorting deterministic?

[ ] Are sort fields allowlisted?

[ ] Are filters bounded?

[ ] Can the query use indexes?

[ ] Are complete entities necessary?

[ ] Could a DTO projection reduce work?

[ ] Is N+1 possible?

[ ] Is a collection fetch join combined with pagination?

[ ] How many SQL queries execute per request?

[ ] Are remote calls happening per row?

[ ] Can remote calls be batched?

[ ] Is concurrency bounded?

[ ] What is the maximum fan-out?

[ ] What is the maximum payload size?

[ ] Should this operation be asynchronous?

[ ] Would JDBC/native SQL materially improve this workload?

[ ] Is the query timeout bounded?

[ ] Can the workload exhaust the connection pool?
```

---

# 199. Fitness Functions

Stable performance rules SHOULD be automated where practical.

Examples:

```text
[ ] Collection endpoints have bounded pagination

[ ] Public page size has maximum limit

[ ] Sort fields pass through approved resolver

[ ] No controller exposes unrestricted Sort directly to persistence

[ ] Read projections do not unnecessarily load entity graphs

[ ] Known N+1 queries have regression tests

[ ] Bulk endpoints have maximum item count

[ ] Remote batch loaders have bounded concurrency

[ ] Export endpoints do not synchronously return unlimited datasets

[ ] Query timeouts exist for expensive operations
```

---

# 200. Enterprise Performance Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] Growing table exposed through unbounded findAll

[ ] Caller can request arbitrary page size

[ ] Deep pagination is known to be pathological and unaddressed

[ ] Sort values are concatenated into SQL

[ ] Search loads full aggregates only to return a small header DTO

[ ] Serialization triggers lazy database queries

[ ] N+1 query exists on critical list endpoint

[ ] One API request causes unbounded remote fan-out

[ ] Bulk payload size is unlimited

[ ] Virtual Threads are used with unlimited downstream concurrency

[ ] Export loads entire large dataset into heap

[ ] Expensive queries have no timeout

[ ] Query design is not evaluated against production-scale data
```

---

# 201. Anti-Patterns

The following are prohibited or strongly discouraged:

- unbounded `findAll()`
- caller-controlled unlimited page size
- deep offset pagination without evaluation
- unstable pagination ordering
- exact count query when caller does not need totals
- arbitrary entity-field filtering
- arbitrary raw sorting
- SQL sort concatenation
- loading complete entities for small read projections
- solving N+1 by making every relationship EAGER
- collection fetch join with pagination without validating generated behavior
- lazy queries triggered during JSON serialization
- indexes added blindly to every column
- remote call per result row
- parallelizing an N+1 integration instead of batching it
- unbounded Virtual Thread fan-out
- unbounded queues
- unlimited bulk requests
- one transaction containing enormous bulk operations
- synchronous generation of massive exports
- loading complete exports into JVM memory
- ORM used dogmatically for every high-volume workload
- native SQL rejected solely because it is native
- performance optimization without measurement
- relying only on average latency
- load testing with unrealistically small datasets

---

# 202. Positive Consequences

The decision provides:

- bounded API workloads
- predictable pagination
- lower database load
- reduced N+1 problems
- smaller memory footprint
- smaller payloads
- safer sorting
- better query plans
- reduced remote fan-out
- safer bulk operations
- controlled concurrency
- scalable exports
- clearer criteria for JPA versus JDBC
- better performance diagnostics

---

# 203. Negative Consequences

The decision introduces:

- explicit pagination design
- query-specific projections
- additional repository methods
- index analysis
- query-count testing
- bulk-limit governance
- asynchronous export infrastructure
- performance measurement requirements

These costs are accepted because data-access inefficiency compounds rapidly as production data and traffic increase.

---

# 204. Neutral Consequences

The decision also means:

- `Page` is not always better than `Slice`
- JPA is not always better than JDBC
- native SQL is not inherently undesirable
- projections are not required for every query
- keyset pagination is not required for every endpoint
- parallelism is not the first optimization
- streaming is not automatically memory-safe
- batching is often more valuable than concurrency
- exact totals are not free

---

# 205. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| N+1 query | High | High | Projection/fetch strategy/tests |
| Deep pagination | High | Medium | Keyset pagination |
| Query explosion | Critical | Medium | Query count monitoring |
| Remote fan-out | High | High | Batch APIs |
| Memory exhaustion | Critical | Medium | Pagination/chunking |
| Pool exhaustion | Critical | Medium | Bounded concurrency |
| Slow count query | Medium/High | High | Slice/optimized count |
| Expensive sort | High | Medium | Allowlist + indexes |
| Oversized bulk request | High | Medium | Maximum batch size |
| Large export timeout | High | High | Async export |

---

# 206. Implementation Guidance

The following rules are mandatory:

1. Growing collection endpoints must use bounded retrieval.
2. Pagination must define default and maximum page sizes.
3. Exact totals must only be calculated when the contract requires them.
4. `Slice` should replace `Page` when exact totals are unnecessary.
5. Deep pagination must evaluate keyset/cursor alternatives.
6. Pagination ordering must be deterministic.
7. Public filters must be explicitly defined.
8. Expensive filter ranges must be bounded where appropriate.
9. Sort fields must use an allowlist/resolver.
10. Raw caller sort expressions must never be concatenated into SQL.
11. Read-only searches should use projections when full entities are unnecessary.
12. N+1 patterns must be eliminated from critical endpoints.
13. EAGER loading must not be the default N+1 solution.
14. Fetch joins with pagination must be carefully validated.
15. JSON serialization must not trigger uncontrolled lazy queries.
16. Index design must follow actual query patterns.
17. Expensive queries must have bounded execution time.
18. Bulk APIs must define maximum item counts.
19. Bulk transaction semantics must be explicit.
20. Batch database retrieval must replace per-item retrieval where semantics permit.
21. Batch remote APIs must replace remote N+1 calls where available.
22. Remote fan-out must be bounded.
23. Batching must be considered before parallelism.
24. Virtual Threads must not create unlimited downstream concurrency.
25. Backpressure must use explicit bounds.
26. Large exports should use asynchronous processing.
27. Large datasets must be processed in bounded chunks.
28. JPA should be used when ORM/domain lifecycle adds value.
29. DTO projection should be considered for read-heavy queries.
30. Native SQL/JDBC should be used when materially better suited to the workload.
31. Large ORM writes must control persistence-context growth.
32. Performance optimization must use measurable evidence.
33. Critical endpoints should observe latency, query count, payload and downstream fan-out.
34. Performance testing must use representative data volumes.
35. Concurrency tests must remain deterministic.
36. Tests must follow established AssertJ/Sonar conventions.

---

# 207. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- JDBC
- HikariCP
- Virtual Threads
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- JaCoCo
- SonarQube
- SAST
- SQL execution plans
- database metrics
- load testing
- architecture fitness functions

---

# 208. Success Criteria

The decision is successful when:

- unbounded collection endpoints are eliminated
- page sizes are consistently bounded
- unnecessary count queries decrease
- large searches use stable pagination
- list endpoints load only required data
- N+1 regressions are detected
- query counts remain predictable
- remote calls per result row decrease
- batch APIs replace unnecessary fan-out
- concurrency remains within dependency capacity
- large exports no longer consume request threads/connections for extended periods
- large workloads remain memory bounded
- database latency remains predictable as datasets grow
- JPA/JDBC decisions reflect workload rather than convention

---

# 209. Alternatives Rejected

## 209.1 Always Use Page

Rejected because exact count queries may be unnecessary and expensive.

---

## 209.2 Always Use Offset Pagination

Rejected because deep pagination can degrade significantly on large datasets.

---

## 209.3 Always Load Entities

Rejected because read-only projections can materially reduce database and JVM work.

---

## 209.4 Make Everything EAGER

Rejected because it replaces one performance problem with uncontrolled over-fetching.

---

## 209.5 Parallelize Every Remote Call

Rejected because concurrency does not eliminate fan-out and can overload dependencies.

---

## 209.6 Always Use JPA

Rejected because high-volume specialized workloads may be better served by projections, native SQL or JDBC.

---

## 209.7 Synchronous Large Export

Rejected because large exports have fundamentally different latency and resource characteristics from interactive API requests.

---

# 210. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard

---

# 211. References

- Java 21 Documentation
- Spring Boot Documentation
- Spring Data JPA Documentation
- Hibernate ORM Documentation
- PostgreSQL Documentation
- HikariCP Documentation
- Jakarta Persistence Specification
- REST API Design Guidance
- AWS Well-Architected Performance Efficiency Pillar
- OWASP API Security
- Martin Fowler — Pagination
- Use The Index, Luke
- PostgreSQL EXPLAIN Documentation

---

# 212. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise API data retrieval and bulk-processing baseline |

---

# 213. Decision Summary

Collection retrieval becomes:

```text
REQUEST
   |
   v
FILTER VALIDATION
   |
   v
SORT ALLOWLIST
   |
   v
BOUNDED PAGINATION
   |
   v
OPTIMIZED QUERY
   |
   v
PROJECTION
   |
   v
BOUNDED RESPONSE
```

Pagination selection becomes:

```text
NEED EXACT TOTAL?
       |
    +--+--+
    |     |
   YES    NO
    |     |
    v     v
   PAGE  SLICE
```

Deep traversal becomes:

```text
OFFSET PAGINATION
       |
       v
OFFSET BECOMES LARGE?
       |
    +--+--+
    |     |
   NO    YES
    |     |
    v     v
 KEEP   KEYSET /
        CURSOR
```

Read-model selection becomes:

```text
QUERY
  |
  v
NEED DOMAIN ENTITY
BEHAVIOR?
  |
+-+----------------+
|                  |
YES                NO
|                  |
v                  v
JPA ENTITY      NEED FEW
                COLUMNS?
                   |
                +--+--+
                |     |
               YES    NO
                |     |
                v     v
             DTO      EVALUATE
          PROJECTION   QUERY
```

Persistence technology becomes:

```text
ORDINARY DOMAIN
PERSISTENCE
     |
     v
    JPA

READ-HEAVY
SUBSET
     |
     v
PROJECTION

COMPLEX DATABASE
OPTIMIZATION
     |
     v
NATIVE SQL

HIGH-VOLUME
TABULAR/BATCH
     |
     v
JDBC
```

N+1 prevention becomes:

```text
1 PAGE
   |
   v
N ITEMS
   |
   X
DO NOT:
N DATABASE QUERIES
OR
N REMOTE CALLS

   |
   v
BATCH / PROJECTION /
FETCH STRATEGY
```

Remote optimization order becomes:

```text
REMOVE CALL
    |
    v
BATCH CALLS
    |
    v
CACHE WHEN VALID
    |
    v
PARALLELIZE
REMAINING CALLS
    |
    v
BOUND CONCURRENCY
```

Virtual Thread usage becomes:

```text
1000 CHEAP
VIRTUAL THREADS
      |
      X
      |
DOES NOT MEAN
      |
      v
1000 SIMULTANEOUS
CALLS TO A SERVICE
THAT SUPPORTS 20
```

Bulk processing becomes:

```text
BULK REQUEST
     |
     v
VALIDATE MAX SIZE
     |
     v
VALIDATE DUPLICATES
     |
     v
BATCH LOAD
     |
     v
VALIDATE BUSINESS
RULES
     |
     v
BOUNDED PROCESSING
     |
     v
EXPLICIT RESULT
SEMANTICS
```

Large exports become:

```text
CLIENT
  |
  v
CREATE EXPORT
  |
  v
202 ACCEPTED
  |
  v
BACKGROUND JOB
  |
  v
CHUNKED QUERY
  |
  v
STREAM FILE
  |
  v
OBJECT STORAGE
  |
  v
AUTHORIZED DOWNLOAD
```

Performance diagnostics become:

```text
API LATENCY
     |
     +--> DATABASE LATENCY
     |
     +--> QUERY COUNT
     |
     +--> ROW COUNT
     |
     +--> DOWNSTREAM CALL COUNT
     |
     +--> DOWNSTREAM LATENCY
     |
     +--> PAYLOAD SIZE
     |
     +--> POOL SATURATION
```

The complete performance equation is:

```text
BOUNDED PAGINATION
        +
DETERMINISTIC SORTING
        +
CONTROLLED FILTERING
        +
CORRECT INDEXING
        +
LEAN PROJECTIONS
        +
NO N+1
        +
BATCH RETRIEVAL
        +
BOUNDED FAN-OUT
        +
BOUNDED CONCURRENCY
        +
BACKPRESSURE
        +
QUERY TIMEOUTS
        +
CHUNKED PROCESSING
        +
ASYNC LARGE WORKLOADS
        +
REPRESENTATIVE MEASUREMENT
        =
SCALABLE DATA-INTENSIVE APIs
```

The governing principle is:

```text
Bound every request.

Bound every page.

Bound every bulk payload.

Bound every query.

Bound every remote fan-out.

Bound every concurrency level.

Use Page only when
the total is worth its cost.

Use Slice when
hasNext is enough.

Use keyset pagination
when offsets become deep.

Do not load an aggregate
to return five fields.

Do not solve N+1
by loading everything.

Do not call another service
once for every row.

Batch before parallelizing.

Cache only when semantics allow it.

Virtual Threads reduce
thread cost.

They do not increase
database or downstream capacity.

Use JPA where ORM helps.

Use projections where
read models are smaller.

Use native SQL where
the database can do the job better.

Use JDBC where high-volume
tabular processing justifies it.

Move massive exports
out of interactive requests.

Process large datasets
in bounded chunks.

Measure p95 and p99,
not averages alone.

Test with production-like
data volumes.

And remember:

performance problems are
usually architecture problems
long before they become
CPU problems.
```
