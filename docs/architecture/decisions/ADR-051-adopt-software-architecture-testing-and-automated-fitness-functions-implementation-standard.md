# ADR-051: Adopt Software Architecture Testing and Automated Fitness Functions Implementation Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-051 |
| Title | Adopt Software Architecture Testing and Automated Fitness Functions Implementation Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Architecture Testing, ArchUnit, Gradle, CI/CD, Automated Governance |
| Related Work Items | ArchUnit, JaCoCo, SonarQube, SAST, Flyway, OpenAPI, Dependency Governance |
| Supersedes | ADR-042 |
| Superseded By | None |

---

# 1. Context

ADR-042 established Architecture Fitness Functions as a governance principle.

ADR-050 established automated architectural compliance as part of the Enterprise Architecture Baseline.

The next step is implementation.

Architecture rules that exist only in documentation depend on developers remembering them during every change.

For example:

```text
Controller must not access Repository directly.

Domain must not depend on Web.

Services must not depend cyclically on each other.

Applied Flyway migrations must remain immutable.

Coverage must remain above the defined threshold.

Critical Sonar findings must fail the pipeline.

SAST findings must be evaluated.

OpenAPI changes must preserve compatibility.

Forbidden dependencies must not enter the project.
```

These rules can often be validated automatically.

The architecture therefore requires executable controls capable of failing the build when objective architectural constraints are violated.

---

# 2. Problem Statement

The organization requires implementation standards for:

- ArchUnit
- architecture tests
- package rules
- layer rules
- dependency direction
- cycle detection
- naming rules
- annotations
- Spring architecture
- domain isolation
- test architecture
- JaCoCo
- SonarQube
- SAST
- dependency analysis
- Flyway validation
- API compatibility
- OpenAPI validation
- Gradle integration
- CI enforcement
- local execution
- exceptions
- fitness-function ownership

---

# 3. Decision Drivers

Primary drivers are:

1. continuous architecture compliance
2. fast developer feedback
3. reduced architecture drift
4. objective governance
5. automated enforcement
6. maintainability
7. secure defaults
8. CI/CD consistency
9. developer autonomy
10. architecture scalability

---

# 4. Decision

Java/Spring Boot services MUST implement automated architecture fitness functions appropriate to their architecture profile.

The target model is:

```text
                 SOURCE CODE
                      |
                      v
                    BUILD
                      |
        +-------------+-------------+
        |             |             |
        v             v             v
      TESTS        ARCHUNIT       JACOCO
        |             |             |
        +-------------+-------------+
                      |
                      v
                   SONAR
                      |
                      v
                    SAST
                      |
                      v
             DEPENDENCY ANALYSIS
                      |
                      v
              CONTRACT VALIDATION
                      |
                      v
                FITNESS RESULT
                      |
                +-----+-----+
                |           |
               PASS        FAIL
                |           |
                v           v
              CI OK      CI BLOCKED
```

---

# 5. Fundamental Principle

The governing principle is:

```text
If an architectural rule can be
reliably expressed as code,
it should not depend exclusively
on human memory.
```

---

# 6. Architecture Tests

Architecture tests are executable tests validating structural characteristics of the application.

They MUST run automatically.

---

# 7. Architecture Tests Are Tests

Architecture tests belong to the normal software lifecycle:

```text
WRITE

RUN LOCALLY

RUN IN CI

MAINTAIN

REVIEW
```

---

# 8. ArchUnit

ArchUnit is the preferred Java mechanism for structural architecture tests.

---

# 9. Dependency

A typical Gradle configuration is:

```groovy
dependencies {
    testImplementation 'com.tngtech.archunit:archunit-junit5:<approved-version>'
}
```

The actual version MUST be governed through the project's dependency-management mechanism.

---

# 10. No Hardcoded Version Drift

Individual projects SHOULD NOT independently select arbitrary ArchUnit versions when a platform-managed version exists.

---

# 11. Architecture Test Location

Architecture tests SHOULD reside in a clearly identifiable package.

Example:

```text
src/test/java/io/astraforge/supplyplatform/architecture
```

---

# 12. Naming

Architecture test classes SHOULD use names such as:

```text
ArchitectureTest

LayerArchitectureTest

DependencyArchitectureTest

NamingArchitectureTest

SpringArchitectureTest
```

---

# 13. Test Method Naming

Where project standards require `test*` naming, architecture tests MUST follow the same convention.

Example:

```java
@Test
void testControllersShouldNotAccessRepositoriesDirectly() {
    ...
}
```

---

# 14. Assertion Diagnostics

Where AssertJ is used around architecture-test support code, meaningful `.as("...")` descriptions MUST follow project testing standards.

---

# 15. Imported Classes

Architecture tests SHOULD import only production packages relevant to the application.

Example:

```java
private static final JavaClasses CLASSES =
        new ClassFileImporter()
                .importPackages("io.astraforge.supplyplatform");
```

