# ADR-028: Adopt Disaster Recovery and Business Continuity Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-028 |
| Title | Adopt Disaster Recovery and Business Continuity Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Disaster Recovery, Business Continuity, Data Protection, Kubernetes, PostgreSQL, Kafka |
| Related Work Items | RTO, RPO, Backup, Restore, PITR, Multi-AZ, Multi-Region, Failover, Failback, DR Testing |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform supports business-critical ordering capabilities through a distributed architecture composed of:

- Java 21
- Spring Boot
- PostgreSQL
- Apache Kafka
- transactional outbox
- Redis
- Kubernetes
- external HTTP services
- GitOps
- external secret management
- workload identity
- CI/CD
- observability infrastructure

ADR-027 establishes how production incidents are:

```text
Detected
Coordinated
Mitigated
Recovered
Reviewed
```

However, ordinary high availability and incident response are not sufficient for catastrophic failures.

The platform must also be capable of recovering from events such as:

- complete Kubernetes cluster loss
- availability-zone failure
- regional cloud failure
- PostgreSQL corruption
- accidental data deletion
- ransomware
- compromised credentials
- Kafka cluster loss
- secret-management failure
- configuration corruption
- DNS failure
- container-registry failure
- destructive deployment
- infrastructure-account compromise

Disaster Recovery must therefore be engineered before a disaster occurs.

---

# 2. Problem Statement

The platform requires standards defining:

- service criticality
- RTO
- RPO
- high availability versus disaster recovery
- multi-AZ architecture
- multi-region requirements
- backup strategy
- backup immutability
- PostgreSQL PITR
- database replication
- database failover
- Kafka recovery
- consumer-offset recovery
- event replay
- transactional-outbox recovery
- Redis recovery
- Kubernetes reconstruction
- GitOps recovery
- secret-management recovery
- KMS recovery
- DNS failover
- dependency recovery
- recovery ordering
- data reconciliation
- split-brain prevention
- failback
- ransomware recovery
- restore testing
- DR exercises
- evidence of recoverability

---

# 3. Decision Drivers

Primary drivers are:

1. business continuity
2. data integrity
3. customer impact
4. deterministic recovery
5. recoverability
6. RTO compliance
7. RPO compliance
8. automation
9. infrastructure reproducibility
10. security
11. resilience
12. auditability
13. operational simplicity
14. cost proportionality
15. regulatory requirements

---

# 4. Decision

The Enterprise Order Platform adopts a tiered Disaster Recovery model based on:

```text
Business Criticality

↓

RTO + RPO

↓

Recovery Architecture

↓

Replication + Backup

↓

Infrastructure Reproducibility

↓

Documented Recovery Procedure

↓

Automated Validation

↓

Periodic DR Exercise

↓

Proven Recoverability
```

Disaster Recovery architecture must be proportional to business impact.

---

# 5. Fundamental Principle

The platform adopts:

```text
High Availability reduces the probability
and duration of ordinary failures.

Disaster Recovery restores the platform
after high-availability mechanisms are
insufficient or unavailable.
```

---

# 6. High Availability Is Not Disaster Recovery

A Kubernetes Deployment with:

```text
replicas: 3
```

does not constitute Disaster Recovery.

---

# 7. Multi-AZ Is Not Automatically DR

Multi-AZ protects against availability-zone failures.

It does not necessarily protect against:

- regional failure
- destructive administrative action
- corrupted data
- compromised credentials
- ransomware
- logical application corruption

---

# 8. Backup Is Not High Availability

Backups provide recovery capability.

They do not provide continuous service availability.

---

# 9. Backup Is Not Proven Recovery

A backup is useful only when it can actually be restored.

Therefore:

```text
Backup Success
```

is insufficient.

The required evidence is:

```text
Restore Success

+

Data Validation

+

Application Validation
```

---

# 10. Service Criticality

Production capabilities must be classified according to business criticality.

The platform adopts:

```text
Tier 0
Tier 1
Tier 2
Tier 3
```

---

# 11. Tier 0

Tier 0 represents foundational capabilities whose loss prevents recovery of multiple other systems.

Examples may include:

- identity
- secret management
- key management
- critical networking
- DNS
- GitOps infrastructure
- artifact registry

depending on enterprise architecture.

---

# 12. Tier 1

Tier 1 represents critical business capabilities.

For the Enterprise Order Platform this may include:

- order creation
- order persistence
- order state management
- critical integration events

---

# 13. Tier 2

Tier 2 represents important capabilities where temporary degradation is acceptable.

Examples may include:

- secondary integrations
- non-critical enrichment
- operational reporting

---

# 14. Tier 3

Tier 3 represents capabilities with comparatively high tolerance for extended recovery.

Examples may include:

- historical reporting
- non-critical analytics
- development-support services

---

# 15. Classification Is Business Driven

Tier classification must be based on:

- customer impact
- revenue impact
- legal obligations
- operational impact
- dependency impact

rather than engineering preference.

---

# 16. RTO

Recovery Time Objective defines:

```text
Maximum Target Time
to restore a business capability
after disaster declaration.
```

---

# 17. RPO

Recovery Point Objective defines:

```text
Maximum Acceptable Data-Loss Window.
```

---

# 18. Example

Conceptually:

```text
RTO = 1 hour

RPO = 5 minutes
```

means the architecture targets:

```text
service restoration within 1 hour

and

no more than approximately 5 minutes
of unrecoverable data
```

under the defined disaster scenario.

---

# 19. RTO Is Not RPO

These objectives address different concerns.

```text
RTO
→ How long can the business remain unavailable?

RPO
→ How much data can the business lose?
```

---

# 20. RTO/RPO Matrix

Exact production values require business approval.

An architectural classification may follow:

| Tier | RTO Expectation | RPO Expectation |
|---|---|---|
| Tier 0 | Very Low | Near Zero |
| Tier 1 | Low | Very Low |
| Tier 2 | Moderate | Moderate |
| Tier 3 | Higher | Higher |

---

# 21. Measurable Objectives

RTO and RPO must be measurable.

Avoid statements such as:

```text
Recover quickly.
```

---

# 22. Disaster Scenarios

DR planning must be scenario based.

---

# 23. Scenario Catalog

At minimum evaluate:

```text
Single Pod Failure

Node Failure

Availability-Zone Failure

Kubernetes Cluster Loss

Database Failure

Database Corruption

Kafka Failure

Regional Failure

Credential Compromise

Secret Manager Failure

DNS Failure

Destructive Human Action

Ransomware / Security Compromise
```

---

# 24. Failure Scope

Recovery architecture depends on failure scope.

Example:

```text
Pod Failure
→ Kubernetes self-healing

AZ Failure
→ Multi-AZ architecture

Region Failure
→ Cross-region recovery

Logical Corruption
→ Point-in-time restore

Ransomware
→ Immutable isolated recovery assets
```

---

# 25. Multi-AZ

Tier 0 and Tier 1 production infrastructure should use multiple availability zones where supported.

---

# 26. Kubernetes Distribution

Critical Kubernetes workloads should be distributed across failure domains.

Use:

- topology spread constraints
- pod anti-affinity where appropriate
- PodDisruptionBudgets
- multiple replicas

according to ADR-025.

---

# 27. Multi-Region

Multi-region architecture is required only when justified by approved RTO/RPO and business requirements.

---

# 28. Cost Proportionality

Multi-region architecture has significant:

- cost
- operational
- consistency
- networking
- security

complexity.

It must not be adopted merely as an architectural fashion.

---

# 29. Active-Active

Active-active means multiple locations actively process production traffic.

---

# 30. Active-Active Complexity

Active-active architectures introduce challenges including:

- concurrent writes
- distributed consistency
- conflict resolution
- duplicate processing
- event ordering
- global routing
- split brain

---

# 31. Active-Passive

Active-passive maintains a secondary recovery environment that becomes active during disaster.

---

# 32. Preferred Default

For stateful business workloads, active-passive is preferred unless RTO/RPO explicitly require active-active.

---

# 33. Warm Standby

Warm standby may maintain:

- infrastructure
- replicated data
- configuration
- required services

with reduced application capacity until failover.

---

# 34. Pilot Light

Pilot-light architectures maintain critical recovery foundations while application capacity is provisioned during recovery.

---

# 35. Cold Recovery

Cold recovery reconstructs infrastructure after disaster.

This is acceptable only when RTO permits it.

---

# 36. Architecture Selection

Recovery topology should be selected based on:

```text
Business Criticality
+
RTO
+
RPO
+
Cost
+
Operational Complexity
```

---

# 37. Backup Strategy

Critical persistent data requires a documented backup strategy.

---

# 38. Backup Scope

Backups must cover relevant state such as:

- PostgreSQL
- critical object storage
- configuration repositories
- key-management metadata where applicable
- secret-manager recovery mechanisms

---

# 39. Backup Frequency

Backup frequency must support the required RPO.

---

# 40. Backup Retention

Retention must consider:

- RPO
- corruption discovery delay
- legal requirements
- ransomware scenarios
- storage cost

---

# 41. 3-2-1 Principle

Where appropriate, critical backup architecture should follow the principle:

```text
3 copies of important data

2 different storage/media mechanisms

1 copy isolated/off-site
```

adapted to cloud architecture.

---

# 42. Immutable Backup

Critical backups should support immutability where platform capabilities permit.

---

# 43. Why Immutability

If an attacker can compromise production and delete every backup, the backups do not provide adequate ransomware recovery.

---

# 44. Backup Access

Backup deletion permissions must be highly restricted.

---

# 45. Separate Trust Boundary

Critical backup infrastructure should use a stronger or separate trust boundary where practical.

---

# 46. Backup Encryption

Backups must be encrypted according to enterprise security standards.

---

# 47. Encryption Key Dependency

Backup recovery requires access to the correct encryption keys.

---

# 48. Key Loss

An encrypted backup whose key is permanently lost is effectively unrecoverable.

---

# 49. PostgreSQL

PostgreSQL requires both high-availability and recovery mechanisms.

---

# 50. PostgreSQL Backup

Critical PostgreSQL databases must have automated backups.

---

# 51. PITR

Point-in-Time Recovery should be enabled where required by RPO and corruption-recovery objectives.

---

# 52. WAL

PITR typically depends on:

```text
Base Backup

+

WAL Archive
```

or equivalent managed-service functionality.

---

# 53. PITR Purpose

PITR supports recovery to a point before:

- accidental deletion
- destructive migration
- logical corruption
- bad batch processing

---

# 54. PITR Is Not Automatic Correctness

Selecting the recovery timestamp requires understanding when corruption began.

---

# 55. Recovery Point

The selected restore point should be:

```text
after the last known valid transaction

and

before the destructive event
```

where evidence permits.

---

# 56. PostgreSQL Replication

Replication may provide high availability and reduced RTO.

---

# 57. Replication Is Not Backup

Replication can replicate corruption.

Example:

```text
DELETE FROM orders;
```

may be replicated perfectly.

Therefore replication does not replace backups.

---

# 58. Synchronous Replication

Synchronous replication can reduce RPO but may increase latency and availability coupling.

---

# 59. Asynchronous Replication

Asynchronous replication improves isolation but may permit some data loss during failover.

---

# 60. Replication Choice

Replication mode must align with approved RPO.

---

# 61. Database Failover

Failover must define:

- promotion mechanism
- application reconnection
- DNS/endpoint behavior
- connection-pool recovery
- data validation
- old-primary fencing

