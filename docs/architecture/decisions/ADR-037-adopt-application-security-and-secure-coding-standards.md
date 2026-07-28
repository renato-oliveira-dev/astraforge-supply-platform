# ADR-037: Adopt Application Security and Secure Coding Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-037 |
| Title | Adopt Application Security and Secure Coding Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Application Security, Spring Security, OWASP, SAST, Secure Coding |
| Related Work Items | Authentication, Authorization, JWT, Validation, SAST, SonarQube, Secrets |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform processes business-critical information through:

- REST APIs
- SQS
- PostgreSQL
- Redis
- AWS services
- external APIs
- batch processing
- file integrations
- web applications

The platform is distributed and independently deployable.

Security therefore cannot be concentrated exclusively at:

```text
API Gateway
```

or:

```text
Frontend
```

Security must exist throughout the application architecture.

The baseline model is:

```text
Internet / Client
        |
        v
API Gateway
        |
        v
Authentication
        |
        v
Application
        |
        +--> Authorization
        +--> Validation
        +--> Business Rules
        +--> Secure Integration
        +--> Data Protection
        +--> Safe Logging
        |
        v
Persistence / Messaging / External Systems
```

---

# 2. Problem Statement

The platform requires standards defining:

- authentication
- authorization
- Spring Security
- JWT
- least privilege
- input validation
- output encoding
- XSS
- SQL injection
- mass assignment
- SSRF
- path traversal
- log injection
- header injection
- deserialization
- sensitive data
- secrets
- cryptography
- token handling
- secure HTTP clients
- dependency security
- SAST
- Sonar security hotspots
- error handling
- security testing
- audit
- incident diagnostics

---

# 3. Decision Drivers

Primary drivers are:

1. confidentiality
2. integrity
3. availability
4. least privilege
5. defense in depth
6. secure defaults
7. OWASP alignment
8. auditability
9. maintainability
10. regulatory readiness
11. automated security verification
12. controlled exposure of business data

---

# 4. Decision

The platform adopts:

```text
DEFENSE IN DEPTH
```

as its application-security model.

Security controls must exist at the layer that owns the relevant security decision.

Conceptually:

```text
                REQUEST
                   |
                   v
             API GATEWAY
                   |
             Authentication
                   |
                   v
               SERVICE
                   |
        +----------+----------+
        |          |          |
        v          v          v
 Authorization Validation  Rate/Capacity
        |          |          |
        +----------+----------+
                   |
                   v
              BUSINESS RULE
                   |
                   v
              DATA ACCESS
                   |
          +--------+--------+
          |                 |
          v                 v
     DATABASE          INTEGRATIONS
```

---

# 5. Fundamental Principle

The primary rule is:

```text
Never trust data merely because
it came from another internal system.
```

Trust boundaries still exist between microservices.

---

# 6. Security Ownership

Security is a shared responsibility across:

```text
Gateway

Application

Domain

Persistence

Messaging

Infrastructure

CI/CD
```

---

# 7. Authentication vs Authorization

Authentication answers:

```text
Who are you?
```

Authorization answers:

```text
Are you allowed to perform this operation?
```

These are separate controls.

---

# 8. Authentication

Authentication should use the platform-approved identity provider and token mechanism.

---

# 9. JWT

JWT bearer tokens may carry authenticated identity and approved claims.

---

# 10. JWT Validation

Services must validate applicable token properties, including:

- signature
- issuer
- expiration
- audience where applicable
- token type where applicable

---

# 11. Token Signature

A JWT must never be trusted merely because it can be decoded.

```text
Decode
   !=
Validate
```

---

# 12. Expiration

Expired tokens must be rejected.

---

# 13. Algorithm

Applications must not accept insecure or unexpected signing algorithms.

---

# 14. Claims

Claims must be treated according to the identity contract.

Do not infer authorization from arbitrary untrusted claims.

---

# 15. Authorization

Authorization remains application-owned even when the gateway performs coarse-grained access control.

---

# 16. Defense in Depth

Example:

```text
Gateway:
    authenticated user

Service:
    user may approve this order

Domain:
    order state allows approval
```

---

# 17. Business Authorization

Authorization may depend on:

- role
- profile
- company
- segment
- customer
- resource ownership
- workflow stage
- operation

---

# 18. Resource-Level Authorization

Checking only:

```text
ROLE_SUPERVISOR
```

may be insufficient.

The service may also need to verify:

```text
Supervisor belongs to allowed company

AND

Order belongs to allowed segment

AND

Order is in permitted workflow state
```

---

# 19. IDOR / BOLA

APIs must protect against insecure direct object references / broken object-level authorization.

Knowing:

```text
/order/{uuid}
```

must not imply permission to access the order.

---

# 20. UUID Is Not Authorization

Using UUIDs does not eliminate object-level authorization requirements.

---

# 21. Least Privilege

Every identity should receive only the permissions required.

This applies to:

- users
- services
- database accounts
- AWS IAM roles
- CI/CD identities

---

# 22. Service Credentials

Services must not share broad credentials unnecessarily.

---

# 23. AWS Identity

Workload identity/IAM roles are preferred over long-lived static AWS credentials.

---

# 24. Input Validation

All external input must be validated according to its intended domain and technical constraints.

---

