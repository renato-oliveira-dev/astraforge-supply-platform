# ADR-019: Adopt Structured Logging

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-019 |
| Title | Adopt Structured Logging |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Observability, Operations and Security |
| Related Work Items | Centralized Logging, Incident Diagnostics and Telemetry Governance |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The AstraForge Supply Platform is a distributed system composed of multiple independently deployable services.

The platform includes:

- synchronous HTTP APIs
- Amazon SQS producers and consumers
- Transactional Outbox dispatchers
- Saga workflows
- scheduled jobs
- PostgreSQL
- Redis
- external service integrations
- Kubernetes workloads
- horizontal application scaling
- OpenTelemetry instrumentation

A single business operation may generate diagnostic information across multiple:

- services
- pods
- threads
- virtual threads
- FIFO MessageGroupIds
- scheduled executions
- retries
- external dependencies

Traditional plain-text logs are difficult to query, correlate and process reliably at scale.

Example of an unstructured log:

```text
Order approval failed for order 924 because workflow service returned error after 3 retries
```

Although readable by a person, this message does not provide stable machine-readable fields for:

- order operation
- dependency
- retry count
- trace ID
- span ID
- correlation ID
- result
- duration
- error category
- application version
- environment

The platform requires a consistent logging standard that supports both human diagnosis and automated analysis.

---

# 2. Problem Statement

The platform requires a logging architecture that:

- emits machine-readable logs
- remains understandable by engineers
- correlates logs across distributed operations
- integrates with OpenTelemetry
- propagates trace and business correlation identifiers
- supports HTTP and SQS workloads
- works with Java 21 and Spring Boot
- works with virtual threads and asynchronous execution
- protects personal and confidential data
- supports Kubernetes logging
- supports centralized aggregation
- controls storage and ingestion cost
- supports production incident investigation
- supports security monitoring
- avoids excessive logging
- remains independent from one logging backend
- distinguishes operational logging from business audit records
- remains testable and governable

---

# 3. Decision Drivers

The primary decision drivers are:

1. distributed correlation
2. machine-readable output
3. incident-response efficiency
4. security and privacy
5. operational consistency
6. OpenTelemetry integration
7. Kubernetes compatibility
8. centralized aggregation
9. controlled telemetry cost
10. low-cardinality search fields
11. log quality
12. backend portability
13. performance
14. testability
15. audit separation
16. compliance with data-protection requirements
17. long-term maintainability

---

# 4. Constraints

The decision must consider:

- services run in multiple Kubernetes pods
- pod instances are ephemeral
- application logs are collected from standard output
- trace sampling may omit some traces
- log volume can be significantly larger than metric or trace volume
- SQS processing may continue long after the original HTTP request
- message delivery is at least once
- retries may generate repeated diagnostic information
- asynchronous execution may lose thread-local context
- personal and confidential data must not be exposed
- stack traces can be large
- logging failures must not stop business processing
- log backends may change
- production and non-production retention policies differ
- audit records require different guarantees from operational logs
- user-provided values are untrusted
- newline injection can corrupt plain log representations
- excessive debug logging can affect performance and cost

---

# 5. Considered Options

## 5.1 Option A: Plain-Text Logging

Applications could continue producing human-formatted messages.

Example:

```text
2026-07-23 10:00:00 ERROR Order update failed for 123
```

### Advantages

- simple
- familiar
- easy to read locally
- minimal initial configuration

### Disadvantages

- difficult automated parsing
- inconsistent field extraction
- fragile search queries
- weak correlation
- difficult schema governance
- difficult aggregation across services
- high dependency on message wording

---

## 5.2 Option B: Backend-Specific Logging SDK

Applications could use a proprietary SDK from one log-management platform.

### Advantages

- deep vendor integration
- vendor-specific metadata
- potentially fast initial adoption

### Disadvantages

- vendor lock-in
- proprietary APIs in application code
- difficult backend migration
- duplicated instrumentation
- inconsistent local development behavior
- reduced portability

---

## 5.3 Option C: Structured JSON Logging

Applications emit one structured JSON object per log event.

### Advantages

- machine-readable
- stable searchable fields
- backend-neutral
- compatible with Kubernetes collectors
- supports trace correlation
- supports field-level filtering
- enables security redaction
- improves dashboard and alert reliability

### Disadvantages

- less visually compact in raw terminal output
- schema governance is required
- careless field creation may increase cost
- JSON encoding introduces some overhead
- developers must follow consistent conventions

---

# 6. Decision

The AstraForge Supply Platform adopts structured JSON logging as the standard production logging format.

Every application log event must be emitted as one complete JSON object.

The platform will use:

- SLF4J as the application logging facade
- Logback or another approved SLF4J-compatible implementation
- structured JSON encoders
- OpenTelemetry trace and span correlation
- controlled MDC usage
- standard output and standard error
- centralized aggregation outside the application
- backend-neutral structured fields

Plain-text logging may be used for local development only when enabled through environment-specific configuration.

---

# 7. Rationale

Structured logging provides a stable contract between applications and the observability platform.

Instead of requiring the backend to parse text such as:

```text
Payment provider timed out after 1500 milliseconds
```

the application emits:

```json
{
  "level": "WARN",
  "service": "payments-service",
  "operation": "payment.authorize",
  "dependency": "external-payment-provider",
  "result": "timeout",
  "durationMs": 1500
}
```

This enables:

- precise searches
- stable dashboards
- reliable alerts
- controlled retention
- field-level redaction
- distributed correlation
- automated analysis

---

# 8. Logging Architecture

The platform logging flow is:

```text
Application

↓

SLF4J

↓

Structured JSON Encoder

↓

stdout / stderr

↓

Kubernetes Log Collector

↓

Centralized Log Backend

↓

Search, Dashboards and Alerts
```

Applications must not communicate directly with the centralized log backend.

---

# 9. Application Responsibilities

Each application is responsible for:

- selecting the correct log level
- creating a meaningful message
- attaching approved structured fields
- correlating logs with traces
- sanitizing sensitive values
- avoiding duplicate logs
- avoiding excessive volume
- preserving one-event-per-record formatting

---

# 10. Platform Responsibilities

The platform is responsible for:

- collecting container logs
- forwarding logs securely
- adding infrastructure metadata
- buffering and retrying delivery
- enforcing retention
- controlling backend access
- providing search and dashboards
- monitoring ingestion health
- applying approved filtering and redaction

---

# 11. Standard Log Structure

Every production log should contain a common core schema.

Recommended fields include:

```json
{
  "timestamp": "2026-07-23T18:40:00.125Z",
  "level": "INFO",
  "service": "orders-service",
  "serviceVersion": "1.8.2",
  "environment": "production",
  "logger": "io.astraforge.supplyplatform.orders.OrderApplicationService",
  "thread": "virtual-142",
  "message": "Order approved successfully",
  "operation": "order.approve",
  "result": "success",
  "traceId": "4f57c9f8064a4d6a8bd90bb2999cda00",
  "spanId": "04af20f271923001",
  "correlationId": "3cc93ab8-57da-4f0d-a7e0-38ec314e58af",
  "durationMs": 142
}
```

Not every field is required for every event.

---

# 12. Mandatory Core Fields

Every log event must contain:

- `timestamp`
- `level`
- `service`
- `environment`
- `logger`
- `message`

Where available, logs must also include:

- `serviceVersion`
- `traceId`
- `spanId`
- `correlationId`
- `operation`
- `result`

