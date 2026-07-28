# ADR-017: Adopt Optimistic Locking for Concurrent Aggregate Updates

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-017 |
| Title | Adopt Optimistic Locking for Concurrent Aggregate Updates |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Persistence, Transactions and Concurrency Control |
| Related Work Items | Aggregate Consistency, Concurrent Commands and Transaction Safety |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform processes concurrent requests that may attempt to modify the same business aggregate.

Examples include:

- two users updating the same order
- approval and cancellation arriving concurrently
- checkout processing while an order is being modified
- duplicate commands delivered through SQS
- concurrent saga responses
- scheduled reconciliation competing with normal processing
- retry execution overlapping with the original request
- multiple service replicas consuming related work
- inventory or payment outcomes arriving simultaneously
- administrative recovery racing with automated processing

The platform runs multiple application instances and relies on PostgreSQL, Spring Boot and JPA/Hibernate.

Without explicit concurrency control, concurrent transactions may overwrite or invalidate one another.

Example:

```text
Transaction A reads Order version 4

Transaction B reads Order version 4

Transaction A changes status to APPROVED

Transaction B changes status to CANCELLED

Transaction A commits

Transaction B commits
```

Without concurrency protection, the last commit may silently overwrite the previous valid update.

This is known as a lost update.

The platform requires a consistent strategy for detecting and handling concurrent aggregate modifications.

---

# 2. Problem Statement

The platform requires a concurrency-control strategy that:

- prevents silent lost updates
- preserves aggregate invariants
- supports multiple application replicas
- works with PostgreSQL
- integrates with JPA and Hibernate
- remains compatible with Clean Architecture
- remains compatible with Domain-Driven Design
- avoids unnecessary database locking
- supports short-lived transactions
- supports controlled retries
- works with transactional outbox persistence
- works with saga processing
- supports HTTP and SQS workloads
- exposes meaningful conflict responses
- remains observable
- remains testable
- avoids duplicate side effects
- supports rolling deployments
- performs predictably under expected contention

---

# 3. Decision Drivers

The primary decision drivers are:

1. aggregate consistency
2. lost-update prevention
3. horizontal scalability
4. low lock contention
5. PostgreSQL compatibility
6. JPA and Hibernate support
7. explicit conflict detection
8. operational observability
9. retry safety
10. compatibility with Transactional Outbox
11. compatibility with Saga processing
12. maintainability
13. testability
14. predictable application behavior
15. minimal infrastructure coupling
16. support for asynchronous commands
17. preservation of domain invariants
18. avoidance of long-held locks

---

# 4. Constraints

The decision must consider:

- application workloads run in multiple Kubernetes pods
- PostgreSQL uses multiversion concurrency control
- transactions must remain short
- message delivery is at least once
- duplicate messages are expected
- JPA entities represent persisted aggregates
- aggregate updates may emit outbox events
- retries may occur at several layers
- business rejection differs from concurrency conflict
- some operations are naturally idempotent
- some operations create irreversible side effects
- database isolation alone does not prevent every domain conflict
- state may change between command validation and commit
- concurrent work may arrive through HTTP, SQS or scheduled jobs
- distributed locks introduce additional failure modes
- applied Flyway migrations must never be modified

---

# 5. Considered Options

## 5.1 Option A: No Explicit Concurrency Control

The platform could rely on the database's default transaction behavior.

### Advantages

- no additional implementation
- no version column
- simple persistence model

### Disadvantages

- silent lost updates
- inconsistent aggregate state
- unpredictable last-write-wins behavior
- difficult incident diagnosis
- weak business correctness
- unsafe horizontal scaling

---

## 5.2 Option B: Pessimistic Locking

The application could lock rows before modification.

Typical SQL behavior:

```sql
SELECT ...
FOR UPDATE;
```

JPA may use:

```java
LockModeType.PESSIMISTIC_WRITE
```

### Advantages

- prevents concurrent writes while the lock is held
- useful for short critical sections
- explicit serialization
- appropriate for some high-contention workflows

### Disadvantages

- lock waiting
- deadlock risk
- reduced throughput
- longer transaction duration
- poor fit for remote calls inside transactions
- increased operational complexity
- potential connection-pool exhaustion
- difficult scaling under contention

---

## 5.3 Option C: Distributed Locking

The platform could coordinate access using Redis or another distributed-lock provider.

### Advantages

- coordination beyond one database
- familiar mutual-exclusion model
- potentially useful for singleton work

### Disadvantages

- lease-expiration complexity
- lock-owner failure scenarios
- clock and network concerns
- additional infrastructure dependency
- risk of stale locks
- risk of split-brain behavior
- does not replace database transaction safety
- unnecessary for normal aggregate updates

---

## 5.4 Option D: Serializable Database Isolation

Transactions could execute using the serializable isolation level.

### Advantages

- strongest transaction isolation
- database detects unsafe serialization patterns
- broad protection against concurrency anomalies

### Disadvantages

- higher abort rate
- increased retry requirements
- lower throughput under contention
- broader scope than necessary
- operational tuning complexity
- may penalize unrelated workloads

---

## 5.5 Option E: Optimistic Locking

Each aggregate row contains a version value.

An update succeeds only when the persisted version matches the version originally read.

Conceptual SQL:

```sql
UPDATE orders
SET status = :status,
    version = version + 1
WHERE id = :id
  AND version = :expected_version;
```

If no row is updated, a concurrent change occurred.

### Advantages

- prevents silent lost updates
- minimal blocking
- good fit for low-to-moderate contention
- natural JPA support
- supports horizontal scaling
- explicit conflict detection
- preserves short transactions
- integrates with aggregate versioning
- easy to observe and test

### Disadvantages

- conflicting transactions fail
- retry policy is required
- users may receive conflict responses
- high-contention aggregates may require redesign
- does not automatically protect cross-row invariants
- careless retries may repeat side effects

---

# 6. Decision

The Enterprise Order Platform adopts optimistic locking as the default concurrency-control strategy for mutable aggregates.

JPA-managed aggregate roots must use a version field through `@Version`.

Example:

```java
@Version
@Column(name = "version", nullable = false)
private long version;
```

The platform will detect concurrent modifications and handle them explicitly.

Pessimistic locking may be used only for narrowly scoped cases supported by documented evidence and architectural review.

Distributed locks are not the default mechanism for aggregate updates.

---

# 7. Rationale

Most platform operations are expected to have low or moderate contention.

Optimistic locking allows transactions to execute without holding blocking write locks during the full operation.

