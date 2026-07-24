# ADR-036: Adopt API Design, REST Contract and Compatibility Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-036 |
| Title | Adopt API Design, REST Contract and Compatibility Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | REST, HTTP, DTO, OpenAPI, Validation, Pagination, Compatibility |
| Related Work Items | REST APIs, OpenAPI, DTOs, Validation, Pagination, Sorting, Error Handling |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform exposes APIs consumed by:

- web applications
- mobile applications
- internal services
- batch jobs
- integration services
- partner systems
- administrative applications

An API is a contract.

Once consumers depend on that contract, implementation changes may become compatibility changes.

Therefore:

```text
Java Refactoring
      !=
API Contract Change
```

and:

```text
Database Refactoring
      !=
API Contract Change
```

The external API contract must remain intentionally isolated from internal implementation details.

---

# 2. Problem Statement

The platform requires standards defining:

- REST resource design
- URI conventions
- HTTP methods
- HTTP status codes
- request DTOs
- response DTOs
- Bean Validation
- error contracts
- pagination
- sorting
- filtering
- search
- PATCH
- PUT
- DELETE
- idempotency
- bulk operations
- OpenAPI
- compatibility
- versioning
- deprecation
- correlation
- security
- performance
- service-to-service APIs

---

# 3. Decision Drivers

Primary drivers are:

1. predictable contracts
2. backward compatibility
3. consumer independence
4. HTTP correctness
5. security
6. maintainability
7. discoverability
8. performance
9. evolvability
10. observability
11. testability
12. governance

---

# 4. Decision

The Enterprise Order Platform adopts resource-oriented REST APIs with explicit transport contracts.

The canonical model is:

```text
CLIENT
   |
   v
HTTP CONTRACT
   |
   +--> URI
   +--> Method
   +--> Headers
   +--> Request DTO
   +--> Validation
   +--> Status
   +--> Response DTO
   +--> Error Contract
   |
   v
APPLICATION
   |
   v
DOMAIN
   |
   v
PERSISTENCE
```

The API contract must not expose persistence implementation accidentally.

---

# 5. Fundamental Principle

The primary API rule is:

```text
Design contracts for consumers.

Do not expose implementation details
merely because they already exist.
```

---

# 6. Resource-Oriented URI Design

URIs should normally represent resources.

Prefer:

```text
/orders
/orders/{orderId}
/customers/{customerId}
/products/{productId}
```

instead of:

```text
/getOrders
/createOrder
/deleteCustomer
```

---

# 7. URI Nouns

Resource paths should primarily use nouns.

HTTP methods communicate the operation.

---

# 8. Collection Resources

Plural nouns are preferred for collections.

Example:

```text
/orders
/customers
/products
```

---

# 9. Resource Identifier

A specific resource should use:

```text
/orders/{orderId}
```

---

# 10. Nested Resources

Nested resources may represent genuine containment or contextual relationships.

Example:

```text
/customers/{customerId}/orders
```

---

# 11. Excessive Nesting

Avoid:

```text
/companies/{companyId}/customers/{customerId}/orders/{orderId}/items/{itemId}
```

unless every level is required to identify or authorize the resource.

---

# 12. Stable URI

URI structure is part of the public contract.

Renaming paths requires compatibility analysis.

---

# 13. Internal Database IDs

The URI must not reveal database implementation unnecessarily.

---

# 14. UUID

UUIDs are appropriate externally when they are the service's stable public identifiers.

---

# 15. HTTP GET

`GET` retrieves representations and must be safe.

---

# 16. GET Side Effects

A GET must not perform business mutations such as:

```text
Approve Order

Cancel Order

Create Customer
```

---

# 17. Incidental Effects

Operational effects such as:

- access logging
- metrics
- cache population

do not violate GET safety.

---

# 18. GET Idempotency

GET is naturally idempotent from the client's resource-state perspective.

---

# 19. HTTP POST

`POST` is appropriate for:

- resource creation
- commands
- non-idempotent operations
- complex searches when GET is unsuitable

---

# 20. Resource Creation

Example:

```text
POST /orders
```

---

# 21. Command Endpoint

Domain commands may use:

```text
POST /orders/{orderId}/approval
```

or another explicit command resource when the operation does not map naturally to CRUD.

---

# 22. Avoid RPC Explosion

Do not create arbitrary action endpoints such as:

```text
/orders/{id}/doSomething
/orders/{id}/processThing
/orders/{id}/executeAction
```

without clear domain semantics.

---

# 23. HTTP PUT

`PUT` represents complete replacement of the target resource representation where supported.

---

# 24. PUT Idempotency

Repeated identical PUT requests should produce the same intended resource state.

---

# 25. Partial Update

Do not call an operation PUT if it semantically performs an undocumented partial update.

---

# 26. HTTP PATCH

`PATCH` is appropriate for explicit partial updates.

---

# 27. PATCH Semantics

The patch format must be defined.

Possible strategies include:

```text
JSON Merge Patch

JSON Patch

Dedicated Partial-Update DTO
```

---

# 28. Null Semantics

For partial updates, the contract must distinguish where necessary:

```text
Field absent
```

from:

```text
Field explicitly null
```

---

# 29. HTTP DELETE

`DELETE` removes or logically deletes a resource according to documented domain semantics.

---

# 30. DELETE Idempotency

Repeated DELETE requests should not recreate state or cause additional business mutation.

---

# 31. Logical Deletion

If DELETE performs logical deletion, this must remain transparent in the API contract unless internal deletion state is intentionally exposed.

---

# 32. HTTP Status Codes

Status codes must communicate actual HTTP/application outcomes.

---

# 33. 200 OK

Use `200 OK` for successful requests returning a representation/result.

---

# 34. 201 Created

Use:

```text
201 Created
```

when a new resource is successfully created.

---

# 35. Location

For resource creation, return:

```text
Location: /orders/{id}
```

where practical.

---

# 36. 202 Accepted

Use:

```text
202 Accepted
```

when processing has been accepted but is intentionally asynchronous and incomplete.

---

# 37. 204 No Content

Use:

```text
204 No Content
```

for successful operations with no response representation.

---

# 38. 400 Bad Request

Use `400` for malformed or invalid request syntax/structure where no more specific contract status applies.

---

# 39. 401 Unauthorized

`401` means authentication is missing or invalid.

---

# 40. 403 Forbidden

`403` means the caller is authenticated but is not permitted to perform the operation.

---

# 41. 404 Not Found

Use `404` when the requested resource cannot be found or when resource hiding is intentionally part of the security model.

---

# 42. 409 Conflict

Use `409` for conflicts with current resource state.

Examples:

```text
Duplicate resource

Invalid state transition

Optimistic locking conflict
```

when appropriate.

---

# 43. 422 Unprocessable Content

`422` may be used for semantically invalid requests when the platform/API family deliberately adopts that convention.

---

# 44. Validation Consistency

An API family must not arbitrarily alternate between:

```text
400

and

422
```

for equivalent validation failures.

---

# 45. 429 Too Many Requests

Rate-limit rejection uses:

```text
429 Too Many Requests
```

---

# 46. 500 Internal Server Error

`500` represents an unexpected server failure.

---

# 47. 502 Bad Gateway

`502` may represent invalid failure from an upstream dependency/gateway.

---

# 48. 503 Service Unavailable

`503` indicates temporary inability to process due to service/capacity/dependency availability where appropriate.

---

# 49. 504 Gateway Timeout

`504` represents upstream timeout at a gateway/proxy boundary.

---

# 50. Do Not Return 200 for Errors

This is prohibited:

```json
HTTP 200

{
  "success": false,
  "error": "Customer not found"
}
```

for ordinary REST error semantics.

---

# 51. Transport Status

HTTP status communicates transport/application outcome.

The response body provides structured details.

---

# 52. Request DTO

External request contracts require dedicated DTOs.

---

# 53. Response DTO

External response contracts require dedicated DTOs.

---

# 54. Entity Exposure

JPA entities must not be exposed directly as API contracts.

---

# 55. Why

Entity exposure couples consumers to:

- persistence structure
- relationships
- lazy loading
- internal fields
- schema evolution

---

# 56. DTO Independence

DTOs may evolve independently from persistence entities.

---

# 57. Records

Java records are preferred for immutable transport DTOs where suitable.

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

# 58. Mutable DTO

Mutable DTOs may still be used where framework/serialization requirements justify them.

---

# 59. Internal Fields

Do not expose internal fields merely because they exist.

Examples:

```text
databaseVersion

internalStatusCode

deletedFlag

technicalSequence
```

unless part of the deliberate contract.

---

# 60. Sensitive Fields

Passwords, tokens, secrets and unnecessary PII must never be exposed.

---

# 61. Bean Validation

Transport-level structural validation should use Jakarta Bean Validation where appropriate.

---

# 62. Examples

```java
@NotNull
UUID idCustomer;

@NotBlank
@Size(max = 20)
String segment;
```

---

# 63. Structural Validation

Bean Validation is appropriate for rules such as:

- required
- size
- format
- numeric range

---

# 64. Domain Validation

Domain validation remains in application/domain services.

---

# 65. Example

This is transport validation:

```text
statusOrder must not be blank
```

This is domain validation:

```text
CANCELLED orders cannot be approved
```

---

# 66. Validation Duplication

Do not duplicate complex domain rules as annotations merely to move logic into DTOs.

---

# 67. Validation Messages

Validation messages should use the platform's standardized i18n/message strategy where applicable.

---

# 68. Input Size

String and collection inputs should have explicit reasonable size limits.

---

# 69. Unbounded Collection

Avoid:

```java
List<UUID> ids;
```

with no maximum when clients could send millions of elements.

---

# 70. Bulk Limit

