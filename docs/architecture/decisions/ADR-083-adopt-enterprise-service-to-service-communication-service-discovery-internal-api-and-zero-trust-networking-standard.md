# ADR-083: Adopt Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-083 |
| Title | Adopt Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Microservices, Internal APIs, Networking, Security, Resilience |
| Related Work Items | REST, WebClient, OAuth2, JWT, Kubernetes, NetworkPolicy, Circuit Breaker |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

A microservice architecture creates distributed communication between independently deployed applications.

A typical business operation may involve:

```text
CART
 |
 +--> CUSTOMERS
 |
 +--> PRODUCTS
 |
 +--> PARAMETERS
 |
 +--> ORDERS
 |
 +--> WORKFLOWS
```

Each dependency introduces:

```text
NETWORK LATENCY

TIMEOUT

PARTIAL FAILURE

AUTHENTICATION

AUTHORIZATION

CONNECTION MANAGEMENT

RETRY

SERVICE DISCOVERY

VERSION COMPATIBILITY

OBSERVABILITY
```

A local Java method call:

```java
customerService.findCustomer(id);
```

may become:

```text
APPLICATION
    |
    v
DNS
    |
    v
NETWORK
    |
    v
LOAD BALANCER
    |
    v
REMOTE SERVICE
    |
    v
DATABASE
```

The architectural consequences are materially different.

---

# 2. Problem Statement

The organization requires standards covering:

- synchronous service communication
- asynchronous communication
- REST
- messaging
- service discovery
- Kubernetes DNS
- internal APIs
- service identity
- OAuth2 Client Credentials
- JWT
- user-token propagation
- token exchange
- mTLS
- zero-trust networking
- NetworkPolicy
- timeout budgets
- retries
- Circuit Breaker
- bulkheads
- connection pools
- Virtual Threads
- fan-out
- cascading calls
- dependency graphs
- API contracts
- observability
- failure testing

---

# 3. Decision Drivers

Primary drivers are:

1. reliability
2. security
3. service autonomy
4. predictable latency
5. fault isolation
6. scalability
7. contract governance
8. observability
9. maintainability
10. least privilege
11. failure containment
12. operational simplicity

---

# 4. Decision

Service-to-service communication MUST deliberately select between:

```text
SYNCHRONOUS REQUEST/RESPONSE
```

and:

```text
ASYNCHRONOUS EVENT/MESSAGE
```

according to business semantics.

The platform MUST NOT default every integration to synchronous REST.

---

# 5. Fundamental Principle

```text
A remote call
is not a method call.

It is a distributed operation
with latency,
partial failure,
security,
capacity,
and consistency consequences.
```

---

# 6. Synchronous Communication

Synchronous communication SHOULD be used when the caller requires the result before it can continue.

Example:

```text
CART
 |
 v
PRODUCTS
 |
 v
PRICE
 |
 v
CONTINUE CHECKOUT
```

---

# 7. Asynchronous Communication

Asynchronous communication SHOULD be preferred when immediate response coupling is unnecessary.

Example:

```text
ORDER CREATED
      |
      v
EVENT
      |
 +----+----+
 |         |
AUDIT   NOTIFICATION
```

---

# 8. Decision Question

Before creating a synchronous dependency, ask:

```text
Does the caller genuinely
need the result now?
```

If not, asynchronous communication SHOULD be considered.

---

# 9. Temporal Coupling

Synchronous communication creates temporal coupling.

Both applications generally need to be available simultaneously.

---

# 10. Availability Multiplication

A request depending synchronously on several services has lower aggregate availability than any individual service.

Conceptually:

```text
A
 |
 +--> B
 |
 +--> C
 |
 +--> D
```

requires several systems to succeed during the same request.

---

# 11. Long Call Chain

Long synchronous dependency chains SHOULD be avoided.

---

# 12. Example

Avoid:

```text
CLIENT
 |
 v
SERVICE A
 |
 v
SERVICE B
 |
 v
SERVICE C
 |
 v
SERVICE D
 |
 v
SERVICE E
```

for ordinary request processing.

---

# 13. Cascading Latency

End-to-end latency includes every synchronous dependency.

---

# 14. Cascading Failure

A slow downstream service can consume resources throughout the upstream chain.

---

# 15. Internal API

Internal APIs MUST still be treated as formal contracts.

---

# 16. Internal Does Not Mean Informal

This reasoning is prohibited:

```text
"It's internal,
so we can change it anytime."
```

---

# 17. Independent Deployment

If two services deploy independently, their integration contract requires compatibility management.

---

# 18. API Ownership

Every internal API MUST have an owning service.

---

# 19. Database Ownership

Services MUST NOT bypass APIs/events by querying another service's database.

---

# 20. Cross-Service SQL

This is prohibited:

```text
ORDER SERVICE
      |
      v
CUSTOMER DATABASE
```

when the Customer Service owns that data.

---

# 21. Shared Database

Shared tables across independently owned microservices SHOULD be avoided.

---

# 22. Service Autonomy

A service owns:

```text
Business Rules

Persistence

Schema

API Contracts

Domain Events
```

for its bounded context.

---

# 23. Service Discovery

Services MUST use approved service discovery.

In Kubernetes, the default SHOULD be Kubernetes Service DNS.

---

# 24. Example

Prefer logical service addressing:

```text
customers-service
```

rather than hardcoded pod addresses.

---

# 25. Pod IP