---

# 13. Timestamp

The timestamp must:

- use UTC
- use ISO 8601
- include milliseconds or greater precision
- include an explicit `Z` or UTC offset

Preferred example:

```text
2026-07-23T18:40:00.125Z
```

Local time without an offset is prohibited.

---

# 14. Log Level Field

The level must use standardized uppercase values:

```text
TRACE

DEBUG

INFO

WARN

ERROR
```

Custom level names are prohibited unless approved platform-wide.

---

# 15. Service Field

The `service` field must match the OpenTelemetry `service.name`.

Good example:

```text
orders-service
```

Poor examples:

```text
orders-service-pod-ab123

orders-service-prod-v18

orders
```

The value must remain stable across replicas.

---

# 16. Service Version

The `serviceVersion` field should identify the deployed build.

Accepted values include:

- semantic version
- release identifier
- build number
- short commit hash

It should match the OpenTelemetry `service.version` resource attribute.

---

# 17. Environment

The `environment` field must use the standardized deployment environment name.

Examples:

```text
local

test

development

staging

production
```

Aliases such as `prod`, `prd` and `live` must not coexist without explicit normalization.

---

# 18. Logger Name

The `logger` field should contain the logger category, normally the Java class name.

Example:

```text
io.astraforge.supplyplatform.orders.application.ApproveOrderService
```

The logger name supports diagnosis but must not be the primary business classification field.

---

# 19. Thread Field

The `thread` field may be included for diagnostics.

Thread names must not be used as stable correlation identifiers.

This is particularly important when using:

- virtual threads
- executor pools
- reactive pipelines
- asynchronous callbacks

---

# 20. Message Field

The message must be:

- concise
- meaningful
- stable enough for human diagnosis
- free from secrets
- independent from backend-specific formatting
- written as a completed diagnostic statement

Good example:

```text
Order approval completed
```

Poor example:

```text
Here
```

Poor example:

```text
An error occurred
```

---

# 21. Structured Fields Over Message Parsing

Important data must be stored as fields rather than embedded only in the message.

Poor example:

```text
Order approval failed after 3 attempts in 1820 ms
```

Preferred:

```json
{
  "message": "Order approval failed",
  "operation": "order.approve",
  "attempts": 3,
  "durationMs": 1820,
  "result": "failure"
}
```

---

# 22. Field Naming Convention

Structured field names must use lower camel case.

Examples:

```text
traceId

spanId

correlationId

aggregateId

eventType

durationMs

retryAttempt
```

Mixing naming styles is prohibited.

---

# 23. Field Types

Fields must preserve semantic types.

Examples:

```json
{
  "retryAttempt": 2,
  "durationMs": 145,
  "success": true
}
```

Do not encode numeric or Boolean values as strings without a compatibility reason.

Poor example:

```json
{
  "retryAttempt": "2",
  "success": "true"
}
```

---

# 24. Stable Field Semantics

A field name must always represent the same concept.

For example:

```text
correlationId
```

must not represent:

- trace ID in one service
- order ID in another service
- request ID in another service

Schema consistency is mandatory.

---

# 25. OpenTelemetry Correlation

Logs created within an active OpenTelemetry span must contain:

- `traceId`
- `spanId`

The identifiers must match the active OpenTelemetry context.

This allows navigation:

```text
Log

↓

Trace

↓

Related spans

↓

Dependency failure
```

---

# 26. Trace ID

The `traceId` field is the distributed trace identifier.

It must not be:

- generated separately by logging code
- replaced with a business identifier
- used as an audit identifier
- modified during propagation

---

# 27. Span ID

The `spanId` field identifies the active span that produced the log.

It allows logs to be associated with a precise operation inside the trace.

---

# 28. Logs Without Active Traces

Some logs occur without an active trace.

Examples:

- application startup
- application shutdown
- configuration validation
- background scheduler initialization
- collector failure
- uncaught early startup exceptions

Such logs remain valid without `traceId` and `spanId`.

---

# 29. Correlation ID

The platform may use a business or request correlation identifier in addition to the OpenTelemetry trace ID.

The `correlationId` should:

- remain stable across a logical workflow
- be propagated through HTTP and SQS
- be distinct from trace ID
- be safe to expose operationally
- have one defined meaning

---

# 30. Correlation ID Generation

A correlation ID may originate from:

- an accepted inbound header
- an API gateway
- the first service in the workflow
- a business command
- a saga coordinator

Untrusted inbound values must be validated before reuse.

---

# 31. Correlation ID Validation

Inbound correlation IDs must have:

- maximum length
- approved character set
- expected format where applicable
- newline removal
- control-character removal

Invalid values should be replaced with a generated identifier.

---

# 32. HTTP Correlation

HTTP request logs should correlate:

```text
Inbound request

↓

Application operation

↓

Outbound HTTP calls

↓

Response
```

Relevant fields may include:

- `httpMethod`
- `httpRoute`
- `httpStatus`
- `clientName`
- `operation`
- `durationMs`
- `correlationId`
- `traceId`
- `spanId`

---

# 33. HTTP Route

Logs must prefer normalized routes.

Preferred:

```text
/api/v1/orders/{orderId}
```

Avoid:

```text
/api/v1/orders/4a378abd-b392-4d12-9ea8-6f646f61e903
```

Raw paths may expose identifiers and create excessive cardinality.

---

# 34. HTTP Request Logging

The platform should log request summaries at controlled boundaries.

A request summary may include:

- method
- normalized route
- result
- status
- duration
- trace ID
- correlation ID

The full request body must not be logged by default.

---

# 35. HTTP Response Logging

The platform should log response summaries rather than full payloads.

A response summary may include:

- status code
- result category
- duration
- response size where useful
- error code where applicable

---

# 36. HTTP Header Logging

HTTP headers must not be logged by default.

Only explicitly allowlisted headers may be captured.

Prohibited headers include:

- `Authorization`
- `Cookie`
- `Set-Cookie`
- proxy authorization headers
- API key headers
- session headers

---

# 37. SQS Correlation

SQS producer and consumer logs should include approved messaging fields.

Examples:

- `eventId`
- `eventType`
- `eventVersion`
- `topic`
- `consumerGroup`
- `operation`
- `retryAttempt`
- `correlationId`
- `causationId`
- `traceId`
- `spanId`

---

# 38. SQS Topic

The SQS queue may be logged because it has bounded cardinality.

Partition and offset may be logged for diagnostics, but must not become metric labels.

---

# 39. SQS Producer Logging

A successful producer log may contain:

```json
{
  "message": "Integration event published",
  "operation": "sqs.publish",
  "eventType": "ORDER_APPROVED",
  "eventVersion": 2,
  "topic": "orders.events",
  "result": "success",
  "durationMs": 24
}
```

Do not log the full event payload.

---

# 40. SQS Consumer Logging

A consumer summary may contain:

```json
{
  "message": "Integration event processed",
  "operation": "sqs.consume",
  "eventType": "ORDER_APPROVED",
  "topic": "orders.events",
  "consumerGroup": "inventory-service",
  "result": "success",
  "durationMs": 87
}
```

---

# 41. Duplicate Event Logging

Duplicate events should not normally be logged as errors.

Recommended classification:

```text
level = INFO or DEBUG

result = duplicate

operation = event.process
```

A sudden increase in duplicates should be detected through metrics.

---

# 42. Dead-Letter Logging

