# ADR-034: Adopt Java 21 Concurrency and Parallelism Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-034 |
| Title | Adopt Java 21 Concurrency and Parallelism Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Java 21, Virtual Threads, Concurrency, Parallelism, Executors, Context Propagation |
| Related Work Items | Virtual Threads, Fan-Out, Bulkhead, WebClient, HikariCP, Kafka, Resilience |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform runs workloads that frequently combine:

- HTTP requests
- database access
- external REST APIs
- Redis
- Kafka
- file processing
- batch jobs
- independent validations
- independent enrichment operations
- report generation

Java 21 provides Virtual Threads as a production-ready concurrency primitive.

Virtual Threads materially reduce the cost of blocking thread-per-task execution.

However:

```text
Cheap Threads
    !=
Unlimited Capacity
```

The platform still has finite:

```text
CPU

Memory

Database Connections

HTTP Connections

Redis Capacity

Kafka Partitions

External API Capacity

File Descriptors

Network Bandwidth
```

Concurrency must therefore remain explicitly bounded by the capacity of constrained resources.

---

# 2. Problem Statement

The platform requires standards defining:

- concurrency
- parallelism
- Virtual Threads
- platform threads
- ExecutorService
- CompletableFuture
- fan-out/fan-in
- bounded concurrency
- semaphores
- bulkheads
- timeout
- deadline
- cancellation
- interruption
- context propagation
- SecurityContext
- RequestContext
- MDC
- HTTP client capacity
- HikariCP capacity
- Redis concurrency
- Kafka concurrency
- batch parallelism
- CPU-bound workloads
- blocking workloads
- synchronization
- shared mutable state
- testing
- observability
- graceful shutdown

---

# 3. Decision Drivers

Primary drivers are:

1. predictable performance
2. high throughput
3. resource protection
4. simple Java code
5. Java 21 adoption
6. resilience
7. context correctness
8. bounded resource usage
9. maintainability
10. observability
11. safe cancellation
12. horizontal scalability

---

# 4. Decision

The Enterprise Order Platform adopts Java 21 Virtual Threads as the preferred thread-per-task execution model for suitable blocking I/O workloads.

The architectural model is:

```text
REQUESTS
    |
    v
VIRTUAL THREADS
    |
    v
BOUNDED ACCESS
TO FINITE RESOURCES
    |
    +--> HTTP
    +--> DATABASE
    +--> REDIS
    +--> FILE SYSTEM
    +--> EXTERNAL SERVICES
```

Virtual Threads improve how waiting is represented.

They do not increase downstream capacity.

---

# 5. Fundamental Rule

The primary concurrency rule is:

```text
Concurrency must be bounded
at the constrained resource.
```

---

# 6. Concurrency vs Parallelism

Concurrency means multiple tasks can make progress during overlapping periods.

Parallelism means multiple tasks execute simultaneously.

---

# 7. Example

A service may have:

```text
1,000 Virtual Threads
```

waiting on network I/O while only:

```text
8 CPU cores
```

execute Java code simultaneously.

---

# 8. Throughput

Throughput depends on the complete system.

Conceptually:

```text
Throughput
    =
min(
    Application Capacity,
    DB Capacity,
    HTTP Capacity,
    Redis Capacity,
    External Capacity,
    CPU Capacity
)
```

---

# 9. Virtual Threads

Virtual Threads are appropriate primarily for:

- blocking HTTP operations
- blocking JDBC
- file I/O
- blocking SDK calls
- independent I/O-bound operations

---

# 10. Virtual Thread Creation

The preferred model is:

```java
Executors.newVirtualThreadPerTaskExecutor()
```

when explicit task execution is required.

---

# 11. Do Not Pool Virtual Threads

Virtual Threads must not be pooled merely to limit thread count.

The thread itself is not normally the scarce resource.

---

# 12. Wrong Model

Avoid:

```text
Pool of 20 Virtual Threads
```

solely because traditional platform-thread pools historically required small sizes.

---

# 13. Correct Limiting Point

If only 20 concurrent calls are allowed to an external system:

```text
Virtual Threads
      |
      v
Semaphore / Bulkhead = 20
      |
      v
External API
```

Limit the external resource usage rather than artificially pooling Virtual Threads.

---

# 14. Platform Threads

Platform threads remain appropriate for:

- framework-managed infrastructure
- event loops
- low-level networking
- CPU-bound worker pools
- specialized libraries

---

# 15. CPU-Bound Work

Virtual Threads do not make CPU-intensive algorithms faster.

---

# 16. CPU Parallelism

CPU-bound work should generally use bounded parallelism close to available CPU capacity.

---

# 17. CPU Pool

For explicit CPU-bound work:

```text
Bounded Platform Thread Pool

≈

Available Processors
```

is a reasonable starting point subject to measurement.

---

# 18. Mixing Workloads

Do not place heavy CPU-bound tasks into an unlimited Virtual Thread execution path without capacity control.

---

# 19. Fan-Out

Parallel fan-out may be used when operations are genuinely independent.

Example:

```text
                REQUEST
                   |
        +----------+----------+
        |          |          |
        v          v          v
    Customer    Products    User
        |          |          |
        +----------+----------+
                   |
                   v
                  JOIN
```

---

# 20. Sequential Cost

If independent calls take:

```text
Customer = 300 ms
Products = 400 ms
User     = 250 ms
```

sequential execution approximates:

```text
950 ms
```

plus overhead.

---

# 21. Concurrent Cost

Concurrent execution can approach:

```text
max(300, 400, 250)
=
400 ms
```

