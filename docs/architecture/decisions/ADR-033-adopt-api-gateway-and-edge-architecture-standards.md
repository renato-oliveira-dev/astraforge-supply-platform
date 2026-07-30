# ADR-033: Adopt API Gateway and Edge Architecture Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-033 |
| Title | Adopt API Gateway and Edge Architecture Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | API Gateway, Edge Security, Routing, Rate Limiting, Authentication |
| Related Work Items | Gateway, JWT, Keycloak, CORS, Routing, Rate Limiting, Correlation |
| Supersedes | None |
| Superseded By | ADR-073 |

---

# 1. Context

The AstraForge Supply Platform consists of independently deployable services exposed to:

- web applications
- mobile applications
- internal enterprise applications
- partner systems
- integration clients
- administrative applications

Direct exposure of every microservice to external networks creates unnecessary complexity around:

- TLS
- authentication
- routing
- CORS
- rate limiting
- request limits
- observability
- attack-surface management
- API lifecycle

The platform therefore requires a controlled edge architecture.

Conceptually:

```text
CLIENT
   |
   v
EDGE / API GATEWAY
   |
   v
MICROSERVICES
```

However, introducing a gateway creates another architectural risk:

```text
Putting every cross-cutting concern
and business rule into the gateway.
```

This eventually produces a centralized distributed monolith.

---

# 2. Problem Statement

The platform requires standards defining:

- API Gateway responsibilities
- service responsibilities
- TLS termination
- authentication
- authorization
- JWT validation
- Keycloak integration
- routing
- CORS
- rate limiting
- throttling
- request-size limits
- response-size considerations
- headers
- trusted proxies
- correlation IDs
- trace propagation
- timeout budgets
- retries
- gateway availability
- health checks
- API versioning
- BFF
- aggregation
- WebSocket
- SSE
- observability
- security
- privacy

---

# 3. Decision Drivers

Primary drivers are:

1. security
2. clear responsibility boundaries
3. horizontal scalability
4. centralized edge policy
5. independent service evolution
6. observability
7. resilience
8. low latency
9. maintainability
10. API governance
11. operational simplicity
12. defense in depth

---

# 4. Decision

The AstraForge Supply Platform adopts:

```text
CLIENT
   |
   v
EDGE
   |
   +-- TLS
   +-- Authentication Enforcement
   +-- Routing
   +-- CORS
   +-- Rate Limiting
   +-- Request Limits
   +-- Correlation
   +-- Edge Observability
   |
   v
SERVICE
   |
   +-- Authorization
   +-- Domain Validation
   +-- Business Rules
   +-- Persistence
   +-- Workflow
   +-- Integration
```

The gateway is infrastructure.

It is not the business domain.

---

# 5. Fundamental Principle

The primary architectural rule is:

```text
Gateway protects and routes.

Service decides and executes.
```

---

# 6. Edge Responsibilities

The edge may own:

- TLS termination
- route resolution
- host/path routing
- authentication enforcement
- basic token validation
- CORS
- coarse rate limiting
- request-size limits
- standard security headers
- correlation propagation
- trace propagation
- edge metrics
- access logging

---

# 7. Service Responsibilities

Services own:

- business authorization
- domain validation
- workflow rules
- resource ownership
- persistence
- business invariants
- integration semantics
- state transitions

---

# 8. Authentication

Authentication answers:

```text
Who is calling?
```

---

# 9. Authorization

Authorization answers:

```text
May this caller perform this operation
on this resource in the current state?
```

---

# 10. Authentication at Edge

The gateway may reject obviously unauthenticated requests before they reach services.

---

# 11. Service Authentication Validation

Services must not blindly trust arbitrary identity headers supplied by external callers.

---

# 12. Defense in Depth

Where JWT-based security is used, services should validate the security context according to the platform trust model.

---

# 13. Keycloak

Keycloak remains the approved identity provider where applicable.

---

# 14. JWT

JWT access tokens may carry claims such as:

```text
sub

iss

aud

exp

roles

scope
```

according to identity-platform configuration.

---

# 15. JWT Signature

Token signatures must be validated against trusted issuer keys.

---

# 16. Issuer

The expected issuer must be validated.

---

# 17. Audience

Audience validation must be used where the identity architecture defines service/API audiences.

---

# 18. Expiration

Expired tokens must be rejected.

---

# 19. Token Logging

Complete JWT tokens must never be logged.

---

# 20. Authorization Ownership

Business authorization belongs to the service.

Example:

```text
User Profile = SUPERVISOR

Order Segment = Forge & Field

Order Status = PENDING_SUPERVISOR
```

Whether that user may approve that order is a business decision.

It must not be encoded only in gateway routing rules.

---

# 21. Gateway Role Checks

The gateway may apply coarse access policies such as:

```text
Authenticated users only
```

or:

```text
Administrative route requires administrative scope
```

when centrally governed.

---

# 22. Fine-Grained Authorization

Fine-grained authorization remains inside the service.

---

# 23. Resource Ownership

Rules such as:

```text
Caller may modify only orders belonging
to the caller's company
```

must be enforced by the owning service.

---

# 24. Trust Boundary

The gateway forms part of the platform trust boundary.

---

# 25. Direct Service Access

Network architecture should prevent unauthorized external clients from bypassing the gateway where gateway enforcement is required.

---

# 26. Internal Calls

Internal service-to-service calls require their own trust and authentication model.

---

# 27. Gateway Is Not Authentication Source

The gateway does not issue identity truth unless explicitly acting as part of the approved identity infrastructure.

---

# 28. Identity Headers

Headers representing authenticated identity must only be accepted from trusted infrastructure.

---

# 29. Header Spoofing

External callers must not be able to inject trusted internal headers such as:

```text
X-User-Id

X-User-Role

X-Company-Id
```

and thereby impersonate another identity.

---

# 30. Header Sanitization

The edge should remove or overwrite protected internal identity headers received from untrusted clients.

---

# 31. Routing

Routing must be declarative and infrastructure-oriented.

---

# 32. Route Example

Conceptually:

```text
/api/orders/**
        ↓
orders-service
```

---

# 33. Business Routing

The gateway must not inspect detailed business state to determine workflow execution.

---

# 34. Service Discovery

Gateway routing may integrate with the platform's approved Kubernetes/service-discovery mechanisms.

---

# 35. Hardcoded Instances

Routes must not depend on individual pod addresses.

---

# 36. Load Balancing

Traffic should be distributed across healthy service instances using platform-supported load balancing.

---

# 37. TLS

External traffic must use TLS.

---

# 38. TLS Termination

TLS may terminate at:

- load balancer
- ingress
- API gateway

according to infrastructure architecture.

---

# 39. Internal Encryption

Internal TLS/mTLS requirements follow enterprise security standards.

---

# 40. TLS Downgrade

The edge must not permit insecure protocol downgrade contrary to platform policy.

---

# 41. CORS

CORS is an edge/browser security policy.

---

# 42. Explicit Origins

Production CORS configuration must use explicitly approved origins.

---

# 43. Wildcard Origin

Using:

```text
Access-Control-Allow-Origin: *
```

with sensitive authenticated APIs is strongly discouraged and may be invalid with credentialed requests.

---

# 44. Allowed Methods

CORS should expose only required HTTP methods.

---

# 45. Allowed Headers

Allowed request headers should be explicitly governed.

---

# 46. Exposed Headers

Only required response headers should be exposed to browser clients.

---

# 47. Preflight

OPTIONS/preflight behavior must not trigger domain processing.

---

# 48. Rate Limiting

The gateway should provide coarse-grained rate limiting for externally exposed APIs where appropriate.

---

# 49. Rate-Limit Dimensions

Possible dimensions include:

- client
- API credential
- tenant
- user
- route
- source network

subject to privacy and cardinality constraints.

---

# 50. Global IP Limiting

IP address alone must not be assumed to uniquely represent a user.

NAT and corporate proxies may aggregate many users behind one address.

---

# 51. Rate Limit Purpose

Rate limiting protects:

- platform capacity
- downstream services
- expensive endpoints
- abuse-sensitive operations

---

# 52. Rate Limit Is Not Authorization

Passing a rate limit does not authorize an operation.

---

# 53. Rate Limit Response

HTTP:

```text
429 Too Many Requests
```

should be used for rate-limit rejection.

---

# 54. Retry-After

`Retry-After` should be returned where the policy can provide meaningful guidance.

---

# 55. Distributed Rate Limit

Rate limiting across multiple gateway instances requires shared/distributed semantics when a globally consistent quota is required.

---

# 56. Local Rate Limit

Per-instance limiting may be sufficient for some coarse infrastructure protections.

---

# 57. Rate Limit Failure

Failure of a distributed rate-limit backend requires an explicit:

```text
fail-open
```

or:

```text
fail-closed
```

policy according to endpoint risk.

---

# 58. Security-Sensitive Endpoint

High-risk endpoints may require fail-closed behavior.

---

# 59. Availability-Sensitive Endpoint

Low-risk availability-critical endpoints may justify controlled fail-open behavior.

---

# 60. Request Size

The edge must enforce bounded request size.

---

# 61. Why

Unbounded request bodies can consume:

- network bandwidth
- gateway memory
- application memory
- CPU
- downstream capacity

---

# 62. Upload Endpoints

Large-file upload endpoints require dedicated size and streaming policies.

---

# 63. Base64

Embedding large binary files as Base64 JSON is discouraged when dedicated upload/object-storage patterns are available.

---

# 64. Header Size

HTTP header size must also remain bounded.

---

# 65. Oversized JWT

Excessively large tokens increase request overhead across every hop.

---

# 66. Response Size

Large API responses should be prevented through:

- pagination
- projection
- bounded collections
- streaming where appropriate

---

# 67. Gateway Transformation

The gateway must not routinely perform complex domain response transformation.

---

# 68. Protocol Adaptation

Simple infrastructure-level protocol adaptation may be acceptable.

