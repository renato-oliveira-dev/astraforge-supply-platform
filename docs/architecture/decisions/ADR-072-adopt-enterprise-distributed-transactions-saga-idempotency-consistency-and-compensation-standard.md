# ADR-072: Adopt Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-072 |
| Title | Adopt Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Distributed Transactions, Saga, Idempotency, Consistency, Compensation |
| Related Work Items | Transactional Outbox, SQS, SQS, PostgreSQL, Microservices, Workflow |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Distributed enterprise workflows often span multiple services.

A simplified order flow may involve:

```text
CART
  |
  v
ORDERS
  |
  +--> CUSTOMERS
  |
  +--> PRODUCTS
  |
  +--> WORKFLOW
  |
  +--> NOTIFICATIONS
  |
  v
MESSAGING
```

A single business operation may therefore affect:

```text
Multiple Databases

Multiple Services

Multiple Queues

External APIs

Caches

Background Workers
```

Unlike a traditional monolith, these operations cannot normally be protected by one local ACID transaction.

---

# 2. The Fundamental Distributed Transaction Problem

Inside one PostgreSQL database:

```text
BEGIN

UPDATE A

UPDATE B

INSERT C

COMMIT
```

can provide atomicity.

Across microservices:

```text
SERVICE A DB

SERVICE B DB

SERVICE C DB
```

there is no single local transaction covering all three.

A failure may therefore occur after some effects have already committed.

Example:

```text
CREATE ORDER
    |
    v
ORDER COMMITTED
    |
    v
RESERVE INVENTORY
    |
    v
INVENTORY COMMITTED
    |
    v
START APPROVAL
    |
    X
WORKFLOW FAILURE
```

The system must explicitly define what happens next.

---

# 3. Problem Statement

The organization requires standards covering:

- distributed transactions
- XA
- two-phase commit
- Saga
- orchestration
- choreography
- transactional outbox
- inbox pattern
- idempotency
- duplicate messages
- duplicate HTTP requests
- consistency
- eventual consistency
- compensation
- retries
- ordering
- optimistic locking
- concurrency
- failure recovery
- intermediate states
- timeout handling
- replay
- reconciliation
- exactly-once claims
- effectively-once processing

---

# 4. Decision Drivers

Primary drivers are:

1. reliability
2. consistency
3. recoverability
4. availability
5. decoupling
6. fault isolation
7. scalability
8. observability
9. operational simplicity
10. duplicate safety
11. retry safety
12. business correctness

---

# 5. Decision

Distributed business workflows MUST NOT rely on implicit cross-service atomicity.

The preferred architecture is:

```text
LOCAL ACID TRANSACTIONS
        +
TRANSACTIONAL OUTBOX
        +
IDEMPOTENT CONSUMERS
        +
SAGA / WORKFLOW
        +
COMPENSATION WHERE REQUIRED
        +
RECONCILIATION
```

rather than global distributed locking.

---

# 6. Fundamental Principle

```text
Within one service:

use ACID.

Across services:

assume partial failure.

Design explicitly for:

retries,
duplicates,
intermediate states,
and recovery.
```

---

# 7. XA / Two-Phase Commit

XA / Two-Phase Commit MUST NOT be the default distributed transaction strategy between microservices.

---

# 8. Why 2PC Is Rejected

2PC introduces strong coordination across distributed participants.

Conceptually:

```text
COORDINATOR
     |
     +--> SERVICE A
     |
     +--> SERVICE B
     |
     +--> SERVICE C
```

Each participant may hold resources while waiting for the global decision.

---

# 9. 2PC Problems

Potential problems include:

```text
Reduced Availability

Coordinator Dependency

Long-Lived Locks

Poor Failure Isolation

Operational Complexity

Cross-Technology Limitations

Scalability Constraints
```

---

# 10. Local Transaction

Every service SHOULD own its local transaction boundary.

---

# 11. Database Ownership

One service SHOULD NOT directly participate in another service's database transaction.

---

# 12. Shared Transaction Manager

A shared cross-service transaction manager is strongly discouraged.

---

# 13. Eventual Consistency

Cross-service workflows SHOULD generally use eventual consistency.

---

# 14. Eventual Consistency Meaning

Eventual consistency does NOT mean:

```text
"the data may be wrong forever"
```

It means:

```text
temporary intermediate states
may exist while the distributed
workflow converges toward a valid state.
```

---

# 15. Business Consistency

The target is business consistency, not immediate global database consistency.

---

# 16. Intermediate State

Distributed workflows MUST explicitly model intermediate states.

Examples:

```text
PENDING

PROCESSING

WAITING_APPROVAL

COMPENSATING

FAILED

COMPLETED
```

---

# 17. Hidden Intermediate State

A service MUST NOT pretend an operation is complete when required distributed steps are still pending.

---

# 18. Saga

A Saga coordinates multiple local transactions forming one distributed business workflow.

---

# 19. Saga Structure

Conceptually:

```text
STEP 1
 LOCAL TX
    |
    v
STEP 2
 LOCAL TX
    |
    v
STEP 3
 LOCAL TX
```

If a later step fails:

```text
COMPENSATE PREVIOUS EFFECTS
```

when required by business semantics.

---

# 20. Saga Is Not Rollback

Saga compensation is not equivalent to database rollback.

---

# 21. Compensation

Compensation is a new business operation that semantically reverses or neutralizes a prior effect.

Example:

```text
RESERVE STOCK
     |
     v
LATER FAILURE
     |
     v
RELEASE STOCK
```

---