Dead-letter routing requires a high-quality structured log.

Relevant fields include:

- event type
- topic
- consumer group
- retry count
- failure category
- destination dead-letter topic
- correlation ID
- trace ID
- sanitized error information

The event payload must remain excluded or redacted.

---

# 43. Transactional Outbox Logging

Outbox logs should distinguish:

- polling
- batch claimed
- event published
- retry scheduled
- publication exhausted
- row marked as sent
- row moved to terminal failure

Relevant fields include:

- `operation`
- `batchSize`
- `eventType`
- `attempt`
- `result`
- `durationMs`

---

# 44. Saga Logging

Saga logs should include:

- saga type
- saga ID where permitted
- current state
- transition
- triggering event
- result
- retry attempt
- compensation state
- trace ID
- correlation ID

Do not log the complete saga context or embedded personal data.

---

# 45. Long-Running Workflow Correlation

A long-running Saga may span several traces.

The stable Saga identifier and correlation identifier support cross-trace investigation.

The trace ID alone is not sufficient for long-running workflow search.

---

# 46. Scheduled Job Logging

Scheduled jobs should log:

- job name
- execution ID
- scheduled time
- start time
- end time
- result
- processed count
- failure count
- duration
- trace ID where instrumentation creates a root span

---

# 47. Batch Logging

Batch processes must avoid one `INFO` log for every successfully processed record.

Preferred pattern:

```text
Start summary

Periodic bounded progress summary

Completion summary

Individual WARN or ERROR only for exceptional records
```

This prevents excessive log volume.

---

# 48. Log Levels

Log levels must communicate operational significance consistently.

| Level | Intended Usage |
|---|---|
| `TRACE` | Temporary, highly detailed diagnostics |
| `DEBUG` | Development and controlled troubleshooting details |
| `INFO` | Important successful lifecycle or business-operation summary |
| `WARN` | Recoverable abnormal condition or degraded behavior |
| `ERROR` | Failed operation requiring investigation or intervention |

---

# 49. TRACE

`TRACE` should be disabled in production by default.

It may be enabled temporarily for:

- one service
- one package
- one bounded diagnostic window
- one approved incident investigation

`TRACE` must not contain secrets or full payloads.

---

# 50. DEBUG

`DEBUG` is appropriate for:

- internal decision details
- cache lookup paths
- retry attempt details
- selected mapping decisions
- controlled troubleshooting context

Production use must be limited because debug logs can create:

- cost
- noise
- performance overhead
- sensitive-data exposure

---

# 51. INFO

`INFO` is appropriate for important operational events.

Examples:

- application started
- order approved
- outbox batch completed
- SQS message/event processed
- circuit breaker changed state
- scheduled job completed
- deployment version loaded

Do not log every method entry and exit at `INFO`.

---

# 52. WARN

`WARN` is appropriate when the operation remains controlled but requires attention.

Examples:

- fallback activated
- dependency retry exhausted but degraded response returned
- cache unavailable and local fallback used
- optimistic-lock conflict after retry
- malformed optional external field ignored
- delayed Saga step

---

# 53. ERROR

`ERROR` is appropriate when:

- an operation failed
- data could not be processed
- an unexpected exception escaped a boundary
- a critical dependency failure prevented completion
- a message was moved to dead letter
- a terminal outbox publication failure occurred

Expected validation errors and business rejections should not automatically use `ERROR`.

---

# 54. Business Rejections

Expected business outcomes should normally be logged at `INFO` or `WARN`, depending on operational significance.

Examples:

- order approval rejected
- payment declined
- insufficient inventory
- invalid workflow transition
- expired quotation

These outcomes are not necessarily system failures.

---

# 55. Authentication and Authorization Failures

Security-related failures require controlled classification.

Examples:

- invalid token
- insufficient permission
- expired session
- forbidden operation

Logs must not include:

- token contents
- password
- claims containing unnecessary personal data
- authorization headers

Repeated suspicious activity may be routed to security monitoring.

---

# 56. Exception Logging

Exceptions should be logged at the boundary that has enough context to classify the failure.

Examples of appropriate boundaries:

- HTTP exception handler
- SQS listener error handler
- scheduled job coordinator
- outbox dispatcher boundary
- message dead-letter handler

---

# 57. Log Once

The same exception should normally be logged once.

Poor flow:

```text
Repository logs ERROR and rethrows

Service logs ERROR and rethrows

Controller advice logs ERROR
```

Preferred flow:

```text
Repository translates or enriches

Service propagates

Boundary logs once with complete context
```

---

# 58. Log or Rethrow

When catching an exception, code must:

- handle it
- translate and rethrow it
- add meaningful context and rethrow it
- or log it when it is intentionally absorbed

Catching, logging and rethrowing at every layer is prohibited.

---

# 59. Exception Message

Raw exception messages may contain:

- SQL
- credentials
- URLs with secrets
- external payloads
- personal data
- implementation details

Exception messages must be sanitized before being copied into structured fields.

---

# 60. Stack Traces

Stack traces are useful for unexpected failures.

They should be included for:

- unhandled exceptions
- unexpected infrastructure failures
- defects
- terminal processing failures

They should not be included for every expected business rejection.

---

# 61. Stack Trace Size

Large stack traces increase storage cost and search noise.

The logging implementation may configure:

- maximum stack depth
- common-frame omission
- maximum character length
- root-cause extraction

Truncation must remain visible through a field such as:

```text
stackTraceTruncated = true
```

---

# 62. Exception Type

The exception class may be logged in a dedicated field.

Example:

```json
{
  "errorType": "java.net.SocketTimeoutException",
  "errorCategory": "dependency_timeout"
}
```

The type supports classification without depending solely on message text.

---

# 63. Error Category

The platform should use controlled error categories.

Examples:

```text
validation

authentication

authorization

business_rejection

dependency_timeout

dependency_unavailable

database

messaging

serialization

concurrency

configuration

unexpected
```

The list must remain governed.

---

# 64. Error Code

Application exceptions should expose stable error codes where useful.

Example:

```text
ORDER_CONCURRENT_MODIFICATION
```

Error codes are more reliable than natural-language messages for:

- dashboards
- searches
- alerts
- support procedures
- client responses

---

# 65. Sensitive Data

Logs must not contain sensitive information unless explicitly approved and protected.

Prohibited examples include:

- passwords
- access tokens
- refresh tokens
- API keys
- private keys
- session cookies
- payment card data
- security answers
- database credentials
- authentication headers

---

# 66. Personal Data

Personal data must not be logged by default.

Examples include:

- full name
- personal email
- telephone number
- physical address
- national identification number
- date of birth
- customer documents
- precise location
- financial account information

Where operationally necessary, data must be minimized and masked.

---

# 67. LGPD and Privacy

Logging practices must support applicable privacy requirements, including LGPD principles such as:

- purpose limitation
- necessity
- security
- prevention
- accountability

The existence of operational value does not automatically justify logging personal data.

---

# 68. Data Minimization

Only data necessary for diagnosis should be included.

Instead of:

```json
{
  "customerEmail": "customer@example.com"
}
```

prefer:

```json
{
  "customerReferencePresent": true
}
```

or a controlled internal identifier where approved.

---

# 69. Identifier Logging

Business identifiers may be logged when operationally required and legally approved.

Examples:

- order ID
- event ID
- saga ID
- correlation ID

They must not be used as metric labels.