---

# 69. Business Transformation

Business-specific transformation belongs in:

- service
- dedicated BFF
- integration adapter

depending on ownership.

---

# 70. Timeout Budget

Gateway timeouts must align with end-to-end SLOs.

---

# 71. Timeout Hierarchy

Timeouts should normally decrease inward.

Conceptually:

```text
Client Timeout
      >
Gateway Timeout
      >
Service Operation Budget
      >
Downstream Timeout
```

---

# 72. Example

Avoid:

```text
Client timeout       = 10s
Gateway timeout      = 60s
Service timeout      = 30s
```

because outer layers may abandon requests while inner processing continues.

---

# 73. Gateway Timeout

Gateway timeout must be finite.

---

# 74. Infinite Wait

Infinite edge request timeouts are prohibited for ordinary APIs.

---

# 75. HTTP 504

Gateway timeout conditions should normally surface as:

```text
504 Gateway Timeout
```

when the gateway timed out waiting for an upstream response.

---

# 76. HTTP 502

Invalid/unavailable upstream responses may surface as:

```text
502 Bad Gateway
```

depending on infrastructure behavior.

---

# 77. HTTP 503

Temporary service unavailability may surface as:

```text
503 Service Unavailable
```

---

# 78. Status Semantics

Gateway status codes must preserve useful distinction between:

- client failure
- authentication failure
- authorization failure
- rate limit
- upstream failure
- timeout

---

# 79. Retry

The gateway must not indiscriminately retry every failed request.

---

# 80. Unsafe Retry

Automatically retrying:

```text
POST /orders
```

may create duplicate business operations.

---

# 81. Retry Eligibility

Retries require:

- idempotency
- transient failure classification
- bounded attempts
- timeout budget

---

# 82. GET Retry

Even GET retries must be bounded because retry amplification can overload downstream services.

---

# 83. Retry Multiplication

Avoid:

```text
Client retries 3×

Gateway retries 3×

Service retries 3×
```

which can result in up to:

```text
27 downstream attempts
```

for one logical operation.

---

# 84. Retry Ownership

Retry responsibility should be assigned deliberately to the layer with sufficient context.

---

# 85. Idempotency Key

Mutation endpoints that support safe client retries may use an idempotency-key mechanism.

---

# 86. Idempotency Ownership

The gateway may propagate an idempotency key.

The owning service must implement business idempotency when required.

---

# 87. Correlation ID

Every request should have a correlation identifier.

---

# 88. Existing Correlation

A valid trusted correlation ID may be propagated.

---

# 89. Missing Correlation

If absent, the edge or first trusted service boundary may generate one.

---

# 90. Untrusted Correlation

Untrusted external correlation values must be validated and bounded before propagation/logging.

---

# 91. Correlation Header

The platform should standardize the correlation header name.

---

# 92. Trace Context

W3C Trace Context should be propagated where distributed tracing is enabled.

---

# 93. Correlation vs Trace

Correlation ID and trace ID are related but not necessarily identical concepts.

---

# 94. Business Identifier

Order ID, customer ID and correlation ID must not be conflated.

---

# 95. Header Propagation

Only required headers should be propagated.

---

# 96. Hop-by-Hop Headers

Hop-by-hop HTTP headers must not be incorrectly forwarded.

---

# 97. Authorization Header

Authorization credentials must only be propagated according to the approved service authentication model.

---

# 98. Cookie Propagation

Cookies must not be blindly propagated to backend services.

---

# 99. Trusted Proxy Headers

Headers such as:

```text
X-Forwarded-For
X-Forwarded-Proto
X-Forwarded-Host
Forwarded
```

must be trusted only when supplied by known proxy infrastructure.

---

# 100. Client IP

Applications must not blindly trust client-supplied `X-Forwarded-For`.

---

# 101. Proxy Chain

The infrastructure must define which proxy in the chain is authoritative for client-network information.

---

# 102. Host Header

Host-header handling must prevent spoofing and routing confusion.

---

# 103. Security Headers

The edge may centrally apply appropriate HTTP security headers.

---

# 104. HSTS

HSTS may be applied for HTTPS-only public domains according to security policy.

---

# 105. Browser Security

Browser-specific security headers must be appropriate to the actual client/application architecture.

---

# 106. API Versioning

API compatibility is governed explicitly.

---

# 107. Versioning Strategy

The platform may use:

```text
URI versioning

Header versioning

Media-type versioning
```

but each API family should use a consistent approach.

---

# 108. Version Routing

The gateway may route API versions.

---

# 109. Version Semantics

The gateway must not contain the business compatibility implementation of each version.

---

# 110. Breaking Change

Breaking API changes require explicit versioning/deprecation strategy.

---

# 111. Additive Change

Backward-compatible additive changes should generally avoid unnecessary major versions.

---

# 112. Deprecation

Deprecated versions require:

- owner
- consumer inventory
- migration plan
- removal date where possible

---

# 113. Version Explosion

Maintaining unlimited API versions is prohibited.

---

