# ADR-077: Adopt Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-077 |
| Title | Adopt Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Scheduling, Batch Processing, Distributed Workloads, Job Coordination |
| Related Work Items | Spring Batch, Kubernetes CronJob, Quartz, ShedLock, PostgreSQL, SQS, SQS |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise systems frequently execute workloads that are not directly initiated by an HTTP request.

Examples include:

```text
DAILY BILLING

ORDER RECONCILIATION

FILE IMPORT

OUTBOX DISPATCH

CACHE REFRESH

REPORT GENERATION

DATA CLEANUP

RETENTION ENFORCEMENT

INTEGRATION SYNCHRONIZATION

NOTIFICATION RETRY
```

These workloads introduce concerns different from ordinary request/response APIs.

A scheduled workload may:

```text
RUN FOR MINUTES OR HOURS

PROCESS MILLIONS OF ROWS

RETRY AFTER FAILURE

RESTART AFTER CRASH

RUN ON MULTIPLE PODS

OVERLAP WITH ITSELF

EXECUTE DURING DEPLOYMENT

DEPEND ON BUSINESS DATE

PRODUCE PARTIAL RESULTS
```

Scheduling therefore requires explicit architecture rather than simply adding:

```java
@Scheduled(...)
```

to arbitrary service methods.

---

# 2. Problem Statement

The organization requires standards covering:

- scheduled jobs
- Spring `@Scheduled`
- Spring Batch
- Kubernetes CronJob
- Quartz
- distributed scheduling
- distributed locking
- leader election
- ShedLock
- overlap prevention
- concurrency
- idempotency
- chunk processing
- restartability
- retries
- checkpoints
- business dates
- misfire handling
- missed schedules
- catch-up behavior
- job metadata
- long-running workloads
- file processing
- batch transactions
- job status
- observability
- operational reprocessing
- manual execution
- shutdown
- deployment behavior

---

# 3. Decision Drivers

Primary drivers are:

1. reliability
2. idempotency
3. restartability
4. predictable execution
5. distributed safety
6. bounded resource usage
7. recoverability
8. operational visibility
9. deployment independence
10. data integrity
11. scalability
12. maintainability

---

# 4. Decision

Scheduled and batch workloads MUST use an execution model appropriate to workload semantics.

The default decision model is:

```text
SIMPLE SHORT PERIODIC TASK?
        |
     +--+--+
     |     |
    YES    NO
     |     |
     v     v
@Scheduled  LONG / DATA-HEAVY /
MAY APPLY   RESTARTABLE?
                |
             +--+--+
             |     |
            YES    NO
             |     |
             v     v
        SPRING BATCH /
        KUBERNETES JOB
```

Kubernetes-native deployments SHOULD prefer Kubernetes CronJob for independently scheduled batch workloads when appropriate.

---

# 5. Fundamental Principle

```text
A schedule tells
WHEN work may start.

It does not define:

whether the work is safe
to repeat,

whether two copies may run,

whether it can restart,

or whether partial completion
is acceptable.
```

---

# 6. Workload Classification

Scheduled workloads SHOULD be classified before implementation.

Useful categories include:

```text
SHORT PERIODIC TASK

LONG-RUNNING BATCH

DATA MIGRATION / BACKFILL

FILE IMPORT / EXPORT

RECONCILIATION

ASYNC RETRY / RECOVERY

MAINTENANCE / CLEANUP
```

---

# 7. Short Periodic Task

A short periodic task is typically:

```text
Fast

Bounded

Idempotent

Low Data Volume

Safe to Retry
```

---

# 8. Long-Running Batch

A long-running batch may require:

```text
Chunking

Checkpointing

Restartability

Execution Metadata

Failure Recovery
```

---

# 9. `@Scheduled`

Spring `@Scheduled` MAY be used for simple in-process periodic tasks.

---

# 10. `@Scheduled` Limit

`@Scheduled` MUST NOT automatically be used for:

```text
Very Long Jobs

Huge Data Processing

Critical Single-Execution Jobs

Restartable Batch Pipelines

Complex Distributed Scheduling
```

---

# 11. Multi-Replica Risk

In Kubernetes:

```text
3 PODS
  |
  +--> EACH HAS @Scheduled
```

may result in:

```text
3 EXECUTIONS
```

for one logical schedule.

---

# 12. Implicit Singleton Assumption

A scheduled job MUST NOT assume that only one application instance exists.

---

# 13. Distributed Coordination

When only one logical execution may occur across replicas, explicit coordination MUST be used.

---

# 14. Coordination Options

Approved options MAY include:

```text
Kubernetes CronJob

Distributed Lock

Leader Election

Quartz Cluster

Dedicated Scheduler
```

depending on requirements.

---

# 15. Prefer External Scheduling

For independent production batch workloads, external scheduling SHOULD generally be preferred over embedding scheduling in every application replica.

---

# 16. Kubernetes CronJob

Kubernetes CronJob SHOULD be the default scheduler for independently executable Kubernetes batch workloads where its semantics are sufficient.

---

# 17. CronJob Model

Conceptually:

```text
CRON SCHEDULE
     |
     v
KUBERNETES CRONJOB
     |
     v
JOB
     |
     v
POD
     |
     v
PROCESS
     |
     v
EXIT
```

---

# 18. Batch Pod

A batch pod SHOULD terminate after completing its workload.

---

# 19. Permanent Service for One Daily Job

A permanently running service SHOULD NOT exist solely to execute one simple daily batch if a CronJob provides a cleaner model.