Applications MUST NOT depend on individual pod IP addresses.

---

# 26. Dynamic Infrastructure

Pods are ephemeral.

Their addresses can change because of:

```text
Deployment

Scaling

Restart

Rescheduling

Node Failure
```

---

# 27. Kubernetes Service

Stable Kubernetes Services SHOULD provide logical network endpoints.

---

# 28. DNS

DNS resolution behavior MUST account for connection pooling and application runtime characteristics.

---

# 29. DNS Cache

Applications SHOULD NOT assume resolved IP addresses remain valid forever.

---

# 30. Hardcoded Host

Environment-specific internal hosts MUST NOT be hardcoded in Java source.

---

# 31. Configuration

Service endpoints MUST use externalized configuration or platform discovery.

---

# 32. Internal Traffic Security

Internal network location MUST NOT automatically imply trust.

---

# 33. Zero Trust

The platform SHOULD follow:

```text
NEVER TRUST
SOLELY BECAUSE
TRAFFIC IS INTERNAL
```

---

# 34. Service Identity

Services SHOULD have identifiable workload identities.

---

# 35. User Identity vs Service Identity

The platform MUST distinguish:

```text
WHO IS THE USER?
```

from:

```text
WHICH SERVICE IS CALLING?
```

---

# 36. OAuth2 Client Credentials

OAuth2 Client Credentials SHOULD be used where machine-to-machine identity is required and supported by the enterprise Identity Provider.

---

# 37. Example

```text
ORDER SERVICE
      |
      v
CLIENT CREDENTIALS
      |
      v
ACCESS TOKEN
      |
      v
WORKFLOW SERVICE
```

---

# 38. Client Secret

Client secrets MUST use approved secret management.

---

# 39. Secret in Repository

Client credentials MUST NOT be committed to source control.

---

# 40. Token Cache

Machine access tokens SHOULD be cached until safely before expiration.

---

# 41. Token Per Request

Obtaining a new OAuth token for every business request SHOULD be avoided.

---

# 42. Token Refresh

Token refresh SHOULD occur before expiry using an intentional safety margin.

---

# 43. Refresh Stampede

Concurrent requests MUST NOT cause uncontrolled simultaneous token refreshes.

---

# 44. Token Refresh Coordination

Token providers SHOULD coalesce concurrent refresh operations.

---

# 45. Token Endpoint Failure

Identity Provider failure MUST have bounded timeout/retry behavior.

---

# 46. Expired Cached Token

An expired token MUST NOT continue to be used indefinitely because the Identity Provider is unavailable.

---

# 47. User Token Propagation

User access tokens MAY be propagated when downstream authorization genuinely requires the original user's delegated identity.

---

# 48. Blind Propagation

User tokens MUST NOT automatically be forwarded to every downstream service.

---

# 49. Audience Problem

A token issued for:

```text
SERVICE A
```

may not be valid for:

```text
SERVICE B
```

---

# 50. Token Exchange

OAuth token exchange or another approved delegation mechanism SHOULD be considered where downstream services require a different audience.

---

# 51. Least Privilege Token

Downstream tokens SHOULD contain only the permissions required for that downstream interaction.

---

# 52. Service Token vs User Token

The architecture MUST explicitly decide whether an integration executes:

```text
AS THE USER
```

or:

```text
AS THE CALLING SERVICE
```

---

# 53. Audit Semantics

That decision affects audit semantics.

---

# 54. Mixed Identity

Where appropriate, the downstream context MAY carry both:

```text
SERVICE IDENTITY

END-USER IDENTITY
```

without conflating their authorization roles.

---

# 55. JWT Validation

Services accepting JWTs MUST validate:

```text
Signature

Issuer

Audience

Expiration

Required Claims
```

---

# 56. Gateway Validation Is Not Enough

A service SHOULD NOT blindly trust a JWT merely because an upstream gateway previously validated another token.

---

# 57. mTLS

mTLS MAY provide workload authentication and encrypted transport between selected services.

---

# 58. mTLS and OAuth

mTLS and OAuth solve related but different problems and MAY coexist.

---

# 59. mTLS Identity

mTLS authenticates the communicating workload/certificate.

---

# 60. OAuth Identity

OAuth access tokens communicate authorization/delegation semantics.

---

# 61. Certificate Rotation

mTLS certificates MUST support automated rotation.

---

# 62. Certificate Expiration

Certificate expiry MUST be monitored.

---

# 63. NetworkPolicy

Kubernetes NetworkPolicy SHOULD restrict unnecessary east-west traffic where supported.

---

# 64. Default Connectivity

The platform SHOULD move toward:

```text
DENY BY DEFAULT
+
ALLOW REQUIRED FLOWS
```

for sensitive workloads where operationally practical.

---

# 65. Example

If Orders requires:

```text
Customers

Products

Workflow
```

it does not automatically require network access to every namespace/service.

---

# 66. NetworkPolicy Is Not Authorization

NetworkPolicy does not replace application authentication or authorization.

---

# 67. Compromised Pod

A compromised pod inside the cluster MUST NOT automatically gain unrestricted access to every internal service.

---

# 68. HTTP Client

Java services SHOULD use a standardized HTTP client abstraction/configuration.

---

# 69. WebClient

Spring WebClient MAY be used for service integrations.

---

# 70. Blocking Client

A blocking client MAY also be valid with Java 21 Virtual Threads when architecture and library requirements justify it.

