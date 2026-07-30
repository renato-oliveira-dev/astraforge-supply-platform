# ADR-047: Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-047 |
| Title | Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Legacy Modernization, Strangler Fig, Migration, Technical Evolution |
| Related Work Items | Java Modernization, Oracle Modernization, Strangler Migration, Legacy Decommission |
| Supersedes | None |
| Superseded By | ADR-087 |

---

# 1. Context

Enterprise platforms commonly evolve over decades.

The existing landscape may therefore contain:

```text
Legacy Java Applications

Modern Java 21 Services

Oracle Forms

Oracle Reports

PL/SQL

Delphi Applications

Batch Processing

Database Procedures

File Integrations

REST APIs

Message-Based Integrations
```

Legacy systems may continue delivering substantial business value while accumulating technical constraints.

Typical constraints include:

- unsupported runtimes
- outdated frameworks
- tightly coupled modules
- shared databases
- undocumented business rules
- synchronous integration
- limited automated testing
- manual deployments
- obsolete libraries
- security vulnerabilities
- difficult scalability
- specialist knowledge concentration

Modernization must address these constraints without unnecessarily risking business continuity.

---

# 2. Problem Statement

The platform requires standards defining:

- modernization strategy
- Strangler Fig
- incremental migration
- legacy containment
- Anti-Corruption Layer
- coexistence
- domain extraction
- database decomposition
- data migration
- CDC
- dual-read
- dual-write
- shadow traffic
- feature flags
- routing
- reconciliation
- cutover
- rollback
- Oracle modernization
- Java modernization
- batch modernization
- integration modernization
- legacy decommission
- migration observability
- business validation
- technical-debt retirement

---

# 3. Decision Drivers

Primary drivers are:

1. business continuity
2. reduced migration risk
3. incremental value delivery
4. technical-debt reduction
5. security modernization
6. runtime modernization
7. architectural decoupling
8. testability
9. operational resilience
10. rollback capability
11. measurable migration progress
12. controlled legacy retirement

---

# 4. Decision

Large legacy systems should generally be modernized incrementally rather than through uncontrolled Big-Bang replacement.

The preferred evolution model is:

```text
                     LEGACY SYSTEM
                          |
                          v
                    DEFINE BOUNDARY
                          |
                          v
                 INTRODUCE FACADE/ACL
                          |
                          v
                  EXTRACT CAPABILITY
                          |
                          v
                    MODERN SERVICE
                          |
                          v
                  MIGRATE TRAFFIC
                          |
                          v
                     VALIDATE
                          |
                          v
                  RETIRE LEGACY PART
```

The cycle repeats until the desired modernization boundary is reached.

---

# 5. Fundamental Principle

The governing rule is:

```text
Modernize business capabilities
incrementally while preserving
business behavior and maintaining
a controlled rollback path.
```

---

# 6. Modernization Is Not Rewrite

Modernization must not automatically mean:

```text
DELETE OLD SYSTEM
      +
WRITE EVERYTHING AGAIN
```

---

# 7. Modernization Options

A component may be:

```text
RETAIN

REHOST

REPLATFORM

REFACTOR

REARCHITECT

REBUILD

REPLACE

RETIRE
```

---

# 8. Decision Per Capability

Modernization strategy should be selected per business capability rather than applying one strategy indiscriminately to an entire portfolio.

---

# 9. Business Value First

Modernization should prioritize business and technical outcomes rather than technology novelty.

---

# 10. Modernization Drivers

Valid drivers include:

```text
Unsupported Technology

Security Risk

High Change Cost

Low Reliability

Performance Limitation

Scalability Limitation

Deployment Risk

Business Agility

Operational Cost

Knowledge Concentration
```

---

# 11. Technology Age Alone

Age alone is insufficient reason to rewrite a stable system.

---

# 12. Legacy Definition

"Legacy" describes architectural/business constraints rather than simply age.

A recent system can become legacy if it is:

- unmaintainable
- unsupported
- untestable
- tightly coupled
- operationally unsafe

---

# 13. Strangler Fig Pattern

Strangler Fig is the preferred pattern for large incremental replacement where applicable.

---

# 14. Canonical Strangler Flow

```text
                    CLIENT
                      |
                      v
               ROUTING/FACADE
                 /         \
                /           \
               v             v
           LEGACY          MODERN
           SYSTEM          SERVICE
```

Traffic gradually moves from legacy capabilities to modern services.

---

# 15. Capability Extraction

Extraction should follow coherent business capabilities.

---

# 16. Avoid Technical Extraction Only

Do not create microservices merely by extracting technical layers such as:

```text
Controller Service

DAO Service

Validation Service
```

---

# 17. Domain Boundary

Extraction should follow domain ownership and bounded-context principles.

---

# 18. Start With Understandable Boundaries

Good initial candidates often have:

- clear business ownership
- manageable dependencies
- measurable behavior
- limited shared state
- meaningful business value

---

# 19. Migration Slice

Prefer vertical slices.

Example:

```text
API
 |
 v
BUSINESS RULE
 |
 v
PERSISTENCE
 |
 v
INTEGRATION
```

rather than migrating only one horizontal layer.

---

# 20. Walking Skeleton

Early modernization should establish a thin end-to-end production-capable path.

---

# 21. Anti-Corruption Layer

Modern services interacting with legacy systems should use an Anti-Corruption Layer where legacy semantics would otherwise leak into the modern domain.

---

# 22. ACL Purpose

The ACL translates:

```text
LEGACY MODEL
      |
      v
ANTI-CORRUPTION LAYER
      |
      v
MODERN DOMAIN MODEL
```

---

# 23. Legacy Vocabulary

Legacy names, codes and technical structures should not automatically become modern domain terminology.

---

# 24. Example

Legacy:

```text
FL_STATUS = 'A'
```

Modern domain:

```text
OrderStatus.APPROVED
```

The translation belongs at the integration boundary.

---

# 25. ACL Responsibilities

An ACL may perform:

- protocol translation
- field mapping
- code translation
- semantic normalization
- error translation
- contract adaptation

---

