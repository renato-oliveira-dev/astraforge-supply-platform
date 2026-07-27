# ADR-090: Adopt Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-090 |
| Title | Adopt Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Event-Driven Architecture, Messaging, AWS SQS, Transactional Outbox, Distributed Systems |
| Related Domains | Cart, Orders, Customers, Products, Workflow |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise distributed systems frequently require business operations to cross service and bounded-context boundaries.

Examples include:

```text
CART CHECKOUT

ORDER CREATION

ORDER APPROVAL

WORKFLOW PROCESSING

AUDIT

NOTIFICATIONS

BILLING

FULFILLMENT
```

Implementing every interaction synchronously creates runtime coupling.

```text
SERVICE A
   |
   v
SERVICE B
   |
   v
SERVICE C
   |
   v
SERVICE D
```

A failure in one dependency can propagate through the entire chain.

Event-driven architecture provides another communication model:

```text
PRODUCER
   |
   v
EVENT
   |
   v
BROKER
   |
   +------> CONSUMER A
   |
   +------> CONSUMER B
   |
   +------> CONSUMER C
```

However, messaging introduces its own distributed-systems concerns:

```text
DUPLICATE DELIVERY

MESSAGE LOSS

OUT-OF-ORDER DELIVERY

POISON MESSAGES

RETRIES

DLQ

SCHEMA EVOLUTION

IDEMPOTENCY

EVENT REPLAY

OBSERVABILITY

TRANSACTION CONSISTENCY
```

Therefore, asynchronous messaging MUST be governed as an architectural capability rather than treated merely as an AWS SDK integration.

---

# 2. Problem Statement

The organization requires standards covering:

- Event-Driven Architecture
- AWS SQS
- Standard queues
- FIFO queues
- commands
- domain events
- integration events
- event envelopes
- Transactional Outbox
- outbox dispatchers
- idempotency
- duplicate delivery
- deduplication
- message ordering
- retries
- backoff
- dead-letter queues
- redrive
- poison messages
- visibility timeout
- consumer concurrency
- event contracts
- schema evolution
- event versioning
- correlation IDs
- trace IDs
- business identifiers
- replay
- observability
- security
- PII
- event ownership
- failure handling
- testing
- operational governance

---

# 3. Decision Drivers

Primary drivers are:

1. delivery reliability
2. service autonomy
3. loose runtime coupling
4. transactional consistency
5. idempotent processing
6. contract evolution
7. operational recoverability
8. observability
9. scalability
10. security
11. auditability
12. business-process resilience

---

# 4. Decision

Business events crossing service or bounded-context boundaries SHALL use governed integration-event contracts.

Where reliable publication must remain consistent with local database state, the Transactional Outbox pattern SHALL be preferred.

Consumers MUST be designed under the assumption that duplicate message delivery can occur.

Exactly-once business processing MUST NOT be assumed from broker delivery semantics alone.

---

# 5. Fundamental Principle

```text
A message broker
does not remove
distributed-systems complexity.

It changes
where that complexity
must be managed.
```

---

# 6. Event-Driven Architecture

Event-driven communication SHOULD be used where:

```text
IMMEDIATE RESPONSE IS NOT REQUIRED

PRODUCER AND CONSUMER SHOULD BE DECOUPLED

MULTIPLE CONSUMERS NEED THE SAME FACT

EVENTUAL CONSISTENCY IS ACCEPTABLE

PROCESSING CAN BE RETRIED

BUSINESS WORKFLOW SPANS SERVICES
```

---

# 7. Synchronous Communication

Synchronous HTTP remains appropriate when:

```text
CALLER REQUIRES IMMEDIATE RESULT

CURRENT AUTHORITATIVE DATA IS REQUIRED

OPERATION CANNOT PROCEED WITHOUT RESPONSE
```

---

# 8. Async Is Not Automatically Better

Messaging MUST NOT replace synchronous APIs merely because asynchronous architecture appears more scalable.

---

# 9. Communication Decision

For each interaction ask:

```text
DO I NEED
THE RESULT NOW?
```

If yes:

```text
SYNCHRONOUS
```

may be appropriate.

If no:

```text
ASYNCHRONOUS EVENT
```

may reduce runtime coupling.

---

# 10. Command

A command requests that an action be performed.

Examples:

```text
ApproveOrder

CancelOrder

GenerateInvoice
```

---

# 11. Event

An event states that something has already occurred.

Examples:

```text
OrderCreated

OrderApproved

OrderCancelled

InvoiceGenerated
```

---

# 12. Command Naming

Commands SHOULD normally use imperative language.

---

# 13. Event Naming

Events SHOULD normally use past tense.

---

# 14. Semantic Difference

```text
COMMAND
   =
PLEASE DO THIS

EVENT
   =
THIS HAPPENED
```

---

# 15. Command Consumer

A command normally has one logical handler.

---

# 16. Event Consumer

An event MAY have zero, one or many consumers.

---

# 17. Command Ownership

The target capability/context owns whether a command is accepted.

---

# 18. Event Ownership

The context where the fact occurred owns the event's meaning.

---

# 19. Domain Event

A Domain Event represents a meaningful fact inside a bounded context.

---

# 20. Integration Event

An Integration Event is a contract published outside the bounded context/process boundary.

---

# 21. Separation

Domain Events and Integration Events SHOULD NOT automatically be represented by the same class.

---

# 22. Translation

Preferred flow:

```text
DOMAIN
   |
   v
DOMAIN EVENT
   |
   v
APPLICATION MAPPER
   |
   v
INTEGRATION EVENT
   |
   v
OUTBOX
   |
   v
BROKER
```

---

# 23. Persistence Entity

JPA entities MUST NOT be serialized directly as integration events.

---

# 24. Internal Model Leakage

Internal aggregate structures MUST NOT become public messaging contracts accidentally.

---

# 25. Event Contract

Every integration event MUST have an explicit contract.

---

# 26. Event Envelope

Enterprise integration events SHOULD use a consistent envelope.

Conceptually:

```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "eventVersion": 1,
  "occurredAt": "2026-07-26T12:30:00Z",
  "traceId": "uuid",
  "correlationId": "uuid",
  "producer": "ecommerce-order-service",
  "aggregateType": "ORDER",
  "aggregateId": "uuid",
  "payload": {
  }
}
```

Exact fields MAY vary according to platform requirements.

---

# 27. Event ID

Every externally published event SHOULD contain a globally unique stable `eventId`.

---

# 28. Event ID Purpose

The identifier supports:

```text
IDEMPOTENCY

TRACEABILITY

AUDIT

DEBUGGING

REPLAY
```

---

# 29. Event Type

`eventType` MUST identify the semantic event.

Example:

```text
ORDER_CREATED
```

---

# 30. Event Version

Externally consumed event contracts SHOULD contain an explicit version or otherwise have a documented versioning strategy.

---

# 31. Occurrence Time

`occurredAt` SHOULD represent when the business event occurred.

---

# 32. Publish Time

Publish/dispatch time MAY be recorded separately when operational analysis requires it.

---

# 33. Producer

The producing service/context SHOULD be identifiable.

---

# 34. Aggregate Type

Events SHOULD identify their relevant aggregate/business-resource type where useful.

---

# 35. Aggregate ID

Events SHOULD carry the stable identifier of the affected aggregate/business object.

