# ADR-081: Adopt Enterprise Search, Indexing, OpenSearch/Elasticsearch, Full-Text Search and Read Model Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-081 |
| Title | Adopt Enterprise Search, Indexing, OpenSearch/Elasticsearch, Full-Text Search and Read Model Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Search, OpenSearch, Elasticsearch, Indexing, CQRS, Read Models |
| Related Work Items | OpenSearch, SQS, Outbox, PostgreSQL, Search APIs |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise systems frequently require searches that exceed the capabilities or desirable workload profile of ordinary transactional database queries.

Typical requirements include:

```text
FULL-TEXT SEARCH

PARTIAL TEXT SEARCH

MULTI-FIELD SEARCH

FACETS

AGGREGATIONS

RELEVANCE RANKING

AUTOCOMPLETE

FUZZY SEARCH

LARGE FILTERED RESULT SETS

SEARCH ACROSS DENORMALIZED DATA
```

A transactional PostgreSQL database remains appropriate for many:

```text
GET BY ID

EXACT FILTERS

BOUNDED PAGINATION

TRANSACTIONAL QUERIES
```

but attempting to make every advanced search requirement execute directly against the transactional database can create:

```text
Complex SQL

Expensive JOINs

Wildcard Scans

Database CPU Pressure

Index Proliferation

Slow Queries

Transactional Workload Contention
```

Search engines such as OpenSearch or Elasticsearch provide specialized capabilities but introduce:

```text
ANOTHER DATA STORE

EVENTUAL CONSISTENCY

INDEX SYNCHRONIZATION

SCHEMA EVOLUTION

REINDEXING

OPERATIONAL COMPLEXITY
```

Therefore, search infrastructure must be treated as a derived read model rather than casually introduced as another authoritative database.

---

# 2. Problem Statement

The organization requires standards covering:

- OpenSearch
- Elasticsearch
- full-text search
- search APIs
- index design
- mappings
- analyzers
- tokenization
- normalization
- keyword fields
- text fields
- filters
- sorting
- relevance
- aggregations
- pagination
- deep pagination
- `search_after`
- Point in Time
- aliases
- zero-downtime reindexing
- event-driven indexing
- Transactional Outbox
- CDC
- bulk indexing
- retry
- DLQ
- duplicate events
- ordering
- rebuild
- eventual consistency
- security
- PII
- multi-tenancy
- observability
- testing

---

# 3. Decision Drivers

Primary drivers are:

1. search performance
2. transactional database protection
3. advanced search capability
4. scalability
5. schema evolution
6. recoverability
7. predictable consistency
8. security
9. operational visibility
10. zero-downtime deployment
11. maintainability
12. deterministic rebuild capability

---

# 4. Decision

OpenSearch/Elasticsearch MAY be introduced when application search requirements materially benefit from a dedicated search engine.

The search index MUST be treated as:

```text
DERIVED READ MODEL
```

rather than:

```text
AUTHORITATIVE BUSINESS STORE
```

The preferred architecture is:

```text
WRITE REQUEST
      |
      v
TRANSACTIONAL SERVICE
      |
      v
POSTGRESQL
      |
      +
      |
      v
OUTBOX / CDC / EVENT
      |
      v
INDEXER
      |
      v
OPENSEARCH
```

Search requests then follow:

```text
CLIENT
   |
   v
SEARCH API
   |
   v
OPENSEARCH
```

---

# 5. Fundamental Principle

```text
The database owns truth.

The search engine owns
a searchable projection
of that truth.

Search may be eventually
consistent.

The index must always
be rebuildable.
```

---

# 6. Source of Truth

Every search index MUST have an authoritative source.

Typical source:

```text
PostgreSQL
```

---

# 7. Search as Source of Truth

OpenSearch MUST NOT become the only persistent representation of critical business data unless a separate explicit architectural decision establishes that responsibility.

---

# 8. Derived Data

Search documents SHOULD contain only information necessary to satisfy search/read requirements.

---

# 9. Denormalization

Search documents MAY intentionally denormalize information.

Example:

```text
ORDER
 |
 +--> CUSTOMER NAME
 |
 +--> DEALER NAME
 |
 +--> STATUS
 |
 +--> PRODUCT DESCRIPTION
```

---

# 10. Denormalization Trade-Off

Denormalization improves read performance but increases synchronization complexity.

---

# 11. Search Boundary

Search-engine adoption SHOULD require a demonstrated need.

---

# 12. PostgreSQL First

Simple exact queries SHOULD generally remain in PostgreSQL.

---

# 13. Search Engine Justification

OpenSearch becomes appropriate when requirements include material use of:

```text
Full Text

Relevance

Faceting

Fuzzy Matching

Large Denormalized Search

High Search Volume
```

---

# 14. Search API

Clients SHOULD access search through application APIs rather than directly querying OpenSearch.

---

# 15. Direct Client Access

Frontend applications MUST NOT receive unrestricted OpenSearch credentials.

---

# 16. Application Boundary

The application search API owns:

```text
Authorization

Query Validation

Business Filters

Pagination Rules

Result Contract
```

---

# 17. Index Naming

Index names MUST follow a predictable naming convention.

Example:

```text
orders-search-v3
```

---

# 18. Physical vs Logical Name

Applications SHOULD access logical aliases rather than hardcoded physical index versions.

Example:

```text
orders-search
       |
       v
orders-search-v3
```

---

# 19. Read Alias

A stable read alias SHOULD identify the active search index.

---

# 20. Write Alias

A write alias MAY be used when index-write architecture benefits from it.

---

# 21. Index Version

Breaking mapping changes SHOULD create a new physical index version.

---

# 22. Mapping

Production mappings MUST be explicit for business-critical indices.

