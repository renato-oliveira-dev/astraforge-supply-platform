# ADR-032: Adopt Distributed Caching and Cache Consistency Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-032 |
| Title | Adopt Distributed Caching and Cache Consistency Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Redis, Caffeine, Spring Cache, Distributed Caching, Resilience |
| Related Work Items | Redis, Cache-Aside, L1/L2 Cache, TTL, Invalidation, Resilience |
| Supersedes | None |
| Superseded By | ADR-059 |

---

# 1. Context

The Enterprise Order Platform uses distributed services with:

- Java 21
- Spring Boot
- PostgreSQL
- Redis
- Kafka
- external HTTP APIs
- Kubernetes
- horizontal scaling
- multiple application replicas

Several workloads repeatedly access data whose authoritative source is comparatively expensive.

Examples include:

- reference data
- configuration data
- product metadata
- customer-related lookup information
- external-service responses
- authorization-supporting metadata
- expensive database queries

Caching can materially improve:

- latency
- throughput
- database load
- external API load
- infrastructure efficiency

However, distributed caching introduces correctness and operational concerns:

```text
Stale Data
Cache Invalidation
Cache Stampede
Hot Keys
Serialization Compatibility
Memory Pressure
Redis Failure
Cross-Pod Consistency
Fallback Overload
```

Caching must therefore be treated as an architectural optimization with explicit consistency semantics.

---

# 2. Problem Statement

The platform requires standards defining:

- cache ownership
- authoritative source
- cache-aside
- local caching
- distributed caching
- two-level caching
- TTL
- expiration
- invalidation
- eviction
- serialization
- key design
- cache consistency
- stale data
- cache stampede
- thundering herd
- request coalescing
- negative caching
- hot keys
- large values
- Redis resilience
- fallback
- cache warming
- observability
- privacy
- security
- deployment compatibility

---

# 3. Decision Drivers

Primary drivers are:

1. correctness
2. predictable latency
3. reduced database load
4. reduced external dependency load
5. resilience
6. bounded staleness
7. horizontal scalability
8. operational simplicity
9. observability
10. memory efficiency
11. deployment safety
12. privacy
13. maintainability

---

# 4. Decision

The Enterprise Order Platform adopts caching according to:

```text
AUTHORITATIVE SOURCE

        ↓

OPTIONAL CACHE

        ↓

BOUNDED STALENESS

        ↓

CONTROLLED INVALIDATION

        ↓

FAIL-SAFE DEGRADATION

        ↓

OBSERVABILITY
```

The fundamental rule is:

```text
Cache is an optimization.

Cache is not automatically
the source of truth.
```

---

# 5. Authoritative Source

Every cached dataset must have an explicitly identifiable authoritative source.

Examples:

```text
PostgreSQL

Remote Customer API

Products Service

Configuration Service
```

---

# 6. Cache Truth

A cached value means:

```text
This is a previously obtained representation
of authoritative data.
```

It does not universally mean:

```text
This is unquestionably the latest value.
```

---

# 7. Cache Classification

Caches must be classified according to their role.

Recommended categories:

```text
PERFORMANCE CACHE

RESILIENCE CACHE

DERIVED DATA CACHE

LOCAL COMPUTATION CACHE
```

---

# 8. Performance Cache

A performance cache exists primarily to reduce latency/load.

If unavailable, correctness should normally remain intact.

---

# 9. Resilience Cache

A resilience cache may intentionally serve stale-but-acceptable data when an authoritative dependency is unavailable.

This requires explicit business approval.

---

# 10. Derived Data Cache

Derived values may be cached when recomputation is expensive and deterministic.

---

# 11. Local Computation Cache

Purely local deterministic computation may use in-process caching when appropriate.

---

# 12. Cache-Aside

Cache-aside is the preferred general pattern.

```text
REQUEST
   |
   v
CACHE LOOKUP
   |
   +---- HIT ----> RETURN
   |
   v
  MISS
   |
   v
AUTHORITATIVE SOURCE
   |
   v
CACHE PUT
   |
   v
RETURN
```

---

# 13. Cache-Aside Responsibility

The application controls:

- cache lookup
- source retrieval
- cache population
- invalidation semantics

---

# 14. Read-Through

Read-through caching may be used when the cache abstraction provides appropriate loading semantics.

---

# 15. Write-Through

Write-through may be used only when the write path and failure semantics are explicitly understood.

---

# 16. Write-Behind

Write-behind is not the default strategy for authoritative business state.

---

# 17. Why Write-Behind Is Dangerous

This architecture:

```text
Application

↓

Cache

↓

Eventually Database
```

introduces risk of:

- lost writes
- reordered writes
- inconsistent recovery
- cache/database divergence

---

# 18. Source-of-Truth Writes

Transactional business writes must normally go directly to the authoritative persistence mechanism.

---

# 19. PostgreSQL

For service-owned transactional data:

```text
PostgreSQL
```

remains authoritative according to ADR-031.

---

# 20. Redis

Redis must not silently become the authoritative transactional database merely because it provides fast access.

---

# 21. L1 Cache

An L1 cache is local to an application instance.

Typical implementation:

```text
Caffeine
```

---

# 22. L1 Advantages

Local caching provides:

- very low latency
- no network round trip
- reduced Redis load

