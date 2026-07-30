# ADR-020: Define Service-Level Objectives

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-020 |
| Title | Define Service-Level Objectives |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Reliability Engineering, Observability and Operations |
| Related Work Items | SLO, SLI, Error Budgets, Reliability Governance |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform is a distributed business platform composed of independently deployable services.

The architecture includes:

- synchronous REST APIs
- Amazon SQS producers and consumers
- Transactional Outbox
- Saga workflows
- PostgreSQL
- Redis
- external dependencies
- Kubernetes
- horizontal scaling
- Resilience4j
- OpenTelemetry
- structured logging
- scheduled and batch workloads

Traditional infrastructure monitoring answers questions such as:

```text
Is the pod running?

Is CPU usage high?

Is PostgreSQL available?

Is SQS reachable?
```

These questions are operationally useful but do not answer the most important reliability question:

```text
Can users successfully perform the business operations they depend on?
```

A service may have:

```text
CPU = 20%

Memory = 45%

Pods = Healthy
```

while 15% of checkout requests are failing.

Infrastructure health alone therefore cannot define platform reliability.

The platform requires measurable reliability objectives based primarily on user-visible and business-relevant outcomes.

---

# 2. Problem Statement

The platform requires a standardized reliability model that:

- defines measurable service reliability
- establishes Service-Level Indicators
- establishes Service-Level Objectives
- defines error budgets
- measures user-visible behavior
- supports synchronous APIs
- supports asynchronous SQS processing
- supports long-running Saga workflows
- distinguishes availability from latency
- supports dependency-aware diagnosis
- avoids unrealistic 100% reliability targets
- provides actionable alerts
- integrates with OpenTelemetry
- integrates with metrics and dashboards
- supports capacity planning
- influences release decisions
- supports incident management
- prevents alert fatigue
- remains understandable by engineering and business stakeholders

---

# 3. Decision Drivers

The primary decision drivers are:

1. customer experience
2. business-operation reliability
3. measurable availability
4. measurable latency
5. asynchronous-processing reliability
6. actionable alerting
7. error-budget governance
8. objective release decisions
9. incident prioritization
10. observability integration
11. platform scalability
12. operational simplicity
13. dependency isolation
14. long-term reliability improvement
15. avoidance of alert fatigue
16. realistic reliability targets
17. service ownership
18. auditable reliability history

---

# 4. Definitions

The platform adopts standard Site Reliability Engineering terminology.

---

# 5. Service-Level Indicator

A Service-Level Indicator, or SLI, is a quantitative measurement of service behavior.

Examples:

```text
Successful HTTP requests / valid HTTP requests
```

```text
Requests completed below 500 ms / successful requests
```

```text
SQS messages/events processed within 30 seconds / valid events received
```

---

# 6. Service-Level Objective

A Service-Level Objective, or SLO, defines the target value for an SLI over a defined measurement window.

Example:

```text
99.9% of valid checkout requests
must complete successfully
over a rolling 30-day period.
```

---

# 7. Service-Level Agreement

A Service-Level Agreement, or SLA, is a contractual or externally committed reliability agreement.

An SLA may include:

- contractual obligations
- financial penalties
- support commitments
- customer-facing guarantees

An SLO is an internal engineering reliability target.

An SLA and an SLO must not be treated as interchangeable concepts.

---

# 8. Error Budget

The error budget represents the acceptable amount of unreliability permitted by an SLO.

For an availability SLO:

```text
Error Budget = 1 - SLO
```

Example:

```text
SLO = 99.9%

Error Budget = 0.1%
```

The error budget provides a quantitative mechanism for balancing:

```text
Reliability

versus

Delivery velocity
```

---

# 9. Decision

The AstraForge Supply Platform adopts Service-Level Objectives as the standard mechanism for defining and governing production reliability.

Every production-critical service must define:

- service owner
- critical user journeys
- SLIs
- SLO targets
- measurement windows
- error budgets
- burn-rate alerts
- dashboards
- runbooks
- dependency considerations
- review cadence

Reliability will be measured primarily from user-visible or business-relevant outcomes rather than infrastructure availability alone.

---

# 10. Reliability Principle

The fundamental platform principle is:

```text
Measure what the user experiences,
not merely what the infrastructure reports.
```

A healthy pod does not imply a healthy business operation.

---

# 11. SLO Scope

SLOs should be defined for:

- critical APIs
- critical business operations
- SQS processing
- Saga completion
- scheduled critical workloads
- data freshness where relevant
- external-facing capabilities
- critical internal platform capabilities

Not every technical method requires an SLO.

---

# 12. Critical User Journeys

SLO design begins with Critical User Journeys.

Examples include:

```text
Create Order

Checkout Cart

Approve Order

Cancel Order

Retrieve Order

Process Payment

Reserve Inventory

Complete Order Workflow
```

These journeys represent outcomes that users or dependent systems care about.

---

# 13. Service-Level Indicator Categories

The platform recognizes several SLI categories:

- availability
- latency
- correctness
- freshness
- durability
- throughput
- asynchronous processing delay
- workflow completion
- dependency success

---

# 14. Availability SLI

Availability measures the proportion of valid requests successfully served.

Conceptually:

```text
successful requests
-------------------
valid requests
```

Example:

```text
99.95% of valid GET /orders/{id} requests succeed.
```

---

# 15. Valid Request Definition

The denominator must represent requests the service is expected to serve successfully.

Normally excluded:

- invalid request syntax
- authentication failures caused by invalid credentials
- authorization denials
- client-side validation failures
- explicitly unsupported operations

Normally included:

- internal errors
- database failures
- dependency failures
- unexpected timeouts
- application defects
- resource exhaustion

Exact classification must be documented for each SLI.

---

# 16. HTTP Success Classification

HTTP status alone is insufficient without business semantics.

Typical classification:

```text
2xx
→ success
```

```text
4xx caused by invalid client input
→ normally excluded from availability denominator
```

```text
5xx
→ service failure
```

Some `409`, `422` or other business responses may represent expected behavior and must be classified explicitly.

---

# 17. Business Success

Where HTTP success does not represent business success, the SLI must measure the business outcome.

Example:

```text
POST /checkout returns HTTP 202

but the workflow fails permanently five minutes later.
```

An HTTP availability SLO alone would incorrectly classify the operation as successful.

The platform should therefore combine:

```text
API acceptance SLO

+

Workflow completion SLO
```

where appropriate.

---

# 18. Latency SLI

Latency measures the proportion of requests completed within a defined threshold.

Example:

```text
99% of successful order retrieval requests
complete within 500 ms.
```

