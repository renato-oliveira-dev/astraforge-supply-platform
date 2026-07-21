# Bounded Contexts

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Bounded Contexts |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

## 1. Purpose

This document defines the initial bounded contexts of Enterprise Order Platform.

Its purpose is to establish:

- Business capability ownership
- Context responsibilities
- Domain model boundaries
- Data ownership
- Included and excluded concepts
- Upstream and downstream relationships
- Synchronous and asynchronous interactions
- Dependency rules
- Initial modular boundaries
- Future evolution paths

A bounded context defines an explicit boundary within which a domain model, terminology, rules, and data ownership remain consistent.

This document focuses on strategic Domain-Driven Design.

Detailed aggregates, entities, value objects, domain services, events, commands, queries, and technical components will be defined in subsequent documents.

---

## 2. Architectural Context

Enterprise Order Platform will initially be implemented as a modular monolith.

Each bounded context will be represented as an explicit application module with:

- Its own business model
- Its own application services
- Its own persistence abstractions
- Its own infrastructure adapters
- Explicit public contracts
- Controlled dependencies
- Independent tests
- Clear ownership of business terminology

Although the contexts will initially run within the same deployable application, they must not behave as an unrestricted shared codebase.

The modular monolith must preserve boundaries that would allow selected contexts to evolve into independently deployable services if future business or operational requirements justify that transition.

---

## 3. Context Landscape

The initial context landscape contains the following bounded contexts:

```text
                          Identity and Access
                                   |
                                   v
                           Order Management
                       Core Domain and Orchestrator
                                   |
          +------------------------+------------------------+
          |                        |                        |
          v                        v                        v
      Customer               Product Catalog             Pricing
          |                        |                        |
          +------------------------+------------------------+
                                   |
                                   v
                                Approval
                                   |
                                   v
                               Inventory
                                   |
                                   v
                              Fulfillment
                                   |
                                   v
                                Payment

Order lifecycle facts
          |
          +------------------> Notification
          |
          +------------------> Audit
          |
          +------------------> Integration Events
```

This diagram represents business collaboration rather than direct technical coupling.

Detailed relationship patterns will be defined in the Context Map.

---

## 4. Context Classification

| Bounded Context | Classification | Initial Priority |
|---|---|---|
| Order Management | Core Domain | High |
| Pricing | Supporting Domain | High |
| Approval | Supporting Domain | High |
| Inventory | Supporting Domain | High |
| Customer | Generic or Supporting Domain | High |
| Product Catalog | Generic Domain | High |
| Fulfillment | Supporting Domain | Medium |
| Payment | Supporting Domain | Medium |
| Notification | Generic Domain | Medium |
| Audit | Generic Domain | High |
| Identity and Access | Generic Domain | High |

The classification may evolve as the business model becomes more detailed.

In particular, Customer and Pricing may become strategically more important depending on future platform ownership and commercial complexity.

---

## 5. General Boundary Principles

All bounded contexts must follow these principles:

- Each context owns its domain model.
- Each context owns its business rules.
- Each context owns its persistence schema or logical persistence boundary.
- One context must not manipulate another context's internal entities.
- One context must not access another context's repositories directly.
- Cross-context communication must use explicit contracts.
- Internal persistence entities must not be exposed across contexts.
- Shared database access must not replace proper integration.
- Domain events must not expose internal implementation details.
- Integration contracts must be versioned when required.
- Context-specific terminology must remain explicit.
- Context dependencies must be directional and documented.
- Cyclic dependencies between contexts must be prevented.
- Consumers must translate external models into their own internal language.
- Event consumers must be idempotent.
- Remote or asynchronous failures must be modeled explicitly.
- Technical frameworks must not define the business boundaries.

---

## 6. Order Management Context

### 6.1 Classification

Core Domain.

### 6.2 Strategic Importance

Order Management contains the primary business value of the platform.

It owns the lifecycle of enterprise orders and coordinates the business capabilities required for an order to progress from draft creation to completion or cancellation.

Most domain modeling effort should be concentrated in this context.

### 6.3 Responsibilities

Order Management is responsible for:

- Creating Orders
- Managing Draft Orders
- Adding Order Items
- Updating Order Item quantities
- Removing Order Items
- Maintaining Customer References
- Preserving Customer Snapshots
- Maintaining Product References
- Preserving Product Snapshots
- Applying Pricing results
- Preserving Pricing Snapshots
- Validating Order consistency
- Submitting Orders
- Determining the next lifecycle action
- Controlling Order Status transitions
- Requesting Approval evaluation
- Reacting to Approval decisions
- Requesting Inventory Reservation
- Reacting to Inventory Reservation results
- Preparing Orders for Fulfillment
- Cancelling eligible Orders
- Recording Order Status History
- Producing Domain Events
- Producing Integration Event candidates
- Enforcing Order invariants
- Supporting Order queries

### 6.4 Owned Concepts

Order Management owns:

- Order
- Order Item
- Order ID
- Order Number
- Order Status
- Order Status History
- Customer Reference
- Customer Snapshot
- Product Reference
- Product Snapshot
- Pricing Snapshot
- Pricing Summary
- Submission Details
- Cancellation Details
- Order Version
- Order-related Domain Events

### 6.5 Excluded Concepts