---

# 20. Cron Expression

Cron schedules MUST be version controlled.

---

# 21. Time Zone

Cron scheduling MUST define time-zone semantics explicitly.

---

# 22. UTC

UTC SHOULD be preferred for infrastructure scheduling unless business-local-time semantics explicitly require another zone.

---

# 23. Business Local Time

If a job must run at:

```text
02:00 America/Sao_Paulo
```

the schedule MUST explicitly account for the business time zone.

---

# 24. DST

Daylight-saving/time-zone changes MUST be considered for local-time schedules.

---

# 25. Business Date

Business processing date MUST NOT be inferred blindly from current system timestamp.

---

# 26. Business Date Parameter

Critical batch processes SHOULD accept an explicit:

```text
businessDate
```

or equivalent processing period.

---

# 27. Example

Prefer:

```text
job run:
businessDate = 2026-07-26
```

over relying solely on:

```java
LocalDate.now()
```

inside processing logic.

---

# 28. Deterministic Reprocessing

Explicit business dates make reprocessing deterministic.

---

# 29. Job Parameters

Job parameters SHOULD uniquely identify a logical execution where restart/replay semantics require it.

---

# 30. Spring Batch

Spring Batch SHOULD be used for complex data-intensive batch workflows requiring features such as:

```text
Job / Step Model

Chunk Processing

Restartability

Execution Metadata

Retry / Skip

Partitioning

Listeners
```

---

# 31. Spring Batch Is Not Mandatory

Simple periodic maintenance tasks MUST NOT be forced into Spring Batch when the framework adds no meaningful value.

---

# 32. Job

A Spring Batch Job represents one logical batch workflow.

---

# 33. Step

A Step SHOULD represent one cohesive stage.

Example:

```text
READ INVOICES

TRANSFORM

PERSIST

PUBLISH RESULT
```

---

# 34. Giant Step

A single step containing the entire complex workflow SHOULD be decomposed when restart or failure boundaries differ.

---

# 35. Chunk Processing

Large datasets SHOULD use chunk-oriented processing where appropriate.

Conceptually:

```text
READ N
  |
  v
PROCESS N
  |
  v
WRITE N
  |
  v
COMMIT
  |
  v
NEXT N
```

---

# 36. Chunk Size

Chunk size MUST be bounded.

---

# 37. Maximum Chunk

Very large chunks can increase:

```text
Memory Usage

Transaction Duration

Rollback Cost

Lock Duration
```

---

# 38. Tiny Chunk

Extremely small chunks can create excessive:

```text
Commits

Round Trips

Metadata Overhead
```

---

# 39. Chunk Tuning

Chunk size SHOULD be evidence-driven.

---

# 40. Memory Model

Batch jobs MUST have a bounded memory model.

---

# 41. Load All Rows

This is prohibited for large datasets:

```text
SELECT ALL
   |
   v
LOAD MILLIONS
INTO JVM HEAP
```

---

# 42. Streaming / Paging Reader

Large readers SHOULD use:

```text
Pagination

Cursor

Streaming

Chunked Files
```

as appropriate.

---

# 43. Database Cursor

Long-lived DB cursors require explicit analysis of:

```text
Connection Duration

Transaction Duration

Failure Recovery
```

---

# 44. Paging

Paging readers SHOULD use deterministic ordering.

---

# 45. Keyset

Large batch traversal MAY use keyset pagination to avoid increasingly expensive offsets.

---

# 46. Restartability

Critical long-running jobs SHOULD be restartable.

---

# 47. Restart Meaning

Restartability means:

```text
PROCESS CRASHES AT 70%
        |
        v
RESTART
        |
        v
CONTINUE SAFELY
```

rather than always starting from zero.

---

# 48. Checkpoint

Checkpoint state SHOULD record sufficient progress for safe continuation.

---

# 49. Checkpoint Durability

Checkpoints MUST be durable when restart semantics require them.

---

# 50. In-Memory Checkpoint

In-memory-only progress is insufficient for critical restartable workloads.

---

# 51. Idempotency

Every scheduled/batch workload MUST define idempotency semantics.

---

# 52. Repeated Schedule

A job may run twice because of:

```text
Manual Reprocessing

Scheduler Retry

Node Failure

Duplicate Trigger

Operator Action

Ambiguous Completion
```

---

# 53. Idempotent Result

Repeated execution for the same logical business period SHOULD not produce duplicate unintended effects.

---

# 54. Example

If generating invoices for one business date:

```text
RUN 1
businessDate=2026-07-26

RUN 2
businessDate=2026-07-26
```

MUST NOT create duplicate invoices unless business rules explicitly require them.

---

# 55. Natural Business Key

A unique business key MAY enforce idempotency.

Example:

```text
customer_id
+
business_date
+
invoice_type
```

---

# 56. Unique Constraint

Database uniqueness SHOULD protect critical batch idempotency where appropriate.

---

# 57. Check-Then-Insert

This is not sufficient alone under concurrency:

```text
IF NOT EXISTS
    INSERT
```

---

# 58. Atomic Constraint

The database SHOULD be the final protection against duplicate concurrent insertion.

---

# 59. Execution Identity

Each physical execution SHOULD have a unique:

```text
executionId
```

while a separate logical business key identifies the job instance.

---

# 60. Job Instance vs Execution

Conceptually:

```text
JOB INSTANCE
businessDate=2026-07-26
      |
      +--> EXECUTION 1 FAILED
      |
      +--> EXECUTION 2 COMPLETED
```

