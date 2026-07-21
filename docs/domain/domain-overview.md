# Domain Overview

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Domain Overview |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

## 1. Purpose

This document provides an initial overview of the business domain modeled by Enterprise Order Platform.

Its purpose is to establish:

- The core business problem
- The main business capabilities
- The domain boundaries
- The primary actors
- The main domain concepts
- The order lifecycle
- The relationships between business areas
- The initial candidates for bounded contexts
- The architectural direction for domain modeling

This document does not define the complete implementation model.

Detailed aggregates, entities, value objects, domain services, events, invariants, and context relationships will be documented in subsequent artifacts.

---

## 2. Domain Summary

Enterprise Order Platform manages the lifecycle of B2B orders submitted by corporate customers.

The platform coordinates several business capabilities required to process an order safely and consistently, including:

- Customer eligibility validation
- Product availability validation
- Pricing calculation
- Discount validation
- Approval workflows
- Inventory reservation
- Order fulfillment preparation
- Payment tracking
- Shipment tracking
- Notifications
- Audit history
- Integration event publishing

The Order domain acts as the central business area.

Other capabilities provide information or execute processes required for an order to progress through its lifecycle.

---

## 3. Core Business Problem

Enterprise order processing involves more than recording a purchase request.

Before an order can be completed, the platform must ensure that:

- The customer exists and is eligible to place orders
- The selected products are valid and available for ordering
- Prices, discounts, taxes, fees, and freight are calculated correctly
- The order complies with commercial policies
- Required approvals are completed
- Inventory is reserved
- Relevant business events are published
- External systems receive consistent information
- Every significant business operation is traceable

The platform must coordinate these rules while remaining reliable under concurrent access, temporary integration failures, duplicate requests, and asynchronous message delivery.

---

## 4. Domain Classification

The domain is initially classified into three categories.

### 4.1 Core Domain

The Core Domain represents the primary business value of the platform.

#### Order Management

Order Management is responsible for:

- Creating orders
- Managing order items
- Calculating order totals
- Validating order consistency
- Submitting orders
- Controlling order status transitions
- Cancelling eligible orders
- Preserving order history
- Coordinating the order lifecycle

This is the most strategically important domain area.

Most architectural and modeling effort should be concentrated here.

---

### 4.2 Supporting Domains

Supporting domains provide capabilities required by the Core Domain.

#### Approval Management

Responsible for:

- Determining whether an order requires approval
- Selecting the applicable approval policy
- Assigning approval responsibility
- Recording approval decisions
- Supporting rejection and review requests
- Managing multiple approval levels when necessary

#### Inventory Management

Responsible for:

- Validating inventory availability
- Reserving inventory
- Confirming reservations
- Handling insufficient inventory
- Releasing reservations
- Preventing duplicate inventory effects

#### Pricing Management

Responsible for:

- Determining effective prices
- Applying discount policies
- Calculating taxes and fees
- Calculating freight
- Producing order totals
- Preserving pricing snapshots

#### Fulfillment Management

Responsible for:

- Preparing approved orders for fulfillment
- Coordinating shipment preparation
- Tracking fulfillment status
- Receiving delivery-related updates

#### Payment Management

Responsible for:

- Registering payment expectations
- Tracking payment status
- Recording payment confirmation
- Associating payments with orders
- Handling payment failures or pending conditions

---

### 4.3 Generic Subdomains

Generic subdomains solve common technical or enterprise problems.

#### Customer Reference Management

Responsible for:

- Providing customer reference data
- Identifying customer status
- Providing customer classification
- Supporting customer eligibility checks

#### Product Catalog

Responsible for:

- Providing product reference data
- Identifying product status
- Providing SKU and descriptive information
- Supporting product ordering validation

#### Identity and Access Management

Responsible for:

- Authenticating users
- Providing user identity
- Enforcing roles and permissions
- Supplying authenticated user context

#### Notification Management

Responsible for:

- Requesting email or other notifications
- Handling notification templates
- Retrying failed notification requests
- Tracking notification delivery when required

#### Audit Management

Responsible for:

- Recording relevant business operations
- Preserving immutable audit records
- Identifying the acting user or system
- Supporting traceability and investigation

---

## 5. Primary Actors

### 5.1 Customer User

A user associated with a corporate customer.

Typical responsibilities:

- Create draft orders
- Add or remove order items
- Review order totals
- Submit orders
- View order status
- Request cancellation when permitted

---

### 5.2 Sales Representative

