# ADR-064: Adopt Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-064 |
| Title | Adopt Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard |
| Status | Accepted |
| Date | 2026-07-25 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Authentication, Authorization, Identity, OAuth2, OIDC, JWT, Service Security |
| Related Work Items | Spring Security 6, Keycloak, OAuth2 Resource Server, JWT, AWS IAM, Kubernetes |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise distributed applications must establish:

```text
WHO is calling?

HOW was the caller authenticated?

WHAT is the caller allowed to do?

WHICH resource may the caller access?

WHICH tenant/customer/segment does the resource belong to?

WHICH identity must be propagated downstream?
```

A typical request path is:

```text
USER
 |
 v
IDENTITY PROVIDER
 |
 v
ACCESS TOKEN
 |
 v
API / SERVICE A
 |
 +--> SERVICE B
 |
 +--> SERVICE C
 |
 +--> SQS
 |
 v
PROTECTED RESOURCE
```

Authentication at the first service does not automatically make downstream operations secure.

Every trust boundary requires explicit security semantics.

---

# 2. Problem Statement

The organization requires standards covering:

- OAuth 2.x
- OpenID Connect
- Keycloak
- JWT
- Spring Security 6
- authentication
- authorization
- issuer validation
- audience validation
- token expiration
- scopes
- roles
- authorities
- RBAC
- ABAC
- method security
- resource ownership
- IDOR/BOLA
- service-to-service authentication
- client credentials
- workload identity
- token propagation
- token caching
- refresh
- CORS
- CSRF
- SecurityContext
- Virtual Threads
- asynchronous execution
- messaging
- least privilege
- multi-tenancy
- segment isolation
- security testing

---

# 3. Decision Drivers

Primary drivers are:

1. zero implicit trust
2. least privilege
3. identity integrity
4. resource isolation
5. service-to-service security
6. token validation correctness
7. authorization consistency
8. prevention of privilege escalation
9. auditability
10. secure asynchronous execution
11. scalability
12. maintainability

---

# 4. Decision

Applicable HTTP APIs MUST use standards-based authentication through OAuth2/OIDC and Spring Security.

Canonical user flow:

```text
USER
  |
  v
OIDC PROVIDER
  |
  v
ACCESS TOKEN
  |
  v
API
  |
  +--> SIGNATURE
  +--> ISSUER
  +--> AUDIENCE
  +--> EXPIRATION
  +--> AUTHORIZATION
  |
  v
RESOURCE
```

Authentication and authorization are separate decisions.

---

# 5. Fundamental Principle

```text
Authentication answers:

WHO ARE YOU?

Authorization answers:

ARE YOU ALLOWED
TO DO THIS
TO THIS RESOURCE
IN THIS CONTEXT?
```

A successfully authenticated caller MUST NOT automatically receive access to every protected resource.

---

# 6. Identity Provider

Enterprise user identities SHOULD be authenticated through an approved centralized Identity Provider.

---

# 7. Keycloak

Keycloak MAY serve as the enterprise OIDC/OAuth2 provider where adopted by the platform.

Applications MUST NOT depend unnecessarily on Keycloak-specific APIs when standard OAuth2/OIDC semantics are sufficient.

---

# 8. OpenID Connect

OIDC SHOULD be used when application functionality requires authenticated user identity.

---

# 9. OAuth2

OAuth2 access tokens SHOULD authorize protected API access.

---

# 10. ID Token

An OIDC ID token identifies the authenticated user to the client application.

It MUST NOT automatically be treated as an API access token.

---

# 11. Access Token

Protected APIs MUST expect an appropriate access token.

---

# 12. Bearer Token

Bearer tokens are credentials.

Anyone possessing a valid bearer token may potentially use it according to its permissions.

They MUST therefore be protected as secrets in transit, memory, telemetry and diagnostics.

---

# 13. HTTPS

Bearer tokens MUST only traverse appropriately secured transport.

---

# 14. JWT

JWT MAY be used as the access-token representation.

A JWT MUST NOT be trusted merely because it can be decoded.

---

# 15. Decode Is Not Validation

This is insufficient:

```text
BASE64 DECODE
      |
      v
READ CLAIMS
      |
      v
TRUST TOKEN
```

---

# 16. JWT Validation

JWT validation MUST verify applicable:

```text
Cryptographic Signature

Issuer

Expiration

Not-Before

Audience

Token Type / Intended Use
```

---

# 17. Signature Validation

JWT signatures MUST be validated using trusted issuer key material.

---

# 18. Algorithm

Applications MUST NOT accept arbitrary token-selected cryptographic algorithms without validation.

---

# 19. `alg=none`

Unsigned JWTs MUST NOT be accepted for protected enterprise APIs.

---

# 20. Issuer

The token issuer MUST match the configured trusted issuer.

---

# 21. Audience

Services SHOULD validate that the token is intended for the relevant API/resource where the identity architecture provides audience semantics.

---

# 22. Expiration

Expired access tokens MUST be rejected.

---

# 23. Clock Skew

A small controlled clock-skew allowance MAY be configured.

Large clock-skew allowances SHOULD NOT be used to compensate for infrastructure time synchronization problems.

---

# 24. `nbf`

Tokens with a future `not-before` claim MUST be rejected outside the approved clock tolerance.

---

# 25. Token Lifetime

Access-token lifetime SHOULD be limited according to security requirements.

---

# 26. Long-Lived Access Token

