# ADR-060: Adopt Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-060 |
| Title | Adopt Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard |
| Status | Accepted |
| Date | 2026-07-25 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | AWS, Kubernetes, Containers, Java 21, Spring Boot, Runtime Engineering |
| Related Work Items | EKS, Docker, Kubernetes, IAM, IRSA, Autoscaling, Deployment, Production Readiness |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise Java services execute inside a distributed cloud runtime.

The application is only one component of the production system:

```text
CLIENT
   |
   v
AWS LOAD BALANCER
   |
   v
KUBERNETES SERVICE
   |
   v
PODS
   |
   v
JAVA 21 / SPRING BOOT
   |
   +--> PostgreSQL / RDS
   +--> Redis / ElastiCache
   +--> Amazon SQS
   +--> SQS
   +--> External APIs
```

Production reliability therefore depends on more than application code.

It also depends on:

```text
Container Images

CPU / Memory Limits

Health Probes

Graceful Shutdown

Replica Distribution

Autoscaling

IAM

Secrets

Network Configuration

Deployment Strategy

JVM Configuration

Dependency Capacity
```

A service that behaves correctly locally may fail in Kubernetes because of incorrect runtime assumptions.

---

# 2. Problem Statement

The organization requires standards covering:

- OCI/container images
- Dockerfiles
- image security
- Kubernetes
- EKS
- Deployments
- Services
- ConfigMaps
- Secrets
- requests
- limits
- CPU throttling
- memory limits
- JVM heap
- Java 21
- Virtual Threads
- startup probes
- readiness probes
- liveness probes
- graceful shutdown
- terminationGracePeriodSeconds
- rolling deployments
- zero downtime
- rollback
- HPA
- PDB
- topology spread
- IAM
- IRSA
- AWS services
- SQS
- MSK
- RDS
- ElastiCache
- load balancers
- production readiness

---

# 3. Decision Drivers

Primary drivers are:

1. availability
2. predictable resource usage
3. security
4. deployment safety
5. scalability
6. graceful degradation
7. operational simplicity
8. cost efficiency
9. observability
10. recoverability
11. portability
12. production readiness

---

# 4. Decision

Enterprise workloads SHOULD run as immutable OCI-compatible containers orchestrated through Kubernetes/EKS where the platform requires container orchestration.

The canonical runtime is:

```text
SOURCE CODE
    |
    v
CI BUILD
    |
    v
TEST / SAST / QUALITY GATES
    |
    v
OCI IMAGE
    |
    v
CONTAINER REGISTRY
    |
    v
KUBERNETES DEPLOYMENT
    |
    v
PODS
    |
    v
JAVA 21 APPLICATION
```

Infrastructure and runtime configuration MUST be version controlled and reproducible.

---

# 5. Fundamental Principle

The governing principle is:

```text
Production behavior is the result of

APPLICATION
    +
JVM
    +
CONTAINER
    +
KUBERNETES
    +
CLOUD INFRASTRUCTURE
    +
DEPENDENCIES

not application code alone.
```

---

# 6. Immutable Container

Application containers MUST be immutable after build.

---

# 7. Environment-Neutral Image

The same application image SHOULD progress through:

```text
DEV

TEST

HML

PRD
```

with environment differences supplied through runtime configuration.

---

# 8. Rebuild Per Environment

Rebuilding application binaries/images separately for each environment SHOULD be avoided.

---

# 9. OCI Image

Container images MUST follow OCI-compatible standards supported by the enterprise runtime.

---

# 10. Minimal Image

Runtime images SHOULD contain only components required to execute the application.

---

# 11. Build Tools

Production runtime images SHOULD NOT contain unnecessary:

```text
Compilers

Maven

Gradle

Source Code

Debug Utilities
```

---

# 12. Multi-Stage Build

Docker multi-stage builds SHOULD be used where they reduce runtime image content.

---

# 13. Base Image

Base images MUST come from approved sources.

---

# 14. Image Version

Base-image versions SHOULD be explicitly controlled.

---

# 15. latest

Production images MUST NOT depend on mutable:

```text
latest
```

tags for deterministic deployment.

---

# 16. Immutable Image Reference

Production deployment SHOULD use:

```text
Version Tag

and/or

Image Digest
```

---

# 17. Image Scanning

Container images MUST pass approved vulnerability scanning before production promotion.

---

# 18. Critical Vulnerability

Critical/high vulnerabilities MUST follow enterprise remediation policy before deployment.

---

# 19. Non-Root

Application containers SHOULD run as a non-root user.

---

# 20. Root Container

Running the application as root requires explicit justification.

---

# 21. Read-Only Filesystem

A read-only root filesystem SHOULD be used where application requirements permit.

---

# 22. Temporary Files

Temporary writable data SHOULD use explicit writable locations/volumes.

---

# 23. Container Persistence

Application containers MUST NOT rely on local container filesystem for durable business data.

---

# 24. Stateless Application

Application pods SHOULD remain stateless where practical.

---

# 25. Durable State

Durable state belongs in approved external systems such as:

```text
RDS

S3

SQS

Approved Persistent Storage
```

---

# 26. Kubernetes Deployment

Stateless application services SHOULD normally use Kubernetes `Deployment`.

---

# 27. Replica Count

Production services SHOULD normally run multiple replicas when availability requirements demand it.

---

# 28. Single Replica

A critical service with one replica represents an availability risk and requires explicit justification.

---

# 29. Requests

Every production workload MUST define resource requests.

At minimum:

```text
CPU Request

Memory Request
```

---

# 30. Limits

Resource limits MUST follow platform policy and workload behavior.

---

