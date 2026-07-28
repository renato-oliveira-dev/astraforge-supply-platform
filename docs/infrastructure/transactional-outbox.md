# Transactional Outbox

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Transactional Outbox |
| Status | Active |
| Version | 1.0.0 |
| Canonical Decision | ADR-090 |

---

# 1. Purpose

The Transactional Outbox guarantees that a local business-state change and the intent to publish its integration event are committed atomically.

The current external queue adapter is Amazon SQS, but the Outbox model itself remains broker-independent.

---

# 2. Dual-Write Problem

Unsafe flow:

```text
update PostgreSQL
      |
      v
commit
      |
      v
send SQS message
      |
      X
publication fails
```

The database contains the business change but the integration event is missing.

The inverse ordering is also unsafe because a message may escape for a transaction that later rolls back.

---

# 3. Atomic Local Transaction

Preferred flow:

```text
BEGIN TRANSACTION
      |
      +--> update aggregate/state
      |
      +--> insert outbox_event
      |
      v
COMMIT
```

Only after commit does the dispatcher publish externally.

---

# 4. Outbox Record

A representative Outbox record contains:

```text
id
event_id
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

The exact schema is service-owned and evolved only through new Flyway migrations. Applied migrations are immutable.

---

# 5. Event Identity

`eventId` represents the integration event and MUST remain stable across dispatcher retries.

A new SQS attempt is not a new business event.

---

# 6. Dispatcher

The dispatcher:

- selects only eligible rows;
- processes bounded batches;
- safely supports horizontal concurrency when needed;
- publishes through the SQS infrastructure adapter;
- records success/failure state;
- applies bounded retry/backoff;
- exposes backlog and failure metrics.

It MUST NOT contain domain business rules.

---

# 7. Concurrency

Multiple dispatcher instances MAY run concurrently.

Database claiming SHOULD prevent workers from intentionally selecting the same row at the same time, for example using an appropriate locking/claim strategy such as `FOR UPDATE SKIP LOCKED` when compatible with the implementation.

Duplicate publication is still possible if SQS accepts a message and the process crashes before the Outbox row is marked sent. This is why consumers remain idempotent.

---

# 8. Retry

Retry is only for transient publication failure.

Retry policy MUST define:

- maximum attempts;
- backoff;
- jitter where useful;
- next eligible attempt;
- terminal failure handling.

A permanent failed Outbox event MUST become operationally visible.

---

# 9. Ordering

The Outbox does not imply global ordering.

When business ordering matters, it SHOULD be scoped to an aggregate/business identifier and mapped to the SQS FIFO `MessageGroupId` where FIFO is chosen.

Standard-queue consumers MUST tolerate out-of-order delivery.

---

# 10. Payload

Persist the immutable integration-event representation required for publication.

Do not serialize JPA entities directly.

Do not store credentials, access tokens, refresh tokens, or unnecessary PII in the payload.

---

# 11. Cleanup and Retention

Sent rows MUST have an explicit retention policy balancing:

```text
forensics
audit/replay needs
storage
privacy
```

Cleanup MUST operate in bounded batches and avoid unnecessary contention with active dispatch.

---

# 12. Observability

Monitor:

```text
pending outbox count
oldest pending outbox age
dispatch rate
dispatch latency
retry count
permanent failures
```

An API can remain healthy while its Outbox backlog grows; therefore Outbox health is part of production health.

---

# 13. Testing

Critical tests verify:

- state update + Outbox insert commit together;
- rollback removes both effects;
- event ID is stable across retries;
- bounded batch processing;
- concurrent dispatcher claiming;
- retry/backoff and terminal failure;
- duplicate publication is harmless because consumers are idempotent;
- cleanup and retention behavior.

Use PostgreSQL Testcontainers for PostgreSQL-specific locking and transaction behavior.

---

# 14. Architecture Rules

```text
Domain -> no SQS/AWS SDK
Application transaction -> business state + Outbox
Dispatcher -> infrastructure only
Consumer -> idempotent
```

The canonical detailed decision is ADR-090.