Order Management does not own:

- Current Customer master data
- Current Product master data
- Inventory stock balances
- Inventory reservation internals
- Approval policy internals
- Payment transaction internals
- Shipment execution internals
- Notification delivery
- Authentication credentials
- User account management
- Complete audit storage
- Kafka or messaging infrastructure details

### 6.6 Primary Actors

- Customer User
- Sales Representative
- Approver, indirectly through Approval
- Operations User
- Administrator
- External systems

### 6.7 Public Capabilities

Initial public capabilities include:

- Create Order
- Get Order
- Search Orders
- Add Order Item
- Update Order Item
- Remove Order Item
- Recalculate Order
- Submit Order
- Cancel Order
- Get Order Status History

### 6.8 Incoming Information

Order Management may receive:

- Authenticated User Context
- Customer eligibility results
- Customer reference data
- Product orderability results
- Product reference data
- Pricing results
- Approval decisions
- Inventory reservation results
- Fulfillment updates
- Payment updates

### 6.9 Outgoing Information

Order Management may produce:

- Approval evaluation requests
- Inventory Reservation requests
- Inventory Release requests
- Fulfillment preparation requests
- Notification requests
- Audit facts
- Order lifecycle Integration Events

### 6.10 Consistency Boundary

The Order aggregate is expected to protect consistency for:

- Order Status
- Order Items
- Customer Snapshot
- Product Snapshots
- Pricing Snapshot
- Submission state
- Cancellation state
- Aggregate version
- Domain event generation

### 6.11 Initial Interaction Style

| Provider Context | Interaction |
|---|---|
| Identity and Access | Synchronous context acquisition |
| Customer | Synchronous lookup and eligibility validation |
| Product Catalog | Synchronous lookup and orderability validation |
| Pricing | Synchronous calculation during draft operations and submission |
| Approval | Synchronous evaluation or asynchronous workflow initiation |
| Inventory | Asynchronous reservation and release |
| Fulfillment | Asynchronous initiation and status updates |
| Payment | Asynchronous status updates or synchronous validation when required |
| Notification | Asynchronous |
| Audit | Local event capture followed by asynchronous processing |

### 6.12 Boundary Rules

- External Customer objects must not enter the Order aggregate directly.
- External Product objects must not enter the Order aggregate directly.
- Pricing results must be translated into Order-owned pricing values.
- Approval internals must not be embedded inside the Order aggregate unless explicitly adopted by a future modeling decision.
- Inventory Reservation entities must not be shared with Order Management.
- Order state transitions must occur only through Order domain behavior.
- Controllers must not modify Order Status directly.
- Persistence adapters must not contain Order business rules.

---

## 7. Customer Context

### 7.1 Classification

Generic or Supporting Domain.

The final classification depends on whether Enterprise Order Platform owns Customer master data or only integrates with an external Customer system.

### 7.2 Responsibilities

Customer Context is responsible for:

- Maintaining Customer reference information
- Providing Customer lookup
- Providing Customer status
- Providing Customer classification
- Evaluating Customer eligibility
- Providing delivery-related Customer data
- Providing commercial attributes required for Order processing
- Exposing stable Customer contracts

### 7.3 Owned Concepts

Customer Context may own:

- Customer
- Customer ID
- Legal Name
- Trade Name
- Document Number
- Customer Status
- Customer Type
- Customer Classification
- Customer Eligibility
- Customer Segment
- Customer Address
- Customer Contact
- Delivery Destination

### 7.4 Excluded Concepts

Customer Context does not own:

- Orders
- Order lifecycle
- Product information
- Pricing calculations
- Inventory
- Approval decisions
- Payments
- Shipments

### 7.5 Public Capabilities

- Get Customer
- Validate Customer Existence
- Evaluate Customer Eligibility
- Get Customer Classification
- Get Delivery Information
- Get Customer Segment

### 7.6 Consumers

Initial consumers include:

- Order Management
- Pricing
- Approval
- Payment
- Fulfillment

### 7.7 Integration Style

The initial interaction is primarily synchronous because Order creation and submission may require immediate Customer validation.

Customer changes may also be propagated asynchronously through events such as:

- CustomerUpdated
- CustomerStatusChanged
- CustomerEligibilityChanged

### 7.8 Boundary Rules

- Order Management stores Customer References and Customer Snapshots, not the complete Customer aggregate.
- Customer eligibility rules belong to Customer Context unless they are specific to Order submission.
- Customer persistence entities must not be reused by other contexts.
- Customer contract changes must preserve backward compatibility or use explicit versioning.

---

## 8. Product Catalog Context

### 8.1 Classification

Generic Domain.

### 8.2 Responsibilities

Product Catalog is responsible for:

- Maintaining Product reference data
- Providing Product lookup
- Managing SKU information
- Providing Product descriptions
- Providing Product categories
- Providing units of measure
- Identifying Product status
- Evaluating Product orderability
- Providing Product restrictions
- Publishing Product changes

### 8.3 Owned Concepts

Product Catalog owns:

- Product
- Product ID
- SKU
- Product Name
- Product Description
- Product Category
- Unit of Measure
- Product Status
- Product Classification
- Ordering Restrictions

### 8.4 Excluded Concepts

Product Catalog does not own:

- Order Items
- Order-specific Product Snapshots
- Inventory quantities
- Customer-specific final pricing
- Approval decisions
- Shipment execution

### 8.5 Public Capabilities

- Get Product
- Get Products in Batch
- Validate Product Existence
- Validate Product Orderability
- Get Product Classification
- Get Ordering Restrictions

### 8.6 Consumers

Initial consumers include:

- Order Management
- Pricing
- Inventory
- Approval
- Fulfillment

### 8.7 Integration Style

The initial interaction is primarily synchronous for Order Item validation.

Product changes may be propagated asynchronously through events such as:

- ProductUpdated
- ProductStatusChanged
- ProductDiscontinued
- ProductRestrictionChanged

### 8.8 Boundary Rules

- Order Management stores Product References and Product Snapshots.
- Inventory owns stock information and must not extend the Product Catalog model with stock fields.
- Pricing owns commercial calculation models and must not modify Product Catalog entities.
- Product Catalog persistence models must remain internal.

---

## 9. Pricing Context

### 9.1 Classification

Supporting Domain.

### 9.2 Responsibilities

Pricing is responsible for:

- Determining Base Prices
- Determining Effective Prices
- Applying Customer-specific pricing
- Applying commercial policies
- Calculating discounts
- Validating discounts
- Calculating taxes
- Calculating fees
- Calculating freight
- Applying rounding rules
- Producing item totals
- Producing Order totals
- Returning explainable Pricing results
- Versioning Pricing policies where necessary

### 9.3 Owned Concepts

Pricing owns:

- Pricing Request
- Pricing Result
- Base Price
- Effective Price
- Discount Policy
- Tax Policy
- Fee Policy
- Freight Policy
- Pricing Rule
- Pricing Policy Version
- Money calculation rules
- Rounding rules

### 9.4 Excluded Concepts

Pricing does not own:

- Order lifecycle
- Order Status
- Order persistence
- Product master data
- Customer master data
- Approval decisions
- Inventory quantities

### 9.5 Public Capabilities

- Calculate Order Pricing
- Calculate Item Pricing
- Validate Discount
- Calculate Tax
- Calculate Freight
- Explain Pricing Result

### 9.6 Inputs

Pricing may consume:

- Customer Reference
- Customer Classification
- Customer Segment
- Product Reference
- Product Category
- Quantity
- Delivery destination
- Payment conditions
- Commercial date
- Currency
- Requested discount

### 9.7 Outputs

Pricing produces a Pricing Result containing values such as:

- Unit Price
- Effective Price
- Discount Amount
- Discount Percentage
- Tax Amount
- Fee Amount
- Freight Amount
- Subtotal
- Grand Total
- Currency
- Applied policy references
- Calculation timestamp

### 9.8 Interaction Style

Pricing is initially synchronous because users generally need immediate totals while editing or submitting an Order.

Asynchronous repricing may be introduced for:

- Large Orders
- Bulk operations
- Scheduled commercial updates
- Complex external calculations

### 9.9 Boundary Rules

- Order Management must not reproduce Pricing rules.
- Pricing must not modify the Order aggregate.
- Pricing results must be translated into Order-owned snapshots.
- Historical Orders must not be recalculated automatically after Pricing policy changes.
- Monetary calculations must use explicit Money, Currency, precision, scale, and rounding rules.
- Floating-point types must not represent monetary values.

---

## 10. Approval Context

### 10.1 Classification

Supporting Domain.

### 10.2 Responsibilities

Approval is responsible for:

- Determining whether an Order requires Approval
- Selecting the applicable Approval Policy
- Creating Approval Processes
- Creating Approval Steps
- Assigning eligible approvers
- Recording Approval Decisions
- Supporting Approval, Rejection, and Review Request decisions
- Coordinating multiple Approval levels
- Preserving Approval history
- Preventing duplicate decisions
- Publishing Approval outcomes

### 10.3 Owned Concepts

Approval owns:

- Approval Process
- Approval Process ID
- Approval Policy
- Approval Step
- Approval Level
- Approver
- Approval Decision
- Approval Comment
- Rejection Reason
- Review Request
- Approval Status
- Approval History

### 10.4 Excluded Concepts

Approval does not own:

- Order aggregate
- Order Items
- Order Pricing calculation
- Customer master data
- Product master data
- Inventory Reservation
- User authentication

### 10.5 Public Capabilities

- Evaluate Approval Requirement
- Start Approval Process
- Get Approval Process
- List Pending Approvals
- Approve
- Reject
- Request Review
- Cancel Approval Process

### 10.6 Inputs

Approval may evaluate:

- Order ID
- Order Number
- Order Total
- Discount Percentage
- Customer Classification
- Product Category
- Business Segment
- Commercial exceptions
- Requested action
- User roles and permissions

### 10.7 Outputs

Approval may produce:

- ApprovalNotRequired
- ApprovalRequired
- ApprovalProcessStarted
- OrderApproved
- OrderRejected
- OrderReviewRequested
- ApprovalProcessCancelled

### 10.8 Interaction Style

Initial possibilities include:

- Synchronous evaluation to determine whether Approval is required
- Asynchronous Approval workflow after Order submission
- Asynchronous outcome events back to Order Management

The final design will be documented in the Context Map and Approval ADRs.

