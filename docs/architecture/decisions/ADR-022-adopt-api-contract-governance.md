# ADR-022: Adopt API Contract Governance

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-022 |
| Title | Adopt API Contract Governance |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | REST APIs, OpenAPI, Contract Evolution and Integration Governance |
| Related Work Items | OpenAPI, REST, Backward Compatibility, Contract Testing, API Versioning |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform is composed of independently deployable services that communicate through synchronous REST APIs and asynchronous integration events.

The platform includes:

- Java 21
- Spring Boot
- REST APIs
- OpenAPI
- Apache Kafka
- PostgreSQL
- Transactional Outbox
- Saga workflows
- Kubernetes
- Resilience4j
- OpenTelemetry
- structured logging
- independent CI/CD pipelines

REST APIs are consumed by:

- frontend applications
- mobile applications
- internal microservices
- batch processes
- administrative applications
- integration platforms
- external systems

In a distributed architecture, an API contract is not merely an implementation detail.

It is a compatibility boundary between independently evolving systems.

A seemingly small change such as:

```text
Rename field

Change field type

Make optional field mandatory

Change enum values

Change HTTP status

Change pagination semantics
```

may break consumers without producing any compilation error in the provider.

The platform therefore requires explicit governance for REST API contracts.

---

# 2. Problem Statement

The platform requires a standardized API governance model that:

- treats APIs as explicit contracts
- supports independent deployments
- uses OpenAPI as the contract definition
- establishes backward-compatibility rules
- defines breaking changes
- defines API versioning
- defines deprecation policy
- standardizes HTTP semantics
- standardizes error responses
- supports idempotency
- standardizes pagination
- standardizes filtering and sorting
- supports distributed tracing
- avoids exposing implementation details
- prevents uncontrolled contract drift
- supports consumer/provider testing
- integrates with CI/CD
- supports zero-downtime deployments
- provides predictable evolution over time

---

# 3. Decision Drivers

Primary decision drivers are:

1. independent service deployment
2. consumer compatibility
3. API predictability
4. contract discoverability
5. zero-downtime deployment
6. reduced integration defects
7. automated compatibility validation
8. consistent HTTP semantics
9. standardized error handling
10. observability
11. security
12. maintainability
13. API lifecycle management
14. consumer migration
15. service ownership
16. testability
17. long-term platform evolution

---

# 4. Decision

The Enterprise Order Platform adopts formal API Contract Governance for all production REST APIs.

The governance model is based on:

```text
OpenAPI Contract

+

Backward-Compatible Evolution

+

Automated Contract Validation

+

Explicit Breaking-Change Management

+

Deprecation Lifecycle

+

Standard HTTP Semantics
```

Every production API must have an explicit, version-controlled contract.

---

# 5. Fundamental Principle

The platform adopts the principle:

```text
An API belongs to its provider,
but its contract is shared with its consumers.
```

A provider may change its internal implementation freely.

It may not change externally observable behavior incompatibly without following the contract-evolution process.

---

# 6. API as a Contract

The API contract includes more than:

```text
URL + HTTP Method
```

It includes:

- paths
- methods
- request headers
- request body
- response body
- field names
- field types
- required fields
- enum values
- validation semantics
- HTTP status codes
- error structures
- pagination
- sorting
- filtering
- authentication expectations
- idempotency behavior
- correlation behavior
- content types
- documented business semantics

---

# 7. OpenAPI

OpenAPI is the canonical machine-readable description format for REST API contracts.

Every production REST API must expose or generate an OpenAPI specification.

---

# 8. OpenAPI Version Control

OpenAPI definitions must be version controlled with the service source code.

Contract history must be auditable.

---

# 9. Contract and Implementation

The implementation and OpenAPI contract must remain synchronized.

The following condition is prohibited:

```text
Runtime API behavior

≠

Published OpenAPI contract
```

---

# 10. Contract-First vs Code-First

The platform supports both:

```text
Contract-first
```

and:

```text
Code-first with generated OpenAPI
```

provided that automated validation prevents contract drift.

For externally important or heavily integrated APIs, contract-first design is preferred.

---

# 11. Contract-First

In contract-first development:

```text
API design

↓

OpenAPI review

↓

Consumer feedback

↓

Implementation

↓

Contract validation
```

This is especially useful for cross-team integration.

---

# 12. Code-First

Code-first may be used for simpler internal APIs when:

- annotations are complete
- generated OpenAPI is deterministic
- CI validates compatibility
- implementation tests validate documented behavior

---

# 13. API Documentation

OpenAPI descriptions should document:

- operation purpose
- request semantics
- field meaning
- validation constraints
- business behavior
- expected responses
- error responses
- pagination
- deprecation

Documentation should explain semantics, not merely repeat field names.

---

# 14. Operation Identifier

Every operation should define a stable:

```text
operationId
```

Example:

```yaml
operationId: getOrderById
```

Operation IDs should remain stable when semantics remain stable.

---

# 15. Resource-Oriented Design

REST APIs should normally model business resources.

Preferred:

```text
GET /orders/{orderId}
```

rather than:

```text
POST /executeGetOrder
```

---

# 16. HTTP Methods

The platform uses HTTP methods according to their semantics.

```text
GET
→ retrieve

POST
→ create or execute non-idempotent command

PUT
→ replace or idempotently establish resource state

PATCH
→ partially modify resource

DELETE
→ remove or logically delete resource
```

Business semantics may justify specialized command endpoints.

---

# 17. GET

GET operations must not create externally visible business side effects.

Operational side effects such as:

- metrics
- traces
- access logs

are acceptable.

---

# 18. GET Idempotency

GET is:

```text
safe

and

idempotent
```

Repeated requests must not mutate business state.

---

# 19. POST

POST is normally used for:

- resource creation
- commands
- operations whose natural semantics are not idempotent

Where retry safety is required, POST should support explicit idempotency.

---

# 20. PUT

PUT should represent an idempotent operation.

Repeating the same PUT request should produce the same intended resource state.

---

# 21. PATCH

PATCH is appropriate for partial updates.

The API must define:

- patch semantics
- omitted-field behavior
- null behavior
- concurrency behavior

---

# 22. DELETE

DELETE should normally be idempotent from the client's perspective.

Repeated deletion may return:

```text
204
```

or:

```text
404
```

depending on documented API semantics.

The behavior must remain consistent.

---

# 23. URI Naming

Paths should:

- use nouns
- use lowercase
- use plural resources where appropriate
- avoid implementation terminology
- remain stable

Preferred:

```text
/orders
/orders/{orderId}
/customers/{customerId}/addresses
```

---

# 24. URI Anti-Patterns

Avoid:

```text
/getOrders

/createOrder

/updateCustomer

/deleteAddress
```

HTTP methods already express the primary operation.

---

# 25. Business Commands

Some domain commands do not map naturally to CRUD.

Acceptable examples include:

```text
POST /orders/{orderId}/approval

POST /orders/{orderId}/cancellation

POST /carts/{cartId}/checkout
```

The resource-oriented meaning must remain explicit.

---

# 26. API Versioning

API versioning is required when incompatible contract evolution cannot be avoided.

Preferred external path strategy:

```text
/api/v1/orders
```

A new incompatible API may become:

```text
/api/v2/orders
```

---

# 27. Versioning Is Not Required for Every Change

Backward-compatible additive changes do not require a new major API version.

Example:

```text
Add optional response field
```

does not normally require `/v2`.

---

# 28. Major Version

A new major API version is appropriate when:

- request semantics change incompatibly
- response semantics change incompatibly
- required fields change incompatibly
- resource model changes fundamentally
- HTTP behavior changes incompatibly
- old consumers cannot continue functioning

---

# 29. Minor Version in URI

The platform does not use URI versions such as:

```text
/v1.1

/v1.2

/v1.3
```

Minor compatible evolution occurs within the same major API version.

---

# 30. Query-Parameter Versioning

Versioning through:

```text
?version=2
```

is not the standard strategy.

---

# 31. Header Versioning

Header-based API versioning is not the default platform strategy because it reduces:

- discoverability
- routing transparency
- operational clarity

It may be approved for specialized APIs.

---

# 32. Backward Compatibility

Backward compatibility means a supported existing consumer can continue operating after the provider is upgraded.

This is essential for independent deployment.

---

# 33. Additive Evolution

The preferred API evolution strategy is additive.

Examples:

- add optional response field
- add optional request field
- add endpoint
- add query parameter with safe default
- add new non-breaking capability

---

# 34. Response Field Addition

Adding an optional response field is normally backward compatible if consumers follow the rule:

```text
Ignore unknown fields.
```

---

# 35. Consumer Robustness

Consumers must not fail merely because a provider adds an unknown response field.

Java deserialization should be configured consistently with this principle where appropriate.

---

# 36. Request Field Addition

Adding an optional request field is backward compatible when omission preserves existing behavior.

---

# 37. Mandatory Request Field

Changing:

```text
optional
```

to:

```text
required
```

is a breaking change.

Existing consumers may not send the field.

---

# 38. Response Field Removal

Removing a response field is a breaking change if supported consumers may depend on it.

---

# 39. Field Rename

Changing:

```json
{
  "customerId": "..."
}
```

to:

```json
{
  "clientId": "..."
}
```

is a breaking change.

A safe transition requires additive compatibility.

---

# 40. Field Type Change

Changing:

```text
integer
```

to:

```text
string
```

is normally breaking.

Even if values appear convertible, generated clients and validators may fail.

---

# 41. Numeric Type Changes

Changing:

```text
integer
```

to:

```text
number
```

must not automatically be considered safe.

Consumer language types and generated models must be considered.

---

# 42. Nullability

Changing a response field from:

```text
nullable
```

to:

```text
non-null
```

may be compatible for consumers but changes the provider guarantee.

Changing from:

```text
non-null
```

to:

```text
nullable
```

is potentially breaking because consumers may assume a value always exists.

---

# 43. Enum Evolution

Enums require special care.

Example:

```text
CREATED
APPROVED
CANCELLED
```

Adding:

```text
REVIEW
```

may break consumers that deserialize directly into a closed enum.

Therefore enum additions must be treated as potentially breaking at the consumer implementation level.

---

# 44. Consumer Enum Handling

Consumers of externally evolving enums should define controlled unknown-value behavior where business semantics permit.

They must not silently map an unknown business-critical state to an incorrect known state.

---

# 45. Enum Removal

Removing an enum value is breaking if it may still be produced or accepted.

---

# 46. Enum Semantic Change

Changing the meaning of an existing enum value without changing the contract is prohibited.

---

# 47. Validation Constraint Changes

Tightening validation can be breaking.

Example:

```text
maxLength: 100
```

changed to:

```text
maxLength: 30
```

Existing valid consumers may now fail.

---

# 48. Pattern Changes

Adding or tightening a regex constraint may be breaking.

Example:

```text
^[A-Z]+$
```

may reject values previously accepted.

---

# 49. HTTP Status Compatibility

Changing expected HTTP status codes may break consumers.

Example:

```text
200
```

to:

```text
204
```

is not automatically harmless.

Clients may depend on a response body.

---

# 50. Error Status Changes

Changing:

```text
404
```

to:

```text
400
```

changes API semantics and must be reviewed.

---

# 51. Content-Type

Changing supported content types may be breaking.

JSON remains the standard REST representation unless explicitly approved otherwise.

Preferred:

```text
application/json
```

---

# 52. Date and Time

Date/time fields must use standardized ISO-8601 representations.

Examples:

```text
2026-07-24
```

and:

```text
2026-07-24T15:30:45-03:00
```

The contract must distinguish:

- date
- local date/time
- instant
- offset date/time

---

# 53. Time Zone

Business timestamps should preserve appropriate timezone or offset semantics.

Ambiguous server-local timestamps should be avoided for distributed integration.

---

# 54. UUID

UUID identifiers should be represented consistently as strings using OpenAPI:

```yaml
type: string
format: uuid
```

---

# 55. Monetary Values

Financial values must use decimal semantics.

Binary floating-point types should not represent authoritative monetary amounts.

OpenAPI must document:

- scale
- precision expectations
- currency semantics where applicable

---

# 56. Boolean Fields

Boolean fields should use actual boolean representation.

Avoid:

```text
"S"

"N"

"Y"

"N"

"1"

"0"
```

unless required by a legacy integration boundary.

---

# 57. Collections

Collection responses should return a predictable structure.

An empty collection should normally be:

```json
[]
```

rather than:

```json
null
```

---

# 58. Pagination

Endpoints returning potentially large result sets must support pagination.

Unbounded collection endpoints are prohibited for production data where result size can grow materially.

---

# 59. Standard Pagination Parameters

The platform standard for page-based pagination is:

```text
page

size

sort
```

Example:

```text
GET /orders?page=0&size=20&sort=createdAt,desc
```

---

# 60. Page Numbering

