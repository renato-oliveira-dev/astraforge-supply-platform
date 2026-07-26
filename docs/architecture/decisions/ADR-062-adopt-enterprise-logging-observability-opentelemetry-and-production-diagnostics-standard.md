# ADR-062: Adopt Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-062 |
| Title | Adopt Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard |
| Status | Accepted |
| Date | 2026-07-25 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Logging, Metrics, Tracing, OpenTelemetry, Production Diagnostics |
| Related Work Items | SLF4J, Logback, Micrometer, OpenTelemetry, Actuator, Circuit Breaker, Kubernetes |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Distributed systems cannot be operated reliably using application logs alone.

A production request may cross:

```text
CLIENT
   |
   v
LOAD BALANCER
   |
   v
CART SERVICE
   |
   +--> CUSTOMERS SERVICE
   +--> PRODUCTS SERVICE
   +--> ORDERS SERVICE
   |
   v
KAFKA / SQS
   |
   v
BACKGROUND PROCESSING
   |
   v
POSTGRESQL / REDIS
```

A failure can therefore originate from:

```text
Application Code

Database

Connection Pool

Redis

Kafka

SQS

HTTP Dependency

Circuit Breaker

JVM

Garbage Collector

Container

Kubernetes

AWS Infrastructure
```

Production diagnostics require correlation between these layers.

---

# 2. Problem Statement

The organization requires standards covering:

- structured logging
- log levels
- SLF4J
- Logback
- correlationId
- requestId
- traceId
- MDC
- exception logging
- log-or-rethrow
- sensitive-data masking
- PII
- secrets
- log injection
- metrics
- Micrometer
- RED
- USE
- JVM metrics
- HikariCP
- Redis
- Kafka
- SQS
- Circuit Breaker
- HTTP clients
- Kubernetes
- distributed tracing
- OpenTelemetry
- sampling
- metric cardinality
- dashboards
- alerting
- SLI
- SLO
- error budgets
- health indicators
- production troubleshooting
- diagnostic endpoints
- telemetry cost

---

# 3. Decision Drivers

Primary drivers are:

1. production diagnosability
2. incident-response speed
3. reliability
4. security
5. performance visibility
6. distributed correlation
7. controlled telemetry cost
8. low cardinality
9. operational consistency
10. actionable alerting
11. measurable SLOs
12. minimal instrumentation overhead

---

# 4. Decision

Production services MUST expose sufficient telemetry to answer:

```text
WHAT FAILED?

WHERE DID IT FAIL?

WHEN DID IT FAIL?

HOW MANY REQUESTS ARE AFFECTED?

HOW LONG HAS IT BEEN FAILING?

WHAT DEPENDENCY IS INVOLVED?

WHAT CHANGED?

IS THE SYSTEM RECOVERING?
```

The observability model is:

```text
                   APPLICATION
                       |
          +------------+------------+
          |            |            |
          v            v            v
        LOGS         METRICS      TRACES
          |            |            |
          +------------+------------+
                       |
                       v
                CORRELATED VIEW
                       |
                       v
               PRODUCTION SIGNAL
```

Logs, metrics and traces are complementary.

No single signal replaces the others.

---

# 5. Fundamental Principle

The governing principle is:

```text
Telemetry must answer
operational questions.

Do not collect data merely
because instrumentation can.
```

---

# 6. Structured Logging

Production logs SHOULD use a machine-readable structured format where supported by the enterprise logging platform.

Preferred representation:

```json
{
  "timestamp": "...",
  "level": "ERROR",
  "service": "orders-service",
  "traceId": "...",
  "correlationId": "...",
  "event": "order.creation.failed",
  "message": "Unable to create order"
}
```

---

# 7. Structured Fields

Frequently queried information SHOULD be represented as structured fields rather than embedded inside free-form messages.

---

# 8. Human Readability

Structured logging MUST remain understandable to engineers during incident investigation.

---

# 9. Log Timestamp

Production logs MUST contain timestamps.

---

# 10. Timestamp Standard

Timestamps SHOULD use a consistent machine-readable format such as ISO-8601.

---

# 11. Time Zone

Distributed production telemetry SHOULD normally use UTC for correlation.

---

# 12. Service Identification

Every log record SHOULD identify the originating service through logging-platform metadata or structured fields.

---

# 13. Environment

Environment SHOULD be available as telemetry metadata.

---

# 14. Instance Metadata

Pod/instance information MAY be available through infrastructure enrichment rather than repeated manually in every application message.

---

# 15. Log Levels

Log levels MUST have consistent semantics.

---

# 16. ERROR

`ERROR` indicates a failure requiring operational or engineering attention.

Examples:

```text
Business operation unexpectedly failed

Database unavailable

Required remote dependency failed

Message permanently failed

Invariant violated
```

---

# 17. WARN

`WARN` indicates abnormal behavior from which the application can continue.

Examples:

```text
Fallback activated

Deprecated configuration used

Recoverable inconsistency detected
```

---

# 18. INFO

`INFO` represents meaningful lifecycle or business-operational events.

Examples:

```text
Application started

Batch execution completed

Circuit breaker changed state

Controlled business process completed
```

---

# 19. DEBUG

`DEBUG` contains diagnostic information useful during development or targeted investigation.

---

# 20. TRACE

`TRACE` contains highly detailed diagnostic information and MUST NOT normally be enabled globally in production.

---

# 21. ERROR Is Not Business Validation

