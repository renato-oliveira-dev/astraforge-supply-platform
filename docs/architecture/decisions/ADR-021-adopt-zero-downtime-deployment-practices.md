# ADR-021: Adopt Zero-Downtime Deployment Practices

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-021 |
| Title | Adopt Zero-Downtime Deployment Practices |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Deployment, Kubernetes, Database Evolution and Reliability |
| Related Work Items | Kubernetes, CI/CD, Flyway, Rolling Updates, Graceful Shutdown |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform consists of independently deployable services running on Kubernetes.

The platform includes:

- Java 21
- Spring Boot
- REST APIs
- Amazon SQS producers and consumers
- PostgreSQL
- Flyway
- Transactional Outbox
- Saga workflows
- Redis
- Resilience4j
- OpenTelemetry
- structured logging
- horizontal scaling
- automated CI/CD pipelines

Production services must evolve continuously.

Deployments may include:

- application code changes
- configuration changes
- database migrations
- event-contract evolution
- API evolution
- dependency upgrades
- security patches
- infrastructure changes

Stopping the entire platform for routine deployments would reduce availability and make independent service deployment impractical.

The platform therefore requires deployment practices that allow compatible application versions to coexist during rolling upgrades.

---

# 2. Problem Statement

The platform requires a deployment strategy that:

- avoids planned downtime for routine releases
- supports Kubernetes rolling updates
- allows old and new application versions to coexist
- preserves active HTTP requests
- preserves SQS processing
- avoids message loss
- avoids duplicate side effects
- supports safe database evolution
- supports schema migrations through Flyway
- supports backward-compatible APIs
- supports backward-compatible events
- enables rollback
- integrates with readiness and liveness probes
- supports graceful shutdown
- works with autoscaling
- supports canary and progressive delivery
- provides deployment observability
- protects Service-Level Objectives
- avoids distributed deployment locks
- minimizes operational risk

---

# 3. Decision Drivers

Primary decision drivers are:

1. production availability
2. independent deployments
3. rollback safety
4. database compatibility
5. event compatibility
6. API compatibility
7. Kubernetes-native operation
8. graceful workload termination
9. SLO protection
10. deployment automation
11. operational simplicity
12. failure isolation
13. observability
14. scalability
15. data integrity
16. repeatability
17. release velocity

---

# 4. Definition

For this platform, zero-downtime deployment means:

```text
A routine application release can be deployed
without intentionally making the supported
business capability unavailable.
```

It does not mean:

```text
No individual pod ever restarts.

No request can ever fail.

No infrastructure component can ever fail.

No deployment can ever introduce a defect.
```

Zero-downtime deployment is an architectural capability that minimizes user-visible interruption during planned releases.

---

# 5. Decision

The Enterprise Order Platform adopts zero-downtime deployment practices as the standard production deployment model.

The platform standardizes on:

```text
Backward-Compatible Change

↓

Database Expansion

↓

Application Rolling Deployment

↓

Compatibility Validation

↓

Traffic Migration

↓

Database Contraction
```

The fundamental rule is:

> A deployment must not require all running service instances to switch versions atomically.

Old and new versions must be capable of coexisting for the duration of the deployment.

---

# 6. Core Principle

Every deployment must assume that, temporarily:

```text
Version N

and

Version N+1
```

are running simultaneously.

Therefore:

```text
Database schema

API contracts

Event contracts

Configuration

Caches
```

must remain compatible with both versions during the transition.

---

# 7. Deployment Atomicity

Distributed application deployment is not atomic.

A Kubernetes rolling deployment may temporarily contain:

```text
Pod A → version 1.8.0

Pod B → version 1.8.0

Pod C → version 1.9.0

Pod D → version 1.9.0
```

Requests may reach any compatible ready pod.

Architecture must explicitly support this condition.

---

# 8. Kubernetes Rolling Update

The default Kubernetes deployment strategy is:

```yaml
strategy:
  type: RollingUpdate
```

Rolling updates progressively replace old pods with new pods.

---

# 9. Rolling Update Configuration

Production workloads must explicitly define appropriate:

```text
maxUnavailable

maxSurge
```

rather than relying blindly on defaults.

Example:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxUnavailable: 0
    maxSurge: 1
```

This pattern is suitable for many critical services because an existing replica is not intentionally removed before replacement capacity becomes available.

The exact values depend on:

- replica count
- resource capacity
- startup duration
- traffic volume
- service tier
- cluster capacity

---

# 10. `maxUnavailable`

`maxUnavailable` controls how many replicas may be unavailable during the update.

For critical services, the preferred starting point is:

```text
maxUnavailable = 0
```

when cluster capacity supports the required surge.

This is not universally mandatory.

Large deployments may require carefully calculated values.

---

# 11. `maxSurge`

`maxSurge` defines additional temporary replicas allowed during deployment.

Example:

```text
replicas = 6

maxSurge = 2
```

The deployment may temporarily run:

```text
8 pods
```

Cluster capacity planning must account for this.

---

# 12. Capacity During Deployment

A zero-downtime strategy must reserve sufficient capacity for overlapping versions.

A cluster operating continuously near 100% resource capacity may be unable to create replacement pods.

Capacity planning must account for:

- deployment surge
- node failure
- autoscaling
- traffic bursts
- pod disruption
- infrastructure maintenance

---

# 13. Readiness Probe

Readiness determines whether a pod may receive traffic.

A new pod must not become ready before it can safely process requests.

Readiness should verify application readiness rather than merely JVM existence.

---

# 14. Readiness Requirements

A pod must not report ready while:

- Spring context is still initializing
- mandatory configuration is invalid
- required internal initialization is incomplete
- required local resources are unavailable
- the application cannot safely serve its supported operations

---

# 15. External Dependencies in Readiness

Readiness must not blindly depend on every external service.

Example:

```text
orders-service
depends optionally on
notifications-service
```

If notifications are temporarily unavailable but order creation can continue, notification health must not make the Orders pod unready.

Otherwise one dependency outage can remove all healthy application pods.

---

# 16. Database in Readiness

Database connectivity may be part of readiness when the service cannot perform its core capability without the database.

The implementation must avoid:

- expensive readiness queries
- excessive connection usage
- cascade failures
- readiness flapping

---

# 17. Liveness Probe

Liveness determines whether Kubernetes should restart the process.

Liveness should identify an application process that cannot recover without restart.

It must not be used as a general dependency-health check.

---

# 18. Liveness Anti-Pattern

The following is prohibited:

```text
SQS unavailable
→ liveness fails
→ Kubernetes restarts every consumer
```

This converts a dependency outage into a restart storm.

---

# 19. Startup Probe

Services with significant startup time should use a startup probe where appropriate.

This prevents liveness from terminating an application while it is legitimately starting.

---

# 20. Probe Separation

The platform treats probes differently:

```text
Startup
→ Has initialization completed?

