# ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-040 |
| Title | Adopt Production Reliability, Incident Response and Operational Readiness Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Reliability, SRE, Incident Response, SLO, Disaster Recovery, Operational Readiness |
| Related Work Items | Observability, Alerting, On-Call, Runbooks, Backup, Restore, Capacity, DR |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

A production service is not complete merely because it:

```text
Compiles

Passes Tests

Passes Sonar

Passes SAST

Deploys Successfully
```

Production introduces additional engineering requirements:

```text
Traffic

Failures

Latency

Resource Exhaustion

Dependency Outages

Database Incidents

Kafka Backlogs

Redis Failures

AWS Failures

Network Problems

Bad Deployments

Data Corruption

Unexpected Load
```

The complete lifecycle is:

```text
DESIGN
  |
  v
BUILD
  |
  v
TEST
  |
  v
DEPLOY
  |
  v
OPERATE
  |
  v
OBSERVE
  |
  v
RESPOND
  |
  v
LEARN
  |
  v
IMPROVE
```

Operational reliability is therefore an architectural responsibility.

---

# 2. Problem Statement

The platform requires standards defining:

- reliability ownership
- SLI
- SLO
- availability
- latency
- error rate
- alerting
- incident severity
- incident response
- on-call
- runbooks
- escalation
- MTTR
- postmortems
- graceful degradation
- dependency failure
- capacity planning
- saturation
- load testing
- backup
- restore
- disaster recovery
- RTO
- RPO
- operational readiness
- game days
- production diagnostics

---

# 3. Decision Drivers

Primary drivers are:

1. production availability
2. predictable service behavior
3. rapid incident detection
4. reduced MTTR
5. data integrity
6. controlled degradation
7. measurable reliability
8. operational simplicity
9. recovery capability
10. capacity awareness
11. continuous learning
12. business continuity

---

# 4. Decision

The Enterprise Order Platform adopts reliability engineering as part of normal software engineering.

The canonical production model is:

```text
                    USERS / SYSTEMS
                          |
                          v
                       SERVICE
                          |
             +------------+------------+
             |            |            |
             v            v            v
          METRICS        LOGS         TRACES
             |            |            |
             +------------+------------+
                          |
                          v
                     OBSERVABILITY
                          |
                          v
                    SLI / SLO MODEL
                          |
                          v
                       ALERTS
                          |
                          v
                  INCIDENT RESPONSE
                          |
                          v
                       RECOVERY
                          |
                          v
                     POSTMORTEM
                          |
                          v
                    IMPROVEMENT
```

---

# 5. Fundamental Principle

The primary operational rule is:

```text
If a service is important enough to run in production,
it is important enough to define how it fails,
how it is detected,
and how it is recovered.
```

---

# 6. Reliability Ownership

The team that owns a service owns its production reliability.

---

# 7. Ownership Does Not End at Deployment

Service ownership includes:

```text
Design

Implementation

Testing

Deployment

Monitoring

Incident Response

Recovery

Continuous Improvement
```

---

# 8. Production Readiness

Operational readiness must be evaluated before a service becomes production critical.

---

# 9. Observability

Observability must allow engineers to determine:

```text
What is failing?

Where is it failing?

When did it start?

Who/what is affected?

What changed?

How severe is it?
```

---

# 10. Observability Signals

The primary signals are:

```text
METRICS

LOGS

TRACES
```

where supported by the platform.

---

# 11. Metrics

Metrics should represent system behavior quantitatively.

Examples:

```text
Request Rate

Error Rate

Latency

Queue Depth

Connection Pool Usage

Thread/Executor Saturation

Database Pool Usage

Kafka Lag

Cache Hit Ratio
```

---

# 12. Logs

Logs should provide diagnostic context without exposing sensitive data.

Logging follows the platform structured-logging standards.

---

# 13. Traces

Distributed tracing may be used where the platform supports it and where it provides sufficient operational value.

Tracing must not be introduced merely as telemetry volume without a defined diagnostic purpose.

---

# 14. Correlation

Requests and distributed operations should support correlation across relevant service boundaries.

---

# 15. SLI

A Service Level Indicator is a measurable signal representing service behavior.

---

# 16. Example SLI

Examples include:

```text
Successful Request Ratio

p95 Request Latency

Order Processing Success Ratio

Kafka Processing Delay
```

---

# 17. SLO

A Service Level Objective defines the expected reliability target for an SLI.

---

# 18. Example

Conceptually:

```text
SLI:
Successful order creation requests

SLO:
99.9% successful over the defined measurement window
```

---

# 19. SLO Must Be Measurable

Avoid objectives such as:

```text
Service should be fast.
```

Prefer:

```text
99% of eligible requests complete
within the agreed latency threshold.
```

---

# 20. SLO Must Represent User Value

