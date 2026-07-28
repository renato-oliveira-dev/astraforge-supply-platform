# ADR-056: Adopt Enterprise REST API Design, Versioning, Error Handling and Integration Contract Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-056 |
| Title | Adopt Enterprise REST API Design, Versioning, Error Handling and Integration Contract Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | REST, HTTP, Spring Boot, OpenAPI, Integration Contracts |
| Related Work Items | REST APIs, OpenAPI, Error Handling, Versioning, Contract Testing |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

REST APIs are one of the primary integration boundaries of the Enterprise Order Platform.

They connect:

```text
Frontend Applications

Mobile Applications

Internal Microservices

Batch Jobs

Integration Platforms

Partner Systems

Operational Tools
```

An API is therefore not merely a controller implementation.

It is a long-lived integration contract.

Poorly governed APIs commonly create:

```text
Inconsistent URLs

Incorrect HTTP Semantics

Breaking Changes

Unbounded Responses

Ambiguous Errors

DTO Coupling

Enum Compatibility Problems

Duplicate Operations

Security Exposure

Client-Specific Workarounds
```

The platform requires a consistent API standard that preserves compatibility while allowing controlled evolution.

---

# 2. Problem Statement

The organization requires standards covering:

- REST resource modeling
- URI conventions
- HTTP methods
- HTTP status codes
- request/response DTOs
- pagination
- filtering
- sorting
- search
- validation
- error handling
- RFC 9457 Problem Details
- correlation identifiers
- idempotency
- OpenAPI
- API versioning
- backward compatibility
- deprecation
- sunset
- enum evolution
- PATCH
- bulk operations
- request limits
- response limits
- security
- contract testing

---

# 3. Decision Drivers

Primary drivers are:

1. consistency
2. backward compatibility
3. interoperability
4. discoverability
5. maintainability
6. security
7. operational diagnostics
8. evolvability
9. predictable client behavior
10. contract governance

---

# 4. Decision

All REST APIs MUST be treated as externally observable contracts.

The canonical lifecycle is:

```text
BUSINESS CAPABILITY
       |
       v
RESOURCE MODEL
       |
       v
HTTP CONTRACT
       |
       v
OPENAPI
       |
       v
IMPLEMENTATION
       |
       v
CONTRACT TEST
       |
       v
DEPLOYMENT
       |
       v
COMPATIBILITY MONITORING
       |
       v
CONTROLLED EVOLUTION
```

---

# 5. Fundamental Principle

The governing principle is:

```text
Design APIs around stable business
resources and capabilities,
not around implementation methods.
```

---

# 6. Resource-Oriented Design

REST APIs SHOULD model business resources.

Prefer:

```text
/orders
/customers
/products
/carts
```

over implementation-oriented endpoints such as:

```text
/getOrders
/createOrder
/deleteCustomer
```

---

# 7. URI Naming

Resource names SHOULD normally use plural nouns.

Example:

```text
/orders
/customers
/products
```

---

# 8. URI Case

URIs SHOULD use lowercase characters.

Prefer:

```text
/order-items
```

over:

```text
/OrderItems
```

---

# 9. URI Word Separator

Use hyphens when multiple words are required.

Example:

```text
/delivery-addresses
```

---

# 10. No Verbs for Basic CRUD

HTTP methods already express standard operations.

Avoid:

```text
POST /orders/create
GET /orders/get
DELETE /orders/delete/{id}
```

Prefer:

```text
POST /orders

GET /orders/{id}

DELETE /orders/{id}
```

---

# 11. Business Actions

Explicit business actions MAY appear when they represent domain commands rather than CRUD.

Examples:

```text
POST /orders/{id}/approve

POST /orders/{id}/cancel

POST /carts/{id}/checkout
```

---

# 12. Business Action Semantics

An action endpoint MUST represent a meaningful domain transition.

It MUST NOT merely compensate for poor resource modeling.

---

# 13. Resource Identifiers

Resource identifiers SHOULD appear in the path.

Example:

```text
GET /orders/{orderId}
```

---

# 14. Query Parameters

Query parameters SHOULD represent:

```text
Filtering

Sorting

Pagination

Projection

Search Options
```

---

# 15. HTTP GET

`GET` retrieves resource state.

GET MUST:

- be safe
- not intentionally modify business state
- remain idempotent

---

# 16. GET Side Effects

Operational side effects such as:

```text
Metrics

Access Logs

Tracing
```

are acceptable.

Business state mutation is not.

---

# 17. HTTP POST

`POST` SHOULD be used for:

```text
Resource Creation

Domain Commands

Non-Idempotent Processing
```

---

# 18. HTTP PUT

`PUT` SHOULD represent idempotent replacement/update semantics when appropriate.

---

# 19. HTTP PATCH

`PATCH` SHOULD represent partial modification.

---

# 20. HTTP DELETE

`DELETE` removes or logically deletes a resource according to the domain contract.

---

# 21. Idempotent DELETE

Repeated DELETE operations SHOULD have predictable idempotent semantics.

---

# 22. HTTP Status Codes

Status codes MUST reflect actual protocol semantics.

---

# 23. 200 OK

Use:

```text
200 OK
```

for successful requests returning a representation.

---

# 24. 201 Created

Use:

```text
201 Created
```

when a new resource is created synchronously.

---

# 25. Location Header

