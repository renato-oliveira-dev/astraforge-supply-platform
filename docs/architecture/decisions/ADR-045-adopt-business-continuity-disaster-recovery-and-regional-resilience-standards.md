# ADR-045: Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-045 |
| Title | Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Business Continuity, Disaster Recovery, Regional Resilience, Backup, Restore |
| Related Work Items | RTO, RPO, Multi-AZ, Multi-Region, PostgreSQL PITR, DR Drills, Chaos Engineering |
| Supersedes | ADR-028 |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform operates business-critical distributed services whose availability depends on multiple layers:

```text
                   BUSINESS CAPABILITY
                           |
                           v
                    APPLICATIONS
                           |
        +------------------+------------------+
        |                  |                  |
        v                  v                  v
    POSTGRESQL           SQS              REDIS
        |                  |                  |
        +------------------+------------------+
                           |
                           v
                    CLOUD PLATFORM
                           |
                           v
                   REGION / NETWORK
```

Normal high availability protects against many infrastructure failures.

It does not automatically protect against:

- regional outage
- destructive deployment
- logical data corruption
- accidental deletion
- credential compromise
- ransomware
- large-scale network failure
- operator error
- cloud control-plane problems
- unrecoverable database corruption

The platform therefore requires explicit business-continuity and disaster-recovery standards.

---

# 2. Problem Statement

The platform requires standards defining:

- business continuity
- disaster recovery
- service criticality
- RTO
- RPO
- backup
- restore
- PostgreSQL PITR
- backup immutability
- backup encryption
- restore testing
- Multi-AZ
- Multi-Region
- regional failover
- regional failback
- DNS
- SQS recovery
- Redis recovery
- object-storage recovery
- external dependency failures
- secrets/configuration recovery
- infrastructure reconstruction
- runbooks
- DR drills
- chaos engineering
- recovery validation
- ownership

---

# 3. Decision Drivers

Primary drivers are:

1. business continuity
2. controlled data loss
3. predictable recovery
4. regional resilience
5. recoverability
6. operational readiness
7. auditable recovery procedures
8. tested backups
9. explicit business tradeoffs
10. reduced disaster uncertainty
11. automated infrastructure recovery
12. dependency-aware resilience

---

# 4. Decision

Business continuity and disaster recovery will be designed from explicit business recovery requirements.

The canonical model is:

```text
               BUSINESS CRITICALITY
                       |
                       v
                  RTO + RPO
                       |
                       v
              RECOVERY STRATEGY
                       |
        +--------------+--------------+
        |              |              |
        v              v              v
      COMPUTE         DATA       INTEGRATIONS
        |              |              |
        +--------------+--------------+
                       |
                       v
                  DR RUNBOOK
                       |
                       v
                    DR TEST
                       |
                       v
               VERIFIED RECOVERY
```

---

# 5. Fundamental Principle

The primary rule is:

```text
A recovery strategy that has never
been successfully tested is an assumption,
not a proven disaster-recovery capability.
```

---

# 6. High Availability vs Disaster Recovery

High availability and disaster recovery are related but different capabilities.

---

# 7. High Availability

High availability primarily protects against failures inside the normal operating environment.

Examples:

```text
Pod failure

Node failure

Single Availability Zone failure

Database instance failure
```

---

# 8. Disaster Recovery

Disaster recovery addresses events requiring restoration or operation from a substantially different recovery state.

Examples:

```text
Region unavailable

Data corrupted

Critical data deleted

Environment destroyed

Large-scale security incident
```

---

# 9. Multi-AZ Is Not Multi-Region

Multi-AZ improves availability within a region.

It must not automatically be described as regional disaster recovery.

---

# 10. Backup Is Not Disaster Recovery

A backup is one component of recovery.

It does not by itself provide:

- infrastructure
- routing
- credentials
- application deployment
- integration recovery
- operational procedures

---

# 11. Service Criticality

Every production capability should have an explicit business criticality.

---

# 12. Suggested Classification

A platform may use:

```text
TIER 0 — Mission Critical

TIER 1 — Business Critical

TIER 2 — Important

TIER 3 — Non-Critical
```

Exact terminology may follow enterprise standards.

---

# 13. Tier 0

Potential characteristics:

- severe business interruption if unavailable
- major financial impact
- regulatory impact
- broad customer impact

---

# 14. Tier 1

Potential characteristics:

- significant operational/business impact
- limited tolerance for extended downtime

---

# 15. Tier 2

Potential characteristics:

- meaningful impact
- temporary manual workaround may exist

---

# 16. Tier 3

Potential characteristics:

- limited immediate business impact
- longer recovery acceptable

---

# 17. Criticality Drives Recovery

Recovery requirements must follow business criticality rather than applying maximum resilience indiscriminately.

---

# 18. RTO

Recovery Time Objective defines the targeted maximum duration to restore a business capability after a qualifying disaster.

---

# 19. RPO

Recovery Point Objective defines the targeted maximum acceptable amount of data loss measured in time.

---

# 20. RTO and RPO Are Business Requirements

Engineering should not invent arbitrary RTO/RPO values without business context.

---

# 21. Example Classification

Illustrative only:

| Tier | RTO | RPO |
|---|---:|---:|
| Tier 0 | ≤ 30 min | ≤ 5 min |
| Tier 1 | ≤ 2 h | ≤ 15 min |
| Tier 2 | ≤ 8 h | ≤ 1 h |
| Tier 3 | ≤ 24 h | ≤ 24 h |

Actual values require business approval.

---

# 22. RTO Starts at Incident

RTO must account for the complete recovery process, not merely application startup.

Conceptually:

```text
DETECTION
   +
DECISION
   +
RECOVERY
   +
VALIDATION
   +
TRAFFIC RESTORATION
   <=
RTO
```

---

# 23. RPO Zero

An RPO of zero requires architecture capable of preventing committed-data loss for the relevant failure scenario.

It must not be declared casually.

---

# 24. Recovery Dependency

A service cannot have a realistic recovery objective better than an unrecoverable mandatory dependency.

---

# 25. Dependency Mapping

Critical services must identify:

```text
Upstream Dependencies

Downstream Dependencies

Databases

SQS

Redis

Identity

DNS

External APIs

Secrets

Certificates
```

---

# 26. Recovery Chain

A business capability may depend on several services.

Its effective recovery is constrained by the slowest mandatory dependency.

---

# 27. Recovery Architecture

Recovery architecture must address:

```text
APPLICATION

DATA

CONFIGURATION

SECRETS

NETWORK

DNS

MESSAGING

IDENTITY

OBSERVABILITY
```

---

# 28. Infrastructure as Code

Recoverable infrastructure should be reproducible through Infrastructure as Code wherever practical.

---

# 29. Manual Infrastructure

Critical infrastructure depending on undocumented manual recreation is a DR risk.

---

# 30. IaC Repository

Infrastructure definitions require:

- version control
- ownership
- review
- secure state management

---

# 31. Recovery From Source

The platform should be capable of reconstructing substantial infrastructure from controlled source artifacts.

---

# 32. Golden Configuration

Recovery must not depend solely on configuration existing inside the failed environment.

---

# 33. Artifact Availability

Recovery requires access to deployable application artifacts.

---

# 34. Artifact Retention

Required production artifacts must remain available for the recovery horizon.

---

# 35. Artifact Integrity

Recovered artifacts must retain supply-chain integrity controls.

---

# 36. Backup Strategy

Persistent business data requires backup according to its RPO, retention and compliance requirements.

---

# 37. Backup Dimensions

A backup strategy must define:

```text
What

Frequency

Retention

Location

Encryption

Ownership

Restore Procedure

Validation
```

---

# 38. Backup Encryption

Backups containing sensitive or production data must be encrypted according to enterprise security standards.

---

# 39. Backup Access

Backup access must follow least privilege.

---

# 40. Backup Isolation

Where threat models justify it, backup copies should have isolation from the primary operational environment.

---

# 41. Immutable Backup

Critical datasets should consider immutable or write-protected backup mechanisms to reduce destructive-attack risk.

---

# 42. Backup Retention

Retention must satisfy:

- RPO
- recovery needs
- legal requirements
- compliance
- cost governance

---

# 43. Infinite Backup Retention

Indefinite backup retention must not be the default.

---

# 44. Backup Monitoring

Backup jobs must be monitored.

---

# 45. Backup Success Is Not Restore Success

A successful backup status only confirms that the backup process reported success.

It does not prove the data can be restored correctly.

---

# 46. Restore Testing

Critical backups must be periodically restored into an isolated environment.

---

# 47. Restore Validation

A restore test should verify:

```text
Backup readable

Database starts

Schema valid

Critical data exists

Application can connect

Representative business queries succeed
```

---

# 48. Restore Frequency

Restore-test frequency should follow criticality.

---

# 49. Restore Evidence

DR governance should retain evidence of successful recovery tests.

---

# 50. PostgreSQL Recovery

PostgreSQL recovery must support the service's RPO/RTO.

---

# 51. PostgreSQL HA

Production databases should use appropriate high-availability mechanisms.

---

# 52. Point-in-Time Recovery

Critical PostgreSQL databases should support Point-in-Time Recovery where required by RPO.

---

# 53. PITR Purpose

PITR can recover to a point before events such as:

```text
Accidental DELETE

Incorrect migration

Logical corruption
```

within configured recovery capability.

---

# 54. PITR Is Not Automatic Business Recovery

After PITR, applications and integrations may require reconciliation.

---

# 55. Recovery Point Selection

Logical corruption recovery requires determining a safe recovery point.

---

# 56. Data Reconciliation

If restoring a database to an earlier point:

```text
DB Time = T1
```

while external systems continued operating until:

```text
T2
```

the interval:

```text
T1 -> T2
```

requires reconciliation analysis.

---

# 57. PostgreSQL Restore Test

Periodic restore testing should include actual PostgreSQL technology compatible with production.

---

# 58. H2 Is Not DR Validation

An H2 test does not validate PostgreSQL backup/recovery behavior.

---

# 59. Flyway and Recovery

Recovered databases must preserve Flyway migration history.

---

# 60. Flyway History

Disaster recovery does not authorize rewriting already applied migration history.

---

# 61. Migration After Restore

If a corrective schema change is required after recovery, create a new Flyway migration.

---

# 62. Database Failover

