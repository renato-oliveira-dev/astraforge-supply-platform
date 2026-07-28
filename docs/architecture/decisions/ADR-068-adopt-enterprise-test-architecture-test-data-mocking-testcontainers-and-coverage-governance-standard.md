# ADR-068: Adopt Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-068 |
| Title | Adopt Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Testing, Quality Engineering, Coverage, Testcontainers, Mockito |
| Related Work Items | Java 21, Spring Boot, JUnit 5, AssertJ, Mockito, JaCoCo, Testcontainers |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Automated tests are part of the production architecture.

A codebase can report:

```text
COVERAGE = 90%
```

while still having poor protection against regressions.

For example:

```text
TEST
 |
 v
CALL METHOD
 |
 v
ASSERT NOT NULL
```

may execute substantial code while validating almost no meaningful behavior.

Conversely:

```text
LOWER COVERAGE
      +
HIGH-VALUE ASSERTIONS
      +
BOUNDARY TESTS
      +
FAILURE TESTS
      +
INTEGRATION TESTS
```

can provide substantially stronger engineering confidence.

Coverage is therefore necessary but insufficient.

The platform requires a testing strategy that evaluates:

```text
BEHAVIOR

BOUNDARIES

FAILURE MODES

INTEGRATION CONTRACTS

PERSISTENCE

CONCURRENCY

ARCHITECTURE

SECURITY

REGRESSIONS
```

rather than merely executing lines.

---

# 2. Problem Statement

The organization requires standards covering:

- unit tests
- integration tests
- controller tests
- repository tests
- client tests
- contract tests
- architecture tests
- end-to-end tests
- Testcontainers
- test fixtures
- `TestConstants`
- Object Mother
- Test Data Builder
- Mockito
- strict stubbing
- excessive mocking
- AssertJ
- assertion descriptions
- parameterized tests
- exception tests
- concurrency tests
- Virtual Threads
- deterministic synchronization
- JaCoCo
- coverage thresholds
- branch coverage
- mutation testing
- flaky tests
- test isolation
- test performance
- test naming
- test organization
- CI quality gates

---

# 3. Decision Drivers

Primary drivers are:

1. regression protection
2. deterministic builds
3. maintainability
4. meaningful coverage
5. fast feedback
6. production similarity
7. architectural confidence
8. refactoring safety
9. security validation
10. failure-path validation
11. concurrency correctness
12. CI reliability

---

# 4. Decision

The platform adopts a layered automated testing architecture.

Canonical model:

```text
                 FEW
          END-TO-END TESTS
                 /\
                /  \
               /    \
          CONTRACT /
         INTEGRATION
            TESTS
           /      \
          /        \
     COMPONENT / REPOSITORY
             TESTS
            /      \
           /        \
        UNIT TESTS
             MANY
```

The exact ratio is workload-dependent.

The governing objective is:

```text
MAXIMUM CONFIDENCE

for

REASONABLE EXECUTION COST
```

not maximum test count.

---

# 5. Fundamental Principle

```text
Test behavior,
not implementation trivia.

Test boundaries
where failures occur.

Use mocks
to isolate dependencies,
not to simulate
the entire application.

Coverage measures
execution.

Assertions measure
intent.

Mutation testing measures
whether those assertions
can detect behavioral change.
```

---

# 6. Test Pyramid

Most deterministic business behavior SHOULD be protected by fast tests.

More expensive tests SHOULD concentrate on boundaries that cannot be meaningfully validated with pure unit tests.

---

# 7. Unit Test

A unit test SHOULD validate one cohesive behavior with minimal external infrastructure.

---

# 8. Spring Context

Pure domain/unit tests SHOULD NOT start Spring merely for dependency injection.

---

# 9. Domain Test

Domain rules SHOULD normally be testable with:

```text
JUnit

AssertJ
```

without:

```text
@SpringBootTest
```

---

# 10. Application Service Test

Application services SHOULD be tested with controlled port/client/repository collaborators.

---

# 11. Controller Test

Controller tests SHOULD focus on the HTTP boundary.

Typical concerns:

```text
Request Mapping

Serialization

Deserialization

Bean Validation

Security

HTTP Status

Headers

Problem Details
```

---

# 12. Controller Business Logic

Controller tests SHOULD NOT duplicate every domain test.

---

# 13. MockMvc

Spring MVC applications SHOULD prefer focused `MockMvc` tests for controller behavior where full server startup is unnecessary.

---

# 14. WebTestClient

Reactive or appropriate HTTP components MAY use `WebTestClient`.

---

# 15. Repository Test

Repository tests SHOULD validate actual persistence behavior.

---

# 16. Mock Repository Test

Mocking `JpaRepository` does not test:

```text
JPQL

SQL

Entity Mapping

Constraints

Transactions

Indexes

Fetch Behavior
```

---

# 17. PostgreSQL Test

PostgreSQL-specific persistence behavior SHOULD be tested against PostgreSQL rather than relying exclusively on H2.

---

# 18. H2

H2 MUST NOT be assumed to be semantically equivalent to PostgreSQL.

---

# 19. Testcontainers

Testcontainers SHOULD be the preferred mechanism for integration tests requiring production-like infrastructure.

---

# 20. PostgreSQL Container

Repository/integration tests MAY use:

```text
PostgreSQL Testcontainer
```

for:

```text
Flyway

Constraints

Native Queries

JSON/JSONB

Indexes

Sequences

Locking

Transactional Behavior
```

---

# 21. Redis Container

Redis-dependent behavior SHOULD use Redis Testcontainers where actual Redis semantics matter.

---

# 22. SQS Container

SQS integration behavior SHOULD use representative queue infrastructure when serialization, FIFO MessageGroupId ordering, acknowledgement, visibility timeout or delivery semantics matter.

---

# 23. LocalStack

AWS integrations MAY use LocalStack or approved alternatives where the simulation provides sufficient semantic fidelity.

---

# 24. Real Cloud Tests