---

# 23. L1 Limitation

Each pod has independent state.

Example:

```text
Pod A L1

Pod B L1

Pod C L1
```

are separate caches.

---

# 24. L2 Cache

An L2 cache is shared across application instances.

Typical implementation:

```text
Redis
```

---

# 25. Two-Level Cache

Where justified:

```text
REQUEST
   |
   v
L1 CACHE
   |
   +---- HIT ----> RETURN
   |
   v
L2 REDIS
   |
   +---- HIT ----> L1 PUT ---> RETURN
   |
   v
AUTHORITATIVE SOURCE
   |
   v
L2 PUT
   |
   v
L1 PUT
   |
   v
RETURN
```

---

# 26. Two-Level Cache Cost

L1 + L2 improves latency but increases consistency complexity.

---

# 27. Cross-Pod Staleness

Example:

```text
Pod A L1 = Customer V1

Pod B L1 = Customer V1

        ↓

Pod A updates Customer to V2

        ↓

Redis invalidated

        ↓

Pod B still contains V1
```

---

# 28. L1 Consistency

L1 cache must therefore use one or more of:

- short TTL
- distributed invalidation
- versioned keys
- bounded staleness

according to business requirements.

---

# 29. L1 Eligibility

Do not introduce L1 caching merely because Redis exists.

Use L1 only where measured latency/load benefits justify additional consistency complexity.

---

# 30. TTL

Every non-permanent cache entry requires an intentional expiration policy.

---

# 31. Infinite TTL

Infinite TTL is prohibited by default.

---

# 32. TTL Selection

TTL must reflect:

```text
Business Staleness Tolerance

+

Source Update Frequency

+

Source Cost

+

Invalidation Reliability
```

---

# 33. Arbitrary TTL

Values such as:

```text
24 hours
```

must not be selected merely because they are convenient.

---

# 34. Short TTL

Short TTL improves freshness but increases source load.

---

# 35. Long TTL

Long TTL reduces source load but increases stale-data exposure.

---

# 36. TTL Jitter

Expiration jitter should be considered for high-volume caches.

Instead of every key expiring at exactly:

```text
10 minutes
```

use bounded randomized expiration where appropriate.

---

# 37. Why Jitter

Without jitter:

```text
10,000 keys populated together

↓

10 minutes

↓

10,000 keys expire together

↓

Massive source reload
```

---

# 38. Cache Stampede

A cache stampede occurs when many callers simultaneously reload the same missing/expired data.

---

# 39. Stampede Example

```text
Popular Key Expires
        |
        v
100 Requests Arrive
        |
        v
100 Cache Misses
        |
        v
100 Database Queries
```

---

# 40. Request Coalescing

For expensive hot-key loads, concurrent misses should be coalesced where practical:

```text
100 Requests

↓

1 Source Request

↓

99 Wait for Same Result

↓

Cache Populated
```

---

# 41. Single Flight

Single-flight/request-coalescing patterns may be used to suppress duplicate concurrent loads.

---

# 42. Local Single Flight

A local single-flight mechanism protects one application replica.

---

# 43. Distributed Stampede

With multiple replicas, local coalescing alone does not eliminate cluster-wide stampede.

---

# 44. Distributed Lock

A distributed lock may be considered for exceptionally expensive reloads.

---

# 45. Distributed Lock Caution

Distributed locking adds:

- failure modes
- timeout requirements
- lock ownership complexity

and must not be the default solution for every cache miss.

---

# 46. Probabilistic Early Refresh

Early-refresh techniques may be used for extremely hot keys where strict TTL expiration creates load spikes.

---

# 47. Stale-While-Revalidate

Where business semantics permit:

```text
Return slightly stale value

↓

Refresh asynchronously
```

may reduce latency and stampede risk.

---

# 48. Stale-While-Revalidate Requirement

Maximum permitted staleness must be explicit.

---

# 49. Cache Invalidation

Cache invalidation must correspond to authoritative data changes.

---

# 50. Invalidation Strategies

Supported strategies include:

```text
TTL

Explicit Eviction

Event-Driven Invalidation

Versioned Keys
```

---

# 51. Explicit Eviction

After authoritative mutation:

```text
Database Commit

↓

Evict Cache
```

may be appropriate.

---

# 52. Transaction Timing

Do not invalidate a cache before a database transaction commits if failure could leave the cache inconsistent with committed state.

---

# 53. After-Commit Invalidation

Where required:

```text
BEGIN

UPDATE DATABASE

COMMIT

↓

INVALIDATE CACHE
```

---

# 54. Invalidation Failure

The architecture must consider:

```text
Database commit succeeds

↓

Redis invalidation fails
```

---

# 55. TTL Safety Net

TTL provides an important upper bound on stale data when invalidation fails.

---

# 56. Event-Driven Invalidation

Kafka events may invalidate distributed/local caches where appropriate.

---

# 57. Invalidation Event

Example:

```text
CustomerUpdated

↓

Consumers invalidate:

customer:{id}
```

---

# 58. Event Delay

Event-driven invalidation is not instantaneous.

The architecture must tolerate propagation delay.

---

# 59. Duplicate Invalidation

Invalidation operations should be idempotent.

---

# 60. Missed Invalidation

