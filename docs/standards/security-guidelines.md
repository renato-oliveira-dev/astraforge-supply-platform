# Security Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Security Guidelines |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the security standards adopted by the Enterprise Order Platform.

It establishes requirements for:

- authentication
- authorization
- OAuth 2.0
- OpenID Connect
- JWT validation
- Keycloak integration
- service-to-service communication
- secrets management
- data protection
- secure coding
- input validation
- API security
- messaging security
- database security
- container security
- Kubernetes security
- dependency management
- SAST
- software composition analysis
- audit
- incident response
- vulnerability remediation
- testing
- security governance

The objective is to ensure that security is embedded throughout the software lifecycle rather than applied only after implementation.

---

# 2. Core Principles

Security must follow these principles:

- defense in depth
- least privilege
- deny by default
- secure by default
- explicit trust boundaries
- strong identity validation
- minimal data exposure
- separation of duties
- traceability
- fail securely
- continuous verification
- automated enforcement where practical

No component should trust input, identity or metadata only because it originated inside the corporate network.

---

# 3. Shared Responsibility

Security is a shared responsibility across:

- software engineering
- platform engineering
- architecture
- DevOps
- security teams
- product owners
- operations
- external integration owners

Application teams are responsible for secure implementation even when identity, network or infrastructure controls are managed by other teams.

---

# 4. Security by Design

Security requirements must be considered during:

- architecture definition
- threat modeling
- API design
- data modeling
- implementation
- testing
- deployment
- monitoring
- incident response
- decommissioning

Security review must not be postponed until production readiness.

---

# 5. Threat Modeling

Threat modeling should be performed for:

- new externally exposed APIs
- authentication flows
- payment-related workflows
- personal-data processing
- new trust boundaries
- file uploads
- administrative functions
- asynchronous workflows
- third-party integrations
- significant architecture changes

The project may use STRIDE or another recognized threat-modeling approach.

---

# 6. Trust Boundaries

The architecture must explicitly identify trust boundaries.

Examples:

```text
Browser → API Gateway

API Gateway → Backend Service

Service → Database

Service → Kafka

Service → External Provider

Kubernetes Workload → Cloud Service
```

Crossing a trust boundary requires:

- authenticated identity
- authorization
- transport protection
- validation
- logging
- failure handling

---

# 7. Authentication

Authentication establishes the identity of:

- users
- services
- workloads
- automation clients
- administrative operators

The platform must not rely on unverified headers or client-provided identifiers as proof of identity.

---

# 8. Authentication Standards

The preferred standards are:

- OAuth 2.0
- OpenID Connect
- JWT bearer tokens
- mutual TLS where required
- workload identity for cloud integrations

Custom authentication protocols are prohibited unless formally approved.

---

# 9. OpenID Connect

OpenID Connect should be used for user authentication.

The platform should validate:

- issuer
- audience
- signature
- expiration
- token usage
- authorized party where applicable
- nonce where applicable to the flow

Identity claims must come from a trusted identity provider.

---

# 10. OAuth 2.0

OAuth 2.0 should be used for delegated authorization and protected API access.

Supported flows should be selected according to client type.

Examples:

| Client | Recommended flow |
|---|---|
| Browser application | Authorization Code with PKCE |
| Native application | Authorization Code with PKCE |
| Service-to-service | Client Credentials |
| CLI or limited-input device | Device Authorization where supported |

The Resource Owner Password Credentials flow is prohibited.

---

# 11. Authorization Code with PKCE

Public clients must use PKCE.

The implementation must validate:

- authorization code
- code verifier
- redirect URI
- state
- nonce where applicable

Authorization codes must be single-use and short-lived.

---

# 12. Client Credentials

Client Credentials may be used for service identities.

Each service must use a dedicated client identity.

Avoid shared credentials such as:

```text
backend-services

internal-applications

system-user
```

Shared identities prevent reliable authorization and audit attribution.

---

# 13. JWT Validation

Every service accepting JWTs must validate:

- cryptographic signature
- trusted issuer
- expected audience
- expiration
- not-before time
- supported signing algorithm
- required scopes or authorities
- token type where applicable

A token must not be accepted only because it can be decoded.

---

# 14. JWT Algorithms

Allowed signing algorithms must be explicitly configured.

Do not accept:

```text
none
```

Do not dynamically trust the algorithm declared by an unverified token.

Prefer asymmetric algorithms for distributed services.

Examples:

```text
RS256

ES256
```

The actual allowed list must follow organizational security policy.

---

# 15. JWT Key Rotation

Services should obtain public keys from a trusted JWKS endpoint.

Key rotation must be supported without service redeployment.

The implementation should:

- cache keys
- refresh unknown key identifiers
- use bounded refresh frequency
- fail securely when keys cannot be validated

---

# 16. JWT Claims

Common claims include:

```text
iss

sub

aud

exp

iat

nbf

jti

azp

scope
```

Application-specific claims may include:

```text
tenantId

companyCode

userCode

roles

permissions
```

Claims must not be trusted outside their documented semantics.

---

# 17. Subject Identity

The `sub` claim should represent the stable authenticated subject identifier.