---

# 71. Client Consistency

Projects SHOULD avoid arbitrary mixtures of HTTP clients without architectural justification.

---

# 72. Shared Configuration

Common HTTP concerns SHOULD be standardized:

```text
Timeouts

Connection Pool

Headers

Authentication

Correlation

Error Mapping

Observability
```

---

# 73. Client Per Service

Logical downstream integrations SHOULD have explicit client components.

Example:

```text
CustomersClient

ProductsClient

WorkflowClient
```

---

# 74. Generic God Client

A generic client containing unrelated integrations SHOULD be avoided.

---

# 75. Typed Contract

Client code SHOULD expose domain/application-oriented operations rather than raw URL construction throughout business services.

---

# 76. Example

Prefer:

```java
customersClient.findCustomer(customerId);
```

over repeated:

```java
webClient.get()
    .uri(...)
```

throughout application code.

---

# 77. Timeout

Every remote call MUST have bounded timeout behavior.

---

# 78. Timeout Categories

Applicable limits include:

```text
CONNECT TIMEOUT

RESPONSE TIMEOUT

READ TIMEOUT

WRITE TIMEOUT

PENDING ACQUIRE TIMEOUT

OVERALL DEADLINE
```

---

# 79. No Infinite Wait

No service dependency should wait indefinitely.

---

# 80. Timeout Budget

Downstream timeouts MUST fit within the caller's remaining request budget.

---

# 81. Example

```text
CLIENT BUDGET        10s

ORDERS               8s

CUSTOMERS CALL        2s

PRODUCTS CALL         2s
```

must be designed rather than independently configuring every call to:

```text
30s
```

---

# 82. Timeout Hierarchy

Inner timeouts SHOULD generally expire before outer request deadlines.

---

# 83. Timeout Is Not Retry

Timeout and retry are separate mechanisms.

---

# 84. Retry

Retries MUST only be used for failures that are:

```text
TRANSIENT
+
SAFE TO RETRY
```

---

# 85. Retryable Examples

Possible retryable failures include:

```text
Connection Reset

Temporary 503

Selected 429

Transient Network Failure
```

---

# 86. Non-Retryable Examples

Usually non-retryable:

```text
400

401

403

404

Business Validation Error
```

unless an explicit contract states otherwise.

---

# 87. POST Retry

Non-idempotent operations MUST NOT be automatically retried without an idempotency strategy.

---

# 88. Idempotency Key

Critical write operations MAY use an explicit idempotency key.

---

# 89. Retry Count

Retries MUST be bounded.

---

# 90. Backoff

Retries SHOULD use backoff with jitter where applicable.

---

# 91. Retry Multiplication

The platform MUST account for retries at:

```text
CLIENT

API GATEWAY

SERVICE CLIENT

LOAD BALANCER

MESSAGE CONSUMER
```

---

# 92. Example

```text
GATEWAY = 3

ORDERS CLIENT = 3

PRODUCTS CLIENT = 3
```

can create severe amplification.

---

# 93. Retry Budget

Retries SHOULD consume a defined retry budget rather than continue independently at every layer.

---

# 94. Circuit Breaker

Circuit Breakers SHOULD protect important remote dependencies where repeated failure would otherwise consume resources and increase latency.

---

# 95. Circuit Breaker Purpose

Circuit Breaker exists to:

```text
FAIL FAST
```

during demonstrated dependency failure.

---

# 96. Circuit Breaker Is Not Retry

Circuit Breaker and retry solve different problems.

---

# 97. Circuit Breaker Naming

Circuit Breakers SHOULD use stable logical dependency names.

Example:

```text
customers-service

products-service

workflows-service
```

---

# 98. Criticality

Dependencies SHOULD be classified by criticality.

Example:

```text
CRITICAL

HIGH

MEDIUM

LOW
```

---

# 99. Critical Dependency

A critical dependency may legitimately cause the business operation to fail.

---

# 100. Optional Dependency

An optional dependency MAY have degraded behavior.

---

# 101. Fallback

Fallback MUST preserve business correctness.

---

# 102. Fake Success

Fallback MUST NOT convert:

```text
DEPENDENCY FAILURE
```

into false business success.

---

# 103. Dangerous Fallback

This is prohibited:

```text
PRICE SERVICE DOWN
      |
      v
RETURN PRICE = 0
```

unless zero is explicitly valid business behavior.

---

# 104. Stale Fallback

Cached stale data MAY be used only when its staleness is business-safe.

---

# 105. Bulkhead

Remote integrations SHOULD have bounded concurrency.

---

# 106. Dependency Isolation

A failing dependency SHOULD NOT consume all application execution capacity.

---

# 107. Separate Bulkhead

High-risk/slow dependencies MAY use independent concurrency limits.

---

# 108. Connection Pool

HTTP connection pools MUST be bounded.

---

# 109. Pool Configuration

Important settings include:

```text
MAX CONNECTIONS

PENDING ACQUIRE LIMIT

PENDING ACQUIRE TIMEOUT

MAX IDLE TIME

MAX LIFE TIME

EVICTION INTERVAL
```

---

# 110. Pool Multiplication

Connection capacity MUST be calculated across pods.

Example:

```text
25 PODS
x
200 CONNECTIONS
=
5000 CONNECTIONS
```

to one downstream service.

---

# 111. Downstream Capacity

Caller-side connection pools MUST account for downstream capacity.

---