Latency should generally be expressed as a distribution rather than an average.

---

# 19. Average Latency

Average latency must not be the primary latency reliability indicator.

Example:

```text
99 requests = 100 ms

1 request = 30 seconds
```

The average may appear acceptable while one user experiences extreme latency.

Percentiles or threshold-based SLIs provide better visibility.

---

# 20. Latency Percentiles

Recommended latency views include:

- p50
- p90
- p95
- p99

However, the formal SLO should normally be expressed as a ratio.

Example:

```text
99% of requests complete below 750 ms.
```

---

# 21. Latency Thresholds

Latency thresholds must be based on:

- user expectations
- business workflow requirements
- dependency behavior
- load testing
- historical production behavior

Thresholds must not be chosen solely because current implementation happens to achieve them.

---

# 22. Correctness SLI

Correctness measures whether operations produce the expected business result.

Examples:

```text
Orders persisted with valid totals

Events published with correct contract

Payments associated with correct orders

Inventory reservations associated with correct items
```

Correctness SLIs may require business-specific instrumentation.

---

# 23. Freshness SLI

Freshness measures whether derived or replicated information is sufficiently current.

Examples:

```text
99.9% of order projections are less than 30 seconds behind.
```

```text
99% of reporting data is updated within 5 minutes.
```

---

# 24. Durability SLI

Durability measures whether acknowledged data remains preserved.

For critical transactional data, durability expectations are significantly stronger than ordinary availability objectives.

Data-loss tolerance must be defined explicitly.

---

# 25. SQS Processing SLI

SQS-based services require asynchronous SLIs.

Example:

```text
99.9% of valid ORDER_CREATED events
are processed successfully within 30 seconds.
```

This combines:

- processing success
- processing delay

---

# 26. SQS Processing Delay

Processing delay should measure:

```text
consumer processing completion time
-
event availability time
```

This captures:

- queue backlog/oldest-message age
- retry delay
- processing duration
- temporary consumer outage

---

# 27. SQS Queue Backlog/Oldest-Message Age

Consumer lag is an important operational metric but is not necessarily the SLI itself.

Example:

```text
Consumer lag = 20,000 records
```

does not directly indicate user impact if throughput is extremely high.

Prefer measuring:

```text
time-to-process
```

for reliability objectives.

---

# 28. Event Processing Success

The platform must distinguish:

- successful processing
- duplicate processing
- expected business rejection
- retryable failure
- terminal failure
- dead-letter routing

These classifications determine SLI calculation.

---

# 29. Duplicate Messages

Correctly detected duplicate messages should normally not count as service failures.

At-least-once delivery means duplicates are an expected platform condition.

Failure to handle a duplicate safely may count as a reliability failure.

---

# 30. Dead-Letter Events

A valid business event moved to a dead-letter topic normally consumes error budget.

The exact classification must consider whether:

- automatic recovery exists
- processing completed within the SLO window
- the event was invalid
- the producer violated the contract

---

# 31. Transactional Outbox SLO

Critical outbox publishers should define publication reliability.

Example:

```text
99.99% of committed outbox events
are successfully published within 60 seconds.
```

---

# 32. Outbox Publication Delay

The measurement begins when the business transaction commits and ends when the event is successfully accepted by the messaging infrastructure.

This measures the actual integration delay experienced by downstream consumers.

---

# 33. Saga SLO

Long-running workflows require completion SLOs.

Example:

```text
99.5% of valid order-creation Sagas
reach a successful terminal state
within 2 minutes.
```

---

# 34. Saga Failure Classification

Saga outcomes must distinguish:

- successful completion
- valid business rejection
- successful compensation
- failed compensation
- timeout
- stuck workflow
- manual intervention required

A compensated Saga is not automatically equivalent to a platform failure.

Its classification depends on business semantics.

---

# 35. Saga Duration

Saga duration should be measured from:

```text
workflow accepted
```

to:

```text
terminal state reached
```

Intermediate retries remain part of the total user-visible duration.

---

# 36. Scheduled Job SLO

Critical scheduled jobs should define:

- expected start window
- completion deadline
- success ratio
- freshness requirement

Example:

```text
99.9% of daily reconciliation jobs
complete before 06:00.
```

---

# 37. Dependency SLOs

A service may depend on:

- PostgreSQL
- SQS
- Redis
- internal services
- external providers

Dependency reliability must be measured separately from the service's own SLO.

---

# 38. Dependency Failure

A downstream failure may still consume the upstream service's error budget if users experience failure.

Example:

```text
Checkout fails because payment provider is unavailable.
```

From the user's perspective:

```text
Checkout failed.
```

The upstream service cannot simply remove the request from its SLO because the root cause was external.

---

# 39. Dependency Attribution

Operational dashboards should distinguish:

```text
User-visible failure

and

Attributed technical cause
```

Example:

```text
Checkout availability = 99.70%

Failure attribution:
- Payment provider: 61%
- Database: 17%
- Application defect: 14%
- Timeout: 8%
```

---

# 40. SLO Ownership

Every SLO must have an owner.

The owner is responsible for:

- definition
- instrumentation
- dashboard
- alerting
- runbook
- review
- remediation
- error-budget policy

Ownership should normally align with service or domain ownership.

---

# 41. SLO Document

Each production-critical service should maintain an SLO document.

Example structure:

```yaml
service: orders-service
owner: order-platform-team

slos:
  - name: order-search-availability
    objective: 99.95
    window: 30d

  - name: order-search-latency
    objective: 99.00
    threshold: 750ms
    window: 30d
```

The exact representation may evolve.

---

# 42. Measurement Window

The default SLO measurement window is:

```text
rolling 30 days
```

Alternative windows may be used when justified.

Examples:

- 7 days
- 28 days
- calendar month
- quarter

Rolling windows are preferred for operational reliability management.

---

# 43. Rolling Window

A rolling window continuously evaluates the previous period.

Example:

```text
At 14:00 today:

evaluate reliability from
14:00 thirty days ago
through
14:00 today.
```

This avoids artificial resets at calendar boundaries.

---

# 44. SLO Target Selection

Targets must reflect business requirements.

Examples:

```text
99%

99.5%

99.9%

99.95%

99.99%
```

Higher reliability targets have significantly higher engineering and infrastructure costs.

---

# 45. 100 Percent Reliability

A 100% SLO is prohibited as a normal platform target.

A 100% objective provides:

```text
zero error budget
```

which prevents meaningful trade-offs and is usually unrealistic for distributed systems.

Exceptions require explicit business justification.

---

# 46. Reliability Cost

Moving from:

```text
99.9%
```