---

# 16. Layer Model

For a conventional layered service:

```text
Controller
    |
    v
Service
    |
    v
Repository
```

is an approved dependency direction.

---

# 17. Direct Controller Repository Access

This dependency SHOULD be prohibited:

```text
Controller
    |
    v
Repository
```

when the service architecture defines a service/application layer.

---

# 18. Example ArchUnit Rule

Conceptually:

```java
noClasses()
        .that()
        .resideInAPackage("..controller..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..repository..");
```

---

# 19. Controller Responsibilities

Controllers SHOULD primarily handle:

```text
HTTP Contract

Request Validation

Authentication Context

Application Invocation

Response Mapping
```

Controllers SHOULD NOT become business-logic containers.

---

# 20. Repository Responsibilities

Repositories SHOULD remain persistence-oriented.

---

# 21. Repository Dependency Direction

Repositories MUST NOT depend on controllers.

---

# 22. Service Dependency Direction

Services MUST NOT depend on controllers.

---

# 23. Domain Dependency Direction

Domain code SHOULD NOT depend on HTTP/controller infrastructure.

---

# 24. Integration Boundary

External integration clients SHOULD remain identifiable.

Example:

```text
integration

client

webclient
```

depending on project conventions.

---

# 25. Domain to Integration

Direct domain-object coupling to transport-specific integration infrastructure SHOULD be avoided.

---

# 26. Package Cycles

Package dependency cycles SHOULD be detected automatically.

Target:

```text
A -> B -> C
```

rather than:

```text
A -> B -> C
^         |
|---------|
```

---

# 27. Cycle Detection

Architecture tests SHOULD detect cycles between major application slices/packages.

---

# 28. Why Cycles Matter

Cycles increase:

- coupling
- refactoring cost
- initialization complexity
- testing complexity
- change blast radius

---

# 29. Spring Components

Spring stereotypes SHOULD align with architectural roles.

---

# 30. Controller Annotation

Classes implementing HTTP controllers SHOULD use the approved controller stereotype.

---

# 31. Repository Annotation

Persistence adapters SHOULD use the project's approved repository pattern.

---

# 32. Service Annotation

Spring services SHOULD use appropriate service stereotypes where Spring-managed components are required.

---

# 33. Component Scanning

Architecture tests MAY verify that components remain inside intended package boundaries.

---

# 34. Naming Conventions

Naming rules MAY be automated.

Examples:

```text
*Controller

*Service

*ServiceImpl

*Repository

*Mapper

*Client

*Configuration
```

where those conventions are actually adopted by the project.

---

# 35. Do Not Enforce Arbitrary Naming

Architecture tests MUST NOT enforce naming conventions that do not provide architectural value.

---

# 36. Implementation Naming

`*ServiceImpl` MUST NOT be required merely because every interface historically had an implementation with that suffix.

---

# 37. Interface Rule

Interfaces SHOULD exist when they provide an actual abstraction boundary.

---

# 38. No Interface Ceremony

Architecture tests MUST NOT force:

```text
Interface + Single Implementation
```

for every service without architectural justification.

---

# 39. Dependency Rules

Architecture tests SHOULD enforce prohibited dependencies.

---

# 40. Example Forbidden Dependency

Domain/application code SHOULD NOT depend directly on:

```text
javax.servlet

jakarta.servlet
```

unless it explicitly belongs to the web boundary.

---

# 41. Framework Isolation

Core business logic SHOULD minimize unnecessary Spring framework coupling.

---

# 42. Utility Dependency

Domain code SHOULD NOT become dependent on controller/web utility classes.

---

# 43. Configuration Isolation

Configuration classes SHOULD NOT contain business logic.

---

# 44. Exception Architecture

Exception packages SHOULD preserve intended dependency direction.

---

# 45. Global Exception Handler

HTTP exception handlers MAY depend on application/domain exceptions.

Domain exceptions MUST NOT depend on HTTP exception handlers.

---

# 46. DTO Architecture

Request/response DTOs SHOULD remain associated with external contracts.

---

# 47. Entity Leakage

JPA entities SHOULD NOT automatically become public REST response contracts.

---

# 48. Repository Exposure

Repositories MUST NOT be exposed directly through controllers merely to reduce implementation code.

---

# 49. Mapping Boundary

Mapping between persistence/domain/API representations SHOULD remain explicit where the models have distinct responsibilities.

---

# 50. Architecture Rule Granularity

Rules SHOULD focus on meaningful boundaries.

Avoid hundreds of brittle rules governing incidental implementation details.

---

# 51. Stable Rules

A fitness function SHOULD represent an architectural invariant expected to remain valid.

---

# 52. Architecture Rule Ownership

Every shared architecture rule MUST have an owner.

---

# 53. Rule Documentation

Non-obvious rules SHOULD explain:

```text
What is prohibited?

Why?

What is the approved alternative?

How is an exception handled?
```

---

# 54. Rule Failure Message

Failures SHOULD be understandable without requiring the developer to inspect the test implementation.

---

# 55. Local Execution

Architecture tests MUST be executable locally.

Typical command:

```text
./gradlew test
```

or a dedicated architecture-test task where justified.

---

# 56. CI Execution

Mandatory architecture tests MUST execute in CI.

---

# 57. Separate Architecture Task

Larger projects MAY define:

```text
architectureTest
```

as a dedicated Gradle task.

---

# 58. Normal Build Integration

If a separate task exists, the normal verification lifecycle MUST still execute it.

Conceptually:

```text
check
  |
  +--> test
  |
  +--> architectureTest
```

---

# 59. Build Cannot Ignore Architecture

A successful release build MUST NOT silently omit mandatory architecture tests.

---

# 60. JaCoCo

JaCoCo is the standard Java code-coverage mechanism where coverage is required.

---

# 61. Coverage Threshold

Projects MUST follow their defined coverage threshold.

For services whose established baseline is:

```text
>= 80%
```

the build SHOULD enforce that threshold according to project policy.

---

# 62. Coverage Dimensions

Where useful, validate:

```text
Line Coverage

Branch Coverage
```

rather than line coverage alone.

---

# 63. Coverage Is Not Correctness

This remains invalid:

```text
100% Coverage
     =
100% Correct
```

---

# 64. Meaningful Tests

Tests created solely to execute lines without asserting behavior SHOULD be rejected during review.

---

# 65. Exclusions

Coverage exclusions MUST be deliberate.

---

# 66. Exclusion Abuse

The following is prohibited:

```text
Coverage too low
      |
      v
Exclude difficult classes
      |
      v
Artificially green report
```

---

# 67. Legitimate Exclusions

Potential examples include narrowly justified generated or framework boilerplate.

---

# 68. Exclusion Review

Broad package exclusions SHOULD require explicit justification.

---

# 69. SonarQube

SonarQube forms part of the automated fitness-function suite.

---

# 70. Quality Gate

Required Sonar Quality Gates MUST block the CI pipeline when failing.

---

# 71. New Code

Quality policies SHOULD emphasize New Code quality while maintaining visibility of existing technical debt.

---

# 72. Sonar Findings

Findings SHOULD be corrected at the source rather than hidden through workarounds.

---

# 73. Nullability

If Sonar identifies a credible nullable dereference, the implementation SHOULD establish the actual invariant or safely handle null.

---

# 74. Exception Handling

If Sonar requires an exception to be logged or rethrown, generated fixes MUST preserve the real error-handling semantics.

---

# 75. No Fake Logging

This is not sufficient merely to silence a rule:

```java
catch (Exception ex) {
    log.debug("error");
}
```

if the exception should actually propagate or trigger domain handling.

---

# 76. Sonar Test Rules

Test code MUST also satisfy applicable Sonar rules.

---

# 77. AssertJ Descriptions

Project test conventions requiring AssertJ descriptions MUST be preserved in generated or modified tests.

Example:

```java
assertThat(result)
        .as("result should contain the expected status")
        .isEqualTo(expectedStatus);
```

---

# 78. SAST

Static Application Security Testing forms part of architecture compliance.

---

# 79. SAST Gate

Applicable unresolved security findings MUST follow the project's security severity policy.

---

# 80. Security Finding Workaround

Code MUST NOT be distorted merely to hide a security scanner finding.

---

# 81. Root Cause

Security findings SHOULD be resolved at the actual trust boundary.

---

# 82. Input vs Output Security

Architecture reviews MUST distinguish:

```text
Input Validation

Domain Preservation

Output Encoding
```

---

# 83. Dependency Scanning

Dependencies MUST participate in automated vulnerability scanning.

---

# 84. Dependency Verification

New dependencies SHOULD be evaluated for:

```text
Necessity

Maintenance

License

Security

Compatibility
```

---

# 85. Forbidden Dependencies

Known prohibited libraries SHOULD be blocked automatically where practical.

---

# 86. Duplicate Libraries

Projects SHOULD avoid multiple libraries solving the same platform concern without justification.

---

# 87. Dependency Direction

Build/module dependency direction MAY be tested.

---

# 88. Internal Modules

In multi-module builds, modules SHOULD have explicit responsibilities.

---

# 89. Module Cycles

Gradle module cycles MUST NOT exist.

---

# 90. Dependency Convergence

Dependency conflicts SHOULD be visible through build tooling.

---

# 91. Dynamic Versions

Architecture validation SHOULD reject uncontrolled versions such as:

```text
1.+

latest.release
```

where project governance prohibits them.

---

# 92. SNAPSHOT Dependencies

Production builds SHOULD NOT depend on uncontrolled SNAPSHOT artifacts unless explicitly governed.

---

