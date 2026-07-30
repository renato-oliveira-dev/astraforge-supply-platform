# ADR-075: Adopt Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-075 |
| Title | Adopt Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Application Lifecycle, Health Checks, Kubernetes Probes, Graceful Shutdown |
| Related Work Items | Spring Boot Actuator, Kubernetes, Messaging, Outbox, Batch Jobs |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

A cloud-native application is not simply:

```text
RUNNING
```

or:

```text
STOPPED
```

A service passes through multiple lifecycle states.

Conceptually:

```text
CREATED
   |
   v
STARTING
   |
   v
INITIALIZING
   |
   v
READY
   |
   v
SERVING
   |
   v
TERMINATING
   |
   v
DRAINING
   |
   v
STOPPED
```

Kubernetes and application infrastructure must understand these states correctly.

Incorrect lifecycle configuration can create:

```text
Traffic Sent Too Early

Restart Loops

Dropped Requests

Duplicate Messages

Interrupted Transactions

Lost Background Work

Deployment Failures

Cascading Outages
```

---

# 2. Problem Statement

The organization requires standards covering:

- Spring Boot lifecycle
- Spring Boot Actuator
- liveness
- readiness
- startup probes
- health groups
- dependency health
- database health
- Redis health
- SQS health
- external API health
- Kubernetes probes
- graceful shutdown
- SIGTERM
- shutdown phases
- preStop
- terminationGracePeriodSeconds
- connection draining
- message consumers
- SQS listeners
- SQS workers
- Outbox dispatchers
- schedulers
- batch jobs
- Virtual Threads
- ExecutorService shutdown
- rolling deployments
- zero-downtime deployment
- restart loops
- cascading failures

---

# 3. Decision Drivers

Primary drivers are:

1. availability
2. zero-downtime deployment
3. safe shutdown
4. predictable startup
5. correct traffic routing
6. reduced restart loops
7. message-processing integrity
8. deployment reliability
9. failure isolation
10. operational visibility
11. graceful degradation
12. workload recovery

---

# 4. Decision

Applications MUST expose distinct lifecycle health semantics for:

```text
LIVENESS

READINESS

STARTUP
```

where Kubernetes probes are used.

These signals MUST have different meanings.

---

# 5. Fundamental Principle

```text
Liveness asks:

SHOULD THIS PROCESS
BE RESTARTED?


Readiness asks:

SHOULD THIS INSTANCE
RECEIVE NEW WORK?


Startup asks:

HAS INITIAL STARTUP
COMPLETED YET?
```

These questions MUST NOT be treated as equivalent.

---

# 6. Spring Boot Actuator

Spring Boot Actuator SHOULD provide the standard application-health endpoints.

---

# 7. Probe Endpoints

Where supported, applications SHOULD expose dedicated probe groups such as:

```text
/actuator/health/liveness

/actuator/health/readiness
```

---

# 8. Management Exposure

Only required Actuator endpoints SHOULD be exposed.

---

# 9. Public Exposure

Detailed Actuator information MUST NOT be exposed publicly without appropriate security.

---

# 10. Health Details

Detailed health information SHOULD be restricted to authorized operational users/systems.

---

# 11. Liveness

Liveness indicates whether the application process is fundamentally capable of continuing operation.

---

# 12. Liveness Failure

Liveness failure tells Kubernetes:

```text
THIS INSTANCE IS BROKEN
AND RESTART MAY HELP.
```

---

# 13. Liveness Must Be Conservative

Liveness SHOULD fail only when restarting the application is a reasonable recovery action.

---

# 14. External Dependency in Liveness

External dependencies SHOULD NOT normally participate in liveness.

---

# 15. Database in Liveness

Database unavailability MUST NOT ordinarily make the application liveness fail.

---

# 16. Redis in Liveness

Redis unavailability SHOULD NOT ordinarily make liveness fail.

---

# 17. SQS in Liveness

SQS service unavailability SHOULD NOT ordinarily make liveness fail.

---

# 18. External API in Liveness

External HTTP dependency failure MUST NOT ordinarily cause liveness failure.

---

# 19. Why

If a shared database fails and every application marks itself non-live:

```text
DATABASE FAILURE
      |
      v
ALL PODS FAIL LIVENESS
      |
      v
KUBERNETES RESTARTS ALL PODS
      |
      v
DATABASE STILL DOWN
      |
      v
RESTART LOOP
```

The platform converts one dependency outage into a broader application outage.

---

# 20. Cascading Restart

Liveness MUST NOT amplify dependency incidents through unnecessary restarts.

---

# 21. Appropriate Liveness Failures

Examples MAY include:

```text
Irrecoverable internal deadlock

Corrupted runtime state

Critical internal component permanently stopped

Application event loop irrecoverably blocked
```

where restart is a meaningful recovery.

---

# 22. Memory Pressure

Out-of-memory conditions are usually handled by process/container termination rather than application health checks.

---

# 23. Deadlock Detection

Custom liveness checks for deadlocks SHOULD only be introduced with strong justification.

---

# 24. Readiness

Readiness indicates whether the instance should receive NEW traffic or work.

---

