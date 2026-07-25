# ADR-059: Adopt Enterprise Redis Caching, Distributed Cache and Data Consistency Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-059 |
| Title | Adopt Enterprise Redis Caching, Distributed Cache and Data Consistency Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Redis, Caching, Distributed Systems, Performance, Resilience |
| Related Work Items | Redis, ResilientCache, Cache-Aside, TTL, Invalidation, Fallback Cache |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Caching is a performance optimization and resilience mechanism used throughout the Enterprise Order Platform.

Typical cacheable data includes:

```text
Reference Data

Configuration

Parameters

Product Metadata

Customer Relationships

Region Information

User Metadata

Expensive Query Results
```

Caching can substantially reduce:

```text
Database Load

Remote API Calls

Network Traffic

Request Latency

Dependency Pressure
```

However, an incorrectly designed cache introduces another class of distributed-system defects:

```text
Stale Data

Incorrect Invalidation

Cache Stampede

Memory Exhaustion

Serialization Failures

Cross-Tenant Data Leakage

Inconsistent Nodes

Redis Dependency Outages

Permanent Stale Fallback Data

Distributed Lock Deadlocks
```

A cache is therefore not merely a performance annotation.

It is a consistency boundary with explicit failure semantics.

---

# 2. Problem Statement

The organization requires standards covering:

- Redis
- cache-aside
- local fallback
- ResilientCache
- TTL
- key conventions
- serialization
- cache invalidation
- negative caching
- stampede protection
- single-flight
- cache warming
- eviction
- memory limits
- stale data
- write-through
- write-behind
- distributed locking
- Redis outages
- fallback synchronization
- consistency
- observability
- security
- testing
- Testcontainers

---

# 3. Decision Drivers

Primary drivers are:

1. predictable performance
2. controlled consistency
3. dependency protection
4. graceful degradation
5. bounded memory
6. operational simplicity
7. failure isolation
8. security
9. observability
10. testability

---

# 4. Decision

Redis is the preferred distributed cache where cross-instance cache sharing is required.

The default caching model is:

```text
APPLICATION
     |
     v
RESILIENT CACHE
     |
     +---------------------+
     |                     |
     v                     v
REDIS                  LOCAL CACHE
PRIMARY                FALLBACK
     |
     v
SOURCE OF TRUTH
```

For ordinary cacheable reads, the preferred strategy is:

```text
CACHE-ASIDE
```

The persistent database or authoritative remote service remains the source of truth.

---

# 5. Fundamental Principle

The governing principle is:

```text
A cache is an optimization.

It must not silently become
the authoritative source of truth.
```

---

# 6. Source of Truth

Every cached value MUST have an identifiable authoritative source.

Examples:

```text
PostgreSQL

External API

Configuration Service

Parameter Service
```

---

# 7. Cache Ownership

Every cache namespace MUST have an identifiable owning application/capability.

---

# 8. Cache-Aside

Cache-aside is the default read strategy.

Canonical flow:

```text
REQUEST
   |
   v
CACHE GET
   |
   +--> HIT ------> RETURN
   |
   +--> MISS
          |
          v
      SOURCE OF TRUTH
          |
          v
       CACHE PUT
          |
          v
        RETURN
```

---

# 9. Cache Miss

A cache miss is normal behavior.

It MUST NOT automatically be treated as an application failure.

---

# 10. Cache Hit

A cache hit SHOULD avoid unnecessary access to the authoritative source.

---

# 11. Cache Population

Cache population SHOULD occur only after successful retrieval of valid source data.

---

# 12. Cache Failure

Failure to populate a non-critical cache SHOULD NOT normally fail an otherwise successful business request.

---

# 13. Redis Role

Redis is a distributed optimization layer.

Redis MUST NOT become an accidental system of record for persistent business data unless an explicit architecture decision defines it as such.

---

# 14. Local Fallback

A bounded in-memory fallback cache MAY be used to improve resilience during temporary Redis failure.

---

# 15. ResilientCache Model

The standard resilient cache model is:

```text
GET
 |
 v
REDIS AVAILABLE?
 /             \
YES             NO
 |               |
 v               v
REDIS GET     LOCAL FALLBACK
 |               |
 +-------+-------+
         |
         v
       RESULT
```

---

# 16. Redis Hit

When Redis contains the value:

```text
Redis
  |
  v
Return Value
  |
  v
Synchronize Local Fallback
```

where local fallback synchronization is enabled.

---

# 17. Redis Miss

When Redis is available but the key is absent:

```text
Redis MISS
    |
    v
Source of Truth
    |
    v
Redis PUT
    |
    v
Local Fallback PUT
```

---

# 18. Redis Failure

When Redis access fails because of infrastructure failure:

```text
Redis Failure
    |
    v
Local Fallback
```

MAY provide degraded service.

---

# 19. Serialization Failure

Serialization/deserialization failure MUST be treated separately from an ordinary cache miss.

---

# 20. Corrupt Cached Value

A corrupt or incompatible cached representation SHOULD normally be:

```text
Detected

Evicted

Reloaded
```

when safe.

---

