# ADR-050: Adopt Enterprise Architecture Baseline and Architecture Governance Operating Model

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-050 |
| Title | Adopt Enterprise Architecture Baseline and Architecture Governance Operating Model |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Enterprise Architecture, Governance, Compliance, Production Readiness |
| Related Work Items | Architecture Baseline, Fitness Functions, Service Scorecard, Production Readiness |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The platform architecture is governed by multiple Architecture Decision Records covering:

```text
Application Architecture

Domain Design

Java

Spring Boot

Databases

Flyway

Messaging

APIs

Security

Observability

Resilience

Concurrency

Testing

CI/CD

Operations

Architecture Governance

FinOps

Disaster Recovery

Data Governance

Legacy Modernization

Developer Experience

AI-Assisted Engineering
```

Individual ADRs provide detailed decisions.

However, an enterprise architecture also requires a mechanism for answering:

```text
Which rules are mandatory?

Which rules are recommendations?

Which rules apply to this service?

How is compliance measured?

How are exceptions approved?

When is a service production ready?

How is architectural drift detected?

How are ADRs superseded?

Who owns architectural compliance?
```

Without a consolidated operating model, ADRs risk becoming documentation that is technically correct but inconsistently enforced.

---

# 2. Problem Statement

The organization requires a unified architecture governance model defining:

- enterprise architecture baseline
- normative terminology
- MUST / SHOULD / MAY
- applicability
- architecture profiles
- automated compliance
- architecture fitness functions
- service scorecards
- architecture reviews
- exceptions
- waivers
- technical debt
- ADR lifecycle
- production readiness
- enterprise readiness
- architecture drift
- continuous governance
- ownership
- evidence
- auditability

---

# 3. Decision Drivers

Primary drivers are:

1. architectural consistency
2. objective governance
3. automation
4. security
5. reliability
6. maintainability
7. developer autonomy
8. reduced manual review
9. traceability
10. measurable compliance
11. controlled exceptions
12. continuous improvement

---

# 4. Decision

The organization adopts a consolidated Enterprise Architecture Baseline governed through:

```text
ADRs
  +
Architecture Profiles
  +
Automated Fitness Functions
  +
Service Scorecards
  +
Architecture Reviews
  +
Exception Governance
  +
Production Readiness Gates
```

The operating model is:

```text
                  ENTERPRISE ADRs
                        |
                        v
              ARCHITECTURE BASELINE
                        |
             +----------+----------+
             |                     |
             v                     v
       AUTOMATED RULES        HUMAN REVIEW
             |                     |
             +----------+----------+
                        |
                        v
                 SERVICE SCORECARD
                        |
                        v
                READINESS DECISION
```

---

# 5. Fundamental Principle

The governing principle is:

```text
Architecture governance should
automate objective rules and
reserve human judgment for
decisions that actually require
architectural judgment.
```

---

# 6. Normative Terminology

Architecture standards use three primary requirement levels:

```text
MUST

SHOULD

MAY
```

---

# 7. MUST

`MUST` defines a mandatory architectural requirement.

Equivalent terms include:

```text
REQUIRED

SHALL
```

---

# 8. MUST Violation

A service violating an applicable MUST requirement is non-compliant unless an approved exception exists.

---

# 9. SHOULD

`SHOULD` defines the preferred architectural approach.

Deviation is permitted when a documented technical or business reason exists.

---

# 10. MAY

`MAY` defines an optional capability or technique.

---

# 11. Requirement Interpretation

The terminology should be interpreted consistently across architecture documentation.

---

# 12. Applicability

Not every ADR applies to every workload.

---

# 13. Example

A service without SQS does not need to comply with SQS-specific producer requirements.

However:

```text
IF SQS is used
THEN applicable SQS standards apply.
```

---

# 14. Architecture Profile

Each service should have an identifiable architecture profile.

Example:

```text
Service Type: REST Microservice

Runtime: Java 21

Framework: Spring Boot

Database: PostgreSQL

Migration: Flyway

Messaging: SQS

Cache: Redis

Deployment: Container

Criticality: High
```

---

# 15. Profile Purpose

The architecture profile determines which controls apply.

---

# 16. Conditional Standards

Controls should support conditions such as:

```text
DATABASE_USED

SQS_USED

REDIS_USED

EXTERNAL_API_USED

PERSONAL_DATA_PROCESSED

CRITICAL_SERVICE

BATCH_WORKLOAD
```

---

# 17. Baseline Categories

The enterprise baseline is organized into:

```text
Architecture

Runtime

Code Quality

Testing

Data

Integration

Security

Observability

Resilience

Delivery

Operations

Governance
```

---

# 18. Architecture Baseline

Every production service MUST have:

```text
Clear Business Purpose

Explicit Owner

Defined Domain Boundary

Documented Dependencies

Repository Ownership

Applicable ADR Compliance
```

---

# 19. Runtime Baseline

Modern Java services MUST use the approved Java runtime baseline.

Current baseline:

```text
Java 21
```

unless an approved exception or newer baseline applies.

---

# 20. Framework Baseline

Spring-based services MUST use supported Spring Boot versions approved by the platform.

---

# 21. Unsupported Runtime

Unsupported runtimes require explicit remediation or an approved time-bound exception.

---

# 22. Build Baseline

Gradle projects MUST use the Gradle Wrapper.

---

# 23. Reproducible Build

The service MUST be buildable through repository-controlled configuration.

---

# 24. Dependency Governance

Dependencies MUST follow ADR-038.

---

# 25. Vulnerabilities

Critical dependency vulnerabilities MUST be addressed according to enterprise security policy or have an approved risk exception.

---

# 26. Code Quality Baseline

