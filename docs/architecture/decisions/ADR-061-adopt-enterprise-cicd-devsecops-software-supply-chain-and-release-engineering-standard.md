# ADR-061: Adopt Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-061 |
| Title | Adopt Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard |
| Status | Accepted |
| Date | 2026-07-25 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | CI/CD, DevSecOps, Supply Chain Security, Release Engineering |
| Related Work Items | Gradle, SonarQube, JaCoCo, SAST, SCA, SBOM, Containers, Kubernetes, Flyway |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise software delivery is itself a production system.

The delivery chain is:

```text
DEVELOPER
    |
    v
SOURCE CONTROL
    |
    v
PULL REQUEST
    |
    v
CI
    |
    +--> COMPILE
    +--> UNIT TEST
    +--> INTEGRATION TEST
    +--> JACOCO
    +--> SONARQUBE
    +--> SAST
    +--> SCA
    +--> SECRET SCAN
    +--> CONTRACT TEST
    |
    v
ARTIFACT BUILD
    |
    v
SBOM / SIGN / SCAN
    |
    v
ARTIFACT REGISTRY
    |
    v
PROMOTION
    |
    v
DEPLOYMENT
    |
    v
VALIDATION
    |
    v
PRODUCTION
```

Failures in this chain can introduce:

- vulnerable dependencies
- compromised artifacts
- untested code
- migration failures
- environment drift
- unverifiable releases
- accidental secret exposure
- incompatible contracts
- production regressions

CI/CD is therefore part of the software architecture and security boundary.

---

# 2. Problem Statement

The organization requires standards covering:

- source-control workflow
- pull requests
- protected branches
- Gradle builds
- reproducible builds
- unit tests
- integration tests
- JaCoCo
- SonarQube
- SAST
- SCA
- dependency scanning
- secret scanning
- container scanning
- SBOM
- artifact provenance
- quality gates
- Flyway validation
- contract tests
- Testcontainers
- artifact repositories
- immutable releases
- artifact promotion
- semantic versioning
- deployment gates
- rollback
- hotfixes
- release traceability
- DORA metrics
- software supply-chain security

---

# 3. Decision Drivers

Primary drivers are:

1. release reliability
2. security
3. traceability
4. reproducibility
5. automation
6. fast feedback
7. quality
8. compliance
9. rollback capability
10. supply-chain integrity
11. deployment consistency
12. developer productivity

---

# 4. Decision

The platform adopts an automated CI/CD and DevSecOps pipeline in which a single immutable release artifact is built, validated and promoted through environments.

Canonical model:

```text
SOURCE COMMIT
      |
      v
     CI
      |
      +--> BUILD
      +--> TEST
      +--> QUALITY
      +--> SECURITY
      +--> CONTRACT
      |
      v
IMMUTABLE ARTIFACT
      |
      v
ARTIFACT REGISTRY
      |
      +--> DEV
      |
      +--> TEST
      |
      +--> HML
      |
      +--> PRD
```

The artifact MUST NOT be rebuilt between environments.

---

# 5. Fundamental Principle

The governing principle is:

```text
BUILD ONCE.

VERIFY ONCE.

PROMOTE THE SAME
IMMUTABLE ARTIFACT.

NEVER CHANGE A RELEASE
AFTER IT HAS BEEN BUILT.
```

---

# 6. Source Control

All production application code MUST reside in approved source control.

---

# 7. Version-Controlled Assets

The repository SHOULD include applicable:

```text
Application Code

Tests

Gradle Configuration

Flyway Migrations

Dockerfile

Kubernetes Configuration

Infrastructure Definitions

Architecture Tests

ADR Documentation
```

---

# 8. Generated Secrets

Secrets MUST NOT be committed to source control.

---

# 9. Protected Branch

Primary release branches MUST use branch protection.

---

# 10. Direct Push

Direct production-oriented changes to protected branches SHOULD be prohibited.

---

# 11. Pull Request

Material production changes SHOULD enter protected branches through pull requests.

---

# 12. PR Validation

A pull request MUST pass applicable automated checks before merge.

---

# 13. Review

Production code SHOULD receive peer review.

---

# 14. Self-Approval

A developer SHOULD NOT be the sole approver of a material production change where organizational controls require independent review.

---

# 15. Small Pull Requests

Pull requests SHOULD remain sufficiently small to allow meaningful review.

---

# 16. Huge PR

Large unrelated changes increase:

```text
Review Risk

Merge Risk

Regression Risk

Rollback Complexity
```

---

# 17. Branch Strategy

The branch strategy SHOULD minimize long-lived divergence.

---

# 18. Trunk-Based Development

Trunk-based or short-lived branch development SHOULD be preferred where organizational workflow permits it.

---

# 19. Long-Lived Feature Branch

Long-lived feature branches SHOULD be avoided because they increase integration risk.

---

# 20. Feature Flags

Incomplete functionality MAY be merged behind controlled feature flags.

---

# 21. Feature Flag Is Not Branch

Feature flags SHOULD reduce deployment coupling rather than become permanent configuration debt.

---

# 22. Build Tool

Gradle is the standard build tool for applicable Java services.

---

# 23. Gradle Wrapper

Repositories MUST include and use:

```text
gradlew

gradlew.bat

gradle/wrapper
```

---

# 24. System Gradle

