# Messaging Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Messaging Guidelines |
| Status | Active |
| Version | 1.0.0 |
| Canonical Decision | ADR-090 |

---

# 1. Purpose

These guidelines define the implementation standard for asynchronous messaging in the AstraForge Supply Platform.

The current baseline is **Amazon SQS**. Kafka-specific guidance is historical and MUST NOT be used for new platform implementation unless a new accepted ADR explicitly reintroduces Kafka for a defined use case.

---

# 2. Core Principles

1. Choose asynchronous communication only when the business semantics permit eventual processing.
2. Commands request actions; events describe facts that already happened.
3. Integration events are versioned contracts, not serialized persistence entities.
4. Assume at-least-once delivery.
5. Every consumer with side effects must be idempotent.
6. Use Transactional Outbox when database state and event publication must not diverge.
7. Bound retries and concurrency.
8. Use monitored DLQs for poison/permanent failures.
9. Make ordering explicit rather than assumed.
10. Keep AWS SDK types outside Domain and core application rules.

---

# 3. Synchronous vs Asynchronous

Use synchronous HTTP when the caller requires the current result to continue safely.

Use SQS when:

- work can complete later;
- eventual consistency is acceptable;
- runtime producer/consumer decoupling is valuable;
- retryable processing is required.

Do not turn a request/response requirement into queue polling merely to call the architecture event-driven.

---

# 4. Commands and Events

Commands use imperative names:

```text
ApproveOrder
CancelOrder
GenerateInvoice
```

Events use past-tense/fact names:

```text
OrderCreated
OrderApproved
OrderCancelled
```

A command normally has one logical handler. An event can feed multiple independent downstream queues through an approved fan-out design.

---

# 5. Event Envelope

Recommended envelope:

```json
{
  "eventId": "uuid",
  "eventType": "ORDER_APPROVED",
  "eventVersion": 1,
  "occurredAt": "2026-07-26T12:30:00Z",
  "producer": "ecommerce-order-service",
  "aggregateType": "ORDER",
  "aggregateId": "uuid",
  "correlationId": "uuid",
  "traceId": "uuid",
  "payload": {}
}
```

Rules:

- `eventId` is globally unique and stable across retry;
- `eventType` describes semantics, not a Java class name;
- `eventVersion` changes only for genuine contract/version strategy needs;
- `occurredAt` represents business occurrence time;
- trace/correlation metadata follows observability/privacy policy.

---

# 6. Event Contract Evolution

Prefer additive compatible changes.

Safe changes commonly include adding optional fields that old consumers can ignore.

Breaking changes include:

- removing required fields;
- changing field type;
- changing field meaning;
- changing an enum contract so old consumers cannot deserialize it;
- reusing an existing event type for a different fact.

Semantic compatibility matters as much as JSON compatibility.

---

# 7. Serialization

JSON is the default representation unless another accepted decision applies.

Serialization MUST be deterministic enough for contract testing and debugging.

Never publish:

- a JPA entity graph;
- framework exceptions;
- credentials/tokens;
- unnecessary PII.

---

# 8. Queue Naming

Logical queue naming SHOULD follow:

```text
<environment>-<domain>-<purpose>
```

Examples:

```text
prod-orders-workflow-events
prod-notifications-order-events
```

DLQ naming:

```text
<source-queue>-dlq
```

Exact AWS naming conventions are governed by platform infrastructure policy.

---

# 9. Standard vs FIFO

Use **Standard** by default when strict ordering is unnecessary.

Use **FIFO** only when ordering is a business requirement.

For FIFO:

- `MessageGroupId` is the smallest ordering boundary, commonly aggregate ID;
- `MessageDeduplicationId` MAY use `eventId`;
- avoid one global group;
- consumer idempotency remains mandatory.

---

# 10. Ordering

SQS Standard does not provide strict application ordering.

Consumers that care about freshness/order SHOULD use business state, sequence/version, or FIFO as required by the contract.

Do not assume one service's publication order is the same as another consumer's observation order after retries/replay.

---

# 11. Idempotency

Duplicate delivery is normal.

Strategies include:

```text
processed_event table + UNIQUE(event_id)
business unique constraint
conditional update
upsert
state-transition guard
idempotency key
```

A simple `exists()` followed by `insert()` without transactional/unique protection is vulnerable to a race condition.

Idempotency MUST protect external side effects such as email, payment, file creation, and downstream commands when duplicates would be harmful.

---

# 12. Transactional Outbox

When a local database change and event publication represent one committed business operation:

```text
BEGIN
  update business state
  insert outbox_event
COMMIT
```

The dispatcher publishes after commit.

`eventId` remains unchanged across Outbox retries.

The Outbox dispatcher processes bounded batches and exposes pending/failed metrics.

---

# 13. Retry Classification

Retryable examples:

- temporary network failure;
- throttling;
- temporary dependency unavailability;
- transient database/connectivity failure when safe.

Non-retryable examples:

- malformed payload;
- unsupported contract version;
- permanent business rejection;
- impossible state transition;
- invalid required data.

Retries MUST be bounded and use controlled backoff; jitter SHOULD be used where synchronized retry storms are possible.

---

# 14. Visibility Timeout

Visibility timeout MUST exceed expected normal processing duration with margin.

Too short produces concurrent duplicate processing.

Too long delays recovery after a dead consumer.

Long-running consumers MAY extend visibility when explicitly designed, but very long processing SHOULD trigger architecture review.

---

# 15. Dead-Letter Queue

Production queues SHOULD have a DLQ when permanent failure is possible.

DLQ requirements:

- monitored depth;
- controlled access;
- failure context without secrets;
- operational runbook;
- controlled redrive.

Never blindly mass-redrive a DLQ.

---

# 16. Consumer Design

Consumers SHOULD:

```text
receive
validate envelope/version
check idempotency
execute application use case
commit durable state
acknowledge/delete message
```

Consumers MUST NOT acknowledge/delete a message before the required local state is durable.

Listener classes should remain thin adapters and not become business-service implementations.

---

# 17. Consumer Concurrency

Concurrency MUST be bounded by downstream capacity.

Virtual Threads do not make these unlimited:

```text
database connections
HTTP connections
remote API quotas
memory
CPU
```

Queue-driven autoscaling MUST avoid shifting overload into PostgreSQL or external services.

---

# 18. Batch Consumption

Batch receive MAY improve efficiency.

Partial failures MUST NOT force already-successful messages to create uncontrolled duplicate effects. Use supported partial-batch failure semantics where applicable and preserve idempotency.

---

# 19. Message Size

Keep messages small.

For large binary or document payloads, use a claim-check pattern and publish a secure object reference plus integrity/context metadata.

Do not use SQS as blob storage.

---

# 20. Fan-Out

SQS queues are consumer work queues.

When several independent systems need the same event, use separate queues populated through an approved fan-out mechanism such as SNS-to-SQS or an explicit publisher design.

Do not copy Kafka topic/consumer-group assumptions into SQS architecture.

---

# 21. Security

Use least-privilege IAM.

Producers receive only required send rights.

Consumers receive only required receive/delete/change-visibility rights.

Prefer workload IAM identity over static access keys.

Messages and DLQs containing business data follow privacy, retention, and encryption policy.

---

# 22. Observability

Monitor at minimum:

```text
queue depth
oldest message age
in-flight messages
processing duration
success/failure rate
duplicates detected
DLQ depth
outbox pending count
outbox oldest age
outbox dispatch latency
```

Do not use event IDs, order IDs, customer IDs, or correlation IDs as metric labels.

---

# 23. Logging

Log safe metadata, for example:

```text
eventType
eventId
aggregateId
correlationId
result
elapsedMs
```

Do not log full payloads by default.

Never log credentials, Authorization headers, access tokens, refresh tokens, or unnecessary PII.

---

# 24. Replay and Redrive

Replay/redrive is a privileged production operation.

Required sequence:

```text
understand failure
fix cause
verify consumer idempotency
choose bounded scope
execute
monitor result
audit operation
```

If reprocessing is intended to create a new business effect, model it as a new business operation instead of disabling idempotency.

---

# 25. Testing

Unit tests cover:

- command/event mapping;
- version validation;
- retry classification;
- idempotency rules;
- error translation.

Integration tests cover:

- Outbox atomicity with PostgreSQL;
- SQS-compatible send/receive behavior;
- duplicate delivery;
- FIFO group ordering where applicable;
- visibility/redelivery;
- DLQ behavior;
- contract serialization/deserialization.

LocalStack or another approved SQS-compatible environment MAY be used where actual broker behavior matters.

Tests MUST follow the project's AssertJ convention and use meaningful `.as("...")` descriptions before applicable assertions.

---

# 26. Operational Runbook

Messaging runbooks MUST address:

```text
outbox backlog
SQS service failure
queue backlog
oldest message age
consumer outage
poison messages
DLQ inspection
redrive
replay
downstream saturation
```

---

# 27. Prohibited Patterns

Do not:

- update PostgreSQL and call SQS as unrelated dual writes when event reliability matters;
- generate a new event ID for each retry;
- assume exactly-once delivery;
- use one global FIFO message group without a business requirement;
- retry every exception indefinitely;
- swallow consumer exceptions and delete the message;
- serialize JPA entities as contracts;
- put tokens/secrets in payloads;
- log every payload;
- make Domain code depend on AWS SDK;
- treat DLQ as disposal;
- copy Kafka partition/consumer-group semantics into SQS designs.

---

# 28. Canonical References

- ADR-008 — Assume At-Least-Once Message Delivery
- ADR-018 — Version Integration Event Contracts
- ADR-090 — Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- `docs/infrastructure/messaging-architecture.md`
- `docs/infrastructure/sqs-architecture.md`
- `docs/infrastructure/transactional-outbox.md`
