# ADR-035: Adopt Engineering Quality and Testing Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-035 |
| Title | Adopt Engineering Quality and Testing Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Testing, Quality Gates, JaCoCo, SonarQube, SAST, AssertJ, Mockito |
| Related Work Items | Unit Tests, Integration Tests, Coverage, Security, CI/CD |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform contains business-critical services implemented primarily with:

- Java 21
- Spring Boot
- Gradle
- PostgreSQL
- Redis
- SQS
- REST integrations
- AWS integrations
- Virtual Threads

The platform requires reliable automated verification because defects may affect:

- orders
- approvals
- customers
- billing
- integrations
- security
- financial calculations
- asynchronous processing

Automated testing is therefore part of the architecture rather than an optional development activity.

However:

```text
High Coverage
    !=
High Quality
```

A project can report:

```text
90% line coverage
```

while still failing to validate critical business behavior.

---

# 2. Problem Statement

The platform requires standards defining:

- test strategy
- unit tests
- integration tests
- repository tests
- controller tests
- contract tests
- architecture tests
- concurrency tests
- security tests
- performance tests
- Testcontainers
- Mockito
- AssertJ
- deterministic testing
- JaCoCo
- SonarQube
- SAST
- quality gates
- test naming
- fixtures
- constants
- mocking
- exception testing
- coverage exclusions
- flaky tests
- test maintainability

---

# 3. Decision Drivers

Primary drivers are:

1. business correctness
2. regression prevention
3. maintainability
4. deterministic builds
5. security
6. fast feedback
7. refactoring confidence
8. architectural integrity
9. production reliability
10. measurable quality
11. CI/CD safety
12. developer productivity

---

# 4. Decision

Testing follows a risk-based strategy:

```text
BUSINESS / TECHNICAL RISK
          |
          v
     TEST STRATEGY
          |
   +------+------+------+
   |      |      |      |
   v      v      v      v
 UNIT  INTEGRATION CONTRACT SECURITY
   |      |      |      |
   +------+------+------+
          |
          v
      COVERAGE
          |
          v
   STATIC ANALYSIS
          |
    +-----+-----+
    |           |
    v           v
 SONAR         SAST
    |           |
    +-----+-----+
          |
          v
     QUALITY GATE
```

Coverage supports quality.

Coverage does not define quality.

---

# 5. Fundamental Principle

The primary testing rule is:

```text
Test behavior that matters.

Do not test merely to increase coverage.
```

---

# 6. Test Pyramid

The preferred baseline is:

```text
              /\
             /  \
            / E2E\
           /------\
          /Contract\
         /----------\
        /Integration \
       /--------------\
      /   Unit Tests   \
     /__________________\
```

The exact distribution depends on risk.

---

# 7. Unit Tests

Unit tests should validate isolated business behavior quickly.

---

# 8. Unit Test Characteristics

Unit tests should normally be:

- fast
- deterministic
- isolated
- repeatable
- easy to diagnose

---

# 9. Unit-Test Targets

Typical targets include:

- services
- validators
- mappers
- calculators
- parsers
- factories
- domain rules
- utility components with meaningful behavior

---

# 10. Business Rules

Business rules require direct tests.

Example:

```text
PENDING_SUPERVISOR
+
SUPERVISOR
+
Allowed Segment
    |
    v
APPROVAL ALLOWED
```

must be tested as a rule, not only incidentally through controller coverage.

---

# 11. Boundary Conditions

Tests must include relevant boundaries.

Examples:

```text
0

1

MAX - 1

MAX

MAX + 1

null

blank

empty
```

according to the contract.

---

# 12. Happy Path

Every critical operation requires happy-path coverage.

---

# 13. Failure Paths

Critical failure paths also require explicit tests.

---

# 14. Exception Behavior

Tests should verify:

- exception type
- relevant message/key
- relevant domain information

without coupling to irrelevant implementation details.

---

# 15. AssertJ

AssertJ is the preferred fluent assertion library for Java tests.

---

# 16. Assertion Description

Assertions should include `.as("...")` descriptions before assertion predicates.

Example:

```java
assertThat(result)
        .as("resultado retornado pelo serviço")
        .isNotNull();
```

---

# 17. Chained Assertions

Related assertions should be chained where readability improves.

Example:

```java
assertThat(response)
        .as("resposta do pedido criado")
        .isNotNull()
        .extracting(OrderResponse::status)
        .as("status inicial do pedido")
        .isEqualTo(OrderStatus.CREATED);
```

---

# 18. Collection Assertions

Prefer semantic collection assertions.

Example:

```java
assertThat(result)
        .as("pedidos retornados para o cliente")
        .hasSize(2)
        .extracting(OrderResponse::id)
        .containsExactly(ORDER_ID_1, ORDER_ID_2);
```

---

# 19. Avoid Manual Assertion Loops

Avoid unnecessary loops solely for assertions when AssertJ provides semantic alternatives.

---

# 20. Exception Assertion

Prefer:

```java
assertThatThrownBy(() -> service.execute(request))
        .as("execução com pedido inválido")
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("invalid");
```

---

# 21. Lambda Simplicity

Exception/assertion lambdas should contain only the invocation necessary for the assertion where practical.

---

# 22. Test Naming

Tests should follow the established project convention using names beginning with:

```text
test
```

---

# 23. Recommended Naming

Examples:

```java
testCreateShouldReturnOrderWhenRequestIsValid()

testCreateShouldThrowExceptionWhenCustomerDoesNotExist()

testApproveShouldRejectCancelledOrder()
```

---

# 24. Name Intent

The test name should communicate:

```text
Operation

+

Expected Behavior

+

Relevant Condition
```

---

# 25. TestConstants

Stable reusable test values should be centralized where this improves consistency.

---

# 26. Deterministic UUIDs

Prefer deterministic constants:

```java
static final UUID ORDER_ID =
        UUID.fromString("11111111-1111-1111-1111-111111111111");
```

instead of unnecessary:

```java
UUID.randomUUID();
```

---

# 27. Why

Random values make failures harder to reproduce and rarely improve ordinary unit tests.

---

# 28. Random Testing

Randomized/property-based testing is different and may intentionally generate data when reproducibility is controlled.

---

# 29. Clock

Code dependent on current time should use an injectable time abstraction where practical.

---

# 30. `now()`

Tests should avoid fragile assumptions around uncontrolled:

```java
OffsetDateTime.now()
```

when exact temporal behavior matters.

---

# 31. Clock Injection

Prefer:

```text
Clock
```

or another controlled time provider.

---

# 32. Sleep

Arbitrary `Thread.sleep` is prohibited as the primary mechanism for test synchronization.

---

# 33. Why Sleep Is Fragile

```text
Fast Machine  -> passes

Slow CI       -> fails

Increase sleep

Build becomes slower

Still flaky
```

---

# 34. Deterministic Coordination

Prefer:

- CountDownLatch
- CyclicBarrier
- CompletableFuture
- controlled executor
- Awaitility where approved

---

# 35. Mockito

Mockito is appropriate for isolating collaborators in unit tests.

---

# 36. Mock Boundary

Mock external collaborators, not the behavior being tested.

---

# 37. Over-Mocking

A test mocking every internal method usually validates implementation rather than behavior.

---

# 38. Real Value Objects

Prefer real:

- records
- DTOs
- enums
- value objects

instead of mocking simple data containers.

---

# 39. Repository Mock

Repository mocks are appropriate for service unit tests.

---

# 40. Repository Implementation

Repository/JPA behavior requires integration testing against a real database engine where behavior matters.

---

# 41. Interaction Verification

Use:

```java
verify(...)
```

when the interaction itself is behaviorally relevant.

---

# 42. Avoid Excessive Verify

Do not verify every internal invocation merely because Mockito permits it.

---

# 43. `verifyNoMoreInteractions`

Use sparingly.

It can make tests unnecessarily coupled to harmless implementation changes.

---

# 44. ArgumentCaptor

Use `ArgumentCaptor` when the generated collaborator argument is meaningful behavior.

---

# 45. Matchers

Avoid unnecessarily broad matchers when exact values matter.

---

# 46. `any()`

`any()` can hide incorrect data propagation.

---

# 47. Strict Stubbing

Strict stubbing is preferred because unused mocks often indicate obsolete or incorrect test setup.

---

# 48. Shared Setup

Common setup may use:

```java
@BeforeEach
```

when it genuinely applies to most tests.

---

# 49. Oversized Setup

Avoid enormous `@BeforeEach` methods configuring behavior irrelevant to most tests.

---

# 50. Fixture Builder

Complex domain fixtures may use dedicated builders/factories.

---

# 51. Object Mother

Reusable test-data factories may be used when they improve clarity.

---

# 52. Production Builders

Do not modify production APIs solely to simplify test fixture creation unless the API improvement is independently justified.

---

# 53. Integration Tests

Integration tests verify real collaboration between important infrastructure components.

---

# 54. Typical Integration Targets

Examples:

- JPA + PostgreSQL
- Redis
- SQS
- HTTP serialization
- Spring Security
- Flyway
- Spring configuration

---

# 55. Testcontainers

Testcontainers is preferred when behavior depends on actual infrastructure semantics.

---

# 56. PostgreSQL Test

Use actual PostgreSQL where testing:

- native queries
- locking
- constraints
- PostgreSQL SQL behavior
- Flyway migrations

---

# 57. H2

H2 must not be assumed equivalent to PostgreSQL.

---

# 58. Database Fidelity

Database integration tests should use the same database technology as production where correctness depends on vendor behavior.

---

# 59. Redis Test

Use Redis Testcontainers when validating:

- TTL
- serialization
- cache commands
- distributed cache behavior

---

# 60. SQS Test

SQS integration testing should validate critical:

- serialization
- topic interaction
- keying
- consumer behavior

using realistic infrastructure where warranted.

---

# 61. Flyway

