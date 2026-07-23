# ADR-007: Adopt the Transactional Outbox Pattern

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-007 |
| Title | Adopt the Transactional Outbox Pattern |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Messaging and Data Consistency |
| Related Work Items | Reliable integration-event publication |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform performs business operations that must update transactional data and notify other bounded contexts.

Examples include:

- creating an order
- approving an order
- rejecting an order
- cancelling an order
- changing an order workflow state
- reserving inventory
- requesting payment authorization
- recording audit information
- notifying downstream services

A typical operation may require both:

1. persisting an aggregate change in PostgreSQL
2. publishing an integration event to Kafka

These operations involve different transactional resources.

PostgreSQL and Kafka do not participate in one shared local ACID transaction.

Without an explicit consistency mechanism, failures may produce inconsistent outcomes.

Example:

```text
Order transaction commits

↓

Kafka publication fails

↓

The order exists, but downstream systems are never notified
```

The opposite ordering also creates risk:

```text
Kafka event is published

↓

Database transaction fails

↓

Downstream systems receive an event for a business change that never committed
```

The platform therefore requires a reliable mechanism for coordinating database state changes and asynchronous event publication.

---

# 2. Problem Statement

The platform must ensure that integration events are published reliably when a business transaction commits.

The solution must:

- avoid distributed two-phase commit
- atomically persist business state and publication intent
- survive application crashes
- support retries
- tolerate duplicate delivery
- preserve event traceability
- support horizontal scaling
- expose operational metrics
- support controlled retention
- integrate with PostgreSQL
- integrate with Kafka
- remain compatible with Clean Architecture
- remain compatible with Domain-Driven Design
- maintain clear transaction boundaries

The decision must define the publication model, outbox schema, dispatcher responsibilities, retry behavior, delivery semantics and operational controls.

---

# 3. Decision Drivers

The primary decision drivers are:

1. reliable integration-event publication
2. transactional consistency
3. failure recovery
4. avoidance of distributed transactions
5. operational visibility
6. retry support
7. horizontal scalability
8. idempotent processing
9. auditability
10. compatibility with PostgreSQL
11. compatibility with Kafka
12. support for eventual consistency
13. bounded-context autonomy
14. deployment resilience
15. maintainability
16. predictable failure semantics

---

# 4. Constraints

The decision must consider:

- PostgreSQL is the primary transactional database
- Flyway manages schema evolution
- Kafka is used for integration events
- Java 21 is the runtime baseline
- Spring Boot is the application framework
- multiple application replicas may run concurrently
- aggregate persistence and outbox persistence must share one database transaction
- Kafka publication may fail independently
- consumers must tolerate duplicate delivery
- migration history is immutable
- public event contracts must remain versioned
- the Domain layer must remain broker-independent
- application transactions must remain short
- operational teams require metrics and diagnostics
- high-volume event tables require retention planning

---

# 5. Considered Options

## 5.1 Option A: Publish Directly After Database Commit

The application could commit the database transaction and then publish the event.

Example:

```text
Save order

Commit transaction

Publish Kafka event
```

### Advantages

- simple implementation
- low database overhead
- no dispatcher
- low publication latency when successful

### Disadvantages

- publication may fail after commit
- application crash may lose events
- retry state is difficult to persist reliably
- no durable publication intent
- operational recovery is manual
- consistency gap exists between database and broker
- event loss is possible

---

## 5.2 Option B: Publish Before Database Commit

The application could publish the event before committing the transaction.

Example:

```text
Publish Kafka event

Commit database transaction
```

### Advantages

- simple ordering
- no outbox table
- event is available quickly

### Disadvantages

- transaction may roll back after publication
- consumers may process nonexistent state
- compensating events become necessary
- downstream inconsistency is possible
- event publication is not atomic with business persistence

---

## 5.3 Option C: Distributed Transaction

The platform could use a distributed transaction coordinator across PostgreSQL and Kafka.

### Advantages

- conceptually atomic resource coordination
- consistent commit decision

### Disadvantages

- high operational complexity
- weak compatibility with modern cloud-native architectures
- reduced availability
- increased latency
- difficult failure recovery
- tight coupling between infrastructure components
- limited Kafka transaction coordination with arbitrary database transactions
- significant implementation and operational cost

---

## 5.4 Option D: Transactional Outbox

The application could persist the aggregate and an outbox record in the same PostgreSQL transaction.

A separate dispatcher would publish pending records to Kafka.

### Advantages

- atomic database persistence
- durable publication intent
- reliable retry
- crash recovery
- no distributed transaction
- clear operational state
- auditability
- scalable dispatcher design
- compatible with eventual consistency
- well-established architecture pattern

### Disadvantages

- additional table and indexes
- dispatcher implementation
- at-least-once delivery
- duplicate publication is possible
- retention and cleanup are required
- slight publication latency
- operational monitoring is required

---

## 5.5 Option E: Change Data Capture

A CDC platform could read database transaction logs and publish changes.

Examples include:

- Debezium
- Kafka Connect
- managed database change streams

### Advantages

- low application publication responsibility
- reads committed database changes
- scalable event capture
- useful for data replication

### Disadvantages

- additional infrastructure
- database-log coupling
- event contracts may become persistence-oriented
- operational complexity
- harder application-level event intent
- schema changes affect capture behavior
- not every database change represents a domain event
- requires separate governance

---

## 5.6 Option F: Poll Business Tables Directly

A background process could identify unpublished business changes from aggregate tables.

### Advantages

