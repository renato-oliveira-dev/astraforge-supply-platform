# ADR-080: Adopt Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-080 |
| Title | Adopt Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Caching, Redis, Local Cache, Performance, Resilience |
| Related Work Items | Redis, Caffeine, Spring Cache, ResilientCache, Kubernetes, PostgreSQL |
| Supersedes | ADR-059 |
| Superseded By | None |

---

# 1. Context

Enterprise applications frequently cache data to reduce:

```text
DATABASE LOAD

REMOTE API CALLS

NETWORK LATENCY

CPU-INTENSIVE CALCULATIONS

REPEATED LOOKUPS
```

Common cached information includes:

```text
PARAMETERS

PRODUCT METADATA

COMPANY INFORMATION

CUSTOMER REFERENCES

REGION INFORMATION

PERMISSIONS

REFERENCE TABLES

REMOTE API RESPONSES
```

Caching can substantially improve performance.

However, caching introduces another copy of data.

The architecture changes from:

```text
APPLICATION
    |
    v
SOURCE OF TRUTH
```

to:

```text
APPLICATION
    |
    +--> CACHE
    |
    v
SOURCE OF TRUTH
```

Once another copy exists, the platform must address:

```text
STALE DATA

INVALIDATION

TTL

CONSISTENCY

SERIALIZATION

FAILURE

CONCURRENCY

CACHE STAMPEDE

MEMORY PRESSURE

MULTI-POD BEHAVIOR
```

Caching therefore cannot be treated merely as a performance annotation.

---

# 2. Problem Statement

The organization requires standards covering:

- Redis
- Spring Cache
- Caffeine/local cache
- distributed cache
- cache-aside
- read-through behavior
- write invalidation
- TTL
- cache keys
- namespaces
- serialization
- cache invalidation
- multi-instance consistency
- negative caching
- stale data
- stale-while-revalidate
- cache stampede
- request coalescing
- cache warming
- Redis outages
- resilient fallback
- local fallback
- circuit breaker
- cache penetration
- eviction
- memory limits
- PII
- authorization data
- observability
- testing

---

# 3. Decision Drivers

Primary drivers are:

1. performance
2. resilience
3. predictable consistency
4. bounded memory
5. multi-instance correctness
6. operational simplicity
7. graceful degradation
8. security
9. maintainability
10. observability
11. scalability
12. recoverability

---

# 4. Decision

Caching MUST be introduced only when:

```text
DATA IS READ FREQUENTLY
        |
        +
SOURCE ACCESS HAS MATERIAL COST
        |
        +
STALE-DATA SEMANTICS ARE ACCEPTABLE
```

The default distributed caching pattern is:

```text
APPLICATION
    |
    v
CACHE
    |
  HIT?
  / \
YES  NO
 |    |
 v    v
RETURN SOURCE
       |
       v
    CACHE
       |
       v
    RETURN
```

This is the Cache-Aside pattern.

---

# 5. Fundamental Principle

```text
A cache is not
the source of truth.

Every cache entry must have:

an identity,

a lifetime,

an invalidation strategy,

a failure strategy,

and an acceptable
staleness model.
```

---

# 6. Source of Truth

Every cached dataset MUST have one authoritative source of truth.

Examples:

```text
PostgreSQL

External API

Configuration Service

Identity Provider
```

---

# 7. Cache as Database

Redis MUST NOT accidentally become the authoritative business database merely because application code starts depending on cached values.

---

# 8. Cache Classification

Caches SHOULD be classified as:

```text
LOCAL

DISTRIBUTED

TWO-LEVEL

COMPUTATION CACHE

NEGATIVE CACHE
```

---

# 9. Local Cache

Local cache exists inside one application instance.

Example:

```text
POD A
 |
 +--> CAFFEINE CACHE
```

---

# 10. Distributed Cache

Distributed cache is shared across instances.

Example:

```text
POD A ---+
         |
POD B ---+--> REDIS
         |
POD C ---+
```

---

# 11. Local Cache Benefit

Local cache provides:

```text
Very Low Latency

No Network Round Trip

Reduced Redis Load
```

---

# 12. Local Cache Risk

Each pod has an independent copy.

```text
POD A = VALUE V2

POD B = VALUE V1

POD C = VALUE V1
```

is possible.

---

# 13. Local Cache Suitability

Local cache SHOULD only be used where temporary cross-instance inconsistency is acceptable or distributed invalidation exists.

---

# 14. Redis

Redis SHOULD be the default shared distributed cache where a distributed cache is required and Redis operational characteristics are appropriate.

---

# 15. Redis Is Remote

Redis MUST be treated as a remote dependency.

Redis can experience:

```text
TIMEOUT

NETWORK FAILURE

CONNECTION EXHAUSTION

FAILOVER

CLUSTER REBALANCE

SERIALIZATION FAILURE
```

---

# 16. Redis Is Not Free

A Redis lookup still has:

```text
Network Latency

Serialization Cost

Connection Cost

Infrastructure Cost
```

Caching extremely cheap local operations in Redis MAY reduce rather than improve performance.

---

# 17. Cache-Aside

Cache-Aside SHOULD be the default application caching model.

---

# 18. Cache-Aside Read

Canonical flow:

```text
READ REQUEST
     |
     v
CACHE GET
     |
   HIT?
   / \
 YES  NO
  |    |
  v    v
RETURN DB/API
        |
        v
     CACHE PUT
        |
        v
      RETURN
```

---

# 19. Cache Miss