# 31. Missing Resource Configuration

Production workloads MUST NOT rely indefinitely on implicit/unbounded scheduling assumptions.

---

# 32. CPU Request

CPU request SHOULD approximate the sustained CPU capacity required by the workload.

---

# 33. CPU Limit

CPU limits require careful evaluation because exceeding the limit can result in throttling.

---

# 34. CPU Throttling

CPU throttling can manifest as:

```text
Increased Latency

Timeouts

Reduced Throughput

Longer GC

Slower Startup
```

without an application exception.

---

# 35. CPU Limit Policy

CPU limits SHOULD be determined through platform policy and performance testing rather than arbitrary symmetry with CPU requests.

---

# 36. Memory Limit

Containers MUST have memory boundaries appropriate to workload and cluster policy.

---

# 37. OOMKilled

When a container exceeds its cgroup memory limit, Kubernetes may terminate it as:

```text
OOMKilled
```

---

# 38. JVM Memory

Container memory is not equivalent to Java heap.

---

# 39. Container Memory Components

Container memory includes:

```text
Java Heap

Metaspace

Thread Stacks

Direct Buffers

Code Cache

Native Libraries

GC Structures

JVM Native Memory
```

---

# 40. Heap Headroom

The JVM heap MUST leave headroom for non-heap/native memory.

---

# 41. Unsafe Configuration

Avoid assuming:

```text
Container Limit = 2 GiB

-Xmx = 2 GiB
```

is safe.

---

# 42. Percentage-Based Heap

Modern Java container-aware heap configuration MAY use:

```text
-XX:MaxRAMPercentage
```

when it provides predictable runtime behavior.

---

# 43. Java Container Awareness

Java 21 container awareness MUST remain enabled unless there is an exceptional reason to override it.

---

# 44. Xmx

Explicit `-Xmx` MAY be used when predictable heap sizing is required.

---

# 45. Heap Sizing

Heap sizing MUST be validated under representative production load.

---

# 46. Native Memory

Native memory consumption SHOULD be evaluated for memory-sensitive services.

---

# 47. Direct Memory

Services using Netty/WebClient or other direct-buffer technologies MUST consider off-heap/direct memory.

---

# 48. Thread Stack

Thread-stack memory remains relevant even with modern concurrency models.

---

# 49. Virtual Threads

Java 21 Virtual Threads MAY be used for high-concurrency blocking workloads.

---

# 50. Virtual Threads Are Not Capacity

Virtual Threads reduce the cost of waiting threads.

They do NOT create additional:

```text
CPU

Database Connections

HTTP Connections

FIFO MessageGroupIds

Memory

Downstream Capacity
```

---

# 51. Bounded Dependencies

Virtual Thread concurrency MUST remain bounded at scarce downstream resources.

Example:

```text
10,000 Virtual Threads
        |
        v
50 Database Connections
        |
        v
PostgreSQL
```

---

# 52. Executor Explosion

Applications MUST NOT create uncontrolled executors/thread pools for every integration.

---

# 53. CPU-Bound Work

CPU-bound work SHOULD use concurrency appropriate to available CPU rather than enormous Virtual Thread fan-out.

---

# 54. Container CPU

Java concurrency settings MUST account for CPU allocated to the container.

---

# 55. ActiveProcessorCount

`-XX:ActiveProcessorCount` MAY be used when platform/container CPU detection requires explicit control.

---

# 56. Startup Probe

Applications with potentially long initialization SHOULD define a startup probe.

---

# 57. Startup Probe Purpose

Startup probe answers:

```text
Has the application finished starting?
```

---

# 58. Readiness Probe

Every production HTTP service MUST define readiness semantics.

---

# 59. Readiness Purpose

Readiness answers:

```text
Can this instance safely receive traffic now?
```

---

# 60. Liveness Probe

Liveness answers:

```text
Is the process sufficiently healthy
that restarting it may restore service?
```

---

# 61. Probe Separation

Startup, readiness and liveness MUST NOT be treated as interchangeable.

---

# 62. Liveness Dependency Check

Liveness SHOULD NOT normally fail merely because an external dependency such as:

```text
Redis

SQS

Remote HTTP API
```

is temporarily unavailable.

---

# 63. Restart Storm

If liveness depends on an unavailable external system:

```text
Dependency Down
     |
     v
Liveness Fails
     |
     v
Pod Restart
     |
     v
Dependency Still Down
     |
     v
Restart Loop
```

---

# 64. Readiness Dependency

Readiness MAY consider critical dependencies when the instance truly cannot serve useful traffic without them.

---

# 65. Graceful Degradation

If the service can operate in degraded mode, readiness SHOULD reflect actual service capability rather than requiring every optional dependency.

---

# 66. Probe Timeout

Probe timeouts MUST be bounded.

---

# 67. Probe Frequency

Probe intervals MUST avoid unnecessary application/platform load.

---

# 68. Spring Boot Actuator

Spring Boot Actuator health groups SHOULD be used to implement appropriate Kubernetes probe semantics.

---

# 69. Probe Security

Health endpoints MUST NOT expose sensitive diagnostic data publicly.

---

# 70. Graceful Shutdown

Applications MUST support graceful termination.

---

# 71. Kubernetes Termination

Typical flow:

```text
SIGTERM
   |
   v
POD BECOMES UNAVAILABLE
   |
   v
STOP ACCEPTING NEW WORK
   |
   v
FINISH IN-FLIGHT WORK
   |
   v
APPLICATION EXITS
```

---

# 72. Spring Graceful Shutdown

Spring Boot graceful shutdown SHOULD be enabled for HTTP workloads.

