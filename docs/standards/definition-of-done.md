# Definition of Done

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Definition of Done |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the objective completion criteria adopted by the Enterprise Order Platform.

A work item is considered done only when it satisfies the applicable criteria for:

- business behavior
- architecture
- implementation
- testing
- security
- persistence
- messaging
- APIs
- observability
- documentation
- deployment
- operations
- quality assurance

The Definition of Done provides a shared quality baseline for stories, defects, refactorings, technical tasks and operational changes.

---

# 2. Scope

This Definition of Done applies to:

- new features
- defect corrections
- refactorings
- architectural changes
- database migrations
- API changes
- event contract changes
- security changes
- performance improvements
- infrastructure changes
- dependency upgrades
- documentation changes
- production hotfixes

Not every criterion applies to every work item.

Non-applicable criteria should be explicitly identified rather than silently ignored.

---

# 3. Core Rule

A work item is not done merely because:

- the code compiles
- the implementation works locally
- unit tests pass
- the Pull Request is open
- the developer has completed coding
- the change has been merged
- the change has been deployed

A work item is done only when it is functionally correct, technically sound, secure, tested, observable, documented and operationally safe.

---

# 4. Responsibility

The author is responsible for demonstrating that the work item satisfies the Definition of Done.

Reviewers are responsible for validating the evidence.

The team is responsible for rejecting incomplete work, even when delivery pressure exists.

---

# 5. Evidence

Completion criteria should be supported by evidence such as:

- automated test results
- quality-gate results
- screenshots where relevant
- API examples
- migration validation
- logs
- metrics
- traces
- benchmark results
- architecture documentation
- deployment validation
- rollback validation
- linked work items

Claims without reproducible evidence should not be treated as complete.

---

# 6. General Completion Criteria

Every completed work item must satisfy the following general criteria:

- acceptance criteria are met
- implementation matches the business objective
- no known blocking defect remains
- code follows project standards
- code has been self-reviewed
- automated tests pass
- quality gates pass
- security checks pass
- documentation is updated
- compatibility impact is understood
- deployment impact is understood
- rollback considerations are documented
- review comments are resolved
- the change is approved
- post-deployment validation is defined where applicable

---

# 7. Business Completion

The business behavior must be complete.

Verify:

- expected actor is supported
- preconditions are enforced
- success behavior is correct
- failure behavior is correct
- state transitions are valid
- business invariants remain protected
- edge cases are handled
- acceptance criteria are covered
- business terminology is consistent
- audit requirements are implemented

The implementation must not rely on undefined or implicit business behavior.

---

# 8. Acceptance Criteria

Each acceptance criterion must be:

- implemented
- testable
- verified
- traceable to code or tests
- unambiguous

Unresolved acceptance criteria prevent completion.

---

# 9. Domain Completion

For domain-related changes, verify:

- the correct bounded context owns the behavior
- aggregate boundaries remain valid
- invariants are protected
- domain methods express business intent
- value objects are used where appropriate
- domain events are emitted correctly
- ubiquitous language is preserved
- domain logic is free from infrastructure concerns
- mutable state is controlled
- invalid states cannot be created through public APIs

---

# 10. Architecture Completion

The implementation must respect the approved architecture.

Verify:

- dependency direction is preserved
- Domain does not depend on frameworks
- Application does not depend on infrastructure implementations
- Infrastructure implements inner-layer ports
- controllers do not access repositories directly
- persistence models do not leak outside infrastructure
- integration DTOs do not become domain models
- package boundaries are respected
- no cyclic dependency is introduced
- architecture tests pass

---

# 11. Design Quality

The implementation must demonstrate acceptable design quality.

Verify:

- classes have cohesive responsibilities
- methods remain understandable
- naming reflects business intent
- duplication is minimized
- complexity is controlled
- dependencies are limited
- extension points are justified
- abstractions provide actual value
- no unnecessary framework coupling is introduced
- no speculative architecture is added

---

# 12. Clean Code Completion

Code must be:

- readable
- maintainable
- explicit
- cohesive
- consistently formatted
- free from dead code
- free from commented-out code
- free from unexplained magic values
- free from unnecessary duplication
- free from unresolved temporary workarounds

Temporary code must be removed before completion.

---

# 13. Java Completion

Java changes must satisfy:

- Java 21 compatibility
- constructor injection
- immutable dependencies
- appropriate record usage
- controlled nullability
- correct Optional usage
- safe resource handling
- preserved exception causes
- no field injection
- no uncontrolled shared mutable state
- no unnecessary reflection
- no deprecated APIs without justification

---

# 14. Spring Boot Completion

Spring Boot changes must satisfy:

- correct layer placement
- typed configuration
- startup validation
- thin controllers
- explicit transaction boundaries
- secure endpoint defaults
- framework annotations outside pure domain code
- stable exception translation
- no hidden bean cycles
- no unnecessary application-context coupling in unit tests

---

# 15. API Completion

For API changes, verify:

- resource naming follows standards
- HTTP method is correct
- status codes are correct
- request DTO is operation-specific
- response DTO is stable
- validation is complete
- Problem Details is used for errors
- error codes are stable
- authorization is enforced
- object-level access is enforced
- pagination is applied where required
- sorting fields are allowlisted
- request and response examples are documented
- OpenAPI is updated
- backward compatibility is preserved or explicitly versioned

---

# 16. API Compatibility

Before completing an API change, verify:

- existing fields were not removed unintentionally
- existing field types were not changed incompatibly
- optional fields were not made mandatory
- enum behavior remains compatible
- status codes remain compatible
- path semantics remain stable
- validation was not tightened without impact analysis
- clients have a migration strategy for breaking changes

Breaking changes require explicit approval and versioning.

---

# 17. API Error Completion

API errors must:

- use the standard Problem Details contract
- include a stable business or technical error code
- avoid stack traces
- avoid internal class names
- avoid SQL details
- avoid raw downstream payloads
- avoid sensitive values
- include a trace identifier where available
- distinguish validation, conflict, not found and dependency failures

---

# 18. Persistence Completion

For persistence changes, verify:

- repository boundaries are preserved
- domain objects are not JPA entities
- aggregate persistence remains consistent
- queries are bounded
- pagination is applied
- N+1 risks are addressed
- fetch strategy is intentional
- indexes support real query patterns
- constraints protect integrity
- optimistic locking is used where required
- database exceptions are translated
- PostgreSQL behavior is tested
- migration impact is understood

---

# 19. Flyway Completion

Every database change must satisfy:

- a new migration was created
- migration naming is correct
- migration version is unique
- no applied migration was modified
- schema changes are backward compatible where required
- constraints are explicit
- indexes are included where justified
- large updates are batched where required
- long-lock risks are assessed
- upgrade path is tested
- clean installation is tested
- rollback or forward-fix strategy is understood

Applied migrations are immutable.

---

# 20. Database Integrity

Database changes must preserve:

- primary keys
- foreign keys
- unique constraints
- check constraints
- non-null requirements
- tenant isolation
- referential integrity
- version columns where required
- audit columns where required

Application validation does not replace database integrity.

---

# 21. Database Performance

Critical database changes must be evaluated for:

- index usage
- sequential scans
- sort cost
- result cardinality
- lock duration
- transaction duration
- connection-pool impact
- query-plan stability
- batch behavior
- write amplification

Use `EXPLAIN ANALYZE` when the change affects critical queries.

---

# 22. Transaction Completion

Transactions must:

- begin at the application boundary
- remain short
- include only required consistency work
- avoid remote HTTP calls
- avoid email delivery
- avoid long calculations
- persist aggregate and outbox atomically where required
- handle rollback correctly
- define concurrency behavior
- preserve idempotency

---

# 23. Messaging Completion

For messaging changes, verify:

- message semantics are explicit
- event or command name is correct
- envelope is complete
- event ID remains stable across retries
- event version is explicit
- payload is minimal
- schema compatibility is preserved
- correlation context is propagated
- partition key is correct
- ordering assumptions are documented
- consumer is idempotent
- retry policy is bounded
- dead-letter handling is configured
- replay behavior is understood
- observability is implemented
- integration tests pass