The platform prefers:

```text
Detect conflict at commit time

instead of

Block concurrent work preemptively
```

This supports:

- multiple service replicas
- higher concurrency
- shorter lock duration
- clearer conflict semantics
- reduced deadlock exposure

---

# 8. Core Principle

Every successful aggregate modification is conditional on the aggregate remaining unchanged since it was read.

Conceptually:

```text
Read aggregate at version N

↓

Validate command

↓

Apply domain behavior

↓

Persist only if version is still N

↓

Increment version to N + 1
```

If the version changed, the transaction must not silently overwrite the newer state.

---

# 9. Aggregate Boundary

Optimistic locking applies primarily to aggregate roots.

The aggregate root defines the consistency boundary.

All invariant-preserving changes inside one aggregate should occur through the root in one transaction.

Example:

```text
Order

├── OrderItem
├── ShippingAddress
├── PaymentTerms
└── ApprovalState
```

If these elements belong to the same aggregate, concurrent updates must be coordinated through the `Order` root.

---

# 10. Aggregate Version

Every mutable aggregate root should contain a persistent version.

Example:

```java
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
```

The version is managed by JPA and Hibernate.

Application code must not increment the version manually.

---

# 11. Version Type

Preferred version types are:

- `long`
- `Long`

A numeric version is preferred because it is:

- simple
- monotonic
- compact
- directly supported by JPA
- easy to inspect
- easy to test

Timestamp-based versioning should not be used unless a specific requirement justifies it.

---

# 12. Database Column

The database schema must contain a non-null version column.

Example:

```sql
ALTER TABLE orders
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
```

For an existing table, migration design must consider:

- current rows
- deployment compatibility
- default removal where appropriate
- rolling deployment
- indexing requirements
- application startup order

Every schema change must use a new Flyway migration.

Applied migrations must never be modified.

---

# 13. Version Column Semantics

The version column:

- belongs to persistence concurrency control
- increments after successful updates
- detects stale writes
- may support event sequencing
- may support API concurrency tokens
- must not represent a business status
- must not be reset during normal updates

---

# 14. JPA Behavior

Hibernate includes the version in the update predicate.

Conceptual SQL:

```sql
UPDATE orders
SET status = ?,
    updated_at = ?,
    version = ?
WHERE id = ?
  AND version = ?;
```

If the affected-row count is zero, Hibernate raises an optimistic-lock exception.

Common exceptions include:

- `OptimisticLockException`
- `OptimisticLockingFailureException`
- `ObjectOptimisticLockingFailureException`
- `StaleObjectStateException`

The application must translate persistence-specific exceptions at the proper boundary.

---

# 15. Exception Translation

Persistence exceptions must not leak directly into the Domain layer or public API.

An application-level exception may be used:

```java
public class ConcurrentAggregateModificationException
        extends RuntimeException {

    private final UUID aggregateId;

    public ConcurrentAggregateModificationException(
            UUID aggregateId,
            Throwable cause
    ) {
        super("The aggregate was modified concurrently.", cause);
        this.aggregateId = aggregateId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }
}
```

The exact implementation must follow the platform exception-handling standards.

---

# 16. Domain Independence

Domain entities must not depend on:

- `jakarta.persistence.Version`
- Hibernate exceptions
- Spring persistence exceptions
- database lock modes

When the domain model is separated from JPA entities, the version remains in the persistence representation or application concurrency token.

When the same entity is used for domain and persistence, JPA concerns must remain limited and controlled.

---

# 17. Transaction Boundary

The aggregate must be:

```text
Loaded

Modified

Persisted

Committed
```

inside one short transaction.

Remote HTTP calls, SQS waits or long-running work must not occur while holding the transaction open.

Preferred flow:

```text
Load required external information

↓

Begin local transaction

↓

Load aggregate

↓

Apply command

↓

Persist aggregate and outbox event

↓

Commit
```

The exact ordering must preserve correctness and avoid stale assumptions.

---

# 18. Transaction Duration

Transactions must remain as short as practical.

Long transactions increase:

- conflict probability
- row-version lifetime
- lock duration
- connection usage
- memory usage
- failure cost
- retry cost

Do not perform inside an aggregate update transaction:

- remote HTTP calls
- user interaction
- blocking waits
- file transfer
- long computation
- arbitrary sleeps
- external payment confirmation

---

# 19. Lost Update Prevention

Optimistic locking directly prevents lost updates on the protected row.

Example:

```text
Order version 5
```

Two transactions read version 5.

Transaction A commits:

```text
status = APPROVED
version = 6
```

Transaction B attempts:

```text
WHERE version = 5
```

No row matches.

Transaction B fails instead of overwriting the approved state.

---

# 20. Read-Modify-Write Operations

Any operation that:

1. reads aggregate state
2. computes a decision
3. changes aggregate state
4. writes the aggregate

must consider concurrency.

Examples:

- approve order
- cancel order
- add item
- remove item
- update quantity
- change shipping address
- update workflow status
- apply discount
- reserve order number
- complete checkout

---

# 21. Aggregate Invariants

Optimistic locking protects invariants only when all relevant state belongs to the same locked aggregate row or is updated consistently within the same transaction.

Example invariant:

```text
A cancelled order cannot be approved.
```

If approval and cancellation both update the same versioned order root, one transaction will fail.

---

# 22. Cross-Aggregate Invariants

Optimistic locking on one aggregate does not automatically protect invariants spanning multiple aggregates.

Example:

```text
Customer total credit exposure must not exceed a limit
across multiple orders.
```

Possible strategies include:

- dedicated aggregate
- database constraint
- serialized application process
- reservation model
- explicit consistency boundary
- event-driven eventual consistency
- carefully scoped pessimistic locking
- serializable transaction where justified

Cross-aggregate consistency requires separate design.

---

# 23. Write Skew

Write skew may occur when transactions update different rows based on a shared condition.

Example:

```text
Transaction A checks active approver count

Transaction B checks active approver count

Both update different approver rows

Shared invariant becomes invalid
```

Row-level optimistic locking alone may not detect this.

Mitigations include:

- model shared state under one aggregate root
- lock or version a shared coordination row
- enforce a database constraint
- use serializable isolation selectively
- redesign the invariant

---

# 24. Database Constraints

Optimistic locking complements but does not replace database constraints.

The database should still enforce:

- uniqueness
- not-null requirements
- foreign keys
- check constraints
- valid ranges where practical

Example:

```sql
ALTER TABLE order_item
ADD CONSTRAINT ck_order_item_quantity_positive
CHECK (quantity > 0);
```

---

# 25. Isolation Level

The default transaction isolation level may remain PostgreSQL `READ COMMITTED` unless a specific use case requires stronger isolation.

Optimistic locking provides stale-write detection independently from a global increase to serializable isolation.

Isolation changes must be justified per use case.

---

# 26. PostgreSQL MVCC

PostgreSQL uses multiversion concurrency control.

Readers generally do not block writers, and writers create new row versions.

Optimistic locking works with MVCC by adding an application-visible version predicate to the update.

MVCC alone does not automatically prevent application-level lost updates in every read-modify-write flow.

---

# 27. Version and Hibernate Dirty Checking

Hibernate increments the version when it detects a versioned entity update.

The platform must understand the effect of:

- dirty checking
- flush timing
- transaction commit
- explicit `save`
- detached entities
- merge operations
- bulk updates
- native SQL

Concurrency correctness must not depend on accidental flush behavior.

---

# 28. Flush Timing

An optimistic-lock conflict may appear:

- during explicit flush
- during repository save
- during transaction commit

The application must treat the entire transaction as failed.

Do not assume the aggregate update succeeded before commit completes.

---

# 29. Detached Entities

Updating a detached entity with stale state is risky.

Preferred pattern:

```text
Receive command DTO

↓

Load current aggregate

↓

Apply explicit domain operation

↓

Commit
```

Avoid:

```text
Receive full detached entity from client

↓

Merge entire object graph
```

Full-state merge can overwrite fields the client did not intend to change.

---

# 30. Patch Semantics

Partial updates should express intended changes explicitly.

Example:

```java
order.changeShippingAddress(newAddress);
```

Avoid generic property copying from request objects into managed entities.

Explicit domain methods improve:

- invariant enforcement
- conflict reasoning
- auditability
- testability
- security

---

# 31. Bulk JPQL Updates

Bulk JPQL or SQL updates may bypass normal entity lifecycle and version handling.

Example:

```java
@Modifying
@Query("""
    update OrderJpaEntity o
       set o.status = :status
     where o.id = :id
""")
```

This may bypass expected optimistic-lock semantics.

Bulk updates to versioned aggregates are prohibited unless they:

- include the expected version
- increment the version correctly
- validate affected-row count
- preserve domain invariants
- have explicit architectural review
- include integration tests

---

# 32. Native SQL Updates

Native updates must include version protection.

Example:

```sql
UPDATE orders
SET status = :new_status,
    version = version + 1
WHERE id = :id
  AND version = :expected_version;
```

The application must verify that exactly one row was updated.

---

# 33. Repository Contract

Repository contracts should focus on aggregate persistence rather than lock implementation.

Example:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    Order save(Order order);
}
```

Persistence adapters translate optimistic-lock failures into application-level concurrency exceptions.

---

# 34. Command Handling

A command handler should:

1. validate command syntax
2. load the current aggregate
3. invoke domain behavior
4. persist the aggregate
5. persist outbox events
6. commit
7. translate conflicts appropriately

Example:

```java
@Transactional
public OrderResult handle(ApproveOrderCommand command) {
    Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.approve(command.approvedBy());

    orderRepository.save(order);
    outboxRepository.saveAll(eventMapper.from(order.pullDomainEvents()));

    return resultMapper.from(order);
}
```

The transaction is successful only after commit.

---

# 35. Transactional Outbox

Aggregate update and outbox persistence must remain atomic.

Conceptual transaction:

```text
Update Order version 7 → 8

+

Insert OrderApproved outbox record

↓

Commit
```

If the optimistic-lock check fails:

- aggregate update must roll back
- outbox insert must roll back
- no event may represent an update that did not commit

---

# 36. Event Aggregate Version

Integration events may include aggregate version where useful.

Example:

```json
{
  "eventId": "5f68fb79-6f85-498f-8366-b052eefdc62f",
  "eventType": "ORDER_APPROVED",
  "aggregateId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "aggregateVersion": 8
}
```

The version may help consumers:

- identify stale events
- detect gaps
- order aggregate-specific changes
- troubleshoot concurrency incidents

The consumer must not assume global ordering across aggregates.

---

# 37. Domain Event Collection

Domain events should be associated only with successful state transitions.

If transaction commit fails due to optimistic locking, generated events must not escape through direct publication.

The Transactional Outbox remains mandatory for durable publication.

---

# 38. Saga Integration

Saga responses may arrive concurrently.

Example:

```text
PaymentAuthorized

and

CancelOrder
```

Both may attempt to update the same order or saga state.

Optimistic locking must ensure that only a valid transition commits.

The losing transaction must:

- reload current state if retry is allowed
- validate transition again
- avoid reusing stale domain decisions
- never force the original update blindly

---

# 39. Saga State Version

Saga state should also use optimistic locking.

Example:

```java
@Version
private long version;
```

This protects against:

- timeout and response races
- duplicate result processing
- cancellation and completion races
- parallel branch completion
- manual recovery conflicts

---

# 40. SQS Consumers

SQS consumers must assume concurrent or duplicate processing can occur.

Optimistic locking is one layer of protection.

It does not replace:

- idempotency
- inbox deduplication
- event-ID tracking
- partition-key strategy
- valid transition checks

---

# 41. Duplicate Delivery

A duplicate message may load an aggregate after the original message already committed.

In that case, the domain may determine that the requested transition is:

- already applied
- invalid
- harmless
- stale

Idempotency should resolve duplicates before they become repeated business effects.

Optimistic locking alone is not a deduplication strategy.

---

# 42. Event Ordering

SQS Standard does not guarantee strict ordering; SQS FIFO preserves ordering within a MessageGroupId.

A stable aggregate key should be used when ordered processing is required.

Even with correct partitioning, optimistic locking remains valuable because:

- HTTP updates may occur concurrently
- multiple consumer groups may update related state
- retries may overlap
- administrative operations may compete
- scheduled work may run concurrently

---

# 43. HTTP Concurrency

Public APIs may expose concurrency control to clients.

Two supported approaches are:

- server-side conflict detection only
- explicit conditional requests using ETags

For APIs where clients edit previously read resources, ETags are recommended.

---

# 44. ETag

The aggregate version may be represented as an HTTP ETag.

Example response:

```http
ETag: "8"
```

Client update:

```http
If-Match: "8"
```

The server updates only if the current version still matches.

---

# 45. Conditional Request

Example:

```http
PUT /api/v1/orders/08ab5af0-a5fb-4b90-a149-5d77057c663d
If-Match: "8"
Content-Type: application/json
```

If the current version is 9, the request must fail rather than overwrite newer data.

---

# 46. HTTP Conflict Status

Concurrency failure should use a consistent HTTP response.

Possible status codes include:

- `409 Conflict`
- `412 Precondition Failed`

Recommended semantics:

- use `412 Precondition Failed` when an explicit `If-Match` precondition fails
- use `409 Conflict` when the server detects a concurrent state conflict without an explicit HTTP precondition

The API guidelines must remain consistent across services.

---

# 47. Problem Details

Concurrency errors should use RFC 9457 Problem Details.

Example:

```json
{
  "type": "https://api.enterprise-order-platform.example/problems/concurrent-modification",
  "title": "Concurrent modification",
  "status": 409,
  "detail": "The order was modified by another operation. Reload the resource and retry.",
  "instance": "/api/v1/orders/08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "code": "ORDER_CONCURRENT_MODIFICATION"
}
```

Do not expose internal persistence exception details.

---

# 48. Client Guidance

Clients receiving a concurrency conflict should:

1. reload current state
2. present or reevaluate the new state
3. reapply intended changes only when still valid
4. submit a new request using the new version

Clients must not automatically overwrite the resource with stale full-state payloads.

---

# 49. Automatic Retry

Automatic retry may be appropriate only when:

- the operation is idempotent
- no external side effect occurred
- the command can be reevaluated against current state
- retry count is bounded
- conflict probability is low
- the retry does not violate user intent
- the full transaction is executed again

---

# 50. Retry Principle

A retry must repeat the entire logical transaction against freshly loaded state.

Correct:

```text
Conflict