CI MUST NOT depend on an arbitrary globally installed Gradle version.

---

# 25. Build Command

The standard verification baseline SHOULD include:

```text
./gradlew clean build
```

plus project-specific quality/security tasks.

---

# 26. Reproducible Build

Builds SHOULD be reproducible from:

```text
Source Commit

Build Configuration

Dependency Metadata

Approved Build Environment
```

---

# 27. Dependency Pinning

Dependency versions SHOULD be controlled explicitly.

---

# 28. Dynamic Version

Uncontrolled dependency declarations such as:

```text
1.+

latest.release
```

SHOULD NOT be used for production dependencies.

---

# 29. Dependency Locking

Gradle dependency locking or equivalent controls SHOULD be considered for critical services.

---

# 30. Dependency Verification

Dependency checksum/signature verification SHOULD be enabled where practical.

---

# 31. Repository Control

Dependencies MUST come from approved artifact repositories.

---

# 32. Arbitrary Repository

Production builds MUST NOT download dependencies from arbitrary untrusted repositories.

---

# 33. Build Environment

CI build environments SHOULD be ephemeral and reproducible.

---

# 34. Developer Machine Dependency

A release MUST NOT depend on undeclared files/configuration from a developer workstation.

---

# 35. Test Layers

The pipeline SHOULD apply progressively more expensive validation.

Canonical order:

```text
FAST
 |
 +--> COMPILE
 +--> UNIT TEST
 +--> STATIC ANALYSIS
 |
 +--> INTEGRATION TEST
 +--> CONTRACT TEST
 |
 +--> SECURITY
 +--> ARTIFACT
 |
 +--> DEPLOYMENT VALIDATION
 |
SLOW
```

---

# 36. Fail Fast

Cheap/high-value checks SHOULD execute early.

---

# 37. Unit Tests

Unit tests MUST execute automatically in CI.

---

# 38. Failed Test

A failed mandatory test MUST fail the pipeline.

---

# 39. Disabled Tests

Tests MUST NOT be routinely disabled merely to obtain a green pipeline.

---

# 40. Flaky Tests

Flaky tests are defects.

---

# 41. Flaky Retry

Automatic test retries MAY be used temporarily for diagnosis but MUST NOT permanently conceal instability.

---

# 42. Java Testing Standard

Java tests MUST follow established engineering conventions.

---

# 43. AssertJ

AssertJ SHOULD be the preferred assertion library for applicable Java tests.

Assertions SHOULD include meaningful:

```java
.as("...")
```

descriptions before assertion predicates where required by the project quality standard.

---

# 44. Test Naming

Test names SHOULD clearly describe the scenario and expected behavior.

---

# 45. Deterministic Tests

Tests SHOULD avoid uncontrolled:

```text
Random UUID

Current Time

Thread.sleep

External Internet

Execution Order
```

when these make results non-deterministic.

---

# 46. TestConstants

Stable test fixtures/constants SHOULD be reused where appropriate.

---

# 47. JaCoCo

JaCoCo is the standard Java code-coverage measurement tool.

---

# 48. Coverage Baseline

Applicable enterprise Java services SHOULD maintain:

```text
>= 80%
```

code coverage unless an explicitly approved project threshold differs.

---

# 49. Coverage Is Not Quality

High coverage does not guarantee correct tests.

---

# 50. Meaningful Coverage

Tests MUST validate behavior, not merely execute lines.

---

# 51. Coverage Gaming

Tests written solely to inflate coverage without meaningful assertions are prohibited.

---

# 52. Coverage Regression

Material coverage regression SHOULD fail or block the quality gate according to project policy.

---

# 53. New Code Coverage

New/changed code SHOULD meet the applicable quality threshold independently of legacy coverage.

---

# 54. SonarQube

SonarQube is a mandatory quality-analysis component for applicable Java services.

---

# 55. Sonar Quality Gate

Production promotion MUST NOT routinely proceed with a failed mandatory Sonar quality gate.

---

# 56. Sonar Categories

Quality gates SHOULD evaluate applicable:

```text
Bugs

Vulnerabilities

Code Smells

Duplications

Coverage

Maintainability

Reliability
```

---

# 57. New Code

Quality governance SHOULD prioritize:

```text
Clean as You Code
```

for new and modified code.

---

# 58. Sonar Suppression

Suppressions such as:

```java
@SuppressWarnings
```

MUST have legitimate technical justification.

---

# 59. False Positive

A confirmed false positive MAY be formally marked according to governance rather than distorting code merely to satisfy tooling.

---

# 60. SAST

Static Application Security Testing MUST execute automatically for production application code.

---

# 61. SAST Failure

Critical/high-confidence security findings MUST block release according to enterprise security policy.

---

# 62. SAST Remediation

Security findings SHOULD be corrected at their source rather than hidden through unsafe sanitization or suppression shortcuts.

---

# 63. Security Suppression

SAST suppression requires documented justification and appropriate approval.

---

# 64. SCA

Software Composition Analysis MUST evaluate third-party dependencies.

---

# 65. Dependency Vulnerabilities

Known dependency vulnerabilities MUST be classified according to:

```text
Severity

Exploitability

Exposure

Available Fix

Business Risk
```

---

# 66. CVSS Alone

CVSS alone SHOULD NOT be the only risk signal.

---

# 67. Vulnerable Dependency