Production services MUST participate in the approved static-analysis process.

---

# 27. SonarQube

Applicable services MUST pass the defined SonarQube Quality Gate.

---

# 28. SAST

Applicable services MUST pass required SAST controls.

---

# 29. Suppression

Security or quality findings MUST NOT be suppressed merely to obtain a successful pipeline.

---

# 30. Test Baseline

Business-critical logic MUST have automated tests appropriate to its risk.

---

# 31. Coverage

Coverage thresholds MUST follow project/platform policy.

Coverage alone MUST NOT be considered proof of correctness.

---

# 32. Test Quality

Tests MUST verify meaningful behavior.

---

# 33. Integration Testing

Technology-specific integration behavior SHOULD be tested against representative technology.

---

# 34. PostgreSQL

PostgreSQL-specific behavior SHOULD use PostgreSQL-compatible integration infrastructure rather than relying solely on H2.

---

# 35. Database Baseline

A service owning persistent data MUST have explicit data ownership.

---

# 36. Database Ownership

Other services MUST NOT directly modify another service's owned database tables.

---

# 37. Flyway

Schema evolution using Flyway MUST follow immutable migration history.

---

# 38. Migration Immutability

```text
APPLIED MIGRATION
       =
    IMMUTABLE
```

Corrections MUST use a new migration version.

---

# 39. Database Performance

Queries on critical paths SHOULD have appropriate indexing and execution-plan validation.

---

# 40. Transaction Boundaries

Transactions MUST have deliberate and bounded scope.

---

# 41. API Baseline

REST APIs MUST follow the API standards defined by the applicable ADRs.

---

# 42. Contract

Public/internal shared API contracts MUST be explicitly defined.

---

# 43. Compatibility

Breaking API changes MUST follow controlled compatibility/versioning procedures.

---

# 44. OpenAPI

REST services SHOULD maintain accurate OpenAPI contracts where applicable.

---

# 45. Messaging Baseline

SQS integrations MUST follow event governance standards.

---

# 46. Event Ownership

Events MUST have identifiable ownership.

---

# 47. Schema Evolution

Event schema evolution MUST preserve required compatibility.

---

# 48. Consumer Idempotency

Consumers processing potentially repeated messages MUST be idempotent where business semantics require it.

---

# 49. Security Baseline

Security is mandatory rather than optional architecture.

---

# 50. Authentication

Protected services MUST use approved authentication mechanisms.

---

# 51. Authorization

Authorization MUST enforce least privilege.

---

# 52. Secrets

Secrets MUST NOT be stored in source code.

---

# 53. Sensitive Logging

Credentials, tokens and sensitive personal information MUST NOT be unnecessarily logged.

---

# 54. Input Validation

Untrusted input MUST be validated at appropriate trust boundaries.

---

# 55. Output Handling

Output encoding/sanitization MUST occur at the correct contextual boundary.

---

# 56. Business Data

Valid domain data MUST NOT be corrupted merely to satisfy generic sanitization.

---

# 57. Observability Baseline

Production services MUST provide sufficient observability for their criticality.

---

# 58. Logging

Logs SHOULD be structured and diagnostically useful.

---

# 59. Correlation

Distributed interactions SHOULD preserve correlation identifiers where applicable.

---

# 60. Metrics

Critical services MUST expose operational metrics appropriate to their workload.

---

# 61. Health

Services MUST expose meaningful health/readiness information compatible with their runtime environment.

---

# 62. Alerting

Critical production failures MUST have actionable alerting.

---

# 63. Resilience Baseline

Remote dependencies MUST have deliberate failure-handling behavior.

---

# 64. Timeout

External calls MUST have bounded timeouts.

---

# 65. Retry

Retries MUST only be used where the operation and failure mode make retries safe.

---

# 66. Circuit Breaker

Circuit breakers SHOULD protect appropriate remote dependencies according to ADR-016.

---

# 67. Retry Storm

Retry configuration MUST avoid uncontrolled retry amplification.

---

# 68. Concurrency Baseline

Concurrency MUST be introduced deliberately.

---

# 69. Java 21

Virtual Threads MAY be used for appropriate I/O-bound workloads according to ADR-034.

---

# 70. Context Propagation

Security/request context MUST be preserved where asynchronous processing requires it.

---

# 71. Bounded Parallelism

Parallelism against bounded downstream resources MUST respect their capacity.

---

# 72. Delivery Baseline

Production changes MUST flow through the approved CI/CD process.

---

# 73. Branch Protection

Production repositories MUST preserve applicable branch protections.

---

# 74. CI

CI MUST execute required:

```text
Build

Tests

Quality Analysis

Security Analysis
```

before production release.

---

# 75. Deployment

Production deployment MUST use an approved deployment mechanism.

---

# 76. Manual Production Modification

Untracked manual production modification SHOULD be avoided.

Emergency changes MUST be reconciled back into source-controlled configuration.

---

# 77. Rollback

Material releases MUST have an understood recovery or rollback strategy.

---

# 78. Operational Baseline

Production services MUST have an operational owner.

---

# 79. Runbook

Critical services SHOULD have operational runbooks.

---

# 80. SLO

Critical services SHOULD have explicit SLOs.

---

# 81. Incident Management

Production incidents MUST follow the applicable incident-management process.

---

# 82. DR

Critical services MUST have recovery requirements appropriate to business criticality.

---

# 83. RTO/RPO

Applicable services MUST have defined:

```text
RTO

RPO
```

---

# 84. Recovery Validation

Critical recovery procedures SHOULD be tested rather than merely documented.

---

# 85. Data Governance Baseline

Data MUST follow classification, privacy, retention and lifecycle standards.

---

# 86. Personal Data