TTL remains useful even with event-driven invalidation because events may be:

- delayed
- temporarily unprocessed
- operationally disrupted

---

# 61. Versioned Key

Versioned keys can make stale values unreachable.

Conceptually:

```text
product:123:v17
```

---

# 62. Versioned-Key Cost

Versioned keys require cleanup of obsolete entries and a reliable mechanism for determining the current version.

---

# 63. Delete vs Update Cache

After source mutation, prefer invalidation over immediately reconstructing complex cached state when reconstruction may be inaccurate or expensive.

---

# 64. Cache Update

Updating cache directly after mutation is acceptable when the new authoritative representation is already known and consistency semantics are clear.

---

# 65. Cache Key

Cache keys are public operational contracts within the caching layer.

---

# 66. Key Design

Keys should be:

- deterministic
- stable
- namespaced
- compact
- non-sensitive

---

# 67. Recommended Shape

Conceptually:

```text
<service>:<cache>:<version>:<identifier>
```

Example:

```text
customers:field-consultant:v2:12345
```

---

# 68. Environment

Environment prefixes are needed only when Redis infrastructure is shared across environments.

Isolated Redis deployments do not require redundant environment prefixes.

---

# 69. Key Collision

Different domains must not accidentally use the same Redis key namespace.

---

# 70. Sensitive Keys

Do not place directly in Redis key names:

- email
- CPF
- document number
- phone
- access token
- personal names

when an opaque identifier is available.

---

# 71. Key Length

Excessively long cache keys waste memory.

---

# 72. Key Version

Schema-breaking cache changes should use a key/schema version.

---

# 73. Deployment Compatibility

During rolling deployment:

```text
Old Pod

+

New Pod
```

may access Redis simultaneously.

Cache serialization must remain compatible.

---

# 74. Serialization

Cached values require explicit serialization strategy.

---

# 75. Java Native Serialization

Java native object serialization is not the preferred distributed-cache format.

---

# 76. JSON

JSON may provide readable interoperable cache values where payload size/performance is acceptable.

---

# 77. Binary Formats

Binary serialization may be used when governed and justified.

---

# 78. Serialization Version

Breaking serialized-value changes require:

- backward compatibility
- key version bump
- controlled eviction

---

# 79. Class Refactoring

Renaming a Java package/class must not unexpectedly make distributed cache values unreadable.

---

# 80. Cache DTO

Distributed cached values should use stable cache DTOs rather than persistence entities.

---

# 81. JPA Entity Cache

Storing attached JPA entities directly in Redis is discouraged.

---

# 82. Lazy Proxy

Hibernate proxies and lazy associations must not leak into distributed cache serialization.

---

# 83. Cache Size

Cached values must remain bounded.

---

# 84. Large Value

Large Redis values increase:

- network latency
- memory pressure
- serialization cost
- GC pressure
- replication cost

---

# 85. Entire Aggregate

Do not cache an entire massive aggregate if callers need only a small lookup result.

---

# 86. Cache Projection

Cache the representation required by the use case.

---

# 87. Redis Memory

Redis memory is finite.

---

# 88. Memory Policy

Redis memory limits and eviction policy must be explicitly configured by the platform.

---

# 89. Eviction Policy

Eviction strategy must reflect whether keys have TTL and whether losing entries is acceptable.

---

# 90. Cache Eviction

For performance caches, eviction should degrade performance, not correctness.

---

# 91. Memory Exhaustion

Applications must not assume every cache write succeeds indefinitely.

---

# 92. Cache Write Failure

For a non-authoritative performance cache:

```text
Source Read Successful

↓

Cache Write Failed

↓

Return Source Value
```

is generally preferable to failing the request.

---

# 93. Cache Read Failure

For a performance cache:

```text
Redis Read Fails

↓

Bypass Cache

↓

Read Authoritative Source
```

where capacity permits.

---

# 94. Redis Failure

A non-critical cache must not automatically become a hard application dependency.

---

# 95. Desired Failure Mode

```text
Redis Unavailable
       |
       v
Cache Bypass
       |
       v
Authoritative Source
       |
       v
Application Continues
```

---

# 96. Critical Caveat

Cache bypass can create a secondary outage.

---

# 97. Fallback Storm

Example:

```text
Redis handles 20,000 reads/s

↓

Redis fails

↓

20,000 reads/s redirected to PostgreSQL

↓

PostgreSQL saturates

↓

Application fails
```

---

# 98. Cache Resilience

Redis fallback must therefore include source protection.

---

# 99. Source Protection

Possible mechanisms include:

- bounded concurrency
- rate limiting
- circuit breaker
- request coalescing
- stale fallback
- load shedding

depending on semantics.

---

# 100. Failure Amplification

The system must avoid converting:

```text
Cache Incident
```

into:

```text
Database Incident
+
API Incident
+
Application Incident
```

---

# 101. Circuit Breaker

A circuit breaker may temporarily bypass repeated failing Redis operations.

---

# 102. Cache Circuit Breaker

Circuit-breaker semantics must distinguish:

```text
Cache unavailable
```

from:

```text
Authoritative source unavailable
```

---

# 103. Nested Resilience

Avoid uncontrolled combinations of:

```text
Cache Retry

+

Circuit Breaker

+

Source Retry

+

HTTP Retry
```