Page-based APIs use zero-based indexing unless an existing established contract explicitly defines otherwise.

The OpenAPI contract must document this.

---

# 61. Maximum Page Size

Every paginated API must define a maximum page size.

Example:

```text
default = 20

maximum = 100
```

Exact values depend on resource characteristics.

---

# 62. Excessive Page Size

If the client requests a size above the maximum, the API must consistently either:

- reject it with validation error

or

- cap it to the documented maximum

Silent inconsistent behavior is prohibited.

---

# 63. Pagination Response

A standard page response should expose sufficient metadata.

Example:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 125,
  "totalPages": 7
}
```

The exact shared representation should remain consistent across the platform.

---

# 64. Spring `Page` Exposure

Directly exposing framework-specific serialization of:

```java
Page<T>
```

as the public contract is discouraged.

Framework upgrades may change serialization details.

A stable platform DTO is preferred.

---

# 65. Cursor Pagination

Cursor-based pagination should be considered for:

- very large datasets
- frequently changing datasets
- high-throughput feeds
- APIs where deep offset pagination is expensive

---

# 66. Cursor Opacity

A pagination cursor must be treated as opaque by consumers.

Clients must not depend on its internal representation.

---

# 67. Sorting

Sorting must use an explicit allowlist.

Clients must not be allowed to reference arbitrary entity properties directly.

---

# 68. Sort Example

Example:

```text
?sort=createdAt,desc
```

The API layer maps the public sort field to an internal query path.

---

# 69. Sort Security

Allowlisted sorting prevents:

- accidental internal-model exposure
- invalid query paths
- unexpected joins
- implementation coupling

---

# 70. Filtering

Filters must use documented API fields.

Persistence implementation details must not leak into the public filtering contract.

---

# 71. Dynamic Query Languages

Exposing unrestricted SQL-like or persistence-specific query languages is prohibited unless explicitly approved.

---

# 72. Search

Complex search endpoints may use POST when query semantics cannot be represented safely or practically through query parameters.

Example:

```text
POST /orders/search
```

with a structured search request.

---

# 73. Search POST Semantics

A search POST must remain read-only even though HTTP POST is used.

Its documentation must make this explicit.

---

# 74. Idempotency

Operations vulnerable to duplicate execution must support idempotency where business impact warrants it.

Examples:

- order creation
- payment initiation
- checkout
- external command submission

---

# 75. Idempotency Key

The preferred mechanism is a request header such as:

```text
Idempotency-Key
```

The exact standard must be consistently documented.

---

# 76. Idempotency Semantics

For the same idempotency key and logically equivalent request:

```text
Repeated request

↓

Same business operation

↓

No duplicate side effect
```

---

# 77. Idempotency-Key Scope

The API must define the key scope.

Possible dimensions include:

- operation
- authenticated client
- customer
- resource type

Keys must not accidentally collide across unrelated operations.

---

# 78. Idempotency Retention

The provider must define how long idempotency records remain valid.

Retention depends on:

- retry window
- business operation
- storage cost
- external integration behavior

---

# 79. Idempotency Conflict

If the same key is reused with a materially different request, the provider should reject the request.

It must not silently execute a different operation under the same key.

---

# 80. Idempotency and Database Transaction

Where practical, idempotency state and business state should participate in a consistent transactional strategy.

Race conditions between concurrent identical requests must be handled.

---

# 81. Optimistic Concurrency

APIs modifying concurrently editable resources should consider explicit concurrency control.

Possible mechanisms include:

- version field
- ETag
- `If-Match`
- domain-specific expected version

---

# 82. Lost Update

The platform must avoid silently overwriting concurrent changes where business correctness requires conflict detection.

---

# 83. Conflict Response

A detected concurrency conflict should normally return a controlled conflict response, commonly:

```text
409 Conflict
```

The exact semantics must be documented.

---

# 84. Error Contract

All platform APIs must use a standardized error structure.

Error responses must be machine-readable and operationally diagnosable.

---

# 85. Standard Error Response

Recommended structure:

```json
{
  "timestamp": "2026-07-24T15:30:45-03:00",
  "status": 400,
  "code": "ORDER_VALIDATION_ERROR",
  "message": "The order request is invalid.",
  "path": "/api/v1/orders",
  "correlationId": "..."
}
```

Additional controlled validation details may be included.

---

# 86. Error Code

The `code` field must be stable and machine-readable.

Preferred:

```text
ORDER_NOT_FOUND
```

rather than requiring consumers to parse:

```text
"Order 123 could not be located in database."
```

---

# 87. Error Message

The human-readable message must not be the primary machine integration contract.

Messages may:

- evolve
- be localized
- provide additional context

Consumers should use stable error codes.

---

# 88. Internal Error Details

Error responses must not expose:

- stack traces
- SQL
- database structure
- internal hostnames
- credentials
- tokens
- secrets
- implementation class names
- sensitive exception details

---

# 89. Validation Errors

Validation failures should provide controlled field-level information.

Example:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "violations": [
    {
      "field": "segment",
      "code": "INVALID_VALUE",
      "message": "Invalid segment."
    }
  ],
  "correlationId": "..."
}
```

---

# 90. Validation Error Stability

Consumers should depend on:

```text
field

code
```

rather than exact localized human-readable messages.

---

# 91. HTTP 400

Use `400 Bad Request` for malformed or invalid request semantics where no more specific platform rule applies.

---

# 92. HTTP 401

Use `401 Unauthorized` when valid authentication is required but missing or invalid.

Despite the HTTP reason phrase, this represents an authentication failure.

---

# 93. HTTP 403

Use `403 Forbidden` when the caller is authenticated but not permitted to perform the operation.

---

# 94. HTTP 404

Use `404 Not Found` when the requested resource does not exist or must intentionally be concealed according to security policy.

---

# 95. HTTP 409

Use `409 Conflict` for state conflicts such as:

- optimistic-lock conflict
- incompatible current state
- idempotency-key conflict
- uniqueness conflict where appropriate

---

# 96. HTTP 422

`422 Unprocessable Content` may be used for domain validation when the request is structurally valid but cannot be processed semantically.

The platform must use `400` versus `422` consistently within an API family.

---

# 97. HTTP 429

Use `429 Too Many Requests` for rate limiting.

Where appropriate, include:

```text
Retry-After
```

---

# 98. HTTP 500

Unexpected provider defects should normally return:

```text
500 Internal Server Error
```

with a generic safe response.

---

# 99. HTTP 502

A gateway or integration component may use:

```text
502 Bad Gateway
```

