# ADR-014: Adopt OpenTelemetry for Distributed Observability

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-014 |
| Title | Adopt OpenTelemetry for Distributed Observability |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Observability |
| Related Work Items | Platform Monitoring, Distributed Tracing and Operational Diagnostics |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform is composed of independently deployable services and infrastructure components.

The platform uses:

- Java 21
- Spring Boot
- PostgreSQL
- Redis
- Amazon SQS
- Transactional Outbox
- Saga orchestration
- synchronous HTTP integrations
- asynchronous integration events
- Kubernetes
- CI/CD pipelines

A single business operation may cross multiple technical boundaries.

Example:

```text
Client Request

↓

Orders API

↓

PostgreSQL Transaction

↓

Transactional Outbox

↓

SQS

↓

Inventory Consumer

↓

Inventory Database

↓

Payment Service

↓

External Payment Provider
```

Traditional application logs are insufficient to reconstruct the complete execution path.

Without distributed observability, engineering and operations teams may be unable to determine:

- where latency was introduced
- which service failed
- which SQS message/event caused a downstream effect
- whether an outbox event was published
- whether a saga is delayed
- which external dependency caused degradation
- whether failures are isolated or systemic
- whether retries are increasing load
- whether service-level objectives are being met

The platform requires a vendor-neutral observability standard that supports traces, metrics and logs across synchronous and asynchronous boundaries.

---

# 2. Problem Statement

The platform requires an observability architecture that:

- provides distributed tracing
- exposes application and infrastructure metrics
- correlates logs with traces
- propagates context across HTTP and SQS
- integrates with Java 21
- integrates with Spring Boot
- supports Kubernetes
- supports automatic and manual instrumentation
- remains vendor-neutral
- supports OTLP exporters
- supports multiple observability backends
- controls telemetry cost
- protects sensitive information
- supports service-level objectives
- supports incident investigation
- supports performance analysis
- supports asynchronous workflows
- supports Transactional Outbox correlation
- supports Saga correlation
- remains testable and maintainable

---

# 3. Decision Drivers

The primary decision drivers are:

1. vendor neutrality
2. distributed trace correlation
3. standardized instrumentation
4. operational visibility
5. HTTP and SQS context propagation
6. support for logs, metrics and traces
7. Spring Boot compatibility
8. Kubernetes compatibility
9. support for managed observability platforms
10. low application coupling
11. consistent semantic conventions
12. cost governance
13. security and privacy
14. testability
15. support for service-level objectives
16. incident-response efficiency
17. long-term maintainability

---

# 4. Constraints

The decision must consider:

- multiple services may participate in one business operation
- events may be processed long after the originating HTTP request
- SQS delivery is at least once
- consumer retries may create multiple processing attempts
- outbox dispatch is asynchronous
- saga workflows may last minutes or longer
- multiple application instances may run concurrently
- telemetry backends may change
- production traffic may be high
- telemetry volume must remain controlled
- personal and confidential data must not leak into telemetry
- logs must remain useful without requiring full payload capture
- correlation identifiers must remain stable
- infrastructure and application metrics must be distinguishable
- telemetry failures must not stop business processing

---

# 5. Considered Options

## 5.1 Option A: Vendor-Specific Instrumentation

Each service could use proprietary libraries from one observability vendor.

### Advantages

- deep integration with one vendor
- potentially faster initial setup
- vendor-specific features
- simplified support within one platform

### Disadvantages

- vendor lock-in
- proprietary APIs leak into application code
- difficult migration
- inconsistent instrumentation between tools
- reduced portability
- duplicated effort when changing backends

---

## 5.2 Option B: Logs Only

The platform could rely only on structured application logs.

### Advantages

- simple implementation
- broad operational familiarity
- low instrumentation complexity

### Disadvantages

- weak distributed correlation
- difficult latency analysis
- difficult asynchronous-flow analysis
- limited dependency visualization
- high search cost
- difficult root-cause analysis
- no native service-level telemetry model

---

## 5.3 Option C: Micrometer Metrics and Proprietary Tracing

Metrics could use Micrometer while traces use a vendor-specific agent or SDK.

### Advantages

- strong Spring integration
- mature metrics support
- incremental adoption

### Disadvantages

- fragmented observability model
- different context-propagation models
- backend coupling
- inconsistent metadata
- greater operational complexity

---

## 5.4 Option D: OpenTelemetry

OpenTelemetry provides standardized APIs, SDKs, semantic conventions, context propagation and OTLP export.

### Advantages

- vendor-neutral standard
- unified trace and metric model
- broad ecosystem
- automatic instrumentation
- manual instrumentation
- OTLP support
- standardized semantic conventions
- strong Java support
- Spring Boot integration
- HTTP and SQS propagation
- compatibility with multiple backends
- supports collectors and centralized processing

### Disadvantages

- instrumentation governance is required
- telemetry volume may become expensive
- semantic conventions evolve
- automatic instrumentation may create excessive spans
- sampling must be designed carefully
- additional collector infrastructure may be required

---

# 6. Decision

The AstraForge Supply Platform adopts OpenTelemetry as the standard distributed-observability framework.

OpenTelemetry will be used for:

- distributed traces
- context propagation
- application metrics
- infrastructure-related application metrics
- log correlation
- telemetry export through OTLP

The platform will use an OpenTelemetry Collector or approved managed equivalent as the primary telemetry gateway.

Application code must remain independent from a specific observability backend.

---

# 7. Rationale

OpenTelemetry provides a standard observability model across:

- HTTP requests
- SQS producers
- SQS consumers
- database access
- Redis access
- external-service calls
- scheduled tasks
- outbox dispatch
- saga workflows
- background processing

It allows telemetry to be exported to different backends without rewriting business code.

Possible backends include:

- Prometheus
- Grafana
- Tempo
- Jaeger
- Loki
- Elasticsearch
- managed cloud observability platforms
- commercial APM platforms

---

# 8. Observability Model

The platform adopts three complementary telemetry signals:

```text
Traces

Metrics

Logs
```

Each signal serves a different purpose.

---

# 9. Traces

Traces represent the execution path of one distributed operation.

They answer questions such as:

- which services participated
- where time was spent
- which dependency failed
- how retries affected latency
- which SQS message/event continued the operation
- where a saga stopped progressing

---

# 10. Metrics

Metrics represent aggregated system behavior over time.

They answer questions such as:

- request rate
- error rate
- latency distribution
- queue backlog/oldest-message age
- outbox backlog
- saga duration
- retry volume
- cache hit ratio
- database pool saturation

---

# 11. Logs

Logs provide detailed discrete diagnostic records.

They answer questions such as:

- what validation failed
- which transition was rejected
- why a retry occurred
- which configuration was invalid
- which external response category was received

Logs must correlate with traces where trace context exists.

---

# 12. OpenTelemetry Architectural Role

OpenTelemetry is an Infrastructure and cross-cutting concern.

Domain code must not depend directly on:

- OpenTelemetry SDK classes
- exporter classes
- backend-specific APIs
- collector configuration
- proprietary trace types

Domain behavior may expose business metadata through application abstractions, but instrumentation remains outside the Domain layer.

---

# 13. Reference Architecture

```text
Application Services

↓

OpenTelemetry Instrumentation

↓

OTLP Export

↓

OpenTelemetry Collector

├── Trace Backend
├── Metrics Backend
└── Log Backend
```

Example:

```text
Spring Boot Application

↓

OTLP/gRPC or OTLP/HTTP

↓

OpenTelemetry Collector

├── Prometheus-compatible metrics
├── Tempo or Jaeger traces
└── Loki or Elasticsearch logs
```

---

# 14. OpenTelemetry Collector

The platform should use the OpenTelemetry Collector as the standard telemetry gateway.

Responsibilities may include:

- receiving telemetry
- batching
- retry
- filtering
- attribute removal
- sampling
- enrichment
- routing
- exporting to one or more backends

Applications should not contain backend-specific routing logic.

---

# 15. Collector Deployment Models

Supported models include:

- agent per Kubernetes node
- sidecar
- centralized gateway
- hybrid agent and gateway
- managed collector service

The preferred initial model is a centralized or gateway-oriented collector deployment, with node-level collection added when required.

---

# 16. OTLP

OTLP is the standard telemetry export protocol.

Preferred protocols:

```text
OTLP over gRPC
```

or:

```text
OTLP over HTTP
```

The final choice depends on:

- network policy
- managed-platform support
- proxy compatibility
- operational requirements

Applications must not export directly to a proprietary backend when OTLP is available.

---

# 17. Automatic Instrumentation

Automatic instrumentation should cover standard technical operations where practical.

Examples:

- inbound HTTP requests
- outbound HTTP requests
- Spring MVC
- Spring WebFlux
- JDBC
- SQS producer
- SQS consumer
- Redis
- scheduled execution
- thread context propagation

Automatic instrumentation reduces manual code and improves consistency.

---

# 18. Manual Instrumentation

Manual instrumentation is required for important business operations that automatic instrumentation cannot represent adequately.

Examples include:

- order approval
- checkout processing
- inventory reservation
- payment authorization
- saga transition
- outbox dispatch cycle
- batch processing
- reconciliation
- compensation
- business validation

Manual spans must represent meaningful operations rather than internal implementation details.

---

# 19. Instrumentation Principle

Instrument business and operational boundaries.

Do not create spans for every private method.

Poor instrumentation:

```text
validateField

mapDto

buildResponse

callHelper

convertEnum
```

Preferred instrumentation:

```text
order.checkout

inventory.reserve

payment.authorize

saga.transition

outbox.publish

order.approve
```

---

# 20. Trace Structure

A trace contains one root span and zero or more child spans.

Example:

```text
POST /api/v1/orders

├── order.create
├── PostgreSQL INSERT
├── outbox.persist
└── response serialization
```

Asynchronous continuation:

```text
SQS publish

↓

SQS consume

├── inventory.reserve
├── PostgreSQL transaction
└── outbox.persist
```

---

# 21. Span Naming

Span names must be:

- stable
- low cardinality
- meaningful
- based on operation
- independent from identifiers

Good examples:

```text
HTTP POST /api/v1/orders

order.create

inventory.reserve

payment.authorize

saga.transition

outbox.dispatch
```

Poor examples:

```text
order.create.123456

payment.authorize.customer-987

GET /orders/4a7f...
```

---

# 22. Span Kinds

Span kinds should be used according to OpenTelemetry semantics:

| Span Kind | Usage |
|---|---|
| `SERVER` | Inbound request or message processing entry point |
| `CLIENT` | Outbound dependency call |
| `PRODUCER` | Message publication |
| `CONSUMER` | Message consumption |
| `INTERNAL` | Internal business operation |

---

# 23. Trace Identifiers

OpenTelemetry trace and span identifiers are infrastructure correlation identifiers.

They do not replace business identifiers such as:

- order ID
- saga ID
- event ID
- command ID
- correlation ID

Business identifiers may be added as controlled attributes and logs.

---

# 24. Correlation Identifier

The platform should maintain an explicit business correlation identifier where required.

A correlation ID may represent:

- originating request
- business workflow
- integration flow
- saga instance

It must remain stable across retries and message delivery where semantically appropriate.

---

# 25. Trace Context Propagation

The platform will use W3C Trace Context as the standard trace-propagation format.

Relevant headers include:

```text
traceparent

tracestate
```

Propagation must work across:

- HTTP
- SQS headers
- scheduled continuation where context is persisted explicitly
- background executors
- virtual threads
- asynchronous callbacks

---

# 26. Baggage

W3C Baggage may be used only for carefully controlled low-cardinality metadata.

Potential examples:

- tenant classification
- deployment region
- business channel
- segment

Baggage must not contain:

- access tokens
- passwords
- personal identifiers
- order IDs
- customer IDs
- full usernames
- sensitive business data
- large values

Baggage propagates across service boundaries and may increase telemetry cost.

---

# 27. HTTP Propagation

Inbound HTTP instrumentation must:

- extract trace context
- create a server span
- propagate context to downstream calls
- correlate application logs
- record HTTP route rather than raw path where possible

Outbound HTTP instrumentation must:

- create a client span
- inject trace context
- record target service
- record status and duration
- sanitize URLs

---

# 28. HTTP Route Cardinality

Metrics and spans must use normalized routes.

Preferred:

```text
/api/v1/orders/{orderId}
```

Avoid:

```text
/api/v1/orders/eead0f67-7977-4693-9312-cb3025cfe651
```

Raw identifiers create unbounded cardinality.

---

# 29. SQS Propagation

SQS producers must inject trace context into SQS headers.

SQS consumers must extract the context and create a consumer-processing span.

Recommended correlation metadata includes:

- trace context
- event ID
- event type
- event version
- saga ID where applicable
- correlation ID
- causation ID

Authentication credentials must never be propagated in SQS headers.

---

# 30. SQS Producer Spans

SQS publication spans should represent logical publication.

Relevant attributes may include:

- messaging system
- destination
- operation
- event type
- event version
- producer service
- result
- retry attempt where bounded

Do not include event payloads as span attributes.

---

# 31. SQS Consumer Spans

SQS consumer spans should include:

- destination
- consumer group
- operation
- event type
- processing result
- retry classification
- dead-letter result where applicable

High-cardinality values such as offset may be logged, but must not become metric labels.

---

# 32. Asynchronous Trace Relationships

Asynchronous processing may occur long after the producer span ends.

The consumer may:

- continue the same trace
- create a new trace linked to the producer span
- use span links for batch processing

The selected model must remain consistent.

For direct one-message-to-one-processing flows, continuing the propagated trace is acceptable.

For delayed, replayed or batch processing, span links may better represent causality.

---

# 33. Span Links

Span links should be used when one operation is causally related to one or more previous operations but is not a direct child.

Examples include:

- batch consumer processing multiple messages
- replay
- scheduled retry
- saga timeout processing
- outbox batch dispatch
- one command created from multiple events

---

# 34. Transactional Outbox Tracing

The outbox introduces a temporal boundary between the business transaction and SQS publication.

The platform must correlate:

```text
Business transaction

↓

Outbox record

↓

Outbox dispatcher

↓

SQS publication
```

The outbox record should persist sufficient correlation metadata, such as:

- event ID
- trace ID
- correlation ID
- causation ID
- aggregate ID
- event type

Sensitive baggage must not be persisted blindly.

---

# 35. Outbox Dispatch Traces

The dispatcher should create spans for:

- polling
- claimed batch
- event serialization
- SQS publication
- publication acknowledgement
- retry scheduling
- terminal failure

Avoid one excessively detailed trace for a very large batch.

Use child spans or span links where appropriate.

---

# 36. Saga Tracing

Saga tracing must correlate:

- saga creation
- saga transitions
- commands
- participant outcomes
- timeouts
- retries
- compensation
- terminal state

Saga ID should be available in logs and controlled trace attributes.

---

# 37. Long-Running Saga Traces

A saga may outlive normal trace-retention or span-duration expectations.

The platform must not keep one span open for the entire saga duration.

Preferred model:

```text
One trace per processing activation

+

Stable saga ID

+

Correlation ID

+

Span links
```

This preserves operational usefulness without creating extremely long-lived spans.

---

# 38. Scheduled Tasks

Scheduled tasks must create new root spans or linked spans.

Examples:

- outbox dispatcher
- timeout processor
- reconciliation job
- cache warm-up
- cleanup job

Span names should represent the scheduled business or operational function.

---

# 39. Virtual Threads

Context propagation must be validated when using virtual threads.

The instrumentation must preserve trace and logging context across:

- virtual-thread executors
- asynchronous tasks
- delegated executors
- callbacks
- structured concurrency where adopted

Thread-local assumptions must be tested.

---

# 40. Executor Propagation

Custom executors must propagate observability context.

Losing context can break:

- trace continuity
- log correlation
- baggage
- span parenting

Approved instrumented executors or explicit context wrapping should be used.

---

# 41. Database Tracing

Database instrumentation should record:

- database system
- operation
- normalized statement category
- duration
- error result
- connection-pool wait where supported

Full SQL parameter values must not be recorded by default.

---

# 42. SQL Sanitization

Telemetry must not expose:

- customer data
- payment data
- credentials
- personal identifiers
- raw dynamic SQL parameters
- access tokens

SQL statements should be normalized or sanitized.

---

# 43. Redis Tracing

Redis instrumentation may record:

- command type
- server
- duration
- error result
- cache operation

Redis keys must not be captured by default when they may contain identifiers or sensitive values.

---

# 44. External-Service Tracing

Outbound integrations should record:

- dependency name
- operation
- protocol
- duration
- status category
- timeout
- retry
- circuit-breaker state where useful

Do not record:

- access tokens
- authorization headers
- personal payloads
- full response bodies
- secret query parameters

---

# 45. Error Recording

Spans should record errors when an operation fails.

Error telemetry should include:

- exception type
- failure category
- result
- sanitized message where safe

Do not mark every business rejection as an infrastructure error.

---

# 46. Business Rejections

Business rejection may be represented as:

- normal span status with business result attribute
- controlled event
- structured log
- business metric

Examples:

- payment declined
- insufficient stock
- invalid state transition
- approval rejected

These outcomes may be expected business behavior rather than system failure.

---

# 47. Span Status

Span status should indicate technical execution outcome.

Use error status for:

- unhandled exception
- failed dependency call
- serialization failure
- database failure
- unexpected processing failure

Do not automatically mark:

- HTTP 404 business lookup
- expected 409 conflict
- validation rejection
- payment rejection

as technical trace errors without classification.

---

# 48. Exception Events

Unhandled exceptions may be recorded as span events.

Captured data must be sanitized.

Stack traces may be exported through approved telemetry pipelines, but sensitive arguments and payloads must not be attached.

---

# 49. Metrics Architecture

Metrics should be exported through OpenTelemetry or an approved integration compatible with the platform's observability model.

The platform may use Micrometer as the Spring-facing metrics facade when bridged consistently to OpenTelemetry.

The final metrics path must avoid duplicate collection.

---

# 50. RED Method

Services should expose RED metrics:

```text
Rate

Errors

Duration
```

Examples:

- HTTP request rate
- HTTP error rate
- HTTP latency
- SQS consumer rate
- SQS consumer failure rate
- SQS processing duration

---

# 51. USE Method

Infrastructure resources should expose USE metrics:

```text
Utilization

Saturation

Errors
```

Examples:

- database connection-pool utilization
- executor saturation
- CPU utilization
- memory pressure
- SQS producer buffer exhaustion
- Redis connection saturation

---

# 52. Domain Metrics

The platform should expose carefully selected business metrics.

Examples:

- orders created
- orders approved
- orders rejected
- checkout completed
- payment declined
- inventory reservation rejected
- saga completed
- saga compensated

Business metrics must have bounded label values.

---

# 53. Counter Metrics

Counters are appropriate for monotonically increasing events.

Examples:

```text
orders.created.total

outbox.published.total

sqs.consumer.errors.total

saga.completed.total
```

