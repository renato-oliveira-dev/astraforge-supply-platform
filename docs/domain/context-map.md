# Context Map

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Context Map |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

## 1. Purpose

This document defines the initial Context Map for Enterprise Order Platform.

Its purpose is to describe:

- The relationships between bounded contexts
- Upstream and downstream responsibilities
- Integration ownership
- Dependency direction
- Strategic Domain-Driven Design relationship patterns
- Synchronous and asynchronous collaboration
- Published contracts
- Anti-Corruption Layers
- Failure isolation
- Initial integration governance

A Context Map does not describe only technical communication.

It documents how independent domain models collaborate while preserving their own terminology, rules, lifecycle, and ownership boundaries.

---

## 2. Context Map Overview

The initial platform contains the following bounded contexts:

- Order Management
- Customer
- Product Catalog
- Pricing
- Approval
- Inventory
- Fulfillment
- Payment
- Notification
- Audit
- Identity and Access

The initial high-level Context Map is:

```text
                            Identity and Access
                                  Upstream
                                     |
                           Open Host Service
                           Published Language
                                     |
                                     v
                              Order Management
                                Core Domain
                                     |
         +---------------------------+---------------------------+
         |                           |                           |
         v                           v                           v
      Customer                 Product Catalog                Pricing
      Upstream                    Upstream                    Upstream
         |                           |                           |
         |                           |                           |
         +-------------+-------------+-------------+-------------+
                       |                           |
                       v                           v
                    Approval                  Inventory
                    Downstream                Downstream
                       |                           |
                       |                           |
                       +-------------+-------------+
                                     |
                                     v
                                Fulfillment
                                Downstream
                                     |
                                     v
                                  Payment
                                  Downstream

Order Management
      |
      +----------------------> Notification
      |                         Downstream
      |
      +----------------------> Audit
      |                         Downstream
      |
      +----------------------> External Consumers
                                Downstream
```

This diagram represents initial business dependency direction.

Individual contracts may establish separate upstream and downstream relationships for specific interactions.

---

## 3. Strategic DDD Relationship Patterns

The initial Context Map uses the following strategic DDD patterns.

### 3.1 Customer-Supplier

A `Customer-Supplier` relationship exists when one context depends on another context to provide capabilities or information.

The upstream supplier should consider legitimate downstream needs without allowing unrestricted coupling.

Initial candidates:

- Customer supplying reference data to Order Management
- Product Catalog supplying Product data to Order Management
- Pricing supplying calculations to Order Management
- Order Management supplying Order facts to Approval
- Order Management supplying Reservation requests to Inventory
- Order Management supplying Fulfillment requests to Fulfillment

---

### 3.2 Open Host Service

An `Open Host Service` exposes a stable and explicit integration interface for multiple consumers.

It prevents each consumer from requiring a custom integration into the provider's internal model.

Initial candidates:

- Customer API
- Product Catalog API
- Pricing API
- Identity and Access API
- Order Integration Event API

---

### 3.3 Published Language

A `Published Language` defines a stable contract used between contexts.

It may be represented by:

- OpenAPI contracts
- Versioned event schemas
- JSON schemas
- Stable command contracts
- Stable query contracts

Initial Published Languages include:

- Customer Reference Contract
- Product Reference Contract
- Pricing Result Contract
- Approval Request Contract
- Approval Outcome Contract
- Inventory Reservation Contract
- Fulfillment Contract
- Payment Outcome Contract
- Order Integration Event Envelope

---

### 3.4 Anti-Corruption Layer

An `Anti-Corruption Layer` protects one context from the terminology, structure, and behavior of another context or external system.

Initial Anti-Corruption Layer candidates:

- External Customer integration
- External Product Catalog integration
- External Inventory system integration
- Payment provider integration
- Shipping provider integration
- Identity provider integration
- Legacy enterprise system integration

---

### 3.5 Conformist

A `Conformist` relationship exists when a downstream context adopts an upstream model because influencing the upstream is impractical.

This pattern should be used carefully.

Possible candidates:

- Identity and Access integration with a third-party identity provider
- External Payment provider contracts
- External logistics provider contracts

Even when a Conformist relationship is required externally, the platform should isolate the external model through an Anti-Corruption Layer.

---

### 3.6 Partnership

A `Partnership` relationship exists when two contexts coordinate closely and evolve together.

Initial candidates:

- Order Management and Approval
- Order Management and Inventory

Partnership should be adopted only when:

- Both teams or modules share planning
- Contract changes are coordinated
- The relationship is strategically important
- Independent evolution is not currently required

The preferred long-term direction is still explicit contracts rather than shared internal models.

---

### 3.7 Separate Ways

`Separate Ways` means two contexts remain independent and do not integrate directly.

Potential examples:

- Notification does not query Inventory
- Audit does not query Product Catalog
- Payment does not directly modify Fulfillment
- Identity and Access does not contain Order authorization rules

Separate Ways reduces unnecessary dependencies.

---

### 3.8 Shared Kernel

A `Shared Kernel` contains a deliberately shared subset of a model.

The project will avoid a business Shared Kernel by default.

Only stable and context-neutral technical primitives may be shared, such as:

- Correlation Identifier
- Clock abstraction
- Event envelope primitives
- Standard error structure
- Pagination primitives

Business entities, statuses, workflows, and persistence models must not be shared through a common module.

---

## 4. Relationship Summary

