# ADR-073: Adopt Enterprise API Gateway, BFF, Edge Security, Traffic Management and Rate Limiting Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-073 |
| Title | Adopt Enterprise API Gateway, BFF, Edge Security, Traffic Management and Rate Limiting Standard |
| Status | Superseded |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | API Gateway, BFF, Edge Security, Traffic Management, Rate Limiting |
| Related Work Items | OAuth2/OIDC, Spring Security, Kubernetes, AWS, API Management |
| Supersedes | ADR-033 |
| Superseded By | ADR-082 |

---

# 1. Context

Enterprise APIs are commonly exposed through an edge layer.

A simplified architecture is:

```text
CLIENT
   |
   v
EDGE / API GATEWAY
   |
   +--> AUTHENTICATION
   +--> RATE LIMITING
   +--> ROUTING
   +--> TLS
   +--> OBSERVABILITY
   |
   v
BACKEND SERVICES
```

The edge may serve:

```text
Web Applications

Mobile Applications

Partner Integrations

Internal Applications

Machine-to-Machine Consumers
```

Without explicit standards, API gateways can become:

```text
BUSINESS LOGIC ENGINES

CENTRALIZED MONOLITHS

TOKEN RELAY MACHINES

UNCONTROLLED PROXIES

SINGLE POINTS OF FAILURE

SECURITY BYPASS POINTS
```

The edge must remain a clearly bounded architectural layer.

---

# 2. Problem Statement

The organization requires standards covering:

- API Gateway
- reverse proxy
- ingress
- edge security
- TLS termination
- authentication
- authorization
- token validation
- token propagation
- BFF
- routing
- request transformation
- response transformation
- rate limiting
- throttling
- quotas
- burst limits
- admission control
- API keys
- partner APIs
- CORS
- WAF
- IP filtering
- bot protection
- load shedding
- timeouts
- retries
- circuit breakers
- canary routing
- blue/green routing
- observability
- API version routing
- request-size limits
- header governance
- correlation
- caching
- edge failures
- high availability

---

# 3. Decision Drivers

Primary drivers are:

1. security
2. traffic control
3. consistent API exposure
4. backend protection
5. scalability
6. resilience
7. observability
8. client-specific adaptation
9. operational simplicity
10. controlled external contracts
11. availability
12. low coupling

---

# 4. Decision

External and controlled internal API exposure SHOULD use an approved edge/API gateway architecture where centralized traffic/security capabilities provide clear value.

Canonical flow:

```text
CLIENT
   |
   v
EDGE
   |
   +--> TLS
   +--> AUTHN
   +--> RATE LIMIT
   +--> REQUEST LIMITS
   +--> ROUTING
   +--> CORRELATION
   |
   v
SERVICE
   |
   v
RESOURCE AUTHORIZATION
```

The gateway does not replace backend authorization.

---

# 5. Fundamental Principle

```text
The edge protects
and routes traffic.

The service owns
business authorization
and business behavior.
```

---

# 6. API Gateway Responsibility

The API Gateway SHOULD own cross-cutting edge concerns.

Examples:

```text
TLS Termination

Routing

Global Authentication Enforcement

Rate Limiting

Request Size Limits

IP Policies

WAF Integration

Correlation Headers

Protocol-Level Observability
```

---

# 7. Gateway Business Logic

The gateway MUST NOT become the primary location for business rules.

---

# 8. Business Validation

Rules such as:

```text
Order can be approved

Customer belongs to segment

Order belongs to company

Credit limit permits checkout
```

belong in the owning service/domain.

---

# 9. Edge Is Not Domain Layer

The gateway MUST NOT duplicate domain state machines.

---

# 10. Gateway as Policy Enforcement Point

The gateway MAY enforce coarse-grained access policies.

Backend services MUST still enforce resource-level authorization.

---

# 11. Authentication at Edge

The gateway MAY reject missing/invalid credentials before traffic reaches backend services.

---

# 12. Backend Validation

Backend APIs SHOULD still validate trusted authentication context according to the adopted security architecture.

---

# 13. Trust Boundary

Services MUST NOT blindly trust arbitrary headers because they originated "inside the network."

---

# 14. Identity Header

Headers such as:

```text
X-User-Id

X-Role

X-Tenant
```

MUST NOT become authoritative merely because a client can send them.

---

# 15. Header Sanitization

The gateway SHOULD remove or overwrite security-sensitive headers supplied by untrusted external clients.

---

# 16. Identity Propagation

Trusted identity propagation MUST use approved security mechanisms.

---

# 17. Bearer Token Propagation

User bearer tokens MAY be forwarded where downstream delegation requires them and token audience semantics permit it.

---

# 18. Blind Relay

The gateway MUST NOT indiscriminately forward credentials to unrelated services.

---

# 19. Token Exchange

Token exchange or equivalent delegation SHOULD be considered when downstream audiences differ.

---

# 20. TLS

External API traffic MUST use approved TLS.

---

# 21. TLS Termination

TLS MAY terminate at the edge/load balancer according to platform architecture.

---