---

# 23. Dynamic Mapping

Unrestricted dynamic mapping SHOULD NOT be relied upon for critical search schemas.

---

# 24. Mapping Explosion

Uncontrolled dynamic fields can create mapping explosion.

---

# 25. Field Count

Index field counts MUST remain bounded.

---

# 26. Arbitrary JSON

Arbitrary user-defined JSON SHOULD NOT automatically become individually indexed fields.

---

# 27. Text Field

Fields requiring full-text analysis SHOULD use appropriate `text` mapping.

---

# 28. Keyword Field

Fields requiring:

```text
Exact Match

Sorting

Aggregation

Grouping
```

SHOULD generally use `keyword` semantics.

---

# 29. Multi-Field

A field MAY require both:

```text
text
```

and:

```text
keyword
```

representations.

---

# 30. Example

Conceptually:

```text
customerName
    |
    +--> text
    |
    +--> keyword
```

when both full-text search and exact sorting/filtering are required.

---

# 31. Identifier

Business identifiers SHOULD generally use exact-match mappings.

---

# 32. UUID

UUIDs SHOULD NOT be analyzed as natural-language text.

---

# 33. Enum

Enum/status values SHOULD generally use exact-match fields.

---

# 34. Numeric Field

Numeric data MUST use numeric mappings rather than text.

---

# 35. Date Field

Dates MUST use explicit date mappings/formats.

---

# 36. Money

Monetary search/sort fields SHOULD use a representation that preserves required numeric semantics.

---

# 37. Floating Point

Floating-point representation SHOULD NOT introduce unacceptable financial comparison errors.

---

# 38. Analyzer

Analyzers MUST be selected according to language and search semantics.

---

# 39. Analyzer Is Contract

Changing an analyzer can materially change search results and normally requires reindexing.

---

# 40. Lowercase

Case normalization MAY be applied where business search semantics are case-insensitive.

---

# 41. Accent Handling

Accent folding MAY be used where users expect:

```text
JOAO
```

to match:

```text
JOÃO
```

---

# 42. Accent Trade-Off

Accent normalization MUST not destroy distinctions required by the business domain.

---

# 43. Language Analyzer

Language-specific analyzers MAY improve:

```text
Stemming

Stop Words

Tokenization
```

but MUST be tested with actual domain terminology.

---

# 44. Business Vocabulary

Product codes, model numbers, customer identifiers and abbreviations frequently require different analysis from natural-language descriptions.

---

# 45. Search Relevance

Relevance ranking MUST be treated as application behavior.

---

# 46. Relevance Testing

Changes to:

```text
Boosts

Analyzers

Field Weights

Fuzziness
```

SHOULD have representative relevance tests.

---

# 47. Exact Filter vs Full Text

Exact business constraints SHOULD use filters rather than full-text scoring queries.

---

# 48. Filter

Examples:

```text
segment = "M&M"

status = "APPROVED"

customerId = UUID
```

should normally use exact filtering.

---

# 49. Query Context

Scoring queries SHOULD be reserved for fields where relevance matters.

---

# 50. Authorization Filter

Authorization restrictions MUST be applied independently of relevance ranking.

---

# 51. Security Filter

Search MUST never return a document merely because it scored highly if the principal is not authorized to see it.

---

# 52. Tenant Filter

Multi-tenant searches MUST enforce tenant isolation server-side.

---

# 53. Client Tenant

A client-provided tenant identifier MUST NOT be trusted without authorization validation.

---

# 54. Search Injection

User input MUST NOT be concatenated into unrestricted query DSL.

---

# 55. Query DSL Exposure

Public APIs SHOULD NOT expose arbitrary OpenSearch JSON DSL unless the endpoint is explicitly trusted and controlled.

---

# 56. Query Builder

Search queries SHOULD be constructed from validated application-level search parameters.

---

# 57. Allowed Filters

Search APIs SHOULD explicitly allow supported filter fields/operators.

---

# 58. Arbitrary Field Search

Clients SHOULD NOT be allowed to search arbitrary internal index fields.

---

# 59. Expensive Queries

Potentially expensive query features MUST be bounded.

Examples:

```text
Leading Wildcard

Regex

Fuzzy Search

Large Terms Query

Huge Aggregation

Script Query
```

---

# 60. Leading Wildcard

Queries such as:

```text
*abc
```

SHOULD be avoided or tightly controlled on large datasets.

---

# 61. Regex

User-controlled regular-expression searches SHOULD be prohibited or tightly bounded.

---

# 62. Script Query

Dynamic scripts from untrusted clients MUST NOT be accepted.

---

# 63. Search Limits

Every search endpoint MUST define maximum:

```text
Page Size

Filter Count

Sort Count

Aggregation Scope

Query Length
```

where applicable.

---

# 64. Pagination

Search APIs MUST use bounded pagination.

---

# 65. `from` + `size`

`from` + `size` MAY be used for shallow pagination.

---

# 66. Deep Pagination

Large `from` offsets SHOULD NOT be used.

---

# 67. Example

Avoid:

```text
from = 500000
size = 100
```

---

# 68. Deep Pagination Cost

Deep pagination can require search nodes to collect/sort large numbers of results only to discard most of them.

---

# 69. `search_after`

Deep traversal SHOULD use:

```text
search_after
```

with deterministic sorting.

---

# 70. Stable Sort

`search_after` requires a stable deterministic sort.

---

# 71. Tie-Breaker

Sort order SHOULD include a unique tie-breaker.

Example:

```text
createdAt DESC
id DESC
```

---

# 72. Point in Time

Point in Time (PIT) SHOULD be considered when a consistent search view is required across multiple pages.

---

# 73. PIT Lifetime

PIT lifetime MUST be bounded.