Tests requiring actual cloud infrastructure SHOULD be limited to dedicated integration environments where emulator fidelity is insufficient.

---

# 25. Container Reuse

Container reuse MAY reduce local execution time but MUST NOT compromise test isolation.

---

# 26. Shared Container

A test suite MAY share infrastructure containers when database/application state is reliably isolated between tests.

---

# 27. Container per Test

Creating a new container for every trivial test SHOULD be avoided when it unnecessarily increases build time.

---

# 28. Migration Validation

Integration tests SHOULD execute real Flyway migrations when validating database compatibility.

---

# 29. Migration Immutability

Existing applied Flyway migrations MUST NOT be modified to make tests pass.

Database corrections MUST use a new migration version.

---

# 30. Integration Test

An integration test validates interaction between real components or infrastructure boundaries.

---

# 31. Integration Scope

Integration tests SHOULD have explicit scope.

Avoid:

```text
@SpringBootTest
```

for every integration concern when a smaller test slice provides equivalent confidence.

---

# 32. Full Application Test

`@SpringBootTest` SHOULD be reserved for scenarios requiring substantial application wiring.

---

# 33. Context Count

The number of unique Spring test contexts SHOULD be controlled because excessive context variation increases build time.

---

# 34. Dirty Context

`@DirtiesContext` SHOULD be used sparingly.

---

# 35. Client Test

HTTP clients SHOULD test:

```text
Request Method

Path

Headers

Authentication

Payload

Success Mapping

Failure Mapping

Timeout Behavior
```

---

# 36. Client Framework

Tests SHOULD validate the client contract without coupling excessively to WebClient/RestClient internal implementation.

---

# 37. Mock HTTP Server

WireMock, MockWebServer or equivalent MAY be used for deterministic HTTP integration tests.

---

# 38. Contract Test

Service boundaries SHOULD have contract tests where contract drift creates material integration risk.

---

# 39. Provider Contract

Provider tests SHOULD validate published contract behavior.

---

# 40. Consumer Contract

Consumer tests SHOULD validate assumptions that the consumer materially depends upon.

---

# 41. Success Contract

Contract testing MUST NOT cover only successful `2xx` behavior.

---

# 42. Failure Contract

Critical contracts SHOULD include:

```text
400

401

403

404

409

429

5xx

Problem Details
```

where applicable.

---

# 43. Messaging Contract

SQS contracts SHOULD validate:

```text
Event Type

Schema

Required Fields

Compatibility

Serialization

Deserialization
```

---

# 44. Architecture Test

Architecture rules SHOULD be tested using ArchUnit or equivalent fitness functions.

---

# 45. Architecture Is Testable

Examples:

```text
Domain must not depend on WebClient

Controllers must not use repositories directly

Packages must not contain cycles

JPA entities must not be exposed by controllers
```

---

# 46. End-to-End Test

End-to-end tests SHOULD be reserved for high-value user/business journeys.

---

# 47. E2E Explosion

The entire regression suite MUST NOT depend primarily on slow, fragile end-to-end tests.

---

# 48. Test Data

Test data MUST be deterministic and understandable.

---

# 49. Random UUID

Random identifiers SHOULD NOT be used when randomness provides no testing value.

Avoid:

```java
UUID.randomUUID()
```

for stable test fixtures.

---

# 50. TestConstants

Stable identifiers SHOULD be centralized when reused across tests.

Example:

```java
public final class TestConstants {

    public static final UUID ORDER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID CUSTOMER_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private TestConstants() {
    }
}
```

---

# 51. TestConstants Scope

`TestConstants` MUST NOT become an uncontrolled global dumping ground.

---

# 52. Local Constant

Values used by one test class SHOULD normally remain local to that test.

---

# 53. Shared Constant

Values representing common stable fixtures MAY belong in `TestConstants`.

---

# 54. Magic Test Values

Repeated unexplained literals SHOULD be replaced by meaningful constants/builders.

---

# 55. Test Fixture

Fixtures SHOULD expose the minimum data required to understand the scenario.

---

# 56. Fixture Noise

Tests SHOULD NOT require dozens of irrelevant fields merely because a production DTO has many properties.

---

# 57. Object Mother

Object Mother MAY be used for common valid baseline objects.

Example:

```text
OrderMother.validOrder()
```

---

# 58. Test Data Builder

Test Data Builder SHOULD be considered when individual tests need controlled variations.

Example:

```java
OrderTestBuilder.anOrder()
        .withStatus(PENDING_ANALYST)
        .withCustomerId(CUSTOMER_ID)
        .build();
```

---

# 59. Builder Defaults

Builder defaults MUST represent valid, unsurprising test state.

---

# 60. Invalid Fixture

Tests SHOULD modify only the field relevant to the invalid scenario where practical.

---

# 61. Fixture Mutation

Shared mutable fixtures MUST NOT leak state between tests.

---

# 62. Immutable Fixture

Immutable test fixtures SHOULD be preferred.

---

# 63. Test Independence

Tests MUST be independently executable.

---

# 64. Execution Order

Tests MUST NOT depend on execution order.

---

# 65. Previous Test State

A test MUST NOT require another test to execute first.

---

# 66. Database Isolation

Database integration tests MUST isolate persistent state.

---

# 67. Cleanup

Cleanup strategies MAY include:

```text
Transaction Rollback

TRUNCATE

Targeted Delete

Fresh Schema
```

depending on semantics.

---

# 68. Transaction Rollback Caveat

Transactional test rollback MUST NOT hide commit-time behavior that the test intends to validate.

---

# 69. Time

Tests involving time SHOULD use controllable clocks.

---

# 70. Clock

Prefer injecting:

```java
Clock
```

over directly calling:

```java
OffsetDateTime.now()
```

inside deterministic business rules.

---

# 71. Sleep

Tests MUST NOT use `Thread.sleep()` merely to wait for asynchronous behavior.

---

# 72. Polling