# 21. Failure Classification

Cache operations SHOULD distinguish:

```text
MISS

CONNECTION FAILURE

TIMEOUT

SERIALIZATION FAILURE

CORRUPT VALUE

APPLICATION FAILURE
```

---

# 22. Broad Exception Swallowing

This is prohibited:

```java
try {
    return redis.get(key);
} catch (Exception ex) {
    return null;
}
```

because it converts every defect into a false cache miss.

---

# 23. Supported Fallback Failures

Fallback behavior SHOULD be restricted to known infrastructure/cache failures such as applicable:

```text
RedisConnectionFailureException

Redis timeout

SerializationException
```

according to implementation semantics.

---

# 24. Unexpected Exception

Unexpected programming defects MUST NOT be silently hidden by fallback logic.

---

# 25. Local Cache Is Bounded

Every local fallback cache MUST have a maximum size.

---

# 26. Unbounded ConcurrentHashMap

An unbounded:

```java
ConcurrentHashMap
```

is NOT an acceptable long-lived production cache.

---

# 27. Local Cache Library

A bounded caching implementation such as Caffeine SHOULD be preferred for sophisticated local caching requirements.

---

# 28. Local Cache TTL

Fallback entries MUST expire.

---

# 29. Permanent Local Fallback

Permanent fallback entries are prohibited unless the cached data is genuinely immutable.

---

# 30. Multi-Instance Limitation

Local fallback values are node-specific.

Therefore:

```text
INSTANCE A

INSTANCE B

INSTANCE C
```

may temporarily observe different cached values during Redis degradation.

---

# 31. Fallback Consistency

Business capabilities using local fallback MUST tolerate temporary bounded inconsistency.

---

# 32. Critical Consistency

Data requiring strict immediate cross-instance consistency SHOULD NOT depend on local cache fallback.

---

# 33. Cache Key

Cache keys are integration contracts between application versions and cache content.

---

# 34. Key Convention

Keys MUST follow a stable convention.

Recommended structure:

```text
<application>:<domain>:<entity>:<version>:<identifier>
```

Example:

```text
customers:region:macro:v1:8e27...
```

---

# 35. Key Version

A version component SHOULD be used when serialized structure or semantic interpretation may change incompatibly.

---

# 36. Key Prefix

Application/domain prefixes MUST prevent collisions between unrelated capabilities.

---

# 37. Environment

Environment separation SHOULD primarily occur through infrastructure isolation or approved namespace strategy.

---

# 38. Tenant

If data is tenant-specific, tenant identity MUST be represented safely in cache isolation/key strategy.

---

# 39. Security Context

Cache keys MUST include every dimension that materially changes the returned data.

---

# 40. Authorization-Sensitive Cache

If a response differs according to:

```text
User

Role

Company

Tenant

Segment
```

those dimensions MUST be considered before caching.

---

# 41. Cross-User Leakage

A cache MUST NOT allow data computed for one authorization context to be returned to another unauthorized context.

---

# 42. Key Length

Cache keys SHOULD remain compact.

---

# 43. Raw Sensitive Data in Key

Sensitive personal data, access tokens and credentials MUST NOT appear directly in cache keys.

---

# 44. Hashing

Hashing MAY be used for large or sensitive key components where appropriate.

---

# 45. Key Determinism

Equivalent requests MUST generate equivalent keys when they are intended to share cached results.

---

# 46. Key Collision

Different semantic requests MUST NOT accidentally map to the same key.

---

# 47. TTL

Every non-immutable cache entry MUST have an explicit expiration strategy.

---

# 48. TTL Selection

TTL MUST be based on:

```text
Business Freshness Requirement

Source Cost

Change Frequency

Failure Tolerance

Memory Capacity
```

---

# 49. Arbitrary TTL

TTL MUST NOT be chosen solely because:

```text
"30 minutes seems reasonable."
```

---

# 50. Short TTL

Very short TTL can create:

```text
Low Hit Ratio

High Source Load

Cache Churn
```

---

# 51. Long TTL

Very long TTL can create:

```text
Stale Data

Delayed Corrections

Memory Pressure
```

---

# 52. TTL Is Not Invalidation

TTL is a safety mechanism.

It does not replace explicit invalidation where prompt consistency is required.

---

# 53. TTL Jitter

TTL jitter SHOULD be considered for high-volume keys to avoid synchronized expiration.

Conceptually:

```text
TTL = BASE_TTL + RANDOM_JITTER
```

---

# 54. Expiration Wave

Without jitter:

```text
100,000 keys created at 10:00
       |
       v
100,000 keys expire at 10:30
       |
       v
SOURCE SPIKE
```

---

# 55. Eviction

Redis memory policy MUST be explicit.

---

# 56. maxmemory

Production Redis SHOULD have controlled memory limits according to platform topology.

---

# 57. Eviction Policy

The Redis eviction policy MUST match workload semantics.

Possible strategies include:

```text
allkeys-lru

allkeys-lfu

volatile-lru

volatile-ttl

noeviction
```

depending on architecture.

---

# 58. Cache Workload

