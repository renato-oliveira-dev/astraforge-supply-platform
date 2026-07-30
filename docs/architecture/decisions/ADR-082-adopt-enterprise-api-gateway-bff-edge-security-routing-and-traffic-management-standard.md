# ADR-082: Adopt Enterprise API Gateway, BFF, Edge Security, Routing and Traffic Management Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-082 |
| Title | Adopt Enterprise API Gateway, BFF, Edge Security, Routing and Traffic Management Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | API Gateway, BFF, Edge Security, Routing, Traffic Management |
| Related Work Items | OAuth2, JWT, Keycloak, Kubernetes, Ingress, WAF, Rate Limiting |
| Supersedes | ADR-073 |
| Superseded By | None |

---

> **Consolidation:** This ADR supersedes ADR-073 and is the canonical decision for API Gateway, BFF, edge security, routing, traffic management and rate-limiting governance.

---

# 1. Context

Enterprise microservice platforms frequently expose APIs to:

```text
WEB APPLICATIONS

MOBILE APPLICATIONS

PARTNER SYSTEMS

INTERNAL SYSTEMS

BATCH JOBS

EXTERNAL INTEGRATIONS
```

Without a controlled edge architecture, clients can become directly coupled to individual microservices:

```text
CLIENT
  |
  +--> CUSTOMER SERVICE
  |
  +--> ORDER SERVICE
  |
  +--> CART SERVICE
  |
  +--> PRODUCT SERVICE
```

This creates problems involving:

```text
SECURITY

SERVICE DISCOVERY

CORS

RATE LIMITING

AUTHENTICATION

ROUTING

API VERSIONING

OBSERVABILITY

SERVICE EXPOSURE

CLIENT COUPLING
```

A controlled edge architecture provides:

```text
CLIENT
   |
   v
EDGE / API GATEWAY
   |
   +--> SERVICE A
   |
   +--> SERVICE B
   |
   +--> SERVICE C
```

However, the gateway itself can become:

```text
A SINGLE BOTTLENECK

A MONOLITHIC BUSINESS LAYER

A SECURITY RISK

A RETRY AMPLIFIER

A LATENCY SOURCE
```

if its responsibilities are not explicitly bounded.

---

# 2. Problem Statement

The organization requires standards covering:

- API Gateway
- Backend for Frontend
- Kubernetes ingress
- edge routing
- authentication
- OAuth2
- OIDC
- JWT
- authorization
- CORS
- TLS
- mTLS
- WAF
- rate limiting
- throttling
- quotas
- request-size limits
- header propagation
- correlation IDs
- trace context
- client IP
- trusted proxies
- API versioning
- timeout budgets
- retries
- circuit breaking
- canary deployment
- blue-green deployment
- traffic splitting
- health checks
- service exposure
- observability
- security testing

---

# 3. Decision Drivers

Primary drivers are:

1. edge security
2. consistent API exposure
3. client isolation
4. traffic protection
5. service encapsulation
6. scalability
7. operational visibility
8. controlled routing
9. resilience
10. deployment safety
11. maintainability
12. defense in depth

---

# 4. Decision

Externally consumed microservice APIs SHOULD be exposed through an approved edge layer.

Preferred architecture:

```text
INTERNET / CLIENT
       |
       v
DNS / CDN / WAF
       |
       v
API GATEWAY / INGRESS
       |
       v
BFF / APPLICATION API
       |
       v
MICROSERVICES
```

Not every layer is mandatory in every deployment.

Responsibilities MUST remain explicit.

---

# 5. Fundamental Principle

```text
The edge protects
and routes traffic.

The BFF adapts APIs
for a client experience.

Business services own
business rules.

The gateway must not become
the enterprise monolith
in front of microservices.
```

---

# 6. Edge Responsibilities

The edge MAY own:

```text
TLS TERMINATION

AUTHENTICATION VALIDATION

ROUTING

RATE LIMITING

REQUEST SIZE LIMITS

CORS

WAF INTEGRATION

TRAFFIC SPLITTING

OBSERVABILITY
```

---

# 7. Edge Non-Responsibilities

The edge SHOULD NOT own substantial:

```text
ORDER RULES

PRICING RULES

WORKFLOW RULES

CUSTOMER RULES

TRANSACTIONAL BUSINESS LOGIC
```

---

# 8. Gateway Business Logic

Complex business logic in gateway policies/scripts is prohibited.

---

# 9. Backend for Frontend

A BFF MAY be introduced when a specific client requires orchestration or representation materially different from the underlying services.

---

# 10. BFF Examples

Possible BFFs:

```text
WEB BFF

MOBILE BFF

PARTNER BFF
```

---

# 11. BFF Responsibility

A BFF MAY perform:

```text
Response Composition

Client-Specific DTO Adaptation

Limited Orchestration

Client-Specific Validation

Protocol Adaptation
```

---

# 12. BFF Domain Logic

Core domain rules MUST remain in domain/application services rather than being duplicated into BFFs.

---

# 13. BFF Duplication

Multiple BFFs MUST NOT independently reimplement the same critical business rule.

---

# 14. BFF Database

A BFF SHOULD NOT directly access another service's database.

---

# 15. Service Encapsulation

Each service remains owner of its persistence and business capabilities.

---

# 16. Direct Microservice Exposure

Internal microservices SHOULD NOT be publicly reachable unless explicit architectural requirements justify direct exposure.

---

# 17. Kubernetes Service

Internal services SHOULD normally use private cluster/network exposure.

---

# 18. Public Load Balancer

Creating an independent public load balancer for every microservice SHOULD be avoided.

---

# 19. Network Defense

Gateway protection does not eliminate internal network security requirements.

---

# 20. Defense in Depth

Internal services MUST still assume requests can be malformed or unauthorized.

---

# 21. Authentication