# 114. BFF

A Backend for Frontend may be used when a specific client requires materially different orchestration or representation.

---

# 115. BFF Ownership

BFFs are client-experience adapters.

They are not universal business-domain owners.

---

# 116. BFF Example

```text
WEB APP
   |
   v
WEB BFF
   |
   +--> Orders
   +--> Customers
   +--> Products
```

may be appropriate for UI-oriented aggregation.

---

# 117. Gateway vs BFF

The API Gateway and BFF have different responsibilities.

```text
Gateway
=
Edge Infrastructure

BFF
=
Client-Specific Backend
```

---

# 118. Gateway Aggregation

The central gateway should not become the default location for multi-service business aggregation.

---

# 119. Aggregation Risk

Central aggregation creates:

- service coupling
- release coupling
- complex timeout handling
- domain leakage
- gateway scaling pressure

---

# 120. GraphQL

If GraphQL is introduced, it requires its own architectural governance for:

- query complexity
- authorization
- N+1
- schema ownership
- federation

It is not implicitly provided by this ADR.

---

# 121. WebSocket

WebSocket traffic may pass through the gateway when supported.

---

# 122. WebSocket Capacity

Long-lived connections require independent capacity planning from ordinary HTTP request traffic.

---

# 123. WebSocket Timeout

Idle/maximum connection policies must be explicit.

---

# 124. SSE

Server-Sent Events may be supported for one-way streaming use cases.

---

# 125. SSE Capacity

SSE also creates long-lived connections and requires capacity planning.

---

# 126. Streaming

Gateway buffering must not accidentally defeat streaming semantics.

---

# 127. Compression

HTTP compression may be applied where payload characteristics justify it.

---

# 128. Compression Cost

Compression trades network bandwidth for CPU.

---

# 129. Sensitive Compression

Compression behavior for sensitive authenticated content should follow security guidance because some side-channel scenarios can be affected by compression.

---

# 130. Caching at Edge

HTTP edge caching may be used only for responses whose semantics explicitly permit it.

---

# 131. Private Data

Authenticated/private responses must not accidentally enter shared public caches.

---

# 132. Cache-Control

Services should provide appropriate cache semantics through standard HTTP headers where applicable.

---

# 133. Gateway Cache

The gateway must not independently cache dynamic business responses without an explicit consistency decision.

---

# 134. ADR-032

Application/distributed cache behavior follows ADR-032.

---

# 135. Gateway Availability

The gateway is a critical infrastructure component.

---

# 136. Single Gateway Instance

A single gateway instance is prohibited for production high-availability architecture.

---

# 137. Horizontal Scaling

Gateway instances must support horizontal scaling.

---

# 138. Statelessness

Gateway request processing should remain stateless where practical.

---

# 139. Session Affinity

Sticky sessions are discouraged unless a protocol/use case explicitly requires them.

---

# 140. Gateway Failure

The platform must tolerate loss of individual gateway instances.

---

# 141. Dependency Failure

The gateway must distinguish:

```text
Gateway itself unhealthy
```

from:

```text
Backend service unhealthy
```

---

# 142. Health Checks

Gateway health checks should verify gateway process readiness without synchronously requiring every backend service to be healthy.

---

# 143. Cascading Readiness

Do not make gateway readiness fail merely because one unrelated downstream service is unavailable.

---

# 144. Routing Health

Traffic should not be routed to unhealthy gateway instances.

---

# 145. Backend Health

Backend health should be handled by service discovery/load-balancing infrastructure.

---

# 146. Circuit Breaker

Circuit breakers may be used at the edge for specific upstream protection where justified.

---

# 147. Duplicate Circuit Breakers

Avoid uncontrolled circuit-breaker stacking across:

```text
Gateway
+
Service
+
HTTP Client
```

without clearly defined ownership.

---

# 148. Business Fallback

The gateway must not invent business fallback values.

---

# 149. Error Contract

Gateway-generated errors should follow a standardized infrastructure error representation.

---

# 150. Service Error

Valid domain errors returned by services should not be converted into generic gateway failures.

---

# 151. Error Preservation

Preserve meaningful HTTP semantics when safe.

---

# 152. Internal Details

Gateway error responses must not expose:

- stack traces
- internal hostnames
- pod names
- framework internals
- credentials

---

# 153. Error Correlation

Error responses should include a safe correlation identifier where appropriate.

---

# 154. Observability

Gateway traffic must be observable.

---

# 155. Required Metrics

Monitor at least:

```text
Request Rate

Response Status

Latency

Active Requests

Upstream Latency

Gateway Errors

Rate-Limit Rejections

Authentication Rejections

Request Size
```

---

# 156. RED Method

Gateway APIs should support:

```text
Rate

Errors

Duration
```

analysis.

---

# 157. Upstream Dimension

Metrics may identify upstream service using bounded service names.

---

# 158. Route Dimension

Stable route templates may be used as metric dimensions.

---

# 159. Raw URI

Raw request URI containing arbitrary IDs must not become a high-cardinality metric label.