Readiness
→ Can this pod safely receive work?

Liveness
→ Is this process irrecoverably unhealthy?
```

These concepts must not be implemented as one identical health check.

---

# 21. Spring Boot Actuator

Spring Boot Actuator health groups should be used where appropriate to separate:

- liveness
- readiness

Application-specific health indicators must be carefully classified.

---

# 22. Graceful Shutdown

Application termination must be graceful.

When Kubernetes terminates a pod, the application must:

1. stop accepting new work
2. allow in-flight work to finish where practical
3. stop consuming new asynchronous work
4. close resources
5. terminate before the Kubernetes grace period expires

---

# 23. Kubernetes Termination Flow

Conceptually:

```text
Pod selected for termination

↓

Pod removed from service traffic

↓

SIGTERM delivered

↓

Application begins graceful shutdown

↓

In-flight operations complete

↓

Resources close

↓

Process exits
```

---

# 24. SIGTERM

Java services must respond correctly to `SIGTERM`.

The application must not require `SIGKILL` during normal deployment.

---

# 25. Termination Grace Period

Every workload must define an appropriate:

```yaml
terminationGracePeriodSeconds
```

The value must exceed the realistic graceful shutdown requirement.

It should consider:

- longest expected HTTP request
- SQS processing time
- transaction duration
- shutdown hooks
- executor shutdown
- connection closing
- telemetry flushing

---

# 26. Grace Period Anti-Pattern

A very short termination grace period may cause:

- interrupted transactions
- abandoned SQS processing
- incomplete responses
- lost telemetry
- unnecessary retries
- duplicate processing

A very long period may unnecessarily slow deployments.

The value must be measured.

---

# 27. HTTP Graceful Shutdown

When termination begins:

```text
new traffic
→ should stop reaching the pod
```

while:

```text
existing requests
→ should be allowed to complete
```

within the configured shutdown window.

---

# 28. Connection Draining

Load balancers, ingress controllers and Kubernetes Services may require time to observe endpoint removal.

Deployment design must account for propagation delay.

Where necessary, controlled draining mechanisms may be used.

---

# 29. `preStop`

A Kubernetes `preStop` hook may be used when needed to support connection draining.

It must not be used as an arbitrary fixed sleep without understanding the network behavior.

Example anti-pattern:

```yaml
preStop:
  exec:
    command:
      - sleep
      - "60"
```

Blind sleeps increase deployment duration and often hide incorrect readiness or load-balancer behavior.

---

# 30. Graceful Shutdown Validation

Graceful shutdown must be tested under active traffic.

A deployment test should verify:

```text
Requests continuously executing

+

Pod termination

↓

No material increase in failed requests
```

---

# 31. SQS Consumer Shutdown

SQS consumers require graceful shutdown.

During pod termination the consumer should:

- stop polling new records
- finish or safely abandon current processing
- commit offsets according to the processing model
- leave the consumer group cleanly

---

# 32. SQS Rebalancing

Rolling deployments may trigger consumer-group rebalances.

Deployment design must account for:

- rebalance duration
- partition ownership changes
- processing interruption
- duplicate delivery
- queue backlog/oldest-message age growth

---

# 33. Cooperative Rebalancing

Cooperative rebalancing should be considered where supported and appropriate.

It can reduce partition movement compared with eager rebalancing.

The exact SQS consumer strategy remains a platform configuration decision.

---

# 34. SQS At-Least-Once Delivery

The platform assumes at-least-once message delivery.

Therefore deployment may produce legitimate redelivery.

Consumers must remain idempotent.

Zero-downtime deployment does not depend on exactly-once processing assumptions.

---

# 35. In-Flight SQS Message

If a consumer terminates while processing an event, the event may be delivered again.

The consumer must safely handle this condition.

---

# 36. Idempotency

Idempotency is mandatory for externally observable operations that may be repeated.

Examples:

- event consumption
- payment commands
- inventory reservations
- workflow transitions
- notification requests where duplication matters
- outbox dispatch

---

# 37. Transactional Outbox

The Transactional Outbox pattern supports deployment safety because business state and the intent to publish an event are committed atomically.

If an outbox dispatcher terminates:

```text
unpublished rows remain in the database
```

and can be processed by another instance.

---

# 38. Outbox Dispatcher Shutdown

Outbox dispatchers must stop claiming new batches during shutdown and complete or safely release current work.

Rows must never remain permanently inaccessible because one pod terminated.

---

# 39. Multiple Dispatcher Versions

During rolling deployment, old and new dispatcher versions may execute concurrently.

Therefore:

- outbox schema must remain compatible
- event contracts must remain compatible
- claiming logic must remain compatible
- state transitions must remain compatible

---

# 40. Database Evolution

Database schema evolution is one of the highest-risk aspects of zero-downtime deployment.

The platform adopts:

```text
Expand and Contract
```

for incompatible schema evolution.

---

# 41. Expand and Contract

The migration model is:

```text
EXPAND

Add backward-compatible database structures

↓

MIGRATE

Deploy applications capable of using the new structure

↓

TRANSITION

Move reads/writes safely

↓

CONTRACT

Remove obsolete structures only after no supported application depends on them
```

---

# 42. Fundamental Database Rule

A database migration deployed before or during an application rollout must remain compatible with the currently running application version.

This means a migration must not immediately:

- drop a column still used by version N
- rename a column used by version N
- make a nullable field mandatory before old code populates it
- change a type incompatibly
- remove a table still queried by old pods

---

# 43. Flyway Migration Immutability

Once a Flyway migration has been applied to a shared environment, it is immutable.

An existing applied migration must never be edited to introduce a new database correction.

Every database adjustment must be implemented through a new migration with a new version.

Example:

```text
V31__add_new_order_column.sql
```

If a later correction is required:

```text
V32__adjust_new_order_column.sql
```

The platform must not modify `V31` after it has been applied.

---

# 44. Why Applied Migrations Are Immutable

Editing an applied migration creates:

- checksum mismatches
- environment divergence
- non-reproducible databases
- deployment failures
- uncertainty about actual schema state

Migration history is part of the production system's immutable deployment history.

---

# 45. Adding a Nullable Column

A safe expansion commonly begins with:

```sql
ALTER TABLE orders
ADD COLUMN approval_source VARCHAR(30);
```

The existing application continues functioning because it does not require the column.

---

# 46. Adding a Mandatory Column

Adding a `NOT NULL` column requires multiple phases.

Unsafe:

```sql
ALTER TABLE orders
ADD COLUMN approval_source VARCHAR(30) NOT NULL;
```

if existing rows and old application versions do not provide the value.

---

# 47. Safe Mandatory Column Evolution

Recommended sequence:

```text
Migration A
→ add nullable column