---

# 73. terminationGracePeriodSeconds

Kubernetes termination grace period MUST be long enough for normal in-flight work to complete.

---

# 74. Excessive Grace Period

Termination grace period MUST NOT be arbitrarily enormous.

---

# 75. Shutdown Timeout Alignment

These values MUST be aligned:

```text
Load Balancer Drain

Kubernetes Termination Grace

Spring Shutdown Timeout

Request Timeout
```

---

# 76. SIGKILL

If the process exceeds termination grace, Kubernetes may force termination.

---

# 77. preStop

A `preStop` hook MAY be used where required to improve traffic-draining behavior.

---

# 78. Fixed Sleep preStop

Large arbitrary `sleep` hooks SHOULD NOT substitute for correct endpoint removal and graceful shutdown.

---

# 79. Background Workers

SQS/background consumers MUST also stop gracefully.

---

# 80. Consumer Shutdown

Consumer shutdown SHOULD:

```text
Stop Fetching New Work

Finish or Safely Abandon Current Work

Preserve Delivery Semantics

Commit/Acknowledge Correctly
```

---

# 81. Rolling Deployment

Rolling deployment is the preferred default deployment strategy.

---

# 82. Zero-Downtime Goal

Production deployment SHOULD preserve service availability when architecture and capacity allow it.

---

# 83. maxUnavailable

Critical services SHOULD use rollout settings that avoid removing excessive healthy capacity simultaneously.

---

# 84. maxSurge

`maxSurge` SHOULD provide temporary deployment capacity where cluster capacity permits.

---

# 85. Readiness During Rollout

A new pod MUST NOT receive production traffic before it becomes ready.

---

# 86. Deployment Compatibility

During rolling deployment:

```text
OLD VERSION
     +
NEW VERSION
```

will coexist.

Contracts MUST tolerate this period.

---

# 87. Database Compatibility

Database changes MUST follow ADR-058 expand/contract principles.

---

# 88. SQS Compatibility

Event contracts MUST follow ADR-057 compatibility rules.

---

# 89. Redis Compatibility

Shared cache structures MUST follow ADR-059 rolling-deployment compatibility rules.

---

# 90. REST Compatibility

REST APIs MUST follow ADR-056 backward-compatibility requirements.

---

# 91. Rollback

Application rollback MUST remain operationally possible.

---

# 92. Database Rollback Limitation

Application rollback does not automatically reverse database migrations.

---

# 93. Forward-Compatible Schema

Database schema MUST therefore remain compatible with the previous application version during the defined rollback window where required.

---

# 94. Deployment Failure

Deployment automation MUST detect:

```text
Failed Startup

Readiness Failure

CrashLoopBackOff

Image Pull Failure

Insufficient Capacity
```

---

# 95. Automated Rollback

Automated rollback MAY be used when platform deployment tooling safely supports it.

---

# 96. Canary

Canary deployment SHOULD be considered for high-risk services/releases.

---

# 97. Canary Flow

```text
NEW VERSION
    |
    v
SMALL TRAFFIC %
    |
    v
OBSERVE
    |
    +--> HEALTHY --> INCREASE
    |
    +--> UNHEALTHY --> ABORT
```

---

# 98. Blue/Green

Blue/green MAY be used when stronger environment isolation or rapid traffic switching is required.

---

# 99. Deployment Strategy

Deployment strategy SHOULD reflect:

```text
Risk

Traffic

State

Database Compatibility

Rollback Requirements

Infrastructure Cost
```

---

# 100. PodDisruptionBudget

Critical multi-replica workloads SHOULD define an appropriate PDB.

---

# 101. PDB Purpose

PDB protects minimum application availability during voluntary disruptions.

---

# 102. PDB Is Not HA

PDB does not protect against every node/application failure.

---

# 103. Impossible PDB

PDB settings MUST NOT make routine cluster maintenance impossible.

---

# 104. Pod Distribution

Critical replicas SHOULD be distributed to reduce correlated failure.

---

# 105. Topology Spread

Kubernetes topology spread constraints SHOULD be considered across:

```text
Nodes

Availability Zones
```

---

# 106. Anti-Affinity

Pod anti-affinity MAY be used where appropriate.

---

# 107. Availability Zone

Critical production services SHOULD avoid placing all replicas in one availability zone where multi-AZ infrastructure is available.

---

# 108. HPA

Horizontal Pod Autoscaler SHOULD be used when workload demand varies materially.

---

# 109. CPU Autoscaling

CPU MAY be an HPA signal for CPU-correlated workloads.

---

# 110. Memory Autoscaling

Memory is often a less responsive autoscaling signal and MUST be used with understanding of workload behavior.

---

# 111. Business Metrics

Autoscaling MAY use workload-specific metrics such as:

```text
HTTP Request Rate

SQS Queue Backlog/Oldest-Message Age

SQS Queue Depth

Processing Latency
```

where platform tooling supports them.

---

# 112. Queue Consumer Scaling

Queue workers SHOULD consider backlog and processing rate.

---

# 113. SQS Scaling

Useful SQS signals include:

```text
ApproximateNumberOfMessagesVisible

AgeOfOldestMessage

Processing Rate
```

---

# 114. SQS Scaling

SQS consumer scaling MUST account for queue backlog, oldest-message age, downstream capacity and FIFO MessageGroupId concurrency where applicable.

---

# 115. SQS Parallelism Ceiling

If a topic has:

```text
12 partitions
```

then one consumer group cannot effectively use 100 replicas for partition-level parallelism.

---

# 116. HPA Maximum

HPA maximum replica count MUST respect downstream capacity.

---

