# ADR-023: Adopt API Security Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-023 |
| Title | Adopt API Security Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | API Security, Authentication, Authorization and Application Security |
| Related Work Items | OAuth 2.0, OpenID Connect, JWT, Keycloak, Spring Security, OWASP, SAST, DAST |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform exposes REST APIs and asynchronous integration capabilities across independently deployable services.

The platform includes:

- Java 21
- Spring Boot
- Spring Security
- OAuth 2.0
- OpenID Connect
- JWT
- Keycloak
- REST APIs
- OpenAPI
- Apache Kafka
- PostgreSQL
- Redis
- Kubernetes
- Resilience4j
- OpenTelemetry
- structured logging
- CI/CD
- SAST
- dependency scanning
- container scanning

APIs may be consumed by:

- frontend applications
- mobile applications
- internal microservices
- batch applications
- integration platforms
- administrative applications
- external systems

The platform processes business-sensitive information and performs operations capable of modifying authoritative business state.

Security therefore cannot be implemented solely at:

```text
Network perimeter

or

API Gateway
```

Every service must participate in the security model.

---

# 2. Problem Statement

The platform requires a standardized API security model that:

- authenticates callers
- authorizes business operations
- supports human and machine identities
- supports service-to-service communication
- propagates identity safely
- applies least privilege
- prevents broken object-level authorization
- prevents broken function-level authorization
- prevents mass assignment
- validates untrusted input
- protects credentials and tokens
- standardizes JWT validation
- integrates with Keycloak
- controls CORS
- handles CSRF appropriately
- supports rate limiting
- protects sensitive data
- prevents security information leakage
- provides security auditing
- supports SAST
- supports DAST
- supports dependency scanning
- integrates with observability
- supports zero-downtime deployment
- remains compatible with API contract governance

---

# 3. Decision Drivers

Primary decision drivers are:

1. confidentiality
2. integrity
3. authentication
4. authorization
5. least privilege
6. defense in depth
7. service independence
8. traceability
9. OWASP API Security alignment
10. secure defaults
11. centralized identity
12. distributed enforcement
13. auditability
14. credential protection
15. automation
16. zero-downtime deployment
17. maintainability

---

# 4. Decision

The Enterprise Order Platform adopts a defense-in-depth API security model based on:

```text
OAuth 2.0

+

OpenID Connect

+

JWT

+

Keycloak

+

Spring Security

+

Domain Authorization

+

Least Privilege

+

OWASP API Security Controls

+

Automated Security Testing
```

Authentication is centralized through the approved identity platform.

Authorization remains enforced by every resource-owning service.

---

# 5. Fundamental Principle

The platform adopts:

```text
Never trust merely because the request
came from inside the network.
```

Internal network location is not authorization.

A caller must possess an appropriate identity and permission for the requested operation.

---

# 6. Defense in Depth

Security controls exist at multiple layers:

```text
Client

↓

Edge / Gateway

↓

Identity Provider

↓

Kubernetes / Network

↓

Spring Security

↓

API Authorization

↓

Application / Domain Authorization

↓

Persistence

↓

Audit / Observability
```

No single layer is considered sufficient by itself.

---

# 7. Authentication vs Authorization

Authentication answers:

```text
Who is the caller?
```

Authorization answers:

```text
Is this caller permitted to perform
this operation on this resource?
```

These concerns must remain distinct.

---

# 8. Authentication

Protected APIs require a verified authenticated identity unless explicitly classified as public.

Authentication must occur before protected business operations are executed.

---

# 9. Authorization

Authentication alone never grants unrestricted application access.

Every protected capability requires authorization appropriate to:

- operation
- resource
- user
- service
- organization
- business context

where applicable.

---

# 10. Identity Provider

Keycloak is the approved identity provider for the platform where it is part of the enterprise authentication architecture.

Keycloak provides capabilities such as:

- OAuth 2.0
- OpenID Connect
- authentication
- token issuance
- client identities
- roles
- claims
- session management

Application services remain responsible for resource authorization.

---

# 11. OAuth 2.0

OAuth 2.0 is used for delegated API access and machine-to-machine authorization.

Applications must use approved OAuth flows appropriate to the client type.

---

# 12. OpenID Connect

OpenID Connect is used when authentication and user identity are required on top of OAuth 2.0.

---

# 13. Authorization Code Flow

Interactive applications should use:

```text
Authorization Code Flow
```

with PKCE where appropriate.

---

# 14. Resource Owner Password Grant

The Resource Owner Password Credentials grant is prohibited for new implementations.

Applications must not collect user passwords merely to exchange them for tokens.

---

# 15. Implicit Flow

The OAuth implicit flow is prohibited for new implementations.

---

# 16. Client Credentials

Machine-to-machine integrations may use:

```text
Client Credentials
```

when the operation represents the service identity itself.

---

# 17. User Delegation

A service must not replace the user's identity with its own service identity when downstream authorization requires knowledge of the original user.

---

# 18. Service Identity vs User Identity

The platform distinguishes:

```text
User identity
```

from:

```text
Service identity
```

They must not be treated as interchangeable.

---

# 19. JWT

JWT access tokens may be used for stateless API authentication.

Each resource server must validate the token rather than trusting decoded token contents blindly.

---

# 20. JWT Validation

JWT validation must include appropriate verification of:

- signature
- issuer
- expiration
- not-before where applicable
- audience where required
- token type where applicable

Additional claims may be validated according to application requirements.

---

# 21. JWT Signature

A token whose signature cannot be validated must be rejected.

---

# 22. JWT Expiration

Expired tokens must be rejected.

Applications must not extend access-token validity locally.

---

# 23. JWT Issuer

The expected token issuer must be configured explicitly.

A valid signature from an untrusted issuer is insufficient.

---

# 24. JWT Audience

Audience validation must be applied where the identity architecture uses audience-restricted access tokens.

A token issued for an unrelated resource must not automatically grant access.

---

# 25. Algorithm Validation

Applications must accept only approved JWT signature algorithms.

The algorithm declared by untrusted token data must not determine unrestricted verification behavior.

---

# 26. Unsigned JWT

Unsigned JWT access tokens are prohibited.

---

# 27. JWT Claims

Claims are untrusted until token validation succeeds.

Application code must not:

```text
decode JWT

↓

read role

↓

authorize
```

without cryptographic and semantic token validation.

---

# 28. JWT Payload

JWT payloads are encoded, not inherently encrypted.

Sensitive information must not be placed in tokens merely because they are JWTs.

---

# 29. Token Lifetime

Access-token lifetime should be limited according to enterprise identity policy.

Long-lived bearer tokens increase compromise impact.

---

# 30. Refresh Tokens

Refresh tokens must be handled only by components authorized to possess them.

Backend resource services should not receive refresh tokens unless explicitly required by the authentication architecture.

---

# 31. Bearer Token

A bearer token grants authority to whoever possesses it.

Therefore bearer tokens are secrets.

---

# 32. Token Logging

The following are prohibited:

```text
log.info("token={}", token);

log.debug("Authorization={}", authorizationHeader);
```

Tokens must not appear in:

- logs
- traces
- metrics
- error responses
- audit messages
- URLs

---

# 33. Authorization Header

The `Authorization` header must be treated as sensitive.