OAuth 2.0 / OpenID Connect SHOULD be used for supported user/client authentication scenarios.

---

# 22. Identity Provider

Authentication SHOULD use an approved Identity Provider.

Example:

```text
Keycloak

Enterprise IdP
```

---

# 23. JWT

JWT access tokens MAY be validated at the edge.

---

# 24. Service Validation

Security-sensitive backend services SHOULD also enforce applicable authentication/authorization rather than blindly trusting edge presence.

---

# 25. Gateway Is Not Authorization

Passing through the gateway MUST NOT itself imply:

```text
AUTHORIZED
```

---

# 26. Token Validation

JWT validation MUST verify applicable:

```text
Signature

Issuer

Audience

Expiration

Not-Before

Required Claims
```

---

# 27. JWT Decode

Simply decoding a JWT is not validation.

---

# 28. Algorithm

Allowed JWT algorithms MUST be explicitly controlled.

---

# 29. `alg=none`

Unsigned tokens MUST NOT be accepted.

---

# 30. Key Rotation

JWT verification MUST support Identity Provider signing-key rotation.

---

# 31. JWKS

JWKS retrieval/caching MUST use bounded and resilient behavior.

---

# 32. Expired Token

Expired tokens MUST be rejected.

---

# 33. Audience

A valid token issued for an unrelated API MUST NOT automatically authorize access to another API.

---

# 34. Authorization

Authorization MUST remain aligned with the resource/business operation.

---

# 35. Coarse Authorization

Gateway MAY enforce coarse-grained authorization.

Example:

```text
Required Scope
```

---

# 36. Fine-Grained Authorization

Fine-grained authorization SHOULD remain close to the protected business resource.

---

# 37. Example

Gateway:

```text
scope = orders.write
```

Service:

```text
Can this user modify
this customer's order?
```

---

# 38. Token Propagation

User access tokens MAY be propagated downstream where user identity is required.

---

# 39. Token Leakage

Access tokens MUST NOT be:

```text
Logged

Placed in URLs

Returned in Errors

Added to Metrics
```

---

# 40. Service Identity

Service-to-service communication SHOULD use an approved workload/service identity mechanism where applicable.

---

# 41. mTLS

mTLS MAY be used for high-assurance service/partner communication.

---

# 42. TLS

External API traffic MUST use HTTPS/TLS.

---

# 43. TLS Version

Deprecated TLS versions/ciphers MUST be disabled according to enterprise security policy.

---

# 44. Certificate

Certificate issuance, renewal and rotation SHOULD be automated.

---

# 45. CORS

CORS MUST be explicitly configured.

---

# 46. CORS Is Browser Policy

CORS is not authentication or authorization.

---

# 47. Wildcard Origin

Authenticated APIs SHOULD NOT casually use:

```text
Access-Control-Allow-Origin: *
```

---

# 48. Allowed Origins

Production origins SHOULD be explicitly configured.

---

# 49. Credentials

Credentialed cross-origin requests require controlled origins.

---

# 50. Preflight

`OPTIONS` handling SHOULD be consistent across the edge architecture.

---

# 51. CORS Duplication

Conflicting CORS rules across:

```text
CDN

Gateway

BFF

Service
```

SHOULD be avoided.

---

# 52. Rate Limiting

Public APIs MUST consider rate limiting.

---

# 53. Rate Limit Dimensions

Rate limiting MAY use:

```text
Client

API Key

User

Tenant

IP

Route

Operation
```

according to trust and business semantics.

---

# 54. IP-Only Limiting

IP address alone is often insufficient because:

```text
NAT

Corporate Proxy

Mobile Networks
```

can aggregate many users.

---

# 55. Authenticated Rate Limit

Authenticated APIs SHOULD prefer stable authenticated identity dimensions where practical.

---

# 56. Anonymous Rate Limit

Anonymous APIs MAY require IP/network-based protection.

---

# 57. Rate Limit Response

Rate-limit rejection SHOULD use:

```text
HTTP 429 Too Many Requests
```

with appropriate metadata where supported.

---

# 58. `Retry-After`

`Retry-After` SHOULD be returned when meaningful.

---

# 59. Distributed Rate Limit

Multi-instance gateways require distributed or otherwise globally coherent limiting where global quotas matter.

---

# 60. Pod-Local Rate Limit

Independent per-pod limits can multiply the effective allowed rate.

---

# 61. Example

```text
10 PODS
x
100 requests/s
=
1000 requests/s
```

when the intended global limit was:

```text
100 requests/s
```

---

# 62. Throttling

Rate limiting protects systems from excessive request rates.

Throttling MAY shape traffic rather than immediately reject it where architecture supports bounded queues.

---

# 63. Edge Queue

The gateway MUST NOT maintain unbounded request queues.

---

# 64. Quota

Longer-term quotas MAY control:

```text
Requests Per Day

Requests Per Month

Partner Contract Limits
```

---

# 65. Rate vs Quota

Short-term rate and contractual quota are separate concerns.

---

# 66. Request Size

Every externally exposed API SHOULD have bounded request sizes.

---

# 67. Global Size Limit

The edge SHOULD establish a safe global maximum.

---

# 68. Endpoint Size Limit

Endpoints such as file upload MAY require explicit route-specific limits.

---

# 69. Oversized Request

Oversized requests SHOULD be rejected before consuming unnecessary backend resources.

---

# 70. Content Length

Declared `Content-Length` MUST NOT be the only defense against oversized streaming/chunked requests.

---

# 71. Header Size

Request header sizes MUST be bounded.

---

# 72. Header Bomb

Very large or excessive headers can consume edge/backend resources.

---

# 73. Header Count

Header count SHOULD also be bounded where supported.

---

# 74. URI Length

Request URI/query-string size SHOULD be bounded.

---

# 75. Query Abuse