to:

```text
99.99%
```

is not a minor improvement.

It may require:

- stronger redundancy
- lower deployment risk
- more testing
- higher infrastructure cost
- stricter dependency objectives
- more automation
- improved disaster recovery
- reduced change velocity

---

# 47. Error Budget Calculation

For request-based SLOs:

```text
Allowed bad events =
total eligible events × error-budget percentage
```

Example:

```text
SLO = 99.9%

Requests = 10,000,000

Error Budget = 0.1%

Allowed bad requests = 10,000
```

---

# 48. Time-Based Availability

For continuously available capabilities, availability may also be discussed in time terms.

For a 30-day period:

| SLO | Approximate Unavailable Time |
|---|---:|
| 99% | 7h 12m |
| 99.9% | 43m 12s |
| 99.95% | 21m 36s |
| 99.99% | 4m 19s |

Request-based SLOs remain preferable when meaningful request volume exists.

---

# 49. Error Budget Purpose

The error budget provides a common language between:

- engineering
- operations
- product
- architecture
- management

It answers:

```text
How much unreliability can the service tolerate
while still meeting its objective?
```

---

# 50. Error Budget Consumption

The platform must track:

```text
Error Budget Remaining
```

and:

```text
Error Budget Burn Rate
```

A service may currently meet its SLO while burning budget at a rate that predicts an imminent breach.

---

# 51. Burn Rate

Burn rate represents how quickly the error budget is being consumed.

Conceptually:

```text
observed error rate
-------------------
allowed error rate
```

For a 99.9% SLO:

```text
allowed error rate = 0.1%
```

If observed error rate is:

```text
1%
```

then:

```text
burn rate = 10
```

---

# 52. Burn Rate Interpretation

Example:

```text
Burn Rate = 1
```

The service is consuming error budget exactly at the sustainable rate.

```text
Burn Rate = 10
```

The service is consuming budget ten times faster than sustainable.

```text
Burn Rate = 0
```

No error budget is currently being consumed.

---

# 53. Multi-Window Burn-Rate Alerts

The platform adopts multi-window burn-rate alerting for critical SLOs.

The objective is to detect:

- severe outages quickly
- slower reliability degradation reliably

without generating excessive noise.

---

# 54. Fast-Burn Alert

A fast-burn alert detects severe reliability loss over a short period.

Example conceptual rule:

```text
High burn rate over 5 minutes

AND

High burn rate over 1 hour
```

This reduces false positives from extremely brief spikes.

---

# 55. Slow-Burn Alert

A slow-burn alert detects sustained degradation.

Example:

```text
Elevated burn rate over 6 hours

AND

Elevated burn rate over 3 days
```

Exact thresholds must be derived from the SLO and operational requirements.

---

# 56. Paging Alerts

Pager alerts should indicate:

```text
Immediate human action is required.
```

Appropriate examples:

- rapid error-budget consumption
- critical checkout failure
- major SQS processing outage
- Saga terminal failure growth
- critical data-processing deadline at risk

---

# 57. Ticket Alerts

Not every SLO problem requires immediate paging.

Slower budget consumption may generate:

- ticket
- backlog item
- daily notification
- reliability review action

---

# 58. Alerting Principle

The platform adopts:

```text
Page on symptoms.

Investigate causes.
```

Primary alerts should represent user-visible impact.

Infrastructure alerts should support diagnosis or identify conditions that require action before user impact.

---

# 59. CPU Alerts

High CPU alone should not automatically page an application team.

Example:

```text
CPU = 95%

Availability = 100%

Latency SLO = healthy
```

The system may simply be efficiently using allocated capacity.

CPU becomes operationally important when:

- saturation is imminent
- autoscaling cannot respond
- latency is degrading
- errors are increasing
- capacity margin is insufficient

---

# 60. Memory Alerts

Memory usage should be interpreted with:

- container limit
- JVM heap
- native memory
- garbage collection
- OOM risk
- request behavior

Memory percentage alone is not a user-facing SLI.

---

# 61. Saturation

Saturation metrics remain important diagnostic signals.

Examples:

- CPU throttling
- connection-pool exhaustion
- executor queue saturation
- SQS consumer backlog
- database connection wait
- bulkhead rejection
- rate-limit rejection

These signals help explain SLO degradation.

---

# 62. Four Golden Signals

The platform should monitor:

- latency
- traffic
- errors
- saturation

These signals complement formal SLOs.

---

# 63. RED Method

Request-driven services should generally expose RED metrics:

```text
Rate

Errors

Duration
```

---

# 64. USE Method

Infrastructure resources may use USE:

```text
Utilization

Saturation

Errors
```

These remain diagnostic metrics rather than replacements for user-facing SLOs.

---

# 65. OpenTelemetry

SLI measurements should derive from OpenTelemetry-compatible telemetry wherever practical.

Signals include:

- metrics
- traces
- structured logs

Metrics remain the preferred source for SLO calculations.

---

# 66. Metrics First

SLO calculations should normally use metrics because metrics provide:

- efficient aggregation
- bounded cardinality
- long retention
- inexpensive rate calculations
- reliable alerting

Logs should not be the primary source of high-volume SLO calculation.

---

# 67. Trace-Based Analysis

Traces are valuable for explaining why an SLO degraded.

Example:

```text
Checkout latency SLO degraded

↓

Trace analysis

↓

85% of slow requests waiting on payment provider
```

---

# 68. Structured Logs

Structured logs support:

- individual failure diagnosis
- correlation IDs
- error codes
- event IDs
- retry context
- operational evidence

They complement but do not replace SLO metrics.

---

# 69. Metric Naming

SLI metrics should follow a standardized naming convention.

Examples:

```text
http.server.request.duration

messaging.process.duration

workflow.duration

outbox.publish.duration
```

Platform-specific metrics should use a documented namespace.

---

# 70. Metric Cardinality

SLO metrics must use bounded labels.

Appropriate labels include:

- service
- operation
- route
- result
- status category
- event type
- workflow type

Prohibited labels include:

- order ID
- customer ID
- event ID
- trace ID
- correlation ID
- saga ID
- arbitrary exception message

---

# 71. Route Normalization

HTTP SLO metrics must use normalized routes.

Preferred:

```text
/api/v1/orders/{orderId}
```

Prohibited:

```text
/api/v1/orders/54cf1881-b347-4d79-b859-b91283543c12
```

---

# 72. Error Classification

Errors should be classified into bounded categories.

Examples:

```text
client

business

dependency

timeout

database

messaging

concurrency

unexpected
```

This enables diagnosis without uncontrolled cardinality.

---

