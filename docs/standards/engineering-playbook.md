# Engineering Playbook

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Engineering Playbook |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the daily engineering practices adopted by the Enterprise Order Platform.

It consolidates the platform standards into a practical guide for:

- onboarding
- feature development
- defect correction
- architecture decisions
- code review
- testing
- database evolution
- messaging
- security
- observability
- deployment
- incident response
- technical debt management

The playbook translates architectural principles into repeatable engineering workflows.

---

# 2. Intended Audience

This document is intended for:

- backend engineers
- technical leads
- software architects
- quality engineers
- DevOps engineers
- platform engineers
- security engineers
- reviewers
- maintainers
- technical product owners

All contributors are expected to understand and follow the relevant sections before modifying production code.

---

# 3. Engineering Principles

The platform adopts the following principles:

- business behavior first
- architecture before framework convenience
- explicit contracts
- small and reversible changes
- automation over manual repetition
- security by design
- observability by default
- deterministic tests
- backward compatibility
- evidence-based optimization
- immutable database migrations
- ownership of production behavior
- continuous technical improvement

---

# 4. Definition of Engineering Quality

A change is considered complete only when it is:

- functionally correct
- architecturally consistent
- secure
- testable
- observable
- documented
- operationally safe
- backward compatible
- reviewable
- deployable
- supportable in production

Compiling successfully is not sufficient.

Passing tests is not sufficient.

A complete implementation must satisfy the full engineering lifecycle.

---

# 5. Platform Architecture

The platform follows:

- Domain-Driven Design
- Clean Architecture
- Hexagonal Architecture principles
- dependency inversion
- bounded contexts
- aggregate consistency boundaries
- event-driven integration
- CQRS where justified
- Transactional Outbox
- eventual consistency
- infrastructure isolation

---

# 6. Dependency Direction

The approved dependency direction is:

```text
Infrastructure

↓

Application

↓

Domain
```

The Domain layer must remain independent from:

- Spring
- JPA
- Hibernate
- REST
- Amazon SQS / AWS SDK
- Redis
- PostgreSQL
- Docker
- Kubernetes

Infrastructure implements interfaces defined by inner layers.

---

# 7. Layer Responsibilities

## 7.1 Domain

The Domain layer contains:

- aggregates
- entities
- value objects
- domain services
- policies
- specifications
- domain events
- business invariants

The Domain layer must not contain transport or persistence concerns.

## 7.2 Application

The Application layer contains:

- use cases
- commands
- queries
- application services
- ports
- orchestration
- transaction boundaries
- authorization coordination

The Application layer coordinates behavior but should not contain infrastructure implementation details.

## 7.3 Infrastructure

The Infrastructure layer contains:

- JPA entities
- repository adapters
- REST clients
- messaging producers
- messaging consumers
- cache adapters
- configuration
- broker integration
- persistence mapping
- framework-specific code

## 7.4 Interface

The Interface layer contains:

- REST controllers
- request DTOs
- response DTOs
- validation adapters
- exception translation
- OpenAPI configuration

---

# 8. Standard Feature Workflow

Every feature should follow this sequence:

1. understand the business problem
2. identify the bounded context
3. define behavior and invariants
4. assess contract impact
5. design the application use case
6. define required ports
7. implement domain behavior
8. implement infrastructure adapters
9. add automated tests
10. update documentation
11. run quality and security checks
12. create a focused Pull Request
13. deploy safely
14. observe production behavior

Skipping design frequently creates avoidable rework.

---

# 9. Before Starting Development

Before coding, confirm:

- business objective
- acceptance criteria
- impacted services
- aggregate ownership
- current state transitions
- API compatibility
- event compatibility
- database impact
- security requirements
- observability requirements
- rollback strategy
- test strategy

Ambiguities should be resolved before implementation when they affect contracts or architecture.

---

# 10. Business Understanding

The engineer must identify:

- actor
- business trigger
- preconditions
- expected result
- failure scenarios
- state transitions
- external dependencies
- audit requirements
- timing expectations
- consistency expectations

A technical task without business context is incomplete.

---

# 11. Ubiquitous Language

Code must use the same terminology used by the business domain.

Examples:

```text
Order

Approval

Inventory Reservation

Payment Authorization

Cancellation
```

Avoid introducing technical synonyms that conflict with established business terms.

Incorrect terminology creates hidden conceptual inconsistency.

---

# 12. Aggregate Design

An aggregate must:

- protect invariants
- control internal state changes
- expose meaningful behavior
- remain transactionally consistent
- reference external aggregates by identifier
- emit domain events when relevant

Avoid exposing mutable collections or public setters.

---

# 13. Aggregate Behavior

Prefer:

```java
order.approve(approvedBy, comments);
```

instead of:

```java
order.setStatus(OrderStatus.APPROVED);
order.setApprovedBy(approvedBy);
order.setApprovalComments(comments);
```

Behavioral methods preserve invariants and business intent.

---

# 14. Value Objects

Use value objects for concepts with validation or semantics.

Examples:

```text
OrderId

CustomerId

Money

EmailAddress

OrderNumber

Quantity
```

Value objects should be:

- immutable
- validated at construction
- comparable by value
- free from framework dependencies

---

# 15. Domain Events

Domain events should be created when a meaningful business fact occurs.

Examples:

```text
OrderCreated

OrderApproved

OrderCancelled
```

Domain events must:

- use past tense
- remain immutable
- contain business-relevant data
- avoid broker-specific metadata
- remain internal to the bounded context unless mapped to integration events

---

# 16. Application Use Cases

Each use case should represent one business capability.

Examples:

```text
CreateOrderUseCase

ApproveOrderUseCase

CancelOrderUseCase

SearchOrdersUseCase
```

Avoid generic orchestration services with unrelated methods.

---

# 17. Commands and Queries

Commands modify state.

Queries retrieve data.

Examples:

```java
CreateOrderCommand

ApproveOrderCommand

SearchOrdersQuery
```

Command and query models should be immutable.

---

# 18. Application Service Responsibilities

Application services should:

- authorize the operation
- load aggregates
- invoke domain behavior
- persist changes
- store outbox events
- coordinate external ports
- define transaction boundaries

Application services should not:

- contain low-level SQL
- serialize JSON
- expose HTTP semantics
- directly depend on broker clients
- implement framework-specific concerns

---

# 19. Port Design

Ports should describe business or application needs.

Good:

```java
CustomerEligibilityPort

InventoryReservationPort

OrderRepository

IntegrationEventPublisher
```

Avoid ports that expose a technology directly:

```java
KafkaProducerPort

JpaOrderPort

RedisPort
```

Technology belongs to adapters.

---

# 20. Adapter Design

Adapters implement ports using specific technologies.

Examples:

```text
JpaOrderRepositoryAdapter

KafkaIntegrationEventPublisher

RedisIdempotencyAdapter

WebClientInventoryAdapter
```

Adapters translate infrastructure failures into application-level exceptions.

---

# 21. Java Version

The platform uses Java 21.

Approved language features may include:

- records
- sealed types
- pattern matching
- switch expressions
- text blocks
- virtual threads
- improved collection APIs

Features should improve clarity rather than demonstrate novelty.

---

# 22. Java Coding Practices

Code should favor:

- immutability
- constructor injection
- small cohesive classes
- meaningful names
- explicit behavior
- typed domain concepts
- defensive copies
- early validation
- controlled exception hierarchies

Avoid clever but obscure implementations.

---

# 23. Records

Use records for immutable data carriers such as:

- commands
- queries
- API DTOs
- integration event payloads
- configuration values
- projections

Do not use records when identity-based mutable lifecycle behavior is required.

---

# 24. Optional

Use `Optional` primarily for return values that may be absent.

Preferred:

```java
Optional<Order> findById(OrderId orderId);
```

Avoid:

- `Optional` fields
- `Optional` request parameters
- calling `get()` without validating presence
- returning null instead of `Optional.empty()`

---

# 25. Null Handling

Nullability must be explicit.

Use:

- validation
- value objects
- non-null contracts
- empty collections
- Optional return values

Avoid defensive null checks that hide invalid upstream behavior.

---

# 26. Exception Strategy

Exceptions should be categorized.

Examples:

```text
DomainException

ApplicationException

ResourceNotFoundException

BusinessConflictException

DependencyUnavailableException

PersistenceException
```

Exceptions must preserve the original cause when relevant.

---

# 27. Exception Translation

Translate exceptions at boundaries.

Example:

```text
SQLException

↓

OrderPersistenceException

↓

Application error

↓

Problem Details response
```

Do not expose infrastructure-specific failures to external clients.

---

# 28. Spring Boot Practices

Spring Boot should provide infrastructure support, not define domain architecture.

Use Spring for:

- dependency injection
- configuration
- transactions
- web adapters
- security
- data access
- observability
- messaging integration

Avoid Spring annotations in pure domain classes.

---

# 29. Dependency Injection

Constructor injection is mandatory.

Dependencies should be:

- explicit
- immutable
- minimal
- interface-oriented where appropriate

Field injection is prohibited.

---

# 30. Configuration

Configuration must use typed properties.

Example:

```java
@ConfigurationProperties(prefix = "platform.orders")
public record OrderProperties(
        int maximumItems,
        Duration processingTimeout
) {
}
```

Configuration must be validated at startup.

---

# 31. Controllers

Controllers should remain thin.

A controller should:

- receive input
- invoke validation
- map to command or query
- call a use case
- map the result
- return the proper HTTP response

Controllers must not execute business workflows.

---

# 32. API Contracts

API contracts must:

- use business-oriented resource names
- use standard HTTP semantics
- use operation-specific DTOs
- return stable error codes
- document validation
- preserve backward compatibility
- use OpenAPI
- avoid exposing persistence entities

---

# 33. Request Validation

Validate at multiple levels.

Interface validation:

- required fields
- length
- format
- range

Domain validation:

- invariants
- state transitions
- business eligibility
- ownership rules

Database validation:

- uniqueness
- referential integrity
- non-null constraints
- check constraints

---

# 34. Problem Details

Errors must use a consistent Problem Details structure.

Example:

```json
{
  "type": "https://enterprise.example/problems/order-not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "The requested order was not found.",
  "instance": "/api/v1/orders/11111111-1111-1111-1111-111111111111",
  "code": "ORDER_NOT_FOUND",
  "traceId": "f64c7f8ac1304c42"
}
```

Error codes must remain stable.

---

# 35. Persistence

PostgreSQL is the primary relational database.

Persistence must:

- protect integrity
- remain isolated from the domain
- use aggregate-oriented repositories
- use explicit indexes
- avoid unbounded queries
- support optimistic locking
- use immutable Flyway migrations
- be tested with PostgreSQL-compatible integration tests

---

# 36. JPA Entities

JPA entities are infrastructure models.

They should use explicit names such as:

```java
OrderJpaEntity
```

Do not use JPA entities as:

- API responses
- domain aggregates
- event payloads
- cache contracts

---

# 37. Repository Usage

Repositories should be accessed through application or domain-defined contracts.

Avoid:

```text
Controller → Spring Data Repository
```

Preferred:

```text
Controller → Use Case → Repository Port → JPA Adapter
```

---

# 38. Transaction Boundaries

Transactions belong to application use cases.

A transaction should include:

- aggregate loading
- business state change
- persistence
- outbox event persistence

A transaction should not include:

- remote HTTP calls
- email delivery
- long-running operations
- broker publication outside a transactional mechanism

---

# 39. Flyway Rule

Applied migrations must never be modified.

Every schema correction requires a new migration with a new version.

Example:

```text
V43__add_order_approval_index.sql
```

This rule is mandatory in every environment.

---

# 40. Zero-Downtime Database Evolution

Use the Expand-Contract strategy:

1. add compatible schema
2. deploy compatible application code
3. backfill data
4. migrate reads and writes
5. remove obsolete structures later

Avoid deployments that require all instances to update simultaneously.

---

# 41. Query Performance

Every critical query should be evaluated for:

- index use
- cardinality
- N+1 behavior
- result size
- sorting cost
- lock behavior
- query plan stability

Use `EXPLAIN ANALYZE` for evidence-based optimization.

---

# 42. Pagination

Potentially large collections must be paginated.

The API must define:

- default size
- maximum size
- sort fields
- stable ordering
- cursor or offset strategy

Unbounded `findAll` behavior is prohibited for production APIs.

---

# 43. Messaging

The platform assumes at-least-once message delivery.

Therefore:

- consumers must be idempotent
- retries must be bounded
- permanent failures require dead-letter handling
- event IDs remain stable
- correlation context must propagate
- message schemas must be versioned

---

# 44. Transactional Outbox

Use Transactional Outbox when business state and an integration event must be committed atomically.

Correct flow:

```text
Begin transaction

Persist aggregate

Persist outbox event

Commit

Dispatch asynchronously
```

Direct broker publication after database commit is not reliable enough for critical events.

---

# 45. Event Contracts

Integration events must include:

- event ID
- event type
- event version
- occurred timestamp
- producer
- aggregate identifier
- correlation identifier
- causation identifier when relevant
- payload

Published contracts must remain independent from internal models.

---

# 46. Consumer Idempotency

Consumers should persist processed event identifiers.

Recommended uniqueness:

```text
consumer_name + event_id
```

Business state and the processed marker should be committed atomically where possible.

---

# 47. Retry Policy

Retries must define:

- retryable failures
- non-retryable failures
- maximum attempts
- backoff
- jitter
- dead-letter behavior
- operational metrics

Infinite retries are prohibited.

---

# 48. Security

Security must be applied across:

- APIs
- services
- databases
- messaging
- caches
- containers
- Kubernetes
- CI/CD
- logs
- operational tooling

Internal network placement does not eliminate the need for authentication and authorization.

---

# 49. Authentication

Use:

- OAuth 2.0
- OpenID Connect
- JWT bearer tokens
- client credentials for services
- workload identity where supported

JWT validation must include:

- signature
- issuer
- audience
- expiration
- allowed algorithm
- required scopes

---

# 50. Authorization

Authorization must be enforced at:

- endpoint level
- application use-case level
- object level
- tenant level

Frontend restrictions are not authorization controls.

---

# 51. Secrets

Secrets must never be stored in:

- source code
- Git history
- Docker images
- documentation
- plain test data
- logs

Use an approved secret-management solution.

---

# 52. Sensitive Logging

Do not log:

- tokens
- passwords
- secrets
- authorization headers
- private keys
- payment-card data
- personal documents
- complete sensitive payloads

Mask values when limited diagnostic visibility is required.

---

# 53. Input Security

All external data is untrusted.

Validate:

- size
- type
- format
- range
- allowed values
- authorization context
- file content
- URL destination
- message schema

Use allowlists whenever practical.

---

# 54. Dependency Security

Every dependency must be:

- necessary
- maintained
- version-controlled
- scanned
- licensed appropriately

The pipeline must run:

- SAST
- dependency vulnerability scanning
- secret scanning
- container scanning
- infrastructure scanning where applicable

---

# 55. Observability

Every production workflow must be diagnosable through:

- structured logs
- metrics
- traces
- correlation identifiers
- audit events where required
- health indicators

Observability is part of implementation, not an optional later task.

---

# 56. Logging

Logs should include structured fields such as:

```text
event

operation

outcome

elapsedMs

traceId

correlationId

aggregateId

errorCode
```

Avoid building logs with ambiguous free-form text only.

---

# 57. Log Levels

Use:

- `ERROR` for failures requiring operational attention
- `WARN` for abnormal recoverable conditions
- `INFO` for meaningful business or lifecycle events
- `DEBUG` for diagnostic details
- `TRACE` only for narrowly controlled investigation

Expected validation failures should not be logged as system errors.

---

# 58. Metrics

Metrics should represent:

- request volume
- latency
- failure rate
- dependency health
- consumer lag
- outbox backlog
- retry volume
- DLQ growth
- database pool pressure
- cache effectiveness

Metric tags must remain low-cardinality.

---

# 59. Distributed Tracing

Use W3C Trace Context.

Propagate:

```text
traceparent

tracestate
```

Correlation context should continue through:

- HTTP calls
- asynchronous messages
- background jobs
- outbox dispatch
- scheduled processing

---

# 60. Audit

Audit records are required for critical business or security operations.

Examples:

- approval
- cancellation
- manual override
- permission changes
- financial actions
- replay execution

Audit is separate from operational logging.

---

# 61. Testing Strategy

The testing strategy includes:

- unit tests
- application tests
- controller slice tests
- repository integration tests
- broker integration tests
- contract tests
- architecture tests
- security tests
- performance tests where justified

The test level must match the behavior being validated.

---

# 62. Unit Tests

Unit tests should:

- remain fast
- remain deterministic
- avoid framework startup
- validate one behavior
- use controlled fixtures
- test edge cases
- avoid unnecessary mocks

Domain tests should generally use real domain objects.

---

# 63. AssertJ Standard

Every AssertJ assertion must include a description using:

```java
.as("...")
```

Example:

```java
assertThat(order.status())
        .as("Order status after approval")
        .isEqualTo(OrderStatus.APPROVED);
```

This is mandatory.

---

# 64. Test Naming

Test names should describe:

- method or behavior
- scenario
- expected result

Preferred:

```java
testApproveShouldChangeStatusWhenOrderIsPending()
```

Avoid generic names:

```java
testApprove()
```

---

# 65. Deterministic Tests

Tests must avoid:

- random UUID generation without control
- current system time
- network dependence
- shared mutable state
- environment-specific assumptions
- `Thread.sleep`
- execution-order dependence

Use deterministic constants, controlled clocks and Awaitility when asynchronous waiting is required.

---

# 66. Mockito

Use Mockito for external boundaries.

Good mock candidates:

- repositories
- REST ports
- message publishers
- clock abstractions
- external services

Avoid mocking:

- value objects
- aggregates
- simple mappers
- basic collections
- the class under test

---

# 67. Integration Tests

Use production-compatible integration infrastructure.

Examples:

- PostgreSQL Testcontainers
- Redis Testcontainers
- LocalStack or another approved SQS-compatible environment

Do not rely exclusively on H2 for PostgreSQL behavior.

---

# 68. Database Tests

Database integration tests should validate:

- Flyway migrations
- constraints
- indexes where relevant
- repository queries
- locking
- optimistic concurrency
- transaction rollback
- PostgreSQL-specific behavior

---

# 69. Messaging Tests

Messaging tests should validate:

- serialization
- deserialization
- event metadata
- FIFO MessageGroupId / ordering behavior when applicable
- consumer/queue ownership
- idempotency
- retries
- dead-letter routing
- outbox dispatch
- replay safety

---

# 70. API Tests

API tests should validate:

- status codes
- request validation
- response schemas
- Problem Details
- authorization
- object-level access
- pagination
- idempotency
- concurrency conflict
- OpenAPI compatibility

---

# 71. Coverage

The project targets at least:

```text
80% line coverage
```

Coverage is a quality signal, not a substitute for meaningful assertions.

Critical business rules should receive stronger coverage than trivial accessors.

---

# 72. SonarQube

Every Pull Request must satisfy the SonarQube quality gate.

Review:

- bugs
- vulnerabilities
- code smells
- duplication
- complexity
- test coverage
- maintainability

Do not resolve findings through broad suppressions.

---

# 73. SAST

SAST findings must be:

- corrected
- proven false positive
- narrowly suppressed with justification
- tracked through approved risk acceptance

Security findings cannot be ignored solely because tests pass.

---

# 74. Architecture Tests

Use ArchUnit or equivalent tests to enforce:

- dependency direction
- package boundaries
- domain independence
- controller restrictions
- repository isolation
- naming rules
- prohibited dependencies

Architecture rules should be automated when practical.

---

# 75. Code Review

Every production change requires review.

Review must cover:

- business correctness
- architecture
- code quality
- security
- performance
- persistence
- messaging
- compatibility
- tests
- documentation
- operations

Approval is an engineering quality decision.

---

# 76. Pull Request Preparation

Before opening a PR:

1. perform self-review
2. remove temporary code
3. run tests
4. run static analysis
5. validate migrations
6. validate API and event compatibility
7. update documentation
8. provide a clear description
9. identify risks
10. define rollback considerations

---

# 77. Pull Request Scope

A PR should solve one coherent concern.

Avoid combining:

- feature work
- unrelated refactoring
- dependency upgrades
- formatting changes
- migration cleanup
- broad renaming

Focused PRs reduce review risk.

---

# 78. Pull Request Description Template

A PR description should include:

```text
Objective

Business Context

Implementation Summary

Architecture Impact

API Impact

Database Impact

Messaging Impact

Security Impact

Testing Performed

Deployment Considerations

Rollback Strategy
```

---

# 79. Review Comment Classification

Review comments may be classified as:

```text
BLOCKER

REQUIRED

SUGGESTION

QUESTION

NIT
```

Blocking comments should include a clear technical reason.

---

# 80. Definition of Done

A task is done when:

- acceptance criteria are satisfied
- implementation follows architecture
- tests are complete
- quality gates pass
- security checks pass
- contracts remain compatible
- documentation is updated
- migrations are valid
- observability is implemented
- deployment impact is understood
- review comments are resolved

---

# 81. Git Workflow

Branches should be short-lived.

Typical branch names:

```text
feature/order-bulk-approval

fix/outbox-retry-classification

refactor/order-query-projection

docs/messaging-guidelines
```

Avoid long-running branches that diverge significantly from the main branch.

---

# 82. Commit Messages

Use Conventional Commits.

Examples:

```text
feat(orders): add bulk approval workflow

fix(outbox): prevent duplicate dispatcher claim

refactor(search): extract order query projection

test(security): cover cross-tenant access rejection

docs(standards): define engineering playbook
```

Commits should represent coherent changes.

---

# 83. Main Branch

The main branch must remain:

- buildable
- tested
- deployable
- protected
- review-controlled

Direct pushes to the main branch are prohibited.

---

# 84. CI Pipeline

The CI pipeline should execute:

1. compilation
2. unit tests
3. integration tests
4. architecture tests
5. coverage validation
6. formatting or static checks
7. SonarQube
8. SAST
9. dependency scanning
10. secret scanning
11. image build
12. container scanning
13. OpenAPI compatibility
14. event schema compatibility
15. artifact publication

---

# 85. Build Reproducibility

Builds must be reproducible.

Use:

- pinned tool versions
- Gradle Wrapper
- controlled dependencies
- deterministic packaging
- immutable artifacts
- traceable commit metadata

Developer machines must not be required to provide undocumented build state.

---

# 86. Deployment Strategy

Production deployment should use a strategy such as:

- rolling deployment
- blue-green
- canary
- progressive delivery

The strategy must support:

- health validation
- rollback
- mixed-version compatibility
- observability
- traffic control

---

# 87. Backward-Compatible Deployment

During rolling deployments, old and new instances may execute simultaneously.

Therefore:

- API additions must be compatible
- database changes must follow Expand-Contract
- event consumers must tolerate supported versions
- cache formats must remain compatible
- feature flags may be required

---

# 88. Feature Flags

Feature flags may support:

- gradual rollout
- operational disablement
- consumer migration
- schema transition
- canary validation

Every flag must have:

- owner
- secure default
- intended lifetime
- removal plan
- monitoring

Permanent stale flags are technical debt.

---

# 89. Rollback

Every material change should consider rollback.

Rollback analysis must include:

- application version
- database compatibility
- event publication
- cache schema
- background jobs
- external side effects
- data migrations

A code rollback cannot always reverse already-completed business actions.

---

# 90. Production Readiness

Before production release, validate:

- health endpoints
- timeouts
- retry limits
- circuit breakers
- database pool
- resource requests and limits
- dashboards
- alerts
- runbooks
- migration duration
- rollback path
- security configuration
- secret availability

---

# 91. Resilience

Remote dependencies must use controlled resilience patterns.

Possible patterns:

- timeout
- retry
- circuit breaker
- bulkhead
- rate limiter
- fallback

Patterns must be selected according to failure semantics.

---

# 92. Timeouts

Every remote call requires explicit timeouts.

Consider:

- connection timeout
- acquisition timeout
- response timeout
- operation timeout
- request budget

Missing timeouts can cause cascading failures.

---

# 93. Retries

Retry only transient failures.

Do not retry:

- validation failures
- authorization failures
- unsupported operations
- permanent business rejection
- malformed payloads

Retries must be bounded and use backoff.

---

# 94. Circuit Breakers

Circuit breakers should protect unstable remote dependencies.

Configuration should define:

- failure threshold
- sliding window
- open duration
- permitted half-open calls
- failure classification
- fallback behavior
- metrics

Circuit breakers must not hide persistent business errors.

---

# 95. Bulkheads

Bulkheads protect limited resources.

Use bounded concurrency for:

- database connections
- external API calls
- broker processing
- file processing
- background tasks

Virtual threads do not remove the need for bulkheads.

---

# 96. Caching

Use caching only when:

- data can safely be stale
- lookup cost justifies it
- invalidation is understood
- authorization is preserved
- failure behavior is defined

Do not cache because it appears to improve architecture diagrams.

---

# 97. Cache Keys

Cache keys must include all relevant dimensions.

Examples:

```text
tenant

customer

locale

permission scope

resource identifier

version
```

Incomplete cache keys can cause data leakage or incorrect responses.

---

# 98. Cache Failure

The application must define behavior when cache infrastructure is unavailable.

Possible behavior:

- bypass cache
- use local fallback
- fail the operation
- return stale data within policy

The decision depends on data criticality.

---

# 99. Performance Engineering

Performance work should be evidence-driven.

Use:

- metrics
- traces
- query plans
- profiler data
- load tests
- production observations

Avoid speculative micro-optimization.

---

# 100. Performance Review Areas

Review:

- database round trips
- N+1 queries
- serialization cost
- remote-call fan-out
- collection size
- algorithmic complexity
- thread usage
- memory allocation
- connection-pool saturation
- cache effectiveness

---

# 101. Virtual Threads

Virtual threads may be used for blocking I/O workloads.

They do not:

- increase database connection capacity
- remove external service limits
- make shared mutable state safe
- eliminate the need for timeouts
- eliminate concurrency control

Executors must have clear ownership and lifecycle.

---

# 102. Parallelism

Parallel processing is appropriate when:

- tasks are independent
- concurrency is bounded
- dependencies support the load
- ordering is not required
- failures are handled explicitly
- context propagation is preserved

Parallelism should not increase operational risk for minor latency gains.

---

# 103. Documentation

Documentation must evolve with implementation.

Update as applicable:

- README
- architecture documents
- ADRs
- OpenAPI
- event catalog
- database documentation
- runbooks
- configuration reference
- operational dashboards
- onboarding material

Outdated documentation is a defect.

---

# 104. Architecture Decision Records

Create an ADR when a decision:

- affects multiple services
- introduces a new technology
- changes a major pattern
- creates long-term constraints
- has significant alternatives
- affects security or data consistency

An ADR should capture context, decision, alternatives and consequences.

---

# 105. Technical Debt

Technical debt should be:

- identified
- described
- prioritized
- owned
- tracked
- reviewed

Avoid hidden debt through comments such as:

```text
TODO fix later
```

without a tracked work item.

---

# 106. Refactoring

Refactoring should:

- preserve behavior
- improve design
- remain covered by tests
- avoid unrelated changes
- be performed incrementally

Large refactors should define migration steps and rollback options.

---

# 107. Dependency Upgrades

Dependency upgrades should be isolated when practical.

Validate:

- release notes
- breaking changes
- transitive dependencies
- security impact
- runtime behavior
- test compatibility
- deployment compatibility

Major framework upgrades require explicit planning.

---

# 108. Incident Response

During an incident:

1. assess impact
2. stabilize the system
3. preserve evidence
4. communicate status
5. mitigate the immediate cause
6. verify recovery
7. perform root-cause analysis
8. define preventive actions
9. add regression protection
10. update runbooks

Avoid speculative changes during active incidents.

---

# 109. Root-Cause Analysis

A root-cause analysis should identify:

- customer impact
- timeline
- triggering event
- technical cause
- contributing factors
- detection gap
- response gap
- corrective actions
- preventive actions
- owners and deadlines

The objective is learning and prevention, not blame.

---

# 110. Hotfixes

Hotfixes must still include:

- focused scope
- review
- tests
- security consideration
- rollback plan
- post-deployment validation

Urgency does not justify bypassing all engineering controls.

---

# 111. Operational Ownership

Teams own their services in production.

Ownership includes:

- dashboards
- alerts
- runbooks
- incident response
- dependency health
- capacity
- error budgets
- documentation
- recovery procedures

Development does not end at deployment.

---

# 112. Service Readiness Checklist

A service is production-ready when it has:

- documented owner
- health endpoints
- structured logging
- metrics
- tracing
- alerts
- runbook
- secure configuration
- resource limits
- database migrations
- backup considerations
- resilience configuration
- tests
- deployment strategy
- rollback strategy

---

# 113. New Service Checklist

Before creating a new service, confirm:

- a separate bounded context exists
- independent lifecycle is required
- independent scaling is required
- ownership is clear
- data ownership is clear
- integration contracts are defined
- operational overhead is justified

Do not create a microservice only to organize code.

---

# 114. Service Boundaries

A service should own:

- its business capability
- its data
- its contracts
- its deployment
- its operational behavior

Cross-service database access is prohibited.

---

# 115. Shared Libraries

Shared libraries should be limited to stable technical concerns.

Appropriate examples:

- logging conventions
- tracing propagation
- Problem Details support
- security utilities
- test utilities

Avoid shared domain libraries that couple bounded contexts.

---

# 116. Shared Library Evolution

Shared libraries must:

- use semantic versioning
- preserve compatibility
- avoid mandatory synchronized deployments
- remain narrowly focused
- provide migration notes
- be tested independently

A shared library should not become a hidden monolith.

---

# 117. Local Development

Local development should be reproducible through:

- documented prerequisites
- Docker Compose where useful
- deterministic configuration
- local database migrations
- test fixtures
- service mocks or containers
- clear startup commands