# 22. Compensation Limit

Some effects cannot be perfectly reversed.

Examples:

```text
Email already sent

External partner notified

Payment settlement completed

Physical shipment started
```

---

# 23. Compensatable Step

Every saga step SHOULD define whether it is:

```text
REVERSIBLE

COMPENSATABLE

IRREVERSIBLE
```

---

# 24. Irreversible Operation

Irreversible operations SHOULD normally occur as late as practical in the workflow.

---

# 25. Pivot Transaction

A Saga MAY identify a pivot step after which compensation semantics materially change.

---

# 26. Saga Orchestration

In orchestration, one explicit coordinator determines workflow progression.

```text
SAGA ORCHESTRATOR
       |
       +--> COMMAND A
       |
       +--> COMMAND B
       |
       +--> COMMAND C
```

---

# 27. Orchestrator Responsibility

The orchestrator SHOULD own:

```text
Workflow State

Step Progression

Failure Classification

Compensation

Timeouts

Recovery
```

---

# 28. Orchestrator Business Ownership

The orchestrator SHOULD belong to the bounded context that owns the business process.

---

# 29. God Orchestrator

A Saga orchestrator MUST NOT become a generic coordinator for unrelated workflows.

---

# 30. Saga Choreography

In choreography, services react to events without one central workflow controller.

```text
SERVICE A
  |
  v
EVENT A
  |
  v
SERVICE B
  |
  v
EVENT B
  |
  v
SERVICE C
```

---

# 31. Choreography Advantages

Advantages include:

```text
Lower Central Coupling

Natural Event-Driven Flow

Independent Participants
```

---

# 32. Choreography Risks

Risks include:

```text
Implicit Workflow

Difficult Global Visibility

Event Cycles

Complex Compensation

Harder Debugging
```

---

# 33. Orchestration vs Choreography

Decision guideline:

```text
COMPLEX MULTI-STEP
BUSINESS WORKFLOW?
        |
     +--+--+
     |     |
    YES    NO
     |     |
     v     v
ORCHESTRATION MAY
PREFERRED      USE
              CHOREOGRAPHY
```

---

# 34. Explicit Workflow

Complex business approval/order lifecycles SHOULD prefer explicit orchestration over accidental chains of unrelated event listeners.

---

# 35. Choreography Limit

Choreography SHOULD NOT result in:

```text
A -> B -> C -> D -> E -> F
```

with no component able to explain the complete business process.

---

# 36. Transactional Outbox

Transactional Outbox is the standard mechanism for coordinating local database state and event publication.

---

# 37. Dual Write Problem

This is unsafe:

```text
UPDATE DATABASE
      |
      v
COMMIT
      |
      v
PUBLISH EVENT
      |
      X
PROCESS CRASH
```

The database changed, but the event was lost.

---

# 38. Reverse Dual Write

This is also unsafe:

```text
PUBLISH EVENT
      |
      v
UPDATE DATABASE
      |
      X
DATABASE FAILURE
```

Consumers may observe an event for state that never committed.

---

# 39. Outbox Flow

Use:

```text
LOCAL TRANSACTION
      |
      +--> UPDATE BUSINESS DATA
      |
      +--> INSERT OUTBOX EVENT
      |
      v
COMMIT
```

Then:

```text
OUTBOX DISPATCHER
      |
      v
BROKER
```

---

# 40. Atomicity Boundary

Business state and Outbox record MUST be committed atomically within the same local database transaction.

---

# 41. Outbox Dispatcher

The dispatcher MUST support:

```text
Retry

Batching

Attempt Count

next_attempt_at

Failure Recording

Sent State
```

---

# 42. Outbox Delivery

Outbox delivery MUST assume possible duplicate publication.

---

# 43. Sent Marking Race

Consider:

```text
SEND EVENT
    |
    v
BROKER ACCEPTS
    |
    X
PROCESS CRASHES
BEFORE OUTBOX MARKED SENT
```

After restart, the event may be sent again.

Therefore consumers MUST be idempotent.

---

# 44. Exactly Once

Distributed systems SHOULD NOT casually claim end-to-end:

```text
EXACTLY ONCE
```

processing.

---

# 45. Broker Exactly Once

A messaging technology may provide exactly-once guarantees within a specific bounded scope.

This does not automatically provide exactly-once business effects across:

```text
Broker

Database

External APIs

Other Services
```

---

# 46. Effectively Once

The preferred business goal is often:

```text
AT-LEAST-ONCE DELIVERY
        +
IDEMPOTENT PROCESSING
        =
EFFECTIVELY-ONCE BUSINESS EFFECT
```

---

# 47. At-Least-Once

All asynchronous consumers MUST assume duplicate delivery unless the infrastructure contract explicitly proves otherwise.

---

# 48. Duplicate Is Normal

Duplicate message processing is not an exceptional edge case.

It is a normal distributed-systems condition.

---

# 49. Idempotency

An operation is idempotent when repeated execution with the same logical request produces no additional unintended effect.

---

# 50. Idempotent Example

Conceptually:

```text
APPROVE ORDER X

first call:
PENDING -> APPROVED

duplicate call:
APPROVED -> APPROVED
NO SECOND SIDE EFFECT
```

when that matches business semantics.

---

# 51. Non-Idempotent Example

This is not naturally idempotent:

```text
increment balance by R$ 100
```

because duplicate execution changes the result twice.

---

# 52. Idempotency Key

Mutation APIs with material retry/duplicate risk SHOULD support an idempotency key where appropriate.

---

