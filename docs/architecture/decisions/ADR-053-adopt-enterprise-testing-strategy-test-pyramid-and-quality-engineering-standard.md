# ADR-053: Adopt Enterprise Testing Strategy, Test Pyramid and Quality Engineering Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-053 |
| Title | Adopt Enterprise Testing Strategy, Test Pyramid and Quality Engineering Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Testing, Quality Engineering, JUnit 5, AssertJ, Mockito, Testcontainers |
| Related Work Items | Unit Testing, Integration Testing, Contract Testing, Performance Testing, Coverage |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform contains business-critical services implemented primarily using:

```text
Java 21

Spring Boot

PostgreSQL

SQS

Redis

REST APIs

External HTTP Integrations

Transactional Outbox

Virtual Threads
```

Testing these services effectively requires more than maximizing code coverage.

A system can have:

```text
90% Coverage
```

and still contain:

- incorrect business behavior
- weak assertions
- unrealistic mocks
- integration failures
- schema incompatibilities
- concurrency defects
- broken database migrations
- contract regressions

The platform therefore requires a deliberate, layered testing strategy.

---

# 2. Problem Statement

The organization requires standards defining:

- test pyramid
- unit tests
- component tests
- integration tests
- contract tests
- end-to-end tests
- repository tests
- service tests
- controller tests
- HTTP client tests
- SQS tests
- Redis tests
- PostgreSQL tests
- Testcontainers
- Mockito
- AssertJ
- JUnit 5
- WireMock
- MockWebServer
- concurrent tests
- Virtual Thread tests
- deterministic tests
- JaCoCo
- mutation testing
- flaky tests
- performance tests
- test data
- quality gates

---

# 3. Decision Drivers

Primary drivers are:

1. business correctness
2. regression prevention
3. fast feedback
4. realistic infrastructure validation
5. deterministic execution
6. maintainability
7. low flakiness
8. contract safety
9. reliable refactoring
10. production confidence

---

# 4. Decision

The platform adopts a layered testing strategy based on risk and feedback speed.

The canonical model is:

```text
                    E2E
                   /   \
                  /     \
             CONTRACT   COMPONENT
                /         \
               /           \
          INTEGRATION     INTEGRATION
              /               \
             /                 \
                    UNIT
```

The majority of business behavior SHOULD be validated below the E2E level.

---

# 5. Fundamental Principle

The governing principle is:

```text
Use the lowest-cost test level
that can reliably validate
the behavior under test.
```

---

# 6. Test Pyramid

The preferred distribution is:

```text
MANY

Unit Tests

     ↓

Component / Integration Tests

     ↓

Contract Tests

     ↓

FEW

End-to-End Tests
```

---

# 7. Pyramid Is Guidance

The test pyramid is not a mandatory numeric ratio.

The correct distribution depends on:

- architecture
- risk
- infrastructure
- integration complexity

---

# 8. Unit Tests

Unit tests validate isolated application behavior.

---

# 9. Unit Test Scope

Typical unit-test targets include:

```text
Domain Rules

Calculations

Validators

Mappers

State Transitions

Application Services

Value Objects

Utility Logic
```

---

# 10. Unit Tests Should Be Fast

Unit tests SHOULD normally:

- avoid Spring context startup
- avoid network
- avoid containers
- avoid filesystem dependencies
- avoid external infrastructure

---

# 11. Framework-Free Unit Tests

If a class can be tested without Spring, prefer a plain JUnit test.

---

# 12. Example

Prefer:

```java
class OrderTotalsCalculatorTest {
}
```

over:

```java
@SpringBootTest
class OrderTotalsCalculatorTest {
}
```

when Spring is unnecessary.

---

# 13. JUnit 5

JUnit 5 is the standard testing framework.

---

# 14. Test Naming

Test methods SHOULD follow the established `test*` convention.

Example:

```java
testCalculateTotalShouldReturnExpectedAmount()
```

---

# 15. Test Intent

A test name SHOULD describe:

```text
Operation

Condition

Expected Result
```

---

# 16. AssertJ

AssertJ is the preferred fluent assertion library.

---

# 17. Assertion Description

Assertions MUST include meaningful `.as("...")` descriptions where required by project standards.

Example:

```java
assertThat(result)
        .as("calculated order total should match the sum of item totals")
        .isEqualByComparingTo(expected);
```

---

# 18. Description Placement

`.as("...")` MUST appear before the assertion predicate it describes.

---

# 19. Assertions Must Be Meaningful

Avoid tests that only check:

```java
assertThat(result).isNotNull();
```

when the behavior requires stronger validation.

---

# 20. Multiple Assertions

Multiple assertions are acceptable when they verify one coherent outcome.

---

# 21. Mockito

Mockito SHOULD be used for external collaborators in unit tests.

---

# 22. Mock Collaborators, Not Value Objects

