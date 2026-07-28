# ADR-065: Adopt Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-065 |
| Title | Adopt Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard |
| Status | Accepted |
| Date | 2026-07-25 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | DDD, Clean Architecture, Modularization, Service Boundaries |
| Related Work Items | Java 21, Spring Boot, ArchUnit, Microservices, Refactoring |
| Supersedes | None |
| Superseded By | None |

---

> **Scope relationship:** ADR-065 is the umbrella baseline for DDD, service boundaries, Clean Architecture and modularization. ADR-088 specializes domain modeling and business-rule governance; ADR-089 specializes Hexagonal Architecture, Ports & Adapters and enforceable module boundaries. These decisions are complementary, not competing.

---

# 1. Context

Enterprise systems naturally increase in structural complexity.

Without explicit architectural boundaries, a service can evolve from:

```text
CONTROLLER
    |
    v
SERVICE
    |
    v
REPOSITORY
```

into:

```text
GIANT CONTROLLER
       |
       v
GOD SERVICE
       |
       +--> REPOSITORY A
       +--> REPOSITORY B
       +--> REPOSITORY C
       +--> CLIENT A
       +--> CLIENT B
       +--> CLIENT C
       +--> CACHE
       +--> MESSAGING
       +--> SECURITY
       +--> MAPPING
       +--> VALIDATION
       +--> BUSINESS RULES
       +--> AUDIT
       +--> NOTIFICATION
```

The opposite extreme is also harmful:

```text
ONE METHOD
   |
   v
ONE CLASS
   |
   v
ONE INTERFACE
   |
   v
ONE IMPLEMENTATION
   |
   v
ONE FACTORY
```

Both extremes increase maintenance cost.

The objective is not:

```text
MAXIMUM NUMBER OF CLASSES
```

or:

```text
MINIMUM NUMBER OF CLASSES
```

The objective is:

```text
CLEAR RESPONSIBILITIES
        +
HIGH COHESION
        +
LOW COUPLING
        +
EXPLICIT DOMAIN BOUNDARIES
```

---

# 2. Problem Statement

The organization requires standards covering:

- Domain-Driven Design
- bounded contexts
- subdomains
- aggregates
- aggregate roots
- entities
- value objects
- domain services
- application services
- repositories
- controllers
- DTOs
- mappers
- ports
- adapters
- anti-corruption layers
- package boundaries
- module dependencies
- shared libraries
- shared kernels
- microservice boundaries
- God Services
- God Controllers
- excessive class fragmentation
- circular dependencies
- duplicate code
- dependency count
- class size
- method size
- package organization
- ArchUnit
- `package-info.java`
- service decomposition
- service consolidation
- microservice extraction
- microservice merging

---

# 3. Decision Drivers

Primary drivers are:

1. maintainability
2. domain clarity
3. high cohesion
4. low coupling
5. testability
6. independent evolution
7. controlled complexity
8. explicit ownership
9. performance
10. transactional correctness
11. developer productivity
12. architectural governance

---

# 4. Decision

Enterprise services MUST be structured around business capabilities and cohesive responsibilities rather than arbitrary technical decomposition.

Canonical dependency direction:

```text
                EXTERNAL WORLD
                      |
                      v
               INFRASTRUCTURE
                      |
                      v
                APPLICATION
                      |
                      v
                   DOMAIN
```

The domain MUST NOT depend on infrastructure concerns.

---

# 5. Fundamental Principle

```text
Architecture exists to
manage change.

Put things together
that change together.

Separate things
that change for
different reasons.
```

---

# 6. Domain-Driven Design

DDD SHOULD be applied pragmatically where business complexity justifies it.

---

# 7. DDD Is Not Annotation Architecture

DDD is not achieved merely by creating packages named:

```text
domain

application

infrastructure
```

The boundaries must represent actual responsibilities.

---

# 8. Ubiquitous Language

Business terminology SHOULD remain consistent between:

```text
Business Discussion

Requirements

Code

Tests

Events

APIs

Documentation
```

---

# 9. Technical Synonyms

Avoid introducing unnecessary technical synonyms for established business concepts.

---

# 10. Subdomain

Business capabilities SHOULD be analyzed as subdomains.

Common classifications:

```text
CORE

SUPPORTING

GENERIC
```

---

# 11. Core Domain

The Core Domain contains business capability that provides meaningful organizational differentiation.

---

# 12. Supporting Subdomain

A Supporting Subdomain is necessary for the business but is not itself the primary differentiator.

---

# 13. Generic Subdomain

Generic capabilities SHOULD prefer established platform/product solutions when appropriate rather than custom reinvention.

---

# 14. Bounded Context

A Bounded Context defines a boundary within which a domain model has consistent meaning.

---

# 15. Same Word, Different Meaning

The same word MAY have different models in different bounded contexts.

Example:

```text
CUSTOMER

Sales Context
    !=
Billing Context
    !=
Logistics Context
```

---

# 16. Shared Database Is Not Bounded Context

A set of tables does not automatically define a bounded context.

---

# 17. Package Is Not Bounded Context

A Java package alone does not establish a meaningful business boundary.

---

# 18. Bounded Context Ownership

Every bounded context SHOULD have identifiable business and technical ownership.

---

# 19. Context Map

Material relationships between bounded contexts SHOULD be documented.

---

# 20. Context Relationships

Relationships MAY include:

```text
Customer/Supplier

Conformist

Anti-Corruption Layer

Published Language

Shared Kernel
```

---

# 21. Shared Kernel

Shared Kernels MUST be small and deliberate.

---

# 22. Shared Domain Library

A large shared domain library used by every microservice is strongly discouraged.

---

# 23. Distributed Monolith