plus orchestration overhead.

---

# 22. Fan-Out Eligibility

Operations may run concurrently when:

- independent
- thread-safe
- context-safe
- failure semantics are known
- downstream capacity permits it

---

# 23. Dependency

Do not parallelize dependent operations.

Example:

```text
Load Customer

↓

Use Customer.companyId

↓

Load Company
```

requires dependency ordering unless the data model permits another strategy.

---

# 24. Fan-Out Amplification

One inbound request may produce many downstream calls.

Example:

```text
100 concurrent requests

×

5 downstream calls

=

500 downstream operations
```

---

# 25. Amplification Rule

Fan-out concurrency must be analyzed at system load, not only at single-request latency.

---

# 26. Bounded Fan-Out

High fan-out requires bounded concurrency.

---

# 27. Bulkhead

A concurrency bulkhead protects a finite dependency.

Conceptually:

```text
Application
    |
    v
Concurrency Limit = 30
    |
    v
External API
```

---

# 28. Semaphore

A semaphore may be used for simple local concurrency limiting.

---

# 29. Resilience4j Bulkhead

Resilience4j bulkheads may be used where standardized resilience configuration is preferred.

---

# 30. Bulkhead Ownership

Concurrency limits should normally correspond to a specific constrained dependency or workload.

---

# 31. Global Semaphore

A single arbitrary global semaphore for unrelated dependencies is discouraged.

---

# 32. Per-Dependency Limit

Prefer:

```text
Products API   = 30

Customers API  = 20

Users API      = 15
```

when capacities differ.

---

# 33. Limit Selection

Concurrency limits must be derived from:

- downstream capacity
- latency
- connection pool
- rate limits
- SLO
- load tests

rather than arbitrary values.

---

# 34. Little's Law

Capacity planning may use:

```text
L = λW
```

where:

```text
L = concurrent work
λ = throughput
W = average time in system
```

---

# 35. Example

If a dependency processes:

```text
100 requests/second
```

with average latency:

```text
200 ms
```

expected in-flight concurrency is approximately:

```text
100 × 0.2 = 20
```

before considering safety margins and tail latency.

---

# 36. HTTP Connection Pool

HTTP concurrency is constrained by connection resources.

---

# 37. WebClient

Reactor Netty WebClient may remain the approved HTTP client where already standardized.

---

# 38. Virtual Threads + WebClient

Using Virtual Threads in application orchestration does not make Reactor Netty connection limits disappear.

---

# 39. Example

```text
500 Virtual Threads

↓

WebClient maxConnections = 50

↓

At most approximately 50 active connections
subject to protocol/pool semantics
```

Additional work waits for capacity.

---

# 40. Pending Acquire

Pending HTTP connection acquisition must be bounded.

---

# 41. Unbounded Pending Queue

An unbounded connection-acquisition queue is prohibited.

---

# 42. HTTP Capacity

HTTP configuration should explicitly define:

- max connections
- pending acquire limit
- connection timeout
- response timeout
- idle timeout
- connection lifetime

---

# 43. HTTP Concurrency Alignment

Application-level concurrency should be aligned with HTTP connection-pool capacity.

---

# 44. Double Queueing

Avoid:

```text
Application Bulkhead Queue

+

HTTP Pending Acquire Queue

+

Downstream Queue
```

all being large.

---

# 45. Queueing

Large layered queues increase latency and obscure overload.

---

# 46. Fail Fast

When capacity is exhausted, bounded failure is often preferable to indefinite waiting.

---

# 47. JDBC

JDBC remains blocking I/O.

Virtual Threads are well suited to waiting for JDBC operations.

---

# 48. HikariCP

Database concurrency remains constrained by HikariCP.

---

# 49. Example

```text
1,000 Virtual Threads

↓

Hikari maximumPoolSize = 20

↓

Approximately 20 concurrent DB connections
```

The remaining requests wait for a connection.

---

# 50. Connection Acquisition Timeout

Hikari connection acquisition must have a finite timeout.

---

# 51. DB Queue

Large numbers of Virtual Threads waiting for DB connections can still create:

- latency
- memory usage
- timeout storms
- downstream overload after capacity becomes available

---

# 52. DB Concurrency

Database concurrency must therefore be explicitly protected.

---

# 53. Transaction Duration

Concurrency optimization must not create long-lived transactions.

---

# 54. External Calls Inside Transaction

Avoid:

```text
BEGIN TRANSACTION

↓

Call External API

↓

Wait

↓

COMMIT
```

unless unavoidable and explicitly justified.

---

# 55. Why

External I/O inside DB transactions unnecessarily holds:

- connection
- locks
- transaction state

---

# 56. Transaction Scope

Database transactions should remain as short as business correctness permits.

---

# 57. Parallel JDBC Calls

Parallel DB queries require multiple DB connections.

---

# 58. One Transaction

Do not assume multiple concurrent tasks can safely share one JPA transaction/entity manager.

---

# 59. EntityManager

JPA `EntityManager` is not a general-purpose thread-safe shared object.

---

# 60. Transaction Context

Transaction context must not be casually propagated into arbitrary concurrent tasks.

---

# 61. Parallel Repository Calls

Parallel repository calls require explicit consideration of:

- transaction boundaries
- connection usage
- consistency requirements

---

# 62. Redis

Redis operations also have finite network/server capacity.

---

# 63. Cache Stampede

Concurrent cache misses must follow ADR-032 stampede controls.

---

# 64. Kafka

Kafka concurrency differs from HTTP request concurrency.

