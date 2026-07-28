# ADR-025: Adopt Kubernetes Runtime Security Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-025 |
| Title | Adopt Kubernetes Runtime Security Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Kubernetes, Runtime Security, Workload Security, Availability and Cluster Governance |
| Related Work Items | Pod Security Standards, RBAC, NetworkPolicy, Workload Identity, Admission Control, Runtime Security |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform deploys containerized workloads to Kubernetes.

The application architecture already establishes standards for:

- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- Amazon SQS
- Redis
- Kubernetes
- Resilience4j
- OpenTelemetry
- structured logging
- API security
- software supply-chain security
- immutable artifacts
- zero-downtime deployment

Kubernetes provides significant workload isolation and orchestration capabilities, but secure operation does not result automatically from deploying a container into a cluster.

A workload can still introduce significant risk through:

- privileged containers
- root execution
- excessive Linux capabilities
- writable filesystems
- unrestricted network communication
- broad ServiceAccount permissions
- unbounded CPU or memory consumption
- insecure secret handling
- untrusted container images
- inappropriate probe configuration
- missing disruption protection
- excessive cluster API access
- insecure administrative access
- runtime compromise

The platform therefore requires an explicit Kubernetes runtime security baseline.

---

# 2. Problem Statement

The platform requires standardized Kubernetes runtime controls that:

- enforce workload isolation
- minimize container privileges
- prevent unnecessary root execution
- restrict Linux capabilities
- use seccomp
- restrict writable filesystem access
- control pod-to-pod communication
- enforce least-privilege RBAC
- control ServiceAccount usage
- integrate with workload identity
- protect Kubernetes Secrets
- verify production images
- enforce trusted registries
- enforce resource requests and limits
- protect cluster stability
- support horizontal autoscaling
- support graceful shutdown
- support zero-downtime deployments
- survive voluntary disruptions
- distribute replicas across failure domains
- secure debugging mechanisms
- support runtime monitoring
- support Kubernetes audit
- control human cluster access
- support incident response
- integrate with the software supply chain

---

# 3. Decision Drivers

Primary decision drivers are:

1. least privilege
2. workload isolation
3. defense in depth
4. cluster stability
5. blast-radius reduction
6. runtime integrity
7. network isolation
8. credential protection
9. zero-downtime deployment
10. workload availability
11. artifact trust
12. auditability
13. operational consistency
14. automated policy enforcement
15. incident response
16. observability
17. maintainability

---

# 4. Decision

The Enterprise Order Platform adopts a Kubernetes runtime security baseline based on:

```text
Pod Security Standards

+

Restricted securityContext

+

Non-Root Containers

+

Read-Only Root Filesystem

+

Minimal Linux Capabilities

+

Seccomp

+

Resource Governance

+

Namespace Isolation

+

Least-Privilege RBAC

+

Dedicated ServiceAccounts

+

NetworkPolicies

+

Workload Identity

+

Admission Policies

+

Verified Immutable Images

+

Runtime Monitoring
```

Security controls should be enforced automatically wherever Kubernetes platform capabilities permit.

---

# 5. Fundamental Principle

The platform adopts:

```text
A container must receive only the privileges,
network access, resources and identities
required to perform its business capability.
```

Kubernetes is not considered a trusted flat network.

---

# 6. Defense in Depth

Runtime security is applied through multiple layers:

```text
Supply Chain

↓

Container Image

↓

Admission Control

↓

Namespace

↓

Pod Security

↓

Container Security Context

↓

ServiceAccount / Workload Identity

↓

NetworkPolicy

↓

Application Security

↓

Runtime Detection

↓

Audit
```

No single control is sufficient.

---

# 7. Pod Security Standards

The Kubernetes Pod Security Standards provide the baseline workload-security model.

The platform recognizes:

```text
Privileged

Baseline

Restricted
```

profiles.

---

# 8. Restricted Profile

Application workloads should target the:

```text
Restricted
```

Pod Security Standard wherever technically possible.

---

# 9. Exceptions

Workloads unable to comply with the Restricted profile require:

- documented technical justification
- narrow scope
- security review
- compensating controls
- explicit owner
- periodic review

---

# 10. Privileged Profile

Ordinary application workloads must not require the Privileged profile.

---

# 11. securityContext

Every production workload must explicitly define appropriate security context rather than relying solely on runtime defaults.

Example:

```yaml
securityContext:
  runAsNonRoot: true
  seccompProfile:
    type: RuntimeDefault
```

Container-level configuration should further restrict privileges.

---

# 12. Container Security Context

Recommended application-container baseline:

```yaml
securityContext:
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  capabilities:
    drop:
      - ALL
  seccompProfile:
    type: RuntimeDefault
```

Application-specific exceptions require justification.

---

# 13. Non-Root Execution

Production application containers should execute as non-root.

---

# 14. runAsNonRoot

The following should normally be configured:

```yaml
runAsNonRoot: true
```

This allows Kubernetes to reject workloads that would execute as root.

---

# 15. Explicit User

Where container-image design permits, a numeric non-root user may be configured.

Example:

```yaml
runAsUser: 10001
```

The exact UID belongs to image/platform conventions.

---

# 16. Dockerfile User

Container images should also define a non-root runtime user.

Example:

```dockerfile
USER 10001
```

Runtime enforcement complements image configuration.

---

# 17. Root Requirement

A workload requiring root privileges must provide explicit technical justification.

---

# 18. Privileged Container

The following is prohibited for ordinary application services:

```yaml
securityContext:
  privileged: true
```

---

# 19. Privilege Escalation

Application containers must normally configure:

```yaml
allowPrivilegeEscalation: false
```

---

# 20. Linux Capabilities

Linux capabilities must follow least privilege.

---

# 21. Drop All Capabilities

The preferred baseline is:

```yaml
capabilities:
  drop:
    - ALL
```

---

# 22. Added Capability

Adding a Linux capability requires explicit justification.

Example:

```yaml
capabilities:
  add:
    - NET_BIND_SERVICE
```

should only be used when genuinely required.

---

# 23. High Ports

Applications should prefer unprivileged ports such as:

```text
8080
```

rather than requiring additional privileges merely to bind to:

```text
80
```

---

# 24. Seccomp

Application containers must use an approved seccomp profile.

Preferred baseline:

```yaml
seccompProfile:
  type: RuntimeDefault
```

---

# 25. Unconfined Seccomp

The following is prohibited for ordinary workloads:

```yaml
seccompProfile:
  type: Unconfined
```

---

# 26. Custom Seccomp

Custom seccomp profiles may be used when required and centrally governed.

---

# 27. Root Filesystem

Application containers should use:

```yaml
readOnlyRootFilesystem: true
```

where technically possible.

---

# 28. Writable Paths

Applications requiring writable storage must use explicitly mounted writable locations.

Examples:

```text
/tmp

application work directory

ephemeral volume

persistent volume
```

depending on semantics.

---

# 29. EmptyDir

Temporary writable storage may use:

```yaml
emptyDir: {}
```

when data does not require persistence.

---

# 30. Temporary Data

Applications must not assume the container filesystem is persistent.

---

# 31. Container Immutability

Application binaries and configuration embedded in the image must not be modified at runtime.

---

# 32. Runtime Package Installation

Production containers must not execute runtime package installation such as:

```text
apt install

yum install

apk add
```

during normal startup.

Required software belongs in the immutable image.

---

# 33. Host Namespace Access

Ordinary application pods must not use:

```yaml
hostNetwork: true
```

```yaml
hostPID: true
```

or:

```yaml
hostIPC: true
```

---

# 34. HostPath

`hostPath` volumes are prohibited for ordinary application workloads.

---

# 35. Host Devices

Direct host-device access is prohibited unless explicitly required and reviewed.

---

# 36. Namespace Isolation

Applications must be organized into namespaces according to platform/environment architecture.

---

# 37. Namespace Purpose

Namespaces provide boundaries for:

- RBAC
- NetworkPolicy
- quotas
- workload organization
- policy enforcement

They are not complete security isolation by themselves.

---

# 38. Environment Separation

Production workloads must be appropriately isolated from non-production workloads.

The exact implementation may use:

- separate clusters
- separate accounts/projects
- separate namespaces

according to enterprise infrastructure standards.

---

# 39. Production Namespace

Production namespaces require stronger access controls than development namespaces.

---

# 40. Resource Requests

Every production container must define CPU and memory requests.

Example:

```yaml
resources:
  requests:
    cpu: 250m
    memory: 512Mi
```

Values must be determined from workload measurements.

---

# 41. Resource Limits

Production workloads should define appropriate resource limits according to platform policy.

Example:

```yaml
resources:
  limits:
    memory: 1Gi
```

CPU limit strategy may depend on workload and platform standards.

---

# 42. Requests Are Scheduling Contracts

Resource requests influence Kubernetes scheduling.

They should represent realistic expected consumption.

---

# 43. Memory Limits

Memory limits must account for Java runtime behavior.

---

# 44. Java Container Awareness

Java 21 is container aware and should size runtime behavior according to available container resources.

Explicit JVM tuning may still be required for predictable production operation.

---

# 45. Heap vs Container Memory

The JVM heap must not consume the entire container memory limit.

Memory is also required for:

- metaspace
- thread stacks
- direct buffers
- native libraries
- JIT
- JVM internals

---

# 46. MaxRAMPercentage

Where appropriate, JVM memory may be controlled through options such as:

```text
-XX:MaxRAMPercentage
```

after load testing.

---

# 47. OOMKilled

Repeated:

```text
OOMKilled
```

events are production defects requiring investigation.

Increasing memory blindly is not the default remediation.

---

# 48. CPU Throttling

CPU limits must be evaluated against latency-sensitive Java workloads.

Excessive throttling can create:

- latency spikes
- timeout cascades
- misleading downstream failures

---

# 49. ResourceQuota

Namespaces should use ResourceQuota where appropriate to prevent uncontrolled resource consumption.

---

# 50. LimitRange

LimitRange may define namespace defaults and boundaries.

Explicit application sizing remains preferred.

---

# 51. ServiceAccount

Every workload requiring Kubernetes identity should use an appropriate ServiceAccount.

---

# 52. Default ServiceAccount

Production application workloads should not rely on the namespace default ServiceAccount when a dedicated identity is appropriate.

---

# 53. Dedicated ServiceAccount

Preferred:

```yaml
serviceAccountName: orders-service
```

rather than sharing one broad application identity across unrelated services.

---

# 54. Token Automount

If a workload does not need Kubernetes API access, ServiceAccount token automount should be disabled where appropriate.

Example:

```yaml
automountServiceAccountToken: false
```

---

# 55. Kubernetes API Access

Application services should not receive Kubernetes API access unless their business or infrastructure responsibility explicitly requires it.

---

# 56. RBAC

Kubernetes RBAC must follow least privilege.

---

# 57. Role

Namespace-scoped permissions should prefer:

```text
Role
```

over:

```text
ClusterRole
```

when cluster-wide permissions are unnecessary.

---

# 58. ClusterRole

ClusterRole permissions require stronger justification because their potential blast radius is larger.

---

# 59. Wildcard Permissions

Broad permissions such as:

```yaml
verbs:
  - "*"
resources:
  - "*"
```

are prohibited for ordinary application workloads.

---

# 60. RBAC Scope

Permissions should explicitly define:

- API group
- resource
- verbs
- namespace where applicable

---

# 61. Secrets RBAC

Permission to read Kubernetes Secrets is highly privileged.

Applications must receive access only to required secrets.

---

# 62. Workload Identity

Where cloud infrastructure supports workload identity, it should be preferred over static cloud credentials stored inside Kubernetes.

Conceptually:

```text
Pod

↓

ServiceAccount

↓

Workload Identity

↓

Short-lived Cloud Credential
```

---

# 63. Static Cloud Credentials

Long-lived static cloud credentials inside application configuration should be avoided where workload identity exists.

---

# 64. Identity Scope

Cloud workload identities must follow least privilege.

---

# 65. Identity Separation

Different services should use different cloud identities where their permission requirements differ.

---

# 66. Kubernetes Secrets

Kubernetes Secrets are sensitive data and require explicit governance.

