# ADR-009: Use Apache Kafka for Integration Events

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-009 |
| Title | Use Apache Kafka for Integration Events |
| Status | Superseded |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Event-Driven Architecture |
| Related Work Items | Asynchronous bounded-context integration |
| Supersedes | None |
| Superseded By | ADR-090 |

---

# 1. Context

The Enterprise Order Platform contains multiple bounded contexts that must exchange business information asynchronously.

Examples of integration scenarios include:

- order creation
- order approval
- order rejection
- order cancellation
- inventory reservation
- inventory release
- payment authorization
- payment rejection
- customer validation
- fulfillment initiation
- audit propagation
- notification requests

These interactions must remain loosely coupled.

A producing bounded context must not require all consumers to be available during the originating business transaction.

The platform already adopts:

- Domain-Driven Design
- Clean Architecture
- Transactional Outbox
- at-least-once delivery
- eventual consistency
- idempotent consumers

A durable event-streaming platform is required to transport integration events between independently deployable services.

---

# 2. Problem Statement

The platform requires an asynchronous messaging technology that:

- reliably transports integration events
- supports high throughput
- supports horizontal scaling
- preserves ordering where required
- retains events for controlled replay
- supports multiple independent consumers
- supports partitioned processing
- supports failure recovery
- integrates with Java 21
- integrates with Spring Boot
- integrates with the Transactional Outbox pattern
- supports observability
- supports secure communication
- supports container and Kubernetes deployment
- supports versioned public event contracts

The decision must define how Kafka is used, what guarantees are expected and which usage patterns are prohibited.

---

# 3. Decision Drivers

The primary decision drivers are:

1. reliable asynchronous communication
2. loose coupling
3. durable event retention
4. consumer independence
5. scalability
6. partition-based ordering
7. replay capability
8. mature ecosystem
9. operational observability
10. Java and Spring integration
11. compatibility with at-least-once delivery
12. compatibility with Transactional Outbox
13. cloud and Kubernetes support
14. security
15. contract governance
16. failure recovery
17. long-term maintainability

---

# 4. Constraints

The decision must consider:

- PostgreSQL stores transactional business state
- Flyway manages database migrations
- the Transactional Outbox persists publication intent
- delivery semantics are at least once
- consumers must be idempotent
- Java 21 is the runtime baseline
- Spring Boot is the application framework
- multiple service replicas may run concurrently
- domain code must remain broker-independent
- event contracts must be versioned
- sensitive data must be minimized
- operational teams require metrics, alerts and runbooks
- workloads may be deployed on Kubernetes

---

# 5. Considered Options

## 5.1 Option A: Apache Kafka

Apache Kafka is a distributed event-streaming platform based on durable partitioned logs.

### Advantages

- high throughput
- durable retention
- replay support
- partition ordering
- consumer groups
- horizontal scalability
- strong ecosystem
- mature Java clients
- Spring integration
- broad cloud support
- extensive operational tooling
- suitable for event-driven architectures
- multiple independent consumers
- strong compatibility with the Transactional Outbox pattern

### Disadvantages

- operational complexity
- partitioning requires careful design
- duplicate delivery remains possible
- ordering is limited to partitions
- consumer lag requires monitoring
- schema evolution requires governance
- topic proliferation must be controlled
- capacity planning is required
- event replay can create operational risk

---

## 5.2 Option B: RabbitMQ

RabbitMQ is a message broker based on queues, exchanges and routing keys.

### Advantages

- mature messaging platform
- flexible routing
- good support for commands and work queues
- straightforward acknowledgement model
- broad protocol support
- strong operational tooling

### Disadvantages

- less natural for retained event streams
- replay is not a primary model
- independent consumer history is less convenient
- event-log semantics are weaker
- less suitable for long-lived integration-event retention
- high fan-out event streams require additional queue management

---

## 5.3 Option C: Cloud-Specific Messaging Service

Examples include:

- Amazon SNS and SQS
- Google Pub/Sub
- Azure Service Bus
- Azure Event Hubs

### Advantages

- managed operations
- platform integration
- reduced infrastructure management
- built-in scalability
- managed security integration

### Disadvantages

- stronger cloud-provider coupling
- behavioral differences across providers
- reduced infrastructure portability
- contract and operational standards become provider-specific
- migration between providers becomes more difficult

Managed Kafka offerings may still be used because they preserve the selected Kafka contract.

---

## 5.4 Option D: Direct Synchronous HTTP Integration

Services could invoke one another directly through REST APIs.

### Advantages

- immediate response
- simple request-response semantics
- easy local understanding
- suitable for synchronous validation

### Disadvantages

- runtime coupling
- cascading failures
- reduced availability
- no durable event history
- no replay
- producer depends on consumer availability
- fan-out becomes expensive
- temporal coupling
- unsuitable as the primary event-notification mechanism

---

## 5.5 Option E: Database-Based Messaging Only

Consumers could poll outbox or integration tables directly.

### Advantages

- no external broker
- transactional data remains centralized
- low initial infrastructure cost

### Disadvantages

- database becomes a shared integration bus
- weak bounded-context isolation
- consumer polling increases database load
- poor consumer independence
- weak scalability
- no native partitioned event streaming
- difficult multi-consumer coordination
- high schema coupling

---

# 6. Decision

The Enterprise Order Platform will use Apache Kafka as its standard broker for asynchronous integration events.

Kafka will transport versioned business integration events between bounded contexts.

The Transactional Outbox dispatcher will publish committed events to Kafka.

Kafka will not replace:

- PostgreSQL as the business system of record
- synchronous APIs where an immediate response is required
- domain events inside an aggregate
- business transactions
- consumer idempotency
- workflow or saga coordination logic

---

# 7. Rationale

Kafka provides the strongest alignment with the platform's requirements for:

- durable event transport
- replay
- consumer independence
- high throughput
- partition-based ordering
- horizontal scaling
- multi-consumer integration
- event-driven workflows

The platform requires more than transient queue delivery.

Consumers may need to:

- process events independently
- recover from downtime
- rebuild projections
- create new integrations from retained history
- replay selected event ranges
- scale processing through consumer groups

Kafka's durable log model directly supports these scenarios.

---

# 8. Architectural Model

```text
Business Transaction

↓

PostgreSQL
    Aggregate state
    Outbox event

↓

Outbox Dispatcher

↓

Kafka Topic

├── Consumer Group: Inventory
├── Consumer Group: Payments
├── Consumer Group: Notifications
├── Consumer Group: Audit
└── Consumer Group: Analytics
```