A user responsible for assisting customers or managing orders on their behalf.

Typical responsibilities:

- Create orders for customers
- Adjust permitted commercial information
- Review customer eligibility
- Submit orders
- Monitor order progress
- Respond to review requests

---

### 5.3 Approver

A user authorized to evaluate orders that require approval.

Typical responsibilities:

- Review pending orders
- Approve orders
- Reject orders
- Request additional information or correction
- Record comments and decision reasons

---

### 5.4 Operations User

A user responsible for operational order processing.

Typical responsibilities:

- Monitor inventory reservation
- Monitor fulfillment
- Track integration failures
- Resolve operational exceptions
- Review processing status

---

### 5.5 Administrator

A user responsible for controlled administrative configuration.

Typical responsibilities:

- Maintain configurable parameters
- Manage approval thresholds
- Configure operational policies
- Review system health
- Access restricted audit information

---

### 5.6 External System

An external application that exchanges information with the platform.

Examples:

- Customer system
- Product system
- Pricing service
- Inventory system
- Payment service
- Shipping provider
- Notification service
- Identity provider

---

## 6. Main Domain Concepts

### 6.1 Order

An Order represents a customer's intention to acquire products under defined commercial conditions.

An order contains:

- A unique identifier
- Customer reference
- Current lifecycle status
- Order items
- Pricing totals
- Customer snapshot
- Submission information
- Approval information
- Cancellation information
- Version information
- Creation and modification timestamps

The Order is expected to be the main aggregate root of the Core Domain.

---

### 6.2 Order Item

An Order Item represents a product and quantity included in an order.

An order item may contain:

- Product identifier
- Product snapshot
- SKU
- Product name
- Quantity
- Unit price
- Discount
- Tax
- Fees
- Item subtotal
- Item total

Order items exist within the Order aggregate boundary.

They must not be modified independently from the Order.

---

### 6.3 Customer

A Customer represents the corporate organization placing or receiving an order.

The platform may not own the complete customer master data.

Instead, it stores customer references and order-specific customer snapshots required to preserve historical integrity.

Relevant information may include:

- Customer identifier
- Legal name
- Trade name
- Document number
- Customer type
- Status
- Eligibility
- Contact information
- Delivery information

---

### 6.4 Product

A Product represents an item that may be included in an order.

The platform may consume product reference data from an external catalog.

Relevant information may include:

- Product identifier
- SKU
- Name
- Description
- Unit of measure
- Product status
- Base price
- Category
- Ordering restrictions

---

### 6.5 Pricing

Pricing represents the commercial calculation associated with an order.

It may include:

- Unit price
- Item subtotal
- Discounts
- Taxes
- Fees
- Freight
- Order subtotal
- Grand total
- Currency
- Rounding rules

Pricing information must be preserved when the order is submitted.

Future changes to catalog prices must not alter historical orders.

---

### 6.6 Approval

An Approval represents a business decision required before an order may continue.

Approval information may include:

- Approval level
- Approval status
- Responsible role
- Responsible user
- Decision date
- Decision comments
- Rejection reason
- Review reason

Approval rules may depend on:

- Order total
- Discount percentage
- Customer classification
- Product category
- Commercial exceptions
- Business segment

---

### 6.7 Inventory Reservation

An Inventory Reservation represents a request to reserve product quantities for an order.

It may include:

- Reservation identifier
- Order identifier
- Product identifier
- Requested quantity
- Reservation status
- External reference
- Expiration date
- Failure reason
- Idempotency key

Inventory reservation may be processed asynchronously.

The platform must assume at-least-once message delivery and duplicate processing attempts.

---

### 6.8 Shipment

A Shipment represents the physical delivery process related to an order.

It may contain:

- Shipment identifier
- Order identifier
- Carrier
- Tracking information
- Shipment status
- Dispatch date
- Estimated delivery date
- Delivery date

Shipment is not part of the initial implementation milestone but remains within the broader domain vision.

---

### 6.9 Payment

A Payment represents financial settlement information associated with an order.

It may contain:

- Payment identifier
- Order identifier
- Payment method
- Amount
- Payment status
- External transaction reference
- Confirmation date
- Failure reason

Payment is not part of the initial implementation milestone but remains within the broader domain vision.

---

### 6.10 Audit Record

An Audit Record represents an immutable trace of a relevant business operation.

It may contain:

- Audit identifier
- Operation type
- Entity type
- Entity identifier
- Acting user or system
- Timestamp
- Correlation identifier
- Safe change details

