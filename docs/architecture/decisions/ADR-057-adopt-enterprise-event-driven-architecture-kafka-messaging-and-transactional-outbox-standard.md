# ADR-057: Adopt Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-057 |
| Title | Adopt Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Event-Driven Architecture, Apache Kafka, Transactional Outbox, Messaging Governance |
| Related Work Items | Kafka, Events, Outbox, Idempotency, Retry, DLQ, Replay |
| Supersedes | None |
| Superseded By | ADR-090 |

---

# 1. Context

The Enterprise Order Platform uses asynchronous messaging to integrate business capabilities across distributed services.

Typical flows include:

```text
Order Created
      |
      v
Kafka
      |
      +--> Workflow
      |
      +--> Audit
      |
      +--> Notification
      |
      +--> Reporting
```

Asynchronous integration provides important benefits:

- reduced temporal coupling
- scalable fan-out
- independent consumers
- event replay
- resilience to temporary downstream failure

However, messaging also introduces complexity:

- duplicate delivery
- ordering
- partitioning
- schema evolution
- poison messages
- retry behavior
- dead-letter handling
- transactional consistency
- replay safety
- operational lag
- retention
- ownership

The platform therefore requires explicit standards for event-driven integration.

---

# 2. Problem Statement

The organization requires standards covering:

- event vs command
- event ownership
- topic naming
- message envelope
- event identifiers
- trace and correlation identifiers
- partition keys
- ordering
- at-least-once delivery
- idempotent consumers
- consumer groups
- schema evolution
- serialization
- retry
- retry topics
- dead-letter queues
- poison messages
- Transactional Outbox
- dispatcher behavior
- replay
- deduplication
- Kafka transactions
- retention
- compaction
- payload size
- observability
- security
- contract testing

---

# 3. Decision Drivers

Primary drivers are:

1. reliable integration
2. business decoupling
3. delivery guarantees
4. schema compatibility
5. operational recoverability
6. idempotency
7. scalable consumption
8. traceability
9. failure isolation
10. replay safety
11. clear ownership
12. predictable messaging behavior

---

# 4. Decision

The platform adopts event-driven integration based on Apache Kafka where asynchronous messaging provides appropriate business and operational value.

The canonical model is:

```text
BUSINESS TRANSACTION
        |
        v
DOMAIN / APPLICATION EVENT
        |
        v
TRANSACTIONAL OUTBOX
        |
        v
OUTBOX DISPATCHER
        |
        v
KAFKA TOPIC
        |
        +----------+----------+
        |          |          |
        v          v          v
    Consumer A Consumer B Consumer C
        |          |          |
        v          v          v
   IDEMPOTENT  IDEMPOTENT  IDEMPOTENT
   PROCESSING  PROCESSING  PROCESSING
```

---

# 5. Fundamental Principle

The governing principle is:

```text
Assume messages can be delivered
more than once.

Design consumers so duplicate
delivery does not create duplicate
business effects.
```

---

# 6. Event-Driven Architecture

Event-driven architecture SHOULD be used when:

- temporal decoupling is valuable
- multiple consumers need the same business fact
- asynchronous processing is acceptable
- replay provides business/operational value

---

# 7. Messaging Is Not Mandatory

Kafka MUST NOT be introduced merely because the platform supports Kafka.

---

# 8. Synchronous vs Asynchronous

Use synchronous APIs when:

```text
Caller requires immediate result

Business operation requires direct acknowledgement

Interaction semantics are request/response
```

Use asynchronous events when:

```text
Fact has already occurred

Consumers can react independently

Immediate response is unnecessary
```

---

# 9. Event vs Command

Events and commands have different semantics.

---

# 10. Event

An event describes a fact that already happened.

Examples:

```text
OrderCreated

OrderApproved

OrderCancelled
```

---

# 11. Command

A command requests an action.

Examples:

```text
ApproveOrder

CancelOrder

GenerateInvoice
```

---

# 12. Naming Difference

Prefer event names in past tense:

```text
OrderCreated
```

rather than:

```text
CreateOrder
```

when publishing a fact.

---

# 13. Event Immutability

A published event represents historical business fact and MUST be treated as immutable.

---

# 14. Event Ownership

Every event MUST have an identifiable producer/owner.

---

# 15. Producer Responsibility

The producer owns:

- business meaning
- schema
- compatibility
- documentation
- publication correctness

---

# 16. Consumer Responsibility

Consumers own:

- idempotent processing
- retry behavior
- local state updates
- monitoring
- compatibility with published contract

---

# 17. Topic Ownership

Every topic MUST have an identifiable owner.

---

# 18. Topic Naming

Topic naming MUST follow a consistent convention.

A recommended model is:

```text
<domain>.<aggregate>.<event-version>
```

Example:

```text
orders.order-events.v1
```

Exact naming MAY follow enterprise Kafka platform conventions.

---

# 19. Topic Naming Requirements

Topic names SHOULD communicate:

- domain
- purpose
- version if topic-level versioning is used

---

# 20. Environment in Topic Name

Environment SHOULD normally be handled by cluster/namespace/configuration rather than embedded inconsistently into business topic names, unless platform conventions require it.

---

# 21. Event Type