---

# 36. Correlation ID

Correlation IDs SHOULD connect operations belonging to the same logical business interaction.

---

# 37. Trace ID

Trace identifiers SHOULD support technical observability across service boundaries.

---

# 38. Correlation vs Trace

They MUST NOT be assumed to have identical lifecycle semantics.

---

# 39. Business Process ID

Long-running workflows MAY additionally use a stable business-process identifier.

---

# 40. Actor

Actor/user information MAY be included when required for:

```text
AUDIT

AUTHORIZATION CONTEXT

WORKFLOW RULES
```

subject to privacy standards.

---

# 41. Event Payload

The payload MUST contain only information required by the contract.

---

# 42. Payload Minimalism

Avoid publishing the complete database record merely because it is available.

---

# 43. Payload Self-Sufficiency

Events SHOULD contain enough data for intended consumers to avoid unnecessary synchronous callbacks when practical.

---

# 44. Payload Trade-Off

There is a balance between:

```text
TOO LITTLE DATA
    |
    v
CALLBACK COUPLING
```

and:

```text
TOO MUCH DATA
    |
    v
CONTRACT / PRIVACY COUPLING
```

---

# 45. PII

Events MUST NOT contain unnecessary personally identifiable information.

---

# 46. Secrets

Events MUST NOT contain:

```text
PASSWORDS

ACCESS TOKENS

REFRESH TOKENS

API KEYS

AUTHORIZATION HEADERS

DATABASE CREDENTIALS
```

---

# 47. Encryption

Sensitive event data MUST follow approved encryption/security standards.

---

# 48. SQS

AWS SQS is an approved enterprise messaging mechanism where queue semantics fit the use case.

---

# 49. Standard Queue

SQS Standard SHOULD be preferred when:

```text
HIGH THROUGHPUT

STRICT ORDERING IS NOT REQUIRED

DUPLICATE DELIVERY CAN BE HANDLED
```

---

# 50. FIFO Queue

SQS FIFO SHOULD be considered when:

```text
ORDERING IS BUSINESS-CRITICAL

PROCESSING ORDER CAN BE SCOPED TO A MESSAGE GROUP

FIFO THROUGHPUT CHARACTERISTICS ARE ACCEPTABLE
```

---

# 51. FIFO Is Not Exactly Once

FIFO deduplication MUST NOT be interpreted as guaranteed exactly-once business execution.

---

# 52. Consumer Idempotency Still Required

Consumers of FIFO queues SHOULD still be idempotent.

---

# 53. Message Group

FIFO `MessageGroupId` SHOULD represent the smallest business scope requiring ordered processing.

---

# 54. Example Message Group

For order lifecycle events:

```text
MessageGroupId = orderId
```

MAY be appropriate.

---

# 55. Global Group

Using one global `MessageGroupId` SHOULD be avoided because it serializes unrelated processing.

---

# 56. Deduplication ID

FIFO `MessageDeduplicationId` MAY use the stable event identifier.

---

# 57. Broker Deduplication

Broker deduplication does not replace application idempotency.

---

# 58. Delivery Semantics

Consumers MUST assume:

```text
AT-LEAST-ONCE
```

delivery semantics unless stronger semantics are explicitly proven end-to-end.

---

# 59. Duplicate Causes

Duplicates may occur because of:

```text
VISIBILITY TIMEOUT EXPIRATION

CONSUMER CRASH

NETWORK FAILURE

ACKNOWLEDGEMENT FAILURE

REDRIVE

MANUAL REPLAY

PRODUCER RETRY
```

---

# 60. Idempotency

Processing the same logical message more than once MUST NOT produce unintended duplicate business effects.

---

# 61. Idempotency Example

If:

```text
OrderApproved event
```

is delivered twice, the consumer MUST NOT send two customer notifications if only one notification is intended.

---

# 62. Idempotency Strategies

Approved strategies MAY include:

```text
PROCESSED EVENT TABLE

BUSINESS UNIQUE CONSTRAINT

IDEMPOTENCY KEY

UPSERT

STATE TRANSITION CHECK

CONDITIONAL UPDATE
```

---

# 63. Processed Event Table

A consumer MAY maintain:

```text
processed_event
```

with a unique `event_id`.

---

# 64. Atomic Consumer

Where possible:

```text
CHECK / REGISTER EVENT

+

BUSINESS STATE CHANGE
```

SHOULD occur atomically in the same local transaction.

---

# 65. Check-Then-Act Race

Naive:

```text
if (!exists(eventId)) {
    process();
    save(eventId);
}
```

MAY be unsafe under concurrency without database uniqueness/transactional protection.

---

# 66. Database Constraint

A unique constraint SHOULD provide the final duplicate-processing guard where database-backed idempotency is used.

---

# 67. Business Idempotency

Business state itself MAY provide idempotency.

Example:

```text
IF ORDER ALREADY APPROVED
AND SAME EVENT IS REPLAYED
THEN NO ADDITIONAL TRANSITION
```

when semantics permit.

---

# 68. Side Effects

External side effects require explicit idempotency design.

Examples:

```text
EMAIL

PAYMENT

EXTERNAL API CALL

FILE GENERATION
```

---

# 69. Exactly Once

The architecture MUST NOT claim exactly-once processing merely because one component advertises deduplication.

---

# 70. End-to-End Exactly Once

True exactly-once business semantics require coordination across:

```text
PRODUCER

BROKER

CONSUMER

DATABASE

EXTERNAL SIDE EFFECTS
```

and are rarely available universally.

---

# 71. Transactional Outbox

When a service must atomically persist business state and guarantee eventual event publication, Transactional Outbox SHALL be preferred.

---

# 72. Dual-Write Problem

Unsafe:

```text
BEGIN DB TRANSACTION

UPDATE ORDER

COMMIT

SEND SQS MESSAGE
```

Failure after commit but before SQS publication causes:

```text
DATABASE = UPDATED

QUEUE = MISSING EVENT
```

---

# 73. Reverse Dual Write

Also unsafe:

```text
SEND SQS MESSAGE

UPDATE DATABASE
```

because:

```text
QUEUE = EVENT EXISTS

DATABASE = CHANGE FAILED
```

---

# 74. Outbox Solution

```text
BEGIN TRANSACTION
      |
      +--> UPDATE ORDER
      |
      +--> INSERT OUTBOX_EVENT
      |
      v
COMMIT
```

then asynchronously:

```text
OUTBOX_EVENT
      |
      v
DISPATCHER
      |
      v
SQS
```

---

# 75. Atomicity

Business state and Outbox record MUST be committed in the same local database transaction.

---

# 76. Outbox Event

An Outbox record SHOULD contain sufficient metadata for reliable dispatch.

Example conceptual fields:

```text
id

aggregate_type

aggregate_id

event_type

destination

payload

status

attempts

next_attempt_at

last_error

created_at

sent_at

trace_id
```

---

# 77. Outbox ID

Outbox identity SHOULD correspond to or preserve the stable integration-event identity.

---

# 78. Outbox Status

Typical states MAY include:

```text
PENDING

PROCESSING

SENT

FAILED
```

Exact state design depends on dispatcher implementation.

---

# 79. Outbox Attempt Count

Dispatch attempts SHOULD be tracked.

---

# 80. Next Attempt

Retry scheduling SHOULD be represented explicitly when persistent retry/backoff is required.