# 25. External Input

External input includes:

- HTTP body
- query parameter
- path parameter
- header
- SQS payload
- file
- database data originating externally
- third-party API response

---

# 26. Validation

Validation answers:

```text
Is this value allowed by the contract/domain?
```

---

# 27. Sanitization

Sanitization means transforming data to remove or normalize undesirable content.

It is not interchangeable with validation.

---

# 28. Encoding

Encoding prepares data for a particular output/interpreter context.

---

# 29. Critical Distinction

The platform explicitly adopts:

```text
VALIDATION
    !=
SANITIZATION
    !=
OUTPUT ENCODING
```

---

# 30. Correct Model

```text
INPUT
  |
  v
VALIDATE
  |
  v
DOMAIN VALUE
  |
  v
STORE / PROCESS
  |
  v
OUTPUT CONTEXT
  |
  +--> JSON serializer
  |
  +--> HTML encoding
  |
  +--> URL encoding
  |
  +--> Parameterized SQL
  |
  +--> Safe structured logging
```

---

# 31. Domain Data Integrity

Valid business data must not be generically corrupted to satisfy a security scanner.

---

# 32. Example: M&M

If:

```text
M&M
```

is valid business data, the canonical domain value remains:

```text
M&M
```

It must not be globally transformed into:

```text
M&amp;M
```

before storage or ordinary JSON serialization merely to suppress an XSS/SAST finding.

---

# 33. Why Generic HTML Escaping Is Wrong

Global HTML escaping can cause:

```text
Input:
M&M

Backend:
M&amp;M

Frontend:
M&amp;amp;M
```

and corrupt the domain contract.

---

# 34. Correct XSS Boundary

HTML encoding belongs where untrusted data is inserted into an HTML context.

---

# 35. JSON Is Not HTML

A JSON REST API should return the correct domain value through a safe JSON serializer.

The presentation layer remains responsible for safe HTML rendering.

---

# 36. Context Matters

Security encoding depends on the interpreter:

```text
HTML context
JavaScript context
URL context
SQL context
Log context
Shell context
```

One generic escaping function cannot safely solve all contexts.

---

# 37. Canonicalization

Validation should operate on an intentionally defined canonical representation where ambiguity matters.

---

# 38. Unicode

Unicode normalization should only be introduced when required by domain/security semantics.

Do not silently modify valid text without justification.

---

# 39. Input Length

All externally controlled text should have reasonable maximum sizes.

---

# 40. Collection Size

Externally supplied collections must have bounded size.

---

# 41. Numeric Range

Numeric inputs require realistic range constraints.

---

# 42. Enum Validation

Externally supplied enumerated values must be validated against approved contract values.

---

# 43. Unknown Value

Unknown enum/status values must produce controlled client errors rather than unexpected server failures.

---

# 44. SQL Injection

Database access must use parameterized queries.

---

# 45. Safe Example

Conceptually:

```java
query.setParameter("customerId", customerId);
```

---

# 46. Unsafe Example

Prohibited:

```java
"SELECT ... WHERE customer = '" + customerInput + "'"
```

---

# 47. JPA

JPA does not automatically make every dynamic query safe.

Dynamic fragments still require controlled construction.

---

# 48. Dynamic Sort

Client input must never be concatenated directly into arbitrary SQL/JPA sort expressions.

---

# 49. Sort Allowlist

Sorting follows ADR-036:

```text
Public Field
     |
     v
Allowlist
     |
     v
Internal Property
```

---

# 50. Dynamic Filters

Dynamic filter values must use parameter binding.

---

# 51. LIKE Queries

Wildcard semantics must be deliberate when user input participates in LIKE queries.

---

# 52. Stored Procedures

Stored procedure calls must use bind parameters rather than dynamic SQL concatenation where possible.

---

# 53. Native SQL

Native SQL requires the same injection protections as JPQL.

---

# 54. XSS

Backend services must not attempt to solve all XSS risks through generic storage-time escaping.

---

# 55. Stored XSS

Stored values may become dangerous when rendered unsafely later.

The correct control remains context-sensitive output handling.

---

# 56. HTML Response

If the backend intentionally generates HTML, it owns appropriate HTML-context encoding.

---

# 57. Templates

Server-side HTML templates should use automatic escaping by default.

---

# 58. Raw HTML

Rendering raw/unescaped HTML requires explicit security review.

---

# 59. Rich Text

If rich HTML is a business requirement, use a deliberately designed allowlist sanitizer appropriate for HTML content.

---

# 60. Do Not Build HTML Sanitizer Casually

Custom regex-based HTML sanitizers are prohibited.

---

# 61. SSRF

Server-Side Request Forgery must be considered whenever users can influence outbound destinations.

---

# 62. Base URL

External service base URLs should normally come from trusted configuration.

---

# 63. User-Controlled URL

Do not accept an arbitrary URL and pass it directly to:

```text
WebClient

RestClient

HttpClient
```

without an explicit security model.

---

# 64. SSRF Allowlist

Where dynamic destinations are required, enforce:

- approved schemes
- approved hosts
- approved ports
- path constraints where applicable

---

# 65. Internal Metadata

Outbound requests must not allow arbitrary access to cloud metadata/internal infrastructure.

---

# 66. Redirects

