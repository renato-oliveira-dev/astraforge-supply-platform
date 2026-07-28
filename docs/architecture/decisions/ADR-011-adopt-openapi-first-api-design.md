# ADR-011: Adopt OpenAPI-First API Design

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-011 |
| Title | Adopt OpenAPI-First API Design |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | API Architecture |
| Related Work Items | API Standardization and Governance |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform exposes REST APIs consumed by:

- Web applications
- Mobile applications
- Internal microservices
- Integration services
- Partner systems
- Future public APIs

As the number of services grows, API consistency becomes increasingly difficult to maintain.

Different teams naturally make different decisions regarding:

- naming conventions
- payload structures
- HTTP status codes
- validation errors
- pagination
- filtering
- authentication
- versioning
- documentation

Without governance, APIs quickly become inconsistent.

The platform requires a contract-driven development approach.

---

# 2. Problem Statement

The platform requires an API strategy that:

- defines contracts before implementation
- allows independent frontend development
- provides stable documentation
- enables backward compatibility
- supports automated validation
- improves API governance
- reduces integration ambiguity
- supports code generation
- standardizes error handling
- integrates with CI/CD

---

# 3. Decision Drivers

Primary drivers include:

1. API consistency
2. Consumer-first design
3. Documentation quality
4. Backward compatibility
5. Automated validation
6. Developer experience
7. Governance
8. Long-term maintainability
9. Tooling compatibility
10. Enterprise standards

---

# 4. Considered Options

## 4.1 Code First

Implementation is written first.

Documentation is generated afterwards.

Advantages:

- faster initial development
- familiar workflow

Disadvantages:

- documentation frequently becomes outdated
- implementation dictates the contract
- consumers discover changes late
- inconsistent APIs

---

## 4.2 OpenAPI First

The API contract is designed before implementation.

Advantages:

- contract-first development
- parallel frontend/backend work
- early API reviews
- stable documentation
- automated validation
- strong governance
- improved testing
- excellent tooling

Disadvantages:

- requires initial design effort
- contract reviews become mandatory
- additional documentation discipline

---

# 5. Decision

The Enterprise Order Platform adopts an **OpenAPI-First** approach.

Every public REST endpoint must begin with an OpenAPI specification.

Implementation follows the approved contract.

---

# 6. Rationale

Designing APIs before implementation produces:

- better contracts
- clearer discussions
- earlier feedback
- improved consistency
- stronger governance

Consumers should review APIs before code exists.

---

# 7. API Lifecycle

The standard lifecycle is:

```text
Business Requirement

↓

OpenAPI Specification

↓

Architecture Review

↓

Implementation

↓

Automated Validation

↓

Integration Testing

↓

Release
```

---

# 8. OpenAPI Version

The platform adopts:

```text
OpenAPI 3.1
```

Future upgrades require a new ADR.

---

# 9. Documentation Source of Truth

The OpenAPI specification is the authoritative API contract.

Implementation must follow the specification.

Code must not redefine the contract independently.

---

# 10. Contract Repository

OpenAPI documents remain version-controlled.

Recommended structure:

```text
docs/api/

orders.yaml

customers.yaml

products.yaml
```

---

# 11. API Versioning

Major versions appear in the URI.

Example:

```text
/api/v1/orders

/api/v2/orders
```

Breaking changes require a new version.

---

# 12. Backward Compatibility

Compatible changes include:

- optional fields
- new endpoints
- additional query parameters
- new response fields

Breaking changes include:

- removed fields
- renamed fields
- changed semantics
- modified required fields
- changed data types

---

# 13. Naming Standards

Resources use nouns.

Examples:

```text
/orders

/customers

/products
```

Avoid verbs.

Poor example:

```text
/createOrder
```

---

# 14. HTTP Methods

Standard usage:

| Method | Purpose |
|---|---|
| GET | Read |
| POST | Create |
| PUT | Replace |
| PATCH | Partial update |
| DELETE | Remove |

---

# 15. Status Codes

Recommended responses:

| Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Created |
| 202 | Accepted |
| 204 | No Content |
| 400 | Invalid Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 422 | Business Validation |
| 500 | Internal Error |

---

# 16. Error Model

The platform adopts **RFC 9457 Problem Details** as the standard error response.

Example:

```json
{
  "type": "https://api.company.com/problems/business-validation",
  "title": "Business Validation Error",
  "status": 422,
  "detail": "Order cannot be approved.",
  "instance": "/orders/123"
}
```

---

# 17. Validation

OpenAPI schemas define:

- required fields
- formats
- length
- enums
- patterns
- numeric constraints

Business rules remain inside the Domain layer.

---

# 18. Pagination

Standard pagination parameters:

```text
page

size

sort
```

Responses include metadata.

---

# 19. Filtering

Filtering uses query parameters.

Example:

```text
GET /orders?status=APPROVED
```

---

# 20. Sorting

Sorting format:

```text
sort=createdAt,desc
```

---

# 21. Idempotency

POST operations that create critical resources should support idempotency keys when appropriate.

---

# 22. Authentication

Authentication requirements must appear explicitly in the OpenAPI specification.

---

# 23. Authorization

Authorization rules belong in documentation but enforcement remains in the application.

---

# 24. Examples

Every endpoint should include:

- request examples
- response examples
- error examples

---

# 25. Deprecation

Deprecated endpoints must include:

- deprecation notice
- replacement endpoint
- removal schedule

---

# 26. Code Generation

OpenAPI may generate:

- DTOs
- API clients
- server stubs
- documentation

Generated code must not contain business logic.

---

# 27. Testing

Contract tests validate:

- requests
- responses
- schemas
- status codes

---

# 28. CI Validation

CI must verify:

- valid OpenAPI syntax
- schema correctness
- duplicate paths
- missing examples
- contract compatibility

---

# 29. Anti-Patterns

The following are prohibited:

- undocumented endpoints
- implementation-first APIs
- inconsistent status codes
- undocumented breaking changes
- verbs in resource names
- HTML error pages
- exposing stack traces

---

# 30. Positive Consequences

The decision provides:

- standardized APIs
- better documentation
- earlier reviews
- safer integrations
- consumer confidence
- stronger governance

---

# 31. Negative Consequences

The decision introduces:

- additional design effort
- mandatory reviews
- documentation maintenance

These costs are acceptable.

---

# 32. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Outdated specification | CI validation |
| Breaking contracts | Versioning |
| Missing documentation | Review checklist |
| Consumer confusion | Examples and RFC 9457 |

---

# 33. Implementation Guidance

Mandatory rules:

1. Every public endpoint begins with an OpenAPI specification.
2. OpenAPI is the source of truth.
3. Breaking changes require a new version.
4. APIs must follow REST conventions.
5. RFC 9457 Problem Details is mandatory.
6. Examples must be included.
7. CI validates every specification.
8. Business rules stay outside OpenAPI.

---

# 34. Validation

Validation includes:

- contract review
- schema validation
- generated documentation
- integration tests
- consumer testing

---

# 35. Success Criteria

The decision is successful when:

- every public API has an OpenAPI contract
- consumers can integrate before implementation
- documentation remains synchronized
- breaking changes are controlled
- contract validation passes automatically

---

# 36. Related Decisions

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-013: Use Testcontainers for Integration Testing

---

# 37. References

- OpenAPI Specification 3.1
- RFC 9457 – Problem Details for HTTP APIs
- RESTful API Design Best Practices
- Enterprise Order Platform API Design Guidelines

---

# 38. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved |

---

# 39. Decision Summary

The Enterprise Order Platform adopts **OpenAPI-First** as the mandatory API design strategy.

Every REST API will:

- begin with an OpenAPI contract;
- follow REST standards;
- use RFC 9457 for error responses;
- support automated validation;
- maintain backward compatibility;
- be reviewed before implementation.

This decision establishes a contract-driven API governance model that improves consistency, documentation quality and developer experience across the platform.
