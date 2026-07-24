# ADR-010: Use Redis for Distributed Caching

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-010 |
| Title | Use Redis for Distributed Caching |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Caching Architecture |
| Related Work Items | Platform performance and scalability |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform executes thousands of read operations against relatively stable datasets.

Examples include:

- product catalog
- customer configuration
- payment methods
- workflow definitions
- application parameters
- permissions
- regional configuration
- tax configuration
- reference tables

Most of these datasets change infrequently but are read continuously.

Querying PostgreSQL for every request unnecessarily increases:

- latency
- database CPU
- connection usage
- I/O
- infrastructure cost

The platform requires a distributed cache that works across multiple application instances.

---

# 2. Problem Statement

The platform requires a cache that:

- supports distributed deployments
- survives multiple application replicas
- offers sub-millisecond access
- integrates with Spring Boot
- scales horizontally
- supports expiration policies
- supports cache invalidation
- is cloud friendly
- supports Kubernetes
- provides operational metrics

---

# 3. Decision Drivers

Primary decision drivers include:

1. low latency
2. distributed architecture
3. operational maturity
4. Spring integration
5. Kubernetes compatibility
6. scalability
7. observability
8. simplicity
9. availability
10. maintainability

---

# 4. Considered Options

## 4.1 Local Memory Cache

Examples:

- ConcurrentHashMap
- Caffeine
- Guava Cache

Advantages:

- extremely fast
- no external infrastructure
- low latency

Disadvantages:

- cache is isolated per instance
- invalidation is difficult
- duplicated memory
- unsuitable for Kubernetes replicas

---

## 4.2 Redis

Advantages:

- distributed
- mature
- extremely fast
- TTL support
- Spring Cache integration
- Pub/Sub
- persistence options
- cloud managed services
- operational tooling

Disadvantages:

- additional infrastructure
- memory consumption
- cache consistency management

---

## 4.3 Memcached

Advantages:

- simple
- fast

Disadvantages:

- fewer capabilities
- weaker ecosystem
- no persistence
- limited advanced data structures

---

# 5. Decision

The Enterprise Order Platform adopts Redis as its distributed cache.

Redis will be used for:

- reference data
- configuration
- authorization metadata
- expensive query results
- lookup tables
- computed projections
- rate limiting (future)
- distributed locks (future evaluation)

Redis will **not** become the system of record.

PostgreSQL remains authoritative.

---

# 6. Rationale

Redis provides:

- extremely low latency
- mature ecosystem
- native Spring Boot support
- Kubernetes compatibility
- operational simplicity
- excellent cloud support

It significantly reduces database load while maintaining acceptable consistency.

---

# 7. Architectural Role

Redis is an Infrastructure component.

```text
Client

↓

Application

↓

Cache Lookup

↓

Redis

↓

Cache Miss

↓

PostgreSQL

↓

Redis Update

↓

Response
```

---

# 8. Cache Responsibilities

Redis stores:

- read models
- immutable reference data
- semi-static configuration
- computed values

Redis does **not** own business truth.

---

# 9. Cache Pattern

The platform adopts **Cache Aside**.

Flow:

```text
Read Request

↓

Redis Lookup

↓

Hit → Return

↓

Miss

↓

Database

↓

Populate Cache

↓

Return
```

---

# 10. Write Strategy

Updates follow:

```text
Database Update

↓

Commit

↓

Invalidate Cache
```

Database writes always occur before cache invalidation.

---

# 11. Cache Consistency

The cache is **eventually consistent**.

Temporary stale reads are acceptable for cached data.

Critical transactional reads must use PostgreSQL directly.

---

# 12. What Should Be Cached

Suitable candidates include:

- lookup tables
- workflow definitions
- tax configuration
- payment methods
- countries
- regions
- static parameters

---

# 13. What Must Not Be Cached

Do not cache:

- active transactions
- mutable aggregates
- pending approvals
- security tokens
- database sessions
- optimistic lock versions

---

# 14. Cache Keys

Keys should be deterministic.

Example:

```text
customer:12345

product:ABC123

workflow:motorcycle

parameter:tax-rate
```

Avoid random key formats.

---

# 15. Namespaces

Recommended namespaces:

```text
customer:

product:

workflow:

config:

parameter:

pricing:
```

---

# 16. TTL Strategy

Examples:

| Data | TTL |
|---|---|
| Workflow | 24h |
| Parameters | 12h |
| Products | 30 min |
| Customer metadata | 15 min |