When eventual completion is genuinely asynchronous, bounded polling MAY be used.

---

# 73. Poll Timeout

Polling MUST have explicit maximum timeout.

---

# 74. Mockito

Mockito SHOULD be used to isolate true collaborators.

---

# 75. Mock Value Object

Do not mock simple:

```text
DTOs

Records

Value Objects

Collections
```

when constructing real instances is simpler.

---

# 76. Mock Domain Entity

Domain entities SHOULD normally be real objects rather than mocks.

---

# 77. Mock Count

A unit test requiring many unrelated mocks SHOULD trigger review of production-class cohesion.

---

# 78. Mock Count Is Signal

There is no universal maximum number of mocks.

The relevant question is:

```text
WHY DOES THIS CLASS
NEED ALL THESE
COLLABORATORS?
```

---

# 79. Mocking Internal Methods

Tests SHOULD NOT mock private/internal methods of the class under test.

---

# 80. Spy

Mockito spies SHOULD be used sparingly.

---

# 81. Partial Mock

Frequent need for partial mocking often indicates unclear class responsibilities.

---

# 82. Static Mock

Static mocking SHOULD be avoided when dependency injection or explicit abstractions provide cleaner design.

---

# 83. Constructor Injection

Constructor-injected dependencies improve production design and testability.

---

# 84. Strict Stubbing

Mockito strict stubbing SHOULD remain enabled.

---

# 85. Unused Stub

Unused stubs SHOULD be removed rather than globally enabling lenient mode.

---

# 86. Lenient

`lenient()` SHOULD be exceptional and justified.

---

# 87. Broad Matcher

Overly broad matchers SHOULD be avoided when exact input is part of the behavior under test.

---

# 88. `any()`

Use:

```java
any()
```

only when the exact argument genuinely does not matter.

---

# 89. ArgumentCaptor

`ArgumentCaptor` SHOULD be used when validating a meaningful object passed to a collaborator.

---

# 90. Captor Overuse

Do not capture every argument merely because Mockito supports it.

---

# 91. Verify

Interaction verification SHOULD be used when the interaction itself is behaviorally meaningful.

---

# 92. Excessive Verify

Tests SHOULD NOT duplicate implementation structure through extensive `verify(...)` calls.

---

# 93. Never

`never()` is valuable when proving that an invalid operation does not invoke an external side effect.

---

# 94. Verification Order

`InOrder` SHOULD be used only when invocation order is contractually meaningful.

---

# 95. Stub Exception

Mocks MAY simulate dependency failures to validate application error behavior.

---

# 96. Checked Exception Mocking

Tests MUST respect actual Java method exception contracts.

---

# 97. AssertJ

AssertJ is the preferred fluent assertion library for Java tests.

---

# 98. Assertion Description

Applicable AssertJ assertion chains MUST contain meaningful:

```java
.as("...")
```

descriptions.

---

# 99. Description Position

Description MUST precede the assertion predicate.

Prefer:

```java
assertThat(result.status())
        .as("order status should be approved")
        .isEqualTo(APPROVED);
```

---

# 100. Meaningful Description

Avoid meaningless descriptions such as:

```java
.as("test")
```

---

# 101. Failure Intent

A description SHOULD explain what behavior failed.

Example:

```java
.as("cancelled orders must not be approved")
```

---

# 102. Assert Chain

Related assertions SHOULD be chained where readability improves.

---

# 103. `assertThatThrownBy`

Exception behavior SHOULD use expressive AssertJ exception assertions.

Example:

```java
assertThatThrownBy(() -> service.approve(order))
        .as("cancelled order approval should be rejected")
        .isInstanceOf(InvalidOrderTransitionException.class)
        .hasMessageContaining("CANCELLED");
```

---

# 104. One Invocation Lambda

Exception assertion lambdas SHOULD contain one meaningful invocation when practical.

---

# 105. AssertJ SoftAssertions

`SoftAssertions` MAY be used when reporting multiple independent failures in one object materially improves diagnostics.

---

# 106. Multiple Assertions

Tests SHOULD NOT combine unrelated behaviors merely to reduce test count.

---

# 107. Test Naming

Test names MUST describe behavior.

---

# 108. `test*` Convention

Where established project convention requires it, test method names SHOULD begin with:

```text
test
```

---

# 109. Example

Prefer:

```java
testApproveShouldRejectCancelledOrder()
```

over:

```java
test1()
```

---

# 110. Given-When-Then

Tests SHOULD have a clear conceptual:

```text
GIVEN

WHEN

THEN
```

structure.

Explicit comments are optional when the code already communicates these sections.

---

# 111. Arrange-Act-Assert

Arrange-Act-Assert is equally acceptable.

---

# 112. One Behavioral Reason

A test SHOULD have one primary behavioral reason to fail.

---

# 113. Parameterized Test

Parameterized tests SHOULD be used when the same behavior must be verified for multiple inputs.

---

# 114. Repeated Copy-Paste Tests

Repeated tests differing only by one input SHOULD be considered for parameterization.

---

# 115. `@ValueSource`

Simple values MAY use:

```java
@ValueSource
```

---

# 116. `@EnumSource`

Enum rules SHOULD consider:

```java
@EnumSource
```

---

# 117. `@MethodSource`

Complex scenarios SHOULD use:

```java
@MethodSource
```

with readable arguments.

---

# 118. Parameterized Display Name

Parameterized tests SHOULD provide useful scenario names.

---

# 119. Parameter Explosion

A parameterized test with a difficult-to-understand matrix SHOULD be split into coherent behavioral groups.

---

# 120. Null Test

Null handling SHOULD be tested where null is allowed or realistically enters the boundary.

---

# 121. Impossible Null

Tests SHOULD NOT spend excessive effort on impossible internal null states already guaranteed by architecture/type/contracts.

---

# 122. Boundary Value

Validation tests SHOULD include meaningful boundaries.

Example:

```text
MAX_LENGTH - 1

MAX_LENGTH

MAX_LENGTH + 1
```

