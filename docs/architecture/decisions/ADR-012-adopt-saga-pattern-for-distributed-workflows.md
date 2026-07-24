# ADR-012: Adopt the Saga Pattern for Distributed Workflows

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-012 |
| Title | Adopt the Saga Pattern for Distributed Workflows |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Distributed Transactions and Workflow Coordination |
| Related Work Items | Order fulfillment and cross-context business workflows |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform coordinates business processes that span multiple bounded contexts.

Examples include:

- order creation
- inventory reservation
- payment authorization
- fraud validation
- order approval
- fulfillment initiation
- shipment preparation
- cancellation
- refund
- inventory release
- customer notification

Each bounded context owns its own:

- domain model
- application services
- database
- transactional boundary
- integration contracts
- deployment lifecycle

A business workflow may therefore require changes in multiple independent systems.

Example:

```text
Order created

↓

Inventory reserved

↓

Payment authorized

↓

Order confirmed

↓

Fulfillment initiated
```

No single local database transaction can atomically commit all these operations.

The platform rejects distributed two-phase commit because it would introduce excessive coupling, latency, availability risk and operational complexity.

A distributed workflow coordination strategy is therefore required.

---

# 2. Problem Statement

The platform must coordinate multi-step business processes across independently deployable bounded contexts.

The solution must:

- preserve bounded-context autonomy
- avoid distributed ACID transactions
- support eventual consistency
- support compensating actions
- tolerate partial failure
- support retries
- support timeouts
- support duplicate delivery
- integrate with Kafka
- integrate with Transactional Outbox
- support idempotent processing
- expose workflow state
- provide operational visibility
- support horizontal scaling
- remain compatible with Clean Architecture
- remain compatible with Domain-Driven Design
- support controlled manual intervention
- remain understandable to engineering and operations teams

---

# 3. Decision Drivers

The primary decision drivers are:

1. distributed business consistency
2. bounded-context autonomy
3. fault tolerance
4. recoverability
5. explicit workflow state
6. operational observability
7. idempotency
8. compatibility with Kafka
9. compatibility with Transactional Outbox
10. support for compensation
11. support for timeouts
12. maintainability
13. testability
14. horizontal scalability
15. auditability
16. avoidance of distributed locking
17. avoidance of two-phase commit
18. support for long-running workflows

---

# 4. Constraints

The decision must consider:

- PostgreSQL is the authoritative store for each bounded context
- Kafka is the asynchronous integration-event broker
- Transactional Outbox is used for reliable event publication
- message delivery is at least once
- consumers must be idempotent
- multiple service instances may run concurrently
- workflows may last seconds, minutes or longer
- external services may be temporarily unavailable
- compensation may fail
- not every business action is technically reversible
- event ordering is partition-specific
- public event contracts must be versioned
- domain code must remain infrastructure-independent
- operational teams require metrics, alerts and runbooks
- rolling deployments must remain safe

---

# 5. Considered Options

## 5.1 Option A: Distributed Two-Phase Commit

A coordinator could attempt to commit multiple transactional resources atomically.

### Advantages

- strong atomicity model
- familiar transaction semantics
- one commit or rollback decision

### Disadvantages

- high coordination cost
- reduced availability
- poor cloud-native compatibility
- tight infrastructure coupling
- difficult failure recovery
- long-held locks
- limited compatibility with Kafka and independent databases
- operational complexity
- weak support for long-running workflows

---

## 5.2 Option B: Ad Hoc Event Choreography

Each bounded context could react to events and publish new events without an explicit workflow model.

### Advantages

- low initial implementation cost
- loose direct coupling
- natural event-driven style
- no central coordinator

### Disadvantages

- hidden workflow
- difficult end-to-end visibility
- difficult timeout handling
- difficult compensation tracking
- complex failure diagnosis
- cyclic event chains
- unclear process ownership
- difficult workflow evolution
- difficult manual recovery

---

## 5.3 Option C: Centralized Workflow Orchestrator

A dedicated saga orchestrator could coordinate commands, events, timeouts and compensations.

### Advantages

- explicit workflow state
- clear process ownership
- centralized timeout handling
- easier operational visibility
- controlled compensation
- easier workflow evolution
- stronger auditability

### Disadvantages

- additional component
- orchestration state must be persisted
- coordinator may become overly complex
- risk of centralizing domain logic incorrectly
- increased implementation effort

---

## 5.4 Option D: External Workflow Engine

Examples include:

- Temporal
- Camunda
- Zeebe
- AWS Step Functions
- Azure Durable Functions
- Conductor

### Advantages

- durable workflow execution
- built-in retries
- built-in timers
- operational interfaces
- workflow history
- strong long-running process support

### Disadvantages

- additional platform dependency
- operational learning curve
- infrastructure coupling
- possible vendor lock-in
- workflow-engine semantics influence application design
- migration complexity
- may be excessive for the initial platform scope

---

## 5.5 Option E: Saga Pattern with Selective Orchestration

The platform could adopt the Saga pattern and use orchestration for complex workflows while allowing limited choreography for simple event propagation.

### Advantages

- explicit coordination where needed
- preserves bounded-context autonomy
- supports compensation
- supports eventual consistency
- supports long-running workflows
- compatible with Kafka and Transactional Outbox
- avoids distributed transactions
- allows incremental adoption
- supports operational visibility

### Disadvantages

- more design discipline
- workflow state persistence
- compensation complexity
- idempotency requirements
- eventual consistency
- more integration testing
- operational runbooks are required

---

# 6. Decision

The Enterprise Order Platform adopts the Saga pattern for distributed business workflows.

The default strategy for complex, stateful or compensating workflows will be orchestration.

Simple event propagation may use choreography when all of the following are true:

- the flow is short
- ownership is clear
- no explicit compensation coordination is required
- no complex timeout handling is required
- no hidden multi-step business invariant is created
- operational visibility remains sufficient

Complex order workflows must use an explicit saga orchestrator or process manager.

---

# 7. Rationale

Distributed business processes cannot rely on one ACID transaction across independent services.

The Saga pattern provides a practical consistency model based on:

```text
Local transactions

+

Reliable messages

+

Explicit workflow state

+

Compensating actions
```

The platform prioritizes:

- autonomy
- resilience
- recoverability
- operational clarity

