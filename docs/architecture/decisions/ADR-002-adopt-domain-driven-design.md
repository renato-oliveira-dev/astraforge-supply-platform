# ADR-002: Adopt Domain-Driven Design

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-002 |
| Title | Adopt Domain-Driven Design |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Domain Architecture |
| Related Work Items | Initial platform architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform manages business processes related to the complete order lifecycle.

These processes include:

- order creation
- item validation
- pricing
- approval
- rejection
- cancellation
- inventory coordination
- payment coordination
- customer eligibility
- audit
- integration with external systems

The platform contains business rules that are more complex than simple data persistence and retrieval.

Examples include:

- permitted order state transitions
- approval authority
- order-type restrictions
- customer eligibility
- item quantity and value limits
- cancellation rules
- duplicate request handling
- aggregate consistency
- event generation
- business conflict detection

If these rules are implemented only through technical structures such as controllers, service classes, repositories and database entities, the business model becomes fragmented and difficult to understand.

The platform requires an architectural approach that keeps business concepts explicit and aligns software structure with the language and boundaries of the business domain.

---

# 2. Problem Statement

The platform needs a consistent method for:

- representing complex business concepts
- organizing business capabilities
- defining ownership boundaries
- protecting business invariants
- maintaining consistent terminology
- separating domain behavior from infrastructure
- modeling aggregate consistency
- defining domain events
- managing integration between business areas
- supporting long-term evolution

The decision must define whether Domain-Driven Design will be adopted as the principal method for modeling and organizing business behavior.

---

# 3. Decision Drivers

The primary decision drivers are:

1. business complexity
2. explicit business behavior
3. consistency of terminology
4. maintainability
5. clear ownership boundaries
6. aggregate integrity
7. domain knowledge preservation
8. alignment between code and business language
9. reduced accidental coupling
10. support for event-driven architecture
11. independent bounded-context evolution
12. improved communication between business and engineering
13. compatibility with Clean Architecture
14. long-term platform evolution

---

# 4. Constraints

The decision must consider:

- Clean Architecture is the approved dependency model
- Java 21 is the primary language
- Spring Boot is used in outer architectural layers
- PostgreSQL is the primary transactional database
- integration may occur through REST and asynchronous messaging
- the platform must support eventual consistency
- several business capabilities interact with order processing
- teams may own different bounded contexts
- public contracts must remain backward compatible
- infrastructure models must remain separate from domain models
- the system must support automated testing

---

# 5. Considered Options

## 5.1 Option A: Transaction Script

Business operations could be implemented as procedural application services.

Example:

```text
Validate request

Load records

Apply conditions

Update database

Publish event
```

### Advantages

- simple for basic workflows
- low initial modeling effort
- familiar to developers
- rapid implementation for small features
- direct control flow

### Disadvantages

- business rules become distributed
- service classes become large
- behavior is separated from state
- domain terminology becomes inconsistent
- invariants are difficult to protect
- duplicated validation becomes common
- changes produce widespread impact
- domain knowledge remains implicit
- testing often focuses on implementation flow
- state transitions become fragile

---

## 5.2 Option B: Anemic Domain Model

The platform could use domain-like entities containing primarily data, while services implement most behavior.

Example:

```java
public class Order {

    private OrderStatus status;

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
```

### Advantages

- familiar object-oriented structure
- easy integration with persistence models
- simple mapping
- minimal design effort
- services retain workflow control

### Disadvantages

- entities do not protect invariants
- public setters allow invalid states
- business behavior becomes service-centric
- models provide little semantic value
- duplication increases
- domain rules are difficult to discover
- aggregate boundaries remain unclear
- application services become overly complex
- behavior and data remain separated

---

## 5.3 Option C: Domain-Driven Design

The platform could explicitly model:

- bounded contexts
- ubiquitous language
- aggregates
- entities
- value objects
- domain services
- policies
- specifications
- domain events
- context relationships

### Advantages

