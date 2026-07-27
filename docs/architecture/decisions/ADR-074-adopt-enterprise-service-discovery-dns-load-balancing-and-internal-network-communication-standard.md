# ADR-074: Adopt Enterprise Service Discovery, DNS, Load Balancing and Internal Network Communication Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-074 |
| Title | Adopt Enterprise Service Discovery, DNS, Load Balancing and Internal Network Communication Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Service Discovery, DNS, Load Balancing, Internal Networking |
| Related Work Items | Kubernetes, AWS, Spring Boot, HTTP Clients, Service Mesh, mTLS |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Distributed services communicate through dynamic infrastructure.

A typical Kubernetes deployment may contain:

```text
ORDERS SERVICE
    |
    v
KUBERNETES SERVICE
    |
    v
MULTIPLE PODS
    |
    +--> POD A
    +--> POD B
    +--> POD C
```

Service instances may be created, replaced, restarted, rescheduled or scaled continuously.

Applications therefore MUST NOT assume:

```text
Fixed Pod IP

Single Instance

Permanent Connection

Static DNS Resolution Forever
```

Internal networking must account for:

```text
Service Discovery

DNS

Load Balancing

Connection Pooling

Keep-Alive

Timeouts

Retries

Pod Termination

Zone Failure

Network Policy

TLS

Service Mesh

Cross-Region Communication
```

---

# 2. Problem Statement

The organization requires standards covering:

- service discovery
- Kubernetes Service
- ClusterIP
- DNS
- service names
- headless services
- load balancing
- client-side load balancing
- server-side load balancing
- connection pooling
- HTTP keep-alive
- HTTP/2
- DNS caching
- DNS TTL
- stale DNS
- stale connections
- connection draining
- retries
- zone awareness
- multi-AZ networking
- cross-region traffic
- service mesh
- mTLS
- network policies
- internal ingress
- private endpoints
- endpoint discovery
- resilience
- observability

---

# 3. Decision Drivers

Primary drivers are:

1. service availability
2. dynamic scaling
3. fault tolerance
4. network efficiency
5. low latency
6. security
7. operational simplicity
8. predictable failover
9. connection reuse
10. topology awareness
11. observability
12. minimal application coupling to infrastructure

---

# 4. Decision

Kubernetes-native service discovery SHOULD be the standard internal discovery mechanism for workloads deployed within Kubernetes.

Canonical flow:

```text
CLIENT SERVICE
      |
      v
DNS NAME
      |
      v
KUBERNETES SERVICE
      |
      v
ENDPOINT SET
      |
      +--> POD A
      +--> POD B
      +--> POD C
```

Applications SHOULD depend on stable service identities rather than pod addresses.

---

# 5. Fundamental Principle

```text
Services are stable.

Instances are disposable.

Applications must connect
to service identities,
not infrastructure instances.
```

---

# 6. Kubernetes Service

Internal service discovery SHOULD use Kubernetes `Service` resources where appropriate.

---

# 7. ClusterIP

`ClusterIP` SHOULD be the default for ordinary internal service-to-service communication.

---

# 8. Pod IP

Applications MUST NOT configure direct pod IPs as normal service endpoints.

---

# 9. Pod Replacement

A pod IP may disappear at any time due to:

```text
Restart

Rescheduling

Scaling

Deployment

Node Failure
```

---

# 10. Service DNS

Clients SHOULD use stable DNS names.

Example:

```text
orders-service
```

or fully qualified:

```text
orders-service.orders.svc.cluster.local
```

when required.

---

# 11. Short DNS Name

Short service names MAY be used when namespace resolution is unambiguous.

---

# 12. Cross-Namespace Communication

Cross-namespace calls SHOULD use explicit namespace-aware DNS names.

Example:

```text
customers-service.customers.svc.cluster.local
```

---

# 13. DNS as Discovery

Kubernetes DNS is part of service discovery and MUST be treated as an operational dependency.

---

# 14. DNS Failure

DNS failures MUST be observable and distinguished from application-level remote failures.

---

# 15. DNS Cache

Applications/JVMs MAY cache DNS results.

Caching behavior MUST not prevent timely discovery of endpoint changes.

---

# 16. Infinite DNS Cache

Infinite DNS caching SHOULD NOT be assumed safe in dynamic environments.

---

# 17. JVM DNS

Java DNS caching behavior MUST be understood when using long-lived processes.

---

# 18. DNS TTL

DNS TTL SHOULD reflect the dynamic nature of the platform.

---

# 19. Service DNS Stability

The service DNS name SHOULD remain stable even as individual endpoints change.

---

# 20. DNS Is Not Health Checking

Successful DNS resolution does not prove that a backend instance is healthy.

---

# 21. Endpoint Health

Kubernetes endpoints SHOULD include only ready pods for normal traffic.

---

# 22. Readiness

Readiness probes MUST determine whether a pod is eligible to receive service traffic.

---

# 23. Liveness