over artificial cross-system atomicity.

---

# 8. Core Principle

A saga is a sequence of local transactions.

Each step:

1. performs one local business transaction
2. persists its result
3. publishes an integration event or responds to a command
4. advances the workflow
5. triggers compensation when required

Example:

```text
Create order

↓

Reserve inventory

↓

Authorize payment

↓

Confirm order
```

Failure path:

```text
Create order

↓

Reserve inventory

↓

Payment rejected

↓

Release inventory

↓

Cancel order
```

---

# 9. Consistency Model

The platform explicitly accepts eventual consistency across bounded contexts.

During saga execution, different services may temporarily hold different views of the overall business process.

This is expected.

The workflow must expose intermediate states clearly.

Examples include:

```text
PENDING_INVENTORY

PENDING_PAYMENT

CONFIRMED

COMPENSATING

CANCELLED

FAILED
```

---

# 10. Local Transaction Boundary

Each saga participant owns one local transaction.

A participant must not attempt to update another bounded context's database.

Example:

```text
Inventory Service transaction:

Reserve inventory

Persist reservation

Persist outbox event

Commit
```

The participant then publishes:

```text
InventoryReserved
```

through its Transactional Outbox.

---

# 11. Saga Orchestrator

The orchestrator coordinates workflow progression.

Responsibilities include:

- starting the saga
- persisting saga state
- sending commands
- consuming replies or events
- validating expected transitions
- managing timeouts
- triggering compensation
- recording failures
- exposing workflow status
- supporting controlled recovery
- preventing duplicate transitions

The orchestrator must not take ownership of participant domain rules.

---

# 12. Process Manager

The saga orchestrator may be implemented as a process manager.

A process manager is a stateful application component that:

- correlates messages
- tracks workflow state
- decides the next step
- persists progress
- issues commands
- reacts to results

It belongs to the Application layer conceptually, with persistence and Kafka adapters in Infrastructure.

---

# 13. Orchestration Versus Choreography

## Orchestration

```text
Saga Orchestrator

├── ReserveInventory command
├── AuthorizePayment command
├── ConfirmOrder command
└── Compensating commands
```

Advantages:

- explicit control flow
- easier timeout handling
- centralized workflow status
- clearer compensation
- simpler operational diagnosis

## Choreography

```text
OrderCreated

↓

InventoryReserved

↓

PaymentAuthorized

↓

OrderConfirmed
```

Advantages:

- low direct coupling
- fewer central coordination components
- natural for simple propagation

The platform prefers orchestration when the workflow contains:

- more than a few business steps
- compensation
- explicit deadlines
- branching
- parallel activities
- conditional paths
- manual intervention
- strong audit requirements

---

# 14. Saga Ownership

Every saga must have one authoritative owner.

The owner is responsible for:

- workflow definition
- saga state
- transition rules
- timeout policy
- compensation policy
- observability
- operational runbook
- schema evolution
- support ownership

A saga must not have ambiguous ownership across teams.

---

# 15. Saga Identifier

Every saga instance must have a globally unique identifier.

Example:

```text
sagaId
```

The identifier must be propagated through:

- commands
- events
- logs
- traces
- saga state
- audit records
- operational tooling

The saga ID must remain stable for the entire workflow.

---

# 16. Correlation

Messages must include sufficient correlation metadata.

Recommended identifiers include:

- saga ID
- event ID
- command ID
- correlation ID
- causation ID
- aggregate ID
- trace ID

Example:

```json
{
  "eventId": "5f68fb79-6f85-498f-8366-b052eefdc62f",
  "sagaId": "e3fc9024-129f-48be-a252-cf65454fb782",
  "correlationId": "e3fc9024-129f-48be-a252-cf65454fb782",
  "causationId": "24a490e4-90ab-44a9-91ec-dcc6cc1436a8",
  "aggregateId": "08ab5af0-a5fb-4b90-a149-5d77057c663d"
}
```

---

# 17. Saga State

Saga state must be persisted durably.

A recommended conceptual model includes:

| Field | Purpose |
|---|---|
| `id` | Saga identifier |
| `saga_type` | Workflow type |
| `aggregate_id` | Primary business aggregate |
| `status` | Current saga status |
| `current_step` | Current workflow step |
| `version` | Optimistic-lock version |
| `started_at` | Start timestamp |
| `updated_at` | Last update timestamp |
| `deadline_at` | Workflow deadline |
| `last_error` | Sanitized failure summary |
| `completed_at` | Completion timestamp |
| `trace_id` | Distributed trace identifier |

Additional business-specific state may be stored in typed columns or controlled JSON.

---

# 18. Recommended Saga Statuses

A common status model may include:

```text
STARTED

IN_PROGRESS

WAITING

COMPENSATING

COMPLETED

CANCELLED

FAILED

MANUAL_REVIEW
```

Status semantics must be explicit.

Do not use vague values such as:

```text
OK

ERROR

DONE
```

---

# 19. Saga Steps

Each saga step must have:

- a stable identifier
- an owning participant
- an initiating command or event
- an expected success response
- expected failure responses
- retry policy
- timeout policy
- compensation behavior
- idempotency strategy
- observability metadata

Example:

```text
Step: RESERVE_INVENTORY

Command: ReserveInventory

Success: InventoryReserved

Failure: InventoryReservationRejected

Compensation: ReleaseInventory
```

---

# 20. Command Semantics

A command requests behavior from one authoritative bounded context.

Examples:

```text
ReserveInventory

AuthorizePayment

ReleaseInventory

CancelPaymentAuthorization
```

A command must:

- have one intended owner
- be explicit
- be versioned
- include a unique command ID
- include saga correlation
- support idempotent handling
- define expected outcomes

---

# 21. Event Semantics

An event reports a fact that already occurred.

Examples:

```text
InventoryReserved

PaymentAuthorized

OrderConfirmed

InventoryReleased
```

An event must not be named as an imperative command.

Poor example:

```text
ReserveInventoryEvent
```

Preferred:

```text
InventoryReserved
```

---

# 22. Command Response Model

Participants should respond through explicit success or failure events.

Example:

```text
ReserveInventory command
```

Possible outcomes:

```text
InventoryReserved

InventoryReservationRejected
```

Timeout is not equivalent to business rejection.