Infrastructure metrics alone do not define service reliability.

For example:

```text
CPU < 70%
```

is not an availability SLO.

---

# 21. Availability

Availability should be measured from successful service outcomes, not merely process uptime.

---

# 22. Process Up Is Not Service Available

This state is possible:

```text
Pod = Running

Application = UP

Database = Reachable

Orders = Failing
```

Therefore technical process health is insufficient.

---

# 23. Latency

Latency SLOs should use percentiles rather than averages.

---

# 24. Average Latency Problem

An average can hide a severely degraded tail.

Prefer:

```text
p95

p99
```

where appropriate.

---

# 25. Error Rate

Expected business rejection must be distinguished from technical failure.

---

# 26. Example

This may be a valid business response:

```text
Order cannot be approved in current status.
```

It should not automatically count as infrastructure failure.

---

# 27. Technical Failure

Examples include:

```text
HTTP 500

Database Timeout

Unexpected Exception

Dependency Timeout

Kafka Processing Failure
```

---

# 28. Error Budget

Where SLO maturity supports it, services should use error budgets.

---

# 29. Error Budget Concept

```text
100% Reliability
       |
       v
SLO Target
       |
       v
Allowed Failure Budget
```

---

# 30. Purpose

Error budgets help balance:

```text
Feature Velocity
       vs
Reliability Work
```

---

# 31. Alerting

Alerts must be actionable.

---

# 32. Actionable Alert

An alert should indicate a condition requiring human or automated intervention.

---

# 33. Non-Actionable Alert

Avoid paging engineers merely because:

```text
one request failed
```

without evidence of meaningful impact.

---

# 34. Alert Fatigue

Too many low-value alerts reduce response quality.

---

# 35. Page vs Notification

Not every signal deserves immediate paging.

Conceptually:

```text
PAGE

TICKET

DASHBOARD

LOG
```

represent different urgency levels.

---

# 36. Page

Paging is reserved for urgent conditions requiring prompt human action.

---

# 37. Ticket

Non-urgent reliability degradation may create normal engineering work.

---

# 38. Dashboard

Dashboards support diagnosis and trend analysis.

They do not replace alerts.

---

# 39. Alert Symptoms

Prefer alerts based on user-visible symptoms.

Examples:

```text
High Error Rate

SLO Burn

Extreme Latency

Processing Backlog
```

---

# 40. Cause Alerts

Infrastructure cause signals remain useful but should be correlated with impact.

---

# 41. CPU Alert

High CPU alone may not justify paging if the service remains healthy.

---

# 42. Saturation Alert

Sustained saturation combined with degradation may require immediate action.

---

# 43. Alert Metadata

An actionable alert should identify:

- service
- environment
- condition
- severity
- current value
- threshold/context
- dashboard
- runbook where available

---

# 44. Runbook

Production-critical alerts should have a practical runbook.

---

# 45. Runbook Purpose

A runbook answers:

```text
What does this alert mean?

What should I inspect?

What is safe to do?

How do I mitigate it?

When should I escalate?
```

---

# 46. Runbook Quality

A runbook must be executable by an engineer who did not write the affected code.

---

# 47. Runbook Contents

Typical content:

```text
Symptoms

Likely Causes

Dashboards

Log Queries

Dependencies

Mitigation

Rollback

Escalation

Recovery Validation
```

---

# 48. Stale Runbook

An incorrect runbook is operational risk.

---

# 49. Runbook Review

Runbooks should be reviewed after incidents and architectural changes.

---

# 50. Incident

An incident is an unplanned event causing or threatening material degradation of production service.

---

# 51. Incident Severity

Incidents require consistent severity classification.

---

# 52. Suggested Severity Model

```text
SEV-1

SEV-2

SEV-3

SEV-4
```

Exact enterprise naming may differ.

---

# 53. SEV-1

Typical characteristics:

```text
Critical business outage

Severe data-integrity risk

Major security impact

Large customer/business impact
```

---

# 54. SEV-2

Typical characteristics:

```text
Major degradation

Important feature unavailable

Significant subset of users affected

No immediate catastrophic data loss
```

---

# 55. SEV-3

Typical characteristics:

```text
Limited degradation

Workaround available

Restricted scope
```

---

# 56. SEV-4

Typical characteristics:

```text
Minor production issue

Low urgency

Little immediate business impact
```

---

# 57. Severity Based on Impact

Severity must reflect impact rather than organizational hierarchy or who reported the issue.

---

# 58. Incident Roles

Major incidents should establish clear roles.

---

# 59. Incident Commander

The Incident Commander coordinates response and decision flow.

---

# 60. Technical Responders

Technical responders investigate and execute mitigation/recovery.

---

# 61. Communications

A designated communication path should provide status to relevant stakeholders.

---

# 62. Role Separation