when an upstream service returns an invalid or failed response.

---

# 100. HTTP 503

Use:

```text
503 Service Unavailable
```

for temporary service inability where retry may later succeed.

---

# 101. HTTP 504

Use:

```text
504 Gateway Timeout
```

when acting as a gateway and an upstream dependency fails to respond within the allowed time.

---

# 102. Remote Error Translation

Services must not blindly expose downstream error payloads.

The provider must translate downstream failures into its own controlled API contract.

---

# 103. Error Sanitization

Sensitive values must never be returned merely because they appeared in a downstream exception.

The API boundary must maintain safe error handling.

This requirement is independent from normal business-text preservation.

---

# 104. Business Text Preservation

Legitimate business values must not be mutated merely as a generic response workaround.

For example, a legitimate value such as:

```text
M&M
```

must remain:

```text
M&M
```

unless the contract explicitly requires another representation.

Incorrect escaping or encoding should be fixed at the responsible boundary rather than globally altering valid business data.

---

# 105. Correlation ID

Every request should participate in distributed correlation.

The platform should propagate a correlation identifier across synchronous calls.

---

# 106. Correlation Header

A standardized header should be used, for example:

```text
X-Correlation-Id
```

The exact platform header name must remain consistent.

---

# 107. Missing Correlation ID

If an incoming request lacks a correlation ID, the edge or service may generate one.

The resulting identifier should propagate downstream.

---

# 108. Correlation ID Response

The correlation ID should be returned to the caller where platform standards require it.

This improves supportability.

---

# 109. Correlation ID Validation

Externally supplied correlation IDs must have bounded:

- length
- character set

Untrusted arbitrary values must not become uncontrolled log content or metric labels.

---

# 110. Trace Context

OpenTelemetry/W3C trace context should propagate independently from business correlation IDs.

The platform should not assume:

```text
correlationId = traceId
```

They serve related but distinct purposes.

---

# 111. Trace Headers

Standard W3C propagation uses headers such as:

```text
traceparent
```

Application code should use platform instrumentation rather than manually constructing trace identifiers.

---

# 112. Correlation ID in Metrics

Correlation IDs must not be metric labels.

They have unbounded cardinality.

---

# 113. Correlation ID in Logs

Correlation IDs are appropriate structured log fields.

---

# 114. Authentication

Authentication requirements must be documented in OpenAPI.

Security schemes should represent the actual production model.

---

# 115. Authorization

Authorization is domain and operation specific.

The API contract should document relevant authorization expectations without exposing sensitive policy implementation.

---

# 116. Bearer Tokens

Bearer tokens must not be:

- logged
- returned in errors
- included in URLs
- stored in telemetry attributes

---

# 117. Sensitive Query Parameters

Secrets must not be passed as query parameters.

URLs are commonly captured in:

- access logs
- proxies
- browser history
- monitoring systems

---

# 118. PII

API contracts must identify sensitive or personally identifiable fields where governance requires it.

Logging and observability must apply appropriate controls.

---

# 119. Request Limits

APIs must define bounded request sizes.

Examples include:

- maximum body size
- collection item limit
- string length
- bulk-operation size

---

# 120. Bulk APIs

Bulk endpoints must define:

- maximum item count
- partial-failure semantics
- transactional semantics
- idempotency behavior
- response mapping

---

# 121. Bulk Atomicity

A bulk API must explicitly define whether it is:

```text
all-or-nothing
```

or:

```text
partial success
```

Consumers must not infer this behavior.

---

# 122. Bulk Partial Response

For partial success, each item should have a stable identifier and result.

Example:

```json
{
  "results": [
    {
      "id": "...",
      "status": "SUCCESS"
    },
    {
      "id": "...",
      "status": "FAILED",
      "code": "ORDER_NOT_FOUND"
    }
  ]
}
```

---

# 123. Async APIs

A REST operation that starts asynchronous work should make this explicit.

A common response is:

```text
202 Accepted
```

when processing continues after the HTTP response.

---

# 124. Async Resource

Where appropriate, asynchronous processing should expose a resource that can be queried.

Example:

```text
POST /reports

↓

202 Accepted

Location: /reports/{reportId}
```

---

# 125. HTTP Timeout

A client-side timeout does not imply that the server-side business operation did not execute.

This is a key reason for idempotency in command APIs.

---

# 126. Retry Semantics

The API contract should make retry behavior understandable.

Clients should know whether an operation is:

- safe to retry
- idempotent
- retryable only with idempotency key
- not automatically retryable

---

# 127. Retry-After

Temporary throttling or unavailability may use:

```text
Retry-After
```

where appropriate.

---

# 128. Resilience4j Retries

Internal retries must respect HTTP semantics.

Automatically retrying a non-idempotent POST without an idempotency mechanism may duplicate business operations.

---

# 129. Timeout Budget

Client timeouts should align with the end-to-end latency SLO.

A client must not configure a timeout that systematically expires before the provider's documented operation expectation without explicit reason.

---

# 130. API Gateway

If an API gateway is used, it may provide:

- authentication
- routing
- rate limiting
- request-size enforcement
- correlation
- observability

Business validation remains the responsibility of the domain service.

---

# 131. Gateway Contract

Gateway behavior must not silently change API semantics.

Examples:

- changing status codes
- altering JSON fields
- swallowing headers

must be governed.

---

# 132. Rate Limiting

Rate limits must be documented when consumers need to design around them.

The API should communicate throttling through standard HTTP semantics.

---

# 133. OpenAPI Examples

Important operations should provide representative examples.

Examples must:

- be valid
- avoid real customer data
- avoid secrets
- remain synchronized with schema

---

# 134. Schema Reuse

Reusable OpenAPI schemas may reduce duplication.

However, excessive shared schemas across unrelated bounded contexts can create unwanted coupling.

---

# 135. Domain Ownership

Each bounded context owns its API model.

A universal enterprise DTO model is prohibited.

---

# 136. Persistence Leakage

API DTOs must not expose JPA entities directly.

Persistence models and public contracts have different lifecycle requirements.

---

# 137. JPA Entity Exposure

Returning:

```java
@Entity
```

objects directly from controllers is prohibited.

Reasons include:

- lazy-loading leakage
- persistence coupling
- accidental field exposure
- uncontrolled contract evolution
- serialization instability

---

# 138. Dedicated DTOs

REST boundaries should use dedicated:

- request DTOs
- response DTOs

where appropriate.

---

# 139. Java Records

Java records are preferred for immutable API DTOs where their semantics fit.