that multiply load.

---

# 104. Cache Retry

Cache operations should generally use minimal bounded retry because cache latency must remain low.

---

# 105. Long Redis Retry

Waiting several seconds repeatedly for a performance cache defeats the purpose of the cache.

---

# 106. Cache Timeout

Redis operations require short bounded timeouts.

---

# 107. Source Timeout

The authoritative source retains its own timeout policy.

---

# 108. Fallback Cache

A local in-memory fallback may be used when Redis fails if stale-data semantics are acceptable.

---

# 109. Resilient Cache

Conceptually:

```text
REQUEST
   |
   v
REDIS
   |
   +---- HIT ----> RETURN
   |
   +---- MISS ----+
   |              |
   X FAILURE      |
   |              |
   v              v
LOCAL FALLBACK   SOURCE
   |
   +---- HIT ----> RETURN STALE IF ALLOWED
```

---

# 110. Fallback Semantics

Local fallback must not silently serve stale values for data requiring strong freshness.

---

# 111. Fallback Synchronization

If Redis succeeds, local fallback may be synchronized where the design requires it.

---

# 112. Fallback Memory

Local fallback requires explicit:

- maximum size
- TTL
- eviction

---

# 113. Unbounded HashMap

An unbounded `ConcurrentHashMap` is not an acceptable production fallback cache.

---

# 114. Caffeine

Caffeine is preferred for bounded local caches where appropriate.

---

# 115. Negative Caching

Negative caching stores:

```text
NOT FOUND
```

for a bounded period.

---

# 116. Why Negative Cache

Without it:

```text
Invalid ID requested 10,000 times

↓

10,000 database/API lookups

↓

10,000 NOT FOUND
```

---

# 117. Negative TTL

Negative entries should generally use shorter TTL than stable positive entries.

---

# 118. Negative Cache Risk

If the object is created shortly afterward, stale negative cache can temporarily hide it.

---

# 119. Negative Cache Eligibility

Use negative caching only when bounded temporary false-negative behavior is acceptable.

---

# 120. Cache Penetration

Repeated requests for nonexistent keys can bypass the cache and overload the source.

Negative caching may mitigate this.

---

# 121. Hot Key

A hot key receives disproportionate traffic.

---

# 122. Hot-Key Risk

One Redis key may become a network/CPU bottleneck even when the overall cluster appears healthy.

---

# 123. Hot-Key Mitigation

Possible strategies include:

- L1 caching
- replication/read scaling
- request coalescing
- controlled key sharding for suitable data

---

# 124. Key Sharding

Do not shard a key merely to hide a poor access model.

---

# 125. Hot-Key Observability

Critical caches should provide visibility into high-frequency keys or cache regions where platform tooling permits.

---

# 126. Cache Warming

Cache warming may be appropriate for predictable high-value datasets.

---

# 127. Startup Warming

Do not require massive synchronous cache warming before application readiness unless business requirements demand it.

---

# 128. Startup Stampede

When many pods start simultaneously:

```text
Pod 1
Pod 2
Pod 3
...
Pod 20

↓

All Warm Same Cache

↓

Source Overload
```

---

# 129. Warm-Up Coordination

Large warming operations require:

- staggering
- bounded concurrency
- leader coordination
- background population

where necessary.

---

# 130. Lazy Population

Lazy cache population is preferred when workload distribution is unpredictable.

---

# 131. Preload

Preload only data with demonstrated value.

---

# 132. Cache Consistency

Cache consistency must be explicitly classified.

Possible models include:

```text
STRONG-LIKE / IMMEDIATE INVALIDATION

BOUNDED STALENESS

EVENTUAL CONSISTENCY

STALE-IF-ERROR
```

---

# 133. Strong Consistency

A normal cache cannot casually promise strong consistency across:

```text
Database

Redis

Multiple Pods

L1 Caches
```

without significant coordination.

---

# 134. Strong Requirement

Data requiring strict transactional correctness should normally be read from authoritative transactional state within the required transaction.

---

# 135. Authorization Cache

Authorization/security-sensitive caches require particularly conservative staleness.

---

# 136. Revocation

A long cache TTL must not prevent timely revocation of permissions or access.

---

# 137. Financial Decision Cache

Cached values used for financial/business-critical decisions require explicit freshness rules.

---

# 138. Display Data

Display/reference metadata may often tolerate greater staleness than transactional decision data.

---

# 139. Cache Region

Caches should be grouped into logical regions with distinct policies.

Example:

```text
products-reference

customer-consultants

parameter-values
```

---

# 140. One Global TTL

A single TTL for every cache is discouraged.

---

# 141. Region Configuration

Each cache region should define:

- owner
- source
- TTL
- max size where applicable
- serialization
- failure behavior
- consistency model

---

# 142. Cache Registry

Critical caches should be inventoried.

---

# 143. Cache Inventory

Recommended metadata:

```text
Cache Name
Owner
Authoritative Source
TTL
Key
Value Type
Consistency
Failure Mode
PII Classification
```

---

# 144. Spring Cache

Spring Cache abstraction may be used where its semantics fit the use case.

---

# 145. `@Cacheable`

`@Cacheable` is appropriate for straightforward deterministic cache-aside reads.

---

# 146. Hidden Complexity