A topic MAY carry multiple event types when they belong to the same aggregate/event stream.

---

# 22. Single Event Topic

A separate topic per event type MAY be used when operational or ownership boundaries justify it.

---

# 23. Topic Design

Topic design MUST consider:

```text
Ownership

Ordering

Retention

Consumers

Throughput

Schema Evolution
```

---

# 24. Event Envelope

Events SHOULD use a standard envelope.

---

# 25. Standard Envelope Fields

A typical event envelope includes:

```text
eventId

eventType

eventVersion

occurredAt

traceId

correlationId

aggregateId

producer

payload
```

---

# 26. Example Envelope

```json
{
  "eventId": "4ef0db4e-59eb-47df-9a67-9f7ad7804719",
  "eventType": "OrderCreated",
  "eventVersion": 1,
  "occurredAt": "2026-07-24T20:15:30Z",
  "traceId": "7e33444fd9134a7a",
  "correlationId": "b764116d-99da-4c04-9c22-a105e951faec",
  "aggregateId": "a1c866c2-45a3-4239-92bf-a6015c3656e2",
  "producer": "ecommerce-order-service",
  "payload": {}
}
```

---

# 27. eventId

`eventId` MUST uniquely identify the logical event.

---

# 28. eventId Stability

Retries/republication of the same logical event MUST preserve the same `eventId` when duplicate detection depends on it.

---

# 29. Random Retry Event ID

Generating a new event ID for every retry can defeat idempotency.

---

# 30. eventType

`eventType` MUST identify the semantic event.

---

# 31. eventVersion

`eventVersion` SHOULD identify payload schema/semantic evolution where the event contract requires it.

---

# 32. occurredAt

`occurredAt` SHOULD represent when the business event occurred.

---

# 33. Publishing Time

Business occurrence time and broker publication time are not necessarily identical.

---

# 34. traceId

`traceId` SHOULD enable distributed tracing correlation.

---

# 35. correlationId

`correlationId` MAY represent a broader business/request workflow identifier.

---

# 36. aggregateId

`aggregateId` SHOULD identify the relevant aggregate/business entity where applicable.

---

# 37. Message Headers

Some envelope metadata MAY be carried in Kafka headers.

---

# 38. Header vs Payload

Stable business semantics SHOULD not depend solely on infrastructure headers if downstream persistence/replay requires those fields.

---

# 39. Serialization

Event serialization MUST use an approved format.

---

# 40. Serialization Formats

Common supported choices include:

```text
JSON

Avro

Protobuf
```

depending on platform governance.

---

# 41. Schema Governance

Published event schemas MUST be governed explicitly.

---

# 42. Schema Registry

A schema registry SHOULD be used when the chosen serialization technology and platform maturity support it.

---

# 43. Schema Compatibility

Schema changes MUST follow defined compatibility rules.

---

# 44. Backward Compatibility

A new producer schema is backward compatible when older consumers can continue reading messages under the agreed compatibility model.

---

# 45. Forward Compatibility

A schema is forward compatible when newer consumers can handle messages produced by older producers.

---

# 46. Full Compatibility

Full compatibility combines required backward and forward behavior.

---

# 47. Additive Field

Adding an optional field with a safe default is generally the preferred evolution mechanism.

---

# 48. Removing Field

Removing a published field is usually breaking.

---

# 49. Renaming Field

Renaming a published field is usually breaking.

---

# 50. Type Change

Changing field type is normally breaking.

---

# 51. Semantic Change

Changing the meaning of a field without changing its schema can also be breaking.

---

# 52. Enum Evolution

Event enums require compatibility analysis.

---

# 53. Adding Enum Value

Adding an enum value may break consumers with exhaustive parsing.

---

# 54. Consumer Robustness

Consumers SHOULD define behavior for unknown future enum values where feasible.

---

# 55. Event Contract Documentation

Events MUST document:

```text
Meaning

Producer

Topic

Partition Key

Fields

Optionality

Compatibility

Retention Expectations
```

---

# 56. Payload Minimization

Events SHOULD contain only data required by consumers and the integration contract.

---

# 57. Entity Dump Anti-Pattern

Publishing entire persistence entities is prohibited.

---

# 58. PII

Personal data in events MUST follow ADR-046.

---

# 59. Sensitive Event Data

Sensitive data SHOULD be minimized because Kafka retention and replication increase the number of stored copies.

---

# 60. Payload Size

Event payload size MUST be bounded.

---

# 61. Large Payload

Large documents/files SHOULD NOT normally be transported directly through Kafka.

---

# 62. Claim Check Pattern

For large payloads, consider:

```text
Store Large Object
      |
      v
Publish Reference
```

when appropriate.

---

# 63. Ordering

Kafka ordering is guaranteed only within a partition.

---

# 64. Ordering Requirement

If business processing requires ordering, related events MUST use a consistent partitioning strategy.

---

# 65. Partition Key

The partition key SHOULD represent the business identity requiring ordered processing.

Example:

```text
orderId
```

---

# 66. Aggregate Ordering

For order lifecycle events:

```text
OrderCreated

OrderApproved

OrderCancelled
```

using:

```text
orderId
```