Each consumer group maintains an independent processing position.

---

# 9. Kafka Architectural Role

Kafka is an Infrastructure concern.

Kafka-specific types must not appear in the Domain layer.

The architecture should follow:

```text
Domain
    Business facts and invariants

Application
    Integration-event intent and ports

Infrastructure
    Kafka producer
    Kafka consumer
    serializers
    topic mapping
    headers
    retry and dead-letter configuration
```

---

# 10. Integration Events

Kafka topics carry integration events rather than internal domain objects.

An integration event must be:

- explicit
- immutable
- versioned
- serializable
- documented
- consumer-oriented
- independent from persistence entities
- independent from Java implementation details

Direct serialization of JPA entities or aggregates is prohibited.

---

# 11. Event Envelope

All integration events should use a standard envelope.

Example:

```json
{
  "eventId": "f69dc875-834c-4c79-a0bf-4ec3c3b069c6",
  "eventType": "ORDER_APPROVED",
  "eventVersion": 1,
  "producer": "orders-service",
  "aggregateType": "ORDER",
  "aggregateId": "f69f14af-f174-4b14-b9b3-28bd3d540ea0",
  "aggregateVersion": 7,
  "occurredAt": "2026-07-23T18:40:00Z",
  "traceId": "00f99be3-680d-446a-b73f-f8032277bb81",
  "correlationId": "7c196fc9-dd65-4a05-961e-854d27f80e58",
  "causationId": "ba2c522b-c120-4877-b576-f6e56bc75352",
  "payload": {
    "orderId": "f69f14af-f174-4b14-b9b3-28bd3d540ea0",
    "status": "APPROVED"
  }
}
```

---

# 12. Required Envelope Fields

Recommended mandatory metadata includes:

| Field | Purpose |
|---|---|
| `eventId` | Stable duplicate-detection identifier |
| `eventType` | Business event classification |
| `eventVersion` | Contract version |
| `producer` | Producing service or bounded context |
| `aggregateType` | Aggregate category |
| `aggregateId` | Aggregate instance |
| `occurredAt` | Business event timestamp |
| `payload` | Event-specific business data |

Recommended optional metadata includes:

- aggregate version
- trace ID
- correlation ID
- causation ID
- tenant ID where applicable
- schema identifier where applicable

---

# 13. Event Identifier

The event ID must:

- be globally unique
- remain unchanged across retries
- remain unchanged during broker redelivery
- be included in the outbox record
- be included in the Kafka message
- support idempotent consumer processing
- support incident investigation
- support controlled replay

The producer must not generate a new event ID for each publication attempt.

---

# 14. Topic Strategy

Topics should represent stable business event streams.

Preferred examples:

```text
orders.lifecycle.v1

payments.lifecycle.v1

inventory.reservations.v1
```

Avoid topics based only on technical implementation classes.

Poor example:

```text
order-service-events
```

A topic name should communicate:

- business area
- stream purpose
- contract generation where appropriate

---

# 15. Topic Naming Convention

Recommended structure:

```text
<bounded-context>.<business-stream>.v<major-version>
```

Examples:

```text
orders.lifecycle.v1

orders.approvals.v1

inventory.reservations.v1

payments.authorizations.v1
```

Topic names must use:

- lowercase characters
- dots between logical segments
- stable terminology
- explicit major version when topic-level versioning is used

---

# 16. Topic Granularity

The platform should avoid both extremes:

```text
One topic for every event type
```

and:

```text
One global topic for all platform events
```

Topic boundaries should consider:

- business ownership
- retention
- access control
- ordering
- throughput
- consumer interest
- schema compatibility
- operational independence

Multiple related event types may share one business stream when they have compatible ownership and lifecycle.

---

# 17. Topic Ownership

Every topic must have one authoritative producing bounded context.

The owner is responsible for:

- event contracts
- compatibility
- retention requirements
- partition-key semantics
- documentation
- access policy
- deprecation
- operational support

Multiple services must not publish competing definitions of the same event stream.

---

# 18. Event Type Within a Topic

A topic may contain multiple related event types.

Example:

```text
orders.lifecycle.v1

ORDER_CREATED
ORDER_APPROVED
ORDER_REJECTED
ORDER_CANCELLED
```

Consumers must route using the envelope event type.

Unrelated events must not be grouped merely to reduce topic count.

---

# 19. Partition Key

The default Kafka key for aggregate lifecycle events is the aggregate identifier.

Example:

```text
key = orderId
```

This provides partition-level ordering for events belonging to the same aggregate.

Alternative keys require documented justification.

---

# 20. Partition Ordering

Kafka guarantees ordering only within a partition.

Therefore:

```text
Events for one order

using the same orderId key

are routed consistently to one partition
```

The platform does not guarantee:

- global topic ordering
- ordering across aggregates
- ordering across topics
- processing-time ordering across consumer groups

---

# 21. Aggregate Version

Events that depend on ordered aggregate state should include an aggregate version or sequence.

Example:

```json
{
  "aggregateVersion": 8
}
```

Consumers may use it to:

- detect stale events
- detect missing transitions
- reject regressions
- rebuild ordered projections
- diagnose unexpected ordering

Version semantics must be monotonic per aggregate.

---

# 22. Partition Count

Partition count must be selected based on:

- expected throughput
- required consumer parallelism
- key distribution
- ordering needs
- future scale
- broker capacity
- replication cost

Partition count should not be selected arbitrarily.

Increasing partition count can change key-to-partition mapping and must be operationally reviewed.

---

# 23. Hot Partitions

A poor key may create hot partitions.

Examples include:

- constant key
- low-cardinality status
- one dominant customer identifier
- current date
- random use of null keys

Partition-key distribution must be validated using realistic traffic.

---

# 24. Null Keys

Null keys may distribute messages across partitions without per-aggregate ordering.

Null keys are permitted only when:

- ordering is irrelevant
- event grouping is unnecessary
- the consumer contract explicitly supports unordered processing

Aggregate lifecycle events should not use null keys.

---

# 25. Replication Factor

Production topics must use a replication factor appropriate to the Kafka cluster's durability requirements.

The value depends on:

- cluster size
- failure tolerance
- managed-service constraints
- availability target
- storage cost

A production topic must not rely on a single replica.

---