---

# 123. Happy Path

Every material use case SHOULD have at least one successful-path test.

---

# 124. Negative Path

Material business rules MUST have negative-path tests.

---

# 125. Failure Path

External integration behavior SHOULD test relevant failure paths.

---

# 126. Null Return from Mock

Mocks SHOULD only return unrealistic nulls when explicitly testing defensive behavior against such conditions.

---

# 127. Concurrency Tests

Concurrency-sensitive code MUST have deterministic tests where feasible.

---

# 128. Virtual Thread Test

Virtual Thread code SHOULD test behavior rather than relying on thread names or scheduler internals unless those details are the requirement.

---

# 129. Latch

`CountDownLatch` MAY coordinate deterministic concurrency scenarios.

---

# 130. Barrier

`CyclicBarrier` or equivalent MAY coordinate simultaneous execution.

---

# 131. CompletableFuture

Controlled `CompletableFuture` instances MAY simulate delayed completion without sleeps.

---

# 132. Atomic Counters

Atomic counters MAY validate maximum observed concurrency.

---

# 133. Bounded Concurrency Test

A concurrency limiter SHOULD test that observed parallelism never exceeds the configured bound.

---

# 134. Example Concept

```text
REQUEST 1 ----\
REQUEST 2 -----\
REQUEST 3 ------> LIMIT = 3
REQUEST 4 -----/  WAITS
REQUEST 5 ----/
```

---

# 135. Race Test

Concurrency tests SHOULD repeatedly exercise critical race-sensitive behavior where deterministic orchestration alone is insufficient.

---

# 136. Timing Assertion

Avoid fragile assertions such as:

```text
operation must finish in exactly 100ms
```

---

# 137. Performance Timing

Timing tests MUST use realistic tolerance and appropriate environments.

---

# 138. Deadlock Test

Concurrency-sensitive components SHOULD have bounded timeout tests capable of detecting deadlocks.

---

# 139. Security Context Propagation

Async/Virtual Thread execution SHOULD test propagation of security/request context when required by architecture.

---

# 140. Context Leakage

Tests SHOULD verify that context from one request does not leak into another asynchronous execution.

---

# 141. JaCoCo

JaCoCo is the standard code-coverage measurement mechanism.

---

# 142. Minimum Coverage

The standard project quality gate is:

```text
>= 80%
```

unless a stricter project threshold applies.

---

# 143. Coverage Dimensions

Projects SHOULD evaluate:

```text
Instruction Coverage

Line Coverage

Branch Coverage
```

where meaningful.

---

# 144. Branch Coverage

Branch coverage is especially important for:

```text
Validation

Policies

State Machines

Error Handling

Fallback Logic
```

---

# 145. Coverage Is Not Goal

The objective is NOT:

```text
MAKE JACOCO GREEN
```

The objective is:

```text
PROTECT PRODUCTION BEHAVIOR
```

with coverage serving as one signal.

---

# 146. Coverage Gaming

The following are prohibited:

```text
Meaningless assertions

Calling methods only for coverage

Ignoring results

Reflection solely to execute private methods

Artificial tests with no behavioral expectation
```

---

# 147. Exclusions

Coverage exclusions MUST be deliberate and minimal.

---

# 148. DTO Exclusion

DTOs MUST NOT automatically be excluded merely because they appear simple if they contain behavior requiring tests.

---

# 149. Generated Code

Generated code MAY be excluded when testing it provides little value and generator behavior is outside project ownership.

---

# 150. Configuration Code

Configuration classes SHOULD be tested when custom logic/wiring has meaningful failure risk.

---

# 151. Mapper Coverage

Custom mapper logic SHOULD have tests.

---

# 152. Lombok

Generated Lombok boilerplate SHOULD NOT drive meaningless tests solely for coverage.

---

# 153. Coverage Regression

CI SHOULD prevent material coverage regression below approved thresholds.

---

# 154. Changed Code

Coverage of changed/new code SHOULD receive particular attention even when global coverage remains above threshold.

---

# 155. New Code

New production behavior SHOULD normally arrive with corresponding tests.

---

# 156. Mutation Testing

Mutation testing SHOULD be considered for critical domain/business-rule modules.

---

# 157. Mutation Concept

Mutation testing changes code such as:

```text
>
```

to:

```text
>=
```

or:

```text
true
```

to:

```text
false
```

and checks whether tests detect the change.

---

# 158. Surviving Mutation

A surviving meaningful mutation indicates that the test suite may execute code without adequately asserting its behavior.

---

# 159. Mutation Tool

PIT or another approved Java mutation-testing tool MAY be used.

---

# 160. Mutation Scope

Mutation testing SHOULD initially target:

```text
Business Rules

Calculators

Validators

Policies

State Transitions
```

rather than the entire codebase indiscriminately.

---

# 161. Mutation CI Cost

Mutation testing MAY run:

```text
Nightly

Scheduled

On Critical Modules

On Changed Code
```

when full execution is too expensive for every pull request.

---

# 162. Mutation Score

Mutation score SHOULD be used as a quality signal rather than a universal arbitrary target.

---

# 163. Equivalent Mutation

Equivalent/non-actionable mutations SHOULD be reviewed rather than forcing meaningless tests.

---

# 164. Flaky Test

A flaky test is one whose result changes without a corresponding production-code change.

---

# 165. Flaky Test Policy

Flaky tests are defects.

---

# 166. Retry Test

Automatically retrying failed tests MUST NOT become the permanent solution for flakiness.

---

# 167. Retry Diagnostic

Temporary retry MAY be used diagnostically while the root cause is being corrected.

---

# 168. Common Flaky Causes

Common causes include:

```text
Thread.sleep

Shared State

Execution Order

Real Clock

Random Data

Uncontrolled Network

Port Collision

Race Conditions

External Cloud Dependency

Timezone Assumptions
```

---

# 169. Quarantine

