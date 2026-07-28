# ADR-089: Adopt Enterprise Hexagonal Architecture, Clean Architecture, Ports & Adapters and Module Boundary Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-089 |
| Title | Adopt Enterprise Hexagonal Architecture, Clean Architecture, Ports & Adapters and Module Boundary Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Architecture, Hexagonal Architecture, Clean Architecture, Ports & Adapters, Java |
| Related Domains | Cart, Orders, Customers, Products, Workflow |
| Supersedes | None |
| Superseded By | None |

---

> **Scope relationship:** This ADR specializes the Clean/Hexagonal Architecture and dependency-boundary portion of ADR-065. Domain modeling, aggregates, domain events and business-rule modeling are specialized by ADR-088.

---

# 1. Context

Enterprise Java applications frequently begin with a conventional layered structure:

```text
controller
    |
    v
service
    |
    v
repository
    |
    v
database
```

This structure is simple and appropriate for many applications.

As systems grow, however, architectural boundaries can erode.

Typical symptoms include:

```text
CONTROLLERS CALLING REPOSITORIES

SERVICES DEPENDING DIRECTLY ON WEBCLIENT

DOMAIN OBJECTS IMPORTING SPRING TYPES

BUSINESS RULES DEPENDING ON HTTP

JPA ENTITIES USED AS API CONTRACTS

EXTERNAL DTOs USED AS DOMAIN OBJECTS

DATABASE DETAILS LEAKING INTO BUSINESS LOGIC

SQS PUBLISHING INSIDE DOMAIN CLASSES

FRAMEWORK ANNOTATIONS DEFINING BUSINESS ARCHITECTURE

CIRCULAR PACKAGE DEPENDENCIES
```

The application then becomes increasingly organized around technology rather than business capabilities.

A typical dependency graph becomes:

```text
BUSINESS LOGIC
     |
     +--> SPRING MVC
     |
     +--> JPA
     |
     +--> WEBCLIENT
     |
     +--> SQS
     |
     +--> REDIS
     |
     +--> HTTP
```

The consequence is that changing infrastructure can require changes throughout the business implementation.

The organization therefore requires an architecture standard that preserves domain and application logic while allowing infrastructure technologies to evolve independently.

---

# 2. Problem Statement

The organization requires standards covering:

- Hexagonal Architecture
- Ports & Adapters
- Clean Architecture
- Dependency Inversion
- Domain layer
- Application layer
- inbound ports
- outbound ports
- inbound adapters
- outbound adapters
- REST controllers
- persistence adapters
- WebClient adapters
- RestClient adapters
- messaging adapters
- SQS adapters
- Redis adapters
- external API integration
- DTO/domain mapping
- framework isolation
- package organization
- package-by-feature
- Gradle modules
- module boundaries
- ArchUnit
- transaction boundaries
- dependency direction
- testing boundaries
- migration from layered architecture
- pragmatic architecture

---

# 3. Decision Drivers

Primary drivers are:

1. maintainability
2. domain isolation
3. testability
4. infrastructure replaceability
5. reduced coupling
6. explicit dependencies
7. service autonomy
8. clean integration boundaries
9. modernization safety
10. architecture governance
11. modularity
12. long-term changeability

---

# 4. Decision

Business-critical services SHOULD adopt Hexagonal/Clean Architecture principles where application complexity justifies the separation.

The fundamental dependency direction SHALL be:

```text
OUTSIDE
   |
   v
APPLICATION
   |
   v
DOMAIN
```

and not:

```text
DOMAIN
   |
   v
INFRASTRUCTURE
```

Infrastructure SHALL depend on abstractions defined by inner architectural layers where Dependency Inversion provides material value.

---

# 5. Fundamental Principle

```text
Business rules
should not know
whether they are invoked
through REST,

persisted through JPA,

published through SQS,

cached in Redis,

or integrated through WebClient.
```

---

# 6. Architecture Model

The preferred conceptual architecture is:

```text
                   OUTSIDE WORLD

        +-----------------------------------+
        |                                   |
        | REST       SQS       BATCH        |
        |  |          |          |          |
        |  v          v          v          |
        |      INBOUND ADAPTERS              |
        |              |                    |
        |              v                    |
        |         INBOUND PORTS              |
        |              |                    |
        |              v                    |
        |      APPLICATION / USE CASES       |
        |              |                    |
        |              v                    |
        |            DOMAIN                 |
        |              |                    |
        |              v                    |
        |         OUTBOUND PORTS             |
        |              |                    |
        |              v                    |
        |      OUTBOUND ADAPTERS             |
        |       /      |       \             |
        |      v       v        v            |
        |     DB     HTTP/API   SQS           |
        |                                   |
        +-----------------------------------+
```

---

# 7. Dependency Rule

Dependencies MUST point toward the business core.

Conceptually:

```text
ADAPTER
   |
   v
APPLICATION
   |
   v
DOMAIN
```

---

# 8. Forbidden Direction

Avoid:

```text
DOMAIN
   |
   v
WEBCLIENT

DOMAIN
   |
   v
JPA REPOSITORY

DOMAIN
   |
   v
HTTP RESPONSE
```

---

# 9. Domain Layer

The Domain layer contains business concepts and rules.

Typical contents include:

```text
ENTITIES

VALUE OBJECTS

AGGREGATES

DOMAIN SERVICES

POLICIES

SPECIFICATIONS

DOMAIN EVENTS

BUSINESS EXCEPTIONS
```

---

# 10. Domain Independence

The Domain SHOULD remain independent of infrastructure frameworks where practical.

---

# 11. Domain Must Not Know HTTP

Domain code MUST NOT depend on:

```text
ResponseEntity

HttpStatus

HttpServletRequest

WebClientResponseException
```

---

# 12. Domain Must Not Know Messaging

Domain code MUST NOT depend directly on:

```text
SqsClient

SQSTemplate

RabbitTemplate
```

---

# 13. Domain Must Not Know External APIs

Domain code SHOULD NOT directly invoke:

```text
WebClient

RestClient

FeignClient
```

---

# 14. Domain Persistence

Domain behavior SHOULD NOT depend directly on Spring Data repository implementations.

---

# 15. Pragmatic JPA

JPA annotations MAY exist on domain/persistence entities when separating models would create more complexity than value.

---

# 16. Framework Purity

Absolute framework purity is NOT required.

---

# 17. Architectural Goal

The goal is:

```text
PROTECT BUSINESS LOGIC
```

not:

```text
REMOVE EVERY
FRAMEWORK ANNOTATION
FROM EVERY CLASS
```

---

# 18. Application Layer

The Application layer coordinates use cases.

---

# 19. Application Responsibilities

Application services/use cases MAY:

```text
RECEIVE COMMAND

LOAD AGGREGATE

VALIDATE APPLICATION PRECONDITIONS

CALL DOMAIN BEHAVIOR

CALL OUTBOUND PORTS

PERSIST RESULT

REGISTER EVENT

RETURN RESULT
```

---

# 20. Application Is Orchestration

Conceptually:

```text
APPLICATION
     |
     +--> LOAD
     |
     +--> ORCHESTRATE
     |
     +--> CALL DOMAIN
     |
     +--> PERSIST
     |
     +--> INTEGRATE
```

---

# 21. Application Business Logic

Complex domain invariants SHOULD NOT accumulate exclusively in application services.

---

# 22. Use Case

A use case represents an application capability.

Examples:

```text
CreateOrder

ApproveOrder

RejectOrder

CheckoutCart

FindResponsibleConsultant
```

