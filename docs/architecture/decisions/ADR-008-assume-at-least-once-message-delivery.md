# ADR-008: Assume At-Least-Once Message Delivery

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-008 |
| Title | Assume At-Least-Once Message Delivery |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Event-Driven Architecture |
| Related Work Items | Reliable asynchronous communication |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform communicates with external bounded contexts through asynchronous integration events.

Examples include:

- OrderCreated
- OrderApproved
- OrderRejected
- OrderCancelled
- InventoryReserved
- PaymentAuthorized
- CustomerValidated

These events are published using the Transactional Outbox pattern.

Because publication occurs asynchronously, failures may happen at any stage of message delivery.

Examples include:

- producer crash
- broker timeout
- consumer restart
- network interruption
- dispatcher crash
- acknowledgement loss
- infrastructure failover

Distributed systems cannot guarantee that a message is processed exactly one time under all failure conditions.

The architecture must therefore explicitly define the expected delivery semantics.

---

# 2. Problem Statement

The platform must determine which delivery guarantee will be assumed for integration events.

The selected model must:

- tolerate infrastructure failures
- support horizontal scaling
- support retries
- support recovery after crashes
- preserve business consistency
- avoid distributed transactions
- remain operationally practical
- support SQS
- support Transactional Outbox
- define consumer responsibilities

---

# 3. Considered Options

## Option A — At-Most-Once

Characteristics:

- messages are delivered zero or one time
- no retries
- message loss is possible

Advantages:

- simple implementation
- low infrastructure cost

Disadvantages:

- lost business events
- unacceptable for critical workflows

---

## Option B — Exactly-Once

Characteristics:

- each business event processed exactly once

Advantages:

- attractive conceptual model

Disadvantages:

- extremely difficult across independent systems
- high operational complexity
- still requires idempotent business behavior
- expensive coordination

Exactly-once processing across database + broker + consumer cannot be guaranteed without significant complexity.

---

## Option C — At-Least-Once

Characteristics:

- every committed event will eventually be delivered
- duplicate deliveries may occur

Advantages:

- reliable
- operationally proven
- compatible with SQS
- compatible with Transactional Outbox
- resilient to crashes
- supports retries

Disadvantages:

- consumers must be idempotent
- duplicate processing must be prevented

---

# 4. Decision

The AstraForge Supply Platform adopts **At-Least-Once** message delivery for every integration event.

This means:

- committed events must eventually be delivered
- duplicate delivery is acceptable
- event loss is unacceptable
- consumers are responsible for idempotency

---

# 5. Rationale

Reliability is more important than avoiding duplicate delivery.

Losing an event such as:

```text
OrderApproved
```

may leave downstream systems permanently inconsistent.

Receiving:

```text
OrderApproved
```

twice is acceptable provided consumers are idempotent.

The architecture therefore favors:

```text
Duplicate delivery

instead of

Lost delivery
```

---

# 6. Delivery Semantics

The platform guarantees:

```text
Committed event

↓

Persisted in Outbox

↓

Eventually published

↓

Eventually consumed
```

The platform does **not** guarantee:

- single delivery
- global ordering
- simultaneous processing
- synchronous consistency

---

# 7. Duplicate Delivery

Duplicate delivery may occur when:

```text
SQS acknowledges publication

↓

Dispatcher crashes

↓

Outbox record remains pending

↓

Dispatcher retries

↓

Consumer receives duplicate
```

This behavior is expected.

---

# 8. Consumer Requirements

Every consumer must be idempotent.

A consumer must safely ignore duplicate events.

Possible strategies include:

- processed-event table
- Inbox Pattern
- unique event identifiers
- aggregate version comparison
- natural business keys
- optimistic locking

---

# 9. Event Identifier

Every integration event must include:

```text
eventId
```

The identifier:

- never changes
- survives retries
- identifies duplicate deliveries
- supports replay
- supports tracing

---

# 10. Retry Model

Retries occur when publication fails.

Example:

```text
Attempt 1

↓

Temporary failure

↓

Retry

↓

Retry

↓

Success
```

Retries must be:

- bounded
- observable
- configurable

---

# 11. Consumer Failures

Consumer failures are expected.

Consumers may retry internally according to their own retry policy.

The producer does not assume consumer success.

---

# 12. Ordering

Ordering is guaranteed only when:

- the same aggregate identifier is used as the FIFO MessageGroupId
- FIFO MessageGroupId ordering is preserved

Ordering is **not** guaranteed across unrelated aggregates.

---

# 13. Event Replay

Replay is supported.

Replay must:

- preserve eventId
- preserve payload
- be auditable
- avoid duplicate business effects

---

# 14. Operational Monitoring

Operations should monitor:

- pending events
- failed events
- oldest pending event
- retry rate
- duplicate rate
- replay count

---

# 15. Anti-Patterns

The following are prohibited:

- assuming exactly-once processing
- deleting failed events
- ignoring duplicate delivery
- changing event IDs during retry
- rebuilding event payloads
- depending on delivery order across aggregates

---

# 16. Positive Consequences

The decision provides:

- reliable delivery
- crash recovery
- retry capability
- operational simplicity
- compatibility with SQS
- compatibility with Transactional Outbox
- resilient architecture

---

# 17. Negative Consequences

The decision introduces:

- duplicate deliveries
- consumer idempotency requirements
- replay governance
- retry monitoring

These costs are acceptable because reliable delivery is more valuable than attempting unrealistic exactly-once guarantees.

---

# 18. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Duplicate delivery | Consumer idempotency |
| Replay duplicates | Stable event IDs |
| Consumer bugs | Contract tests |
| Retry storms | Exponential backoff |
| Event backlog | Monitoring and alerts |
| Poison messages | Dead-letter strategy and operational replay |

---

# 19. Implementation Guidance

Mandatory rules:

1. Every event has a unique immutable eventId.
2. Consumers must be idempotent.
3. Retries are expected.
4. Duplicate deliveries are expected.
5. Lost committed events are unacceptable.
6. Replay must preserve eventId.
7. Event contracts must be versioned.
8. Delivery semantics must be documented for every consumer.

---

# 20. Validation

The architecture will be validated through:

- duplicate-delivery tests
- retry tests
- replay tests
- consumer idempotency tests
- integration tests
- chaos testing
- failure injection

---

# 21. Success Criteria

The decision is successful when:

- committed events are never permanently lost
- duplicate events do not produce duplicate business effects
- consumers remain idempotent
- retries recover transient failures
- replay succeeds safely
- operational monitoring detects publication problems

---

# 22. Related Decisions

- ADR-001 — Adopt Clean Architecture
- ADR-002 — Adopt Domain-Driven Design
- ADR-005 — Use PostgreSQL as the Primary Database
- ADR-006 — Use Flyway for Database Migrations
- ADR-007 — Adopt Transactional Outbox
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard

---

# 23. References

- Enterprise Integration Patterns
- Microservices Patterns — Chris Richardson
- Amazon SQS Documentation
- Transactional Outbox Pattern
- AstraForge Supply Platform Messaging Guidelines

---

# 24. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved |

---

# 25. Decision Summary

The AstraForge Supply Platform explicitly adopts **At-Least-Once** delivery semantics.

The architecture assumes that:

- every committed event must eventually be delivered;
- duplicate delivery is normal;
- consumers must be idempotent;
- retries are part of normal operation.

This decision complements **ADR-007 (Transactional Outbox)** and establishes the messaging guarantees expected across every bounded context.
