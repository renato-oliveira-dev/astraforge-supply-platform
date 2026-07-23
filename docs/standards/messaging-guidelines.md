# Messaging Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Messaging Guidelines |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the messaging standards adopted by the Enterprise Order Platform.

It establishes rules for:

- event-driven architecture
- message contracts
- event envelopes
- Kafka
- RabbitMQ
- Amazon SQS
- producers
- consumers
- transactional outbox
- idempotency
- ordering
- partitioning
- retries
- dead-letter handling
- replay
- schema evolution
- observability
- security
- testing
- operational governance

The objective is to ensure that asynchronous communication remains reliable, traceable, backward compatible and operationally manageable.

---

# 2. Core Principles

Messaging must be:

- asynchronous by intent
- contract-driven
- idempotent
- observable
- resilient
- versioned
- secure
- replayable when required
- independent from internal persistence models
- tolerant of duplicate delivery

Messages must represent stable integration contracts rather than serialized implementation details.

---

# 3. Messaging Use Cases

Messaging is appropriate for:

- domain event distribution
- integration event distribution
- asynchronous workflows
- eventual consistency
- long-running operations
- decoupled notifications
- audit event propagation
- data synchronization
- workload buffering
- retryable background processing

Messaging should not be used only to avoid defining a clear synchronous contract.

---

# 4. Synchronous Versus Asynchronous Communication

Use synchronous communication when:

- the caller requires an immediate result
- the operation is short
- consistency is required before continuing
- the dependency is part of the same user interaction

Use asynchronous communication when:

- immediate completion is unnecessary
- processing may take a long time
- temporary dependency unavailability must be tolerated
- workloads must be buffered
- several consumers react independently
- eventual consistency is acceptable

The communication model must reflect business semantics.

---

# 5. Messaging Technologies

The platform may use:

| Technology | Preferred use |
|---|---|
| Kafka | Durable event streaming and multiple independent consumers |
| RabbitMQ | Routing-oriented messaging and work queues |
| Amazon SQS | Managed queue-based asynchronous processing |
| Transactional Outbox | Reliable publication after database transactions |

Technology selection must follow the use case rather than team preference.

---

# 6. Event Types

The platform distinguishes:

- domain events
- integration events
- commands
- notifications

Each type has different semantics and ownership.

---

# 7. Domain Events

Domain events represent facts that occurred inside a bounded context.

Examples:

```text
OrderCreated

OrderApproved

OrderCancelled

InventoryReserved

PaymentAuthorized
```

Domain events:

- use past-tense names
- are created by domain behavior
- remain independent from brokers
- may contain domain-specific types internally
- are not automatically public integration contracts

---

# 8. Integration Events

Integration events represent facts published for external consumers.

Examples:

```text
OrderCreatedIntegrationEvent

OrderApprovalCompletedIntegrationEvent

InventoryReservationFailedIntegrationEvent
```

Integration events must:

- use stable external schemas
- avoid internal domain implementation details
- be versioned
- include standard metadata
- support backward-compatible evolution

---

# 9. Commands

Commands request that a consumer perform an action.

Examples:

```text
ReserveInventoryCommand

AuthorizePaymentCommand

GenerateOrderDocumentCommand
```

Commands use imperative names.

Commands differ from events because they express intent rather than a completed fact.

---

# 10. Notifications

Notifications communicate informational outcomes with no required business response.

Examples:

```text
SendOrderApprovedEmailNotification

PublishOrderStatusNotification
```

Notifications must not be used for critical state transitions unless delivery guarantees and failure handling are explicit.

---

# 11. Event Naming

Domain and integration events use past tense.

Good:

```text
OrderCreated

PaymentAuthorized

InventoryReservationFailed
```

Avoid:

```text
CreateOrder

ProcessPayment

ReserveInventory
```

Those names represent commands.

---

# 12. Event Type Names

External event type values use uppercase snake case.

Examples:

```text
ORDER_CREATED

ORDER_APPROVED

PAYMENT_AUTHORIZED

INVENTORY_RESERVATION_FAILED
```

Event type values are stable public contracts.

---

# 13. Message Envelope

Every integration message must use a standard envelope.

Recommended structure:

```json
{
  "eventId": "11111111-1111-1111-1111-111111111111",
  "eventType": "ORDER_CREATED",
  "eventVersion": 1,
  "occurredAt": "2026-07-23T14:30:00Z",
  "producer": "order-service",
  "aggregateType": "ORDER",
  "aggregateId": "22222222-2222-2222-2222-222222222222",
  "correlationId": "33333333-3333-3333-3333-333333333333",
  "causationId": "44444444-4444-4444-4444-444444444444",
  "traceId": "f64c7f8ac1304c42",
  "payload": {
    "orderId": "22222222-2222-2222-2222-222222222222",
    "customerId": "55555555-5555-5555-5555-555555555555",
    "status": "CREATED"
  }
}
```

---

