# ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-042 |
| Title | Adopt Architecture Fitness Functions and Automated Governance Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Architecture Fitness Functions, ArchUnit, CI/CD, Static Analysis, Automated Governance |
| Related Work Items | Clean Architecture, ArchUnit, Flyway, Testing Standards, API Governance, Quality Gates |
| Supersedes | None |
| Superseded By | ADR-051 |

---

# 1. Context

The AstraForge Supply Platform has established architectural standards covering:

- Clean Architecture
- Domain-Driven Design
- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Kafka
- Redis
- REST APIs
- concurrency
- security
- observability
- testing
- supply-chain security
- CI/CD
- production reliability
- technical debt

Documented standards provide direction, but documentation alone cannot reliably prevent architecture drift.

A common failure mode is:

```text
Architecture Rule
       |
       v
Documented
       |
       v
Developers Understand It
       |
       v
Months Pass
       |
       v
New Code Violates Rule
       |
       v
Review Misses It
       |
       v
Violation Becomes Precedent
```

The platform therefore requires executable architectural constraints.

---

# 2. Problem Statement

The platform requires standards defining automated verification for:

- Clean Architecture boundaries
- package dependency direction
- circular dependencies
- controller responsibilities
- service responsibilities
- repository boundaries
- persistence isolation
- domain isolation
- naming conventions
- package conventions
- dependency restrictions
- testing conventions
- Flyway migration governance
- API compatibility
- OpenAPI validation
- event-contract compatibility
- dependency governance
- quality gates
- architectural exceptions
- CI enforcement

---

# 3. Decision Drivers

Primary drivers are:

1. prevention of architecture drift
2. early feedback
3. consistent implementation
4. scalable governance
5. reduced manual-review burden
6. refactoring confidence
7. explicit architecture constraints
8. CI/CD enforcement
9. maintainability
10. reduced technical debt
11. cross-service consistency
12. auditable governance

---

# 4. Decision

The AstraForge Supply Platform adopts architecture fitness functions as automated checks that continuously verify important architectural properties.

The canonical model is:

```text
ARCHITECTURE DECISION
        |
        v
ARCHITECTURAL RULE
        |
        v
CAN IT BE AUTOMATED?
       / \
     YES  NO
      |    |
      v    v
FITNESS   REVIEW /
FUNCTION  DOCUMENTATION
      |
      v
CI/CD
      |
      v
PASS / FAIL
```

Architecture constraints that are deterministic and mechanically verifiable should be implemented as automated checks.

---

# 5. Fundamental Principle

The primary rule is:

```text
If an architectural rule is important,
stable,
and mechanically verifiable,
prefer enforcing it automatically.
```

---

# 6. Fitness Function Definition

An architecture fitness function is an automated mechanism that measures or verifies an architectural characteristic.

Examples:

```text
No package cycles

Domain does not depend on infrastructure

Controllers do not access repositories directly

Flyway migrations are uniquely versioned

Public API contracts remain compatible

Coverage remains above quality gate
```

---

# 7. Types of Fitness Functions

The platform recognizes several categories:

```text
STRUCTURAL

QUALITY

SECURITY

DATA

API

MESSAGING

DEPENDENCY

OPERATIONAL
```

---

# 8. Structural Fitness Functions

Structural checks validate code organization and dependency direction.

---

# 9. ArchUnit

ArchUnit is the preferred Java tool for package/class architecture rules.

---

# 10. Architecture Test Location

Architecture tests should reside in the normal test source set or an explicitly defined architecture-test source set.

Example:

```text
src/test/java/com/company/orders/architecture
```

---

# 11. Architecture Test Naming

Architecture-test classes should use clear names such as:

```text
ArchitectureTest

CleanArchitectureTest

PackageDependencyTest

LayerDependencyTest
```

---

# 12. Architecture Tests Are Tests

Architecture rules run as part of automated build verification.

A failing architecture test should fail CI unless an explicit approved exception exists.

---

# 13. Imported Classes

ArchUnit should inspect production classes belonging to the service's controlled namespaces.

---

# 14. External Libraries

Architecture tests should not attempt to enforce internal package rules on external dependencies.

---

# 15. Clean Architecture Boundary

The platform should verify dependency direction.

Conceptually:

```text
DOMAIN
   ^
   |
APPLICATION
   ^
   |
ADAPTERS / INFRASTRUCTURE
```

Dependencies point inward.

---

# 16. Domain Independence

The domain layer must not depend directly on:

- controllers
- web infrastructure
- persistence adapters
- messaging adapters
- Spring MVC
- JPA implementation classes

where the service's Clean Architecture model separates them.

---

# 17. Example Rule

Conceptually:

```java
noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
                "..controller..",
                "..repository.impl..",
                "..messaging..",
                "..webclient.."
        );
```

Exact package patterns should reflect the service structure.

---

# 18. Application Layer

Application/use-case classes may depend on:

- domain
- ports
- application DTOs

