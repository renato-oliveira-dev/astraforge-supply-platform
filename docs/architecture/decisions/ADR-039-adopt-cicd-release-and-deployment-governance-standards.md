# ADR-039: Adopt CI/CD, Release and Deployment Governance Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-039 |
| Title | Adopt CI/CD, Release and Deployment Governance Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | CI/CD, Git, Release, Deployment, Flyway, Rollback, Zero Downtime |
| Related Work Items | Pipelines, Quality Gates, Release Management, Database Migration, Deployment |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform consists of independently deployable services using technologies such as:

- Java 21
- Spring Boot
- Gradle
- PostgreSQL
- Flyway
- SQS
- Redis
- AWS
- containers
- Kubernetes/container orchestration
- SonarQube
- SAST/SCA
- automated testing

A source-code change does not become production software directly.

The actual delivery path is:

```text
Developer
    |
    v
Git
    |
    v
Pull Request
    |
    v
Quality Gates
    |
    v
Controlled Build
    |
    v
Immutable Artifact
    |
    v
DEV
    |
    v
QA / HML
    |
    v
PROD
```

Every transition is part of the software-delivery architecture.

---

# 2. Problem Statement

The platform requires standards defining:

- source-control workflow
- branch protection
- pull requests
- code review
- quality gates
- build reproducibility
- artifact versioning
- artifact promotion
- environment configuration
- release governance
- deployment strategies
- database migrations
- Flyway governance
- backward compatibility
- rollback
- roll-forward
- health checks
- zero-downtime deployment
- feature flags
- emergency releases
- production approvals
- deployment auditability

---

# 3. Decision Drivers

Primary drivers are:

1. production stability
2. traceability
3. reproducibility
4. automation
5. security
6. auditability
7. fast feedback
8. controlled promotion
9. rollback capability
10. zero-downtime evolution
11. database safety
12. reduced deployment risk

---

# 4. Decision

The platform adopts an automated, immutable-artifact delivery model.

The canonical lifecycle is:

```text
SOURCE CHANGE
      |
      v
PULL REQUEST
      |
      v
AUTOMATED VERIFICATION
      |
      v
REVIEW
      |
      v
MERGE
      |
      v
CONTROLLED BUILD
      |
      v
IMMUTABLE RELEASE ARTIFACT
      |
      v
PROMOTION
      |
   +--+--+
   |     |
   v     v
  DEV   QA/HML
           |
           v
          PROD
```

The artifact promoted to production must be the artifact that passed the required verification.

---

# 5. Fundamental Principle

The primary release rule is:

```text
BUILD ONCE

PROMOTE MANY
```

Not:

```text
BUILD FOR DEV

BUILD AGAIN FOR QA

BUILD AGAIN FOR PROD
```

---

# 6. Source Control

Git is the authoritative source for application and deployment-related source artifacts.

---

# 7. Main Branch

The primary branch must represent releasable or near-releasable software according to the team's branching strategy.

---

# 8. Direct Push

Direct uncontrolled pushes to protected production-relevant branches are prohibited.

---

# 9. Pull Request

Production code changes should normally enter protected branches through pull requests.

---

# 10. Pull Request Scope

Pull requests should be sufficiently focused to allow effective review.

---

# 11. Giant Pull Request

Unrelated refactorings, dependency upgrades and business changes should not be combined unnecessarily.

---

# 12. Code Review

At least the required number of authorized reviewers must approve changes according to repository policy.

---

# 13. Self-Approval

A developer should not be the sole approver of their own production-sensitive change.

---

# 14. Branch Protection

Protected branches should enforce applicable controls such as:

```text
Pull Request Required

Required Reviews

Required CI Checks

No Uncontrolled Force Push

Controlled Administrative Override
```

---

# 15. Force Push

Force pushes to protected release branches should be prohibited.

---

# 16. History Integrity

Production release history must remain auditable.

---

# 17. Signed Commits

Commit/tag signing may be required according to enterprise source-control policy.

---

# 18. CI Trigger

Pull requests must trigger the relevant automated verification pipeline.

---

# 19. Fast Feedback

Cheap/high-signal checks should execute early.

---

# 20. Recommended PR Pipeline

```text
Checkout
   |
   v
Environment Validation
   |
   v
Compile
   |
   v
Unit Tests
   |
   v
Static Analysis
   |
   v
SAST
   |
   v
SCA
   |
   v
Integration Tests
   |
   v
Contract Validation
   |
   v
Coverage Gate
```