# 53. HTTP Idempotency

Typical pattern:

```text
POST /orders
Idempotency-Key: abc123
```

---

# 54. Idempotency Record

Server records conceptually:

```text
KEY

REQUEST FINGERPRINT

STATUS

RESULT REFERENCE

CREATED_AT

EXPIRES_AT
```

---

# 55. Same Key Same Request

The same idempotency key with the same logical request SHOULD return the same logical outcome.

---

# 56. Same Key Different Request

Using the same key for a materially different payload MUST be rejected.

---

# 57. Idempotency Fingerprint

The system SHOULD compare a stable request fingerprint where appropriate.

---

# 58. Idempotency Scope

Key scope MUST be defined.

Examples:

```text
Per User

Per Customer

Per Operation

Per Service
```

---

# 59. Key Lifetime

Idempotency keys MUST have a defined retention duration.

---

# 60. Infinite Key Storage

Idempotency records SHOULD NOT accumulate forever without retention policy.

---

# 61. Processing Key State

Concurrent duplicate requests require explicit processing state.

Possible states:

```text
PROCESSING

SUCCEEDED

FAILED
```

---

# 62. Concurrent Duplicate

If two requests with the same key arrive simultaneously:

```text
REQUEST A ----\
               > SAME KEY
REQUEST B ----/
```

only one logical operation SHOULD execute.

---

# 63. Database Constraint

A unique database constraint SHOULD protect idempotency-key uniqueness where appropriate.

---

# 64. Idempotent Consumer

Message consumers SHOULD use event/message identity to prevent duplicate business effects.

---

# 65. Inbox Pattern

Inbox Pattern MAY store consumed message IDs.

Conceptually:

```text
MESSAGE
   |
   v
INBOX LOOKUP
   |
 +--+--+
 |     |
NEW   SEEN
 |     |
 v     v
PROCESS SKIP
 |
 v
STORE MESSAGE ID
```

---

# 66. Inbox Atomicity

Where duplicate detection and local business changes must remain consistent, both SHOULD occur in the same local transaction.

---

# 67. Inbox Key

The key SHOULD use a stable event/message identity.

---

# 68. Event ID

Integration events SHOULD have globally unique stable identifiers.

---

# 69. Event ID Reuse

A producer MUST NOT reuse one event ID for distinct logical events.

---

# 70. Inbox Retention

Inbox records MUST have retention consistent with realistic broker replay/redelivery windows.

---

# 71. Infinite Inbox

Inbox storage MUST NOT grow indefinitely without strategy.

---

# 72. Natural Idempotency

Where business state itself makes processing naturally idempotent, a separate Inbox MAY be unnecessary.

---

# 73. Idempotency Decision

Ask:

```text
CAN THE SAME MESSAGE
ARRIVE TWICE?
      |
     YES
      |
      v
CAN BUSINESS OPERATION
SAFELY REPEAT?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
NATURAL  INBOX /
IDEMPOTENCY DEDUP
MAY SUFFICE
```

---

# 74. Check-Then-Act Race

This is unsafe without concurrency protection:

```text
IF NOT PROCESSED
    PROCESS
    MARK PROCESSED
```

because two workers may both pass the check.

---

# 75. Atomic Duplicate Protection

Use:

```text
Unique Constraint

Atomic Insert

Transactional Lock

Optimistic Lock
```

as appropriate.

---

# 76. Optimistic Locking

Optimistic locking SHOULD protect concurrent modifications where conflicts are expected to be relatively infrequent.

---

# 77. Version Column

JPA aggregates MAY use:

```java
@Version
```

where appropriate.

---

# 78. Lost Update

Optimistic locking helps prevent:

```text
REQUEST A READ VERSION 5

REQUEST B READ VERSION 5

A WRITES VERSION 6

B OVERWRITES A
```

---

# 79. Optimistic Conflict

An optimistic-lock conflict SHOULD be classified explicitly.

---

# 80. Retry Optimistic Lock

Automatic retry of optimistic-lock failures MUST consider business semantics.

---

# 81. Blind Retry

Blindly retrying a state transition may no longer be valid after another transaction changed the resource.

---

# 82. Pessimistic Lock

Pessimistic locking MAY be used when exclusive access is genuinely necessary.

---

# 83. Lock Scope

Distributed workflows SHOULD avoid holding database locks across remote calls.

---

# 84. Remote Call Inside Lock

This is strongly discouraged:

```text
BEGIN TRANSACTION

LOCK ROW

CALL REMOTE SERVICE

WAIT

COMMIT
```

---

# 85. Lock Duration

Locks MUST remain as short as practical.

---

# 86. Business Concurrency

Business concurrency SHOULD be represented through state/version semantics rather than broad locking when possible.

---

# 87. Ordering

Message ordering MUST be treated as scoped rather than global.

---

# 88. SQS Ordering

SQS Standard does not guarantee strict ordering; SQS FIFO ordering is scoped to a MessageGroupId.

---

# 89. Partition Key

Events requiring order for the same aggregate SHOULD use a stable partition key such as:

```text
orderId
```

when SQS is used.

---

# 90. Global Ordering

Global event ordering SHOULD NOT be assumed.

---

# 91. SQS Standard Ordering

Standard SQS queues do not provide strict global ordering.

---

# 92. FIFO Ordering

FIFO queues MAY provide ordered processing within their supported message-group semantics.

---

# 93. Out-of-Order Event

Consumers MUST define behavior when an event arrives out of sequence where such delivery is possible.

---

# 94. Event Version