Long-lived bearer access tokens SHOULD be avoided.

---

# 27. Token Revocation

JWT validation is commonly stateless.

Systems requiring immediate revocation MUST explicitly design for that requirement rather than assuming token expiration provides immediate revocation.

---

# 28. Refresh Token

Refresh tokens SHOULD remain at appropriate trusted client/authentication boundaries.

---

# 29. Refresh Token Propagation

Backend microservices SHOULD NOT routinely propagate user refresh tokens through the service graph.

---

# 30. Spring Security

Spring Security 6 is the standard security framework for applicable Spring Boot services.

---

# 31. Resource Server

Protected APIs SHOULD use Spring Security OAuth2 Resource Server support rather than custom JWT parsing.

---

# 32. SecurityFilterChain

HTTP authorization rules MUST be explicitly defined through `SecurityFilterChain`.

---

# 33. Default Policy

Protected applications SHOULD prefer:

```text
DENY UNLESS EXPLICITLY ALLOWED
```

over:

```text
ALLOW UNLESS EXPLICITLY DENIED
```

---

# 34. Public Endpoints

Public endpoints MUST be explicitly identified.

Examples MAY include:

```text
Liveness

Readiness

Approved API Documentation

Authentication Callback
```

according to platform policy.

---

# 35. `permitAll`

`permitAll()` MUST NOT be applied broadly merely to resolve authentication problems during development.

---

# 36. Endpoint Authorization

Authorization MUST consider HTTP method as well as path.

---

# 37. Example

```text
GET    /orders/{id}  -> READ permission

POST   /orders       -> CREATE permission

PUT    /orders/{id}  -> UPDATE permission

DELETE /orders/{id}  -> DELETE permission
```

---

# 38. Roles

Roles SHOULD represent stable organizational/application responsibilities.

Examples:

```text
ANALYST

SUPERVISOR

ADMINISTRATOR
```

---

# 39. Permission

Fine-grained permissions SHOULD describe capabilities.

Examples:

```text
orders:read

orders:create

orders:approve

orders:cancel
```

---

# 40. Scope

OAuth scopes SHOULD represent delegated API capabilities appropriate to the authorization architecture.

---

# 41. Authority Mapping

JWT claims MUST be mapped to Spring Security authorities through controlled mapping logic.

---

# 42. Prefix Semantics

Teams MUST understand Spring Security distinctions such as:

```text
ROLE_ADMIN

SCOPE_orders.read
```

rather than relying on accidental prefix behavior.

---

# 43. Claim Trust

Authorization MUST use claims issued by a trusted authority.

---

# 44. Client-Supplied Role

A caller MUST NOT gain permissions by sending fields such as:

```json
{
  "role": "ADMIN"
}
```

inside an ordinary business request.

---

# 45. Request User Profile

When business payloads contain fields such as:

```text
userProfile

userProfileWorkflow
```

the application MUST distinguish business workflow data from security authority.

---

# 46. Security Authority Source

Security authorization MUST derive from trusted authentication/security context, not arbitrary request payload values.

---

# 47. RBAC

Role-Based Access Control MAY authorize operations based on assigned roles.

---

# 48. RBAC Limitation

RBAC alone may be insufficient when access depends on:

```text
Customer

Segment

Company

Region

Resource Ownership

Order State
```

---

# 49. ABAC

Attribute-Based Access Control SHOULD complement RBAC when authorization depends on contextual attributes.

---

# 50. Authorization Model

A typical decision may be:

```text
AUTHENTICATED
      +
ROLE = SUPERVISOR
      +
SEGMENT = M&M
      +
RESOURCE.SEGMENT = M&M
      +
RESOURCE.COMPANY IN ALLOWED COMPANIES
      =
ALLOW
```

---

# 51. Method Security

Business-sensitive authorization SHOULD be enforced near the business operation when endpoint rules alone are insufficient.

---

# 52. `@PreAuthorize`

Spring method security MAY use:

```java
@PreAuthorize(...)
```

for explicit operation-level authorization.

---

# 53. Controller-Only Authorization

Critical business authorization MUST NOT rely solely on controller routing if the same service method can be invoked through another entry point.

---

# 54. Defense in Depth

Authorization MAY exist at:

```text
HTTP Boundary

Application Service

Domain/Resource Policy
```

when each layer protects a distinct concern.

---

# 55. Duplicate Security Logic

Authorization rules SHOULD NOT be copy-pasted inconsistently across controllers.

---

# 56. Authorization Policy

Complex authorization SHOULD be encapsulated in named policy components.

Example:

```java
orderAuthorization.canApprove(authentication, order)
```

---

# 57. Resource-Level Authorization

Possession of permission:

```text
orders:read
```

does not necessarily authorize reading every order.

---

# 58. BOLA

APIs MUST protect against Broken Object Level Authorization.

---

# 59. IDOR

Changing:

```text
/orders/123
```

to:

```text
/orders/124
```

MUST NOT grant access merely because both resources exist.

---

# 60. Resource Ownership

Resource ownership/visibility MUST be verified after identifying the resource.

---

# 61. Query Authorization

Where practical, authorization constraints SHOULD be incorporated into database queries to avoid loading inaccessible resources.

---

# 62. Example

Prefer conceptually:

```text
find order
where id = ?
and company_id in caller_allowed_companies
```

rather than retrieving arbitrary data and forgetting a later ownership check.