Good candidates:

```text
Repository

External Client

Publisher

Clock

Port
```

---

# 23. Avoid Mocking Domain Data

Domain objects and DTOs SHOULD generally be created as real objects rather than mocked.

---

# 24. Over-Mocking

A unit test mocking most behavior of the class ecosystem may provide little confidence.

---

# 25. Mocking Signal

If a service test requires many unrelated mocks, the production class may have excessive responsibility.

---

# 26. Mockito Strictness

Unnecessary stubbing SHOULD be avoided.

---

# 27. Verification

Interaction verification SHOULD be used when the interaction itself is part of the contract.

---

# 28. Do Not Verify Everything

This is discouraged:

```text
verify every method call
```

when observable behavior already proves correctness.

---

# 29. ArgumentCaptor

Use `ArgumentCaptor` when validating an important interaction payload.

---

# 30. Captor Abuse

Do not reconstruct the entire implementation through captor assertions unnecessarily.

---

# 31. Exception Tests

Exception tests SHOULD validate:

- expected type
- relevant message/code
- meaningful side effects

---

# 32. Exception Lambda

Exception assertion lambdas SHOULD contain only the invocation relevant to the assertion.

Example:

```java
assertThatThrownBy(() -> service.approve(request))
        .as("approve should reject an order already cancelled")
        .isInstanceOf(InvalidOrderStatusException.class);
```

---

# 33. Deterministic Tests

Tests MUST be deterministic.

---

# 34. Random Data

Random data SHOULD NOT be used unless randomness itself is relevant to the test.

---

# 35. UUID

Prefer fixed UUID constants.

Example:

```java
private static final UUID ORDER_ID =
        UUID.fromString("11111111-1111-1111-1111-111111111111");
```

---

# 36. TestConstants

Shared stable values SHOULD be centralized where that improves consistency.

---

# 37. Time

Tests depending on current time SHOULD use controlled time.

---

# 38. Clock

Prefer injecting:

```java
Clock
```

for business-time logic.

---

# 39. Fixed Clock

Example:

```java
Clock.fixed(...)
```

provides deterministic behavior.

---

# 40. Thread.sleep

Tests SHOULD NOT use:

```java
Thread.sleep(...)
```

as ordinary synchronization.

---

# 41. Why

`Thread.sleep` creates:

- slow tests
- timing races
- CI instability
- flaky behavior

---

# 42. Concurrent Test Synchronization

Use deterministic mechanisms such as:

```text
CountDownLatch

CyclicBarrier

Phaser

CompletableFuture Coordination

Awaitility
```

where appropriate.

---

# 43. Awaitility

Awaitility MAY be used for eventually consistent asynchronous behavior.

---

# 44. Awaitility Bounds

Async tests MUST have bounded timeout behavior.

---

# 45. Unit Test Data

Test data SHOULD emphasize relevant fields.

---

# 46. Test Builder

Builders MAY reduce setup noise for complex domain objects.

---

# 47. Default Builder

Builders should supply sensible defaults while allowing relevant differences to remain obvious.

---

# 48. Builder Anti-Pattern

Do not hide all business-relevant test input inside opaque fixtures.

---

# 49. Object Mother

Object Mother or Fixture Factory patterns MAY be used for complex test domains when they improve readability.

---

# 50. Data Isolation

Each test SHOULD create or control the data it requires.

---

# 51. Shared Mutable State

Tests MUST avoid shared mutable state across test methods.

---

# 52. Test Order

Tests MUST NOT rely on execution order.

---

# 53. Integration Tests

Integration tests validate collaboration with real infrastructure or framework behavior.

---

# 54. Integration Test Targets

Examples:

```text
PostgreSQL

Flyway

Redis

SQS

HTTP serialization

Spring Security

Transactions

Repository Queries
```

---

# 55. Testcontainers

Testcontainers is the preferred approach for infrastructure integration tests where practical.

---

# 56. Representative Infrastructure

Use technology equivalent to production semantics.

Example:

```text
Production PostgreSQL
        |
        v
Testcontainers PostgreSQL
```

---

# 57. H2 Limitation

H2 MUST NOT be treated as proof of PostgreSQL behavior.

---

# 58. PostgreSQL Integration Tests

PostgreSQL-specific tests SHOULD validate:

- native queries
- indexes where relevant
- constraints
- JSON types
- locking
- transaction behavior
- Flyway migrations

---

# 59. Flyway Test

A clean PostgreSQL test database SHOULD successfully execute the complete migration history.

---

# 60. Historical Migration

Tests MUST NOT require editing an already applied migration to become green.

---

# 61. Corrective Migration

Any schema correction MUST use a new migration version.

---

# 62. Repository Tests

Repository tests SHOULD use actual persistence infrastructure when query behavior matters.

---

# 63. @DataJpaTest