# 22. Internal TLS

Sensitive internal traffic SHOULD remain encrypted according to zero-trust and infrastructure requirements.

---

# 23. TLS Version

Weak/deprecated TLS versions and cipher suites MUST be disabled according to enterprise security policy.

---

# 24. Certificate Management

Certificates MUST use controlled issuance, storage and rotation.

---

# 25. Manual Certificate

Production certificate lifecycle SHOULD NOT depend on manual last-minute replacement.

---

# 26. Routing

Routing rules MUST remain deterministic and version controlled.

---

# 27. Host Routing

Host-based routing MAY be used.

Example:

```text
api.example.com
```

---

# 28. Path Routing

Path-based routing MAY be used.

Example:

```text
/orders/**
    ->
orders-service
```

---

# 29. Routing Ownership

Route configuration MUST have clear ownership.

---

# 30. Route Drift

Manual route changes SHOULD be reconciled into declarative configuration.

---

# 31. Route Collision

Ambiguous/overlapping route rules MUST be avoided.

---

# 32. Default Route

A broad fallback route SHOULD NOT accidentally expose services intended to remain private.

---

# 33. Private Service

Services not intended for direct external access SHOULD remain unreachable from the public edge.

---

# 34. Ingress

Kubernetes Ingress or Gateway API MAY provide cluster-level routing beneath or as part of the API gateway architecture.

---

# 35. Ingress vs API Gateway

Ingress and API Gateway solve overlapping but not always identical concerns.

Conceptually:

```text
API GATEWAY
    |
    v
EDGE/API POLICY

INGRESS
    |
    v
CLUSTER ROUTING
```

---

# 36. Duplication

Capabilities MUST NOT be duplicated across:

```text
Cloud Load Balancer

WAF

API Gateway

Ingress

Service
```

without clear ownership.

---

# 37. BFF

Backend for Frontend MAY be used when a specific client type requires a tailored backend interaction model.

---

# 38. BFF Candidate

A BFF is appropriate when:

```text
Web and mobile require
materially different aggregation

Client-specific payloads differ

Client-specific workflows differ

Latency optimization requires
server-side aggregation
```

---

# 39. BFF Is Not Mandatory

Every frontend MUST NOT automatically get its own BFF.

---

# 40. BFF Business Ownership

A BFF SHOULD own presentation/client orchestration, not core domain logic.

---

# 41. BFF Aggregation

A BFF MAY aggregate calls to multiple backend services.

---

# 42. BFF Fan-Out

BFF fan-out MUST follow performance/resilience standards.

---

# 43. BFF N+1

A BFF MUST NOT replace one client-side N+1 problem with server-side unbounded fan-out.

---

# 44. BFF Data Ownership

A BFF SHOULD NOT become the source of truth for core business state.

---

# 45. BFF Database

A BFF SHOULD NOT normally own a domain database.

A small client-specific state store MAY be justified for presentation concerns.

---

# 46. BFF Security

BFF authorization MUST not weaken backend authorization.

---

# 47. API Composition

Gateway-level composition SHOULD be limited.

Complex composition belongs in a BFF/application service with explicit ownership.

---

# 48. Request Transformation

The edge MAY perform limited technical request transformations.

Examples:

```text
Header Normalization

Protocol Adaptation

Version Routing
```

---

# 49. Business Transformation

The edge SHOULD NOT transform substantial domain payload semantics.

---

# 50. Response Transformation

Technical response normalization MAY be allowed.

Business response shaping SHOULD normally occur in service/BFF contracts.

---

# 51. Contract Ownership

The component exposing the contract MUST own and test the transformation semantics.

---

# 52. Rate Limiting

Externally exposed APIs SHOULD use rate limiting where abuse or capacity risk exists.

---

# 53. Rate Limit Purpose

Rate limiting protects:

```text
Availability

Fairness

Downstream Capacity

Abuse Resistance

Cost
```

---

# 54. Rate Limit Dimension

Limits MAY be applied by:

```text
Client

API Key

OAuth Client

User

Tenant

Source IP

Endpoint

Operation
```

according to contract/security requirements.

---

# 55. IP-Only Limit

IP-only rate limiting SHOULD NOT be the only identity mechanism for authenticated distributed consumers.

---

# 56. NAT

Many legitimate users may share one source IP behind NAT.

This MUST be considered before using IP-based limits.

---

# 57. Global Limit

Global service limits MAY protect overall backend capacity.

---

# 58. Consumer Limit

Per-consumer limits SHOULD prevent one caller from exhausting shared capacity.

---

# 59. Rate vs Burst

Rate and burst are distinct.

Example:

```text
100 requests / second

burst = 200
```

---

# 60. Token Bucket

Token-bucket or equivalent algorithms SHOULD be used where burst tolerance is required.

---

# 61. Leaky Bucket

Leaky-bucket semantics MAY be appropriate where smooth output is preferred.

---

# 62. Fixed Window

Fixed-window algorithms are simpler but can create boundary bursts.

---

# 63. Sliding Window

