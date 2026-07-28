# ADR-088: Adopt Enterprise Domain-Driven Design, Bounded Context, Aggregate, Domain Event and Business Rule Modeling Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-088 |
| Title | Adopt Enterprise Domain-Driven Design, Bounded Context, Aggregate, Domain Event and Business Rule Modeling Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Domain-Driven Design, Domain Modeling, Microservices, Business Architecture |
| Related Domains | Cart, Orders, Customers, Products, Workflow |
| Supersedes | None |
| Superseded By | None |

---

> **Scope relationship:** This ADR specializes the domain-modeling portion of ADR-065. Architecture dependency direction, Ports & Adapters and module-boundary enforcement are specialized by ADR-089.

---

# 1. Context

Enterprise systems are not primarily collections of:

```text
CONTROLLERS

SERVICES

REPOSITORIES

DTOs

DATABASE TABLES
```

They implement business capabilities.

For an order platform, those capabilities may include:

```text
CUSTOMER MANAGEMENT

PRODUCT CATALOG

SHOPPING CART

PRICING

ORDER CREATION

ORDER APPROVAL

WORKFLOW

BILLING

FULFILLMENT
```

A technically well-structured Java application can still have a poor domain model.

Example:

```text
OrderController
      |
      v
OrderService
      |
      v
OrderRepository
      |
      v
tb_order
```

This architecture says very little about:

```text
WHAT IS AN ORDER?

WHEN CAN IT CHANGE?

WHO CAN APPROVE IT?

WHAT MAKES IT VALID?

WHICH STATES ARE ALLOWED?

WHICH BUSINESS RULES MUST ALWAYS HOLD?
```

Domain-Driven Design provides techniques for aligning software structure with business concepts.

---

# 2. Problem Statement

The organization requires standards covering:

- Domain-Driven Design
- strategic DDD
- tactical DDD
- bounded contexts
- ubiquitous language
- context mapping
- aggregates
- aggregate roots
- entities
- value objects
- invariants
- domain services
- application services
- repositories
- factories
- specifications
- policies
- domain events
- integration events
- anti-corruption layers
- transactional boundaries
- eventual consistency
- business-rule modeling
- aggregate size
- domain ownership
- anemic domain models
- microservice boundaries
- cross-context communication

---

# 3. Decision Drivers

Primary drivers are:

1. business-rule correctness
2. maintainability
3. domain clarity
4. service autonomy
5. explicit ownership
6. reduced coupling
7. transactional consistency
8. testability
9. architecture evolution
10. integration safety
11. business/software alignment
12. modernization sustainability

---

# 4. Decision

Complex business capabilities SHALL be modeled using Domain-Driven Design principles where domain complexity justifies them.

DDD MUST NOT be interpreted as:

```text
EVERY CLASS
MUST USE
A DESIGN PATTERN
```

Instead:

```text
DOMAIN COMPLEXITY
      |
      v
EXPLICIT BUSINESS MODEL
      |
      v
CLEAR OWNERSHIP
      |
      v
PROTECTED INVARIANTS
```

---

# 5. Fundamental Principle

```text
The database
does not define
the business model.

The business model
defines what the system
must protect.
```

---

# 6. Strategic DDD

Strategic DDD SHOULD identify:

```text
BUSINESS CAPABILITIES

SUBDOMAINS

BOUNDED CONTEXTS

OWNERSHIP

RELATIONSHIPS
```

before tactical patterns are applied.

---

# 7. Tactical DDD

Tactical DDD MAY use:

```text
ENTITY

VALUE OBJECT

AGGREGATE

DOMAIN SERVICE

REPOSITORY

FACTORY

SPECIFICATION

DOMAIN EVENT
```

where each concept has actual domain meaning.

---

# 8. Pattern Inflation

DDD patterns MUST NOT be introduced mechanically.

---

# 9. CRUD Domain

Simple CRUD/reference-data functionality MAY remain intentionally simple.

---

# 10. Complexity Test

DDD provides greatest value where behavior contains:

```text
BUSINESS RULES

STATE TRANSITIONS

INVARIANTS

MULTIPLE ACTORS

COMPLEX DECISIONS
```

---

# 11. Ubiquitous Language

Each bounded context MUST develop a consistent business vocabulary.

---

# 12. Language Alignment

The same domain concept SHOULD use consistent terminology in:

```text
BUSINESS DISCUSSIONS

CODE

APIs

EVENTS

DOCUMENTATION

TESTS
```

---

# 13. Translation

Technical naming MAY differ where external/legacy terminology requires translation, but the mapping MUST be explicit.

---

# 14. Ambiguous Terms

Generic words such as:

```text
STATUS

TYPE

DATA

PROCESS

ITEM
```

SHOULD be qualified when ambiguity exists.

---

# 15. Contextual Meaning

A term may legitimately have different meanings in different bounded contexts.

---

# 16. Example: Customer

In:

```text
CUSTOMERS CONTEXT
```

Customer may represent:

```text
REGISTRATION

COMMERCIAL INFORMATION

REGIONS

CONSULTANT RESPONSIBILITY
```

while in:

```text
ORDERS CONTEXT
```

the order may require only:

```text
CUSTOMER ID

NAME SNAPSHOT

COMMERCIAL CONDITIONS
```

---

# 17. Shared Model

Different contexts MUST NOT automatically share the same internal domain model merely because they refer to the same business entity.

---

# 18. Bounded Context

A bounded context defines the boundary within which a domain model and language are consistent.

---

# 19. Context Ownership

Each bounded context SHOULD have explicit ownership of:

```text
BUSINESS RULES

DATA

APIs

EVENTS
```

---

# 20. Context Boundary

A bounded context is not automatically equivalent to:

```text
ONE PACKAGE

ONE TABLE

ONE MICROSERVICE
```

---

# 21. Microservice Mapping

A microservice SHOULD generally belong to one primary bounded context.

---

# 22. Multiple Contexts in One Service

Multiple bounded contexts MAY temporarily coexist in one deployable application when:

```text
BOUNDARIES REMAIN EXPLICIT

MODULES REMAIN SEPARATE

DATA OWNERSHIP IS CLEAR
```

---

# 23. One Context Across Many Services

A large bounded context MAY require multiple deployable components, but this increases coordination complexity and requires deliberate design.

---

# 24. Context Identification

Context boundaries SHOULD be discovered from:

```text
BUSINESS OWNERSHIP

LANGUAGE

CHANGE PATTERNS

TRANSACTIONAL RULES

DATA OWNERSHIP

TEAM RESPONSIBILITY
```

---

# 25. Database Table Boundary

Database-table boundaries MUST NOT determine bounded contexts by themselves.

---

# 26. Service Boundary

A service boundary SHOULD reflect business capability and ownership rather than arbitrary technical layering.

---

# 27. Example Enterprise Context Map

A conceptual platform map may contain:

```text
CUSTOMERS
    |
    v
CART
    |
    v
ORDERS
    |
    v
WORKFLOW

PRODUCTS
    |
    +------> CART
    |
    +------> ORDERS
```

Exact dependencies MUST follow actual business ownership.

---

# 28. Context Map