# 93. Gradle Wrapper

CI MUST execute the repository Gradle Wrapper.

---

# 94. Wrapper Validation

The wrapper SHOULD be validated as part of repository governance.

---

# 95. Java Version Fitness Function

CI SHOULD verify the approved Java version.

---

# 96. Toolchain

Gradle Java Toolchains SHOULD be used where appropriate to make Java-version requirements explicit.

Example:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

---

# 97. Compiler Warnings

Relevant compiler warnings SHOULD remain visible.

---

# 98. Warning Suppression

Broad warning suppression SHOULD be avoided.

---

# 99. Flyway Fitness Functions

Database-enabled services MUST validate Flyway conventions.

---

# 100. Migration Naming

Migration files MUST follow the approved naming convention.

Example:

```text
V27__create_outbox_event.sql
```

---

# 101. Migration Ordering

Migration versions MUST remain uniquely ordered.

---

# 102. Migration Immutability

Applied migrations MUST NOT be modified.

---

# 103. CI Migration Validation

Where a baseline database is available, CI SHOULD detect migration checksum/history incompatibility.

---

# 104. Corrective Migration

Required behavior:

```text
V27 applied
    |
    v
Problem discovered
    |
    v
CREATE V28
```

not:

```text
EDIT V27
```

---

# 105. Migration Test

Important migrations SHOULD be exercised against representative database technology.

---

# 106. PostgreSQL

PostgreSQL migrations SHOULD be tested against PostgreSQL.

---

# 107. Oracle

Oracle-specific migrations/procedures require Oracle-compatible validation appropriate to the environment.

---

# 108. Schema Validation

Applications using ORM SHOULD validate important schema/entity compatibility where appropriate.

---

# 109. OpenAPI Fitness Functions

REST APIs SHOULD automate contract validation where feasible.

---

# 110. OpenAPI Generation

Generated OpenAPI SHOULD reflect the implementation contract.

---

# 111. Contract Drift

CI SHOULD detect unintended drift between expected and generated API contracts.

---

# 112. Breaking Change Detection

Shared APIs SHOULD use automated breaking-change detection where feasible.

---

# 113. Breaking Changes

Examples include:

```text
Removing Endpoint

Removing Response Field

Changing Required Field

Changing Type

Removing Enum Value

Changing Status Semantics
```

depending on compatibility policy.

---

# 114. Intentional Breaking Change

An intentional breaking change requires the approved compatibility/versioning process.

---

# 115. Contract Snapshot

Projects MAY retain an approved API contract snapshot for comparison.

---

# 116. Consumer Contracts

Consumer-driven contract testing MAY complement OpenAPI compatibility validation.

---

# 117. Event Contract Fitness Functions

SQS/event-driven systems SHOULD validate event schema compatibility.

---

# 118. Event Schema

Schema changes MUST follow applicable compatibility rules.

---

# 119. Required Event Fields

Critical event metadata MAY be tested automatically.

Examples:

```text
eventId

eventType

traceId
```

where defined by the event architecture.

---

# 120. Event Naming

Event types SHOULD follow approved naming conventions.

---

# 121. Serialization

Producer and consumer serialization compatibility SHOULD be validated.

---

# 122. Configuration Fitness Functions

Configuration can also be tested.

---

# 123. Required Properties

Critical configuration SHOULD fail fast when absent.

---

# 124. Secrets

Architecture validation SHOULD detect likely secrets committed to source through approved secret-scanning tooling.

---

# 125. Environment-Specific Secrets

Secrets MUST remain externalized.

---

# 126. Actuator

Actuator exposure MUST follow security standards.

---

# 127. CORS

Unrestricted CORS SHOULD be detected or reviewed according to application context.

---

# 128. Debug Configuration

Production profiles MUST NOT enable unsafe debug behavior.

---

# 129. Observability Fitness Functions

Critical observability capabilities SHOULD be automatically verified where practical.

---

# 130. Health Endpoint

Services SHOULD expose the required health/readiness capabilities.

---

# 131. Correlation

Integration layers SHOULD preserve correlation context according to platform standards.

---

# 132. Sensitive Logging

Static/security analysis SHOULD detect known sensitive logging patterns where possible.

---

# 133. Resilience Fitness Functions

Remote integrations SHOULD have explicit timeout configuration.

---

# 134. Missing Timeout

A remote HTTP client without bounded timeout SHOULD be considered an architectural warning or failure according to criticality.

---

# 135. Circuit Breaker

Where a dependency requires Circuit Breaker protection, configuration SHOULD be validated.

---

# 136. Retry

Retry configuration SHOULD have bounded attempts.

---

# 137. Connection Pools

External HTTP and database connection pools SHOULD have explicit limits appropriate to workload.

---

# 138. Concurrency Fitness Functions

Automated analysis MAY detect prohibited concurrency patterns.

---

# 139. Thread.sleep