Retention and access policies must consider whether an identifier can indirectly identify a person.

---

# 70. Masking

Approved masking should preserve limited diagnostic value.

Examples:

```text
john.doe@example.com
→ j***@example.com
```

```text
4111111111111111
→ ************1111
```

Masking functions must be centralized, tested and reusable.

---

# 71. Hashing

Hashing may be used when stable correlation is required without exposing a raw value.

Hashing must:

- use an approved cryptographic algorithm
- use a controlled salt or key where required
- avoid reversible encoding
- be evaluated for dictionary-attack risk
- follow security architecture standards

---

# 72. Redaction

Redaction removes the value entirely.

Example:

```json
{
  "authorization": "[REDACTED]"
}
```

For prohibited fields, omission is generally preferable to retaining a redacted placeholder.

---

# 73. Sanitization

Untrusted values must be sanitized before logging.

Sanitization should remove or neutralize:

- carriage return
- newline
- tab where unsafe
- control characters
- terminal escape sequences
- excessively long values

Structured JSON encoding reduces but does not eliminate all injection risks.

---

# 74. Log Injection

An attacker may attempt to inject false log entries using newline characters.

Example input:

```text
invalid-user
ERROR Administrator authenticated
```

The logging encoder and sanitization layer must ensure this remains one safe structured value.

---

# 75. Maximum Field Length

User-controlled and external values must have a maximum logged length.

Long values should be truncated safely.

Example fields requiring limits:

- external error message
- validation detail
- URL
- file name
- user agent
- event metadata
- exception message

---

# 76. Truncation

Truncation should preserve evidence that truncation occurred.

Example:

```json
{
  "externalMessage": "The remote provider returned an invalid response...",
  "externalMessageTruncated": true
}
```

---

# 77. Payload Logging

Full HTTP, SQS, database or external-service payload logging is prohibited by default.

Payloads may contain:

- personal data
- secrets
- financial information
- confidential business fields
- large binary content
- unstable schemas

---

# 78. Diagnostic Payload Logging

Temporary payload logging requires:

- explicit approval
- non-production environment where possible
- field allowlist
- masking
- expiration date
- owner
- rollback plan
- cost assessment
- security review

---

# 79. Request and Response Filters

Generic request/response logging filters must not blindly serialize all bodies.

Approved filters should log only:

- method
- route
- status
- duration
- content type
- bounded size metadata
- approved correlation fields

---

# 80. Database Logging

Application logs must not contain:

- complete SQL with parameters
- connection strings with credentials
- database passwords
- personal query values

SQL debugging in production requires explicit temporary approval.

---

# 81. External API Logging

External API logs should include:

- dependency name
- operation
- status category
- duration
- timeout
- retry attempt
- circuit-breaker state
- sanitized error category

Do not log:

- full request
- full response
- tokens
- signed URLs
- secret query parameters

---

# 82. MDC

Mapped Diagnostic Context may be used to attach request and trace correlation fields.

Approved MDC fields include:

- trace ID
- span ID
- correlation ID
- operation
- event ID
- saga ID where appropriate

MDC usage must remain bounded and controlled.

---

# 83. MDC Lifecycle

MDC values must be:

- added at the correct processing boundary
- propagated across asynchronous execution
- removed after processing
- restored when nested context completes

Failure to clear MDC may leak context between unrelated operations.

---

# 84. Thread Pools

Traditional thread pools reuse threads.

MDC values left on a reused thread can appear in another request's logs.

Every executor integration must ensure context restoration and cleanup.

---

# 85. Virtual Threads

Virtual threads reduce some thread-reuse concerns but do not eliminate context-propagation requirements.

The platform must validate MDC and OpenTelemetry context across:

- virtual-thread executors
- structured concurrency
- delegated tasks
- callbacks
- asynchronous APIs

---

# 86. Reactive Context

Where reactive programming is used, thread-local MDC alone is insufficient.

Logging correlation must integrate with the reactive context and OpenTelemetry context propagation.

Manual blocking bridges that lose context are prohibited.

---

# 87. Executor Instrumentation

Approved executors should propagate:

- OpenTelemetry context
- MDC fields
- security context where required
- request context where required

Propagation must be covered by tests.

---

# 88. Context Cleanup

Context cleanup is mandatory even when processing fails.

Use:

- `try/finally`
- scoped context APIs
- framework-managed filters
- instrumented executors

Do not depend on normal completion.

---

# 89. Logger Usage

Java classes should declare a logger using the standard SLF4J approach.

Example:

```java
private static final Logger LOGGER =
        LoggerFactory.getLogger(OrderApplicationService.class);
```

Lombok logging annotations may be used only when consistent with project standards.

---

# 90. Parameterized Logging

Parameterized logging must be used.

Preferred:

```java
LOGGER.info(
        "Order approval completed: orderId={}, result={}",
        orderId,
        result
);
```

Avoid eager string concatenation:

```java
LOGGER.info(
        "Order approval completed: orderId=" + orderId
                + ", result=" + result
);
```

---

# 91. Structured Arguments

Where the logging encoder supports structured arguments, important fields should be attached as structured values.

Conceptual example:

```java
LOGGER.atInfo()
        .addKeyValue("operation", "order.approve")
        .addKeyValue("orderId", orderId)
        .addKeyValue("result", "success")
        .log("Order approval completed");
```

The exact API must be standardized in a shared logging library.

---

# 92. Lazy Evaluation

Expensive diagnostic values must not be calculated when the level is disabled.

Examples:

- large object serialization
- collection transformation
- stack inspection
- database lookup
- payload masking

Logging must not materially alter application behavior.

---

# 93. Object Logging

Passing arbitrary domain objects to a logger is prohibited.

Poor example:

```java
LOGGER.info("Order: {}", order);
```

The object's `toString()` may expose:

- personal data
- full child collections
- secrets
- unstable implementation details

Log explicit approved fields instead.

---

# 94. DTO Logging

Request and response DTOs must not be logged as complete objects by default.

Use a safe summary.

Example:

```json
{
  "operation": "order.create",
  "itemCount": 4,
  "segment": "MOTORCYCLE",
  "hasDeliveryAddress": true
}
```

---

# 95. Entity `toString()`

JPA entity `toString()` implementations must not include:

- lazy collections
- bidirectional relationships
- personal data
- secrets
- complete aggregate graphs

Logging entities through `toString()` is prohibited.

---

# 96. Collection Logging

Large collections must not be logged in full.

Preferred fields include:

- collection size
- bounded sample
- success count
- failure count
- skipped count

---

# 97. Success Logging

Successful low-level operations should not all generate `INFO` logs.

Examples that normally should not produce `INFO`:

- repository save succeeded
- mapper completed
- DTO created
- validation method entered
- cache getter returned

Business or operational boundaries should generate the summary.

---

# 98. Method Entry and Exit Logging

Automatic method-entry and method-exit logging is prohibited as a general production pattern.

It creates:

- excessive volume
- weak diagnostic value
- duplicated trace information
- performance overhead
- possible data exposure

Distributed traces are more appropriate for execution-path visualization.

---

# 99. Retry Logging

Retries must be logged without amplification.

Recommended strategy:

- first transient retry at `DEBUG`
- meaningful degradation at `WARN`
- exhaustion at `WARN` or `ERROR` depending on outcome
- total retries captured through metrics

Do not produce a full stack trace for every retry attempt.

---

# 100. Circuit Breaker Logging

