# ADR-001: Adopt Clean Architecture

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-001 |
| Title | Adopt Clean Architecture |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Software Architecture |
| Related Work Items | Initial platform architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform is designed as a long-lived backend platform responsible for order creation, validation, approval, cancellation, persistence and integration with external systems.

The platform must support:

- complex business rules
- long-term maintainability
- multiple infrastructure technologies
- independent testing
- controlled architectural evolution
- event-driven integration
- database persistence
- secure APIs
- operational observability
- gradual modernization

The application uses Java 21 and Spring Boot, with PostgreSQL, SQS, Redis and Kubernetes as supporting technologies.

Without explicit architectural boundaries, business logic may become coupled to:

- Spring annotations
- JPA and Hibernate
- REST controllers
- SQS producers and consumers
- Redis clients
- database schemas
- third-party APIs
- deployment infrastructure

This coupling would make the platform harder to test, maintain, migrate and evolve.

A clear dependency model is required to ensure that business behavior remains stable even when frameworks and infrastructure change.

---

# 2. Problem Statement

The platform needs an architectural structure that:

- isolates business rules from frameworks
- prevents infrastructure concerns from leaking into the domain
- supports independent testing
- allows infrastructure implementations to change
- preserves explicit dependency direction
- supports Domain-Driven Design
- avoids controller-driven or database-driven architecture
- remains understandable as the codebase grows

The decision must define how source code is organized and how dependencies flow between architectural layers.

---

# 3. Decision Drivers

The primary decision drivers are:

1. business logic independence
2. maintainability
3. testability
4. explicit dependency direction
5. framework isolation
6. infrastructure replaceability
7. architectural consistency
8. support for Domain-Driven Design
9. long-term evolution
10. reduced coupling
11. clear ownership of responsibilities
12. automated architecture validation

---

# 4. Constraints

The decision must consider the following constraints:

- Java 21 is the primary programming language
- Spring Boot is the application framework
- PostgreSQL is the primary transactional database
- SQS may be used for integration events
- Redis may be used for distributed caching
- REST is the primary synchronous interface
- the platform must support automated testing
- production deployments run in containers
- workloads may execute on Kubernetes
- the application must remain compatible with gradual architectural evolution
- infrastructure-specific code is still required and must have an explicit location

---

# 5. Considered Options

## 5.1 Option A: Traditional Layered Architecture

A conventional layered structure could be adopted:

```text
Controller

↓

Service

↓

Repository

↓

Database
```

### Advantages

- familiar to many Spring developers
- simple for small applications
- fast initial implementation
- low conceptual overhead
- directly supported by common tutorials

### Disadvantages

- frequently becomes persistence-driven
- services often accumulate unrelated business logic
- domain objects commonly become JPA entities
- infrastructure concerns leak into business logic
- dependency direction is not always enforced
- controllers may access repositories directly
- testing often requires Spring context startup
- framework migration becomes difficult
- large service classes become common
- architectural boundaries degrade over time

---

## 5.2 Option B: Clean Architecture

The application is organized around concentric architectural boundaries.

The dependency direction points inward:

```text
Infrastructure

↓

Application

↓

Domain
```

Outer layers depend on inner layers.

Inner layers do not depend on outer layers.

### Advantages

- business logic remains framework-independent
- infrastructure is replaceable
- domain behavior is easier to test
- dependency direction is explicit
- supports Domain-Driven Design
- reduces technology leakage
- improves architectural consistency
- enables ports and adapters
- makes application use cases explicit
- supports automated architecture tests
- improves long-term maintainability

### Disadvantages

- additional abstractions are required
- mapping between layers may increase
- initial implementation is more verbose
- engineers require architectural discipline
- small use cases may appear more complex
- poor abstraction design may create unnecessary interfaces
- package structure requires governance

---

## 5.3 Option C: Framework-Centric Spring Architecture

The application could be structured around Spring stereotypes and technical modules.

Example:

```text
controller

service

repository

entity

config
```

### Advantages

- rapid development
- familiar structure
- low initial learning curve
- minimal mapping
- direct framework integration

### Disadvantages