HTTP logging filters must redact or exclude it.

---

# 34. Token Propagation

When downstream processing requires the original user's authorization context, the approved token or delegated identity context may be propagated.

Propagation must be explicit.

---

# 35. Blind Token Forwarding

Blindly forwarding incoming tokens to every downstream service is prohibited.

A downstream service should receive the identity context only when:

- required
- trusted
- permitted by audience and identity architecture

---

# 36. Service-to-Service Authentication

Internal services must authenticate to protected downstream services.

Internal network location is insufficient.

---

# 37. Service Credentials

Service credentials must be:

- unique where practical
- scoped
- rotated
- stored securely
- excluded from source control

---

# 38. Shared Service Credential

A single enterprise-wide client credential shared by unrelated services is prohibited.

It destroys meaningful identity and least privilege.

---

# 39. Least Privilege

Every identity must receive the minimum authority necessary to perform its responsibilities.

This applies to:

- users
- services
- CI/CD
- database users
- Kubernetes service accounts
- cloud identities
- Kafka identities
- administrative tools

---

# 40. Roles

Roles represent meaningful collections of permissions.

They must not become an uncontrolled list of UI-specific flags.

---

# 41. Scopes

OAuth scopes may represent permissions delegated to clients.

Scopes should remain:

- understandable
- stable
- bounded

---

# 42. Roles vs Scopes

Roles and scopes serve related but distinct purposes.

The platform must not assume:

```text
role = scope
```

in every context.

---

# 43. Domain Authorization

Business authorization often requires more than checking a token role.

Example:

```text
User has ORDER_APPROVER role
```

does not necessarily mean:

```text
User may approve every order.
```

The application may also need to validate:

- segment
- company
- customer
- region
- workflow stage
- order ownership
- approval level

---

# 44. Resource-Level Authorization

Authorization must be evaluated against the requested resource where required.

---

# 45. BOLA

Broken Object Level Authorization is explicitly addressed.

Example vulnerable endpoint:

```text
GET /orders/{orderId}
```

The service must not assume that possession of a valid `orderId` means the authenticated caller may access that order.

---

# 46. BOLA Prevention

For protected resources:

```text
Load or identify resource

↓

Determine caller authorization context

↓

Verify access to that resource

↓

Return resource
```

---

# 47. Identifier Security

UUID identifiers do not provide authorization.

The fact that an identifier is difficult to guess does not make the resource protected.

---

# 48. IDOR

Insecure Direct Object Reference vulnerabilities must be prevented through authorization, not identifier obscurity.

---

# 49. BFLA

Broken Function Level Authorization must also be prevented.

Example:

```text
POST /admin/orders/reprocess
```

must not rely merely on the frontend hiding the button.

---

# 50. Backend Enforcement

Authorization must be enforced by the backend.

Frontend authorization is a usability feature, not a security boundary.

---

# 51. Method Security

Spring Security method authorization may be used where appropriate.

Examples include:

```java
@PreAuthorize(...)
```

However, complex domain rules should not become unreadable expressions embedded throughout controllers.

---

# 52. Authorization Service

Complex authorization logic should be encapsulated in dedicated application/domain authorization components.

Example:

```java
orderAuthorizationService.requireApprovalPermission(
        user,
        order
);
```

---

# 53. Deny by Default

Protected operations should follow:

```text
deny by default
```

Access must be explicitly granted.

---

# 54. Authorization Failure

An authenticated caller without permission should normally receive:

```text
403 Forbidden
```

subject to resource-concealment policy.

---

# 55. Resource Concealment

For selected resources, returning:

```text
404 Not Found
```

instead of:

```text
403 Forbidden
```

may prevent unauthorized resource enumeration.

This must be a deliberate policy.

---

# 56. Field-Level Authorization

Some callers may be authorized to access a resource but not every field.

Example:

```text
Customer
```

may contain information restricted to specific roles.

---

# 57. Field Filtering

Field-level authorization must be implemented explicitly.

It must not depend on clients voluntarily ignoring fields.

---

# 58. Write Authorization

Field-level authorization applies to writes as well as reads.

A caller allowed to update:

```text
contactPhone
```

must not automatically be allowed to modify:

```text
creditLimit
```

---

# 59. Mass Assignment

Mass assignment vulnerabilities must be prevented.

---

# 60. Mass Assignment Example

Unsafe conceptual model:

```java
Customer entity = mapper.from(request);
repository.save(entity);
```

when the request can populate internal fields such as:

- status
- role
- approval level
- audit data
- ownership
- internal flags

---

# 61. Dedicated Request DTOs

External request DTOs must expose only fields callers are permitted to submit.

Do not deserialize external requests directly into persistence entities.

---

# 62. Explicit Mapping

Sensitive state transitions should use explicit mapping.

Example:

```java
customer.changePhone(request.phone());
```

rather than unrestricted property copying.

---

# 63. Reflection-Based Property Copying

Generic reflection-based property copying across security boundaries is discouraged.

It can unintentionally expose new fields when models evolve.

---

# 64. Input Trust Boundary

All external input is untrusted.

This includes:

- body
- path variables
- query parameters
- headers
- cookies
- uploaded files
- JWT claims after structural parsing but before token validation
- webhook payloads
- Kafka events from external trust zones

---

# 65. Input Validation

Input must be validated according to intended business semantics.

Validation may include:

- required fields
- maximum length
- numeric range
- allowed values
- format
- collection size
- cross-field rules
- resource existence
- authorization

---

# 66. Allowlist Validation

Where a bounded set of values is expected, allowlist validation is preferred.

Example:

```text
segment ∈ {AUTO, MOTO, M&M}
```

rather than accepting arbitrary strings and attempting to sanitize them later.

---

# 67. Validation vs Sanitization

Validation and sanitization are not interchangeable.

Preferred:

```text
Reject invalid input
```

rather than:

```text
Accept anything

↓

Mutate it generically

↓

Hope it becomes safe
```

---

# 68. Business Data Integrity

Security controls must not corrupt legitimate business data.

For example:

```text
M&M
```

must not become:

```text
M&amp;M
```

inside a JSON business contract merely because generic HTML escaping was applied.

---

# 69. Context-Specific Encoding

Output encoding must match the destination context.

Examples:

```text
HTML
JavaScript
SQL
JSON
URL
Shell
```

have different security requirements.

There is no universal string-escaping function that makes arbitrary data safe everywhere.

---

# 70. JSON Output

For JSON APIs, legitimate strings should be serialized by the JSON framework.

HTML encoding must not be indiscriminately applied to normal business values.

---

# 71. SQL Injection

SQL must use parameterized access.

Unsafe:

```java
"SELECT * FROM orders WHERE id = '" + id + "'"
```

---

# 72. JPA Parameters

JPA queries must bind parameters rather than concatenate untrusted values.

---

# 73. Dynamic Sorting

Dynamic sort properties must use an allowlist.

Untrusted input must not directly become:

- SQL fragments
- JPQL fragments
- database identifiers

---

# 74. Dynamic Query Construction

Complex dynamic queries must use controlled query-building mechanisms.

User-provided query fragments are prohibited.

---

# 75. NoSQL Injection

The same principle applies to:

- Redis commands
- Elasticsearch queries
- document databases
- search engines

where applicable.

---