Relationships between bounded contexts SHOULD be documented.

---

# 29. Context Mapping Patterns

Applicable relationships MAY include:

```text
CUSTOMER / SUPPLIER

CONFORMIST

ANTI-CORRUPTION LAYER

OPEN HOST SERVICE

PUBLISHED LANGUAGE

SHARED KERNEL
```

---

# 30. Shared Kernel

Shared Kernel SHOULD be used sparingly.

---

# 31. Shared Kernel Risk

A shared domain library creates coordinated change.

---

# 32. Shared Kernel Candidate

Only genuinely stable shared concepts SHOULD be considered.

---

# 33. Shared DTO Library

Creating a common JAR containing every service DTO is prohibited as a default integration strategy.

---

# 34. Reason

It creates:

```text
COMPILE-TIME COUPLING

LOCKSTEP UPGRADES

DOMAIN LEAKAGE

SERVICE AUTONOMY LOSS
```

---

# 35. Anti-Corruption Layer

An Anti-Corruption Layer SHOULD isolate one context from another context's domain model where semantic differences exist.

---

# 36. ACL Flow

```text
EXTERNAL CONTEXT
       |
       v
EXTERNAL CONTRACT
       |
       v
TRANSLATOR / ADAPTER
       |
       v
LOCAL DOMAIN MODEL
```

---

# 37. Legacy ACL

Legacy integrations SHOULD frequently use an ACL.

---

# 38. External DTO

External DTOs MUST NOT automatically become internal domain entities.

---

# 39. Domain Translation

Translation SHOULD occur at the integration boundary.

---

# 40. Entity

An Entity is defined primarily by identity and lifecycle.

---

# 41. Entity Identity

Two entities with identical attributes may still represent different business objects.

---

# 42. Example

```text
Order #1001
```

remains the same order as its status changes.

---

# 43. Entity Equality

Entity equality SHOULD reflect identity semantics appropriate to the model.

---

# 44. Database ID

A database surrogate key MAY represent entity identity but SHOULD NOT automatically define the entire domain semantics.

---

# 45. Value Object

A Value Object is defined by its values rather than identity.

---

# 46. Value Object Examples

Potential examples include:

```text
Money

Address

DateRange

EmailAddress

PhoneNumber

Quantity
```

when domain semantics justify them.

---

# 47. Value Object Immutability

Value Objects SHOULD normally be immutable.

---

# 48. Value Equality

Value Objects SHOULD use value-based equality.

---

# 49. Validation

A Value Object SHOULD protect its own valid state where practical.

---

# 50. Invalid Value Object

Prefer preventing creation of:

```text
INVALID VALUE
```

rather than creating it and hoping later validation detects it.

---

# 51. Primitive Obsession

Repeated domain concepts represented only by primitive types SHOULD trigger modeling review.

---

# 52. Primitive Obsession Example

Instead of spreading:

```java
String email
```

with repeated validation everywhere, a domain-specific value type MAY provide stronger semantics where complexity warrants it.

---

# 53. Over-Modeling

Not every String requires a custom Value Object.

---

# 54. Aggregate

An Aggregate is a transactional consistency boundary around domain objects.

---

# 55. Aggregate Root

External code MUST interact with an Aggregate through its Aggregate Root.

---

# 56. Aggregate Invariant

The Aggregate Root MUST protect invariants belonging to the aggregate.

---

# 57. Aggregate Example

Conceptually:

```text
ORDER
 |
 +--> ORDER ITEM
 |
 +--> ORDER ITEM
 |
 +--> TOTALS
 |
 +--> STATUS
```

If these must remain transactionally consistent, they MAY belong to one aggregate.

---

# 58. Aggregate Is Not Object Graph

An Aggregate is not simply:

```text
EVERYTHING
RELATED
TO AN ENTITY
```

---

# 59. Aggregate Size

Aggregates SHOULD remain as small as possible while protecting required invariants.

---

# 60. Large Aggregate

Large aggregates increase:

```text
LOCK CONTENTION

LOAD COST

TRANSACTION SIZE

COUPLING

CONCURRENCY CONFLICTS
```

---

# 61. Aggregate Boundary Question

Ask:

```text
WHAT MUST
BE CONSISTENT
IMMEDIATELY
IN ONE TRANSACTION?
```

---

# 62. Immediate Consistency

Only data required by the same invariant SHOULD generally require the same aggregate transaction.

---

# 63. Cross-Aggregate Rule

Rules spanning multiple aggregates SHOULD normally use IDs/references rather than loading a giant shared object graph.

---

# 64. Aggregate Reference

One aggregate SHOULD generally reference another aggregate by identity.

---

# 65. Direct Object Graph

Direct mutable object references between aggregates SHOULD be avoided.

---

# 66. Transaction

A command SHOULD normally modify one aggregate per local transaction.

---

# 67. Multiple Aggregates

If one business operation must affect multiple aggregates, consider:

```text
APPLICATION ORCHESTRATION

DOMAIN EVENTS

SAGA

EVENTUAL CONSISTENCY
```

before expanding aggregate boundaries.

---

# 68. Aggregate Repository

Repositories SHOULD normally operate on Aggregate Roots.

---

# 69. Child Repository

Repositories for internal aggregate children SHOULD generally be avoided.

---

# 70. Invariant

An invariant is a business condition that MUST remain true.

---

# 71. Example Invariant

Conceptually:

```text
AN APPROVED ORDER
CANNOT RETURN
TO CREATED
```

if that is the business rule.

---

# 72. Invariant Location

Invariants SHOULD be enforced as close as practical to the domain model responsible for them.

---

# 73. Controller Validation

Controllers MUST NOT be the authoritative location for business invariants.

---

# 74. DTO Validation

Bean Validation MAY protect request shape.

Example:

```text
NOT NULL

MAX LENGTH

FORMAT
```

but does not replace domain rules.

---

# 75. Validation Layers

Validation SHOULD distinguish:

```text
TRANSPORT VALIDATION

APPLICATION VALIDATION

DOMAIN INVARIANT
```

---

# 76. Transport Validation

Examples:

```text
REQUIRED FIELD

STRING LENGTH

JSON FORMAT
```

---

# 77. Application Validation

Examples:

```text
CUSTOMER EXISTS

USER EXISTS

EXTERNAL RESOURCE AVAILABLE
```

---

# 78. Domain Validation

Examples:

```text
ORDER CAN BE APPROVED

STATUS TRANSITION IS VALID

QUANTITY RESPECTS DOMAIN RULE
```

---

# 79. Business Rule Duplication

The same domain invariant MUST NOT be independently reimplemented across controllers/services.

---

# 80. Domain Behavior

Behavior SHOULD reside with the domain concept that owns it where practical.

---

# 81. Anemic Domain Model

A model containing only getters/setters while all business logic resides in giant services SHOULD trigger review.

---

# 82. Anemic Model Example

Avoid:

```java
order.setStatus(APPROVED);
```

from arbitrary code if approval requires business validation.

---

# 83. Behavior-Rich Model

Prefer conceptual behavior such as:

```java
order.approve(approvalContext);
```

when approval is genuinely an Order-domain responsibility.

---

# 84. Encapsulation

