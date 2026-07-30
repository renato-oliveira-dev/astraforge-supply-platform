# ADR-054: Adopt Enterprise Performance Engineering, Capacity Testing and JVM Optimization Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-054 |
| Title | Adopt Enterprise Performance Engineering, Capacity Testing and JVM Optimization Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Performance, JVM, Capacity, PostgreSQL, SQS, Redis, Java 21 |
| Related Work Items | Performance Engineering, Virtual Threads, Database Optimization, Capacity Planning |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise applications must remain predictable not only functionally but also under realistic production workloads.

A service can be functionally correct while still failing operationally because of:

```text
High Latency

Connection Pool Exhaustion

Thread Exhaustion

Memory Pressure

Excessive Garbage Collection

Database Contention

N+1 Queries

Slow SQL

SQS Queue Backlog/Oldest-Message Age

Redis Saturation

External API Bottlenecks

Unbounded Parallelism
```

Performance therefore cannot be treated as a final-stage optimization activity.

It is an architectural quality attribute.

---

# 2. Problem Statement

The organization requires standards covering:

- performance engineering
- capacity planning
- latency
- throughput
- percentiles
- profiling
- Java Flight Recorder
- Java Mission Control
- heap
- native memory
- garbage collection
- CPU
- threads
- Virtual Threads
- connection pools
- PostgreSQL
- SQS
- Redis
- external integrations
- JMH
- load testing
- stress testing
- spike testing
- soak testing
- performance regression
- CI/CD performance gates

---

# 3. Decision Drivers

Primary drivers are:

1. predictable latency
2. production stability
3. scalability
4. efficient infrastructure utilization
5. measurable capacity
6. controlled concurrency
7. database efficiency
8. early regression detection
9. operational resilience
10. evidence-based optimization

---

# 4. Decision

Performance engineering MUST be treated as a continuous engineering discipline.

The canonical model is:

```text
BUSINESS WORKLOAD
       |
       v
PERFORMANCE OBJECTIVES
       |
       v
BASELINE
       |
       v
MEASURE
       |
       v
PROFILE
       |
       v
IDENTIFY BOTTLENECK
       |
       v
OPTIMIZE
       |
       v
VALIDATE
       |
       v
REGRESSION CONTROL
```

---

# 5. Fundamental Principle

The governing principle is:

```text
Measure before optimizing.

Optimize the actual bottleneck.

Measure again after changing it.
```

---

# 6. Performance Requirements

Critical services MUST define measurable performance objectives.

---

# 7. Performance Dimensions

At minimum, applicable services SHOULD understand:

```text
Latency

Throughput

Concurrency

Resource Utilization

Error Rate

Saturation
```

---

# 8. Average Is Insufficient

Average latency alone MUST NOT be used as the primary performance indicator.

---

# 9. Percentiles

Critical APIs SHOULD monitor percentiles such as:

```text
p50

p95

p99
```

---

# 10. Example

An API with:

```text
Average = 150 ms
p95     = 400 ms
p99     = 4.5 s
```

has a materially different operational profile from one whose p99 remains close to its p95.

---

# 11. Tail Latency

Tail latency MUST be considered for user-facing and synchronous distributed workflows.

---

# 12. Performance Budget

Critical operations SHOULD have explicit performance budgets.

Example:

```text
POST /orders

p95 <= 500 ms

p99 <= 1 s

Error Rate < 1%
```

Actual thresholds MUST derive from business and operational requirements.

---

# 13. Performance Budget Ownership

Performance objectives MUST have an identified owner.

---

# 14. Throughput

Throughput MUST be expressed using meaningful workload units.

Examples:

```text
Requests / second

Orders / minute

Events / second

Invoices / hour
```

---

# 15. Workload Model

Performance tests MUST use a documented workload model.

---

# 16. Workload Characteristics

The model SHOULD define:

```text
Request Distribution

Read / Write Ratio

Payload Size

Concurrency

Arrival Rate

Data Volume

Think Time

External Dependency Behavior
```

---

# 17. Production Representativeness

Performance results are only meaningful relative to the workload being simulated.

---

# 18. Baseline

Performance-sensitive services SHOULD maintain a known baseline.

---

# 19. Baseline Example

```text
Version: 3.12.0

Throughput: 850 req/s

p95: 310 ms

p99: 720 ms

CPU: 62%

Memory: 1.8 GB
```

---

# 20. Regression

A material unexplained degradation from the approved baseline MUST be investigated.

---

# 21. Profiling

Profiling SHOULD precede non-trivial optimization.

---

# 22. Java Flight Recorder

Java Flight Recorder is the preferred JVM-level profiling mechanism for representative Java workloads.

---

# 23. Java Mission Control

Java Mission Control SHOULD be used to analyze JFR recordings where appropriate.

---

# 24. Profiling Areas

JFR analysis SHOULD consider:

```text
CPU Hotspots

Allocation Hotspots

Garbage Collection

Thread Contention

Locking

I/O

Socket Activity

Exceptions

Class Loading
```

---

# 25. Production Profiling

Low-overhead production profiling MAY be used according to operational and security policy.

---

# 26. Profiling Data

Profiling artifacts MUST be handled according to data-security requirements.

---

# 27. CPU Bound Workload

CPU-bound workload is primarily constrained by computational capacity.

Examples:

```text
Encryption

Compression

Complex Calculations

Serialization

Large Transformations
```

---

# 28. I/O Bound Workload

I/O-bound workload spends significant time waiting for:

```text
Database

HTTP

Filesystem

Messaging

Network
```

---

# 29. Optimization Strategy

CPU-bound and I/O-bound workloads MUST NOT automatically use the same concurrency strategy.

---

# 30. CPU Parallelism

CPU-bound parallelism SHOULD generally remain related to available processing capacity.

---

# 31. I/O Concurrency

I/O-bound workloads MAY support substantially greater concurrency.

---

# 32. Virtual Threads

Java 21 Virtual Threads SHOULD be considered for high-concurrency blocking I/O workloads.

---

# 33. Virtual Threads Do Not Increase Database Capacity

This remains true:

```text
10,000 Virtual Threads

!=

10,000 Database Connections
```

---

# 34. Downstream Capacity

Concurrency MUST remain bounded by constrained downstream resources.

---

# 35. Concurrency Model

Target:

```text
REQUESTS
    |
    v
VIRTUAL THREADS
    |
    v
CONCURRENCY LIMIT
    |
    v
DOWNSTREAM RESOURCE
```

---

# 36. Unbounded Parallelism

Unbounded task submission is prohibited for critical workloads.

---

# 37. Little's Law

Capacity analysis SHOULD consider the relationship between:

```text
Concurrency

Throughput

Latency
```

Conceptually:

```text
Concurrency ≈ Throughput × Latency
```

---

# 38. Practical Implication

Increasing latency while maintaining arrival rate increases concurrent work in the system.

This can amplify saturation.

---

# 39. Backpressure

Systems MUST have a strategy for overload.

---

# 40. Overload Strategies

Depending on architecture:

```text
Queue

Throttle

Reject

Rate Limit

Defer

Scale
```

---

# 41. Infinite Queue

An effectively infinite queue is not a valid capacity strategy.

---

# 42. Queueing

Queues move work in time.

They do not create processing capacity.

---

# 43. JVM Memory

Java services MUST understand their memory envelope.

---

# 44. Heap

Heap sizing SHOULD reflect:

```text
Live Object Set

Allocation Rate

Container Memory

GC Strategy

Traffic Characteristics
```

---

# 45. Container Memory

JVM memory planning MUST account for more than heap.

---

# 46. Memory Components

Examples:

```text
Java Heap

Metaspace

Thread Stacks

Code Cache

Direct Buffers

Native Libraries

JVM Native Memory
```

---

# 47. Container Limit

The process MUST remain safely below the container memory limit under expected workload.

---

# 48. Native Memory

Native memory SHOULD be investigated when RSS materially exceeds expected heap usage.

---

# 49. Native Memory Tracking

JVM Native Memory Tracking MAY be enabled for diagnostic environments where needed.

---

# 50. Memory Leak

A growing heap alone does not prove a memory leak.

---

# 51. Leak Investigation

Analyze:

```text
Post-GC Live Set

Heap Dumps

Object Retention

Allocation Rate

GC Behavior
```

---

# 52. Heap Dump

Heap dumps MAY contain sensitive application data and MUST be protected accordingly.

---

# 53. Garbage Collection

GC tuning MUST be workload-driven.

---

# 54. Default GC

The approved/default Java 21 GC SHOULD normally be retained unless measurement demonstrates a reason to change it.

---

# 55. GC Metrics

Monitor:

```text
Pause Duration

Pause Frequency

Allocation Rate

Promotion

Heap Occupancy

CPU Cost
```

---

# 56. GC Tuning Anti-Pattern

Do not copy JVM flags from unrelated applications without measurement.

---

# 57. JVM Flags

Custom JVM flags MUST have a documented reason.

---

# 58. Flag Review

JVM flags SHOULD be revisited after major Java runtime upgrades.

---

# 59. Object Allocation

High allocation rate SHOULD be investigated when it contributes materially to CPU or GC pressure.

---

# 60. Premature Allocation Optimization

Do not sacrifice readability merely to eliminate insignificant short-lived allocations.

---

# 61. String Processing

Heavy string manipulation SHOULD be profiled when present on high-throughput paths.

---

# 62. Serialization

Serialization/deserialization cost SHOULD be considered for large or frequent payloads.

---

# 63. Payload Size

Large payloads affect:

```text
Network

Serialization

Memory

GC

Latency
```

---

# 64. API Payload

REST APIs SHOULD avoid returning substantially more data than consumers require.

---

# 65. Pagination