Liveness MUST NOT be used as the primary load-balancing health signal.

---

# 24. Headless Service

Headless services MAY be used when consumers need direct endpoint discovery.

Example:

```yaml
clusterIP: None
```

---

# 25. Headless Use Cases

Typical use cases include:

```text
Stateful Distributed Systems

Direct Peer Discovery

Specialized Client-Side Balancing
```

---

# 26. Headless Caution

Headless services SHOULD NOT be used merely to bypass Kubernetes load balancing without a specific requirement.

---

# 27. Load Balancing

Internal traffic MUST use an explicit load-balancing strategy.

---

# 28. Kubernetes Service Load Balancing

For ordinary services:

```text
CLIENT
   |
   v
CLUSTERIP
   |
   v
READY POD
```

is the standard model.

---

# 29. Server-Side Load Balancing

Kubernetes Service provides infrastructure-level/server-side load distribution from the application's perspective.

---

# 30. Client-Side Load Balancing

Client-side load balancing MAY be used when:

```text
Endpoint Awareness

Topology Awareness

Specialized Protocol Behavior

Per-Endpoint Metrics

Advanced Retry Routing
```

provide material value.

---

# 31. Client-Side Complexity

Client-side load balancing introduces:

```text
Endpoint Discovery

Health Management

Retry Decisions

Balancing Logic
```

into application/client libraries.

It SHOULD therefore be justified.

---

# 32. Spring Cloud LoadBalancer

Spring Cloud LoadBalancer MAY be used when client-side balancing is explicitly required.

It SHOULD NOT be introduced automatically when Kubernetes Service routing is sufficient.

---

# 33. Double Load Balancing

Avoid unnecessary:

```text
Client-Side Load Balancer
        +
Kubernetes Service Load Balancer
```

when the two layers provide no distinct value.

---

# 34. Load Balancing Algorithm

Infrastructure SHOULD use an appropriate supported load-balancing algorithm.

Applications SHOULD NOT assume strict round-robin behavior unless guaranteed.

---

# 35. Uneven Traffic

Long-lived persistent connections can create uneven request distribution even when connection establishment is balanced.

---

# 36. Connection Pooling

HTTP clients MUST reuse connections through bounded connection pools.

---

# 37. New Connection per Request

Creating a new TCP/TLS connection for every request SHOULD be avoided.

---

# 38. Connection Reuse

Connection reuse reduces:

```text
TCP Handshake Cost

TLS Handshake Cost

Latency

CPU

Network Overhead
```

---

# 39. Pool Bounds

Connection pools MUST define bounds.

Applicable settings include:

```text
Max Connections

Pending Acquire Limit

Acquire Timeout

Idle Timeout

Connection Lifetime
```

---

# 40. Unlimited Pool

Unbounded connection pools are prohibited.

---

# 41. Pool Capacity

Pool capacity MUST align with:

```text
Expected Concurrency

Downstream Capacity

Pod Count

Timeouts

Request Rate
```

---

# 42. Connection Pool Is Backpressure

The pool is one layer of concurrency control.

---

# 43. Pending Acquire

Waiting indefinitely for a connection is prohibited.

---

# 44. Acquisition Timeout

Connection acquisition MUST have a bounded timeout.

---

# 45. Connect Timeout

TCP connection establishment MUST have a bounded timeout.

---

# 46. Response Timeout

Remote response waiting MUST have a bounded timeout.

---

# 47. Idle Timeout

Idle connections SHOULD be evicted after a configured duration.

---

# 48. Connection Lifetime

Maximum connection lifetime SHOULD be considered in environments where infrastructure/network endpoints change over time.

---

# 49. Stale Connection

A pooled connection can become invalid because:

```text
Pod terminated

Load balancer closed connection

NAT state expired

Firewall state expired

Network changed
```

---

# 50. Stale Connection Handling

Clients MUST safely detect and replace stale connections.

---

# 51. Keep-Alive

HTTP keep-alive SHOULD be enabled for internal HTTP communication.

---

# 52. Keep-Alive Compatibility

Client and infrastructure idle timeouts SHOULD be compatible.

---

# 53. Timeout Mismatch

If:

```text
LOAD BALANCER IDLE TIMEOUT = 60s
```

but:

```text
CLIENT ASSUMES CONNECTION VALID FOR 10m
```

stale connection failures may occur.

---

# 54. Timeout Alignment

Network timeout hierarchies SHOULD be intentionally aligned.

---

# 55. HTTP/1.1

HTTP/1.1 with connection pooling remains acceptable for ordinary service communication.

---

# 56. HTTP/2

HTTP/2 SHOULD be considered when:

```text
Multiplexing

Large Concurrent Request Volume

Protocol Efficiency

gRPC
```

provide meaningful value.

---

# 57. HTTP/2 Multiplexing

HTTP/2 allows multiple streams over fewer TCP connections.

---

# 58. HTTP/2 Concentration Risk