Example:

```java
public record OrderResponse(
        UUID id,
        String number,
        OrderStatus status
) {
}
```

---

# 140. DTO Validation

Request DTO validation should use Jakarta Bean Validation where appropriate.

Examples:

```java
@NotNull
@NotBlank
@Size
@Pattern
@Positive
```

Custom business validation remains in the appropriate domain/application layer.

---

# 141. Validation Duplication

OpenAPI constraints and runtime validation must remain aligned.

Example:

```java
@Size(max = 30)
```

must not conflict with:

```yaml
maxLength: 50
```

---

# 142. Domain Validation

Not every domain rule belongs in annotations.

Example:

```text
Order can only be cancelled while pending approval.
```

This belongs to domain/application behavior rather than a simple DTO annotation.

---

# 143. API Mapping

Mapping between:

```text
API DTO

and

Domain model
```

must be explicit.

Controllers should not contain large amounts of mapping and business logic.

---

# 144. Controller Responsibility

Controllers should primarily handle:

- HTTP contract
- input validation
- authentication context
- application-service invocation
- response mapping

Business rules belong elsewhere.

---

# 145. OpenAPI Tags

Operations should use meaningful tags aligned with API capabilities.

Tags should not simply mirror internal package structure.

---

# 146. Deprecation

An API element may be marked deprecated when consumers should migrate away from it.

OpenAPI should use:

```yaml
deprecated: true
```

where applicable.

---

# 147. Deprecation Is Not Removal

Deprecation means:

```text
Still supported

but scheduled for future removal.
```

Consumers must be given a migration window.

---

# 148. Deprecation Policy

A deprecation must define:

- deprecated element
- replacement
- deprecation date
- planned removal date or review date
- known consumers
- owner
- migration guidance

---

# 149. Deprecation Window

The minimum deprecation window depends on:

- consumer ownership
- release cadence
- external commitments
- business criticality

No universal number of days should be assumed without organizational policy.

---

# 150. Internal Consumer Migration

For internal APIs, the provider team must identify known consumers before removing deprecated functionality.

---

# 151. Consumer Inventory

Critical APIs should maintain a consumer inventory where practical.

This may include:

- consuming service
- owning team
- API version
- contact
- migration status

---

# 152. Usage Telemetry

API usage telemetry may support deprecation decisions.

Example:

```text
Deprecated endpoint received zero production requests
for the approved observation period.
```

This is stronger evidence than assumption.

---

# 153. Removal

A deprecated API may be removed only when:

- deprecation policy is satisfied
- supported consumers migrated
- usage evidence is acceptable
- contractual obligations permit removal
- rollback implications are understood

---

# 154. Breaking Change

A breaking change is any change that can cause a previously valid supported consumer to fail or behave incorrectly.

---

# 155. Breaking Change Examples

Breaking changes include:

- removing endpoint
- removing field
- renaming field
- changing field type
- making optional input required
- narrowing accepted values
- removing enum value
- changing enum semantics
- changing response structure
- changing status semantics
- changing pagination semantics
- changing authentication requirement
- removing supported content type
- changing idempotency behavior
- changing resource ownership semantics

---

# 156. Potentially Breaking Changes

The following require compatibility analysis:

- adding enum value
- tightening validation
- changing default value
- changing sort behavior
- changing nullability
- changing precision
- changing timeout behavior
- changing error code
- changing ordering guarantees

---

# 157. Breaking Change Process

A required breaking change must follow:

```text
Identify change

↓

Analyze consumers

↓

Design compatible transition if possible

↓

Create new major contract if necessary

↓

Publish migration guidance

↓

Deprecate old contract

↓

Migrate consumers

↓

Observe usage

↓

Remove old contract
```

---

# 158. Parallel API Versions

When a new major version is required, versions may coexist temporarily.

Example:

```text
/api/v1/orders

/api/v2/orders
```

---

# 159. Version Duplication

Maintaining multiple major versions has cost.

Therefore new versions must not be created for trivial compatible changes.

---

# 160. Version Ownership

The provider owns maintenance of supported versions until their documented retirement.

---

# 161. Compatibility Automation

CI/CD must automatically evaluate API compatibility where tooling permits.

A pull request that introduces an unapproved breaking change should fail.

---

# 162. OpenAPI Diff

The pipeline should compare:

```text
baseline OpenAPI
```

against:

```text
candidate OpenAPI
```

and identify:

- added operations
- removed operations
- schema changes
- required-field changes
- response changes
- parameter changes

---

# 163. Compatibility Baseline

The baseline should represent the currently supported production contract, not an arbitrary developer branch.

---

# 164. False Positives

Automated compatibility tools may produce false positives or miss semantic breaks.

Architecture review remains required for ambiguous contract changes.

---

# 165. Semantic Compatibility

Some breaking changes are invisible to schema diff.

Example:

```text
status = APPROVED
```

previously meant:

```text
final approval
```

and now means:

```text
analyst approval only
```

The schema is unchanged, but the contract is broken.

---

# 166. Contract Tests

The platform requires contract testing for critical service integrations.

Contract testing complements:

- unit tests
- integration tests
- end-to-end tests

---

# 167. Provider Contract Test

Provider tests verify that the service satisfies its published contract.

---

# 168. Consumer Contract Test

Consumer tests verify assumptions the consumer makes about the provider.

This is particularly valuable for independently deployed services.

---

# 169. Contract Test Scope

Contract tests should focus on integration behavior such as:

- endpoint
- method
- request
- response
- required fields
- status codes
- headers
- error contract

They should not duplicate all business logic tests.

---

# 170. Consumer-Driven Contracts

Consumer-driven contract testing may be adopted for high-value internal integrations.

The provider must not blindly accept every consumer-specific implementation detail as a permanent contract.

Contracts should remain domain-oriented.

---

# 171. Contract Test Environment

Contract verification should run in CI without requiring a permanently shared environment where practical.

---

# 172. Integration Tests

Integration tests should verify real serialization, validation and controller behavior.

Mock-only controller tests are insufficient for critical contract guarantees.

---

# 173. Serialization Tests

Tests should verify important serialization behavior such as:

- enum values
- date/time format
- null behavior
- field names
- numeric precision
- unknown fields where relevant

---

# 174. Error Contract Tests

Every major error category should have contract tests.

Examples:

```text
400

401

403

404

409

422

429

500

503
```

as applicable.

---

# 175. OpenAPI Validation Tests

Tests should verify that runtime responses conform to the published OpenAPI contract where practical.