but should not depend directly on infrastructure implementations.

---

# 19. Port Direction

Application code should depend on abstractions such as:

```text
CustomerPort

OrderRepository

AuditPort
```

rather than:

```text
WebClientCustomersClient

JpaOrderRepositoryImpl

AwsSqsPublisher
```

---

# 20. Infrastructure

Infrastructure implements outward-facing ports.

---

# 21. Adapter Dependency

Infrastructure may depend inward on:

- domain
- application ports

because it implements those abstractions.

---

# 22. Controller Boundary

Controllers should depend on application/use-case services.

---

# 23. Controller-to-Repository

The following dependency should be automatically prohibited where the architecture defines a service/use-case layer:

```text
Controller
     |
     X
     |
 Repository
```

---

# 24. Why

Direct controller-to-repository access bypasses:

- application orchestration
- authorization
- domain rules
- transactional boundaries
- reusable use cases

---

# 25. Controller-to-Entity

Controllers should not expose or depend on persistence entities as transport contracts.

---

# 26. Repository Boundary

Repository interfaces belonging to the application/domain abstraction should remain independent of JPA implementation details where the architecture separates those concerns.

---

# 27. Repository Implementation

Persistence implementations may depend on:

```text
Spring Data JPA

Hibernate

JDBC

PostgreSQL-specific infrastructure
```

---

# 28. Domain Annotation Policy

If the architecture requires a framework-independent domain model, architecture tests should prevent infrastructure annotations in domain packages.

---

# 29. Practical Exceptions

Framework annotations intentionally accepted in the domain model must be documented rather than hidden through broad exclusions.

---

# 30. Circular Dependencies

Package cycles are prohibited.

---

# 31. Cycle Detection

ArchUnit or equivalent tooling should execute package-cycle analysis.

Conceptually:

```text
slices()
    .matching("com.company.orders.(*)..")
    .should().beFreeOfCycles();
```

---

# 32. Cycle Scope

Cycle rules should be scoped intelligently.

A package pattern too broad may create noise.

A pattern too narrow may miss real architecture cycles.

---

# 33. Spring Bean Cycles

Where detectable through tests/static analysis, Spring bean circular dependencies should also be prohibited.

---

# 34. Dependency Between Bounded Contexts

Dependencies across bounded contexts should follow the documented context map.

---

# 35. Context Rule

One bounded context must not access another context's internal persistence implementation directly.

---

# 36. Public Context API

Cross-context collaboration should use the intended:

- application API
- port
- event
- integration boundary

---

# 37. Internal Package

Java package visibility and conventions may be used to reduce accidental access to internals.

---

# 38. Dependency Package Rules

Architecture tests may enforce rules such as:

```text
..orders.. cannot depend on ..billing.internal..

..domain.. cannot depend on ..infrastructure..

..controller.. cannot depend on ..entity..
```

---

# 39. Class Naming

Stable naming conventions may be automated.

Examples:

```text
*Controller

*Service

*Repository

*Mapper

*Request

*Response

*Event
```

---

# 40. Naming Rule Purpose

Naming rules should improve discoverability.

They must not become cosmetic governance with no architectural value.

---

# 41. Controller Naming

Classes annotated as REST controllers should follow the controller naming convention.

---

# 42. Repository Naming

Repository abstractions/implementations should follow consistent naming patterns.

---

# 43. DTO Naming

Transport DTO names should identify direction or purpose where ambiguity exists.

Examples:

```text
OrderCreateRequest

OrderResponse

OrderHeaderResponse
```

---

# 44. Event Naming

Integration events should follow the event-governance naming standard.

---

# 45. Package Naming

Package names should use lowercase and communicate responsibility.

---

# 46. Generic Package Smells

Architecture review should discourage large dumping-ground packages such as:

```text
util

common

misc

helper
```

when more meaningful ownership exists.

---

# 47. Automated Package Rule

Automation may detect forbidden generic packages if the organization adopts that constraint consistently.

---

# 48. Dependency Restrictions

Certain dependencies should be restricted to specific architectural layers.

---

# 49. Spring MVC

Spring MVC controller APIs should normally appear only in web/controller adapters.

---

# 50. JPA

JPA/Spring Data infrastructure should remain within persistence-related packages according to the service architecture.

---

# 51. Kafka

Kafka-specific infrastructure should remain within messaging adapters rather than leaking through domain models.

---

# 52. AWS SDK

AWS SDK types should normally remain inside AWS/infrastructure adapters.

---

# 53. WebClient

WebClient/RestClient types should remain inside integration adapters.

---

# 54. DTO Leakage

Infrastructure-specific DTOs should not leak into domain APIs.

---

# 55. Annotation Restrictions

Architecture tests may inspect annotations to enforce boundaries.

Examples:

```text
@Entity only in persistence model packages

@RestController only in controller packages

@Repository only in persistence/infrastructure packages
```

according to project design.

---

# 56. Service Annotation