as partition key preserves order for the same order within the topic partition.

---

# 67. Global Ordering

Global ordering across all business entities SHOULD NOT be assumed.

---

# 68. Partition Count

Partition count determines potential consumer parallelism.

---

# 69. Too Few Partitions

Too few partitions may constrain throughput.

---

# 70. Too Many Partitions

Too many partitions increase:

- broker metadata
- open files
- rebalance cost
- operational complexity

---

# 71. Partition Planning

Partition count SHOULD consider:

```text
Expected Throughput

Consumer Parallelism

Ordering Requirements

Growth
```

---

# 72. Partition Key Distribution

Partition keys SHOULD avoid severe hotspotting.

---

# 73. Consumer Group

Consumer groups provide competing-consumer semantics.

---

# 74. Same Consumer Group

Consumers in the same group share partitions.

---

# 75. Different Consumer Groups

Independent business capabilities SHOULD use separate consumer groups when each must receive the event.

---

# 76. Example

```text
orders.order-events.v1
       |
       +--> workflow-group
       |
       +--> audit-group
       |
       +--> reporting-group
```

Each group independently receives the stream.

---

# 77. Delivery Semantics

The standard assumption is:

```text
AT-LEAST-ONCE
```

delivery.

---

# 78. Duplicate Delivery

Consumers MUST assume duplicates can occur.

---

# 79. Duplicate Sources

Duplicates may result from:

```text
Producer retry

Consumer retry

Offset replay

Failover

Outbox redispatch

Manual replay
```

---

# 80. Consumer Idempotency

Critical consumers MUST be idempotent.

---

# 81. Idempotency Key

Typical deduplication key:

```text
eventId
```

---

# 82. Idempotency Strategies

Possible implementations include:

```text
Processed Event Table

Unique Database Constraint

Business Key

State Version

Idempotency Cache
```

---

# 83. Durable Deduplication

Critical business effects SHOULD use durable deduplication rather than only in-memory state.

---

# 84. Processed Event Table

Conceptually:

```text
processed_event

event_id
consumer_name
processed_at
```

with uniqueness on:

```text
event_id + consumer_name
```

where appropriate.

---

# 85. Atomic Consumer Processing

Where possible, local business state update and deduplication state SHOULD commit atomically.

---

# 86. Exactly Once Semantics

Kafka transactional features MAY reduce duplication in specific Kafka-to-Kafka pipelines.

They do not automatically provide end-to-end exactly-once business semantics across external databases/services.

---

# 87. Business Exactly Once

The platform SHOULD describe guarantees precisely.

Prefer:

```text
At-least-once delivery with idempotent processing
```

over unsupported claims of universal exactly-once behavior.

---

# 88. Producer Reliability

Producers SHOULD use broker acknowledgement settings appropriate to message criticality.

---

# 89. Producer Idempotence

Kafka producer idempotence SHOULD be enabled where supported and appropriate.

---

# 90. Producer Retry

Producer retries MUST remain bounded/configured according to platform standards.

---

# 91. Transactional Outbox

Business transactions that require reliable event publication from relational data MUST use Transactional Outbox or another explicitly approved atomicity pattern.

---

# 92. Dual Write Problem

This is unsafe:

```text
BEGIN DB TRANSACTION

Save Order

COMMIT

Publish Kafka Event
```

because:

```text
DB commit succeeds

Kafka publish fails
```

leaving inconsistent integration state.

---

# 93. Reverse Dual Write

This is also unsafe:

```text
Publish Kafka Event

Save Database

Database fails
```

because consumers may observe an event for a transaction that never committed.

---

# 94. Outbox Pattern

Use:

```text
BEGIN TRANSACTION
      |
      +--> Save Business Data
      |
      +--> Save Outbox Event
      |
COMMIT
```

followed asynchronously by:

```text
Outbox Dispatcher
      |
      v
Kafka
```

---

# 95. Atomicity

Business state and outbox event MUST be committed in the same local database transaction.

---

# 96. Outbox Record

A standard outbox record SHOULD include fields such as:

```text
id

aggregateType

aggregateId

eventType

destination

payload

status

attempts

nextAttemptAt

lastError

createdAt

sentAt

traceId
```

---

# 97. Outbox Status

Reference statuses MAY include:

```text
PENDING

PROCESSING

SENT

FAILED
```

depending on implementation.

---

# 98. Dispatcher

The dispatcher MUST process outbox records asynchronously.

---

# 99. Dispatcher Batch Size

Dispatcher batch size MUST be bounded.

---

# 100. Dispatcher Pagination

Database reads MUST remain bounded.

---

# 101. Concurrent Dispatch

Multiple dispatcher instances MUST coordinate safely to avoid uncontrolled duplicate processing.

---

# 102. Duplicate Publish

Even with coordination, duplicate Kafka publication remains possible after partial failure.

Consumers MUST still be idempotent.

---

# 103. Example Partial Failure

```text
Kafka publish succeeds

Dispatcher crashes before marking SENT
```

After restart, the event may be published again.

---

# 104. Outbox Retry

Failed publication MUST follow bounded retry policy.

---

# 105. nextAttemptAt

Retry scheduling SHOULD avoid hot-loop processing.

