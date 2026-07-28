# ADR-067: Adopt Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-067 |
| Title | Adopt Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Error Handling, Exceptions, REST, Problem Details, Integration Failures |
| Related Work Items | Spring Boot, RFC 9457, WebClient, RestClient, SQS, SQS, Resilience4j |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Errors are observable integration behavior.

A failure can originate from:

```text
CLIENT INPUT

AUTHENTICATION

AUTHORIZATION

DOMAIN RULE

DATABASE

REMOTE HTTP SERVICE

REDIS

SQS

SQS

TIMEOUT

CIRCUIT BREAKER

PROGRAMMING DEFECT
```

If all failures are handled as generic:

```text
500 Internal Server Error
```

the system becomes difficult to:

```text
Integrate

Operate

Monitor

Retry Safely

Diagnose

Support
```

The opposite extreme is also problematic:

```text
Every Exception
    |
    v
Expose Internal Details
    |
    v
CLIENT
```

Error handling must therefore balance:

```text
Correct Protocol Semantics

Stable Contracts

Useful Diagnostics

Security

Retryability

Operational Visibility
```

---

# 2. Problem Statement

The organization requires standards covering:

- exception taxonomy
- domain exceptions
- validation exceptions
- integration exceptions
- infrastructure exceptions
- error codes
- RFC 9457 Problem Details
- `@RestControllerAdvice`
- HTTP status mapping
- Bean Validation errors
- business errors
- authentication errors
- authorization errors
- not-found errors
- conflict errors
- remote HTTP failures
- WebClient
- RestClient
- timeout
- Circuit Breaker
- retryability
- SQS errors
- SQS errors
- DLQ
- correlation IDs
- tracing IDs
- internationalization
- sanitization
- stack traces
- logging
- Sonar exception rules
- sensitive information
- failure contracts between services

---

# 3. Decision Drivers

Primary drivers are:

1. predictable API behavior
2. stable integration contracts
3. secure error exposure
4. diagnosability
5. retry correctness
6. maintainability
7. observability
8. client usability
9. distributed failure isolation
10. auditability
11. internationalization
12. automated testing

---

# 4. Decision

The platform adopts an explicit exception taxonomy internally and RFC 9457 Problem Details as the standard REST failure representation.

Canonical flow:

```text
FAILURE
   |
   v
CLASSIFY
   |
   +--> VALIDATION
   |
   +--> DOMAIN
   |
   +--> SECURITY
   |
   +--> RESOURCE
   |
   +--> CONFLICT
   |
   +--> REMOTE
   |
   +--> INFRASTRUCTURE
   |
   +--> UNEXPECTED
   |
   v
TRANSLATE AT BOUNDARY
   |
   v
STABLE FAILURE CONTRACT
```

---

# 5. Fundamental Principle

```text
Exceptions are internal control
and diagnostic mechanisms.

Failure contracts are external
integration contracts.

Do not expose internal exception
structure directly to clients.
```

---

# 6. Exception Taxonomy

Exceptions SHOULD be classified according to semantics rather than technical convenience.

A reference taxonomy is:

```text
ApplicationException
    |
    +--> ValidationException
    |
    +--> BusinessRuleException
    |
    +--> ResourceNotFoundException
    |
    +--> ConflictException
    |
    +--> AuthorizationException
    |
    +--> IntegrationException
    |       |
    |       +--> RemoteValidationException
    |       +--> RemoteUnavailableException
    |       +--> RemoteTimeoutException
    |       +--> RemoteRateLimitException
    |
    +--> InfrastructureException
```

Exact class hierarchy MAY remain simpler where additional inheritance adds no value.

---

# 7. Semantic Exceptions

Exception names MUST communicate failure meaning.

Prefer:

```text
OrderNotFoundException

InvalidOrderTransitionException

CustomerNotEligibleException

RemoteServiceUnavailableException
```

over:

```text
ServiceException

GenericException

ApplicationError
```

---

# 8. Generic Exception

A generic catch-all application exception SHOULD NOT become the normal mechanism for every failure.

---

# 9. Domain Exception

Domain/business exceptions represent violations of business invariants or requested operations that are invalid in the current business state.

---

# 10. Domain Exception Example

Examples:

```text
OrderAlreadyCancelledException

OrderApprovalNotAllowedException

CreditLimitExceededException
```

---

# 11. Technical Exception

Technical/infrastructure failures MUST remain distinguishable from business rejection.

---

# 12. Business vs Technical

These are semantically different:

```text
CUSTOMER NOT ELIGIBLE
```

and:

```text
CUSTOMERS SERVICE UNAVAILABLE
```

They MUST NOT map to the same error code merely because both prevent the operation.

---

# 13. Validation

Validation failures SHOULD be separated into:

```text
STRUCTURAL VALIDATION

SEMANTIC / BUSINESS VALIDATION
```

---

# 14. Structural Validation

Examples:

```text
Missing field

Invalid length

Invalid format

Malformed UUID

Unsupported enum syntax
```

---

# 15. Business Validation

Examples:

```text
Order does not belong to customer

Transition is not allowed

Customer status prevents checkout
```

---

# 16. Bean Validation

Jakarta Bean Validation failures MUST be translated into a stable validation-error contract.

---

# 17. Binding Errors

Malformed request bodies and type-conversion failures MUST be handled predictably.

---

# 18. Malformed JSON

Malformed JSON SHOULD normally produce:

```text
400 Bad Request
```

with a safe error contract.

---

# 19. Unknown JSON Field

Unknown-field behavior MUST be explicit.

The platform MAY either:

```text
Reject Unknown Fields

or

Ignore Unknown Fields
```

according to compatibility policy.

---

# 20. Problem Details