Business identifiers such as customer number or employee code should be modeled separately.

Do not infer authorization solely from display names or email addresses.

---

# 18. Token Expiration

Access tokens should be short-lived.

Long-lived access tokens increase exposure after compromise.

Refresh tokens must be:

- securely stored
- rotated where supported
- revoked when necessary
- unavailable to backend logs
- protected from browser script access where applicable

---

# 19. Token Propagation

User tokens should be propagated downstream only when delegated user identity is required.

Do not forward user tokens indiscriminately.

For service-owned operations, prefer a service identity with explicit authorization.

---

# 20. Token Exchange

Token exchange may be used when a downstream service requires a token specifically scoped for its audience.

The exchanged token should have:

- restricted audience
- minimal scopes
- short expiration
- preserved subject context where required

---

# 21. Keycloak Integration

Keycloak may provide:

- user authentication
- client authentication
- role mapping
- scope mapping
- token issuance
- federation
- session management

Application code must not depend on Keycloak-specific behavior when a standard OAuth 2.0 or OpenID Connect mechanism is sufficient.

---

# 22. Keycloak Realms

Realm design must consider:

- tenant isolation
- administrative boundaries
- user population
- policy separation
- operational complexity

A realm must not be created for every minor application without architectural justification.

---

# 23. Keycloak Clients

Each application or service should have a dedicated client.

Client configuration should define:

- client type
- redirect URIs
- allowed flows
- scopes
- service account use
- secret or key rotation
- logout behavior

Wildcard redirect URIs are prohibited in production.

---

# 24. Roles and Scopes

Roles represent organizational or application assignments.

Scopes and permissions represent allowed capabilities.

Examples:

```text
orders:read

orders:create

orders:approve

orders:cancel
```

Avoid relying only on broad roles such as:

```text
ADMIN

USER
```

Fine-grained capabilities provide safer authorization.

---

# 25. Authorization

Authorization must occur:

- at the API boundary
- within application use cases
- at object level
- before sensitive data access
- before state-changing operations

Endpoint-level role checks alone are insufficient for business-sensitive resources.

---

# 26. Deny by Default

Access must be denied unless explicitly authorized.

New endpoints must not become public merely because no security rule exists.

Security configuration should require authentication by default and allow anonymous access only for explicitly documented paths.

---

# 27. Method Security

Use method-level authorization where it protects application capabilities.

Example:

```java
@PreAuthorize("hasAuthority('orders:approve')")
```

Method security complements, but does not replace, object-level authorization.

---

# 28. Object-Level Authorization

Every operation involving a specific resource must verify whether the principal may access that resource.

Validation may include:

- tenant
- customer
- company
- business unit
- ownership
- assignment
- region
- profile
- workflow authority

This protects against Broken Object Level Authorization.

---

# 29. Tenant Isolation

Tenant identity must come from a trusted authenticated claim or trusted gateway context.

All relevant queries must include tenant restrictions.

The platform must prevent:

- cross-tenant reads
- cross-tenant updates
- cross-tenant search leakage
- cross-tenant cache collisions
- cross-tenant event processing

---

# 30. Administrative Access

Administrative operations require:

- stronger permissions
- explicit audit
- narrower endpoint exposure
- separation from ordinary user functions
- additional approval where required

Administrative access must not be granted only through hidden UI controls.

---

# 31. Service-to-Service Authorization

Service-to-service calls must use authenticated service identity.

Authorization should evaluate:

- calling service
- audience
- scopes
- permitted operation
- optional delegated user context

Network location alone is not authorization.

---

# 32. Mutual TLS

Mutual TLS may be required for:

- highly sensitive integrations
- regulated environments
- service mesh identity
- external partner connections

Certificate validation must include:

- trusted chain
- expiration
- hostname or identity
- revocation strategy
- rotation process

---

# 33. Workload Identity

Cloud workloads should use workload identity instead of static credentials.

Examples:

- Kubernetes service account federation
- AWS IAM Roles for Service Accounts
- cloud-managed workload identity
- short-lived signed credentials

Static access keys embedded in applications are prohibited.

---

# 34. Secrets Management

Secrets must be stored in an approved secrets-management solution.

Examples:

- cloud secret manager
- HashiCorp Vault
- Kubernetes integration with external secret stores
- managed key vault

Secrets must not be stored in:

- source code
- Git history
- Docker images
- documentation examples
- plain configuration files
- logs
- test fixtures

---

# 35. Secret Types

Secrets include:

- client secrets
- database passwords
- private keys
- API keys
- bearer tokens
- signing keys
- encryption keys
- webhook secrets
- service credentials

The absence of the word `password` does not make a value non-sensitive.

---

# 36. Secret Rotation

Secrets must support rotation.

The rotation strategy should define:

- owner
- frequency
- overlap period
- service reload behavior
- emergency revocation
- audit trail
- rollback

Rotation must not require code changes.

---

# 37. Secret Injection

Secrets should be injected at runtime.

Preferred mechanisms:

- environment-backed secret references
- mounted secret files
- external secret providers
- short-lived credential providers

Avoid exposing secrets through process arguments because they may be visible in process listings.

---

# 38. Configuration Separation