---

# 176. Test Naming and Quality

Java tests should follow established project quality standards.

Tests should:

- use deterministic data
- avoid random UUID generation when constants suffice
- avoid `Thread.sleep`
- keep lambdas focused
- use clear test names
- use AssertJ descriptions

Example:

```java
assertThat(response.statusCode())
        .as("status code returned by order API")
        .isEqualTo(HttpStatus.OK);
```

---

# 177. Sonar Compliance

Contract-related tests and production code must comply with Sonar rules.

Quality requirements must not be bypassed merely because the code is generated or infrastructure-oriented.

---

# 178. Generated Code

Generated API clients or models must be clearly separated from handwritten code.

Generated code should not be manually modified if regeneration would overwrite the change.

---

# 179. Client Generation

OpenAPI-generated clients may be used when they improve consistency.

Consumers must still understand:

- timeout
- retry
- authentication
- resilience
- compatibility

Generated clients do not replace integration design.

---

# 180. Shared Client Libraries

Shared client libraries must not become a mechanism forcing synchronized service releases.

Consumers should be able to upgrade independently.

---

# 181. Client Library Versioning

Client libraries must use semantic versioning or another explicit compatibility model.

Breaking client changes require major-version treatment.

---

# 182. WebClient

Spring WebClient integrations must map remote contracts into local application abstractions.

The rest of the domain should not depend directly on low-level HTTP response structures.

---

# 183. Remote Error Handling

WebClient integrations must:

- apply bounded timeouts
- translate remote errors
- preserve correlation
- avoid leaking tokens
- integrate with Resilience4j where required
- expose meaningful local exceptions

---

# 184. Dependency DTO Isolation

A service should avoid spreading another service's DTO classes throughout its domain.

Use a local anti-corruption boundary where appropriate.

---

# 185. API Composition

An API that composes multiple dependencies must expose its own stable contract.

It must not simply concatenate arbitrary downstream payloads.

---

# 186. Partial Dependency Failure

Composite APIs must define behavior when optional dependencies fail.

Possible behaviors include:

- complete failure
- partial response
- fallback
- omitted optional data

The behavior must be documented.

---

# 187. Partial Response

If partial responses are supported, the contract must make incompleteness explicit.

Consumers must not mistake partial data for complete data.

---

# 188. Performance Contract

APIs should have documented performance expectations for critical operations.

Formal reliability targets belong to ADR-020.

---

# 189. Response Size

Large response payloads should be avoided.

Strategies include:

- pagination
- summary DTOs
- field-specific endpoints
- asynchronous export

---

# 190. Header Responses

Search/list endpoints should return only the data required for list presentation where appropriate.

Detailed resource payloads should not automatically be reused for every search result.

---

# 191. N+1 API Calls

API design should avoid forcing consumers into unnecessary N+1 request patterns.

Example:

```text
GET responsible consultant
```

should include identifiers already required by the consuming workflow when they are naturally part of the same contract and can be obtained without inappropriate coupling.

---

# 192. Over-Fetching

Avoid returning entire domain graphs merely because the data is available.

Contracts should be purpose-driven.

---

# 193. Under-Fetching

Avoid contracts that require repeated secondary calls for information naturally belonging to the requested representation.

The balance should be evaluated from actual use cases.

---

# 194. Caching

Cache-related HTTP headers may be used when resource semantics permit.

Examples:

```text
ETag

Cache-Control
```

Sensitive or highly dynamic data requires appropriate policies.

---

# 195. ETag

ETags may support:

- caching
- optimistic concurrency

Their semantics must be documented if exposed.

---

# 196. Security Headers

Security-related response headers should normally be managed consistently through platform or gateway configuration where applicable.

---

# 197. CORS

CORS is a browser security policy and must be configured intentionally.

Using:

```text
Access-Control-Allow-Origin: *
```

for sensitive authenticated APIs is prohibited unless explicitly justified.

---

# 198. API Ownership

Every API must have an identifiable owner.

Ownership metadata should include:

- service
- team
- repository
- documentation
- support contact
- lifecycle status

---

# 199. Lifecycle Status

An API may have lifecycle status such as:

```text
experimental

active

deprecated

retired
```

Critical consumers should not depend on experimental APIs without explicit agreement.

---

# 200. API Catalog

The organization should maintain an API catalog where platform tooling supports it.

The catalog should provide:

- API name
- owner
- OpenAPI contract
- version
- lifecycle
- service
- documentation

---

# 201. Production Readiness

A new critical REST API is not production-ready until it has:

- OpenAPI contract
- ownership
- authentication definition
- authorization behavior
- validation
- error contract
- correlation
- pagination where necessary
- request limits
- tests
- compatibility baseline
- observability
- SLO where business critical

---

# 202. API Review

Critical new APIs should receive design review before implementation becomes difficult to change.

Review should evaluate:

- domain semantics
- resource design
- naming
- compatibility
- idempotency
- errors
- pagination
- security
- observability
- performance

---

# 203. API Review Is Not Implementation Review

API governance focuses on externally observable behavior.

Internal implementation may evolve independently as long as the contract remains satisfied.

---

# 204. Contract Governance Workflow

The standard workflow is:

```text
Design

↓

Define or update OpenAPI

↓

Review contract

↓

Analyze compatibility

↓

Implement

↓

Run contract tests

↓

Run OpenAPI diff

↓

Deploy compatibly

↓

Observe consumers

↓

Deprecate when necessary
```

---

# 205. Zero-Downtime Integration

This ADR directly supports ADR-021.

During rolling deployment:

```text
Provider N

Provider N+1

Consumer N

Consumer N+1
```

may coexist.

Contracts must therefore tolerate mixed-version communication.

---

# 206. Provider-First Compatibility

A provider may deploy first when the new provider remains compatible with old consumers.

Example:

```text
Add optional response field.
```

---

# 207. Consumer-First Compatibility

A consumer should deploy first when it must learn a new representation before the provider begins producing it.

Example:

```text
Consumer accepts old + new enum representation

↓

Provider starts producing new representation
```

---

# 208. Expand and Contract for APIs

Breaking API evolution should use an Expand and Contract model similar to database evolution.

```text
EXPAND

Introduce compatible new capability

↓

MIGRATE

Move consumers

↓

OBSERVE

Verify old capability is unused

↓

CONTRACT

Remove deprecated capability
```

---

# 209. API Compatibility Window

The provider must preserve old behavior throughout the approved consumer migration window.

---

# 210. Observability

REST API telemetry must support:

- route
- method
- status category
- duration
- service version
- trace context
- correlation in logs

---

# 211. Route Normalization

Metrics must use:

```text
/orders/{orderId}
```

not:

```text
/orders/ed8b709e-...
```

to prevent cardinality explosion.

---

# 212. Error Metrics

Metrics should classify errors using bounded categories.

Do not use:

```text
exception.message
```

as a metric label.

---

# 213. Contract Version Telemetry

Where multiple major API versions coexist, telemetry should distinguish:

```text
v1

v2
```

using bounded attributes.

---

# 214. Deprecated API Telemetry

Deprecated endpoints should expose usage metrics.

This supports evidence-based retirement.

---

# 215. Logging

Structured API logs should include relevant fields such as:

- service
- version
- method
- normalized route
- status
- duration
- correlationId
- errorCode

Subject to security and privacy policy.

---

# 216. Request Body Logging

Request bodies must not be logged by default.

They may contain:

- credentials
- PII
- financial information
- business-sensitive data

---

# 217. Response Body Logging

Response bodies must not be logged indiscriminately.

Diagnostics should use controlled structured metadata.

---

# 218. API SLO

Critical APIs must define SLOs according to ADR-020.

Examples:

- availability
- latency
- business success

---

# 219. Client Errors and SLO

Expected client validation failures should normally not consume provider availability error budget.

Unexpected provider failures should.

The exact SLI classification must remain explicit.

---

# 220. Contract Error and SLO

A provider returning a response that violates its documented contract is a reliability defect even if the HTTP status is `200`.

---

# 221. Contract Drift Monitoring

Where tooling permits, production API behavior may be sampled for contract conformance.

This complements CI validation.

---

# 222. API Security Testing

Critical APIs should include tests for:

- authentication
- authorization
- malformed input
- oversized input
- injection attempts
- sensitive error leakage
- unsupported methods
- invalid content type

---

# 223. SAST

API implementations must pass the organization's SAST controls.

Security findings must be corrected at the responsible boundary rather than hidden through generic transformations that alter legitimate business data.

---

# 224. Input Validation

All external input is untrusted.

Validation must occur before unsafe use.

This does not mean every string should be globally escaped or mutated.

Validation and output encoding are context-specific controls.

---

# 225. Output Encoding

Output encoding must be appropriate to the consumer context.

A JSON API should preserve valid business strings and rely on correct JSON serialization.

HTML-specific escaping should not be applied indiscriminately to business values in JSON contracts.

---

# 226. Anti-Patterns

The following are prohibited:

- undocumented production APIs
- API behavior differing from published OpenAPI
- exposing JPA entities directly
- changing contracts without compatibility analysis
- removing fields without migration
- renaming fields in place
- making optional request fields mandatory without transition
- changing field types incompatibly
- changing enum semantics in place
- assuming enum additions are always harmless
- silently changing HTTP status semantics
- returning stack traces
- exposing downstream exception payloads directly
- using human-readable messages as integration error codes
- exposing tokens in errors
- logging bearer tokens
- using correlation IDs as metric labels
- unbounded collection endpoints
- unrestricted sorting against persistence properties
- undocumented bulk partial-success semantics
- retrying non-idempotent commands blindly
- assuming HTTP timeout means operation did not execute
- creating a new major API version for every additive change
- keeping obsolete major versions forever
- removing deprecated APIs without consumer analysis
- sharing persistence DTOs as enterprise-wide API models
- forcing coordinated provider/consumer deployment
- rebuilding contracts manually without version control
- using generic HTML escaping to alter legitimate JSON business values
- accepting OpenAPI compatibility solely because schema diff passed
- treating a successful HTTP 200 as proof of contract correctness

---

# 227. Positive Consequences

The decision provides:

- predictable REST contracts
- safer independent deployments
- explicit compatibility rules
- automated breaking-change detection
- improved consumer confidence
- standardized error handling
- standardized pagination
- safer retries
- explicit idempotency
- improved observability
- better API documentation
- controlled deprecation
- reduced integration incidents
- stronger zero-downtime capability
- better API ownership
- improved security boundaries
- clearer client-generation support

---

# 228. Negative Consequences

The decision introduces:

- OpenAPI maintenance
- compatibility checks
- additional contract tests
- deprecation lifecycle management
- temporary parallel API versions
- consumer inventory effort
- more design review
- idempotency storage where required
- standardized DTO mapping
- CI/CD complexity

These costs are accepted because unmanaged API evolution creates distributed coupling and production integration failures.

---

# 229. Neutral Consequences

The decision also means:

- not every API change requires a new version
- some enum additions require consumer preparation
- deprecated endpoints may remain for multiple releases
- provider teams must understand consumer behavior
- consumers must tolerate compatible additive evolution
- API DTOs may duplicate portions of domain models
- some endpoints require explicit idempotency infrastructure
- some breaking changes require temporary parallel contracts

---

# 230. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Provider breaks existing consumer | Critical | Medium | Automated compatibility checks |
| OpenAPI differs from runtime | High | Medium | Integration and conformance tests |
| Semantic break not detected by diff | High | Medium | Contract review |
| Enum addition breaks generated client | High | Medium | Consumer compatibility policy |
| Deprecated API remains forever | Medium | High | Lifecycle ownership and usage telemetry |
| New major version duplicates code | Medium | Medium | Version only for real breaking changes |
| Error messages become consumer contract | Medium | High | Stable machine-readable error codes |
| Duplicate POST creates duplicate business operation | Critical | Medium | Idempotency keys |
| Pagination allows excessive load | High | Medium | Maximum page size |
| Sort leaks persistence model | Medium | Medium | Explicit sort allowlist |
| Correlation ID causes metric cardinality | High | Low | Logs only, never metric dimension |
| Downstream error leaks secrets | Critical | Low | Controlled error translation |
| Generic sanitization mutates valid business text | High | Medium | Context-specific validation/encoding |
| Timeout causes unsafe client retry | Critical | Medium | Idempotency and retry semantics |
| Contract versions require coordinated deployment | High | Medium | Expand and Contract |
| JPA changes alter API unexpectedly | High | Medium | Dedicated DTOs |
| Unknown response fields break clients | High | Medium | Robust consumer policy |
| Breaking change bypasses CI | High | Low | Protected pipeline gates |
| Unbounded bulk request exhausts service | High | Medium | Explicit bulk limits |
| Contract removal occurs while consumer remains | Critical | Low | Consumer inventory and telemetry |

---

# 231. Implementation Guidance

The following rules are mandatory:

1. Every production REST API must have an OpenAPI contract.
2. OpenAPI definitions must be version controlled.
3. Runtime behavior must remain aligned with OpenAPI.
4. APIs must evolve backward compatibly whenever practical.
5. Additive evolution is preferred.
6. Breaking changes require explicit compatibility management.
7. Major API versions are created only for incompatible evolution.
8. Minor compatible changes do not create URI minor versions.
9. Field removal is breaking.
10. Field rename is breaking.
11. Field type changes require compatibility analysis.
12. Making an optional request field mandatory is breaking.
13. Enum additions must be treated as potentially breaking for consumers.
14. Enum semantic changes in place are prohibited.
15. Validation tightening requires compatibility analysis.
16. HTTP status semantics are part of the contract.
17. Public API DTOs must not expose JPA entities.
18. Potentially large collections must be paginated.
19. Pagination must define default and maximum size.
20. Public sort fields must use an allowlist.
21. Critical duplicate-sensitive commands must support idempotency.
22. Idempotency-key reuse with different payloads must be rejected.
23. APIs must use stable machine-readable error codes.
24. Human-readable messages must not be the primary integration contract.
25. Internal exception details must not be exposed.
26. Legitimate business values must not be globally mutated by inappropriate encoding.
27. Correlation identifiers must propagate across synchronous calls.
28. Correlation IDs must not be metric labels.
29. W3C/OpenTelemetry trace context must remain supported.
30. Bearer tokens must never be logged or returned.
31. Request and bulk sizes must be bounded.
32. Async REST operations must make asynchronous semantics explicit.
33. Retry behavior must respect idempotency.
34. Critical API integrations require contract tests.
35. CI/CD should compare candidate OpenAPI with the supported production baseline.
36. Deprecation must precede removal.
37. Deprecated API usage should be observable.
38. Provider and consumer versions must be independently deployable.
39. API evolution must support ADR-021 zero-downtime deployment.
40. Critical APIs must define reliability objectives according to ADR-020.

---

# 232. Validation

The decision will be validated through:

- OpenAPI schema validation
- OpenAPI diff
- provider contract tests
- consumer contract tests
- integration tests
- serialization tests
- validation tests
- error-contract tests
- authentication tests
- authorization tests
- idempotency tests
- concurrent-request tests
- pagination tests
- sorting allowlist tests
- bulk-limit tests
- correlation propagation tests
- OpenTelemetry tests
- security tests
- SAST
- API compatibility review
- deprecation telemetry
- rolling-version integration tests
- production-readiness review

---

# 233. Success Criteria

The decision is successful when:

- every production REST API has an authoritative contract
- provider implementation matches OpenAPI
- breaking changes are detected before production
- old and new service versions communicate safely during rolling deployments
- consumers can upgrade independently
- APIs expose consistent errors
- machine integrations use stable error codes
- duplicate-sensitive operations are retry-safe
- pagination prevents uncontrolled result sets
- contract changes remain auditable
- deprecated endpoints have measurable usage
- API retirement is evidence based
- distributed tracing propagates correctly
- legitimate business values remain unchanged across JSON boundaries
- API security controls do not leak implementation details
- contract defects decrease over time

---

# 234. Alternatives Rejected

## 234.1 Informal API Contracts

Rejected because documentation and implementation drift cannot be reliably controlled.

---

## 234.2 Java DTOs as the Contract

Rejected because consumers may use different technologies and Java implementation classes are not stable integration specifications.

---

## 234.3 New Version for Every Change

Rejected because compatible additive evolution does not justify major-version proliferation.

---

## 234.4 Never Version APIs

Rejected because some incompatible business evolution cannot be represented safely within one contract.

---

## 234.5 Breaking Changes with Coordinated Deployment

Rejected because mandatory coordinated deployment undermines independent microservice delivery and zero-downtime operation.

---

## 234.6 Human-Readable Errors Only

Rejected because message text is unsuitable as a stable machine contract.

---

## 234.7 Direct JPA Entity Exposure

Rejected because persistence evolution would unintentionally become API evolution.

---

## 234.8 Unlimited Search Responses

Rejected because dataset growth would create unpredictable memory, latency and network behavior.

---

## 234.9 Global Response Sanitization

Rejected because context-insensitive transformations can corrupt legitimate business values while failing to address the actual security boundary.

---

# 235. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
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
- ADR-023: Adopt API Security Standards

---

# 236. References

- OpenAPI Specification
- HTTP Semantics
- RFC 9110
- RFC 9457 — Problem Details for HTTP APIs
- W3C Trace Context
- OpenTelemetry Specification
- Spring Boot Documentation
- Spring Framework Web Documentation
- Jakarta Bean Validation
- OWASP API Security
- Consumer-Driven Contract Testing
- Semantic Versioning
- Enterprise Order Platform API Guidelines
- Enterprise Order Platform Security Standards
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices

---

# 237. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial REST API contract-governance baseline |

---

# 238. Decision Summary

The Enterprise Order Platform adopts formal API Contract Governance for all production REST APIs.

The governance lifecycle is:

```text
DESIGN

↓

CONTRACT

↓

REVIEW

↓

IMPLEMENT

↓

VERIFY

↓

DEPLOY COMPATIBLY

↓

OBSERVE

↓

DEPRECATE

↓

RETIRE
```

OpenAPI is the canonical machine-readable REST contract.

The primary evolution strategy is:

```text
Backward-Compatible Additive Change
```

When incompatible evolution is unavoidable:

```text
EXPAND

↓

INTRODUCE NEW CONTRACT

↓

MIGRATE CONSUMERS

↓

OBSERVE OLD CONTRACT

↓

DEPRECATE

↓

CONTRACT
```

The platform standardizes:

```text
OpenAPI

Backward Compatibility

Major API Versioning

Contract Testing

Stable Error Codes

Pagination

Sorting

Idempotency

Correlation IDs

OpenTelemetry Trace Context

Deprecation Governance
```

The fundamental deployment requirement is:

```text
Provider N

Provider N+1

Consumer N

Consumer N+1
```

must be able to coexist whenever they remain within supported contracts.

An API change is not considered safe merely because:

```text
the provider compiles

or

the provider's tests pass.
```

It is safe when:

```text
The contract remains valid

+

Supported consumers remain compatible

+

Runtime behavior matches OpenAPI

+

Security boundaries remain intact

+

The change can be deployed independently
```

This decision establishes the REST contract-governance foundation required for independently deployable services in the Enterprise Order Platform.