Database failover behavior must be understood by applications.

---

# 63. Connection Recovery

Applications must recover from broken/stale database connections after failover.

---

# 64. DNS/Endpoint Changes

If database endpoints change during failover, application connection behavior must be tested.

---

# 65. SQS Recovery

SQS requires explicit disaster-recovery analysis.

---

# 66. SQS Availability

Broker replication within a region improves availability but does not automatically provide regional DR.

---

# 67. Event Recovery Requirements

For each critical event stream, determine whether disaster recovery requires:

```text
No Event Loss

Bounded Event Loss

Replay Capability

Cross-Region Replication

Source-System Reconstruction
```

---

# 68. SQS Source of Truth

SQS must not automatically be treated as the permanent source of truth for every business entity.

---

# 69. Event Replay

Consumers requiring replay must have sufficient retention or another event-reconstruction mechanism.

---

# 70. SQS Retention and RTO

Long event replay windows can significantly increase recovery time.

---

# 71. Replay Throughput

DR testing should consider whether consumers can process backlog fast enough to meet RTO.

---

# 72. Consumer Idempotency

Recovery/replay reinforces the requirement for idempotent consumers.

---

# 73. Duplicate Events

DR procedures must assume duplicate delivery may occur.

---

# 74. Event Ordering

Recovery procedures must consider ordering requirements where business semantics depend on order.

---

# 75. Cross-Region SQS

Cross-region replication may be adopted when justified by:

- RPO
- RTO
- event criticality
- operational complexity
- cost

---

# 76. Replication Lag

Cross-region replication does not imply zero RPO.

Replication lag must be measured.

---

# 77. SQS Failback

Returning to the original SQS environment requires controlled message/event reconciliation.

---

# 78. Redis Recovery

Redis recovery requirements depend on how Redis is used.

---

# 79. Redis as Cache

If Redis contains reconstructable cache data:

```text
LOSS
 |
 v
REBUILD CACHE
```

may be acceptable.

---

# 80. Cache Recovery

For reconstructable caches, restoring stale cache data may be worse than rebuilding from the authoritative source.

---

# 81. Redis as Critical State

If Redis contains non-reconstructable business state, stronger persistence/recovery requirements apply.

---

# 82. Architectural Warning

Critical non-reconstructable business data should not reside only in an ephemeral cache architecture.

---

# 83. Cache Stampede During Recovery

Cold-cache recovery can create sudden load against databases and APIs.

---

# 84. Recovery Throttling

Cache warming/repopulation may require controlled concurrency.

---

# 85. Object Storage Recovery

Object storage containing business-critical artifacts/data requires:

- retention
- versioning where appropriate
- replication where required
- access controls
- recovery procedures

---

# 86. Object Versioning

Versioning may protect against accidental overwrite/deletion where justified.

---

# 87. Cross-Region Replication

Cross-region object replication should follow business recovery requirements rather than being enabled universally.

---

# 88. Secret Recovery

Applications cannot recover without required secrets/credentials.

---

# 89. Secret Management

Critical secrets should be recoverable through approved secret-management mechanisms.

---

# 90. Secrets in Backup

Do not solve secret recovery by placing plaintext secrets into ordinary backup archives.

---

# 91. Certificate Recovery

Certificates and trust configuration required for recovery must be included in DR planning.

---

# 92. Key Management

Encrypted backups require recoverable key-management infrastructure.

---

# 93. Encryption Key Loss

A perfect backup without the required decryption key is unrecoverable.

---

# 94. Identity Dependency

Authentication/authorization platforms are critical recovery dependencies.

---

# 95. External Dependencies

Third-party/external systems may remain unavailable even when the platform successfully fails over.

---

# 96. Dependency Classification

External dependencies should be classified as:

```text
MANDATORY

DEGRADABLE

ASYNCHRONOUS

OPTIONAL
```

---

# 97. Mandatory Dependency

If a mandatory external dependency is unavailable, the business capability may remain unavailable.

---

# 98. Degradable Dependency

Where possible, services should degrade gracefully when non-critical dependencies fail.

---

# 99. Asynchronous Dependency

Asynchronous integration may permit local business processing while downstream recovery occurs later.

---

# 100. Circuit Breakers During Disaster

Resilience4j protects against repeated calls to failing dependencies but does not itself provide disaster recovery.

---

# 101. Retry Storm

Disaster conditions must not trigger uncontrolled retry storms.

---

# 102. Recovery Traffic

Recovery often creates abnormal traffic:

```text
Retries

Replays

Cache Misses

Backlog Processing

Reconnections
```

Capacity planning must account for this.

---

# 103. Multi-AZ

Critical production services should normally tolerate loss of a single Availability Zone where platform capabilities support it.

---

# 104. Replica Distribution

Application replicas should be distributed across failure domains where appropriate.

---

# 105. Single-AZ Hidden Dependency

A Multi-AZ application depending on a Single-AZ mandatory component is not truly Multi-AZ at the business-capability level.

---

# 106. Multi-Region

Multi-Region is an architectural decision, not a default requirement.

---

# 107. Multi-Region Cost

Multi-Region introduces substantial:

- infrastructure cost
- data-replication complexity
- consistency complexity
- operational complexity
- testing requirements