---

# 63. Search Endpoints

Search/list endpoints MUST enforce the same resource isolation as single-resource endpoints.

---

# 64. Count Leakage

Counts, pagination metadata and existence checks can leak unauthorized information and MUST respect authorization boundaries.

---

# 65. 403 vs 404

Security policy MUST define whether inaccessible resources return:

```text
403 Forbidden
```

or:

```text
404 Not Found
```

to reduce resource-enumeration exposure where appropriate.

---

# 66. Multi-Tenancy

Multi-tenant systems MUST enforce tenant isolation at every data-access boundary.

---

# 67. Tenant Identifier

Tenant identity MUST originate from trusted security/context information.

---

# 68. Tenant Header

An arbitrary caller-provided tenant header MUST NOT become authoritative without validation against authenticated identity.

---

# 69. Segment Isolation

Where business access is segment-specific, segment isolation MUST be explicitly enforced.

---

# 70. Request Segment

A payload containing:

```text
segment = M&M
```

MUST NOT by itself prove that the caller is authorized for that segment.

---

# 71. Cross-Segment Access

Cross-segment access MUST require explicit authorization.

---

# 72. Company Isolation

Company-level access restrictions MUST be validated where applicable.

---

# 73. Region Isolation

Regional authorization MUST be enforced when business rules require it.

---

# 74. Service-to-Service Security

Internal network location MUST NOT automatically imply trust.

---

# 75. Zero Trust Principle

Conceptually:

```text
INTERNAL SERVICE
      !=
AUTOMATICALLY TRUSTED SERVICE
```

---

# 76. Machine Identity

Service-to-service communication SHOULD use explicit workload/service identity.

---

# 77. User Delegation

When downstream authorization must act on behalf of the user, the architecture MUST preserve appropriate user identity/delegation context.

---

# 78. Token Propagation

User access tokens MAY be propagated downstream when:

```text
the downstream API accepts that token,

the audience/security model permits it,

and user-level authorization is required.
```

---

# 79. Blind Token Propagation

Bearer tokens MUST NOT be forwarded indiscriminately to every downstream service.

---

# 80. Audience Boundary

A token intended only for Service A MUST NOT automatically be reused against Service B.

---

# 81. Token Exchange

OAuth token exchange or equivalent delegation mechanisms SHOULD be considered when downstream services require different audiences while preserving user delegation.

---

# 82. Client Credentials

OAuth2 Client Credentials SHOULD be used for machine-to-machine calls where no user delegation is required and the identity architecture supports it.

---

# 83. Service Account

Each service SHOULD have a distinct machine identity where practical.

---

# 84. Shared Client

Multiple unrelated applications SHOULD NOT share the same OAuth client credentials.

---

# 85. Client Secret

Client secrets MUST follow ADR-063 secret-management requirements.

---

# 86. Workload Identity

Cloud-native workloads SHOULD prefer workload/federated identity over long-lived static credentials where supported.

---

# 87. AWS

AWS integrations SHOULD prefer IAM role/workload identity mechanisms appropriate to EKS rather than embedded access keys.

---

# 88. Least Privilege

Machine identities MUST receive only permissions required by their workload.

---

# 89. Token Acquisition

Service token acquisition MUST use bounded:

```text
Connection Timeout

Response Timeout

Retry

Concurrency
```

---

# 90. Token Endpoint Failure

Identity-provider failure MUST have defined resilience behavior.

---

# 91. Token Cache

Machine access tokens SHOULD be cached until near expiration rather than reacquired for every outbound request.

---

# 92. Token Cache Key

Token caches MUST distinguish relevant:

```text
Client

Audience

Scope
```

when these affect token semantics.

---

# 93. Token Refresh Buffer

Cached tokens SHOULD refresh before expiration using a controlled safety buffer.

---

# 94. Refresh Stampede

Concurrent requests MUST NOT cause uncontrolled simultaneous token refresh.

---

# 95. Single-Flight Refresh

Token providers SHOULD coordinate refresh so one refresh can serve concurrent callers where appropriate.

---

# 96. Refresh Failure

If refresh fails but the existing token remains valid, controlled continued use MAY be appropriate until the safe expiration boundary.

---

# 97. Expired Cached Token

Expired tokens MUST NOT be returned from cache as valid credentials.

---

# 98. Token Cache Security

Token caches MUST be protected from unnecessary exposure.

---

# 99. Token Logging

Token cache diagnostics MUST NOT log token values.

---

# 100. Authentication Failure

Invalid or absent authentication SHOULD normally produce:

```text
401 Unauthorized
```

---

# 101. Authorization Failure

An authenticated caller lacking required authorization SHOULD normally produce:

```text
403 Forbidden
```

subject to resource-hiding policy.

---

# 102. Error Detail

Authentication/authorization errors MUST NOT expose sensitive internal validation details.

---

# 103. Invalid Signature Response

Do not return details such as cryptographic key internals to clients.

---

# 104. Security Logging

Security-relevant failures SHOULD be observable without logging credentials.

---

# 105. Security Event

Examples include:

```text
Invalid Token

Expired Token

Forbidden Operation

Repeated Authorization Failure

Privilege Change
```

according to audit/security policy.

---

# 106. Authentication Logging

Routine successful authentication SHOULD NOT generate excessive per-request log noise if infrastructure telemetry already provides sufficient visibility.

---

# 107. Brute Force

