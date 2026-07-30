# ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-030 |
| Title | Adopt Kafka Event Governance and Schema Evolution Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Apache Kafka, Event-Driven Architecture, Schema Governance, Integration |
| Related Work Items | Kafka, Outbox, Event Contracts, Idempotency, Retry, DLQ, Replay, Schema Evolution |
| Supersedes | None |
| Superseded By | ADR-090 |

---

# 1. Context

The AstraForge Supply Platform uses Apache Kafka for asynchronous integration between independently deployable services.

The architecture includes:

- Java 21
- Spring Boot
- PostgreSQL
- Apache Kafka
- Spring Kafka
- transactional outbox
- Kubernetes
- distributed observability
- external HTTP integrations
- independently deployable microservices

Kafka provides durable asynchronous messaging, but reliable event-driven architecture requires more than publishing JSON messages to topics.

Without explicit governance, Kafka environments tend to accumulate:

- inconsistent topic names
- undocumented topics
- ambiguous events
- incompatible schema changes
- duplicated business data
- unstable partition keys
- uncontrolled retries
- permanently failing messages
- unbounded DLQs
- unsafe replays
- duplicate processing
- excessive consumer lag
- undocumented consumers
- accidental PII propagation
- operationally orphaned topics

Therefore Kafka must be governed as a shared distributed integration platform.

---

# 2. Problem Statement

The platform requires standards defining:

- event semantics
- event naming
- topic naming
- topic ownership
- producer ownership
- consumer ownership
- event envelope
- event identity
- correlation
- causation
- aggregate identity
- event versioning
- schema evolution
- compatibility rules
- serialization
- partitioning
- ordering
- consumer groups
- delivery semantics
- idempotency
- deduplication
- retries
- backoff
- dead-letter topics
- poison messages
- replay
- retention
- compaction
- transactional outbox
- observability
- security
- privacy
- disaster recovery
- lifecycle management

---

# 3. Decision Drivers

Primary drivers are:

1. reliability
2. backward compatibility
3. independent deployment
4. deterministic integration
5. idempotent processing
6. operational visibility
7. controlled evolution
8. safe replay
9. data minimization
10. clear ownership
11. scalability
12. maintainability
13. disaster recoverability
14. auditability
15. reduced temporal coupling

---

# 4. Decision

The AstraForge Supply Platform adopts governed event-driven integration based on:

```text
BUSINESS EVENT

↓

STABLE CONTRACT

↓

TRANSACTIONAL OUTBOX

↓

KAFKA

↓

AT-LEAST-ONCE DELIVERY

↓

IDEMPOTENT CONSUMER

↓

CONTROLLED RETRY

↓

OBSERVABLE FAILURE

↓

SAFE REPLAY
```

Kafka events are treated as durable integration contracts rather than implementation details.

---

# 5. Fundamental Principle

The platform adopts:

```text
Events describe business facts.

Commands request actions.

Events do not expose internal entities.

Consumers must tolerate duplicates.

Schemas evolve compatibly.

Failures must remain observable.

Replay must be safe.
```

---

# 6. Event Definition

An event represents a fact that has already occurred.

Examples:

```text
OrderCreated

OrderApproved

OrderRejected

OrderCancelled
```

---

# 7. Past-Tense Semantics

Event names should represent completed facts.

Preferred:

```text
OrderCreated
```

Avoid event names that ambiguously describe commands:

```text
CreateOrder
```

---

# 8. Event vs Command

Conceptually:

```text
COMMAND

CreateOrder

"Please perform this action."
```

versus:

```text
EVENT

OrderCreated

"This business fact occurred."
```

---

# 9. Event Immutability

Published events are immutable historical facts.

A producer must not conceptually modify an already published event.

Corrections require:

- another event
- compensating event
- explicit business correction mechanism

depending on domain semantics.

---

# 10. Business Events

Events should represent meaningful domain occurrences.

Avoid generic technical events such as:

```text
DatabaseRowUpdated
```

unless the integration genuinely requires change-data-capture semantics.

---

# 11. Event Ownership

Every event type must have an owning domain/service.

---

# 12. Producer Ownership

The authoritative domain should own production of the event.

---

# 13. Consumer Independence

Consumers may interpret an event for their own domain without becoming owners of the event contract.

---

# 14. Topic Ownership

Every production topic requires an accountable owner.

---

# 15. Topic Inventory

The platform must maintain an inventory containing at least:

```text
Topic

Owner

Producer

Event Types

Consumers

Partition Strategy

Retention

Classification

Criticality
```

---

# 16. Orphan Topic

Production topics without identifiable ownership are not acceptable.

---

# 17. Topic Naming

Topic names must follow a predictable convention.

A recommended conceptual format is:

```text
<domain>.<entity-or-capability>.<purpose>
```

Examples:

```text
orders.order.events

customers.customer.events

billing.invoice.events
```

Exact enterprise naming conventions may add:

- environment
- organization
- region
- version

where infrastructure standards require them.

---

# 18. Environment in Topic Name

Environment identifiers should be included only when required by the Kafka deployment topology.

If DEV, HML and PROD use isolated clusters, environment duplication in every topic name may be unnecessary.

---

# 19. Topic Name Stability

Topic names should remain stable.

Frequent renaming creates operational and consumer migration complexity.

---

# 20. Sensitive Topic Names

Topic names must not contain:

- customer names
- email addresses
- document numbers
- personal identifiers

---

# 21. Event Type Naming

Event types use explicit domain terminology.

Preferred:

```text
ORDER_CREATED

ORDER_APPROVED

ORDER_REJECTED
```

or equivalent standardized naming.

---

# 22. Ambiguous Names

Avoid:

```text
UPDATE

CHANGE

PROCESS

EVENT
```

without business context.

---

# 23. Event Envelope

Events should use a consistent envelope.

Conceptually:

```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "eventVersion": 1,
  "occurredAt": "2026-07-24T15:00:00Z",
  "traceId": "uuid-or-trace-id",
  "correlationId": "uuid",
  "causationId": "uuid",
  "aggregateType": "ORDER",
  "aggregateId": "uuid",
  "producer": "ecommerce-order-service",
  "payload": {
  }
}
```

Exact fields may vary by integration standard.

---

# 24. Event ID

Every event requires a stable unique identifier:

```text
eventId
```

---

# 25. Event ID Stability

Retries of the same logical event must preserve the same `eventId`.

Do not generate a new ID merely because publication is retried.

---

# 26. Why Stable Event ID

Stable identity enables:

- deduplication
- tracing
- audit
- replay
- incident investigation

---

# 27. Event Type

`eventType` identifies the semantic business event.

---

# 28. Event Version

`eventVersion` identifies the contract version when explicit event versioning is required.

---

# 29. occurredAt

Events require the timestamp at which the business fact occurred.

---

# 30. Publication Timestamp

If publication timing is operationally important, it may be tracked separately from business occurrence time.

---

# 31. occurredAt vs publishedAt

They are different concepts:

```text
occurredAt
→ Business fact happened.

publishedAt
→ Event reached messaging infrastructure.
```

---

# 32. Aggregate Type

`aggregateType` identifies the business aggregate associated with the event.

Example:

```text
ORDER
```

---

# 33. Aggregate ID

`aggregateId` identifies the aggregate instance.

Example:

```text
idOrder
```

---

# 34. Producer

Producer metadata identifies the authoritative service publishing the event.

---

# 35. Trace ID

`traceId` supports distributed technical tracing.

---

# 36. Correlation ID

`correlationId` groups messages belonging to a broader business interaction.

---

# 37. Causation ID

`causationId` identifies the command/event that caused another event.

---

# 38. Correlation vs Causation

Example:

```text
Request A
correlationId = C1

↓

OrderCreated
eventId = E1
correlationId = C1

↓

InvoiceRequested
eventId = E2
correlationId = C1
causationId = E1
```

---

# 39. Identifier Privacy

Tracing/correlation identifiers must remain opaque.

They must not encode personal information.

---

# 40. Payload

The payload contains only event-specific business information.

---

# 41. Payload Minimization

According to ADR-029, payloads must contain only data required by legitimate consumers.

---

# 42. Entity Serialization

Serializing complete JPA entities into Kafka is prohibited.

---

# 43. Event DTO

Events require dedicated contracts independent from persistence entities.

---

# 44. Internal Implementation Leakage

Event contracts must not expose:

- Hibernate proxies
- internal database metadata
- lazy-loading structures
- persistence implementation details

---

# 45. Serialization Format

The platform must use an explicitly governed serialization format.

Supported strategic choices may include:

- JSON
- Avro
- Protobuf

according to enterprise standards.

---

# 46. JSON

JSON provides:

- readability
- broad interoperability
- easy diagnostics

but requires explicit schema governance to prevent uncontrolled evolution.

---

# 47. Avro

Avro provides strong integration with schema registries and compatibility validation.

---

# 48. Protobuf

Protobuf provides compact strongly typed contracts and explicit field numbering.

---

# 49. Format Decision

Serialization format must be standardized at platform level rather than selected independently by every producer.

---

# 50. Schema Registry

Where supported, production event schemas should be managed through an approved schema registry.

---

# 51. Schema as Contract

The schema is a production integration contract.

Changing it requires the same discipline as changing an API contract.

---

# 52. Compatibility

Schema compatibility must be explicitly configured.

Common models include:

```text
BACKWARD

FORWARD

FULL
```

and transitive variants.

---

# 53. Backward Compatibility

Backward compatibility means:

```text
New Consumer

can read

Old Events
```

---

# 54. Forward Compatibility

Forward compatibility means:

```text
Old Consumer

can read

New Events
```

subject to serialization format semantics.

---

# 55. Full Compatibility

Full compatibility combines backward and forward compatibility constraints.

---

# 56. Compatibility Selection

Compatibility mode must reflect deployment and replay requirements.

---

# 57. Preferred Evolution

Prefer additive compatible schema evolution.

---

# 58. Adding Fields

New fields should generally be:

- optional
- nullable where appropriate
- defaulted

according to serialization format.

---

# 59. Required New Field

Adding a mandatory field without a compatible default can break existing consumers or historical replay.

---

# 60. Removing Fields

Fields must not be removed until consumer compatibility and historical replay have been considered.

---

# 61. Rename Is Remove + Add

Renaming a field is effectively:

```text
Remove Old Field

+

Add New Field
```

from a contract perspective.

---

# 62. Field Meaning

The semantic meaning of an existing field must not be silently changed.

---

# 63. Type Change

Changing:

```text
String
```

to:

```text
Integer
```

is generally a breaking contract change.

---

# 64. Semantic Type Change

Even if the wire type remains the same, semantic changes may still be breaking.

Example:

```text
status = internal code
```

becoming:

```text
status = display label
```

is a contract change.

---

# 65. Enum Evolution

Enums require special care.

---

# 66. Unknown Enum Values

Consumers should avoid failing catastrophically solely because a producer introduced a new valid enum value.

---

# 67. Enum Strategy

Where appropriate, consumers should support:

```text
UNKNOWN
```

or equivalent controlled handling.

---

# 68. Event Versioning

Versioning is required when compatible evolution cannot satisfy the business change.

---

# 69. Versioning Strategy

Possible strategies include:

```text
eventVersion field

new event type

new topic
```

depending on compatibility impact.

---

# 70. New Topic for Every Change

Creating a new topic for every small schema change is discouraged.

---

# 71. Breaking Change

A breaking event-contract change requires an explicit migration plan.

---

# 72. Consumer Migration

Migration may follow:

```text
Producer supports compatible/new contract

↓

Consumers migrate

↓

Old contract usage drains

↓

Old version retired
```

---

# 73. Dual Publishing

Temporary dual publishing may be used during major migrations when justified.

---

# 74. Dual Publishing Risk

Dual publishing increases:

- duplicate semantics
- producer complexity
- operational load
- migration risk

and must have a retirement date.

---

# 75. Consumer-Driven Compatibility

Important consumers should be considered during contract evolution.

---

# 76. Schema CI Validation

CI must validate schema compatibility before incompatible changes reach production.

---

# 77. Contract Tests

Critical producer/consumer contracts should have automated tests.

---

# 78. Historical Event Tests

Consumers should be tested against representative older event versions when replay compatibility matters.

---

# 79. Partitioning

Kafka ordering exists only within a partition.

---

# 80. Ordering Guarantee

Do not claim:

```text
Global Topic Ordering
```

for a multi-partition topic.

---

# 81. Partition Key

Partition keys must reflect required ordering semantics.

---

# 82. Aggregate Partitioning

For order events, a typical key is:

```text
idOrder
```

so events for the same order remain in the same partition.

---

# 83. Partition-Key Stability

The partition key for a business stream must remain stable.

---

# 84. Random Partition Key

Random partition keys are inappropriate when aggregate ordering matters.

---

# 85. Personal Data in Key