# 73. Availability Metric

Conceptual counters:

```text
requests_total{
  operation="order.search",
  result="success"
}
```

and:

```text
requests_total{
  operation="order.search",
  result="failure"
}
```

The SLI derives from their ratio.

---

# 74. Latency Metric

Latency should use histograms.

Example:

```text
http.server.request.duration
```

Histograms support:

- threshold ratios
- percentiles
- burn-rate calculations
- aggregation across instances

---

# 75. Histogram Buckets

Histogram buckets must align with meaningful latency thresholds.

Example:

```text
100 ms

250 ms

500 ms

750 ms

1 s

2 s

5 s
```

Buckets should not be selected arbitrarily.

---

# 76. Native Histograms

Native histogram capabilities may be adopted where supported by the metrics backend and platform standards.

This is an implementation detail and does not change the SLO definition.

---

# 77. Counter Reset

SLO calculations must correctly handle counter resets caused by:

- pod restart
- deployment
- scaling
- application crash

Metrics backends should use rate functions designed for monotonic counters.

---

# 78. Multiple Replicas

SLIs must aggregate across all healthy and unhealthy service replicas.

A failing pod must not disappear from the reliability calculation merely because another replica succeeds.

---

# 79. Deployment Windows

Deployments remain part of normal service operation.

Failures during deployment normally consume error budget.

Excluding deployment windows would hide real user impact.

---

# 80. Maintenance Windows

Planned maintenance may be treated differently only when:

- formally defined
- communicated
- contractually allowed
- technically measurable
- approved by reliability governance

The default is not to exclude maintenance automatically.

---

# 81. Low-Traffic Services

Request-based SLOs can become statistically unstable for low-traffic services.

Alternative indicators may include:

- synthetic probes
- scheduled canary operations
- time-based availability
- workflow completion
- freshness

The measurement strategy must reflect actual usage.

---

# 82. Synthetic Monitoring

Synthetic tests may validate critical journeys continuously.

Examples:

```text
Create test order

Retrieve test order

Perform controlled workflow
```

Synthetic monitoring should not create unintended business side effects.

---

# 83. Synthetic SLI Limitations

Synthetic probes do not fully represent real-user behavior.

They complement rather than replace real traffic measurements.

---

# 84. External Dependencies

External providers may have their own SLA or SLO.

The platform should record:

- dependency objective
- observed dependency reliability
- upstream impact
- fallback behavior
- contractual escalation path

---

# 85. Dependency Budget

An upstream service cannot promise reliability that is mathematically incompatible with critical dependencies unless it has mechanisms such as:

- redundancy
- caching
- fallback
- asynchronous decoupling
- multiple providers
- graceful degradation

---

# 86. SLO Composition

Consider a synchronous workflow requiring three dependencies, each with:

```text
99.9% availability
```

A simplistic independent approximation gives:

```text
0.999 × 0.999 × 0.999
≈ 99.70%
```

End-to-end reliability can therefore be lower than each individual dependency.

Architecture must consider reliability composition.

---

# 87. Parallel Dependencies

Parallel dependency execution may reduce latency but can change reliability characteristics.

If all parallel calls are mandatory, every dependency must succeed.

If some calls are optional, graceful degradation may preserve the user journey.

The SLO should reflect actual business success.

---

# 88. Resilience4j

Resilience mechanisms influence SLO outcomes.

Examples:

- retry may recover transient failures
- circuit breaker may prevent cascading failure
- bulkhead may preserve capacity
- rate limiter may intentionally reject excess traffic
- fallback may preserve degraded functionality

The SLI should measure the final user-visible outcome, not individual internal attempts.

---

# 89. Retry Metrics

Internal retry failures should not each consume error budget if the final operation succeeds within its SLO.

Example:

```text
Attempt 1 = timeout

Attempt 2 = success

Total latency = 400 ms

Latency threshold = 750 ms
```

The user-visible operation remains good.

Retries should still be monitored diagnostically.

---

# 90. Circuit Breaker

Circuit-breaker rejection may count as a failed user operation if the request cannot be served successfully.

The fact that the breaker behaved correctly does not make the user request successful.

---

# 91. Fallback

A fallback may satisfy an availability SLO only if the degraded result is considered an acceptable successful business response.

Fallback success must be defined explicitly.

---

# 92. Graceful Degradation

The platform should identify capabilities that can degrade independently.

Example:

```text
Order creation succeeds

Recommendation service unavailable

Recommendations omitted
```

If recommendations are optional, order creation remains successful.

---

# 93. Error Budget Policy

Every critical service must define an error-budget policy.

The policy determines actions when budget consumption reaches predefined thresholds.

---

# 94. Healthy Error Budget

When the service has sufficient budget remaining, teams may continue normal:

- feature development
- deployments
- refactoring
- experiments
- dependency upgrades

subject to normal engineering controls.

---

# 95. Elevated Budget Consumption

When burn rate becomes elevated, teams should prioritize:

- reliability investigation
- defect remediation
- dependency analysis
- capacity review
- rollback readiness

---

# 96. Exhausted Error Budget

When the error budget is exhausted, the default policy is to prioritize reliability work over discretionary risk.

Possible actions include:

- pause high-risk releases
- reduce deployment frequency
- prioritize defects
- increase review requirements
- rollback unstable features
- perform architecture remediation
- improve testing
- improve capacity

---

# 97. Release Freeze

Error-budget exhaustion does not imply an absolute ban on every deployment.

Reliability-improving changes must still be deployable.

Examples:

- bug fix
- rollback
- capacity improvement
- security fix
- observability correction
- dependency recovery

---

# 98. Change Risk

Release decisions should consider:

```text
Error Budget Remaining

×

Change Risk

×

Business Priority
```

A service with nearly exhausted budget should avoid unnecessary high-risk change.

---

# 99. Error Budget Exceptions

Business leadership may approve exceptional risk acceptance.

The exception must document:

- reason
- owner
- duration
- expected impact
- mitigation
- rollback strategy

---

# 100. SLO Review

SLOs should be reviewed periodically.

Recommended cadence:

```text
quarterly
```

Critical services may require monthly operational review.

---

# 101. Review Questions

SLO review should ask:

- Does the SLI still represent user experience?
- Is the target appropriate?
- Is the service consistently overperforming?
- Is the service consistently missing the objective?
- Are exclusions still valid?
- Are alerts actionable?
- Are dependencies dominating failures?
- Is telemetry accurate?
- Has business criticality changed?

---

# 102. Consistently Exceeding SLO

If a service consistently achieves:

```text
99.999%
```

against a:

```text
99%
```