| Upstream Context | Downstream Context | Relationship Pattern | Primary Style |
|---|---|---|---|
| Identity and Access | Order Management | Open Host Service, Published Language, ACL | Synchronous |
| Customer | Order Management | Customer-Supplier, Open Host Service, Published Language, ACL | Synchronous |
| Product Catalog | Order Management | Customer-Supplier, Open Host Service, Published Language, ACL | Synchronous |
| Pricing | Order Management | Customer-Supplier, Open Host Service, Published Language | Synchronous |
| Order Management | Approval | Customer-Supplier, Partnership, Published Language | Sync and Async |
| Approval | Order Management | Published Language | Asynchronous |
| Order Management | Inventory | Customer-Supplier, Partnership, Published Language | Asynchronous |
| Inventory | Order Management | Published Language | Asynchronous |
| Order Management | Fulfillment | Customer-Supplier, Published Language | Asynchronous |
| Fulfillment | Order Management | Published Language | Asynchronous |
| Order Management | Payment | Customer-Supplier, Published Language | Sync or Async |
| Payment | Order Management | Published Language, ACL | Asynchronous |
| Order Management | Notification | Customer-Supplier, Published Language | Asynchronous |
| Order Management | Audit | Customer-Supplier, Published Language | Asynchronous |
| Order Management | External Consumers | Open Host Service, Published Language | Asynchronous |

---

## 5. Identity and Access to Order Management

### 5.1 Relationship

Identity and Access is upstream.

Order Management is downstream.

### 5.2 Pattern

- Open Host Service
- Published Language
- Anti-Corruption Layer

### 5.3 Purpose

Order Management requires authenticated identity information to:

- Identify the acting User
- Validate permissions
- Associate Customer Users with Customers
- Identify System Actors
- Record business responsibility
- Produce Audit facts

### 5.4 Published Contract

The initial User Context contract may contain:

```text
UserContext
├── userId
├── username
├── roles
├── permissions
├── customerId
├── actorType
└── correlationId
```

The contract must not expose:

- Access tokens
- Refresh tokens
- Passwords
- Identity-provider internal objects
- Framework-specific security types

### 5.5 Integration Style

Synchronous.

The API adapter validates authentication and translates the external identity representation into an internal User Context.

### 5.6 Boundary Rules

- Spring Security objects must not enter the Domain Layer.
- Order Management must not depend on identity-provider DTOs.
- Authentication and business authorization remain separate concerns.
- A Role does not automatically satisfy every business rule.
- User Context must be immutable during an application operation.
- Identity failures must be translated into stable application errors.

---

## 6. Customer to Order Management

### 6.1 Relationship

Customer is upstream.

Order Management is downstream.

### 6.2 Pattern

- Customer-Supplier
- Open Host Service
- Published Language
- Anti-Corruption Layer

### 6.3 Purpose

Order Management requires Customer information to:

- Validate Customer existence
- Validate Customer eligibility
- Obtain Customer classification
- Obtain Customer segment
- Obtain delivery information
- Create Customer Snapshots

### 6.4 Published Contract

The Customer Reference contract may contain:

```text
CustomerReferenceData
├── customerId
├── legalName
├── tradeName
├── documentNumber
├── customerType
├── customerStatus
├── classification
├── segment
├── eligibility
├── contactInformation
└── deliveryInformation
```

Only fields required by the consumer should be exposed.

### 6.5 Integration Style

Primary:

- Synchronous lookup
- Synchronous eligibility validation

Optional:

- Asynchronous Customer change events
- Local replicated reference data

### 6.6 Upstream Responsibilities

Customer must:

- Provide stable identifiers
- Provide explicit eligibility outcomes
- Version breaking contracts
- Return stable error codes
- Meet agreed latency and availability targets
- Publish relevant Customer changes when required

### 6.7 Downstream Responsibilities

Order Management must:

- Translate Customer contracts into its own model
- Preserve Customer Snapshots when required
- Avoid storing unnecessary Customer data
- Handle Customer unavailability explicitly
- Avoid reproducing Customer master-data rules
- Avoid assuming that a previously eligible Customer remains eligible indefinitely

### 6.8 Failure Behavior

Possible outcomes:

- Customer not found
- Customer not eligible
- Customer data incomplete
- Customer service unavailable
- Customer response timeout
- Customer contract incompatible

Business and technical failures must remain distinguishable.

### 6.9 Boundary Rules

- Customer persistence entities must not leave Customer Context.
- Order Management must not directly query Customer tables.
- Customer Snapshot is owned by Order Management.
- A Customer Snapshot must not be automatically refreshed after Order submission.
- External Customer models must be isolated by an Anti-Corruption Layer.

---

## 7. Product Catalog to Order Management

### 7.1 Relationship

Product Catalog is upstream.

Order Management is downstream.

### 7.2 Pattern

- Customer-Supplier
- Open Host Service
- Published Language
- Anti-Corruption Layer

### 7.3 Purpose

Order Management requires Product information to:

- Validate Product existence
- Validate Product orderability
- Resolve SKU
- Resolve descriptive information
- Resolve unit of measure
- Apply ordering restrictions
- Create Product Snapshots

### 7.4 Published Contract

The Product Reference contract may contain:

```text
ProductReferenceData
├── productId
├── sku
├── name
├── description
├── category
├── unitOfMeasure
├── status
├── orderable
└── orderingRestrictions
```

### 7.5 Integration Style

Primary:

- Synchronous Product lookup
- Synchronous batch lookup
- Synchronous orderability validation

Optional:

- Product lifecycle events
- Local replicated reference data

### 7.6 Upstream Responsibilities

Product Catalog must:

- Provide stable Product identifiers
- Provide batch operations where required
- Provide explicit Product status
- Provide explicit orderability outcomes
- Publish relevant Product changes
- Version breaking contracts

### 7.7 Downstream Responsibilities

Order Management must:

- Translate Product contracts into Product Snapshots
- Preserve historical Product information
- Avoid storing the full Product Catalog model
- Avoid reproducing Product orderability rules
- Use batch operations when validating multiple Order Items

### 7.8 Failure Behavior

Possible outcomes:

- Product not found
- Product inactive
- Product not orderable
- Product restricted for Customer segment
- Product service unavailable
- Partial batch response
- Product contract incompatible

### 7.9 Boundary Rules

- Product Catalog entities must not become Order Item entities.
- Order Management must not access Product Catalog persistence.
- Product Snapshot is owned by Order Management.
- Product changes must not mutate submitted Orders.
- Product identifiers and SKU must not be treated as interchangeable unless explicitly defined.

---

## 8. Pricing to Order Management

### 8.1 Relationship

Pricing is upstream.

Order Management is downstream.

### 8.2 Pattern

- Customer-Supplier
- Open Host Service
- Published Language

### 8.3 Purpose

Order Management requires Pricing to:

- Calculate item values
- Calculate discounts
- Calculate taxes
- Calculate fees
- Calculate freight
- Calculate Order totals
- Validate requested commercial conditions
- Produce explainable Pricing results

### 8.4 Published Request Contract

```text
PricingRequest
├── customerReference
├── customerClassification
├── customerSegment
├── currency
├── commercialDate
├── deliveryDestination
├── paymentConditions
└── items
    ├── productReference
    ├── category
    ├── quantity
    └── requestedDiscount
```

### 8.5 Published Result Contract

```text
PricingResult
├── currency
├── subtotal
├── discountTotal
├── taxTotal
├── feeTotal
├── freightTotal
├── grandTotal
├── policyVersion
├── calculatedAt
└── items
    ├── productReference
    ├── unitPrice
    ├── effectivePrice
    ├── discount
    ├── tax
    ├── fee
    └── total
```

### 8.6 Integration Style

Primary:

- Synchronous calculation

Possible future alternatives:

- Asynchronous bulk pricing
- Cached reference pricing
- Scheduled repricing
- Event-driven commercial updates

### 8.7 Upstream Responsibilities

Pricing must:

- Return deterministic results for the same effective inputs and policy version
- Define rounding rules
- Define precision and scale
- Return stable error codes
- Provide policy references
- Avoid exposing internal rule-engine models

### 8.8 Downstream Responsibilities

Order Management must:

- Translate Pricing results into Order-owned values
- Preserve accepted Pricing Snapshots
- Avoid recalculating submitted Orders automatically
- Validate that all requested items have corresponding Pricing results
- Reject incomplete or inconsistent Pricing responses

### 8.9 Failure Behavior

Possible outcomes:

- Pricing unavailable
- Unsupported Currency
- Invalid discount
- Missing Product price
- Missing tax configuration
- Incomplete Pricing result
- Pricing contract incompatible

### 8.10 Boundary Rules

- Pricing does not modify Orders.
- Order Management does not reproduce Pricing policies.
- Pricing policy version should be preserved where necessary.
- Floating-point values must not be used for monetary contracts.
- Pricing Snapshot belongs to Order Management after acceptance.

---

## 9. Order Management to Approval

### 9.1 Relationship

Order Management is upstream for Approval requests.

Approval is downstream for workflow execution.

Approval becomes upstream when publishing decisions back to Order Management.

### 9.2 Pattern

- Customer-Supplier
- Partnership
- Published Language

### 9.3 Purpose

Order Management collaborates with Approval to:

- Determine whether Approval is required
- Start Approval Processes
- Provide business facts needed by Approval policies
- Receive Approval decisions
- Continue or stop the Order lifecycle

### 9.4 Approval Evaluation Contract

```text
ApprovalEvaluationRequest
├── orderId
├── orderNumber
├── customerReference
├── customerClassification
├── customerSegment
├── grandTotal
├── discountPercentage
├── currency
├── productCategories
├── commercialExceptions
├── submittedBy
├── submittedAt
└── correlationId
```

### 9.5 Approval Evaluation Result

```text
ApprovalEvaluationResult
├── approvalRequired
├── policyReference
├── requiredLevels
└── reason
```

### 9.6 Approval Process Request

```text
StartApprovalProcess
├── approvalRequestId
├── orderId
├── orderNumber
├── approvalPolicyReference
├── approvalFacts
├── requestedAt
└── correlationId
```

### 9.7 Approval Outcome Contract

```text
ApprovalOutcome
├── eventId
├── approvalProcessId
├── orderId
├── decision
├── approvalLevel
├── decidedBy
├── decidedAt
├── comments
├── reason
├── correlationId
└── eventVersion
```

### 9.8 Integration Style

Possible initial flow:

1. Order Management synchronously evaluates whether Approval is required.
2. Order Management asynchronously starts the Approval Process.
3. Approval asynchronously publishes decisions.
4. Order Management applies a valid state transition.

### 9.9 Order Management Responsibilities