- no dedicated outbox table
- simple for very limited cases

### Disadvantages

- business tables require publication metadata
- event reconstruction may be unreliable
- state transitions may be overwritten
- difficult to represent multiple events
- poor separation of concerns
- weak auditability
- tightly couples integration to persistence schema

---

# 6. Decision

The Enterprise Order Platform will adopt the Transactional Outbox pattern for reliable integration-event publication.

For every business operation that requires an integration event:

1. the aggregate state is persisted
2. the integration event is mapped to an outbox record
3. both are committed in the same PostgreSQL transaction
4. an asynchronous dispatcher retrieves pending records
5. the dispatcher publishes them to Kafka
6. successful records are marked as sent
7. failed records remain available for retry

The platform accepts at-least-once delivery semantics.

Consumers must therefore be idempotent.

---

# 7. Rationale

The Transactional Outbox pattern provides the required reliability without introducing distributed transactions.

The database becomes the atomic boundary for:

```text
Business state

+

Publication intent
```

Once the transaction commits, the platform can recover publication even if:

- the process crashes
- Kafka is unavailable
- the network fails
- the application restarts
- the dispatcher is temporarily disabled
- a deployment interrupts processing

The outbox record is durable and can be retried until publication succeeds or the event reaches a terminal failure state.

---

# 8. Architectural Model

```text
Client

↓

Application Use Case

↓

Domain Aggregate

↓

Repository Adapter

↓

PostgreSQL Transaction
    Aggregate changes
    Outbox event

↓

Commit

↓

Outbox Dispatcher

↓

Kafka

↓

Consumer
```

The synchronous business transaction ends after PostgreSQL commits.

Kafka publication occurs asynchronously.

---

# 9. Transaction Boundary

The aggregate update and outbox insertion must occur in the same local database transaction.

Example:

```java
@Transactional
public CreateOrderResult execute(CreateOrderCommand command) {
    var order = orderFactory.create(command);

    orderRepository.save(order);

    var integrationEvent = eventMapper.toIntegrationEvent(
            new OrderCreated(
                    order.id(),
                    order.customerId()
            )
    );

    outboxRepository.save(
            OutboxEvent.pending(integrationEvent)
    );

    return resultMapper.toResult(order);
}
```

If either persistence operation fails, the transaction must roll back.

---

# 10. Domain Event and Integration Event Separation

Domain events and integration events are separate models.

Example:

```text
Domain event:

OrderApproved
```

```text
Integration event:

OrderApprovedIntegrationEventV1
```

A domain event represents an internal business fact.

An integration event represents a public contract for other bounded contexts.

The outbox stores the integration event, not a broker-specific domain object.

---

# 11. Outbox Aggregate Type

The outbox record must identify the business aggregate type.

Examples:

```text
ORDER

PAYMENT

INVENTORY_RESERVATION
```

The aggregate type supports:

- diagnostics
- filtering
- partitioning analysis
- operational reporting
- event routing
- retention policies

It must not be used as a substitute for the event type.

---

# 12. Aggregate Identifier

Each outbox record must include the aggregate identifier.

Example:

```text
aggregate_id = order UUID
```

This supports:

- traceability
- per-aggregate ordering analysis
- troubleshooting
- consumer deduplication strategies
- event correlation

---

# 13. Event Type

Each event must have an explicit type.

Examples:

```text
ORDER_CREATED

ORDER_APPROVED

ORDER_REJECTED

ORDER_CANCELLED
```

The event type must be stable and documented.

Renaming a public event type is a contract change.

---

# 14. Destination

The outbox record should contain the logical destination.

Example:

```text
orders.lifecycle.v1
```

The destination may represent:

- Kafka topic
- logical channel
- routing identifier

The Domain layer must not know the destination.

Destination selection belongs to Application or Infrastructure mapping.

---

# 15. Recommended Outbox Schema

A recommended PostgreSQL table is:

```sql
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    destination VARCHAR(200) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    last_error VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ,
    trace_id UUID
);
```

The exact schema may evolve through new Flyway migrations.

Applied migrations must never be modified.

---

# 16. Required Fields

The minimum recommended fields are:

| Field | Purpose |
|---|---|
| `id` | Unique event identifier |
| `aggregate_type` | Business aggregate category |
| `aggregate_id` | Aggregate instance identifier |
| `event_type` | Public event type |
| `destination` | Logical publication destination |
| `payload` | Serialized integration-event contract |
| `status` | Dispatch lifecycle status |
| `attempts` | Publication-attempt count |
| `next_attempt_at` | Next eligible retry time |
| `last_error` | Sanitized last publication error |
| `created_at` | Creation timestamp |
| `sent_at` | Successful publication timestamp |
| `trace_id` | Distributed tracing or correlation identifier |

Additional fields require clear operational or contract value.

---

# 17. Outbox Status

Recommended statuses are:

```text
PENDING

PROCESSING

SENT

FAILED
```

Possible semantics:

- `PENDING`: eligible for publication
- `PROCESSING`: currently claimed by a dispatcher
- `SENT`: publication completed successfully
- `FAILED`: terminal failure after the retry limit

The implementation may omit `PROCESSING` when database row locking safely coordinates dispatchers.

---

# 18. Event Identifier

Every outbox event must have a globally unique identifier.

The same identifier must be included in the published event envelope.

This identifier supports:

- consumer idempotency
- auditability
- duplicate detection
- incident analysis
- traceability

The event ID must not change between retries.

---

# 19. Event Envelope