Deployment A
→ new application writes the column

Backfill
→ populate historical rows

Validation
→ confirm no null values remain

Migration B
→ add NOT NULL constraint
```

Only after all supported writers populate the field may the constraint become mandatory.

---

# 48. Column Rename

Direct column rename is unsafe during rolling deployment.

Unsafe:

```sql
ALTER TABLE orders
RENAME COLUMN old_status TO workflow_status;
```

Old pods still query:

```text
old_status
```

---

# 49. Safe Column Rename

Use transitional dual compatibility.

Conceptually:

```text
Add workflow_status

↓

Application supports old_status and workflow_status

↓

Backfill workflow_status

↓

Switch reads to workflow_status

↓

Stop writing old_status

↓

Remove old_status in a later release
```

---

# 50. Dual Write

Temporary dual writes may be necessary during schema transition.

Example:

```text
write old_status

and

write workflow_status
```

Dual writes introduce consistency risk and should be:

- temporary
- tested
- observable
- removed after transition

---

# 51. Database Trigger Alternative

Database triggers should not be the default mechanism for application schema compatibility.

They can hide business behavior and increase operational complexity.

Use only when justified by architecture review.

---

# 52. Backfill

Large backfills must not execute as uncontrolled blocking migrations.

A backfill may require:

- bounded batches
- restartability
- progress tracking
- throttling
- lock minimization
- observability
- independent execution

---

# 53. Large Migration

A migration that rewrites millions of rows may:

- lock tables
- increase WAL
- consume I/O
- increase replication lag
- block transactions
- violate latency SLOs

Large data movement must be designed separately from simple DDL deployment.

---

# 54. PostgreSQL DDL

PostgreSQL DDL behavior must be evaluated for the actual database version and table size.

Engineers must understand:

- lock level
- lock duration
- table rewrite behavior
- index construction behavior
- transaction implications

before executing production migrations.

---

# 55. Index Creation

Large index creation should avoid unnecessary write blocking.

Where appropriate, PostgreSQL concurrent index creation may be used.

Example:

```sql
CREATE INDEX CONCURRENTLY idx_orders_status
ON orders(status);
```

Flyway execution strategy must account for commands that cannot run inside a normal migration transaction.

---

# 56. Constraint Validation

Where PostgreSQL supports staged validation, constraints may be introduced in phases to reduce operational risk.

The specific approach must be reviewed for the PostgreSQL version in use.

---

# 57. Migration Timeouts

Production migrations should define appropriate:

- lock timeout
- statement timeout

where applicable.

A migration should fail safely rather than unexpectedly block critical traffic for an unbounded period.

---

# 58. Migration Observability

Production migration execution must be observable.

Relevant information includes:

- migration version
- start time
- completion time
- duration
- success
- failure
- database
- application release
- lock-related failure

Sensitive SQL parameter values must not be logged.

---

# 59. Migration Ownership

Database migrations belong to the application or bounded context that owns the schema.

Multiple unrelated services must not independently mutate the same tables.

---

# 60. Migration Ordering

The deployment pipeline must know whether a migration is:

```text
Backward-compatible expansion

or

Destructive contraction
```

Expansion may execute before the application rollout.

Contraction requires proof that old code no longer depends on the old structure.

---

# 61. Destructive Migration

Destructive migrations include:

- dropping column
- dropping table
- dropping index still required
- narrowing data type
- adding restrictive constraint
- removing enum value
- renaming structure
- removing compatibility view

They require explicit review.

---

# 62. Delayed Contraction

Database contraction should normally occur in a later release than the application transition.

Example:

```text
Release 42
→ add new column

Release 43
→ application migrates usage

Release 44
→ verify old column unused

Release 45
→ remove old column
```

The exact number of releases depends on deployment cadence and rollback requirements.

---

# 63. Rollback Window

Old database structures must remain available for at least the defined rollback window.

If version N+1 is rolled back to version N, the database must still support N.

---

# 64. Roll Forward

For some database changes, roll forward is safer than rollback.

Example:

```text
New application has already written data
using a new schema representation.
```

Reverting application code may no longer be safe.

The deployment plan must explicitly define whether failure recovery uses:

```text
rollback

or

roll forward
```

---

# 65. Database Rollback Scripts

Automatic down migrations are not the default production rollback strategy.

Destructive reversal can cause data loss.

Recovery must be designed for the specific migration.

---

# 66. API Compatibility

Public and inter-service APIs must remain compatible during rolling deployment.

Version N and N+1 clients and servers may interact temporarily.

---

# 67. Additive API Changes

Preferred API evolution is additive.

Examples:

- new optional response field
- new optional request field with safe default
- new endpoint
- new query parameter with default behavior

---

# 68. Removing API Fields

Removing a field requires evidence that no supported client depends on it.

The recommended process is:

```text
Deprecate

↓

Observe usage

↓

Migrate consumers

↓

Wait compatibility window

↓

Remove
```

---

# 69. Required Request Fields

Making a previously optional request field mandatory is a breaking change.

Old clients may not send it.

A transition strategy is required.

---

# 70. Response Fields

Consumers should ignore unknown response fields.

This supports additive API evolution.

---

# 71. API Contract Tests

Consumer and provider compatibility should be validated through:

- integration tests
- OpenAPI validation
- contract tests
- backward-compatibility tests

---

# 72. Event Compatibility

Integration events must follow ADR-018.

Old and new event consumers may coexist during rolling deployment.

Breaking event changes require explicit contract version evolution.

---

# 73. Additive Event Change

Adding an optional field is normally compatible.

Example:

```json
{
  "orderId": "...",
  "status": "APPROVED",
  "approvalSource": "WORKFLOW"
}
```

Old consumers should ignore `approvalSource`.

---

# 74. Event Removal

An event field must not be removed while supported consumers depend on it.

---

# 75. Event Semantic Change

Changing the meaning of an existing field without changing the contract version is prohibited.

Semantic compatibility matters as much as structural compatibility.

---

# 76. Topic Stability

SQS queue names should remain stable across normal application releases.

Topic duplication must not be used as the default deployment-version mechanism.

---

# 77. Configuration Compatibility

Configuration changes must also support overlapping versions.

Removing an environment variable or secret immediately may break old pods still running during deployment.

---

# 78. Configuration Expansion

Preferred sequence:

```text
Add new configuration