---

# 61. Overlap

Every recurring job MUST define whether overlapping executions are allowed.

---

# 62. Prevent Overlap

If overlap is unsafe:

```text
RUN A
  |
  | still running
  |
NEXT SCHEDULE
  |
  X
DO NOT START RUN B
```

---

# 63. CronJob concurrencyPolicy

Kubernetes CronJob SHOULD configure an explicit `concurrencyPolicy`.

Possible semantics:

```text
Allow

Forbid

Replace
```

---

# 64. Forbid

`Forbid` SHOULD be used when overlap is unsafe and skipping the overlapping scheduled start is acceptable.

---

# 65. Replace

`Replace` SHOULD only be used when interrupting the currently running job is safe.

---

# 66. Allow

`Allow` requires the job itself to be concurrency-safe.

---

# 67. Default Assumption

Overlap MUST NOT remain an accidental default.

---

# 68. Distributed Lock

Distributed locking MAY prevent concurrent logical execution where external scheduling is not sufficient.

---

# 69. ShedLock

ShedLock MAY be used for simple Spring-scheduled distributed locks when its database/cache-based semantics are sufficient.

---

# 70. ShedLock Limitation

ShedLock coordinates execution.

It does NOT make the business operation idempotent.

---

# 71. Lock Is Not Idempotency

```text
LOCK
```

prevents some concurrent executions.

```text
IDEMPOTENCY
```

protects repeated logical execution.

Both concerns are distinct.

---

# 72. Lock Duration

Distributed locks MUST have bounded duration/lease semantics.

---

# 73. Permanent Lock

A process crash MUST NOT leave a permanent lock preventing future execution.

---

# 74. LockAtMostFor

Maximum lock duration MUST exceed expected execution time with justified margin when lease-based scheduling is used.

---

# 75. Too Short Lease

If lock lease expires while work is still active:

```text
JOB A STILL RUNNING
      |
LEASE EXPIRES
      |
JOB B STARTS
```

causing overlap.

---

# 76. Too Long Lease

Excessive leases delay recovery after process failure.

---

# 77. LockAtLeastFor

Minimum lock duration MAY prevent excessively frequent execution for short jobs.

---

# 78. Leader Election

Leader election MAY be used when one replica should perform recurring coordination work.

---

# 79. Leader Workload

Leader election is suitable for:

```text
Lightweight Coordination

Continuous Singleton Background Work
```

---

# 80. Leader Loss

Work MUST tolerate leader loss and re-election.

---

# 81. Leader Is Not Durable Job State

Leader identity MUST NOT be the only storage of workflow progress.

---

# 82. Quartz

Quartz MAY be used when scheduling requirements exceed Kubernetes CronJob / simple Spring scheduling.

---

# 83. Quartz Candidate

Quartz MAY be justified for:

```text
Dynamic Runtime Schedules

Persistent Job Definitions

Calendar Rules

Advanced Misfire Semantics

Application-Managed Scheduling
```

---

# 84. Quartz Not Default

Quartz SHOULD NOT be introduced merely to execute static cron schedules already well served by Kubernetes.

---

# 85. Quartz Cluster

Quartz clustering requires a correctly configured shared job store when multiple application instances participate.

---

# 86. Scheduler Ownership

Scheduling logic MUST have one clear owner.

---

# 87. Duplicate Schedulers

The same logical job MUST NOT be independently scheduled by:

```text
Kubernetes CronJob
+
Quartz
+
@Scheduled
```

simultaneously.

---

# 88. Misfire

A misfire occurs when a scheduled execution cannot start at the intended time.

---

# 89. Misfire Policy

Every critical schedule MUST define what happens when execution is missed.

Possible semantics:

```text
SKIP

RUN IMMEDIATELY

CATCH UP ALL MISSED PERIODS

RUN LATEST ONLY
```

---

# 90. Catch-Up

Catch-up behavior MUST be business-driven.

---

# 91. Example

A reconciliation job may safely run once after downtime.

A daily financial closing job may require processing each missed business date individually.

---

# 92. `startingDeadlineSeconds`

Kubernetes CronJobs SHOULD configure missed-schedule handling where appropriate.

---

# 93. Long Scheduler Outage

After a long scheduler outage, uncontrolled creation of many missed Jobs SHOULD be prevented.

---

# 94. Catch-Up Storm

Example:

```text
SCHEDULER DOWN 24 HOURS
       |
       v
1440 MISSED MINUTE JOBS
       |
       X
DO NOT CREATE
UNCONTROLLED STORM
```

unless explicitly required.

---

# 95. Retry

Batch retries MUST distinguish transient from permanent failures.

---

# 96. Retryable

Potentially retryable failures include:

```text
Temporary Database Connectivity

HTTP Timeout

Rate Limit

Transient Broker Failure
```

when operations are safe to retry.

---

# 97. Non-Retryable

Normally non-retryable:

```text
Invalid Input Record

Business Rule Violation

Unsupported Format

Permanent Configuration Error
```

---

# 98. Retry Bound

Retries MUST be bounded.

---

# 99. Retry per Item

Spring Batch retry MAY apply to individual items when appropriate.

---

# 100. Retry Entire Job

Restarting an entire large job for one transient item failure SHOULD be avoided when finer-grained recovery is available.

---

# 101. Skip

Item skipping MAY be allowed for explicitly acceptable bad records.

---

# 102. Skip Policy