---

# 24. Event Contract Completion

Every public event must document:

- owner
- destination
- event type
- version
- key
- envelope
- payload
- required fields
- optional fields
- ordering
- delivery semantics
- retention
- consumers
- compatibility policy
- deprecation policy

Events are considered external contracts.

---

# 25. Transactional Outbox Completion

Outbox-related work must verify:

- aggregate and outbox record commit atomically
- rollback removes both changes
- payload is immutable
- retries preserve event identity
- concurrent dispatchers are safe
- attempts are tracked
- next attempt is calculated
- terminal failures are visible
- sent records follow retention policy
- dispatcher metrics exist
- backlog alerting exists where critical

---

# 26. Consumer Idempotency Completion

An idempotent consumer must demonstrate:

- first delivery is processed
- duplicate delivery is ignored
- concurrent duplicate delivery is processed once
- marker and business state commit atomically where possible
- rollback permits safe redelivery
- duplicate handling is observable
- external side effects are protected

---

# 27. Retry Completion

Retry behavior must define:

- retryable failures
- non-retryable failures
- maximum attempts
- initial delay
- backoff
- jitter
- timeout
- dead-letter destination
- final failure behavior
- metrics
- alerts where required

Infinite retry prevents completion.

---

# 28. Security Completion

Every change must be reviewed for security impact.

Verify:

- authentication is enforced
- authorization is enforced
- object-level authorization is enforced
- tenant isolation is preserved
- input is validated
- output is safe
- secrets are externally managed
- sensitive values are not logged
- transport is protected
- dependencies are scanned
- security findings are resolved
- audit is implemented where required
- failures deny access safely

---

# 29. Authentication Completion

Authentication-related changes must verify:

- token signature
- issuer
- audience
- expiration
- not-before time
- allowed algorithm
- token type
- required claims
- key rotation behavior
- failure behavior
- test coverage for invalid tokens

---

# 30. Authorization Completion

Authorization-related changes must verify:

- permission checks
- scope checks
- role mapping
- object ownership
- tenant restriction
- company or business-unit restriction
- administrative separation
- service identity
- delegated user context
- negative test scenarios

Frontend-only authorization does not satisfy completion.

---

# 31. Secrets Completion

Secret-related work must verify:

- no secret exists in source control
- no secret exists in test fixtures
- no secret exists in image layers
- runtime injection is configured
- rotation is supported
- access is least privilege
- secret failures are observable
- documentation contains placeholders only
- exposed credentials were revoked, not only deleted

---

# 32. Input Validation Completion

Input validation must cover:

- required fields
- length limits
- numeric ranges
- formats
- allowed enum values
- collection limits
- nested object limits
- file size
- file type
- URL scheme
- sort fields
- filter fields
- message schema

Unbounded input prevents completion.

---

# 33. Secure Error Completion

Error handling must not expose:

- credentials
- tokens
- personal data
- stack traces
- SQL
- internal paths
- class names
- infrastructure hostnames
- raw provider errors
- private configuration

Internal logs must retain safe diagnostic context.

---

# 34. Logging Completion

Logging must be:

- structured
- useful
- correctly leveled
- correlated
- free from sensitive data
- free from unnecessary full payloads
- consistent with logging standards

Critical operations should include:

```text
event

operation

outcome

elapsedMs

traceId

correlationId

errorCode
```

---

# 35. Metrics Completion

Metrics must be added when the change introduces a significant operational behavior.

Consider:

- request count
- success count
- failure count
- latency
- retries
- dead-letter volume
- consumer lag
- outbox backlog
- dependency failures
- cache effectiveness
- pool saturation
- batch throughput

Metric tags must remain low-cardinality.

---

# 36. Tracing Completion

Distributed operations must propagate tracing context through:

- inbound HTTP
- outbound HTTP
- asynchronous messages
- outbox dispatch
- scheduled tasks
- background processing

Trace propagation must not introduce sensitive information.

---

# 37. Audit Completion

Audit is required for critical actions such as:

- approvals
- cancellations
- manual overrides
- permission changes
- financial actions
- administrative operations
- event replay
- security changes

Audit records must include:

- actor
- action
- resource
- timestamp
- outcome
- reason where relevant
- correlation identifier

---

# 38. Unit Test Completion

Unit tests must:

- cover new behavior
- cover failure scenarios
- remain deterministic
- remain isolated
- avoid framework startup where unnecessary
- avoid uncontrolled random values
- avoid current system time
- avoid `Thread.sleep`
- use meaningful fixtures
- include meaningful assertions
- verify behavior rather than implementation detail

---

# 39. AssertJ Completion

Every AssertJ assertion must include a description.

Example:

```java
assertThat(result.status())
        .as("Order status after successful approval")
        .isEqualTo(OrderStatus.APPROVED);
```

Assertions without `.as("...")` do not satisfy the project testing standard.

---

# 40. Test Naming Completion

Test names must explain:

- behavior under test
- scenario
- expected outcome

Preferred:

```java
testApproveShouldRejectCancelledOrder()
```

Avoid:

```java
testApprove()
```

---

# 41. Mockito Completion

Mockito-based tests must verify:

- mocks represent external boundaries
- stubbing is necessary
- argument matchers are not overly broad
- no unnecessary interactions are tested
- no domain object is mocked without justification
- captured arguments are asserted meaningfully
- unused stubs are removed

---

# 42. Integration Test Completion

Integration tests must be included when behavior depends on:

- PostgreSQL
- Kafka
- RabbitMQ
- Redis
- SQS-compatible infrastructure
- HTTP serialization
- security configuration
- transaction behavior
- locking
- migrations
- framework configuration

Use Testcontainers when production-compatible infrastructure behavior is required.

---

# 43. Repository Test Completion

Repository tests should verify:

- persistence
- retrieval
- query filters
- sorting
- pagination
- constraints
- projections
- optimistic locking
- transaction rollback
- PostgreSQL-specific behavior

H2-only validation is insufficient for PostgreSQL-specific behavior.

---

# 44. API Test Completion

API tests should verify:

- successful response
- validation failure
- not found
- conflict
- unauthorized
- forbidden
- cross-tenant rejection
- malformed input
- pagination
- sorting
- error contract
- response fields
- backward compatibility where applicable

---

# 45. Messaging Test Completion

Messaging tests should verify:

- serialization
- deserialization
- envelope metadata
- schema version
- partition key
- duplicate handling
- retry classification
- dead-letter routing
- outbox dispatch
- consumer recovery
- correlation propagation
- replay safety

---

# 46. Security Test Completion

Security tests must include relevant negative scenarios.

Examples:

- missing token
- expired token
- wrong audience
- wrong issuer
- insufficient permission
- wrong tenant
- unauthorized object
- invalid signature
- unsupported algorithm
- malicious input
- secret masking
- protected actuator access

---

# 47. Regression Test Completion

Every corrected defect must include a regression test when technically feasible.

The regression test must fail before the correction and pass after it.

The test should reproduce the real defect condition rather than a simplified unrelated scenario.

---

# 48. Coverage Completion

The project targets at least:

```text
80% line coverage
```

Completion also requires:

- meaningful branch coverage
- critical business-path coverage
- failure-scenario coverage
- no artificial tests created only to increase the metric
- no exclusion of relevant code without justification

---

# 49. Quality Gate Completion

Before completion:

- compilation passes
- unit tests pass
- integration tests pass
- architecture tests pass
- coverage threshold passes
- SonarQube quality gate passes
- no new blocker issue exists
- no new critical issue exists
- duplication remains acceptable
- cognitive complexity remains acceptable
- static-analysis suppressions are justified

---

# 50. SAST Completion

SAST findings must be:

- corrected
- confirmed as false positive
- narrowly suppressed with justification
- covered by approved risk acceptance

A finding cannot be ignored only because the vulnerable path is not currently expected to execute.

---

# 51. Dependency Completion

Dependency changes must verify:

- dependency is necessary
- version is controlled
- release notes are reviewed
- transitive impact is understood
- vulnerability scan passes
- license is acceptable
- compatibility tests pass
- unused previous dependencies are removed
- SBOM is updated

---

# 52. Container Completion

Container changes must verify:

- minimal base image
- trusted image source
- non-root user
- no embedded secret
- no unnecessary build tool in runtime
- vulnerability scan passes
- immutable version or digest
- health behavior works
- writable directories are explicit
- unnecessary capabilities are removed

---

# 53. Kubernetes Completion

Kubernetes changes must verify:

- dedicated service account
- least-privilege RBAC
- resource requests
- resource limits
- readiness probe
- liveness probe
- secure secret integration
- non-root security context
- no privilege escalation
- network policy where required
- ingress restrictions
- immutable image reference
- environment-specific configuration

---

# 54. Configuration Completion

Configuration changes must verify:

- typed property binding
- startup validation
- safe defaults
- environment-specific overrides
- no secret exposure
- documentation
- backward compatibility
- failure behavior
- test coverage
- no hidden dependency on a developer machine

---

# 55. Documentation Completion

Documentation must be updated when the change affects:

- architecture
- APIs
- events
- database
- configuration
- security
- deployment
- operations
- onboarding
- runbooks
- troubleshooting
- local development

Outdated documentation prevents completion.

---

# 56. README Completion

Update the README when the change affects:

- project purpose
- prerequisites
- startup commands
- available modules
- supported infrastructure
- configuration
- local environment
- test execution
- deployment process
- project status

---

# 57. OpenAPI Completion

OpenAPI must reflect:

- new endpoints
- removed or deprecated endpoints
- parameters
- headers
- schemas
- validation
- examples
- status codes
- error responses
- authorization requirements
- pagination
- idempotency
- concurrency headers

Generated documentation must match runtime behavior.

---

# 58. ADR Completion

An ADR is required when the change:

- introduces a new technology
- changes an architectural pattern
- affects multiple bounded contexts
- changes consistency guarantees
- changes deployment strategy
- creates long-term constraints
- changes authentication or authorization architecture
- introduces a significant data model
- has major alternatives with trade-offs

---

# 59. Operational Documentation

Operationally relevant changes must update:

- dashboards
- alerts
- runbooks
- dependency maps
- support procedures
- recovery procedures
- replay procedures
- migration procedures
- rollback procedures

---

# 60. Performance Completion

Performance-related work must include:

- baseline
- hypothesis
- measurement method
- result
- comparison
- resource impact
- regression protection
- limitations

Claims such as “faster” or “more scalable” require evidence.

---

# 61. Load Test Completion

Load testing is required when the change materially affects:

- throughput
- latency
- concurrency
- database load
- broker consumption
- memory usage
- connection pools
- batch processing
- large payloads
- bulk operations

The test must reflect realistic behavior.

---

# 62. Resilience Completion

Remote-dependency changes must define:

- connection timeout
- response timeout
- acquisition timeout
- retry policy
- retry classification
- circuit breaker
- bulkhead
- fallback
- observability
- failure response

Defaults must be reviewed.

---

# 63. Cache Completion

Cache changes must define:

- business justification
- cache key
- tenant or authorization dimensions
- TTL
- invalidation
- stale-data policy
- fallback
- serialization
- failure behavior
- observability
- tests

A cache without a valid invalidation strategy is incomplete.

---

# 64. Concurrency Completion

Concurrent behavior must verify:

- thread safety
- bounded concurrency
- ordering requirements
- race conditions
- locking strategy
- duplicate handling
- executor ownership
- executor shutdown
- context propagation
- downstream capacity

Virtual threads do not remove these requirements.

---

# 65. Batch Processing Completion

Batch-related work must define:

- batch size
- pagination
- transaction size
- partial failure behavior
- retry granularity
- idempotency
- memory limits
- observability
- restart behavior
- duplicate behavior
- processing order where relevant

---

# 66. Feature Flag Completion

A feature flag must have:

- owner
- purpose
- secure default
- environment strategy
- rollout plan
- rollback plan
- monitoring
- expiration date
- removal work item