### 10.9 Boundary Rules

- Approval decisions must not directly update Order persistence.
- Order Management reacts to explicit Approval outcomes.
- Approval must validate approver authorization using Identity and Access contracts.
- The same Approval Step must not be completed more than once.
- Rejection requires an explicit Rejection Reason.
- Approval policies must be independently testable.
- Approval workflow state must not be represented only by fields inside Order.

---

## 11. Inventory Context

### 11.1 Classification

Supporting Domain.

### 11.2 Responsibilities

Inventory is responsible for:

- Providing Inventory Availability
- Receiving Reservation Requests
- Reserving Product quantities
- Managing Reservation Items
- Confirming Reservations
- Rejecting Reservations
- Releasing Reservations
- Expiring Reservations
- Preventing duplicate reservation effects
- Tracking external Inventory references
- Publishing Reservation outcomes

### 11.3 Owned Concepts

Inventory owns:

- Inventory Item Reference
- Available Quantity
- Reserved Quantity
- Inventory Reservation
- Reservation ID
- Reservation Request ID
- Reservation Item
- Reservation Status
- Reservation Failure
- Reservation Expiration
- Inventory Release

### 11.4 Excluded Concepts

Inventory does not own:

- Order lifecycle
- Order Status
- Product descriptions
- Product pricing
- Approval decisions
- Shipment details
- Payment details

### 11.5 Public Capabilities

- Check Availability
- Request Reservation
- Get Reservation
- Confirm Reservation
- Release Reservation
- Expire Reservation

### 11.6 Inputs

Reservation requests may contain:

- Reservation Request ID
- Order ID
- Correlation ID
- Product references
- Requested quantities
- Requested expiration
- Request timestamp

### 11.7 Outputs

Inventory may produce:

- InventoryReservationRequested
- InventoryReserved
- InventoryReservationFailed
- InventoryPartiallyReserved
- InventoryReleased
- InventoryReservationExpired

### 11.8 Interaction Style

Inventory Reservation is expected to be asynchronous because:

- It may depend on external warehouse systems.
- Processing may be long-running.
- Temporary failures may require retries.
- Duplicate delivery must be tolerated.
- Order processing should not maintain a remote distributed transaction.

Synchronous availability checks may be offered as advisory information.

### 11.9 Boundary Rules

- Availability does not guarantee Reservation.
- Reservation requests must be idempotent.
- Duplicate requests must not reserve stock twice.
- Release requests must be idempotent.
- Inventory must not update Order tables.
- Order Management must react only to explicit Inventory outcomes.
- Reservation status must not reuse Order Status.
- Inventory Product references must not reuse Product Catalog persistence entities.

---

## 12. Fulfillment Context

### 12.1 Classification

Supporting Domain.

### 12.2 Responsibilities

Fulfillment is responsible for:

- Receiving Orders ready for fulfillment
- Creating Fulfillment requests
- Coordinating picking
- Coordinating packing
- Coordinating dispatch
- Managing Shipments
- Tracking delivery progress
- Recording carrier information
- Recording tracking information
- Publishing Fulfillment outcomes

### 12.3 Owned Concepts

Fulfillment owns:

- Fulfillment Request
- Fulfillment ID
- Shipment
- Shipment ID
- Carrier
- Tracking Number
- Dispatch Date
- Estimated Delivery Date
- Delivery Date
- Fulfillment Status
- Shipment Status

### 12.4 Excluded Concepts

Fulfillment does not own:

- Order aggregate
- Order Pricing
- Customer eligibility
- Product Catalog
- Inventory balances
- Payment transactions
- Approval policies

### 12.5 Public Capabilities

- Prepare Fulfillment
- Create Shipment
- Register Dispatch
- Update Tracking
- Confirm Delivery
- Get Fulfillment Status

### 12.6 Inputs

Fulfillment may consume:

- Order Ready for Fulfillment event
- Order reference
- Customer delivery snapshot
- Product shipment information
- Reserved quantities
- Delivery priority
- Carrier selection requirements

### 12.7 Outputs

Fulfillment may produce:

- FulfillmentStarted
- ShipmentCreated
- OrderDispatched
- DeliveryDelayed
- OrderDelivered
- FulfillmentFailed

### 12.8 Interaction Style

Fulfillment is expected to be asynchronous because it represents a long-running business process.

### 12.9 Boundary Rules

- Fulfillment must use Order snapshots and explicit contracts.
- Fulfillment must not query Order persistence tables directly.
- Shipment Status is independent from Order Status.
- Order Management may derive Order lifecycle transitions from Fulfillment facts.
- Delivery execution must not occur inside an Order transaction.

---

## 13. Payment Context

### 13.1 Classification

Supporting Domain.

### 13.2 Responsibilities

Payment is responsible for:

- Registering payment expectations
- Managing Payment lifecycle
- Tracking Payment Status
- Communicating with financial providers
- Recording external transaction references
- Confirming Payments
- Recording Payment failures
- Recording cancellations or refunds
- Publishing Payment outcomes

### 13.3 Owned Concepts

Payment owns:

- Payment
- Payment ID
- Payment Method
- Payment Status
- Payment Amount
- External Transaction Reference
- Payment Confirmation
- Payment Failure
- Refund

### 13.4 Excluded Concepts