---

# 74. PIT Resource Cost

Large numbers of long-lived PIT contexts can consume cluster resources.

---

# 75. Scroll

Scroll APIs SHOULD primarily be reserved for controlled batch/export processing rather than ordinary interactive pagination.

---

# 76. Search Result Limit

Interactive search SHOULD define a practical maximum traversal depth.

---

# 77. Export

Exporting an entire search result SHOULD follow asynchronous export standards rather than unrestricted interactive pagination.

---

# 78. Sorting

Only explicitly supported fields SHOULD be sortable.

---

# 79. Text Sorting

Analyzed `text` fields SHOULD NOT be used directly for sorting.

---

# 80. Keyword Sort

A keyword/raw representation SHOULD be used where textual sorting is required.

---

# 81. Aggregations

Aggregations MUST be bounded.

---

# 82. High-Cardinality Aggregation

Aggregating millions of unique identifiers can consume substantial resources.

---

# 83. User-Controlled Aggregation

Clients SHOULD NOT have unrestricted access to arbitrary aggregation definitions.

---

# 84. Source Filtering

Search responses SHOULD retrieve only fields required by the API.

---

# 85. Large Document

Large unused document fields SHOULD not be transferred for every search result.

---

# 86. `_source`

Source filtering SHOULD reduce unnecessary network and deserialization cost.

---

# 87. Search Document Size

Search documents SHOULD remain intentionally bounded.

---

# 88. Binary Data

Large binary content MUST NOT be embedded into search documents.

---

# 89. Large Text

Very large text fields SHOULD only be indexed when actual search requirements justify them.

---

# 90. Index Synchronization

The index MUST be synchronized from authoritative business changes through a reliable mechanism.

---

# 91. Preferred Mechanisms

Supported patterns include:

```text
TRANSACTIONAL OUTBOX

CDC

DURABLE INTEGRATION EVENT
```

---

# 92. Unsafe Dual Write

This is prohibited where consistency matters:

```text
SAVE POSTGRESQL
      |
      v
CALL OPENSEARCH
```

as unrelated operations.

---

# 93. Failure Window

```text
DATABASE COMMIT
      |
      X
PROCESS CRASH
      |
INDEX NEVER UPDATED
```

must be addressed.

---

# 94. Outbox

Transactional Outbox SHOULD be used when the service owns the business write and index-update event.

---

# 95. CDC

Change Data Capture MAY be used when database-level change streams provide a better integration boundary.

---

# 96. Domain Event

Indexing SHOULD use semantically meaningful events when domain changes determine projection content.

---

# 97. Event Identity

Index events MUST have stable identifiers.

---

# 98. Idempotency

Indexer consumers MUST be idempotent.

---

# 99. Duplicate Event

Processing the same indexing event twice MUST NOT create duplicate logical search documents.

---

# 100. Document ID

The search document ID SHOULD normally be derived from a stable business/entity identifier.

---

# 101. Upsert

Indexing SHOULD use deterministic upsert/replacement semantics where appropriate.

---

# 102. Delete Event

Deletion of authoritative data MUST have explicit index deletion semantics.

---

# 103. Soft Delete

If the source uses soft deletion, the search model MUST explicitly determine whether the document is:

```text
REMOVED FROM INDEX
```

or:

```text
INDEXED AS INACTIVE
```

---

# 104. Ordering

Events for the same entity may arrive out of order.

---

# 105. Example

```text
VERSION 12 UPDATE

VERSION 11 UPDATE
```

may be consumed in that order.

---

# 106. Version

Index documents SHOULD carry an applicable source version or last-modified sequence when ordering matters.

---

# 107. Stale Update

An older event MUST NOT overwrite a newer indexed state.

---

# 108. Delete Ordering

A delayed update MUST NOT resurrect an entity after a newer delete where version semantics prohibit it.

---

# 109. Partitioning

Messaging partition keys SHOULD preserve per-entity ordering where practical.

---

# 110. Global Ordering

Global ordering SHOULD NOT be required unless the business model genuinely needs it.

---

# 111. Eventual Consistency

Search results are generally eventually consistent with transactional state.

---

# 112. API Contract

This eventual consistency MUST be accepted as part of the search API semantics.

---

# 113. Write Then Search

Immediately after updating an entity:

```text
PUT /orders/{id}
```

a subsequent:

```text
GET /orders/search
```

may temporarily show older data.

---

# 114. Read-Your-Write

When immediate read-your-write consistency is mandatory, the API SHOULD read authoritative storage rather than forcing every search write to synchronously refresh OpenSearch.

---

# 115. Refresh

Forcing index refresh after every write SHOULD be avoided.

---

# 116. Refresh Cost

Aggressive refresh reduces indexing throughput and increases cluster cost.

---

# 117. Bulk Indexing

Indexer services SHOULD batch writes where throughput benefits justify it.

---

# 118. Bulk Size

Bulk request size MUST be bounded by:

```text
Document Count

Payload Bytes

Latency Budget
```

---

# 119. Giant Bulk

Extremely large bulk requests SHOULD NOT be used.

---

# 120. Partial Bulk Failure

Bulk responses MUST be inspected per item.

---

# 121. HTTP 200

A successful bulk HTTP response does NOT mean every document was indexed successfully.

---

# 122. Retry

Only retryable failed bulk items SHOULD be retried.

---

# 123. Retryable Index Failure

Examples MAY include:

```text
429

503

Temporary Node Failure
```

---

# 124. Permanent Index Failure

Examples MAY include:

```text
Mapping Conflict

Invalid Field

Malformed Document
```

---

# 125. Poison Document

A permanently invalid document MUST NOT be retried forever.

---

# 126. DLQ