Configuration must distinguish:

- public configuration
- sensitive configuration
- environment-specific configuration
- feature flags

Secrets must not be included in standard configuration dumps or actuator responses.

---

# 39. Data Classification

Data should be classified according to organizational policy.

Example categories:

- Public
- Internal
- Confidential
- Restricted

Classification determines:

- access
- encryption
- logging
- retention
- masking
- transmission
- incident severity

---

# 40. Data Minimization

Collect, process and retain only data required for legitimate business purposes.

Avoid storing fields simply because they may be useful later.

Data minimization reduces:

- breach impact
- compliance exposure
- storage cost
- integration risk

---

# 41. Personal Data

Personal data must be protected throughout:

- API requests
- database records
- events
- logs
- caches
- exports
- backups
- test data

Personal data must not be copied into development environments without approved sanitization.

---

# 42. Sensitive Data in Logs

Never log:

- passwords
- access tokens
- refresh tokens
- client secrets
- private keys
- full credit card data
- CVV
- session cookies
- authorization headers
- personal documents
- raw authentication assertions

Logs must use identifiers or masked values instead.

---

# 43. Data Masking

Mask data when limited diagnostic visibility is required.

Examples:

```text
jo***@example.com

***-**-1234

**** **** **** 4567
```

Masking must be consistent and must not allow reconstruction.

---

# 44. Encryption in Transit

All external and internal sensitive communication must use TLS.

Plain HTTP is prohibited in production except inside explicitly approved isolated infrastructure where another authenticated encrypted layer exists.

TLS configuration should follow current organizational cryptographic standards.

---

# 45. Encryption at Rest

Restricted or confidential data should be encrypted at rest.

Applicable locations include:

- PostgreSQL volumes
- object storage
- backups
- Kafka storage
- secret stores
- persistent queues

Encryption keys must be managed separately from encrypted data.

---

# 46. Application-Level Encryption

Application-level field encryption may be required when infrastructure encryption is insufficient.

Suitable fields may include:

- highly sensitive personal identifiers
- financial account data
- partner credentials

The design must support:

- key rotation
- query limitations
- deterministic or randomized encryption trade-offs
- backup restoration
- failure handling

---

# 47. Password Storage

Applications must never store plaintext passwords.

Where password storage is unavoidable, use an approved password-hashing algorithm.

Examples:

```text
Argon2id

bcrypt
```

Use:

- unique salt
- appropriate work factor
- secure migration strategy

General-purpose hashes such as SHA-256 are not password-storage algorithms.

---

# 48. Input Validation

All external input must be treated as untrusted.

Input includes:

- JSON
- query parameters
- path parameters
- headers
- files
- events
- database import files
- environment values
- third-party responses

Validation must occur before dangerous or expensive processing.

---

# 49. Structural Validation

Use Bean Validation for structural constraints.

Examples:

```java
@NotNull

@NotBlank

@Size

@Positive

@Pattern

@Email
```

Validation limits should prevent oversized or abusive input.

---

# 50. Allowlist Validation

Prefer allowlists over denylists.

Examples:

- supported sort fields
- accepted enum values
- permitted file types
- allowed URL schemes
- supported command names

Denylist validation often misses unexpected dangerous input.

---

# 51. Canonicalization

Input should be normalized only when required by business semantics.

Security decisions must be made on canonical representations.

Examples:

- normalized path
- normalized hostname
- normalized identifier case
- decoded URL

Avoid multiple inconsistent decoding or normalization stages.

---

# 52. Output Encoding

Output encoding must match the rendering context.

Examples:

- HTML encoding for HTML
- JavaScript encoding for script contexts
- URL encoding for URL components
- SQL parameterization for database queries

Business data should not be permanently HTML-escaped in the database.

---

# 53. SQL Injection Prevention

All database operations must use:

- parameterized queries
- JPA parameters
- typed criteria
- safe repository methods

Never concatenate untrusted values into SQL.

Incorrect:

```java
String sql = "select * from order_header where status = '" + status + "'";
```

---

# 54. Dynamic Query Safety

Dynamic sorting and filtering must map client-facing values to approved internal fields.

Do not concatenate arbitrary client-provided property names into JPQL or SQL.

Use explicit mappings such as:

```text
createdAt → order.created_at

status → order.status
```

---

# 55. Command Injection Prevention

Do not build operating-system commands using untrusted input.

Prefer platform APIs over shell execution.

When process execution is unavoidable:

- use fixed executable paths
- pass arguments separately
- validate allowed values
- avoid invoking a shell
- define timeouts
- capture output safely

---

# 56. Path Traversal Prevention

File paths must not be created directly from untrusted filenames.

Validate:

- canonical path
- destination root
- filename length
- allowed extension
- symbolic links
- path separators

Generated storage identifiers are preferred over user filenames.

---

# 57. Server-Side Request Forgery

Outbound URLs must not be accepted and invoked without validation.

Protect against SSRF through:

- destination allowlists
- scheme restrictions
- DNS and IP validation
- blocking metadata endpoints
- redirect restrictions
- network egress controls

Do not trust a URL merely because it uses HTTPS.

---