Large collections MUST use bounded retrieval.

---

# 66. Compression

Compression MAY reduce network usage but increases CPU consumption.

It MUST be evaluated as a trade-off.

---

# 67. Database Performance

Database access is a primary performance boundary.

---

# 68. Query Measurement

Slow SQL MUST be analyzed using actual database execution information.

---

# 69. PostgreSQL

For PostgreSQL, use:

```text
EXPLAIN

EXPLAIN ANALYZE
```

where appropriate.

---

# 70. EXPLAIN ANALYZE Safety

`EXPLAIN ANALYZE` executes the statement.

It MUST be used carefully with modifying statements and production workloads.

---

# 71. Query Plan

Analysis SHOULD consider:

```text
Sequential Scan

Index Scan

Join Strategy

Rows Estimated

Rows Actual

Sort

Buffers

Execution Time
```

---

# 72. Indexes

Indexes MUST correspond to real access patterns.

---

# 73. Over-Indexing

More indexes are not automatically better.

Indexes increase:

```text
Storage

Write Cost

Maintenance

Vacuum Work
```

---

# 74. Composite Index

Composite index column order MUST reflect query patterns.

---

# 75. N+1

N+1 query patterns MUST be treated as performance defects when material.

---

# 76. ORM

ORM convenience does not eliminate the need to understand generated SQL.

---

# 77. Hibernate SQL

Critical persistence flows SHOULD be inspected at SQL level during performance analysis.

---

# 78. Fetch Strategy

Fetch strategies MUST be workload-aware.

---

# 79. EAGER

Global EAGER loading is not an acceptable generic N+1 solution.

---

# 80. Batch Fetching

Batch fetching MAY reduce round trips where appropriate.

---

# 81. Projection

Read-specific projections SHOULD be considered when only a subset of entity data is required.

---

# 82. Query-Specific DTO

High-volume read paths MAY use dedicated query DTOs/projections.

---

# 83. Pagination Performance

Offset pagination can become expensive at large offsets.

---

# 84. Keyset Pagination

Keyset pagination SHOULD be considered for very large sequential datasets.

---

# 85. Transaction Duration

Database transactions SHOULD remain short.

---

# 86. Remote Call Inside Transaction

Avoid:

```text
BEGIN TRANSACTION

UPDATE DATABASE

CALL REMOTE API

WAIT

COMMIT
```

unless architecture explicitly requires it.

---

# 87. Lock Duration

Long transactions increase lock duration and contention.

---

# 88. Connection Pool

Database connection pools MUST have explicit sizing.

---

# 89. Pool Size

Pool size MUST NOT be increased blindly to solve latency.

---

# 90. Database Saturation

Increasing connections against an already saturated database can reduce total throughput.

---

# 91. Pool Metrics

Monitor:

```text
Active Connections

Idle Connections

Pending Requests

Acquisition Time

Timeouts
```

---

# 92. Pool Exhaustion

Connection-pool exhaustion MUST be distinguishable from slow application processing.

---

# 93. HTTP Connection Pools

External HTTP clients MUST use bounded connection pools where pooling is applicable.

---

# 94. HTTP Pool Metrics

Monitor:

```text
Active Connections

Pending Acquires

Connection Creation

Timeouts

Response Latency
```

---

# 95. Timeouts

Every remote dependency MUST have bounded timeout behavior.

---

# 96. Timeout Budget

Timeouts SHOULD reflect the calling operation's end-to-end latency budget.

---

# 97. Timeout Stacking

Nested remote calls MUST consider cumulative timeout.

---

# 98. Retry Amplification

Retries increase load.

---

# 99. Retry Storm

During dependency degradation:

```text
100 requests
    |
    v
3 retries each
    |
    v
potentially 300 attempts
```

can worsen the incident.

---

# 100. Retry Policy

Retry MUST be:

```text
Bounded

Selective

Idempotency-Aware

Backoff-Aware
```

---

# 101. Circuit Breaker

Circuit breakers SHOULD prevent continued expensive calls to failing dependencies.

---

# 102. Bulkhead

Bulkhead isolation SHOULD be considered when one dependency can consume resources required by unrelated workloads.

---

# 103. External API Performance

External integrations SHOULD be measured independently.

---

# 104. Remote Latency

End-to-end latency SHOULD distinguish:

```text
Application Processing

Database Time

Remote Dependency Time
```

---

# 105. N+1 HTTP Calls

Repeated per-item remote calls SHOULD be avoided when a batch API exists.

---

# 106. Preferred Pattern

Prefer:

```text
1 batch request for 100 products
```

over:

```text
100 individual requests
```

when the external contract supports batching.

---

# 107. Parallel HTTP Calls

Parallel calls MAY reduce wall-clock latency when batching is unavailable.

---

# 108. Parallel Call Limit

Parallelism MUST be bounded.

---

# 109. SQS Performance

SQS performance requires producer and consumer analysis.

---

