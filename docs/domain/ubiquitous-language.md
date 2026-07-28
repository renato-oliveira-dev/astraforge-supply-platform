# Ubiquitous Language

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Ubiquitous Language |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

## 1. Purpose

This document defines the initial ubiquitous language used throughout Enterprise Order Platform.

The goal is to establish a consistent business vocabulary shared across:

- Requirements
- Domain documentation
- Architecture documentation
- Source code
- API contracts
- Event contracts
- Tests
- Logs
- Monitoring
- Technical discussions

The same business concept should use the same name whenever its meaning is the same.

Different bounded contexts may use different terms only when the business meaning is intentionally different.

---

## 2. Language Principles

The project follows these language principles:

- Business terminology takes precedence over generic technical terminology.
- Names must reflect business meaning rather than database structure.
- Domain concepts should not be renamed only to fit framework conventions.
- A term should have one explicit meaning inside a bounded context.
- Ambiguous abbreviations should be avoided.
- Generic terms such as `data`, `item`, `detail`, `process`, or `status code` should not replace specific business terms.
- API fields, event fields, class names, and test names should align with the documented vocabulary.
- Changes to established terminology should be reviewed as domain changes, not cosmetic refactoring.

---

## 3. Core Order Terms

### Order

An `Order` represents a corporate customer's formal intention to acquire one or more products under defined commercial conditions.

An Order contains business state and behavior related to:

- Customer reference
- Order items
- Pricing
- Submission
- Approval
- Inventory reservation
- Cancellation
- Lifecycle status
- Historical snapshots

Preferred usage:

- `Order`
- `OrderId`
- `OrderNumber`
- `OrderStatus`
- `OrderRepository`

Avoid:

- Request
- Transaction
- Generic process
- Header
- Master record

---

### Draft Order

A `Draft Order` is an Order that has not yet been submitted.

A Draft Order may generally be:

- Updated
- Have items added
- Have items removed
- Repriced
- Discarded
- Submitted

Preferred status name:

```text
DRAFT
```

Avoid:

- Open Order
- Temporary Order
- Pending Order
- Saved Order

---

### Submitted Order

A `Submitted Order` is an Order whose commercial information was confirmed and sent for business processing.

After submission:

- The order is no longer freely editable.
- Customer and product snapshots are preserved.
- Pricing results become immutable.
- Approval or inventory processing may begin.
- Relevant domain events are generated.

Preferred operation:

```text
submitOrder
```

Avoid:

- Finalize
- Save Final
- Confirm Record
- Close Draft

---

### Order Item

An `Order Item` represents a product and quantity included in an Order.

An Order Item belongs exclusively to one Order aggregate.

Typical attributes include:

- Product identifier
- Product snapshot
- Quantity
- Unit price
- Discount
- Taxes
- Fees
- Subtotal
- Total

Preferred usage:

- `OrderItem`
- `OrderItemId`
- `addItem`
- `updateItemQuantity`
- `removeItem`

Avoid:

- Detail
- Detail Record
- Row
- Line Entity
- Product Row

---

### Order Number

An `Order Number` is the business-facing identifier used to reference an Order.

It is different from the internal technical identifier.

Example distinction:

- `OrderId`: internal unique identifier
- `OrderNumber`: human-readable or business-readable identifier

Avoid using the terms interchangeably.

---

### Order Status

`Order Status` represents the current lifecycle state of an Order.

The status must only change through explicit domain operations.

Initial status candidates:

```text
DRAFT
SUBMITTED
PENDING_APPROVAL
APPROVED
REJECTED
PROCESSING
INVENTORY_PENDING
INVENTORY_RESERVED
INVENTORY_FAILED
READY_FOR_FULFILLMENT
FULFILLMENT_IN_PROGRESS
COMPLETED
CANCELLED
```

Avoid:

- Status Code
- Processing Flag
- State Number
- Generic Situation

---

### Order Status Transition

An `Order Status Transition` is a valid change from one Order Status to another.

Examples:

```text
DRAFT -> SUBMITTED
SUBMITTED -> PENDING_APPROVAL
PENDING_APPROVAL -> APPROVED
PROCESSING -> INVENTORY_PENDING
INVENTORY_RESERVED -> READY_FOR_FULFILLMENT
```

A transition must:

- Be explicit
- Be validated
- Preserve invariants
- Produce relevant domain events
- Be recorded in history when required

---

### Order Status History

`Order Status History` is the chronological record of lifecycle transitions performed on an Order.

It may contain:

- Previous status
- New status
- Transition timestamp
- Responsible user or system
- Correlation identifier
- Transition reason

