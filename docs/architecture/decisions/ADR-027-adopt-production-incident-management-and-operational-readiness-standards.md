# ADR-027: Adopt Production Incident Management and Operational Readiness Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-027 |
| Title | Adopt Production Incident Management and Operational Readiness Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Production Operations, Incident Management, SRE, Reliability, Disaster Recovery |
| Related Work Items | Production Readiness Review, SLO, On-Call, Runbooks, Incident Response, Postmortem, Game Days |
| Supersedes | None |
| Superseded By | ADR-040 |

---

# 1. Context

The AstraForge Supply Platform is a distributed production system composed of:

- Java 21 services
- Spring Boot applications
- PostgreSQL
- Apache Kafka
- transactional outbox
- Redis
- Kubernetes
- external HTTP integrations
- Resilience4j
- OpenTelemetry
- structured logging
- CI/CD
- GitOps
- external secret management
- workload identity

The architecture already establishes standards for:

- service boundaries
- persistence
- messaging
- resilience
- observability
- SLOs
- zero-downtime deployment
- API security
- software supply-chain security
- Kubernetes runtime security
- configuration and secret management

However, architecture is incomplete if the platform cannot be operated reliably during failures.

Production systems inevitably experience:

```text
Application Failure

Dependency Failure

Infrastructure Failure

Configuration Failure

Deployment Failure

Data Failure

Capacity Failure

Security Incident

Human Error
```

The platform therefore requires explicit operational-readiness and incident-management standards.

---

# 2. Problem Statement

The platform requires a standardized operational model that:

- determines whether a service is production ready
- defines service ownership
- establishes on-call responsibility
- defines incident severity
- establishes incident roles
- provides escalation procedures
- standardizes incident communication
- provides actionable alerts
- uses SLOs and error budgets operationally
- supports rapid mitigation
- supports rollback
- supports feature kill switches
- uses resilience mechanisms intentionally during incidents
- provides diagnostic procedures
- covers Kubernetes failures
- covers Kafka failures
- covers PostgreSQL failures
- covers outbox failures
- covers dependency outages
- defines degraded-mode operation
- establishes disaster-recovery expectations
- defines RTO and RPO
- requires runbooks
- requires postmortems
- tracks corrective actions
- validates readiness through game days
- supports controlled chaos engineering
- continuously improves production reliability

---

# 3. Decision Drivers

Primary decision drivers are:

1. customer impact reduction
2. mean time to detect
3. mean time to acknowledge
4. mean time to mitigate
5. mean time to recover
6. operational predictability
7. service ownership
8. production safety
9. availability
10. data integrity
11. observability
12. rapid rollback
13. incident coordination
14. auditability
15. disaster preparedness
16. continuous learning
17. organizational scalability

---

# 4. Decision

The AstraForge Supply Platform adopts a production operations model based on:

```text
Production Readiness

+

Explicit Ownership

+

SLO-Based Monitoring

+

Actionable Alerting

+

Structured Incident Response

+

Rapid Mitigation

+

Runbooks

+

Observability

+

Disaster Recovery

+

Postmortems

+

Continuous Reliability Improvement
```

Operational readiness is a prerequisite for production deployment.

---

# 5. Fundamental Principle

The platform adopts:

```text
A service is not production ready
merely because it compiles,
passes tests,
or can be deployed.

It is production ready only when
it can also be safely operated,
observed,
recovered,
and supported.
```

---

# 6. Production Readiness Review

Every new production service or materially changed critical service must undergo a Production Readiness Review.

---

# 7. PRR Purpose

The Production Readiness Review verifies that the service can be operated safely under realistic production conditions.

---

# 8. PRR Scope

The review should cover:

- architecture
- dependencies
- ownership
- observability
- SLOs
- alerts
- dashboards
- capacity
- scalability
- resilience
- deployment
- rollback
- configuration
- secrets
- security
- data recovery
- runbooks
- incident response
- disaster recovery

---

# 9. PRR Is Not Code Review

Code review answers primarily:

```text
Is this implementation acceptable?
```

Production Readiness Review answers:

```text
Can this system be safely operated in production?
```

---

# 10. Production Readiness Checklist

A service must normally have:

```text
Owner

SLO

Dashboard

Alerts

Runbook

Health Probes

Resource Sizing

Resilience Configuration

Rollback Strategy

Security Controls

Dependency Inventory

Recovery Strategy
```

before production approval.

---

# 11. Service Ownership

Every production service must have an explicit owning team.

---

# 12. Ownership Metadata

Ownership should be discoverable through approved engineering metadata.

Recommended information:

```text
Service

Owning Team

Technical Contact

Repository

Runbook

Dashboard

SLO

Escalation Path
```

---

# 13. No Owner

A production service without an accountable owner is considered an operational defect.

---

# 14. Ownership Scope

The owning team is responsible for:

- code
- configuration
- production behavior
- alerts
- dashboards
- runbooks
- dependency understanding
- incident participation
- corrective actions

---

# 15. Shared Responsibility

Platform teams own shared infrastructure capabilities.

Application teams remain responsible for understanding how their applications use those capabilities.

---

# 16. On-Call

Critical production services require an on-call support model appropriate to business requirements.

---

# 17. On-Call Objective

The purpose of on-call is to ensure that actionable production failures have an accountable responder.

---

# 18. On-Call Rotation

On-call responsibility should be distributed through an explicit rotation.

---

# 19. Primary Responder

The primary responder acknowledges and begins investigation.

---

# 20. Secondary Responder

A secondary responder or escalation path must exist for critical services.

---

# 21. Escalation

Escalation must occur when:

- the primary responder does not acknowledge
- required expertise is missing
- impact increases
- mitigation fails
- incident severity increases

---

# 22. Alert Ownership

Every paging alert must have an owning team.

---

# 23. Unowned Alert

An alert without an owner must not remain in the production paging system.

---

# 24. Incident Severity

Production incidents must use standardized severity classification.

The platform adopts:

```text
SEV1

SEV2

SEV3
```

with lower-impact operational issues handled outside the major-incident process.

---

# 25. SEV1

SEV1 represents critical production impact.

Typical characteristics include:

- major customer-facing outage
- critical business process unavailable
- widespread inability to create/process orders
- severe data integrity risk
- confirmed major security compromise
- unrecoverable or rapidly expanding impact

---

# 26. SEV1 Response

SEV1 requires:

- immediate acknowledgment
- incident commander
- dedicated incident channel/war room
- active mitigation
- stakeholder communication
- executive/business escalation where appropriate
- continuous coordination until stabilized

---

# 27. SEV2

SEV2 represents significant but contained production impact.

Examples:

- substantial degradation
- important functionality unavailable
- significant subset of users affected
- growing processing backlog
- elevated error rate threatening SLO

---

# 28. SEV2 Response

SEV2 requires prompt technical response and escalation according to impact and duration.

---

# 29. SEV3

SEV3 represents limited production impact.

Examples:

- localized degradation
- non-critical functionality affected
- operational issue with workaround
- limited backlog
- issue not currently threatening major SLO consumption

---

# 30. Severity Is Impact-Based

Severity must be determined by:

```text
Actual / Potential Business Impact
```

rather than:

```text
How technically interesting the failure is
```

---

# 31. Severity Change

Incident severity may increase or decrease as impact becomes clearer.

---

# 32. Security Incident

Security incidents must additionally follow enterprise security incident-response procedures.

---

# 33. Incident Lifecycle

The standard incident lifecycle is:

```text
Detect

↓

Acknowledge

↓

Classify

↓

Coordinate

↓

Diagnose

↓

Mitigate

↓

Recover

↓

Verify

↓

Close

↓

Learn
```

---

# 34. Detection

Incidents may be detected through:

- monitoring
- alerting
- customer reports
- business monitoring
- security monitoring
- deployment monitoring
- dependency notifications

---

# 35. Acknowledgment

Paging alerts must be acknowledged within the operational target defined for their severity.

---

# 36. Classification

The initial responder determines provisional severity based on available evidence.

---

# 37. Incident Commander

SEV1 incidents require an Incident Commander.

SEV2 incidents should use one when coordination complexity warrants it.

---

# 38. Incident Commander Responsibility

The Incident Commander coordinates the response.

The role is not necessarily assigned to the engineer with the deepest technical knowledge.

---

# 39. Incident Commander Duties

The Incident Commander manages:

- incident severity
- responder coordination
- workstream assignment
- communication cadence
- escalation
- mitigation decisions
- incident timeline
- recovery confirmation

---

# 40. Technical Lead

A technical lead may coordinate technical diagnosis separately from incident command.

---

# 41. Communications Lead

Large incidents may designate a communications lead.

---

# 42. Scribe

Major incidents should maintain an incident timeline.

A designated scribe may capture:

- events
- observations
- decisions
- actions
- timestamps

---

# 43. War Room

SEV1 incidents require a dedicated collaboration channel or war room.

---

# 44. War Room Objective

The war room provides one authoritative coordination location.

---

# 45. Parallel Investigation

Complex incidents should use explicit workstreams.

Example:

```text
Workstream A
Application

Workstream B
Database

Workstream C
Kafka

Workstream D
Infrastructure
```

---

# 46. Avoid Duplicate Work

The Incident Commander should prevent multiple responders from unknowingly performing the same investigation.

---

# 47. Communication

Incident communication must distinguish:

```text
Known Facts

Hypotheses

Actions

Decisions
```

---

# 48. Hypothesis

A hypothesis must not be communicated as established fact.

---

# 49. Communication Cadence

Major incidents require periodic status updates appropriate to severity.

---

# 50. Status Update

A useful update includes:

```text
Current Impact

Current Severity

What Changed

Mitigation Status

Next Actions

Next Update
```

---

# 51. External Communication

Customer or public communication follows enterprise communication procedures.

---

# 52. Technical Accuracy

External communication must not speculate about unconfirmed root causes.

---

# 53. SLO Integration

ADR-020 SLOs are operational decision inputs.

---

# 54. Error Budget

Error-budget consumption must influence:

- incident priority
- reliability work
- deployment risk
- feature-release decisions

---

# 55. Error Budget Burn

Fast error-budget burn should generate higher urgency than isolated low-impact failures.

---

# 56. Burn-Rate Alerting

SLO alerts should preferably use burn-rate approaches that detect both:

```text
Fast catastrophic consumption
```

and:

```text
Slow sustained degradation
```

---

# 57. SLO vs Infrastructure Metric

An infrastructure metric alone does not necessarily indicate customer impact.

Example:

```text
CPU = 85%
```

does not automatically imply an incident.

---

# 58. Customer-Centric Alerting

Paging should prioritize signals representing:

- availability
- latency
- correctness
- business processing
- SLO consumption

---

# 59. Actionable Alert

Every paging alert must answer:

```text
What failed?

Why does it matter?

Which service?

Which environment?

What should the responder inspect first?

Where is the runbook?
```

---

# 60. Non-Actionable Alert

An alert that routinely requires no action should not page.

---

# 61. Alert Fatigue

Persistent false positives are operational defects.

---

# 62. Warning vs Page

Not every anomaly should page an engineer.

Use:

```text
Dashboard / Warning / Ticket
```

for non-urgent conditions.

Use:

```text
Page
```

for conditions requiring timely human action.

---

# 63. Alert Deduplication

Correlated failures should be deduplicated or grouped where monitoring tooling permits.

---

# 64. Dependency Alert Storm

A shared dependency outage must not generate hundreds of indistinguishable pages to the same responders.

---

# 65. Alert Routing

Alerts must route according to service ownership and severity.

---

# 66. Runbook

Every critical paging alert must reference a runbook.

---

# 67. Runbook Purpose

A runbook provides safe initial diagnostic and mitigation procedures.

---

# 68. Runbook Content

A production runbook should include:

- service purpose
- ownership
- dependencies
- dashboards
- important metrics
- common alerts
- diagnostic steps
- safe mitigation
- rollback procedure
- escalation
- recovery verification

---

# 69. Runbook Commands

Commands must be safe, explicit and environment-aware.

---

# 70. Destructive Commands

Potentially destructive commands must be clearly identified and protected.

---

# 71. Runbook Validation

Runbooks must be periodically exercised.

An untested runbook may be incorrect when needed.

---

# 72. Runbook Drift

Runbooks must evolve when:

- architecture changes
- commands change
- dashboards change
- alerts change
- incidents reveal gaps

---

# 73. Diagnostic Principle

Incident diagnosis should move from broad impact toward evidence-based narrowing.

---

# 74. Initial Questions

Responders should quickly determine:

```text
What is impacted?

When did it start?

What changed?

Is impact growing?

Is it isolated?

Which dependency path is failing?
```

---

# 75. Change Correlation

Recent changes are high-value diagnostic signals.

Inspect:

- deployments
- configuration changes
- feature-flag changes
- secret rotations
- infrastructure changes
- database migrations

---

# 76. Correlation Is Not Causation

A recent deployment is a strong hypothesis but not automatically the root cause.

---

# 77. Observability Triad

Diagnosis uses:

```text
Metrics

+

Logs

+

Traces
```