---

# 108. Multi-Region Justification

Adopt Multi-Region when business RTO/RPO cannot reasonably be achieved with regional reconstruction/restore strategies.

---

# 109. Regional Strategies

Potential strategies include:

```text
BACKUP AND RESTORE

PILOT LIGHT

WARM STANDBY

ACTIVE/PASSIVE

ACTIVE/ACTIVE
```

---

# 110. Backup and Restore

Characteristics:

```text
Lower cost

Higher RTO

Potentially higher RPO
```

---

# 111. Pilot Light

Critical foundational components remain available in the recovery region while broader capacity is provisioned during disaster.

---

# 112. Warm Standby

A reduced but functional environment exists in the recovery region.

---

# 113. Active/Passive

One region handles normal production traffic while another is maintained for failover.

---

# 114. Active/Active

Multiple regions actively serve production traffic.

---

# 115. Active/Active Complexity

Active/Active introduces difficult questions involving:

- data consistency
- conflict resolution
- global routing
- event ordering
- identity
- distributed transactions

It must not be selected merely because it appears maximally resilient.

---

# 116. Strategy Selection

The simplest strategy capable of satisfying approved RTO/RPO should generally be preferred.

---

# 117. Regional Data Replication

Data replication strategy must align with consistency and RPO requirements.

---

# 118. Asynchronous Replication

Asynchronous cross-region replication normally implies a non-zero RPO.

---

# 119. Synchronous Cross-Region Replication

Synchronous replication can increase write latency and failure coupling.

It requires explicit justification.

---

# 120. Regional Independence

A DR region should avoid unnecessary dependencies on components that fail together with the primary region.

---

# 121. Hidden Regional Dependency

Examples include:

```text
Region-B application
     |
     v
Region-A database
```

Such architecture does not provide independent regional recovery.

---

# 122. DNS Recovery

Traffic routing is part of disaster recovery.

---

# 123. DNS TTL

DNS TTL should support the intended failover strategy.

---

# 124. Extremely Long TTL

Long DNS caching can delay failover.

---

# 125. Extremely Short TTL

Very short TTL may increase DNS load and does not guarantee every client honors it precisely.

---

# 126. Global Traffic Management

Global load-balancing/traffic-management services may be used where regional failover requires them.

---

# 127. Health-Based Routing

Automated routing requires reliable health signals.

---

# 128. False Failover

Poor health signals can route traffic away from a healthy region.

---

# 129. Automatic Regional Failover

Automatic failover should only be used when failure detection and recovery behavior are sufficiently trustworthy.

---

# 130. Manual Approval

Some disaster scenarios may require human approval before regional failover.

---

# 131. Failover Decision

Runbooks must define:

```text
WHO

CAN DECLARE

A DISASTER
```

---

# 132. Disaster Declaration

Disaster declaration authority should be explicit.

---

# 133. Incident Command

Major disaster recovery should operate under an Incident Commander.

---

# 134. Failover Runbook

A regional failover runbook must contain actionable steps.

---

# 135. Runbook Content

Applicable:

```text
[ ] Confirm disaster criteria

[ ] Establish incident command

[ ] Freeze risky changes

[ ] Assess data state

[ ] Select recovery point

[ ] Restore/activate infrastructure

[ ] Restore/activate databases

[ ] Restore messaging

[ ] Restore secrets/configuration

[ ] Deploy applications

[ ] Validate dependencies

[ ] Execute smoke tests

[ ] Switch traffic

[ ] Monitor errors/latency

[ ] Validate business transactions

[ ] Communicate status
```

---

# 136. Failback

Failback is a separate recovery operation.

---

# 137. Failback Is Not Reverse Failover

Data changed in the recovery environment must be reconciled before returning traffic.

---

# 138. Failback Plan

A DR strategy is incomplete if it explains failover but not how normal architecture will eventually be restored.

---

# 139. Failback Requirements

Applicable:

```text
Data synchronization

Event reconciliation

Traffic migration

Validation

Rollback path
```

---

# 140. Split Brain

Multi-region systems must explicitly prevent or manage split-brain conditions.

---

# 141. Dual Writer Risk

Two independent regions writing to a single logical dataset without conflict strategy can corrupt business state.

---

# 142. Fencing

Fencing mechanisms should prevent unauthorized/obsolete writers where required.

---

# 143. Recovery Consistency

After recovery, technical health alone is insufficient.

Business consistency must be validated.

---

# 144. Business Validation

Examples:

```text
Can an order be created?

Can checkout complete?

Can order status progress?

Can customer data be read?

Are integration events flowing?
```

---

# 145. Technical Smoke Test

Technical tests should verify:

- health endpoints
- database connectivity
- SQS connectivity
- Redis connectivity
- authentication

---

# 146. Business Smoke Test

Business smoke tests verify end-to-end capability.

---

# 147. DR Validation Layers

```text
INFRASTRUCTURE
      |
      v
TECHNICAL SERVICES
      |
      v
APPLICATION
      |
      v
BUSINESS JOURNEY
```

All applicable layers require validation.

---

# 148. Recovery Observability

The recovery environment must provide:

- logs
- metrics
- traces where applicable
- dashboards
- alerts

---