↓

Start new transaction

↓

Reload aggregate

↓

Reapply domain command

↓

Validate invariants

↓

Attempt commit
```

Incorrect:

```text
Conflict

↓

Force update with old values
```

---

# 51. Retry Boundaries

Optimistic-lock retries should occur outside the failed transaction.

A transaction marked for rollback must not be reused.

The retry mechanism must start a new transaction for every attempt.

---

# 52. Retry Count

Retries must be bounded.

A typical policy may allow:

```text
1 to 3 additional attempts
```

The exact value depends on:

- operation cost
- contention frequency
- user-facing latency
- idempotency
- business semantics

Unlimited retry is prohibited.

---

# 53. Retry Backoff

Small randomized backoff may reduce immediate repeated collision.

Example:

```text
Attempt 1: immediate

Attempt 2: 20–50 ms

Attempt 3: 50–150 ms
```

Backoff must not hide high-contention design problems.

---

# 54. Retryable Operations

Potentially retryable operations include:

- incrementing an internal retry counter
- recording idempotent processing state
- applying an associative update
- advancing a saga from one valid state
- updating aggregate metadata with deterministic input

Every operation still requires explicit review.

---

# 55. Non-Retryable Operations

Automatic retry should normally be avoided for:

- user-edited full-resource updates
- commands with ambiguous intent after state change
- operations that call external systems inside the attempt
- payment capture
- sending an external notification directly
- irreversible side effects
- commands whose validation result may materially change
- operations requiring user confirmation

---

# 56. External Side Effects

External side effects must not occur before the local transaction is durably committed when retry could repeat them.

Preferred approach:

```text
Update aggregate

+

Persist outbox event

↓

Commit

↓

Publish integration event asynchronously
```

Do not:

```text
Call external payment provider

↓

Optimistic-lock conflict

↓

Retry and call provider again
```

unless the external call has a stable idempotency key and the workflow explicitly supports it.

---

# 57. Resilience4j Retry

Resilience4j Retry may be used for optimistic-lock retry only when transaction boundaries are correctly recreated.

Applying retry directly around a method whose transaction is created outside the retry interceptor may cause retries to occur inside the same rollback-only transaction.

The interceptor order must be validated through integration tests.

A dedicated retry coordinator is often clearer.

---

# 58. Spring Retry and AOP

When using annotation-based retry and transactions, proxy order matters.

The implementation must verify that:

```text
Each retry attempt

↓

Creates a fresh transaction
```

Self-invocation must not bypass Spring proxies.

Hidden retry behavior is discouraged for business-critical operations.

---

# 59. Explicit Retry Coordinator

A controlled retry coordinator may be used.

Conceptual example:

```java
public final class OptimisticLockRetryExecutor {

    public <T> T execute(
            Supplier<T> operation,
            int maxAttempts
    ) {
        int attempt = 1;

        while (true) {
            try {
                return operation.get();
            }
            catch (ConcurrentAggregateModificationException exception) {
                if (attempt >= maxAttempts) {
                    throw exception;
                }

                attempt++;
            }
        }
    }
}
```

Production implementation must include:

- bounded attempts
- fresh transaction per attempt
- optional backoff
- metrics
- logging
- interruption handling
- no retry for unrelated exceptions

---

# 60. User-Initiated Changes

For interactive updates, returning a conflict is often preferable to automatic retry.

Example:

```text
User A edits shipping address

User B changes order status