Skip semantics MUST be defined by business rules.

---

# 103. Unlimited Skip

A job MUST NOT report success after skipping an uncontrolled percentage of invalid records.

---

# 104. Skip Limit

Skip limits SHOULD be bounded.

---

# 105. Reject File

For some file imports, one invalid record may require rejecting the entire file.

---

# 106. Partial File

For other integrations, valid records MAY continue while invalid records are reported separately.

---

# 107. Contract Explicitness

Partial-success semantics MUST be explicit.

---

# 108. Dead Letter

Unprocessable asynchronous batch items MAY use a DLQ/reject store where appropriate.

---

# 109. Retry Metadata

Failure metadata SHOULD include:

```text
Item Identity

Attempt Count

Safe Error Classification

Timestamp
```

without sensitive payload leakage.

---

# 110. Transaction Boundary

Batch transaction boundaries SHOULD align with chunk/recovery semantics.

---

# 111. Huge Transaction

One transaction for millions of records is prohibited.

---

# 112. Commit Frequency

Commit frequency SHOULD balance:

```text
Throughput

Recovery Granularity

Lock Duration

Transaction Overhead
```

---

# 113. Remote Calls in Batch Transaction

Remote calls SHOULD NOT remain inside long database transactions.

---

# 114. External Enrichment

If batch processing requires external data, batch retrieval SHOULD be preferred over per-row calls.

---

# 115. Remote N+1

This MUST be avoided:

```text
FOR EACH OF 1,000,000 ROWS
    CALL CUSTOMER API
```

---

# 116. Batch External API

External bulk APIs SHOULD be used where available.

---

# 117. Parallelism

Batch steps MAY process independent work concurrently.

---

# 118. Bounded Parallelism

Parallelism MUST remain bounded.

---

# 119. Virtual Threads

Java 21 Virtual Threads MAY simplify I/O-heavy batch concurrency.

---

# 120. Virtual Thread Limitation

Virtual Threads MUST NOT create unlimited concurrent requests against:

```text
Database

HTTP API

S3

SQS

Other Finite Dependencies
```

---

# 121. Concurrency Budget

Batch concurrency SHOULD consider:

```text
DB Pool Size

Remote Service Capacity

Broker Capacity

CPU

Memory

Network
```

---

# 122. Partitioning

Large workloads MAY be partitioned.

---

# 123. Partition Key

Partitions SHOULD be deterministic and non-overlapping.

Examples:

```text
ID RANGE

REGION

CUSTOMER GROUP

DATE RANGE
```

---

# 124. Duplicate Partition

Two workers MUST NOT process the same partition unintentionally.

---

# 125. Partition Completeness

Partitioning MUST ensure all required data is processed.

---

# 126. Hot Partition

Uneven partitions may create long tails.

---

# 127. Dynamic Partitioning

Dynamic partitioning MAY improve load distribution where complexity is justified.

---

# 128. Database Query

Batch queries MUST use indexes appropriate to their traversal strategy.

---

# 129. Pagination

Offset pagination SHOULD be avoided for extremely deep batch traversal where keyset/ordered ranges provide better performance.

---

# 130. Ordering

Batch processing MUST define whether processing order matters.

---

# 131. Order-Independent Work

Order-independent items SHOULD not introduce unnecessary global ordering constraints.

---

# 132. Ordered Processing

If business order matters, the partitioning/concurrency model MUST preserve it.

---

# 133. File Processing

File-based jobs require explicit file lifecycle states.

---

# 134. File State

Suggested states:

```text
RECEIVED

VALIDATING

PROCESSING

PROCESSED

FAILED

PARTIALLY_PROCESSED
```

---

# 135. File Claim

Multiple workers MUST NOT process the same file simultaneously unless explicitly partitioned.

---

# 136. Atomic Claim

File work SHOULD use an atomic claim/lease mechanism.

---

# 137. File Completion

A file MUST NOT be marked:

```text
PROCESSED
```

before required durable work completes.

---

# 138. File Archive

Successfully processed files SHOULD move to controlled archival/retention according to business requirements.

---

# 139. Failed File

Failed files SHOULD have controlled reprocessing semantics.

---

# 140. Filename Idempotency

Filename alone SHOULD NOT always be treated as a globally unique logical identity.

---

# 141. File Fingerprint

Hash, source identifier or business reference MAY improve duplicate-file detection.

---

# 142. Input Immutability

A file must not change while being processed without explicit version semantics.

---

# 143. Job Metadata

Critical jobs MUST persist sufficient execution metadata.

---

# 144. Metadata Fields

Applicable metadata includes:

```text
jobName

jobInstanceId

executionId

businessDate

startedAt

finishedAt

status

attempt

processedCount

failedCount

skippedCount
```

---

# 145. Job Status

Canonical job statuses MAY include:

```text
PENDING

STARTED

COMPLETED

FAILED

STOPPING

STOPPED

ABANDONED
```

depending on framework semantics.

---

# 146. Status Integrity

A job MUST NOT report COMPLETED if required work failed.

---

# 147. Partial Success

Partial-success status SHOULD be explicit where supported.

---

# 148. Status Endpoint

Operational APIs MAY expose job status.

---

# 149. Admin Endpoint

Manual job execution endpoints MUST be protected by strong authorization.

---

# 150. Manual Trigger

Manual execution SHOULD require explicit parameters.

---

# 151. Default Current Date

A manual production reprocessing endpoint SHOULD NOT silently default to today's date when business date matters.

---