---

# 81. Last Error

A sanitized diagnostic error MAY be persisted for operations.

---

# 82. Secret Leakage

Outbox error storage MUST NOT persist secrets or full sensitive remote responses.

---

# 83. Outbox Dispatcher

The dispatcher is infrastructure responsible for publishing pending Outbox events.

---

# 84. Dispatcher Responsibilities

The dispatcher SHOULD:

```text
SELECT ELIGIBLE EVENTS

CLAIM WORK SAFELY

PUBLISH

UPDATE STATUS

RETRY FAILURES

RECORD METRICS
```

---

# 85. Dispatcher Business Logic

The dispatcher MUST NOT contain unrelated Order/Cart business rules.

---

# 86. Dispatcher Concurrency

Multiple dispatcher instances SHOULD be supported safely when horizontal scaling is required.

---

# 87. Double Dispatch

Concurrent dispatcher workers MUST minimize uncontrolled duplicate publication.

---

# 88. Database Locking

Applicable strategies MAY include:

```text
SELECT FOR UPDATE SKIP LOCKED
```

or equivalent database-safe claiming.

---

# 89. Duplicate Publication Still Possible

Even with safe claiming, a crash after successful broker publication but before Outbox status update can cause duplicate publication.

---

# 90. Therefore

Consumers MUST remain idempotent.

---

# 91. Dispatcher Batch

Outbox processing SHOULD use bounded batches.

---

# 92. Batch Size

Batch size MUST be configurable within validated limits.

---

# 93. Unbounded Query

Dispatchers MUST NOT load every pending event into memory.

---

# 94. Dispatcher Polling

Polling frequency SHOULD balance:

```text
LATENCY

DATABASE LOAD

COST
```

---

# 95. Dispatcher Backoff

Repeated publication failures SHOULD use bounded backoff.

---

# 96. Retry Storm

Dispatchers MUST avoid immediate unbounded retry loops.

---

# 97. Permanent Failure

Events exceeding the approved attempt policy MUST become operationally visible.

---

# 98. Failed Outbox

Permanent Outbox failure MUST NOT silently disappear.

---

# 99. Recovery

Operations SHOULD have a controlled mechanism to:

```text
INSPECT

CORRECT

RETRY

REPLAY
```

failed events.

---

# 100. Manual Database Update

Directly changing Outbox statuses manually in production SHOULD be avoided except through approved operational procedures.

---

# 101. Retry

Message processing failures SHOULD be classified before retry.

---

# 102. Transient Failure

Examples:

```text
NETWORK TIMEOUT

TEMPORARY 503

DATABASE CONNECTION FAILURE

THROTTLING
```

MAY justify retry.

---

# 103. Permanent Failure

Examples:

```text
INVALID CONTRACT

UNSUPPORTED EVENT VERSION

MISSING REQUIRED BUSINESS DATA

IMPOSSIBLE STATE TRANSITION
```

generally SHOULD NOT be retried indefinitely.

---

# 104. Backoff

Retries SHOULD use bounded exponential or policy-based backoff where applicable.

---

# 105. Jitter

Jitter SHOULD be considered to avoid synchronized retry storms.

---

# 106. Retry Limit

Every retry mechanism MUST have a defined limit or terminal state.

---

# 107. Nested Retries

Retry policies across:

```text
HTTP CLIENT

APPLICATION

SQS

OUTBOX
```

MUST be reviewed together.

---

# 108. Retry Multiplication

If each layer retries independently:

```text
3 x 3 x 3
```

can produce:

```text
27 ATTEMPTS
```

for one logical operation.

---

# 109. Retry Ownership

One layer SHOULD own each retry responsibility whenever practical.

---

# 110. Visibility Timeout

SQS visibility timeout MUST exceed expected message-processing duration with appropriate margin.

---

# 111. Too Short

If visibility timeout is too short:

```text
CONSUMER A STILL PROCESSING
       |
       v
MESSAGE BECOMES VISIBLE
       |
       v
CONSUMER B RECEIVES SAME MESSAGE
```

---

# 112. Too Long

Excessive visibility timeout delays recovery after consumer failure.

---

# 113. Dynamic Extension

Visibility extension MAY be used for legitimately long-running processing.

---

# 114. Long Processing

Very long-running work SHOULD trigger architectural review rather than relying indefinitely on visibility extensions.

---

# 115. Dead-Letter Queue

Production queues SHOULD normally have a DLQ when messages can fail permanently.

---

# 116. DLQ Purpose

A DLQ isolates messages that cannot be processed successfully after the approved retry policy.

---

# 117. DLQ Is Not Disposal

A DLQ is NOT a trash can.

---

# 118. DLQ Monitoring

DLQ message count MUST be monitored.

---

# 119. DLQ Alert

Unexpected DLQ growth SHOULD generate an operational alert.

---

# 120. DLQ Inspection

Operators MUST have a safe way to inspect failure metadata.

---

# 121. DLQ Payload Security

DLQ access MUST follow the same or stronger security controls as the source queue.

---

# 122. Redrive

DLQ redrive MUST be controlled.

---

# 123. Blind Redrive

Mass redrive without understanding the failure cause is prohibited.

---

# 124. Fix Before Replay

Before redrive:

```text
IDENTIFY CAUSE

FIX CAUSE

VERIFY IDEMPOTENCY

SELECT SCOPE

REDRIVE
```

---

# 125. Poison Message

A poison message repeatedly fails because its content or semantics cannot be processed.

---

# 126. Poison Handling

Poison messages SHOULD reach a terminal failure path rather than consume resources indefinitely.

---

# 127. Validation

Message-envelope validation SHOULD occur before business processing.

---

# 128. Invalid JSON

Malformed payloads SHOULD fail predictably and eventually reach the approved failure path.

---

# 129. Unsupported Version

Unsupported event versions MUST produce an explicit diagnostic result.

---

# 130. Unknown Event Type

Unknown event types MUST NOT be silently treated as known events.

---

# 131. Schema Evolution

Event contracts MUST evolve compatibly whenever possible.

---

# 132. Additive Change

Adding an optional field is generally safer than removing or changing an existing field's semantics.

---

# 133. Breaking Change

Breaking changes require explicit versioning/migration.

---

# 134. Semantic Compatibility

Schema compatibility is insufficient if field meaning changes.

---

# 135. Example

Changing:

```text
status = APPROVED
```

from:

```text
analyst approval
```

to:

```text
final approval
```

is a semantic breaking change even if the JSON type remains `String`.

---

# 136. Event Version Strategy

Approved strategies MAY include:

```text
VERSION FIELD

VERSIONED EVENT TYPE

VERSIONED SCHEMA
```

---

# 137. Version Explosion

Creating a new event version for every additive field SHOULD be avoided.

---

# 138. Consumer Tolerance

Consumers SHOULD ignore unknown optional fields unless strict validation is required by the contract.

---

# 139. Required Fields

Removing required fields requires compatibility planning.

---

# 140. Enum Evolution

Consumers MUST be designed for possible future enum values where contracts permit extension.

---

# 141. Java Enum Risk

Direct deserialization into a closed Java enum can break when a producer adds a new value.

---

# 142. Enum Strategy

Integration contracts SHOULD define how unknown enum values are handled.

---

# 143. Event Contract Repository