---

# 23. Use Case Interface

An inbound port MAY define a use-case contract.

Example:

```java
public interface ApproveOrderUseCase {

    OrderApprovalResult approve(ApproveOrderCommand command);
}
```

---

# 24. Interface Requirement

Not every application service requires an interface.

---

# 25. Interface Justification

An inbound-port interface SHOULD be introduced when it provides:

```text
BOUNDARY CLARITY

MULTIPLE ADAPTERS

TESTABILITY

MODULE ISOLATION

API STABILITY
```

---

# 26. Interface Inflation

Creating:

```text
OrderService

OrderServiceImpl
```

with exactly one implementation solely as ceremony is discouraged.

---

# 27. Port

A Port is an explicit contract through which the application communicates across an architectural boundary.

---

# 28. Port Types

Ports are classified as:

```text
INBOUND PORT

OUTBOUND PORT
```

---

# 29. Inbound Port

An inbound port exposes application behavior to an external actor.

---

# 30. Inbound Examples

```text
CreateOrderUseCase

ApproveOrderUseCase

CheckoutCartUseCase

SearchCustomerUseCase
```

---

# 31. Outbound Port

An outbound port represents something the application needs from infrastructure or another context.

---

# 32. Outbound Examples

```text
OrderRepository

CustomerGateway

ProductPriceGateway

WorkflowGateway

OrderEventPublisher

AuditGateway
```

---

# 33. Port Ownership

The layer requiring a capability SHOULD own the abstraction for that capability.

---

# 34. Dependency Inversion

Instead of:

```text
OrderService
    |
    v
WebClientCustomersClient
```

prefer:

```text
OrderApplicationService
       |
       v
CustomerGateway
       ^
       |
WebClientCustomerAdapter
```

---

# 35. Why

The application expresses:

```text
WHAT IT NEEDS
```

while infrastructure decides:

```text
HOW IT IS OBTAINED
```

---

# 36. Port Naming

Port names SHOULD express business/application capability.

---

# 37. Avoid Technical Port Naming

Avoid unnecessary abstractions such as:

```text
HttpPort

DatabasePort

WebClientPort
```

when a domain-oriented name is clearer.

---

# 38. Preferred Naming

Prefer:

```text
CustomerGateway

ProductCatalogGateway

OrderRepository

WorkflowPublisher
```

---

# 39. Inbound Adapter

Inbound adapters translate external interaction into application commands/queries.

---

# 40. Inbound Adapter Examples

```text
REST CONTROLLER

SQS LISTENER

SQS CONSUMER

BATCH JOB

SCHEDULED JOB

CLI
```

---

# 41. REST Controller

REST Controllers SHOULD remain thin.

---

# 42. Controller Responsibilities

Controllers SHOULD primarily:

```text
RECEIVE HTTP REQUEST

VALIDATE TRANSPORT SHAPE

MAP REQUEST

CALL USE CASE

MAP RESPONSE
```

---

# 43. Controller Must Not

Controllers SHOULD NOT contain:

```text
BUSINESS RULES

DATABASE QUERIES

MULTIPLE EXTERNAL API ORCHESTRATIONS

TRANSACTIONAL BUSINESS LOGIC
```

---

# 44. Controller to Repository

Direct:

```text
CONTROLLER
    |
    v
REPOSITORY
```

access is prohibited for business use cases.

---

# 45. Exception

Simple infrastructure/admin endpoints MAY justify direct technical access only when explicitly outside the business-domain architecture.

---

# 46. Request DTO

HTTP request DTOs belong to the inbound adapter/API contract.

---

# 47. Domain Entity as Request

Domain entities SHOULD NOT be used directly as external API request models.

---

# 48. Why

External API contracts and internal models evolve for different reasons.

---

# 49. Request Mapping

Conceptually:

```text
HTTP REQUEST DTO
       |
       v
MAPPER
       |
       v
COMMAND / APPLICATION INPUT
```

---

# 50. Response Mapping

Conceptually:

```text
APPLICATION RESULT
       |
       v
API MAPPER
       |
       v
HTTP RESPONSE DTO
```

---

# 51. Mapping Cost

Mapping SHOULD be introduced at meaningful architectural boundaries rather than mechanically between every method call.

---

# 52. Mapping Explosion

Avoid unnecessary chains such as:

```text
REQUEST DTO
   |
   v
CONTROLLER DTO
   |
   v
SERVICE DTO
   |
   v
DOMAIN DTO
   |
   v
ENTITY DTO
```

without distinct semantics.

---

# 53. Outbound Adapter

Outbound adapters implement outbound ports.

---

# 54. Persistence Adapter

A persistence adapter MAY contain:

```text
SPRING DATA REPOSITORY

JPA MAPPING

DATABASE QUERY

PERSISTENCE ENTITY

DOMAIN/PERSISTENCE TRANSLATION
```

---

# 55. Persistence Port

Application/domain code SHOULD depend on a repository abstraction representing its needs.

---

# 56. Example

```java
public interface OrderRepository {

    Optional<Order> findById(UUID id);

    Order save(Order order);
}
```

---

# 57. Spring Data Adapter

Infrastructure MAY implement the port using:

```text
JpaRepository

JdbcTemplate

jOOQ

R2DBC
```

according to architecture decisions.

---

# 58. Persistence Technology

Changing persistence technology SHOULD have minimal effect on domain/application behavior when abstraction value justifies the port.

---

# 59. Spring Data Leakage

Spring Data abstractions SHOULD NOT unnecessarily leak into core domain behavior.

---

# 60. Specification Leakage

`JpaSpecificationExecutor` and Criteria APIs SHOULD remain near persistence boundaries unless deliberately part of an application query architecture.

---

# 61. Query Architecture

Complex read-only queries MAY use a dedicated query path rather than reconstructing aggregates unnecessarily.

---

# 62. CQRS-Compatible Query

Conceptually:

```text
REST QUERY
    |
    v
QUERY SERVICE
    |
    v
QUERY REPOSITORY
    |
    v
PROJECTION
```

---

# 63. Aggregate Hydration

Read-only reporting endpoints SHOULD NOT be forced to hydrate large aggregates when projections are sufficient.

---

# 64. Command Persistence

Commands modifying domain state SHOULD preserve aggregate invariants.

---

# 65. HTTP Outbound Adapter

External HTTP integrations SHOULD be isolated behind outbound ports where they represent external capabilities.

---

# 66. WebClient Adapter

Example:

```text
APPLICATION
    |
    v
ProductPriceGateway
    ^
    |
WebClientProductPriceAdapter
    |
    v
PRODUCTS API
```

---

# 67. RestClient Adapter

The same principle applies to synchronous `RestClient` integrations.

---

# 68. External Contract Mapping

External response DTOs SHOULD be translated before entering core business logic where semantic separation exists.

---

# 69. Remote Error

Remote HTTP errors SHOULD be translated into application/integration error semantics at the adapter boundary.

---

# 70. WebClient Exception Leakage

Core application/domain code SHOULD NOT require detailed knowledge of:

```text
WebClientResponseException
```

---

# 71. Adapter Error Translation

Conceptually:

```text
HTTP 404
   |
   v
WEBCLIENT ADAPTER
   |
   v
CustomerNotFound / GatewayResult
```

according to application semantics.

---

# 72. Timeout

Timeout configuration belongs to infrastructure/adapters.

---

# 73. Circuit Breaker

Circuit-breaker configuration belongs to infrastructure/integration architecture.

---

# 74. Domain Awareness of Circuit Breaker