Large numbers of query parameters SHOULD be rejected according to API limits.

---

# 76. Compression

Request decompression MUST account for decompression-bomb risks.

---

# 77. WAF

Internet-facing APIs SHOULD use approved WAF capabilities where available.

---

# 78. WAF Purpose

WAF provides an additional protection layer against classes of malicious traffic.

---

# 79. WAF Is Not Secure Coding

WAF MUST NOT replace:

```text
Input Validation

Authorization

Parameterized Queries

Output Encoding
```

---

# 80. Managed Rules

Managed WAF rules SHOULD be introduced with monitoring/tuning to avoid unacceptable false positives.

---

# 81. Custom Rules

Custom WAF rules SHOULD have:

```text
Owner

Reason

Test

Review Date
```

---

# 82. Emergency Rule

Temporary emergency WAF rules MUST not silently become permanent undocumented application logic.

---

# 83. Routing

Routes MUST be explicit and version-controlled.

---

# 84. Route Ownership

Every public route SHOULD map to an owning application/team.

---

# 85. Wildcard Routing

Overly broad wildcard routes SHOULD be avoided.

---

# 86. Internal Endpoint Exposure

Routing rules MUST NOT accidentally expose:

```text
/actuator

/internal

/admin

/debug

metrics endpoints
```

---

# 87. Actuator

Management endpoints SHOULD use separate protected exposure where possible.

---

# 88. API Versioning

The gateway MAY route multiple API versions.

Example:

```text
/api/v1/orders
/api/v2/orders
```

---

# 89. Version Ownership

API compatibility remains an application-contract concern rather than merely a routing concern.

---

# 90. Version Translation

Complex version transformation SHOULD NOT accumulate indefinitely in gateway policies.

---

# 91. Deprecation

Deprecated versions SHOULD have explicit:

```text
Owner

Deadline

Traffic Metric

Migration Plan
```

---

# 92. Header Propagation

Only approved headers SHOULD be propagated downstream.

---

# 93. Hop-by-Hop Headers

Hop-by-hop headers MUST be handled according to HTTP semantics.

---

# 94. Client-Controlled Internal Header

Clients MUST NOT be able to forge trusted internal identity/authorization headers.

---

# 95. Example

If the platform uses:

```text
X-User-Id
X-User-Roles
X-Internal-Client
```

the gateway MUST remove any untrusted inbound versions before generating trusted values.

---

# 96. Header Spoofing

Never:

```text
CLIENT
  |
  | X-User-Roles: ADMIN
  v
GATEWAY
  |
  | passes unchanged
  v
SERVICE
```

---

# 97. Trusted Header Pattern

Prefer:

```text
CLIENT HEADER
     |
     X
REMOVE
     |
     v
VALIDATE IDENTITY
     |
     v
CREATE TRUSTED INTERNAL HEADER
```

when internal headers are genuinely required.

---

# 98. Prefer Token Claims

Where practical, signed token claims SHOULD be preferred over unsigned identity headers.

---

# 99. Correlation ID

The edge SHOULD accept or generate a correlation/request identifier.

---

# 100. Untrusted Correlation ID

Inbound correlation IDs MUST be validated and length-bounded.

---

# 101. Correlation Injection

Correlation values MUST be sanitized before logging.

---

# 102. Trace Context

Approved W3C Trace Context SHOULD be propagated when distributed tracing is enabled.

---

# 103. Trace Header

Malformed trace headers MUST NOT break request processing.

---

# 104. Baggage

Trace baggage MUST be tightly controlled.

---

# 105. PII in Baggage

PII, tokens and secrets MUST NOT be placed in tracing baggage.

---

# 106. Client IP

Applications sometimes require the originating client IP.

---

# 107. `X-Forwarded-For`

`X-Forwarded-For` MUST NOT be blindly trusted from arbitrary internet clients.

---

# 108. Trusted Proxy

Only forwarding headers inserted/validated by trusted proxies may be used as authoritative network metadata.

---

# 109. Header Chain

The platform MUST define which proxy in the forwarding chain is trusted.

---

# 110. Direct Backend Access

Backend services MUST NOT become reachable through a path that bypasses the trusted proxy while still trusting forwarded identity/IP headers.

---

# 111. Forwarded Header

RFC `Forwarded` or controlled `X-Forwarded-*` conventions SHOULD be standardized.

---

# 112. Host Header

Host-header handling MUST prevent host-header injection issues.

---

# 113. External URL Generation

Applications generating external URLs MUST use trusted configured public origins or validated forwarded metadata.

---

# 114. Timeout Budget

Every request path MUST have an end-to-end timeout budget.

---

# 115. Example

Conceptually:

```text
CLIENT TIMEOUT       10s
      |
GATEWAY BUDGET        8s
      |
BFF BUDGET            6s
      |
SERVICE CALL          3s
```

rather than every layer independently using:

```text
30s
```

---

# 116. Downstream Timeout

An inner downstream timeout MUST leave sufficient time for upstream error handling.

---

# 117. Gateway Timeout

Gateway timeout MUST be bounded.

---

# 118. `504`

Backend timeout SHOULD map appropriately to:

```text
504 Gateway Timeout
```

when the edge acts as a gateway and semantics apply.

---

# 119. Retry

Automatic edge retries MUST be conservative.

---

# 120. Non-Idempotent Retry

The gateway MUST NOT blindly retry:

```text
POST /orders
```

unless idempotency semantics explicitly make the operation safe.

---

# 121. Idempotent Methods

Even nominally idempotent methods require bounded retry and failure classification.

---

# 122. Retry Multiplication

Retry configuration MUST consider all layers:

```text
CLIENT

CDN

GATEWAY

BFF

SERVICE CLIENT

MESSAGE PROCESSOR
```

---

# 123. Example