# 26. Minimum In-Sync Replicas

Broker and producer durability settings should align.

Relevant controls include:

```text
replication.factor

min.insync.replicas

acks
```

The combination must prevent acknowledgement when durability requirements are not met.

A typical production model uses:

```text
acks = all
```

with an appropriate minimum in-sync replica setting.

---

# 27. Producer Acknowledgements

The Kafka producer must use acknowledgements aligned with durable event publication.

Preferred:

```text
acks = all
```

The outbox record must be marked as sent only after successful broker acknowledgement.

---

# 28. Idempotent Producer

Kafka producer idempotence should be enabled where supported.

It helps prevent duplicates introduced by producer retries within a producer session.

However, it does not provide exactly-once business processing across:

- PostgreSQL
- outbox
- dispatcher
- Kafka
- consumer database

Consumer idempotency remains mandatory.

---

# 29. Producer Retries

Kafka-client retries must be coordinated with outbox retries.

The platform must avoid uncontrolled nested retry behavior.

Potential layers include:

- Kafka producer retries
- dispatcher retries
- resilience library retries
- scheduler retries

The total failure behavior must remain bounded and observable.

---

# 30. Producer Timeouts

Producer configuration must define explicit values for:

- delivery timeout
- request timeout
- metadata timeout
- maximum block time
- connection setup timeout

Framework defaults must not be accepted without review.

---

# 31. Message Size

Maximum event size must be controlled.

Large events increase:

- producer memory
- broker storage
- network cost
- consumer memory
- retry cost
- outbox storage
- replication load
- processing latency

Kafka must not be used for large binary documents.

Large artifacts should be stored externally and referenced through an immutable identifier and validated metadata.

---

# 32. Compression

Producer compression may be enabled based on measured workload.

Common algorithms include:

- lz4
- snappy
- zstd

Selection should consider:

- CPU cost
- network reduction
- event size
- broker support
- consumer compatibility

Compression must be benchmarked with representative payloads.

---

# 33. Batching

Producer batching may improve throughput.

Relevant settings include:

- batch size
- linger
- compression
- buffer memory

Batching must not violate required publication-latency objectives.

The outbox dispatcher should not create unbounded in-memory batches.

---

# 34. Serialization Format

JSON is the initial standard serialization format for integration events.

Reasons include:

- readability
- broad interoperability
- operational diagnostics
- mature Java support
- low adoption complexity
- alignment with existing API models

JSON serialization must remain explicit and contract-driven.

---

# 35. Schema Registry

A schema registry may be introduced when the platform requires stronger centralized contract enforcement.

Possible formats include:

- JSON Schema
- Avro
- Protobuf

Adoption requires a separate or amended decision covering:

- compatibility policy
- registry availability
- subject naming
- deployment sequencing
- generated models
- operational ownership

The initial JSON decision does not prevent future registry adoption.

---

# 36. Serialization Rules

Event serialization must define:

- property names
- date and time format
- number precision
- enum representation
- null handling
- unknown-field handling
- optional fields
- version behavior
- identifier format

Consumers must not depend on Java class names.

---

# 37. Timestamp Format

Timestamps must use ISO 8601 with UTC semantics.

Example:

```text
2026-07-23T18:40:00Z
```

Ambiguous local timestamps are prohibited in public event contracts.

---

# 38. Numeric Precision

Monetary values must not use binary floating-point types.

Events should represent monetary amounts using:

- decimal-compatible numeric values
- explicit currency
- documented scale and rounding

Example:

```json
{
  "amount": 1250.45,
  "currency": "BRL"
}
```

---

# 39. Enum Evolution

Event enum values are public contract values.

Rules include:

- use stable explicit names
- do not rename existing values
- do not change meaning
- consumers should handle unknown future values where practical
- removal requires a new contract version

Java enum ordinals must never be serialized.

---

# 40. Event Versioning

Every event must have an explicit major contract version.

A breaking change requires a new major version.

Possible strategies include:

- a new topic version
- a new event type version
- a new envelope version
- a new schema-registry version

The project must use a consistent strategy per event family.

---

# 41. Backward-Compatible Changes

Generally compatible changes include:

- adding optional fields
- adding metadata fields
- adding new event types to a compatible topic
- adding enum values when consumers tolerate unknown values
- adding fields with defined defaults

Compatibility must still be tested.

---

# 42. Breaking Changes

Potentially breaking changes include:

- removing fields
- renaming fields
- changing field types
- changing identifier format
- changing event semantics
- changing nullability
- changing monetary precision
- changing key strategy
- changing ordering assumptions
- changing enum meaning

Breaking changes require explicit migration planning.

---

# 43. Dual Publication

A breaking contract migration may require dual publication.

Example:

```text
orders.lifecycle.v1

and

orders.lifecycle.v2
```

Migration sequence:

1. create the new topic
2. publish both versions
3. migrate consumers
4. verify consumer adoption
5. stop publishing the old version
6. retain the old topic according to policy
7. decommission after approval

---

# 44. Consumer Groups

Each independent business capability must use its own consumer group.

Example:

```text
Topic: orders.lifecycle.v1

Group: inventory-order-events-v1

Group: payments-order-events-v1

Group: notifications-order-events-v1
```

Consumers that share one group divide the work.

Consumers in different groups each receive the event independently.

---

# 45. Consumer Group Naming

Recommended structure:

```text
<consumer-service>-<stream-purpose>-v<major-version>
```

Examples:

```text
inventory-service-orders-v1

payments-service-orders-v1

notifications-service-orders-v1
```

Names must remain stable across deployments.

Random consumer-group identifiers are prohibited for durable business consumers.

---

# 46. Consumer Scaling

Consumer parallelism is bounded by the number of topic partitions.

Adding replicas beyond the available partition count does not increase active processing parallelism.

Scaling decisions must consider:

- partition count
- processing time
- downstream capacity
- consumer lag
- database connection pool
- idempotency-store throughput

---

# 47. Offset Commit Strategy

Offsets must be committed only after successful processing according to the consumer's transaction model.

The platform must not acknowledge an event before required side effects are durable.

A typical safe sequence is:

```text
Receive event

↓

Validate contract

↓

Check idempotency

↓

Execute business operation

↓

Persist consumer result and processed-event record

↓

Commit transaction

↓

Commit Kafka offset
```

---

# 48. Auto-Commit

Automatic offset commit should be disabled for business-critical consumers.

Reason:

- the offset may advance before processing is complete
- application crashes may lose business processing
- error handling becomes unclear

Acknowledgement behavior must remain explicit.

---

# 49. Consumer Idempotency

Every business consumer must implement idempotent processing.

Recommended mechanism:

```text
processed_event
```

Example schema:

```sql
CREATE TABLE processed_event (
    consumer_name VARCHAR(150) NOT NULL,
    event_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (consumer_name, event_id)
);
```

The idempotency record and business side effects should commit in the same local transaction where possible.

---

# 50. Inbox Pattern

Consumers with critical side effects may implement the Inbox pattern.

The inbox stores received event identity and optionally the payload before or during processing.

Benefits include:

- duplicate detection
- auditability
- controlled retry
- crash recovery
- clear processing state

The exact implementation may vary by bounded context.

---

# 51. Duplicate Processing

A duplicate event must not create duplicate business side effects.

Examples to prevent:

- creating the same reservation twice
- charging a payment twice
- sending duplicate irreversible instructions
- applying the same state transition twice
- incrementing counters twice
- inserting duplicate records

Idempotency must be enforced durably, not only in memory.

---

# 52. Consumer Transactions

Consumer processing should use a local database transaction when side effects affect one owned database.

The transaction may contain:

- idempotency check
- aggregate load
- business behavior
- persistence
- consumer outbox insertion
- processed-event recording

External HTTP calls should not remain inside a long-running database transaction.

---

# 53. Consumer-Produced Events

When consuming an event causes another integration event, the consumer must use its own Transactional Outbox.

Example:

```text
OrderApproved consumed

↓

Inventory reservation persisted

+

InventoryReserved outbox event persisted
```

The consumer must not publish the new event directly as a substitute for its local transaction boundary.

---

# 54. Error Classification

Consumer failures must be classified as:

- transient
- permanent
- contract-related
- business rejection
- infrastructure failure
- poison message

The classification determines retry and dead-letter behavior.

---

# 55. Transient Failures

Examples include:

- temporary database unavailability
- temporary external-service failure
- network timeout
- broker rebalancing
- temporary lock contention

Transient failures may be retried with bounded backoff.

---

# 56. Permanent Failures

Examples include:

- unsupported event version
- malformed payload
- missing mandatory data
- invalid business transition that cannot become valid
- prohibited event type
- unrecoverable serialization issue

Permanent failures must not cause infinite retry.

---

# 57. Retry Topics

Retry topics may be used for delayed consumer retries.

Example:

```text
orders.lifecycle.v1

orders.lifecycle.v1.retry-1

orders.lifecycle.v1.retry-2

orders.lifecycle.v1.dlq
```

Retry-topic architecture must define:

- delay strategy
- attempt count
- header propagation
- ordering impact
- dead-letter transition
- replay process

---

# 58. Blocking Retry

Short local retries may be used for quickly recoverable transient failures.

They must be:

- bounded
- low in count
- low in delay
- non-disruptive to partition processing

Long blocking retries can stall a partition and should be avoided.

---

# 59. Dead-Letter Topic

A dead-letter topic may retain events that cannot be processed automatically.

Recommended suffix:

```text
.dlq
```

Example:

```text
orders.lifecycle.v1.dlq
```

A dead-letter event should include:

- original topic
- original partition
- original offset
- event ID
- event type
- attempt count
- failure category
- sanitized error summary
- failure timestamp
- original payload or controlled reference

---

# 60. Dead-Letter Governance

A dead-letter topic is not a substitute for error resolution.

The platform must define:

- ownership
- monitoring
- retention
- investigation
- replay
- access control
- closure criteria

Dead-letter messages must not accumulate without operational visibility.

---

# 61. Replay

Kafka retention supports replay.

Replay may be used for:

- consumer recovery
- projection rebuilding
- defect correction
- new consumer initialization
- controlled incident remediation

Replay must be planned because it may reproduce historical side effects.

---

# 62. Replay Safety

Before replay:

- verify consumer idempotency
- define topic and partition range
- define offset range
- verify contract compatibility
- isolate the replay consumer group where appropriate
- verify downstream capacity
- monitor processing
- preserve audit evidence
- define stop criteria

---

# 63. Offset Reset

Offset reset is a controlled operational action.

It must not be performed casually in production.

The runbook must record:

- consumer group
- topic
- partitions
- previous offsets
- target offsets
- reason
- operator
- approval
- expected impact

---

# 64. Retention

Topic retention must be based on business and operational requirements.

Consider:

- replay window
- audit needs
- consumer outage tolerance
- storage cost
- event volume
- legal retention
- data sensitivity

Retention must not be inherited blindly from cluster defaults.

---

# 65. Time-Based Retention

Time-based retention may be used for integration streams.

Example concepts:

```text
7 days

30 days

90 days
```

The period must be documented per topic class.

---

# 66. Size-Based Retention

Size-based retention may provide additional storage protection.

It must be configured carefully because it can shorten the effective replay window during traffic spikes.

Operational monitoring should compare actual retention behavior with expected consumer recovery requirements.

---

# 67. Log Compaction

Log compaction may be used for state-oriented streams where only the latest value per key is required.

Examples may include:

- reference-data snapshots
- configuration streams
- latest entity-state projections

Lifecycle integration-event topics should not use compaction automatically.

The choice between retention and compaction must be explicit.

---

# 68. Tombstones

Compacted topics may use null-valued tombstone records for key deletion.

Tombstone behavior must be documented because it affects:

- consumer state
- retention
- replay
- deletion semantics

Tombstones must not be introduced into event streams without a specific state-stream design.

---

# 69. Topic Creation

Production topics should be provisioned through controlled infrastructure automation.

Examples include:

- Terraform
- Helm
- GitOps
- managed-platform configuration
- approved administration pipeline

Automatic topic creation should be disabled in production where operationally possible.

---

# 70. Topic Configuration as Code

Topic configuration should be version controlled.

It should define:

- name
- partition count
- replication factor
- retention
- cleanup policy
- minimum in-sync replicas
- maximum message size
- ownership
- access policy

Changes require review.

---

# 71. Topic Deletion

Topic deletion is a high-risk operational action.

Before deletion:

- confirm no active producers
- confirm no active consumers
- archive required data
- verify retention obligations
- verify replay is no longer required
- obtain approval
- preserve configuration history

---

# 72. Security

Kafka security must include:

- encrypted transport
- authenticated clients
- topic-level authorization
- least privilege
- credential rotation
- secret protection
- restricted administrative access
- auditability

Anonymous production access is prohibited.

---

# 73. Transport Encryption

Production Kafka communication must use TLS or the approved managed-service equivalent.

Plaintext transport is permitted only in isolated local development environments.

---

# 74. Authentication

Supported authentication depends on deployment architecture.

Possible mechanisms include:

- SASL/SCRAM
- mutual TLS
- OAuth-based authentication
- cloud-provider identity
- approved managed-service credentials

The selected mechanism must support secure rotation.

---

# 75. Authorization

Producers should receive write access only to owned topics.

Consumers should receive read access only to required topics and consumer groups.

Administrative privileges must remain separated from application credentials.

---

# 76. Secret Management

Kafka credentials must not be stored in:

- source code
- committed property files
- event payloads
- logs
- container images
- test fixtures with production values

Secrets must come from approved runtime secret sources.

---

# 77. Sensitive Data

Event payloads must follow data-minimization principles.

Do not publish:

- passwords
- access tokens
- private keys
- full payment credentials
- unnecessary personal data
- confidential fields not required by consumers
- internal stack traces

Topic ACLs do not remove the obligation to minimize sensitive data.

---

# 78. Data Classification

Each topic should declare a data classification, such as:

```text
Public

Internal

Confidential

Restricted
```

Classification affects:

- access
- retention
- encryption
- logging
- replay
- operational tooling
- non-production data handling

---

# 79. Observability

Kafka integration must expose producer, consumer and broker-relevant indicators.

Producer metrics should include:

- publication rate
- publication errors
- acknowledgement latency
- retry rate
- record size
- batch size
- buffer exhaustion
- metadata failures

Consumer metrics should include:

- processing rate
- processing latency
- consumer lag
- retry rate
- dead-letter rate
- rebalance count
- duplicate count
- processing errors

---

# 80. Consumer Lag

Consumer lag is a primary health indicator.

Monitor:

- lag per consumer group
- lag per topic
- lag per partition
- oldest unprocessed-event age
- trend over time

A low message count may still represent severe business delay when events are old.

---

# 81. Metrics Cardinality

Metrics must not use unbounded labels such as:

- event ID
- aggregate ID
- customer ID
- order ID
- Kafka offset

Approved bounded labels may include:

- topic
- consumer group
- result
- event type where the type set is controlled
- error category

---

# 82. Logging

Kafka logs should include appropriate diagnostic fields:

- event ID
- event type
- topic
- partition
- offset
- consumer group
- attempt
- trace ID
- processing duration
- result

Full payload logging is prohibited by default.

---

# 83. Tracing

Trace context should propagate through event metadata or approved Kafka headers.

Tracing should connect:

```text
Inbound request

↓

Business transaction

↓

Outbox event

↓

Kafka publication

↓

Consumer processing

↓

Downstream effect
```

Trace propagation must not include authentication tokens.

---

# 84. Health Checks

Kafka health behavior must distinguish:

- application liveness
- application readiness
- producer availability
- consumer status
- backlog condition

Temporary Kafka unavailability must not automatically make the application process dead.

Readiness depends on the service's operational role and backlog policy.

---

# 85. Alerting

Alerts should cover:

- sustained producer failure
- oldest outbox event beyond threshold
- consumer lag beyond threshold
- dead-letter growth
- repeated rebalance
- partition under-replication
- authentication failure
- authorization failure
- broker storage pressure
- abnormal retry rate
- no event processing during expected traffic

---

# 86. Consumer Rebalancing

Consumer-group rebalancing can temporarily pause processing.

Consumer design should:

- keep processing bounded
- avoid excessive blocking
- close resources correctly
- handle partition revocation
- avoid long in-memory uncommitted batches
- support cooperative rebalancing where appropriate

---

# 87. Graceful Shutdown

Consumers must shut down gracefully.

Shutdown should:

- stop accepting new records
- complete bounded active work
- commit safe offsets
- release partitions
- close clients
- stop executors
- respect Kubernetes termination deadlines

Producers must flush bounded pending records and close safely.

---

# 88. Concurrency

Consumer concurrency must be explicit.

It must consider:

- partition count
- ordering requirements
- downstream capacity
- database connections
- transaction throughput
- event-processing duration
- virtual-thread or platform-thread execution model

Unbounded asynchronous dispatch from a listener is prohibited.

---

# 89. Virtual Threads

Virtual threads may be used for blocking consumer workloads when justified.

They do not remove limits imposed by:

- Kafka partitions
- database connections
- external-service limits
- memory
- downstream rate limits

Concurrency must still be bounded.

---

# 90. Backpressure

Kafka consumers must avoid pulling more work than they can process safely.

Backpressure controls may include:

- listener concurrency
- maximum poll records
- pause and resume
- bounded executor queues
- database pool alignment
- external-service concurrency limits

---

# 91. Maximum Poll Interval

Consumer processing must remain compatible with Kafka's maximum poll interval.

Long-running processing may trigger group rebalancing.

Mitigations include:

- smaller poll batches
- bounded processing
- pause and resume
- dedicated workers
- adjusted timeout with evidence
- decoupled inbox processing

---

# 92. Maximum Poll Records

`max.poll.records` must be selected based on actual processing cost.

A large batch can:

- exceed transaction limits
- exceed poll intervals
- increase memory use
- delay offset commits
- complicate failure recovery

---

# 93. Retry and Ordering

Retry topics may alter ordering.

A failed event routed to a delayed retry topic may allow later events for the same aggregate to be processed first.

Consumers that require strict aggregate ordering must use one of:

- blocking bounded retry
- aggregate-version validation
- deferred processing
- partition pause
- state-aware reconciliation

The trade-off must be explicit.

---

# 94. Poison Message Isolation

One invalid event must not permanently block unrelated partitions or aggregates.

The consumer must eventually:

- classify the failure
- stop automatic retry
- move or record the event for remediation
- continue safe processing according to ordering rules

---

# 95. Local Development

Local development may use Kafka through:

- Docker Compose
- Testcontainers
- an approved local development platform

Developers must be able to:

- start the broker
- create required topics
- publish sample events
- inspect records
- run consumers
- validate retry and dead-letter behavior

---

# 96. Integration Testing

Kafka integration tests should use a production-compatible Kafka container or approved test infrastructure.

Tests must validate:

- producer publication
- consumer processing
- topic configuration assumptions
- message key
- headers
- serialization
- event envelope
- retries
- dead-letter flow
- idempotency
- offset behavior

---

# 97. Embedded Broker Limitation

An embedded broker may be useful for narrow tests but must not be the only integration-test strategy.

Production-relevant behavior should be validated against a real Kafka-compatible broker through Testcontainers or equivalent infrastructure.

---

# 98. Contract Tests

Producer and consumer contract tests must verify:

- event type
- event version
- required fields
- optional fields
- enum behavior
- timestamp format
- identifier format
- unknown fields
- backward compatibility
- topic and key semantics

---

# 99. Serialization Tests

Serialization tests should compare semantic JSON structures rather than fragile formatting.

They must ensure:

- no internal fields leak
- monetary precision is preserved
- timestamps use UTC
- enums use stable names
- identifiers remain strings
- null behavior is intentional

---

# 100. Duplicate-Delivery Tests

Every consumer must have tests proving duplicate events do not repeat business side effects.

The test should process the same event ID more than once and verify:

- one durable business effect
- one processed-event record
- safe subsequent acknowledgement

---

# 101. Ordering Tests

Where ordering matters, tests must verify:

- stable aggregate key
- monotonic aggregate version
- stale-event handling
- duplicate-version handling
- missing-version behavior
- parallel processing of unrelated aggregates

---

# 102. Failure-Injection Tests

Failure tests should cover:

- broker unavailable
- producer timeout
- consumer database failure
- malformed payload
- unsupported event version
- dead-letter publication failure
- offset commit failure
- application restart
- rebalance during processing

---

# 103. Architecture Tests

Architecture tests must enforce:

- Domain does not depend on Kafka
- Application does not depend on Kafka client classes
- Kafka adapters remain in Infrastructure
- controllers do not publish directly to Kafka
- JPA entities are not event contracts
- consumers invoke application ports
- event DTOs do not replace domain models

---

# 104. Spring Kafka

Spring for Apache Kafka will be the standard Spring integration library.

It may provide:

- producer templates
- listener containers
- consumer factories
- producer factories
- error handling
- retry topics
- header mapping
- serialization integration
- metrics integration

Framework defaults must still be reviewed explicitly.

---

# 105. Kafka Producer Adapter

The Infrastructure layer should implement a producer port.

Example:

```java
public interface IntegrationEventPublisher {

    void publish(IntegrationEvent event);
}
```

Infrastructure implementation:

```java
@Component
public class KafkaIntegrationEventPublisher
        implements IntegrationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaIntegrationEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(IntegrationEvent event) {
        // Kafka-specific publication
    }
}
```

For transactional business events, this port is normally invoked by the outbox dispatcher rather than directly by the business use case.

---

# 106. Consumer Adapter

Kafka listeners belong to Infrastructure or Interface adapters.

Example structure:

```text
infrastructure/messaging/kafka/consumer
    OrderLifecycleKafkaConsumer
    OrderLifecycleEventDeserializer
    OrderLifecycleErrorHandler
```

The listener should:

1. deserialize
2. validate
3. map to an application command
4. invoke the use case
5. acknowledge according to the processing result

Business logic must not remain in the listener.

---

# 107. Listener Responsibilities

A Kafka listener may handle:

- message metadata extraction
- deserialization
- validation
- observability context
- error classification
- acknowledgement coordination
- application-command mapping

It must not:

- implement aggregate rules
- access unrelated repositories directly
- perform persistence mapping inside business logic
- construct arbitrary HTTP responses
- contain large branching workflows

---

# 108. Configuration Properties

Kafka configuration must use typed properties.

Example:

```java
@ConfigurationProperties(prefix = "messaging.kafka")
public record KafkaProperties(
        String bootstrapServers,
        String securityProtocol,
        Producer producer,
        Consumer consumer
) {

    public record Producer(
            String acknowledgements,
            Duration deliveryTimeout,
            int maximumRequestSize
    ) {
    }

    public record Consumer(
            int concurrency,
            int maximumPollRecords,
            Duration maximumPollInterval
    ) {
    }
}
```

Mandatory properties must be validated during startup.

---

# 109. Environment Configuration

Environment-specific Kafka values include:

- broker endpoints
- authentication
- certificates
- consumer concurrency
- topic names where external mapping is required
- security protocol

Business event semantics must not vary by environment.

---

# 110. Topic Mapping

Logical destinations should be mapped to physical topics through Infrastructure configuration.

Example:

```text
Logical destination:

ORDER_LIFECYCLE_V1

Physical topic:

orders.lifecycle.v1
```

This prevents broker naming details from leaking into application logic.

---

# 111. Startup Validation

The application should validate critical Kafka configuration at startup where practical.

Examples:

- missing bootstrap servers
- invalid security protocol
- missing topic mapping
- invalid concurrency
- invalid retry limits
- invalid message-size configuration

Kafka unavailability may not always block application startup, especially when outbox buffering allows temporary decoupling.

---

# 112. Availability Model

For producers using Transactional Outbox:

```text
PostgreSQL available
Kafka unavailable
```

may still allow business transactions to commit while events accumulate in the outbox.

For consumers:

```text
Kafka unavailable
```

means event processing pauses and resumes later.

The platform must monitor backlog and lag during these conditions.

---

# 113. Kafka Transactions

Kafka transactions are not the primary mechanism for coordinating PostgreSQL and Kafka.

They may provide broker-level atomicity for Kafka-only operations, such as consume-transform-produce workflows.

Their use requires separate justification and does not replace the Transactional Outbox for database-originated events.

---

# 114. Exactly-Once Semantics

Kafka's exactly-once features do not guarantee exactly-once business effects in external databases or services.

The platform retains:

```text
At-least-once delivery

+

Idempotent consumers
```

as the cross-system consistency model.

---

# 115. Request-Reply over Kafka

Kafka request-reply is not the default interaction pattern.

Use synchronous HTTP when:

- the caller requires an immediate response
- the dependency is part of the online request contract
- timeout behavior is explicit
- availability trade-offs are acceptable

Use Kafka for asynchronous business facts and workflows.

---

# 116. Commands over Kafka

Kafka may carry asynchronous commands only when the ownership and semantics are explicit.

Commands differ from events:

```text
Command:
ReserveInventory

Event:
InventoryReserved
```

Events describe facts that already occurred.

Commands request behavior from a specific owner.