REST APIs MUST use RFC 9457-style Problem Details for standardized errors.

---

# 21. Base Fields

The canonical fields are:

```text
type

title

status

detail

instance
```

---

# 22. Platform Extensions

The platform SHOULD support applicable extensions such as:

```text
errorCode

timestamp

correlationId

traceId

violations

dependency
```

---

# 23. Example

```json
{
  "type": "https://api.example.com/problems/order-not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "The requested order could not be found.",
  "instance": "/orders/8b8b24f2-5708-4daf-b850-0a15dd79d857",
  "errorCode": "ORDER_NOT_FOUND",
  "correlationId": "64cc1dcf-6b1e-4ad5-bde9-5ea8fc4da860"
}
```

---

# 24. Stable Error Code

`errorCode` MUST be machine-readable and stable.

---

# 25. Message Is Not Contract

Clients MUST NOT be required to parse:

```text
detail

title

localized message
```

to determine failure type.

---

# 26. Error Code Naming

Error codes SHOULD use stable uppercase identifiers.

Example:

```text
ORDER_NOT_FOUND

VALIDATION_ERROR

CUSTOMER_NOT_ELIGIBLE

DEPENDENCY_UNAVAILABLE
```

---

# 27. Error Code Scope

Specific codes SHOULD be used when clients can meaningfully react differently.

---

# 28. Excessive Error Codes

Do not create a unique external error code for every internal exception if consumers gain no value.

---

# 29. Type URI

Problem `type` SHOULD identify a documented problem category when appropriate.

---

# 30. `about:blank`

Generic `about:blank` MAY be used when no dedicated documented problem type is necessary.

---

# 31. Title

`title` SHOULD be concise and category-oriented.

---

# 32. Detail

`detail` SHOULD explain the specific occurrence safely.

---

# 33. Instance

`instance` SHOULD identify the affected request/resource context without exposing sensitive query information.

---

# 34. Timestamp

A timestamp MAY be included for diagnostic correlation.

---

# 35. Correlation ID

Failure responses SHOULD include the request correlation identifier where available.

---

# 36. Trace ID

`traceId` MAY be returned where platform policy permits it and where it improves operational support.

---

# 37. Internal IDs

Internal database/server identifiers MUST NOT be exposed merely for debugging convenience.

---

# 38. Validation Contract

Validation failures SHOULD include structured violations.

---

# 39. Violation Example

```json
{
  "type": "https://api.example.com/problems/validation",
  "title": "Request validation failed",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "violations": [
    {
      "field": "segment",
      "code": "NotBlank",
      "message": "Segment is required"
    }
  ]
}
```

---

# 40. Field Name

Validation responses SHOULD use external contract field names rather than internal Java property paths where they differ.

---

# 41. Nested Validation

Nested validation paths SHOULD remain understandable to clients.

Example:

```text
orders[2].statusOrder
```

---

# 42. Validation Order

Clients MUST NOT depend on one particular order of validation violations unless the contract explicitly guarantees it.

---

# 43. Global Validation

Cross-field validation failures MAY use:

```text
field = null
```

or a defined logical object path.

---

# 44. HTTP Status Mapping

HTTP status mapping MUST remain consistent.

---

# 45. 400

Use:

```text
400 Bad Request
```

for malformed or structurally invalid requests.

---

# 46. 401

Use:

```text
401 Unauthorized
```

for missing/invalid authentication.

---

# 47. 403

Use:

```text
403 Forbidden
```

for authenticated callers denied access.

---

# 48. 404

Use:

```text
404 Not Found
```

for inaccessible/nonexistent resources according to resource-hiding policy.

---

# 49. 409

Use:

```text
409 Conflict
```

for conflict with existing state.

Examples:

```text
Duplicate resource

Optimistic locking conflict

Invalid state transition where conflict semantics fit
```

---

# 50. 412

Use:

```text
412 Precondition Failed
```

for failed HTTP preconditions such as conditional update headers.

---

# 51. 422

`422 Unprocessable Content` MAY be used for semantic validation if adopted consistently.

The platform MUST avoid inconsistent arbitrary use of both 400 and 422 for identical failure categories.

---

# 52. 429

Use:

```text
429 Too Many Requests
```

for client-facing rate limiting.

---

# 53. 500

Use:

```text
500 Internal Server Error
```

for unexpected internal failures.

---

# 54. 502

Use:

```text
502 Bad Gateway
```

where this service is acting as a gateway/integration boundary and receives an unusable upstream response.

---

# 55. 503

Use:

```text
503 Service Unavailable
```

when a required dependency or local service condition prevents temporary processing.

---

# 56. 504

`504 Gateway Timeout` MAY be used when gateway/upstream timeout semantics apply.

---

# 57. No 200 Error

Failed operations MUST NOT return:

```text
200 OK
```

with an embedded error object.

---

# 58. Exception Translation Boundary

Internal exceptions SHOULD be translated near the external boundary.

For REST:

```text
@RestControllerAdvice
```

is the preferred centralized mechanism.

---

# 59. Global Handler

A global exception handler SHOULD:

```text
Classify

Map Status

Resolve Error Code

Resolve Safe Message

Add Correlation

Return ProblemDetail
```

---

# 60. Handler Business Logic

`@RestControllerAdvice` MUST NOT become a large business-rule engine.

---

# 61. Exception-to-Status Mapping

Mapping SHOULD be deterministic and testable.

---

# 62. Example Mapping

```text
ResourceNotFoundException
    ->
404 / RESOURCE_NOT_FOUND

BusinessConflictException
    ->
409 / BUSINESS_CONFLICT

RemoteTimeoutException
    ->
503 or 504 according to contract

Unexpected Exception
    ->
500 / INTERNAL_ERROR
```

