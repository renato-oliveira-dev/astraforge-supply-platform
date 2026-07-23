# API Design Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | API Design Guidelines |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the API design standards adopted by the Enterprise Order Platform.

It establishes conventions for:

- REST resource modeling
- URL structure
- HTTP methods
- status codes
- request and response models
- validation
- pagination
- filtering
- sorting
- idempotency
- concurrency control
- error responses
- security
- versioning
- compatibility
- OpenAPI documentation
- observability
- performance
- testing
- deprecation

The objective is to provide APIs that are predictable, secure, evolvable and consistent across all platform services.

---

# 2. Core Principles

APIs must be:

- resource-oriented
- consistent
- explicit
- secure by default
- backward compatible
- observable
- idempotent where required
- documented
- easy to consume
- independent from persistence models

API contracts are long-lived integration assets.

Internal implementation changes must not unintentionally change public behavior.

---

# 3. API Style

The default API style is REST over HTTP using JSON.

Alternative protocols may be adopted when justified by the use case.

Examples:

- Kafka for asynchronous event distribution
- gRPC for internal low-latency communication
- file exchange for legacy integrations
- GraphQL for specific client-driven query scenarios

The use of another protocol requires an explicit architecture decision.

---

# 4. Base Path

REST APIs must use a consistent base path.

Recommended pattern:

```text
/api/v1
```

Examples:

```text
/api/v1/orders

/api/v1/customers

/api/v1/inventory-reservations
```

Infrastructure-specific prefixes should not appear in business resource paths.

Avoid:

```text
/rest/v1

/services/orders

/backend/orders

/internal-api/orders
```

unless they represent an intentionally separate contract boundary.

---

# 5. Resource Naming

Resources must use plural nouns in lowercase kebab-case.

Examples:

```text
/orders

/customers

/order-items

/payment-authorizations

/inventory-reservations
```

Avoid verbs in primary resource names.

Incorrect:

```text
/create-order

/get-orders

/process-payment
```

---

# 6. Resource Hierarchy

Nested resources should represent a meaningful ownership relationship.

Examples:

```text
/orders/{orderId}/items

/orders/{orderId}/approvals

/customers/{customerId}/orders
```

Avoid excessive nesting.

Recommended maximum:

```text
three resource levels
```

Prefer:

```text
/order-items/{itemId}
```

instead of:

```text
/customers/{customerId}/orders/{orderId}/items/{itemId}/details
```

when the item has its own stable identity.

---

# 7. Resource Identifiers

Path parameters must use explicit identifier names.

Preferred:

```text
/orders/{orderId}

/customers/{customerId}

/orders/{orderId}/items/{itemId}
```

Avoid:

```text
/orders/{id}
```

Explicit identifiers improve documentation, tracing and generated client readability.

---

# 8. Identifier Representation

UUIDs should use the canonical textual format.

Example:

```text
11111111-1111-1111-1111-111111111111
```

Identifiers must be treated as opaque by clients.

Clients must not infer:

- creation time
- ordering
- region
- business meaning
- storage partition

unless the contract explicitly defines those semantics.

---

# 9. Business Keys

When a resource has both a technical identifier and a business key, the API must distinguish them clearly.

Example:

```json
{
  "orderId": "11111111-1111-1111-1111-111111111111",
  "orderNumber": "ORD-2026-00001234"
}
```

Use the technical identifier for stable resource addressing.

Use business keys for presentation, search and external references.

---

# 10. HTTP Methods

HTTP methods must follow their standard semantics.

| Method | Purpose |
|---|---|
| `GET` | Retrieve a resource or collection |
| `POST` | Create a resource or invoke a non-idempotent operation |
| `PUT` | Replace a complete resource representation |
| `PATCH` | Partially update a resource |
| `DELETE` | Remove or deactivate a resource |
| `HEAD` | Retrieve metadata without a response body |
| `OPTIONS` | Discover supported communication options |

Do not use `POST` for all operations by default.

---

# 11. GET Semantics

`GET` requests must:

- be safe
- not modify business state
- be idempotent
- support caching where appropriate

A `GET` endpoint must not trigger:

- order approval
- payment execution
- message publication with business side effects
- database updates unrelated to access tracking

Operational telemetry does not violate safe-method semantics.

---

# 12. POST Semantics

Use `POST` to create a resource when the server assigns its identifier.

Example:

```text
POST /api/v1/orders
```

Request:

```json
{
  "customerId": "22222222-2222-2222-2222-222222222222",
  "items": [
    {
      "productId": "33333333-3333-3333-3333-333333333333",
      "quantity": 2
    }
  ]
}
```

Successful response:

```text
201 Created
```

---

# 13. PUT Semantics

Use `PUT` when replacing the complete current representation of a resource.

Example:

```text
PUT /api/v1/orders/{orderId}/shipping-address
```

`PUT` must be idempotent.

Submitting the same request multiple times should produce the same resulting state.

---

# 14. PATCH Semantics

Use `PATCH` for partial updates.

Recommended media type:

```text
application/merge-patch+json
```

or a specific business request contract.

Example:

```text
PATCH /api/v1/orders/{orderId}
```

Request:

```json
{
  "contactEmail": "customer@example.com"
}
```

Partial update semantics must be explicit for:

- omitted fields
- null fields
- empty collections
- immutable attributes

---

# 15. JSON Patch

RFC 6902 JSON Patch may be used when generic patch operations are beneficial.

Example:

```json
[
  {
    "op": "replace",
    "path": "/contactEmail",
    "value": "customer@example.com"
  }
]
```

JSON Patch should not be adopted when a domain-specific operation contract would be clearer.

---

# 16. DELETE Semantics

`DELETE` should be idempotent.

Possible successful outcomes:

```text
204 No Content
```

or:

```text
404 Not Found
```

The project should use one consistent policy per resource type.

For business cancellation, prefer an explicit cancellation resource or command instead of deleting historical data.

Example:

```text
POST /orders/{orderId}/cancellations
```

---

# 17. Business Actions

Business actions that do not map naturally to CRUD should be modeled as subresources.

Preferred:

```text
POST /orders/{orderId}/approvals

POST /orders/{orderId}/cancellations

POST /orders/{orderId}/submissions

POST /orders/{orderId}/inventory-reservations
```

Alternative action notation may be used consistently:

```text
POST /orders/{orderId}:approve
```

The platform should prefer subresources because they provide clearer resource semantics and auditability.

---

# 18. Command Resources

Long-running or asynchronous operations may create a command resource.

Example:

```text
POST /orders/{orderId}/exports
```

Response:

```text
202 Accepted
```

```json
{
  "operationId": "44444444-4444-4444-4444-444444444444",
  "status": "PENDING",
  "statusUrl": "/api/v1/operations/44444444-4444-4444-4444-444444444444"
}
```

---

# 19. HTTP Status Codes

Use status codes according to HTTP semantics.

| Status | Meaning |
|---|---|
| `200` | Successful request with response body |
| `201` | Resource created |
| `202` | Request accepted for asynchronous processing |
| `204` | Successful request without body |
| `304` | Cached representation remains valid |
| `400` | Invalid request structure or format |
| `401` | Authentication required or invalid |
| `403` | Authenticated but unauthorized |
| `404` | Resource not found |
| `409` | Conflict with current state |
| `412` | Precondition failed |
| `415` | Unsupported media type |
| `422` | Semantically invalid business request |
| `429` | Rate limit exceeded |
| `500` | Unexpected internal failure |
| `502` | Invalid upstream gateway response |
| `503` | Temporary service unavailability |
| `504` | Upstream timeout |

---

# 20. Resource Creation Response

A successful resource creation should return:

- HTTP `201 Created`
- `Location` header
- created resource representation or identifier

Example:

```text
Location: /api/v1/orders/11111111-1111-1111-1111-111111111111
```

Response:

```json
{
  "orderId": "11111111-1111-1111-1111-111111111111",
  "status": "CREATED",
  "createdAt": "2026-07-23T14:30:00Z"
}
```

---

# 21. Empty Successful Responses

Use `204 No Content` when the client does not need a response body.

Examples:

```text
DELETE /orders/{orderId}

PUT /orders/{orderId}/shipping-address
```

Do not return meaningless bodies such as:

```json
{
  "success": true
}
```

unless the contract requires additional information.

---

# 22. Request Models

Request models must be operation-specific.

Examples:

```java
CreateOrderRequest

ApproveOrderRequest

UpdateShippingAddressRequest

SearchOrdersRequest
```

Avoid generic request models shared by unrelated operations.

Incorrect:

```java
OrderRequest
```

when create, update and approval require different fields and validation rules.

---

# 23. Response Models

Response models must reflect consumer needs.

Examples:

```java
OrderSummaryResponse

OrderDetailsResponse

CreateOrderResponse

ApprovalResultResponse
```

Do not expose persistence entities directly.

API models must not depend on:

- JPA annotations
- lazy-loading behavior
- bidirectional persistence relationships
- database column names
- internal audit structures

---

# 24. Domain Model Exposure

Domain objects should not be serialized directly as public contracts by default.

Explicit API models protect against:

- accidental contract changes
- leaking internal fields
- circular references
- domain refactoring impact
- serialization coupling

Use dedicated mappers at the API boundary.

---

# 25. JSON Naming

JSON properties use lower camel case.

Example:

```json
{
  "orderId": "11111111-1111-1111-1111-111111111111",
  "customerId": "22222222-2222-2222-2222-222222222222",
  "createdAt": "2026-07-23T14:30:00Z"
}
```

Avoid mixing:

```text
order_id

OrderId

ORDER_ID
```

in JSON contracts.

---

# 26. Date and Time

Use ISO 8601 representations.

Instant:

```text
2026-07-23T14:30:00Z
```

Offset date-time:

```text
2026-07-23T11:30:00-03:00
```

Local date:

```text
2026-07-23
```

Local time:

```text
14:30:00
```

Prefer UTC timestamps for system events.

Use local dates only for domain concepts without timezone semantics.

---

# 27. Monetary Values

Monetary values must not use floating-point numbers internally.