Automatic redirects require consideration in SSRF-sensitive clients.

---

# 67. DNS

Host allowlisting must consider DNS resolution/rebinding threats for high-risk dynamic outbound access.

---

# 68. WebClient

WebClient clients should use trusted configured base URLs.

---

# 69. Path Variables in Outbound Calls

User-controlled values inserted into paths must be encoded as path components rather than concatenated as raw URLs.

---

# 70. Query Parameters

Use URI builders/query parameter APIs instead of manual URL concatenation.

---

# 71. Path Traversal

File access must not allow user input to escape an approved base directory.

---

# 72. Unsafe File Example

Prohibited conceptually:

```text
baseDirectory + "/" + userFilename
```

without validation and canonical path checks.

---

# 73. Canonical Path

Resolved paths must remain within the approved base directory.

---

# 74. Filename

Uploaded/generated filenames should not be trusted as filesystem paths.

---

# 75. Extension

File extensions alone are not reliable proof of file type.

---

# 76. File Size

Uploads require size limits.

---

# 77. Archive Extraction

ZIP/archive extraction must protect against path traversal such as Zip Slip.

---

# 78. Decompression Bomb

Archive handling must consider decompression/resource amplification.

---

# 79. File Parsing

Parsers should reject malformed files predictably and with bounded resource consumption.

---

# 80. XML

XML parsers must disable dangerous external entity behavior unless explicitly required and securely configured.

---

# 81. XXE

External entity resolution must not expose:

- local files
- internal network resources

---

# 82. Deserialization

Untrusted data must not control arbitrary Java type instantiation.

---

# 83. Polymorphic Deserialization

Jackson polymorphic deserialization requires strict control.

---

# 84. Default Typing

Broad unsafe default typing for untrusted payloads is prohibited.

---

# 85. DTO Binding

Deserialize external payloads into explicit contract DTOs.

---

# 86. Mass Assignment

Dedicated request DTOs must prevent unauthorized assignment of internal fields.

---

# 87. Example

A client creating an order must not be able to set:

```text
approvedBy

approvedAt

internalWorkflowStatus

createdBy

createdAt
```

unless those fields are explicitly client-controlled.

---

# 88. Entity Binding

Binding request JSON directly to JPA entities is prohibited.

---

# 89. Header Injection

Externally controlled values used in headers require validation against the header's expected format.

---

# 90. CRLF

User input must not create arbitrary additional HTTP headers through CR/LF injection.

---

# 91. Redirect

Dynamic redirects require allowlisted/validated destinations.

---

# 92. Open Redirect

Do not redirect users to arbitrary externally supplied URLs.

---

# 93. Log Injection

Untrusted input must not be allowed to forge log entries.

---

# 94. Structured Logging

Structured logging is preferred.

Example conceptually:

```java
log.warn(
        "Remote request failed customerId={} status={}",
        customerId,
        status
);
```

---

# 95. CR/LF in Logs

Externally supplied text may require normalization when inserted into line-oriented logs.

---

# 96. Domain Mutation

Log safety must not mutate the stored/returned business value.

Sanitize only the representation sent to the log sink where necessary.

---

# 97. Sensitive Logging

Never log:

- passwords
- bearer tokens
- refresh tokens
- private keys
- client secrets
- session credentials

---

# 98. Authorization Header

`Authorization` headers must never appear in application logs.

---

# 99. Token Masking

If an exception/message can contain tokens, sensitive values must be masked before logging.

---

# 100. Remote Errors

Remote HTTP error bodies must be:

- bounded
- safely represented
- screened for sensitive information

before logging.

---

# 101. Full Payload Logging

Logging complete request/response bodies is prohibited by default for business APIs.

---

# 102. PII

Personally identifiable information should be logged only when operationally necessary and permitted.

---

# 103. Identifier Preference

Prefer stable technical/business identifiers over complete personal records in logs.

---

# 104. Logging vs Audit

Operational logs and audit records have different purposes.

---

# 105. Audit

Security/business-sensitive operations should create appropriate audit events.

Examples:

- approval
- cancellation
- permission-sensitive updates
- administrative changes

---

# 106. Audit Content

Audit should identify, where applicable:

```text
Who

What

When

Resource

Outcome

Correlation
```

---

# 107. Audit Integrity

Users must not be able to arbitrarily supply authoritative audit identity fields.

---

# 108. Authentication Identity

Audit identity should originate from trusted authenticated context.

---

# 109. SecurityContext

Spring Security's `SecurityContext` must remain authoritative for authenticated user context where adopted.

---

# 110. Async Context

Security context propagation across concurrent execution follows ADR-034.

---

# 111. Context Leakage

SecurityContext from one request must never leak into another request/task.

---

# 112. ThreadLocal Cleanup

Thread-local security/request context must be cleaned correctly after propagated execution.

---

# 113. Secrets

Secrets must not be stored in source code.

---

# 114. Prohibited

Examples:

```text
database password in application.yml

AWS secret in Java constant

OAuth secret committed to Git

private key in repository
```

---

# 115. Secret Source

Secrets should come from the approved secret-management mechanism.

---

# 116. Environment Variables

Environment variables may transport secret references/values where platform policy permits, but must not be treated as inherently secure storage.

---

# 117. Secret Rotation