Sliding-window algorithms MAY provide fairer enforcement at additional implementation cost.

---

# 64. Distributed Rate Limiter

A distributed gateway deployment requires rate-limit state semantics that remain correct across gateway replicas.

---

# 65. Local Rate Limit

Pure per-instance counters MUST NOT be assumed to represent a global rate limit.

---

# 66. Redis Rate Limit

Redis MAY support distributed rate limiting when latency and availability characteristics are acceptable.

---

# 67. Rate Limiter Dependency

A centralized rate-limit store MUST NOT unintentionally become a critical single point of failure without defined behavior.

---

# 68. Rate Limiter Failure Policy

Failure semantics MUST be explicit.

Possible strategies:

```text
FAIL OPEN

FAIL CLOSED

LOCAL FALLBACK
```

---

# 69. Security-Sensitive Limit

Abuse/security controls MAY require fail-closed semantics.

---

# 70. Availability-Sensitive Limit

Selected availability-protection controls MAY use controlled fallback when the central limiter is unavailable.

---

# 71. 429

Rate-limit rejection SHOULD return:

```text
429 Too Many Requests
```

---

# 72. Retry-After

`Retry-After` SHOULD be returned where meaningful.

---

# 73. Rate Limit Headers

Standard or platform-defined rate-limit response headers MAY expose:

```text
Limit

Remaining

Reset
```

when useful to clients.

---

# 74. Information Exposure

Rate-limit metadata MUST not expose sensitive platform internals.

---

# 75. Quota

Quota controls usage over longer periods.

Examples:

```text
10,000 requests/day

1,000,000 requests/month
```

---

# 76. Rate Limit vs Quota

```text
RATE LIMIT
   ->
short-window traffic control

QUOTA
   ->
longer-term usage entitlement
```

---

# 77. Quota Ownership

Commercial/business quotas require explicit product/business ownership.

---

# 78. Backend Limits

Gateway rate limiting does not replace backend concurrency limits.

---

# 79. Defense in Depth

Conceptually:

```text
EDGE RATE LIMIT
       +
SERVICE BULKHEAD
       +
DB POOL LIMIT
       +
DOWNSTREAM LIMIT
```

---

# 80. Admission Control

Systems MAY reject work before expensive processing when capacity is exhausted.

---

# 81. Load Shedding

Load shedding SHOULD preserve the most important operations during overload when possible.

---

# 82. Priority

Priority classes MAY differentiate:

```text
Critical writes

Interactive reads

Batch/export traffic
```

when business requirements justify it.

---

# 83. Priority Starvation

Lower priority workloads MUST not be permanently starved unintentionally.

---

# 84. Request Size

The edge MUST enforce bounded request sizes.

---

# 85. Unlimited Body

Unlimited HTTP request bodies are prohibited.

---

# 86. Payload-Specific Limit

Upload endpoints MAY have larger approved limits than ordinary JSON endpoints.

---

# 87. Oversized Request

Oversized requests SHOULD be rejected before reaching expensive business processing where practical.

---

# 88. 413

Oversized payloads SHOULD use:

```text
413 Content Too Large
```

where HTTP semantics apply.

---

# 89. Header Size

Header size MUST also remain bounded.

---

# 90. Header Abuse

Oversized headers can consume proxy/server resources and MUST be constrained.

---

# 91. URL Length

URL and query-string limits SHOULD be defined.

---

# 92. Sensitive Query Parameter

Sensitive information SHOULD not be placed in query parameters where avoidable.

---

# 93. Method Restrictions

The gateway MAY restrict unsupported HTTP methods.

---

# 94. TRACE Method

HTTP TRACE SHOULD normally be disabled unless explicitly required.

---

# 95. CORS

CORS MAY be centralized at the edge where architecture supports consistent policy.

---

# 96. CORS Ownership

CORS MUST have one clearly authoritative configuration layer.

---

# 97. Duplicate CORS

Conflicting gateway/service CORS policies SHOULD be avoided.

---

# 98. Allowed Origins

Production allowed origins MUST be explicitly controlled.

---

# 99. Wildcard CORS

Wildcard origins SHOULD be avoided when authenticated browser interactions are involved.

---

# 100. WAF

A Web Application Firewall SHOULD protect applicable Internet-facing APIs.

---

# 101. WAF Role

WAF provides defense in depth against classes of malicious traffic.

It does not replace application security.

---

# 102. Managed Rules

Managed WAF rules MAY provide baseline protections.

---

# 103. Custom Rules

Custom rules SHOULD target demonstrated threat patterns rather than arbitrary blocking.

---

# 104. False Positive

WAF changes MUST consider false-positive impact on legitimate traffic.

---

# 105. WAF Observation Mode

New high-impact rules MAY begin in monitoring/count mode before blocking.

---

# 106. IP Allowlist

IP allowlisting MAY restrict selected partner/admin APIs.

---

# 107. IP Allowlist Is Not Authentication

Network origin alone SHOULD NOT be treated as sufficient identity for high-risk operations.

---

# 108. Deny List