---

# 106. Maximum Attempts

Maximum dispatcher attempts MUST be explicit.

---

# 107. Exhausted Outbox

Events that cannot be published after the configured policy MUST become operationally visible.

---

# 108. Permanent Event Loss

Failed outbox events MUST NOT silently disappear.

---

# 109. Outbox Monitoring

Monitor:

```text
Pending Count

Oldest Pending Age

Dispatch Rate

Failure Rate

Retry Count

Exhausted Events
```

---

# 110. Outbox Cleanup

Sent events MAY be archived/deleted according to retention requirements.

---

# 111. Outbox Retention

Retention MUST balance:

- auditability
- replay requirements
- operational diagnosis
- database size

---

# 112. Outbox Payload

Outbox payload MUST represent the event actually intended for publication.

---

# 113. Mutable Reconstruction

Dispatch SHOULD NOT rely on reconstructing historical event content from current mutable business state when exact event semantics matter.

---

# 114. Retry Topics

Kafka retry topics MAY be used when delayed retry is required without blocking normal consumption.

---

# 115. Retry Topic Model

Example:

```text
orders.order-events.v1
        |
        v
PROCESS
        |
     FAILURE
        |
        v
orders.order-events.retry-1
        |
        v
orders.order-events.retry-2
        |
        v
DLQ
```

---

# 116. Retry Topic Ownership

Retry infrastructure belongs to the consuming capability.

---

# 117. Retry Count

Kafka retry stages MUST be bounded.

---

# 118. Retry Delay

Retry delays SHOULD increase appropriately for transient dependency failures.

---

# 119. Retry Metadata

Retry messages SHOULD preserve original:

```text
eventId

eventType

traceId

correlationId
```

---

# 120. Retry Attempt

Retry metadata SHOULD identify attempt count where operationally useful.

---

# 121. Poison Message

A poison message is a message that repeatedly fails due to non-transient characteristics.

---

# 122. Poison Message Handling

A poison message MUST NOT block normal partition processing indefinitely.

---

# 123. Dead-Letter Queue

Permanent failures SHOULD be routed to an owned DLQ when automatic recovery is not appropriate.

---

# 124. DLQ Is Not Trash

A DLQ is not:

```text
A place to forget failed messages.
```

---

# 125. DLQ Requirements

Every DLQ MUST have:

```text
Owner

Alert

Dashboard

Retention Policy

Investigation Procedure

Replay Procedure
```

---

# 126. DLQ Event Metadata

DLQ messages SHOULD preserve enough metadata to diagnose the failure.

---

# 127. Failure Reason

Failure details MUST be sanitized before inclusion in DLQ payload/headers if sensitive information may be exposed.

---

# 128. DLQ Replay

Replay MUST preserve idempotency.

---

# 129. Replay Authorization

Manual replay of production DLQ messages MUST be controlled and auditable.

---

# 130. Replay

Event replay is a normal operational capability.

---

# 131. Replay Scenarios

Examples:

```text
Consumer bug corrected

Projection rebuild

Disaster recovery

Failed integration recovery

New consumer bootstrap
```

---

# 132. Replay Safety

Consumers MUST distinguish between:

```text
New Event

Replayed Event
```

only when business semantics require different handling.

Normally idempotency SHOULD make replay safe without special-case behavior.

---

# 133. Side Effects During Replay

Consumers performing irreversible external side effects require explicit replay strategy.

---

# 134. Example

A replay MUST NOT accidentally resend:

```text
Customer emails

Payments

External orders
```

unless that behavior is explicitly intended.

---

# 135. Replay Modes

Consumers MAY support modes such as:

```text
Normal Processing

Projection Rebuild

Side-Effect Suppressed Replay
```

when justified.

---

# 136. Offset Reset

Kafka offset reset MUST be controlled.

---

# 137. Manual Offset Change

Production offset changes require:

```text
Owner

Reason

Scope

Expected Impact

Rollback/Recovery Plan
```

---

# 138. Retention

Topic retention MUST follow business and operational requirements.

---

# 139. Time-Based Retention

Events MAY be retained for a defined time window.

---

# 140. Size-Based Retention

Size-based retention MAY complement time-based retention.

---

# 141. Infinite Retention

Infinite retention MUST NOT be the default.

---

# 142. Replay Window

Retention SHOULD support the required operational replay window.

---

# 143. Privacy

Retention MUST follow ADR-046 when events contain personal data.

---

# 144. Compaction

Log compaction MAY be used for state-oriented topics where the latest value per key is meaningful.

---

# 145. Event History vs Compaction

Compaction MUST NOT be used when full historical sequence is required for business semantics.

---

# 146. Tombstone

Compacted topics MAY use tombstone records for key deletion according to Kafka semantics.

---

# 147. Compaction + Retention

Compaction and time retention can coexist and MUST be configured deliberately.

---

# 148. Kafka Security

Kafka access MUST follow least privilege.

---

# 149. Producer Authorization

A producer SHOULD only publish to approved topics.

---

# 150. Consumer Authorization

A consumer SHOULD only consume required topics/groups.

---

# 151. Encryption

Kafka traffic SHOULD use approved encryption in transit.

---