# 110. SQS Producer Metrics

Monitor:

```text
Send Rate

Request Latency

Batch Size

Compression

Retries

Errors
```

---

# 111. Producer Batching

Producer batching MAY improve throughput at the cost of additional latency.

---

# 112. SQS Consumer Metrics

Monitor:

```text
Queue Backlog/Oldest-Message Age

Records Consumed

Processing Rate

Poll Duration

Rebalances

Errors
```

---

# 113. Queue Backlog/Oldest-Message Age

Lag is a key capacity signal.

---

# 114. Lag Interpretation

Growing lag means:

```text
Arrival Rate
    >
Processing Rate
```

for a sustained period.

---

# 115. SQS Parallelism

Consumer scalability is constrained by partitioning.

---

# 116. Excess Consumers

More consumers than useful partitions do not increase effective partition parallelism.

---

# 117. Processing Time

Slow event handlers reduce consumer throughput.

---

# 118. Batch Consumption

Batch processing SHOULD be considered where business semantics allow it.

---

# 119. Backlog Recovery

Capacity tests SHOULD validate backlog recovery.

---

# 120. Recovery Objective

A critical consumer SHOULD have an understood recovery rate after an outage.

---

# 121. Redis Performance

Redis performance SHOULD be evaluated using:

```text
Hit Ratio

Miss Ratio

Latency

Memory

Evictions

Connection Usage
```

---

# 122. Cache Hit Ratio

A low hit ratio can indicate ineffective caching.

---

# 123. High Hit Ratio Is Not Sufficient

A cache can have a high hit ratio while serving stale or incorrect data.

Correctness remains primary.

---

# 124. Cache Stampede

High-concurrency cache misses MAY cause cache stampede.

---

# 125. Stampede Protection

Strategies MAY include:

```text
Request Coalescing

Single-Flight Loading

Controlled Locking

TTL Jitter
```

---

# 126. Cache TTL

TTL MUST reflect data consistency requirements.

---

# 127. Cache Memory

Cache growth MUST be bounded.

---

# 128. Performance Test Types

The platform recognizes:

```text
Load

Stress

Spike

Soak

Capacity

Scalability

Microbenchmark
```

---

# 129. Load Testing

Load testing validates expected operating workload.

---

# 130. Stress Testing

Stress testing identifies behavior beyond expected capacity.

---

# 131. Spike Testing

Spike testing validates sudden load changes.

---

# 132. Soak Testing

Soak testing validates long-duration stability.

---

# 133. Soak Detection

Soak tests are particularly useful for identifying:

```text
Memory Leaks

Connection Leaks

Resource Accumulation

Performance Degradation
```

---

# 134. Capacity Testing

Capacity testing determines sustainable workload limits.

---

# 135. Scalability Testing

Scalability testing determines how throughput/latency change as resources are increased.

---

# 136. Linear Scaling

Applications MUST NOT assume linear scaling.

---

# 137. Load Testing Tools

Approved tools MAY include:

```text
k6

Gatling

JMeter
```

according to team/platform standards.

---

# 138. Tool Choice

The tool is secondary to the quality of the workload model and measurements.

---

# 139. JMH

JMH MUST be used for serious JVM microbenchmarking.

---

# 140. JMH Targets

Suitable examples:

```text
Parser

Serializer

Algorithm

Mapping Function

Calculation
```

---

# 141. JMH Is Not Load Testing

JMH does not replace service-level load testing.

---

# 142. Microbenchmark Isolation

Microbenchmark improvements may have negligible end-to-end impact.

---

# 143. Optimize System Bottleneck

Priority:

```text
System Bottleneck

before

Micro-Optimization
```

---

# 144. Warmup

JVM benchmarks MUST account for JIT warmup.

---

# 145. JVM Optimization

Benchmark methodology MUST account for:

```text
Warmup

Forks

Iterations

Dead-Code Elimination

Constant Folding
```

---

# 146. Performance Test Environment

Performance environments SHOULD be representative and controlled.

---

# 147. Environment Metadata

Results SHOULD record:

```text
Application Version

Java Version

CPU

Memory

Replica Count

Database Configuration

Dataset Size

Test Tool Version

Workload
```

---

# 148. Comparison Validity

Do not compare performance results from materially different environments without accounting for those differences.

---

# 149. Test Data Volume

Performance data volume SHOULD resemble expected production characteristics.

---

# 150. Database Cardinality

Query plans can change as cardinality changes.

---

# 151. Cold vs Warm Cache

Performance tests SHOULD distinguish cold-cache and warm-cache scenarios where relevant.

---

# 152. Ramp-Up

Load SHOULD normally ramp gradually unless spike behavior is specifically under test.

---

# 153. Coordinated Omission

Load-testing methodology SHOULD account for coordinated omission where the selected tool/model is susceptible to it.

---

# 154. Open vs Closed Workload

Performance engineers SHOULD understand whether the load model is:

```text
Open

or

Closed
```