# 26. ACL Must Not Become New Legacy

The ACL should remain focused on boundary translation.

It must not accumulate unrelated business logic indefinitely.

---

# 27. Legacy Encapsulation

Direct access to legacy implementation details should be progressively reduced.

---

# 28. Shared Database Legacy

A shared database is one of the most difficult modernization constraints.

---

# 29. Database Ownership Target

The target architecture should move toward:

```text
SERVICE
   |
   v
OWNED DATA
```

rather than:

```text
SERVICE A ----\
SERVICE B -----+--> SHARED DATABASE
SERVICE C ----/
```

---

# 30. Shared Database Transition

Immediate physical separation may not always be practical.

Logical ownership should be established first.

---

# 31. Logical Ownership

Even while schemas remain shared temporarily, define which service owns writes for each business dataset.

---

# 32. Single Writer Principle

During migration, prefer one authoritative writer for a business fact whenever possible.

---

# 33. Multiple Writers

Multiple independent writers significantly increase migration complexity and divergence risk.

---

# 34. Database Direct Access

New services should avoid introducing new direct dependencies on legacy tables unless explicitly required as a transitional mechanism.

---

# 35. Transitional Access

Temporary direct legacy database access must have:

- owner
- justification
- migration plan
- removal criterion

---

# 36. Database Views

Views may provide temporary compatibility boundaries but must not become permanent substitutes for service contracts without explicit architectural approval.

---

# 37. Stored Procedures

Existing PL/SQL may contain substantial business knowledge.

---

# 38. Do Not Rewrite PL/SQL Blindly

Before migrating PL/SQL logic:

```text
UNDERSTAND
   |
   v
CHARACTERIZE
   |
   v
TEST
   |
   v
MIGRATE
```

---

# 39. Characterization Tests

When reliable specifications are unavailable, characterization tests should capture existing observable behavior before replacement.

---

# 40. Golden Master

Golden-master testing may be appropriate for deterministic legacy transformations.

---

# 41. Golden Master Caution

Existing behavior may contain defects.

Characterization proves compatibility, not correctness.

---

# 42. Known Legacy Defects

Known defects should be explicitly classified as:

```text
PRESERVE TEMPORARILY

or

CORRECT DURING MIGRATION
```

---

# 43. Behavior Change

A modernization project must not accidentally mix architectural migration with undocumented business-rule changes.

---

# 44. Business Rule Changes

Intentional behavior changes require explicit requirements and tests.

---

# 45. Oracle Forms Modernization

Oracle Forms applications may be modernized incrementally.

---

# 46. Forms Decomposition

Identify:

```text
Presentation Logic

Validation Logic

Business Logic

Database Logic

Integration Logic
```

before deciding migration boundaries.

---

# 47. Forms Trigger Logic

Business rules embedded in Forms triggers must be inventoried before replacing the UI.

---

# 48. Hidden Logic

Logic may exist in:

- WHEN-VALIDATE-ITEM
- PRE-INSERT
- POST-QUERY
- KEY-COMMIT
- program units
- database packages

Modernization must account for all relevant behavior.

---

# 49. Forms as Transitional Client

Oracle Forms may temporarily consume modern service APIs while UI modernization proceeds separately when technically viable.

---

# 50. Database API

Where direct REST consumption is impractical, controlled database/package integration may serve as a temporary bridge.

---

# 51. Transitional Bridge

A transitional bridge must have a retirement plan.

---

# 52. Reports Modernization

Oracle Reports should be analyzed separately from transactional Forms behavior.

---

# 53. Reporting Workload

Reporting may require:

- dedicated read model
- replica
- reporting database
- asynchronous export

rather than loading transactional services.

---

# 54. Delphi Modernization

Delphi applications should be decomposed by business capability rather than translated line-by-line into Java.

---

# 55. Language Translation Is Not Modernization

This:

```text
Delphi Procedure
      |
      v
Equivalent Java Method
```

does not by itself modernize architecture.

---

# 56. Semantic Migration

The target should capture business intent using modern architectural boundaries.

---

# 57. Java Legacy Modernization

Java applications should be modernized incrementally when possible.

---

# 58. Java Runtime

Unsupported Java runtimes should be upgraded according to an explicit compatibility plan.

---

# 59. Runtime Jump

Large runtime jumps require evaluation of:

- language changes
- removed APIs
- dependencies
- frameworks
- JVM behavior
- build tooling
- tests

---

# 60. Java 21 Target

For the current platform baseline, modern Java services should target Java 21 unless a newer approved platform baseline supersedes it.

---

# 61. Framework Modernization

Spring/Spring Boot upgrades must account for:

- Jakarta namespace migration
- Spring Security changes
- Hibernate changes
- configuration changes
- testing changes
- observability changes

---

# 62. Dependency Upgrade

Do not update hundreds of dependencies simultaneously without compatibility validation.

---

# 63. Upgrade Layers

A safer sequence may be:

```text
BUILD TOOL

JDK

FRAMEWORK

DEPENDENCIES

APPLICATION REFACTORING
```

depending on compatibility constraints.

---

# 64. Separate Mechanical From Behavioral Changes

Where practical, separate:

```text
TECHNICAL UPGRADE
```

from:

```text
BUSINESS CHANGE
```

to simplify diagnosis and rollback.

---

# 65. Automated Tests Before Modernization

Critical legacy behavior should gain sufficient automated coverage before substantial refactoring.

---

# 66. Test Pyramid

Modernized services should progressively establish:

```text
UNIT TESTS

COMPONENT TESTS

INTEGRATION TESTS

CONTRACT TESTS

END-TO-END TESTS
```

according to risk.

---

# 67. Coverage Is Not Correctness

Coverage percentage alone does not prove behavior preservation.

---

# 68. Critical Path Coverage

Prioritize:

- business rules
- monetary calculations
- status transitions
- integrations
- persistence
- error behavior

---

# 69. Static Analysis

Modernization should improve:

- Sonar quality
- SAST
- dependency security
- code maintainability

rather than carrying all legacy defects unchanged.

---

# 70. Technical Debt Budget