A cache miss is normal behavior and MUST NOT normally be treated as an application error.

---

# 20. Write Strategy

For authoritative database updates, the default strategy SHOULD be:

```text
UPDATE SOURCE
      |
      v
INVALIDATE CACHE
```

---

# 21. Cache Update Before Database

This is unsafe:

```text
UPDATE CACHE
      |
      X
DATABASE UPDATE FAILS
```

because cache may advertise state that never became authoritative.

---

# 22. Database First

The authoritative update MUST succeed before cache invalidation/update is considered successful.

---

# 23. Transaction Boundary

Cache mutation SHOULD NOT be assumed to participate atomically in a relational database transaction.

---

# 24. Commit-Aware Invalidation

Where required, cache invalidation SHOULD occur after successful database commit.

---

# 25. Rollback

A rolled-back database transaction MUST NOT publish a new cache value as if the transaction succeeded.

---

# 26. Invalidation Failure

If database commit succeeds but cache invalidation fails, stale cache may remain.

The architecture MUST define recovery.

---

# 27. TTL as Safety Net

A bounded TTL SHOULD normally exist even when explicit invalidation is implemented.

---

# 28. TTL

Every non-permanent cache entry MUST have an intentional lifetime.

---

# 29. Arbitrary TTL

TTL MUST NOT be selected solely because:

```text
"30 minutes seems reasonable"
```

---

# 30. TTL Inputs

TTL SHOULD consider:

```text
Data Change Frequency

Acceptable Staleness

Source Cost

Traffic

Memory

Invalidation Reliability
```

---

# 31. Short TTL

A very short TTL can reduce cache effectiveness and increase source load.

---

# 32. Long TTL

A very long TTL can increase stale-data exposure.

---

# 33. No TTL

Indefinite caching requires explicit justification and reliable invalidation.

---

# 34. TTL Jitter

High-volume cache entries SHOULD consider TTL jitter.

---

# 35. Expiration Synchronization

Without jitter:

```text
1,000,000 KEYS
TTL = 60 MINUTES
CREATED TOGETHER
      |
      v
EXPIRE TOGETHER
      |
      v
SOURCE LOAD SPIKE
```

---

# 36. TTL Jitter Model

Prefer bounded variation:

```text
BASE TTL
   +
RANDOM JITTER
```

where exact expiration is not required.

---

# 37. Cache Key

Cache keys MUST be deterministic.

---

# 38. Key Namespace

Keys SHOULD use explicit namespaces.

Example:

```text
customers:v2:{customerId}
```

instead of:

```text
{customerId}
```

---

# 39. Key Version

A version component MAY simplify incompatible cache-schema changes.

---

# 40. Key Collision

Different logical data types MUST NOT accidentally share the same key space.

---

# 41. Key Composition

All dimensions affecting the cached result MUST participate in the key.

---

# 42. Missing Dimension

If response depends on:

```text
customerId
+
segment
+
process
```

a cache key containing only:

```text
customerId
```

is incorrect.

---

# 43. Authorization Context

If cached output varies by authorization context, relevant security dimensions MUST participate in cache design.

---

# 44. Cross-User Leak

Caching a user-specific response under a global key can expose one user's data to another.

This is prohibited.

---

# 45. PII in Key

Sensitive personal information SHOULD NOT appear directly in cache keys.

---

# 46. Token in Key

Access tokens, passwords and secrets MUST NOT appear in cache keys.

---

# 47. Key Length

Cache key size SHOULD remain bounded.

---

# 48. Serialization

Distributed cached values require explicit serialization.

---

# 49. Java Native Serialization

Java native object serialization SHOULD NOT be the default distributed cache format.

---

# 50. Portable Format

JSON or another explicitly governed portable format SHOULD generally be preferred.

---

# 51. Serialization Contract

Cached representation SHOULD be treated as a compatibility contract when multiple application versions may read the same entry.

---

# 52. Rolling Deployment

During rolling deployment:

```text
OLD POD
   |
   +--> CACHE

NEW POD
   |
   +--> SAME CACHE
```

Both versions may encounter the same cached representation.

---

# 53. Cache Schema Evolution

Cache value changes MUST account for mixed-version deployments.

---

# 54. Versioned Namespace

Incompatible cache representation changes SHOULD use a new key namespace/version.

Example:

```text
customers:v1:...
customers:v2:...
```

---

# 55. Deserialization Failure

A deserialization failure SHOULD normally be treated as a recoverable cache miss when safe.

---

# 56. Corrupt Entry

A corrupt/incompatible cache entry SHOULD be evicted where appropriate.

---

# 57. Serialization Error Logging

Serialization failures MUST be logged safely without dumping sensitive cached values.

---

# 58. Negative Caching

Negative caching stores absence.

Example:

```text
customer:123
    |
    v
NOT FOUND
```

---

# 59. Negative Cache Benefit

It can prevent repeated expensive source queries for missing resources.

---

# 60. Negative Cache Risk

A resource may be created while an old negative entry remains.

---

# 61. Negative TTL

Negative cache TTL SHOULD generally be shorter than positive cache TTL.

---

# 62. Negative Invalidation

Resource creation SHOULD invalidate applicable negative entries where practical.

---

# 63. Cache Penetration

Repeated requests for nonexistent keys can bypass cache and overload the source.

Negative caching MAY mitigate this.

---

# 64. Null Caching

Caching `null` MUST have explicit semantics.

---

# 65. Cache Stampede