Exact ordering may be optimized.

---

# 21. Mandatory Quality Gates

Applicable quality gates include:

- compilation
- automated tests
- coverage
- SonarQube
- SAST
- SCA
- secret scanning
- dependency checks
- contract validation
- migration validation

---

# 22. Broken Build

A failing required quality gate must prevent normal merge/promotion.

---

# 23. Local Success Is Insufficient

This is insufficient:

```text
Works on my machine.
```

The authoritative result is the controlled CI execution.

---

# 24. CI Environment

CI should use controlled:

- JDK
- Gradle
- dependency repositories
- environment variables
- credentials
- build container/runner

---

# 25. Gradle Wrapper

Builds must use the repository-controlled Gradle Wrapper.

Example:

```text
./gradlew clean build
```

---

# 26. Clean Build

Release builds should start from a clean controlled workspace.

---

# 27. Test Isolation

Tests must not depend on uncontrolled state from previous pipeline executions.

---

# 28. Flaky Tests

Flaky tests are defects.

Repeated automatic retries must not become a mechanism for hiding them.

---

# 29. Test Failure

A failed test must be investigated rather than simply rerun until green.

---

# 30. Coverage

Coverage follows ADR-035.

Coverage is a gate, not a substitute for meaningful assertions.

---

# 31. SonarQube

Required SonarQube quality gates must pass before normal promotion.

---

# 32. SAST

Required SAST findings must be handled according to ADR-037.

---

# 33. Security Finding

Do not alter correct business behavior merely to make a scanner green.

---

# 34. SCA

Dependency security follows ADR-038.

---

# 35. Secret Scan

Secret scanning must execute before release promotion.

---

# 36. Build Artifact

Successful release pipelines produce a uniquely identifiable artifact.

---

# 37. Artifact Identity

The artifact identity must allow mapping to:

```text
Git Commit

Build

Version

Container Image

Deployment
```

---

# 38. Artifact Immutability

Once published as a release artifact, it must not be modified in place.

---

# 39. Same Version Replacement

Publishing different binary content under the same immutable release identity is prohibited.

---

# 40. New Change

Any changed artifact requires a new build/release identity.

---

# 41. Version Strategy

Services require a deterministic version strategy.

Possible inputs include:

```text
Semantic Version

Release Number

Git SHA

CI Build Number
```

according to platform conventions.

---

# 42. Semantic Versioning

Where Semantic Versioning is adopted:

```text
MAJOR.MINOR.PATCH
```

should reflect externally meaningful compatibility semantics.

---

# 43. Internal Build Identity

A Git SHA/build number may supplement the business/release version.

---

# 44. `latest`

`latest` must not be the authoritative production release identity.

---

# 45. Container Digest

Container digest is preferred for immutable deployment identity where supported.

---

# 46. Build Once

The production artifact must originate from the same build lineage tested before promotion.

---

# 47. Environment-Specific Build

Environment differences must not require recompiling application source.

---

# 48. Configuration

Environment-specific behavior must normally come from external configuration.

---

# 49. Configuration Separation

Conceptually:

```text
APPLICATION ARTIFACT
        +
ENVIRONMENT CONFIGURATION
        +
SECRETS
        =
RUNNING SERVICE
```

---

# 50. Configuration in Artifact

Production credentials or environment-specific secrets must not be embedded in the binary/container.

---

# 51. Environment Promotion

Promotion should move an immutable artifact between environments.

---

# 52. Promotion Model

```text
Artifact A
   |
   +--> DEV
   |
   +--> QA/HML
   |
   +--> PROD
```

---

# 53. Rebuild During Promotion

Rebuilding application code between QA and production is prohibited under normal release flow.

---

# 54. Promotion Evidence

Promotion should preserve evidence of:

- artifact version
- image digest
- source revision
- pipeline execution
- approvals
- deployment outcome

---

# 55. DEV

DEV provides early integration verification.

---

# 56. QA/HML

QA/HML should validate behavior representative of production as far as practical.

---

# 57. Environment Parity

Production-like environments should minimize unnecessary differences in:

- runtime
- database engine/version
- messaging
- network behavior
- configuration structure

---

# 58. Environment Data

Production-sensitive data must not be copied into lower environments without appropriate protection/governance.

---

# 59. Production Promotion

Production deployment requires all mandatory gates and approvals.

---

# 60. Manual Approval

A production approval may be manual where governance requires it.

---

# 61. Manual Deployment Steps

Manual approval is different from manually executing undocumented deployment commands.