Do not assume every application/domain service must carry Spring `@Service`.

Architecture tests must reflect actual architectural policy rather than impose unnecessary framework coupling.

---

# 57. Public API Surface

Classes should expose the smallest necessary visibility.

---

# 58. Public Class Growth

Architecture governance may review excessive public API surface in internal modules.

---

# 59. Dependency Count

Classes with excessive constructor dependencies should be detected by static analysis or custom fitness functions where useful.

---

# 60. Threshold

A dependency-count threshold is a review signal, not an automatic proof of bad design.

---

# 61. Example Policy

A project may define:

```text
> 15 constructor dependencies
    -> warning/review

> 20
    -> architecture review
```

if that threshold proves useful.

---

# 62. Do Not Game Threshold

Do not create artificial facades merely to reduce the visible constructor count.

---

# 63. Method Complexity

SonarQube cognitive complexity remains a quality fitness function.

---

# 64. Complexity Failure

Complexity findings should trigger cohesive refactoring.

---

# 65. Mechanical Extraction

Creating meaningless helper methods solely to reduce the numeric score is discouraged.

---

# 66. Duplication

Sonar duplication detection provides an automated signal.

---

# 67. Duplication Review

Duplicated business rules are more important than incidental syntactic duplication.

---

# 68. Test Fitness Functions

Testing conventions should be automated where feasible.

---

# 69. Test Naming

A custom static rule may verify project test naming conventions if this provides sufficient value.

---

# 70. AssertJ

Tests should use AssertJ according to ADR-035.

---

# 71. AssertJ `.as(...)`

The platform requires meaningful AssertJ `.as("...")` descriptions before assertion predicates according to project testing conventions.

---

# 72. Static Enforcement

Where reliable tooling can enforce missing assertion descriptions without excessive false positives, the rule should be automated.

---

# 73. Quality Review Fallback

When static enforcement is impractical, code review must preserve the convention.

---

# 74. No Arbitrary Sleeps

Tests should not contain arbitrary `Thread.sleep(...)` for synchronization.

---

# 75. Automated Sleep Rule

Architecture/static checks may fail tests containing direct `Thread.sleep` except in explicitly approved test infrastructure.

---

# 76. Why

Sleep-based tests are:

- slow
- timing-sensitive
- CI-sensitive
- often flaky

---

# 77. Random UUID Rule

Unnecessary `UUID.randomUUID()` in deterministic unit tests should be avoided.

---

# 78. Static Rule Scope

This should not prohibit legitimate random/property-based tests.

The rule, if automated, must distinguish intentional randomized testing from ordinary fixtures.

---

# 79. TestConstants

Reusable stable identifiers should be centralized when this improves consistency.

---

# 80. Disabled Tests

CI should detect and surface:

```text
@Disabled
```

tests.

---

# 81. Disabled-Test Policy

Long-lived disabled tests require explicit justification.

---

# 82. Coverage Fitness Function

JaCoCo remains an automated quality fitness function.

---

# 83. Baseline

The service should maintain the agreed coverage baseline.

Example:

```text
>= 80%
```

unless a different approved quality threshold applies.

---

# 84. Coverage on New Code

Coverage of new/changed code should be measured separately where tooling permits.

---

# 85. Coverage Is Not Architecture

Coverage is only one signal and must not replace behavior-quality review.

---

# 86. SonarQube Fitness Function

The Sonar quality gate is mandatory according to project standards.

---

# 87. Sonar Categories

Automated gates may cover:

- bugs
- vulnerabilities
- code smells
- duplication
- maintainability
- coverage

---

# 88. SAST Fitness Function

SAST is a mandatory security fitness function.

---

# 89. SAST Semantics

Automation detects potential risks.

Human review determines appropriate remediation when context matters.

---

# 90. SCA Fitness Function

Dependency/security scanning follows ADR-038.

---

# 91. Dependency Graph

CI should detect unexpected dependency changes.

---

# 92. Dynamic Dependency Version

Automated build checks should prohibit dynamic versions such as:

```text
1.+

latest.release
```

---

# 93. Snapshot Dependency

Production builds should automatically detect prohibited snapshot dependencies.

---

# 94. Repository Governance

Build configuration should prevent unauthorized dependency repositories where possible.

---

# 95. Dependency Locking

When dependency locking is adopted, CI should verify lock consistency.

---

# 96. Gradle Verification

Gradle dependency verification features may be used to strengthen artifact-integrity controls.

---

# 97. Flyway Fitness Functions

Migration governance must be automated wherever practical.

---

# 98. Critical Flyway Rule

Applied migration history is immutable.

---

# 99. Git Cannot Know Every Environment

Source-level CI cannot always know whether a migration has been executed in every environment.

Therefore multiple controls are required.

---

# 100. Migration History Validation

Flyway validation must run against managed/shared environments or controlled test databases as appropriate.

---

# 101. Checksum Validation