according to ADR-014 and ADR-019.

---

# 78. Metrics

Metrics answer questions such as:

- when failure started
- magnitude
- affected instances
- latency
- error rate
- saturation
- backlog

---

# 79. Logs

Logs provide detailed event and failure context.

---

# 80. Traces

Distributed traces help identify where latency or failure propagates across service boundaries.

---

# 81. Correlation ID

Requests should remain traceable through correlation identifiers according to platform observability standards.

---

# 82. Sensitive Data

Incident diagnosis must not weaken ADR-023 sensitive-data protections.

---

# 83. Production Data Access

Incident urgency does not automatically authorize unrestricted access to production data.

---

# 84. Mitigation First

During major incidents:

```text
Mitigate customer impact first.

Perform complete root-cause analysis afterward.
```

---

# 85. Mitigation vs Fix

Mitigation restores or protects service.

A permanent fix may occur later.

---

# 86. Example

Valid mitigation:

```text
Disable optional failing feature

↓

Restore core ordering capability
```

Permanent correction can follow after stabilization.

---

# 87. Rollback

Rollback is a first-class mitigation mechanism.

---

# 88. Deployment Rollback

When a deployment strongly correlates with impact and rollback is safe:

```text
Rollback
```

should generally be preferred over debugging indefinitely in production.

---

# 89. Rollback Target

Rollback must use a previously approved immutable artifact.

---

# 90. Database Compatibility

Rollback must respect ADR-006 and ADR-021 database compatibility rules.

---

# 91. Flyway

An already applied Flyway migration must not be modified as an incident shortcut.

Database remediation requires a new migration or an approved recovery procedure.

---

# 92. Rollback Verification

After rollback, verify:

- error rate
- latency
- business processing
- backlog
- SLO
- data integrity

---

# 93. Feature Kill Switch

Risky optional capabilities should use feature kill switches where justified.

---

# 94. Kill Switch Purpose

A kill switch allows:

```text
Disable optional capability

without

redeploying the entire service
```

---

# 95. Safe Default

Kill switches must fail toward safer behavior.

---

# 96. Kill Switch Authorization

Production kill-switch access must be restricted and audited.

---

# 97. Circuit Breaker During Incident

Circuit breakers from ADR-016 are part of incident containment.

---

# 98. Circuit Breaker Objective

When a dependency fails, the circuit breaker should reduce:

- repeated failing calls
- resource exhaustion
- cascading latency
- pressure on the failed dependency

---

# 99. Circuit Breaker Is Not Repair

Opening a circuit does not fix the dependency.

It limits damage.

---

# 100. Manual Circuit Changes

Changing circuit-breaker thresholds during an incident must be controlled.

---

# 101. Retry During Incident

Increasing retries during dependency failure is generally dangerous.

It can amplify the outage.

---

# 102. Retry Storm

A retry storm can produce:

```text
Dependency Failure

↓

Retries

↓

Higher Load

↓

Greater Failure

↓

More Retries
```

---

# 103. Timeout

Reducing excessive dependency timeout may be a valid containment action when evidence supports it.

---

# 104. Bulkhead

Bulkheads should prevent one failing dependency from consuming all execution capacity.

---

# 105. Degraded Mode

Critical business services should define whether degraded operation is possible.

---

# 106. Degraded Mode Example

If an optional recommendation service fails:

```text
Core Order Creation
→ continues

Recommendations
→ unavailable
```

---

# 107. Critical Dependency

If correctness requires a dependency, degraded mode must not fabricate success.

---

# 108. Fail Closed

Security-sensitive operations must fail closed where required.

---

# 109. Fail Open

Fail-open behavior requires explicit risk acceptance.

---

# 110. Fallback

Fallback responses must preserve business correctness.

---

# 111. Stale Data

Using stale cached data during dependency failure is permitted only where business semantics allow it.

---

# 112. Kubernetes Troubleshooting

Runbooks must cover common Kubernetes failure states.

---

# 113. CrashLoopBackOff

Investigate:

- application startup exception
- invalid configuration
- missing secret
- dependency initialization
- memory failure
- probe configuration

---

# 114. OOMKilled

Investigate:

- heap usage
- native memory
- direct buffers
- thread count
- memory leak
- workload growth
- container limit

---

# 115. Pending Pod

Investigate:

- resource availability
- quota
- affinity
- topology constraints
- taints/tolerations
- persistent volume availability

---

# 116. Readiness Failure

Investigate:

- application health
- dependency required for traffic
- startup state
- probe timeout
- probe endpoint

---

# 117. Liveness Failure

Repeated liveness failures require investigation.

Increasing probe thresholds blindly is not the default remediation.

---

# 118. Pod Restart

A pod restart may temporarily mitigate a process-level issue.

It is not a root-cause fix.

---

# 119. Replica Failure

If only one replica fails while others remain healthy, compare:

- node
- configuration
- resource consumption
- pod events
- dependency connectivity

---

# 120. Node Failure

Kubernetes should reschedule workloads according to cluster capabilities.

Operational monitoring must verify required capacity is restored.

---

# 121. Deployment Stuck

Investigate:

- readiness
- image pull
- admission policy
- scheduling
- resource limits
- missing configuration
- missing secret

---

# 122. ImagePullBackOff

Investigate:

- image reference
- registry availability
- image existence
- credentials
- admission/security policy

---

# 123. Admission Rejection

Admission-policy rejection is usually a deployment/configuration defect, not a reason to disable security controls.

---

# 124. Kafka Operational Readiness

Kafka consumers and producers require explicit operational monitoring.

---

# 125. Kafka Consumer Lag

Consumer lag is a critical signal for asynchronous processing.

---

# 126. Lag Interpretation

Increasing lag may indicate:

- consumer failure
- insufficient capacity
- slow dependency
- poison message
- partition imbalance
- database bottleneck

---

# 127. Lag Alert

Lag alerting should consider both:

```text
Backlog Size

and

Backlog Age
```

where possible.

---

# 128. Backlog Age

A large backlog processed quickly may be acceptable.

A smaller backlog containing very old events may be more serious.

---

# 129. Kafka Partition

Partition-level imbalance should be visible when diagnosing consumer lag.

---

# 130. Consumer Group

Runbooks must identify relevant consumer groups.

---

# 131. Poison Message

Repeated failure on one message must not indefinitely block processing without an explicit failure strategy.

---

# 132. Dead-Letter Strategy

Dead-letter handling must follow the platform messaging architecture.

---

# 133. Replay

Message replay must be controlled and account for idempotency.

---

# 134. Duplicate Processing

Consumers must assume duplicate delivery can occur.

---

# 135. Kafka Broker Failure