Authentication infrastructure SHOULD detect/rate-limit repeated abusive authentication attempts according to platform capabilities.

---

# 108. CORS

CORS is a browser security policy, not an authentication mechanism.

---

# 109. Allowed Origins

Production CORS MUST explicitly define trusted origins where browser cross-origin access is required.

---

# 110. Wildcard CORS

This combination requires strong scrutiny:

```text
Access-Control-Allow-Origin: *
```

especially when credentials are involved.

---

# 111. CORS Least Privilege

Allowed:

```text
Origins

Methods

Headers
```

SHOULD be minimized.

---

# 112. CORS Environment

CORS origins MAY be environment-specific externalized configuration.

---

# 113. CSRF

CSRF protection requirements depend on authentication mechanism.

---

# 114. Stateless Bearer API

A stateless API authenticating exclusively through bearer tokens in the `Authorization` header may disable CSRF when browser cookie-based authentication is not used.

---

# 115. Cookie Authentication

Applications using authentication cookies MUST evaluate CSRF protection explicitly.

---

# 116. Disable CSRF

`csrf.disable()` MUST NOT be copied blindly without understanding the authentication model.

---

# 117. Session

Stateless REST APIs SHOULD normally avoid server-side HTTP sessions for bearer-token authentication.

---

# 118. SessionCreationPolicy

Applicable APIs SHOULD use:

```text
SessionCreationPolicy.STATELESS
```

where the security architecture is stateless.

---

# 119. Basic Authentication

HTTP Basic Authentication SHOULD NOT be the standard user authentication mechanism for enterprise APIs.

---

# 120. API Key

API keys MAY be appropriate for selected integrations but SHOULD NOT replace identity-aware OAuth2 where stronger identity/authorization semantics are required.

---

# 121. API Key Storage

API keys MUST follow secret-management rules.

---

# 122. API Key Scope

API keys SHOULD be scoped and rotatable where supported.

---

# 123. SecurityContext

Spring Security's `SecurityContext` represents the current authenticated security context.

---

# 124. SecurityContextHolder

Application code SHOULD use standard Spring Security abstractions rather than custom global user state.

---

# 125. Static Current User

A mutable static `currentUser` is prohibited.

---

# 126. Async Execution

Security context does not automatically remain correct across arbitrary asynchronous execution boundaries.

---

# 127. Executor Propagation

Custom executors MUST explicitly preserve required security context.

---

# 128. DelegatingSecurityContext

Spring Security context-propagation utilities SHOULD be preferred over custom implementations where applicable.

---

# 129. Virtual Threads

Java 21 Virtual Threads do not eliminate authorization-context propagation requirements.

---

# 130. Virtual Thread Executor

Virtual Thread executors used for user-context operations MUST preserve the required security context.

---

# 131. Request Context

If request metadata is required asynchronously, its propagation MUST also be explicit.

---

# 132. Context Leakage

Security context MUST NOT leak from one task/request to another.

---

# 133. Cleanup

Context propagation infrastructure MUST restore/clear context correctly after execution.

---

# 134. Scheduled Job

Scheduled/background jobs MUST have an explicit machine/system identity model.

---

# 135. Fake User

Background jobs MUST NOT invent arbitrary user identities merely to satisfy authorization code.

---

# 136. System Actor

Where audit requires an actor, a controlled:

```text
SYSTEM
```

or service identity SHOULD be used.

---

# 137. Messaging

SQS processing requires explicit identity and authorization semantics.

---

# 138. User Token in Message

Long-lived asynchronous messages SHOULD NOT normally carry reusable bearer access tokens.

---

# 139. Token Expiration in Queue

An access token embedded in a queued message may expire before processing and increases credential exposure.

---

# 140. Message Identity Context

Messages SHOULD carry only the minimum identity/business context needed for processing and audit.

---

# 141. Trusted Producer

Consumers MUST NOT assume every message is trusted merely because it arrived from the broker.

---

# 142. Broker Authorization

SQS access MUST use least-privilege service identities.

---

# 143. Topic Authorization

Services SHOULD receive access only to required topics/queues.

---

# 144. Producer Authorization

Producer permissions SHOULD be separate from consumer permissions where platform capabilities support it.

---

# 145. Outbox Security

Transactional Outbox events MUST NOT persist unnecessary credentials.

---

# 146. JWT in Outbox

Bearer JWTs SHOULD NOT be stored in Outbox payloads as a general pattern.

---

# 147. Audit Actor

Outbox/audit payloads MAY store stable non-secret actor identifiers when required.

---

# 148. Privilege Escalation

Applications MUST prevent vertical and horizontal privilege escalation.

---

# 149. Vertical Escalation

Vertical escalation occurs when a lower-privileged caller gains higher-level capabilities.

Example:

```text
ANALYST
   |
   X
   |
ADMIN OPERATION
```

---

# 150. Horizontal Escalation

Horizontal escalation occurs when a caller accesses another peer's resource without permission.

Example:

```text
CUSTOMER A
    |
    X
    |
CUSTOMER B ORDER
```

---

# 151. Mass Assignment

Request binding MUST NOT allow callers to set protected security-sensitive fields merely because those fields exist in a persistence model.

---

# 152. Entity as Request

Persistence entities SHOULD NOT be used directly as public request contracts.

---

# 153. DTO

Dedicated request DTOs SHOULD expose only caller-controlled fields.

---

# 154. Administrative Fields

