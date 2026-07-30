# ADR-086: Adopt Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-086 |
| Title | Adopt Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Git, Pull Requests, CI/CD, Quality Engineering, DevSecOps |
| Related Work Items | Java 21, Spring Boot, Gradle, SonarQube, SAST, SCA, JaCoCo, Flyway, OpenAPI |
| Supersedes | ADR-069 |
| Superseded By | None |

---

# 1. Context

Enterprise software quality is not established only by implementation quality.

The complete delivery path is:

```text
REQUIREMENT
    |
    v
CODE
    |
    v
LOCAL VALIDATION
    |
    v
COMMIT
    |
    v
PULL REQUEST
    |
    v
AUTOMATED QUALITY GATES
    |
    v
HUMAN REVIEW
    |
    v
MERGE
    |
    v
ARTIFACT
    |
    v
DEPLOYMENT
```

A technically correct implementation can still create production risk through:

```text
INCOMPLETE TESTS

UNREVIEWED CODE

SECURITY FINDINGS

BROKEN MIGRATIONS

API BREAKING CHANGES

LOW COVERAGE

POOR COMMITS

LARGE PULL REQUESTS

UNCONTROLLED MERGES

NON-REPRODUCIBLE BUILDS
```

Therefore, source-control and delivery governance are architectural concerns.

---

# 2. Problem Statement

The organization requires standards covering:

- Git
- branch strategy
- protected branches
- pull requests
- PR size
- commit conventions
- Conventional Commits
- review requirements
- CODEOWNERS
- architecture review
- SonarQube
- SAST
- SCA
- secret scanning
- JaCoCo
- test coverage
- mutation testing
- Gradle validation
- Flyway validation
- API contract validation
- OpenAPI
- CI/CD
- merge strategy
- release tags
- build artifacts
- rollback
- emergency changes
- Definition of Done

---

# 3. Decision Drivers

Primary drivers are:

1. production quality
2. maintainability
3. security
4. auditability
5. review effectiveness
6. deployment reliability
7. defect prevention
8. fast feedback
9. traceability
10. reproducibility
11. architectural consistency
12. developer productivity

---

# 4. Decision

All production changes MUST flow through a controlled source-management and CI/CD process.

The normal path SHALL be:

```text
BRANCH
   |
   v
IMPLEMENT
   |
   v
TEST
   |
   v
PULL REQUEST
   |
   v
QUALITY GATES
   |
   v
REVIEW
   |
   v
MERGE
   |
   v
BUILD
   |
   v
DEPLOY
```

Direct uncontrolled production-branch modification is prohibited.

---

# 5. Fundamental Principle

```text
Code is not done
when it compiles.

It is done when
it is reviewed,
tested,
secure,
observable,
deployable,
maintainable,
and accepted
by the delivery pipeline.
```

---

# 6. Protected Branch

Primary integration/release branches MUST be protected.

Examples:

```text
main

master

develop
```

according to repository strategy.

---

# 7. Direct Push

Direct developer push to protected production branches SHOULD be prohibited.

---

# 8. Pull Request

Production code changes MUST normally enter protected branches through Pull Requests.

---

# 9. Branch Protection

Protected branches SHOULD enforce:

```text
REQUIRED REVIEW

REQUIRED CI

NO UNRESOLVED COMMENTS

NO FAILED QUALITY GATES

CONTROLLED FORCE PUSH

CONTROLLED DELETION
```

---

# 10. Administrator Bypass

Administrative bypass MUST be exceptional and auditable.

---

# 11. Force Push

Force push to protected release branches SHOULD be prohibited.

---

# 12. Branch Deletion

Protected branches MUST NOT be casually deleted.

---

# 13. Branch Strategy

Repositories SHOULD use the simplest branch strategy compatible with release requirements.

---

# 14. Trunk-Based Development

Trunk-based development with short-lived branches SHOULD be preferred where organizational processes support it.

---

# 15. Long-Lived Feature Branch

Long-lived feature branches SHOULD be avoided.

---

# 16. Branch Lifetime

Feature branches SHOULD live for the shortest practical period.

---

# 17. Reason

Long-lived branches increase:

```text
MERGE CONFLICTS

INTEGRATION RISK

STALE ASSUMPTIONS

LARGE PRs

DELIVERY DELAY
```

---

# 18. Branch Naming

Branch names SHOULD identify purpose/work item.

Examples:

```text
feature/1639-bulk-order-approval

fix/1610-cart-checkout-error

refactor/order-workflow-validation
```

---

# 19. Sensitive Data in Branch Name

Branch names MUST NOT contain:

```text
PASSWORDS

TOKENS

CUSTOMER PII

CONFIDENTIAL DATA
```

---

# 20. Pull Request Scope

A Pull Request SHOULD represent one coherent change.

---

# 21. Mixed Concern

Avoid combining unrelated:

```text
FEATURE

REFACTOR

DEPENDENCY UPGRADE

DATABASE CHANGE

FORMATTING
```

in one PR unless necessary.

---

