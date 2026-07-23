# Naming Conventions

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Naming Conventions |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the naming conventions adopted by the Enterprise Order Platform.

The objective is to establish a consistent vocabulary across:

- source code
- domain models
- application use cases
- REST APIs
- database objects
- messages and events
- configuration
- infrastructure
- observability
- tests
- documentation

Naming should communicate intent, business meaning and architectural responsibility.

---

# 2. General Principles

Names must be:

- meaningful
- explicit
- consistent
- searchable
- aligned with the ubiquitous language
- free from unnecessary abbreviations

A good name should reduce the need for comments.

Prefer business terminology over technical shortcuts.

Good:

```text
OrderApprovalPolicy

InventoryReservation

CustomerCreditLimit
```

Avoid:

```text
OrderHelper

InventoryManager

CustomerUtil
```

---

# 3. Language

All technical names must be written in English.

This includes:

- packages
- classes
- methods
- variables
- database objects
- REST resources
- event names
- configuration properties
- logs
- metrics
- tests

Business terminology must remain consistent with the project's ubiquitous language.

---

# 4. Avoid Ambiguous Terms

Avoid generic terms that do not communicate responsibility.

Examples to avoid:

```text
Manager

Processor

Handler

Service

Helper

Util

Data

Info

Object

Common

Misc
```

These terms may be used only when combined with a precise architectural or business meaning.

Acceptable:

```text
OrderCommandHandler

PaymentEventProcessor

CustomerApplicationService
```

Avoid:

```text
OrderManager

DataProcessor

CommonUtils
```

---

# 5. Abbreviations

Avoid abbreviations unless they are:

- widely understood
- part of the business vocabulary
- part of an established technical standard

Acceptable:

```text
API

HTTP

UUID

JWT

SQL

DTO

URL

URI

ID
```

Avoid private or unclear abbreviations:

```text
OrdProc

CustMgr

InvRes

PmtSvc
```

In Java identifiers, acronyms should follow normal casing.

Preferred:

```java
customerId
apiClient
httpStatus
jwtToken
```

Avoid:

```java
customerID
APIClient
HTTPStatus
JWTToken
```

---

# 6. Java Package Names

Package names must:

- use lowercase letters
- contain no underscores
- represent business or architectural concepts
- remain concise
- avoid implementation details when unnecessary

Good:

```text
com.enterprise.orderplatform.domain.order

com.enterprise.orderplatform.application.approval

com.enterprise.orderplatform.infrastructure.persistence.order
```

Avoid:

```text
com.enterprise.orderplatform.domain.Order

com.enterprise.orderplatform.application.order_services

com.enterprise.orderplatform.misc
```

---

# 7. Java Class Names

Java classes use PascalCase.

Examples:

```java
Order

OrderItem

OrderApprovalPolicy

CreateOrderService

JpaOrderRepository
```

Class names should communicate the primary responsibility of the type.

Avoid suffixes that do not add architectural meaning.

---

# 8. Interfaces

Interfaces should be named according to the capability they expose.

Good:

```java
OrderRepository

InventoryGateway

PaymentAuthorizer

DomainEventPublisher

CreateOrderUseCase
```

Avoid prefixing interfaces with `I`.

Incorrect:

```java
IOrderRepository

ICustomerService
```

The abstraction should be identifiable from its contract and package.

---

# 9. Abstract Classes

Abstract classes should not automatically use the `Abstract` prefix.

Use it only when it improves clarity.

Acceptable:

```java
AbstractDomainEvent

AbstractAuditableEntity
```

Prefer a domain-oriented name when possible.

Example:

```java
BaseAggregateRoot
```

must be avoided unless the abstraction truly represents a meaningful and stable shared concept.

---

# 10. Aggregate Roots

Aggregate Root names must use singular business nouns.

Examples:

```java
Order

Customer

InventoryReservation

Payment

ApprovalWorkflow
```

Avoid technical suffixes:

```java
OrderAggregate

OrderModel

OrderDomainObject
```

The package and behavior should make the aggregate role clear.

---

# 11. Entities

Entity names must represent domain concepts with identity.

Examples:

```java
OrderItem

ApprovalStep

PaymentAttempt

Shipment
```