`@DataJpaTest` MAY be used with representative PostgreSQL infrastructure.

---

# 64. Repository Behavior

Repository tests SHOULD verify:

```text
Filtering

Sorting

Pagination

Relationships

Constraints

Custom Queries
```

where relevant.

---

# 65. Query Count

Critical persistence paths MAY include query-count tests to detect N+1 regressions.

---

# 66. Query Count Caution

Do not freeze incidental query counts for every repository operation.

---

# 67. Service Tests

Service/application tests SHOULD generally be plain unit tests unless framework behavior is specifically under test.

---

# 68. Controller Tests

Controllers SHOULD have focused web-layer tests.

---

# 69. MockMvc

Spring MVC controllers SHOULD use:

```text
MockMvc
```

or equivalent approved web-test infrastructure.

---

# 70. Controller Test Scope

Controller tests SHOULD validate:

- status codes
- request validation
- serialization
- response shape
- error mapping
- security behavior where appropriate

---

# 71. Controller Test Does Not Replace Service Test

Business rules SHOULD remain validated independently.

---

# 72. @SpringBootTest

`@SpringBootTest` SHOULD be used intentionally.

---

# 73. Avoid Full Context for Everything

Starting the complete Spring application for every trivial unit test slows the feedback loop.

---

# 74. Component Tests

Component tests validate a service as a deployable logical unit while controlling external dependencies.

---

# 75. Component Scope

Typical component test:

```text
HTTP Endpoint
     |
     v
Spring Application
     |
     v
PostgreSQL / Redis
     |
     v
Stubbed External Systems
```

---

# 76. Component Tests Provide High Value

They validate multiple application layers while remaining more deterministic than full E2E tests.

---

# 77. External HTTP Clients

HTTP clients require dedicated tests.

---

# 78. WireMock

WireMock MAY be used for HTTP protocol simulation.

---

# 79. MockWebServer

MockWebServer MAY also be used for lightweight HTTP client testing.

---

# 80. Client Test Scope

Validate:

```text
HTTP Method

URL

Query Parameters

Headers

Authentication

Request Body

Response Mapping

Timeout Handling

4xx Handling

5xx Handling

Malformed Response
```

---

# 81. Remote Error Mapping

Tests SHOULD validate translation of remote errors into application exceptions.

---

# 82. Sensitive Data

HTTP-client tests SHOULD validate that sensitive values are not leaked into error messages where applicable.

---

# 83. Circuit Breaker Tests

Resilience behavior SHOULD be tested independently from business behavior where possible.

---

# 84. Retry Tests

Retry tests MUST remain deterministic and fast.

---

# 85. Avoid Real Delays

Long retry backoffs SHOULD be overridden with short test configuration rather than real production durations.

---

# 86. SQS Integration Tests

SQS integrations require tests at multiple levels.

---

# 87. Producer Unit Test

Producer logic MAY be unit tested around:

- event construction
- destination selection
- serialization preparation

---

# 88. Consumer Unit Test

Consumer business processing SHOULD be unit tested without SQS when possible.

---

# 89. SQS Infrastructure Test

Integration tests SHOULD validate actual producer/consumer configuration where important.

---

# 90. SQS Test Scope

Validate:

```text
Serialization

Deserialization

Headers

Topic Configuration Assumptions

Consumer Processing

Retry/DLQ Behavior

Idempotency
```

---

# 91. Event Idempotency

Consumer tests SHOULD validate duplicate event handling.

---

# 92. Event Ordering

Where ordering matters, tests SHOULD explicitly validate relevant ordering behavior.

---

# 93. Transactional Outbox

Outbox tests SHOULD validate:

```text
Business Transaction

Outbox Persistence

Dispatcher Selection

Status Transition

Retry

Idempotency
```

---

# 94. Outbox Atomicity

Integration tests SHOULD verify that the business state and outbox record commit atomically where required.

---

# 95. Redis Tests

Redis integration SHOULD be tested against representative Redis infrastructure where cache behavior matters.

---

# 96. Redis Test Scope

Validate:

```text
Serialization

TTL

Eviction Behavior

Fallback

Cache Hit

Cache Miss

Cache Invalidation
```

---

# 97. Cache Failure

Tests SHOULD validate expected behavior when Redis is unavailable.

---

# 98. Fallback Consistency

Fallback behavior MUST preserve business correctness.

---

# 99. Contract Testing

Contract tests validate provider-consumer compatibility.

---

# 100. API Contracts

REST contract validation SHOULD use:

- OpenAPI compatibility
- consumer-driven contracts
- schema validation

where appropriate.

---

# 101. Provider Contract

Providers MUST own their published contract.

---

# 102. Consumer Contract

Consumers SHOULD test the assumptions they depend on.

---

# 103. Breaking Change Detection