Payment does not own:

- Order aggregate
- Order lifecycle
- Pricing policies
- Customer master data
- Inventory
- Shipment execution

### 13.5 Public Capabilities

- Register Payment Expectation
- Authorize Payment
- Confirm Payment
- Fail Payment
- Cancel Payment
- Refund Payment
- Get Payment Status

### 13.6 Interaction Style

The interaction style depends on Payment Method.

Possible patterns include:

- Synchronous authorization
- Asynchronous confirmation
- Provider webhook processing
- Scheduled reconciliation
- Event-driven Payment updates

### 13.7 Boundary Rules

- Payment Status must remain distinct from Order Status.
- Payment provider models must be translated through an Anti-Corruption Layer.
- Duplicate provider callbacks must be idempotent.
- Payment credentials and sensitive data must not be propagated to other contexts.
- Order Management must consume stable Payment outcomes rather than provider-specific payloads.

---

## 14. Notification Context

### 14.1 Classification

Generic Domain.

### 14.2 Responsibilities

Notification is responsible for:

- Receiving Notification requests
- Selecting templates
- Resolving channels
- Preparing Notification content
- Sending email notifications
- Sending SMS notifications when supported
- Sending push notifications when supported
- Tracking delivery attempts
- Retrying transient failures
- Recording delivery status
- Applying channel-specific policies

### 14.3 Owned Concepts

Notification owns:

- Notification Request
- Notification ID
- Notification Template
- Notification Channel
- Recipient
- Delivery Attempt
- Delivery Status
- Notification Failure

### 14.4 Excluded Concepts

Notification does not own:

- Order business rules
- Approval decisions
- Customer eligibility
- Inventory Reservation
- Audit history

### 14.5 Public Capabilities

- Request Notification
- Get Notification Status
- Retry Notification
- Manage Templates, when included in scope

### 14.6 Inputs

Notification requests may contain:

- Notification type
- Recipient reference
- Template reference
- Safe template variables
- Correlation identifier
- Business entity reference
- Preferred channel

### 14.7 Outputs

Notification may produce:

- NotificationRequested
- NotificationSent
- NotificationDeliveryFailed
- NotificationPermanentlyFailed

### 14.8 Interaction Style

Notification processing is asynchronous.

A business transaction must not fail solely because a notification provider is temporarily unavailable.

### 14.9 Boundary Rules

- Business contexts request Notifications; they do not send them directly.
- Notification templates must not contain business decision logic.
- Sensitive data must not be included unless explicitly required and protected.
- Duplicate Notification requests must be controlled through idempotency.
- Provider-specific failures must be translated into stable internal outcomes.

---

## 15. Audit Context

### 15.1 Classification

Generic Domain.

### 15.2 Responsibilities

Audit is responsible for:

- Receiving business Audit facts
- Recording immutable Audit Records
- Preserving actor information
- Preserving operation timestamps
- Preserving entity references
- Preserving Correlation Identifiers
- Supporting investigation
- Supporting regulatory or governance requirements
- Applying retention policies
- Restricting Audit access

### 15.3 Owned Concepts

Audit owns:

- Audit Record
- Audit ID
- Audit Operation
- Audit Actor
- Audit Entity Reference
- Audit Timestamp
- Audit Change Set
- Audit Retention Policy

### 15.4 Excluded Concepts

Audit does not own:

- Order Status History
- Order business rules
- Approval workflow
- Authentication
- Operational logs
- Distributed trace storage

### 15.5 Public Capabilities

- Record Audit Fact
- Search Audit Records
- Get Entity Audit Trail
- Apply Retention Policy

### 15.6 Interaction Style

Audit capture should be reliable and generally asynchronous after the originating business transaction safely records the required fact.

Local business history may be persisted in the originating context when it is part of the aggregate's business model.

### 15.7 Boundary Rules

- Audit Records must be immutable.
- Audit data must not expose tokens, passwords, secrets, or unnecessary personal data.
- Business History and Audit Trail must remain separate concepts.
- Audit failure must not silently discard mandatory records.
- Audit contracts must use stable business operation names.
- Audit must not query internal tables of other contexts.

---

## 16. Identity and Access Context

### 16.1 Classification

Generic Domain.

### 16.2 Responsibilities

Identity and Access is responsible for:

- Authenticating Users
- Representing User identities
- Managing Roles
- Managing Permissions
- Supplying authenticated User Context
- Validating access policies
- Supporting System Actors
- Integrating with external identity providers

### 16.3 Owned Concepts

Identity and Access owns:

- User
- User ID
- Role
- Permission
- Authentication Session
- Access Token validation
- System Actor
- Identity Provider Reference

### 16.4 Excluded Concepts

Identity and Access does not own:

- Customer eligibility
- Order approval policies
- Order lifecycle
- Audit business history
- Notification recipients
- Customer master data

### 16.5 Public Capabilities

- Authenticate
- Validate Token
- Get User Context
- Check Permission
- Get User Roles
- Resolve System Actor

### 16.6 Interaction Style

Authentication and authorization checks are synchronous.

Identity lifecycle changes may be published asynchronously.

### 16.7 Boundary Rules