For major incidents, one engineer should not simultaneously be expected to:

```text
debug

coordinate

communicate

document everything
```

when sufficient responders are available.

---

# 63. Incident Priorities

During an incident:

```text
1. Protect people/data/security

2. Reduce business impact

3. Restore service

4. Determine root cause

5. Improve system
```

Root-cause perfection is not required before mitigation.

---

# 64. Mitigation vs Resolution

Mitigation reduces impact.

Resolution removes the underlying defect.

---

# 65. Example

```text
Disable failing integration
```

may mitigate an incident while the underlying defect remains unresolved.

---

# 66. Recovery

Recovery must be validated using service behavior, not merely deployment success.

---

# 67. Recovery Validation

Verify applicable:

```text
Error Rate Normal

Latency Normal

Backlog Recovering

Business Transactions Succeed

Data Integrity Preserved
```

---

# 68. Incident Timeline

Major incidents should preserve a timeline of meaningful events.

---

# 69. Timeline Examples

```text
21:05 alert fired

21:09 incident declared

21:15 deployment identified

21:22 rollback initiated

21:28 error rate normalized
```

---

# 70. MTTR

Mean Time to Restore/Recover is an important operational metric.

---

# 71. MTTR Goal

The goal is not merely to fix bugs faster.

The architecture should make recovery faster.

---

# 72. Recovery-Friendly Architecture

Examples:

```text
Feature Flags

Rollback

Circuit Breakers

Kill Switches

Immutable Releases

Runbooks

Good Observability
```

---

# 73. MTTD

Mean Time to Detect should also be monitored where useful.

---

# 74. Detection Delay

An incident that begins at:

```text
01:00
```

but is detected at:

```text
08:00
```

has a reliability problem even if remediation takes only five minutes.

---

# 75. On-Call

Production-critical systems require an explicit support/on-call model.

---

# 76. On-Call Responsibility

On-call engineers require:

- access
- documentation
- dashboards
- alerts
- runbooks
- escalation paths

---

# 77. Access During Incident

Emergency production access must follow security and audit policies.

---

# 78. On-Call Without Access

Paging someone who lacks the ability to diagnose or mitigate the issue is ineffective.

---

# 79. Escalation

Escalation paths must be defined for:

- application
- database
- cloud/infrastructure
- security
- external vendors
- business ownership

---

# 80. Dependency Failure

External dependencies must be assumed to fail.

---

# 81. Dependency Failure Modes

Examples:

```text
Timeout

Connection Failure

5xx

Rate Limit

Invalid Response

Slow Response

Partial Outage
```

---

# 82. Timeout

All remote calls require finite timeout behavior.

---

# 83. Retry

Retries must be bounded and appropriate to operation semantics.

---

# 84. Retry Amplification

A degraded dependency plus aggressive retries can create:

```text
Original Traffic
      +
Retries
      =
Dependency Collapse
```

---

# 85. Circuit Breaker

Circuit breakers should protect applicable remote dependencies according to the resilience ADR.

---

# 86. Bulkhead

Concurrency limits prevent one dependency from consuming all service resources.

---

# 87. Graceful Degradation

Where business semantics allow, non-critical dependency failure should not necessarily cause complete service failure.

---

# 88. Example

If notification delivery fails after a durable business transaction:

```text
Order Creation
      |
      +--> SUCCESS

Notification
      |
      +--> RETRY / ASYNC FAILURE
```

the order may remain successful if notification is not transactional to order creation.

---

# 89. Critical Dependency

Critical dependencies may make the operation unavailable.

This must be explicit.

---

# 90. Fallback

Fallbacks must preserve correct business semantics.

---

# 91. Fake Success

Never return successful business results merely because a fallback suppressed an actual critical failure.

---

# 92. Cached Fallback

Cached/stale data may be acceptable only where domain semantics permit it.

---

# 93. Failure Isolation

Failure in one integration should not unnecessarily exhaust:

```text
HTTP Threads

Virtual Threads

Database Connections

Connection Pools

Memory
```

---

# 94. Capacity

Capacity planning is part of reliability.

---

# 95. Capacity Signals

Relevant resources include:

```text
CPU

Memory

Database Connections

HTTP Connections

Kafka Consumers

Queue Depth

Thread/Task Concurrency

Disk

Network
```

---

# 96. Utilization vs Saturation

High utilization is not automatically a problem.

Saturation means demand exceeds effective service capacity.

---

# 97. Headroom

Critical services require reasonable capacity headroom.

---

# 98. Normal Peak

Capacity must consider peak traffic rather than only average traffic.

---

# 99. Growth

Capacity planning should consider expected business growth.

---

# 100. Load Testing

High-volume critical paths require representative load testing where feasible.

---

# 101. Load Test Objective

Load tests should answer questions such as:

```text
What throughput can the service sustain?

Where does saturation begin?

What happens under overload?

How does latency degrade?
```

---

# 102. Load Test Is Not Benchmark Theater

A large requests-per-second number without production relevance is not useful.

---

# 103. Production-Like Dataset

Database performance testing should consider representative data volume.

---

# 104. Connection Pool

Database connection pools require explicit sizing.

---

# 105. More Connections Is Not Always Better

Increasing database connections can move saturation from application to database.

---

# 106. HTTP Pool

Outbound connection pools require sizing and timeout configuration.

---

# 107. Virtual Threads

Virtual threads reduce thread-management cost but do not create unlimited downstream capacity.

---

# 108. Critical Rule

```text
Virtual Threads
       !=
Infinite Database Connections

Virtual Threads
       !=
Infinite HTTP Connections

Virtual Threads
       !=
Infinite External API Capacity
```

---

# 109. Concurrency Boundaries

Concurrency must remain bounded around scarce downstream resources.

---

# 110. Backpressure

Systems processing asynchronous workloads require backpressure or bounded consumption behavior.

---

# 111. Kafka Lag

Kafka consumer lag is an important operational signal.

---

# 112. Lag Interpretation

Lag alone requires context:

```text
Incoming Rate

Processing Rate

Age of Oldest Message

Business SLA
```

---

# 113. Growing Lag

Continuously growing lag indicates the consumer cannot keep pace.

---

# 114. Poison Message

A poison message must not indefinitely block partition progress.

---

# 115. DLQ

Dead-letter/recovery strategy should prevent repeated uncontrolled failure loops.

---

# 116. Redis Failure

Redis/cache failure behavior must follow cache criticality.

---

# 117. Cache as Optimization

If Redis is only an optimization:

```text
Redis Down
    |
    v
Fallback to Source
```

may be appropriate.

---

# 118. Cache Stampede

Cache failure/expiry must not create uncontrolled load against the source system.

---

# 119. Database Reliability

Database reliability requires:

- connection management
- transaction control
- indexing
- backup
- restore
- capacity
- migration discipline

---

# 120. Database Timeout

Database operations should not wait indefinitely.

---

# 121. Long Transaction

Long transactions increase:

- locking
- resource retention
- rollback cost
- contention

and should be minimized.

---

# 122. Lock Monitoring

Critical database workloads require visibility into blocking/lock contention where supported.

---

# 123. Backup

Production data requiring recovery must have an approved backup strategy.

---

# 124. Backup Is Not Recovery

A successful backup job does not prove that recovery works.

---

# 125. Restore Testing

Restore procedures must be tested periodically according to criticality.

---

# 126. RPO

Recovery Point Objective defines acceptable data loss measured in time.

Example:

```text
RPO = 15 minutes
```

means recovery may tolerate up to the defined recent data-loss window according to the business agreement.

---

# 127. RTO

Recovery Time Objective defines the target time to restore service after a qualifying disaster.

Example:

```text
RTO = 2 hours
```

---

# 128. RTO vs RPO

```text
RTO
=
How long can recovery take?

RPO
=
How much recent data loss is acceptable?
```

---

# 129. Business Ownership

RTO and RPO must reflect business requirements rather than arbitrary engineering preference.

---

# 130. Disaster Recovery

Critical services require a disaster-recovery strategy appropriate to their RTO/RPO.

---

# 131. Disaster Scenarios

Planning should consider applicable scenarios such as:

```text
Database Loss

Region Failure

Credential Compromise

Data Corruption

Critical Dependency Loss

Deployment Failure
```

---

# 132. DR Documentation

Disaster recovery requires documented procedures.

---

# 133. DR Test

A DR plan that has never been exercised provides limited assurance.

---

# 134. Game Day

Game days intentionally exercise controlled failure scenarios.

---

# 135. Game Day Goals

Examples:

```text
Validate Runbook

Validate Alert

Measure Recovery Time

Identify Missing Access

Test Dependency Failure

Test Restore Procedure
```

---

# 136. Safe Exercise

Game days must be designed to avoid uncontrolled production harm.

---

# 137. Chaos Engineering

Chaos engineering may be introduced where system maturity justifies it.

---

# 138. Chaos Is Not Random Destruction

A chaos experiment requires:

```text
Hypothesis

Controlled Failure

Observation

Abort Condition

Learning
```

---

# 139. Postmortem

Material incidents require post-incident analysis.

---

# 140. Postmortem Objective

The objective is system improvement, not assigning personal blame.

---

# 141. Human Error

Stopping at:

```text
Engineer made a mistake.
```

is insufficient root-cause analysis.

---

# 142. Better Questions

Ask:

```text
Why was the mistake possible?

Why was it not detected?

Why did it reach production?

Why was recovery difficult?

Which control was missing?
```

---

# 143. Root Cause