A small number of HTTP/2 connections may concentrate significant traffic on specific endpoints depending on load-balancing architecture.

---

# 59. HTTP/2 Reset

Clients MUST correctly handle stream/connection resets.

---

# 60. gRPC

gRPC MAY use HTTP/2 for internal service contracts where its contract/performance characteristics are justified.

---

# 61. gRPC Is Not Default REST Replacement

gRPC SHOULD NOT automatically replace REST simply because services are internal.

---

# 62. Connection Establishment

Connection creation SHOULD occur lazily or during initialization according to client/runtime semantics.

---

# 63. Startup Dependency

An application SHOULD NOT fail startup merely because an optional downstream service is temporarily unavailable unless that dependency is essential for readiness/startup.

---

# 64. DNS and Startup

DNS resolution at startup SHOULD not unnecessarily couple application boot to downstream availability.

---

# 65. Service Discovery Failure

Runtime discovery failure SHOULD use explicit error classification.

---

# 66. Retry

Network retries MUST follow ADR-055.

---

# 67. Retry Target

Retries SHOULD re-attempt through the service/discovery layer so another healthy endpoint may be selected.

---

# 68. Same Dead Connection

A retry MUST NOT repeatedly reuse a known invalid connection.

---

# 69. Retry Safety

Only retry operations that are safe according to idempotency semantics.

---

# 70. Connection Failure Retry

A connection failure occurring before request transmission MAY be safer to retry than an ambiguous timeout after transmission.

---

# 71. Unknown Outcome

Network failure after bytes were transmitted may produce an unknown remote outcome.

---

# 72. Idempotency

Mutating retries MUST follow ADR-072 idempotency requirements.

---

# 73. Circuit Breaker

Circuit Breakers SHOULD operate at the logical dependency level.

Example:

```text
customers-service
```

rather than:

```text
pod-10-23-4-17
```

for ordinary service behavior.

---

# 74. Per-Endpoint Circuit Breaker

Per-endpoint circuit breakers MAY be appropriate with explicit client-side endpoint-aware balancing.

---

# 75. Dependency Health

A few failed pods should not automatically mark the entire logical service unavailable if healthy capacity remains.

---

# 76. Load Balancer Health

Infrastructure SHOULD route only to healthy/ready endpoints.

---

# 77. Pod Termination

Pod termination MUST support connection draining.

---

# 78. Graceful Termination Flow

Preferred flow:

```text
POD MARKED NOT READY
       |
       v
REMOVED FROM SERVICE ENDPOINTS
       |
       v
STOP NEW TRAFFIC
       |
       v
DRAIN IN-FLIGHT REQUESTS
       |
       v
TERMINATE
```

---

# 79. SIGTERM

Applications MUST handle normal container termination signals correctly.

---

# 80. Grace Period

Kubernetes `terminationGracePeriodSeconds` MUST allow reasonable in-flight completion.

---

# 81. PreStop

A `preStop` hook MAY be used where infrastructure propagation delay requires additional draining time.

---

# 82. Fixed Sleep PreStop

Blind long sleeps SHOULD NOT be the sole shutdown design.

---

# 83. Graceful Shutdown

Spring Boot graceful shutdown SHOULD be enabled where appropriate.

---

# 84. In-Flight Request

In-flight requests SHOULD receive a bounded chance to complete.

---

# 85. New Request During Shutdown

Once termination begins, the pod SHOULD stop receiving new work as soon as practical.

---

# 86. Connection Draining

Infrastructure load balancers MUST support deregistration/connection draining where relevant.

---

# 87. Persistent Connection

Long-lived connections MUST be handled carefully during rolling deployments.

---

# 88. WebSocket

WebSocket/long-lived streaming connections require explicit deployment/draining semantics.

---

# 89. Zone Awareness

Production workloads SHOULD span multiple availability zones where platform architecture supports it.

---

# 90. Cross-Zone Traffic

Cross-zone traffic may introduce:

```text
Latency

Cost

Failure Coupling
```

and SHOULD be understood.

---

# 91. Topology-Aware Routing

Topology-aware routing MAY prefer local-zone endpoints when sufficient healthy capacity exists.

---

# 92. Locality Is Optimization

Zone locality MUST NOT compromise availability when local capacity is unavailable.

---

# 93. Topology Spread

Backend pods SHOULD use topology spread/anti-affinity according to ADR-060.

---

# 94. Single Zone Service

Critical services MUST NOT unintentionally place all replicas in one failure domain.

---

# 95. Zone Failure

Clients/infrastructure MUST tolerate endpoint loss from one zone.

---

# 96. Regional Communication

Cross-region synchronous communication SHOULD be minimized.

---

# 97. Cross-Region Latency

Cross-region calls add:

```text
Higher Latency

Network Variance

Cost

Failure Modes
```

---

# 98. Region-Local Preference

Business operations SHOULD prefer region-local dependencies when architecture permits.

---

# 99. Multi-Region Data

