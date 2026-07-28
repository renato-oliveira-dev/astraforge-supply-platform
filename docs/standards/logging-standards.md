# Logging Standards

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Logging Standards |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the logging standards adopted by the Enterprise Order Platform.

The objectives are:

- improve production troubleshooting
- support distributed tracing
- reduce Mean Time To Recovery (MTTR)
- provide operational visibility
- protect sensitive information
- maintain consistent logging across all services

Logs are operational assets and must be treated as structured, searchable data rather than free-form text.

---

# 2. Core Principles

Every log entry should be:

- structured
- contextual
- deterministic
- searchable
- concise
- actionable

A log should answer:

- What happened?
- Where did it happen?
- Why did it happen?
- Which business operation was affected?
- Which dependency was involved?
- Can the issue be correlated with other services?

---

# 3. Logging Goals

Logs must support:

- troubleshooting
- incident response
- audit support
- operational dashboards
- distributed tracing
- performance analysis
- security investigation

Logs are **not** intended for business reporting or analytics.

---

# 4. Logging Framework

The platform standard is:

- SLF4J API
- Logback (default Spring Boot implementation)

Application code must depend only on the SLF4J API.

Avoid direct dependencies on logging implementations.

---

# 5. Structured Logging

Logs should be structured using key-value pairs.

Preferred:

```java
log.info(
    "event=order_created orderId={} customerId={} elapsedMs={}",
    orderId,
    customerId,
    elapsedMillis
);
```

Avoid:

```java
log.info("Created order " + orderId);
```

Structured fields improve indexing and querying.

---

# 6. Mandatory Context Fields

Every business or integration log should include, when applicable:

| Field | Description |
|---|---|
| event | Event name |
| operation | Business operation |
| traceId | Distributed trace identifier |
| correlationId | Business/request correlation |
| requestId | Incoming request identifier |
| elapsedMs | Execution time |
| outcome | SUCCESS / FAILURE |
| dependency | External system |
| environment | Runtime environment |

---

# 7. Event Naming

Event names use lowercase snake case.

Examples:

```text
order_created

inventory_reserved

payment_authorized

external_call_failed

outbox_event_dispatched
```

---

# 8. Business vs Technical Logs

Business logs describe domain events.

Examples:

```text
order_created

approval_completed

inventory_reserved
```

Technical logs describe infrastructure behavior.

Examples:

```text
database_connection_failed

kafka_publish_timeout

redis_cache_miss
```

Do not mix business and infrastructure information in the same log entry.

---

# 9. Log Levels

Use log levels consistently.

| Level | Purpose |
|---|---|
| TRACE | Detailed execution flow |
| DEBUG | Development diagnostics |
| INFO | Normal business milestones |
| WARN | Recoverable degradation |
| ERROR | Operation failed |

Avoid using ERROR for expected business validation failures.

---

# 10. TRACE

TRACE is reserved for:

- internal algorithm flow
- parser details
- protocol exchanges
- temporary diagnostics

TRACE should normally be disabled in production.

---

# 11. DEBUG

Use DEBUG for information useful during development.

Examples:

- generated SQL identifiers
- cache decisions
- retry attempts
- mapping diagnostics

Avoid excessive DEBUG logging inside tight loops.

---

# 12. INFO

INFO represents meaningful business or operational milestones.

Examples:

```text
application_started

order_created

inventory_reserved

payment_completed

scheduler_started
```

INFO should not log every internal method invocation.

---

# 13. WARN

WARN indicates abnormal but recoverable situations.

Examples:

```text
cache_unavailable

retry_attempt

fallback_used

dependency_slow

duplicate_request
```

The application continues operating.

---

# 14. ERROR

ERROR indicates an operation failed.

Examples:

```text
database_unavailable

payment_timeout

unexpected_failure

message_processing_failed
```

Every ERROR should provide sufficient diagnostic context.

---

# 15. Logging Ownership

Each failure should normally be logged once.

Recommended ownership:

| Layer | Responsibility |
|---|---|
| Domain | No infrastructure logging |
| Application | Business recovery or audit only |
| Infrastructure | Dependency-specific diagnostics |
| HTTP Boundary | Unexpected request failures |
| Scheduler | Unhandled scheduled failures |
| Messaging | Final processing failures |

Avoid duplicate logs across layers.

---

# 16. Exception Logging

Always pass the exception object as the final logging argument.

Correct:

```java
log.error(
    "event=database_failure operation=save_order",
    exception
);
```

Avoid logging only:

```java
exception.getMessage()
```

The stack trace is often essential for diagnosis.

---

# 17. Stack Trace Policy

Include stack traces for:

- unexpected failures
- infrastructure failures
- programming defects

Avoid stack traces for:

- validation errors
- resource not found
- business rule violations
- authentication failures

Excessive stack traces increase noise and storage costs.

---

# 18. Correlation IDs

Every request should carry:

```text
traceId

correlationId

requestId
```

These identifiers must be propagated across:

- REST
- Amazon SQS
- SQS
- scheduled jobs
- asynchronous execution