# 117. Autoscaling Cascade

Avoid:

```text
Traffic Spike
   |
   v
HPA Adds 50 Pods
   |
   v
Each Opens 30 DB Connections
   |
   v
PostgreSQL Receives 1,500 Connections
   |
   v
OUTAGE
```

---

# 118. Capacity Equation

Autoscaling must account for:

```text
MAX_REPLICAS
    ×
PER_REPLICA_RESOURCE_USAGE
    <=
DEPENDENCY CAPACITY
```

---

# 119. Scale-Up

Scale-up SHOULD be responsive enough to workload growth.

---

# 120. Scale-Down

Scale-down SHOULD avoid excessive oscillation.

---

# 121. Stabilization Window

HPA stabilization SHOULD be configured where workload characteristics require it.

---

# 122. Cold Start

Application startup duration affects autoscaling effectiveness.

---

# 123. Startup Optimization

Slow startup SHOULD be measured and optimized when it limits recovery/autoscaling.

---

# 124. Vertical Scaling

Vertical resource changes MAY complement horizontal scaling.

---

# 125. VPA

Vertical Pod Autoscaler recommendations MAY inform resource tuning where supported.

---

# 126. ConfigMap

Non-sensitive runtime configuration MAY use ConfigMaps or approved external configuration mechanisms.

---

# 127. Secret

Sensitive configuration MUST use approved secret-management mechanisms.

---

# 128. Secret in ConfigMap

Passwords, tokens and credentials MUST NOT be stored in plain ConfigMaps.

---

# 129. Environment Variables

Environment variables MAY expose configuration to processes but require consideration because secrets can become visible through diagnostics or process metadata.

---

# 130. Secret Manager

AWS Secrets Manager or another approved enterprise secret store SHOULD be used where appropriate.

---

# 131. Rotation

Credentials SHOULD support rotation without source-code changes.

---

# 132. Configuration Validation

Applications MUST fail fast when required configuration is invalid.

---

# 133. Optional Configuration

Optional configuration MUST have safe defaults.

---

# 134. Configuration Drift

Environment configuration SHOULD be managed declaratively to reduce drift.

---

# 135. Infrastructure as Code

Cloud/Kubernetes infrastructure SHOULD be managed through approved Infrastructure as Code.

---

# 136. Manual Production Changes

Manual production infrastructure changes SHOULD be exceptional and auditable.

---

# 137. IAM

AWS permissions MUST follow least privilege.

---

# 138. Static AWS Credentials

Long-lived static AWS credentials SHOULD NOT be embedded in application configuration.

---

# 139. IRSA / Workload Identity

EKS workloads SHOULD use approved workload identity mechanisms such as IAM Roles for Service Accounts or the current enterprise EKS identity standard.

---

# 140. Service Identity

Each workload SHOULD receive only the AWS permissions required by that workload.

---

# 141. Shared IAM Role

Broad shared IAM roles across unrelated services SHOULD be avoided.

---

# 142. SQS Permission

An SQS producer should generally require only appropriate actions such as:

```text
sqs:SendMessage
```

for approved queues.

---

# 143. Consumer Permission

A consumer may require:

```text
sqs:ReceiveMessage

sqs:DeleteMessage

sqs:ChangeMessageVisibility
```

as appropriate.

---

# 144. Wildcard IAM

Permissions such as:

```text
Action: "*"
Resource: "*"
```

are prohibited for normal application workloads.

---

# 145. SQS

SQS SHOULD be used for queue-oriented asynchronous workloads where its delivery model is appropriate.

---

# 146. SQS Delivery

Consumers MUST assume at-least-once delivery.

---

# 147. SQS Idempotency

Business consumers MUST remain idempotent where duplicate processing can create duplicate effects.

---

# 148. Visibility Timeout

SQS visibility timeout MUST exceed expected processing duration with appropriate margin or be extended safely during long processing.

---

# 149. Visibility Too Short

If processing takes longer than visibility timeout:

```text
Consumer A processing
        |
        v
Visibility expires
        |
        v
Consumer B receives same message
```

---

# 150. SQS DLQ

Critical queues SHOULD define dead-letter handling.

---

# 151. maxReceiveCount

DLQ redrive policy MUST be bounded and aligned with retry semantics.

---

# 152. SQS Long Polling

Long polling SHOULD be used where it reduces empty receives and cost.

---

# 153. SQS Message Size

Payloads MUST respect SQS message-size constraints.

---

# 154. Large Payload

Large payloads SHOULD use a claim-check pattern such as:

```text
S3 Object
   +
SQS Reference
```

where appropriate.

---

# 155. FIFO

SQS FIFO SHOULD only be used when ordering/deduplication semantics justify its constraints.

---

# 156. Amazon SQS

Amazon SQS workloads MUST follow ADR-057.

---

# 157. SQS Credentials

SQS authentication/authorization MUST use approved platform mechanisms.

---

# 158. SQS Network

SQS endpoints MUST remain in approved private/network-controlled infrastructure.

---

# 159. RDS

RDS/PostgreSQL workloads MUST follow ADR-058.

---

# 160. RDS Connections

Application autoscaling MUST respect RDS connection capacity.

---

# 161. RDS Multi-AZ

Critical production databases SHOULD use appropriate high-availability topology.

---

# 162. RDS Backup

Automated backup/PITR requirements MUST reflect business RPO/RTO.

---

# 163. ElastiCache

Redis/ElastiCache workloads MUST follow ADR-059.

---

# 164. Redis Failure

Application runtime behavior MUST not assume Redis is permanently available.

---

# 165. S3

S3 SHOULD be used for durable object storage when appropriate.