objective, possibilities include:

- target is too weak
- service is overengineered
- measurement is incorrect
- traffic is unrepresentative

The SLO should be reviewed.

---

# 103. Consistently Missing SLO

If a service continuously misses its SLO, the team must determine whether:

- reliability is insufficient
- objective is unrealistic
- dependency architecture is inadequate
- capacity is insufficient
- telemetry is incorrect
- business requirements changed

Changing the SLO solely to make dashboards green is prohibited.

---

# 104. Dashboard

Every critical service should expose an SLO dashboard containing:

- current SLI
- SLO target
- error budget remaining
- burn rate
- request volume
- latency distribution
- error categories
- dependency attribution
- recent deployments
- relevant incidents

---

# 105. Executive View

A higher-level platform dashboard should show:

- critical journey
- current SLO status
- error budget remaining
- trend
- owner
- active incident

This view should avoid low-level infrastructure noise.

---

# 106. Engineering View

Engineering dashboards should include diagnostic signals such as:

- pod health
- CPU
- memory
- JVM
- garbage collection
- database pool
- SQS queue backlog/oldest-message age
- circuit-breaker state
- retry rate
- dependency latency

These explain SLO behavior.

---

# 107. Alert Runbook

Every paging SLO alert must have a runbook.

The runbook should include:

- SLO affected
- user impact
- likely causes
- diagnostic dashboards
- trace queries
- log queries
- dependency checks
- rollback procedure
- escalation
- recovery validation

---

# 108. Incident Severity

Incident severity should consider:

- SLO impact
- criticality of user journey
- burn rate
- duration
- number of affected users
- data-integrity risk
- security impact

---

# 109. Incident Resolution

An incident is not resolved merely because infrastructure appears healthy.

Resolution should verify:

```text
SLI recovered

AND

burn rate returned to sustainable levels
```

---

# 110. Post-Incident Review

Significant SLO incidents should produce a post-incident review.

The review should analyze:

- timeline
- customer impact
- error-budget consumption
- detection
- mitigation
- root cause
- contributing factors
- remediation
- alert quality
- observability gaps

---

# 111. Blameless Analysis

Incident analysis should focus on:

- system behavior
- process
- architecture
- safeguards
- assumptions
- tooling

rather than individual blame.

---

# 112. Capacity Planning

SLO data should influence capacity planning.

Capacity decisions should consider:

- traffic growth
- latency trend
- saturation
- autoscaling response
- dependency capacity
- error-budget consumption
- seasonal peaks

---

# 113. Load Testing

Load tests should validate whether SLOs remain achievable at:

- expected traffic
- peak traffic
- projected traffic
- degraded dependency conditions

---

# 114. Performance Budgets

Critical operations may define latency budgets.

Example:

```text
Checkout total budget = 2 seconds
```

Possible allocation:

```text
Application processing: 300 ms

Customer service: 200 ms

Product service: 250 ms

Payment service: 700 ms

Database: 150 ms

Network and margin: 400 ms
```

Budgets guide dependency timeout configuration.

---

# 115. Timeout Alignment

Timeouts should align with end-to-end latency objectives.

A dependency timeout longer than the entire user-facing latency SLO is usually incorrect.

---

# 116. Retry Alignment

Retry configuration must fit inside the operation latency budget.

Example:

```text
SLO threshold = 1 second

Dependency timeout = 900 ms

3 retries
```

This configuration cannot realistically satisfy the latency objective.

---

# 117. Bulkhead Capacity

Bulkhead configuration should protect the SLO of unrelated operations.

One failing dependency must not consume all application concurrency.

---

# 118. Autoscaling

Horizontal Pod Autoscaler configuration should consider:

- traffic
- CPU
- memory
- custom saturation metrics
- startup time
- SLO latency
- expected burst behavior

Autoscaling is a mechanism for meeting SLOs, not an objective itself.

---

# 119. Kubernetes Availability

Pod availability is not equivalent to application availability.

Example:

```text
10 / 10 pods Ready
```

while:

```text
database queries fail
```

means the application may still violate its SLO.

---

# 120. Readiness

Readiness probes should represent whether a pod can safely receive traffic.

They should not necessarily depend on every optional external dependency.

Poorly designed readiness checks can amplify outages.

---

# 121. Liveness

Liveness checks should detect unrecoverable process failure.

They must not restart healthy applications because a remote dependency is temporarily unavailable.

---

# 122. Database Reliability

Database diagnostics should include:

- connection acquisition time
- query latency
- transaction failure rate
- deadlocks
- lock waits
- connection saturation
- replication health where applicable

The user-facing SLO remains authoritative for application reliability.

---

# 123. Optimistic Locking

Expected optimistic-lock conflicts should be classified according to business behavior.

If the API correctly returns a conflict requiring the user to reload state, this may be a valid expected response rather than availability failure.

Unexpected conflict storms remain a reliability concern.

---

# 124. Data Integrity

Data-integrity failures have higher severity than ordinary availability failures.

Examples:

- lost committed order
- duplicate financial operation
- incorrect payment association
- corrupted aggregate
- missing mandatory integration event

Reliability governance must not reduce these failures to ordinary error-budget mathematics.

---

# 125. Durability Objectives

Critical data may require effectively zero tolerated known loss.

This is separate from accepting a small availability error budget.

Availability and durability objectives must not be conflated.

---

# 126. Security Incidents

Security failures are not governed solely through error budgets.

A security incident may require immediate action even if the service remains within its availability SLO.

---

# 127. Compliance Failures

Compliance violations are not acceptable merely because error budget remains.

Error budgets govern reliability risk, not legal or security risk acceptance.

---

# 128. Feature-Level SLO

A service may define different SLOs for different capabilities.

Example:

```text
Order search: 99.95%

Order creation: 99.9%

Historical export: 99.0%
```

Not every operation has equal business criticality.

---

# 129. Tier Classification

Services may be assigned reliability tiers.

Example:

```text
Tier 0 — Mission critical

Tier 1 — Business critical

Tier 2 — Important

Tier 3 — Supporting
```

Each tier may define baseline reliability expectations.

---

# 130. Tier 0

Tier 0 may include capabilities whose prolonged failure stops core business operations.

Examples:

- order creation
- checkout
- payment coordination
- authentication infrastructure

Tier 0 requires:

- strict SLOs
- paging
- tested runbooks
- disaster recovery
- redundancy
- regular reliability review

---

# 131. Tier 1

Tier 1 represents important capabilities with significant business impact but limited short-term degradation tolerance.

---

# 132. Tier 2

Tier 2 services may tolerate longer recovery windows.