# 58. Deserialization Security

Do not enable unsafe polymorphic deserialization for untrusted data.

Avoid including Java class names in payloads.

Use:

- explicit DTOs
- approved subtype mappings
- schema validation
- bounded collection sizes

---

# 59. XML Security

When XML is required:

- disable external entity resolution
- disable DTD processing unless essential
- prevent entity expansion
- validate document size
- use secure parser configuration

This protects against XXE and expansion attacks.

---

# 60. Regular Expression Safety

Regular expressions applied to untrusted input must be reviewed for excessive backtracking.

Prefer:

- simple bounded expressions
- explicit length limits
- precompiled patterns
- linear-time parsing when possible

Regex denial of service must be considered for public inputs.

---

# 61. File Upload Security

File uploads must define:

- maximum size
- allowed content types
- allowed extensions
- content signature validation
- malware scanning
- storage isolation
- filename sanitization
- access control
- retention

Client-provided MIME type alone is not trusted.

---

# 62. File Content Validation

Validate actual file content when format matters.

Examples:

- magic bytes
- parser validation
- archive structure
- image dimensions
- document type
- checksum

Do not execute or render uploaded content without appropriate isolation.

---

# 63. Archive Security

Archive extraction must protect against:

- path traversal
- symbolic link abuse
- excessive file count
- excessive expanded size
- nested archive bombs
- duplicate filenames

Extraction limits must be configured.

---

# 64. Mass Assignment

API requests must use operation-specific DTOs.

Do not bind external requests directly to persistence entities or rich domain objects.

This prevents clients from modifying fields such as:

- status
- owner
- tenant
- audit metadata
- role
- approval authority
- internal price

---

# 65. Cross-Site Scripting

Backend APIs should return correctly typed JSON and avoid generating unsafe HTML.

User-provided text must be encoded by the final rendering context.

Sanitization should be used only when the business allows a restricted subset of markup.

---

# 66. Cross-Site Request Forgery

Cookie-authenticated browser applications must implement CSRF protection.

Stateless bearer-token APIs may disable CSRF when:

- authentication is not based on automatically submitted cookies
- the security rationale is documented
- no state-changing cookie-authenticated endpoint remains exposed

---

# 67. CORS

CORS must use explicit allowed origins.

Avoid wildcard origins for credentialed APIs.

Configuration must specify:

- origins
- methods
- headers
- exposed headers
- credential behavior
- cache duration

---

# 68. Security Headers

Browser-facing services should configure appropriate headers.

Examples:

```text
Strict-Transport-Security

Content-Security-Policy

X-Content-Type-Options

Referrer-Policy

Permissions-Policy

Cache-Control
```

Legacy headers should not replace modern protections.

---

# 69. Content Security Policy

A Content Security Policy should:

- restrict script origins
- restrict frame embedding
- restrict object sources
- avoid broad wildcards
- avoid unsafe inline scripts where practical
- support nonce or hash-based scripts

CSP is a defense-in-depth control, not a replacement for output encoding.

---

# 70. API Security

APIs must enforce:

- authenticated access by default
- operation authorization
- object authorization
- request validation
- size limits
- rate limiting
- secure errors
- audit for critical operations
- TLS
- idempotency where required

---

# 71. API Error Security

Public errors must not expose:

- stack traces
- SQL
- constraint names
- internal hostnames
- class names
- package names
- access tokens
- raw downstream responses
- implementation-specific exception types

Internal logs should retain sufficient sanitized diagnostic context.

---

# 72. Rate Limiting

Rate limiting should protect:

- login operations
- password-reset operations
- expensive searches
- bulk endpoints
- file uploads
- external APIs
- administrative actions

Rate-limit identity should use authenticated client or principal where possible.

---

# 73. Abuse Protection

Additional protections may include:

- quotas
- concurrency limits
- request-size limits
- circuit breakers
- bot mitigation
- reputation checks
- anomaly detection
- temporary blocking

Controls must distinguish legitimate high-volume use from abuse.

---

# 74. Idempotency Security

Idempotency keys must:

- be bounded in length
- be scoped to the authenticated client or business context
- not expose sensitive data
- be stored with a request fingerprint
- reject mismatched replays

An attacker must not retrieve another client’s result by guessing an idempotency key.

---

# 75. Messaging Security

Messaging infrastructure must provide:

- authenticated producers
- authenticated consumers
- destination authorization
- encrypted transport
- least-privilege access
- secret rotation
- message integrity where required
- sensitive-data minimization

---

# 76. Message Trust

Messages must be treated as untrusted input even when delivered through an internal broker.

Consumers must validate:

- event type
- event version
- required fields
- identifiers
- payload limits
- authorized producer where supported

---

# 77. Topic and Queue Authorization

A service should only have permissions needed for its responsibilities.

Examples:

```text
order-service → publish orders.order-events

inventory-service → consume orders.order-events

inventory-service → publish inventory.reservation-events
```

Administrative broker permissions must not be granted to application workloads.

---

# 78. Message Integrity

Digital signatures or message authentication may be required for:

- cross-company integration
- webhooks
- high-risk commands
- untrusted transport intermediaries

Integrity validation must occur before business processing.