---

# 65. Kafka Partition

Within a consumer group, effective parallel consumption of a topic is fundamentally bounded by available partitions.

---

# 66. Example

```text
Topic Partitions = 6

Consumer Instances × Concurrency = 20
```

does not create 20 useful partition consumers for that topic/group.

---

# 67. Kafka Ordering

Parallel processing must preserve required ordering semantics.

---

# 68. Per-Key Ordering

If business correctness requires ordered events for an aggregate, partitioning/key strategy must support that requirement.

---

# 69. Kafka Listener Concurrency

Listener concurrency must be aligned with:

- partition count
- processing latency
- downstream capacity
- ordering requirements

---

# 70. Async Kafka Processing

Offloading Kafka records to arbitrary asynchronous executors can break:

- offset semantics
- ordering
- retry behavior
- error handling

and requires explicit design.

---

# 71. Batch Processing

Batch jobs may use concurrency for independent records or partitions.

---

# 72. Batch Concurrency

Batch parallelism must be bounded according to:

- DB pool
- API limits
- file I/O
- CPU
- memory

---

# 73. Batch Amplification

A batch processing:

```text
100,000 records
```

must not create:

```text
100,000 simultaneous tasks
```

without bounded admission.

---

# 74. Chunking

Large workloads should use:

- chunking
- paging
- bounded queues
- partitioning

where appropriate.

---

# 75. ExecutorService

Executor ownership must be explicit.

---

# 76. Dedicated Executor

Dedicated executors may be appropriate for workloads requiring:

- isolation
- lifecycle control
- context propagation
- dedicated observability

---

# 77. Shared Executor

Do not place unrelated critical workloads onto a shared executor without understanding contention.

---

# 78. Executor Naming

Named executor beans should describe their workload.

Examples:

```text
orderApprovalVirtualThreadExecutor

reportVirtualThreadExecutor
```

---

# 79. Executor Lifecycle

Application-owned executors must be shut down cleanly.

---

# 80. Try-With-Resources

Where locally scoped:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // tasks
}
```

may provide safe lifecycle management.

---

# 81. Per-Request Executor

Creating a new executor for every HTTP request is generally unnecessary when a managed executor can provide the required semantics.

---

# 82. CompletableFuture

`CompletableFuture` may be used for explicit fan-out/fan-in orchestration.

---

# 83. Executor Explicitness

Do not rely accidentally on the common ForkJoinPool for blocking business I/O.

---

# 84. Wrong Example

Avoid:

```java
CompletableFuture.supplyAsync(this::callRemoteApi);
```

without understanding which executor executes the task.

---

# 85. Preferred

Use an explicitly managed executor:

```java
CompletableFuture.supplyAsync(
        this::callRemoteApi,
        integrationExecutor
);
```

---

# 86. Common Pool

The common ForkJoinPool is shared process-wide and should not become an accidental blocking-I/O executor.

---

# 87. CompletableFuture Failure

Every asynchronous pipeline requires explicit failure semantics.

---

# 88. Join

`join()` may wrap failures in `CompletionException`.

Error handling must preserve useful root-cause semantics.

---

# 89. Get

`get()` introduces checked `ExecutionException` and interruption semantics.

The chosen API must be handled deliberately.

---

# 90. Exception Swallowing

Async exceptions must not disappear silently.

---

# 91. Timeout

Every remote concurrent operation requires bounded execution time.

---

# 92. Individual Timeout

Each dependency may have an individual timeout.

---

# 93. Overall Deadline

The parent operation also requires an overall deadline.

---

# 94. Example

```text
Overall Request Budget = 3s

Customers = 1s

Products = 1.5s

Users = 800ms
```

Individual timeouts must fit within the overall budget.

---

# 95. Deadline

A deadline answers:

```text
How much time remains for this entire operation?
```

---

# 96. Timeout Composition

Independent timeout values must not accidentally exceed the caller's remaining deadline.

---

# 97. Timeout Is Not Cancellation

A caller timing out does not automatically guarantee all underlying work has stopped.

---

# 98. Cancellation

Cancellation behavior must be designed explicitly.

---

# 99. Orphan Work

Avoid continuing expensive downstream work after the result can no longer be used.

---

# 100. Interruption

Blocking code must respect thread interruption where the underlying API supports it.

---

# 101. InterruptedException

Do not swallow `InterruptedException`.

---

# 102. Interrupt Restoration

When not propagating `InterruptedException`, restore the interrupt status where appropriate:

```java
Thread.currentThread().interrupt();
```

---

# 103. Cancellation Safety

Cancellation must not leave partially mutated business state without defined transactional semantics.

---

# 104. Partial Success

Fan-out must define whether partial success is acceptable.

---

# 105. Fail-Fast

Some operations should fail as soon as one mandatory dependency fails.

---

# 106. Collect-All

Other operations may need to collect all validation failures.

---

# 107. Optional Dependency

Optional enrichments may fail without failing the entire operation when explicitly designed.

---

# 108. Failure Matrix

For each concurrent dependency, define:

```text
MANDATORY

OPTIONAL

FALLBACK AVAILABLE

RETRYABLE