Test-quality controls SHOULD flag unnecessary `Thread.sleep(...)`.

---

# 140. Unbounded Executors

Production code SHOULD NOT create uncontrolled/unbounded executors without justification.

---

# 141. Common Pool

Implicit use of shared common pools SHOULD be reviewed for critical asynchronous workloads.

---

# 142. Virtual Threads

Virtual Thread executors MUST follow context-propagation requirements where applicable.

---

# 143. Reflection

Architecture/security analysis SHOULD identify unnecessary reflective access.

---

# 144. setAccessible

Generated application code SHOULD avoid `setAccessible(true)` unless explicitly justified.

---

# 145. Repository Fitness Functions

Repositories SHOULD have automated metadata validation.

---

# 146. Required Files

Applicable repositories SHOULD contain:

```text
README

Gradle Wrapper

Build Configuration

CI Configuration
```

and other baseline artifacts required by the architecture profile.

---

# 147. Package Documentation

Where project standards require `package-info.java`, repository checks MAY verify its presence.

---

# 148. README Fitness Function

Automated validation MAY verify required README sections.

---

# 149. Documentation Accuracy

Automation can verify presence more easily than semantic accuracy.

Human review remains necessary.

---

# 150. Architecture Metadata

Services SHOULD expose machine-readable architecture metadata where platform tooling supports it.

Example:

```yaml
service:
  name: ecommerce-order-service
  domain: orders
  runtime: java21
  database: postgresql
  messaging: sqs
```

---

# 151. Service Catalog

Metadata SHOULD feed the service catalog automatically where practical.

---

# 152. Criticality

Architecture metadata SHOULD identify service criticality.

---

# 153. Ownership

Ownership metadata MUST be discoverable.

---

# 154. CI Fitness Function Stages

The recommended verification sequence is:

```text
COMPILE
   |
   v
UNIT TEST
   |
   v
ARCHITECTURE TEST
   |
   v
INTEGRATION TEST
   |
   v
JACOCO VERIFY
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
CONTRACT CHECK
   |
   v
PACKAGE
```

Exact ordering MAY vary when pipeline efficiency requires parallel execution.

---

# 155. Parallel CI

Independent verification stages SHOULD execute in parallel when this materially reduces feedback time without compromising correctness.

---

# 156. Fail Fast

Cheap deterministic checks SHOULD generally execute before expensive validation.

---

# 157. Example

```text
Compilation failure
```

should normally be detected before running expensive integration environments.

---

# 158. CI Feedback

Architecture validation MUST provide actionable failure messages.

---

# 159. Failure Example

Prefer:

```text
Controller CustomerController depends directly on CustomerRepository.
Controllers must access persistence through the application/service layer.
```

over:

```text
Architecture test failed.
```

---

# 160. Baseline Architecture Test Suite

A standard Java service SHOULD include checks for applicable:

```text
[ ] Layer dependency direction

[ ] Controller -> Repository prohibition

[ ] Domain isolation

[ ] Package cycles

[ ] Naming conventions

[ ] Forbidden dependencies

[ ] Module cycles

[ ] Java version

[ ] Dependency policy

[ ] Flyway conventions

[ ] Coverage threshold

[ ] Sonar Quality Gate

[ ] SAST Gate

[ ] Dependency vulnerability policy

[ ] API compatibility

[ ] Event compatibility

[ ] Repository metadata
```

---

# 161. Architecture Profile

Not every rule applies to every service.

Example:

```text
NO DATABASE
    |
    v
FLYWAY RULES = NOT_APPLICABLE
```

---

# 162. Conditional Enforcement

Fitness functions SHOULD derive applicability from the service architecture profile where practical.

---

# 163. Exception Handling

A legitimate architecture exception MUST NOT be implemented by deleting the architecture test.

---

# 164. Exception Model

Preferred model:

```text
RULE
 |
 v
VIOLATION
 |
 v
APPROVED EXCEPTION?
   /       \
 NO        YES
 |          |
 v          v
FAIL     ALLOW WITH
         TRACEABILITY
```

---

# 165. Exception Scope

Exceptions MUST be as narrow as practical.

---

# 166. Bad Exception

Avoid:

```text
Ignore all architecture violations
in package service.
```

---

# 167. Better Exception

Prefer a narrowly scoped exclusion for a known class/dependency with an associated exception record.

---

# 168. Exception Expiration

Time-bound exceptions SHOULD be detectable as expired.

---

# 169. Baseline Evolution

Architecture tests MUST evolve when ADRs change.

---

# 170. Rule Migration

New rules SHOULD use staged rollout where immediate enforcement would break many existing services.

---

# 171. Staged Rollout

Recommended:

```text
OBSERVE
   |
   v
REPORT
   |
   v
WARN
   |
   v
ENFORCE
```

---

# 172. New Services

New services SHOULD receive current architecture tests through the Golden Path.

---

