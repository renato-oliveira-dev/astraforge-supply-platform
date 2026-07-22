# Application Services

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Application Services |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Application Layer of the Enterprise Order Platform.

It establishes:

- Use Cases
- Application Services
- Command Handlers
- Query Handlers
- Transaction boundaries
- Domain orchestration
- Port usage
- Event publication flow
- Integration boundaries

Application Services coordinate domain behavior.

They do not implement business rules.

---

# 2. Layer Responsibilities

```
REST Controller

↓

Application Service

↓

Domain Model

↓

Repository

↓

Infrastructure
```

The Application Layer orchestrates domain execution.

---

# 3. Responsibilities

Application Services are responsible for:

- loading aggregates
- invoking domain behavior
- coordinating repositories
- opening transactions
- mapping DTOs
- collecting Domain Events
- creating Outbox records
- committing work

Application Services are NOT responsible for:

- pricing calculations
- business validation
- aggregate invariants
- persistence implementation
- messaging implementation

---

# 4. Use Case Catalog

| Command | Service |
|---------|---------|
| CreateOrder | CreateOrderService |
| AddItem | AddOrderItemService |
| RemoveItem | RemoveOrderItemService |
| UpdateQuantity | UpdateOrderItemQuantityService |
| PriceOrder | PriceOrderService |
| SubmitOrder | SubmitOrderService |
| ApproveOrder | ApproveOrderService |
| RejectOrder | RejectOrderService |
| RequestReview | RequestReviewService |
| CancelOrder | CancelOrderService |
| CompleteOrder | CompleteOrderService |

---

# 5. Query Catalog

| Query | Service |
|--------|---------|
| FindOrder | FindOrderQueryService |
| SearchOrders | SearchOrdersQueryService |
| OrderHistory | OrderHistoryQueryService |
| CustomerOrders | CustomerOrdersQueryService |

Queries never modify state.

---

# 6. Command Flow

Typical execution:

```
Receive Command

↓

Load Aggregate

↓

Execute Domain Behavior

↓

Save Aggregate

↓

Collect Domain Events

↓

Create Outbox Records

↓

Commit Transaction
```

---

# 7. Example: Submit Order

```
SubmitOrderCommand

↓

SubmitOrderService

↓

OrderRepository.load()

↓

Order.submit()

↓

OrderRepository.save()

↓

DomainEvents

↓

Outbox

↓

Commit
```

---

# 8. Transaction Boundary

One Application Service equals one business transaction.

```
@Transactional

SubmitOrderService.execute()
```

The Domain Layer must not manage transactions.

---

# 9. Aggregate Loading

Application Services load aggregates using repositories.

Example

```java
Order order =
        repository.find(orderId);
```

Never load partial aggregates when executing commands.

---

# 10. Aggregate Saving

After successful execution:

```java
repository.save(order);
```

Only the aggregate root is persisted.

---

# 11. Domain Events

Application Services collect Domain Events.

Example

```java
List<DomainEvent> events =
        order.domainEvents();
```

The aggregate records events.

The Application Layer processes them.

---

# 12. Outbox Creation

For each Integration Event:

```
Domain Event

↓

Mapper

↓

Integration Event

↓

Outbox Record
```

No Kafka publishing occurs here.

---

# 13. Exception Handling

Business exceptions propagate unchanged.

Examples:

- OrderAlreadySubmittedException
- InvalidOrderTransitionException
- OrderWithoutItemsException

Infrastructure exceptions are translated outside the Domain Layer.

---

# 14. DTO Mapping

Controllers exchange DTOs.

Application Services translate them into domain objects.

```
REST DTO

↓

Command

↓

Application Service

↓

Domain
```

---

# 15. Ports

Application Services depend only on abstractions.

Examples

```
OrderRepository

PricingPort

CustomerPort

InventoryPort

NotificationPort

Clock
```

---

# 16. External Integrations

Application Services call external systems only through Ports.

Never:

```
WebClient

Feign

HTTP

inside Domain
```

---

# 17. Validation

Validation occurs at different levels.

Controller

- syntax

Application

- command completeness

Domain

- business rules

---

# 18. Command Example

```java
public record SubmitOrderCommand(

        OrderId orderId,

        UserId submittedBy,

        CorrelationId correlationId

) {
}
```

Commands are immutable.

---

# 19. Query Example

```java
public record FindOrderQuery(

        OrderId orderId

) {
}
```

---

# 20. Service Design

Preferred

```java
public interface SubmitOrderUseCase {

    void execute(
            SubmitOrderCommand command
    );

}
```

Implementation

```java
class SubmitOrderService
```

---

# 21. Dependency Rules

Application Layer may depend on:

- Domain
- Ports

Application Layer must not depend on:

- Controllers
- Kafka
- Database
- JPA implementation
- REST clients

---

# 22. Idempotency

Commands that may be retried should support idempotency.

Examples:

- SubmitOrder
- CancelOrder
- ApproveOrder

---

# 23. Security

Authenticated user information becomes domain objects.

Example

```
AuthenticatedUser

↓

UserId

↓

Domain
```

Never expose security framework classes.

---

# 24. Logging

Application Services log:

- start
- finish
- elapsed time
- failures

Never log:

- passwords
- tokens
- sensitive payloads

---

# 25. Testing

Every Application Service should test:

- successful execution
- repository interactions
- domain invocation
- outbox creation
- transaction behavior
- exception propagation

Infrastructure should be mocked.

---

# 26. Architecture Rules

Application Services:

- orchestrate only
- contain no business rules
- remain framework-light
- depend on Ports
- invoke aggregates
- never bypass aggregate behavior

---

# 27. Decision Summary

The Application Layer:

- coordinates use cases
- owns transactions
- invokes aggregates
- persists aggregate roots
- creates Outbox records
- maps DTOs
- isolates infrastructure

---

# 28. Next Documentation Step

Next document:

```
docs/application/ports.md
```

It will define:

- Input Ports
- Output Ports
- Repository Ports
- External Service Ports
- Port ownership
- Dependency inversion
- Hexagonal Architecture contracts