```text
CLIENT RETRY       3
x
GATEWAY RETRY      3
x
SERVICE RETRY      3
=
27 ATTEMPTS
```

is unacceptable unless explicitly engineered.

---

# 124. Edge Retry Policy

Edge retries SHOULD generally be:

```text
LOW COUNT

SHORT

TRANSIENT-FAILURE ONLY

IDEMPOTENCY-AWARE
```

---

# 125. Backoff

Retries SHOULD use bounded backoff/jitter where applicable.

---

# 126. Retry Budget

A retry MUST fit inside the remaining request deadline.

---

# 127. Circuit Breaker

Circuit breaking MAY be implemented at gateway/BFF level for unstable downstream dependencies.

---

# 128. Circuit Breaker Duplication

Circuit Breakers across multiple layers MUST be coordinated to avoid confusing failure behavior.

---

# 129. Critical Business Context

Domain-specific fallback SHOULD generally remain in application services rather than generic gateway configuration.

---

# 130. Bulkhead

Gateway concurrency SHOULD be bounded.

---

# 131. Route Isolation

Critical routes MAY require independent concurrency/rate limits from expensive bulk routes.

---

# 132. Slow Client

Slow clients MUST NOT consume unbounded gateway resources.

---

# 133. Connection Limits

Edge connection counts and idle timeouts MUST be bounded.

---

# 134. Keep-Alive

Keep-alive settings SHOULD balance connection reuse and resource consumption.

---

# 135. HTTP/2

HTTP/2 MAY improve connection efficiency but stream/concurrency limits MUST be configured appropriately.

---

# 136. HTTP/3

HTTP/3 MAY be adopted when platform/client requirements justify it and operational support is mature.

---

# 137. WebSocket

WebSocket routes require explicit:

```text
Connection Limits

Idle Timeout

Authentication

Scaling

Drain Behavior
```

---

# 138. Long-Lived Connection

Long-lived connections require special deployment/shutdown handling.

---

# 139. Graceful Shutdown

Gateway/BFF instances MUST drain existing requests/connections during planned termination where supported.

---

# 140. Kubernetes Readiness

Traffic MUST only be routed to ready instances.

---

# 141. Liveness

Liveness MUST NOT depend on optional downstream services.

---

# 142. Readiness

Readiness SHOULD represent the ability to safely serve the traffic assigned to the instance.

---

# 143. Gateway Dependency Health

A temporary downstream failure SHOULD NOT necessarily make the entire gateway unready.

---

# 144. Route-Level Failure

Per-route resilience is generally preferable to removing the entire gateway from service because one backend is unavailable.

---

# 145. Canary Deployment

The edge MAY route a controlled percentage of traffic to a new application version.

---

# 146. Canary Example

```text
v1 = 95%

v2 = 5%
```

---

# 147. Canary Metric

Canary progression MUST use measurable health criteria.

---

# 148. Canary Signals

Applicable signals include:

```text
5xx Rate

Latency

Timeout Rate

Business Errors

Resource Saturation
```

---

# 149. Canary User Cohort

Canary MAY use stable user/tenant cohorts when random traffic splitting is inappropriate.

---

# 150. Sticky Routing

Sticky routing SHOULD only be used where state/session requirements justify it.

---

# 151. Stateless Services

Stateless APIs SHOULD not require sticky sessions.

---

# 152. Blue-Green

Blue-green routing MAY support rapid deployment and rollback.

---

# 153. Database Compatibility

Traffic switching does not solve incompatible database migrations.

---

# 154. Expand/Contract

Blue-green/canary deployments SHOULD use backward-compatible database evolution.

---

# 155. Flyway

Already-applied Flyway migrations MUST NOT be modified.

Database changes required by new API versions MUST use new migrations.

---

# 156. Traffic Shift

Traffic SHOULD only move after:

```text
Readiness

Smoke Tests

Migration Compatibility

Observability Validation
```

---

# 157. Rollback

Rollback MUST account for:

```text
Database Changes

Message Contracts

Cache Changes

Search Index Changes
```

not merely application binaries.

---

# 158. Shadow Traffic

Traffic mirroring MAY be used for validation.

---

# 159. Shadow Write

Mirrored traffic MUST NOT accidentally execute duplicate production writes.

---

# 160. Shadow PII

Mirrored traffic containing PII requires approved handling.

---

# 161. Request Transformation

Simple protocol/header transformations MAY occur at the gateway.

---

# 162. Complex Transformation

Large business payload transformations SHOULD reside in application code where they can be versioned and tested properly.

---

# 163. Response Transformation

Gateway response transformations SHOULD remain minimal.

---

# 164. Error Contract

The edge MUST preserve intentional application error semantics where possible.

---

# 165. Generic Gateway Error

Infrastructure failures MAY use gateway-level error contracts.

---

# 166. Problem Details

HTTP APIs SHOULD use standardized Problem Details where established by platform standards.

---

# 167. Error Leakage

Gateway errors MUST NOT expose:

```text
Internal Hostnames

Stack Traces

Pod Names

Credentials

Internal URLs
```

---

# 168. Backend Error

Raw backend infrastructure errors SHOULD be sanitized before external exposure.

---

# 169. Status Code Preservation

The gateway SHOULD avoid converting every backend failure into:

```text
500
```

---

# 170. `401`

Missing/invalid authentication SHOULD map to:

```text
401 Unauthorized
```

where applicable.

---

# 171. `403`

Authenticated but unauthorized access SHOULD map to:

```text
403 Forbidden
```

where applicable.

---

# 172. `404`

Resource-not-found semantics SHOULD remain distinguishable from authorization according to the application's security model.

---

# 173. `429`

Rate limiting SHOULD use:

```text
429 Too Many Requests
```

---

# 174. `502`

Invalid/unavailable upstream responses MAY map to:

```text
502 Bad Gateway
```