Permanently failing index events SHOULD move to a DLQ or equivalent repair workflow.

---

# 127. DLQ Ownership

DLQ must have:

```text
Monitoring

Diagnosis

Correction

Replay
```

---

# 128. Retry Backoff

Transient indexing retries SHOULD use bounded backoff with jitter.

---

# 129. Retry Storm

Cluster overload MUST NOT trigger immediate aggressive retries from every indexer.

---

# 130. Backpressure

Indexing consumers MUST use bounded concurrency.

---

# 131. Queue Lag

If indexing throughput falls below event production:

```text
INDEX LAG
```

must become visible.

---

# 132. Index Lag

Search freshness SHOULD be measured.

---

# 133. Freshness Metric

Useful measure:

```text
current time
-
source event occurredAt
```

for recently processed indexing events.

---

# 134. Rebuild

Every derived search index MUST have a complete rebuild procedure.

---

# 135. Rebuild Requirement

The architecture MUST assume an index can be:

```text
CORRUPTED

DELETED

MISMAPPED

OUT OF SYNC

REPLACED
```

---

# 136. Rebuild Source

Rebuild MUST use authoritative source data.

---

# 137. Rebuild Must Not Depend on Old Index

A corrupted index MUST NOT be required to reconstruct its replacement.

---

# 138. Rebuild Flow

Preferred:

```text
SOURCE OF TRUTH
      |
      v
NEW INDEX VERSION
      |
      v
VALIDATE
      |
      v
ALIAS SWITCH
      |
      v
OLD INDEX RETIRE
```

---

# 139. Example

```text
orders-search-v7
        |
        v
REINDEX
        |
        v
VALIDATE
        |
        v
orders-search alias
        |
        v
orders-search-v8
```

---

# 140. Zero-Downtime Reindex

Breaking mapping/analyzer changes SHOULD use new-index creation plus alias switch.

---

# 141. In-Place Breaking Mapping

Breaking mapping changes SHOULD NOT rely on unsafe in-place mutation.

---

# 142. Reindex Validation

Before alias switch, validate:

```text
Document Count

Sample Queries

Required Fields

Mapping

Business Reconciliation

Index Health
```

---

# 143. Count Equality

Exact count equality alone is insufficient validation if business filtering affects indexed population.

---

# 144. Dual Write During Rebuild

Large rebuilds MAY require simultaneous event application to old/new indices or a catch-up strategy.

---

# 145. Race During Rebuild

Without catch-up:

```text
REBUILD START

SOURCE CHANGES

REBUILD FINISH

NEW INDEX MISSING CHANGES
```

can occur.

---

# 146. Rebuild Strategies

Possible strategies include:

```text
SNAPSHOT + EVENT CATCH-UP

DUAL INDEXING

HIGH-WATER MARK

CDC OFFSET
```

---

# 147. High-Water Mark

Rebuild SHOULD establish a deterministic point from which incremental changes can be replayed.

---

# 148. Alias Switch

Alias switching SHOULD be atomic where supported.

---

# 149. Rollback

The previous index SHOULD remain available for a bounded period when rapid rollback is valuable.

---

# 150. Old Index Cleanup

Old physical indices MUST eventually be removed according to retention/cost policy.

---

# 151. Index Templates

Reusable index settings/mappings SHOULD use controlled index templates where appropriate.

---

# 152. Shards

Shard count MUST be intentionally selected.

---

# 153. Too Many Shards

Excessive small shards create cluster overhead.

---

# 154. Too Few Shards

Insufficient shard distribution may limit scalability.

---

# 155. Shard Planning

Shard design SHOULD consider:

```text
Index Size

Growth

Node Count

Query Pattern

Write Throughput

Recovery Time
```

---

# 156. Replica

Replica count MUST reflect availability and search throughput requirements.

---

# 157. Replica Cost

Replicas increase:

```text
Storage

Indexing Work

Infrastructure Cost
```

---

# 158. Cluster Capacity

Capacity planning MUST include:

```text
Heap

Disk

CPU

Network

Shard Count

Query Rate

Index Rate

Retention
```

---

# 159. Disk Watermark

Disk saturation MUST be monitored.

---

# 160. JVM Heap

OpenSearch heap pressure MUST be monitored.

---

# 161. Query Concurrency

Application concurrency MUST NOT assume unlimited cluster search capacity.

---

# 162. Search Bulkhead

Search traffic MAY require bulkhead/concurrency controls to protect the cluster.

---

# 163. Timeout

Every application search request MUST have a bounded timeout.

---

# 164. Search Timeout

Application-level deadlines SHOULD align with search-engine query timeout where applicable.

---

# 165. Slow Search

Slow queries MUST NOT hold application threads/connections indefinitely.

---

# 166. Cancellation

Where supported, abandoned requests SHOULD avoid unnecessary continued downstream work.

---

# 167. Circuit Breaker

Application clients MAY use Circuit Breaker protection for search-cluster outages.

---

# 168. Search Outage

The application MUST define behavior when search is unavailable.

Possible responses:

```text
503 SERVICE UNAVAILABLE

LIMITED DATABASE FALLBACK

DEGRADED SEARCH
```

---

# 169. Database Fallback

Automatically rerouting arbitrary OpenSearch queries to PostgreSQL SHOULD NOT be done unless the database can safely support the workload.

---

# 170. Fallback Load Risk

A search outage must not become a transactional database outage.

---

# 171. Degraded Search

A deliberately limited fallback MAY support only:

```text
Exact ID

Small Exact Filters
```

rather than replicating full-text search in PostgreSQL.

---

# 172. Security

Search infrastructure MUST follow least privilege.

---

# 173. Credentials

OpenSearch credentials MUST use approved secret/identity management.

---