# 25. Readiness Failure

When readiness fails:

```text
POD REMAINS RUNNING
       |
       v
POD REMOVED FROM
SERVICE TRAFFIC
```

but is not necessarily restarted.

---

# 26. Startup Readiness

An application MUST remain not-ready until mandatory initialization completes.

---

# 27. Termination Readiness

An application SHOULD become not-ready before graceful shutdown begins.

---

# 28. Dependency Readiness

Dependencies MAY influence readiness when the service cannot provide meaningful functionality without them.

---

# 29. Dependency Readiness Criteria

Before including a dependency in readiness, ask:

```text
IF THIS DEPENDENCY FAILS,
CAN THE INSTANCE STILL
SERVE MEANINGFUL REQUESTS?
```

---

# 30. Database Readiness

A database MAY participate in readiness when most core operations require it.

---

# 31. Redis Readiness

Redis SHOULD NOT make the service globally unready when the service supports a valid degraded fallback.

---

# 32. Optional Dependency

Optional integrations SHOULD NOT normally make global readiness fail.

---

# 33. Notification Service Example

If notifications fail but Orders can still be created safely:

```text
notifications-service DOWN
        |
        v
orders-service
SHOULD REMAIN READY
```

assuming async retry/degradation exists.

---

# 34. Critical Dependency

If the service cannot perform any meaningful operation without PostgreSQL, database connectivity MAY influence readiness.

---

# 35. Cascading Readiness Failure

Dependency checks MUST not create uncontrolled cascades.

---

# 36. Example Cascade

Avoid:

```text
SERVICE C DOWN
    |
    v
SERVICE B NOT READY
    |
    v
SERVICE A NOT READY
    |
    v
ENTIRE PLATFORM UNAVAILABLE
```

when A or B could still serve useful degraded functionality.

---

# 37. Readiness Is Admission Control

Readiness is effectively:

```text
SHOULD THE PLATFORM
SEND NEW WORK HERE?
```

It is not a complete monitoring system.

---

# 38. Monitoring vs Health

Health endpoints MUST NOT replace observability and alerting.

---

# 39. Startup Probe

A `startupProbe` SHOULD be used for workloads with startup duration that may exceed normal liveness timing.

---

# 40. Startup Probe Purpose

During startup-probe evaluation:

```text
LIVENESS DOES NOT
PREMATURELY KILL
THE STARTING PROCESS.
```

---

# 41. Slow Startup

Slow-starting applications SHOULD NOT solve startup problems by configuring excessively permissive liveness thresholds alone.

---

# 42. Startup Budget

Startup time MUST have a bounded expected budget.

---

# 43. Startup Failure

An application unable to initialize within the approved startup window SHOULD fail and become visible operationally.

---

# 44. Infinite Startup

A process waiting forever during startup is prohibited.

---

# 45. Startup Dependency

Mandatory startup dependencies MUST have bounded connection/retry behavior.

---

# 46. Flyway Startup

Flyway migration execution MAY participate in startup according to deployment architecture.

---

# 47. Flyway Failure

If mandatory migrations fail:

```text
APPLICATION MUST NOT
BECOME READY.
```

---

# 48. Migration Retry

Database connection retry during startup MUST remain bounded.

---

# 49. Existing Migration

Existing applied Flyway migrations MUST NEVER be modified to make application startup succeed.

---

# 50. New Migration

Database changes MUST use new versioned migrations.

---

# 51. Startup Initialization

Startup tasks SHOULD be classified as:

```text
MANDATORY

OPTIONAL
```

---

# 52. Mandatory Initialization

Examples MAY include:

```text
Configuration Validation

Required Bean Creation

Schema Compatibility

Security Initialization
```

---

# 53. Optional Initialization

Examples MAY include:

```text
Cache Warm-Up

Optional Metadata Prefetch

Noncritical Background Preparation
```

---

# 54. Optional Startup Failure

Failure of optional initialization SHOULD NOT necessarily prevent readiness if runtime fallback exists.

---

# 55. Cache Warm-Up

Cache warm-up SHOULD NOT create excessive startup latency.

---

# 56. Thundering Herd on Startup

When many pods start simultaneously, they MUST NOT overwhelm dependencies through identical initialization work.

---

# 57. Startup Jitter

Controlled jitter MAY be used for noncritical background initialization to reduce synchronized load.

---

# 58. Startup Retry

Retry during startup MUST use bounded attempts and backoff.

---

# 59. Startup Retry Storm

All replicas retrying a failed dependency simultaneously can create a recovery storm.

---

# 60. Probe Configuration

Probe timing MUST reflect actual application behavior.

---

# 61. Probe Timeout

Probe timeouts MUST be short and bounded.

---

# 62. Probe Handler

Health probe handlers MUST be inexpensive.

---

# 63. Expensive Probe

A probe MUST NOT execute:

```text
Complex Database Query

Remote Fan-Out

Large Cache Scan

Business Workflow
```

---

# 64. Probe Frequency

Probe frequency SHOULD provide fast detection without creating significant load.

---

# 65. failureThreshold

Failure thresholds SHOULD tolerate short transient noise while detecting real problems within an acceptable time.