A standard integration-event envelope should include:

```json
{
  "eventId": "3a31c8d3-1c30-4fd3-94d0-6e0e084d20a8",
  "eventType": "ORDER_APPROVED",
  "eventVersion": 1,
  "producer": "enterprise-order-platform",
  "aggregateType": "ORDER",
  "aggregateId": "59b1177f-3c48-4a35-a78f-ae8374b56327",
  "occurredAt": "2026-07-23T15:30:00Z",
  "traceId": "71624720-cb91-45bc-9fb5-c9341ba4c733",
  "payload": {
    "orderId": "59b1177f-3c48-4a35-a78f-ae8374b56327",
    "status": "APPROVED"
  }
}
```

The exact contract must be documented and versioned.

---

# 20. Payload Storage

The outbox payload should be stored as JSONB.

Advantages include:

- readable diagnostics
- flexible event schemas
- PostgreSQL validation
- efficient storage
- selective operational queries when justified

The payload must represent the complete publication contract required by the dispatcher.

The dispatcher must not reconstruct events from mutable business tables.

---

# 21. Payload Immutability

Once created, an outbox payload must not be modified.

Retries must publish the same logical event.

Changing the payload during retry would create ambiguity about which event occurred.

A correction to an already created event requires:

- a new event
- a new event ID
- an explicit corrective business or integration contract

---

# 22. Event Serialization

Serialization must be deterministic and tested.

Requirements include:

- stable field names
- explicit timestamp format
- explicit enum representation
- supported null behavior
- no direct aggregate serialization
- no JPA proxy serialization
- no broker-specific fields in Domain
- compatibility tests
- versioned public schema

Serialization failure must prevent the business transaction from committing when the outbox payload cannot be created safely.

---

# 23. Event Versioning

Each public integration event must have an explicit version.

Examples:

```text
OrderCreatedIntegrationEventV1

OrderCreatedIntegrationEventV2
```

Versioning may be represented through:

- event-type suffix
- envelope version field
- destination version
- schema registry version

The selected method must remain consistent.

Breaking event changes require a new version.

---

# 24. Event Contract Compatibility

Backward-compatible changes may include:

- adding optional fields
- adding fields with safe defaults
- adding metadata ignored by older consumers

Potentially breaking changes include:

- removing fields
- renaming fields
- changing field types
- changing enum meaning
- changing nullability
- changing identifier format
- changing timestamp semantics

Breaking changes require explicit versioning and migration planning.

---

# 25. Dispatcher Responsibility

The outbox dispatcher is responsible for:

- selecting eligible records
- claiming work safely
- publishing to Kafka
- recording success
- recording failure
- calculating retry time
- limiting attempts
- exposing metrics
- supporting graceful shutdown
- avoiding unbounded memory use

The dispatcher must not implement business rules.

---

# 26. Polling Strategy

The default implementation uses database polling.

Example:

```text
Every configured interval:

Select eligible PENDING events

Claim a bounded batch

Publish each event

Update status
```

Polling configuration must include:

- polling interval
- batch size
- maximum attempts
- retry delay
- concurrency
- query timeout

All values must be externally configurable.

---

# 27. Batch Size

Dispatcher batch size must be bounded.

Example configuration:

```yaml
outbox:
  dispatcher:
    batch-size: 100
```

The selected value must consider:

- Kafka throughput
- transaction duration
- database load
- memory use
- event size
- retry behavior
- application replica count

A maximum allowed configuration should prevent accidental excessive values.

---

# 28. Polling Query

A typical PostgreSQL claim query may use:

```sql
SELECT *
FROM outbox_event
WHERE status = 'PENDING'
  AND next_attempt_at <= CURRENT_TIMESTAMP
ORDER BY created_at
FOR UPDATE SKIP LOCKED
LIMIT :batch_size;
```

`SKIP LOCKED` allows multiple dispatcher instances to claim different records.

The exact query must be validated through integration and load testing.

---

# 29. Claiming Records

Records must be claimed in a way that prevents concurrent dispatchers from publishing the same row simultaneously under normal operation.

Possible approaches include:

- `FOR UPDATE SKIP LOCKED`
- atomic status update with returned rows
- lease-based claiming
- dedicated single dispatcher

The platform should prefer a horizontally scalable database-locking strategy.

Duplicate publication must still be tolerated because a crash can occur after Kafka accepts a message but before the outbox row is marked as sent.

---

# 30. Dispatcher Transaction Scope

The dispatcher must avoid long database transactions.

Preferred sequence:

1. claim a bounded batch
2. commit the claim
3. publish outside the claim transaction
4. update individual or bounded result groups

An alternative may hold locks during publication for very small workloads, but this increases transaction duration and lock risk.

The selected implementation must document its crash semantics.

---

# 31. Publication Success

An event may be marked `SENT` only after Kafka acknowledges successful publication according to the configured producer semantics.

On success, update:

```text
status = SENT

sent_at = current timestamp

last_error = null
```

The update must be durable.

---

# 32. Publication Failure

When publication fails:

- increment `attempts`
- store a sanitized error summary
- calculate `next_attempt_at`
- retain the original event ID
- retain the original payload
- emit metrics and logs
- classify terminal versus retryable failure

The full sensitive exception must not be stored in the database.

---

# 33. Retry Policy

Retries must be:

- bounded
- delayed
- observable
- configurable
- failure-aware

A typical strategy is exponential backoff with a maximum delay.

Example:

```text
Attempt 1: 5 seconds

Attempt 2: 15 seconds

Attempt 3: 45 seconds

Attempt 4: 2 minutes

Attempt 5: 5 minutes
```

Jitter may be added to avoid synchronized retry storms.

---

# 34. Maximum Attempts

The dispatcher must define a maximum number of automatic attempts.

After the limit:

```text
status = FAILED
```

The event must remain available for:

- diagnosis
- controlled replay
- operational remediation
- audit

The system must not retry terminally failed events forever without visibility.

---

# 35. Retryable Failures

Examples of retryable failures may include:

- Kafka temporarily unavailable
- network timeout
- transient broker error
- temporary authentication infrastructure failure
- leader-election transition
- throttling

Failure classification must be based on actual client behavior and documented policies.

---

# 36. Non-Retryable Failures

Examples of potentially non-retryable failures include:

- invalid destination
- incompatible serialization
- permanently invalid credentials
- oversized event
- contract validation failure
- prohibited event type

These failures may move directly to `FAILED` or use a limited retry count depending on operational policy.

---

# 37. At-Least-Once Delivery

The platform explicitly accepts at-least-once publication.

Duplicate publication may occur when:

```text
Kafka accepts the event

↓

Dispatcher crashes before marking the row as SENT

↓

The event is retried after restart
```

Exactly-once business processing must not be assumed from Kafka producer configuration alone.

Consumer idempotency is mandatory.

---

# 38. Consumer Idempotency

Consumers must use the event ID to prevent duplicate side effects.

Possible approaches include:

- processed-event table
- inbox pattern
- aggregate version check
- unique business operation key
- idempotent upsert
- state-transition validation

The idempotency mechanism must be durable when duplicate side effects would be harmful.

---

# 39. Ordering

Global event ordering is not guaranteed.

Where per-aggregate ordering is required:

- use the aggregate ID as the Kafka message key
- preserve event creation order where practical
- include aggregate version or sequence
- consumers must detect stale events
- parallel dispatcher behavior must be reviewed

The platform must not assume ordering across unrelated aggregates.

---

# 40. Aggregate Version

An integration event may include aggregate version information.

Example:

```json
{
  "aggregateVersion": 7
}
```

This can support:

- stale-event detection
- ordering validation
- projection consistency
- duplicate handling

Aggregate version semantics must be documented.

---

# 41. Kafka Message Key

The recommended Kafka message key is the aggregate identifier.

Example:

```text
key = orderId
```

This supports partition-level ordering for events belonging to the same aggregate.

The dispatcher must not use a random key when aggregate ordering matters.

---

# 42. Kafka Headers

Kafka headers may include:

- event ID
- event type
- event version
- trace ID
- correlation ID
- producer
- content type

Headers must complement the event envelope rather than contain the only copy of critical contract information.

Broker headers belong to Infrastructure.

---

# 43. Traceability

The outbox record and published event should preserve trace or correlation metadata.

Recommended values include:

- trace ID
- correlation ID
- causation ID
- command ID
- request ID

These identifiers support end-to-end incident investigation.

Sensitive authentication tokens must never be propagated.

---

# 44. Causation Identifier

A causation identifier may represent the command or event that caused a new event.

Example:

```text
Command ID

↓

OrderApproved event
```

This supports causal-chain analysis across asynchronous workflows.

---

# 45. Outbox Repository Port

The Application layer may define an outbox persistence port.

Example:

```java
public interface OutboxEventRepository {

    void save(OutboxEvent event);
}
```

The concrete PostgreSQL implementation belongs to Infrastructure.

The Domain layer must not depend on the outbox repository.

---

# 46. Outbox Event Model

The internal outbox model should be immutable where practical.

Example:

```java
public record OutboxEvent(
        UUID id,
        String aggregateType,
        UUID aggregateId,
        String eventType,
        String destination,
        String payload,
        OutboxStatus status,
        int attempts,
        Instant nextAttemptAt,
        Instant createdAt,
        UUID traceId
) {
}
```

Persistence-specific annotations must remain in Infrastructure.

---

# 47. Event Mapping

Event mapping should be explicit.

Example:

```java
public final class OrderIntegrationEventMapper {

    public OrderApprovedIntegrationEventV1 map(
            OrderApproved domainEvent
    ) {
        return new OrderApprovedIntegrationEventV1(
                domainEvent.eventId(),
                domainEvent.orderId(),
                domainEvent.approvedBy(),
                domainEvent.occurredAt()
        );
    }
}
```

Avoid reflection-based generic event mapping for business-critical contracts unless strongly justified.

---

# 48. Outbox Creation Timing

The integration event must be created from committed business intent within the application transaction.

The payload should not depend on lazy-loading after transaction completion.

All required event data must be available before the outbox row is inserted.

---

# 49. Domain Event Collection

Aggregates may register domain events internally.

Example:

```java
order.approve(actorId, comment);

var domainEvents = order.releaseDomainEvents();
```

The Application layer may map relevant domain events to integration events.

Releasing events must not cause them to be silently lost if persistence fails.

The event lifecycle must be explicit.

---

# 50. Transaction Rollback

If the business transaction rolls back:

- aggregate changes must not persist
- outbox events must not persist
- no Kafka event should be published

This is the central consistency guarantee of the pattern.

---

# 51. Event Publication Latency

Outbox publication is asynchronous and introduces bounded latency.

Latency depends on:

- polling interval
- dispatcher backlog
- Kafka availability
- retry timing
- batch size
- database load