For a Redis instance dedicated purely to disposable cache data, eviction is generally acceptable.

---

# 59. Mixed Redis Workload

Cache and durable/coordination workloads SHOULD NOT be mixed casually in the same Redis instance when eviction semantics differ.

---

# 60. Memory Pressure

Redis memory pressure MUST be observable.

---

# 61. Serialization

Cached values MUST use a deliberate serialization strategy.

---

# 62. Java Native Serialization

Java native object serialization SHOULD NOT be the default distributed cache format.

---

# 63. JSON

JSON MAY be used where interoperability and diagnostics are valuable.

---

# 64. Binary Formats

Binary serialization MAY be used where performance and schema governance justify it.

---

# 65. Serialization Contract

The cache serialization contract MUST consider rolling deployments.

---

# 66. Rolling Deployment

During deployment:

```text
OLD APPLICATION
       +
NEW APPLICATION
       +
SHARED REDIS
```

may coexist.

Both versions MUST safely interpret shared cache entries or use isolated key versions.

---

# 67. Incompatible Cache Schema

For incompatible serialized changes, prefer:

```text
NEW CACHE KEY VERSION
```

rather than attempting unsafe deserialization.

---

# 68. Cache Migration

Cache data is normally disposable.

Migrating cached values is usually less desirable than allowing controlled repopulation.

---

# 69. Deserialization Security

Untrusted polymorphic deserialization MUST NOT be enabled indiscriminately.

---

# 70. Type Metadata

Serialization type metadata MUST NOT expose unnecessary implementation internals.

---

# 71. Invalidation

Cache invalidation semantics MUST be explicit.

---

# 72. Write Flow

Canonical write pattern:

```text
UPDATE SOURCE OF TRUTH
        |
        v
COMMIT
        |
        v
EVICT / INVALIDATE CACHE
```

---

# 73. Invalidate Before Commit

Invalidating before transaction commit can create a race:

```text
Evict Cache

Concurrent Reader Reloads Old DB Value

Transaction Commits

Old Value Remains Cached
```

---

# 74. After-Commit Invalidation

Where consistency requires it, invalidation SHOULD occur after successful transaction commit.

---

# 75. Transaction Synchronization

Spring transaction synchronization or an event/outbox mechanism MAY be used to coordinate after-commit invalidation.

---

# 76. Distributed Invalidation

When multiple application instances maintain local fallback caches, invalidation MAY require cross-instance propagation.

---

# 77. Local Eviction Only

Evicting only the current process's fallback cache does not invalidate other nodes.

---

# 78. Invalidation Event

A distributed invalidation mechanism MAY use:

```text
Kafka

Redis Pub/Sub

Dedicated Invalidation Topic
```

when justified.

---

# 79. Invalidation Reliability

If stale local values can materially affect business behavior, invalidation delivery MUST have appropriate reliability semantics.

---

# 80. Clear Operation

A cache-wide clear operation MUST be used carefully.

---

# 81. Namespace Clear

Prefer clearing only the owned namespace rather than indiscriminately flushing Redis.

---

# 82. FLUSHALL

Application code MUST NOT use:

```text
FLUSHALL
```

as ordinary invalidation behavior.

---

# 83. Cache Stampede

A cache stampede occurs when many requests simultaneously reload the same expired/missing value.

---

# 84. Stampede Example

```text
HOT KEY EXPIRES
      |
      v
10,000 REQUESTS MISS
      |
      v
10,000 DATABASE CALLS
```

---

# 85. Stampede Protection

High-volume expensive cache loads SHOULD implement stampede protection.

---

# 86. Single-Flight

Single-flight ensures one load per key while concurrent callers await/share the result.

Conceptually:

```text
100 REQUESTS
     |
     v
SAME CACHE KEY
     |
     v
ONE SOURCE LOAD
     |
     v
100 RESULTS
```

---

# 87. Per-Key Coordination

Stampede protection SHOULD coordinate per key rather than globally.

---

# 88. Global Lock

A single global cache-load lock is prohibited for high-concurrency systems.

---

# 89. Local Single-Flight

Local single-flight prevents duplicate loads inside one application instance.

---

# 90. Distributed Stampede

Multiple application instances may still simultaneously reload the same key.

---

# 91. Distributed Coordination

Distributed stampede protection MAY be used for exceptionally expensive/hot data.

---

# 92. Lock Cost

Distributed locking introduces additional failure modes and MUST NOT be the default solution for ordinary cache misses.

---

# 93. Stale-While-Revalidate

For suitable non-critical data, stale-while-revalidate MAY be used.

---

# 94. Stale-While-Revalidate Flow

```text
ENTRY EXPIRED
    |
    v
STALE VALUE AVAILABLE?
   /          \
 YES           NO
  |             |
  v             v
RETURN STALE   LOAD
  |
  v
ASYNC REFRESH
```

---

# 95. Maximum Staleness

Stale-while-revalidate MUST define a maximum acceptable stale period.

---

# 96. Stale Forever

Serving stale data indefinitely is prohibited.

---

# 97. Negative Caching

Known absence MAY be cached when repeated misses are expensive.