# 149. Observability Must Recover Too

A recovered application without observability significantly increases recovery risk.

---

# 150. DR Runbooks

Critical services require maintained DR runbooks.

---

# 151. Runbook Location

Runbooks must remain accessible even if the primary production environment is unavailable.

---

# 152. Runbook Versioning

Runbooks should be version controlled where practical.

---

# 153. Runbook Owner

Every runbook requires an owner.

---

# 154. Runbook Review

Runbooks must be updated after architecture changes affecting recovery.

---

# 155. DR Drill

Critical recovery procedures must be exercised periodically.

---

# 156. Drill Types

Potential drills:

```text
Tabletop Exercise

Backup Restore

Database PITR

AZ Failure

Dependency Failure

Regional Failover

Regional Failback
```

---

# 157. Tabletop Exercise

A tabletop validates decision-making and procedures without necessarily changing production infrastructure.

---

# 158. Technical Exercise

A technical DR exercise validates actual recovery mechanisms.

---

# 159. Tabletop Is Not Enough

For critical services, discussion alone does not prove technical recoverability.

---

# 160. Drill Frequency

Frequency should follow criticality and enterprise policy.

---

# 161. Example Cadence

Illustrative:

```text
Tier 0 -> at least twice yearly

Tier 1 -> at least yearly

Tier 2 -> periodic restore test

Tier 3 -> risk-based
```

Actual cadence requires enterprise approval.

---

# 162. Drill Measurement

Measure:

```text
Actual Recovery Time

Actual Recovery Point

Manual Steps

Failures

Unexpected Dependencies

Validation Results
```

---

# 163. Actual RTO

If the objective is:

```text
RTO = 2 hours
```

but the tested recovery takes:

```text
5 hours
```

the actual capability does not meet the stated RTO.

---

# 164. Recovery Debt

The gap must be treated as operational/technical debt.

---

# 165. DR Drill Failure

A failed DR test is valuable evidence and requires remediation.

It must not be hidden merely to report compliance.

---

# 166. Chaos Engineering

Chaos Engineering may be used to validate resilience assumptions.

---

# 167. Chaos Objective

Chaos experiments should test hypotheses.

Example:

```text
If one pod fails,
the service remains available
within SLO.
```

---

# 168. Chaos Is Not Random Destruction

Uncontrolled failure injection without a hypothesis, blast-radius control and abort condition is prohibited.

---

# 169. Experiment Design

A chaos experiment requires:

- hypothesis
- expected behavior
- blast radius
- observability
- abort condition
- owner

---

# 170. Start Small

Chaos experiments should begin with limited blast radius.

---

# 171. Pre-Production First

New destructive experiments should generally be validated outside production first.

---

# 172. Production Chaos

Production chaos testing may be appropriate for mature systems with adequate controls.

---

# 173. Example Experiments

```text
Terminate application pod

Remove one node

Simulate dependency timeout

Interrupt Redis connection

Break database connection

Introduce SQS service failure
```

according to environment and safety controls.

---

# 174. Regional Chaos

Regional failure simulation requires significantly stronger governance.

---

# 175. Game Day

A Game Day combines teams and controlled failure scenarios to validate:

- technology
- communication
- ownership
- runbooks
- decision-making

---

# 176. Security Disaster Recovery

Security incidents may require different recovery procedures.

---

# 177. Credential Compromise

Recovery may require:

```text
Credential rotation

Secret revocation

Certificate replacement

Session invalidation
```

---

# 178. Compromised Backup

Recovery planning must consider whether backups could contain compromised data or malicious persistence.

---

# 179. Clean Recovery Point

Security recovery may require identifying a trusted point before compromise.

---

# 180. Forensic Preservation

Security incidents may require preserving evidence before destructive recovery operations.

---

# 181. Business Continuity

Business continuity extends beyond technical restoration.

---

# 182. Manual Workaround

For some capabilities, temporary manual procedures may reduce required technical RTO.

---

# 183. Manual Procedure Validation

A manual workaround must itself be documented and feasible.

---

# 184. Data Reentry

If business operations continue manually during outage, post-recovery data reentry/reconciliation must be planned.

---

# 185. Communication

Major DR events require communication procedures.

---

# 186. Stakeholders

Applicable stakeholders may include:

```text
Engineering

Operations

Security

Business

Customer Support

Leadership

External Partners
```

---

# 187. Status Communication

Recovery communication should distinguish:

```text
Incident Detected

Disaster Declared

Recovery In Progress

Traffic Restored

Business Validated

Incident Closed
```

---

# 188. Change Freeze

During major disaster recovery, unrelated production changes should normally be frozen.

---

# 189. Recovery Changes

Emergency recovery changes must remain traceable.

---

# 190. Configuration Drift

The DR environment must not silently diverge from the primary environment.

---

# 191. IaC Reduces Drift

Infrastructure as Code should be used to minimize regional/environmental configuration drift.

---

# 192. DR Environment Testing

Standby infrastructure should be periodically validated.

---

# 193. Dormant Standby Risk

Infrastructure that remains unused for years may fail when finally needed due to:

- expired credentials
- outdated configuration
- missing dependencies
- incompatible versions

---

# 194. Dependency Version

Recovery environments must use supported compatible software versions.