Event schemas/contracts SHOULD be version-controlled.

---

# 144. Contract Review

Changes to integration events SHOULD receive architectural/consumer-impact review.

---

# 145. Consumer Inventory

Important events SHOULD have identifiable consumers.

---

# 146. Unknown Consumers

Public enterprise events SHOULD assume consumers may exist outside the producing team's immediate codebase.

---

# 147. Deletion

An event contract MUST NOT be removed merely because no consumer exists in the producer repository.

---

# 148. Deprecation

Event deprecation SHOULD have:

```text
ANNOUNCEMENT

MIGRATION WINDOW

CONSUMER VALIDATION

REMOVAL DATE
```

where governance requires it.

---

# 149. Ordering

Ordering MUST be treated as an explicit business requirement.

---

# 150. Global Ordering

Global ordering SHOULD be avoided unless absolutely required.

---

# 151. Aggregate Ordering

Most business ordering requirements SHOULD be scoped to:

```text
AGGREGATE ID

CUSTOMER ID

WORKFLOW ID
```

---

# 152. Standard Queue Ordering

SQS Standard consumers MUST tolerate messages arriving out of order.

---

# 153. Example

A consumer MAY theoretically observe:

```text
ORDER_APPROVED
```

before:

```text
ORDER_CREATED
```

under certain distributed delivery/replay conditions.

The consumer design MUST define how such cases are handled if relevant.

---

# 154. Version/Sequence

An aggregate sequence/version MAY help consumers detect stale or out-of-order events.

---

# 155. Stale Event

Consumers MAY ignore events older than already-processed aggregate state when business semantics permit.

---

# 156. FIFO Ordering

FIFO ordering applies within a Message Group.

---

# 157. Parallelism

Different FIFO message groups MAY process concurrently.

---

# 158. Consumer Concurrency

Consumer concurrency MUST be bounded.

---

# 159. Unbounded Thread Creation

Consumers MUST NOT create unbounded threads per message.

---

# 160. Java 21

Java 21 Virtual Threads MAY be used for I/O-heavy consumer processing when compatible with downstream resource limits.

---

# 161. Virtual Threads Do Not Remove Limits

Even with Virtual Threads:

```text
DATABASE CONNECTIONS

HTTP CONNECTIONS

REMOTE RATE LIMITS

SQS IN-FLIGHT LIMITS
```

remain finite.

---

# 162. Bulkhead

Concurrency SHOULD be limited according to downstream capacity.

---

# 163. Backpressure

Consumers MUST have a strategy for workloads exceeding downstream processing capacity.

---

# 164. Queue as Buffer

The queue MAY absorb temporary load spikes.

---

# 165. Queue Is Not Infinite Capacity

Sustained producer rate greater than consumer capacity creates growing backlog.

---

# 166. Queue Depth

Queue depth MUST be monitored.

---

# 167. Age of Oldest Message

The age of the oldest message is a critical operational metric.

---

# 168. Consumer Lag

Messaging health SHOULD consider:

```text
QUEUE DEPTH

OLDEST MESSAGE AGE

PROCESSING RATE

FAILURE RATE

DLQ RATE
```

---

# 169. Scaling

Consumer scaling SHOULD be driven by:

```text
BACKLOG

PROCESSING LATENCY

DOWNSTREAM CAPACITY
```

rather than CPU alone.

---

# 170. Autoscaling

Autoscaling MAY use queue metrics where infrastructure supports it.

---

# 171. Downstream Protection

Autoscaling consumers MUST NOT overwhelm databases or external APIs.

---

# 172. Batch Receive

SQS consumers SHOULD use efficient bounded batch receive where supported.

---

# 173. Partial Batch Failure

Batch consumers MUST define how partial processing failures are handled.

---

# 174. Whole Batch Retry

Retrying an entire batch because one message failed MAY duplicate successful work.

---

# 175. Partial Failure Reporting

Where supported, partial batch failure semantics SHOULD be used.

---

# 176. Consumer Transaction

Each message SHOULD normally have an explicit local processing transaction.

---

# 177. Transaction Scope

The transaction SHOULD include:

```text
IDEMPOTENCY RECORD

LOCAL BUSINESS STATE
```

where atomicity is required.

---

# 178. Acknowledge

A message MUST NOT be considered successfully processed before required local durable changes succeed.

---

# 179. External Side Effect

If message processing includes an external side effect, failure semantics MUST be explicitly designed.

---

# 180. Consumer Outbox

A consumer MAY use its own Outbox to reliably trigger further external events.

---

# 181. Event Chain

Conceptually:

```text
SQS EVENT
   |
   v
CONSUMER
   |
   v
LOCAL TRANSACTION
   |
   +--> BUSINESS CHANGE
   |
   +--> OUTBOX EVENT
   |
   v
COMMIT
```

---

# 182. Event Cascade

Long uncontrolled event chains SHOULD be avoided.

---

# 183. Event Loop

Architectures MUST prevent accidental loops such as:

```text
A publishes X
B consumes X
B publishes Y
A consumes Y
A publishes X
...
```

---

# 184. Event Causation

A `causationId` MAY be included where event-chain diagnosis requires it.

---

# 185. Correlation

A correlation identifier SHOULD remain stable across one logical distributed business flow.

---

# 186. Trace Propagation

Trace context SHOULD be propagated through message headers/envelopes according to observability standards.

---

# 187. Logging

Message processing logs SHOULD include controlled identifiers such as:

```text
eventId

eventType

aggregateId

traceId

correlationId
```

where permitted.

---

# 188. Payload Logging

Full production message payloads SHOULD NOT be logged by default.

---

# 189. Sensitive Data

Sensitive fields MUST be masked or omitted.

---

# 190. Log Injection

Message-derived text MUST be handled according to secure logging standards.

---

# 191. Metrics

Producers SHOULD expose metrics for:

```text
EVENTS CREATED

OUTBOX PENDING

OUTBOX SENT

OUTBOX FAILED

DISPATCH LATENCY
```

---

# 192. Consumer Metrics

Consumers SHOULD expose:

```text
MESSAGES RECEIVED

MESSAGES PROCESSED

MESSAGES FAILED

DUPLICATES DETECTED

PROCESSING DURATION

DLQ / TERMINAL FAILURES
```

---

# 193. Cardinality

Metrics MUST NOT use event IDs or aggregate UUIDs as metric labels.

---

# 194. Tracing

Distributed traces SHOULD connect producer and consumer processing where supported.

---

# 195. Async Trace

Async processing creates a causal boundary and SHOULD use appropriate messaging trace semantics rather than pretending it is a synchronous HTTP span.

---

# 196. Alerting

Operational alerts SHOULD exist for:

```text
DLQ > THRESHOLD

OLDEST MESSAGE AGE > SLO

OUTBOX BACKLOG

DISPATCH FAILURE RATE

CONSUMER FAILURE RATE
```

---

# 197. Outbox Backlog

A growing Outbox backlog is a production reliability incident even when application APIs remain healthy.

---

# 198. Readiness

Service readiness SHOULD consider critical messaging dependencies according to platform availability strategy.

---

# 199. Broker Outage

Temporary SQS unavailability SHOULD NOT necessarily make business APIs unavailable when Outbox safely buffers publication.

---

# 200. Outbox Advantage

This allows:

```text
DATABASE AVAILABLE

SQS TEMPORARILY UNAVAILABLE
```

while business transactions continue if eventual publication remains acceptable.

---

# 201. Outbox Capacity

Outbox growth during broker outage MUST be operationally bounded and monitored.

---

# 202. Retention

Sent Outbox events SHOULD have a retention/cleanup strategy.

---

# 203. Immediate Delete

Immediate deletion after dispatch MAY reduce forensic capability.

---

# 204. Permanent Retention

Permanent retention MAY create unnecessary database growth.

---

# 205. Retention Policy

Retention SHOULD balance:

```text
AUDIT

REPLAY

FORENSICS

STORAGE

PRIVACY
```

---

# 206. Cleanup

Outbox cleanup SHOULD operate in bounded batches.

---

# 207. Cleanup Locking

Cleanup MUST avoid causing excessive contention with active dispatch.

---

# 208. Partitioning

High-volume Outbox tables MAY require partitioning according to database architecture standards.

---

# 209. Indexes

Outbox indexes SHOULD support dispatcher access patterns.

Typical candidates:

```text
status

next_attempt_at

created_at
```

according to actual query plans.

---

# 210. Replay

Event replay MAY be required for:

```text
RECOVERY

NEW CONSUMER BOOTSTRAP

PROJECTION REBUILD

CORRECTED PROCESSING
```

---

# 211. Replay Safety

Consumers MUST define whether they are replay-safe.

---

# 212. Replay vs Retry

Replay is not the same as automatic retry.

---

# 213. Replay Scope

Replay SHOULD be selectable by:

```text
EVENT TYPE

DATE RANGE

AGGREGATE

EVENT ID
```

where operational tooling supports it.

---

# 214. Replay Authorization

Production replay MUST require controlled authorization.

---

# 215. Replay Audit

Manual replay SHOULD be auditable.

---

# 216. Replay Idempotency

Idempotency rules MUST continue to apply during replay.

---

# 217. Reprocessing

Sometimes reprocessing intentionally needs a new business effect.

This MUST be modeled explicitly rather than bypassing idempotency controls casually.

---

# 218. Event Store

Transactional Outbox MUST NOT automatically be treated as a permanent Event Store.

---

# 219. Event Sourcing

Using events for integration does not mean the system uses Event Sourcing.

---

# 220. SQS Queue Ownership

Every queue MUST have an identifiable owning team/service.

---

# 221. Queue Naming

Queue names SHOULD follow enterprise naming conventions and identify:

```text
ENVIRONMENT

DOMAIN / SERVICE

PURPOSE
```

as required by infrastructure governance.

---

# 222. Environment Isolation

DEV, QA, UAT and PROD MUST use appropriately isolated queues.

---

# 223. Production Queue

Non-production services MUST NOT consume production queues.

---

# 224. IAM

Producer and consumer permissions MUST follow least privilege.

---

# 225. Producer IAM

A producer requiring only `SendMessage` SHOULD NOT receive broad administrative SQS permissions.

---

# 226. Consumer IAM

Consumers SHOULD receive only the queue operations required for processing.

---

# 227. DLQ IAM

DLQ inspection/redrive permissions SHOULD be restricted.

---

# 228. Encryption at Rest

Production queues SHOULD use approved encryption-at-rest configuration.

---

# 229. Encryption in Transit

Messaging communication MUST use approved secure transport.

---

# 230. AWS Credentials

Static AWS credentials MUST NOT be embedded in application configuration or source code.

---

# 231. Workload Identity

Workload identity/IAM roles SHOULD be preferred.

---

# 232. Local Development

Local SQS emulation MAY be used for development/integration tests where appropriate.

---

# 233. Endpoint Override

SQS endpoint override SHOULD be configurable for local/test environments only.

---

# 234. Production Endpoint Override

Unexpected custom endpoint overrides MUST NOT be enabled in production.

---

# 235. Region

AWS region MUST be explicitly governed/configured.

---

# 236. SQS Client

`SqsAsyncClient` MAY be used by infrastructure adapters/dispatchers.

---

# 237. SDK Leakage

AWS SDK classes SHOULD NOT leak into domain models.

---

# 238. Client Reuse

SQS clients SHOULD be reused rather than recreated per message.

---

# 239. Connection Pool

HTTP client resources underlying AWS SDK clients MUST be bounded.

---

# 240. Shutdown

Messaging clients/executors MUST participate in graceful shutdown.

---

# 241. Graceful Shutdown

On shutdown, consumers SHOULD:

```text
STOP RECEIVING NEW WORK

ALLOW BOUNDED IN-FLIGHT COMPLETION

RELEASE RESOURCES
```

according to platform shutdown standards.

---

# 242. Duplicate During Shutdown

Shutdown interruption may cause redelivery; idempotency remains required.

---

# 243. Order Service

Orders SHOULD publish integration events for facts owned by Orders.

Potential examples:

```text
ORDER_CREATED

ORDER_APPROVED

ORDER_REJECTED

ORDER_REVIEW_REQUESTED
```

according to actual business contracts.

---

# 244. Order Event Payload

A workflow event MAY contain:

```text
eventId

eventType

traceId

segment

idCustomer

idOrder

typeOrder

statusOrder

requestUser

userProfileWorkflow

process

comments
```

when required by the Workflow contract.

---

# 245. Minimal Contract

Fields MUST exist because consumers need their semantics, not merely because they are available in the Order entity.

---

# 246. Orders Outbox

Order state mutation and corresponding Workflow event registration SHOULD share one local transaction when both represent one committed business operation.

---

# 247. Orders Dispatcher

The Orders Outbox dispatcher SHOULD:

```text
PROCESS BOUNDED BATCHES

SUPPORT MULTIPLE INSTANCES SAFELY

USE CONFIGURED MAX ATTEMPTS

APPLY BACKOFF

RECORD SANITIZED FAILURES

EXPOSE METRICS
```

---

# 248. Orders Event ID

`eventId` MUST remain stable across dispatch retries.

---

# 249. Wrong Behavior

Do NOT generate:

```text
NEW EVENT ID
```

for every SQS retry of the same Outbox event.

---

# 250. Why

A new identifier would defeat consumer event-level deduplication.

---

# 251. Workflow Consumer

Workflow MUST treat Order events as external integration contracts.

---

# 252. Workflow Local Model

Workflow SHOULD translate Order event payloads into its own process model.

---

# 253. Workflow Idempotency

Workflow MUST safely handle duplicate Order events.

---

# 254. Workflow State

Duplicate messages MUST NOT create duplicate workflow instances unless explicitly intended.

---

# 255. Workflow Ordering

If workflow transitions require per-order ordering, FIFO/message-group or explicit aggregate-version handling SHOULD be evaluated.

---

# 256. Cart

Cart SHOULD use messaging only for facts/events whose asynchronous semantics fit the checkout/business process.

---

# 257. Checkout

If successful checkout requires immediate Order creation confirmation, synchronous Cart → Orders communication MAY remain appropriate.

---

# 258. Post-Checkout Event

Secondary actions after successful checkout MAY use asynchronous events.

---

# 259. Notification

Notifications SHOULD generally not block critical order transactions when asynchronous processing is acceptable.

---

# 260. Audit

Audit integration MAY use asynchronous messaging when business/audit guarantees are preserved.

---