Examples may include:

- reporting
- non-critical exports
- administrative tools

---

# 133. Tier-Based Targets

Reliability tiers may define starting targets, but individual SLOs still require business validation.

A tier must not automatically impose an inappropriate objective.

---

# 134. Multi-Region Systems

If multi-region deployment is introduced, SLOs must define whether they measure:

- regional reliability
- global reliability
- failover success
- failover duration

This requires additional architecture decisions.

---

# 135. Disaster Recovery

Disaster recovery objectives include:

```text
RTO — Recovery Time Objective

RPO — Recovery Point Objective
```

These are related to but distinct from SLOs.

---

# 136. Recovery Time Objective

RTO defines the acceptable time to restore service after a major disaster.

---

# 137. Recovery Point Objective

RPO defines the acceptable amount of data loss measured in time.

Example:

```text
RPO = 5 minutes
```

means up to five minutes of data may theoretically be lost under the defined disaster scenario.

Critical transactional systems may require significantly stronger objectives.

---

# 138. SLO and Disaster Recovery

A service may meet normal availability SLOs but still have inadequate disaster-recovery capability.

Both dimensions must be governed.

---

# 139. SLO as Code

SLO definitions should be version controlled.

Preferred approach:

```text
SLO as Code
```

Definitions may include:

- owner
- objective
- SLI query
- window
- labels
- alert policy
- documentation links

---

# 140. Version Control

SLO changes require code review.

Review should include:

- service owner
- platform/SRE where applicable
- architecture for critical changes
- product/business representative where target meaning changes

---

# 141. SLO History

Historical SLO definitions should remain available through version control.

This allows teams to understand:

- when targets changed
- why targets changed
- whether instrumentation changed
- whether reliability improved

---

# 142. Instrumentation Changes

Changing the SLI query can alter measured reliability even when system behavior did not change.

Instrumentation changes must therefore be reviewed and documented.

---

# 143. Missing Telemetry

Missing telemetry must not automatically be interpreted as success.

The platform must distinguish:

```text
Good

Bad

Unknown
```

Unknown measurement periods require investigation.

---

# 144. Telemetry Pipeline Failure

If the metrics pipeline fails, the service may remain healthy while SLO calculation becomes unavailable.

Telemetry pipeline availability must therefore be monitored separately.

---

# 145. SLO Backfill

Backfilling missing SLO data should be avoided unless the source data is authoritative and the methodology is documented.

Fabricating successful periods is prohibited.

---

# 146. Alert Evaluation Failure

Failure to evaluate an SLO alert is itself an observability risk.

Alerting infrastructure must expose its own health.

---

# 147. Service Ownership Metadata

Services should expose ownership metadata.

Examples:

- team
- repository
- runbook
- dashboard
- escalation contact
- tier

This enables automated operational routing.

---

# 148. Production Readiness

A new Tier 0 or Tier 1 service must not be considered production-ready without:

- defined SLO
- valid SLI instrumentation
- dashboard
- burn-rate alerts
- runbook
- ownership
- load-test evidence
- dependency analysis

---

# 149. New Service Baseline

Before sufficient production data exists, a provisional SLO may be established.

It must be reviewed after enough real traffic has been collected.

---

# 150. New Feature Baseline

Critical new features should define expected reliability before release.

Instrumentation must exist before significant production exposure.

---

# 151. SLO During Canary Deployment

Canary deployments should compare:

- error rate
- latency
- business success
- resource saturation

against the stable version.

A canary that threatens the service's error budget should be stopped or rolled back.

---

# 152. Automated Rollback

Where deployment automation is mature, SLO-related indicators may participate in automated rollback.

Automation must avoid reacting to statistically insignificant traffic.

---

# 153. Deployment Markers

Dashboards should display deployment markers.

This supports correlation between:

```text
Reliability degradation

and

Recent release
```

---

# 154. Feature Flag Correlation

Reliability dashboards may correlate major feature-flag changes with SLO behavior.

High-cardinality user-level flag dimensions must be avoided.

---

# 155. Error Budget Reporting

Periodic reporting should include:

- SLO status
- budget remaining
- budget consumed
- major incidents
- top failure causes
- reliability work completed
- upcoming risks

---

# 156. Reliability Review

A recurring reliability review should focus on:

```text
What consumed the error budget?

What risks threaten the next period?

What changes will improve reliability?
```

It should not become a dashboard-status meeting without action.

---

# 157. Reliability Backlog

Recurring SLO problems should generate reliability backlog items.

Examples:

- dependency timeout redesign
- database index improvement
- retry-policy correction
- Saga recovery automation
- capacity increase
- cache resilience
- test coverage
- observability gaps

---

# 158. Reliability Engineering Work

Reliability work includes:

- reducing failure probability
- reducing blast radius
- improving detection
- improving recovery
- improving rollback
- reducing manual intervention
- improving capacity
- eliminating toil
- improving data integrity

---

# 159. Toil

Repeated manual recovery should be measured and reduced.

Examples:

- manually replaying SQS messages
- manually restarting consumers
- manually correcting Saga state
- manually republishing outbox records

Recurring toil indicates missing automation or architectural weakness.

---

# 160. Error Budget and Technical Debt

Error-budget consumption can help prioritize technical debt.

A component repeatedly causing budget loss should receive higher remediation priority than debt with no measurable reliability impact.

---

# 161. Testing Strategy

SLO implementation must be tested.

Tests should validate:

- event classification
- success classification
- failure classification
- business-rejection classification
- normalized routes
- metric labels
- histogram boundaries
- SQS processing metrics
- Saga completion metrics
- outbox publication metrics
- duplicate-event behavior
- error-budget calculations
- alert expressions

---

# 162. Unit Tests

Unit tests should validate deterministic classification logic.

Examples:

```text
HTTP 200 → good

HTTP 500 → bad

HTTP 400 validation → excluded

duplicate SQS message/event → good/neutral according to policy
```

---

# 163. Integration Tests

Integration tests should verify that real application operations produce the expected telemetry.

Examples:

- successful HTTP request increments good-event counter
- failed dependency increments bad-event counter
- SQS message/event records processing duration
- Saga completion records terminal state
- outbox publication records delay

---

# 164. Metric Label Tests

Tests should verify that high-cardinality labels are not emitted.

Prohibited examples:

```text
orderId

customerId

eventId

traceId

exceptionMessage
```

---

# 165. Alert Tests

Alert expressions should be tested against synthetic metric series.

Scenarios include:

- healthy service
- short transient spike
- severe outage
- slow degradation
- zero traffic
- telemetry loss
- deployment restart