---

# 79. Replay Protection

Commands and sensitive messages may require replay protection through:

- event identifiers
- timestamps
- nonces
- idempotency markers
- signature windows
- sequence validation

Broker delivery retry must not be confused with malicious replay.

---

# 80. Database Security

Database security must include:

- dedicated application users
- least-privilege grants
- encrypted connections
- credential rotation
- restricted administrative access
- backup protection
- audit where required
- network restrictions

Applications must not connect as database superusers.

---

# 81. Database Accounts

Use separate accounts for:

- application runtime
- Flyway migrations
- reporting
- administration
- read-only support

Runtime applications should not have schema-alteration permissions.

---

# 82. Database Migration Security

Migration credentials may require elevated privileges but must be isolated from runtime credentials.

Migration scripts must be reviewed for:

- destructive operations
- data exposure
- unintended grants
- unsafe functions
- long locks
- insecure defaults

Applied Flyway migrations must never be altered.

---

# 83. Row-Level Security

PostgreSQL Row-Level Security may be used when it provides meaningful additional tenant isolation.

It must not be adopted without understanding:

- connection identity
- session context
- query behavior
- administrative bypass
- testing complexity
- operational support

Application-level authorization remains required.

---

# 84. Cache Security

Caches must not leak data between:

- users
- tenants
- companies
- permissions
- environments

Cache keys must include all authorization-relevant dimensions.

Sensitive cached values should have:

- limited TTL
- controlled serialization
- encryption where required
- explicit eviction

---

# 85. Cache Authorization

Authorization must be checked before returning cached data.

Do not assume that possession of a cache key implies permission.

Avoid caching complete authorization-sensitive responses under overly broad keys.

---

# 86. Docker Security

Container images must:

- use trusted base images
- use minimal runtime images
- run as a non-root user
- avoid embedded secrets
- include only required files
- be scanned for vulnerabilities
- use pinned or controlled versions
- remove package-manager caches

---

# 87. Multi-Stage Builds

Use multi-stage builds to separate:

- compilation
- testing
- packaging
- runtime

The runtime image must not contain:

- build tools
- source code
- test fixtures
- unnecessary package managers
- local credentials

---

# 88. Container User

Containers must run as a non-root user.

Example:

```dockerfile
USER 10001
```

File permissions must support runtime behavior without elevated privileges.

---

# 89. Container Filesystem

Use a read-only root filesystem where practical.

Writable locations should be explicitly mounted.

Applications must not rely on arbitrary filesystem writes.

---

# 90. Container Capabilities

Drop unnecessary Linux capabilities.

The application should not require:

- privileged mode
- host networking
- host PID namespace
- unrestricted device access

Any exception requires security approval.

---

# 91. Image Provenance

Container images should have:

- trusted registry origin
- immutable digest
- build traceability
- vulnerability scan result
- software bill of materials
- optional signature or attestation

Production deployments should avoid mutable tags such as `latest`.

---

# 92. Kubernetes Security

Kubernetes workloads must use:

- dedicated service accounts
- least-privilege RBAC
- non-root execution
- resource limits
- network policies
- secure secret integration
- restricted pod security settings
- controlled ingress and egress

---

# 93. Kubernetes Service Accounts

Each workload should use a dedicated service account.

Disable automatic service-account token mounting when the workload does not need Kubernetes API access.

Do not use the default service account for production workloads.

---

# 94. Kubernetes RBAC

RBAC permissions must be explicit and minimal.

Application workloads should normally have no permission to:

- list secrets
- create pods
- modify deployments
- read unrelated config maps
- access cluster-wide resources

---

# 95. Pod Security Context

Recommended controls include:

```yaml
securityContext:
  runAsNonRoot: true
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - ALL
```

Actual values must be compatible with the runtime image.

---

# 96. Network Policies

Network policies should restrict:

- inbound callers
- outbound dependencies
- database access
- broker access
- namespace communication
- internet egress

Default-deny policies are preferred where operationally feasible.

---

# 97. Ingress Security

Ingress configuration must enforce:

- TLS
- approved hostnames
- request-size limits
- timeout limits
- rate limits where applicable
- secure headers
- path restrictions

Administrative endpoints must not be exposed through public ingress.

---

# 98. Kubernetes Secrets

Native Kubernetes Secrets are not sufficient as the only protection when stronger secret-management integration is required.

Prefer external secret stores with controlled synchronization or workload identity.

Secret values must not appear in manifests committed to source control.

---

# 99. Resource Limits

CPU and memory requests and limits support availability and abuse resistance.

Unbounded resources can allow one workload to affect others.

Limits must be validated through performance testing rather than guessed blindly.

---

# 100. Actuator Security

Spring Boot Actuator endpoints must be restricted.

Publicly exposed endpoints should be minimal.

Typical public health exposure:

```text
/actuator/health/liveness

/actuator/health/readiness
```

Sensitive endpoints such as environment, heap dump and mappings require strong administrative controls or must remain disabled.

---

# 101. Management Port

A separate management port or network path may be used for operational endpoints.

Management traffic should be restricted by:

- network policy
- authentication
- authorization
- ingress rules
- monitoring platform identity