# 14. Envelope Fields

| Field | Required | Description |
|---|---:|---|
| `eventId` | Yes | Unique message identifier |
| `eventType` | Yes | Stable event type |
| `eventVersion` | Yes | Schema version |
| `occurredAt` | Yes | Business event timestamp |
| `producer` | Yes | Producing service |
| `aggregateType` | Yes | Aggregate category |
| `aggregateId` | Yes | Aggregate identifier |
| `correlationId` | Yes | Distributed business correlation |
| `causationId` | No | Identifier of the message or request that caused this event |
| `traceId` | No | Distributed tracing identifier |
| `payload` | Yes | Event-specific data |

---

# 15. Event Identifier

`eventId` must:

- be globally unique
- remain stable across retries
- remain stable across outbox publication attempts
- be used for consumer idempotency
- be included in logs and dead-letter records

Do not generate a new event identifier for every retry.

---

# 16. Correlation Identifier

`correlationId` groups messages belonging to the same business flow.

Examples:

- order creation workflow
- payment authorization flow
- order approval saga
- inventory reservation process

The correlation identifier must be propagated across synchronous and asynchronous boundaries.

---

# 17. Causation Identifier

`causationId` identifies the direct trigger of a message.

Example:

```text
OrderCreated event

↓

ReserveInventory command

↓

InventoryReserved event
```

The `ReserveInventory` command uses the `OrderCreated` event ID as its causation identifier.

This enables causal-chain reconstruction.

---

# 18. Event Timestamp

`occurredAt` represents when the business event occurred.

It must not represent:

- broker publication time
- consumer processing time
- outbox dispatch time

Transport timestamps may be captured separately.

Use UTC in ISO 8601 format.

---

# 19. Producer Identification

The producer field should contain a stable service identifier.

Examples:

```text
order-service

inventory-service

payment-service
```

Avoid environment-specific values:

```text
order-service-pod-6c7869

order-service-prod-node-2
```

Runtime instance data belongs in logs and headers, not the business event contract.

---

# 20. Event Payload

The payload must contain only data required by legitimate consumers.

Avoid publishing complete aggregate snapshots by default.

Include:

- identifiers
- state relevant to the event
- immutable business facts
- data needed to avoid unnecessary synchronous calls

Avoid including:

- secrets
- access tokens
- internal persistence fields
- unnecessary personal data
- lazy-loaded object graphs

---

# 21. Payload Design

Payloads should be:

- explicit
- immutable
- minimal
- consumer-oriented
- backward compatible
- independent from JPA entities
- independent from API request DTOs

Create dedicated event payload models.

---

# 22. Event Granularity

Events should represent one meaningful business fact.

Good:

```text
OrderApproved

OrderCancelled
```

Avoid generic events:

```text
OrderChanged

EntityUpdated

DataModified
```

Generic events force consumers to infer meaning from payload differences.

---

# 23. Event Immutability

Published events are immutable historical facts.

They must not be modified after publication.

Corrections require:

- a compensating event
- a new corrected event
- a new event version when the schema changes

Do not edit broker history or previously persisted outbox payloads.

---

# 24. Event Versioning

Every event must declare an integer version.

Example:

```json
{
  "eventType": "ORDER_CREATED",
  "eventVersion": 2
}
```

Version increments are required for incompatible schema changes.

---

# 25. Compatible Event Changes

Usually backward-compatible:

- adding an optional field
- adding metadata
- adding an optional nested object
- increasing a field length without semantic change
- adding a new event type

Consumers must ignore unknown fields.

---

# 26. Breaking Event Changes

Breaking changes include:

- removing a field
- renaming a field
- changing a field type
- making an optional field mandatory
- changing enum semantics
- changing timestamp format
- changing identifier format
- moving fields to another structure
- changing nullability

Breaking changes require a new event version.

---

# 27. Version Transition

During migration, producers may:

- publish both versions
- publish a new version to a new topic
- use one envelope with version-aware consumers
- introduce an adapter or translator

The transition plan must define:

- active consumers
- migration deadline
- rollback strategy
- replay compatibility
- deprecation date

---

# 28. Schema Registry

Kafka events should use a schema registry when schema governance is required.

Possible formats:

- Avro
- JSON Schema
- Protobuf

Schema selection must consider:

- compatibility validation
- generated code
- ecosystem support
- schema evolution
- payload readability
- operational tooling

Plain JSON may be used when automated schema compatibility is still enforced.

---

# 29. Schema Compatibility

Recommended compatibility mode:

```text
BACKWARD
```

or:

```text
BACKWARD_TRANSITIVE
```

A new producer schema must remain consumable by existing compatible consumers.

The chosen compatibility mode must be documented per event family.

---

# 30. Serialization

Serialization must be deterministic.

The platform standard for JSON messaging includes:

- UTF-8
- ISO 8601 timestamps
- stable property names
- string enum values
- no polymorphic class metadata
- no Java class names in payloads
- no default timezone dependence