Multi-region writes require explicit data consistency and ownership design.

---

# 100. Cross-Region Retry

Retrying across regions MUST consider business consistency and idempotency.

---

# 101. Cross-Region Failover

Automatic regional failover MUST be designed together with:

```text
Data Replication

DNS

Traffic Routing

State Ownership

RPO/RTO
```

and not only network routing.

---

# 102. Network Policy

Kubernetes NetworkPolicy SHOULD restrict workload communication according to least privilege.

---

# 103. Default Allow

A cluster-wide unrestricted flat network SHOULD NOT be assumed acceptable for sensitive production workloads.

---

# 104. Default Deny

Namespaces SHOULD consider default-deny ingress/egress policies where platform operations support them.

---

# 105. Explicit Allow

Required communication SHOULD be explicitly permitted.

Example:

```text
orders-service
    ->
customers-service
```

---

# 106. Database Network Policy

Only services requiring PostgreSQL access SHOULD be allowed to connect to it.

---

# 107. Redis Network Policy

Redis should only be reachable by authorized workloads.

---

# 108. Kafka Network Policy

Kafka connectivity SHOULD be limited to services requiring broker access.

---

# 109. Egress

Outbound Internet access SHOULD be restricted where practical.

---

# 110. SSRF Mitigation

Egress controls provide defense in depth against SSRF and compromised workload behavior.

---

# 111. Private Endpoint

AWS services SHOULD use private connectivity/endpoints where justified by security, availability and cost requirements.

---

# 112. NAT Gateway

NAT Gateway usage SHOULD be understood because it can affect:

```text
Cost

Port Exhaustion

Availability

Routing
```

---

# 113. Connection Scaling

Large numbers of outbound connections may exhaust:

```text
NAT Ports

Load Balancer Connections

Downstream Limits
```

even when application threads are inexpensive.

---

# 114. Virtual Threads

Virtual Threads MUST NOT create unbounded network concurrency.

---

# 115. Network Capacity

Concurrency limits MUST consider actual network and downstream resource capacity.

---

# 116. Service Mesh

A service mesh MAY be adopted when cross-cutting network requirements justify its operational complexity.

---

# 117. Service Mesh Candidate

Potential capabilities include:

```text
mTLS

Traffic Policy

Telemetry

Retries

Circuit Breaking

Traffic Splitting

Identity
```

---

# 118. Service Mesh Is Not Mandatory

A mesh MUST NOT be introduced merely because the architecture uses microservices.

---

# 119. Mesh Cost

A service mesh introduces:

```text
Operational Complexity

Resource Overhead

Additional Failure Modes

Configuration Surface

Debugging Complexity
```

---

# 120. Sidecar

Sidecar-based meshes add per-pod resource and networking overhead.

---

# 121. Ambient Mesh

Sidecarless/ambient architectures MAY reduce some overhead but still require governance.

---

# 122. Mesh Policy Ownership

If a mesh is adopted, ownership of:

```text
Retries

Timeouts

mTLS

Circuit Breaking
```

must be explicit between mesh and application.

---

# 123. Retry Duplication

Mesh retries plus application retries can create retry amplification.

---

# 124. Mesh Timeout

Mesh timeout MUST align with application deadlines.

---

# 125. mTLS

mTLS SHOULD be considered for service-to-service authentication where required by zero-trust/platform architecture.

---

# 126. Encryption

mTLS provides transport encryption and mutual workload authentication.

---

# 127. mTLS Is Not Authorization

mTLS identity does not automatically authorize business/resource access.

---

# 128. Workload Identity

Certificates/service identities SHOULD map to stable workload identities rather than pod IPs.

---

# 129. Certificate Rotation

mTLS certificates MUST support automatic rotation.

---

# 130. Static Certificate

Static long-lived certificates inside container images are prohibited.

---

# 131. Mesh Bypass

Applications MUST NOT bypass required mesh/security controls through alternate ungoverned routes.

---

# 132. Internal Load Balancer

Internal cloud load balancers MAY expose services across cluster/VPC boundaries when required.

---

# 133. Public Load Balancer

Services intended only for internal communication MUST NOT be accidentally exposed through public load balancers.

---

# 134. Internal DNS

Private DNS SHOULD resolve private service endpoints where appropriate.

---

# 135. Split-Horizon DNS

Split-horizon DNS MAY be used where internal/external resolution differs intentionally.

---

# 136. DNS Naming

Service DNS names SHOULD remain stable and environment-appropriate.

---

# 137. Environment in Hostname

Environment-specific hostnames MAY be used when services cross cluster/network boundaries.

---

# 138. Hardcoded Hostname

Infrastructure hostnames MUST remain externalized configuration.

---

# 139. IP Literal

Hardcoded infrastructure IP addresses in application configuration are strongly discouraged.

---

# 140. Service Registry

A separate service registry such as Eureka/Consul SHOULD NOT be introduced when Kubernetes-native discovery already satisfies the requirement.