---

# 54. Histogram Metrics

Histograms should be used for distributions.

Examples:

```text
http.server.duration

order.checkout.duration

saga.duration

outbox.publish.duration

external.payment.duration
```

Histograms support percentiles and service-level objectives.

---

# 55. Gauge Metrics

Gauges represent current state.

Examples:

```text
outbox.pending

saga.in_progress

database.connections.active

executor.queue.size

cache.entries
```

Gauge collection must remain efficient.

---

# 56. Metric Naming

Metric names must be:

- stable
- descriptive
- consistent
- low cardinality
- independent from vendor naming

Do not encode identifiers in metric names.

---

# 57. Metric Labels

Appropriate labels may include:

- service
- operation
- result
- status category
- event type
- saga type
- bounded context
- dependency
- environment

Only bounded values are permitted.

---

# 58. Prohibited Metric Labels

Do not use:

- order ID
- customer ID
- event ID
- saga ID
- command ID
- email
- username
- URL with identifiers
- exception message
- SQL statement
- SQS message identifier / receive context

as metric labels.

---

# 59. Cardinality Governance

High-cardinality telemetry can cause:

- excessive memory usage
- backend instability
- high storage cost
- slow queries
- unusable dashboards

Every new metric label must be reviewed for bounded cardinality.

---

# 60. Logs and OpenTelemetry

The platform will use structured logs correlated with OpenTelemetry trace context.

Recommended fields include:

- timestamp
- level
- service
- environment
- trace ID
- span ID
- correlation ID
- event ID where relevant
- saga ID where relevant
- operation
- result
- error category

---

# 61. Structured Logging

Logs must use structured fields rather than manually formatted text where supported.

Preferred conceptual structure:

```json
{
  "timestamp": "2026-07-23T18:40:00Z",
  "level": "INFO",
  "service": "orders-service",
  "traceId": "4f57c9f8064a4d6a8bd90bb2999cda00",
  "spanId": "04af20f271923001",
  "correlationId": "3cc93ab8-57da-4f0d-a7e0-38ec314e58af",
  "operation": "order.approve",
  "result": "success",
  "durationMs": 142
}
```

---

# 62. Log Levels

Recommended usage:

| Level | Usage |
|---|---|
| `TRACE` | Temporary deep diagnostics in controlled environments |
| `DEBUG` | Detailed development diagnostics |
| `INFO` | Important lifecycle and business-operation summaries |
| `WARN` | Recoverable abnormal condition |
| `ERROR` | Failed operation requiring investigation |

Expected business rejection must not automatically be logged as `ERROR`.

---

# 63. Duplicate Logging

The same exception must not be logged repeatedly at multiple layers.

Preferred behavior:

- lower layer adds context or rethrows
- boundary layer logs once
- trace records failure
- metric records failure category

This avoids log amplification.

---

# 64. Payload Logging

Full request, response and event payload logging is prohibited by default.

Payload logging may expose:

- personal data
- credentials
- financial data
- business-confidential data
- large volumes
- contract internals

Controlled redacted logging may be introduced for specific diagnostics.

---

# 65. Correlation Across Signals

The observability backend should allow navigation between:

```text
Metric

↓

Exemplar or time window

↓

Trace

↓

Correlated logs
```

Trace IDs should be available in logs.

Metrics may use exemplars where the backend supports them.

---

# 66. Sampling

Trace sampling is required to control volume and cost.

Sampling strategy must consider:

- traffic volume
- incident needs
- error visibility
- latency analysis
- critical business flows
- backend cost
- regulatory constraints

---

# 67. Head Sampling

Head sampling decides at trace start whether to record the trace.

### Advantages

- low processing overhead
- simple
- predictable volume

### Disadvantages

- cannot know final outcome
- may miss rare failures
- may miss slow traces

---

# 68. Tail Sampling

Tail sampling decides after observing completed trace data.

### Advantages

- retain errors
- retain slow traces
- retain selected business flows
- better diagnostic value

### Disadvantages

- collector memory cost
- more complex infrastructure
- delayed export
- requires centralized trace processing

---

# 69. Preferred Sampling Strategy

The preferred production strategy is:

```text
Baseline head sampling

+

Collector-side tail sampling where operationally justified
```

Tail sampling should prioritize:

- traces with errors
- traces above latency threshold
- selected critical workflows
- compensation flows
- unusual retry behavior
- low-volume important operations

---

# 70. Sampling Consistency

Sampling decisions should remain consistent across a distributed trace.

Downstream services should honor propagated sampling decisions.

A service must not independently resample every child operation without explicit design.

---

# 71. Critical Trace Retention

Certain flows may require higher sampling probability.

Examples:

- order checkout
- payment authorization
- saga compensation
- manual recovery
- failed outbox publication
- dead-letter processing

High-value tracing must still avoid sensitive payload capture.

---

# 72. Metrics Are Not Sampled Like Traces

Metrics should remain aggregated and reliable even when trace sampling is low.

Operational alerting must not depend only on sampled traces.

Logs and metrics remain necessary.

---

# 73. Telemetry Volume

Telemetry volume must be budgeted.

Primary cost drivers include:

- spans per request
- attributes per span
- metric cardinality
- log volume
- payload size
- retention period
- sampling rate
- duplicate instrumentation

---

# 74. Cost Governance

Each environment should define:

- trace sampling rate
- log-level policy
- metric-retention policy
- trace-retention policy
- maximum attribute count
- maximum attribute length
- collector batch configuration
- export limits

---

# 75. Telemetry Attribute Limits

The platform should configure limits for:

- number of attributes
- attribute value length
- number of events per span
- number of links per span

Unbounded attribute growth is prohibited.

---

# 76. Resource Attributes

Every service must export standard resource attributes.

Examples include:

```text
service.name

service.namespace

service.version

deployment.environment.name

cloud.region

k8s.namespace.name

k8s.pod.name
```

Resource attributes must follow approved semantic conventions.

---

# 77. Service Name

`service.name` must be stable across replicas.

Good example:

```text
orders-service
```

Poor examples:

```text
orders-service-pod-abc123

orders-service-20260723-1840
```

Replica identity belongs in Kubernetes resource attributes.

---

# 78. Service Version

`service.version` should identify the deployed application version.

Examples:

- semantic version
- build version
- release tag
- short commit identifier

The value must support incident comparison between deployments.

---

# 79. Environment Name

Telemetry must identify the environment.

Examples:

```text
local

test

development

staging

production
```

Environment names must be standardized.

---

# 80. Semantic Conventions

The platform should follow OpenTelemetry semantic conventions for:

- HTTP
- messaging
- database
- RPC
- exceptions
- resources
- Kubernetes

Custom attributes should use a controlled namespace.

Example:

```text
enterprise.order.id

enterprise.saga.id

enterprise.event.type
```

Custom attributes must remain documented.

---

# 81. Custom Attribute Governance

A custom telemetry attribute must define:

- name
- description
- type
- cardinality
- sensitivity
- applicable spans
- retention implications

Unreviewed custom attributes should not proliferate.

---

# 82. Sensitive Attributes

The following must not be exported by default:

- passwords
- tokens
- authorization headers
- cookies
- private keys
- payment credentials
- personal documents
- full customer records
- raw request bodies
- raw response bodies
- database connection passwords

---

# 83. Attribute Redaction

The collector or application instrumentation should redact or remove prohibited attributes.

Examples:

```text
http.request.header.authorization

http.request.header.cookie

db.connection_string

url.query
```

Collector-side filtering provides an additional defense but does not replace safe application instrumentation.

---

# 84. URL Sanitization

URLs must not expose:

- access tokens
- secret query parameters
- personal identifiers
- dynamic resource identifiers where avoidable

Use normalized routes and sanitized query information.

---

# 85. Header Capture

HTTP header capture is disabled by default.

Only allowlisted headers may be captured.

Potential allowlisted headers include:

- correlation ID
- safe content type
- approved business channel header

Authorization and cookie headers are prohibited.

---

# 86. Telemetry Security

Telemetry pipelines must use:

- encrypted transport
- authenticated exporters
- protected collectors
- least-privilege backend access
- secret rotation
- network restrictions
- tenant or environment isolation where required

---

# 87. Telemetry Availability

Telemetry failure must not stop business processing.

Applications should:

- use bounded export queues
- use non-blocking export where possible
- drop telemetry under sustained pressure according to policy
- expose exporter failure metrics
- avoid unbounded memory accumulation

---

# 88. Collector Failure

If the collector is unavailable:

- business requests continue
- exporters retry within bounded limits
- telemetry may be dropped
- application memory remains protected
- alerts should detect collector unavailability

---

# 89. Backpressure

Telemetry export must not create application backpressure that materially affects business operations.

Batch processors should use bounded queues.

When limits are exceeded, dropping telemetry is preferable to exhausting application resources.

---

# 90. Observability SLOs

The observability platform should define its own service objectives.

Examples:

- telemetry ingestion availability
- maximum export delay
- maximum trace-search delay
- metric freshness
- log-search freshness
- acceptable telemetry drop rate

---

# 91. Application SLOs

Application SLOs should be derived from user-facing behavior.

Examples:

```text
99.9% successful order API availability

P95 checkout latency below 1 second

P99 order saga completion below 2 minutes
```

SLOs must not be defined only from infrastructure health.

---

# 92. Service-Level Indicators

Possible SLIs include:

- successful request ratio
- request latency
- saga completion ratio
- saga completion duration
- event-processing delay
- outbox publication delay
- consumer-processing delay
- dependency success ratio

---

# 93. Error Budget

Services with formal SLOs should calculate error budgets.

Error budgets support decisions regarding:

- release velocity
- reliability work
- incident escalation
- technical debt
- feature rollout
- capacity investment

---

# 94. Availability Measurement

Availability should measure meaningful service outcomes.

For example, a technically successful HTTP response that returns an unusable business result may not represent successful availability.

SLI semantics must reflect user value.

---

# 95. Latency Measurement

Latency should be measured at appropriate boundaries.

Examples:

- HTTP server duration
- checkout business duration
- outbox publication delay
- SQS message/event age
- saga completion duration
- external dependency latency

---

# 96. Event Processing Delay

SQS consumer observability should distinguish:

- broker lag
- event age
- processing duration
- retry delay

Low offset lag does not always mean low business delay.

---

# 97. Outbox Metrics

Required outbox metrics include:

```text
outbox.pending

outbox.oldest.age

outbox.published.total

outbox.failed.total

outbox.retry.total

outbox.publish.duration
```

---

# 98. Saga Metrics

Required saga metrics include:

```text
saga.started.total

saga.completed.total

saga.failed.total

saga.compensated.total

saga.timeout.total

saga.in.progress

saga.duration
```

---

# 99. SQS Metrics

Relevant SQS application metrics include:

- producer send rate
- producer error rate
- producer retry rate
- record size
- consumer processing rate
- consumer processing errors
- queue backlog/oldest-message age
- consumer rebalance count
- dead-letter rate

---

# 100. Redis Metrics

Relevant Redis metrics include:

- cache hit
- cache miss
- command latency
- connection failures
- timeout
- fallback activation
- eviction where available

---

# 101. Database Metrics

Relevant database application metrics include:

- query duration
- connection-pool usage
- connection-acquisition time
- transaction duration
- rollback rate
- deadlock count
- lock timeout
- optimistic-lock conflict rate

---

# 102. Resilience Metrics

Relevant resilience metrics include:

- circuit-breaker state
- circuit-breaker failure rate
- circuit-breaker rejection count
- retry count
- timeout count
- bulkhead saturation
- fallback count

---

# 103. Dashboard Strategy

Dashboards should be audience-specific.

Recommended dashboard classes:

- executive service-health overview
- service operational dashboard
- SQS and outbox dashboard
- saga workflow dashboard
- dependency dashboard
- infrastructure saturation dashboard
- release comparison dashboard

---

# 104. Service Dashboard

A service dashboard should include:

- request rate
- error rate
- latency percentiles
- top dependencies
- database pool
- SQS consumer state
- outbox backlog
- JVM health
- recent deployment version

---

# 105. Saga Dashboard

A saga dashboard should include:

- started
- completed
- failed
- compensating
- manual review
- timeout
- duration percentiles
- oldest active saga
- failures by step
- compensation failures

---

# 106. Alerts

Alerts should be based on actionable conditions.

Examples:

- error-budget burn
- sustained high error rate
- severe latency increase
- oldest outbox event beyond threshold
- queue backlog/oldest-message age beyond threshold
- saga timeout increase
- compensation failure
- collector export failure
- database pool saturation
- dead-letter growth

---

# 107. Alert Quality

Alerts must be:

- actionable
- owned
- severity-classified
- linked to a runbook
- resistant to transient noise
- based on meaningful thresholds