TTL must reflect business volatility.

---

# 17. Cache Invalidation

Invalidation occurs after successful transaction commit.

Methods include:

- explicit delete
- namespace invalidation
- event-driven invalidation

---

# 18. Event-Driven Invalidation

Future versions may invalidate cache through Kafka events.

Example:

```text
CustomerUpdated

↓

Customer Cache Invalidated
```

---

# 19. Serialization

JSON is the default serialization format.

Requirements:

- deterministic
- version tolerant
- human readable

---

# 20. Compression

Compression may be enabled for large cached objects after performance analysis.

---

# 21. Security

Redis must use:

- TLS
- authentication
- restricted network access
- least privilege

Public Redis exposure is prohibited.

---

# 22. Monitoring

Monitor:

- hit ratio
- miss ratio
- latency
- memory usage
- evictions
- expired keys
- command failures
- connections

---

# 23. Metrics

Recommended metrics:

```text
cache.hit

cache.miss

cache.eviction

cache.latency

cache.memory

cache.connections
```

---

# 24. Failure Strategy

Redis is an optimization.

If Redis is unavailable:

```text
Application

↓

Database

↓

Continue
```

Business functionality must continue.

---

# 25. Resilience

Redis failures must not:

- reject business transactions
- corrupt data
- prevent writes

Fallback is PostgreSQL.

---

# 26. Circuit Breaker

Redis access may be protected by Circuit Breakers.

Repeated failures should temporarily bypass Redis.

---

# 27. Warm-up

Optional cache warm-up may execute during startup.

Warm-up must never block application availability indefinitely.

---

# 28. Scaling

Redis may scale through:

- replication
- clustering
- managed cloud services

Scaling strategy depends on workload.

---

# 29. High Availability

Production deployments should use:

- replication
- automatic failover
- managed services where possible

---

# 30. Kubernetes

Redis may run:

- as a managed cloud service
- StatefulSet
- Redis Operator

Managed services are preferred.

---

# 31. Testing

Integration tests should verify:

- cache hit
- cache miss
- invalidation
- TTL expiration
- serialization
- fallback behavior

---

# 32. Anti-Patterns

The following are prohibited:

- storing business truth in Redis
- caching mutable aggregates
- infinite TTL without justification
- ignoring invalidation
- using Redis as a relational database
- exposing Redis publicly

---

# 33. Positive Consequences

The decision provides:

- lower latency
- reduced database load
- improved scalability
- better throughput
- operational simplicity
- distributed cache consistency

---

# 34. Negative Consequences

The decision introduces:

- additional infrastructure
- cache invalidation complexity
- eventual consistency
- operational monitoring

These costs are acceptable.

---

# 35. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Stale cache | TTL + invalidation |
| Redis outage | Database fallback |
| Memory exhaustion | Eviction policies |
| Serialization incompatibility | Versioned models |
| Hot keys | Proper key design |
| Cache stampede | Cache Aside + locking when necessary |

---

# 36. Implementation Guidance

Mandatory rules:

1. PostgreSQL is the source of truth.
2. Redis is an optimization layer.
3. Cache Aside is the default strategy.
4. Cache invalidation occurs after commit.
5. TTL is mandatory.
6. Keys must be deterministic.
7. Redis failures must not stop business processing.
8. Sensitive data must not be cached.
9. Metrics must be collected.
10. Redis remains an Infrastructure concern.

---

# 37. Validation

Validation includes:

- hit-rate analysis
- latency measurements
- load testing
- fallback testing
- failover testing
- cache invalidation tests

---

# 38. Success Criteria

The decision is successful when:

- database load decreases significantly
- cache hit ratio remains high
- Redis failures do not affect business availability
- cache invalidation remains reliable
- latency targets are achieved

---

# 39. Related Decisions

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-009: Use Apache Kafka for Integration Events
- ADR-015: Deploy Workloads on Kubernetes

---

# 40. References

- Redis Documentation
- Spring Data Redis Documentation
- Spring Cache Documentation
- Redis Cluster Specification
- Enterprise Order Platform Redis Architecture

---

# 41. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved |

---

# 42. Decision Summary

The Enterprise Order Platform adopts **Redis** as its distributed cache.

Redis provides:

- sub-millisecond access
- distributed caching
- reduced database load
- horizontal scalability
- seamless Spring Boot integration

The platform follows the **Cache Aside** pattern, treats Redis strictly as an optimization layer, and preserves PostgreSQL as the single source of truth.