# 22. PR Size

Pull Requests SHOULD remain small enough for meaningful review.

---

# 23. Large PR

Large PRs MUST be decomposed where technically practical.

---

# 24. Why Small PRs

Smaller PRs generally improve:

```text
REVIEW QUALITY

REVIEW SPEED

DEFECT DETECTION

ROLLBACK

UNDERSTANDING
```

---

# 25. Line Count

No universal line-count threshold determines correctness.

However, unusually large diffs SHOULD trigger decomposition review.

---

# 26. Generated Code

Generated files SHOULD be identified so they do not distort review size.

---

# 27. Formatting Noise

Unrelated formatting changes SHOULD NOT be mixed with functional changes.

---

# 28. File Movement

Large file moves/renames SHOULD be separated from behavioral changes where practical.

---

# 29. PR Description

Material PRs SHOULD explain:

```text
WHAT CHANGED

WHY

HOW IT WORKS

RISKS

TESTS

MIGRATIONS

API IMPACT

ROLLBACK / ROLL-FORWARD
```

---

# 30. Work Item

PRs SHOULD reference the relevant work item when one exists.

---

# 31. Screenshot

Screenshots MAY support UI changes but MUST NOT replace automated validation.

---

# 32. Backend Evidence

Backend PRs SHOULD provide test/build evidence where useful.

---

# 33. Self Review

Authors MUST review their own diff before requesting review.

---

# 34. Debug Code

Temporary debug code MUST be removed.

---

# 35. Commented Code

Obsolete commented-out implementation SHOULD be removed rather than retained indefinitely.

---

# 36. TODO

New TODO/FIXME comments SHOULD reference a concrete reason/work item when representing deferred required work.

---

# 37. Secret Review

Authors MUST verify that no credentials/secrets are introduced.

---

# 38. Commit

Commits SHOULD represent understandable logical changes.

---

# 39. Commit Message

Commit messages SHOULD explain intent.

---

# 40. Conventional Commits

Conventional Commits SHOULD be adopted where compatible with enterprise tooling.

Examples:

```text
feat(orders): add bulk approval endpoint

fix(cart): preserve remote checkout error

test(customers): increase resilient cache coverage

refactor(workflow): consolidate status validation

docs(architecture): add database migration standard
```

---

# 41. Commit Types

Recommended types include:

```text
feat

fix

refactor

test

docs

build

ci

perf

chore
```

---

# 42. Breaking Change

Breaking changes MUST be clearly identified.

---

# 43. Meaningless Commit

Avoid:

```text
fix

changes

update

test

final

final2
```

as standalone commit messages.

---

# 44. Commit Secret

Sensitive data MUST NOT appear in commit messages.

---

# 45. Commit History

Repository history SHOULD remain useful for:

```text
AUDIT

BISECT

CHANGELOG

INCIDENT ANALYSIS
```

---

# 46. Squash Merge

Squash merge MAY be preferred where the repository wants one coherent commit per PR.

---

# 47. Merge Commit

Merge commits MAY be appropriate where preserving branch history is required.

---

# 48. Rebase

Rebase MAY be used according to team policy before merge.

---

# 49. Shared History Rewrite

Published/shared protected history MUST NOT be rewritten casually.

---

# 50. Merge Strategy

Each repository SHOULD use a consistent merge strategy.

---

# 51. Reviewer Independence

Material changes require review by someone other than the author.

---

# 52. Approval Count

Repositories SHOULD require at least the enterprise-defined minimum number of approvals.

Critical changes MAY require additional reviewers.

---

# 53. CODEOWNERS

CODEOWNERS SHOULD be used for critical areas where supported.

Examples:

```text
/security/

/architecture/

/database/migration/

/authentication/

/payments/
```

---

# 54. Ownership

Code ownership SHOULD reflect technical responsibility rather than organizational prestige.

---

# 55. Self Approval

Authors MUST NOT be the sole approver of their own production change.

---

# 56. Review Scope

Code review MUST evaluate more than syntax.

---

# 57. Functional Review

Reviewers SHOULD verify:

```text
DOES IT SOLVE THE REQUIREMENT?

ARE EDGE CASES COVERED?

ARE BUSINESS RULES CORRECT?
```

---

# 58. Architecture Review

Reviewers SHOULD verify:

```text
CORRECT LAYER?

CORRECT SERVICE?

CORRECT DOMAIN?

NEW COUPLING?

DUPLICATION?

UNNECESSARY COMPLEXITY?
```

---

# 59. Security Review

Reviewers SHOULD inspect:

```text
AUTHENTICATION

AUTHORIZATION

INPUT VALIDATION

SECRETS

LOGGING

SENSITIVE DATA

INJECTION

REMOTE CALLS
```

where applicable.

---

# 60. Performance Review

Material changes SHOULD consider:

```text
N+1

DATABASE QUERIES

REMOTE CALLS

LOOPS

MEMORY

CONCURRENCY

CONNECTION POOLS

CACHE
```

---

# 61. Reliability Review