Events SHOULD carry enough metadata to detect stale ordering where required.

---

# 95. Aggregate Version

An aggregate/event sequence version MAY support ordering decisions.

---

# 96. Stale Event

A stale event SHOULD not overwrite newer state.

---

# 97. Event Replay

Consumers MUST tolerate replay when the architecture permits it.

---

# 98. Replay Safety

Replay requires:

```text
Idempotency

Ordering Awareness

Backward-Compatible Schemas
```

---

# 99. Retry

Retries MUST follow ADR-055.

---

# 100. Retry Prerequisite

A retry is only safe when:

```text
Failure is transient

Operation is idempotent
or
Duplicate effects are otherwise controlled
```

---

# 101. Retry Budget

Retries MUST be bounded.

---

# 102. Retry Amplification

In a call chain:

```text
A retries B 3x

B retries C 3x

C retries D 3x
```

one original request may create:

```text
3 × 3 × 3 = 27
```

downstream attempts.

---

# 103. Retry Ownership

Retry SHOULD normally occur at the boundary best able to classify transient failure.

---

# 104. Duplicate Retry Layers

Multiple independent retry layers SHOULD be avoided unless intentionally budgeted.

---

# 105. Timeout

Saga steps MUST have explicit timeout semantics where waiting indefinitely is unacceptable.

---

# 106. Timeout Is State

A workflow timeout SHOULD become an explicit workflow condition.

---

# 107. Timeout Compensation

A timed-out step MAY require:

```text
RETRY

QUERY STATUS

COMPENSATE

MANUAL REVIEW
```

depending on business semantics.

---

# 108. Unknown Outcome

A timeout does not always mean the remote operation failed.

---

# 109. Ambiguous Result

Example:

```text
SEND PAYMENT REQUEST
        |
        v
REMOTE PROCESSES PAYMENT
        |
        X
NETWORK TIMEOUT
```

The caller does not know whether the payment happened.

---

# 110. Query Before Retry

For non-idempotent external operations, the system SHOULD use:

```text
Operation ID

Idempotency Key

Status Query
```

to resolve ambiguous outcomes before blindly retrying.

---

# 111. External Idempotency

Where external APIs support idempotency keys, integrations SHOULD use them for sensitive mutation operations.

---

# 112. Operation Identifier

External mutations SHOULD carry a stable operation identifier where supported.

---

# 113. Compensation Idempotency

Compensation operations MUST themselves be idempotent.

---

# 114. Duplicate Compensation

A compensation may also be retried or delivered twice.

Therefore:

```text
RELEASE RESERVATION
```

must safely tolerate duplicate execution.

---

# 115. Compensation State

Saga state SHOULD record completed and compensated steps.

---

# 116. Compensation Order

Compensations commonly execute in reverse logical order where dependencies require it.

---

# 117. Reverse Order Not Universal

Compensation order MUST follow business dependencies rather than a mechanical stack rule when semantics differ.

---

# 118. Failed Compensation

Compensation can fail.

This MUST be explicitly supported.

---

# 119. Compensation Retry

Transient compensation failure SHOULD use bounded retry where safe.

---

# 120. Manual Intervention

Persistent compensation failures MAY require operational/manual intervention.

---

# 121. Compensation DLQ

Failed asynchronous compensations MAY use DLQ/recovery queues where appropriate.

---

# 122. Saga State Persistence

Saga state MUST be durable when workflow recovery after process restart is required.

---

# 123. In-Memory Saga

Long-running business workflows MUST NOT rely solely on in-memory coordination.

---

# 124. Workflow Recovery

After service restart, the system MUST be able to determine:

```text
Current Saga State

Completed Steps

Pending Step

Compensation State

Attempts
```

---

# 125. Saga Instance ID

Each workflow instance SHOULD have a unique stable identifier.

---

# 126. Correlation ID

Saga and related events SHOULD preserve correlation identifiers.

---

# 127. Trace ID

Trace identifiers MAY support observability but MUST NOT be the durable business workflow identity.

---

# 128. Workflow Identity

Use:

```text
sagaId / processId / workflowId
```

for durable workflow identity.

---

# 129. Workflow State Machine

Complex Saga orchestration SHOULD use an explicit state machine.

---

# 130. Transition Validation

Invalid workflow transitions MUST be rejected.

---

# 131. Duplicate Command

A duplicate command SHOULD NOT cause the same state transition and side effects repeatedly.

---

# 132. Command Identity

Distributed commands SHOULD have unique command/message IDs where duplicate handling requires them.

---

# 133. Command vs Event

A command requests an action.

An event states that something already happened.

They MUST NOT be conflated.

---

# 134. Command Naming

Commands SHOULD use imperative intent.

Examples:

```text
ReserveInventory

StartApproval

CancelOrder
```

---

# 135. Event Naming

Events SHOULD use past-tense facts.

Examples:

```text
InventoryReserved

ApprovalStarted

OrderCancelled
```

---

# 136. Event Consumer Side Effect

A consumer MUST decide whether its side effect is safely repeatable.

---

# 137. Email Example

Sending an email is normally not naturally idempotent.

---

# 138. Notification Deduplication

Notifications SHOULD use stable notification/event identity to prevent duplicate sends where duplicates are unacceptable.

---

# 139. Payment Example

Payment/financial operations require especially strict idempotency and reconciliation.

---

# 140. Inventory Example

Inventory reservation SHOULD use stable reservation identity.

---

# 141. Reservation

A reservation SHOULD be addressable by a stable reservation or order identifier.

---

