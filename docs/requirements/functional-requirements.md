# Functional Requirements

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Functional Requirements |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

## 1. Purpose

This document defines the initial functional requirements for AstraForge Supply Platform.

Each requirement has a unique identifier to support traceability across:

- Business rules
- Architecture documentation
- Source code
- Automated tests
- API documentation
- GitHub issues
- Architecture Decision Records

## 2. Requirement Conventions

Requirements use the following format:

```text
FR-[DOMAIN]-[NUMBER]
```

Examples:

```text
FR-AUTH-001
FR-CUST-001
FR-ORD-001
FR-APPR-001
```

Priority levels:

| Priority | Description |
|---|---|
| Must Have | Required for the initial usable version |
| Should Have | Important but not essential for the first release |
| Could Have | Desirable future capability |
| Won't Have | Explicitly excluded from the current scope |

---

## 3. Authentication and Authorization

### FR-AUTH-001 — Authenticate Users

**Priority:** Must Have

The platform must authenticate users through a valid JSON Web Token.

#### Acceptance Criteria

- Requests to protected resources must require authentication.
- Invalid or expired tokens must be rejected.
- Authentication failures must not expose sensitive technical information.
- The authenticated user identifier must be available to application services.

---

### FR-AUTH-002 — Authorize Operations by Role

**Priority:** Must Have

The platform must authorize operations according to the authenticated user's roles and permissions.

#### Initial Roles

- CUSTOMER
- SALES_REPRESENTATIVE
- APPROVER
- OPERATIONS
- ADMINISTRATOR

#### Acceptance Criteria

- Unauthorized operations must return an appropriate HTTP status.
- Authorization rules must be enforced on the server.
- Users must not be able to elevate their own permissions.
- Administrative operations must require administrator privileges.

---

### FR-AUTH-003 — Propagate User Context

**Priority:** Must Have

The platform must propagate authenticated user information to business operations that require traceability.

#### Acceptance Criteria

- Approval decisions must identify the responsible user.
- Cancellation operations must identify the responsible user.
- Audit records must include the acting user when available.
- User identifiers must not be accepted exclusively from untrusted request fields.

---

## 4. Customer Management

### FR-CUST-001 — Register Customer References

**Priority:** Must Have

The platform must support the registration of customer reference data required by order processing.

#### Required Information

- Customer identifier
- Legal name
- Trade name, when applicable
- Document number
- Status
- Customer type
- Contact information

#### Acceptance Criteria

- Customer identifiers must be unique.
- Document numbers must not be duplicated.
- Required fields must be validated.
- Invalid customer data must be rejected.

---

### FR-CUST-002 — Retrieve Customer Information

**Priority:** Must Have

The platform must allow authorized users to retrieve customer information.

#### Acceptance Criteria

- Customers must be retrievable by identifier.
- Customer search must support pagination.
- Sensitive information must only be exposed when required.
- Nonexistent customers must produce a clear not-found response.

---

### FR-CUST-003 — Validate Customer Eligibility

**Priority:** Must Have

The platform must validate whether a customer is eligible to create or submit an order.

#### Acceptance Criteria

- Inactive customers must not submit new orders.
- Blocked customers must not submit new orders.
- Customer eligibility must be checked before order submission.
- Eligibility failures must provide a clear business error.

---

### FR-CUST-004 — Preserve Customer Snapshot

**Priority:** Should Have

The platform should preserve relevant customer information in the order at submission time.

#### Acceptance Criteria

- Changes to customer master data must not alter historical order information.
- The snapshot must contain only information relevant to the order.
- Snapshot creation must occur before the order becomes immutable.

---

## 5. Product Catalog

### FR-PROD-001 — Register Product References

**Priority:** Must Have

The platform must support product reference data required by order processing.

#### Required Information

- Product identifier
- SKU
- Name
- Description
- Status
- Unit of measure
- Base price

#### Acceptance Criteria

- Product identifiers must be unique.
- SKUs must not be duplicated.
- Prices must not be negative.
- Required fields must be validated.