# 76. Command Injection

Untrusted input must never be concatenated into operating-system commands.

---

# 77. SSRF

Server-Side Request Forgery must be prevented.

APIs accepting URLs must not automatically allow access to arbitrary:

- internal hosts
- metadata services
- localhost
- private network ranges

without explicit business justification and controls.

---

# 78. URL Allowlist

When the application must call user-influenced destinations, use controlled destination allowlists or equivalent validated routing.

---

# 79. Open Redirect

Redirect destinations influenced by users must be validated.

---

# 80. File Upload

File-upload endpoints require controls for:

- size
- type
- filename
- storage location
- malware scanning where required
- decompression behavior
- authorization

---

# 81. Content-Type Trust

The client-provided content type alone must not be considered authoritative for security-sensitive file handling.

---

# 82. Filename

Uploaded filenames must not directly determine filesystem paths.

---

# 83. Path Traversal

User-controlled path data must not permit traversal such as:

```text
../../
```

into unintended resources.

---

# 84. Request Size

The platform must enforce bounded request sizes.

This protects against:

- memory exhaustion
- parser abuse
- oversized bulk requests

---

# 85. Collection Size

Collection inputs must define maximum sizes.

Example:

```text
orders <= 100
```

according to endpoint semantics.

---

# 86. String Length

Externally supplied strings must have reasonable maximum lengths.

This applies even when the database column can store substantially more data.

---

# 87. Regex

Complex regular expressions must be reviewed for catastrophic backtracking where user-controlled input is processed.

---

# 88. Deserialization

JSON deserialization must use controlled DTO types.

Unsafe polymorphic deserialization of untrusted types is prohibited.

---

# 89. Unknown JSON Fields

Unknown-field handling should follow ADR-022 compatibility requirements.

Ignoring unknown fields may support backward compatibility but must not enable mass assignment because request DTOs remain explicitly bounded.

---

# 90. Error Handling

Security-related errors must follow the standardized API error contract from ADR-022.

---

# 91. Error Leakage

Error responses must not expose:

- stack traces
- SQL
- secrets
- tokens
- passwords
- internal filesystem paths
- Kubernetes details
- cloud credentials
- private hostnames
- implementation class names

---

# 92. Authentication Error

Authentication failure should provide enough information for the client to understand that authentication failed without revealing unnecessary validation internals.

---

# 93. User Enumeration

Authentication and account-related APIs must avoid unnecessary user enumeration.

Example:

```text
User exists but password wrong
```

versus:

```text
User does not exist
```

should not be distinguishable unless business requirements explicitly require it.

---

# 94. Logging

Security-relevant events must use structured logging according to ADR-019.

---

# 95. Sensitive Logging

Logs must not contain:

- passwords
- access tokens
- refresh tokens
- API keys
- client secrets
- private keys
- session identifiers where sensitive
- complete payment credentials

---

# 96. Log Masking

When a sensitive value may legitimately appear in diagnostic input, logging infrastructure must redact it.

Masking must target sensitive data rather than generically mutate all business strings.

---

# 97. Log Injection

Externally supplied strings must not be allowed to forge log structure.

Structured logging is preferred over concatenated free-form logs.

---

# 98. Correlation ID

Correlation IDs must be validated and bounded before inclusion in logs.

They remain diagnostic identifiers, not authorization credentials.

---

# 99. Security Audit

Security audit records are distinct from ordinary application logs.

Audit records capture security or business accountability events.

---

# 100. Audit Events

Relevant audit events may include:

- authentication failure
- authorization denial
- privileged operation
- role or permission change
- critical configuration change
- approval
- cancellation
- sensitive data access
- administrative operation

---

# 101. Audit Record

An audit record should include controlled information such as:

- actor
- action
- resource
- timestamp
- result
- correlation identifier
- relevant business context

without exposing secrets.

---

# 102. Audit Immutability

Audit records should be protected against unauthorized modification or deletion according to enterprise audit requirements.

---

# 103. Audit Failure

Failure to write an audit record for a security-critical operation must have an explicitly defined behavior.

The platform must not silently lose mandatory audit evidence.

---

# 104. CORS

Cross-Origin Resource Sharing must be configured intentionally.

---

# 105. CORS Allowlist

Production authenticated APIs should use explicit allowed origins.

Wildcard origin configuration is prohibited where credentials or sensitive operations are involved unless specifically justified.

---

# 106. CORS Is Not Authentication

CORS is a browser-enforced cross-origin policy.

It does not protect APIs from:

- curl
- backend clients
- malicious services
- direct HTTP calls

Authentication and authorization remain mandatory.

---

# 107. CORS Methods

Only required HTTP methods should be allowed for browser cross-origin access.

---

# 108. CORS Headers

Only required request headers should be allowed.

Avoid unrestricted configuration without justification.

---

# 109. CSRF

CSRF protection depends on the authentication model.

---

# 110. Stateless Bearer APIs

For APIs authenticated exclusively through bearer tokens supplied explicitly in the `Authorization` header and not automatically attached by the browser as ambient credentials, traditional CSRF risk is substantially different from cookie-authenticated applications.

CSRF configuration may therefore differ.

---

# 111. Cookie Authentication

Applications using browser cookies for authentication must explicitly evaluate and mitigate CSRF.

Disabling CSRF blindly is prohibited.

---

# 112. SameSite

Authentication cookies should use appropriate `SameSite` behavior where cookies are part of the architecture.

---

# 113. Secure Cookies

Sensitive cookies must use:

```text
Secure
```

and:

```text
HttpOnly
```

where appropriate.

---

# 114. TLS

Production API traffic must use TLS.

Sensitive bearer credentials must not traverse plaintext networks.

---

# 115. TLS Verification

HTTP clients must validate server certificates.

Disabling certificate verification in production is prohibited.

---

# 116. Trust-All Client

Configurations equivalent to:

```text
trust all certificates
```

must never be used in production.

---

# 117. Hostname Verification

TLS hostname verification must remain enabled.

---

# 118. Internal TLS

Internal traffic should use enterprise-approved transport security according to platform infrastructure standards.

---

# 119. Rate Limiting

Rate limiting should protect APIs against:

- accidental abuse
- runaway clients
- brute-force behavior
- resource exhaustion

---

# 120. Rate Limit Dimensions

Rate limits may be applied by:

- client
- user
- service identity
- IP
- endpoint
- tenant

depending on threat model and architecture.

---

# 121. Rate Limit Response

Rate limiting should use:

```text
429 Too Many Requests
```

with `Retry-After` where appropriate.

---

# 122. Rate Limiting Is Not Authorization

Rate limiting does not replace authentication or authorization.

---

# 123. Brute-Force Protection

Authentication-sensitive operations require controls against brute-force attacks.

Where authentication is centralized, the identity platform should provide the primary protection.

---

# 124. Resource Exhaustion

Application endpoints must be designed to prevent unbounded resource consumption.

Controls include:

- pagination
- bulk limits
- request-size limits
- timeouts
- concurrency limits
- rate limits

---

# 125. Expensive Queries

Authenticated users must not automatically be permitted to execute arbitrarily expensive searches.

Search capabilities require bounded query semantics.

---

# 126. Timeouts

Outbound HTTP clients must use bounded:

- connection timeout
- response timeout
- acquisition timeout