Flyway checksum mismatch must fail deployment unless an explicitly reviewed recovery procedure is being executed.

---

# 102. No Silent Repair

CI/CD must not automatically execute:

```text
flyway repair
```

to hide checksum mismatches.

---

# 103. Migration Version Uniqueness

CI must detect duplicate Flyway versions.

---

# 104. Example

This must fail:

```text
V27__create_order_index.sql

V27__add_order_column.sql
```

---

# 105. Migration Naming

Migration files should follow the approved naming convention.

---

# 106. Version Ordering

Migration versions must be deterministically ordered.

---

# 107. Applied Migration Modification

Repository/pipeline tooling should detect changed historical migration files where a baseline/reference is available.

---

# 108. Migration Review Signal

Any modification under:

```text
db/migration
```

to an existing versioned migration should trigger heightened review.

---

# 109. Correct Database Fix

Automated review guidance should reinforce:

```text
Existing V27 wrong?

Create V28.
```

---

# 110. Migration Test

A clean database must successfully apply the complete Flyway history.

---

# 111. PostgreSQL Test

Migration verification should run against the supported PostgreSQL technology rather than relying solely on H2.

---

# 112. Migration Idempotence

Versioned Flyway migrations do not need to be arbitrarily rerunnable.

Their execution semantics are governed by Flyway history.

---

# 113. DDL Review

Static automation cannot determine all production DDL risk.

Large-table operations still require human/operational review.

---

# 114. API Fitness Functions

Published APIs require automated contract verification.

---

# 115. OpenAPI Generation/Validation

OpenAPI must remain synchronized with runtime contracts.

---

# 116. OpenAPI CI

CI should validate:

- specification syntax
- schema validity
- operation consistency

where tooling permits.

---

# 117. OpenAPI Diff

Published API changes should be compared to the previous accepted contract.

---

# 118. Breaking Change

CI should flag potential breaking changes such as:

```text
Path Removal

Method Removal

Required Field Addition

Response Field Removal

Type Change

Enum Restriction
```

---

# 119. Human Review

Automated compatibility tools are conservative and may require human assessment.

---

# 120. Contract Baseline

The comparison baseline must represent the currently supported published contract.

---

# 121. Generated Documentation

Documentation-generation failure should not silently hide an incompatible API.

---

# 122. Sort Allowlist

Static/functional tests should validate API sort fields against an explicit mapping rather than arbitrary JPA paths.

---

# 123. Pagination Limits

API tests should verify configured maximum page sizes.

---

# 124. Error Contract

Contract tests should verify stable error-code/status behavior.

---

# 125. Event Fitness Functions

Kafka/event contracts require automated compatibility controls.

---

# 126. Schema Registry

Where a schema registry is used, compatibility checks should run before publication/deployment.

---

# 127. JSON Events

If JSON events are used without a schema registry, explicit event-contract tests become even more important.

---

# 128. Event Metadata

Automated tests may verify required event envelope fields such as:

```text
eventId

eventType

occurredAt

aggregateId
```

where mandated.

---

# 129. Event ID Stability

Retry-related tests should verify the same logical event retains its event ID.

---

# 130. Consumer Compatibility

Critical consumers should be tested against representative older event versions where historical replay matters.

---

# 131. Database Fitness Functions

Repository/data-access rules require automated verification where practical.

---

# 132. N+1

Critical query paths may use query-count tests to detect N+1 regressions.

---

# 133. Why Not Globally Count Queries

Not every query count should be frozen because minor ORM changes may produce equivalent behavior.

Use query-count tests on high-risk paths.

---

# 134. PostgreSQL Integration

Custom PostgreSQL-specific query behavior should be validated using PostgreSQL integration tests.

---

# 135. Constraint Tests

Critical database invariants should have integration tests validating actual constraints.

---

# 136. Security Fitness Functions

Security automation should include applicable:

- SAST
- dependency scan
- secret scan
- authorization tests
- API security tests

---

# 137. Secret Scan

CI should fail on confirmed committed secrets according to policy.

---

# 138. Token Patterns

Automated secret detection must not be assumed infallible.

False positives and undiscovered secrets remain possible.

---

# 139. Security Hotspots

Security hotspots require human review where static tools cannot determine full context.

---

# 140. Container Fitness Functions

Container builds should verify baseline controls.

---

# 141. Image Scan

Container vulnerability scans should run before promotion.

---

# 142. Root User

CI/policy checks may ensure the runtime container does not run as root unless approved.

---

# 143. Mutable Tag

Deployment-policy checks should prevent use of mutable image tags as authoritative production identity.

---

# 144. Runtime Fitness Functions

Some architecture properties can only be verified at runtime.

---

# 145. Health Probe Test

Deployment verification should confirm readiness/liveness behavior.

---

# 146. Configuration Validation

Applications should fail startup for missing mandatory configuration rather than proceed with silently insecure defaults.

---

# 147. Smoke Tests