---

### FR-PROD-002 — Retrieve Products

**Priority:** Must Have

The platform must allow authorized users to retrieve product information.

#### Acceptance Criteria

- Products must be retrievable by identifier.
- Products must be retrievable by SKU.
- Product search must support pagination.
- Inactive products must be clearly identified.

---

### FR-PROD-003 — Validate Product Availability for Ordering

**Priority:** Must Have

The platform must validate whether a product can be included in an order.

#### Acceptance Criteria

- Inactive products must not be added to new orders.
- Discontinued products must not be added unless explicitly permitted.
- Product validation must occur when an item is added.
- Product validation must be repeated before order submission.

---

### FR-PROD-004 — Preserve Product Snapshot

**Priority:** Must Have

The platform must preserve relevant product information in each order item.

#### Acceptance Criteria

- Historical orders must not change when product data changes.
- The snapshot must include the SKU and product name.
- The order item must preserve the effective unit price.
- The snapshot must be created before order submission.

---

## 6. Order Management

### FR-ORD-001 — Create Draft Order

**Priority:** Must Have

The platform must allow authorized users to create a draft order for an eligible customer.

#### Acceptance Criteria

- The customer must exist.
- The customer must be active.
- The new order must receive a unique identifier.
- The initial status must be `DRAFT`.
- The creation operation must be audited.

---

### FR-ORD-002 — Add Order Item

**Priority:** Must Have

The platform must allow items to be added to a draft order.

#### Acceptance Criteria

- The order must exist.
- The order must be in `DRAFT` status.
- The product must exist and be available for ordering.
- Quantity must be greater than zero.
- The product must not be duplicated when the business rule requires item consolidation.
- The order total must be recalculated.

---

### FR-ORD-003 — Update Order Item

**Priority:** Must Have

The platform must allow the quantity or eligible commercial information of an order item to be updated while the order is editable.

#### Acceptance Criteria

- The order must be in an editable status.
- Quantity must be greater than zero.
- Invalid prices or discounts must be rejected.
- The order total must be recalculated.
- The modification must be audited.

---

### FR-ORD-004 — Remove Order Item

**Priority:** Must Have

The platform must allow an item to be removed from a draft order.

#### Acceptance Criteria

- The order must be in `DRAFT` status.
- The item must belong to the order.
- The order total must be recalculated.
- Removing a nonexistent item must produce a clear response.

---

### FR-ORD-005 — Retrieve Order

**Priority:** Must Have

The platform must allow authorized users to retrieve an order and its items.

#### Acceptance Criteria

- Orders must be retrievable by identifier.
- The response must include the current status.
- The response must include calculated totals.
- Access must respect authorization and ownership rules.
- Nonexistent orders must produce a not-found response.

---

### FR-ORD-006 — Search Orders

**Priority:** Must Have

The platform must provide paginated order search.

#### Initial Filters

- Order identifier
- Customer identifier
- Status
- Creation date range
- Submitted date range

#### Acceptance Criteria

- Search results must be paginated.
- Sorting must use an approved list of fields.
- Invalid sorting fields must be rejected.
- Search operations must not expose unauthorized orders.

---

### FR-ORD-007 — Submit Order

**Priority:** Must Have

The platform must allow an eligible draft order to be submitted for processing.

#### Acceptance Criteria

- The order must be in `DRAFT` status.
- The order must contain at least one item.
- The customer must remain eligible.
- All products must remain eligible.
- Prices and totals must be validated.
- Required commercial information must be present.
- The order status must transition according to approval rules.
- The operation must be audited.
- Relevant events must be produced.

---

### FR-ORD-008 — Prevent Invalid State Transitions

**Priority:** Must Have

The platform must reject order state transitions not allowed by the domain rules.

#### Acceptance Criteria

- Valid transitions must be explicitly defined.
- Invalid transitions must produce a business error.
- State transition rules must be implemented in the domain layer.
- Controllers must not directly manipulate order status.

---

### FR-ORD-009 — Cancel Order

**Priority:** Must Have