Bulk APIs require explicit maximum batch size.

---

# 71. Error Contract

Errors must use a standardized response representation.

---

# 72. Recommended Error Shape

Conceptually:

```json
{
  "timestamp": "2026-07-24T15:30:00Z",
  "status": 400,
  "code": "ORDER_INVALID_STATUS",
  "message": "Order status does not allow this operation.",
  "correlationId": "..."
}
```

---

# 73. Machine-Readable Code

Error responses should provide stable machine-readable error codes where consumers need programmatic handling.

---

# 74. Human Message

Human-readable messages may evolve more freely than stable error codes.

---

# 75. Internal Exception

Java exception class names must not become public error codes.

Avoid:

```text
NullPointerException

DataIntegrityViolationException
```

in external contracts.

---

# 76. Stack Trace

Stack traces must never be returned to ordinary API clients.

---

# 77. Internal Details

Error responses must not expose:

- SQL
- internal hostnames
- pod names
- filesystem paths
- framework internals
- secrets

---

# 78. Validation Errors

Multiple field-validation errors may be represented as structured details.

Example:

```json
{
  "code": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "segment",
      "code": "SIZE"
    }
  ]
}
```

---

# 79. Correlation ID

Error responses should include or allow correlation with the request identifier.

---

# 80. Pagination

Collection endpoints returning potentially large result sets must use bounded pagination.

---

# 81. Page Size

Page size requires:

```text
Default

Minimum

Maximum
```

---

# 82. Example

Conceptually:

```text
page=0
size=20
```

with a maximum such as:

```text
size <= 100
```

according to endpoint capacity.

---

# 83. Client-Supplied Huge Page

Requests such as:

```text
size=1000000
```

must not bypass server limits.

---

# 84. Pagination Model

Offset/page pagination is appropriate for many administrative/query APIs.

---

# 85. Cursor Pagination

Cursor/keyset pagination should be considered for:

- very large datasets
- high-write datasets
- infinite scrolling
- latency-sensitive deep pagination

---

# 86. Stable Ordering

Pagination requires deterministic ordering.

---

# 87. Tie-Breaker

If sorting by a non-unique field, add a stable tie-breaker where needed.

Example:

```text
createdAt DESC,
id DESC
```

---

# 88. Sorting

Public sorting must use an explicit allowlist.

---

# 89. Critical Rule

Never expose arbitrary JPA/entity property paths directly through:

```text
sort=<property>
```

without mapping and validation.

---

# 90. Public Sort Field

The API may expose:

```text
sort=createdAt,desc
```

while internally mapping to:

```text
entity.audit.createdAt
```

---

# 91. Sort Mapping

Use a controlled mapping layer:

```text
API FIELD
    |
    v
ALLOWLIST
    |
    v
INTERNAL PATH
```

---

# 92. Unknown Sort

Unsupported public sort fields must produce a controlled client error rather than an internal persistence exception.

---

# 93. Sort Direction

Only supported directions should be accepted.

Typically:

```text
ASC

DESC
```

---

# 94. Default Sort

Paginated APIs should define a stable default sort.

---

# 95. Filtering

Filters are part of the API contract.

---

# 96. Explicit Filters

Prefer explicitly supported filters:

```text
status

customerId

segment

createdFrom

createdTo
```

---

# 97. Entity Leakage Through Filters

Do not automatically expose every entity field as a filter.

---

# 98. Filter Validation

Filter values require validation before query construction.

---

# 99. Dynamic Query

Dynamic filters should use safe parameterized query mechanisms.

---

# 100. SQL Injection

Never concatenate untrusted filter values into SQL/JPQL.

---

# 101. Search

Simple search may use GET query parameters.

---

# 102. Complex Search

For large structured search criteria, a command-like query endpoint may use:

```text
POST /orders/search
```

when GET query strings become impractical.

---

# 103. Search POST

A POST search must remain semantically read-only even though POST itself is not defined as safe by HTTP.

---

# 104. Search Response

Search endpoints should still use standard pagination and sorting contracts where applicable.

---

# 105. Query Parameter Naming

Query parameter naming must remain consistent within an API family.

---

# 106. Date-Time

API timestamps should use ISO-8601-compatible representations.

---

# 107. Offset

Use offset-aware timestamps where the instant matters.

Example:

```text
2026-07-24T15:30:00-03:00
```

or UTC:

```text
2026-07-24T18:30:00Z
```

---

# 108. Local Date

Use date-only representations when the domain value genuinely has no time component.

---

# 109. Time Zone

Do not silently infer server-local timezone for business instants.

---

# 110. Decimal Numbers

Financial values require decimal semantics.

---

# 111. Money

Money values should normally use decimal representations rather than binary floating point.

---

# 112. Enum Contract

Enums exposed externally become contract values.

---

# 113. Internal Enum Rename

Renaming a Java enum constant can become a breaking API change if serialized directly.

---

# 114. External Value

Where internal and external semantics differ, use explicit mapping.