# 152. Authentication

Broker authentication MUST use approved platform mechanisms.

---

# 153. Secrets

Kafka credentials MUST NOT be embedded in source code.

---

# 154. Sensitive Logging

Kafka payloads MUST NOT be indiscriminately logged.

---

# 155. Headers

Headers containing tokens or other credentials MUST NOT be propagated unless the protocol explicitly requires and safely supports it.

---

# 156. Observability

Kafka integrations MUST be observable.

---

# 157. Producer Metrics

Monitor:

```text
Publish Rate

Publish Errors

Retry Rate

Latency

Record Size
```

---

# 158. Consumer Metrics

Monitor:

```text
Processing Rate

Failure Rate

Consumer Lag

Retry Count

DLQ Count

Processing Latency
```

---

# 159. Consumer Lag

Consumer lag is a critical operational metric.

---

# 160. Lag Alone

Lag MUST be interpreted with:

```text
Arrival Rate

Processing Rate

Partition Count

Business SLA
```

---

# 161. Oldest Event Age

For critical workflows, oldest unprocessed event age MAY be more meaningful than raw lag count.

---

# 162. Processing Latency

Measure:

```text
event occurred
      ->
event processed
```

when end-to-end event latency matters.

---

# 163. Trace Propagation

Distributed trace context SHOULD propagate through Kafka headers according to OpenTelemetry/W3C standards where applicable.

---

# 164. Span Model

A producer publication and consumer handling SHOULD be trace-correlated.

---

# 165. Trace Is Not Event Identity

Trace IDs MUST NOT replace:

```text
eventId
```

---

# 166. Logging

Messaging logs SHOULD include:

```text
eventId

eventType

topic

partition

offset

consumerGroup

elapsedMs
```

where useful and safe.

---

# 167. Log Cardinality

Business identifiers SHOULD be logged deliberately and MUST NOT become unbounded metric labels.

---

# 168. Event Contract Testing

Published events MUST have automated schema/contract validation appropriate to criticality.

---

# 169. Producer Test

Producer tests SHOULD validate:

```text
Event Type

Required Metadata

Payload Mapping

Partition Key

Serialization
```

---

# 170. Consumer Test

Consumer tests SHOULD validate:

```text
Deserialization

Business Processing

Idempotency

Failure Classification

Retry

DLQ
```

---

# 171. Compatibility Test

Schema changes SHOULD be automatically checked for compatibility.

---

# 172. Integration Test

Critical Kafka behavior SHOULD use representative Kafka infrastructure where practical.

---

# 173. Embedded Mock Limitation

Pure mocks do not prove:

```text
Serialization

Headers

Partitioning

Broker behavior
```

---

# 174. Testcontainers

Kafka-compatible Testcontainers infrastructure SHOULD be used where broker semantics matter.

---

# 175. Idempotency Test

A critical consumer SHOULD test:

```text
same eventId
processed twice
```

and verify:

```text
one logical business effect
```

---

# 176. Ordering Test

Where order matters, tests SHOULD verify partition-key/order assumptions.

---

# 177. Retry Test

Retry tests MUST verify bounded attempts.

---

# 178. DLQ Test

DLQ tests SHOULD verify permanent failure routing.

---

# 179. Replay Test

Critical consumers SHOULD test replay safety.

---

# 180. Outbox Integration Test

Transactional Outbox tests SHOULD verify:

```text
Business Data Saved

Outbox Event Saved

Both Commit Together
```

---

# 181. Rollback Test

If the business transaction rolls back, the outbox event MUST also roll back.

---

# 182. Dispatcher Test

Dispatcher tests SHOULD validate:

```text
Batch Selection

Publish Success

Publish Failure

Retry Scheduling

Status Transition

Max Attempts
```

---

# 183. Concurrent Dispatcher Test

Concurrent dispatch behavior SHOULD be tested where multiple dispatcher instances can operate.

---

# 184. Performance

Kafka architecture MUST consider throughput and backpressure.

---

# 185. Producer Throughput

Producer configuration SHOULD balance:

```text
Latency

Batching

Compression

Durability
```

---

# 186. Compression

Compression MAY improve network/storage efficiency.

---

# 187. Compression Cost

Compression increases CPU use and MUST be workload-appropriate.

---

# 188. Consumer Throughput

Consumer processing time SHOULD be measured.

---

# 189. Slow Consumer

A slow consumer SHOULD be optimized at the actual bottleneck before blindly increasing concurrency.

---

# 190. Consumer Parallelism

Effective parallelism is bounded by partition count.

---

# 191. Async Processing Inside Consumer

Additional internal parallelism MAY be used when ordering semantics remain correct.

---

# 192. Commit Ordering

Parallel consumer processing MUST carefully manage offset commit semantics.

---

# 193. Out-of-Order Completion

Parallel processing can complete records in a different order than consumption.

This MUST be considered where ordered state changes matter.

---

# 194. Backpressure

Consumer internal queues MUST be bounded.

---

# 195. Poll Loop

Kafka consumers MUST continue respecting broker polling/session behavior.

---

# 196. Long Processing

Long-running processing MAY require architecture such as:

```text
Consume

Persist Work

Acknowledge

Process Asynchronously
```