Domain code MUST NOT know whether a Circuit Breaker exists.

---

# 75. Retry

Retry infrastructure SHOULD remain outside core domain behavior.

---

# 76. Retry Semantic Effect

If retry changes business semantics, application/domain idempotency rules MUST still account for it.

---

# 77. Messaging Adapter

Message-broker infrastructure SHOULD be isolated through adapters.

---

# 78. SQS Publisher

Instead of:

```text
OrderService
    |
    v
SqsAsyncClient
```

prefer:

```text
OrderApplicationService
       |
       v
OrderEventPublisher
       ^
       |
SqsOrderEventPublisher
```

---

# 79. Event Contract

The adapter MAY translate an application/domain event into the external integration-event contract.

---

# 80. SQS Listener

Inbound SQS listeners SHOULD:

```text
DESERIALIZE

VALIDATE MESSAGE ENVELOPE

MAP

CALL USE CASE

HANDLE ACK/RETRY SEMANTICS
```

---

# 81. Listener Business Logic

Complex business rules SHOULD NOT live inside message listeners.

---

# 82. Messaging Infrastructure

Queue URLs, visibility timeout, retry policy and DLQ configuration belong to infrastructure/operations.

---

# 83. Redis Adapter

Cache infrastructure SHOULD be isolated from business logic when caching is an implementation concern.

---

# 84. Cache Port

Not every cache requires a domain port.

---

# 85. Cache Transparency

If caching only accelerates a repository/gateway, the adapter MAY hide the cache internally.

---

# 86. Cache Business Semantics

If cache/freshness affects business behavior, its semantics MUST become explicit at the appropriate application/domain boundary.

---

# 87. Audit Adapter

Audit integration SHOULD use an explicit application/infrastructure boundary.

---

# 88. Audit Domain Separation

Audit infrastructure MUST NOT become mixed with domain state mutation unnecessarily.

---

# 89. Framework Isolation

Framework-specific concerns SHOULD remain near adapters.

---

# 90. Examples

```text
@RestController

@Entity

@Repository

@Component

@SqsListener

@Configuration
```

SHOULD remain in layers where their framework semantics belong.

---

# 91. Spring Annotation

Using Spring annotations does not automatically violate Clean Architecture.

---

# 92. Important Question

The important question is:

```text
DOES THE BUSINESS RULE
DEPEND ON THE FRAMEWORK?
```

---

# 93. Configuration

Spring configuration belongs to infrastructure/bootstrap.

---

# 94. Bootstrap

Application startup wiring SHOULD be separated from business behavior.

---

# 95. Bean Configuration

Infrastructure beans MAY configure:

```text
WEBCLIENT

RESTCLIENT

OBJECTMAPPER

SQS CLIENT

EXECUTOR

RESILIENCE4J

REDIS
```

---

# 96. Domain Bean

Pure domain objects SHOULD NOT require container-managed lifecycle unless necessary.

---

# 97. Transaction Boundary

Transaction boundaries SHOULD normally exist in the application/use-case layer.

---

# 98. Transaction Example

```text
CONTROLLER
    |
    v
APPLICATION SERVICE
    |
    +--> BEGIN TRANSACTION
    |
    +--> LOAD AGGREGATE
    |
    +--> DOMAIN CHANGE
    |
    +--> SAVE
    |
    +--> OUTBOX
    |
    +--> COMMIT
```

---

# 99. Controller Transaction

Controllers SHOULD NOT own business transaction boundaries.

---

# 100. Repository Transaction

Repositories SHOULD NOT define the entire business transaction merely because they access the database.

---

# 101. Domain Transaction

Domain entities SHOULD remain unaware of database transaction infrastructure.

---

# 102. Remote Call Inside Transaction

Long-running external calls SHOULD generally occur outside database transactions when business consistency permits.

---

# 103. Transaction Design

Where external validation must occur before persistence:

```text
CALL REMOTE DEPENDENCY
       |
       v
VALIDATE
       |
       v
BEGIN LOCAL TRANSACTION
       |
       v
PERSIST
```

MAY be preferable.

---

# 104. Race Condition

Moving remote validation outside the transaction MUST account for race conditions where authoritative state can change.

---

# 105. Transactional Outbox

When state mutation and event creation must be atomic:

```text
APPLICATION TRANSACTION
        |
        +--> DOMAIN STATE
        |
        +--> OUTBOX
        |
        v
COMMIT
```

---

# 106. Outbox Dispatcher

Outbox dispatch belongs to infrastructure.

---

# 107. Domain Does Not Dispatch

Domain code SHOULD create/record facts but MUST NOT directly send network messages.

---

# 108. Dependency Injection

Constructor injection SHOULD be preferred.

---

# 109. Required Dependency

Required application dependencies SHOULD be explicit in constructors.

---

# 110. Field Injection

Field injection SHOULD be avoided.

---

# 111. Static Locator

Service Locator/static application-context lookup is prohibited for normal business dependencies.

---

# 112. Dependency Count

Excessive constructor dependencies SHOULD trigger responsibility review.

---

# 113. Port Explosion

A class with twenty outbound ports likely has excessive orchestration responsibility.

---

# 114. Cohesion

Application use cases SHOULD remain cohesive.

---

# 115. Package Organization

Large services SHOULD prefer organization that exposes business capabilities.

---

# 116. Pure Package by Layer

Avoid scaling indefinitely as:

```text
controller/
service/
repository/
mapper/
dto/
entity/
```

where hundreds of unrelated domain concepts become mixed.

---

# 117. Package by Feature

Prefer conceptually:

```text
orders/
    creation/
    approval/
    cancellation/
    query/
```

with architecture boundaries inside the feature where appropriate.

---

# 118. Alternative Context Structure

Another valid organization:

```text
orders/
    domain/
    application/
    adapter/
        in/
        out/
```

---

# 119. No Universal Folder Template

One folder template is NOT mandated for every service.

---

# 120. Package Goal

Package organization SHOULD make:

```text
BUSINESS CAPABILITY

DEPENDENCY DIRECTION

OWNERSHIP
```

easy to understand.

---

# 121. Example Hexagonal Feature

```text
orders/
└── approval/
    ├── domain/
    │   ├── OrderApprovalPolicy.java
    │   └── ApprovalDecision.java
    │
    ├── application/
    │   ├── ApproveOrderUseCase.java
    │   ├── ApproveOrderService.java
    │   └── port/
    │       └── out/
    │           ├── OrderRepository.java
    │           └── WorkflowGateway.java
    │
    └── adapter/
        ├── in/
        │   └── web/
        │       └── OrderApprovalController.java
        │
        └── out/
            ├── persistence/
            │   └── JpaOrderRepositoryAdapter.java
            │
            └── workflow/
                └── WebClientWorkflowAdapter.java
```

---

# 122. Naming Flexibility

Exact package names MAY differ.

Architectural semantics matter more than directory aesthetics.

---

# 123. Module Boundary

Material architectural boundaries SHOULD be enforceable.

---

# 124. Java Package Boundary

Java package visibility MAY provide lightweight enforcement.

---

# 125. Gradle Module

Gradle modules MAY provide stronger compile-time boundaries.

---

# 126. Multi-Module Candidate

Consider a Gradle module when:

```text
BOUNDARY IS STABLE

DEPENDENCY DIRECTION MATTERS

TEAM OWNERSHIP IS DISTINCT

BUILD IS LARGE

REUSE IS CONTROLLED
```

---

# 127. Module Explosion