Not all legacy technical debt must be fixed in the first migration increment.

---

# 71. Debt Classification

Classify debt as:

```text
MUST FIX BEFORE MIGRATION

FIX DURING MIGRATION

DEFER WITH OWNER
```

---

# 72. Security Debt

Critical security vulnerabilities should not be intentionally reproduced in the modern implementation.

---

# 73. Integration Modernization

Legacy integrations should be explicitly inventoried.

---

# 74. Integration Types

Examples:

```text
Database Links

Shared Tables

Files

FTP/SFTP

SOAP

REST

Queues

Kafka

Scheduled Jobs

Stored Procedures
```

---

# 75. Integration Contract

Each integration should have an explicit contract before replacement.

---

# 76. File Integration

File-based integrations may remain valid where business requirements justify them.

---

# 77. File Does Not Automatically Mean Bad

Modernization should not replace a stable batch file integration merely for architectural fashion.

---

# 78. File Governance

Legacy file integrations should nevertheless define:

- layout
- encoding
- sequence
- idempotency
- retry
- reconciliation
- error handling

---

# 79. API Migration

When replacing legacy APIs, preserve compatibility or provide controlled version migration.

---

# 80. Consumer Inventory

Before changing an interface, identify known consumers.

---

# 81. Unknown Consumers

Shared databases and undocumented interfaces frequently create unknown consumers.

Observability should help discover them before cutover.

---

# 82. Traffic Observation

Where possible, observe:

- API access
- database access
- file consumption
- scheduled execution

before removing legacy capabilities.

---

# 83. Shadow Traffic

Shadow traffic may validate modern implementations without serving their result to the user.

---

# 84. Shadow Model

```text
                    REQUEST
                       |
             +---------+---------+
             |                   |
             v                   v
          LEGACY               MODERN
             |                   |
             v                   v
        REAL RESPONSE       SHADOW RESPONSE
             |                   |
             v                   v
           CLIENT             COMPARE
```

---

# 85. Shadow Safety

Shadow execution must not create unintended duplicate side effects.

---

# 86. Read-Only Shadow

Shadow traffic is easiest for read operations.

---

# 87. Write Shadow

Write shadowing requires special care to prevent:

- duplicate orders
- duplicate payments
- duplicate messages
- duplicate external actions

---

# 88. Isolated Shadow Writes

If write behavior must be compared, use isolated targets or controlled simulation where possible.

---

# 89. Response Comparison

Shadow comparison should account for expected nondeterminism.

---

# 90. Ignore Fields

Comparison may ignore fields such as:

```text
generatedId

timestamp

traceId
```

when they are intentionally different.

---

# 91. Semantic Comparison

Compare business meaning rather than raw JSON byte equality where appropriate.

---

# 92. Dual Read

Dual-read may compare old and new data sources.

---

# 93. Dual-Read Pattern

```text
          READ REQUEST
               |
       +-------+-------+
       |               |
       v               v
    LEGACY           MODERN
       |               |
       +-------+-------+
               |
               v
             COMPARE
```

---

# 94. Authoritative Read

During dual-read, one source must remain authoritative for the user-visible result until cutover.

---

# 95. Read Divergence

Divergences must be observable and classified.

---

# 96. Dual Write

Dual-write may be required during some migrations but is inherently risky.

---

# 97. Naive Dual Write

Avoid:

```text
WRITE DB A
   |
   v
WRITE DB B
```

without failure coordination.

---

# 98. Failure Scenario

If:

```text
DB A = SUCCESS

DB B = FAILURE
```

the systems diverge.

---

# 99. Preferred Replication

Where practical, prefer:

```text
AUTHORITATIVE WRITE
        |
        v
      OUTBOX
        |
        v
       EVENT
        |
        v
 SECONDARY MODEL
```

over synchronous independent dual writes.

---

# 100. Transactional Outbox

Transactional Outbox should be considered when reliable event publication is required from a database transaction.

---

# 101. CDC

Change Data Capture may be useful for legacy migration where modifying the legacy application is difficult.

---

# 102. CDC Pattern

```text
LEGACY DATABASE
      |
      v
TRANSACTION LOG
      |
      v
     CDC
      |
      v
MIGRATION PIPELINE
      |
      v
MODERN DATA STORE
```

---

# 103. CDC Benefits

CDC can:

- reduce changes to legacy applications
- replicate changes incrementally
- support low-downtime migration

---

# 104. CDC Limitations

CDC exposes database changes, not necessarily business intent.

---

# 105. Database Event vs Domain Event

A row update is not automatically a domain event.

---

# 106. CDC Mapping

CDC consumers may require an Anti-Corruption Layer to translate legacy database semantics.

---

# 107. CDC Ordering

CDC pipelines must preserve required ordering semantics.

---

# 108. CDC Idempotency

Repeated CDC events must not corrupt the target.

---

# 109. CDC Schema Change

Legacy schema changes can break CDC pipelines.

Schema evolution must therefore be coordinated.

---

# 110. Initial Data Load

Migration commonly requires:

```text
INITIAL SNAPSHOT
       +
INCREMENTAL CHANGES
```

---

# 111. Snapshot Consistency

The initial snapshot must have a well-defined consistency point relative to incremental replication.

---

# 112. Migration Watermark

A migration watermark/checkpoint should identify replication progress.

---

# 113. Reconciliation

Data migration requires reconciliation.

---

# 114. Record Count

Record counts are useful but insufficient.

---

# 115. Reconciliation Dimensions

Validate:

```text
Record Count

Key Coverage

Business Totals

Status Distribution

Null Distribution

Referential Integrity

Representative Records

Business Invariants
```

---

# 116. Financial Reconciliation

For monetary domains, reconcile business totals where meaningful.

---

# 117. Example

```text
LEGACY TOTAL ORDER VALUE
            =
MODERN TOTAL ORDER VALUE
```

for the same defined population and point in time.

---

# 118. Reconciliation Tolerance

Any accepted tolerance must be explicit.

---

# 119. Silent Difference

Unexplained differences must not simply be ignored to meet migration deadlines.

---