Expected client/business validation failures SHOULD NOT automatically produce `ERROR` logs.

---

# 22. 4xx Noise

Routine expected `4xx` responses SHOULD avoid noisy stack traces unless they indicate abuse, integration defects or unexpected behavior.

---

# 23. Log Volume

Log level selection MUST account for production volume.

---

# 24. Logging in Loops

High-frequency loops MUST NOT emit unrestricted INFO/WARN logs per element.

---

# 25. Parameterized Logging

Use parameterized logging:

```java
log.info("Order {} processed successfully", orderId);
```

rather than eager string concatenation.

---

# 26. Expensive Logging

Expensive diagnostic value generation SHOULD be guarded when necessary.

---

# 27. Exception Logging

Exceptions MUST preserve sufficient diagnostic context.

---

# 28. Stack Trace

Unexpected failures SHOULD normally preserve the stack trace at the appropriate boundary.

---

# 29. Message-Only Logging

This is insufficient for unexpected exceptions:

```java
log.error("Error: {}", ex.getMessage());
```

because the stack trace is lost.

---

# 30. Preferred Exception Logging

Prefer:

```java
log.error("Unable to process order {}", orderId, ex);
```

at the appropriate logging boundary.

---

# 31. Log or Rethrow

An exception MUST NOT be repeatedly logged at every architectural layer.

The preferred rule is:

```text
HANDLE
   |
   +--> LOG WHEN THIS IS THE
   |    RESPONSIBLE BOUNDARY
   |
   +--> OR RETHROW
```

---

# 32. Log and Rethrow

This pattern SHOULD be avoided:

```java
catch (Exception ex) {
    log.error("Failure", ex);
    throw ex;
}
```

when an upper boundary will log the same exception again.

---

# 33. Duplicate Stack Traces

One failure SHOULD NOT normally generate identical stack traces from:

```text
Repository

Service

Controller

GlobalExceptionHandler
```

---

# 34. Context Enrichment

Intermediate layers MAY enrich an exception with useful domain context and rethrow it without logging.

---

# 35. Sonar Compliance

Exception handling MUST comply with applicable Sonar rules, including avoiding catch blocks that neither meaningfully handle nor appropriately propagate exceptions.

---

# 36. Catch Exception

Broad:

```java
catch (Exception ex)
```

SHOULD be avoided unless the architectural boundary genuinely requires it.

---

# 37. Throwable

Application code MUST NOT routinely catch:

```java
Throwable
```

because JVM-level errors should generally propagate.

---

# 38. Sensitive Logging

Logs MUST NOT expose:

```text
Passwords

Access Tokens

Refresh Tokens

Authorization Headers

Private Keys

Secret Keys

Session Secrets
```

---

# 39. Authorization Header

Never log complete:

```text
Authorization: Bearer ...
```

values.

---

# 40. PII

Personally identifiable information MUST be logged only when justified by business and security requirements.

---

# 41. Data Minimization

Prefer identifiers over complete customer/user objects.

---

# 42. Payload Logging

Complete request/response payload logging SHOULD NOT be the default in production.

---

# 43. Sensitive Payload

Sensitive payloads MUST be masked or omitted.

---

# 44. Masking

Masking MUST occur before sensitive data reaches the logging sink.

---

# 45. Mask Failure

Masking failure MUST NOT result in exposing the original secret.

---

# 46. Log Injection

Untrusted input MUST be normalized or structured safely to reduce log-injection risks.

---

# 47. CRLF

External values containing:

```text
\r

\n
```

MUST NOT be allowed to forge additional log records in plain-text logging formats.

---

# 48. Sanitization Scope

Security sanitization SHOULD be applied at the logging boundary rather than corrupting legitimate business values throughout the application.

---

# 49. Business Data Preservation

Characters such as:

```text
&

<

>
```

MUST NOT be globally transformed merely to make logs safe when doing so changes valid business data.

---

# 50. Correlation

Distributed requests MUST be correlatable across service boundaries.

---

# 51. traceId

When distributed tracing is enabled, `traceId` is the preferred cross-service trace correlation identifier.

---

# 52. correlationId

A business/request correlation identifier MAY coexist with `traceId` when it has independent operational meaning.

---

# 53. requestId

A request-specific identifier MAY identify an individual inbound request.

---

# 54. Identifier Semantics

Identifiers MUST have defined semantics.

Avoid creating:

```text
requestId

correlationId

traceId

transactionId

operationId
```

with no clear distinction.

---

# 55. Propagation

Required correlation identifiers MUST propagate through:

```text
HTTP

Kafka

SQS

Async Execution
```

where appropriate.

---

# 56. HTTP Headers

Approved correlation headers SHOULD be propagated by standardized client/server filters.

---

# 57. MDC

SLF4J MDC MAY be used to enrich logs with request-scoped identifiers.

---

# 58. MDC Cleanup

MDC values MUST be removed/restored when execution context ends.

---

# 59. Thread Pool Leakage

Failure to clean MDC can cause one request's identifiers to appear in another request's logs.

---

# 60. Async MDC

Thread switching requires explicit context propagation when MDC-based correlation is required.

---

# 61. Virtual Threads

Virtual Threads do not eliminate the need to reason about logging/security/request context propagation.

---

# 62. Delegating Context

Executors MAY require wrappers that propagate:

```text
SecurityContext

Request Context

MDC
```

where applicable.