---

# 160. User ID Metric

User IDs must not become metric labels.

---

# 161. Access Logging

Access logs should capture useful request metadata without sensitive payloads.

---

# 162. Recommended Fields

Examples:

```text
timestamp

method

route

status

durationMs

correlationId

traceId

upstreamService
```

---

# 163. Sensitive Query Parameters

Sensitive query parameters must be redacted or excluded.

---

# 164. Authorization Logging

Authorization headers must never be logged.

---

# 165. Cookies

Authentication/session cookies must not be logged.

---

# 166. Request Body

Request bodies must not be indiscriminately logged at the gateway.

---

# 167. Response Body

Response bodies must not be indiscriminately logged.

---

# 168. Sampling

High-volume edge tracing/logging may use controlled sampling according to observability standards.

---

# 169. Security Monitoring

The edge should expose signals for:

- authentication failures
- rate-limit violations
- suspicious request patterns
- malformed requests

without implementing a complete SIEM inside the gateway.

---

# 170. WAF

A Web Application Firewall may complement the gateway for externally exposed services.

---

# 171. WAF vs Service Validation

WAF protection does not replace service-side input validation.

---

# 172. Input Validation

The gateway may reject malformed protocol-level requests.

---

# 173. Domain Validation

Domain-specific validation remains in the service.

---

# 174. Example

Gateway may reject:

```text
Body > 10 MB
```

Service decides:

```text
Order cannot be approved because
its current status is CANCELLED.
```

---

# 175. Request Normalization

Protocol-level normalization may occur at the edge.

---

# 176. Business Data Mutation

The gateway must not silently alter business values to make downstream validation pass.

---

# 177. HTML Escaping

The gateway must not mutate domain strings such as:

```text
Forge & Field
```

into:

```text
M&amp;M
```

as a generic security strategy.

Context-appropriate encoding belongs at the output/rendering boundary where required.

---

# 178. Security Sanitization

Security controls must distinguish:

```text
Validation

Encoding

Sanitization

Normalization
```

These are not interchangeable operations.

---

# 179. Request ID

Infrastructure request IDs may coexist with business correlation IDs.

---

# 180. Trace Propagation

Gateway-generated traces should propagate downstream using standardized context.

---

# 181. Context Loss

Asynchronous processing beyond the HTTP request must explicitly propagate or recreate relevant correlation context.

---

# 182. Privacy

Gateway telemetry follows ADR-029.

---

# 183. Data Minimization

The edge should process/store only information required for edge responsibilities.

---

# 184. PII

PII must not be duplicated into gateway logs merely because all traffic passes through the gateway.

---

# 185. Secrets

Gateway secrets follow ADR-026.

---

# 186. Configuration

Routes and policies should be externally configurable according to platform configuration standards.

---

# 187. Configuration Validation

Invalid gateway configuration must fail deployment safely rather than silently exposing unintended routes.

---

# 188. Default Route

A permissive catch-all route to internal services is discouraged.

---

# 189. Route Allowlist

Externally exposed routes should be intentional.

---

# 190. Administrative Endpoints

Internal administrative/actuator endpoints must not be externally exposed by default.

---

# 191. Swagger/OpenAPI

API documentation exposure must follow environment/security policy.

---

# 192. Production API Documentation

If API documentation is exposed in production, access must be intentional and governed.

---

# 193. Actuator

Gateway/service actuator endpoints require separate access controls.

---

# 194. Kubernetes

The gateway should integrate naturally with Kubernetes deployment and service-discovery patterns.

---

# 195. Graceful Shutdown

Gateway instances must support graceful shutdown.

---

# 196. Drain

During termination:

```text
Stop New Traffic

↓

Complete / Bound Existing Requests

↓

Terminate
```

---

# 197. Rolling Deployment

Gateway deployments must support rolling upgrades without unnecessary interruption.

---

# 198. Configuration Compatibility

Route/policy changes must consider mixed old/new gateway instances during rolling deployment.

---

# 199. Canary

Canary routing may be used for controlled deployments.

---

# 200. Canary Routing

Canary selection must use stable infrastructure-level criteria.

---

# 201. Business Canary

Complex business rollout decisions should not be encoded ad hoc into gateway route scripts.

---

# 202. Feature Flags

Feature rollout follows dedicated feature-flag governance where applicable.

---

# 203. Blue/Green

Blue/green routing may be implemented at the edge/platform layer.

---

# 204. Rollback

Gateway configuration changes require rapid rollback capability.

---

# 205. DNS

DNS behavior and TTL must be considered during edge migrations.

---

# 206. Client Compatibility

Edge changes must account for client DNS and connection reuse behavior.

---

# 207. HTTP Version

HTTP/2 or later may be used where platform/client support permits.

---

# 208. Keep-Alive

Connection reuse should be enabled/configured appropriately.

---

# 209. Upstream Connection Pool

Gateway-to-service connection pools are finite resources.

---

# 210. Connection Budget

Gateway concurrency must not exceed downstream capacity merely because the gateway can accept more client connections.