The platform must allow authorized users to cancel an eligible order.

#### Acceptance Criteria

- Cancellation eligibility must depend on the current status.
- Completed orders must not be cancelled.
- Shipped orders must not be cancelled through the initial process.
- A cancellation reason must be recorded.
- The responsible user must be identified.
- Reserved inventory must be released when applicable.
- The operation must be audited.
- An order-cancelled event must be produced.

---

### FR-ORD-010 — Maintain Order Status History

**Priority:** Must Have

The platform must preserve the history of order status transitions.

#### Acceptance Criteria

- Each transition must record the previous status.
- Each transition must record the new status.
- Each transition must record the date and time.
- Each transition must identify the responsible user or system.
- Historical entries must not be overwritten.

---

## 7. Pricing and Totals

### FR-PRICE-001 — Calculate Order Item Total

**Priority:** Must Have

The platform must calculate the total value of each order item.

#### Initial Formula

```text
item subtotal = quantity × unit price
item total = subtotal - discount + tax + fees
```

#### Acceptance Criteria

- Monetary calculations must use decimal arithmetic.
- Floating-point types must not be used for monetary values.
- Rounding rules must be explicit.
- Negative totals must be rejected unless explicitly supported.

---

### FR-PRICE-002 — Calculate Order Total

**Priority:** Must Have

The platform must calculate the financial totals of the order.

#### Initial Totals

- Items subtotal
- Discount total
- Tax total
- Fee total
- Freight total
- Grand total

#### Acceptance Criteria

- Totals must be recalculated after relevant item changes.
- The grand total must be derived from the component totals.
- Stored totals must be validated before submission.
- Calculation rules must be covered by automated tests.

---

### FR-PRICE-003 — Apply Discount Rules

**Priority:** Should Have

The platform should support explicit discount policies.

#### Acceptance Criteria

- Discounts must be represented by domain concepts.
- Unauthorized discounts must be rejected.
- Maximum discount limits must be configurable.
- Applied discounts must be auditable.

---

### FR-PRICE-004 — Preserve Pricing Snapshot

**Priority:** Must Have

The platform must preserve the pricing information effective at order submission time.

#### Acceptance Criteria

- Future price changes must not alter submitted orders.
- Unit price, discounts, taxes, and fees must be preserved.
- Pricing snapshots must be immutable after submission.

---

## 8. Approval Workflow

### FR-APPR-001 — Determine Approval Requirement

**Priority:** Must Have

The platform must determine whether an order requires approval.

#### Initial Criteria May Include

- Order total
- Discount percentage
- Customer risk classification
- Product category
- Commercial exception

#### Acceptance Criteria

- Approval rules must be explicit.
- Orders not requiring approval may continue automatically.
- Orders requiring approval must transition to `PENDING_APPROVAL`.
- The decision must be reproducible from the recorded data.

---

### FR-APPR-002 — List Pending Approvals

**Priority:** Must Have

The platform must allow approvers to retrieve orders pending their decision.

#### Acceptance Criteria

- Results must be paginated.
- Users must only see approvals within their responsibility.
- Results must include sufficient decision information.
- Sensitive data must not be unnecessarily exposed.

---

### FR-APPR-003 — Approve Order

**Priority:** Must Have

The platform must allow an authorized approver to approve an eligible order.

#### Acceptance Criteria

- The order must be pending approval.
- The approver must have the required role.
- The approver must be eligible for the relevant approval level.
- The decision must identify the approver.
- Comments may be recorded.
- The operation must be audited.
- An approval event must be produced.

---

### FR-APPR-004 — Reject Order

**Priority:** Must Have

The platform must allow an authorized approver to reject an eligible order.

#### Acceptance Criteria

- The order must be pending approval.
- The approver must have the required role.
- A rejection reason must be provided.
- The order must transition to `REJECTED`.
- The operation must be audited.
- A rejection event must be produced.

---

### FR-APPR-005 — Request Order Review

**Priority:** Should Have

The platform should allow an approver to return an order for additional information or correction.

#### Acceptance Criteria