according to ADR-016 resilience practices.

---

# 127. Security and Retries

Retries must not amplify attacks or duplicate sensitive operations.

Non-idempotent operations require explicit retry safety.

---

# 128. Circuit Breaker

Circuit breakers protect service reliability.

They are not security controls, but they can reduce cascading impact from compromised or malfunctioning dependencies.

---

# 129. Secrets

Secrets include:

- passwords
- client secrets
- API keys
- private keys
- certificates with private material
- database credentials
- signing keys

---

# 130. Source Control

Secrets must never be committed to source control.

---

# 131. Application Configuration

Production secrets must not be stored as plaintext values in ordinary application configuration committed to the repository.

---

# 132. Secret Management

Secrets must be obtained through the approved secret-management mechanism.

Examples may include:

- cloud secret manager
- enterprise vault
- Kubernetes-integrated secret mechanism

according to infrastructure standards.

---

# 133. Environment Variables

Environment variables may expose secrets to process and operational tooling.

Their use must follow platform security policy.

They are not automatically secure merely because they are not in source code.

---

# 134. Secret Rotation

Secrets must be rotatable without requiring source-code changes.

---

# 135. Zero-Downtime Secret Rotation

Where the dependency permits, credential rotation should support:

```text
Old credential valid

+

New credential valid

↓

Deploy new credential

↓

Verify

↓

Revoke old credential
```

This supports ADR-021.

---

# 136. Secret Caching

Applications may cache credentials only according to the secret-management architecture and must respect rotation requirements.

---

# 137. Secret Logging

Secret retrieval failures must not log the retrieved secret value.

---

# 138. Database Security

Each service should use its own database identity where practical.

---

# 139. Database Least Privilege

Application database users should receive only required permissions.

An application that only accesses its own schema should not receive broad cluster administration rights.

---

# 140. Database Ownership

Database schema ownership must follow bounded-context ownership.

Cross-service direct table access is prohibited unless explicitly approved.

---

# 141. Flyway Identity

Database migration credentials may require broader privileges than runtime credentials.

Where practical, migration and runtime identities should be separated.

---

# 142. Flyway Security

Flyway migrations must not embed production secrets.

Applied migration immutability from ADR-021 remains mandatory.

---

# 143. Kafka Security

Kafka access must follow least privilege.

Producers and consumers should receive permissions only for required topics and operations.

---

# 144. Kafka Authorization

A service producing to:

```text
order-events
```

must not automatically receive unrestricted access to every platform topic.

---

# 145. Kafka Event Trust

An event received from Kafka must not automatically be considered authorized merely because it came from Kafka.

Trust depends on:

- topic governance
- producer identity
- ACLs
- event validation
- integration boundary

---

# 146. Event Validation

Consumed integration events must be validated according to ADR-018.

Malformed or incompatible events require controlled handling.

---

# 147. Kubernetes Security

Workloads must use appropriately scoped Kubernetes identities.

---

# 148. Service Accounts

Services should use dedicated service accounts where platform infrastructure requires workload identity.

---

# 149. Kubernetes RBAC

Kubernetes permissions must follow least privilege.

Application pods should not receive Kubernetes API permissions unless required.

---

# 150. Privileged Containers

Application containers must not run privileged unless explicitly approved for an infrastructure-specific requirement.

---

# 151. Root User

Application containers should run as non-root where practical and supported by the runtime image.

---

# 152. Read-Only Filesystem

A read-only root filesystem should be used where application requirements permit.

Required writable locations should be explicit.

---

# 153. Linux Capabilities

Unnecessary Linux capabilities should be dropped.

---

# 154. Container Image

Container images must use approved base images and receive vulnerability scanning.

---

# 155. Image Provenance

Artifact provenance and signing should be adopted according to the software supply-chain standard.

---

# 156. Dependency Security

Third-party dependencies are part of the attack surface.

---

# 157. Dependency Scanning

CI/CD must scan dependencies for known vulnerabilities according to enterprise policy.

---

# 158. Vulnerability Severity

Vulnerability handling must consider:

- severity
- exploitability
- exposure
- available remediation
- business criticality

A CVSS number alone is not the complete risk decision.

---

# 159. Dependency Pinning

Dependency versions must remain controlled and reproducible.

Unbounded dynamic production dependency versions are prohibited.

---

# 160. Framework Updates

Security-supported versions of:

- Java
- Spring Boot
- Spring Security
- database drivers
- Kafka clients

must be maintained.

---

# 161. SAST

Static Application Security Testing is mandatory in CI/CD according to enterprise policy.

---

# 162. SAST Findings

SAST findings must be:

```text
Investigated

↓

Classified

↓

Corrected or formally accepted
```

They must not be silenced merely to make the pipeline green.

---

# 163. Correct Security Boundary

SAST remediation must occur at the correct trust boundary.

Example:

```text
Unsafe input enters SQL construction
```

should be fixed with:

```text
parameterized query
```

not by globally altering every returned business string.

---

# 164. SAST Suppression

Suppressions require:

- technical justification
- narrow scope
- review
- traceability

Broad suppression of security rules is prohibited.

---

# 165. DAST

Dynamic Application Security Testing should be applied to production-representative deployed APIs according to risk classification.

---

# 166. DAST Scope

DAST should test areas such as:

- authentication
- authorization
- injection
- malformed requests
- security headers
- exposed endpoints
- information leakage

---

# 167. API Security Tests

Critical APIs require explicit automated security tests.

---

# 168. Authentication Tests

Tests must verify:

```text
No token
→ rejected

Invalid token
→ rejected

Expired token
→ rejected

Valid token
→ authenticated
```

as applicable.

---

# 169. Authorization Tests

Tests must verify:

```text
Authenticated but unauthorized
→ denied
```

and not merely the happy path.

---

# 170. BOLA Tests

Tests should verify:

```text
User A can access Order A

User A cannot access unauthorized Order B
```

even when the caller knows Order B's identifier.

---

# 171. Role Tests

Every privileged endpoint should include tests for both:

- allowed role/context
- denied role/context

---

# 172. Mass Assignment Tests

Security tests should attempt to submit fields the caller must not control.

The API must either:

- not expose the field in the request DTO

or

- reject unauthorized modification

---

# 173. Validation Tests

Tests should cover:

- oversized strings
- invalid enum values
- oversized collections
- malformed identifiers
- invalid ranges
- cross-field validation

---

# 174. Error Leakage Tests

Tests should verify that internal exceptions do not expose sensitive implementation information.

---

# 175. Token Leakage Tests

Tests should verify that tokens are absent from:

- logs where testable
- error responses
- exception messages

---

# 176. Security Test Quality

Security tests must follow established Java test quality standards.

Example:

```java
assertThat(response.getStatusCode())
        .as("HTTP status returned for unauthorized access")
        .isEqualTo(HttpStatus.FORBIDDEN);
```

---

# 177. Deterministic Tests

Security tests must use deterministic identities and identifiers where practical.

Random values should not make failures difficult to reproduce.

---

# 178. Sonar

Security-related code and tests must comply with Sonar quality requirements.

Security fixes must not introduce avoidable maintainability defects.

---

# 179. OpenAPI Security Schemes

OpenAPI contracts must document authentication schemes.

Example:

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