Audit records must not expose credentials, tokens, or unnecessary sensitive data.

---

## 7. Order Lifecycle

The initial order lifecycle is expected to include the following states:

```text
DRAFT
  |
  v
SUBMITTED
  |
  +------------------------+
  |                        |
  v                        v
PENDING_APPROVAL        PROCESSING
  |                        |
  +---------+              |
  |         |              |
  v         v              |
APPROVED  REJECTED         |
  |                        |
  v                        |
PROCESSING <---------------+
  |
  v
INVENTORY_PENDING
  |
  +---------------------------+
  |                           |
  v                           v
INVENTORY_RESERVED     INVENTORY_FAILED
  |
  v
READY_FOR_FULFILLMENT
  |
  v
FULFILLMENT_IN_PROGRESS
  |
  v
COMPLETED
```

Cancellation may be permitted from selected states.

The final state machine will be refined during aggregate modeling.

---

## 8. Initial State Descriptions

### DRAFT

The order is editable.

Items may be added, updated, or removed.

No approval or inventory reservation has started.

---

### SUBMITTED

The customer or sales representative has submitted the order.

The order has passed initial validation and is no longer freely editable.

---

### PENDING_APPROVAL

The order requires one or more business approvals.

Processing cannot continue until the required decision is completed.

---

### APPROVED

The required approval was granted.

The order may continue to operational processing.

This may be a transient state depending on the implementation.

---

### REJECTED

The approval request was rejected.

The order cannot proceed without a new business process or explicit review flow.

---

### PROCESSING

The order is being prepared for downstream processing.

This may include final validation, event preparation, and inventory reservation initiation.

---

### INVENTORY_PENDING

An inventory reservation request was created and is awaiting a result.

---

### INVENTORY_RESERVED

Required inventory was successfully reserved.

---

### INVENTORY_FAILED

The inventory reservation could not be completed.

The order cannot continue to fulfillment while this condition remains unresolved.

---

### READY_FOR_FULFILLMENT

The order completed its required validations, approvals, and inventory reservation.

It is ready to enter the fulfillment process.

---

### FULFILLMENT_IN_PROGRESS

The order is being prepared, dispatched, or delivered.

---

### COMPLETED

The business process associated with the order has been completed.

The order is no longer eligible for normal modification.

---

### CANCELLED

The order was cancelled according to the applicable business rules.

Cancellation may require:

- A cancellation reason
- Identification of the responsible user
- Inventory release
- Event publication
- Audit registration

---

## 9. Initial Business Invariants

The following invariants are initial candidates for enforcement within the domain model.

### Order Invariants

- An order must have a valid customer reference.
- An order must contain at least one item before submission.
- A submitted order must not be freely edited.
- A completed order must not be cancelled.
- An order item must belong to exactly one order.
- Order totals must be consistent with item totals.
- Order status transitions must follow explicit domain rules.
- Every state-changing operation must preserve aggregate consistency.

### Order Item Invariants

- Quantity must be greater than zero.
- Unit price must not be negative.
- Discounts must not produce an invalid negative total.
- Product identity must remain stable after submission.
- Pricing information must become immutable after submission.

### Approval Invariants

- Only eligible users may approve or reject an order.
- An approval decision must reference the responsible user.
- A rejected order must contain a rejection reason.
- The same approval level must not be completed more than once.

### Inventory Invariants

- A reservation request must reference a valid order.
- Duplicate requests must not reserve inventory more than once.
- Released inventory must not be released repeatedly.
- Inventory confirmation must reference the original request.

---

## 10. Consistency Boundaries

The Order aggregate will protect business consistency for:

- Order status
- Order items
- Customer snapshot
- Product snapshots
- Pricing totals
- Submission state
- Cancellation state
- Order version

The following capabilities are expected to remain outside the Order aggregate:

- Customer master data
- Product master data
- Inventory stock levels
- Payment processing
- Shipment execution
- Notification delivery
- Complete audit storage

These external capabilities will interact with Order Management through explicit application ports, integration contracts, or domain events.

---

## 11. Transactional Boundaries

A single local transaction may include:

- Updating an Order aggregate
- Persisting order status history
- Persisting domain event records
- Persisting transactional outbox records
- Persisting idempotency information when required

A local transaction should not include long-running calls to:

- Inventory systems
- Payment providers
- Shipping providers
- Notification systems
- Other remote services

Remote collaboration should occur outside the local database transaction.

Where strong distributed consistency is unavailable, the platform will use eventual consistency and compensating actions where necessary.