---

# 66. successThreshold

Readiness recovery semantics SHOULD be configured carefully to avoid flapping.

---

# 67. Probe Flapping

Repeated transitions:

```text
READY
NOT READY
READY
NOT READY
```

can create routing instability.

---

# 68. Readiness Stabilization

Where necessary, readiness recovery SHOULD require sufficient stable success.

---

# 69. Health Group

Spring Boot health groups SHOULD separate:

```text
liveness

readiness

operational detail
```

---

# 70. Generic Health Endpoint

A generic:

```text
/actuator/health
```

MUST NOT automatically be used for both readiness and liveness without reviewing included indicators.

---

# 71. Built-In Health Indicators

Default framework indicators MUST be reviewed.

---

# 72. Automatic Inclusion Risk

A newly added dependency may introduce a health indicator that unexpectedly changes readiness behavior.

---

# 73. Health Indicator Ownership

Material health indicators SHOULD have explicit ownership and rationale.

---

# 74. Custom Health Indicator

Custom health indicators SHOULD be small, safe and bounded.

---

# 75. Remote Health Calls

One service SHOULD NOT call another service's health endpoint on every local health probe.

---

# 76. Health Dependency Graph

Avoid creating a distributed health graph such as:

```text
A HEALTH -> B HEALTH
B HEALTH -> C HEALTH
C HEALTH -> D HEALTH
```

---

# 77. Reason

This couples platform routing to dependency topology and can create cascading unavailability.

---

# 78. Graceful Shutdown

Applications MUST support graceful shutdown.

---

# 79. Shutdown Goal

The shutdown sequence should:

```text
STOP ACCEPTING NEW WORK

STOP SCHEDULING NEW WORK

DRAIN IN-FLIGHT WORK

FLUSH REQUIRED STATE

CLOSE RESOURCES

EXIT
```

---

# 80. SIGTERM

Containerized applications MUST handle `SIGTERM` as the normal shutdown signal.

---

# 81. SIGKILL

SIGKILL provides no graceful cleanup and SHOULD only occur when shutdown exceeds the grace period or the process is irrecoverable.

---

# 82. Spring Boot Graceful Shutdown

Spring Boot graceful web-server shutdown SHOULD be enabled.

---

# 83. Web Request Drain

When shutdown begins:

```text
NEW HTTP REQUESTS
SHOULD STOP ARRIVING
```

while existing requests receive bounded time to finish.

---

# 84. Readiness Before Shutdown

The pod SHOULD transition to not-ready before the process exits.

---

# 85. Endpoint Propagation

Infrastructure may require a short period for readiness changes to propagate.

---

# 86. preStop

A Kubernetes `preStop` hook MAY provide a short controlled delay or shutdown action when required by routing propagation.

---

# 87. preStop Sleep

A short sleep MAY be used as a pragmatic propagation buffer when empirically justified.

---

# 88. Excessive preStop Sleep

Large arbitrary sleeps SHOULD NOT hide incorrect lifecycle configuration.

---

# 89. Termination Grace Period

`terminationGracePeriodSeconds` MUST exceed the expected normal shutdown duration with reasonable margin.

---

# 90. Grace Period Too Short

If the grace period is too short:

```text
SIGTERM
   |
   v
IN-FLIGHT PROCESSING
   |
   X
SIGKILL
```

resulting in dropped/interrupted work.

---

# 91. Grace Period Too Long

Excessively long grace periods slow rollout and node evacuation.

---

# 92. Shutdown Budget

Each workload SHOULD define a realistic shutdown budget.

---

# 93. HTTP Shutdown

HTTP services SHOULD stop receiving new traffic before closing client/server resources.

---

# 94. Connection Pool Shutdown

Outbound HTTP connection pools MUST close cleanly.

---

# 95. Database Pool Shutdown

Database connection pools MUST close cleanly after active transactional work completes or times out.

---

# 96. Redis Shutdown

Redis clients SHOULD close gracefully.

---

# 97. Executor Shutdown

Application-managed executors MUST be shut down explicitly.

---

# 98. ExecutorService

Shutdown SHOULD conceptually use:

```text
STOP ACCEPTING TASKS

WAIT FOR TASKS

FORCE AFTER TIMEOUT
```

where appropriate.

---

# 99. Infinite Executor Wait

Shutdown MUST NOT wait forever for executor completion.

---

# 100. Virtual Threads

Virtual Threads do not remove shutdown requirements.

---

# 101. Virtual Thread Tasks

Running Virtual Thread tasks MUST still have:

```text
Cancellation

Deadline

Shutdown Semantics
```

---

# 102. Structured Task Lifetime

Background tasks MUST NOT survive unintentionally beyond application lifecycle boundaries.

---

# 103. Scheduled Tasks

Schedulers MUST stop scheduling new work during shutdown.

---

# 104. Scheduler Shutdown

Existing scheduled task execution SHOULD receive bounded completion according to workload semantics.

---

# 105. Cron Job

Kubernetes CronJobs SHOULD prefer job lifecycle semantics rather than embedding every batch workload inside a permanently running service.

---

# 106. SQS Consumer