---

# 166. Load Testing

Load testing should validate SLO feasibility.

The platform must understand:

- maximum sustainable throughput
- latency under load
- saturation point
- error rate near capacity
- autoscaling behavior
- dependency bottlenecks

---

# 167. Failure Injection

Controlled failure injection should validate SLO behavior.

Examples:

- dependency latency
- dependency HTTP 500
- database slowdown
- SQS unavailability
- Redis outage
- pod termination
- packet loss

The objective is to confirm that:

- SLIs reflect user impact
- alerts trigger correctly
- resilience mechanisms behave as expected

---

# 168. Chaos Engineering

Chaos experiments should validate assumptions such as:

```text
One pod can fail without SLO impact.

Redis can fail without checkout failure.

One dependency timeout does not exhaust all request threads.
```

Experiments must be controlled and approved.

---

# 169. SLO Validation Against Logs

Periodic validation should compare metric-derived SLO results with sampled structured logs.

This can identify classification or instrumentation defects.

Logs must not become the primary calculation source.

---

# 170. SLO Validation Against Traces

Trace sampling may be used to verify:

- latency attribution
- dependency contribution
- unexpected retry behavior
- workflow paths

---

# 171. Anti-Patterns

The following are prohibited:

- defining uptime only from pod health
- using CPU as an availability SLO
- using average latency as the only latency indicator
- setting every service to 99.99%
- defining 100% SLO without exceptional justification
- excluding deployment failures automatically
- excluding dependency failures from user-facing SLOs
- counting invalid client requests as server failures without defined semantics
- counting every internal retry as a user-visible failure
- using SQS backlog/oldest-message age alone as the asynchronous SLI
- defining SLOs without owners
- defining SLOs without runbooks
- paging on every infrastructure threshold
- paging on non-actionable conditions
- using high-cardinality metric labels
- changing SLI queries without review
- changing SLO targets solely to make reports green
- treating compensated Saga outcomes without business classification
- treating logs as the primary SLO data source
- ignoring missing telemetry
- resetting error budgets manually after incidents
- using error budget to excuse security violations
- using error budget to excuse data corruption
- treating SLA and SLO as synonyms
- measuring only service internals instead of user outcomes
- setting latency objectives without load-test evidence
- configuring retries that exceed the end-to-end latency budget
- ignoring dependency reliability composition

---

# 172. Positive Consequences

The decision provides:

- objective reliability measurement
- user-centered monitoring
- measurable error budgets
- better alert quality
- reduced alert fatigue
- objective release-risk decisions
- stronger incident prioritization
- improved capacity planning
- clearer service ownership
- measurable dependency impact
- better architecture trade-offs
- better alignment between engineering and business
- actionable reliability trends
- measurable impact of resilience improvements
- standardized production-readiness criteria

---

# 173. Negative Consequences

The decision introduces:

- instrumentation effort
- SLO definition effort
- dashboard maintenance
- alert maintenance
- reliability governance
- classification complexity
- service-owner responsibility
- error-budget policy decisions
- metric-storage requirements
- regular review effort

These costs are accepted because reliability without measurable objectives cannot be governed effectively.

---

# 174. Neutral Consequences

The decision also means:

- not every failure consumes error budget
- not every infrastructure problem creates user impact
- some user-visible failures originate from dependencies
- some valid business rejections are not service failures
- different capabilities may have different targets
- higher reliability targets increase engineering cost
- error-budget exhaustion influences release decisions
- telemetry correctness becomes part of reliability engineering
- SLOs may evolve as business requirements change

---

# 175. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| SLI does not represent user experience | High | Medium | Begin with critical user journeys |
| Target is unrealistically high | High | Medium | Use business requirements and historical data |
| Target is too weak | Medium | Medium | Periodic SLO review |
| Client errors distort availability | Medium | High | Explicit denominator classification |
| Dependency failures are hidden | High | Medium | Measure end-to-end outcome and attribute cause |
| High-cardinality labels increase cost | High | Medium | Enforce bounded dimensions |
| Alerting becomes noisy | High | Medium | Multi-window burn-rate alerts |
| Missing telemetry appears healthy | High | Low | Explicit unknown state |
| SLO query changes historical meaning | Medium | Medium | Version-control and review |
| Error budget blocks reliability fixes | High | Low | Allow reliability-improving releases |
| Teams manipulate targets | High | Low | Architecture and business governance |
| SQS queue backlog/oldest-message age misrepresents delay | Medium | Medium | Measure event processing time |
| Retry hides dependency instability | Medium | High | Monitor retries separately |
| Synthetic checks differ from users | Medium | Medium | Combine synthetic and real traffic |
| Error budget excuses data loss | High | Low | Separate durability and integrity objectives |
| Error budget excuses security issue | High | Low | Security governance remains independent |
| Low traffic causes unstable ratios | Medium | Medium | Synthetic or time-based indicators |
| SLO calculations fail after pod restart | Medium | Low | Correct monotonic-counter queries |
| SLO is defined but nobody owns it | High | Medium | Mandatory ownership metadata |
| Reliability dashboard lacks actionability | Medium | Medium | Runbook and review requirements |

---

# 176. Implementation Guidance

The following rules are mandatory:

1. Every production-critical service must define SLOs.
2. Every SLO must have an owner.
3. SLOs must begin from critical user journeys.
4. User-visible outcomes take precedence over infrastructure health.
5. SLIs must define good, bad and excluded events explicitly.
6. The default measurement window is a rolling 30-day period.
7. A 100% SLO requires exceptional justification.
8. Every SLO must define an error budget.
9. Critical SLOs must use burn-rate alerting.
10. Paging alerts must be actionable.
11. Metrics are the preferred SLO calculation source.
12. Structured logs and traces support diagnosis.
13. High-cardinality metric labels are prohibited.
14. HTTP routes must be normalized.
15. SQS SLIs should measure processing time rather than record lag alone.
16. Transactional Outbox publication delay must be measurable.
17. Critical Sagas must define completion objectives.
18. Dependency failures remain part of user-visible reliability when they cause user failure.
19. Internal retries must not each count as user-visible failures.
20. Final operation outcome determines the user-facing SLI.
21. Error-budget policies must influence change risk.
22. Reliability-improving releases remain allowed during budget exhaustion.
23. Security, compliance and data-integrity failures are not excused by error budgets.
24. SLO definitions must be version controlled.
25. SLI query changes require review.
26. SLO dashboards must display error-budget status.
27. Critical alerts require runbooks.
28. New Tier 0 and Tier 1 services require SLOs before production readiness.
29. Load tests must validate that critical SLOs are achievable.
30. Missing telemetry must not automatically be considered success.