Domain state SHOULD NOT be freely mutable when mutations have business meaning.

---

# 85. Setter

Public setters SHOULD NOT bypass invariants.

---

# 86. JPA Constraint

JPA persistence requirements MAY influence entity implementation but MUST NOT eliminate domain encapsulation unnecessarily.

---

# 87. Persistence Model

The persistence representation and domain model MAY be the same class for pragmatic reasons when complexity remains manageable.

---

# 88. Separate Models

Separate persistence and domain models MAY be justified when:

```text
PERSISTENCE CONCERNS DISTORT DOMAIN

LEGACY SCHEMA IS COMPLEX

DOMAIN MODEL IS RICH

MULTIPLE STORAGE MODELS EXIST
```

---

# 89. Mapping Cost

Separating persistence/domain models introduces mapping cost and SHOULD NOT be done automatically.

---

# 90. Domain Service

A Domain Service represents domain behavior that does not naturally belong to one Entity or Value Object.

---

# 91. Domain Service Characteristics

A Domain Service SHOULD:

```text
USE DOMAIN LANGUAGE

REPRESENT BUSINESS LOGIC

REMAIN INFRASTRUCTURE-INDEPENDENT
```

where practical.

---

# 92. Domain Service Anti-Pattern

`OrderDomainService` MUST NOT become another generic dumping ground.

---

# 93. Application Service

An Application Service orchestrates a use case.

---

# 94. Application Service Responsibilities

Typical responsibilities:

```text
LOAD AGGREGATE

CALL DOMAIN BEHAVIOR

CALL EXTERNAL PORTS

PERSIST

PUBLISH EVENT

MANAGE USE-CASE FLOW
```

---

# 95. Application Service Business Logic

Application services SHOULD avoid becoming the sole home of complex domain invariants.

---

# 96. Application vs Domain

Conceptually:

```text
APPLICATION SERVICE
       |
       v
ORCHESTRATES

DOMAIN MODEL
       |
       v
DECIDES
```

---

# 97. Repository

A Repository provides collection-like access to aggregates.

---

# 98. Repository Language

Repository APIs SHOULD use domain language.

---

# 99. Repository Example

Prefer concepts such as:

```text
findOrderById

save

existsByCustomer
```

according to domain need.

---

# 100. Generic Repository

A custom generic repository abstraction SHOULD NOT be created merely to hide Spring Data without meaningful value.

---

# 101. Repository Leakage

Domain/application layers SHOULD avoid depending unnecessarily on persistence-specific query machinery.

---

# 102. `Pageable`

Framework pagination types MAY exist at application boundaries but SHOULD be prevented from contaminating core domain behavior where unnecessary.

---

# 103. Factory

A Factory MAY encapsulate complex valid aggregate creation.

---

# 104. Factory Need

Use a Factory when creation requires:

```text
MULTIPLE RULES

DERIVED VALUES

MULTIPLE COLLABORATORS

NONTRIVIAL INVARIANTS
```

---

# 105. Simple Constructor

Simple valid creation does not require a Factory pattern.

---

# 106. Specification

A Specification MAY represent a reusable business predicate.

---

# 107. Specification Example

Conceptually:

```text
OrderCanBeApproved
```

rather than duplicating a complex conditional.

---

# 108. Specification Composition

Specifications MAY be composed when this improves business readability.

---

# 109. Specification Abuse

Do not wrap every `if` statement in a Specification.

---

# 110. Policy

A Policy MAY represent a domain decision that can vary independently.

---

# 111. Strategy

Strategy MAY be used when multiple business algorithms implement the same conceptual decision.

---

# 112. Business Rule Naming

Rules SHOULD be named using business meaning rather than technical implementation.

---

# 113. Example

Prefer:

```text
OrderApprovalPolicy
```

over:

```text
StatusIfElseHelper
```

---

# 114. State Transition

Domain lifecycle transitions MUST be explicit.

---

# 115. Order State Machine

A conceptual order lifecycle might be:

```text
CREATED
   |
   v
PENDING_ANALYST
   |
   +--> APPROVED_ANALYST
   |
   v
PENDING_SUPERVISOR
   |
   +--> APPROVED_SUPERVISOR
   |
   +--> CANCELLED
```

Exact transitions MUST follow business requirements.

---

# 116. Arbitrary Status Assignment

Arbitrary:

```java
setStatus(...)
```

SHOULD NOT bypass lifecycle rules.

---

# 117. Transition Method

Prefer explicit behavior:

```text
approveByAnalyst()

sendToSupervisor()

cancel()
```

where it accurately reflects the domain.

---

# 118. State Machine Complexity

A dedicated state-machine abstraction MAY be justified for sufficiently complex workflows.

---

# 119. State Machine Overhead

A state-machine framework SHOULD NOT be introduced for a trivial lifecycle.

---

# 120. Workflow Context

Workflow orchestration and Order domain state MUST have clearly defined ownership.

---

# 121. Duplicate Ownership

Orders and Workflow MUST NOT independently become authoritative for the same business state without a reconciliation model.

---

# 122. Source of Truth

For every important business state, define:

```text
WHO OWNS IT?
```

---

# 123. Derived State

Other contexts MAY maintain derived/read state when ownership remains explicit.

---

# 124. Domain Event

A Domain Event represents something meaningful that occurred within a domain.

---

# 125. Domain Event Naming

Domain events SHOULD use past tense.

Examples:

```text
OrderCreated

OrderApproved

OrderCancelled

CartCheckedOut
```

---

# 126. Event Meaning

An event describes:

```text
SOMETHING
THAT ALREADY HAPPENED
```

---

# 127. Command vs Event

Command:

```text
ApproveOrder
```

Event:

```text
OrderApproved
```

---

# 128. Domain Event Immutability

Domain Events SHOULD be immutable.

---

# 129. Event Data

Events SHOULD contain sufficient information for their intended consumers without exposing the entire aggregate unnecessarily.

---

# 130. Entity Serialization

Serializing a complete JPA entity as an integration event is prohibited.

---

# 131. Domain Event Scope

An internal Domain Event MAY remain inside one bounded context.

---

# 132. Integration Event

An Integration Event crosses a bounded-context/process boundary.

---

# 133. Domain vs Integration Event

They MAY share business meaning but SHOULD NOT automatically be the same Java object.

---

# 134. Reason

Integration contracts require independent:

```text
VERSIONING

COMPATIBILITY

SECURITY

EVOLUTION
```

---

# 135. Event Translation

Conceptually:

```text
DOMAIN EVENT
     |
     v
APPLICATION / EVENT MAPPER
     |
     v
INTEGRATION EVENT
     |
     v
BROKER
```

---

# 136. Event Ownership

The publishing bounded context owns the meaning of the event it publishes.

---

# 137. Consumer Model

Consumers SHOULD translate events into their own local model.

---

# 138. Consumer Coupling

Consumers SHOULD NOT depend on publisher internal classes.

---

# 139. Event Versioning

Integration-event evolution MUST follow compatibility standards.

---

# 140. Event ID

Integration events SHOULD contain a stable event identifier where deduplication/traceability requires it.

---

# 141. Correlation