---

# 102. Secure Coding

Production code must:

- validate untrusted input
- avoid hardcoded secrets
- preserve exception causes
- use safe APIs
- close resources
- avoid insecure deserialization
- avoid reflection-based bypasses
- use cryptographic libraries correctly
- avoid sensitive logging
- enforce authorization explicitly

---

# 103. Exception Handling

Security failures must fail securely.

Do not:

- ignore authentication exceptions
- default to permitted access
- continue after failed signature validation
- expose internal exception details
- catch broad exceptions and return success

Unexpected security failures should deny the operation.

---

# 104. Fail Open Versus Fail Closed

Security controls should fail closed.

Examples:

- authorization service unavailable → deny sensitive operation
- token validation unavailable → reject request
- signature cannot be verified → reject message

Availability fallbacks must not silently bypass security.

---

# 105. Cryptography

Use approved standard libraries and algorithms.

Do not implement custom cryptographic primitives.

Cryptographic design must define:

- algorithm
- key size
- key ownership
- rotation
- storage
- nonce or initialization vector
- authentication mode
- error handling

---

# 106. Randomness

Security-sensitive randomness must use a cryptographically secure generator.

Use:

```java
SecureRandom
```

Do not use:

```java
Random

ThreadLocalRandom
```

for tokens, keys, nonces or password-reset values.

---

# 107. Constant-Time Comparison

Secrets, signatures and authentication codes should use constant-time comparison where relevant.

Avoid ordinary string equality for sensitive cryptographic comparisons when the framework does not already provide safe validation.

---

# 108. URL Redirection

Redirect targets must be allowlisted.

Do not redirect users to arbitrary client-provided URLs.

This protects against open redirect attacks and credential phishing.

---

# 109. Email and Notification Security

Notifications must not include excessive sensitive data.

Links for sensitive actions should use:

- short-lived tokens
- one-time use
- secure random values
- server-side validation
- explicit expiration

Do not place credentials or personal data in URL query strings.

---

# 110. Logging Security Events

Security logs may include:

- authentication failure
- authorization denial
- token validation failure
- suspicious input
- rate-limit rejection
- privilege change
- administrative action
- secret rotation failure

Security logs must exclude credentials and sensitive payloads.

---

# 111. Audit Events

Critical business and security actions require immutable audit records.

Examples:

- order approval
- manual override
- permission change
- user deactivation
- refund authorization
- secret administrative access
- replay execution

Audit records should include:

```text
actor

action

resource

timestamp

outcome

reason

correlationId
```

---

# 112. Audit Integrity

Audit data must be protected from unauthorized:

- modification
- deletion
- access
- truncation

Operational logs are not a substitute for immutable audit records.

---

# 113. Security Monitoring

Monitor for:

- repeated authentication failures
- authorization denials
- abnormal token use
- unusual access patterns
- secret access failures
- suspicious file uploads
- elevated error rates
- dependency exploitation attempts
- unexpected administrative activity

Monitoring thresholds should minimize both missed incidents and alert fatigue.

---

# 114. Dependency Management

Dependencies must be:

- required
- maintained
- version-controlled
- vulnerability-scanned
- licensed appropriately
- periodically reviewed

Unused dependencies must be removed.

---

# 115. Version Pinning

Dependency versions should be controlled through:

- dependency management
- lock files where applicable
- approved BOMs
- automated update tools

Dynamic version selectors are prohibited in production builds.

Avoid:

```text
latest

+

RELEASE
```

---

# 116. Software Composition Analysis

The pipeline must scan dependencies for known vulnerabilities.

Findings should include:

- CVE
- severity
- affected version
- fixed version
- exploitability
- reachable code where available
- remediation deadline

Severity alone should not replace contextual risk analysis.

---

# 117. Transitive Dependencies

Transitive dependencies require the same security attention as direct dependencies.

The team must understand which framework or library introduces a vulnerable component.

Exclusions or overrides should be tested for compatibility.

---

# 118. Software Bill of Materials

Builds should generate an SBOM.

The SBOM supports:

- vulnerability response
- supply-chain visibility
- licensing review
- incident investigation
- release traceability

A standard format such as CycloneDX or SPDX should be used.

---

# 119. SAST

Static Application Security Testing must run in CI.

SAST should detect issues such as:

- injection
- insecure cryptography
- path traversal
- unsafe deserialization
- sensitive logging
- resource leaks
- weak randomness
- authorization mistakes
- insecure exception handling

Findings must be reviewed, not merely suppressed.

---

# 120. SAST Suppression

A suppression requires:

- documented false-positive rationale
- narrow scope
- reviewer approval
- issue reference where required
- periodic revalidation

Broad project-level suppressions are prohibited.

---

# 121. Secret Scanning

Secret scanning must run:

- before commit where supported
- in pull requests
- in CI
- on repository history when required

A detected secret must be considered compromised even when quickly removed from the latest commit.

The secret must be revoked or rotated.

---

# 122. Container Scanning

Container images must be scanned for:

- operating-system vulnerabilities
- application dependencies
- embedded secrets
- insecure configuration
- malware where supported