Application-level incident response should distinguish:

```text
Application Consumer Problem

from

Kafka Infrastructure Problem
```

---

# 136. Producer Failure

Producer errors require inspection of:

- broker connectivity
- authentication
- authorization
- serialization
- topic existence
- timeout
- broker health

---

# 137. Outbox Operational Readiness

Transactional outbox requires dedicated monitoring.

---

# 138. Outbox Backlog

Monitor:

- pending event count
- oldest pending event age
- dispatch failure count
- retry count
- permanently failed events

---

# 139. Outbox Age

Oldest pending event age is often more operationally useful than count alone.

---

# 140. Outbox Incident

Growing outbox backlog may indicate:

- Kafka outage
- dispatcher failure
- authentication failure
- serialization defect
- database contention
- dispatcher capacity shortage

---

# 141. Outbox Data Safety

Do not manually delete pending outbox events merely to reduce backlog.

---

# 142. Outbox Replay

Replay/retry must preserve idempotency and event-ordering requirements.

---

# 143. PostgreSQL Operational Readiness

Database health requires application-level and infrastructure-level signals.

---

# 144. Connection Pool

Monitor:

- active connections
- idle connections
- pending acquisition
- acquisition timeout
- pool exhaustion

---

# 145. Pool Exhaustion

Connection-pool exhaustion may be caused by:

- slow queries
- leaked transactions
- traffic spike
- database latency
- excessive concurrency
- incorrect pool sizing

---

# 146. Pool Size

Increasing the pool blindly can worsen database saturation.

---

# 147. Database Connections

Total database capacity must consider:

```text
Pool Size

×

Number of Replicas

×

Number of Services
```

---

# 148. Query Latency

Slow query diagnosis should inspect:

- execution plan
- indexes
- locks
- row volume
- statistics
- contention

---

# 149. Database Locks

Lock contention can appear as application latency or timeout.

---

# 150. Long Transaction

Long-running transactions should be visible and investigated.

---

# 151. Database CPU

High database CPU requires workload/query diagnosis before scaling or parameter changes.

---

# 152. Database Storage

Storage capacity and growth must be monitored.

---

# 153. Database Replication

If replication is used, replication lag must be monitored according to read/recovery semantics.

---

# 154. Database Failover

Database failover procedures must be documented and tested according to infrastructure architecture.

---

# 155. Migration Failure

A failed schema migration must follow a controlled recovery procedure.

---

# 156. Migration Incident

Do not modify the already executed migration in source control to make history appear successful.

---

# 157. Corrective Migration

Schema correction after an applied migration must use a new migration.

---

# 158. External Dependency Failure

Every critical downstream integration should define expected outage behavior.

---

# 159. Dependency Inventory

The platform should maintain a dependency map including:

- service
- owner
- criticality
- timeout
- retry
- circuit breaker
- fallback
- SLO where known

---

# 160. Dependency Criticality

Dependencies should be classified conceptually as:

```text
Critical

Degradable

Optional
```

---

# 161. Critical Dependency

Failure prevents safe completion of the business operation.

---

# 162. Degradable Dependency

Failure permits reduced functionality.

---

# 163. Optional Dependency

Failure should not prevent the primary business capability.

---

# 164. Dependency Timeout

Timeouts must be shorter than the caller's total request budget.

---

# 165. Timeout Budget

For a synchronous chain:

```text
Client

↓

Service A

↓

Service B

↓

Service C
```

each downstream timeout must respect the remaining end-to-end latency budget.

---

# 166. Dependency Dashboard

Critical dependencies should be visible in service dashboards.

---

# 167. Third-Party Incident

Third-party outages must be tracked separately from internal application defects while still measuring customer impact.

---

# 168. Cache Incident

Redis/cache failures require defined application behavior.

---

# 169. Cache Is Not Source of Truth

Where Redis is a cache, loss of cache must not imply loss of authoritative business data.

---

# 170. Cache Stampede

Cache outage or expiration may generate sudden load on authoritative systems.

Mitigation must account for this risk.

---

# 171. Cache Fallback

Fallback to source systems must be capacity-safe.

---

# 172. Cache Staleness

Stale-cache behavior must respect domain correctness.

---

# 173. Capacity Management

Production readiness requires capacity planning.

---

# 174. Capacity Questions

Teams should understand:

- normal throughput
- peak throughput
- CPU profile
- memory profile
- connection usage
- Kafka throughput
- database load
- scaling limits

---

# 175. Headroom

Critical services require reasonable capacity headroom.

---

# 176. HPA

HPA configuration from ADR-025 must be validated under realistic load.

---

# 177. HPA Maximum

The maximum replica count must consider downstream capacity.

---

# 178. Autoscaling Cascade

Scaling one service can overload a fixed-capacity dependency.

---

# 179. Load Test

Critical paths should undergo load/performance testing appropriate to expected production traffic.

---

# 180. Stress Test

Stress testing should identify behavior beyond expected capacity.

---

# 181. Saturation

The platform must understand failure behavior when resources saturate.

---

# 182. Graceful Degradation Under Load

Where possible, optional work should be reduced before critical business processing fails.

---

# 183. Rate Limiting

Rate limiting may protect services from overload according to API architecture.

---

# 184. Backpressure

Asynchronous systems should use backpressure or bounded processing capacity.

---

# 185. Unbounded Queue

Unbounded in-memory queues are prohibited for production processing.

---

# 186. Thread Exhaustion

Thread or virtual-thread concurrency must still respect downstream capacity.

---

# 187. Virtual Threads

Java virtual threads reduce thread-management cost but do not make:

```text
Database Connections

HTTP Connections

CPU

Memory

Downstream Capacity
```

unlimited.

---

# 188. Concurrency Limit

External dependency concurrency should remain bounded where necessary.

---

# 189. Disaster Recovery

Critical services require a disaster-recovery strategy.

---

# 190. Disaster

A disaster is a failure exceeding ordinary high-availability mechanisms.

Examples:

- region failure
- major database loss
- critical infrastructure corruption
- catastrophic security incident

---

# 191. RTO

Recovery Time Objective defines the targeted maximum time to restore the capability after a disaster.

---

# 192. RPO

Recovery Point Objective defines the targeted maximum acceptable data-loss window.

---

# 193. RTO/RPO Ownership

RTO and RPO must be driven by business criticality, not chosen solely by engineering convenience.

---

# 194. Example Classification

Conceptually:

```text
Critical Ordering Capability
→ low RTO
→ low RPO

Non-Critical Reporting
→ higher RTO may be acceptable
```

Exact values require business agreement.

---

# 195. Backup Is Not DR