---

# 62. Deployment Automation

Production deployments should be automated and repeatable.

---

# 63. Runbook

Exceptional/manual recovery operations require documented runbooks.

---

# 64. Deployment Strategy

Deployment strategy depends on service characteristics.

Supported strategies may include:

```text
Rolling

Blue-Green

Canary
```

---

# 65. Rolling Deployment

Rolling deployment replaces instances gradually.

---

# 66. Rolling Compatibility

During a rolling deployment:

```text
OLD VERSION
     +
NEW VERSION
```

may run simultaneously.

Therefore compatibility is mandatory.

---

# 67. API Compatibility During Rollout

New application versions must tolerate temporary coexistence with old callers/providers where deployment ordering permits it.

---

# 68. Database Compatibility During Rollout

Both old and new application versions may temporarily access the same database schema.

---

# 69. Expand-and-Contract

Database changes requiring zero downtime should use:

```text
EXPAND
   |
   v
DEPLOY COMPATIBLE APPLICATION
   |
   v
MIGRATE USAGE / DATA
   |
   v
CONTRACT
```

---

# 70. Example: Column Rename

Do not perform:

```text
RENAME old_column TO new_column
```

in one deployment if the old application still requires `old_column`.

---

# 71. Safer Evolution

Prefer:

```text
Migration N:
    add new_column

Application:
    support migration

Data migration:
    populate new_column

Later release:
    stop using old_column

Later migration:
    remove old_column
```

---

# 72. Destructive Migration

Destructive database changes require explicit compatibility analysis.

---

# 73. Flyway

Flyway is the authoritative schema migration mechanism where adopted.

---

# 74. Flyway Migration Naming

Versioned migrations follow the project's defined naming convention.

Example:

```text
V27__create_outbox_event.sql
```

---

# 75. Flyway Migration Immutability

A versioned Flyway migration that has already been executed must be treated as immutable.

---

# 76. Critical Rule

Never modify an already applied versioned migration to correct database behavior.

---

# 77. Correct Fix

If:

```text
V27
```

was applied and a defect is discovered, create:

```text
V28
```

or the next available version.

---

# 78. Incorrect Fix

Prohibited:

```text
Edit V27

Recommit V27

Expect existing databases to update
```

---

# 79. Why Applied Migrations Are Immutable

Flyway records migration history/checksums.

Changing an applied migration can create:

- checksum mismatch
- environment divergence
- unreproducible schema history
- deployment failure
- uncertainty about actual database state

---

# 80. Shared Environment Rule

Once a migration has executed in a shared environment, treat it as applied and immutable.

---

# 81. Production Is Not the Boundary

The rule is not:

```text
Migration may be edited until PROD.
```

The rule is:

```text
Once applied to an environment whose history matters,
do not rewrite history.
```

---

# 82. Local Development Exception

A developer may recreate disposable local databases while designing a migration before it enters shared migration history.

However, once the migration is committed/applied as part of the shared delivery flow, corrections should use a new migration.

---

# 83. Migration Ordering

Migration versions must remain globally deterministic for the service.

---

# 84. Parallel Development

Teams must coordinate migration versions when multiple branches introduce schema changes concurrently.

---

# 85. Duplicate Version

Two different migrations must not use the same Flyway version.

---

# 86. Migration Review

Database migrations require code review like application code.

---

# 87. Migration Validation

Review must consider:

- locking
- execution time
- table size
- indexes
- constraints
- backward compatibility
- rollback/roll-forward
- data migration
- production load

---

# 88. DDL Risk

DDL that is trivial on an empty local database may be expensive on a production table containing millions of rows.

---

# 89. Index Creation

Large index creation requires operational impact assessment.

---

# 90. NOT NULL

Adding a mandatory column requires safe evolution.

Avoid unsafe assumptions such as:

```text
ALTER TABLE huge_table
ADD new_column ... NOT NULL
```

without considering existing rows and locking behavior.

---

# 91. Safer Mandatory Column Evolution

Typical pattern:

```text
1. Add nullable column
2. Deploy compatible code
3. Backfill
4. Validate
5. Add constraint
```

when appropriate.

---

# 92. Data Migration

Large data migrations should not automatically be performed as one giant blocking Flyway transaction.

---

# 93. Online Data Migration

Large backfills may require a separately controlled operational process.

---

# 94. Schema vs Data Migration

Schema evolution and large operational data migration may require different mechanisms.

---

# 95. Migration Transaction