Personal data should not be used directly as a partition key where an opaque aggregate identifier is available.

---

# 86. Partition Count

Partition count affects:

- throughput
- parallelism
- ordering
- consumer scalability

---

# 87. Partition Increase

Increasing partition count can affect key-to-partition mapping and ordering assumptions.

It must be planned.

---

# 88. Consumer Groups

Each independent logical processing responsibility should use an appropriate consumer group.

---

# 89. Same Consumer Group

Consumers in the same group cooperate to process partitions.

---

# 90. Different Consumer Groups

Different groups independently receive the event stream.

---

# 91. Group Naming

Consumer-group names must identify:

- consuming service
- logical responsibility

and remain stable.

---

# 92. Dynamic Random Group

Random consumer-group names are prohibited for normal production consumers because they can cause uncontrolled replay and retention behavior.

---

# 93. Delivery Semantics

The platform assumes:

```text
AT-LEAST-ONCE
```

delivery for business integration.

---

# 94. Duplicate Delivery

Duplicate delivery is an expected distributed-systems condition, not an exceptional theoretical case.

---

# 95. Exactly-Once

Kafka provides mechanisms that can provide exactly-once semantics within specific Kafka transactional boundaries.

---

# 96. End-to-End Exactly-Once

The platform does not claim generic exactly-once semantics across:

```text
Kafka

+

PostgreSQL

+

HTTP APIs

+

External Systems
```

---

# 97. Why

A consumer may:

```text
1. Receive event

2. Commit database transaction

3. Crash before Kafka offset commit

4. Receive event again
```

---

# 98. Therefore

Consumers must be idempotent.

---

# 99. Idempotency

Processing the same logical event multiple times must not produce incorrect duplicate business effects.

---

# 100. Idempotency Key

The preferred event idempotency key is:

```text
eventId
```

where event identity is stable.

---

# 101. Processed Event Store

A consumer may maintain:

```text
processed_event
```

or equivalent deduplication state when required.

---

# 102. Atomic Deduplication

Where possible, deduplication and business-state mutation should occur in the same local database transaction.

---

# 103. Conceptual Flow

```text
BEGIN

Check eventId

If already processed:
    return safely

Apply business mutation

Record eventId as processed

COMMIT
```

---

# 104. Unique Constraint

A unique constraint on processed `eventId` provides stronger concurrency protection than application-only checks.

---

# 105. Check-Then-Act Race

This alone is unsafe:

```text
SELECT event

if not found:
    INSERT event
```

without appropriate transactional/uniqueness protection.

---

# 106. Natural Idempotency

Some operations are naturally idempotent.

Example:

```text
SET status = APPROVED
```

may be idempotent if domain rules permit repeated execution.

---

# 107. Non-Idempotent Operation

Example:

```text
balance = balance + amount
```

is not naturally idempotent.

---

# 108. External Side Effects

External HTTP calls require explicit idempotency strategy.

---

# 109. External Idempotency Key

Where supported, propagate a stable idempotency key to the external system.

---

# 110. External System Without Idempotency

If an external dependency cannot deduplicate requests, additional local workflow/state control is required.

---

# 111. Offset Commit

Consumer offset strategy must align with business transaction semantics.

---

# 112. Premature Commit

Do not commit the Kafka offset before required durable business processing is complete.

---

# 113. Failure Before Commit

If processing fails before offset commit, redelivery is expected.

---

# 114. Retry

Transient failures should be retried according to a controlled policy.

---

# 115. Retry Classification

Errors should be classified conceptually as:

```text
TRANSIENT

PERMANENT

UNKNOWN
```

---

# 116. Transient Failure

Examples may include:

- temporary network failure
- temporary downstream unavailability
- database connection exhaustion
- rate limiting

---

# 117. Permanent Failure

Examples may include:

- invalid schema
- impossible business invariant
- permanently malformed payload

---

# 118. Retry Permanent Failure

Retrying a permanent error indefinitely is prohibited.

---

# 119. Retry Backoff

Retries should use bounded backoff.

---

# 120. Exponential Backoff

Exponential backoff with bounded jitter may be used for appropriate transient failures.

---

# 121. Retry Storm

Immediate aggressive retries can amplify outages.

---

# 122. Retry Budget

Retry count and duration must be bounded.

---

# 123. Blocking Retry

Long blocking retries on the main consumer thread may prevent progress for unrelated records in the same partition.

---

# 124. Retry Topic

Retry topics may be used when delayed asynchronous retry is required.

---

# 125. Retry Ordering Trade-Off

Moving records to retry topics may affect strict per-key ordering.

This trade-off must be explicit.

---

# 126. Dead-Letter Topic

Messages that cannot be safely processed after the configured policy should be routed to a controlled dead-letter mechanism where appropriate.

---

# 127. DLQ Naming

Dead-letter topics should follow predictable naming.

Example:

```text
orders.order.events.dlt
```

or the approved enterprise equivalent.

---

# 128. DLQ Is Not Success

Moving a message to DLQ does not mean the business transaction succeeded.

---

# 129. DLQ Observability

DLQ publication must generate operational visibility.

---

# 130. DLQ Alert

Critical DLQ growth should trigger an actionable alert.

---

# 131. DLQ Metadata

Dead-letter records should preserve sufficient metadata for diagnosis:

- original topic
- partition
- offset
- eventId
- eventType
- failure classification
- retry count
- failure timestamp

without unnecessarily exposing sensitive information.

---

# 132. Stack Trace

Large stack traces should not be blindly copied into Kafka message headers.

---

# 133. DLQ Retention

DLQs require explicit retention.

---

# 134. Infinite DLQ

DLQ must not become permanent uncontrolled storage.

---

# 135. DLQ Ownership

Every DLQ requires an operational owner.

---

# 136. Poison Message

A poison message consistently fails processing due to message content or incompatible assumptions.

---

# 137. Poison Message Handling

Poison messages must not indefinitely block an entire partition.

---

# 138. Business Investigation

Some poison messages require business remediation rather than technical retry.

---

# 139. Replay

Kafka replay is a production operation with business impact.

---

# 140. Replay Is Not Retry

Retry handles a processing failure.

Replay intentionally reprocesses historical events.

---

# 141. Replay Preconditions

Before replay, determine:

- topic
- consumer group
- partitions
- start position
- end position
- event types
- expected side effects
- idempotency readiness
- downstream impact

---

# 142. Replay Authorization

Production replay requires explicit authorization.

---

# 143. Replay Scope

Replay should use the smallest necessary scope.

---