The platform must define an acceptable service-level objective for publication delay.

---

# 52. Publication SLO

Recommended operational indicators include:

- P50 outbox age
- P95 outbox age
- P99 outbox age
- oldest pending event age
- pending count
- failed count
- publication success rate
- retry rate

Thresholds must reflect business criticality.

---

# 53. Indexing Strategy

Recommended indexes may include:

```sql
CREATE INDEX idx_outbox_event_pending
    ON outbox_event (next_attempt_at, created_at)
    WHERE status = 'PENDING';
```

Additional indexes may support:

- aggregate traceability
- failed-event operations
- retention cleanup
- sent-event archival

Every index must be justified because outbox writes occur in business transactions.

---

# 54. Partial Indexes

PostgreSQL partial indexes are preferred for high-selectivity dispatcher queries.

Example:

```sql
CREATE INDEX idx_outbox_event_dispatch
    ON outbox_event (next_attempt_at, created_at)
    WHERE status = 'PENDING';
```

This reduces index size compared with indexing all historical sent events.

---

# 55. Retention

Sent events must not remain indefinitely without policy.

Retention may use:

- scheduled deletion
- archival table
- partitioning
- external object storage
- database-native partition removal

The retention period must consider:

- audit needs
- incident investigation
- replay requirements
- storage cost
- regulatory constraints

---

# 56. Cleanup

Cleanup must be:

- incremental
- observable
- bounded
- safe under concurrent dispatch
- indexed
- independent from critical request transactions

Avoid deleting millions of rows in one transaction.

Example:

```sql
DELETE FROM outbox_event
WHERE id IN (
    SELECT id
    FROM outbox_event
    WHERE status = 'SENT'
      AND sent_at < :cutoff
    ORDER BY sent_at
    LIMIT :batch_size
);
```

---

# 57. Partitioning

Partitioning may be considered when event volume justifies it.

Potential partition keys include:

- creation month
- sent timestamp
- status combined with time-based strategy

Partitioning requires separate design review because it affects:

- indexes
- cleanup
- constraints
- query plans
- migration complexity
- dispatcher queries

---

# 58. Failed Event Retention

Failed events should be retained longer than routine sent events unless legal or security constraints require otherwise.

They support:

- diagnosis
- replay
- root-cause analysis
- operational audit

Retention policy must not expose sensitive payloads beyond approved periods.

---

# 59. Manual Replay

The platform must support controlled replay of failed events.

Replay must:

- preserve the original event ID by default
- preserve the original payload
- record the operator or automated process
- reset retry metadata explicitly
- produce an audit trail
- avoid bypassing authorization
- avoid uncontrolled bulk replay

A new event ID should be used only when the business meaning is a new event rather than a retry.

---

# 60. Replay Safety

Before replay:

- verify the destination
- verify the contract remains supported
- verify consumer idempotency
- verify no harmful duplicate side effect exists
- verify the original business state still permits processing
- define replay scope
- monitor results

Replay is an operational action, not a normal application shortcut.

---

# 61. Poison Events

An event that repeatedly fails due to its content is a poison event.

The platform must:

- stop unbounded retry
- mark the event as failed
- expose the failure
- retain the sanitized error
- support controlled remediation
- avoid blocking unrelated events

One poison event must not block the entire dispatcher.

---

# 62. Dispatcher Scaling

Multiple dispatcher instances may run concurrently.

Scaling must account for:

- database claim strategy
- Kafka producer concurrency
- connection-pool size
- batch size
- poll frequency
- event ordering
- downstream broker capacity

Increasing replica count must not produce unbounded database polling.

---

# 63. Poll Coordination

Each dispatcher should use configurable jitter or scheduling variation where appropriate to avoid synchronized polling spikes.

Polling should back off when:

- no records are available
- the database is degraded
- Kafka is unavailable
- shutdown has begun

---

# 64. Database Connection Usage

Dispatcher concurrency must remain compatible with the PostgreSQL connection pool.

The number of concurrent claim and update operations must be bounded.

Outbox publication must not starve request processing of database connections.

---

# 65. Kafka Producer Configuration

Producer configuration must define:

- acknowledgements
- delivery timeout
- request timeout
- retry behavior
- idempotent-producer setting where appropriate
- compression
- maximum request size
- batching
- security
- serialization

Kafka producer idempotence may reduce duplicate broker writes during producer retries, but it does not remove the need for consumer idempotency.

---

# 66. Event Size

Integration events must have bounded size.

Large payloads increase:

- database storage
- transaction cost
- Kafka latency
- network usage
- retry cost
- consumer memory use

Large documents should be stored externally with an immutable reference where appropriate.

The outbox must not become a general file-transfer mechanism.

---

# 67. Sensitive Data

Outbox payloads must minimize sensitive information.

Do not include:

- access tokens
- passwords
- private keys
- unnecessary personal information
- complete confidential records when identifiers are sufficient
- internal stack traces

Data classification and retention requirements apply to outbox payloads.

---

# 68. Encryption

Database and broker transport must use approved encryption.

Payload-level encryption may be considered when:

- sensitive data is unavoidable
- multiple infrastructure operators have access
- regulatory requirements apply
- topic-level controls are insufficient

Encryption design must preserve consumer compatibility and operational diagnostics.

---

# 69. Access Control

Database access to the outbox table should be restricted.

The application requires:

- insert access for business transactions
- select and update access for the dispatcher
- delete access for cleanup where applicable

Operational replay access must be controlled and audited.

---

