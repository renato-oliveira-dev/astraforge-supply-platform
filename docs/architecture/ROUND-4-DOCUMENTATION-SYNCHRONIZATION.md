# Round 4 — Complementary Documentation Synchronization Report

## Scope

All 51 Markdown documents outside `docs/architecture/decisions/` were reviewed against the consolidated ADR-001 through ADR-090 baseline.

## Canonical messaging decision

The current messaging baseline is **ADR-090 / Amazon SQS**. Kafka-specific operational guidance has been removed from active complementary documentation. The historical `docs/infrastructure/kafka-architecture.md` path is retained as a superseded redirect so old links do not silently lead to obsolete instructions.

A new canonical document was added:

`docs/infrastructure/sqs-architecture.md`

## Principal changes

- README technology stack and roadmap aligned from Kafka to Amazon SQS.
- Messaging architecture rewritten around SQS Standard/FIFO semantics.
- Transactional Outbox documentation aligned to SQS while keeping the Outbox core broker-independent.
- Messaging guidelines rewritten to remove Kafka topic/partition/consumer-group assumptions.
- SQS naming, FIFO message groups, DLQ and stable event-ID rules standardized.
- Testing guidance changed from Kafka Testcontainers to SQS-compatible infrastructure such as LocalStack where broker behavior is required.
- Domain documentation now explicitly keeps Amazon SQS/AWS SDK outside the Domain layer.
- Resilience and observability documentation now uses SQS backlog, oldest-message age, DLQ, and SendMessage terminology.
- ADR guide examples updated to the current SQS decision.

## Historical preservation

Kafka ADRs remain available as historical decisions with superseded status.

The Kafka infrastructure filename is retained only as a historical redirect.

## Result

The active complementary documentation no longer presents Kafka as the current reference messaging implementation.