Annotations do not eliminate:

- key design
- TTL
- invalidation
- serialization
- failure handling

---

# 147. `@CacheEvict`

Eviction annotations require correct transaction timing.

---

# 148. Transaction-Aware Eviction

For mutations, cache invalidation should occur only after authoritative commit when stale-on-rollback would be problematic.

---

# 149. `sync=true`

Local cache synchronization mechanisms may reduce duplicate local loads where supported.

---

# 150. Cluster Limitation

Local synchronization does not coordinate other Kubernetes pods.

---

# 151. Null Caching

Caching `null` values must be an explicit policy.

---

# 152. Null Ambiguity

The cache layer must distinguish where necessary:

```text
CACHE MISS
```

from:

```text
CACHED NOT FOUND
```

---

# 153. Redis Key Scan

Production application logic must avoid expensive broad key scans.

---

# 154. `KEYS`

Redis `KEYS` over large production keyspaces is strongly discouraged.

---

# 155. SCAN

Operational iteration should use safer incremental mechanisms such as `SCAN` where appropriate.

---

# 156. Prefix Deletion

Designing invalidation around scanning millions of keys by prefix is discouraged.

---

# 157. Version Namespace

Versioned namespaces may provide safer bulk logical invalidation.

---

# 158. Redis Transactions

Redis transactions/Lua may be used when atomic cache operations genuinely require them.

---

# 159. Lua Complexity

Complex business workflows must not migrate into Redis Lua scripts merely for speed.

---

# 160. Distributed Locks

Redis-based distributed locks require explicit review.

---

# 161. Lock Correctness

A distributed lock must define:

- ownership
- expiration
- timeout
- fencing/consistency requirements
- failure behavior

---

# 162. Cache vs Lock Service

Using Redis as cache does not automatically make it the correct distributed coordination mechanism.

---

# 163. Cache Observability

Cache behavior must be observable.

---

# 164. Required Metrics

Monitor at least:

```text
Hit Count

Miss Count

Hit Ratio

Evictions

Load Duration

Load Failures

Redis Errors

Fallback Usage
```

---

# 165. Hit Ratio

Hit ratio alone is insufficient.

---

# 166. Example

A cache may have:

```text
99% hit ratio
```

but the remaining 1% may trigger extremely expensive queries.

---

# 167. Source Load

Monitor authoritative-source load caused by cache misses.

---

# 168. Cache Latency

Redis latency should be monitored independently from source latency.

---

# 169. Fallback Metric

Fallback usage must be measurable.

---

# 170. Stale Fallback Metric

Serving stale data due to dependency failure should be separately observable.

---

# 171. Cache Error Rate

Cache errors must not disappear merely because fallback succeeds.

---

# 172. Degraded State

The service may remain available while operating in degraded cache mode.

This state should be observable.

---

# 173. Metrics Cardinality

Cache keys must not become metric labels.

---

# 174. Cache Region Label

Low-cardinality cache region names may be appropriate metric dimensions.

---

# 175. Logging

Cache logs must follow ADR-019.

---

# 176. Routine Miss

Ordinary cache misses should not produce noisy ERROR logs.

---

# 177. Redis Failure Log

Redis infrastructure failures should include enough context for diagnosis without logging sensitive cached values.

---

# 178. Cache Value Logging

Full cached values must not be logged by default.

---

# 179. Tracing

Cache operations may participate in distributed traces where observability instrumentation supports it.

---

# 180. Trace Detail

Trace spans should identify cache region/operation, not sensitive keys or full values.

---

# 181. Health Checks

Cache health semantics depend on cache criticality.

---

# 182. Non-Critical Cache

If Redis is only an optimization:

```text
Redis DOWN
```

should not necessarily make application readiness:

```text
DOWN
```

---

# 183. Critical Redis Dependency

If Redis is required for correctness, coordination or mandatory session state, readiness semantics may differ.

---

# 184. Dependency Classification

Redis dependency criticality must therefore be explicit.

---

# 185. Kubernetes

Redis health failures must not trigger unnecessary pod restart loops when the external Redis service is the actual problem.

---

# 186. Cache Failure Recovery

After Redis recovery, applications should return to normal cache usage without requiring restart where practical.

---

# 187. Redis Reconnect

Client reconnect behavior must be bounded and observable.

---

# 188. Redis Client

The approved Spring/Redis client configuration must define:

- connection timeout
- command timeout
- pool/resources where applicable
- reconnect behavior

---

# 189. Retry Storm

All pods aggressively reconnecting/retrying Redis simultaneously can amplify an outage.

---

# 190. Resilience4j

Resilience4j may protect cache operations when appropriate, but policy must remain simple and bounded.

---

# 191. Bulkhead

A bulkhead/concurrency limiter may prevent degraded cache behavior from consuming excessive application resources.

---

# 192. Virtual Threads

Virtual threads do not remove Redis/source capacity limits.

---

# 193. Parallel Cache Misses

Java 21 concurrency must not create unbounded parallel source loads during cache misses.

---

# 194. Concurrency Budget

Cache loaders require bounded concurrency.

---

# 195. ADR-034

Detailed concurrency rules are defined in ADR-034.

---

# 196. Privacy

Cached data follows ADR-029.

---

# 197. Data Minimization