Flyway migrations must be validated from a clean database.

---

# 62. Migration Immutability

An existing/applied migration must never be modified to fix a later issue.

Database corrections require:

```text
New Change
    |
    v
New Flyway Migration
    |
    v
New Version
```

---

# 63. Migration Test

CI should detect:

- invalid migration order
- checksum problems
- incompatible SQL
- schema bootstrap failure

---

# 64. Controller Tests

Controller tests should validate HTTP contracts.

---

# 65. Controller Scope

Typical concerns:

- route
- HTTP method
- request validation
- response status
- JSON contract
- security
- exception mapping

---

# 66. Controller Business Logic

Do not duplicate detailed service business-rule tests at controller level unnecessarily.

---

# 67. JSON Assertions

JSON tests should validate relevant contract fields rather than the entire serialized implementation when unnecessary.

---

# 68. Contract Tests

Service-to-service contracts require explicit protection.

---

# 69. Contract Scope

Contract tests should protect:

- endpoint
- method
- request shape
- response shape
- required headers
- status semantics

---

# 70. Provider Compatibility

Provider changes must not silently break known consumers.

---

# 71. Consumer Assumptions

Consumer expectations should be explicit.

---

# 72. OpenAPI

OpenAPI contracts should participate in compatibility validation where practical.

---

# 73. Architecture Tests

Architectural boundaries should be executable where practical.

---

# 74. ArchUnit

ArchUnit may enforce rules such as:

```text
Controller
    ↓
Service
    ↓
Repository
```

and prevent forbidden dependencies.

---

# 75. Example Rule

Domain packages should not depend directly on web/controller infrastructure where Clean Architecture boundaries prohibit it.

---

# 76. Package Governance

Architecture tests may validate package conventions and dependency direction.

---

# 77. Security Tests

Security-critical behavior requires explicit automated tests.

---

# 78. Authentication Tests

Verify protected endpoints reject unauthenticated access.

---

# 79. Authorization Tests

Verify authenticated but unauthorized users are rejected.

---

# 80. Security Boundary

Do not test only:

```text
401 Unauthorized
```

and assume:

```text
403 Forbidden
```

business authorization is covered.

---

# 81. Injection

Relevant input handling should be tested against injection-style malicious data.

---

# 82. Sensitive Data

Tests should verify sensitive information is not exposed in error responses/logging where critical.

---

# 83. SAST

Static Application Security Testing is mandatory in the CI/CD quality process.

---

# 84. SAST Findings

SAST findings must be:

```text
Fixed

or

Formally Assessed
```

They must not simply be ignored.

---

# 85. Security Workaround

Do not alter valid domain data merely to silence a SAST finding.

---

# 86. Example

Changing:

```text
M&M
```

into:

```text
M&amp;M
```

at a generic backend layer is not an acceptable substitute for context-appropriate output encoding.

---

# 87. SonarQube

SonarQube is part of the mandatory quality gate.

---

# 88. Sonar Categories

The project should address:

- bugs
- vulnerabilities
- security hotspots
- code smells
- duplication
- maintainability
- coverage

according to the agreed quality profile.

---

# 89. Sonar Is Not Compiler

Passing Sonar does not prove functional correctness.

---

# 90. Sonar Workaround

Do not degrade architecture merely to remove a Sonar warning.

Resolve the underlying design issue where practical.

---

# 91. Exception Rule

Exceptions must follow the established principle:

```text
Either log or rethrow appropriately.
```

---

# 92. Duplicate Error Logging

Avoid:

```text
Repository logs error + throws

Service logs same error + throws

Controller advice logs same error
```

unless each log has distinct operational value.

---

# 93. Nullable Analysis

Potential nullability findings should be corrected through explicit contracts/control flow rather than unsafe suppression.

---

# 94. SuppressWarnings

`@SuppressWarnings` requires a legitimate reason.

---

# 95. Blanket Suppression

Broad suppressions used merely to make quality gates green are prohibited.

---

# 96. JaCoCo

JaCoCo measures test execution coverage.

---

# 97. Coverage Baseline

The platform target is:

```text
>= 80%
```

for the agreed project-level coverage metric unless a stricter service-specific gate applies.

---

# 98. Coverage Scope

Coverage should be monitored both:

```text
Globally

and

On New/Changed Code
```

where tooling permits.

---

# 99. New Code

High coverage on new code is particularly important because legacy coverage should not permanently prevent incremental improvement.

---

# 100. Coverage Is Signal

Coverage answers:

```text
Was this code executed by tests?
```

It does not answer:

```text
Was the correct behavior meaningfully verified?
```

---

# 101. Line Coverage

Line coverage alone is insufficient.

---

# 102. Branch Coverage

Branch coverage is especially relevant for:

- validators
- decision logic
- workflow rules
- status transitions
- exception paths

---

# 103. Condition Coverage

Complex boolean logic requires meaningful condition coverage.

---

# 104. Artificial Coverage

Tests that execute code without asserting behavior are prohibited as a quality strategy.