Circuit-breaker state transitions should be logged.

Relevant fields include:

- dependency
- previous state
- new state
- failure rate
- operation
- result

Individual circuit-open rejections should normally be measured rather than logged at high volume.

---

# 101. Optimistic Locking Logging

Expected concurrency conflicts should not be logged as unexpected system errors.

Recommended fields include:

- aggregate type
- operation
- retry attempt
- result
- correlation ID
- trace ID

High conflict rates should be monitored through metrics.

---

# 102. Validation Logging

Normal request validation failures should not generate stack traces.

A boundary summary may include:

- route
- error code
- invalid field count
- result
- trace ID
- correlation ID

Avoid logging rejected field values.

---

# 103. Startup Logging

Startup logs should include:

- service name
- service version
- environment
- active profiles
- Java version
- startup duration
- successful readiness transition

They must not include full environment variables or secret configuration.

---

# 104. Configuration Logging

Applications may log the presence or category of configuration.

Preferred:

```json
{
  "message": "External payment integration configured",
  "dependency": "payment-provider",
  "endpointConfigured": true,
  "authenticationConfigured": true
}
```

Prohibited:

```json
{
  "apiKey": "secret-value",
  "password": "secret-value"
}
```

---

# 105. Shutdown Logging

Shutdown logs should include:

- shutdown initiated
- reason where available
- graceful-drain result
- pending-work summary
- shutdown duration

A pod termination is not necessarily an application error.

---

# 106. Kubernetes Logging

Applications must write logs to:

- standard output
- standard error

Applications must not write rotating application log files inside containers.

Kubernetes and the platform collector manage transport and persistence.

---

# 107. stdout and stderr

Recommended convention:

- normal structured logs to stdout
- severe runtime or bootstrap failures may use stderr

The collector configuration must handle both streams consistently.

---

# 108. One Event Per Line

Each JSON log event must occupy one logical line.

This supports:

- container-runtime collection
- line-oriented forwarding
- reliable parsing
- reduced multiline complexity

Stack traces should be encoded as structured content rather than uncontrolled multiline output where possible.

---

# 109. Multiline Logs

Uncontrolled multiline logs are discouraged.

When a stack trace requires multiline representation, the collector must preserve it as one log event.

The preferred encoder should emit stack traces inside the JSON object.

---

# 110. Pod Metadata

The platform collector may enrich logs with:

- cluster
- namespace
- pod name
- container name
- node
- deployment
- replica set

Applications should not manually generate these infrastructure fields.

---

# 111. Pod Name

Pod name has high operational value but changes across deployments and restarts.

It should be used for instance diagnosis, not as a stable service identifier.

---

# 112. Container File System

Applications must not rely on container-local log files for persistence.

Container file systems are ephemeral.

---

# 113. Log Collector

An approved collector may include:

- Fluent Bit
- Vector
- OpenTelemetry Collector
- managed cloud agents
- another approved CNCF-compatible collector

The final choice is a platform implementation detail.

---

# 114. OpenTelemetry Logs

OpenTelemetry log export may be adopted where supported and operationally mature.

The application-level logging contract remains:

- SLF4J
- structured fields
- trace correlation
- backend independence

The application must not depend directly on a specific log backend.

---

# 115. Centralized Backend

Potential backends include:

- Grafana Loki
- Elasticsearch
- OpenSearch
- managed cloud logging
- commercial observability platforms

The backend must support:

- structured field search
- trace correlation
- access control
- retention policies
- secure ingestion
- dashboards and alerts

---

# 116. Loki Considerations

When using Loki, labels must remain low cardinality.

Appropriate Loki labels may include:

- service
- environment
- level
- namespace

Identifiers such as:

- trace ID
- order ID
- event ID
- saga ID

should remain structured log fields rather than index labels.

---

# 117. Elasticsearch or OpenSearch Considerations

Dynamic field mapping must be controlled.

Unbounded field names or inconsistent types may cause:

- mapping explosion
- index instability
- ingestion failure
- expensive storage
- failed searches

Index templates should define the standard schema.

---

# 118. Field Cardinality

High-cardinality fields are acceptable as searchable values only when the backend architecture supports them appropriately.

They must not automatically become indexed labels.

Examples:

- trace ID
- correlation ID
- event ID
- order ID

---

# 119. Dynamic Field Names

Dynamic field names are prohibited.

Poor example:

```json
{
  "customer_12345": "active"
}
```

Preferred:

```json
{
  "customerId": "12345",
  "customerStatus": "active"
}
```

---

# 120. Schema Governance

The structured logging schema must be documented and version controlled.

Every shared field should define:

- name
- type
- meaning
- cardinality
- sensitivity
- required status
- allowed values
- retention considerations

---

# 121. Custom Fields

Service-specific fields are allowed only when:

- they provide meaningful diagnostic value
- their semantics are documented
- they do not duplicate standard fields
- they use bounded types
- they do not expose sensitive data
- they do not create dynamic mappings

---

# 122. Reserved Fields

The platform should reserve common fields such as:

```text
timestamp

level

service

serviceVersion

environment

logger

message

traceId

spanId

correlationId

operation

result

durationMs

errorCode

errorCategory

errorType
```

Services must not redefine their semantics.

---

# 123. Logging Library

The platform should provide a shared logging library or starter that configures:

- JSON encoding
- standard fields
- OpenTelemetry correlation
- MDC integration
- sanitization
- masking
- environment metadata
- service metadata
- exception formatting
- field limits

---

# 124. Shared Library Boundaries

The shared logging library must not:

- contain business-specific messages
- create coupling between bounded contexts
- swallow application exceptions
- automatically serialize request bodies
- automatically log every method
- force one centralized backend

---

# 125. Backend Independence

Applications must not import APIs from:

- Loki clients
- Elasticsearch clients
- proprietary logging vendors
- direct log-shipping SDKs

Backend routing belongs to the platform collector.

---

# 126. Logging Configuration

Logging configuration must remain externalized by environment.

Configuration includes:

- root level
- package levels
- encoder
- stack-trace limits
- sampling
- asynchronous queue size
- redaction
- destination stream

---

# 127. Production Log Level

The normal production root level should be:

```text
INFO
```

Selected infrastructure packages may use:

```text
WARN
```

Temporary overrides require controlled operational procedures.

---

# 128. Framework Logging

Noisy framework packages should use controlled levels.

Examples include:

- Hibernate SQL logging
- SQS protocol internals
- Netty internals
- Spring security debug logging
- connection-pool internals

These should not be enabled broadly in production.

---

# 129. SQL Logging

Hibernate SQL and parameter logging must be disabled in production by default.

Parameter binding logs may expose personal or confidential data.

---

# 130. Security Debug Logging

Spring Security debug output must not be enabled in production without explicit security approval.

It may expose:

- filter behavior
- authentication details
- session state
- request metadata

---

# 131. Asynchronous Logging

Asynchronous logging may be used to reduce application latency.

The implementation must define:

- queue capacity
- overflow behavior
- shutdown flushing
- error handling
- memory limit
- discard policy

---

# 132. Bounded Queue

The asynchronous logging queue must be bounded.

An unbounded queue can cause memory exhaustion during backend or disk pressure.

---

# 133. Overflow Policy

When the logging queue is full, the platform must define which logs may be dropped.

A possible policy is:

- preserve `ERROR`
- prioritize `WARN`
- drop `DEBUG` first
- sample repetitive `INFO`