---

# 155. Arrival Rate

Open workload models are often useful when production traffic arrives independently of server response time.

---

# 156. User Model

Closed models may be appropriate for interactive user workflows with think time.

---

# 157. Error Rate

Performance tests MUST evaluate errors, not latency alone.

---

# 158. Fast Failure

A system returning:

```text
500 in 10 ms
```

is not performant.

---

# 159. Correctness Under Load

Performance testing MUST validate functional correctness under load.

---

# 160. Saturation

Tests SHOULD identify the saturation point.

---

# 161. Saturation Indicators

Examples:

```text
CPU near sustained capacity

Connection pool pending growth

SQS queue backlog/oldest-message age growth

Queue growth

GC pressure

Timeout growth
```

---

# 162. Knee Point

Capacity analysis SHOULD identify where additional load causes disproportionate latency growth.

---

# 163. Graceful Degradation

Systems SHOULD degrade predictably near capacity.

---

# 164. Collapse

Architecture SHOULD avoid uncontrolled resource collapse.

---

# 165. Autoscaling

Autoscaling SHOULD use meaningful workload/resource signals.

---

# 166. CPU Autoscaling

CPU alone MAY be insufficient for I/O-bound services.

---

# 167. Alternative Signals

Depending on workload:

```text
Request Rate

Latency

Queue Depth

SQS Queue Backlog/Oldest-Message Age

Concurrency

Custom Business Metrics
```

---

# 168. Scale-Up Delay

Capacity planning MUST account for startup and scaling delay.

---

# 169. Minimum Capacity

Critical services SHOULD maintain sufficient minimum capacity for expected baseline traffic.

---

# 170. Headroom

Production capacity SHOULD include operational headroom.

---

# 171. Headroom Purpose

Headroom accommodates:

```text
Traffic Variation

Node Failure

Dependency Slowdown

Deployments

Recovery
```

---

# 172. Capacity Planning

Capacity planning SHOULD answer:

```text
What is current demand?

What is sustainable capacity?

What is expected growth?

What is the bottleneck?

What happens after a node fails?
```

---

# 173. Failure Capacity

Critical services SHOULD understand capacity under degraded topology.

Example:

```text
Normal: 4 replicas

Failure: 3 replicas

Can 3 replicas sustain expected peak?
```

---

# 174. Performance Regression Testing

Critical performance paths SHOULD have automated regression validation.

---

# 175. CI Performance Tests

Not every performance test belongs in every pull request.

---

# 176. PR Performance Gate

Fast stable benchmarks MAY execute in PR pipelines.

---

# 177. Scheduled Performance Suite

Larger tests MAY execute:

```text
Nightly

Weekly

Before Release
```

---

# 178. Regression Threshold

Performance gates MUST allow enough tolerance to avoid meaningless environmental noise.

---

# 179. Statistical Comparison

Performance regression decisions SHOULD consider repeated measurements rather than a single sample where variability is significant.

---

# 180. CI Failure

A performance gate SHOULD fail only on a meaningful regression against an approved threshold/baseline.

---

# 181. Observability Correlation

Load-test results SHOULD correlate application metrics with test-client metrics.

---

# 182. Required Signals

During critical performance tests collect:

```text
Latency

Throughput

Errors

CPU

Memory

GC

Threads

Connection Pools

Database Metrics

SQS Queue Backlog/Oldest-Message Age

Redis Metrics
```

as applicable.

---

# 183. Distributed Tracing

Tracing MAY be used selectively during performance diagnosis.

---

# 184. Tracing Overhead

Observability overhead itself SHOULD be considered when interpreting performance results.

---

# 185. Logging Overhead

Excessive logging can materially affect performance.

---

# 186. Hot-Path Logging

High-volume hot paths SHOULD avoid excessive INFO logging.

---

# 187. Debug Logging

Debug/trace logging MUST NOT be enabled indiscriminately in production performance tests.

---

# 188. Exception Cost

Repeated exceptions on normal control paths can create CPU and allocation overhead.

---

# 189. Exceptions as Flow Control

Exceptions SHOULD NOT be used for expected high-frequency control flow.

---

# 190. Performance and Clean Code

Performance optimization MUST preserve maintainability unless the measured benefit justifies additional complexity.

---

# 191. Optimization Documentation

Non-obvious performance optimizations SHOULD document:

```text
Problem

Measurement

Decision

Trade-Off

Expected Benefit
```

---

# 192. Performance ADR

Major architectural optimizations SHOULD receive an ADR when they materially change system structure or operational characteristics.

---

# 193. Premature Optimization

This is prohibited:

```text
Assume bottleneck
      |
      v
Add complexity
      |
      v
Never measure
```

---

# 194. Correct Process

Use:

```text
MEASURE
   |
   v
PROFILE
   |
   v
HYPOTHESIS
   |
   v
CHANGE
   |
   v
BENCHMARK
   |
   v
COMPARE
```

---