---

# 63. Catch-All Handler

A final catch-all handler SHOULD exist for unexpected exceptions.

---

# 64. Safe 500

Unexpected error responses MUST use a safe generic external message.

---

# 65. Internal Stack Trace

Stack traces MUST NOT be returned to API clients.

---

# 66. Exception Message Exposure

Raw exception messages MUST NOT automatically populate `detail`.

---

# 67. Database Exception

Do not expose:

```text
SQL

table names

constraint internals

JDBC URL

database host
```

unless deliberately sanitized and contractually useful.

---

# 68. Constraint Violation

Known database constraint violations SHOULD be translated into meaningful domain/application errors where practical.

---

# 69. Unknown Constraint Failure

Unknown persistence failures SHOULD remain infrastructure/internal errors.

---

# 70. Internationalization

Human-readable error messages MAY be internationalized.

---

# 71. Stable Semantics

Localization MUST NOT change:

```text
HTTP status

errorCode

problem type
```

---

# 72. Message Key

Internal exceptions MAY carry a stable message key rather than hardcoded final user text.

---

# 73. Locale

Locale resolution SHOULD follow approved application/API conventions.

---

# 74. Missing Translation

A missing translation MUST have a safe fallback.

---

# 75. HTML Escaping

HTML escaping MUST NOT be applied indiscriminately to ordinary JSON API business data.

---

# 76. M&M Example

A legitimate business value:

```text
M&M
```

MUST remain:

```text
M&M
```

and MUST NOT become:

```text
M&amp;M
```

because of generic response sanitization.

---

# 77. Output Context

Output escaping belongs to the rendering context that requires it.

JSON REST contracts MUST preserve legitimate data semantics.

---

# 78. SAST Sanitization

SAST remediation MUST address the actual unsafe sink/source boundary rather than globally mutating valid business content.

---

# 79. Error Sanitization

Error messages derived from remote/untrusted sources MUST be sanitized before logging or propagation.

---

# 80. Remote Error Payload

A remote error body MUST NOT be blindly returned to the caller.

---

# 81. Remote Error Extractor

Remote error extraction SHOULD:

```text
Parse Safely

Limit Length

Normalize Control Characters

Mask Credentials

Extract Approved Message/Code

Fallback Safely
```

---

# 82. Maximum Error Length

Externally sourced error messages SHOULD have maximum length limits.

---

# 83. Newline Sanitization

Remote/logged error text SHOULD normalize unsafe:

```text
\r

\n

\t
```

where required for log safety.

---

# 84. Secret Masking

Remote failure text MUST mask values such as:

```text
Bearer tokens

Authorization headers

Passwords

API keys

Client secrets
```

---

# 85. Null-Safe Sanitization

Sanitization utilities MUST remain null-safe.

---

# 86. Sanitization Return

A sanitizer MUST NOT unexpectedly return `null` when callers assume a valid string unless its contract explicitly allows it.

---

# 87. WebClient

WebClient-based integrations MUST map remote failures explicitly.

---

# 88. RestClient

RestClient-based integrations MUST follow the same semantic error-taxonomy standard.

---

# 89. Protocol Client Boundary

Client adapters SHOULD own:

```text
HTTP Status Interpretation

Remote Payload Parsing

Transport Exceptions

Dependency Naming
```

---

# 90. Service Layer

Application services SHOULD consume meaningful client exceptions rather than raw framework exceptions where practical.

---

# 91. Raw WebClient Exception

`WebClientResponseException` SHOULD NOT leak across the entire application architecture as the business error abstraction.

---

# 92. Raw RestClient Exception

Framework-specific REST-client exceptions SHOULD be translated at the integration boundary.

---

# 93. Remote 400

A downstream `400` does not automatically mean the current service should return `400`.

The service MUST interpret the dependency contract.

---

# 94. Remote Business Validation

If a downstream service returns a known business rejection, this MAY translate into a local business/integration validation error.

---

# 95. Remote 401/403

Authentication/authorization failure against a downstream service usually indicates:

```text
Configuration

Credential

Delegation

Permission
```

failure and MUST NOT be blindly exposed as the caller's own 401/403.

---

# 96. Remote 404

A downstream 404 MUST be interpreted according to local semantics.

---

# 97. Remote 409

A downstream conflict MAY be propagated semantically when the same business conflict remains meaningful to the caller.

---

# 98. Remote 429

Remote rate limiting SHOULD translate to a retryable dependency/rate-limit classification.

---

# 99. Remote 5xx

Remote server failures SHOULD become integration/dependency failures.

---

# 100. Connection Failure

Connection errors SHOULD map to:

```text
DEPENDENCY_UNAVAILABLE
```

or a more specific internal classification.

---

# 101. Timeout

Timeouts MUST have a dedicated classification.

---

# 102. Timeout vs Unavailable

A timeout is not semantically identical to immediate connection refusal.

Operational telemetry SHOULD distinguish them.

---

# 103. Retryability

Exception/failure classification MUST expose whether a failure is potentially retryable when retry behavior depends on it.

---

# 104. Retryable Examples

Potentially retryable:

```text
Timeout

Connection reset

HTTP 503

Selected HTTP 429
```

depending on idempotency and policy.

---

# 105. Non-Retryable Examples

Normally non-retryable:

```text
Malformed request

Business rule violation

403 Forbidden

Unsupported operation
```

---

# 106. Retry Is Not Exception Handling

Throwing a retryable exception MUST NOT itself imply unlimited retry.

Retry policy belongs to ADR-055 resilience rules.

---

# 107. Circuit Breaker Open

Circuit Breaker rejection MUST be distinguishable from an actual remote call failure.