---

# 180. Endpoint Security Documentation

Public and protected endpoints must be distinguishable from the contract.

---

# 181. Security Contract Compatibility

Changing an endpoint from:

```text
public
```

to:

```text
authenticated
```

is a contract change and requires compatibility analysis according to ADR-022.

---

# 182. Authorization Tightening

Changing authorization requirements may break existing consumers.

Security necessity may justify the change, but migration and incident response must still be considered.

---

# 183. Security Overrides Compatibility

Security takes precedence when an active vulnerability requires immediate remediation.

The change must still be:

- documented
- communicated
- traceable
- tested

---

# 184. Security Headers

Platform ingress or application layers should apply appropriate security headers where relevant to the client context.

Browser-specific headers are not automatically meaningful for machine-only APIs.

---

# 185. Cache-Control

Sensitive responses must use appropriate cache controls.

Confidential user-specific information must not be unintentionally cached by shared intermediaries.

---

# 186. Sensitive URL Data

Sensitive data should not appear in URLs.

URLs may be captured in:

- access logs
- proxies
- monitoring
- browser history
- referrer information

---

# 187. Query Parameters

Credentials and high-sensitivity secrets must not be transmitted through query parameters.

---

# 188. API Keys

Where legacy or external integrations require API keys, they must be:

- treated as secrets
- transmitted securely
- scoped where possible
- rotatable
- never logged

OAuth-based mechanisms remain preferred for modern integrations.

---

# 189. Webhooks

Inbound webhooks require explicit authenticity validation.

Possible controls include:

- signed payload
- HMAC
- mTLS
- OAuth
- approved source validation

depending on the provider capability.

---

# 190. Webhook Replay

Security-sensitive webhooks should protect against replay where provider protocol permits.

Possible inputs include:

- timestamp
- nonce
- event identifier
- signature

---

# 191. Webhook Idempotency

Webhook processing must be idempotent because legitimate providers may retry delivery.

---

# 192. Outbound Webhooks

Outbound webhook destinations must be controlled to prevent SSRF and data exfiltration.

---

# 193. Sensitive Data

Sensitive information must be classified according to enterprise data-governance standards.

Security controls depend on classification.

---

# 194. Data Minimization

APIs should expose only information required for the business capability.

---

# 195. Excessive Data Exposure

Returning an entire object and expecting the frontend to hide sensitive fields is prohibited.

---

# 196. DTO Projection

Response DTOs must explicitly control exposed fields.

---

# 197. Encryption at Rest

Sensitive persistent data must use enterprise-approved encryption-at-rest controls where required.

---

# 198. Encryption in Transit

Sensitive data must use encrypted transport.

---

# 199. Password Storage

If an application ever owns password credentials, passwords must use approved adaptive password hashing.

Plaintext or reversible password storage is prohibited.

Centralized identity means business services should normally not store user passwords.

---

# 200. PII Logging

Personally identifiable information should not be logged unless required and explicitly governed.

When necessary, logging should minimize or mask the value.

---

# 201. Observability Security

Observability systems themselves are sensitive systems.

Access to:

- logs
- traces
- dashboards
- audit data

must be controlled.

---

# 202. Trace Attributes

Sensitive request data must not be copied indiscriminately into span attributes.

---

# 203. Metric Labels

Sensitive values and unbounded user identifiers must not be metric labels.

---

# 204. Security Metrics

Recommended bounded security metrics include:

- authentication failures
- authorization denials
- rate-limit rejections
- invalid-token count
- security-validation failures

with carefully controlled dimensions.

---

# 205. Alerting

Security alerts should focus on actionable signals.

Examples:

- unusual authentication failures
- authorization-denial spikes
- abnormal privileged activity
- secret-access anomalies
- high rate-limit rejection
- suspicious webhook signature failures

---

# 206. Security Event Correlation

Security investigations should be able to correlate:

```text
Audit event

+

Application log

+

Trace

+

Deployment version
```

without placing secrets into telemetry.

---

# 207. Zero-Downtime Security Changes

Security configuration changes must support ADR-021 where practical.

Examples include:

- signing-key rotation
- certificate rotation
- client-secret rotation
- role transition
- scope transition

---

# 208. JWT Signing-Key Rotation

Resource servers must support normal identity-provider signing-key rotation.

Applications should rely on approved key-discovery mechanisms rather than hardcoding a single permanent signing key.

---

# 209. JWKS

Where OpenID Connect/JWT architecture uses JWKS, resource servers should obtain verification keys through the approved discovery mechanism.

---

# 210. JWKS Caching

Verification keys may be cached, but rotation must remain supported.

---

# 211. Key Rotation Failure

Temporary inability to refresh signing keys must be handled carefully.

The service must not:

```text
disable signature verification
```

as a fallback.

---

# 212. Certificate Rotation

Certificate replacement should permit overlap where infrastructure supports it.

---

# 213. Role Migration

Removing or renaming a role requires analysis of:

- users
- clients
- applications
- downstream authorization

Role changes are integration changes.

---

# 214. Security Configuration Versioning

Security configuration must be version controlled where appropriate.

Manual undocumented production security configuration is prohibited.

---

# 215. Feature Flags

Feature flags must not be used to bypass mandatory authorization.

---

# 216. Security Feature Flag

Temporary security controls behind feature flags require explicit risk review.

Default behavior should fail securely.

---

# 217. Fail Secure

When security state cannot be determined, protected operations should normally deny access rather than grant access.

---

# 218. Authorization Dependency Failure

If authorization requires an external policy service and that service is unavailable, the default for protected operations should be:

```text
deny
```

unless a formally approved availability/security trade-off exists.

---

# 219. Cached Authorization

Authorization decisions may be cached only when:

- semantics permit
- TTL is bounded
- revocation implications are understood
- cache keys correctly represent security context

---

# 220. Authorization Cache Key

A cache must not accidentally reuse an authorization decision across different:

- users
- roles
- tenants
- resources

---

# 221. Privileged Operations

Highly privileged operations may require additional controls such as:

- stronger authentication
- explicit audit
- restricted network access
- separation of duties
- approval workflow

depending on enterprise policy.

---

# 222. Separation of Duties

Where business risk requires it, the same actor should not both initiate and approve a critical operation.

This rule belongs to domain authorization.

---

# 223. Administrative Endpoints

Administrative endpoints must not be exposed through ordinary public routing unless required.

---

# 224. Actuator Security

Spring Boot Actuator endpoints must be explicitly secured.

---

# 225. Health Endpoints

Health endpoints should expose only the information required by infrastructure.

They must not reveal unnecessary dependency or configuration details publicly.

---

# 226. Environment Endpoint

Actuator endpoints exposing:

- environment
- configuration
- beans
- mappings

must not be publicly accessible.

---

# 227. Metrics Endpoint

Metrics endpoints must follow platform access controls.

They may reveal operational information useful to attackers.

---

# 228. Swagger / API Documentation

Production API documentation exposure must follow enterprise policy.

If exposed, documentation must not reveal secrets or privileged internal endpoints to unauthorized audiences.

---

# 229. Debug Endpoints

Temporary debug endpoints are prohibited in production unless explicitly controlled and approved.

---

# 230. Default Credentials

Default credentials are prohibited.

---

# 231. Test Credentials