---

# 105. Example

This is insufficient:

```java
service.execute(request);
```

if no relevant result, state or interaction is verified.

---

# 106. Getter Coverage

Do not write meaningless tests solely to execute trivial generated accessors unless contract/value semantics warrant them.

---

# 107. Coverage Exclusion

Coverage exclusions require architectural justification.

---

# 108. Acceptable Exclusions

Possible examples:

- generated code
- framework bootstrap
- trivial configuration binding where separately validated

subject to project policy.

---

# 109. Unacceptable Exclusion

Do not exclude:

- complex service logic
- validators
- exception handlers
- security logic

merely because they are difficult to test.

---

# 110. Mutation Testing

Mutation testing may be introduced for high-risk business logic.

---

# 111. Purpose

Mutation testing asks:

```text
If production behavior is subtly changed,
do tests detect it?
```

---

# 112. Mutation Example

If:

```java
amount > limit
```

is mutated to:

```java
amount >= limit
```

a good boundary test should detect the difference.

---

# 113. Mutation Scope

Mutation testing is especially valuable for:

- calculations
- validators
- workflow rules
- authorization
- status transitions

---

# 114. Mutation Cost

Mutation testing may be computationally expensive and does not need to run across every class on every local build.

---

# 115. Concurrency Tests

Concurrency testing follows ADR-034.

---

# 116. Concurrent Rule

Critical parallel operations should validate:

- bounded concurrency
- failure propagation
- context propagation
- context isolation
- cancellation
- race-sensitive behavior

---

# 117. Performance Tests

Performance tests are required for critical high-volume paths.

---

# 118. Performance Is Not Unit Test

Unit benchmarks cannot substitute for system load testing.

---

# 119. Load Metrics

Measure:

- throughput
- p50
- p95
- p99
- error rate
- CPU
- memory
- DB connections
- HTTP connections

---

# 120. Regression Baseline

Critical operations should maintain comparable performance baselines where feasible.

---

# 121. N+1 Tests

Data-access performance tests should detect important N+1 regressions where practical.

---

# 122. Query Count

For selected critical use cases, query-count assertions may be appropriate.

---

# 123. Flaky Tests

Flaky tests are defects.

---

# 124. Retry Failed Test

Automatically retrying flaky tests indefinitely is not a valid fix.

---

# 125. Flaky Root Causes

Common causes include:

- timing
- uncontrolled clock
- shared state
- random data
- test order
- external dependency
- concurrency race
- port conflicts

---

# 126. Test Isolation

Tests must not depend on execution order.

---

# 127. Shared Database State

Integration tests should isolate/reset data sufficiently to remain order-independent.

---

# 128. Parallel Test Execution

Parallel test execution may be enabled only when test isolation supports it.

---

# 129. Static Mutable Test State

Mutable static test fixtures are discouraged.

---

# 130. External Internet

Automated builds should not depend unnecessarily on uncontrolled public internet services.

---

# 131. Stub Server

External HTTP integrations may use controlled stubs such as WireMock or MockWebServer where appropriate.

---

# 132. Contract Fidelity

Stubs must reflect actual provider contracts sufficiently to remain useful.

---

# 133. Golden Files

Golden/snapshot files may be used for stable complex output formats.

---

# 134. Snapshot Review

Snapshot updates must be reviewed as behavioral changes, not blindly regenerated.

---

# 135. File Tests

Fixed-layout/file integration requires tests for:

- exact length
- exact position
- encoding
- line terminators
- numeric formatting
- null/default handling

---

# 136. Locale

Tests involving locale-sensitive formatting must explicitly control locale.

---

# 137. Decimal Values

Financial/decimal tests should use `BigDecimal` semantics deliberately.

---

# 138. BigDecimal Comparison

Use scale-sensitive or scale-insensitive assertions according to the actual domain contract.

---

# 139. Floating Point

Binary floating-point types should not be used for exact financial assertions where decimal precision is required.

---

# 140. Error Contracts

Global exception handling requires tests for:

- status
- error code
- message
- correlation information
- sensitive-data absence

---

# 141. Logging Tests

Do not over-test exact log wording unless logs themselves are a contractual/operational requirement.

---

# 142. Log Sanitization

Security-sensitive sanitization/masking behavior should be tested directly.

---

# 143. Package-Private Testing

Do not weaken production visibility merely to make testing easier.

---

# 144. Private Methods

Private methods should normally be tested through public behavior.

---

# 145. Reflection

Reflection should not be the default technique for testing private implementation.

---

# 146. Extract Component

If a private method contains substantial independently meaningful behavior, consider extracting a cohesive component rather than testing it reflectively.

---

# 147. Test Production Symmetry

Not every production class requires a one-to-one test class.

---

# 148. Behavior Ownership

Tests should be organized around behavior and responsibility.

---

# 149. Duplicate Tests

Duplicated tests increase maintenance without proportionate confidence.

---

# 150. Parameterized Tests

Parameterized tests are preferred for repeated rule variants.