↓

Deploy application that supports both configurations

↓

Switch configuration usage

↓

Remove old configuration later
```

---

# 79. Secret Rotation

Secret rotation should support overlapping credentials where the external system permits it.

Conceptually:

```text
Old credential valid

+

New credential valid

↓

Deploy new credential

↓

Verify

↓

Revoke old credential
```

---

# 80. Cache Compatibility

Cache formats may change between application versions.

During rolling deployment:

```text
Version N
and
Version N+1
```

may access the same Redis entries.

Cache serialization therefore requires compatibility planning.

---

# 81. Cache Key Versioning

When cached object format changes incompatibly, version the cache key namespace.

Example:

```text
orders:v1:{id}
```

becomes:

```text
orders:v2:{id}
```

This is preferable to allowing incompatible deserialization.

---

# 82. Cache Rebuild

Caches must remain rebuildable from authoritative sources.

Deployment must not depend on cache contents for durable business correctness.

---

# 83. Session State

Application services should remain stateless where practical.

In-memory user session affinity complicates zero-downtime deployment.

---

# 84. Sticky Sessions

Sticky sessions are discouraged as a dependency for correctness.

A request should be able to reach another compatible replica.

---

# 85. In-Memory State

Business-critical state must not exist only in pod memory.

Pod termination is a normal deployment operation.

---

# 86. Local Cache

Local in-memory caches may be used for optimization when:

- they are disposable
- correctness does not depend on them
- stale data is bounded
- invalidation behavior is understood

---

# 87. Scheduled Jobs

Scheduled workloads require special deployment handling.

If every replica runs the same scheduler, rolling deployment may temporarily increase the number of schedulers.

---

# 88. Distributed Scheduler Safety

Scheduled jobs must use one of:

- distributed locking
- leader election
- Kubernetes CronJob
- idempotent execution
- another approved single-execution strategy

depending on workload requirements.

---

# 89. Scheduler Version Coexistence

If old and new scheduler versions may run simultaneously, job payload and database structures must remain compatible.

---

# 90. Kubernetes CronJob

Kubernetes CronJob is preferred for workloads that naturally execute as independent scheduled processes and do not require continuous application ownership.

---

# 91. Leader Election

Leader election may be used when exactly one active application instance should coordinate a background responsibility.

Leader loss must be recoverable.

---

# 92. Distributed Locks

Distributed locks must:

- have bounded leases
- recover from pod failure
- avoid indefinite ownership
- use appropriate fencing where stale owners could cause damage

---

# 93. Deployment Strategies

The platform supports several deployment strategies:

```text
Rolling Update

Canary

Blue/Green

Progressive Delivery
```

Rolling Update is the default.

Other strategies are selected based on release risk.

---

# 94. Rolling Deployment

Use rolling deployment when:

- changes are backward compatible
- rollback is straightforward
- traffic can be distributed across versions
- deployment risk is normal

---

# 95. Canary Deployment

Canary deployment introduces the new version to a limited portion of production traffic.

Example:

```text
95% → stable version

5% → canary version
```

Canary is appropriate for:

- high-risk changes
- major performance changes
- new dependency behavior
- framework upgrades
- substantial refactoring

---

# 96. Canary Evaluation

Canary analysis should compare:

- availability
- latency
- error rate
- resource consumption
- dependency behavior
- business success
- SLO burn rate

against the stable version.

---

# 97. Canary Traffic

Canary traffic must be statistically meaningful.

A canary receiving only a few requests may appear healthy while hiding defects.

---

# 98. Canary Duration

Canary duration must account for:

- traffic volume
- workload diversity
- asynchronous behavior
- scheduled operations
- caching
- business cycles

A fixed arbitrary duration is insufficient.

---

# 99. Automated Canary Analysis

Where platform maturity permits, canary analysis should be automated.

The pipeline may automatically:

```text
promote

pause

or

rollback
```

based on predefined reliability criteria.

---

# 100. Blue/Green Deployment

Blue/Green maintains two complete application environments:

```text
Blue → current

Green → candidate
```

Traffic switches after validation.

---

# 101. Blue/Green Advantages

Advantages include:

- rapid traffic switch
- easy application rollback
- isolated candidate validation

---

# 102. Blue/Green Limitations

Blue/Green does not solve database incompatibility.

Both versions may still share the same database.

Therefore Expand and Contract remains required.

---

# 103. Blue/Green Cost

Blue/Green may temporarily require approximately double application capacity.

It should be reserved for workloads where the operational benefit justifies the cost.

---

# 104. Progressive Delivery

Progressive delivery combines:

- deployment automation
- traffic control
- observability
- automated analysis
- controlled promotion

The platform should evolve toward progressive delivery for critical services.

---

# 105. Feature Flags

Feature flags may decouple:

```text
code deployment

from

feature activation
```

This reduces release risk.

---

# 106. Feature Flag Requirements

Production feature flags must define:

- owner
- purpose
- default state
- rollout plan
- rollback behavior
- expiration date

---

# 107. Feature Flag Debt

Temporary feature flags must be removed after rollout completion.

Permanent stale flags increase:

- complexity
- test combinations
- dead code
- operational ambiguity

---

# 108. Database Changes Behind Feature Flags

Feature flags do not eliminate database compatibility requirements.

Even disabled code may coexist with old application versions and shared schema.

---

# 109. Dark Launch

New code paths may be deployed without user-visible activation.

This allows validation of:

- startup
- dependencies
- configuration
- resource consumption

before enabling the feature.

---

# 110. Shadow Traffic

Shadow traffic may be used for selected high-risk changes.

A new implementation receives a copy of production traffic without controlling the user response.

It must not create duplicate business side effects.

---

# 111. Shadow Write Prohibition

Shadow processing must not:

- charge customers
- create real orders
- send notifications
- mutate authoritative business state

unless specifically designed with isolated destinations.

---

# 112. Deployment Pipeline

The production pipeline should conceptually execute:

```text
Build

↓

Unit Tests

↓

Static Analysis

↓

SAST

↓

Integration Tests

↓

Contract Tests

↓

Artifact Creation

↓

Database Compatibility Validation

↓

Deploy Candidate

↓

Readiness Validation

↓

Progressive Traffic

↓

SLO Validation

↓

Promotion

↓

Post-Deployment Verification
```

---

# 113. Immutable Artifact

The same application artifact tested before production must be promoted to production.

Rebuilding source code independently for each environment is discouraged.

---

# 114. Container Image

Container images must be immutable.

Use a unique identifier such as:

```text
semantic version