Reviewers SHOULD inspect:

```text
TIMEOUT

RETRY

CIRCUIT BREAKER

IDEMPOTENCY

TRANSACTION

FAILURE PATH
```

where applicable.

---

# 62. Test Review

Tests MUST themselves be reviewed.

---

# 63. Test Quality

High coverage with weak assertions is not sufficient.

---

# 64. Test Intent

Tests SHOULD communicate business/technical intent.

---

# 65. Java Test Naming

Java tests SHOULD follow established project naming conventions.

Where the project convention requires it, test methods SHOULD use the `test*` pattern consistently.

---

# 66. AssertJ

AssertJ SHOULD be used according to established Java test conventions.

Applicable assertions MUST contain meaningful descriptions:

```java
assertThat(result)
        .as("should return the expected order status")
        .isEqualTo(expectedStatus);
```

---

# 67. Assertion Description

Descriptions MUST explain what failed, not merely repeat the assertion syntax.

---

# 68. Weak Description

Avoid:

```java
.as("result")
```

---

# 69. Better Description

Prefer:

```java
.as("should preserve the workflow status returned by the order")
```

---

# 70. Deterministic Test Data

Tests SHOULD use deterministic fixtures.

---

# 71. Random UUID

Random UUID generation SHOULD be avoided where deterministic constants adequately test the behavior.

---

# 72. Test Constants

Shared stable values SHOULD use appropriate test constants where this improves readability.

---

# 73. Sleep

Tests SHOULD NOT depend on arbitrary:

```java
Thread.sleep(...)
```

for synchronization.

---

# 74. Concurrency Test

Concurrency tests SHOULD use deterministic coordination primitives.

---

# 75. Mockito

Mocks SHOULD verify meaningful interactions without over-specifying irrelevant implementation details.

---

# 76. Mock Everything

Tests SHOULD NOT mock so aggressively that they merely reproduce the implementation.

---

# 77. Integration Test

Important infrastructure behavior SHOULD use integration tests with real compatible infrastructure where practical.

---

# 78. Testcontainers

Testcontainers SHOULD be used for relevant:

```text
POSTGRESQL

REDIS

SQS

OTHER SUPPORTED INFRASTRUCTURE
```

---

# 79. H2

H2 MUST NOT substitute for PostgreSQL-specific behavior where database semantics matter.

---

# 80. Unit Test

Unit tests SHOULD remain fast and isolated.

---

# 81. Integration Test Classification

Repositories SHOULD clearly distinguish unit and integration tests where execution strategy requires it.

---

# 82. Build Gate

Every PR MUST compile successfully.

---

# 83. Clean Build

The CI pipeline SHOULD execute a clean build.

For Gradle projects:

```text
./gradlew clean build
```

or the approved equivalent.

---

# 84. Local Cache Independence

A successful CI build MUST NOT depend on stale developer-local build artifacts.

---

# 85. Warning

Compiler/build warnings SHOULD be reviewed and reduced.

---

# 86. SonarQube

SonarQube quality analysis MUST execute for applicable repositories.

---

# 87. Quality Gate

A failed mandatory SonarQube Quality Gate MUST block normal merge/release.

---

# 88. New Code

Quality policy SHOULD emphasize:

```text
NO NEW CRITICAL ISSUES

NO NEW BLOCKER ISSUES

CONTROLLED CODE SMELLS

CONTROLLED DUPLICATION

ADEQUATE COVERAGE
```

---

# 89. Sonar Suppression

Suppressing a Sonar finding requires technical justification.

---

# 90. `NOSONAR`

`NOSONAR` SHOULD be exceptional.

---

# 91. False Positive

Confirmed false positives MAY be marked according to approved Sonar governance.

---

# 92. Rule Evasion

Code MUST NOT be intentionally rewritten into a more obscure form solely to evade static analysis.

---

# 93. Clean Code

Static-analysis compliance SHOULD improve code clarity, not reduce it.

---

# 94. SAST

SAST MUST execute for production services.

---

# 95. SAST Gate

Unacceptable new security findings MUST block promotion according to security policy.

---

# 96. Critical SAST

Critical exploitable SAST findings MUST NOT be released without explicit approved exception.

---

# 97. SCA

Software Composition Analysis MUST validate dependency vulnerabilities.

---

# 98. Secret Scanning

Repositories and/or pipelines MUST perform secret scanning.

---

# 99. Secret Detection

A discovered real secret MUST be:

```text
REVOKED / ROTATED

REMOVED

INVESTIGATED
```

Removing it from the latest commit alone is insufficient.

---

# 100. Git History Secret

Secrets committed to Git history MUST be treated as compromised.

---

# 101. JaCoCo

Java projects SHOULD use JaCoCo for coverage measurement.

---

# 102. Coverage Baseline

The enterprise Java-service target SHALL be at least:

```text
80%
```

where this baseline applies.

---

# 103. Coverage Meaning

Coverage is a quality signal, not proof of correctness.

---

# 104. New Code Coverage

New or modified code SHOULD meet or exceed the project's required coverage threshold.