Fields such as:

```text
role

authority

ownerId

tenantId

approvalLevel
```

MUST NOT be caller-controlled unless explicitly required and authorized.

---

# 155. User Identifier

A request-provided user ID MUST NOT automatically override authenticated identity.

---

# 156. Acting User

When a business request legitimately identifies another user, the distinction between:

```text
AUTHENTICATED ACTOR

TARGET USER
```

MUST remain explicit.

---

# 157. Impersonation

Administrative impersonation, if supported, MUST be explicitly authorized and auditable.

---

# 158. Audit

Security-sensitive operations MUST preserve appropriate actor information.

---

# 159. Audit Fields

Applicable audit records SHOULD identify:

```text
Actor

Action

Resource

Timestamp

Result
```

without storing credentials.

---

# 160. Authorization Decision Logging

High-risk authorization decisions MAY generate audit events.

---

# 161. Sensitive Audit Data

Audit trails MUST follow privacy/data-governance requirements.

---

# 162. Authentication Availability

Identity-provider availability is a critical dependency.

---

# 163. Existing JWT

A temporary identity-provider outage SHOULD NOT necessarily invalidate already-issued locally verifiable JWTs.

---

# 164. JWKS

JWT resource servers commonly obtain signing keys from a JWKS endpoint.

---

# 165. JWKS Cache

Signing keys SHOULD be cached according to framework/provider capabilities.

---

# 166. Key Rotation

JWT validation MUST support issuer signing-key rotation.

---

# 167. Unknown `kid`

Unknown signing-key identifiers MAY trigger controlled key refresh.

---

# 168. JWKS Failure

Temporary JWKS endpoint failure SHOULD use safe framework caching semantics where valid trusted key material is already available.

---

# 169. Stale Key Forever

Signing keys MUST NOT be trusted indefinitely without an appropriate rotation strategy.

---

# 170. TLS

Identity-provider and token endpoint communication MUST validate TLS.

---

# 171. Disable TLS Verification

Production configuration MUST NOT disable TLS certificate verification.

---

# 172. Security Headers

Web applications SHOULD configure appropriate HTTP security headers according to exposure and architecture.

---

# 173. Clickjacking

Browser-facing applications SHOULD consider frame protection.

---

# 174. Content Type

APIs SHOULD use appropriate content-type protections and avoid content sniffing where applicable.

---

# 175. CSP

Browser-rendered applications SHOULD consider Content Security Policy according to frontend architecture.

---

# 176. Rate Limiting

Sensitive/high-cost endpoints SHOULD consider rate limiting according to abuse and capacity risk.

---

# 177. Authorization Is Not Rate Limiting

An authorized caller can still overload or abuse an operation.

---

# 178. Input Validation

Authentication does not eliminate input-validation requirements.

---

# 179. Trusted User Input

Authenticated input MUST still be treated as untrusted application input.

---

# 180. SQL Injection

Authentication does not protect against SQL injection.

---

# 181. SSRF

Authentication does not protect against SSRF.

---

# 182. XSS

Authentication does not protect against XSS.

---

# 183. Defense Layers

Security requires:

```text
AUTHENTICATION
       +
AUTHORIZATION
       +
INPUT VALIDATION
       +
OUTPUT SAFETY
       +
SECRET MANAGEMENT
       +
TRANSPORT SECURITY
       +
AUDIT
```

---

# 184. Security Failure Behavior

Authorization dependencies MUST fail safely.

---

# 185. Fail Closed

If a mandatory authorization decision cannot be completed safely, access SHOULD be denied.

---

# 186. Authorization Cache

Authorization decisions MAY be cached only when revocation/freshness semantics are explicitly understood.

---

# 187. Role Cache

Long-lived caching of user permissions can delay revocation and MUST be governed accordingly.

---

# 188. Authorization Source

The source of truth for authorization data MUST be explicit.

---

# 189. Distributed Authorization

Duplicating independently managed permission rules across many services SHOULD be avoided.

---

# 190. Central Policy Engine

A centralized policy engine MAY be considered for highly complex cross-service authorization, but it introduces availability and latency trade-offs.

---

# 191. Local Enforcement

Even with centralized policy definition, enforcement MUST occur at the protected resource boundary.

---

# 192. Security Configuration

Security configuration MUST be externalized only where runtime variability is legitimate.

---

# 193. Dynamic Security Weakening

Critical security controls MUST NOT be casually weakened through ordinary feature flags.

---

# 194. Least Privilege Configuration

Production default permissions SHOULD be minimal.

---

# 195. Admin Role

Administrator roles SHOULD NOT be assigned as a convenience for integration failures.

---

# 196. Service Admin

Service identities SHOULD NOT receive administrator privileges merely to avoid permission configuration.

---

# 197. Wildcard Permission

Wildcard permissions SHOULD be minimized.

---

# 198. Credential Rotation

OAuth clients, API keys and machine credentials MUST support controlled rotation.

---

# 199. Rotation Audit

Credential rotation SHOULD be auditable.

---

# 200. Security Tests

Authorization behavior MUST have automated tests.

---

# 201. Authentication Test

Tests SHOULD verify:

```text
Missing Token

Invalid Token

Expired Token

Valid Token
```

at appropriate integration boundaries.

---

# 202. Authorization Test

Tests MUST verify both:

```text
ALLOW

DENY
```

paths.

---

# 203. Role Test