---

# 98. Negative Cache Example

```text
customerId -> NOT_FOUND
```

for a short controlled TTL.

---

# 99. Negative Cache TTL

Negative-cache TTL SHOULD normally be shorter than positive-cache TTL.

---

# 100. Mutable Existence

Negative caching MUST be used carefully when a resource may be created shortly after the miss.

---

# 101. Null Ambiguity

Cache APIs MUST distinguish:

```text
CACHE MISS

CACHED NULL / ABSENCE
```

if negative caching is supported.

---

# 102. Write-Through

Write-through caching MAY be used when the cache architecture explicitly requires synchronous cache updates.

---

# 103. Write-Through Risk

Source-of-truth and cache update ordering MUST be defined.

---

# 104. Write-Behind

Write-behind caching MUST NOT be used for critical business persistence without a dedicated architecture decision.

---

# 105. Data Loss Risk

A cache is not an acceptable implicit durable write queue for critical transactions.

---

# 106. Read-Through

Framework-managed read-through MAY be used when it preserves explicit failure and consistency semantics.

---

# 107. Spring Cache

Spring Cache abstraction MAY be used for straightforward caching.

---

# 108. Annotation Limitation

Annotations such as:

```java
@Cacheable
@CacheEvict
```

MUST NOT obscure complex consistency requirements.

---

# 109. Self Invocation

Proxy behavior of Spring cache annotations MUST be understood.

Self-invocation may bypass interception similarly to transactional proxies.

---

# 110. Conditional Cache

Conditional caching MAY be used to avoid caching inappropriate values.

---

# 111. Cache Exceptions

Cache infrastructure exceptions MUST be classified consistently.

---

# 112. Redis Timeout

Redis operations MUST have bounded timeouts.

---

# 113. Cache Must Be Faster

A cache timeout MUST generally be much shorter than the business operation timeout.

---

# 114. Slow Cache

A cache that consistently responds more slowly than the source can reduce system performance.

---

# 115. Redis Circuit Breaker

Circuit breaking MAY be considered when repeated Redis failures otherwise consume excessive latency.

---

# 116. Circuit Breaker Interaction

Redis circuit-breaker behavior MUST align with local fallback strategy.

---

# 117. Retry

Cache operations SHOULD NOT use aggressive retries.

---

# 118. Retry Amplification

This is dangerous:

```text
Request

Redis Retry x3

Database Retry x3

HTTP Retry x3
```

because retry multiplication can amplify load.

---

# 119. Redis Outage

Redis outage behavior MUST be predefined.

---

# 120. Degraded Mode

During Redis outage, the application MAY:

```text
Use Local Fallback

Load Source Directly

Reject Non-Critical Expensive Operation
```

depending on capacity and business requirements.

---

# 121. Source Protection

Fallback to the source MUST NOT create an uncontrolled thundering herd.

---

# 122. Redis Failure + Cache Miss

When both Redis and local fallback miss:

```text
SOURCE LOAD
```

MUST remain capacity-safe.

---

# 123. Load Shedding

Load shedding MAY be preferable to overwhelming the database during severe cache failure.

---

# 124. Fallback Freshness

Local fallback entries SHOULD retain independent expiration metadata.

---

# 125. Redis Recovery

When Redis recovers, cache behavior SHOULD naturally return to primary distributed-cache operation.

---

# 126. Fallback Synchronization

Successful Redis reads/writes MAY refresh the local fallback cache.

---

# 127. syncFallback

A `syncFallback` operation SHOULD:

```text
Update fallback safely

Preserve fallback TTL

Avoid throwing into successful Redis path
```

unless fallback correctness is itself critical.

---

# 128. put

A resilient `put` SHOULD consider:

```text
Redis Write

Fallback Synchronization

Serialization Failure

Redis Failure
```

---

# 129. get

A resilient `get` SHOULD consider:

```text
Redis Hit

Redis Miss

Redis Failure

Redis Serialization Failure

Fallback Hit

Fallback Miss
```

---

# 130. evict

A resilient `evict` SHOULD attempt to prevent stale fallback state.

---

# 131. Redis Eviction Failure

If distributed eviction fails but local eviction succeeds, the inconsistency MUST remain bounded by Redis TTL or another invalidation mechanism.

---

# 132. Local Eviction Failure

Unexpected local-cache failures SHOULD NOT be silently ignored if they can produce persistent incorrect behavior.

---

# 133. clear

`clear` MUST operate only on the intended cache namespace.

---

# 134. Redis KEYS

Production cache clearing MUST NOT rely on:

```text
KEYS *
```

for large Redis datasets.

---

# 135. SCAN

When key enumeration is unavoidable, Redis:

```text
SCAN
```

SHOULD be preferred over blocking `KEYS`.

---

# 136. Better Than Scan

Versioned namespace invalidation SHOULD be considered when bulk deletion would otherwise require expensive scans.

---

# 137. Namespace Versioning

Conceptually:

```text
customers:macro:v12:...
```

A logical namespace version change can make older entries unreachable and allow them to expire naturally.

---

# 138. Cache Warming