Post-deployment smoke tests provide runtime fitness functions for critical paths.

---

# 148. Synthetic Checks

Synthetic checks may continuously validate selected user/business journeys.

---

# 149. SLO Fitness Function

SLO burn/error-budget alerting is a production-time fitness function.

---

# 150. Performance Fitness Functions

Performance requirements should be automated selectively.

---

# 151. Regression Threshold

Critical operations may define performance-regression thresholds.

---

# 152. Avoid Fragile Micro-Timing in CI

Ordinary CI environments may be too variable for strict millisecond microbenchmarks.

---

# 153. Performance Environment

Material performance gates should use controlled benchmark/load-test environments where possible.

---

# 154. JMH

JMH should be used for JVM microbenchmarks rather than naive elapsed-time tests.

---

# 155. Load Testing

Critical end-to-end paths should use load tests against representative infrastructure/data.

---

# 156. Concurrency Fitness Functions

ADR-034 concurrency rules should be validated through automated tests and metrics.

---

# 157. Context Propagation

Tests should verify required:

- SecurityContext
- correlation context
- request context

inside concurrent execution.

---

# 158. Context Isolation

Tests must verify no cross-request context leakage.

---

# 159. Bounded Concurrency

Critical fan-out code should have tests proving the intended concurrency limit where feasible.

---

# 160. No Unbounded Executor Queue

Static/configuration review should prevent unbounded executor queues in controlled application executors unless explicitly justified.

---

# 161. Architecture Rules as Code

Architecture tests should be version controlled alongside the application.

---

# 162. Why

Changing an architecture rule is itself an architectural change and should be reviewed.

---

# 163. Shared Architecture Library

Common ArchUnit rules may be distributed through a shared test library when:

- rules are stable
- cross-service use is valuable
- versioning is controlled

---

# 164. Shared Rule Risk

A shared rule library can create coupling.

Services must remain able to evolve through versioned adoption.

---

# 165. Local Rules

Service-specific architecture constraints should remain local.

---

# 166. Suggested Architecture Test Structure

Conceptually:

```text
architecture/
    ArchitectureTest.java
    LayerArchitectureTest.java
    PackageCycleTest.java
    NamingConventionTest.java
    DependencyRestrictionTest.java
```

---

# 167. Example ArchitectureTest

Conceptual example:

```java
class ArchitectureTest {

    private static final JavaClasses CLASSES =
            new ClassFileImporter()
                    .withImportOption(
                            ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages("com.company.orders");

    @Test
    void testDomainShouldNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .check(CLASSES);
    }
}
```

Exact package layout should follow the service.

---

# 168. AssertJ vs ArchUnit Assertions

The project's AssertJ `.as(...)` convention applies to AssertJ-based tests.

ArchUnit's own rule-description facilities should be used to provide equivalent diagnostic clarity.

---

# 169. Rule Description

Architecture rules should describe why they exist.

Example:

```text
Controllers must not depend directly on repositories
because application services own orchestration and
transaction boundaries.
```

---

# 170. Failure Diagnostics

A fitness-function failure must be understandable from CI output.

---

# 171. Cryptic Rules

Avoid custom rules whose failures only say:

```text
Architecture violation.
```

---

# 172. Exception Mechanism

Architecture rules may support narrowly scoped approved exceptions.

---

# 173. Exception Metadata

Where automation supports it, exceptions should include:

```text
Reason

Tracking Ticket

Owner

Expiration
```

---

# 174. Example Exception

Conceptually:

```text
LegacyOrderAdapter
exception until CARD-2048
```

---

# 175. No Permanent Silent Ignore

Architecture violations must not be suppressed permanently without documentation.

---

# 176. Baseline Mode

Legacy systems may initially adopt a baseline approach.

---

# 177. Existing Violations

A large legacy system with hundreds of current violations should not necessarily block adoption of fitness functions.

---

# 178. Ratchet Strategy

Use a ratchet:

```text
Current Known Violations
        |
        v
Baseline
        |
        v
No New Violations
        |
        v
Gradually Remove Existing Violations
```

---

# 179. Why Ratchet

This allows improvement without requiring a big-bang rewrite.

---

# 180. Baseline Must Shrink

The baseline should not become a permanent ignored list.

---

# 181. Baseline Review

Exceptions/baselines require periodic reduction review.

---

# 182. New Code Policy

New code should meet current architecture standards even when legacy areas still have approved debt.

---

# 183. Pipeline Integration

Fitness functions must run in CI at appropriate stages.

---

# 184. Fast Fitness Functions

Fast structural rules should run during normal pull-request validation.

---

# 185. Slow Fitness Functions

Expensive:

- load tests
- container scans
- full compatibility suites

may run in dedicated pipeline stages.

---

# 186. Failure Policy

A mandatory fitness-function failure must block normal promotion.

---

# 187. Warning Mode

New rules may begin in warning/observation mode before becoming mandatory when rollout risk requires it.

---