The orchestrator must distinguish:

- explicit negative result
- technical failure
- no response
- duplicate response
- stale response

---

# 23. Participant Idempotency

Every saga participant must process commands idempotently.

Duplicate commands may occur because of:

- producer retries
- broker redelivery
- orchestrator crash
- acknowledgement loss
- replay
- timeout recovery

A duplicate command must not repeat harmful business effects.

---

# 24. Command Identifier

Every command must contain a stable unique command ID.

The participant should persist processed-command identity when durable deduplication is required.

Example schema:

```sql
CREATE TABLE processed_command (
    consumer_name VARCHAR(150) NOT NULL,
    command_id UUID NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    result_type VARCHAR(150) NOT NULL,
    result_payload JSONB,
    PRIMARY KEY (consumer_name, command_id)
);
```

Persisting the previous result may allow deterministic duplicate responses.

---

# 25. Idempotent Response

When a duplicate command is received, the participant should return the same logical outcome where practical.

Example:

```text
First ReserveInventory command:

InventoryReserved
```

Duplicate:

```text
Return InventoryReserved again

Do not reserve twice
```

This allows the orchestrator to recover safely.

---

# 26. Transactional Outbox Integration

Every saga participant that changes local state and publishes an outcome must use the Transactional Outbox.

Example:

```text
Persist reservation

+

Persist InventoryReserved outbox event

↓

Commit
```

Direct publication after a database commit is prohibited for transactional saga outcomes.

---

# 27. Orchestrator Outbox

The orchestrator must also use the Transactional Outbox.

When advancing a saga:

```text
Update saga state

+

Persist next command in outbox

↓

Commit
```

This prevents saga state from advancing without reliably issuing the next command.

---

# 28. Inbox Integration

Critical saga consumers should use an Inbox or processed-message table.

The inbox and local business effect should commit in the same transaction where practical.

This ensures:

- duplicate detection
- durable processing
- crash recovery
- traceability

---

# 29. Optimistic Locking

Saga state must use optimistic locking or equivalent concurrency control.

Multiple messages may arrive concurrently.

Examples include:

- timeout and success response
- duplicate responses
- parallel branch completion
- cancellation and normal progression

The orchestrator must prevent invalid concurrent transitions.

---

# 30. Saga Version

A saga-state version should increase on every successful transition.

Example:

```text
version = 8
```

The version supports:

- optimistic concurrency
- stale-message detection
- audit analysis
- safe retries
- transition validation

---

# 31. Transition Rules

Saga transitions must be explicit.

Example:

```text
PENDING_INVENTORY
    → PENDING_PAYMENT
    → CONFIRMED
```

Failure path:

```text
PENDING_PAYMENT
    → COMPENSATING_INVENTORY
    → CANCELLED
```

Invalid transitions must be rejected safely.

---

# 32. State Machine

Complex sagas should be modeled as explicit state machines.

The state machine must define:

- states
- triggers
- guards
- transitions
- terminal states
- compensation states
- timeout transitions
- manual-review transitions

The state machine may be implemented directly in domain/application code or through an approved workflow framework.

---

# 33. Terminal States

Terminal states must be explicit.

Examples:

```text
COMPLETED

CANCELLED

FAILED

MANUAL_REVIEW
```

A terminal saga must not silently resume through unrelated duplicate messages.

Controlled replay or recovery must be explicit.

---

# 34. Compensation

Compensation is a business action that semantically reverses or mitigates a previous action.

Examples:

```text
Reserve inventory
→ Release inventory
```

```text
Authorize payment
→ Cancel authorization
```

```text
Capture payment
→ Refund payment
```

Compensation is not necessarily a technical rollback.

---

# 35. Compensation Semantics

Compensation must be defined by business meaning.

A compensation may:

- reverse the original action
- create an offsetting action
- mark the resource as cancelled
- issue a refund
- release a reservation
- create manual work
- record an irreversible failure

The platform must not assume every step is fully reversible.

---

# 36. Compensation Ordering

Compensations generally execute in reverse order of successful forward steps.

Example:

```text
1. Reserve inventory
2. Authorize payment
3. Create shipment
```

Failure after step 3 may require:

```text
1. Cancel shipment
2. Cancel payment authorization
3. Release inventory
```

The exact business ordering must be documented.

---

# 37. Compensation Idempotency

Compensating commands must also be idempotent.

Duplicate compensation must not:

- refund twice
- release inventory twice
- cancel an already completed operation incorrectly
- create duplicate audit records
- produce invalid state transitions

---

# 38. Compensation Failure

Compensation may fail.

The saga must support:

- retry
- timeout
- escalation
- manual review
- partial compensation visibility
- operational alerts
- audit history

A compensation failure must not be hidden behind a generic failed status.

---

# 39. Irreversible Steps

Some actions may be irreversible.

Examples may include:

- external legal submission
- physical shipment already handed over
- irreversible notification to an external authority
- completed financial settlement in certain contexts

Irreversible steps should occur as late as practical.

The workflow must define mitigation rather than fictional rollback.

---

# 40. Pivot Transaction

A saga may define a pivot transaction.

Before the pivot:

- compensation is expected to remain possible

After the pivot:

- the workflow should move toward completion through retries or forward recovery

Example:

```text
Payment capture
```

may represent a pivot depending on business rules.

The pivot must be explicitly documented.

---

# 41. Retriable Transaction

Some saga steps should be retried until a bounded operational threshold because they are expected to succeed eventually.

Examples may include:

- sending a command to an available participant
- completing a post-pivot bookkeeping action
- publishing a notification request

Retry does not eliminate the need for terminal failure policy.

---

# 42. Timeout

Every asynchronous request-response saga step must define a timeout.

The orchestrator must not wait indefinitely.

Example:

```text
Inventory reservation deadline: 30 seconds

Payment authorization deadline: 60 seconds
```

Timeout duration must reflect business and infrastructure behavior.

---

# 43. Timeout Persistence

Timeouts must be persisted durably.

Do not rely only on an in-memory timer.

The system must recover deadlines after:

- restart
- deployment
- failover
- pod rescheduling
- temporary outage

---

# 44. Timeout Processing

A timeout processor should identify overdue saga steps.

Example query:

```sql
SELECT *
FROM saga_instance
WHERE status = 'WAITING'
  AND deadline_at <= CURRENT_TIMESTAMP
ORDER BY deadline_at
FOR UPDATE SKIP LOCKED
LIMIT :batch_size;
```

Timeout processing must be horizontally safe.

---

# 45. Timeout Versus Failure

A timeout means no acceptable result arrived before the deadline.

It does not prove that the participant did nothing.

Example:

```text
Payment authorization succeeds

↓

Response is delayed

↓

Orchestrator times out
```

Compensation and reconciliation must account for this ambiguity.

---

# 46. Late Responses

The orchestrator must define handling for late responses.

Possible behavior includes:

- ignore because saga is terminal
- trigger reconciliation
- trigger compensation
- update manual-review state
- accept when transition remains valid
- record for audit only

Late responses must never cause an invalid silent transition.

---

# 47. Retry Policy

Each saga step must define its own retry policy.

Retries must be:

- bounded
- delayed
- observable
- idempotent
- failure-aware

Different participants may require different policies.

---

# 48. Retry Layers

Potential retry layers include:

- Kafka producer retry
- outbox dispatcher retry
- consumer retry
- saga command retry
- participant external-service retry

These layers must be coordinated.

Uncontrolled nested retries can create:

- excessive latency
- retry storms
- duplicate pressure
- hidden failure duration
- resource exhaustion

---

# 49. Backoff

Retries should use exponential or policy-based backoff with jitter.

Example:

```text
5 seconds

15 seconds

45 seconds

2 minutes

5 minutes
```

The maximum delay and total retry duration must be explicit.

---

# 50. Business Rejection

A business rejection is not a technical failure.

Examples:

```text
Insufficient inventory

Payment declined

Customer blocked

Order already cancelled
```

Business rejections should produce explicit outcome events and usually trigger compensation or a terminal business state.

They should not be retried as transient infrastructure errors.

---

# 51. Technical Failure

Technical failures include:

- network timeout
- database unavailable
- broker unavailable
- temporary provider outage
- lock contention
- service restart

These may be retried according to policy.

---

# 52. Failure Classification

Saga failures should be classified as:

```text
BUSINESS_REJECTION

TRANSIENT_INFRASTRUCTURE_FAILURE

PERMANENT_TECHNICAL_FAILURE

TIMEOUT

COMPENSATION_FAILURE

CONTRACT_FAILURE

MANUAL_INTERVENTION_REQUIRED
```

Classification supports:

- retry decisions
- alerts
- dashboards
- reporting
- remediation

---

# 53. Parallel Steps

A saga may execute independent steps in parallel.

Example:

```text
Order validated

├── Reserve inventory
└── Validate fraud rules
```

The orchestrator must define:

- join condition
- failure behavior
- timeout behavior
- compensation
- partial completion
- concurrency control

---

# 54. Join Semantics

Parallel branches may use:

- all-success
- first-success
- quorum
- optional branch
- best-effort branch

Example:

```text
Inventory reserved

AND

Payment authorized

↓

Confirm order
```

Join semantics must be explicit and tested.

---

# 55. Branching

A saga may branch based on business data.

Example:

```text
Order type = STANDARD
    → normal fulfillment

Order type = PREORDER
    → preorder workflow
```

Branch decisions must remain deterministic from persisted saga state.

---

# 56. Dynamic Workflow Changes

An in-progress saga must not unexpectedly change behavior merely because the application version changed.

Strategies include:

- saga definition version
- persisted workflow version
- backward-compatible transition handlers
- versioned command and event contracts

---

# 57. Saga Definition Version

Each saga instance should record the saga-definition version.

Example:

```text
sagaDefinitionVersion = 1
```

This supports:

- rolling deployments
- workflow evolution
- incident diagnosis
- compatibility
- controlled migration

---

# 58. Workflow Evolution

Changes must distinguish between:

- new saga instances
- existing saga instances
- backward-compatible event handling
- deprecated participants
- new compensation logic
- changed timeout policies

Existing in-flight sagas must remain processable.

---

# 59. Rolling Deployment

During rolling deployment, multiple application versions may process saga messages.

Therefore:

- message contracts must remain compatible
- state transitions must tolerate version overlap
- new mandatory fields must not appear without migration strategy
- database schema changes must be backward compatible
- saga-definition version must be respected

---

# 60. Database Schema

A conceptual saga table may be:

```sql
CREATE TABLE saga_instance (
    id UUID PRIMARY KEY,
    saga_type VARCHAR(100) NOT NULL,
    saga_definition_version INTEGER NOT NULL,
    aggregate_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(100),
    state JSONB NOT NULL,
    version BIGINT NOT NULL,
    deadline_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    last_error VARCHAR(2000),
    trace_id UUID
);
```

The exact schema must evolve through new Flyway migrations.

Applied migrations must never be modified.

---

# 61. Typed State Versus JSONB

Typed columns should be used for:

- identifiers
- status
- version
- deadlines
- timestamps
- indexed query fields
- operational filtering

JSONB may be used for workflow-specific state when:

- schema remains controlled
- serialization is versioned
- query requirements are limited
- validation exists

Critical operational fields must not be hidden only inside JSONB.

---

# 62. Saga History

A separate history table may record transitions.

Example:

```sql
CREATE TABLE saga_transition_history (
    id UUID PRIMARY KEY,
    saga_id UUID NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    trigger_type VARCHAR(150) NOT NULL,
    trigger_id UUID,
    occurred_at TIMESTAMPTZ NOT NULL,
    details JSONB
);
```

Benefits include:

- auditability
- incident analysis
- workflow reconstruction
- manual recovery
- compliance evidence

---

# 63. History Immutability

Saga transition history should be append-only.

Corrections should be represented through new records rather than mutation of historical facts.

Sensitive data must be minimized.

---

# 64. Message Ordering

Kafka ordering is partition-specific.

Saga messages should use a stable key such as:

```text
sagaId
```

or, where appropriate:

```text
aggregateId
```

The selected key must support required workflow ordering.

---

# 65. Out-of-Order Messages

The orchestrator must tolerate or safely reject out-of-order messages.

Possible controls include:

- saga version
- expected current step
- command ID correlation
- aggregate version
- transition guards
- deferred processing
- reconciliation