Creating a Gradle module for every package is prohibited.

---

# 128. Example Modules

A sufficiently large service MAY evolve toward:

```text
orders-domain

orders-application

orders-adapters

orders-bootstrap
```

but this is NOT mandatory.

---

# 129. Simpler Alternative

For many microservices:

```text
ONE GRADLE MODULE
+
STRICT PACKAGES
+
ARCHUNIT
```

is preferable.

---

# 130. Build Complexity

Module boundaries MUST justify their build/dependency-management overhead.

---

# 131. ArchUnit

ArchUnit SHOULD enforce stable architecture rules.

---

# 132. Example Rule

Conceptually:

```text
domain
must not depend on
adapter
```

---

# 133. Controller Rule

```text
controller
must not depend directly on
repository
```

---

# 134. Adapter Rule

Inbound adapters MAY depend on application ports.

---

# 135. Outbound Adapter Rule

Outbound adapters MAY depend on outbound-port contracts.

---

# 136. Domain Framework Rule

Domain packages SHOULD NOT depend on HTTP/controller packages.

---

# 137. Cross-Context Rule

One bounded context MUST NOT import another context's internal implementation packages.

---

# 138. Architecture Test Location

Architecture tests SHOULD execute as part of the normal test suite.

---

# 139. CI

Architecture-rule violations SHOULD fail CI when rules are mandatory.

---

# 140. Suppression

Architecture-rule suppression MUST require explicit justification.

---

# 141. Permanent Suppression

Permanent broad exclusions SHOULD be avoided.

---

# 142. Architecture Fitness Function

Architecture rules should be treated as executable architecture documentation.

---

# 143. DTO Categories

DTOs SHOULD have explicit purpose.

Potential categories:

```text
API REQUEST

API RESPONSE

APPLICATION COMMAND

APPLICATION QUERY

INTEGRATION REQUEST

INTEGRATION RESPONSE

EVENT CONTRACT

PERSISTENCE PROJECTION
```

---

# 144. DTO Reuse

DTO reuse across architectural boundaries SHOULD NOT be the default.

---

# 145. Same Shape

Two objects having the same fields does not mean they represent the same contract.

---

# 146. Mapping Boundary

Mapping SHOULD occur where ownership changes.

---

# 147. Mapping Example

```text
Products API Response
        |
        v
ProductsAdapter
        |
        v
Local ProductSnapshot
```

---

# 148. Mapper

Dedicated mappers MAY be used when mapping complexity warrants them.

---

# 149. Simple Mapping

Simple mappings MAY remain inline when a mapper class would add no value.

---

# 150. Mapper Business Logic

Mappers SHOULD NOT become hidden business-rule engines.

---

# 151. Mapper Side Effect

Mapping SHOULD normally be deterministic and side-effect free.

---

# 152. API Evolution

API DTOs MAY evolve independently of domain classes.

---

# 153. Domain Evolution

Domain classes MAY evolve without automatically breaking external API contracts.

---

# 154. Persistence Evolution

Database representation MAY evolve without automatically changing API contracts.

---

# 155. Adapter Contract Test

Outbound HTTP adapters SHOULD have focused tests for:

```text
REQUEST

HEADERS

SERIALIZATION

ERROR MAPPING

TIMEOUT BEHAVIOR
```

where appropriate.

---

# 156. Persistence Adapter Test

Persistence adapters SHOULD receive database integration tests where query/mapping behavior matters.

---

# 157. Domain Test

Domain behavior SHOULD be testable without:

```text
SPRING BOOT CONTEXT

DATABASE

HTTP SERVER

AWS
```

where practical.

---

# 158. Application Test

Application services SHOULD be testable with mocked/fake outbound ports.

---

# 159. Controller Test

Controller tests SHOULD focus on HTTP contract and delegation.

---

# 160. Test Pyramid

Architecture SHOULD naturally support:

```text
MANY FAST DOMAIN TESTS

APPLICATION TESTS

FOCUSED ADAPTER TESTS

FEWER END-TO-END TESTS
```

---

# 161. Mocking Boundary

Mock architectural ports rather than arbitrary internal implementation details where practical.

---

# 162. Mock WebClient

Application-service unit tests SHOULD NOT need to mock low-level WebClient internals if an outbound port exists.

---

# 163. Fake Adapter

In-memory fake adapters MAY improve application testing for suitable ports.

---

# 164. Test Coupling

Tests SHOULD NOT make refactoring impossible by asserting irrelevant implementation details.

---

# 165. Adapter Replacement

An adapter SHOULD be replaceable without rewriting business rules.

---

# 166. Example

Conceptually:

```text
CustomerGateway
      ^
      |
      +--> RestClientCustomerAdapter

later:

CustomerGateway
      ^
      |
      +--> CachedCustomerAdapter
```

without modifying the business use case.

---

# 167. Decorator Adapter

Cross-cutting infrastructure MAY decorate adapters.

Example:

```text
CustomerGateway
      ^
      |
CachedCustomerGateway
      |
      v
RemoteCustomerGateway
```

---

# 168. Resilience Decorator

Circuit-breaker/retry behavior MAY decorate integration adapters.

---

# 169. Logging Decorator

Logging SHOULD remain controlled and avoid excessive wrapper layers without value.

---

# 170. Observability

Tracing/metrics SHOULD be added primarily at:

```text
USE CASE BOUNDARIES

INTEGRATION BOUNDARIES

MESSAGE PROCESSING BOUNDARIES
```

---

# 171. Domain Logging

Domain entities SHOULD generally not become responsible for technical logging.

---

# 172. Correlation ID

Correlation context SHOULD be propagated by infrastructure/application boundaries rather than manually threaded through every domain method unless it has business meaning.

---

# 173. Security Adapter

Authentication belongs primarily to inbound/security infrastructure.

---

# 174. Application Authorization

Application use cases MAY enforce authorization decisions requiring use-case context.

---

# 175. Domain Eligibility

Domain rules MAY independently determine whether the requested action is valid.

---

# 176. Three Concerns

Keep distinct:

```text
AUTHENTICATION
      |
      v
WHO ARE YOU?

AUTHORIZATION
      |
      v
MAY YOU CALL THIS USE CASE?

DOMAIN RULE
      |
      v
CAN THIS BUSINESS OBJECT
PERFORM THIS ACTION?
```

---

# 177. Security Principal

Core domain objects SHOULD NOT depend on Spring Security principal types.

---

# 178. Actor Model

If actor identity has domain meaning, translate security identity into an application/domain concept.

---

# 179. Example

```text
JWT PRINCIPAL
     |
     v
CONTROLLER / SECURITY ADAPTER
     |
     v
ApprovalActor
```

---

# 180. Error Boundary

Errors SHOULD be translated as they cross architectural boundaries.

---

# 181. Domain Error

Example:

```text
OrderCannotBeApproved
```

---

# 182. Application Error

Example:

```text
CustomerUnavailable
```

---

# 183. Infrastructure Error

Example:

```text
HTTP 503

SOCKET TIMEOUT

SQL EXCEPTION
```

---

# 184. API Error

Example:

```json
{
  "code": "ORDER_CANNOT_BE_APPROVED",
  "message": "..."
}
```

---

# 185. Translation Flow

```text
INFRASTRUCTURE FAILURE
        |
        v
ADAPTER TRANSLATION
        |
        v
APPLICATION ERROR
        |
        v
API EXCEPTION HANDLER
        |
        v
HTTP RESPONSE
```

---

# 186. SQL Exception Leakage