CANCELLABLE
```

---

# 109. Context Propagation

Concurrency must preserve required request context.

---

# 110. SecurityContext

Spring Security `SecurityContext` must be propagated when asynchronous tasks require caller identity.

---

# 111. DelegatingSecurityContext

Spring Security context-aware executor wrappers may be used where appropriate.

---

# 112. Security Context Leakage

Context from one request must never leak into another task/request.

---

# 113. RequestContext

Request-scoped metadata used by integrations may require explicit propagation.

---

# 114. MDC

MDC/logging context does not automatically become correct merely because Virtual Threads are used.

---

# 115. Correlation ID

Correlation identifiers must remain available in concurrent tasks when required for logs/tracing.

---

# 116. Context Snapshot

Context should be captured at task submission and restored for task execution when required.

---

# 117. Context Cleanup

Propagated thread-local context must be cleaned after execution.

---

# 118. ThreadLocal

ThreadLocal-heavy designs require review when adopting Virtual Threads.

---

# 119. InheritableThreadLocal

Do not rely casually on `InheritableThreadLocal` for application correctness.

---

# 120. Scoped Values

Java Scoped Values may become useful for immutable context propagation, but adoption requires a separate compatibility/design review when depending on preview/incubator status in the selected JDK.

---

# 121. Preview Features

Production code must not depend on Java preview features without explicit architectural approval.

---

# 122. Structured Concurrency

Structured concurrency provides useful task-lifecycle semantics but its Java 21 API status must be considered before production standardization.

---

# 123. Production Baseline

This ADR does not mandate Java 21 preview structured-concurrency APIs.

---

# 124. Structured Principles

The architecture nevertheless adopts structured-concurrency principles:

```text
Child tasks belong to a parent operation.

Parent waits for relevant children.

Failure policy is explicit.

Cancellation is bounded.

Tasks do not escape uncontrolled.
```

---

# 125. Task Leakage

Fire-and-forget work from request handlers is prohibited unless explicitly modeled as durable asynchronous processing.

---

# 126. Durable Async Work

Business work that must survive request/process failure should normally use:

- Kafka
- Outbox
- job infrastructure

rather than an in-memory executor.

---

# 127. Executor Is Not Queue

An `ExecutorService` is not a durable message broker.

---

# 128. Restart

In-memory submitted tasks may disappear when the pod terminates.

---

# 129. Shared Mutable State

Concurrent code should minimize shared mutable state.

---

# 130. Immutability

Immutable DTOs, records and value objects are preferred for data shared across tasks.

---

# 131. Defensive Copies

Mutable collections crossing concurrency boundaries should use defensive copies where required.

---

# 132. Thread Safety

Singleton Spring beans may be invoked concurrently.

---

# 133. Stateful Singleton

Mutable request-specific state must not be stored in singleton service fields.

---

# 134. Wrong Example

Avoid:

```java
@Service
class OrderService {

    private UUID currentOrderId;
}
```

for request-specific state.

---

# 135. Local State

Request/task state should remain local to the invocation whenever possible.

---

# 136. Collections

Concurrent collections may be used when genuinely required.

---

# 137. ConcurrentHashMap

`ConcurrentHashMap` does not automatically make a compound multi-step operation atomic.

---

# 138. Atomic Operation

Use atomic collection operations such as:

```java
computeIfAbsent
```

when semantics fit.

---

# 139. Locks

Explicit locks should be used only when simpler immutability/confinement approaches are insufficient.

---

# 140. `synchronized`

`synchronized` remains valid Java, but blocking behavior inside synchronized sections requires care with Virtual Threads.

---

# 141. Pinning

Virtual Thread pinning can reduce scalability when a Virtual Thread cannot unmount from its carrier while blocked.

---

# 142. Pinning Sources

Problematic patterns historically include blocking operations while holding certain JVM/native synchronization constructs.

The exact behavior depends on JDK implementation/version and must be measured rather than assumed.

---

# 143. Long Blocking Critical Section

Avoid long blocking I/O while holding a lock.

---

# 144. Lock Scope

Critical sections should remain short.

---

# 145. ReentrantLock

`ReentrantLock` may be preferable where advanced locking semantics are required.

---

# 146. Lock Ordering

Multiple locks require consistent ordering to prevent deadlocks.

---

# 147. Deadlock

Virtual Threads do not eliminate deadlocks.

---

# 148. Race Condition

Virtual Threads do not eliminate race conditions.

---

# 149. Atomicity

Thread-safe primitives must reflect actual business atomicity requirements.

---

# 150. Database Atomicity

Cross-request business invariants should normally rely on transactional/database mechanisms where appropriate, not JVM locks alone.

---

# 151. Multi-Pod Deployment

A JVM lock protects only one application instance.

---

# 152. Distributed Concurrency

Kubernetes replicas mean:

```text
Pod A Lock
    !=
Pod B Lock
```

---

# 153. Distributed Business Invariants

Use appropriate:

- database constraints
- optimistic locking
- pessimistic locking
- idempotency
- messaging semantics

for distributed correctness.

---

# 154. Optimistic Locking

Optimistic locking is preferred for many low-contention concurrent-update scenarios.

---

# 155. Retry Conflict

Optimistic-lock retry must remain bounded.

---

# 156. Pessimistic Lock

Pessimistic locking requires careful timeout and transaction-duration management.

---

# 157. Parallel Streams

Parallel streams are not the default concurrency mechanism for service business logic.

---

# 158. Why

`parallelStream()` typically uses the common ForkJoinPool and obscures:

- executor ownership
- blocking behavior
- context propagation
- concurrency limits

---

# 159. Parallel Stream Eligibility

Use only for measured CPU-oriented operations where its execution semantics are appropriate.

---

# 160. Reactive Programming

Reactive programming remains valid where streaming/backpressure semantics justify it.

---

# 161. Virtual Threads vs Reactive

Virtual Threads and reactive programming solve overlapping but different concerns.

---

# 162. Virtual Thread Preference

For ordinary imperative blocking service orchestration, Virtual Threads may provide simpler code.

---

# 163. Reactive Preference

Reactive pipelines remain appropriate for:

- streaming
- event-driven flows
- Reactor-native infrastructure
- explicit backpressure

---

# 164. No Forced Rewrite

Existing stable WebClient/Reactor integrations must not be rewritten merely to claim Virtual Thread adoption.

---

# 165. Blocking Event Loop

Blocking operations must not execute on Reactor event-loop threads.

---

# 166. Event Loop Protection

If blocking work enters a reactive pipeline, it must be isolated appropriately.

---

# 167. WebClient `.block()`

Calling `.block()` requires awareness of execution context.

It must not block a Reactor event-loop thread.

---

# 168. MVC Service

In an imperative MVC/Virtual Thread request model, blocking at a controlled boundary may be acceptable when the client configuration and architecture support it.

---

# 169. Retry

Concurrency and retry interact multiplicatively.

---

# 170. Example

```text
50 concurrent requests