Role-based operations SHOULD test every materially distinct permitted/forbidden role.

---

# 204. Resource Ownership Test

Tests MUST verify that authorized users cannot access unauthorized resources by changing identifiers.

---

# 205. BOLA Test

Critical resource endpoints SHOULD have explicit BOLA/IDOR tests.

---

# 206. Segment Test

Segment-restricted operations SHOULD test:

```text
Allowed Segment

Different Segment

Missing Segment Authority
```

---

# 207. Company Test

Company-restricted operations SHOULD test cross-company denial.

---

# 208. Method Security Test

Method-security policies SHOULD be tested independently where they contain business-sensitive authorization.

---

# 209. Security Context Propagation Test

Custom async/Virtual Thread executors MUST test required SecurityContext propagation.

---

# 210. Context Leakage Test

Concurrency tests SHOULD verify one caller's SecurityContext cannot leak into another task.

---

# 211. Token Provider Test

Machine-token providers SHOULD test:

```text
Initial Token Acquisition

Cached Token

Refresh

Concurrent Refresh

Provider Failure

Expired Token
```

---

# 212. Token Logging Test

Security tests SHOULD verify credentials do not appear in diagnostic output where custom sanitization exists.

---

# 213. Test JWT

Tests SHOULD use synthetic test tokens or supported security-test abstractions.

---

# 214. Production Token in Test

Real production bearer tokens MUST NOT be committed into tests.

---

# 215. Spring Security Test

Spring Security testing support SHOULD be used where appropriate.

---

# 216. AssertJ

Java security tests SHOULD follow established testing conventions, including meaningful AssertJ:

```java
.as("...")
```

descriptions.

---

# 217. Security Architecture Tests

Architecture fitness functions SHOULD enforce stable security rules where practical.

---

# 218. Security Review Checklist

Material changes SHOULD evaluate:

```text
[ ] Is authentication required?

[ ] Is the endpoint explicitly authorized?

[ ] Are JWT issuer/signature/expiration validated?

[ ] Is audience validation required?

[ ] Are authorities derived from trusted claims?

[ ] Is request userProfile being mistaken for authority?

[ ] Is resource ownership validated?

[ ] Can changing an ID expose another resource?

[ ] Is tenant/segment/company isolation enforced?

[ ] Is method security needed?

[ ] Does downstream communication need user delegation?

[ ] Is blind token propagation occurring?

[ ] Could client credentials use workload identity instead?

[ ] Are tokens cached safely?

[ ] Can token refresh stampede?

[ ] Are tokens present in logs?

[ ] Is CORS minimal?

[ ] Is CSRF configuration appropriate?

[ ] Does async execution preserve SecurityContext?

[ ] Could SecurityContext leak between tasks?

[ ] Are both ALLOW and DENY paths tested?
```

---

# 219. Security Fitness Functions

Stable security requirements SHOULD be automated where practical.

Examples:

```text
[ ] Protected controllers are not globally permitAll

[ ] Resource server validation is enabled

[ ] No custom insecure JWT decoder

[ ] No bearer token logging

[ ] No refresh token in SQS payloads

[ ] No production access keys in source

[ ] No unrestricted wildcard CORS

[ ] Stateless APIs do not create unnecessary sessions

[ ] Sensitive methods have authorization policies

[ ] SecurityContext propagation tests exist for custom executors

[ ] Cross-resource authorization tests exist

[ ] Service credentials use approved secret storage
```

---

# 220. Enterprise Security Gate

A service is not considered production compliant when applicable conditions include:

```text
[ ] JWT is decoded without signature validation

[ ] Issuer validation is absent

[ ] Expired tokens are accepted

[ ] Audience is ignored despite required audience isolation

[ ] Protected endpoints are permitAll

[ ] Request payload role controls security authority

[ ] Resource ownership is not validated

[ ] Cross-tenant access is possible

[ ] Cross-segment access is unintentionally possible

[ ] Bearer tokens appear in logs

[ ] Refresh tokens are propagated through microservices

[ ] User tokens are stored in Outbox messages

[ ] Service credentials are committed to Git

[ ] Machine identities have unnecessary admin privileges

[ ] SecurityContext leaks between asynchronous tasks

[ ] Authorization has no negative tests

[ ] Production TLS verification can be disabled
```

---

# 221. Anti-Patterns

The following are prohibited or strongly discouraged:

- custom JWT parsing instead of standard resource-server validation
- trusting a JWT because it can be decoded
- accepting unsigned tokens
- ignoring issuer
- ignoring expiration
- blindly ignoring audience semantics
- using ID tokens as arbitrary API access tokens
- accepting role/authority directly from request payload
- broad `permitAll()` to resolve security bugs
- authorization only at UI level
- authorization only in API Gateway when resource-level rules exist
- checking role but not resource ownership
- trusting request tenant/segment without identity validation
- sharing administrator credentials among services
- propagating bearer tokens to unrelated services
- obtaining a new client-credentials token on every request
- token refresh stampedes
- logging JWTs
- storing JWTs in SQS/Outbox payloads
- long-lived static AWS access keys in applications
- global mutable current-user state
- losing SecurityContext in custom asynchronous execution
- leaking SecurityContext between tasks
- inventing fake users for scheduled jobs
- disabling CSRF without understanding the authentication model
- wildcard CORS without justification
- using authentication as a substitute for input validation
- caching permissions indefinitely
- using feature flags to disable authentication/authorization
- assigning admin privileges merely to fix integration problems