IP deny lists MAY complement abuse controls but are inherently reactive.

---

# 109. Bot Protection

Public APIs susceptible to automated abuse MAY require bot/automation controls.

---

# 110. API Key

API keys MAY identify selected partner/client applications.

---

# 111. API Key Is Secret

API keys MUST follow secret-management rules.

---

# 112. API Key Authorization

An API key SHOULD NOT automatically grant unrestricted API access.

---

# 113. API Key Rotation

API keys MUST support controlled rotation.

---

# 114. API Key Logging

API key values MUST NOT be logged.

---

# 115. Partner API

Partner-facing APIs SHOULD have explicitly defined:

```text
Authentication

Quota

Rate Limit

Contract Version

Support Policy

Deprecation Policy
```

---

# 116. Timeout

The gateway MUST enforce bounded upstream timeouts.

---

# 117. Infinite Gateway Timeout

Infinite or excessively large proxy timeouts are prohibited.

---

# 118. Timeout Hierarchy

Timeouts SHOULD satisfy:

```text
CLIENT DEADLINE
      >
GATEWAY TIMEOUT
      >
SERVICE TIMEOUT
      >
DOWNSTREAM TIMEOUT
```

with reasonable margins.

---

# 119. Timeout Budget

The edge MUST NOT consume the entire client deadline before backend processing can finish.

---

# 120. Timeout Response

Gateway timeout behavior SHOULD use correct HTTP semantics, often:

```text
504 Gateway Timeout
```

when appropriate.

---

# 121. Retry at Gateway

Gateway-level retries MUST be used cautiously.

---

# 122. Mutation Retry

The gateway MUST NOT blindly retry non-idempotent mutation requests.

---

# 123. GET Retry

Selected idempotent reads MAY be retried for transient connection failures within strict budgets.

---

# 124. Retry Multiplication

Gateway retries combined with service/client retries can amplify traffic dramatically.

---

# 125. Retry Ownership

One layer SHOULD normally own a particular retry decision.

---

# 126. Retry Budget

Total retry amplification MUST be bounded.

---

# 127. Circuit Breaker

The edge MAY use circuit breaking for upstream protection, but service-specific dependency circuit breakers still belong in services.

---

# 128. Duplicate Circuit Breakers

Multiple circuit breakers across edge and service layers MUST have distinct purpose.

---

# 129. Bulkhead

Gateway worker/thread/connection pools MUST remain bounded.

---

# 130. Connection Pool

Backend connection pools from gateway to services MUST be sized according to capacity.

---

# 131. Queueing

Excessive request queueing at the gateway SHOULD be avoided.

---

# 132. Queueing Latency

Queueing can hide overload until client latency becomes unacceptable.

---

# 133. Backpressure

The edge SHOULD reject or shed work instead of allowing unbounded internal queues.

---

# 134. Canary Routing

The edge MAY route a controlled percentage of traffic to a new version.

---

# 135. Canary Dimensions

Canary targeting MAY use:

```text
Percentage

Client

Tenant

Header

Region
```

when safe and controlled.

---

# 136. Canary Security

Untrusted clients MUST NOT be allowed to arbitrarily select privileged deployment versions unless explicitly supported.

---

# 137. Canary Observability

Canary rollout MUST compare:

```text
Error Rate

Latency

Resource Usage

Business Failure
```

against baseline.

---

# 138. Canary Rollback

Canary traffic MUST be quickly reversible.

---

# 139. Blue/Green

Blue/green routing MAY support low-risk version switching.

---

# 140. Session Affinity

Session affinity SHOULD be avoided for stateless APIs unless the application genuinely requires it.

---

# 141. Stateless Backend

Stateless API services should allow requests to be routed to any healthy replica.

---

# 142. Sticky Session

Sticky sessions increase operational coupling and SHOULD be justified.

---

# 143. API Version Routing

The gateway MAY route API versions when versioning strategy requires it.

---

# 144. Version Logic

The gateway MUST NOT silently transform incompatible API versions with complex business translation.

---

# 145. Deprecated Version

Deprecated versions SHOULD have measurable usage before removal.

---

# 146. Version Metrics

Gateway telemetry SHOULD identify API version usage using bounded dimensions.

---

# 147. Sunset

Sunset/deprecation headers MAY communicate retirement timelines.

---

# 148. Route Removal

Old route removal MUST occur only after consumer migration policy is satisfied.

---

# 149. Caching at Edge

Edge caching MAY be used for safe read responses.

---

# 150. Cache Candidate

Good candidates include:

```text
Public Reference Data

Rarely Changing Metadata

Static Content
```

---

# 151. Sensitive Cache

Authenticated personalized responses SHOULD NOT be cached at shared edge scope without explicit safe cache-key semantics.

---

# 152. Cache Key

Cache keys MUST account for all dimensions affecting response semantics.

---

# 153. Authorization Cache Leakage

A response for User A MUST NOT be served to User B because authorization dimensions were omitted from the edge cache key.

---

# 154. Mutation Cache

Mutation responses SHOULD NOT generally be cached.

---