A message must not advance a saga from an invalid state.

---

# 66. Duplicate Responses

Duplicate participant responses are expected.

The orchestrator must detect them through:

- event ID
- command ID
- saga state
- processed-message record
- transition history

Duplicates should result in no additional business transition.

---

# 67. Unknown Saga

A message may reference an unknown saga due to:

- delayed delivery
- retention mismatch
- incorrect producer
- deleted data
- environment misconfiguration
- contract defect

Unknown-saga handling must be explicit.

Possible actions include:

- dead-letter
- retry for a bounded time
- operational alert
- reconciliation
- controlled ignore for known obsolete messages

---

# 68. Unknown Event Type

Unsupported event types or versions must not be silently accepted.

They should be:

- classified
- logged safely
- moved to dead-letter or failure handling
- alerted when business-critical
- preserved for diagnosis

---

# 69. Manual Intervention

Some sagas require manual review.

Examples:

- compensation repeatedly failed
- external settlement ambiguity
- conflicting participant responses
- irreversible step partially completed
- regulatory hold
- missing historical data

The saga must enter an explicit state such as:

```text
MANUAL_REVIEW
```

---

# 70. Manual Recovery

Manual recovery actions must be:

- authorized
- auditable
- idempotent
- limited in scope
- validated against current saga state
- documented
- observable

Possible actions include:

- retry step
- retry compensation
- mark reconciled
- continue workflow
- cancel workflow
- attach operator note

Direct database updates are prohibited except under an approved emergency procedure.

---

# 71. Administrative API

A protected operational API may expose:

- saga status
- transition history
- current step
- retry eligibility
- failure classification
- manual-recovery commands

It must not expose sensitive payloads unnecessarily.

---

# 72. Saga Query API

Business-facing status queries may expose simplified state.

Example:

```json
{
  "orderId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "workflowStatus": "PENDING_PAYMENT",
  "startedAt": "2026-07-23T18:00:00Z",
  "updatedAt": "2026-07-23T18:00:12Z"
}
```

Internal compensation and infrastructure details should remain restricted unless operationally required.

---

# 73. Cancellation

Saga cancellation may be:

- business-initiated
- timeout-initiated
- operator-initiated
- participant-initiated

Cancellation must define:

- allowed states
- compensations
- irreversible-step behavior
- concurrent response handling
- terminal status
- audit trail

---

# 74. Cancellation Race

A cancellation request may race with normal completion.

Example:

```text
CancelOrder command

and

PaymentAuthorized event
```

The state machine and optimistic locking must produce one valid result.

The system must not rely on arrival order alone.

---

# 75. External Services

A saga participant may call an external system.

The participant remains responsible for:

- timeout
- retry
- idempotency key
- response persistence
- error classification
- reconciliation
- outbox publication

The orchestrator should not directly own another bounded context's external integration.

---

# 76. External Idempotency Key

Where supported, external operations should receive a stable idempotency key derived from:

- command ID
- saga ID
- business-operation ID

The key must remain stable across retries.

---

# 77. Reconciliation

Some ambiguous outcomes require reconciliation.

Example:

```text
Payment request timed out

Unknown whether provider authorized
```

A reconciliation process may:

- query external provider
- inspect participant state
- resolve saga outcome
- issue compensation
- move to manual review

Reconciliation must be explicit and observable.

---

# 78. Eventual Consistency in APIs

Synchronous API responses must not imply that the full distributed workflow completed when only the initial local transaction committed.

A typical response may be:

```text
202 Accepted
```

with:

- order ID
- saga ID
- current status
- status-query location

---

# 79. API Response Model

Example:

```json
{
  "orderId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "sagaId": "e3fc9024-129f-48be-a252-cf65454fb782",
  "status": "PENDING_INVENTORY"
}
```

The API must distinguish:

- request accepted
- workflow completed
- workflow failed
- workflow pending

---

# 80. Client Polling

Clients may poll a workflow-status endpoint when appropriate.

Polling must define:

- recommended interval
- terminal states
- authorization
- retention
- response model

Unbounded high-frequency polling should be avoided.

---

# 81. Push Notification

The platform may notify clients through:

- WebSocket
- Server-Sent Events
- webhook
- notification service
- event subscription

Such mechanisms require separate design decisions where introduced.

---

# 82. Observability

Saga observability must connect:

```text
Initial request

↓

Saga creation

↓

Commands

↓

Participant outcomes

↓

Compensation

↓

Terminal state
```

Required correlation includes:

- saga ID
- aggregate ID
- command ID
- event ID
- trace ID
- participant
- step
- attempt

---

# 83. Logging

Saga logs should include:

- saga ID
- saga type
- aggregate ID
- current step
- previous status
- new status
- trigger type
- trigger ID
- attempt
- elapsed time
- failure category
- result

Logs must not include:

- access tokens
- secrets
- full sensitive payloads
- uncontrolled stack traces
- payment credentials

---

# 84. Metrics

Recommended metrics include:

```text
saga.started

saga.completed

saga.cancelled

saga.failed

saga.compensating

saga.manual_review

saga.duration

saga.step.duration

saga.step.retry

saga.timeout

saga.compensation.failure

saga.in_progress
```

Metric labels must remain bounded.

---

# 85. Recommended Metric Labels

Appropriate labels may include:

- saga type
- step
- result
- failure category
- participant
- environment

Do not use:

- saga ID
- order ID
- customer ID
- command ID
- event ID

as metric labels.

---

# 86. Alerts

Alerts should cover:

- saga failure rate
- compensation failure
- manual-review growth
- oldest in-progress saga
- timeout rate
- stuck saga count
- repeated participant failure
- saga-duration SLO breach
- unexpected transition errors
- unknown-saga messages

---

# 87. Saga Duration SLO

Each saga type should define service-level objectives.

Examples:

```text
P95 order confirmation within 30 seconds

P99 order confirmation within 2 minutes
```

SLOs must account for:

- participant latency
- retry policy
- timeout
- compensation
- business criticality

---

# 88. Stuck Saga Detection

A saga is potentially stuck when:

- no transition occurs beyond an expected interval
- deadline has passed
- current step has no active retry
- participant response is missing
- compensation remains incomplete
- status is inconsistent with outbox activity