Avoid suffixing domain entities with `Entity`.

Incorrect in the domain:

```java
OrderEntity

CustomerEntity
```

The `Entity` suffix may be used for dedicated persistence models.

Example:

```java
OrderJpaEntity

OrderItemJpaEntity
```

---

# 12. Value Objects

Value Object names must represent the business concept they encapsulate.

Examples:

```java
OrderId

CustomerId

Money

EmailAddress

ShippingAddress

Quantity

Percentage
```

Avoid suffixing every type with `ValueObject`.

Incorrect:

```java
MoneyValueObject

OrderIdValueObject
```

---

# 13. Domain Services

Domain Service names should describe a stateless business capability or decision.

Examples:

```java
OrderPricingService

InventoryAllocationService

CreditEligibilityService
```

Prefer a more precise suffix when applicable:

```java
OrderApprovalPolicy

ShippingCostCalculator

PaymentEligibilityRule
```

Avoid generic names such as:

```java
OrderDomainService

BusinessService
```

unless no more specific concept exists.

---

# 14. Policies

Policy classes should use the `Policy` suffix.

Examples:

```java
OrderApprovalPolicy

CancellationPolicy

CustomerCreditPolicy

InventoryAllocationPolicy
```

Policies should represent business decisions or configurable rules.

---

# 15. Specifications

Specifications should describe the condition they evaluate.

Examples:

```java
OrderCanBeApprovedSpecification

CustomerHasAvailableCreditSpecification

InventoryCanFulfillOrderSpecification
```

For concise domain APIs, condition-oriented names may be used:

```java
EligibleForApproval

AvailableForReservation
```

Use one convention consistently within each bounded context.

---

# 16. Business Rules

Explicit rule objects should use the `Rule` suffix.

Examples:

```java
MaximumOrderValueRule

ApprovalAuthorityRule

InventoryAvailabilityRule
```

Rule names must describe the enforced constraint.

Avoid:

```java
ValidationRule1

GenericBusinessRule
```

---

# 17. Factories

Factories should use the name of the object they create, followed by `Factory`.

Examples:

```java
OrderFactory

PaymentFactory

DomainEventFactory
```

Static factory methods should describe the creation intent.

Examples:

```java
Order.create(...)

Order.restore(...)

Money.of(...)

Payment.pending(...)
```

Avoid vague static methods:

```java
build()

generate()

make()
```

when a more precise intent exists.

---

# 18. Commands

Commands represent an intention to change application state.

Command names should use an imperative business action followed by `Command`.

Examples:

```java
CreateOrderCommand

ApproveOrderCommand

CancelOrderCommand

ReserveInventoryCommand
```

Avoid:

```java
OrderCreationCommand

OrderCommand

ProcessOrderCommand
```

The action should be explicit.

---

# 19. Command Handlers

Command handlers should follow one of these patterns:

```java
CreateOrderCommandHandler

ApproveOrderCommandHandler
```

or, when the application exposes use cases directly:

```java
CreateOrderService

ApproveOrderService
```

Do not mix both conventions indiscriminately.

The project should adopt one dominant model per application module.

---

# 20. Queries

Queries represent information retrieval without changing state.

Query names should describe the requested information followed by `Query`.

Examples:

```java
GetOrderByIdQuery

SearchOrdersQuery

ListPendingApprovalsQuery
```

Avoid:

```java
OrderQuery

GetDataQuery

FindInformationQuery
```

---

# 21. Query Handlers

Query handlers should follow the corresponding query name.

Examples:

```java
GetOrderByIdQueryHandler

SearchOrdersQueryHandler

ListPendingApprovalsQueryHandler
```

Dedicated read services may use:

```java
OrderQueryService

ApprovalQueryService
```

when they group cohesive query operations.

---

# 22. Use Cases

Use-case interfaces should use a verb-oriented business name followed by `UseCase`.

Examples:

```java
CreateOrderUseCase

ApproveOrderUseCase

CancelOrderUseCase

SearchOrdersUseCase
```

Avoid generic interfaces:

```java
OrderUseCase

BaseUseCase

GenericUseCase
```

---

# 23. Application Services