Cache warming MAY be used for predictable hot data.

---

# 139. Startup Warming

Application startup SHOULD NOT depend on loading the entire cache unless explicitly required.

---

# 140. Startup Storm

Simultaneous startup of many replicas can create a cache-warming storm.

---

# 141. Controlled Warming

Cache warming SHOULD be:

```text
Bounded

Rate Limited

Selective

Observable
```

---

# 142. Lazy Population

Lazy cache population remains the preferred default.

---

# 143. Distributed Lock

Redis distributed locks MAY be used only when the business operation genuinely requires distributed mutual exclusion.

---

# 144. Lock Is Not Transaction

A Redis lock does not provide a database transaction.

---

# 145. Lock Lease

Distributed locks MUST have bounded leases/expiration.

---

# 146. Infinite Lock

A lock without expiration is prohibited.

---

# 147. Lock Ownership

Only the lock owner MUST release the lock.

---

# 148. Token

A unique ownership token SHOULD be associated with the lock.

---

# 149. Safe Unlock

Unlock MUST verify ownership before deleting the lock.

---

# 150. SET NX

A lock MAY use atomic Redis semantics such as:

```text
SET key token NX PX <lease>
```

when appropriate.

---

# 151. Lock Expiration

A lease can expire while work is still running.

Therefore lock-based correctness MUST account for delayed/stalled owners.

---

# 152. Fencing Token

For critical distributed coordination, fencing tokens SHOULD be considered.

---

# 153. Redlock

Distributed lock algorithms MUST NOT be adopted mechanically without understanding their consistency assumptions and failure model.

---

# 154. Database Alternative

For database-owned resources, database locking or uniqueness constraints MAY be safer than Redis distributed locking.

---

# 155. Idempotency Alternative

Idempotency is often preferable to distributed locking for duplicate request protection.

---

# 156. Cache Consistency

Cache consistency requirements MUST be explicitly classified.

---

# 157. Strong Consistency

Strictly consistent data SHOULD normally bypass cache or use architecture specifically designed for strict consistency.

---

# 158. Eventual Consistency

Most reference-data caching uses bounded eventual consistency.

---

# 159. Bounded Staleness

The maximum acceptable stale interval SHOULD be known for business-relevant cached data.

---

# 160. Invalidation vs TTL

Consistency can be expressed as:

```text
STALE WINDOW
    <=
min(
    INVALIDATION PROPAGATION,
    TTL
)
```

depending on the design.

---

# 161. Cache Correctness

Performance gain MUST NOT override business correctness.

---

# 162. Cache Hit Ratio

Hit ratio MUST be measured.

---

# 163. High Hit Ratio

A high hit ratio is useful only when cached data is correct and memory cost is acceptable.

---

# 164. Low Hit Ratio

A consistently low hit ratio may indicate that caching adds complexity without sufficient benefit.

---

# 165. Cache Metrics

Applications SHOULD expose:

```text
Hits

Misses

Hit Ratio

Puts

Evictions

Load Time

Load Failures

Fallback Hits

Fallback Misses
```

where appropriate.

---

# 166. Redis Metrics

Operational Redis monitoring SHOULD include:

```text
Memory Usage

Evictions

Connections

Latency

CPU

Command Rate

Keyspace Hits

Keyspace Misses

Replication Health
```

---

# 167. Fallback Metrics

ResilientCache SHOULD expose metrics such as:

```text
redis.failure

fallback.hit

fallback.miss

fallback.sync

serialization.failure
```

according to platform observability conventions.

---

# 168. Metric Cardinality

Cache keys MUST NOT be used as unrestricted metric labels.

---

# 169. Logging

Cache logs SHOULD identify:

```text
Cache Name

Operation

Failure Classification
```

without logging sensitive values.

---

# 170. Miss Logging

Normal cache misses SHOULD NOT generate noisy warning/error logs.

---

# 171. Redis Failure Logging

Redis infrastructure failures SHOULD be visible without producing uncontrolled repeated logs during prolonged outages.

---

# 172. Log Sampling

Repeated cache-infrastructure errors MAY require rate limiting/sampling.

---

# 173. Security

Redis MUST follow least-privilege and network-isolation standards.

---

# 174. Authentication

Production Redis MUST use approved authentication mechanisms.

---

# 175. Encryption

Redis traffic SHOULD use approved encryption in transit where required by platform policy.

---

# 176. Internet Exposure

Production Redis MUST NOT be directly exposed to the public Internet.

---

# 177. Credentials

Redis credentials MUST NOT be committed to source code.

---

# 178. Sensitive Cached Data

Sensitive data SHOULD be minimized in caches.

---

# 179. Encryption at Rest

Infrastructure-level encryption at rest SHOULD follow platform/data-classification requirements.

---

# 180. Cache Data Retention

Cached sensitive data MUST not outlive legitimate business/security retention requirements.

---

# 181. Cache Key Enumeration

Redis access permissions SHOULD prevent unrelated applications from arbitrarily enumerating or modifying another application's keys where infrastructure supports isolation.

---

# 182. Testing Strategy