---

# 63. Metrics

Metrics MUST represent aggregate operational behavior.

---

# 64. RED Method

Request-driven services SHOULD monitor RED:

```text
RATE

ERRORS

DURATION
```

---

# 65. Rate

Measure request/operation throughput.

---

# 66. Errors

Measure unsuccessful operations using meaningful failure classification.

---

# 67. Duration

Measure latency distributions.

---

# 68. Percentiles

Latency SHOULD be analyzed using appropriate percentiles such as:

```text
p50

p95

p99
```

rather than averages alone.

---

# 69. Average Latency

Average latency can hide severe tail latency.

---

# 70. USE Method

Infrastructure/resources SHOULD consider USE:

```text
UTILIZATION

SATURATION

ERRORS
```

---

# 71. CPU

Monitor:

```text
CPU Utilization

CPU Throttling
```

---

# 72. Memory

Monitor:

```text
Container Memory

Heap

Non-Heap

GC
```

---

# 73. JVM Metrics

Applicable Java services SHOULD expose:

```text
Heap Usage

Metaspace

GC Pause

GC Allocation

Threads

Class Loading
```

---

# 74. GC

GC behavior SHOULD be correlated with latency and memory pressure.

---

# 75. HikariCP

Database-backed services SHOULD monitor:

```text
Active Connections

Idle Connections

Pending Connections

Connection Acquisition Time

Timeouts
```

---

# 76. Pool Saturation

A saturated HikariCP pool is an operational signal and SHOULD be observable.

---

# 77. Database Metrics

Application telemetry SHOULD expose relevant query/database symptoms without creating unbounded per-query metrics.

---

# 78. Redis Metrics

Redis integrations SHOULD expose applicable:

```text
Hits

Misses

Failures

Fallback Hits

Latency
```

---

# 79. Kafka Metrics

Kafka consumers SHOULD expose:

```text
Consumer Lag

Processing Rate

Processing Failures

Retries

DLQ Events
```

where applicable.

---

# 80. Kafka Lag

Consumer lag is a critical backlog signal.

---

# 81. SQS Metrics

SQS workers SHOULD monitor applicable:

```text
Visible Messages

Age of Oldest Message

Processing Rate

Processing Failure

DLQ Volume
```

---

# 82. Circuit Breaker

Circuit Breaker metrics SHOULD expose:

```text
CLOSED

OPEN

HALF_OPEN

Failure Rate

Slow Call Rate

Rejected Calls
```

---

# 83. Circuit Breaker Transition

State transitions SHOULD produce operationally useful events.

---

# 84. HTTP Client Metrics

Important outbound integrations SHOULD expose:

```text
Request Count

Latency

Error Rate

Timeout Rate
```

---

# 85. Integration Name

Metrics SHOULD identify bounded logical dependency names.

Example:

```text
customers-service

products-service

workflows-service
```

---

# 86. Dynamic URL Labels

Full URLs containing IDs MUST NOT be used as metric labels.

---

# 87. Metric Cardinality

Metric label cardinality MUST remain bounded.

---

# 88. Forbidden Labels

Avoid unrestricted metric labels containing:

```text
userId

customerId

orderId

UUID

Email

RequestId

TraceId

Raw URL
```

---

# 89. Cardinality Explosion

This is prohibited:

```text
orders_processed{
    orderId="<unique UUID>"
}
```

---

# 90. Bounded Labels

Prefer:

```text
method

status

operation

dependency

result

exception_category
```

when their value sets remain bounded.

---

# 91. Micrometer

Micrometer SHOULD be the standard application metrics facade for applicable Spring Boot services.

---

# 92. Custom Metrics

Custom metrics MUST answer a specific operational or business question.

---

# 93. Metric Naming

Custom metric names SHOULD follow enterprise naming conventions.

---

# 94. Counter

Counters SHOULD represent monotonically increasing event counts.

---

# 95. Gauge

Gauges SHOULD represent current state.

---

# 96. Timer

Timers SHOULD represent operation latency and count.

---

# 97. Distribution Summary

Distribution summaries MAY represent non-time distributions such as payload sizes.

---

# 98. Duplicate Metrics

Applications SHOULD NOT create custom metrics duplicating existing reliable framework/platform metrics without justification.

---

# 99. Distributed Tracing

Distributed tracing MAY be enabled where request-path diagnostics justify its operational cost.

---

# 100. OpenTelemetry

OpenTelemetry is the preferred vendor-neutral tracing/telemetry standard where distributed tracing is required.

---

# 101. Tracing Is Not Mandatory Everywhere

OpenTelemetry tracing MUST NOT be enabled automatically for every service merely because the library is available.

---

# 102. Enablement Criteria

Tracing SHOULD be enabled when it materially improves diagnosis of:

```text
Multi-Service Request Paths

High-Value Business Transactions

Complex Integration Failures

Latency Attribution
```

---

# 103. Instrumentation Cost

Tracing introduces:

```text
CPU Overhead

Memory Overhead

Network Traffic

Storage Cost

Backend Cost

Operational Complexity
```

---

# 104. Existing Platform

If the enterprise platform already provides adequate correlation and metrics, additional application-level tracing SHOULD be justified before introduction.

---

# 105. Duplicate Telemetry

Applications MUST avoid accidentally exporting duplicate telemetry through multiple overlapping agents/libraries.

---

# 106. Auto-Instrumentation

