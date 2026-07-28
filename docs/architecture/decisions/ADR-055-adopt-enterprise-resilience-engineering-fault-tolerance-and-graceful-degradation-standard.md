# ADR-055: Adopt Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-055 |
| Title | Adopt Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Resilience, Fault Tolerance, Resilience4j, HTTP, SQS, Redis |
| Related Work Items | Circuit Breaker, Retry, Timeout, Bulkhead, Idempotency, Graceful Degradation |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise distributed systems operate under partial failure.

A request may interact with:

```text
API Gateway

Application Service

PostgreSQL

Redis

SQS

External REST APIs

Identity Provider

Third-Party Services
```

Any of these dependencies can become:

```text
Slow

Unavailable

Overloaded

Partially Available

Intermittently Failing
```

A resilient system therefore cannot assume:

```text
Dependency Call
      |
      v
Always Succeeds
```

The architecture must assume:

```text
Dependency Call
      |
      +--> Success
      |
      +--> Timeout
      |
      +--> Rejection
      |
      +--> Connection Failure
      |
      +--> Invalid Response
      |
      +--> Partial Failure
      |
      +--> Dependency Overload
```

Resilience is consequently an architectural property rather than an exception-handling technique.

---

# 2. Problem Statement

The organization requires consistent standards covering:

- timeout
- retry
- Circuit Breaker
- Bulkhead
- Rate Limiting
- fallback
- graceful degradation
- idempotency
- backpressure
- dependency classification
- failure isolation
- cascading failures
- retry storms
- HTTP failure taxonomy
- Redis fallback
- SQS retry/redrive
- dead-letter handling
- recovery
- resilience testing
- chaos engineering
- Resilience4j
- observability of resilience mechanisms

---

# 3. Decision Drivers

Primary drivers are:

1. availability
2. controlled failure
3. business correctness
4. failure isolation
5. predictable recovery
6. overload protection
7. bounded resource consumption
8. reduced cascading failure
9. diagnosability
10. operational simplicity

---

# 4. Decision

All distributed dependencies MUST have an explicit resilience strategy proportional to their business criticality.

The canonical resilience model is:

```text
REQUEST
   |
   v
TIME BUDGET
   |
   v
CONCURRENCY CONTROL
   |
   v
DEPENDENCY CALL
   |
   +-----------------------------+
   |                             |
SUCCESS                       FAILURE
   |                             |
   v                             v
RETURN                    CLASSIFY FAILURE
                                 |
                     +-----------+-----------+
                     |           |           |
                     v           v           v
                 RETRYABLE   NON-RETRYABLE  OVERLOAD
                     |           |           |
                     v           v           v
                  RETRY       FAIL FAST    PROTECT
                     |
                     v
              CIRCUIT BREAKER
                     |
                     v
              FALLBACK?
                /       \
              YES        NO
               |          |
               v          v
         SAFE FALLBACK   ERROR
```

---

# 5. Fundamental Principle

The governing principle is:

```text
A dependency failure must not
automatically become a platform failure.
```

---

# 6. Resilience Is Not Infinite Retry

Resilience does not mean:

```text
Keep trying until it works.
```

It means:

```text
Detect failure

Bound resource consumption

Protect dependencies

Preserve correctness

Recover predictably
```

---

# 7. Failure Taxonomy

Failures MUST be classified before resilience behavior is selected.

---

# 8. Failure Categories

Typical categories include:

```text
Transient Failure

Permanent Failure

Business Rejection

Authentication Failure

Authorization Failure

Rate Limiting

Timeout

Connection Failure

Dependency Overload

Invalid Response

Internal Defect
```

---

# 9. Transient Failure

Examples:

```text
Temporary network interruption

HTTP 503

Connection reset

Temporary database failover
```

These MAY be retryable.

---

# 10. Permanent Failure

Examples:

```text
Invalid endpoint

Malformed request

Unsupported operation
```

Retry normally provides no value.

---

# 11. Business Rejection

Examples:

```text
Order already cancelled

Credit limit exceeded

Invalid workflow transition
```

Business rejection MUST NOT be treated as infrastructure failure.

---

# 12. Authentication Failure

HTTP:

```text
401
```

normally requires credential/token handling rather than generic retry.

---

# 13. Authorization Failure

HTTP:

```text
403
```

MUST NOT normally be retried.

---

# 14. Not Found

HTTP:

```text
404
```

MUST be interpreted according to the dependency contract.

It is not automatically an infrastructure failure.

---

# 15. Conflict

HTTP:

```text
409
```

often represents business or concurrency semantics.

It MUST NOT automatically be retried.

---

# 16. Rate Limiting

HTTP:

```text
429
```

requires controlled handling.

---

# 17. Server Errors

Selected:

```text
5xx
```

responses MAY be transient.

Retry eligibility depends on:

```text
Operation Idempotency

Failure Type

Retry Budget

Dependency Contract
```

---

# 18. Timeout

Every synchronous remote dependency MUST have bounded timeout behavior.

---

# 19. Infinite Wait

This is prohibited:

```text
CALL DEPENDENCY
      |
      v
WAIT FOREVER
```

---

# 20. Timeout Layers

Applicable timeout types include:

```text
Connection Timeout

Connection Acquisition Timeout

Read Timeout

Response Timeout

Write Timeout

Overall Operation Timeout
```

---

# 21. Timeout Budget

Timeouts MUST derive from the caller's end-to-end latency budget.

---

# 22. Example