Migration transactional behavior must be understood for the database technology and operation.

---

# 96. Migration Failure

A partially failed migration requires diagnosis before any manual repair action.

---

# 97. Flyway Repair

`flyway repair` or equivalent history manipulation must not be used casually to hide migration inconsistencies.

---

# 98. Repair Governance

Production/shared-environment history repair requires explicit technical review.

---

# 99. Baseline

Flyway baseline operations require explicit migration/governance planning.

---

# 100. Out-of-Order

Out-of-order migration behavior should not be enabled casually as a substitute for version coordination.

---

# 101. Repeatable Migration

Repeatable migrations have different semantics from versioned migrations and must be used deliberately.

---

# 102. Versioned Migration

Business/schema history requiring deterministic one-time evolution should normally use versioned migrations.

---

# 103. Rollback

Application rollback must be planned before production deployment.

---

# 104. Rollback Is Not Always Database Rollback

If the new release already changed persistent data/schema:

```text
Rollback Application
```

does not necessarily mean:

```text
Rollback Database
```

---

# 105. Roll-Forward Preference

For many database failures, a corrective forward migration is safer than attempting destructive reversal.

---

# 106. Database Rollback

Database rollback must not be assumed to be automatically safe.

---

# 107. Destructive Down Migration

Automatic destructive down-migrations are discouraged for production data.

---

# 108. Recovery Strategy

Before deployment, determine whether failure will use:

```text
Application rollback

Roll-forward

Feature disablement

Database restore

Corrective migration
```

---

# 109. Backup

High-risk database operations require appropriate backup/recovery readiness.

---

# 110. Restore Testing

A backup that has never been tested for restoration provides limited confidence.

---

# 111. Feature Flags

Feature flags may decouple deployment from feature activation.

---

# 112. Deployment vs Release

With feature flags:

```text
DEPLOY CODE
    !=
RELEASE FEATURE
```

---

# 113. Feature Flag Use Cases

Feature flags are appropriate for:

- progressive activation
- risky integrations
- staged migrations
- operational kill switches

---

# 114. Feature Flag Is Not Authorization

Feature flags must not replace security authorization.

---

# 115. Flag Default

A flag must have an explicit safe default.

---

# 116. Missing Flag

Missing/unavailable feature-flag infrastructure must have defined behavior.

---

# 117. Temporary Flags

Temporary rollout flags require cleanup ownership.

---

# 118. Permanent Flag Debt

Leaving obsolete flags indefinitely creates unnecessary complexity.

---

# 119. Kill Switch

High-risk external integrations may benefit from an operational kill switch.

---

# 120. Health Checks

Deployment automation requires meaningful health checks.

---

# 121. Liveness

Liveness answers:

```text
Should this process be restarted?
```

---

# 122. Readiness

Readiness answers:

```text
Should this instance receive traffic?
```

---

# 123. Difference

Liveness and readiness must not be treated as identical signals.

---

# 124. Dependency in Liveness

Transient failure of an external dependency should not automatically make application liveness fail and cause restart loops.

---

# 125. Readiness Dependency

Critical startup/runtime dependencies may affect readiness according to service semantics.

---

# 126. Startup Probe

Slow-starting applications may use startup probes where supported.

---

# 127. Deployment Success

A container entering `Running` state does not by itself prove deployment success.

---

# 128. Post-Deployment Verification

Deployment should verify:

- readiness
- error rate
- latency
- startup failures
- critical dependency behavior

---

# 129. Smoke Tests

Critical services should support post-deployment smoke tests.

---

# 130. Synthetic Transaction

Where safe, a synthetic transaction may validate a critical path.

---

# 131. Monitoring Window

High-risk deployments may require an observation period before full promotion.

---

# 132. Canary Deployment

Canary deployment exposes the new version to a controlled subset of traffic.

---

# 133. Canary Signals

Promotion should consider:

```text
Error Rate

Latency

Resource Usage

Business Failures
```

---

# 134. Canary Abort

The rollout should stop when defined failure thresholds are exceeded.

---

# 135. Blue-Green

Blue-green deployment maintains separate old/new environments or deployment sets and switches traffic after validation.

---

# 136. Blue-Green Database

Blue-green application deployment does not automatically provide separate database state.

Shared database compatibility remains necessary.

---

# 137. Rolling

Rolling deployments remain appropriate for ordinary stateless services when compatibility requirements are satisfied.

---

# 138. Stateful Workload

Stateful workloads require additional deployment/recovery consideration.