---

# 166. S3 Access

S3 bucket access MUST use least privilege.

---

# 167. Public Bucket

Application data buckets MUST NOT be public unless explicitly designed and approved for public distribution.

---

# 168. S3 Encryption

Approved encryption at rest SHOULD be enabled.

---

# 169. Load Balancer

External HTTP traffic SHOULD enter through approved AWS/Kubernetes ingress/load-balancing infrastructure.

---

# 170. TLS

Externally exposed production traffic MUST use approved TLS configuration.

---

# 171. TLS Termination

TLS MAY terminate at an approved load balancer/ingress layer according to platform architecture.

---

# 172. Internal Traffic

Internal service traffic MUST follow enterprise network/security requirements.

---

# 173. Timeout Hierarchy

Timeouts across the request chain MUST be coherent.

Preferred relationship:

```text
CLIENT TIMEOUT
      >
INGRESS / LB TIMEOUT
      >
APPLICATION TIMEOUT
      >
DOWNSTREAM TIMEOUT
```

with appropriate margins.

---

# 174. Timeout Inversion

Avoid configurations where an outer layer gives up before the application/downstream timeout can complete predictably.

---

# 175. Keep-Alive

HTTP keep-alive behavior SHOULD be aligned across:

```text
Load Balancer

Ingress

Application

HTTP Client
```

---

# 176. Connection Pool

Outbound HTTP connection pools MUST be bounded.

---

# 177. DNS

Applications MUST tolerate normal cloud DNS changes and endpoint rotation.

---

# 178. Hardcoded IP

Cloud service IP addresses MUST NOT normally be hardcoded.

---

# 179. Network Failure

Applications MUST assume:

```text
Connection Reset

Timeout

DNS Failure

Transient Network Failure
```

can occur.

---

# 180. Resilience

Runtime dependency failures MUST follow ADR-055 resilience standards.

---

# 181. Observability

Every production workload MUST provide operational telemetry.

---

# 182. Minimum Signals

Monitor at least:

```text
Request Rate

Error Rate

Latency

CPU

Memory

Restarts

Replica Availability
```

---

# 183. Kubernetes Signals

Operational dashboards SHOULD include:

```text
Desired Replicas

Available Replicas

Pending Pods

CrashLoopBackOff

OOMKilled

CPU Throttling

HPA Activity
```

---

# 184. Dependency Signals

Applications SHOULD expose relevant:

```text
DB Pool Saturation

Redis Failures

SQS Queue Backlog/Oldest-Message Age

SQS Backlog

HTTP Client Failures

Circuit Breaker State
```

---

# 185. Pod Restart

Pod restarts MUST be observable.

---

# 186. Restart Is Signal

Repeated restarts MUST NOT be normalized as ordinary production behavior.

---

# 187. OOMKilled Alert

Repeated `OOMKilled` events require investigation.

---

# 188. CPU Throttle Monitoring

CPU throttling SHOULD be monitored for latency-sensitive workloads.

---

# 189. Logs

Container logs SHOULD be written to:

```text
stdout

stderr
```

for collection by the platform logging stack.

---

# 190. Local Log File

Applications SHOULD NOT depend on durable local log files inside containers.

---

# 191. Structured Logging

Structured logs SHOULD be used according to enterprise observability standards.

---

# 192. Correlation

Logs SHOULD preserve applicable:

```text
traceId

correlationId

requestId
```

---

# 193. Sensitive Logs

Secrets, credentials and sensitive payloads MUST NOT be logged.

---

# 194. Metrics Cardinality

Pod/request identifiers MUST NOT be used indiscriminately as high-cardinality metric labels.

---

# 195. Production Debugging

Temporary diagnostic changes MUST be controlled and removed after investigation.

---

# 196. Ephemeral Containers

Kubernetes ephemeral containers MAY be used for controlled diagnostics where platform policy permits.

---

# 197. Security Context

Kubernetes workloads SHOULD define appropriate security context.

---

# 198. Privilege Escalation

Application containers SHOULD disable privilege escalation where possible.

---

# 199. Linux Capabilities

Unnecessary Linux capabilities SHOULD be dropped.

---

# 200. Host Namespace

Normal application pods MUST NOT use host networking/process namespaces without explicit justification.

---

# 201. hostPath

`hostPath` volumes SHOULD NOT be used by ordinary stateless application services.

---

# 202. Network Policy

Network policies SHOULD restrict unnecessary east-west communication where platform networking supports them.

---

# 203. Kubernetes RBAC

Service accounts MUST follow least privilege.

---

# 204. Default Service Account

Production workloads SHOULD NOT rely on broad permissions attached to the default service account.

---

# 205. Secret Exposure

Kubernetes manifests MUST NOT contain plaintext production secrets in source repositories.

---

# 206. Image Pull

Private registries MUST use approved authentication mechanisms.

---

# 207. Supply Chain

Build and deployment pipelines SHOULD preserve software supply-chain integrity.

---

# 208. SBOM

Production artifacts SHOULD provide an SBOM where enterprise security tooling requires it.

---

# 209. Image Provenance

Image provenance/signing SHOULD be adopted where platform capabilities support it.

---

# 210. CI/CD Promotion

Production promotion MUST occur through approved CI/CD processes.

---

# 211. Quality Gate

Deployment MUST respect applicable:

```text
Compilation

Unit Tests

Integration Tests

JaCoCo

SonarQube

SAST

Dependency Scan

Container Scan

Architecture Tests
```

---

# 212. Failed Gate

Quality/security gates MUST NOT be routinely bypassed to accelerate normal delivery.

---