Application services should represent business orchestration.

Examples:

```java
CreateOrderService

ApproveOrderService

CancelOrderService

SynchronizeInventoryService
```

Avoid vague names:

```java
OrderServiceImpl

BusinessServiceImpl

ApplicationManager
```

The `Impl` suffix should not be used by default.

---

# 24. Ports

Ports should be named by the capability required by the application.

Examples:

```java
OrderRepository

InventoryGateway

PaymentGateway

CustomerDirectory

NotificationSender

DomainEventPublisher
```

Avoid exposing implementation technology in port names.

Incorrect:

```java
KafkaEventPublisherPort

PostgresOrderRepositoryPort

RedisCachePort
```

Technology belongs in adapter names.

---

# 25. Inbound Ports

Inbound ports represent application capabilities.

Examples:

```java
CreateOrderUseCase

ApproveOrderUseCase

GetOrderDetailsUseCase
```

The `InboundPort` suffix is unnecessary when the contract already communicates intent.

---

# 26. Outbound Ports

Outbound ports represent external capabilities required by the application.

Examples:

```java
OrderRepository

InventoryGateway

PaymentAuthorizer

CustomerProvider

ClockProvider
```

The `OutboundPort` suffix may be used only when the architecture requires explicit differentiation.

---

# 27. Adapters

Adapter names should identify both the technology and the implemented capability.

Examples:

```java
JpaOrderRepository

KafkaDomainEventPublisher

RedisOrderCache

RestInventoryGateway

SqsNotificationSender
```

Avoid:

```java
OrderRepositoryImpl

EventPublisherImpl

ExternalServiceAdapter
```

---

# 28. Controllers

REST controllers should use the resource or business capability followed by `Controller`.

Examples:

```java
OrderController

OrderApprovalController

CustomerOrderController
```

Avoid:

```java
OrderRestController

OrderApi

OrderEndpoint
```

The package and annotation already identify the REST concern.

---

# 29. Request Models

Inbound HTTP models should use the operation followed by `Request`.

Examples:

```java
CreateOrderRequest

ApproveOrderRequest

SearchOrdersRequest

UpdateShippingAddressRequest
```

Avoid generic names:

```java
OrderRequest

RequestDto

OrderPayload
```

---

# 30. Response Models

Outbound HTTP models should describe the returned resource or representation followed by `Response`.

Examples:

```java
OrderResponse

OrderSummaryResponse

OrderDetailsResponse

ApprovalResultResponse
```

Avoid:

```java
OrderDto

ResponseDto

GenericResponse
```

---

# 31. Internal DTOs

Use the `Dto` suffix only when the object is explicitly a data transfer model between technical boundaries.

Examples:

```java
InventoryApiDto

PaymentProviderDto

OrderProjectionDto
```

Do not use `Dto` for domain objects or application commands.

---

# 32. Mappers

Mapper names should identify both source and target when ambiguity exists.

Examples:

```java
OrderRequestMapper

OrderResponseMapper

OrderPersistenceMapper

OrderEventMapper
```

For highly explicit integration boundaries:

```java
InventoryApiResponseMapper

KafkaOrderEventMapper
```

Avoid generic names:

```java
Mapper

CommonMapper

ObjectMapperUtil
```

---

# 33. Validators

Validators should describe what they validate.

Examples:

```java
CreateOrderValidator

ApprovalRequestValidator

OrderTransitionValidator

CustomerEligibilityValidator
```

Avoid:

```java
Validator

GenericValidator

OrderValidationService
```

Structural validation and business validation should not be hidden behind the same vague abstraction.

---

# 34. Exception Names

Exceptions must describe the failure condition.

Examples:

```java
OrderNotFoundException

InvalidOrderTransitionException

InventoryReservationException

PaymentAuthorizationException

CustomerCreditExceededException
```

Avoid generic names:

```java
BusinessException

ApplicationException

ServiceException
```

Base exception types may exist, but concrete exceptions must remain meaningful.

---

# 35. Event Names

Domain Events must describe something that already happened.

Use past tense followed by `Event`.

Examples:

```java
OrderCreatedEvent

OrderApprovedEvent

OrderCancelledEvent

InventoryReservedEvent

PaymentAuthorizedEvent
```