Credential design should support rotation.

---

# 118. Long-Lived Secret

Long-lived static credentials should be minimized.

---

# 119. Git History

Removing a secret from the current file does not remove it from Git history.

Compromised committed secrets require rotation.

---

# 120. Configuration Logging

Application startup logs must not dump complete configuration objects containing secrets.

---

# 121. `toString`

Credential/configuration classes must not expose secrets through generated `toString()` output.

---

# 122. Lombok

Avoid Lombok-generated `toString()` containing secret fields.

---

# 123. Cryptography

Use established cryptographic libraries and platform primitives.

---

# 124. Custom Crypto

Custom cryptographic algorithms are prohibited.

---

# 125. Password Storage

Passwords must use approved adaptive password hashing mechanisms when the application owns password storage.

---

# 126. Encryption vs Hashing

```text
Encryption
    !=
Hashing
```

Passwords are not ordinarily stored using reversible encryption.

---

# 127. Randomness

Security-sensitive randomness must use cryptographically secure random generators.

---

# 128. UUID

UUIDs should not automatically be treated as authentication secrets.

---

# 129. TLS

External and service-to-service traffic must use TLS according to platform policy.

---

# 130. Certificate Validation

TLS certificate validation must not be disabled in production.

---

# 131. Trust-All

Trust-all certificate managers are prohibited.

---

# 132. Hostname Verification

Hostname verification must not be disabled in production.

---

# 133. Development Exceptions

Temporary local development exceptions must not silently reach production profiles.

---

# 134. HTTP Timeout

Security also depends on resource availability.

External clients require finite:

- connection timeout
- response timeout
- acquisition timeout

---

# 135. Resource Exhaustion

Unbounded waits can become availability vulnerabilities.

---

# 136. Rate Limiting

Rate limiting follows ADR-033.

---

# 137. Bulkhead

Concurrency limits follow ADR-034.

---

# 138. Payload Limits

HTTP request body size must be bounded according to use case.

---

# 139. JSON Depth

Deeply nested/unreasonably complex payloads should be bounded where parser/resource exhaustion is plausible.

---

# 140. Regex

Regular expressions processing untrusted input require review for catastrophic backtracking.

---

# 141. ReDoS

Security-sensitive regexes should avoid pathological complexity.

---

# 142. Pagination

Unbounded search/page requests are an availability/security concern.

Pagination follows ADR-036.

---

# 143. Expensive Filters

Public APIs should not expose arbitrary expensive query capabilities.

---

# 144. Sorting

Uncontrolled sorting can expose internal structure and cause expensive queries.

---

# 145. Error Handling

Errors must fail safely.

---

# 146. Client Error

Expected validation/business errors should return controlled API errors.

---

# 147. Unexpected Error

Unexpected exceptions should return a generic safe error contract.

---

# 148. Stack Trace

Never return stack traces to clients.

---

# 149. SQL Error

Never expose SQL statements/database internals to clients.

---

# 150. Internal Host

Never expose internal network topology unnecessarily.

---

# 151. Log Detail

Operational logs may contain more diagnostic information than the client response, subject to sensitive-data rules.

---

# 152. GlobalExceptionHandler

A centralized exception boundary should consistently translate internal exceptions into safe HTTP contracts.

---

# 153. Catch-All

A final catch-all handler should prevent accidental implementation-detail leakage.

---

# 154. Exception Logging

Unexpected exceptions must be logged or propagated to a layer that logs them appropriately.

---

# 155. Sonar Exception Rule

Exception handling must satisfy:

```text
Either log or rethrow appropriately.
```

---

# 156. Duplicate Logging

Avoid logging the same exception at every layer.

---

# 157. SAST

SAST is mandatory as defined by ADR-035.

---

# 158. Finding Handling

A SAST finding must be:

```text
UNDERSTOOD
     |
     +--> TRUE POSITIVE --> FIX
     |
     +--> FALSE POSITIVE --> ASSESS / DOCUMENT
```

---

# 159. Scanner-Driven Corruption

This is prohibited:

```text
Scanner complains
       |
       v
Generic sanitizer added everywhere
       |
       v
Business data changed
       |
       v
Scanner green
       |
       v
Application wrong
```

---

# 160. Root Cause

The correct question is:

```text
Which interpreter/context creates the vulnerability?
```

---

# 161. Taint Analysis

Taint-flow findings must be reviewed from:

```text
SOURCE
  |
  v
DATA FLOW
  |
  v
SINK
```

---

# 162. Sink

Security remediation should normally protect the dangerous sink/context rather than corrupting the source value globally.

---

# 163. Example: SQL

```text
User Input
    |
    v
String
    |
    v
SQL Sink
```

Correct fix:

```text
Parameterized Query
```

not:

```text
Remove apostrophes from every name
```

---

# 164. Example: HTML

```text
Business Value
    |
    v
HTML Sink
```

Correct fix:

```text
HTML-context encoding
```

not:

```text
Store HTML entities in domain data
```

---

# 165. Example: Log

```text
Remote Error
    |
    v
Log Sink
```

Correct fix:

```text
Bound / mask / safely represent
```

not:

```text
Change remote/domain data globally
```

---

# 166. Sonar Security Hotspots

Security hotspots require human review.