# 144. Full Topic Replay

Replaying an entire topic is discouraged when a bounded range can solve the problem.

---

# 145. Replay by Offset

Offsets provide precise partition-level control.

---

# 146. Replay by Timestamp

Timestamp-based positioning may be useful when incident timing is known.

---

# 147. Replay Dry Run

Where feasible, replay tooling should provide inspection/dry-run capability.

---

# 148. Replay Rate

Replay must be rate controlled to avoid overwhelming:

- databases
- APIs
- external systems
- caches

---

# 149. Replay Side Effects

Before replay, explicitly evaluate:

```text
Will emails be resent?

Will notifications be duplicated?

Will external APIs be called again?

Will financial state change?

Will audit records duplicate?
```

---

# 150. Replay-Safe Consumers

Critical consumers should be designed for safe replay.

---

# 151. Replay Audit

Production replay operations must be auditable.

---

# 152. Replay Completion

Replay completion requires business validation, not only consumer lag reaching zero.

---

# 153. Retention

Every topic requires an explicit retention strategy.

---

# 154. Retention Drivers

Retention depends on:

- business requirements
- replay requirements
- DR requirements
- privacy requirements
- storage cost

---

# 155. Retention Time

Time-based retention should be explicitly configured.

---

# 156. Retention Size

Size-based retention may complement time-based retention.

---

# 157. Infinite Retention

Infinite retention is prohibited by default.

---

# 158. Privacy

ADR-029 applies to all Kafka retention.

---

# 159. DR

ADR-028 recovery requirements must be compatible with Kafka retention.

---

# 160. Retention Gap

If:

```text
Required Replay Window > Kafka Retention
```

another authoritative recovery source must exist.

---

# 161. Log Compaction

Compaction retains the latest record per key according to Kafka semantics.

---

# 162. Compaction Use Case

Compaction may be appropriate for state-like topics.

---

# 163. Event History

Compaction is generally not a replacement for event-history retention when every event occurrence matters.

---

# 164. Tombstone

Tombstones may represent deletion in compacted topics.

---

# 165. Tombstone Governance

Tombstone behavior must be tested and documented.

---

# 166. Compaction and Privacy

Compaction does not automatically guarantee immediate physical removal of historical personal data.

---

# 167. Transactional Outbox

Business events originating from a PostgreSQL transaction must use the transactional outbox pattern where atomic state/event consistency is required.

---

# 168. Dual Write Problem

This is prohibited as the default:

```text
BEGIN DB TRANSACTION

INSERT order

COMMIT

publish Kafka event
```

because the process may fail between database commit and Kafka publication.

---

# 169. Reverse Dual Write

This is also unsafe:

```text
publish Kafka event

then

commit database
```

because Kafka may contain an event for business state that never committed.

---

# 170. Outbox Solution

Use:

```text
BEGIN TRANSACTION

Persist Business State

Persist Outbox Event

COMMIT
```

---

# 171. Dispatcher

An independent dispatcher publishes pending outbox events to Kafka.

---

# 172. Outbox Event ID

The outbox record must preserve the stable business `eventId`.

---

# 173. Dispatcher Retry

Publishing retries must preserve the same event identity.

---

# 174. Dispatcher Crash

If the dispatcher:

```text
Publishes event

↓

Crashes before marking SENT
```

the event may be published again.

---

# 175. Consequence

Consumers must be idempotent even when the producer uses transactional outbox.

---

# 176. Outbox Status

Typical states may include:

```text
PENDING

SENT

FAILED
```

according to implementation.

---

# 177. Outbox Attempts

Publication attempts must be bounded/observable.

---

# 178. Outbox Failure

Repeated outbox publication failures require operational visibility.

---

# 179. Outbox Ordering

If event order for an aggregate matters, dispatcher design must preserve the required ordering semantics.

---

# 180. Parallel Dispatcher

Parallel outbox dispatch can improve throughput but must not violate required aggregate ordering.

---

# 181. SKIP LOCKED

Database locking mechanisms such as `FOR UPDATE SKIP LOCKED` may support concurrent dispatchers where supported and correctly designed.

---

# 182. Dispatcher Batch

Outbox publication should use bounded batches.

---

# 183. Outbox Retention

Successfully published records require controlled retention according to ADR-029.

---

# 184. Pending Event Deletion

Pending outbox events must never be deleted solely because they are old.

---

# 185. Outbox DR

ADR-028 requires outbox records to participate in database recovery.

---

# 186. Kafka Transactions

Kafka transactions may be used for operations fully contained within supported Kafka transactional semantics.

---

# 187. Kafka Transaction Limitation

Kafka transactions do not magically create atomic transactions with arbitrary PostgreSQL or HTTP operations.

---

# 188. Distributed Transaction

The platform does not adopt XA/distributed two-phase commit as the default Kafka/database consistency strategy.

---

# 189. Saga

Long-running cross-service business workflows may use saga/process-manager patterns where required.

---

# 190. Compensation

Compensating actions must represent valid business semantics rather than technical rollback assumptions.

---

# 191. Consumer State

Consumer business processing state must be durable before successful acknowledgement/offset completion.

---

# 192. Consumer Concurrency

Consumer concurrency must respect:

- partition count
- ordering
- downstream capacity
- database capacity

---

# 193. Excessive Concurrency

More consumer threads do not necessarily increase throughput if:

```text
Partitions < Consumers
```

or downstream dependencies are saturated.

---

# 194. Backpressure

Consumers must avoid overwhelming downstream dependencies.

---

# 195. Rate Limiting

Rate/concurrency limiting may be required for expensive downstream operations.

---

# 196. Batch Consumption

Batch consumption may improve throughput.

---

# 197. Batch Failure

Batch processing requires explicit partial-failure semantics.

---

# 198. Batch Idempotency

Each logical event must remain independently safe from duplicate processing.

---

# 199. Consumer Lag

Consumer lag is a primary operational signal.

---

# 200. Lag Interpretation

Lag alone does not prove failure.

It must be interpreted with:

- traffic volume
- processing rate
- expected latency
- partition distribution

---

# 201. Growing Lag

Persistently growing lag indicates the consumer cannot keep up.

---

# 202. Lag Alert

Critical consumers require actionable lag thresholds.

---

# 203. Lag SLO

Where business processing latency matters, consumer lag/time-to-process should connect to SLOs from ADR-020.

---

# 204. Event Age

Monitoring event age can be more meaningful than raw offset lag for business latency.

---

# 205. Producer Metrics

Monitor at least:

- publish rate
- publish failures
- publish latency
- retry rate

---

# 206. Consumer Metrics

Monitor at least:

- processing rate
- processing latency
- failures
- retries
- lag
- DLQ count

---

# 207. Outbox Metrics

Monitor:

- pending count
- oldest pending age
- publish failures
- retry attempts
- dispatcher latency

---

# 208. Oldest Pending Event

The age of the oldest pending outbox event is often more actionable than queue size alone.

---

# 209. Structured Logging

Kafka logs must follow ADR-019.

---

# 210. Kafka Log Context

Useful context may include:

- eventId
- eventType
- topic
- partition
- offset
- consumer group
- traceId

without full sensitive payloads.

---

# 211. Payload Logging

Full Kafka payload logging is prohibited by default in production.

---

# 212. Trace Propagation

Trace context should propagate through Kafka where supported.

---

# 213. Trace Creation

Consumers should continue or appropriately link trace context according to observability standards.

---

# 214. Trace Failure

Missing trace context must not prevent business event processing.

---

# 215. High Cardinality

Event IDs belong in logs/traces, not metric labels.

---

# 216. Health Checks

Kafka connectivity may contribute to readiness only when inability to communicate with Kafka means the application cannot safely serve its responsibility.

---

# 217. Dependency Semantics

Not every producer must become unready during a temporary Kafka outage if transactional outbox safely buffers events.

---

# 218. Outbox Resilience

With transactional outbox:

```text
Kafka unavailable

↓

Business transaction may still commit

↓

Outbox retains event

↓

Dispatcher publishes later
```

if business requirements permit asynchronous delay.

---

# 219. Queue Capacity

Outbox growth during prolonged Kafka outage must be monitored.

---

# 220. Recovery Surge

After Kafka recovery, dispatchers must avoid uncontrolled surge that overwhelms brokers or consumers.

---

# 221. Security

Kafka requires authentication and authorization according to enterprise platform security.

---

# 222. Producer ACL

A producer should write only to required topics.

---

# 223. Consumer ACL

A consumer should read only required topics/groups.

---

# 224. Administrative ACL

Topic creation/deletion/configuration permissions must be restricted.

---

# 225. Encryption

Kafka traffic and storage protection must follow ADR-029 and platform security standards.

---

# 226. Secret Management

Kafka credentials must follow ADR-026.

---

# 227. Topic Deletion

Deleting a production topic is a high-impact operation requiring controlled authorization.

---

# 228. Auto Topic Creation

Automatic topic creation should be disabled in governed production environments.

---

# 229. Infrastructure as Code

Production topics and important configuration should be declaratively managed where platform capabilities permit.

---

# 230. Topic Configuration

Important configuration includes:

- partitions
- replication factor
- retention
- cleanup policy
- minimum ISR

according to platform standards.

---

# 231. Replication Factor

Production topics require adequate broker replication.

---

# 232. ISR

`min.insync.replicas` and producer acknowledgment strategy must align with durability requirements.

---

# 233. Producer Acknowledgment

Critical producers should use durability-oriented acknowledgment configuration appropriate to the platform.

---

# 234. `acks=all`

For critical events, `acks=all` is generally preferred when supported by the Kafka durability design.

---

# 235. Producer Idempotence

Kafka producer idempotence should be enabled where appropriate.

---

# 236. Producer Idempotence Limitation

Producer idempotence reduces duplicate Kafka writes caused by producer retry behavior.

It does not replace consumer idempotency.

---

# 237. Schema Validation

Malformed or incompatible events should be rejected before normal business processing.

---

# 238. Deserialization Failure

Deserialization errors require controlled handling and observability.

---

# 239. Unknown Event Type

Unknown event types must not silently trigger unrelated business behavior.

---

# 240. Consumer Contract

A consumer must explicitly declare which event types it supports.

---

# 241. Default Switch Branch

Event-type switches require safe handling of unknown values.

---

# 242. Silent Ignore

Silently ignoring a critical unknown event type is discouraged unless explicitly part of the contract.

---

# 243. Consumer Deployment

Consumers must be deployable independently from producers when contracts remain compatible.

---

# 244. Producer Deployment

Producers must not require simultaneous deployment of every consumer for compatible schema evolution.

---

# 245. Temporal Coupling

Schema governance exists partly to reduce coordinated deployment requirements.

---

# 246. Event Choreography

Choreography is appropriate when services react independently to business facts.

---

# 247. Event Orchestration

Explicit orchestration/process management is preferable when a workflow requires:

- controlled sequence
- business state
- timeout
- compensation
- visibility

---

# 248. Choreography Explosion

Long chains of implicit event reactions are discouraged when no component owns the overall business process.

---

# 249. Event Loop

Architecture must prevent accidental event cycles.

Example:

```text
A publishes X

B consumes X and publishes Y

A consumes Y and republishes X
```

without termination semantics.

---

# 250. Event Storm

A single business action must not unintentionally generate an uncontrolled cascade of redundant events.

---

# 251. Event Granularity

Events should be granular enough to express meaningful facts but not so granular that every internal field mutation becomes an integration event.

---

# 252. Snapshot vs Event

A state snapshot and a domain event are different concepts.

---

# 253. Domain Event

```text
OrderApproved
```

means approval occurred.

---

# 254. Snapshot

```text
OrderCurrentState
```

represents current state.

Consumers must know which semantic model they are consuming.

---

# 255. Event Sourcing

Using Kafka does not mean the platform uses Event Sourcing.

---

# 256. Event Sourcing Decision

Event Sourcing requires a separate explicit architectural decision.

---

# 257. Source of Truth

Unless explicitly designed otherwise:

```text
PostgreSQL domain state
```

remains authoritative for transactional service state.

Kafka is the integration event stream.

---

# 258. Rebuild From Kafka

A service must not assume it can rebuild all authoritative state from Kafka unless event completeness and retention were explicitly designed for that purpose.

---

# 259. Data Ownership

Consumers must not reinterpret replicated event data as ownership of another domain's master data.

---

# 260. Cached Projection

Consumers may maintain local projections when justified.

---

# 261. Projection Rebuild

Projection consumers should support rebuild/replay where practical.

---

# 262. Projection Version

Materialized projections may require their own schema/version migration.

---

# 263. Event Contract Documentation

Important events require documentation describing:

- semantic meaning
- producer
- payload
- partition key
- compatibility
- consumers
- retention

---

# 264. Example Payload

Documentation examples must use fictitious data according to ADR-029.

---

# 265. Consumer Registration

