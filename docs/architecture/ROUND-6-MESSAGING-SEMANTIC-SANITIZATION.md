# Round 6 — Messaging Semantic Sanitization

## Objective

Remove normative Kafka-specific guidance from ADRs that remain `Accepted` after ADR-090 established Amazon SQS as the canonical messaging baseline.

Historical ADRs whose status is `Superseded` remain unchanged as architectural records.

## Changes

- Updated active ADR terminology from Kafka-specific concepts to Amazon SQS or technology-neutral messaging concepts.
- Replaced partition/offset/rebalance assumptions with SQS Standard/FIFO, `MessageGroupId`, acknowledgement/delete, visibility timeout, redelivery, queue backlog and oldest-message-age semantics where applicable.
- Replaced historical ADR-009/ADR-057 references inside active guidance with ADR-090 where they represented the current messaging decision.
- Preserved ADR-009, ADR-030 and ADR-057 as superseded historical decisions.
- Extended `scripts/validate_adr_governance.py` with an active-messaging-baseline fitness function.

## Automated Guardrail

The validator now fails when any `Accepted` ADR other than ADR-090 contains the term `Kafka` (case-insensitive). ADR-090 is the only accepted exception because it explicitly documents the transition from the superseded Kafka baseline to SQS.

## Validation Result

```text
ADR Governance Validation
=========================
Configured ADR range : ADR-001..ADR-090
ADR files discovered : 90
Accepted             : 74
Superseded           : 16
Warnings             : 0
Errors               : 0

RESULT: PASSED
```

A negative regression test was also executed by injecting `Kafka MUST be used.` into ADR-001. The validator correctly returned exit code 1 and identified ADR-001 as violating the active SQS baseline.

## Final State

- ADR sequence: 001–090 complete.
- Active ADRs: no Kafka-specific normative guidance outside ADR-090's historical transition note.
- Superseded Kafka ADRs: preserved unchanged.
- Canonical messaging decision: ADR-090.