A scheduled detector should identify and expose these cases.

---

# 89. Health Checks

Application health should distinguish:

- orchestrator process liveness
- Kafka connectivity
- database connectivity
- timeout processor activity
- outbox backlog
- stuck saga condition

A single failed saga must not make the process unhealthy.

Systemic workflow failure should affect readiness or operational status according to policy.

---

# 90. Security

Saga messages and state must follow least privilege.

Security requirements include:

- authenticated Kafka clients
- topic-level authorization
- encrypted transport
- protected operational APIs
- controlled replay
- controlled manual intervention
- sensitive-data minimization
- audit trails

---

# 91. Sensitive Data

Saga state and messages must contain only necessary business data.

Avoid:

- credentials
- tokens
- full payment data
- unnecessary personal data
- confidential provider responses
- raw stack traces

Identifiers are preferred when full data is unnecessary.

---

# 92. Authorization

Business cancellation or recovery commands must validate authorization.

An internal event is not automatically trusted merely because it arrived through Kafka.

Consumer identity, topic ownership and contract validity still matter.

---

# 93. Data Retention

Completed saga state must have a defined retention policy.

Retention must consider:

- incident investigation
- business audit
- legal requirements
- replay and reconciliation
- storage cost
- sensitive-data policy

---

# 94. Archival

Long-term history may move to:

- archival tables
- analytical storage
- object storage
- audit platform

Archival must preserve:

- saga ID
- workflow type
- terminal result
- key transitions
- timestamps
- correlation identifiers

---

# 95. Cleanup

Cleanup must be incremental and observable.

Do not delete large saga volumes in one transaction.

Terminal sagas under active audit, investigation or reconciliation must not be removed.

---

# 96. Partitioning

Database partitioning may be considered for very high saga volume.

Possible strategies include:

- completion month
- start month
- saga type plus time
- active versus archived storage

Partitioning requires separate review.

---

# 97. Horizontal Scaling

Multiple orchestrator instances may run concurrently.

Concurrency safety must use:

- Kafka consumer-group partitioning
- optimistic locking
- idempotency
- database row locking where appropriate
- stable message keys

A single-instance assumption is prohibited unless enforced and documented.

---

# 98. Database Connections

Saga processing concurrency must remain aligned with:

- database connection pool
- Kafka partitions
- participant capacity
- outbox dispatcher throughput
- timeout processor throughput

Unbounded concurrency is prohibited.

---

# 99. Virtual Threads

Virtual threads may be used for blocking orchestration or participant work when justified.

They do not remove limits imposed by:

- database connections
- external APIs
- Kafka partitions
- rate limits
- memory
- downstream throughput

Concurrency must remain bounded.

---

# 100. Graceful Shutdown

The orchestrator must shut down gracefully.

Shutdown should:

- stop accepting new Kafka records
- complete bounded active transactions
- commit safe offsets
- stop timeout claims
- stop claiming new work
- close producers and consumers
- release resources
- respect Kubernetes termination deadlines

In-flight saga state must remain recoverable.

---

# 101. Disaster Recovery

Disaster recovery must preserve:

- saga state
- outbox state
- inbox or processed-message state
- event IDs
- command IDs
- transition history
- deadlines

After restore, duplicate messages may occur.

Idempotency remains mandatory.

---

# 102. Multi-Region

Active-active saga orchestration across regions requires separate design analysis.

Concerns include:

- saga ownership
- duplicate command emission
- global ordering
- database write authority
- Kafka replication
- regional failover
- clock behavior
- external provider affinity

The initial architecture assumes one authoritative region per saga instance.

---

# 103. Testing Strategy

Testing must cover:

- successful workflow
- every business rejection path
- every compensation path
- timeout behavior
- retry behavior
- duplicate commands
- duplicate responses
- late responses
- out-of-order responses
- concurrent transitions
- optimistic-lock conflicts
- participant failure
- compensation failure
- manual-review transition
- workflow-version compatibility
- restart recovery
- rolling deployment compatibility

---

# 104. Unit Tests

Unit tests should validate:

- state-transition rules
- guards
- next-command selection
- compensation selection
- timeout decisions
- failure classification
- retry calculation
- terminal-state behavior
- late-message behavior

Pure workflow logic should not require Spring or Kafka.

---

# 105. State Machine Tests

Every state must have tests covering:

- allowed transitions
- rejected transitions
- terminal behavior
- duplicate trigger behavior
- timeout trigger
- compensation trigger
- cancellation trigger

Transition tables should be testable directly.

---

# 106. Persistence Integration Tests

PostgreSQL Testcontainers should validate:

- saga creation
- optimistic locking
- atomic saga-state and outbox persistence
- timeout query
- `SKIP LOCKED`
- transition history
- concurrent update behavior
- cleanup
- migration compatibility

H2 must not substitute for PostgreSQL-specific behavior.

---

# 107. Kafka Integration Tests

Kafka integration tests should validate:

- command publication
- response consumption
- message keys
- headers
- correlation metadata
- duplicate delivery
- consumer-group behavior
- retry topics where used
- dead-letter behavior
- restart recovery

---

# 108. End-to-End Tests

End-to-end tests should execute complete scenarios.

Example success:

```text
Create order

Reserve inventory

Authorize payment

Confirm order
```

Example failure:

```text
Create order

Reserve inventory

Reject payment

Release inventory

Cancel order
```

---

# 109. Failure-Injection Tests

Failure injection should cover:

- orchestrator crash after state update
- orchestrator crash before command publication
- participant crash after local commit
- duplicate Kafka delivery
- delayed response
- lost response
- timeout race
- compensation failure
- database outage
- Kafka outage
- optimistic-lock conflict

---

# 110. Crash Window Validation

A required test should prove:

```text
Saga state advances

+

Next command outbox record persists

in one transaction
```

If the transaction rolls back, neither change may persist.

---

# 111. Idempotency Tests

Participant tests must prove:

- duplicate command does not repeat side effects
- duplicate compensation does not repeat side effects
- duplicate response does not advance saga twice
- previous result can be returned safely
- processed-message records are durable

---

# 112. Timeout Tests

Timeout tests must avoid uncontrolled real-time waiting.

Use:

- controlled clock
- persisted deadlines
- deterministic scheduler invocation
- Testcontainers where query behavior matters