Example:

```text
Internal:

PENDING_SUPERVISOR

External:

Pendente(Supervisor)
```

---

# 115. Enum Evolution

Adding a new enum value may break consumers that assume exhaustive known values.

---

# 116. Consumer Robustness

Consumers should be designed to tolerate compatible evolution where possible.

---

# 117. Nullability

Nullability is part of the contract.

---

# 118. Optional Response Field

Changing:

```text
nullable
```

to:

```text
required
```

or vice versa may affect consumers.

---

# 119. Missing vs Null

JSON:

```json
{}
```

and:

```json
{
  "field": null
}
```

may have different semantics.

The contract must define the distinction where relevant.

---

# 120. Empty Collection

Collection responses should generally prefer:

```json
[]
```

instead of:

```json
null
```

unless null has explicit domain meaning.

---

# 121. Boolean

Boolean naming should remain semantically clear.

Prefer:

```text
active

enabled

available
```

rather than ambiguous flags.

---

# 122. Backward Compatibility

Backward compatibility is mandatory for published APIs unless a breaking change follows the approved versioning process.

---

# 123. Usually Compatible Changes

Commonly compatible changes include:

- adding optional response fields
- adding optional request fields with safe defaults
- adding new endpoints

subject to consumer behavior.

---

# 124. Potentially Breaking Changes

Examples:

- removing a field
- renaming a field
- changing field type
- changing nullability
- changing enum values
- changing URI
- changing HTTP method
- changing status semantics
- changing validation constraints
- changing pagination defaults materially
- changing sort semantics
- changing error codes

---

# 125. Stricter Validation

Making validation stricter may be a breaking change.

Example:

```text
maxLength 100
```

becoming:

```text
maxLength 20
```

can break existing clients.

---

# 126. Response Field Removal

Do not remove response fields simply because the current frontend no longer uses them.

Other consumers may exist.

---

# 127. Consumer Inventory

Breaking-change analysis requires identifying known consumers.

---

# 128. OpenAPI

Every externally consumed REST API should maintain an OpenAPI specification.

---

# 129. OpenAPI Accuracy

OpenAPI must reflect actual runtime behavior.

---

# 130. Documentation Drift

A specification that differs from implementation is a defect.

---

# 131. OpenAPI Contents

Document:

- paths
- methods
- parameters
- request bodies
- responses
- schemas
- validation constraints
- authentication
- relevant examples

---

# 132. Response Documentation

Document meaningful error responses, not only `200`.

---

# 133. Examples

Examples should use realistic but non-sensitive values.

---

# 134. OpenAPI Diff

CI should consider automated OpenAPI compatibility/diff analysis for published APIs.

---

# 135. Breaking Contract Detection

Potential breaking changes should be detected before deployment.

---

# 136. Generated Client

Generated clients may be used, but generation does not remove the need for contract governance.

---

# 137. Versioning

Breaking changes require an explicit compatibility/version strategy.

---

# 138. URI Versioning

Where URI versioning is adopted:

```text
/api/v1/orders
```

is acceptable.

---

# 139. Avoid Premature Versioning

Do not create:

```text
v2
```

for every additive field.

---

# 140. Version Trigger

A new major API version is justified by incompatible contract evolution, not ordinary implementation refactoring.

---

# 141. Parallel Versions

When introducing a new major version:

```text
v1
+
v2
```

may coexist during migration.

---

# 142. Version Ownership

Each active version requires an owner.

---

# 143. Deprecation

Deprecated APIs require an explicit lifecycle.

---

# 144. Deprecation Process

Conceptually:

```text
Introduce Replacement
       |
       v
Mark Deprecated
       |
       v
Notify Consumers
       |
       v
Measure Usage
       |
       v
Migration Window
       |
       v
Remove
```

---

# 145. Immediate Removal

Published APIs must not normally disappear immediately after replacement.

---

# 146. Usage Telemetry

Where feasible, endpoint usage should inform deprecation decisions.

---

# 147. Idempotency

Idempotency must be considered for mutation endpoints that clients may safely retry.

---

# 148. Natural Idempotency

PUT and DELETE are expected to have idempotent semantics.

---

# 149. POST Idempotency

POST operations may require an explicit idempotency mechanism.

---

# 150. Idempotency-Key

Where supported:

```text
Idempotency-Key
```

may identify one logical mutation.

---

# 151. Server Ownership

The service owns idempotency enforcement.

The gateway may only propagate the key.

---

# 152. Duplicate Request

A retry using the same valid idempotency key should not create duplicate business resources when the endpoint guarantees idempotency.

---

# 153. Key Scope

The idempotency-key scope must be defined.

Possible dimensions:

```text
Client

Endpoint

Business Operation

Time Window
```

---

# 154. Idempotency Retention

Stored idempotency records require bounded retention.

---

# 155. Payload Conflict

Reusing the same idempotency key with a materially different payload should produce a controlled conflict.