---

# 67. Base64

Kubernetes Secret values being Base64 encoded does not make them encrypted.

---

# 68. Secret Source

Production secrets should originate from the approved enterprise secret-management solution.

---

# 69. Secret in Git

Plain production secrets must never be committed to Git manifests.

---

# 70. External Secrets

External secret integration may be used to synchronize or mount secrets from an approved secret store.

---

# 71. Secret Rotation

Secret delivery must support rotation according to ADR-023 and ADR-024.

---

# 72. Secret Mounting

Secrets should be exposed only to containers that require them.

---

# 73. Environment Variable Secrets

Environment variables may be appropriate for some applications but can be exposed through diagnostics and process environments.

Secret mounting strategy must follow platform policy.

---

# 74. Secret Volume

File-based secret mounts may be preferable where applications and secret infrastructure support rotation.

---

# 75. Secret Logging

Application startup logs must never print resolved secrets.

---

# 76. Secret Failure

Missing mandatory secrets should cause controlled startup failure rather than insecure defaults.

---

# 77. Network Trust

Kubernetes cluster networking is not considered inherently trusted.

---

# 78. NetworkPolicy

Production namespaces should use NetworkPolicies where the network plugin supports enforcement.

---

# 79. Default Deny

The target network-security posture is:

```text
Default Deny

+

Explicit Allow
```

---

# 80. Ingress Policy

Pods should receive inbound traffic only from required sources.

---

# 81. Egress Policy

Pods should initiate outbound connections only to required destinations where platform capabilities permit practical enforcement.

---

# 82. Example

An Orders service may require:

```text
Ingress:
Ingress Controller / API Gateway

Egress:
PostgreSQL
SQS
Customers Service
Products Service
OpenTelemetry Collector
DNS
```

It does not require unrestricted communication with every pod.

---

# 83. DNS

Default-deny egress policies must explicitly account for required DNS resolution.

---

# 84. Database Network Access

Only workloads requiring database access should be able to reach database endpoints.

---

# 85. SQS Network Access

Only authorized producers/consumers should have network access to SQS endpoints in addition to IAM authorization.

---

# 86. NetworkPolicy Is Not Authorization

NetworkPolicy does not replace:

- OAuth
- JWT
- mTLS where required
- SQS IAM policy
- database credentials
- application authorization

---

# 87. Service Mesh

A service mesh may provide additional:

- mTLS
- traffic policy
- telemetry

if adopted by the platform.

This ADR does not require a service mesh.

---

# 88. Ingress

External API traffic must enter through approved ingress/gateway infrastructure.

---

# 89. Direct Pod Exposure

Application pods must not be directly exposed to the public network.

---

# 90. Service Type

Public application exposure should not use arbitrary:

```yaml
type: NodePort
```

without infrastructure justification.

---

# 91. LoadBalancer

Direct LoadBalancer services should be governed by platform architecture.

Ingress/gateway-based exposure is preferred for normal APIs.

---

# 92. TLS

External production traffic must use TLS according to ADR-023.

---

# 93. Internal TLS

Internal transport encryption follows enterprise infrastructure security policy.

---

# 94. Admission Control

Security requirements should be enforced before workloads are admitted into the cluster.

---

# 95. Admission Policies

Admission controls should progressively enforce rules such as:

- non-root
- no privileged containers
- no privilege escalation
- approved seccomp
- required resource requests
- required limits where policy applies
- approved registries
- immutable images
- no `latest`
- required labels
- restricted host access
- trusted signatures

---

# 96. Policy Engine

Policies may be implemented using approved Kubernetes/platform mechanisms such as:

- ValidatingAdmissionPolicy
- policy engines
- managed cloud admission controls

according to infrastructure standards.

---

# 97. Policy as Code

Admission policies must be version controlled.

---

# 98. Admission Enforcement

Critical security rules should eventually operate in:

```text
Enforce
```

mode.

---

# 99. Policy Rollout

Where appropriate:

```text
Audit

↓

Warn

↓

Enforce
```

may be used during controlled adoption.

---

# 100. Policy Exception

Admission-policy exceptions require:

- justification
- owner
- scope
- expiration
- review

---

# 101. Image Registry

Production workloads must use approved image registries.

---

# 102. Image Tag

Production deployment using only:

```text
latest
```

is prohibited.

---

# 103. Image Digest

Production workloads should deploy images by immutable digest.

Example:

```yaml
image: registry.example/orders-service@sha256:...
```

---

# 104. Image Pull Policy

Image pull behavior must align with immutable artifact strategy.

Digest-based images eliminate ambiguity regarding content identity.

---

# 105. Image Verification

Production admission should verify image signatures and/or attestations where platform tooling supports it.

---

# 106. Supply Chain

Runtime admission closes the chain established by ADR-024:

```text
Source

↓

Build

↓

Scan

↓

SBOM

↓

Provenance

↓

Sign

↓

Registry

↓

Admission Verification

↓

Runtime
```

---

# 107. Unsigned Artifact

When signature enforcement is active, unsigned or invalidly signed production images must be rejected.

---

# 108. Provenance Policy

Admission may also validate provenance requirements where infrastructure supports attestation policy.

---

# 109. Liveness Probe

Liveness answers:

```text
Should Kubernetes restart this container?
```

---

# 110. Liveness Principle

Liveness must represent failure that can reasonably be corrected by restarting the process.

---

# 111. External Dependency in Liveness

External dependencies must not normally be included in liveness.

Bad:

```text
Database unavailable
→ liveness fails
→ Kubernetes restarts every pod
```

This can amplify an external outage.

---

# 112. Readiness Probe

Readiness answers:

```text
Should this pod receive traffic now?
```

---

# 113. Readiness Principle

Readiness should represent whether the instance can safely process its required traffic.

---

# 114. Startup Probe

Slow-starting services should use startup probes where necessary.

---

# 115. Startup vs Liveness

Startup probes prevent liveness from killing an application before initialization is complete.

---

# 116. Spring Boot Actuator

Spring Boot Actuator health groups should map appropriately to Kubernetes probes.

---

# 117. Probe Security

Probe endpoints must not expose sensitive internal information.