# 120. Migration Dashboard

Large migrations should expose:

```text
Records Migrated

Records Pending

Replication Lag

Errors

Reconciliation Differences

Traffic Percentage

Legacy Traffic

Modern Traffic
```

---

# 121. Migration Observability

Modernization requires observability before cutover.

---

# 122. Comparison Metrics

Useful metrics include:

```text
Legacy Error Rate

Modern Error Rate

Legacy Latency

Modern Latency

Response Divergence

Data Divergence
```

---

# 123. Feature Flags

Feature flags may control migration routing.

---

# 124. Flag Examples

```text
orders.modern-read.enabled

orders.modern-write.enabled

orders.modern-percentage
```

---

# 125. Flag Scope

Flags may target:

- environment
- tenant
- customer
- business segment
- percentage

where technically and legally appropriate.

---

# 126. Kill Switch

Critical migration paths should have a rapid rollback/disable mechanism where practical.

---

# 127. Feature Flag Lifecycle

Migration flags are temporary technical mechanisms.

---

# 128. Flag Debt

After migration stabilizes, obsolete flags and dead branches must be removed.

---

# 129. Progressive Traffic

Traffic should be migrated progressively where architecture permits.

Example:

```text
1%

5%

10%

25%

50%

100%
```

Exact stages depend on risk and traffic volume.

---

# 130. Percentage Is Not Mandatory

For low-volume or tenant-specific systems, migration by:

```text
Customer

Region

Business Unit

Capability
```

may provide safer cohorts.

---

# 131. Canary Migration

Early cohorts should provide representative validation while limiting blast radius.

---

# 132. Migration Cohort

Cohorts should be explicitly identifiable.

---

# 133. Cohort Consistency

Avoid routing the same stateful workflow randomly between incompatible implementations.

---

# 134. Sticky Routing

Stateful migrations may require sticky routing by business key.

---

# 135. Example

```text
customerId -> Legacy
```

or:

```text
customerId -> Modern
```

during a controlled migration phase.

---

# 136. Workflow Ownership

A business workflow should not arbitrarily alternate owners mid-transaction unless designed for it.

---

# 137. Cutover

Cutover is the controlled transition of authority from legacy to modern implementation.

---

# 138. Cutover Preconditions

Before cutover:

```text
[ ] Functional validation passed

[ ] Performance validation passed

[ ] Security validation passed

[ ] Data reconciliation passed

[ ] Observability ready

[ ] Capacity validated

[ ] Support team ready

[ ] Rollback tested

[ ] Runbook approved
```

---

# 139. Change Window

High-risk cutovers may use a controlled change window.

---

# 140. Freeze

Some migrations may require a short write freeze.

---

# 141. Zero-Downtime Goal

Zero downtime is desirable but must not be promised without an architecture that supports it.

---

# 142. Controlled Downtime

A short planned outage may be safer than a complex zero-downtime strategy for some systems.

---

# 143. Business Decision

Downtime tradeoffs require business agreement.

---

# 144. Final Synchronization

Before switching write authority:

```text
STOP / CONTROL WRITES
        |
        v
FINAL SYNC
        |
        v
RECONCILE
        |
        v
SWITCH AUTHORITY
```

when the migration strategy requires it.

---

# 145. Write Authority

At cutover, the authoritative writer must be unambiguous.

---

# 146. Read Authority

The authoritative read source should also be explicit.

---

# 147. Rollback

Every significant cutover requires a rollback strategy.

---

# 148. Rollback Is Not Always Symmetric

After the modern system accepts new writes, returning to legacy may require reverse synchronization.

---

# 149. Rollback Window

The migration plan should define the period during which rollback remains technically feasible.

---

# 150. Point of No Return

Some migrations eventually reach a point where rollback becomes disproportionately risky.

This point must be explicit.

---

# 151. Forward Fix

After the point of no return, forward-fix may become safer than rollback.

---

# 152. Rollback Data

Rollback procedures must account for data created after cutover.

---

# 153. Rollback Testing

Critical rollback procedures should be exercised before production cutover where practical.

---

# 154. Migration Runbook

A significant cutover requires an operational runbook.

---

# 155. Runbook Content

Applicable:

```text
Preparation

Preconditions

Traffic Change

Data Synchronization

Validation

Monitoring

Rollback Trigger

Rollback Steps

Communication

Post-Cutover Validation
```

---

# 156. Go/No-Go

High-risk migrations should have explicit Go/No-Go criteria.

---

# 157. Go/No-Go Authority

Decision authority must be identified before the change begins.

---

# 158. Rollback Trigger

Rollback triggers should be measurable where practical.

Examples:

```text
Error Rate > Threshold

Latency > Threshold

Data Divergence

Critical Business Failure

Integration Failure
```

---

# 159. Hypercare

Significant migrations should have a defined post-cutover observation period.

---

# 160. Hypercare Monitoring

Monitor:

```text
Errors

Latency

Business Transactions

Data Reconciliation

Integration Backlogs

Customer Impact
```

---

# 161. Legacy Fallback

Keeping legacy available temporarily may provide fallback.

---

# 162. Fallback Duration

Fallback infrastructure must have an explicit expiration date.

---

# 163. Permanent Fallback

Maintaining two implementations indefinitely creates:

- duplicate cost
- operational complexity
- divergent behavior
- security exposure

---

# 164. Legacy Decommission

Modernization is not complete when traffic reaches the modern service.

It is complete when obsolete legacy components are safely retired.

---

# 165. Decommission Preconditions

Before decommission:

```text
[ ] 100% intended traffic migrated

[ ] Required observation period completed

[ ] No unexplained legacy traffic

[ ] Data reconciliation complete

[ ] Rollback window closed

[ ] Required historical data retained

[ ] Consumers migrated

[ ] Jobs migrated

[ ] Integrations migrated

[ ] Operational procedures updated

[ ] Business owner approves
```

---

# 166. Dependency Discovery

Before shutdown, verify hidden dependencies.

---

# 167. Legacy Traffic

A legacy component showing traffic after expected migration requires investigation.

---

# 168. Scheduled Jobs