---

# 31. Deserialization

Consumers must:

- validate required metadata
- validate event type
- validate supported version
- reject malformed payloads
- ignore unknown optional fields
- avoid trusting producer-controlled class metadata

Deserialization errors are normally non-retryable.

---

# 32. Kafka Topic Naming

Kafka topics use lowercase dot-separated names.

Recommended pattern:

```text
<domain>.<entity>.<event>
```

Examples:

```text
orders.order.created

orders.order.approved

inventory.reservation.completed

payments.authorization.failed
```

Environment names should not be embedded in logical topic names when deployment infrastructure already provides environment isolation.

---

# 33. Topic Ownership

Each topic must have an owning bounded context.

The owner is responsible for:

- schema
- compatibility
- retention
- partitioning
- security
- documentation
- deprecation
- consumer communication

Consumers must not redefine producer contracts.

---

# 34. Topic Granularity

Possible strategies:

- one topic per event family
- one topic per aggregate
- one topic per event type

The platform should prefer event-family or aggregate topics unless operational isolation requires separate topics.

Example:

```text
orders.order-events
```

with several event types may be suitable when ordering across the aggregate is required.

---

# 35. Kafka Keys

Kafka keys determine partitioning and ordering.

For aggregate events, use:

```text
aggregateId
```

Example:

```text
key = orderId
```

All events for the same aggregate should use the same key when ordering is required.

---

# 36. Partitioning

Partition count must consider:

- expected throughput
- consumer parallelism
- key distribution
- ordering requirements
- future growth
- broker capacity

Increasing partitions may change key-to-partition allocation and affect global ordering assumptions.

---

# 37. Ordering

Kafka guarantees ordering only within a partition.

The platform must not assume:

- global topic ordering
- ordering across different keys
- ordering across independent topics
- processing completion order across consumer instances

When aggregate ordering matters, use the aggregate identifier as the key.

---

# 38. Out-of-Order Events

Consumers must consider delayed or out-of-order delivery.

Possible protections:

- aggregate version
- event sequence
- occurred-at comparison
- state validation
- idempotent state transitions
- stale-event rejection

Do not rely only on wall-clock timestamps when strict ordering is required.

---

# 39. Aggregate Sequence

Events requiring strict aggregate evolution may include:

```text
aggregateVersion
```

Example:

```json
{
  "aggregateId": "22222222-2222-2222-2222-222222222222",
  "aggregateVersion": 8
}
```

Consumers may reject or defer events when a previous version is missing.

---

# 40. Kafka Consumer Groups

Consumer group names use lowercase kebab-case.

Recommended pattern:

```text
<service>-<purpose>
```

Examples:

```text
inventory-service-order-events

notification-service-order-events

analytics-service-order-events
```

Different independent consumers must use different groups.

---

# 41. Consumer Group Semantics

Consumers in the same group share work.

Consumers in different groups each receive the message independently.

Do not place unrelated business consumers in the same group.

Doing so causes messages to be distributed instead of broadcast.

---

# 42. Kafka Producer Configuration

Producers should use reliable defaults.

Recommended considerations:

- acknowledgements
- retries
- delivery timeout
- request timeout
- idempotent producer
- compression
- batching
- maximum in-flight requests

Critical event publication should use:

```text
acks=all
```

and producer idempotence when supported.

---

# 43. Kafka Consumer Configuration

Consumers must define:

- group ID
- offset reset policy
- maximum poll records
- poll interval
- session timeout
- heartbeat interval
- manual or automatic acknowledgment
- retry strategy
- dead-letter strategy

Defaults must not be accepted without review.

---

# 44. Offset Management

Offsets must be committed only after successful processing according to the delivery strategy.

Avoid committing before:

- persistence
- idempotency registration
- required side effects
- transactional completion

Premature commits can cause message loss.

---

# 45. At-Least-Once Delivery

The default delivery assumption is:

```text
at least once
```

This means:

- messages may be delivered more than once
- consumers must be idempotent
- duplicates are normal operational behavior
- side effects require deduplication

Do not design consumers assuming exactly-once business processing.

---

# 46. Exactly-Once Semantics

Broker-level exactly-once features do not guarantee exactly-once business effects across:

- databases
- external APIs
- emails
- payment providers
- multiple services

Business idempotency remains required.

---

# 47. Consumer Idempotency

Consumers should persist processed event identifiers.

Example table:

```sql
processed_message
```

Suggested fields:

```text
consumer_name

event_id

processed_at

payload_hash
```

Recommended unique constraint:

```text
consumer_name + event_id
```

---

# 48. Idempotent Consumer Flow

Recommended flow:

1. receive message
2. start transaction
3. verify processed event identifier
4. skip if already processed
5. execute business operation
6. persist processed marker
7. commit transaction
8. acknowledge message

The marker and business state should be persisted atomically when possible.

---

# 49. Duplicate Message Handling