---

# 141. Duplicate Registry

Running an application service registry plus Kubernetes discovery without distinct need adds unnecessary complexity.

---

# 142. Consul

Consul MAY be used when non-Kubernetes workloads or broader service-discovery requirements justify it.

---

# 143. Eureka

Eureka MAY remain in legacy environments but SHOULD not be adopted by default for Kubernetes-native workloads.

---

# 144. Discovery Abstraction

Applications SHOULD depend on logical service configuration rather than registry-specific APIs where possible.

---

# 145. Internal HTTP URL

Preferred:

```text
http://customers-service
```

or approved secure equivalent.

Avoid:

```text
http://10.10.17.43:8080
```

---

# 146. Port

Service ports SHOULD remain stable contract/infrastructure configuration.

---

# 147. Named Port

Kubernetes named ports SHOULD be used where they improve clarity.

---

# 148. Health Port

Management/health ports MAY be separated from business traffic where security/operational architecture requires it.

---

# 149. Management Exposure

Management ports MUST not be broadly exposed internally without need.

---

# 150. Connection Timeout Budget

Connection timeouts SHOULD be significantly shorter than overall request deadlines.

---

# 151. DNS Timeout

DNS resolution MUST not consume unbounded portions of request deadlines.

---

# 152. Connection Pool Wait

Pool acquisition wait MUST be included in the end-to-end latency budget.

---

# 153. End-to-End Deadline

Conceptually:

```text
TOTAL REQUEST DEADLINE
      |
      +--> DNS
      +--> POOL ACQUIRE
      +--> CONNECT
      +--> TLS
      +--> REMOTE PROCESSING
      +--> RETRY
```

All components consume the same finite budget.

---

# 154. Timeout Multiplication

Sequential timeout maxima MUST NOT exceed the caller's deadline by large margins.

---

# 155. DNS Retry

DNS retries SHOULD be bounded.

---

# 156. Network Error Taxonomy

Internal clients SHOULD distinguish:

```text
DNS Failure

Connection Refused

Connection Timeout

Connection Reset

TLS Failure

Response Timeout

HTTP Error
```

where operationally useful.

---

# 157. TLS Failure

TLS errors MUST NOT be downgraded into generic "service unavailable" logs without preserving diagnostic classification.

---

# 158. Certificate Error

Certificate-validation failures SHOULD be treated as security/configuration defects, not normal retry conditions.

---

# 159. Retry TLS Failure

Repeated retry of an invalid certificate normally provides no value.

---

# 160. Observability

Internal communication MUST be observable.

---

# 161. Client Metrics

Important dependencies SHOULD expose:

```text
Request Count

Latency

Errors

Timeouts

Connection Failures

Pool Saturation
```

---

# 162. DNS Metrics

DNS failures/latency SHOULD be observable at platform or application level.

---

# 163. Connection Pool Metrics

Monitor applicable:

```text
Active Connections

Idle Connections

Pending Acquires

Acquire Timeouts

Connection Creation
```

---

# 164. Network Metrics

Platform telemetry SHOULD expose applicable:

```text
Connection Count

Reset Rate

Packet Loss

Cross-Zone Traffic

NAT Saturation
```

---

# 165. Raw Endpoint Metric

Pod IPs SHOULD NOT become high-cardinality application metrics by default.

---

# 166. Dependency Label

Use bounded logical dependency labels such as:

```text
customers-service
```

---

# 167. Distributed Tracing

Tracing SHOULD record logical service boundaries rather than relying only on infrastructure addresses.

---

# 168. Network Logs

Network/proxy logs MUST follow privacy and credential-protection standards.

---

# 169. Authorization Header

Internal proxies MUST NOT log bearer credentials.

---

# 170. Alerting

Critical internal-network alerts SHOULD include:

```text
DNS Error Surge

Connection Failure Surge

Pool Saturation

TLS Failure

Cross-Zone Anomaly

Service Endpoint Depletion
```

---

# 171. Zero Ready Endpoint

A service with zero ready endpoints MUST be operationally visible.

---

# 172. Endpoint Flapping

Repeated endpoint addition/removal MAY indicate readiness or deployment instability and SHOULD be observable.

---

# 173. Readiness Failure Storm

A shared dependency outage MUST NOT necessarily remove every consuming pod from readiness if the service can still provide meaningful behavior.

---

# 174. Dependency Readiness

Readiness dependency checks MUST follow ADR-075 lifecycle rules and should not create cascading outages.

---

# 175. Testing Strategy

Internal networking behavior SHOULD have focused validation.

---

# 176. DNS Test

Application integration tests SHOULD use logical service endpoints rather than production IP assumptions.

---

# 177. Client Timeout Test

HTTP clients MUST have tests for:

```text
Connection Timeout

Response Timeout
```

where custom behavior exists.

---

# 178. Connection Failure Test

Tests SHOULD verify connection-refused/unavailable classification.