The exact implementation must be tested.

---

# 134. Logging Failure

Logging failure must not stop normal business processing.

However, severe logging infrastructure failures should be observable through:

- internal metrics
- fallback stderr reporting
- collector health alerts
- dropped-log counters

---

# 135. Synchronous Emergency Logging

Critical bootstrap failures may require synchronous stderr logging because the asynchronous logger may not be initialized.

This should remain limited to startup and fatal runtime boundaries.

---

# 136. Performance

Logging introduces costs through:

- message construction
- JSON encoding
- stack-trace generation
- masking
- queueing
- I/O
- collection
- indexing
- retention

Logging performance must be evaluated under realistic load.

---

# 137. Performance Principles

The platform must:

- avoid eager expensive calculations
- avoid unnecessary stack traces
- avoid full-object serialization
- use parameterized logging
- bound field length
- reduce repetitive success logs
- use metrics for high-frequency counts
- use traces for detailed execution paths

---

# 138. Log Volume

Log volume should be measured by:

- service
- level
- operation
- environment
- deployment version

Unexpected growth may indicate:

- retry storm
- new verbose logging
- exception loop
- duplicate boundary logging
- debug level enabled
- poisoned SQS message
- health-check logging

---

# 139. Health Check Logging

Successful liveness and readiness probes should not generate application `INFO` logs for every request.

Failures may be logged at a controlled level.

Access-log configuration should exclude or sample health endpoints where appropriate.

---

# 140. Access Logs

HTTP access logs may be generated at the ingress, gateway or application layer.

The platform must avoid duplicate access logs across all layers unless each serves a distinct purpose.

---

# 141. Log Sampling

Repetitive logs may be sampled.

Examples:

- repeated dependency timeout
- circuit-open rejection
- identical invalid external response
- duplicate SQS message
- health-check failure storm

Sampling must preserve:

- first occurrence
- periodic summaries
- total count through metrics
- severe transitions

---

# 142. Sampling Restrictions

Do not sample away:

- unique terminal failures
- security incidents
- data-integrity failures
- dead-letter routing
- failed Saga compensation
- fatal startup failures

---

# 143. Aggregated Summaries

For repetitive conditions, emit summaries.

Example:

```json
{
  "message": "External dependency timeout summary",
  "dependency": "pricing-service",
  "windowSeconds": 60,
  "failureCount": 824,
  "sampledLogCount": 10
}
```

---

# 144. Metrics Versus Logs

Use metrics for:

- counts
- rates
- latency distributions
- alert thresholds
- state values

Use logs for:

- event-specific context
- diagnostic detail
- failure classification
- recovery information

Do not use logs as the only source for high-frequency operational metrics.

---

# 145. Traces Versus Logs

Use traces for:

- distributed execution path
- timing decomposition
- span relationships
- dependency chains

Use logs for:

- detailed decisions
- sanitized error context
- operational summaries
- discrete lifecycle events

The signals are complementary.

---

# 146. Audit Versus Logging

Operational logs are not the authoritative business audit trail.

Audit requirements may include:

- immutable storage
- legal retention
- actor identity
- before-and-after values
- non-repudiation
- controlled access
- explicit business semantics

These belong to a dedicated audit mechanism.

---

# 147. Security Audit Events

Security audit events may use a dedicated pipeline or schema.

Examples:

- login success
- login failure
- privilege change
- permission denial
- administrative action
- secret rotation
- account lockout

They require stricter retention and access policies than ordinary application logs.

---

# 148. Business Audit Events

Committed business actions such as order approval should be recorded through the audit architecture when required.

An `INFO` log saying that an order was approved does not replace an audit record.

---

# 149. Retention

Retention must be defined per:

- environment
- log category
- security classification
- operational value
- compliance requirement
- storage cost

Production `ERROR` logs may require longer retention than development `DEBUG` logs.

---

# 150. Retention Categories

Suggested categories include:

- application operational logs
- security logs
- audit records
- platform logs
- ingress access logs
- debug diagnostics

Each category may have a different retention period.

---

# 151. Data Deletion

When privacy or legal requirements require deletion, the logging architecture must support the approved process.

This reinforces the principle of avoiding personal data in logs.

---

# 152. Access Control

Centralized logs must use role-based access control.

Access should follow least privilege.

Examples:

- developers access assigned non-production services
- production support accesses approved production logs
- security team accesses security events
- audit team accesses audit records
- administrators manage retention and ingestion

---

# 153. Production Access

Production log access must be:

- authenticated
- authorized
- auditable
- limited by role
- reviewed periodically

Exporting production logs to personal devices is prohibited unless explicitly approved.

---

# 154. Encryption

Logs must be encrypted:

- in transit
- at rest

Collector-to-backend communication must use approved authentication and TLS.

---

# 155. Tenant Isolation

If the platform becomes multi-tenant, log access must prevent cross-tenant disclosure.

Tenant identifiers must be treated as potentially sensitive and high cardinality.

---

# 156. Log Governance

A logging governance process should review:

- new common fields
- sensitive fields
- retention changes
- high-volume logs
- new alerts
- production debug requests
- backend mapping changes
- schema changes

---

# 157. Code Review

Code review should verify:

- correct level
- meaningful message
- no duplicate logging
- no sensitive data
- no full objects
- no full payloads
- parameterized logging
- bounded values
- trace correlation
- consistent error classification

---

# 158. Logging Checklist

For every new log, reviewers should ask:

1. Is this event operationally useful?
2. Is the level correct?
3. Could a metric be more appropriate?
4. Could a trace already provide the information?
5. Does the log contain sensitive data?
6. Are important values structured?
7. Are fields bounded and documented?
8. Could this event occur at high frequency?
9. Is the same failure logged elsewhere?
10. Does the message remain understandable without hidden context?

---

# 159. Testing Strategy

Logging tests should validate:

- JSON format
- mandatory fields
- trace and span correlation
- correlation ID propagation
- masking
- redaction
- sanitization
- maximum field length
- exception formatting
- no secret leakage
- correct log level
- MDC cleanup
- asynchronous context propagation

---

# 160. Unit Tests

Unit tests are appropriate for:

- masking utilities
- sanitization utilities
- structured field builders
- error classification
- correlation ID validation
- truncation
- safe external-error extraction

---

# 161. Integration Tests

Integration tests should validate:

- Spring Boot logging configuration
- active OpenTelemetry context
- MDC propagation
- HTTP filter behavior
- SQS listener correlation
- virtual-thread propagation
- JSON encoder output
- exception-handler logging

---

# 162. Log Capture Tests

Tests may capture logs using an approved test appender.

Assertions should verify structured fields rather than exact full JSON ordering.

---

# 163. Sensitive Data Tests

Tests must prove that logs do not expose:

- authorization headers
- passwords
- access tokens
- cookies
- secret query parameters
- database credentials
- complete personal payloads

---

# 164. Correlation Tests

A correlation integration test should verify:

```text
Inbound trace context

↓

Application log

↓

Outbound operation

↓

Same trace ID
```

For SQS:

```text
Producer context

↓

SQS headers

↓

Consumer context

↓

Consumer logs contain expected correlation
```

---

# 165. MDC Cleanup Test

A test should process two operations sequentially on a reused executor thread.

It must prove that fields from the first operation do not appear in the second operation.

---

# 166. Virtual-Thread Test

A virtual-thread test should verify that:

- trace ID propagates
- span ID propagates
- correlation ID propagates
- context is cleared afterward