Avoid imperative names:

```java
CreateOrderEvent

ApproveOrderEvent

ReserveInventoryEvent
```

These resemble commands rather than events.

---

# 36. Integration Events

Integration Events should represent externally published facts.

Examples:

```java
OrderCreatedIntegrationEvent

OrderApprovedIntegrationEvent

InventoryReservationFailedIntegrationEvent
```

When the package already makes the event type explicit, the shorter name may be used:

```java
OrderCreatedEvent
```

The selected convention must remain consistent.

---

# 37. Event Payloads

Serialized event contracts should use one of these patterns:

```java
OrderCreatedEventPayload

OrderCreatedEventMessage
```

Use `Payload` for the event body and `Message` when the object represents the complete envelope.

Example:

```java
OrderCreatedEventMessage
OrderCreatedEventPayload
```

---

# 38. Event Envelopes

Generic event envelopes should use an explicit name.

Examples:

```java
EventEnvelope<T>

IntegrationEventEnvelope<T>

MessageEnvelope<T>
```

Fields should use consistent names:

```text
eventId

eventType

eventVersion

occurredAt

aggregateId

correlationId

causationId

payload
```

---

# 39. Kafka Topics

Kafka topic names must be:

- lowercase
- separated with dots
- business-oriented
- versioned when the contract requires it
- independent from consumer names

Recommended pattern:

```text
<domain>.<entity>.<event>.v<version>
```

Examples:

```text
orders.order.created.v1

orders.order.approved.v1

inventory.reservation.completed.v1

payments.authorization.failed.v1
```

Avoid:

```text
order-created-topic

kafka-order-events

orderQueue
```

---

# 40. Kafka Consumer Groups

Consumer group names should identify the consuming application and responsibility.

Recommended pattern:

```text
<application>.<capability>.v<version>
```

Examples:

```text
inventory-service.order-reservation.v1

notification-service.order-created.v1

analytics-service.order-events.v1
```

Do not name consumer groups after deployment instances.

---

# 41. Kafka Keys

Kafka message keys should represent the entity or ordering boundary.

Examples:

```text
orderId

customerId

reservationId
```

The key name should be documented in the event contract.

---

# 42. RabbitMQ Exchanges

Exchange names should be lowercase and dot-separated.

Recommended pattern:

```text
<domain>.<purpose>
```

Examples:

```text
orders.events

payments.commands

notifications.events
```

---

# 43. RabbitMQ Queues

Queue names should identify the consumer and responsibility.

Recommended pattern:

```text
<consumer>.<purpose>
```

Examples:

```text
notification-service.order-created

billing-service.order-approved

inventory-service.reserve-order-items
```

Avoid queue names based only on the producer.

---

# 44. Routing Keys

Routing keys should use business-oriented dot-separated names.

Examples:

```text
order.created

order.approved

inventory.reservation.failed
```

Use past tense for facts and imperative form for commands.

Event:

```text
order.created
```

Command:

```text
inventory.reserve
```

---

# 45. SQS Queues

SQS queue names should use lowercase kebab-case.

Recommended pattern:

```text
<application>-<capability>-<environment>
```

Examples:

```text
inventory-order-reservation-prod

notification-order-events-dev
```

Dead-letter queues should use the `-dlq` suffix.

Example:

```text
inventory-order-reservation-prod-dlq
```

---

# 46. REST Resource Names

REST resources must use plural nouns.

Examples:

```text
/orders

/customers

/inventory-reservations

/payment-authorizations
```

Avoid verbs in resource paths.

Incorrect:

```text
/createOrder

/getCustomers

/cancelOrder
```

---

# 47. REST Resource Actions

Business actions that do not map cleanly to CRUD may use explicit subresources or action endpoints.

Preferred:

```text
POST /orders/{orderId}/approvals

POST /orders/{orderId}/cancellations

POST /orders/{orderId}/inventory-reservations
```

Acceptable when necessary:

```text
POST /orders/{orderId}:approve
```

One convention should be adopted consistently.

---

# 48. REST Path Parameters

Path parameter names use lower camel case.

Examples:

```text
/orders/{orderId}

/customers/{customerId}

/orders/{orderId}/items/{itemId}
```

Avoid generic identifiers:

```text
/orders/{id}
```

when multiple identifiers may exist in the same API.

---

# 49. REST Query Parameters

Query parameters use lower camel case.

Examples:

```text
customerId

createdFrom

createdTo

orderStatus

page

size

sort
```

Boolean filters should read naturally.

Examples:

```text
includeCancelled=true

onlyPending=true
```

---

# 50. REST Headers

Standard headers should be preferred.

Custom headers use canonical HTTP casing.

Examples:

```text
X-Correlation-Id

X-Request-Id

Idempotency-Key
```

Do not create custom headers for information that belongs in the body or standard headers.

---

# 51. API Operation IDs

OpenAPI operation identifiers should use lower camel case and a verb-oriented name.

Examples:

```text
createOrder

getOrderById

searchOrders

approveOrder

cancelOrder
```

Operation IDs must remain unique across the API specification.

---

# 52. API Error Codes

Application error codes must use uppercase snake case.

Examples:

```text
ORDER_NOT_FOUND

INVALID_ORDER_TRANSITION

INVENTORY_UNAVAILABLE

PAYMENT_AUTHORIZATION_FAILED

CUSTOMER_CREDIT_EXCEEDED
```

Error codes are stable external contracts and must not contain implementation details.

---

# 53. Database Tables

Database table names use lowercase snake case.

Use singular or plural consistently across the schema.

This project adopts singular table names.

Examples:

```sql
order_header

order_item

inventory_reservation

payment_authorization

outbox_event
```

Avoid reserved words.

Because `order` is commonly reserved, use an explicit business table name such as:

```sql
order_header
```

---

# 54. Database Columns

Column names use lowercase snake case.

Examples:

```sql
order_id

customer_id

created_at

updated_at

approval_status

total_amount
```

Boolean columns should use natural predicates.

Examples:

```sql
is_active

has_pending_approval

requires_review
```

Use one convention consistently.

---

# 55. Primary Keys

Primary key columns should identify the entity.

Examples:

```sql
order_id

customer_id

reservation_id
```

Avoid generic `id` columns in complex schemas when explicit names improve query readability.

Primary key constraint names follow:

```text
pk_<table>
```

Example:

```text
pk_order_header
```

---

# 56. Foreign Keys

Foreign key columns should use the referenced entity name followed by `_id`.

Examples:

```sql
customer_id

order_id

payment_id
```

Foreign key constraint names follow:

```text
fk_<source_table>_<target_table>
```

Example:

```text
fk_order_item_order_header
```

When multiple references to the same table exist, include the business role.

Example:

```text
fk_order_header_created_by_user
```

---

# 57. Unique Constraints

Unique constraint names follow:

```text
uk_<table>_<column_or_business_key>
```

Examples:

```text
uk_order_header_external_reference

uk_outbox_event_event_id
```

---

# 58. Check Constraints

Check constraint names follow:

```text
ck_<table>_<rule>
```

Examples:

```text
ck_order_item_quantity_positive

ck_order_header_total_amount_non_negative
```

The name should describe the enforced invariant.

---

# 59. Indexes

Index names follow:

```text
idx_<table>_<columns_or_purpose>
```

Examples:

```text
idx_order_header_customer_id

idx_order_header_status_created_at

idx_outbox_event_pending_dispatch
```

Do not include every indexed column when a concise business-purpose name is clearer.

---

# 60. Database Sequences

Sequence names follow:

```text
seq_<entity_or_table>
```

Examples:

```text
seq_order_number

seq_invoice_number
```

UUID-based entities generally do not require sequences.

---

# 61. Database Views

View names use the `vw_` prefix.

Examples:

```text
vw_order_summary

vw_pending_approval
```

Materialized views use:

```text
mv_<name>
```

Examples:

```text
mv_daily_order_totals
```

---

# 62. Database Functions

Database functions use lowercase snake case and a verb-oriented name.

Examples:

```text
calculate_order_total

normalize_customer_reference

find_available_inventory
```

Avoid generic names such as:

```text
process_data

execute_rule
```

---

# 63. Database Procedures