# 174. TLS

Traffic to the search cluster MUST use approved encryption in transit.

---

# 175. At-Rest Encryption

Sensitive search indices MUST use approved encryption at rest.

---

# 176. Network Access

Search clusters SHOULD be private/internal rather than publicly exposed.

---

# 177. Index Permission

Indexer and search API MAY have different permissions.

Example:

```text
SEARCH API = READ

INDEXER = WRITE
```

---

# 178. Delete Permission

Only components requiring index deletion SHOULD receive delete privileges.

---

# 179. Administrative API

Cluster administration MUST NOT be exposed through ordinary application endpoints.

---

# 180. PII

Search documents MUST follow data-minimization standards.

---

# 181. Search Copies PII

Indexing PII creates another stored copy subject to:

```text
Privacy

Retention

Deletion

Access Control

Audit
```

---

# 182. Unnecessary PII

Fields not required for search/result display SHOULD NOT be indexed.

---

# 183. Sensitive Field

Highly sensitive fields SHOULD not be indexed merely because they exist in the source entity.

---

# 184. `_source` Security

A field excluded from search may still exist in `_source`.

Therefore privacy review MUST consider stored source as well as indexed terms.

---

# 185. Disable/Filter Source

Source storage/filtering MAY be adjusted where appropriate, but rebuild/debug requirements must be considered.

---

# 186. Right to Deletion

Privacy deletion workflows MUST remove applicable personal data from search indices.

---

# 187. Reindex Privacy

Old index versions containing deleted personal data MUST also follow retention/deletion requirements.

---

# 188. Snapshots

Search snapshots/backups containing PII MUST follow retention/privacy controls.

---

# 189. Logging

Search request logging MUST avoid exposing sensitive query values unnecessarily.

---

# 190. Query PII

Users may search by:

```text
Email

Document Number

Customer Name
```

Therefore raw search queries can contain PII.

---

# 191. Slow Query Logs

Search-engine slow logs MUST also be privacy-reviewed.

---

# 192. Observability

Search architecture MUST be observable at application and cluster levels.

---

# 193. Application Metrics

Useful metrics include:

```text
search_requests

search_duration

search_errors

search_timeouts

result_count

index_events_processed

index_events_failed

index_lag

bulk_duration
```

---

# 194. Bounded Labels

Safe bounded labels MAY include:

```text
search_type

result

index_logical_name
```

---

# 195. Query Metric

Raw search query text MUST NOT be a metric label.

---

# 196. Document ID Metric

Document IDs MUST NOT be metric labels.

---

# 197. Cluster Metrics

Operational metrics SHOULD include:

```text
Cluster Health

Node Availability

Heap Pressure

Disk Usage

Shard Count

Rejected Requests

Search Latency

Indexing Latency

Refresh Time

Merge Time
```

---

# 198. Search Latency

Latency percentiles SHOULD be monitored.

---

# 199. Indexing Lag

Index freshness lag SHOULD have explicit monitoring.

---

# 200. DLQ Metric

Indexer DLQ growth MUST trigger operational visibility.

---

# 201. Rejected Requests

Thread-pool/search/indexing rejection metrics SHOULD be monitored.

---

# 202. Cluster Health

Persistent:

```text
YELLOW
```

or:

```text
RED
```

health must be operationally understood rather than ignored.

---

# 203. Logs

Application search logs SHOULD include safe:

```text
searchType

filtersCount

pageSize

sort

elapsedMs

resultCount
```

---

# 204. Full Query Logging

Complete OpenSearch DSL SHOULD NOT be logged by default in production.

---

# 205. Trace

Distributed tracing MAY include search client spans.

---

# 206. Trace Attributes

Safe attributes MAY include:

```text
db.system=opensearch

logicalIndex

operation
```

---

# 207. Query Content in Trace

Raw user query text SHOULD be excluded unless explicitly approved.

---

# 208. Testing Strategy

Search requires dedicated tests beyond repository mocking.

---

# 209. Mapping Test

Critical mappings SHOULD be validated against a real compatible search engine.

---

# 210. Analyzer Test

Analyzer behavior SHOULD test representative domain values.

---

# 211. Accent Test

Where accent folding is intended, verify cases such as:

```text
João
Joao
```

according to business requirements.

---

# 212. Exact Identifier Test

Identifiers MUST not be unintentionally tokenized.

---

# 213. Filtering Test

Exact filters MUST return deterministic expected results.

---

# 214. Authorization Test

Search MUST verify unauthorized documents cannot appear.

---

# 215. Tenant Isolation Test

Cross-tenant leakage MUST have explicit negative tests.

---

# 216. Pagination Test

`search_after` pagination MUST verify:

```text
No Duplicate Results

No Missing Results

Stable Ordering
```

for a stable dataset.

---

# 217. Tie-Breaker Test

Duplicate primary sort values MUST verify unique tie-breaker behavior.

---

# 218. PIT Test

PIT behavior SHOULD be tested where used.

---

# 219. Deep Pagination Test

APIs MUST reject or avoid unsupported deep offset pagination.

---

# 220. Expensive Query Test

Unsupported wildcard/regex/script operations SHOULD have rejection tests.

---

# 221. Indexer Idempotency Test

Processing the same event twice MUST produce one logical current document.

---

# 222. Ordering Test

Process:

```text
VERSION 2

then VERSION 1
```

and verify version 1 cannot overwrite version 2.

---

# 223. Delete Ordering Test

Delayed update MUST not resurrect a newer deleted document.

---

# 224. Bulk Partial Failure Test

Bulk indexing MUST test mixed:

```text
SUCCESS

RETRYABLE FAILURE

PERMANENT FAILURE
```

within one response.

---

# 225. DLQ Test