---

# 167. Format Validation

Structured log output should be validated as valid JSON.

Every emitted event must be parseable independently.

---

# 168. Schema Validation

Where practical, representative logs should be validated against a documented schema.

This helps prevent:

- field type drift
- renamed fields
- missing mandatory metadata
- invalid level values
- dynamic field creation

---

# 169. Load Testing

Load tests should measure:

- log events per second
- application latency impact
- CPU overhead
- memory overhead
- asynchronous queue usage
- dropped-log count
- collector throughput
- backend ingestion delay
- storage growth

---

# 170. Collector Failure Testing

Failure tests should simulate:

- collector unavailable
- slow backend
- network partition
- queue saturation
- malformed log event
- backend rejection

Business processing must continue within the defined degradation policy.

---

# 171. Static Analysis

Static analysis or custom rules should detect:

- string concatenation in logging
- logging of complete request objects
- logging of authorization headers
- direct `printStackTrace`
- `System.out.println`
- `System.err.println`
- logger calls inside high-volume loops
- logging and rethrowing without justification
- dynamic logger names
- full exception-message exposure

---

# 172. Prohibited Output APIs

Production application code must not use:

```java
System.out.println(...);
System.err.println(...);
exception.printStackTrace();
```

Approved logging APIs must be used.

Bootstrap code is the only possible exception and requires justification.

---

# 173. Architecture Tests

Architecture tests should enforce:

- Domain code does not depend on log backends
- no proprietary logging SDK in application layers
- no direct Loki or Elasticsearch client use for application logs
- controllers do not log full requests
- exception handlers own boundary logging
- shared sanitization utilities remain in approved modules
- business audit does not depend solely on logs

---

# 174. Alerting From Logs

Log-based alerts may be used for discrete rare events.

Examples:

- dead-letter publication
- terminal compensation failure
- invalid production configuration
- security policy violation
- fatal startup failure

High-frequency reliability alerts should prefer metrics.

---

# 175. Alert Fields

Log-based alerts should rely on stable fields such as:

- `errorCode`
- `errorCategory`
- `operation`
- `result`
- `service`
- `environment`

Alerts should not depend on fragile message-text matching where structured fields exist.

---

# 176. Dashboards

Logging dashboards may include:

- error volume by service
- top error codes
- warnings by dependency
- dead-letter events
- Saga compensation failures
- outbox terminal failures
- security denials
- log volume by level
- dropped-log count

---

# 177. Runbooks

Critical log-based alerts should link to runbooks.

A runbook should include:

- event meaning
- likely causes
- required searches
- related metrics
- trace lookup
- recovery actions
- escalation criteria
- resolution validation

---

# 178. Incident Search Pattern

A recommended investigation sequence is:

```text
Correlation ID or error code

↓

Structured logs

↓

Trace ID

↓

Distributed trace

↓

Dependency metrics

↓

Related workflow state
```

---

# 179. Release Correlation

Logs must contain the deployed service version.

This enables searches such as:

```text
Errors before release

versus

Errors after release
```

---

# 180. Feature Flags

Feature-flag state may be logged only when:

- operationally useful
- low cardinality
- not user-specific
- not sensitive

Do not log complete flag-evaluation context for every request.

---

# 181. Temporary Diagnostic Configuration

Temporary production diagnostic logging must define:

- requester
- approver
- service
- package
- level
- start time
- expiration time
- expected volume
- security impact
- rollback method

---

# 182. Automatic Expiration

Where possible, temporary log-level overrides should expire automatically.

This reduces the risk of leaving verbose production logging enabled.

---

# 183. Cost Governance

The platform should define log-volume budgets by service.

Cost control should consider:

- events per request
- average event size
- stack-trace volume
- retention
- indexing
- cardinality
- production traffic
- debug overrides

---

# 184. Cost Review

A service exceeding its logging budget should review:

- duplicate logs
- high-frequency success logs
- stack-trace repetition
- payload size
- retry loops
- debug packages
- health-check logs
- access-log duplication

---

# 185. Log Quality Metrics

The platform may monitor:

- malformed JSON count
- missing service field
- missing environment field
- dropped-log count
- ingestion delay
- high-cardinality field growth
- unknown error-category count
- debug volume in production

---

# 186. Operational SLOs

The centralized logging platform should define operational objectives for:

- ingestion availability
- maximum search delay
- retention correctness
- query availability
- collector delivery success
- acceptable dropped-log rate

---

# 187. Disaster Recovery

The log backend should have an appropriate recovery strategy based on its operational value.

Operational logs do not necessarily require the same durability as transactional business data.

Security and audit records may require stronger guarantees.

---

# 188. Local Development

Local development may use a human-readable console layout.

The local format should still expose:

- level
- service
- trace ID
- correlation ID
- logger
- message

Production JSON behavior must be covered by tests.

---

# 189. Test Environment

Automated tests should not forward logs to production backends.

Test logs may be:

- captured in memory
- written to test output
- reduced through log-level configuration
- validated through a test appender

---

# 190. Non-Production Logging

Non-production environments may use more verbose logging, but security restrictions remain identical.

Secrets and personal data are prohibited in every environment.

---

# 191. Logging Documentation

Each service should document:

- important operations
- error codes
- custom structured fields
- dashboards
- alerts
- runbooks
- temporary debug procedures

---

# 192. Anti-Patterns

The following are prohibited:

- plain-text-only production logging
- direct log-backend SDK usage
- full request-body logging
- full response-body logging
- full SQS payload logging
- logging passwords
- logging tokens
- logging cookies
- logging authorization headers
- logging private keys
- logging database credentials
- logging full domain objects
- logging full DTOs
- logging JPA entities through `toString()`
- logging every method entry and exit
- logging every successful repository operation
- logging the same exception at multiple layers
- catching, logging and rethrowing without additional value
- using `System.out.println`
- using `System.err.println`
- using `printStackTrace`
- eager string concatenation
- unbounded exception messages
- uncontrolled multiline logs
- dynamic field names
- inconsistent field types
- using order IDs or trace IDs as Loki labels
- treating logs as the audit system
- enabling SQL parameter logging in production
- enabling security debug logging indefinitely
- leaving MDC uncleared
- losing correlation across executors
- logging user-provided newlines without sanitization
- keeping production `DEBUG` enabled without expiration
- relying on text matching when structured fields exist
- creating alerts for every exception
- blocking business processing because log export failed
- using logs as the only source of operational counters
- storing application logs only inside the container file system

---

# 193. Positive Consequences

The decision provides:

- machine-readable logs
- reliable distributed correlation
- easier incident investigation
- backend portability
- consistent field semantics
- improved security controls
- centralized log aggregation
- better dashboard reliability
- better alert reliability
- OpenTelemetry trace integration
- SQS and Saga correlation
- improved Kubernetes operations
- reduced message-text parsing
- clearer error classification
- stronger cost governance
- easier automated validation

---

# 194. Negative Consequences

The decision introduces:

- structured schema governance
- JSON encoding overhead
- logging-library maintenance
- MDC and context-propagation complexity
- collector infrastructure
- field-mapping management
- retention cost
- test requirements
- production access governance
- developer training
- risk of excessive field creation

These costs are accepted because unstructured logging is insufficient for operating the distributed platform.

---

# 195. Neutral Consequences

The decision also means:

- raw terminal logs may be less visually compact
- some identifiers remain high-cardinality searchable fields
- not every log contains trace context
- metrics remain the preferred source for high-frequency alerting
- traces remain the preferred source for execution-path analysis
- audit records remain separate
- production debug logging requires controlled procedures
- some repetitive logs may be sampled
- telemetry backends may apply different indexing strategies

---

# 196. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Sensitive data is logged | High | Medium | Allowlisting, sanitization, masking and security tests |
| Log volume becomes excessive | High | High | Budgets, sampling and review |
| Duplicate exception logging increases noise | Medium | High | Boundary logging and code-review rules |
| MDC leaks between requests | High | Medium | Scoped context and cleanup tests |
| Context is lost in virtual threads | Medium | Medium | Instrumented executors and propagation tests |
| Dynamic fields break backend mappings | High | Medium | Schema governance and index templates |
| High-cardinality labels increase cost | High | Medium | Restrict labels to bounded fields |
| Collector outage affects application | High | Low | Non-blocking export and bounded queues |
| Debug logging remains enabled | Medium | Medium | Automatic expiration and operational approval |
| Stack traces consume excessive storage | Medium | High | Controlled exception logging and truncation |
| Plain-text fallback breaks parsing | Medium | Low | Production configuration validation |
| Log injection corrupts records | High | Low | JSON encoding and sanitization |
| Logs are treated as audit records | High | Medium | Separate audit architecture |
| Full payloads expose confidential data | High | Medium | Prohibit payload logging by default |
| Field type changes break searches | Medium | Medium | Version-controlled schema |
| Backend-specific APIs create lock-in | Medium | Low | SLF4J and collector-based routing |
| Asynchronous queue exhausts memory | High | Low | Bounded queue and overflow policy |
| Logging increases request latency | Medium | Medium | Parameterized and asynchronous logging |
| Production access exposes data | High | Low | RBAC and access auditing |
| Trace sampling prevents correlation | Low | Medium | Logs remain searchable by correlation ID |

---

# 197. Implementation Guidance

The following rules are mandatory:

1. Production logs must use structured JSON.
2. Applications must use SLF4J.
3. Applications must log to stdout or stderr.
4. Applications must not write rotating log files inside containers.
5. Every event must contain the standard core fields.
6. Timestamps must use UTC and ISO 8601.
7. Service names must match OpenTelemetry resource names.
8. Logs inside active spans must contain trace and span IDs.
9. Business correlation IDs must remain distinct from trace IDs.
10. Important diagnostic data must use structured fields.
11. Field names must use lower camel case.
12. Shared field semantics must remain stable.
13. Sensitive data must not be logged.
14. Full payload logging is prohibited by default.
15. Authorization headers, cookies, tokens and passwords must never be logged.
16. User-provided values must be sanitized and length-limited.
17. Parameterized logging is mandatory.
18. Arbitrary object logging is prohibited.
19. The same exception must not be logged at multiple layers.
20. Expected business rejections must not automatically be logged as errors.
21. Boundary components own terminal exception logging.
22. MDC and trace context must be propagated and cleared correctly.
23. Logging context propagation must be tested for executors and virtual threads.
24. Production root logging should normally use `INFO`.
25. Temporary debug logging requires approval and expiration.
26. High-frequency conditions should use metrics and sampling.
27. Log-export failure must not stop business processing.
28. Asynchronous logging queues must remain bounded.
29. Backend-specific logging APIs are prohibited in business applications.
30. Operational logs must not replace business audit records.
31. The structured logging schema must be version controlled.
32. New common fields require governance review.
33. Logging performance and volume must be load tested.
34. Collector health and dropped-log counts must be monitored.
35. Production log access must use least privilege and auditing.

---

# 198. Validation

The decision will be validated through:

- JSON format tests
- logging schema tests
- trace-correlation tests
- HTTP correlation tests
- SQS correlation tests
- Saga correlation tests
- MDC cleanup tests
- virtual-thread propagation tests
- secret-redaction tests
- personal-data leakage tests
- log-injection tests
- field-length tests
- exception-format tests
- duplicate-logging review
- collector failure tests
- asynchronous queue tests
- log-volume load tests
- backend mapping validation
- retention review
- access-control review
- production-readiness review

---

# 199. Success Criteria

The decision is successful when:

- every production application emits valid structured JSON
- logs can be searched through stable fields
- logs correlate with OpenTelemetry traces
- HTTP and SQS operations preserve correlation
- long-running Sagas can be investigated across traces
- sensitive values are absent from logs
- log injection is prevented
- duplicate exception logging is minimized
- production log volume remains within budget
- logging does not materially degrade business latency
- collector failure does not stop application processing
- dashboards and alerts use stable structured fields
- production access is controlled and auditable
- applications remain independent from the selected log backend
- operational logs remain clearly separated from business audit records

---

# 200. Alternatives Rejected

## 200.1 Plain-Text Production Logging

Rejected because it requires fragile parsing and provides weak distributed correlation.

---

## 200.2 Proprietary Backend SDKs

Rejected because they create vendor lock-in and application-level backend coupling.

---

## 200.3 Full Payload Logging

Rejected because it creates unacceptable security, privacy, performance and cost risks.

---

## 200.4 Logs as the Primary Metrics System

Rejected because log-derived metrics are expensive and less reliable for high-frequency operational monitoring.

---

## 200.5 Logs as the Business Audit Trail

Rejected because operational logs do not provide the required immutability, retention and business semantics.

---

## 200.6 Uncontrolled MDC Usage

Rejected because stale MDC data can leak between operations and create misleading diagnostics.

---

# 201. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-017: Adopt Optimistic Locking for Concurrent Aggregate Updates
- ADR-018: Version Integration Event Contracts
- ADR-020: Define Service-Level Objectives

---

# 202. References

- SLF4J Documentation
- Logback Documentation
- OpenTelemetry Logging Specification
- OpenTelemetry Trace Context
- Spring Boot Logging Documentation
- Kubernetes Logging Architecture
- Grafana Loki Documentation
- Elasticsearch Documentation
- OpenSearch Documentation
- OWASP Logging Cheat Sheet
- OWASP Application Logging Vocabulary
- Brazilian General Data Protection Law
- AstraForge Supply Platform Logging Standards
- AstraForge Supply Platform Security Guidelines
- AstraForge Supply Platform Observability Architecture
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience

---

# 203. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | AstraForge Supply Platform Architecture Team | Approved | Initial structured logging architecture baseline |

---

# 204. Decision Summary

The AstraForge Supply Platform adopts structured JSON logging as the standard production logging model.

The platform standardizes on:

```text
SLF4J

+

Structured JSON Encoder

+

OpenTelemetry Trace Correlation

+

Controlled MDC

+

stdout / stderr

+

Centralized Platform Collection
```

Every production log must be:

```text
Machine readable

Human understandable

Secure

Correlated

Bounded

Backend independent
```

The platform requires correlation across:

```text
HTTP requests

SQS messages/events

Transactional Outbox

Saga workflows

Scheduled jobs

External dependencies

Virtual-thread execution
```

Logs must not expose:

```text
Credentials

Tokens

Cookies

Personal payloads

Full request bodies

Full response bodies

Full event payloads
```

Operational logs remain distinct from:

```text
Metrics

Distributed traces

Security audit records

Business audit records
```

This decision establishes a consistent, secure and scalable logging foundation for diagnosing and operating the AstraForge Supply Platform in distributed production environments.