Personal data processing MUST follow applicable privacy requirements.

---

# 87. Retention

Data MUST NOT be retained indefinitely without a legitimate requirement.

---

# 88. Deletion

Required deletion must propagate to relevant controlled data copies according to policy.

---

# 89. FinOps Baseline

Cloud resources MUST have identifiable ownership and cost attribution where applicable.

---

# 90. Resource Tags

Applicable resources SHOULD identify:

```text
Service

Team

Environment

Domain

Cost Center
```

---

# 91. Legacy Baseline

Legacy systems remain governed systems.

---

# 92. Legacy Exception

Legacy technology does not create an automatic exemption from:

- security
- ownership
- operational
- data-governance

requirements.

---

# 93. Modernization

Unsupported legacy components SHOULD have explicit modernization, containment or retirement plans.

---

# 94. Developer Experience Baseline

Standard engineering workflows SHOULD use approved Golden Paths.

---

# 95. Platform Capability

Teams SHOULD reuse approved platform capabilities when they satisfy the requirement.

---

# 96. Custom Implementation

A custom implementation duplicating a platform capability SHOULD have technical justification.

---

# 97. AI Baseline

AI-assisted engineering MUST follow ADR-049.

---

# 98. AI Accountability

AI-generated code MUST remain owned and reviewed through the normal SDLC.

---

# 99. Architecture Fitness Functions

Objective architectural rules SHOULD be automated wherever practical.

---

# 100. Fitness Function Definition

A fitness function is an automated or repeatable mechanism verifying an architectural characteristic.

---

# 101. Example Fitness Functions

Examples include:

```text
Java Version

Forbidden Dependencies

Package Dependency Rules

Test Coverage

Sonar Quality Gate

SAST Gate

Flyway Naming

Docker Base Image

Dependency Versions

API Contract Validation

Architecture Tests
```

---

# 102. Architecture Tests

Tools such as ArchUnit MAY verify structural architecture.

---

# 103. Example

An architecture test may ensure:

```text
controller
    |
    v
service
    |
    v
repository
```

and prohibit unintended:

```text
controller
    |
    v
repository
```

where the project's architecture requires service-layer mediation.

---

# 104. Automated Before Manual

If a rule can be reliably automated, automation SHOULD be preferred over repeated manual review.

---

# 105. Shift Left

Architecture validation SHOULD occur as early as practical.

---

# 106. Local Validation

Developers SHOULD be able to execute important architecture checks locally.

---

# 107. CI Validation

Mandatory automated architecture controls MUST run in CI.

---

# 108. Continuous Architecture

Architecture compliance is evaluated continuously rather than only during initial service creation.

---

# 109. Architecture Drift

Architecture drift occurs when implementation progressively diverges from approved standards.

---

# 110. Drift Examples

```text
Unsupported Java Version

New Direct DB Coupling

Unapproved Dependency

Missing Tests

Disabled Security Control

Expired Exception

Deprecated Platform Component
```

---

# 111. Drift Detection

Drift SHOULD be detected through automated inventory and scorecards.

---

# 112. Service Scorecard

Each production service SHOULD have an architecture scorecard.

---

# 113. Scorecard Purpose

The scorecard provides a concise view of:

```text
Compliance

Risk

Technical Debt

Readiness

Ownership
```

---

# 114. Scorecard Categories

Recommended categories:

```text
Architecture

Runtime

Quality

Testing

Security

Data

Integration

Observability

Resilience

Delivery

Operations

Governance
```

---

# 115. Scorecard Status

Each control should preferably use discrete states:

```text
PASS

WARNING

FAIL

NOT_APPLICABLE

EXCEPTION
```

---

# 116. Avoid False Precision

A single numeric score MUST NOT hide critical failures.

---

# 117. Example

This is unsafe:

```text
Architecture Score: 92%
```

if the missing 8% includes:

```text
No Authentication
```

---

# 118. Critical Gate

Critical requirements operate as gates independently of aggregate scores.

---

# 119. Optional Numeric Score

A numeric score MAY be used for trend analysis if critical gate status remains separately visible.

---

# 120. Weighting

If numeric scoring is used, control weights MUST reflect risk rather than convenience.

---

# 121. Scorecard Evidence

A scorecard result SHOULD link to evidence.

Examples:

```text
CI Result

Sonar Result

SAST Result

Dependency Scan

Repository Configuration

Dashboard

Runbook

ADR Exception
```

---

# 122. Evidence-Based Governance

Architecture compliance decisions SHOULD be based on evidence rather than assertion.

---

# 123. Architecture Review

Human Architecture Review remains necessary for decisions not adequately represented through automated rules.

---

# 124. Review Triggers

Architecture Review SHOULD occur for material changes such as:

```text
New Business Capability

New Service

New Database Technology

New Messaging Technology

New External Integration Pattern

Major Data Ownership Change

Cross-Domain Dependency

Critical Security Architecture

Major Migration

Material ADR Exception
```

---

# 125. Routine Changes

Routine implementation following an established Golden Path SHOULD NOT require a full architecture committee review.

---

# 126. Governance Bottleneck

Architecture governance MUST NOT become a mandatory manual approval queue for ordinary compliant engineering work.

---

# 127. Architecture Review Objective

Review should answer:

```text
Is the boundary correct?

Is ownership clear?

Is the technology justified?

Are failure modes understood?

Is data ownership correct?

Are security implications addressed?

Does this conflict with existing ADRs?
```

---

# 128. Review Evidence

Significant architecture reviews SHOULD produce durable decisions.

---

# 129. ADR

An ADR SHOULD be created when a decision:

- materially affects architecture
- has significant trade-offs
- affects multiple teams
- establishes a reusable standard
- would be expensive to reverse