A hotspot is not automatically a vulnerability and must not be blindly "fixed" without understanding context.

---

# 167. Suppression

Security-rule suppression requires:

- narrow scope
- justification
- review

---

# 168. Blanket Suppression

Broad security suppressions are prohibited.

---

# 169. Dependency Security

Third-party dependencies are part of the attack surface.

---

# 170. Dependency Scanning

CI/CD should detect known dependency vulnerabilities.

---

# 171. Vulnerability Severity

Dependency findings should be prioritized according to:

- exploitability
- exposure
- severity
- available remediation
- actual usage

---

# 172. CVE Presence

A CVE in a dependency does not automatically mean the application is exploitable.

It still requires assessment.

---

# 173. Critical Vulnerability

Known exploitable critical vulnerabilities require priority remediation.

---

# 174. Dependency Upgrade

Security upgrades require regression testing.

---

# 175. Unused Dependency

Unused dependencies should be removed.

---

# 176. Dependency Minimization

Smaller dependency surfaces reduce:

- CVE exposure
- build complexity
- attack surface

---

# 177. Supply Chain

Build dependencies/plugins must come from approved repositories.

---

# 178. Version Pinning

Critical build/dependency versions should be controlled rather than floating unpredictably.

---

# 179. Artifact Integrity

Artifact integrity/signature/provenance controls should follow platform supply-chain standards.

---

# 180. SQS Security

SQS consumers must not trust event payloads solely because they originate from SQS.

---

# 181. Event Validation

Incoming events require:

- schema validation
- required field validation
- business validation

---

# 182. Event Authorization

Where event producers have differing trust levels, authorization/trust must be part of the event architecture.

---

# 183. Event Payload

Sensitive data should not be placed into events unnecessarily.

---

# 184. SQS Headers

Do not trust arbitrary identity claims from message headers unless produced by an approved trusted mechanism.

---

# 185. Deserialization Failure

Malformed SQS payloads must not cause infinite poison-message loops.

---

# 186. DLQ

Poison-message handling should follow the platform SQS resilience strategy.

---

# 187. Redis Security

Redis must not be treated as an authoritative security boundary.

---

# 188. Cache Authorization

A cached resource still requires correct authorization semantics.

---

# 189. Cache Key

Cache keys must prevent cross-tenant/cross-scope data leakage.

---

# 190. Sensitive Cache

Sensitive cached data requires appropriate TTL and protection.

---

# 191. Cache Poisoning

Externally controlled cache keys/values require validation and ownership controls.

---

# 192. Database Security

Database accounts should use least privilege.

---

# 193. Application DB User

The application database user should not have unnecessary administrative privileges.

---

# 194. Schema Migration User

Where platform design permits, migration privileges may be separated from runtime privileges.

---

# 195. Data Constraints

Database constraints provide defense in depth for important invariants.

---

# 196. Encryption at Rest

Sensitive persisted data should use platform-approved encryption-at-rest controls.

---

# 197. Field Encryption

Application-level field encryption should be introduced only when threat/risk requirements justify its operational complexity.

---

# 198. Sensitive Data Minimization

The safest unnecessary sensitive data is data that was never collected.

---

# 199. Data Retention

Retention follows the platform privacy/data governance ADR.

---

# 200. Security Testing

Security controls require automated behavioral tests.

---

# 201. Authentication Test

Verify missing/invalid authentication is rejected.

---

# 202. Authorization Test

Verify valid authentication with insufficient privilege is rejected.

---

# 203. Object Authorization Test

Verify users cannot access another unauthorized resource merely by changing the resource ID.

---

# 204. Mass Assignment Test

Verify internal protected fields cannot be set through public request payloads.

---

# 205. Injection Test

Critical dynamic query paths require injection-oriented tests.

---

# 206. XSS Contract Test

Verify valid business strings are preserved by JSON APIs.

For example:

```text
M&M
```

must remain:

```text
M&M
```

through normal API persistence/return flows unless the API contract explicitly defines otherwise.

---

# 207. Error Leakage Test

Verify unexpected errors do not expose:

- stack traces
- SQL
- secrets
- tokens

---

# 208. Secret Masking Test

Verify token/secret masking in logging utilities where such utilities exist.

---

# 209. SSRF Test

Dynamic outbound destination logic requires tests proving forbidden destinations are rejected.

---

# 210. Path Traversal Test

File handling should test traversal attempts without allowing paths outside the intended directory.

---

# 211. Security Regression

A discovered security defect should receive a regression test where feasible.

---

# 212. Logging Test

Security-sensitive log handling may require focused tests.

Do not test every log statement.

---

# 213. Concurrency Security

Security context propagation tests follow ADR-034.

---

# 214. SAST + Behavioral Test

Static analysis does not replace runtime behavioral security testing.

---

# 215. Threat Modeling

High-risk features require lightweight threat modeling.

---

# 216. Threat Model Questions

At minimum:

```text
What is the asset?

Who can call this?

What input is controlled externally?

What trust boundaries are crossed?

What could be abused?

What happens on failure?
```

---

# 217. STRIDE

STRIDE or another approved threat-modeling method may be used for higher-risk changes.

---

# 218. Security Review Triggers

Explicit security review is required when introducing:

- authentication changes
- authorization changes
- dynamic outbound URLs
- file uploads
- archive extraction
- cryptography
- sensitive-data storage
- new public APIs
- rich HTML
- custom deserialization
- administrative endpoints
- new externally reachable infrastructure

---

# 219. Administrative APIs

Administrative APIs require stronger authorization and should not be exposed publicly without explicit justification.

---

# 220. Actuator

Spring Boot Actuator endpoints require controlled exposure.

---

# 221. Health

Health information exposed externally must not reveal unnecessary infrastructure details.

---

# 222. Environment Endpoint

Sensitive Actuator endpoints such as environment/configuration information must not be publicly exposed.

---

# 223. Swagger/OpenAPI UI

API documentation UI exposure must follow environment/security policy.

---

# 224. CORS

CORS is a browser security policy and must be configured narrowly.

---

# 225. Wildcard CORS

Avoid broad:

```text
Access-Control-Allow-Origin: *
```

for authenticated sensitive applications unless explicitly justified.

---

# 226. Credentials + CORS

Credentialed cross-origin requests require explicit trusted origins.

---

# 227. CSRF

CSRF protection depends on the authentication mechanism.

---

# 228. Bearer APIs

Stateless bearer-token APIs have different CSRF characteristics from cookie/session-authenticated applications.

---

# 229. Do Not Disable Blindly

CSRF must not be disabled merely because a copied Spring Security configuration did so.

---

# 230. Security Headers

Browser-facing applications/gateways should apply appropriate security headers according to their content and deployment model.

---

# 231. API Headers

Backend JSON APIs should not mechanically add irrelevant browser headers without understanding their purpose.

---

# 232. Cache Headers

Sensitive responses require safe caching policies.

---

# 233. Token in URL

Authentication tokens must not be placed in query strings.

---

# 234. Why

URLs may appear in:

- browser history
- proxy logs
- access logs
- analytics

---

# 235. Password in URL

Passwords/secrets in URLs are prohibited.

---

# 236. Sensitive GET Parameters

Highly sensitive data should not be transported in GET query parameters when avoidable.

---

# 237. Error Message

Authentication errors should not expose unnecessary account-security information.

---

# 238. Enumeration

Login/account recovery flows must consider user enumeration risks where applicable.

---

# 239. Denial of Service

Application security includes availability.

---

# 240. Bounded Resources

The following must remain bounded:

```text
Request Size

Collection Size

Page Size

Batch Size

Concurrency

Queue Size

Timeout

Retry

File Size
```

---

# 241. Retry Storm

Security/resilience controls must prevent retries from amplifying outages.

---

# 242. Circuit Breaker

Circuit breakers follow ADR-016.

---

# 243. Concurrency Limit

Bulkheads/concurrency limits follow ADR-034.

---

# 244. API Limits

API limits follow ADR-033 and ADR-036.

---

# 245. Secure Defaults

Security-sensitive configuration must fail toward a safe state.

---

# 246. Missing Security Config

Missing required issuer/audience/security configuration should normally fail application startup rather than silently disabling protection.

---

# 247. Production Profile

Production security must not depend on developers remembering to enable it manually.

---

# 248. Debug Features

Debug/security bypasses must not be enabled in production.

---

# 249. Test Authentication

Test-only authentication mechanisms must be isolated from production configuration.

---

# 250. Fail Open vs Fail Closed

Authorization should normally fail closed.

---

# 251. Dependency Failure

A failed authorization dependency must not automatically mean:

```text
Allow request
```

---

# 252. Security Availability Tradeoff

Any fail-open security decision requires explicit architectural approval.

---

# 253. Code Review

Security is part of normal code review.

---

# 254. Reviewer Questions

Reviewers should ask:

```text
Can input reach a dangerous sink?

Can authorization be bypassed?

Can another customer's data be accessed?

Can sensitive data leak?

Can this exhaust a finite resource?

Can the caller control an outbound destination?

Can this change security semantics?
```

---

# 255. SAST Is Not Review

Passing SAST does not prove an implementation is secure.

---

# 256. Security Is Not Sanitizer

Adding a generic sanitizer is not equivalent to secure coding.

---

# 257. Anti-Patterns

The following are prohibited or strongly discouraged:

- trusting decoded but unvalidated JWTs
- treating UUID secrecy as authorization
- gateway-only authorization
- trusting internal-service input automatically
- generic HTML escaping of all backend strings
- storing HTML entities as canonical domain data
- fixing XSS by corrupting valid business values
- SQL string concatenation with user input
- arbitrary dynamic JPA/SQL sort expressions
- request binding directly to JPA entities
- arbitrary outbound URLs
- trust-all TLS
- disabled hostname verification
- hard-coded credentials
- credentials committed to Git
- secrets in logs
- bearer tokens in URLs
- complete request/response body logging by default
- stack traces returned to clients
- SQL errors returned to clients
- unsafe Jackson default typing
- arbitrary polymorphic deserialization
- filesystem paths built directly from user input
- unsafe archive extraction
- unbounded file uploads
- custom cryptography
- non-cryptographic randomness for security tokens
- blindly suppressing SAST findings
- changing valid business behavior only to satisfy scanners
- broad Sonar security suppressions
- wildcard CORS for authenticated systems without justification
- disabling CSRF without understanding authentication semantics
- public exposure of sensitive Actuator endpoints
- unbounded requests/pages/batches
- fail-open authorization without explicit approval
- assuming successful SAST means the application is secure