A permanent unmanaged flag is incomplete technical debt.

---

# 67. Deployment Completion

Before deployment, verify:

- artifact is immutable
- image scan passes
- configuration is available
- secrets are available
- migrations are compatible
- old and new versions can coexist
- health checks are correct
- dashboards are available
- alerts are configured
- rollout strategy is defined
- rollback strategy is defined
- post-deployment checks are defined

---

# 68. Post-Deployment Validation

After deployment, validate:

- service starts successfully
- readiness becomes healthy
- liveness remains healthy
- error rate is normal
- latency is normal
- database migration completed
- consumer lag is acceptable
- outbox backlog is acceptable
- no new DLQ growth exists
- logs contain no unexpected failures
- key business flow works
- feature flags are correct

---

# 69. Rollback Completion

Rollback planning must consider:

- application artifact
- database compatibility
- event compatibility
- cache compatibility
- background jobs
- message consumers
- data already changed
- external side effects
- feature flags
- deployment configuration

A rollback plan must not assume that all business actions are reversible.

---

# 70. Production Readiness

A new service or major capability must have:

- owner
- support contact
- health endpoints
- logging
- metrics
- tracing
- alerts
- dashboards
- runbook
- secure configuration
- capacity assumptions
- resource limits
- resilience policies
- backup considerations
- recovery procedures
- deployment strategy
- rollback strategy

---

# 71. Defect Completion

A defect is done when:

- root cause is understood
- defect scenario is reproduced
- correction addresses the cause
- regression test is added
- related scenarios are reviewed
- no workaround remains hidden
- operational impact is understood
- documentation is updated where relevant
- deployment risk is assessed
- production validation is defined

A symptom-only correction is incomplete when the underlying cause remains active.

---

# 72. Refactoring Completion

A refactoring is done when:

- behavior is preserved
- tests demonstrate preservation
- duplication or complexity is reduced
- architecture is improved or maintained
- no unrelated behavior change is introduced
- performance is not degraded unexpectedly
- public contracts remain compatible
- documentation is updated if structure changes
- dead code is removed

---

# 73. Technical Debt Completion

A technical-debt item is done when:

- the identified debt is actually removed or reduced
- no equivalent workaround remains
- architecture impact is validated
- tests protect the improvement
- quality metrics improve where relevant
- documentation reflects the new state
- follow-up debt is tracked explicitly

Moving the debt to another class does not complete the task.

---

# 74. Hotfix Completion

A hotfix must still satisfy:

- focused scope
- root-cause understanding
- regression test
- review
- security validation
- deployment plan
- rollback plan
- production validation
- follow-up analysis where controls were temporarily reduced

Urgency may reduce ceremony but not eliminate engineering responsibility.

---

# 75. Documentation-Only Completion

Documentation-only work must verify:

- technical accuracy
- consistency with implementation
- correct paths
- correct commands
- correct terminology
- valid examples
- valid links
- no secret or sensitive information
- readable structure
- review approval

---

# 76. Work Item Closure

Before closing a work item, verify:

- the final implementation is merged
- required deployment is complete
- post-deployment validation succeeded
- acceptance criteria are confirmed
- linked defects are resolved
- documentation is available
- follow-up items are created
- evidence is attached
- no hidden manual step remains

---

# 77. Exception Process

A criterion may be waived only through an explicit exception.

The exception must include:

- criterion not satisfied
- technical reason
- business justification
- risk
- compensating control
- owner
- approval
- expiration
- remediation work item

Exceptions must not become informal permanent standards.

---

# 78. Definition of Ready Relationship

The Definition of Ready ensures a work item is sufficiently understood before development.

The Definition of Done ensures the work item is sufficiently complete after development.

Work should not start with unresolved critical ambiguity and should not finish with unresolved critical quality gaps.

---

# 79. Author Checklist

Before requesting review, the author must confirm:

- acceptance criteria implemented
- self-review completed
- business behavior verified
- architecture preserved
- tests added
- tests passing
- AssertJ descriptions present
- coverage acceptable
- SonarQube passing
- SAST reviewed
- dependencies scanned
- migrations validated
- API compatibility checked
- event compatibility checked
- security reviewed
- observability added
- documentation updated
- deployment impact documented
- rollback considered

---

# 80. Reviewer Checklist

Before approval, the reviewer must confirm:

- implementation solves the correct problem
- business rules are correct
- design is maintainable
- architecture is preserved
- security is acceptable
- performance risk is acceptable
- tests are meaningful
- compatibility is preserved
- migration is safe
- messaging behavior is reliable
- observability is sufficient
- documentation is current
- quality gates pass
- operational risk is understood

---

# 81. Product Owner Checklist

Where product validation is required, confirm:

- acceptance criteria are satisfied
- expected business behavior is demonstrated
- error scenarios are acceptable
- user-visible behavior is correct
- wording and terminology are correct
- workflow is complete
- deferred behavior is explicitly tracked

---

# 82. DevOps Checklist

For infrastructure or deployment changes, confirm:

- deployment manifests are valid
- secrets are available
- resource limits are configured
- probes are configured
- image is immutable
- scan results are acceptable
- migration order is correct
- rollout is safe
- rollback is possible
- monitoring is available
- alerts are configured
- environment separation is preserved

---

# 83. Quality Engineering Checklist

Where dedicated quality validation applies, confirm:

- acceptance scenarios are covered
- regression scenarios are covered
- negative scenarios are covered
- integration behavior is validated
- performance is validated where required
- compatibility is validated
- evidence is retained
- defects are tracked
- release risk is understood

---

# 84. Security Checklist

Before completion, confirm:

- no secret is committed
- authentication is correct
- authorization is correct
- tenant isolation is correct
- object-level access is correct
- input is bounded
- data is minimized
- logging is safe
- dependencies are secure
- transport is protected
- error responses are safe
- audit is implemented where required
- security tests pass

---

# 85. Mandatory Blocking Conditions

A work item must not be marked done when any of the following exists:

- failing build
- failing automated test
- failing quality gate
- unresolved blocker defect
- unresolved critical security finding
- modified applied Flyway migration
- missing required authorization
- known data leakage
- missing required migration
- incompatible API without approved versioning
- incompatible event without migration strategy
- unbounded critical query
- infinite retry
- hardcoded secret
- missing rollback consideration for high-risk change
- missing required documentation
- unreviewed production change

---

# 86. Anti-Patterns

The following do not satisfy the Definition of Done:

- “works on my machine”
- manual-only validation
- adding tests after production failure without fixing root cause
- increasing coverage with meaningless assertions
- suppressing static-analysis findings broadly
- modifying old Flyway migrations
- documenting behavior that differs from runtime behavior
- hiding incomplete work behind a feature flag without a plan
- logging full payloads for diagnostics
- postponing security without risk acceptance
- merging with known failing tests
- accepting TODO comments as completion
- assuming rollback without validating compatibility
- closing the task before production validation where deployment is in scope

---

# 87. Architecture Rules

A completed implementation must:

- preserve bounded-context ownership
- preserve dependency direction
- protect domain invariants
- isolate infrastructure
- use stable contracts
- protect trust boundaries
- validate external input
- preserve data integrity
- use immutable migrations
- assume duplicate message delivery
- remain observable
- include deterministic tests
- pass automated quality gates
- support safe deployment
- support operational recovery

---

# 88. Decision Summary

The Enterprise Order Platform considers work complete only when the applicable criteria for the following areas are satisfied:

- business behavior
- Domain-Driven Design
- Clean Architecture
- Java 21
- Spring Boot
- API contracts
- PostgreSQL
- JPA and Hibernate
- Flyway
- messaging
- Transactional Outbox
- idempotency
- security
- logging
- metrics
- tracing
- audit
- automated testing
- Testcontainers
- coverage
- SonarQube
- SAST
- dependency security
- Docker
- Kubernetes
- documentation
- deployment
- rollback
- production validation
- operational ownership

The Definition of Done is a mandatory quality agreement for every contribution to the platform.