---

# 62. Fencing

After failover, the old primary must not independently resume accepting writes.

---

# 63. Split Brain

The architecture must prevent:

```text
Primary A accepting writes

and

Primary B accepting incompatible writes
```

unless explicitly designed for multi-writer semantics.

---

# 64. PostgreSQL Failback

Returning to the original region/database after recovery is a separate operation from failover.

---

# 65. Failback Planning

Failback must define:

- data synchronization
- consistency validation
- traffic transition
- rollback
- old environment retirement

---

# 66. Flyway During DR

Database recovery must preserve Flyway schema history.

---

# 67. Migration Reconstruction

Do not reconstruct schema by editing historical migrations.

Use the migration history exactly as version controlled.

---

# 68. New Correction Migration

If recovered data/schema requires correction, use a new Flyway migration.

---

# 69. Kafka DR

Kafka requires explicit recovery planning because:

```text
Data

Consumer Position

Ordering

Duplicates

Retention
```

all affect recovery.

---

# 70. Kafka Topic Inventory

Critical topics must be inventoried with:

- owner
- producer
- consumers
- partitions
- retention
- criticality
- replay characteristics

---

# 71. Kafka Replication

Cross-region Kafka replication may be used where RTO/RPO require it.

---

# 72. Kafka Recovery Strategy

The strategy must define whether recovery uses:

- replicated cluster
- restored cluster
- source-system replay
- outbox replay
- combination

---

# 73. Consumer Offsets

Consumer offsets are part of recovery state.

---

# 74. Offset Loss

Loss or incorrect restoration of offsets can cause:

```text
Duplicate Processing

or

Skipped Processing
```

---

# 75. At-Least-Once

Consumers must remain compatible with at-least-once delivery semantics where applicable.

---

# 76. Idempotency

Idempotent consumers are a fundamental DR capability.

---

# 77. Replay

Replay must be controlled by:

- topic
- partition
- offset/time range
- consumer
- business impact

---

# 78. Replay Authorization

Production replay requires restricted authorization and auditability.

---

# 79. Event Ordering

Replay must respect ordering requirements within the defined partitioning model.

---

# 80. Event Retention

Kafka retention must support expected recovery/replay requirements.

---

# 81. Retention Gap

If required recovery exceeds Kafka retention, another recovery source must exist.

---

# 82. Transactional Outbox

The transactional outbox is a major recovery asset.

---

# 83. Outbox Persistence

Because the outbox is stored transactionally with business data, database recovery can preserve events not yet published.

---

# 84. Outbox Recovery

After database recovery:

```text
Recovered Business Data

+

Recovered Pending Outbox Events

↓

Dispatcher

↓

Kafka
```

can restore publication of committed events.

---

# 85. Duplicate Publication

Recovery may cause some events to be published again.

Consumers must remain idempotent.

---

# 86. Outbox Reconciliation

After DR, compare:

- business transactions
- outbox records
- Kafka publication
- consumer processing

for critical workflows.

---

# 87. Redis

Redis recovery strategy depends on its role.

---

# 88. Cache-Only Redis

If Redis is purely a cache:

```text
Loss of Redis
```

should normally result in:

```text
Cache Reconstruction
```

rather than authoritative-data restoration.

---

# 89. Cache Reconstruction

Cache rebuilding must avoid overwhelming:

- PostgreSQL
- downstream APIs
- network capacity

---

# 90. Cache Warm-Up

Critical caches may require controlled warm-up.

---

# 91. Redis as State

If Redis stores authoritative state, it requires explicit persistence, backup and DR treatment.

---

# 92. Preferred Principle

Business-critical durable state should not reside exclusively in an ephemeral cache.

---

# 93. Kubernetes Recovery

Kubernetes clusters must be reconstructable from declarative infrastructure.

---

# 94. Cluster as Cattle

The recovery model should prefer:

```text
Recreate Cluster
```

over:

```text
Manually repair every cluster object
```

after catastrophic cluster loss.

---

# 95. Infrastructure as Code

Cluster infrastructure must be represented through approved Infrastructure as Code where applicable.

---

# 96. GitOps

Application deployment state should be reconstructable from GitOps repositories.

---

# 97. Recovery Flow

Conceptually:

```text
Provision Infrastructure

↓

Create Kubernetes Cluster

↓

Restore Platform Components

↓

Restore Identity / Secrets Integration

↓

Restore GitOps Controller

↓

Reconcile Desired State

↓

Restore Stateful Dependencies

↓

Deploy Applications
```

---

# 98. Kubernetes Object Backup

Cluster-object backups may accelerate recovery.

They do not replace declarative infrastructure.

---

# 99. Ephemeral Objects

Not every Kubernetes object requires backup.

Many should simply be reconstructed.

---

# 100. Persistent Volumes

Persistent-volume recovery depends on the storage/data architecture and must be explicitly defined.

---

# 101. GitOps Source of Truth

GitOps repositories are a recovery source for declarative configuration.

---

# 102. Git Repository DR

Critical Git repositories require enterprise backup/availability protection.

---

# 103. Repository Compromise

A compromised repository must not automatically be trusted as a recovery source.

---

# 104. Trusted Recovery Revision

Recovery may require selecting a known-good repository revision before compromise.

---

# 105. Artifact Registry

Container images required for recovery must remain available.

---

# 106. Registry DR

Critical artifact registries require appropriate availability/replication or another trusted recovery path.

---

# 107. Rebuild vs Restore Artifact

Prefer deploying the exact previously approved artifact digest.

---

# 108. Rebuilding Historical Version

Rebuilding old source may produce a different artifact due to dependency/toolchain changes.

Therefore immutable artifact retention is valuable for DR.

---

# 109. Secret Manager

Secret-management infrastructure is a Tier 0 dependency.