Contract tooling SHOULD detect breaking changes before deployment.

---

# 104. Event Contracts

SQS messages/events SHOULD have compatibility tests where schemas are shared.

---

# 105. Contract Tests Are Not E2E

Contract tests validate compatibility without requiring every dependent service to run simultaneously.

---

# 106. End-to-End Tests

E2E tests validate complete business journeys across system boundaries.

---

# 107. E2E Scope

Examples:

```text
Create Cart

Checkout

Create Order

Approve Order

Publish Workflow Event
```

---

# 108. Keep E2E Small

E2E suites SHOULD focus on high-value critical journeys.

---

# 109. Why Few E2E Tests

E2E tests are:

- slower
- more expensive
- more fragile
- harder to diagnose

---

# 110. E2E Is Not Primary Business Test Layer

Business-rule permutations SHOULD be validated lower in the pyramid.

---

# 111. E2E Environment

E2E tests require a controlled representative environment.

---

# 112. Test Environment Drift

Environment drift can produce false failures or false confidence.

---

# 113. E2E Data

E2E tests SHOULD use controlled test data.

---

# 114. Production Data

Real customer production data MUST NOT be required for ordinary E2E testing.

---

# 115. Security Tests

Security-sensitive behavior requires explicit tests.

---

# 116. Authentication Tests

Validate:

```text
Missing Token

Invalid Token

Expired Token

Valid Token
```

where applicable.

---

# 117. Authorization Tests

Validate relevant:

```text
Allowed Role

Forbidden Role

Ownership Rule

Cross-Customer Access
```

---

# 118. Security Regression

Authorization tests SHOULD be present for critical endpoints.

---

# 119. Input Validation Tests

Boundary tests SHOULD verify invalid:

```text
Null

Blank

Length

Format

Range
```

where applicable.

---

# 120. Injection

Security testing SHOULD validate relevant injection protections through suitable security tooling and targeted tests.

---

# 121. Sensitive Logging Tests

Where risk warrants it, tests SHOULD validate that secrets are sanitized from error/log output.

---

# 122. Concurrency Tests

Concurrent business behavior requires deterministic concurrency tests.

---

# 123. Concurrency Test Targets

Examples:

```text
Duplicate Checkout

Concurrent Update

Optimistic Lock

Idempotency

Outbox Dispatch

Cache Population

Parallel Validation
```

---

# 124. Virtual Thread Tests

Virtual Thread workflows SHOULD validate:

- task completion
- bounded downstream concurrency
- context propagation
- failure aggregation
- no context leakage

---

# 125. SecurityContext

Concurrent tests MUST verify SecurityContext propagation where business behavior depends on authenticated identity.

---

# 126. Request Context

Request/correlation context SHOULD be tested when explicitly propagated across asynchronous execution.

---

# 127. No Timing Assumptions

Concurrency tests SHOULD coordinate events rather than assume scheduler timing.

---

# 128. Race Detection

Repeated execution MAY be used for difficult concurrency defects, but deterministic orchestration is preferred.

---

# 129. Property-Based Testing

Property-based testing MAY be used for logic with broad input spaces.

---

# 130. Suitable Domains

Examples:

```text
Calculations

Parsing

Serialization

Value Objects

Validation
```

---

# 131. Property Tests Must Remain Reproducible

Failures SHOULD expose the seed/input required for reproduction.

---

# 132. Parameterized Tests

JUnit parameterized tests SHOULD be used when the same behavior applies to a defined set of inputs.

---

# 133. Good Parameterization

Example:

```text
all forbidden workflow statuses
```

---

# 134. Avoid Massive Parameter Tables

If each case has different behavior, separate tests may be clearer.

---

# 135. Snapshot Testing

Snapshot/golden-master testing MAY be used for stable complex output where appropriate.

---

# 136. Snapshot Review

Snapshot changes MUST be reviewed rather than blindly regenerated.

---

# 137. Mutation Testing

Mutation testing MAY be introduced for high-value business logic.

---

# 138. Purpose

Mutation testing evaluates whether tests detect intentionally modified logic.

---

# 139. Mutation Testing Is Not Mandatory Everywhere

It can be expensive and SHOULD target critical logic.

---

# 140. Suitable Mutation Targets

Examples:

```text
Financial Calculations

Workflow Rules

Eligibility Rules

Status Transitions

Security Decisions
```

---

# 141. Mutation Score

Mutation score SHOULD be interpreted as a quality signal rather than a universal target.

---

# 142. Performance Tests

Performance testing is separate from ordinary unit/integration testing.

---

# 143. Performance Test Types

Examples:

```text
Load Test

Stress Test

Spike Test

Soak Test

Capacity Test

Microbenchmark
```

---

# 144. Load Test

Validates expected workload.

---

# 145. Stress Test

Determines behavior beyond expected capacity.

---