---

# 258. Positive Consequences

The decision provides:

- consistent secure coding
- defense in depth
- stronger authorization
- reduced injection risk
- correct XSS remediation
- preserved domain data
- safer logging
- safer outbound integrations
- improved secret handling
- stronger security testing
- better SAST remediation
- reduced scanner-driven workarounds

---

# 259. Negative Consequences

The decision introduces:

- additional security review
- more explicit validation
- authorization checks
- threat modeling
- security regression tests
- secret-management dependencies
- dependency vulnerability remediation

These costs are accepted because security failures can affect confidentiality, integrity, availability and business operations.

---

# 260. Neutral Consequences

The decision also means:

- not every SAST finding is a true vulnerability
- not every sanitizer improves security
- security encoding depends on context
- internal systems still cross trust boundaries
- UUIDs remain identifiers rather than authorization mechanisms
- some controls belong at the gateway while others remain service-owned

---

# 261. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Broken object authorization | Critical | Medium | Resource-level authorization |
| SQL injection | Critical | Low | Parameterized queries |
| Stored/reflected XSS | High | Medium | Context-sensitive encoding |
| Domain-data corruption | High | Medium | No generic HTML escaping |
| SSRF | Critical | Low | Destination allowlists |
| Path traversal | Critical | Low | Safe canonical paths |
| Secret leakage | Critical | Medium | Secret management + masking |
| JWT misuse | Critical | Low | Full token validation |
| Mass assignment | High | Medium | Dedicated request DTOs |
| Log injection | Medium | Medium | Structured safe logging |
| Vulnerable dependency | Critical | Medium | Dependency scanning |
| Resource exhaustion | High | Medium | Bounded resources |
| Security context leakage | Critical | Low | Context isolation |
| SAST false-positive workaround | High | Medium | Source-to-sink analysis |

---

# 262. Implementation Guidance

The following rules are mandatory:

1. Security follows defense in depth.
2. Internal callers are not automatically trusted.
3. Authentication and authorization must remain separate concerns.
4. JWTs must be cryptographically and semantically validated.
5. Resource-level authorization must protect business resources.
6. UUIDs must never be treated as authorization controls.
7. Least privilege applies to users, services and infrastructure.
8. External input must be bounded and validated.
9. Validation, sanitization and encoding must remain distinct.
10. Valid domain data must not be generically HTML-escaped before storage/JSON return.
11. Output encoding must match the actual sink/context.
12. Database queries must use parameter binding.
13. Dynamic sort/filter expressions must use controlled mappings.
14. Request DTOs must prevent mass assignment.
15. Arbitrary outbound URLs are prohibited without an explicit SSRF-safe model.
16. File paths must not be constructed unsafely from external input.
17. Untrusted polymorphic deserialization is prohibited.
18. Structured logging is preferred.
19. Secrets/tokens must never be logged.
20. Remote error bodies must be bounded and masked where necessary.
21. Secrets must not be stored in source code.
22. TLS validation must not be disabled in production.
23. Custom cryptography is prohibited.
24. Security-sensitive randomness requires secure random generation.
25. Client-facing errors must not expose implementation details.
26. SAST findings must be analyzed from source to sink.
27. Security fixes must address the vulnerable sink/context rather than corrupting source data.
28. Sonar security hotspots require human review.
29. Dependency vulnerabilities require risk-based assessment and remediation.
30. SQS payloads require validation.
31. Cache design must prevent cross-scope data leakage.
32. Database accounts must follow least privilege.
33. Critical security controls require automated behavioral tests.
34. High-risk features require threat modeling.
35. Application resources must remain bounded.
36. Authorization must normally fail closed.
37. Security configuration must use secure production defaults.
38. Test/debug bypasses must never reach production.
39. Security remains part of normal code review.
40. Passing SAST/Sonar does not replace secure design review.

---

# 263. Security Production Readiness Gate

A service/change is not production ready until:

```text
[ ] Authentication reviewed

[ ] JWT validation reviewed

[ ] Authorization reviewed

[ ] Object-level authorization reviewed

[ ] Least privilege reviewed

[ ] Request DTO reviewed

[ ] Mass assignment reviewed

[ ] Input lengths bounded

[ ] Collection sizes bounded

[ ] Numeric ranges bounded

[ ] Enum validation reviewed

[ ] SQL/JPQL parameterization reviewed

[ ] Dynamic sorting reviewed

[ ] Dynamic filtering reviewed

[ ] XSS/output context reviewed

[ ] Valid domain values preserved

[ ] SSRF exposure reviewed

[ ] Outbound destinations reviewed

[ ] File handling reviewed

[ ] Path traversal reviewed

[ ] Deserialization reviewed

[ ] Sensitive logging reviewed

[ ] Token masking reviewed

[ ] Error responses reviewed

[ ] Secret storage reviewed

[ ] TLS validation reviewed

[ ] Dependency vulnerabilities reviewed

[ ] SQS input reviewed

[ ] Cache isolation reviewed

[ ] Database privileges reviewed

[ ] Resource limits reviewed

[ ] Security tests pass

[ ] SAST passes / findings assessed

[ ] Sonar security hotspots reviewed

[ ] Threat model completed where required

[ ] Code review completed
```