A successful creation SHOULD provide:

```text
Location
```

when a stable resource URI exists.

---

# 26. 202 Accepted

Use:

```text
202 Accepted
```

when processing has been accepted but is not complete.

---

# 27. 204 No Content

Use:

```text
204 No Content
```

when an operation succeeds and intentionally returns no response body.

---

# 28. 400 Bad Request

Use:

```text
400 Bad Request
```

for malformed requests or request-level validation failures that do not fit a more specific status.

---

# 29. 401 Unauthorized

Use:

```text
401 Unauthorized
```

when authentication is missing or invalid.

---

# 30. 403 Forbidden

Use:

```text
403 Forbidden
```

when authentication exists but authorization denies access.

---

# 31. 404 Not Found

Use:

```text
404 Not Found
```

when the requested resource does not exist or cannot appropriately be exposed to the caller.

---

# 32. 409 Conflict

Use:

```text
409 Conflict
```

for conflicts with current resource/business state.

Examples:

```text
Duplicate business resource

Invalid state transition due to current state

Optimistic concurrency conflict
```

when applicable.

---

# 33. 412 Precondition Failed

Use:

```text
412 Precondition Failed
```

when HTTP conditional request preconditions fail.

---

# 34. 415 Unsupported Media Type

Use:

```text
415 Unsupported Media Type
```

when the request media type is unsupported.

---

# 35. 422 Unprocessable Content

`422` MAY be used for semantically invalid requests when the platform deliberately distinguishes semantic validation from malformed requests.

The choice between `400` and `422` MUST remain consistent within the API standard.

---

# 36. 429 Too Many Requests

Use:

```text
429 Too Many Requests
```

when rate limiting rejects the request.

---

# 37. 500 Internal Server Error

Use:

```text
500 Internal Server Error
```

for unexpected internal failures.

---

# 38. 502 Bad Gateway

`502` MAY represent an invalid/unusable upstream response when the service acts as an integration boundary.

---

# 39. 503 Service Unavailable

Use:

```text
503 Service Unavailable
```

when temporary service unavailability prevents processing.

---

# 40. 504 Gateway Timeout

`504` MAY be used where gateway/proxy semantics apply to upstream timeout.

Internal APIs MUST apply status semantics consistently.

---

# 41. Do Not Return 200 for Errors

This is prohibited:

```json
{
  "success": false,
  "error": "Order not found"
}
```

with:

```text
HTTP 200
```

for a failed operation.

---

# 42. Request DTOs

Public API requests MUST use explicit contract DTOs.

---

# 43. Persistence Entities

JPA entities MUST NOT be exposed directly as public API contracts.

---

# 44. Response DTOs

Responses SHOULD use API-specific DTOs/records.

---

# 45. DTO Independence

The API contract SHOULD remain independent from:

```text
Database Schema

JPA Mapping

Internal Domain Implementation

External Provider DTO
```

---

# 46. Java Records

Java records SHOULD be considered for immutable API DTOs where appropriate.

---

# 47. Validation

Input validation MUST occur at the API boundary.

---

# 48. Bean Validation

Jakarta Bean Validation SHOULD be used for structural constraints.

Examples:

```java
@NotNull
@NotBlank
@Size
@Pattern
@Positive
```

---

# 49. Business Validation

Complex business rules belong in application/domain services rather than controller annotations.

---

# 50. Validation Layers

Canonical model:

```text
REQUEST
   |
   v
STRUCTURAL VALIDATION
   |
   v
APPLICATION VALIDATION
   |
   v
DOMAIN RULES
```

---

# 51. Validation Message

Validation errors MUST be understandable by API consumers.

---

# 52. Error Contract

The platform adopts a standardized error representation based on:

```text
RFC 9457
Problem Details for HTTP APIs
```

---

# 53. Problem Details

Canonical fields include:

```text
type

title

status

detail

instance
```

---

# 54. Extension Fields

Platform extensions MAY include:

```text
errorCode

timestamp

correlationId

traceId

violations
```

---

# 55. Example Error

```json
{
  "type": "https://api.example.com/problems/order-not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "The requested order could not be found.",
  "instance": "/orders/123",
  "errorCode": "ORDER_NOT_FOUND",
  "correlationId": "f18f4c8e..."
}
```

---

# 56. Stable Error Code

Machine-readable:

```text
errorCode
```

SHOULD remain stable across message localization changes.

---

# 57. Human Message

Human-readable error text MUST NOT be used as the sole programmatic integration contract.

---

# 58. Internationalization

Human-readable messages MAY be localized.

Machine-readable error semantics MUST remain stable.

---

# 59. Validation Violations

Field validation failures SHOULD expose structured violations.

Example:

```json
{
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

# 60. Sensitive Errors

Error responses MUST NOT expose:

```text
Stack Traces

Database SQL

Passwords

Tokens

Internal Credentials

Sensitive Infrastructure Details
```

---

# 61. Internal Exception

Internal exception messages MUST NOT automatically become public API messages.

---

# 62. Global Exception Handling

Spring Boot APIs SHOULD centralize HTTP exception mapping.

Example:

```text
@RestControllerAdvice
```

---

# 63. Exception Mapping

Mapping SHOULD be deterministic.

Example:

```text
ResourceNotFoundException
        |
        v