Stored procedures use lowercase snake case and imperative names.

Examples:

```text
archive_completed_orders

rebuild_order_summary

generate_order_export
```

Business logic should remain outside the database unless justified by architectural constraints.

---

# 64. Flyway Migrations

Migration files follow:

```text
V<version>__<description>.sql
```

Examples:

```text
V1__create_order_schema.sql

V2__create_order_header_table.sql

V3__create_order_item_table.sql

V4__add_order_status_index.sql
```

Descriptions must:

- use lowercase snake case
- describe the change
- avoid vague names
- remain immutable after application

Incorrect:

```text
V5__fix.sql

V6__adjustments.sql

V7__changes.sql
```

---

# 65. Repeatable Migrations

Repeatable migrations follow:

```text
R__<description>.sql
```

Examples:

```text
R__create_order_summary_view.sql

R__refresh_reporting_functions.sql
```

Use repeatable migrations only for objects whose definitions are intentionally recreated.

---

# 66. Spring Configuration Properties

Spring configuration property names use lowercase kebab-case.

Examples:

```yaml
integration:
  inventory:
    base-url: http://inventory-service
    connect-timeout: 2s
    response-timeout: 5s

outbox:
  dispatcher:
    batch-size: 100
    max-attempts: 10
```

Avoid camelCase in configuration files.

---

# 67. Configuration Property Prefixes

Prefixes should represent the capability or integration.

Examples:

```text
integration.inventory

integration.payment

messaging.kafka

outbox.dispatcher

security.oauth2

cache.order
```

Avoid generic prefixes:

```text
app.config

settings

properties
```

---

# 68. Environment Variables

Environment variables use uppercase snake case.

Examples:

```text
INVENTORY_BASE_URL

PAYMENT_RESPONSE_TIMEOUT

POSTGRES_HOST

POSTGRES_DATABASE

KAFKA_BOOTSTRAP_SERVERS
```

Names should remain consistent with the mapped Spring property.

Example:

```text
integration.inventory.base-url
```

maps naturally to:

```text
INTEGRATION_INVENTORY_BASE_URL
```

---

# 69. Secrets

Secret names should communicate scope and purpose.

Examples:

```text
ORDER_DATABASE_PASSWORD

PAYMENT_PROVIDER_CLIENT_SECRET

KAFKA_SASL_PASSWORD
```

Avoid:

```text
PASSWORD

SECRET

TOKEN
```

Secret values must never appear in source code, logs or documentation examples.

---

# 70. Feature Flags

Feature flags use lower camel case in code.

Examples:

```java
newApprovalWorkflowEnabled

inventoryReservationV2Enabled

asyncOrderCreationEnabled
```

External configuration uses kebab-case.

Examples:

```yaml
features:
  new-approval-workflow-enabled: true
```

Avoid negative flag names.

Prefer:

```text
newApprovalWorkflowEnabled
```

Avoid:

```text
disableOldApprovalWorkflow
```

---

# 71. Bean Names

Do not assign explicit Spring bean names unless required.

When multiple beans implement the same contract, use capability-oriented names.

Examples:

```java
@Bean
RestClient inventoryRestClient(...)
```

```java
@Bean
ExecutorService orderValidationExecutor(...)
```

Avoid:

```java
@Bean("bean1")
```

---

# 72. Executor Names

Executors should identify their workload.

Examples:

```text
orderValidationExecutor

reportGenerationExecutor

outboxDispatcherExecutor

integrationCallExecutor
```

Avoid generic names:

```text
taskExecutor

executor

asyncExecutor
```

unless the bean is truly the application-wide default.

---

# 73. Cache Names

Cache names should identify the cached resource or query.

Examples:

```text
customerById

productById

orderSummaryById

approvalRulesByProfile
```

Distributed cache prefixes may include the application and version.

Example:

```text
order-platform:customer-by-id:v1
```

---

# 74. Cache Keys

Cache keys should be deterministic and stable.

Examples:

```text
customer:<customerId>

order-summary:<orderId>

approval-rule:<segment>:<profile>:<orderType>
```

Do not use serialized object `toString()` values unless the format is explicitly stable.

---

# 75. Metrics