×

5 parallel downstream calls

×

3 retries

=

750 possible downstream attempts
```

---

# 171. Retry Budget

Retries must be included in capacity calculations.

---

# 172. Circuit Breaker

Circuit breakers reduce repeated calls to unhealthy dependencies but do not themselves limit concurrency.

---

# 173. Bulkhead vs Circuit Breaker

```text
Circuit Breaker
=
Should calls continue?

Bulkhead
=
How many calls may run concurrently?
```

---

# 174. Rate Limiter

```text
Rate Limiter
=
How many calls per time interval?
```

---

# 175. Timeout

```text
Timeout
=
How long may a call consume capacity?
```

---

# 176. Combined Resilience

A dependency policy may therefore contain:

```text
Timeout

+

Concurrency Limit

+

Circuit Breaker

+

Optional Bounded Retry
```

but every component must have a defined purpose.

---

# 177. Resilience Order

The order of resilience decorators must be deliberate because it changes semantics.

---

# 178. Metrics

Concurrency behavior must be observable.

---

# 179. Required Metrics

Critical workloads should monitor:

```text
Active Tasks

Task Duration

Queue / Pending Work

Concurrency Rejections

Timeouts

Cancellations

Dependency Latency

Connection Pool Usage

DB Pool Usage
```

---

# 180. Hikari Metrics

Monitor at least:

```text
active

idle

pending

max
```

where available.

---

# 181. HTTP Pool Metrics

Monitor:

```text
active connections

idle connections

pending acquisition

acquisition timeout
```

where available.

---

# 182. Bulkhead Metrics

Monitor:

```text
available permits

concurrent calls

rejected calls
```

where supported.

---

# 183. Executor Metrics

Traditional thread-pool metrics are less meaningful for thread-per-task Virtual Thread executors than for bounded platform pools.

Focus on workload/resource metrics.

---

# 184. Virtual Thread Count

A high Virtual Thread count alone does not necessarily indicate a problem.

---

# 185. Waiting Location

More useful questions include:

```text
What are they waiting for?

How long?

Which dependency?

Is capacity saturated?
```

---

# 186. Thread Dumps

Thread dumps remain valuable for diagnosing blocked/waiting Virtual Threads.

---

# 187. JFR

Java Flight Recorder should be used for deeper concurrency/performance diagnosis where appropriate.

---

# 188. Pinning Diagnostics

JDK tooling/JFR capabilities should be used to identify problematic Virtual Thread pinning when suspected.

---

# 189. Logging

Do not log every task submission/completion in high-volume concurrency paths.

---

# 190. Failure Logs

Log meaningful aggregate/dependency failures according to structured logging standards.

---

# 191. High Cardinality

Task IDs and arbitrary request identifiers must not become metric labels.

---

# 192. Tracing

Concurrent downstream calls should remain children/related spans of the originating trace when tracing is enabled.

---

# 193. Trace Fan-Out

Tracing should make fan-out visible:

```text
Order Request
    |
    +--> Customers
    |
    +--> Products
    |
    +--> Users
```

---

# 194. Graceful Shutdown

Concurrency infrastructure must participate in graceful shutdown.

---

# 195. New Work

During shutdown, stop accepting new nonessential work.

---

# 196. In-Flight Work

Allow bounded time for in-flight work to complete where appropriate.

---

# 197. Forced Termination

After the grace period, remaining tasks may require cancellation according to workload semantics.

---

# 198. Durable Work

Work that cannot safely be lost must not depend only on graceful shutdown.

Use durable messaging/persistence.

---

# 199. Kubernetes

Pod termination grace period must align with application shutdown behavior.

---

# 200. Readiness

A terminating pod should leave readiness before final shutdown so new traffic is drained.

---

# 201. Testing

Concurrent code requires deterministic automated tests where practical.

---

# 202. No `Thread.sleep`

Tests should not use arbitrary `Thread.sleep` as the primary synchronization mechanism.

---

# 203. Coordination

Prefer deterministic primitives such as:

- CountDownLatch
- CyclicBarrier
- CompletableFuture coordination
- Awaitility where approved
- controlled test executors

---

# 204. Race Test

Critical concurrency logic should test simultaneous operations.

---

# 205. Context Propagation Test

Verify:

```text
SecurityContext

Correlation

RequestContext
```

inside submitted tasks where required.

---

# 206. Context Isolation Test

Verify one request's context does not leak into another.

---

# 207. Timeout Test

Timeout tests should use controlled dependencies rather than long real waits.

---

# 208. Cancellation Test

Verify cancellation semantics for expensive concurrent operations.

---

# 209. Partial Failure Test

Test combinations such as:

```text
Customers succeeds