Controllers SHOULD NOT receive raw SQL exceptions.

---

# 187. HTTP Exception Leakage

Domain code SHOULD NOT receive raw HTTP-client exceptions.

---

# 188. SQS Exception Leakage

Domain code SHOULD NOT receive AWS SDK exceptions.

---

# 189. Global Exception Handler

Global exception handlers belong to inbound/API infrastructure.

---

# 190. Error Sanitization

Sensitive error sanitization belongs at trust/output boundaries.

---

# 191. Sanitization Business Data

Infrastructure sanitization MUST NOT silently modify legitimate business data.

---

# 192. Configuration Port

Application configuration SHOULD NOT automatically require a port.

---

# 193. Technical Configuration

Timeouts, URLs and pool sizes belong to infrastructure configuration.

---

# 194. Business Configuration

Business-configurable policy values MAY be exposed through an application/domain abstraction when dynamic ownership requires it.

---

# 195. Environment Variable

Domain classes SHOULD NOT directly read environment variables.

---

# 196. Clock Port

Time-dependent business logic MAY depend on:

```java
Clock
```

or an application abstraction to enable deterministic testing.

---

# 197. UUID Generation

Identity generation MAY use a port when generation strategy matters to domain/application behavior.

---

# 198. Randomness

Randomness affecting business behavior SHOULD be injectable/testable.

---

# 199. File Adapter

Legacy file integrations SHOULD be modeled as adapters.

---

# 200. Batch Adapter

Batch processing SHOULD invoke application use cases rather than duplicate business rules.

---

# 201. Scheduled Adapter

Schedulers SHOULD trigger application behavior rather than contain the business operation.

---

# 202. Forms/Legacy Adapter

Legacy interfaces MAY remain adapters around modernized application/domain logic where migration constraints require them.

---

# 203. Adapter Principle

The architecture does not care whether the caller is:

```text
REST

BATCH

QUEUE

LEGACY FORM

CLI
```

when they invoke the same business capability.

---

# 204. Multiple Inbound Adapters

One use case MAY be invoked by multiple inbound adapters.

---

# 205. Example

```text
REST CONTROLLER ----+
                    |
SQS LISTENER -------+--> ApproveOrderUseCase
                    |
BATCH JOB ----------+
```

when semantics are genuinely identical.

---

# 206. Avoid Duplicate Logic

Adapters MUST NOT independently reproduce the same use-case logic.

---

# 207. Cart Architecture

Cart SHOULD separate:

```text
CART BUSINESS RULES

CHECKOUT ORCHESTRATION

PRODUCT INTEGRATION

ORDERS INTEGRATION

PERSISTENCE

HTTP
```

---

# 208. Cart Example

```text
CartController
      |
      v
CheckoutCartUseCase
      |
      v
CheckoutCartService
      |
      +--> CartRepository
      |
      +--> ProductPriceGateway
      |
      +--> CustomerGateway
      |
      +--> OrdersGateway
```

with infrastructure implementations behind the ports where beneficial.

---

# 209. Cart Product Adapter

Product price calculation MAY be represented as:

```text
ProductPriceGateway
        ^
        |
WebClientProductPriceAdapter
```

---

# 210. Cart Orders Adapter

Order creation MAY be represented as:

```text
OrdersGateway
     ^
     |
WebClientOrdersAdapter
```

---

# 211. Cart Domain

Cart domain behavior SHOULD remain independent from Orders API DTOs.

---

# 212. Orders Architecture

Orders SHOULD separate:

```text
ORDER DOMAIN

ORDER USE CASES

WORKFLOW INTEGRATION

CUSTOMERS INTEGRATION

PRODUCTS INTEGRATION

AUDIT

PERSISTENCE

MESSAGING
```

---

# 213. Orders Approval Example

```text
OrderApprovalController
        |
        v
ApproveOrderUseCase
        |
        v
ApproveOrderService
        |
        +--> OrderRepository
        |
        +--> CustomerGateway
        |
        +--> UserGateway
        |
        +--> WorkflowEventPublisher
```

---

# 214. Workflow Event Publisher

The application SHOULD depend on:

```text
WorkflowEventPublisher
```

rather than:

```text
SqsAsyncClient
```

---

# 215. Outbox Implementation

The outbound implementation MAY persist an Outbox record rather than immediately publishing to SQS.

---

# 216. Application Semantics

The application requests:

```text
PUBLISH / REGISTER WORKFLOW EVENT
```

while infrastructure determines the reliable delivery mechanism.

---

# 217. Customers Architecture

Customers SHOULD separate:

```text
CUSTOMER DOMAIN

REGION/CONSULTANT RULES

USER API INTEGRATION

PERSISTENCE

CACHE

HTTP
```

---

# 218. Consultant Resolution

Consultant-resolution rules SHOULD not depend directly on `RestClient`.

---

# 219. User Gateway

Conceptually:

```text
ResponsibleConsultantService
        |
        v
UserDirectoryGateway
        ^
        |
RestClientUserDirectoryAdapter
```

---

# 220. Cache Decoration

A resilient cache MAY decorate the outbound adapter without changing domain/application rules.

---

# 221. Products Architecture

Products SHOULD expose product/pricing capabilities through stable contracts.

---

# 222. Consumers

Cart/Orders SHOULD implement their own outbound adapters to Products rather than importing Products internals.

---

# 223. Workflow Architecture

Workflow SHOULD remain a separate bounded context and expose process capabilities through contracts.

---

# 224. Workflow Internal Model

Orders MUST NOT import Workflow persistence/domain implementation classes.

---

# 225. Migration Strategy

Existing layered applications SHOULD migrate incrementally.

---

# 226. No Big-Bang Rewrite

A complete architectural rewrite is NOT required.

---

# 227. Step 1

Identify high-coupling/high-change areas.

---

# 228. Step 2

Identify use cases.

Example:

```text
CHECKOUT CART

APPROVE ORDER

SEARCH ORDERS
```

---

# 229. Step 3

Separate controller logic from application orchestration.

---

# 230. Step 4

Introduce outbound ports around high-value external dependencies.

---

# 231. Step 5

Move integration implementations behind adapters.

---

# 232. Step 6

Extract important business invariants into domain behavior/policies.

---

# 233. Step 7

Add architecture fitness functions.

---

# 234. Step 8

Continue incrementally when touched by business changes.

---

# 235. Migration Priority

Prioritize:

```text
HIGH CHANGE FREQUENCY

HIGH DEFECT RATE

HIGH COUPLING

CRITICAL BUSINESS RULES
```

---

# 236. Low-Risk Legacy Area

Stable simple code MAY remain conventionally layered.

---

# 237. Architecture Consistency

New development in strategically important areas SHOULD follow the target architecture even while older areas migrate gradually.

---

# 238. Transitional Architecture

Temporary coexistence is acceptable:

```text
LEGACY LAYERED AREA
        |
        +
        |
NEW HEXAGONAL FEATURE
```

provided boundaries remain controlled.

---

# 239. Adapter Around Legacy

Legacy implementations MAY temporarily serve as outbound adapters.

---

# 240. Example

```text
NEW APPLICATION
      |
      v
CustomerGateway
      ^
      |
LegacyCustomerServiceAdapter
```

---

# 241. Strangler Alignment

Ports & Adapters works naturally with incremental strangler modernization.

---

# 242. Architecture Decision

Hexagonal Architecture is a means, not a product requirement.

---

# 243. Pragmatism

Architecture complexity MUST remain proportional to domain/system complexity.

---

# 244. Small CRUD Service