Duplicate messages should normally be acknowledged without reprocessing.

A duplicate log should include:

```text
eventId

consumer

eventType

outcome=DUPLICATE
```

Duplicate deliveries should not be logged as errors.

---

# 50. Transactional Outbox

The Transactional Outbox pattern is mandatory when a service must atomically:

- persist business state
- publish an integration event

The business change and outbox row must be stored in the same database transaction.

---

# 51. Outbox Record

Recommended outbox fields:

```text
id

aggregate_type

aggregate_id

event_type

event_version

destination

payload

status

attempts

next_attempt_at

last_error

created_at

sent_at

trace_id

correlation_id
```

---

# 52. Outbox Atomicity

Correct flow:

```text
Begin transaction

Persist aggregate

Persist outbox event

Commit transaction
```

Incorrect flow:

```text
Persist aggregate

Commit

Publish broker event
```

The incorrect flow may lose events after a process failure.

---

# 53. Outbox Status

Recommended statuses:

```text
PENDING

PROCESSING

SENT

FAILED

DEAD
```

Status transitions must be explicit and observable.

---

# 54. Outbox Dispatch

The dispatcher should:

- select eligible pending records
- claim records safely
- publish messages
- update attempts
- set sent timestamp
- schedule retries
- classify permanent failures
- support concurrent instances

---

# 55. Concurrent Outbox Dispatchers

Use database locking patterns such as:

```sql
FOR UPDATE SKIP LOCKED
```

when supported and appropriate.

This enables multiple dispatcher instances without processing the same row simultaneously.

Idempotent publication is still required.

---

# 56. Outbox Batch Size

Batch size must be configurable.

It should balance:

- transaction duration
- memory consumption
- broker throughput
- retry cost
- database lock duration

Avoid loading all pending events into memory.

---

# 57. Outbox Retry

Outbox retry policy should define:

- maximum attempts
- initial delay
- backoff
- jitter
- retryable exception types
- permanent failure types
- next-attempt calculation

Retries must not run in a tight loop.

---

# 58. Outbox Payload Immutability

Once inserted, an outbox payload must not be modified.

Retry processing must publish the same logical event.

If correction is required, create a new outbox event with a new event identifier.

---

# 59. Outbox Cleanup

Retention policy must define how long sent records remain available.

Consider:

- incident investigation
- replay
- audit
- storage cost
- compliance

Cleanup should be incremental and observable.

---

# 60. RabbitMQ Exchanges

RabbitMQ exchange names use lowercase dot-separated names.

Examples:

```text
orders.events

payments.commands

notifications.events
```

Exchange types must be selected intentionally:

- direct
- topic
- fanout
- headers

---

# 61. RabbitMQ Queues

Queue names use lowercase dot-separated names.

Recommended pattern:

```text
<consumer>.<purpose>
```

Examples:

```text
inventory.order-reservation

notifications.order-approved

billing.payment-authorized
```

Queues belong to consumers, not producers.

---

# 62. RabbitMQ Routing Keys

Routing keys should describe the event or command.

Examples:

```text
order.created

order.approved

payment.authorization.requested
```

Routing keys must remain stable integration contracts.

---

# 63. RabbitMQ Acknowledgment

Consumers must acknowledge messages only after successful processing.

Use:

- acknowledge
- reject
- requeue
- dead-letter

according to failure classification.

Avoid infinite immediate requeue loops.

---

# 64. SQS Queue Naming

SQS queue names use lowercase kebab-case when permitted.

Examples:

```text
order-approval-events

inventory-reservation-commands

order-events-dead-letter
```

FIFO queues use the required suffix:

```text
order-events.fifo
```

---

# 65. SQS Standard Queues

Standard queues provide:

- high throughput
- at-least-once delivery
- best-effort ordering

Consumers must be idempotent.

Do not assume message order.

---

# 66. SQS FIFO Queues

Use FIFO queues when:

- strict ordering is required
- deduplication semantics are useful
- throughput limitations are acceptable

Define:

- message group ID
- deduplication ID
- expected concurrency
- ordering scope

For order events:

```text
messageGroupId = orderId
```

may preserve per-order ordering.

---

# 67. SQS Visibility Timeout

Visibility timeout must exceed the normal processing time with an appropriate safety margin.

Long-running consumers may extend visibility when supported.

If the timeout is too short, duplicate concurrent processing may occur.

---

# 68. Message Retention

Retention must be configured according to:

- replay needs
- outage tolerance
- legal requirements
- storage cost
- recovery objectives

Retention differs from audit retention.

---

# 69. Retry Classification

Failures must be classified as:

- retryable
- non-retryable
- duplicate
- malformed
- unauthorized
- unsupported version
- poison message

Classification must drive broker behavior.

---

# 70. Retryable Failures

Examples:

- dependency timeout
- temporary database outage
- broker unavailability
- rate limit
- transient network failure
- temporary lock timeout

Retry attempts must be bounded.