Products fails

Users succeeds
```

and verify defined behavior.

---

# 210. Bulkhead Test

Verify calls beyond the concurrency limit are handled according to policy.

---

# 211. Pool Exhaustion Test

Critical integrations should test:

- HTTP pool exhaustion
- DB pool exhaustion

where feasible.

---

# 212. Load Test

Concurrency configuration must be validated under realistic load.

---

# 213. Unit Tests Are Insufficient

Unit tests cannot prove system-level concurrency capacity.

---

# 214. Load-Test Variables

Measure:

- request concurrency
- p50/p95/p99 latency
- throughput
- CPU
- memory
- DB pool
- HTTP pool
- dependency saturation
- timeout rate
- error rate

---

# 215. Step Load

Increase concurrency gradually to identify saturation points.

---

# 216. Knee Point

Identify where additional concurrency stops increasing throughput and starts increasing latency/errors.

---

# 217. Concurrency Limit

Production limits should normally remain below destructive saturation.

---

# 218. Performance Regression

Concurrency configuration changes require performance-regression validation for critical paths.

---

# 219. Sonar

Concurrency code must remain Sonar-clean.

---

# 220. Exception Handling

Concurrent exception handling must satisfy the project rule:

```text
Either log or rethrow,
without silently swallowing the exception.
```

---

# 221. Duplicate Logging

Do not both log and rethrow at every layer when that creates duplicate error logs.

---

# 222. InterruptedException Handling

`InterruptedException` must be propagated or handled while preserving interruption semantics.

---

# 223. Assertions

Concurrency tests must follow the platform's AssertJ/Sonar conventions.

---

# 224. AssertJ Description

Assertions should include meaningful descriptions:

```java
assertThat(result)
        .as("resultado da validação concorrente")
        .isNotNull();
```

---

# 225. Test Naming

Tests should follow established project naming conventions.

---

# 226. Test Constants

Stable test constants should be preferred over unnecessary random identifiers.

---

# 227. Security

Concurrency must not bypass security checks.

---

# 228. Caller Identity

Parallel execution under a caller identity must preserve the intended authenticated principal.

---

# 229. Privilege Escalation

Fallback to an empty/system SecurityContext must not accidentally grant broader permissions.

---

# 230. Audit

Audit events generated from concurrent tasks must preserve correct user and correlation information.

---

# 231. Thread-Safe Credentials

Token providers used concurrently must be thread-safe.

---

# 232. Token Refresh

Concurrent token refresh must avoid:

```text
100 requests

↓

token expired

↓

100 login/refresh requests
```

---

# 233. Refresh Coalescing

Token refresh should be coalesced/synchronized where appropriate.

---

# 234. Cached Token

Token caching must consider:

- expiration
- refresh buffer
- concurrent access
- failed refresh
- secret handling

---

# 235. File Processing

Concurrent file processing must respect:

- file descriptor limits
- storage throughput
- ordering
- memory

---

# 236. Entire File in Memory

Parallelism must not multiply large per-task memory allocation uncontrollably.

---

# 237. Memory Amplification

Example:

```text
100 concurrent tasks

×

50 MB per task

=

5 GB
```

before application/framework overhead.

---

# 238. Memory Budget

Concurrency design must include per-task memory cost.

---

# 239. Backpressure

Producers must not indefinitely outpace consumers.

---

# 240. Bounded Queue

Where queues are used, they must normally be bounded.

---

# 241. Queue Capacity

Queue capacity must be based on acceptable latency/memory, not merely a large number.

---

# 242. Queue Is Not Capacity

A larger queue does not increase processing capacity.

---

# 243. Overload

The preferred overload sequence is:

```text
Normal Processing

↓

Concurrency Limit Reached

↓

Short Bounded Wait if Appropriate

↓

Reject / Shed Load

↓

Recover
```

not:

```text
Normal Processing

↓

Infinite Queue

↓

Memory Growth

↓

Timeout Storm

↓

