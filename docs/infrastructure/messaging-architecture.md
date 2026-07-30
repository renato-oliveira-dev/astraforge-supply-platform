# Messaging Architecture

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Messaging Architecture |
| Status | Active |
| Version | 1.0.0 |
| Canonical Decision | ADR-090 |

---

# 1. Purpose

This document defines the asynchronous messaging architecture of the AstraForge Supply Platform under the current architectural baseline.

The platform uses **Amazon SQS** for queue-based asynchronous integration and the **Transactional Outbox Pattern** when a database state change and an integration event must be made reliable as one local transaction.

The Domain and Application layers remain independent of the AWS SDK and SQS-specific types.

---

# 2. Core Principles

The messaging architecture follows these rules:

- integration events are explicit contracts;
- commands and events have different semantics;
- producers never rely on unsafe database-plus-broker dual writes;
- consumers assume at-least-once delivery;
- consumers are idempotent;
- retries are bounded;
- poison messages reach a monitored dead-letter queue;
- ordering is requested only where the business requires it;
- queue depth and oldest-message age are production health signals;
- sensitive data is minimized in messages and logs.

---

# 3. High-Level Architecture

```text
Application / Use Case
        |
        v
Domain State Change
        |
        +--> Integration Event
        |
        v
Transactional Outbox
        |
        v
Outbox Dispatcher
        |
        v
Amazon SQS
        |
        +--> Consumer A
        +--> Consumer B / dedicated queue
        +--> Workflow Consumer
```

SQS is queue-oriented. When multiple independent consumers need the same fact, fan-out SHOULD use an approved pattern such as SNS-to-SQS or separate publication/routing architecture rather than assuming one SQS queue behaves like a Kafka topic with consumer groups.

---

# 4. SQS Queue Selection

Use **SQS Standard** when:

- strict ordering is not required;
- high throughput is desired;
- duplicate delivery is safe because consumers are idempotent.

Use **SQS FIFO** when:

- ordering is a business requirement;
- ordering can be scoped to a `MessageGroupId`;
- FIFO throughput characteristics are acceptable.

FIFO does not eliminate the requirement for idempotent consumers.

---

# 5. Queue Organization

Queues represent consumer-owned work streams, not shared global event logs.

Recommended logical naming:

```text
<environment>-<domain>-<purpose>
```

Examples:

```text
prod-orders-workflow-events
prod-notifications-order-events
prod-orders-workflow-events-dlq
```

Exact cloud naming MUST follow the enterprise infrastructure naming policy.

---

# 6. Event Envelope

Integration events SHOULD use a stable envelope containing fields such as:

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
  "payload": {}
}
```

`eventId` remains stable across publication retries.

---

# 7. Publication Lifecycle

```text
BEGIN LOCAL TRANSACTION
        |
        +--> update business state
        +--> insert outbox event
        |
        v
COMMIT
        |
        v
bounded outbox dispatcher
        |
        v
SQS SendMessage
        |
        v
mark outbox event SENT
```

A crash after SQS accepted the message but before the Outbox row is marked `SENT` can cause duplicate publication. Consumers therefore remain idempotent.

---

# 8. Consumer Lifecycle

```text
Receive Message
      |
      v
Validate Envelope / Version
      |
      v
Idempotency Check
      |
      v
Local Transaction
      |
      +--> business change
      +--> processed-event marker when used
      |
      v
Commit
      |
      v
Acknowledge/Delete Message
```

A message MUST NOT be acknowledged as successful before required durable local changes succeed.

---

# 9. Ordering

Ordering is explicit rather than assumed.

SQS Standard consumers tolerate out-of-order messages.

When FIFO is required, `MessageGroupId` SHOULD represent the smallest business scope requiring serialized processing, typically an aggregate identifier such as `orderId`.

A single global message group is prohibited unless global serialization is truly required.

---

# 10. Retry and Visibility Timeout

Retryable failures include transient network, service availability, throttling, and infrastructure failures where re-execution is safe.

Non-retryable failures include malformed contracts, unsupported versions, impossible business states, and other permanent errors.

Visibility timeout MUST exceed normal processing duration with an appropriate margin. Retry counts MUST be bounded and coordinated with application/HTTP retry policies to avoid amplification.

---

# 11. Dead-Letter Queues

Production queues SHOULD have a DLQ when permanent processing failure is possible.

A DLQ is an operational recovery mechanism, not message disposal.

Before redrive:

```text
identify root cause
    -> fix root cause
    -> verify idempotency
    -> select controlled scope
    -> redrive
```

DLQ depth MUST be monitored and alerted.

---

# 12. Message Size and Payload Design

Messages MUST remain within SQS service limits and SHOULD be substantially smaller than the maximum whenever possible.

Large binary payloads SHOULD use a claim-check pattern such as:

```text
S3 object
   ^
   |
SQS message contains object reference + integrity metadata
```

Do not publish complete persistence entities merely because they are available.

---

# 13. Security

Messaging follows least privilege:

- producers receive only required send permissions;
- consumers receive only required receive/delete/change-visibility permissions;
- DLQ redrive privileges are restricted;
- workload IAM roles are preferred over static AWS credentials;
- encryption in transit and approved encryption at rest are required;
- passwords, access tokens, refresh tokens, API keys, and credentials are forbidden in event payloads.

---

# 14. Observability

Critical metrics include:

```text
outbox_pending
outbox_failed
outbox_dispatch_latency
messages_received
messages_processed
messages_failed
duplicates_detected
queue_depth
oldest_message_age
dlq_depth
processing_duration
```

Event IDs and aggregate UUIDs MUST NOT be metric labels.

Logs SHOULD include safe event metadata such as event type, event ID, correlation ID, aggregate ID, result, and elapsed time, while avoiding full payload logging by default.

---

# 15. Replay and Recovery

Replay/redrive operations MUST be:

- authorized;
- auditable;
- scope-controlled;
- idempotency-aware.

Reprocessing that intentionally creates a new business effect MUST be represented as a new business operation rather than bypassing duplicate protection casually.

---

# 16. Testing

Critical messaging tests cover:

- event serialization and versioning;
- Outbox atomicity;
- stable event ID across retry;
- duplicate delivery;
- idempotent consumer behavior;
- retryable versus permanent failures;
- DLQ behavior;
- FIFO ordering/message-group behavior where applicable;
- shutdown/redelivery behavior;
- contract compatibility.

AWS connectivity is not required for domain tests. LocalStack or another approved SQS-compatible integration environment MAY be used for infrastructure tests.

---

# 17. Architecture Rules

```text
Domain -> no AWS SDK
Domain -> no SQS client
Application -> outbound messaging port / Outbox abstraction
Infrastructure -> SQS implementation
Consumer -> use case, not duplicated business rules
```

---

# 18. Canonical Decisions

This document is governed primarily by:

- **ADR-008** — Assume At-Least-Once Message Delivery
- **ADR-018** — Version Integration Event Contracts
- **ADR-090** — Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard

Earlier Kafka-specific ADRs remain historical and are superseded by ADR-090 under the current platform baseline.