Complex incidents may have multiple contributing causes rather than one simplistic root cause.

---

# 144. Postmortem Contents

A useful postmortem includes:

- impact
- timeline
- detection
- contributing factors
- mitigation
- recovery
- what worked
- what failed
- action items

---

# 145. Action Items

Postmortem actions should be:

- concrete
- owned
- prioritized
- trackable

---

# 146. Weak Action

Avoid:

```text
Be more careful next time.
```

---

# 147. Stronger Action

Prefer:

```text
Add automated validation preventing duplicate migration versions.
```

---

# 148. Recurring Incident

Repeated incidents with the same failure mode indicate unresolved systemic risk.

---

# 149. Incident Learning

Incident learnings should influence:

- architecture
- tests
- alerts
- runbooks
- deployment
- capacity
- security

---

# 150. Change Correlation

Operational tooling should make recent deployments/configuration changes visible during incident investigation.

---

# 151. Release Marker

Dashboards should support correlation with deployment version/time where possible.

---

# 152. Operational Metadata

Operators should be able to identify:

```text
Service Version

Git Revision

Deployment Time

Environment
```

through approved mechanisms.

---

# 153. Health Endpoint

Health endpoints must provide useful operational status without leaking sensitive internals.

---

# 154. Liveness

Liveness indicates whether the process should continue running.

---

# 155. Readiness

Readiness indicates whether the instance should receive traffic.

---

# 156. Dependency Outage and Liveness

External dependency failure should generally not cause liveness restart loops.

---

# 157. Startup

Startup dependencies should fail predictably when mandatory configuration/infrastructure is unavailable.

---

# 158. Graceful Shutdown

Services must support graceful shutdown.

---

# 159. Shutdown Goals

During shutdown:

```text
Stop Accepting New Work

Complete/Terminate In-Flight Work Safely

Release Resources

Exit
```

---

# 160. Kubernetes Termination

Shutdown behavior must fit orchestration termination/grace periods.

---

# 161. Kafka Shutdown

Consumers should stop consumption and commit/handle work according to delivery semantics.

---

# 162. HTTP Shutdown

Instances should be removed from traffic before termination where supported.

---

# 163. In-Flight Requests

Abrupt termination should not unnecessarily fail valid in-flight requests.

---

# 164. Idempotency

Retry/recovery paths require idempotency where duplicate execution is possible.

---

# 165. Duplicate Processing

Distributed systems must assume:

```text
Message Delivered Twice

HTTP Request Retried

Job Restarted
```

can occur.

---

# 166. Exactly Once

Do not assume global exactly-once behavior without explicit technical guarantees.

---

# 167. Idempotency Key

Business operations vulnerable to duplicate submission may use idempotency keys.

---

# 168. Reconciliation

Critical distributed workflows should support reconciliation where partial failures can create inconsistent state.

---

# 169. Scheduled Reconciliation

Where appropriate, reconciliation jobs may detect:

```text
Missing Events

Stuck Orders

Incomplete Integration

State Divergence
```

---

# 170. Manual Recovery

Manual recovery tools must be safe, authorized and auditable.

---

# 171. Admin Endpoint

Administrative recovery endpoints require strong authorization and should not be exposed as ordinary public APIs.

---

# 172. Data Repair

Production data repair must preserve:

- auditability
- business invariants
- referential integrity

---

# 173. Direct SQL Repair

Direct production SQL should be a controlled exception, not the normal recovery mechanism.

---

# 174. Operational Simplicity

Prefer architectures that are understandable under incident pressure.

---

# 175. Cleverness Cost

A technically clever mechanism that nobody can safely operate at 03:00 increases production risk.

---

# 176. Dependency Map

Critical services should maintain an understandable dependency map.

Example:

```text
Orders
  |
  +--> PostgreSQL
  +--> Kafka
  +--> Customers
  +--> Products
  +--> Workflow
  +--> Audit
```

---

# 177. Criticality Classification

Dependencies should be classified according to operational importance.

Example:

```text
CRITICAL

HIGH

MEDIUM
```

---

# 178. Dependency Criticality

Criticality should influence:

- timeout
- circuit breaker
- fallback
- alerting
- degradation strategy

---

# 179. External Vendor

Third-party dependencies require documented operational contacts/escalation where business critical.

---

# 180. SLA vs SLO

External SLA and internal SLO are related but not identical.

---

# 181. SLA

An SLA is a formal service commitment.

---

# 182. SLO

An SLO is an engineering reliability objective.

---

# 183. Operational Dashboard

Production-critical services should have dashboards covering relevant golden signals.

---

# 184. Golden Signals

A useful baseline is:

```text
Latency

Traffic

Errors

Saturation
```

---

# 185. Business Metrics

Technical dashboards should be complemented by critical business signals.

Examples:

```text
Orders Created

Orders Approved

Checkout Failures

Integration Processing Rate
```

---

# 186. Business Signal Benefit

A system can appear technically healthy while failing its business purpose.

---

# 187. Cardinality

Metrics must avoid uncontrolled high-cardinality labels.

---

# 188. Dangerous Metric Labels

Avoid labels such as arbitrary:

```text
customerId

orderId

requestId
```

when they create unbounded cardinality.

---

# 189. Logs for High-Cardinality Detail

High-cardinality diagnostic identifiers usually belong in logs/traces rather than metric dimensions.

---

# 190. Monitoring Cost

Observability has infrastructure and operational cost.

Instrumentation must remain purposeful.

---

# 191. Telemetry Volume

More telemetry is not automatically better observability.

---

# 192. Signal Quality

Prefer:

```text
Useful Signal
```

over:

```text
Maximum Data Volume
```

---

# 193. Operational Readiness Review

A new production-critical service requires an operational readiness review.

---

# 194. Operational Readiness Gate

A service is not production ready until:

```text
[ ] Service owner identified

[ ] Production support model defined

[ ] Critical dependencies identified

[ ] Dependency criticality classified

[ ] SLI defined

[ ] SLO defined where applicable

[ ] Availability measurement defined

[ ] Latency measurement defined

[ ] Error-rate measurement defined

[ ] Business success metrics defined

[ ] Dashboards available

[ ] Alerts actionable

[ ] Alert severity defined

[ ] Runbooks available

[ ] Escalation path documented

[ ] Deployment/version visible

[ ] Structured logging validated

[ ] Sensitive logging reviewed

[ ] Correlation supported

[ ] Timeouts configured

[ ] Retry policy reviewed

[ ] Circuit breakers reviewed

[ ] Concurrency limits reviewed

[ ] Graceful degradation reviewed

[ ] Capacity limits understood

[ ] Database pool sized

[ ] HTTP pools sized

[ ] Kafka capacity reviewed

[ ] Load testing completed where required

[ ] Graceful shutdown validated

[ ] Idempotency reviewed

[ ] Duplicate-processing behavior reviewed

[ ] Backup strategy defined

[ ] Restore procedure defined

[ ] Restore tested according to criticality

[ ] RPO defined

[ ] RTO defined

[ ] Disaster recovery documented

[ ] Rollback/roll-forward documented

[ ] Manual recovery procedures secured

[ ] Incident severity model understood

[ ] On-call access validated

[ ] Game day performed where required
```

---

# 195. Incident Readiness Checklist

For a major incident, responders should quickly establish:

```text
[ ] Incident severity

[ ] Incident commander

[ ] Technical responders

[ ] Communication channel

[ ] Impact scope

[ ] Start time

[ ] Recent deployments

[ ] Recent configuration changes

[ ] Error-rate behavior

[ ] Latency behavior

[ ] Dependency status

[ ] Database status

[ ] Kafka lag

[ ] Resource saturation

[ ] Mitigation options

[ ] Rollback safety

[ ] Data-integrity risk

[ ] Stakeholder communication

[ ] Recovery validation
```

---

# 196. Anti-Patterns

The following are prohibited or strongly discouraged:

- considering deployment success equivalent to production readiness
- monitoring only CPU and memory
- using process uptime as the sole availability measure
- alerting on every isolated error
- paging on non-actionable conditions
- alerts without ownership
- alerts without diagnostic context
- stale runbooks
- root-cause analysis based only on "human error"
- postmortems focused on blame
- action item "be more careful"
- infinite retries
- retry storms
- infinite timeouts
- fake-success fallbacks
- treating virtual threads as infinite downstream capacity
- unbounded asynchronous processing
- ignoring continuously growing Kafka lag
- poison messages blocking processing indefinitely
- assuming backup success proves restore capability
- undefined RTO/RPO for critical services
- DR plans never exercised
- direct SQL as routine production recovery
- unaudited administrative recovery
- abrupt shutdown without workload consideration
- assuming exactly-once behavior without guarantees
- uncontrolled metric cardinality
- collecting telemetry without operational purpose
- on-call without required production access
- repeatedly accepting the same incident without systemic remediation

---

# 197. Positive Consequences

The decision provides:

- measurable reliability
- faster incident detection
- lower MTTR
- better operational ownership
- actionable alerting
- safer degradation
- stronger capacity planning
- tested recovery capability
- improved disaster readiness
- better incident learning
- reduced repeated failures
- clearer production readiness

---

# 198. Negative Consequences

The decision introduces:

- SLO definition effort
- dashboard maintenance
- alert tuning
- runbook maintenance
- on-call responsibility
- capacity testing
- backup/restore exercises
- DR exercises
- postmortem work
- operational tooling cost