# 146. Spike Test

Validates sudden traffic increases.

---

# 147. Soak Test

Validates long-duration stability.

---

# 148. Capacity Test

Determines sustainable throughput and bottlenecks.

---

# 149. Microbenchmark

JVM microbenchmarks SHOULD use JMH.

---

# 150. Naive Timing

Tests using:

```java
System.nanoTime()
```

inside ordinary unit tests SHOULD NOT be treated as reliable JVM benchmarks.

---

# 151. Performance Environment

Material performance gates SHOULD use controlled infrastructure.

---

# 152. Baseline

Performance testing SHOULD compare against an established baseline where practical.

---

# 153. Performance Regression

A significant unexplained regression MUST be investigated before release for critical paths.

---

# 154. Database Performance

Critical query paths SHOULD be tested with representative data volumes.

---

# 155. Small Dataset Trap

A query performing well on 10 rows does not prove behavior on millions of rows.

---

# 156. SQS Performance

Consumer throughput SHOULD be validated against expected traffic and partitioning.

---

# 157. Backlog Recovery

Critical consumers SHOULD demonstrate ability to recover acceptable backlog within operational objectives.

---

# 158. Coverage

JaCoCo remains the standard coverage mechanism.

---

# 159. Coverage Baseline

Where the service standard defines:

```text
>= 80%
```

the build SHOULD enforce the baseline.

---

# 160. New Code

New or changed code SHOULD maintain or improve coverage.

---

# 161. Critical Logic

Critical business logic MAY require coverage significantly above the global minimum.

---

# 162. Branch Coverage

Branch coverage SHOULD be considered for condition-heavy logic.

---

# 163. Coverage Exclusions

Exclusions MUST be narrowly justified.

---

# 164. Generated Code

Generated code MAY be excluded where testing it provides little value and the generator itself is trusted/tested.

---

# 165. DTO Coverage

Do not create meaningless tests solely to raise coverage on trivial generated behavior.

---

# 166. Coverage Quality

Prefer:

```text
80% meaningful behavioral coverage
```

over:

```text
95% execution-only coverage
```

---

# 167. Flaky Tests

Flaky tests are defects.

---

# 168. Flaky Definition

A test is flaky when identical code/environment can produce inconsistent results without legitimate nondeterminism.

---

# 169. Common Causes

Examples:

```text
Thread.sleep

Shared State

Real Clock

Random Input

Uncontrolled Network

Execution Order

Race Condition
```

---

# 170. Quarantine

Temporary test quarantine MAY be used only with:

- owner
- reason
- remediation item
- expiration/review

---

# 171. Disabled Tests

`@Disabled` MUST NOT become a permanent test-management strategy.

---

# 172. Retrying Tests

Automatic retry MAY reduce transient environmental noise but MUST NOT hide persistent flakiness.

---

# 173. Flaky Metrics

Teams SHOULD monitor flaky test frequency.

---

# 174. Test Runtime

Test runtime is a Developer Experience metric.

---

# 175. Feedback Budget

Suites SHOULD be structured so developers receive fast feedback for common changes.

---

# 176. Suggested Layering

Example:

```text
Local / PR Fast Path
    |
    +--> Compile
    +--> Unit Tests
    +--> Architecture Tests
    +--> Fast Integration Tests

Main / CI
    |
    +--> Full Integration
    +--> Contract
    +--> Sonar
    +--> SAST

Release
    |
    +--> Component
    +--> E2E
    +--> Performance where required
```

---

# 177. Parallel CI

Independent test suites SHOULD execute in parallel where safe.

---

# 178. Test Selection

Test selection MAY use changed-component analysis if correctness remains reliable.

---

# 179. Do Not Skip Critical Tests Blindly

Test optimization MUST NOT omit impacted critical suites.

---

# 180. Test Reporting

CI MUST expose clear failure reports.

---

# 181. Failure Diagnostics

A test failure SHOULD reveal:

```text
Expected Behavior

Actual Behavior

Relevant Context
```

---

# 182. Logging in Tests

Test logging SHOULD remain useful without flooding CI output.

---

# 183. Integration Container Logs

Container logs MAY be attached on failure for diagnosis.

---

# 184. Test Cleanup

Integration tests MUST clean or isolate their data.

---

# 185. Transaction Rollback

Transaction rollback MAY isolate repository tests when semantics permit it.

---

# 186. Container Isolation

Separate databases/schemas MAY be used for stronger isolation.

---

# 187. Parallel Tests

Tests executed in parallel MUST not share conflicting mutable resources.

---

# 188. Static Port

Tests SHOULD avoid fixed ports when dynamic allocation is available.

---

# 189. Testcontainers Ports

Use mapped container ports rather than assumptions about host ports.

---

# 190. External Network

Automated tests SHOULD avoid dependency on uncontrolled public network services.