---

# 177. Initial Platform SLO Baseline

The following targets represent an initial baseline and must be validated against business requirements before contractual adoption.

| Capability | Indicator | Initial Objective | Window |
|---|---|---:|---|
| Order retrieval | Availability | 99.95% | Rolling 30 days |
| Order retrieval | Latency < 750 ms | 99.0% | Rolling 30 days |
| Order creation | Availability | 99.9% | Rolling 30 days |
| Checkout | Availability | 99.9% | Rolling 30 days |
| Checkout | Latency < 2 s | 99.0% | Rolling 30 days |
| SQS critical-event processing | Processed < 30 s | 99.9% | Rolling 30 days |
| Transactional Outbox | Published < 60 s | 99.99% | Rolling 30 days |
| Critical Saga | Terminal state < 2 min | 99.5% | Rolling 30 days |

These values are engineering baselines, not external SLAs.

---

# 178. Example Checkout SLO

```yaml
service: cart-service
capability: checkout
owner: commerce-platform-team

availability:
  objective: 99.9
  window: 30d
  good:
    - checkout completed
    - accepted asynchronous checkout
  bad:
    - internal error
    - dependency failure preventing checkout
    - timeout
  excluded:
    - invalid request
    - unauthorized request
    - explicit business validation rejection

latency:
  objective: 99.0
  threshold: 2s
  window: 30d
```

---

# 179. Example SQS SLO

```yaml
service: orders-service
capability: order-event-processing
owner: order-platform-team

processing:
  objective: 99.9
  threshold: 30s
  window: 30d

good:
  - processed successfully
  - duplicate safely ignored

bad:
  - terminal processing failure
  - dead-letter after valid event
  - processing exceeded 30s

excluded:
  - invalid unsupported contract
```

Contract violations must also be monitored separately.

---

# 180. Example Outbox SLO

```yaml
service: orders-service
capability: transactional-outbox
owner: order-platform-team

publication:
  objective: 99.99
  threshold: 60s
  window: 30d

start:
  transactionCommittedAt

end:
  brokerAcknowledgedAt
```

---

# 181. Example Saga SLO

```yaml
service: orders-service
capability: order-creation-saga
owner: order-platform-team

completion:
  objective: 99.5
  threshold: 2m
  window: 30d

good:
  - completed

businessOutcome:
  - compensated_due_to_valid_business_rejection

bad:
  - compensation_failed
  - timeout
  - stuck
  - manual_recovery_required
```

Business classification must determine whether successful compensation consumes the SLO error budget.

---

# 182. Validation

The decision will be validated through:

- SLI instrumentation tests
- metric classification tests
- SLO query review
- burn-rate alert tests
- synthetic metric tests
- OpenTelemetry validation
- HTTP integration tests
- SQS integration tests
- Saga integration tests
- Transactional Outbox tests
- failure injection
- chaos experiments
- load testing
- dashboard review
- runbook review
- error-budget policy review
- production-readiness review
- quarterly SLO review

---

# 183. Success Criteria

The decision is successful when:

- every critical service has measurable SLOs
- every SLO has an accountable owner
- critical user journeys are represented
- availability is measured from user outcomes
- latency is measured through meaningful thresholds
- SQS processing delay is measurable
- critical Saga completion is measurable
- outbox publication delay is measurable
- error budgets are visible
- burn-rate alerts identify reliability risk early
- paging alerts correspond to actionable user impact
- infrastructure metrics support diagnosis rather than replace SLOs
- release decisions consider error-budget health
- reliability work can be prioritized quantitatively
- telemetry gaps are visible
- SLO definitions remain version controlled
- reliability trends can be compared across releases

---

# 184. Alternatives Rejected

## 184.1 Infrastructure Health as Reliability

Rejected because healthy infrastructure does not guarantee successful user operations.

---

## 184.2 CPU and Memory Thresholds as Primary Alerts

Rejected because resource utilization alone does not represent user-visible reliability.

---

## 184.3 Average Latency

Rejected as the primary latency measure because averages hide tail latency.

---

## 184.4 100 Percent Availability

Rejected as a general target because distributed systems require realistic reliability trade-offs and error budgets.

---

## 184.5 Alert on Every Error

Rejected because it produces alert fatigue and does not account for service objectives or traffic volume.

---

## 184.6 SQS Queue Backlog/Oldest-Message Age as the Only Messaging SLI

Rejected because record count does not directly represent event-processing delay or user impact.

---

## 184.7 Logs as the Primary SLO Source

Rejected because metrics provide more efficient and reliable aggregation for high-volume SLO calculations.

---

## 184.8 Excluding Dependency Failures

Rejected because users experience the end-to-end service, regardless of which dependency caused the failure.

---

# 185. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-017: Adopt Optimistic Locking for Concurrent Aggregate Updates
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-021: Adopt Zero-Downtime Deployment Practices

---

# 186. References

- Google Site Reliability Engineering
- Google SRE Workbook
- Service Level Objectives
- Error Budgets
- Multi-Window Multi-Burn-Rate Alerts
- OpenTelemetry Specification
- Prometheus Documentation
- Kubernetes Documentation
- Amazon SQS Documentation
- Resilience4j Documentation
- AstraForge Supply Platform Observability Architecture
- AstraForge Supply Platform Reliability Guidelines
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging

---

# 187. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved | Initial SLO and error-budget governance baseline |

---

# 188. Decision Summary

The AstraForge Supply Platform adopts Service-Level Objectives as the standard mechanism for defining production reliability.

The reliability model is:

```text
Critical User Journey

↓

Service-Level Indicator

↓

Service-Level Objective

↓

Error Budget

↓

Burn-Rate Alerting

↓

Reliability Action
```

The platform measures:

```text
Availability

Latency

Correctness

Freshness

SQS Processing Delay

Outbox Publication Delay

Saga Completion
```

The fundamental principle is:

```text
Measure user-visible outcomes,
not merely infrastructure health.
```

The platform does not treat:

```text
Pod Ready

CPU Low

Memory Healthy

SQS Connected
```

as proof that the business capability is reliable.

Every critical SLO must have:

```text
Owner

Objective

Measurement Window

Good / Bad Event Definition

Error Budget

Dashboard

Burn-Rate Alerts

Runbook
```

Error budgets provide an objective mechanism for balancing:

```text
Reliability

and

Change Velocity
```

while security, compliance, durability and data integrity remain independent non-negotiable concerns.

This decision establishes a measurable reliability-engineering model for operating the AstraForge Supply Platform at scale.