Legacy scheduled jobs are common hidden dependencies.

---

# 169. Database Consumers

Check for:

- direct SQL clients
- reports
- DB links
- batch jobs
- external applications

before removing legacy tables/schemas.

---

# 170. File Consumers

Verify whether downstream organizations still consume legacy files.

---

# 171. Authentication Consumers

Legacy credentials/integration accounts should be inventoried before revocation.

---

# 172. Decommission Sequence

A typical sequence is:

```text
STOP TRAFFIC
     |
     v
OBSERVE
     |
     v
DISABLE JOBS
     |
     v
REVOKE INTEGRATIONS
     |
     v
ARCHIVE REQUIRED DATA
     |
     v
REMOVE COMPUTE
     |
     v
REMOVE DATABASE
     |
     v
REMOVE CREDENTIALS
     |
     v
REMOVE MONITORING
```

Actual order depends on architecture.

---

# 173. Disable Before Delete

Where practical, disable a legacy component before irreversible deletion.

---

# 174. Observation Period

A disabled-but-recoverable period can reveal unknown dependencies.

---

# 175. Legacy Data

Legacy data must follow ADR-046 retention and privacy requirements.

---

# 176. Archive

Required historical data may be archived before system removal.

---

# 177. Archive Format

Archive format should remain readable for the required retention period.

---

# 178. Application Dependency

Do not require an unsupported legacy application merely to read legally retained historical data.

---

# 179. Credentials

Legacy credentials must be revoked after decommission.

---

# 180. Network Access

Legacy network rules, firewall rules and endpoints must be removed when no longer needed.

---

# 181. DNS

Obsolete DNS records should be removed or redirected according to migration requirements.

---

# 182. Certificates

Legacy certificates should be revoked/expired appropriately.

---

# 183. Monitoring

Legacy dashboards and alerts should be retired after the component is genuinely decommissioned.

---

# 184. Documentation

Architecture documentation must be updated to remove decommissioned components.

---

# 185. CMDB / Service Catalog

Service inventory must reflect the final state.

---

# 186. Cost

Legacy infrastructure cost should disappear after decommission unless retained for an explicit requirement.

---

# 187. FinOps Verification

ADR-044 cost reporting can help confirm legacy resources were actually removed.

---

# 188. Security Verification

Security tooling should confirm obsolete:

- hosts
- containers
- accounts
- secrets
- endpoints

are removed.

---

# 189. Decommission Evidence

Significant legacy retirement should produce auditable evidence.

---

# 190. Modernization Waves

Large portfolios should be migrated in waves.

---

# 191. Wave Planning

A wave may group capabilities based on:

- domain
- dependency
- business priority
- technical risk
- team capacity

---

# 192. Dependency Graph

Modernization planning should maintain a dependency graph for complex landscapes.

---

# 193. Avoid Random Migration Order

Migrating components without understanding dependencies can create temporary architecture more complex than either the old or target architecture.

---

# 194. Transitional Architecture

Temporary architecture is acceptable when intentional.

---

# 195. Transitional Architecture Must Expire

Every significant transitional mechanism requires:

```text
OWNER

PURPOSE

REMOVAL CONDITION
```

---

# 196. Transitional Debt

Examples include:

```text
Dual Writes

Temporary DB Access

Legacy Adapters

Compatibility APIs

Feature Flags

Synchronization Jobs
```

---

# 197. Transitional Debt Register

Large modernization programs should track transitional architecture explicitly.

---

# 198. Architecture Fitness Functions

Modernization progress should be measurable.

---

# 199. Potential Fitness Functions

Examples:

```text
Unsupported JDK Count

Legacy Framework Count

Direct Legacy DB Dependencies

Legacy API Traffic

Legacy Job Count

Migration Reconciliation Errors

Services Without Automated Tests

Known Critical Vulnerabilities

Expired Migration Flags

Legacy Infrastructure Cost
```

---

# 200. Modernization Dashboard

A portfolio dashboard may expose:

```text
Capabilities Total

Capabilities Migrated

Capabilities Remaining

Legacy Traffic %

Modern Traffic %

Legacy Cost

Critical Risks

Transitional Dependencies

Decommission Candidates
```

---

# 201. Progress Measurement

Lines of code rewritten are not a useful primary modernization KPI.

---

# 202. Better KPIs

Prefer:

```text
Business Capabilities Migrated

Legacy Traffic Removed

Legacy Infrastructure Retired

Deployment Frequency Improved

Lead Time Improved

Incident Rate

Security Risk Reduced

Change Failure Rate
```

---

# 203. DORA Metrics

DORA metrics may help assess whether modernization improves delivery capability.

---

# 204. Modernization Failure

A migration that moves technology but makes delivery slower and less reliable may not have achieved its intended outcome.

---

# 205. Performance Baseline

Critical legacy workloads should have performance baselines before migration.

---

# 206. Performance Comparison

Compare:

```text
Latency

Throughput

Resource Consumption

Error Rate

Batch Duration
```

---

# 207. Batch Modernization

Batch workloads require explicit migration strategy.

---

# 208. Batch Characteristics

Inventory:

```text
Schedule

Input

Output

Volume

Duration

Dependencies

Restartability

Idempotency

SLA
```

---

# 209. Restartability

Modern batch processing should support safe restart where business semantics permit.

---

# 210. Batch Checkpoint

Large jobs may require checkpoints to avoid complete restart.

---

# 211. Duplicate Processing

Batch migration must account for duplicate execution.

---

# 212. File Sequence

Legacy integrations using sequential files must preserve sequencing requirements where external contracts depend on them.

---

# 213. External Protocol

Do not change an external partner's required protocol merely because the internal architecture changed.

---

# 214. Adapter

Use an adapter to isolate external legacy protocols from the modern domain.

---

# 215. Example

```text
MODERN DOMAIN
     |
     v
FILE ADAPTER
     |
     v
LEGACY PARTNER FORMAT
```

---

# 216. Contract Preservation

External contractual compatibility may outlive internal legacy technology.

---

# 217. Modernization Security