The history is append-only.

Avoid:

- Status Log
- Generic Audit Table
- Situation History

---

## 4. Customer Terms

### Customer

A `Customer` is the corporate organization associated with an Order.

The platform may consume Customer master data from another system.

Order Management should preserve only the data needed to:

- Validate eligibility
- Process the order
- Preserve historical integrity
- Support fulfillment
- Support audit requirements

Avoid:

- Client, unless a bounded context explicitly adopts that term
- Account
- Buyer record
- Company record

---

### Customer Reference

A `Customer Reference` is the identifier used by Order Management to refer to a Customer owned by another bounded context or external system.

Preferred usage:

- `CustomerId`
- `CustomerReference`

Avoid:

- Customer object when only the identifier is available
- Customer entity inside Order Management if the context does not own Customer master data

---

### Customer Snapshot

A `Customer Snapshot` is an immutable copy of selected customer information preserved at a relevant point in the Order lifecycle.

The snapshot protects historical consistency when Customer master data changes later.

Possible fields:

- Customer identifier
- Legal name
- Trade name
- Document number
- Customer type
- Delivery information
- Contact information

A snapshot is not the current Customer master record.

---

### Customer Eligibility

`Customer Eligibility` represents whether a Customer is currently permitted to place or receive Orders.

Eligibility may depend on:

- Customer status
- Commercial restrictions
- Credit restrictions
- Business segment
- Contractual conditions
- Operational restrictions

Preferred operation:

```text
validateCustomerEligibility
```

Avoid:

- Generic customer validation
- Active flag check
- Simple status validation

---

## 5. Product Terms

### Product

A `Product` represents an item that may be included in an Order.

Product master data may be owned by a Product Catalog bounded context or external system.

Typical information includes:

- Product identifier
- SKU
- Name
- Description
- Category
- Unit of measure
- Status
- Ordering restrictions

---

### Product Reference

A `Product Reference` is the identifier used by Order Management to refer to a Product owned elsewhere.

Preferred usage:

- `ProductId`
- `ProductReference`

Avoid creating an Order Management Product entity that duplicates the entire external catalog.

---

### Product Snapshot

A `Product Snapshot` is an immutable copy of selected Product information preserved in an Order Item.

Possible fields:

- Product identifier
- SKU
- Product name
- Description
- Unit of measure
- Category

The snapshot ensures that historical Orders are not altered by future Product Catalog changes.

---

### SKU

`SKU` is the business identifier used to distinguish a sellable product or variation.

SKU should not automatically be treated as the internal Product identifier.

Preferred usage:

- `Sku`
- `sku`

Avoid:

- Product code, when the business specifically means SKU
- Internal database identifier

---

### Orderable Product

An `Orderable Product` is a Product currently eligible to be included in an Order.

A product may be non-orderable because it is:

- Inactive
- Discontinued
- Restricted
- Unavailable for the Customer segment
- Outside the permitted sales channel

Preferred operation:

```text
validateProductOrderability
```

---

## 6. Quantity and Measurement Terms

### Quantity

`Quantity` represents the number of units of a Product requested in an Order Item.

Quantity must:

- Be greater than zero
- Respect product-specific constraints
- Respect unit-of-measure rules
- Use appropriate numeric precision

Preferred usage:

- `Quantity`
- `requestedQuantity`
- `reservedQuantity`

Avoid:

- Amount, when referring to number of units
- Value
- Generic number

---

### Unit of Measure

`Unit of Measure` defines how a Product quantity is expressed.

Examples:

- Unit
- Box
- Kilogram
- Liter
- Meter

Preferred usage:

- `UnitOfMeasure`

Avoid:

- Measure code
- Unit flag

---

## 7. Pricing Terms

### Pricing

`Pricing` represents the process and result of determining the commercial values of an Order or Order Item.

Pricing may include:

- Base price
- Effective price
- Discount
- Tax
- Fee
- Freight
- Subtotal
- Grand total

Avoid:

- Generic calculation
- Value processing
- Price service when referring to the business capability as a whole

---

### Base Price

`Base Price` is the initial price associated with a Product before discounts, taxes, fees, or freight.

---

### Unit Price

`Unit Price` is the effective price applied to one unit of an Order Item.

The Unit Price may differ from the Base Price due to:

- Customer agreements
- Commercial policies
- Promotions
- Segment rules
- Price lists

---

### Effective Price

`Effective Price` is the final unit price accepted for the Order Item after applicable pricing rules are evaluated.

---

### Discount

A `Discount` is a commercial reduction applied to a price.

A Discount may be represented as:

- Percentage
- Monetary amount
- Contractual adjustment
- Promotional adjustment

A Discount must not produce an invalid negative total.

---

### Tax

A `Tax` is a legally or commercially required monetary value applied to an Order or Order Item.

Tax rules must define:

- Calculation base
- Rate
- Rounding mode
- Jurisdiction or tax category
- Precision and scale

---

### Fee

A `Fee` is an additional commercial charge that is not represented as Product Price, Tax, or Freight.

Examples:

- Service fee
- Handling fee
- Administrative fee

---

### Freight

`Freight` is the cost associated with transporting the Order to the delivery destination.

Preferred usage:

- `FreightAmount`
- `FreightCalculation`

Avoid:

- Shipping tax
- Delivery value

---

### Subtotal

`Subtotal` is the calculated amount before selected additions or deductions.

Its exact formula must be explicit within the relevant Pricing policy.

---

### Grand Total

`Grand Total` is the final monetary amount associated with an Order after all applicable prices, discounts, taxes, fees, and freight are included.

Preferred usage:

- `grandTotal`

Avoid:

- Final value
- Total general
- Amount final

---

### Money

`Money` is a value object representing a monetary amount and its currency.

Money must define:

- Decimal amount
- Currency
- Precision
- Scale
- Rounding behavior

Floating-point types must not be used to represent Money.

Preferred implementation concept:

```text
Money(amount, currency)
```

---

### Currency

`Currency` identifies the monetary unit used in a Pricing result.

Examples:

```text
BRL
USD
EUR
```

Currency should use an established standard such as ISO 4217.

---

### Percentage

`Percentage` is a value object representing a proportional rate.

It may be used for:

- Discounts
- Taxes
- Fees
- Approval thresholds

A Percentage must define its allowed range and precision.

---

### Pricing Snapshot

A `Pricing Snapshot` is the immutable result of Pricing preserved when an Order reaches a defined lifecycle stage.

It prevents future pricing-rule changes from altering historical Orders.

---

## 8. Submission Terms

### Submission

`Submission` is the business action that changes an Order from Draft to a formally processing state.

Submission includes:

- Validating required data
- Validating customer eligibility
- Validating product orderability
- Confirming pricing
- Preserving snapshots
- Locking unrestricted editing
- Generating domain events

Preferred operation:

```text
submit
```

Avoid:

- Save
- Final save
- Commit order
- Confirm database record

---

### Submitted By

`Submitted By` identifies the user or system responsible for submitting the Order.

Preferred usage:

- `submittedBy`
- `SubmittedBy`

---

### Submitted At

`Submitted At` is the instant when the Order submission was accepted.

Preferred usage:

- `submittedAt`

Persisted timestamps should use UTC.

---

## 9. Approval Terms

### Approval

`Approval` is a formal business decision that allows an Order to continue.

Approval should not mean a generic boolean field.

It may require:

- One or more approval levels
- An eligible approver
- A decision timestamp
- Comments
- Business justification

---

### Approval Process

An `Approval Process` represents the complete set of approval activities required for an Order.

It may contain:

- Approval policy
- Approval steps
- Current level
- Decisions
- Responsible roles
- Completion status

---

### Approval Policy

An `Approval Policy` defines when an Order requires approval and which approval path applies.

A policy may evaluate:

- Order total
- Discount
- Product category
- Customer classification
- Commercial exception
- Business segment

---

### Approval Step

An `Approval Step` is one decision stage inside an Approval Process.

An Approval Step may be:

- Pending
- Approved
- Rejected
- Returned for review
- Skipped by policy

---

### Approver

An `Approver` is a user authorized to make an Approval Decision.

Authorization must be validated by the server.

---

### Approval Decision

An `Approval Decision` is the formal outcome recorded by an Approver.

Initial decisions:

```text
APPROVE
REJECT
REQUEST_REVIEW
```

Avoid:

- Update status
- Set flag
- Save approval

---

### Approval Comment

An `Approval Comment` is optional explanatory information recorded with an Approval Decision.

---

### Rejection Reason

A `Rejection Reason` is the mandatory business explanation for rejecting an Order or Approval Step.

Avoid using a generic observation field.

---

### Review Request

A `Review Request` indicates that an Order requires correction, clarification, or additional information before a final Approval Decision.

Preferred status concept:

```text
REVIEW_REQUESTED
```

---

### Approval Threshold

An `Approval Threshold` is a configured business limit that determines whether an Order requires a specific Approval Step.

Examples:

- Grand Total above a defined amount
- Discount above a defined percentage

---

## 10. Inventory Terms

### Inventory