For a simple CRUD service:

```text
CONTROLLER

SERVICE

REPOSITORY
```

may remain appropriate.

---

# 245. Complex Service

For a complex integration-heavy service:

```text
USE CASES

DOMAIN

PORTS

ADAPTERS
```

provide stronger value.

---

# 246. Ceremony Test

Before introducing an abstraction ask:

```text
WHAT CHANGE
OR RISK
DOES THIS BOUNDARY
PROTECT US FROM?
```

---

# 247. No Answer

If there is no meaningful answer, the abstraction MAY be unnecessary.

---

# 248. Port Test

A port SHOULD represent a stable application need rather than mirror a library API.

---

# 249. Bad Port

Avoid:

```java
interface WebClientPort {
    Mono<String> get(String url);
}
```

---

# 250. Better Port

Prefer:

```java
interface ProductPriceGateway {

    PriceResult calculatePrice(
            ProductId productId,
            CustomerId customerId);
}
```

---

# 251. Infrastructure Type Leakage

Infrastructure-specific types SHOULD NOT unnecessarily cross ports.

---

# 252. Reactor Leakage

If the application is intentionally reactive, Reactor types MAY be part of application contracts.

Otherwise they SHOULD remain adapter/infrastructure details.

---

# 253. CompletableFuture Leakage

Async types SHOULD enter application ports only when asynchronous semantics are intentionally part of the use case.

---

# 254. Virtual Threads

Virtual Threads MAY allow synchronous-looking application ports while infrastructure handles scalable blocking I/O.

---

# 255. Concurrency Ownership

Concurrency strategy SHOULD normally remain application/infrastructure behavior rather than domain behavior.

---

# 256. Parallel Integration

An application service MAY execute independent outbound ports concurrently when:

```text
CALLS ARE INDEPENDENT

RESOURCE LIMITS EXIST

FAILURE SEMANTICS ARE DEFINED

CONTEXT PROPAGATION IS CORRECT
```

---

# 257. Domain Parallelism

Domain rules SHOULD NOT become coupled to executor/thread implementation.

---

# 258. Timeout Budget

Application use cases SHOULD have bounded integration latency.

---

# 259. Adapter Timeout

Individual outbound adapters SHOULD respect the use-case latency budget.

---

# 260. Cancellation

Parallel outbound operations SHOULD support cancellation/interruption semantics where relevant.

---

# 261. Port Granularity

Ports SHOULD be cohesive.

---

# 262. God Gateway

Avoid:

```text
ExternalSystemsGateway
```

containing dozens of unrelated integrations.

---

# 263. Capability Gateway

Prefer:

```text
CustomerGateway

ProductGateway

WorkflowGateway
```

where these represent distinct capabilities.

---

# 264. One Method Port

A one-method port is acceptable when it represents one meaningful capability.

---

# 265. Interface Size

Interface size SHOULD reflect cohesive responsibility rather than arbitrary method-count rules.

---

# 266. Read/Write Ports

Read and write ports MAY be separated when their consumers/evolution differ materially.

---

# 267. Domain Repository

Repository ports SHOULD reflect aggregate needs rather than database-table CRUD.

---

# 268. Database-Specific Method

Avoid core abstractions such as:

```text
findByColumnXAndColumnYAndDeletedFalseOrderByCreatedAtDesc
```

when a domain-oriented query can express intent more clearly.

---

# 269. Query Port

Dedicated query ports MAY expose query semantics without pretending every query is aggregate-domain behavior.

---

# 270. Pagination

Pagination belongs primarily to query/application/API concerns.

---

# 271. Sorting

External sort fields SHOULD be validated/mapped before reaching persistence infrastructure.

---

# 272. Sort Path

Database/internal property names SHOULD NOT automatically become public API sort contracts.

---

# 273. Persistence Mapping

Persistence adapters SHOULD protect the application from schema peculiarities.

---

# 274. Legacy Schema

A legacy schema SHOULD NOT force awkward naming throughout the domain.

---

# 275. Database Column

Example:

```text
TB_ORD_HDR.CD_ST_ORD
```

MAY map internally to:

```text
OrderStatus
```

---

# 276. Database Evolution

Flyway migrations belong to persistence/infrastructure concerns.

---

# 277. Domain Awareness of Flyway

Domain/application code MUST NOT depend on migration version details.

---

# 278. Migration Rule

Applied Flyway migrations MUST remain immutable; database corrections require a new migration version.

---

# 279. Bootstrap Module

A bootstrap/application module MAY assemble:

```text
SPRING BOOT

CONFIGURATION

ADAPTER IMPLEMENTATIONS

PORT BINDINGS
```

---

# 280. Composition Root

Dependency wiring SHOULD occur at a clear composition root.

---

# 281. Manual Wiring

Manual bean configuration MAY make architectural bindings explicit.

---

# 282. Component Scanning

Component scanning MAY be used, but MUST NOT obscure forbidden dependencies.

---

# 283. Circular Spring Dependency

Circular bean dependencies SHOULD be treated as architectural smells.

---

# 284. `@Lazy`

`@Lazy` SHOULD NOT be used merely to hide circular business dependencies.

---

# 285. Circular Dependency Resolution

Resolve cycles through:

```text
RESPONSIBILITY REDESIGN

EVENTS

EXTRACTED PORT

APPLICATION ORCHESTRATION
```

rather than container tricks.

---

# 286. Architecture Documentation

Major services SHOULD document their:

```text
INBOUND ADAPTERS

USE CASES

OUTBOUND PORTS

OUTBOUND ADAPTERS

DOMAIN

DATA OWNERSHIP
```

---

# 287. Diagram

A simple architecture diagram SHOULD be sufficient to understand dependency direction.

---

# 288. Code as Architecture

Package/module structure and architecture tests SHOULD reinforce documentation.

---

# 289. Documentation Drift

Architecture documentation without executable boundaries will eventually drift.

---

# 290. Architecture Review Checklist

```text
[ ] What is the business capability?

[ ] What is the use case?

[ ] What belongs to the domain?

[ ] What belongs to application orchestration?

[ ] What are the inbound adapters?

[ ] What are the outbound dependencies?

[ ] Which outbound dependencies deserve ports?

[ ] Are ports named by capability?

[ ] Are framework types leaking inward?

[ ] Is the controller thin?

[ ] Does any controller access a repository directly?

[ ] Does domain code know HTTP?

[ ] Does domain code know WebClient/RestClient?

[ ] Does domain code know SQS/AWS?

[ ] Are external DTOs translated?

[ ] Are persistence concerns isolated?

[ ] Are transaction boundaries explicit?

[ ] Are remote calls unnecessarily inside transactions?

[ ] Are application services cohesive?

[ ] Is there a God Gateway?

[ ] Are package boundaries clear?

[ ] Can ArchUnit enforce the stable rules?

[ ] Is the architecture more complex than the problem requires?
```

---

# 291. Port Review Checklist

```text
[ ] Does this port represent a real application need?

[ ] Is the port owned by the consuming layer?

[ ] Is its name business/capability oriented?

[ ] Does it avoid mirroring infrastructure APIs?

[ ] Are infrastructure types excluded where unnecessary?

[ ] Is the interface cohesive?

[ ] Is the abstraction stable enough to justify itself?

[ ] Can it be replaced by another adapter?

[ ] Can application tests fake/mock it easily?

[ ] Does it reduce meaningful coupling?
```

---

# 292. Adapter Review Checklist

