# Ports

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Ports |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines every Port used by the Enterprise Order Platform.

It establishes:

- Input Ports
- Output Ports
- Repository Ports
- Integration Ports
- Dependency inversion
- Ownership
- Port contracts
- Adapter responsibilities

Ports isolate the Domain and Application layers from Infrastructure.

---

# 2. Hexagonal Architecture Overview

```
                    REST API

                       │

                REST Controller

                       │

                Input Port (Use Case)

                       │

              Application Service

      ┌────────────────┼─────────────────┐
      │                │                 │
      │                │                 │
Repository Port   Pricing Port    Event Publisher Port
      │                │                 │
      ▼                ▼                 ▼
Persistence      External API      Messaging Adapter
```

---

# 3. Port Categories

The platform defines four categories:

- Input Ports
- Output Ports
- Repository Ports
- Infrastructure Ports

---

# 4. Input Ports

Input Ports expose application use cases.

Examples:

```
CreateOrderUseCase

SubmitOrderUseCase

CancelOrderUseCase

ApproveOrderUseCase

FindOrderQuery
```

They are implemented by Application Services.

---

# 5. Input Port Example

```java
public interface SubmitOrderUseCase {

    void execute(
            SubmitOrderCommand command
    );

}
```

The Controller depends on this interface.

---

# 6. Output Ports

Output Ports define services required by the Application Layer.

Examples

```
PricingPort

CustomerPort

InventoryPort

NotificationPort

ClockPort

IdentityProviderPort
```

---

# 7. Repository Ports

Repositories are Output Ports.

Example

```java
public interface OrderRepository {

    Optional<Order> findById(
            OrderId id
    );

    void save(
            Order order
    );

}
```

The Domain knows nothing about JPA.

---

# 8. Pricing Port

Purpose

Obtain accepted pricing.

```java
PricingSnapshot calculate(
        PricingRequest request
);
```

Implementation may use

- REST
- gRPC
- Messaging

Application never knows.

---

# 9. Customer Port

Purpose

Retrieve Customer information.

Produces

```
CustomerSnapshot
```

Never Customer Entity.

---

# 10. Inventory Port

Purpose

Send inventory requests.

Operations

```
reserve()

release()
```

No business logic.

---

# 11. Notification Port

Purpose

Notify external users.

Possible channels

- email
- sms
- push
- webhook

---

# 12. Clock Port

Purpose

Provide time.

Example

```java
Instant now();
```

Improves testing.

---

# 13. Identity Port

Purpose

Provide authenticated actor.

Returns

```
UserId
```

instead of framework security objects.

---

# 14. Event Publisher Port

Purpose

Persist Integration Events into the Outbox.

Example

```java
publish(
        IntegrationEvent event
);
```

Implementation decides how to fulfill the port using the approved messaging architecture. For reliable state-plus-event publication the normal implementation is:

- Transactional Outbox
- Amazon SQS infrastructure adapter

Another broker requires an explicit accepted ADR.

---

# 15. File Storage Port

Purpose

Store documents.

Operations

```
upload()

download()

delete()
```

---

# 16. Audit Port

Purpose

Register audit facts.

Application depends only on abstraction.

---

# 17. Search Port

Used by query services.

Possible implementation

ElasticSearch

OpenSearch

Database View

---

# 18. Dependency Direction

Allowed

```
Controller

↓

Input Port

↓

Application

↓

Output Port

↓

Adapter
```

Forbidden

```
Domain

↓

REST

↓

Amazon SQS

↓

JPA

↓

Spring
```

---

# 19. Adapter Responsibilities

Adapters translate technologies.

Example

```
Pricing REST

↓

Pricing Adapter

↓

Pricing Port
```

No business logic.

---

# 20. Port Ownership

| Port | Owner |
|------|-------|
| OrderRepository | Application |
| PricingPort | Application |
| CustomerPort | Application |
| InventoryPort | Application |
| NotificationPort | Application |

Infrastructure only implements them.

---

# 21. Testing

Ports are mocked.

Application Services are tested without infrastructure.

---

# 22. Architecture Rules

Ports:

- are interfaces
- belong to Application
- contain no implementation
- expose business concepts
- hide infrastructure

---

# 23. Decision Summary

The platform adopts:

- Input Ports for use cases
- Output Ports for integrations
- Repository Ports for persistence
- Dependency inversion
- Framework-independent contracts

---

# 24. Next Documentation Step

Next document:

```
docs/application/repositories.md
```

It defines repository contracts, aggregate persistence rules and transaction consistency.