---

# 130. ADR Lifecycle

ADR states are:

```text
Proposed

Accepted

Deprecated

Superseded

Rejected
```

---

# 131. Proposed

The decision is under evaluation.

---

# 132. Accepted

The decision is approved and active.

---

# 133. Deprecated

The decision remains historically relevant but should no longer be used for new implementations.

---

# 134. Superseded

A newer ADR replaces the decision.

---

# 135. Rejected

The proposal was evaluated and not adopted.

---

# 136. ADR Immutability

Accepted ADR history SHOULD generally remain preserved.

---

# 137. Decision Evolution

When architecture changes materially, prefer:

```text
NEW ADR
   |
   v
SUPERSEDES OLD ADR
```

rather than rewriting history.

---

# 138. Minor Corrections

Non-semantic corrections such as typographical fixes MAY be applied without creating a new ADR.

---

# 139. ADR Index

The architecture repository SHOULD maintain an ADR index.

---

# 140. ADR Index Fields

Recommended:

```text
Number

Title

Status

Date

Area

Superseded By
```

---

# 141. Architecture Exception

A service unable to comply with a MUST requirement requires an approved architecture exception.

---

# 142. Exception Is Not Compliance

An exception means:

```text
KNOWN NON-COMPLIANCE
        +
ACCEPTED TEMPORARILY
```

not:

```text
COMPLIANT
```

---

# 143. Exception Content

An exception MUST identify:

```text
Control

Service

Reason

Risk

Compensating Controls

Owner

Approval

Expiration Date
```

---

# 144. Time-Bound Exceptions

Exceptions SHOULD normally expire.

---

# 145. Permanent Exceptions

Permanent exceptions require exceptional justification and periodic review.

---

# 146. Expired Exception

An expired exception becomes unresolved non-compliance.

---

# 147. Exception Renewal

Renewal requires explicit reassessment.

---

# 148. Compensating Control

Where possible, an exception SHOULD define compensating controls.

---

# 149. Example

If immediate runtime modernization is impossible:

```text
Unsupported Runtime
       |
       v
Temporary Exception
       |
       +--> Network Isolation
       +--> Enhanced Monitoring
       +--> Restricted Access
       +--> Upgrade Deadline
```

---

# 150. Technical Debt

Accepted architectural deviations SHOULD enter the technical-debt management process when remediation is required.

---

# 151. Debt Ownership

Technical debt without an owner is unlikely to be resolved.

---

# 152. Debt Priority

Priority should consider:

```text
Risk

Business Impact

Change Frequency

Security

Operational Cost

Remediation Cost
```

---

# 153. Production Ready

`Production Ready` means the service satisfies the minimum controls required for safe production operation.

---

# 154. Production Ready Is a Gate

A critical MUST failure blocks Production Ready status unless an explicit approved exception permits release.

---

# 155. Production Readiness Categories

The minimum review includes:

```text
Ownership

Build

Testing

Security

Configuration

Data

Observability

Resilience

Deployment

Operations

Recovery
```

---

# 156. Production Readiness Checklist

A service is not Production Ready until applicable items are satisfied:

```text
[ ] Business owner identified

[ ] Technical owner identified

[ ] Repository owner identified

[ ] Architecture profile documented

[ ] Applicable ADRs identified

[ ] Java/runtime supported

[ ] Build reproducible

[ ] Dependency governance active

[ ] Unit tests passing

[ ] Integration tests passing where required

[ ] Coverage policy satisfied

[ ] Sonar Quality Gate passing

[ ] SAST passing

[ ] Dependency scan passing

[ ] Secrets externalized

[ ] Authentication configured where required

[ ] Authorization validated

[ ] Sensitive logging reviewed

[ ] Database ownership defined

[ ] Flyway validated where applicable

[ ] API contracts documented where applicable

[ ] Messaging contracts validated where applicable

[ ] Timeouts configured

[ ] Retry behavior validated

[ ] Circuit breaker configured where applicable

[ ] Health/readiness configured

[ ] Logging configured

[ ] Metrics configured

[ ] Alerting configured for critical failures

[ ] CI/CD configured

[ ] Deployment strategy defined

[ ] Rollback/recovery strategy understood

[ ] Runbook available where required

[ ] SLO defined for critical services

[ ] RTO/RPO defined where required

[ ] Data classification understood

[ ] Retention requirements understood

[ ] Cost ownership established

[ ] Architecture exceptions documented

[ ] No expired exceptions
```

---

# 157. Enterprise Ready

`Enterprise Ready` represents a maturity level above minimum production safety.

---

# 158. Enterprise Ready Characteristics

An Enterprise Ready service should demonstrate:

```text
Strong Architecture Compliance

Automated Governance

Operational Maturity

Measured Reliability

Documented Ownership

Controlled Cost

Tested Recovery

Low Architectural Drift

Maintainable Technical Debt

Developer Self-Service
```

---

# 159. Enterprise Ready Checklist

Applicable:

```text
[ ] Production Ready

[ ] Architecture fitness functions automated

[ ] Architecture scorecard available

[ ] No unresolved critical architecture debt

[ ] SLO measured

[ ] Alert quality validated

[ ] DR/recovery tested

[ ] Capacity validated

[ ] Performance baseline available

[ ] Cost dashboard available

[ ] Service catalog complete

[ ] Runbook tested

[ ] Dependency ownership documented

[ ] API/event compatibility governed

[ ] Data lifecycle automated where appropriate

[ ] Security controls continuously validated

[ ] Platform Golden Path adopted or deviation justified

[ ] Technical debt reviewed periodically

[ ] Decommission strategy understood
```

---

# 160. Maturity Levels

