# Messaging Architecture

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Messaging Architecture |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the messaging architecture of the Enterprise Order Platform.

It establishes:

- asynchronous communication
- event publication
- event consumption
- topic organization
- retry strategy
- dead-letter handling
- schema evolution
- ordering guarantees
- consumer responsibilities
- producer responsibilities

The platform adopts an **Event-Driven Architecture (EDA)** based on Integration Events.

---

# 2. Goals

The messaging platform must provide:

- loose coupling
- scalability
- resilience
- eventual consistency
- independent deployments
- replay capability
- observability
- fault tolerance

---

# 3. High-Level Architecture

```
              Application Service
                       │
                       ▼
                Domain Events
                       │
                       ▼
           Integration Event Mapper
                       │
                       ▼
               Transactional Outbox
                       │
                       ▼
              Outbox Dispatcher
                       │
                       ▼
              Message Broker
                       │
      ┌────────────────┼────────────────┐
      ▼                ▼                ▼
 Inventory      Notification      Analytics
 Consumer         Consumer         Consumer
```

---

# 4. Broker

The reference implementation uses:

```
Apache Kafka
```

Alternative implementations may use:

- RabbitMQ
- Azure Service Bus
- Amazon SNS/SQS
- Google Pub/Sub

The Domain and Application layers remain independent of the messaging technology.

---

# 5. Event Lifecycle

```
Aggregate

↓

Domain Event

↓

Integration Event

↓

Outbox

↓

Dispatcher

↓

Kafka

↓

Consumer

↓

Business Processing
```

---

# 6. Topic Organization

Topics follow the pattern:

```
<context>.<aggregate>.<event>
```

Examples

```
orders.order.created

orders.order.submitted

orders.order.approved

orders.order.cancelled

inventory.reservation.confirmed

inventory.reservation.failed

payment.authorized

payment.failed

shipment.created

shipment.delivered
```

---

# 7. Naming Rules

Topics:

- lowercase
- dot-separated
- business-oriented
- technology-independent

Avoid:

```
topic1

ordersTopic

kafka-orders
```

---

# 8. Event Envelope

Every Integration Event contains:

```
EventId

EventType

AggregateId

AggregateType

AggregateVersion

OccurredAt

CorrelationId

CausationId

TraceId

Payload
```

---

# 9. Producer Responsibilities

The producer:

- publishes Integration Events
- validates serialization
- adds metadata
- preserves ordering keys
- never performs business logic

---

# 10. Consumer Responsibilities

Consumers:

- deserialize events
- validate contracts
- process business logic
- acknowledge messages
- implement idempotency
- emit metrics

---

# 11. Partition Strategy

Partition key:

```
AggregateId
```

Benefits:

- preserves ordering
- improves scalability
- distributes workload

All events for the same Aggregate are routed to the same partition.

---

# 12. Ordering Guarantees

Ordering is guaranteed only within a partition.

Example:

```
OrderCreated

↓

OrderSubmitted

↓

OrderApproved

↓

OrderCompleted
```

Cross-aggregate ordering is not guaranteed.

---

# 13. Consumer Groups

Each business capability owns an independent consumer group.

Example

```
Inventory Service

↓

inventory-group
```

```
Notification Service

↓

notification-group
```

```
Analytics Service

↓

analytics-group
```

Each consumer group processes every event independently.

---

# 14. Retry Strategy

Transient failures should trigger retries.

Recommended approach:

```
Main Topic

↓

Retry Topic (5s)

↓

Retry Topic (30s)

↓

Retry Topic (5m)

↓

DLQ
```

---

# 15. Dead Letter Queue

Events that cannot be processed are moved to:

```
<topic>.dlq
```

Example

```
orders.order.created.dlq
```

DLQ events require operational investigation.

---

# 16. Idempotency

Consumers must tolerate duplicate delivery.

Recommended key:

```
EventId
```

Alternative:

```
AggregateId + AggregateVersion
```

Duplicate processing must not change business state.

---

# 17. Event Contracts

Contracts are immutable.

Breaking changes require:

- new version
- new schema
- backward compatibility

Never modify published contracts in place.

---

# 18. Schema Evolution

Preferred strategy:

```
v1

↓

v2

↓

v3
```

Consumers should ignore unknown fields whenever possible.

---

# 19. Message Size

Recommended maximum:

```
< 1 MB
```

Large binary content must not be embedded.

Instead, publish a reference (e.g., object storage URI).

---

# 20. Security

Messages must never contain:

- passwords
- tokens
- credentials
- sensitive personal data

Sensitive fields should be encrypted or omitted.

---

# 21. Observability

Each producer and consumer exposes metrics:

- publish rate
- consume rate
- retry count
- processing latency
- consumer lag
- DLQ size
- failures

---

# 22. Logging

Log:

- publication
- consumption
- retries
- failures
- processing time

Avoid logging full payloads in production.

---

# 23. Monitoring

Recommended dashboards:

- topic throughput
- partition distribution
- consumer lag
- retry rate
- DLQ count
- publication latency
- processing latency

---

# 24. Failure Recovery

Consumers must support restart without data loss.

Recovery flow:

```
Consumer Restart

↓

Resume Offset

↓

Continue Processing
```

---

# 25. Replay

Events may be replayed for:

- rebuilding projections
- analytics
- auditing
- disaster recovery

Replay requires idempotent consumers.

---

# 26. Testing

Messaging tests should verify:

- serialization
- deserialization
- ordering
- duplicate delivery
- retry flow
- DLQ routing
- contract compatibility
- schema evolution

---

# 27. Architecture Rules

Messaging infrastructure:

- never invokes aggregates directly
- never bypasses Application Services
- transports Integration Events only
- remains asynchronous
- preserves eventual consistency

---

# 28. Decision Summary

The platform adopts:

- Event-Driven Architecture
- Apache Kafka (reference implementation)
- Transactional Outbox
- Integration Events
- Aggregate-based partitioning
- idempotent consumers
- retry topics
- dead-letter queues
- schema versioning
- consumer isolation

---

# 29. Next Documentation Step

Next document

```
docs/infrastructure/idempotency.md
```

It will define:

- duplicate detection
- exactly-once vs at-least-once semantics
- idempotency keys
- consumer persistence
- replay safety
- deduplication algorithms