`Inventory` represents the available and reserved quantities of Products.

Order Management does not own the complete Inventory model.

---

### Inventory Availability

`Inventory Availability` represents whether a requested Product quantity can currently be reserved.

Availability is not the same as a completed reservation.

---

### Inventory Reservation

An `Inventory Reservation` is the business process of allocating Product quantities to an Order.

A reservation may be:

- Requested
- Pending
- Confirmed
- Failed
- Released
- Expired

Avoid:

- Stock lock
- Quantity block
- Inventory flag

---

### Reservation Request

A `Reservation Request` is the instruction sent to Inventory Management asking it to reserve quantities for an Order.

It must contain enough information to support:

- Processing
- Idempotency
- Traceability
- Failure handling

---

### Reservation Item

A `Reservation Item` represents one Product quantity included in an Inventory Reservation.

---

### Reservation Confirmation

A `Reservation Confirmation` represents the successful result of an Inventory Reservation request.

---

### Reservation Failure

A `Reservation Failure` represents the unsuccessful result of an Inventory Reservation request.

It should contain a safe and stable failure reason.

---

### Inventory Release

`Inventory Release` is the process of making previously reserved quantities available again.

It may occur because of:

- Order cancellation
- Reservation expiration
- Processing failure
- Business rejection

Release operations must be idempotent.

---

### Partial Reservation

A `Partial Reservation` occurs when only part of the requested quantity can be reserved.

Whether Partial Reservation is allowed is an open domain decision.

---

### Reservation Expiration

`Reservation Expiration` is the instant or business condition after which a reservation is no longer valid.

---

## 11. Cancellation Terms

### Cancellation

`Cancellation` is the business action that terminates an Order before normal completion.

Cancellation must be permitted only from explicitly allowed statuses.

It may require:

- Cancellation reason
- Responsible user or system
- Inventory release
- Event publication
- Audit record
- Downstream notification

Preferred operation:

```text
cancel
```

Avoid:

- Delete Order
- Disable Order
- Set inactive

---

### Cancellation Reason

A `Cancellation Reason` is the business explanation required to cancel an Order.

It should be represented explicitly.

Preferred usage:

- `CancellationReason`

Avoid:

- Observation
- Note
- Generic description

---

### Cancelled By

`Cancelled By` identifies the user or system responsible for the Cancellation.

---

### Cancelled At

`Cancelled At` is the instant when the Cancellation was completed.

---

## 12. Fulfillment Terms

### Fulfillment

`Fulfillment` is the business process that prepares and delivers an approved and inventory-backed Order.

Fulfillment may include:

- Picking
- Packing
- Dispatch
- Shipment
- Delivery confirmation

---

### Ready for Fulfillment

`Ready for Fulfillment` means that the Order completed all required preconditions to begin Fulfillment.

Typical preconditions:

- Required approvals completed
- Inventory reserved
- Required payment condition satisfied
- Order data finalized

Preferred status:

```text
READY_FOR_FULFILLMENT
```

---

### Shipment

A `Shipment` represents the physical dispatch and delivery of all or part of an Order.

---

### Carrier

A `Carrier` is the organization responsible for transporting a Shipment.

---

### Tracking Number

A `Tracking Number` is the external identifier used to track a Shipment.

---

### Estimated Delivery Date

`Estimated Delivery Date` is the expected business date for delivery.

It is not the same as a timestamped event instant.

---

## 13. Payment Terms

### Payment

A `Payment` represents financial settlement associated with an Order.

A Payment may be:

- Pending
- Authorized
- Confirmed
- Failed
- Cancelled
- Refunded

---

### Payment Method

`Payment Method` represents the commercial method used to settle an Order.

Examples:

- Invoice
- Bank transfer
- Credit
- External payment provider

---

### Payment Status

`Payment Status` represents the current lifecycle state of a Payment.

Payment Status must not be reused as Order Status.

---

### Payment Confirmation

`Payment Confirmation` is the successful recognition of the expected financial settlement.

---

### External Transaction Reference

An `External Transaction Reference` is the identifier assigned by a Payment provider or financial system.

---

## 14. Identity and Access Terms

### User

A `User` is a person or system identity that interacts with the platform.

A User is not automatically a Customer.

---

### Authenticated User

An `Authenticated User` is a User whose identity was validated by the configured identity provider.

---

### Role

A `Role` groups permissions associated with a responsibility.

Examples:

- Customer User
- Sales Representative
- Approver
- Operations User
- Administrator

---

### Permission

A `Permission` authorizes a specific business action or resource access.

Authorization rules should use business-relevant permissions where practical.

---