If services cannot evolve independently because they all depend on the same internal domain model, the architecture risks becoming a distributed monolith.

---

# 24. Anti-Corruption Layer

Integrations with external or legacy models SHOULD use an Anti-Corruption Layer when their concepts differ from the local domain.

---

# 25. External DTO

External API DTOs MUST NOT automatically become internal domain models.

---

# 26. Translation Boundary

Conceptually:

```text
EXTERNAL MODEL
      |
      v
ANTI-CORRUPTION
     LAYER
      |
      v
LOCAL MODEL
```

---

# 27. Domain Model

Domain objects SHOULD express meaningful business behavior where business complexity warrants it.

---

# 28. Anemic Domain Model

An anemic model is not automatically incorrect.

For simple CRUD-oriented capabilities it MAY be appropriate.

---

# 29. Rich Domain Model

A richer domain model SHOULD be preferred where invariants and state transitions are complex.

---

# 30. Entity

An Entity is defined primarily by identity and lifecycle.

---

# 31. Entity Identity

Entity equality semantics MUST reflect domain identity requirements.

---

# 32. Persistence Identity

Database implementation details SHOULD NOT unnecessarily define domain identity semantics.

---

# 33. Value Object

A Value Object is defined by its values rather than independent identity.

Examples MAY include:

```text
Money

Address

DateRange

Quantity

Percentage
```

---

# 34. Value Object Immutability

Value Objects SHOULD normally be immutable.

---

# 35. Java Record

Java records SHOULD be considered for immutable Value Objects where their semantics are appropriate.

---

# 36. Primitive Obsession

Important domain concepts SHOULD NOT always be represented as generic:

```text
String

BigDecimal

UUID
```

when a dedicated type materially improves correctness.

---

# 37. Example

Instead of repeatedly passing:

```java
BigDecimal amount
```

a meaningful domain concept MAY use:

```java
Money amount
```

when currency and monetary invariants matter.

---

# 38. Aggregate

An Aggregate is a transactional consistency boundary.

---

# 39. Aggregate Root

External modifications to an Aggregate SHOULD occur through its Aggregate Root.

---

# 40. Aggregate Invariant

The Aggregate Root MUST preserve invariants within the aggregate boundary.

---

# 41. Aggregate Size

Aggregates SHOULD remain as small as possible while preserving required transactional invariants.

---

# 42. Giant Aggregate

A giant object graph loaded and persisted for every small operation SHOULD be avoided.

---

# 43. Cross-Aggregate Transaction

Cross-aggregate consistency SHOULD NOT automatically imply one giant aggregate.

---

# 44. Eventual Consistency

Eventual consistency SHOULD be considered when business invariants do not require immediate atomic consistency.

---

# 45. Aggregate Repository

Repositories SHOULD normally operate around Aggregate Roots.

---

# 46. Repository Abstraction

Repositories represent domain/application persistence needs rather than arbitrary database utility APIs.

---

# 47. Generic Repository

A universal generic repository exposing unrestricted CRUD for every entity SHOULD be avoided when it bypasses domain invariants.

---

# 48. Repository Method

Repository methods SHOULD express meaningful retrieval intent.

Prefer:

```text
findPendingOrdersForCustomer(...)
```

when meaningful over exposing arbitrary persistence mechanics.

---

# 49. Query Complexity

Complex read/query models MAY use specialized query repositories/services without forcing them through aggregate mutation models.

---

# 50. CQRS Pragmatism

CQRS MAY be applied selectively where read and write concerns differ materially.

---

# 51. CQRS Is Not Mandatory

Every CRUD service MUST NOT be split into separate command/query infrastructures merely for architectural fashion.

---

# 52. Domain Service

A Domain Service SHOULD represent domain behavior that:

```text
Does not naturally belong to one Entity

Does not represent orchestration/infrastructure

Uses domain concepts
```

---

# 53. Application Service

Application Services coordinate use cases.

Typical responsibilities:

```text
Authorization

Loading Aggregates

Calling Domain Behavior

Persisting Results

Publishing Events

Coordinating Integrations
```

---

# 54. Application Service Is Not God Service

Application orchestration MUST still be decomposed when responsibilities become unrelated.

---

# 55. Infrastructure Service

Infrastructure components implement technical concerns such as:

```text
HTTP

Database

SQS

SQS

Redis

Email

File Storage
```

---

# 56. Domain Dependency

Domain classes MUST NOT depend directly on:

```text
WebClient

RestClient

SQSTemplate

SqsClient

JpaRepository

RedisTemplate

HttpServletRequest
```

---

# 57. Framework Leakage

Domain logic SHOULD minimize dependency on Spring annotations and infrastructure types.

---

# 58. Pragmatic Spring

Complete framework isolation is not required when it adds disproportionate complexity.

The objective is controlled dependency direction, not ideological purity.

---

# 59. Clean Architecture

The architecture SHOULD follow the dependency rule:

```text
OUTER LAYERS
     |
     v
INNER LAYERS
```

Inner business rules SHOULD NOT depend on outer technical mechanisms.

---

# 60. Ports

Ports define capabilities required or exposed by an architectural boundary.

---

# 61. Inbound Port

An inbound port represents an application use case.

Example:

```text
ApproveOrderUseCase
```

---

# 62. Outbound Port

An outbound port represents a capability required from infrastructure.

Example:

```text
CustomerGateway
```

---

# 63. Adapter

Adapters translate between ports and technical implementations.

---

# 64. Inbound Adapter

Examples:

```text
REST Controller

SQS Consumer

SQS Listener

Scheduled Job
```

---

# 65. Outbound Adapter

Examples:

```text
JPA Repository Adapter

REST Client

SQS Producer

SQS Producer

Redis Cache Adapter
```