Modernized systems must follow current security standards.

---

# 218. Do Not Reproduce Weak Security

Legacy practices such as:

```text
Shared Passwords

Hardcoded Secrets

Weak TLS

Unrestricted DB Accounts
```

must not be intentionally carried forward.

---

# 219. Authentication Migration

Identity migration requires explicit compatibility planning.

---

# 220. Authorization Migration

Modern authorization must preserve required business permissions.

---

# 221. Permission Comparison

Critical migrations should compare authorization behavior.

---

# 222. Excess Permission

A modern system granting broader access than legacy may create a security regression.

---

# 223. Missing Permission

A modern system granting less access may create a business regression.

---

# 224. Audit Migration

Required auditability must remain available throughout migration.

---

# 225. Historical Audit

Legacy audit records subject to retention requirements must remain accessible appropriately after decommission.

---

# 226. Migration Failure Isolation

A migration component should fail without unnecessarily destabilizing the authoritative legacy path.

---

# 227. Shadow Failure

Failure of a non-authoritative shadow system must not normally fail the real legacy transaction.

---

# 228. Replication Backpressure

Migration pipelines must handle backpressure.

---

# 229. Migration Lag

Replication lag must be measurable.

---

# 230. Excessive Lag

If lag exceeds the migration's consistency assumptions, cutover must be blocked.

---

# 231. Data Freeze

A data freeze may be used for final migration when continuous synchronization is disproportionately complex.

---

# 232. Freeze Duration

The expected freeze must be measured and approved.

---

# 233. Migration Capacity

Migration workload must not exhaust production database capacity.

---

# 234. Bulk Extraction

Large data extraction requires:

- batching
- throttling
- monitoring
- restartability

---

# 235. Production Protection

Migration throughput is secondary to production stability.

---

# 236. Historical Data Migration

Not all historical data must necessarily move into the new operational database.

---

# 237. Data Segmentation

Possible strategy:

```text
ACTIVE DATA
    |
    v
MODERN DATABASE

HISTORICAL DATA
    |
    v
ARCHIVE / READ-ONLY STORE
```

when business requirements permit.

---

# 238. Avoid New Database Bloat

Do not migrate obsolete historical data blindly merely to reproduce legacy storage.

---

# 239. Data Governance Integration

Historical-data decisions must follow ADR-046.

---

# 240. DR During Migration

Both old and new architectures require recoverability during coexistence.

---

# 241. Migration DR

ADR-045 applies throughout migration.

---

# 242. Transitional Recovery

Runbooks must understand which system is authoritative at each migration phase.

---

# 243. Disaster During Dual Operation

A disaster occurring during migration must not leave recovery teams guessing which database contains authoritative state.

---

# 244. Authority Matrix

Complex migrations should maintain an authority matrix.

Example:

| Capability | Read Authority | Write Authority | Recovery Source |
|---|---|---|---|
| Customer | Modern | Modern | Modern DB |
| Orders | Legacy | Legacy | Legacy DB |
| Product Snapshot | Modern | Legacy | Rebuild/Event |

---

# 245. Authority Changes

Every authority transition should be explicitly recorded.

---

# 246. Migration Governance

Large modernization initiatives require architecture governance.

---

# 247. Decision Records

Significant transitional decisions should be documented through ADRs where appropriate.

---

# 248. Exception

Exceptions to modernization standards require:

- reason
- owner
- risk
- expiration/review date

---

# 249. Business Ownership

Business owners must participate in validation of migrated capabilities.

---

# 250. Engineering Ownership

Engineering owns technical correctness, operability and migration safety.

---

# 251. Shared Accountability

Successful modernization requires:

```text
BUSINESS
   +
ENGINEERING
   +
ARCHITECTURE
   +
PLATFORM
   +
SECURITY
   +
OPERATIONS
```

---

# 252. Modernization Governance Gate

A significant legacy capability is not ready for production migration until:

```text
[ ] Business capability identified

[ ] Business owner identified

[ ] Technical owner identified

[ ] Current behavior understood

[ ] Legacy dependencies inventoried

[ ] Current data ownership understood

[ ] Target domain ownership defined

[ ] Target architecture approved

[ ] Modernization strategy selected

[ ] Transitional architecture documented

[ ] ACL defined where required

[ ] Data migration strategy defined

[ ] Initial load strategy defined

[ ] Incremental synchronization defined where required

[ ] Reconciliation rules defined

[ ] Authoritative writer defined

[ ] Authoritative reader defined

[ ] Feature flags/routing defined where applicable

[ ] Shadow strategy defined where applicable

[ ] Security validation completed

[ ] Automated tests sufficient for risk

[ ] Performance baseline available

[ ] Performance validation completed

[ ] Observability available

[ ] Migration dashboard available where required

[ ] Cutover plan documented

[ ] Go/No-Go criteria documented

[ ] Rollback strategy documented

[ ] Rollback feasibility tested

[ ] DR strategy understood during migration

[ ] Hypercare plan defined

[ ] Legacy decommission criteria defined

[ ] Transitional mechanisms have removal criteria
```

---

# 253. Legacy Decommission Gate

A legacy component is not considered ready for permanent retirement until:

```text
[ ] Intended traffic is zero

[ ] Unknown traffic investigated

[ ] All known consumers migrated

[ ] Scheduled jobs migrated or retired

[ ] File integrations migrated or retired

[ ] Direct DB consumers migrated

[ ] Required data archived/migrated

[ ] Retention requirements satisfied

[ ] Audit requirements satisfied

[ ] Observation period completed

[ ] Rollback window intentionally closed

[ ] Business validation completed

[ ] Business owner approves retirement

[ ] Credentials can be revoked

[ ] Network access can be removed

[ ] Compute can be removed

[ ] Database can be removed/archived

[ ] Monitoring can be retired

[ ] Documentation updated

[ ] Service catalog updated

[ ] Cost removal verified
```

---

# 254. Anti-Patterns

The following are prohibited or strongly discouraged:

- Big-Bang rewrite without compelling justification
- rewriting a stable system solely because it is old
- line-by-line language translation presented as architecture modernization
- extracting microservices by technical layer
- reproducing legacy database coupling in new services
- creating new permanent direct dependencies on legacy tables
- rewriting PL/SQL without understanding business behavior
- replacing Oracle Forms without inventorying trigger/program-unit logic
- mixing large technical migration with unrelated business-rule changes
- assuming characterization tests prove existing behavior is correct
- naive synchronous dual writes
- dual reads without explicit authority
- CDC treated as equivalent to domain events
- data migration validated only by row counts
- shadow writes causing real duplicate side effects
- feature flags with no removal plan
- permanent coexistence without explicit reason
- cutover without rollback strategy
- rollback ignoring post-cutover writes
- migration without reconciliation
- cutover while replication lag exceeds accepted limits
- migration workloads destabilizing production
- migrating all historical data without business need
- decommissioning legacy before identifying hidden consumers
- leaving legacy credentials active after retirement
- keeping obsolete infrastructure indefinitely "for safety"
- measuring modernization primarily by lines of code rewritten
- considering modernization complete before legacy retirement

---

# 255. Positive Consequences

The decision provides:

- lower modernization risk
- incremental business delivery
- controlled legacy containment
- clearer domain ownership
- safer data migration
- measurable compatibility
- better rollback capability
- improved security
- progressive Java modernization
- controlled Oracle modernization
- explicit transitional architecture
- objective legacy decommission criteria

---

# 256. Negative Consequences

The decision introduces:

- temporary coexistence
- adapters
- synchronization
- reconciliation
- feature flags
- migration dashboards
- additional testing
- transitional infrastructure cost

These costs are accepted because incremental migration materially reduces the risk of large enterprise modernization.

---

# 257. Neutral Consequences

The decision also means:

- some legacy components may remain for years
- not every component requires microservice extraction
- some file integrations may remain
- some PL/SQL may remain appropriate
- temporary duplication is sometimes necessary
- controlled downtime may occasionally be preferable to extreme migration complexity

---

# 258. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Big-Bang failure | Critical | Medium | Incremental Strangler |
| Unknown legacy behavior | High | High | Characterization tests |
| Hidden consumers | High | High | Traffic/dependency discovery |
| Data divergence | Critical | Medium | Single writer + reconciliation |
| Dual-write failure | Critical | Medium | Outbox/CDC |
| CDC lag | High | Medium | Lag monitoring |
| Shadow side effects | Critical | Low/Medium | Isolated/read-only shadow |
| Performance regression | High | Medium | Baseline + comparison |
| Authorization regression | Critical | Medium | Security comparison |
| Rollback impossible | Critical | Medium | Rollback planning |
| Transitional architecture becomes permanent | High | High | Expiration/removal criteria |
| Legacy never decommissioned | High | High | Decommission gate |
| Migration overloads production | Critical | Medium | Throttling |
| Historical data bloat | Medium | High | Retention/archive strategy |
| Business rules accidentally changed | High | Medium | Explicit behavior validation |

---

# 259. Implementation Guidance

The following rules are mandatory:

1. Modernization strategy must be selected per business capability.
2. Big-Bang replacement requires explicit justification.
3. Incremental Strangler migration is preferred for large replaceable legacy systems where applicable.
4. Extraction must follow coherent domain capabilities rather than technical layers.
5. Legacy semantics should be isolated through Anti-Corruption Layers where required.
6. New permanent direct dependencies on legacy databases should be avoided.
7. Transitional legacy DB access requires an owner and removal plan.
8. Existing PL/SQL/Forms logic must be understood before replacement.
9. Characterization tests should capture critical undocumented legacy behavior.
10. Intentional business-rule changes must be distinguished from migration changes.
11. Modern Java services should target the approved Java platform baseline.
12. Technical upgrades should be separated from unrelated behavioral changes where practical.
13. Critical migration paths require sufficient automated tests.
14. Modernization must improve rather than reproduce critical security weaknesses.
15. Integration consumers must be inventoried before contract removal.
16. Shadow traffic must not create uncontrolled side effects.
17. Dual-read requires explicit authoritative result selection.
18. Naive independent dual-write should be avoided.
19. Transactional Outbox or CDC should be preferred where they better preserve consistency.
20. CDC database changes must not automatically be interpreted as domain events.
21. Data migration requires reconciliation beyond simple row counts.
22. Replication lag must be measurable.
23. Feature flags used for migration require lifecycle/removal plans.
24. Traffic migration should be progressive where practical.
25. Stateful workflows require consistent routing during coexistence.
26. Cutover requires explicit Go/No-Go criteria.
27. Significant cutovers require rollback strategies.
28. Rollback must account for data written after cutover.
29. Migration workload must not compromise production stability.
30. Transitional architecture requires owner, purpose and removal condition.
31. DR requirements remain applicable throughout migration.
32. Legacy decommission is part of modernization scope.
33. Legacy shutdown requires validation that hidden consumers no longer exist.
34. Obsolete credentials, infrastructure and network access must be removed.
35. Modernization success must be measured through business/operational outcomes rather than rewritten LOC.

---

# 260. Validation

This ADR will be validated through:

- architecture reviews
- dependency inventories
- characterization tests
- unit/integration/contract tests
- SAST
- SonarQube
- migration dashboards
- CDC monitoring
- reconciliation reports
- shadow comparisons
- performance testing
- authorization testing
- cutover rehearsals
- rollback testing
- DR exercises
- legacy-traffic monitoring
- FinOps reports
- decommission reviews

---

# 261. Success Criteria

The decision is successful when:

- modernization proceeds without uncontrolled business interruption
- legacy dependencies progressively decrease
- modern domain boundaries become clearer
- direct legacy database dependencies decrease
- data migrations reconcile successfully
- migration traffic can be controlled and rolled back
- security posture improves
- delivery lead time improves
- deployment risk decreases
- legacy infrastructure is actually retired
- transitional mechanisms do not become permanent architecture
- business capabilities continue behaving correctly throughout migration

---

# 262. Alternatives Rejected

## 262.1 Big-Bang Rewrite as Default