SQS consumers require explicit shutdown semantics.

---

# 107. SQS Listener Stop

On shutdown, listeners SHOULD stop polling new records before existing processing is abandoned.

---

# 108. Offset Commit

Offset handling MUST remain consistent with business processing completion.

---

# 109. Commit Before Processing

Offsets MUST NOT be committed before the business effect is safely complete unless the architecture explicitly accepts possible message loss.

---

# 110. Shutdown During Processing

If shutdown occurs during message handling, the consumer MUST preserve at-least-once/idempotency semantics.

---

# 111. Duplicate After Shutdown

A message may be redelivered after interrupted processing.

Consumers MUST remain idempotent.

---

# 112. SQS In-Flight Message Redelivery

Consumer shutdown may cause partition rebalance.

---

# 113. Rebalance Awareness

Long processing and shutdown behavior MUST account for SQS long-polling, visibility-timeout and in-flight message settings.

---

# 114. SQS Consumer

SQS workers MUST stop receiving new messages before shutdown completion.

---

# 115. Visibility Timeout

Messages being processed MUST remain consistent with visibility-timeout semantics.

---

# 116. Interrupted SQS Work

If work cannot complete before termination:

```text
MESSAGE MUST BECOME
SAFELY RETRYABLE
```

according to idempotency semantics.

---

# 117. Delete After Success

SQS messages SHOULD be deleted only after successful business processing.

---

# 118. Outbox Dispatcher

Outbox dispatchers MUST stop claiming new events during shutdown.

---

# 119. Claimed Outbox Event

Events already claimed SHOULD either:

```text
COMPLETE SAFELY
```

or:

```text
BECOME ELIGIBLE FOR RETRY
```

after restart.

---

# 120. Outbox Lock

Shutdown MUST NOT leave permanent logical locks preventing later processing.

---

# 121. Outbox Recovery

Dispatcher state MUST remain recoverable after abrupt process termination.

---

# 122. Inbox Processing

Inbox/idempotency processing MUST remain transactionally safe if the application terminates mid-message.

---

# 123. Batch Job

Batch jobs MUST define behavior on termination.

---

# 124. Restartable Batch

Long-running batch work SHOULD be restartable when business requirements justify it.

---

# 125. Checkpoint

Batch systems MAY persist checkpoints to avoid reprocessing the entire workload.

---

# 126. Idempotent Batch

Reprocessing after restart MUST not create duplicate business effects.

---

# 127. File Processing

File-processing jobs SHOULD avoid marking a file complete before all required records are durably processed.

---

# 128. Partial File

Interrupted processing MUST expose a recoverable intermediate state.

---

# 129. Shutdown Ordering

Application component shutdown order matters.

---

# 130. Recommended Ordering

Conceptually:

```text
1. MARK NOT READY

2. STOP NEW HTTP TRAFFIC

3. STOP MESSAGE INTAKE

4. STOP SCHEDULERS

5. DRAIN ACTIVE WORK

6. FLUSH OUTBOX / REQUIRED STATE

7. CLOSE CLIENTS

8. CLOSE DATABASE/CACHE POOLS

9. EXIT
```

Exact ordering MAY differ by workload.

---

# 131. Producer Shutdown

Message producers SHOULD flush bounded outstanding sends when required.

---

# 132. Infinite Producer Flush

Producer shutdown MUST NOT block forever.

---

# 133. Telemetry Flush

Telemetry exporters MAY receive a short bounded flush window during shutdown.

---

# 134. Telemetry Is Secondary

Telemetry flush MUST NOT indefinitely block critical application shutdown.

---

# 135. Deployment

Rolling deployments MUST use lifecycle semantics to support zero or near-zero downtime.

---

# 136. Rolling Update

During a rolling update:

```text
OLD PODS
   +
NEW PODS
```

may coexist.

---

# 137. New Pod Sequence

New pod SHOULD:

```text
START

INITIALIZE

PASS STARTUP

BECOME READY

RECEIVE TRAFFIC
```

---

# 138. Old Pod Sequence

Old pod SHOULD:

```text
BECOME NOT READY

DRAIN

TERMINATE
```

---

# 139. Readiness Before Traffic

New pods MUST NOT receive production traffic until they are ready.

---

# 140. Startup Migration Compatibility

During rolling deployment:

```text
V1
+
V2
```

may coexist.

Database/configuration changes MUST remain compatible according to the expand/contract strategy.

---

# 141. minReadySeconds

Kubernetes `minReadySeconds` MAY ensure a new pod remains ready for a minimum period before being considered available.

---

# 142. maxUnavailable

Critical services SHOULD configure rollout availability limits appropriate to their replica count and SLO.

---

# 143. maxSurge

`maxSurge` SHOULD account for temporary infrastructure capacity during rollout.

---

# 144. Small Replica Count

For services with very few replicas, rolling-update configuration MUST be reviewed carefully to avoid downtime.

---

# 145. PodDisruptionBudget

Critical replicated workloads SHOULD define appropriate PodDisruptionBudgets.

---

# 146. PDB Is Not Replica Count

A PDB does not create replicas.

---

# 147. PDB and Node Maintenance