Commands require explicit destination ownership and failure semantics.

---

# 117. Event Choreography

Kafka supports choreography between bounded contexts.

Example:

```text
OrderCreated

↓

Inventory reserves stock

↓

InventoryReserved

↓

Payment authorizes amount

↓

PaymentAuthorized
```

Choreography should remain understandable and observable.

Complex workflows may require saga orchestration or explicit process management.

---

# 118. Event Storm Risk

Uncontrolled event chaining can create:

- hidden workflows
- cyclic dependencies
- difficult incident analysis
- excessive topic traffic
- duplicate side effects
- unclear ownership

Event flows must be documented in context maps or workflow diagrams.

---

# 119. Event Ownership

A producer owns the meaning of the event.

Consumers own their reaction.

A producer must not publish events designed solely around one consumer's internal model unless that contract is explicitly intentional.

---

# 120. Consumer Independence

Consumers must not coordinate through shared offsets or shared database tables.

Each bounded context should maintain:

- its own consumer group
- its own idempotency state
- its own retry policy
- its own business transaction
- its own observability

---

# 121. Cross-Context Data

Events should include enough information for the intended integration contract without copying entire aggregates unnecessarily.

Possible strategies:

- event-carried state transfer
- identifier plus consumer query
- hybrid approach

The choice depends on:

- coupling
- latency
- consistency
- data sensitivity
- consumer autonomy

---

# 122. Event-Carried State Transfer

An event may carry selected state required by consumers.

Benefits:

- reduced synchronous dependency
- consumer autonomy
- better resilience
- projection support

Costs:

- larger payload
- duplicated data
- schema evolution
- stale-data management

Only contract-relevant fields should be included.

---

# 123. Identifier-Only Events

An identifier-only event may require consumers to call the producer.

Benefits:

- smaller payload
- producer remains source of current state

Costs:

- runtime coupling
- consumer failure during producer outage
- loss of historical event state
- increased latency
- N+1 integration calls

Identifier-only contracts should be chosen deliberately.

---

# 124. Event Immutability

Published events are immutable historical facts.

They must not be updated in Kafka.

Corrections require:

- a new corrective event
- a compensating event
- a new versioned contract where semantics changed

---

# 125. Schema Evolution Ownership

The producing team is responsible for ensuring that event evolution does not unexpectedly break consumers.

Required practices include:

- contract review
- compatibility testing
- deprecation notice
- migration window
- consumer inventory
- rollout coordination for breaking changes

---

# 126. Consumer Inventory

Each topic should maintain a known consumer inventory where feasible.

This supports:

- impact analysis
- contract evolution
- deprecation
- incident communication
- retention changes
- access review

Unknown consumers increase change risk.

---

# 127. Deprecation

Deprecated events or topic versions must have:

- deprecation date
- replacement contract
- migration instructions
- known consumers
- final publication date
- retention period
- removal approval

Immediate removal is prohibited.

---

# 128. Capacity Planning

Capacity planning must consider:

- events per second
- average and maximum event size
- partition count
- replication
- retention
- compression
- consumer count
- replay load
- traffic spikes
- growth rate

Kafka storage requirements include replicated copies.

---

# 129. Load Testing

Load tests should validate:

- producer throughput
- acknowledgement latency
- outbox dispatch rate
- consumer throughput
- consumer lag
- partition balance
- retry behavior
- database side effects
- replay capacity
- broker recovery

---

# 130. Disaster Recovery

Kafka disaster-recovery strategy depends on deployment topology.

It may include:

- managed-service recovery
- cross-cluster replication
- backup of topic configuration
- infrastructure-as-code reconstruction
- producer outbox replay
- consumer idempotency
- offset recovery procedures

The PostgreSQL outbox provides a recovery source for unpublished producer events.

---

# 131. Multi-Region

Multi-region Kafka requires separate analysis.

Concerns include:

- cluster ownership
- topic replication
- active-active producers
- duplicate delivery
- ordering
- regional failover
- consumer offsets
- data residency
- latency

The initial architecture assumes one authoritative Kafka topology per deployed platform environment.

---

# 132. Operational Runbook

The Kafka runbook must include:

- checking topic health
- checking producer errors
- checking consumer lag
- checking consumer-group membership
- inspecting dead-letter topics
- replaying messages
- resetting offsets
- rotating credentials
- handling broker outages
- handling under-replicated partitions
- handling schema incompatibility
- scaling consumers
- managing topic retention
- escalating storage pressure

---

# 133. Anti-Patterns

The following are prohibited:

- serializing domain aggregates directly
- serializing JPA entities
- using Java class names as event contracts
- generating a new event ID during retry
- assuming global ordering
- assuming exactly-once business effects
- using random consumer-group names
- enabling automatic offset commit for critical consumers
- logging full sensitive payloads
- using Kafka as a large-file store
- publishing transactional events without the outbox
- omitting idempotency
- creating topics manually without governance
- changing event semantics without versioning
- using one global topic for unrelated events
- creating one topic per trivial event without justification
- using unbounded listener concurrency
- retrying poison events forever
- ignoring consumer lag
- treating a dead-letter topic as permanent resolution
- exposing Kafka classes to the Domain layer

---

# 134. Positive Consequences

The decision provides:

- durable asynchronous integration
- consumer independence
- replay capability
- partition-based ordering
- high throughput
- horizontal scalability
- multiple independent consumer groups
- mature ecosystem
- strong Java and Spring integration
- support for event-driven workflows
- compatibility with Transactional Outbox
- compatibility with at-least-once delivery
- improved bounded-context autonomy
- reduced synchronous coupling
- stronger failure recovery

---

# 135. Negative Consequences

The decision introduces:

- broker operational complexity
- partition-management responsibility
- contract-governance requirements
- consumer idempotency requirements
- replay risk
- monitoring requirements
- dead-letter management
- retention planning
- security configuration
- capacity planning
- consumer-lag management
- possible event duplication
- ordering limitations
- increased integration-testing complexity

These costs are accepted because durable event streaming is a strategic requirement for the platform.

---

# 136. Neutral Consequences

The decision also means:

- integration becomes eventually consistent
- Kafka availability is separated from producer business transactions through the outbox
- consumers process events independently
- event contracts become long-lived public artifacts
- events may outlive the application version that created them
- operational teams must manage topics, offsets and lag
- replay becomes possible but requires governance
- ordering expectations must remain partition-specific

---