---

# 110. Secret Recovery

DR planning must define how required secrets become available in the recovery environment.

---

# 111. Secret Replication

Secret replication across regions must follow enterprise security architecture.

---

# 112. Secret Scope

Recovery must not result in broader secret permissions than normal production.

---

# 113. Secret Rotation After Disaster

Security-related disasters may require rotating credentials during recovery.

---

# 114. Compromised Secret

Known-compromised credentials must never be restored merely because they existed in a backup.

---

# 115. KMS

Key-management infrastructure must be included in DR planning.

---

# 116. KMS Dependency

Loss of encryption keys can prevent recovery of:

- backups
- secrets
- encrypted databases
- object storage
- application data

---

# 117. Key Recovery

KMS/key-recovery procedures must follow enterprise security policy.

---

# 118. Key Separation

Key recovery must preserve separation of duties.

---

# 119. Certificate Recovery

TLS certificates and certificate issuance must be recoverable.

---

# 120. DNS

DNS is part of the DR architecture.

---

# 121. DNS Failover

Regional recovery may require redirecting traffic to a recovery environment.

---

# 122. DNS TTL

DNS TTL affects failover speed.

---

# 123. Low TTL Trade-Off

Very low TTL can improve failover responsiveness but increases DNS query volume and does not guarantee every client immediately respects the change.

---

# 124. Global Traffic Management

Global load balancing/traffic management may be preferred over manual DNS changes when RTO requires rapid failover.

---

# 125. Traffic Activation

Recovery infrastructure must not receive production traffic before readiness and data validation are complete.

---

# 126. Traffic Ramp

After failover, traffic may be progressively increased where platform capability permits.

---

# 127. External Dependencies

DR planning must include external service dependencies.

---

# 128. Dependency Map

For every Tier 0/Tier 1 capability, identify:

```text
Dependency

Criticality

Region

Recovery Capability

Recovery Owner

RTO/RPO Dependency
```

---

# 129. Weakest Dependency

A service cannot realistically promise a 15-minute RTO if a mandatory dependency requires four hours to recover.

---

# 130. Composite RTO

Business-capability RTO must consider the entire dependency chain.

---

# 131. SaaS Dependency

Third-party SaaS recovery capabilities must be understood where they affect critical workflows.

---

# 132. Recovery Ordering

Recovery must occur in dependency order.

---

# 133. Example Recovery Order

A conceptual sequence is:

```text
1. Network / DNS / Identity

2. KMS / Secret Management

3. Kubernetes / Platform

4. PostgreSQL

5. Kafka

6. Redis / Supporting Infrastructure

7. GitOps

8. Core Services

9. Integration Services

10. Optional Services

11. External Traffic
```

Actual order depends on architecture.

---

# 134. Circular Dependencies

Recovery plans must identify and eliminate or explicitly solve circular bootstrap dependencies.

---

# 135. Bootstrap Problem

Example:

```text
GitOps requires Secret Manager

Secret Manager integration requires Kubernetes

Kubernetes configuration requires GitOps
```

requires a documented bootstrap sequence.

---

# 136. Recovery Automation

Recovery should be automated where practical.

---

# 137. Automation Benefits

Automation improves:

- speed
- repeatability
- auditability
- reduction of human error

---

# 138. Automation Validation

Recovery automation must itself be tested.

---

# 139. Script Rot

A recovery script that has not run for two years cannot be assumed valid.

---

# 140. Recovery Environment

A DR environment must be sufficiently compatible with production to satisfy recovery objectives.

---

# 141. Configuration

DR configuration must be versioned and reviewed.

---

# 142. Secrets

DR must use appropriate production-grade secret material.

---

# 143. Capacity

Recovery capacity must support the required business workload.

---

# 144. Reduced Capacity

A DR environment may initially run reduced capacity if business requirements permit.

---

# 145. Minimum Viable Business Capacity

Recovery planning should define the minimum capacity required to restore critical business capability.

---

# 146. Priority Workload

During constrained recovery:

```text
Critical Orders
```

may take precedence over:

```text
Reporting / Analytics / Optional Workloads
```

---

# 147. Business Continuity

Business Continuity extends beyond technical infrastructure recovery.

---

# 148. Business Continuity Question

The relevant question is:

```text
Can the business continue its critical process?
```

not merely:

```text
Are the pods running?
```

---

# 149. Manual Workaround

Where technically appropriate, business continuity may define temporary manual processes.

---

# 150. Manual Workaround Risk

Manual fallback must account for:

- duplicate transactions
- later reconciliation
- authorization
- auditability
- data entry errors

---

# 151. Reconciliation

After recovery, the platform must reconcile potentially inconsistent state.

---

# 152. Reconciliation Scope

Possible comparisons include:

```text
Orders

Payments

Inventory

Outbox Events

Kafka Events

External System State
```

according to domain.

---

# 153. Reconciliation Is Mandatory

For critical workflows, technical recovery without data reconciliation is incomplete.

---

# 154. Recovery Gap

An RPO greater than zero implies a potential reconciliation requirement.

---

# 155. Lost Transactions

Transactions occurring after the recovery point may need:

- replay
- reconstruction
- external reconciliation
- business remediation

---

# 156. Duplicate Transactions

Recovery/replay can also produce duplicates.

---

# 157. Idempotency Keys

Business operations supporting idempotency keys improve safe replay/recovery.

---

# 158. External Side Effects

External side effects require special care.

Example:

```text
Local database restored

but

external system already processed the transaction
```

---

# 159. Exactly-Once Assumption

Do not assume exactly-once behavior across distributed external systems.

---

# 160. Recovery Ledger

Critical recovery operations may maintain a reconciliation ledger or equivalent audit record.

---

# 161. Disaster Declaration

A disaster must be explicitly declared according to incident-management governance.