A vulnerable dependency SHOULD be upgraded, replaced or explicitly risk-accepted.

---

# 68. Transitive Dependency

Transitive dependencies are part of the software supply chain and MUST be included in scanning.

---

# 69. Unused Dependency

Unused dependencies SHOULD be removed.

---

# 70. Dependency Minimization

Fewer dependencies reduce:

```text
Attack Surface

Build Complexity

Upgrade Cost

Conflict Risk
```

---

# 71. Secret Scanning

Repositories and CI SHOULD automatically scan for secrets.

---

# 72. Secret Types

Scanning SHOULD detect applicable:

```text
Passwords

API Keys

Private Keys

Tokens

Cloud Credentials
```

---

# 73. Secret Found

A committed secret MUST be treated as compromised according to security policy.

---

# 74. Delete Is Insufficient

Deleting the secret from the latest commit does not necessarily remove it from repository history.

---

# 75. Rotation

Exposed credentials MUST be rotated/revoked.

---

# 76. Container Build

Container images MUST be created only after required application verification succeeds.

---

# 77. Container Scan

Production container images MUST undergo vulnerability scanning.

---

# 78. OS Packages

Container scanning MUST include relevant operating-system packages.

---

# 79. Application Dependencies

Container/supply-chain scanning SHOULD also account for application dependencies.

---

# 80. Base Image

Base-image vulnerabilities MUST be managed as part of application release engineering.

---

# 81. Minimal Base Image

Minimal approved base images SHOULD be preferred.

---

# 82. Image Digest

The exact deployed image SHOULD be traceable by digest.

---

# 83. Artifact Immutability

Published release artifacts MUST be immutable.

---

# 84. Overwriting Version

An existing release version MUST NOT be overwritten with different binary content.

---

# 85. Version Uniqueness

A version uniquely identifies one immutable artifact.

---

# 86. Build Metadata

Artifacts SHOULD contain or expose traceability metadata such as:

```text
Version

Git Commit

Build Time

Pipeline ID
```

where appropriate.

---

# 87. SBOM

Production artifacts SHOULD generate a Software Bill of Materials.

---

# 88. SBOM Content

The SBOM SHOULD identify relevant:

```text
Application Components

Libraries

Versions

Package Identifiers
```

---

# 89. SBOM Format

Approved formats MAY include:

```text
CycloneDX

SPDX
```

---

# 90. SBOM Association

The SBOM MUST be associated with the exact release artifact it describes.

---

# 91. SBOM Mutation

An SBOM MUST NOT silently describe a different rebuilt artifact using the same release version.

---

# 92. Provenance

Build provenance SHOULD establish:

```text
What was built?

From which source?

By which pipeline?

Using which process?
```

---

# 93. Artifact Signing

Release artifact/image signing SHOULD be adopted where enterprise platform capabilities support it.

---

# 94. Signature Verification

Deployment systems SHOULD verify approved provenance/signatures where available.

---

# 95. SLSA

Software supply-chain maturity SHOULD move toward applicable SLSA principles.

---

# 96. Build Isolation

Production builds SHOULD execute in controlled environments isolated from arbitrary developer state.

---

# 97. Pipeline Credentials

CI/CD credentials MUST use least privilege.

---

# 98. Long-Lived Pipeline Credentials

Long-lived static cloud credentials SHOULD be avoided where workload/federated identity is available.

---

# 99. Pipeline Secret

Pipeline secrets MUST NOT appear in logs.

---

# 100. Pull Request Security

Untrusted pull-request code MUST NOT automatically gain access to privileged production secrets.

---

# 101. Fork Security

CI pipelines processing external/untrusted forks MUST isolate secret-bearing stages.

---

# 102. Artifact Repository

Release artifacts MUST be stored in approved repositories/registries.

---

# 103. Artifact Promotion

Promotion means:

```text
Use the same artifact
in a higher environment.
```

---

# 104. Rebuild Is Not Promotion

This is prohibited:

```text
DEV BUILD
    |
    v
DEV

then

NEW BUILD
    |
    v
PRD
```

while claiming both are the same release.

---

# 105. Canonical Promotion

Use:

```text
BUILD
  |
  v
ARTIFACT X
  |
  +--> DEV
  +--> TEST
  +--> HML
  +--> PRD
```

---

# 106. Environment Configuration

Environment-specific configuration MUST remain outside the immutable application artifact where practical.

---

# 107. Configuration Promotion

Configuration changes SHOULD also be version controlled and auditable.

---

# 108. Semantic Versioning

Services SHOULD use a consistent versioning strategy.

Semantic Versioning MAY be used:

```text
MAJOR.MINOR.PATCH
```

where public compatibility semantics make it appropriate.

---

# 109. Internal Services

Internal services MAY use another deterministic release-version convention when enterprise tooling requires it.

---

# 110. Version Source

Release version generation MUST be automated or otherwise controlled.

---

# 111. Snapshot

Snapshot/development versions MUST NOT be promoted as final production releases.

---

# 112. Git Tag

Production releases SHOULD be traceable to a source-control tag or immutable commit.

---

# 113. Release Notes

Material releases SHOULD provide concise change information.

---

# 114. Conventional Commits

Conventional Commits MAY be used to improve automated changelog/version generation.

---

# 115. Database Migration Validation