---

# 156. Optimistic Concurrency

APIs modifying concurrently updated resources should consider optimistic concurrency control.

---

# 157. ETag

HTTP `ETag` / `If-Match` may be used where appropriate.

---

# 158. Version Field

A domain/API version token may alternatively support optimistic concurrency where deliberately designed.

---

# 159. Lost Update

The API must not silently accept lost updates where business correctness requires conflict detection.

---

# 160. Bulk Operations

Bulk APIs require explicit per-item semantics.

---

# 161. Bulk Request Limit

Bulk item count must be bounded.

---

# 162. Bulk Atomicity

The contract must define whether processing is:

```text
ALL-OR-NOTHING
```

or:

```text
PARTIAL SUCCESS
```

---

# 163. Partial Result

For partial success, response items should identify individual outcomes.

---

# 164. Example

Conceptually:

```json
{
  "results": [
    {
      "id": "...",
      "status": "SUCCESS"
    },
    {
      "id": "...",
      "status": "ERROR",
      "code": "ORDER_INVALID_STATUS"
    }
  ]
}
```

---

# 165. Bulk HTTP Status

The overall HTTP status and per-item result semantics must be explicitly documented.

---

# 166. Transaction Boundary

Do not assume a bulk HTTP request automatically implies one database transaction.

---

# 167. Long Bulk Transaction

Large bulk operations should avoid giant transactions.

---

# 168. Asynchronous Bulk

Very large bulk workloads should consider asynchronous job semantics.

---

# 169. Async Operation

For asynchronous processing:

```text
POST
   |
   v
202 Accepted
   |
   v
Operation Resource
   |
   v
GET /operations/{id}
```

may be used.

---

# 170. Operation Resource

An operation resource may expose:

```text
PENDING

RUNNING

SUCCEEDED

FAILED
```

according to the use case.

---

# 171. Polling

Polling endpoints require reasonable client polling guidance.

---

# 172. Retry-After

`Retry-After` may help control polling frequency.

---

# 173. Correlation Headers

The API should propagate the platform-standard correlation identifier.

---

# 174. Correlation Input

Externally supplied correlation IDs must be validated and bounded.

---

# 175. Correlation Output

Returning the correlation identifier in response headers/errors can improve supportability.

---

# 176. Business ID vs Correlation

Never use an Order ID as a substitute for a correlation ID.

---

# 177. Authentication

Authentication follows ADR-033.

---

# 178. Authorization

Business authorization remains service-owned.

---

# 179. Security by DTO

Response DTOs should minimize data exposure.

---

# 180. Mass Assignment

Request DTOs must prevent clients from setting internal fields they should not control.

---

# 181. Example

A create request must not automatically allow clients to set:

```text
approvedBy

createdAt

internalStatus

auditUser
```

unless contractually allowed.

---

# 182. Deserialization

Unknown-property handling must be intentionally configured according to compatibility strategy.

---

# 183. Unknown Request Fields

Ignoring unknown fields improves some forms of forward compatibility but can also hide client mistakes.

The policy must be deliberate.

---

# 184. Unknown Enum

Unknown enum handling must avoid uncontrolled `500` responses.

---

# 185. Content Type

JSON APIs should explicitly use appropriate content types.

Example:

```text
application/json
```

---

# 186. Unsupported Media Type

Unsupported content types should produce:

```text
415 Unsupported Media Type
```

where applicable.

---

# 187. Accept

Unsupported response representations may produce:

```text
406 Not Acceptable
```

where content negotiation is used.

---

# 188. Character Encoding

Text encoding must be explicit and consistent, normally UTF-8.

---

# 189. Compression

Compression may be enabled according to payload/performance characteristics.

---

# 190. Cache-Control

HTTP caching semantics should be explicitly defined where responses are cacheable.

---

# 191. Sensitive Responses

Sensitive authenticated responses should normally prevent unintended shared caching.

---

# 192. Conditional GET

`ETag` or `Last-Modified` may support efficient conditional GET where appropriate.

---

# 193. N+1 API

Avoid requiring consumers to perform excessive sequential requests when a cohesive contract can efficiently provide required data.

---

# 194. Over-Fetching

At the same time, avoid returning massive nested object graphs for every request.

---

# 195. Projection

Purpose-specific response projections may be appropriate.

Example:

```text
OrderHeaderResponse
```

for search/list screens rather than the complete order aggregate.

---

# 196. List vs Detail DTO

Collection and detail endpoints may use different response DTOs.

---

# 197. Performance Contract

API design affects:

- DB queries
- serialization
- network payload
- memory
- consumer latency

---

# 198. Maximum Collection

Nested collections should also have realistic bounded expectations.

---

# 199. Service-to-Service API

Internal APIs still require contracts.

"Internal" does not mean "safe to break".

---

# 200. Consumer Coupling

Microservices that deploy independently require compatible inter-service API evolution.

---

# 201. Timeout