---

# 66. Interface Everywhere

Ports MUST NOT be created mechanically for every class.

---

# 67. Interface Value

An interface SHOULD exist when it provides a meaningful abstraction boundary.

---

# 68. One Implementation Interface

An interface with one implementation MAY still be justified when it represents an architectural port.

---

# 69. Accidental Interface

This pattern SHOULD be avoided when there is no abstraction value:

```text
FooService
    |
    v
FooServiceImpl
```

solely because every service historically used an interface.

---

# 70. `Impl` Naming

`Impl` SHOULD NOT be the default naming strategy when a more meaningful implementation name exists.

Prefer:

```text
WebClientProductsGateway

JpaOrderRepository

RedisCustomerCache
```

over generic:

```text
ProductsServiceImpl

RepositoryImpl

CacheImpl
```

where meaningful.

---

# 71. Controller

Controllers are transport adapters.

---

# 72. Controller Responsibility

Controllers SHOULD primarily:

```text
Receive Request

Trigger Validation

Extract Security Context

Call Application Use Case

Map Response
```

---

# 73. Business Logic in Controller

Business rules MUST NOT accumulate inside controllers.

---

# 74. Repository in Controller

Controllers SHOULD NOT directly access persistence repositories.

---

# 75. External Client in Controller

Controllers SHOULD NOT directly orchestrate external clients.

---

# 76. God Controller

A controller with extensive:

```text
Validation

Persistence

Business Rules

External Calls

Mapping

Error Handling
```

MUST be refactored.

---

# 77. DTO

Transport DTOs define external contracts.

---

# 78. Request DTO

Request DTOs SHOULD contain only caller-controlled fields.

---

# 79. Response DTO

Response DTOs SHOULD expose only contract-required information.

---

# 80. Entity Exposure

JPA entities MUST NOT normally be exposed directly as REST contracts.

---

# 81. Persistence Coupling

API contracts MUST NOT unintentionally become coupled to persistence schema.

---

# 82. Mapper

Mappers SHOULD translate between structurally different representations.

---

# 83. Trivial Mapper

A dedicated mapper class SHOULD NOT be created merely to move one identical field between two trivial types unless consistency or generated mapping justifies it.

---

# 84. Complex Mapping

Complex mapping logic SHOULD be isolated and tested.

---

# 85. Mapper Business Rule

Mappers MUST NOT become hidden business-rule engines.

---

# 86. Validation Layers

Validation SHOULD occur at the correct semantic boundary.

---

# 87. Transport Validation

Examples:

```text
Required Field

String Length

Format

Enum Syntax
```

---

# 88. Business Validation

Examples:

```text
Order belongs to customer

Order can be approved in current state

Customer is eligible for operation
```

---

# 89. Database Validation

Database constraints SHOULD protect structural data integrity.

---

# 90. Duplicate Validation

The same validation SHOULD NOT be copy-pasted across multiple controllers/services.

---

# 91. Validator

Reusable complex validation MAY be encapsulated in dedicated validator/policy components.

---

# 92. Validator Explosion

Every `if` statement does not require a new validator class.

---

# 93. Package Organization

Packages SHOULD communicate architectural/business intent.

---

# 94. Feature-Oriented Packaging

For sufficiently complex services, feature/domain-oriented packaging SHOULD be preferred over a single global technical-layer structure.

Instead of:

```text
controller/
service/
repository/
dto/
mapper/
```

consider:

```text
orders/
    api/
    application/
    domain/
    infrastructure/

approval/
    api/
    application/
    domain/
    infrastructure/
```

where boundaries justify it.

---

# 95. Small Service

A small service MAY use simpler packaging.

Architecture MUST remain proportional to complexity.

---

# 96. Package Boundary

Packages SHOULD have explicit dependency direction.

---

# 97. Package Cycles

Circular package dependencies MUST be avoided.

---

# 98. `package-info.java`

Significant packages SHOULD contain meaningful `package-info.java` documentation when required by project conventions.

---

# 99. Package Documentation

`package-info.java` SHOULD explain package responsibility rather than merely restating the package name.

---

# 100. Public Surface

Package/module APIs SHOULD expose the smallest practical public surface.

---

# 101. Package-Private

Package-private visibility SHOULD be used when implementation details do not require broader exposure.

---

# 102. Modular Monolith

A modular monolith MAY be preferable to multiple microservices when independent deployment is not required.

---

# 103. Microservice

A microservice SHOULD represent a meaningful independently evolvable business capability.

---

# 104. Microservice Is Not Class

A microservice MUST NOT be created merely because a code module has grown.

---

# 105. Extraction Criteria

A new microservice SHOULD require several meaningful drivers such as:

```text
Independent Business Ownership

Independent Deployment Need

Different Scalability Profile

Different Availability Requirement

Different Security Boundary

Independent Data Ownership

Independent Change Cadence
```

---

# 106. Single Driver

One minor technical inconvenience SHOULD NOT normally justify a new microservice.

---

# 107. Network Cost

Extracting a microservice introduces:

```text
Network Latency

Serialization

Deployment

Observability

Security

Failure Modes

Data Consistency

Operational Ownership
```

---

# 108. Distributed Complexity

Moving a method into another process does not remove complexity.

It converts local complexity into distributed complexity.

---

# 109. Microservice Extraction Decision

Conceptually:

```text
MODULE TOO LARGE?
      |
      v
CAN INTERNAL
MODULARIZATION SOLVE IT?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
MODULARIZE  EVALUATE
IN PROCESS  SERVICE
            BOUNDARY
```

---

# 110. Service Consolidation

Existing microservices SHOULD be considered for consolidation when they have:

```text
Same Ownership

Same Release Cadence

Strong Synchronous Coupling

Shared Data Lifecycle

No Independent Scaling Need
```

---

# 111. Nano-Service

Extremely small services with no meaningful independent capability SHOULD be avoided.

---

# 112. Distributed Monolith Indicator

Warning signs include:

```text
Service A cannot deploy without B

Service B cannot deploy without C

All services require coordinated release

All services share one database

Every request crosses many synchronous services
```

---

# 113. Database Ownership

A microservice SHOULD own its persistence boundary.

---

# 114. Shared Database

Multiple services directly mutating the same tables SHOULD be avoided.

---

# 115. Cross-Service Repository

Service A MUST NOT import Service B's JPA repository to bypass Service B's API/domain boundary.

---

# 116. Cross-Service Entity

Sharing JPA entity classes between services is strongly discouraged.

---

# 117. API Contract

Cross-service interaction SHOULD use explicit:

```text
REST Contract

Event Contract

Approved Messaging Contract
```

---

# 118. Client DTO Ownership

A service SHOULD own its representation of an external service contract rather than importing the provider's internal model library.

---

# 119. Shared Contract Library

Shared contract libraries MAY be used selectively but MUST NOT become hidden compile-time coupling across the platform.

---

# 120. Event Contract

Published events SHOULD follow ADR-057.

---

# 121. Domain Event

A Domain Event represents something meaningful that occurred within the domain.

---

# 122. Integration Event

An Integration Event represents information intentionally published outside the bounded context.

---

# 123. Domain Event Is Not Automatically Integration Event

Internal domain events MUST NOT automatically become externally published contracts.

---

# 124. Translation

Domain events MAY be translated into stable integration events at the application/infrastructure boundary.

---

# 125. Transaction Boundary

Transaction boundaries SHOULD align with use cases and aggregate consistency requirements.

---

# 126. Giant Transaction

Transactions MUST NOT remain open unnecessarily across:

```text
Remote HTTP Calls

Slow External Services

Long Computations
```

---

# 127. Remote Call in Transaction

Remote calls inside database transactions SHOULD be avoided where practical.

---

# 128. Transactional Outbox

When database state and external event publication must remain coordinated, Transactional Outbox SHOULD follow ADR-057.

---

# 129. God Service

A God Service is a class accumulating unrelated responsibilities and dependencies.

---

# 130. Warning Signals

Warning signals include:

```text
Many unrelated methods

Many repositories

Many external clients

Large constructor

Many condition branches

High change frequency

Low cohesion

Difficult unit testing
```

---

# 131. Dependency Count

A class with more than approximately:

```text
15-20 injected dependencies
```

MUST trigger architectural review.

This is a review threshold, not an automatic violation.

---

# 132. Dependency Meaning

The important question is not merely:

```text
HOW MANY DEPENDENCIES?
```

but:

```text
WHY DOES ONE CLASS
NEED ALL OF THEM?
```

---

# 133. Dependency Grouping

Dependencies MUST NOT be hidden inside a wrapper solely to reduce constructor parameter count.

---

# 134. Facade

A facade is justified when it represents a cohesive capability.

It MUST NOT be used to disguise poor cohesion.

---

# 135. Class Size

Large classes SHOULD trigger review when size indicates multiple responsibilities.

---

# 136. Line Count

Line count alone MUST NOT be used as an architectural rule.

---

# 137. Method Size

Methods SHOULD remain small enough for their behavior to be understood without excessive cognitive load.

---

# 138. Extract Method

Methods SHOULD be extracted when doing so reveals meaningful intent.

---

# 139. Fragmentation

Do not extract methods/classes that merely force the reader to jump through files without improving abstraction.

---

# 140. Cognitive Complexity

High cognitive complexity SHOULD trigger simplification.

---

# 141. Conditional Complexity

Complex conditionals SHOULD be replaced with meaningful policies/strategies when multiple business variants exist.

---

# 142. Strategy

Strategy patterns MAY be appropriate for varying algorithms/business rules.

---

# 143. Strategy Explosion

Do not create a strategy hierarchy for a stable two-line conditional.

---

# 144. Switch on Business Type

Repeated switches on the same business discriminator across many classes MAY indicate missing polymorphism or policy abstraction.

---

# 145. Duplicate Code

Meaningful duplicated business logic SHOULD be consolidated.

---

# 146. Similar Is Not Duplicate

Code that looks syntactically similar but represents different domain concepts SHOULD NOT automatically be unified.

---

# 147. Wrong Abstraction

A bad shared abstraction can be more expensive than controlled duplication.

---

# 148. Rule of Three

Small duplication MAY be tolerated until a stable common abstraction becomes evident.

---

# 149. Shared Utility

Generic utility classes SHOULD remain small and technical.

---

# 150. `Utils`

Large classes named:

```text
Utils

Helper

Common

Manager
```

SHOULD trigger review because they often hide unclear responsibility.

---

# 151. Static Utility

Pure stateless technical functions MAY use static utility classes where appropriate.

---

# 152. Domain Utility

Business behavior SHOULD generally have meaningful domain naming rather than generic utility naming.

---

# 153. Manager Class

`Manager` SHOULD be avoided when a more precise capability name exists.

---

# 154. Support Class

`Support` MAY be appropriate for cohesive reusable technical/application behavior.

---

# 155. Support Explosion

`Support` MUST NOT become a generic destination for unrelated methods.

---

# 156. Service Naming

Service names SHOULD reveal responsibility.

Prefer:

```text
OrderApprovalService

OrderPricingService

OrderCancellationPolicy
```

over:

```text
OrderHelper

OrderManager

CommonOrderService
```

