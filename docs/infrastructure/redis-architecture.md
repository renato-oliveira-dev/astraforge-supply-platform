# Redis Architecture

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Redis Architecture |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines how Redis is used by the AstraForge Supply Platform.

It establishes:

- distributed cache
- cache invalidation
- cache-aside pattern
- TTL policies
- resilience
- monitoring
- scalability
- operational guidelines

Redis is an optimization layer.

PostgreSQL remains the system of record.

---

# 2. Goals

Redis is used to improve:

- response time
- throughput
- scalability
- database load reduction
- horizontal scaling

Redis must never become the primary persistence mechanism.

---

# 3. High-Level Architecture

```
Client

↓

REST API

↓

Application Service

↓

Cache Lookup

↓

Redis

↓

Cache Hit?

↓

YES

↓

Return Data

↓

NO

↓

Repository

↓

PostgreSQL

↓

Populate Cache

↓

Return Response
```

---

# 4. Cache Pattern

The platform adopts:

```
Cache Aside
```

Flow

```
Read

↓

Redis

↓

Miss

↓

Database

↓

Populate Cache
```

Writes always update the database first.

---

# 5. Source of Truth

Only PostgreSQL owns business data.

Redis stores temporary copies.

Loss of Redis must never result in data loss.

---

# 6. Cached Data

Recommended cache candidates

- Product Catalog
- Customer Profile
- Configuration
- Parameters
- Exchange Rates
- Workflow Configuration
- Read Projections

Avoid caching highly volatile transactional data.

---

# 7. Cache Keys

Naming convention

```
<context>:<resource>:<identifier>
```

Examples

```
orders:summary:8f7c...

customer:profile:12345

product:details:ABC123

workflow:approval:MOTORCYCLE
```

---

# 8. TTL Strategy

Different data requires different expiration.

| Data | Suggested TTL |
|-------|---------------|
| Product Catalog | 1 hour |
| Customer Profile | 15 minutes |
| Parameters | 6 hours |
| Configuration | 24 hours |
| Read Projections | 5 minutes |

TTL values should remain configurable.

---

# 9. Cache Invalidation

Preferred strategy

```
Database Update

↓

Commit

↓

Evict Cache

↓

Next Read

↓

Reload
```

Never update cache before committing the transaction.

---

# 10. Write Strategy

Write flow

```
Application Service

↓

Repository

↓

Commit

↓

Cache Eviction
```

This prevents stale data caused by transaction rollback.

---

# 11. Read Strategy

```
GET

↓

Redis

↓

Hit?

↓

Return

↓

Miss

↓

Database

↓

Cache

↓

Return
```

---

# 12. Serialization

Preferred

```
JSON
```

Alternative

- MessagePack
- CBOR

Serialized objects should be version tolerant.

---

# 13. Distributed Locks

Redis may provide distributed locks only when required.

Typical use cases

- scheduled jobs
- leader election
- singleton execution

Business transactions should not depend on Redis locks.

---

# 14. Rate Limiting

Redis may support API throttling.

Example

```
User

↓

Token Bucket

↓

Allowed?

↓

YES

↓

Continue
```

---

# 15. Temporary Tokens

Suitable for

- password reset tokens
- email verification
- session metadata

Always define expiration.

---

# 16. Idempotency Support

Redis may store short-lived idempotency keys for REST APIs.

Persistent idempotency for Integration Events remains in PostgreSQL.

---

# 17. Memory Management

Use eviction policies appropriate for cached data.

Recommended

```
allkeys-lru
```

or

```
volatile-lru
```

Selection depends on workload characteristics.

---

# 18. High Availability

Production deployment should use:

- Redis Sentinel

or

- Redis Cluster

Single-node Redis is acceptable only for local development.

---

# 19. Monitoring

Expose metrics

- hit ratio
- miss ratio
- memory usage
- key count
- eviction count
- command latency
- connection count

---

# 20. Logging

Log

- connection failures
- cache warm-up
- cache invalidation
- serialization failures

Avoid excessive cache-hit logging.

---

# 21. Resilience

If Redis becomes unavailable:

```
Application

↓

Database

↓

Continue
```

Redis failures must degrade performance, not functionality.

---

# 22. Security

Enable

- authentication
- TLS (when applicable)
- network isolation
- least privilege

Never expose Redis directly to the public internet.

---

# 23. Backup

Redis persistence is optional depending on deployment.

Business recovery relies on PostgreSQL.

Redis can be rebuilt from the primary database.

---

# 24. Testing

Verify

- cache hit
- cache miss
- eviction
- expiration
- serialization
- Redis outage
- fallback behavior

---

# 25. Architecture Rules

Redis:

- is optional at runtime
- never owns business data
- improves read performance
- supports horizontal scalability
- must fail gracefully

---

# 26. Decision Summary

The platform adopts:

- Cache-Aside Pattern
- PostgreSQL as source of truth
- configurable TTL
- distributed cache
- graceful degradation
- cache invalidation after commit
- Redis Cluster/Sentinel for production

---

# 27. Next Documentation Step

Next document

```
docs/infrastructure/observability.md
```

It will define:

- logging strategy
- metrics
- distributed tracing
- health checks
- OpenTelemetry
- Prometheus
- Grafana
- alerting
- SLOs
- operational dashboards
