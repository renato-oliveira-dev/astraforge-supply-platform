# Transactional Outbox

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Transactional Outbox |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines how the Enterprise Order Platform guarantees reliable publication of Integration Events.

The platform adopts the **Transactional Outbox Pattern** to ensure that:

- database updates
- event persistence

are committed atomically.

No event is published directly from the application transaction.

---

# 2. Problem Statement

Publishing directly to Kafka after committing the database introduces inconsistency.

Example

```
Save Order

↓

Commit Database

↓

Publish Kafka

↓

Kafka unavailable
```

Result

```
Order exists

Event lost
```

The opposite is equally problematic.

```
Publish Kafka

↓

Success

↓

Database rollback
```

Consumers receive an event for data that does not exist.

---

# 3. Solution Overview

Instead of publishing immediately:

```
Application Service

↓

Aggregate

↓

Repository

↓

Outbox Record

↓

Commit
```

Publication occurs later.

```
Outbox Dispatcher

↓

Kafka

↓

Mark as Published
```

---

# 4. Flow

```
HTTP Request

↓

Application Service

↓

Aggregate

↓

Domain Events

↓

Integration Events

↓

Outbox Table

↓

COMMIT

↓

Dispatcher

↓

Kafka

↓

Success

↓

Update Status
```

---

# 5. Outbox Responsibilities

The Outbox is responsible for:

- durable storage
- retry support
- ordering
- publication state
- recovery after failures
- operational visibility

---

# 6. Outbox Table

Recommended schema

```
outbox_event
```

Columns

| Column | Description |
|---------|-------------|
| id | Event identifier |
| aggregate_id | Aggregate Root identifier |
| aggregate_type | Aggregate type |
| event_type | Integration Event name |
| destination | Topic or queue |
| payload | Serialized event |
| status | Publication status |
| attempts | Retry count |
| next_attempt_at | Retry scheduling |
| created_at | Event creation |
| published_at | Successful publication |
| correlation_id | Business correlation |
| causation_id | Originating event |
| trace_id | Distributed tracing |

---

# 7. Event Lifecycle

```
NEW

↓

READY

↓

PUBLISHING

↓

PUBLISHED
```

Failure path

```
READY

↓

FAILED

↓

WAITING_RETRY

↓

READY
```

Maximum retries

```
FAILED_PERMANENTLY
```

---

# 8. Transaction Boundary

The same database transaction persists:

- aggregate changes
- outbox rows

Example

```
@Transactional

Order.submit()

↓

Repository.save()

↓

Outbox.save()

↓

Commit
```

Atomicity is guaranteed.

---

# 9. Dispatcher

The Dispatcher continuously scans for pending events.

Responsibilities

- poll events
- lock rows
- publish messages
- update status
- retry failures
- send permanent failures to DLQ

---

# 10. Dispatcher Architecture

```
Scheduler

↓

Dispatcher

↓

Batch Loader

↓

Publisher

↓

Status Updater
```

Each responsibility should remain isolated.

---

# 11. Polling Strategy

Typical interval

```
500 ms

or

1 second
```

Batch size

```
100

250

500
```

Configurable.

---

# 12. Ordering

Ordering is guaranteed per Aggregate.

Example

```
OrderCreated

↓

OrderSubmitted

↓

OrderApproved

↓

OrderCompleted
```

Dispatcher must preserve this sequence.

---

# 13. Concurrency

Multiple dispatcher instances may run simultaneously.

Use row-level locking.

Examples

```
SELECT ...

FOR UPDATE SKIP LOCKED
```

or equivalent database mechanisms.

---

# 14. Retry Strategy

Retry should use exponential backoff.

Example

```
Attempt 1

5 seconds

Attempt 2

15 seconds

Attempt 3

45 seconds

Attempt 4

2 minutes

Attempt 5

10 minutes
```

Configurable.

---

# 15. Permanent Failure

After maximum retries:

```
FAILED_PERMANENTLY
```

Event remains stored for investigation.

Optionally:

```
Dead Letter Queue
```

---

# 16. Idempotency

The Dispatcher may publish the same event more than once.

Consumers must therefore be idempotent.

Use:

- EventId
- AggregateVersion
- CorrelationId

---

# 17. Event Payload

Payload should contain:

```
Metadata

+

Business Data
```

Never include internal framework objects.

---

# 18. Serialization

Preferred

```
JSON
```

Alternative

- Avro
- Protobuf

Payloads must be versioned.

---

# 19. Observability

Collect metrics for:

- pending events
- published events
- failed events
- retry count
- dispatcher latency
- publication throughput

---

# 20. Logging

Log:

- dispatcher startup
- publication success
- retries
- permanent failures

Avoid logging entire payloads containing sensitive data.

---

# 21. Monitoring

Recommended dashboards

- Outbox queue depth
- Retry rate
- Publication latency
- DLQ count
- Dispatcher health

---

# 22. Recovery

After application restart:

```
Dispatcher

↓

Read pending rows

↓

Resume publication
```

No event is lost.

---

# 23. Performance

Recommendations

- index status
- index next_attempt_at
- index aggregate_id
- partition large tables
- archive published events

---

# 24. Cleanup

Published events should not remain indefinitely.

Strategies

- scheduled archival
- retention policy
- partition pruning

Typical retention

```
30–90 days
```

depending on auditing requirements.

---

# 25. Testing

Verify:

- atomic persistence
- retry behavior
- duplicate publication
- ordering
- dispatcher restart
- concurrent dispatchers
- DLQ routing

---

# 26. Architecture Rules

The Outbox:

- is the only publication source
- never publishes inside the business transaction
- guarantees durability
- supports retries
- isolates messaging failures

---

# 27. Decision Summary

The platform adopts:

- Transactional Outbox Pattern
- asynchronous publication
- configurable retries
- exponential backoff
- ordered publication per Aggregate
- durable event storage
- idempotent consumers
- operational monitoring

---

# 28. Next Documentation Step

Next document

```
docs/infrastructure/messaging-architecture.md
```

It will define:

- Kafka topology
- Topics
- Partitions
- Consumers
- Producer strategy
- Dead Letter Queue
- Retry Topics
- Message contracts
- Schema evolution