404 + RESOURCE_NOT_FOUND
```

---

# 64. Generic Failure

Unexpected exceptions SHOULD map to a safe generic response while retaining detailed internal diagnostics.

---

# 65. Either Log or Rethrow

Exception handling MUST avoid catching exceptions merely to suppress them.

An exception SHOULD:

```text
Be handled meaningfully

or

Be propagated
```

Logging strategy MUST avoid duplicate logging across layers.

---

# 66. Correlation Identifier

Requests SHOULD carry or receive a correlation identifier.

---

# 67. Correlation Propagation

Correlation context SHOULD propagate through:

```text
HTTP Calls

SQS Events

Async Processing

Logs
```

where relevant.

---

# 68. Trace Identifier

Distributed tracing identifiers MAY coexist with business/request correlation identifiers.

---

# 69. Correlation ID Is Not Business ID

Do not use:

```text
correlationId
```

as a substitute for:

```text
orderId
customerId
eventId
```

---

# 70. Idempotency

Critical retriable POST operations SHOULD support explicit idempotency where duplicate execution could create duplicate business effects.

---

# 71. Idempotency-Key

Where applicable, clients MAY provide:

```text
Idempotency-Key
```

---

# 72. Idempotency Scope

The contract MUST define:

```text
Key Scope

Retention

Duplicate Behavior

Response Behavior

Conflict Behavior
```

---

# 73. Same Key Same Request

Repeated equivalent requests using the same valid key SHOULD produce one logical business effect.

---

# 74. Same Key Different Request

Reuse of the same idempotency key with materially different payload SHOULD be rejected.

---

# 75. Pagination

Collection endpoints returning potentially large result sets MUST support bounded pagination.

---

# 76. Page Size

Page size MUST have:

```text
Default

Maximum
```

limits.

---

# 77. Unbounded Collection

This is prohibited for potentially large datasets:

```text
GET /orders
```

returning every historical order.

---

# 78. Pagination Parameters

A common offset-based contract is:

```text
page

size
```

---

# 79. Pagination Response

Responses SHOULD expose sufficient navigation metadata.

Example:

```text
content

page

size

totalElements

totalPages
```

when total-count semantics are appropriate.

---

# 80. Count Cost

APIs SHOULD avoid expensive total-count queries when clients do not require totals.

---

# 81. Keyset Pagination

Keyset/cursor pagination SHOULD be considered for high-volume sequential access.

---

# 82. Cursor Contract

Cursor values SHOULD be opaque to clients.

---

# 83. Sorting

Sorting MUST be restricted to approved fields.

---

# 84. Arbitrary Sort Property

Clients MUST NOT be allowed to reference arbitrary internal persistence paths.

---

# 85. Sort Direction

Supported values SHOULD be explicit:

```text
asc

desc
```

---

# 86. Filtering

Filtering SHOULD use stable API field names.

---

# 87. Filter Validation

Unknown or unsupported filters SHOULD produce deterministic behavior.

---

# 88. Search

Complex search endpoints MAY use:

```text
GET with query parameters
```

for moderate/simple filters.

---

# 89. Complex Search Body

For complex structured criteria, an explicit search resource MAY use:

```text
POST /orders/search
```

when GET query semantics become impractical.

---

# 90. Search POST

Using POST for complex search MUST NOT imply creation of a business order resource.

---

# 91. Projection

APIs MAY expose reduced representations for high-volume searches.

---

# 92. Header/List DTO

List/search endpoints SHOULD avoid returning full detail objects when only summary/header data is required.

---

# 93. Payload Efficiency

Contract design SHOULD minimize unnecessary:

```text
Serialization

Network Traffic

Memory Allocation

Database Fetching
```

---

# 94. API Versioning

Backward-compatible evolution is preferred over unnecessary version creation.

---

# 95. Version Only When Needed

A new API version SHOULD be introduced only for changes that cannot reasonably remain backward compatible.

---

# 96. Versioning Strategy

The platform SHOULD standardize one primary public versioning strategy.

A common strategy is:

```text
/api/v1/orders
```

---

# 97. Internal Service APIs

Internal APIs MAY follow gateway/platform conventions if versioning is handled outside the resource path.

Consistency remains mandatory.

---

# 98. Breaking Changes

Examples of potentially breaking changes include:

```text
Removing a field

Renaming a field

Changing field type

Changing field semantics

Making optional field mandatory

Removing enum value

Changing status semantics

Changing endpoint URI

Changing HTTP method
```

---

# 99. Additive Change

Adding an optional response field is generally backward compatible for correctly implemented clients.

---

# 100. Client Robustness

Clients SHOULD tolerate unknown additive response fields.

---

# 101. Request Compatibility

Adding a new mandatory request field is normally breaking.

---

# 102. Optional Request Field

New request fields SHOULD normally be optional or have safe server-side defaults when compatibility is required.

---

# 103. Field Removal

Fields MUST NOT be removed from an active API version without completing the deprecation process.

---

# 104. Field Rename

Renaming:

```text
userId
```

to:

```text
requestUser
```

is a breaking contract change unless compatibility is preserved.

---

# 105. DTO Evolution

DTO evolution MUST consider both:

```text
Serialization Compatibility