A cache stampede occurs when many requests miss the same hot key simultaneously.

---

# 66. Stampede Example

```text
HOT KEY EXPIRES
      |
      v
1000 REQUESTS
      |
      v
1000 CACHE MISSES
      |
      v
1000 DATABASE CALLS
```

---

# 67. Stampede Protection

Hot/expensive keys SHOULD use stampede protection where needed.

---

# 68. Request Coalescing

Preferred model:

```text
1000 MISSES
    |
    v
ONE LOAD
    |
    v
999 WAIT / REUSE
```

---

# 69. Per-Key Lock

Per-key synchronization MAY be used locally for request coalescing.

---

# 70. Global Lock

A global cache-load lock SHOULD NOT serialize unrelated keys.

---

# 71. Distributed Stampede

For multiple replicas, local synchronization alone may still produce:

```text
ONE LOAD PER POD
```

---

# 72. Distributed Lock

A short-lived distributed lock MAY be used for extremely expensive hot-key reconstruction when justified.

---

# 73. Lock Complexity

Distributed locks SHOULD NOT be added to every cache lookup.

---

# 74. Lock Failure

Cache correctness MUST NOT depend solely on a distributed cache-rebuild lock.

---

# 75. Stale-While-Revalidate

Some read-heavy data MAY use stale-while-revalidate semantics.

---

# 76. Stale Model

Conceptually:

```text
ENTRY FRESH
   |
   v
RETURN

ENTRY STALE
   |
   +--> RETURN STALE
   |
   +--> REFRESH ASYNC
```

---

# 77. Stale Data Acceptance

Stale-while-revalidate MUST only be used when bounded stale data is business-safe.

---

# 78. Financial/Security Data

Strongly consistency-sensitive financial, authorization or security decisions SHOULD NOT rely on arbitrarily stale cache data.

---

# 79. Authorization Cache

Authorization caching requires particularly short, explicit revocation semantics.

---

# 80. Revoked Permission

A revoked permission MUST not remain effective indefinitely because of cache TTL.

---

# 81. Security Cache Review

Security-sensitive caches require explicit threat analysis.

---

# 82. Cache Invalidation

Every cache MUST define how it is invalidated.

---

# 83. Invalidation Strategies

Possible strategies include:

```text
TTL ONLY

EXPLICIT EVICTION

EVENT-DRIVEN INVALIDATION

VERSIONED KEYS

CACHE REPLACEMENT
```

---

# 84. TTL-Only

TTL-only invalidation is suitable only when bounded staleness is acceptable.

---

# 85. Explicit Eviction

Write paths SHOULD explicitly evict affected entries when freshness requirements justify it.

---

# 86. Related Keys

An update may invalidate multiple keys.

Example:

```text
CUSTOMER UPDATE
      |
      +--> customerById
      |
      +--> customerSearch
      |
      +--> consultantResponsible
```

---

# 87. Cache Dependency Map

Material caches SHOULD document which mutations affect which keys.

---

# 88. Query Cache

Caching arbitrary search results can create complex invalidation requirements.

---

# 89. Search Cache

Search/list caches SHOULD be introduced cautiously.

---

# 90. Entity Cache

Caching stable entity/reference lookups is generally simpler than caching arbitrary query combinations.

---

# 91. Event-Driven Invalidation

Distributed invalidation MAY use integration events.

---

# 92. Example

```text
CUSTOMER UPDATED
      |
      v
EVENT
      |
      +--> POD / CACHE CONSUMER
      |
      v
EVICT CUSTOMER CACHE
```

---

# 93. At-Least-Once Invalidation

Invalidation consumers MUST tolerate duplicate events.

---

# 94. Lost Invalidation

TTL SHOULD remain a safety net against missed invalidation events where possible.

---

# 95. Ordering

Out-of-order invalidation/update events MUST be considered.

---

# 96. Cache Update Events

Publishing complete new values to caches is more sensitive to ordering than simple eviction.

---

# 97. Prefer Eviction

When ordering is difficult, event-driven eviction SHOULD generally be preferred over event-driven cache-value replacement.

---

# 98. Multi-Level Cache

A two-level cache MAY combine:

```text
L1 = LOCAL CACHE

L2 = REDIS
```

---

# 99. Two-Level Read

```text
REQUEST
   |
   v
L1
 |
MISS
 |
 v
L2
 |
MISS
 |
 v
SOURCE
 |
 +--> L2
 |
 +--> L1
```

---

# 100. Two-Level Complexity

Two-level caching introduces additional consistency complexity.

---

# 101. L1 Staleness

Invalidating Redis does not automatically invalidate each pod's local L1 cache.

---

# 102. Distributed L1 Invalidation

Two-level cache SHOULD use:

```text
Short L1 TTL

and/or

Distributed Invalidation
```

---

# 103. Resilient Cache

For selected reference data, the platform MAY use:

```text
PRIMARY REDIS CACHE
       |
       v
REDIS FAILURE?
       |
     +-+-+
     |   |
    NO  YES
     |   |
     v   v
  REDIS LOCAL FALLBACK
```

---

# 104. Local Fallback Purpose

Local fallback exists to preserve availability during temporary distributed-cache failure.

---

# 105. Local Fallback Is Not Redis Replacement

Fallback MUST NOT silently become the permanent cache architecture.

---

# 106. Source vs Local Fallback

The fallback policy MUST define whether Redis failure causes:

```text
LOCAL FALLBACK LOOKUP

SOURCE LOOKUP

LOCAL + SOURCE

FAIL REQUEST
```

---

# 107. Business Safety

Fallback choice depends on data semantics.

---

# 108. Reference Data

Relatively stable reference data is a good candidate for local fallback.

---

# 109. Volatile Data

Highly volatile business state may be unsafe for local stale fallback.

---

# 110. Fallback TTL

Local fallback MUST have bounded TTL/size.

---

# 111. Fallback Synchronization

Successful primary cache/source reads MAY synchronize the local fallback where appropriate.

---

# 112. Fallback Eviction

Primary cache eviction SHOULD also evict local fallback where feasible.

---

# 113. Fallback Clear

Cache clear operations SHOULD account for both cache levels.

---

# 114. Redis Failure

Redis failures MUST NOT automatically become HTTP 500 when the cache is optional and source access remains healthy.

---

# 115. Optional Cache

For an optional cache:

```text
CACHE FAILURE
     |
     v
SOURCE OF TRUTH
     |
     v
SUCCESS
```

is preferable.

---

# 116. Cache as Availability Dependency

If source capacity cannot survive cache loss, Redis has effectively become an availability-critical dependency.

This MUST be recognized operationally.

---

# 117. Redis Outage Load

Fallback-to-source behavior MUST be capacity-tested.

---

# 118. Cache Collapse

A Redis outage can cause:

```text
CACHE HIT RATE -> 0

DATABASE LOAD -> VERY HIGH
```

---

# 119. Source Protection

Redis-failure fallback SHOULD use:

```text
Bounded Concurrency

Circuit Breaker where appropriate

Request Coalescing

Rate Limiting

Local Fallback
```

to protect the source.

---

# 120. Redis Timeout

Redis operations MUST have bounded timeouts.

---

# 121. Long Redis Timeout

Waiting several seconds for an optional cache defeats its performance purpose.

---

# 122. Fail Fast

Optional cache access SHOULD fail quickly enough to allow fallback within the request latency budget.

---

# 123. Redis Retry

Redis retries MUST be bounded.

---

# 124. Retry Storm

Application-level Redis retries SHOULD not amplify infrastructure failover problems.

---

# 125. Circuit Breaker

A Circuit Breaker MAY be used around Redis access when repeated failures would otherwise add latency and load.

---

# 126. Circuit Open

When Redis Circuit Breaker is open, the application SHOULD immediately execute its defined fallback path.

---

# 127. Circuit Breaker Scope

Cache Circuit Breaker SHOULD distinguish Redis availability failures from application programming/serialization defects where practical.

---

# 128. Serialization Failure

A serialization bug SHOULD NOT be hidden indefinitely as if Redis were unavailable.

---

# 129. Connection Pool

Redis connection pools MUST be bounded.

---

# 130. Connection Exhaustion

Connection-pool exhaustion MUST have bounded wait time.

---

# 131. Pool Sizing

Redis pool sizing SHOULD consider:

```text
POD COUNT

REQUEST CONCURRENCY

CACHE OPERATIONS PER REQUEST

REDIS CAPACITY
```

---

# 132. Multiplication

Example:

```text
20 PODS
x
100 CONNECTIONS
=
2000 POTENTIAL CONNECTIONS
```

must be considered.

---

# 133. Memory

Redis memory usage MUST be bounded.

---

# 134. Maxmemory

Production Redis SHOULD have explicit memory/capacity management.

---

# 135. Eviction Policy

Redis eviction policy MUST match cache semantics.

---

# 136. Cache vs Persistent Redis

Redis instances used as disposable caches SHOULD be architecturally distinguished from Redis used for durable/coordination purposes.

---

# 137. `noeviction`

`noeviction` may be appropriate for some non-cache Redis use cases but can cause cache writes to fail under memory pressure.

---

# 138. LRU/LFU

LRU/LFU-style eviction MAY be appropriate for cache workloads depending on access patterns.

---

# 139. Application Correctness

Application correctness MUST NOT depend on a cache entry remaining present until TTL.

Redis may evict it earlier.

---

# 140. Cache Miss After Eviction

Unexpected eviction MUST behave as a normal miss.

---

# 141. Local Cache Size

Local caches MUST have explicit maximum size/weight.

---

# 142. Unbounded Map

This is prohibited as a cache:

```java
new ConcurrentHashMap<>()
```

with unlimited growth and no eviction policy.

---

# 143. Caffeine

Caffeine SHOULD be preferred over ad hoc in-memory maps for bounded local caching.

---

# 144. Local Cache Policy

Local caches SHOULD define:

```text
maximumSize / maximumWeight

expireAfterWrite / expireAfterAccess

statistics where useful
```

---

# 145. Reference Retention

Cached values SHOULD not unintentionally retain huge object graphs.

---

# 146. Cache Warming

Cache warming MAY be used for predictable hot reference data.

---

# 147. Startup Warming

Application readiness SHOULD NOT normally wait for warming every optional cache entry.

---

# 148. Warm Incrementally

Large cache warming SHOULD be incremental and bounded.

---

# 149. Warm Storm

When 50 pods start simultaneously:

```text
50 PODS
   |
   v
ALL WARM SAME DATA
   |
   v
SOURCE OVERLOAD
```

must be prevented.

---

# 150. Lazy Loading

Lazy loading is often safer than warming the entire dataset.

---

# 151. Selective Warmup

Only demonstrated hot keys SHOULD be considered for proactive warming.

---