---

# 179. Retry Test

Network retry tests MUST verify bounded attempts and idempotency constraints.

---

# 180. Pool Saturation Test

Critical client infrastructure SHOULD test bounded pool behavior where practical.

---

# 181. Graceful Shutdown Test

Applications SHOULD validate that shutdown:

```text
Stops new traffic

Allows bounded in-flight completion
```

where integration testing permits.

---

# 182. NetworkPolicy Test

Critical network policies SHOULD be validated in representative environments.

---

# 183. mTLS Test

Where mTLS is adopted, tests SHOULD verify unauthorized workload identities cannot establish approved service communication.

---

# 184. Certificate Rotation Test

Certificate rotation SHOULD not require service downtime where automatic rotation is part of the platform contract.

---

# 185. Load Distribution Test

Performance tests SHOULD detect severe imbalance where load-balancing behavior is critical.

---

# 186. Zone Failure Test

Critical workloads SHOULD test or exercise planned zone-failure scenarios according to resilience strategy.

---

# 187. Service Mesh Test

Mesh policies MUST be tested together with application retry/timeout behavior to avoid conflicting controls.

---

# 188. Architecture Review Checklist

Material networking changes SHOULD evaluate:

```text
[ ] Is Kubernetes Service sufficient?

[ ] Are pod IPs referenced directly?

[ ] Is DNS caching appropriate?

[ ] Is headless service actually needed?

[ ] Is client-side load balancing justified?

[ ] Are connection pools bounded?

[ ] Are pool-acquire timeouts configured?

[ ] Are connect/response timeouts configured?

[ ] Are idle/lifetime settings aligned with infrastructure?

[ ] Could stale connections occur?

[ ] Is HTTP/2 useful?

[ ] Are retries safe?

[ ] Could retries reuse broken connections?

[ ] Is graceful shutdown configured?

[ ] Is connection draining sufficient?

[ ] Are pods spread across zones?

[ ] Is cross-zone traffic acceptable?

[ ] Is cross-region synchronous traffic necessary?

[ ] Are NetworkPolicies least-privilege?

[ ] Is unrestricted egress necessary?

[ ] Is service mesh justified?

[ ] Who owns retries: app or mesh?

[ ] Is mTLS required?

[ ] Are certificates automatically rotated?

[ ] Are private services accidentally public?

[ ] Are network metrics and alerts available?
```

---

# 189. Network Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] No hardcoded pod IPs

[ ] Internal services use approved DNS naming

[ ] HTTP clients define bounded connect timeouts

[ ] HTTP clients define bounded response timeouts

[ ] Connection pools have maximum size

[ ] Connection acquisition timeout configured

[ ] Critical deployments span zones

[ ] Private services use internal exposure

[ ] Production namespaces have required NetworkPolicies

[ ] Static certificates not embedded in images

[ ] Retry policy not duplicated between mesh and app without justification
```

---

# 190. Enterprise Network Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] Application depends on fixed pod IP

[ ] DNS is cached indefinitely in a dynamic topology without justification

[ ] Connection pool is unbounded

[ ] Connection acquisition can wait forever

[ ] Connect/response timeout is absent

[ ] Every request establishes a new TCP/TLS connection

[ ] Remote calls are retried blindly after ambiguous mutation timeout

[ ] Database locks remain held across remote network calls

[ ] Pod terminates without draining traffic

[ ] Critical service replicas exist in one zone only

[ ] Internal sensitive service is publicly exposed

[ ] NetworkPolicy allows unrestricted access without justification

[ ] Static long-lived mTLS certificate exists in image

[ ] Service mesh and application both retry without shared budget

[ ] Cross-region synchronous dependency exists without latency/failure analysis
```

---

# 191. Anti-Patterns

The following are prohibited or strongly discouraged:

- hardcoded pod IPs
- static instance lists
- infinite DNS caching
- unnecessary headless services
- client-side load balancing without real need
- double load balancing without distinct purpose
- new TCP connection per request
- unbounded HTTP connection pools
- unlimited pending acquisition queues
- no connection acquisition timeout
- idle connections kept forever
- ignoring infrastructure keep-alive timeout mismatch
- blind network retry on mutations
- retrying TLS certificate validation failures
- holding database locks while making network calls
- terminating pods before endpoint draining
- all critical replicas in one availability zone
- unrestricted flat production network
- public exposure for internal-only services
- service mesh adopted solely because microservices exist
- duplicated mesh/application retries
- static certificates bundled with containers
- separate service registry introduced without need in Kubernetes
- synchronous cross-region call chains without explicit architecture

---

# 192. Positive Consequences

The decision provides:

- stable service discovery
- reduced infrastructure coupling
- reliable pod replacement
- efficient connection reuse
- bounded network resource usage
- safer rolling deployments
- improved multi-zone resilience
- stronger internal network security
- controlled service mesh adoption
- better diagnostics
- reduced stale-connection failures
- clearer timeout/retry ownership

