# Code Review Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Code Review Guidelines |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the code review standards adopted by the AstraForge Supply Platform.

The objectives of code review are to:

- ensure correctness
- protect architectural integrity
- prevent regressions
- improve maintainability
- share knowledge
- detect security risks
- validate performance
- enforce engineering standards
- preserve long-term code quality

A code review is an engineering quality gate, not merely a source control approval.

---

# 2. Review Principles

Every review must be:

- objective
- constructive
- evidence-based
- respectful
- technically justified
- focused on the code rather than the author
- reproducible

Comments should explain **why** a change is required whenever the reason is not immediately obvious.

---

# 3. Scope of Review

Every Pull Request must be reviewed from the following perspectives:

- business correctness
- domain modeling
- architecture
- clean code
- security
- performance
- scalability
- maintainability
- observability
- testing
- documentation
- operational impact

---

# 4. Review Philosophy

The reviewer is responsible for evaluating:

- correctness
- risks
- maintainability
- operational impact

The reviewer is **not** responsible for rewriting the implementation.

Suggestions are preferable to personal coding preferences.

---

# 5. Pull Request Size

PRs should remain focused.

Recommended maximum:

```text
≈ 400 changed lines
```

Large changes should be split whenever practical.

Small PRs:

- are easier to review
- reduce defects
- reduce merge conflicts
- improve deployment safety

---

# 6. Pull Request Description

Every PR should include:

- objective
- business motivation
- architectural impact
- database changes
- API changes
- event changes
- migration strategy
- rollback considerations
- testing performed

Screenshots should be included only for UI changes.

---

# 7. Business Validation

Reviewers should verify:

- business rules
- workflow correctness
- state transitions
- validations
- invariants
- edge cases
- error handling

A technically correct implementation may still violate business rules.

---

# 8. Domain Review

Check whether:

- aggregates remain consistent
- invariants are preserved
- value objects remain immutable
- entities expose meaningful behavior
- domain events remain correct
- ubiquitous language is respected

Business terminology must remain consistent.

---

# 9. Architecture Review

Verify that dependencies follow the approved architecture.

Correct direction:

```text
Infrastructure

↓

Application

↓

Domain
```

The Domain layer must never depend on:

- Spring
- JPA
- REST
- Amazon SQS
- SQL
- HTTP

---

# 10. Dependency Direction

Reject changes introducing:

- cyclic dependencies
- layer violations
- controller-to-repository access
- infrastructure leakage
- framework coupling inside domain logic

---

# 11. SOLID Review

Evaluate:

- Single Responsibility
- Open/Closed
- Liskov Substitution
- Interface Segregation
- Dependency Inversion

Large classes usually indicate SRP violations.

---

# 12. Class Size

Investigate classes with:

- excessive methods
- excessive dependencies
- multiple responsibilities
- unrelated behavior

Very large classes usually deserve decomposition.

---

# 13. Method Review

Methods should be:

- cohesive
- understandable
- deterministic
- focused on one responsibility

Reviewers should question methods that require extensive comments to explain their logic.

---

# 14. Complexity

Review cognitive complexity.

Indicators:

- deep nesting
- multiple boolean branches
- repeated conditions
- long switch statements
- duplicated validation

Prefer decomposition over clever implementations.

---

# 15. Naming

Names must express business meaning.

Good:

```java
approveOrder()

calculateOutstandingBalance()

reserveInventory()
```

Avoid:

```java
process()

handle()

execute()

doStuff()
```

---

# 16. Readability

Code should read naturally.

Prefer:

```java
if (order.canBeApproved()) {
```

instead of:

```java
if (!(!order.isRejected() && !order.isCancelled())) {
```

---

# 17. Duplication

Reviewers should identify duplicated:

- business logic
- validation
- SQL
- mapping
- exception handling
- conversion
- constants

Duplicate logic increases maintenance cost.

---

# 18. Magic Values

Replace unexplained literals with named constants.

Example:

```java
MAX_RETRY_ATTEMPTS
```

instead of:

```java
5
```

unless the value is universally obvious.

---

# 19. Domain Logic

Business rules belong inside:

- aggregates
- domain services
- policies

Avoid business decisions inside:

- controllers
- repositories
- REST clients
- SQS consumers/listeners

---

# 20. Spring Usage

Verify appropriate Spring usage.

Controllers:

- request mapping
- validation
- delegation

Services:

- orchestration

Repositories:

- persistence

Avoid Spring annotations in the Domain layer.

---

# 21. Dependency Injection

Constructor injection is mandatory.

Reject:

- field injection
- mutable dependency injection

Dependencies should be immutable.

---

# 22. Transactions

Review transaction boundaries.

Transactions should:

- remain short
- avoid HTTP calls
- avoid direct SQS publishing from business/domain code
- avoid long-running work

---

# 23. Persistence Review

Verify:

- repository boundaries
- aggregate persistence
- query efficiency
- index usage
- pagination
- optimistic locking

Repositories must not contain business workflows.

---

# 24. SQL Review

Check:

- parameterized queries
- index usage
- N+1 risk
- fetch strategy
- pagination
- sorting

Never concatenate SQL.

---

# 25. Flyway Review

Review every migration.

Verify:

- version
- naming
- rollback considerations
- indexes
- constraints

Applied migrations must never be modified.

Every correction requires a new migration.

---

# 26. API Review

Verify:

- resource naming
- HTTP methods
- status codes
- request validation
- response model
- pagination
- error contract

REST consistency is mandatory.