PDBs help prevent too many voluntary disruptions simultaneously.

---

# 148. PDB Misconfiguration

Overly restrictive PDBs can block cluster maintenance.

---

# 149. Deployment Timeout

Deployment tooling MUST have a bounded rollout timeout.

---

# 150. Failed Rollout

A rollout that never achieves readiness MUST fail visibly rather than remain pending indefinitely.

---

# 151. Automatic Rollback

Automated rollback MAY be used when supported and validated.

---

# 152. Probe and Rollback

Readiness/probe failures SHOULD contribute to safe deployment failure detection.

---

# 153. Startup Failure Loop

Repeated failed startups require operational visibility.

---

# 154. CrashLoopBackOff

`CrashLoopBackOff` is a symptom, not a root cause.

---

# 155. Restart Backoff

Kubernetes restart backoff prevents tight restart loops but does not replace root-cause investigation.

---

# 156. Liveness Misconfiguration

Aggressive liveness is a common cause of self-inflicted restart loops.

---

# 157. Probe Resource Contention

CPU starvation may cause health probes to fail even when application logic is otherwise correct.

---

# 158. Resource Requests

Appropriate CPU/memory requests help stabilize lifecycle behavior.

---

# 159. Probe Thread Starvation

Health endpoints SHOULD remain responsive under normal application load.

---

# 160. Separate Management Port

A separate management port MAY isolate probes from business traffic.

---

# 161. Separate Port Risk

A healthy management port does not necessarily prove the business server is functional.

---

# 162. Probe Path Alignment

Health checks SHOULD reflect the actual serving process sufficiently to avoid false health.

---

# 163. Readiness and Capacity

Readiness MAY be influenced by severe local saturation when the instance cannot safely accept more work.

---

# 164. Saturation as Readiness

Using saturation for readiness MUST avoid rapid flapping and should be carefully engineered.

---

# 165. Bulkhead

Application-level bulkheads SHOULD normally reject excess work rather than oscillating pod readiness on every transient spike.

---

# 166. Graceful Degradation

A degraded but useful service SHOULD generally remain ready.

---

# 167. Example

If Redis fails but the service can use database fallback:

```text
REDIS DOWN
   |
   v
CACHE DEGRADED
   |
   v
SERVICE STILL READY
```

---

# 168. Partial Capability

Applications MAY expose capability-specific health separately from global readiness.

---

# 169. Operational Health

A detailed operational health endpoint MAY report:

```text
Database UP

Redis DOWN

SQS UP

Notification DOWN
```

without using every status to control Kubernetes restart/routing.

---

# 170. Health Status Meaning

Operational status and Kubernetes lifecycle status MUST remain conceptually distinct.

---

# 171. Startup Observability

Startup duration SHOULD be measured.

---

# 172. Startup Metrics

Track applicable:

```text
Application Start Duration

Flyway Duration

Bean Initialization

Cache Warm-Up

Time to Readiness
```

---

# 173. Readiness Metrics

Readiness transitions SHOULD be observable where platform telemetry supports it.

---

# 174. Restart Metrics

Monitor restart counts and restart reasons.

---

# 175. Shutdown Metrics

Graceful vs forced termination SHOULD be observable where practical.

---

# 176. Kubernetes Events

Kubernetes events are valuable for diagnosing:

```text
Probe Failures

Evictions

OOMKilled

Failed Scheduling

CrashLoopBackOff
```

---

# 177. Logs

Lifecycle logs SHOULD identify major transitions.

Examples:

```text
Application startup completed

Application became ready

Shutdown initiated

Message intake stopped

Graceful shutdown completed
```

---

# 178. Lifecycle Log Volume

Probe requests themselves SHOULD NOT flood application logs.

---

# 179. Access Logs

Health-probe access logs MAY be excluded or sampled to reduce noise.

---

# 180. Correlation

Health probes generally do not require business correlation IDs.

---

# 181. Alerting

Critical lifecycle alerts SHOULD include:

```text
Repeated Liveness Failure

Readiness Failure Duration

CrashLoopBackOff

High Restart Count

Rollout Stuck

Forced Shutdown

Zero Ready Replicas
```

---

# 182. Zero Ready Replica

A critical service reaching:

```text
0 READY REPLICAS
```

requires immediate operational visibility.

---

# 183. Readiness Duration

A short readiness transition during deployment may be expected.

A sustained not-ready state requires investigation.

---

# 184. Restart Rate

High restart rate indicates:

```text
Application defect

Probe misconfiguration

Resource pressure

Dependency startup coupling
```

and must be diagnosed.

---

# 185. Testing Strategy

Lifecycle behavior MUST have appropriate automated and environment-level validation.

---

# 186. Actuator Test

Tests SHOULD verify required health endpoints are available with correct access policy.

---

# 187. Liveness Dependency Test

A test SHOULD verify selected external dependency failure does not incorrectly fail liveness.

---

# 188. Readiness Test

Readiness behavior SHOULD be tested for critical required dependencies where custom health logic exists.

---

# 189. Degraded Dependency Test

Fallback-capable dependencies SHOULD test that service remains ready when degradation is supported.