```text
[ ] Does the adapter implement one clear boundary?

[ ] Does it translate external models?

[ ] Does it translate infrastructure errors?

[ ] Are timeout/retry/resilience concerns configured here?

[ ] Does it avoid business-rule duplication?

[ ] Does it avoid leaking external DTOs inward?

[ ] Are sensitive values protected in logs?

[ ] Does it have focused integration/contract tests?

[ ] Are resources bounded?

[ ] Is observability adequate?
```

---

# 293. Architecture Fitness Functions

The following SHOULD be automated where applicable:

```text
[ ] Domain cannot depend on adapter packages

[ ] Domain cannot depend on controller packages

[ ] Domain cannot depend on WebClient/RestClient

[ ] Domain cannot depend on AWS SDK

[ ] Controllers cannot depend directly on repositories

[ ] Inbound adapters depend on application/use-case contracts

[ ] Outbound adapters implement approved outbound ports

[ ] Cross-context internal package imports are forbidden

[ ] Circular package dependencies are forbidden

[ ] Applied Flyway migrations remain immutable

[ ] Production System.out/printStackTrace is forbidden

[ ] Critical architecture tests execute in CI
```

---

# 294. Example ArchUnit Intent

Conceptually:

```java
noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
                "..controller..",
                "..adapter..",
                "org.springframework.web..",
                "software.amazon.awssdk..");
```

Exact package rules MUST reflect the service's chosen structure.

---

# 295. Enterprise Architecture Gate

A change is not considered compliant when applicable conditions include:

```text
[ ] Controller accesses repository directly

[ ] Controller contains significant business rules

[ ] Domain imports HTTP types

[ ] Domain directly invokes WebClient/RestClient

[ ] Domain directly publishes to SQS

[ ] Application directly depends on AWS SDK without architectural justification

[ ] External DTO is used as domain model without translation

[ ] JPA entity is exposed directly as public API contract

[ ] Infrastructure exception leaks into domain behavior

[ ] One common gateway contains unrelated external systems

[ ] Port simply mirrors WebClient/RestClient methods

[ ] Interface exists only because every service historically had Service/ServiceImpl

[ ] Transaction boundary exists in controller

[ ] Long remote calls unnecessarily hold DB transactions

[ ] Circular dependency is hidden using @Lazy

[ ] New module exists without meaningful architectural boundary

[ ] Architecture package rules are documented but not enforced where automation is practical

[ ] Hexagonal patterns add more complexity than the business problem
```

---

# 296. Anti-Patterns

The following are prohibited or strongly discouraged:

- controller-to-repository business flows
- business rules inside controllers
- God Services
- God Gateways
- domain depending on HTTP
- domain depending on AWS SDK
- domain depending directly on WebClient/RestClient
- domain publishing directly to message brokers
- external DTOs as domain entities
- JPA entities as public API contracts
- infrastructure exceptions leaking across layers
- interface-per-class ceremony
- `Service`/`ServiceImpl` without architectural purpose
- generic technical ports
- port mirroring library APIs
- mapper explosion
- module explosion
- package-by-layer at unlimited scale
- `@Lazy` as circular-dependency architecture
- transaction in controllers
- remote calls inside long DB transactions without justification
- architecture purity that provides no business/engineering value

---

# 297. Positive Consequences

The decision provides:

- stronger business-core isolation
- clearer dependency direction
- easier application testing
- infrastructure replaceability
- cleaner integration contracts
- reduced framework leakage
- improved modularity
- better modernization boundaries
- reduced controller/service complexity
- explicit transaction ownership
- improved architecture governance
- easier adoption of DDD boundaries

---

# 298. Negative Consequences

The decision introduces:

- additional interfaces where justified
- adapter classes
- mapping code
- architecture tests
- package/module governance
- more deliberate dependency design
- potential onboarding cost

These costs are accepted for systems whose complexity requires stronger architectural boundaries.

---

# 299. Neutral Consequences

The decision also means:

- not every service requires full Hexagonal Architecture
- JPA annotations may remain in pragmatic domain models
- one implementation does not automatically require an interface
- one-method ports can be valid
- simple mappings need not have mapper classes
- one Gradle module may be sufficient
- package-by-feature and domain/application/adapter layouts can both be valid
- framework use is acceptable when dependency direction remains controlled
- architecture purity is secondary to maintainability and business correctness

---

# 300. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Overengineering | High | Medium | Complexity-driven adoption |
| Port explosion | Medium | Medium | Capability-oriented ports |
| Mapper explosion | Medium | Medium | Map only at meaningful boundaries |
| Module explosion | Medium | Low | Use packages + ArchUnit first |
| Domain framework leakage | Medium | Medium | Architecture tests |
| God Application Service | High | Medium | Use-case decomposition |
| Infrastructure leakage | High | Medium | Adapter error/model translation |
| Transaction misuse | High | Medium | Application-owned boundaries |
| Circular dependencies | High | Medium | ArchUnit + responsibility redesign |
| Migration disruption | High | Low/Medium | Incremental modernization |

---

# 301. Implementation Guidance

The following rules are mandatory:

1. Dependency direction must protect the business core.
2. Domain behavior must not depend on HTTP, messaging or remote-client infrastructure.
3. Application services should represent cohesive use cases.
4. Controllers must remain thin and must not access repositories directly for business flows.
5. Outbound ports should represent application capabilities rather than infrastructure APIs.
6. Ports must be introduced only when they provide meaningful boundary value.
7. External HTTP integrations should be isolated in outbound adapters where appropriate.
8. Infrastructure exceptions should be translated at adapter boundaries.
9. External DTOs should not leak into core domain behavior when semantic ownership differs.
10. JPA entities should not be exposed directly as public API contracts.
11. Message listeners should delegate business behavior to application use cases.
12. Domain code must not directly publish to brokers.
13. Reliable event publication must remain compatible with Transactional Outbox standards.
14. Transaction boundaries should normally be application/use-case concerns.
15. Long remote calls should not unnecessarily hold database transactions.
16. Constructor injection should be preferred.
17. Circular dependencies must not be hidden using framework workarounds.
18. Package organization should make business capabilities and dependency direction understandable.
19. Gradle modules should be introduced only for meaningful boundaries.
20. Stable architectural dependency rules should be automated through ArchUnit or equivalent.
21. Domain tests should run without unnecessary Spring/infrastructure dependencies.
22. Application tests should mock/fake outbound ports rather than low-level client internals where practical.
23. Adapter tests should verify external contract, mapping and failure semantics.
24. Existing layered systems should migrate incrementally rather than through architectural rewrites.
25. Hexagonal/Clean Architecture ceremony must remain proportional to actual system complexity.

---

# 302. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Gradle
- JUnit 5
- AssertJ
- Mockito
- ArchUnit
- Testcontainers
- JaCoCo
- SonarQube
- SAST
- Spring Data JPA
- PostgreSQL
- Flyway
- WebClient
- RestClient
- Resilience4j
- AWS SQS
- Transactional Outbox
- Redis where applicable
- OpenAPI
- contract tests
- architecture fitness functions
- CI/CD quality gates

---

# 303. Success Criteria

The decision is successful when:

- business rules can be tested without infrastructure
- controllers remain thin
- application use cases are explicit
- external systems are represented through clear capabilities
- domain code does not depend on HTTP/AWS/client infrastructure
- infrastructure exceptions do not leak into business logic
- cross-context models are translated
- persistence changes have reduced impact on business logic
- architecture cycles decrease
- God Services decrease
- application services become more cohesive
- architecture rules automatically prevent dependency erosion
- existing services can migrate incrementally without disruptive rewrites