# 152. Dry Run

High-risk batch processes MAY support a dry-run mode.

---

# 153. Dry Run Semantics

Dry run MUST not produce real side effects.

---

# 154. Dry Run Audit

Operational dry runs SHOULD be auditable where relevant.

---

# 155. Reprocessing

Reprocessing MUST be designed before production incidents occur.

---

# 156. Reprocessing Scope

Operators SHOULD be able to target:

```text
One Execution

One Business Date

One File

One Partition

One Failed Item Set
```

where business architecture permits.

---

# 157. Replay All

"Replay everything" SHOULD NOT be the only recovery mechanism.

---

# 158. Manual Data Fix

Manual direct data modification SHOULD remain exceptional.

---

# 159. Operational Control

Job reprocessing SHOULD use controlled application/batch interfaces instead of arbitrary SQL where practical.

---

# 160. Deployment

Scheduled jobs must behave correctly during application deployment.

---

# 161. `@Scheduled` During Rolling Update

An in-process scheduled job may execute in:

```text
OLD POD

NEW POD
```

simultaneously during rolling deployment.

---

# 162. Distributed Lock During Deployment

Distributed coordination MUST account for mixed-version execution.

---

# 163. Mixed Job Versions

Two different application versions SHOULD NOT process the same logical job concurrently unless backward compatibility is explicitly designed.

---

# 164. CronJob Image Version

Kubernetes CronJob SHOULD reference an immutable application image version.

---

# 165. Latest Image

Production CronJobs MUST NOT rely on mutable:

```text
latest
```

image tags.

---

# 166. Job Rollout

Changing a CronJob template affects future Jobs, not necessarily currently running Jobs.

---

# 167. Running Old Job

Deployment processes MUST account for jobs already running on previous versions.

---

# 168. Forced Termination

Long-running batch jobs SHOULD NOT be terminated during deployment without restart/recovery semantics.

---

# 169. Graceful Shutdown

Batch workloads MUST respond correctly to termination signals.

---

# 170. Stop New Chunk

On shutdown, a batch SHOULD stop starting new units/chunks after a safe boundary.

---

# 171. Complete Current Chunk

Current transaction/chunk SHOULD complete if within shutdown budget.

---

# 172. Rollback Current Chunk

If completion is impossible, transactional work SHOULD rollback and become restartable.

---

# 173. Shutdown Timeout

Batch shutdown MUST remain bounded.

---

# 174. Kubernetes Job Deadline

Jobs SHOULD define execution deadlines where indefinite runtime is invalid.

---

# 175. `activeDeadlineSeconds`

`activeDeadlineSeconds` MAY bound Kubernetes Job runtime.

---

# 176. Job Timeout

Application-level timeouts MAY also be necessary for individual steps.

---

# 177. Deadline Is Failure

A job exceeding its valid processing window MUST become a visible failed/timed-out execution.

---

# 178. History Retention

Kubernetes Job history SHOULD be bounded.

---

# 179. Successful Job History

Retain only an operationally useful number of successful Jobs.

---

# 180. Failed Job History

Failed Jobs MAY be retained longer for diagnostics but still require bounds.

---

# 181. TTL After Finished

Finished Kubernetes Jobs MAY use TTL cleanup.

---

# 182. Application Metadata Retention

Business execution history retention may differ from Kubernetes pod/job retention.

---

# 183. Scheduler Metadata Is Not Audit

Kubernetes Job history alone is insufficient when formal business auditability is required.

---

# 184. Observability

Scheduled workloads MUST be observable.

---

# 185. Required Metrics

Critical jobs SHOULD expose:

```text
executions_started

executions_completed

executions_failed

duration

items_processed

items_failed

items_skipped

retry_count
```

---

# 186. Metric Labels

Use bounded dimensions such as:

```text
job_name

step_name

result
```

---

# 187. Execution ID Metric

Execution IDs MUST NOT be metric labels.

---

# 188. Business Date Metric

Business date SHOULD generally not become a high-cardinality metric label.

---

# 189. Job Logs

Logs SHOULD contain:

```text
jobName

executionId

businessDate

step

status

processed counts
```

where safe.

---

# 190. Per-Item Logging

Logging every successful row in high-volume batch processing SHOULD be avoided.

---

# 191. Error Logging

Failed items MAY be logged or stored with safe bounded diagnostic information.

---

# 192. Sensitive Input

Batch logs MUST follow data privacy standards.

---

# 193. Tracing

Distributed tracing MAY be useful for external calls made by batch jobs.

---

# 194. Million Spans

Creating one trace span for every record in a million-row batch SHOULD be avoided unless sampling/aggregation is intentionally designed.

---

# 195. Trace Granularity

Tracing SHOULD focus on:

```text
Job

Step

Chunk

Important Integration Calls
```

rather than every trivial item.

---

# 196. Alerting

Critical alerts SHOULD include:

```text
Job Did Not Start

Job Failed

Job Exceeded Duration

Job Overlap

Repeated Retry

Missed Business Date

Growing Failure Backlog
```

---

# 197. Missing Execution

Absence of expected job execution is itself an operational failure.

---

# 198. Completion Deadline

Time-sensitive jobs SHOULD have completion SLOs.

Example:

```text
Daily billing must complete
before 06:00 business time.
```

---

# 199. Freshness SLI

Batch systems MAY use data freshness as an SLI.

---

# 200. Job Heartbeat

Very long-running jobs MAY emit progress heartbeat/status updates.

---

# 201. Stuck Job

