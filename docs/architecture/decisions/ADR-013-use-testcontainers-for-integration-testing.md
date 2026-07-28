# ADR-013: Use Testcontainers for Integration Testing

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-013 |
| Title | Use Testcontainers for Integration Testing |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Quality Engineering |
| Related Work Items | Automated Testing Strategy |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform depends on infrastructure components that provide behavior impossible to reproduce accurately using mocks or in-memory implementations.

Examples include:

- PostgreSQL
- SQS
- Redis
- Object Storage
- Authentication providers
- External APIs
- Message serialization
- Database migrations

Historically many enterprise applications relied on:

- H2
- Embedded databases
- Fake repositories
- Mock infrastructure
- Shared integration environments

These approaches frequently produce tests that pass in CI while failing in production because infrastructure behavior differs.

The platform requires production-like automated integration testing.

---

# 2. Problem Statement

The platform requires an integration testing strategy that:

- executes against real infrastructure
- remains reproducible
- is fully automated
- runs in CI/CD
- supports local development
- isolates tests
- minimizes environment drift
- supports PostgreSQL
- supports SQS
- supports Redis
- avoids shared environments
- remains deterministic
- scales with microservices

---

# 3. Decision Drivers

Primary drivers include:

1. production fidelity
2. deterministic execution
3. developer productivity
4. CI reproducibility
5. infrastructure isolation
6. confidence before deployment
7. maintainability
8. portability
9. automation
10. long-term quality

---

# 4. Considered Options

## Option A — Shared Integration Environment

Advantages:

- no local infrastructure
- centralized environment

Disadvantages:

- unstable
- shared state
- flaky tests
- difficult scheduling
- environment drift

---

## Option B — Embedded Infrastructure

Examples:

- H2
- Embedded SQS
- Fake Redis

Advantages:

- fast
- simple setup

Disadvantages:

- behavior differs from production
- unsupported PostgreSQL features
- inaccurate SQL behavior
- hidden production defects

---

## Option C — Docker Compose

Advantages:

- real infrastructure
- reusable locally

Disadvantages:

- manual lifecycle
- shared containers
- weak test isolation
- coordination overhead

---

## Option D — Testcontainers

Advantages:

- real infrastructure
- isolated tests
- automatic lifecycle
- CI friendly
- production fidelity
- excellent Spring Boot integration
- parallel execution support

Disadvantages:

- Docker dependency
- slower than pure unit tests
- image download on first execution

---

# 5. Decision

The Enterprise Order Platform adopts **Testcontainers** as the standard integration-testing framework.

Every infrastructure integration test must execute against real containerized dependencies whenever practical.

---

# 6. Rationale

Integration tests should validate actual infrastructure behavior rather than approximations.

Testcontainers provides:

- realistic execution
- isolated environments
- reproducibility
- automatic cleanup
- strong community support

---

# 7. Scope

Testcontainers should be used for:

- PostgreSQL
- SQS
- Redis
- LocalStack (when applicable)
- MinIO (when applicable)
- WireMock integration
- future infrastructure services

---

# 8. Architecture

```text
JUnit

↓

Testcontainers

↓

Docker Containers

↓

Real Infrastructure

↓

Application Tests
```

---

# 9. Test Pyramid

Recommended testing distribution:

```text
70%

Unit Tests

20%

Integration Tests

10%

End-to-End Tests
```

Integration tests complement—not replace—unit tests.

---

# 10. PostgreSQL

Integration tests must use PostgreSQL containers.

Reasons:

- JSONB support
- indexes
- constraints
- transactions
- locking
- SQL compatibility

H2 is not considered a production-equivalent replacement.

---

# 11. SQS

SQS integration tests validate:

- publication
- consumption
- retries
- headers
- ordering
- serialization
- idempotency
- dead-letter processing

---

# 12. Redis

Redis integration tests validate:

- cache hit
- cache miss
- TTL
- serialization
- invalidation
- fallback behavior

---

# 13. Flyway

Flyway migrations execute automatically inside Testcontainers.

Tests validate:

- migration ordering
- schema compatibility
- startup integrity