---

# 108. Call Not Attempted

When a Circuit Breaker is OPEN:

```text
NO REMOTE REQUEST OCCURRED
```

This distinction SHOULD remain visible operationally.

---

# 109. Bulkhead Rejection

Bulkhead/concurrency-limit rejection SHOULD have a dedicated overload classification where meaningful.

---

# 110. Rate Limiter Rejection

Internal rate-limiter rejection SHOULD remain distinguishable from downstream rate limiting.

---

# 111. Fallback

Fallback MUST NOT silently suppress a failure when the caller needs to know degraded semantics.

---

# 112. Silent Empty Fallback

This is prohibited when failure matters:

```java
catch (RemoteException ex) {
    return List.of();
}
```

---

# 113. SQS

SQS consumer failures MUST use explicit failure classification.

---

# 114. SQS Categories

Typical categories:

```text
Deserialization Failure

Schema Failure

Business Validation Failure

Transient Dependency Failure

Permanent Processing Failure
```

---

# 115. Deserialization Failure

An unreadable event MUST NOT be repeatedly retried indefinitely.

---

# 116. Schema Failure

Incompatible schema failure SHOULD be treated as a contract/integration defect.

---

# 117. Business Event Failure

A business-invalid message SHOULD normally be non-retryable unless external state can legitimately change the result.

---

# 118. Transient Event Failure

Temporary infrastructure/dependency failures MAY be retried according to ADR-057.

---

# 119. Poison Message

Poison messages MUST eventually leave the normal processing path.

---

# 120. DLQ

DLQ records SHOULD preserve:

```text
Original Event Identity

Failure Classification

Retry Count

Safe Failure Context
```

---

# 121. DLQ Secret Safety

DLQ failure metadata MUST NOT expose credentials or sensitive stack traces indiscriminately.

---

# 122. SQS

SQS failure handling MUST use equivalent classification principles.

---

# 123. Visibility Timeout

Processing failure MUST respect SQS delivery and visibility semantics.

---

# 124. maxReceiveCount

Repeated processing failures MUST eventually follow configured DLQ/redrive policy.

---

# 125. Async Failure Contract

Asynchronous failures cannot return HTTP Problem Details to the original caller after the request has completed.

They require:

```text
Retry

DLQ

Status Resource

Notification

Operational Alert
```

according to the workflow.

---

# 126. Background Job Failure

Background jobs SHOULD persist or expose failure status when clients need asynchronous result visibility.

---

# 127. Logging Boundary

Unexpected exceptions SHOULD normally be logged once at the responsible outer boundary.

---

# 128. Log or Rethrow

The platform adopts:

```text
HANDLE AND LOG

or

RETHROW
```

rather than mechanical logging at every layer.

---

# 129. Sonar Rule

Code MUST comply with Sonar guidance equivalent to:

```text
Either log or rethrow this exception.
```

without creating duplicate exception logs.

---

# 130. Correct Catch

A catch block may:

```text
Translate and rethrow

Recover meaningfully

Return a safe fallback when contractually valid

Log if this is the responsible boundary
```

---

# 131. Incorrect Catch

Avoid:

```java
catch (Exception ex) {
    // do nothing
}
```

---

# 132. Duplicate Logging

Avoid:

```text
CLIENT ADAPTER logs

SERVICE logs

CONTROLLER logs

ADVICE logs
```

for the same failure.

---

# 133. Translation Without Log

An integration adapter MAY translate:

```text
WebClientRequestException
```

into:

```text
RemoteUnavailableException
```

without logging if the outer failure boundary will log it appropriately.

---

# 134. Context Preservation

Translated exceptions SHOULD preserve the original cause.

---

# 135. Cause

Prefer:

```java
throw new RemoteUnavailableException(..., ex);
```

when preserving cause is useful.

---

# 136. Cause Loss

Creating a new exception without the original cause SHOULD be avoided for unexpected technical failures.

---

# 137. Sensitive Cause

Preserving internal cause does not mean exposing it externally.

---

# 138. Stack Trace Policy

Unexpected technical failures SHOULD retain internal stack traces.

---

# 139. Expected Business Exception

Expected business rejections MAY not require stack traces at ERROR level.

---

# 140. Stack Trace Noise

Do not emit stack traces for every routine validation failure.

---

# 141. Error Logging Context

Useful error logs SHOULD include applicable:

```text
Operation

Dependency

Resource ID

Correlation ID

traceId

elapsedMs

Failure Classification
```

---

# 142. Sensitive Context

Context MUST remain free of secrets and unnecessary PII.

---

# 143. elapsedMs

Remote/infrastructure failure diagnostics SHOULD include elapsed duration where useful.

---

# 144. Correlation Propagation

Failure handling MUST preserve correlation context across:

```text
HTTP

SQS

SQS

Async Tasks
```

where supported.

---

# 145. Metrics

Error categories SHOULD produce bounded aggregate metrics where operationally valuable.

---

# 146. Good Metric Dimensions

Examples:

```text
operation

dependency

failure_category

status
```

---

# 147. Bad Metric Dimensions

Do not use:

```text
exception_message

orderId

customerId

traceId

raw URL
```

as unbounded metric labels.

---

# 148. Alerting

Alerts SHOULD use aggregate failure symptoms rather than individual exception occurrences.

---

# 149. Error Rate

Critical APIs SHOULD monitor error rate by meaningful operation/status category.

---

# 150. Dependency Error Rate

Critical integrations SHOULD monitor:

```text
Timeouts

Connection Failures

5xx

Rate Limits

Circuit Open
```

---

# 151. Error Budget

Unexpected server errors SHOULD contribute appropriately to service reliability SLI/SLO calculations.