when direct consumer processing would violate poll/session constraints.

---

# 197. Topic Provisioning

Topics SHOULD be provisioned through Infrastructure as Code or approved platform automation.

---

# 198. Manual Topic Creation

Manual production topic creation SHOULD be avoided.

---

# 199. Topic Configuration

Topic configuration MUST be version controlled where platform tooling supports it.

---

# 200. Required Topic Metadata

Topic definitions SHOULD include:

```text
Name

Owner

Partitions

Replication

Retention

Compaction

Data Classification

Producer

Consumers
```

---

# 201. Replication Factor

Production replication MUST meet platform durability/availability requirements.

---

# 202. Min ISR

Broker/topic durability configuration SHOULD align with required write guarantees.

---

# 203. Environment Strategy

DEV/TEST/HML/PRD topic configuration SHOULD preserve relevant semantics while scaling capacity appropriately.

---

# 204. Topic Decommission

Unused topics MUST follow a controlled decommission process.

---

# 205. Decommission Checks

Before deletion:

```text
[ ] Producers stopped

[ ] Consumers stopped/migrated

[ ] Retention requirements checked

[ ] Replay requirements checked

[ ] Data governance checked

[ ] Owner approved
```

---

# 206. Event Versioning

Event versioning MAY occur through:

```text
Schema evolution in same topic

Explicit eventVersion field

New topic version
```

depending on compatibility needs.

---

# 207. New Topic Version

A new topic version SHOULD be reserved for materially incompatible evolution.

---

# 208. Dual Publishing

During migration, producers MAY temporarily publish old and new contracts.

---

# 209. Dual Publish Risk

Dual publishing creates:

- duplicate infrastructure
- additional cost
- divergence risk
- operational complexity

It MUST have a retirement plan.

---

# 210. Consumer Migration

A version migration SHOULD follow:

```text
Publish New Version

Migrate Consumers

Measure Old Version Usage

Stop Old Publishing

Retain as Required

Decommission
```

---

# 211. Event-Driven Failure Matrix

Critical event flows SHOULD define:

| Failure | Expected Behavior |
|---|---|
| Kafka unavailable | Outbox remains pending |
| Producer transient failure | Bounded retry |
| Consumer transient failure | Retry |
| Consumer permanent failure | DLQ |
| Duplicate event | Idempotent handling |
| Schema incompatible | Contract failure / DLQ |
| Consumer outage | Lag accumulates |
| Recovery | Backlog drains safely |

---

# 212. Messaging Review Checklist

A new event integration SHOULD evaluate:

```text
[ ] Is async messaging appropriate?

[ ] Is this an event or command?

[ ] Who owns the event?

[ ] Who owns the topic?

[ ] What is the schema?

[ ] What is the eventId?

[ ] What is the partition key?

[ ] Is ordering required?

[ ] What delivery guarantee applies?

[ ] Is the consumer idempotent?

[ ] What is the retry policy?

[ ] Is a DLQ required?

[ ] Who owns the DLQ?

[ ] What is the replay strategy?

[ ] What is the retention period?

[ ] Does the event contain PII?

[ ] Is payload size bounded?

[ ] How is compatibility tested?

[ ] How is lag monitored?

[ ] Is Transactional Outbox required?
```

---

# 213. Messaging Fitness Functions

Stable messaging rules SHOULD be automated where practical.

Examples:

```text
[ ] Event envelope contains required metadata

[ ] eventId is mandatory

[ ] Event schemas pass compatibility checks

[ ] Topic names follow convention

[ ] Partition key is defined

[ ] Consumer group is explicit

[ ] Retry is bounded

[ ] DLQ exists where required

[ ] Outbox batch size is bounded

[ ] Applied Flyway migration for outbox remains immutable

[ ] Payload size is within configured limit

[ ] Critical consumers expose lag metrics

[ ] Event contracts are documented
```

---

# 214. Enterprise Messaging Gate

An event integration is not considered compliant when applicable conditions include:

```text
[ ] Event has no owner

[ ] Topic has no owner

[ ] Event semantics are ambiguous

[ ] Event lacks stable eventId

[ ] Ordering requirement has no partition strategy

[ ] Consumer is not idempotent

[ ] Retry is unbounded

[ ] Poison message can block partition indefinitely

[ ] DLQ exists without monitoring/ownership

[ ] Schema change is breaking without migration strategy

[ ] PII is unnecessarily included

[ ] Payload is unbounded

[ ] Business DB and Kafka are dual-written without atomicity strategy

[ ] Outbox failure can silently lose events

[ ] Consumer lag is not observable

[ ] Replay safety is unknown
```

---

# 215. Anti-Patterns

The following are prohibited or strongly discouraged:

- Kafka introduced without asynchronous business need
- event names written as commands
- ambiguous event ownership
- event payloads generated directly from JPA entities
- missing eventId
- new eventId generated on every retry of the same logical event
- assuming global Kafka ordering
- random partition keys that break aggregate ordering
- assuming exactly-once business behavior by default
- non-idempotent critical consumers
- in-memory-only deduplication for critical persistent effects
- direct database + Kafka dual writes without atomicity strategy
- outbox events reconstructed from current mutable state when historical payload matters
- infinite retry
- poison messages blocking partitions forever
- DLQ used as permanent trash
- DLQ without owner
- manual replay without audit/control
- replay triggering unintended irreversible side effects
- unlimited Kafka retention by default
- PII-heavy event payloads without necessity
- large binary payloads directly embedded in Kafka
- consumer concurrency greater than useful partition parallelism without reason
- unbounded internal consumer queues
- event contract changes without compatibility validation
- dual publishing without retirement plan
- manual unmanaged production topic configuration

---

# 216. Positive Consequences

The decision provides:

- reliable asynchronous integration
- clearer event semantics
- consistent event envelopes
- safer schema evolution
- predictable ordering
- idempotent processing
- controlled retries
- operational DLQ governance
- reliable Transactional Outbox
- safer replay
- better Kafka observability
- reduced event-loss risk
- clearer topic ownership

---

# 217. Negative Consequences

The decision introduces:

- event schema governance
- idempotency persistence
- outbox infrastructure
- retry/DLQ operations
- contract testing
- topic metadata maintenance
- replay procedures
- additional monitoring

These costs are accepted because asynchronous integration without explicit delivery and recovery semantics creates substantial distributed-system risk.

---

# 218. Neutral Consequences

The decision also means:

- duplicate delivery remains possible
- not every workflow requires Kafka
- not every event requires a separate topic
- not every consumer requires the same retry policy
- exactly-once is not the default business guarantee
- some event histories may use compaction while others require full retention
- replay behavior depends on side effects

---

# 219. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate business effect | Critical | Medium | Idempotent consumer |
| Event loss | Critical | Medium | Transactional Outbox |
| Schema break | High | Medium | Compatibility validation |
| Poison message | High | Medium | Bounded retry + DLQ |
| Consumer lag | High | Medium | Metrics + capacity planning |
| Ordering defect | High | Medium | Stable partition key |
| Replay side effect | Critical | Medium | Replay-safe consumer design |
| DLQ accumulation | High | Medium | Ownership + alerts |
| PII retention | Critical | Low/Medium | Payload minimization |
| Outbox growth | High | Medium | Monitoring + cleanup |
| Hot partition | High | Medium | Key-distribution analysis |
| Version proliferation | Medium | Medium | Compatibility-first evolution |

---

# 220. Implementation Guidance

The following rules are mandatory:

1. Kafka must only be introduced when asynchronous messaging provides justified value.
2. Events must represent facts and use clear semantic naming.
3. Every event and topic must have identifiable ownership.
4. Critical events must use a stable unique eventId.
5. Retry of the same logical event must preserve duplicate-detection identity.
6. Event contracts must use an approved serialization/schema strategy.
7. Schema changes must follow compatibility governance.
8. Event payloads must be minimized.
9. Personal data in events must follow ADR-046.
10. Event payload size must be bounded.
11. Ordering requirements must define an appropriate partition key.
12. Global ordering must not be assumed.
13. Consumer groups must reflect independent consumption responsibilities.
14. The standard delivery assumption is at-least-once.
15. Critical consumers must be idempotent.
16. Critical deduplication should be durable.
17. Database business state and required event publication must use Transactional Outbox or another approved atomicity mechanism.
18. Outbox data must commit atomically with business data.
19. Outbox dispatch must be asynchronous and bounded.
20. Outbox retry must be bounded and observable.
21. Outbox events exhausting retry must not disappear silently.
22. Kafka retry must be bounded.
23. Poison messages must not block normal consumption indefinitely.
24. DLQs must have owners, monitoring, retention and replay procedures.
25. Replay must preserve idempotency.
26. Irreversible side effects require explicit replay semantics.
27. Topic retention must be deliberate.
28. Compaction must only be used where latest-state semantics are appropriate.
29. Kafka security must enforce least privilege.
30. Producer and consumer metrics must be available.
31. Consumer lag must be monitored.
32. Critical event contracts must have automated compatibility tests.
33. Critical Kafka behavior should use representative integration testing.
34. Topic provisioning should be automated through approved platform/IaC mechanisms.
35. Topic decommission must be controlled.
36. Incompatible event evolution must use an explicit migration/version strategy.
37. Dual publishing must have an explicit retirement plan.
38. Messaging fitness functions should automatically enforce stable enterprise rules where practical.

---

# 221. Validation

This ADR will be validated through:

- Apache Kafka
- Spring Kafka
- schema registry tooling where applicable
- Transactional Outbox tests
- PostgreSQL integration tests
- Testcontainers
- JUnit 5
- AssertJ
- contract/schema validation
- consumer idempotency tests
- retry/DLQ tests
- replay tests
- Kafka metrics
- OpenTelemetry
- architecture fitness functions
- CI/CD gates

---

# 222. Success Criteria

The decision is successful when:

- critical events are not lost during producer failure
- duplicate delivery does not create duplicate business effects
- event ownership is clear
- schema-breaking changes are detected before release
- ordering-sensitive flows behave predictably
- poison messages no longer block normal processing
- DLQ backlog is visible and actively managed
- outbox backlog is measurable
- replay is safe
- consumer lag is predictable
- event contracts remain documented
- topic lifecycle is governed
- event-driven integrations can recover from temporary failures without manual data reconstruction