If the complete operation has:

```text
1 second
```

available, a downstream call cannot safely consume:

```text
5 seconds
```

before failing.

---

# 23. Timeout Hierarchy

Prefer:

```text
OUTER TIME BUDGET
      >
INNER DEPENDENCY TIME BUDGET
```

---

# 24. Timeout Composition

For sequential calls:

```text
TOTAL LATENCY
    ≈
CALL A
+
CALL B
+
CALL C
+
APPLICATION PROCESSING
```

Timeouts MUST account for this composition.

---

# 25. Retry

Retry is permitted only for failures reasonably expected to succeed on a subsequent attempt.

---

# 26. Retry Eligibility

Before retrying, evaluate:

```text
Is the failure transient?

Is the operation idempotent?

Is there enough time budget?

Will retry increase dependency overload?

Does the dependency explicitly permit retry?
```

---

# 27. Retry Count

Retries MUST be bounded.

---

# 28. Infinite Retry

Infinite synchronous retry is prohibited.

---

# 29. Retry Backoff

Retries SHOULD use backoff where appropriate.

---

# 30. Exponential Backoff

A common model is:

```text
Attempt 1 -> wait 100 ms

Attempt 2 -> wait 200 ms

Attempt 3 -> wait 400 ms
```

Actual values depend on workload.

---

# 31. Jitter

Jitter SHOULD be considered to prevent synchronized retries.

---

# 32. Thundering Herd

Without jitter:

```text
10,000 clients fail
       |
       v
wait 1 second
       |
       v
10,000 clients retry simultaneously
```

can recreate overload.

---

# 33. Retry Storm

Retry amplification MUST be considered.

Example:

```text
1,000 incoming requests
        |
        v
3 attempts each
        |
        v
up to 3,000 dependency calls
```

---

# 34. Retry Budget

Critical integrations SHOULD define an explicit retry budget.

---

# 35. Retry Ownership

Only one appropriate architectural layer SHOULD normally own retry behavior.

---

# 36. Nested Retry

Avoid:

```text
API Gateway Retry
      +
Service Retry
      +
HTTP Client Retry
      +
SDK Retry
```

without understanding the multiplicative effect.

---

# 37. Retry Multiplication

If each of four layers makes three attempts:

```text
3 × 3 × 3 × 3 = 81
```

potential attempts may result from one logical request.

---

# 38. Idempotency

Retries of operations with side effects require idempotency analysis.

---

# 39. Idempotent Operation

Conceptually:

```text
execute(operation, key)

execute(operation, same key)

=
one business effect
```

---

# 40. Idempotency Key

Critical retriable write operations SHOULD use a stable idempotency key when the protocol permits it.

---

# 41. Random Retry Identity

A retry MUST NOT generate a new business identity when doing so defeats duplicate detection.

---

# 42. Idempotency Storage

Idempotency state MAY be maintained through:

```text
Database Unique Constraint

Dedicated Idempotency Table

Event Identifier

Business Key

Distributed Store
```

depending on the architecture.

---

# 43. Database Constraint

Where possible, database uniqueness SHOULD provide the final protection against duplicate persistent effects.

---

# 44. Exactly Once

Distributed systems SHOULD avoid casually claiming:

```text
exactly once
```

semantics.

---

# 45. Practical Model

Prefer explicit:

```text
At-Least-Once Delivery
        +
Idempotent Processing
```

where appropriate.

---

# 46. Circuit Breaker

Circuit Breakers SHOULD protect dependencies where repeated calls during failure would waste resources or amplify an outage.

---

# 47. Circuit Breaker States

Canonical states:

```text
CLOSED
   |
   | failures exceed threshold
   v
OPEN
   |
   | wait duration
   v
HALF_OPEN
   |
   +--> success --> CLOSED
   |
   +--> failure --> OPEN
```

---

# 48. CLOSED

Calls execute normally while metrics are collected.

---

# 49. OPEN

Calls fail fast without invoking the dependency.

---

# 50. HALF_OPEN

A limited number of probe calls determine whether the dependency has recovered.

---

# 51. Circuit Breaker Purpose

Circuit Breaker exists primarily to:

```text
Stop wasting resources

Reduce pressure on failing dependency

Allow recovery

Fail quickly
```

---

# 52. Circuit Breaker Is Not Retry

Circuit Breaker and Retry solve different problems.

---

# 53. Retry + Circuit Breaker

Their composition MUST be intentional.

---

# 54. Resilience4j

Resilience4j is the preferred resilience library for Java/Spring Boot services where applicable.

---

# 55. Approved Mechanisms

Relevant Resilience4j capabilities include:

```text
CircuitBreaker

Retry

Bulkhead

RateLimiter

TimeLimiter
```

---

# 56. Central Configuration

Resilience configuration SHOULD be externally configurable and centrally standardized where practical.

---

# 57. Dependency Classification

External dependencies MUST be classified according to business impact.

The standard classification is:

```text
CRITICAL

HIGH

MEDIUM
```

---

# 58. CRITICAL Dependency

Failure prevents completion of the primary business operation.

Examples may include:

```text
Cart

Products

Workflow
```

for order-processing paths where these systems are mandatory.

---

# 59. HIGH Dependency

Failure significantly affects business functionality but may permit controlled degradation in some scenarios.

Examples may include:

```text
Customers

Companies

Parameters

Suppliers

Users

Colors
```

depending on the workflow.

---

# 60. MEDIUM Dependency