Consumers must use finite HTTP timeouts.

---

# 202. Retry

Consumers must not retry mutation endpoints unless idempotency and retry eligibility are understood.

---

# 203. Circuit Breaker

Service clients should follow ADR-016 resilience standards.

---

# 204. Client Error Mapping

Consumers should map remote errors into meaningful integration/domain errors rather than leaking raw HTTP client exceptions everywhere.

---

# 205. Remote Error Body

Remote error payloads must be bounded before logging/propagation.

---

# 206. Sensitive Remote Data

Remote error messages must not expose bearer tokens or sensitive headers.

---

# 207. API Client DTOs

External provider DTOs should remain isolated from internal domain objects.

---

# 208. Anti-Corruption Layer

Important external integrations may use an anti-corruption layer to translate external semantics.

---

# 209. API Evolution

Contract evolution follows:

```text
Requirement
    |
    v
Compatibility Analysis
    |
    +---- Compatible ----> Extend Existing Contract
    |
    +---- Breaking ------> Version / Migration Plan
```

---

# 210. Database Change

A database change does not automatically require an API change.

---

# 211. Domain Refactoring

A domain refactoring does not automatically require an API change.

---

# 212. API Stability

Stable APIs allow internal architecture to evolve independently.

---

# 213. Controller Responsibility

Controllers should remain thin.

---

# 214. Controller Flow

Preferred:

```text
HTTP
 |
 v
Validate Transport
 |
 v
Application Service
 |
 v
Map Response
 |
 v
HTTP
```

---

# 215. Controller Business Logic

Complex business rules must not accumulate in controllers.

---

# 216. Mapper

Explicit mappers may translate:

```text
Request DTO -> Command

Domain -> Response DTO
```

---

# 217. Mapping Layer

Mapping must not become a hidden business-rule layer.

---

# 218. Exception Handler

Global exception handling should provide consistent error contracts.

---

# 219. Catch-All

A final unexpected-exception handler may prevent implementation details from leaking.

---

# 220. Catch-All Logging

Unexpected exceptions must be logged at the appropriate centralized boundary.

---

# 221. Expected Business Error

Expected business exceptions should not necessarily generate noisy ERROR logs.

---

# 222. OpenAPI Security

Security schemes must be represented accurately in OpenAPI.

---

# 223. Documentation Exposure

Swagger/OpenAPI UI exposure follows environment/security policy.

---

# 224. API Examples

Documentation examples must not contain real credentials or sensitive production data.

---

# 225. Testing

API contracts require automated tests.

---

# 226. Controller Contract Test

Verify:

- method
- route
- status
- validation
- JSON
- headers

---

# 227. Compatibility Test

Published contract changes should be checked for backward compatibility.

---

# 228. Sort Test

Verify public sort values map correctly to internal paths.

---

# 229. Unsupported Sort Test

Verify unsupported sort values fail cleanly.

---

# 230. Pagination Limit Test

Verify server-side maximum page size.

---

# 231. Validation Boundary Test

Verify exact allowed limits.

Example:

```text
20 characters -> accepted

21 characters -> rejected
```

when maximum is 20.

---

# 232. Error Contract Test

Verify stable error:

```text
status

code

required fields
```

without over-coupling to dynamic values.

---

# 233. Enum Test

Externally serialized enum values require explicit compatibility tests where critical.

---

# 234. Idempotency Test

Verify duplicate POST behavior where idempotency is promised.

---

# 235. Authorization Test

Verify authenticated users cannot access resources outside their permitted scope.

---

# 236. API Performance Test

Critical list/search endpoints require performance testing with realistic:

- filters
- sorting
- page sizes
- dataset volumes

---

# 237. API Observability

Monitor at least:

```text
Request Rate

Latency

Status

Route

Dependency Failures
```

according to ADR-019/ADR-020/ADR-033.

---

# 238. Route Template

Metrics should use:

```text
/orders/{orderId}
```

rather than:

```text
/orders/0bce...
```

to prevent high cardinality.

---

# 239. Sensitive Query Logging

Sensitive query parameters must not be indiscriminately logged.

---

# 240. Deprecation Metrics

Deprecated endpoint usage should be measurable where practical.

---

# 241. Anti-Patterns

The following are prohibited or strongly discouraged:

- verbs as default REST resource paths
- GET performing business mutations
- returning 200 for ordinary errors
- exposing JPA entities directly
- exposing internal persistence fields accidentally
- leaking stack traces
- leaking Java exception class names
- unbounded page sizes
- unbounded bulk requests
- arbitrary entity/JPA fields exposed as sort parameters
- arbitrary entity fields exposed as filters
- SQL/JPQL string concatenation with client values
- unstable pagination ordering
- using PUT for undocumented partial update
- ambiguous PATCH null semantics
- changing published field types without compatibility analysis
- removing response fields because one consumer stopped using them
- exposing new enum values without compatibility consideration
- treating OpenAPI as optional documentation
- allowing OpenAPI to drift from runtime behavior
- creating v2 for every additive change
- removing deprecated APIs without consumer migration
- retrying non-idempotent POST blindly
- mass assignment of internal fields
- giant nested response graphs
- returning complete aggregates for simple list screens
- assuming internal APIs may break freely
- controller business logic
- mapping layers containing hidden domain rules
- using database schema as API design
- exposing raw remote-client exceptions
- using business identifiers as correlation IDs