# 195. Performance Testing and Security

Performance tests MUST NOT bypass essential security behavior unless the specific test intentionally isolates another component.

---

# 196. Authentication Cost

Authentication/authorization overhead SHOULD be included in representative endpoint load tests.

---

# 197. Rate Limiting

Rate-limit behavior SHOULD be capacity tested where applicable.

---

# 198. Denial-of-Service Resilience

Security and performance engineering SHOULD jointly evaluate resource-exhaustion risks.

---

# 199. Large Payload Protection

APIs SHOULD enforce appropriate payload limits.

---

# 200. Pagination Limits

APIs SHOULD enforce maximum page/batch sizes where unbounded requests could exhaust resources.

---

# 201. Batch Size

Batch sizes MUST be bounded.

---

# 202. Batch Trade-Off

Larger batches can improve throughput but increase:

```text
Memory

Transaction Duration

Latency

Failure Blast Radius
```

---

# 203. Batch Tuning

Batch size SHOULD be measured rather than guessed.

---

# 204. Outbox Performance

Transactional Outbox implementations SHOULD monitor:

```text
Pending Events

Dispatch Rate

Retry Count

Oldest Pending Age

Failure Rate
```

---

# 205. Outbox Backlog

A growing outbox backlog is a capacity signal.

---

# 206. Dispatcher Batch Size

Dispatcher batch size SHOULD balance:

```text
Database Load

Network Efficiency

Memory

Recovery Speed
```

---

# 207. Dispatcher Concurrency

Dispatcher concurrency MUST be bounded.

---

# 208. Idempotency Performance

Idempotency mechanisms SHOULD be efficient enough for expected duplicate rates and throughput.

---

# 209. Locking

High-contention locks SHOULD be identified through profiling.

---

# 210. synchronized

`synchronized` is not prohibited.

Its contention characteristics MUST be understood on hot paths.

---

# 211. Virtual Thread Pinning

Virtual Thread workloads SHOULD be evaluated for operations that may pin carrier threads where material to the Java runtime/version in use.

---

# 212. Context Propagation

Context-propagation mechanisms SHOULD be measured when used heavily in concurrent workloads.

---

# 213. Performance Fitness Functions

The following SHOULD be automated where practical:

```text
[ ] Remote clients have bounded timeouts

[ ] Connection pools have explicit limits

[ ] Batch/page sizes are bounded

[ ] Critical JMH benchmarks do not regress

[ ] Critical API p95/p99 remain within budget

[ ] Error rate remains within threshold

[ ] Critical SQL regressions are detected

[ ] SQS queue backlog/oldest-message age remains within recovery objective

[ ] Memory remains below safe envelope

[ ] No unbounded executor configuration exists
```

---

# 214. Performance Scorecard

Critical services SHOULD expose a performance scorecard.

Example:

```text
API p95              PASS

API p99              PASS

Error Rate           PASS

CPU Headroom         PASS

Memory Headroom      PASS

DB Pool Saturation   PASS

SQS Queue Backlog/Oldest-Message Age            WARNING

Redis Hit Ratio      PASS

Capacity Test        PASS
```

---

# 215. Performance Review Checklist

A material performance-sensitive change SHOULD evaluate:

```text
[ ] What workload is affected?

[ ] What is the baseline?

[ ] Is this CPU or I/O bound?

[ ] Are remote calls added?

[ ] Are database queries added?

[ ] Is there an N+1 risk?

[ ] Does payload size increase?

[ ] Does transaction duration increase?

[ ] Does concurrency increase?

[ ] Is concurrency bounded?

[ ] Does connection-pool demand increase?

[ ] Does SQS processing time change?

[ ] Does cache behavior change?

[ ] Is memory allocation materially affected?

[ ] Has performance been measured?
```

---

# 216. Enterprise Performance Gate

A critical service is not considered performance-compliant when applicable conditions include:

```text
[ ] No defined performance objective

[ ] Unbounded remote timeout

[ ] Unbounded concurrency

[ ] Unbounded batch/page size

[ ] Known material N+1

[ ] Database pool repeatedly exhausted

[ ] HTTP pool repeatedly exhausted

[ ] Uncontrolled SQS queue backlog/oldest-message age growth

[ ] Memory exceeds safe container envelope

[ ] Significant unexplained p95/p99 regression

[ ] Load test produces unacceptable error rate

[ ] Critical capacity unknown

[ ] Performance optimization has no supporting measurement
```

---

# 217. Anti-Patterns

The following are prohibited or strongly discouraged:

- optimizing before measuring
- average latency as the only latency metric
- arbitrary JVM flags copied from other systems
- assuming Virtual Threads remove downstream limits
- unbounded concurrency
- unbounded executors
- infinite queues
- increasing database pool size blindly
- using EAGER as generic N+1 remediation
- ignoring generated Hibernate SQL
- remote calls inside unnecessarily long transactions
- per-item HTTP calls when batch endpoints exist
- unbounded parallel HTTP fan-out
- retry storms
- cache without explicit consistency semantics
- cache growth without bounds
- assuming more SQS consumers always increase throughput
- performance tests using unrealistic tiny datasets
- treating unit-test timing as a benchmark
- JVM microbenchmarks without JMH
- performance comparison across incomparable environments
- ignoring errors during load testing
- ignoring tail latency
- load tests without workload models
- performance optimization that materially increases complexity without measured benefit
- capacity planning based solely on CPU utilization

---

# 218. Positive Consequences

The decision provides:

- predictable latency
- measurable capacity
- improved JVM diagnostics
- safer concurrency
- better database efficiency
- controlled connection pools
- reduced N+1 behavior
- improved SQS capacity management
- measurable Redis effectiveness
- early performance regression detection
- evidence-based optimization
- improved production stability

---

# 219. Negative Consequences

The decision introduces:

- performance-test infrastructure
- profiling expertise requirements
- additional observability
- capacity-test environments
- CI execution cost
- baseline maintenance
- more sophisticated workload modeling

These costs are accepted because performance defects in distributed enterprise systems are often significantly more expensive to diagnose after production saturation.

---

# 220. Neutral Consequences

The decision also means:

- not every service requires identical load tests
- not every optimization belongs in CI
- not every high CPU percentage indicates a defect
- not every allocation requires optimization
- not every query requires a new index
- not every workload benefits from Virtual Threads
- capacity remains dependent on infrastructure and workload

---

# 221. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Unrealistic workload | High | Medium | Production-informed workload model |
| Noisy benchmark | Medium | High | Repeated controlled runs |
| Premature optimization | High | Medium | Measure-first rule |
| JVM over-tuning | High | Medium | Prefer defaults |
| DB over-connection | High | Medium | Pool/database metrics |
| Excessive concurrency | Critical | Medium | Explicit bounds |
| SQS backlog | High | Medium | Lag monitoring |
| Memory exhaustion | Critical | Medium | Memory envelope |
| Performance CI instability | Medium | Medium | Statistical tolerance |
| Misleading averages | High | High | p95/p99 |
| Cache inconsistency | High | Medium | Correctness-first policy |

---

# 222. Implementation Guidance

The following rules are mandatory:

1. Critical services must have measurable performance objectives.
2. Tail latency must be considered through percentiles such as p95 and p99.
3. Performance optimization must be measurement-driven.
4. JFR/JMC should be preferred for JVM profiling.
5. CPU-bound and I/O-bound workloads must be distinguished.
6. Virtual Threads must not be interpreted as unlimited downstream concurrency.
7. Concurrency must remain bounded.
8. JVM memory planning must include heap and non-heap/native memory.
9. GC tuning must be workload-driven.
10. Arbitrary JVM tuning flags must not be copied without evidence.
11. PostgreSQL query performance must use real execution-plan analysis where needed.
12. Material N+1 behavior must be corrected.
13. Large queries must remain bounded.
14. Database connection pools must have explicit limits.
15. HTTP connection pools must have explicit limits where applicable.
16. Remote integrations must have bounded timeouts.
17. Retry policies must avoid load amplification.
18. Batch APIs should be preferred over N+1 remote calls where available.
19. Parallel remote calls must have explicit concurrency limits.
20. SQS queue backlog/oldest-message age must be monitored.
21. Critical consumers must have understood backlog-recovery capacity.
22. Redis caching must have measurable effectiveness and bounded memory behavior.
23. Load tests must use documented workload models.
24. JMH must be used for serious JVM microbenchmarks.
25. Performance results must record environment and workload metadata.
26. Tests must evaluate errors as well as latency.
27. Critical systems must understand saturation behavior.
28. Production capacity should maintain operational headroom.
29. Performance regression tests should be automated for critical paths.
30. Non-obvious performance optimizations should document supporting measurements.

---

# 223. Validation

This ADR will be validated through:

- Java Flight Recorder
- Java Mission Control
- JVM metrics
- heap analysis
- Native Memory Tracking where required
- PostgreSQL EXPLAIN / EXPLAIN ANALYZE
- connection-pool metrics
- SQS metrics
- Redis metrics
- JMH
- k6
- Gatling
- JMeter
- CI/CD
- performance dashboards
- capacity tests
- architecture reviews

---

# 224. Success Criteria

The decision is successful when:

- critical APIs maintain defined p95/p99 objectives
- saturation points are understood
- production capacity has measurable headroom
- database pool exhaustion decreases
- HTTP pool exhaustion decreases
- N+1 regressions decrease
- SQS queue backlog/oldest-message age is predictable and recoverable
- memory behavior remains within the defined envelope
- performance regressions are detected before production
- optimizations are supported by measurements
- Virtual Thread adoption does not overload downstream dependencies
- performance incidents become easier to diagnose

---

# 225. Alternatives Rejected

## 225.1 Optimize Only After Production Problems