- framework becomes the architecture
- domain logic becomes coupled to Spring
- JPA entities frequently leak into APIs
- business behavior becomes distributed across technical classes
- testing becomes infrastructure-heavy
- technology replacement becomes expensive
- bounded contexts are less visible
- package structure communicates technical roles rather than business capabilities

---

## 5.4 Option D: Keep Architecture Informal

The platform could rely on conventions without adopting a formal architecture.

### Advantages

- no initial governance effort
- maximum local implementation freedom
- fewer explicit abstractions
- rapid prototyping

### Disadvantages

- inconsistent implementations
- architectural drift
- hidden coupling
- duplicated patterns
- difficult onboarding
- increased review effort
- unpredictable maintenance cost
- no automated enforcement
- business rules become distributed
- infrastructure decisions become difficult to reverse

---

# 6. Decision

The AstraForge Supply Platform will adopt Clean Architecture as its primary application architecture.

The architecture will define the following principal layers:

```text
Interface

Infrastructure

Application

Domain
```

The mandatory dependency direction is:

```text
Interface ───────┐
                 │
Infrastructure ──┼──> Application ───> Domain
                 │
External Systems ┘
```

The Domain layer is the innermost layer.

The Domain layer must not depend on:

- Spring
- Spring Boot
- Spring Data
- JPA
- Hibernate
- REST
- SQS
- Redis
- PostgreSQL
- Docker
- Kubernetes
- external service clients

The Application layer may depend on the Domain layer.

Infrastructure and Interface layers may depend on Application and Domain contracts.

Dependency inversion will be used whenever inner layers require behavior provided by external technology.

---

# 7. Rationale

Clean Architecture was selected because the business domain is expected to remain more stable than the frameworks and infrastructure used to implement it.

The order lifecycle includes behavior such as:

- creation
- validation
- approval
- cancellation
- status transitions
- business conflicts
- financial calculations
- domain events
- authorization coordination

These behaviors must remain understandable and testable without requiring:

- a database
- a message broker
- an HTTP server
- a Spring application context
- external services

The selected architecture supports this requirement by placing business behavior in the inner layers and allowing technology-specific code to depend on business contracts.

The additional mapping and abstraction cost is accepted because the platform is intended to be long-lived, integration-heavy and operationally significant.

---

# 8. Architectural Model

## 8.1 Domain Layer

The Domain layer contains:

- aggregates
- entities
- value objects
- domain services
- domain policies
- domain specifications
- domain exceptions
- domain events
- business invariants

The Domain layer defines business behavior and rules.

It must remain independent from application frameworks.

---

## 8.2 Application Layer

The Application layer contains:

- use cases
- application services
- commands
- queries
- application ports
- repository interfaces
- authorization coordination
- transaction orchestration
- application exceptions
- result models where appropriate

The Application layer coordinates domain behavior.

It must not contain concrete infrastructure implementations.

---

## 8.3 Infrastructure Layer

The Infrastructure layer contains:

- JPA entities
- Spring Data repositories
- repository adapters
- SQS producers
- SQS consumers
- Redis adapters
- external HTTP clients
- serialization configuration
- persistence mapping
- broker configuration
- framework configuration
- security integration
- observability integration

Infrastructure implements contracts defined by inner layers.

---

## 8.4 Interface Layer

The Interface layer contains:

- REST controllers
- request DTOs
- response DTOs
- request validation
- API mapping
- exception translation
- OpenAPI configuration
- HTTP-specific behavior

The Interface layer translates external requests into application commands and queries.

---

# 9. Dependency Rules

The following dependency rules are mandatory.

## 9.1 Domain Dependencies

The Domain layer may depend only on:

- Java standard library
- carefully approved language-level utilities
- domain-owned abstractions

It must not depend on application or infrastructure packages.

---

## 9.2 Application Dependencies

The Application layer may depend on:

- Domain
- application-owned contracts
- Java standard library
- carefully approved framework-neutral libraries

It must not depend on concrete infrastructure adapters.

---

## 9.3 Infrastructure Dependencies

The Infrastructure layer may depend on:

- Application
- Domain
- Spring Boot
- JPA
- SQS
- Redis
- PostgreSQL drivers
- external client libraries
- observability libraries

Infrastructure must implement inward-facing contracts.

---

## 9.4 Interface Dependencies