where appropriate.

---

# 175. `503`

Unavailable upstream/service capacity MAY map to:

```text
503 Service Unavailable
```

---

# 176. `504`

Upstream timeout MAY map to:

```text
504 Gateway Timeout
```

---

# 177. Security Headers

The edge MAY centrally add applicable browser security headers.

---

# 178. HSTS

Internet HTTPS applications SHOULD consider:

```text
Strict-Transport-Security
```

according to deployment/domain requirements.

---

# 179. CSP

Content Security Policy is primarily relevant to browser-delivered content and SHOULD be configured according to frontend architecture.

---

# 180. API Security Headers

Headers irrelevant to pure APIs SHOULD not be mechanically added without understanding their purpose.

---

# 181. Cache Headers

Gateway/CDN caching MUST be explicit.

---

# 182. Authenticated Response Cache

Authenticated/user-specific responses MUST NOT accidentally become publicly cacheable.

---

# 183. `Cache-Control`

Sensitive API responses SHOULD use appropriate cache-control directives.

---

# 184. Cache Key at Edge

If CDN/gateway response caching is used, cache key dimensions MUST include all relevant request variation.

---

# 185. Authorization Cache Leak

Caching a personalized response without authorization/user variation can cause critical cross-user data leakage.

---

# 186. Compression Response

Response compression MAY reduce bandwidth.

---

# 187. Compression Sensitive Data

Compression of responses containing secrets alongside attacker-controlled data requires security consideration.

---

# 188. API Documentation

Publicly exposed APIs SHOULD have controlled documentation.

---

# 189. Swagger/OpenAPI Exposure

Production Swagger/OpenAPI exposure MUST be intentional and authorized according to platform policy.

---

# 190. Internal API Documentation

Internal endpoints MUST not become public solely because they appear in generated OpenAPI.

---

# 191. Observability

The edge MUST provide traffic observability.

---

# 192. Metrics

Useful metrics include:

```text
requests_total

request_duration

active_requests

request_size

response_size

rate_limit_rejections

authentication_failures

upstream_errors

upstream_timeouts
```

---

# 193. Dimensions

Bounded metric dimensions MAY include:

```text
route

method

status_class

upstream_service
```

---

# 194. Raw URL

Raw URLs containing identifiers MUST NOT become unbounded metric labels.

---

# 195. Route Template

Metrics SHOULD use route templates:

```text
/orders/{id}
```

instead of:

```text
/orders/4e0c...
```

---

# 196. User Metric

User IDs, JWTs, IP addresses and correlation IDs MUST NOT be metric labels.

---

# 197. Logs

Access logs SHOULD include safe:

```text
requestId

method

route

status

elapsedMs

responseSize

upstream
```

---

# 198. Authorization Header

`Authorization` MUST NEVER be logged.

---

# 199. Cookie

Sensitive cookies MUST NOT be logged.

---

# 200. Query String

Query strings MUST be sanitized or omitted when they may contain sensitive values.

---

# 201. Request Body

Request bodies MUST NOT be logged by default at the edge.

---

# 202. Response Body

Response bodies MUST NOT be logged by default.

---

# 203. Client IP Logging

Client IP storage/logging MUST follow privacy and retention requirements.

---

# 204. Trace

Edge tracing SHOULD establish or continue distributed trace context.

---

# 205. Span

Gateway/BFF SHOULD create server/client spans where tracing is enabled.

---

# 206. Sampling

Trace sampling MUST be bounded.

---

# 207. Error Sampling

Higher sampling for errors MAY be useful if privacy/cardinality controls remain satisfied.

---

# 208. Health Metrics

Gateway saturation SHOULD monitor:

```text
CPU

Memory

Connections

Event Loop / Thread Saturation

Request Queue

GC

Network
```

---

# 209. Alerting

Applicable alerts include:

```text
5xx Surge

Authentication Failure Surge

429 Surge

Latency Increase

Upstream Timeout Surge

Connection Saturation

Gateway Instance Loss

Certificate Expiration

WAF Block Surge
```

---

# 210. SLO

Externally exposed API platforms SHOULD define availability and latency SLOs.

---

# 211. Gateway SLO

Gateway availability SHOULD be measured independently from individual backend-service availability.

---

# 212. Route SLO

Critical routes MAY have stricter SLOs than general gateway traffic.

---

# 213. Testing Strategy

Edge behavior requires dedicated automated tests.

---

# 214. Routing Test

Verify each public route resolves to the intended backend.

---

# 215. Negative Routing Test

Internal/admin routes MUST verify they are not externally exposed.

---

# 216. Authentication Test

Test:

```text
Missing Token

Invalid Signature

Expired Token

Wrong Issuer

Wrong Audience
```

---

# 217. Authorization Test

Verify insufficient scopes/roles are rejected.

---

# 218. Header Spoof Test

Client-supplied trusted internal headers MUST be removed or rejected.

---

# 219. CORS Test

Test:

```text
Allowed Origin

Disallowed Origin

Preflight

Credentialed Request
```

---

# 220. Rate Limit Test

Verify configured limits produce deterministic:

```text
429
```

behavior.

---

# 221. Distributed Rate Test

Multi-instance rate limiting SHOULD verify the effective global limit where required.

---

# 222. Request Size Test

Oversized payloads MUST be rejected before reaching the backend where feasible.

---

# 223. Header Size Test

Oversized header requests SHOULD be rejected.

---

# 224. Timeout Test

Slow upstream MUST produce bounded timeout behavior.

---

# 225. Retry Test

Verify non-idempotent requests are not automatically duplicated.

---

# 226. Retry Multiplication Test

Critical request paths SHOULD verify effective retry counts.

---

# 227. Circuit Breaker Test

Repeated upstream failure SHOULD verify expected circuit behavior where configured.

---