Critical consumers should be identifiable before breaking event changes are approved.

---

# 266. Ownership Change

Changing event ownership requires explicit architectural review.

---

# 267. Topic Lifecycle

Topics follow:

```text
PROPOSE

↓

REVIEW

↓

CREATE

↓

OPERATE

↓

EVOLVE

↓

DEPRECATE

↓

RETIRE
```

---

# 268. Topic Deprecation

Deprecated topics require:

- owner
- migration plan
- consumer inventory
- retirement date

---

# 269. Topic Retirement

Before deleting a topic verify:

- no active producer dependency
- no active consumers
- retention obligations satisfied
- DR/replay requirements satisfied

---

# 270. Consumer Group Retirement

Obsolete consumer groups should be intentionally retired.

---

# 271. DR

Kafka DR follows ADR-028.

---

# 272. DR Recovery

Recovery must consider:

```text
Topic Data

Partitions

Consumer Offsets

Schemas

ACLs

Topic Configuration
```

---

# 273. Schema Registry DR

Schema registry metadata must itself have an appropriate recovery strategy.

---

# 274. Replay After DR

DR replay must use the same production replay governance.

---

# 275. Offset Recovery

Recovered offsets must be validated against recovered topic data.

---

# 276. Offset Ahead of Data

An inconsistent recovery where offsets point beyond available recovered data requires explicit remediation.

---

# 277. Offset Behind Data

Offsets behind recovered processing state may cause duplicate delivery.

Idempotency must protect business correctness.

---

# 278. Performance

Kafka performance optimization must be evidence based.

---

# 279. Producer Batching

Producer batching may improve throughput at the cost of latency.

---

# 280. Compression

Compression may reduce network/storage cost at the cost of CPU.

---

# 281. Consumer Fetch

Consumer fetch configuration should align with throughput/latency objectives.

---

# 282. Premature Tuning

Do not modify Kafka settings based solely on generic internet recommendations.

---

# 283. Load Testing

Critical event flows should be load tested.

---

# 284. Load-Test Scope

Measure:

- producer throughput
- broker behavior
- consumer throughput
- lag growth
- database impact
- external dependency impact

---

# 285. Failure Testing

Tests should include:

- broker unavailable
- consumer crash
- duplicate event
- malformed event
- schema mismatch
- database failure
- external API failure
- DLQ routing
- replay

---

# 286. Duplicate Test

Every critical consumer should have an automated test demonstrating duplicate-event safety.

---

# 287. Ordering Test

Consumers relying on aggregate ordering require tests validating ordering assumptions.

---

# 288. Retry Test

Retry tests must verify both transient recovery and permanent failure termination.

---

# 289. DLQ Test

DLQ tests must verify that failed messages remain diagnosable and observable.

---

# 290. Replay Test

Critical consumers should periodically validate replay capability.

---

# 291. Outbox Test

Transactional outbox tests should validate:

```text
Business State + Outbox Event
```

commit atomically.

---

# 292. Dispatcher Duplicate Test

Tests should simulate:

```text
Publish succeeded

but

SENT update failed
```

and verify duplicate-safe downstream behavior.

---

# 293. Architecture Fitness Functions

Automated checks should enforce where practical:

- schema compatibility
- topic naming
- forbidden sensitive fields
- required event metadata
- serialization standards

---

# 294. Sonar/SAST

Kafka code remains subject to normal SonarQube and SAST quality gates.

---

# 295. Java Consumer Design

Consumers should separate:

```text
Kafka Infrastructure

↓

Event Deserialization

↓

Application Use Case

↓

Domain Logic
```

---

# 296. Listener Complexity

`@KafkaListener` methods should remain thin.

---

# 297. Preferred Listener

Conceptually:

```java
@KafkaListener(...)
public void consume(OrderCreatedEvent event) {
    orderCreatedHandler.handle(event);
}
```

rather than embedding complex business orchestration directly in the listener.

---

# 298. Exception Handling

Consumer exception policy should be centralized where practical.

---

# 299. Domain Exception

Domain validation failures must be distinguished from transient infrastructure failures.

---

# 300. Remote Failure

HTTP integration failures should be classified before retry.

---

# 301. Retry Duplication

Do not stack multiple uncontrolled retry layers such as:

```text
WebClient Retry
+
Resilience4j Retry
+
Kafka Listener Retry
+
Retry Topic
```

without an explicit retry budget.

---

# 302. Retry Multiplication

If each layer retries three times:

```text
3 × 3 × 3
```

can produce 27 attempts for one logical operation.

---

# 303. Retry Ownership

Exactly one layer should normally own each retry responsibility.

---

# 304. Circuit Breaker

Circuit breakers may protect external dependencies but do not replace Kafka retry/error handling.

---

# 305. Timeout

External operations triggered by consumers require bounded timeouts.

---

# 306. Virtual Threads

Virtual threads may improve concurrency for blocking I/O workloads, but do not remove:

- downstream capacity limits
- ordering constraints
- Kafka partition limits

---

# 307. Concurrency Budget

Consumer concurrency must be explicitly bounded.

---

# 308. Database Pool

Kafka consumer concurrency must consider available database connections.

---

# 309. Example Constraint

If:

```text
Consumer Concurrency = 50
```

but:

```text
DB Pool = 10
```

the system may create contention rather than throughput.

---

# 310. Graceful Shutdown

Consumers must support graceful shutdown.

---

# 311. Shutdown Objective

During shutdown:

```text
Stop receiving new work

↓

Complete or safely abandon current processing

↓

Commit only completed processing

↓

Terminate
```

---

# 312. Kubernetes

Kafka consumer shutdown behavior must align with Kubernetes termination grace periods.

---

# 313. Rebalance

Consumer rebalances are normal Kafka behavior.

---

# 314. Long Processing

Long-running record processing can interfere with consumer-group stability.

---

# 315. max.poll.interval

`max.poll.interval.ms` must align with realistic processing time.

---

# 316. Poll Thread

Designs must avoid blocking the Kafka poll loop beyond safe limits.

---

# 317. Observed Configuration

Kafka configuration must be based on measured workload behavior.

---

# 318. Anti-Patterns

The following are prohibited or strongly discouraged:

- Kafka topics without owners
- random topic naming
- personal data in topic names
- serializing JPA entities
- using events as database dumps
- changing field meaning silently
- breaking schemas without migration
- adding mandatory fields without compatibility consideration
- uncontrolled enum evolution
- creating a new topic for every trivial schema change
- global-order assumptions on multi-partition topics
- unstable partition keys
- random consumer groups in production
- assuming duplicate delivery will never occur
- claiming generic exactly-once across Kafka/database/HTTP
- consumers without idempotency
- application-only deduplication without concurrency protection
- committing offsets before durable processing
- infinite retries
- immediate retry storms
- retrying permanent failures indefinitely
- DLQs without owners
- DLQs without alerts
- infinite DLQ retention
- poison messages permanently blocking partitions
- production replay without authorization
- replaying more history than required
- replay without evaluating side effects
- replay without rate control
- infinite topic retention by default
- treating compaction as event-history retention
- dual writes instead of transactional outbox
- generating new event IDs on dispatcher retry
- assuming outbox eliminates duplicate delivery
- deleting old pending outbox events
- using XA as the default Kafka/PostgreSQL strategy
- excessive consumer concurrency
- logging complete Kafka payloads
- putting event IDs in metric labels
- automatic production topic creation
- broad producer/consumer ACLs
- untested schema compatibility
- event choreography without workflow ownership
- accidental event cycles
- treating Kafka usage as Event Sourcing
- assuming Kafka can reconstruct state without explicit design
- uncontrolled nested retry layers

---

# 319. Positive Consequences

The decision provides:

- stable event contracts
- predictable schema evolution
- safer independent deployments
- deterministic event identity
- improved tracing
- stronger idempotency
- controlled duplicate handling
- safer retries
- observable poison messages
- governed DLQs
- safer replay
- improved privacy
- better disaster recovery
- clearer ownership
- improved Kafka operations
- reduced distributed consistency risk

---

# 320. Negative Consequences

The decision introduces:

- schema governance overhead
- contract review
- deduplication storage where required
- DLQ operations
- replay procedures
- additional monitoring
- topic lifecycle management
- more integration testing

These costs are accepted because Kafka contracts are long-lived production dependencies.

---

# 321. Neutral Consequences

The decision also means:

- duplicates remain possible
- some consumers require local deduplication tables
- strict ordering may reduce parallelism
- replay can require throttling
- schema changes may require staged migrations
- Kafka does not replace PostgreSQL as transactional source of truth
- transactional outbox improves consistency but does not provide end-to-end exactly-once processing

---

# 322. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Breaking schema change | Critical | Medium | Schema compatibility CI |
| Duplicate event | High | High | Idempotent consumers |
| Event ordering violation | High | Medium | Stable aggregate partition key |
| Poison message | High | Medium | Bounded retry + DLQ |
| DLQ ignored | High | Medium | Ownership + alerts |
| Replay duplicates side effects | Critical | Medium | Idempotency + replay review |
| Consumer lag grows indefinitely | High | Medium | Lag monitoring and capacity planning |
| Kafka outage loses events | Critical | Low | Transactional outbox |
| Outbox duplicate publication | Medium | High | Stable event ID + idempotent consumer |
| Sensitive data leaked in event | Critical | Medium | Contract minimization |
| Topic deleted accidentally | Critical | Low | Restricted administration |
| Offset corruption during DR | Critical | Low | Recovery validation |
| Retry storm overloads dependency | High | Medium | Bounded backoff |
| Consumer concurrency overloads DB | High | Medium | Concurrency budget |
| Unknown event breaks consumer | High | Medium | Compatible evolution |

---

# 323. Implementation Guidance

The following rules are mandatory:

1. Every production topic must have an owner.
2. Every critical event type must have an authoritative producer.
3. Topic names must follow the approved naming convention.
4. Event types must represent explicit business semantics.
5. Events must use stable unique `eventId`.
6. Publication retries must preserve `eventId`.
7. Correlation and causation must remain distinguishable.
8. Event contracts must use dedicated DTO/schema definitions.
9. JPA entities must not be serialized directly to Kafka.
10. Event payloads must be minimized.
11. Serialization format must be governed.
12. Schema compatibility must be validated automatically where possible.
13. Compatible additive evolution is preferred.
14. Field semantics must never change silently.
15. Breaking changes require explicit migration.
16. Partition keys must reflect required ordering.
17. Consumers must not assume global topic ordering.
18. Consumer-group names must remain stable.
19. Business integration assumes at-least-once delivery.
20. Critical consumers must be idempotent.
21. Deduplication must be concurrency safe.
22. Offset completion must occur only after required durable processing.
23. Retries must be bounded.
24. Permanent errors must not retry indefinitely.
25. Retry policies must avoid multiplicative nested retries.
26. DLQs must have owners, retention and monitoring.
27. Poison messages must not indefinitely block processing.
28. Production replay requires authorization.
29. Replay scope must be minimized.
30. Replay must evaluate external side effects.
31. Replay must be rate controlled where necessary.
32. Every topic requires explicit retention.
33. Kafka retention must align with privacy and DR.
34. Compaction must be used only with explicit state semantics.
35. PostgreSQL/Kafka dual writes must use transactional outbox where atomic consistency is required.
36. Outbox retries must preserve event identity.
37. Consumers must tolerate duplicate outbox publication.
38. Pending outbox events must not be silently discarded.
39. Kafka transactions must not be represented as arbitrary distributed transactions.
40. Consumer concurrency must respect downstream capacity.
41. Critical consumer lag must be monitored.
42. Event age should be monitored where processing latency matters.
43. Full event payloads must not be logged by default.
44. Event IDs must not be metric labels.
45. Producer and consumer ACLs must follow least privilege.
46. Automatic topic creation should be disabled in production.
47. Important topic configuration should be declaratively managed.
48. Critical event flows require failure and duplicate-delivery tests.
49. Kafka DR must include topic data, offsets, schemas and configuration.
50. Kafka usage must not be represented as Event Sourcing unless explicitly adopted.

---

# 324. Kafka Production Readiness Gate

A critical event flow is not considered production ready until:

```text
[ ] Topic owner identified

[ ] Producer owner identified

[ ] Consumer inventory documented

[ ] Topic naming validated

[ ] Event semantics documented

[ ] Event ID stable

[ ] Event envelope defined

[ ] Schema registered/governed

[ ] Compatibility mode defined

[ ] Schema CI validation enabled

[ ] Partition key defined

[ ] Ordering requirement documented

[ ] Consumer groups defined

[ ] Idempotency strategy implemented

[ ] Duplicate-event test exists

[ ] Offset strategy reviewed

[ ] Retry classification defined

[ ] Retry count bounded

[ ] Retry backoff defined

[ ] DLQ strategy defined

[ ] DLQ owner defined

[ ] DLQ retention defined

[ ] DLQ alert defined

[ ] Poison-message handling tested

[ ] Replay procedure documented

[ ] Replay authorization defined

[ ] Topic retention defined

[ ] Privacy review completed

[ ] Transactional outbox used where required

[ ] Outbox duplicate scenario tested

[ ] Consumer lag monitored

[ ] Event age monitored where required

[ ] Producer failures monitored

[ ] Outbox backlog monitored

[ ] Kafka ACLs reviewed

[ ] DR strategy reviewed

[ ] Load test completed where critical

[ ] Failure scenarios tested
```