A critical flaky test MAY be temporarily quarantined only with:

```text
Owner

Reason

Tracking Item

Removal Deadline
```

---

# 170. Disabled Test

`@Disabled` tests MUST have a documented reason and remediation plan.

---

# 171. Permanent Disabled Test

Permanently disabled tests SHOULD be deleted if they no longer represent intended behavior.

---

# 172. Test Failure

A failing test MUST NOT be weakened merely to make CI pass unless the expected behavior genuinely changed.

---

# 173. Assertion Removal

Removing an assertion to eliminate a failure is prohibited when the assertion still represents valid behavior.

---

# 174. Production Change

When requirements change:

```text
UPDATE EXPECTED BEHAVIOR

THEN

UPDATE TEST
```

rather than blindly preserving obsolete expectations.

---

# 175. Test Performance

Test execution time is an engineering concern.

---

# 176. Fast Feedback

Pull-request test suites SHOULD provide feedback quickly enough to support iterative development.

---

# 177. Test Segmentation

Suites MAY be separated into:

```text
Unit

Integration

Contract

Architecture

E2E

Performance

Mutation
```

---

# 178. PR Pipeline

Pull requests SHOULD execute all tests necessary to provide safe merge confidence within practical time limits.

---

# 179. Scheduled Suite

Very expensive:

```text
Performance

Mutation

Extended E2E
```

tests MAY additionally run on scheduled pipelines.

---

# 180. Parallel Test Execution

Independent tests MAY run concurrently when isolation is guaranteed.

---

# 181. Shared State and Parallelism

Tests relying on shared mutable static state MUST NOT be parallelized without explicit synchronization/isolation.

---

# 182. Database Parallel Tests

Parallel database tests MUST prevent cross-test data collision.

---

# 183. Unique Test Data

Where concurrent tests share infrastructure, deterministic unique fixture namespaces MAY be used.

---

# 184. Build Cache

Build/test caching MAY be used where inputs and outputs remain correctly declared.

---

# 185. Test Report

CI MUST retain useful test failure reports.

---

# 186. Failure Diagnostics

A test failure SHOULD provide enough information to understand:

```text
Expected behavior

Actual behavior

Relevant scenario
```

without rerunning locally merely to discover the assertion meaning.

---

# 187. Assertion Message

Meaningful AssertJ `.as(...)` descriptions are part of failure diagnostics.

---

# 188. Logs in Tests

Tests SHOULD NOT emit excessive production logs during successful execution.

---

# 189. Expected Exception Logging

Tests SHOULD avoid polluting build output with expected stack traces where logging configuration can safely suppress them.

---

# 190. Test Package

Test package structure SHOULD generally mirror relevant production package structure.

---

# 191. Test Utility Package

Reusable test utilities SHOULD live in explicitly test-only packages/source sets.

---

# 192. Production Dependency

Production code MUST NOT depend on test utilities.

---

# 193. Test Utility Quality

Test utilities are code and SHOULD follow maintainability standards.

---

# 194. Test Builder Duplication

Common fixture-building behavior SHOULD be consolidated when semantics are truly shared.

---

# 195. Test Abstraction

Do not create elaborate test frameworks that make individual scenarios difficult to understand.

---

# 196. Hidden Setup

A test SHOULD make behaviorally relevant setup visible.

---

# 197. Base Test Class

Large inheritance hierarchies of test base classes SHOULD be avoided.

---

# 198. Composition

Reusable test fixtures/utilities SHOULD generally prefer composition over deep test inheritance.

---

# 199. Reflection

Reflection MUST NOT be used merely to access private production methods for coverage.

---

# 200. Private Method

If a private method contains substantial independently test-worthy logic, production design SHOULD be reviewed.

---

# 201. Package-Private Testing

Package-private collaborators MAY be tested directly when they represent meaningful units and broader visibility is unnecessary.

---

# 202. Production Visibility

Production methods MUST NOT be made public solely to enable tests.

---

# 203. Exception Tests

Exception tests SHOULD verify:

```text
Type

Semantic Error Code

Relevant Context

Cause
```

where appropriate.

---

# 204. Exact Message

Tests SHOULD avoid exact full exception-message assertions unless wording itself is contractual.

---

# 205. i18n Tests

Localized messages SHOULD be tested separately from machine-readable failure semantics.

---

# 206. Problem Details Tests

REST failure tests SHOULD validate stable fields rather than incidental JSON ordering.

---

# 207. JSON Assertion

JSON tests SHOULD assert only fields relevant to the scenario unless full contract equality is intentionally required.

---

# 208. Snapshot Testing

Snapshot/golden-file testing MAY be used for stable complex outputs but MUST be reviewed carefully when snapshots change.

---

# 209. Blind Snapshot Update

Updating a snapshot merely because the test failed is prohibited.

---

# 210. Database Query Test

Critical repository tests SHOULD verify query semantics using representative datasets.

---

# 211. N+1 Regression

Performance-sensitive queries SHOULD have tests capable of detecting N+1 regressions where practical.

---

# 212. Query Count

Query-count assertions MAY be used for stable critical retrieval paths.

---

# 213. Test Data Volume

Tests targeting pagination/performance behavior SHOULD use enough data to cross relevant boundaries.

---

# 214. Pagination Boundary

For page size 20, tests SHOULD consider scenarios such as:

```text
0

1

19

20

21

40

41
```

where appropriate.

---

# 215. Bulk Boundary

For bulk limit `N`, test:

```text
N - 1

N

N + 1
```

---

# 216. Resilience Tests

Circuit Breaker tests SHOULD validate:

```text
CLOSED

OPEN

HALF_OPEN
```

where custom behavior depends on these states.

---

# 217. Retry Tests

Retry tests MUST verify bounded attempts.

---

# 218. Retry Sleep

Tests SHOULD configure retry delays to deterministic minimal values or use controllable mechanisms.

---

# 219. Timeout Tests

Timeout tests SHOULD avoid depending on long real-world waits.