---

# 191. Stub External Dependencies

External third-party behavior SHOULD normally be simulated for automated component tests.

---

# 192. Sandbox Integration

Real third-party sandbox tests MAY complement stubs for high-risk integrations.

---

# 193. Contract Accuracy

Stubs MUST be maintained against real provider contracts.

---

# 194. Test Pyramid Decision Guide

Use a **Unit Test** when:

```text
Behavior is local

Infrastructure is irrelevant

Fast deterministic validation is possible
```

Use an **Integration Test** when:

```text
Framework/database/message behavior matters
```

Use a **Contract Test** when:

```text
Provider/consumer compatibility matters
```

Use a **Component Test** when:

```text
The service must be validated through its real external boundary
while dependencies remain controlled
```

Use an **E2E Test** when:

```text
A critical business journey across deployable systems must be validated
```

---

# 195. Repository Example

For:

```text
OrderRepository.findHeaders(...)
```

use PostgreSQL integration testing if behavior depends on actual query semantics.

---

# 196. Service Example

For:

```text
OrderApprovalService.approve(...)
```

use unit tests with mocked ports for business orchestration.

---

# 197. Controller Example

For:

```text
POST /orders/approve
```

use focused web tests for HTTP contract and validation.

---

# 198. Client Example

For:

```text
CustomersClient
```

use WireMock/MockWebServer integration tests for HTTP protocol and error mapping.

---

# 199. End-to-End Example

For:

```text
Cart Checkout
      |
      v
Order Creation
      |
      v
Workflow Publication
```

use a limited E2E test to prove the critical cross-service journey.

---

# 200. Test Ownership

The team owning production code owns its tests.

---

# 201. Broken Test Ownership

A broken test is not automatically "QA's problem."

---

# 202. Quality Engineering

Quality is a shared engineering responsibility.

---

# 203. QA Role

Dedicated QA engineers, where present, SHOULD complement engineering through:

- exploratory testing
- risk analysis
- automation expertise
- scenario design

rather than owning all quality alone.

---

# 204. Developer Responsibility

Developers MUST validate the changes they implement.

---

# 205. Shift Left

Testing should begin during implementation, not after the feature is considered "complete."

---

# 206. Shift Right

Production observability and synthetic testing SHOULD complement pre-production validation for critical systems.

---

# 207. Synthetic Tests

Synthetic tests MAY continuously validate critical production journeys without using sensitive production data.

---

# 208. Canary Validation

Canary deployments MAY use targeted smoke/business tests before full traffic migration.

---

# 209. Production Testing Safety

Production validation MUST avoid destructive or uncontrolled test behavior.

---

# 210. Test Data Governance

Test data MUST follow ADR-046.

---

# 211. No Real PII in Fixtures

Automated fixtures MUST NOT contain copied real customer PII.

---

# 212. Synthetic Data

Synthetic data is preferred.

---

# 213. Anonymized Data

Approved anonymized datasets MAY be used when realism requires production-like distributions.

---

# 214. Secrets

Test source MUST NOT contain production secrets.

---

# 215. CI Credentials

Integration-test credentials SHOULD be short-lived and environment-scoped where applicable.

---

# 216. Test Database

Tests MUST NOT execute destructive operations against production databases.

---

# 217. Environment Guard

High-risk integration tools SHOULD include safeguards preventing production endpoints from being used accidentally.

---

# 218. Quality Gate

A service is not considered test-compliant solely because:

```text
./gradlew test
```

returns success.

---

# 219. Enterprise Testing Gate

A service SHOULD satisfy applicable:

```text
[ ] Unit tests for business logic

[ ] Repository integration tests where custom persistence exists

[ ] Controller/web tests for public APIs

[ ] HTTP client tests for integrations

[ ] SQS tests where messaging exists

[ ] Redis tests where cache semantics matter

[ ] Flyway migration test where database exists

[ ] Contract validation for shared contracts

[ ] Security tests for protected endpoints

[ ] Concurrency tests for concurrent workflows

[ ] JaCoCo policy satisfied

[ ] No unexplained flaky tests

[ ] Test data is compliant

[ ] Critical E2E journeys defined

[ ] Performance tests defined where capacity risk exists
```

---

# 220. Test Review Checklist

Reviewers SHOULD evaluate:

```text
[ ] Does the test verify real behavior?

[ ] Is the test at the correct level?

[ ] Is infrastructure mocked unnecessarily?

[ ] Is infrastructure simulated unrealistically?

[ ] Are assertions strong enough?

[ ] Are edge cases covered?

[ ] Are failures covered?

[ ] Is the test deterministic?

[ ] Does it use Thread.sleep?

[ ] Is test data stable?

[ ] Are AssertJ descriptions present?

[ ] Does the test overfit implementation details?

[ ] Could this be a faster lower-level test?

[ ] Does this integration need representative infrastructure?
```