Having backups does not by itself establish disaster-recovery capability.

---

# 196. Restore Test

Backups must be restorable.

Restore procedures must be tested periodically.

---

# 197. Recovery Validation

A DR exercise must verify:

- data availability
- application startup
- configuration
- secrets
- connectivity
- Kafka integration
- business correctness

---

# 198. Recovery Dependencies

DR planning must include:

- databases
- Kafka
- secrets
- configuration
- identity
- DNS
- ingress
- certificates
- container registry

---

# 199. Region Dependency

A supposedly multi-region application is not resilient if a critical dependency remains single-region without an accepted risk.

---

# 200. Data Consistency

DR design must explicitly define consistency and data-loss expectations.

---

# 201. Kafka DR

Kafka disaster-recovery strategy must define:

- topic recovery
- replication approach
- consumer offsets
- event replay
- duplicate handling

according to infrastructure capability.

---

# 202. Database DR

Database DR must define:

- backup
- replication
- restore
- failover
- integrity validation

---

# 203. Secret DR

Secret-management infrastructure must be recoverable.

---

# 204. GitOps DR

Declarative configuration repositories are part of recovery capability.

---

# 205. DNS Recovery

DNS changes and propagation behavior must be understood for regional failover.

---

# 206. DR Runbook

Critical services require documented DR procedures.

---

# 207. DR Exercise

Critical DR procedures must be periodically exercised.

---

# 208. Recovery Exercise

A successful exercise should demonstrate actual recovery, not merely review documentation.

---

# 209. Incident Recovery

An incident is not resolved solely because error rates decrease.

---

# 210. Recovery Verification

Before resolution, verify:

- customer functionality
- SLO signals
- backlog recovery
- data integrity
- dependency stability
- replica health

---

# 211. Backlog Recovery

After restoring a dependency, backlog processing can create a second capacity event.

---

# 212. Controlled Drain

Large backlogs should be drained at a rate that does not overload recovered dependencies.

---

# 213. Recovery Observation

Major incidents should remain under observation for an appropriate stabilization period.

---

# 214. Incident Closure

Closure requires:

- impact ended
- mitigation stable
- customer processing verified
- follow-up ownership established

---

# 215. Root Cause Analysis

Root-cause analysis occurs after stabilization unless needed immediately for mitigation.

---

# 216. Postmortem

Significant incidents require a postmortem.

---

# 217. Postmortem Objective

The objective is:

```text
Understand the system

and

Reduce recurrence or impact
```

not to assign personal blame.

---

# 218. Blameless Approach

Human actions should be analyzed in the context of:

- available information
- system design
- tooling
- procedures
- incentives
- safeguards

---

# 219. Accountability

Blameless does not mean absence of accountability.

Corrective actions must have owners and deadlines.

---

# 220. Postmortem Content

A postmortem should include:

- summary
- customer/business impact
- severity
- duration
- detection
- timeline
- technical cause
- contributing factors
- mitigation
- recovery
- what worked
- what did not work
- corrective actions

---

# 221. Root Cause

Avoid superficial root causes such as:

```text
Engineer made mistake.
```

Ask why the system allowed one action to create significant impact.

---

# 222. Five Whys

Techniques such as Five Whys may assist analysis but should not force a single simplistic cause.

---

# 223. Contributing Factors

Distributed-system incidents commonly have multiple contributing factors.

---

# 224. Detection Gap

If customers detected the incident before monitoring, that is an observability gap.

---

# 225. Mitigation Gap

If responders knew the cause but lacked a safe mitigation mechanism, that is an operational-design gap.

---

# 226. Runbook Gap

If responders had to rediscover known procedures, the runbook is incomplete.

---

# 227. Test Gap

If a failure mode could reasonably have been caught before production, testing strategy should be improved.

---

# 228. Architecture Gap

If one component failure caused widespread cascading impact, architecture/resilience controls should be reviewed.

---

# 229. Corrective Action

Corrective actions should address system improvements.

Examples:

- add alert
- remove noisy alert
- add circuit breaker
- reduce timeout
- add capacity
- add test
- improve runbook
- automate rollback
- improve isolation
- add validation
- remove single point of failure

---

# 230. Action Priority

Corrective actions should be prioritized by risk reduction.

---

# 231. Action Owner

Every accepted corrective action must have an owner.

---

# 232. Action Deadline

High-risk corrective actions should have explicit target dates.

---

# 233. Action Tracking

Postmortem actions must be tracked to completion.

---

# 234. Repeated Incident

Repeated incidents with the same unresolved cause indicate failure of the corrective-action process.

---

# 235. Postmortem Distribution

Relevant learnings should be shared across teams where systemic lessons apply.

---

# 236. Postmortem Data

Postmortems must protect sensitive customer, employee and security information.

---

# 237. Game Days

The platform adopts controlled game days for critical services.

---

# 238. Game Day Purpose

A game day validates:

- monitoring
- alerts
- runbooks
- escalation
- resilience
- recovery
- team readiness

---

# 239. Game Day Scenario

Examples:

- downstream API unavailable
- Kafka unavailable
- PostgreSQL failover
- Redis unavailable
- pod termination
- node drain
- expired credential simulation
- configuration rollback
- large outbox backlog

---

# 240. Controlled Execution

Game days must define:

- scope
- owner
- expected impact
- abort conditions
- recovery procedure

---

# 241. Production Game Day

Production exercises require stronger controls than non-production exercises.

---

# 242. Chaos Engineering

Controlled chaos engineering may be used to validate resilience assumptions.

---

# 243. Chaos Principle

Chaos engineering is:

```text
Controlled experimentation
```

not:

```text
Randomly breaking production
```

---

# 244. Hypothesis

A chaos experiment must begin with a hypothesis.

Example:

```text
If one Orders pod is terminated,
customer-facing availability remains within SLO.
```

---

# 245. Blast Radius

Chaos experiments must start with the smallest practical blast radius.

---

# 246. Abort Condition

Experiments require explicit abort conditions.

---

# 247. Observability Requirement

Do not run resilience experiments without sufficient observability to evaluate the result.

---

# 248. Security Controls

Chaos testing must not bypass security controls.

---

# 249. Data Integrity

Experiments must not intentionally risk uncontrolled business-data corruption.

---

# 250. Progressive Reliability Testing

Recommended progression:

```text
Automated Test

↓

Integration Environment

↓

HML

↓

Controlled Production Experiment
```

where justified.

---

# 251. Operational Automation

Repeated safe incident procedures should be automated where practical.

---

# 252. Automation Objective

Automation reduces:

- human error
- response time
- command inconsistency

---

# 253. Dangerous Automation