- Provide immutable Approval facts
- Avoid exposing the Order aggregate
- Preserve Approval request identifiers
- Process outcomes idempotently
- Validate that outcomes correspond to the expected Order state
- Reject stale or inconsistent outcomes

### 9.10 Approval Responsibilities

- Own Approval workflow state
- Validate approver eligibility
- Prevent duplicate decisions
- Preserve Approval history
- Publish stable outcomes
- Avoid directly modifying Order persistence

### 9.11 Failure Behavior

Possible outcomes:

- Approval policy unavailable
- Approval not required
- Approval Process creation failed
- Duplicate Approval request
- Unauthorized Approver
- Approval rejected
- Review requested
- Stale Approval outcome

### 9.12 Boundary Rules

- Approval must not call Order repositories.
- Order Management must not mutate Approval state.
- Approval Status and Order Status must remain separate.
- Approval events must represent completed facts.
- Duplicate Approval outcomes must not produce duplicate Order transitions.

---

## 10. Order Management to Inventory

### 10.1 Relationship

Order Management is upstream for Reservation requests.

Inventory is downstream for Reservation execution.

Inventory becomes upstream when publishing Reservation outcomes.

### 10.2 Pattern

- Customer-Supplier
- Partnership
- Published Language
- Anti-Corruption Layer for external stock systems

### 10.3 Purpose

Order Management collaborates with Inventory to:

- Request Product quantity Reservation
- Receive Reservation confirmation
- Receive Reservation failure
- Release reserved quantities
- React to Reservation expiration

### 10.4 Reservation Request Contract

```text
InventoryReservationRequest
├── eventId
├── reservationRequestId
├── orderId
├── orderNumber
├── requestedAt
├── expiresAt
├── correlationId
├── eventVersion
└── items
    ├── productReference
    └── requestedQuantity
```

### 10.5 Reservation Outcome Contract

```text
InventoryReservationOutcome
├── eventId
├── reservationRequestId
├── reservationId
├── orderId
├── status
├── processedAt
├── failureReason
├── correlationId
├── eventVersion
└── items
    ├── productReference
    ├── requestedQuantity
    └── reservedQuantity
```

### 10.6 Release Request Contract

```text
InventoryReleaseRequest
├── eventId
├── releaseRequestId
├── reservationId
├── orderId
├── reason
├── requestedAt
├── correlationId
└── eventVersion
```

### 10.7 Integration Style

Asynchronous.

Optional synchronous capability:

- Advisory availability check

Availability does not guarantee successful Reservation.

### 10.8 Order Management Responsibilities

- Generate a stable Reservation Request ID
- Publish requests through the Transactional Outbox
- Keep the Order in an explicit pending state
- Process outcomes idempotently
- Validate Reservation quantities
- Request release after eligible Cancellation
- Handle delayed or duplicate outcomes

### 10.9 Inventory Responsibilities

- Process Reservation requests idempotently
- Prevent duplicate stock effects
- Preserve Reservation state
- Publish explicit outcomes
- Process Release requests idempotently
- Translate external warehouse failures
- Avoid modifying Order state directly

### 10.10 Failure Behavior

Possible outcomes:

- Inventory Reserved
- Inventory Partially Reserved
- Inventory Reservation Failed
- Inventory Reservation Expired
- Duplicate Reservation Request
- External warehouse unavailable
- Unknown Product reference
- Release failed

### 10.11 Boundary Rules

- Inventory Reservation and Order are separate aggregates.
- Inventory must not depend on the internal Order model.
- Order Management must not depend on Inventory persistence models.
- Inventory outcomes must contain stable business status values.
- Duplicate events must not duplicate Reservation or Release effects.
- Partial Reservation behavior requires an explicit domain decision.

---

## 11. Order Management to Fulfillment

### 11.1 Relationship

Order Management is upstream for Fulfillment initiation.

Fulfillment is downstream for execution.

Fulfillment becomes upstream when publishing progress.

### 11.2 Pattern

- Customer-Supplier
- Published Language

### 11.3 Purpose

The relationship supports:

- Fulfillment initiation
- Shipment preparation
- Dispatch
- Delivery tracking
- Completion updates
- Fulfillment failure reporting

### 11.4 Fulfillment Request Contract

```text
FulfillmentRequest
├── eventId
├── fulfillmentRequestId
├── orderId
├── orderNumber
├── customerSnapshot
├── deliverySnapshot
├── requestedAt
├── correlationId
├── eventVersion
└── items
    ├── productReference
    ├── productSnapshot
    └── quantity
```

### 11.5 Fulfillment Outcome Contract

```text
FulfillmentOutcome
├── eventId
├── fulfillmentId
├── orderId
├── status
├── shipmentReference
├── carrierReference
├── trackingNumber
├── occurredAt
├── failureReason
├── correlationId
└── eventVersion
```

### 11.6 Integration Style

Asynchronous.

### 11.7 Boundary Rules

- Fulfillment receives snapshots and explicit contracts.
- Fulfillment does not access Order tables.
- Fulfillment Status is independent from Order Status.
- Order Management decides how Fulfillment facts affect the Order lifecycle.
- Delivery execution must not occur inside an Order transaction.
- Duplicate Fulfillment outcomes must be idempotent.

---

## 12. Order Management to Payment

### 12.1 Relationship

Order Management is upstream when registering Payment expectations.

Payment becomes upstream when publishing financial outcomes.

### 12.2 Pattern