# 142. Cancel vs Delete

Compensation SHOULD often use a business operation such as:

```text
CANCEL
```

rather than deleting history.

---

# 143. Audit Trail

Distributed workflow history SHOULD be auditable.

---

# 144. Audit Record

Audit SHOULD capture applicable:

```text
Workflow ID

Step

State Change

Timestamp

Actor

Failure Classification
```

without exposing secrets.

---

# 145. Reconciliation

Distributed workflows MUST have a reconciliation strategy for states that fail to converge automatically.

---

# 146. Reconciliation Purpose

Reconciliation answers:

```text
WHAT SHOULD EXIST?

WHAT ACTUALLY EXISTS?

WHAT DIFFERENCE REMAINS?

HOW DO WE REPAIR IT?
```

---

# 147. Scheduled Reconciliation

Critical workflows SHOULD consider scheduled reconciliation jobs.

---

# 148. Example

```text
ORDERS:
ORDER = APPROVED

WORKFLOW:
PROCESS = MISSING
```

A reconciliation process may detect and repair or escalate the inconsistency.

---

# 149. Reconciliation Is Not Primary Flow

Reconciliation MUST NOT become an excuse for unreliable normal processing.

---

# 150. Repair Action

Repair SHOULD be:

```text
Idempotent

Auditable

Bounded

Observable
```

---

# 151. Manual Reprocessing

Operations MAY provide controlled manual reprocessing for failed workflow instances.

---

# 152. Manual Replay Security

Manual reprocessing MUST require appropriate authorization.

---

# 153. Replay Safety

Manual replay MUST use the same duplicate/idempotency safeguards as automatic processing.

---

# 154. Eventual Consistency UX

User-facing APIs MUST represent eventual consistency honestly.

---

# 155. 202 Accepted

Long-running distributed operations MAY return:

```text
202 Accepted
```

with a status resource.

---

# 156. Premature 200

Do not return:

```text
200 COMPLETED
```

when mandatory distributed processing has only been queued.

---

# 157. Status Endpoint

Long-running workflows SHOULD expose status when users/clients need progress visibility.

Example:

```text
GET /operations/{id}
```

---

# 158. Workflow Status

Possible contract:

```text
PENDING

PROCESSING

COMPLETED

FAILED

COMPENSATING

CANCELLED
```

---

# 159. Polling

Status polling SHOULD have rate/interval guidance.

---

# 160. Notification

Completion MAY additionally be communicated through events/webhooks where appropriate.

---

# 161. Cache

Caches MUST NOT become the authoritative store for Saga or idempotency state unless explicitly designed as durable enough for the requirement.

---

# 162. Redis Idempotency

Redis MAY support idempotency when:

```text
Loss semantics are acceptable

Persistence is configured appropriately

TTL semantics match requirements
```

---

# 163. Database Idempotency

PostgreSQL SHOULD be preferred for high-integrity idempotency records tied to business transactions.

---

# 164. Idempotency and Cache Eviction

Idempotency guarantees MUST NOT disappear unexpectedly because a cache entry was evicted earlier than the duplicate retry window.

---

# 165. State Ownership

Each distributed state item MUST have one authoritative owner.

---

# 166. Duplicate Ownership

Two services MUST NOT independently believe they own the same workflow state.

---

# 167. Data Copy

Services MAY maintain derived copies of external state, but ownership and consistency semantics MUST remain explicit.

---

# 168. Compensation Ownership

The service owning the original effect SHOULD normally own its compensation capability.

---

# 169. Orchestrator Direct Database Access

A Saga orchestrator MUST NOT compensate by directly modifying another service's database.

---

# 170. Compensation Contract

Compensation MUST occur through the owning service's approved contract.

---

# 171. Failure Categories

Saga steps SHOULD distinguish:

```text
BUSINESS REJECTION

TRANSIENT TECHNICAL FAILURE

PERMANENT TECHNICAL FAILURE

TIMEOUT

UNKNOWN OUTCOME
```

---

# 172. Business Rejection

A business rejection MAY cause immediate compensation without retry.

---

# 173. Transient Failure

A transient technical failure MAY be retried before compensation.

---

# 174. Permanent Failure

Permanent failure SHOULD avoid futile retry loops.

---

# 175. Unknown Outcome

Unknown outcomes require reconciliation/status inquiry rather than assuming success or failure.

---

# 176. Observability

Distributed transactions MUST be observable end-to-end.

---

# 177. Required Signals

Monitor applicable:

```text
Saga Started

Saga Completed

Saga Failed

Saga Compensating

Compensation Failed

Retry Count

Outbox Backlog

Inbox Duplicate Count

DLQ Count

Reconciliation Findings
```

---

# 178. Metrics Cardinality

Saga IDs and order IDs MUST NOT be unrestricted metric labels.

---

# 179. Logs

Logs MAY contain workflow IDs for correlation when permitted.

---

# 180. Tracing

Distributed tracing SHOULD correlate the online portions of a Saga but does not replace durable workflow state.

---

# 181. Dashboard

Critical workflows SHOULD have operational dashboards.

---

# 182. Alerting

Alerts SHOULD identify conditions such as:

```text
Growing Outbox Backlog

Stuck Sagas

Repeated Compensation Failure

DLQ Growth

Reconciliation Drift
```

---

# 183. Stuck Saga

A Saga that remains in an intermediate state beyond its expected deadline MUST become operationally visible.

---

# 184. Outbox Age

Oldest unsent Outbox event age is an important reliability metric.

---

# 185. Inbox Duplicate Metric