- business behavior becomes explicit
- terminology remains consistent
- invariants are protected
- aggregate boundaries define transactional consistency
- domain knowledge becomes part of the code
- context ownership is clearer
- integration boundaries become explicit
- complex behavior becomes easier to test
- supports event-driven architecture
- aligns with Clean Architecture
- reduces accidental coupling
- improves long-term maintainability

### Disadvantages

- higher initial modeling effort
- requires domain collaboration
- incorrect aggregate design may create performance problems
- excessive modeling may add unnecessary complexity
- teams require DDD knowledge
- context boundaries require governance
- mapping between models may increase
- not every feature requires advanced tactical patterns

---

## 5.4 Option D: Database-Centric Model

The database schema could be treated as the primary model.

Application structures would closely follow tables and relationships.

### Advantages

- direct persistence mapping
- reduced model duplication
- simple CRUD implementation
- familiar reporting model
- low mapping overhead

### Disadvantages

- database structure drives business behavior
- table boundaries replace domain boundaries
- invariants are distributed
- business terminology becomes persistence-oriented
- schema changes affect application behavior directly
- domain behavior is difficult to isolate
- cross-table transactions become common
- services become tightly coupled to persistence
- business evolution becomes expensive

---

## 5.5 Option E: Keep Domain Modeling Informal

Teams could use business terminology and models without adopting formal DDD rules.

### Advantages

- lower governance effort
- local flexibility
- gradual modeling
- fewer explicit concepts

### Disadvantages

- inconsistent aggregate definitions
- ambiguous ownership
- terminology drift
- duplicated concepts
- context boundaries remain unclear
- integration contracts become inconsistent
- teams may implement conflicting business rules
- domain models degrade over time

---

# 6. Decision

The Enterprise Order Platform will adopt Domain-Driven Design as the primary approach for modeling business behavior and organizing business capabilities.

The platform will apply:

## Strategic DDD

- bounded contexts
- ubiquitous language
- context mapping
- explicit ownership
- published integration contracts
- anti-corruption layers where required

## Tactical DDD

- aggregates
- aggregate roots
- entities
- value objects
- domain services
- domain policies
- specifications where justified
- domain events
- repositories
- domain exceptions

DDD patterns will be applied according to domain complexity.

The platform will not introduce tactical DDD abstractions where a simpler model adequately represents the behavior.

---

# 7. Rationale

The platform contains business behavior that must remain explicit, consistent and protected over time.

Order processing is not limited to CRUD operations.

It includes rules such as:

- whether an order may transition to another status
- who may approve an order
- which items may belong to an order
- which business limits apply
- whether cancellation is permitted
- how duplicate commands are handled
- when domain events are generated
- which aggregate owns a state change

Domain-Driven Design provides a shared method for representing these rules in code.

DDD was selected because it complements Clean Architecture:

```text
Clean Architecture defines dependency direction.

DDD defines business modeling.
```

Together, they allow the business domain to remain independent from technical implementation details.

---

# 8. Strategic Design

## 8.1 Bounded Contexts

The platform will organize business capabilities into bounded contexts.

Initial contexts may include:

```text
Orders

Customers

Inventory

Payments

Notifications

Identity and Access
```

Each bounded context owns:

- its language
- its models
- its business rules
- its data
- its APIs
- its events
- its lifecycle
- its operational behavior

---

## 8.2 Context Ownership

Each business concept must have one authoritative owner.

Examples:

```text
Order is owned by the Orders context.

Customer eligibility is owned by the Customers context.

Inventory availability is owned by the Inventory context.

Payment authorization is owned by the Payments context.
```

A context may store references or snapshots from another context but must not become a second source of truth.

---

## 8.3 Ubiquitous Language

Each bounded context must maintain a consistent vocabulary shared by:

- business stakeholders
- engineers
- product owners
- analysts
- testers
- architects

Terms used in code, documentation and conversations should remain aligned.

Examples:

```text
Order

Order Item

Approval

Rejection

Cancellation

Reservation

Authorization
```

Avoid introducing multiple terms for the same domain concept without explicit distinction.

---

## 8.4 Context Map

