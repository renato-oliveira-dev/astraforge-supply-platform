# Package Structure

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Package Structure |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the package organization adopted by the AstraForge Supply Platform.

The package structure is based on:

- Domain-Driven Design
- Clean Architecture
- Hexagonal Architecture
- Vertical Slice Architecture

The objective is to maximize cohesion while minimizing coupling between modules.

---

# 2. Architectural Principles

The project is organized by **business capability first**, not by technical stereotype.

Good:

```
order
customer
inventory
payment
approval
```

Avoid:

```
controller
service
entity
repository
util
dto
```

The package structure should communicate the business domain.

---

# 3. High-Level Structure

```
io.astraforge.supplyplatform

├── application
├── domain
├── infrastructure
├── configuration
├── shared
```

---

# 4. Domain Layer

```
domain
│
├── order
├── customer
├── inventory
├── payment
├── approval
├── shared
```

Each bounded context owns its own model.

---

# 5. Order Module

```
order
│
├── aggregate
├── entity
├── valueobject
├── event
├── service
├── policy
├── specification
├── repository
├── exception
```

Only business concepts belong here.

---

# 6. Application Layer

```
application
│
├── order
│   ├── command
│   ├── query
│   ├── usecase
│   ├── handler
│   ├── mapper
│   ├── port
│   └── validator
│
├── inventory
├── payment
├── approval
```

The application layer orchestrates business use cases.

---

# 7. Infrastructure Layer

```
infrastructure

├── persistence
├── messaging
├── web
├── security
├── cache
├── integration
├── scheduler
├── configuration
```

Infrastructure implements application ports.

---

# 8. Persistence

```
persistence

├── entity
├── repository
├── mapper
├── specification
├── projection
├── converter
```

Persistence concerns never leak into the domain.

---

# 9. REST API

```
web

├── controller
├── request
├── response
├── advice
├── mapper
```

Controllers remain thin.

---

# 10. Messaging

```
messaging

├── producer
├── consumer
├── payload
├── mapper
├── configuration
```

Messaging adapters should not contain business rules.

---

# 11. External Integrations

```
integration

├── inventory
├── payment
├── customer
├── notification
```

Each external dependency owns its own package.

---

# 12. Configuration

```
configuration

├── security
├── database
├── sqs
├── redis
├── web
├── observability
├── jackson
```

Avoid generic configuration classes.

---

# 13. Shared Package

The `shared` package contains only truly cross-cutting concerns.

Examples:

- Result
- PageResponse
- CorrelationId
- ClockProvider
- DomainConstants

Never place business logic in `shared`.

---

# 14. Naming Rules

Package names must:

- be lowercase
- be singular where appropriate
- represent business concepts
- avoid abbreviations

Good:

```
inventory
approval
customer
payment
```

Avoid:

```
svc
util
misc
common
helper
```

---

# 15. Dependency Rules

Allowed dependencies:

```
application
        ↓
domain

infrastructure
        ↓
application

configuration
        ↓
infrastructure
```

The domain must not depend on Spring, JPA, HTTP or messaging frameworks.

---

# 16. Visibility Rules

Prefer package-private visibility for internal collaborators.

Expose only intentional APIs as `public`.

---

# 17. Feature Isolation

Each business capability owns:

- commands
- queries
- handlers
- validators
- DTOs
- mappers

Avoid global folders shared by unrelated features.

---

# 18. Mapping Responsibilities

Mapping belongs at architectural boundaries.

Examples:

- REST ↔ Command
- Entity ↔ Aggregate
- Event ↔ DTO

Business objects should not map themselves.

---

# 19. Package Size

Large packages indicate missing modularization.

Recommended limits:

- ≤ 20 classes per package
- ≤ 10 direct dependencies per class
- cohesive responsibilities

---

# 20. Cyclic Dependencies

Package cycles are prohibited.

Architecture tests must verify:

- no cyclic dependencies
- correct layer access
- bounded context isolation

---

# 21. Package Documentation

Every significant package must include:

```
package-info.java
```

The documentation should explain:

- purpose
- responsibilities
- allowed dependencies
- architectural constraints

---

# 22. Example Structure

```
order

├── aggregate
│     Order.java
│
├── valueobject
│     Money.java
│     OrderId.java
│
├── event
│     OrderCreatedEvent.java
│
├── policy
│     ApprovalPolicy.java
│
├── repository
│     OrderRepository.java
│
└── exception
      OrderNotFoundException.java
```

---

# 23. Architecture Validation

The project should enforce package rules using ArchUnit.

Example rules include:

- Domain must not depend on Spring.
- Controllers must not access repositories directly.
- Infrastructure must not be referenced by the domain.
- Messaging adapters must invoke application use cases.
- Configuration packages must not contain business logic.

---

# 24. Refactoring Guidelines

When a package grows excessively:

1. Identify unrelated responsibilities.
2. Extract cohesive subpackages.
3. Preserve public contracts.
4. Update architecture tests.
5. Update `package-info.java`.

Avoid introducing generic "utils" packages as a shortcut.

---

# 25. Anti-Patterns

The following structures are prohibited:

```
util
helper
misc
common
manager
base
```

when they become catch-all packages for unrelated functionality.

Likewise, avoid:

- giant service packages
- generic DTO folders
- repository packages shared across bounded contexts
- infrastructure classes referenced directly by the domain

---

# 26. Decision Summary

The package organization prioritizes:

- business capabilities over technical layers
- high cohesion
- low coupling
- explicit boundaries
- architecture enforcement
- long-term maintainability