- A review reason must be provided.
- The order must transition to an editable review status.
- The requesting approver must be identified.
- The operation must be audited.
- Relevant users must be notified.

---

## 9. Inventory Management

### FR-INV-001 — Request Inventory Reservation

**Priority:** Must Have

The platform must request inventory reservation for eligible order items.

#### Acceptance Criteria

- Reservation must only occur for eligible order statuses.
- Each reservation request must have an idempotency identifier.
- Duplicate requests must not reserve inventory twice.
- The request must include product, quantity, and order references.

---

### FR-INV-002 — Confirm Inventory Reservation

**Priority:** Must Have

The platform must process successful inventory reservation results.

#### Acceptance Criteria

- The result must reference the original reservation request.
- Duplicate confirmation messages must be handled safely.
- The order must transition according to the lifecycle rules.
- The confirmation must be audited.
- A reservation-confirmed event must be produced.

---

### FR-INV-003 — Handle Insufficient Inventory

**Priority:** Must Have

The platform must handle inventory reservation failures caused by insufficient stock.

#### Acceptance Criteria

- The order must not proceed to fulfillment.
- The failure reason must be recorded.
- The operation must be traceable.
- Relevant users must be notified.
- A reservation-failed event must be produced.

---

### FR-INV-004 — Release Inventory

**Priority:** Must Have

The platform must request inventory release when a reserved order is cancelled or otherwise invalidated.

#### Acceptance Criteria

- Release operations must be idempotent.
- Inventory must not be released more than once.
- Release failures must support retry.
- The release request must be traceable.

---

## 10. Event Publishing

### FR-EVENT-001 — Produce Domain Events

**Priority:** Must Have

The platform must produce domain events for relevant business state changes.

#### Initial Events

- OrderCreated
- OrderSubmitted
- OrderApproved
- OrderRejected
- OrderCancelled
- InventoryReservationRequested
- InventoryReserved
- InventoryReservationFailed

#### Acceptance Criteria

- Events must represent completed business facts.
- Events must contain a unique identifier.
- Events must contain occurrence date and time.
- Events must not expose unnecessary sensitive information.

---

### FR-EVENT-002 — Publish Integration Events Reliably

**Priority:** Must Have

The platform must publish integration events without losing events committed as part of business transactions.

#### Acceptance Criteria

- Business data and outbox records must be persisted atomically.
- Event publication must support retries.
- Successfully published events must not be published indefinitely.
- Publication failures must be observable.
- Consumers must be able to identify duplicate messages.

---

### FR-EVENT-003 — Support Event Idempotency

**Priority:** Must Have

The platform must safely handle duplicate integration events.

#### Acceptance Criteria

- Incoming events must have unique identifiers.
- Already processed events must not repeat business effects.
- Idempotency records must support concurrent processing.
- Processing results must remain traceable.

---

## 11. Audit

### FR-AUD-001 — Record Business Audit Events

**Priority:** Must Have

The platform must record important business operations.

#### Initial Audited Operations

- Order creation
- Item addition, update, and removal
- Order submission
- Approval
- Rejection
- Review request
- Cancellation
- Inventory reservation
- Administrative changes

#### Acceptance Criteria

- Audit records must include operation type.
- Audit records must include date and time.
- Audit records must identify the acting user or system.
- Audit records must reference the affected entity.
- Audit records must not store secrets or credentials.

---

### FR-AUD-002 — Retrieve Audit History

**Priority:** Should Have

The platform should allow authorized users to retrieve audit information.

#### Acceptance Criteria

- Audit search must be paginated.
- Search must support entity identifier and date range.
- Access must be restricted.
- Audit records must be immutable through normal application operations.

---

## 12. Notifications

### FR-NOTIF-001 — Request Business Notifications

**Priority:** Should Have

The platform should request notifications for relevant business events.

#### Initial Notification Triggers

- Order submitted
- Approval requested
- Order approved
- Order rejected
- Review requested
- Order cancelled
- Inventory unavailable

#### Acceptance Criteria