---

# 162. Disaster Authority

The organization must define who can authorize:

- regional failover
- database restore
- major replay
- DR traffic activation

---

# 163. Failover Decision

Failover has risks and must not occur solely because one transient alert fires.

---

# 164. Decision Inputs

Failover should consider:

- confirmed impact
- expected outage duration
- current data replication state
- RPO risk
- recovery environment readiness
- dependency status

---

# 165. Failover Point of No Return

Some failover actions materially complicate returning to the original environment.

Runbooks must identify them.

---

# 166. Split-Brain Prevention

Before activating a new write environment, the old environment must be fenced when required.

---

# 167. Fencing Mechanisms

Depending on infrastructure, fencing may involve:

- disabling traffic
- revoking write credentials
- disabling database writers
- network isolation
- infrastructure controls

---

# 168. Recovery Validation

Before production traffic is enabled, validate:

```text
Infrastructure

Database

Schema

Configuration

Secrets

Kafka

Application Health

Business Transactions
```

---

# 169. Smoke Test

Automated smoke tests should validate critical business paths.

---

# 170. Synthetic Transaction

Where safe, a controlled synthetic transaction may validate end-to-end functionality.

---

# 171. Data Validation

Database availability alone does not prove data correctness.

---

# 172. Kafka Validation

Kafka broker availability alone does not prove consumers are correctly processing recovered events.

---

# 173. Outbox Validation

After recovery, verify pending outbox processing and event publication.

---

# 174. Observability Recovery

Monitoring, logging and tracing must be restored early enough to observe the recovery process.

---

# 175. Blind Recovery

Recovering production without observability significantly increases operational risk.

---

# 176. Security Recovery

Security controls must remain active during DR.

---

# 177. Emergency Security Exception

Any temporary exception requires explicit approval, scope and later remediation.

---

# 178. Ransomware Recovery

Ransomware or destructive compromise requires a different recovery mindset from ordinary infrastructure failure.

---

# 179. Trusted Recovery

The organization must identify a trusted recovery point.

---

# 180. Compromise Window

If compromise began days before detection, the newest backup may already contain compromised state.

---

# 181. Backup History

Retention must allow recovery from before the compromise window where required.

---

# 182. Immutable Recovery Assets

Critical recovery assets should resist modification/deletion from ordinary production credentials.

---

# 183. Credential Reset

Security-disaster recovery may require rotation of:

- database credentials
- OAuth secrets
- cloud identities
- certificates
- API keys
- administrative credentials

---

# 184. Recovered Infrastructure Trust

Compromised infrastructure should generally be rebuilt from trusted definitions rather than merely restarted.

---

# 185. Clean-Room Recovery

Severe compromise may require recovery into an isolated clean environment before production activation.

---

# 186. Forensic Preservation

Security recovery must preserve required forensic evidence according to enterprise incident-response policy.

---

# 187. Failback

Failback is the controlled return from the DR environment to the preferred production environment.

---

# 188. Failback Is Separate

Successful failover does not imply failback will be simple.

---

# 189. Failback Preconditions

Before failback:

- primary environment restored
- security validated
- infrastructure validated
- data synchronized
- dependencies healthy
- rollback available

---

# 190. Data Direction

After failover, the DR environment may become the authoritative writer.

Failback must therefore synchronize:

```text
DR → Primary
```

rather than blindly restoring the old primary state.

---

# 191. Failback Split Brain

Both environments must not independently process conflicting writes during failback.

---

# 192. Failback Verification

Apply the same rigor as failover:

```text
Validate

↓

Switch

↓

Observe

↓

Reconcile
```

---

# 193. DR Testing

DR capability must be periodically tested.

---

# 194. Documentation Review Is Not Test

Reading the DR document in a meeting is not a DR exercise.

---

# 195. Restore Test

A restore test actually restores data.

---

# 196. Component DR Test

Individual component tests may validate:

- PostgreSQL restore
- Kafka recovery
- secret recovery
- cluster reconstruction

---

# 197. Integrated DR Test

Critical platforms require integrated exercises covering the end-to-end business capability.

---

# 198. DR Drill

A DR drill should measure:

```text
Actual Recovery Time

Actual Recovery Point

Operational Errors

Manual Steps

Data Reconciliation

Business Validation
```

---

# 199. RTO Validation

If:

```text
Target RTO = 60 minutes
```

but the exercise requires:

```text
2 hours 30 minutes
```

the architecture does not satisfy the stated RTO.

---

# 200. RPO Validation

RPO must be validated from actual recovered data, not backup-job timestamps alone.

---

# 201. Exercise Frequency

Exercise frequency should increase with business criticality.

---

# 202. Tier 0/Tier 1

Tier 0 and Tier 1 capabilities require regular DR exercises.

---

# 203. Exercise Variation

Exercises should vary scenarios.

Repeatedly testing only one easy failure mode creates false confidence.

---

# 204. Surprise Exercise

Controlled unannounced elements may be used where organizational maturity permits.

---

# 205. Production DR Test

Production-impacting DR tests require explicit governance and safety controls.

---

# 206. Game Day Integration

DR exercises complement ADR-027 game days.

---

# 207. Exercise Evidence

Evidence should include:

- start time
- declaration time
- restore point
- recovery completion
- business validation
- observed RTO
- observed RPO
- problems
- corrective actions

---

# 208. Corrective Actions

DR test findings require owners and target dates.

---

# 209. Failed DR Test

A failed DR test is valuable if it identifies weaknesses before a real disaster.

---

# 210. False Confidence

The greater risk is claiming DR capability that has never been exercised.

---

# 211. DR Dashboard

Critical recovery capabilities should expose appropriate readiness signals.

---

# 212. Backup Monitoring

Monitor:

- backup success
- backup age
- backup duration
- backup size anomalies
- replication health

