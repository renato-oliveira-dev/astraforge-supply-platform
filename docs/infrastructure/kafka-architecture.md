# Kafka Architecture

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Kafka Architecture |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines how Apache Kafka is used by the Enterprise Order Platform.

It establishes:

- topic topology
- producer architecture
- consumer architecture
- partition strategy
- retries
- dead-letter queues
- schema evolution
- operational guidelines
- scalability
- monitoring

Kafka is the reference messaging platform for asynchronous communication.

---

# 2. Goals

Kafka provides:

- high throughput
- durability
- scalability
- replay capability
- consumer isolation
- event ordering per aggregate
- fault tolerance

---

# 3. High-Level Architecture

```
Application Service

↓

Transactional Outbox

↓

Outbox Dispatcher

↓

Kafka Producer

↓

Kafka Broker

↓

Kafka Topic

↓

Kafka Consumer

↓

Application Service
```

---

# 4. Topic Naming Convention

Format

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

notification.email.sent
```

---

# 5. Topic Categories

Business Events

```
orders.*

inventory.*

payment.*

shipment.*
```

Operational Topics

```
retry.*

dlq.*

audit.*
```

---

# 6. Topic Configuration

Recommended defaults

| Property | Value |
|----------|------:|
| Replication Factor | 3 |
| Min In-Sync Replicas | 2 |
| Cleanup Policy | delete |
| Compression | zstd |
| Message Format | JSON (or Avro) |

---

# 7. Partition Strategy

Partition key

```
AggregateId
```

Benefits

- ordering
- scalability
- balanced workload

Never partition randomly.

---

# 8. Producer Architecture

```
Application

↓

Outbox Dispatcher

↓

Kafka Producer

↓

Kafka Cluster
```

The producer never publishes directly from the business transaction.

---

# 9. Producer Configuration

Recommended settings

| Property | Recommendation |
|----------|----------------|
| acks | all |
| enable.idempotence | true |
| retries | Integer.MAX_VALUE |
| delivery.timeout.ms | configurable |
| linger.ms | 5–20 ms |
| batch.size | workload dependent |
| compression.type | zstd |

---

# 10. Producer Keys

Every event uses:

```
AggregateId
```

as Kafka Message Key.

This guarantees ordering for the aggregate.

---

# 11. Consumer Architecture

Each bounded context owns independent consumers.

Example

```
Inventory Consumer

↓

Payment Consumer

↓

Notification Consumer

↓

Analytics Consumer
```

Consumers never share business logic.

---

# 12. Consumer Groups

Each business capability has its own group.

Examples

```
inventory-service

payment-service

notification-service

analytics-service
```

---

# 13. Offset Management

Offsets are committed only after successful processing.

Flow

```
Receive

↓

Process

↓

Commit Offset
```

Never commit before processing.

---

# 14. Retry Topics

Recommended topology

```
orders.order.created

↓

retry.5s

↓

retry.30s

↓

retry.5m

↓

retry.30m

↓

DLQ
```

---

# 15. Dead Letter Queue

Each topic has its own DLQ.

Example

```
orders.order.created.dlq
```

DLQ messages are never discarded automatically.

---

# 16. Message Ordering

Ordering is guaranteed only within one partition.

The platform guarantees ordering per Aggregate.

Cross-aggregate ordering is intentionally unsupported.

---

# 17. Schema Registry

Preferred approach

```
Confluent Schema Registry
```

Alternative

```
JSON Schema Registry
```

Every event schema is versioned.

---

# 18. Schema Evolution

Allowed

- optional fields
- new fields
- backward-compatible changes

Forbidden

- removing required fields
- changing semantic meaning
- incompatible type changes

---

# 19. Event Versioning

Example

```
OrderCreated v1

↓

OrderCreated v2

↓

OrderCreated v3
```

Consumers should support gradual migration.

---

# 20. Message Size

Recommended

```
< 1 MB
```

Large payloads should be stored externally.

Example

```
Object Storage

↓

Reference URI

↓

Kafka Event
```

---

# 21. Security

Kafka communication should use:

- TLS
- SASL
- ACLs
- authenticated producers
- authenticated consumers

Sensitive data should be encrypted when required.

---

# 22. Observability

Expose metrics

- producer throughput
- consumer throughput
- consumer lag
- retry count
- DLQ count
- broker latency
- publish latency
- processing latency

---

# 23. Logging

Log

- publication
- retries
- failures
- rebalance events
- consumer startup
- consumer shutdown

Never log sensitive payloads.

---

# 24. Rebalancing

Consumers must tolerate partition reassignment.

Business processing must remain idempotent.

---

# 25. Scaling

Increase throughput by:

- adding partitions
- increasing consumer instances
- optimizing batch size
- tuning compression

Avoid increasing partitions without evaluating ordering implications.

---

# 26. Disaster Recovery

Kafka clusters should support:

- multi-broker deployment
- replication
- backup
- cross-region replication (when required)

Recovery procedures must be tested regularly.

---

# 27. Testing

Kafka integration tests should verify:

- serialization
- schema compatibility
- ordering
- retries
- DLQ routing
- consumer restart
- replay
- partition assignment

---

# 28. Architecture Rules

Kafka:

- transports Integration Events only
- is isolated by the Outbox
- never bypasses Application Services
- supports replay
- assumes idempotent consumers

---

# 29. Decision Summary

The platform adopts:

- Apache Kafka
- aggregate-based partitioning
- Outbox publishing
- idempotent producers
- idempotent consumers
- retry topics
- dedicated DLQs
- schema versioning
- secure communication
- operational observability

---

# 30. Next Documentation Step

Next document

```
docs/infrastructure/postgresql-architecture.md
```

It will define:

- persistence architecture
- schema organization
- indexing strategy
- optimistic locking
- migrations
- partitioning
- auditing
- performance tuning
- backup and recovery
