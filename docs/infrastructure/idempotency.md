# Idempotency

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Idempotency |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the idempotency strategy adopted by the Enterprise Order Platform.

It establishes:

- duplicate detection
- consumer behavior
- replay safety
- retry compatibility
- storage strategy
- API idempotency
- message idempotency
- implementation guidelines

The platform assumes **at-least-once delivery**.

Every component must therefore tolerate duplicate execution.

---

# 2. Why Idempotency?

Distributed systems cannot guarantee that a message is delivered exactly once.

Typical situations include:

- broker retries
- network failures
- consumer restart
- dispatcher restart
- timeout retries
- manual replay
- disaster recovery

The business result must remain identical regardless of duplicate deliveries.

---

# 3. Delivery Semantics

The platform adopts:

```
At-Least-Once Delivery
```

Meaning:

```
One message

↓

One or more deliveries
```

Consumers are responsible for ensuring idempotent processing.

---

# 4. Exactly-Once Processing

Exactly-once delivery is not assumed.

Instead:

```
At-Least-Once

+

Idempotent Consumer

=

Exactly-Once Business Result
```

---

# 5. Idempotency Scope

Idempotency applies to:

- Integration Events
- REST Commands
- Scheduled Jobs
- Batch Imports
- External Webhooks

Read operations do not require idempotency.

---

# 6. Event Identity

Every Integration Event contains:

```
EventId

AggregateId

AggregateVersion

OccurredAt

CorrelationId

Payload
```

The primary idempotency key is:

```
EventId
```

---

# 7. Consumer Processing Flow

```
Receive Event

↓

Extract EventId

↓

Already Processed?

↓

YES

↓

ACK

↓

STOP
```

Otherwise:

```
Process

↓

Persist Result

↓

Mark Event Processed

↓

ACK
```

---

# 8. Processed Event Store

Consumers maintain a persistent store.

Example table:

```
processed_event
```

Columns

| Column | Description |
|---------|-------------|
| event_id | Unique event identifier |
| consumer_name | Logical consumer / queue processing identity |
| processed_at | Processing timestamp |
| aggregate_id | Aggregate identifier |
| aggregate_version | Version |
| correlation_id | Business correlation |

---

# 9. Uniqueness Constraint

The following combination must be unique:

```
(event_id, consumer_name)
```

This allows different logical consumers or consumer-owned queues to process the same event independently.

---

# 10. Consumer Algorithm

```
Receive Event

↓

INSERT processed_event

↓

Unique Constraint?

↓

No

↓

Continue

↓

Business Processing

↓

Commit
```

If insertion fails:

```
Duplicate

↓

Ignore

↓

ACK
```

---

# 11. Transaction Boundary

The following operations occur in the same transaction:

- insert processed_event
- update business state
- commit

This guarantees atomic processing.

---

# 12. Aggregate Version

Consumers may also validate:

```
AggregateVersion
```

If an older version arrives:

```
Ignore
```

This protects against out-of-order delivery.

---

# 13. Replay

Replay is always supported.

Example:

```
Controlled Replay / Redrive Source

↓

Consumer

↓

Already Processed?

↓

Ignore
```

Replay never changes business state.

---

# 14. Retry Compatibility

Retry does not require special business logic.

Every retry follows the normal flow:

```
Retry

↓

Duplicate Detection

↓

Already Processed?

↓

ACK
```

---

# 15. REST Idempotency

Commands exposed through REST should support an optional header:

```
Idempotency-Key
```

Example

```
POST /orders

Idempotency-Key:
3b52ab4d...
```

The server stores:

- key
- request hash
- response
- expiration

Repeated requests with the same key return the original response.

---

# 16. REST Idempotency Store

Suggested table:

```
idempotency_request
```

Columns:

- key
- request_hash
- response_body
- response_status
- created_at
- expires_at

---

# 17. Scheduled Jobs

Jobs should be restartable.

Each processed record stores:

- execution identifier
- business identifier
- completion status

Restart resumes from the last committed position.

---

# 18. Batch Processing

Imports should identify records by business key.

Example:

```
Invoice Number

Customer

Business Date
```

Duplicate records are ignored.

---

# 19. Webhooks

Incoming webhooks should include:

```
WebhookId
```

If unavailable:

```
ProviderId

+

Timestamp

+

Payload Hash
```

---

# 20. External APIs

When calling external APIs that support idempotency, always propagate an idempotency key.

Examples:

- payment authorization
- shipment creation
- invoice generation

---

# 21. Expiration

Processed events should not remain forever.

Suggested retention:

```
90 days
```

Older records may be archived or removed according to compliance requirements.

---

# 22. Performance

Recommendations:

- index EventId
- index ConsumerName
- batch cleanup
- partition large tables

Duplicate detection must remain O(log n) or better.

---

# 23. Observability

Expose metrics:

- duplicate events
- processed events
- replay count
- retry count
- ignored events
- idempotency violations

---

# 24. Logging

Log:

- duplicate detection
- replay execution
- expired keys
- unexpected conflicts

Do not log sensitive payloads.

---

# 25. Testing

Tests must verify:

- duplicate delivery
- concurrent delivery
- replay
- retry
- transaction rollback
- unique constraint behavior
- REST idempotency
- webhook duplication

---

# 26. Architecture Rules

Idempotency:

- is mandatory for every consumer
- is transparent to business logic
- uses persistent storage
- supports replay
- supports retries
- never depends on broker guarantees

---

# 27. Decision Summary

The platform adopts:

- at-least-once delivery
- persistent duplicate detection
- EventId as primary key
- REST idempotency keys
- replay-safe consumers
- transactionally consistent processing
- configurable retention
- operational monitoring

---

# 28. Next Documentation Step

Next document

```
docs/infrastructure/saga-pattern.md
```

It will define:

- choreography vs orchestration
- distributed transactions
- compensation actions
- long-running business processes
- process managers
- timeout handling
- failure recovery