OpenTelemetry auto-instrumentation MAY be used where validated for compatibility and overhead.

---

# 107. Manual Instrumentation

Manual spans SHOULD be limited to meaningful operations not already represented adequately.

---

# 108. Span Explosion

Do not create spans for trivial methods.

---

# 109. Span Boundary

Useful span boundaries often correspond to:

```text
Inbound Request

Remote HTTP Call

Database Operation

Kafka Publish/Consume

SQS Publish/Consume

Major Business Operation
```

---

# 110. Span Attribute Cardinality

Span attributes SHOULD avoid unnecessary sensitive/high-cardinality data.

---

# 111. Trace Context

Standard trace context SHOULD be propagated across supported protocols.

---

# 112. W3C Trace Context

W3C Trace Context SHOULD be preferred where compatible with the platform.

---

# 113. Async Messaging

Tracing across Kafka/SQS MUST respect asynchronous causality rather than pretending asynchronous processing is a synchronous call stack.

---

# 114. Sampling

Production tracing MUST define a sampling strategy.

---

# 115. 100% Sampling

100% production trace sampling SHOULD NOT be assumed appropriate for high-volume services.

---

# 116. Head Sampling

Head-based sampling MAY control telemetry volume early in trace creation.

---

# 117. Tail Sampling

Tail-based sampling MAY preserve traces based on outcomes such as:

```text
Errors

High Latency

Selected Operations
```

when supported.

---

# 118. Error Traces

Sampling strategy SHOULD retain sufficient failed/slow traces for diagnostics.

---

# 119. Sampling Consistency

Sampling SHOULD avoid fragmented distributed traces where practical.

---

# 120. Sampling Configuration

Sampling rates MUST be externally configurable.

---

# 121. Trace Storage

Trace retention MUST reflect diagnostic value, security and cost.

---

# 122. Trace Sensitive Data

Trace attributes MUST follow the same sensitive-data restrictions as logs.

---

# 123. OpenTelemetry Failure

Telemetry exporter failure MUST NOT normally fail business requests.

---

# 124. Telemetry Backpressure

Telemetry export MUST be bounded to avoid exhausting application resources.

---

# 125. Export Timeout

Telemetry exporters MUST have bounded timeouts.

---

# 126. Telemetry Retry

Telemetry retries MUST be bounded.

---

# 127. Telemetry Outage

An observability-backend outage MUST NOT become an application outage.

---

# 128. Disable Unneeded Telemetry

Unused exporters/instrumentation SHOULD be disabled.

---

# 129. Prometheus

Prometheus-compatible metrics MAY be exposed when required by the enterprise monitoring platform.

---

# 130. Metrics Endpoint Security

Metrics endpoints MUST follow infrastructure/network security policy.

---

# 131. Public Metrics Endpoint

Internal metrics SHOULD NOT normally be publicly Internet-accessible.

---

# 132. Actuator

Spring Boot Actuator SHOULD provide standardized operational endpoints where appropriate.

---

# 133. Endpoint Exposure

Only required Actuator endpoints SHOULD be exposed.

---

# 134. Sensitive Actuator Endpoints

Endpoints exposing:

```text
Environment

Beans

Configuration

Heap

Thread Details
```

MUST be restricted or disabled according to security policy.

---

# 135. Health

Health endpoints MUST remain lightweight.

---

# 136. Liveness

Liveness MUST answer whether restart is appropriate.

---

# 137. Readiness

Readiness MUST answer whether the instance can safely receive traffic.

---

# 138. Health Details

Detailed health information SHOULD NOT be exposed anonymously.

---

# 139. Health Dependency Storm

Health checks MUST NOT generate excessive load against downstream dependencies.

---

# 140. Dashboards

Dashboards SHOULD be designed around operational questions.

---

# 141. Service Dashboard

A service dashboard SHOULD normally show:

```text
Traffic

Error Rate

Latency

CPU

Memory

Replicas

Restarts

Dependency Health
```

---

# 142. Dependency Dashboard

Critical dependency dashboards SHOULD expose saturation and failures.

---

# 143. Business Dashboard

Critical business flows MAY expose aggregate business-operational metrics.

---

# 144. Dashboard Is Not Alert

A dashboard requiring someone to watch it continuously is not an alerting strategy.

---

# 145. Alerting

Alerts MUST indicate conditions requiring human or automated action.

---

# 146. Actionable Alert

Every production alert SHOULD answer:

```text
WHAT IS WRONG?

WHAT IS THE IMPACT?

WHAT SHOULD BE CHECKED?
```

---

# 147. Symptom-Based Alert

Alerts SHOULD prefer user-impact symptoms over low-level causes where possible.

---

# 148. Example

Prefer alerting on:

```text
Sustained elevated checkout error rate
```

rather than only:

```text
CPU > 70%
```

---

# 149. Cause Alerts

Cause-level alerts MAY complement symptom alerts for critical infrastructure.

---

# 150. Alert Fatigue

Low-value/noisy alerts MUST be tuned or removed.

---

# 151. Warning Storm

Repeated transient warnings SHOULD NOT page engineers without demonstrated user impact.

---

# 152. Threshold

Alert thresholds MUST be based on operational requirements and observed baselines.

---

# 153. Duration

Alerts SHOULD normally require sustained failure rather than a single transient sample.

---

# 154. Severity

Alert severity MUST reflect impact.

---

# 155. SLI