- Notification failure must not roll back a completed business transaction.
- Notification requests must contain a correlation identifier.
- Sensitive information must not be included unnecessarily.
- Failed requests must support retry.

---

## 13. Administration

### FR-ADMIN-001 — Manage Configurable Parameters

**Priority:** Should Have

The platform should support administrative management of selected business parameters.

#### Initial Parameters

- Approval thresholds
- Maximum discount percentage
- Retry limits
- Reservation expiration
- Notification configuration

#### Acceptance Criteria

- Only administrators may modify parameters.
- Parameter changes must be audited.
- Invalid parameter values must be rejected.
- Parameter history should be preserved when relevant.

---

### FR-ADMIN-002 — Review System Health

**Priority:** Must Have

The platform must expose operational health information.

#### Acceptance Criteria

- Liveness information must be available.
- Readiness information must be available.
- Infrastructure failures must be represented safely.
- Health endpoints must not expose credentials or sensitive configuration.

---

## 14. API Behavior

### FR-API-001 — Use Consistent Error Responses

**Priority:** Must Have

The platform must return errors using a consistent response structure.

#### Initial Error Information

- Timestamp
- HTTP status
- Error code
- Safe message
- Request path
- Correlation identifier
- Validation details, when applicable

#### Acceptance Criteria

- Internal stack traces must not be exposed.
- Business errors must have stable error codes.
- Validation errors must identify invalid fields.
- Error messages must be safe for external consumers.

---

### FR-API-002 — Support Request Correlation

**Priority:** Must Have

The platform must support correlation identifiers across synchronous and asynchronous processing.

#### Acceptance Criteria

- Incoming correlation identifiers must be reused when valid.
- A new identifier must be created when one is not provided.
- Correlation identifiers must appear in structured logs.
- Correlation identifiers must be propagated to external integrations.
- Events must include traceability information.

---

### FR-API-003 — Support Pagination

**Priority:** Must Have

The platform must use consistent pagination for collection resources.

#### Acceptance Criteria

- Page size must have a configured maximum.
- Invalid page values must be rejected.
- Responses must include pagination metadata.
- Sorting must use approved fields only.

---

### FR-API-004 — Document APIs

**Priority:** Must Have

The platform must provide machine-readable and human-readable API documentation.

#### Acceptance Criteria

- OpenAPI documentation must be generated.
- Request and response schemas must be documented.
- Business error responses must be documented.
- Security requirements must be represented.
- Example payloads should be provided.

---

## 15. Traceability Matrix

The following matrix connects capabilities to initial requirements.

| Capability | Requirements |
|---|---|
| Authentication | FR-AUTH-001 to FR-AUTH-003 |
| Customer Management | FR-CUST-001 to FR-CUST-004 |
| Product Catalog | FR-PROD-001 to FR-PROD-004 |
| Order Management | FR-ORD-001 to FR-ORD-010 |
| Pricing | FR-PRICE-001 to FR-PRICE-004 |
| Approval Workflow | FR-APPR-001 to FR-APPR-005 |
| Inventory | FR-INV-001 to FR-INV-004 |
| Events | FR-EVENT-001 to FR-EVENT-003 |
| Audit | FR-AUD-001 to FR-AUD-002 |
| Notifications | FR-NOTIF-001 |
| Administration | FR-ADMIN-001 to FR-ADMIN-002 |
| API Standards | FR-API-001 to FR-API-004 |

## 16. Initial Delivery Scope

The first implementation milestone will prioritize:

- FR-AUTH-001
- FR-AUTH-002
- FR-CUST-002
- FR-CUST-003
- FR-PROD-002
- FR-PROD-003
- FR-ORD-001
- FR-ORD-002
- FR-ORD-003
- FR-ORD-004
- FR-ORD-005
- FR-ORD-007
- FR-ORD-008
- FR-PRICE-001
- FR-PRICE-002
- FR-AUD-001
- FR-API-001
- FR-API-002
- FR-API-003
- FR-API-004

The scope may evolve after domain modeling and architecture review.