# 261. Critical Audit

If an audit record must be atomically guaranteed with the business state, Outbox or local transactional audit persistence SHOULD be used.

---

# 262. Event Consumer Error Model

Consumer failures SHOULD distinguish:

```text
RETRYABLE

NON-RETRYABLE

DUPLICATE

IGNORED / STALE

SUCCESS
```

where operationally useful.

---

# 263. Swallowed Exception

Consumers MUST NOT catch exceptions and silently acknowledge failed messages.

---

# 264. Generic Catch

A broad catch MAY exist at the message boundary only when it:

```text
LOGS SAFELY

CLASSIFIES FAILURE

PRESERVES RETRY/DLQ SEMANTICS
```

---

# 265. `printStackTrace`

`printStackTrace()` is prohibited.

---

# 266. Logging Exception

Exceptions SHOULD be logged once at the appropriate ownership boundary.

---

# 267. Retry Logging

Repeated retries SHOULD avoid producing uncontrolled log storms.

---

# 268. Testing Producer

Producer tests SHOULD verify:

```text
EVENT TYPE

EVENT VERSION

EVENT ID STABILITY

PAYLOAD MAPPING

OUTBOX PERSISTENCE
```

---

# 269. Outbox Test

Integration tests SHOULD verify that business state and Outbox state commit or roll back together.

---

# 270. Dispatcher Test

Dispatcher tests SHOULD verify:

```text
SUCCESSFUL PUBLICATION

RETRY

MAX ATTEMPTS

STATUS TRANSITION

BACKOFF

DUPLICATE-SAFE CLAIMING
```

where applicable.

---

# 271. Consumer Test

Consumer tests SHOULD verify:

```text
VALID EVENT

DUPLICATE EVENT

INVALID EVENT

UNSUPPORTED VERSION

TRANSIENT FAILURE

PERMANENT FAILURE
```

---

# 272. Idempotency Test

At least one test SHOULD demonstrate that duplicate delivery does not duplicate the protected business effect.

---

# 273. Contract Test

Producer/consumer compatibility SHOULD be tested where event contracts are business critical.

---

# 274. Local Integration Test

Local SQS-compatible infrastructure MAY be used through Testcontainers/LocalStack where approved.

---

# 275. AWS Unit Test

Unit tests SHOULD NOT require real AWS connectivity.

---

# 276. Domain Test

Domain tests MUST NOT require SQS.

---

# 277. Application Test

Application tests SHOULD depend on event-publisher/outbox abstractions rather than AWS SDK internals.

---

# 278. AssertJ

Java messaging tests SHOULD follow project AssertJ/Sonar conventions.

Example:

```java
assertThat(savedEvent.status())
        .as("should keep the workflow event pending until it is dispatched")
        .isEqualTo(OutboxStatus.PENDING);
```

---

# 279. Deterministic Tests

Tests SHOULD use stable event IDs/timestamps where assertions require determinism.

---

# 280. Sleep

`Thread.sleep()` SHOULD NOT be used to coordinate asynchronous messaging tests.

---

# 281. Awaiting Async Results

Use deterministic synchronization or approved awaiting mechanisms with bounded timeout.

---

# 282. Test Constants

Stable test constants SHOULD be used for:

```text
EVENT IDS

ORDER IDS

TRACE IDS

CUSTOMER IDS
```

where appropriate.

---

# 283. Architecture Fitness Functions

Messaging architecture SHOULD enforce rules such as:

```text
DOMAIN MUST NOT DEPEND ON AWS SDK

DOMAIN MUST NOT DEPEND ON SQS LISTENERS

CONTROLLERS MUST NOT DIRECTLY SEND SQS

JPA ENTITIES MUST NOT BE EVENT CONTRACTS

EVENT ADAPTERS MUST REMAIN OUTSIDE DOMAIN

OUTBOX DISPATCHER MUST NOT CONTAIN DOMAIN BUSINESS RULES
```

---

# 284. Operational Runbook

Critical messaging flows MUST have operational procedures covering:

```text
QUEUE BACKLOG

DLQ

OUTBOX FAILURE

BROKER OUTAGE

CONSUMER FAILURE

REDRIVE

REPLAY
```

---

# 285. Incident Diagnosis

Recommended diagnostic order:

```text
1. IS PRODUCER CREATING EVENTS?

2. ARE OUTBOX EVENTS PENDING?

3. IS DISPATCHER HEALTHY?

4. IS SQS RECEIVING?

5. IS QUEUE BACKLOG GROWING?

6. ARE CONSUMERS RUNNING?

7. ARE FAILURES RETRYING?

8. IS DLQ GROWING?

9. ARE DUPLICATES BEING DETECTED?

10. IS DOWNSTREAM CAPACITY SATURATED?
```

---

# 286. Messaging Review Checklist

```text
[ ] Is this interaction genuinely asynchronous?

[ ] Is it a command or event?

[ ] Who owns the event?

[ ] Is the event contract explicit?

[ ] Is eventId stable?

[ ] Is event versioning defined?

[ ] Is correlation supported?

[ ] Does payload contain only required data?

[ ] Does payload avoid secrets/unnecessary PII?

[ ] Is Outbox required?

[ ] Is producer dual-write safe?

[ ] Is consumer idempotent?

[ ] Is duplicate delivery tested?

[ ] Is ordering required?

[ ] Standard or FIFO?

[ ] Is MessageGroupId scoped correctly?

[ ] Are retries bounded?

[ ] Are nested retries controlled?

[ ] Is visibility timeout appropriate?

[ ] Is a DLQ configured?

[ ] Is DLQ monitored?

[ ] Is redrive controlled?

[ ] Can unsupported versions be diagnosed?

[ ] Are queue depth and oldest-message age monitored?

[ ] Is concurrency bounded?

[ ] Are downstream resources protected?

[ ] Is replay safe?

[ ] Is graceful shutdown defined?
```

---

# 287. Outbox Review Checklist

```text
[ ] Business change and Outbox insert use the same transaction

[ ] Event ID remains stable across retries

[ ] Dispatcher queries bounded batches

[ ] Dispatcher supports safe horizontal concurrency

[ ] Duplicate publication remains harmless to consumers

[ ] Retry attempts are persisted

[ ] Backoff is bounded

[ ] Permanent failures become visible

[ ] Errors are sanitized

[ ] Sent-event retention is defined

[ ] Cleanup is bounded

[ ] Required indexes exist

[ ] Backlog metrics exist

[ ] Broker outage behavior is understood

[ ] Operational replay/retry procedure exists
```

---

# 288. Consumer Review Checklist

```text
[ ] Duplicate event produces no unintended duplicate effect

[ ] Idempotency is concurrency safe

[ ] Business state and idempotency record are atomic where required

[ ] Invalid payload is classified correctly

[ ] Unsupported version is explicit

[ ] Retryable failures are distinguishable

[ ] Permanent failures do not retry forever

[ ] Message is not acknowledged before durable processing

[ ] Ordering assumptions are explicit

[ ] Stale-event behavior is defined

[ ] External side effects have idempotency semantics

[ ] Logging avoids sensitive payloads

[ ] Metrics avoid high-cardinality identifiers

[ ] Consumer concurrency is bounded

[ ] Shutdown/redelivery behavior is safe
```

---

# 289. Enterprise Messaging Gate