---

# 190. Startup Validation Test

Invalid mandatory configuration SHOULD prevent successful startup/readiness.

---

# 191. Flyway Failure Test

Critical integration tests SHOULD verify migration failure prevents readiness where applicable.

---

# 192. Graceful HTTP Shutdown Test

Tests SHOULD verify in-flight requests can complete within shutdown grace when practical.

---

# 193. Message Consumer Shutdown Test

Critical messaging components SHOULD verify that shutdown stops new intake and preserves retry semantics.

---

# 194. Outbox Shutdown Test

Outbox dispatchers SHOULD test recovery from interruption around dispatch.

---

# 195. Scheduler Shutdown Test

Schedulers SHOULD not begin new work after shutdown starts.

---

# 196. Executor Shutdown Test

Custom executors SHOULD verify bounded termination.

---

# 197. Forced Termination Test

Critical workflows SHOULD consider failure-injection tests simulating abrupt termination.

---

# 198. Restart Recovery Test

Durable workflows SHOULD verify recovery after application restart.

---

# 199. Kubernetes Environment Test

Representative environment testing SHOULD validate:

```text
Startup Probe

Readiness Probe

Liveness Probe

Rolling Deployment

Connection Drain
```

---

# 200. Deployment Smoke Test

Deployment pipelines SHOULD verify that new replicas become ready and serve a basic smoke scenario before rollout completion.

---

# 201. Probe Review Checklist

```text
[ ] Does liveness only detect process-level unrecoverable failure?

[ ] Are external dependencies excluded from liveness?

[ ] Does readiness represent ability to accept new work?

[ ] Are optional dependencies excluded from global readiness?

[ ] Is startupProbe required?

[ ] Are probe handlers inexpensive?

[ ] Are timeouts short and bounded?

[ ] Are thresholds resistant to brief transients?

[ ] Could probe dependencies create cascading outages?

[ ] Are health details secured?

[ ] Are probe requests excluded from noisy logs?
```

---

# 202. Shutdown Review Checklist

```text
[ ] Does SIGTERM trigger graceful shutdown?

[ ] Does the pod become not-ready before exit?

[ ] Is terminationGracePeriodSeconds sufficient?

[ ] Is preStop required?

[ ] Does HTTP traffic drain?

[ ] Do message consumers stop intake?

[ ] Are schedulers stopped?

[ ] Are executors drained with timeout?

[ ] Are Outbox workers recoverable?

[ ] Are SQS acknowledgement/delete and visibility-timeout semantics safe?

[ ] Are SQS messages safe for redelivery?

[ ] Are DB/HTTP/Redis clients closed correctly?

[ ] Is telemetry flush bounded?

[ ] Can shutdown become stuck indefinitely?
```

---

# 203. Deployment Review Checklist

```text
[ ] New pod must pass startup before readiness

[ ] New pod must be ready before receiving traffic

[ ] Old pod drains before termination

[ ] maxUnavailable is safe

[ ] maxSurge fits capacity

[ ] PodDisruptionBudget is appropriate

[ ] V1/V2 can coexist

[ ] Database migrations support rollout compatibility

[ ] Rollout timeout is bounded

[ ] Failed readiness stops promotion

[ ] Rollback path exists
```

---

# 204. Lifecycle Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] Liveness endpoint configured

[ ] Readiness endpoint configured

[ ] Startup probe configured where required

[ ] External HTTP dependencies excluded from liveness

[ ] terminationGracePeriodSeconds defined

[ ] Graceful shutdown enabled

[ ] Critical deployment has >1 replica

[ ] maxUnavailable configured

[ ] PodDisruptionBudget exists where required

[ ] Health details not publicly exposed

[ ] Message consumers use idempotent processing
```

---

# 205. Enterprise Lifecycle Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] Database outage causes all pods to fail liveness

[ ] External service failure causes restart loop

[ ] Pod receives traffic before initialization completes

[ ] Health probe executes expensive business logic

[ ] Probe has unbounded dependency call

[ ] Graceful shutdown is disabled

[ ] SIGTERM immediately interrupts active business work

[ ] termination grace period is shorter than normal drain time

[ ] SQS consumer commits before durable business completion without explicit semantics

[ ] SQS message is deleted before successful processing

[ ] Outbox worker can permanently lock events after termination

[ ] Scheduler continues starting new jobs during shutdown

[ ] Executor shutdown can wait forever

[ ] Rolling deployment can reduce critical service to zero available replicas

[ ] New schema breaks old pods during rollout

[ ] CrashLoopBackOff is caused by misconfigured aggressive liveness
```

---

# 206. Anti-Patterns

The following are prohibited or strongly discouraged:

- same endpoint/semantics for liveness and readiness without review
- database dependency in liveness by default
- remote API checks in liveness
- health-check fan-out across services
- expensive database queries in probes
- infinite startup waits
- extremely permissive liveness instead of startupProbe
- aggressive probes causing restart loops
- exposing full Actuator details publicly
- immediate process exit on SIGTERM
- arbitrary long `preStop` sleeps
- too-short termination grace periods
- SQS consumer shutdown without acknowledgement, visibility-timeout and redelivery analysis
- deleting SQS messages before business completion
- message handlers without duplicate safety
- in-memory-only recovery state for long workflows
- scheduler intake continuing during shutdown
- executors with no shutdown policy
- telemetry flush blocking termination indefinitely
- zero-downtime claims without readiness/draining tests
- database migrations incompatible with mixed-version rollout