# 112. Large Pool

A larger connection pool does not create downstream capacity.

---

# 113. Pending Queue

Pending connection-acquire queues MUST be bounded.

---

# 114. Unbounded Pending Requests

Unbounded pending requests can turn dependency slowdown into application memory exhaustion.

---

# 115. Keep-Alive

Connection reuse SHOULD be enabled where appropriate.

---

# 116. Stale Connection

Idle/lifetime settings SHOULD avoid excessive reuse of stale infrastructure connections.

---

# 117. DNS and Pooling

Long-lived connection pools can delay adoption of new DNS endpoints.

---

# 118. Virtual Threads

Java 21 Virtual Threads MAY be used for I/O-bound synchronous integration work.

---

# 119. Virtual Thread Benefit

Virtual Threads reduce the cost of blocked Java execution threads.

---

# 120. Virtual Thread Limitation

Virtual Threads do NOT make these resources unlimited:

```text
HTTP CONNECTIONS

DATABASE CONNECTIONS

DOWNSTREAM CAPACITY

MEMORY

CPU

PROVIDER QUOTAS
```

---

# 121. Unbounded Fan-Out

This remains unsafe even with Virtual Threads:

```text
100,000 ITEMS
      |
      v
100,000 REMOTE CALLS
```

---

# 122. Concurrency Limit

Parallel remote calls MUST have explicit concurrency bounds.

---

# 123. Parallelism

Independent downstream calls MAY execute concurrently when this materially reduces latency.

---

# 124. Example

Sequential:

```text
CUSTOMERS 300 ms
     |
PRODUCTS  400 ms
     |
USERS     300 ms

TOTAL ~= 1000 ms
```

Independent parallel calls MAY approach:

```text
MAX(300, 400, 300)
```

plus orchestration overhead.

---

# 125. Dependency Independence

Calls MUST only be parallelized when they are logically independent.

---

# 126. Parallel Failure

Parallel orchestration MUST define what happens when one dependency fails.

---

# 127. Cancellation

Remaining unnecessary work SHOULD be cancelled where supported after a decisive failure.

---

# 128. Partial Result

Partial results MUST only be returned when the API contract explicitly permits them.

---

# 129. Fan-Out

High fan-out endpoints require special review.

---

# 130. N+1 Remote Calls

The distributed equivalent of an N+1 database query MUST be avoided.

---

# 131. Example

Avoid:

```text
100 ORDER ITEMS
      |
      v
100 PRODUCT API CALLS
```

when a batch API can retrieve required products in one bounded request.

---

# 132. Batch API

Frequently requested related resources SHOULD support batch retrieval where appropriate.

---

# 133. Batch Bound

Batch endpoints MUST have maximum item limits.

---

# 134. Batch Partial Failure

Batch response semantics MUST define missing/failed items explicitly.

---

# 135. Request Deduplication

Repeated identical downstream reads within one request MAY be coalesced where beneficial.

---

# 136. Caching

Stable downstream reference data MAY use caching according to ADR-080.

---

# 137. Snapshot

Business workflows MAY persist required snapshots to avoid repeated dependence on volatile remote data.

---

# 138. Snapshot Semantics

Snapshots MUST represent intentional business history rather than accidental stale cache.

---

# 139. Dependency Graph

Each service SHOULD maintain an understandable dependency graph.

Example:

```text
CART
 |
 +--> CUSTOMERS
 |
 +--> PRODUCTS
 |
 +--> ORDERS

ORDERS
 |
 +--> WORKFLOWS
 |
 +--> USERS
```

---

# 140. Circular Dependency

Synchronous circular service dependencies SHOULD be prohibited.

---

# 141. Example

Avoid:

```text
A --> B --> C --> A
```

---

# 142. Circular Runtime Risk

Circular dependencies increase:

```text
Failure Propagation

Deployment Coupling

Reasoning Complexity

Latency
```

---

# 143. Dependency Depth

Critical request paths SHOULD minimize synchronous dependency depth.

---

# 144. Dependency Criticality Map

Dependencies SHOULD document whether they are:

```text
REQUIRED

DEGRADABLE

OPTIONAL
```

---

# 145. Failure Matrix

Critical integrations SHOULD define behavior for:

```text
TIMEOUT

5xx

4xx

INVALID RESPONSE

AUTH FAILURE

CIRCUIT OPEN

DNS FAILURE
```

---

# 146. Contract

Internal API contracts MUST be explicit.

---

# 147. OpenAPI

REST contracts SHOULD use OpenAPI where practical.

---

# 148. Contract Compatibility

Providers SHOULD preserve backward compatibility during consumer migration windows.

---

# 149. Additive Change

Adding an optional response field is generally safer than removing or changing an existing field.

---

# 150. Breaking Change

Breaking internal API changes require controlled versioning/migration.

---

# 151. Consumer Assumption

Providers MUST NOT assume every consumer deploys simultaneously.

---

# 152. Tolerant Reader

Consumers SHOULD ignore unknown response fields where appropriate.

---

# 153. Enum Evolution

Consumers SHOULD deliberately handle unknown future enum values where contract requirements allow.

---

# 154. Required Field

Changing an optional field to mandatory can be a breaking change.

---

# 155. API Version

Versioning SHOULD be introduced when compatibility cannot otherwise be maintained.

---

# 156. Internal Version Explosion

Versioning every small additive change SHOULD be avoided.

---