Cache only fields required by the use case.

---

# 198. PII

Personal data must not be cached merely because doing so is technically easy.

---

# 199. TTL and Retention

Cache TTL must not violate retention/deletion requirements.

---

# 200. Right to Deletion

Where deletion requirements apply, invalidation must include relevant caches.

---

# 201. Redis Backup

Performance caches generally should not require backup if they can be reconstructed.

---

# 202. Reconstructability

If a cache cannot be safely reconstructed, it may not actually be only a cache.

---

# 203. Disaster Recovery

DR design must classify whether cached data needs recovery or can be rebuilt.

---

# 204. Cache Rebuild

After disaster recovery:

```text
Empty Cache

↓

Normal Traffic

↓

Potential Stampede
```

must be considered.

---

# 205. DR Warming

Critical high-volume caches may require controlled post-recovery warming.

---

# 206. Security

Redis access must use enterprise authentication/network controls.

---

# 207. Least Privilege

Applications should receive only required Redis permissions where infrastructure supports granular authorization.

---

# 208. Secrets

Redis credentials follow ADR-026.

---

# 209. Encryption

Redis transport/storage security follows ADR-029 and platform security standards.

---

# 210. Cache Injection

User-controlled values must not be used unsafely to construct arbitrary cache operations.

---

# 211. Cache Poisoning

Only trusted application paths should populate authoritative cache representations.

---

# 212. Cache Key Validation

External input used in cache keys must be validated and normalized according to domain semantics.

---

# 213. Testing

Caching behavior requires automated tests.

---

# 214. Hit Test

Verify:

```text
Cache Hit

↓

Source Not Called
```

where expected.

---

# 215. Miss Test

Verify:

```text
Cache Miss

↓

Source Called

↓

Cache Populated
```

---

# 216. Redis Failure Test

Verify:

```text
Redis Failure

↓

Correct Fallback
```

---

# 217. Source Failure Test

Verify source failure separately from cache failure.

---

# 218. Invalidation Test

Verify mutation invalidates/updates relevant cache entries.

---

# 219. Rollback Test

Verify failed database transactions do not incorrectly publish committed-cache state.

---

# 220. TTL Test

Expiration behavior should be tested without unnecessary real-time sleeps where practical.

---

# 221. Stampede Test

Critical hot caches should test concurrent miss behavior.

---

# 222. Serialization Test

Rolling-deployment compatibility should be tested when cache DTOs change.

---

# 223. Negative Cache Test

Verify bounded NOT FOUND behavior where enabled.

---

# 224. L1/L2 Test

Two-level caches require tests covering:

- L1 hit
- L1 miss/L2 hit
- L1/L2 miss
- Redis failure
- L1 stale behavior
- invalidation

---

# 225. Testcontainers

Redis Testcontainers may be used for realistic integration tests where supported.

---

# 226. Mock Limitation

Mocking Redis alone does not validate:

- TTL
- serialization
- connection behavior
- actual Redis commands

---

# 227. Performance Testing

Cache performance tests should measure:

- hit latency
- miss latency
- source load
- Redis throughput
- serialization cost

---

# 228. Failure Load Test

Test:

```text
Normal Load

↓

Redis Becomes Unavailable

↓

Source Load Increase
```

for critical high-volume caches.

---

# 229. Recovery Load Test

Test Redis recovery and cache repopulation behavior.

---

# 230. Cache ROI

Caching should provide measurable value.

---

# 231. Low-Value Cache

A cache with:

- low hit rate
- trivial source cost
- high invalidation complexity

should be reconsidered.

---

# 232. Cache Removal

Caches are architecture that can be removed when their cost exceeds their benefit.

---

# 233. Cache Review

Critical cache regions should be periodically reviewed for:

- hit ratio
- memory
- staleness
- source savings
- failure behavior

---

# 234. Anti-Patterns

The following are prohibited or strongly discouraged:

- treating Redis as source of truth without explicit architectural decision
- caching without identifying authoritative source
- infinite TTL by default
- arbitrary TTL values
- identical expiration for massive key populations without considering jitter
- uncontrolled cache stampede
- unbounded concurrent reload
- cache invalidation before authoritative commit
- relying exclusively on invalidation without TTL safety where stale data matters
- storing PII in Redis keys
- caching persistence entities directly
- serializing Hibernate proxies
- unbounded local fallback maps
- massive Redis values
- assuming cache writes cannot fail
- failing business requests solely because a non-critical cache write failed
- blindly bypassing Redis without protecting PostgreSQL/external APIs
- long retry loops against Redis
- nested uncontrolled resilience policies
- serving stale authorization data without explicit policy
- one TTL for every cache
- broad production `KEYS` scans
- prefix invalidation requiring massive key scans
- complex business workflows inside Redis Lua
- casual distributed locking
- logging complete cached values
- using cache keys as metric labels
- making a performance-only Redis dependency mandatory for Kubernetes readiness
- unbounded parallel cache loaders
- cache retention violating privacy requirements
- backing up reconstructable cache data without need
- using caching to hide inefficient database queries
- introducing L1 cache without accounting for cross-pod staleness

---

# 235. Positive Consequences

The decision provides:

- lower latency
- reduced database load
- reduced external API load
- bounded stale-data behavior
- improved Redis failure resilience
- safer cross-pod caching
- reduced cache stampedes
- predictable cache contracts
- improved observability
- safer deployments
- better privacy controls

---

# 236. Negative Consequences

The decision introduces:

- cache policy management
- TTL tuning
- invalidation complexity
- additional testing
- serialization governance
- fallback capacity planning
- possible event-driven invalidation
- additional metrics

These costs are accepted because distributed caches otherwise create hidden consistency and availability risks.

---

# 237. Neutral Consequences

The decision also means:

- some requests may intentionally bypass cache
- stale values may be acceptable for some domains but forbidden for others
- L1 cache is not appropriate everywhere
- Redis failure may increase source traffic
- empty-cache recovery may temporarily reduce performance
- cache misses remain normal application behavior
- cache consistency is use-case specific

---

# 238. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Stale data | High | Medium | TTL + invalidation |
| Cache stampede | High | Medium | Coalescing + jitter |
| Redis outage overloads DB | Critical | Medium | Source protection |
| L1 stale across pods | High | Medium | Short TTL/event invalidation |
| Serialization incompatibility | High | Medium | Versioned keys/contracts |
| Redis memory exhaustion | High | Medium | Bounded TTL/eviction |
| Hot key | High | Medium | L1/coalescing/monitoring |
| Negative cache hides new data | Medium | Medium | Short negative TTL |
| Cache invalidation fails | High | Medium | TTL safety net |
| PII exposed in keys | Critical | Low | Opaque identifiers |
| Cache retry storm | High | Medium | Short bounded timeout/retry |
| Empty cache causes source surge | High | Medium | Controlled warming/coalescing |

---

# 239. Implementation Guidance

The following rules are mandatory:

1. Every cache must have an authoritative source.
2. Cache must not become source of truth accidentally.
3. Cache-aside is the preferred general strategy.
4. Transactional writes go to the authoritative source.
5. Every cache region requires explicit TTL.
6. Infinite TTL is prohibited by default.
7. TTL must reflect business staleness tolerance.
8. High-volume expiration should consider jitter.
9. Expensive hot-key reloads must consider request coalescing.
10. Cache invalidation must respect transaction commit.
11. TTL should provide a stale-data safety bound.
12. Event-driven invalidation must tolerate delay/duplicates.
13. L1 caches require explicit cross-pod consistency strategy.
14. Cache keys must be deterministic and namespaced.
15. Sensitive data must not appear directly in cache keys.
16. Breaking serialization changes require compatibility or versioned keys.
17. JPA entities must not be stored directly in distributed cache.
18. Cache values must remain bounded.
19. Local fallback caches must have maximum size and TTL.
20. Performance-cache write failures should normally not fail successful source reads.
21. Performance-cache read failures should normally allow controlled source fallback.
22. Source fallback must be capacity protected.
23. Cache retries must remain short and bounded.
24. Cache and source failures must be independently observable.
25. Negative caching requires explicit short TTL.
26. Redis keyspace scans must not be part of normal request processing.
27. Distributed locks require explicit review.
28. Cache hit/miss/eviction/failure metrics must exist.
29. Fallback and stale-fallback usage must be measurable.
30. Cache keys must not become high-cardinality metric labels.
31. Non-critical Redis failure must not automatically fail application readiness.
32. Cache loaders require bounded concurrency.
33. Cached data follows privacy/retention standards.
34. Reconstructable performance caches normally do not require backup.
35. Cache failure/recovery behavior must be load tested for critical caches.
36. Cache must not hide structurally inefficient source queries.

---

# 240. Cache Production Readiness Gate

A critical cache is not production ready until:

```text
[ ] Cache owner identified

[ ] Authoritative source identified

[ ] Cache role classified

[ ] Consistency model defined

[ ] Maximum acceptable staleness defined

[ ] TTL defined

[ ] TTL jitter reviewed

[ ] Key format defined

[ ] PII in key reviewed

[ ] Value schema defined

[ ] Serialization compatibility reviewed

[ ] Maximum value size reviewed

[ ] Invalidation strategy defined

[ ] Transaction timing reviewed

[ ] Stampede risk reviewed

[ ] Request coalescing reviewed

[ ] Negative caching reviewed

[ ] L1/L2 strategy reviewed

[ ] Redis failure behavior defined

[ ] Source protection defined

[ ] Retry/timeout policy defined

[ ] Local fallback bounded

[ ] Hit/miss metrics available

[ ] Redis error metrics available

[ ] Fallback metrics available

[ ] Source load metrics available

[ ] Privacy review completed

[ ] Redis failure test completed

[ ] Cache recovery test completed

[ ] Concurrent miss test completed

[ ] Rolling-deployment serialization reviewed
```

---

# 241. Validation

This ADR will be validated through:

- architecture reviews
- cache inventory
- cache hit/miss tests
- invalidation tests
- Redis integration tests
- Testcontainers
- concurrency tests
- stampede tests
- Redis failure tests
- source-fallback tests
- recovery tests
- serialization compatibility tests
- load tests
- privacy reviews
- production metrics
- periodic cache-efficiency reviews

---

# 242. Success Criteria

The decision is successful when:

- caches materially reduce source load
- cache failures do not corrupt business state
- non-critical Redis outages do not automatically cause application outages
- Redis outages do not immediately overload PostgreSQL
- stale-data behavior is explicit
- cross-pod L1 staleness is bounded
- cache stampedes remain controlled
- serialization changes survive rolling deployments
- cache keys do not expose sensitive information
- cache behavior can be diagnosed from production telemetry
- caches can be removed without loss of authoritative business data

---

# 243. Alternatives Rejected

## 243.1 Redis as Universal Source of Truth

Rejected because cache and transactional persistence have different correctness requirements.

---

## 243.2 Infinite TTL Everywhere

Rejected because stale values can persist indefinitely.

---

## 243.3 Cache Everything

Rejected because cache introduces consistency, memory and operational cost.

---

## 243.4 Fail Request Whenever Redis Fails

Rejected for performance-only caches because it unnecessarily reduces availability.

---

## 243.5 Always Bypass Redis During Failure Without Source Protection

Rejected because it can transfer the outage directly to PostgreSQL or external services.

---

## 243.6 Local Cache Everywhere

Rejected because independent pod state increases consistency complexity.

---

## 243.7 Redis Only, Never Local Cache

Rejected because extremely hot read workloads may benefit materially from bounded L1 caching.

---

## 243.8 Cache as Fix for Slow SQL

Rejected because inefficient queries should first be corrected according to ADR-031.

---

# 244. Related Decisions

This ADR is related to:

- ADR-005: Use PostgreSQL as the Primary Database
- ADR-009: Use Apache Kafka for Integration Events
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Concurrency and Parallelism Standards

---

# 245. References

- Redis Documentation
- Spring Data Redis Documentation
- Spring Cache Documentation
- Caffeine Documentation
- Resilience4j Documentation
- PostgreSQL Documentation
- Apache Kafka Documentation
- Java 21 Documentation
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards

---

# 246. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial distributed caching baseline |

---

# 247. Decision Summary

The caching hierarchy is:

```text
                 REQUEST
                    |
                    v
             +-------------+
             |  L1 CACHE   |
             |  Caffeine   |
             +-------------+
               |         |
             HIT        MISS
               |         |
               |         v
               |   +-------------+
               |   |  L2 CACHE   |
               |   |    Redis    |
               |   +-------------+
               |      |       |
               |     HIT     MISS
               |      |       |
               |      |       v
               |      |  +----------------+
               |      |  | AUTHORITATIVE  |
               |      |  |     SOURCE     |
               |      |  +----------------+
               |      |          |
               |      +----------+
               |             |
               +-------------+
                     |
                     v
                   RETURN
```

But the correctness hierarchy is always:

```text
AUTHORITATIVE SOURCE
        |
        v
      TRUTH
        |
        v
       CACHE
        |
        v
OPTIMIZED REPRESENTATION
```

not:

```text
CACHE
  |
  v
TRUTH
```

Redis failure for a performance cache follows:

```text
REDIS FAILURE
      |
      v
OPEN / BYPASS CACHE PATH
      |
      v
PROTECT AUTHORITATIVE SOURCE
      |
      +-------------------+
      |                   |
      v                   v
SOURCE HAS CAPACITY    SOURCE SATURATED
      |                   |
      v                   v
READ SOURCE          LOAD SHED /
      |              STALE FALLBACK /
      v              CONTROLLED FAILURE
RETURN
```

The important distinction is:

```text
Cache Failure
     !=
Source Failure
```

and also:

```text
Cache Failure
     !=
Application Failure
```

when cache is only a performance optimization.

Cache stampede protection follows:

```text
HOT KEY EXPIRES
      |
      v
100 CONCURRENT REQUESTS
      |
      v
COALESCE
      |
      v
1 SOURCE LOAD
      |
      v
CACHE POPULATED
      |
      v
100 REQUESTS COMPLETE
```

rather than:

```text
HOT KEY EXPIRES
      |
      v
100 REQUESTS
      |
      v
100 SOURCE CALLS
      |
      v
SOURCE SATURATION
```

Two-level cache consistency follows:

```text
AUTHORITATIVE DATA CHANGES
          |
          v
      COMMIT FIRST
          |
          v
    INVALIDATE REDIS
          |
          v
DISTRIBUTED INVALIDATION
          |
     +----+----+
     |         |
     v         v
   POD A     POD B
   L1 EVICT  L1 EVICT
```

with TTL as a safety boundary:

```text
Invalidation Lost
      |
      v
Stale Value Exists
      |
      v
TTL Expires
      |
      v
Source Reload
```

The resilience hierarchy is:

```text
1. Cache Hit

2. Cache Miss → Source

3. Cache Failure → Controlled Source Fallback

4. Source Protection

5. Optional Bounded Stale Fallback

6. Controlled Failure
```

not:

```text
Redis failed

↓

Send unlimited traffic to PostgreSQL
```

Cache configuration must therefore answer six questions:

```text
1. What is the authoritative source?

2. How stale may the value become?

3. How is it invalidated?

4. What happens when Redis fails?

5. How is the source protected?

6. How do we know the cache is working?
```

The definitive principle is:

```text
A cache should make a healthy system faster.

It must not silently redefine truth.

It must not make correctness depend
on stale state.

And its failure must not automatically
become a cascading platform outage.
```