Flyway migrations MUST be validated during CI.

---

# 116. Applied Migration Immutability

Previously applied Flyway migrations MUST remain immutable.

---

# 117. Migration Modification Gate

CI SHOULD detect modification of migrations known to have been released/applied.

---

# 118. Database Correction

Database corrections MUST use:

```text
NEW MIGRATION
+
NEW VERSION
```

---

# 119. Empty Database Test

CI SHOULD validate migration from an empty PostgreSQL database.

---

# 120. Upgrade Test

Critical services SHOULD validate migration from a representative previous released schema.

---

# 121. Testcontainers PostgreSQL

PostgreSQL migration/integration behavior SHOULD use Testcontainers where database semantics matter.

---

# 122. Migration Compatibility

CI/CD SHOULD validate expand/contract compatibility for material schema evolution.

---

# 123. Destructive Migration

Destructive migrations MUST receive explicit review.

---

# 124. Migration Rollback Assumption

Pipelines MUST NOT assume that rolling back the application automatically rolls back database schema.

---

# 125. Contract Testing

Service integration contracts SHOULD be automatically validated.

---

# 126. REST Contract

REST contract tests SHOULD validate applicable:

```text
Request

Response

Status

Required Fields

Compatibility
```

---

# 127. Kafka Contract

Kafka event contracts SHOULD validate:

```text
Schema

eventType

eventVersion

Envelope

Compatibility
```

---

# 128. Consumer Compatibility

Contract governance SHOULD prevent a producer from deploying an incompatible change before consumers are ready.

---

# 129. OpenAPI

OpenAPI contracts SHOULD be validated for applicable REST services.

---

# 130. Breaking API Change

Breaking API changes MUST follow ADR-056 versioning/migration requirements.

---

# 131. Integration Tests

Integration tests SHOULD execute against representative infrastructure where semantics matter.

---

# 132. Testcontainers

Testcontainers SHOULD be used for applicable:

```text
PostgreSQL

Redis

Kafka
```

integration testing.

---

# 133. Mock Limitation

Mocks do not prove infrastructure protocol behavior.

---

# 134. Integration-Test Scope

Integration tests SHOULD focus on meaningful boundaries rather than duplicating every unit test.

---

# 135. Pipeline Parallelism

Independent checks SHOULD execute in parallel where doing so improves feedback time without compromising correctness.

---

# 136. Parallel Test Safety

Parallel tests MUST remain isolated and deterministic.

---

# 137. Shared Mutable Test Data

Parallel CI MUST NOT rely on unsafe shared mutable state.

---

# 138. Build Cache

Gradle build caching MAY be used to improve pipeline performance.

---

# 139. Cache Correctness

CI caching MUST NOT cause stale or incorrect release artifacts.

---

# 140. Cache Key

Pipeline cache keys MUST include relevant build/dependency inputs.

---

# 141. Clean Build

Release validation SHOULD periodically or always perform a clean build according to project risk.

---

# 142. CI Duration

Pipeline duration SHOULD be monitored.

---

# 143. Slow Pipeline

A slow pipeline reduces feedback quality and encourages bypass behavior.

---

# 144. Optimize Safely

Pipeline optimization SHOULD use:

```text
Parallelism

Caching

Test Segmentation

Incremental Analysis
```

without removing necessary controls.

---

# 145. Quality Gate

A release candidate MUST satisfy all mandatory quality gates.

---

# 146. Example Gate

```text
COMPILE                PASS
UNIT TEST              PASS
INTEGRATION TEST       PASS
JACOCO                  PASS
SONAR                   PASS
SAST                    PASS
SCA                     PASS
SECRET SCAN             PASS
CONTRACT TEST           PASS
FLYWAY VALIDATION       PASS
CONTAINER SCAN          PASS
```

---

# 147. Gate Ownership

Each mandatory gate MUST have an identifiable owner/policy.

---

# 148. Warning vs Blocking

Tool findings MUST have explicit:

```text
Warning

Blocking

Exception
```

semantics.

---

# 149. Permanent Warning

Critical controls SHOULD NOT remain permanently configured as non-blocking warnings.

---

# 150. Quality Exception

A gate exception MUST be:

```text
Explicit

Time-Bounded

Owned

Auditable

Risk-Accepted
```

---

# 151. Exception Expiration

Exceptions SHOULD have expiration/remediation dates.

---

# 152. Deployment Gate

Production deployment MUST require successful release validation.

---

# 153. Environment Promotion

Higher-environment promotion SHOULD require evidence from lower-environment validation where appropriate.

---

# 154. Manual Approval

Manual production approval MAY be required according to organizational risk/change policy.

---

# 155. Manual Approval Is Not Testing

Human approval MUST NOT substitute for automated validation.

---

# 156. Deployment Identity

Every deployment MUST identify:

```text
Service

Version

Artifact

Commit

Environment

Timestamp

Pipeline
```

---

# 157. Deployment Audit

Production deployment history MUST be auditable.

---

# 158. Kubernetes Deployment

Container deployment MUST follow ADR-060.

---

# 159. Readiness Gate

A rollout MUST verify that new instances become ready.

---

# 160. Deployment Timeout

Deployment automation MUST have bounded waiting.

---

# 161. Failed Rollout

A rollout that does not achieve required health MUST fail.

---

# 162. Smoke Test