Failure affects secondary functionality without necessarily blocking the core transaction.

Examples may include:

```text
Audit

Notifications
```

when asynchronous recovery is supported.

---

# 61. Classification Is Contextual

A dependency can have different criticality in different business operations.

---

# 62. Resilience Policy

Dependency classification SHOULD influence:

```text
Timeout

Circuit Breaker Threshold

Open Duration

Fallback Strategy

Alert Severity

Recovery Objective
```

---

# 63. Reference Circuit Breaker Baseline

A platform baseline MAY use:

```text
Sliding Window Size: 10

Minimum Calls: 5

Half-Open Calls: 3
```

with open-state duration varying by criticality.

---

# 64. Example Open Durations

A reference policy MAY use:

```text
CRITICAL -> 5 seconds

HIGH     -> 10 seconds

MEDIUM   -> 15 seconds
```

These values are starting points and MUST be validated against dependency behavior and workload.

---

# 65. Failure Rate Threshold

Failure thresholds MUST be explicit.

---

# 66. Slow Call

Circuit Breaker configuration SHOULD consider slow-call behavior when latency degradation is operationally equivalent to failure.

---

# 67. Slow Dependency

A dependency does not need to return HTTP 500 to cause an outage.

This:

```text
Dependency responds in 30 seconds
```

can be more damaging than fast failure.

---

# 68. Bulkhead

Bulkheads isolate resource consumption.

---

# 69. Bulkhead Principle

```text
Dependency A overloaded
        |
        v
resources allocated to A exhausted
        |
        X
must not consume resources
required by B and C
```

---

# 70. Bulkhead Types

Isolation MAY use:

```text
Concurrency Limits

Dedicated Executors

Connection Pools

Semaphores

Queues
```

---

# 71. Virtual Thread Bulkhead

Virtual Threads do not remove the need for bulkhead isolation.

---

# 72. Semaphore Bulkhead

A semaphore-style concurrency limit MAY be appropriate for Virtual Thread workloads.

---

# 73. Bulkhead Capacity

Bulkhead size MUST reflect downstream capacity.

---

# 74. Queue Capacity

Queues associated with isolated execution MUST be bounded.

---

# 75. Rejection

When capacity is exhausted, explicit rejection is often safer than uncontrolled queue growth.

---

# 76. Rate Limiting

Rate limiting protects finite capacity.

---

# 77. Rate Limit Dimensions

Limits MAY apply by:

```text
Client

User

Customer

Endpoint

Tenant

API Key

Global Service Capacity
```

---

# 78. Rate Limit Response

HTTP APIs SHOULD use appropriate protocol semantics such as:

```text
429 Too Many Requests
```

where applicable.

---

# 79. Retry-After

`Retry-After` SHOULD be honored where provided and appropriate.

---

# 80. Rate Limiting Is Not Authorization

Rate limiting and authorization solve different concerns.

---

# 81. Backpressure

Backpressure prevents producers from overwhelming consumers.

---

# 82. Synchronous Backpressure

Examples include:

```text
Concurrency Limit

Queue Limit

429 Response

503 Response

Load Shedding
```

---

# 83. Asynchronous Backpressure

Messaging systems require:

```text
Consumer Capacity

Queue/FIFO Group Strategy

Lag Monitoring

Bounded Internal Processing
```

---

# 84. Load Shedding

When capacity is exhausted, low-priority work MAY be rejected to preserve critical operations.

---

# 85. Priority

Priority-based degradation MUST be explicit and business-approved.

---

# 86. Graceful Degradation

Graceful degradation means preserving the maximum correct functionality possible during dependency failure.

---

# 87. Correct Degradation

Example:

```text
Notification service unavailable
        |
        v
Order transaction succeeds
        |
        v
Notification queued/retried later
```

if notification delivery is not part of the atomic business requirement.

---

# 88. Incorrect Degradation

This is not acceptable:

```text
Pricing service unavailable
        |
        v
Assume price = 0
        |
        v
Complete order
```

unless zero is explicitly correct according to the business contract.

---

# 89. Fallback

Fallback MUST preserve business semantics.

---

# 90. Fallback Questions

Before introducing fallback, answer:

```text
Is stale data acceptable?

How stale?

Can empty data be distinguished from failure?

Can a default value create incorrect business decisions?

How will recovery occur?
```

---

# 91. Default Value Fallback

Generic:

```java
return 0;
```

or:

```java
return List.of();
```

is prohibited when it hides an actual dependency failure.

---

# 92. Cache Fallback

Cached data MAY be used during dependency failure when stale-data semantics are explicitly acceptable.

---

# 93. Redis Primary Cache

Redis MAY act as the primary distributed cache.

---

# 94. Local Fallback Cache

An in-memory fallback MAY provide temporary resilience when Redis is unavailable.

---

# 95. Fallback Cache Semantics

The architecture MUST define:

```text
Maximum Staleness

Capacity

Eviction

Synchronization

Recovery

Consistency
```

---

# 96. Redis Failure

Redis failure MUST NOT automatically become application failure if the cache is non-critical and the underlying source remains available.

---

# 97. Cache Is Not Source of Truth

Unless explicitly designed otherwise:

```text
CACHE
 !=
SYSTEM OF RECORD
```

---

# 98. Cache Recovery

When Redis recovers, fallback behavior SHOULD converge safely toward the primary cache/source.

---

# 99. Cache Stampede

Resilience design SHOULD prevent dependency overload after widespread cache misses.