User A submits old representation
```

The server should not silently combine or overwrite user changes without defined merge semantics.

---

# 61. Commutative Operations

Some operations may be safely retried because they are commutative or can be expressed atomically.

Example:

```text
increment retry count
```

A direct atomic database update may be more appropriate than loading the aggregate.

However, atomic updates must still preserve domain rules and version semantics.

---

# 62. High-Contention Aggregates

Frequent optimistic-lock conflicts may indicate:

- aggregate is too large
- aggregate owns unrelated state
- hotspot identifier
- partitioning problem
- inappropriate synchronous coordination
- excessive command concurrency
- retry storm
- long transaction duration
- missing message-key strategy

The default response must not be to increase retry count indefinitely.

---

# 63. Aggregate Redesign

Potential redesign strategies include:

- split independent consistency boundaries
- introduce reservation records
- introduce append-only commands
- partition work by aggregate ID
- serialize commands through SQS
- use a dedicated counter table
- use database atomic operations
- model a queue
- use event sourcing where justified
- use narrowly scoped pessimistic locking

---

# 64. Pessimistic Locking Exception

Pessimistic locking may be approved when:

- contention is demonstrably high
- operation is short
- transaction performs no remote calls
- lock order is deterministic
- throughput impact is understood
- timeout is configured
- deadlock handling exists
- load tests support the choice
- architecture review approves it

---

# 65. Lock Timeout

Approved pessimistic locking must define a lock timeout.

Waiting indefinitely is prohibited.

Lock-timeout failures must be classified and observable.

---

# 66. Deadlock Handling

Deadlocks may still occur with pessimistic or multi-row updates.

The platform must:

- maintain deterministic lock order
- keep transactions short
- classify deadlock exceptions
- use bounded retry where safe
- emit metrics
- investigate recurring patterns

---

# 67. Distributed Locks

Distributed locks must not be used to compensate for a poorly defined aggregate boundary.

A distributed lock may be considered for:

- singleton scheduled tasks
- leader-only maintenance
- one-at-a-time external reconciliation

It still requires:

- lease ownership
- expiration policy
- fencing tokens where necessary
- failure recovery
- observability
- architectural approval

---

# 68. Fencing Tokens

When a distributed lock protects an external or long-running resource, a simple lease may be insufficient.

A fencing token may be required to prevent an expired lock owner from performing stale work.

This concern is separate from normal JPA optimistic locking.

---

# 69. Version Exposure

The persistence version may be exposed externally only through a controlled representation.

Supported representations include:

- ETag
- API version field
- command expected-version field

Internal JPA implementation details should not leak unnecessarily.

---

# 70. Expected Version in Commands

Asynchronous commands may include an expected aggregate version when the workflow requires conditional execution.

Example:

```json
{
  "commandId": "a72014fc-e099-4a42-9432-2cd523c50f60",
  "orderId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "expectedVersion": 8
}
```

The participant must define behavior when:

- current version equals expected version
- current version is lower
- current version is higher
- command was already processed

---

# 71. Expected Version Semantics

Expected-version checks are useful when command validity depends on exact aggregate state.

They must not replace idempotency.

A command may be duplicated with the same expected version after the original has already succeeded.

The receiver should detect the duplicate command before classifying it as a stale conflict.

---

# 72. Version in Events

Aggregate version in events can support consumer ordering checks.

Example:

```text
Current consumer version: 10

Received event version: 10
→ duplicate or already applied

Received event version: 11
→ next event

Received event version: 13
→ possible gap

Received event version: 9
→ stale event
```

Consumers must define explicit behavior for each case.

---

# 73. Event Version Versus Aggregate Version

These values are distinct:

```text
eventVersion
```

represents the event contract version.

```text
aggregateVersion
```

represents the source aggregate state version.

They must not be conflated.

---

# 74. Audit History

Concurrency conflicts should be observable but not necessarily stored as business audit events.

A business audit record should represent committed business actions.

A failed optimistic-lock attempt did not commit a business state change.

Operational diagnostics may record:

- aggregate type
- conflict count
- operation
- attempt
- service version
- correlation ID
- trace ID

---

# 75. Logging

Concurrency logs should include:

- aggregate type
- aggregate ID where allowed
- operation
- expected version where available
- attempt
- result
- correlation ID
- trace ID

Avoid logging:

- full aggregate state
- personal data
- request payloads
- raw persistence exception internals

---

# 76. Log Level

Recommended logging behavior:

- first retryable conflict: `DEBUG` or controlled `INFO`
- exhausted retry: `WARN`
- unexpected systemic failure: `ERROR`
- expected client conflict: normally not `ERROR`

The same exception must not be logged repeatedly at every layer.

---

# 77. Metrics

Recommended metrics include:

```text
aggregate.optimistic_lock.conflict

aggregate.optimistic_lock.retry

aggregate.optimistic_lock.retry_exhausted

aggregate.update.success

aggregate.update.duration
```

---

# 78. Metric Labels

Appropriate labels include:

- aggregate type
- operation
- result
- service
- environment

Prohibited labels include:

- aggregate ID
- order ID
- customer ID
- command ID
- event ID
- exception message

---

# 79. Conflict Rate

Conflict rate should be monitored.

A sustained increase may indicate:

- traffic shift
- command duplication
- consumer partitioning issue
- long transactions
- aggregate hotspot
- retry policy defect
- recent deployment regression

---

# 80. Alerting

Alerts may be appropriate for:

- high conflict ratio
- retry exhaustion growth
- conflicts concentrated on one operation
- rising transaction duration
- deadlock growth
- high lock-wait duration
- saga transition conflicts
- outbox transaction rollback increase

Individual expected conflicts should not trigger alerts.

---

# 81. OpenTelemetry

Concurrency conflicts should correlate with traces.

Potential span attributes include:

```text
enterprise.aggregate.type

enterprise.operation

enterprise.concurrency.result

enterprise.retry.attempt
```

High-cardinality identifiers should remain controlled.

---

# 82. Trace Events

A retryable concurrency conflict may be recorded as a span event.

Example:

```text
optimistic-lock-conflict
```

Attributes may include:

- attempt
- aggregate type
- expected version
- retry scheduled

Sensitive data must not be attached.

---

# 83. Testing Strategy

Testing must cover:

- successful version increment
- concurrent update conflict
- conflict exception translation
- transaction rollback
- outbox rollback on conflict
- bounded retry
- retry exhaustion
- no retry for business exceptions
- new transaction per retry
- ETag validation
- stale expected version
- duplicate command handling
- saga race conditions
- bulk-update protection
- pessimistic-lock exception cases where approved

---

# 84. Unit Tests

Unit tests should validate:

- retry decision
- retry attempt limits
- exception classification
- command expected-version logic
- conflict response mapping
- domain transition reevaluation
- client conflict semantics

Pure domain tests should not depend on JPA exceptions.

---

# 85. Persistence Integration Tests

PostgreSQL Testcontainers must validate actual concurrency behavior.

Tests should use:

- separate transactions
- separate persistence contexts
- controlled synchronization
- deterministic coordination
- real JPA versioning
- actual PostgreSQL behavior

H2 is not sufficient for these tests.

---

# 86. Deterministic Concurrency Tests

Concurrency tests must avoid timing assumptions based on `Thread.sleep`.

Use deterministic coordination primitives such as:

- `CountDownLatch`
- `CyclicBarrier`
- `Phaser`
- futures
- controlled transaction callbacks

---

# 87. Example Concurrent Test Scenario

A concurrency integration test should:

1. create one order
2. load it in transaction A
3. load it in transaction B
4. modify both instances
5. commit transaction A
6. attempt to commit transaction B
7. assert an optimistic-lock failure
8. verify final database state
9. verify only one outbox event committed

---

# 88. Separate Persistence Contexts

Concurrent update tests must not use one shared EntityManager.

Each transaction requires an independent persistence context to simulate real concurrent requests.

---

# 89. Flush in Tests

Tests should explicitly flush when needed to force the optimistic-lock check at a deterministic point.

Example:

```java
entityManager.flush();
```

Assertions must not depend accidentally on transaction cleanup.

---

# 90. Outbox Atomicity Test

A required test must prove:

```text
Aggregate update conflict