---

# 139. SQS Consumer Deployment

Deploying multiple consumer versions simultaneously requires event-schema compatibility.

---

# 140. Event Compatibility

SQS evolution follows the platform event-governance ADR.

---

# 141. Producer First vs Consumer First

Deployment ordering must consider event compatibility.

---

# 142. Backward-Compatible Event Evolution

Additive compatible event changes reduce deployment-order coupling.

---

# 143. API Deployment Ordering

Service-to-service API changes follow ADR-036 compatibility standards.

---

# 144. Breaking API Deployment

A breaking provider change must not be deployed before consumers are migrated unless version coexistence is provided.

---

# 145. Distributed Deployment

A multi-service feature must assume partial deployment can occur.

---

# 146. Atomic Multi-Service Deployment

Do not design ordinary microservice changes assuming all services update atomically.

---

# 147. Compatibility Window

Services must tolerate an intentional compatibility window.

---

# 148. Configuration Deployment

Configuration changes require the same governance principles as code when they can alter production behavior.

---

# 149. Configuration Review

Security-sensitive configuration changes require review.

---

# 150. Secret Rotation Deployment

Secret rotation should support overlap where required to avoid downtime.

---

# 151. Infrastructure Change

Infrastructure-as-code changes should follow review, CI and controlled promotion.

---

# 152. Manual Infrastructure Drift

Manual production infrastructure changes should be minimized and reconciled back into authoritative configuration.

---

# 153. Drift Detection

Where supported, infrastructure/configuration drift should be detectable.

---

# 154. Release Notes

Material releases should provide concise release information.

---

# 155. Release Content

Release metadata should identify:

- version
- source revision
- significant changes
- migration impact
- compatibility considerations
- known risks where relevant

---

# 156. Database Change Notice

Operationally significant migrations must be visible in release planning.

---

# 157. Breaking Change Notice

Breaking contract changes require explicit consumer migration communication.

---

# 158. Deployment Window

High-risk changes may require controlled deployment windows.

---

# 159. Low-Risk Deployment

Routine low-risk deployments should not require unnecessary manual ceremony when automated gates provide sufficient confidence.

---

# 160. Risk-Based Governance

Deployment governance should be proportional to change risk.

---

# 161. Change Risk Factors

Risk increases with:

- database destructive change
- authentication/authorization change
- public API breaking change
- large dependency/framework upgrade
- critical integration change
- high-volume processing change
- infrastructure change
- large data migration

---

# 162. Emergency Release

Critical production incidents/security vulnerabilities may require an expedited release path.

---

# 163. Emergency Does Not Mean Uncontrolled

Emergency releases must preserve essential:

- source control
- artifact identity
- build
- critical tests
- security checks
- deployment traceability

---

# 164. Emergency Approval

Approval paths may be shortened according to incident policy.

---

# 165. Post-Emergency Review

Emergency changes require subsequent review when normal governance was intentionally reduced.

---

# 166. Hotfix

A hotfix must be merged back into the authoritative development history.

---

# 167. Production-Only Patch

Untracked production-only source changes are prohibited.

---

# 168. Manual JAR Replacement

Manually replacing application binaries on production hosts is prohibited under normal operation.

---

# 169. Database Manual Fix

Ad hoc manual production database changes should be avoided.

---

# 170. Emergency Database Fix

If an emergency manual database correction is unavoidable, it must be:

- authorized
- recorded
- validated
- reconciled into migration/history where applicable

---

# 171. Migration Drift

No environment should depend indefinitely on undocumented manual schema changes.

---

# 172. Observability

Deployment events should be correlated with application telemetry.

---

# 173. Deployment Marker

Monitoring systems should make release/version transitions visible.

---

# 174. Version Metadata

Running services should expose safe build/version metadata through approved operational mechanisms.

---

# 175. Sensitive Metadata

Version metadata must not expose secrets or unnecessary internal details publicly.

---

# 176. Incident Diagnosis

Operators should be able to answer:

```text
Which version is running?

When was it deployed?

Which commit produced it?

Which migration level exists?

What changed?
```

---

# 177. Rollback Trigger

Rollback/abort criteria should be defined before high-risk deployment.

---

# 178. Examples

Potential triggers include:

```text
Error rate above threshold

Critical smoke test failure

Startup failure

Severe latency regression

Data integrity issue
```

---

# 179. Automatic Rollback

Automated rollback may be used when failure signals are reliable and rollback is safe.

---