A job with no progress beyond a defined interval MUST become visible.

---

# 202. Progress

Progress SHOULD be based on durable work completed rather than only elapsed time.

---

# 203. Reconciliation

Critical batch results SHOULD support reconciliation where missing/duplicate processing would create material business impact.

---

# 204. Expected vs Actual

Conceptually:

```text
EXPECTED 10,000 RECORDS
        |
        v
PROCESSED 9,999
        |
        v
RECONCILIATION FAILURE
```

---

# 205. Batch Testing

Batch workloads require dedicated automated testing.

---

# 206. Job Unit Test

Business processors SHOULD have pure unit tests where possible.

---

# 207. Reader Test

Custom readers SHOULD verify:

```text
Ordering

Pagination

Restart Behavior

Boundary Conditions
```

---

# 208. Writer Test

Writers SHOULD verify:

```text
Persistence

Batching

Idempotency

Failure Behavior
```

---

# 209. Job Integration Test

Critical jobs SHOULD have end-to-end batch integration tests against representative infrastructure.

---

# 210. Testcontainers

PostgreSQL, SQS, Redis and other infrastructure-sensitive batch tests SHOULD use Testcontainers where applicable.

---

# 211. Restart Test

Critical restartable jobs MUST test:

```text
Fail Midway

Restart

Continue

No Duplicate Effects
```

---

# 212. Overlap Test

Singleton jobs SHOULD test concurrent start attempts.

---

# 213. Idempotency Test

Run the same logical job twice and verify no unintended duplicate effect.

---

# 214. Chunk Boundary Test

Chunk behavior SHOULD be tested near boundaries.

Example:

```text
chunk size = 100

99 items

100 items

101 items
```

---

# 215. Retry Test

Retry tests MUST verify bounded attempts.

---

# 216. Skip Test

Skip-enabled jobs SHOULD verify skip limits.

---

# 217. Business Date Test

Jobs MUST test explicit business-date processing.

---

# 218. Time Zone Test

Local-time schedules SHOULD test zone/DST-sensitive logic where applicable.

---

# 219. Shutdown Test

Critical jobs SHOULD test safe interruption and restart.

---

# 220. Concurrency Test

Parallel batch logic SHOULD verify maximum observed concurrency.

---

# 221. No `Thread.sleep`

Concurrency/batch tests SHOULD avoid `Thread.sleep` as synchronization.

---

# 222. AssertJ

Tests MUST follow established project conventions, including meaningful:

```java
.as("...")
```

descriptions before applicable assertions.

---

# 223. Job Architecture Review Checklist

Material jobs SHOULD evaluate:

```text
[ ] Is this actually a batch workload?

[ ] Should scheduling be Kubernetes-native?

[ ] Is @Scheduled sufficient?

[ ] Is Spring Batch justified?

[ ] Is Quartz justified?

[ ] Can multiple replicas execute this?

[ ] Is distributed coordination required?

[ ] Is overlap allowed?

[ ] What happens when a schedule is missed?

[ ] Is catch-up required?

[ ] Is business date explicit?

[ ] Is the job idempotent?

[ ] Is restartability required?

[ ] What is the checkpoint?

[ ] What is the chunk size?

[ ] Is memory bounded?

[ ] Are remote calls batched?

[ ] Is parallelism bounded?

[ ] What happens on invalid input?

[ ] Is retry allowed?

[ ] Is skip allowed?

[ ] How is partial success represented?

[ ] How is manual reprocessing performed?

[ ] What happens during deployment?

[ ] What happens on SIGTERM?

[ ] Is there a completion deadline?

[ ] How is missing execution detected?

[ ] Is reconciliation required?
```

---

# 224. Batch Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] Production CronJobs use immutable image tags

[ ] concurrencyPolicy explicitly configured

[ ] Long jobs define timeout/deadline

[ ] Job retries are bounded

[ ] Critical jobs persist execution metadata

[ ] Business-date jobs accept explicit date

[ ] High-volume jobs use bounded chunking

[ ] Batch processors have idempotency tests

[ ] No production job relies only on pod-local state

[ ] Failed execution generates observability signal
```

---

# 225. Enterprise Batch Gate

A scheduled workload is not considered compliant when applicable conditions include:

```text
[ ] @Scheduled runs independently on every replica unintentionally

[ ] Job assumes single application instance

[ ] Overlap semantics are undefined

[ ] Distributed lock can remain forever after crash

[ ] CronJob uses mutable latest image tag

[ ] Job processes millions of records in one transaction

[ ] Entire dataset is loaded into heap

[ ] Restart from partial failure creates duplicate effects

[ ] Business date depends only on LocalDate.now()

[ ] Missed-schedule behavior is undefined

[ ] Retry is unbounded

[ ] Invalid records are skipped without a limit

[ ] Remote N+1 exists across large dataset

[ ] Parallelism can exhaust DB/downstream service

[ ] Job is marked complete before durable work completes

[ ] Manual reprocessing requires arbitrary production SQL

[ ] Missing daily execution cannot be detected