- Customer-Supplier
- Published Language
- Anti-Corruption Layer for external providers

### 12.3 Purpose

The relationship may support:

- Payment expectation registration
- Authorization
- Confirmation
- Failure
- Cancellation
- Refund
- Reconciliation

### 12.4 Payment Request Contract

```text
PaymentRequest
├── paymentRequestId
├── orderId
├── orderNumber
├── customerReference
├── amount
├── currency
├── paymentMethod
├── requestedAt
└── correlationId
```

### 12.5 Payment Outcome Contract

```text
PaymentOutcome
├── eventId
├── paymentId
├── paymentRequestId
├── orderId
├── status
├── amount
├── currency
├── externalTransactionReference
├── occurredAt
├── failureReason
├── correlationId
└── eventVersion
```

### 12.6 Integration Style

Depending on Payment Method:

- Synchronous authorization
- Asynchronous confirmation
- Provider webhook
- Scheduled reconciliation
- Event-driven updates

### 12.7 Boundary Rules

- Payment Status and Order Status remain independent.
- Payment provider payloads must not enter Order Management.
- Payment credentials and sensitive financial data must not be exposed.
- Duplicate provider callbacks must be idempotent.
- Order Management consumes stable Payment outcomes.
- Payment must not recalculate Order Pricing unless explicitly required by a documented policy.

---

## 13. Order Management to Notification

### 13.1 Relationship

Order Management is upstream.

Notification is downstream.

### 13.2 Pattern

- Customer-Supplier
- Published Language

### 13.3 Purpose

Order Management may request Notifications for:

- Order creation
- Order submission
- Approval requirement
- Approval outcome
- Inventory failure
- Order cancellation
- Fulfillment progress
- Order completion

### 13.4 Notification Request Contract

```text
NotificationRequest
├── notificationRequestId
├── notificationType
├── businessEntityType
├── businessEntityId
├── recipients
├── templateReference
├── templateVariables
├── requestedAt
├── correlationId
└── eventVersion
```

### 13.5 Integration Style

Asynchronous.

### 13.6 Boundary Rules

- Business contexts request Notifications.
- Notification owns channel selection and delivery.
- Notification templates must not contain business decision logic.
- Notification failure must not roll back the originating business transaction.
- Sensitive data must be minimized.
- Duplicate requests must not generate uncontrolled duplicate messages.

---

## 14. Order Management to Audit

### 14.1 Relationship

Order Management is upstream.

Audit is downstream.

### 14.2 Pattern

- Customer-Supplier
- Published Language

### 14.3 Purpose

Audit records relevant business operations such as:

- Order creation
- Order submission
- Order cancellation
- Approval outcome
- Inventory outcome
- Administrative configuration changes
- Restricted data access

### 14.4 Audit Fact Contract

```text
AuditFact
├── auditFactId
├── operation
├── entityType
├── entityId
├── actor
├── occurredAt
├── correlationId
├── safeChangeDetails
└── schemaVersion
```

### 14.5 Integration Style

The originating context should reliably persist the Audit fact or related outbox record in the same local transaction when the Audit event is mandatory.

Audit processing is asynchronous.

### 14.6 Boundary Rules

- Audit Records are immutable.
- Audit does not own Order Status History.
- Audit must not query internal context tables.
- Audit facts must not expose secrets.
- Mandatory Audit facts must not be silently discarded.
- Audit operation names must be stable business terms.

---

## 15. Order Management to External Consumers

### 15.1 Relationship

Order Management is upstream.

External consumers are downstream.

### 15.2 Pattern

- Open Host Service
- Published Language

### 15.3 Purpose

Selected Order lifecycle facts may be published to:

- Analytics platforms
- Reporting systems
- Customer portals
- Data warehouses
- External enterprise systems
- Operational monitoring systems
- Partner integrations

### 15.4 Event Envelope

```text
IntegrationEventEnvelope
├── eventId
├── eventType
├── eventVersion
├── aggregateType
├── aggregateId
├── occurredAt
├── publishedAt
├── correlationId
├── causationId
├── producer
└── payload
```

### 15.5 Boundary Rules

- Event contracts must remain stable.
- Internal persistence entities must not be serialized directly.
- Sensitive data must be minimized.
- Breaking schema changes require explicit versioning.
- Consumers must assume at-least-once delivery.
- Event order must not be assumed unless the contract explicitly guarantees it.
- Not every Domain Event becomes an Integration Event.

---

## 16. Synchronous Collaboration Matrix

| Consumer | Provider | Operation | Required Result |
|---|---|---|---|
| Order Management | Identity and Access | Resolve User Context | Immediate |
| Order Management | Customer | Get Customer | Immediate |
| Order Management | Customer | Evaluate Eligibility | Immediate |
| Order Management | Product Catalog | Get Product | Immediate |
| Order Management | Product Catalog | Get Products in Batch | Immediate |
| Order Management | Product Catalog | Validate Orderability | Immediate |
| Order Management | Pricing | Calculate Pricing | Immediate |
| Order Management | Approval | Evaluate Approval Requirement | Immediate |
| Approval | Identity and Access | Validate Approver | Immediate |
| Pricing | Customer | Get Commercial Attributes | Immediate |
| Pricing | Product Catalog | Get Pricing Attributes | Immediate |
| Order Management | Inventory | Check Availability | Advisory only |
| Order Management | Payment | Authorize Payment | Conditional |