Crash
```

---

# 244. Architecture Review Trigger

Concurrency architecture review is required when introducing:

- new executor
- new high-volume fan-out
- distributed lock
- large parallel batch
- parallel DB access
- custom thread pool
- blocking operation in reactive event loop
- new retry + concurrency combination

---

# 245. Anti-Patterns

The following are prohibited or strongly discouraged:

- treating Virtual Threads as unlimited capacity
- pooling Virtual Threads solely to limit thread count
- unbounded fan-out
- unbounded executor queues
- accidental use of common ForkJoinPool for blocking I/O
- `parallelStream()` for arbitrary service I/O
- large blocking work on Reactor event loops
- ignoring WebClient connection limits
- ignoring HikariCP limits
- parallel JPA work assuming one EntityManager is thread-safe
- long external calls inside DB transactions
- arbitrary global concurrency limits
- retry multiplication
- fire-and-forget business work from HTTP requests
- treating executors as durable queues
- swallowing asynchronous exceptions
- swallowing `InterruptedException`
- ignoring cancellation
- context leakage between tasks
- storing request state in singleton bean fields
- long blocking I/O while holding locks
- JVM locks for distributed business invariants
- excessive task creation for large batches without chunking
- unbounded token refresh concurrency
- using `Thread.sleep` to stabilize concurrency tests
- increasing concurrency without load testing
- increasing queues to hide saturation
- measuring only thread count instead of constrained resources

---

# 246. Positive Consequences

The decision provides:

- simpler blocking Java code
- efficient I/O concurrency
- lower platform-thread overhead
- improved fan-out latency
- explicit capacity protection
- safer DB usage
- safer HTTP integrations
- predictable overload behavior
- improved context propagation
- better concurrency observability
- standardized Java 21 usage

---

# 247. Negative Consequences

The decision introduces:

- concurrency-budget management
- bulkhead configuration
- context-propagation infrastructure
- additional concurrency tests
- more load testing
- resource-capacity monitoring
- explicit cancellation/failure design

These costs are accepted because uncontrolled concurrency creates severe production instability.

---

# 248. Neutral Consequences

The decision also means:

- not every operation should be parallel
- Virtual Threads do not replace reactive programming everywhere
- connection pools remain necessary
- some workloads remain CPU-bound
- Kafka concurrency remains partition-bound
- concurrency limits vary by dependency
- high thread counts may be normal
- throughput remains constrained by the slowest finite resource

---

# 249. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Unbounded concurrency | Critical | Medium | Per-resource bulkheads |
| DB pool exhaustion | Critical | Medium | Hikari alignment |
| HTTP pool exhaustion | High | Medium | Bounded connection pool |
| Retry amplification | Critical | Medium | Retry budget |
| Context loss | High | Medium | Context-aware executors |
| Context leakage | Critical | Low | Capture/restore/cleanup |
| Kafka ordering violation | Critical | Medium | Partition-aware processing |
| CPU saturation | High | Medium | CPU-bound pools |
| Deadlock | Critical | Low | Minimize locks |
| Memory amplification | High | Medium | Bounded tasks/chunks |
| Orphan work | Medium | Medium | Cancellation/deadlines |
| Token refresh stampede | High | Medium | Refresh coalescing |
| Event-loop blocking | High | Medium | Execution isolation |
| Executor task loss | High | Medium | Durable async architecture |

---

# 250. Implementation Guidance

The following rules are mandatory:

1. Virtual Threads are preferred for suitable blocking I/O orchestration.
2. Virtual Threads must not be treated as unlimited downstream capacity.
3. Concurrency must be bounded at constrained resources.
4. CPU-bound workloads require bounded CPU parallelism.
5. Independent I/O operations may use fan-out/fan-in.
6. High-volume fan-out requires concurrency budgeting.
7. Dependency-specific limits are preferred over arbitrary global limits.
8. WebClient connection-pool limits must align with application concurrency.
9. HTTP pending acquisition must remain bounded.
10. HikariCP capacity must be considered before parallel DB access.
11. Database transactions must remain short.
12. Slow external calls should normally not occur inside DB transactions.
13. JPA EntityManager/transaction context must not be casually shared across tasks.
14. Kafka concurrency must respect partitions and ordering.
15. Large batch workloads must use bounded partitioning/chunking.
16. Executors must have explicit ownership and lifecycle.
17. Blocking `CompletableFuture` tasks must not accidentally use the common pool.
18. Concurrent operations require individual timeouts and an overall deadline.
19. Cancellation semantics must be explicit.
20. `InterruptedException` must not be swallowed.
21. Fire-and-forget durable business work must use durable infrastructure.
22. SecurityContext must be propagated when required.
23. Request/correlation context must be propagated when required.
24. Propagated context must be cleaned after task completion.
25. Request-specific mutable state must not live in singleton fields.
26. Shared mutable state must be minimized.
27. JVM locks must not be used as distributed correctness guarantees.
28. Retry amplification must be included in concurrency calculations.
29. Queue capacity must remain bounded.
30. Load shedding is preferred over unbounded queueing.
31. Critical concurrency paths require resource metrics.
32. Concurrency changes require realistic load testing.
33. Concurrency tests should use deterministic synchronization instead of arbitrary sleeps.
34. Async exceptions must be handled explicitly.
35. Concurrency test assertions must follow established AssertJ/Sonar conventions.

---

# 251. Concurrency Production Readiness Gate

A new concurrent workload is not production ready until:

```text
[ ] Workload classified as I/O-bound or CPU-bound

[ ] Dependencies identified

[ ] Independence of parallel tasks confirmed

[ ] Maximum fan-out calculated

[ ] Expected request concurrency estimated

[ ] Amplification calculated

[ ] DB connection demand calculated

[ ] HTTP connection demand calculated

[ ] Redis demand reviewed

[ ] Kafka partition limits reviewed

[ ] Concurrency limit defined

[ ] Queue/pending limits defined

[ ] Timeout defined per dependency

[ ] Overall deadline defined

[ ] Cancellation policy defined

[ ] Failure policy defined

[ ] Partial-success policy defined

[ ] Retry budget calculated

[ ] SecurityContext propagation tested

[ ] Correlation propagation tested

[ ] Context isolation tested

[ ] Executor lifecycle reviewed

[ ] Graceful shutdown reviewed

[ ] Pool exhaustion tested

[ ] Load test completed

[ ] p95/p99 latency reviewed

[ ] CPU saturation reviewed

[ ] Memory amplification reviewed

[ ] Hikari metrics reviewed

[ ] HTTP pool metrics reviewed

[ ] Bulkhead metrics reviewed