---

# 221. Anti-Patterns

The following are prohibited or strongly discouraged:

- `@SpringBootTest` for every test
- mocking the entire application
- mocking DTOs/value objects
- interaction verification for every method call
- random UUIDs in deterministic tests
- real current time without control
- Thread.sleep synchronization
- test-order dependencies
- shared mutable test state
- H2 used as proof of PostgreSQL compatibility
- real external public network calls in normal automated suites
- full E2E validation for every business permutation
- meaningless tests written solely for coverage
- broad JaCoCo exclusions
- permanent `@Disabled`
- infinite flaky-test retries
- test fixtures containing real customer PII
- production secrets in test code
- performance conclusions from ordinary unit-test timings
- fragile assertions against incidental implementation details
- contract stubs diverging from actual providers
- E2E suites so large that failures become undiagnosable

---

# 222. Positive Consequences

The decision provides:

- stronger business correctness
- faster developer feedback
- realistic database validation
- safer API evolution
- safer SQS integration
- better concurrency testing
- improved refactoring confidence
- reduced flaky tests
- more meaningful coverage
- predictable quality gates
- lower dependence on large E2E suites

---

# 223. Negative Consequences

The decision introduces:

- container infrastructure in tests
- test-suite maintenance
- contract tooling
- CI execution cost
- performance-testing environments
- additional test design effort

These costs are accepted because detecting integration and behavioral defects before production is significantly cheaper than production remediation.

---

# 224. Neutral Consequences

The decision also means:

- not every class requires direct unit tests
- coverage targets do not require trivial DTO tests
- not every repository method requires an integration test
- some critical scenarios justify expensive tests
- some infrastructure behavior cannot be adequately mocked
- mutation testing remains selective

---

# 225. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Slow test suite | High | Medium | Layering + parallelization |
| Flaky integration tests | High | Medium | Isolation + deterministic infra |
| Mock-heavy false confidence | High | High | Component/integration tests |
| E2E explosion | High | Medium | Test pyramid |
| Coverage gaming | Medium | High | Behavioral review |
| Container CI cost | Medium | Medium | Reuse/parallel pipeline |
| Contract drift | High | Medium | Automated compatibility |
| Test data leakage | Critical | Low/Medium | Synthetic data |
| Performance regressions | High | Medium | Baselines/load tests |
| Concurrency defects | High | Medium | Deterministic coordination |
| H2 incompatibility | High | Medium | PostgreSQL Testcontainers |

---

# 226. Implementation Guidance

The following rules are mandatory:

1. Testing strategy must be risk-based and layered.
2. Unit tests should validate business logic without unnecessary Spring context.
3. JUnit 5 is the standard unit-test framework.
4. AssertJ is the preferred assertion library where standardized.
5. Required AssertJ `.as("...")` descriptions must be preserved.
6. Test names should follow the established `test*` convention.
7. Mockito should mock external collaborators rather than domain data.
8. Tests must remain deterministic.
9. Unnecessary random UUID generation should be avoided.
10. Time-dependent logic should use controllable time where needed.
11. Thread.sleep should not be used as ordinary synchronization.
12. Testcontainers should be used for representative infrastructure where applicable.
13. PostgreSQL-specific behavior must not rely solely on H2 tests.
14. Flyway migration history must be validated on representative database infrastructure.
15. Already applied Flyway migrations remain immutable.
16. Repository custom query behavior should receive integration testing.
17. Controllers should receive focused HTTP-layer testing.
18. External HTTP clients should receive protocol/error-mapping tests.
19. SQS processing should test serialization, idempotency and failure behavior.
20. Redis tests should validate cache semantics where material.
21. Shared API/event contracts should receive compatibility validation.
22. E2E tests should focus on critical business journeys.
23. Authentication and authorization require explicit tests for protected capabilities.
24. Concurrent workflows require deterministic concurrency tests.
25. JaCoCo thresholds must follow project policy.
26. Coverage must not be gamed through meaningless tests or broad exclusions.
27. Flaky tests must be treated as defects.
28. Test retries must not conceal persistent flakiness.
29. Performance testing must use suitable tools and representative environments.
30. JMH should be used for JVM microbenchmarks.
31. Test data must follow data-governance standards.
32. Real customer PII must not appear in ordinary automated fixtures.
33. CI must expose actionable test-failure diagnostics.
34. Tests should execute at the lowest reliable level capable of proving the behavior.
35. Quality remains a shared engineering responsibility.

---

# 227. Validation

This ADR will be validated through:

- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- PostgreSQL
- SQS test infrastructure
- Redis test infrastructure
- WireMock
- MockWebServer
- JaCoCo
- SonarQube
- contract testing
- performance testing
- CI test reports
- flaky-test reporting
- code review

---