# 180. Unsafe Automatic Rollback

Do not automatically roll back application versions when database/event compatibility makes rollback unsafe.

---

# 181. Rollback Compatibility

A new database migration must be assessed for compatibility with the previous application version.

---

# 182. Expand Migration Benefit

Expand-and-contract patterns improve rollback compatibility.

---

# 183. Data Transformation

Irreversible data transformations require explicit recovery planning.

---

# 184. Deployment Concurrency

Multiple deployments affecting the same service/environment require coordination.

---

# 185. Deployment Lock

Pipeline/environment locking may prevent overlapping conflicting production deployments.

---

# 186. Migration Concurrency

Only controlled migration execution should modify a service schema during deployment.

---

# 187. Multiple Instances

Flyway/database migration execution must be configured so horizontally scaled application startup does not create unsafe migration races.

---

# 188. Migration Ownership

The service owning the schema owns its migration history.

---

# 189. Cross-Service Database Modification

One microservice must not casually deploy Flyway migrations against another service's owned schema.

---

# 190. Database Ownership

Database ownership follows service boundaries.

---

# 191. Deployment Permissions

CI/CD deployment identities require least privilege.

---

# 192. Production Credential

Production credentials must not be available to ordinary PR validation jobs.

---

# 193. Environment Separation

DEV credentials must not automatically grant PROD access.

---

# 194. Audit Trail

Production deployment actions must be attributable.

---

# 195. Approval Identity

Approval identity must come from the trusted CI/CD/platform identity system.

---

# 196. Deployment Logs

Pipeline logs must avoid leaking secrets.

---

# 197. Artifact Download

Production deployments must retrieve artifacts from approved repositories/registries.

---

# 198. Artifact Verification

Artifact identity/integrity must be verified according to platform capability.

---

# 199. Production Readiness Gate

A release is not production ready until:

```text
[ ] Pull request reviewed

[ ] Protected branch requirements satisfied

[ ] Compilation passes

[ ] Unit tests pass

[ ] Integration tests pass

[ ] Contract tests pass where applicable

[ ] Coverage gate passes

[ ] SonarQube gate passes

[ ] SAST passes / findings assessed

[ ] SCA passes / findings assessed

[ ] Secret scan passes

[ ] Dependency changes reviewed

[ ] Flyway migrations reviewed

[ ] Applied migrations were not modified

[ ] New DB corrections use new migration versions

[ ] Database backward compatibility reviewed

[ ] API/event compatibility reviewed

[ ] Artifact version uniquely identified

[ ] Artifact immutable

[ ] Container image scanned

[ ] SBOM generated

[ ] Source revision traceable

[ ] Same artifact promoted across environments

[ ] Environment configuration reviewed

[ ] Production secrets externalized

[ ] Deployment strategy selected

[ ] Readiness/liveness validated

[ ] Smoke tests defined

[ ] Rollback/roll-forward strategy reviewed

[ ] Feature flags reviewed where applicable

[ ] Observability ready

[ ] Production approval completed where required
```

---

# 200. Anti-Patterns

The following are prohibited or strongly discouraged:

- direct uncontrolled push to protected production branches
- bypassing required CI checks
- merging failing tests
- rerunning flaky tests until green without investigation
- production build from developer workstation
- rebuilding different binaries per environment
- mutable release artifacts
- overwriting released versions
- using `latest` as authoritative production identity
- embedding environment secrets in application artifacts
- undocumented manual production deployment
- manual JAR replacement
- assuming container `Running` means healthy
- identical liveness/readiness semantics without justification
- destructive database migration without compatibility analysis
- modifying an already applied Flyway migration
- changing an applied migration checksum to "fix" deployment
- casual `flyway repair`
- assuming migrations are editable until production
- giant blocking data migration without production-volume analysis
- assuming application rollback automatically rolls back database state
- destructive automatic down migrations
- breaking APIs requiring atomic multi-service deployment
- treating feature flags as authorization
- permanent obsolete feature flags
- untracked production hotfix
- undocumented manual schema drift
- exposing production credentials to PR builds
- deploying an artifact different from the tested/scanned artifact

---

# 201. Positive Consequences

The decision provides:

- deterministic releases
- stronger traceability
- safer database evolution
- reliable artifact promotion
- improved rollback planning
- better zero-downtime capability
- reduced environment drift
- stronger production governance
- improved incident diagnosis
- consistent Flyway history
- safer multi-service evolution

---

# 202. Negative Consequences

The decision introduces:

- pipeline complexity
- compatibility planning
- migration discipline
- artifact governance
- release metadata
- additional deployment verification
- feature-flag lifecycle management
- operational planning for risky migrations

These costs are accepted because deployment failures directly affect production availability and data integrity.

---

# 203. Neutral Consequences

The decision also means:

- rollback is not always the safest recovery mechanism
- roll-forward is often preferable for database defects
- deployment and feature release may occur separately
- old and new service versions may coexist temporarily
- database migrations must support compatibility windows
- production approval can remain risk-based rather than universally manual

---

# 204. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Wrong artifact deployed | Critical | Low | Immutable artifact promotion |
| Database incompatibility | Critical | Medium | Expand-and-contract |
| Flyway checksum mismatch | High | Medium | Applied migration immutability |
| Broken rollback | Critical | Medium | Pre-deployment recovery analysis |
| Environment drift | High | Medium | External configuration + IaC |
| Flaky CI | Medium | Medium | Treat flaky tests as defects |
| Secret leakage | Critical | Low | CI secret isolation |
| Partial multi-service deployment | High | Medium | Compatibility windows |
| Failed migration | Critical | Low | Migration review/testing |
| Bad production rollout | Critical | Medium | Health checks/canary/rollback |
| Artifact substitution | Critical | Low | Immutable identity/provenance |
| Manual DB drift | High | Medium | Migration reconciliation |

---

# 205. Implementation Guidance

The following rules are mandatory:

1. Production-relevant branches must be protected.
2. Production changes normally require pull requests and review.
3. Required CI quality gates must block failing changes.
4. Release builds must use controlled JDK and Gradle Wrapper versions.
5. Production artifacts must be produced by controlled CI.
6. Released artifacts must be immutable.
7. A changed binary requires a new release identity.
8. Build once and promote the same artifact through environments.
9. Environment differences belong in external configuration, not recompilation.
10. Production secrets must remain outside application artifacts.
11. Deployment automation must be repeatable and auditable.
12. Rolling deployments must account for old/new version coexistence.
13. API/event/database changes require compatibility analysis.
14. Zero-downtime database evolution should use expand-and-contract where required.
15. Versioned Flyway migrations are immutable after application.
16. An applied migration must never be edited to perform a correction.
17. Database corrections require a new Flyway migration/version.
18. Shared-environment execution is sufficient to establish migration history.
19. Flyway repair/history manipulation requires explicit technical review.
20. Large data migrations require production-volume analysis.
21. Rollback must not assume automatic database reversal.
22. Roll-forward should be preferred when safer for persistent state.
23. Feature flags may separate deployment from activation.
24. Temporary feature flags require cleanup.
25. Liveness and readiness must have distinct intended semantics.
26. Deployment success requires post-deployment verification.
27. Critical services should support smoke tests.
28. High-risk releases require explicit rollback/abort criteria.
29. Emergency releases must remain traceable and reproducible.
30. Manual production fixes must be reconciled into authoritative source/configuration.
31. Production deployment identities must follow least privilege.
32. Production artifacts must remain traceable to source revisions.
33. The artifact tested/scanned must be the artifact promoted.

---

# 206. Validation

This ADR will be validated through:

- repository branch policies
- pull-request reviews
- CI quality gates
- artifact metadata
- deployment pipelines
- Flyway validation
- schema compatibility review
- automated tests
- smoke tests
- deployment telemetry
- rollback exercises
- disaster-recovery exercises
- security audits
- release audits
- production incident reviews

---

# 207. Success Criteria

The decision is successful when:

- production deployments are reproducible
- every running version maps to a known source revision
- the same tested artifact reaches production
- database history remains deterministic
- Flyway checksum mismatches caused by rewritten migrations disappear
- services can perform rolling deployments safely
- schema changes support application compatibility windows
- failed releases have explicit recovery paths
- production deployments require minimal undocumented manual action
- deployment events are observable
- emergency fixes remain auditable
- release frequency can increase without proportionally increasing deployment risk

---

# 208. Alternatives Rejected

## 208.1 Rebuild Per Environment

Rejected because the production artifact would differ from the artifact tested in previous environments.

---

## 208.2 Edit Flyway Migration Until Production

Rejected because shared environments already establish migration history and checksums.

---

## 208.3 Automatic Database Rollback

Rejected as a universal strategy because persistent-data transformations may be irreversible or unsafe to reverse.

---

## 208.4 Manual Production Deployment

Rejected as the normal process because it reduces repeatability and auditability.