↓

Transaction rollback

↓

No outbox event persisted
```

This protects against publication of events for failed updates.

---

# 91. Retry Transaction Test

A required test must prove that each retry:

- reloads the aggregate
- runs in a fresh transaction
- receives current state
- revalidates the domain command
- does not reuse a managed stale entity

---

# 92. API Integration Tests

API tests should validate:

- valid `If-Match`
- stale `If-Match`
- missing required precondition where enforced
- `409 Conflict`
- `412 Precondition Failed`
- Problem Details response
- new ETag after successful update

---

# 93. SQS Integration Tests

SQS tests should validate:

- duplicate command
- concurrent command processing
- expected-version mismatch
- idempotency before version conflict
- retry behavior
- dead-letter behavior where appropriate
- final aggregate state
- emitted event count

---

# 94. Saga Concurrency Tests

Saga tests should cover:

- timeout versus success response
- cancellation versus completion
- duplicate success response
- compensation versus late success
- parallel branch completion
- manual recovery versus automatic retry

Exactly one valid transition must commit.

---

# 95. Load Testing

Load testing should measure:

- conflict rate
- throughput
- retry amplification
- transaction duration
- connection-pool usage
- CPU cost
- lock wait
- deadlocks
- tail latency

High conflict rates require architectural analysis.

---

# 96. Chaos and Failure Testing

Failure tests should cover:

- application crash before commit
- application crash after commit
- database failover
- retry during deployment
- duplicate SQS delivery
- delayed transaction
- optimistic-lock conflict after external dependency preparation
- collector or logging failure during conflict

Business state must remain consistent.

---

# 97. Static Analysis

Code review and static analysis should identify:

- bulk updates to versioned tables
- native SQL without version checks
- direct version modification
- full-entity merge from API payload
- retry around non-idempotent operations
- remote calls inside transactions
- swallowed optimistic-lock exceptions
- unbounded retry loops

---

# 98. Architecture Tests

Architecture tests should enforce:

- Domain does not depend on JPA lock exceptions
- controllers do not implement retry loops
- repository adapters perform persistence exception translation
- outbox persistence occurs inside the aggregate transaction
- external calls do not occur from persistence adapters
- JPA entities do not expose arbitrary version mutation
- bulk update repositories require explicit review

---

# 99. Migration Strategy

Adding version columns to existing tables must follow zero-downtime migration principles where required.

A possible sequence is:

1. add nullable or defaulted version column
2. deploy code capable of reading the column
3. backfill existing rows
4. enforce `NOT NULL`
5. remove temporary defaults where appropriate

The exact sequence depends on PostgreSQL version, table size and deployment model.

Every step uses a new migration when necessary.

---

# 100. Existing Data

Existing rows must receive a valid initial version.

Typical initial value:

```text
0
```

or:

```text
1
```

The platform must standardize one convention.

The recommended initial database value is `0`, allowing the first update to increment it to `1`.

---

# 101. Rolling Deployment Compatibility

During migration, old and new application versions may overlap.

The deployment strategy must ensure that:

- old code does not overwrite version behavior
- new code can read existing rows
- database defaults support transition safely
- versioned writes are not mixed with unsafe unversioned writes
- rollback behavior is understood

A direct mixed-version deployment may be unsafe if old code updates rows without incrementing the version.

---

# 102. Cutover Strategy

For critical aggregates, introducing optimistic locking may require:

- maintenance window
- coordinated rollout
- temporary write restriction
- blue-green deployment
- feature flag
- version-aware dual compatibility

The migration plan must be documented per service.

---

# 103. Indexing

A primary-key index is normally sufficient for:

```sql
WHERE id = ?
  AND version = ?;
```

A separate index on version alone is generally unnecessary.

Composite indexing should be introduced only based on query evidence.

---

# 104. Table Partitioning

For partitioned tables, optimistic-lock updates must include the correct partition key where required.

Partition movement and row-location changes require separate testing.

---

# 105. Soft Delete

Soft-delete operations are state changes and must increment the aggregate version.

Example:

```text
active = false