# 152. Cache Preload Failure

Optional cache-preload failure SHOULD NOT necessarily prevent application startup.

---

# 153. Kubernetes Readiness

Readiness MUST reflect whether the application can actually serve required traffic.

---

# 154. Optional Redis

If application can safely serve from the source without Redis, Redis failure SHOULD NOT automatically make readiness false.

---

# 155. Mandatory Redis

If Redis is genuinely required for correctness, readiness MAY depend on it.

---

# 156. Explicit Classification

Redis dependency MUST therefore be classified as:

```text
OPTIONAL PERFORMANCE DEPENDENCY

DEGRADABLE DEPENDENCY

MANDATORY CORRECTNESS DEPENDENCY
```

---

# 157. Health Check

Health endpoints SHOULD distinguish cache degradation from complete application failure where appropriate.

---

# 158. Cache Clear

Administrative cache-clear operations MUST be protected.

---

# 159. Global Clear

A global cache flush is a high-impact operation.

---

# 160. `FLUSHALL`

Production application code MUST NOT casually execute:

```text
FLUSHALL
```

---

# 161. Namespace Clear

Application cache cleanup SHOULD target controlled namespaces.

---

# 162. Cache Eviction Endpoint

Administrative eviction endpoints MUST require strong authorization.

---

# 163. Bulk Eviction

Bulk key deletion MUST be designed to avoid blocking Redis.

---

# 164. `KEYS *`

Production cache maintenance SHOULD NOT use blocking broad:

```text
KEYS *
```

on large Redis datasets.

---

# 165. SCAN

Incremental `SCAN`-based strategies MAY be used when namespace traversal is unavoidable.

---

# 166. Prefer Version Rotation

For large namespaces, rotating a cache namespace/version MAY be safer than deleting millions of keys synchronously.

---

# 167. Observability

Caching MUST be observable.

---

# 168. Metrics

Useful metrics include:

```text
cache_gets

cache_hits

cache_misses

cache_puts

cache_evictions

cache_errors

fallback_hits

source_loads

load_duration

redis_latency
```

---

# 169. Hit Ratio

Hit ratio SHOULD be measured for important caches.

---

# 170. High Hit Ratio Is Not Enough

A high hit ratio does not prove caching is beneficial if:

```text
cache lookup cost
>
source lookup cost
```

---

# 171. Cache Value Metric

Cache keys/values MUST NOT become metric labels.

---

# 172. Cache Name

A bounded logical cache name MAY be a metric label.

---

# 173. Result Dimension

Bounded result labels MAY include:

```text
hit

miss

fallback

error
```

---

# 174. Redis Metrics

Operational Redis metrics SHOULD include:

```text
memory usage

evictions

connections

latency

CPU

network

keyspace hits

keyspace misses
```

---

# 175. Fallback Metric

Local fallback usage MUST be observable.

---

# 176. Silent Fallback

A system MUST NOT silently operate on fallback for days without operators knowing Redis is unhealthy.

---

# 177. Degraded Mode

Cache fallback SHOULD produce a degraded-mode signal.

---

# 178. Logs

Cache logs SHOULD avoid one log entry per hit.

---

# 179. Cache Hit Logging

Normal cache hits SHOULD generally not be logged at INFO.

---

# 180. Cache Error Logging

Redis failures SHOULD be logged with bounded frequency to avoid log storms.

---

# 181. Error Sanitization

Cache error logs MUST not expose cached PII or secrets.

---

# 182. Tracing

Cache spans MAY be useful when distributed tracing is enabled.

---

# 183. Trace Attributes

Safe attributes MAY include:

```text
cache.system=redis

cache.name

cache.operation
```

---

# 184. Key in Trace

Full sensitive cache keys SHOULD NOT be placed into traces.

---

# 185. Alerting

Applicable alerts include:

```text
Redis Unavailable

Redis Latency High

Eviction Surge

Memory Saturation

Connection Saturation

Hit Ratio Collapse

Fallback Usage Surge

Source Load Surge
```

---

# 186. Cache SLO

Redis latency/availability MAY have explicit SLOs when cache is operationally important.

---

# 187. Degradation SLO

Applications with resilient fallback SHOULD also monitor degraded-mode duration.

---

# 188. Testing Strategy

Cache behavior requires dedicated tests.

---

# 189. Cache Hit Test

Verify:

```text
FIRST CALL -> SOURCE

SECOND CALL -> CACHE
```

---

# 190. Cache Miss Test

Missing entry MUST correctly load from source.

---

# 191. Put Test

Successful source load SHOULD populate cache when configured.

---

# 192. Eviction Test

Source mutation SHOULD invalidate the applicable cache.

---

# 193. Rollback Test

A rolled-back transaction MUST NOT expose an uncommitted cache state.

---

# 194. TTL Test

Expiration semantics SHOULD be tested without fragile long sleeps.

---

# 195. Negative Cache Test

Repeated missing-resource lookup SHOULD verify negative caching when enabled.

---

# 196. Negative Invalidation Test

Creating the previously missing resource SHOULD invalidate its negative cache where required.

---

# 197. Serialization Test

Distributed cache serialization/deserialization MUST be tested.

---

# 198. Compatibility Test

Rolling-deployment-sensitive cache formats SHOULD test compatible old/new representations where required.

---

# 199. Redis Failure Test

When Redis is unavailable, verify the defined fallback behavior.

---

# 200. Local Fallback Test

Resilient cache SHOULD test:

```text
Redis Success

Redis Connection Failure

Serialization Failure

Local Fallback Hit

Local Fallback Miss

Source Success

Source Failure
```

---

# 201. Fallback Synchronization Test

Successful Redis/source reads SHOULD synchronize fallback according to policy.

---

# 202. Evict Both Test

Eviction SHOULD remove primary and local fallback values where applicable.

---

# 203. Clear Test

Clear operations SHOULD affect all intended cache levels.

---

# 204. Stampede Test

Concurrent misses for one key SHOULD verify bounded source calls when stampede protection exists.

---

# 205. Concurrency Test

Concurrency tests SHOULD use deterministic coordination primitives rather than `Thread.sleep`.

---

# 206. Multi-Key Test

Request coalescing for one key MUST NOT block unrelated keys unnecessarily.

---

# 207. Memory Test

Large local-cache scenarios SHOULD verify maximum-size eviction.

---

# 208. Cache Key Test

Key generation SHOULD have deterministic unit tests.

---

# 209. Security Test

User-specific cache tests MUST verify one user's data cannot be returned to another.

---

# 210. PII Test

Sensitive values SHOULD not appear in keys/logs/metrics.

---

# 211. Testcontainers

Redis integration tests SHOULD use Testcontainers where actual Redis behavior matters.

---

# 212. Mock Limitation

Mocking `RedisTemplate` alone does not validate:

```text
Serialization

TTL

Actual Key Behavior

Connection Failure

Redis Data Types
```

---

# 213. Failure Injection

Critical fallback architecture SHOULD test real Redis unavailability.

---

# 214. AssertJ

Java tests MUST follow established conventions, including meaningful:

```java
.as("...")
```

before applicable assertions.

---

# 215. Cache Architecture Review Checklist

```text
[ ] Why is this data cached?

[ ] What is the source of truth?

[ ] Is cache actually faster/cheaper?

[ ] Is cache local or distributed?

[ ] Is temporary staleness acceptable?

[ ] What is the cache key?

[ ] Does the key include every response dimension?

[ ] Could the key leak PII?

[ ] What is the TTL?

[ ] Why was that TTL selected?

[ ] Is TTL jitter required?

[ ] How is cache invalidated?

[ ] What happens if invalidation is lost?

[ ] Can Redis evict the entry early?

[ ] Is serialization version-compatible?

[ ] What happens during rolling deployment?

[ ] Is negative caching useful?

[ ] Can negative caching hide newly created data?

[ ] Can a hot-key expiration cause stampede?

[ ] Is request coalescing needed?

[ ] Is stale fallback business-safe?

[ ] What happens when Redis is unavailable?

[ ] Can the source survive Redis outage?

[ ] Is local fallback bounded?

[ ] Is local fallback invalidated?

[ ] Is Redis optional or mandatory?

[ ] Is local cache size bounded?

[ ] Could cache warming overload the source?

[ ] Are cache metrics available?

[ ] Can operators see degraded fallback mode?
```

---

# 216. Cache Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] Local caches have maximum size/weight

[ ] Distributed entries have intentional TTL

[ ] Cache keys use namespaces

[ ] Cache keys exclude secrets

[ ] Redis operations have bounded timeout

[ ] Cache fallback has tests

[ ] Critical invalidation has tests

[ ] Cache serialization has integration tests

[ ] User-specific caches include security dimensions

[ ] Fallback usage has metrics

[ ] Redis outage has degradation test

[ ] Cache hit/miss metrics exist
```

---

# 217. Enterprise Cache Gate

A cache implementation is not considered compliant when applicable conditions include:

```text
[ ] Cache is treated as authoritative data without explicit design

[ ] Cache key omits a dimension affecting the result

[ ] User-specific data uses a global shared key

[ ] Access token or password appears in cache key

[ ] Distributed cache has no serialization compatibility strategy

[ ] Cache has no TTL and no reliable invalidation

[ ] Database rollback can leave uncommitted cache state

[ ] Cache invalidation occurs before database commit

[ ] Negative cache can hide new data indefinitely

[ ] Hot-key expiration can cause uncontrolled source fan-out

[ ] Redis timeout is unbounded

[ ] Redis retry is unbounded

[ ] Redis outage causes uncontrolled database overload

[ ] Local fallback is unbounded

[ ] Local fallback has no invalidation strategy

[ ] Local cache uses an unlimited ConcurrentHashMap

[ ] Cache warming from every pod overloads source

[ ] Production code uses broad FLUSHALL

[ ] PII appears in cache metrics