Services MAY be classified using maturity levels.

Example:

```text
LEVEL 0 — Experimental

LEVEL 1 — Development Ready

LEVEL 2 — Production Ready

LEVEL 3 — Enterprise Ready

LEVEL 4 — Optimized
```

---

# 161. Level 0 — Experimental

Suitable for:

- prototypes
- spikes
- local experimentation

Not approved for normal production workloads.

---

# 162. Level 1 — Development Ready

The service has:

- ownership
- repository
- build
- tests
- development environment

but may not yet satisfy production controls.

---

# 163. Level 2 — Production Ready

The service satisfies mandatory production gates.

---

# 164. Level 3 — Enterprise Ready

The service satisfies stronger operational and governance maturity.

---

# 165. Level 4 — Optimized

The service demonstrates continuous optimization through:

- measured performance
- automated architecture governance
- cost optimization
- tested resilience
- high-quality developer experience

---

# 166. Maturity Is Not Prestige

Maturity levels describe operational characteristics, not team status.

---

# 167. Criticality

Readiness requirements SHOULD account for service criticality.

---

# 168. Criticality Levels

Example:

```text
LOW

MEDIUM

HIGH

MISSION_CRITICAL
```

---

# 169. Criticality Effect

Higher criticality may require stronger:

- availability
- recovery
- monitoring
- security
- performance
- change-management

controls.

---

# 170. One Size Does Not Fit All

A low-risk internal utility does not require identical controls to a mission-critical order-processing service.

---

# 171. Baseline Minimum

Criticality MAY increase requirements but MUST NOT remove universal baseline security and ownership requirements.

---

# 172. Architecture Compliance Pipeline

The target pipeline is:

```text
SOURCE
  |
  v
BUILD
  |
  v
TEST
  |
  v
ARCHITECTURE TESTS
  |
  v
SONAR
  |
  v
SAST
  |
  v
DEPENDENCY SCAN
  |
  v
CONTRACT VALIDATION
  |
  v
SCORECARD UPDATE
  |
  v
DEPLOYMENT ELIGIBILITY
```

---

# 173. Policy as Code

Architecture rules SHOULD increasingly become Policy as Code.

---

# 174. Policy Examples

Examples:

```text
Required Java Version

Approved Container Images

Required Resource Tags

Forbidden Public Exposure

Required Encryption

Required Repository Metadata
```

---

# 175. Policy Location

Policies SHOULD be centrally governed but executable close to the development workflow.

---

# 176. Policy Versioning

Policies MUST be version controlled.

---

# 177. Policy Changes

Material policy changes require communication and migration guidance.

---

# 178. Policy Rollout

New controls SHOULD support staged rollout when immediate enforcement would create unreasonable disruption.

Example:

```text
OBSERVE
   |
   v
WARN
   |
   v
ENFORCE
```

---

# 179. Existing Services

New standards SHOULD define how existing services reach compliance.

---

# 180. No Instant Compliance Fiction

Publishing a new ADR does not magically make every existing service compliant.

---

# 181. Adoption Plan

Material new standards should define:

```text
New Services

Existing Services

Deadline

Exceptions

Migration Support
```

---

# 182. New Service Standard

New services SHOULD normally comply immediately with the current baseline.

---

# 183. Existing Service Standard

Existing services should have risk-based remediation plans.

---

# 184. Architecture Inventory

The organization SHOULD maintain an inventory of production services.

---

# 185. Inventory Fields

Recommended:

```text
Service

Domain

Owner

Criticality

Runtime

Framework

Database

Messaging

Repository

Deployment

Lifecycle

Compliance Status
```

---

# 186. Inventory Automation

Inventory SHOULD be populated automatically where practical.

---

# 187. Service Catalog Integration

Architecture scorecards SHOULD integrate with the service catalog.

---

# 188. Dashboard

Architecture governance SHOULD provide portfolio-level visibility.

---

# 189. Portfolio Dashboard

Useful indicators:

```text
Services Total

Production Ready

Enterprise Ready

Unsupported Runtime

Critical Vulnerabilities

Expired Exceptions

Missing Owners

Missing Runbooks

Missing SLOs

Architecture Drift

Technical Debt
```

---

# 190. Trend

Governance metrics SHOULD show trends rather than only current snapshots.

---

# 191. Architecture Health

Portfolio architecture health should improve over time.

---

# 192. Governance Metrics

Useful metrics include:

```text
% Services on Supported Java

% Passing Sonar

% Passing SAST

% With Owners

% With SLO

% With Tested Recovery

Expired Exceptions

Critical Architecture Debt

Average Exception Age
```

---

# 193. Metric Gaming

Metrics MUST NOT become substitutes for engineering judgment.

---

# 194. Compliance Theater

A service should not be considered healthy merely because checkboxes are green while real operational risks remain.

---

# 195. Evidence Quality

Evidence itself must be trustworthy.

---

# 196. Manual Attestation

Manual attestation SHOULD be minimized for objectively detectable controls.

---

# 197. Architecture Ownership

Architecture governance is a shared responsibility.

---

# 198. Enterprise Architecture

Enterprise/Platform Architecture owns:

- baseline standards
- cross-cutting ADRs
- exception framework
- architecture governance model

---

# 199. Platform Team

Platform Engineering owns:

- Golden Paths
- automated controls
- common tooling
- developer enablement

---

# 200. Security

Security owns applicable security policy and risk governance.

---

# 201. Domain Team

Domain/service teams own:

- implementation
- service architecture
- compliance
- operational outcomes
- remediation

---

# 202. Product/Business

Business owners provide:

- criticality
- availability expectations
- business impact
- recovery priorities

---

# 203. No Architecture Police Model