Relationships between bounded contexts must be documented.

Possible relationships include:

- Customer-Supplier
- Conformist
- Partnership
- Published Language
- Open Host Service
- Anti-Corruption Layer
- Separate Ways

The context map must identify:

- upstream context
- downstream context
- contract owner
- integration style
- dependency direction
- compatibility responsibility

---

## 8.5 Published Language

Integration between contexts must use explicit published contracts.

Examples:

- versioned REST APIs
- versioned integration events
- documented message schemas
- stable error contracts

Internal domain models must not be exposed directly as public contracts.

---

## 8.6 Anti-Corruption Layer

An Anti-Corruption Layer must be used when integrating with a model that should not influence the internal domain.

Examples:

- legacy order systems
- third-party payment providers
- external inventory systems
- vendor-specific identity claims
- external ERP structures

The Anti-Corruption Layer translates external concepts into internal domain terminology.

---

# 9. Tactical Design

## 9.1 Aggregate

An aggregate defines a transactional consistency boundary.

An aggregate must:

- have one aggregate root
- protect invariants
- control internal modifications
- expose business behavior
- avoid direct modification of internal entities
- reference other aggregates by identifier
- remain transactionally consistent
- emit domain events when relevant

---

## 9.2 Aggregate Root

The aggregate root is the only external entry point for modifications to the aggregate.

Example:

```java
public final class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderItem> items;

    public void approve(ActorId approvedBy, ApprovalComment comment) {
        ensureApprovalIsAllowed();
        status = OrderStatus.APPROVED;
        registerEvent(new OrderApproved(id, approvedBy));
    }
}
```

External code must not modify internal state directly.

---

## 9.3 Aggregate Size

Aggregates should remain as small as possible while still protecting invariants.

Avoid aggregates that load:

- unrelated historical records
- entire customer structures
- payment history
- inventory data
- notification records
- large external collections

Cross-context consistency should use identifiers, events and workflows rather than oversized aggregates.

---

## 9.4 Aggregate References

Aggregates should reference other aggregates using identifiers.

Preferred:

```java
private CustomerId customerId;
```

Avoid:

```java
private Customer customer;
```

when `Customer` belongs to another aggregate or bounded context.

---

## 9.5 Entity

An entity has:

- stable identity
- lifecycle
- mutable state controlled by behavior
- business meaning

Entity equality should be based on identity when identity is established.

Entities must not expose unrestricted public setters.

---

## 9.6 Value Object

A value object represents a concept defined by its values.

Examples:

```text
OrderId

CustomerId

OrderNumber

Money

Quantity

EmailAddress

ApprovalComment
```

Value objects should be:

- immutable
- validated at construction
- comparable by value
- self-contained
- framework-independent

Example:

```java
public record Quantity(int value) {

    public Quantity {
        if (value <= 0) {
            throw new InvalidQuantityException(value);
        }
    }
}
```

---

## 9.7 Domain Service

A domain service may be used when business behavior:

- does not naturally belong to one entity
- coordinates domain concepts
- remains independent from infrastructure
- represents meaningful business logic

Example:

```java
public final class OrderPricingService {

    public Money calculateTotal(List<OrderItem> items) {
        // Domain calculation
    }
}
```

Domain services must not become generic containers for all business logic.

---

## 9.8 Domain Policy

A domain policy represents a business decision rule.

Examples:

```text
OrderApprovalPolicy

CancellationPolicy

CustomerEligibilityPolicy

DiscountPolicy
```

Policies are useful when rules:

- vary by configuration
- vary by order type
- have multiple implementations
- are reused
- require clear business naming

---

## 9.9 Specification

A specification may represent a reusable business predicate.

Example:

```java
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);
}
```

Specifications should be introduced only when they improve expressiveness and reuse.

Simple validation should remain simple.

---

## 9.10 Repository

A repository provides aggregate-oriented persistence access.

Example:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    Order save(Order order);
}
```

Repositories should not expose unrestricted table-oriented operations to the Domain or Application layer.

---

## 9.11 Domain Event

A domain event represents a business fact that has already occurred.

Examples:

```text
OrderCreated