---

# 207. Positive Consequences

The decision provides:

- safer rolling deployments
- reduced restart loops
- correct traffic admission
- cleaner dependency failure isolation
- fewer dropped requests
- safer messaging shutdown
- better Outbox recovery
- predictable startup
- improved zero-downtime capability
- clearer operational diagnostics
- improved Kubernetes behavior
- reduced cascading failures

---

# 208. Negative Consequences

The decision introduces:

- probe design and tuning
- shutdown coordination
- messaging lifecycle management
- rollout configuration
- additional environment testing
- lifecycle observability

These costs are accepted because application lifecycle is a core reliability concern in orchestrated environments.

---

# 209. Neutral Consequences

The decision also means:

- a dependency can be unhealthy while the service remains live
- a service can be live while not ready
- a service can be degraded while still ready
- restart is not the correct recovery for every failure
- not every dependency belongs in readiness
- not every workload requires startupProbe
- graceful shutdown still requires bounded force termination
- readiness is not a monitoring replacement

---

# 210. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Cascading restart loop | Critical | Medium | Conservative liveness |
| Traffic sent too early | High | Medium | Startup/readiness probes |
| Dropped requests | High | Medium | Graceful drain |
| Duplicate message processing | High | Medium | Idempotency |
| Lost message | Critical | Low/Medium | Commit/delete after success |
| Stuck shutdown | High | Medium | Bounded grace |
| Rollout downtime | Critical | Medium | Update strategy + probes |
| Probe flapping | Medium | Medium | Threshold tuning |
| Dependency cascade | Critical | Medium | Readiness discipline |
| Startup storm | High | Medium | Bounded retry/jitter |

---

# 211. Implementation Guidance

The following rules are mandatory:

1. Liveness, readiness and startup semantics must remain distinct.
2. Liveness must only fail when restart is a meaningful recovery.
3. External dependencies must not normally participate in liveness.
4. Readiness must represent ability to safely accept new work.
5. Optional/fallback-capable dependencies must not unnecessarily fail global readiness.
6. Startup probes should protect legitimately slow-starting workloads.
7. Health probes must be inexpensive and bounded.
8. Health details must be secured.
9. Applications must support graceful SIGTERM shutdown.
10. Pods should become not-ready before terminating.
11. In-flight HTTP work should receive bounded drain time.
12. `terminationGracePeriodSeconds` must reflect real shutdown needs.
13. `preStop` should be used only when it solves a defined draining/propagation requirement.
14. Custom executors must stop accepting tasks and terminate within bounded time.
15. SQS consumers must stop intake before termination.
16. Message completion/offset/delete semantics must preserve at-least-once correctness.
17. Outbox workers must recover safely from process termination.
18. Schedulers must stop launching new work during shutdown.
19. Long-running batch work should support restart/recovery where required.
20. Rolling deployments must ensure new pods are ready before old capacity is removed.
21. Critical services must configure safe `maxUnavailable`, `maxSurge` and disruption behavior.
22. Database/configuration evolution must support mixed-version rollout.
23. Lifecycle transitions, restart counts and zero-ready-replica conditions must be observable.
24. Lifecycle behavior must be tested in representative Kubernetes environments.
25. Existing Flyway migrations remain immutable; lifecycle-related DB changes require new migrations.

---

# 212. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Boot Actuator
- Spring graceful shutdown
- Kubernetes startupProbe
- Kubernetes readinessProbe
- Kubernetes livenessProbe
- Kubernetes Deployments
- PodDisruptionBudgets
- PostgreSQL
- Flyway
- SQS
- SQS
- Transactional Outbox
- Virtual Threads
- JUnit 5
- AssertJ
- Testcontainers
- Kubernetes integration testing
- deployment smoke tests
- failure-injection testing
- observability dashboards

---

# 213. Success Criteria

The decision is successful when:

- dependency outages do not create unnecessary restart storms
- applications receive no traffic before initialization completes
- terminating pods stop new intake before exit
- active requests drain predictably
- messaging workers preserve delivery semantics during shutdown
- Outbox processing recovers after abrupt restarts
- scheduled jobs stop cleanly
- failed rollouts are detected automatically
- critical services maintain availability during normal deployments
- probe flapping decreases
- startup and shutdown durations become measurable
- zero-ready-replica incidents are immediately visible

---

# 214. Alternatives Rejected

## 214.1 Database Health in Liveness

Rejected as the default because restarting the application does not repair a database outage and can create cascading restarts.

---

## 214.2 One Generic Health Probe

Rejected because liveness, readiness and startup answer different operational questions.

---

## 214.3 Immediate Shutdown on SIGTERM

Rejected because it interrupts in-flight work and increases duplicate/lost processing risk.

---

## 214.4 Extremely Long preStop Sleep