+

commit hash
```

Avoid deploying mutable tags such as:

```text
latest
```

as the authoritative production version.

---

# 115. Image Digest

Production deployment should support image digest identification.

This provides exact artifact traceability.

---

# 116. Configuration Separation

Environment-specific configuration must remain separate from the immutable application artifact.

---

# 117. Deployment Identity

Every running application should expose its deployment identity through observability metadata.

Recommended fields:

- service name
- service version
- commit
- build
- environment

---

# 118. OpenTelemetry Deployment Metadata

OpenTelemetry resource attributes should identify the deployed version.

Example:

```text
service.name

service.version

deployment.environment.name
```

This enables comparison between versions.

---

# 119. Structured Deployment Logs

Deployment lifecycle logs should include:

- service
- version
- environment
- startup duration
- readiness transition
- shutdown initiation
- shutdown completion

---

# 120. Deployment Metrics

Recommended deployment-related metrics include:

- startup duration
- readiness duration
- termination duration
- deployment error rate
- deployment latency
- rollback count
- failed rollout count

---

# 121. Deployment Markers

Observability dashboards must display deployment markers.

This allows engineers to correlate:

```text
release

↓

latency increase

↓

error-budget burn
```

---

# 122. Post-Deployment Verification

A successful Kubernetes rollout does not prove application success.

Post-deployment verification must validate:

- SLO health
- error rate
- latency
- SQS processing
- database behavior
- dependency health
- critical business journeys

---

# 123. Smoke Tests

Production smoke tests should validate a minimal set of safe critical operations.

Tests must avoid harmful business side effects.

---

# 124. Synthetic Transactions

Where appropriate, controlled synthetic transactions may verify critical journeys.

Synthetic data must be clearly identifiable and safely isolated.

---

# 125. Rollout Timeout

Deployment pipelines must have bounded rollout timeouts.

A deployment must not wait indefinitely for a pod that never becomes ready.

---

# 126. Failed Readiness

If a new version repeatedly fails readiness:

```text
traffic must remain on healthy old replicas
```

and promotion must stop.

---

# 127. CrashLoop

A candidate entering `CrashLoopBackOff` must stop the rollout.

Automation must not continue replacing healthy replicas with an unhealthy version.

---

# 128. SLO During Deployment

Deployment health must be evaluated against ADR-020.

A deployment threatening the service's error budget should:

```text
pause

rollback

or

roll forward with a corrective release
```

depending on the failure mode.

---

# 129. Error Budget

Services with severely depleted error budgets should use stricter deployment controls.

Possible controls:

- smaller canary
- longer observation
- manual approval
- reduced deployment frequency
- additional test gates

---

# 130. Rollback

Application rollback must be automated where practical.

The pipeline must know the last known-good artifact.

---

# 131. Rollback Preconditions

Rollback is safe only when:

- database remains compatible
- event contracts remain compatible
- configuration remains compatible
- data written by the new version can be read by the old version

---

# 132. Rollback Incompatibility

If version N+1 writes a representation version N cannot understand, simple rollback is unsafe.

This is why compatibility must be designed before deployment.

---

# 133. Roll Forward Strategy

When rollback is unsafe, recovery must use a corrective forward deployment.

The deployment plan must identify this possibility before production release.

---

# 134. Rollback Testing

Rollback capability should be tested periodically.

An untested rollback procedure must not be assumed to work.

---

# 135. Database Backup

Database backups do not replace safe deployment design.

Restoring a production database to undo an application deployment can cause loss of legitimate transactions completed after the backup.

---

# 136. Deployment Lock

The platform should avoid a global deployment lock requiring all services to deploy together.

Such coupling would contradict independent service ownership.

---

# 137. Service Dependency Order

Routine service deployments should not require strict coordinated ordering when contracts remain backward compatible.

If deployment order is mandatory, the change should be treated as a compatibility risk.

---

# 138. Consumer First

For some contract transitions, a safe sequence may be:

```text
Deploy consumers capable of old + new

↓

Deploy producer using new representation
```

---

# 139. Producer First

For additive compatible changes, producer-first deployment may also be safe if consumers ignore unknown fields.

The exact sequence depends on the contract.

---

# 140. Expand First

Database evolution generally follows:

```text
Schema expansion first

Application transition second

Schema contraction last
```

---

# 141. Emergency Deployment

Emergency fixes must still respect:

- database compatibility
- event compatibility
- API compatibility
- artifact traceability
- rollback safety

Urgency does not eliminate distributed-system constraints.

---

# 142. Security Patch

Critical security patches may justify accelerated approval, but the artifact must still pass the minimum required security and deployment validation.

---

# 143. Manual Production Changes

Manual modification of running application containers is prohibited.

Examples:

```text
kubectl exec

and then edit application files
```

Production state must be reproducible through version-controlled deployment artifacts and configuration.

---

# 144. Manual Database Changes

Untracked production schema changes are prohibited.

Database changes must use version-controlled migrations or an explicitly governed emergency database procedure followed by reconciliation into migration history.

---

# 145. Migration Drift

Flyway validation must detect unexpected migration drift.

Shared environments must not silently contain schema history that differs from source control.

---

# 146. Environment Parity

Pre-production environments should approximate production characteristics for:

- Kubernetes behavior
- PostgreSQL version
- SQS version
- deployment strategy
- health probes
- resource limits
- graceful shutdown
- observability

Perfect parity is not always economical, but deployment semantics should remain representative.

---

# 147. Integration Environment

Integration environments must test version coexistence where practical.

Example:

```text
Consumer N

Producer N+1
```

and:

```text
Consumer N+1

Producer N
```

---

# 148. Database Compatibility Test

CI/CD should validate migrations against:

```text
Current production schema

↓

Apply new migrations

↓

Run previous application compatibility tests where feasible

↓

Run new application tests
```

This detects destructive expansion changes before production.

---

# 149. Testcontainers

Testcontainers should be used to validate database migrations against a real PostgreSQL instance.

Migration tests should verify:

- clean installation
- upgrade from previous schema
- new application behavior
- compatibility assumptions

---

# 150. Migration Test

A migration test should not merely verify:

```text
Flyway completed successfully.
```

It should verify resulting schema semantics.

---

# 151. Contract Compatibility Tests

CI should detect incompatible changes to:

- OpenAPI
- event schemas
- shared integration contracts

Breaking changes require explicit architecture handling.

---

# 152. Graceful Shutdown Test

An automated integration test should:

1. start the application
2. begin an operation
3. send termination
4. verify expected completion behavior
5. verify clean resource shutdown

---

# 153. SQS Deployment Test

A deployment-oriented SQS test should verify:

- consumer shutdown
- rebalance
- duplicate delivery handling
- idempotency
- continued processing by remaining consumers

---

# 154. Rolling Version Test

Critical services should periodically test:

```text
Version N