---

# 100. HTTP Failure Taxonomy

HTTP clients SHOULD consistently map remote failures.

---

# 101. Reference Mapping

Conceptually:

```text
2xx -> Success

400 -> Invalid request / contract failure

401 -> Authentication failure

403 -> Authorization failure

404 -> Contract-specific absence

409 -> Conflict/business concurrency

429 -> Rate limited

5xx -> Dependency failure candidate

Timeout -> Dependency timeout

Connection failure -> Dependency unavailable
```

---

# 102. Remote Exception

Remote exceptions SHOULD preserve:

```text
Dependency Name

HTTP Status

Safe Error Code

Correlation Context
```

where available.

---

# 103. Sensitive Remote Data

Remote error payloads MUST be sanitized before logging or external propagation when they may contain sensitive information.

---

# 104. Error Translation

Internal dependency details SHOULD NOT leak unnecessarily through public APIs.

---

# 105. Failure Boundary

Translate technical failures into appropriate application/API semantics at a deliberate boundary.

---

# 106. Cascading Failure

The architecture MUST actively prevent cascading failures.

---

# 107. Typical Cascade

```text
DEPENDENCY SLOWS
       |
       v
REQUESTS ACCUMULATE
       |
       v
THREADS / CONNECTIONS OCCUPIED
       |
       v
CALLER SLOWS
       |
       v
UPSTREAM RETRIES
       |
       v
MORE LOAD
       |
       v
PLATFORM FAILURE
```

---

# 108. Cascade Protection

Primary controls include:

```text
Timeout

Circuit Breaker

Bulkhead

Rate Limiting

Backpressure

Load Shedding

Bounded Retry
```

---

# 109. Failure Isolation

Failure domains SHOULD be as small as practical.

---

# 110. Shared Resource Risk

Unrelated dependencies SHOULD NOT unnecessarily share a single constrained resource pool when one can starve the others.

---

# 111. Database Failure

Database resilience MUST consider:

```text
Connection Acquisition

Transaction Timeout

Failover

Deadlock

Lock Timeout

Pool Saturation
```

---

# 112. Database Retry

Database operations MUST NOT be generically retried without considering transaction and idempotency semantics.

---

# 113. Deadlock Retry

Selected transaction failures MAY be retryable when the operation is safe and the policy is bounded.

---

# 114. Connection Pool

Connection acquisition MUST have a bounded timeout.

---

# 115. Database Overload

Application retries MUST NOT overwhelm a recovering database.

---

# 116. SQS Resilience

SQS resilience MUST assume at-least-once processing where applicable.

---

# 117. Consumer Failure

Consumer processing failure SHOULD follow an explicit policy.

---

# 118. SQS Failure Flow

Conceptually:

```text
EVENT
  |
  v
PROCESS
  |
  +--> SUCCESS --> COMMIT
  |
  +--> TRANSIENT FAILURE
  |       |
  |       v
  |     RETRY
  |
  +--> PERMANENT FAILURE
          |
          v
       DLQ / PARK
```

---

# 119. Retry / Redrive Policy

Retry topics MAY be used to delay repeated processing without blocking normal partition progress where appropriate.

---

# 120. Dead Letter Queue

A DLQ is a containment mechanism, not a disposal mechanism.

---

# 121. DLQ Ownership

Every DLQ MUST have:

```text
Owner

Monitoring

Retention

Investigation Procedure

Replay Procedure
```

---

# 122. Poison Message

A permanently invalid message MUST NOT block a consumer partition indefinitely.

---

# 123. SQS Retry/Redrive

SQS retry/redrive MUST be bounded.

---

# 124. SQS Backoff

Retry timing SHOULD avoid immediate hot-loop processing of failing events.

---

# 125. SQS Idempotency

Consumers SHOULD use stable event/business identifiers for duplicate protection.

---

# 126. Event Replay

Consumers MUST tolerate legitimate replay according to their delivery semantics.

---

# 127. Event Schema Failure

Schema incompatibility SHOULD be classified separately from transient infrastructure failure.

---

# 128. Outbox Resilience

Transactional Outbox dispatch MUST tolerate temporary broker failure.

---

# 129. Outbox Retry

Unsent events SHOULD remain available for bounded controlled retry.

---

# 130. Outbox Failure Metadata

Outbox implementations SHOULD retain:

```text
Attempts

Next Attempt

Last Error

Creation Time

Destination

Trace ID
```

where applicable.

---

# 131. Outbox Maximum Attempts

Maximum attempts MUST be explicit.

---

# 132. Exhausted Outbox

Events exhausting retry policy MUST become operationally visible.

---

# 133. Outbox Recovery

Recovery MUST support safe replay without duplicating business effects.

---

# 134. Notification Resilience

Secondary notification failures SHOULD normally be isolated from core transactions.

---

# 135. Audit Resilience

Audit behavior MUST follow the business/regulatory criticality of the audit record.

---

# 136. Mandatory Audit

If audit persistence is legally or operationally mandatory for the transaction, failure semantics MUST reflect that requirement.

---

# 137. Best-Effort Audit

Best-effort audit MUST be explicitly classified rather than assumed.

---

# 138. Health Checks

Health endpoints SHOULD distinguish:

```text
Liveness

Readiness
```

---

# 139. Liveness

Liveness answers:

```text
Should this process be restarted?
```

---

# 140. Readiness

Readiness answers:

```text
Should this instance receive traffic?
```