Avoid undocumented manual environment setup.

---

# 118. Developer Validation Commands

The repository should document commands similar to:

```bash
./gradlew clean build
```

```bash
./gradlew test
```

```bash
./gradlew integrationTest
```

```bash
./gradlew jacocoTestReport
```

```bash
./gradlew sonar
```

The exact tasks must match the build configuration.

---

# 119. Code Ownership

Critical areas should have clear reviewers or owners.

Examples:

- security
- database migrations
- messaging contracts
- platform configuration
- authentication
- payment workflows
- deployment manifests

Ownership improves review quality and incident routing.

---

# 120. Engineering Metrics

Useful engineering metrics may include:

- deployment frequency
- lead time for changes
- change failure rate
- recovery time
- escaped defects
- flaky test rate
- quality-gate failures
- dependency age
- technical debt age

Metrics should support improvement, not individual punishment.

---

# 121. Continuous Improvement

Teams should periodically review:

- recurring incidents
- slow reviews
- flaky tests
- build duration
- deployment failures
- architecture violations
- security findings
- operational toil
- recurring manual work

Repeated problems should result in system-level improvements.

---

# 122. Anti-Patterns

The following practices are prohibited:

- business logic in controllers
- direct controller-to-repository access
- exposing JPA entities
- modifying applied Flyway migrations
- publishing events without idempotency considerations
- infinite retries
- hardcoded secrets
- unbounded queries
- logging sensitive payloads
- ignoring SonarQube or SAST findings
- using `Thread.sleep` in tests
- random test data without control
- direct pushes to the main branch
- undocumented breaking changes
- shared production credentials
- speculative performance optimization
- microservices without clear boundaries
- broad security suppressions
- permanent feature flags without ownership
- production changes without observability
- incidents without preventive follow-up

---

# 123. Daily Development Checklist

Before completing daily development work, verify:

- Is the business behavior clear?
- Is the correct bounded context being modified?
- Are invariants protected?
- Are dependencies flowing inward?
- Are contracts compatible?
- Are errors explicit?
- Are tests deterministic?
- Are AssertJ descriptions present?
- Are database migrations new and immutable?
- Is sensitive data protected?
- Are logs and metrics sufficient?
- Are retries and timeouts bounded?
- Is the code understandable to another engineer?
- Is documentation current?
- Is the change ready for review?

---

# 124. Pull Request Checklist

Before requesting approval:

- build passes
- tests pass
- coverage is acceptable
- SonarQube passes
- SAST is reviewed
- dependency scans pass
- migrations are validated
- API compatibility is checked
- event compatibility is checked
- architecture tests pass
- documentation is updated
- deployment impact is described
- rollback is considered
- no secrets or sensitive data are present

---

# 125. Production Release Checklist

Before production deployment:

- artifact is immutable
- image scan passes
- configuration is validated
- secrets are available
- migrations are reviewed
- compatibility is preserved
- dashboards are available
- alerts are active
- runbook is current
- rollback path is understood
- health checks are configured
- capacity is sufficient
- feature flags are correctly configured
- post-deployment validation is defined

---

# 126. Architecture Rules

Every implementation must:

- preserve bounded-context ownership
- keep the domain independent
- use application ports
- isolate infrastructure
- enforce business invariants
- preserve contract compatibility
- secure every trust boundary
- validate external input
- use immutable database migrations
- assume duplicate message delivery
- remain observable
- include deterministic tests
- pass automated quality gates
- support safe deployment and recovery

---

# 127. Decision Summary

The Enterprise Order Platform adopts:

- Domain-Driven Design
- Clean and Hexagonal Architecture
- Java 21
- Spring Boot
- PostgreSQL
- Spring Data JPA and Hibernate
- Flyway with immutable migrations
- Amazon SQS under the current baseline; another broker requires an explicit ADR
- Transactional Outbox
- idempotent consumers
- Redis for justified caching scenarios
- REST APIs with Problem Details
- OAuth 2.0, OpenID Connect and JWT
- Docker and Kubernetes
- structured logging, metrics and tracing
- JUnit 5, AssertJ, Mockito and Testcontainers
- JaCoCo with an 80% coverage target
- SonarQube and SAST quality gates
- short-lived branches and Conventional Commits
- mandatory code review
- backward-compatible deployments
- operational ownership
- continuous engineering improvement

This playbook is the practical reference for designing, implementing, reviewing, deploying and operating the platform.