and

Version N+1
```

against the same:

- database
- SQS queues
- Redis
- external contract stubs

---

# 155. Load Test During Deployment

Load testing should include deployment under traffic.

This reveals:

- capacity shortage
- connection-draining failures
- readiness problems
- startup latency
- queue backlog/oldest-message age spikes
- database contention

---

# 156. Failure Injection

Deployment validation should include controlled failures such as:

- new pod fails startup
- new pod fails readiness
- pod terminates during request
- pod terminates during SQS processing
- database migration lock timeout
- SQS in-flight message redelivery
- Redis unavailable
- dependency timeout

---

# 157. PodDisruptionBudget

Critical workloads should define a PodDisruptionBudget where appropriate.

The PDB protects availability during voluntary disruptions such as:

- node maintenance
- cluster upgrades
- voluntary eviction

---

# 158. PDB Limitation

A PodDisruptionBudget does not protect against:

- application crash
- node failure
- bad deployment
- capacity exhaustion

It is one component of availability design.

---

# 159. Anti-Affinity

Critical replicas should avoid unnecessary concentration on one failure domain.

Pod topology spread or anti-affinity should be considered.

---

# 160. Topology Spread

Where appropriate, replicas should be distributed across:

- nodes
- availability zones
- failure domains

A rolling deployment should not accidentally reduce resilience by concentrating new pods.

---

# 161. Resource Requests

Accurate Kubernetes resource requests are important for zero-downtime rollout.

If requests are unrealistically low:

- nodes may become overloaded

If requests are unrealistically high:

- surge pods may remain Pending

---

# 162. Resource Limits

Resource limits must be load tested.

A new version using more memory may:

```text
start successfully

↓

receive traffic

↓

hit memory limit

↓

restart repeatedly
```

Canary deployment helps detect this.

---

# 163. JVM Startup

Java startup characteristics must be considered.

Relevant factors include:

- class loading
- Spring initialization
- connection pools
- cache warm-up
- JIT compilation
- memory allocation

---

# 164. Cache Warm-Up

A new pod may technically be ready while still suffering cold-cache latency.

Readiness and canary analysis should consider whether warm-up materially affects SLOs.

---

# 165. Connection Pools

New pods create new connection pools.

A surge deployment can temporarily increase:

```text
database connections

HTTP connections

SQS connections
```

Dependency capacity must support deployment overlap.

---

# 166. Database Connection Surge

Example:

```text
10 normal pods

20 DB connections each

= 200 connections
```

With:

```text
maxSurge = 5
```

deployment may temporarily require:

```text
300 connections
```

Pool sizing and PostgreSQL capacity must account for this.

---

# 167. SQS Consumer Surge

Additional SQS consumer replicas do not necessarily increase parallelism beyond the configured queue/FIFO message-group concurrency.

Deployment configuration should understand partition count.

---

# 168. External API Surge

Rolling deployments can temporarily increase outbound connection pools.

External providers may observe additional concurrent connections.

---

# 169. DNS

Applications must tolerate normal DNS changes and connection lifecycle during pod replacement.

Long-lived connections must not permanently pin traffic to terminated endpoints.

---

# 170. Service Discovery

Applications should use platform-supported service discovery rather than storing pod addresses.

---

# 171. Long-Lived HTTP Connections

Connection pools must handle:

- endpoint removal
- stale connections
- connection reset
- DNS changes

Retry behavior must remain bounded and idempotency-aware.

---

# 172. WebSocket and Streaming Connections

Long-lived connections require specialized draining strategies.

Routine HTTP rolling-update assumptions may not be sufficient.

Services using:

- WebSocket
- SSE
- long polling
- streaming RPC

must define explicit connection migration behavior.

---

# 173. Background Executors

Application shutdown must close background executors.

Executors must not prevent JVM termination indefinitely.

---

# 174. Virtual Threads

Virtual threads simplify high-concurrency execution but do not eliminate shutdown requirements.

The application must still:

- stop accepting new tasks
- allow bounded completion
- propagate cancellation appropriately
- close external resources

---

# 175. Scheduled Virtual Threads

Scheduled or executor-managed virtual-thread work must respect application lifecycle.

Detached tasks that survive logical shutdown are prohibited.

---

# 176. Observability Flush

Telemetry exporters should receive a bounded opportunity to flush during graceful shutdown.

Telemetry flush must not indefinitely block termination.

---

# 177. Logging During Shutdown

Shutdown logs should identify:

- service
- version
- pod
- shutdown start
- shutdown reason where known
- completion
- duration

---

# 178. Deployment Audit

Production deployments must be auditable.

Deployment history should identify:

- service
- artifact
- version
- commit
- environment
- initiator
- pipeline
- timestamp
- result
- rollback where applicable

---

# 179. Change Approval

Approval requirements should depend on:

- service tier
- change risk
- error-budget state
- migration risk
- security impact

Automation should replace unnecessary manual gates where reliable controls exist.

---

# 180. Deployment Frequency

Zero-downtime practices should support frequent small deployments.

Smaller changes generally provide:

- smaller blast radius
- easier diagnosis
- easier rollback
- lower change complexity

---

# 181. Large Releases

Large bundled releases increase:

- compatibility risk
- migration risk
- rollback complexity
- diagnosis difficulty

The platform should favor incremental delivery.

---

# 182. Deployment Toil

Deployment procedures requiring repeated manual actions should be automated.

Examples:

- manually scaling pods
- manually disabling consumers
- manually editing configuration
- manually running standard migrations
- manually checking basic health endpoints

---

# 183. Automated Gates

The pipeline should automatically evaluate:

- tests
- static analysis
- security checks
- migration validation
- rollout status
- readiness
- SLO indicators
- smoke tests

---

# 184. Manual Gate

Manual approval remains appropriate for:

- destructive database migration
- unusually high-risk release
- major infrastructure transition
- depleted error budget
- exceptional security conditions

---

# 185. Deployment Metrics and DORA

The platform should track deployment performance indicators such as:

- deployment frequency
- lead time for changes
- change failure rate
- failed deployment recovery time

These complement SLO measurements.

---

# 186. Change Failure Rate

A deployment should count as failed when it requires:

- rollback
- emergency corrective deployment
- incident response
- significant user-visible degradation

The exact organizational definition must remain consistent.

---

# 187. Recovery Time

Recovery time should measure how quickly the platform returns to acceptable service after a failed deployment.

---

# 188. Release Health

Release health should combine:

```text
Deployment Result