Events SHOULD carry correlation/trace identifiers according to observability standards.

---

# 142. Timestamp

Integration events SHOULD contain an occurrence timestamp where relevant.

---

# 143. Aggregate ID

Events SHOULD identify the relevant aggregate/business object.

---

# 144. User Context

Actor/user information MAY be included where required for:

```text
AUDIT

WORKFLOW

BUSINESS RULES
```

subject to privacy/security standards.

---

# 145. Event PII

Events MUST NOT contain unnecessary sensitive information.

---

# 146. Eventual Consistency

Cross-context consistency SHOULD normally be eventual rather than implemented through distributed database transactions.

---

# 147. Distributed Transaction

Distributed two-phase commit SHOULD generally be avoided across microservices.

---

# 148. Local Transaction

Each service SHOULD commit its own local state transactionally.

---

# 149. Event Publication

Reliable event publication SHOULD use patterns such as Transactional Outbox where required.

---

# 150. Outbox

Conceptually:

```text
LOCAL TRANSACTION
      |
      +--> UPDATE AGGREGATE
      |
      +--> INSERT OUTBOX EVENT
      |
      v
COMMIT
```

then:

```text
OUTBOX
   |
   v
DISPATCHER
   |
   v
BROKER
```

---

# 151. Dual Write

Avoid:

```text
DATABASE COMMIT
       |
       v
BROKER PUBLISH
```

as two unrelated operations when event loss would violate business reliability.

---

# 152. Event Consumer

Consumers MUST assume duplicate delivery is possible.

---

# 153. Idempotency

Event consumers SHOULD be idempotent.

---

# 154. Event Ordering

Consumers MUST NOT assume global event ordering unless infrastructure/contracts explicitly provide it.

---

# 155. Aggregate Ordering

Where ordering matters, ordering SHOULD normally be scoped to the relevant aggregate/business key.

---

# 156. Saga

A Saga MAY coordinate business operations spanning multiple bounded contexts.

---

# 157. Saga Need

Use a Saga when:

```text
MULTIPLE LOCAL TRANSACTIONS

BUSINESS PROCESS

COMPENSATION / RECOVERY

EVENTUAL CONSISTENCY
```

must be coordinated.

---

# 158. Saga Is Not Database Rollback

A Saga does not provide distributed ACID rollback.

---

# 159. Compensation

Compensation is a business operation.

---

# 160. Compensation Example

If an order reservation fails after another step succeeds, compensation MAY release the previous reservation.

---

# 161. Compensation Is Not Undo

Some real-world operations cannot be literally undone.

---

# 162. Saga Orchestration

A Saga MAY use orchestration.

```text
ORCHESTRATOR
    |
    +--> STEP A
    |
    +--> STEP B
    |
    +--> STEP C
```

---

# 163. Saga Choreography

A Saga MAY use choreography.

```text
EVENT A
   |
   v
SERVICE B
   |
   v
EVENT B
   |
   v
SERVICE C
```

---

# 164. Choreography Risk

Excessive choreography can make business flow difficult to understand.

---

# 165. Orchestration Risk

Excessive orchestration can centralize domain knowledge in one process manager.

---

# 166. Saga Choice

The approach MUST reflect process complexity and ownership.

---

# 167. Domain Error

Domain-rule violations SHOULD use explicit domain/application error semantics.

---

# 168. Technical Error

Domain rejection MUST be distinguished from:

```text
NETWORK FAILURE

DATABASE FAILURE

TIMEOUT

BROKER FAILURE
```

---

# 169. Example

```text
ORDER_CANNOT_BE_APPROVED
```

is fundamentally different from:

```text
WORKFLOW_SERVICE_TIMEOUT
```

---

# 170. HTTP Mapping

Domain/application errors MAY be mapped to HTTP responses at the API boundary.

---

# 171. HTTP Leakage

Core domain behavior SHOULD NOT require knowledge of HTTP status codes.

---

# 172. Exception

Domain exceptions MAY represent exceptional business-rule failures when consistent with the project's error model.

---

# 173. Result Type

Expected business rejection MAY alternatively use explicit result types.

---

# 174. Exception Abuse

Exceptions SHOULD NOT be used as arbitrary control flow for every normal business branch.

---

# 175. Cart Context

The Cart bounded context SHOULD own concepts such as:

```text
CART

CART ITEM

CHECKOUT PREPARATION

CART TOTALS

CART-SPECIFIC LIMITS
```

according to actual business requirements.

---

# 176. Cart Product Snapshot

Cart MAY maintain product snapshots required for:

```text
DISPLAY

CHECKOUT

HISTORICAL CONSISTENCY
```

without becoming authoritative for the Product catalog.

---

# 177. Product Ownership

Products remains authoritative for product/catalog semantics.

---

# 178. Snapshot

A snapshot MUST be clearly distinguished from authoritative product data.

---

# 179. Cart Price

Pricing ownership MUST be explicit.

If Products/Pricing owns price calculation, Cart SHOULD consume the published capability rather than duplicate the pricing algorithm.

---

# 180. Cart Checkout

Checkout SHOULD represent a business operation, not merely a controller forwarding JSON.

---

# 181. Checkout Invariants

Checkout MAY validate:

```text
CART EXISTS

CART IS CHECKOUT-ELIGIBLE

CUSTOMER IS VALID

ITEMS ARE VALID

PRICES ARE RESOLVED

LIMITS ARE SATISFIED
```

according to ownership.

---

# 182. Cart to Orders

Cart MUST NOT directly persist Orders database tables.

---

# 183. Order Creation Contract

Cart SHOULD communicate with Orders through an explicit contract.

---

# 184. Orders Context

Orders SHOULD own:

```text
ORDER IDENTITY

ORDER LIFECYCLE

ORDER ITEMS / SNAPSHOTS

ORDER TOTALS

ORDER BUSINESS STATE
```

according to business requirements.

---

# 185. Order Snapshot

Orders SHOULD preserve historical data required to understand the order even when upstream master data later changes.

---

# 186. Historical Truth

An order should not necessarily change historically because:

```text
PRODUCT NAME CHANGED

CUSTOMER NAME CHANGED

PRICE CHANGED
```

after order creation.

---

# 187. Master vs Transaction Data

Transactional records SHOULD preserve required historical truth independently of mutable master data.

---

# 188. Order Approval

Approval rules SHOULD have explicit ownership.

---

# 189. Approval Rule

If:

```text
ORDER TYPE
+
ORDER STATUS
+
USER PROFILE
+
SEGMENT
```

determine whether an action is permitted, that rule SHOULD exist as an explicit business policy/specification rather than scattered conditionals.

---

# 190. Workflow Context

Workflow SHOULD own process orchestration assigned to Workflow.

---

# 191. Workflow Not Order

Workflow MUST NOT become a second copy of the entire Order aggregate.

---

# 192. Workflow Reference

Workflow SHOULD normally reference Order by identity and maintain only process data it owns.

---

# 193. Customers Context

Customers SHOULD own customer/master-data rules assigned to the Customers domain.

---

# 194. Customer Consumer

Orders and Cart SHOULD NOT directly query Customers database tables.

---