# 157. Consumer-Driven Contract

Consumer-driven contract tests MAY be used for important service integrations.

---

# 158. Contract Test Limitation

Contract tests do not replace integration tests for:

```text
TLS

Authentication

Network Failure

Timeout

Serialization

Real HTTP Behavior
```

---

# 159. Error Contract

Remote services SHOULD provide stable error contracts.

---

# 160. Problem Details

HTTP Problem Details SHOULD be used where established by platform standards.

---

# 161. Remote Error Mapping

Clients SHOULD translate remote technical errors into application-level exceptions intentionally.

---

# 162. Raw Exception Leakage

Business/application layers SHOULD NOT depend directly on arbitrary HTTP-client exception classes.

---

# 163. Example

Prefer:

```text
CustomerNotFoundException

ExternalServiceUnavailableException
```

where semantically appropriate.

---

# 164. Status Preservation

Remote status/context SHOULD be preserved internally where useful for diagnosis without exposing sensitive details.

---

# 165. Remote Error Body

Remote error bodies MUST be treated as untrusted data.

---

# 166. Log Injection

Remote messages MUST be sanitized before logging.

---

# 167. Sensitive Error

Tokens, credentials and personal data MUST be masked from remote-error logs.

---

# 168. Correlation

Service-to-service calls SHOULD propagate approved correlation context.

---

# 169. Request ID

A request/correlation ID SHOULD be available across the distributed call path.

---

# 170. Trace Context

W3C Trace Context SHOULD be propagated when distributed tracing is enabled.

---

# 171. Baggage

Tracing baggage MUST remain bounded and privacy-safe.

---

# 172. Authorization in Trace

Authorization tokens MUST NOT be trace attributes.

---

# 173. Logging

Remote integration logs SHOULD contain safe:

```text
dependency

operation

result

status

elapsedMs

correlationId
```

---

# 174. URL Logging

URLs containing sensitive query parameters MUST be sanitized.

---

# 175. Request Body Logging

Full request payloads MUST NOT be logged by default.

---

# 176. Response Body Logging

Full remote response payloads MUST NOT be logged by default.

---

# 177. Metrics

Useful integration metrics include:

```text
client_requests

client_duration

client_errors

client_timeouts

circuit_state

connection_pool_active

connection_pool_pending
```

---

# 178. Metric Dimensions

Bounded dimensions MAY include:

```text
dependency

operation

result

status_class
```

---

# 179. ID Metric

Customer IDs, order IDs, URLs and correlation IDs MUST NOT become metric labels.

---

# 180. Dependency Dashboard

Critical services SHOULD have dashboards showing downstream health.

---

# 181. Distributed Trace

Tracing SHOULD make waterfall latency visible.

Example:

```text
ORDERS 850ms
 |
 +--> CUSTOMERS 150ms
 |
 +--> PRODUCTS 600ms
 |
 +--> USERS 80ms
```

---

# 182. Critical Path

Tracing SHOULD help identify which downstream dependency dominates end-to-end latency.

---

# 183. Alerting

Applicable alerts include:

```text
Dependency Failure Rate

Dependency Timeout Rate

Circuit Open

Latency Increase

Connection Pool Saturation

Authentication Failure

DNS Failure

Certificate Expiration
```

---

# 184. Dependency SLO

Critical service dependencies SHOULD have known availability/latency expectations.

---

# 185. SLO Composition

End-to-end SLOs MUST account for required downstream dependencies.

---

# 186. Testing Strategy

Distributed integrations require dedicated tests.

---

# 187. Client Unit Test

Client mapping SHOULD test:

```text
Request

Headers

Response

Error Mapping
```

---

# 188. Authentication Test

Verify required machine/user credentials are propagated correctly.

---

# 189. Token Leakage Test

Authorization values MUST not appear in exception/log output.

---

# 190. Timeout Test

Slow downstream behavior MUST trigger bounded timeout.

---

# 191. Retry Test

Transient failures SHOULD verify the exact maximum number of attempts.

---

# 192. Non-Retry Test

Business/validation errors MUST verify no unnecessary retry occurs.

---

# 193. POST Test

Non-idempotent writes MUST verify they are not duplicated by resilience configuration.

---

# 194. Circuit Breaker Test

Repeated failures SHOULD verify:

```text
CLOSED

OPEN

HALF_OPEN

CLOSED
```

transitions where applicable.

---

# 195. Fallback Test

Fallback MUST verify business correctness rather than merely returning any value.

---

# 196. Connection Pool Test

Pool saturation SHOULD verify bounded pending behavior for critical clients.

---

# 197. Concurrency Test

Parallel integrations MUST verify concurrency limits.

---

# 198. No `Thread.sleep`

Concurrency tests SHOULD use deterministic coordination rather than `Thread.sleep`.

---

# 199. Batch Test

Batch integrations SHOULD verify:

```text
Maximum Batch Size

Missing IDs

Duplicate IDs

Partial Results
```

---

# 200. N+1 Test

Critical orchestration tests SHOULD verify a bounded number of downstream calls.

---

# 201. Contract Test

Provider/consumer compatibility SHOULD have automated contract validation where valuable.

---

# 202. Integration Test

Real HTTP integration behavior SHOULD be tested with controlled test servers/containers.

---

# 203. Network Failure Test

Critical integrations SHOULD test:

```text
Connection Refused

Connection Reset

DNS Failure

Timeout
```

---