Post-deployment smoke tests SHOULD validate critical application availability.

---

# 163. Synthetic Validation

Critical flows MAY use controlled synthetic transactions after deployment.

---

# 164. Observability Gate

High-risk releases SHOULD evaluate operational telemetry during rollout.

---

# 165. Canary Analysis

Canary deployment SHOULD compare applicable:

```text
Error Rate

Latency

Resource Usage

Business Failures
```

before broader promotion.

---

# 166. Rollback

Every production release MUST have an understood rollback or forward-recovery strategy.

---

# 167. Application Rollback

Application rollback SHOULD redeploy the previous immutable artifact.

---

# 168. Rebuild Previous Version

Rollback MUST NOT require rebuilding the previous release from source.

---

# 169. Schema Compatibility

Rollback capability depends on database compatibility.

---

# 170. Event Compatibility

Rollback capability also depends on Kafka/event compatibility.

---

# 171. Cache Compatibility

Shared Redis representations must remain compatible or version-isolated.

---

# 172. Forward Fix

For some database/data failures, forward fix is safer than destructive rollback.

---

# 173. Rollback Decision

Incident response SHOULD distinguish:

```text
ROLLBACK APPLICATION

DISABLE FEATURE

ROLL FORWARD

RESTORE DATA
```

---

# 174. Feature Flag Rollback

Feature flags MAY provide rapid functional disablement without binary rollback.

---

# 175. Kill Switch

High-risk integrations MAY provide controlled kill switches where appropriate.

---

# 176. Hotfix

Production hotfixes MUST still pass applicable automated quality/security gates.

---

# 177. Emergency Does Not Mean Uncontrolled

An emergency release MAY use an accelerated path but MUST preserve:

```text
Traceability

Review

Build Integrity

Testing

Security

Audit
```

as much as practical.

---

# 178. Hotfix Merge-Back

Hotfix changes MUST be reconciled with the primary development line.

---

# 179. Release Freeze

Release freezes MAY be used during high-risk business periods.

---

# 180. Freeze Exception

Freeze exceptions MUST follow explicit approval.

---

# 181. Change Failure

Failed releases MUST feed engineering learning rather than only operational remediation.

---

# 182. Post-Incident Review

Material release failures SHOULD receive blameless technical analysis.

---

# 183. DORA Metrics

Engineering delivery SHOULD monitor DORA metrics where organizationally applicable.

---

# 184. Deployment Frequency

Measure how frequently production changes are successfully deployed.

---

# 185. Lead Time for Changes

Measure elapsed time from committed change to production availability.

---

# 186. Change Failure Rate

Measure the proportion of production changes requiring remediation such as:

```text
Rollback

Hotfix

Incident Response
```

---

# 187. Mean Time to Restore

Measure recovery time from production degradation/failure.

---

# 188. Metric Purpose

DORA metrics SHOULD guide system improvement rather than individual developer performance evaluation.

---

# 189. Gaming Metrics

Teams MUST NOT optimize delivery metrics by artificially manipulating definitions.

---

# 190. Deployment Frequency Alone

Higher deployment frequency is not automatically better if reliability degrades.

---

# 191. Release Health

Release engineering SHOULD balance:

```text
SPEED

QUALITY

SECURITY

RELIABILITY
```

---

# 192. Pipeline Observability

CI/CD itself MUST be observable.

---

# 193. Pipeline Metrics

Monitor applicable:

```text
Build Success Rate

Build Duration

Queue Time

Test Duration

Flaky Test Rate

Deployment Duration

Deployment Failure Rate
```

---

# 194. Security Metrics

Monitor applicable:

```text
Open Vulnerabilities

Remediation Age

Secret Findings

SAST Findings

Dependency Findings
```

---

# 195. Quality Metrics

Monitor:

```text
Coverage

New Code Coverage

Duplications

Quality Gate Failures
```

---

# 196. Release Metrics

Monitor:

```text
Deployments

Rollbacks

Hotfixes

Failed Rollouts
```

---

# 197. Pipeline Alerting

Critical pipeline/platform failures SHOULD be operationally visible.

---

# 198. CI Availability

CI/CD infrastructure is a critical engineering dependency and SHOULD have defined ownership.

---

# 199. Pipeline Code

Pipeline definitions SHOULD be version controlled.

---

# 200. Reusable Pipeline

Common controls SHOULD be implemented through reusable enterprise pipeline components/templates where practical.

---

# 201. Copy-Paste Pipeline

Duplicating large pipeline definitions across many services SHOULD be avoided.

---

# 202. Central Template Risk

Shared templates MUST be versioned because an uncontrolled central change can affect many services simultaneously.

---

# 203. Template Version

Services SHOULD pin or deliberately adopt pipeline-template versions.

---

# 204. Supply-Chain Threat Model

The delivery system MUST consider compromise of:

```text
Source Repository

Build Runner

Dependency Repository

Container Registry

CI Credentials

Deployment Credentials

Base Images

Third-Party Actions/Plugins
```

---

# 205. Third-Party CI Action

Third-party pipeline actions/plugins MUST be approved and version pinned.

---

# 206. Mutable Action Tag

Security-sensitive pipelines SHOULD NOT depend on mutable third-party action tags.

---

# 207. Build Runner

Privileged build runners MUST be tightly controlled.

---

# 208. Runner Isolation