A change is not considered compliant when applicable conditions include:

```text
[ ] Database update and SQS publication use unsafe dual writes

[ ] Event ID changes on every retry

[ ] Consumer assumes message is delivered only once

[ ] Consumer has no idempotency strategy for non-idempotent effects

[ ] JPA entity is serialized directly to SQS

[ ] Event payload contains credentials or tokens

[ ] Event contract has no ownership

[ ] Breaking event change has no versioning strategy

[ ] FIFO uses one global MessageGroupId without justification

[ ] Standard queue consumer assumes strict ordering

[ ] Retry is unbounded

[ ] Multiple retry layers multiply attempts uncontrollably

[ ] Visibility timeout is lower than normal processing duration without mitigation

[ ] Production queue lacks required DLQ

[ ] DLQ exists but is not monitored

[ ] DLQ is blindly redriven

[ ] Consumer catches exception and acknowledges failure silently

[ ] Queue backlog/oldest-message age are not observable

[ ] Outbox dispatcher loads unbounded records

[ ] Outbox permanent failures disappear silently

[ ] Consumer concurrency ignores downstream capacity

[ ] Production message payloads containing PII are logged unnecessarily

[ ] AWS SDK types leak into domain logic

[ ] Event replay bypasses idempotency without explicit business intent
```

---

# 290. Anti-Patterns

The following are prohibited or strongly discouraged:

- fire-and-forget without delivery guarantees
- database/SQS dual writes
- new event ID on retry
- exactly-once assumptions
- non-idempotent consumers
- check-then-act deduplication without concurrency protection
- JPA entities as events
- secrets in events
- complete database rows as event payloads
- indefinite retry
- nested retry explosion
- DLQ as permanent garbage storage
- blind DLQ redrive
- global FIFO MessageGroupId
- assuming SQS Standard ordering
- unbounded consumer concurrency
- unbounded Outbox polling
- `Thread.sleep()` in messaging tests
- full payload logging in production
- AWS SDK inside domain code
- event versioning based only on JSON syntax
- silent unsupported-event handling
- uncontrolled event choreography
- using Outbox as Event Sourcing without an explicit decision

---

# 291. Positive Consequences

The decision provides:

- reliable event publication
- reduced runtime coupling
- stronger transactional consistency
- duplicate-safe consumers
- explicit messaging contracts
- controlled event evolution
- better failure recovery
- observable queue health
- safer horizontal scaling
- clearer producer/consumer ownership
- improved workflow resilience
- stronger operational governance

---

# 292. Negative Consequences

The decision introduces:

- Outbox persistence
- dispatcher infrastructure
- idempotency storage/logic
- DLQ operations
- event version governance
- eventual consistency
- additional monitoring
- replay procedures
- more distributed failure modes

These costs are accepted where asynchronous integration provides material architectural value.

---

# 293. Neutral Consequences

The decision also means:

- not every integration should use events
- HTTP remains valid for immediate request/response
- SQS Standard remains valid even with duplicate delivery
- FIFO does not eliminate idempotency
- Outbox does not provide exactly-once delivery
- Outbox is not automatically an Event Store
- Domain Events and Integration Events may differ
- some consumers may use business-state idempotency rather than a processed-event table
- eventual consistency must be treated as a business characteristic, not merely a technical detail

---

# 294. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate processing | Critical | High | Idempotent consumers |
| Event loss | Critical | Medium | Transactional Outbox |
| Out-of-order event | High | Medium | FIFO/version/state handling |
| Poison message | High | Medium | Bounded retry + DLQ |
| Retry storm | High | Medium | Backoff + ownership |
| Contract break | High | Medium | Versioning + contract tests |
| Outbox backlog | High | Medium | Metrics + alerts |
| Consumer overload | High | Medium | Bounded concurrency |
| Sensitive-data leak | Critical | Low/Medium | Payload/log governance |
| Replay side effects | High | Medium | Idempotency + controlled tooling |

---

# 295. Implementation Guidance

The following rules are mandatory:

1. Commands and events must have distinct semantics.
2. Integration events must have explicit ownership and contracts.
3. Events should use stable identifiers.
4. Event contracts should support explicit evolution/versioning.
5. Integration events must not expose JPA entities directly.
6. Event payloads must not contain credentials or unnecessary sensitive information.
7. SQS Standard consumers must tolerate duplicates and ordering variation.
8. FIFO consumers must remain idempotent.
9. Message groups must be scoped to the smallest required ordering domain.
10. Business processing must not assume exactly-once broker delivery.
11. Consumers with non-idempotent effects must implement an idempotency strategy.
12. Database-backed deduplication should use uniqueness/transactional guarantees.
13. Business-state mutation and event publication requiring atomicity must use Transactional Outbox or an approved equivalent.
14. Outbox event IDs must remain stable across retries.
15. Outbox dispatchers must process bounded batches.
16. Dispatchers must support safe horizontal concurrency where required.
17. Permanent Outbox failures must become operationally visible.
18. Retry policies must be bounded.
19. Retry ownership across layers must prevent multiplicative retry storms.
20. SQS visibility timeout must reflect processing duration.
21. Critical production queues should have monitored DLQs.
22. DLQ redrive must be controlled and preceded by root-cause analysis.
23. Consumer concurrency must respect database/HTTP/downstream capacity.
24. Queue depth and oldest-message age must be monitored.
25. Full production payload logging should be avoided.
26. AWS SDK types must remain outside the domain layer.
27. Messaging clients must be reused and gracefully shut down.
28. Replay must be controlled, auditable and idempotency-aware.
29. Critical messaging paths must have automated duplicate/failure tests.
30. Messaging architecture rules should be automated through ArchUnit or equivalent where practical.

---

# 296. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- AWS SDK v2
- AWS SQS
- SQS Standard
- SQS FIFO where applicable
- PostgreSQL
- Spring Data JPA
- Flyway
- Transactional Outbox
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- LocalStack where approved
- ArchUnit
- JaCoCo
- SonarQube
- SAST
- OpenTelemetry-compatible trace propagation where applicable
- CloudWatch/platform queue metrics
- CI/CD quality gates
- contract tests
- operational runbooks

---

# 297. Success Criteria

The decision is successful when:

- no critical event can be lost through database/broker dual-write gaps
- duplicate delivery does not duplicate protected business effects
- event IDs remain stable across publication retries
- producers and consumers evolve contracts safely
- DLQ growth is detected operationally
- Outbox backlog is observable
- consumers recover safely after crashes
- queue scaling does not overwhelm downstream systems
- event payloads remain free of secrets and unnecessary PII
- messaging infrastructure does not leak into domain logic
- Order/Workflow integration can tolerate temporary broker/consumer failures
- replay/redrive can be performed safely under controlled procedures

---

# 298. Alternatives Rejected

## 298.1 Direct Database Update + SQS Send

Rejected because two independent systems cannot be atomically updated through ordinary application code.

---

## 298.2 SQS Send Before Database Commit

Rejected because an event could be published for a business transaction that later rolls back.

---

## 298.3 Exactly-Once Assumption

Rejected because broker-level delivery semantics do not guarantee exactly-once business side effects end-to-end.

---

## 298.4 Non-Idempotent Consumers

Rejected because duplicate delivery is a normal distributed-system condition.

---

## 298.5 FIFO Everywhere