Avoid `Thread.sleep`.

---

# 113. Concurrency Tests

Concurrency tests should verify:

- timeout and response race
- cancellation and completion race
- duplicate response race
- parallel branch completion
- optimistic-lock retry
- no invalid double transition

---

# 114. Contract Tests

Commands and events must have compatibility tests covering:

- required fields
- optional fields
- version
- identifiers
- enum values
- unknown fields
- timestamp format
- correlation metadata

---

# 115. Architecture Tests

Architecture tests must enforce:

- Domain does not depend on Kafka
- Domain does not depend on saga persistence adapters
- listeners invoke Application services
- orchestrator business rules do not live in Kafka listeners
- JPA entities are not public event contracts
- participants do not access other bounded contexts' repositories
- direct broker publication does not replace the outbox
- controllers do not coordinate distributed workflows directly

---

# 116. Operational Runbook

The saga runbook must include:

- locating a saga
- reading current state
- reading transition history
- checking pending commands
- checking participant responses
- checking timeouts
- retrying a step
- retrying compensation
- moving to manual review
- reconciling ambiguous external state
- cancelling a saga
- handling unknown-saga messages
- handling stuck workflows
- escalating participant outages

---

# 117. Example Order Saga

A typical order-confirmation saga may be:

```text
STARTED

↓

ReserveInventory

↓

InventoryReserved

↓

AuthorizePayment

↓

PaymentAuthorized

↓

ConfirmOrder

↓

COMPLETED
```

Failure path:

```text
AuthorizePayment

↓

PaymentRejected

↓

ReleaseInventory

↓

InventoryReleased

↓

CancelOrder

↓

CANCELLED
```

---

# 118. Example Order Saga States

Possible states include:

```text
STARTED

PENDING_INVENTORY

PENDING_PAYMENT

PENDING_CONFIRMATION

COMPENSATING_PAYMENT

COMPENSATING_INVENTORY

COMPLETED

CANCELLED

FAILED

MANUAL_REVIEW
```

The actual state model must reflect domain language.

---

# 119. Example Command Envelope

```json
{
  "commandId": "a72014fc-e099-4a42-9432-2cd523c50f60",
  "commandType": "RESERVE_INVENTORY",
  "commandVersion": 1,
  "sagaId": "e3fc9024-129f-48be-a252-cf65454fb782",
  "aggregateId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "correlationId": "e3fc9024-129f-48be-a252-cf65454fb782",
  "causationId": "90ddf491-d91c-4d87-b00f-dc17d28c129d",
  "occurredAt": "2026-07-23T18:00:00Z",
  "payload": {
    "orderId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
    "items": [
      {
        "productId": "3de60eb5-e0fd-4ae4-85f0-c9a86528ce77",
        "quantity": 2
      }
    ]
  }
}
```

---

# 120. Example Outcome Event

```json
{
  "eventId": "5f68fb79-6f85-498f-8366-b052eefdc62f",
  "eventType": "INVENTORY_RESERVED",
  "eventVersion": 1,
  "sagaId": "e3fc9024-129f-48be-a252-cf65454fb782",
  "commandId": "a72014fc-e099-4a42-9432-2cd523c50f60",
  "aggregateId": "08ab5af0-a5fb-4b90-a149-5d77057c663d",
  "occurredAt": "2026-07-23T18:00:03Z",
  "payload": {
    "reservationId": "951e3647-0ac4-427d-a75a-6cd2579b576f"
  }
}
```

---

# 121. Anti-Patterns

The following are prohibited:

- distributed two-phase commit for normal business workflows
- direct updates to another bounded context's database
- hiding complex workflows in uncontrolled choreography
- implementing domain rules inside the orchestrator
- implementing workflow logic inside Kafka listeners
- assuming exactly-once delivery
- omitting participant idempotency
- generating new command IDs on retry
- generating new event IDs on broker retry
- advancing saga state without atomically persisting the next command
- waiting indefinitely without timeout
- treating timeout as proof of participant failure
- assuming every action is reversible
- retrying business rejection
- retrying poison messages forever
- mutating historical saga records silently
- using in-memory-only saga state
- using in-memory-only timers
- manually changing saga tables as normal recovery
- ignoring late responses
- accepting invalid state transitions
- using one generic `ERROR` state for every failure
- logging full sensitive payloads
- using unbounded workflow concurrency
- modifying an applied Flyway migration

---

# 122. Positive Consequences

The decision provides:

- explicit distributed-workflow coordination
- bounded-context autonomy
- avoidance of distributed transactions
- reliable recovery
- clear compensation
- timeout support
- retry support
- explicit workflow state
- operational visibility
- auditability
- testable state transitions
- support for long-running processes
- compatibility with Kafka
- compatibility with Transactional Outbox
- horizontal scalability
- controlled manual intervention
- clearer incident diagnosis

---

# 123. Negative Consequences

The decision introduces:

- eventual consistency
- workflow-state persistence
- compensation complexity
- additional commands and events
- more operational monitoring
- more integration tests
- timeout management
- manual-recovery procedures
- message-version governance
- duplicate-processing defenses
- workflow-evolution complexity
- increased implementation effort

These costs are accepted because distributed business workflows require explicit coordination and recovery semantics.

---

# 124. Neutral Consequences

The decision also means:

- not every API request completes the entire business workflow synchronously
- intermediate states become part of the business model
- compensation is a first-class business concern
- some failures require forward recovery rather than rollback
- workflow ownership must be assigned explicitly
- clients may need status polling
- operational teams must understand saga state
- message ordering remains partition-specific
- duplicate and late messages remain normal distributed-system behavior

---