These costs are accepted because production reliability cannot be achieved solely through pre-production testing.

---

# 199. Neutral Consequences

The decision also means:

- 100% availability is not the universal objective
- not every error should page an engineer
- not every dependency failure should fail the entire service
- rollback is not always safer than roll-forward
- backups require restore testing
- high utilization is not automatically unhealthy
- business metrics are required alongside technical metrics
- observability quality matters more than raw telemetry volume

---

# 200. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Incident detected late | Critical | Medium | SLI/SLO alerts |
| Alert fatigue | High | High | Actionable alert policy |
| Dependency outage | High | High | Timeout/CB/degradation |
| Retry storm | Critical | Medium | Bounded retries |
| Capacity exhaustion | Critical | Medium | Saturation monitoring |
| Kafka backlog | High | Medium | Lag monitoring/scaling |
| Database saturation | Critical | Medium | Pool/capacity management |
| Failed recovery | Critical | Low | Restore/DR testing |
| Data loss | Critical | Low | Backup + RPO |
| Long outage | Critical | Medium | RTO + runbooks |
| Repeated incidents | High | Medium | Postmortem actions |
| Context leakage in metrics | Medium | Medium | Cardinality governance |
| Operational complexity | High | Medium | Runbooks/simplification |

---

# 201. Implementation Guidance

The following rules are mandatory:

1. Production reliability is owned by the service team.
2. Critical services require measurable operational indicators.
3. SLOs must represent meaningful service outcomes.
4. Availability must not be measured solely by process uptime.
5. Latency objectives should use meaningful percentiles.
6. Business rejections must be distinguishable from technical failures.
7. Alerts must be actionable.
8. Paging must be reserved for conditions requiring prompt action.
9. Production-critical alerts require practical runbooks.
10. Major incidents require clear coordination.
11. Incident severity must be based on impact.
12. Mitigation takes precedence over perfect root-cause analysis during active impact.
13. Recovery must be validated through service/business behavior.
14. Incident timelines must be preserved for material incidents.
15. Production-critical systems require an explicit support/on-call model.
16. Remote dependencies require finite timeouts.
17. Retries must be bounded.
18. Retry amplification must be considered.
19. Critical integrations require appropriate resilience controls.
20. Fallbacks must preserve business correctness.
21. Scarce resources require bounded concurrency.
22. Virtual threads do not remove downstream capacity limits.
23. Capacity planning must consider peak traffic and growth.
24. Critical high-volume paths require representative performance testing.
25. Kafka lag must be monitored according to business processing requirements.
26. Poison messages must not create infinite processing loops.
27. Cache failure must have defined semantics.
28. Production data requires an approved backup strategy.
29. Critical restore procedures must be tested.
30. Critical services require explicit RTO/RPO.
31. Disaster-recovery plans must be exercised according to criticality.
32. Material incidents require postmortems.
33. Postmortems focus on system improvement rather than personal blame.
34. Postmortem actions require ownership and tracking.
35. Services must support graceful shutdown.
36. Duplicate execution must be considered in distributed workflows.
37. Critical distributed processes should support reconciliation where appropriate.
38. Administrative recovery mechanisms require authorization and audit.
39. Metrics must avoid uncontrolled cardinality.
40. Telemetry must provide operational value rather than volume alone.
41. New critical services require operational readiness review.

---

# 202. Validation

This ADR will be validated through:

- SLO reviews
- alert reviews
- runbook reviews
- incident exercises
- postmortems
- load tests
- capacity tests
- backup validation
- restore tests
- disaster-recovery exercises
- game days
- production readiness reviews
- incident metrics
- availability metrics
- latency metrics
- error-budget analysis
- operational audits

---

# 203. Success Criteria

The decision is successful when:

- incidents are detected quickly
- alerts correlate with real impact
- responders understand what to do
- MTTR decreases
- recurring incidents decrease
- dependency failures remain isolated where possible
- services degrade predictably
- capacity limits are understood before production saturation
- Kafka backlog is detected before business SLA violation
- backup restoration is proven
- RTO/RPO can be demonstrated
- major incidents generate actionable improvements
- running versions can be correlated with deployments
- business health is observable alongside technical health
- production teams can recover systems safely under pressure

---

# 204. Alternatives Rejected

## 204.1 Monitoring Infrastructure Only

Rejected because infrastructure can appear healthy while business operations fail.

---

## 204.2 Alert on Every Error

Rejected because alert fatigue reduces response effectiveness.

---

## 204.3 Infinite Retry for Reliability

Rejected because retries can amplify dependency outages.

---

## 204.4 Backup Without Restore Testing

Rejected because backup existence does not demonstrate recoverability.

---

## 204.5 Human Error as Root Cause

Rejected because it fails to identify missing systemic controls.

---

## 204.6 100% Availability as Universal Target