---

## 208.5 Atomic Deployment of All Microservices

Rejected because independent deployment is a fundamental microservice capability.

---

## 208.6 Disable Quality Gates for Faster Delivery

Rejected because deployment speed must come from automation and engineering quality rather than removing controls.

---

# 209. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-030: Adopt SQS Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards

---

# 210. References

- Continuous Delivery
- Trunk-Based Development
- Semantic Versioning
- Flyway Documentation
- Gradle Documentation
- Spring Boot Documentation
- Kubernetes Deployment Documentation
- Kubernetes Probes Documentation
- OWASP Software Supply Chain Security
- NIST Secure Software Development Framework
- SLSA
- CycloneDX
- OpenAPI Specification

---

# 211. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial CI/CD, release and deployment governance baseline |

---

# 212. Decision Summary

The definitive delivery model is:

```text
                       GIT
                        |
                        v
                 PULL REQUEST
                        |
                        v
              +------------------+
              |  QUALITY GATES   |
              +------------------+
              | Compile          |
              | Tests            |
              | Coverage         |
              | Sonar            |
              | SAST             |
              | SCA              |
              | Secret Scan      |
              | Contract Checks  |
              +------------------+
                        |
                        v
                  CODE REVIEW
                        |
                        v
                      MERGE
                        |
                        v
                CONTROLLED BUILD
                        |
                        v
                IMMUTABLE ARTIFACT
                        |
                  +-----+-----+
                  |           |
                  v           v
                 SBOM       IMAGE SCAN
                  |           |
                  +-----+-----+
                        |
                        v
                       DEV
                        |
                        v
                     QA / HML
                        |
                        v
                      PROD
```

The artifact rule is:

```text
BUILD ONCE
    |
    v
Artifact A
    |
    +---------> DEV
    |
    +---------> QA/HML
    |
    +---------> PROD
```

not:

```text
Source
  |
  +--> Build A --> DEV
  |
  +--> Build B --> QA
  |
  +--> Build C --> PROD
```

For Flyway:

```text
V27
 |
 v
APPLIED
 |
 v
IMMUTABLE
```

If a defect exists:

```text
V27 APPLIED
     |
     v
DEFECT DISCOVERED
     |
     v
CREATE V28
     |
     v
CORRECT FORWARD
```

Never:

```text
V27 APPLIED
     |
     v
EDIT V27
     |
     v
CHECKSUM MISMATCH /
ENVIRONMENT DIVERGENCE
```

Zero-downtime database evolution follows:

```text
        RELEASE N

           EXPAND
              |
              v
        ADD NEW STRUCTURE
              |
              v
     OLD APP STILL WORKS
              |
              v
       RELEASE N + 1
              |
              v
     NEW APP USES STRUCTURE
              |
              v
        DATA MIGRATION
              |
              v
       RELEASE N + 2
              |
              v
          CONTRACT
              |
              v
     REMOVE OLD STRUCTURE
```

Deployment recovery follows:

```text
                 FAILURE
                    |
          +---------+---------+
          |                   |
          v                   v
   APPLICATION SAFE?    PERSISTENT STATE
          |                CHANGED?
          |                   |
          v                   v
      ROLLBACK          COMPATIBILITY
                              |
                       +------+------+
                       |             |
                       v             v
                 ROLLBACK SAFE   NOT SAFE
                       |             |
                       v             v
                   ROLLBACK     ROLL-FORWARD
```

Feature delivery follows:

```text
DEPLOY
  |
  v
CODE PRESENT
  |
  v
FEATURE FLAG OFF
  |
  v
VALIDATE
  |
  v
PROGRESSIVE ENABLEMENT
  |
  v
FULL RELEASE
  |
  v
REMOVE TEMPORARY FLAG
```

The most important database rule is:

```text
Database migration history is append-only.

Never rewrite an applied migration.

Correct history by adding the next migration.
```

And the overall release rule is:

```text
SOURCE
   |
   v
VERIFY
   |
   v
BUILD ONCE
   |
   v
IDENTIFY
   |
   v
SCAN
   |
   v
PROMOTE
   |
   v
OBSERVE
   |
   v
RECOVER SAFELY
```

The target state is:

```text
FAST DELIVERY
      +
AUTOMATION
      +
IMMUTABILITY
      +
COMPATIBILITY
      +
TRACEABILITY
      +
SAFE DATABASE EVOLUTION
      =
RELIABLE CONTINUOUS DELIVERY
```