Rejected because it hides lifecycle problems and unnecessarily slows deployment.

---

## 214.5 Retry Forever During Startup

Rejected because failed startup must become operationally visible.

---

## 214.6 Commit Message Offset Before Processing

Rejected as the standard because process failure can cause message loss.

---

## 214.7 Treat Every Dependency as Readiness Critical

Rejected because it creates cascading platform outages and prevents graceful degradation.

---

# 215. Related Decisions

This ADR extends and implements:

- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Application Resilience
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-074: Enterprise Service Discovery, DNS, Load Balancing and Internal Network Communication Standard

---

# 216. References

- Kubernetes Documentation — Liveness, Readiness and Startup Probes
- Kubernetes Pod Lifecycle Documentation
- Kubernetes Deployment Documentation
- Kubernetes PodDisruptionBudget Documentation
- Spring Boot Actuator Documentation
- Spring Boot Graceful Shutdown Documentation
- Spring for Amazon SQS Documentation
- Amazon SQS Consumer Documentation
- AWS SQS Documentation
- Java ExecutorService Documentation
- PostgreSQL Documentation
- Flyway Documentation
- Google Site Reliability Engineering

---

# 217. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | AstraForge Supply Platform Architecture Team | Approved | Initial application lifecycle and Kubernetes health-probe baseline |

---

# 218. Decision Summary

Application lifecycle becomes:

```text
START
  |
  v
STARTUP PROBE
  |
  v
INITIALIZATION
  |
  v
READY
  |
  v
SERVING
  |
  v
SIGTERM
  |
  v
NOT READY
  |
  v
DRAINING
  |
  v
STOPPED
```

Liveness becomes:

```text
PROCESS
   |
   v
CAN RESTART
FIX THIS?
   |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
FAIL  KEEP
LIVE  LIVE
```

Readiness becomes:

```text
INSTANCE
   |
   v
CAN ACCEPT
NEW WORK SAFELY?
   |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
READY NOT READY
```

Therefore:

```text
DATABASE DOWN
```

does not automatically mean:

```text
RESTART EVERY POD
```

Graceful shutdown becomes:

```text
SIGTERM
   |
   v
NOT READY
   |
   v
STOP NEW HTTP WORK
   |
   v
STOP MESSAGE INTAKE
   |
   v
STOP SCHEDULERS
   |
   v
DRAIN ACTIVE WORK
   |
   v
CLOSE CLIENTS / POOLS
   |
   v
EXIT
```

SQS shutdown becomes:

```text
STOP POLLING
    |
    v
COMPLETE CURRENT
BUSINESS PROCESSING
    |
    v
COMMIT SAFE OFFSET
    |
    v
CLOSE CONSUMER
```

SQS shutdown becomes:

```text
STOP RECEIVE
    |
    v
COMPLETE CURRENT
MESSAGE
    |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
DELETE  ALLOW
MESSAGE REDELIVERY
```

Outbox shutdown becomes:

```text
STOP CLAIMING
NEW EVENTS
    |
    v
COMPLETE OR
RELEASE ACTIVE WORK
    |
    v
RESTART-SAFE STATE
```

Rolling deployment becomes:

```text
V2 POD START
      |
      v
STARTUP OK
      |
      v
READINESS OK
      |
      v
RECEIVE TRAFFIC
      |
      v
V1 NOT READY
      |
      v
DRAIN V1
      |
      v
TERMINATE V1
```

The complete lifecycle equation is:

```text
DISTINCT STARTUP
        +
CONSERVATIVE LIVENESS
        +
MEANINGFUL READINESS
        +
BOUNDED PROBES
        +
GRACEFUL SIGTERM
        +
TRAFFIC DRAINING
        +
MESSAGE INTAKE CONTROL
        +
EXECUTOR SHUTDOWN
        +
RECOVERABLE OUTBOX
        +
ROLLING-UPDATE SAFETY
        +
MIXED-VERSION COMPATIBILITY
        +
LIFECYCLE OBSERVABILITY
        =
ZERO-DOWNTIME-READY APPLICATION LIFECYCLE
```

The governing principle is:

```text
Do not ask liveness
whether the database is alive.

Ask liveness whether
restarting this process
would actually help.

Ask readiness whether
this instance can safely
accept new work.

Use startup probes
for legitimate startup time.

Do not send traffic
before initialization completes.

Do not run expensive
business logic in probes.

Do not create dependency
health-call chains.

Become not-ready
before shutting down.

Stop taking new work.

Drain existing work.

Stop consumers.

Stop schedulers.

Close executors with a deadline.

Do not wait forever.

Allow interrupted messages
to be redelivered safely.

Make consumers idempotent.

Make Outbox workers
restart-safe.

Give the application
enough time to shut down,
but not unlimited time.

Roll new pods in
before old capacity leaves.

Keep schema and configuration
compatible while versions coexist.

Measure startup.

Measure readiness.

Measure restarts.

Measure forced termination.

And remember:

a healthy cloud-native service
is not merely a process
that is still running.

It is a process that knows
when it can accept work,

when it should stop
accepting work,

and how to leave
without damaging the system.
```