---

# 211. Backpressure

The edge should reject or queue only within bounded limits when downstream capacity is exhausted.

---

# 212. Unbounded Queue

Unbounded request queues are prohibited.

---

# 213. Queueing Effect

Large queues often convert overload into:

```text
High latency

↓

Timeout

↓

Retry

↓

More overload
```

---

# 214. Load Shedding

Controlled load shedding is preferable to uncontrolled system collapse.

---

# 215. 503 During Overload

`503 Service Unavailable` may be appropriate when capacity is temporarily unavailable.

---

# 216. Priority

Traffic prioritization may be introduced where business-critical flows require differentiated capacity.

---

# 217. Priority Starvation

Priority systems must prevent indefinite starvation of lower-priority legitimate traffic.

---

# 218. External vs Internal Traffic

External and internal traffic may require different:

- authentication
- quotas
- routes
- network controls

---

# 219. Partner APIs

Partner-facing APIs require explicit:

- identity
- quotas
- versioning
- lifecycle
- support ownership

---

# 220. Machine-to-Machine

Machine clients should use appropriate machine identity rather than shared human credentials.

---

# 221. Client Credentials

OAuth client credentials may be used for approved service/partner scenarios.

---

# 222. Credential Rotation

Machine credentials must support rotation.

---

# 223. mTLS

mTLS may be used for high-trust machine integrations where required.

---

# 224. Gateway Plugins

Gateway plugins/extensions require governance.

---

# 225. Plugin Risk

Arbitrary custom gateway code increases:

- upgrade risk
- security risk
- operational complexity
- vendor coupling

---

# 226. Custom Plugin

Custom plugins should be introduced only when standard capabilities cannot satisfy the requirement cleanly.

---

# 227. Gateway Database

The gateway must not maintain a business-domain database.

---

# 228. Gateway Persistence

Limited infrastructure persistence may exist for:

- configuration
- rate-limit state
- operational metadata

depending on the gateway technology.

---

# 229. Business Queries

The gateway must not query the Orders database to decide whether an order may be approved.

---

# 230. Service Ownership

The Orders service owns Orders business decisions.

The Customers service owns Customers business decisions.

---

# 231. Gateway Deployment Independence

Changing an Orders business rule should not normally require a gateway deployment.

---

# 232. Organizational Scalability

The gateway must not become a central development bottleneck for every service team.

---

# 233. Policy Governance

Cross-cutting edge policies should be centrally governed but independently consumable by service teams.

---

# 234. Contract Testing

Gateway routes should have automated contract/smoke tests.

---

# 235. Routing Test

Verify:

```text
Public Route

↓

Correct Upstream Service
```

---

# 236. Authentication Test

Verify unauthenticated access is rejected where required.

---

# 237. Spoofed Header Test

Verify protected internal headers cannot be injected by external callers.

---

# 238. CORS Test

Verify only approved origins/methods/headers.

---

# 239. Rate-Limit Test

Verify threshold and recovery behavior.

---

# 240. Timeout Test

Verify upstream timeout behavior and status mapping.

---

# 241. Request-Size Test

Verify oversized payload rejection.

---

# 242. Correlation Test

Verify correlation/trace propagation.

---

# 243. Backend Failure Test

Verify one backend failure does not make unrelated routes unavailable.

---

# 244. Gateway Instance Failure Test

Verify traffic continues after loss of a gateway instance.

---

# 245. Security Test

Edge configuration must participate in security testing.

---

# 246. Performance Test

Gateway performance must be load tested independently and end-to-end.

---

# 247. Latency Budget

Gateway processing should consume only a controlled portion of end-to-end latency.

---

# 248. Gateway CPU

Expensive custom transformations/security scripts must not create excessive gateway CPU cost.

---

# 249. Gateway Memory

Large buffering and payload transformations must not create uncontrolled memory consumption.

---

# 250. Capacity Test

Capacity testing must include:

- requests/second
- concurrent requests
- connection count
- payload size
- TLS cost
- authentication cost
- upstream latency

---

# 251. Anti-Patterns

The following are prohibited or strongly discouraged:

- business rules in the central gateway
- treating authentication as complete authorization
- trusting client-supplied identity headers
- allowing external bypass of required gateway controls
- wildcard CORS for sensitive authenticated APIs
- rate limiting as a replacement for authorization
- unbounded request bodies
- unrestricted headers
- gateway business-data transformation
- infinite gateway timeouts
- indiscriminate retries
- retry multiplication across layers
- gateway-generated business fallback values
- raw URI IDs as metric labels
- user IDs as metric labels
- logging JWTs
- logging authorization headers
- indiscriminate body logging
- gateway readiness depending on every downstream service
- caching private responses without explicit policy
- business orchestration in the gateway
- using gateway as universal BFF
- business database access from gateway
- unbounded request queues
- arbitrary custom gateway plugins
- externally exposing actuator/admin endpoints by default
- permissive catch-all routes
- silently mutating business values
- trusting arbitrary `X-Forwarded-*` headers
- sticky sessions without explicit requirement
- one gateway instance in production
- custom gateway logic for every service feature