---

# 242. Positive Consequences

The decision provides:

- predictable APIs
- stronger compatibility
- cleaner controller boundaries
- safer DTO evolution
- explicit HTTP semantics
- safer pagination
- controlled sorting/filtering
- improved OpenAPI quality
- easier client integration
- better security
- improved performance
- simpler internal refactoring

---

# 243. Negative Consequences

The decision introduces:

- DTO mapping
- contract governance
- compatibility analysis
- OpenAPI maintenance
- more contract testing
- deprecation lifecycle management

These costs are accepted because APIs are long-lived integration boundaries.

---

# 244. Neutral Consequences

The decision also means:

- not every domain operation maps directly to CRUD
- command endpoints remain valid where semantically justified
- some APIs may use cursor pagination
- some POST endpoints may be read-only searches
- internal APIs still require compatibility management
- API and persistence models intentionally diverge

---

# 245. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Breaking consumer | Critical | Medium | Compatibility analysis |
| Entity leakage | High | Medium | Dedicated DTOs |
| Unbounded queries | Critical | Medium | Pagination limits |
| Invalid sort path | High | Medium | Sort allowlist |
| OpenAPI drift | High | Medium | Automated validation |
| Enum incompatibility | High | Medium | Explicit mapping |
| Duplicate POST | Critical | Medium | Idempotency |
| Sensitive error exposure | Critical | Low | Global error contract |
| Mass assignment | Critical | Medium | Dedicated request DTO |
| N+1 consumer calls | Medium | Medium | Purpose-specific projections |
| Over-fetching | Medium | Medium | List/detail contracts |
| Deprecated API persists forever | Medium | High | Usage telemetry/lifecycle |

---

# 246. Implementation Guidance

The following rules are mandatory:

1. REST URIs should primarily represent resources.
2. GET must not perform business mutations.
3. HTTP status codes must reflect actual outcomes.
4. Ordinary errors must not be returned as `200 OK`.
5. Request and response DTOs must be separated from persistence entities.
6. Records should be preferred for immutable DTOs where suitable.
7. Structural validation belongs at the transport boundary.
8. Domain validation remains in the service/domain layer.
9. Input strings and collections must have reasonable bounds.
10. Error responses must use standardized machine-readable codes.
11. Internal exceptions and stack traces must not leak.
12. Large collections require bounded pagination.
13. Pagination requires deterministic sorting.
14. Public sorting must use an explicit allowlist and mapping.
15. JPA/entity property paths must not be exposed directly as public sorting contracts.
16. Filters must be explicit and validated.
17. Dynamic queries must use parameterized query mechanisms.
18. Financial values require decimal semantics.
19. Externally exposed enum values must be treated as contracts.
20. Published API changes require compatibility analysis.
21. Stricter validation is a potential breaking change.
22. OpenAPI must accurately describe runtime behavior.
23. Breaking changes require an explicit migration/version strategy.
24. Deprecation requires consumer migration and usage monitoring where possible.
25. POST operations requiring safe retry must implement explicit idempotency.
26. Bulk operations require bounded size and defined atomicity semantics.
27. Correlation identifiers must remain separate from business identifiers.
28. Request DTOs must prevent mass assignment.
29. List endpoints should avoid unnecessarily returning full aggregates.
30. Internal service APIs require the same compatibility discipline.
31. Controllers must remain thin.
32. Global exception handling must preserve a consistent error contract.
33. API contracts require automated controller/compatibility tests.
34. Critical search endpoints require realistic performance testing.
35. API observability must use bounded route templates rather than raw IDs.

---

# 247. API Production Readiness Gate

A new or modified API is not production ready until:

```text
[ ] Resource URI reviewed

[ ] HTTP method reviewed

[ ] Success status reviewed

[ ] Error statuses reviewed

[ ] Request DTO defined

[ ] Response DTO defined

[ ] Persistence entity not exposed

[ ] Required fields defined

[ ] Nullability defined

[ ] Size limits defined

[ ] Collection limits defined

[ ] Domain validation defined

[ ] Error codes defined

[ ] Sensitive fields reviewed

[ ] Pagination reviewed

[ ] Maximum page size defined

[ ] Stable default sorting defined

[ ] Sort allowlist defined

[ ] Filter allowlist defined

[ ] Dynamic queries parameterized

[ ] Enum compatibility reviewed

[ ] Date/time semantics reviewed

[ ] Decimal semantics reviewed

[ ] Idempotency reviewed

[ ] Bulk semantics reviewed

[ ] Correlation propagation reviewed

[ ] Authorization reviewed

[ ] OpenAPI updated

[ ] OpenAPI examples reviewed

[ ] Backward compatibility checked

[ ] Consumer impact reviewed

[ ] Contract tests pass

[ ] Security tests pass

[ ] Performance reviewed

[ ] Sonar/SAST gates pass
```