# 70. Logging

Dispatcher logs should include:

- event ID
- event type
- aggregate type
- aggregate ID
- destination
- attempt count
- elapsed time
- result
- trace ID

Logs must not include full sensitive payloads.

Successful publication should not create excessive per-event log volume at high throughput.

---

# 71. Metrics

Recommended metrics include:

```text
outbox.events.created

outbox.events.pending

outbox.events.sent

outbox.events.failed

outbox.publish.duration

outbox.publish.retries

outbox.oldest.pending.age

outbox.dispatch.batch.size

outbox.cleanup.deleted
```

Metric labels must avoid unbounded cardinality.

Do not use event ID or aggregate ID as metric labels.

---

# 72. Alerts

Alerts should cover:

- oldest pending event exceeds threshold
- pending backlog grows continuously
- failed event count increases
- dispatcher stops publishing
- Kafka publication error rate increases
- database claim query becomes slow
- cleanup stops functioning
- outbox table growth exceeds capacity threshold

Alerts must distinguish transient incidents from sustained failure.

---

# 73. Health Checks

Dispatcher health may expose:

- enabled state
- last successful dispatch
- current backlog
- oldest pending event age
- recent failure rate

The application liveness check must not fail solely because Kafka is temporarily unavailable.

Readiness behavior depends on whether the service can safely accept new work while publication is delayed.

---

# 74. Graceful Shutdown

On shutdown, the dispatcher must:

- stop claiming new records
- complete or cancel current bounded publications
- persist final status updates where safe
- close Kafka producer resources
- stop executors
- respect platform termination deadlines

Shutdown must not silently abandon claimed records permanently.

Lease or status recovery must handle interrupted processing.

---

# 75. Stuck Processing Records

When `PROCESSING` status or leases are used, the platform must recover records abandoned by crashed dispatchers.

Possible strategy:

```text
processing_started_at older than threshold

↓

Reset to PENDING
```

Recovery must avoid reclaiming legitimately long-running work.

---

# 76. Clock Handling

Outbox timestamps and retry calculations must use a controlled clock abstraction where application testing benefits.

Database current time may be used for claim eligibility to ensure consistency across replicas.

Time semantics must be explicit.

---

# 77. Scheduler Implementation

The dispatcher may use:

- Spring scheduling
- a managed executor
- a dedicated worker process
- a Kubernetes-managed worker deployment

If Spring scheduling is used in multiple replicas, database claiming must provide concurrency safety.

A single-instance assumption is not sufficient unless enforced operationally.

---

# 78. Dedicated Worker

A dedicated outbox worker may be introduced when:

- publication volume is high
- scaling characteristics differ from API traffic
- deployment independence is valuable
- failure isolation is required
- operational ownership differs

The worker must use the same event and schema contracts.

---

# 79. Application Availability During Broker Failure

The platform may continue accepting business transactions during temporary Kafka unavailability when:

- PostgreSQL remains available
- outbox capacity is sufficient
- backlog limits are monitored
- business policy permits delayed integration
- disk growth remains controlled

This decoupling is a major benefit of the pattern.

However, prolonged broker failure must trigger operational limits and alerts.

---

# 80. Backlog Protection

The system must define protection for excessive backlog.

Possible controls include:

- alerts
- rate limiting
- degraded-mode behavior
- admission control
- temporary rejection of selected operations
- storage-capacity thresholds
- broker recovery procedures

Backlog protection must be driven by business criticality.

---

# 81. Schema Evolution

Outbox schema evolution must use new Flyway migrations.

Mandatory rule:

> Never modify an existing migration.

Schema changes must remain compatible with:

- current application version
- new application version
- dispatcher version
- rolling deployment overlap

---

# 82. Adding Outbox Columns

New columns should generally be:

- nullable initially
- defaulted safely
- backward compatible
- populated by new application versions
- made mandatory only after backfill and compatibility validation

---

# 83. Changing Status Values

Changing status names or meanings is a breaking operational change.

A staged migration may require:

1. dispatcher support for old and new values
2. data migration
3. application rollout
4. removal of legacy status handling later

Status values must not be changed casually.

---

# 84. Event Destination Changes

Changing a topic or destination requires planning for:

- old consumers
- new consumers
- dual publication
- replay
- contract compatibility
- cutover monitoring

The destination stored in existing outbox rows must remain valid or be migrated explicitly.

---

# 85. Testing Strategy

Testing must cover:

- atomic aggregate and outbox persistence
- transaction rollback
- successful publication
- transient publication failure
- retry scheduling
- maximum attempts
- terminal failure
- duplicate publication
- concurrent dispatchers
- row claiming
- graceful shutdown
- serialization
- event versioning
- consumer idempotency
- cleanup
- migration compatibility

---

# 86. Unit Tests

Unit tests should validate:

- event mapping
- retry calculation
- failure classification
- status transitions
- payload construction
- validation
- event-envelope creation

Unit tests must not require Spring or Kafka where pure logic is under test.

---

# 87. Persistence Integration Tests

PostgreSQL Testcontainers must validate:

- outbox insertion
- transaction atomicity
- claim query behavior
- `SKIP LOCKED`
- partial indexes
- concurrent dispatchers
- retry eligibility
- cleanup queries
- optimistic or status-based claiming

H2 must not replace PostgreSQL for these tests.

---

# 88. Kafka Integration Tests

Kafka integration tests should validate:

- publication
- message key
- headers
- serialized payload
- retry behavior where practical
- producer acknowledgements
- destination mapping
- consumer duplicate handling