Service Level Indicators measure user-relevant service behavior.

---

# 156. Availability SLI

Example:

```text
successful eligible requests
----------------------------
total eligible requests
```

---

# 157. Latency SLI

Example:

```text
percentage of eligible requests
completed within target latency
```

---

# 158. SLO

Service Level Objectives define target SLI performance.

---

# 159. Example SLO

Conceptually:

```text
99.9% successful eligible requests
over the defined measurement window
```

---

# 160. SLO Scope

SLOs MUST define:

```text
Service

Operation

Eligible Traffic

Target

Measurement Window
```

---

# 161. Error Budget

Error budget represents the allowed unreliability within the SLO.

---

# 162. Error Budget Use

Error budgets SHOULD help balance:

```text
Reliability

Delivery Velocity

Risk
```

---

# 163. SLO Is Not 100%

A 100% SLO is generally inappropriate for ordinary distributed services unless business requirements genuinely demand it.

---

# 164. Dependency SLO

Application SLOs MUST consider dependency reliability.

---

# 165. Composite Availability

A synchronous chain of dependencies generally has lower end-to-end availability than each dependency considered independently.

---

# 166. Burn Rate

Error-budget burn-rate alerting SHOULD be considered for mature SLO-based operations.

---

# 167. Fast Burn

Fast-burn alerts detect severe rapid SLO consumption.

---

# 168. Slow Burn

Slow-burn alerts detect persistent degradation.

---

# 169. Production Diagnostics

Production troubleshooting SHOULD begin with correlated signals rather than arbitrary code changes.

---

# 170. Diagnostic Sequence

Recommended sequence:

```text
1. DEFINE USER IMPACT

2. IDENTIFY TIME WINDOW

3. CHECK DEPLOYMENT/CHANGE

4. CHECK RED METRICS

5. CHECK RESOURCE SATURATION

6. CHECK DEPENDENCIES

7. CORRELATE LOGS/TRACES

8. FORM HYPOTHESIS

9. VALIDATE

10. REMEDIATE
```

---

# 171. Change Correlation

Incident investigation SHOULD identify recent:

```text
Deployments

Configuration Changes

Infrastructure Changes

Dependency Releases
```

---

# 172. Restart Is Not Diagnosis

Restarting pods MAY restore service but does not identify root cause.

---

# 173. Repeated Restart

Repeated restart as routine remediation indicates unresolved reliability problems.

---

# 174. Heap Dump

Heap dumps MAY be collected for controlled memory investigations.

---

# 175. Heap Dump Security

Heap dumps may contain sensitive data and MUST be protected accordingly.

---

# 176. Thread Dump

Thread dumps MAY be used for:

```text
Deadlock

Blocking

Thread Saturation

Pinning Investigation
```

---

# 177. Virtual Thread Diagnostics

Java 21 diagnostics SHOULD distinguish Virtual Thread concurrency from carrier-thread/resource saturation.

---

# 178. JFR

Java Flight Recorder SHOULD be considered for difficult production performance diagnostics.

---

# 179. JFR Overhead

JFR configuration MUST be appropriate for production overhead requirements.

---

# 180. Native Memory Tracking

Native Memory Tracking MAY be enabled temporarily or strategically for difficult container-memory investigations.

---

# 181. Diagnostic Cost

Expensive diagnostics SHOULD be enabled intentionally and for bounded periods.

---

# 182. Debug Logging

Temporary production DEBUG logging MUST have:

```text
Defined Scope

Defined Duration

Owner

Rollback/Disable Plan
```

---

# 183. Global DEBUG

Global DEBUG logging in a high-volume production service SHOULD be avoided.

---

# 184. Dynamic Log Level

Controlled runtime log-level changes MAY be supported where security and operational controls permit.

---

# 185. Diagnostic Endpoint

Custom diagnostic endpoints SHOULD be avoided unless existing platform/JVM capabilities are insufficient.

---

# 186. Diagnostic Security

Diagnostic capabilities MUST NOT create unauthorized access paths.

---

# 187. Observability and Performance

Telemetry overhead MUST be measured for high-volume services.

---

# 188. Logging Cost

Excessive synchronous logging can reduce throughput and increase latency.

---

# 189. Async Logging

Asynchronous logging MAY improve application latency but introduces buffering and loss semantics that MUST be understood.

---

# 190. Log Queue

Asynchronous logging queues MUST be bounded.

---

# 191. Logging Backpressure

Logging failure MUST NOT normally exhaust application memory.

---

# 192. Telemetry Cost Budget

High-volume services SHOULD have an explicit telemetry-volume/cost strategy.

---

# 193. Duplicate Logging

The same business event SHOULD NOT be logged repeatedly by multiple layers without additional operational value.

---

# 194. Success Logging

Per-request success logging MAY be unnecessary when metrics already provide request volume.

---

# 195. Audit vs Application Log

Audit records and diagnostic logs are different concerns.

---

# 196. Audit Trail

Security/business audit requirements MUST NOT depend solely on ordinary application log retention.

---

# 197. Audit Integrity

Audit events SHOULD use the dedicated audit architecture where required.

---

# 198. Observability Retention

Retention MUST reflect:

```text
Operational Need

Security

Compliance

Storage Cost
```

---

# 199. Log Retention

Log retention SHOULD be explicitly defined by environment/data classification.

---

# 200. Metric Retention

Metrics may require longer aggregate retention than raw logs.

---