---

# 71. Non-Retryable Failures

Examples:

- malformed payload
- unsupported event type
- unsupported event version
- missing required field
- permanent business rejection
- invalid authentication configuration
- serialization incompatibility

These failures should normally be routed to dead-letter handling.

---

# 72. Retry Backoff

Use exponential backoff with jitter where practical.

Example sequence:

```text
5 seconds

30 seconds

2 minutes

10 minutes

30 minutes
```

The exact policy depends on business urgency and dependency recovery characteristics.

---

# 73. Retry Limits

Every retry mechanism must define a maximum attempt count.

Infinite retry is prohibited.

After exhaustion, the message must transition to a controlled failure state.

---

# 74. Dead-Letter Queue

Every critical queue or consumer must define a dead-letter strategy.

Dead-letter records should preserve:

- original message
- destination
- event ID
- event type
- event version
- consumer
- attempt count
- failure code
- sanitized failure message
- first failure timestamp
- final failure timestamp
- correlation ID
- trace ID

---

# 75. Dead-Letter Naming

Recommended patterns:

Kafka:

```text
<source-topic>.dlq
```

RabbitMQ:

```text
<queue>.dead-letter
```

SQS:

```text
<source-queue>-dlq
```

Names must make the source relationship clear.

---

# 76. Dead-Letter Processing

Dead-letter messages require:

- monitoring
- ownership
- alerting
- investigation procedure
- correction procedure
- replay procedure
- retention policy

A DLQ is not a permanent storage solution.

---

# 77. Poison Messages

A poison message deterministically fails every processing attempt.

Examples:

- corrupt schema
- unsupported data
- invalid mandatory business identifier
- incompatible event version

Poison messages must not remain in active retry loops.

---

# 78. Replay

Replay is the controlled reprocessing of historical messages.

Replay may be used for:

- consumer recovery
- read-model rebuild
- corrected business processing
- migration
- disaster recovery

Replay must be planned and auditable.

---

# 79. Replay Safety

Before replay, verify:

- consumer idempotency
- external side effects
- retention availability
- schema compatibility
- destination capacity
- ordering
- rate limits
- downstream impact

Replaying payment or notification events may cause harmful duplicate effects without protections.

---

# 80. Replay Modes

Possible modes:

- original topic replay
- dedicated replay topic
- consumer offset reset
- DLQ redrive
- database-backed reprocessing
- outbox re-publication

The chosen mode must preserve traceability.

---

# 81. Replay Metadata

Replayed messages may include metadata such as:

```text
replay=true

replayId

originalEventId

replayedAt

replayedBy
```

The original event identifier should generally remain unchanged for idempotency.

---

# 82. Reprocessing

Reprocessing differs from replay when the original payload is transformed or corrected.

A corrected message should:

- receive a new event ID
- reference the original event
- document the correction reason
- preserve correlation context

---

# 83. Consumer Design

Consumers should remain thin adapters.

Responsibilities:

- deserialize
- validate metadata
- establish context
- invoke application use case
- acknowledge or classify failure
- record operational metrics

Consumers must not contain complex business rules.

---

# 84. Consumer Application Boundary

Preferred flow:

```text
Broker Listener

↓

Message Mapper

↓

Application Command

↓

Use Case
```

Avoid:

```text
Broker Listener

↓

Repository

↓

Direct database update
```

---

# 85. Producer Design

Producers should:

- receive an integration event model
- serialize using the standard envelope
- set transport headers
- publish to the configured destination
- translate broker failures
- emit metrics

Producers must not construct business events from persistence entities.

---

# 86. Transport Headers

Transport headers may include:

```text
eventId

eventType

eventVersion

correlationId

causationId

traceparent

contentType
```

The payload remains the authoritative event contract.

Headers should support routing and observability.

---

# 87. Header Limits

Headers must:

- be bounded in size
- avoid sensitive data
- avoid full payload duplication
- use stable names
- remain compatible with the broker

Do not store large serialized objects in headers.

---

# 88. Correlation Propagation

Consumers must restore correlation context before application processing.

The context should propagate to:

- logs
- outbound HTTP calls
- new messages
- audit records
- metrics where cardinality permits

MDC must be cleared after processing.

---

# 89. Distributed Tracing

Messaging instrumentation should propagate W3C Trace Context where supported.

Recommended fields:

```text
traceparent

tracestate
```

A consumer span should be linked to or continue the producer trace according to the tracing implementation.

---

# 90. Logging

Producer logs should include:

```text
event

eventId

eventType

destination

aggregateId

elapsedMs

outcome

traceId
```

Consumer logs should include:

```text
event

eventId

eventType

consumer

destination

partition

offset

attempt

elapsedMs

outcome

traceId
```

Do not log full payloads by default.

---

# 91. Messaging Metrics

Recommended producer metrics:

```text
messaging.publish.total

messaging.publish.failures

messaging.publish.duration

outbox.pending.count

outbox.dispatch.failures
```