---

## 12. Domain Events

Initial domain event candidates include:

- OrderCreated
- OrderItemAdded
- OrderItemUpdated
- OrderItemRemoved
- OrderSubmitted
- OrderApprovalRequired
- OrderApproved
- OrderRejected
- OrderReviewRequested
- InventoryReservationRequested
- InventoryReserved
- InventoryReservationFailed
- InventoryReleaseRequested
- OrderReadyForFulfillment
- OrderCancelled
- OrderCompleted

Domain events represent relevant facts that occurred within the domain.

Not every domain event must be published externally.

Integration events will be derived from selected domain events according to integration requirements.

---

## 13. Initial Bounded Context Candidates

The following bounded contexts are initial candidates.

### Order Management Context

Responsibilities:

- Order creation
- Item management
- Submission
- Order lifecycle
- Cancellation
- Order history
- Order snapshots
- Coordination of order processing

Classification:

- Core Domain

---

### Pricing Context

Responsibilities:

- Price calculation
- Discount policies
- Tax calculation
- Fee calculation
- Freight calculation
- Pricing result production

Classification:

- Supporting Domain

---

### Approval Context

Responsibilities:

- Approval policy selection
- Approval workflow
- Approval levels
- Approval decisions
- Rejection and review

Classification:

- Supporting Domain

---

### Inventory Context

Responsibilities:

- Reservation request
- Reservation confirmation
- Reservation failure
- Inventory release
- Reservation idempotency

Classification:

- Supporting Domain

---

### Customer Context

Responsibilities:

- Customer reference data
- Customer status
- Customer classification
- Customer eligibility information

Classification:

- Generic or Supporting Domain, depending on platform ownership

---

### Product Catalog Context

Responsibilities:

- Product reference data
- SKU management
- Product status
- Ordering restrictions
- Product descriptive information

Classification:

- Generic Domain

---

### Fulfillment Context

Responsibilities:

- Fulfillment preparation
- Shipment coordination
- Dispatch status
- Delivery tracking

Classification:

- Supporting Domain

---

### Payment Context

Responsibilities:

- Payment registration
- Payment status
- Payment confirmation
- Payment failure tracking

Classification:

- Supporting Domain

---

### Notification Context

Responsibilities:

- Notification request
- Template selection
- Delivery coordination
- Delivery status

Classification:

- Generic Domain

---

### Audit Context

Responsibilities:

- Business audit records
- Operational traceability
- Historical access
- Audit retention

Classification:

- Generic Domain

---

### Identity and Access Context

Responsibilities:

- Authentication
- User identity
- Roles and permissions
- Access control information

Classification:

- Generic Domain

---

## 14. Context Interaction Overview

The initial interaction model is:

```text
Customer Context
       |
       v
Order Management <-------- Product Catalog
       |
       +--------> Pricing
       |
       +--------> Approval
       |
       +--------> Inventory
       |
       +--------> Fulfillment
       |
       +--------> Payment
       |
       +--------> Audit
       |
       +--------> Notification
       |
       +--------> Integration Events
```

Order Management is the central orchestrator of the business lifecycle.

However, it must not own the internal models of other bounded contexts.

Each context should expose explicit contracts for the information or behavior it provides.

---

## 15. Context Dependency Principles

The following dependency principles should guide the model:

- A bounded context must not access another context's database tables directly.
- A context must not use another context's internal persistence entities.
- Cross-context communication must use explicit contracts.
- Domain terminology may differ between contexts when the business meaning differs.
- Shared kernels should be avoided unless the shared model is stable and genuinely common.
- Integration contracts must not expose unnecessary internal details.
- Context relationships must be documented before implementation.
- Synchronous integration should be used when an immediate response is required.
- Asynchronous integration should be preferred for completed facts and long-running processes.
- Event consumers must be idempotent.

---

## 16. Ubiquitous Language Principles

The project must establish a consistent business vocabulary.

The same domain concept should use the same name in:

- Requirements
- Domain documentation
- Source code
- Tests
- API contracts
- Event contracts
- Logs
- Architecture documentation

Technical names should not replace established business terminology.

Examples:

| Preferred Term | Avoid |
|---|---|
| Order | Request, transaction, generic process |
| Order Item | Detail, row, line entity |
| Submit Order | Save final order |
| Approve Order | Update approval flag |
| Inventory Reservation | Stock lock |
| Customer Eligibility | Customer validation generic flag |
| Order Status | Processing code |
| Cancellation Reason | Observation field |