---

# 248. Validation

This ADR will be validated through:

- architecture reviews
- API reviews
- OpenAPI reviews
- contract tests
- controller tests
- compatibility checks
- integration tests
- security tests
- pagination tests
- sorting tests
- filtering tests
- idempotency tests
- load tests
- SonarQube
- SAST
- consumer feedback
- production API telemetry

---

# 249. Success Criteria

The decision is successful when:

- API consumers can evolve independently
- internal refactoring rarely requires API changes
- breaking changes are detected before deployment
- APIs use consistent HTTP semantics
- persistence details remain private
- pagination protects service capacity
- public sorting does not expose entity implementation
- errors remain machine-readable and secure
- OpenAPI accurately represents runtime contracts
- safe retry behavior is explicit
- API changes do not unexpectedly break known consumers

---

# 250. Alternatives Rejected

## 250.1 Expose JPA Entities Directly

Rejected because persistence evolution would become API evolution.

---

## 250.2 Always Return HTTP 200

Rejected because it destroys standard HTTP error semantics.

---

## 250.3 Allow Arbitrary Sort Fields

Rejected because it leaks persistence structure and may create invalid or expensive queries.

---

## 250.4 Unlimited Page Size

Rejected because consumers could exhaust application/database resources.

---

## 250.5 Version Every Change

Rejected because compatible additive evolution does not justify version proliferation.

---

## 250.6 Break Internal APIs Freely

Rejected because independently deployed services are still consumers/providers.

---

## 250.7 Retry Every POST

Rejected because non-idempotent mutations may create duplicate business operations.

---

# 251. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-009: Use Apache Kafka for Integration Events
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-033: Adopt API Gateway and Edge Architecture Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-037: Adopt Application Security and Secure Coding Standards

---

# 252. References

- HTTP Semantics
- OpenAPI Specification
- Jakarta Bean Validation
- OWASP API Security
- Spring MVC Documentation
- Spring Boot Documentation
- Spring Security Documentation
- RFC 9110
- RFC 9457
- RFC 6902
- RFC 7396

---

# 253. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial REST API and compatibility baseline |

---

# 254. Decision Summary

The definitive API boundary is:

```text
                    CONSUMER
                       |
                       v
                +--------------+
                | REST CONTRACT|
                +--------------+
                | URI          |
                | HTTP Method  |
                | Request DTO  |
                | Validation   |
                | Status       |
                | Response DTO |
                | Error        |
                +--------------+
                       |
                       v
                  CONTROLLER
                       |
                       v
              APPLICATION SERVICE
                       |
                       v
                    DOMAIN
                       |
                       v
                 PERSISTENCE
```

Never:

```text
DATABASE ENTITY
      |
      v
CONTROLLER
      |
      v
JSON
```

because this creates:

```text
Database Change
      =
API Change
```

Instead:

```text
Entity
   |
   v
Mapping
   |
   v
Response DTO
   |
   v
Stable API Contract
```

Sorting follows:

```text
CLIENT

sort=createdAt,desc
        |
        v
PUBLIC SORT ALLOWLIST
        |
        v
OrderSortPathResolver
        |
        v
entity.audit.createdAt
        |
        v
DATABASE
```

Never:

```text
CLIENT
   |
   v
sort=<arbitrary JPA path>
   |
   v
JpaSort
   |
   v
DATABASE
```

Pagination follows:

```text
CLIENT
   |
   | size=1000000
   v
SERVER
   |
   v
MAXIMUM PAGE SIZE
   |
   v
BOUNDED QUERY
```

Compatibility follows:

```text
API CHANGE
    |
    v
COMPATIBILITY ANALYSIS
    |
    +-----------------------+
    |                       |
    v                       v
COMPATIBLE               BREAKING
    |                       |
    v                       v
EVOLVE CURRENT        VERSION / MIGRATE
CONTRACT                   |
                            v
                    DEPRECATE OLD
                            |
                            v
                    MEASURE USAGE
                            |
                            v
                         REMOVE
```

Error handling follows:

```text
DOMAIN FAILURE
      |
      v
EXCEPTION MAPPING
      |
      +--> HTTP STATUS
      +--> STABLE CODE
      +--> SAFE MESSAGE
      +--> CORRELATION ID
      |
      v
CLIENT
```

not:

```text
Exception
    |
    v
HTTP 200
    |
    v
"success": false
```

And API evolution follows the most important rule:

```text
Internal code belongs to the service.

API contracts belong to consumers.
```

Therefore:

```text
Refactor freely internally.

Evolve externally deliberately.
```