---

# 220. Cache Tests

Cache tests SHOULD validate:

```text
Hit

Miss

Put

Eviction

Fallback

Serialization Failure

Primary Cache Failure
```

where applicable.

---

# 221. Cache Implementation

Actual Redis semantics SHOULD use integration tests when in-memory mocks cannot provide sufficient confidence.

---

# 222. Messaging Tests

Messaging tests SHOULD validate both:

```text
Producer Contract

Consumer Behavior
```

where the service owns both concerns.

---

# 223. Outbox Tests

Transactional Outbox tests SHOULD validate:

```text
Persistence

Status Transition

Retry

Max Attempts

next_attempt_at

sent_at

Failure Persistence
```

---

# 224. Transaction Test

Tests SHOULD verify transactional behavior where rollback/atomicity is part of the requirement.

---

# 225. Mock Transaction

Mocking repositories alone is insufficient to prove transaction rollback.

---

# 226. Security Test

Protected endpoints MUST test:

```text
Unauthenticated

Unauthorized

Authorized
```

behavior as applicable.

---

# 227. JWT Test

JWT tests SHOULD use deterministic claims representing required authorization scenarios.

---

# 228. Security Context

Tests SHOULD avoid depending unnecessarily on a globally mutable SecurityContext.

---

# 229. SAST Regression

Security fixes SHOULD include regression tests where behavior is testable.

---

# 230. Sanitization Test

Sanitization/masking tests SHOULD verify:

```text
Bearer token masking

Control-character handling

Maximum length

Null handling
```

without corrupting legitimate business values.

---

# 231. Legitimate Data Test

A regression test SHOULD ensure legitimate values such as:

```text
M&M
```

remain unchanged when no rendering-context escaping is required.

---

# 232. Sonar

Test code MUST also comply with applicable Sonar quality rules.

---

# 233. Test Smell

Sonar findings in test code MUST NOT automatically be dismissed merely because the code is under:

```text
src/test
```

---

# 234. Test Exception Handling

Test helper catch blocks MUST follow the same principle:

```text
handle meaningfully
or
rethrow
```

---

# 235. Empty Catch

Empty catch blocks in tests are prohibited.

---

# 236. AssertJ Sonar Convention

AssertJ assertions MUST consistently use meaningful `.as(...)` descriptions according to established project convention.

---

# 237. Quality Gate

A merge SHOULD be blocked when:

```text
Tests fail

Required coverage fails

Architecture tests fail

Critical contract tests fail

Required SAST/Sonar gate fails
```

---

# 238. Coverage Waiver

A temporary coverage waiver MUST have explicit technical justification.

---

# 239. Test Review Checklist

Code review SHOULD evaluate:

```text
[ ] Does the new behavior have tests?

[ ] Is the correct test level being used?

[ ] Could this be a pure unit test instead of SpringBootTest?

[ ] Does persistence behavior need Testcontainers?

[ ] Are production-specific SQL features tested against PostgreSQL?

[ ] Are mocks limited to actual collaborators?

[ ] Is excessive mock count revealing a God Service?

[ ] Are test IDs deterministic?

[ ] Are shared constants meaningful?

[ ] Would a Test Data Builder improve readability?

[ ] Are fixtures isolated?

[ ] Does the test depend on execution order?

[ ] Is Thread.sleep used?

[ ] Are concurrency tests deterministic?

[ ] Are AssertJ descriptions meaningful?

[ ] Are exception assertions specific?

[ ] Are parameterized tests appropriate?

[ ] Are boundary values tested?

[ ] Are negative/failure paths tested?

[ ] Does the test verify behavior rather than implementation?

[ ] Is coverage meaningful?

[ ] Could mutations survive because assertions are weak?

[ ] Is the test flaky?

[ ] Does the test unnecessarily start infrastructure?

[ ] Does a test change weaken valid production expectations?
```

---

# 240. Architecture Fitness Functions

Stable test architecture rules SHOULD be automated.

Examples:

```text
[ ] Domain unit tests do not require Spring context

[ ] Architecture tests execute in CI

[ ] Required JaCoCo threshold is enforced

[ ] Testcontainers integration suite executes real Flyway migrations

[ ] Disabled tests are reported

[ ] No production package depends on test packages

[ ] Required test naming convention is enforced

[ ] Critical boundary tests exist

[ ] Test results are published by CI
```

---

# 241. Enterprise Test Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] Critical business rules have no tests

[ ] Coverage is achieved through meaningless assertions

[ ] Repository SQL is tested only with mocked repositories

[ ] PostgreSQL-specific behavior relies solely on H2 tests

[ ] Tests modify existing Flyway migrations

[ ] Tests depend on execution order

[ ] Shared mutable fixtures leak between tests

[ ] Thread.sleep is used for routine synchronization

[ ] Random UUIDs make deterministic fixtures difficult to diagnose

[ ] Mockito lenient mode hides unnecessary stubs globally

[ ] Tests mock value objects unnecessarily

[ ] One unit test requires excessive unrelated mocks without architectural review

[ ] AssertJ assertions lack required meaningful descriptions

[ ] Concurrency limits are untested

[ ] Critical failure paths are untested

[ ] JaCoCo falls below approved threshold

[ ] Flaky tests are permanently retried instead of fixed

[ ] @Disabled tests have no remediation plan

[ ] Production visibility is increased solely for testing