Business Semantic Compatibility
```

---

# 106. Enum Compatibility

Enums are especially sensitive integration contracts.

---

# 107. Adding Enum Value

Adding a response enum value can break consumers that assume a closed exhaustive set.

---

# 108. Consumer Enum Handling

Consumers SHOULD define safe behavior for unknown future enum values where feasible.

---

# 109. Removing Enum Value

Removing a published enum value is normally breaking.

---

# 110. Renaming Enum Value

Renaming an enum value is normally breaking.

---

# 111. Internal vs External Enum

Internal Java enum names SHOULD NOT automatically define the external API representation.

---

# 112. Explicit Mapping

Prefer:

```text
Internal Status
      |
      v
API Mapping
      |
      v
External Status
```

when external compatibility requires independence.

---

# 113. Business Status

Status values MUST have documented semantics.

---

# 114. Case Sensitivity

Enum/string case sensitivity MUST be explicit and consistent.

---

# 115. PATCH Semantics

PATCH contracts MUST clearly define partial-update behavior.

---

# 116. Missing vs Null

PATCH MUST distinguish where relevant:

```text
Field absent

Field explicitly null

Field supplied with value
```

---

# 117. JSON Merge Patch

RFC 7396 JSON Merge Patch MAY be adopted when its null semantics fit the resource model.

---

# 118. JSON Patch

RFC 6902 JSON Patch MAY be adopted for operation-based patch requirements.

---

# 119. Ad Hoc PATCH

Custom partial-update DTOs MAY be preferable when domain rules require explicit controlled fields.

---

# 120. PATCH Validation

Partial updates MUST still satisfy resulting domain invariants.

---

# 121. Optimistic Concurrency

APIs modifying concurrently edited resources SHOULD consider optimistic concurrency controls.

---

# 122. ETag

HTTP-native concurrency MAY use:

```text
ETag

If-Match
```

where appropriate.

---

# 123. Version Field

A domain/resource version field MAY alternatively support concurrency semantics.

---

# 124. Lost Update

APIs SHOULD prevent silent lost updates where concurrent editing is material.

---

# 125. Bulk APIs

Bulk operations SHOULD be explicit contracts.

---

# 126. Bulk Request

Example:

```text
POST /orders/approve/bulk
```

is acceptable for a domain-level mass approval capability.

---

# 127. Bulk Size

Bulk request size MUST be bounded.

---

# 128. Bulk Atomicity

The contract MUST define whether the bulk operation is:

```text
Atomic

or

Partial Success
```

---

# 129. Partial Success

If partial success is allowed, item-level results MUST be explicit.

---

# 130. Bulk Error Example

Conceptually:

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
      "errorCode": "INVALID_STATUS"
    }
  ]
}
```

---

# 131. Bulk Performance

Bulk operations SHOULD reduce unnecessary N+1 database and HTTP behavior.

---

# 132. Bulk Validation

Shared validations SHOULD be performed once when possible.

---

# 133. Per-Item Validation

Item-specific business rules remain independently validated.

---

# 134. Request Size Limit

HTTP request body size MUST be bounded at appropriate infrastructure layers.

---

# 135. Response Size

APIs SHOULD avoid unbounded response size.

---

# 136. String Length

Text fields SHOULD have explicit reasonable maximum lengths.

---

# 137. Collection Length

Request collections SHOULD have explicit maximum sizes.

---

# 138. Defensive Boundary

API limits protect:

```text
Memory

CPU

Database

Serialization

Downstream Dependencies
```

---

# 139. File Upload

File-upload APIs MUST define:

```text
Maximum Size

Allowed Media Types

Security Validation

Storage Strategy
```

---

# 140. Content-Type

APIs MUST use explicit media types.

Typically:

```text
application/json
```

---

# 141. Accept

Content negotiation SHOULD be deterministic.

---

# 142. Character Encoding

UTF-8 SHOULD be the standard encoding for textual JSON APIs.

---

# 143. Date and Time

API date/time formats MUST be standardized.

---

# 144. ISO 8601

Date/time values SHOULD use ISO 8601 compatible representations.

---

# 145. OffsetDateTime

For timestamps requiring an explicit offset, Java APIs SHOULD consider:

```java
OffsetDateTime
```

---

# 146. LocalDate

Date-only business values SHOULD use:

```java
LocalDate
```

rather than timestamp strings.

---

# 147. Time Zone

Time-zone semantics MUST NOT be implicit.

---

# 148. Money

Financial amounts MUST use decimal-safe representations.

---

# 149. BigDecimal

Java monetary calculations SHOULD use:

```java
BigDecimal
```

rather than binary floating-point types.

---

# 150. Money Contract

The API MUST define currency semantics when amounts can involve multiple currencies.

---

# 151. Boolean

Boolean fields SHOULD use meaningful positive names.

Prefer:

```text
active
enabled
approved
```

over confusing double negatives.

---

# 152. Nullability

API nullability SHOULD be explicit in schema/documentation.

---

# 153. Empty vs Null

Contracts SHOULD distinguish:

```text
null

empty string

empty collection

missing property
```

where business semantics differ.

---

# 154. Collections

Response collections SHOULD normally return:

```json
[]
```

rather than `null` when the semantic value is an empty collection.

---

# 155. OpenAPI

Every supported REST API MUST have an OpenAPI contract.

---

# 156. OpenAPI Scope

The specification SHOULD document:

```text
Paths

Operations

Parameters

Schemas

Validation

Responses

Errors

Authentication

Examples
```

---