# 228. Client Disconnect Test

Abandoned client requests SHOULD not create uncontrolled backend work where cancellation can propagate.

---

# 229. Canary Test

Traffic split SHOULD verify configured percentages/cohorts within expected statistical tolerance.

---

# 230. Blue-Green Test

Traffic switching and rollback SHOULD be exercised.

---

# 231. Graceful Shutdown Test

Gateway/BFF termination SHOULD verify request draining.

---

# 232. Forwarded IP Test

Forged internet `X-Forwarded-For` MUST not become trusted client identity.

---

# 233. Host Header Test

Invalid host headers SHOULD be rejected according to configuration.

---

# 234. Error Sanitization Test

Backend failures MUST not expose internal infrastructure details.

---

# 235. Cache Isolation Test

If edge caching exists, user-specific responses MUST verify cross-user isolation.

---

# 236. WAF Test

Critical WAF rules SHOULD have controlled positive/negative validation.

---

# 237. Security Test

OWASP API Security scenarios SHOULD be included in security validation.

---

# 238. Load Test

Gateway capacity SHOULD be tested using realistic:

```text
Request Rate

Payload Size

Connection Count

Route Mix

TLS Cost
```

---

# 239. Failure Injection

Critical edge architecture SHOULD test:

```text
Backend Down

Backend Slow

DNS Failure

Certificate Problem

Rate-Limit Backend Failure

Identity Provider/JWKS Failure
```

---

# 240. AssertJ

Java BFF/gateway tests MUST follow established project conventions, including meaningful:

```java
.as("...")
```

before applicable assertions.

---

# 241. Edge Architecture Review Checklist

```text
[ ] Is this API intended to be public?

[ ] Does traffic pass through an approved edge?

[ ] Can clients bypass the gateway?

[ ] Are internal/admin routes protected?

[ ] Is authentication validated correctly?

[ ] Is JWT issuer validated?

[ ] Is JWT audience validated?

[ ] Is token expiration validated?

[ ] Is fine-grained authorization enforced by the service?

[ ] Are client-supplied trusted headers stripped?

[ ] Is CORS explicit?

[ ] Is rate limiting required?

[ ] Is the rate limit global or per instance?

[ ] Are request/header/URI sizes bounded?

[ ] Is WAF applicable?

[ ] Is TLS mandatory?

[ ] Is mTLS required?

[ ] Are forwarded headers trusted only from known proxies?

[ ] Can direct backend access bypass proxy assumptions?

[ ] Is the timeout budget defined end-to-end?

[ ] Are edge retries actually safe?

[ ] Could retry layers multiply?

[ ] Is concurrency bounded?

[ ] Can expensive routes starve critical routes?

[ ] Is graceful shutdown supported?

[ ] Are readiness semantics correct?

[ ] Is canary/blue-green routing required?

[ ] Are database migrations backward compatible?

[ ] Can rollback safely occur?

[ ] Are error contracts preserved?

[ ] Are infrastructure details sanitized?

[ ] Could edge caching leak user data?

[ ] Are access logs privacy-safe?

[ ] Are route-level metrics available?

[ ] Are certificates monitored?
```

---

# 242. Edge Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] External routes use HTTPS

[ ] JWT issuer/audience validation configured

[ ] Internal trusted headers are stripped inbound

[ ] Public routes have request-size limits

[ ] Gateway timeout is bounded

[ ] Edge retry count is bounded

[ ] Non-idempotent POST is not blindly retried

[ ] Rate-limit rejection uses HTTP 429

[ ] Authorization header is excluded from logs

[ ] Metrics use route templates

[ ] Internal actuator/admin routes are not public

[ ] Graceful shutdown is configured

[ ] Certificate expiration has alerting

[ ] Canary rollback procedure exists
```

---

# 243. Enterprise Edge Gate

An API edge implementation is not considered compliant when applicable conditions include:

```text
[ ] Internal microservices are unnecessarily exposed directly to the internet

[ ] Gateway contains core business rules

[ ] BFF directly queries another service's database

[ ] JWT is decoded but signature is not validated

[ ] Issuer/audience are ignored

[ ] Gateway presence is treated as sufficient authorization

[ ] Client can forge trusted user/role headers

[ ] CORS uses unrestricted wildcard with sensitive credentialed access

[ ] Public API has no traffic protection

[ ] Effective rate limit multiplies unexpectedly by pod count

[ ] Request body size is unbounded

[ ] Headers are unbounded

[ ] X-Forwarded-For is blindly trusted

[ ] Client can bypass trusted proxy and reach backend

[ ] Every layer uses independent long timeouts

[ ] Non-idempotent writes are automatically retried

[ ] Retry layers multiply uncontrollably

[ ] Gateway maintains unbounded request queues

[ ] Canary proceeds without health criteria

[ ] Rollback ignores database compatibility

[ ] Existing Flyway migration is modified for deployment convenience

[ ] Backend stack traces/internal hosts are externally exposed

[ ] Authorization token is logged

[ ] Raw user IDs become metric labels

[ ] Personalized responses are cached globally