# 195. Consultant Rule

Consultant-responsibility resolution belongs to the context owning that business rule.

---

# 196. Customer Snapshot

Downstream contexts MAY retain customer snapshots required for transactional history.

---

# 197. Products Context

Products SHOULD own product catalog and product-related business capabilities assigned to that context.

---

# 198. Product Consumer

Other contexts SHOULD use:

```text
API

EVENT

SNAPSHOT
```

rather than database coupling.

---

# 199. Cross-Context Validation

A context SHOULD avoid synchronous calls merely to validate data it could safely accept by identifier/eventual consistency.

---

# 200. Synchronous Validation

Synchronous validation IS justified where the current business operation cannot safely proceed without authoritative current information.

---

# 201. Availability Coupling

Every synchronous validation creates runtime availability coupling.

---

# 202. Validation Question

Before adding a synchronous call ask:

```text
MUST I KNOW THIS
RIGHT NOW
TO PROTECT
A BUSINESS INVARIANT?
```

---

# 203. Cached Reference Data

Stable reference data MAY be replicated/cached when ownership and freshness semantics are explicit.

---

# 204. Data Replication

Replicated data MUST have a defined:

```text
OWNER

FRESHNESS MODEL

UPDATE MECHANISM
```

---

# 205. Cross-Service Join

Runtime database joins across service-owned schemas are prohibited.

---

# 206. Composite View

Cross-context read models SHOULD be built through:

```text
API COMPOSITION

EVENT-PROJECTED READ MODEL

DEDICATED QUERY SERVICE
```

depending on requirements.

---

# 207. CQRS

CQRS MAY be used when command and query models have materially different requirements.

---

# 208. CQRS Not Mandatory

DDD does not require CQRS.

---

# 209. CQRS Simple System

Do not create separate command/query infrastructure for trivial CRUD without benefit.

---

# 210. Read Model

A read model MAY denormalize information from multiple contexts for efficient queries.

---

# 211. Read Model Ownership

A read model is not authoritative for source-domain business rules unless explicitly designed as such.

---

# 212. Event Sourcing

Event Sourcing MAY be used only where event history as the source of truth provides significant domain value.

---

# 213. Event Sourcing Not Required

DDD does not require Event Sourcing.

---

# 214. Event Sourcing Cost

Event Sourcing introduces:

```text
EVENT VERSIONING

REPLAY

SNAPSHOTS

PROJECTION MANAGEMENT

OPERATIONAL COMPLEXITY
```

and therefore requires explicit justification.

---

# 215. Aggregate Test

Aggregate business behavior SHOULD be unit tested without infrastructure where practical.

---

# 216. Domain Test

Domain tests SHOULD focus on:

```text
INVARIANTS

STATE TRANSITIONS

BUSINESS DECISIONS

BOUNDARIES
```

---

# 217. Test Language

Tests SHOULD use domain vocabulary.

---

# 218. Example Test Intent

Prefer:

```text
should reject supervisor approval when analyst approval is still pending
```

over:

```text
should return false
```

---

# 219. AssertJ

Applicable Java domain tests SHOULD use meaningful AssertJ descriptions.

Example:

```java
assertThat(order.status())
        .as("should move the order to analyst-approved status")
        .isEqualTo(APPROVED_ANALYST);
```

---

# 220. Domain Test Infrastructure

Pure domain tests SHOULD NOT require Spring context startup when infrastructure is unnecessary.

---

# 221. Fast Domain Tests

Business-rule tests SHOULD remain fast enough to execute continuously.

---

# 222. Application Test

Application-service tests SHOULD verify orchestration and collaborator interaction.

---

# 223. Integration Test

Repository/adapters SHOULD receive integration testing against compatible infrastructure.

---

# 224. Contract Test

Cross-context contracts SHOULD receive contract/compatibility testing where appropriate.

---

# 225. Architecture Test

Architecture boundaries SHOULD be automated where practical.

---

# 226. ArchUnit

ArchUnit MAY enforce rules such as:

```text
DOMAIN MUST NOT DEPEND ON CONTROLLER

CONTROLLER MUST NOT ACCESS REPOSITORY DIRECTLY

ONE CONTEXT MUST NOT IMPORT ANOTHER CONTEXT'S INTERNAL DOMAIN
```

---

# 227. Package Structure

Packages SHOULD communicate architecture/domain intent.

---

# 228. Package by Layer

Pure package-by-layer:

```text
controller/

service/

repository/

entity/
```

MAY become difficult to navigate in large domains.

---

# 229. Package by Feature

Larger systems SHOULD consider feature/domain-oriented organization.

Example:

```text
orders/
    approval/
    creation/
    query/
    integration/
```

with appropriate internal layering.

---

# 230. Context Package

Bounded-context boundaries SHOULD remain visible in source organization.

---

# 231. Internal Visibility

Implementation details SHOULD remain inaccessible to other contexts where language/module mechanisms allow.

---

# 232. Java Modules

JPMS MAY enforce module boundaries where its operational complexity is justified.

---

# 233. Multi-Module Gradle

Gradle modules MAY enforce bounded-context/module boundaries.

---

# 234. Module Explosion

Every package does not require its own Gradle module.

---

# 235. Architecture Dependency

Module boundaries SHOULD correspond to meaningful ownership and dependency constraints.

---

# 236. Domain Model Review

Material business-domain changes SHOULD review:

```text
LANGUAGE

OWNER

INVARIANT

AGGREGATE

TRANSACTION

EVENT

INTEGRATION
```

---

# 237. Domain Discovery

Event Storming MAY be used to discover domain processes.

---

# 238. Event Storming Concepts

Workshops MAY identify:

```text
COMMANDS

DOMAIN EVENTS

ACTORS

POLICIES

AGGREGATES

EXTERNAL SYSTEMS

HOTSPOTS
```

---

# 239. Business Participation

Domain modeling MUST include business/domain knowledge for complex rules.

---

# 240. Developer-Only Domain

Developers SHOULD NOT invent business semantics in isolation when authoritative domain expertise exists.

---

# 241. Ambiguous Rule

Ambiguous business rules MUST be clarified or explicitly documented as assumptions.

---

# 242. Legacy Rule

Legacy behavior SHOULD be treated as evidence, not automatically as correct business policy.

---

# 243. Business Rule Documentation

Critical invariants SHOULD be represented in:

```text
CODE

TESTS

DOCUMENTATION
```

where appropriate.

---

# 244. Comment

A comment SHOULD explain non-obvious business rationale rather than restating code.

---

# 245. Magic Business Number

Business-significant constants SHOULD have names.

---

# 246. Hardcoded Rule

Avoid unexplained:

```java
if (quantity > 37) {
```

when `37` represents a business threshold.

---

# 247. Configurable Rule

Rules SHOULD NOT automatically become configuration.

---

# 248. Configuration Question

A value belongs in configuration when operations/business legitimately need to change it independently of software deployment.

---

# 249. Domain Constant

Stable domain invariants MAY remain code constants.

---

# 250. Rules Engine

A rules engine SHOULD NOT be introduced merely because the application contains business rules.

---

# 251. Rules Engine Justification

Consider a rules engine only when:

```text
RULE VOLUME IS HIGH

RULES CHANGE FREQUENTLY

BUSINESS USERS NEED AUTHORING

COMPOSITION IS COMPLEX
```

and operational costs are accepted.

---

# 252. Database Stored Rule

Business rules SHOULD NOT be scattered across:

```text
JAVA

DATABASE TRIGGERS

FRONTEND

BATCH

INTEGRATION
```

without explicit ownership.

---

# 253. Frontend Validation

Frontend validation improves UX but MUST NOT be the authoritative enforcement of backend business invariants.

---

# 254. Duplicate Validation

Some validation MAY exist at multiple boundaries for UX/security, but authoritative business ownership MUST remain clear.

---

# 255. Domain Security

Authorization decisions tied to business state MAY require domain information.

---

# 256. Authorization Boundary

Authentication/authorization infrastructure and domain eligibility rules MUST remain conceptually distinct.

---

# 257. Example

```text
USER HAS ROLE APPROVER
```

is authorization.

```text
THIS ORDER MAY BE APPROVED
IN ITS CURRENT STATE
```

is a domain rule.

Both may be required.

---

# 258. Temporal Rule

Time-dependent business rules SHOULD use an injectable clock abstraction where deterministic testing is required.

---

# 259. `now()`

Scattered direct system-time calls SHOULD be avoided in complex time-dependent domain logic.

---

# 260. Time Zone

Domain time semantics MUST define the relevant time zone where business rules depend on calendar dates.

---

# 261. Money

Monetary domain logic SHOULD use precise decimal semantics.

---

# 262. Floating Point Money

`float` and `double` SHOULD NOT represent authoritative monetary values.

---

# 263. BigDecimal

Java monetary calculations SHOULD generally use `BigDecimal` or an approved money abstraction.

---

# 264. Scale

Rounding and scale MUST follow explicit business rules.

---

# 265. Quantity

Quantity semantics SHOULD define:

```text
UNIT

PRECISION

MINIMUM

MAXIMUM
```

where relevant.

---

# 266. Domain Collection

Collections exposed by aggregates SHOULD prevent unauthorized external mutation.

---

# 267. Defensive Copy

Immutable/defensive collection exposure SHOULD be used where appropriate.

---

# 268. Aggregate Child Creation

Aggregate children SHOULD normally be created/added through aggregate behavior when invariants apply.

---

# 269. Aggregate Delete

Removing aggregate children SHOULD also respect domain invariants.

---

# 270. Persistence Cascade

JPA cascade configuration MUST reflect aggregate lifecycle rather than convenience alone.

---

# 271. Lazy Loading

Domain behavior SHOULD NOT unknowingly depend on open persistence sessions.

---

# 272. Transaction Boundary

Application-service transaction boundaries SHOULD be explicit.

---

# 273. Remote Call in Transaction

Long remote calls inside database transactions SHOULD be avoided.

---

# 274. Reason

They increase:

```text
LOCK DURATION

CONNECTION OCCUPANCY

FAILURE COUPLING
```

---

# 275. Domain Event Timing

Event publication timing MUST respect transaction consistency.

---

# 276. Before Commit Event

Events representing committed facts MUST NOT escape externally before the corresponding state is durable.

---

# 277. Outbox Alignment

Transactional Outbox SHOULD be used when external event reliability matters.

---

# 278. Event Handler Transaction

Event handlers SHOULD define their own local transaction boundaries.

---

# 279. Retry

Retries MUST respect domain idempotency.

---

# 280. Duplicate Command

Commands that can be retried SHOULD have idempotency semantics where duplicate execution would be harmful.

---

# 281. Business Identity

Idempotency keys SHOULD align with business operation identity when possible.

---

# 282. Domain Observability

Logs/metrics SHOULD use meaningful domain concepts.

---

# 283. Example Metric

Prefer:

```text
orders.approval.rejected
```

with controlled reason dimensions over generic:

```text
service.error
```

for domain monitoring.

---

# 284. Metric Cardinality

Domain identifiers such as order/customer UUIDs MUST NOT become unbounded metric labels.

---

# 285. Audit

Important business transitions SHOULD be auditable where required.

---

# 286. Audit Event

Audit records and domain integration events are related but distinct concerns.

---

# 287. Audit Immutability

Audit records SHOULD preserve:

```text
WHO

WHAT

WHEN

TARGET

RESULT
```

according to audit/privacy standards.

---

# 288. Domain Ownership Matrix

The platform SHOULD maintain an ownership matrix for major concepts.

Example:

| Concept | Authoritative Context | Consumers |
|---|---|---|
| Customer registration | Customers | Cart, Orders |
| Product catalog | Products | Cart, Orders |
| Cart lifecycle | Cart | Orders |
| Order lifecycle | Orders | Workflow, other consumers |
| Workflow process | Workflow | Orders/other participants |

Exact ownership MUST reflect actual business architecture.

---

# 289. Duplicate Source of Truth

A major concept MUST NOT have two independent authoritative owners.

---

# 290. Ownership Transfer

Changing authoritative ownership requires an explicit migration strategy.

---

# 291. Domain Boundary Review Checklist

```text
[ ] What business capability does this context own?

[ ] What language is authoritative inside it?

[ ] Which data does it own?

[ ] Which invariants does it protect?

[ ] Which aggregates exist?

[ ] What requires immediate consistency?

[ ] What can be eventually consistent?

[ ] Which contexts does it depend on?

[ ] Are those dependencies synchronous?

[ ] Are synchronous dependencies truly necessary?

[ ] What events does it publish?

[ ] What events does it consume?

[ ] Are external models translated?

[ ] Is an Anti-Corruption Layer required?

[ ] Are transactional boundaries clear?

[ ] Is any database ownership shared?

[ ] Is there any duplicate source of truth?

[ ] Can the context deploy independently?

[ ] Are business rules tested without unnecessary infrastructure?
```

---

# 292. Aggregate Review Checklist

```text
[ ] What is the Aggregate Root?

[ ] What identity defines it?

[ ] What invariants must it protect?

[ ] Which objects must change atomically?

[ ] Is the aggregate larger than necessary?

[ ] Does it reference other aggregates by ID?

[ ] Can external code mutate internal children directly?

[ ] Are lifecycle transitions explicit?

[ ] Are public setters bypassing rules?

[ ] Does the repository operate on the root?

[ ] Are collections protected?

[ ] Does JPA cascade reflect lifecycle ownership?

[ ] Can domain behavior be unit tested without Spring?

[ ] Are concurrency conflicts understood?
```

---

# 293. Domain Event Review Checklist

```text
[ ] Does the event represent something that happened?

[ ] Is the name in past tense?

[ ] Who owns its meaning?

[ ] Is it internal or integration-facing?

[ ] Does it contain only required data?

[ ] Does it avoid JPA entity serialization?

[ ] Does it expose unnecessary PII?

[ ] Does it have stable event identity where required?

[ ] Is correlation supported?

[ ] Is event versioning defined?

[ ] Is publication transactionally reliable?

[ ] Can consumers handle duplicates?

[ ] Can consumers tolerate expected ordering semantics?

[ ] Is the consumer translating into its own model?
```

