# Kafka Architecture — Historical / Superseded

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Kafka Architecture |
| Status | Superseded |
| Superseded By | ADR-090 and `docs/infrastructure/sqs-architecture.md` |

---

This document path is retained only to preserve historical links.

Kafka is **not the current messaging baseline** for the AstraForge Supply Platform.

The current canonical messaging architecture is Amazon SQS with governed integration-event contracts, idempotent consumers, monitored DLQs, and Transactional Outbox where reliable state-plus-event publication is required.

Use:

- `docs/infrastructure/sqs-architecture.md`
- `docs/infrastructure/messaging-architecture.md`
- `docs/infrastructure/transactional-outbox.md`
- `docs/architecture/decisions/ADR-090-adopt-enterprise-event-driven-architecture-sqs-transactional-outbox-idempotency-event-contract-and-messaging-governance-standard.md`

Historical Kafka-specific ADRs remain in the repository as architectural history and are marked superseded.