---

# 152. Client Errors and SLO

Expected caller-invalid 4xx errors SHOULD NOT automatically count as service availability failure.

---

# 153. Server-Caused 4xx

Some 4xx responses caused by provider defects MAY still indicate service-quality problems and require analysis.

---

# 154. Problem Details Documentation

Published APIs MUST document their stable error responses.

---

# 155. OpenAPI

OpenAPI SHOULD document:

```text
HTTP status

Problem schema

errorCode

validation violations

representative examples
```

---

# 156. Consumer Contract

Consumers SHOULD depend on:

```text
Status

errorCode

documented fields
```

rather than exact human text.

---

# 157. Error Evolution

Adding an optional error-response field is generally backward compatible.

---

# 158. Error Field Removal

Removing a published error field is potentially breaking.

---

# 159. Error Code Removal

Removing or changing the meaning of a published error code is breaking when consumers rely on it.

---

# 160. Error Code Addition

Adding new possible error codes may affect clients with exhaustive handling and SHOULD follow compatibility review.

---

# 161. Enum Failure Code

Clients SHOULD be designed defensively for unknown future error codes where practical.

---

# 162. Inter-Service Errors

Internal service-to-service REST calls SHOULD still use stable failure contracts.

---

# 163. Internal Does Not Mean Unstructured

Internal APIs MUST NOT rely on parsing arbitrary exception text merely because both systems are maintained by the same organization.

---

# 164. Remote Error Preservation

A local service MAY preserve a downstream stable error code when:

```text
the semantic meaning remains correct,

the code is part of an approved shared contract,

and exposing it does not leak internal boundaries.
```

---

# 165. Error Translation

Otherwise, translate into a local stable error code.

---

# 166. Leaky Dependency Contract

Public clients SHOULD NOT need to know every internal downstream service name.

---

# 167. Dependency Field

A `dependency` field SHOULD normally be reserved for internal/controlled APIs or operational diagnostics.

---

# 168. Public Error

Public API errors SHOULD expose business-facing semantics rather than internal topology.

---

# 169. Partial Success

Bulk operations with partial success MUST represent item-level errors explicitly.

---

# 170. Bulk Error Example

```json
{
  "results": [
    {
      "id": "A",
      "status": "SUCCESS"
    },
    {
      "id": "B",
      "status": "FAILED",
      "errorCode": "ORDER_INVALID_STATUS",
      "detail": "Order cannot be approved in its current state."
    }
  ]
}
```

---

# 171. Bulk Global Failure

A request-wide failure SHOULD remain separate from per-item failures.

---

# 172. Atomic Bulk

For atomic bulk operations, one item failure MAY fail the entire request according to documented semantics.

---

# 173. Error Determinism

The same logical failure SHOULD generally map to the same status/errorCode regardless of which internal path discovered it.

---

# 174. Error Precedence

When multiple validations fail, precedence SHOULD be predictable where only one error is returned.

---

# 175. Prefer Structured Multi-Violation

For structural request validation, returning multiple violations MAY improve client usability.

---

# 176. Domain Validation Aggregation

Business validation SHOULD only aggregate failures when doing so preserves domain semantics and does not execute unnecessary expensive checks.

---

# 177. Fail Fast Internally

Fail-fast internal validation MAY be appropriate when later checks depend on earlier invariants.

---

# 178. Security Failure

Security-sensitive failure messages SHOULD avoid revealing whether protected resources or accounts exist where enumeration risk applies.

---

# 179. Authentication Detail

Avoid overly specific responses such as:

```text
User exists but password is wrong.
```

when the authentication architecture requires generic responses.

---

# 180. Authorization Detail

Authorization responses SHOULD not reveal hidden resource data.

---

# 181. Exception Serialization

Exception objects MUST NEVER be serialized directly as API responses.

---

# 182. Cause Serialization

Nested exception causes MUST NOT appear in public responses.

---

# 183. `toString()`

Exception `toString()` output MUST NOT be returned as a client-facing contract.

---

# 184. Error Persistence

Persistent error records such as Outbox/DLQ `last_error` MUST have bounded length.

---

# 185. `last_error`

`last_error` SHOULD store a safe operational summary rather than unlimited stack traces.

---

# 186. Full Diagnostic

Full stack traces belong in controlled logging/observability systems, not small transactional error columns.

---

# 187. Database Column Limit

Persistent error-text fields MUST use explicit reasonable limits appropriate to schema and operational use.

---

# 188. Truncation

Truncation SHOULD preserve a recognizable marker such as:

```text
...
```

when text has been shortened.

---

# 189. Safe Truncation

Sensitive data MUST be masked before truncation.

---

# 190. Failure Persistence

Retry metadata SHOULD preserve:

```text
failure category

attempt count

next attempt

safe last error
```

where relevant.

---

# 191. Exception Construction

Exceptions SHOULD contain enough structured context to map them without parsing their message.

---

# 192. Bad Mapping

Avoid:

```java
if (ex.getMessage().contains("not found")) {
    ...
}
```

---

# 193. Structured Exception

Prefer explicit:

```text
Exception Type

Error Code

Context Fields
```

---

# 194. Exception Message

Exception messages SHOULD remain diagnostic and human-readable, but MUST NOT be the primary machine classification mechanism.

---

# 195. Checked vs Unchecked

Application/domain exceptions MAY use unchecked exceptions where callers cannot reasonably recover locally.

---

# 196. Checked Exception

Checked exceptions MAY remain appropriate when the Java API genuinely requires callers to make a local recovery decision.

---

# 197. Framework Exceptions

Framework-specific checked/unchecked behavior MUST NOT dictate the public failure contract.

---