---

# 193. Negative Consequences

The decision introduces:

- network-policy management
- connection-pool tuning
- DNS/runtime configuration awareness
- topology planning
- graceful shutdown requirements
- possible mesh complexity
- network observability requirements

These costs are accepted because service communication is a critical runtime dependency in distributed systems.

---

# 194. Neutral Consequences

The decision also means:

- Kubernetes Service is sufficient for many workloads
- not every service needs client-side balancing
- not every service needs a service mesh
- not every internal API requires HTTP/2
- not every dependency should participate in readiness
- cross-zone traffic may be acceptable when availability requires it
- connection reuse is beneficial but requires stale-connection handling
- mTLS does not replace business authorization

---

# 195. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| DNS failure | High | Low/Medium | Kubernetes DNS resilience + telemetry |
| Stale connection | Medium/High | Medium | Idle/lifetime controls |
| Pool exhaustion | Critical | Medium | Bounded pools |
| Retry amplification | Critical | Medium | Retry ownership |
| Zone outage | Critical | Low/Medium | Multi-AZ distribution |
| Network lateral movement | Critical | Medium | NetworkPolicy |
| Mesh misconfiguration | High | Medium | Clear ownership + tests |
| Certificate expiry | Critical | Low | Automated rotation |
| Cross-region latency | High | Medium | Region-local design |
| Public exposure mistake | Critical | Low/Medium | Explicit private routing |

---

# 196. Implementation Guidance

The following rules are mandatory:

1. Kubernetes-native discovery should be used for in-cluster services.
2. Applications must use stable service identities rather than pod IPs.
3. DNS caching must remain compatible with dynamic infrastructure.
4. `ClusterIP` should be the default internal service type.
5. Headless services require explicit use-case justification.
6. Client-side load balancing should only be added when it provides distinct value.
7. HTTP connections must be pooled and reused.
8. Connection pools must be bounded.
9. Pool acquisition must have bounded timeout.
10. Connect and response timeouts must be explicit.
11. Idle and maximum connection lifetimes should align with infrastructure behavior.
12. Clients must recover from stale connections.
13. Keep-alive should be used for ordinary service communication.
14. HTTP/2 should be adopted only where its multiplexing/protocol benefits justify it.
15. Network retries must respect operation idempotency.
16. Circuit breakers should normally operate on logical dependency identity.
17. Pods must stop receiving traffic before termination.
18. Graceful shutdown and connection draining must be configured.
19. Critical workloads should span multiple availability zones.
20. Cross-region synchronous communication should be minimized.
21. Production network access should follow least privilege.
22. NetworkPolicies should restrict ingress/egress where platform architecture supports it.
23. Private services must not be exposed publicly unintentionally.
24. Service mesh adoption requires explicit cost/benefit analysis.
25. Retry/timeout ownership between mesh and application must be explicit.
26. mTLS certificates must rotate automatically and must not be embedded statically.
27. Virtual Threads must not create unlimited network concurrency.
28. Internal communication must expose useful network/client telemetry.
29. Critical network failure modes must have automated or environment-level validation.

---

# 197. Validation

This ADR will be validated through:

- Kubernetes Services
- CoreDNS
- Kubernetes EndpointSlices
- Kubernetes NetworkPolicy
- AWS VPC
- AWS internal load balancers
- private endpoints where appropriate
- Java 21
- Spring Boot
- Reactor Netty / approved HTTP clients
- HikariCP where relevant to downstream DB access
- service mesh where adopted
- mTLS
- OpenTelemetry/Micrometer
- Testcontainers
- integration tests
- load tests
- failure-injection tests
- multi-zone resilience exercises

---

# 198. Success Criteria

The decision is successful when:

- services no longer depend on fixed instance addresses
- pod replacement does not require application reconfiguration
- connection reuse reduces connection overhead
- pool saturation is observable and bounded
- stale connections recover predictably
- rolling deployments drain traffic safely
- critical services tolerate a single-zone failure
- private services remain private
- internal connectivity follows least privilege
- cross-region synchronous dependencies remain exceptional
- service mesh, when used, has explicit policy ownership
- network incidents can be classified and diagnosed quickly

---

# 199. Alternatives Rejected

## 199.1 Hardcoded Service Instance Addresses

Rejected because instances are ephemeral.

---

## 199.2 External Service Registry by Default

Rejected because Kubernetes already provides discovery for in-cluster workloads.

---

## 199.3 Client-Side Load Balancing Everywhere

Rejected because it introduces unnecessary application complexity when Kubernetes Service routing is sufficient.

---

## 199.4 New Connection per Request

Rejected because TCP/TLS setup cost reduces performance and increases resource consumption.

---

## 199.5 Unlimited Connection Pool

Rejected because downstream systems have finite capacity.

---

## 199.6 Service Mesh by Default

Rejected because mesh operational complexity must be justified by real requirements.

---

## 199.7 Flat Unrestricted Cluster Network