Duplicate counts MAY help detect producer retry/redelivery behavior.

---

# 186. Error Budget

Distributed workflow failure rates SHOULD contribute to applicable business SLI/SLOs.

---

# 187. Testing Strategy

Distributed consistency logic requires focused automated testing.

---

# 188. Saga Unit Test

Saga state-transition logic SHOULD have pure unit tests where practical.

---

# 189. Orchestrator Test

Orchestration tests SHOULD verify:

```text
Step Order

Failure Routing

Retry Policy

Compensation

Terminal State
```

---

# 190. Outbox Integration Test

Outbox tests MUST verify atomic persistence of:

```text
Business State

Outbox Event
```

---

# 191. Transaction Rollback Test

If business transaction fails, the Outbox record MUST also rollback.

---

# 192. Duplicate Consumer Test

Consumers MUST have tests delivering the same event multiple times.

---

# 193. Duplicate Effect Test

The test MUST verify no duplicated business side effect occurs.

---

# 194. Inbox Test

Inbox implementations SHOULD test concurrent duplicate delivery.

---

# 195. Idempotency API Test

HTTP idempotency SHOULD test:

```text
First Request

Duplicate Same Request

Concurrent Duplicate

Same Key Different Payload

Expired Key
```

---

# 196. Compensation Test

Every material compensation path SHOULD have tests.

---

# 197. Compensation Duplicate Test

Compensation SHOULD also be invoked twice in tests to prove idempotency where required.

---

# 198. Retry Test

Retryable failure tests MUST verify maximum attempts.

---

# 199. Permanent Failure Test

Permanent business failures MUST verify that inappropriate retry does not occur.

---

# 200. Timeout Test

Saga timeout behavior SHOULD be deterministic and avoid long sleeps.

---

# 201. Recovery Test

Tests SHOULD simulate process restart between workflow steps where durable recovery is required.

---

# 202. Reconciliation Test

Critical reconciliation logic SHOULD test detection and repair/escalation of inconsistent states.

---

# 203. Ordering Test

Consumers sensitive to event order SHOULD test stale/out-of-order events.

---

# 204. Optimistic Lock Test

Concurrent update tests SHOULD verify conflict detection.

---

# 205. Testcontainers

PostgreSQL, SQS and other infrastructure-sensitive tests SHOULD use Testcontainers where appropriate.

---

# 206. Failure Injection

Integration tests SHOULD inject selected failures such as:

```text
Broker unavailable

Consumer crash

Timeout

Duplicate event

Database rollback
```

---

# 207. Crash Window Test

Critical Outbox logic SHOULD conceptually validate crash windows around:

```text
Broker Publish

Outbox Sent Marking
```

---

# 208. AssertJ

Tests MUST follow established project standards, including meaningful:

```java
.as("...")
```

descriptions.

---

# 209. Architecture Review Checklist

Distributed workflows SHOULD evaluate:

```text
[ ] Is one local transaction sufficient?

[ ] Are we attempting cross-service atomicity?

[ ] Is Saga required?

[ ] Orchestration or choreography?

[ ] Who owns workflow state?

[ ] Is state durable?

[ ] What are intermediate states?

[ ] Which steps are compensatable?

[ ] Which steps are irreversible?

[ ] Are irreversible steps delayed appropriately?

[ ] Are compensations idempotent?

[ ] Can messages be duplicated?

[ ] Is consumer processing idempotent?

[ ] Is Inbox required?

[ ] Is Outbox required?

[ ] Are dual writes present?

[ ] Is HTTP idempotency required?

[ ] What is idempotency-key scope?

[ ] What is key retention?

[ ] Can concurrent duplicate requests occur?

[ ] Are unique constraints used?

[ ] Can events arrive out of order?

[ ] Is partitioning correct?

[ ] Are retries bounded?

[ ] Could retries amplify?

[ ] What happens on timeout?

[ ] Could outcome be unknown?

[ ] Is reconciliation available?

[ ] Can the workflow recover after restart?

[ ] Is manual replay safe?

[ ] Are stuck workflows observable?
```

---

# 210. Distributed Consistency Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] Outbox used for DB + event dual-write cases

[ ] Outbox event has unique event ID

[ ] Consumers declare duplicate handling

[ ] Idempotency tables have unique constraints

[ ] Saga state has durable identifier

[ ] Retry counts are bounded

[ ] DLQ configured where required

[ ] No remote calls inside long database locks

[ ] Compensation handlers are tested

[ ] Applied Flyway migrations remain immutable
```

---

# 211. Enterprise Distributed Transaction Gate

A workflow is not considered compliant when applicable conditions include:

```text
[ ] Service assumes remote call is part of local DB transaction

[ ] DB update + event publish uses unsafe dual write

[ ] Consumer assumes messages arrive exactly once

[ ] Duplicate message creates duplicate business effect

[ ] HTTP retry can create duplicate order/payment

[ ] Idempotency key has no uniqueness protection

[ ] Same idempotency key can execute different payloads

[ ] Saga state exists only in memory

[ ] Compensation is non-idempotent

[ ] Failed compensation has no recovery path

[ ] Poison messages retry forever

[ ] Remote call occurs while DB lock is held for long duration

[ ] Event ordering is assumed globally

[ ] Unknown timeout outcome is blindly retried

[ ] Stuck Saga cannot be detected