Micrometer metric names should use lowercase dot notation.

Examples:

```text
orders.created.total

orders.approval.duration

inventory.reservation.failures

outbox.dispatch.batch.size
```

Counters should generally describe a cumulative event.

Timers should describe a duration-bearing operation.

Avoid embedding dynamic identifiers in metric names.

---

# 76. Metric Tags

Metric tag names use lowercase snake case or lowercase compact names consistently.

Recommended examples:

```text
status

outcome

order_type

dependency

operation

exception
```

Tag values must have controlled cardinality.

Never use:

```text
order_id

customer_id

email

request_id

trace_id
```

as metric tags.

---

# 77. Log Events

Structured log event names should use lowercase snake case.

Examples:

```text
order_created

order_approval_failed

inventory_reservation_completed

external_call_failed

outbox_event_dispatched
```

Logs should also contain stable fields such as:

```text
operation

outcome

elapsedMs

traceId

correlationId
```

---

# 78. Correlation Identifiers

Use consistent field names across HTTP, logs, events and traces.

Recommended:

```text
correlationId

traceId

requestId

causationId
```

Definitions:

| Field | Meaning |
|---|---|
| `traceId` | Distributed trace identifier |
| `correlationId` | Business or request flow correlation |
| `requestId` | Identifier of one inbound request |
| `causationId` | Identifier of the message or event that caused another event |

Do not use these terms interchangeably.

---

# 79. Trace Attributes

Tracing attribute names should follow OpenTelemetry semantic conventions whenever available.

Custom attributes should use namespaced keys.

Examples:

```text
order.id

order.type

order.status

messaging.event_type

external.dependency
```

Avoid high-cardinality values unless necessary for trace diagnosis.

---

# 80. Docker Resources

Docker image names use lowercase kebab-case.

Examples:

```text
enterprise-order-platform

order-service

inventory-service
```

Tags should represent immutable versions.

Examples:

```text
1.0.0

1.0.0-rc.1

sha-a8f9c21
```

Avoid relying on `latest` in controlled deployments.

---

# 81. Kubernetes Resources

Kubernetes resource names use lowercase kebab-case.

Examples:

```text
order-service

order-service-config

order-service-secrets

order-service-api

order-service-worker
```

Names should identify:

- application
- component
- optional environment or region when required

---

# 82. Kubernetes Labels

Recommended labels:

```yaml
app.kubernetes.io/name: order-service
app.kubernetes.io/instance: order-service-prod
app.kubernetes.io/version: 1.0.0
app.kubernetes.io/component: api
app.kubernetes.io/part-of: enterprise-order-platform
app.kubernetes.io/managed-by: helm
```

Prefer Kubernetes recommended label conventions.

---

# 83. Helm Values

Helm values use lower camel case.

Examples:

```yaml
replicaCount: 3

imagePullPolicy: IfNotPresent

serviceAccount:
  create: true

resources:
  limits:
    memory: 1Gi
```

Follow the conventions of the chosen chart structure consistently.

---

# 84. Git Branches

Branch names use lowercase kebab-case after the branch category.

Examples:

```text
feature/order-approval

bugfix/payment-timeout

release/1.2.0

hotfix/order-duplication
```

Avoid:

```text
feature/OrderApproval

feature/order_approval

dev-renato
```

---

# 85. Commit Messages

Commits follow Conventional Commits.

Pattern:

```text
<type>(<scope>): <description>
```

Examples:

```text
feat(order): implement approval workflow

fix(inventory): prevent duplicate reservation

docs(standards): define naming conventions

test(payment): cover authorization timeout

refactor(outbox): simplify dispatcher orchestration
```

Descriptions should:

- use imperative mood
- begin with lowercase
- omit a final period
- remain concise

---

# 86. Git Tags

Release tags follow Semantic Versioning.

Examples:

```text
v1.0.0

v1.2.0

v2.0.0-rc.1
```

Tags are immutable.

---

# 87. Test Classes

Test class names use the production class or behavior followed by `Test`.

Examples:

```java
OrderTest

ApproveOrderServiceTest

JpaOrderRepositoryTest

OrderControllerTest
```

Integration tests may use:

```java
OrderCreationIntegrationTest
```

End-to-end tests may use:

```java
OrderLifecycleEndToEndTest
```

---

# 88. Test Methods

Test methods should begin with `test` and describe:

- behavior
- condition
- expected result

Examples:

```java
testApproveShouldChangeStatusWhenOrderIsPending()

testApproveShouldThrowExceptionWhenOrderDoesNotExist()

testCreateShouldPublishEventAfterPersistence()
```

Avoid:

```java
test1()

shouldWork()

approveOrderTest()
```

---

# 89. Test Constants

Shared deterministic test values should use uppercase snake case.

Examples:

```java
ORDER_ID

CUSTOMER_ID

ORDER_NUMBER

DEFAULT_CREATED_AT

APPROVED_STATUS
```

Avoid random values unless randomness is explicitly under test.

---

# 90. Test Fixtures

Fixture classes should describe the domain object they create.

Examples:

```java
OrderFixture

CustomerFixture

PaymentFixture

OrderRequestFixture
```

Fixture methods should describe the scenario.

Examples:

```java
validOrder()

pendingApprovalOrder()

cancelledOrder()

orderWithUnavailableInventory()
```

Avoid generic methods:

```java
create()

build()

mockData()
```

---

# 91. Architecture Test Names

Architecture tests should describe the enforced rule.

Examples:

```java
testDomainShouldNotDependOnSpring()

testControllersShouldNotAccessRepositoriesDirectly()

testInfrastructureShouldImplementApplicationPorts()

testPackagesShouldNotHaveCycles()
```

---

# 92. Documentation Files

Markdown filenames use lowercase kebab-case.

Examples:

```text
coding-standards.md

package-structure.md

naming-conventions.md

transactional-outbox.md
```

Avoid:

```text
CodingStandards.md

coding_standards.md
```

---

# 93. ADR Names

Architecture Decision Record files follow:

```text
ADR-<number>-<short-description>.md
```

Examples:

```text
ADR-001-use-clean-architecture.md

ADR-002-adopt-postgresql.md

ADR-003-use-transactional-outbox.md
```

Numbers are sequential and immutable.

---

# 94. Diagram Files

Diagram files should use lowercase kebab-case.

Examples:

```text
system-context.puml

order-creation-sequence.puml

deployment-view.drawio
```

Include the diagram type in the name when useful.

---

# 95. Naming Review Checklist

Before introducing a new name, verify:

- Does it use the ubiquitous language?
- Does it communicate business intent?
- Is it consistent with existing names?
- Is it searchable?
- Does it avoid unnecessary abbreviations?
- Does it avoid implementation leakage?
- Does it represent an action, fact or concept correctly?
- Will the meaning remain clear to a new engineer?

---

# 96. Rename Policy

Names should be corrected when they create:

- ambiguity
- architectural confusion
- inconsistent vocabulary
- incorrect business meaning
- implementation leakage

Renaming requires evaluation of:

- public API compatibility
- event compatibility
- database migration impact
- serialized contracts
- monitoring dashboards
- documentation
- consumers

Public contract renames may require versioning rather than direct replacement.

---

# 97. Anti-Patterns

The following naming patterns should be avoided:

```text
CommonUtils

BaseService

GenericManager

DataProcessor

ObjectHelper

OrderServiceImpl

Dto1

NewOrder

OldOrder

FinalOrderService

TempRepository
```

Temporary names tend to become permanent and must not enter the main branch.

---

# 98. Architecture Rules

Naming must:

- reflect business capabilities
- preserve domain language
- expose architectural responsibility
- distinguish commands from events
- distinguish ports from adapters
- avoid technology leakage in domain contracts
- use stable external contract names
- remain consistent across code and infrastructure

---

# 99. Decision Summary

The project adopts:

- English technical naming
- ubiquitous language alignment
- PascalCase for Java types
- lower camel case for Java members
- lowercase packages
- imperative names for commands
- past tense for events
- explicit port and adapter names
- plural REST resources
- lowercase snake case for database objects
- dot-separated event and metric names
- kebab-case for infrastructure resources
- uppercase snake case for error codes and environment variables
- deterministic and descriptive test naming