Rejected because it increases lateral movement and blast radius.

---

# 200. Related Decisions

This ADR extends and implements:

- ADR-014: Distributed Observability
- ADR-016: Application Resilience
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-073: Enterprise API Gateway, BFF, Edge Security, Traffic Management and Rate Limiting Standard

---

# 201. References

- Kubernetes Service Documentation
- Kubernetes DNS Documentation
- Kubernetes EndpointSlice Documentation
- Kubernetes NetworkPolicy Documentation
- Kubernetes Topology Aware Routing
- Java Networking Documentation
- Reactor Netty Documentation
- Spring Framework HTTP Client Documentation
- HTTP/1.1 Specification
- HTTP/2 Specification
- AWS VPC Documentation
- AWS Elastic Load Balancing Documentation
- Istio Documentation
- Envoy Documentation
- Linkerd Documentation
- NIST Zero Trust Architecture
- Google Site Reliability Engineering

---

# 202. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise service discovery and internal network communication baseline |

---

# 203. Decision Summary

Service discovery becomes:

```text
SERVICE A
   |
   v
customers-service
   |
   v
KUBERNETES DNS
   |
   v
KUBERNETES SERVICE
   |
   +--> POD A
   +--> POD B
   +--> POD C
```

instead of:

```text
SERVICE A
   |
   v
10.0.17.42
```

Connection management becomes:

```text
REQUEST
   |
   v
CONNECTION POOL
   |
   +--> EXISTING HEALTHY CONNECTION
   |
   +--> CREATE WITHIN LIMIT
   |
   +--> WAIT WITH TIMEOUT
   |
   X
NO UNBOUNDED GROWTH
```

Timeout budgeting becomes:

```text
REQUEST DEADLINE
      |
      +--> DNS
      +--> POOL ACQUIRE
      +--> CONNECT
      +--> TLS
      +--> REMOTE CALL
      +--> RETRY
```

with every stage bounded.

Pod termination becomes:

```text
SIGTERM
   |
   v
NOT READY
   |
   v
REMOVE FROM ENDPOINTS
   |
   v
STOP NEW TRAFFIC
   |
   v
DRAIN IN-FLIGHT
   |
   v
EXIT
```

Zone resilience becomes:

```text
SERVICE
  |
  +--> POD A / AZ-A
  |
  +--> POD B / AZ-B
  |
  +--> POD C / AZ-C
```

rather than:

```text
SERVICE
  |
  +--> POD A / AZ-A
  +--> POD B / AZ-A
  +--> POD C / AZ-A
```

Network security becomes:

```text
DEFAULT DENY
     |
     v
EXPLICIT COMMUNICATION
     |
     +--> orders -> customers
     +--> orders -> postgres
     +--> orders -> kafka
```

Service mesh adoption becomes:

```text
NEED mTLS /
ADVANCED TRAFFIC POLICY /
NETWORK TELEMETRY?
       |
    +--+--+
    |     |
   NO    YES
    |     |
    v     v
KEEP    EVALUATE
SIMPLE  MESH COST
```

mTLS means:

```text
SERVICE A
    |
    v
MUTUAL TLS
    |
    v
SERVICE B
```

but still requires:

```text
AUTHENTICATION
      !=
BUSINESS AUTHORIZATION
```

The complete internal networking equation is:

```text
STABLE SERVICE DISCOVERY
        +
KUBERNETES DNS
        +
READY ENDPOINTS
        +
BOUNDED CONNECTION POOLS
        +
CONNECTION REUSE
        +
BOUNDED TIMEOUTS
        +
SAFE RETRIES
        +
GRACEFUL CONNECTION DRAINING
        +
MULTI-ZONE DISTRIBUTION
        +
NETWORK LEAST PRIVILEGE
        +
CONTROLLED mTLS
        +
TOPOLOGY AWARENESS
        +
NETWORK OBSERVABILITY
        =
RELIABLE INTERNAL SERVICE COMMUNICATION
```

The governing principle is:

```text
Connect to services,
not pods.

Treat pod instances
as disposable.

Use Kubernetes discovery
before adding another registry.

Reuse network connections.

Bound connection pools.

Bound acquisition waits.

Bound connection timeouts.

Bound response timeouts.

Expect stale connections.

Align keep-alive behavior
with infrastructure.

Retry through the logical
service boundary,
not a dead endpoint.

Do not blindly retry
ambiguous mutations.

Drain traffic before
terminating pods.

Spread critical replicas
across failure domains.

Prefer region-local traffic.

Restrict network access
to what workloads require.

Do not introduce a service mesh
without a concrete reason.

If a mesh is used,
decide clearly whether
the application or mesh
owns retries and timeouts.

Rotate mTLS identities
automatically.

Do not embed static
certificates in images.

Use logical dependency names
for observability.

And remember:

in cloud-native systems,
the address of a service
should be stable,

even though every machine
behind that address
is temporary.
```