- Domain objects must not depend on Spring Security types.
- Access tokens must not enter the Domain Layer.
- Business authorization may require collaboration between Identity and the owning business context.
- A Role does not automatically replace a business eligibility rule.
- External identity provider models must be translated into internal User Context.
- Authorization must be enforced server-side.

---

## 17. Integration Events Capability

Integration Events may initially be implemented as infrastructure within the modular monolith rather than as an independent business bounded context.

Its responsibility is to reliably expose selected business facts to external consumers.

### 17.1 Responsibilities

- Persist Outbox Records
- Publish Integration Events
- Apply event versioning
- Add correlation metadata
- Retry transient publication failures
- Track publication attempts
- Route permanently failed events
- Support idempotent consumers
- Preserve event contract stability

### 17.2 Owned Technical Concepts

- Outbox Record
- Event Envelope
- Event Type
- Event Version
- Publication Status
- Publication Attempt
- Destination
- Dead-letter metadata

### 17.3 Boundary Rules

- Domain Events and Integration Events are not the same concept.
- Not every Domain Event must be published externally.
- Event payloads must not expose internal persistence entities.
- Business transactions and Outbox Records must be persisted atomically when required.
- Event publication must not occur before the related business transaction commits.
- Event consumers must assume at-least-once delivery.
- Breaking contract changes require explicit versioning.

---

## 18. Initial Context Relationships

| Consumer Context | Provider Context | Initial Interaction | Purpose |
|---|---|---|---|
| Order Management | Identity and Access | Synchronous | Obtain authenticated User Context |
| Order Management | Customer | Synchronous | Validate Customer and eligibility |
| Order Management | Product Catalog | Synchronous | Validate Products and orderability |
| Order Management | Pricing | Synchronous | Calculate and validate commercial values |
| Order Management | Approval | Synchronous and asynchronous | Evaluate requirements and receive decisions |
| Order Management | Inventory | Asynchronous | Reserve and release Inventory |
| Order Management | Fulfillment | Asynchronous | Initiate and track Fulfillment |
| Order Management | Payment | Synchronous or asynchronous | Validate or receive Payment outcomes |
| Order Management | Notification | Asynchronous | Request lifecycle notifications |
| Order Management | Audit | Asynchronous | Record business Audit facts |
| Pricing | Customer | Synchronous | Obtain commercial Customer attributes |
| Pricing | Product Catalog | Synchronous | Obtain Product pricing attributes |
| Approval | Identity and Access | Synchronous | Validate approver identity and permissions |
| Approval | Customer | Synchronous or replicated reference | Evaluate Customer classification |
| Approval | Product Catalog | Synchronous or replicated reference | Evaluate Product categories |
| Inventory | Product Catalog | Synchronous or local reference | Resolve Product identifiers |
| Fulfillment | Order Management | Asynchronous contract | Receive ready-for-fulfillment facts |
| Payment | Order Management | Asynchronous contract | Associate Payment with Order reference |

These are initial relationships and may change after the Context Map and aggregate design are completed.

---

## 19. Upstream and Downstream Direction

The initial upstream and downstream direction is expected to be:

| Upstream | Downstream | Rationale |
|---|---|---|
| Identity and Access | Order Management | Identity provides authentication context |
| Customer | Order Management | Customer provides authoritative reference data |
| Product Catalog | Order Management | Product Catalog provides authoritative Product data |
| Pricing | Order Management | Pricing provides commercial calculation results |
| Order Management | Approval | Order Management requests Approval processing |
| Approval | Order Management | Approval publishes business decisions |
| Order Management | Inventory | Order Management requests Reservation |
| Inventory | Order Management | Inventory publishes Reservation outcomes |
| Order Management | Fulfillment | Order Management requests Fulfillment |
| Fulfillment | Order Management | Fulfillment publishes progress |
| Order Management | Notification | Notification consumes requests |
| Order Management | Audit | Audit consumes business facts |
| Order Management | External Consumers | External consumers receive Integration Events |

Some relationships are bidirectional at the business-process level.

Each individual contract must still have a clearly defined provider and consumer.

---

## 20. Data Ownership

Each bounded context owns its data.

### 20.1 Ownership Rules

- Order Management owns Order data.
- Customer owns current Customer data.
- Product Catalog owns current Product data.
- Pricing owns Pricing policies and calculation definitions.
- Approval owns Approval Processes and Decisions.
- Inventory owns Reservations and stock-related state.
- Fulfillment owns Shipments and delivery state.
- Payment owns Payment transactions.
- Notification owns Notification requests and delivery attempts.
- Audit owns Audit Records.
- Identity and Access owns identity and authorization data.

### 20.2 Prohibited Access

The following practices are prohibited:

- Direct SQL access to another context's tables
- Foreign keys across context-owned schemas
- Shared JPA entities across contexts
- Shared repositories across contexts
- Updating another context's tables
- Reusing another context's persistence model
- Joining context-owned tables to implement business logic
- Treating a shared database as a shared domain model

### 20.3 Modular Monolith Interpretation

Because the initial architecture is a modular monolith, contexts may use the same PostgreSQL instance.

However, logical ownership must remain explicit through:

- Separate schemas where practical
- Module-specific migrations
- Module-specific repositories
- Explicit module APIs
- Architecture tests
- Restricted package visibility

Using the same database instance does not authorize unrestricted cross-context access.

---

## 21. Snapshot Ownership