---

# 151. Example

Statuses:

```text
CANCELLED

APPROVED_ANALYST

APPROVED_SUPERVISOR
```

may be tested through a parameterized rule when expected behavior is identical.

---

# 152. Parameterized Test Clarity

Parameterized test display names should make failures diagnosable.

---

# 153. Test Data Size

Use the smallest fixture that demonstrates the behavior.

---

# 154. Massive Fixtures

Large object graphs make failures difficult to understand.

---

# 155. Arrange-Act-Assert

Tests should generally have a clear logical structure:

```text
Arrange

Act

Assert
```

without requiring comments when the code itself is clear.

---

# 156. One Behavior

A unit test should normally focus on one behavioral scenario.

---

# 157. Multiple Assertions

Multiple assertions are acceptable when they verify one coherent outcome.

---

# 158. One Assertion Myth

The platform does not mandate exactly one assertion per test.

---

# 159. Test Comments

Comments should explain non-obvious reasoning, not restate code.

---

# 160. Production Bug

Every meaningful production defect should trigger consideration of a regression test.

---

# 161. Regression Test

A bug fix should preferably include a test that:

```text
Fails Before Fix

Passes After Fix
```

---

# 162. Test-First Reproduction

When practical:

```text
Reproduce with test

↓

Implement fix

↓

Verify regression
```

---

# 163. Refactoring

Refactoring should preserve externally observable behavior.

---

# 164. Characterization Tests

Legacy behavior may first be protected with characterization tests before major refactoring.

---

# 165. Legacy Code

Low legacy coverage is not a reason to avoid improvement indefinitely.

---

# 166. Incremental Improvement

Modified legacy areas should receive meaningful additional coverage.

---

# 167. Test Smells

The following indicate possible design problems:

- huge setup
- excessive mocks
- dozens of constructor dependencies
- difficult fixture creation
- private-method testing pressure
- repeated static mocking

---

# 168. Design Feedback

Difficult testing is often useful architectural feedback.

---

# 169. Static Mocking

Static mocking should be exceptional.

---

# 170. Constructor Injection

Constructor injection improves explicit dependencies and testability.

---

# 171. Excessive Dependencies

Classes with excessive constructor dependencies should be reviewed for responsibility fragmentation.

---

# 172. Quality Gate

The CI/CD pipeline must enforce quality before deployment.

---

# 173. Recommended Pipeline

```text
COMPILE
   |
   v
UNIT TEST
   |
   v
INTEGRATION TEST
   |
   v
JACOCO
   |
   v
SONARQUBE
   |
   v
SAST
   |
   v
CONTRACT / SECURITY CHECKS
   |
   v
PACKAGE
   |
   v
DEPLOYMENT ELIGIBLE
```

Exact ordering may vary for pipeline efficiency.

---

# 174. Build Failure

Mandatory quality-gate failure must prevent normal promotion.

---

# 175. Manual Override

Quality-gate overrides require explicit governance and traceability.

---

# 176. No Silent Override

Developers must not silently disable:

- tests
- Sonar
- SAST
- coverage

to make a pipeline pass.

---

# 177. Disabled Tests

`@Disabled` tests require a documented temporary reason.

---

# 178. Permanent Disabled Tests

Permanently disabled tests should be removed or repaired.

---

# 179. Ignored Failure

Tests must not catch exceptions merely to prevent failure.

---

# 180. Test Exception Swallowing

This is prohibited:

```java
try {
    service.execute();
} catch (Exception ignored) {
    // ignored
}
```

unless ignoring the exception is itself the behavior under test and explicitly asserted.

---

# 181. Test Logging

Tests should not emit excessive logs during successful builds.

---

# 182. CI Diagnostics

Failures must provide enough assertion context to diagnose the issue from CI output.

---

# 183. Assertion Messages

This is one reason meaningful AssertJ `.as(...)` descriptions are required.

---

# 184. Test Runtime

The test suite must remain fast enough to provide useful developer feedback.

---

# 185. Test Segmentation

Long-running tests may be separated into:

```text
Unit

Integration

Contract

Performance
```

pipeline stages.

---

# 186. Fast Local Feedback

Developers should be able to run unit tests rapidly before full CI validation.

---

# 187. Integration Runtime

Testcontainers should be reused/configured responsibly to avoid unnecessary suite overhead while preserving isolation.

---

# 188. Coverage Optimization

Do not sacrifice test quality merely to reduce execution time.

---

# 189. Security Quality

Security testing must include both:

```text
Static Analysis

and

Behavioral Verification
```

---

# 190. Dependency Vulnerabilities

Dependency/security scanning should be part of the software-supply-chain quality process.

---

# 191. Dependency Upgrade

Security upgrades require regression validation.

---

# 192. Generated Sources

Generated sources should be clearly identified so quality tooling can treat them appropriately.

---

# 193. Lombok

Generated Lombok boilerplate does not require meaningless hand-written coverage solely to satisfy percentages.

---

# 194. Mapper Testing