---

# 157. Command Service

Mutation use cases MAY be grouped in cohesive command services.

---

# 158. Query Service

Read-heavy concerns MAY use dedicated query services.

---

# 159. Command/Query Split

Separating command/query services is appropriate when it improves cohesion, not as a mandatory naming pattern.

---

# 160. Orchestrator

An orchestrator MAY coordinate multiple capabilities for one use case.

---

# 161. Orchestrator Business Logic

Orchestrators SHOULD coordinate rather than become the exclusive location of all business rules.

---

# 162. Workflow

Long-running workflows SHOULD use explicit workflow/process models rather than one giant synchronous method.

---

# 163. State Machine

State machines MAY be appropriate when lifecycle transitions are complex and explicit.

---

# 164. State Transition

State transitions MUST enforce valid source and target states.

---

# 165. Enum

Enums SHOULD represent closed stable sets.

---

# 166. Enum Business Logic

Small cohesive behavior MAY live in enums when it improves encapsulation.

---

# 167. Giant Enum

Large unrelated business workflows SHOULD NOT be forced into one enum.

---

# 168. Mapper Duplication

Repeated mapping logic SHOULD be centralized when semantics are identical.

---

# 169. Mapper Coupling

A universal mapper depending on dozens of services/repositories is prohibited.

---

# 170. Enrichment

Response enrichment requiring external data SHOULD be separated from pure structural mapping.

---

# 171. Batch Enrichment

Collections SHOULD prefer batch loading over N+1 remote calls.

---

# 172. N+1 Service Calls

This pattern MUST be avoided:

```text
FOR EACH ORDER
    CALL PRODUCTS
    CALL CUSTOMERS
    CALL USERS
```

when batch APIs or preloading can solve the problem.

---

# 173. Parallelism

Independent remote calls MAY execute concurrently according to ADR-034 and ADR-055.

---

# 174. Parallelism Is Not Architecture

Concurrency MUST NOT be used to hide an unnecessarily chatty service boundary.

---

# 175. Client Boundary

Each external dependency SHOULD have a cohesive client/gateway abstraction.

---

# 176. WebClient Leakage

`WebClient` SHOULD NOT be propagated throughout business services.

---

# 177. Remote Error Translation

Infrastructure-specific remote failures SHOULD be translated into meaningful application/domain failure semantics at the integration boundary.

---

# 178. Cache Boundary

Caching SHOULD remain behind an explicit capability boundary.

---

# 179. Cache in Domain

Domain rules SHOULD NOT depend directly on Redis APIs.

---

# 180. Cache Is Optimization

Cache implementation MUST NOT redefine domain ownership.

---

# 181. Circular Dependency

Spring circular dependencies MUST NOT be solved merely by:

```java
@Lazy
```

without understanding the architectural cycle.

---

# 182. Circular Dependency Signal

A circular dependency usually indicates incorrect responsibility placement.

---

# 183. Dependency Direction

Allowed conceptual flow:

```text
API
 |
 v
APPLICATION
 |
 v
DOMAIN
 ^
 |
INFRASTRUCTURE ADAPTERS
```

---

# 184. Forbidden Direction

Avoid:

```text
DOMAIN
  |
  v
CONTROLLER
```

or:

```text
DOMAIN
  |
  v
WEBCLIENT
```

---

# 185. Module Boundary

Modules SHOULD expose explicit APIs and hide implementation details.

---

# 186. Java Module System

JPMS MAY be used where its operational/tooling value justifies adoption, but is not mandatory.

---

# 187. Gradle Module

Gradle multi-module architecture MAY enforce significant compile-time boundaries.

---

# 188. Module Explosion

A separate Gradle module MUST NOT be created for every package.

---

# 189. Module Criteria

A module SHOULD represent a meaningful:

```text
Business Capability

Architectural Boundary

Reusable Technical Capability
```

---

# 190. Dependency Graph

Module dependency graphs MUST remain acyclic.

---

# 191. ArchUnit

ArchUnit SHOULD enforce stable Java architectural boundaries.

---

# 192. Architecture Test

Examples:

```text
Controllers may depend on application layer

Domain must not depend on controllers

Domain must not depend on WebClient

Repositories must not be used by controllers

Package cycles are forbidden
```

---

# 193. ArchUnit Example

Conceptually:

```java
noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
                "..controller..",
                "..webclient..",
                "..repository.jpa..");
```

---

# 194. Architecture Test Description

Architecture tests SHOULD provide meaningful assertion descriptions according to project test conventions.

---

# 195. Architectural Fitness Function

Architecture rules that remain stable SHOULD be executable rather than relying solely on documentation.

---

# 196. Sonar

SonarQube SHOULD complement architecture governance through:

```text
Complexity

Duplication

Maintainability

Dependency Smells
```

but MUST NOT replace architectural review.

---

# 197. SAST

Refactoring MUST preserve security boundaries and pass applicable SAST requirements.

---

# 198. Refactoring

Refactoring SHOULD preserve externally observable behavior unless behavior change is intentional.

---

# 199. Characterization Test

Before major legacy refactoring, characterization tests SHOULD capture important current behavior.

---

# 200. Incremental Refactoring

Large architectural refactoring SHOULD prefer incremental migration.

---

# 201. Big Bang Rewrite

Large rewrites SHOULD require strong justification.

---

# 202. Strangler Pattern

The Strangler pattern MAY incrementally replace legacy capabilities.

---

# 203. Compatibility Layer

Temporary compatibility adapters MAY support incremental migration.

---

# 204. Temporary Architecture

Temporary components MUST have explicit removal criteria.

---

# 205. Dead Code

Dead classes, methods and obsolete abstractions SHOULD be removed.