[ ] Job shutdown can leave unrecoverable partial state
```

---

# 226. Anti-Patterns

The following are prohibited or strongly discouraged:

- `@Scheduled` on every replica without coordination
- permanent application solely for one simple cron task
- overlap left undefined
- distributed lock treated as idempotency
- infinite lock lease
- Quartz introduced for static simple CronJobs
- same job triggered by multiple scheduler technologies
- use of local system date as hidden business date
- loading entire large dataset into memory
- one huge batch transaction
- offset pagination across extremely deep batch traversal
- remote API call per record
- unbounded Virtual Thread fan-out
- retrying permanent bad input
- unlimited skip policy
- filename-only duplicate protection when insufficient
- job marked completed despite required failed items
- mutable `latest` image in production CronJob
- infinite job runtime
- per-record success logging in high-volume workloads
- relying only on Kubernetes Job history for business audit
- no mechanism to detect a job that never ran

---

# 227. Positive Consequences

The decision provides:

- predictable job execution
- safer multi-replica deployments
- controlled scheduling
- restartable batch workloads
- stronger idempotency
- bounded memory and transactions
- safer reprocessing
- clear business-date semantics
- reduced duplicate processing
- better operational diagnostics
- controlled job concurrency
- stronger reconciliation

---

# 228. Negative Consequences

The decision introduces:

- execution metadata
- scheduler configuration
- distributed coordination where necessary
- Spring Batch complexity for advanced workloads
- checkpoint/restart logic
- operational reprocessing controls
- additional monitoring

These costs are accepted because unattended workloads require stronger recovery guarantees than ad hoc scheduled methods provide.

---

# 229. Neutral Consequences

The decision also means:

- not every scheduled task needs Spring Batch
- not every batch needs Quartz
- not every job must be singleton
- not every invalid record must fail the whole job
- not every job needs parallelism
- not every job needs distributed locking when Kubernetes already creates one execution
- restartability may be more important than raw execution speed
- scheduler history and business execution history are different concerns

---

# 230. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate job execution | Critical | Medium | Coordination + idempotency |
| Overlapping runs | High | Medium | Explicit concurrency policy |
| Memory exhaustion | Critical | Medium | Chunking |
| Huge transaction | Critical | Medium | Chunk commits |
| Missed schedule | High | Medium | Misfire policy + alert |
| Duplicate restart effects | Critical | Medium | Idempotency/checkpoints |
| Lock stuck | High | Low/Medium | Lease expiry |
| Retry storm | High | Medium | Bounded retry |
| Remote N+1 | High | Medium | Batch integration |
| Undetected job failure | Critical | Medium | Completion monitoring |

---

# 231. Implementation Guidance

The following rules are mandatory:

1. Scheduled workloads must be classified before selecting scheduling technology.
2. Kubernetes CronJob should be preferred for independently scheduled Kubernetes batch workloads.
3. `@Scheduled` must not assume one application replica.
4. Complex restartable workloads should use Spring Batch or an equivalent explicit batch model.
5. Quartz should only be introduced for advanced dynamic scheduling requirements.
6. One logical job must have one authoritative scheduler.
7. Time-zone semantics must be explicit.
8. Business processing date must be explicit where business date matters.
9. Every recurring job must define overlap semantics.
10. Distributed coordination must be used when only one execution is allowed across replicas.
11. Distributed locks must use bounded leases.
12. Locking does not replace business idempotency.
13. Critical workloads must be safe to repeat or explicitly deduplicated.
14. Database uniqueness should protect critical natural business keys where appropriate.
15. Large jobs must have bounded memory.
16. Large datasets must be processed in chunks/pages/streams.
17. Long-running critical jobs should be restartable.
18. Checkpoint state must be durable where restartability requires it.
19. Retry and skip behavior must be bounded.
20. Large remote workloads must avoid per-item N+1 calls.
21. Batch concurrency must respect DB and downstream capacity.
22. Virtual Threads must not create unbounded downstream pressure.
23. File workloads must have explicit lifecycle and duplicate detection.
24. Critical jobs must persist execution metadata.
25. Manual reprocessing must be controlled and authorized.
26. Batch shutdown must preserve safe retry/restart semantics.
27. CronJobs must use immutable release images.
28. Job execution deadlines and history retention must be explicit.
29. Critical schedules must detect missing, failed and excessively long executions.
30. Batch processing must have automated restart, duplicate and boundary tests.
31. Existing applied Flyway migrations remain immutable; batch metadata/schema changes require new migrations.

---

# 232. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Scheduling
- Spring Batch
- Kubernetes CronJob
- Kubernetes Job
- Quartz where explicitly adopted
- ShedLock where appropriate
- PostgreSQL
- Flyway
- SQS
- SQS
- Testcontainers
- JUnit 5
- AssertJ
- deterministic concurrency tests
- CI/CD
- production job dashboards and alerts

---

# 233. Success Criteria

The decision is successful when:

- scheduled jobs no longer execute accidentally on every replica
- overlap behavior is explicit
- critical batch workloads recover from crashes
- repeated executions do not create duplicate business effects
- business-date reprocessing is deterministic
- large jobs remain memory bounded
- batch transactions remain bounded
- remote N+1 patterns are eliminated
- missed schedules are detectable
- failed/stuck executions are visible
- operators can safely reprocess targeted workloads
- deployments do not corrupt running batch work
- job completion and data freshness are measurable

---

# 234. Alternatives Rejected

## 234.1 `@Scheduled` for Every Batch

Rejected because multi-replica coordination, restartability and execution metadata become difficult.

---

## 234.2 Quartz for Every Schedule

Rejected because Kubernetes-native static scheduling is simpler for many workloads.

---

## 234.3 Distributed Lock as Only Correctness Mechanism

Rejected because lock loss, duplicate triggers and manual replay still require idempotent business processing.

---

## 234.4 One Transaction per Entire Job

Rejected because rollback, locks and memory grow without bounds.

---

## 234.5 Load Entire Dataset Into Memory

Rejected because production data volume is not predictably bounded.

---

## 234.6 Hidden Current-Date Processing

Rejected because reprocessing and time-zone correctness become ambiguous.

---

## 234.7 Retry Entire Job for Every Item Failure

Rejected because finer-grained recovery provides better efficiency and control for large workloads.

---

# 235. Related Decisions

This ADR extends and implements:

- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-076: Enterprise Database Migration, Zero-Downtime Schema Evolution and Expand/Contract Standard

---

# 236. References

- Spring Batch Documentation
- Spring Framework Scheduling Documentation
- Kubernetes CronJob Documentation
- Kubernetes Job Documentation
- Quartz Scheduler Documentation
- ShedLock Documentation
- Java 21 Documentation
- PostgreSQL Documentation
- Spring Data Documentation
- Amazon SQS Documentation
- AWS SQS Documentation
- Google Site Reliability Engineering
- Enterprise Integration Patterns

---

# 237. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | AstraForge Supply Platform Architecture Team | Approved | Initial enterprise scheduling and batch-processing baseline |

---

# 238. Decision Summary

Scheduler selection becomes:

```text
WORKLOAD
   |
   v