Development or test credentials must not be valid in production.

---

# 232. Production Profile

Production security must not depend solely on developers remembering to enable a profile manually.

Secure production configuration must be automated.

---

# 233. Security Misconfiguration

The platform explicitly addresses security misconfiguration through:

- secure defaults
- infrastructure as code
- configuration review
- automated scanning
- immutable deployments

---

# 234. Dependency Failure Messages

Downstream authentication failures must be translated into the provider's controlled error model.

Raw Keycloak or external-service responses must not be forwarded blindly.

---

# 235. Availability vs Security

Security controls must be engineered for availability, but availability pressure must not result in bypassing authentication or authorization.

---

# 236. Break-Glass Access

Emergency privileged access, if supported, must be:

- exceptional
- time bounded
- strongly authenticated
- audited
- reviewed afterward

---

# 237. Security Incident Response

Critical services must have procedures for:

- credential compromise
- token abuse
- secret leakage
- unauthorized access
- vulnerable dependency
- signing-key issue
- malicious API traffic

---

# 238. Credential Compromise

If a credential is compromised:

```text
Revoke / rotate

↓

Contain access

↓

Identify affected systems

↓

Review audit evidence

↓

Remediate

↓

Validate
```

---

# 239. Token Revocation

JWT access tokens are commonly self-contained and may remain valid until expiration.

Token lifetime and identity-provider revocation capabilities must be considered in incident-response design.

---

# 240. Security Patch

Critical security patches may require expedited deployment.

ADR-021 zero-downtime and compatibility principles still apply where technically possible.

---

# 241. Threat Modeling

Critical new capabilities should undergo threat modeling.

---

# 242. Threat Model Questions

Threat modeling should consider:

```text
What are we protecting?

Who can call this?

What input is untrusted?

What happens if an identifier is changed?

Can one user access another user's data?

Can the operation be replayed?

Can a request cause excessive resource usage?

Can secrets leak?

Can downstream services be abused?

Can the caller modify fields they should not control?
```

---

# 243. STRIDE

STRIDE may be used as a structured threat-modeling technique:

```text
Spoofing

Tampering

Repudiation

Information Disclosure

Denial of Service

Elevation of Privilege
```

---

# 244. OWASP API Security

API design and review must consider the current OWASP API Security Top 10 categories applicable to the service.

Particular attention is required for:

- Broken Object Level Authorization
- Broken Authentication
- Broken Object Property Level Authorization
- Unrestricted Resource Consumption
- Broken Function Level Authorization
- Unrestricted Access to Sensitive Business Flows
- Server Side Request Forgery
- Security Misconfiguration
- Improper Inventory Management
- Unsafe Consumption of APIs

---

# 245. Sensitive Business Flows

Some legitimate API operations can be abused even with valid authentication.

Examples:

- mass order creation
- repeated quotation
- bulk export
- repeated notification
- expensive report generation

Protection may require:

- quotas
- rate limits
- business limits
- anomaly detection

---

# 246. API Inventory

Every production API must have identifiable ownership and lifecycle according to ADR-022.

Unknown or forgotten production endpoints are security risks.

---

# 247. Deprecated API Security

Deprecated endpoints remain security responsibilities until removed.

They must continue receiving:

- vulnerability fixes
- authentication
- authorization
- monitoring

throughout their supported lifetime.

---

# 248. Shadow APIs

Undocumented alternate endpoints bypassing normal governance are prohibited.

---

# 249. Old API Versions

Older supported API versions must maintain equivalent required security controls.

A legacy version must not remain accessible with weaker authentication merely for compatibility.

---

# 250. Security Review

A security review is required for changes involving:

- authentication
- authorization
- sensitive data
- new external exposure
- file upload
- webhook
- dynamic outbound URL
- privileged operations
- cryptography
- secrets
- administrative APIs

---

# 251. Cryptography

Application teams must use established cryptographic libraries and platform standards.

Custom cryptographic algorithms are prohibited.

---

# 252. Randomness

Security-sensitive random values must use cryptographically secure randomness.

---

# 253. Encryption Keys

Encryption keys must not be hardcoded in source code.

---

# 254. Hashing

General-purpose fast hashes such as:

```text
MD5

SHA-1
```

must not be used for password storage.

---

# 255. Security Quality Gate

A production release must not proceed with unresolved critical security findings unless an explicit formal risk acceptance exists.

---

# 256. CI/CD Security

The deployment pipeline itself must use least privilege.

CI/CD identities should receive only the access necessary for their deployment responsibilities.

---

# 257. Pipeline Secrets

Pipeline secrets must use the approved secret store.

They must not appear in build logs.

---

# 258. Pull Request Security

Security-sensitive changes require appropriate code review.

No individual should bypass protected-branch controls merely because a change is urgent.

---

# 259. Artifact Integrity

Production deployment must use immutable artifacts according to ADR-021.

Security validation applies to the same artifact promoted to production.

---

# 260. SBOM

Software Bill of Materials should be generated according to supply-chain security requirements.

---

# 261. Container Scanning

Container images must undergo vulnerability scanning before production promotion.

---

# 262. IaC Scanning

Infrastructure-as-Code should undergo security scanning where supported.

Examples include:

- Kubernetes manifests
- Helm configuration
- Terraform

---

# 263. Secret Scanning

Repositories and pipelines should use automated secret detection.

---

# 264. Security Test Pyramid

Security verification should include complementary techniques:

```text
Unit Security Tests

↓

Integration Security Tests

↓

SAST

↓

Dependency Scan

↓

Container Scan

↓

DAST

↓

Penetration Testing where required
```

No single technique replaces the others.

---

# 265. Penetration Testing

High-risk externally exposed capabilities should receive penetration testing according to enterprise security policy.

---

# 266. False Positives

Security tooling may generate false positives.

A finding may be classified as false positive only with documented technical evidence.

---

# 267. False Negative

Passing SAST does not prove an application is secure.

Architecture and authorization defects such as BOLA may not be detected by static analysis.

---

# 268. Security Documentation

Critical services should document:

- authentication model
- authorization model
- privileged roles
- sensitive data
- service credentials
- external trust boundaries
- incident procedures

---

# 269. Security Runbook

Critical services should maintain operational procedures for:

- token validation failure
- Keycloak outage
- secret rotation
- certificate rotation
- compromised credential
- abnormal authorization failures
- dependency vulnerability
- API abuse

---

# 270. Keycloak Outage

Applications must define expected behavior if identity infrastructure becomes temporarily unavailable.

Already validated self-contained access tokens may continue to be validated locally when cryptographic material is available and token validity permits.

New authentication flows may fail.

---

# 271. Security Dependency Cascades

A temporary identity dependency problem must not cause unnecessary application restart storms.

Liveness must follow ADR-021 principles.

---

# 272. Security and Readiness

A service should become unready only when it cannot safely perform its required capability.

External identity-provider reachability must not automatically be placed into liveness.

---

# 273. Security Telemetry Cardinality

Security telemetry must avoid unbounded labels such as:

- userId
- tokenId
- customerId
- IP address

in metrics.

Such information may belong in controlled logs or audit systems when permitted.

---

# 274. Data Retention

Security logs and audit records must follow enterprise retention and privacy policies.

---

# 275. Right to Access