# 201. Trace Retention

Trace retention MAY be shorter because of volume/cost.

---

# 202. Data Deletion

Telemetry containing regulated data MUST follow applicable data-lifecycle requirements.

---

# 203. Test Strategy

Observability code MUST be tested where it affects behavior or security.

---

# 204. Logging Test

Tests SHOULD validate critical sanitization/masking behavior.

---

# 205. Secret Masking Test

Tests MUST verify known sensitive fields are not exposed.

---

# 206. Correlation Test

Context propagation SHOULD have automated tests when custom propagation logic exists.

---

# 207. Async Context Test

Tests SHOULD verify context propagation across custom executors when required.

---

# 208. Metric Test

Critical custom metrics SHOULD have tests verifying expected increment/record behavior.

---

# 209. Cardinality Test

Architecture tests MAY reject prohibited high-cardinality metric labels.

---

# 210. Health Test

Readiness/liveness semantics SHOULD be integration tested.

---

# 211. OpenTelemetry Test

Custom tracing instrumentation SHOULD verify span creation/attributes without requiring the production telemetry backend.

---

# 212. Exporter Failure Test

Where custom telemetry export behavior exists, exporter failure SHOULD be verified not to break business processing.

---

# 213. Exception Logging Test

Tests SHOULD verify that exception-handling boundaries preserve expected response behavior without duplicate handling logic.

---

# 214. Observability Review Checklist

Material changes SHOULD evaluate:

```text
[ ] Are important failures observable?

[ ] Is the correct layer logging the exception?

[ ] Is the stack trace preserved?

[ ] Could the exception be logged twice?

[ ] Are secrets masked?

[ ] Is PII minimized?

[ ] Can log injection occur?

[ ] Are trace/correlation IDs propagated?

[ ] Is MDC cleaned correctly?

[ ] Are custom metrics necessary?

[ ] Are labels bounded?

[ ] Could metric cardinality explode?

[ ] Are latency percentiles available?

[ ] Is pool saturation observable?

[ ] Are Circuit Breaker states visible?

[ ] Is Kafka/SQS backlog visible?

[ ] Are health semantics correct?

[ ] Is tracing justified?

[ ] Is sampling configured?

[ ] Could telemetry failure affect business traffic?

[ ] Are alerts actionable?
```

---

# 215. Observability Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] No Authorization header logging

[ ] No raw access-token logging

[ ] Correlation filter installed where required

[ ] MDC cleaned after request

[ ] Metrics avoid UUID labels

[ ] Metrics avoid user/customer IDs

[ ] Actuator exposure is restricted

[ ] Readiness endpoint configured

[ ] Liveness endpoint configured

[ ] Custom metrics use approved naming

[ ] Telemetry exporters have bounded timeouts

[ ] Sensitive-data masking tests exist

[ ] No duplicate OpenTelemetry exporters

[ ] Production TRACE disabled by default
```

---

# 216. Enterprise Observability Gate

A service is not considered production compliant when applicable conditions include:

```text
[ ] Critical failures cannot be diagnosed

[ ] Stack traces are systematically discarded

[ ] Same exception is logged repeatedly

[ ] Access tokens can appear in logs

[ ] PII is unnecessarily logged

[ ] Correlation is lost across critical integrations

[ ] MDC leaks between requests

[ ] Metric labels contain unbounded IDs

[ ] HikariCP saturation is invisible

[ ] Queue backlog is invisible

[ ] Circuit Breaker state is invisible

[ ] Liveness depends incorrectly on optional dependencies

[ ] Actuator exposes sensitive information publicly

[ ] Tracing creates unacceptable overhead

[ ] Telemetry outage can fail business requests