# 157. OpenAPI Accuracy

OpenAPI MUST reflect the deployed implementation.

---

# 158. Stale Documentation

An OpenAPI document that differs from production behavior is a defect.

---

# 159. Operation ID

Operations SHOULD have stable meaningful `operationId` values when client generation or tooling depends on them.

---

# 160. Schema Reuse

Reusable schemas SHOULD use OpenAPI components where practical.

---

# 161. Examples

Representative request/response examples SHOULD be included for non-trivial contracts.

---

# 162. Error Documentation

OpenAPI SHOULD document expected error responses.

---

# 163. Security Scheme

Authentication requirements MUST be represented in OpenAPI.

---

# 164. OpenAPI Exposure

Interactive API documentation exposure MUST follow environment/security policy.

---

# 165. Production API Docs

Public exposure of Swagger/OpenAPI UI in production MUST be an explicit security/platform decision.

---

# 166. Contract Testing

Published APIs MUST have automated contract validation appropriate to their criticality.

---

# 167. Provider Validation

Provider tests SHOULD verify implementation against the published API contract.

---

# 168. Consumer Validation

Critical consumers SHOULD validate the assumptions they depend on.

---

# 169. Breaking Change Detection

CI SHOULD detect incompatible OpenAPI changes.

---

# 170. Contract Diff

Contract comparison SHOULD detect changes such as:

```text
Removed endpoint

Removed field

New mandatory field

Type change

Enum contraction

Status-code change
```

---

# 171. Additive Compatibility

Not every OpenAPI difference is breaking.

Governance tooling MUST distinguish additive changes from incompatible changes.

---

# 172. API-First

API-first design SHOULD be considered for contracts shared across teams.

---

# 173. Code-First

Code-first generation MAY be used when governance guarantees contract review and compatibility.

---

# 174. Source of Truth

Each API MUST have a clearly identified contract source of truth.

---

# 175. Deprecation

Deprecated APIs MUST remain explicitly identifiable.

---

# 176. Deprecation Process

The process SHOULD include:

```text
Announce

Mark Deprecated

Identify Consumers

Provide Replacement

Migration Window

Measure Usage

Sunset

Remove
```

---

# 177. Deprecation Header

Standard HTTP deprecation metadata SHOULD be used where supported by platform conventions.

---

# 178. Sunset

A planned retirement MAY expose:

```text
Sunset
```

metadata according to applicable HTTP standards.

---

# 179. Consumer Discovery

Before removing an API, active consumers SHOULD be identified.

---

# 180. Usage Metrics

Deprecated API usage SHOULD be measurable.

---

# 181. No Surprise Removal

Published APIs MUST NOT disappear without an approved compatibility/deprecation process.

---

# 182. Version Lifetime

Version proliferation SHOULD be avoided.

---

# 183. Too Many Versions

Maintaining:

```text
v1
v2
v3
v4
v5
```

simultaneously creates operational and maintenance cost.

---

# 184. Compatibility First

Prefer compatible evolution inside the existing version when semantics permit it.

---

# 185. Security

Every API MUST apply security according to its exposure and data classification.

---

# 186. Authentication

Protected APIs MUST require approved authentication.

---

# 187. Authorization

Authorization MUST be enforced server-side.

---

# 188. Frontend Validation

Frontend authorization/visibility MUST NOT be treated as security enforcement.

---

# 189. Object-Level Authorization

Endpoints retrieving resources by identifier MUST consider whether the caller is authorized for that specific resource.

---

# 190. Mass Assignment

Request DTOs MUST expose only fields clients are permitted to control.

---

# 191. Entity Binding

Binding arbitrary client JSON directly to persistence entities is prohibited.

---

# 192. Input Validation

All untrusted input MUST be validated according to business and security requirements.

---

# 193. Injection

Database access MUST use parameterized mechanisms.

---

# 194. Sensitive Fields

Sensitive values MUST NOT be returned unless explicitly required.

---

# 195. Tokens

Authentication tokens MUST NOT appear in API response bodies or error details unless the protocol specifically requires them.

---

# 196. Security Headers

Platform/gateway standards SHOULD enforce appropriate HTTP security headers.

---

# 197. CORS

CORS MUST be explicitly configured.

---

# 198. Wildcard CORS

Unrestricted:

```text
*
```

CORS MUST NOT be used for credentialed protected APIs without an approved reason.

---

# 199. Cache-Control

Sensitive API responses SHOULD use appropriate cache directives.

---

# 200. Rate Limiting

Public or resource-sensitive APIs SHOULD use rate limiting where required by threat/capacity analysis.

---

# 201. API Logging

Request logging MUST NOT indiscriminately log complete request/response bodies.

---

# 202. Sensitive Payload

Sensitive payload fields MUST be masked or excluded.

---

# 203. Diagnostic Context

API logs SHOULD include:

```text
Operation

HTTP Method

Status

Elapsed Time

Correlation ID
```

where applicable.

---

# 204. Performance

API design MUST consider performance characteristics.

---

# 205. N+1 API

Avoid API designs requiring consumers to execute excessive request chains when a reasonable aggregate/batch capability can be provided.

---

# 206. Chatty API

A highly chatty API SHOULD be evaluated for:

```text
Batching

Aggregation

Projection
```

---

# 207. Over-Fetching

Responses SHOULD avoid returning expensive unused data.

