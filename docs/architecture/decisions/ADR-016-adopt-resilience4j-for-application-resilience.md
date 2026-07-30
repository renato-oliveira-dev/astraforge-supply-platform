# ADR-016: Adopt Resilience4j for Application Resilience

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-016 |
| Title | Adopt Resilience4j for Application Resilience |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Resilience and Fault Tolerance |
| Related Work Items | Platform Reliability |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform communicates with multiple internal and external dependencies.

Examples include:

- Customer Service
- Inventory Service
- Product Service
- Workflow Service
- Notification Service
- Payment Gateway
- Identity Provider
- SQS
- Redis
- External APIs

Every distributed dependency introduces potential failure modes including:

- increased latency
- intermittent failures
- network partitions
- service overload
- resource exhaustion
- cascading failures
- slow responses
- dependency unavailability

A resilient platform must tolerate these failures while preserving system stability.

---

# 2. Problem Statement

The platform requires a resilience framework that:

- prevents cascading failures
- isolates dependency failures
- supports retries
- limits concurrency
- enforces execution timeouts
- protects downstream services
- integrates with Spring Boot
- exposes operational metrics
- supports OpenTelemetry
- remains lightweight
- is cloud-native
- supports synchronous and asynchronous execution
- avoids vendor lock-in

---

# 3. Decision Drivers

Primary decision drivers include:

1. fault tolerance
2. service availability
3. operational visibility
4. lightweight implementation
5. Spring Boot integration
6. cloud-native compatibility
7. observability
8. scalability
9. maintainability
10. predictable failure behavior

---

# 4. Considered Options

## 4.1 Manual Resilience

Advantages:

- no additional libraries
- maximum flexibility

Disadvantages:

- duplicated code
- inconsistent behavior
- difficult maintenance
- weak observability

---

## 4.2 Hystrix

Advantages:

- historically mature
- complete feature set

Disadvantages:

- maintenance mode
- deprecated ecosystem
- no longer recommended

---

## 4.3 Service Mesh Only

Advantages:

- infrastructure-managed resilience
- language independent

Disadvantages:

- business-level retries remain impossible
- application has limited visibility
- no application-aware fallbacks

---

## 4.4 Resilience4j

Advantages:

- lightweight
- modular
- Spring Boot integration
- Micrometer/OpenTelemetry support
- actively maintained
- cloud-native
- functional programming support

Disadvantages:

- requires governance
- incorrect configuration may reduce availability

---

# 5. Decision

The AstraForge Supply Platform adopts **Resilience4j** as the standard resilience framework.

The following modules are approved:

- CircuitBreaker
- Retry
- TimeLimiter
- Bulkhead
- RateLimiter
- Cache (where appropriate)

---

# 6. Architectural Principles

Resilience policies must:

- be explicit
- be configuration-driven
- remain observable
- never hide failures
- avoid infinite retries
- avoid retry storms
- preserve business correctness

---

# 7. Failure Taxonomy

Failures are classified as:

- transient
- permanent
- business
- infrastructure
- timeout
- overload

Each category requires different handling.

---

# 8. Circuit Breaker

Circuit Breakers protect failing dependencies.

States include:

```text
Closed

↓

Open

↓

Half Open
```

---

# 9. Closed State

Requests execute normally.

Failures are monitored.

---

# 10. Open State

Requests fail immediately.

No downstream call is attempted.

---

# 11. Half Open State

A limited number of requests evaluate dependency recovery.

Successful calls close the breaker.

Repeated failures reopen it.

---

# 12. Circuit Breaker Scope

Every remote dependency owns an independent Circuit Breaker.

Examples:

- customers-service
- products-service
- payment-service
- notification-service

Circuit Breakers must never be shared across unrelated dependencies.

---

# 13. Retry

Retries are appropriate only for transient failures.

Examples:

- connection timeout
- temporary network interruption
- HTTP 503
- HTTP 504

---

# 14. Retry Policy

Retries must define:

- maximum attempts
- retry interval
- exponential backoff
- jitter
- retryable exceptions
- retryable status codes

---

# 15. Exponential Backoff

Retries should increase delay progressively.

Example:

```text
250 ms

500 ms

1 s

2 s
```

Jitter is recommended to reduce synchronized retries.

---

# 16. Non-Retryable Errors

Retries are prohibited for:

- validation failures
- authorization failures
- authentication failures
- business rule violations
- duplicate requests
- malformed payloads

---

# 17. TimeLimiter

TimeLimiter bounds execution time.

Long-running requests should fail predictably.

Timeouts must reflect business expectations.

---

# 18. Bulkhead

Bulkheads isolate resource consumption.

Separate thread pools or semaphore limits should protect:

- external APIs
- payment integrations
- notification providers

---

# 19. RateLimiter

Rate limiting prevents excessive outbound traffic.

Use cases include:

- third-party APIs
- licensing restrictions
- provider quotas

---

# 20. Fallback

Fallbacks are optional.

A fallback must:

- preserve correctness
- never fabricate business data
- never silently ignore failures

Examples:

- cached configuration
- default feature flags
- degraded informational responses

---

# 21. Fallback Restrictions

Fallbacks must not:

- invent customer data
- approve orders
- fabricate payment confirmation
- bypass authorization

Business integrity has priority over availability.

---

# 22. Composition

Recommended execution order:

```text
RateLimiter

↓

Bulkhead

↓

TimeLimiter

↓

Retry

↓

Circuit Breaker
```

The exact composition depends on the dependency characteristics.

---

# 23. Idempotency

Retries require idempotent operations.

Commands that modify state must guarantee safe repetition.

---

# 24. HTTP Integrations

HTTP clients should use:

- Circuit Breaker
- Retry
- TimeLimiter

Bulkhead is recommended for expensive dependencies.

---

# 25. SQS Consumers

SQS retries must distinguish:

- infrastructure failures
- poison messages
- business rejection

Business rejection should not trigger endless retries.

---

# 26. Transactional Outbox

Outbox publication retries are acceptable.

Duplicate publication must remain impossible through idempotent processing.

---

# 27. Observability

Every resilience component exposes metrics.

Examples:

- breaker state
- retries
- timeout count
- rejected calls
- rate limit events

---

# 28. Logging

Log transitions including:

- breaker opened
- breaker closed
- timeout
- retry exhaustion
- bulkhead rejection

Avoid excessive retry logging.

---

# 29. Metrics

Recommended metrics include:

```text
circuitbreaker.calls

circuitbreaker.state

retry.calls

retry.failed

bulkhead.rejected

ratelimiter.wait

timelimiter.timeout
```

---

# 30. OpenTelemetry

Resilience events should correlate with traces.

Relevant span attributes may include:

- retry attempt
- breaker state
- timeout
- fallback activation

---

# 31. Configuration

Policies remain externalized.

Different environments may define different thresholds.

Code should not hardcode operational limits.

---

# 32. Chaos Engineering

Resilience configuration should be validated through controlled failure injection.

Examples:

- latency
- dependency outage
- packet loss
- DNS failure
- SQS unavailability

---

# 33. Testing

Tests should validate:

- breaker transitions
- retry exhaustion
- timeout behavior
- fallback activation
- bulkhead rejection
- rate limiting
- configuration loading

---

# 34. Integration Testing

Integration tests should validate:

- real HTTP failures
- retry timing
- timeout propagation
- circuit recovery
- concurrent execution

---

# 35. Anti-Patterns

The following are prohibited:

- infinite retries
- retrying business validation failures
- retry storms
- shared circuit breakers
- hidden fallbacks
- swallowing exceptions
- blocking indefinitely
- unbounded concurrency
- ignoring timeout configuration

---

# 36. Positive Consequences

The decision provides:

- improved resilience
- controlled degradation
- failure isolation
- operational visibility
- better dependency protection

---

# 37. Negative Consequences

The decision introduces:

- configuration complexity
- additional operational tuning
- monitoring requirements

These costs are acceptable.

---

# 38. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Retry storm | Exponential backoff with jitter |
| Misconfigured breaker | Operational dashboards |
| Excessive fallback | Architecture review |
| Thread exhaustion | Bulkhead limits |
| Timeout too short | Performance testing |

---

# 39. Implementation Guidance

Mandatory rules:

1. Every remote dependency uses an independent Circuit Breaker.
2. Retries apply only to transient failures.
3. Timeouts are mandatory for remote calls.
4. Bulkheads isolate expensive dependencies.
5. Rate limiting protects quota-constrained services.
6. Fallbacks never compromise business correctness.
7. Retry requires idempotency.
8. Policies remain externalized.
9. Metrics and logs are mandatory.
10. Chaos testing validates resilience.

---

# 40. Validation

Validation includes:

- resilience unit tests
- integration tests
- chaos experiments
- performance testing
- observability review
- operational dashboards

---

# 41. Success Criteria

The decision is successful when:

- dependency failures remain isolated
- cascading failures are minimized
- retries improve transient recovery
- timeouts prevent resource exhaustion
- circuit breakers recover automatically
- resilience metrics support operational diagnosis

---

# 42. Related Decisions

- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes

---

# 43. References

- Resilience4j Documentation
- Release It! – Michael T. Nygard
- Enterprise Integration Patterns
- Google SRE Workbook
- AstraForge Supply Platform Resilience Guide

---

# 44. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved |

---

# 45. Decision Summary

The AstraForge Supply Platform adopts **Resilience4j** as its standard resilience framework.

The platform standardizes on:

- Circuit Breaker
- Retry
- TimeLimiter
- Bulkhead
- RateLimiter
- Configuration-driven policies
- OpenTelemetry integration

Resilience policies are designed to:

- isolate failures
- prevent cascading outages
- protect dependencies
- preserve business correctness
- provide full operational visibility

This decision establishes a consistent resilience architecture that improves availability, fault tolerance and operational reliability across all platform services.