---

# 141. Dependency Health

A non-critical dependency failure SHOULD NOT automatically make the application non-live.

---

# 142. Readiness Dependency

Only dependencies required to safely serve traffic SHOULD normally affect readiness.

---

# 143. Health Cascade

Health checks MUST NOT create additional dependency overload.

---

# 144. Recovery

Resilience includes recovery, not merely failure containment.

---

# 145. Recovery Questions

Every critical dependency strategy SHOULD answer:

```text
How is failure detected?

How is load reduced?

How is recovery detected?

How is normal traffic restored?

How is backlog recovered?
```

---

# 146. Recovery Ramp

After an outage, traffic SHOULD return gradually where sudden recovery load could re-trigger failure.

---

# 147. Half-Open Probe

Circuit Breaker half-open state provides controlled recovery probing.

---

# 148. Backlog Recovery

Asynchronous systems MUST understand recovery capacity.

---

# 149. Recovery Capacity

After outage:

```text
Processing Capacity
        >
Current Arrival Rate
```

is required to drain backlog.

---

# 150. Recovery Time

Critical systems SHOULD define recovery objectives for accumulated work.

---

# 151. Observability

Every resilience mechanism MUST be observable.

---

# 152. Circuit Breaker Metrics

Monitor:

```text
State

Failure Rate

Slow Call Rate

Rejected Calls

Successful Calls

Failed Calls
```

---

# 153. Retry Metrics

Monitor:

```text
Retry Attempts

Retry Success

Retry Exhaustion

Retry Delay
```

---

# 154. Bulkhead Metrics

Monitor:

```text
Concurrent Calls

Available Capacity

Rejected Calls

Queue Depth
```

where applicable.

---

# 155. Rate Limiter Metrics

Monitor:

```text
Allowed Requests

Rejected Requests

Wait Time
```

---

# 156. Dependency Metrics

Each important dependency SHOULD expose:

```text
Latency

Success Rate

Error Rate

Timeout Rate

Circuit State
```

---

# 157. Dependency Name

Metrics SHOULD use stable dependency identifiers.

---

# 158. Cardinality

Metric labels MUST avoid unbounded cardinality.

---

# 159. Logging

Resilience logs SHOULD include:

```text
Dependency

Operation

Failure Classification

Elapsed Time

Correlation Identifier
```

where applicable.

---

# 160. elapsedMs

Dependency-call diagnostics SHOULD include elapsed duration where useful.

---

# 161. Log Noise

Every retry attempt MUST NOT automatically produce high-severity logs.

---

# 162. Severity

Logging severity SHOULD reflect operational meaning.

---

# 163. Alerting

Alerts SHOULD focus on user/business impact rather than isolated transient failures.

---

# 164. Alert Examples

Useful signals include:

```text
Circuit OPEN

High timeout rate

Sustained retry exhaustion

Bulkhead rejection

DLQ growth

Outbox backlog

SQS queue backlog/oldest-message age

Fallback activation
```

---

# 165. Fallback Metric

Fallback execution MUST be observable.

Otherwise a system can silently operate in degraded mode indefinitely.

---

# 166. Degraded Mode

Services SHOULD expose degraded-state information where operationally useful.

---

# 167. Resilience Testing

Resilience behavior MUST be tested.

---

# 168. Unit Tests

Unit tests SHOULD validate:

```text
Failure Classification

Fallback Decisions

Idempotency

Error Translation
```

---

# 169. Integration Tests

Integration tests SHOULD validate:

```text
Timeout

HTTP Errors

Circuit Breaker

Retry

Redis Failure

SQS Failure
```

where applicable.

---

# 170. Circuit Breaker Test

Tests SHOULD verify state transition without relying on long real-time delays.

---

# 171. Retry Test

Tests MUST verify:

```text
Eligible failures retry

Ineligible failures do not retry

Maximum attempts are respected
```

---

# 172. Fallback Test

Fallback tests MUST verify semantic correctness, not only that "something is returned."

---

# 173. Idempotency Test

Critical write workflows SHOULD test duplicate requests/events.

---

# 174. Concurrency Failure Test

Resilience tests SHOULD evaluate failure under concurrent load where relevant.

---

# 175. Recovery Test

Testing only failure without recovery is incomplete for critical resilience mechanisms.

---

# 176. Chaos Engineering

Chaos engineering MAY be used to validate resilience assumptions in controlled environments.

---

# 177. Chaos Principle

Chaos testing asks:

```text
Does the system actually behave
the way the architecture assumes
when dependencies fail?
```

---

# 178. Chaos Experiments

Examples include:

```text
Increase Dependency Latency

Terminate Instance

Disable Redis

Interrupt SQS Connectivity

Reject Database Connections

Inject HTTP 500

Inject HTTP 429

Cause Packet Loss
```

---

# 179. Chaos Safety

Chaos experiments MUST have:

```text
Defined Scope

Expected Outcome

Abort Condition

Owner

Observability

Recovery Plan
```

---

# 180. Production Chaos

Production chaos requires explicit organizational approval and mature operational controls.

---

# 181. Game Days

Resilience game days SHOULD be considered for critical services.

---

# 182. Game Day Scenarios

Examples:

```text
Database unavailable

SQS unavailable

Redis unavailable

Critical API slow

Authentication provider degraded

Region/zone failure
```

---

# 183. Expected Behavior

Every game-day scenario SHOULD define expected system behavior before execution.

---

# 184. Resilience and Performance