[ ] Reflection is used solely to execute private methods for coverage
```

---

# 242. Anti-Patterns

The following are prohibited or strongly discouraged:

- `@SpringBootTest` for every test
- H2 assumed equivalent to PostgreSQL
- mocked repository tests presented as SQL integration tests
- random UUIDs without testing purpose
- giant global `TestConstants`
- mutable shared test fixtures
- test-order dependencies
- `Thread.sleep` synchronization
- unlimited polling
- mocking records/value objects
- excessive spies
- static mocking as default design
- global Mockito leniency
- unused stubs
- excessive interaction verification
- tests coupled to private methods
- reflection solely for coverage
- public production methods created solely for tests
- meaningless `.as("test")`
- assertions without behavioral meaning
- calling methods only to increase JaCoCo
- deleting valid assertions to make CI green
- permanently retrying flaky tests
- permanent unexplained `@Disabled`
- blind snapshot regeneration
- exact timing assertions in ordinary CI
- enormous test inheritance hierarchies
- tests that require reading many helper classes to understand basic setup

---

# 243. Positive Consequences

The decision provides:

- stronger regression protection
- more deterministic builds
- better test diagnostics
- production-like persistence validation
- safer refactoring
- improved concurrency confidence
- better Sonar compliance
- meaningful JaCoCo governance
- lower flaky-test rates
- clearer fixture patterns
- reduced unnecessary Spring startup
- improved CI reliability

---

# 244. Negative Consequences

The decision introduces:

- Testcontainers infrastructure cost
- additional fixture maintenance
- contract-test maintenance
- architecture-test maintenance
- coverage governance
- occasional mutation-testing cost
- explicit flaky-test remediation

These costs are accepted because automated tests are a primary control against production regression.

---

# 245. Neutral Consequences

The decision also means:

- 100% coverage is not required
- 80% coverage does not prove correctness
- not every class requires a Spring test
- not every collaborator should be mocked
- not every integration requires full E2E testing
- not every mutation must be killed
- not every test fixture belongs in `TestConstants`
- not every repeated test requires parameterization
- slower integration tests remain necessary for selected boundaries

---

# 246. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| False confidence from coverage | High | High | Behavioral + mutation tests |
| Flaky CI | High | Medium | Deterministic synchronization |
| Slow build | Medium | High | Test segmentation |
| Mock overuse | Medium | High | Test architecture review |
| DB incompatibility | High | Medium | PostgreSQL Testcontainers |
| Fixture complexity | Medium | Medium | Builders/Object Mothers |
| Concurrency regression | High | Medium | Deterministic concurrency tests |
| Contract drift | High | Medium | Contract testing |
| Test maintenance burden | Medium | Medium | Test code quality standards |
| Coverage gaming | High | Medium | Review + mutation testing |

---

# 247. Implementation Guidance

The following rules are mandatory:

1. Tests must validate behavior rather than merely execute code.
2. Pure domain tests must avoid unnecessary Spring context.
3. Controller tests must focus on HTTP boundary behavior.
4. Repository semantics must be tested against representative persistence infrastructure.
5. PostgreSQL-specific behavior should use PostgreSQL Testcontainers.
6. Real Flyway migrations should execute in applicable integration tests.
7. Applied migrations must never be modified to satisfy tests.
8. Test data must be deterministic unless randomness is itself under test.
9. Stable shared identifiers should use controlled constants.
10. `TestConstants` must remain cohesive.
11. Builders/Object Mothers should simplify complex fixture construction.
12. Shared mutable fixtures must be avoided.
13. Tests must not depend on execution order.
14. Time-dependent logic should use controllable clocks.
15. `Thread.sleep` must not be used for ordinary synchronization.
16. Mockito should mock actual external collaborators, not simple data objects.
17. Strict stubbing should remain enabled.
18. Global Mockito leniency is prohibited.
19. Excessive mock count must trigger production-design review.
20. AssertJ is the preferred fluent assertion framework.
21. Applicable AssertJ chains must contain meaningful `.as("...")` descriptions.
22. Test method names should follow established `test*` convention.
23. Parameterized tests should replace meaningful copy-paste scenario repetition.
24. Boundary values must be tested for validations and limits.
25. Critical negative/failure paths must be tested.
26. Concurrency tests must use deterministic synchronization.
27. Virtual Thread tests must validate observable behavior and concurrency limits.
28. JaCoCo minimum coverage must remain at or above the approved project threshold, normally 80%.
29. Coverage must not be increased through meaningless tests.
30. New/changed code must receive particular coverage attention.
31. Mutation testing should target critical business logic where valuable.
32. Flaky tests must be treated as defects.
33. Permanent test retries must not hide flakiness.
34. Disabled tests must have explicit justification and remediation.
35. Production visibility must not be increased solely for tests.
36. Reflection must not be used merely to test private methods.
37. Contract tests must include important failure behavior.
38. Security fixes should receive regression tests where applicable.
39. Test code must comply with applicable Sonar/SAST rules.
40. CI must block merges when mandatory test quality gates fail.

---

# 248. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- JUnit 5
- AssertJ
- Mockito
- MockMvc
- WebTestClient
- Testcontainers
- PostgreSQL
- Redis
- SQS
- LocalStack where appropriate
- WireMock
- MockWebServer
- Flyway
- ArchUnit
- JaCoCo
- PIT
- SonarQube
- SAST
- CI quality gates

---

# 249. Success Criteria

The decision is successful when:

- tests remain deterministic
- business rules can be tested without full application startup
- repository behavior is validated against production-like databases
- critical integration contracts are protected
- test fixtures become easier to understand
- excessive mock usage decreases
- AssertJ failures clearly explain violated behavior
- concurrency tests no longer depend on sleeps
- JaCoCo remains at or above the approved threshold
- new-code coverage remains strong
- mutation testing identifies weak assertions in critical rules
- flaky tests decrease
- test execution remains fast enough for productive pull-request feedback
- architecture regressions are detected automatically
- production refactoring becomes safer

---

# 250. Alternatives Rejected

## 250.1 Coverage Percentage as Sole Quality Metric

Rejected because code execution does not prove behavioral verification.

---

## 250.2 100% Coverage Requirement

Rejected as a universal rule because it incentivizes low-value tests and coverage gaming.

---

## 250.3 Full Spring Context for Every Test

Rejected because it increases feedback time and hides component boundaries.

---

## 250.4 H2 for All Persistence Tests

Rejected because production PostgreSQL semantics can differ materially.

---

## 250.5 Mock Everything

Rejected because excessive mocking tests implementation structure instead of real behavior.

---

## 250.6 E2E-Heavy Strategy

Rejected because E2E tests are slower, more fragile and less diagnostic.

---

## 250.7 Retry Flaky Tests Forever

Rejected because retries conceal nondeterministic defects.

---

# 251. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard

---

# 252. References

- Java 21 Documentation
- Spring Boot Testing Documentation
- JUnit 5 Documentation
- AssertJ Documentation
- Mockito Documentation
- Testcontainers Documentation
- PostgreSQL Documentation
- Flyway Documentation
- ArchUnit Documentation
- JaCoCo Documentation
- PIT Mutation Testing Documentation
- SonarQube Documentation
- OWASP Testing Guide
- Martin Fowler — Test Pyramid
- Growing Object-Oriented Software, Guided by Tests
- xUnit Test Patterns

---

# 253. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise test architecture and coverage-governance baseline |

---

# 254. Decision Summary

The test architecture becomes:

```text
                    E2E
                     |
                  CONTRACT
                     |
                INTEGRATION
                     |
             COMPONENT / SLICE
                     |
                   UNIT