# 204. Authentication Failure Test

Test:

```text
401

403

Expired Machine Token

Invalid Audience
```

---

# 205. Malformed Response Test

Unexpected/malformed downstream payloads MUST fail deterministically.

---

# 206. Large Response Test

Client response-size behavior SHOULD be bounded for high-risk integrations.

---

# 207. Failure Injection

Staging/resilience exercises SHOULD simulate downstream outages.

---

# 208. Chaos Testing

Controlled chaos testing MAY validate failure containment for critical dependency graphs.

---

# 209. AssertJ

Java tests MUST follow established quality conventions, including meaningful:

```java
.as("...")
```

before applicable assertions.

---

# 210. Service Communication Review Checklist

```text
[ ] Does this interaction need to be synchronous?

[ ] Could an event remove temporal coupling?

[ ] Who owns the data?

[ ] Is another service's database being accessed?

[ ] What is the service discovery mechanism?

[ ] Are endpoints externally configured?

[ ] What identity does the caller use?

[ ] Is this acting as the user or as the service?

[ ] Is the token audience correct?

[ ] Is token exchange required?

[ ] Is mTLS required?

[ ] Is NetworkPolicy required?

[ ] What is the connect timeout?

[ ] What is the response timeout?

[ ] What is the overall deadline?

[ ] Is retry actually safe?

[ ] Is the operation idempotent?

[ ] Could retries multiply?

[ ] Is Circuit Breaker appropriate?

[ ] What is the fallback?

[ ] Is the fallback business-safe?

[ ] Is concurrency bounded?

[ ] Is the connection pool bounded?

[ ] How many connections exist across all pods?

[ ] Could this create N+1 remote calls?

[ ] Is a batch API available?

[ ] Can independent calls execute concurrently?

[ ] Is there a synchronous dependency cycle?

[ ] What is the maximum dependency depth?

[ ] Is the API contract backward compatible?

[ ] Can provider and consumer deploy independently?

[ ] Are remote errors sanitized?

[ ] Is correlation propagated?

[ ] Are dependency metrics available?

[ ] Has downstream failure been tested?
```

---

# 211. Service Communication Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] No cross-service database access

[ ] Remote clients have bounded connect timeout

[ ] Remote clients have bounded response timeout

[ ] Connection pools are bounded

[ ] Pending connection acquisition is bounded

[ ] Retry attempts are bounded

[ ] POST retries require idempotency

[ ] Circuit Breakers use stable dependency names

[ ] Authorization headers are excluded from logs

[ ] Integration metrics use bounded dependency names

[ ] Batch endpoints have maximum size

[ ] Critical clients have timeout tests

[ ] Critical clients have circuit-breaker tests

[ ] Consumer contracts have compatibility validation
```

---

# 212. Enterprise Service Communication Gate

A service integration is not considered compliant when applicable conditions include:

```text
[ ] Service directly queries another service's database

[ ] Every integration defaults to synchronous REST without analysis

[ ] Internal API contract is undocumented

[ ] Pod IP is hardcoded

[ ] Internal network location is treated as authentication

[ ] User token is blindly forwarded everywhere

[ ] JWT audience is ignored

[ ] Machine credentials are hardcoded

[ ] OAuth token is requested on every business request

[ ] Token refresh creates concurrency stampede

[ ] Remote timeout is unbounded

[ ] Connection pool is unbounded

[ ] Pending acquire queue is unbounded

[ ] Retry is applied to every exception

[ ] Non-idempotent POST is blindly retried

[ ] Retry layers multiply uncontrollably

[ ] Fallback creates false business success

[ ] Virtual Threads are used to justify unlimited remote calls

[ ] Endpoint creates N+1 service calls

[ ] Synchronous dependency cycle exists

[ ] Provider assumes every consumer deploys simultaneously

[ ] Remote error body containing secrets is logged