[ ] Fallback mode is invisible operationally
```

---

# 218. Anti-Patterns

The following are prohibited or strongly discouraged:

- caching without measured need
- treating Redis as the source of truth accidentally
- cache keys missing business dimensions
- PII/secrets in cache keys
- indefinite cache with no invalidation
- cache mutation before database commit
- caching arbitrary searches without invalidation analysis
- Java native serialization as default distributed format
- incompatible cache schema during rolling deployment
- negative cache with excessive TTL
- no cache-stampede protection for demonstrated hot expensive keys
- global cache-load locks
- stale security/authorization data without revocation strategy
- assuming Redis never fails
- long optional-cache timeout
- unbounded Redis retry
- unbounded Redis connection pool
- unbounded local map
- startup warming of entire datasets from every pod
- `FLUSHALL` as normal cache invalidation
- `KEYS *` against large production keyspaces
- logging every cache hit
- silent permanent local fallback

---

# 219. Positive Consequences

The decision provides:

- lower source-system load
- reduced latency
- predictable cache semantics
- safer Redis degradation
- bounded local memory
- stronger multi-pod consistency
- safer rolling deployments
- controlled invalidation
- reduced cache stampede risk
- better security
- measurable cache effectiveness
- improved resilience

---

# 220. Negative Consequences

The decision introduces:

- TTL/invalidation design
- Redis infrastructure
- serialization contracts
- fallback complexity
- cache-specific testing
- monitoring
- potential eventual consistency
- more operational capacity planning

These costs are accepted because uncontrolled caching frequently creates correctness and availability problems more difficult than the performance issue it was intended to solve.

---

# 221. Neutral Consequences

The decision also means:

- not every database lookup should be cached
- not every cache needs Redis
- not every cache needs local fallback
- not every cache needs distributed locking
- cache misses are expected
- Redis eviction is normal for cache workloads
- stale data can be acceptable for some reference data
- stale data can be unacceptable for authorization or financial decisions
- local fallback improves availability but weakens freshness
- a high hit ratio alone does not prove a cache is valuable

---

# 222. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Stale data | High | Medium | TTL + invalidation |
| Cross-user data leak | Critical | Low/Medium | Correct key design |
| Redis outage | High | Medium | Fallback + source protection |
| DB overload after Redis failure | Critical | Medium | Local fallback/coalescing |
| Cache stampede | High | Medium | Coalescing + jitter |
| Memory exhaustion | Critical | Medium | Bounded caches |
| Serialization incompatibility | High | Medium | Versioned schema/key |
| Lost invalidation | High | Medium | TTL safety net |
| Negative stale result | Medium/High | Medium | Short negative TTL |
| Silent degraded mode | High | Medium | Metrics + alerts |

---

# 223. Implementation Guidance

The following rules are mandatory:

1. Caching must have a demonstrated performance/resilience purpose.
2. Every cache must identify its authoritative source.
3. Cache-aside should be the default distributed caching pattern.
4. Cache entries must have explicit TTL/invalidation semantics.
5. TTL must reflect acceptable staleness rather than arbitrary convention.
6. TTL jitter should be considered for synchronized high-volume expiration.
7. Cache keys must be deterministic, namespaced and complete.
8. Security/user-specific dimensions must participate in keys when required.
9. Secrets and unnecessary PII must not appear in keys.
10. Distributed serialization must use an explicit compatibility strategy.
11. Incompatible representation changes should use versioned namespaces.
12. Database changes must commit before cache invalidation/update is published.
13. TTL should normally protect against lost invalidation.
14. Negative caching must use bounded, generally shorter TTLs.
15. Hot expensive keys should use stampede protection where demonstrated.
16. Distributed locks must not be introduced indiscriminately for cache loading.
17. Security-sensitive data must have explicit freshness/revocation semantics.
18. Redis must be treated as a remote dependency with bounded timeout.
19. Redis retries must remain bounded.
20. Optional Redis failure should degrade to the defined fallback/source path.
21. Redis outage fallback must protect the authoritative source from overload.
22. Local fallback must have bounded size and lifetime.
23. Local fallback must participate in eviction/clear semantics where applicable.
24. Local caches must use bounded cache implementations such as Caffeine.
25. Cache warming must not create startup load storms.
26. Redis dependency must be classified as optional, degradable or mandatory.
27. Administrative cache operations must be authorized and scoped.
28. Production cache cleanup must avoid broad blocking Redis operations.
29. Cache effectiveness and degraded fallback mode must be observable.
30. Cache architecture must have automated hit, miss, eviction, fallback, concurrency and security tests.
31. Real Redis semantics should be tested with Testcontainers where relevant.

---

# 224. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Cache
- Spring Data Redis
- Redis
- Caffeine
- PostgreSQL
- Resilience4j where applicable
- Micrometer
- Testcontainers Redis
- JUnit 5
- AssertJ
- deterministic concurrency tests
- failure injection
- load tests
- memory tests
- CI/CD quality gates
- Redis dashboards and alerts

---

# 225. Success Criteria

The decision is successful when:

- caching demonstrably reduces latency or source load
- cache keys cannot cross-contaminate user/business contexts
- stale-data limits are explicit
- cache invalidation follows committed source changes
- rolling deployments do not fail on incompatible cached representations
- hot-key expiration does not create uncontrolled source fan-out
- Redis outages degrade predictably
- source systems survive cache outages
- local fallback remains bounded
- operators can detect prolonged fallback mode
- local cache cannot grow without limit
- cache behavior is covered by automated tests
- cache performance is measurable rather than assumed

---

# 226. Alternatives Rejected

## 226.1 Cache Everything

Rejected because caching adds consistency, memory and invalidation costs and can make inexpensive reads slower.

---

## 226.2 Redis as Authoritative Database by Accident

Rejected because cache eviction/failure semantics differ from authoritative persistence.

---

## 226.3 No TTL

Rejected as the default because lost invalidation can leave stale entries indefinitely.

---

## 226.4 In-Memory Map as Distributed Cache

Rejected because each pod has independent, volatile and potentially unbounded state.

---

## 226.5 Fail Every Request When Redis Is Down

Rejected for optional caches because source/fallback access can preserve availability.

---

## 226.6 Always Fall Back to Database Without Protection

Rejected because Redis failure can instantly overload the source.

---

## 226.7 Global Lock on Cache Miss

Rejected because unrelated keys become unnecessarily serialized.

---

## 226.8 Flush Entire Redis on Every Change

Rejected because broad invalidation causes load spikes and may affect unrelated workloads.

---

# 227. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-077: Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard
- ADR-079: Enterprise Notification, Email, SMS, Push and External Communication Standard

---

# 228. References

- Redis Documentation
- Spring Data Redis Documentation
- Spring Cache Documentation
- Caffeine Documentation
- Resilience4j Documentation
- PostgreSQL Documentation
- Kubernetes Documentation
- AWS ElastiCache Documentation
- OWASP Application Security Guidance
- Testcontainers Documentation
- Google Site Reliability Engineering
- Designing Data-Intensive Applications

---

# 229. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise caching and resilient Redis baseline |

---

# 230. Decision Summary

The normal read path becomes:

```text
REQUEST
   |
   v