# 228. Success Criteria

The decision is successful when:

- critical business behavior has strong automated coverage
- test suites provide fast feedback
- PostgreSQL behavior is tested realistically
- contract regressions are detected before release
- flaky tests decrease
- E2E suites remain focused
- coverage remains meaningful
- concurrent workflows are deterministic under test
- infrastructure failures are tested
- developers can refactor with confidence
- production defects caused by untested integration assumptions decrease

---

# 229. Alternatives Rejected

## 229.1 E2E-Heavy Testing

Rejected because large E2E suites are slow, fragile and difficult to diagnose.

---

## 229.2 Unit Tests Only

Rejected because framework, persistence and integration behavior require realistic validation.

---

## 229.3 H2 for All Database Tests

Rejected because H2 does not provide full PostgreSQL compatibility.

---

## 229.4 Coverage Percentage as Quality

Rejected because execution does not prove correctness.

---

## 229.5 Mock Every Dependency

Rejected because excessive mocking can validate implementation assumptions instead of real behavior.

---

## 229.6 Test Everything Through SpringBootTest

Rejected because it unnecessarily slows feedback and obscures test boundaries.

---

# 230. Related Decisions

This ADR extends and implements:

- ADR-006: Use Flyway for Database Migrations
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-010: Use Redis for Distributed Caching
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-036: API Design and Compatibility Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-039: CI/CD, Release and Deployment Governance Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-048: Engineering Productivity and Developer Experience Standards
- ADR-049: AI-Assisted Software Engineering Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard

---

# 231. References

- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- WireMock
- OkHttp MockWebServer
- JaCoCo
- SonarQube
- JMH
- Consumer-Driven Contract Testing
- Test Pyramid
- Google Testing Blog
- Martin Fowler — Test Pyramid
- PostgreSQL Documentation
- Amazon SQS Documentation
- Redis Documentation

---

# 232. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial enterprise testing and quality engineering baseline |

---

# 233. Decision Summary

The definitive testing model is:

```text
                         E2E
                          |
                    CRITICAL JOURNEYS
                          |
                       CONTRACT
                          |
                      COMPONENT
                          |
                     INTEGRATION
                          |
                         UNIT
```

The majority of business permutations remain close to the bottom:

```text
FAST
DETERMINISTIC
ISOLATED
```

while infrastructure-specific behavior moves upward only when required.

The decision tree is:

```text
WHAT ARE WE TESTING?
        |
        v
PURE BUSINESS LOGIC?
   /             \
 YES              NO
  |                |
  v                v
UNIT TEST     DOES INFRASTRUCTURE
                  MATTER?
                  /   \
                YES    NO
                 |      |
                 v      v
          INTEGRATION   UNIT
```

For service contracts:

```text
PROVIDER
   |
   v
CONTRACT
   |
   v
COMPATIBILITY TEST
   |
   v
CONSUMER
```

For database behavior:

```text
POSTGRESQL PRODUCTION
          ^
          |
POSTGRESQL TESTCONTAINER
```

rather than assuming:

```text
H2
 =
POSTGRESQL
```

For concurrency:

```text
CONCURRENT OPERATION
        |
        v
CONTROL START POINT
        |
        v
SYNCHRONIZE ACTORS
        |
        v
EXECUTE
        |
        v
ASSERT STATE
```

not:

```text
START THREAD
    |
    v
SLEEP 500ms
    |
    v
HOPE
```

For test quality:

```text
TEST EXECUTES CODE
       |
       v
NOT ENOUGH

TEST DETECTS WRONG BEHAVIOR
       |
       v
VALUABLE TEST
```

For coverage:

```text
COVERAGE
   +
ASSERTION QUALITY
   +
EDGE CASES
   +
FAILURE CASES
   +
REALISTIC INTEGRATION
   =
CONFIDENCE
```

For E2E:

```text
MANY BUSINESS RULES
         |
         v
     UNIT / COMPONENT

FEW CRITICAL JOURNEYS
         |
         v
         E2E
```

The complete quality equation is:

```text
FAST UNIT TESTS
      +
REALISTIC INTEGRATION TESTS
      +
CONTRACT VALIDATION
      +
FOCUSED E2E
      +
DETERMINISTIC CONCURRENCY TESTS
      +
MEANINGFUL COVERAGE
      +
SECURITY TESTING
      +
PERFORMANCE VALIDATION
      +
LOW FLAKINESS
      =
ENTERPRISE QUALITY ENGINEERING
```

The governing principle is:

```text
A test is valuable not because
it executes code.

A test is valuable because
it detects behavior that would
be wrong in production.

Choose the lowest test level
that can prove the requirement.

Use real infrastructure where
infrastructure semantics matter.

Keep E2E focused.

Keep tests deterministic.

And optimize for confidence,
not for test count or
coverage percentage alone.
```