Recommended JSON representation:

```json
{
  "amount": 1250.75,
  "currency": "BRL"
}
```

For integrations requiring maximum decimal precision preservation, a string may be used:

```json
{
  "amount": "1250.75",
  "currency": "BRL"
}
```

The selected representation must remain consistent across the API.

---

# 28. Decimal Precision

The contract must define:

- maximum precision
- scale
- rounding mode
- accepted range

Example:

```text
precision: 12

scale: 2
```

Do not rely only on database column definitions to communicate API limits.

---

# 29. Enumerations

Enums must be represented using stable external values.

Example:

```json
{
  "status": "PENDING_APPROVAL"
}
```

Do not expose ordinal numbers.

Incorrect:

```json
{
  "status": 2
}
```

Enum removal or renaming is a breaking contract change.

---

# 30. Boolean Values

Use JSON booleans.

Correct:

```json
{
  "active": true
}
```

Avoid:

```json
{
  "active": "Y"
}
```

unless required by a legacy contract.

Boolean property names should read naturally.

Examples:

```text
active

cancelled

requiresApproval

hasPendingItems
```

---

# 31. Nullability

Nullability must be intentional and documented.

Distinguish:

- required field
- optional field
- nullable field
- omitted field

Prefer omission for unavailable optional data when it does not create ambiguity.

Do not use `null` indiscriminately.

---

# 32. Empty Collections

Collections should generally be returned as empty arrays instead of null.

Preferred:

```json
{
  "items": []
}
```

Avoid:

```json
{
  "items": null
}
```

This simplifies client handling and maintains schema consistency.

---

# 33. Optional Request Fields

Optional fields should have explicit behavior.

The contract must define whether an omitted field means:

- retain current value
- use default
- clear value
- derive value
- no filtering

For partial updates, omitted and explicit null must not be treated as equivalent unless documented.

---

# 34. Validation

Requests must be validated at the interface boundary.

Validation categories:

- structural validation
- format validation
- range validation
- cross-field validation
- business validation

Bean Validation is preferred for structural constraints.

Business rules belong in the domain or application layer.

---

# 35. Structural Validation

Examples:

```java
@NotNull

@NotBlank

@Size

@Min

@Max

@Positive

@Email

@Pattern
```

Validation messages should be clear and stable enough for human consumption.

Machine consumers should rely on stable error codes.

---

# 36. Cross-Field Validation

Cross-field validation should use a dedicated validator when rules involve multiple fields.

Example:

```text
createdFrom must be before or equal to createdTo
```

Avoid placing complex validation logic in controllers.

---

# 37. Business Validation

Business validation may include:

- current state compatibility
- customer eligibility
- order ownership
- approval authority
- inventory availability
- credit limit

These validations must not be implemented only through request annotations.

---

# 38. Validation Error Contract

Validation errors must use a consistent Problem Details response.

Example:

```json
{
  "type": "https://enterprise.example/problems/request-validation",
  "title": "Request validation failed",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "instance": "/api/v1/orders",
  "code": "REQUEST_VALIDATION_FAILED",
  "traceId": "f64c7f8ac1304c42",
  "errors": [
    {
      "field": "customerId",
      "code": "NotNull",
      "message": "customerId must not be null"
    }
  ]
}
```

---

# 39. Malformed Requests

Malformed JSON must return:

```text
400 Bad Request
```

Recommended error code:

```text
MALFORMED_REQUEST_BODY
```

The response must not expose:

- Jackson class names
- parser stack traces
- Java package names
- internal implementation details

---

# 40. Pagination

Collection endpoints returning potentially large datasets must support pagination.

Recommended parameters:

```text
page

size

sort
```

Example:

```text
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

Default page size and maximum page size must be documented.

---

# 41. Page Indexing

The platform adopts zero-based page indexes.

Example:

```text
page=0
```

The convention must remain consistent across all APIs.

Do not mix zero-based and one-based pagination across services.

---

# 42. Page Response

Recommended response structure:

```json
{
  "content": [
    {
      "orderId": "11111111-1111-1111-1111-111111111111",
      "status": "CREATED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 125,
  "totalPages": 7,
  "first": true,
  "last": false
}
```

Internal Spring `Page` objects must not be exposed directly.

---

# 43. Cursor Pagination

Cursor-based pagination should be preferred for:

- very large datasets
- high write rates
- event streams
- stable forward navigation
- APIs where offset cost becomes excessive

Example:

```text
GET /api/v1/orders?limit=50&after=eyJjcmVhdGVkQXQiOi...
```

Response:

```json
{
  "content": [],
  "nextCursor": "eyJjcmVhdGVkQXQiOi...",
  "hasNext": true
}
```

Cursors must be opaque to clients.

---

# 44. Pagination Limits

Every paginated endpoint must define:

- default size
- maximum size
- minimum size
- supported sort fields
- stable ordering

Example:

```text
default size: 20

maximum size: 200
```

Requests above the maximum should be rejected or capped consistently.

Rejecting with a validation error is preferred because silent capping can surprise clients.

---

# 45. Stable Sorting

Paginated queries must use deterministic ordering.

When the selected sort field is not unique, add a stable secondary sort.

Example:

```text
createdAt DESC, orderId DESC
```

This prevents duplicated or missing records between pages.

---

# 46. Sorting

Sorting syntax:

```text
sort=<field>,<direction>
```

Examples:

```text
sort=createdAt,desc

sort=orderNumber,asc
```

Multiple sort parameters may be supported:

```text
sort=status,asc&sort=createdAt,desc
```

Only allow explicitly supported fields.

---

# 47. Sort Field Mapping

External sort names must be mapped to approved internal query paths.

Do not pass client-provided field names directly into persistence queries.

Example mapping:

```text
createdAt → order.createdAt

customerName → customerProjection.displayName
```

This prevents:

- invalid property access
- implementation leakage
- injection risks
- unstable contracts

---

# 48. Filtering

Filters use lower camel case query parameters.

Examples:

```text
customerId

status

createdFrom

createdTo

orderType

minimumAmount

maximumAmount
```

Example:

```text
GET /api/v1/orders?customerId=...&status=APPROVED
```

---

# 49. Multiple Filter Values

Multiple values may use repeated parameters.

Example:

```text
status=CREATED&status=PENDING_APPROVAL
```

This is preferred over comma-separated values when framework and documentation support are clearer.

The selected convention must be consistent.

---

# 50. Date Range Filters

Date range fields must use explicit names.

Examples:

```text
createdFrom

createdTo

approvedFrom

approvedTo
```

The contract must define whether boundaries are inclusive or exclusive.

Recommended:

```text
from inclusive

to exclusive
```

for timestamp intervals.

---

# 51. Search Endpoints

Simple searches should use collection filters.

Example:

```text
GET /orders?customerId=...&status=CREATED
```

Complex searches with large structured criteria may use:

```text
POST /orders/search
```

This exception is justified when:

- criteria exceed practical URL limits
- nested predicates are required
- request bodies improve clarity
- caching is not essential

Search endpoints must remain read-only despite using `POST`.

---

# 52. Free-Text Search

Use an explicit query parameter.

Example:

```text
query

searchTerm
```

Preferred:

```text
query
```

The contract must define searchable fields and matching behavior.

Avoid undocumented global search semantics.

---

# 53. Field Selection

Sparse fieldsets may be supported for high-volume APIs.

Example:

```text
GET /orders/{orderId}?fields=orderId,status,total
```

This should be adopted only when performance benefits justify added contract complexity.

Do not expose arbitrary reflective field access.

---

# 54. Resource Expansion

Related resources may be optionally expanded.

Example:

```text
GET /orders/{orderId}?expand=items,customer
```

Expansion must be bounded and explicitly supported.

Avoid arbitrary nested expansion that can create uncontrolled query cost.

---

# 55. Request Size Limits

The platform must define limits for:

- request body size
- collection size
- string length
- attachment size
- bulk operation size
- query parameter count

Limits must be enforced before expensive processing.

---

# 56. Bulk Operations

Bulk APIs must define:

- maximum item count
- atomic or partial semantics
- ordering guarantees
- duplicate behavior
- idempotency behavior
- per-item failure response
- timeout behavior

Example:

```text
POST /api/v1/orders/approvals
```

---

# 57. Atomic Bulk Operations

Atomic bulk operations succeed or fail as one transaction.

Example response on failure:

```text
409 Conflict
```

No item may be persisted if any critical validation fails.

Atomicity must be explicitly documented.

---

# 58. Partial Bulk Operations

Partial processing must return an explicit result.

Example:

```json
{
  "requested": 3,
  "succeeded": 2,
  "failed": 1,
  "results": [
    {
      "orderId": "11111111-1111-1111-1111-111111111111",
      "outcome": "APPROVED"
    },
    {
      "orderId": "22222222-2222-2222-2222-222222222222",
      "outcome": "REJECTED",
      "code": "INVALID_ORDER_TRANSITION",
      "message": "The order cannot be approved from its current status."
    }
  ]
}
```

Do not return HTTP `200` with an ambiguous generic message.

---

# 59. Idempotency

Idempotency protection is required for operations where duplicate processing would create harmful side effects.

Examples:

- order creation
- payment authorization
- refund creation
- external command submission
- bulk approval
- asynchronous job triggering

---

# 60. Idempotency Header

Use:

```text
Idempotency-Key
```

Example:

```text
Idempotency-Key: 9f6d6af0-7c48-4f08-8a81-6fb343d7de2d
```

The key should be unique within the documented scope.

---

# 61. Idempotency Scope

The contract must define key scope.

Possible scopes:

```text
client + endpoint

customer + operation

authenticated principal + endpoint

global operation key
```

A key must not accidentally conflict across unrelated operations.

---

# 62. Idempotency Replay

When the same key and equivalent payload are submitted again:

- do not repeat the side effect
- return the original result
- preserve the original status where practical
- expose replay metadata when useful

Optional response header:

```text
Idempotency-Replayed: true
```

---

# 63. Idempotency Conflict

When the same key is reused with a different payload:

```text
409 Conflict
```

Recommended error code:

```text
IDEMPOTENCY_KEY_CONFLICT
```

The new request must not be processed.

---

# 64. Request Fingerprinting

Idempotency implementations may use a canonical request fingerprint.

The fingerprint should include relevant fields and exclude unstable metadata.

Do not use raw JSON string comparison because property order and formatting may differ.

---

# 65. Optimistic Concurrency

APIs that update mutable resources should support optimistic concurrency where lost updates are possible.

Recommended mechanisms:

- ETag
- `If-Match`
- explicit version field

---

# 66. ETag

Example response:

```text
ETag: "7"
```

Update request:

```text
If-Match: "7"
```

When the version is stale:

```text
412 Precondition Failed
```

Recommended error code:

```text
RESOURCE_VERSION_CONFLICT
```

---

# 67. Explicit Version Field

An alternative request contract may include:

```json
{
  "version": 7,
  "contactEmail": "customer@example.com"
}
```

Stale version conflicts should return:

```text
409 Conflict
```

Use one convention consistently for each resource.

---

# 68. Conditional GET

Read-heavy APIs may support:

- ETag
- `If-None-Match`
- `Last-Modified`
- `If-Modified-Since`

Unchanged resources may return:

```text
304 Not Modified
```

Conditional caching must not expose outdated security-sensitive data.

---

# 69. Caching Headers

Use HTTP caching intentionally.

Possible headers:

```text
Cache-Control

ETag

Last-Modified

Expires
```

Examples:

```text
Cache-Control: no-store
```

for sensitive responses.

```text
Cache-Control: private, max-age=60
```

for safe user-specific responses.

---

# 70. Problem Details

Errors must follow Problem Details semantics.

Required fields:

```text
type

title

status

detail

instance

code

traceId
```

Optional fields:

```text
timestamp

errors

retryable

dependency
```

---

# 71. Stable Error Codes

Error codes must use uppercase snake case.

Examples:

```text
ORDER_NOT_FOUND

INVALID_ORDER_TRANSITION

REQUEST_VALIDATION_FAILED

DEPENDENCY_UNAVAILABLE

IDEMPOTENCY_KEY_CONFLICT

RESOURCE_VERSION_CONFLICT
```

Clients should use `code`, not human-readable `detail`, for programmatic decisions.

---

# 72. Safe Error Details

Public error messages must not expose:

- stack traces
- SQL
- constraint names
- class names
- package names
- internal service URLs
- access tokens
- raw upstream payloads
- user-sensitive data

Internal logs should preserve the diagnostic cause.

---

# 73. Dependency Errors

The API should not expose external provider implementation details.

Avoid:

```json
{
  "code": "RESTCLIENT_RESPONSE_EXCEPTION"
}
```

Prefer:

```json
{
  "code": "INVENTORY_SERVICE_UNAVAILABLE",
  "status": 503
}
```

The error should reflect the platform contract.

---

# 74. Authentication

Protected APIs must use standard authentication mechanisms.

Recommended:

```text
OAuth 2.0

OpenID Connect

JWT bearer tokens
```

Authentication details must not be passed through query parameters.

Incorrect:

```text
GET /orders?token=...
```

---

# 75. Authorization

Authorization must be enforced server-side.

Authorization may consider:

- scopes
- roles
- permissions
- customer ownership
- tenant
- business unit
- order status
- approval authority

Client-side checks are not security controls.

---

# 76. OAuth Scopes

Scopes should use capability-oriented names.

Examples:

```text
orders:read

orders:create

orders:approve

orders:cancel
```

Avoid broad scopes:

```text
admin

full-access
```

unless explicitly justified.

---

# 77. Object-Level Authorization

The API must validate whether the authenticated principal may access the specific resource.

Possessing:

```text
orders:read
```

does not automatically grant access to every order.

Validate:

- ownership
- tenant
- region
- business unit
- assigned responsibility

---

# 78. Multi-Tenancy

Tenant context must come from a trusted source.

Possible sources:

- validated JWT claim
- gateway-injected signed header
- authenticated service identity

Do not trust a client-provided tenant identifier without authorization validation.

---

# 79. Security Headers

API responses should use appropriate security headers.

Examples:

```text
X-Content-Type-Options: nosniff

Cache-Control: no-store
```

Browser-facing APIs may additionally require:

```text
Content-Security-Policy

Strict-Transport-Security

Referrer-Policy
```

---

# 80. CORS

CORS must use an explicit allowlist.

Do not use:

```text
Access-Control-Allow-Origin: *
```

for credentialed or sensitive APIs.

Allowed:

- origins
- methods
- headers
- credentials
- cache duration

must be configured per environment.

---

# 81. CSRF

Stateless bearer-token APIs generally do not require CSRF protection when authentication is not cookie-based.

Cookie-authenticated browser APIs must evaluate and enable CSRF protection.

Disabling CSRF requires an explicit security rationale.

---

# 82. Rate Limiting

Rate limits should protect:

- authentication endpoints
- expensive searches
- bulk operations
- public APIs
- external-facing integrations

When exceeded:

```text
429 Too Many Requests
```

Useful headers:

```text
Retry-After

RateLimit-Limit

RateLimit-Remaining

RateLimit-Reset
```

---

# 83. Rate Limit Identity

Rate limits may be calculated by:

- authenticated client
- principal
- tenant
- API key
- IP address
- operation

IP-only rate limiting is insufficient for many authenticated APIs.

---

# 84. Correlation Headers

Recommended inbound headers:

```text
X-Correlation-Id

X-Request-Id
```

The service may generate missing values.

These identifiers must be:

- validated
- sanitized
- bounded in length
- propagated to downstream calls
- included in logs
- returned when useful

---

# 85. Trace Propagation

Distributed tracing should follow W3C Trace Context.

Headers:

```text
traceparent

tracestate
```

Do not invent a proprietary trace format when standard propagation is available.

---

# 86. User-Agent

External clients should provide a meaningful `User-Agent`.

Example:

```text
enterprise-order-portal/2.4.1
```

Service-to-service clients may include:

```text
inventory-service/1.8.0
```

User-Agent data must be sanitized before logging.

---

# 87. Content Type

JSON requests must use:

```text
Content-Type: application/json
```

JSON responses should use:

```text
Content-Type: application/json
```

Problem Details responses should use:

```text
application/problem+json
```

---

# 88. Character Encoding

UTF-8 is the default encoding.

APIs must preserve valid Unicode text.

Do not perform unnecessary HTML escaping of business data at persistence or service boundaries.

Output encoding belongs to the rendering context.

---

# 89. Compression

HTTP response compression may be enabled for sufficiently large payloads.

Do not compress:

- already compressed content
- very small responses
- sensitive responses in contexts where compression-related attacks are relevant

Compression policy should be configured centrally.

---

# 90. Versioning

The default versioning strategy is URI versioning.

Example:

```text
/api/v1/orders
```

Major versions represent incompatible contract changes.

Minor compatible additions do not require a new path version.

---

# 91. Compatible Changes

Usually backward-compatible:

- adding an optional response field
- adding an optional request field with a safe default
- adding a new endpoint
- adding a new optional query parameter
- adding a new error extension field
- increasing a maximum limit with no semantic change

Compatibility must still be tested.

---

# 92. Breaking Changes

Breaking changes include:

- removing a field
- renaming a field
- changing field type
- making an optional field required
- changing enum values
- changing status code semantics
- changing resource paths
- changing date formats
- changing pagination conventions
- changing error codes
- changing nullability
- reducing accepted limits

Breaking changes require versioning or a managed migration.

---

# 93. Enum Evolution

Adding a new enum response value may break clients that assume exhaustive values.

Therefore:

- document enum extensibility
- encourage tolerant readers
- assess consumers before adding values
- version when strict compatibility is required

Request enums may be extended more safely when existing clients do not send the new value.

---

# 94. Tolerant Reader

Clients should ignore unknown response fields.

Servers should reject unknown request fields only when strict validation is required for safety.

The platform should define one policy for request deserialization.

Strict unknown-field rejection improves contract correctness but can reduce forward compatibility.

The selected policy must be documented.

---

# 95. Deprecation

Deprecated APIs must communicate:

- replacement endpoint
- deprecation date
- planned removal date
- migration guidance
- affected clients

Useful response headers:

```text
Deprecation: true

Sunset: Sat, 31 Jan 2027 23:59:59 GMT

Link: </api/v2/orders>; rel="successor-version"
```

---

# 96. Deprecation Period

The deprecation period must consider:

- number of consumers
- deployment independence
- business criticality
- contractual obligations
- migration complexity

Do not remove a public API immediately after introducing its replacement.

---

# 97. Consumer Inventory

Externally consumed APIs should maintain a consumer inventory.

Record:

- consumer name
- owner
- contact
- version
- authentication method
- traffic volume
- criticality
- migration status

This supports safe deprecation and incident communication.

---

# 98. OpenAPI

Every REST API must provide an OpenAPI specification.

The specification should describe:

- paths
- methods
- operation IDs
- parameters
- schemas
- validation constraints
- security
- response codes
- Problem Details
- examples
- deprecations

---

# 99. OpenAPI Source

The project must define whether OpenAPI is:

- design-first
- code-first
- hybrid

The Enterprise Order Platform adopts a hybrid approach:

- contract reviewed as an architectural artifact
- runtime generated documentation validated against expectations
- compatibility checks automated in CI

---

# 100. Operation IDs

Operation IDs must be unique and stable.

Examples:

```text
createOrder

getOrderById

searchOrders

approveOrder

cancelOrder
```

Changing an operation ID may break generated clients even when the URL remains unchanged.

---

# 101. OpenAPI Tags

Tags should represent business capabilities.

Examples:

```text
Orders

Order Approvals

Inventory Reservations

Payments
```

Avoid technical tags:

```text
Controller

REST

Endpoints
```

---

# 102. Schema Names

OpenAPI schemas should use meaningful contract names.

Examples:

```text
CreateOrderRequest

OrderDetailsResponse

OrderSummaryPageResponse

ProblemDetailResponse
```

Avoid generated or implementation-oriented names.

---

# 103. API Examples

OpenAPI should include realistic, sanitized examples.

Examples must:

- use valid deterministic identifiers
- avoid production data
- demonstrate required fields
- reflect actual enum values
- match runtime behavior

---

# 104. Documentation Descriptions

Descriptions should explain business meaning.

Weak:

```text
The status field.
```

Better:

```text
Current order lifecycle status. Orders in CANCELLED status cannot be approved or modified.
```

---

# 105. OpenAPI Error Documentation

Each operation should document relevant failures.

Example:

```text
400 — malformed or structurally invalid request

401 — authentication required

403 — insufficient permission

404 — order not found

409 — invalid current state or idempotency conflict

422 — business rule rejection

503 — required dependency unavailable
```

Do not document only success responses.

---

# 106. API Compatibility Validation

CI should detect incompatible API changes.

Validation should compare:

- current branch specification
- latest released specification

The pipeline should fail or require explicit approval for breaking changes.

---

# 107. Contract Tests

Contract tests should verify:

- request schema
- response schema
- field names
- enum representation
- status codes
- headers
- Problem Details
- nullability
- authentication expectations

Generated OpenAPI alone does not prove runtime compatibility.

---

# 108. Controller Responsibilities

Controllers should:

- receive requests
- validate structural input
- map requests to commands or queries
- invoke application use cases
- map results to responses
- return HTTP semantics

Controllers must not contain:

- business rules
- persistence queries
- transaction orchestration
- external client calls
- message publication logic

---

# 109. Mapper Responsibilities

API mappers should translate:

```text
Request → Command

Application Result → Response
```

Mappers should not:

- access repositories
- call integrations
- make business decisions
- start transactions

---

# 110. Application Boundary

The controller must depend on an application port.

Preferred:

```text
OrderController → CreateOrderUseCase
```

Avoid:

```text
OrderController → JpaOrderRepository
```

or:

```text
OrderController → RestInventoryClient
```

---

# 111. Transactions

Transaction boundaries belong in the application layer.

Controllers must not define transaction semantics.

A request may complete before asynchronous side effects finish when the contract explicitly uses `202 Accepted`.

---

# 112. Asynchronous Processing

Use `202 Accepted` when work is queued or continues asynchronously.

The response should provide:

- operation identifier
- current status
- status resource
- estimated retry guidance when applicable

Do not return `200 OK` implying completion when processing is still pending.

---

# 113. Operation Status Resource

Example:

```text
GET /api/v1/operations/{operationId}
```

Response:

```json
{
  "operationId": "44444444-4444-4444-4444-444444444444",
  "type": "ORDER_EXPORT",
  "status": "COMPLETED",
  "createdAt": "2026-07-23T14:30:00Z",
  "completedAt": "2026-07-23T14:30:05Z",
  "resultUrl": "/api/v1/order-exports/55555555-5555-5555-5555-555555555555"
}
```

---

# 114. Webhooks

Webhook APIs must define:

- event types
- signature mechanism
- retry behavior
- timeout
- delivery identifiers
- ordering guarantees
- duplicate behavior
- replay support
- consumer response expectations

Webhook consumers must treat deliveries as potentially duplicated.

---

# 115. Webhook Security

Webhook requests should include a cryptographic signature.

Example headers:

```text
Webhook-Id

Webhook-Timestamp

Webhook-Signature
```

Signatures must cover:

- body
- timestamp
- delivery identifier

Consumers should reject stale requests outside the accepted replay window.

---

# 116. File Uploads

File uploads should use:

```text
multipart/form-data
```

or a dedicated object-storage upload flow.

The API must define:

- maximum size
- supported content types
- checksum
- malware scanning
- filename handling
- retention
- asynchronous processing behavior

Never trust the client-provided filename as a storage path.

---

# 117. Large File Uploads

Large files should generally use pre-signed object-storage URLs.

Recommended flow:

1. request upload authorization
2. upload directly to object storage
3. notify the platform of completion
4. process asynchronously

This prevents application servers from becoming file-transfer bottlenecks.

---

# 118. File Downloads

File download responses should include:

```text
Content-Type

Content-Length

Content-Disposition

ETag
```

Use safe sanitized filenames.

Authorization must be revalidated at download time.

---

# 119. Performance

API design must avoid unnecessary payload and query cost.

Consider:

- pagination
- projections
- batching
- caching
- compression
- cursor navigation
- asynchronous processing
- bounded expansion
- request limits

Do not expose endpoints that load unbounded collections.

---

# 120. N+1 Query Prevention

Response design and persistence implementation must avoid N+1 query patterns.

Use:

- projections
- batch loading
- fetch plans
- dedicated query models

Do not solve N+1 problems by exposing a larger unbounded domain graph.

---

# 121. Timeouts

Every outbound dependency call must have explicit timeouts.

The API should also define request-processing timeout budgets.

Long work should become asynchronous instead of holding HTTP connections indefinitely.

---

# 122. Retry Guidance

When a temporary failure is safe to retry, responses may include:

```text
Retry-After
```

Examples:

```text
429 Too Many Requests

503 Service Unavailable
```

Do not advise retries for permanent business failures.

---

# 123. Partial Responses

Partial responses are allowed only when explicitly defined.

The response must indicate:

- completed data
- unavailable sections
- error classification
- whether retrying may help

Do not silently omit failed data.

---

# 124. Health Endpoints

Health and operational endpoints are not business APIs.

Recommended paths:

```text
/actuator/health

/actuator/info
```

Exposure must be restricted according to environment and security policy.

Business resource APIs must not depend on health endpoint contracts.

---

# 125. API Metrics

Recommended metrics:

```text
http.server.requests

api.request.failures

api.validation.failures

api.rate.limit.rejections

api.idempotency.replays
```

Useful tags:

```text
method

route

status

outcome

exception_type
```

Do not tag metrics with raw resource identifiers.

---

# 126. Structured API Logs

Request completion logs should include:

```text
event

operation

method

route

status

elapsedMs

traceId

correlationId

outcome
```

Do not log full request or response bodies by default.

---

# 127. Audit

Security or business-sensitive API operations may generate immutable audit records.

Examples:

- order approval
- cancellation
- permission change
- refund
- manual override

Audit records are separate from operational logs.

---

# 128. API Testing

API tests should cover:

- successful requests
- structural validation
- business rejection
- authentication
- authorization
- not found
- conflict
- idempotent replay
- idempotency conflict
- concurrency conflict
- dependency failure
- serialization
- Problem Details
- OpenAPI compatibility

---

# 129. Controller Tests

Controller tests should use `@WebMvcTest` or an equivalent slice.

They should mock application use cases, not repositories.

Example:

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
}
```

---

# 130. Integration Tests

API integration tests should validate:

- complete Spring configuration
- security filters
- persistence
- Flyway migrations
- transaction behavior
- serialization
- exception translation

Use Testcontainers for production-compatible dependencies.

---

# 131. Idempotency Tests

Required scenarios:

- first request processed
- identical replay returns original result
- same key with different payload rejected
- concurrent duplicate requests produce one side effect
- expired key follows retention policy

---

# 132. Pagination Tests

Pagination tests should validate:

- default page
- maximum size
- unsupported sort field
- deterministic ordering
- empty page
- last page
- stable filtering
- cursor invalidation behavior when applicable

---

# 133. Security Tests

Required scenarios:

- missing token
- invalid token
- expired token
- insufficient scope
- wrong tenant
- wrong ownership
- valid authorized access

Do not disable security globally for API tests.

---

# 134. Error Contract Tests

Tests must verify:

- HTTP status
- stable error code
- problem type
- title
- safe detail
- trace identifier
- validation errors
- absence of sensitive information

---

# 135. OpenAPI Tests

Validate:

- expected paths
- stable operation IDs
- schema constraints
- security requirements
- error responses
- deprecation metadata
- example validity

The specification must remain synchronized with runtime behavior.

---

# 136. API Review Checklist

Before approving a new endpoint, verify:

- Is the resource name business-oriented?
- Is the HTTP method semantically correct?
- Is the status code correct?
- Is the request model operation-specific?
- Is the response independent from persistence?
- Are validation rules documented?
- Is pagination required?
- Are sorting fields controlled?
- Is idempotency required?
- Is concurrency protection required?
- Are authorization rules explicit?
- Are Problem Details documented?
- Is sensitive data protected?
- Is OpenAPI complete?
- Is backward compatibility preserved?
- Are observability fields available?
- Are limits defined?
- Are tests sufficient?

---

# 137. Anti-Patterns

The following practices are prohibited:

- exposing JPA entities directly
- using verbs for all URLs
- returning HTTP 200 for every outcome
- returning stack traces to clients
- using generic request and response DTOs
- unbounded collection endpoints
- client-controlled persistence sort paths
- non-idempotent retries without protection
- passing authentication tokens in query parameters
- trusting tenant identifiers without authorization
- changing error codes without compatibility analysis
- using enum ordinals
- modifying response semantics without versioning
- undocumented partial success
- returning null collections
- embedding business rules in controllers
- calling repositories directly from controllers
- logging full sensitive payloads
- removing APIs without a deprecation period

---

# 138. Architecture Rules

REST APIs must:

- model business resources
- use standard HTTP semantics
- preserve backward compatibility
- use stable error contracts
- enforce security server-side
- protect against duplicate side effects
- separate API and persistence models
- remain observable
- document limits and behavior
- validate contracts automatically
- support safe evolution

---

# 139. Decision Summary

The project adopts:

- REST over HTTP with JSON
- URI-based major versioning
- plural kebab-case resources
- explicit resource identifiers
- standard HTTP method semantics
- Problem Details error responses
- stable machine-readable error codes
- operation-specific request and response models
- zero-based pagination
- deterministic sorting
- controlled filtering
- idempotency keys for sensitive commands
- optimistic concurrency where required
- OAuth 2.0 and scope-based authorization
- object-level access control
- W3C trace propagation
- OpenAPI documentation
- compatibility validation in CI
- explicit deprecation and sunset policies