# 188. Rule Rollout

Recommended process:

```text
DEFINE RULE

↓

OBSERVE CURRENT VIOLATIONS

↓

FIX / BASELINE

↓

ENABLE FAILURE

↓

MONITOR
```

---

# 189. Rule Stability

Avoid introducing noisy rules with high false-positive rates as mandatory gates.

---

# 190. False Positive Cost

A gate engineers routinely bypass is worse than a smaller set of trustworthy controls.

---

# 191. Governance Hierarchy

Automated governance should prioritize:

```text
High Impact
+
Low Ambiguity
+
Low False Positive Rate
```

---

# 192. Review Still Required

Automation cannot evaluate every architectural tradeoff.

---

# 193. Human Architecture Review

Human review remains necessary for questions such as:

- whether a new abstraction is justified
- whether a service should be split
- whether eventual consistency is acceptable
- whether a new technology is appropriate
- whether the bounded context is correct

---

# 194. Automation Complements Judgment

The model is:

```text
AUTOMATION
    +
CODE REVIEW
    +
ARCHITECTURE REVIEW
    +
PRODUCTION FEEDBACK
```

---

# 195. Fitness Function Ownership

Every mandatory cross-service fitness function requires an owning team.

---

# 196. Owner Responsibilities

Owners maintain:

- rule implementation
- documentation
- compatibility
- false-positive triage
- rollout strategy

---

# 197. Rule Deprecation

Fitness functions that no longer reflect current architecture must be:

```text
Updated

Deprecated

Removed
```

deliberately.

---

# 198. Rule Versioning

Shared rule libraries should use explicit versions.

---

# 199. Forced Rule Upgrade

Services should not receive unexpected architecture-rule behavior through floating dependency versions.

---

# 200. Architecture Test Dependency

A shared architecture-test dependency must use a controlled version according to ADR-038.

---

# 201. Metrics

Architecture automation should produce useful governance metrics.

---

# 202. Suggested Metrics

Examples:

```text
Architecture Gate Failures

New Architecture Violations

Open Exceptions

Expired Exceptions

Package Cycles

API Breaking Changes Prevented

Migration Validation Failures

Dependency Policy Violations
```

---

# 203. Metrics Are Not Targets Alone

The goal is not:

```text
0 architecture-test failures
```

by weakening every rule.

The goal is architecture compliance.

---

# 204. Auditability

CI should preserve sufficient evidence of mandatory gate execution for release traceability.

---

# 205. Quality Gate Matrix

A service should define which controls run at which stage.

Example:

| Control | PR | Main | Release | Runtime |
|---|---:|---:|---:|---:|
| Compile | Yes | Yes | Yes | No |
| Unit tests | Yes | Yes | Yes | No |
| ArchUnit | Yes | Yes | Yes | No |
| JaCoCo | Yes | Yes | Yes | No |
| SonarQube | Yes | Yes | Yes | No |
| SAST | Yes | Yes | Yes | No |
| SCA | Yes | Yes | Yes | No |
| Flyway validation | Yes | Yes | Yes | No |
| OpenAPI compatibility | Yes | Yes | Yes | No |
| Container scan | No/Optional | Yes | Yes | No |
| Smoke test | No | Optional | Yes | Yes |
| SLO validation | No | No | No | Yes |

---

# 206. Architecture Governance Gate

A service is not considered fully governed until:

```text
[ ] Architecture tests exist

[ ] Package-cycle rule exists

[ ] Layer dependency rules exist

[ ] Controller-to-repository rule reviewed

[ ] Domain dependency rule reviewed

[ ] Infrastructure leakage reviewed

[ ] Architecture-test failures block CI

[ ] Coverage gate configured

[ ] Sonar gate configured

[ ] SAST gate configured

[ ] SCA gate configured

[ ] Flyway validation configured

[ ] Duplicate migration version detection exists

[ ] Applied migration immutability process exists

[ ] API compatibility validation exists where applicable

[ ] Event compatibility validation exists where applicable

[ ] Secret scanning configured

[ ] Container scanning configured where applicable

[ ] Approved exception process exists

[ ] Legacy violations have baseline/remediation plan

[ ] Shared fitness-function versions controlled

[ ] Rule ownership defined

[ ] Rule documentation available

[ ] Production smoke/SLO controls defined where applicable
```

---

# 207. Anti-Patterns

The following are prohibited or strongly discouraged:

- architecture rules existing only in documentation when easily automatable
- controllers directly accessing repositories in violation of the architecture
- domain packages depending on infrastructure
- package cycles
- broad ArchUnit exclusions
- architecture tests weakened whenever they fail
- meaningless naming rules with no architectural value
- using complexity thresholds as absolute design truth
- blindly fixing Sonar metrics through artificial abstractions
- arbitrary `Thread.sleep` in tests
- disabling tests to satisfy CI
- modifying applied Flyway migrations
- automatically running `flyway repair` on checksum mismatch
- duplicate Flyway versions
- OpenAPI changes without compatibility analysis
- public API breaking changes discovered only after deployment
- dynamic dependency versions
- unscanned production dependencies
- permanent architecture exception lists
- shared rules using floating versions
- architecture rules with no owner
- mandatory gates with persistent high false-positive rates
- measuring governance success by weakening rules until everything passes