OrderApproved

OrderRejected

OrderCancelled
```

Domain events must:

- use past-tense names
- remain immutable
- contain domain-relevant information
- be registered by domain behavior
- remain independent from broker technology

---

## 9.12 Domain Exception

A domain exception represents violation of a business rule.

Examples:

```text
OrderCannotBeApprovedException

OrderAlreadyCancelledException

InvalidOrderTransitionException

OrderItemLimitExceededException
```

Domain exceptions must use business terminology.

They must not include HTTP status codes or database error details.

---

# 10. Order Aggregate Guidance

The `Order` aggregate is expected to own behavior related to:

- order lifecycle
- order status
- order items
- order totals
- approval transitions
- rejection transitions
- cancellation transitions
- internal consistency
- domain event registration

The Order aggregate should not own:

- customer master data
- inventory stock
- payment-provider state
- notification delivery
- external user profile structures
- infrastructure retry state

---

# 11. State Transitions

State transitions must be implemented as domain behavior.

Preferred:

```java
order.approve(actor, comment);
```

Avoid:

```java
order.setStatus(APPROVED);
```

The aggregate must validate:

- current status
- target status
- required actor
- required comment
- business restrictions
- duplicate behavior

Invalid transitions must fail explicitly.

---

# 12. Business Invariants

An invariant is a rule that must always remain true within an aggregate.

Examples:

- an order must contain at least one item
- quantity must be greater than zero
- a cancelled order cannot be approved
- an approved order cannot be modified without an explicit workflow
- an order total must equal the sum of its item totals
- duplicate item identifiers are not allowed
- approval actor must be authorized by the application workflow

Invariants must be protected in:

- constructors
- factory methods
- behavioral methods
- aggregate operations

---

# 13. Creation Rules

Aggregate creation should occur through:

- validated constructors
- static factory methods
- named factories
- domain factories when creation is complex

Example:

```java
public static Order create(
        OrderId orderId,
        CustomerId customerId,
        List<OrderItem> items
) {
    validateItems(items);

    var order = new Order(
            orderId,
            customerId,
            OrderStatus.CREATED,
            List.copyOf(items)
    );

    order.registerEvent(new OrderCreated(orderId, customerId));
    return order;
}
```

Factories should prevent invalid initial state.

---

# 14. Encapsulation

Domain collections must not expose mutable internal state.

Preferred:

```java
public List<OrderItem> items() {
    return List.copyOf(items);
}
```

Avoid:

```java
public List<OrderItem> getItems() {
    return items;
}
```

All modifications must occur through aggregate behavior.

---

# 15. Domain Factories

A domain factory may be used when aggregate creation requires:

- multiple domain objects
- complex validation
- domain policies
- calculated values
- coordinated construction

Factories must not hide infrastructure access.

Infrastructure data should be obtained by the Application layer before factory invocation.

---

# 16. Application and Domain Responsibilities

The Application layer coordinates the use case.

The Domain layer decides business behavior.

Example:

```text
Application:

Authorize actor

Load order

Obtain required external information

Invoke order.approve(...)

Save order

Persist outbox event
```

```text
Domain:

Validate current status

Apply approval rules

Change state

Register OrderApproved
```

---

# 17. External Information

The Domain layer must not call external services.

When business behavior requires external information:

1. the Application layer obtains the information through a port
2. the Application layer translates it into a domain concept
3. the Domain layer evaluates the rule

Example:

```java
var eligibility = customerEligibilityPort.check(command.customerId());

order.confirmCustomerEligibility(eligibility);
```

---

# 18. Authorization Boundary

Technical authorization belongs primarily to Interface and Application layers.

Examples:

- token validation
- role extraction
- scope validation
- tenant extraction

Business authorization may belong to the Domain when it is part of the business rule.

Example:

```text
Only a supervisor may approve orders above a specified amount.
```

The Application layer should provide the domain with the relevant business authorization context.

---

# 19. Domain Event Lifecycle

The recommended domain-event lifecycle is:

```text
Application loads aggregate