[ ] Alerts are systematically noisy/non-actionable
```

---

# 217. Anti-Patterns

The following are prohibited or strongly discouraged:

- `System.out.println` for production application logging
- string-concatenated log messages where parameterized logging is appropriate
- logging only `exception.getMessage()` for unexpected failures
- logging and rethrowing at every layer
- swallowing exceptions to avoid Sonar findings
- logging full bearer tokens
- logging passwords or secret keys
- logging complete sensitive payloads by default
- global HTML escaping of valid business data as a logging-security workaround
- unrestricted newline injection into plain-text logs
- generating a new correlation ID at every service hop
- MDC without cleanup
- unrestricted per-user/per-order metric labels
- using UUIDs as metric dimensions
- measuring only average latency
- creating custom metrics duplicating existing framework metrics
- enabling every OpenTelemetry instrumentation without analysis
- creating spans for trivial internal methods
- 100% trace sampling by default on high-volume systems
- unbounded telemetry exporter queues
- making business requests depend on observability-backend availability
- exposing sensitive Actuator endpoints publicly
- liveness checks that restart healthy applications because Redis/Kafka is unavailable
- dashboards without actionable alerts
- paging on every transient warning
- using pod restart as the permanent incident-remediation strategy
- enabling global DEBUG indefinitely in production

---

# 218. Positive Consequences

The decision provides:

- faster production diagnosis
- improved distributed correlation
- cleaner exception logs
- lower secret-leakage risk
- controlled metric cardinality
- better performance visibility
- database-pool diagnostics
- queue-backlog visibility
- Circuit Breaker visibility
- controlled tracing overhead
- better alert quality
- measurable SLOs
- improved incident response

---

# 219. Negative Consequences

The decision introduces:

- telemetry implementation effort
- metric governance
- logging discipline
- dashboard maintenance
- alert tuning
- storage cost
- tracing cost where enabled
- additional operational testing

These costs are accepted because production systems without adequate observability transfer engineering cost from development into incidents.

---

# 220. Neutral Consequences

The decision also means:

- not every method needs a log
- not every request needs a custom metric
- not every service needs distributed tracing
- not every trace should be retained
- not every dependency belongs in liveness
- some expected failures should not be ERROR
- some production diagnostics should be temporary
- metrics can often replace repetitive success logs

---

# 221. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Secret leakage | Critical | Medium | Masking + tests |
| PII leakage | Critical | Medium | Data minimization |
| Log explosion | High | Medium | Level/volume governance |
| Metric cardinality explosion | Critical | Medium | Bounded labels |
| Tracing overhead | High | Medium | Sampling |
| Telemetry backend outage | High | Medium | Non-blocking bounded export |
| Alert fatigue | High | High | Actionable alert design |
| Missing stack trace | High | Medium | Logging boundary standard |
| Duplicate exception logs | Medium | High | Log-or-rethrow |
| MDC leakage | High | Low/Medium | Cleanup + propagation tests |
| Diagnostic endpoint exposure | Critical | Low | Security controls |

---

# 222. Implementation Guidance

The following rules are mandatory:

1. Production logging must use SLF4J-compatible structured logging conventions.
2. Log levels must have consistent operational semantics.
3. Expected validation failures must not automatically become ERROR logs.
4. Unexpected exceptions must preserve diagnostic context.
5. Exceptions should normally be logged once at the responsible boundary.
6. Logging and rethrowing the same exception through every layer must be avoided.
7. Secrets and credentials must never be logged.
8. Sensitive data must be minimized.
9. Log-injection risks must be controlled.
10. Logging sanitization must not corrupt legitimate business values.
11. Distributed requests must remain correlatable.
12. MDC must be cleaned/restored correctly.
13. Custom async execution must preserve required context.
14. Request-driven services should expose RED metrics.
15. Infrastructure/resource monitoring should consider USE.
16. Java services should expose JVM/GC metrics where operationally useful.
17. Database services should expose HikariCP saturation metrics.
18. Redis behavior should expose relevant cache/failure metrics.
19. Kafka/SQS consumers should expose backlog and failure metrics.
20. Circuit Breaker state and transitions should be observable.
21. Outbound HTTP dependencies should expose bounded dependency metrics.
22. Metric cardinality must remain bounded.
23. IDs such as UUID/orderId/customerId must not be unrestricted metric labels.
24. Micrometer should be used as the Spring application metric facade where applicable.
25. OpenTelemetry should only be enabled when its diagnostic value justifies its overhead.
26. Duplicate telemetry instrumentation/export must be avoided.
27. Production tracing must use a controlled sampling strategy.
28. Telemetry exporter failures must not normally fail business requests.
29. Telemetry buffers, retries and timeouts must be bounded.
30. Actuator endpoints must be restricted to required operational capabilities.
31. Readiness and liveness semantics must remain distinct.
32. Dashboards must focus on operational questions.
33. Alerts must be actionable.
34. SLI/SLO definitions should measure user-relevant behavior.
35. Critical services should consider error-budget-based operations.
36. Production diagnostics should correlate telemetry before speculative code changes.
37. Expensive diagnostics must be temporary and controlled.
38. Observability security behavior must have automated tests.
39. Critical context propagation must have automated tests.
40. Telemetry overhead must be measured for high-volume workloads.

---

# 223. Validation

This ADR will be validated through:

- SLF4J
- Logback
- structured logging
- Spring Boot Actuator
- Micrometer
- Resilience4j metrics
- HikariCP metrics
- Redis metrics
- Kafka metrics
- AWS SQS metrics
- JVM metrics
- Kubernetes metrics
- OpenTelemetry where justified
- JUnit 5
- AssertJ
- Mockito
- integration tests
- architecture tests
- SonarQube
- SAST
- production dashboards
- alerting rules
- SLO monitoring

---

# 224. Success Criteria

The decision is successful when:

- production failures can be correlated across services
- unexpected failures retain useful stack traces
- duplicate exception logging decreases
- credentials no longer appear in application logs
- telemetry cardinality remains controlled
- database-pool saturation is visible
- Kafka/SQS backlog is visible
- Circuit Breaker behavior is visible
- tail latency is measurable
- alert noise decreases
- incident diagnosis becomes faster
- tracing overhead remains controlled where enabled
- observability-backend failures do not affect business processing
- SLOs reflect actual user experience

---

# 225. Alternatives Rejected

## 225.1 Logs Only

Rejected because logs alone do not efficiently represent aggregate system health or distributed latency.

---

## 225.2 Metrics Only

Rejected because metrics cannot provide sufficient event-level diagnostic context.

---

## 225.3 Trace Everything at 100%

Rejected because cost and runtime overhead can become excessive.

---

## 225.4 Log Exceptions at Every Layer

Rejected because duplicate stack traces obscure the responsible failure boundary.

---

## 225.5 User IDs as Metric Labels

Rejected because high cardinality can destabilize monitoring infrastructure.

---

## 225.6 Observability Backend as Required Dependency

Rejected because monitoring failure must not become business failure.

---

## 225.7 Enable OpenTelemetry Everywhere by Default

Rejected because instrumentation must provide measurable diagnostic value relative to its cost and complexity.

---

# 226. Related Decisions

This ADR extends and implements:

- ADR-014: Distributed Observability
- ADR-016: Application Resilience
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
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard

---

# 227. References

- OpenTelemetry Specification
- W3C Trace Context
- Micrometer Documentation
- Spring Boot Actuator Documentation
- SLF4J Documentation
- Logback Documentation
- Resilience4j Documentation
- Prometheus Documentation
- OpenMetrics
- AWS CloudWatch Documentation
- Kubernetes Documentation
- Java Flight Recorder Documentation
- Google Site Reliability Engineering
- OWASP Logging Cheat Sheet

---

# 228. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-25 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise observability and production-diagnostics baseline |

---

# 229. Decision Summary

The observability architecture becomes:

```text
APPLICATION
    |
    +-------------------+
    |         |         |
    v         v         v
  LOGS      METRICS   TRACES
    |         |         |
    +---------+---------+
              |
              v
        CORRELATION
              |
              v
      DASHBOARDS / ALERTS
              |
              v
      INCIDENT RESPONSE