Avoid alerts for every individual exception.

---

# 108. Burn-Rate Alerts

Services with SLOs should prefer multi-window burn-rate alerts.

This provides better detection of:

- rapid severe incidents
- slower sustained degradation

than static error-rate thresholds alone.

---

# 109. Runbooks

Every critical alert should link to a runbook.

A runbook should include:

- meaning of the alert
- likely causes
- dashboards
- diagnostic queries
- immediate actions
- rollback criteria
- escalation contacts
- recovery validation

---

# 110. Trace Search

Operational teams should be able to search traces using controlled attributes such as:

- service name
- operation
- error status
- event type
- saga type
- correlation ID
- selected business identifier where allowed

Searchable identifiers must respect privacy and cost constraints.

---

# 111. Log Search

Logs should support searches by:

- trace ID
- correlation ID
- saga ID
- event ID
- command ID
- service
- operation
- error category

Identifiers should appear as structured fields.

---

# 112. Telemetry Retention

Retention must differ by signal and environment.

Consider:

- operational need
- compliance
- cost
- incident-reconstruction window
- traffic volume
- sensitivity

Production retention may differ between:

- metrics
- traces
- logs

---

# 113. Non-Production Telemetry

Non-production environments should use reduced retention and volume.

However, they must preserve enough observability to validate:

- propagation
- instrumentation
- dashboards
- alerts
- deployment changes

---

# 114. Local Development

Local development may use:

- console exporter
- local OpenTelemetry Collector
- Jaeger
- Grafana Tempo
- Prometheus
- Docker Compose

Local telemetry must remain optional and simple to start.

---

# 115. Test Environment

Automated tests should not export telemetry to production backends.

Tests may use:

- in-memory exporters
- test collectors
- disabled export
- local Testcontainers infrastructure

---

# 116. Unit Testing Instrumentation

Manual instrumentation should be testable without requiring a remote collector.

Tests may verify:

- span name
- span attributes
- error recording
- context propagation
- no sensitive attributes
- correct parent-child relationship

---

# 117. Integration Testing

Integration tests should validate context propagation across:

- inbound HTTP to outbound HTTP
- SQS producer to SQS consumer
- outbox record to dispatcher publication
- executor boundaries
- virtual threads
- scheduled tasks

---

# 118. Trace Propagation Tests

A propagation test should verify that:

```text
Inbound trace ID

↓

Application processing

↓

Outbound dependency span

↓

Same distributed trace
```

For SQS:

```text
Producer trace context

↓

SQS headers

↓

Consumer extraction

↓

Expected trace or span link
```

---

# 119. Log Correlation Tests

Tests should verify that logs generated inside an active span include:

- trace ID
- span ID
- correlation ID where applicable

Logs outside a trace must remain valid.

---

# 120. Cardinality Tests

Automated or review-based checks should prevent dynamic identifiers from becoming:

- metric labels
- span names
- resource attributes

---

# 121. Security Tests

Tests should verify that telemetry does not export:

- authorization headers
- cookies
- passwords
- secret query parameters
- raw sensitive payloads
- database credentials

---

# 122. Architecture Tests

Architecture tests should enforce:

- Domain does not depend on OpenTelemetry
- business services do not depend on exporters
- backend-specific telemetry APIs remain outside core layers
- controllers do not implement telemetry infrastructure directly
- custom instrumentation uses approved abstractions
- sensitive DTOs are not automatically attached as attributes

---

# 123. Spring Boot Integration

The platform may use:

- OpenTelemetry Java agent
- OpenTelemetry Spring Boot starter
- Micrometer Observation integration
- OpenTelemetry SDK
- approved bridge components

The selected implementation must avoid duplicate spans and duplicate metrics.

---

# 124. Java Agent

The Java agent is preferred for broad automatic instrumentation when compatible with deployment and security requirements.

Advantages:

- low code intrusion
- consistent library instrumentation
- easier upgrades
- broad coverage

Limitations:

- configuration complexity
- potential duplicate instrumentation
- less explicit control
- startup overhead
- agent compatibility requirements

---

# 125. Spring Boot Starter

A Spring Boot starter may be used where programmatic configuration or application-level control is required.

The team must document whether the platform standard uses:

- agent only
- starter only
- coordinated agent and starter
- Micrometer Observation bridge

Mixed approaches must not create duplicate telemetry.

---

# 126. Manual Tracer Usage

Direct tracer usage should be isolated in reusable instrumentation components.

Example abstraction:

```java
public interface BusinessObservation {

    <T> T observe(
            String operation,
            Supplier<T> action
    );
}
```

Infrastructure implementations may use OpenTelemetry or Micrometer Observation.

Domain code remains independent.

---

# 127. Annotation-Based Instrumentation

Annotations may be used for stable business operations.

They should not be applied excessively.

Instrumentation annotations must not obscure:

- transaction boundaries
- error handling
- business behavior
- performance implications

---

# 128. Instrumentation Ownership

Every service team owns:

- meaningful business spans
- service metrics
- log quality
- dashboard accuracy
- alert quality
- sensitive-data review
- telemetry cost

The platform team owns:

- collector architecture
- semantic standards
- backend integration
- common libraries
- global dashboards
- shared alerts
- governance

---

# 129. Instrumentation Review

Code review should verify:

- stable span names
- bounded attributes
- no sensitive values
- correct error classification
- correct context propagation
- no duplicate metrics
- no full payload capture
- meaningful business value
- acceptable volume

---

# 130. Change Management

Observability changes are production changes.

They can affect:

- performance
- cost
- data exposure
- dashboards
- alerts
- incident response

Significant instrumentation changes require review and validation.

---

# 131. Deployment Correlation

Telemetry must identify deployment version.

Dashboards should allow comparison:

```text
Before deployment

versus

After deployment
```

This supports regression detection.

---

# 132. Release Markers

The observability platform should receive release markers or deployment annotations where supported.

Release markers help correlate:

- error increase
- latency regression
- resource saturation
- queue backlog/oldest-message age
- saga failures

with a deployment.

---

# 133. Feature Flags

Where feature flags are adopted, low-cardinality flag state may be included in controlled telemetry.

Do not attach high-cardinality user-specific flag evaluations to every span.

---

# 134. Dependency Map

Trace data should support generation of a service-dependency map.

The map should show:

- HTTP dependencies
- SQS flows
- database dependencies
- Redis dependencies
- external providers