Resilience mechanisms affect performance.

---

# 185. Retry Cost

Retry increases:

```text
Latency

Traffic

CPU

Connection Usage
```

---

# 186. Circuit Breaker Benefit

Open Circuit Breakers can reduce resource consumption during outages.

---

# 187. Bulkhead Cost

Bulkheads intentionally reject excess work to protect overall service health.

---

# 188. Resilience and Consistency

Availability MUST NOT silently override business consistency.

---

# 189. CAP Trade-Off

Distributed failure scenarios MAY require explicit consistency/availability trade-offs.

---

# 190. Business Decision

Such trade-offs MUST be business and architecture decisions, not accidental implementation behavior.

---

# 191. Fallback Data

Fallback data MUST communicate stale/degraded semantics when consumers need that distinction.

---

# 192. Resilience and Security

Security failures MUST NOT be disguised as generic availability failures.

---

# 193. Authentication Retry

Credential refresh MAY be attempted according to authentication design.

Blind retry of invalid credentials is prohibited.

---

# 194. Secret Exposure

Resilience diagnostics MUST NOT expose tokens or credentials.

---

# 195. Rate-Limit Bypass

Fallback/retry logic MUST NOT bypass intended rate limits.

---

# 196. Resilience Configuration

Configuration MUST have safe defaults.

---

# 197. Environment Overrides

Environment-specific tuning MAY override approved defaults.

---

# 198. Configuration Validation

Invalid resilience configuration SHOULD fail fast where practical.

---

# 199. Configuration Example

Conceptually:

```yaml
resilience:
  dependencies:
    products:
      criticality: CRITICAL
      timeout: 2s
      circuit-breaker:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-calls-in-half-open-state: 3
```

Exact configuration structure depends on implementation.

---

# 200. Configuration Duplication

Resilience configuration SHOULD NOT be duplicated inconsistently across services without justification.

---

# 201. Central Baseline

Platform-level defaults SHOULD provide a baseline while allowing workload-specific overrides.

---

# 202. Resilience Review

A new external dependency MUST answer:

```text
What happens if it is unavailable?

What happens if it is slow?

Can the operation retry?

Is it idempotent?

Is fallback possible?

What is the timeout?

What is the concurrency limit?

What is its criticality?

How is failure observed?

How does recovery occur?
```

---

# 203. Failure Mode Analysis

Critical workflows SHOULD receive explicit failure-mode analysis.

---

# 204. Example Workflow

```text
CHECKOUT
   |
   +--> Customers
   |
   +--> Products
   |
   +--> Orders
   |
   +--> Workflow
```

Each edge requires an explicit failure policy.

---

# 205. Resilience Matrix

Critical workflows SHOULD maintain a matrix similar to:

| Dependency | Criticality | Timeout | Retry | Circuit Breaker | Fallback |
|---|---|---:|---|---|---|
| Products | CRITICAL | Defined | Selective | Yes | No unsafe default |
| Workflow | CRITICAL | Defined | Selective | Yes | Business-defined |
| Customers | HIGH | Defined | Selective | Yes | Context-specific |
| Notifications | MEDIUM | Defined | Async | Yes/Async | Deferred delivery |

Values MUST reflect actual business requirements.

---

# 206. Resilience Fitness Functions

Stable invariants SHOULD be automated where practical.

Examples:

```text
[ ] Every HTTP client has timeout configuration

[ ] Circuit Breaker names are registered

[ ] Retry count is bounded

[ ] Retryable status list is explicit

[ ] Connection pools are bounded

[ ] Executor queues are bounded

[ ] SQS retries are bounded

[ ] DLQ configuration exists where required

[ ] Fallback paths emit metrics

[ ] Critical integrations have resilience tests

[ ] No generic infinite retry exists
```

---

# 207. Architecture Test

Architecture tests MAY verify approved resilience wrappers/configuration for external clients.

---

# 208. Static Analysis

Static analysis SHOULD detect prohibited patterns where practical.

---

# 209. Operational Readiness

A service is not resilience-ready merely because Resilience4j is present as a dependency.

---

# 210. Enterprise Resilience Gate

A critical integration is not considered compliant when applicable conditions include:

```text
[ ] No timeout

[ ] Unbounded retry

[ ] Retry on non-idempotent operation without protection

[ ] No failure classification

[ ] No overload protection

[ ] No dependency criticality

[ ] Unsafe fallback

[ ] Silent fallback

[ ] Unbounded concurrency

[ ] Unbounded queue

[ ] Circuit Breaker missing where required

[ ] DLQ without ownership

[ ] SQS poison message can block processing indefinitely

[ ] No idempotency for at-least-once critical processing

[ ] No resilience metrics

[ ] Recovery behavior unknown
```

---

# 211. Resilience Review Checklist

A material integration change SHOULD evaluate:

```text
[ ] What is the dependency criticality?

[ ] What failures can occur?

[ ] What is the timeout?

[ ] Is retry allowed?

[ ] Is the operation idempotent?

[ ] What is the maximum attempt count?

[ ] Is backoff required?

[ ] Is jitter required?

[ ] Is Circuit Breaker required?

[ ] Is Bulkhead required?

[ ] What is the concurrency limit?

[ ] Is Rate Limiting relevant?

[ ] Can fallback preserve correctness?

[ ] How is degraded mode observed?

[ ] How does recovery occur?

[ ] Are duplicate operations safe?

[ ] Are resilience tests present?

[ ] Can this failure cascade?
```