---

# 325. Validation

This ADR will be validated through:

- topic inventory review
- schema-registry validation
- schema compatibility CI
- producer contract tests
- consumer contract tests
- historical-event tests
- duplicate-delivery tests
- ordering tests
- retry tests
- poison-message tests
- DLQ tests
- replay tests
- transactional-outbox tests
- dispatcher duplicate-publication tests
- consumer-lag monitoring
- event-age monitoring
- Kafka failure exercises
- database failure exercises
- external dependency failure tests
- Kafka DR exercises
- security ACL reviews
- privacy reviews
- load testing

---

# 326. Success Criteria

The decision is successful when:

- every critical topic has known ownership
- event contracts evolve without accidental consumer breakage
- duplicate delivery does not corrupt business state
- outbox publication survives Kafka outages
- dispatcher retries do not change event identity
- consumer retries are bounded
- poison messages remain observable
- DLQs are actively managed
- replays can be executed safely
- critical ordering requirements are preserved
- consumer lag is visible and actionable
- event payloads remain minimized
- schemas can be recovered during DR
- production teams can explain the semantics of every critical event flow
- Kafka integration does not depend on unrealistic end-to-end exactly-once assumptions

---

# 327. Alternatives Rejected

## 327.1 Ungoverned JSON Events

Rejected because readability alone does not provide compatibility governance.

---

## 327.2 Database Entities as Events

Rejected because persistence models are not stable integration contracts.

---

## 327.3 Exactly-Once Everywhere

Rejected because distributed side effects extend beyond Kafka transactional boundaries.

---

## 327.4 Infinite Retry

Rejected because permanent failures become endless processing loops.

---

## 327.5 DLQ Without Operational Ownership

Rejected because it merely hides failed business processing.

---

## 327.6 Replay Without Idempotency

Rejected because it can duplicate irreversible business effects.

---

## 327.7 Direct Database + Kafka Dual Write

Rejected because failures between independent commits create inconsistent state.

---

## 327.8 XA / Two-Phase Commit as Default

Rejected because it creates substantial coupling and operational complexity.

---

## 327.9 One Topic per Schema Version

Rejected as the default because it creates unnecessary topic proliferation.

---

## 327.10 Global Ordering

Rejected because it severely constrains scalability and is normally unnecessary when aggregate ordering is sufficient.

---

# 328. Related Decisions

This ADR is related to:

- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-009: Use Apache Kafka for Integration Events
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-031: Adopt Database Performance and Data Access Standards

---

# 329. References

- Apache Kafka Documentation
- Spring for Apache Kafka Documentation
- Confluent Schema Registry Documentation
- CloudEvents Specification
- Enterprise Integration Patterns
- Designing Data-Intensive Applications
- Google Site Reliability Engineering
- OWASP
- ADR-009: Use Apache Kafka for Integration Events
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-019: Adopt Structured Logging
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards

---

# 330. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial Kafka governance baseline |

---

# 331. Decision Summary

The AstraForge Supply Platform treats Kafka as a governed integration platform:

```text
DOMAIN TRANSACTION
        |
        +-------------------+
        |                   |
        v                   v
 BUSINESS STATE       OUTBOX EVENT
        |                   |
        +---------+---------+
                  |
                COMMIT
                  |
                  v
         OUTBOX DISPATCHER
                  |
                  v
                KAFKA
                  |
        +---------+---------+
        |                   |
        v                   v
   CONSUMER A          CONSUMER B
        |                   |
        v                   v
   IDEMPOTENT           IDEMPOTENT
   PROCESSING           PROCESSING
```

Delivery semantics are:

```text
AT-LEAST-ONCE

+

STABLE EVENT ID

+

IDEMPOTENT CONSUMER

+

CONTROLLED RETRY

+

OBSERVABLE FAILURE

+

SAFE REPLAY
```

Not:

```text
"Kafka guarantees that every distributed
business side effect happens exactly once."
```

Schema evolution follows:

```text
CURRENT CONTRACT
       |
       v
ADDITIVE COMPATIBLE CHANGE
       |
       v
OLD + NEW CONSUMERS WORK
       |
       v
CONSUMERS MIGRATE
       |
       v
OLD CONTRACT RETIRED
```

A breaking change requires explicit migration:

```text
BREAKING REQUIREMENT

↓

NEW CONTRACT / VERSION

↓

COMPATIBILITY PLAN

↓

CONSUMER MIGRATION

↓

OBSERVATION

↓

OLD VERSION RETIREMENT
```

Duplicate processing is treated as normal:

```text
EVENT E1
   |
   v
CONSUMER
   |
   v
DATABASE COMMIT
   |
   X
CRASH BEFORE OFFSET COMMIT
   |
   v
EVENT E1 AGAIN
   |
   v
IDEMPOTENCY CHECK
   |
   v
NO DUPLICATE BUSINESS EFFECT
```

Retry follows:

```text
FAILURE
   |
   v
CLASSIFY
   |
   +----------+-----------+
   |                      |
   v                      v
TRANSIENT              PERMANENT
   |                      |
   v                      v
BOUNDED RETRY       NO ENDLESS RETRY
   |                      |
   +----------+-----------+
              |
              v
        SUCCESS OR DLQ
```

Replay follows:

```text
INCIDENT / REBUILD

↓

DEFINE EXACT RANGE

↓

VALIDATE IDEMPOTENCY

↓

EVALUATE SIDE EFFECTS

↓

AUTHORIZE

↓

THROTTLED REPLAY

↓

OBSERVE

↓

BUSINESS RECONCILIATION
```

And Kafka observability must make visible:

```text
Producer Failure

Consumer Failure

Retry

DLQ

Consumer Lag

Event Age

Outbox Backlog

Oldest Pending Outbox Event
```

The definitive architectural principle is:

```text
Kafka provides durable event delivery.

Business correctness comes from
contract governance,
transactional publication,
idempotent processing,
controlled failure handling,
and safe replay.
```