Architecture teams SHOULD NOT own every implementation decision.

---

# 204. Guardrails Over Gates

Prefer:

```text
AUTOMATED GUARDRAILS
```

over:

```text
MANUAL APPROVAL FOR EVERYTHING
```

---

# 205. Architecture Review Board

An Architecture Review Board MAY handle high-impact cross-domain decisions and significant exceptions.

---

# 206. ARB Scope

The ARB should focus on:

- strategic decisions
- cross-domain impacts
- high-risk exceptions
- new platform technologies
- material standard changes

---

# 207. ARB Anti-Pattern

The ARB MUST NOT become the approval authority for routine implementation details.

---

# 208. Exception Escalation

Higher-risk exceptions require higher-level approval.

---

# 209. Risk-Based Governance

Governance effort should be proportional to:

```text
IMPACT
   x
PROBABILITY
   x
BLAST RADIUS
```

---

# 210. Architecture Change Classification

Changes MAY be classified:

```text
STANDARD

SIGNIFICANT

STRATEGIC
```

---

# 211. Standard Change

Uses approved patterns and technologies.

Normally handled through normal team review and automated controls.

---

# 212. Significant Change

Introduces material architecture impact.

Requires architecture review.

---

# 213. Strategic Change

Changes enterprise/platform direction.

Requires formal ADR and broader governance.

---

# 214. Production Readiness Review

Production Readiness Review SHOULD focus on evidence and unresolved risk.

---

# 215. Review Timing

Readiness review MUST NOT first occur immediately before production deployment.

---

# 216. Progressive Readiness

Readiness should be built throughout development.

---

# 217. Architecture at Inception

Major architecture decisions SHOULD occur early enough to influence implementation.

---

# 218. Continuous Validation

Architecture remains subject to validation after production deployment.

---

# 219. Production Does Not Freeze Architecture

Services continue evolving and can become non-compliant through drift.

---

# 220. Periodic Review

Critical services SHOULD receive periodic architecture/operational maturity reviews.

---

# 221. Triggered Review

Review may also be triggered by:

```text
Major Incident

Security Finding

Major Scale Increase

Ownership Change

Technology EOL

Repeated SLO Failure

Major Migration
```

---

# 222. Architecture Debt from Incidents

Incidents exposing architectural weaknesses SHOULD create remediation actions where appropriate.

---

# 223. Architecture Retirement

Services no longer needed SHOULD follow controlled decommission procedures.

---

# 224. Retired Service

A retired service MUST NOT remain indefinitely as unmanaged infrastructure.

---

# 225. Decommission Evidence

Retirement SHOULD verify:

- traffic removed
- consumers migrated
- credentials revoked
- infrastructure removed
- data handled correctly
- documentation updated

---

# 226. Baseline Evolution

This architecture baseline is expected to evolve.

---

# 227. New Technology

New technologies MAY be introduced through controlled evaluation.

---

# 228. Technology Lifecycle

Technology may move through:

```text
ASSESS

TRIAL

ADOPT

HOLD

RETIRE
```

---

# 229. Technology Radar

A Technology Radar MAY complement ADR governance.

---

# 230. Adopt

Approved for normal use.

---

# 231. Trial

Approved for controlled use with defined scope.

---

# 232. Assess

Under evaluation.

---

# 233. Hold

Not recommended for new implementation.

---

# 234. Retire

Must be removed according to migration planning.

---

# 235. Baseline Version

The Enterprise Architecture Baseline SHOULD itself be versioned.

---

# 236. Version Example

```text
Enterprise Architecture Baseline 1.0
```

---

# 237. Baseline Change

A material baseline update should produce:

```text
Version

Change Summary

Affected Services

Migration Requirement

Effective Date
```

---

# 238. Governance Repository

Architecture governance artifacts SHOULD reside in version-controlled repositories.

---

# 239. Architecture as Code

The target state increasingly represents architecture through:

```text
ADRs

ArchUnit

CI Rules

Policy as Code

IaC

Contracts

Service Metadata

Automated Scorecards
```

---

# 240. Architecture Documentation

Diagrams remain useful but MUST NOT be the sole source of architectural truth.

---

# 241. Executable Architecture

Where practical:

```text
ARCHITECTURE INTENT
        |
        v
EXECUTABLE RULE
        |
        v
CONTINUOUS VALIDATION
```

is preferred.

---

# 242. Enterprise Architecture Governance Gate

A service cannot be classified as compliant when any applicable condition exists:

```text
[ ] Missing accountable owner

[ ] Unsupported critical runtime without exception

[ ] Critical unresolved security vulnerability

[ ] Required authentication absent

[ ] Required authorization absent

[ ] Secrets committed to source

[ ] Mandatory CI security gate bypassed

[ ] Applied Flyway migration modified

[ ] Critical data ownership undefined

[ ] Production deployment outside approved controls

[ ] Required recovery strategy absent

[ ] Expired critical architecture exception

[ ] Unknown critical production dependency
```

---

# 243. Production Ready Gate

A service cannot be classified as Production Ready while an applicable critical gate is failing unless an explicitly authorized risk exception exists.

---

# 244. Enterprise Ready Gate

Enterprise Ready additionally requires demonstrated operational maturity rather than documentation alone.

---

# 245. Architecture Exception Gate

An architecture exception is valid only when:

```text
[ ] Requirement identified

[ ] Reason documented

[ ] Risk documented

[ ] Owner identified

[ ] Compensating controls identified where applicable

[ ] Approval recorded

[ ] Expiration/review date defined

[ ] Remediation plan defined where applicable
```

---

# 246. Anti-Patterns

The following are prohibited or strongly discouraged:

- ADRs treated only as documentation with no enforcement strategy
- every architecture rule requiring manual approval
- every service forced through architecture committee review
- compliance represented only by a misleading percentage
- critical security failures hidden inside aggregate scores
- permanent undocumented architecture exceptions
- exceptions without owners
- exceptions without expiration/review dates
- expired exceptions silently considered valid
- rewriting accepted ADR history to hide previous decisions
- architecture diagrams as the only source of truth
- unsupported runtimes without visibility
- service ownership inferred informally
- production readiness evaluated only immediately before release
- manually attesting controls that can be reliably automated
- architecture governance used to centralize ordinary team decisions
- metrics used as substitutes for engineering judgment
- declaring existing services instantly compliant after publishing a new standard
- custom platform implementations without justification
- ignoring architecture drift after production release
- allowing retired services to remain as unmanaged infrastructure
- introducing enterprise technologies without lifecycle governance

---

# 247. Positive Consequences

This decision provides:

- unified architecture governance
- objective baseline requirements
- clearer MUST/SHOULD/MAY semantics
- automated compliance
- measurable architecture health
- explicit exception governance
- stronger production readiness
- reduced architecture drift
- better portfolio visibility
- stronger service ownership
- consistent ADR lifecycle
- risk-based human review
- increased team autonomy for standard changes

---

# 248. Negative Consequences

The decision introduces:

- scorecard implementation
- architecture inventory maintenance
- policy-as-code development
- fitness-function maintenance
- exception governance
- readiness evidence
- periodic reviews

These costs are accepted because architecture without continuous governance progressively diverges from its intended state.

---

# 249. Neutral Consequences

The decision also means:

- not every service has identical controls
- criticality affects required maturity
- some architecture decisions remain subjective
- not every rule can be automated
- legacy systems may require temporary exceptions
- baseline evolution creates remediation work
- compliance is continuous rather than a one-time certification

---

# 250. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Governance becomes bureaucracy | High | Medium | Automation + risk-based review |
| Score gaming | Medium | Medium | Critical gates + evidence |
| Too many exceptions | High | Medium | Expiration + dashboards |
| Legacy non-compliance persists | High | High | Remediation plans |
| Automated rule false positives | Medium | Medium | Rule validation |
| Architecture board bottleneck | High | Medium | Standard-change autonomy |
| Baseline becomes stale | High | Medium | Versioned evolution |
| Teams ignore ADRs | High | Medium | Fitness functions |
| Compliance hides real risk | High | Medium | Evidence-based reviews |
| Tooling fragmentation | Medium | Medium | Platform Golden Paths |
| Architecture inventory becomes stale | Medium | High | Automation |

---

# 251. Implementation Guidance

The following rules are mandatory:

1. Applicable architecture requirements must be classified as MUST, SHOULD or MAY.
2. Services must have identifiable architecture profiles.
3. Applicability must be determined from workload characteristics rather than blindly applying every control.
4. Applicable MUST violations require remediation or approved exceptions.
5. Objective architecture rules should be automated wherever practical.
6. Mandatory automated controls must execute through CI or equivalent governance.
7. Architecture scorecards should distinguish PASS, WARNING, FAIL, NOT_APPLICABLE and EXCEPTION.
8. Critical failures must remain visible independently of aggregate scores.
9. Architecture Reviews should focus on significant decisions rather than routine implementation.
10. Routine Golden Path changes should not require centralized manual approval.
11. Material architectural decisions should be captured through ADRs.
12. ADR lifecycle and supersession must preserve decision history.
13. Architecture exceptions must have owner, risk, approval and expiration/review date.
14. Expired exceptions represent unresolved non-compliance.
15. Production Ready status requires all applicable critical gates to pass or have explicit approved exceptions.
16. Enterprise Ready requires demonstrated operational maturity.
17. Service criticality should increase controls proportionally to risk.
18. Production services must have accountable ownership.
19. Architecture drift must be continuously monitored.
20. New baseline requirements must define adoption expectations for existing services.
21. Portfolio architecture health should be measurable.
22. Architecture governance metrics must not replace engineering judgment.
23. Architecture governance should prefer automated guardrails over manual gates.
24. New enterprise technologies require controlled lifecycle governance.
25. Architecture baseline changes must be versioned and communicated.
26. Architecture governance artifacts must remain version controlled.
27. Services remain subject to architecture validation after deployment.
28. Decommissioned services must be removed from infrastructure and governance inventories.
29. AI-assisted implementation remains subject to the same architecture baseline.
30. Architecture compliance is an engineering responsibility shared across architecture, platform, security and domain teams.

---

# 252. Validation

This ADR will be validated through:

- CI architecture checks
- ArchUnit
- SonarQube
- SAST
- dependency scanning
- policy-as-code
- service catalog
- architecture scorecards
- architecture reviews
- production readiness reviews
- exception dashboards
- runtime inventory
- vulnerability inventory
- SLO reporting
- DR exercises
- FinOps reporting
- periodic architecture assessments

---

# 253. Success Criteria

The decision is successful when:

- architecture requirements are objectively understandable
- developers know which standards apply
- most routine compliance is automated
- architecture reviews focus on meaningful decisions
- services have visible ownership
- critical architecture failures cannot hide inside aggregate scores
- production readiness is evidence-based
- exceptions are visible and time-bound
- unsupported technology decreases
- architecture drift decreases
- Enterprise Ready services demonstrate operational maturity
- architecture governance increases rather than reduces delivery autonomy

---

# 254. Alternatives Rejected

## 254.1 Architecture Review Board Approves Everything

Rejected because centralized approval of routine implementation creates unnecessary bottlenecks.

---

## 254.2 Documentation-Only Governance