Recommended consumer metrics:

```text
messaging.consume.total

messaging.consume.failures

messaging.consume.duration

messaging.duplicates

messaging.retry.total

messaging.dead_letter.total
```

---

# 92. Metric Tags

Controlled tags may include:

```text
destination

event_type

consumer

outcome

failure_type

retryable
```

Do not use:

- event ID
- aggregate ID
- correlation ID
- raw exception message

as metric tags.

---

# 93. Consumer Lag

Kafka consumers must monitor:

- current offset
- end offset
- lag
- processing rate
- rebalance frequency
- poll duration

Lag thresholds should reflect business recovery objectives.

---

# 94. Alerting

Alerts should cover:

- sustained consumer lag
- dead-letter growth
- outbox backlog
- retry exhaustion
- publication failure rate
- consumer failure rate
- unsupported version volume
- malformed message volume
- broker unavailability

Single expected business rejections should not trigger incidents.

---

# 95. Backpressure

Consumers must protect downstream dependencies.

Possible mechanisms:

- bounded concurrency
- batch size
- poll limits
- prefetch limits
- semaphore
- bulkhead
- queue visibility control
- rate limiting

Virtual threads do not remove the need to bound database connections or external calls.

---

# 96. Consumer Concurrency

Concurrency must consider:

- partition count
- queue semantics
- ordering requirements
- database capacity
- downstream limits
- idempotency design

More threads do not always produce more throughput.

---

# 97. Batch Consumption

Batch consumers may improve throughput.

Batch semantics must define:

- atomicity
- partial failure
- acknowledgment
- retry granularity
- ordering
- maximum batch size

A failed item must not force unnecessary duplicate processing of successful items unless the contract requires atomic batches.

---

# 98. Batch Publication

Batch producers should:

- preserve individual event IDs
- report partial failure
- avoid oversized requests
- respect broker limits
- maintain ordering where required

Do not combine unrelated events into one opaque payload only for throughput.

---

# 99. Message Size

Message payload size must be bounded.

Large messages increase:

- broker storage
- network cost
- latency
- consumer memory
- retry cost

For large documents, publish a storage reference instead of the complete object.

---

# 100. Claim Check Pattern

Use the Claim Check pattern for large payloads.

Recommended flow:

1. store document in object storage
2. calculate checksum
3. publish metadata and storage reference
4. authorize consumer access
5. process and expire according to retention

The message should include:

```text
objectKey

contentType

size

checksum
```

---

# 101. Security

Messaging security must include:

- encrypted transport
- authenticated producers
- authenticated consumers
- destination authorization
- least privilege
- secret rotation
- auditability

Broker credentials must not be embedded in code or messages.

---

# 102. Topic and Queue Authorization

Permissions should restrict:

- producer write access
- consumer read access
- administrative operations
- schema changes
- replay operations
- dead-letter access

A service should access only the destinations it requires.

---

# 103. Sensitive Data

Messages must not contain unnecessary sensitive information.

Prohibited unless explicitly required and protected:

- passwords
- tokens
- secrets
- full payment card data
- complete personal documents
- private authentication material

Data minimization applies to asynchronous contracts.

---

# 104. Encryption

Highly sensitive payload fields may require application-level encryption in addition to transport encryption.

Encryption design must consider:

- key rotation
- consumer authorization
- replay
- retention
- operational diagnosis
- schema evolution

Do not create custom cryptographic algorithms.

---

# 105. Retention and Privacy

Message retention must comply with:

- privacy requirements
- data classification
- legal retention
- deletion obligations
- audit requirements

Long broker retention may conflict with personal-data deletion obligations.

---

# 106. Contract Documentation

Every public event or command must document:

- owner
- destination
- event type
- version
- envelope
- payload schema
- field semantics
- ordering
- delivery guarantee
- retry behavior
- retention
- security classification
- consumers
- deprecation policy

---

# 107. Consumer Registry

Maintain a registry of known consumers.

Record:

- consumer name
- owner
- event versions
- criticality
- deployment independence
- contact
- migration status
- replay support

This is required for safe event evolution.

---

# 108. Message Catalog

The platform should maintain a message catalog.

Example entry:

```text
Event: ORDER_CREATED
Owner: order-service
Topic: orders.order-events
Version: 1
Key: orderId
Retention: 14 days
Classification: Internal
Consumers:
  - inventory-service
  - notification-service
```

---

# 109. Testing Strategy

Messaging tests should include:

- unit tests
- serialization tests
- consumer tests
- producer tests
- broker integration tests
- outbox integration tests
- idempotency tests
- retry tests
- dead-letter tests
- replay tests
- compatibility tests

---

# 110. Serialization Tests

Serialization tests must verify:

- exact field names
- event type
- version
- timestamp format
- enum representation
- required metadata
- payload shape
- absence of internal fields

Use the production-configured serializer.

---

# 111. Deserialization Tests