Rejected because performance is an architectural quality attribute.

---

## 225.2 Average Latency Only

Rejected because averages hide tail latency.

---

## 225.3 Maximum Concurrency

Rejected because downstream systems remain finite.

---

## 225.4 Maximum Database Pool Size

Rejected because excessive connections can reduce database performance.

---

## 225.5 JVM Tuning First

Rejected because application, database or external dependencies are frequently the actual bottleneck.

---

## 225.6 Unit-Test Timing as Performance Testing

Rejected because JVM optimization requires proper benchmark methodology.

---

# 226. Related Decisions

This ADR extends and implements:

- ADR-009: Amazon SQS Integration Events
- ADR-010: Redis Distributed Caching
- ADR-016: Application Resilience
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-039: CI/CD, Release and Deployment Governance Standards
- ADR-040: Observability and Production Diagnostics Standards
- ADR-042: Architecture Fitness Functions
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard

---

# 227. References

- Java 21 Documentation
- Java Flight Recorder
- Java Mission Control
- Java Virtual Threads
- Java Native Memory Tracking
- JMH
- PostgreSQL Documentation
- Amazon SQS Documentation
- Redis Documentation
- Little's Law
- k6
- Gatling
- Apache JMeter
- USE Method
- RED Method

---

# 228. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial enterprise performance engineering and capacity baseline |

---

# 229. Decision Summary

The performance engineering lifecycle becomes:

```text
REQUIREMENT
     |
     v
PERFORMANCE BUDGET
     |
     v
BASELINE
     |
     v
LOAD TEST
     |
     v
OBSERVE
     |
     v
PROFILE
     |
     v
BOTTLENECK
     |
     v
OPTIMIZE
     |
     v
MEASURE AGAIN
     |
     v
REGRESSION GATE
```

For latency:

```text
AVERAGE
   |
   v
NOT ENOUGH

p50 + p95 + p99
   |
   v
TAIL VISIBILITY
```

For Virtual Threads:

```text
10,000 REQUESTS
       |
       v
VIRTUAL THREADS
       |
       v
BOUNDED CONCURRENCY
       |
       v
50 HTTP CONNECTIONS
       |
       v
REMOTE SERVICE
```

rather than:

```text
10,000 Virtual Threads
       =
10,000 simultaneous downstream calls
```

For database performance:

```text
APPLICATION
     |
     v
HIBERNATE
     |
     v
SQL
     |
     v
EXPLAIN ANALYZE
     |
     v
REAL EXECUTION PLAN
     |
     v
OPTIMIZATION
```

For N+1 integrations:

```text
100 ITEMS
   |
   +--> 100 DATABASE QUERIES
   |
   +--> 100 HTTP REQUESTS

          BAD
```

Prefer:

```text
100 ITEMS
   |
   +--> BATCH QUERY
   |
   +--> BATCH API

          BETTER
```

when supported by semantics and contracts.

For SQS:

```text
EVENT ARRIVAL RATE
       |
       v
PROCESSING RATE
       |
       +------------------+
       |                  |
Arrival <= Processing   Arrival > Processing
       |                  |
       v                  v
     STABLE          LAG GROWS
```

For memory:

```text
CONTAINER MEMORY
       |
       +--> Heap
       |
       +--> Metaspace
       |
       +--> Thread Stacks
       |
       +--> Direct Memory
       |
       +--> Native Memory
```

For optimization:

```text
MEASURE
   |
   v
PROFILE
   |
   v
FIND BOTTLENECK
   |
   v
CHANGE ONE THING
   |
   v
MEASURE AGAIN
```

For capacity:

```text
EXPECTED PEAK
      +
FAILURE SCENARIO
      +
GROWTH
      +
HEADROOM
      =
REQUIRED CAPACITY
```

For performance gates:

```text
VERSION N
   |
   v
BASELINE

VERSION N+1
   |
   v
PERFORMANCE TEST
   |
   v
COMPARE
   |
   +------ PASS ------> RELEASE
   |
   +------ FAIL ------> INVESTIGATE
```

The complete performance equation is:

```text
PERFORMANCE OBJECTIVES
        +
REPRESENTATIVE WORKLOAD
        +
OBSERVABILITY
        +
JVM PROFILING
        +
DATABASE ANALYSIS
        +
BOUNDED CONCURRENCY
        +
CAPACITY PLANNING
        +
LOAD TESTING
        +
REGRESSION CONTROL
        =
PREDICTABLE PRODUCTION PERFORMANCE
```

The governing principle is:

```text
Performance engineering is
not the art of making code
look faster.

It is the discipline of
measuring where time and
resources are actually spent.

Virtual Threads do not create
database connections.

Retries do not create capacity.

Queues do not create throughput.

Caches do not create correctness.

More parallelism is not
automatically more performance.

Measure the workload.

Find the bottleneck.

Change the smallest thing
that materially improves it.

Then measure again.
```