Rejected because standards without validation progressively lose effectiveness.

---

## 254.3 One Compliance Percentage

Rejected because aggregate percentages can hide critical risks.

---

## 254.4 No Exceptions

Rejected because enterprise environments contain legitimate transitional and exceptional requirements.

---

## 254.5 Permanent Exceptions by Default

Rejected because they convert governance failures into invisible architecture debt.

---

## 254.6 Same Controls for Every Workload

Rejected because governance should be proportional to workload risk and characteristics.

---

# 255. Related Decisions

This ADR consolidates and governs the architecture decisions established by ADR-001 through ADR-049.

Particularly relevant decisions include:

- ADR-006: Flyway Database Migrations
- ADR-016: Application Resilience
- ADR-030: SQS Event Governance
- ADR-031: Database Performance and Data Access
- ADR-034: Java 21 Concurrency and Parallelism
- ADR-036: API Design and Compatibility
- ADR-037: Application Security and Secure Coding
- ADR-038: Dependency and Software Supply Chain Security
- ADR-039: CI/CD, Release and Deployment Governance
- ADR-040: Production Reliability and Operational Readiness
- ADR-041: Architecture Governance and Technical Debt
- ADR-042: Architecture Fitness Functions
- ADR-043: Service Ownership and Team Topologies
- ADR-044: FinOps and Cloud Cost Governance
- ADR-045: Business Continuity and Disaster Recovery
- ADR-046: Data Governance, Privacy and Retention
- ADR-047: Legacy Modernization
- ADR-048: Developer Experience and Platform Engineering
- ADR-049: AI-Assisted Software Engineering

---

# 256. References

- RFC 2119
- RFC 8174
- Domain-Driven Design
- Team Topologies
- DORA
- Google Site Reliability Engineering
- NIST Secure Software Development Framework
- OWASP
- SLSA
- OpenSSF
- SonarQube
- ArchUnit
- Technology Radar
- Platform Engineering
- Architecture Fitness Functions
- Continuous Architecture

---

# 257. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial consolidated Enterprise Architecture Baseline |

---

# 258. Decision Summary

The architecture operating model becomes:

```text
                  BUSINESS STRATEGY
                         |
                         v
                ENTERPRISE ARCHITECTURE
                         |
                         v
                        ADRs
                         |
                         v
               ARCHITECTURE BASELINE
                         |
             +-----------+-----------+
             |                       |
             v                       v
       GOLDEN PATHS             EXCEPTIONS
             |                       |
             v                       v
       IMPLEMENTATION          RISK ACCEPTANCE
             |
             v
       FITNESS FUNCTIONS
             |
             v
       SERVICE SCORECARD
             |
             v
       READINESS STATUS
```

The normative model is:

```text
MUST
 |
 +--> Mandatory
 |
 +--> Failure = Non-Compliant
 |
 +--> Exception required


SHOULD
 |
 +--> Recommended
 |
 +--> Deviation requires rationale


MAY
 |
 +--> Optional
```

The service lifecycle is:

```text
EXPERIMENTAL
     |
     v
DEVELOPMENT READY
     |
     v
PRODUCTION READY
     |
     v
ENTERPRISE READY
     |
     v
OPTIMIZED
     |
     v
RETIRED
```

Governance follows:

```text
CAN THE RULE
BE AUTOMATED?
   /      \
 YES       NO
  |         |
  v         v
FITNESS    HUMAN
FUNCTION   REVIEW
  |         |
  +----+----+
       |
       v
   EVIDENCE
```

Architecture exceptions follow:

```text
NON-COMPLIANCE
      |
      v
   EXCEPTION
      |
      +--> Reason
      +--> Risk
      +--> Owner
      +--> Approval
      +--> Compensating Control
      +--> Expiration
      |
      v
  REMEDIATION
```

Production readiness becomes:

```text
               SERVICE
                  |
                  v
          ARCHITECTURE PROFILE
                  |
                  v
          APPLICABLE CONTROLS
                  |
         +--------+--------+
         |                 |
         v                 v
     AUTOMATED           HUMAN
      EVIDENCE           REVIEW
         |                 |
         +--------+--------+
                  |
                  v
            CRITICAL GATES
                  |
             +----+----+
             |         |
            PASS      FAIL
             |         |
             v         v
        PRODUCTION   BLOCK /
          READY      EXCEPTION
```

Enterprise readiness extends that model:

```text
PRODUCTION READY
       +
AUTOMATED GOVERNANCE
       +
MEASURED SLO
       +
TESTED RECOVERY
       +
SECURITY MATURITY
       +
CAPACITY VALIDATION
       +
COST VISIBILITY
       +
LOW ARCHITECTURE DRIFT
       =
ENTERPRISE READY
```

The complete architecture governance equation is:

```text
CLEAR STANDARDS
      +
MUST / SHOULD / MAY
      +
APPLICABILITY
      +
GOLDEN PATHS
      +
FITNESS FUNCTIONS
      +
POLICY AS CODE
      +
SERVICE SCORECARDS
      +
RISK-BASED REVIEWS
      +
TIME-BOUND EXCEPTIONS
      +
PRODUCTION READINESS
      +
CONTINUOUS VALIDATION
      =
SUSTAINABLE ENTERPRISE ARCHITECTURE
```

The governing principle is:

```text
Architecture governance must not
depend on architects manually
checking every implementation.

Standards that can be automated
should become executable controls.

Decisions requiring judgment
should remain human decisions.

Teams following the approved path
should be able to move quickly.

Deviations should remain possible,
but visible, owned, justified
and proportional to risk.

Architecture is not compliant
because a document says it is.

Architecture is compliant when
the running system continuously
provides evidence that it is.
```