[ ] Reconciliation strategy is absent for critical workflows
```

---

# 212. Anti-Patterns

The following are prohibited or strongly discouraged:

- XA/2PC as default microservice transaction strategy
- cross-service database transactions
- shared transaction manager across independently deployed services
- database update followed by unprotected direct event publish
- event publish followed by independent database commit
- assuming exactly-once business processing
- consumers without duplicate handling
- unbounded retries
- retries on permanent business failures
- retries on non-idempotent external operations without operation identity
- in-memory-only long-running Saga state
- compensation without idempotency
- compensation by directly changing another service's database
- holding database locks during remote calls
- assuming global SQS ordering
- treating timeout as definite failure
- using trace ID as durable workflow state ID
- endless Inbox/Outbox retention without policy
- returning completed status before distributed workflow completes
- relying on reconciliation as the normal processing mechanism

---

# 213. Positive Consequences

The decision provides:

- safer distributed workflows
- reduced duplicate effects
- resilient event publication
- explicit partial-failure handling
- recoverable Saga workflows
- controlled compensation
- improved retry safety
- clearer ownership
- stronger operational visibility
- safer message replay
- improved consistency semantics
- reduced distributed-lock coupling

---

# 214. Negative Consequences

The decision introduces:

- Saga state
- Outbox/Inbox persistence
- compensation logic
- idempotency storage
- additional workflow states
- reconciliation jobs
- more integration testing
- eventual-consistency complexity

These costs are accepted because distributed workflows inherently contain partial-failure complexity whether the architecture models it explicitly or not.

---

# 215. Neutral Consequences

The decision also means:

- not every workflow needs a Saga
- not every consumer needs a separate Inbox table
- not every operation requires an HTTP idempotency key
- not every business action is compensatable
- eventual consistency is not always visible to users
- choreography remains appropriate for simple decoupled event reactions
- orchestration remains appropriate for explicit multi-step workflows
- exactly-once semantics remain technology- and boundary-specific

---

# 216. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate business effect | Critical | High | Idempotency |
| Lost event | Critical | Medium | Transactional Outbox |
| Saga stuck | High | Medium | Durable state + timeout |
| Compensation failure | High | Medium | Retry + reconciliation |
| Retry storm | Critical | Medium | Bounded retry |
| Out-of-order event | High | Medium | Version/queue-ordering strategy |
| Unknown remote outcome | Critical | Medium | Idempotency + status query |
| Inbox growth | Medium | High | Retention policy |
| Outbox backlog | High | Medium | Monitoring + dispatcher |
| Concurrent lost update | High | Medium | Optimistic locking |

---

# 217. Implementation Guidance

The following rules are mandatory:

1. Cross-service workflows must assume partial failure.
2. XA/2PC must not be the default microservice transaction strategy.
3. Each service must own its local ACID transaction.
4. Eventual consistency must be explicitly modeled.
5. Intermediate workflow states must be visible in the domain/application state where required.
6. Complex multi-step workflows should use explicit Saga orchestration.
7. Choreography should remain limited to workflows that stay understandable without a central coordinator.
8. Business-state and event dual writes must use Transactional Outbox.
9. Outbox delivery must assume duplicate publication.
10. Consumers must assume at-least-once delivery.
11. Business side effects must be idempotent where duplicate delivery is possible.
12. Integration events must have stable unique IDs.
13. Inbox/dedup storage should be used where natural idempotency is insufficient.
14. Duplicate detection must be concurrency safe.
15. HTTP mutation operations with material duplicate risk should use idempotency keys.
16. The same idempotency key must not execute materially different requests.
17. Idempotency records must have retention policy.
18. Optimistic locking should protect appropriate concurrent aggregate updates.
19. Database locks must not remain held during remote network calls without exceptional justification.
20. Message ordering must be treated as queue/FIFO-message-group scoped.
21. Consumers must handle stale/out-of-order events when possible.
22. Retries must be bounded and failure-aware.
23. Retry amplification across service chains must be considered.
24. Timeouts must distinguish failure from unknown outcome.
25. Non-idempotent external operations require stable operation identity/status reconciliation.
26. Compensation operations must themselves be idempotent.
27. Saga state must be durable when recovery after restart is required.
28. Failed compensations must have retry/reconciliation/manual recovery paths.
29. Critical distributed workflows must have reconciliation strategies.
30. Stuck workflows, Outbox backlog and DLQ growth must be observable.
31. Distributed workflow tests must include duplicate, timeout, rollback and recovery scenarios.
32. Applied Flyway migrations remain immutable; schema changes for Saga/Outbox/Inbox must use new migrations.

---

# 218. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- PostgreSQL
- Spring Data JPA
- optimistic locking
- Transactional Outbox
- SQS
- SQS
- workflow/Saga services
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- failure-injection tests
- integration tests
- reconciliation tests
- observability dashboards
- CI/CD architecture fitness functions

---

# 219. Success Criteria

The decision is successful when:

- dual-write event-loss windows are removed
- duplicate messages do not create duplicate business effects
- HTTP retries do not create duplicate critical mutations
- Saga workflows recover after process restart
- compensation behavior is explicit and tested
- retry storms decrease
- unknown remote outcomes are reconciled safely
- stale events do not overwrite newer state
- Outbox backlog is visible
- stuck workflows are detectable
- reconciliation resolves or escalates persistent inconsistencies
- distributed workflow behavior is understandable from durable state and telemetry

---

# 220. Alternatives Rejected

## 220.1 XA / Two-Phase Commit for All Services

Rejected because it couples availability and resource locking across distributed participants.

---

## 220.2 Database Then Publish Event

Rejected because a crash can permanently lose the event.

---

## 220.3 Publish Event Then Commit Database

Rejected because consumers can observe state that never committed.

---

## 220.4 Assume Broker Exactly Once Solves Everything

Rejected because end-to-end business side effects span databases and external systems outside the broker's transactional scope.

---

## 220.5 Retry Everything

Rejected because permanent failures and non-idempotent operations can create damage.

---

## 220.6 In-Memory Saga

Rejected for long-running critical workflows because restart loses workflow state.

---

## 220.7 Compensation as Direct Database Modification

Rejected because it violates service ownership boundaries.

---

# 221. Related Decisions

This ADR extends and implements:

- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-069: Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard

---

# 222. References

- Saga Pattern
- Transactional Outbox Pattern
- Idempotent Consumer Pattern
- Enterprise Integration Patterns
- Designing Data-Intensive Applications
- Building Microservices
- Amazon SQS Documentation
- AWS SQS Documentation
- PostgreSQL Documentation
- Spring Framework Transaction Documentation
- Microservices Patterns — Chris Richardson
- Google Site Reliability Engineering

---

# 223. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | AstraForge Supply Platform Architecture Team | Approved | Initial enterprise distributed consistency and Saga baseline |

---

# 224. Decision Summary

Within one service:

```text
BEGIN
  |
  +--> BUSINESS DATA
  |
  +--> OUTBOX
  |
