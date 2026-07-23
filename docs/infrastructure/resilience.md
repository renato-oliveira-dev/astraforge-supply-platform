# Resilience

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Resilience |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the resilience strategy adopted by the Enterprise Order Platform.

It establishes:

- Circuit Breaker
- Retry
- Timeout
- Bulkhead
- Fallback
- Graceful Degradation
- Failure Isolation
- Rate Limiting
- Chaos Testing

The goal is to prevent cascading failures while maintaining service availability whenever possible.

---

# 2. Resilience Principles

The platform follows these principles:

- Fail Fast
- Isolate Failures
- Recover Automatically
- Degrade Gracefully
- Protect Shared Resources
- Prefer Availability over Complete Failure
- Never Retry Non-Idempotent Operations Blindly

---

# 3. Failure Domains

Failures are isolated per dependency.

Examples

```
Orders

↓

Inventory

↓

Payment

↓

Notification

↓

Customer
```

A failure in one dependency must not propagate to the others.

---

# 4. Circuit Breaker

Every remote integration must be protected by a Circuit Breaker.

Typical candidates

- REST APIs
- Kafka producers
- Redis
- SMTP
- External payment gateways

---

# 5. Circuit Breaker States

```
Closed

↓

Open

↓

Half Open

↓

Closed
```

Transitions are automatic based on configured thresholds.

---

# 6. Circuit Breaker Configuration

Recommended defaults

| Property | Value |
|----------|-------|
| Failure Rate Threshold | 50% |
| Sliding Window | 20 calls |
| Minimum Calls | 10 |
| Wait Duration in Open State | 30 s |
| Permitted Calls in Half Open | 5 |

Tune these values per dependency.

---

# 7. Timeouts

Every remote call must define explicit timeouts.

Recommended values

| Dependency | Timeout |
|------------|---------|
| Internal REST | 2 s |
| External REST | 5 s |
| Kafka Publish | 3 s |
| Redis | 500 ms |
| PostgreSQL Query | 2 s |

No request should wait indefinitely.

---

# 8. Retry Strategy

Retries are appropriate only for transient failures.

Typical retryable conditions

- network interruption
- temporary unavailability
- HTTP 429
- HTTP 503
- connection reset

Do not retry permanent business errors.

---

# 9. Exponential Backoff

Retry intervals should grow progressively.

Example

```
Attempt 1

↓

500 ms

↓

Attempt 2

↓

1 s

↓

Attempt 3

↓

2 s

↓

Attempt 4

↓

4 s
```

Jitter should be applied to reduce synchronized retries.

---

# 10. Retry Limits

Maximum retries should be limited.

Recommended

```
3 attempts
```

Unlimited retries are prohibited.

---

# 11. Bulkhead Pattern

Critical resources must be isolated.

Separate execution pools should exist for:

- database operations
- Kafka consumers
- external APIs
- scheduled jobs

Resource exhaustion in one area must not affect the others.

---

# 12. Thread Isolation

Example

```
HTTP Requests

↓

Web Thread Pool

Kafka Consumers

↓

Messaging Thread Pool

Scheduled Jobs

↓

Scheduler Pool
```

Each workload operates independently.

---

# 13. Fallback Strategy

Fallbacks should return meaningful degraded responses.

Examples

- cached configuration
- default parameters
- temporarily unavailable message

Fallbacks must never hide data corruption or business failures.

---

# 14. Graceful Degradation

When a non-critical dependency fails, the platform should continue operating with reduced functionality.

Examples

- notification failure does not cancel an order
- recommendation service unavailable does not block checkout
- reporting service failure does not affect transactional flows

---

# 15. Failure Isolation

Dependencies must fail independently.

Example

```
Orders

↓

Inventory (Unavailable)

↓

Payment

↓

Notification
```

Only the inventory flow is affected.

---

# 16. Rate Limiting

Protect public APIs against excessive traffic.

Strategies

- Token Bucket
- Fixed Window
- Sliding Window

Rate limits should be configurable.

---

# 17. Load Shedding

When capacity is exhausted:

- reject excess requests quickly
- preserve service for existing workloads
- avoid resource starvation

Prefer HTTP 429 over degraded system-wide performance.

---

# 18. Idempotency

Retries require idempotent operations.

REST APIs

- Idempotency-Key

Integration Events

- Processed Event Store

This prevents duplicate side effects.

---

# 19. Messaging Resilience

Kafka consumers must support:

- retry topics
- dead-letter queues
- replay
- idempotent processing

Poison messages must not block consumption.

---

# 20. Database Resilience

Protect PostgreSQL using:

- connection pools
- query timeouts
- optimistic locking
- transaction boundaries

Long-running transactions should be avoided.

---

# 21. Cache Resilience

If Redis becomes unavailable:

```
Application

↓

PostgreSQL

↓

Continue Processing
```

Redis failures should impact latency, not correctness.

---

# 22. Monitoring

Track

- circuit breaker state
- retry count
- timeout count
- fallback executions
- bulkhead saturation
- rate limit rejections

---

# 23. Chaos Engineering

Validate resilience through controlled failures.

Examples

- terminate service instances
- introduce network latency
- simulate database outage
- block Kafka brokers
- exhaust Redis connections

These experiments should be executed in non-production environments.

---

# 24. Testing

Verify

- circuit breaker transitions
- retry behavior
- timeout handling
- fallback responses
- bulkhead isolation
- graceful degradation
- idempotent retries

---

# 25. Architecture Rules

The platform:

- fails fast
- isolates failures
- retries only transient faults
- applies bounded retries
- degrades gracefully
- protects critical resources

---

# 26. Decision Summary

The platform adopts:

- Circuit Breaker
- Retry with Exponential Backoff
- Explicit Timeouts
- Bulkhead Isolation
- Graceful Degradation
- Rate Limiting
- Failure Isolation
- Chaos Testing
- Idempotent Retries

---

# 27. Next Documentation Step

Next document

```
docs/infrastructure/security-architecture.md
```

It will define:

- Authentication
- Authorization
- OAuth2
- JWT
- Keycloak integration
- Secrets management
- Encryption
- API security
- Zero Trust
- OWASP recommendations