### User Context

`User Context` contains the identity information required by an application operation.

It may contain:

- User identifier
- Roles
- Permissions
- Customer association
- Correlation identifier

Avoid passing framework-specific security objects into the Domain Layer.

---

### System Actor

A `System Actor` is a trusted technical identity that performs automated operations.

Examples:

- Scheduled process
- Message consumer
- External integration
- Administrative automation

---

## 15. Audit and Traceability Terms

### Audit Record

An `Audit Record` is an immutable record of a relevant business operation.

It should contain:

- Operation
- Entity type
- Entity identifier
- Acting user or system
- Timestamp
- Correlation identifier
- Safe change information

---

### Audit Trail

An `Audit Trail` is the chronological collection of Audit Records associated with a business entity or process.

---

### Business History

`Business History` represents domain-relevant historical information, such as Order Status History.

Business History is not always identical to a technical Audit Trail.

---

### Correlation Identifier

A `Correlation Identifier` is a value used to trace a request or business flow across components.

Preferred usage:

- `CorrelationId`
- `correlationId`

It should propagate through:

- HTTP calls
- Application operations
- Logs
- Domain events
- Integration events
- Message consumers

---

### Trace Identifier

A `Trace Identifier` is an observability identifier associated with distributed tracing.

It may differ from the business Correlation Identifier.

The two terms must not be treated as interchangeable without an explicit design decision.

---

## 16. Event Terms

### Domain Event

A `Domain Event` represents a relevant fact that occurred inside a domain model.

Examples:

- `OrderCreated`
- `OrderSubmitted`
- `OrderApproved`
- `OrderCancelled`

A Domain Event:

- Uses past tense
- Represents something that already occurred
- Is immutable
- May remain internal to the bounded context

---

### Integration Event

An `Integration Event` is a message published for consumption outside the originating bounded context.

It may be derived from one or more Domain Events.

An Integration Event must define:

- Event type
- Schema version
- Event identifier
- Occurrence timestamp
- Correlation metadata
- Business payload

Not every Domain Event becomes an Integration Event.

---

### Event Type

`Event Type` is the stable identifier that classifies an event contract.

Example:

```text
order.submitted.v1
```

---

### Event Version

`Event Version` identifies the schema version of an Integration Event.

Breaking schema changes require an explicit versioning strategy.

---

### Event Identifier

An `Event Identifier` uniquely identifies an event instance.

It supports:

- Idempotency
- Duplicate detection
- Traceability
- Operational investigation

---

### Event Occurred At

`Event Occurred At` is the instant when the business fact represented by the event occurred.

It is not necessarily the same as the publication timestamp.

---

### Event Publisher

An `Event Publisher` is the component responsible for making Integration Events available to external consumers.

---

### Event Consumer

An `Event Consumer` is a component that processes an Integration Event.

Consumers must assume duplicate delivery and support idempotent processing.

---

## 17. Transactional Outbox Terms

### Transactional Outbox

The `Transactional Outbox` is a reliability pattern used to store business changes and pending Integration Events within the same local database transaction.

It avoids the inconsistency risk of:

1. Committing business data.
2. Failing before publishing the related event.

---

### Outbox Record

An `Outbox Record` is the persisted representation of an Integration Event awaiting publication.

Typical fields:

- Identifier
- Aggregate type
- Aggregate identifier
- Event type
- Destination
- Payload
- Status
- Attempts
- Next attempt timestamp
- Creation timestamp
- Publication timestamp
- Correlation identifier

---

### Outbox Publisher

The `Outbox Publisher` retrieves pending Outbox Records and publishes them to the configured message infrastructure.

---

### Publication Attempt

A `Publication Attempt` is one attempt to publish an Outbox Record.

Attempts must be bounded and observable.

---

### Publication Failure

A `Publication Failure` represents an unsuccessful attempt to publish an Integration Event.

It must not invalidate the business transaction already completed.

---

## 18. Reliability Terms

### Idempotency

`Idempotency` is the property that allows an operation to be repeated without duplicating its business effect.

Idempotency is required for operations vulnerable to:

- Client retries
- Network retries
- Duplicate messages
- Consumer redelivery
- Concurrent duplicate requests

---

### Idempotency Key

An `Idempotency Key` is the identifier used to recognize repeated attempts of the same business operation.

Its scope and retention period must be explicit.

---

### Duplicate Request

A `Duplicate Request` is a repeated invocation representing the same intended business operation.

A Duplicate Request must not produce duplicate Orders, Reservations, Payments, or events.

---

### Retry

A `Retry` is a controlled re-execution of a failed technical operation.