# 125. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate command repeats side effect | High | High | Require durable participant idempotency |
| Duplicate response advances saga twice | High | High | Persist processed messages and validate transitions |
| Timeout races with successful response | High | Medium | Use optimistic locking and late-response policy |
| Compensation fails | High | Medium | Retry, alert and support manual review |
| Workflow becomes too centralized | High | Medium | Keep participant domain logic in bounded contexts |
| Choreography hides business process | High | Medium | Require orchestration for complex workflows |
| Saga state becomes inconsistent | High | Low | Atomically persist state and outbox command |
| Event contract changes break in-flight sagas | High | Medium | Version saga definitions and contracts |
| Stuck sagas go unnoticed | High | Medium | Monitor age, deadlines and transition activity |
| Business rejection is retried incorrectly | Medium | Medium | Explicit failure classification |
| Manual recovery causes invalid transition | High | Low | Protected idempotent recovery commands |
| Out-of-order response corrupts flow | High | Medium | Validate step, command ID and saga version |
| Irreversible action occurs too early | High | Low | Identify pivot and order steps carefully |
| Nested retries create storms | Medium | Medium | Coordinate retry layers and use bounded backoff |
| Saga table grows indefinitely | Medium | Medium | Define retention and archival |
| Sensitive workflow data is exposed | High | Low | Minimize payload and restrict operational access |
| One orchestrator instance becomes bottleneck | High | Low | Use Kafka partitioning and horizontal scaling |
| Rolling deployment breaks old sagas | High | Medium | Persist saga-definition version |
| Unknown saga messages are discarded | High | Low | Dead-letter and alert |
| Direct database intervention damages history | High | Low | Use controlled recovery APIs and audit trail |

---

# 126. Implementation Guidance

The following rules are mandatory:

1. Distributed business workflows must not use two-phase commit.
2. Complex workflows must use an explicit saga orchestrator.
3. Simple choreography requires documented justification.
4. Every saga must have one authoritative owner.
5. Every saga instance must have a stable unique saga ID.
6. Saga state must be persisted durably.
7. Saga transitions must be explicit.
8. Complex sagas must use a state-machine model.
9. Every command must have a stable unique command ID.
10. Every event must have a stable unique event ID.
11. Saga participants must be idempotent.
12. Compensating actions must be idempotent.
13. Saga state and outgoing commands must persist atomically through the outbox.
14. Participant state and outcome events must persist atomically through the outbox.
15. Asynchronous steps must define timeouts.
16. Timeouts must survive application restart.
17. Business rejection must be distinguished from technical failure.
18. Retries must be bounded and delayed.
19. Compensation failure must remain visible.
20. Late responses must have explicit handling.
21. Invalid transitions must be rejected.
22. Optimistic locking or equivalent concurrency control is mandatory.
23. In-flight saga compatibility must be preserved during deployment.
24. Saga-definition versions must be persisted when workflow evolution requires it.
25. Operational metrics and alerts are mandatory.
26. Manual recovery must be authorized and audited.
27. Sensitive data must be minimized.
28. Saga migrations must use new Flyway versions.
29. Applied Flyway migrations must never be modified.
30. End-to-end failure and compensation tests are mandatory.

---

# 127. Validation

The decision will be validated through:

- workflow state-machine tests
- participant idempotency tests
- compensation tests
- timeout tests
- retry tests
- duplicate-message tests
- late-response tests
- out-of-order-message tests
- concurrent-transition tests
- PostgreSQL Testcontainers
- Kafka integration tests
- end-to-end saga tests
- failure injection
- restart recovery tests
- rolling deployment compatibility tests
- operational dashboard review
- runbook exercises
- manual-recovery rehearsal
- production-readiness review

---

# 128. Success Criteria

The decision is successful when:

- distributed workflows complete without cross-service transactions
- saga state remains recoverable after crashes
- duplicate messages do not create duplicate business effects
- failed workflows trigger correct compensation
- compensation failures remain visible
- timeouts are processed reliably
- late responses do not corrupt workflow state
- invalid transitions are rejected
- complex workflows are operationally understandable
- stuck sagas are detected
- manual recovery is controlled and auditable
- in-flight sagas survive rolling deployments
- participant autonomy is preserved
- Kafka-specific code remains outside the Domain layer
- workflow duration remains within defined SLOs
- distributed failures can be diagnosed through saga correlation

---

# 129. Alternatives Rejected

## 129.1 Distributed Two-Phase Commit

Rejected because it would reduce availability, increase coupling and conflict with independently deployable bounded contexts.

---

## 129.2 Uncontrolled Choreography for All Workflows

Rejected because complex workflows would become hidden, difficult to compensate and difficult to operate.

Choreography remains acceptable for simple event propagation with explicit review.

---

## 129.3 Synchronous Service Chain

Rejected because long request chains introduce temporal coupling, cascading failure and poor recoverability.

---

## 129.4 External Workflow Engine as the Initial Default

Rejected for the initial implementation because it introduces additional infrastructure and operational complexity before the workflow requirements justify it.

An external workflow engine may be adopted later through a separate ADR if:

- workflow volume grows substantially
- timers become highly complex
- long-running workflow history becomes difficult to manage
- business users require visual workflow operations
- custom orchestration cost becomes excessive

---

# 130. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Apache Kafka for Integration Events
- ADR-011: Adopt OpenAPI-First API Design
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Use OpenTelemetry for Distributed Tracing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-017: Use Optimistic Locking for Aggregate Concurrency
- ADR-018: Version Integration-Event Contracts

---

# 131. References

- Saga Pattern
- Microservices Patterns
- Enterprise Integration Patterns
- Domain-Driven Design
- Apache Kafka Documentation
- Transactional Outbox Pattern
- Spring for Apache Kafka Documentation
- PostgreSQL Documentation
- Enterprise Order Platform Messaging Architecture
- Enterprise Order Platform Domain Events
- Enterprise Order Platform Idempotency Guide
- Enterprise Order Platform Resilience Guide
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Apache Kafka for Integration Events

---

# 132. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial distributed workflow and compensation baseline |

---

# 133. Decision Summary

The Enterprise Order Platform adopts the Saga pattern for distributed business workflows.

Complex workflows will use explicit orchestration based on:

```text
Persisted saga state

+

Local participant transactions

+

Transactional Outbox

+

At-least-once messaging

+

Idempotent commands and events

+

Compensating actions
```

The platform accepts:

```text
Eventual consistency

Intermediate workflow states

Possible duplicate delivery

Possible late responses

Possible compensation failure
```

Therefore:

```text
Saga state must be durable

Transitions must be explicit

Timeouts must be persisted

Participants must be idempotent

Compensations must be idempotent

Failures must remain observable

Manual recovery must be controlled
```

This decision establishes a resilient and operationally explicit model for coordinating long-running business processes across autonomous bounded contexts without distributed transactions.