---

# 118. Probe Cost

Health probes must be inexpensive.

A probe must not execute expensive business queries.

---

# 119. Graceful Shutdown

Production services must support graceful shutdown.

---

# 120. SIGTERM

Applications must handle Kubernetes termination semantics correctly.

Conceptually:

```text
SIGTERM

↓

Stop accepting new work

↓

Complete bounded in-flight work

↓

Release resources

↓

Exit
```

---

# 121. Spring Boot Graceful Shutdown

Spring Boot graceful shutdown should be enabled/configured according to application behavior.

---

# 122. terminationGracePeriodSeconds

Pods must receive an appropriate termination grace period.

The value must reflect realistic request and processing durations.

---

# 123. PreStop

A `preStop` hook may be used when required to coordinate traffic draining.

It must not become an arbitrary sleep used without understanding shutdown behavior.

---

# 124. Shutdown Timeout

Application shutdown must remain bounded.

A pod cannot wait indefinitely.

---

# 125. SQS Consumer Shutdown

SQS consumers must stop polling and close cleanly during termination.

---

# 126. Outbox Dispatcher Shutdown

Background dispatchers must stop accepting new work and finish or safely release current work.

---

# 127. Scheduled Jobs

Scheduled/background processing must account for pod termination and duplicate execution.

---

# 128. Zero-Downtime Deployment

Runtime configuration must support ADR-021.

---

# 129. RollingUpdate

Stateless application deployments should normally use:

```yaml
strategy:
  type: RollingUpdate
```

---

# 130. maxUnavailable

Critical APIs should generally minimize unavailable replicas during rolling deployment.

Example:

```yaml
maxUnavailable: 0
```

when capacity permits.

---

# 131. maxSurge

`maxSurge` should permit replacement capacity during deployment.

---

# 132. Readiness and Rolling Deployment

A new pod must not receive production traffic until readiness succeeds.

---

# 133. Old Pod Removal

Old pods should remain available until replacement capacity is ready according to deployment strategy.

---

# 134. PodDisruptionBudget

Critical replicated workloads should use PodDisruptionBudget.

---

# 135. PDB Purpose

PDB protects availability during voluntary disruptions such as:

- node maintenance
- cluster upgrades
- controlled evictions

---

# 136. PDB Limitation

PDB does not protect against every failure.

It cannot prevent:

- node crash
- application crash
- zone outage

---

# 137. PDB Example

Example:

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
spec:
  minAvailable: 2
```

Actual values depend on replica count and SLO.

---

# 138. Replica Count

Critical production services should normally run multiple replicas.

---

# 139. Single Replica

A single replica cannot provide high availability during:

- deployment
- restart
- node failure

unless the service's availability requirement explicitly accepts this.

---

# 140. Pod Anti-Affinity

Critical replicas should avoid unnecessary concentration on the same node.

---

# 141. Topology Spread

Topology spread constraints are preferred for distributing replicas across available failure domains.

---

# 142. Node Distribution

At minimum, critical replicas should be distributed across multiple nodes where infrastructure permits.

---

# 143. Zone Distribution

High-availability services should distribute replicas across availability zones where the cluster topology supports it.

---

# 144. Topology Key

Topology policies may use keys such as:

```text
kubernetes.io/hostname

topology.kubernetes.io/zone
```

---

# 145. Hard vs Soft Scheduling

Strict anti-affinity can prevent scheduling during constrained capacity.

Policies must balance:

```text
availability distribution
```

against:

```text
schedulability
```

---

# 146. Autoscaling

Horizontal Pod Autoscaling should be used where workload characteristics justify it.

---

# 147. HPA

HPA may scale based on:

- CPU
- memory
- custom metrics
- external metrics

according to workload behavior.

---

# 148. CPU HPA

CPU-based scaling requires meaningful CPU requests.

---

# 149. Custom Metrics

For some workloads, business or application metrics may be better scaling signals than CPU.

Examples:

- SQS queue backlog/oldest-message age
- request concurrency
- queue depth

---

# 150. Scaling Security

Autoscaling does not remove the need for request/resource limits.

An attacker should not be able to cause unlimited infrastructure growth.

---

# 151. Maximum Replicas

Autoscaling must define a bounded maximum.

---

# 152. Minimum Replicas

Minimum replicas must reflect availability requirements.

---

# 153. Scale-to-Zero

Critical synchronous APIs should not scale to zero unless latency and availability requirements explicitly permit cold starts.

---

# 154. Vertical Pod Autoscaler

VPA may be used for recommendations or controlled resource adjustment where supported.

It must be evaluated against application restart behavior.

---

# 155. Cluster Autoscaler

Cluster autoscaling complements HPA but remains an infrastructure concern.

Applications must not assume new node capacity is instantaneous.

---

# 156. Node Selection

Workloads should use node selectors, affinity or taints/tolerations only when required by infrastructure or workload characteristics.

---

# 157. Tolerations

Broad tolerations that allow application workloads onto protected infrastructure nodes are prohibited.

---

# 158. Control Plane Nodes

Application workloads must not be scheduled onto control-plane nodes unless explicitly supported by the managed platform architecture.

---

# 159. Dedicated Nodes

Sensitive or resource-intensive workloads may use dedicated node pools where justified.

---

# 160. Runtime Classes

Alternative runtime classes or stronger sandboxing may be adopted for high-risk workloads where platform capabilities support them.

---

# 161. Ephemeral Containers

Ephemeral containers are primarily diagnostic tools.

---

# 162. Debug Access

Use of ephemeral containers in production must be restricted to authorized operators.

---

# 163. Debug Image

Diagnostic images must come from approved registries.

---

# 164. Debug Privileges

Debugging must not automatically grant privileged host access.

---

# 165. Production Debugging

Production debugging must preserve:

- access control
- auditability
- least privilege
- data confidentiality

---

# 166. kubectl exec

Production `kubectl exec` access must be restricted.

---

# 167. Shell Access

Routine production operation must not depend on opening shells inside application containers.

---

# 168. Immutable Operations

Preferred operational model:

```text
Observe

↓

Change source/configuration

↓

Build/validate

↓