A dedicated ubiquitous language document will refine the official vocabulary.

---

## 17. Modeling Principles

The domain model should favor:

- Behavior-rich aggregates
- Explicit state transitions
- Immutable value objects
- Business-specific exceptions
- Domain events
- Explicit invariants
- Controlled aggregate modification
- Dependency-free domain code
- Small transactional boundaries
- Clear context ownership

The domain model should avoid:

- Anemic entities with only getters and setters
- Public setters for business-critical state
- Status changes performed directly by controllers
- Business rules embedded in persistence adapters
- Framework annotations controlling domain behavior
- Generic utility services containing unrelated rules
- Large aggregates containing external context data
- Distributed transactions across external services

---

## 18. Initial Aggregate Candidates

### Order Aggregate

Aggregate root:

- Order

Internal elements:

- OrderItem
- CustomerSnapshot
- ProductSnapshot
- PricingSummary
- OrderStatusHistory
- CancellationDetails

Possible value objects:

- OrderId
- CustomerId
- ProductId
- Quantity
- Money
- Percentage
- Currency
- OrderNumber
- CorrelationId

---

### Approval Aggregate

Possible aggregate root:

- ApprovalProcess

Internal elements:

- ApprovalStep
- ApprovalDecision
- ApprovalPolicyReference

Possible value objects:

- ApprovalProcessId
- ApprovalLevel
- ApproverId
- ApprovalComment
- RejectionReason

The final ownership of approval information will be refined during bounded-context modeling.

---

### Inventory Reservation Aggregate

Possible aggregate root:

- InventoryReservation

Internal elements:

- ReservationItem
- ReservationStatus
- ReservationFailure

Possible value objects:

- ReservationId
- ReservationRequestId
- InventoryReference
- ReservedQuantity
- ReservationExpiration

---

## 19. Initial Integration Strategy

The platform will use a combination of synchronous and asynchronous communication.

### Synchronous Communication

Suitable for:

- Customer eligibility lookup
- Product lookup
- Immediate pricing calculation
- Authentication and authorization data
- Queries that require an immediate response

Expected controls:

- Connection timeout
- Response timeout
- Retry for transient failures
- Circuit breaker where appropriate
- Correlation identifier propagation
- Safe error mapping

---

### Asynchronous Communication

Suitable for:

- Inventory reservation requests
- Order lifecycle events
- Notifications
- Audit integration
- Fulfillment initiation
- Payment updates
- External system synchronization

Expected controls:

- Transactional outbox
- Idempotent consumers
- Retry policy
- Dead-letter handling
- Event versioning
- Correlation metadata
- Duplicate detection

---

## 20. Open Domain Questions

The following questions must be resolved during the next modeling stages:

1. Does pricing belong entirely to an independent bounded context or partially inside Order Management?
2. Is approval a separate aggregate or part of the Order aggregate lifecycle?
3. Can an order have multiple customer delivery destinations?
4. Can the same product appear multiple times with different commercial conditions?
5. Is inventory reserved per order or per order item?
6. Does inventory reservation expire automatically?
7. Can an order proceed with partial inventory reservation?
8. Can rejected orders return to draft or review status?
9. Are multiple approval levels sequential or parallel?
10. Can cancellation occur after fulfillment begins?
11. Which data must be preserved in customer and product snapshots?
12. Is payment required before fulfillment?
13. Are shipment and payment capabilities part of the initial platform or future integrations?
14. Which domain events must become public integration events?
15. Which operations require explicit idempotency keys from API clients?

These questions will be refined through future ADRs, domain modeling, and implementation decisions.

---

## 21. Initial Modeling Scope

The first domain implementation milestone will focus on:

- Order aggregate
- Order item management
- Customer reference validation
- Product reference validation
- Pricing totals
- Order submission
- Order status transition rules
- Order cancellation
- Order status history
- Audit event generation
- Domain event generation

The following capabilities will initially be represented through ports or simplified contracts:

- Approval
- Inventory
- Notifications
- External customer data
- External product data

Shipment and payment capabilities will remain outside the first implementation milestone.

---

## 22. Next Documentation Steps

The domain documentation will evolve through the following artifacts:

1. Ubiquitous Language
2. Bounded Contexts
3. Context Map
4. Order Aggregate
5. Domain Events Catalog
6. Order State Machine
7. Business Rules Catalog
8. Domain Service Responsibilities

The next document will define the official business vocabulary used throughout the project.