---

# 206. Commented Code

Large blocks of commented-out code SHOULD NOT remain as version control already preserves history.

---

# 207. Deprecated Code

Deprecated code SHOULD have a migration/removal plan.

---

# 208. Refactoring Decision: Unify Classes

Classes SHOULD be considered for unification when they:

```text
Have the same responsibility

Change for the same reason

Contain substantially duplicated logic

Have artificial boundaries

Cannot be meaningfully named separately
```

---

# 209. Refactoring Decision: Keep Separate

Classes SHOULD remain separate when they:

```text
Represent different domain concepts

Have different change drivers

Have different security requirements

Have different transactional semantics

Have independent testing concerns
```

---

# 210. Refactoring Decision: Split Class

A class SHOULD be considered for decomposition when it:

```text
Has multiple unrelated responsibilities

Has excessive unrelated dependencies

Changes for unrelated reasons

Mixes orchestration and domain rules

Mixes mapping and remote access

Mixes persistence and business policy

Is difficult to test without extensive mocking
```

---

# 211. Mock Count Signal

A unit test requiring a very large number of unrelated mocks MAY indicate excessive class responsibility.

---

# 212. Constructor Signal

A large constructor is an architectural signal, not merely a formatting inconvenience.

---

# 213. Testability Signal

If meaningful behavior cannot be tested without booting the entire application, boundaries SHOULD be reviewed.

---

# 214. Private Method Testing

Tests SHOULD validate observable behavior rather than private implementation methods.

---

# 215. Reflection Testing

Reflection SHOULD NOT be used merely to test private methods.

---

# 216. Integration Boundary Testing

Adapters SHOULD have focused integration/contract tests.

---

# 217. Domain Test

Domain rules SHOULD normally be testable without Spring context.

---

# 218. Application Service Test

Application orchestration SHOULD be testable with controlled port dependencies.

---

# 219. Repository Test

Persistence-specific behavior SHOULD use representative database integration tests.

---

# 220. Controller Test

Controller tests SHOULD focus on:

```text
HTTP Contract

Validation

Authorization

Status

Serialization
```

rather than retesting all domain behavior.

---

# 221. Architecture Review Checklist

Material structural changes SHOULD evaluate:

```text
[ ] What business capability owns this code?

[ ] Is the responsibility cohesive?

[ ] Is this domain, application or infrastructure logic?

[ ] Does the dependency direction point inward?

[ ] Is infrastructure leaking into the domain?

[ ] Is the class becoming a God Service?

[ ] Are dependencies genuinely related?

[ ] Is a facade hiding poor cohesion?

[ ] Is there duplicated business logic?

[ ] Would unification create a wrong abstraction?

[ ] Is a new interface actually a port?

[ ] Is a new class actually necessary?

[ ] Is a new microservice actually necessary?

[ ] Can internal modularization solve the problem?

[ ] Are transaction boundaries clear?

[ ] Are remote calls inside transactions?

[ ] Is N+1 remote access occurring?

[ ] Are package cycles present?

[ ] Can ArchUnit enforce this rule?

[ ] Are tests aligned with boundaries?
```

---

# 222. Architecture Fitness Functions

Stable rules SHOULD be automated.

Examples:

```text
[ ] Domain does not depend on controller packages

[ ] Domain does not depend on WebClient

[ ] Domain does not depend on SQS APIs

[ ] Controllers do not access repositories directly

[ ] Controllers do not access external clients directly

[ ] Package dependency graph is acyclic

[ ] JPA entities are not REST response contracts

[ ] WebClient remains in infrastructure/integration packages

[ ] No cross-service JPA repository dependency

[ ] package-info.java exists where project standard requires it

[ ] Architecture tests pass
```

---

# 223. Enterprise Architecture Gate

A service is not considered structurally compliant when applicable conditions include:

```text
[ ] Controllers contain material business rules

[ ] Controllers directly orchestrate repositories and clients

[ ] Domain depends on infrastructure frameworks

[ ] One service class owns unrelated capabilities

[ ] Large constructor dependency count has no cohesive justification

[ ] Circular dependencies exist

[ ] Shared domain library tightly couples multiple microservices

[ ] Multiple services directly mutate the same persistence tables

[ ] JPA entities are exposed as public API contracts

[ ] External DTOs are used directly as local domain models

[ ] Remote calls occur repeatedly in N+1 loops

[ ] Microservice extraction has no meaningful independent boundary

[ ] Architecture rules exist only in documentation despite being automatable
```

---

# 224. Anti-Patterns

The following are prohibited or strongly discouraged:

- God Services
- God Controllers
- universal `CommonService`
- giant `Utils`
- giant `Manager`
- interfaces created mechanically for every implementation
- meaningless `Impl` naming
- one class per trivial method
- excessive package fragmentation
- architecture driven purely by line count
- hiding constructor dependencies inside wrappers
- domain depending directly on WebClient
- domain depending directly on Redis/SQS
- repositories accessed directly by controllers
- JPA entities exposed as API contracts
- shared JPA entities between microservices
- shared database tables mutated by multiple services
- circular Spring dependencies solved only with `@Lazy`
- giant transactional methods containing remote calls
- new microservice created merely because a class is large
- synchronous microservice chains replacing local method calls without business justification
- premature CQRS
- premature event sourcing
- mapper classes containing business workflows
- validators for every trivial condition
- strategies for trivial stable branches
- abstractions created solely to remove two similar lines
- uncontrolled copy/paste of business rules
- architecture tests without meaningful rules
- big-bang rewrites without migration strategy

---

# 225. Positive Consequences

The decision provides:

- clearer business boundaries
- lower coupling
- higher cohesion
- improved testability
- fewer God Services
- fewer circular dependencies
- better domain ownership
- cleaner integration boundaries
- controlled microservice growth
- improved refactoring safety
- explicit package architecture
- automated architectural governance

---

# 226. Negative Consequences

The decision introduces:

- additional architectural analysis
- more deliberate package design
- domain modeling effort
- mapping boundaries
- architecture tests
- occasional additional types
- refactoring effort for legacy services

These costs are accepted because unmanaged structural complexity compounds over the lifetime of enterprise systems.

---

# 227. Neutral Consequences

The decision also means:

- not every service requires rich DDD
- not every entity requires a repository
- not every dependency requires an interface
- not every duplicated line should be abstracted
- not every large class must automatically be split
- not every small class should remain separate
- not every bounded context requires a microservice
- not every microservice should remain separate forever
- architectural simplicity is context-dependent

---

# 228. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| God Service | High | High | Cohesion review + decomposition |
| Distributed monolith | Critical | Medium | Service-boundary governance |
| Circular dependencies | High | Medium | ArchUnit |
| Wrong abstraction | High | Medium | Domain-driven review |
| Excessive fragmentation | Medium | Medium | Proportional architecture |
| Shared database coupling | Critical | Medium | Data ownership |
| N+1 integrations | High | Medium | Batch loading |
| Framework leakage | Medium | Medium | Dependency rules |
| Big-bang refactoring | High | Medium | Incremental migration |
| Microservice proliferation | High | Medium | Extraction criteria |

---

# 229. Implementation Guidance

The following rules are mandatory:

1. Architecture must prioritize business capability and cohesion.
2. DDD must be applied pragmatically rather than ceremonially.
3. Bounded contexts must represent meaningful model boundaries.
4. Domain terminology should follow ubiquitous language.
5. Aggregates must represent transactional consistency boundaries.
6. Aggregate roots must protect aggregate invariants.
7. Value Objects should be immutable.
8. Domain logic must not directly depend on infrastructure APIs.
9. Application services should orchestrate use cases.
10. Controllers must remain transport adapters.
11. Controllers must not directly access repositories or remote clients.
12. JPA entities must not normally be exposed as REST contracts.
13. External service DTOs must not automatically become local domain models.
14. Anti-Corruption Layers should isolate materially different external models.
15. Interfaces must represent meaningful abstractions rather than naming convention.
16. Generic `Impl` naming should be replaced with capability-specific names where appropriate.
17. Package structure must communicate architectural/business intent.
18. Package dependency cycles must be eliminated.
19. Significant package responsibilities should be documented with `package-info.java` according to project convention.
20. Classes with approximately 15-20 or more injected dependencies must receive architectural review.
21. Constructor dependencies must not be hidden merely to satisfy dependency-count rules.
22. God Services must be decomposed by cohesive responsibility.
23. Excessive class fragmentation must also be avoided.
24. Duplicate business rules should be consolidated when semantics are genuinely identical.
25. Similar but semantically different domain rules must not be forced into shared abstractions.
26. Remote N+1 patterns must be eliminated where batching is available.
27. Parallelism must not hide poor service boundaries.
28. Circular dependencies must not be masked with `@Lazy`.
29. Microservice extraction must require meaningful independent capability drivers.
30. Internal modularization must be evaluated before introducing a new distributed boundary.
31. Microservice consolidation should be considered where independent boundaries no longer exist.
32. Service data ownership must remain explicit.
33. Cross-service persistence access must be prohibited.
34. Transaction boundaries must avoid unnecessary remote calls.
35. Stable architecture rules must be enforced through ArchUnit where practical.
36. Refactoring must preserve behavior through automated tests.
37. Major legacy refactoring should use characterization tests.
38. Architectural migration should normally be incremental.
39. Dead and obsolete abstractions should be removed.
40. Tests must follow architectural boundaries rather than implementation details.

---

# 230. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Gradle
- ArchUnit
- JUnit 5
- AssertJ
- Mockito
- JaCoCo
- SonarQube
- SAST
- package dependency analysis
- Spring Modulith where appropriate
- PostgreSQL
- Testcontainers
- REST contract tests
- SQS contract tests
- architecture review

---

# 231. Success Criteria

The decision is successful when:

- controllers remain thin
- business logic has identifiable ownership
- God Services decrease
- constructor dependency counts become explainable
- package cycles are eliminated
- domain code is testable without infrastructure
- remote N+1 patterns decrease
- shared persistence coupling decreases
- architecture tests detect boundary violations
- new microservices require explicit architectural justification
- class decomposition improves cohesion rather than merely increasing file count
- duplicated business logic decreases without creating artificial abstractions
- service boundaries align with business capabilities

---

# 232. Alternatives Rejected

## 232.1 Technical-Layer Architecture Only

Rejected as the universal model because large systems lose business-capability visibility.

---

## 232.2 One Interface per Service

Rejected because interfaces without abstraction boundaries add ceremony.

---

## 232.3 One Class per Responsibility at Any Cost

Rejected because excessive fragmentation increases navigation and cognitive overhead.

---

## 232.4 Large Shared Domain Library

Rejected because it creates compile-time coupling between bounded contexts.

---

## 232.5 New Microservice for Every Large Module

Rejected because distributed boundaries introduce substantial operational complexity.

---

## 232.6 Shared Database as Integration

Rejected because it bypasses service ownership and contracts.

---

## 232.7 Big-Bang Rewrite

Rejected as the default because migration risk is usually excessive.

---

# 233. Related Decisions

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

---

# 234. References