Poison documents MUST reach the controlled failure path after bounded attempts.

---

# 226. Rebuild Test

A search index SHOULD be reproducible from authoritative source data in integration/staging environments.

---

# 227. Alias Test

Reindex testing SHOULD verify alias switch behavior.

---

# 228. Zero-Downtime Test

Reads SHOULD continue through index replacement.

---

# 229. Search Outage Test

Application behavior during cluster unavailability MUST be tested.

---

# 230. Timeout Test

Search timeout/failure mapping MUST be deterministic.

---

# 231. Testcontainers

OpenSearch/Elasticsearch-compatible integration testing SHOULD use containerized real search infrastructure where practical.

---

# 232. Mock Limitation

Mocking the search client does not validate:

```text
Mapping

Analyzer

Query DSL

Sorting

Aggregation

search_after

Bulk Partial Failure
```

---

# 233. Representative Data

Search tests SHOULD include representative production-like vocabulary and data distribution without exposing real sensitive production data.

---

# 234. Relevance Regression

Business-critical relevance SHOULD have a regression dataset.

---

# 235. Performance Test

Critical search APIs SHOULD be load-tested with realistic:

```text
Index Size

Query Mix

Concurrency

Aggregation Patterns
```

---

# 236. AssertJ

Java tests MUST follow established project quality conventions, including meaningful:

```java
.as("...")
```

before applicable assertions.

---

# 237. Search Architecture Review Checklist

```text
[ ] Why is a search engine required?

[ ] Could PostgreSQL safely satisfy this query?

[ ] What is the source of truth?

[ ] Can the index be completely rebuilt?

[ ] What is the logical alias?

[ ] Is the physical index versioned?

[ ] Are mappings explicit?

[ ] Could dynamic fields cause mapping explosion?

[ ] Which fields are text?

[ ] Which fields are keyword?

[ ] Which analyzers are required?

[ ] Is relevance business-tested?

[ ] Are exact constraints implemented as filters?

[ ] Is authorization applied server-side?

[ ] Is tenant isolation guaranteed?

[ ] Can users submit arbitrary query DSL?

[ ] Are expensive query features bounded?

[ ] Is page size bounded?

[ ] Is deep pagination using search_after?

[ ] Is sorting deterministic?

[ ] Is a unique tie-breaker present?

[ ] Is PIT required?

[ ] How does the index receive updates?

[ ] Is dual-write failure avoided?

[ ] Is indexing idempotent?

[ ] Can events arrive out of order?

[ ] Can an old update overwrite a new one?

[ ] How are deletes indexed?

[ ] How are bulk partial failures handled?

[ ] Is DLQ operationally owned?

[ ] What is current indexing lag?

[ ] How is a complete rebuild performed?

[ ] How are changes caught up during rebuild?

[ ] Is alias switch tested?

[ ] What happens when search is unavailable?

[ ] Could fallback overload PostgreSQL?

[ ] Does the index contain PII?

[ ] Are old indices deleted?

[ ] Are search queries privacy-safe in logs?

[ ] Are cluster and application metrics available?
```

---

# 238. Search Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] Application uses logical search alias

[ ] Production mapping is explicitly versioned

[ ] Page size has a configured maximum

[ ] Deep pagination does not use huge from offsets

[ ] search_after includes deterministic tie-breaker

[ ] Search API does not expose unrestricted DSL

[ ] Search client has bounded timeout

[ ] Indexer is idempotent

[ ] Stale events cannot overwrite newer documents

[ ] Bulk response is inspected per item

[ ] Permanent indexing failures reach DLQ

[ ] Index rebuild procedure exists

[ ] Indexing lag has monitoring

[ ] PII fields are explicitly reviewed

[ ] Raw queries are excluded from metric labels
```

---

# 239. Enterprise Search Gate

A search implementation is not considered compliant when applicable conditions include:

```text
[ ] OpenSearch is the accidental only source of critical business data

[ ] Index cannot be rebuilt from authoritative data

[ ] Application hardcodes one physical index forever

[ ] Breaking mapping change is attempted unsafely in place

[ ] Dynamic mapping can create arbitrary field explosion

[ ] UUID/status fields are analyzed as natural language

[ ] Authorization relies on client-supplied filters

[ ] Client can execute unrestricted search DSL

[ ] Expensive wildcard/regex/script queries are unbounded

[ ] Interactive API allows unlimited page size

[ ] Deep pagination uses enormous from offsets

[ ] search_after sort is nondeterministic

[ ] Database + OpenSearch use unsafe dual write

[ ] Duplicate events create inconsistent documents

[ ] Old events can overwrite newer indexed state

[ ] Delete can be undone by delayed stale event

[ ] Bulk HTTP 200 is assumed to mean every item succeeded

[ ] Poison document retries forever

[ ] Indexing lag is invisible

[ ] Reindex has no catch-up strategy

[ ] Search outage automatically floods transactional database

[ ] Search index contains unnecessary sensitive data

[ ] Old PII-containing index versions remain forever