Rejected because strict ordering is unnecessary for many events and can reduce throughput/scalability.

---

## 298.6 Standard Queue Everywhere

Rejected because some workflows may have legitimate per-aggregate ordering requirements.

---

## 298.7 Infinite Retry

Rejected because permanent failures and poison messages require a terminal operational path.

---

## 298.8 Shared Java Event Classes Across Services

Rejected because independent services require independently governed contracts rather than shared internal implementation coupling.

---

## 298.9 Event Sourcing by Default

Rejected because Transactional Outbox and integration events do not require event-sourced aggregates.

---

# 299. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-083: Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard
- ADR-084: Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard
- ADR-085: Enterprise Dependency Management, Gradle, SBOM, Supply Chain Security and Vulnerability Governance Standard
- ADR-086: Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard
- ADR-088: Enterprise Domain-Driven Design, Bounded Context, Aggregate, Domain Event and Business Rule Modeling Standard
- ADR-089: Enterprise Hexagonal Architecture, Clean Architecture, Ports & Adapters and Module Boundary Standard

---

# 300. References

- AWS — Amazon Simple Queue Service Documentation
- AWS — SQS Standard Queues
- AWS — SQS FIFO Queues
- AWS — Dead-Letter Queues
- AWS — Visibility Timeout
- AWS SDK for Java 2.x Documentation
- Chris Richardson — Microservices Patterns
- Enterprise Integration Patterns
- Eric Evans — Domain-Driven Design
- Vaughn Vernon — Implementing Domain-Driven Design
- Martin Fowler — Event-Driven Architecture references
- Sam Newman — Building Microservices
- Java 21 Documentation
- Spring Boot Documentation
- Testcontainers Documentation
- ArchUnit Documentation

---

# 301. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise event-driven architecture and messaging governance baseline |

---

# 302. Decision Summary

Reliable publication becomes:

```text
BUSINESS TRANSACTION
        |
        +--> UPDATE DOMAIN STATE
        |
        +--> INSERT OUTBOX EVENT
        |
        v
      COMMIT
        |
        v
OUTBOX DISPATCHER
        |
        v
       SQS
```

instead of:

```text
UPDATE DATABASE
      |
      v
SEND SQS
      |
      X
NETWORK FAILURE
      |
      v
EVENT LOST
```

Consumer processing becomes:

```text
MESSAGE
   |
   v
VALIDATE CONTRACT
   |
   v
CHECK IDEMPOTENCY
   |
   v
BEGIN TRANSACTION
   |
   +--> APPLY BUSINESS CHANGE
   |
   +--> RECORD EVENT PROCESSED
   |
   v
COMMIT
   |
   v
ACKNOWLEDGE
```

Duplicate delivery becomes:

```text
EVENT 123
   |
   +------> DELIVERY 1 --> PROCESS
   |
   +------> DELIVERY 2 --> DUPLICATE --> NO DUPLICATE EFFECT
```

Ordering becomes:

```text
DO WE REQUIRE ORDERING?
        |
        +--> NO
        |     |
        |     v
        |   STANDARD
        |
        +--> YES
              |
              v
        WHAT IS THE
        ORDERING SCOPE?
              |
              v
        MESSAGE GROUP
              |
              v
             FIFO
```

Failure handling becomes:

```text
PROCESS
   |
   X
FAILURE
   |
   v
CLASSIFY
   |
   +--> TRANSIENT
   |       |
   |       v
   |     RETRY
   |
   +--> PERMANENT
           |
           v
          DLQ
           |
           v
     INVESTIGATE
           |
           v
      FIX / REDRIVE
```

For Orders and Workflow:

```text
ORDER DOMAIN CHANGE
        |
        v
ORDER APPLICATION
        |
        +--> SAVE ORDER
        |
        +--> SAVE WORKFLOW OUTBOX EVENT
        |
        v
      COMMIT
        |
        v
OUTBOX DISPATCHER
        |
        v
       SQS
        |
        v
WORKFLOW CONSUMER
        |
        v
IDEMPOTENCY
        |
        v
WORKFLOW PROCESS
```

The complete messaging equation is:

```text
EXPLICIT EVENT SEMANTICS
        +
STABLE EVENT IDENTITY
        +
VERSIONED CONTRACT
        +
TRANSACTIONAL OUTBOX
        +
BOUNDED DISPATCH
        +
AT-LEAST-ONCE ASSUMPTION
        +
IDEMPOTENT CONSUMER
        +
CONTROLLED ORDERING
        +
BOUNDED RETRY
        +
DLQ
        +
CONTROLLED REDRIVE
        +
OBSERVABILITY
        +
SECURITY
        +
REPLAY GOVERNANCE
        =
RELIABLE ENTERPRISE EVENT-DRIVEN ARCHITECTURE
```

The governing principle is:

```text
Events are facts.

Commands are requests.

Do not confuse them.

An event says
something happened.

Give it
a stable identity.

Do not regenerate
that identity
because delivery failed.

The event
did not happen twice.

Delivery happened twice.

Assume duplicates.

Always.

Do not design
a consumer
that works only
when the network
behaves perfectly.

The network
will not.

Do not update
the database

and then hope
SQS succeeds.

Do not publish
to SQS

and then hope
the database commits.

Use one
local transaction.

Persist the state.

Persist the event.

Commit both.

Then dispatch.

That is Outbox.

But Outbox
does not create
exactly once.

A dispatcher
can publish successfully

and crash
before marking
the event sent.

So the event
can arrive again.

That is why
the consumer
must still
be idempotent.

Use database constraints.

Use event IDs.

Use business state.

Use whatever mechanism
correctly protects
the business effect.

Do not trust
a check-then-act
race condition.

Do not use FIFO
because ordering
sounds safer.

First ask:

Do we need ordering?

Then ask:

Ordering of what?

Usually
one Order.

Not the entire company.

Use the smallest
message group
that protects
the invariant.

Do not retry forever.

Some failures
are temporary.

Some messages
will never succeed.

Know the difference.

Use backoff.

Bound attempts.

Use a DLQ.

And monitor it.

A DLQ nobody watches
is merely
a slower data-loss mechanism.

Do not blindly
redrive failures.

Understand them.

Fix them.

Verify idempotency.

Then replay.

Monitor queue depth.

Monitor message age.

Monitor Outbox backlog.

Monitor failures.

An API can be green

while ten thousand
business events
are waiting
behind a broken dispatcher.

That system
is not healthy.

Do not log
every payload.

Do not put
credentials in events.

Do not publish
JPA entities.

Publish contracts.

Own them.

Version them.

Evolve them carefully.

Protect consumers
you may not even know exist.

Keep AWS
at the adapter boundary.

The Order domain
does not care
that the broker
is SQS.

The business fact is:

OrderApproved.

Infrastructure decides
how that fact
reaches Workflow.

Use messaging
when asynchronous
semantics fit
the business.

Use HTTP
when the caller
needs an answer now.

Do not build
event-driven architecture
for architectural fashion.

Build it
where decoupling,
resilience
and eventual consistency
solve a real problem.

And remember:

Reliable messaging
is not achieved
by successfully calling
SendMessage.

It is achieved when

the business state,

the event,

the broker,

the consumer,

the retry model,

the idempotency model,

and the operational recovery model

all agree
on what happens

when something fails.
```