---

# 208. Positive Consequences

The decision provides:

- earlier architecture feedback
- lower architecture drift
- consistent service structure
- enforceable Clean Architecture boundaries
- safer Flyway governance
- stronger API compatibility
- standardized test quality
- reduced manual-review burden
- auditable architecture controls
- incremental modernization support
- improved refactoring confidence

---

# 209. Negative Consequences

The decision introduces:

- architecture-test maintenance
- CI execution cost
- custom rule development
- exception governance
- migration compatibility tooling
- contract-diff tooling
- rule ownership responsibilities

These costs are accepted because automated prevention is usually cheaper than correcting architecture drift after it becomes systemic.

---

# 210. Neutral Consequences

The decision also means:

- not every architectural property can be automated
- architecture tests may differ between services
- legacy services may initially require baselines
- some rules remain review-based
- rule thresholds may evolve
- false-positive management becomes part of governance

---

# 211. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Excessive rigid governance | High | Medium | Automate only stable rules |
| False-positive fatigue | High | Medium | High-signal gates |
| Rule bypass | High | Medium | Narrow exception governance |
| Legacy adoption blocked | High | Medium | Ratchet baseline |
| Shared-rule coupling | Medium | Medium | Versioned library |
| CI slowdown | Medium | Medium | Stage expensive checks |
| Architecture-rule obsolescence | Medium | Medium | Periodic review |
| Cosmetic metric gaming | Medium | High | Outcome-based governance |
| Missing architecture property | High | Medium | Human architecture review |
| Migration history corruption | Critical | Low | Flyway validation |
| API compatibility regression | Critical | Medium | Contract diff |
| Rule ownership gap | High | Medium | Explicit owner |

---

# 212. Implementation Guidance

The following rules are mandatory:

1. Important deterministic architectural constraints should be automated.
2. ArchUnit is preferred for Java structural architecture tests.
3. Domain-to-infrastructure dependencies must be prevented where Clean Architecture requires it.
4. Controller-to-repository access must be prohibited where application services own orchestration.
5. Package cycles must fail architecture validation.
6. Infrastructure-specific types must not leak through protected domain/application boundaries.
7. Architecture-test failures must fail CI unless an approved exception exists.
8. Architecture exceptions must remain narrow and traceable.
9. Legacy services should use a ratchet strategy rather than disabling governance.
10. New code must not increase baseline architecture violations.
11. JaCoCo, SonarQube, SAST and SCA remain automated fitness functions.
12. Test conventions should be automated where reliable.
13. Arbitrary test sleeps should be prohibited where automated detection is practical.
14. AssertJ descriptive assertion conventions must remain enforced through tooling/review.
15. Flyway migration version uniqueness must be automated.
16. Flyway validation must fail on unexpected checksum mismatches.
17. CI/CD must never silently repair Flyway history.
18. Existing applied migrations remain immutable; corrections use new versions.
19. OpenAPI contracts should undergo automated compatibility checks for published APIs.
20. Kafka/event schemas should undergo compatibility checks where applicable.
21. Dynamic dependency versions must be prohibited.
22. Secret scanning and dependency scanning are mandatory.
23. Container artifacts should be scanned before production promotion.
24. Runtime smoke/SLO checks should complement build-time fitness functions.
25. Shared architecture-rule libraries must be versioned.
26. Mandatory rules require identifiable ownership.
27. Architecture automation must prioritize low ambiguity and high impact.
28. Automated checks complement rather than replace engineering judgment.
29. Fitness functions must be periodically reviewed for continued relevance.
30. Rule removal/change must be treated as an architectural governance change.

---

# 213. Validation

This ADR will be validated through:

- ArchUnit
- Gradle validation
- JaCoCo
- SonarQube
- SAST
- SCA
- secret scanning
- Flyway validation
- PostgreSQL integration tests
- OpenAPI validation/diff
- event-schema compatibility checks
- container scans
- deployment smoke tests
- SLO monitoring
- CI/CD gate audits
- periodic architecture reviews

---

# 214. Success Criteria

The decision is successful when:

- architecture violations are detected before merge
- package cycles remain absent
- domain/application boundaries remain stable
- controllers do not bypass application orchestration
- applied Flyway migration mutations are detected before deployment
- duplicate migration versions are rejected automatically
- breaking APIs are detected before release
- dependency-policy violations fail early
- architecture exceptions decrease over time
- legacy baselines shrink rather than grow
- CI failures provide clear architectural diagnostics
- human review focuses on tradeoffs rather than repeatedly detecting mechanical violations

---

# 215. Alternatives Rejected