---

# 208. Under-Fetching

Extreme normalization that forces dozens of remote calls SHOULD also be avoided.

---

# 209. API Timeout

Server and client timeouts MUST align with resilience standards.

---

# 210. Long-Running Operation

Long-running operations SHOULD consider asynchronous processing.

---

# 211. Async Pattern

Canonical pattern:

```text
POST /reports
      |
      v
202 Accepted
      |
      v
/report-jobs/{id}
      |
      v
GET STATUS
```

---

# 212. Async Status

Async resources SHOULD expose explicit lifecycle states.

---

# 213. Polling

Polling contracts SHOULD define reasonable frequency or retry guidance.

---

# 214. Webhook/Event Alternative

For suitable integrations, asynchronous events MAY be preferable to repeated polling.

---

# 215. Controller Responsibility

Controllers SHOULD remain thin.

---

# 216. Controller Flow

Prefer:

```text
HTTP Mapping

Validation

Authentication Context

Application Service Invocation

Response Mapping
```

---

# 217. Business Logic in Controller

Complex business rules MUST NOT reside in controllers.

---

# 218. Mapper

Dedicated mappers MAY separate API DTOs from domain/application models.

---

# 219. Mapping Complexity

Simple mappings SHOULD NOT be abstracted unnecessarily.

---

# 220. Client Design

HTTP clients SHOULD encapsulate dependency-specific protocol details.

---

# 221. Client Boundary

Application services SHOULD NOT repeatedly construct:

```text
URLs

Headers

HTTP status mapping
```

for the same dependency.

---

# 222. Remote DTO

External provider DTOs SHOULD remain isolated from internal domain models.

---

# 223. Error Mapping

HTTP clients MUST translate remote errors consistently.

---

# 224. Resilience

HTTP clients MUST follow ADR-055 for:

```text
Timeout

Retry

Circuit Breaker

Bulkhead

Failure Classification
```

---

# 225. API Evolution Workflow

Changes SHOULD follow:

```text
PROPOSE CONTRACT CHANGE
        |
        v
CLASSIFY COMPATIBILITY
        |
        +--> COMPATIBLE
        |       |
        |       v
        |    UPDATE CONTRACT
        |
        +--> BREAKING
                |
                v
          CAN IT BE REDESIGNED?
              /       \
            YES        NO
             |          |
             v          v
       COMPATIBLE    VERSION /
         CHANGE      DEPRECATION
```

---

# 226. API Review Checklist

Every material API change SHOULD evaluate:

```text
[ ] Is the resource model clear?

[ ] Are URI conventions followed?

[ ] Is the HTTP method correct?

[ ] Is the status code correct?

[ ] Is the request bounded?

[ ] Is the response bounded?

[ ] Are DTOs independent from persistence?

[ ] Is validation explicit?

[ ] Are errors standardized?

[ ] Are error codes stable?

[ ] Is sensitive information protected?

[ ] Is pagination required?

[ ] Are sorting/filtering fields controlled?

[ ] Is the change backward compatible?

[ ] Are enum changes safe?

[ ] Is idempotency required?

[ ] Is concurrency control required?

[ ] Is OpenAPI updated?

[ ] Are contract tests updated?

[ ] Is deprecation required?

[ ] Are security requirements satisfied?
```

---

# 227. API Fitness Functions

Stable API invariants SHOULD be automated where practical.

Examples:

```text
[ ] Controllers do not expose JPA entities

[ ] OpenAPI is generated/validated

[ ] Breaking changes fail CI

[ ] Page size has maximum bounds

[ ] Bulk size has maximum bounds

[ ] Errors follow Problem Details

[ ] Error responses do not expose stack traces

[ ] Protected endpoints declare security

[ ] External clients have timeout configuration

[ ] Deprecated endpoints are measurable

[ ] Public DTOs remain independent from persistence entities
```

---

# 228. Enterprise API Gate

An API is not considered compliant when applicable conditions include:

```text
[ ] Implementation-oriented URI

[ ] Incorrect HTTP method

[ ] HTTP 200 used for business/technical failure

[ ] JPA entity exposed directly

[ ] Unbounded collection response

[ ] Unbounded bulk request

[ ] Internal stack trace exposed

[ ] Unstable human message used as machine contract

[ ] Breaking change without version/deprecation strategy

[ ] New mandatory request field added incompatibly

[ ] Enum compatibility ignored

[ ] OpenAPI differs from implementation

[ ] No contract validation

[ ] Authorization relies on frontend

[ ] Sensitive payload logged

[ ] Retry/idempotency semantics undefined for critical writes
```

---

# 229. Anti-Patterns

The following are prohibited or strongly discouraged:

- `/getOrders`
- `/createOrder`
- `/deleteOrder`
- HTTP 200 for failures
- GET modifying business state
- persistence entities exposed as API DTOs
- controllers containing domain logic
- arbitrary internal sort paths
- unbounded pagination
- unbounded bulk operations
- returning stack traces
- exposing raw database exceptions
- client logic depending on localized error text
- silent breaking changes
- renaming published fields without compatibility
- removing enum values from active contracts
- adding mandatory fields without compatibility analysis
- arbitrary API version proliferation
- stale OpenAPI documentation
- direct external-provider DTO leakage into domain models
- generic PATCH without defined null semantics
- ignoring lost-update concurrency
- logging full sensitive request bodies
- client-specific hacks embedded into shared resource semantics
- frontend-only authorization
- uncontrolled wildcard CORS
- APIs requiring N+1 remote requests when reasonable batching is possible