---

# 252. Positive Consequences

The decision provides:

- centralized edge security
- reduced external attack surface
- consistent routing
- standardized authentication enforcement
- controlled CORS
- rate limiting
- bounded requests
- better correlation
- improved edge observability
- clear business ownership
- independent service evolution
- improved availability

---

# 253. Negative Consequences

The decision introduces:

- critical gateway infrastructure
- additional capacity planning
- edge configuration governance
- more integration testing
- policy lifecycle management
- possible gateway vendor/platform dependency

These costs are accepted because controlled edge architecture substantially reduces duplicated external-facing infrastructure.

---

# 254. Neutral Consequences

The decision also means:

- services still enforce authorization
- services remain independently secured
- some clients may use dedicated BFFs
- not all retries belong at the gateway
- not all failures should be hidden by the gateway
- edge and service observability overlap intentionally
- gateway availability becomes part of platform SLO design

---

# 255. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Gateway becomes monolith | Critical | Medium | Strict responsibility boundary |
| Gateway outage affects all APIs | Critical | Low | HA and horizontal scaling |
| Header spoofing | Critical | Medium | Strip/overwrite trusted headers |
| Retry amplification | High | Medium | Central retry ownership |
| Rate-limit backend failure | High | Low | Explicit fail-open/fail-closed |
| CORS misconfiguration | High | Medium | Explicit origin allowlist |
| Sensitive logging | Critical | Medium | Redaction/minimization |
| Gateway latency | High | Medium | Performance budgets |
| Backend outage cascades | High | Medium | Isolation and bounded timeout |
| Unbounded queueing | Critical | Medium | Load shedding |
| Gateway business coupling | High | Medium | Service ownership enforcement |
| Incorrect proxy trust | High | Medium | Trusted proxy configuration |

---

# 256. Implementation Guidance

The following rules are mandatory:

1. The gateway is edge infrastructure, not the business domain.
2. External traffic must use TLS.
3. Authentication may be enforced at the edge.
4. Business authorization remains in the owning service.
5. Services must not blindly trust external identity headers.
6. Protected identity headers must be sanitized at the edge.
7. Routing must remain infrastructure-oriented.
8. Production CORS origins must be explicitly controlled.
9. Rate limiting must protect capacity without replacing authorization.
10. Request and header sizes must be bounded.
11. Gateway timeouts must be finite and aligned with end-to-end budgets.
12. Gateway retries require idempotency and bounded attempts.
13. Retry ownership must avoid multiplicative retries.
14. Correlation and trace context must be propagated.
15. Trusted proxy headers must only be accepted from trusted infrastructure.
16. Public sort/business semantics must not be implemented through gateway scripts.
17. API version routing may occur at the edge, but version business behavior remains downstream.
18. BFF and gateway responsibilities must remain distinct.
19. Gateway aggregation must not become default business orchestration.
20. Private responses must not be edge-cached without explicit policy.
21. Gateway instances must be horizontally scalable.
22. Gateway readiness must not depend on every downstream service.
23. Gateway errors must not expose internal implementation details.
24. Access logs must not contain credentials or unrestricted payloads.
25. Metrics must use bounded route/service dimensions.
26. Raw IDs must not become metric labels.
27. Administrative endpoints must not be publicly exposed by default.
28. Gateway request queues must remain bounded.
29. Load shedding must be preferred over uncontrolled collapse.
30. Business databases must not be accessed by the gateway.
31. Business-rule changes should not normally require gateway deployment.
32. Gateway routes/security policies require automated tests.
33. Gateway capacity requires independent and end-to-end load testing.
34. Rolling gateway deployments must preserve route/policy compatibility.
35. Business values must not be silently mutated at the edge.

---

# 257. Gateway Production Readiness Gate

An externally exposed API is not production ready until:

```text
[ ] Public route explicitly defined

[ ] TLS enabled

[ ] Authentication policy defined

[ ] Service authorization defined

[ ] Identity-header trust reviewed

[ ] Header spoofing protection tested

[ ] CORS policy defined

[ ] Rate-limit policy reviewed

[ ] Request-size limit defined

[ ] Header-size limit reviewed

[ ] Gateway timeout defined

[ ] Downstream timeout budget aligned

[ ] Retry ownership defined

[ ] Idempotency requirements reviewed

[ ] Correlation propagation verified

[ ] Trace propagation verified

[ ] Trusted proxy configuration reviewed

[ ] API versioning reviewed

[ ] Error mapping reviewed

[ ] Sensitive logging reviewed

[ ] Gateway metrics available

[ ] Upstream metrics available

[ ] High-cardinality metrics reviewed

[ ] Gateway HA verified

[ ] Backend isolation tested

[ ] Load shedding reviewed

[ ] Rolling deployment tested

[ ] Security tests completed

[ ] Performance tests completed
```

---

# 258. Validation

This ADR will be validated through:

- architecture reviews
- gateway configuration reviews
- route tests
- authentication tests
- authorization integration tests
- header spoofing tests
- CORS tests
- rate-limit tests
- timeout tests
- retry tests
- request-size tests
- security tests
- failure-injection tests
- load tests
- gateway metrics
- distributed tracing
- periodic edge-security reviews

---

# 259. Success Criteria

The decision is successful when:

- external APIs use a controlled entry point
- gateway failure of one instance does not interrupt service
- unauthenticated requests are rejected early
- business authorization remains service-owned
- identity-header spoofing is prevented
- CORS is explicit
- abusive traffic can be throttled
- oversized requests are rejected safely
- gateway retries do not create duplicate business operations
- downstream failures remain isolated
- correlation is preserved end-to-end
- gateway telemetry is actionable
- gateway changes do not become mandatory for routine domain changes
- gateway latency remains a small part of end-to-end latency

---

# 260. Alternatives Rejected

## 260.1 Expose Every Microservice Directly

Rejected because it duplicates edge security and increases external attack surface.

---

## 260.2 Put All Authorization in Gateway

Rejected because the gateway lacks sufficient domain state and would centralize business logic.

---

## 260.3 Put Business Orchestration in Gateway

Rejected because it creates a centralized distributed monolith.

---

## 260.4 Retry Every Failure at Gateway

Rejected because non-idempotent operations can duplicate state and retries amplify load.

---

## 260.5 Trust Forwarded Identity Headers from Any Client

Rejected because it enables identity spoofing.

---

## 260.6 Use Gateway as Universal BFF

Rejected because client-specific orchestration and edge infrastructure are different responsibilities.

---

## 260.7 Make Gateway Readiness Depend on All Services

Rejected because one backend failure would remove healthy routes from service.

---

# 261. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-009: Use Apache Kafka for Integration Events
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-034: Adopt Concurrency and Parallelism Standards

---

# 262. References

- OAuth 2.0
- OpenID Connect
- JWT
- W3C Trace Context
- OWASP API Security
- Keycloak Documentation
- Kubernetes Documentation
- Spring Security Documentation
- Resilience4j Documentation
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards

---

# 263. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial API Gateway and edge architecture baseline |

---

# 264. Decision Summary

The definitive responsibility model is:

```text
                       INTERNET
                          |
                          v
                +-------------------+
                |    API GATEWAY    |
                +-------------------+
                | TLS               |
                | Authentication    |
                | Routing           |
                | CORS              |
                | Rate Limiting     |
                | Request Limits    |
                | Correlation       |
                | Edge Telemetry    |
                +-------------------+
                          |
             +------------+------------+
             |            |            |
             v            v            v
         ORDERS       CUSTOMERS     PRODUCTS
         SERVICE       SERVICE       SERVICE
             |            |            |
             v            v            v
       Authorization Authorization Authorization
       Validation    Validation    Validation
       Business      Business      Business
       Persistence   Persistence   Persistence
```

Authentication and authorization remain distinct:

```text
JWT valid?
    |
   YES
    |
    v
AUTHENTICATED
    |
    v
May this user approve
this specific order?
    |
    v
SERVICE / DOMAIN RULE
```

The gateway must never make this architectural leap:

```text
JWT role = SUPERVISOR
       |
       v
Therefore every order
can be approved.
```

Instead:

```text
Authenticated User
       |
       v
Orders Service
       |
       +--> User Profile
       +--> Segment
       +--> Order Type
       +--> Current Status
       +--> Customer
       +--> Workflow Rules
       |
       v
AUTHORIZATION DECISION
```

Timeouts follow an end-to-end budget:

```text
CLIENT
  |
  | 10s
  v
GATEWAY
  |
  | 8s
  v
SERVICE
  |
  | 5s
  v
DEPENDENCY
```

Retries must avoid amplification:

```text
BAD

Client × 3
   |
Gateway × 3
   |
Service × 3
   |
   v
27 attempts
```

Prefer explicit ownership:

```text
Logical Request
      |
      v
Layer with enough context
decides whether retry is safe
      |
      v
Bounded retry
```

Overload behavior follows:

```text
TRAFFIC
   |
   v
RATE LIMIT
   |
   v
BOUNDED CONCURRENCY
   |
   v
DOWNSTREAM CAPACITY
   |
   +---- CAPACITY AVAILABLE ---> PROCESS
   |
   +---- SATURATED ------------> LOAD SHED
                                      |
                                      v
                                     429
                                      or
                                     503
```

The gateway must not become:

```text
              GIANT GATEWAY
                    |
        +-----------+-----------+
        |           |           |
        v           v           v
     Orders      Customers    Products
      Rules         Rules       Rules
        |           |           |
        +-----------+-----------+
                    |
                    v
            CENTRALIZED MONOLITH
```

The correct architecture is:

```text
Gateway owns edge concerns.

Services own domain concerns.

Identity provider owns identity.

Each service owns its data.

Observability connects the flow.
```

The definitive principle is:

```text
The API Gateway is the front door.

It is not the house.
```