# 173. Existing Services

Existing services SHOULD adopt new rules according to risk-based migration plans.

---

# 174. Central Rule Library

Stable cross-service architecture tests MAY be distributed through a shared internal test library.

---

# 175. Central Library Advantage

This provides:

```text
Consistent Rules

Versioning

Reduced Duplication

Central Improvements
```

---

# 176. Central Library Risk

A central test library can create excessive coupling.

---

# 177. Rule Design

Only stable enterprise rules SHOULD be centralized.

Service-specific architecture rules SHOULD remain inside the service repository.

---

# 178. Architecture Test Library Versioning

Shared architecture-test libraries MUST be versioned.

---

# 179. Upgrade Strategy

Projects SHOULD have a controlled path for upgrading shared rule versions.

---

# 180. No Silent Rule Change

A shared library upgrade MUST NOT unexpectedly introduce blocking rules without release communication.

---

# 181. Rule Severity

Fitness functions MAY classify findings:

```text
INFO

WARNING

ERROR

CRITICAL
```

---

# 182. Blocking Rules

Applicable `ERROR` and `CRITICAL` controls SHOULD block according to governance policy.

---

# 183. Warning

Warnings SHOULD remain visible but need not block immediately.

---

# 184. Critical Security

Critical security controls MUST NOT be downgraded merely to preserve pipeline success.

---

# 185. Fitness Function Registry

The platform SHOULD maintain a registry of architecture fitness functions.

---

# 186. Registry Fields

Recommended:

```text
Rule ID

Name

Description

ADR

Severity

Applicability

Implementation

Owner

Blocking

Exception Policy
```

---

# 187. Example Rule IDs

```text
ARCH-LAYER-001

JAVA-RUNTIME-001

DB-FLYWAY-001

SEC-DEPENDENCY-001

API-COMPAT-001
```

---

# 188. Rule Traceability

Automated rules SHOULD reference the ADR or standard they enforce.

---

# 189. Bidirectional Traceability

Target:

```text
ADR
 |
 v
CONTROL
 |
 v
AUTOMATED RULE
 |
 v
CI RESULT
```

and:

```text
CI FAILURE
 |
 v
RULE
 |
 v
ADR / RATIONALE
```

---

# 190. Architecture Scorecard Integration

Fitness-function results SHOULD feed ADR-050 service scorecards.

---

# 191. Scorecard State

Example:

```text
Architecture Layers     PASS

Java Runtime            PASS

Flyway                  PASS

Coverage                PASS

Sonar                   PASS

SAST                    PASS

API Compatibility       WARNING

Expired Exception       FAIL
```

---

# 192. Evidence

Scorecard results SHOULD link to actual CI/tool evidence.

---

# 193. Architecture Test Review

Architecture-test changes require code review like production code.

---

# 194. Weakening a Rule

A pull request weakening or deleting a mandatory architecture rule SHOULD receive elevated scrutiny.

---

# 195. Governance Code Ownership

Central architecture-control files MAY use CODEOWNERS or equivalent ownership rules.

---

# 196. Bypass Governance

Emergency bypass mechanisms, if supported, MUST be:

```text
Authorized

Audited

Time-Bound

Visible
```

---

# 197. Silent Bypass

Silent architecture-check bypass is prohibited.

---

# 198. Developer Experience

Architecture tests SHOULD improve developer experience by identifying violations early.

---

# 199. Architecture Tests Are Not Punishment

Their purpose is:

```text
FAST FEEDBACK
```

not:

```text
LATE GOVERNANCE SURPRISE
```

---

# 200. Local First

Developers SHOULD discover architecture violations before opening a pull request whenever practical.

---

# 201. Performance

Architecture-test suites SHOULD remain sufficiently fast for normal development.

---

# 202. Expensive Checks

Expensive portfolio/security checks MAY remain CI-only where local execution is impractical.

---

# 203. Determinism

Fitness functions MUST be deterministic whenever the underlying property is deterministic.

---

# 204. Flaky Architecture Test

A flaky architecture test is itself an architecture-governance defect.

---

# 205. Network Dependency

Structural architecture tests SHOULD NOT require external network access.

---

# 206. Time Dependency

Architecture tests SHOULD NOT depend unnecessarily on current time.

---

# 207. Ordering Dependency

Architecture tests SHOULD NOT depend on test execution order.

---

# 208. Architecture Test Quality Gate

An architecture-test suite is considered mature when:

```text
[ ] Rules represent actual ADRs

[ ] Rules are deterministic

[ ] Failure messages are actionable

[ ] Tests run locally

[ ] Tests run in CI

[ ] Critical rules block releases

[ ] Exceptions are controlled

[ ] Rule ownership is explicit

[ ] Rules are versioned

[ ] Results feed architecture governance

[ ] Rules do not enforce incidental style

[ ] Execution time remains reasonable
```

---

# 209. Service Fitness Function Gate