COMMIT
```

Across services:

```text
LOCAL TX
   |
   v
EVENT / COMMAND
   |
   v
LOCAL TX
   |
   v
EVENT / COMMAND
   |
   v
LOCAL TX
```

with:

```text
IDEMPOTENCY
+
RETRY
+
COMPENSATION
+
RECOVERY
```

Outbox solves:

```text
DATABASE
    +
EVENT
```

coordination:

```text
ONE LOCAL TRANSACTION
        |
        +--> BUSINESS CHANGE
        +--> OUTBOX RECORD
```

Consumer semantics become:

```text
AT-LEAST-ONCE DELIVERY
        |
        v
MESSAGE MAY REPEAT
        |
        v
IDEMPOTENT CONSUMER
        |
        v
ONE BUSINESS EFFECT
```

Idempotent HTTP mutation becomes:

```text
REQUEST
  |
  +--> IDEMPOTENCY KEY
  |
  v
UNIQUE CLAIM
  |
  +--> NEW
  |      |
  |      v
  |   PROCESS
  |
  +--> EXISTING
         |
         v
      RETURN SAME
      LOGICAL RESULT
```

Saga orchestration becomes:

```text
ORCHESTRATOR
    |
    +--> STEP A
    |
    +--> STEP B
    |
    +--> STEP C
    |
    X
 FAILURE
    |
    v
COMPENSATE
B THEN A
```

when business dependencies require that order.

Timeouts become:

```text
TIMEOUT
  |
  X
  |
DOES NOT NECESSARILY MEAN
  |
  v
REMOTE OPERATION FAILED
```

Therefore ambiguous outcomes require:

```text
OPERATION ID
    +
STATUS QUERY
    +
RECONCILIATION
```

Retry becomes:

```text
FAILURE
   |
   v
TRANSIENT?
   |
 +--+--+
 |     |
NO    YES
 |     |
 v     v
STOP  IDEMPOTENT?
       |
     +-+-+
     |   |
    NO  YES
     |   |
     v   v
   STOP  BOUNDED
         RETRY
```

Concurrency becomes:

```text
READ VERSION 5
     |
  +--+--+
  |     |
 A       B
  |     |
  v     v
UPDATE UPDATE
  |     |
  v     X
VER 6 OPTIMISTIC
      CONFLICT
```

Reconciliation becomes:

```text
EXPECTED STATE
      |
      v
COMPARE
      |
      v
ACTUAL DISTRIBUTED STATE
      |
   +--+--+
   |     |
MATCH   DRIFT
   |     |
   v     v
DONE   REPAIR /
       ESCALATE
```

The complete distributed consistency equation is:

```text
LOCAL ACID
    +
TRANSACTIONAL OUTBOX
    +
AT-LEAST-ONCE ASSUMPTION
    +
IDEMPOTENT CONSUMERS
    +
IDEMPOTENT MUTATIONS
    +
DURABLE SAGA STATE
    +
EXPLICIT INTERMEDIATE STATES
    +
BOUNDED RETRIES
    +
IDEMPOTENT COMPENSATION
    +
OPTIMISTIC CONCURRENCY
    +
ORDERING AWARENESS
    +
RECONCILIATION
    +
OBSERVABILITY
    =
RELIABLE DISTRIBUTED BUSINESS CONSISTENCY
```

The governing principle is:

```text
Do not pretend
distributed systems
are local transactions.

Use ACID locally.

Use Saga across
business boundaries.

Use Outbox when state
and event must agree.

Assume messages duplicate.

Make consumers idempotent.

Do not promise exactly-once
where the entire business effect
cannot provide it.

Use idempotency keys
for critical retryable mutations.

Protect duplicate detection
against concurrency races.

Model intermediate states.

Persist long-running
workflow state.

Make compensation
a real business operation.

Make compensation idempotent.

Do not hold database locks
while waiting on the network.

Treat timeout as an
unknown outcome when appropriate.

Retry only failures
that can safely be retried.

Bound every retry.

Do not assume
global message ordering.

Reconcile distributed state
when automatic convergence fails.

Make stuck workflows visible.

And design every distributed
operation assuming that
the process can fail

one instruction after
the previous side effect
successfully committed.
```