Mappers with meaningful transformations require tests.

---

# 195. Trivial Mapper

Pure generated mappings may use generated-code strategies where approved.

---

# 196. Validation Testing

Bean Validation constraints should be tested at the correct boundary.

---

# 197. Duplicate Validation Tests

Do not duplicate every annotation test at:

```text
DTO Unit Test

Controller Test

Integration Test
```

without additional value.

---

# 198. Service Validation

Business validation independent of transport must remain tested at service/domain level.

---

# 199. Repository Testing

Repository tests should validate meaningful custom behavior.

---

# 200. Spring Data Generated CRUD

Do not write exhaustive tests for framework-generated CRUD merely to test Spring Data itself.

---

# 201. Custom Query

Custom queries require tests, particularly for:

- joins
- projections
- filters
- pagination
- sorting

---

# 202. Pagination

Pagination tests should validate:

- page boundaries
- total elements where contractual
- sort behavior
- empty results

---

# 203. Sorting

Public sort fields should be tested against the API contract rather than exposing internal entity paths accidentally.

---

# 204. Serialization

DTO serialization/deserialization should be tested where JSON compatibility is important.

---

# 205. Enum Compatibility

Enums exposed through APIs/events require compatibility tests.

---

# 206. SQS Event Tests

Event tests should validate:

- event type
- key
- payload
- required identifiers
- serialization
- compatibility

---

# 207. Outbox Tests

Outbox implementations should test:

- persistence with business transaction
- dispatch eligibility
- retry
- maximum attempts
- status transition
- duplicate safety

---

# 208. Idempotency Tests

Idempotent consumers/endpoints require duplicate-request/event tests.

---

# 209. Cache Tests

Caching follows ADR-032 and should test:

- hit
- miss
- eviction
- TTL
- fallback
- Redis failure
- concurrent miss

---

# 210. Resilience Tests

Resilience policies should test:

- timeout
- circuit breaker
- retry
- bulkhead
- fallback

where behavior is critical.

---

# 211. Retry Test

Verify retry count and eligibility, not merely final success.

---

# 212. Circuit Breaker Test

Circuit-breaker tests should avoid fragile real-time sleeps where state can be controlled deterministically.

---

# 213. Virtual Thread Tests

Java 21 concurrency tests follow ADR-034 and must validate behavior rather than merely checking thread names.

---

# 214. Quality Metrics

Engineering quality should be evaluated using multiple signals.

---

# 215. Quality Model

```text
Quality
  =
Business Correctness
+
Test Effectiveness
+
Coverage
+
Maintainability
+
Security
+
Architecture
+
Operational Reliability
```

---

# 216. Coverage Trend

Coverage trend is more informative than one isolated number.

---

# 217. Technical Debt

Quality findings should feed a visible technical-debt process rather than remaining indefinitely ignored.

---

# 218. Critical Findings

Critical security/correctness findings require priority remediation.

---

# 219. Clean Code

Clean code is expected, but abstraction must remain purposeful.

---

# 220. Over-Abstraction

Do not create:

```text
Interface
+
Abstract Class
+
Factory
+
Strategy
+
Adapter
```

for trivial behavior merely to appear architecturally sophisticated.

---

# 221. Duplication

Meaningful duplication should be removed when a stable shared abstraction exists.

---

# 222. Premature Abstraction

Small superficial similarity does not automatically justify abstraction.

---

# 223. Complexity

Complex methods should be decomposed by cohesive responsibility.

---

# 224. Sonar Cognitive Complexity

Cognitive-complexity findings should trigger design review rather than mechanical extraction of meaningless one-line methods.

---

# 225. Method Size

Method length alone is not the architectural criterion.

Cohesion and readability are more important.

---

# 226. Code Review

Automated tools complement code review.

They do not replace it.

---

# 227. Reviewer Responsibility

Reviewers should evaluate:

- correctness
- architecture
- security
- performance
- readability
- test quality

---

# 228. AI-Generated Code

AI-assisted code is subject to exactly the same quality gates as manually written code.

---

# 229. AI Test Generation

Generated tests must be reviewed for:

- meaningful assertions
- valid behavior
- duplication
- implementation coupling
- false confidence

---

# 230. Coverage Gaming

Generating large quantities of trivial tests solely to raise coverage is prohibited.

---

# 231. Anti-Patterns

The following are prohibited or strongly discouraged:

- treating coverage percentage as proof of quality
- tests without meaningful assertions
- tests written solely for JaCoCo
- arbitrary `Thread.sleep`
- unnecessary `UUID.randomUUID()`
- uncontrolled current time
- over-mocking
- mocking DTOs/value objects
- excessive interaction verification
- huge shared setup
- testing private methods through reflection by default
- weakening production visibility for tests
- H2 as assumed PostgreSQL equivalent
- changing an applied Flyway migration
- silently ignoring SAST findings
- changing valid domain data merely to silence security tools
- blanket `SuppressWarnings`
- disabling quality tools to pass CI
- flaky tests accepted as normal
- test-order dependency
- uncontrolled internet dependencies
- permanent disabled tests
- swallowed exceptions
- duplicate tests with no additional value
- static mocking as default
- testing framework-generated CRUD exhaustively
- arbitrary coverage exclusions
- giant fixtures
- random test data without reproducibility
- changing production behavior only to make tests pass
- blindly accepting AI-generated tests