Automated remediation with large blast radius requires safeguards.

---

# 254. Auto-Restart

Kubernetes already provides process/pod restart behavior.

Additional automatic restart logic should not mask persistent defects.

---

# 255. Auto-Rollback

Automated rollback may be adopted when reliable health criteria exist.

---

# 256. Auto-Rollback Guardrail

A rollback should not occur solely because one noisy metric crosses a threshold.

---

# 257. Security Automation

Security incidents may require automated credential revocation or workload isolation according to enterprise security architecture.

---

# 258. Operational Metrics

The platform should measure operational effectiveness.

---

# 259. MTTD

Mean Time to Detect measures time from incident start to detection.

---

# 260. MTTA

Mean Time to Acknowledge measures time from alert/detection to responder acknowledgment.

---

# 261. MTTM

Mean Time to Mitigate measures time until customer impact is materially reduced.

---

# 262. MTTR

Mean Time to Recover measures time until service is restored.

---

# 263. Metric Interpretation

These metrics should guide improvement rather than individual performance evaluation.

---

# 264. Incident Frequency

Incident frequency and recurring failure categories should be reviewed.

---

# 265. Change Failure Rate

The platform should monitor how frequently deployments/configuration changes cause incidents.

---

# 266. Rollback Rate

Rollback frequency can reveal deployment-quality or validation gaps.

---

# 267. Alert Quality

Alert quality should be measured through:

- actionable percentage
- false-positive rate
- duplicate rate
- acknowledgment behavior

---

# 268. Reliability Review

Teams should periodically review:

- SLO performance
- error-budget consumption
- incidents
- postmortem actions
- alert quality
- capacity
- operational debt

---

# 269. Operational Debt

Operational debt includes:

- missing runbooks
- noisy alerts
- missing dashboards
- unresolved postmortem actions
- undocumented dependencies
- manual recovery procedures
- untested backups

---

# 270. Reliability Work

Reliability improvements are production engineering work, not optional cleanup.

---

# 271. Release Decision

Severe error-budget exhaustion may justify slowing or temporarily stopping risky feature releases.

---

# 272. Reliability Priority

When a service repeatedly violates its SLO, reliability work takes priority over additional feature complexity until risk is reduced.

---

# 273. Operational Documentation

Operational documentation must be stored in an accessible, version-controlled or otherwise governed location.

---

# 274. Documentation Availability

Incident responders must be able to access runbooks during an outage.

---

# 275. Dependency on Failed System

Critical incident documentation must not exist exclusively inside the system likely to be unavailable during the incident.

---

# 276. Access

On-call responders must have necessary production read access before incidents occur.

---

# 277. Just-in-Time Privilege

Elevated write access should remain just-in-time where platform capabilities permit.

---

# 278. Break Glass

Break-glass access follows ADR-025 and must be:

- authenticated
- time bounded
- audited
- reviewed

---

# 279. Incident Access

Incident severity does not justify bypassing auditability.

---

# 280. Security During Incident

Operational urgency must not lead to:

- sharing passwords
- disabling authentication globally
- committing secrets
- exposing sensitive logs
- permanently disabling admission controls

---

# 281. Temporary Security Change

Any emergency reduction of a security control requires:

- explicit risk decision
- narrow scope
- incident documentation
- restoration after stabilization

---

# 282. Production Changes During Incident

Production changes during major incidents must be coordinated through the Incident Commander.

---

# 283. Change Freeze

A major incident may require temporary suspension of unrelated production deployments.

---

# 284. Concurrent Changes

Unrelated changes during an incident complicate diagnosis and increase risk.

---

# 285. Incident Timeline

All significant production changes during an incident should be recorded in the timeline.

---

# 286. Data Correction

Production data correction requires controlled procedures.

---

# 287. Direct SQL

Ad hoc production SQL changes should not be the default incident-response mechanism.

---

# 288. Data Repair

When data repair is required, prefer:

- reviewed script
- bounded scope
- backup/recovery consideration
- audit trail
- verification

---

# 289. Idempotent Repair

Repair procedures should be idempotent where practical.

---

# 290. Backup Before Repair

High-risk data correction should consider an appropriate recovery point before execution.

---

# 291. Business Validation

Technical recovery must include business-level validation.

Example:

```text
HTTP 200
```

does not prove:

```text
Orders are being processed correctly.
```

---

# 292. Synthetic Monitoring

Critical user journeys may use synthetic monitoring.

---

# 293. Business Metrics

Critical workflows should expose business-processing indicators.

Examples:

```text
Orders created

Orders approved

Orders failed

Events pending
```

with appropriate cardinality and data-protection controls.

---

# 294. Business Anomaly

A service may be technically healthy while business throughput is unexpectedly zero.

---

# 295. Operational Dashboard

Critical service dashboards should combine:

```text
Customer Signals

Application Signals

Dependency Signals

Infrastructure Signals

Business Signals
```

---

# 296. Dashboard First View

A responder should be able to determine system health quickly without navigating dozens of unrelated dashboards.

---

# 297. Dashboard Consistency

Services should follow common dashboard conventions where practical.

---

# 298. Dashboard Links

Dashboards should link to:

- logs
- traces
- runbooks
- deployment history

where tooling supports it.

---

# 299. Incident Evidence

Relevant logs, traces and metrics should be preserved according to retention requirements.

---

# 300. Evidence Integrity

Security incidents may require additional evidence-preservation procedures.

---

# 301. Anti-Patterns

The following are prohibited or strongly discouraged:

- deploying critical services without an owner
- paging alerts without runbooks
- paging on every infrastructure anomaly
- ignoring persistent false-positive alerts
- classifying severity by technical complexity rather than impact
- having multiple uncoordinated incident leaders
- debugging indefinitely instead of applying a safe rollback
- changing many unrelated variables simultaneously
- increasing retries during dependency overload without analysis
- disabling circuit breakers as a default incident response
- deleting outbox records to hide backlog
- blindly increasing database connection pools
- blindly increasing Kubernetes memory limits
- treating pod restart as root-cause remediation
- modifying already applied Flyway migrations
- replaying Kafka messages without idempotency analysis
- treating cache as authoritative data when it is not
- restoring a compromised credential
- using production incidents to bypass security controls permanently
- making unaudited production changes
- using shared administrator credentials
- relying on undocumented manual recovery
- assuming backups work without restore tests
- defining RTO/RPO without business input
- performing chaos experiments without hypothesis or abort criteria
- running uncontrolled chaos against production
- closing incidents before backlog and data integrity are verified
- postmortems focused on individual blame
- postmortem actions without owners
- repeatedly accepting the same unresolved incident cause
- treating reliability work as optional