---

# 230. Positive Consequences

The decision provides:

- consistent APIs
- predictable HTTP semantics
- safer client integration
- stable error contracts
- stronger backward compatibility
- controlled API evolution
- improved OpenAPI quality
- safer enum evolution
- better security
- bounded resource consumption
- stronger contract testing
- easier client generation
- improved operational diagnostics

---

# 231. Negative Consequences

The decision introduces:

- additional API governance
- compatibility analysis
- OpenAPI maintenance
- contract testing
- deprecation management
- explicit DTO mapping
- additional validation

These costs are accepted because API incompatibilities propagate across teams and systems and are substantially more expensive to remediate after release.

---

# 232. Neutral Consequences

The decision also means:

- not every change requires a new version
- not every endpoint maps directly to CRUD
- POST can legitimately represent business commands
- some search APIs may use POST
- some APIs may use cursor rather than offset pagination
- some partial updates may use custom DTOs instead of generic JSON Patch
- internal and external status representations may differ intentionally

---

# 233. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Breaking consumer | Critical | Medium | Contract compatibility gate |
| Version proliferation | High | Medium | Compatibility-first evolution |
| Error contract drift | High | Medium | Problem Details standard |
| Unbounded payload | High | Medium | Explicit limits |
| Enum incompatibility | High | Medium | Compatibility testing |
| OpenAPI drift | High | Medium | Automated validation |
| DTO/domain coupling | Medium | High | Explicit contract DTO |
| Unauthorized resource access | Critical | Medium | Object-level authorization |
| Duplicate POST | High | Medium | Idempotency |
| Sensitive data leakage | Critical | Low/Medium | Error/log sanitization |

---

# 234. Implementation Guidance

The following rules are mandatory:

1. APIs must model stable business resources/capabilities.
2. Standard CRUD operations must use HTTP methods rather than verb-oriented URIs.
3. Domain commands may use explicit action endpoints where appropriate.
4. HTTP status codes must reflect actual operation semantics.
5. HTTP 200 must not represent failed operations.
6. Public API contracts must not expose persistence entities directly.
7. Request and response DTOs must remain explicit integration contracts.
8. Structural validation must occur at the API boundary.
9. Business rules must remain outside controllers.
10. Error responses must follow the platform Problem Details standard.
11. Machine-readable error codes must remain stable.
12. Internal exception details must not leak through public APIs.
13. Correlation context should propagate across integration boundaries.
14. Critical retriable write operations must consider idempotency.
15. Potentially large collections must be bounded and paginated.
16. Sorting must use an approved external field allowlist.
17. Bulk operations must have explicit maximum sizes.
18. Bulk atomicity/partial-success semantics must be documented.
19. Backward-compatible evolution is preferred over new versions.
20. Breaking changes require versioning or an approved migration/deprecation strategy.
21. Published fields must not be removed or renamed silently.
22. Enum changes require explicit compatibility analysis.
23. Internal enum names must not unnecessarily dictate external contracts.
24. PATCH semantics must explicitly define missing/null behavior.
25. Lost-update protection should be implemented where concurrent editing matters.
26. Request and response sizes must be bounded.
27. Date/time contracts must use standardized explicit semantics.
28. Financial values must use decimal-safe representations.
29. Every supported REST API must have an accurate OpenAPI contract.
30. CI should detect breaking OpenAPI changes.
31. Deprecated APIs must have an explicit migration and retirement process.
32. Server-side authorization is mandatory.
33. API DTOs must prevent mass assignment.
34. Sensitive information must not be indiscriminately logged.
35. External HTTP clients must encapsulate protocol and error-mapping behavior.
36. Resilience policies must follow ADR-055.
37. Critical APIs must have automated contract tests.
38. API changes must receive compatibility review before release.

---

# 235. Validation

This ADR will be validated through:

- Spring Boot
- Spring MVC
- Jakarta Bean Validation
- Spring ProblemDetail
- OpenAPI
- Swagger tooling
- JUnit 5
- AssertJ
- MockMvc
- WireMock
- MockWebServer
- contract testing
- OpenAPI diff tooling
- SonarQube
- SAST
- architecture tests
- CI/CD quality gates

---

# 236. Success Criteria

The decision is successful when:

- APIs use consistent resource conventions
- clients receive predictable status codes
- error contracts are standardized
- OpenAPI matches deployed behavior
- breaking changes are detected before release
- version proliferation remains controlled
- enum changes stop unexpectedly breaking consumers
- large requests/responses remain bounded
- duplicate critical writes are safely handled
- deprecated endpoints have measurable migration paths
- public APIs no longer expose persistence implementation details
- API security regressions decrease

---

# 237. Alternatives Rejected

## 237.1 Controller-Driven API Design

Rejected because implementation structure should not define the public contract.

---

## 237.2 HTTP 200 for Every Response

Rejected because it destroys standard HTTP semantics.

---

## 237.3 JPA Entities as API Models

Rejected because it couples persistence and integration contracts.

---

## 237.4 Version Every Change

Rejected because version proliferation creates unnecessary operational cost.

---

## 237.5 Never Version APIs