Snapshots are owned by the consuming context.

### 21.1 Customer Snapshot

Order Management owns the Customer Snapshot stored with an Order.

Customer Context owns current Customer data.

A Customer Snapshot is not synchronized automatically after Order submission.

### 21.2 Product Snapshot

Order Management owns the Product Snapshot stored with an Order Item.

Product Catalog owns current Product data.

A Product Snapshot is not replaced when Product Catalog data changes.

### 21.3 Pricing Snapshot

Order Management owns the accepted Pricing Snapshot associated with a submitted Order.

Pricing owns the policies and calculations that produced the result.

A Pricing Snapshot must remain historically stable.

---

## 22. Shared Concepts

The project should minimize shared domain models.

Potential technical primitives may be shared only when they contain no context-specific behavior.

Examples of possible shared technical concepts:

- Correlation Identifier
- Pagination metadata
- Generic event envelope
- Standard error representation
- Clock abstraction

Business value objects should not be placed in a generic shared module merely because they have similar fields.

Examples:

- Order Status and Payment Status must be separate types.
- Order Money and Payment Amount may share a stable Money primitive, but each context controls its own business rules.
- Customer entities must not be shared.
- Product entities must not be shared.
- Approval User and Identity User must not automatically be the same model.

Shared Kernel will not be adopted unless a future ADR provides a strong justification.

---

## 23. Synchronous Communication Principles

Synchronous communication is suitable when:

- An immediate response is required.
- The user cannot continue without the result.
- The operation represents a query or validation.
- Temporary inconsistency is not acceptable for that interaction.
- The provider can meet the required latency and availability targets.

Initial synchronous use cases include:

- Customer lookup
- Customer eligibility validation
- Product lookup
- Product orderability validation
- Pricing calculation
- Authentication
- Permission validation
- Advisory Inventory availability

All synchronous integrations must define:

- Connection timeout
- Response timeout
- Error mapping
- Retry policy for transient failures
- Circuit-breaker policy where justified
- Correlation propagation
- Observability
- Fallback behavior where valid
- Data classification and security controls

Retries must not be applied blindly to state-changing operations.

---

## 24. Asynchronous Communication Principles

Asynchronous communication is suitable when:

- The process is long-running.
- Immediate consistency is not required.
- Temporary failures must not block the originating transaction.
- Multiple consumers need the same business fact.
- The operation requires retry and recovery.
- The provider may be temporarily unavailable.

Initial asynchronous use cases include:

- Inventory Reservation
- Inventory Release
- Approval outcome processing
- Fulfillment initiation
- Payment status updates
- Notification requests
- Audit facts
- Order lifecycle Integration Events

All asynchronous interactions must define:

- Event type
- Event version
- Event identifier
- Correlation identifier
- Causation identifier where required
- Occurrence timestamp
- Producer
- Consumer
- Delivery expectations
- Idempotency behavior
- Retry policy
- Dead-letter strategy
- Ordering requirements
- Retention requirements
- Sensitive-data restrictions

---

## 25. Transaction Boundaries

A local transaction may include:

- Updating one Aggregate
- Persisting Aggregate state
- Persisting context-owned history
- Persisting Domain Events
- Persisting Outbox Records
- Persisting idempotency records

A local transaction should not include:

- Waiting for another context to complete a long-running process
- Remote Inventory Reservation
- Remote Payment processing
- Notification delivery
- Shipment execution
- Kafka broker availability
- External identity-provider modification

Distributed transactions across bounded contexts will not be used as the default consistency mechanism.

Eventual consistency and compensating actions will be applied where required.

---

## 26. Dependency Rules

The initial module dependency rules are:

```text
Identity and Access
Customer
Product Catalog
Pricing
Approval
Inventory
Fulfillment
Payment
Notification
Audit
        ^
        |
Order Management coordinates through explicit contracts
```

This diagram does not mean Order Management may depend on every context's implementation.

It may depend only on stable ports or public contracts.

### 26.1 Allowed Dependencies

- Application services may depend on their own Domain Layer.
- Infrastructure adapters may depend on application ports.
- REST adapters may depend on application use cases.
- A context may depend on another context's published contract.
- Domain code may depend on context-owned value objects and domain abstractions.

### 26.2 Prohibited Dependencies

- Domain code depending on Spring
- Domain code depending on JPA
- Domain code depending on Kafka
- Controllers depending directly on repositories
- One context depending on another context's persistence package
- One context instantiating another context's aggregate
- Infrastructure details leaking into public business contracts
- Cyclic module dependencies
- Shared mutable domain objects across contexts

---

## 27. Anti-Corruption Layers

Anti-Corruption Layers should be introduced when integrating with models that do not match the platform's language.

Likely candidates include:

- External Customer systems
- External Product systems
- External Inventory systems
- Payment providers
- Shipping providers
- Identity providers
- Legacy enterprise systems

An Anti-Corruption Layer may contain:

- External client adapter
- External request and response models
- Translators
- Mappers
- Error translators
- Contract version handling
- Resilience policies

External models must not become internal Domain models by convenience.

---

## 28. Failure Isolation

Each context must isolate failures from other contexts where practical.

Examples:

- Notification failure must not invalidate a submitted Order.
- Audit publication failure must retain a recoverable local record.
- Inventory unavailability must result in an explicit Order state.
- Approval service unavailability must not corrupt Order state.
- Customer or Product validation failure must produce a stable application error.
- Kafka publication failure must not roll back an already committed Order when the Outbox Pattern is used.
- Payment provider failure must not expose provider-specific errors directly to Order Management.

Failures must be translated into context-specific outcomes.

---

## 29. Initial Modular Structure

The exact package structure will be documented later.

An initial conceptual structure may be:

```text
com.renatooliveira.enterpriseorders
├── order
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── customer
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── product
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── pricing
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── approval
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── inventory
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── fulfillment
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── payment
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── notification
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── audit
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── api
├── identity
│   ├── application
│   ├── infrastructure
│   └── api
└── shared
    └── kernel
```

The `shared.kernel` module should remain minimal.

Business concepts must not be moved there only to avoid explicit integration.

---

## 30. Architecture Enforcement

Context boundaries should be enforced through automated tests and build rules.

Planned controls include:

- ArchUnit module dependency tests
- Package visibility restrictions
- No access to another context's infrastructure package
- No access to another context's persistence entities
- No cyclic context dependencies
- Domain independence from frameworks
- Application independence from inbound adapters
- Public API restrictions
- Module-specific test suites
- SonarQube or SonarCloud quality gates

Example architectural rule:

```text
Order domain must not depend on Spring, JPA, Kafka, REST, or another bounded context.
```

---

## 31. Initial Context Implementation Scope

The first implementation milestone should prioritize:

### Fully Modeled

- Order Management
- Core Pricing behavior required by Orders
- Customer reference contracts
- Product reference contracts
- Audit event generation
- Identity User Context

### Simplified or Port-Based

- Approval
- Inventory
- Notification

### Deferred

- Fulfillment
- Payment
- Advanced Customer management
- Advanced Product Catalog management
- Multi-warehouse Inventory
- Multi-carrier Shipment orchestration

Deferred contexts must still have explicit boundaries so temporary implementations do not contaminate Order Management.

---

## 32. Future Microservice Evolution

The initial modular monolith does not imply that every context should become a microservice.

Extraction should occur only when justified by requirements such as:

- Independent deployment
- Independent scaling
- Different availability requirements
- Different data ownership
- Organizational ownership
- Regulatory isolation
- Technology-specific needs
- High rate of independent change
- Failure isolation requirements

Likely future extraction candidates include:

- Inventory
- Notification
- Payment
- Fulfillment
- Pricing

Order Management should remain a modular monolith context or become an independent service only after its internal boundaries are stable.

Microservice extraction must not be performed only for portfolio appearance or architectural fashion.

---

## 33. Open Boundary Questions

The following questions require future decisions:

1. Does Enterprise Order Platform own Customer master data?
2. Does Enterprise Order Platform own Product Catalog data?
3. Is Pricing a separate bounded context in the first implementation?
4. Is Pricing called during every Draft modification or only during explicit recalculation?
5. Is Approval Process an independent aggregate?
6. Is Approval evaluation synchronous while decisions are asynchronous?
7. Can Order Management proceed when Approval is temporarily unavailable?
8. Does Inventory Reservation occur per Order or per Order Item?
9. Is partial Inventory Reservation allowed?
10. Does Inventory Reservation expire automatically?
11. Does Fulfillment start automatically after Inventory Reservation?
12. Is Payment required before Fulfillment?
13. Does Payment receive the accepted Order total or recalculate financial values?
14. Is Notification a separate module or infrastructure capability?
15. Which Audit facts must be recorded transactionally?
16. Which Order Domain Events become Integration Events?
17. Which contexts require local replicated reference data?
18. Should each context use a separate PostgreSQL schema?
19. Which contexts may be extracted into microservices first?
20. Which public contracts require semantic versioning?

These questions will be refined through the Context Map, ADRs, aggregate modeling, and implementation feedback.

---

## 34. Decision Summary

The initial strategic decisions are:

- Order Management is the Core Domain.
- The system starts as a modular monolith.
- Bounded contexts are explicit even within one deployment.
- Each context owns its model and data.
- Direct cross-context database access is prohibited.
- Order Management coordinates the lifecycle through explicit contracts.
- Customer and Product master data remain outside the Order aggregate.
- Pricing results are translated into immutable Order snapshots.
- Approval, Inventory, Fulfillment, and Payment maintain independent lifecycle models.
- Inventory, Notification, Audit, and lifecycle integration are primarily asynchronous.
- Domain Events and Integration Events remain distinct.
- The Transactional Outbox Pattern will protect event publication reliability.
- Event consumers must be idempotent.
- External systems must be isolated through Anti-Corruption Layers.
- Context boundaries will be enforced with automated architecture tests.
- Microservice extraction will be requirement-driven rather than technology-driven.

---

## 35. Next Documentation Step

The next document will define the relationships between bounded contexts using strategic DDD patterns:

```text
docs/domain/context-map.md
```

It will establish:

- Upstream and downstream relationships
- Customer-Supplier relationships
- Conformist relationships
- Open Host Services
- Published Languages
- Anti-Corruption Layers
- Separate Ways
- Partnership relationships
- Synchronous contracts
- Asynchronous contracts
- Context dependency direction