Deploy
```

rather than manually changing running containers.

---

# 169. Port Forward

Production `kubectl port-forward` access must be restricted and audited according to platform capabilities.

---

# 170. Copy

`kubectl cp` into or out of production containers must be restricted.

---

# 171. Human Cluster Access

Human Kubernetes access must follow least privilege.

---

# 172. Authentication

Cluster users must authenticate through approved enterprise identity mechanisms.

---

# 173. Shared Accounts

Shared administrative Kubernetes user accounts are prohibited.

---

# 174. Individual Accountability

Administrative actions must be attributable to an individual or approved automation identity.

---

# 175. MFA

Privileged cluster access should require multi-factor authentication where enterprise identity infrastructure supports it.

---

# 176. Production Admin

Permanent cluster-admin access for ordinary developers is prohibited.

---

# 177. Just-in-Time Access

Privileged production access should be temporary or just-in-time where platform capabilities permit.

---

# 178. Break-Glass Access

Emergency cluster access must be:

- strongly authenticated
- narrowly controlled
- audited
- time bounded
- reviewed afterward

---

# 179. Kubernetes Audit

Kubernetes audit logging should be enabled according to managed-platform capabilities.

---

# 180. Audit Events

Audit records should provide visibility into security-relevant actions such as:

- workload creation
- workload modification
- secret access
- RBAC changes
- exec requests
- port-forward requests
- admission-policy changes
- ServiceAccount changes

---

# 181. Audit Access

Kubernetes audit logs are security-sensitive and require controlled access.

---

# 182. Audit Retention

Retention follows enterprise security and compliance requirements.

---

# 183. Runtime Monitoring

The platform should monitor workloads for runtime security anomalies.

---

# 184. Runtime Threat Detection

Where available, runtime threat detection should identify behaviors such as:

- unexpected shell execution
- suspicious process creation
- privilege escalation attempts
- sensitive filesystem access
- unexpected outbound network activity
- crypto-mining behavior
- container escape indicators

---

# 185. Behavioral Baseline

Runtime alerts should focus on deviations relevant to application workload behavior.

---

# 186. False Positives

Runtime detection rules require tuning to avoid excessive non-actionable alerts.

---

# 187. Security Alert

High-confidence runtime security alerts require defined incident-response procedures.

---

# 188. Runtime Process Model

Application images should contain only processes required by the service.

Unexpected utilities increase attack surface.

---

# 189. Shell in Image

Production runtime images should avoid shells where operationally practical.

Distroless/minimal images may reduce runtime attack surface.

---

# 190. Debuggability Trade-Off

Minimal images improve attack-surface reduction but can complicate debugging.

Ephemeral diagnostic containers provide a safer alternative.

---

# 191. Package Managers

Runtime images should avoid unnecessary package managers.

---

# 192. Image Vulnerability

A newly discovered critical image vulnerability requires remediation through ADR-024:

```text
Update dependency/base image

↓

Build new immutable image

↓

Scan

↓

Sign

↓

Promote

↓

Deploy
```

---

# 193. No In-Place Patch

The running container must not be manually patched.

---

# 194. Runtime Drift

Running container filesystem changes should be considered ephemeral and non-authoritative.

---

# 195. Configuration

Application configuration must be externalized appropriately.

---

# 196. ConfigMap

Non-sensitive configuration may use ConfigMaps.

---

# 197. ConfigMap Security

ConfigMaps are not appropriate for secrets.

---

# 198. Configuration Validation

Applications must validate required configuration during startup.

---

# 199. Secure Defaults

Missing security-sensitive configuration must fail securely.

---

# 200. Environment Configuration

Environment-specific configuration must not require rebuilding the image.

This preserves ADR-024's build-once principle.

---

# 201. Configuration Rollout

Configuration changes must be versioned and auditable.

---

# 202. ConfigMap Restart

Applications must explicitly define whether configuration is:

- dynamically reloadable
- restart-required

Silent assumptions are prohibited.

---

# 203. Secret Reload

Credential rotation should support secret reload or controlled rolling restart where required.

---

# 204. Labels

Workloads should use standardized labels.

Recommended dimensions include:

```text
application

service

version

environment

team
```

according to platform conventions.

---

# 205. Security Labels

Labels used by security policy must be controlled.

Applications must not self-assign labels that bypass admission or network policy.

---

# 206. Annotations

Annotations may contain operational metadata but must not contain secrets.

---

# 207. Pod Metadata

Sensitive business information must not be placed unnecessarily into:

- pod names
- labels
- annotations

because Kubernetes metadata is broadly observable to cluster tooling.

---

# 208. Observability

Kubernetes runtime observability integrates with ADR-014 and ADR-019.

---

# 209. Logs

Containers should write application logs to:

```text
stdout