[ ] Internal management endpoints are publicly routed
```

---

# 244. Anti-Patterns

The following are prohibited or strongly discouraged:

- public exposure of every microservice
- gateway as business monolith
- BFF owning domain rules
- BFF direct database coupling
- trusting unsigned identity headers
- decoding JWT without validation
- trusting arbitrary forwarded headers
- permissive CORS without analysis
- per-pod rate limiting when global quota is required
- unlimited request/header size
- unrestricted WAF bypass
- long independent timeout at every layer
- automatic retry of non-idempotent writes
- retry multiplication
- unbounded gateway queue
- direct public actuator exposure
- complex payload transformation in gateway scripts
- raw infrastructure errors returned externally
- personalized response cached under global key
- access token in logs
- request/response body logging by default
- metric cardinality based on IDs
- canary without rollback criteria
- deployment requiring modification of an already-applied Flyway migration

---

# 245. Positive Consequences

The decision provides:

- consistent external API exposure
- reduced microservice attack surface
- centralized traffic protection
- standardized authentication enforcement
- safer header propagation
- controlled rate limiting
- bounded edge resource consumption
- safer deployment traffic shifting
- better observability
- reduced client/service coupling
- improved defense in depth
- controlled failure behavior

---

# 246. Negative Consequences

The decision introduces:

- additional infrastructure
- another network hop
- gateway capacity planning
- route configuration
- certificate management
- rate-limit infrastructure
- additional failure modes
- more operational monitoring

These costs are accepted because uncontrolled public microservice exposure creates substantially larger security and operational risks.

---

# 247. Neutral Consequences

The decision also means:

- not every application requires a BFF
- not every service needs mTLS
- not every route needs the same rate limit
- gateway authentication does not remove service authorization
- gateway outage can affect many APIs
- canary routing does not solve database incompatibility
- WAF does not replace secure coding
- edge retries are intentionally limited
- direct database fallback is not an edge responsibility

---

# 248. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Gateway outage | Critical | Low/Medium | HA + autoscaling |
| Auth bypass | Critical | Low | Defense in depth |
| Header spoofing | Critical | Medium | Strip/rebuild trusted headers |
| Retry amplification | High | Medium | Retry budget |
| Rate-limit bypass | High | Medium | Distributed identity-based limits |
| Backend overload | Critical | Medium | Limits + bulkheads |
| WAF false positive | Medium/High | Medium | Monitor/tune |
| Client IP spoofing | High | Medium | Trusted proxies |
| Canary regression | High | Medium | Automated health gates |
| Sensitive logging | Critical | Medium | Logging controls |

---

# 249. Implementation Guidance

The following rules are mandatory:

1. Externally consumed microservices should use an approved edge architecture.
2. Internal services should not be publicly exposed without explicit justification.
3. Gateway responsibilities must remain infrastructure/traffic oriented.
4. Core business logic must remain outside gateway configuration.
5. BFFs may adapt/orchestrate but must not duplicate core domain rules.
6. BFFs must not directly access another service's persistence.
7. OAuth2/OIDC/JWT validation must verify signature, issuer, audience and temporal claims.
8. Gateway authentication must not replace resource-level authorization.
9. Trusted internal headers must be stripped from untrusted inbound requests before regeneration.
10. Access tokens must never be logged.
11. CORS must use explicit production policy.
12. Public APIs must evaluate appropriate rate limiting.
13. Global rate limits must account for multi-instance gateway deployment.
14. Request, header and URI sizes must be bounded.
15. WAF must complement rather than replace application security.
16. Forwarded network headers must only be trusted from approved proxies.
17. Backend bypass paths must not invalidate proxy trust assumptions.
18. Request paths must have explicit end-to-end timeout budgets.
19. Gateway retries must be bounded, transient-only and idempotency-aware.
20. Retry multiplication across layers must be prevented.
21. Gateway concurrency and queues must be bounded.
22. Critical and expensive traffic should be isolated where appropriate.
23. Readiness and graceful shutdown must support safe Kubernetes traffic management.
24. Canary/blue-green routing must use measurable promotion/rollback criteria.
25. Database migrations must remain compatible with overlapping deployment versions.
26. Applied Flyway migrations must never be modified; schema changes require new migration versions.
27. Error responses must preserve useful application semantics while hiding infrastructure details.
28. Edge caching must never leak personalized responses between users.
29. Access logging must exclude secrets and sensitive bodies by default.
30. Metrics must use bounded route templates rather than high-cardinality identifiers.
31. Gateway capacity, saturation, authentication failures, throttling and upstream failures must be monitored.
32. Edge security and failure behavior must have automated integration/security tests.

---

# 250. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway where applicable
- OAuth2 Resource Server
- Keycloak / Enterprise IdP
- Kubernetes
- Kubernetes Ingress / approved Gateway implementation
- AWS ALB/NLB where applicable
- AWS WAF where applicable
- TLS/mTLS
- Resilience4j where applicable
- Micrometer
- OpenTelemetry where enabled
- Testcontainers where applicable
- JUnit 5
- AssertJ
- OWASP API Security testing
- load testing
- failure injection
- canary deployment exercises
- CI/CD security gates

---

# 251. Success Criteria

The decision is successful when:

- internal services cannot be unintentionally accessed from the internet
- edge authentication is consistently enforced
- fine-grained service authorization remains intact
- trusted headers cannot be spoofed by clients
- rate limits protect downstream capacity
- request-size abuse is rejected early
- forwarded IP metadata cannot be trivially forged
- retries cannot duplicate unsafe business operations
- timeout budgets prevent runaway latency
- gateway failures are observable
- canary releases can be automatically stopped or rolled back
- database evolution remains compatible with rolling deployment
- sensitive request information does not leak through logs/metrics
- edge capacity can be measured and load-tested

---

# 252. Alternatives Rejected

## 252.1 Expose Every Microservice Directly

Rejected because it increases attack surface, client coupling and duplicated traffic/security controls.

---

## 252.2 Put Business Logic in Gateway

Rejected because infrastructure configuration becomes an untestable distributed business monolith.

---

## 252.3 Trust Gateway Completely

Rejected because bypass, misconfiguration and internal threats require defense in depth.

---

## 252.4 Retry Every Failed Request at Edge

Rejected because non-idempotent operations may be duplicated and retries can amplify outages.

---

## 252.5 Trust All Forwarded Headers

Rejected because internet clients can spoof network and identity metadata.

---

## 252.6 Use Per-Pod Limits for Global Contractual Quotas

Rejected because autoscaling changes the effective global quota.

---

## 252.7 Unlimited Gateway Request Size

Rejected because malicious/accidental large payloads can exhaust edge and backend resources.

---

# 253. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise Authentication, Authorization, OAuth2, OIDC and JWT Security Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-065: Enterprise WebClient, HTTP Client and External Integration Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-078: Enterprise File Processing, Upload, Download, Streaming and Large File Handling Standard
- ADR-080: Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard
- ADR-081: Enterprise Search, Indexing, OpenSearch/Elasticsearch, Full-Text Search and Read Model Standard

---

# 254. References

- OAuth 2.0
- OpenID Connect
- RFC 7519 — JSON Web Token
- RFC 9110 — HTTP Semantics
- RFC 7239 — Forwarded HTTP Extension
- W3C Trace Context
- OWASP API Security Top 10
- OWASP Authentication Cheat Sheet
- OWASP Authorization Cheat Sheet
- OWASP Transport Layer Security Cheat Sheet
- OWASP REST Security Cheat Sheet
- Spring Security Documentation
- Spring Cloud Gateway Documentation
- Kubernetes Documentation
- AWS WAF Documentation
- AWS Elastic Load Balancing Documentation
- Keycloak Documentation
- Google Site Reliability Engineering

---

# 255. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | AstraForge Supply Platform Architecture Team | Approved | Initial enterprise API edge and traffic-management baseline |

---

# 256. Decision Summary

The exposure model becomes:

```text
CLIENT
  |
  v