---

# 304. Alternatives Rejected

## 304.1 Traditional Layering Everywhere

Rejected as the universal architecture because complex integration-heavy domains require stronger dependency boundaries.

Traditional layering remains acceptable for sufficiently simple services.

---

## 304.2 Full Hexagonal Architecture Everywhere

Rejected because simple CRUD/reference-data services may not justify the additional abstraction.

---

## 304.3 Framework-Free Domain at Any Cost

Rejected because eliminating harmless framework annotations can create excessive mapping and maintenance complexity.

---

## 304.4 Interface for Every Class

Rejected because interfaces should represent meaningful boundaries, not naming ceremony.

---

## 304.5 Multi-Module Gradle for Every Layer

Rejected because excessive build modularization increases complexity without necessarily improving architecture.

---

## 304.6 Direct Infrastructure Usage Everywhere

Rejected because it creates strong coupling between business behavior and implementation technology.

---

## 304.7 Big-Bang Architectural Rewrite

Rejected because existing systems can migrate safely through incremental feature/use-case extraction.

---

# 305. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-083: Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard
- ADR-084: Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard
- ADR-085: Enterprise Dependency Management, Gradle, SBOM, Supply Chain Security and Vulnerability Governance Standard
- ADR-086: Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard
- ADR-087: Enterprise Technical Debt, Legacy Modernization, Refactoring and Continuous Architecture Governance Standard
- ADR-088: Enterprise Domain-Driven Design, Bounded Context, Aggregate, Domain Event and Business Rule Modeling Standard

---

# 306. References

- Alistair Cockburn — Hexagonal Architecture
- Robert C. Martin — Clean Architecture
- Eric Evans — Domain-Driven Design
- Vaughn Vernon — Implementing Domain-Driven Design
- Martin Fowler — Patterns of Enterprise Application Architecture
- Martin Fowler — Dependency Inversion
- Sam Newman — Building Microservices
- Chris Richardson — Microservices Patterns
- ArchUnit Documentation
- Java 21 Documentation
- Spring Boot Documentation
- Spring Framework Documentation
- Testcontainers Documentation
- Gradle Documentation

---

# 307. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial Hexagonal Architecture, Clean Architecture and Ports & Adapters governance baseline |

---

# 308. Decision Summary

The architectural dependency becomes:

```text
INBOUND ADAPTER
      |
      v
INBOUND PORT
      |
      v
APPLICATION
      |
      v
DOMAIN
```

while external dependencies become:

```text
APPLICATION
      |
      v
OUTBOUND PORT
      ^
      |
OUTBOUND ADAPTER
      |
      v
INFRASTRUCTURE
```

A REST flow becomes:

```text
HTTP
 |
 v
CONTROLLER
 |
 v
REQUEST MAPPING
 |
 v
USE CASE
 |
 v
DOMAIN
 |
 v
REPOSITORY PORT
 |
 v
JPA ADAPTER
 |
 v
DATABASE
```

An external integration becomes:

```text
APPLICATION
     |
     v
CustomerGateway
     ^
     |
RestClientCustomerAdapter
     |
     v
CUSTOMERS API
```

instead of:

```text
BUSINESS SERVICE
      |
      v
RESTCLIENT
      |
      v
REMOTE API
```

Messaging becomes:

```text
DOMAIN CHANGE
      |
      v
APPLICATION
      |
      v
EVENT PORT
      |
      v
OUTBOX ADAPTER
      |
      v
OUTBOX
      |
      v
DISPATCHER
      |
      v
SQS
```

rather than:

```text
DOMAIN ENTITY
      |
      v
SQS CLIENT
```

Testing becomes:

```text
DOMAIN TEST
     |
     +--> NO SPRING
     +--> NO DATABASE
     +--> NO HTTP
     +--> FAST

APPLICATION TEST
     |
     +--> MOCK / FAKE PORTS

ADAPTER TEST
     |
     +--> REALISTIC INFRASTRUCTURE

E2E TEST
     |
     +--> CRITICAL FLOWS
```

Package architecture evolves from:

```text
controller/
service/
repository/
dto/
mapper/
```

toward business-visible organization such as:

```text
orders/
    approval/
        domain/
        application/
        adapter/

    creation/
        domain/
        application/
        adapter/

    query/
        application/
        adapter/
```

where this improves navigation and ownership.

For Cart:

```text
CartController
      |
      v
CheckoutCartUseCase
      |
      v
CheckoutCartService
      |
      +--> CartRepository
      |
      +--> ProductPriceGateway
      |
      +--> CustomerGateway
      |
      +--> OrdersGateway
```

For Orders:

```text
OrderApprovalController
        |
        v
ApproveOrderUseCase
        |
        v
ApproveOrderService
        |
        +--> OrderRepository
        |
        +--> CustomerGateway
        |
        +--> UserGateway
        |
        +--> WorkflowEventPublisher
```

For Customers:

```text
CustomerController
       |
       v
ResponsibleConsultantUseCase
       |
       v
ResponsibleConsultantService
       |
       +--> CustomerRepository
       |
       +--> UserDirectoryGateway
                    ^
                    |
          RestClientUserAdapter
```

The complete architecture equation is:

```text
DOMAIN MODEL
      +
COHESIVE USE CASES
      +
DEPENDENCY INVERSION
      +
INBOUND PORTS
      +
OUTBOUND PORTS
      +
THIN ADAPTERS
      +
EXPLICIT MAPPING
      +
FRAMEWORK ISOLATION
      +
APPLICATION TRANSACTIONS
      +
CAPABILITY-ORIENTED PACKAGES
      +
ARCHUNIT FITNESS FUNCTIONS
      +
INCREMENTAL MIGRATION
      =
EVOLVABLE ENTERPRISE ARCHITECTURE
```

The governing principle is:

```text
Keep the business
at the center.

HTTP is an adapter.

The database
is an adapter.

SQS is an adapter.

Redis is an adapter.

WebClient is an adapter.

RestClient is an adapter.

They are important,

but they are not
the business.

Controllers receive.

Use cases orchestrate.

The domain decides.

Repositories persist.

Gateways integrate.

Adapters translate.

Keep dependencies
pointing inward.

Do not let
OrderApproval
know how WebClient works.

Do not let
Cart checkout
know the Orders
HTTP response structure.

Do not let
a Customer rule
depend on RestClient.

Define what
the application needs.

That is the port.

Implement how
the outside world
provides it.

That is the adapter.

Do not create
interfaces merely
to append Impl
to class names.

An interface
must protect
a meaningful boundary.

Do not create
five DTOs
for every field
because a diagram
showed five layers.

Map when ownership
or semantics change.

Do not create
a Gradle module
for every folder.

Enforce boundaries
only where the boundary
has value.

Use ArchUnit
to stop architecture
from slowly eroding.

Keep controllers thin.

Keep transactions deliberate.

Keep remote calls bounded.

Keep infrastructure errors
outside the domain.

Keep framework details
near framework boundaries.

But do not chase
architectural purity
for its own sake.

A JPA annotation
is not automatically
an architecture failure.

A simple CRUD service
does not need
twenty ports.

Architecture exists
to make change safer,

not to make
the directory tree
more impressive.

For complex systems,

protect the business core.

Make integrations replaceable.

Make use cases visible.

Make dependency direction
obvious.

Make architecture
executable through tests.

And when migrating
an existing system,

do it incrementally.

One capability.

One boundary.

One improvement
at a time.
```