Deserialization tests must verify:

- valid current version
- unknown optional fields
- missing mandatory fields
- unsupported version
- malformed JSON
- invalid enum value
- invalid metadata

---

# 112. Producer Unit Tests

Producer tests should verify:

- destination
- key
- headers
- serialized event
- exception translation
- correlation propagation

Do not test the broker only through mocked internal library calls.

---

# 113. Consumer Unit Tests

Consumer tests should verify:

- mapping to application command
- use-case invocation
- duplicate handling
- failure classification
- unsupported version handling
- context propagation

Use real event payload objects where practical.

---

# 114. Kafka Integration Tests

Use a compatible Kafka Testcontainer for critical broker behavior.

Validate:

- publication
- consumption
- partition key
- headers
- consumer groups
- retry routing
- dead-letter routing
- offset behavior
- duplicate handling

---

# 115. RabbitMQ Integration Tests

Use RabbitMQ Testcontainers to validate:

- exchange declaration
- queue binding
- routing key
- acknowledgment
- requeue
- dead-letter exchange
- consumer retry behavior

---

# 116. SQS Integration Tests

Where AWS integration is critical, use a controlled test environment such as LocalStack when its fidelity is sufficient.

Validate:

- queue configuration
- visibility timeout
- message attributes
- redrive policy
- FIFO group behavior
- deduplication behavior

Production smoke tests may still be required for AWS-specific behavior.

---

# 117. Outbox Integration Tests

Required scenarios:

- aggregate and event commit atomically
- rollback removes both
- dispatcher selects pending events
- sent status is persisted
- failure increments attempts
- retry time is calculated
- concurrent dispatchers do not claim the same row
- exhausted events become dead
- payload remains unchanged

---

# 118. Idempotency Integration Tests

Required scenarios:

- first delivery processed
- duplicate delivery ignored
- duplicate during concurrent execution processed once
- business state and processed marker commit atomically
- rollback allows safe redelivery
- same event ID for another consumer remains independent

---

# 119. Retry Tests

Retry tests must validate:

- retryable exception retried
- permanent exception not retried
- maximum attempts
- backoff configuration
- final dead-letter routing
- attempt metadata

Do not use `Thread.sleep`.

Use Awaitility, controlled clocks or test-specific short policies.

---

# 120. Ordering Tests

Ordering tests should validate:

- same aggregate key routes consistently
- consumer processes aggregate versions correctly
- stale events are rejected or ignored
- missing sequence handling follows policy

Do not assert global ordering when the broker does not guarantee it.

---

# 121. Replay Tests

Replay tests should verify:

- idempotent consumer behavior
- no duplicate side effect
- schema compatibility
- original event ID preservation
- replay metadata
- rate-limited processing where required

---

# 122. Contract Compatibility Tests

CI must compare event schemas against released versions.

The pipeline should detect:

- removed fields
- incompatible type changes
- required-field additions
- enum compatibility risks
- invalid default changes
- unsupported version changes

Breaking changes require explicit approval and migration planning.

---

# 123. Failure Injection Tests

Critical messaging workflows should test:

- broker unavailable
- database unavailable
- serialization failure
- acknowledgment failure
- consumer crash after commit
- consumer crash before commit
- timeout
- dead-letter destination unavailable

These scenarios validate real delivery guarantees.

---

# 124. Consumer Crash Scenarios

Important crash boundaries:

```text
before business transaction

during business transaction

after commit but before acknowledgment

after acknowledgment
```

The design must remain safe under each boundary.

The post-commit, pre-acknowledgment scenario normally creates a duplicate and therefore requires idempotency.

---

# 125. Operational Runbooks

Runbooks should exist for:

- consumer lag
- broker outage
- outbox backlog
- dead-letter growth
- replay
- schema incompatibility
- poison message
- credential failure
- partition imbalance

Runbooks must identify owners and safe recovery steps.

---

# 126. Deployment Considerations

During deployment:

- old and new consumers may run simultaneously
- producers may publish new optional fields
- rebalances may occur
- in-flight messages may use older versions
- rolling upgrades may temporarily mix code versions

Contracts must support mixed-version operation.

---

# 127. Consumer Deployment

Consumers should tolerate both the current and immediately previous supported event versions during migration when practical.

Do not deploy a consumer that requires a producer change not yet available.

---

# 128. Producer Deployment

Producers must not publish an incompatible schema before consumers are ready.

Recommended order:

1. deploy tolerant consumers
2. enable new producer schema
3. observe migration
4. retire old consumer support
5. deprecate old schema

---

# 129. Feature Flags

Feature flags may control:

- new producer versions
- new consumer behavior
- dual publication
- new destinations
- replay functionality

Flags must have safe defaults and removal plans.

---

# 130. Dual Publication

Publishing the same logical event to two destinations may be used temporarily during migration.

Dual publication must define:

- event identity
- failure behavior
- consistency expectations
- rollback
- monitoring
- removal date