Untrusted workloads SHOULD NOT share privileged persistent runners without isolation.

---

# 209. Artifact Boundary

Only artifacts produced by approved pipeline stages SHOULD be eligible for production promotion.

---

# 210. Manual Artifact Upload

Manually uploaded binaries SHOULD NOT bypass normal provenance controls.

---

# 211. Production Registry

Production deployment SHOULD pull only from approved artifact/container registries.

---

# 212. Artifact Retention

Release artifacts MUST be retained long enough to satisfy rollback and audit requirements.

---

# 213. Previous Release

At minimum, operational rollback MUST have access to required previous approved artifacts.

---

# 214. Artifact Cleanup

Artifact cleanup policies MUST not remove still-supported rollback targets prematurely.

---

# 215. Release Evidence

A release SHOULD preserve evidence including applicable:

```text
Test Results

Coverage

Sonar Result

Security Scan

SBOM

Artifact Digest

Deployment Record
```

---

# 216. Compliance Evidence

Evidence SHOULD be generated automatically rather than reconstructed manually after an incident/audit.

---

# 217. Pipeline Failure Classification

Failures SHOULD distinguish:

```text
CODE FAILURE

TEST FAILURE

QUALITY FAILURE

SECURITY FAILURE

INFRASTRUCTURE FAILURE

DEPLOYMENT FAILURE
```

---

# 218. Infrastructure Failure

A temporary CI infrastructure outage SHOULD NOT be mislabeled as an application defect.

---

# 219. Retry Pipeline

Retrying infrastructure-failed jobs MAY be appropriate.

---

# 220. Retrying Code Failure

Repeatedly rerunning deterministic failing tests without code changes is not remediation.

---

# 221. Release Candidate

A release candidate is the exact artifact proposed for production.

---

# 222. Post-Build Mutation

A release candidate MUST NOT be modified after verification.

---

# 223. Configuration Validation

Runtime configuration SHOULD be validated before or during deployment.

---

# 224. Missing Configuration

Missing mandatory configuration MUST fail safely.

---

# 225. Secret Availability

Deployment SHOULD validate that required secret references exist without exposing secret values.

---

# 226. Environment Drift

Environment drift SHOULD be detected through declarative infrastructure/configuration tooling.

---

# 227. Manual Drift

Untracked manual production configuration changes SHOULD be reconciled back into source-controlled definitions or removed.

---

# 228. Release Checklist

A production release SHOULD satisfy:

```text
[ ] Source reviewed

[ ] Build reproducible

[ ] Unit tests passed

[ ] Integration tests passed

[ ] JaCoCo threshold passed

[ ] SonarQube gate passed

[ ] SAST passed

[ ] SCA passed

[ ] Secret scan passed

[ ] Contract tests passed

[ ] Flyway validated

[ ] Applied migrations unchanged

[ ] Container scan passed

[ ] SBOM generated

[ ] Artifact immutable

[ ] Artifact digest recorded

[ ] Runtime configuration validated

[ ] Rollback/forward recovery understood

[ ] Deployment health validated
```

---

# 229. Pull Request Checklist

Material pull requests SHOULD evaluate:

```text
[ ] Tests added/updated

[ ] Assertions meaningful

[ ] Sonar issues addressed

[ ] Security findings addressed

[ ] Dependencies justified

[ ] API compatibility preserved

[ ] Event compatibility preserved

[ ] Flyway rule respected

[ ] Performance implications reviewed

[ ] Observability updated

[ ] Documentation updated
```

---

# 230. Release Fitness Functions

Stable delivery rules SHOULD be automated.

Examples:

```text
[ ] Gradle Wrapper present

[ ] Build succeeds

[ ] Tests succeed

[ ] Coverage >= required threshold

[ ] Sonar gate passes

[ ] SAST gate passes

[ ] SCA gate passes

[ ] Secret scan passes

[ ] Flyway validates

[ ] Released migrations unchanged

[ ] OpenAPI compatibility passes

[ ] Kafka schema compatibility passes

[ ] Container scan passes

[ ] SBOM generated

[ ] Image is non-root

[ ] Artifact version is immutable

[ ] Deployment manifest uses immutable image
```

---

# 231. Enterprise Release Gate

A release is not considered compliant when applicable conditions include:

```text
[ ] Tests fail

[ ] Mandatory quality gate fails

[ ] Coverage threshold fails

[ ] Critical SAST finding unresolved

[ ] Critical vulnerable dependency unresolved

[ ] Secret committed

[ ] Applied Flyway migration modified

[ ] Breaking contract introduced without migration strategy

[ ] Artifact rebuilt between environments

[ ] Existing version overwritten

[ ] Container vulnerability policy fails

[ ] Artifact provenance unavailable

[ ] Production artifact cannot be traced to source commit

[ ] Rollback artifact unavailable

[ ] Production deployment bypasses required pipeline controls
```

---

# 232. Anti-Patterns

The following are prohibited or strongly discouraged:

- manual production builds
- compiling releases on developer workstations
- different binary builds for DEV and PRD
- mutable release versions
- mutable `latest` as production identity
- direct push to protected release branches
- routinely bypassing failed tests
- disabling tests to make the build green
- meaningless coverage-only tests
- Sonar suppression without justification
- SAST suppression as the default remediation
- ignoring transitive dependency vulnerabilities
- committing credentials
- deleting leaked credentials without rotating them
- unpinned untrusted pipeline actions
- privileged CI runners for arbitrary untrusted code
- manually uploading unverified production binaries
- modifying applied Flyway migrations
- using `flyway repair` to legitimize altered migration history
- breaking REST/Kafka contracts without migration strategy
- rebuilding an artifact for production
- production deployment without traceable commit/artifact
- rollback requiring artifact rebuild
- indefinite quality-gate exceptions
- emergency releases without audit trail
- DORA metrics used to rank individual developers

---

# 233. Positive Consequences

The decision provides:

- reproducible builds
- faster defect detection
- stronger quality gates
- stronger security gates
- immutable releases
- improved rollback
- migration safety
- contract protection
- artifact traceability
- dependency visibility
- SBOM generation
- stronger supply-chain security
- measurable delivery performance

---

# 234. Negative Consequences

The decision introduces:

- more pipeline stages
- security tooling
- additional build time
- SBOM/provenance management
- stricter release governance
- dependency remediation effort
- pipeline maintenance

These costs are accepted because release automation is significantly less expensive than diagnosing unverifiable or compromised production releases.

---

# 235. Neutral Consequences

The decision also means:

- not every repository needs identical pipeline duration
- not every security finding has identical risk
- not every release requires manual approval
- not every service requires semantic versioning
- some quality exceptions remain legitimate
- some infrastructure failures require pipeline retry
- some production failures require forward fixes instead of rollback

---

# 236. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Vulnerable dependency | Critical | Medium | SCA + remediation |
| Secret leakage | Critical | Low/Medium | Secret scanning + rotation |
| Compromised artifact | Critical | Low | Provenance + signing |
| Test regression | High | Medium | Mandatory CI |
| Low coverage | Medium/High | Medium | JaCoCo gate |
| Code-quality regression | High | Medium | SonarQube |
| SAST defect | Critical | Medium | Blocking security gate |
| Migration corruption | Critical | Low/Medium | Immutable Flyway history |
| Breaking contract | High | Medium | Contract tests |
| Environment drift | High | Medium | Artifact promotion + IaC |
| Failed deployment | High | Medium | Health gates + rollback |
| Pipeline compromise | Critical | Low | Least privilege + isolation |

---

# 237. Implementation Guidance

The following rules are mandatory:

1. Production code must be version controlled.
2. Material changes should use pull-request review.
3. Protected branches must enforce required checks.
4. Gradle Wrapper must be used for Java builds.
5. Dependency versions must remain controlled.
6. Production builds must be reproducible.
7. Unit tests must run automatically.
8. Mandatory failing tests must fail the pipeline.
9. Java tests must follow established project testing conventions.
10. AssertJ assertions should contain meaningful `.as("...")` descriptions where required.
11. Applicable Java services should maintain at least 80% JaCoCo coverage unless another approved threshold exists.
12. Coverage must not replace behavioral test quality.
13. SonarQube quality gates must execute automatically.
14. SAST must execute automatically.
15. SCA/dependency vulnerability scanning must execute automatically.
16. Secret scanning must protect source and pipeline flows.
17. Container images must be scanned.
18. Release artifacts must be immutable.
19. Existing artifact versions must never be overwritten.
20. Production releases must be traceable to source commits.
21. SBOMs should be generated for production artifacts.
22. Build provenance should be recorded.
23. CI/CD credentials must follow least privilege.
24. Untrusted PR code must not automatically receive production secrets.
25. The same immutable artifact must be promoted across environments.
26. Production artifacts must not be rebuilt after lower-environment validation.
27. Flyway migrations must be validated in CI.
28. Previously applied Flyway migrations must never be modified.
29. Database corrections must use new migrations with new versions.
30. PostgreSQL-specific migration behavior should be tested with PostgreSQL.
31. REST and Kafka contract compatibility should be automated.
32. Infrastructure-sensitive integration tests should use representative infrastructure.
33. Quality/security gates must have explicit blocking semantics.
34. Gate exceptions must be explicit, owned, auditable and time-bounded.
35. Deployment must identify exact artifact/version/commit.
36. Production rollout must verify readiness.
37. Rollback should redeploy a previously verified immutable artifact.
38. Rollback must not require rebuilding old releases.
39. Emergency releases must preserve traceability and essential controls.
40. CI/CD performance and failure rates must be observable.
41. DORA metrics should be used for process improvement.
42. Pipeline definitions must be version controlled.
43. Third-party CI components must be approved and pinned.
44. Release evidence should be generated automatically.

---

# 238. Validation

This ADR will be validated through:

- Git
- approved source-control platform
- Gradle
- Java 21
- JUnit 5
- AssertJ
- Mockito
- JaCoCo
- SonarQube
- SAST
- SCA
- secret scanning
- dependency scanning
- SBOM tooling
- CycloneDX/SPDX
- Testcontainers
- Flyway
- PostgreSQL
- Redis
- Kafka
- OpenAPI validation
- OCI container scanning
- artifact registries
- Kubernetes
- AWS/EKS
- CI/CD quality gates
- deployment metrics

---

# 239. Success Criteria

The decision is successful when:

- every production deployment is traceable to an immutable artifact
- every artifact is traceable to source
- the same artifact progresses across environments
- failing tests prevent defective promotion
- coverage regressions are visible
- Sonar regressions are blocked
- critical security findings are detected before production
- vulnerable dependencies are visible
- exposed secrets are detected early
- applied Flyway migrations remain immutable
- breaking contracts are detected before deployment
- SBOMs identify production components
- rollback uses previously verified artifacts
- pipeline failures are diagnosable
- release lead time improves without increasing change failure rate

---

# 240. Alternatives Rejected

## 240.1 Manual Production Builds

Rejected because they are difficult to reproduce and audit.

---

## 240.2 Rebuild for Every Environment

Rejected because tested and production binaries could differ.

---

## 240.3 Quality Tools as Non-Blocking Reports

Rejected for mandatory controls because defects would become normalized.

---

## 240.4 Coverage Without Behavioral Assertions

Rejected because line execution alone does not validate correctness.

---

## 240.5 Modify Existing Flyway Migration

Rejected because released migration history is immutable.

---

## 240.6 Production Artifact Without Provenance

Rejected because source-to-runtime traceability is required.

---

## 240.7 Permanent Security Exceptions

Rejected because temporary risk acceptance must not become invisible permanent debt.

---

# 241. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-045: Disaster Recovery and Regional Resilience Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-057: Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard

---

# 242. References

- Gradle Documentation
- JUnit 5 Documentation
- AssertJ Documentation
- JaCoCo Documentation
- SonarQube Documentation
- OWASP
- OWASP Software Component Verification Standard
- CycloneDX
- SPDX
- SLSA
- NIST Secure Software Development Framework
- Testcontainers Documentation
- Flyway Documentation
- Kubernetes Documentation
- AWS Well-Architected Framework
- DORA / Accelerate Research

---

# 243. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-25 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise CI/CD, DevSecOps and release-engineering baseline |

---

# 244. Decision Summary

The software delivery lifecycle becomes:

```text
CODE
 |
 v
PULL REQUEST
 |
 v
CI
 |
 +--> COMPILE
 +--> UNIT TEST
 +--> INTEGRATION TEST
 +--> JACOCO
 +--> SONAR
 +--> SAST
 +--> SCA
 +--> SECRET SCAN
 +--> CONTRACT TEST
 +--> FLYWAY VALIDATION
 |
 v
BUILD
 |
 v
IMMUTABLE ARTIFACT
 |
 +--> SBOM
 +--> SCAN
 +--> PROVENANCE
 |
 v
REGISTRY
 |
 v
PROMOTION
 |
 +--> DEV
 +--> TEST
 +--> HML
 +--> PRD
```

The critical artifact rule is:

```text
BUILD ONCE
    |
    v
ARTIFACT SHA-256 XYZ
    |
    +--> DEV
    +--> TEST
    +--> HML
    +--> PRD
```

not:

```text
BUILD DEV

BUILD TEST

BUILD HML

BUILD PRD
```

For Java quality:

```text
CODE
 |
 +--> JUNIT
 |
 +--> ASSERTJ
 |
 +--> JACOCO >= 80%
 |
 +--> SONARQUBE
 |
 +--> SAST
 |
 v
QUALITY GATE
```

For database evolution:

```text
V27 APPLIED
    |
    v
CHANGE REQUIRED
    |
    X
DO NOT MODIFY V27
    |
    v
CREATE V28
```

For supply-chain security:

```text
SOURCE
  |
  v
CONTROLLED BUILD
  |
  v
DEPENDENCY VERIFICATION
  |
  v
SCA
  |
  v
CONTAINER SCAN
  |
  v
SBOM
  |
  v
PROVENANCE
  |
  v
IMMUTABLE ARTIFACT
```

For rollback:

```text
CURRENT RELEASE V42
       |
       X
    FAILURE
       |
       v
PREVIOUS VERIFIED V41
       |
       v
REDEPLOY
```

There is no rebuild of V41.

For release governance:

```text
QUALITY
   +
SECURITY
   +
COMPATIBILITY
   +
TRACEABILITY
   +
IMMUTABILITY
   +
RECOVERABILITY
   =
RELEASE CANDIDATE
```

The complete delivery equation is:

```text
VERSION-CONTROLLED SOURCE
        +
PEER REVIEW
        +
REPRODUCIBLE BUILD
        +
AUTOMATED TESTING
        +
JACOCO
        +
SONARQUBE
        +
SAST
        +
SCA
        +
SECRET SCANNING
        +
CONTRACT TESTING
        +
IMMUTABLE FLYWAY HISTORY
        +
SBOM
        +
PROVENANCE
        +
IMMUTABLE ARTIFACT
        +
CONTROLLED PROMOTION
        +
SAFE DEPLOYMENT
        +
ROLLBACK CAPABILITY
        +
PIPELINE OBSERVABILITY
        =
ENTERPRISE RELEASE ENGINEERING
```

The governing principle is:

```text
Do not make production
the first real quality gate.

Build once.

Test the exact artifact
that will be deployed.

Promote instead of rebuilding.

Keep migration history immutable.

Never replace a released artifact.

Know every dependency inside
the production binary.

Make security part of CI,
not a final manual activity.

Make every release traceable
from production back to source.

Keep the previous verified
artifact available for recovery.

Automate rules that humans
should not have to remember.

And treat the software delivery
pipeline as production-critical
infrastructure.
```