Retries should only apply to transient failures.

---

### Transient Failure

A `Transient Failure` is a temporary failure that may succeed if attempted again.

Examples:

- Temporary network interruption
- Temporary service unavailability
- Database connection timeout

---

### Permanent Failure

A `Permanent Failure` is an error that is not expected to succeed through automatic retry.

Examples:

- Invalid request
- Unauthorized operation
- Unknown Product
- Business rule violation

---

### Timeout

A `Timeout` is the maximum time allowed for an operation or integration response.

All remote integrations must use explicit timeouts.

---

### Circuit Breaker

A `Circuit Breaker` is a resilience mechanism that temporarily prevents calls to a repeatedly failing dependency.

It is not a substitute for:

- Timeouts
- Correct error handling
- Idempotency
- Retry limits

---

### Compensating Action

A `Compensating Action` is a business operation used to reduce or reverse the effect of a previously completed step in an eventually consistent process.

Example:

- Releasing Inventory after Order Cancellation

A compensating action is not necessarily a technical rollback.

---

## 19. Consistency Terms

### Aggregate

An `Aggregate` is a cluster of domain objects protected by a single consistency boundary.

All modifications to an Aggregate occur through its Aggregate Root.

---

### Aggregate Root

An `Aggregate Root` is the entry point responsible for protecting the invariants of an Aggregate.

Initial aggregate-root candidates:

- `Order`
- `ApprovalProcess`
- `InventoryReservation`

---

### Invariant

An `Invariant` is a business rule that must always remain true within a consistency boundary.

Example:

- A submitted Order must contain at least one Order Item.

---

### Transaction Boundary

A `Transaction Boundary` defines the set of local changes that must succeed or fail atomically.

A local transaction must not depend on completing remote calls unless explicitly justified.

---

### Strong Consistency

`Strong Consistency` means that all required data is immediately consistent when an operation completes.

It is generally limited to a local aggregate and local transaction.

---

### Eventual Consistency

`Eventual Consistency` means that related bounded contexts may become consistent after asynchronous processing completes.

Temporary intermediate states are expected and must be explicitly modeled.

---

### Snapshot

A `Snapshot` is an immutable copy of selected external or calculated information preserved for historical consistency.

Examples:

- Customer Snapshot
- Product Snapshot
- Pricing Snapshot

---

## 20. Architecture Terms

### Bounded Context

A `Bounded Context` is an explicit boundary within which a domain model and language are consistent.

A term may have different meanings in different Bounded Contexts.

---

### Context Map

A `Context Map` documents the relationships and dependencies between Bounded Contexts.

---

### Core Domain

The `Core Domain` is the domain area that provides the primary strategic value of the platform.

Initial Core Domain:

- Order Management

---

### Supporting Domain

A `Supporting Domain` is a business-specific capability required by the Core Domain but not the primary differentiator.

Examples:

- Approval
- Pricing
- Inventory
- Fulfillment

---

### Generic Subdomain

A `Generic Subdomain` provides a common capability that is not unique to the business.

Examples:

- Identity and Access
- Notifications
- Audit infrastructure

---

### Port

A `Port` is an interface that defines how the Application or Domain Layer communicates with an external capability.

Examples:

- `LoadCustomerPort`
- `CalculatePricingPort`
- `ReserveInventoryPort`
- `PublishOrderEventPort`

---

### Inbound Port

An `Inbound Port` defines an application use case that can be invoked by an external actor.

Examples:

- `CreateOrderUseCase`
- `SubmitOrderUseCase`
- `CancelOrderUseCase`

---

### Outbound Port

An `Outbound Port` defines a capability required by the Application or Domain Layer.

Examples:

- Persistence
- Messaging
- Customer lookup
- Product lookup
- Inventory reservation

---

### Adapter

An `Adapter` implements or invokes a Port to connect the application to an external technology or actor.

Examples:

- REST Controller
- JPA Repository Adapter
- SQS Publisher Adapter
- HTTP Customer Client Adapter

---

### Use Case

A `Use Case` is an application-level operation that coordinates domain behavior and external dependencies.

Examples:

- Create Order
- Add Order Item
- Submit Order
- Approve Order
- Cancel Order

A Use Case does not replace domain behavior.

---

## 21. API Terms

### Command

A `Command` represents an intention to change business state.

Examples:

- `CreateOrderCommand`
- `SubmitOrderCommand`
- `CancelOrderCommand`

---

### Query

A `Query` requests information without changing business state.

Examples:

- `GetOrderQuery`
- `SearchOrdersQuery`

---

### Request

A `Request` is the transport-level representation received by an API adapter.