The Interface layer may depend on:

- Application use cases
- application request and result contracts
- API mapping utilities
- Spring Web
- validation libraries
- security context adapters

It must not directly use persistence adapters.

---

# 10. Ports and Adapters

Dependency inversion will be implemented through ports and adapters.

Example:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    Order save(Order order);
}
```

Infrastructure implementation:

```java
@Component
public class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    public JpaOrderRepositoryAdapter(
            SpringDataOrderRepository repository,
            OrderPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return repository.findById(orderId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Order save(Order order) {
        var entity = mapper.toEntity(order);
        var savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
```

The port expresses an application need.

The adapter contains the technology-specific implementation.

---

# 11. Repository Boundaries

Repository interfaces must be defined according to aggregate needs.

Preferred:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    Order save(Order order);
}
```

Avoid exposing framework-specific abstractions:

```java
public interface OrderRepository
        extends JpaRepository<OrderJpaEntity, UUID> {
}
```

from the Domain or Application layer.

Spring Data repositories remain internal to the Infrastructure layer.

---

# 12. Use Case Boundaries

Application use cases should expose explicit business capabilities.

Example:

```java
public interface ApproveOrderUseCase {

    ApproveOrderResult execute(ApproveOrderCommand command);
}
```

The controller depends on the use-case interface rather than a concrete service.

This makes the application entry point explicit and testable.

---

# 13. Controller Rules

Controllers must:

- receive HTTP requests
- validate interface-level input
- map requests to commands or queries
- invoke application use cases
- map results to responses
- return appropriate HTTP status codes

Controllers must not:

- contain domain rules
- execute repository queries directly
- manage transactions
- publish broker messages directly
- access JPA entities
- coordinate complex workflows
- implement authorization solely through UI assumptions

---

# 14. Domain Model Rules

Domain classes must:

- express business terminology
- protect invariants
- expose behavior
- avoid public setters
- control state transitions
- avoid persistence annotations
- avoid serialization annotations
- remain testable without Spring

Preferred:

```java
order.approve(approvedBy, comments);
```

Avoid:

```java
order.setStatus(OrderStatus.APPROVED);
```

---

# 15. Persistence Model Isolation

Persistence models are infrastructure concerns.

Use explicit names such as:

```text
OrderJpaEntity

OrderItemJpaEntity

OutboxEventJpaEntity
```

They must not be used as:

- domain aggregates
- controller responses
- integration event payloads
- cache contracts
- application commands

Mapping between persistence and domain models is accepted as an architectural cost.

---

# 16. API Model Isolation

Request and response models belong to the Interface layer.

Examples:

```text
CreateOrderRequest

OrderResponse

OrderSummaryResponse
```

API models must not be reused as:

- domain objects
- persistence entities
- event payloads
- repository projections unless explicitly designed for that boundary

---

# 17. Integration Model Isolation

External service DTOs and public event payloads must remain isolated from the domain model.

Examples:

```text
InventoryReservationRequest

CustomerEligibilityResponse

OrderCreatedIntegrationEvent
```

Changes in external contracts must not force direct changes to domain entities.

---

# 18. Transaction Boundaries

Transaction boundaries belong to the Application layer.

A transactional use case may include:

- loading an aggregate
- executing domain behavior
- persisting aggregate state
- persisting outbox events

It must avoid:

- remote HTTP calls inside long transactions
- broker publication without an approved transactional mechanism
- long-running computation
- unrelated aggregate modification
- presentation-specific behavior

---

# 19. Domain Events and Integration Events

Domain events belong to the Domain layer.

Integration events belong to the Infrastructure or Application integration boundary.

Example:

```text
OrderApproved
```

may be a domain event.

It may be mapped to:

```text
OrderApprovedIntegrationEventV1
```

before publication.

The domain event must not contain SQS-specific fields.

---

# 20. Exception Boundaries

Exceptions must be translated across boundaries.

Example:

```text
SQLException

↓

OrderPersistenceException

↓

Application failure

↓

Problem Details response
```

Infrastructure-specific exceptions must not leak into controllers or external API contracts.

---

# 21. Security Boundary

The Interface and Infrastructure layers may extract technical security information such as:

- JWT claims
- scopes
- roles
- tenant identifiers
- authenticated subject

The Application layer should receive an application-oriented security context.

Example:

```java
public record CurrentActor(
        ActorId actorId,
        TenantId tenantId,
        Set<Permission> permissions
) {
}
```

The Domain layer should receive only the authorization information required for business behavior.

---

# 22. Package Structure

The project should use business-capability-oriented packages.

Recommended structure:

```text
com.enterprise.orders
├── domain
│   ├── model
│   ├── event
│   ├── service
│   ├── policy
│   └── exception
├── application
│   ├── command
│   ├── query
│   ├── port
│   ├── service
│   └── exception
├── infrastructure
│   ├── persistence
│   ├── messaging
│   ├── client
│   ├── cache
│   ├── security
│   └── configuration
└── interface
    └── rest
        ├── controller
        ├── request
        ├── response
        └── advice
```

The exact package structure may evolve, but dependency direction must remain unchanged.

---

# 23. Mapping Strategy

Explicit mapping is preferred between:

- API request and command
- domain and persistence entity
- external response and application model
- domain event and integration event
- application result and API response

Mapping may be:

- manual
- generated
- encapsulated in dedicated mapper classes

Mappings must not contain hidden business rules.

---

# 24. Framework Usage

Spring Boot remains an important implementation framework.

This decision does not reject Spring.

It establishes that Spring supports the architecture rather than defining it.

Spring annotations should be concentrated in outer layers.

Pure domain classes must remain free from framework annotations.

---

# 25. Testing Implications

The selected architecture enables different test scopes.

## 25.1 Domain Tests

Domain behavior can be tested without Spring.

Example:

```java
@Test
void testApproveShouldChangeStatusWhenOrderIsPending() {
    var order = OrderTestFixture.pendingOrder();

    order.approve(TestConstants.APPROVER_ID, "Approved");

    assertThat(order.status())
            .as("Order status after approval")
            .isEqualTo(OrderStatus.APPROVED);
}
```

---

## 25.2 Application Tests

Application use cases may be tested with mocked ports.

Mocks should represent external boundaries such as:

- repositories
- external service ports
- event stores
- clocks
- authorization providers

---

## 25.3 Infrastructure Tests

Infrastructure adapters should be tested using production-compatible dependencies where required.

Examples:

- PostgreSQL through Testcontainers
- SQS through Testcontainers
- Redis through Testcontainers
- HTTP stubs for external services

---

## 25.4 Architecture Tests

Architecture tests must validate dependency rules.

Example concepts:

```text
Domain must not depend on Spring.

Domain must not depend on Infrastructure.

Controllers must not depend on repositories.

Application must not depend on JPA adapters.
```

ArchUnit or an equivalent tool may enforce these rules.

---

# 26. Positive Consequences

The decision provides:

- strong separation of concerns
- framework-independent domain behavior
- easier unit testing
- clearer application use cases
- replaceable infrastructure
- explicit repository contracts
- better support for DDD
- reduced coupling
- improved code review
- automated architectural governance
- safer modernization
- clearer onboarding
- improved maintainability
- better resilience to framework changes

---

# 27. Negative Consequences

The decision introduces:

- additional interfaces
- additional mapping
- more source files
- greater initial design effort
- higher learning requirements
- risk of over-abstraction
- possible duplication between models
- need for architecture tests
- need for continuous review discipline
- more explicit dependency wiring

These costs are accepted because the platform is intended to be long-lived and business-critical.

---

# 28. Neutral Consequences

The decision also changes how engineers work:

- business use cases become explicit
- technical models are separated from domain models
- mapping becomes a normal architectural activity
- package structure becomes part of governance
- architectural violations become review blockers
- infrastructure implementation details become less visible to the domain

---

# 29. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Excessive abstraction | Medium | Medium | Create ports only for real boundaries |
| Too many mappings | Medium | High | Use focused mapper components and generated mapping where appropriate |
| Architecture ignored over time | High | Medium | Enforce ArchUnit tests and code review rules |
| Domain becomes anemic | High | Medium | Require behavior-rich aggregates and domain methods |
| Application services become too large | High | Medium | Model explicit use cases and decompose orchestration |
| Engineers bypass ports | High | Medium | Prohibit direct controller-to-repository access |
| Spring annotations leak into Domain | Medium | Medium | Add automated dependency tests |
| Infrastructure exceptions leak inward | Medium | Medium | Require boundary exception translation |
| Unnecessary interfaces for local classes | Low | Medium | Use interfaces only at meaningful architectural boundaries |
| Duplicate models increase maintenance | Medium | Medium | Keep boundary models minimal and mapping explicit |

---

# 30. Implementation Guidance

The following rules are mandatory:

1. new business rules belong in the Domain layer
2. application workflows belong in the Application layer
3. external technologies belong in Infrastructure
4. HTTP behavior belongs in Interface
5. dependencies must point inward
6. repository contracts belong to inner layers
7. Spring Data repositories remain in Infrastructure
8. controllers must not access persistence directly
9. JPA entities must not leave Infrastructure
10. domain entities must not include JPA annotations
11. integration events must not reuse domain entities directly
12. exceptions must be translated at boundaries
13. transaction boundaries belong to application use cases
14. architecture tests must protect the dependency model
15. deviations require an approved ADR or explicit architecture review

---

# 31. Migration Guidance

Existing code that does not comply should be migrated incrementally.

Recommended sequence:

1. identify business behavior
2. create domain models
3. create application use cases
4. define required ports
5. wrap existing infrastructure in adapters
6. redirect controllers to use cases
7. isolate persistence entities
8. introduce explicit mappings
9. add architecture tests
10. remove obsolete direct dependencies

A full rewrite is not required.

Incremental migration is preferred.

---

# 32. Validation

The decision will be validated through:

- code review
- package inspection
- ArchUnit tests
- dependency analysis
- unit-test isolation
- framework-free domain tests
- prohibition of direct controller-to-repository access
- inspection of JPA entity usage
- review of transaction boundaries
- review of external client usage
- architectural documentation consistency

---

# 33. Success Criteria

The decision is successful when:

- Domain compiles without Spring dependencies
- Domain tests run without Spring context startup
- controllers invoke application use cases
- application services depend on ports
- persistence is implemented through adapters
- JPA entities remain inside Infrastructure
- public APIs use dedicated DTOs
- domain rules are not duplicated in controllers
- architecture tests prevent dependency violations
- infrastructure technologies can evolve without redesigning core domain behavior
- new contributors can identify layer ownership consistently

---

# 34. Alternatives Rejected

## 34.1 Traditional Layered Architecture

Rejected as the primary architecture because it does not provide sufficiently strong dependency rules for a long-lived, integration-heavy platform.

It may work for small services, but without stricter boundaries it commonly becomes persistence-centric.

---

## 34.2 Framework-Centric Architecture

Rejected because it makes Spring and JPA the primary organizing concepts.

The business domain must remain the central architectural concern.

---

## 34.3 Informal Architecture

Rejected because conventions without enforcement are insufficient for a platform expected to evolve across multiple contributors and services.

---

# 35. Related Decisions

This ADR is related to future decisions including:

- ADR-002: Adopt Domain-Driven Design
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt Transactional Outbox
- ADR-013: Use Testcontainers for Integration Testing
- ADR-016: Use Problem Details for API Errors
- ADR-017: Use Optimistic Locking for Aggregate Concurrency

---

# 36. References

- Robert C. Martin, *Clean Architecture*
- Alistair Cockburn, Hexagonal Architecture
- Eric Evans, *Domain-Driven Design*
- Vaughn Vernon, *Implementing Domain-Driven Design*
- AstraForge Supply Platform architecture documentation
- AstraForge Supply Platform package structure standard
- AstraForge Supply Platform code review guidelines

---

# 37. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved | Initial architectural baseline |

---

# 38. Decision Summary

The AstraForge Supply Platform adopts Clean Architecture.

The system will be organized around:

```text
Domain

Application

Infrastructure

Interface
```

Dependencies must point inward.

The Domain layer remains independent from frameworks and infrastructure.

Application use cases coordinate domain behavior through ports.

Infrastructure implements persistence, messaging, caching and external integration adapters.

The Interface layer translates external protocols into application requests.

This decision establishes the foundational dependency model for all services and modules in the platform.