Synchronous operations require explicit resilience and latency policies.

---

## 17. Asynchronous Collaboration Matrix

| Producer | Consumer | Message |
|---|---|---|
| Order Management | Approval | ApprovalProcessRequested |
| Approval | Order Management | OrderApproved |
| Approval | Order Management | OrderRejected |
| Approval | Order Management | OrderReviewRequested |
| Order Management | Inventory | InventoryReservationRequested |
| Inventory | Order Management | InventoryReserved |
| Inventory | Order Management | InventoryReservationFailed |
| Inventory | Order Management | InventoryReservationExpired |
| Order Management | Inventory | InventoryReleaseRequested |
| Order Management | Fulfillment | FulfillmentRequested |
| Fulfillment | Order Management | FulfillmentStarted |
| Fulfillment | Order Management | OrderDispatched |
| Fulfillment | Order Management | OrderDelivered |
| Fulfillment | Order Management | FulfillmentFailed |
| Order Management | Payment | PaymentRequested |
| Payment | Order Management | PaymentConfirmed |
| Payment | Order Management | PaymentFailed |
| Order Management | Notification | NotificationRequested |
| Order Management | Audit | AuditFactRecorded |
| Order Management | External Consumers | Order lifecycle events |

---

## 18. Published Language Governance

Every published contract must define:

- Contract owner
- Consumers
- Business purpose
- Schema
- Required fields
- Optional fields
- Validation rules
- Error model
- Version
- Compatibility policy
- Security classification
- Idempotency behavior
- Retention expectations
- Deprecation strategy

### 18.1 Contract Ownership

The provider owns the contract.

Consumers may request changes through documented collaboration.

A consumer must not infer undocumented fields or provider internals.

### 18.2 Backward Compatibility

Backward-compatible changes may include:

- Adding optional fields
- Adding new non-breaking event types
- Expanding documented value ranges where safe

Potentially breaking changes include:

- Removing fields
- Renaming fields
- Changing meaning
- Changing required fields
- Changing data types
- Reusing an existing enum value with new semantics

Breaking changes require a new version or an explicit migration strategy.

### 18.3 Consumer Tolerance

Consumers should:

- Ignore unknown optional fields
- Validate required fields
- Reject incompatible versions safely
- Avoid depending on field order
- Avoid depending on undocumented values
- Handle duplicate messages

---

## 19. Anti-Corruption Layer Structure

A typical Anti-Corruption Layer may contain:

```text
integration
├── client
├── contract
│   ├── request
│   └── response
├── mapper
├── translator
├── error
├── resilience
└── adapter
```

Example:

```text
Order Management Domain
          |
          v
LoadCustomerPort
          |
          v
ExternalCustomerAdapter
          |
          +── CustomerApiClient
          +── ExternalCustomerResponse
          +── CustomerContractMapper
          +── CustomerErrorTranslator
          +── Timeout Policy
          +── Retry Policy
          +── Circuit Breaker
```

### 19.1 ACL Responsibilities

An Anti-Corruption Layer must:

- Isolate external terminology
- Translate identifiers
- Translate data structures
- Translate errors
- Normalize external inconsistencies
- Protect the internal model
- Apply resilience controls
- Preserve observability
- Prevent sensitive-data leakage

### 19.2 ACL Non-Responsibilities

An Anti-Corruption Layer must not:

- Contain core Order rules
- Reproduce the external provider's complete domain
- Hide permanent business failures as technical retries
- Automatically change invalid business data
- Convert every provider error into a generic exception
- Bypass context-owned validation

---

## 20. Error Translation

Each context must expose stable error semantics.

Example translation:

```text
External Customer API:
HTTP 404
    |
    v
Anti-Corruption Layer
    |
    v
CustomerNotFound
    |
    v
Order Application Error:
CUSTOMER_NOT_FOUND
```

Another example:

```text
External Inventory System:
Warehouse timeout
    |
    v
Inventory ACL
    |
    v
TransientInventoryFailure
    |
    v
Retry or pending Reservation state
```

Error translation must preserve the difference between:

- Validation failure
- Business rule violation
- Authentication failure
- Authorization failure
- Resource not found
- Conflict
- Transient dependency failure
- Permanent dependency failure
- Timeout
- Internal error

---

## 21. Resilience by Relationship

| Relationship | Timeout | Retry | Circuit Breaker | Idempotency |
|---|---|---|---|---|
| Identity to Order | Required | Limited | Optional | Not usually |
| Customer to Order | Required | Transient reads only | Recommended | Query safe |
| Product to Order | Required | Transient reads only | Recommended | Query safe |
| Pricing to Order | Required | Carefully evaluated | Recommended | Request key optional |
| Order to Approval | Required for sync evaluation | Limited | Recommended | Required for workflow start |
| Order to Inventory | Consumer processing timeout | Message retry | Consumer isolation | Required |
| Order to Fulfillment | Consumer processing timeout | Message retry | Consumer isolation | Required |
| Order to Payment | Required | Provider-specific | Recommended | Required |
| Order to Notification | Asynchronous processing timeout | Message retry | Provider-specific | Required |
| Order to Audit | Asynchronous processing timeout | Message retry | Consumer isolation | Required |

Retries must only apply to failures considered transient.

---

## 22. Idempotency Across Contexts

Idempotency is mandatory for asynchronous state-changing interactions.

### 22.1 Required Identifiers

Contracts may include:

- Event ID
- Command ID
- Request ID
- Reservation Request ID
- Payment Request ID
- Fulfillment Request ID
- Correlation ID
- Causation ID

### 22.2 Producer Responsibilities

The producer must:

- Generate stable identifiers
- Preserve identifiers during retries
- Avoid creating a new business request for every technical retry
- Publish through a reliable mechanism
- Record publication status when required

### 22.3 Consumer Responsibilities

The consumer must:

- Detect previously processed identifiers
- Avoid duplicate business effects
- Return or reproduce the existing result where appropriate
- Persist processing state atomically with business changes
- Define deduplication retention

### 22.4 Idempotency Scope

The scope must be explicit.

Examples:

```text
Reservation Request ID unique per Order Reservation attempt
Payment Request ID unique per intended Payment
Event ID globally unique
Cancellation Command ID unique per client operation
```

---

## 23. Event Ordering

The platform must not assume global event ordering.

Ordering may be required within a specific aggregate stream.

Example:

```text
OrderSubmitted
OrderApprovalRequired
OrderApproved
InventoryReservationRequested
InventoryReserved
OrderReadyForFulfillment
```

Possible controls include:

- Aggregate version
- Sequence number
- Partition key based on Aggregate ID
- Expected current state
- Optimistic locking
- Stale event rejection

Consumers must validate that an event is compatible with their current state.

---

## 24. Eventual Consistency

The following relationships are eventually consistent:

- Order and Approval
- Order and Inventory
- Order and Fulfillment
- Order and Payment
- Order and Notification
- Order and Audit
- Order and external consumers

Temporary states are expected.

Examples:

```text
PENDING_APPROVAL
INVENTORY_PENDING
PAYMENT_PENDING
FULFILLMENT_IN_PROGRESS
```

The user-facing system should communicate these states clearly.

Eventual consistency must not be hidden behind ambiguous generic statuses.

---

## 25. Compensating Actions

Potential compensating actions include:

| Completed Action | Later Failure or Change | Compensating Action |
|---|---|---|
| Inventory reserved | Order cancelled | Release Inventory |
| Payment authorized | Order rejected | Cancel authorization |
| Fulfillment requested | Order cancelled before dispatch | Cancel Fulfillment request |
| Notification requested | Order state superseded | Send corrective Notification if required |
| Approval completed | Order invalidated by policy | Start explicit review or cancellation process |

A compensating action is a new business operation.

It is not a distributed rollback.

Every compensation must be:

- Explicit
- Idempotent
- Auditable
- Observable
- Governed by business rules

---

## 26. Security Across Contexts

All context integrations must apply least privilege.

### 26.1 Synchronous Security

Synchronous contracts must define:

- Authentication mechanism
- Authorization scope
- Token propagation policy
- Service identity
- Correlation propagation
- Sensitive-field restrictions

### 26.2 Asynchronous Security

Asynchronous contracts must define:

- Producer identity
- Topic or queue permissions
- Message encryption requirements
- Sensitive-data classification
- Consumer authorization
- Schema validation
- Replay controls where required

### 26.3 Security Boundary Rules

- End-user tokens must not be propagated without necessity.
- Service credentials must not enter domain payloads.
- Event payloads must not contain secrets.
- Sensitive Customer or Payment data must be minimized.
- Logs must not expose credentials or tokens.
- Authorization decisions remain owned by the business capability performing the action.

---

## 27. Observability Across Contexts

All context interactions must support traceability.

Required metadata may include:

- Correlation ID
- Trace ID
- Causation ID
- Event ID
- Aggregate ID
- Request ID
- Producer
- Consumer
- Contract version
- Processing duration
- Outcome
- Retry attempt

### 27.1 Logging

Logs should use stable business language.

Preferred:

```text
Inventory reservation requested for order
Approval outcome received
Pricing calculation completed
Customer eligibility validation failed
```

Avoid:

```text
DTO sent
Object processed
Status updated
Call failed
```

### 27.2 Metrics

Possible metrics include:

- Customer validation latency
- Product lookup latency
- Pricing calculation latency
- Approval processing duration
- Reservation success rate
- Reservation failure rate
- Outbox publication lag
- Consumer retry count
- Dead-letter count
- Notification delivery rate

---

## 28. Context Dependency Rules

### 28.1 Allowed

- A context may depend on another context's published contract.
- A context may implement an outbound port using an adapter.
- A context may consume versioned Integration Events.
- A context may translate external models through an ACL.
- Infrastructure may depend on application ports.

### 28.2 Prohibited

- Direct dependency on another context's domain package
- Direct dependency on another context's infrastructure package
- Direct dependency on another context's persistence entities
- Direct access to another context's database tables
- Shared mutable domain objects
- Cross-context repository invocation
- Cyclic module dependencies
- Internal class serialization as public contracts
- Shared enum reuse across unrelated lifecycle models

---

## 29. Initial Module Dependency Direction

The conceptual dependency direction is:

```text
Inbound Adapters
      |
      v
Application Layer
      |
      v
Domain Layer
      ^
      |
Outbound Ports
      ^
      |
Infrastructure Adapters
```

For cross-context collaboration:

```text
Consumer Application Layer
      |
      v
Consumer-Owned Port
      |
      v
Integration Adapter
      |
      v
Provider Published Contract
```

The consumer must own the port representing what it needs.

The provider must not force its internal model into the consumer.

---

## 30. Architecture Enforcement

Planned ArchUnit rules include:

```text
Order domain must not depend on other bounded contexts.
```

```text
A bounded context must not access another context's infrastructure package.
```

```text
A bounded context must not access another context's persistence package.
```

```text
Domain packages must not depend on Spring, JPA, Kafka, or REST.
```

```text
Only public context contracts may be accessed across module boundaries.
```

```text
Context dependencies must not form cycles.
```

Additional controls may include:

- Java module boundaries
- Package-private implementations
- Dedicated Gradle modules
- Module-specific schemas
- Public API annotations
- Dependency analysis in CI
- Architecture decision tests

---

## 31. Initial Contract Versioning Strategy

### 31.1 REST Contracts

REST contracts will use:

- OpenAPI documentation
- Stable resource semantics
- Backward-compatible field evolution
- Explicit API versioning when breaking changes are unavoidable

Possible version strategy:

```text
/api/v1/orders
```

Versioning should not be introduced unnecessarily for every internal change.

### 31.2 Event Contracts

Integration Events will use explicit event versions.

Example:

```text
order.submitted.v1
inventory.reserved.v1
approval.completed.v1
```

### 31.3 Internal Module Contracts

Internal modular-monolith contracts should evolve through compatible Java interfaces where practical.

Breaking internal contracts still require coordinated changes and architecture review.

---

## 32. Initial Technology Mapping

The Context Map remains technology-independent.

The initial implementation may map relationships as follows:

| Relationship | Initial Technology |
|---|---|
| User Context | Spring Security adapter |
| Customer lookup | REST client |
| Product lookup | REST client |
| Pricing calculation | Internal module call or REST-style port |
| Approval evaluation | Internal module contract |
| Approval workflow | Kafka or internal event bus |
| Inventory Reservation | Kafka |
| Fulfillment initiation | Kafka |
| Payment outcomes | Kafka or webhook adapter |
| Notification requests | Kafka |
| Audit facts | Transactional Outbox and Kafka |
| External Order events | Transactional Outbox and Kafka |

An internal module call must still use an explicit context contract.

It must not bypass boundaries merely because both contexts run in the same process.

---

## 33. Initial Deployment Interpretation

During the modular-monolith phase:

- Contexts share one deployable application.
- Contexts may share one PostgreSQL instance.
- Contexts may communicate through in-process ports.
- Asynchronous flows may use Kafka.
- Context-owned schemas and repositories remain isolated.
- Public contracts remain explicit.
- Architecture tests protect dependencies.

Possible future deployment:

```text
Order Service
Customer Service
Product Service
Pricing Service
Approval Service
Inventory Service
Notification Service
```

Extraction is optional and requirement-driven.

The Context Map should remain valid even when deployment topology changes.

---

## 34. Open Context Map Questions

The following questions require future decisions:

1. Is Pricing initially an internal module or an external service?
2. Is Customer data owned by the platform or always external?
3. Is Product Catalog owned by the platform or always external?
4. Should Customer and Product reference data be replicated locally?
5. Is Approval evaluation synchronous?
6. Is Approval workflow always asynchronous?
7. Should Approval and Order Management use Partnership initially?
8. Does Inventory support partial Reservation?
9. What ordering guarantees are required for Inventory events?
10. Is Fulfillment included in the first operational release?
11. Is Payment required before Fulfillment?
12. Which Payment providers require a Conformist relationship?
13. Which external systems require dedicated Anti-Corruption Layers?
14. Which Domain Events become public Integration Events?
15. Which consumers need Order snapshots in event payloads?
16. Which interactions require Causation ID?
17. What is the deduplication retention period for each consumer?
18. What is the retry policy for each asynchronous contract?
19. Which events require dead-letter handling?
20. Which relationships require formal consumer-driven contract tests?
21. Should contexts use separate PostgreSQL schemas from the first release?
22. Which contracts require independent semantic versioning?
23. Which context is the first candidate for microservice extraction?
24. Is Notification a bounded context or an infrastructure capability?
25. Should Audit consume business events or dedicated Audit facts?

---

## 35. Decision Summary

The initial Context Map establishes that:

- Order Management is the Core Domain.
- Customer, Product Catalog, Pricing, and Identity are upstream providers.
- Approval, Inventory, Fulfillment, Notification, Audit, and external consumers are downstream from Order Management for initiated processes.
- Approval, Inventory, Fulfillment, and Payment become upstream when publishing outcomes.
- Cross-context communication uses explicit Published Languages.
- Provider internals must not leak into consumer models.
- External systems are isolated through Anti-Corruption Layers.
- Shared Kernel usage is minimized.
- Synchronous communication is used for immediate queries and validations.
- Asynchronous communication is used for long-running processes and completed facts.
- The Transactional Outbox Pattern protects reliable Integration Event publication.
- Asynchronous consumers must be idempotent.
- Global event ordering is not assumed.
- Eventual consistency is represented through explicit business states.
- Compensating actions are explicit business operations.
- Context boundaries are enforced even inside the modular monolith.
- Microservice extraction remains optional and requirement-driven.

---

## 36. Next Documentation Step

The next document will define the primary aggregate of the Core Domain:

```text
docs/domain/order-aggregate.md
```

It will establish:

- Aggregate boundary
- Aggregate root
- Entities
- Value objects
- Invariants
- Domain behaviors
- State transitions
- Domain events
- Persistence considerations
- Concurrency control
- Creation, submission, cancellation, and lifecycle rules