---

# 223. Alternatives Rejected

## 223.1 Direct Database + Kafka Dual Write

Rejected because independent commits can diverge.

---

## 223.2 Exactly-Once Assumption Everywhere

Rejected because end-to-end business effects frequently cross systems outside Kafka transactions.

---

## 223.3 Non-Idempotent Consumers

Rejected because duplicate delivery is expected under at-least-once semantics.

---

## 223.4 Infinite Retry

Rejected because poison messages and persistent failures would block progress.

---

## 223.5 DLQ Without Operations

Rejected because failed events require active ownership and recovery.

---

## 223.6 Kafka for Every Integration

Rejected because asynchronous messaging is not appropriate for every interaction.

---

# 224. Related Decisions

This ADR extends and implements:

- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Apache Kafka for Integration Events
- ADR-012: Adopt Saga Pattern for Distributed Workflows
- ADR-014: Adopt Distributed Observability
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-045: Disaster Recovery and Regional Resilience Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-053: Enterprise Testing Strategy
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard

---

# 225. References

- Apache Kafka Documentation
- Spring for Apache Kafka Documentation
- Confluent Schema Registry
- Transactional Outbox Pattern
- Idempotent Consumer Pattern
- Enterprise Integration Patterns
- Designing Data-Intensive Applications
- Domain-Driven Design
- OpenTelemetry Semantic Conventions
- Google Site Reliability Engineering

---

# 226. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise event-driven architecture and Kafka messaging baseline |

---

# 227. Decision Summary

The messaging lifecycle becomes:

```text
BUSINESS TRANSACTION
        |
        v
SAVE DOMAIN STATE
        +
SAVE OUTBOX EVENT
        |
        v
      COMMIT
        |
        v
OUTBOX DISPATCHER
        |
        v
      KAFKA
        |
        v
   CONSUMERS
        |
        v
IDEMPOTENT PROCESSING
```

The fundamental delivery model is:

```text
AT-LEAST-ONCE
      +
IDEMPOTENCY
      =
SAFE DUPLICATE DELIVERY
```

Event identity:

```text
LOGICAL EVENT
     |
     v
   eventId
     |
     +--> Producer retry
     |
     +--> Outbox retry
     |
     +--> Consumer retry
     |
     +--> Replay
```

The identity remains stable.

Ordering:

```text
ORDER A events
      |
      v
partition key = orderA
      |
      v
PARTITION 1

ORDER B events
      |
      v
partition key = orderB
      |
      v
PARTITION 3
```

Kafka guarantees order inside each partition, not globally.

Transactional consistency:

```text
BAD

DB COMMIT
   |
   v
KAFKA PUBLISH
   |
   X
 FAILURE


GOOD

BEGIN TRANSACTION
   |
   +--> BUSINESS DATA
   |
   +--> OUTBOX EVENT
   |
 COMMIT
   |
   v
ASYNC DISPATCH
```

Consumer processing:

```text
EVENT
  |
  v
EVENT ID ALREADY PROCESSED?
      /        \
    YES         NO
     |           |
     v           v
   IGNORE      PROCESS
                  |
                  v
             SAVE RESULT
                  +
             SAVE EVENT ID
```

Retry/DLQ:

```text
PROCESS
  |
  +--> SUCCESS
  |
  +--> TRANSIENT FAILURE
  |        |
  |        v
  |    BOUNDED RETRY
  |
  +--> PERMANENT FAILURE
           |
           v
          DLQ
           |
           v
      INVESTIGATE
           |
           v
         REPLAY
```

Schema evolution:

```text
EVENT V1
   |
   v
ADD OPTIONAL FIELD
   |
   v
COMPATIBILITY CHECK
   |
   v
EVENT EVOLVES
```

rather than silently changing:

```text
field type

field meaning

required fields

enum semantics
```

Replay:

```text
HISTORICAL EVENTS
       |
       v
    REPLAY
       |
       v
IDEMPOTENT CONSUMER
       |
       v
SAFE RECONSTRUCTION
```

The complete messaging equation is:

```text
CLEAR EVENT SEMANTICS
       +
STABLE EVENT IDENTITY
       +
SCHEMA GOVERNANCE
       +
PARTITIONING
       +
AT-LEAST-ONCE DELIVERY
       +
IDEMPOTENT CONSUMERS
       +
TRANSACTIONAL OUTBOX
       +
BOUNDED RETRY
       +
OWNED DLQ
       +
SAFE REPLAY
       +
OBSERVABILITY
       =
RELIABLE EVENT-DRIVEN ARCHITECTURE
```

The governing principle is:

```text
Kafka does not make a system
reliable by itself.

Reliability comes from explicit
delivery semantics.

Assume duplicates.

Design idempotent consumers.

Preserve event identity.

Keep schema evolution compatible.

Use partitioning intentionally.

Never dual-write business state
and Kafka independently when
both must succeed.

Do not let poison messages stop
the stream.

Do not use DLQs as trash.

Make replay safe.

And treat every published event
as a long-lived integration
contract owned by the producer
and depended upon by consumers.
```