Rejected because large enterprise systems contain hidden behavior and dependencies that make complete replacement disproportionately risky.

---

## 262.2 Keep Legacy Forever

Rejected because unsupported technology and accumulated coupling eventually create unacceptable operational and security risk.

---

## 262.3 Translate Legacy Code Line by Line

Rejected because language translation alone preserves legacy architecture.

---

## 262.4 Create Microservices Around Existing Shared Tables

Rejected because this preserves database coupling while adding distributed-system complexity.

---

## 262.5 Synchronous Dual Write Everywhere

Rejected because partial failure creates data divergence.

---

## 262.6 Migrate All Historical Data

Rejected because historical data should follow actual business, retention and access requirements.

---

## 262.7 Keep Legacy Running Permanently as Rollback

Rejected because indefinite duplicate architecture increases cost, complexity and security exposure.

---

# 263. Related Decisions

This ADR is related to:

- ADR-002: Adopt Domain-Driven Design
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-009: Use Apache Kafka for Integration Events
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards
- ADR-044: Adopt FinOps, Capacity Efficiency and Cloud Cost Governance Standards
- ADR-045: Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards
- ADR-046: Adopt Data Governance, Privacy, Retention and Lifecycle Standards
- ADR-048: Adopt Engineering Productivity, Developer Experience and InnerSource Standards

---

# 264. References

- Martin Fowler — Strangler Fig Application
- Domain-Driven Design
- Anti-Corruption Layer Pattern
- AWS Prescriptive Guidance — Legacy Modernization
- Microsoft Cloud Adoption Framework
- Google Cloud Migration Center
- Transactional Outbox Pattern
- Change Data Capture
- DORA
- Team Topologies
- Oracle Database Documentation
- Oracle Forms Documentation
- Java 21 Documentation
- Spring Boot Documentation

---

# 265. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial legacy modernization and incremental migration baseline |

---

# 266. Decision Summary

The definitive modernization strategy is:

```text
                 LEGACY LANDSCAPE
                        |
                        v
               IDENTIFY CAPABILITY
                        |
                        v
                DEFINE BOUNDARY
                        |
                        v
             CHARACTERIZE BEHAVIOR
                        |
                        v
                 BUILD MODERN
                        |
                        v
                   COMPARE
                        |
                        v
               MIGRATE TRAFFIC
                        |
                        v
                  VALIDATE
                        |
                        v
               RETIRE LEGACY
```

The preferred architecture is:

```text
                      CLIENT
                        |
                        v
                 ROUTING LAYER
                  /           \
                 /             \
                v               v
             LEGACY           MODERN
                |               |
                |               v
                |          MODERN DOMAIN
                |               |
                +---- ACL ------+
```

For data ownership:

```text
BEFORE

Legacy App A ----\
Legacy App B -----+--> SHARED DATABASE
Legacy App C ----/


TARGET

Service A ------> Data A

Service B ------> Data B

Service C ------> Data C
```

The transition does not need to occur in one step.

For legacy business rules:

```text
LEGACY CODE
     |
     v
CHARACTERIZATION
     |
     v
AUTOMATED TEST
     |
     v
MODERN IMPLEMENTATION
     |
     v
BEHAVIOR COMPARISON
```

For migration:

```text
             LEGACY DATABASE
                    |
        +-----------+-----------+
        |                       |
        v                       v
 INITIAL SNAPSHOT              CDC
        |                       |
        +-----------+-----------+
                    |
                    v
              MODERN DATABASE
                    |
                    v
               RECONCILIATION
```

For writes, prefer:

```text
APPLICATION
     |
     v
AUTHORITATIVE DATABASE
     |
     v
TRANSACTIONAL OUTBOX
     |
     v
EVENT
     |
     v
SECONDARY MODEL
```

instead of:

```text
APPLICATION
    |
    +------> DATABASE A
    |
    +------> DATABASE B

with no atomicity.
```

For progressive migration:

```text
LEGACY 100%
     |
     v
MODERN 1%
     |
     v
MODERN 5%
     |
     v
MODERN 25%
     |
     v
MODERN 50%
     |
     v
MODERN 100%
     |
     v
OBSERVATION
     |
     v
LEGACY DECOMMISSION
```

For stateful workflows:

```text
BUSINESS KEY
     |
     v
CONSISTENT ROUTING
   /           \
  v             v
LEGACY         MODERN
```

For cutover:

```text
              PRECONDITIONS
                    |
                    v
               FINAL SYNC
                    |
                    v
              RECONCILIATION
                    |
                    v
                 GO/NO-GO
                    |
             +------+------+
             |             |
            GO           NO-GO
             |             |
             v             v
         CUTOVER          STOP
             |
             v
         HYPERCARE
             |
       +-----+-----+
       |           |
       v           v
     HEALTHY     FAILURE
       |           |
       v           v
    CONTINUE    ROLLBACK
```

Modernization does not end at:

```text
NEW SYSTEM RUNNING
```

It ends at:

```text
NEW SYSTEM RUNNING
        +
TRAFFIC MIGRATED
        +
DATA RECONCILED
        +
CONSUMERS MIGRATED
        +
LEGACY DISABLED
        +
LEGACY INFRASTRUCTURE REMOVED
        +
LEGACY CREDENTIALS REVOKED
        +
DOCUMENTATION UPDATED
```

The complete modernization equation is:

```text
DOMAIN UNDERSTANDING
        +
CHARACTERIZATION
        +
INCREMENTAL EXTRACTION
        +
ANTI-CORRUPTION LAYERS
        +
CONTROLLED DATA MIGRATION
        +
OBSERVABILITY
        +
PROGRESSIVE CUTOVER
        +
ROLLBACK
        +
LEGACY DECOMMISSION
        =
SUSTAINABLE MODERNIZATION
```

The governing principle is:

```text
The objective of modernization
is not to replace old code
with new code.

The objective is to reduce
business and technical risk
while progressively moving
business capabilities toward
a maintainable, secure,
observable and evolvable
architecture.

A legacy system is not truly
modernized while the organization
still depends on the legacy system.
```