SIMPLE SHORT
PERIODIC TASK?
   |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
@Scheduled  RESTARTABLE /
MAY APPLY   DATA HEAVY?
               |
             +-+-+
             |   |
            YES  NO
             |   |
             v   v
         SPRING  K8S
         BATCH   JOB /
                 CRONJOB
```

In Kubernetes, singleton scheduling becomes:

```text
CRON SCHEDULE
      |
      v
ONE CRONJOB
      |
      v
ONE JOB
      |
      v
ONE OR CONTROLLED
SET OF WORKERS
```

instead of:

```text
3 SERVICE PODS
    |
    +--> JOB
    +--> JOB
    +--> JOB
```

Idempotency becomes:

```text
LOGICAL JOB
businessDate=X
     |
     v
RUN 1
     |
     v
PARTIAL FAILURE
     |
     v
RUN 2
     |
     v
SAME LOGICAL RESULT
WITHOUT DUPLICATE EFFECT
```

Large processing becomes:

```text
DATASET
   |
   v
CHUNK 1
   |
COMMIT
   |
CHUNK 2
   |
COMMIT
   |
CHUNK N
```

rather than:

```text
MILLIONS OF ROWS
      |
      v
ONE TRANSACTION
```

Restartability becomes:

```text
0%
 |
 v
25%
 |
 v
50%
 |
 X
CRASH
 |
 v
RESTART
 |
 v
RESUME FROM SAFE
CHECKPOINT
```

Overlap becomes explicit:

```text
NEXT SCHEDULE
     |
     v
PREVIOUS JOB RUNNING?
     |
   +-+-+
   |   |
  NO  YES
   |   |
   v   v
START POLICY:
      ALLOW /
      FORBID /
      REPLACE
```

Missed schedules become:

```text
SCHEDULE MISSED
      |
      v
BUSINESS POLICY
      |
      +--> SKIP
      +--> RUN NOW
      +--> PROCESS EACH PERIOD
      +--> LATEST ONLY
```

Business date becomes:

```text
SCHEDULER TIME
      !=
BUSINESS PROCESSING DATE
```

and therefore:

```text
JOB
 |
 +--> executionId
 |
 +--> businessDate
 |
 +--> attempt
```

Batch concurrency becomes:

```text
VIRTUAL THREADS
      |
      X
      |
DO NOT MEAN
      |
      v
UNLIMITED DB /
API CONCURRENCY
```

Operational control becomes:

```text
JOB
 |
 +--> STARTED
 |
 +--> PROGRESS
 |
 +--> COMPLETED
 |
 +--> FAILED
 |
 +--> REPROCESS
```

with durable execution metadata.

The complete batch equation is:

```text
EXPLICIT SCHEDULER OWNERSHIP
        +
EXPLICIT BUSINESS DATE
        +
OVERLAP POLICY
        +
IDEMPOTENT PROCESSING
        +
BOUNDED CHUNKING
        +
DURABLE CHECKPOINTS
        +
RESTARTABILITY
        +
BOUNDED RETRIES
        +
BOUNDED CONCURRENCY
        +
CONTROLLED REPROCESSING
        +
JOB METADATA
        +
COMPLETION MONITORING
        +
RECONCILIATION
        =
RELIABLE ENTERPRISE BATCH PROCESSING
```

The governing principle is:

```text
Do not confuse
a cron expression
with a batch architecture.

Choose the scheduler
for the workload.

Do not assume
one application replica.

Make overlap explicit.

Use a distributed lock
only when coordination
is actually required.

Do not confuse locking
with idempotency.

Make critical jobs
safe to rerun.

Use explicit business dates.

Process large datasets
in bounded chunks.

Commit progress regularly.

Do not load the world
into memory.

Persist restart state.

Retry only transient failures.

Bound every retry.

Bound every skip policy.

Batch remote calls.

Bound parallelism.

Virtual Threads do not make
databases infinite.

Give files explicit
processing states.

Keep execution metadata.

Detect jobs that failed.

Detect jobs that never ran.

Detect jobs that stopped
making progress.

Make reprocessing
a supported operation,
not an emergency SQL script.

And design every batch
assuming that it may fail

after processing exactly
one item more than the
last checkpoint you expected.
```