WAF / EDGE
  |
  v
API GATEWAY
  |
  v
BFF / API
  |
  v
MICROSERVICES
```

instead of:

```text
CLIENT
 |
 +--> CART
 +--> ORDERS
 +--> CUSTOMERS
 +--> PRODUCTS
 +--> USERS
```

Security becomes:

```text
TOKEN
  |
  v
SIGNATURE
  |
  v
ISSUER
  |
  v
AUDIENCE
  |
  v
EXPIRATION
  |
  v
COARSE AUTHORIZATION
  |
  v
SERVICE
  |
  v
RESOURCE AUTHORIZATION
```

Trusted headers become:

```text
UNTRUSTED CLIENT HEADER
        |
        X
      STRIP
        |
        v
AUTHENTICATED IDENTITY
        |
        v
TRUSTED INTERNAL CONTEXT
```

Traffic protection becomes:

```text
REQUEST
   |
   v
WAF
   |
   v
SIZE LIMIT
   |
   v
RATE LIMIT
   |
   v
CONCURRENCY LIMIT
   |
   v
ROUTE
```

Timeout management becomes:

```text
END-TO-END DEADLINE
       |
       +--> EDGE BUDGET
       |
       +--> BFF BUDGET
       |
       +--> SERVICE BUDGET
       |
       +--> DOWNSTREAM BUDGET
```

Retry becomes:

```text
FAILURE
   |
   v
TRANSIENT?
   |
 +---+---+
 |       |
NO      YES
 |       |
FAIL   IDEMPOTENT?
         |
       +-+-+
       |   |
      NO  YES
       |   |
     FAIL BOUNDED
          RETRY
```

Canary delivery becomes:

```text
PRODUCTION TRAFFIC
       |
       +--> 95% CURRENT
       |
       +--> 5% CANDIDATE
                |
                v
         HEALTH METRICS
                |
           +----+----+
           |         |
         GOOD       BAD
           |         |
           v         v
       INCREASE   ROLLBACK
```

The complete edge equation is:

```text
PRIVATE MICROSERVICES
        +
CONTROLLED EDGE
        +
TLS
        +
VALIDATED IDENTITY
        +
RESOURCE AUTHORIZATION
        +
SAFE HEADER PROPAGATION
        +
CORS GOVERNANCE
        +
WAF
        +
RATE LIMITING
        +
BOUNDED REQUEST SIZE
        +
TRUSTED PROXY MODEL
        +
TIMEOUT BUDGET
        +
IDEMPOTENCY-AWARE RETRY
        +
BOUNDED CONCURRENCY
        +
GRACEFUL DRAINING
        +
CANARY / BLUE-GREEN
        +
SAFE ERROR CONTRACTS
        +
LOW-CARDINALITY METRICS
        +
SECURE LOGGING
        =
SAFE ENTERPRISE API EDGE
```

The governing principle is:

```text
Do not expose
every microservice
to the internet.

Put a controlled boundary
in front of the platform.

But do not move
the business platform
into that boundary.

The gateway routes.

The gateway protects.

The gateway limits.

The gateway observes.

The BFF adapts.

The domain service
owns business behavior.

Validate tokens.

Do not merely decode them.

Validate signature.

Validate issuer.

Validate audience.

Validate expiration.

Do not assume
gateway authentication
equals business authorization.

Strip untrusted
identity headers.

Never trust
X-Forwarded-For
from arbitrary clients.

Know your trusted proxies.

Bound request bodies.

Bound headers.

Bound URLs.

Bound connections.

Bound concurrency.

Bound timeouts.

Bound retries.

Do not multiply retries
across every layer.

Never blindly retry
a business POST.

Rate-limit intentionally.

Remember that
ten pods with local limits
may mean ten times
the intended traffic.

Use WAF as defense in depth,
not as a substitute
for secure code.

Do not expose actuator,
admin or debug endpoints
through broad routes.

Keep access tokens
out of logs.

Keep IDs
out of metric dimensions.

Use route templates
for observability.

Drain traffic
during shutdown.

Route only to
ready instances.

Canary with metrics.

Rollback with evidence.

Keep database evolution
compatible with overlapping
application versions.

Never modify an already
applied Flyway migration.

Use a new migration.

And remember:

the API gateway is
the front door
of the platform.

A strong front door
does not justify
removing the locks
from every room behind it.
```