[ ] Downstream outage behavior has never been tested
```

---

# 213. Anti-Patterns

The following are prohibited or strongly discouraged:

- cross-service database access
- shared persistence as integration API
- hardcoded pod IPs
- hardcoded environment URLs
- trusting all internal traffic
- blindly forwarding user JWTs
- ignoring token audience
- machine credentials in source
- token request per business operation
- uncoordinated token refresh
- infinite remote timeout
- unlimited connection pools
- unlimited pending connection requests
- retry every exception
- retry non-idempotent writes without idempotency
- multiple independent retry layers
- fallback returning fabricated business values
- unlimited fan-out with Virtual Threads
- remote N+1
- synchronous circular dependencies
- generic god HTTP client
- domain code coupled to HTTP-client exceptions
- logging authorization headers
- high-cardinality integration metrics
- internal APIs without compatibility strategy

---

# 214. Positive Consequences

The decision provides:

- stronger service autonomy
- reduced temporal coupling
- safer internal networking
- explicit service identity
- controlled delegated identity
- bounded remote-call latency
- reduced retry amplification
- improved failure isolation
- safer connection management
- reduced N+1 integrations
- clearer dependency graphs
- stronger contract compatibility
- improved distributed observability

---

# 215. Negative Consequences

The decision introduces:

- identity infrastructure
- more explicit client configuration
- resilience policies
- NetworkPolicy maintenance
- contract governance
- distributed testing
- operational dashboards
- potential event-driven complexity

These costs are accepted because distributed communication without explicit controls produces cascading failures and hidden coupling.

---

# 216. Neutral Consequences

The decision also means:

- not every interaction should become asynchronous
- not every interaction should remain synchronous
- not every internal service needs mTLS
- not every call should propagate the user token
- not every failure deserves retry
- not every dependency deserves fallback
- Virtual Threads improve execution scalability but do not create downstream capacity
- some business operations legitimately fail when a critical dependency is unavailable

---

# 217. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Cascading failure | Critical | Medium | Timeout + CB + bulkhead |
| Retry amplification | Critical | Medium | Retry budget |
| Credential leakage | Critical | Low/Medium | Secret management |
| Token misuse | Critical | Medium | Audience/delegation controls |
| Connection exhaustion | High | Medium | Bounded pools |
| N+1 remote calls | High | Medium | Batch APIs |
| Dependency cycle | High | Low/Medium | Architecture governance |
| Contract breakage | High | Medium | Compatibility tests |
| Internal lateral movement | Critical | Medium | Zero trust + NetworkPolicy |
| Downstream overload | Critical | Medium | Concurrency limits |

---

# 218. Implementation Guidance

The following rules are mandatory:

1. Every integration must explicitly choose synchronous or asynchronous semantics.
2. Synchronous calls should only be used when the result is required immediately.
3. Services must not query another service's database.
4. Internal APIs must be treated as independently deployed contracts.
5. Kubernetes services/DNS should provide service discovery.
6. Pod addresses and environment endpoints must not be hardcoded.
7. Internal network location must not be treated as sufficient trust.
8. Machine-to-machine integrations must use approved service identity.
9. User-token propagation must only occur when delegated user identity is required.
10. Token audience and authorization semantics must be validated.
11. Token exchange should be used when downstream audience/delegation requires it.
12. Credentials and certificates must use approved secret/identity management.
13. NetworkPolicy should enforce least-required network connectivity where practical.
14. HTTP clients must use standardized timeout, pool, security and observability configuration.
15. Every remote call must have bounded timeout.
16. Retries must be bounded, transient-only and idempotency-aware.
17. Retry multiplication across layers must be controlled.
18. Circuit Breakers should protect material unstable dependencies.
19. Fallback must preserve business correctness.
20. Connection pools and pending acquisition must be bounded.
21. Pool capacity must account for total pod count.
22. Virtual Threads must not be used to justify unbounded downstream concurrency.
23. Parallel calls must have explicit concurrency limits.
24. N+1 remote-call patterns must be eliminated through batch APIs or redesigned data access.
25. Synchronous dependency cycles must be avoided.
26. Dependency criticality and failure behavior must be explicit.
27. Internal API changes must preserve compatibility during migration windows.
28. Remote errors must be translated and sanitized.
29. Correlation/trace context should propagate safely.
30. Integration metrics must use bounded dependency/operation dimensions.
31. Critical distributed failure scenarios must have automated tests.
32. Dependency graphs should remain documented and reviewable.

---

# 219. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring WebClient
- Reactor Netty where applicable
- Java 21 Virtual Threads where appropriate
- OAuth2 Client Credentials
- OIDC/JWT
- Keycloak / Enterprise IdP
- Kubernetes DNS
- Kubernetes NetworkPolicy
- TLS/mTLS
- Resilience4j
- SQS where asynchronous integration applies
- OpenAPI
- Testcontainers
- WireMock or equivalent controlled HTTP test infrastructure
- JUnit 5
- AssertJ
- Micrometer
- OpenTelemetry where enabled
- failure injection
- load testing
- architecture tests

---

# 220. Success Criteria

The decision is successful when:

- cross-service database coupling is eliminated
- synchronous dependencies exist only where justified
- internal service identity is explicit
- user delegation is not confused with machine identity
- service endpoints remain stable across pod replacement
- remote calls cannot wait indefinitely
- retries cannot create uncontrolled amplification
- connection pools cannot grow without limit
- downstream outages cannot consume all upstream capacity
- Virtual Threads remain constrained by downstream capacity
- N+1 service-call patterns are detectable and removed
- internal contracts support independent deployment
- critical dependency failures are visible in metrics/traces
- distributed failure behavior is exercised before production incidents

---

# 221. Alternatives Rejected

## 221.1 Synchronous REST for Everything

Rejected because it creates unnecessary temporal coupling and cascading availability dependencies.

---

## 221.2 Events for Everything

Rejected because some operations genuinely require immediate request/response semantics.

---

## 221.3 Shared Database Integration

Rejected because it breaks service ownership and independent schema evolution.

---

## 221.4 Trust All Cluster Traffic

Rejected because internal network location is not sufficient security identity.

---

## 221.5 Propagate User JWT Everywhere

Rejected because audience, least privilege and service-identity semantics differ across integrations.

---

## 221.6 Unlimited Virtual-Thread Fan-Out

Rejected because Virtual Threads do not create unlimited downstream/network capacity.

---

## 221.7 Retry Every Remote Failure

Rejected because permanent failures and non-idempotent operations can cause amplification and duplicate business effects.

---

# 222. Related Decisions

This ADR extends and implements:

- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise Authentication, Authorization, OAuth2, OIDC and JWT Security Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-065: Enterprise WebClient, HTTP Client and External Integration Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-080: Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard
- ADR-082: Enterprise API Gateway, BFF, Edge Security, Routing and Traffic Management Standard

---

# 223. References

- RFC 6749 — OAuth 2.0
- RFC 7519 — JSON Web Token
- OAuth 2.0 Token Exchange
- OpenID Connect
- RFC 9110 — HTTP Semantics
- Kubernetes Services Documentation
- Kubernetes DNS Documentation
- Kubernetes NetworkPolicy Documentation
- Spring Security Documentation
- Spring WebClient Documentation
- Reactor Netty Documentation
- Resilience4j Documentation
- OWASP API Security Top 10
- OWASP Zero Trust Architecture Guidance
- NIST Zero Trust Architecture
- W3C Trace Context
- Google Site Reliability Engineering
- Enterprise Integration Patterns

---

# 224. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise service communication and zero-trust networking baseline |

---

# 225. Decision Summary

Service integration selection becomes:

```text
BUSINESS NEED
     |
     v