- Domain-Driven Design — Eric Evans
- Implementing Domain-Driven Design — Vaughn Vernon
- Clean Architecture — Robert C. Martin
- Patterns of Enterprise Application Architecture — Martin Fowler
- Refactoring — Martin Fowler
- Building Microservices — Sam Newman
- Spring Modulith Documentation
- ArchUnit Documentation
- Spring Boot Documentation
- Java 21 Documentation
- OWASP Software Architecture Guidance
- AWS Well-Architected Framework

---

# 235. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-25 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise DDD, modularization and service-boundary baseline |

---

# 236. Decision Summary

The architectural dependency model becomes:

```text
            API / MESSAGING
                  |
                  v
             APPLICATION
                  |
                  v
               DOMAIN
                  ^
                  |
           INFRASTRUCTURE
```

Business modeling becomes:

```text
BOUNDED CONTEXT
      |
      +--> AGGREGATE
      |       |
      |       +--> AGGREGATE ROOT
      |       +--> ENTITY
      |       +--> VALUE OBJECT
      |
      +--> DOMAIN SERVICE
      |
      +--> DOMAIN EVENT
```

External integration becomes:

```text
EXTERNAL SYSTEM
       |
       v
    ADAPTER
       |
       v
ANTI-CORRUPTION
     LAYER
       |
       v
LOCAL APPLICATION
       |
       v
LOCAL DOMAIN
```

Controller responsibility becomes:

```text
HTTP
 |
 v
CONTROLLER
 |
 +--> CONTRACT VALIDATION
 +--> SECURITY CONTEXT
 |
 v
APPLICATION USE CASE
```

not:

```text
CONTROLLER
 |
 +--> REPOSITORY
 +--> WEBCLIENT
 +--> BUSINESS RULE
 +--> CACHE
 +--> MAPPING
 +--> TRANSACTION
```

A God Service is handled as:

```text
GOD SERVICE
    |
    +--> ORDER APPROVAL
    +--> PRICING
    +--> CUSTOMER VALIDATION
    +--> NOTIFICATION
    +--> REPORTING
    |
    v
IDENTIFY COHESIVE
CAPABILITIES
    |
    +--> ApprovalService
    +--> PricingService
    +--> CustomerPolicy
    +--> NotificationPort
    +--> ReportService
```

but decomposition stops before:

```text
OneMethodService

OneIfValidator

OneFieldMapper

OneLineHelper
```

becomes the architecture.

Class review becomes:

```text
LARGE CLASS
    |
    v
MULTIPLE CHANGE REASONS?
    |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
SPLIT  KEEP
```

Class unification becomes:

```text
CLASS A + CLASS B
       |
       v
SAME RESPONSIBILITY?
       |
       v
SAME CHANGE DRIVER?
       |
       v
SAME DOMAIN SEMANTICS?
       |
    +--+--+
    |     |
   YES    NO
    |     |
    v     v
CONSIDER KEEP
MERGE    SEPARATE
```

Microservice extraction becomes:

```text
LARGE MODULE
     |
     v
CAN INTERNAL
MODULARIZATION SOLVE IT?
     |
  +--+--+
  |     |
 YES    NO
  |     |
  v     v
KEEP    CHECK
LOCAL   INDEPENDENT:
        |
        +--> OWNERSHIP
        +--> DATA
        +--> SCALING
        +--> RELEASE
        +--> SECURITY
        +--> AVAILABILITY
              |
              v
        SERVICE BOUNDARY
```

A service boundary therefore requires more than:

```text
"THIS CLASS IS LARGE"
```

Shared code follows:

```text
SAME SYNTAX
    |
    v
SAME SEMANTICS?
    |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
MAY    DO NOT
SHARE  FORCE
```

Testing follows:

```text
DOMAIN
  |
  +--> PURE UNIT TESTS

APPLICATION
  |
  +--> USE-CASE TESTS

INFRASTRUCTURE
  |
  +--> INTEGRATION TESTS

API
  |
  +--> CONTRACT / SECURITY TESTS

ARCHITECTURE
  |
  +--> ARCHUNIT
```

The complete structural equation is:

```text
UBIQUITOUS LANGUAGE
        +
BOUNDED CONTEXTS
        +
COHESIVE AGGREGATES
        +
EXPLICIT APPLICATION USE CASES
        +
CONTROLLED DEPENDENCY DIRECTION
        +
PORTS / ADAPTERS WHERE VALUABLE
        +
ANTI-CORRUPTION BOUNDARIES
        +
EXPLICIT DATA OWNERSHIP
        +
HIGH COHESION
        +
LOW COUPLING
        +
CONTROLLED CLASS SIZE
        +
CONTROLLED DEPENDENCY COUNT
        +
NO ARTIFICIAL FRAGMENTATION
        +
NO DISTRIBUTED MONOLITH
        +
AUTOMATED ARCHITECTURE TESTS
        =
SUSTAINABLE ENTERPRISE ARCHITECTURE
```

The governing principle is:

```text
Model the business,
not the framework.

Keep domain rules close
to the domain concepts
they protect.

Keep infrastructure outside
the domain.

Keep controllers thin.

Use application services
to coordinate use cases.

Create interfaces where
there is a real boundary,
not because every class
needs an Impl.

Split classes because
responsibilities differ,
not because a line-count
threshold was exceeded.

Merge classes when their
separation is artificial.

Do not hide excessive
dependencies inside wrappers.

Do not turn every conditional
into a new class.

Do not turn every module
into a microservice.

Try internal modularization
before introducing a network.

Do not share persistence models
between bounded contexts.

Do not solve circular
dependencies with annotations.

Automate stable architecture
rules with ArchUnit.

Allow some duplication
when the alternative is
the wrong abstraction.

And optimize the architecture
for the ability to understand,
test, change and operate
the system safely.
```