Rejected because some legitimate changes are fundamentally incompatible.

---

## 237.6 Human Error Message as Contract

Rejected because localization and wording changes would break consumers.

---

## 237.7 OpenAPI as Documentation Only

Rejected because the specification must function as an enforceable integration contract.

---

# 238. Related Decisions

This ADR extends and implements:

- ADR-016: Application Resilience
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-036: API Design and Compatibility Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Observability and Production Diagnostics Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard

---

# 239. References

- RFC 9110 — HTTP Semantics
- RFC 9457 — Problem Details for HTTP APIs
- RFC 7396 — JSON Merge Patch
- RFC 6902 — JSON Patch
- RFC 6585 — Additional HTTP Status Codes
- OpenAPI Specification
- Spring Framework Documentation
- Spring Boot Documentation
- Jakarta Bean Validation
- OWASP API Security Top 10
- REST Architectural Style
- Consumer-Driven Contract Testing
- Semantic Versioning principles

---

# 240. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise REST API and integration contract baseline |

---

# 241. Decision Summary

The API architecture becomes:

```text
BUSINESS CAPABILITY
        |
        v
RESOURCE
        |
        v
HTTP SEMANTICS
        |
        v
DTO CONTRACT
        |
        v
OPENAPI
        |
        v
IMPLEMENTATION
        |
        v
CONTRACT TEST
```

For HTTP operations:

```text
GET     -> READ

POST    -> CREATE / COMMAND

PUT     -> IDEMPOTENT UPDATE/REPLACEMENT

PATCH   -> PARTIAL UPDATE

DELETE  -> REMOVE
```

For error handling:

```text
EXCEPTION
    |
    v
CLASSIFY
    |
    +--> VALIDATION ------> 400
    |
    +--> AUTHENTICATION --> 401
    |
    +--> AUTHORIZATION ---> 403
    |
    +--> NOT FOUND -------> 404
    |
    +--> CONFLICT --------> 409
    |
    +--> RATE LIMIT ------> 429
    |
    +--> UNAVAILABLE -----> 503
    |
    +--> UNEXPECTED ------> 500
```

with:

```text
HTTP STATUS
     +
STABLE ERROR CODE
     +
SAFE HUMAN MESSAGE
     +
CORRELATION CONTEXT
```

For DTO boundaries:

```text
HTTP
 |
 v
REQUEST DTO
 |
 v
APPLICATION / DOMAIN
 |
 v
PERSISTENCE ENTITY
```

and never:

```text
HTTP
 |
 v
JPA ENTITY
```

For compatibility:

```text
CONTRACT CHANGE
      |
      v
BREAKING?
   /       \
 NO         YES
 |           |
 v           v
EVOLVE    REDESIGN?
            /   \
          YES    NO
           |      |
           v      v
      COMPATIBLE VERSION /
        CHANGE   DEPRECATE
```

For enums:

```text
INTERNAL ENUM
      |
      v
EXPLICIT MAPPING
      |
      v
EXTERNAL CONTRACT
```

For large collections:

```text
SEARCH
  |
  v
FILTER
  |
  v
SORT ALLOWLIST
  |
  v
BOUNDED PAGE
  |
  v
SUMMARY DTO
```

For bulk operations:

```text
BULK REQUEST
     |
     v
SIZE LIMIT
     |
     v
SHARED VALIDATION
     |
     v
ITEM VALIDATION
     |
     v
BATCH PROCESSING
     |
     v
EXPLICIT RESULT
```

For idempotency:

```text
POST
 |
 v
IDEMPOTENCY KEY
 |
 v
SEEN BEFORE?
 /        \
NO        YES
|          |
v          v
PROCESS   SAME REQUEST?
             /      \
           YES       NO
            |         |
            v         v
         RETURN     CONFLICT
         ORIGINAL
         RESULT
```

For OpenAPI:

```text
OPENAPI CONTRACT
       |
       +--> DOCUMENTATION
       |
       +--> CLIENT GENERATION
       |
       +--> PROVIDER TEST
       |
       +--> COMPATIBILITY DIFF
       |
       +--> GOVERNANCE
```

For API retirement:

```text
ACTIVE
  |
  v
DEPRECATED
  |
  v
CONSUMER MIGRATION
  |
  v
USAGE -> ZERO
  |
  v
SUNSET
  |
  v
REMOVAL
```

The complete API quality equation is:

```text
RESOURCE-ORIENTED DESIGN
          +
CORRECT HTTP SEMANTICS
          +
EXPLICIT DTO CONTRACTS
          +
STANDARDIZED ERRORS
          +
BOUNDED PAYLOADS
          +
IDEMPOTENCY
          +
BACKWARD COMPATIBILITY
          +
OPENAPI
          +
CONTRACT TESTING
          +
SECURITY
          =
STABLE ENTERPRISE API
```

The governing principle is:

```text
An API is not an implementation detail.

Once another application depends
on it, it becomes a contract.

Do not expose persistence models.

Do not encode failures as HTTP 200.

Do not make human-readable messages
the machine contract.

Do not introduce breaking changes
because they are convenient for
the provider.

Do not version unnecessarily.

Do not allow collections, bulk
operations or payloads to grow
without bounds.

Design the contract first.

Make compatibility explicit.

Automate its verification.

And evolve APIs as long-lived
enterprise integration products.
```