CACHE
   |
 +---+---+
 |       |
HIT     MISS
 |       |
 v       v
RETURN  SOURCE
          |
          v
       CACHE PUT
          |
          v
        RETURN
```

Write consistency becomes:

```text
UPDATE SOURCE
      |
      v
    COMMIT
      |
      v
INVALIDATE CACHE
```

rather than:

```text
UPDATE CACHE
      |
      v
UPDATE DATABASE
      |
      X
    FAILURE
```

Cache keys become:

```text
namespace
   +
version
   +
business identity
   +
all relevant dimensions
```

For example:

```text
consultant:v2:
{segment}:
{process}:
{customerId}
```

rather than:

```text
{customerId}
```

when the result depends on all three dimensions.

Redis degradation becomes:

```text
REQUEST
   |
   v
REDIS
   |
 +---+---+
 |       |
 OK    FAILURE
 |       |
 v       v
CACHE   FALLBACK POLICY
          |
       +--+--+
       |     |
     LOCAL SOURCE
       |     |
       +--+--+
          |
          v
        RESULT
```

Two-level caching becomes:

```text
REQUEST
   |
   v
L1 LOCAL
   |
 MISS
   |
   v
L2 REDIS
   |
 MISS
   |
   v
SOURCE
   |
   +--> L2
   |
   +--> L1
```

with explicit invalidation.

Stampede protection becomes:

```text
HOT KEY EXPIRES
      |
      v
1000 REQUESTS
      |
      v
COALESCE
      |
      v
ONE / BOUNDED
SOURCE LOAD
      |
      v
CACHE REFILL
      |
      v
ALL REQUESTS CONTINUE
```

instead of:

```text
1000 MISSES
    |
    v
1000 DATABASE CALLS
```

Expiration becomes:

```text
BASE TTL
   +
BOUNDED JITTER
```

to avoid synchronized expiry.

Negative caching becomes:

```text
REQUEST UNKNOWN ID
      |
      v
SOURCE = NOT FOUND
      |
      v
SHORT NEGATIVE CACHE
      |
      v
REPEATED REQUESTS
DO NOT HAMMER SOURCE
```

Local fallback becomes:

```text
REDIS HEALTHY
     |
     v
PRIMARY CACHE
     |
     +--> SYNCHRONIZE
          BOUNDED LOCAL FALLBACK

REDIS DOWN
     |
     v
LOCAL FALLBACK
     |
     v
DEGRADED MODE METRIC
```

but only where stale fallback is business-safe.

Rolling cache evolution becomes:

```text
OLD APPLICATION
      |
      v
customers:v1

NEW APPLICATION
      |
      v
customers:v2
```

when representations are incompatible.

The complete caching equation is:

```text
MEASURED NEED
      +
AUTHORITATIVE SOURCE
      +
CORRECT CACHE KEY
      +
BOUNDED TTL
      +
INVALIDATION
      +
SERIALIZATION COMPATIBILITY
      +
STAMPede PROTECTION
      +
BOUNDED MEMORY
      +
BOUNDED REDIS TIMEOUT
      +
SOURCE PROTECTION
      +
RESILIENT FALLBACK
      +
MULTI-POD CONSISTENCY
      +
SECURITY-AWARE KEYS
      +
OBSERVABILITY
      +
FAILURE TESTING
      =
SAFE ENTERPRISE CACHING
```

The governing principle is:

```text
Do not cache
because caching sounds fast.

Measure first.

Know the source of truth.

Know what stale means.

Know how stale is allowed
to become.

Give every cache entry
an intentional lifetime.

Build complete cache keys.

Include every dimension
that changes the result.

Never leak one user's data
through another user's key.

Do not put secrets in keys.

Version incompatible
cache representations.

Commit authoritative data
before invalidating cache.

Use TTL as a safety net.

Keep negative cache short.

Protect hot keys
from stampedes.

Do not globally lock
unrelated cache misses.

Treat Redis as remote.

Give Redis short,
bounded timeouts.

Bound retries.

Assume Redis can disappear.

Know whether the application
can survive without it.

Protect the database
when the cache disappears.

Use local fallback only
when stale data is safe.

Bound local fallback.

Invalidate every cache level.

Do not use an unlimited map
as a cache.

Do not warm everything
from every pod.

Do not FLUSHALL
as a normal strategy.

Measure hit rate.

Measure fallback.

Measure Redis latency.

Measure source load.

Alert when fallback becomes
the normal operating mode.

And remember:

a cache improves architecture
only when the system remains
correct when the cache

is empty,

is stale,

is slow,

contains an incompatible value,

evicts an entry early,

or disappears completely.
```