Caching behavior MUST have automated tests appropriate to complexity.

---

# 183. Unit Tests

Unit tests SHOULD validate:

```text
Key Generation

TTL Selection

Fallback Logic

Failure Classification

Invalidation Logic
```

---

# 184. ResilientCache Tests

`ResilientCache` tests SHOULD cover at minimum:

```text
Redis Hit

Redis Miss

Redis Failure + Fallback Hit

Redis Failure + Fallback Miss

Serialization Failure

put

evict

clear

syncFallback
```

---

# 185. AssertJ

Java tests SHOULD use AssertJ assertions with descriptive `.as("...")` context according to the established testing standard.

---

# 186. Redis Integration Test

Redis-specific behavior SHOULD be tested against real Redis where semantics matter.

---

# 187. Testcontainers Redis

Testcontainers SHOULD be used for integration tests involving:

```text
TTL

Expiration

Serialization

Redis Commands

Eviction Logic

Distributed Locking
```

where practical.

---

# 188. Mock Limitation

Mockito tests do not prove actual Redis TTL or command behavior.

---

# 189. Expiration Test

TTL tests SHOULD avoid fragile long `Thread.sleep` usage.

Prefer bounded polling/eventual assertions where appropriate.

---

# 190. Concurrency Test

Stampede/single-flight logic SHOULD have concurrent tests.

---

# 191. Failure Test

Redis outage behavior SHOULD be tested explicitly.

---

# 192. Recovery Test

Tests SHOULD validate recovery from fallback mode to normal Redis operation where implementation complexity warrants it.

---

# 193. Serialization Compatibility Test

Rolling-deployment-sensitive caches SHOULD test old/new representation compatibility or key-version isolation.

---

# 194. Performance Testing

High-volume caches SHOULD be performance-tested under realistic concurrency.

---

# 195. Stampede Test

A stampede-protected loader SHOULD verify that many concurrent requests for one missing key produce approximately one source load per coordination scope.

---

# 196. Cache Review Checklist

A material caching change SHOULD evaluate:

```text
[ ] What is the source of truth?

[ ] Is caching actually beneficial?

[ ] Is Redis required?

[ ] Is local fallback appropriate?

[ ] Is fallback bounded?

[ ] What is the TTL?

[ ] What is the maximum stale period?

[ ] Is explicit invalidation required?

[ ] Does invalidation happen after commit?

[ ] Is the cache key complete?

[ ] Could authorization context leak?

[ ] Is serialization rolling-deployment safe?

[ ] Is negative caching useful?

[ ] Can stampede occur?

[ ] Is single-flight required?

[ ] What happens when Redis fails?

[ ] Can the source survive cache failure?

[ ] Is load shedding required?

[ ] Are metrics available?

[ ] Are Redis integration tests present?
```

---

# 197. Cache Fitness Functions

Stable cache invariants SHOULD be automated where practical.

Examples:

```text
[ ] Cache keys use approved namespace

[ ] Local caches have maximum size

[ ] Local caches have expiration

[ ] Redis timeouts are bounded

[ ] Cache failures do not hide programming exceptions

[ ] Sensitive data is not present in keys

[ ] Cache clear does not use FLUSHALL

[ ] Production bulk deletion does not use KEYS *

[ ] Cache DTOs are serialization-safe

[ ] Critical caches expose hit/miss metrics

[ ] ResilientCache fallback behavior is tested
```

---

# 198. Enterprise Cache Gate

A cache implementation is not considered compliant when applicable conditions include:

```text
[ ] Source of truth is undefined

[ ] Cache is treated as accidental durable storage

[ ] Local fallback is unbounded

[ ] TTL is missing

[ ] Cache key can collide

[ ] Authorization-sensitive dimensions are omitted

[ ] Sensitive data is exposed in keys

[ ] Redis failures are caught as generic Exception and hidden

[ ] Cache invalidation occurs before DB commit without race analysis

[ ] Stampede can overwhelm the source

[ ] Redis outage can overwhelm PostgreSQL

[ ] Distributed lock has no expiration

[ ] Cache serialization breaks rolling deployment

[ ] FLUSHALL is used by application code

[ ] Redis KEYS * is used against large production keyspaces

[ ] Cache behavior has no observability
```

---

# 199. Anti-Patterns

The following are prohibited or strongly discouraged:

- caching without an identified source of truth
- using Redis because "cache is always faster"
- treating cache as authoritative persistent storage
- unbounded local fallback maps
- cache entries without TTL
- arbitrary TTL without freshness analysis
- cache keys containing tokens or passwords
- incomplete keys that ignore tenant/company/segment context
- swallowing every Redis exception
- converting serialization defects into normal misses
- using Java native serialization as the default distributed format
- incompatible cache schema changes without key versioning
- invalidating before transaction commit without race analysis
- relying exclusively on TTL where immediate invalidation is required
- global lock for all cache loads
- unlimited source reloads after hot-key expiration
- indefinite stale fallback
- excessive negative-cache TTL
- using write-behind as accidental business persistence
- aggressive Redis retries
- Redis outage causing uncontrolled database traffic
- distributed locks without lease
- deleting locks without verifying ownership
- using Redis lock where database uniqueness or idempotency is safer
- using `FLUSHALL`
- production `KEYS *` on large datasets
- warming the entire cache from every replica at startup
- logging every cache miss as warning
- using cache keys as high-cardinality metric dimensions
- assuming Mockito proves Redis semantics