---

# 19. MDC Usage

Use SLF4J MDC (Mapped Diagnostic Context) for request-scoped values.

Example:

```java
MDC.put("traceId", traceId);
MDC.put("correlationId", correlationId);
```

Clear the MDC after request completion to prevent context leakage.

---

# 20. Elapsed Time

Operations involving I/O should log execution time.

Example:

```java
log.info(
    "event=inventory_lookup dependency=inventory-service elapsedMs={}",
    elapsedMillis
);
```

Use a consistent field name:

```text
elapsedMs
```

---

# 21. External Integrations

Outbound integrations should log:

- dependency
- operation
- duration
- status
- retry count (if applicable)

Never log full request or response payloads by default.

---

# 22. Sensitive Data

Never log:

- passwords
- bearer tokens
- API keys
- JWTs
- refresh tokens
- credit card numbers
- CVVs
- session identifiers
- secrets

Sensitive values must be masked before logging.

---

# 23. Personal Data

Avoid logging personal information.

Instead of:

```text
customerEmail=john@example.com
```

Prefer:

```text
customerId=...
```

If email logging is required for diagnosis, mask it.

Example:

```text
jo***@example.com
```

---

# 24. Payload Logging

Full payload logging should be disabled by default.

Enable only:

- in controlled development environments
- temporarily during incident investigation
- after sanitization

Large payloads should be truncated.

---

# 25. SQL Logging

Do not enable SQL statement logging in production.

If SQL diagnostics are required:

- enable temporarily
- restrict to affected services
- avoid parameter logging containing sensitive data

---

# 26. SQS Messaging Logging

Producer logs should include:

```text
queue

eventType

eventId

messageGroupId (FIFO only, when safe)

elapsedMs
```

Consumer logs should include:

```text
queue

eventType

eventId

receiveCount (when useful)

processingOutcome

elapsedMs
```

---

# 27. Outbox Logging

Outbox dispatch logs should include:

- eventId
- destination
- attempt
- elapsedMs
- outcome

Example:

```text
event=outbox_dispatch_success
```

---

# 28. Retry Logging

Log retries at WARN level.

Include:

- dependency
- attempt
- maximum attempts
- delay
- failure classification

Avoid logging every retry as ERROR.

---

# 29. Performance Logging

Long-running operations should be logged.

Recommended thresholds:

| Operation | Threshold |
|---|---:|
| REST call | >500 ms |
| Database query | >200 ms |
| SQS SendMessage | >200 ms |
| Scheduled task | configurable |

Thresholds should be configurable.

---

# 30. Log Injection Prevention

User-controlled input must never be written directly to logs.

Sanitize:

- CR
- LF
- tabs
- ANSI escape sequences

Prevent log forging attacks.

---

# 31. High Cardinality

Avoid high-cardinality values in structured log fields intended for aggregation.

Examples:

- raw payloads
- stack traces as fields
- huge JSON blobs

Unique identifiers are acceptable in logs but not in metrics.

---

# 32. Security Events

Security logs should record:

- authentication success/failure
- authorization denial
- token expiration
- suspicious activity
- account lockout

Do not log credentials.

---

# 33. Audit vs Logs

Audit records are not operational logs.

Audit events must be:

- immutable
- business-focused
- retained according to compliance rules

Operational logs may be rotated or sampled.

---

# 34. Sampling

Very high-volume informational logs may be sampled when operationally justified.

ERROR logs must never be sampled without explicit approval.

---

# 35. Log Rotation

Production logging should support:

- rotation
- compression
- retention policies

Retention depends on compliance and operational requirements.

---

# 36. Testing Logging

Important logging behavior should be verified in tests when it represents contractual behavior.

Examples:

- fallback activation
- retry exhaustion
- security event emission

Do not over-test every log message.

---

# 37. Review Checklist

Before introducing a new log entry, verify:

- Is the level correct?
- Does it include sufficient context?
- Does it avoid sensitive data?
- Is it structured?
- Is it actionable?
- Could it generate excessive volume?
- Is it duplicated elsewhere?
- Is the event name consistent?
- Does it support correlation?

---

# 38. Anti-Patterns

The following are prohibited:

- logging passwords
- logging tokens
- string concatenation in log messages
- logging inside tight loops without need
- duplicate exception logging
- logging and swallowing exceptions
- logging every getter/setter invocation
- printing stack traces directly
- `System.out.println`
- `exception.printStackTrace()`

---

# 39. Architecture Rules

Logging must:

- use structured messages
- preserve correlation identifiers
- protect sensitive information
- distinguish business and technical events
- avoid duplicate logging
- support observability
- minimize operational noise
- provide deterministic diagnostics

---

# 40. Decision Summary

The platform adopts:

- SLF4J as the logging API
- structured key-value logging
- single-owner exception logging
- mandatory correlation identifiers
- sensitive-data masking
- business-oriented event names
- explicit log-level policy
- MDC-based request context propagation
- integration performance logging
- log injection prevention
- separation between operational logs and audit events