```

Testing responsibility becomes:

```text
DOMAIN
  |
  +--> PURE UNIT TEST

APPLICATION
  |
  +--> USE-CASE TEST
  |    WITH CONTROLLED PORTS

CONTROLLER
  |
  +--> HTTP CONTRACT TEST

REPOSITORY
  |
  +--> POSTGRESQL
       TESTCONTAINERS

EXTERNAL CLIENT
  |
  +--> MOCK HTTP SERVER /
       CONTRACT TEST

ARCHITECTURE
  |
  +--> ARCHUNIT
```

Test-data construction becomes:

```text
SIMPLE SCENARIO
      |
      v
LOCAL FIXTURE

REUSED STABLE VALUE
      |
      v
TestConstants

COMMON VALID OBJECT
      |
      v
OBJECT MOTHER

COMPLEX VARIATIONS
      |
      v
TEST DATA BUILDER
```

Mockito usage becomes:

```text
COLLABORATOR
    |
    v
IS IT EXTERNAL TO
THE UNIT?
    |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
MOCK   USE REAL
       OBJECT
```

Excessive mocking becomes an architectural signal:

```text
TEST NEEDS
15 MOCKS
   |
   v
DO NOT ASK ONLY:
"HOW DO I MOCK THEM?"
   |
   v
ALSO ASK:
"WHY DOES THIS CLASS
NEED 15 DEPENDENCIES?"
```

AssertJ becomes:

```java
assertThat(actual)
        .as("meaningful description of expected behavior")
        .isEqualTo(expected);
```

rather than:

```java
assertThat(actual)
        .isEqualTo(expected);
```

when the project quality convention requires the description.

Concurrency testing becomes:

```text
THREAD.SLEEP
     |
     X

CONTROLLED
SYNCHRONIZATION
     |
     +--> CountDownLatch
     +--> Barrier
     +--> Controlled Future
     +--> Atomic Counter
```

Coverage governance becomes:

```text
JACOCO >= 80%
      |
      v
NECESSARY SIGNAL
      |
      X
      |
NOT PROOF OF
CORRECTNESS
```

The stronger model is:

```text
COVERAGE
    +
MEANINGFUL ASSERTIONS
    +
BOUNDARY TESTS
    +
FAILURE TESTS
    +
CONTRACT TESTS
    +
MUTATION TESTING
    =
HIGHER CONFIDENCE
```

Flaky tests become:

```text
FLAKY TEST
    |
    v
DEFECT
    |
    +--> FIND ROOT CAUSE
    |
    +--> FIX ISOLATION
    |
    +--> FIX TIME
    |
    +--> FIX CONCURRENCY
    |
    +--> FIX EXTERNAL DEPENDENCY
```

not:

```text
FLAKY TEST
    |
    v
RETRY 5 TIMES
    |
    v
IGNORE PROBLEM
```

The complete testing equation is:

```text
FAST UNIT TESTS
        +
FOCUSED COMPONENT TESTS
        +
PRODUCTION-LIKE INTEGRATION TESTS
        +
CONTRACT TESTS
        +
ARCHITECTURE TESTS
        +
DETERMINISTIC TEST DATA
        +
CONTROLLED MOCKING
        +
MEANINGFUL ASSERTIONS
        +
NEGATIVE-PATH TESTING
        +
DETERMINISTIC CONCURRENCY TESTING
        +
JACOCO GOVERNANCE
        +
SELECTIVE MUTATION TESTING
        +
FLAKY-TEST ELIMINATION
        =
SUSTAINABLE AUTOMATED QUALITY
```

The governing principle is:

```text
Write tests for behavior,
not percentages.

Keep domain tests fast.

Do not start Spring
when Java objects are enough.

Use real PostgreSQL semantics
when database behavior matters.

Do not modify an applied
Flyway migration to fix a test.

Use deterministic IDs.

Keep TestConstants controlled.

Use builders when they
make scenarios clearer.

Mock collaborators,
not data.

If a test needs many mocks,
review the production design.

Keep Mockito strict.

Do not hide unused stubs
with global leniency.

Describe AssertJ assertions
with meaningful .as(...).

Use parameterized tests
for genuine scenario matrices.

Test boundaries.

Test failures.

Test maximum values.

Test one value beyond
the maximum.

Do not synchronize tests
with Thread.sleep.

Test Virtual Thread behavior
without assuming unlimited
downstream capacity.

Keep JaCoCo at or above
the approved threshold,
normally 80%.

But never confuse
80% coverage with
80% correctness.

Use mutation testing
where business risk
justifies it.

Treat flaky tests
as defects.

Do not weaken valid tests
to make the build green.

And optimize the test suite
for one outcome:

a developer should be able
to change production code
and learn quickly and reliably
whether important behavior
was broken.
```