---

# 27. Backward Compatibility

Review whether changes break:

- APIs
- events
- DTOs
- database
- consumers
- clients

Breaking changes require explicit versioning.

---

# 28. Event Review

Verify:

- event naming
- payload
- version
- metadata
- compatibility
- idempotency

Events are public contracts.

---

# 29. Messaging Review

Review:

- producer logic
- consumer logic
- retries
- DLQ
- idempotency
- outbox usage
- ordering

---

# 30. Outbox Review

Verify:

- transactional consistency
- retry handling
- payload immutability
- dispatcher safety
- SKIP LOCKED usage when applicable

---

# 31. Security Review

Always verify:

- authentication
- authorization
- tenant isolation
- object ownership
- input validation
- secret handling
- secure logging

Security defects block approval.

---

# 32. Sensitive Data

Reject code that logs:

- passwords
- tokens
- secrets
- authorization headers
- personal documents

---

# 33. Exception Handling

Exceptions should:

- preserve context
- preserve causes
- use meaningful types
- avoid swallowing failures

Never ignore exceptions.

---

# 34. Logging

Logs should:

- contain correlation identifiers
- be structured
- avoid sensitive data
- provide operational value

Avoid excessive INFO logging.

---

# 35. Observability

Verify:

- metrics
- tracing
- correlation propagation
- structured logging

Critical workflows must remain observable.

---

# 36. Performance Review

Review:

- algorithmic complexity
- allocations
- database round trips
- remote calls
- caching
- batching

Optimize only where evidence supports it.

---

# 37. Concurrency

Review:

- thread safety
- synchronization
- virtual thread usage
- executor lifecycle
- race conditions

---

# 38. Resource Management

Verify:

- streams closed
- executors closed
- connections released
- files closed

Resource leaks are production defects.

---

# 39. Null Safety

Review:

- Optional usage
- null handling
- defensive programming

Avoid unnecessary nullable state.

---

# 40. Immutability

Prefer immutable:

- DTOs
- Value Objects
- configuration
- event payloads

Mutable shared state increases defects.

---

# 41. Testing Review

Every functional change should include tests.

Review:

- unit tests
- integration tests
- regression tests

Critical business logic must not rely solely on manual testing.

---

# 42. Test Quality

Verify:

- deterministic data
- meaningful assertions
- descriptive names
- edge cases
- negative scenarios

Coverage percentage alone is insufficient.

---

# 43. AssertJ

Every AssertJ assertion must include:

```java
.as("...")
```

before assertion methods.

This is mandatory.

---

# 44. Mockito

Review:

- unnecessary mocks
- excessive stubbing
- broad any() matchers
- interaction verification

Mock boundaries, not domain objects.

---

# 45. SonarQube

Every PR must pass Sonar.

Review:

- bugs
- vulnerabilities
- code smells
- duplication
- coverage

New critical findings block approval.

---

# 46. SAST

SAST findings require review.

Suppressions require:

- justification
- narrow scope
- reviewer agreement

---

# 47. Dependency Review

Review newly added libraries.

Questions:

- Is it necessary?
- Is it maintained?
- Does Java already provide this capability?
- Does Spring already provide this capability?

Avoid dependency inflation.

---

# 48. Documentation

Review:

- JavaDoc where appropriate
- ADR updates
- OpenAPI
- architecture documentation
- README updates

Documentation should evolve with code.

---

# 49. Breaking Changes

Breaking changes require:

- architecture review
- migration strategy
- communication
- rollback plan

---

# 50. Pull Request Approval

Approve only when:

- business rules are correct
- architecture is preserved
- tests are adequate
- security is acceptable
- documentation is updated
- quality gates pass

Approval indicates engineering confidence, not perfection.

---

# 51. Approval Checklist

Before approving, verify:

- Business behavior correct?
- Domain model preserved?
- Architecture respected?
- Tests added?
- Sonar passing?
- SAST reviewed?
- Performance acceptable?
- Security validated?
- Logging appropriate?
- Documentation updated?
- Flyway correct?
- API compatible?
- Event contracts preserved?
- No duplicated code?
- No hidden technical debt introduced?

---

# 52. Common Review Anti-Patterns

Avoid:

- approving without reading
- reviewing only formatting
- requesting subjective style changes
- rewriting entire PRs unnecessarily
- ignoring architecture violations
- ignoring security findings
- approving failing quality gates
- accepting TODOs without tracking
- focusing only on coverage percentage

---

# 53. Reviewer Responsibilities

Reviewers are responsible for:

- protecting code quality
- protecting architecture
- protecting maintainability
- protecting production stability
- mentoring through constructive feedback

They are not responsible for implementing the solution.

---

# 54. Author Responsibilities

Authors are responsible for:

- self-review
- running tests
- updating documentation
- explaining design decisions
- responding to review comments
- keeping the PR focused

---

# 55. Architecture Rules

Every approved Pull Request must:

- preserve clean architecture
- preserve DDD boundaries
- maintain backward compatibility
- satisfy security standards
- satisfy testing standards
- satisfy persistence standards
- satisfy messaging standards
- satisfy API standards
- satisfy observability standards

---

# 56. Decision Summary

Every code review validates:

- business correctness
- architectural integrity
- maintainability
- readability
- clean code
- SOLID
- security
- performance
- persistence
- messaging
- APIs
- testing
- SonarQube
- SAST
- documentation
- operational readiness

Code review is considered a mandatory engineering quality gate before any change reaches the main branch.
