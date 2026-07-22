# Domain Services

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Domain Services |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Domain Services of the Enterprise Order Platform.

It establishes:

- Responsibilities
- Design principles
- Service catalog
- Business policies
- Specifications
- Aggregate collaboration
- Domain algorithms
- Validation ownership
- Java implementation guidelines

Domain Services encapsulate business behavior that does not naturally belong to a single Entity or Value Object.

---

# 2. What is a Domain Service?

A Domain Service represents domain behavior that:

- belongs to the domain
- uses ubiquitous language
- has no independent identity
- is usually stateless
- coordinates business rules
- protects business invariants

A Domain Service is **not**:

- an Application Service
- a Repository
- a REST Controller
- an Infrastructure Adapter
- a Kafka Producer
- a JPA Service

---

# 3. Design Principles

Every Domain Service should be:

- stateless
- cohesive
- deterministic
- side-effect free whenever possible
- framework independent
- reusable

---

# 4. Decision Matrix

Business behavior should belong to:

| Behavior | Owner |
|----------|-------|
| Entity internal state | Entity |
| Mathematical calculation | Value Object |
| Cross-Entity business rule | Domain Service |
| External orchestration | Application Service |
| Database access | Repository |
| Messaging | Infrastructure |

---

# 5. Initial Domain Service Catalog

The Order domain initially defines:

| Service | Responsibility |
|----------|----------------|
| OrderPricingPolicy | Validate pricing |
| OrderSubmissionPolicy | Validate submission |
| OrderCancellationPolicy | Validate cancellation |
| OrderApprovalPolicy | Validate approval |
| InventoryReservationPolicy | Reservation rules |
| OrderCompletionPolicy | Completion rules |
| OrderConsistencyValidator | Cross-aggregate consistency |
| OrderNumberGeneratorPolicy | Number generation |
| OrderSnapshotFactory | Snapshot creation |

---

# 6. OrderPricingPolicy

Purpose

Validate accepted pricing before it becomes part of the Order.

Responsibilities

- currency consistency
- totals validation
- tax validation
- discount validation
- freight validation
- pricing version validation

Example

```java
public interface OrderPricingPolicy {

    void validate(
            Order order,
            PricingSnapshot pricing
    );

}
```

The policy never calculates prices.

Pricing calculation belongs to the Pricing Context.

---

# 7. OrderSubmissionPolicy

Purpose

Determine whether an Order may be submitted.

Example validations

- at least one item
- pricing exists
- pricing current
- customer exists
- snapshots complete

Returns

```
allowed

or

business exception
```

---

# 8. OrderCancellationPolicy

Purpose

Determine whether cancellation is allowed.

Possible inputs

```
Order

Current Status

Inventory State

Payment State

Shipment State
```

Possible outputs

```
Allowed

Denied

Compensations Required
```

---

# 9. OrderApprovalPolicy

Purpose

Validate approval outcome.

Checks

- workflow id
- approver
- approval state
- duplicate approval
- review request
- rejection

---

# 10. InventoryReservationPolicy

Purpose

Validate reservation result.

Possible rules

- reservation expiration
- partial reservation
- inventory consistency

---

# 11. OrderCompletionPolicy

Purpose

Determine whether an Order may become COMPLETED.

Possible checks

- fulfillment complete
- payment complete
- inventory consistent

---

# 12. OrderConsistencyValidator

Validates consistency involving multiple business concepts.

Examples

- pricing vs items

- snapshots vs customer

- totals vs item totals

- shipment eligibility

---

# 13. OrderNumberGeneratorPolicy

Responsible for generating Order Numbers.

Possible implementations

Sequential

Date-based

Region-based

External ERP

Domain depends only on abstraction.

---

# 14. OrderSnapshotFactory

Creates immutable snapshots.

Produces

```
CustomerSnapshot

ProductSnapshot

PricingSnapshot
```

Snapshots must always be complete.

---

# 15. Specification Pattern

Complex business rules should use Specifications.

Example

```
OrderIsEditableSpecification

OrderCanBeCancelledSpecification

OrderCanBeSubmittedSpecification

OrderRequiresApprovalSpecification
```

---

# 16. OrderCanBeSubmittedSpecification

Example

```
Order

↓

Items exist

↓

Pricing valid

↓

Snapshots complete

↓

Allowed
```

---

# 17. OrderRequiresApprovalSpecification

Determines

```
Approval required?

YES

NO
```

Possible inputs

- customer
- amount
- region
- segment
- order type

---

# 18. OrderCanBeCancelledSpecification

Evaluates

```
Current status

↓

Inventory

↓

Payment

↓

Shipment

↓

Allowed?
```

---

# 19. Policy vs Specification

Specification

```
returns boolean
```

Policy

```
contains business decision
```

Example

```
Specification

↓

true

↓

Policy

↓

throw exception

or

continue
```

---

# 20. Collaboration

Domain Services collaborate with

- Entities

- Value Objects

Never with

- HTTP

- Kafka

- SQL

---

# 21. Domain Algorithms

Examples

Pricing consistency

Allocation

Approval requirement

Shipment eligibility

Inventory allocation

---

# 22. Side Effects

Prefer

```
pure functions
```

Avoid

```
database

network

logging
```

inside Domain Services.

---

# 23. Java Design

Prefer

```java
public interface OrderPricingPolicy {

}
```

Implementation

```java
final class DefaultOrderPricingPolicy
```

---

# 24. Dependency Rules

Allowed

```
Entity

↓

Domain Service

↓

Value Object
```

Forbidden

```
Domain Service

↓

Repository

↓

Kafka

↓

REST

↓

Spring
```

---

# 25. Testing

Every Domain Service should test

- positive scenarios

- negative scenarios

- edge cases

- invalid inputs

- business invariants

without Spring.

---

# 26. Decision Summary

Domain Services:

- encapsulate cross-entity business logic
- are stateless
- are framework independent
- use ubiquitous language
- do not access infrastructure
- coordinate business rules only

---

# 27. Next Documentation Step

Next document

```
docs/application/application-services.md
```

This document will define:

- Use Cases
- Command Handlers
- Query Handlers
- Transactions
- Ports
- Orchestration
- Integration boundaries