---

# 294. DDD Fitness Functions

Stable architecture rules SHOULD be automated where practical.

Examples:

```text
[ ] Domain packages do not depend on controllers

[ ] Domain packages do not depend on HTTP types

[ ] Controllers do not access repositories directly

[ ] One context does not import another context's internal entities

[ ] Cross-context database access is prohibited

[ ] Integration events do not expose JPA entities

[ ] Aggregate children are not independently persisted through public repositories

[ ] Forbidden circular context dependencies fail CI

[ ] Critical business rules have tests

[ ] Architecture rules execute in CI
```

---

# 295. Enterprise Domain Modeling Gate

A domain change is not considered compliant when applicable conditions include:

```text
[ ] Database tables define service boundaries without business analysis

[ ] Two contexts independently own the same business state

[ ] One service directly updates another service's tables

[ ] External DTO became an internal domain entity without translation

[ ] Business invariant exists only in controller validation

[ ] Public setter bypasses aggregate lifecycle rules

[ ] Aggregate contains unrelated objects only because they have foreign keys

[ ] Aggregate loads an unnecessarily large graph

[ ] Child entity has an independent repository despite aggregate ownership

[ ] Business rule is duplicated across multiple services/classes

[ ] Domain model is purely anemic despite significant behavior

[ ] Domain service is a generic dumping ground

[ ] Shared DTO library couples all services

[ ] Integration event serializes persistence entities

[ ] External event is published before local transaction durability

[ ] Consumer assumes exactly-once delivery

[ ] Distributed transaction is introduced without explicit justification

[ ] Microservice extraction leaves uncontrolled shared database ownership

[ ] Cross-context synchronous validation is added without consistency justification

[ ] DDD pattern is introduced without actual domain value
```

---

# 296. Anti-Patterns

The following are prohibited or strongly discouraged:

- database-driven domain boundaries
- service-per-table
- entity-per-table thinking as domain design
- shared JPA entities across microservices
- shared database ownership
- giant aggregates
- aggregate-per-database-schema
- public setters for meaningful lifecycle transitions
- business logic only in controllers
- business logic only in giant application services
- anemic model for complex behavioral domains
- generic `DomainService`
- repository for every table
- DTOs reused as entities
- external models leaking into local domains
- common JAR containing every service contract
- synchronous validation for every cross-service reference
- distributed ACID transactions by default
- assuming exactly-once messaging
- serializing JPA entities to queues
- treating domain events and integration events as automatically identical
- CQRS everywhere
- Event Sourcing everywhere
- rules engine for simple `if` statements
- microservices defined by technical layers
- DDD terminology without domain modeling

---

# 297. Positive Consequences

The decision provides:

- explicit business ownership
- stronger invariant protection
- clearer transactional boundaries
- reduced cross-service coupling
- improved domain vocabulary
- safer integration contracts
- improved testability
- better modernization boundaries
- reduced business-rule duplication
- clearer event semantics
- safer eventual consistency
- stronger microservice autonomy

---

# 298. Negative Consequences

The decision introduces:

- additional modeling effort
- explicit context translation
- more domain-specific classes
- event-contract governance
- aggregate-design decisions
- eventual-consistency complexity
- increased need for business participation

These costs are accepted for domains whose complexity justifies richer modeling.

---

# 299. Neutral Consequences

The decision also means:

- some services may remain CRUD-oriented
- not every entity requires an aggregate hierarchy
- not every primitive requires a Value Object
- not every operation requires a Domain Service
- DDD does not require microservices
- DDD does not require CQRS
- DDD does not require Event Sourcing
- persistence and domain models may sometimes remain the same
- eventual consistency is not automatically superior to immediate consistency
- strategic modeling is generally more important than pattern count

---

# 300. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Wrong bounded context | High | Medium | Domain discovery/context mapping |
| Giant aggregate | High | Medium | Invariant-based boundaries |
| Anemic model | Medium/High | Medium | Behavior ownership |
| Pattern overengineering | Medium | Medium | Complexity-driven adoption |
| Shared database coupling | Critical | Medium | Explicit data ownership |
| Event inconsistency | Critical | Medium | Transactional Outbox |
| Duplicate events | High | High | Idempotent consumers |
| Event contract break | High | Medium | Versioning/contract tests |
| Synchronous coupling | High | High | Eventual consistency/replication |
| Duplicate business rules | High | Medium | Authoritative policies |

---

# 301. Implementation Guidance

The following rules are mandatory:

1. Complex domains must use explicit business language.
2. Bounded contexts must have clear business and data ownership.
3. Database tables must not independently define context/service boundaries.
4. Microservices must not share uncontrolled table ownership.
5. External models must be translated where semantic differences exist.
6. Shared domain/DTO libraries across services must be exceptional.
7. Entities must protect identity/lifecycle semantics.
8. Value Objects should be immutable and valid by construction where appropriate.
9. Aggregates must represent transactional consistency boundaries.
10. Aggregate Roots must protect aggregate invariants.
11. Aggregates should remain as small as business consistency permits.
12. Cross-aggregate references should normally use identity.
13. Business invariants must not exist solely in controllers.
14. Transport, application and domain validation must remain conceptually distinct.
15. Complex domain models should avoid unrestricted public state mutation.
16. Domain Services must represent genuine domain behavior.
17. Application Services should orchestrate use cases rather than accumulate all business decisions.
18. Repositories should normally operate on Aggregate Roots.
19. Factories/Specifications/Policies should be introduced only when domain complexity justifies them.
20. Domain lifecycle transitions must be explicit.
21. Every important business state must have one authoritative owner.
22. Domain Events should describe completed domain facts.
23. Integration Events must be treated as independently governed contracts.
24. Persistence entities must not be serialized directly as integration events.
25. Cross-service consistency should generally use local transactions and eventual consistency.
26. Reliable event publication should use Transactional Outbox where business reliability requires it.
27. Event consumers must support duplicate delivery/idempotency.
28. Sagas may coordinate multi-context business processes where necessary.
29. Cart, Orders, Customers, Products and Workflow must not bypass each other's service/data boundaries.
30. Historical transaction snapshots must be distinguished from authoritative master data.
31. Synchronous cross-context validation must be justified by immediate business consistency requirements.
32. Critical domain rules must have deterministic automated tests.
33. Architecture boundaries should be enforced with ArchUnit or equivalent fitness functions where practical.
34. DDD patterns must not be introduced merely for architectural appearance.

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
- PostgreSQL
- Flyway
- OpenAPI
- SQS/SQS where applicable
- Transactional Outbox
- contract tests
- architecture fitness functions
- SonarQube
- SAST
- CI/CD quality gates
- Event Storming/context mapping where applicable

---

# 303. Success Criteria

The decision is successful when:

- major business concepts have explicit owners
- business terminology is consistent in code and documentation
- service boundaries align with business capabilities
- shared database coupling decreases
- important invariants have one authoritative implementation
- domain transitions cannot be bypassed accidentally
- aggregates remain focused
- cross-context contracts are explicit
- event consumers tolerate duplicate delivery
- historical transaction data remains stable despite master-data changes
- synchronous service dependencies exist only where justified
- architecture tests prevent context-boundary erosion
- domain tests execute quickly without unnecessary framework infrastructure