# 155. Cache-Control

Backend cache directives SHOULD be respected or explicitly overridden according to contract.

---

# 156. Correlation

The edge SHOULD establish or preserve correlation identifiers.

---

# 157. Existing Correlation ID

If an external correlation ID is accepted, it MUST be validated for safe format/length.

---

# 158. Untrusted Correlation ID

External correlation IDs MUST NOT be allowed to inject arbitrary log content.

---

# 159. Trace Context

W3C Trace Context SHOULD be propagated where tracing is enabled.

---

# 160. Request ID

The edge MAY generate a unique request ID for every inbound request.

---

# 161. Observability

The edge MUST expose operational telemetry.

---

# 162. RED Metrics

Monitor:

```text
RATE

ERRORS

DURATION
```

---

# 163. Edge Metrics

Applicable edge metrics include:

```text
Request Count

Status Distribution

Latency

Active Connections

Rejected Requests

Rate-Limit Hits

WAF Blocks

Upstream Timeouts

Upstream Connection Failures
```

---

# 164. Route Dimension

Metrics MAY use bounded route/template identifiers.

---

# 165. Raw URL Metric

Raw URLs containing IDs MUST NOT be metric labels.

---

# 166. Client Dimension

Client identity MAY be a bounded metric dimension for controlled partner/client populations.

---

# 167. User ID Metric

User IDs MUST NOT be metric dimensions.

---

# 168. Rate Limit Metrics

Rate-limiter telemetry SHOULD expose:

```text
Allowed

Rejected

Limiter Failure

Fallback Usage
```

---

# 169. WAF Metrics

Monitor:

```text
Allowed

Blocked

Rule Match

False Positive Investigations
```

---

# 170. Logs

Gateway access logs SHOULD be structured.

---

# 171. Access Log Data

Access logs SHOULD contain safe fields such as:

```text
Timestamp

Method

Route

Status

Latency

Request ID

Client Identity Category
```

---

# 172. Access Log PII

Access logs MUST minimize PII.

---

# 173. Authorization Logging

Bearer tokens and API keys MUST never be recorded in access logs.

---

# 174. Query Logging

Sensitive query parameters SHOULD be masked or excluded.

---

# 175. Header Logging

Header allowlists SHOULD be preferred over dumping every request header.

---

# 176. Alerting

Critical edge alerts SHOULD include:

```text
High 5xx Rate

High 429 Rate

Gateway Saturation

Upstream Timeout Surge

WAF Block Surge

Certificate Expiration Risk
```

---

# 177. 429 Alert

High 429 rate MAY indicate:

```text
Abuse

Misconfigured Consumer

Insufficient Capacity

Incorrect Limit
```

and requires contextual analysis.

---

# 178. High Availability

The API Gateway MUST be deployed without a single-instance dependency in production.

---

# 179. Replica

Multiple gateway replicas SHOULD be used according to traffic and availability requirements.

---

# 180. Zone Distribution

Replicas SHOULD span failure domains where supported.

---

# 181. Gateway State

Gateway application instances SHOULD remain stateless where practical.

---

# 182. Rate-Limit State

Distributed state such as global rate-limit counters MUST use an architecture compatible with multiple replicas.

---

# 183. Configuration Availability

Gateway routing/security configuration MUST be highly available.

---

# 184. Configuration Failure

Bad configuration MUST fail safely.

---

# 185. Config Validation

Route/policy configuration SHOULD be validated before promotion.

---

# 186. Rollback

Gateway configuration releases MUST be rollback-capable.

---

# 187. Deployment

Gateway deployment SHOULD use controlled rolling/canary strategies.

---

# 188. Gateway Outage

Edge outage is a platform-level availability event and MUST have defined operational ownership.

---

# 189. Dependency Failure

A backend failure MUST NOT crash the gateway process.

---

# 190. Graceful Degradation

The gateway SHOULD fail affected routes independently where the platform supports isolation.

---

# 191. Health

Gateway health checks MUST distinguish:

```text
LIVENESS

READINESS
```

---

# 192. Backend Health

A single backend service outage MUST NOT make the entire gateway liveness fail.

---

# 193. Readiness

Gateway readiness SHOULD represent whether the gateway can safely process its intended traffic.

---

# 194. Graceful Shutdown

Gateway instances MUST stop accepting new traffic before termination and allow bounded completion of in-flight requests.

---

# 195. Security Boundary

Administrative gateway interfaces MUST be isolated from public data-plane access.

---

# 196. Management API

Gateway management APIs MUST require strong authentication and authorization.

---

# 197. Admin Exposure

Gateway administration MUST NOT be exposed publicly unless explicitly secured and justified.

---

# 198. Configuration Secret

Gateway secrets MUST follow ADR-063.

---

# 199. WAF/Gateway Rules as Code

Material gateway/WAF policies SHOULD be version controlled.

---

# 200. Manual Production Rule

Manual emergency edge rules MAY be applied only with audit and later reconciliation to the source of truth.

---

# 201. Testing Strategy

Gateway behavior MUST have automated validation.