A Request should not automatically become a Domain object.

---

### Response

A `Response` is the transport-level representation returned by an API adapter.

A Response should expose only the information required by the API contract.

---

### Business Error Code

A `Business Error Code` is a stable identifier representing a business or validation failure.

Example:

```text
ORDER_NOT_EDITABLE
```

Error codes should remain stable independently from translated messages.

---

### Validation Error

A `Validation Error` represents invalid external input.

Examples:

- Missing required field
- Invalid length
- Invalid format
- Unsupported value

---

### Business Rule Violation

A `Business Rule Violation` occurs when validly structured input attempts an operation prohibited by the domain.

Example:

- Cancelling a completed Order

---

## 22. Time Terms

### Instant

An `Instant` represents a precise point on the global timeline.

Persisted instants should use UTC.

Examples:

- `submittedAt`
- `cancelledAt`
- `occurredAt`

---

### Business Date

A `Business Date` represents a calendar date relevant to a business process without an exact time.

Examples:

- Expected delivery date
- Billing date

---

### Duration

A `Duration` represents an exact elapsed amount of time.

Examples:

- Request timeout
- Retry delay

---

### Clock

A `Clock` is the abstraction used to obtain the current time.

Time-dependent domain logic should use an injected Clock rather than direct static calls.

---

## 23. Preferred Naming Examples

| Business Concept | Preferred Name | Avoid |
|---|---|---|
| Corporate purchase request | Order | Transaction, Request |
| Product included in an order | Order Item | Detail, Row |
| Finalize a draft for processing | Submit Order | Final Save |
| Preserve customer information | Customer Snapshot | Customer Copy |
| Preserve product information | Product Snapshot | Product Cache |
| Reserve requested quantities | Inventory Reservation | Stock Lock |
| Release reserved quantities | Inventory Release | Unlock Stock |
| Reject an approval | Reject Order | Update approval status |
| Return for correction | Request Review | Reopen flag |
| Terminate an eligible order | Cancel Order | Delete Order |
| Unique business flow identifier | Correlation Identifier | Log ID |
| Business fact that occurred | Domain Event | Notification Object |
| External event contract | Integration Event | SQS message contract |
| Repeated operation protection | Idempotency | Duplicate check |
| Local atomic consistency boundary | Transaction Boundary | Method transaction |
| Historical commercial value | Pricing Snapshot | Cached price |

---

## 24. Prohibited or Discouraged Terms

The following terms should be avoided when a more precise domain term exists:

- Data
- Detail
- Record
- Object
- Process
- Generic service
- Generic manager
- Utility
- Handler
- Status flag
- Type code
- Observation
- Final value
- Request, when the concept is specifically an Order
- Transaction, when the concept is specifically an Order
- Customer validation, when the concept is Customer Eligibility
- Stock lock, when the concept is Inventory Reservation

These terms are not universally forbidden.

They should be used only when they accurately represent the intended concept.

---

## 25. Context-Specific Language

The same word may have different meanings in different Bounded Contexts.

Examples:

### Status

- `Order Status` belongs to Order Management.
- `Payment Status` belongs to Payment.
- `Reservation Status` belongs to Inventory.
- `Shipment Status` belongs to Fulfillment.

These concepts must use distinct types.

---

### Customer

- Customer Context owns current Customer master data.
- Order Management owns Customer References and Customer Snapshots.
- Payment may use a payer concept with different information.
- Fulfillment may use a recipient concept.

The complete Customer entity should not be shared across contexts.

---

### Product

- Product Catalog owns current Product data.
- Order Management owns Product References and Product Snapshots.
- Inventory owns stock-related Product identifiers and quantities.
- Pricing owns price-relevant Product attributes.

---

### Order

- Order Management owns the business Order.
- Inventory may refer to the Order only by identifier and reservation contract.
- Payment may refer to a payable obligation associated with an Order.
- Fulfillment may refer to a fulfillment request derived from an Order.

Other contexts must not reuse the internal Order aggregate.

---

## 26. Source Code Naming Rules

The following naming rules should guide source code:

- Aggregate roots use business nouns.
- Commands use imperative business intent.
- Events use past-tense business facts.
- Use cases use explicit business operations.
- Ports describe required capabilities.
- Adapters identify technology only outside the Domain Layer.
- Value objects use domain terminology.
- Boolean variables should describe a business fact.
- Exception names should describe the violated business rule.
- Generic suffixes such as `Helper`, `Util`, or `Manager` should be avoided.

Examples:

```text
Order
OrderItem
Money
CustomerSnapshot
SubmitOrderCommand
SubmitOrderUseCase
OrderSubmitted
LoadCustomerPort
JpaOrderRepositoryAdapter
OrderNotEditableException
```

Avoid:

```text
OrderManager
OrderUtils
OrderData
ProcessOrder
StatusHelper
GenericHandler
CommonService
```

---

## 27. API Naming Rules

API contracts should follow these principles:

- Use business nouns for resources.
- Use HTTP semantics consistently.
- Avoid exposing persistence terminology.
- Avoid exposing internal class names.
- Use stable business error codes.
- Use explicit operation endpoints only when a business action is not naturally represented as resource state.

Examples:

```text
POST /orders
POST /orders/{orderId}/items
POST /orders/{orderId}/submission
POST /orders/{orderId}/cancellation
GET /orders/{orderId}
GET /orders
```

Possible action-oriented alternatives must be recorded in the API design documentation.

Avoid:

```text
POST /saveOrder
POST /updateStatus
GET /findOrderByPrimaryKey
POST /process
```

---

## 28. Event Naming Rules

Domain and Integration Events should:

- Use past tense
- Represent completed facts
- Be immutable
- Include stable identifiers
- Avoid technical implementation names

Preferred:

```text
OrderCreated
OrderSubmitted
OrderApprovalRequired
OrderApproved
OrderRejected
InventoryReservationRequested
InventoryReserved
InventoryReservationFailed
OrderCancelled
OrderCompleted
```

Avoid:

```text
CreateOrderEvent
SendOrderKafkaMessage
OrderProcessingDTO
UpdateOrderStatusEvent
```

---

## 29. Test Naming Rules

Test names should describe observable business behavior.

Preferred examples:

```text
testSubmitShouldRejectOrderWithoutItems
testCancelShouldRejectCompletedOrder
testAddItemShouldRecalculateOrderTotals
testApproveShouldRecordApproverAndDecisionDate
```

Assertions should include meaningful descriptions.

Example:

```java
assertThat(order.status())
        .as("Order status after a successful submission")
        .isEqualTo(OrderStatus.SUBMITTED);
```

Tests should avoid vague names such as:

```text
testOrder
testSuccess
testError
testMethod
```

---

## 30. Logging Language Rules

Logs should use stable business terminology.

Preferred:

```text
Order submitted successfully
Inventory reservation requested
Order cancellation rejected because status is COMPLETED
Approval decision recorded
```

Avoid:

```text
Object saved
Process completed
Status updated
Operation failed
```

Logs should include relevant identifiers without exposing sensitive data.

---

## 31. Open Language Decisions

The following terminology still requires confirmation:

1. Whether `Order Number` is generated at creation or submission.
2. Whether `Review Request` returns an Order to `DRAFT` or to a dedicated status.
3. Whether `APPROVED` is a persistent Order Status or a transient transition.
4. Whether `PROCESSING` is too generic and should be replaced by more precise states.
5. Whether `Customer User` should be renamed to `Buyer`.
6. Whether `Sales Representative` is the correct official business term.
7. Whether `Inventory Reservation` occurs per Order or per Order Item.
8. Whether `Pricing Snapshot` belongs inside the Order aggregate.
9. Whether `Approval Process` is an independent aggregate.
10. Whether `Fulfillment` is the official term or whether the business uses `Order Execution`.
11. Whether `Payment` belongs to the first implementation scope.
12. Whether `Notification` should be modeled as a business capability or only as infrastructure.
13. Whether `Correlation Identifier` and `Trace Identifier` remain separate concepts.
14. Whether API operations should use subresources or explicit business action endpoints.
15. Whether `Customer Eligibility` includes credit evaluation or only operational permission.

These decisions will be refined through bounded-context modeling, ADRs, and domain workshops.

---

## 32. Governance

This document is the initial source of truth for project terminology.

When adding a new domain concept:

1. Confirm that an existing term does not already represent the concept.
2. Define the concept in business language.
3. Identify the owning Bounded Context.
4. Define preferred and discouraged names.
5. Update requirements, documentation, APIs, events, tests, and source code consistently.
6. Record significant terminology changes in an ADR when they affect architecture or public contracts.

Accepted terminology should not be changed casually.

A terminology change may indicate a deeper change in business understanding.

---

## 33. Next Documentation Step

The next domain document will define the bounded contexts and their responsibilities in greater detail:

```text
docs/domain/bounded-contexts.md
```

It will establish:

- Context ownership
- Context responsibilities
- Included and excluded concepts
- Upstream and downstream dependencies
- Synchronous and asynchronous interactions
- Data ownership
- Initial context boundaries