---

# 304. Alternatives Rejected

## 304.1 Database-First Domain Modeling

Rejected because relational structure does not necessarily represent business boundaries or invariants.

---

## 304.2 CRUD Service for Every Domain

Rejected because complex business domains require explicit behavior and lifecycle modeling.

---

## 304.3 Rich DDD Everywhere

Rejected because simple reference-data/CRUD domains do not justify unnecessary tactical complexity.

---

## 304.4 Shared Entity Library

Rejected because it tightly couples service internals and persistence models.

---

## 304.5 One Distributed Transaction

Rejected as the default cross-service consistency model because it reduces service autonomy and resilience.

---

## 304.6 Event Sourcing Everywhere

Rejected because operational and modeling complexity is substantial and justified only for specific domains.

---

## 304.7 CQRS Everywhere

Rejected because command/query separation should solve concrete requirements rather than become mandatory ceremony.

---

## 304.8 Business Rules in Controllers

Rejected because transport boundaries are not authoritative domain models.

---

# 305. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-083: Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard
- ADR-084: Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard
- ADR-086: Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard
- ADR-087: Enterprise Technical Debt, Legacy Modernization, Refactoring and Continuous Architecture Governance Standard

---

# 306. References

- Eric Evans — Domain-Driven Design
- Vaughn Vernon — Implementing Domain-Driven Design
- Vaughn Vernon — Domain-Driven Design Distilled
- Martin Fowler — Domain-Driven Design references
- Martin Fowler — Bounded Context
- Martin Fowler — CQRS
- Martin Fowler — Event Sourcing
- Chris Richardson — Microservices Patterns
- Sam Newman — Building Microservices
- Sam Newman — Monolith to Microservices
- Enterprise Integration Patterns
- Java 21 Documentation
- Spring Boot Documentation
- ArchUnit Documentation
- Testcontainers Documentation

---

# 307. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise DDD and domain modeling governance baseline |

---

# 308. Decision Summary

Strategic modeling becomes:

```text
BUSINESS
   |
   v
CAPABILITIES
   |
   v
BOUNDED CONTEXTS
   |
   v
OWNERSHIP
   |
   v
CONTEXT MAP
```

instead of:

```text
DATABASE
   |
   v
TABLES
   |
   v
SERVICES
```

Domain behavior becomes:

```text
COMMAND
   |
   v
APPLICATION SERVICE
   |
   v
AGGREGATE
   |
   +--> VALIDATE INVARIANT
   |
   +--> CHANGE STATE
   |
   +--> RAISE DOMAIN EVENT
   |
   v
REPOSITORY
```

An aggregate becomes:

```text
AGGREGATE ROOT
      |
      +--> ENTITY
      |
      +--> VALUE OBJECT
      |
      +--> ENTITY
      |
      v
INVARIANTS
```

not:

```text
ROOT ENTITY
      |
      +--> EVERYTHING WITH
           A FOREIGN KEY
```

Cross-context communication becomes:

```text
CONTEXT A
    |
    v
PUBLIC CONTRACT
    |
    v
ANTI-CORRUPTION LAYER
    |
    v
CONTEXT B MODEL
```

rather than:

```text
CONTEXT A ENTITY
      |
      v
SHARED JAR
      |
      v
CONTEXT B
```

Domain and integration events become:

```text
AGGREGATE
    |
    v
DOMAIN EVENT
    |
    v
APPLICATION MAPPING
    |
    v
INTEGRATION EVENT
    |
    v
OUTBOX
    |
    v
BROKER
```

Cross-service consistency becomes:

```text
LOCAL TRANSACTION A
        |
        v
EVENT
        |
        v
LOCAL TRANSACTION B
        |
        v
EVENTUAL CONSISTENCY
```

instead of:

```text
SERVICE A
    |
    +------ DISTRIBUTED DB TRANSACTION ------+
    |                                         |
SERVICE B                                  SERVICE C
```

For the platform:

```text
CUSTOMERS
   |
   | authoritative customer data
   v
CART
   |
   | checkout contract
   v
ORDERS
   |
   | order lifecycle / events
   v
WORKFLOW
```

while:

```text
PRODUCTS
   |
   +--> product/pricing capability --> CART
   |
   +--> product information -------> ORDERS
```

with each context retaining ownership of its own rules.

The complete domain-modeling equation is:

```text
UBIQUITOUS LANGUAGE
       +
BOUNDED CONTEXTS
       +
EXPLICIT OWNERSHIP
       +
SMALL AGGREGATES
       +
PROTECTED INVARIANTS
       +
BEHAVIOR-RICH DOMAIN MODELING
       +
CLEAR APPLICATION ORCHESTRATION
       +
DOMAIN EVENTS
       +
VERSIONED INTEGRATION EVENTS
       +
ANTI-CORRUPTION LAYERS
       +
LOCAL TRANSACTIONS
       +
EVENTUAL CONSISTENCY
       +
IDEMPOTENT CONSUMERS
       +
ARCHITECTURE FITNESS FUNCTIONS
       =
BUSINESS-ALIGNED ENTERPRISE ARCHITECTURE
```

The governing principle is:

```text
Start with the business.

Not the controller.

Not the repository.

Not the table.

Ask:

What capability
are we implementing?

Who owns it?

What language
does the business use?

What must always
remain true?

That is the invariant.

What must change
atomically?

That helps define
the aggregate.

Keep aggregates small.

Do not load
the entire business universe
into one transaction.

Protect lifecycle changes.

Do not let
an arbitrary setter
approve an order.

Name the action.

Validate the rule.

Preserve the invariant.

Keep transport validation
separate from business rules.

@NotNull
does not model
order approval.

Keep application services
focused on orchestration.

Let the domain decide
what the domain owns.

Do not create
DomainService
as a new name
for GodService.

Do not share
JPA entities
between services.

Do not create
one common DTO JAR
that makes every service
compile against
every other service.

Publish contracts.

Translate them.

Protect local language.

Know the source of truth.

Customers owns
customer semantics.

Products owns
product semantics.

Cart owns
cart semantics.

Orders owns
order semantics.

Workflow owns
workflow semantics.

Snapshots are allowed.

Duplicate ownership is not.

Use events
for facts that happened.

Commands request.

Events describe.

ApproveOrder
is a command.

OrderApproved
is an event.

Do not publish
a database entity
to the broker.

Publish a contract.

Version it.

Assume duplicates.

Design idempotency.

Do not assume
the network gives you
exactly once.

Keep transactions local.

Use eventual consistency
where boundaries require it.

Use Outbox
when state and event
must not diverge.

Use Sagas
for real distributed
business processes,

not because
the pattern name
sounds architectural.

Do not adopt CQRS
because DDD was mentioned.

Do not adopt
Event Sourcing
because events exist.

Do not create
Value Objects
for every String.

Do not create
patterns for patterns.

Model complexity
where complexity exists.

Keep simple domains simple.

Make complex domains explicit.

And remember:

a good domain model
does not merely
store business data.

It protects
business truth.
```