+

SLO Health

+

Error Budget

+

Critical Business Journey Validation
```

---

# 189. Security

Deployment credentials must use least privilege.

CI/CD systems must not expose:

- Kubernetes credentials
- database passwords
- registry tokens
- signing keys
- cloud credentials

in logs.

---

# 190. Artifact Signing

Artifact signing and provenance should be adopted where supported by the software-supply-chain architecture.

This strengthens confidence that the deployed artifact is the artifact produced by the approved pipeline.

---

# 191. Image Scanning

Container images must undergo vulnerability scanning before production deployment according to security policy.

---

# 192. SBOM

Production artifacts should provide a Software Bill of Materials where required by platform security standards.

---

# 193. Deployment Failure Classification

Deployment failures should use controlled categories.

Examples:

```text
startup

readiness

migration

configuration

capacity

dependency

security

contract

runtime

slo_regression
```

---

# 194. Runbook

Every critical service should maintain a deployment runbook containing:

- normal deployment
- rollback
- roll-forward
- migration failure
- readiness failure
- SQS consumer failure
- stuck rollout
- SLO regression
- emergency recovery

---

# 195. Stuck Deployment

A rollout that does not progress must not remain indefinitely unresolved.

Automation should detect:

- unavailable replicas
- Pending pods
- image pull failure
- readiness failure
- CrashLoop
- insufficient capacity

---

# 196. Migration Failure

If a Flyway migration fails:

- application promotion must stop
- failure must be investigated
- the migration must not be silently edited if already applied elsewhere
- correction must use a new migration when required
- schema state must be verified before retry

---

# 197. Partial Migration

A partially executed database change requires explicit diagnosis.

Engineers must determine:

- transaction behavior
- committed DDL
- Flyway history
- schema state
- safe corrective action

Blindly rerunning arbitrary SQL is prohibited.

---

# 198. Zero-Downtime Is End-to-End

Zero-downtime deployment is not achieved merely because Kubernetes reports:

```text
deployment successfully rolled out
```

The complete chain must remain compatible:

```text
Client

↓

Ingress

↓

API

↓

Application

↓

Database

↓

SQS

↓

Consumers

↓