## 215.1 Documentation-Only Governance

Rejected because deterministic rules can drift silently.

---

## 215.2 Manual Code Review for Every Rule

Rejected because repetitive structural checks are better automated.

---

## 215.3 One Universal ArchUnit Suite for Every Service

Rejected because service-specific architecture may legitimately differ.

---

## 215.4 Fail All Legacy Violations Immediately

Rejected because this may block incremental governance adoption.

---

## 215.5 Ignore Current Violations Permanently

Rejected because baselines must decrease over time.

---

## 215.6 Automatically Fix Architecture Violations

Rejected because many violations require design decisions, not mechanical code mutation.

---

## 215.7 SonarQube as the Only Fitness Function

Rejected because general static analysis does not fully express domain/layer architecture.

---

# 216. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-004: Use Spring Boot
- ADR-006: Use Flyway for Database Migrations
- ADR-009: Use Apache Kafka for Integration Events
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards

---

# 217. References

- Building Evolutionary Architectures
- ArchUnit Documentation
- Gradle Documentation
- Flyway Documentation
- SonarQube Documentation
- JaCoCo Documentation
- OpenAPI Specification
- OWASP
- CycloneDX
- Apache Kafka Documentation
- PostgreSQL Documentation
- DORA
- Google SRE

---

# 218. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial architecture fitness-function baseline |

---

# 219. Decision Summary

The definitive governance model becomes:

```text
                  ADR
                   |
                   v
            ARCHITECTURE RULE
                   |
                   v
         CAN THIS BE AUTOMATED?
             /           \
           YES            NO
            |              |
            v              v
      FITNESS FUNCTION   REVIEW
            |              |
            +------+-------+
                   |
                   v
                  CI
                   |
             +-----+-----+
             |           |
             v           v
           PASS         FAIL
             |           |
             v           v
           MERGE      FIX / APPROVED
                       EXCEPTION
```

For Java architecture:

```text
CONTROLLER
     |
     v
APPLICATION
     |
     v
DOMAIN
     ^
     |
INFRASTRUCTURE
implements ports
```

and automated rules prevent:

```text
Controller -------> Repository Implementation

Domain -----------> WebClient

Domain -----------> Spring Data JPA

Application ------> AWS SDK Implementation
```

unless the architecture explicitly permits the dependency.

For package cycles:

```text
A
|
v
B
|
v
C
|
+-----> A
```

must become:

```text
CI FAILURE
```

rather than hidden architectural coupling.

For Flyway:

```text
db/migration/
   |
   +--> V27
   +--> V28
   +--> V29
```

CI verifies:

```text
UNIQUE VERSION
+
VALID NAMING
+
FULL MIGRATION SUCCESS
+
CHECKSUM CONSISTENCY
```

and:

```text
APPLIED MIGRATION MODIFIED
          |
          v
     VALIDATION FAILURE
          |
          v
      DO NOT REPAIR
      AUTOMATICALLY
          |
          v
   CREATE NEXT MIGRATION
```

For APIs:

```text
CURRENT OPENAPI
       |
       v
NEW OPENAPI
       |
       v
CONTRACT DIFF
       |
    +--+--+
    |     |
    v     v
COMPATIBLE BREAKING
    |        |
    v        v
 PASS      REVIEW /
           VERSION
```

For legacy architecture:

```text
100 EXISTING VIOLATIONS
          |
          v
       BASELINE
          |
          v
NEW PR ADDS 1?
      /       \
    YES        NO
     |          |
     v          v
   FAIL       PASS
     |
     v
NO NEW DEBT
```

and progressively:

```text
100
 ↓
80
 ↓
50
 ↓
20
 ↓
0
```

The desired quality system becomes:

```text
                  SOURCE
                    |
        +-----------+-----------+
        |           |           |
        v           v           v
     ARCHUNIT      TESTS      CONTRACTS
        |           |           |
        v           v           v
      JACOCO      SONAR       OPENAPI
        |           |           |
        +-----------+-----------+
                    |
             +------+------+
             |             |
             v             v
           SAST           SCA
             |             |
             +------+------+
                    |
                    v
              FLYWAY VALIDATION
                    |
                    v
               IMAGE SCAN
                    |
                    v
             RELEASE ELIGIBLE
```

But automated governance is deliberately bounded:

```text
AUTOMATION
can answer:

"Does Domain depend on Infrastructure?"
```

It cannot reliably answer:

```text
"Should this capability become
a separate microservice?"
```

Therefore the complete governance model is:

```text
AUTOMATED FITNESS FUNCTIONS
            +
       CODE REVIEW
            +
    ARCHITECTURE REVIEW
            +
    PRODUCTION FEEDBACK
            =
EVOLUTIONARY ARCHITECTURE
```

The definitive principle is:

```text
Architecture rules that can be tested
should become tests.

Architecture rules that cannot be tested
must remain explicit decisions.

Neither should depend solely on memory.
```