A standard Java/Spring Boot service is not considered fully compliant until applicable controls include:

```text
[ ] Java version validation

[ ] Gradle Wrapper validation

[ ] Unit-test execution

[ ] Architecture-test execution

[ ] Integration-test execution where required

[ ] JaCoCo verification

[ ] Sonar Quality Gate

[ ] SAST

[ ] Dependency vulnerability scanning

[ ] Secret scanning

[ ] Flyway validation where applicable

[ ] API compatibility validation where applicable

[ ] Event compatibility validation where applicable

[ ] Repository ownership validation

[ ] Architecture exception validation
```

---

# 210. Anti-Patterns

The following are prohibited or strongly discouraged:

- architecture rules existing only in documentation when reliable automation is feasible
- controllers directly accessing repositories against established architecture
- domain code depending on HTTP infrastructure
- cyclic package architecture
- ArchUnit tests enforcing meaningless style preferences
- interfaces created solely to satisfy architecture tests
- deleting tests to resolve architecture violations
- broad architecture exclusions
- architecture exceptions without traceability
- architecture tests omitted from normal CI
- JaCoCo exclusions used to manufacture coverage
- meaningless tests created solely for coverage
- Sonar findings suppressed without justification
- SAST findings hidden through code distortion
- arbitrary dependency versions
- uncontrolled SNAPSHOT dependencies
- modifying applied Flyway migrations
- API breaking changes without compatibility governance
- event schema changes without compatibility validation
- architecture checks with vague failure messages
- flaky fitness functions
- architecture checks requiring unnecessary external network access
- silent governance bypasses
- central architecture libraries containing service-specific rules
- architecture scorecards without evidence

---

# 211. Positive Consequences

The decision provides:

- executable architecture
- earlier feedback
- reduced architectural drift
- consistent layer boundaries
- stronger dependency governance
- safer Flyway evolution
- stronger API compatibility
- measurable compliance
- less repetitive manual review
- better Sonar/SAST integration
- stronger CI/CD governance
- scalable architecture governance

---

# 212. Negative Consequences

The decision introduces:

- architecture-test maintenance
- Gradle configuration
- CI execution cost
- rule-library governance
- exception management
- migration effort for existing services

These costs are accepted because manual architecture governance does not scale with the number of services and teams.

---

# 213. Neutral Consequences

The decision also means:

- some rules remain human-review concerns
- not every service uses identical rules
- architecture tests evolve with architecture
- false positives require rule improvement
- existing services may require staged adoption
- automated compliance does not eliminate architectural judgment

---

# 214. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Brittle architecture tests | Medium | Medium | Test stable invariants |
| Too many rules | High | Medium | Fitness-function registry |
| Developer frustration | High | Medium | Actionable failures |
| CI slowdown | Medium | Medium | Parallel execution |
| False positives | Medium | Medium | Rule validation |
| Rules become stale | High | Medium | ADR traceability |
| Broad exceptions | High | Medium | Narrow exception scope |
| Coverage gaming | Medium | Medium | Test-quality review |
| Central rule blast radius | High | Medium | Versioning |
| Governance bypass | High | Low | Audit + CODEOWNERS |

---

# 215. Implementation Guidance

The following rules are mandatory:

1. Java services must implement architecture tests appropriate to their architecture profile.
2. ArchUnit is the preferred structural architecture-testing mechanism.
3. Architecture tests must execute locally and in CI.
4. Layer dependency direction should be automatically enforced where the service uses layered architecture.
5. Controllers should not access repositories directly when a service/application layer is defined.
6. Domain code should remain isolated from transport infrastructure.
7. Package/module cycles should be detected.
8. Architecture tests must enforce meaningful invariants rather than incidental style.
9. Mandatory architecture tests must participate in the normal verification lifecycle.
10. JaCoCo thresholds must follow project quality policy.
11. Coverage exclusions must not be used to manufacture compliance.
12. Sonar Quality Gates must remain part of CI.
13. SAST must remain part of security validation.
14. Test code must follow applicable Sonar and AssertJ conventions.
15. Dependency governance must detect prohibited or unsafe dependencies.
16. Java runtime requirements should be machine-verifiable.
17. Flyway migration conventions must be validated.
18. Applied Flyway migrations must never be modified.
19. Shared APIs should use automated compatibility validation where feasible.
20. Event contracts should use compatibility validation where applicable.
21. Critical configuration and security properties should be validated automatically where practical.
22. Fitness-function failures must provide actionable diagnostics.
23. Architecture exceptions must be narrow, visible and traceable.
24. Architecture tests must not be deleted merely to allow a violation.
25. Shared enterprise rules may be centralized only when stable across services.
26. Service-specific rules should remain within service repositories.
27. Shared architecture-rule libraries must be versioned.
28. Architecture-rule changes require code review.
29. Fitness-function results should feed the architecture scorecard.
30. Automated rules should reference the ADR or standard they enforce.
31. Fitness functions must remain deterministic.
32. Critical controls must not be silently bypassed.
33. New services should receive architecture tests through the Golden Path.
34. Existing services should adopt new rules through risk-based migration.
35. Architecture automation complements rather than replaces engineering judgment.