Aggregate executes behavior

Aggregate registers domain event

Application persists aggregate

Application persists integration event through outbox

Transaction commits
```

Domain events may be mapped to integration events before publication.

---

# 20. Domain and Integration Events

Domain events and integration events are separate concepts.

Example:

```text
Domain event:

OrderApproved
```

```text
Integration event:

OrderApprovedIntegrationEventV1
```

The integration event may include:

- event ID
- version
- producer
- correlation ID
- occurred timestamp
- public payload

Broker metadata must not contaminate the domain event.

---

# 21. Eventual Consistency

Consistency within an aggregate is transactional.

Consistency between bounded contexts is usually eventual.

Example:

```text
Order approved

↓

OrderApprovedIntegrationEvent published

↓

Inventory or Notification context reacts asynchronously
```

Cross-context transactions must not be created through shared database access.

---

# 22. Cross-Context Workflows

Business processes spanning multiple contexts may use:

- choreography
- orchestration
- sagas
- process managers
- asynchronous events
- compensating actions

The selected mechanism must define:

- ownership
- state
- timeout behavior
- retries
- idempotency
- compensation
- observability

---

# 23. Shared Kernel

A Shared Kernel should be avoided by default.

It may be considered only for:

- highly stable concepts
- closely coordinated teams
- jointly owned code
- explicit versioning

Sharing mutable domain models across bounded contexts creates strong coupling.

---

# 24. Generic Subdomains

Generic technical capabilities should not be modeled as core domain behavior.

Examples:

- authentication token parsing
- email transport
- tracing
- logging
- generic file storage
- broker configuration

These belong to supporting or infrastructure concerns.

---

# 25. Core Domain

The core domain is the business capability that provides the platform's primary value.

For the Enterprise Order Platform, the core domain includes:

- order lifecycle
- order rules
- order approval
- order consistency
- order state transitions
- business validation
- coordination of order-related processes

Core-domain code should receive the highest level of design attention and test coverage.

---

# 26. Supporting Subdomains

Supporting subdomains may include:

- notification coordination
- audit coordination
- reporting projections
- workflow configuration
- document generation

Supporting subdomains may use simpler models when business complexity does not justify rich domain modeling.

---

# 27. Model Depth

DDD does not require every class to use advanced modeling.

Use rich domain modeling where:

- rules are complex
- state transitions matter
- invariants must be protected
- terminology is important
- behavior changes frequently

Use simpler application models where:

- behavior is primarily CRUD
- rules are minimal
- no complex invariant exists
- data is read-only or projection-oriented

---

# 28. CQRS Relationship

DDD may be combined with CQRS.

The command side may use:

- aggregates
- repositories
- domain events
- application use cases

The query side may use:

- projections
- read models
- SQL queries
- optimized DTOs
- denormalized views

Query models do not need to reconstruct aggregates when no business behavior is executed.

---

# 29. Persistence Independence

The domain model must not be shaped primarily by ORM limitations.

Persistence mapping may use:

- dedicated JPA entities
- embedded persistence components
- converters
- explicit mappers

The cost of mapping is accepted to preserve domain integrity.

---

# 30. Testing Implications

## 30.1 Aggregate Tests

Aggregate tests must validate:

- creation
- valid behavior
- invalid behavior
- state transitions
- invariants
- domain events
- duplicate operations
- boundary values

Example:

```java
@Test
void testApproveShouldRejectCancelledOrder() {
    var order = OrderTestFixture.cancelledOrder();

    assertThatThrownBy(() ->
            order.approve(TestConstants.APPROVER_ID, TestConstants.APPROVAL_COMMENT)
    )
            .as("Approval of a cancelled order")
            .isInstanceOf(OrderCannotBeApprovedException.class);
}
```

---

## 30.2 Value Object Tests

Value-object tests must validate:

- valid construction
- invalid construction
- equality
- boundary values
- immutability
- normalization when part of the domain rule

---

## 30.3 Domain Service Tests

Domain-service tests should use real domain objects where practical.

Mocks should not be required unless the service depends on a domain abstraction that genuinely varies.

---

## 30.4 Application Tests

Application tests must verify orchestration separately from domain behavior.

Examples:

- correct aggregate is loaded
- required port is invoked
- domain method is called through observable state
- aggregate is persisted
- outbox event is stored
- authorization is enforced

---

# 31. Positive Consequences

The decision provides:

- explicit business models
- consistent terminology
- protected invariants
- clearer state transitions
- improved testability
- stronger context ownership
- reduced accidental coupling
- easier business collaboration
- better alignment between code and business behavior
- clearer integration contracts
- improved architecture documentation
- better support for event-driven workflows
- long-term preservation of domain knowledge

---

# 32. Negative Consequences

The decision introduces:

- additional modeling effort
- need for domain discovery
- need for bounded-context governance
- more domain types
- additional mapping
- training requirements
- risk of overengineering
- risk of incorrectly designed aggregates
- increased effort for seemingly simple features
- need for continuous terminology alignment

These costs are accepted because the platform contains long-lived and complex business behavior.

---

# 33. Neutral Consequences

The decision changes engineering practice:

- business discussions influence code structure
- domain terminology becomes part of review
- aggregate boundaries become architectural constraints
- cross-context operations require explicit integration
- data duplication may be accepted between contexts
- eventual consistency becomes a normal design concern
- not every table maps to a domain entity
- not every domain concept maps directly to a table

---

# 34. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Overengineering simple behavior | Medium | Medium | Apply tactical patterns only where complexity justifies them |
| Incorrect aggregate boundaries | High | Medium | Review invariants, transaction scope and load behavior |
| Anemic domain model | High | Medium | Require behavioral methods and protected state |
| Domain services become procedural services | High | Medium | Place behavior on entities and value objects when natural |
| Terminology divergence | Medium | Medium | Maintain ubiquitous-language documentation |
| Shared models couple contexts | High | Medium | Use published contracts and anti-corruption layers |
| Oversized aggregates | High | Medium | Reference external aggregates by identifier |
| Excessive cross-context calls | High | Medium | Use local snapshots and asynchronous integration where appropriate |
| DDD becomes annotation-driven | Medium | Low | Keep domain framework-independent |
| Mapping overhead increases | Medium | High | Use focused mapping and minimal boundary models |
| Domain events expose internals | Medium | Medium | Map domain events to public integration events |
| Teams apply inconsistent patterns | High | Medium | Use standards, examples, ADRs and architecture reviews |

---

# 35. Implementation Guidance

The following rules are mandatory:

1. business terminology must remain consistent
2. each concept must have an owning bounded context
3. aggregate roots must protect invariants
4. aggregate state must change through behavior
5. public setters are prohibited for controlled domain state
6. value objects should represent validated business concepts
7. cross-aggregate references should use identifiers
8. domain events must represent business facts
9. domain events must remain broker-independent
10. public integration events must use dedicated contracts
11. repositories must operate primarily on aggregate roots
12. domain exceptions must use business terminology
13. external models must be translated through boundaries
14. tactical patterns must be justified by complexity
15. cross-context consistency must not rely on shared databases
16. significant context-boundary changes require architecture review
17. domain code must remain independent from Spring and persistence frameworks
18. aggregate tests must validate invariants and transitions

---

# 36. Modeling Workflow

The recommended modeling workflow is:

1. identify the business capability
2. establish ubiquitous language
3. identify business actors
4. identify commands
5. identify business rules
6. identify invariants
7. identify entities and value objects
8. identify aggregate boundaries
9. identify domain events
10. identify context ownership
11. identify external dependencies
12. define application use cases
13. define public contracts
14. validate the model with domain experts
15. implement incrementally

---

# 37. Event Storming

Event Storming may be used to discover the domain.

Typical sequence:

```text
Domain Event