External Dependencies
```

---

# 199. Anti-Patterns

The following are prohibited:

- stopping all replicas for routine deployment
- requiring atomic deployment of multiple services
- modifying an already-applied Flyway migration
- dropping a database column before old code stops using it
- renaming a database column during a single rolling release
- adding mandatory database fields without transition
- running uncontrolled massive backfills in blocking migrations
- assuming Blue/Green solves database compatibility
- publishing breaking events without contract versioning
- removing API fields without consumer migration
- deleting old configuration while old pods still require it
- using incompatible shared cache serialization without versioning
- storing critical state only in pod memory
- relying on sticky sessions for correctness
- treating liveness as dependency health
- restarting pods because SQS is unavailable
- using arbitrary long `preStop` sleeps as the primary drain strategy
- using insufficient termination grace periods
- ignoring SQS redelivery during deployment
- assuming exactly-once behavior
- using mutable production image tags as release identity
- rebuilding different artifacts per environment
- manually modifying running containers
- executing untracked production schema changes
- assuming rollback is always safe
- automatically reversing destructive migrations
- ignoring deployment surge capacity
- ignoring connection-pool surge
- ignoring SLO degradation during rollout
- promoting a canary solely because pods are ready
- deploying critical changes without rollback or roll-forward planning
- keeping temporary feature flags indefinitely
- treating a successful Kubernetes rollout as sufficient production validation

---

# 200. Positive Consequences

The decision provides:

- routine releases without planned application downtime
- safer independent deployments
- controlled database evolution
- stronger rollback capability
- reduced deployment blast radius
- compatibility between overlapping versions
- better Kubernetes utilization
- safer SQS consumer deployments
- graceful request draining
- better release observability
- improved SLO protection
- support for progressive delivery
- reduced release coordination
- improved deployment automation
- safer schema migration practices
- stronger production traceability

---

# 201. Negative Consequences

The decision introduces:

- additional migration phases
- temporary schema duplication
- possible temporary dual writes
- compatibility code
- increased test requirements
- additional deployment capacity
- more sophisticated CI/CD
- longer lifecycle for destructive changes
- additional operational observability
- more complex rollback analysis
- feature-flag lifecycle management

These costs are accepted because distributed zero-downtime deployment cannot be achieved safely through simple application replacement.

---

# 202. Neutral Consequences

The decision also means:

- some database changes require several releases
- rollback may occasionally be replaced by roll-forward
- old structures may remain temporarily unused
- deployment temporarily consumes additional capacity
- SQS messages may be redelivered during consumer termination or deployment transitions
- some messages may legitimately be redelivered
- application versions coexist temporarily
- feature activation may occur separately from code deployment
- destructive cleanup happens after compatibility verification

---

# 203. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| New version incompatible with database | Critical | Medium | Expand and Contract |
| Applied Flyway migration is modified | High | Low | Immutable migration policy |
| Old pods fail after migration | Critical | Medium | Backward-compatible expansion |
| Rollback becomes impossible | High | Medium | Pre-deployment compatibility analysis |
| Pod receives traffic before ready | High | Medium | Correct readiness probe |
| Liveness creates restart storm | High | Medium | Separate liveness from dependency health |
| In-flight requests are terminated | High | Medium | Graceful shutdown and draining |
| SQS message/event is redelivered | Medium | High | Idempotent consumers |
| Consumer lag increases during rollout | Medium | Medium | Capacity and rebalance monitoring |
| Surge pods cannot schedule | High | Medium | Capacity planning |
| Database connection count spikes | High | Medium | Pool and surge analysis |
| Migration blocks production traffic | Critical | Medium | Lock analysis, timeout and staged migration |
| Backfill overloads database | High | Medium | Bounded asynchronous backfill |
| Cache format is incompatible | Medium | Medium | Cache key versioning |
| Configuration removed too early | High | Medium | Expand/transition/contract |
| Canary has insufficient traffic | Medium | Medium | Statistical evaluation |
| Deployment passes but business fails | Critical | Medium | SLO and synthetic validation |
| Feature flag becomes permanent debt | Medium | High | Owner and expiration |
| Manual production drift occurs | High | Low | Immutable deployment automation |
| Rollback corrupts new data | Critical | Low | Explicit rollback compatibility analysis |

---

# 204. Implementation Guidance

The following rules are mandatory:

1. Kubernetes RollingUpdate is the default deployment strategy.
2. Critical services must explicitly configure rolling-update behavior.
3. Version N and N+1 must be able to coexist.
4. Readiness must represent traffic-serving capability.
5. Liveness must not represent remote dependency availability.
6. Startup probes should protect legitimately slow startup where necessary.
7. Graceful shutdown is mandatory.
8. Termination grace periods must be based on measured workload behavior.
9. SQS consumers must support graceful termination.
10. SQS consumers must remain idempotent.
11. Transactional Outbox dispatch must survive pod termination.
12. Database evolution must follow Expand and Contract for incompatible changes.
13. Applied Flyway migrations are immutable.
14. Every later database correction must use a new Flyway migration version.
15. Destructive schema changes must be delayed until old versions no longer depend on them.
16. Database rollback compatibility must be analyzed before release.
17. Large backfills must be bounded and observable.
18. APIs must remain backward compatible during rollout.
19. Event contracts must follow ADR-018.
20. Configuration changes must support overlapping versions.
21. Incompatible cache formats must use versioned cache namespaces.
22. Business-critical state must not depend on pod-local memory.
23. Scheduled workloads must be safe during replica overlap.
24. Production container images must be immutable and identifiable.
25. Deployment metadata must be visible in telemetry.
26. Post-deployment validation must include SLO health.
27. High-risk releases should use canary or another progressive strategy.
28. Rollback or roll-forward strategy must be defined before production deployment.
29. Manual modification of running containers is prohibited.
30. Untracked database schema changes are prohibited.
31. CI/CD must test migration and contract compatibility.
32. Critical services must test graceful shutdown.
33. Critical services should test deployment under load.
34. Production deployment history must be auditable.
35. Security, SAST and artifact validation remain part of release gates.

---

# 205. Validation

The decision will be validated through:

- Kubernetes rollout tests
- readiness tests
- liveness tests
- startup tests
- graceful-shutdown tests
- active-traffic deployment tests
- SQS in-flight message redelivery tests
- SQS duplicate-delivery tests
- idempotency tests
- Transactional Outbox recovery tests
- Flyway migration tests
- previous-version database compatibility tests
- OpenAPI compatibility tests
- event-contract compatibility tests
- Redis serialization compatibility tests
- canary analysis
- load testing
- failure injection
- SLO validation
- rollback exercises
- roll-forward exercises
- deployment runbook reviews
- production-readiness reviews

---

# 206. Success Criteria

The decision is successful when:

- routine deployments require no planned application outage
- healthy traffic continues during rolling updates
- old and new versions coexist safely
- active HTTP requests are not unnecessarily terminated
- SQS processing continues during deployments
- duplicate SQS delivery remains safe
- database migrations do not break old replicas
- applied Flyway migrations remain immutable
- destructive schema changes occur only after compatibility windows
- rollback safety is known before release
- critical deployment failures stop promotion automatically
- canary releases are evaluated through reliability signals
- deployment markers correlate releases with telemetry
- SLO regressions are detected before full promotion
- production artifacts remain traceable
- deployment procedures are repeatable and auditable
- routine service releases do not require coordinated platform-wide deployment

---

# 207. Alternatives Rejected

## 207.1 Stop-and-Replace Deployment

Rejected because it creates intentional service downtime and does not scale with independent microservice releases.

---

## 207.2 Atomic Multi-Service Deployment

Rejected because distributed services cannot reliably be upgraded as one atomic unit.

---

## 207.3 Immediate Destructive Database Migration

Rejected because old application replicas remain active during rolling deployment.

---

## 207.4 Editing Existing Flyway Migrations

Rejected because it destroys migration immutability and causes environment divergence.

---

## 207.5 Blue/Green as the Only Strategy

Rejected because Blue/Green does not solve shared database, event or contract compatibility.

---

## 207.6 Always Roll Back

Rejected because some schema and data transitions make forward correction safer than application rollback.

---

## 207.7 Readiness Equal to Liveness

Rejected because the two probes answer fundamentally different operational questions.

---

## 207.8 Restart on Dependency Failure

Rejected because it amplifies external outages into application restart storms.

---

## 207.9 Sticky Sessions as Deployment Strategy

Rejected because they introduce replica affinity and complicate horizontal scaling and failure recovery.

---

# 208. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-017: Adopt Optimistic Locking for Concurrent Aggregate Updates
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-022: Adopt API Contract Governance

---

# 209. References

- Kubernetes Deployments Documentation
- Kubernetes Pod Lifecycle Documentation
- Kubernetes Probes Documentation
- Kubernetes PodDisruptionBudget Documentation
- Spring Boot Graceful Shutdown Documentation
- Spring Boot Actuator Documentation
- Flyway Documentation
- PostgreSQL Documentation
- Amazon SQS Consumer Documentation
- OpenTelemetry Specification
- Google Site Reliability Engineering
- Continuous Delivery
- Expand and Contract Database Migration Pattern
- Enterprise Order Platform Database Migration Standards
- Enterprise Order Platform CI/CD Standards
- Enterprise Order Platform Reliability Guidelines
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-018: Version Integration Event Contracts
- ADR-020: Define Service-Level Objectives

---

# 210. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial zero-downtime deployment architecture baseline |

---

# 211. Decision Summary

The Enterprise Order Platform adopts zero-downtime deployment practices for routine production releases.

The standard deployment model is:

```text
EXPAND

↓

DEPLOY

↓

VALIDATE

↓

TRANSITION

↓

CONTRACT
```

Kubernetes workloads use:

```text
Rolling Updates

+

Readiness Probes

+

Liveness Probes

+

Startup Probes where required

+

Graceful Shutdown

+

Controlled Surge Capacity
```

Database evolution follows:

```text
Backward-Compatible Expansion

↓

Application Transition

↓

Data Migration / Backfill

↓

Compatibility Validation

↓

Delayed Destructive Contraction
```

A fundamental database rule applies to every service:

```text
An applied Flyway migration is immutable.

Never modify an existing applied migration.

Every correction requires a new migration version.
```

Deployments must assume temporary coexistence of:

```text
Version N

+

Version N+1
```

across:

```text
Application code

Database schema

REST contracts

SQS message/event contracts

Configuration

Redis cache structures
```

Deployment success is not defined merely by:

```text
Kubernetes rollout completed
```

but by:

```text
Application healthy

+

Critical business journeys healthy

+

SLOs healthy

+

Messaging healthy

+

Database healthy
```

This decision establishes the deployment foundation required for frequent, independently deployable and operationally safe releases of the Enterprise Order Platform.