---

# 216. Validation

This ADR will be validated through:

- Gradle builds
- ArchUnit
- JUnit 5
- JaCoCo
- SonarQube
- SAST
- dependency scanning
- secret scanning
- Flyway validation
- OpenAPI comparison
- event-schema validation
- CI/CD
- architecture scorecards
- architecture reviews

---

# 217. Success Criteria

The decision is successful when:

- architectural violations are discovered before production
- common violations are discovered before pull-request approval
- developers receive actionable local feedback
- controller/service/repository boundaries remain consistent
- package cycles decrease
- dependency drift decreases
- Flyway history remains immutable
- API breaking changes become visible
- Sonar/SAST remain integrated into normal development
- architecture reviews spend less time checking mechanical rules
- service scorecards contain automated evidence
- architecture governance scales with the number of services

---

# 218. Alternatives Rejected

## 218.1 Manual Architecture Review Only

Rejected because manual review does not scale and cannot reliably detect every structural violation.

---

## 218.2 SonarQube Alone

Rejected because Sonar provides valuable quality analysis but does not represent every enterprise architecture invariant.

---

## 218.3 ArchUnit Alone

Rejected because structural Java architecture is only one part of architecture compliance.

---

## 218.4 Coverage Alone

Rejected because high coverage does not prove architecture or behavioral correctness.

---

## 218.5 Central Architecture Team Reviews Every Pull Request

Rejected because this creates an organizational bottleneck.

---

# 219. Related Decisions

This ADR implements and extends:

- ADR-006: Use Flyway for Database Migrations
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-048: Adopt Engineering Productivity, Developer Experience and InnerSource Standards
- ADR-049: Adopt AI-Assisted Software Engineering and Responsible AI Development Standards
- ADR-050: Adopt Enterprise Architecture Baseline and Architecture Governance Operating Model

---

# 220. References

- ArchUnit
- JUnit 5
- AssertJ
- Gradle
- JaCoCo
- SonarQube
- Flyway
- OpenAPI
- OWASP
- SLSA
- Architecture Fitness Functions
- Continuous Architecture

---

# 221. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial executable architecture and fitness-function implementation baseline |

---

# 222. Decision Summary

The architecture validation model becomes:

```text
                    ADR
                     |
                     v
             ARCHITECTURE RULE
                     |
                     v
              FITNESS FUNCTION
                     |
                     v
                  GRADLE
                     |
                     v
                    CI
                     |
              +------+------+
              |             |
             PASS           FAIL
              |             |
              v             v
           RELEASE       CORRECT
```

For Java architecture:

```text
CONTROLLER
    |
    v
SERVICE
    |
    v
REPOSITORY
```

with automated prevention of:

```text
CONTROLLER
    |
    +----------> REPOSITORY
```

when that dependency violates the approved service architecture.

For database evolution:

```text
V27
 |
 v
APPLIED
 |
 v
IMMUTABLE

Problem?
 |
 v
CREATE V28
```

For quality:

```text
CODE
 |
 +--> JUNIT
 |
 +--> ARCHUNIT
 |
 +--> JACOCO
 |
 +--> SONAR
 |
 +--> SAST
 |
 +--> DEPENDENCY SCAN
 |
 +--> CONTRACT CHECK
 |
 v
RELEASE ELIGIBILITY
```

For architecture exceptions:

```text
RULE VIOLATION
      |
      v
IS THERE AN APPROVED
NARROW EXCEPTION?
    /       \
   NO       YES
   |         |
   v         v
 FAIL     ALLOW
            |
            v
       TRACK EXPIRY
```

For traceability:

```text
ADR-XXX
   |
   v
ARCH-LAYER-001
   |
   v
ArchitectureTest
   |
   v
CI RESULT
   |
   v
SERVICE SCORECARD
```

The complete fitness-function equation is:

```text
ARCHITECTURE INTENT
       +
EXECUTABLE RULES
       +
FAST LOCAL FEEDBACK
       +
CI ENFORCEMENT
       +
QUALITY GATES
       +
SECURITY GATES
       +
CONTRACT VALIDATION
       +
TRACEABLE EXCEPTIONS
       +
SERVICE SCORECARDS
       =
CONTINUOUS ARCHITECTURE GOVERNANCE
```

The governing principle is:

```text
An architectural rule that can
be checked reliably by software
should become executable.

Developers should discover
violations while developing,
not during a late architecture
review or after production.

Human architects should spend
their time evaluating boundaries,
trade-offs and strategic decisions,
while deterministic tools enforce
deterministic rules.
```