The map is diagnostic and does not replace the architecture documentation.

---

# 135. Observability During Incidents

During incidents, telemetry should support:

- identifying affected operations
- identifying first failing dependency
- comparing healthy and unhealthy instances
- locating slow traces
- correlating errors with deployments
- measuring backlog
- validating recovery

---

# 136. Temporary Diagnostic Increase

Temporary increases in log level or sampling may be allowed through controlled configuration.

They must define:

- scope
- duration
- owner
- rollback time
- cost impact
- security review

Temporary diagnostic settings must not remain indefinitely.

---

# 137. Data Residency

Telemetry may contain operational metadata subject to residency requirements.

Backend location and cross-region export must comply with organizational policy.

---

# 138. Multi-Tenant Observability

If the platform becomes multi-tenant, telemetry must prevent unauthorized tenant data exposure.

Tenant identifiers must be reviewed for:

- cardinality
- sensitivity
- access control
- retention
- cross-tenant visibility

---

# 139. Audit Versus Observability

Observability data is not the authoritative audit record.

Audit requirements may require:

- immutable records
- stronger retention
- legal evidence
- restricted mutation
- explicit business actors

Telemetry supports diagnostics and operations but does not replace the audit subsystem.

---

# 140. Event History Versus Traces

SQS message/event retention and saga transition history are not replaced by traces.

Traces may be sampled or expire earlier.

Business recovery must rely on durable business records rather than telemetry.

---

# 141. Performance Overhead

Instrumentation overhead must be measured.

Relevant costs include:

- CPU
- memory
- network
- serialization
- exporter queues
- span creation
- context propagation
- log formatting

---

# 142. Performance Budget

The platform should define an acceptable observability overhead budget.

Example categories:

- request latency overhead
- CPU overhead
- memory overhead
- network volume

Exact targets should be established through benchmarks.

---

# 143. Load Testing

Load testing must measure:

- application performance with telemetry enabled
- collector throughput
- export queue behavior
- sampling effectiveness
- backend ingestion
- metric cardinality
- log volume
- failure behavior when collector is unavailable

---

# 144. Collector Scaling

Collector capacity planning must consider:

- spans per second
- metrics per second
- logs per second
- batch size
- tail-sampling memory
- exporter latency
- backend limits
- high-availability requirements

---

# 145. Collector High Availability

Production collectors should avoid a single point of failure.

Possible strategies include:

- multiple replicas
- load balancing
- gateway pools
- queue persistence where supported
- managed collectors

---

# 146. Collector Configuration as Code

Collector configuration must be version controlled.

Configuration should define:

- receivers
- processors
- filters
- resource enrichment
- sampling
- exporters
- retry
- batching
- memory limits
- security

---

# 147. Collector Memory Protection

Collector pipelines must use memory protection mechanisms where supported.

Examples include:

- memory limiter
- bounded queues
- batching
- telemetry dropping under pressure

Collector overload must not cascade into application failure.

---

# 148. Export Retry

Collector and application exporter retries must be bounded.

Uncontrolled retry may amplify outages.

Retry configuration should include:

- maximum elapsed time
- backoff
- queue size
- drop policy
- alerting

---

# 149. Multiple Backends

The collector may route signals to multiple destinations.

Examples:

```text
Metrics → Prometheus-compatible backend

Traces → Tempo

Logs → Loki
```

or:

```text
All signals → Managed observability platform
```

Routing decisions remain outside business applications.

---

# 150. Backend Migration

A backend migration should require only collector and platform-level changes where possible.

Application instrumentation should remain unchanged.

This is a primary benefit of the OpenTelemetry decision.

---

# 151. Governance Documents

The following documents should remain aligned:

- observability architecture
- logging standards
- resilience standards
- messaging guidelines
- security guidelines
- engineering playbook
- definition of done
- incident-response runbooks

---

# 152. Anti-Patterns

The following are prohibited:

- vendor-specific tracing code inside Domain
- exporting telemetry directly to proprietary backends without architectural approval
- logging full payloads by default
- recording authorization headers
- recording cookies
- using identifiers in span names
- using unbounded metric labels
- opening one span for an entire long-running saga
- creating spans for every private method
- treating telemetry as an audit system
- relying only on traces for alerting
- relying only on logs for distributed correlation
- enabling duplicate automatic and manual instrumentation
- using raw URLs with identifiers as metric labels
- attaching SQL parameters to spans
- ignoring collector failures
- allowing unbounded exporter queues
- blocking business processing on telemetry export
- using trace IDs as business identifiers
- using sampled trace counts as authoritative business totals
- storing sensitive baggage
- leaving temporary debug logging enabled indefinitely
- creating alerts without ownership
- collecting telemetry without retention and cost policies

---

# 153. Positive Consequences

The decision provides:

- vendor-neutral observability
- standardized distributed tracing
- consistent context propagation
- HTTP and SQS correlation
- saga and outbox visibility
- improved incident diagnosis
- better latency analysis
- backend portability
- standardized semantic conventions
- unified telemetry export
- better service-level monitoring
- improved release regression detection
- stronger operational dashboards
- reduced dependence on proprietary agents
- support for automatic and manual instrumentation

---

# 154. Negative Consequences

The decision introduces:

- collector infrastructure
- instrumentation governance
- telemetry cost
- sampling complexity
- semantic-convention management
- performance overhead
- security review requirements
- dashboard maintenance
- alert maintenance
- backend capacity planning
- possible duplicate instrumentation
- context-propagation testing
- operational learning curve

These costs are accepted because distributed observability is essential for operating a distributed platform safely.

---

# 155. Neutral Consequences

The decision also means:

- not every trace will necessarily be retained
- metrics remain the primary alerting signal
- logs remain necessary for detailed diagnosis
- telemetry may be dropped under sustained export failure
- long-running sagas require correlation across multiple traces
- business records remain authoritative
- observability conventions become part of engineering governance
- telemetry quality becomes a shared engineering responsibility

---