# 137. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate delivery causes duplicate side effects | High | High | Require durable consumer idempotency |
| Consumer lag grows unnoticed | High | Medium | Monitor lag and oldest-event age |
| Contract change breaks consumers | High | Medium | Version contracts and run compatibility tests |
| Hot partition limits throughput | High | Medium | Validate partition-key distribution |
| Poison message blocks processing | High | Medium | Bounded retry and dead-letter handling |
| Topic count grows without control | Medium | Medium | Enforce topic governance |
| Sensitive data is published | High | Low | Apply data minimization and security review |
| Replay repeats harmful side effects | High | Medium | Require idempotency and controlled runbooks |
| Producer retries create duplicates | Medium | Medium | Enable producer idempotence and retain stable event IDs |
| Consumer acknowledges before durable processing | High | Medium | Use explicit post-processing acknowledgement |
| Partition count is insufficient | High | Medium | Perform capacity planning |
| Partition increase changes distribution | Medium | Medium | Review operational impact before change |
| Dead-letter messages are ignored | High | Medium | Alert, assign ownership and define remediation |
| Broker outage grows outbox backlog | High | Medium | Monitor capacity and publication SLO |
| Long consumer processing triggers rebalance | Medium | Medium | Bound processing and tune poll configuration |
| Excessive event size degrades broker | High | Low | Enforce payload-size limits |
| Topic retention is too short | High | Low | Define retention from recovery requirements |
| Topic retention is excessive | Medium | Medium | Review storage and data classification |
| Kafka-specific code leaks inward | Medium | Medium | Enforce architecture tests |

---

# 138. Implementation Guidance

The following rules are mandatory:

1. Kafka is the standard broker for integration events.
2. Transactional business events must be published through the Transactional Outbox.
3. Domain code must remain Kafka-independent.
4. Integration events must be explicit and versioned.
5. Every event must have a stable unique event ID.
6. Aggregate lifecycle events should use the aggregate ID as the Kafka key.
7. Ordering assumptions must remain partition-specific.
8. Consumers must use stable consumer-group names.
9. Automatic offset commit is prohibited for critical business consumers.
10. Consumers must be idempotent.
11. Business side effects and idempotency records should commit atomically.
12. Consumer-produced events must use the consumer's local outbox.
13. Retries must be bounded.
14. Poison messages must reach a visible terminal state.
15. Dead-letter topics require ownership and runbooks.
16. Event payload size must be bounded.
17. Sensitive data must be minimized.
18. Production transport must be encrypted and authenticated.
19. Topic access must follow least privilege.
20. Topic configuration must be managed as code.
21. Production topic auto-creation should be disabled.
22. Consumer lag and oldest-event age must be monitored.
23. Replay and offset reset must be controlled and auditable.
24. Contract tests are mandatory.
25. Integration tests must use Kafka-compatible infrastructure.
26. Global ordering must never be assumed.
27. Exactly-once business processing must never be assumed.
28. Full event payloads must not be logged by default.
29. Topic retention must be defined explicitly.
30. Breaking contract changes require versioned migration.

---

# 139. Validation

The decision will be validated through:

- producer integration tests
- consumer integration tests
- Kafka Testcontainers
- contract tests
- serialization tests
- duplicate-delivery tests
- consumer-idempotency tests
- partition-key tests
- ordering tests
- retry tests
- dead-letter tests
- consumer-lag monitoring
- load testing
- replay rehearsal
- failure injection
- security review
- architecture tests
- production-readiness review
- operational runbook review

---

# 140. Success Criteria

The decision is successful when:

- committed outbox events are published reliably to Kafka
- consumers can process events independently
- duplicate delivery does not create duplicate business effects
- per-aggregate ordering is preserved where required
- event contracts remain backward compatible
- consumer lag remains within service objectives
- dead-letter events are visible and remediated
- event replay can be performed safely
- topic retention supports recovery requirements
- sensitive data is protected
- Kafka-specific dependencies remain outside Domain
- topic configuration is reproducible
- producer and consumer failures are observable
- the platform supports horizontal consumer scaling
- Kafka outages do not immediately invalidate committed producer transactions
- bounded contexts remain loosely coupled

---

# 141. Alternatives Rejected

## 141.1 RabbitMQ

Rejected as the primary event-streaming platform because retained logs, replay, consumer independence and partition-based scaling are central requirements.

RabbitMQ may still be considered for specialized work-queue scenarios through a separate ADR.

---

## 141.2 Cloud-Specific Messaging APIs

Rejected as the platform-wide abstraction because stronger provider coupling would reduce portability.

Managed Kafka services remain compatible with this decision.

---

## 141.3 Direct Synchronous HTTP

Rejected as the primary integration-event mechanism because it introduces temporal coupling and does not provide durable replayable event delivery.

HTTP remains appropriate for synchronous request-response operations.

---

## 141.4 Database Polling by Consumers

Rejected because it would turn PostgreSQL into a shared integration bus and weaken bounded-context ownership.

---

# 142. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-010: Use Redis for Distributed Caching
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Use OpenTelemetry for Distributed Tracing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-018: Version Integration-Event Contracts

---

# 143. References

- Apache Kafka Documentation
- Apache Kafka Producer Configuration
- Apache Kafka Consumer Configuration
- Apache Kafka Design Documentation
- Spring for Apache Kafka Documentation
- Transactional Outbox Pattern
- Enterprise Integration Patterns
- Microservices Patterns
- Enterprise Order Platform Messaging Architecture
- Enterprise Order Platform Messaging Guidelines
- Enterprise Order Platform Domain Events
- Enterprise Order Platform Idempotency Guide
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery

---

# 144. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial Kafka integration-event architecture baseline |

---

# 145. Decision Summary

The Enterprise Order Platform adopts Apache Kafka as its standard asynchronous integration-event broker.

Kafka provides:

```text
Durable event transport

Partition-based ordering

Independent consumer groups

Replay capability

Horizontal scalability

High-throughput processing
```

The platform will use Kafka together with:

```text
Transactional Outbox

At-least-once delivery

Stable event identifiers

Idempotent consumers

Versioned contracts

Bounded retries

Dead-letter handling

Operational observability
```

Kafka remains an Infrastructure concern.

The Domain layer must remain independent from broker-specific APIs, topics, headers, producers and consumers.

This decision establishes Kafka as the durable event-streaming backbone for loosely coupled bounded-context integration.