Applied migrations must never be modified.

---

# 14. Isolation

Each test class receives isolated infrastructure.

Tests must not depend on execution order.

---

# 15. Lifecycle

Containers may be:

- per class
- shared reusable containers
- reusable local containers (developer option)

CI must prioritize isolation.

---

# 16. Data Initialization

Test data should be created through:

- SQL scripts
- builders
- fixtures
- application APIs

Avoid manual database manipulation during tests.

---

# 17. Determinism

Tests must produce identical results regardless of:

- execution order
- machine
- operating system
- CI runner

---

# 18. Parallel Execution

Parallel execution is encouraged.

Tests must avoid:

- shared mutable state
- static test data
- fixed ports

---

# 19. Dynamic Ports

Containers must expose dynamic ports.

Hardcoded ports are prohibited.

---

# 20. Docker Dependency

Docker is required.

Developers must have a compatible runtime.

CI runners must provide container support.

---

# 21. Spring Boot Integration

Spring Boot integrates using:

- `@ServiceConnection`
- `DynamicPropertySource`
- container lifecycle annotations

Configuration should remain automatic whenever possible.

---

# 22. Performance

Containers increase execution time.

Mitigations include:

- reusable images
- reusable local containers
- selective integration tests
- parallel execution

---

# 23. Resource Cleanup

Containers must terminate automatically.

Manual cleanup should not be required.

---

# 24. Logging

Failed tests should expose:

- container logs
- application logs
- SQL errors
- SQS logs

---

# 25. Security

Test infrastructure must never contain:

- production credentials
- production databases
- production SQS queues
- production Redis instances

---

# 26. CI/CD

CI pipelines execute Testcontainers automatically.

Build success depends on:

- passing unit tests
- passing integration tests
- successful migrations

---

# 27. Anti-Patterns

The following are prohibited:

- replacing PostgreSQL with H2
- depending on shared environments
- fixed ports
- production infrastructure
- test ordering
- mutable global state

---

# 28. Positive Consequences

The decision provides:

- production-like tests
- reproducible execution
- improved confidence
- infrastructure validation
- reduced environment drift

---

# 29. Negative Consequences

The decision introduces:

- slower execution
- Docker dependency
- image downloads

These costs are acceptable.

---

# 30. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Slow tests | Parallel execution |
| Docker unavailable | CI validation |
| Large images | Image reuse |
| Flaky startup | Wait strategies |
| Resource exhaustion | Proper cleanup |

---

# 31. Implementation Guidance

Mandatory rules:

1. Infrastructure integration tests use Testcontainers.
2. PostgreSQL replaces H2.
3. SQS integration uses representative queue infrastructure.
4. Redis integration uses real Redis.
5. Containers use dynamic ports.
6. Tests remain isolated.
7. Flyway migrations execute normally.
8. Tests remain deterministic.
9. Production resources are prohibited.
10. Container lifecycle remains automatic.

---

# 32. Validation

Validation includes:

- PostgreSQL tests
- SQS tests
- Redis tests
- migration validation
- startup validation
- CI execution
- parallel execution verification

---

# 33. Success Criteria

The decision is successful when:

- integration tests execute reliably
- production defects decrease
- CI remains reproducible
- infrastructure behavior matches production
- developers trust integration results

---

# 34. Related Decisions

- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-010: Use Redis for Distributed Caching
- ADR-012: Adopt the Saga Pattern for Distributed Workflows

---

# 35. References

- Testcontainers Documentation
- Spring Boot Testcontainers Documentation
- PostgreSQL Documentation
- Amazon SQS Documentation
- Redis Documentation
- Enterprise Order Platform Testing Standards

---

# 36. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved |

---

# 37. Decision Summary

The Enterprise Order Platform adopts **Testcontainers** as the standard framework for infrastructure integration testing.

Integration tests execute against real containerized services instead of embedded substitutes, providing:

- production-like behavior
- isolated execution
- deterministic results
- automatic infrastructure lifecycle
- reproducible CI pipelines

This decision significantly increases confidence in database, messaging, caching and migration behavior while reducing environment-specific defects.