---

# 105. Coverage Regression

Changes SHOULD NOT materially reduce overall coverage without justification.

---

# 106. Branch Coverage

Branch coverage SHOULD be considered for decision-heavy business logic.

---

# 107. Artificial Coverage

Tests created only to execute lines without validating behavior are prohibited.

---

# 108. Getter Test

Trivial getter/setter tests SHOULD NOT be created solely to inflate coverage unless contract behavior requires them.

---

# 109. Exception Paths

Meaningful failure paths SHOULD be tested.

---

# 110. Boundary Paths

Boundary conditions SHOULD be tested.

---

# 111. Null Handling

Null/empty behavior SHOULD be tested where permitted by contract.

---

# 112. Mutation Testing

Mutation testing MAY be used for critical business-rule modules.

---

# 113. Mutation Purpose

Mutation testing evaluates whether tests detect behavioral changes rather than merely execute code.

---

# 114. Mutation Scope

Mutation testing SHOULD target high-value logic rather than necessarily every generated/boilerplate class.

---

# 115. Mutation Survivor

Important surviving mutants SHOULD trigger test-quality review.

---

# 116. Flyway

Repositories containing Flyway migrations MUST validate migrations.

---

# 117. Migration Immutability

Previously applied migrations MUST NOT be modified.

Corrections MUST use a new migration version.

---

# 118. Duplicate Version

Duplicate Flyway migration versions MUST fail CI.

---

# 119. Migration Test

Database migrations SHOULD be tested using real PostgreSQL-compatible infrastructure.

---

# 120. Destructive Migration

Destructive migrations require explicit review.

---

# 121. API Contract

REST API changes MUST evaluate contract compatibility.

---

# 122. OpenAPI

OpenAPI documents SHOULD be validated where the API uses OpenAPI.

---

# 123. Breaking API

Breaking API changes MUST NOT be introduced accidentally.

---

# 124. Contract Diff

Automated OpenAPI/contract diff SHOULD be used where practical.

---

# 125. Response Field Removal

Removing or changing response fields requires compatibility analysis.

---

# 126. Request Requirement

Changing an optional request field to required is potentially breaking.

---

# 127. Enum

Removing or changing enum values requires consumer analysis.

---

# 128. HTTP Status

Changing HTTP status semantics can be a breaking contract change.

---

# 129. Message Contract

SQS/event contracts require equivalent compatibility governance.

---

# 130. Event Field

Consumers MUST be considered before removing or changing event fields.

---

# 131. CI Stages

A representative Java-service pipeline SHOULD include:

```text
CHECKOUT

BUILD

UNIT TEST

INTEGRATION TEST

JACOCO

SONAR

SAST

SCA

SECRET SCAN

MIGRATION VALIDATION

CONTRACT VALIDATION

ARTIFACT BUILD
```

as applicable.

---

# 132. Parallel CI

Independent CI stages SHOULD run in parallel when this materially reduces feedback time.

---

# 133. Fail Fast

Fast deterministic checks SHOULD execute early.

---

# 134. Expensive Tests

Expensive validation SHOULD remain mandatory where risk justifies it, even if scheduled later in the pipeline.

---

# 135. CI Timeout

Pipeline jobs MUST have bounded execution time.

---

# 136. Flaky Test

Flaky tests MUST be treated as defects.

---

# 137. Retry Flaky Test

Automatically retrying flaky tests MUST NOT become a permanent substitute for fixing them.

---

# 138. Quarantine

Temporary test quarantine MAY be used only with ownership and remediation tracking.

---

# 139. Disabled Test

Disabled tests require justification.

---

# 140. Ignored Test Debt

Permanently ignored tests SHOULD be removed or repaired.

---

# 141. Pipeline Failure

Pipeline failures MUST be investigated rather than repeatedly rerun until they randomly pass.

---

# 142. CI Reproducibility

CI MUST use controlled:

```text
JDK

GRADLE

DEPENDENCIES

CONTAINER IMAGE

ENVIRONMENT
```

---

# 143. Artifact Once

A production artifact SHOULD be built once and promoted across environments.

---

# 144. Rebuild Per Environment

Rebuilding different binaries for DEV/HML/PRD SHOULD be avoided.

---

# 145. Configuration

Environment differences SHOULD be externalized through configuration.

---

# 146. Artifact Identity

Artifacts MUST have immutable identities.

---

# 147. Container Digest

Container deployments SHOULD support image-digest traceability.

---

# 148. Build Metadata

Release metadata SHOULD identify:

```text
VERSION

COMMIT

BUILD

TIMESTAMP

PIPELINE
```

where appropriate.

---

# 149. Release Tag

Production releases SHOULD have immutable source-control tags or equivalent traceability.

---

# 150. Tag Reuse

A released tag MUST NOT be moved to different source code.

---

# 151. Semantic Versioning

Semantic Versioning MAY be used where compatible with release strategy.

---

# 152. Changelog

Release notes/changelog SHOULD be generated or maintained for material releases.