---

# 195. Capacity in Recovery Region

A recovery region must have sufficient capacity to meet the agreed degraded/normal operating mode.

---

# 196. Cloud Quotas

DR planning must consider regional cloud quotas.

---

# 197. Quota Failure

A recovery strategy requiring creation of hundreds of resources can fail if the recovery region lacks required quota.

---

# 198. Capacity Reservation

Critical recovery strategies may require pre-provisioned/reserved capacity where on-demand availability cannot be assumed.

---

# 199. DR Cost

DR has a recurring cost.

---

# 200. DR Cost vs RTO

Generally:

```text
LOWER RTO / LOWER RPO
          |
          v
HIGHER COST + COMPLEXITY
```

---

# 201. FinOps Integration

DR architecture must therefore align with ADR-044.

---

# 202. Do Not Optimize Away DR

Idle-looking standby resources may be intentional resilience capacity.

---

# 203. Tagging

DR resources should be tagged/identified so FinOps tooling does not incorrectly classify them as ordinary waste.

---

# 204. DR Resource Metadata

Applicable metadata may identify:

```text
dr-purpose

primary-region

recovery-region

service

owner

criticality
```

---

# 205. DR Ownership

Every critical service requires an owner for its recovery capability.

---

# 206. Shared Platform DR

Platform teams own recovery of shared platform components.

---

# 207. Domain Recovery

Domain teams own application/business validation after shared infrastructure recovery.

---

# 208. Joint Recovery

Critical recovery usually requires coordinated platform and domain teams.

---

# 209. Recovery Dependency Ownership

Runbooks should identify the owner of every critical recovery dependency.

---

# 210. Recovery Automation

Repeatable recovery steps should be automated where practical.

---

# 211. Automation Priority

High-value automation includes:

```text
Infrastructure creation

Application deployment

Configuration restoration

Database restore orchestration

Smoke tests

Traffic switching
```

---

# 212. Automation Risk

Automation must not blindly perform destructive operations.

---

# 213. Human Confirmation

Destructive actions may require explicit confirmation.

---

# 214. Recovery Idempotency

Recovery automation should be safely repeatable where practical.

---

# 215. Partial Failure

Automation must report partial recovery clearly.

---

# 216. DR Fitness Functions

Architecture governance should automate recoverability checks where practical.

---

# 217. Potential Fitness Functions

Examples:

```text
Backup job success

Restore-test success

PITR availability

Required IaC exists

DR runbook exists

Runbook owner exists

Service criticality defined

RTO/RPO defined

DR drill not overdue

Required replication healthy
```

---

# 218. Recovery Dashboard

Critical services should expose recovery-readiness information where practical.

---

# 219. Suggested Dashboard

```text
Service

Criticality

RTO

RPO

Last Backup

Last Restore Test

Last DR Drill

Replication Lag

Recovery Region

Runbook
```

---

# 220. Disaster Recovery Governance Gate

A critical production capability is not considered DR-ready until:

```text
[ ] Business criticality defined

[ ] RTO approved

[ ] RPO approved

[ ] Service owner identified

[ ] DR owner identified

[ ] Mandatory dependencies mapped

[ ] Dependency recovery requirements known

[ ] Recovery strategy selected

[ ] Multi-AZ requirements implemented

[ ] Multi-Region decision documented

[ ] Infrastructure reproducible

[ ] Production artifacts recoverable

[ ] Configuration recoverable

[ ] Secrets recoverable

[ ] Encryption keys recoverable

[ ] Database backup configured

[ ] Backup retention defined

[ ] Backup encrypted

[ ] PITR configured where required

[ ] Restore successfully tested

[ ] SQS recovery strategy defined

[ ] Redis recovery strategy defined

[ ] Object storage recovery defined

[ ] DNS failover understood

[ ] Cloud quotas validated

[ ] Recovery capacity validated

[ ] Failover runbook exists

[ ] Failback runbook exists

[ ] Technical smoke tests exist

[ ] Business smoke tests exist

[ ] Observability available during recovery

[ ] Disaster declaration authority defined

[ ] Incident command process defined

[ ] DR drill executed

[ ] Actual recovery time measured

[ ] Actual recovery point measured

[ ] Recovery gaps tracked
```

---

# 221. Anti-Patterns

The following are prohibited or strongly discouraged:

- declaring backup equivalent to DR
- declaring Multi-AZ equivalent to Multi-Region
- RTO/RPO invented solely by engineering
- RPO zero without architecture capable of delivering it
- backups never restored
- relying only on backup-job success
- DR runbooks inaccessible during primary outage
- manual infrastructure reconstruction without documentation
- recovery dependent on artifacts existing only in failed infrastructure
- plaintext secrets in backup archives
- encrypted backups without recoverable keys
- PostgreSQL recovery tested only with H2
- modifying applied Flyway migrations during recovery
- assuming SQS replication automatically provides regional DR
- assuming Redis must always be restored rather than rebuilt
- critical state stored only in non-durable cache
- Multi-Region adopted without business justification
- Active/Active selected merely for perceived sophistication
- failover without failback planning
- uncontrolled dual writers
- DNS omitted from recovery planning
- automatic failover based on unreliable health checks
- DR plans ignoring external dependencies
- uncontrolled retry storms during recovery
- DR environments with insufficient cloud quota
- deleting standby capacity because it appears idle
- tabletop exercises treated as proof of technical recovery
- chaos engineering performed as random destruction
- DR tests that never measure actual RTO/RPO
- failed DR drills hidden for compliance reasons

