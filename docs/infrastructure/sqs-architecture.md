# Amazon SQS Architecture

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Amazon SQS Architecture |
| Status | Active |
| Version | 1.0.0 |
| Canonical Decision | ADR-090 |

---

# 1. Purpose

This document defines the concrete Amazon SQS infrastructure architecture used by the Enterprise Order Platform.

SQS is an infrastructure adapter. Domain and application code MUST remain independent of AWS SDK types.

---

# 2. Queue Types

Use **Standard queues** by default when strict ordering is unnecessary.

Use **FIFO queues** only when business processing requires ordered handling within a defined `MessageGroupId` scope.

Both queue types require idempotent consumers.

---

# 3. Reference Architecture

```text
Business Transaction
      |
      +--> PostgreSQL state
      +--> outbox_event
      |
      v
Commit
      |
      v
Outbox Dispatcher
      |
      v
SQS Queue
      |
      v
Consumer Adapter
      |
      v
Application Use Case
      |
      v
Local Transaction
```

---

# 4. Queue Naming

Logical pattern:

```text
<environment>-<domain>-<purpose>
```

Dead-letter queue:

```text
<source-queue>-dlq
```

Examples:

```text
prod-orders-workflow-events
prod-orders-workflow-events-dlq
```

---

# 5. Message Identity

`eventId` identifies the business/integration event and MUST remain stable across retries.

For FIFO queues, `MessageDeduplicationId` MAY use `eventId`.

`MessageGroupId` SHOULD normally use the aggregate/business identifier whose events require ordering.

---

# 6. Visibility Timeout

Visibility timeout MUST exceed expected normal processing duration with operational margin.

It MUST NOT be used as a substitute for fixing unexpectedly long-running consumers.

Long-running processing MAY extend visibility when justified and bounded.

---

# 7. Retry and DLQ

SQS redrive policy MUST define a bounded receive count before a poison/permanent message reaches its DLQ.

Application retries, HTTP retries, Outbox retries, and SQS redelivery MUST be reviewed together to avoid multiplicative retry storms.

DLQ growth is an alertable production condition.

---

# 8. Consumer Concurrency

Concurrency MUST respect downstream constraints:

```text
database pool
HTTP connection pools
external API quotas
memory
CPU
```

Java 21 Virtual Threads MAY be used for I/O-heavy processing but do not remove these capacity limits.

---

# 9. Fan-Out

One SQS message is processed by one consumer from a queue.

When multiple independent systems require the same event, use an approved fan-out design, for example separate queues populated through an approved publisher or SNS-to-SQS pattern. Do not model independent consumers as if SQS were a Kafka consumer-group log.

---

# 10. IAM and Security

Use least privilege.

Producer permissions SHOULD be limited to send operations for the required queue.

Consumer permissions SHOULD be limited to required receive/delete/visibility operations.

Static AWS access keys in source code or application configuration are prohibited; workload IAM identity is preferred.

---

# 11. Observability

Monitor at minimum:

- approximate visible-message count;
- age of oldest message;
- in-flight messages;
- receive/delete failures;
- DLQ depth;
- consumer processing duration;
- Outbox backlog and dispatch latency.

---

# 12. Testing

Infrastructure tests SHOULD verify:

- Standard queue send/receive;
- FIFO group ordering where used;
- message attributes and envelope serialization;
- redelivery/idempotency;
- DLQ/redrive behavior;
- visibility-timeout behavior;
- graceful consumer shutdown.

Use LocalStack or another approved compatible test environment when real SQS semantics are necessary without calling production AWS services.

---

# 13. Related Documents

- `docs/infrastructure/messaging-architecture.md`
- `docs/infrastructure/transactional-outbox.md`
- `docs/infrastructure/idempotency.md`
- `docs/standards/messaging-guidelines.md`
- `docs/architecture/decisions/ADR-090-adopt-enterprise-event-driven-architecture-sqs-transactional-outbox-idempotency-event-contract-and-messaging-governance-standard.md`