[ ] Raw user queries appear in metrics
```

---

# 240. Anti-Patterns

The following are prohibited or strongly discouraged:

- OpenSearch for every simple lookup
- search engine as accidental source of truth
- direct frontend access to cluster
- unrestricted dynamic mappings
- indexing arbitrary JSON fields
- analyzing identifiers
- unrestricted query DSL
- leading-wildcard searches over large datasets
- arbitrary regex/script queries
- unlimited aggregations
- huge `from + size`
- nondeterministic `search_after`
- synchronous database/OpenSearch dual writes
- no event idempotency
- no event version/order protection
- retrying mapping failures forever
- ignoring partial bulk failures
- DLQ without ownership
- index with no rebuild strategy
- breaking mapping change in place
- reindex without event catch-up
- hardcoded physical index name
- forced refresh after every write
- database fallback for arbitrary full-text workload
- unnecessary PII in index
- raw query text in metric labels
- treating mocks as sufficient search integration testing

---

# 241. Positive Consequences

The decision provides:

- scalable advanced search
- reduced transactional database search load
- explicit eventual-consistency semantics
- deterministic index rebuild
- zero-downtime schema evolution
- safer deep pagination
- stronger authorization boundaries
- controlled indexing retry
- improved search observability
- better operational recovery
- relevance governance
- safer privacy handling

---

# 242. Negative Consequences

The decision introduces:

- additional infrastructure
- duplicated data
- eventual consistency
- event/index pipelines
- schema/mapping governance
- reindex procedures
- cluster capacity management
- specialized testing
- additional security/privacy scope

These costs are accepted only where dedicated search capabilities materially justify them.

---

# 243. Neutral Consequences

The decision also means:

- PostgreSQL remains appropriate for many searches
- search results may briefly lag transactional writes
- denormalization is intentional
- not every text field needs full-text indexing
- not every query needs relevance scoring
- reindexing is a normal lifecycle operation
- aliases become part of deployment architecture
- index counts may legitimately differ from source row counts
- search availability and transactional availability can differ

---

# 244. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Index drift | High | Medium | Reliable events + reconciliation |
| Stale search results | Medium/High | Medium | Lag monitoring |
| Lost indexing event | High | Medium | Outbox/CDC |
| Out-of-order update | High | Medium | Version protection |
| Mapping failure | High | Medium | Explicit schema + DLQ |
| Search cluster overload | High | Medium | Query limits |
| DB overload during fallback | Critical | Medium | Limited fallback |
| PII duplication | Critical | Medium | Data minimization |
| Reindex data loss | Critical | Low/Medium | Catch-up strategy |
| Deep pagination overload | High | Medium | `search_after` |

---

# 245. Implementation Guidance

The following rules are mandatory:

1. Search engines must be introduced only for justified search workloads.
2. Transactional persistence must remain authoritative unless explicitly decided otherwise.
3. Every search index must be fully rebuildable.
4. Search clients must use application APIs rather than unrestricted cluster access.
5. Physical indices should be versioned and accessed through logical aliases.
6. Critical mappings must be explicit.
7. Arbitrary dynamic-field growth must be prevented.
8. Field mappings must match actual search/sort/filter semantics.
9. Analyzer changes must account for reindexing.
10. Exact business constraints must use filter semantics where appropriate.
11. Authorization and tenant isolation must be server-enforced.
12. Arbitrary public OpenSearch DSL must not be exposed.
13. Expensive search capabilities must be bounded.
14. Page size must be bounded.
15. Deep pagination must avoid large offsets.
16. `search_after` must use deterministic sorting with a unique tie-breaker.
17. PIT should be used only where consistent traversal requires it.
18. Search-index synchronization must use a reliable Outbox/CDC/event mechanism.
19. Index consumers must be idempotent.
20. Stale events must not overwrite newer document versions.
21. Delete ordering must prevent stale resurrection.
22. Bulk requests must be bounded.
23. Bulk responses must be inspected item-by-item.
24. Permanent indexing failures must not retry indefinitely.
25. DLQ/recovery procedures must exist.
26. Indexing concurrency and retry must be bounded.
27. Search freshness/index lag must be measurable.
28. Breaking mappings must use new-index/reindex/alias-switch deployment.
29. Rebuild must include a deterministic incremental catch-up strategy.
30. Search outage fallback must not overload transactional persistence.
31. Search data must follow PII minimization, retention and deletion standards.
32. Search client and cluster operations must have bounded timeouts.
33. Cluster capacity, heap, disk, shards and rejection rates must be monitored.
34. Search mappings, analyzers, pagination, indexing and rebuild behavior must have integration tests.
35. Business-critical relevance must have regression tests.

---

# 246. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- OpenSearch Java Client
- OpenSearch
- Elasticsearch-compatible concepts where applicable
- PostgreSQL
- Transactional Outbox
- SQS where applicable
- CDC where applicable
- Flyway
- Testcontainers
- JUnit 5
- AssertJ
- Resilience4j
- Micrometer
- OpenTelemetry where enabled
- load testing
- relevance regression tests
- failure injection
- rebuild exercises
- CI/CD quality gates

---

# 247. Success Criteria

The decision is successful when:

- advanced search does not overload transactional persistence
- search indices can be rebuilt without relying on the old index
- search schema changes can be deployed without downtime
- indexing events are not lost after business commits
- duplicate events are safe
- stale events cannot overwrite newer data
- indexing lag is measurable
- poison documents become operationally visible
- deep pagination does not create uncontrolled cluster work
- search authorization cannot leak cross-user or cross-tenant data
- search outages do not cascade into database outages
- unnecessary PII is excluded from search
- relevance behavior is regression-tested
- search capacity is measurable and predictable

---

# 248. Alternatives Rejected

## 248.1 Use OpenSearch for Every Query

Rejected because simple transactional queries are often more efficiently and consistently served by PostgreSQL.

---

## 248.2 OpenSearch as Only Business Store

Rejected as the general standard because search indices are optimized derived representations with different consistency and lifecycle semantics.

---

## 248.3 Database + Search Synchronous Dual Write

Rejected because one write may succeed while the other fails.

---

## 248.4 Huge Offset Pagination

Rejected because deep `from + size` produces excessive cluster work.

---

## 248.5 Reindex in Place

Rejected for breaking mapping/analyzer changes because safe versioned index replacement provides better rollback and deployment characteristics.

---

## 248.6 Retry Every Failed Document Forever

Rejected because permanent mapping/data defects become poison-message loops.

---

## 248.7 Automatic Full Database Search Fallback

Rejected because search-cluster failure could cascade into transactional database failure.

---

# 249. Related Decisions

This ADR extends and implements:

- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-077: Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard
- ADR-080: Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard

---

# 250. References

- OpenSearch Documentation
- Elasticsearch Documentation
- Apache Lucene Documentation
- PostgreSQL Documentation
- Amazon SQS Documentation
- AWS OpenSearch Service Documentation
- Spring Boot Documentation
- Testcontainers Documentation
- OWASP Application Security Guidance
- Transactional Outbox Pattern
- Change Data Capture Pattern
- Enterprise Integration Patterns
- Designing Data-Intensive Applications
- Google Site Reliability Engineering

---

# 251. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise search, indexing and read-model baseline |

---

# 252. Decision Summary

The authoritative write path becomes:

```text
APPLICATION
    |
    v