---

# 222. Positive Consequences

The decision provides:

- explicit recovery expectations
- measurable RTO/RPO
- stronger backup governance
- verified restore capability
- safer PostgreSQL recovery
- explicit SQS/Redis recovery
- clearer Multi-AZ/Multi-Region decisions
- controlled failover/failback
- better regional resilience
- improved operational readiness
- evidence-based DR capability
- safer Chaos Engineering

---

# 223. Negative Consequences

The decision introduces:

- backup/storage cost
- standby infrastructure cost
- DR automation effort
- restore testing
- runbook maintenance
- DR exercises
- regional architecture complexity
- additional operational governance

These costs are accepted because an untested recovery strategy can expose the business to substantially larger losses.

---

# 224. Neutral Consequences

The decision also means:

- not every service requires Multi-Region
- some services may accept longer RTO/RPO
- some caches should be rebuilt instead of restored
- some recovery decisions require human approval
- DR cost increases as recovery objectives become stricter
- different services may legitimately use different recovery strategies

---

# 225. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Regional outage | Critical | Low/Medium | DR strategy |
| Data loss | Critical | Medium | RPO + backup/replication |
| Backup unusable | Critical | Medium | Restore tests |
| Logical corruption | Critical | Medium | PITR |
| Failed failover | Critical | Medium | DR drills |
| Failed failback | High | Medium | Explicit plan |
| Split brain | Critical | Low/Medium | Fencing/consistency design |
| Missing secret/key | Critical | Low | Secret/key recovery |
| Recovery quota shortage | Critical | Medium | Quota validation |
| SQS replay exceeds RTO | High | Medium | Replay testing |
| Cold cache overload | High | Medium | Controlled warm-up |
| DNS delays failover | High | Medium | TTL/routing design |
| Retry storm | High | Medium | Bounded retries |
| Standby configuration drift | High | Medium | IaC + validation |
| DR too expensive | Medium | Medium | Tiered recovery strategy |

---

# 226. Implementation Guidance

The following rules are mandatory:

1. Production capabilities require explicit criticality classification.
2. Critical services require approved RTO and RPO.
3. RTO/RPO must be driven by business requirements.
4. High availability and disaster recovery must remain distinct concepts.
5. Multi-AZ must not be represented as Multi-Region DR.
6. Backups must not be represented as complete DR capability.
7. Critical persistent data requires backup and restore procedures.
8. Critical backups must be periodically restored and validated.
9. Backup encryption and access controls are mandatory.
10. PostgreSQL PITR must be used where required by RPO/logical recovery needs.
11. PostgreSQL recovery testing must use representative PostgreSQL technology.
12. Applied Flyway migrations remain immutable during disaster recovery.
13. SQS recovery/replay requirements must be explicit.
14. Redis recovery must distinguish reconstructable cache from critical state.
15. Critical non-reconstructable state must not depend solely on ephemeral cache.
16. Infrastructure should be reproducible through IaC where practical.
17. Recovery artifacts/configuration/secrets/keys must remain available independently of the failed workload.
18. Multi-Region requires explicit RTO/RPO justification.
19. The simplest recovery strategy satisfying business requirements should generally be preferred.
20. Regional failover must include traffic/DNS planning.
21. Failback must be designed and tested separately from failover.
22. Split-brain/dual-writer risks require explicit controls.
23. Recovery procedures must validate business behavior, not only technical health.
24. Critical DR runbooks require ownership and maintenance.
25. Critical recovery procedures must be periodically exercised.
26. DR tests must measure actual recovery time and recovery point.
27. Gaps against RTO/RPO must be tracked as technical/operational debt.
28. Chaos experiments require hypotheses, blast-radius control and abort conditions.
29. Recovery planning must include mandatory external dependencies.
30. Recovery-region cloud quota/capacity must be validated.
31. DR resources must be protected from accidental FinOps cleanup.
32. Repeatable recovery operations should be automated where safe.
33. DR readiness should be represented through automated fitness functions where practical.

---

# 227. Validation

This ADR will be validated through:

- backup monitoring
- restore tests
- PostgreSQL PITR tests
- infrastructure reconstruction tests
- SQS replay tests
- Redis recovery tests
- DNS/failover tests
- DR tabletop exercises
- technical DR exercises
- regional Game Days
- Chaos Engineering
- runbook reviews
- RTO/RPO measurements
- architecture reviews
- recovery-readiness dashboards
- post-incident reviews

---

# 228. Success Criteria

The decision is successful when:

- every critical capability has approved RTO/RPO
- backup restorability is demonstrated
- recovery does not depend on undocumented knowledge
- PostgreSQL can be restored within required objectives
- SQS replay behavior is understood
- Redis recovery semantics are explicit
- failover and failback are executable
- regional dependencies are understood
- DR runbooks remain current
- cloud quotas do not unexpectedly prevent recovery
- actual DR exercises meet stated objectives
- recovery gaps become visible engineering work
- business capabilities can be validated after recovery