---

# 213. Restore Monitoring

Restore tests should produce auditable results.

---

# 214. Replication Lag

Cross-region/database replication lag directly affects potential RPO.

---

# 215. RPO Risk Alert

Replication lag exceeding the approved recovery window should generate an actionable operational signal.

---

# 216. Backup Failure

A failed backup is a production reliability event even when the application remains online.

---

# 217. Consecutive Backup Failure

Repeated backup failures may require incident escalation according to criticality.

---

# 218. Capacity of Backup Storage

Backup storage capacity and retention must be monitored.

---

# 219. Recovery Credentials

Recovery credentials must be tested without exposing them.

---

# 220. Break Glass

Critical DR operations may require break-glass access according to ADR-025.

---

# 221. Separation of Duties

High-impact recovery operations should preserve appropriate separation of duties.

---

# 222. Production Deletion

The same identity that operates an application should not automatically have permission to delete all recovery assets.

---

# 223. Recovery Documentation

DR documentation must remain available if the primary production platform is unavailable.

---

# 224. Offline/Emergency Access

Critical recovery documentation and contact paths require an independent access strategy where justified.

---

# 225. Dependency Contact

External critical dependencies should have documented escalation/support channels.

---

# 226. DR Ownership

Every Tier 0/Tier 1 capability must have an accountable DR owner.

---

# 227. DR Owner Responsibilities

The owner ensures:

- RTO/RPO documented
- recovery architecture exists
- runbook maintained
- tests scheduled
- corrective actions completed

---

# 228. DR Inventory

Maintain an inventory containing:

```text
Capability

Tier

Owner

RTO

RPO

Data Stores

Dependencies

Backup Mechanism

Recovery Strategy

Last DR Test

Last Restore Test
```

---

# 229. Architecture Review

Material architecture changes must assess DR impact.

---

# 230. New Stateful Component

Adding a new stateful component requires answering:

```text
How is it backed up?

How is it restored?

What is its RTO?

What is its RPO?

How is it reconciled?
```

---

# 231. New Dependency

Adding a mandatory external dependency requires assessing its recovery characteristics.

---

# 232. Data Residency

Cross-region backups/replication must respect data-residency and regulatory requirements.

---

# 233. Privacy

DR copies contain production data and require the same data-protection classification as primary data.

---

# 234. Retention

Backup retention must not violate approved data-retention policies.

---

# 235. Right to Deletion

Where regulatory deletion requirements apply, backup handling must follow enterprise legal/privacy policy.

---

# 236. DR Cost

DR cost must be explicit.

---

# 237. Cost Categories

Include:

- secondary infrastructure
- replicated storage
- cross-region traffic
- backup storage
- licensing
- testing
- operational support

---

# 238. Cost vs Risk

A cheaper recovery model may be acceptable for Tier 3.

It may be unacceptable for Tier 1.

---

# 239. DR Technical Debt

Examples include:

- untested restore
- outdated runbook
- excessive replication lag
- manual cluster creation
- missing recovery secrets
- undocumented dependencies
- unsupported RTO

---

# 240. DR Debt Priority

DR debt affecting Tier 0/Tier 1 must be treated as reliability risk.

---

# 241. Anti-Patterns

The following are prohibited or strongly discouraged:

- calling multiple replicas "DR"
- treating Multi-AZ as complete regional DR
- assuming replication replaces backup
- assuming backup success proves restore capability
- backups that production administrators can trivially delete
- backups without encryption
- encryption without recoverable keys
- undefined RTO
- undefined RPO
- RTO/RPO chosen only by engineering
- active-active without conflict strategy
- failover without fencing
- restoring compromised credentials
- restoring the latest backup blindly after security compromise
- manually rebuilding Kubernetes from memory
- depending on mutable infrastructure
- DR procedures dependent on one engineer
- replaying Kafka without idempotency
- ignoring consumer offsets during Kafka recovery
- deleting outbox events during recovery
- assuming Redis cache must always be restored
- recovering applications before foundational dependencies
- activating traffic before validation
- declaring recovery complete when pods merely become Ready
- ignoring business-data reconciliation
- assuming failback is trivial
- failback without authoritative-data synchronization
- DR documentation never exercised
- DR exercises that do not actually restore anything
- claiming RTO/RPO compliance without measurements
- allowing DR findings to remain indefinitely unresolved

---

# 242. Positive Consequences

The decision provides:

- explicit business recovery objectives
- predictable disaster response
- improved data protection
- measurable recoverability
- stronger ransomware resilience
- better PostgreSQL recovery
- safer Kafka replay
- controlled outbox recovery
- reproducible Kubernetes reconstruction
- improved secret/key recovery
- dependency-aware restoration
- safer failover
- safer failback
- reduced split-brain risk
- better recovery automation
- measurable DR readiness
- stronger audit evidence

---

# 243. Negative Consequences

The decision introduces:

- backup costs
- replication costs
- secondary infrastructure
- DR engineering effort
- testing effort
- operational complexity
- additional security controls
- data reconciliation procedures
- failover/failback complexity

These costs must be proportional to business criticality.

---

# 244. Neutral Consequences

The decision also means:

- not every service receives multi-region architecture
- different services may have different RTO/RPO
- some DR environments may operate at reduced capacity
- active-passive may be preferred over active-active
- recovery may intentionally prioritize core business functions
- some disasters require reconciliation rather than perfectly lossless recovery

---