---

# 232. Positive Consequences

The decision provides:

- stronger regression protection
- higher refactoring confidence
- deterministic builds
- better Sonar/SAST compliance
- meaningful coverage
- clearer tests
- improved security
- better architecture enforcement
- improved production reliability
- consistent Java testing conventions

---

# 233. Negative Consequences

The decision introduces:

- additional test implementation effort
- Testcontainers infrastructure cost
- longer CI pipelines
- quality-gate maintenance
- performance-test infrastructure
- security-review effort

These costs are accepted because production defects and unsafe refactoring are substantially more expensive.

---

# 234. Neutral Consequences

The decision also means:

- 100% coverage is not required universally
- some classes require more than 80%
- some trivial generated code may require less/no direct coverage
- unit tests remain the majority
- integration tests are intentionally slower
- quality is evaluated using multiple independent signals

---

# 235. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Coverage gaming | High | Medium | Behavioral review |
| Flaky tests | High | Medium | Deterministic synchronization |
| Excessive CI duration | Medium | Medium | Test segmentation |
| Mock-heavy tests | Medium | Medium | Integration testing |
| DB behavior mismatch | High | Medium | PostgreSQL Testcontainers |
| Security regression | Critical | Medium | SAST + security tests |
| Sonar workaround degrades code | High | Medium | Design-oriented remediation |
| Legacy low coverage | High | High | Incremental new-code improvement |
| Fragile implementation tests | Medium | Medium | Behavior-based assertions |
| AI-generated low-value tests | Medium | Medium | Mandatory review |

---

# 236. Implementation Guidance

The following rules are mandatory:

1. Tests must verify meaningful behavior.
2. Critical business rules require direct tests.
3. AssertJ is the preferred assertion library.
4. AssertJ assertions must use meaningful `.as("...")` descriptions.
5. Test names should follow the established `test...` convention.
6. Stable test identifiers should use deterministic constants.
7. Arbitrary `Thread.sleep` must not be used for synchronization.
8. Mockito should isolate collaborators rather than mock the subject behavior.
9. Simple DTOs/value objects should normally be real objects.
10. Integration tests must use realistic infrastructure where vendor semantics matter.
11. PostgreSQL-specific behavior should be tested against PostgreSQL.
12. Applied Flyway migrations must never be modified; fixes require new migrations.
13. Controller tests should focus on HTTP contracts.
14. Business rules should remain primarily tested at service/domain level.
15. Security-critical behavior requires explicit tests.
16. SAST findings must be fixed or formally assessed.
17. SonarQube is part of the mandatory quality gate.
18. Exceptions must not be silently swallowed.
19. JaCoCo project coverage target is at least the agreed 80% baseline unless stricter rules apply.
20. Coverage on new/changed code should be monitored separately where possible.
21. Branch coverage matters for decision-heavy logic.
22. Tests solely executing lines without assertions are not acceptable.
23. Coverage exclusions require justification.
24. Flaky tests are defects.
25. Tests must remain order-independent.
26. Production bug fixes should include regression tests where practical.
27. Architecture boundaries should be automatically tested where valuable.
28. Critical concurrent behavior must follow ADR-034 testing rules.
29. Performance-critical flows require load testing.
30. CI quality-gate failures must prevent normal promotion.
31. Quality controls must not be silently disabled.
32. AI-generated code/tests receive the same review and gates as human-written code.
33. Test quality must be reviewed independently from coverage percentage.

---

# 237. Quality Production Readiness Gate

A service/release is not production ready until:

```text
[ ] Unit tests pass

[ ] Integration tests pass

[ ] Critical business rules covered

[ ] Failure paths covered

[ ] Boundary conditions covered

[ ] Regression tests added for relevant defects

[ ] Database integration verified

[ ] Flyway validation passes

[ ] HTTP contracts verified

[ ] Security behavior tested

[ ] SQS/event contracts verified where applicable

[ ] Cache behavior verified where applicable

[ ] Concurrency behavior verified where applicable

[ ] JaCoCo gate passes

[ ] New-code coverage reviewed

[ ] Branch coverage reviewed for critical rules

[ ] SonarQube quality gate passes

[ ] SAST gate passes

[ ] Security hotspots reviewed

[ ] No unexplained disabled tests

[ ] No known flaky tests

[ ] No arbitrary test sleeps

[ ] Deterministic identifiers used where appropriate

[ ] AssertJ descriptions present

[ ] Architecture rules pass

[ ] Critical performance tests pass

[ ] Test runtime remains acceptable

[ ] Code review completed
```

---

# 238. Validation

This ADR will be validated through:

- CI/CD quality gates
- unit tests
- integration tests
- Testcontainers
- contract tests
- ArchUnit
- JaCoCo
- SonarQube
- SAST
- security tests
- concurrency tests
- mutation testing where applicable
- load tests
- code reviews
- defect escape analysis
- flaky-test monitoring

---

# 239. Success Criteria

The decision is successful when:

- critical business regressions are caught before deployment
- new code maintains meaningful coverage
- JaCoCo remains at or above the agreed baseline
- SonarQube quality gates remain green
- SAST findings are actively resolved
- test failures are deterministic and diagnosable
- refactoring does not frequently introduce regressions
- architecture violations are detected automatically
- security regressions are caught earlier
- production defects result in stronger regression protection
- coverage growth corresponds to behavioral confidence rather than artificial execution

---

# 240. Alternatives Rejected

## 240.1 Coverage Percentage as Primary Quality Measure

Rejected because execution coverage does not measure assertion quality.

---

## 240.2 100% Coverage for Every Class

Rejected because it encourages low-value tests and coverage gaming.

---

## 240.3 Unit Tests Only

Rejected because infrastructure and contract behavior cannot be fully validated through mocks.

---

## 240.4 Integration Tests Only

Rejected because feedback becomes slower and failures become harder to isolate.

---

## 240.5 H2 as Universal Database Test Substitute

Rejected because PostgreSQL-specific semantics matter.

---

## 240.6 Ignore Sonar/SAST Until Release

Rejected because late remediation is expensive and risky.

---

## 240.7 Disable Flaky Tests

Rejected as a permanent strategy because the underlying defect remains.

---

# 241. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-030: Adopt SQS Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards

---

# 242. References

- Java 21 Documentation
- JUnit 5 Documentation
- AssertJ Documentation
- Mockito Documentation
- Testcontainers Documentation
- JaCoCo Documentation
- SonarQube Documentation
- OWASP Testing Guide
- Spring Boot Testing Documentation
- Spring Security Testing Documentation
- PostgreSQL Documentation
- Amazon SQS Documentation
- ArchUnit Documentation

---

# 243. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial engineering quality baseline |

---

# 244. Decision Summary

The definitive model is:

```text
                     SOFTWARE CHANGE
                           |
                           v
                    BUSINESS RISK
                           |
                           v
                    TEST STRATEGY
                           |
       +-------------------+-------------------+
       |                   |                   |
       v                   v                   v
     UNIT             INTEGRATION          CONTRACT
       |                   |                   |
       +-------------------+-------------------+
                           |
                           v
                       SECURITY
                           |
                           v
                       COVERAGE
                           |
                 +---------+---------+
                 |                   |
                 v                   v
             SONARQUBE             SAST
                 |                   |
                 +---------+---------+
                           |
                           v
                     QUALITY GATE
                           |
                           v
                       RELEASE
```

The wrong quality model is:

```text
Write Tests
    |
    v
Reach 80%
    |
    v
QUALITY
```

The correct model is:

```text
Business Rule
      |
      v
Meaningful Scenario
      |
      v
Meaningful Assertion
      |
      v
Regression Protection
      |
      v
Coverage Confirms Execution
```

Coverage therefore means:

```text
COVERAGE
   =
Evidence that code was executed
```

not:

```text
COVERAGE
   =
Evidence that code is correct
```

For example:

```java
@Test
void testCalculateShouldApplyCorrectRule() {
    var result = calculator.calculate(INPUT);

    assertThat(result)
            .as("valor calculado para a regra informada")
            .isEqualTo(EXPECTED_VALUE);
}
```

is materially stronger than:

```java
@Test
void testCalculate() {
    calculator.calculate(INPUT);
}
```

even though both may execute the same production lines.

The quality hierarchy is:

```text
                 CORRECTNESS
                     |
                     v
               TEST QUALITY
                     |
                     v
                  COVERAGE
                     |
                     v
             STATIC ANALYSIS
                     |
                     v
              SECURITY ANALYSIS
                     |
                     v
               QUALITY GATE
```

For production defects:

```text
DEFECT
   |
   v
REPRODUCE
   |
   v
REGRESSION TEST FAILS
   |
   v
IMPLEMENT FIX
   |
   v
REGRESSION TEST PASSES
   |
   v
FULL QUALITY GATE
```

For database changes:

```text
Existing Migration
        |
        X
        |
   NEVER MODIFY
        |
        v
New Requirement / Fix
        |
        v
New Flyway Migration
        |
        v
Integration Test
```

And for Sonar/SAST:

```text
Finding
   |
   v
Understand Root Cause
   |
   +---- Real Problem ----> Fix Design / Code
   |
   +---- False Positive --> Assess / Document
```

not:

```text
Finding
   |
   v
Change Valid Business Data
   |
   v
Tool Becomes Green
   |
   v
Application Becomes Wrong
```

The definitive principle is:

```text
Coverage tells us where tests went.

Assertions tell us what they verified.

Business scenarios tell us whether
the verification actually matters.
```