---

# 229. Alternatives Rejected

## 229.1 Backups as the Entire DR Strategy

Rejected because infrastructure, routing, configuration, credentials and operational procedures must also recover.

---

## 229.2 Multi-Region for Every Service

Rejected because complexity and cost must be justified by business RTO/RPO.

---

## 229.3 Active/Active Everywhere

Rejected because it introduces substantial consistency and operational complexity.

---

## 229.4 DR Documentation Without Testing

Rejected because documentation alone does not prove recoverability.

---

## 229.5 Restore Only After a Real Disaster

Rejected because discovering restore failures during an actual disaster is unacceptable.

---

## 229.6 Automatic Failover for Every Failure

Rejected because incorrect detection can cause unnecessary or damaging failovers.

---

## 229.7 Maximum Resilience Regardless of Cost

Rejected because recovery architecture must be proportional to business criticality.

---

# 230. Related Decisions

This ADR is related to:

- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-010: Use Redis for Distributed Caching
- ADR-014: Adopt Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-030: Adopt SQS Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards
- ADR-044: Adopt FinOps, Capacity Efficiency and Cloud Cost Governance Standards
- ADR-046: Adopt Data Governance, Privacy, Retention and Lifecycle Standards

---

# 231. References

- AWS Well-Architected Framework — Reliability Pillar
- AWS Disaster Recovery Guidance
- PostgreSQL Backup and Restore
- PostgreSQL Continuous Archiving and Point-in-Time Recovery
- Amazon SQS Documentation
- Redis Documentation
- Google Site Reliability Engineering
- Chaos Engineering Principles
- NIST Contingency Planning Guidance
- DORA
- FinOps Foundation

---

# 232. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial business continuity and disaster recovery baseline |

---

# 233. Decision Summary

The definitive resilience hierarchy is:

```text
              BUSINESS CRITICALITY
                       |
                       v
                    RTO/RPO
                       |
                       v
               RECOVERY STRATEGY
                       |
        +--------------+--------------+
        |              |              |
        v              v              v
      MULTI-AZ       BACKUP       MULTI-REGION
        |              |              |
        +--------------+--------------+
                       |
                       v
                  DR RUNBOOK
                       |
                       v
                    DR TEST
                       |
                       v
                PROVEN RECOVERY
```

The concepts remain explicitly separated:

```text
HIGH AVAILABILITY
       !=
DISASTER RECOVERY
```

and:

```text
BACKUP
   !=
RESTORE
   !=
DISASTER RECOVERY
```

The complete chain is:

```text
BACKUP
   |
   v
RESTORE
   |
   v
VALIDATE DATA
   |
   v
DEPLOY APPLICATION
   |
   v
RESTORE INTEGRATIONS
   |
   v
SWITCH TRAFFIC
   |
   v
VALIDATE BUSINESS
```

For PostgreSQL:

```text
BASE BACKUP
     +
WAL / RECOVERY DATA
     |
     v
    PITR
     |
     v
SAFE RECOVERY POINT
     |
     v
APPLICATION VALIDATION
     |
     v
DATA RECONCILIATION
```

For SQS:

```text
EVENTS
  |
  v
RETENTION / REPLICATION
  |
  v
RECOVERY
  |
  v
REPLAY
  |
  v
IDEMPOTENT CONSUMERS
```

For Redis:

```text
               REDIS DATA
                   |
          +--------+--------+
          |                 |
          v                 v
     RECONSTRUCTABLE   CRITICAL STATE
          |                 |
          v                 v
      REBUILD CACHE      PERSISTENCE /
                          RECOVERY
```

For regional resilience:

```text
             REQUIRED RTO/RPO
                    |
                    v
             SELECT STRATEGY
                    |
      +-------------+-------------+
      |             |             |
      v             v             v
BACKUP/RESTORE   WARM STANDBY   ACTIVE/ACTIVE
  lower cost       medium          high
  higher RTO       RTO             complexity
```

The strategy is selected from business requirements, not prestige.

Failover is only half the lifecycle:

```text
PRIMARY
   |
   v
DISASTER
   |
   v
FAILOVER
   |
   v
RECOVERY REGION
   |
   v
BUSINESS CONTINUES
   |
   v
RECONCILIATION
   |
   v
FAILBACK
   |
   v
NORMAL OPERATIONS
```

For DR validation:

```text
DOCUMENTATION
      |
      v
TABLETOP
      |
      v
RESTORE TEST
      |
      v
FAILOVER TEST
      |
      v
BUSINESS VALIDATION
      |
      v
MEASURE RTO/RPO
```

The governing equation is:

```text
BACKUPS
   +
INFRASTRUCTURE AS CODE
   +
RECOVERABLE CONFIGURATION
   +
RECOVERABLE SECRETS
   +
RUNBOOKS
   +
TESTED FAILOVER
   +
TESTED FAILBACK
   +
BUSINESS VALIDATION
   =
DISASTER RECOVERY CAPABILITY
```

And the definitive principle is:

```text
We do not claim that a system
can recover because a diagram,
backup job or runbook says so.

We claim it can recover only
after recovery has been exercised,
measured and validated.
```