# 245. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Backup cannot be restored | Critical | Medium | Periodic restore tests |
| Region unavailable | Critical | Low | Cross-region recovery for justified tiers |
| Database corruption replicated | Critical | Medium | PITR and independent backups |
| Ransomware deletes backups | Critical | Low | Immutable isolated backups |
| Encryption key unavailable | Critical | Low | KMS recovery strategy |
| Kafka offsets lost | High | Medium | Offset recovery and idempotent consumers |
| Duplicate event replay | High | Medium | Idempotency |
| Outbox duplicated after restore | Medium | Medium | Idempotent consumption |
| Split brain during failover | Critical | Low | Fencing |
| DR environment lacks capacity | High | Medium | Capacity validation |
| Recovery blocked by circular dependency | Critical | Low | Bootstrap design |
| Secrets unavailable | Critical | Low | Secret-manager DR |
| DNS delays failover | High | Medium | Traffic-management design |
| Failback overwrites newer DR data | Critical | Low | Authoritative-state synchronization |
| DR documentation outdated | High | Medium | Periodic exercises |
| Stated RTO cannot be achieved | Critical | Medium | Measured DR drills |
| RPO exceeded due to replication lag | Critical | Medium | Lag monitoring |
| Recovery completes with inconsistent business state | Critical | Medium | Reconciliation |

---

# 246. Implementation Guidance

The following rules are mandatory:

1. Critical production capabilities must receive a DR tier.
2. Tier 0/Tier 1 capabilities must have approved RTO and RPO.
3. RTO/RPO must be business driven.
4. High availability and DR must be treated as separate concerns.
5. Replication must not replace backups.
6. Backups must be periodically restored and validated.
7. Critical backups should be immutable where supported.
8. Backup deletion privileges must be strongly restricted.
9. Backup encryption keys must themselves be recoverable.
10. PostgreSQL critical databases must have automated backup.
11. PITR should be enabled where corruption recovery/RPO requires it.
12. PostgreSQL failover must include fencing.
13. Database failback must explicitly synchronize authoritative data.
14. Applied Flyway migrations must never be edited during recovery.
15. Kafka recovery must include consumer-offset strategy.
16. Kafka replay must assume duplicate delivery.
17. Critical consumers must be idempotent.
18. Kafka retention must support required replay windows or another recovery source must exist.
19. Transactional outbox recovery must preserve pending committed events.
20. Outbox recovery must tolerate duplicate publication.
21. Cache-only Redis should normally be reconstructed rather than treated as authoritative state.
22. Kubernetes infrastructure must be reconstructable declaratively.
23. GitOps repositories must support reconstruction of deployment state.
24. Recovery should use previously approved immutable application artifacts.
25. Secret-management and KMS infrastructure must be included in DR.
26. Known-compromised credentials must never be restored.
27. DNS/global traffic management must be included in regional recovery design.
28. Recovery must occur in dependency order.
29. Circular bootstrap dependencies must have explicit resolution procedures.
30. Recovery automation must be periodically executed.
31. DR capacity must support minimum required business throughput.
32. Business continuity must be validated beyond Kubernetes health.
33. Critical workflows require post-recovery reconciliation.
34. Failover must have explicit authorization.
35. Traffic must not activate before recovery validation.
36. Failback must be treated as a separate controlled operation.
37. Security controls must remain active during DR.
38. Ransomware recovery must use a trusted recovery point.
39. Tier 0/Tier 1 capabilities must undergo periodic DR exercises.
40. DR exercises must measure actual RTO and RPO.
41. DR findings must have owners and target dates.
42. Backup and replication health must be monitored.
43. Replication lag threatening RPO must generate an operational signal.
44. DR documentation must remain accessible during primary-platform failure.
45. DR ownership must be explicit.
46. New stateful technologies require recovery design before production adoption.
47. Cross-region recovery must respect data-residency requirements.
48. DR architecture cost must be proportional to criticality.
49. Recovery completion requires business validation.
50. Untested recovery capability must not be represented as proven DR capability.

---

# 247. Disaster Recovery Readiness Gate

A Tier 0/Tier 1 capability is not considered DR-ready until:

```text
[ ] Business criticality classified

[ ] DR owner defined

[ ] RTO approved

[ ] RPO approved

[ ] Failure scenarios documented

[ ] Dependency map documented

[ ] Backup mechanism configured

[ ] Backup retention defined

[ ] Backup encryption validated

[ ] Backup immutability reviewed

[ ] Restore tested

[ ] PITR validated where required

[ ] Database failover documented

[ ] Database fencing validated

[ ] Kafka recovery strategy documented

[ ] Consumer offsets considered

[ ] Consumers idempotent

[ ] Outbox recovery validated

[ ] Redis recovery strategy defined

[ ] Kubernetes reconstruction automated

[ ] GitOps recovery validated

[ ] Artifact registry recovery validated

[ ] Secret Manager recovery validated

[ ] KMS recovery validated

[ ] DNS/traffic failover documented

[ ] Recovery order documented

[ ] Bootstrap dependencies resolved

[ ] Recovery capacity validated

[ ] Reconciliation procedure documented

[ ] Failback procedure documented

[ ] Security-disaster procedure documented

[ ] DR exercise completed

[ ] Actual RTO measured

[ ] Actual RPO measured

[ ] Corrective actions tracked
```

---

# 248. Validation

This ADR will be validated through:

- automated backup validation
- PostgreSQL restore tests
- PostgreSQL PITR exercises
- database failover tests
- database fencing tests
- Kafka recovery tests
- consumer-offset recovery tests
- event replay exercises
- outbox recovery tests
- Redis reconstruction tests
- Kubernetes cluster reconstruction
- GitOps bootstrap tests
- secret-manager recovery exercises
- KMS recovery exercises
- DNS/traffic failover tests
- dependency-loss scenarios
- regional recovery exercises
- ransomware tabletop exercises
- clean-room recovery exercises where appropriate
- failback tests
- reconciliation validation
- integrated DR drills

---

# 249. Success Criteria

The decision is successful when:

- every critical capability has known RTO/RPO
- recovery architecture matches business criticality
- backups are demonstrably restorable
- critical data can be restored to the required RPO
- Tier 0/Tier 1 capabilities can recover within measured RTO
- PostgreSQL corruption can be recovered through PITR where required
- database failover does not create split brain
- Kafka recovery does not lose control of offsets/replay
- outbox recovery preserves committed integration events
- Kubernetes can be reconstructed without undocumented manual knowledge
- application state can be restored from GitOps
- required secrets and keys can be recovered
- regional traffic can be redirected safely
- post-recovery reconciliation identifies inconsistencies
- failback can occur without overwriting newer authoritative data
- ransomware recovery does not depend on compromised production assets
- DR exercises regularly validate the architecture

---

# 250. Alternatives Rejected

## 250.1 Backups Only

Rejected because backup alone does not define infrastructure, dependency, traffic or business recovery.

---

## 250.2 Replication Only

Rejected because replication can propagate logical corruption.

---

## 250.3 Multi-AZ as Complete DR

Rejected because Multi-AZ does not protect against every regional or logical disaster.

---

## 250.4 Active-Active Everywhere

Rejected because complexity and cost are not justified for every service.

---

## 250.5 Manual Recovery

Rejected for critical services because it is slow, error prone and dependent on tribal knowledge.

---

## 250.6 Rebuild Everything From Source During Disaster

Rejected because exact immutable approved artifacts provide stronger reproducibility.

---

## 250.7 Restore Latest Backup Automatically

Rejected because the latest backup may already contain corruption or compromise.

---

## 250.8 DR Without Reconciliation

Rejected because technical infrastructure recovery does not guarantee business-state correctness.

---

## 250.9 DR Documentation Without Exercises

Rejected because untested procedures do not demonstrate recoverability.

---

# 251. Related Decisions

This ADR is related to:

- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-009: Use Apache Kafka for Integration Events
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-024: Adopt Software Supply Chain Security
- ADR-025: Adopt Kubernetes Runtime Security Standards
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards

---

# 252. References

- Google Site Reliability Engineering
- Google Site Reliability Workbook
- AWS Well-Architected Framework — Reliability Pillar
- Azure Well-Architected Framework — Reliability
- PostgreSQL Backup and Restore Documentation
- PostgreSQL Continuous Archiving and PITR
- Apache Kafka Documentation
- Kubernetes Documentation
- OpenGitOps Principles
- NIST Cybersecurity Framework
- NIST Contingency Planning Guide
- CIS Controls
- OWASP
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-025: Adopt Kubernetes Runtime Security Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards

---

# 253. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial Disaster Recovery and Business Continuity baseline |

---

# 254. Decision Summary

The Enterprise Order Platform distinguishes:

```text
HIGH AVAILABILITY

Routine Failure

↓

Self-Healing

↓

Continue Service
```

from:

```text
DISASTER RECOVERY

Catastrophic Failure

↓

Declare Disaster

↓

Recover Trusted Infrastructure

↓

Restore Data

↓

Reconcile

↓

Validate

↓

Restore Business Capability
```

Criticality drives architecture:

```text
BUSINESS IMPACT

↓

TIER

↓

RTO + RPO

↓

RECOVERY TOPOLOGY

↓

BACKUP + REPLICATION

↓

RECOVERY PROCEDURE

↓

DR EXERCISE
```

For PostgreSQL:

```text
REPLICATION
      +
BACKUP
      +
WAL / PITR
      +
FENCING
      +
RESTORE TEST
      +
RECONCILIATION
```

For Kafka:

```text
TOPIC DATA
      +
CONSUMER OFFSETS
      +
RETENTION
      +
IDEMPOTENCY
      +
CONTROLLED REPLAY
```

For the transactional outbox:

```text
BUSINESS TRANSACTION
        +
OUTBOX EVENT
        |
        v
DATABASE RECOVERY
        |
        v
OUTBOX DISPATCH
        |
        v
KAFKA
        |
        v
IDEMPOTENT CONSUMERS
```

For Kubernetes:

```text
INFRASTRUCTURE AS CODE

↓

CLUSTER

↓

PLATFORM SERVICES

↓

IDENTITY / SECRETS

↓

GITOPS

↓

DECLARATIVE APPLICATION STATE

↓

WORKLOADS
```

For ransomware or security compromise:

```text
IDENTIFY TRUSTED RECOVERY POINT

↓

ISOLATE COMPROMISED ENVIRONMENT

↓

PRESERVE EVIDENCE

↓

REBUILD TRUSTED INFRASTRUCTURE

↓

RESTORE IMMUTABLE DATA

↓

ROTATE CREDENTIALS

↓

VALIDATE

↓

RECONCILE

↓

RESTORE TRAFFIC
```

Failover is:

```text
PRIMARY

   X

DISASTER

   ↓

FENCE PRIMARY

   ↓

PROMOTE / RESTORE DR

   ↓

VALIDATE

   ↓

ACTIVATE TRAFFIC
```

Failback is a different operation:

```text
DR IS NOW AUTHORITATIVE

↓

REBUILD PRIMARY

↓

SYNCHRONIZE DR → PRIMARY

↓

VALIDATE

↓

CONTROLLED TRAFFIC SWITCH

↓

OBSERVE

↓

RETIRE TEMPORARY DR STATE
```

The definitive DR test is not:

```text
Backup job = SUCCESS
```

It is:

```text
DISASTER SIMULATED

↓

DATA RESTORED

↓

PLATFORM RECONSTRUCTED

↓

APPLICATION STARTED

↓

BUSINESS TRANSACTION COMPLETED

↓

DATA RECONCILED

↓

MEASURED RTO <= TARGET RTO

AND

MEASURED RPO <= TARGET RPO
```

The platform therefore adopts the principle:

```text
If recovery has never been exercised,
recovery has not been proven.
```