---

# 222. Positive Consequences

The decision provides:

- standardized authentication
- consistent JWT validation
- stronger authorization
- resource-level isolation
- BOLA/IDOR protection
- segment/company isolation
- safer service-to-service communication
- controlled token propagation
- secure machine identities
- reduced credential exposure
- correct SecurityContext propagation
- stronger negative security testing
- improved auditability

---

# 223. Negative Consequences

The decision introduces:

- authorization-policy complexity
- additional security tests
- identity-provider dependency
- machine-token management
- resource-ownership validation
- token lifecycle management
- context-propagation requirements
- additional integration governance

These costs are accepted because authorization defects can create direct unauthorized access to enterprise data and operations.

---

# 224. Neutral Consequences

The decision also means:

- not every internal call needs user-token propagation
- not every service-to-service call uses the same OAuth flow
- not every authorization decision is pure RBAC
- not every resource should reveal whether it exists
- not every background operation has a human actor
- JWT does not provide immediate revocation automatically
- CORS and CSRF solve different security problems
- authenticated users remain untrusted input sources

---

# 225. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Authentication bypass | Critical | Low | Standard Resource Server |
| Privilege escalation | Critical | Medium | RBAC/ABAC + tests |
| BOLA/IDOR | Critical | Medium | Resource-level authorization |
| Cross-tenant leakage | Critical | Low/Medium | Tenant isolation |
| Token leakage | Critical | Medium | Sanitization + no logging |
| Token refresh stampede | High | Medium | Coordinated cache |
| IdP outage | High | Medium | Local JWT validation/caching |
| Excessive service privilege | Critical | Medium | Least privilege |
| SecurityContext leakage | Critical | Low | Context cleanup + tests |
| Authorization drift | High | Medium | Centralized policy components |

---

# 226. Implementation Guidance

The following rules are mandatory:

1. Applicable APIs must use approved OAuth2/OIDC authentication.
2. Spring Security 6 Resource Server should validate bearer JWTs.
3. JWT signature, issuer and expiration must be validated.
4. Audience must be validated where required by token architecture.
5. Unsigned JWTs must never authorize protected APIs.
6. ID tokens must not be used indiscriminately as access tokens.
7. Authentication and authorization must remain separate concerns.
8. Protected APIs should deny access unless explicitly authorized.
9. Public endpoints must be explicitly defined.
10. Security authorities must derive from trusted security claims.
11. Request payload roles/profiles must not become security authorities.
12. Resource-level authorization must prevent BOLA/IDOR.
13. Search endpoints must preserve the same isolation as resource endpoints.
14. Tenant, segment, company and region boundaries must be enforced where applicable.
15. Complex authorization should use reusable policy components.
16. Service-to-service calls must use explicit machine/user identity semantics.
17. Bearer tokens must not be blindly propagated.
18. Client Credentials should be used where machine identity without user delegation is appropriate.
19. Cloud workloads should prefer workload identity over static credentials.
20. Machine access tokens should be cached safely.
21. Token refresh must occur before expiration.
22. Concurrent token refresh must be controlled.
23. Credentials and tokens must never be logged.
24. Refresh tokens must not routinely traverse backend services.
25. Bearer tokens should not be stored in asynchronous message payloads.
26. CORS must follow least privilege.
27. CSRF configuration must reflect the actual authentication mechanism.
28. Stateless bearer APIs should remain stateless where appropriate.
29. Custom asynchronous execution must preserve required SecurityContext.
30. SecurityContext must never leak between tasks.
31. Background processing must use an explicit machine/system actor model.
32. Authorization failures must fail safely.
33. Authorization caches must define freshness/revocation semantics.
34. Security-sensitive operations must be auditable.
35. Authorization must have both positive and negative automated tests.
36. Critical resource APIs must include BOLA/IDOR tests.
37. Custom Virtual Thread executors must test SecurityContext propagation.
38. Production TLS verification must not be disabled.

---

# 227. Validation

This ADR will be validated through:

- Java 21
- Spring Boot 4.1
- Spring Security 6
- OAuth2 Resource Server
- OpenID Connect
- Keycloak
- JWT
- JWKS
- AWS IAM
- EKS workload identity
- Kubernetes
- JUnit 5
- Spring Security Test
- AssertJ
- Mockito
- integration tests
- architecture tests
- SonarQube
- SAST
- DAST where applicable
- secret scanning
- security audit events

---

# 228. Success Criteria

The decision is successful when:

- protected endpoints consistently require valid identities
- invalid/expired tokens are rejected
- authorization no longer depends on caller-supplied role fields
- cross-customer/resource access is prevented
- segment/company isolation is consistently enforced
- service identities have minimal privileges
- machine tokens are reused safely instead of reacquired per request
- tokens do not appear in application logs
- asynchronous execution preserves the correct authenticated context
- SecurityContext does not leak between requests/tasks
- negative authorization tests exist for critical operations
- production credentials remain outside source control

---

# 229. Alternatives Rejected

## 229.1 Custom JWT Parser

Rejected because standard Spring Security validation is safer and more maintainable.

---

## 229.2 Trust All Internal Network Calls

Rejected because network location does not prove workload identity or authorization.

---

## 229.3 Role from Request Payload

Rejected because caller-controlled business data cannot establish security authority.

---

## 229.4 RBAC Without Resource Checks