↓

Command

↓

Actor

↓

Aggregate

↓

Policy

↓

External System

↓

Read Model
```

Event Storming is a discovery technique.

Its output must still be refined into an explicit domain model.

---

# 38. Domain Review Questions

During design and code review, ask:

- Does this name match business terminology?
- Which bounded context owns this concept?
- Which aggregate protects this invariant?
- Can invalid state be created?
- Is behavior placed on the correct domain object?
- Is this rule duplicated?
- Does this external model leak into the domain?
- Is this event a business fact?
- Is this operation transactionally consistent?
- Does this aggregate require too much data?
- Is eventual consistency acceptable?
- Is a tactical DDD pattern justified?

---

# 39. Migration Guidance

Existing procedural or persistence-centric code should be migrated incrementally.

Recommended sequence:

1. identify business rules in services
2. group rules by business capability
3. introduce value objects
4. introduce behavior on domain entities
5. identify aggregate roots
6. define repository ports
7. isolate persistence models
8. introduce domain events
9. establish bounded-context ownership
10. add domain tests
11. remove duplicated rules
12. introduce anti-corruption layers where needed

A full platform rewrite is not required.

---

# 40. Validation

The decision will be validated through:

- domain model reviews
- ubiquitous-language consistency
- bounded-context documentation
- aggregate tests
- value-object tests
- architecture tests
- repository boundary reviews
- event contract reviews
- context-map maintenance
- prohibition of framework annotations in Domain
- review of public setters
- review of cross-context database access
- review of domain event design

---

# 41. Success Criteria

The decision is successful when:

- core business rules are represented in domain code
- aggregate methods express business intent
- invalid state transitions are rejected
- value objects protect validated concepts
- bounded contexts have clear ownership
- public contracts remain separate from internal domain models
- cross-context integrations use explicit contracts
- domain tests run without infrastructure
- business terminology remains consistent
- domain events represent meaningful business facts
- duplicate business rules are reduced
- application services focus on orchestration
- domain models remain independent from Spring and JPA
- new engineers can identify where business behavior belongs

---

# 42. Alternatives Rejected

## 42.1 Transaction Script

Rejected as the primary domain approach because it distributes business behavior across procedural application services and provides insufficient protection for complex invariants.

Transaction Script may still be used for simple supporting capabilities with minimal business complexity.

---

## 42.2 Anemic Domain Model

Rejected because entities containing only data do not adequately protect state transitions or preserve business knowledge.

---

## 42.3 Database-Centric Model

Rejected because database structure should not define the business model or ownership boundaries.

---

## 42.4 Informal Domain Modeling

Rejected because platform-wide consistency requires explicit context boundaries, terminology and modeling rules.

---

# 43. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Kafka for Integration Events
- ADR-016: Use Problem Details for API Errors
- ADR-017: Use Optimistic Locking for Aggregate Concurrency

---

# 44. References

- Eric Evans, *Domain-Driven Design*
- Vaughn Vernon, *Implementing Domain-Driven Design*
- Vaughn Vernon, *Domain-Driven Design Distilled*
- Alberto Brandolini, Event Storming
- Martin Fowler, Domain Model
- Enterprise Order Platform domain overview
- Enterprise Order Platform ubiquitous language
- Enterprise Order Platform bounded contexts
- Enterprise Order Platform context map
- ADR-001: Adopt Clean Architecture

---

# 45. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial domain architecture baseline |

---

# 46. Decision Summary

The Enterprise Order Platform adopts Domain-Driven Design as the primary method for modeling complex business behavior.

The platform will use:

```text
Strategic Design

Bounded Contexts

Ubiquitous Language

Context Mapping

Published Contracts

Anti-Corruption Layers
```

and, where justified:

```text
Aggregates

Entities

Value Objects

Domain Services

Policies

Specifications

Domain Events

Repositories
```

Business behavior must remain explicit, testable and independent from infrastructure.

Tactical DDD patterns will be applied according to actual domain complexity rather than as mandatory ceremony.

This decision establishes the business-modeling foundation for the Enterprise Order Platform.