---

# 153. Conventional Commit Automation

Conventional Commits MAY support automated:

```text
CHANGELOG

VERSIONING

RELEASE NOTES
```

---

# 154. Deployment Gate

Passing PR CI does not automatically imply unrestricted production deployment.

---

# 155. Environment Promotion

Promotion SHOULD follow enterprise deployment controls.

---

# 156. DEV

DEV deployment SHOULD provide rapid integration feedback.

---

# 157. HML / QA

Higher environments SHOULD validate representative integrations and deployment behavior.

---

# 158. Production

Production promotion MUST use approved artifacts and configuration.

---

# 159. Manual Artifact Modification

Artifacts MUST NOT be manually modified between environments.

---

# 160. Deployment Strategy

Services SHOULD use an appropriate safe deployment strategy such as:

```text
ROLLING

CANARY

BLUE/GREEN
```

according to platform capability and risk.

---

# 161. Readiness

New instances MUST not receive production traffic before readiness conditions pass.

---

# 162. Deployment Verification

Post-deployment validation SHOULD verify:

```text
HEALTH

ERROR RATE

LATENCY

DEPENDENCIES

BUSINESS SMOKE TEST
```

where applicable.

---

# 163. Rollback

Application rollback procedures MUST be defined.

---

# 164. Database Compatibility

Rollback MUST account for database migration compatibility.

---

# 165. Flyway Rollback

An application rollback MUST NOT assume an already-applied database migration can simply be edited or removed.

---

# 166. Roll-Forward

Database correction SHOULD generally use a new migration and roll forward.

---

# 167. Feature Flag

Feature flags MAY decouple deployment from feature activation.

---

# 168. Flag Purpose

Feature flags MAY reduce deployment risk for material behavioral changes.

---

# 169. Flag Debt

Temporary feature flags MUST have cleanup ownership.

---

# 170. Emergency Change

Emergency production changes MAY use an accelerated process.

---

# 171. Emergency Does Not Mean Uncontrolled

Emergency changes still require:

```text
TRACEABILITY

TESTING PROPORTIONAL TO RISK

REVIEW WHEN POSSIBLE

POST-CHANGE REVIEW
```

---

# 172. Break Glass

Break-glass access MUST be:

```text
RESTRICTED

AUDITED

TIME-BOUND
```

---

# 173. Hotfix

Hotfixes MUST be merged back into the normal development lineage.

---

# 174. Production-Only Fix

A production-only code branch that never returns to the main development line is prohibited.

---

# 175. Revert

A revert SHOULD be preferred over manually recreating previous source state when reverting a Git change.

---

# 176. Revert Traceability

Revert commits SHOULD reference the reverted change.

---

# 177. Dependency Update

Dependency upgrades MUST pass the same relevant quality gates as functional changes.

---

# 178. Documentation Change

Documentation-only changes MAY use a reduced pipeline where technically safe.

---

# 179. Pipeline Optimization

Quality gates MAY be conditionally skipped only when the change provably cannot affect the corresponding artifact/risk.

---

# 180. Skip Governance

Skip conditions MUST be encoded and reviewed, not decided arbitrarily by individual developers.

---

# 181. Architecture Decision

Changes violating an accepted ADR require:

```text
COMPLIANCE

OR

NEW/SUPERSEDING ADR
```

rather than silent architectural divergence.

---

# 182. Architecture Review Trigger

Examples requiring architectural review MAY include:

```text
NEW DATABASE

NEW MESSAGE BROKER

NEW FRAMEWORK

NEW SECURITY MODEL

NEW SERVICE

CROSS-SERVICE DATA OWNERSHIP CHANGE

NEW EXTERNAL INTEGRATION
```

---

# 183. Technical Debt

Technical debt introduced intentionally MUST be explicit.

---

# 184. Debt Owner

Significant accepted debt SHOULD have:

```text
OWNER

RATIONALE

FOLLOW-UP
```

---

# 185. Quality Gate Exception

Quality-gate exceptions MUST be:

```text
EXPLICIT

APPROVED

TIME-BOUND

AUDITABLE
```

---

# 186. Permanent Exception

Permanent exceptions SHOULD be extremely rare and architecturally justified.

---

# 187. Definition of Done

A development task is not complete solely because implementation has finished.

---

# 188. Functional DoD

Applicable requirements MUST be implemented and verified.

---

# 189. Code DoD

Code MUST be:

```text
READABLE

MAINTAINABLE

CONSISTENT

REVIEWED
```

---

# 190. Test DoD

New/modified behavior MUST have appropriate automated tests.

---

# 191. Coverage DoD

Coverage MUST meet applicable project/enterprise thresholds.

For applicable Java services:

```text
JaCoCo >= 80%
```

---

# 192. Sonar DoD

Mandatory Sonar Quality Gate MUST pass.

---

# 193. Security DoD

Applicable:

```text
SAST

SCA

SECRET SCAN
```

gates MUST pass.

---

# 194. Migration DoD

Database changes MUST:

```text
USE NEW FLYWAY MIGRATION

PRESERVE APPLIED MIGRATIONS

PASS VALIDATION

SUPPORT DEPLOYMENT COMPATIBILITY
```

---

# 195. API DoD

API changes MUST preserve compatibility or explicitly implement an approved migration/versioning strategy.

---

# 196. Documentation DoD

Material operational/API/architecture changes MUST update corresponding documentation.

---

# 197. Observability DoD

New critical paths SHOULD include appropriate:

```text
LOGS

METRICS

TRACES
```

according to observability standards.

---

# 198. Error DoD

Failure behavior MUST be intentional and tested.

---

# 199. Performance DoD

Material performance-sensitive changes MUST demonstrate no unacceptable regression.

---

# 200. Dependency DoD

New dependencies MUST satisfy dependency/supply-chain governance.

---

# 201. Deployment DoD

The change MUST be deployable through the standard pipeline.

---

# 202. Rollback DoD

Rollback/roll-forward implications MUST be understood for high-risk changes.

---

# 203. No Local-Only Success

"Works on my machine" is not completion evidence.

---

# 204. PR Review Checklist

```text
[ ] Does the implementation satisfy the requirement?

[ ] Is the PR focused on one coherent change?

[ ] Is the diff small enough to review effectively?

[ ] Was unrelated formatting avoided?

[ ] Is the code readable?

[ ] Is duplicated logic avoided?

[ ] Are abstractions justified?

[ ] Are business rules in the correct layer?

[ ] Are exceptions handled correctly?

[ ] Are logs safe and useful?

[ ] Are secrets absent?

[ ] Is input validation adequate?

[ ] Is authorization correct?

[ ] Are remote calls bounded?

[ ] Are transactions correct?

[ ] Are database queries efficient?

[ ] Is N+1 avoided?

[ ] Is concurrency bounded?

[ ] Are new dependencies necessary?

[ ] Are tests meaningful?

[ ] Are failure paths tested?

[ ] Are assertions descriptive?

[ ] Is test data deterministic?

[ ] Is Thread.sleep avoided?

[ ] Does coverage meet policy?

[ ] Does Sonar pass?

[ ] Does SAST pass?

[ ] Does SCA pass?

[ ] Does secret scanning pass?

[ ] Are Flyway migrations immutable?

[ ] Is a new migration version used?

[ ] Is API compatibility preserved?

[ ] Is documentation updated?

[ ] Can the change be safely deployed?

[ ] Is rollback/roll-forward understood?
```

---

# 205. CI/CD Fitness Functions

Stable controls SHOULD be automated where practical.

```text
[ ] Protected branch requires PR

[ ] Author cannot self-approve alone

[ ] Required CI checks cannot be bypassed normally

[ ] Build executes with Gradle Wrapper

[ ] Java version is controlled

[ ] Unit tests pass

[ ] Integration tests pass

[ ] JaCoCo threshold passes

[ ] Sonar Quality Gate passes

[ ] SAST passes

[ ] SCA passes

[ ] Secret scanning passes

[ ] Duplicate Flyway versions fail

[ ] Applied Flyway migrations remain immutable

[ ] OpenAPI breaking changes are detected where supported

[ ] Artifact identity includes source traceability

[ ] Production release tag is immutable

[ ] Deployment uses approved artifact

[ ] Failed tests cannot be silently ignored
```

---

# 206. Enterprise Pull Request Gate

A Pull Request is not considered compliant when applicable conditions include:

```text
[ ] Direct protected-branch modification bypasses normal review

[ ] Author is sole approver

[ ] Mandatory CI failed

[ ] Tests fail

[ ] Sonar Quality Gate fails

[ ] Unacceptable SAST finding exists

[ ] Unacceptable dependency vulnerability exists

[ ] Secret scanning detected a credential

[ ] Coverage is below required threshold

[ ] Tests exist only to inflate coverage

[ ] Existing applied Flyway migration was modified

[ ] API breaking change is undocumented

[ ] New dependency is unjustified

[ ] Debug code remains

[ ] Sensitive data is logged

[ ] PR mixes multiple unrelated large changes

[ ] Critical review comments remain unresolved

[ ] Production artifact cannot be traced to source

[ ] Quality-gate exception has no approval/expiration
```

---

# 207. Anti-Patterns

The following are prohibited or strongly discouraged:

- direct push to protected production branches
- self-approved production changes
- giant multi-concern PRs
- meaningless commit messages
- secrets in commits
- unresolved critical review comments
- merging with failed tests
- rerunning flaky pipelines until green
- fake coverage tests
- Sonar suppression without justification
- disabling SAST to release
- permanent SCA suppression
- editing applied Flyway migrations
- undocumented breaking API changes
- rebuilding different production artifacts per environment
- mutable release tags
- manual artifact modification
- uncontrolled break-glass changes
- production-only hotfix branches
- "works on my machine" as acceptance evidence

---

# 208. Positive Consequences

The decision provides:

- higher review quality
- improved defect detection
- stronger security gates
- consistent test quality
- controlled coverage
- improved migration safety
- API compatibility protection
- reproducible builds
- auditable delivery
- safer production deployment
- improved incident traceability
- clearer Definition of Done

---

# 209. Negative Consequences

The decision introduces:

- additional CI execution
- mandatory review time
- security-analysis overhead
- coverage maintenance
- contract-validation work
- migration checks
- stricter merge controls

These costs are accepted because preventing production defects is cheaper than diagnosing uncontrolled changes after deployment.

---

# 210. Neutral Consequences

The decision also means:

- not every PR requires the same reviewers
- not every documentation change requires the full runtime test suite
- 80% coverage does not guarantee correctness
- 100% coverage is not automatically desirable
- a green pipeline does not eliminate the need for review
- human review does not eliminate the need for automation
- emergency processes can be faster without becoming uncontrolled

---

# 211. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Defect merged | High | Medium | Review + tests |
| Security vulnerability | Critical | Medium | SAST/SCA |
| Secret exposure | Critical | Low/Medium | Secret scanning |
| Coverage gaming | Medium | Medium | Test-quality review |
| Flaky pipeline | High | Medium | Flaky-test remediation |
| Migration regression | Critical | Medium | Flyway gates |
| API breakage | High | Medium | Contract validation |
| Review bottleneck | Medium | Medium | Small PRs/CODEOWNERS |
| CI duration | Medium | Medium | Parallelization |
| Emergency bypass abuse | Critical | Low | Audited break-glass |

---

# 212. Implementation Guidance

The following rules are mandatory:

1. Protected branches must use controlled PR-based integration.
2. Direct push/force push to production branches should be prohibited.
3. Feature branches should remain short-lived.
4. PRs should contain one coherent change.
5. Large PRs should be decomposed where practical.
6. Authors must self-review before requesting review.
7. Material production changes require independent approval.
8. CODEOWNERS should protect critical technical areas.
9. Commit messages should communicate intent and follow repository conventions.
10. Mandatory CI checks must pass before normal merge.
11. Java builds must use the Gradle Wrapper and controlled JDK.
12. Unit and applicable integration tests must pass.
13. Tests must verify behavior rather than merely execute code.
14. Applicable AssertJ assertions must use meaningful `.as("...")` descriptions.
15. Deterministic test data should be preferred.
16. Arbitrary `Thread.sleep` synchronization in tests should be avoided.
17. Applicable Java services must maintain the required JaCoCo baseline, normally at least 80%.
18. SonarQube mandatory Quality Gates must pass.
19. SAST, SCA and secret-scanning gates must pass according to policy.
20. Security/static-analysis suppressions require explicit justification.
21. Applied Flyway migrations are immutable; corrections require new migration versions.
22. API/event changes must evaluate backward compatibility.
23. CI should validate contracts automatically where practical.
24. Production artifacts should be built once and promoted.
25. Release artifacts/tags/images must have immutable traceable identities.
26. Production deployment must use approved artifacts.
27. Emergency changes must remain auditable.
28. Hotfixes must return to the normal source lineage.
29. Quality-gate exceptions must be explicit, approved and time-bound.
30. Definition of Done includes code, tests, quality, security, compatibility, documentation and deployment readiness.

---

# 213. Validation

This ADR will be validated through:

- Git
- protected branches
- Pull Requests
- CODEOWNERS
- Conventional Commits
- Java 21
- Gradle Wrapper
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- JaCoCo
- SonarQube
- SAST
- SCA
- secret scanning
- Flyway
- PostgreSQL
- OpenAPI validation
- contract testing
- artifact repositories
- container registries
- CI/CD quality gates
- deployment verification

---

# 214. Success Criteria

The decision is successful when:

- production changes cannot normally bypass review
- PRs become smaller and easier to understand
- failed quality gates reliably prevent unsafe merges
- new code maintains required test coverage
- tests provide useful failure diagnostics
- security findings are detected before production
- secrets cannot silently enter source control
- applied migrations remain immutable
- breaking API changes become deliberate rather than accidental
- releases are traceable to source and build
- emergency changes remain auditable
- teams share a concrete Definition of Done

---

# 215. Alternatives Rejected

## 215.1 Direct Push to Main

Rejected because it bypasses independent review and automated merge gates.

---

## 215.2 Review Without CI

Rejected because human reviewers should not manually reproduce deterministic automated checks.

---

## 215.3 CI Without Human Review

Rejected because automated tools cannot fully evaluate architecture, business semantics and maintainability.

---

## 215.4 Coverage as Sole Quality Metric

Rejected because code can achieve high execution coverage with ineffective assertions.

---

## 215.5 100% Coverage Everywhere

Rejected as a universal policy because it can incentivize low-value tests and does not guarantee correctness.

---

## 215.6 Skip Security Gates for Internal Services

Rejected because internal services remain part of the attack and supply-chain surface.

---

## 215.7 Modify Failed Flyway Migration

Rejected because applied migration history is immutable.

---