Rejected because it does not prevent horizontal authorization failures.

---

## 229.5 Forward User Token Everywhere

Rejected because token audiences, privilege boundaries and exposure differ between services.

---

## 229.6 Acquire Client Token Per Request

Rejected because it creates unnecessary latency, load and IdP dependency.

---

## 229.7 Static Cloud Credentials

Rejected where workload identity is available because rotation and exposure risks are higher.

---

# 230. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard

---

# 231. References

- OAuth 2.0
- OAuth 2.0 Security Best Current Practice
- OpenID Connect Core
- JWT / JOSE Standards
- Spring Security Documentation
- Spring Security OAuth2 Resource Server
- Keycloak Documentation
- OWASP Authentication Cheat Sheet
- OWASP Authorization Cheat Sheet
- OWASP API Security Top 10
- OWASP JWT Cheat Sheet
- NIST Digital Identity Guidelines
- AWS IAM Best Practices
- Kubernetes Security Documentation
- Zero Trust Architecture Principles

---

# 232. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-25 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise authentication and authorization baseline |

---

# 233. Decision Summary

The security boundary becomes:

```text
REQUEST
   |
   v
AUTHENTICATION
   |
   +--> SIGNATURE
   +--> ISSUER
   +--> AUDIENCE
   +--> EXPIRATION
   |
   v
AUTHENTICATED IDENTITY
   |
   v
AUTHORIZATION
   |
   +--> ROLE
   +--> PERMISSION
   +--> RESOURCE
   +--> SEGMENT
   +--> COMPANY
   +--> CONTEXT
   |
   v
ALLOW / DENY
```

JWT processing becomes:

```text
JWT
 |
 v
VERIFY SIGNATURE
 |
 v
VERIFY ISSUER
 |
 v
VERIFY AUDIENCE
 |
 v
VERIFY EXPIRATION
 |
 v
MAP TRUSTED CLAIMS
 |
 v
AUTHORIZATION
```

not:

```text
JWT
 |
 v
BASE64 DECODE
 |
 v
TRUST
```

Resource authorization becomes:

```text
AUTHENTICATED USER
        |
        v
orders:read
        |
        v
ORDER EXISTS?
        |
        v
CALLER MAY ACCESS
THIS ORDER?
        |
     +--+--+
     |     |
    YES    NO
     |     |
     v     X
   RETURN DENY
```

Segment isolation becomes:

```text
REQUEST
segment=M&M
    |
    v
AUTHENTICATED CALLER
    |
    v
AUTHORIZED FOR M&M?
    |
  +-+-+
  |   |
 YES  NO
  |   |
  v   X
PROCESS DENY
```

Service-to-service security becomes:

```text
SERVICE A
    |
    +--> USER DELEGATION REQUIRED?
    |          |
    |        YES
    |          |
    |          v
    |     APPROPRIATE
    |     DELEGATED TOKEN
    |
    +--> NO
             |
             v
       MACHINE IDENTITY
             |
             v
      CLIENT CREDENTIALS
          OR WORKLOAD
           IDENTITY
```

Token caching becomes:

```text
REQUEST
   |
   v
TOKEN CACHE
   |
 +---+----------------+
 |                    |
VALID             NEAR EXPIRY
 |                    |
 v                    v
USE              SINGLE REFRESH
                      |
                      v
                  CACHE TOKEN
```

Asynchronous security becomes:

```text
REQUEST THREAD
     |
     v
SECURITY CONTEXT
     |
     v
CONTROLLED PROPAGATION
     |
     v
VIRTUAL THREAD / EXECUTOR
     |
     v
BUSINESS OPERATION
     |
     v
CLEAR / RESTORE CONTEXT
```

Messaging becomes:

```text
USER REQUEST
    |
    v
BUSINESS EVENT
    |
    +--> actorId
    +--> correlationId
    +--> required context
    |
    X
NO REUSABLE BEARER TOKEN
    |
    v
SQS
```

The complete security equation is:

```text
TRUSTED IDENTITY
       +
VALIDATED TOKEN
       +
LEAST PRIVILEGE
       +
RBAC
       +
ABAC
       +
RESOURCE AUTHORIZATION
       +
TENANT / SEGMENT ISOLATION
       +
SECURE SERVICE IDENTITY
       +
CONTROLLED TOKEN PROPAGATION
       +
SECURE TOKEN LIFECYCLE
       +
CONTEXT PROPAGATION
       +
AUDIT
       +
NEGATIVE SECURITY TESTING
       =
ENTERPRISE ACCESS CONTROL
```

The governing principle is:

```text
Never trust a token
because it can be decoded.

Validate who issued it,
who it is for,
and whether it is still valid.

Never confuse authentication
with authorization.

Never trust a role supplied
inside an ordinary request.

Authorize the resource,
not merely the endpoint.

Protect against both vertical
and horizontal privilege escalation.

Treat tenant, segment and company
boundaries as security boundaries
when the business requires them.

Do not trust services merely
because they are inside the cluster.

Use explicit machine identities.

Propagate user credentials only
when delegation genuinely requires it.

Do not put bearer tokens
into asynchronous business events.

Cache machine tokens safely
instead of requesting one
for every call.

Preserve SecurityContext across
controlled asynchronous execution.

Never allow context to leak
between requests.

Test who may enter.

Test who must be denied.

And enforce authorization
at the boundary that owns
the protected resource.
```