# 198. Exception Wrapping

Wrapping SHOULD add semantic meaning.

---

# 199. Wrapper Without Meaning

This is discouraged:

```java
catch (Exception ex) {
    throw new RuntimeException(ex);
}
```

when it adds no useful semantics.

---

# 200. Retry Classification Interface

Retry-sensitive exception types MAY expose controlled metadata such as:

```text
retryable = true/false
```

through type semantics or policy mapping.

---

# 201. Do Not Put Policy Everywhere

Retry decision SHOULD remain centralized rather than every exception independently defining arbitrary retry behavior.

---

# 202. Testing Strategy

Failure contracts MUST have automated tests.

---

# 203. Handler Tests

Global exception-handler tests SHOULD verify:

```text
HTTP status

errorCode

safe detail

correlation ID

validation violations
```

---

# 204. Sensitive Data Tests

Tests MUST verify secrets are not exposed in:

```text
Problem Details

Logs

Remote error extraction

Persistent failure summaries
```

---

# 205. Remote Client Tests

HTTP client tests SHOULD verify mapping for:

```text
2xx

400

401

403

404

409

429

5xx

Timeout

Connection Failure
```

as applicable.

---

# 206. Circuit Breaker Test

Tests SHOULD distinguish:

```text
Remote failure

CircuitBreaker OPEN
```

---

# 207. Retryability Test

Failure classification tests SHOULD verify which failures are eligible/ineligible for retry.

---

# 208. SQS Tests

SQS consumer tests SHOULD verify:

```text
Retryable failure

Non-retryable failure

DLQ routing

Poison message handling
```

---

# 209. SQS Tests

SQS workers SHOULD verify equivalent redrive/failure behavior.

---

# 210. i18n Test

Localized human messages SHOULD be tested without tying machine contract assertions to one language where unnecessary.

---

# 211. AssertJ

Java tests MUST follow project standards, including meaningful:

```java
.as("...")
```

descriptions before applicable AssertJ assertions.

---

# 212. Exception Assertion

Prefer:

```java
assertThatThrownBy(...)
        .as("invalid transition should be rejected")
        .isInstanceOf(...)
        .hasMessageContaining(...);
```

where appropriate.

---

# 213. Stable Test Data

Tests SHOULD use stable identifiers rather than random UUIDs when deterministic fixtures are appropriate.

---

# 214. No Internal Leakage Test

Public handler tests SHOULD explicitly verify absence of:

```text
stackTrace

SQL

password

authorization token
```

when relevant.

---

# 215. Error Contract Test

Critical service integrations SHOULD have contract tests around published error semantics.

---

# 216. Error Review Checklist

Material changes SHOULD evaluate:

```text
[ ] What failure category is this?

[ ] Is it business or technical?

[ ] What is the stable errorCode?

[ ] What HTTP status is correct?

[ ] Is this retryable?

[ ] Is idempotency relevant?

[ ] Is the exception translated at the right boundary?

[ ] Could the same exception be logged multiple times?

[ ] Is the original cause preserved internally?

[ ] Is the external message safe?

[ ] Could secrets appear in the error?

[ ] Could remote payloads leak?

[ ] Is message length bounded?

[ ] Is correlation preserved?

[ ] Is i18n appropriate?

[ ] Does OpenAPI document the error?

[ ] Do consumers depend on message text?

[ ] Are negative/error tests present?

[ ] Does async processing require retry/DLQ behavior?
```

---

# 217. Failure Fitness Functions

Stable failure-handling rules SHOULD be automated where practical.

Examples:

```text
[ ] Controllers do not return raw exception objects

[ ] Global handler returns Problem Details

[ ] 500 responses contain no stack trace

[ ] Error codes use approved format

[ ] Authorization headers are never returned

[ ] Remote error messages are length bounded

[ ] WebClient exceptions are translated at client boundary

[ ] Business exceptions do not produce generic 500

[ ] Catch blocks do not silently swallow exceptions

[ ] Error metrics use bounded labels

[ ] OpenAPI includes common failure schemas
```

---

# 218. Enterprise Failure Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] HTTP 200 returned for failure

[ ] Raw exception serialized to client

[ ] Internal stack trace exposed

[ ] Error semantics depend only on message text

[ ] Same logical failure maps inconsistently

[ ] Business rejection becomes generic 500

[ ] Remote HTTP body is blindly propagated

[ ] Bearer token can appear in failure message

[ ] WebClient/RestClient exceptions leak through all layers

[ ] Exceptions are logged at every layer

[ ] Catch block silently ignores failure

[ ] Retryability is undefined for critical integrations

[ ] Poison messages can retry forever

[ ] DLQ failure context contains uncontrolled sensitive data