Access to security logs must itself be authorized and auditable where required.

---

# 276. Non-Repudiation

For business operations requiring strong accountability, audit evidence must identify the authenticated actor and operation reliably.

---

# 277. Impersonation

Administrative impersonation, if supported, must be explicit and auditable.

Audit must preserve both:

```text
actual actor
```

and:

```text
effective identity
```

---

# 278. Delegation

Delegated operations must preserve enough identity context to determine:

- who initiated the action
- which service performed it
- under which authority

---

# 279. Async Identity

When an authenticated HTTP operation produces an asynchronous Kafka workflow, required actor information may need to be captured in the event or durable business/audit state.

---

# 280. Token in Event

Bearer access tokens must not be placed in Kafka events merely to preserve identity.

---

# 281. Actor Context

Events may contain controlled identity references such as:

```text
actorId
requestUser
correlationId
```

when required by business and audit semantics.

They must not contain authentication secrets.

---

# 282. Saga Security

Saga steps must independently enforce the permissions relevant to their trust boundary where required.

An upstream service saying:

```text
authorized=true
```

must not universally replace downstream authorization.

---

# 283. Outbox Security

Transactional Outbox payloads must not contain unnecessary secrets.

Outbox tables are durable data stores and may be visible to operational personnel.

---

# 284. Replay Security

Replaying an outbox event must not accidentally bypass authorization assumptions that were valid only during the original HTTP request.

Authorization semantics for asynchronous processing must be explicit.

---

# 285. Security Boundaries

The platform recognizes the following major trust boundaries:

```text
Internet / User Device

↓

Edge

↓

Application Services

↓

Persistence

↓

Messaging

↓

External Enterprise Services

↓

Third-Party Services
```

Crossing each boundary requires explicit trust analysis.

---

# 286. Third-Party APIs

Third-party API responses are untrusted input.

They must be:

- validated
- bounded
- safely parsed
- translated into local models

---

# 287. Unsafe API Consumption

The platform explicitly addresses unsafe consumption of APIs.

A trusted vendor does not justify blindly trusting:

- response size
- JSON shape
- redirects
- URLs
- error content

---

# 288. Redirect Following

HTTP clients should not automatically follow arbitrary redirects for security-sensitive integrations without understanding destination trust.

---

# 289. Remote Error Content

Remote error messages must be treated as untrusted data.

They may be logged only through controlled sanitization/redaction appropriate to logging context.

---

# 290. Remote Error to Client

Remote errors must be translated into stable local API error codes according to ADR-022.

---

# 291. Anti-Patterns

The following are prohibited:

- trusting requests because they originate inside the network
- treating authentication as sufficient authorization
- trusting UUID secrecy as access control
- checking authorization only in the frontend
- allowing arbitrary resource access after role validation
- exposing JPA entities as request models
- unrestricted mass assignment
- generic property copying into privileged domain state
- blindly forwarding bearer tokens
- sharing one client credential across unrelated services
- logging access tokens
- logging refresh tokens
- putting tokens in URLs
- putting bearer tokens into Kafka events
- hardcoding secrets
- committing secrets to source control
- using trust-all TLS
- disabling hostname verification
- using wildcard CORS indiscriminately for authenticated APIs
- disabling CSRF without evaluating authentication semantics
- concatenating untrusted SQL
- accepting arbitrary dynamic sort/query fragments
- unrestricted SSRF-capable URL calls
- returning stack traces to clients
- exposing downstream error payloads directly
- using global HTML escaping as generic API security
- corrupting legitimate business data such as `M&M`
- assuming SAST alone proves security
- suppressing security findings without justification
- allowing unlimited request or collection sizes
- exposing sensitive data and expecting frontend hiding
- exposing privileged Actuator endpoints publicly
- using default production credentials
- manually bypassing security controls during incidents
- failing open when authorization cannot be determined
- treating feature flags as authorization controls
- storing business-critical authorization solely in UI state
- using outdated API versions with weaker security
- placing secrets in traces or metric labels
- granting application pods unnecessary Kubernetes privileges
- running privileged containers without justification
- using custom cryptography
- assuming a trusted third-party API response is inherently safe

---

# 292. Positive Consequences

The decision provides:

- standardized authentication
- centralized identity
- distributed authorization
- stronger least privilege
- BOLA protection
- BFLA protection
- mass-assignment protection
- safer service-to-service communication
- improved secret handling
- consistent JWT validation
- better security observability
- controlled auditing
- stronger API input validation
- reduced sensitive-data leakage
- automated security verification
- alignment with OWASP API Security
- safer zero-downtime credential rotation
- improved incident response
- stronger supply-chain security

---

# 293. Negative Consequences

The decision introduces:

- additional authorization code
- more security tests
- token-validation configuration
- role/scope governance
- credential lifecycle management
- audit storage
- security scanning
- more CI/CD gates
- threat-modeling effort
- security review requirements
- possible latency from authorization checks

These costs are accepted because security is a fundamental platform requirement rather than an optional infrastructure feature.

---

# 294. Neutral Consequences

The decision also means:

- not every authenticated user can access every resource
- frontend permissions duplicate some backend authorization for UX purposes
- some service calls require user identity while others require service identity
- security fixes may occasionally require compatibility-impacting changes
- old API versions remain security responsibilities until retired
- some authorization logic remains domain-specific
- security audit and application logging remain separate concerns
- SAST findings require engineering analysis rather than mechanical changes

---

# 295. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Valid user accesses another user's resource | Critical | Medium | Resource-level authorization and BOLA tests |
| Privileged endpoint lacks authorization | Critical | Low | Deny-by-default and BFLA tests |
| JWT accepted without complete validation | Critical | Low | Spring Security resource-server validation |
| Token leaks into logs | Critical | Medium | Header redaction and security tests |
| Shared service credential is compromised | Critical | Medium | Per-service identity and least privilege |
| Mass assignment changes privileged field | Critical | Medium | Dedicated request DTOs and explicit mapping |
| SQL injection | Critical | Low | Parameterized queries |
| SSRF reaches internal infrastructure | Critical | Medium | Destination validation and allowlists |
| Secret committed to repository | Critical | Medium | Secret scanning and secure stores |
| CORS exposes browser capability | High | Medium | Explicit origin allowlist |
| CSRF incorrectly disabled | High | Medium | Authentication-model analysis |
| SAST fix corrupts business data | High | Medium | Correct trust-boundary remediation |
| Security error exposes internals | High | Medium | Standard error translation |
| Authorization dependency fails open | Critical | Low | Fail-secure behavior |
| Excessive resource consumption | High | Medium | Rate and request limits |
| Vulnerable dependency reaches production | Critical | Medium | Dependency and container scanning |
| Signing-key rotation breaks APIs | High | Low | JWKS/discovery support |
| Credential rotation causes downtime | High | Medium | Overlapping credential transition |
| Old API version has weaker security | Critical | Low | Equivalent security throughout support lifecycle |
| Audit evidence is lost | High | Low | Durable governed audit mechanism |
| Third-party API returns malicious data | High | Medium | Treat remote responses as untrusted |

---

# 296. Implementation Guidance

The following rules are mandatory:

1. Protected APIs require authenticated identities.
2. Authentication and authorization must remain separate concerns.
3. Keycloak/OIDC/OAuth 2.0 are used according to the approved identity architecture.
4. JWT tokens must undergo cryptographic and semantic validation.
5. Expired tokens must be rejected.
6. Token issuer must be validated.
7. Audience must be validated where required.
8. Unsigned tokens are prohibited.
9. Bearer tokens must never be logged.
10. Internal network location must never replace authentication.
11. Service-to-service integrations require an approved identity.
12. Service identities must follow least privilege.
13. User and service identities must remain distinguishable.
14. Resource-level authorization is mandatory where resource ownership or scope applies.
15. UUID secrecy must never be treated as authorization.
16. Privileged functions require backend authorization.
17. Protected operations should deny by default.
18. Field-level authorization must be explicit where required.
19. External request DTOs must expose only permitted writable fields.
20. JPA entities must not be external request models.
21. Generic mass assignment into privileged state is prohibited.
22. External input must be validated.
23. SQL must use parameterized queries.
24. Dynamic sorting/filtering must use controlled allowlists.
25. User-controlled outbound destinations require SSRF controls.
26. Security error responses must not expose internal implementation.
27. Legitimate JSON business values must not be globally HTML-escaped.
28. CORS must use explicit production policy.
29. CSRF configuration must match the authentication model.
30. Production traffic must use TLS.
31. Trust-all TLS configurations are prohibited.
32. Request sizes and collection sizes must be bounded.
33. Rate limiting must be applied where threat and business models require it.
34. Secrets must never be committed to source control.
35. Production secrets must use the approved secret-management mechanism.
36. Credentials must be rotatable.
37. Database access must follow least privilege.
38. Kafka access must follow least privilege.
39. Kubernetes workload identities must follow least privilege.
40. Production containers should run non-root where practical.
41. Dependencies and container images must undergo vulnerability scanning.
42. SAST is mandatory according to enterprise policy.
43. SAST findings must be remediated at the correct security boundary.
44. DAST must be applied according to application risk.
45. Critical APIs require authentication and authorization tests.
46. BOLA tests are required for resource-scoped critical APIs.
47. Mass-assignment tests are required for sensitive mutation APIs.
48. OpenAPI must document security schemes.
49. Security-related contract changes must follow ADR-022.
50. Credential and signing-key changes must support ADR-021 where practical.
51. Security telemetry must avoid secrets and high-cardinality metric labels.
52. Audit records must preserve required actor accountability.
53. Bearer tokens must not be stored in asynchronous events.
54. Third-party API responses must be treated as untrusted input.
55. Critical new capabilities must undergo threat modeling according to risk.

---

# 297. Validation

The decision will be validated through:

- authentication tests
- JWT validation tests
- authorization tests
- BOLA tests
- BFLA tests
- field-level authorization tests
- mass-assignment tests
- SQL injection tests
- SSRF tests
- request-limit tests
- CORS tests
- CSRF tests where applicable
- error-leakage tests
- token-leakage tests
- secret scanning
- SAST
- DAST
- dependency scanning
- container scanning
- IaC scanning
- OpenAPI security validation
- credential-rotation exercises
- signing-key rotation tests
- threat modeling
- penetration testing where required
- security review
- production-readiness review

---

# 298. Success Criteria

The decision is successful when:

- protected APIs consistently authenticate callers
- authorization is enforced by resource-owning services
- users cannot access unauthorized resources by changing identifiers
- privileged functions cannot be invoked without required authority
- external requests cannot mutate internal privileged fields
- service identities use least privilege
- tokens and secrets do not appear in application telemetry
- JWT key rotation occurs without application downtime
- legitimate business data is preserved by security controls
- critical SAST findings are resolved before release
- dependency vulnerabilities are governed
- API security behavior is represented in OpenAPI
- security changes remain compatible with zero-downtime deployment
- security incidents can be correlated through audit and observability
- deprecated APIs retain required security until retirement
- security controls remain automated and reproducible

---

# 299. Alternatives Rejected

## 299.1 Perimeter-Only Security

Rejected because internal network location does not establish user or service authorization.

---

## 299.2 Authentication-Only Security

Rejected because a valid identity may still attempt unauthorized resource or function access.

---

## 299.3 Frontend Authorization

Rejected as a security boundary because clients can call APIs directly.

---

## 299.4 UUIDs as Access Control

Rejected because identifier unpredictability is not authorization.

---

## 299.5 Shared Internal Service Credential

Rejected because compromise creates excessive blast radius and destroys caller attribution.

---

## 299.6 Direct Entity Binding

Rejected because it enables accidental mass assignment and couples external contracts to persistence.

---

## 299.7 Generic String Sanitization

Rejected because security is context-specific and generic mutation can corrupt legitimate business data without fixing the actual vulnerability.

---

## 299.8 Trust-All TLS

Rejected because it defeats server authentication and enables man-in-the-middle attacks.

---

## 299.9 SAST as the Only Security Control

Rejected because static analysis cannot reliably identify all authorization, business-logic and runtime vulnerabilities.

---

## 299.10 Fail-Open Authorization

Rejected because availability problems must not silently grant protected access.

---

# 300. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Apache Kafka for Integration Events
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-017: Adopt Optimistic Locking for Concurrent Aggregate Updates
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-022: Adopt API Contract Governance
- ADR-024: Adopt Software Supply Chain Security

---

# 301. References

- OAuth 2.0
- OpenID Connect
- JWT
- Keycloak Documentation
- Spring Security Documentation
- OWASP API Security Top 10
- OWASP Application Security Verification Standard
- OWASP Cheat Sheet Series
- OpenAPI Specification
- W3C Trace Context
- OpenTelemetry Specification
- Kubernetes Security Documentation
- Apache Kafka Security Documentation
- PostgreSQL Security Documentation
- Enterprise Order Platform Security Standards
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-022: Adopt API Contract Governance

---

# 302. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial API security architecture baseline |

---

# 303. Decision Summary

The Enterprise Order Platform adopts defense-in-depth API security based on:

```text
OAuth 2.0

+

OpenID Connect

+

JWT

+

Keycloak

+

Spring Security

+

Domain Authorization

+

Least Privilege

+

OWASP API Security

+

Automated Security Verification
```

The fundamental access model is:

```text
Authenticate Caller

↓

Validate Token

↓

Identify Actor

↓

Authorize Function

↓

Authorize Resource

↓

Authorize Sensitive Fields

↓

Validate Input

↓

Execute Business Operation

↓

Audit Where Required
```

Security does not stop at authentication.

The platform explicitly protects against:

```text
BOLA

BFLA

Mass Assignment

Injection

SSRF

Credential Leakage

Excessive Data Exposure

Resource Exhaustion

Security Misconfiguration

Unsafe API Consumption
```

A valid token means:

```text
The caller has an authenticated identity.
```

It does **not** mean:

```text
The caller may access every resource
or perform every operation.
```

Security controls must preserve legitimate business information. Context-specific security remediation is mandatory; indiscriminate transformations that corrupt values such as:

```text
M&M
```

are prohibited.

The platform therefore establishes:

```text
Centralized Identity

+

Distributed Authorization

+

Explicit Trust Boundaries

+

Automated Verification

+

Auditable Security Decisions
```

as the security foundation for all Enterprise Order Platform APIs.