version = version + 1
```

Concurrent update and deletion must not silently overwrite one another.

---

# 106. Audit Columns

Updates to:

- `updated_at`
- `updated_by`

should occur only for successful writes.

A failed optimistic-lock attempt must not create a committed audit timestamp change.

---

# 107. Collection Updates

Changes to child collections may or may not increment the root version automatically depending on mapping and ownership.

The persistence mapping must be tested.

When collection changes affect aggregate invariants, the root version must reflect the modification.

---

# 108. Child Entity Versioning

Child entities may also use `@Version` when they have independent concurrent modification needs.

However, independent child versioning must not weaken aggregate-root invariant control.

The decision should reflect whether the child is:

- part of the aggregate
- independently addressable
- independently mutable
- independently consistent

---

# 109. Version Increment on Child Changes

For critical aggregates, integration tests must prove that changing a child entity or collection causes the expected root concurrency behavior.

If Hibernate mapping does not increment the root version automatically, alternatives include:

- explicit root touch
- root-level update method
- mapping adjustment
- versioning the child
- redesigning aggregate boundaries

---

# 110. Read-Only Transactions

Read-only transactions do not require version increments.

They may still read stale data relative to concurrent commits.

Read consistency requirements must be defined separately.

---

# 111. Caching

Second-level cache or application cache may expose stale aggregate representations.

Cache invalidation must occur after successful commit.

A stale cached representation must not be used to perform blind writes.

Writes should load or validate authoritative current state.

---

# 112. Redis Cache

Redis must not become the authority for aggregate versions.

The PostgreSQL version remains authoritative.

A cache entry may include version metadata to support freshness checks.

---

# 113. CQRS

In CQRS, the write model uses optimistic locking to preserve command-side invariants.

Read models may be eventually consistent and may expose their own projection version.

Read-model version must not be confused with write-aggregate version.

---

# 114. Projection Updates

Projection consumers may use event or aggregate version to:

- reject stale events
- detect gaps
- prevent out-of-order overwrite
- support rebuild diagnostics

Projection concurrency strategy may differ from aggregate concurrency strategy.

---

# 115. Event Sourcing

If event sourcing is adopted later, expected stream version provides an optimistic-concurrency mechanism.

Conceptual append:

```text
Append events only if stream version = expected version
```

This would require a separate ADR.

---

# 116. Batch Processing

Batch updates to aggregates must preserve version checks.

A batch job should process aggregates individually or use a controlled version-aware update strategy.

Blind mass updates are prohibited for invariant-rich aggregates.

---

# 117. Administrative Operations

Administrative recovery tools must respect optimistic locking.

Operators must not bypass concurrency by directly changing database rows during normal operations.

Recovery commands should:

- load current state
- validate allowed transition
- commit with version protection
- record operator identity
- create audit history

---

# 118. Reconciliation

Reconciliation processes may encounter state changed by normal operations.

They must:

- reload current state
- compare external and internal facts
- apply only valid corrective actions
- tolerate optimistic-lock conflicts
- use bounded retry where safe
- avoid overwriting newer state

---

# 119. Conflict Resolution

The platform does not adopt generic automatic field-level merging.

Conflict resolution remains operation-specific.

Possible outcomes include:

- retry command
- reject command
- reload and reevaluate
- merge explicitly defined independent fields
- move to manual review
- preserve newer state

---

# 120. Last-Write-Wins

Last-write-wins is prohibited for mutable business aggregates unless explicitly justified for a non-critical field.

It may be acceptable for selected telemetry or ephemeral metadata, but not for:

- order status
- payment state
- approval state
- financial values
- inventory reservation
- customer authorization
- saga state

---

# 121. Field-Level Concurrency

Some systems use field-level versions or patch merging.

The platform does not adopt field-level optimistic locking as the default because it increases complexity and may obscure aggregate invariants.

Aggregate-level consistency remains preferred.

---

# 122. Command Serialization

For very high contention, commands may be serialized by aggregate key through SQS.

Example:

```text
FIFO MessageGroupId = orderId
```

This reduces concurrent consumer updates within one consumer group.

It does not eliminate all concurrency sources and does not replace version checks.

---

# 123. Clock Independence

Numeric optimistic locking does not depend on synchronized clocks.

This is preferred over timestamp comparison for concurrency detection.

Timestamps remain useful for audit and observability, not as the primary version token.

---

# 124. Security

A client-supplied version is untrusted input.

The server must:

- validate its format
- compare it with authoritative state
- enforce authorization independently
- avoid exposing sensitive historical state
- prevent version manipulation from bypassing business rules

Correct version does not imply permission.

---

# 125. Information Disclosure

Concurrency responses should not reveal:

- who changed the resource
- confidential update details
- internal database state
- SQL
- stack traces

Authorized user interfaces may retrieve current state through normal APIs.

---

# 126. Operational Runbook

The concurrency runbook should include:

- finding conflict metrics
- identifying affected operation
- checking deployment changes
- checking transaction duration
- checking SQS queue/FIFO MessageGroupId distribution
- checking duplicate messages
- checking retry settings
- detecting aggregate hotspots
- checking database locks
- reviewing deadlocks
- deciding whether aggregate redesign is required

---

# 127. Incident Diagnosis

A conflict incident should be investigated using:

```text
Conflict-rate metric

↓

Trace

↓

Application logs

↓

Database transaction timing

↓

SQS processing metadata

↓