[ ] OpenAPI does not document stable error contract
```

---

# 219. Anti-Patterns

The following are prohibited or strongly discouraged:

- one `GenericException` for all failures
- HTTP 200 containing an error flag
- raw exceptions as API responses
- stack traces in JSON responses
- returning database error messages directly
- parsing exception message strings for classification
- using localized message text as client contract
- exposing downstream topology unnecessarily
- blindly returning downstream error bodies
- converting every downstream failure into 500
- converting every remote 400 into local 400
- retrying business-validation exceptions
- hiding failures behind empty lists/default values
- catch-and-ignore
- logging and rethrowing at every layer
- losing exception causes during translation
- logging tokens in remote errors
- globally escaping legitimate API values to satisfy SAST
- using generic HTML escaping that turns `M&M` into `M&amp;M`
- unlimited remote error messages
- unlimited `last_error` persistence
- retrying poison messages forever
- DLQs with raw secrets or full uncontrolled stack traces
- undocumented failure codes
- consumer logic coupled to exact English error messages

---

# 220. Positive Consequences

The decision provides:

- stable error contracts
- predictable HTTP behavior
- safer client integrations
- better retry decisions
- clearer domain/infrastructure separation
- improved diagnostics
- less duplicate logging
- reduced secret exposure
- consistent asynchronous failure handling
- better OpenAPI contracts
- improved supportability

---

# 221. Negative Consequences

The decision introduces:

- exception taxonomy maintenance
- explicit translation code
- error-code governance
- additional tests
- i18n management
- remote-failure mapping
- contract documentation

These costs are accepted because inconsistent failure behavior creates direct coupling and operational ambiguity across distributed services.

---

# 222. Neutral Consequences

The decision also means:

- not every exception requires a unique public code
- not every failure should be logged as ERROR
- not every remote error should be propagated
- not every client error belongs in the SLO
- not every business validation requires a stack trace
- some failures are intentionally hidden behind 404 according to security policy
- asynchronous workflows use different failure-delivery mechanisms than HTTP

---

# 223. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Sensitive error leakage | Critical | Medium | Sanitization + tests |
| Client contract break | High | Medium | Stable error codes |
| Wrong retry behavior | High | Medium | Failure taxonomy |
| Duplicate logging | Medium | High | Single logging boundary |
| Business error as 500 | High | Medium | Explicit mapping |
| Remote topology leakage | Medium | Medium | Local translation |
| Error-code proliferation | Medium | Medium | Governance |
| Poison-message loop | High | Medium | Bounded retry + DLQ |
| i18n contract coupling | Medium | Medium | Stable machine codes |
| Lost diagnostic cause | High | Medium | Preserve exception cause |

---

# 224. Implementation Guidance

The following rules are mandatory:

1. Internal failures must have explicit semantic classification where materially useful.
2. Business and technical failures must remain distinguishable.
3. REST failures must use standardized Problem Details.
4. Machine-readable error codes must be stable.
5. Clients must not rely on localized human text for machine behavior.
6. HTTP status codes must represent actual protocol semantics.
7. Bean Validation failures must use structured violations.
8. Global REST exception translation should use `@RestControllerAdvice`.
9. Unexpected failures must return safe generic 500 responses.
10. Stack traces must never be exposed to API consumers.
11. Raw internal exception messages must not automatically become public details.
12. Legitimate JSON business values must not be globally HTML-escaped.
13. SAST fixes must target actual unsafe boundaries.
14. Remote error payloads must be parsed, length bounded and sanitized.
15. Secrets must be masked before logging or persistence.
16. WebClient/RestClient exceptions should be translated at integration boundaries.
17. Remote HTTP status must be interpreted in local business context.
18. Timeout, unavailable, rate-limit and Circuit Breaker failures must remain distinguishable.
19. Retry eligibility must derive from explicit failure classification and ADR-055 policy.
20. Circuit Breaker OPEN must be distinguishable from an attempted remote call failure.
21. Fallback must not silently hide semantically relevant failures.
22. SQS failures must distinguish retryable and permanent conditions.
23. Poison messages must not retry forever.
24. DLQ failure metadata must remain safe and bounded.
25. Unexpected exceptions should normally be logged once at the responsible boundary.
26. Catch blocks must either meaningfully handle or propagate failure.
27. Exception translation should preserve the original cause internally.
28. Error logs should preserve correlation context.
29. Error metrics must avoid high-cardinality labels.
30. Public error contracts must be represented in OpenAPI.
31. Error-code evolution must follow compatibility governance.
32. Persistent failure summaries must have maximum lengths.
33. Tests must cover failure mapping and sensitive-data protection.
34. Critical consumers must have error-contract tests.
35. Java tests must follow established AssertJ/Sonar conventions.

---

# 225. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring MVC
- Spring `ProblemDetail`
- Jakarta Bean Validation
- `@RestControllerAdvice`
- Spring WebClient
- Spring RestClient
- Resilience4j
- AWS SDK for Java 2.x SQS integration
- AWS SQS integrations
- OpenAPI
- JUnit 5
- AssertJ
- Mockito
- MockMvc
- WireMock
- MockWebServer
- Testcontainers
- SonarQube
- SAST
- contract tests
- architecture fitness functions

---

# 226. Success Criteria

The decision is successful when:

- public errors follow one predictable structure
- business failures stop becoming generic 500 responses
- clients use stable error codes instead of parsing messages
- remote integration failures are consistently classified
- retries occur only for appropriate failure classes
- exception stack traces are preserved internally but never exposed externally
- duplicate error logging decreases
- sensitive values no longer appear in failure responses/logs
- legitimate business text remains uncorrupted
- SQS poison failures move predictably to DLQ
- OpenAPI accurately documents expected failures
- production incidents are easier to correlate and diagnose

---

# 227. Alternatives Rejected

## 227.1 One Generic Error Response

Rejected because clients cannot reliably distinguish failure semantics.

---

## 227.2 Raw Exception Propagation

Rejected because it leaks implementation detail and sensitive information.

---

## 227.3 HTTP Status Only

Rejected because clients often require stable machine-readable business failure codes.

---

## 227.4 Human Message as Contract

Rejected because wording and localization change over time.

---

## 227.5 Log Every Exception Everywhere

Rejected because duplicate logs obscure the actual failure boundary.

---

## 227.6 Catch and Return Empty Value

Rejected because it converts failures into incorrect successful semantics.

---

## 227.7 Global Response Escaping

Rejected because transport-level JSON data must preserve legitimate business values.

---

# 228. Related Decisions

This ADR extends and implements:

- ADR-014: Distributed Observability
- ADR-016: Application Resilience
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-055: Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard
- ADR-056: Enterprise REST API Design, Versioning, Error Handling and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard

---

# 229. References

- RFC 9457 — Problem Details for HTTP APIs
- RFC 9110 — HTTP Semantics
- Spring Framework Documentation
- Spring Boot Documentation
- Spring ProblemDetail
- Jakarta Bean Validation
- Spring WebClient Documentation
- Spring RestClient Documentation
- Resilience4j Documentation
- Amazon SQS Documentation
- AWS SQS Documentation
- OpenAPI Specification
- OWASP Error Handling Cheat Sheet
- OWASP Logging Cheat Sheet
- Google Site Reliability Engineering

---

# 230. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise exception taxonomy and failure-contract baseline |

---

# 231. Decision Summary

The internal failure flow becomes:

```text
FAILURE
   |
   v