---

# 202. Route Test

Tests SHOULD verify route mapping.

---

# 203. Auth Test

Protected routes SHOULD verify unauthenticated requests are rejected.

---

# 204. Header Test

Tests SHOULD verify untrusted security headers are stripped/overwritten.

---

# 205. Rate Limit Test

Rate-limit behavior SHOULD test:

```text
Below Limit

At Limit

Above Limit

Burst

Reset
```

---

# 206. Failure Policy Test

Distributed rate-limiter failure behavior MUST be tested.

---

# 207. Payload Limit Test

Request-size boundaries SHOULD test:

```text
MAX - 1

MAX

MAX + 1
```

---

# 208. Timeout Test

Upstream timeout behavior SHOULD be tested deterministically.

---

# 209. Retry Test

Any configured gateway retry MUST verify method/idempotency constraints.

---

# 210. CORS Test

Browser-facing APIs SHOULD have automated CORS policy tests.

---

# 211. WAF Test

Critical custom WAF rules SHOULD be validated with safe representative traffic.

---

# 212. Canary Test

Routing rules SHOULD verify controlled canary selection.

---

# 213. Cache Test

Edge cache behavior SHOULD verify authorization-sensitive responses cannot leak across callers.

---

# 214. Correlation Test

Tests SHOULD verify request/correlation IDs are propagated correctly.

---

# 215. Security Test

Tests MUST verify credentials are absent from gateway logs where custom logging exists.

---

# 216. Load Test

Critical public gateways SHOULD receive representative traffic/load testing.

---

# 217. Rate Limit Under Load

Limiter correctness SHOULD be validated under concurrent multi-replica load where global semantics are required.

---

# 218. Edge Review Checklist

Material edge changes SHOULD evaluate:

```text
[ ] Is this an edge concern or business concern?

[ ] Does backend authorization still exist?

[ ] Are untrusted identity headers removed?

[ ] Is TLS configured correctly?

[ ] Is the route private or public?

[ ] Are route rules deterministic?

[ ] Is a BFF actually required?

[ ] Is BFF fan-out bounded?

[ ] What is the request-size limit?

[ ] Are rate limits required?

[ ] What is the rate-limit dimension?

[ ] Are burst limits defined?

[ ] Is limiter state global or per instance?

[ ] What happens if the limiter store fails?

[ ] Are 429 semantics documented?

[ ] Are gateway retries safe?

[ ] Could retries multiply downstream?

[ ] Are timeout budgets aligned?

[ ] Is WAF appropriate?

[ ] Are sensitive headers excluded from logs?

[ ] Is correlation propagated?

[ ] Could edge cache leak authenticated data?

[ ] Is canary routing observable?

[ ] Can configuration be rolled back?

[ ] Is high availability sufficient?
```

---

# 219. Edge Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] Public routes explicitly declared

[ ] Private services not publicly routable

[ ] Request-size limit configured

[ ] Header-size limit configured

[ ] Authentication required for protected routes

[ ] Security identity headers sanitized

[ ] Rate-limit policy configured for high-risk public APIs

[ ] Bearer tokens excluded from access logs

[ ] API keys excluded from access logs

[ ] Gateway timeouts bounded

[ ] Mutation retries disabled unless explicitly safe

[ ] TLS policy approved

[ ] Route configuration version controlled
```

---

# 220. Enterprise Edge Gate

An API exposure is not considered compliant when applicable conditions include:

```text
[ ] Business authorization exists only in gateway

[ ] Backend trusts arbitrary external X-User/X-Role headers

[ ] Private service is unintentionally publicly routed

[ ] Request body size is unlimited

[ ] Header size is unlimited

[ ] Public high-risk API has no abuse/capacity control

[ ] Rate limit depends only on one local gateway replica when global semantics are claimed

[ ] Rate-limiter failure behavior is undefined

[ ] Gateway retries non-idempotent mutations blindly

[ ] Edge timeout exceeds client deadline without rationale

[ ] Gateway access logs contain bearer tokens or API keys

[ ] Authenticated responses are cached without safe identity-aware semantics

[ ] WAF/route production rules are changed with no audit trail

[ ] Gateway is a single production instance