POSTGRESQL
    |
    +--> OUTBOX / CDC
             |
             v
          INDEXER
             |
             v
         OPENSEARCH
```

rather than:

```text
APPLICATION
   |
   +--> POSTGRESQL
   |
   +--> OPENSEARCH
          |
          X
     PARTIAL FAILURE
```

Search becomes:

```text
CLIENT
  |
  v
SEARCH API
  |
  +--> AUTHORIZATION
  |
  +--> QUERY VALIDATION
  |
  +--> LIMITS
  |
  v
OPENSEARCH
```

instead of direct unrestricted cluster access.

Index lifecycle becomes:

```text
orders-search-v7
       |
       | CURRENT ALIAS
       v
orders-search
       |
       |
CREATE v8
       |
       v
REBUILD
       |
       v
CATCH UP EVENTS
       |
       v
VALIDATE
       |
       v
ATOMIC ALIAS SWITCH
       |
       v
orders-search-v8
```

Deep pagination becomes:

```text
PAGE 1
  |
  v
SORT:
createdAt DESC,
id DESC
  |
  v
LAST SORT VALUES
  |
  v
search_after
  |
  v
PAGE 2
```

instead of:

```text
from = 500000
size = 100
```

Index-event handling becomes:

```text
EVENT VERSION 12
      |
      v
INDEX VERSION 12

EVENT VERSION 11
      |
      v
COMPARE VERSION
      |
      X
IGNORE STALE EVENT
```

Bulk indexing becomes:

```text
BULK RESPONSE
      |
      +--> SUCCESS
      |
      +--> RETRYABLE
      |       |
      |       v
      |   BACKOFF
      |
      +--> PERMANENT
              |
              v
             DLQ
```

Rebuild becomes:

```text
AUTHORITATIVE DATABASE
        |
        v
NEW INDEX
        |
        +--> BASE SNAPSHOT
        |
        +--> EVENT CATCH-UP
        |
        v
VALIDATION
        |
        v
ALIAS SWITCH
```

Search outage becomes:

```text
SEARCH REQUEST
      |
      v
OPENSEARCH
      |
      X
    OUTAGE
      |
      v
DEFINED DEGRADATION
      |
 +----+----------------+
 |                     |
503             LIMITED SAFE
                DB FALLBACK
```

rather than automatically sending arbitrary full-text workload to PostgreSQL.

The complete search equation is:

```text
AUTHORITATIVE DATABASE
        +
DERIVED SEARCH MODEL
        +
EXPLICIT MAPPINGS
        +
CORRECT ANALYZERS
        +
SERVER-SIDE AUTHORIZATION
        +
BOUNDED QUERY FEATURES
        +
DETERMINISTIC PAGINATION
        +
TRANSACTIONAL OUTBOX / CDC
        +
IDEMPOTENT INDEXING
        +
EVENT VERSION PROTECTION
        +
BOUNDED BULK PROCESSING
        +
DLQ
        +
INDEX LAG MONITORING
        +
REBUILDABILITY
        +
VERSIONED INDICES
        +
ATOMIC ALIAS SWITCH
        +
PII MINIMIZATION
        +
CAPACITY ENGINEERING
        +
REAL INTEGRATION TESTS
        =
SAFE ENTERPRISE SEARCH
```

The governing principle is:

```text
Do not introduce
a search engine
because SQL became
slightly inconvenient.

Use PostgreSQL
for transactional truth.

Use OpenSearch
when search semantics
justify OpenSearch.

Treat the index
as a projection.

Make every index
rebuildable.

Never depend on
the old index
to rebuild the new one.

Avoid synchronous
database/search dual writes.

Publish changes reliably.

Assume events duplicate.

Assume events arrive
out of order.

Protect document versions.

Do not let stale updates
resurrect deleted data.

Define mappings explicitly.

Use text for text.

Use keyword for exact values.

Do not analyze UUIDs.

Test analyzers against
real business vocabulary.

Separate scoring
from business filtering.

Authorization always wins
over relevance.

Do not expose
arbitrary query DSL.

Bound expensive queries.

Bound page size.

Do not deep-page
with giant offsets.

Use deterministic
search_after.

Inspect every bulk item.

Do not retry
mapping errors forever.

Make poison documents visible.

Measure index lag.

Version physical indices.

Switch aliases atomically.

Keep rollback possible.

Do not flood PostgreSQL
when OpenSearch fails.

Minimize PII.

Remember that an index
is another stored copy
of sensitive information.

Monitor heap.

Monitor disk.

Monitor shards.

Monitor rejections.

Monitor latency.

Test against
a real search engine.

And remember:

a search index is valuable
only when it can disappear
completely today

and be reconstructed
correctly tomorrow

from authoritative data
without changing
business truth.
```