stderr
```

for platform collection.

---

# 210. Log Files

Applications should not depend on persistent local log files inside containers.

---

# 211. Sensitive Logs

Runtime logs must follow ADR-023 sensitive-data requirements.

---

# 212. Metrics

Application metrics should expose bounded operational dimensions.

---

# 213. Pod Metrics

Platform monitoring should capture:

- CPU
- memory
- restarts
- throttling
- OOMKilled
- replica availability
- probe failures

---

# 214. Security Metrics

Relevant runtime-security metrics may include:

- admission denials
- policy violations
- unauthorized API attempts
- runtime alerts
- privileged workload attempts

---

# 215. Restart Count

Repeated pod restarts require investigation.

Restarting is recovery behavior, not a permanent solution.

---

# 216. CrashLoopBackOff

`CrashLoopBackOff` must be treated as an application/configuration/runtime failure requiring root-cause analysis.

---

# 217. Eviction

Pod eviction events should be monitored.

Common causes include:

- memory pressure
- disk pressure
- resource contention
- maintenance

---

# 218. Pending Pods

Long-running Pending pods may indicate:

- insufficient resources
- unsatisfiable affinity
- missing tolerations
- quota exhaustion

---

# 219. Deployment Monitoring

Deployments must be observed for:

- unavailable replicas
- readiness failure
- rollout timeout
- excessive restart
- scheduling failure

---

# 220. Rollout Failure

A failed rollout should stop progression rather than blindly continuing promotion.

---

# 221. Rollback

Rollback must use a previously approved immutable image according to ADR-021 and ADR-024.

---

# 222. Rollback Security

A rollback target must remain acceptable under current security policy.

---

# 223. Stateful Workloads

Stateful workloads require additional controls beyond ordinary stateless Deployments.

---

# 224. StatefulSet

StatefulSets should be used only where stable identity/storage semantics are required.

---

# 225. Database Deployment

Primary production databases should normally use managed or dedicated database infrastructure rather than being casually deployed as application pods.

---

# 226. PersistentVolume

Persistent storage requires explicit:

- access mode
- encryption
- backup
- retention
- lifecycle

policies.

---

# 227. StorageClass

Only approved StorageClasses should be used.

---

# 228. Volume Permissions

Mounted volumes must use least-privilege filesystem permissions.

---

# 229. fsGroup

`fsGroup` may be configured where required for mounted-volume access.

It should not be applied unnecessarily.

---

# 230. Backup Credentials

Backup workloads must use dedicated identities and secrets.

---

# 231. Jobs

Kubernetes Jobs must follow the same security baseline as long-running application pods.

---

# 232. CronJobs

CronJobs must define bounded execution behavior.

---

# 233. Job Concurrency

CronJobs should explicitly configure concurrency policy where duplicate execution is unsafe.

Example:

```yaml
concurrencyPolicy: Forbid
```

when semantics require it.

---

# 234. Job History

Job history limits should prevent uncontrolled accumulation.

---

# 235. Job Deadline

Long-running jobs should define appropriate execution deadlines where applicable.

---

# 236. Batch Idempotency

Batch processing must remain idempotent where Kubernetes retry/restart semantics can cause repeated execution.

---

# 237. Init Containers

Init containers must follow the same privilege and image-trust requirements as application containers.

---

# 238. Sidecars

Sidecars expand the pod attack surface.

Only required sidecars should be deployed.

---

# 239. Sidecar Identity

A sidecar sharing the pod network/security boundary must be treated as a trusted component of that workload.

---

# 240. Sidecar Resources

Sidecars require explicit resource requests/limits according to policy.

---

# 241. Sidecar Supply Chain

Sidecar images must follow ADR-024 supply-chain controls.

---

# 242. DaemonSets

DaemonSets have broad node coverage and therefore require stronger review.

Ordinary business applications should not deploy DaemonSets.

---

# 243. Operators

Kubernetes Operators may hold broad cluster permissions.

Operator installation requires platform/security review.

---

# 244. Custom Resource Definitions

CRDs extend the Kubernetes API and must be governed as platform-level capabilities.

---

# 245. Admission Webhooks

Admission webhooks are critical cluster components.

Failure policy and availability must be designed carefully.

---

# 246. Fail Open vs Fail Closed

Security-critical admission controls should normally fail closed.

Availability implications must be engineered explicitly.

---

# 247. Webhook Availability

A security admission webhook must itself be highly available.

---

# 248. Policy Dependency

Critical admission policy must not depend on fragile external services without resilient architecture.

---

# 249. Control Plane

Application teams must not depend on undocumented control-plane implementation details.

---

# 250. Kubernetes Version

Clusters must run supported Kubernetes versions according to platform lifecycle policy.

---

# 251. Upgrade

Cluster upgrades must be tested against:

- APIs
- manifests
- admission policies
- controllers
- networking
- storage

---

# 252. Deprecated Kubernetes APIs

Deprecated Kubernetes API versions must be migrated before removal.

---

# 253. Manifest API Version

Deployment manifests must use supported API versions.

---

# 254. Client Version

Administrative Kubernetes clients should remain reasonably compatible with cluster versions.

---

# 255. Supply-Chain Integration

ADR-024 determines whether an artifact can be trusted before runtime.

ADR-025 determines whether that trusted artifact receives safe runtime privileges.

---

# 256. API Security Integration

ADR-023 determines application identity and authorization.

Kubernetes network location must never replace those controls.

---

# 257. Zero-Downtime Integration

ADR-021 defines deployment compatibility.

Kubernetes runtime configuration must provide:

- sufficient replicas
- readiness
- graceful shutdown
- rolling strategy
- disruption protection
- topology distribution

to implement it.

---

# 258. Kubernetes Architecture Integration

ADR-015 establishes Kubernetes as the workload platform.

This ADR defines the mandatory runtime-security posture on that platform.

---

# 259. Security Boundary Model

The resulting deployment trust chain becomes:

```text
Reviewed Source

↓

Trusted CI/CD

↓

Scanned Artifact

↓

SBOM + Provenance

↓

Signed Image

↓

Approved Registry

↓

Admission Verification

↓

Restricted Pod

↓

Least-Privilege Identity

↓

Restricted Network

↓

Authenticated Application

↓

Authorized Business Operation

↓