[ ] Sonar analysis passed
```

---

# 252. Validation

This ADR will be validated through:

- architecture reviews
- code reviews
- concurrency tests
- context-propagation tests
- integration tests
- pool-exhaustion tests
- Kafka ordering tests
- batch tests
- load tests
- stress tests
- JFR analysis
- thread dumps
- Hikari metrics
- HTTP connection metrics
- Resilience4j metrics
- production SLO monitoring

---

# 253. Success Criteria

The decision is successful when:

- Virtual Threads simplify blocking orchestration
- increased concurrency improves throughput until known capacity limits
- DB pools remain protected
- HTTP pools remain protected
- Kafka ordering remains correct
- retries do not amplify outages
- context is preserved across concurrent operations
- no cross-request context leakage occurs
- overload produces controlled degradation
- concurrent batch processing remains memory-bounded
- shutdown does not create uncontrolled task loss
- concurrency behavior is diagnosable in production

---

# 254. Alternatives Rejected

## 254.1 Traditional Fixed Thread Pools for All Blocking I/O

Rejected as the default because Java 21 Virtual Threads provide simpler and more scalable thread-per-task blocking concurrency.

---

## 254.2 Unlimited Virtual Thread Fan-Out

Rejected because downstream resources remain finite.

---

## 254.3 Reactive Programming for Every Service Operation

Rejected as a universal requirement because ordinary blocking orchestration can be simpler with Virtual Threads.

---

## 254.4 `parallelStream()` for Service Integrations

Rejected as the default because executor ownership and blocking behavior become implicit.

---

## 254.5 Common ForkJoinPool for Blocking Integrations

Rejected because unrelated workloads contend for a process-wide shared resource.

---

## 254.6 Large Queues Instead of Concurrency Limits

Rejected because queues increase latency without increasing throughput.

---

## 254.7 JVM Locks for Distributed Business Rules

Rejected because application replicas do not share JVM locks.

---

# 255. Related Decisions

This ADR is related to:

- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-009: Use Apache Kafka for Integration Events
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards
- ADR-035: Adopt Engineering Quality and Testing Standards

---

# 256. References

- Java 21 Documentation
- JEP 444: Virtual Threads
- Java ExecutorService Documentation
- CompletableFuture Documentation
- Spring Security Concurrency Support
- Spring Framework Documentation
- Reactor Netty Documentation
- HikariCP Documentation
- Apache Kafka Documentation
- Resilience4j Documentation
- Java Flight Recorder Documentation
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards

---

# 257. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial Java 21 concurrency baseline |

---

# 258. Decision Summary

The central rule is:

```text
Virtual Threads
      |
      v
Cheap Concurrency
      |
      X
      |
      +---- DOES NOT MEAN ----+
                              |
                              v
                    Unlimited Capacity
```

Instead:

```text
                    APPLICATION
                         |
                         v
                  VIRTUAL THREADS
                         |
       +-----------------+-----------------+
       |                 |                 |
       v                 v                 v
   HTTP LIMIT         DB LIMIT         REDIS LIMIT
       |                 |                 |
       v                 v                 v
 Connection Pool      HikariCP       Redis Capacity
       |                 |                 |
       +-----------------+-----------------+
                         |
                         v
                  SYSTEM THROUGHPUT
```

Fan-out follows:

```text
                        REQUEST
                           |
                           v
                    OVERALL DEADLINE
                           |
              +------------+------------+
              |            |            |
              v            v            v
          Customers     Products      Users
          Bulkhead      Bulkhead      Bulkhead
              |            |            |
              v            v            v
           Timeout      Timeout      Timeout
              |            |            |
              +------------+------------+
                           |
                           v
                     FAILURE POLICY
                           |
                           v
                         JOIN
                           |
                           v
                       RESPONSE
```

Capacity must be evaluated multiplicatively:

```text
Inbound Requests
        ×
Fan-Out
        ×
Retries
        =
Potential Downstream Load
```

For example:

```text
100 inbound requests
       ×
5 downstream operations
       ×
2 attempts
       =
1,000 possible downstream calls
```

Database concurrency follows:

```text
1,000 Virtual Threads
          |
          v
   HikariCP = 20
          |
          v
20 DB Connections
          |
          v
Remaining work waits
```

Increasing this:

```text
Virtual Threads
1000 → 10000
```

does not change:

```text
HikariCP
20 → 20
```

and therefore does not automatically increase database throughput.

The same applies to HTTP:

```text
Virtual Threads = 1,000

WebClient Connections = 50

External API Capacity = 30
```

The effective safe concurrency is constrained by the smallest relevant capacity.

Therefore:

```text
Virtual Thread Count
        !=
Safe Concurrency
```

and:

```text
Concurrency
    !=
Parallelism
    !=
Throughput
```

For durable asynchronous work:

```text
WRONG

HTTP Request
     |
     v
executor.submit(...)
     |
     v
Return 200
     |
     v
Pod Dies
     |
     v
Task Lost
```

Use:

```text
HTTP Request
     |
     v
Transactional State / Outbox
     |
     v
Kafka
     |
     v
Consumer
     |
     v
Durable Processing
```

For overload:

```text
GOOD

Traffic
   |
   v
Bounded Admission
   |
   +---- Capacity ----> Execute
   |
   +---- Saturated ---> Short Wait / Reject
                            |
                            v
                    Controlled Degradation
```

instead of:

```text
BAD

Traffic
   |
   v
Unlimited Tasks
   |
   v
Unlimited Waiting
   |
   v
Pool Saturation
   |
   v
Timeout Storm
   |
   v
Retries
   |
   v
Cascading Failure
```

And the most important production rule is:

```text
Do not ask:

"How many Virtual Threads can Java create?"

Ask:

"How many concurrent operations
can each dependency safely sustain?"
```

That is the capacity that governs the architecture.