---

# 212. Anti-Patterns

The following are prohibited or strongly discouraged:

- infinite timeout
- infinite retry
- retrying every exception
- retrying every HTTP status
- retrying business validation failures
- retrying authorization failures
- retrying non-idempotent writes without protection
- nested retry policies without amplification analysis
- retry without backoff where repeated immediate calls amplify failure
- Circuit Breaker used as a substitute for timeout
- Circuit Breaker without observability
- unbounded concurrency
- unbounded queues
- Virtual Threads interpreted as infinite capacity
- unsafe fallback values
- converting dependency failure to empty data indiscriminately
- silent fallback
- cache fallback without staleness semantics
- DLQ without ownership
- poison messages blocking FIFO MessageGroupIds indefinitely
- health checks causing dependency overload
- non-critical dependency failure making process liveness fail
- resilience configuration copied without workload validation
- chaos testing without abort/recovery controls
- claiming exactly-once semantics without proving the complete delivery model
- hiding authentication/security failures as availability problems

---

# 213. Positive Consequences

The decision provides:

- reduced cascading failures
- faster failure detection
- bounded resource consumption
- controlled retries
- safer dependency recovery
- explicit fallback semantics
- stronger idempotency
- improved SQS failure handling
- safer Redis degradation
- better operational visibility
- predictable dependency behavior
- stronger production availability

---

# 214. Negative Consequences

The decision introduces:

- additional configuration
- resilience-specific tests
- more operational metrics
- dependency classification work
- idempotency persistence where required
- recovery procedures
- chaos/game-day effort

These costs are accepted because uncontrolled distributed failure can cause substantially greater operational and business impact.

---

# 215. Neutral Consequences

The decision also means:

- not every failure is retried
- not every dependency requires fallback
- not every dependency requires identical Circuit Breaker settings
- some requests should fail fast
- some excess load should be rejected
- some stale data may be acceptable
- some business operations must remain unavailable when a critical dependency fails

---

# 216. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Retry amplification | Critical | Medium | Retry budget + ownership |
| Cascading failure | Critical | Medium | Timeout + CB + Bulkhead |
| Unsafe fallback | Critical | Medium | Business semantic review |
| Duplicate writes | High | Medium | Idempotency |
| Circuit flapping | Medium | Medium | Threshold tuning |
| Dependency overload | High | Medium | Rate limit + concurrency bounds |
| SQS poison message | High | Medium | Bounded retry + DLQ |
| Silent degraded mode | High | Medium | Metrics + alerts |
| Cache inconsistency | High | Medium | Explicit stale-data semantics |
| Recovery overload | High | Medium | Controlled ramp |
| Configuration drift | Medium | Medium | Central baseline |

---

# 217. Implementation Guidance

The following rules are mandatory:

1. Every synchronous remote dependency must have bounded timeouts.
2. Failures must be classified before retry behavior is selected.
3. Retry must be bounded.
4. Business validation and authorization failures must not be generically retried.
5. Retry of side-effecting operations requires idempotency analysis.
6. Retry ownership must avoid multiplicative retry amplification.
7. Backoff and jitter should be used where repeated clients can synchronize.
8. Critical dependencies should use Circuit Breaker where repeated calls during failure would amplify impact.
9. Circuit Breakers must be observable.
10. Dependency criticality must be explicit.
11. CRITICAL/HIGH/MEDIUM policies should guide resilience configuration.
12. Concurrency must remain bounded.
13. Bulkhead isolation should protect finite downstream capacity where required.
14. Queues must remain bounded.
15. Rate limiting should protect finite capacity where applicable.
16. Fallback must preserve business correctness.
17. Dependency failures must not be indiscriminately converted into zero/empty values.
18. Cache fallback requires explicit staleness and consistency semantics.
19. HTTP errors must be consistently classified.
20. Sensitive remote error information must be sanitized.
21. SQS retry/redrive must be bounded.
22. Poison messages must not block processing indefinitely.
23. DLQs must have ownership, monitoring and replay procedures.
24. At-least-once critical consumers require idempotent processing.
25. Outbox dispatch must tolerate temporary broker failure.
26. Health checks must distinguish liveness from readiness.
27. Non-critical dependency failure must not automatically fail liveness.
28. Every resilience mechanism must expose useful operational metrics.
29. Fallback activation must be observable.
30. Resilience tests must cover failure and recovery.
31. Critical workflows should receive controlled failure-injection testing.
32. Major resilience assumptions should be periodically validated through game days or equivalent exercises.
33. Recovery capacity must be understood for asynchronous backlogs.
34. Resilience configuration must use safe defaults and explicit overrides.
35. New external integrations must document their failure strategy before production release.

---

# 218. Validation

This ADR will be validated through:

- Resilience4j
- JUnit 5
- AssertJ
- Mockito
- WireMock
- MockWebServer
- Testcontainers
- SQS integration tests
- Redis integration tests
- concurrency tests
- architecture tests
- load tests
- resilience dashboards
- alerts
- controlled chaos experiments
- operational game days

---

# 219. Success Criteria

The decision is successful when:

- cascading failures decrease
- remote calls fail within bounded time
- retry storms are prevented
- Circuit Breaker states are operationally visible
- critical dependency failures are isolated
- unsafe fallbacks are eliminated
- duplicate side effects decrease
- SQS poison messages no longer block normal processing
- DLQ backlog is actively managed
- Redis outages degrade predictably
- asynchronous backlogs recover within defined objectives
- dependency outages are easier to diagnose
- services recover without immediately recreating overload