Audited Runtime
```

---

# 260. Anti-Patterns

The following are prohibited:

- privileged application containers
- routine root execution
- `allowPrivilegeEscalation: true`
- unrestricted Linux capabilities
- `seccomp: Unconfined`
- unnecessary writable root filesystems
- runtime package installation
- host networking for ordinary services
- host PID/IPC access
- arbitrary hostPath mounts
- missing production resource requests
- uncontrolled memory consumption
- broad ClusterRole permissions
- wildcard Kubernetes RBAC
- sharing broad ServiceAccounts across unrelated applications
- unnecessary ServiceAccount token automount
- static broad cloud credentials when workload identity exists
- production secrets committed to Git
- treating Base64 as encryption
- unrestricted pod networking
- treating NetworkPolicy as application authorization
- public direct pod exposure
- production deployment with `latest`
- mutable image identity
- bypassing admission policies
- external dependency checks in liveness
- expensive database queries in health probes
- arbitrary sleeps instead of proper graceful shutdown
- single-replica critical services without explicit availability acceptance
- placing all replicas on one node unnecessarily
- unlimited HPA maximum
- unrestricted `kubectl exec`
- shared administrator accounts
- permanent cluster-admin for ordinary developers
- unaudited emergency access
- manual in-place container patching
- secrets in ConfigMaps
- secrets in pod labels or annotations
- ignoring repeated OOMKilled events
- blindly increasing resources without investigation
- using untrusted debug images
- running arbitrary business DaemonSets
- unreviewed Kubernetes Operators
- security admission controls with no availability design
- outdated Kubernetes API versions
- relying on cluster network location instead of API authorization

---

# 261. Positive Consequences

The decision provides:

- stronger workload isolation
- reduced container privileges
- smaller runtime attack surface
- improved network segmentation
- least-privilege Kubernetes identities
- stronger cloud credential security
- safer secret management
- controlled resource consumption
- better cluster stability
- secure image admission
- stronger supply-chain enforcement
- improved zero-downtime deployment
- improved disruption tolerance
- better replica distribution
- controlled autoscaling
- stronger operational accountability
- better runtime threat detection
- improved incident response
- standardized Kubernetes manifests

---

# 262. Negative Consequences

The decision introduces:

- more Kubernetes configuration
- admission-policy complexity
- NetworkPolicy maintenance
- RBAC governance
- resource-sizing work
- workload-identity configuration
- secret-management integration
- runtime-security tooling
- stricter debugging controls
- additional production access procedures
- possible scheduling constraints
- policy exception management

These costs are accepted because Kubernetes runtime defaults alone do not provide sufficient production security.

---

# 263. Neutral Consequences

The decision also means:

- some workloads may require documented security exceptions
- developers may have less direct production shell access
- minimal images can make debugging less convenient
- strict topology constraints can affect scheduling
- resource limits require ongoing tuning
- NetworkPolicies require dependency awareness
- admission enforcement may reject previously accepted manifests
- security posture becomes partially dependent on cluster/platform capabilities

---

# 264. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Container compromise gains root | Critical | Medium | Non-root and no privilege escalation |
| Container escape impact increases | Critical | Low | Restricted profile, seccomp and capability removal |
| Compromised pod accesses unrelated services | Critical | Medium | NetworkPolicy default deny |
| Compromised ServiceAccount controls cluster | Critical | Low | Least-privilege RBAC |
| Cloud credential stolen from pod | Critical | Medium | Workload identity and short-lived credentials |
| Secret exposed in Git | Critical | Medium | External secret management and secret scanning |
| Malicious image deployed | Critical | Low | Digest, signing and admission verification |
| Memory leak destabilizes node | High | Medium | Resource limits and monitoring |
| CPU contention causes latency cascade | High | Medium | Requests, sizing and monitoring |
| Database outage causes restart storm | High | Medium | Correct liveness design |
| Deployment drops traffic | High | Medium | Readiness and graceful shutdown |
| Node maintenance removes too many replicas | High | Medium | PDB |
| Zone failure removes all replicas | Critical | Low | Topology spread |
| HPA creates excessive infrastructure load | High | Medium | Bounded max replicas |
| Debug access leaks data | High | Medium | Restricted audited access |
| Admission webhook outage blocks cluster | High | Low | HA policy infrastructure |
| Runtime attack remains undetected | High | Medium | Runtime threat detection |
| Security exception becomes permanent | High | Medium | Expiration and periodic review |
| Kubernetes upgrade breaks manifests | High | Medium | API lifecycle governance |

---

# 265. Implementation Guidance

The following rules are mandatory:

1. Application workloads should target the Restricted Pod Security Standard.
2. Production application containers must normally run as non-root.
3. Privileged containers are prohibited for ordinary services.
4. Privilege escalation must normally be disabled.
5. Linux capabilities should default to `drop: ALL`.
6. Seccomp should use `RuntimeDefault`.
7. Root filesystem should be read-only where technically possible.
8. Writable locations must be explicit.
9. Runtime package installation is prohibited.
10. Host networking, PID and IPC access are prohibited for ordinary services.
11. `hostPath` is prohibited for ordinary application workloads.
12. Production containers must define resource requests.
13. Memory limits must account for complete JVM memory consumption.
14. Dedicated ServiceAccounts should be used where workload identity is required.
15. ServiceAccount token automount should be disabled when Kubernetes API access is unnecessary.
16. Kubernetes RBAC must follow least privilege.
17. Wildcard RBAC is prohibited for ordinary services.
18. Workload identity should replace static cloud credentials where available.
19. Production secrets must not be stored in Git.
20. Kubernetes Secret Base64 encoding must not be treated as encryption.
21. Production namespaces should use NetworkPolicies where supported.
22. Network security should target default-deny with explicit allows.
23. NetworkPolicy must not replace application authentication/authorization.
24. External APIs must use approved ingress/gateway infrastructure.
25. Production workloads must use approved image registries.
26. `latest` alone is prohibited.
27. Immutable image digests should identify production artifacts.
28. Image signature/attestation verification should be enforced where supported.
29. Critical security policies should be enforced through admission control.
30. Liveness must not depend on ordinary external dependency availability.
31. Readiness must reflect ability to safely receive traffic.
32. Startup probes should protect slow application initialization where necessary.
33. Production services must support graceful shutdown.
34. Termination grace periods must reflect realistic workload behavior.
35. Critical stateless services should use rolling deployment.
36. Critical replicated services should use PodDisruptionBudget.
37. High-availability services must use multiple replicas.
38. Critical replicas should be distributed across nodes/failure domains.
39. Autoscaling must use bounded minimum and maximum replica counts.
40. Production debug access must be restricted and auditable.
41. Routine operations must not depend on `kubectl exec`.
42. Human cluster access must use individual identities.
43. Permanent cluster-admin access for ordinary developers is prohibited.
44. Break-glass access must be controlled and audited.
45. Kubernetes audit logging should be enabled according to platform capabilities.
46. Runtime security monitoring should be enabled for critical workloads.
47. ConfigMaps must not contain secrets.
48. Runtime configuration changes must be versioned and auditable.
49. Jobs, CronJobs, init containers and sidecars must follow the same security baseline.
50. Kubernetes versions and manifest API versions must remain supported.

---

# 266. Validation

The decision will be validated through:

- manifest linting
- Kubernetes schema validation
- Pod Security enforcement
- admission-policy tests
- securityContext validation
- RBAC review
- ServiceAccount review
- NetworkPolicy tests
- workload-identity validation
- secret-management validation
- image-digest validation
- signature/attestation verification
- resource-sizing review
- JVM memory tests
- liveness tests
- readiness tests
- startup tests
- graceful-shutdown tests
- rolling-deployment tests
- PodDisruptionBudget tests
- node-drain exercises
- topology-spread validation
- HPA load tests
- production-access review
- Kubernetes audit review
- runtime-security alert tests
- cluster-upgrade compatibility tests
- production-readiness review

---

# 267. Success Criteria

The decision is successful when:

- production application pods run without root privileges
- privilege escalation is disabled
- unnecessary Linux capabilities are absent
- seccomp is enforced
- writable filesystem access is minimized
- every workload has controlled resources
- application ServiceAccounts have minimal permissions
- workloads without Kubernetes API requirements do not receive unnecessary tokens
- pod network access is restricted to required dependencies
- production secrets do not reside in Git
- static cloud credentials are minimized
- production images have immutable identities
- admission policies reject unsafe workloads
- probes behave correctly during dependency failures
- rolling deployments do not interrupt supported traffic
- pod termination drains in-flight work safely
- voluntary disruptions preserve required availability
- critical replicas are distributed across failure domains
- autoscaling remains bounded
- production debugging is attributable and auditable
- runtime anomalies can be detected
- every running workload can be correlated with its trusted supply-chain artifact

---

# 268. Alternatives Rejected

## 268.1 Kubernetes Defaults Only

Rejected because default Kubernetes workload behavior does not enforce the required least-privilege security posture.

---

## 268.2 Root Containers

Rejected because ordinary Java/Spring Boot services do not require root execution.

---

## 268.3 Privileged Containers

Rejected because they dramatically increase host compromise impact.

---

## 268.4 Flat Cluster Network

Rejected because a compromised workload would receive excessive lateral movement capability.

---

## 268.5 Shared Broad ServiceAccount

Rejected because compromise of one service would expose permissions unrelated to its capability.

---

## 268.6 Static Cloud Credentials

Rejected where workload identity exists because long-lived credentials increase compromise impact and rotation complexity.

---

## 268.7 Liveness Checking Every Dependency

Rejected because dependency outages could trigger cluster-wide restart storms.

---

## 268.8 Single Replica for Critical APIs

Rejected because it cannot provide zero-downtime deployment or resilience to ordinary pod/node failure.

---

## 268.9 Manual Runtime Patching

Rejected because it destroys artifact immutability, provenance and reproducibility.

---

## 268.10 Unrestricted Production Shell Access

Rejected because it increases operational risk and weakens accountability.

---

# 269. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-024: Adopt Software Supply Chain Security
- ADR-026: Adopt Platform Configuration and Secret Management Standards

---

# 270. References

- Kubernetes Documentation
- Kubernetes Pod Security Standards
- Kubernetes RBAC Documentation
- Kubernetes NetworkPolicy Documentation
- Kubernetes Security Checklist
- Kubernetes Security Context Documentation
- Kubernetes Seccomp Documentation
- Kubernetes Resource Management Documentation
- Kubernetes Horizontal Pod Autoscaling
- Kubernetes PodDisruptionBudget
- Kubernetes Topology Spread Constraints
- Kubernetes Admission Control
- Kubernetes Audit Documentation
- OWASP Kubernetes Top Ten
- NSA/CISA Kubernetes Hardening Guidance
- CIS Kubernetes Benchmark
- NIST Container Security Guidance
- Spring Boot Actuator Documentation
- Java 21 Documentation
- OCI Image Specification
- SLSA
- Sigstore
- ADR-015: Deploy Workloads on Kubernetes
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-023: Adopt API Security Standards
- ADR-024: Adopt Software Supply Chain Security

---

# 271. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial Kubernetes runtime security baseline |

---

# 272. Decision Summary

The Enterprise Order Platform adopts a restricted Kubernetes runtime model.

The complete workload trust chain becomes:

```text
REVIEWED SOURCE