---

# 200. Positive Consequences

The decision provides:

- lower latency
- reduced database load
- reduced remote-service traffic
- controlled Redis outage behavior
- bounded local fallback
- safer cache invalidation
- improved rolling-deployment compatibility
- stampede protection
- predictable TTL semantics
- better cache observability
- improved Redis security
- stronger cache testing

---

# 201. Negative Consequences

The decision introduces:

- additional cache design
- explicit TTL management
- fallback memory usage
- invalidation complexity
- serialization governance
- Redis monitoring
- concurrency tests
- additional failure modes

These costs are accepted because caching without explicit consistency and failure semantics can introduce defects that are significantly harder to diagnose than the performance problems caching was intended to solve.

---

# 202. Neutral Consequences

The decision also means:

- not every query should be cached
- not every cache requires Redis
- not every Redis cache requires local fallback
- not every miss requires negative caching
- not every hot key requires distributed locking
- stale data can be acceptable for some capabilities
- strict-consistency operations may intentionally bypass cache
- cache contents are generally disposable

---

# 203. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Stale data | High | Medium | TTL + invalidation |
| Cache stampede | High | Medium | Single-flight + jitter |
| Redis outage | High | Medium | Bounded fallback |
| Database overload during outage | Critical | Medium | Source protection |
| Memory exhaustion | Critical | Medium | Bounded cache + eviction |
| Cross-user leakage | Critical | Low/Medium | Complete key design |
| Serialization incompatibility | High | Medium | Key versioning |
| Invalid invalidation ordering | High | Medium | After-commit eviction |
| Distributed lock defect | Critical | Low/Medium | Lease + ownership |
| Cache complexity without benefit | Medium | Medium | Hit-ratio measurement |

---

# 204. Implementation Guidance

The following rules are mandatory:

1. Every cache must have an identifiable source of truth.
2. Cache-aside is the preferred default read strategy.
3. Redis should be used when distributed cache sharing provides measurable value.
4. Redis must not become accidental persistent business storage.
5. Local fallback must be bounded.
6. Local fallback must have expiration.
7. Local fallback may only be used where temporary cross-instance inconsistency is acceptable.
8. Cache keys must follow a stable namespace convention.
9. Every semantic dimension affecting the cached value must participate in key design.
10. Sensitive credentials must never appear in cache keys.
11. Non-immutable cache values must have explicit TTL.
12. TTL must reflect business freshness requirements.
13. TTL jitter should be considered for high-volume expiration patterns.
14. Redis memory and eviction policies must be explicit.
15. Serialization must remain compatible with rolling deployments.
16. Incompatible serialized representations should use new cache-key versions.
17. Cache invalidation must be coordinated with successful source-of-truth updates.
18. Database-backed invalidation should normally occur after transaction commit.
19. Multi-node local fallback invalidation must account for other instances.
20. High-volume expensive loads must consider stampede protection.
21. Single-flight coordination should be per key.
22. Stale-while-revalidate must have a maximum stale period.
23. Negative caching must use controlled TTL.
24. Cache infrastructure failures must be distinguished from ordinary misses.
25. Unexpected programming exceptions must not be hidden by fallback logic.
26. Redis operations must have bounded timeouts.
27. Redis retries must not amplify dependency failure.
28. Redis outage behavior must be predefined.
29. Source systems must be protected from cache-failure traffic spikes.
30. ResilientCache must synchronize fallback appropriately after successful Redis operations.
31. Cache-wide clear operations must remain namespace-scoped.
32. Application code must not use Redis FLUSHALL for normal invalidation.
33. Large production keyspaces must not use blocking KEYS-based invalidation.
34. Distributed locks must have bounded leases and ownership verification.
35. Idempotency/database constraints should be preferred over distributed locks where they solve the actual problem.
36. Cache behavior must expose useful metrics.
37. Sensitive cache content must follow enterprise security/data-governance standards.
38. Redis-specific behavior should be integration-tested against Redis.
39. ResilientCache must have explicit tests for Redis failure, serialization failure and fallback behavior.
40. Cache performance must be measured rather than assumed.

---

# 205. Validation

This ADR will be validated through:

- Redis
- Spring Data Redis
- Spring Cache
- Caffeine where applicable
- ResilientCache
- Testcontainers Redis
- JUnit 5
- AssertJ
- Mockito
- concurrency tests
- failure-injection tests
- Micrometer
- operational Redis metrics
- SonarQube
- SAST
- architecture fitness functions
- CI/CD quality gates

---

# 206. Success Criteria

The decision is successful when:

- cache hit ratios are measurable
- Redis outages no longer automatically become application outages
- Redis outages do not create uncontrolled database overload
- local fallback remains memory-bounded
- stale data windows are explicit
- invalidation races decrease
- serialization changes remain safe during rolling deployment
- cache stampedes are controlled
- sensitive cache data remains protected
- cache failures are diagnosable
- Redis-specific behavior is covered by integration tests
- caching demonstrably reduces latency or source load

---

# 207. Alternatives Rejected

## 207.1 Redis as Source of Truth by Default

Rejected because cache availability and eviction semantics differ from durable business persistence.

---

## 207.2 Unbounded Local Map Fallback

Rejected because Redis failure could eventually become JVM memory exhaustion.

---

## 207.3 TTL-Only Consistency for Every Cache

Rejected because some business data requires prompt invalidation.

---

## 207.4 Catch Exception and Return Null

Rejected because infrastructure and programming failures become indistinguishable from cache misses.

---

## 207.5 Distributed Lock for Every Cache Miss

Rejected because it adds unnecessary distributed coordination and failure modes.

---

## 207.6 Redis KEYS for Bulk Production Invalidation

Rejected because it can block Redis on large keyspaces.

---

## 207.7 Full Cache Warmup on Every Startup

Rejected because horizontally scaled deployments can overload the source simultaneously.

---

# 208. Related Decisions

This ADR extends and implements:

- ADR-014: Distributed Observability
- ADR-016: Application Resilience
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-057: Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard

---

# 209. References

- Redis Documentation
- Spring Data Redis Documentation
- Spring Cache Documentation
- Caffeine Documentation
- Testcontainers
- Cache-Aside Pattern
- Cache Stampede / Thundering Herd
- Single-Flight Pattern
- Cache Invalidation Patterns
- Distributed Systems Consistency Models
- OWASP
- Google Site Reliability Engineering
- Designing Data-Intensive Applications

---

# 210. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise Redis and distributed caching baseline |

---

# 211. Decision Summary

The cache architecture becomes:

```text
REQUEST
   |
   v
RESILIENT CACHE
   |
   +----------------------+
   |                      |
   v                      v
REDIS                 LOCAL FALLBACK
   |                      |
   +-----------+----------+
               |
               v
        SOURCE OF TRUTH
```

The primary read flow:

```text
CACHE
  |
  +--> HIT
  |     |
  |     v
  |   RETURN
  |
  +--> MISS
        |
        v
      SOURCE
        |
        v
      CACHE
        |
        v
      RETURN
```

During Redis failure:

```text
REDIS
  |
  X
FAILURE
  |
  v
LOCAL FALLBACK
  |
  +--> HIT --> RETURN
  |
  +--> MISS
        |
        v
   PROTECTED SOURCE LOAD
```

The local fallback must remain:

```text
BOUNDED
   +
EXPIRING
   +
OBSERVABLE
```

For writes:

```text
UPDATE DATABASE
       |
       v
     COMMIT
       |
       v
INVALIDATE CACHE
```

rather than:

```text
INVALIDATE
    |
    v
RELOAD OLD DATA
    |
    v
DATABASE COMMIT
```

For stampede protection:

```text
1,000 REQUESTS
      |
      v
SAME MISSING KEY
      |
      v
SINGLE-FLIGHT
      |
      v
ONE SOURCE LOAD
      |
      v
1,000 RESPONSES
```

For serialization evolution:

```text
OLD FORMAT
    |
    X
INCOMPATIBLE CHANGE
    |
    v
NEW KEY VERSION
    |
    v
LAZY REPOPULATION
```

For TTL:

```text
BUSINESS FRESHNESS
       +
SOURCE COST
       +
CHANGE FREQUENCY
       +
MEMORY CAPACITY
       =
TTL POLICY
```

For cache consistency:

```text
SOURCE OF TRUTH
       |
       v
CACHE
       |
       v
BOUNDED STALENESS
```

For distributed locking:

```text
NEED MUTUAL EXCLUSION?
        |
      NO|YES
        |
        v
CAN DB CONSTRAINT /
IDEMPOTENCY SOLVE IT?
      /       \
    YES        NO
     |          |
     v          v
USE THEM    CONSIDER
            DISTRIBUTED
               LOCK
```

The complete caching equation is:

```text
CLEAR SOURCE OF TRUTH
        +
STABLE CACHE KEYS
        +
BOUNDED TTL
        +
SAFE SERIALIZATION
        +
CORRECT INVALIDATION
        +
BOUNDED LOCAL FALLBACK
        +
STAMPEDE PROTECTION
        +
SOURCE PROTECTION
        +
OBSERVABILITY
        +
REAL REDIS TESTING
        =
RELIABLE ENTERPRISE CACHING
```

The governing principle is:

```text
Cache only when it provides
measurable value.

Keep the source of truth explicit.

Bound memory.

Bound staleness.

Bound timeouts.

Do not hide programming defects
as cache misses.

Invalidate only when the
authoritative update is safe.

Do not let a hot-key expiration
become a database outage.

Do not let a Redis outage become
a JVM memory leak.

Do not use distributed locks
when idempotency or database
constraints solve the real problem.

And design every fallback knowing
that degraded service must still
preserve business correctness.
```