Tests may use Kafka-compatible Testcontainers infrastructure.

---

# 89. Atomicity Test

A required integration test should prove:

```text
When aggregate persistence succeeds but outbox insertion fails,
the aggregate transaction rolls back.
```

Another test should prove:

```text
When the business operation fails,
no outbox event is committed.
```

---

# 90. Crash Simulation

Tests should simulate important crash windows.

Examples:

- after claim and before publication
- after Kafka acknowledgement and before `SENT` update
- during status update
- during shutdown

The expected result must preserve eventual retry and idempotency.

---

# 91. Concurrency Test

A concurrency integration test should run multiple dispatchers against the same pending records.

It should verify:

- no normal simultaneous claim of the same row
- all eligible records are eventually processed
- no records remain permanently locked
- duplicate publication remains safely tolerated

Timing-only assertions and `Thread.sleep` should be avoided.

---

# 92. Consumer Contract Tests

Consumers should verify compatibility with published schemas.

Contract tests should validate:

- required fields
- optional fields
- version handling
- enum values
- timestamp format
- identifier format
- unknown-field behavior

---

# 93. Architecture Tests

Architecture tests should enforce:

- Domain does not depend on Kafka
- Domain does not depend on outbox persistence
- Kafka classes remain in Infrastructure
- outbox adapters implement inner-layer ports
- integration-event DTOs do not replace domain models
- controllers do not publish directly to Kafka
- application use cases do not depend on Kafka producer types

---

# 94. Failure Scenarios

The platform must explicitly support:

## Database Failure Before Commit

Result:

```text
No aggregate change

No outbox event
```

## Kafka Failure After Commit

Result:

```text
Aggregate committed

Outbox remains pending

Retry later
```

## Crash After Kafka Acknowledgement

Result:

```text
Event may be published again

Consumer idempotency prevents harmful duplicate effects
```

## Dispatcher Failure

Result:

```text
Business API may continue

Backlog accumulates

Monitoring detects delay
```

---

# 95. Operational Runbook

The platform must maintain an operational runbook covering:

- checking backlog
- identifying oldest pending events
- inspecting failed events
- validating Kafka availability
- replaying events
- disabling the dispatcher
- enabling the dispatcher
- cleaning sent events
- recovering stuck records
- handling schema migration issues
- escalating storage risk

---

# 96. Manual Dispatcher Disablement

The dispatcher may support an operational enablement flag.

Example:

```yaml
outbox:
  dispatcher:
    enabled: true
```

Disabling publication must not disable outbox insertion.

Business transactions should continue recording publication intent unless business policy requires otherwise.

---

# 97. Disaster Recovery

Database recovery must preserve:

- aggregate state
- outbox state
- event IDs
- statuses
- retry metadata

After restore, the dispatcher may republish events that were already published before the backup point.

Consumer idempotency is therefore also required for disaster-recovery scenarios.

---

# 98. Multi-Region Considerations

Multi-region active-active outbox processing requires separate architecture analysis.

Concerns include:

- database write ownership
- duplicate dispatch
- aggregate ordering
- Kafka destination topology
- clock consistency
- regional failover
- event ID uniqueness

The initial platform assumes one authoritative transactional database region per bounded context.

---

# 99. Anti-Patterns

The following are prohibited:

- publishing directly to Kafka inside domain objects
- assuming database and Kafka operations are one atomic transaction
- deleting outbox rows immediately after publication without retention policy
- changing the event ID on retry
- rebuilding payloads from current mutable aggregate state
- storing authentication tokens in event payloads
- retrying forever without terminal visibility
- using unbounded dispatcher batches
- processing outbox records without concurrency control
- assuming exactly-once business processing
- omitting consumer idempotency
- modifying an applied Flyway migration
- exposing broker-specific types to the Domain layer
- performing long external calls inside the business transaction
- silently discarding failed events

---

# 100. Positive Consequences

The decision provides:

- atomic business-state and publication-intent persistence
- reliable event publication
- crash recovery
- temporary broker-failure tolerance
- clear retry state
- operational visibility
- horizontal dispatcher scalability
- auditable event history
- clean separation between domain and broker
- compatibility with eventual consistency
- removal of distributed-transaction requirements
- support for event replay
- strong integration with PostgreSQL
- support for rolling deployment
- improved incident diagnosis

---

# 101. Negative Consequences

The decision introduces:

- additional database writes
- outbox-table growth
- dispatcher implementation
- retry complexity
- cleanup requirements
- at-least-once delivery
- duplicate-event handling
- publication latency
- operational monitoring
- additional integration tests
- event-schema governance
- capacity planning

These costs are accepted because reliable event publication is essential to platform consistency.

---

# 102. Neutral Consequences

The decision also means:

- Kafka publication becomes asynchronous
- consumers must be idempotent
- database commit does not imply immediate downstream processing
- event contracts become versioned public artifacts
- outbox retention becomes an operational responsibility
- replay becomes possible but must be controlled
- database availability remains required for business transaction acceptance
- Kafka outages may increase database backlog

---