# 156. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Sensitive data leaks into telemetry | High | Medium | Attribute allowlists, redaction and security tests |
| Metric cardinality becomes excessive | High | Medium | Label governance and automated review |
| Trace volume creates excessive cost | High | High | Sampling and span-volume controls |
| Automatic instrumentation creates duplicate spans | Medium | Medium | Standardize agent and starter configuration |
| Context is lost across asynchronous execution | High | Medium | Propagation tests and instrumented executors |
| Collector outage affects applications | High | Low | Bounded queues and non-blocking export |
| Telemetry overhead degrades performance | Medium | Medium | Benchmarking and performance budget |
| Long-running sagas create unusable traces | Medium | High | Multiple traces with stable saga correlation |
| Vendor-specific features create lock-in | Medium | Medium | Keep application export OTLP-based |
| Logs and traces cannot be correlated | High | Low | Inject trace and span IDs into structured logs |
| Sampling hides rare errors | High | Medium | Tail sampling and reliable error metrics |
| Alert noise overwhelms operations | Medium | Medium | Actionable alert governance |
| Telemetry is treated as audit evidence | High | Low | Maintain separate audit architecture |
| Backend retention is insufficient | Medium | Medium | Define retention requirements per signal |
| Baggage propagates sensitive data | High | Low | Strict baggage allowlist |
| Collector memory becomes saturated | High | Medium | Memory limiter, batching and scaling |
| New attributes break dashboards | Medium | Medium | Semantic convention and change review |
| Production and non-production telemetry mix | High | Low | Environment isolation and resource attributes |
| Outbox and SQS traces lose original context | High | Medium | Persist approved correlation metadata |
| High-volume logs increase cost | Medium | High | Structured logging and level policies |

---

# 157. Implementation Guidance

The following rules are mandatory:

1. OpenTelemetry is the standard observability framework.
2. OTLP is the standard export protocol.
3. Applications must remain backend-independent.
4. OpenTelemetry Collector or an approved equivalent must mediate production export.
5. W3C Trace Context is the standard propagation format.
6. HTTP and SQS propagation must be supported.
7. Trace and span IDs must appear in structured logs.
8. Domain code must not depend on OpenTelemetry.
9. Automatic instrumentation should cover standard technical operations.
10. Manual spans must represent meaningful business operations.
11. Span names must remain stable and low cardinality.
12. Metric labels must remain bounded.
13. Sensitive data must not be exported.
14. Authorization headers, cookies and tokens must never be captured.
15. Full payload logging is prohibited by default.
16. Telemetry export must not block business processing.
17. Export queues and retries must remain bounded.
18. Sampling must be defined for production.
19. Metrics must remain reliable independently of trace sampling.
20. Long-running sagas must use stable correlation across multiple traces.
21. Outbox records must preserve approved correlation metadata.
22. SQS messages must propagate trace context through headers.
23. Custom attributes require governance.
24. Collector configuration must be version controlled.
25. Dashboards and alerts must have clear ownership.
26. Critical alerts must link to runbooks.
27. Telemetry retention must be defined.
28. Performance overhead must be measured.
29. Security and propagation tests are mandatory.
30. Observability data must not replace durable business audit records.

---

# 158. Validation

The decision will be validated through:

- HTTP propagation tests
- SQS propagation tests
- outbox correlation tests
- saga correlation tests
- virtual-thread context tests
- structured-log correlation tests
- metric-cardinality review
- security tests
- telemetry redaction tests
- collector failure tests
- sampling validation
- load testing
- exporter backpressure testing
- dashboard review
- alert review
- runbook exercises
- deployment comparison
- production-readiness review

---

# 159. Success Criteria

The decision is successful when:

- one distributed operation can be traced across services
- HTTP and SQS context propagation works reliably
- outbox publication can be correlated with the originating transaction
- saga transitions can be investigated across multiple processing activations
- logs contain trace and span correlation
- service metrics expose rate, errors and duration
- infrastructure metrics expose utilization, saturation and errors
- sensitive information is absent from telemetry
- metric cardinality remains controlled
- collector failures do not stop business operations
- telemetry overhead remains within the defined budget
- dashboards support incident diagnosis
- alerts are actionable
- backend migration does not require business-code changes
- OpenTelemetry-specific code remains outside the Domain layer
- service-level objectives can be measured objectively

---

# 160. Alternatives Rejected

## 160.1 Proprietary Instrumentation as the Platform Standard

Rejected because it would introduce vendor lock-in and backend-specific code.

Proprietary backends may still receive telemetry through OTLP or the OpenTelemetry Collector.

---

## 160.2 Logs-Only Observability

Rejected because logs alone do not provide reliable distributed trace correlation or latency decomposition.

---

## 160.3 Metrics-Only Observability

Rejected because metrics identify symptoms but often lack request-level diagnostic context.

---

## 160.4 One Long Trace for an Entire Saga

Rejected because long-running workflows may exceed practical trace-duration and retention expectations.

Stable saga correlation across multiple traces is preferred.

---

## 160.5 Direct Application Export to Multiple Backends

Rejected because it increases application complexity, backend coupling and duplicated export configuration.

---

# 161. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-010: Use Redis for Distributed Caching
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives

---

# 162. References

- OpenTelemetry Specification
- OpenTelemetry Java Documentation
- OpenTelemetry Semantic Conventions
- OpenTelemetry Protocol Specification
- OpenTelemetry Collector Documentation
- W3C Trace Context
- W3C Baggage
- Spring Boot Observability Documentation
- Micrometer Observation Documentation
- Prometheus Documentation
- Grafana Documentation
- Jaeger Documentation
- Grafana Tempo Documentation
- AstraForge Supply Platform Observability Guide
- AstraForge Supply Platform Logging Standards
- AstraForge Supply Platform Messaging Architecture
- AstraForge Supply Platform Resilience Guide
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows

---

# 163. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved | Initial distributed observability architecture baseline |

---

# 164. Decision Summary

The AstraForge Supply Platform adopts OpenTelemetry as its standard framework for distributed observability.

The platform will use:

```text
OpenTelemetry instrumentation

+

W3C Trace Context

+

OTLP

+

OpenTelemetry Collector

+

Backend-independent telemetry export
```

The observability model combines:

```text
Traces

Metrics

Structured logs
```

The platform requires correlation across:

```text
HTTP

SQS

Transactional Outbox

Saga workflows

Scheduled processing

External dependencies
```

OpenTelemetry remains a cross-cutting Infrastructure concern.

The Domain layer must remain independent from:

```text
Tracing SDKs

Exporters

Collectors

Backend-specific APIs
```

Telemetry must remain:

```text
Secure

Low cardinality

Cost controlled

Operationally actionable

Non-blocking

Vendor neutral
```

This decision establishes a consistent and portable observability foundation for diagnosing, measuring and operating the AstraForge Supply Platform across distributed synchronous and asynchronous workflows.