[ ] Gateway contains material domain business logic
```

---

# 221. Anti-Patterns

The following are prohibited or strongly discouraged:

- API Gateway as God Service
- domain logic in edge scripts
- backend authorization removed because gateway authenticates
- trusting caller-supplied identity headers
- blind bearer-token forwarding
- public wildcard route to all services
- BFF per client without meaningful need
- BFF as domain database owner
- unbounded request bodies
- unlimited headers
- rate limiting only after expensive backend processing
- global rate limit implemented with per-instance counters
- blind fail-open/fail-closed rate-limit behavior
- API keys in logs
- bearer tokens in access logs
- query parameters with secrets
- retries on POST mutations without idempotency semantics
- retry at every layer
- large gateway request queues
- caching authenticated content without correct keys
- unrestricted wildcard CORS
- WAF treated as application-security replacement
- IP allowlist treated as identity
- one gateway instance in production
- manual untracked production routing rules

---

# 222. Positive Consequences

The decision provides:

- consistent API exposure
- stronger edge security
- backend capacity protection
- controlled abuse handling
- safer partner integration
- bounded traffic
- better observability
- safer canary deployment
- centralized TLS governance
- reduced accidental public exposure
- controlled client-specific composition
- improved edge availability

---

# 223. Negative Consequences

The decision introduces:

- edge infrastructure
- rate-limit policy management
- WAF tuning
- routing governance
- additional operational dependency
- possible BFF services
- distributed limiter complexity
- additional failure modes

These costs are accepted because uncontrolled external traffic directly affects platform availability and security.

---

# 224. Neutral Consequences

The decision also means:

- not every internal service requires an API Gateway
- not every API requires the same rate limit
- not every client requires a BFF
- not every route should be publicly exposed
- not every edge failure should be retried
- WAF rules require tuning
- some traffic-control policies belong at multiple layers for different reasons
- gateway authentication does not eliminate backend authorization

---

# 225. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Gateway outage | Critical | Low/Medium | HA deployment |
| Backend overload | Critical | Medium | Rate limits + admission control |
| Identity spoofing | Critical | Medium | Header sanitization |
| Retry amplification | High | Medium | Single retry ownership |
| Rate-limit inconsistency | High | Medium | Distributed limiter |
| WAF false positive | High | Medium | Monitor/tune rules |
| Sensitive access logs | Critical | Medium | Header/query filtering |
| Cache data leakage | Critical | Low/Medium | Safe cache-key semantics |
| Public route exposure | Critical | Low/Medium | Explicit routes |
| BFF distributed monolith | High | Medium | Boundary governance |

---

# 226. Implementation Guidance

The following rules are mandatory:

1. The API Gateway must remain focused on edge concerns.
2. Business authorization must remain enforced by backend services.
3. Untrusted security-sensitive headers must be removed or overwritten.
4. TLS configuration must follow enterprise security policy.
5. Public and private routes must be explicitly controlled.
6. Route configuration must be version controlled.
7. BFFs must only be introduced for meaningful client-specific orchestration needs.
8. BFFs must not become owners of core domain rules.
9. BFF fan-out must remain bounded.
10. Public/high-risk APIs must use appropriate traffic-control policies.
11. Rate-limit dimensions must reflect real consumer identity/capacity semantics.
12. Global limits must not rely on isolated per-instance counters.
13. Rate-limiter failure behavior must be explicit.
14. 429 responses should provide appropriate retry guidance.
15. Request, header and URL sizes must be bounded.
16. Gateway timeouts must align with end-to-end deadline hierarchy.
17. Gateway retries must be limited to safe/idempotent operations.
18. Retry amplification across gateway and backend must be prevented.
19. Excessive request queueing must be avoided.
20. Edge caching must not leak authenticated data.
21. WAF should provide defense in depth without replacing application security.
22. Access logs must exclude bearer tokens, API keys and unnecessary PII.
23. Correlation and trace context should be propagated safely.
24. Canary/blue-green routing must be observable and reversible.
25. Gateway deployments must be highly available.
26. Management interfaces must be strongly protected.
27. Edge configuration changes must be auditable and rollback-capable.
28. Gateway/limiter behavior must have automated boundary and load tests.

---

# 227. Validation

This ADR will be validated through:

- approved cloud API gateway
- AWS load-balancing services
- AWS WAF
- Kubernetes Ingress / Gateway API
- OAuth2/OIDC
- Spring Security
- Redis where distributed limiting is required
- structured access logging
- OpenTelemetry/Micrometer where appropriate
- JUnit/integration tests
- load testing
- security testing
- CI/CD configuration validation
- route fitness functions

---

# 228. Success Criteria

The decision is successful when:

- backend services remain protected from uncontrolled traffic
- public/private exposure is explicit
- identity spoofing through headers is prevented
- rate-limit behavior remains consistent across edge replicas
- gateway retries do not duplicate business mutations
- API keys/tokens do not appear in access logs
- request sizes are bounded
- backend authorization remains intact
- BFFs improve client interaction without duplicating domain ownership
- edge failures are observable
- gateway configuration can be rolled back safely
- canary traffic can be controlled and reversed quickly

---

# 229. Alternatives Rejected

## 229.1 Business Logic in API Gateway

Rejected because it centralizes domain behavior in infrastructure and creates a distributed monolith.

---

## 229.2 Gateway Authentication as Sole Authorization

Rejected because resource ownership and domain access rules belong at the protected resource.

---

## 229.3 One BFF per Frontend Automatically

Rejected because BFFs add deployment and operational complexity.

---

## 229.4 Per-Instance Global Rate Limit

Rejected because multiple gateway replicas would each enforce independent limits.

---

## 229.5 Unlimited Edge Queueing

Rejected because queues convert overload into latency and memory pressure.

---

## 229.6 Retry Every Failed Request

Rejected because mutation retries can duplicate side effects and amplify failures.

---

## 229.7 WAF as Primary Application Security

Rejected because WAF cannot understand full domain authorization or application behavior.

---

# 230. Related Decisions

This ADR extends and implements:

- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard

---

# 231. References

- RFC 9110 — HTTP Semantics
- RFC 6585 — Additional HTTP Status Codes
- OAuth 2.0 Security Best Current Practice
- OpenID Connect Core
- OWASP API Security Top 10
- OWASP REST Security Cheat Sheet
- AWS WAF Documentation
- AWS API Gateway Documentation
- Kubernetes Gateway API Documentation
- Kubernetes Ingress Documentation
- Envoy Documentation
- NGINX Documentation
- Spring Cloud Gateway Documentation
- Google Site Reliability Engineering

---

# 232. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise API edge and traffic-management baseline |

---

# 233. Decision Summary

The edge architecture becomes:

```text
CLIENT
   |
   v