↓

TRUSTED CI/CD

↓

SCANNED ARTIFACT

↓

SBOM + PROVENANCE

↓

SIGNED IMAGE

↓

APPROVED REGISTRY

↓

ADMISSION VERIFICATION

↓

RESTRICTED POD

↓

NON-ROOT PROCESS

↓

MINIMAL CAPABILITIES

↓

SECCOMP

↓

READ-ONLY FILESYSTEM

↓

LEAST-PRIVILEGE IDENTITY

↓

RESTRICTED NETWORK

↓

AUTHENTICATED API

↓

AUTHORIZED BUSINESS OPERATION

↓

OBSERVED + AUDITED RUNTIME
```

The default application-container posture is:

```yaml
securityContext:
  runAsNonRoot: true
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - ALL
  seccompProfile:
    type: RuntimeDefault
```

The default network posture is:

```text
DENY

↓

EXPLICITLY ALLOW REQUIRED COMMUNICATION
```

The default identity posture is:

```text
Dedicated Workload Identity

+

Least-Privilege ServiceAccount

+

Minimal RBAC

+

No Unnecessary Kubernetes API Token
```

The default availability posture is:

```text
Multiple Replicas

+

Correct Readiness

+

Correct Liveness

+

Graceful Shutdown

+

Rolling Update

+

PodDisruptionBudget

+

Topology Distribution

+

Bounded Autoscaling
```

A workload being successfully scheduled by Kubernetes does **not** establish that it is secure.

A production workload is considered compliant only when:

```text
Artifact Trust

+

Runtime Restriction

+

Identity Restriction

+

Network Restriction

+

Resource Governance

+

Availability Controls

+

Runtime Observability

+

Auditability
```

are applied together.

This ADR therefore connects the platform's four critical deployment-security layers:

```text
ADR-015
Kubernetes Platform

↓

ADR-021
Zero-Downtime Deployment

↓

ADR-024
Trusted Software Supply Chain

↓

ADR-025
Restricted Runtime Execution
```

while ADR-023 continues to enforce authentication and business authorization **inside the application**, independent of Kubernetes network location.
