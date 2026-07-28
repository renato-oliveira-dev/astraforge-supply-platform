# ADR-018: Version Integration Event Contracts

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-018 |
| Title | Version Integration Event Contracts |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Messaging, Event Contracts and Evolution |
| Related Work Items | SQS Integration, Event-Driven Architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform is an event-driven system built around Amazon SQS.

Business events are exchanged between independent services such as:

- Orders
- Inventory
- Payments
- Notifications
- Customers
- Workflow
- Reporting
- Audit

Each service evolves independently and is deployed without synchronized releases.

Consequently, event contracts must evolve without breaking existing consumers.

---

# 2. Problem Statement

The platform requires a strategy that:

- allows event evolution
- preserves backward compatibility
- supports multiple consumer versions
- avoids synchronized deployments
- supports rolling upgrades
- enables schema validation
- supports replay
- allows gradual migration
- integrates with SQS
- remains technology independent

---

# 3. Decision Drivers

Primary drivers include:

1. backward compatibility
2. independent deployments
3. consumer stability
4. replay capability
5. schema governance
6. operational safety
7. long-term maintainability
8. event evolution
9. observability
10. auditability

---

# 4. Considered Options

## Option A — Unversioned Events

Advantages:

- simplest implementation

Disadvantages:

- breaking changes
- deployment coordination
- fragile consumers

---

## Option B — Topic Versioning

Example:

```text
orders.v1

orders.v2
```

Advantages:

- explicit separation

Disadvantages:

- topic proliferation
- migration complexity
- duplicated infrastructure

---

## Option C — Event Contract Versioning

Each event carries its own version.

Advantages:

- flexible evolution
- consumer compatibility
- reduced topic proliferation
- gradual migration

Disadvantages:

- version governance required

---

# 5. Decision

The Enterprise Order Platform adopts **event contract versioning**.

Every integration event must expose an explicit contract version.

---

# 6. Event Version vs Aggregate Version

These concepts are independent.

Contract Version:

```text
Represents the event schema.
```

Aggregate Version:

```text
Represents the business aggregate state.
```

They must never be confused.

---

# 7. Event Structure

Every event should contain:

- eventId
- eventType
- eventVersion
- occurredAt
- aggregateId
- aggregateVersion
- correlationId
- causationId
- payload

---

# 8. Example

```json
{
  "eventId": "f76e12cb-fc58-4a54-b9f0-1bb53a8c62e7",
  "eventType": "ORDER_APPROVED",
  "eventVersion": 2,
  "occurredAt": "2026-07-23T18:00:00Z",
  "aggregateId": "6dbbb1ef-8145-4cb7-b6b5-df6ec5d8a36b",
  "aggregateVersion": 8,
  "correlationId": "df4d63ef-c41c-40aa-ae94-4b9db04a6e4f",
  "causationId": "2a4b30bc-d45f-4932-9b98-2b844db6d49d",
  "payload": {
    "orderId": "6dbbb1ef-8145-4cb7-b6b5-df6ec5d8a36b",
    "status": "APPROVED"
  }
}
```

---

# 9. Versioning Rules

Contract versions:

- are immutable
- increase monotonically
- never decrease
- never reuse previous numbers

---

# 10. Compatible Changes

Allowed without new major contract:

- optional field
- optional metadata
- documentation improvements

---

# 11. Breaking Changes

Require a new contract version.

Examples:

- removing field
- renaming field
- changing field meaning
- incompatible type changes

---

# 12. Consumer Compatibility

Consumers should ignore unknown fields whenever possible.

This enables forward compatibility.

---

# 13. Producer Compatibility

Producers must continue publishing required fields for supported versions.

---

# 14. Schema Governance

Every event contract must be documented.

Recommended artifacts include:

- JSON Schema
- AsyncAPI
- Avro Schema
- Protobuf

The platform standard should remain consistent.

---

# 15. Topic Strategy

Topic names represent business domains.

Examples:

```text
orders.events

inventory.events

payments.events
```

Contract evolution occurs through eventVersion rather than topic duplication.

---

# 16. Replay

Older events must remain interpretable.

Consumers should retain compatibility with supported historical versions.

---

# 17. Deprecation

Deprecated versions must define:

- deprecation date
- replacement version
- removal date
- migration guidance

---

# 18. Validation

Producers validate outgoing contracts.

Consumers validate incoming contracts before processing.

Invalid events should be rejected.

---

# 19. Observability

Metrics should include:

- event version usage
- invalid contract count
- unsupported version count

---

# 20. Security

Sensitive data must never be added merely for compatibility.

Contract evolution must respect security standards.

---

# 21. Testing

Tests should validate:

- schema compatibility
- serialization
- deserialization
- backward compatibility
- forward compatibility
- replay

---

# 22. Anti-Patterns

The following are prohibited:

- removing required fields
- changing field semantics silently
- reusing version numbers
- publishing undocumented events
- incompatible changes without version increment

---

# 23. Positive Consequences

The decision provides:

- safer evolution
- independent deployments
- replay compatibility
- improved governance
- predictable integrations

---

# 24. Negative Consequences

The decision introduces:

- documentation effort
- schema management
- version lifecycle governance

These costs are acceptable.

---

# 25. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Consumer incompatibility | Version validation |
| Schema drift | Central contract governance |
| Legacy consumers | Supported compatibility period |
| Version proliferation | Clear lifecycle management |

---

# 26. Implementation Guidance

Mandatory rules:

1. Every event exposes `eventVersion`.
2. Event versions are immutable.
3. Breaking changes require a new version.
4. Consumers validate supported versions.
5. Producers document every contract.
6. Events remain backward compatible whenever practical.
7. Topic names remain stable.
8. Replay compatibility is preserved.
9. Contract documentation is version controlled.
10. Deprecated versions define migration plans.

---

# 27. Validation

Validation includes:

- contract review
- schema validation
- compatibility testing
- replay testing
- consumer integration testing

---

# 28. Success Criteria

The decision is successful when:

- producers evolve independently
- consumers continue operating during upgrades
- replay succeeds
- schema changes remain predictable
- breaking changes are explicitly versioned

---

# 29. Related Decisions

- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-017: Adopt Optimistic Locking for Concurrent Aggregate Updates

---

# 30. References

- Amazon SQS Documentation
- AsyncAPI Specification
- JSON Schema Specification
- Apache Avro Specification
- Enterprise Integration Patterns

---

# 31. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved |

---

# 32. Decision Summary

The Enterprise Order Platform adopts explicit versioning for all integration-event contracts.

The platform standardizes on:

- immutable event contracts
- explicit `eventVersion`
- backward compatibility
- documented schemas
- stable topic names
- predictable evolution
- replay support
- consumer validation

This decision enables independent service evolution while preserving reliable communication across the event-driven platform.