# 213. Emergency Deployment

Emergency bypass procedures, if permitted, MUST be auditable and followed by remediation.

---

# 214. Migration Before Deployment

Database migration sequencing MUST follow application compatibility requirements rather than a universal assumption that migration always executes first.

---

# 215. Deployment Sequence

A typical compatible sequence is:

```text
EXPAND DATABASE

DEPLOY NEW APPLICATION

BACKFILL / MIGRATE

VERIFY

CONTRACT DATABASE LATER
```

---

# 216. Startup Migration

Application startup SHOULD NOT create uncontrolled migration races across many replicas.

---

# 217. Flyway Execution

Flyway MAY execute through:

```text
Dedicated Migration Job

Controlled Deployment Stage

Single Authorized Instance
```

according to platform architecture.

---

# 218. Migration Credentials

Application runtime credentials SHOULD NOT require DDL privileges merely because migrations exist.

---

# 219. Production Readiness Review

A new production service MUST undergo readiness evaluation appropriate to criticality.

---

# 220. Production Readiness Checklist

Review:

```text
[ ] Multiple replicas where required

[ ] CPU request defined

[ ] Memory request defined

[ ] Memory limit defined

[ ] JVM sizing validated

[ ] Startup probe defined where needed

[ ] Readiness probe correct

[ ] Liveness probe correct

[ ] Graceful shutdown enabled

[ ] Termination grace aligned

[ ] HPA strategy defined

[ ] HPA maximum respects dependencies

[ ] PDB defined where required

[ ] Multi-AZ distribution considered

[ ] IAM least privilege

[ ] Secrets externalized

[ ] Network exposure reviewed

[ ] TLS configured

[ ] Database pool bounded

[ ] HTTP pools bounded

[ ] Redis behavior defined

[ ] SQS semantics defined

[ ] Logs centralized

[ ] Metrics available

[ ] Alerts defined

[ ] Rollback strategy documented

[ ] Database compatibility validated

[ ] Load/performance test completed
```

---

# 221. Capacity Planning

Production capacity MUST be evaluated end-to-end.

---

# 222. Per-Pod Capacity

Determine approximate:

```text
Requests / Second

CPU / Request

Memory / Pod

DB Connections / Pod

HTTP Connections / Pod
```

---

# 223. Fleet Capacity

Then evaluate:

```text
POD CAPACITY
    ×
REPLICA COUNT
    =
SERVICE CAPACITY
```

---

# 224. Dependency Capacity

Service capacity MUST NOT exceed critical dependency capacity without backpressure/resilience mechanisms.

---

# 225. Maximum Replica Analysis

For example:

```text
30 max replicas
      ×
20 DB connections
      =
600 potential DB connections
```

The database must be designed for that concurrency or application limits must change.

---

# 226. Scaling Is Not Infinite

Horizontal scaling cannot fix a bottleneck in a fixed-capacity downstream system.

---

# 227. Load Test

Critical services SHOULD be load-tested with production-like:

```text
CPU Limits

Memory Limits

Replica Counts

Connection Pools

Timeouts
```

---

# 228. Kubernetes-Aware Performance Test

Running a benchmark with unlimited local resources is insufficient evidence for production capacity.

---

# 229. Failure Testing

Critical services SHOULD test scenarios such as:

```text
Pod Termination

Redis Outage

Database Slowdown

SQS Unavailability

SQS Backlog

Remote API Timeout
```

---

# 230. Pod Termination Test

Deployment tests SHOULD verify that terminating a pod does not create material request loss beyond accepted semantics.

---

# 231. Autoscaling Test

HPA behavior SHOULD be validated for critical variable-load services.

---

# 232. Recovery Test

The platform SHOULD validate recovery after dependency restoration.

---

# 233. Runtime Fitness Functions

Stable runtime rules SHOULD be automated where practical.

Examples:

```text
[ ] Container runs as non-root

[ ] Image uses approved registry

[ ] Image tag is immutable

[ ] CPU request exists

[ ] Memory request exists

[ ] Memory limit exists

[ ] Readiness probe exists

[ ] Liveness probe exists

[ ] Graceful shutdown configured

[ ] Production replicas >= required minimum

[ ] HPA max is bounded

[ ] PDB exists for critical services

[ ] No plaintext secrets in manifests

[ ] IAM wildcard permissions rejected

[ ] Deployment references approved service account

[ ] Image vulnerability gate passes
```

---

# 234. Enterprise Runtime Gate

A workload is not considered production compliant when applicable conditions include:

```text
[ ] Container runs unnecessarily as root

[ ] Mutable latest image used

[ ] No resource requests

[ ] JVM heap consumes entire container limit

[ ] Liveness fails because optional dependency is down

[ ] Graceful shutdown absent

[ ] Single replica for critical service without justification

[ ] HPA can exceed downstream capacity

[ ] All replicas can land on one failure domain

[ ] Plaintext secrets committed

[ ] Static AWS credentials embedded

[ ] IAM uses broad wildcard access

[ ] Application stores durable state in container filesystem

[ ] Deployment breaks previous-version compatibility

[ ] Database migration is destructive during rolling deployment

[ ] Observability is insufficient to diagnose runtime failure
```

---

# 235. Anti-Patterns

The following are prohibited or strongly discouraged:

- mutable `latest` production images
- rebuilding different binaries for every environment
- unnecessary tools in runtime images
- root containers without justification
- durable application data in container filesystem
- critical production services with unjustified single replicas
- missing resource requests
- heap equal to total container memory
- using Virtual Threads as justification for unlimited downstream concurrency
- liveness tied to every external dependency
- arbitrary probe configuration
- killing pods without graceful shutdown
- huge fixed `preStop` sleeps as the primary draining strategy
- HPA without downstream-capacity analysis
- scaling SQS consumers beyond useful queue/FIFO MessageGroupId concurrency or downstream capacity
- scaling HTTP services until the database collapses
- plaintext secrets in ConfigMaps
- static AWS credentials in application properties
- wildcard IAM permissions
- public Redis/RDS/SQS endpoints without explicit architecture
- hardcoded cloud IP addresses
- unbounded HTTP connection pools
- local persistent log files in containers
- manual undocumented production infrastructure changes
- Flyway races across uncontrolled startup replicas
- destructive database migration coupled to the same rollout
- assuming rollback reverses schema
- performance testing only with unlimited developer-machine resources

---

# 236. Positive Consequences

The decision provides:

- reproducible deployments
- stronger container security
- predictable resource consumption
- improved Kubernetes scheduling
- safer Java memory configuration
- better graceful shutdown
- reduced deployment downtime
- controlled autoscaling
- safer AWS authorization
- better dependency capacity management
- stronger production observability
- improved rollback capability
- reduced environment drift

---

# 237. Negative Consequences

The decision introduces:

- additional infrastructure configuration
- resource tuning
- probe design
- capacity analysis
- IAM management
- deployment testing
- operational dashboards
- stricter CI/CD controls

These costs are accepted because runtime configuration is part of application correctness in distributed cloud systems.

---

# 238. Neutral Consequences

The decision also means:

- not every service requires HPA
- not every workload requires Kubernetes
- not every deployment requires canary
- not every service requires the same CPU/memory values
- Virtual Threads do not remove resource limits
- some workloads legitimately require persistent volumes
- some probes require service-specific semantics
- different AWS services retain different scaling constraints

---

# 239. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| OOMKilled | Critical | Medium | JVM/container memory sizing |
| CPU throttling | High | Medium | Requests/limits testing |
| Deployment outage | Critical | Medium | Readiness + rolling strategy |
| Request loss on shutdown | High | Medium | Graceful termination |
| Database overload from HPA | Critical | Medium | Capacity-aware scaling |
| IAM overprivilege | Critical | Medium | Least privilege |
| Secret leakage | Critical | Low/Medium | Secret management |
| Restart storm | High | Medium | Correct liveness semantics |
| Single-AZ concentration | High | Low/Medium | Topology spread |
| SQS over-scaling | Medium | Medium | Queue/FIFO-group-aware scaling |
| SQS duplicate effect | High | Medium | Idempotent consumers |
| Migration incompatibility | Critical | Medium | Expand/contract |

---

# 240. Implementation Guidance

The following rules are mandatory:

1. Production application artifacts must be immutable.
2. The same application image should progress across environments.
3. Production images must use deterministic versioning.
4. Images must come from approved registries/base images.
5. Containers should run as non-root.
6. Durable business state must not depend on container filesystem.
7. Production workloads must define resource requests.
8. Memory limits must leave sufficient JVM native-memory headroom.
9. Java 21 container awareness must remain enabled.
10. Virtual Threads must not create unbounded downstream concurrency.
11. Production HTTP workloads must define correct readiness and liveness semantics.
12. Startup probes should protect applications with long initialization.
13. Liveness must not normally depend on optional external systems.
14. Applications must support graceful shutdown.
15. Kubernetes termination grace must align with application shutdown behavior.
16. Rolling deployments must preserve old/new version compatibility.
17. Database evolution must follow expand/contract.
18. SQS contracts must remain compatible during rollout.
19. Redis serialization must remain rollout compatible.
20. Critical workloads should have multiple replicas.
21. Critical replicas should be distributed across failure domains.
22. Appropriate PDBs should protect voluntary disruptions.
23. HPA must be based on workload-appropriate signals.
24. Maximum autoscaling must respect downstream capacity.
25. SQS consumer scaling must account for queue backlog, oldest-message age, downstream capacity and FIFO MessageGroupId concurrency.
26. SQS consumers must assume duplicate delivery.
27. AWS permissions must follow least privilege.
28. Static long-lived AWS credentials must not be embedded in applications.
29. EKS workloads should use approved workload identity.
30. Secrets must use approved secret-management mechanisms.
31. Infrastructure must be declarative/version controlled where practical.
32. Runtime connection pools and queues must be bounded.
33. Timeout hierarchies must be coherent.
34. Production workloads must expose operational telemetry.
35. OOMKilled, restart loops and CPU throttling must be observable.
36. Production deployment must pass applicable quality/security gates.
37. Database migrations must not rely on uncontrolled concurrent application startup.
38. Critical services must have documented rollback/recovery strategies.
39. Capacity planning must include dependencies, not only application pods.
40. Production-like performance testing should use realistic container resource limits.

---

# 241. Validation

This ADR will be validated through:

- AWS
- Amazon EKS
- Kubernetes
- OCI containers
- Amazon ECR
- IAM
- workload identity / IRSA
- Amazon RDS
- Amazon ElastiCache
- Amazon MSK
- Amazon SQS
- Amazon S3
- Java 21
- Spring Boot
- Spring Boot Actuator
- HikariCP
- Kubernetes metrics
- CI/CD
- SonarQube
- SAST
- container vulnerability scanning
- load testing
- resilience testing
- architecture fitness functions

---

# 242. Success Criteria

The decision is successful when:

- deployments become reproducible
- zero-downtime rollouts become predictable
- pod termination no longer causes unnecessary request loss
- OOMKilled incidents decrease
- CPU throttling is visible and controlled
- autoscaling does not overwhelm databases/dependencies
- critical replicas remain distributed
- AWS permissions remain least-privileged
- production secrets are externalized
- application rollback remains operationally viable
- dependency outages produce controlled degradation
- production incidents contain sufficient telemetry for diagnosis
- capacity planning reflects actual end-to-end constraints

---

# 243. Alternatives Rejected

## 243.1 Unlimited Container Resources

Rejected because resource contention would become unpredictable.

---

## 243.2 Heap Equal to Container Memory

Rejected because JVM native/off-heap memory also consumes container memory.

---

## 243.3 Liveness Based on All Dependencies

Rejected because dependency outages would create restart storms.

---

## 243.4 Autoscaling Without Dependency Limits

Rejected because application scaling can overwhelm fixed-capacity dependencies.

---

## 243.5 Static AWS Credentials

Rejected because workload identity provides safer credential lifecycle and least privilege.

---

## 243.6 Mutable Production Images

Rejected because deployments would not be reproducible.

---

## 243.7 Application Filesystem as Durable Storage

Rejected because pods are ephemeral.

---

# 244. Related Decisions

This ADR extends and implements:

- ADR-014: Distributed Observability
- ADR-016: Application Resilience
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-045: Disaster Recovery and Regional Resilience Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard

---

# 245. References

- Kubernetes Documentation
- Amazon EKS Documentation
- AWS Well-Architected Framework
- AWS IAM Documentation
- Amazon RDS Documentation
- Amazon ElastiCache Documentation
- Amazon MSK Documentation
- Amazon SQS Documentation
- Java 21 Documentation
- Spring Boot Documentation
- OCI Image Specification
- Kubernetes Autoscaling Documentation
- Google Site Reliability Engineering
- OWASP
- CNCF Cloud Native Security Guidance

---

# 246. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-25 | AstraForge Supply Platform Architecture Team | Approved | Initial AWS, Kubernetes, container and runtime deployment baseline |

---

# 247. Decision Summary

The production runtime becomes:

```text
SOURCE
  |
  v
CI/CD
  |
  +--> TEST
  +--> SONAR
  +--> SAST
  +--> IMAGE SCAN
  |
  v
IMMUTABLE OCI IMAGE
  |
  v
ECR
  |
  v
EKS / KUBERNETES
  |
  v
DEPLOYMENT
  |
  v
PODS
  |
  v
JAVA 21 / SPRING BOOT
```

Container memory:

```text
CONTAINER MEMORY LIMIT
        |
        +--> HEAP
        |
        +--> METASPACE
        |
        +--> DIRECT MEMORY
        |
        +--> THREAD STACKS
        |
        +--> JVM NATIVE
```

Therefore:

```text
-Xmx
<
CONTAINER MEMORY LIMIT
```

with sufficient headroom.

Virtual Threads:

```text
MANY VIRTUAL THREADS
        |
        v
BOUNDED RESOURCES
        |
        +--> DB POOL
        +--> HTTP POOL
        +--> CPU
        +--> SQS PARTITIONS
```

Health:

```text
STARTUP
  |
  v
STARTUP PROBE
  |
  v
READY
  |
  +--> READINESS
  |
  +--> LIVENESS
```

where:

```text
READINESS
=
CAN RECEIVE TRAFFIC?

LIVENESS
=
SHOULD KUBERNETES RESTART ME?
```

Graceful shutdown:

```text
SIGTERM
   |
   v
REMOVE FROM TRAFFIC
   |
   v
STOP NEW WORK
   |
   v
COMPLETE IN-FLIGHT WORK
   |
   v
CLOSE RESOURCES
   |
   v
EXIT
```

Rolling deployment:

```text
OLD V1 V1 V1
      |
      v
V1 V1 V2
      |
      v
V1 V2 V2
      |
      v
V2 V2 V2
```

Therefore:

```text
V1 + V2
```

must coexist safely.

Autoscaling:

```text
LOAD
  |
  v
HPA
  |
  v
MORE PODS
  |
  v
MORE CONNECTIONS
  |
  v
MORE DOWNSTREAM LOAD
```

which means:

```text
AUTOSCALING
must be constrained by
DEPENDENCY CAPACITY
```

AWS identity:

```text
POD
 |
 v
SERVICE ACCOUNT
 |
 v
WORKLOAD IDENTITY / IAM ROLE
 |
 v
ONLY REQUIRED AWS ACTIONS
```

instead of:

```text
STATIC ACCESS KEY
        +
STATIC SECRET KEY
```

Production readiness becomes:

```text
SECURE IMAGE
     +
RESOURCE BOUNDS
     +
CORRECT PROBES
     +
GRACEFUL SHUTDOWN
     +
MULTIPLE REPLICAS
     +
FAILURE-DOMAIN DISTRIBUTION
     +
CAPACITY-AWARE AUTOSCALING
     +
LEAST-PRIVILEGE IAM
     +
SECRET MANAGEMENT
     +
COMPATIBLE DEPLOYMENT
     +
OBSERVABILITY
     =
PRODUCTION-READY CLOUD SERVICE
```

The governing principle is:

```text
Do not treat Kubernetes as
an application launcher.

Treat runtime configuration
as architecture.

Size the JVM for the container,
not the developer workstation.

Do not confuse more threads
with more capacity.

Do not make liveness dependent
on systems a restart cannot fix.

Drain traffic before termination.

Design every release knowing
old and new versions coexist.

Scale only as far as dependencies
can sustain.

Use workload identity instead
of embedded cloud credentials.

Keep production images immutable.

And make every production workload
observable, bounded, reproducible,
secure and recoverable.
```