Aggregate design
```

Increasing retry limits without diagnosis is not an acceptable permanent fix.

---

# 128. Anti-Patterns

The following are prohibited:

- mutable aggregate without concurrency control
- silent last-write-wins
- manually incrementing `@Version`
- exposing JPA exceptions directly through APIs
- retrying inside a rollback-only transaction
- retrying without reloading current state
- retrying non-idempotent external side effects
- unbounded optimistic-lock retry
- using `Thread.sleep` for concurrency tests
- merging full client entities directly
- bulk JPQL updates that bypass versioning
- native SQL updates without expected-version predicates
- direct SQS publication before local commit
- treating optimistic locking as idempotency
- treating idempotency as optimistic locking
- using Redis locks for ordinary database-row updates
- remote service calls inside aggregate transactions
- long-held transactions
- swallowing concurrency conflicts
- returning generic internal errors for expected conflicts
- using aggregate IDs as metric labels
- modifying applied Flyway migrations
- assuming PostgreSQL MVCC alone prevents all lost updates
- relying only on SQS ordering
- forcing stale state after conflict
- automatic field-level merge without domain semantics

---

# 129. Positive Consequences

The decision provides:

- lost-update prevention
- explicit concurrency conflicts
- aggregate invariant protection
- reduced blocking
- good horizontal scalability
- native JPA support
- compatibility with PostgreSQL MVCC
- safer saga transitions
- safer concurrent command handling
- atomic outbox integration
- consistent API conflict semantics
- observable contention
- deterministic testing
- reduced dependence on distributed locks
- clearer transaction boundaries

---

# 130. Negative Consequences

The decision introduces:

- version columns
- conflict exceptions
- retry design
- client conflict handling
- additional integration tests
- migration complexity for existing tables
- possible user-visible conflicts
- need for aggregate redesign under high contention
- careful handling of detached entities
- restrictions on bulk updates
- deployment coordination when introducing versioning

These costs are accepted because silent data loss is not acceptable.

---

# 131. Neutral Consequences

The decision also means:

- some valid operations may need to be retried
- not every conflict should be retried automatically
- high conflict rates become an architectural signal
- database isolation and optimistic locking remain separate concerns
- cross-aggregate invariants require additional design
- idempotency remains mandatory for duplicate messages
- SQS ordering remains helpful but insufficient
- pessimistic locking remains available only by exception
- public APIs may expose ETags or expected versions

---

# 132. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| High conflict rate reduces throughput | High | Medium | Monitor contention and redesign hotspots |
| Automatic retry repeats side effects | High | Medium | Retry only full idempotent transactions |
| Retry occurs inside same transaction | High | Medium | Validate proxy and transaction boundaries |
| Bulk update bypasses versioning | High | Medium | Prohibit unreviewed bulk aggregate updates |
| Detached merge overwrites fields | High | Medium | Load current aggregate and apply explicit commands |
| Child update does not increment root version | High | Medium | Test mappings and enforce root touch |
| Old deployment bypasses new version column | High | Low | Use coordinated migration and cutover |
| Conflict exposed as generic server error | Medium | Medium | Standardize exception translation |
| High retries amplify database load | High | Medium | Bound retries and add backoff |
| Saga responses race | High | High | Version saga state and validate transitions |
| Duplicate messages create repeated effects | High | High | Combine idempotency with optimistic locking |
| Cross-row invariant suffers write skew | High | Low | Redesign consistency boundary or use stronger control |
| Pessimistic fallback creates deadlocks | High | Low | Require review, lock ordering and timeout |
| Cache supplies stale write state | Medium | Medium | Load authoritative state for writes |
| Conflict metrics create high cardinality | Medium | Low | Use bounded labels only |
| Direct SQL forgets version check | High | Medium | Code review, tests and static analysis |
| External call occurs before failed commit | High | Medium | Use outbox and idempotency keys |
| Version field is mutated manually | High | Low | Encapsulation and code review |
| Client retries stale payload indefinitely | Medium | Medium | Return clear conflict guidance and ETag |
| Applied migration is edited | High | Low | Add a new Flyway migration only |

---

# 133. Implementation Guidance

The following rules are mandatory:

1. Mutable aggregate roots must use optimistic locking by default.
2. JPA-managed aggregate roots must use `@Version`.
3. Version fields must not be modified manually.
4. Aggregate updates must execute inside short transactions.
5. Remote calls must not execute inside aggregate transactions.
6. Persistence conflicts must be translated into application-level exceptions.
7. Public APIs must return consistent conflict responses.
8. ETags should be used for client-managed resource editing where applicable.
9. Automatic retries must be bounded.
10. Every retry must use a fresh transaction.
11. Every retry must reload current aggregate state.
12. Domain rules must be reevaluated on retry.
13. Non-idempotent external side effects must not be repeated.
14. Aggregate update and outbox persistence must remain atomic.
15. Failed optimistic-lock transactions must not emit events.
16. SQS idempotency remains mandatory.
17. Saga state must use version protection.
18. Bulk updates to versioned aggregates require explicit version handling and review.
19. Native SQL updates must include expected-version predicates.
20. PostgreSQL Testcontainers must validate concurrency behavior.
21. Concurrency tests must use deterministic synchronization.
22. `Thread.sleep` must not be used to coordinate concurrency tests.
23. High conflict rates must trigger architectural analysis.
24. Pessimistic locking requires documented justification.
25. Distributed locks are not the default for aggregate writes.
26. Cross-aggregate invariants require separate consistency design.
27. Conflict metrics and structured logs are mandatory.
28. Version columns must be introduced through new Flyway migrations.
29. Applied Flyway migrations must never be modified.
30. Rolling deployment compatibility must be validated before introducing versioning into an existing service.

---

# 134. Validation

The decision will be validated through:

- JPA version increment tests
- concurrent transaction tests
- PostgreSQL Testcontainers
- conflict exception translation tests
- API ETag tests
- Problem Details tests
- bounded retry tests
- fresh-transaction retry tests
- outbox rollback tests
- duplicate SQS message tests
- saga race-condition tests
- migration tests
- rolling deployment analysis
- mapping tests for child updates
- native SQL review
- bulk update review
- load testing
- conflict-rate dashboards
- incident runbook exercises
- production-readiness review

---

# 135. Success Criteria

The decision is successful when:

- concurrent updates never silently overwrite committed state
- stale writes are detected reliably
- aggregate invariants remain valid
- failed updates do not publish outbox events
- HTTP clients receive clear conflict responses
- retryable operations use fresh state and fresh transactions
- non-idempotent side effects are not repeated
- duplicate messages do not create duplicate business effects
- saga races resolve to one valid transition
- conflict rates remain within acceptable thresholds
- high-contention hotspots are visible
- optimistic-lock exceptions do not leak from persistence
- concurrency tests are deterministic
- production deployment remains compatible with version-column migrations
- Domain code remains independent from JPA concurrency exceptions

---

# 136. Alternatives Rejected

## 136.1 No Explicit Concurrency Protection

Rejected because silent lost updates violate business correctness.

---

## 136.2 Pessimistic Locking as the Default

Rejected because it increases blocking, deadlock risk and resource consumption.

Pessimistic locking remains available for narrowly justified cases.

---

## 136.3 Serializable Isolation for Every Transaction

Rejected because it imposes broader concurrency and retry costs than required for normal aggregate updates.

Serializable isolation may be selected for specific cross-row invariants through separate analysis.

---

## 136.4 Redis Distributed Locks for Aggregate Updates

Rejected because distributed locks add lease and failure complexity while PostgreSQL remains the authoritative store.

---

## 136.5 Last-Write-Wins

Rejected for business aggregates because it silently discards valid committed changes.

---

## 136.6 Generic Field-Level Merge

Rejected because field-level merging may violate aggregate invariants and obscure user intent.

---

# 137. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-011: Adopt OpenAPI-First API Design
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-018: Version Integration-Event Contracts

---

# 138. References

- Jakarta Persistence Specification
- Hibernate ORM Documentation
- PostgreSQL Documentation
- PostgreSQL Multiversion Concurrency Control
- Spring Data JPA Documentation
- Domain-Driven Design
- Patterns of Enterprise Application Architecture
- Enterprise Order Platform Persistence Guidelines
- Enterprise Order Platform Testing Standards
- Enterprise Order Platform Transactional Outbox Architecture
- Enterprise Order Platform Saga Architecture
- Enterprise Order Platform API Design Guidelines
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing

---

# 139. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial aggregate concurrency-control baseline |

---

# 140. Decision Summary

The Enterprise Order Platform adopts optimistic locking as the default concurrency-control strategy for mutable aggregates.

The platform standardizes on:

```text
JPA @Version

+

PostgreSQL conditional updates

+

Explicit conflict translation

+

Bounded and idempotent retry

+

Transactional Outbox atomicity
```

The expected execution model is:

```text
Load aggregate at version N

↓

Apply domain command

↓

Persist aggregate and outbox event

↓

Commit only if version is still N

↓

Increment to version N + 1
```

When a conflict occurs:

```text
Do not overwrite newer state

Do not publish an event

Do not force stale data

Do not retry blindly
```

Instead:

```text
Reload current state

Reevaluate business rules

Retry only when safe

Otherwise return an explicit conflict
```

Optimistic locking does not replace:

```text
Idempotency

Database constraints

SQS ordering

Saga transition validation

Cross-aggregate consistency design
```

This decision establishes a scalable and explicit concurrency model that prevents silent lost updates while preserving short transactions, aggregate consistency and horizontal application scalability.