Production promotion may be blocked by severity and policy thresholds.

---

# 123. Infrastructure Scanning

Kubernetes and infrastructure definitions should be scanned for:

- privileged containers
- public exposure
- excessive permissions
- missing encryption
- unrestricted ingress or egress
- mutable image tags
- embedded secrets
- insecure defaults

---

# 124. Dynamic Security Testing

DAST may be used against deployed test environments.

It should validate:

- authentication behavior
- authorization behavior
- input handling
- secure headers
- error leakage
- common web vulnerabilities
- API exposure

DAST complements SAST and code review.

---

# 125. Penetration Testing

Penetration testing should be considered for:

- major releases
- public applications
- sensitive workflows
- significant architecture changes
- regulated environments

Findings require tracked remediation and retesting.

---

# 126. Security Unit Tests

Unit tests should cover:

- authorization policies
- claim mapping
- permission evaluation
- masking
- input validation
- signature validation wrappers
- safe error translation

Tests must use deterministic identities and tokens.

---

# 127. Security Integration Tests

Integration tests should validate:

- missing token
- invalid token
- expired token
- wrong issuer
- wrong audience
- insufficient scope
- wrong tenant
- wrong ownership
- permitted access
- protected actuator endpoints
- CORS behavior where applicable

---

# 128. Token Test Fixtures

Security tests should generate controlled tokens using test keys or approved test utilities.

Do not use production tokens in tests.

Claims must be explicit and deterministic.

---

# 129. Authorization Regression Tests

Every corrected authorization defect should include a regression test.

Examples:

```java
testGetOrderShouldRejectUserFromAnotherTenant()

testApproveShouldRejectUserWithoutApprovalPermission()
```

The test must validate both status and absence of protected data.

---

# 130. Negative Testing

Security testing must include invalid and malicious input.

Examples:

- oversized values
- unexpected enum values
- traversal sequences
- malformed tokens
- unsupported algorithms
- duplicate headers
- invalid signatures
- cross-tenant identifiers
- malicious filenames
- excessive nested JSON

---

# 131. Security Test Data

Security test data must not contain real:

- credentials
- personal data
- access tokens
- private keys
- production identifiers

Use sanitized deterministic fixtures.

---

# 132. CI Security Gates

The pipeline should enforce security gates for:

- failed tests
- critical SAST findings
- known exploitable dependency vulnerabilities
- exposed secrets
- unsafe container configuration
- incompatible security configuration
- invalid signatures or attestations where required

Exceptions require documented risk acceptance.

---

# 133. Vulnerability Severity

Vulnerabilities should be classified using:

- technical severity
- exploitability
- exposure
- business impact
- data sensitivity
- compensating controls
- active exploitation status

A lower technical severity may still be urgent in a high-risk business flow.

---

# 134. Remediation Targets

Remediation deadlines should follow organizational policy.

A practical model may distinguish:

- Critical
- High
- Medium
- Low

Critical actively exploitable vulnerabilities require immediate action.

The final deadlines must align with the organization’s security governance.

---

# 135. Risk Acceptance

Risk acceptance requires:

- identified vulnerability
- business justification
- compensating controls
- expiration date
- accountable approver
- remediation plan
- periodic review

Risk acceptance must not be permanent by default.

---

# 136. Security Incident Response

The project must support investigation of:

- credential compromise
- unauthorized access
- data exposure
- malicious dependency
- token abuse
- privilege escalation
- message forgery
- supply-chain compromise

Runbooks should identify:

- contacts
- containment actions
- evidence sources
- credential rotation
- communication process
- recovery steps

---

# 137. Evidence Preservation

During an incident, preserve:

- logs
- audit events
- deployment metadata
- container digests
- token identifiers
- access records
- message identifiers
- configuration versions

Evidence access must be restricted and auditable.

---

# 138. Credential Compromise

When a credential may be compromised:

1. revoke or rotate it
2. identify affected systems
3. search logs and audit records
4. invalidate active sessions when applicable
5. replace dependent configuration
6. document the incident
7. add preventive controls

Removing the credential from source control is not sufficient.

---

# 139. Security Patch Management

Security updates should follow an expedited path.

The team must balance:

- vulnerability risk
- compatibility risk
- regression risk
- deployment urgency

Automated dependency updates still require testing.

---

# 140. Backup Security

Backups must be:

- encrypted
- access controlled
- monitored
- retained according to policy
- tested for restoration
- deleted according to retention rules

Backup credentials and encryption keys require separate protection.

---

# 141. Disaster Recovery Security

Disaster recovery procedures must preserve:

- identity controls
- encryption
- auditability
- secret handling
- network restrictions
- data classification

Recovery must not introduce broad temporary permissions that remain permanently enabled.

---

# 142. Environment Separation

Development, test, staging and production must use separate:

- credentials
- identities
- data
- databases
- topics and queues
- secret scopes
- access controls

Production secrets must never be used in local development.

---

# 143. Production Access

Production access must be:

- limited
- attributable
- time-bound where possible
- approved
- logged
- reviewed

Shared administrative accounts are prohibited.

---

# 144. Break-Glass Access

Emergency access should:

- require explicit activation
- use strong authentication
- be time-limited
- generate alerts
- create an audit trail
- require post-event review

Break-glass access must not become normal operational access.

---

# 145. Secure Defaults

Default behavior must be secure.

Examples:

```text
authentication required

authorization denied unless allowed

TLS enabled

sensitive actuator endpoints disabled

payload logging disabled

unknown JWT algorithms rejected

unknown origins rejected
```

Insecure behavior must not require users to remember to disable it.

---

# 146. Feature Flags and Security

Security controls must not be casually disabled through feature flags.

A flag affecting authentication, authorization or data protection requires:

- strict ownership
- secure default
- audit
- environment restriction
- expiration plan

---

# 147. Third-Party Integrations

Third-party integrations must define:

- authentication
- authorization
- data classification
- timeout
- retry
- certificate validation
- logging
- incident contact
- credential rotation
- contractual security requirements

External responses remain untrusted input.

---

# 148. Webhook Security

Inbound webhooks must validate:

- signature
- timestamp
- delivery identifier
- replay window
- expected source
- payload schema

Outbound webhooks should use:

- TLS
- signed payloads
- bounded retries
- secret rotation
- minimal data

---

# 149. DNS and Certificate Validation

TLS clients must validate hostnames and certificate chains.

Disabling certificate validation is prohibited outside narrowly controlled test code.

Test-only insecure clients must not be packaged into production artifacts.

---

# 150. Timeouts and Security

All external calls require timeouts.

Missing timeouts can allow resource exhaustion.

Timeout configuration should include:

- connection timeout
- response timeout
- acquisition timeout
- overall operation budget

---

# 151. Resource Exhaustion

Protect limited resources through:

- request limits
- bounded queues
- connection pools
- bulkheads
- concurrency controls
- pagination
- timeouts
- circuit breakers
- upload limits

Virtual threads do not eliminate downstream resource constraints.

---

# 152. Denial-of-Service Resistance

Public and expensive operations should be evaluated for:

- algorithmic complexity
- payload expansion
- regex complexity
- database query cost
- unbounded sorting
- fan-out
- file decompression
- cache stampede
- expensive authorization lookups

---

# 153. Cache Stampede Protection

Critical cache paths may require:

- request coalescing
- bounded refresh
- jittered TTL
- stale-while-revalidate
- fallback
- concurrency control

Security-sensitive data must not remain stale beyond acceptable policy.

---

# 154. Security Documentation

Each service should document:

- authentication model
- authorization model
- public endpoints
- required scopes
- service identities
- secret dependencies
- data classification
- audit events
- security contacts
- known trust boundaries

---

# 155. Security Review Checklist

Before approving a feature, verify:

- Is the trust boundary understood?
- Is authentication required?
- Is the token fully validated?
- Is authorization enforced at operation and object level?
- Is tenant isolation preserved?
- Is input bounded and validated?
- Are queries parameterized?
- Are sensitive values excluded from logs?
- Are secrets externally managed?
- Is transport encrypted?
- Is data classification understood?
- Are external calls restricted and timed out?
- Are errors safe?
- Are dependencies scanned?
- Are security tests present?
- Is audit required?
- Is operational monitoring defined?
- Is incident ownership clear?

---

# 156. Anti-Patterns

The following practices are prohibited:

- hardcoded secrets
- plaintext credentials
- accepting decoded but unverified JWTs
- trusting client-provided roles
- authorization only in the frontend
- shared service credentials
- wildcard production redirect URIs
- public actuator administration endpoints
- database superuser application accounts
- string-concatenated SQL
- arbitrary client-controlled URLs
- logging bearer tokens
- disabling TLS certificate validation
- exposing stack traces
- unrestricted file uploads
- insecure polymorphic deserialization
- privileged containers
- running containers as root without justification
- unrestricted Kubernetes RBAC
- production secrets in Git
- ignoring critical security scan findings
- permanent security suppressions without review
- relying only on network location for trust
- failing open after authorization or validation errors

---

# 157. Architecture Rules

Security must:

- authenticate every protected caller
- validate token signature and claims
- deny access by default
- enforce object-level authorization
- preserve tenant isolation
- use least-privilege service identities
- manage secrets externally
- encrypt sensitive communication
- minimize sensitive data
- validate all external input
- use parameterized database access
- protect messaging destinations
- run containers without unnecessary privileges
- restrict Kubernetes permissions
- scan code, dependencies and images
- generate auditable security events
- fail securely
- support incident investigation

---

# 158. Decision Summary

The project adopts:

- OAuth 2.0 and OpenID Connect
- Authorization Code with PKCE for public clients
- Client Credentials for service identities
- complete JWT validation
- Keycloak as an identity provider where applicable
- capability-oriented scopes
- object-level authorization
- tenant isolation
- workload identity
- external secrets management
- TLS for protected communication
- data minimization and masking
- secure API and messaging contracts
- least-privilege database access
- non-root minimal containers
- Kubernetes RBAC and network policies
- SAST, dependency, secret and image scanning
- SBOM generation
- immutable audit records for critical operations
- automated security tests
- documented vulnerability remediation
- deny-by-default and fail-closed behavior