---

# 264. Validation

This ADR will be validated through:

- code reviews
- architecture reviews
- threat modeling
- authentication tests
- authorization tests
- object-level authorization tests
- security integration tests
- SAST
- SonarQube
- dependency scanning
- secret scanning
- API security tests
- injection tests
- SSRF tests
- file-security tests
- logging tests
- penetration testing where required
- production security monitoring

---

# 265. Success Criteria

The decision is successful when:

- valid domain data is preserved
- authorization is enforced at resource level
- APIs do not leak implementation details
- SQL injection paths are eliminated
- XSS is handled at the correct rendering context
- arbitrary outbound destinations are prevented
- secrets do not appear in source or logs
- SAST findings are resolved without business-data corruption
- dependency vulnerabilities are actively managed
- security regressions are detected before production
- security controls remain effective across concurrent execution
- services fail safely under invalid/malicious input

---

# 266. Alternatives Rejected

## 266.1 Gateway-Only Security

Rejected because business authorization belongs to the service/domain boundary.

---

## 266.2 Generic Backend HTML Escaping

Rejected because encoding is context-sensitive and generic escaping corrupts valid domain data.

---

## 266.3 Sanitizing Input Instead of Parameterized SQL

Rejected because altering input is not a reliable SQL-injection defense.

---

## 266.4 Trust Internal Services

Rejected because compromised/misconfigured internal systems remain a threat source.

---

## 266.5 SAST as the Security Architecture

Rejected because static analysis detects classes of issues but cannot establish complete security correctness.

---

## 266.6 UUIDs as Access Control

Rejected because identifiers do not establish caller permission.

---

## 266.7 Trust-All TLS for Operational Convenience

Rejected because it defeats server identity verification.

---

# 267. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-030: Adopt SQS Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-032: Adopt Distributed Caching and Cache Consistency Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards

---

# 268. References

- OWASP Top 10
- OWASP API Security Top 10
- OWASP ASVS
- OWASP Cheat Sheet Series
- Spring Security Documentation
- Java 21 Security Documentation
- Jakarta Bean Validation
- Jackson Documentation
- SonarQube Security Rules
- CWE
- NIST Secure Software Development Framework
- RFC 7519: JSON Web Token

---

# 269. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial application security baseline |

---

# 270. Decision Summary

The definitive security model is:

```text
                    UNTRUSTED INPUT
                          |
                          v
                     VALIDATION
                          |
                          v
                   CANONICAL DOMAIN
                         VALUE
                          |
             +------------+------------+
             |            |            |
             v            v            v
          DATABASE       JSON         LOG
             |            |            |
             v            v            v
       PARAMETERS     SERIALIZER   SAFE LOGGING
```

Not:

```text
INPUT
  |
  v
GENERIC SANITIZER
  |
  v
MODIFIED BUSINESS DATA
  |
  v
EVERYTHING ELSE
```

For XSS:

```text
M&M
 |
 v
DOMAIN
 |
 v
DATABASE
 |
 v
JSON API
 |
 v
"M&M"
 |
 v
FRONTEND HTML CONTEXT
 |
 v
HTML ENCODING
```

not:

```text
M&M
 |
 v
BACKEND GENERIC ESCAPE
 |
 v
M&amp;M
 |
 v
DATABASE
 |
 v
JSON
 |
 v
M&amp;M
 |
 v
POSSIBLE DOUBLE ENCODING
```

For SQL injection:

```text
INPUT
  |
  v
VALIDATION
  |
  v
BIND PARAMETER
  |
  v
DATABASE
```

not:

```text
INPUT
  |
  v
REMOVE QUOTES
  |
  v
STRING CONCATENATION
  |
  v
SQL
```

For authorization:

```text
JWT
 |
 v
AUTHENTICATION
 |
 v
IDENTITY
 |
 v
ROLE / PROFILE
 |
 v
RESOURCE AUTHORIZATION
 |
 v
BUSINESS RULE
 |
 v
ALLOW / DENY
```

For SAST:

```text
             FINDING
                |
                v
             SOURCE
                |
                v
            DATA FLOW
                |
                v
              SINK
                |
        +-------+-------+
        |               |
        v               v
 REAL RISK        FALSE POSITIVE
        |               |
        v               v
FIX CORRECT       DOCUMENT /
CONTEXT           ASSESS
```

The central engineering rule is:

```text
Do not ask:

"How do we make the scanner green?"

Ask:

"What is the dangerous sink,
what context interprets the data,
and what control belongs at that boundary?"
```

And the security hierarchy is:

```text
IDENTITY
   |
   v
AUTHORIZATION
   |
   v
VALIDATION
   |
   v
BUSINESS RULE
   |
   v
SAFE DATA ACCESS
   |
   v
CONTEXT-SENSITIVE OUTPUT
   |
   v
SAFE OBSERVABILITY
```

The objective is not merely:

```text
Sonar = Green
SAST  = Green
```

The objective is:

```text
SECURE
+
CORRECT
+
MAINTAINABLE
+
OBSERVABLE
+
BUSINESS DATA PRESERVED
```