# 103. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate event publication | High | High | Require consumer idempotency |
| Dispatcher backlog grows | High | Medium | Monitor age, count and capacity |
| Outbox table grows indefinitely | High | Medium | Implement retention and cleanup |
| Claim query causes contention | Medium | Medium | Use bounded batches and `SKIP LOCKED` |
| Event contract breaks consumers | High | Medium | Version contracts and use compatibility tests |
| Poison event retries forever | High | Medium | Limit attempts and mark terminal failure |
| Event payload exposes sensitive data | High | Low | Minimize payload and apply security review |
| Kafka accepts event before status update fails | High | Medium | Accept duplicate and use idempotent consumers |
| Dispatcher overloads database | High | Medium | Bound polling, concurrency and connections |
| Dispatcher overloads Kafka | Medium | Medium | Configure producer and batch limits |
| Incorrect ordering affects projections | High | Medium | Use aggregate key and version checks |
| Manual replay causes duplicate side effects | High | Low | Use controlled replay and idempotency validation |
| Cleanup blocks production workload | Medium | Medium | Delete incrementally with indexes |
| Migration breaks dispatcher compatibility | High | Low | Use backward-compatible Flyway migrations |
| Event serialization fails in production | High | Low | Serialize before commit and use contract tests |
| Failed records are ignored operationally | High | Medium | Alert on failed count and oldest event age |

---

# 104. Implementation Guidance

The following rules are mandatory:

1. Business state and outbox records must persist in the same PostgreSQL transaction.
2. Direct Kafka publication must not replace the outbox for transactional business events.
3. Domain events and integration events must remain separate.
4. The Domain layer must remain Kafka-independent.
5. Every event must have a stable unique event ID.
6. Event payloads must remain immutable after creation.
7. Integration-event contracts must be versioned.
8. Dispatcher batches must be bounded.
9. Dispatcher concurrency must be controlled.
10. Multiple dispatcher instances must claim rows safely.
11. Publication failures must be persisted.
12. Retries must be bounded and delayed.
13. Terminal failures must remain visible.
14. Consumers must be idempotent.
15. Per-aggregate ordering must use an appropriate Kafka key when required.
16. Outbox metrics and alerts are mandatory.
17. Sent-event retention must be defined.
18. Cleanup must be incremental.
19. Replay must be controlled and auditable.
20. Sensitive data must be minimized.
21. Outbox migrations must use new Flyway versions.
22. Applied migrations must never be modified.
23. Integration tests must use PostgreSQL.
24. Crash and duplicate scenarios must be tested.
25. Operational runbooks must be maintained.

---

# 105. Validation

The decision will be validated through:

- PostgreSQL transaction tests
- Flyway migration tests
- outbox repository integration tests
- concurrent dispatcher tests
- Kafka publication tests
- consumer idempotency tests
- event-contract tests
- retry-policy tests
- failure-injection tests
- cleanup tests
- metrics review
- load testing
- backlog recovery testing
- disaster-recovery rehearsal
- architecture tests
- production-readiness review

---

# 106. Success Criteria

The decision is successful when:

- aggregate changes and outbox records commit atomically
- no committed business event is permanently lost during temporary Kafka failure
- dispatch resumes after application restart
- duplicate events do not cause duplicate business side effects
- multiple dispatcher instances operate safely
- pending-event age remains within the defined SLO
- failed events are visible and diagnosable
- event contracts remain backward compatible
- outbox-table growth remains controlled
- cleanup does not degrade transactional workloads
- Domain code remains independent from Kafka
- production incidents can be traced using event IDs
- replay can be performed safely
- schema evolution remains backward compatible
- operational teams have actionable metrics and runbooks

---

# 107. Alternatives Rejected

## 107.1 Direct Publication After Commit

Rejected because an application crash or broker failure after database commit can permanently lose the integration event.

---

## 107.2 Publication Before Commit

Rejected because consumers may receive an event for a transaction that later rolls back.

---

## 107.3 Distributed Transaction

Rejected because the operational complexity, availability impact and infrastructure coupling are not justified.

---

## 107.4 Change Data Capture as the Primary Mechanism

Rejected for the initial platform because application-defined integration events must represent explicit business intent rather than arbitrary table changes.

CDC may be reconsidered for specific data-replication or analytical use cases through a separate ADR.

---

## 107.5 Polling Business Tables

Rejected because it couples integration logic to mutable persistence structures and cannot reliably reconstruct all business events.

---

# 108. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Kafka for Integration Events
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Use OpenTelemetry for Distributed Tracing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-017: Use Optimistic Locking for Aggregate Concurrency

---

# 109. References

- Transactional Outbox Pattern
- Microservices Patterns
- PostgreSQL Documentation
- PostgreSQL Row-Level Locking Documentation
- Apache Kafka Producer Documentation
- Spring for Apache Kafka Documentation
- Flyway Documentation
- Testcontainers PostgreSQL Documentation
- Enterprise Order Platform Messaging Architecture
- Enterprise Order Platform Domain Events
- Enterprise Order Platform Transactional Outbox Guide
- Enterprise Order Platform Messaging Guidelines
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations

---

# 110. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial reliable event-publication baseline |

---

# 111. Decision Summary

The Enterprise Order Platform adopts the Transactional Outbox pattern for reliable integration-event publication.

Every relevant business transaction will atomically persist:

```text
Aggregate state

+

Integration-event publication intent
```

A separate dispatcher will publish pending events to Kafka and persist the publication result.

The platform accepts:

```text
At-least-once delivery

Eventual consistency

Possible duplicate publication
```

Therefore:

```text
Consumers must be idempotent

Events must have stable unique identifiers

Retries must be bounded

Failures must remain observable

Outbox retention must be controlled
```

This decision provides reliable event publication without distributed transactions while preserving Clean Architecture, Domain-Driven Design and bounded-context autonomy.