```

Exception handling becomes:

```text
EXCEPTION
    |
    v
CAN THIS LAYER HANDLE IT?
    |
 +--+--+
 |     |
YES    NO
 |      |
 v      v
HANDLE  RETHROW
 +          |
LOG IF      v
USEFUL   RESPONSIBLE
          BOUNDARY
             |
             v
            LOG
```

instead of:

```text
REPOSITORY LOGS
      |
SERVICE LOGS
      |
CONTROLLER LOGS
      |
HANDLER LOGS
      |
      v
FOUR IDENTICAL STACK TRACES
```

Metric design becomes:

```text
REQUEST SERVICE
      |
      +--> RATE
      +--> ERRORS
      +--> DURATION
```

while resource analysis becomes:

```text
RESOURCE
   |
   +--> UTILIZATION
   +--> SATURATION
   +--> ERRORS
```

Java runtime visibility includes:

```text
JVM
 |
 +--> HEAP
 +--> METASPACE
 +--> GC
 +--> THREADS

DATABASE
 |
 +--> ACTIVE CONNECTIONS
 +--> IDLE CONNECTIONS
 +--> PENDING CONNECTIONS

MESSAGING
 |
 +--> LAG
 +--> BACKLOG
 +--> FAILURES

RESILIENCE
 |
 +--> CIRCUIT BREAKER STATE
 +--> FAILURE RATE
 +--> REJECTED CALLS
```

Metric cardinality follows:

```text
GOOD
----
dependency=customers-service
status=success
operation=checkout

BAD
---
customerId=UUID
orderId=UUID
traceId=UUID
email=user@example
```

Distributed correlation becomes:

```text
HTTP REQUEST
    |
    v
TRACE / CORRELATION CONTEXT
    |
    +--> CART
    |
    +--> PRODUCTS
    |
    +--> ORDERS
    |
    +--> KAFKA / SQS
    |
    v
CORRELATED DIAGNOSTICS
```

Tracing follows:

```text
DO WE NEED DISTRIBUTED TRACING?
          |
        +-+-+
        |   |
       NO  YES
        |   |
        v   v
DO NOT   ENABLE
ADD      WITH
COST     CONTROLLED
         SAMPLING
```

Telemetry failure follows:

```text
APPLICATION
     |
     v
TELEMETRY EXPORT
     |
     X
BACKEND FAILURE
     |
     v
DROP / BUFFER WITHIN
DEFINED BOUNDS
     |
     v
BUSINESS PROCESS CONTINUES
```

Alerting follows:

```text
TELEMETRY
    |
    v
USER-IMPACT SIGNAL
    |
    v
SUSTAINED CONDITION
    |
    v
ACTIONABLE ALERT
```

Production diagnosis follows:

```text
USER IMPACT
    |
    v
TIME WINDOW
    |
    v
RECENT CHANGE?
    |
    v
RED METRICS
    |
    v
RESOURCE SATURATION
    |
    v
DEPENDENCIES
    |
    v
LOG / TRACE CORRELATION
    |
    v
HYPOTHESIS
    |
    v
VALIDATION
    |
    v
REMEDIATION
```

The complete observability equation is:

```text
STRUCTURED LOGGING
        +
CORRELATION
        +
SECURE LOGGING
        +
RED METRICS
        +
USE METRICS
        +
JVM VISIBILITY
        +
DEPENDENCY METRICS
        +
BOUNDED CARDINALITY
        +
SELECTIVE DISTRIBUTED TRACING
        +
ACTIONABLE ALERTING
        +
SLI / SLO
        +
CONTROLLED DIAGNOSTICS
        =
PRODUCTION OBSERVABILITY
```

The governing principle is:

```text
Log once at the correct boundary.

Preserve the stack trace
when the failure is unexpected.

Never put credentials in logs.

Do not corrupt valid business data
to solve a logging problem.

Propagate correlation context.

Measure rate, errors and duration.

Observe saturation before
increasing concurrency.

Never put UUIDs into unrestricted
metric dimensions.

Do not enable tracing merely
because the library exists.

Sample according to diagnostic
value and operational cost.

Do not allow telemetry failure
to become business failure.

Alert on conditions that
require action.

Use dashboards to investigate,
not as a substitute for alerts.

And collect only telemetry
that helps engineers understand,
protect and operate the system.
```