---

# 302. Positive Consequences

The decision provides:

- predictable incident response
- faster mitigation
- clearer ownership
- improved alert quality
- reduced alert fatigue
- better use of SLOs
- safer rollback
- improved dependency diagnosis
- standardized Kubernetes troubleshooting
- standardized Kafka troubleshooting
- standardized PostgreSQL troubleshooting
- explicit outbox monitoring
- stronger disaster preparedness
- measurable recovery objectives
- better runbooks
- better postmortems
- stronger organizational learning
- improved operational automation
- validated resilience assumptions
- reduced production risk

---

# 303. Negative Consequences

The decision introduces:

- on-call responsibility
- operational-review overhead
- runbook maintenance
- incident-process training
- postmortem effort
- game-day effort
- DR testing costs
- monitoring costs
- additional reliability engineering work
- corrective-action tracking

These costs are accepted because distributed production systems require deliberate operational engineering.

---

# 304. Neutral Consequences

The decision also means:

- some alerts will be removed rather than added
- some deployments may be delayed because reliability is insufficient
- some incidents may trigger temporary release freezes
- some mitigations may intentionally reduce functionality
- DR objectives may require additional infrastructure investment
- reliability testing may reveal previously unknown architectural limitations

---

# 305. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Incident detected too late | Critical | Medium | SLO and business monitoring |
| Alert fatigue causes missed incident | Critical | Medium | Actionable alert standards |
| No responder available | Critical | Low | On-call and escalation |
| Incident response becomes chaotic | High | Medium | Incident Commander |
| Wrong mitigation increases impact | Critical | Medium | Runbooks and coordinated changes |
| Bad deployment remains active too long | High | Medium | Rapid rollback |
| Dependency failure cascades | Critical | Medium | Circuit breakers, timeout and bulkheads |
| Retry storm amplifies outage | High | Medium | Bounded retries |
| Kafka backlog grows unnoticed | High | Medium | Lag and age monitoring |
| Outbox events remain undispatched | High | Medium | Backlog and oldest-age monitoring |
| Database pool exhaustion causes outage | High | Medium | Pool metrics and capacity planning |
| DR backup cannot be restored | Critical | Low | Restore testing |
| RTO/RPO not achievable | Critical | Medium | DR exercises |
| Production repair corrupts data | Critical | Low | Controlled repair procedures |
| Security controls bypassed during incident | Critical | Medium | Governed break-glass process |
| Same incident repeats | High | Medium | Postmortem action tracking |
| Chaos experiment causes customer impact | High | Low | Small blast radius and abort criteria |

---

# 306. Implementation Guidance

The following rules are mandatory:

1. Every production service must have an owning team.
2. Critical services must have an appropriate on-call model.
3. Production services must undergo Production Readiness Review before initial launch.
4. Materially changed critical services may require renewed PRR.
5. Every critical service must have an SLO.
6. Paging alerts must be actionable.
7. Every paging alert must have an owner.
8. Critical paging alerts must reference a runbook.
9. Alert noise and false positives must be actively reduced.
10. Incident severity must be based on business/customer impact.
11. SEV1 incidents require an Incident Commander.
12. SEV1 incidents require a dedicated coordination channel.
13. Major incidents must maintain an event timeline.
14. Communication must distinguish facts from hypotheses.
15. Mitigation takes priority over complete root-cause analysis during active impact.
16. Rollback must be a first-class deployment mitigation.
17. Rollbacks must use approved immutable artifacts.
18. Already applied Flyway migrations must never be modified as incident remediation.
19. Optional risky capabilities should support kill switches where justified.
20. Circuit breakers must be used to contain dependency failures.
21. Retry configuration must not amplify dependency incidents.
22. Degraded modes must preserve business correctness.
23. Kubernetes runbooks must cover common pod/deployment failures.
24. Kafka consumers must expose lag/backlog operational signals.
25. Outbox implementations must monitor pending count and oldest pending age.
26. PostgreSQL connection pools must expose saturation metrics.
27. Database connection capacity must consider all service replicas.
28. Critical dependencies must be inventoried and classified.
29. Timeouts must respect end-to-end request budgets.
30. Critical services require capacity planning.
31. Autoscaling must account for downstream capacity.
32. Critical services require defined disaster-recovery strategy.
33. RTO and RPO require business agreement.
34. Backups must be periodically restore-tested.
35. Critical DR procedures must be exercised.
36. Incident recovery must verify business processing and data integrity.
37. Significant incidents require postmortems.
38. Postmortems must focus on systemic learning rather than personal blame.
39. Corrective actions must have owners.
40. High-risk corrective actions must have target dates.
41. Repeated incidents must trigger escalation of unresolved corrective work.
42. Critical services should participate in periodic game days.
43. Chaos experiments must define hypothesis, blast radius and abort conditions.
44. Production chaos testing requires explicit controls.
45. Production changes during major incidents must be coordinated.
46. Major incidents may trigger a temporary unrelated-change freeze.
47. Production data repair must be controlled and auditable.
48. Operational dashboards must include customer/application/dependency signals.
49. Reliability metrics must be used for system improvement, not individual evaluation.
50. Operational documentation must remain accessible during outages.

---

# 307. Production Readiness Gate

A critical service is not considered production ready until the following are satisfied:

```text
[ ] Owner defined

[ ] Repository identified

[ ] SLO defined

[ ] Dashboard available

[ ] Paging alerts configured

[ ] Alerts have runbooks

[ ] Dependency inventory documented

[ ] Timeouts configured

[ ] Retries bounded

[ ] Circuit breakers configured

[ ] Resource sizing validated

[ ] HPA reviewed

[ ] Database pool sizing reviewed

[ ] Kafka lag monitored where applicable

[ ] Outbox monitored where applicable

[ ] Health probes validated

[ ] Graceful shutdown validated

[ ] Rolling deployment tested

[ ] Rollback tested

[ ] Configuration validated

[ ] Secret management validated

[ ] Security controls validated

[ ] Backup/recovery defined

[ ] RTO/RPO defined where critical

[ ] DR runbook available

[ ] On-call/escalation defined
```

---

# 308. Validation

The decision will be validated through:

- Production Readiness Reviews
- alert tests
- paging tests
- runbook exercises
- rollback exercises
- feature kill-switch tests
- dependency-failure tests
- circuit-breaker tests
- timeout tests
- Kubernetes pod-termination tests
- Kubernetes node-drain tests
- Kafka consumer-lag simulations
- outbox backlog simulations
- PostgreSQL connection-pool saturation tests
- database failover exercises
- cache failure tests
- secret-rotation exercises
- backup restore tests
- DR exercises
- game days
- controlled chaos experiments
- postmortem reviews
- corrective-action tracking
- periodic SLO/error-budget reviews