CLASSIFY
   |
   +--> VALIDATION
   +--> BUSINESS
   +--> SECURITY
   +--> RESOURCE
   +--> CONFLICT
   +--> REMOTE
   +--> INFRASTRUCTURE
   +--> UNEXPECTED
   |
   v
TRANSLATE
   |
   v
STABLE CONTRACT
```

REST failure handling becomes:

```text
EXCEPTION
    |
    v
@RestControllerAdvice
    |
    +--> HTTP STATUS
    +--> ERROR CODE
    +--> SAFE MESSAGE
    +--> CORRELATION
    +--> VIOLATIONS
    |
    v
PROBLEM DETAILS
```

The public response contains:

```text
WHAT CATEGORY FAILED?

WHAT STABLE CODE IDENTIFIES IT?

WHAT SAFE INFORMATION
CAN THE CLIENT USE?
```

not:

```text
WHICH JAVA EXCEPTION
AND STACK TRACE OCCURRED?
```

Remote integration handling becomes:

```text
HTTP CLIENT
    |
    v
REMOTE RESULT
    |
    +--> 2xx
    |
    +--> 4xx
    |
    +--> 429
    |
    +--> 5xx
    |
    +--> TIMEOUT
    |
    +--> CONNECTION FAILURE
    |
    v
LOCAL FAILURE TAXONOMY
```

not:

```text
WebClientResponseException
        |
        v
PROPAGATED THROUGH
EVERY APPLICATION LAYER
```

Retry decisions become:

```text
FAILURE
   |
   v
CLASSIFY
   |
   +--> BUSINESS ------------> DO NOT RETRY
   |
   +--> VALIDATION ----------> DO NOT RETRY
   |
   +--> AUTHORIZATION -------> DO NOT RETRY
   |
   +--> TRANSIENT NETWORK ---> RETRY MAY APPLY
   |
   +--> TIMEOUT -------------> RETRY MAY APPLY
   |
   +--> RATE LIMIT ----------> POLICY DEPENDENT
```

with idempotency and retry budgets still enforced.

Logging becomes:

```text
LOWER LAYER
   |
   v
TRANSLATE / RETHROW
   |
   v
RESPONSIBLE BOUNDARY
   |
   v
LOG ONCE
```

rather than:

```text
ADAPTER LOG
   +
SERVICE LOG
   +
CONTROLLER LOG
   +
ADVICE LOG
```

Security becomes:

```text
RAW REMOTE ERROR
      |
      v
MASK SECRETS
      |
      v
NORMALIZE CONTROL CHARACTERS
      |
      v
LIMIT SIZE
      |
      v
SAFE DIAGNOSTIC / CONTRACT
```

Valid business data remains intact:

```text
M&M
```

stays:

```text
M&M
```

and does not become:

```text
M&amp;M
```

merely because generic sanitization exists.

Asynchronous failure becomes:

```text
MESSAGE
  |
  v
PROCESS
  |
  +--> SUCCESS
  |
  +--> RETRYABLE
  |       |
  |       v
  |   BOUNDED RETRY
  |
  +--> PERMANENT
          |
          v
         DLQ
          |
          v
   SAFE FAILURE CONTEXT
```

The complete failure-handling equation is:

```text
SEMANTIC EXCEPTION TAXONOMY
        +
CORRECT HTTP STATUS
        +
RFC 9457 PROBLEM DETAILS
        +
STABLE ERROR CODES
        +
STRUCTURED VALIDATION ERRORS
        +
LOCAL REMOTE-ERROR TRANSLATION
        +
RETRYABILITY CLASSIFICATION
        +
BOUNDED ASYNC FAILURE HANDLING
        +
SINGLE LOGGING BOUNDARY
        +
CORRELATION
        +
SENSITIVE-DATA PROTECTION
        +
I18N WITHOUT CONTRACT COUPLING
        +
OPENAPI DOCUMENTATION
        +
FAILURE CONTRACT TESTING
        =
PREDICTABLE ENTERPRISE FAILURE SEMANTICS
```

The governing principle is:

```text
Classify failure before
deciding how to handle it.

Do not confuse business rejection
with infrastructure failure.

Do not return HTTP 200
for unsuccessful operations.

Expose stable error codes,
not Java exception classes.

Do not make clients parse
human-readable messages.

Translate remote failures
at the integration boundary.

Preserve the original cause
for internal diagnostics.

Log unexpected failures once,
at the layer responsible
for recording them.

Either handle an exception
meaningfully or propagate it.

Do not silently swallow it.

Do not expose stack traces,
SQL, tokens or credentials.

Do not fix security findings
by corrupting valid business data.

Keep JSON data semantically correct.

Bound every externally sourced
error message.

Distinguish retryable failures
from permanent failures.

Never retry poison messages forever.

Document the failure contract
with the same care as
the success contract.

Because in distributed systems,
failure behavior is part
of the API.
```