Rejected because reliability objectives must reflect business requirements and engineering tradeoffs.

---

## 204.7 Unlimited Concurrency with Virtual Threads

Rejected because downstream resources remain finite.

---

## 204.8 Maximum Telemetry Collection

Rejected because telemetry volume without useful signals increases cost and operational complexity.

---

# 205. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-009: Use Apache Kafka for Integration Events
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards

---

# 206. References

- Google Site Reliability Engineering
- Google SRE Workbook
- DORA
- NIST
- OWASP
- Spring Boot Actuator
- Kubernetes Health Probes
- Apache Kafka Operations
- PostgreSQL Operations
- AWS Well-Architected Framework
- OpenTelemetry
- Resilience4j
- ITIL Incident Management

---

# 207. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial production reliability and operational readiness baseline |

---

# 208. Decision Summary

The definitive production lifecycle is:

```text
                   DEPLOY
                      |
                      v
                   OBSERVE
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
     METRICS         LOGS         TRACES
        |             |             |
        +-------------+-------------+
                      |
                      v
                   SLI/SLO
                      |
                      v
                    ALERT
                      |
                      v
                  INCIDENT
                      |
                      v
                  MITIGATE
                      |
                      v
                   RECOVER
                      |
                      v
                  VALIDATE
                      |
                      v
                 POSTMORTEM
                      |
                      v
                   IMPROVE
```

The health model is:

```text
POD RUNNING
     !=
SERVICE HEALTHY
     !=
BUSINESS HEALTHY
```

The preferred monitoring hierarchy is:

```text
BUSINESS OUTCOME
       |
       v
SERVICE SLI
       |
       v
APPLICATION SIGNALS
       |
       v
INFRASTRUCTURE
```

rather than diagnosing reliability exclusively from:

```text
CPU

MEMORY

POD STATUS
```

For dependencies:

```text
                 REMOTE CALL
                     |
                     v
                   TIMEOUT
                     |
                     v
              BOUNDED RETRY?
                 /       \
               YES        NO
                |          |
                v          v
          SAFE RETRY    FAIL FAST
                |
                v
          CIRCUIT BREAKER
                |
                v
             BULKHEAD
                |
                v
       GRACEFUL DEGRADATION
             IF VALID
```

For concurrency:

```text
                JAVA 21
                   |
                   v
            VIRTUAL THREADS
                   |
                   v
          CHEAPER CONCURRENCY
                   |
                   v
             BUT STILL:
                   |
       +-----------+-----------+
       |           |           |
       v           v           v
   DATABASE      HTTP       EXTERNAL
 CONNECTIONS   CONNECTIONS    API
       |           |           |
       +-----------+-----------+
                   |
                   v
               FINITE
```

Therefore:

```text
Virtual Threads
      +
No Concurrency Control
      !=
Scalability
```

For disaster recovery:

```text
                 DISASTER
                    |
                    v
              DETECT / DECLARE
                    |
                    v
               DR PROCEDURE
                    |
          +---------+---------+
          |                   |
          v                   v
       RESTORE              FAILOVER
          |                   |
          +---------+---------+
                    |
                    v
             VALIDATE DATA
                    |
                    v
             VALIDATE SERVICE
                    |
                    v
             RESUME BUSINESS
```

with:

```text
RPO = acceptable data-loss window

RTO = acceptable recovery-time window
```

For incidents:

```text
INCIDENT
   |
   v
IMPACT
   |
   v
SEVERITY
   |
   v
COORDINATE
   |
   v
MITIGATE
   |
   v
RECOVER
   |
   v
LEARN
```

not:

```text
INCIDENT
   |
   v
FIND SOMEONE TO BLAME
```

A useful postmortem asks:

```text
Why was this failure possible?

Why was it not detected earlier?

Why did existing controls not stop it?

Why was recovery difficult?

What systemic change prevents recurrence?
```

The final operational principle is:

```text
Reliability is not:

"the service usually works."

Reliability is:

we know what correct behavior means,
we measure it,
we detect meaningful degradation,
we contain failures,
we recover predictably,
and we improve after incidents.
```

The desired production state is:

```text
OBSERVABLE
    +
MEASURABLE
    +
BOUNDED
    +
RESILIENT
    +
RECOVERABLE
    +
OPERABLE
    +
CONTINUOUSLY IMPROVING
```

And the complete engineering lifecycle established through the ADR sequence becomes:

```text
ARCHITECTURE
     |
     v
IMPLEMENTATION
     |
     v
QUALITY
     |
     v
SECURITY
     |
     v
SUPPLY CHAIN
     |
     v
CI/CD
     |
     v
PRODUCTION
     |
     v
OBSERVABILITY
     |
     v
INCIDENT LEARNING
     |
     +------------------+
     |                  |
     +----> ARCHITECTURE
```