---

# 309. Success Criteria

The decision is successful when:

- every critical service has clear ownership
- production incidents have an accountable responder
- SEV1 incidents are consistently coordinated
- paging alerts represent actionable conditions
- responders can locate runbooks immediately
- SLO violations are detected before prolonged customer impact
- deployments can be rolled back rapidly
- optional failures can be isolated from critical business capability
- dependency outages do not routinely cause cascading failure
- Kafka backlog is detected and diagnosed
- outbox backlog cannot silently accumulate
- database saturation is visible before total failure
- critical backups have proven restore capability
- RTO/RPO objectives are demonstrably achievable
- incident recovery includes business validation
- postmortems produce measurable corrective actions
- repeated incidents decrease
- game days identify weaknesses before real incidents
- operational knowledge is not concentrated in one individual

---

# 310. Alternatives Rejected

## 310.1 Best-Effort Production Support

Rejected because critical distributed systems require explicit operational ownership.

---

## 310.2 Monitoring Without On-Call

Rejected because detection without accountable response does not provide operational reliability.

---

## 310.3 Alert on Every Error

Rejected because it creates alert fatigue and reduces detection quality.

---

## 310.4 Infrastructure Metrics Only

Rejected because infrastructure health does not necessarily represent customer experience.

---

## 310.5 Debug Before Mitigation

Rejected for major incidents because extended diagnosis can unnecessarily prolong customer impact.

---

## 310.6 Restart as Standard Resolution

Rejected because restart may hide persistent application defects.

---

## 310.7 Manual Tribal-Knowledge Recovery

Rejected because recovery must not depend on one experienced engineer being available.

---

## 310.8 Backup Without Restore Testing

Rejected because an untested backup cannot be considered proven recoverability.

---

## 310.9 Postmortem Focused on Individual Error

Rejected because it fails to improve the system conditions that allowed the incident.

---

## 310.10 Uncontrolled Chaos Testing

Rejected because resilience testing must itself be engineered safely.

---

# 311. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-009: Use Apache Kafka for Integration Events
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-024: Adopt Software Supply Chain Security
- ADR-025: Adopt Kubernetes Runtime Security Standards
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards

---

# 312. References

- Google SRE — Site Reliability Engineering
- Google SRE — The Site Reliability Workbook
- Google SRE — Incident Response
- Google SRE — Postmortem Culture
- Google SRE — Error Budgets
- AWS Well-Architected Reliability Pillar
- Microsoft Azure Well-Architected Reliability
- Kubernetes Documentation
- PostgreSQL Documentation
- Apache Kafka Documentation
- OpenTelemetry Documentation
- Spring Boot Actuator Documentation
- Resilience4j Documentation
- NIST Cybersecurity Framework
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-025: Adopt Kubernetes Runtime Security Standards
- ADR-026: Adopt Platform Configuration and Secret Management Standards

---

# 313. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial production operations and incident-management baseline |

---

# 314. Decision Summary

The AstraForge Supply Platform adopts the following production lifecycle:

```text
DESIGN

↓

IMPLEMENT

↓

TEST

↓

SECURE

↓

PRODUCTION READINESS REVIEW

↓

DEPLOY

↓

OBSERVE

↓

OPERATE

↓

RESPOND

↓

RECOVER

↓

LEARN

↓

IMPROVE
```

A production service requires:

```text
OWNER

+

SLO

+

DASHBOARD

+

ACTIONABLE ALERTS

+

RUNBOOK

+

ON-CALL

+

ROLLBACK

+

RECOVERY STRATEGY
```

Incident response follows:

```text
DETECT

↓

ACKNOWLEDGE

↓

CLASSIFY

↓

COORDINATE

↓

MITIGATE

↓

RECOVER

↓

VERIFY

↓

LEARN
```

During active customer impact:

```text
MITIGATION
```

takes precedence over:

```text
PERFECT ROOT-CAUSE UNDERSTANDING
```

provided mitigation is safe and controlled.

The platform's major-incident coordination model becomes:

```text
INCIDENT COMMANDER
        |
        +------------------+
        |                  |
        v                  v
 TECHNICAL LEAD     COMMUNICATION
        |
        +----------------------------+
        |              |             |
        v              v             v
 APPLICATION       DATABASE        PLATFORM
        |
        v
 DEPENDENCIES
```

The primary diagnostic model is:

```text
CUSTOMER IMPACT

↓

SLO / BUSINESS SIGNALS

↓

METRICS

↓

TRACES

↓

LOGS

↓

DEPENDENCIES

↓

INFRASTRUCTURE

↓

RECENT CHANGES
```

The primary mitigation toolbox becomes:

```text
ROLLBACK

FEATURE KILL SWITCH

CIRCUIT BREAKER

DEGRADED MODE

CAPACITY ADJUSTMENT

DEPENDENCY ISOLATION

CONTROLLED FAILOVER

CONTROLLED DATA REPAIR
```

The asynchronous operational model becomes:

```text
KAFKA

Consumer Lag
+
Oldest Message Age
+
Consumer Health

↓

OUTBOX

Pending Count
+
Oldest Pending Age
+
Dispatch Failures

↓

BUSINESS PROCESSING

Throughput
+
Failures
+
Backlog
```

The database operational model becomes:

```text
APPLICATION

↓

CONNECTION POOL

Active
Idle
Pending
Timeout

↓

POSTGRESQL

Connections
Queries
Locks
CPU
Storage
Replication
```

The disaster-recovery model becomes:

```text
BUSINESS CRITICALITY

↓

RTO + RPO

↓

RECOVERY ARCHITECTURE

↓

BACKUP / REPLICATION

↓

RUNBOOK

↓

RESTORE TEST

↓

DR EXERCISE

↓

PROVEN RECOVERABILITY
```

A backup is therefore not considered sufficient evidence of recoverability.

The required proof is:

```text
WE RESTORED IT

AND

THE BUSINESS CAPABILITY WORKED
```

Post-incident improvement follows:

```text
INCIDENT

↓

POSTMORTEM

↓

SYSTEMIC CONTRIBUTING FACTORS

↓

CORRECTIVE ACTIONS

↓

OWNER + DEADLINE

↓

IMPLEMENTATION

↓

VALIDATION

↓

REDUCED FUTURE RISK
```

The final operational principle is:

```text
Production reliability is not
the absence of failure.

Production reliability is
the ability to detect,
contain,
recover from,
and learn from failure
without losing control of the system.
```