NEED RESULT NOW?
     |
   +-+-+
   |   |
  YES  NO
   |   |
   v   v
SYNC  ASYNC
API   EVENT
```

Service ownership becomes:

```text
ORDERS SERVICE
      |
      v
ORDERS DATABASE

CUSTOMERS SERVICE
      |
      v
CUSTOMERS DATABASE
```

rather than:

```text
ORDERS SERVICE
      |
      +--> ORDERS DB
      |
      +--> CUSTOMERS DB
```

Service discovery becomes:

```text
APPLICATION
     |
     v
KUBERNETES SERVICE DNS
     |
     v
READY PODS
```

instead of hardcoded pod addresses.

Identity becomes:

```text
REQUEST
   |
   +--> SERVICE IDENTITY
   |
   +--> OPTIONAL USER DELEGATION
   |
   v
DOWNSTREAM AUTHORIZATION
```

Remote-call resilience becomes:

```text
CALL
 |
 v
BOUNDED TIMEOUT
 |
 v
SAFE RETRY?
 |
 +--> NO --> FAIL
 |
 +--> YES
        |
        v
   BOUNDED RETRY
        |
        v
 CIRCUIT BREAKER
```

Connection management becomes:

```text
POD
 |
 v
BOUNDED CONNECTION POOL
 |
 v
BOUNDED PENDING ACQUIRE
 |
 v
DOWNSTREAM SERVICE
```

Virtual Thread usage becomes:

```text
10,000 VIRTUAL THREADS
        |
        v
CONCURRENCY LIMIT
        |
        v
CONNECTION POOL
        |
        v
DOWNSTREAM CAPACITY
```

rather than:

```text
10,000 VIRTUAL THREADS
        |
        v
10,000 REMOTE CALLS
```

Remote N+1 becomes:

```text
100 ITEMS
    |
    v
BATCH REQUEST
    |
    v
PRODUCT SERVICE
```

instead of:

```text
100 ITEMS
    |
    v
100 HTTP REQUESTS
```

Dependency architecture becomes:

```text
SERVICE
 |
 +--> REQUIRED DEPENDENCY
 |
 +--> DEGRADABLE DEPENDENCY
 |
 +--> OPTIONAL DEPENDENCY
```

with explicit failure semantics.

The complete service-communication equation is:

```text
SERVICE OWNERSHIP
        +
EXPLICIT SYNC/ASYNC CHOICE
        +
STABLE SERVICE DISCOVERY
        +
ZERO TRUST
        +
SERVICE IDENTITY
        +
CORRECT USER DELEGATION
        +
LEAST PRIVILEGE
        +
BOUNDED TIMEOUT
        +
IDEMPOTENCY-AWARE RETRY
        +
CIRCUIT BREAKER
        +
BULKHEAD
        +
BOUNDED CONNECTION POOL
        +
BOUNDED CONCURRENCY
        +
BATCH APIs
        +
CONTRACT COMPATIBILITY
        +
SAFE ERROR MAPPING
        +
CORRELATION
        +
OBSERVABILITY
        +
FAILURE TESTING
        =
RELIABLE SERVICE-TO-SERVICE COMMUNICATION
```

The governing principle is:

```text
Do not treat
a remote service call
as a Java method call.

The network can fail.

DNS can fail.

Authentication can fail.

Authorization can fail.

Connections can exhaust.

The downstream can become slow.

The downstream can disappear.

Choose synchronous communication
only when you need
the answer now.

Otherwise consider events.

Never query
another service's database.

Respect bounded contexts.

Use stable service discovery.

Never hardcode pod addresses.

Do not trust traffic
because it came
from inside the cluster.

Know the calling service.

Know the end user.

Do not confuse them.

Do not blindly propagate tokens.

Validate audience.

Use least privilege.

Bound every timeout.

Bound every retry.

Retry only
what is safe.

Never fabricate success
as a fallback.

Bound connection pools.

Bound pending acquisition.

Calculate capacity
across every pod.

Virtual Threads make
blocking cheaper.

They do not make
remote systems infinite.

Parallelize independent calls,
but bound concurrency.

Avoid remote N+1.

Prefer bounded batch APIs.

Keep dependency graphs shallow.

Never create
synchronous service cycles.

Treat internal APIs
as real contracts.

Assume providers
and consumers deploy
at different times.

Sanitize remote errors.

Never log tokens.

Propagate correlation safely.

Measure downstream latency.

Measure timeouts.

Measure pool saturation.

Measure circuit state.

Test dependency failure.

And remember:

microservice reliability
does not come from
having many small applications.

It comes from ensuring
that the failure of one
does not automatically become
the failure of all the others.
```