---

# 220. Alternatives Rejected

## 220.1 Retry Every Failure

Rejected because retry can amplify dependency outages.

---

## 220.2 Fallback Everything

Rejected because fallback can silently corrupt business behavior.

---

## 220.3 Circuit Breaker Only

Rejected because Circuit Breaker does not replace timeout, idempotency, backpressure or isolation.

---

## 220.4 Maximum Availability at Any Cost

Rejected because availability must not silently violate business correctness.

---

## 220.5 Unlimited Virtual Thread Concurrency

Rejected because downstream resources remain finite.

---

## 220.6 Manual Operational Recovery Only

Rejected because predictable failure and recovery behavior should be designed and tested.

---

# 221. Related Decisions

This ADR extends and implements:

- ADR-009: Amazon SQS Integration Events
- ADR-010: Redis Distributed Caching
- ADR-016: Application Resilience
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Observability and Production Diagnostics Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering, Capacity Testing and JVM Optimization Standard

---

# 222. References

- Resilience4j Documentation
- Spring Boot Documentation
- Java 21 Documentation
- Amazon SQS Documentation
- Redis Documentation
- PostgreSQL Documentation
- Release It!
- Site Reliability Engineering
- Building Secure and Reliable Systems
- Designing Data-Intensive Applications
- AWS Builders Library — Timeouts, Retries and Backoff
- Microsoft Cloud Design Patterns
- Circuit Breaker Pattern
- Bulkhead Pattern
- Retry Pattern

---

# 223. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise resilience and graceful degradation baseline |

---

# 224. Decision Summary

The resilience model becomes:

```text
CALL
 |
 v
TIMEOUT
 |
 v
FAILURE?
 /    \
NO    YES
|      |
v      v
OK   CLASSIFY
       |
       +--> BUSINESS ERROR ------> FAIL
       |
       +--> AUTH ERROR ----------> FAIL
       |
       +--> TRANSIENT -----------> RETRY?
       |
       +--> OVERLOAD ------------> PROTECT
```

Retry:

```text
TRANSIENT FAILURE
       |
       v
IDEMPOTENT?
   /       \
 NO         YES
 |           |
 v           v
FAIL      BUDGET?
            / \
          NO  YES
          |    |
          v    v
        FAIL  BACKOFF
                |
                v
              RETRY
```

Circuit Breaker:

```text
CLOSED
   |
   | failure threshold
   v
OPEN
   |
   | recovery delay
   v
HALF_OPEN
   |
   +---- success ----> CLOSED
   |
   +---- failure ----> OPEN
```

Bulkhead:

```text
                 SERVICE
                    |
          +---------+---------+
          |         |         |
          v         v         v
       PRODUCTS  CUSTOMERS  NOTIFY
          |         |         |
       LIMIT A   LIMIT B   LIMIT C
```

Failure in one dependency cannot consume every resource available to the service.

Fallback:

```text
DEPENDENCY FAILURE
        |
        v
IS THERE A BUSINESS-CORRECT FALLBACK?
       / \
     YES  NO
      |    |
      v    v
FALLBACK  FAIL
      |
      v
MARK DEGRADED
```

SQS:

```text
EVENT
  |
  v
PROCESS
  |
  +--> SUCCESS
  |
  +--> TRANSIENT
  |      |
  |      v
  |   BOUNDED RETRY
  |
  +--> PERMANENT
         |
         v
        DLQ
         |
         v
   INVESTIGATE / REPLAY
```

Redis:

```text
REQUEST
   |
   v
REDIS
  / \
 OK  FAIL
 |     |
 v     v
DATA  FALLBACK ALLOWED?
        / \
      YES  NO
       |    |
       v    v
 LOCAL /   SOURCE /
 STALE     ERROR
 CACHE
```

with explicit staleness semantics.

Cascading-failure protection:

```text
DEPENDENCY SLOW
       |
       v
TIMEOUT
       |
       v
CIRCUIT BREAKER
       |
       v
BULKHEAD
       |
       v
LOAD SHEDDING
       |
       v
SERVICE SURVIVES
```

Recovery:

```text
DEPENDENCY RECOVERS
        |
        v
CONTROLLED PROBES
        |
        v
HALF_OPEN
        |
        v
STABLE?
   /         \
 NO           YES
 |             |
 v             v
OPEN       RESTORE
           TRAFFIC
```

The complete resilience equation is:

```text
BOUNDED TIMEOUTS
       +
FAILURE CLASSIFICATION
       +
SELECTIVE RETRY
       +
IDEMPOTENCY
       +
CIRCUIT BREAKERS
       +
BULKHEADS
       +
RATE LIMITING
       +
BACKPRESSURE
       +
SAFE FALLBACK
       +
OBSERVABILITY
       +
RECOVERY TESTING
       =
CONTROLLED DISTRIBUTED FAILURE
```

The governing principle is:

```text
Distributed systems will fail.

The architectural question is not
whether a dependency will eventually
become unavailable or slow.

The question is what the rest of
the platform does when that happens.

Do not wait forever.

Do not retry forever.

Do not retry everything.

Do not hide failure behind
incorrect default values.

Do not allow one failing dependency
to consume every available resource.

Bound the failure.

Protect the business transaction.

Make degraded behavior visible.

And design recovery with the same
care used to design failure handling.
```