Avoid permanent dual publication without clear ownership.

---

# 131. Eventual Consistency

Asynchronous flows are eventually consistent.

APIs and user interfaces should communicate:

- pending state
- processing state
- final state
- failure state

Do not present asynchronous operations as immediately completed.

---

# 132. Saga Interaction

Saga workflows may use commands and events.

Example:

```text
OrderCreated

↓

ReserveInventoryCommand

↓

InventoryReserved

↓

AuthorizePaymentCommand

↓

PaymentAuthorized
```

Each step must be:

- idempotent
- observable
- compensatable when required
- correlated
- versioned

---

# 133. Compensation Events

Compensation represents a new business action.

Examples:

```text
InventoryReservationReleased

PaymentAuthorizationCancelled

OrderCreationCompensated
```

Compensation does not delete the original event history.

---

# 134. Choreography

Choreography is suitable when:

- consumers react independently
- workflow remains simple
- coupling is low
- ownership is clear

Excessive choreography may create hidden workflows.

Use orchestration when process visibility and control are more important.

---

# 135. Orchestration

An orchestrator should:

- track workflow state
- issue commands
- receive results
- manage timeouts
- trigger compensation
- preserve correlation
- support restart and recovery

The orchestrator must not become a general-purpose service containing all domain logic.

---

# 136. Timeout Events

Long-running workflows should represent timeouts explicitly.

Examples:

```text
PaymentAuthorizationTimedOut

InventoryReservationExpired
```

Timeout handling must be idempotent.

---

# 137. Delayed Messages

Delayed processing may use:

- retry topics
- RabbitMQ dead-letter exchanges
- SQS delay
- scheduled outbox
- scheduler-backed commands

The selected mechanism must define precision, scale and delivery guarantees.

---

# 138. Scheduled Messages

Scheduled messages should include:

```text
scheduledAt

expiresAt
```

when relevant.

Consumers must define behavior for late delivery.

---

# 139. Expired Messages

Messages that are no longer valid should not execute outdated business actions.

Examples:

- expired approval command
- outdated promotion recalculation
- obsolete inventory request

Use explicit expiration metadata and consumer validation.

---

# 140. Message Priorities

Priority queues should be used sparingly.

Priority may cause:

- starvation
- unpredictable throughput
- operational complexity
- fairness issues

Separate queues are often clearer for different criticality classes.

---

# 141. Anti-Patterns

The following practices are prohibited:

- publishing JPA entities
- assuming exactly-once business delivery
- generating a new event ID on retry
- infinite retry loops
- acknowledging before successful processing
- logging complete sensitive payloads
- using generic events such as `EntityChanged`
- changing event schemas without compatibility analysis
- publishing directly after database commit without an outbox when atomicity is required
- putting unrelated consumers in the same Kafka group
- assuming global ordering
- using broker retries instead of idempotency
- treating a DLQ as permanent archival storage
- replaying messages without side-effect analysis
- embedding business logic in broker listeners
- publishing oversized payloads
- exposing internal class names in event schemas
- modifying previously published historical events
- ignoring consumer lag
- deploying incompatible producers before consumers

---

# 142. Review Checklist

Before approving a message contract or consumer, verify:

- Is messaging appropriate for the use case?
- Is the message an event, command or notification?
- Is the name semantically correct?
- Is the envelope complete?
- Is the event ID stable?
- Is the correlation ID propagated?
- Is the payload minimal?
- Is sensitive data excluded?
- Is the version explicit?
- Is compatibility preserved?
- Is the destination owned?
- Is the partition key correct?
- Is ordering required?
- Is the consumer idempotent?
- Is retry classification explicit?
- Is the maximum retry count defined?
- Is dead-letter handling configured?
- Is replay safe?
- Are logs and metrics available?
- Are contract and integration tests present?
- Is operational ownership documented?

---

# 143. Architecture Rules

Messaging must:

- use explicit message semantics
- preserve stable contracts
- assume at-least-once delivery
- enforce consumer idempotency
- use Transactional Outbox when atomic publication is required
- propagate correlation and trace context
- classify failures deterministically
- bound retries
- route permanent failures to dead-letter handling
- preserve aggregate ordering where required
- support schema evolution
- remain observable
- protect sensitive information
- provide controlled replay

---

# 144. Decision Summary

The project adopts:

- domain events for internal business facts
- versioned integration events for external communication
- standard event envelopes
- stable event identifiers
- correlation and causation identifiers
- Kafka for durable event streaming
- RabbitMQ and SQS for queue-based processing when justified
- aggregate identifiers as partition keys
- at-least-once delivery assumptions
- idempotent consumers
- Transactional Outbox
- bounded retries with backoff
- explicit dead-letter handling
- controlled replay and reprocessing
- schema compatibility validation
- distributed tracing propagation
- structured messaging logs and metrics
- Testcontainers-based messaging integration tests
- documented message ownership and consumer registry