WAF
   |
   v
API GATEWAY
   |
   +--> TLS
   +--> AUTHENTICATION
   +--> RATE LIMIT
   +--> REQUEST LIMIT
   +--> ROUTING
   +--> CORRELATION
   |
   v
BACKEND
   |
   v
RESOURCE AUTHORIZATION
```

The responsibility boundary is:

```text
EDGE
 |
 +--> TRAFFIC
 +--> PROTOCOL
 +--> COARSE SECURITY
 +--> ROUTING

SERVICE
 |
 +--> BUSINESS
 +--> RESOURCE AUTHORIZATION
 +--> DOMAIN STATE
```

Rate limiting becomes:

```text
REQUEST
   |
   v
IDENTIFY LIMIT KEY
   |
   v
RATE / BURST POLICY
   |
 +--+--+
 |     |
ALLOW REJECT
 |     |
 v     v
ROUTE  429
```

Global limiting becomes:

```text
GATEWAY A ----\
               \
GATEWAY B ------> SHARED /
               /  CONSISTENT
GATEWAY C ----/   LIMIT STATE
```

rather than:

```text
GATEWAY A = 100/s
GATEWAY B = 100/s
GATEWAY C = 100/s

claimed global limit = 100/s

actual possible = 300/s
```

Request protection becomes:

```text
INBOUND REQUEST
      |
      +--> BODY LIMIT
      +--> HEADER LIMIT
      +--> URL LIMIT
      +--> RATE LIMIT
      +--> AUTH
      |
      v
EXPENSIVE BACKEND WORK
```

BFF becomes:

```text
WEB
 |
 v
WEB BFF
 |
 +--> ORDER
 +--> CUSTOMER
 +--> PRODUCT
```

only when client-specific aggregation justifies it.

It does not become:

```text
WEB BFF
 |
 +--> CORE BUSINESS RULES
 +--> DOMAIN DATABASE
 +--> GLOBAL WORKFLOW OWNER
```

Retry becomes:

```text
REQUEST FAILED
      |
      v
IS METHOD / OPERATION
SAFE TO RETRY?
      |
   +--+--+
   |     |
  NO    YES
   |     |
   v     v
RETURN  RETRY WITHIN
FAILURE STRICT BUDGET
```

Edge caching becomes:

```text
RESPONSE
   |
   v
PUBLIC / SAFE
TO SHARE?
   |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
CACHE DO NOT
      SHARED-CACHE
```

High availability becomes:

```text
EDGE
 |
 +--> REPLICA A
 |
 +--> REPLICA B
 |
 +--> REPLICA C
 |
 v
MULTIPLE FAILURE DOMAINS
```

The complete edge equation is:

```text
EXPLICIT ROUTING
        +
TLS
        +
AUTHENTICATION
        +
HEADER SANITIZATION
        +
REQUEST LIMITS
        +
RATE LIMITING
        +
QUOTA / ADMISSION CONTROL
        +
WAF
        +
BOUNDED TIMEOUTS
        +
SAFE RETRIES
        +
CORRELATION
        +
SAFE ACCESS LOGGING
        +
CONTROLLED CANARY ROUTING
        +
HIGH AVAILABILITY
        +
BACKEND RESOURCE AUTHORIZATION
        =
SAFE ENTERPRISE API EDGE
```

The governing principle is:

```text
Protect the backend
before expensive work begins.

Authenticate at the edge
when appropriate.

Authorize again where
the resource is owned.

Do not trust identity headers
from untrusted callers.

Do not move domain logic
into the gateway.

Use BFF only where
client-specific orchestration
provides real value.

Bound request size.

Bound header size.

Bound traffic.

Bound bursts.

Bound retries.

Bound queueing.

Do not claim a global
rate limit with local counters.

Define what happens
when the limiter fails.

Do not retry mutations
without idempotency semantics.

Do not log credentials.

Do not cache personalized
responses with unsafe keys.

Use WAF as defense in depth,
not as application security.

Version and audit edge policy.

Make canary routing reversible.

Deploy the edge redundantly.

And remember:

the API Gateway
is the front door.

It should control
who enters and how much
traffic may enter.

It should not become
the building itself.
```