## 215.8 Rebuild for Every Environment

Rejected because DEV/HML/PRD could receive different binaries from the same nominal release.

---

# 216. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-083: Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard
- ADR-084: Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard
- ADR-085: Enterprise Dependency Management, Gradle, SBOM, Supply Chain Security and Vulnerability Governance Standard

---

# 217. References

- Git Documentation
- Conventional Commits Specification
- GitHub/GitLab Branch Protection Documentation
- CODEOWNERS Documentation
- Gradle User Manual
- JUnit 5 Documentation
- AssertJ Documentation
- Mockito Documentation
- JaCoCo Documentation
- SonarQube Documentation
- OWASP ASVS
- OWASP SAMM
- OWASP Software Component Verification Standard
- CycloneDX
- Flyway Documentation
- OpenAPI Specification
- Testcontainers Documentation
- NIST Secure Software Development Framework
- SLSA Supply-chain Levels for Software Artifacts
- Google Engineering Practices — Code Review

---

# 218. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | AstraForge Supply Platform Architecture Team | Approved | Initial enterprise PR, CI/CD quality gate and Definition of Done baseline |

---

# 219. Decision Summary

Development flow becomes:

```text
WORK ITEM
    |
    v
SHORT-LIVED BRANCH
    |
    v
IMPLEMENTATION
    |
    v
LOCAL TEST
    |
    v
PULL REQUEST
    |
    +--> SELF REVIEW
    |
    +--> AUTOMATED CI
    |
    +--> HUMAN REVIEW
    |
    v
MERGE
```

Quality validation becomes:

```text
CODE
 |
 +--> COMPILE
 |
 +--> UNIT TEST
 |
 +--> INTEGRATION TEST
 |
 +--> JACOCO >= 80%
 |
 +--> SONAR
 |
 +--> SAST
 |
 +--> SCA
 |
 +--> SECRET SCAN
 |
 +--> FLYWAY VALIDATION
 |
 +--> CONTRACT VALIDATION
 |
 v
MERGEABLE
```

Test quality becomes:

```text
TEST
 |
 +--> DETERMINISTIC DATA
 |
 +--> CLEAR ARRANGE / ACT / ASSERT
 |
 +--> MEANINGFUL ASSERTION
 |
 +--> AssertJ .as("...")
 |
 +--> SUCCESS PATH
 |
 +--> FAILURE PATH
 |
 +--> BOUNDARY CONDITIONS
 |
 v
BEHAVIORAL CONFIDENCE
```

Flyway review becomes:

```text
DATABASE CHANGE
      |
      v
EXISTING MIGRATION?
      |
      +--> NEVER MODIFY
      |
      v
NEW MIGRATION VERSION
      |
      v
VALIDATE
      |
      v
TEST UPGRADE
```

Artifact promotion becomes:

```text
SOURCE COMMIT
     |
     v
CI BUILD
     |
     v
IMMUTABLE ARTIFACT
     |
     +--> DEV
     |
     +--> HML
     |
     +--> PRD
```

rather than:

```text
SOURCE
 |
 +--> BUILD FOR DEV
 |
 +--> BUILD AGAIN FOR HML
 |
 +--> BUILD AGAIN FOR PRD
```

Definition of Done becomes:

```text
FUNCTIONAL REQUIREMENT
        +
CLEAN IMPLEMENTATION
        +
CODE REVIEW
        +
UNIT TESTS
        +
INTEGRATION TESTS
        +
COVERAGE
        +
SONAR
        +
SAST
        +
SCA
        +
SECRET SCAN
        +
DATABASE SAFETY
        +
CONTRACT COMPATIBILITY
        +
DOCUMENTATION
        +
OBSERVABILITY
        +
DEPLOYMENT READINESS
        =
DONE
```

The governing principle is:

```text
Do not merge
because the code compiles.

Compile it.

Test it.

Review it.

Measure coverage.

But do not game coverage.

A line executed
is not necessarily
a behavior verified.

Write assertions
that explain failures.

Keep tests deterministic.

Do not sleep
and hope concurrency works.

Run Sonar.

Fix the code,
not merely the warning.

Run SAST.

Run SCA.

Scan for secrets.

Never commit credentials.

If a secret enters Git,
assume it is compromised.

Protect your branches.

Do not self-approve
your own production change.

Keep Pull Requests focused.

Small changes
are easier to understand,
review,
test,
deploy,
and revert.

Treat Flyway history
as immutable.

Never change
an applied migration.

Create the next version.

Protect API contracts.

Consumers may deploy
at different times.

Build the artifact once.

Promote the same artifact.

Know exactly
which commit
is running in production.

Keep emergency changes
fast but controlled.

And remember:

Done does not mean
"I finished coding."

Done means:

the requirement works,

the code is maintainable,

the tests prove behavior,

the quality gates pass,

the security gates pass,

the database is safe,

the contracts remain valid,

the artifact is reproducible,

the deployment is understood,

and another engineer
can confidently review,
operate,
and evolve
what you delivered.
```
