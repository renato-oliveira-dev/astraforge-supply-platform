# Exception Handling

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Exception Handling |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the exception-handling standards adopted by the Enterprise Order Platform.

It establishes rules for:

- exception classification
- exception hierarchy
- domain failures
- application failures
- infrastructure failures
- exception translation
- API error responses
- integration failures
- retryability
- logging ownership
- sensitive information protection
- root-cause preservation
- operational diagnostics

The objective is to make failures predictable, observable, secure and consistent across all platform modules.

---

# 2. Core Principles

Exception handling must follow these principles:

- exceptions represent exceptional conditions
- business failures must be explicit
- technical failures must not leak across architectural boundaries
- original causes must be preserved
- errors must be logged only at the appropriate ownership boundary
- external responses must not expose sensitive implementation details
- retry decisions must be explicit
- error codes must remain stable
- failures must preserve enough context for diagnosis

Exceptions must not be used as a substitute for normal control flow.

---

# 3. Exception Categories

The platform classifies exceptions into four primary categories:

| Category | Purpose |
|---|---|
| Domain | Business invariant or domain rule violation |
| Application | Use-case orchestration or application contract failure |
| Infrastructure | Technical dependency, persistence or communication failure |
| Interface | Invalid or unsupported inbound request |

Each category has distinct responsibilities and translation rules.

---

# 4. Domain Exceptions

Domain exceptions represent violations of business rules or aggregate invariants.

Examples:

```text
InvalidOrderTransitionException

OrderCannotBeCancelledException

CustomerCreditExceededException

InventoryReservationNotAllowedException

ApprovalAuthorityExceededException
```

Domain exceptions must:

- use business terminology
- remain framework-independent
- not contain HTTP status codes
- not depend on Spring
- not depend on persistence or messaging technologies
- describe the violated business rule

---

# 5. Domain Exception Example

```java
package com.enterprise.orderplatform.domain.order.exception;

import com.enterprise.orderplatform.domain.order.valueobject.OrderId;
import com.enterprise.orderplatform.domain.order.valueobject.OrderStatus;

public final class InvalidOrderTransitionException
        extends DomainException {

    private final OrderId orderId;
    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;

    public InvalidOrderTransitionException(
            OrderId orderId,
            OrderStatus currentStatus,
            OrderStatus targetStatus
    ) {
        super(
                "Order %s cannot transition from %s to %s"
                        .formatted(orderId, currentStatus, targetStatus)
        );

        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public OrderId orderId() {
        return orderId;
    }

    public OrderStatus currentStatus() {
        return currentStatus;
    }

    public OrderStatus targetStatus() {
        return targetStatus;
    }
}
```

The exception exposes structured business context without depending on transport concerns.

---

# 6. Base Domain Exception

A base domain exception may be used to identify domain-level failures.

Example:

```java
package com.enterprise.orderplatform.domain.shared.exception;

public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
```

The base class must remain lightweight.

It must not include:

- HTTP status
- logging behavior
- localization logic
- framework annotations
- serialization concerns

---

# 7. Application Exceptions

Application exceptions represent failures related to use-case orchestration or application-level contracts.

Examples:

```text
OrderNotFoundException

CustomerNotFoundException

DuplicateRequestException

UseCaseAuthorizationException

ConcurrentOrderModificationException

ApplicationDependencyException
```

Application exceptions may represent:

- requested resource absence
- failed orchestration
- use-case precondition failure
- duplicate command detection
- application concurrency conflict
- invalid application request context

---

# 8. Resource Not Found Exceptions

Not-found exceptions should identify the missing resource and its identifier.

Example:

```java
package com.enterprise.orderplatform.application.order.exception;

import com.enterprise.orderplatform.domain.order.valueobject.OrderId;

public final class OrderNotFoundException
        extends ApplicationException {

    private final OrderId orderId;

    public OrderNotFoundException(OrderId orderId) {
        super("Order %s was not found".formatted(orderId));
        this.orderId = orderId;
    }

    public OrderId orderId() {
        return orderId;
    }
}
```

Do not throw generic exceptions such as:

```java
new RuntimeException("Not found");
```

---

# 9. Base Application Exception

```java
package com.enterprise.orderplatform.application.shared.exception;

public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
```

Application exceptions should not directly depend on HTTP infrastructure.

---

# 10. Infrastructure Exceptions

Infrastructure exceptions represent failures in technical adapters.

Examples:

```text
OrderPersistenceException

KafkaPublicationException

RedisAccessException

InventoryClientException

PaymentProviderUnavailableException

OutboxDispatchException
```

Infrastructure exceptions may represent:

- database access failure
- network communication failure
- serialization failure
- cache access failure
- broker publication failure
- external provider failure
- file system failure

---

# 11. Infrastructure Exception Example

```java
package com.enterprise.orderplatform.infrastructure.persistence.exception;

public final class OrderPersistenceException
        extends InfrastructureException {

    public OrderPersistenceException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
```

Base class:

```java
package com.enterprise.orderplatform.infrastructure.shared.exception;

public abstract class InfrastructureException
        extends RuntimeException {

    protected InfrastructureException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
```

Infrastructure exceptions must preserve the original cause.

---

# 12. Interface Exceptions

Interface exceptions represent invalid inbound communication.

Examples:

```text
MalformedRequestException

UnsupportedMediaTypeException

InvalidHeaderException

MissingIdempotencyKeyException
```

Most structural interface errors should be handled directly by framework validation.

Custom interface exceptions should be introduced only when framework exceptions do not sufficiently express the contract violation.

---

# 13. Exception Translation

Exceptions must be translated when crossing architectural boundaries.

Recommended flow:

```text
Technology-specific exception

↓

Infrastructure adapter exception

↓

Application-level interpretation

↓

HTTP or messaging error representation
```

Example:

```text
DataIntegrityViolationException

↓

OrderPersistenceException

↓

DuplicateOrderReferenceException

↓

HTTP 409 Problem Detail
```

Each translation should add relevant context without discarding the original cause.

---

# 14. Persistence Exception Translation

Persistence adapters must not expose JPA, Hibernate or JDBC exceptions to the application layer.

Example:

```java
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    public JpaOrderRepository(
            SpringDataOrderRepository repository,
            OrderPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(Order order) {
        try {
            repository.save(mapper.toEntity(order));
        } catch (DataIntegrityViolationException exception) {
            throw new OrderPersistenceException(
                    "Unable to persist order %s"
                            .formatted(order.id()),
                    exception
            );
        }
    }
}
```

The adapter may translate specific database failures into more precise exceptions when the underlying constraint is known.

---

# 15. Constraint Violation Translation

Database constraint violations should be translated into business-relevant failures when possible.

Example:

```java
catch (DataIntegrityViolationException exception) {
    if (constraintNameResolver.isDuplicateExternalReference(exception)) {
        throw new DuplicateOrderReferenceException(
                order.externalReference(),
                exception
        );
    }

    throw new OrderPersistenceException(
            "Unable to persist order %s".formatted(order.id()),
            exception
    );
}
```

Constraint-name interpretation belongs in the infrastructure layer.

---

# 16. External Integration Exception Translation

External client adapters must translate client-library exceptions.

Example:

```java
@Component
public class RestInventoryGateway implements InventoryGateway {

    private final RestClient restClient;

    public RestInventoryGateway(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public InventoryAvailability findAvailability(ProductId productId) {
        try {
            InventoryResponse response = restClient.get()
                    .uri("/inventory/{productId}", productId.value())
                    .retrieve()
                    .body(InventoryResponse.class);

            return InventoryResponseMapper.toDomain(response);
        } catch (RestClientResponseException exception) {
            throw translateResponseException(productId, exception);
        } catch (RestClientException exception) {
            throw new InventoryServiceUnavailableException(
                    productId,
                    exception
            );
        }
    }
}
```

Raw HTTP client exceptions must not leave the adapter.

---

# 17. Remote Status Classification

Remote HTTP failures should be classified according to their semantics.

| HTTP status | Classification |
|---|---|
| 400 | Invalid request sent to dependency |
| 401 | Authentication or credential failure |
| 403 | Authorization failure |
| 404 | Remote resource not found |
| 409 | Remote state conflict |
| 422 | Remote business rejection |
| 429 | Rate limit or temporary capacity failure |
| 500 | Remote unexpected failure |
| 502 | Gateway or temporary dependency failure |
| 503 | Temporary dependency unavailability |
| 504 | Dependency timeout |

Classification must support consistent:

- retry behavior
- logging severity
- API translation
- metrics
- alerting

---

# 18. Retryable Exceptions

Retryability must be explicit.

A retryable exception indicates that the same operation may succeed later without changing the request.

Examples:

```text
ExternalServiceUnavailableException

ExternalServiceTimeoutException

RateLimitExceededException

TransientDatabaseException

BrokerUnavailableException
```

Retryable exceptions should implement a marker contract only when that improves policy enforcement.

Example:

```java
public interface RetryableFailure {
}
```

```java
public final class InventoryServiceTimeoutException
        extends InfrastructureException
        implements RetryableFailure {
}
```

Do not infer retryability only from exception names.

---

# 19. Non-Retryable Exceptions

Non-retryable failures include:

- invalid input
- business rule rejection
- authentication failure
- authorization failure
- unsupported operation
- resource not found
- malformed response contract
- permanent configuration error

Examples:

```text
InvalidOrderTransitionException

CustomerCreditExceededException

ExternalAuthenticationException

UnsupportedOrderTypeException
```

Retrying these failures without changing the input or configuration is prohibited.

---

# 20. Retry Policy Ownership

Retry behavior belongs in infrastructure resilience policies.

Business code should not contain ad hoc retry loops.

Avoid:

```java
for (int attempt = 0; attempt < 3; attempt++) {
    try {
        paymentGateway.authorize(payment);
        break;
    } catch (Exception exception) {
        // retry
    }
}
```

Prefer declarative or centralized resilience configuration.

Retry policy must define:

- retryable failures
- maximum attempts
- delay
- backoff
- jitter
- operation idempotency
- timeout budget

---

# 21. Idempotency Before Retry

Retries are allowed only when the operation is safe to repeat.

Safe examples:

- read-only GET requests
- idempotent PUT requests
- commands protected by an idempotency key
- event publication using a stable event identifier

Unsafe examples:

- payment authorization without idempotency protection
- duplicate order creation
- non-idempotent external command execution

Retry policy must consider business side effects.

---

# 22. Timeout Exceptions

Timeouts must use explicit exception types.

Examples:

```text
InventoryServiceTimeoutException

PaymentAuthorizationTimeoutException

DatabaseQueryTimeoutException

OutboxDispatchTimeoutException
```

Avoid representing all timeout failures as a generic unavailable dependency.

A timeout provides important operational information.

---

# 23. Circuit Breaker Exceptions

Circuit breaker open-state failures should be distinguishable from direct dependency failures.

Example:

```java
public final class ExternalServiceCircuitOpenException
        extends InfrastructureException
        implements RetryableFailure {

    private final String dependency;

    public ExternalServiceCircuitOpenException(
            String dependency,
            Throwable cause
    ) {
        super(
                "Circuit breaker is open for dependency %s"
                        .formatted(dependency),
                cause
        );

        this.dependency = dependency;
    }

    public String dependency() {
        return dependency;
    }
}
```

This distinction supports accurate alerting and metrics.

---

# 24. Bulkhead Rejection

Capacity rejection should use an explicit exception.

Example:

```text
ExternalCallCapacityExceededException
```

This failure may map internally to:

- HTTP 503
- asynchronous retry
- backpressure
- degraded behavior

It must not be misclassified as a business rejection.

---

# 25. Exception Messages

Internal exception messages should be:

- clear
- concise
- diagnostic
- free from secrets
- contextual
- written in English

Good:

```text
Unable to reserve inventory for order 4fa2... because the inventory service timed out
```

Avoid:

```text
Error occurred

Something went wrong

Failed

Exception in service
```

---

# 26. Structured Exception Context

When diagnostic context is needed programmatically, expose typed fields.

Example:

```java
public final class PaymentAuthorizationException
        extends ApplicationException {

    private final PaymentId paymentId;
    private final PaymentFailureReason reason;

    public PaymentAuthorizationException(
            PaymentId paymentId,
            PaymentFailureReason reason,
            Throwable cause
    ) {
        super(
                "Payment %s could not be authorized: %s"
                        .formatted(paymentId, reason),
                cause
        );

        this.paymentId = paymentId;
        this.reason = reason;
    }

    public PaymentId paymentId() {
        return paymentId;
    }

    public PaymentFailureReason reason() {
        return reason;
    }
}
```

Do not parse exception messages to recover structured information.

---

# 27. Sensitive Information

Exception messages must not contain:

- passwords
- access tokens
- refresh tokens
- API keys
- authorization headers
- session identifiers
- full credit card numbers
- security answers
- private encryption material
- complete sensitive payloads

Sensitive external error payloads must be sanitized before storage or logging.

---

# 28. Personal Data

Personal data should be minimized in exceptions.

Avoid including:

- complete email addresses
- phone numbers
- tax identifiers
- complete addresses
- customer document numbers

When necessary for diagnostics, use:

- internal identifiers
- masked values
- correlation identifiers

---

# 29. External Error Sanitization

External response bodies must be sanitized before being included in exceptions or logs.

Sanitization should remove or mask:

- bearer tokens
- passwords
- secrets
- authorization headers
- cookies
- card numbers
- private customer data
- control characters
- excessive payload length

The original unsanitized response must not be propagated.

---

# 30. Maximum Error Message Length

Messages captured from external dependencies should have a maximum configured length.

Example:

```java
private static final int MAX_EXTERNAL_MESSAGE_LENGTH = 500;
```

Long payloads must be safely truncated.

The truncation marker should be explicit:

```text
...
```

Sanitization must occur before truncation when sensitive data may be present.

---

# 31. Cause Preservation

When translating exceptions, always preserve the original cause.

Incorrect:

```java
catch (SQLException exception) {
    throw new OrderPersistenceException(
            "Unable to persist order"
    );
}
```

Correct:

```java
catch (SQLException exception) {
    throw new OrderPersistenceException(
            "Unable to persist order",
            exception
    );
}
```

Cause preservation is required for:

- stack-trace analysis
- root-cause diagnosis
- observability
- incident investigation

---

# 32. Exception Chaining

Do not create unnecessarily deep exception chains.

Each layer should translate only when it adds architectural or semantic value.

Avoid translating:

```text
Exception A

↓

Exception B

↓

Exception C

↓

Exception D
```

when all intermediate exceptions communicate the same meaning.

---

# 33. Catch Scope

Catch the narrowest exception type that can be handled meaningfully.

Avoid:

```java
catch (Exception exception) {
}
```

Broad catches may be acceptable only at outer execution boundaries such as:

- scheduler entry points
- message listener boundaries
- thread execution boundaries
- global HTTP handlers

Even at these boundaries, the exception must be handled explicitly.

---

# 34. Never Catch Throwable

Application code must not catch `Throwable`.

Catching `Throwable` may intercept serious JVM errors such as:

- `OutOfMemoryError`
- `StackOverflowError`
- linkage errors

These failures generally cannot be handled safely by application logic.

---

# 35. InterruptedException

`InterruptedException` must preserve the thread interruption status.

Correct:

```java
catch (InterruptedException exception) {
    Thread.currentThread().interrupt();

    throw new OrderProcessingInterruptedException(
            "Order processing was interrupted",
            exception
    );
}
```

Never swallow an interruption.

---

# 36. CompletionException

Asynchronous wrapper exceptions must be unwrapped when necessary.

Example:

```java
catch (CompletionException exception) {
    Throwable cause = exception.getCause();

    if (cause instanceof ApplicationException applicationException) {
        throw applicationException;
    }

    throw new ParallelValidationException(
            "Parallel order validation failed",
            cause
    );
}
```

Avoid exposing `CompletionException` as the meaningful business failure.

---

# 37. ExecutionException

When using `Future`, unwrap `ExecutionException` and preserve the underlying failure.

Example:

```java
private static RuntimeException translateExecutionFailure(
        ExecutionException exception
) {
    Throwable cause = exception.getCause();

    if (cause instanceof RuntimeException runtimeException) {
        return runtimeException;
    }

    return new ParallelExecutionException(
            "Parallel operation failed",
            cause
    );
}
```

---

# 38. Virtual Thread Failures

Virtual threads do not change exception semantics.

Every submitted task must have:

- explicit failure handling
- timeout handling
- cancellation handling
- context propagation
- resource bounds

Failures from parallel tasks must not be silently discarded.

---

# 39. Aggregating Parallel Failures

When multiple independent operations fail, preserve meaningful context.

Possible strategies:

- fail fast on the first critical failure
- collect validation failures
- aggregate dependency failures
- return a partial result only when contractually supported

Validation failures may be grouped into a structured result.

Technical failures should normally preserve the first root cause and record related failures as suppressed exceptions when useful.

---

# 40. Suppressed Exceptions

Suppressed exceptions may be used when multiple cleanup or parallel failures occur.

Example:

```java
primaryException.addSuppressed(secondaryException);
```

Do not use suppressed exceptions as a replacement for a clear aggregate error model.

---

# 41. Logging Ownership

Every failure should normally be logged once.

Recommended ownership:

| Layer | Logging responsibility |
|---|---|
| Domain | No infrastructure logging |
| Application | Log only when it owns recovery or business audit |
| Infrastructure adapter | Log when adding dependency-specific diagnostic context |
| HTTP boundary | Log unexpected failures |
| Message boundary | Log final processing failure |
| Scheduler boundary | Log uncaught scheduled execution failure |

Avoid logging the same exception at every layer.

---

# 42. Log or Rethrow Rule

A caught exception must be:

- handled
- translated and rethrown
- logged and resolved
- or logged and rethrown only when the current boundary owns unique diagnostic context

Never catch and ignore an exception.

Incorrect:

```java
catch (Exception exception) {
    return Optional.empty();
}
```

Correct translation:

```java
catch (RemoteClientException exception) {
    throw new CustomerDirectoryException(
            "Unable to retrieve customer %s"
                    .formatted(customerId),
            exception
    );
}
```

Correct recovery:

```java
catch (RedisConnectionFailureException exception) {
    log.warn(
            "event=cache_read_failed cache={} key={} fallback=in_memory",
            CACHE_NAME,
            customerId,
            exception
    );

    return fallbackCache.get(customerId);
}
```

---

# 43. Logging and Rethrowing

Logging and rethrowing the same exception is allowed only when the current layer adds unique operational information.

Example:

```java
catch (RestClientException exception) {
    log.warn(
            "event=external_call_failed dependency={} operation={} elapsedMs={}",
            DEPENDENCY_NAME,
            OPERATION_NAME,
            elapsedMillis,
            exception
    );

    throw new InventoryServiceUnavailableException(
            productId,
            exception
    );
}
```

The outer global handler should avoid logging the same expected infrastructure failure again at the same severity.

---

# 44. Log Levels

Recommended levels:

| Level | Use |
|---|---|
| `TRACE` | Fine-grained diagnostic flow |
| `DEBUG` | Development and troubleshooting context |
| `INFO` | Business milestones and normal operational state |
| `WARN` | Recoverable degradation or expected abnormal condition |
| `ERROR` | Failed operation requiring investigation |

Business validation failures should not automatically be logged as errors.

Examples:

- invalid order transition: normally `WARN` or no log
- dependency timeout after retries: `ERROR`
- fallback cache used successfully: `WARN`
- resource not found: usually no error log
- unexpected null state: `ERROR`

---

# 45. Stack Traces

Stack traces should be included for unexpected or technical failures.

Expected business exceptions generally do not require full stack traces at error level.

Avoid producing large volumes of stack traces for:

- validation failures
- resource-not-found responses
- authorization rejections
- known business conflicts

Logging strategy must consider operational volume.

---

# 46. Correlation Context

Error logs should include:

```text
traceId

correlationId

requestId

operation

dependency

outcome

elapsedMs
```

Example:

```java
log.error(
        "event=order_approval_failed orderId={} operation={} "
                + "dependency={} elapsedMs={} traceId={}",
        orderId,
        OPERATION_APPROVE_ORDER,
        dependency,
        elapsedMillis,
        traceId,
        exception
);
```

Do not include identifiers as metric tags when cardinality is unbounded.

---

# 47. Global HTTP Exception Handling

Use `@RestControllerAdvice` to translate exceptions into Problem Details.

The handler must:

- map known exceptions explicitly
- return stable error codes
- avoid exposing stack traces
- include correlation information
- sanitize messages
- distinguish business and technical failures
- provide consistent validation responses

---

# 48. Error Response Standard

The API error model follows Problem Details semantics.

Example:

```json
{
  "type": "https://enterprise.example/problems/invalid-order-transition",
  "title": "Invalid order transition",
  "status": 409,
  "detail": "The order cannot transition from APPROVED to CREATED.",
  "instance": "/api/v1/orders/6dc7.../status",
  "code": "INVALID_ORDER_TRANSITION",
  "traceId": "f64c7f8ac1304c42",
  "timestamp": "2026-07-22T21:30:00Z"
}
```

---

# 49. Stable Error Codes

Application error codes use uppercase snake case.

Examples:

```text
ORDER_NOT_FOUND

INVALID_ORDER_TRANSITION

ORDER_ALREADY_APPROVED

CUSTOMER_CREDIT_EXCEEDED

INVENTORY_UNAVAILABLE

PAYMENT_AUTHORIZATION_FAILED

DEPENDENCY_UNAVAILABLE

REQUEST_VALIDATION_FAILED

CONCURRENT_MODIFICATION
```

Error codes are public contracts.

They must not change without compatibility analysis.

---

# 50. Error Code Ownership

Each bounded context owns its error codes.

Error codes should:

- be unique
- express business or application meaning
- avoid framework terminology
- avoid database terminology
- avoid internal class names

Incorrect:

```text
DATA_INTEGRITY_VIOLATION

NULL_POINTER_EXCEPTION

HIBERNATE_ERROR

RESTCLIENT_EXCEPTION
```

Correct:

```text
DUPLICATE_ORDER_REFERENCE

INVALID_REQUEST

ORDER_PERSISTENCE_FAILURE
```

---

# 51. HTTP Mapping

Recommended exception-to-status mapping:

| Exception type | HTTP status |
|---|---|
| Validation failure | 400 |
| Authentication failure | 401 |
| Authorization failure | 403 |
| Resource not found | 404 |
| State conflict | 409 |
| Business semantic rejection | 422 |
| Rate limit | 429 |
| Unexpected internal failure | 500 |
| Temporary dependency failure | 503 |
| Dependency timeout | 504 |

The mapping must reflect contract semantics, not exception inheritance alone.

---

# 52. 400 Versus 422

Use `400 Bad Request` when:

- JSON is malformed
- required fields are missing
- field formats are invalid
- request structure is invalid

Use `422 Unprocessable Entity` when:

- the request is structurally valid
- the business meaning is invalid
- a business rule rejects the operation

Use `409 Conflict` when the request conflicts with current resource state.

---

# 53. Problem Type URIs

Problem type identifiers should be stable.

Recommended pattern:

```text
https://enterprise.example/problems/<problem-name>
```

Examples:

```text
https://enterprise.example/problems/order-not-found

https://enterprise.example/problems/invalid-order-transition

https://enterprise.example/problems/dependency-unavailable
```

The URI may later resolve to human-readable documentation.

---

# 54. Global Handler Example

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI ORDER_NOT_FOUND_TYPE =
            URI.create(
                    "https://enterprise.example/problems/order-not-found"
            );

    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ProblemDetail> handleOrderNotFound(
            OrderNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.NOT_FOUND
        );

        problem.setType(ORDER_NOT_FOUND_TYPE);
        problem.setTitle("Order not found");
        problem.setDetail(
                "The requested order could not be found."
        );
        problem.setInstance(
                URI.create(request.getRequestURI())
        );
        problem.setProperty("code", "ORDER_NOT_FOUND");
        problem.setProperty("traceId", currentTraceId());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }
}
```

External details may intentionally be less specific than internal exception messages.

---

# 55. Validation Error Response

Validation failures should return field-level details.

Example:

```json
{
  "type": "https://enterprise.example/problems/request-validation",
  "title": "Request validation failed",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "instance": "/api/v1/orders",
  "code": "REQUEST_VALIDATION_FAILED",
  "traceId": "f64c7f8ac1304c42",
  "errors": [
    {
      "field": "customerId",
      "code": "NotNull",
      "message": "customerId must not be null"
    },
    {
      "field": "items",
      "code": "NotEmpty",
      "message": "items must not be empty"
    }
  ]
}
```

---

# 56. Validation Field Model

A validation issue may use an immutable record.

```java
public record FieldValidationError(
        String field,
        String code,
        String message
) {
}
```

Do not expose rejected values when they may contain sensitive information.

---

# 57. Method Argument Validation

Handle:

```text
MethodArgumentNotValidException

ConstraintViolationException

HandlerMethodValidationException
```

using one consistent validation response contract.

Do not return separate incompatible structures for each framework exception.

---

# 58. Malformed JSON

Malformed JSON should return:

- HTTP 400
- stable error code
- generic safe message
- request trace identifier

Do not expose internal Jackson parser stack traces or class names.

Example code:

```text
MALFORMED_REQUEST_BODY
```

---

# 59. Unsupported Enum Values

Invalid external enum values should result in a clear validation failure.

Example:

```json
{
  "field": "status",
  "code": "INVALID_ENUM_VALUE",
  "message": "status must be one of: CREATED, APPROVED, CANCELLED"
}
```

Avoid exposing Java enum implementation details.

---

# 60. Authentication Errors

Authentication failures should return HTTP 401.

Example code:

```text
AUTHENTICATION_REQUIRED

INVALID_ACCESS_TOKEN

ACCESS_TOKEN_EXPIRED
```

Do not reveal:

- token parsing internals
- key identifiers
- signature verification details
- security configuration

---

# 61. Authorization Errors

Authorization failures should return HTTP 403.

Example codes:

```text
ACCESS_DENIED

INSUFFICIENT_PERMISSION

ORDER_SCOPE_ACCESS_DENIED
```

Do not confirm the existence of a protected resource when doing so creates an information disclosure risk.

In some contexts, returning 404 may be more appropriate.

---

# 62. Unexpected Exceptions

Unexpected exceptions must be converted into a safe generic response.

Example:

```json
{
  "type": "https://enterprise.example/problems/internal-error",
  "title": "Internal server error",
  "status": 500,
  "detail": "An unexpected error occurred while processing the request.",
  "code": "INTERNAL_ERROR",
  "traceId": "f64c7f8ac1304c42"
}
```

The complete exception must remain available in internal logs.

---

# 63. Production Stack Traces

Production API responses must not contain:

- stack traces
- package names
- class names
- SQL statements
- database constraint details
- framework error pages
- internal hostnames
- file-system paths

Development environments may expose additional diagnostics only through controlled local configuration.

---

# 64. Message Localization

Public error messages may be localized.

Stable error codes must remain language-independent.

Recommended separation:

```text
code = ORDER_NOT_FOUND

message = localized human-readable text
```

Do not use localized messages as machine-readable identifiers.

---

# 65. Messaging Consumer Failures

Message consumers must classify failures as:

- successful
- retryable
- non-retryable
- duplicate
- malformed
- poison message

Processing behavior must be explicit for each category.

---

# 66. Retryable Message Failures

Retryable message failures may result in:

- SQS redelivery
- controlled delayed retry/reprocessing
- visibility timeout extension

Examples:

```text
temporary database outage

dependency timeout

broker connectivity failure

rate limit
```

Retry attempts must be bounded.

---

# 67. Non-Retryable Message Failures

Non-retryable failures should not loop indefinitely.

Examples:

```text
invalid event schema

unsupported event version

missing mandatory business key

permanent business rejection

malformed payload
```

These messages should be:

- rejected
- sent to a dead-letter destination
- recorded for investigation
- associated with the original event identifier

---

# 68. Dead-Letter Context

Dead-lettered messages should preserve:

```text
eventId

eventType

eventVersion

originalDestination

consumer

attemptCount

failureCode

failureTimestamp

traceId

correlationId
```

Sensitive payloads must still follow security and retention policies.

---

# 69. Poison Messages

A poison message is a message that repeatedly causes deterministic processing failure.

The consumer must prevent infinite redelivery.

Poison-message handling should include:

- bounded retries
- dead-letter routing
- alerting
- replay procedure
- failure classification
- audit metadata

---

# 70. SQS Consumer Exceptions

SQS consumer/listener methods should not contain complex exception logic.

Delegate classification to:

- error handlers
- recoverers
- SQS redrive/retry policies
- dead-letter publishers

Application exceptions should be translated into listener-processing outcomes at the adapter boundary.

---

# 71. Outbox Dispatch Failures

Outbox dispatch failures must retain:

- event identifier
- destination
- attempt count
- next attempt timestamp
- sanitized last error
- failure classification

The dispatcher should distinguish:

- transient broker failure
- serialization failure
- invalid destination
- exhausted retry attempts

Serialization failures are usually non-retryable without a code or data correction.

---

# 72. Scheduled Job Failures

Scheduled job boundaries must catch and report uncaught failures.

Example:

```java
@Scheduled(fixedDelayString = "${outbox.dispatch.interval}")
public void dispatch() {
    try {
        dispatchPendingEventsUseCase.execute();
    } catch (RuntimeException exception) {
        log.error(
                "event=outbox_dispatch_failed operation=dispatch_pending_events",
                exception
        );
    }
}
```

The scheduler must not silently stop due to an uncaught exception.

Where supported, centralized scheduler error handling is preferred.

---

# 73. Batch Processing Failures

Batch operations should distinguish:

- complete failure
- partial failure
- skipped item
- retried item
- permanently rejected item

A batch response should not report success when individual critical items failed.

Failure semantics must be documented.

---

# 74. Partial Failure Model

When partial success is supported, use an explicit result.

Example:

```java
public record BulkApprovalResult(
        int requested,
        int approved,
        int rejected,
        List<BulkApprovalFailure> failures
) {
}
```

Failure item:

```java
public record BulkApprovalFailure(
        UUID orderId,
        String code,
        String message
) {
}
```

Do not rely on exceptions to represent every expected per-item rejection in large batches.

---

# 75. Business Result Versus Exception

Use a result object when failure is an expected and frequent outcome.

Examples:

- payment declined
- item rejected during bulk validation
- eligibility evaluation
- search result absence
- optional cache miss

Use an exception when:

- an invariant is violated
- a requested resource is unexpectedly absent
- execution cannot continue
- a dependency fails
- an application contract is broken

---

# 76. Payment Rejection

A normal payment decline may be modeled as a domain result rather than an exception.

Example:

```java
public sealed interface PaymentAuthorizationResult
        permits PaymentApproved, PaymentDeclined {
}
```

Technical provider failures should still use exceptions.

Example:

```text
PaymentProviderUnavailableException
```

This distinction prevents expected business outcomes from being treated as system errors.

---

# 77. Fallback Behavior

A fallback is allowed only when it preserves valid business semantics.

Acceptable examples:

- cache miss falls back to database
- optional Redis outage falls back to bounded in-memory cache
- non-critical notification failure is queued for retry

Unacceptable examples:

- unavailable inventory service treated as available inventory
- failed payment treated as approved
- failed authorization treated as permitted
- missing customer data replaced with invented values

Fallback decisions must be documented.

---

# 78. Cache Failure Handling

Cache failures should normally not fail the business operation when the cache is explicitly optional.

Example:

```java
try {
    return redisCache.get(customerId);
} catch (RedisConnectionFailureException exception) {
    log.warn(
            "event=cache_read_failed cache={} key={} fallback=database",
            CUSTOMER_CACHE,
            customerId,
            exception
    );

    return customerRepository.findById(customerId);
}
```

Cache failure handling must not hide database or authoritative-source failures.

---

# 79. Serialization Failures

Serialization exceptions must identify:

- contract type
- event or operation identifier
- target destination
- serializer
- event version when applicable

Do not include the complete serialized payload in logs by default.

Example:

```text
OrderEventSerializationException
```

---

# 80. Deserialization Failures

Deserialization failures should be considered non-retryable unless the source payload may change without republishing.

Required handling:

- classify as malformed or unsupported
- preserve message metadata
- route to dead-letter handling
- record a sanitized error
- alert when volume exceeds threshold

---

# 81. Optimistic Locking

Optimistic locking conflicts should be translated into an application concurrency exception.

Example:

```text
ConcurrentOrderModificationException
```

Recommended HTTP status:

```text
409 Conflict
```

The response should instruct the client to refresh or retry only when the operation is safely repeatable.

---

# 82. Pessimistic Locking

Lock timeout failures should be distinguished from business conflicts.

Example:

```text
OrderLockTimeoutException
```

Depending on the operation, this may be:

- retryable
- mapped to 409
- mapped to 503
- processed asynchronously

The decision must be documented per use case.

---

# 83. Duplicate Requests

Idempotency conflicts should use explicit errors.

Examples:

```text
DuplicateRequestException

IdempotencyKeyConflictException

RequestAlreadyProcessedException
```

Differentiate:

- exact replay returning the previous result
- same key with a different payload
- duplicate business reference
- concurrent processing of the same key

---

# 84. Idempotency Key Conflict

When an idempotency key is reused with a different request payload:

- reject the request
- return HTTP 409
- use a stable error code
- do not process the new payload

Example code:

```text
IDEMPOTENCY_KEY_CONFLICT
```

---

# 85. Configuration Exceptions

Invalid mandatory configuration must fail application startup.

Examples:

```text
MissingConfigurationException

InvalidIntegrationConfigurationException

UnsupportedSecurityConfigurationException
```

Do not silently replace mandatory production configuration with permissive defaults.

---

# 86. Startup Failures

Startup failures should clearly identify:

- configuration prefix
- missing or invalid property
- affected subsystem
- safe expected format

Do not include secret values.

Good:

```text
Property integration.payment.base-url must be a valid HTTPS URI
```

Avoid:

```text
Payment configuration invalid: clientSecret=...
```

---

# 87. Feature Flag Failures

Feature flag provider failures require documented behavior.

Possible strategies:

- fail closed
- use last known value
- use safe default
- disable optional feature

Security-sensitive flags should generally fail closed.

Do not invent different fallback semantics across call sites.

---

# 88. Error Metrics

Metrics should distinguish failure categories.

Examples:

```text
orders.processing.failures

integration.requests.failures

outbox.dispatch.failures

messages.processing.failures
```

Recommended tags:

```text
operation

dependency

failure_type

retryable

outcome
```

Tag values must have controlled cardinality.

Do not use full exception messages as metric tags.

---

# 89. Failure Type Tag

Use a controlled failure classification.

Examples:

```text
validation

business_rule

not_found

conflict

timeout

unavailable

rate_limited

serialization

authentication

authorization

unexpected
```

Avoid using raw exception class names when they create unstable dashboards.

---

# 90. Alerting

Alerts should be based on operational impact, not individual exceptions alone.

Relevant signals:

- error-rate increase
- retry exhaustion
- dead-letter growth
- timeout increase
- circuit breaker open duration
- dependency failure rate
- unexpected exception volume
- business operation failure ratio

Expected validation errors should not trigger production incident alerts.

---

# 91. Audit Events

Business audit events and technical logs are different concerns.

Audit events may record:

- order approved
- order cancelled
- payment authorized
- role used for approval
- actor identifier
- timestamp
- business reason

Technical exceptions should not be used as the audit record.

Audit publication failures require their own reliability strategy.

---

# 92. Testing Exception Behavior

Tests must verify:

- exception type
- stable error code
- HTTP status
- preserved cause
- safe response detail
- retry classification
- logging behavior when significant
- no sensitive information exposure

---

# 93. Unit Test Example

```java
@Test
void testApproveShouldThrowExceptionWhenOrderDoesNotExist() {
    OrderId orderId = TestConstants.ORDER_ID;

    when(orderRepository.findById(orderId))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.approve(orderId))
            .as("should reject approval when the order does not exist")
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining(orderId.toString());
}
```

---

# 94. Cause Preservation Test

```java
@Test
void testSaveShouldPreserveOriginalCauseWhenPersistenceFails() {
    SQLException cause = new SQLException("database unavailable");

    when(jpaRepository.save(any(OrderJpaEntity.class)))
            .thenThrow(new DataAccessResourceFailureException(
                    "database unavailable",
                    cause
            ));

    assertThatThrownBy(() -> repository.save(TestFixtures.validOrder()))
            .as("should preserve the persistence failure cause")
            .isInstanceOf(OrderPersistenceException.class)
            .hasCauseInstanceOf(DataAccessResourceFailureException.class);
}
```

---

# 95. Controller Advice Test

```java
@Test
void testGetOrderShouldReturnNotFoundProblemDetail() throws Exception {
    when(getOrderUseCase.execute(TestConstants.ORDER_ID))
            .thenThrow(
                    new OrderNotFoundException(TestConstants.ORDER_ID)
            );

    mockMvc.perform(
                    get("/api/v1/orders/{orderId}", TestConstants.ORDER_ID)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.title").value("Order not found"))
            .andExpect(jsonPath("$.traceId").isNotEmpty());
}
```

Where AssertJ is used, every assertion chain must include `.as("...")`.

---

# 96. Sensitive Data Test

```java
@Test
void testSanitizeShouldRemoveBearerTokenFromRemoteError() {
    String remoteMessage =
            "Authorization: Bearer abc.def.ghi payment failed";

    String sanitized = sanitizer.sanitize(remoteMessage);

    assertThat(sanitized)
            .as("sanitized message should not expose bearer credentials")
            .doesNotContain("abc.def.ghi")
            .contains("Bearer ***");
}
```

---

# 97. Retry Classification Test

```java
@Test
void testTimeoutExceptionShouldBeRetryable() {
    RuntimeException exception =
            new InventoryServiceTimeoutException(
                    TestConstants.PRODUCT_ID,
                    new SocketTimeoutException("timeout")
            );

    assertThat(exception)
            .as("inventory timeout should be classified as retryable")
            .isInstanceOf(RetryableFailure.class);
}
```

---

# 98. Architecture Tests

Architecture tests should verify:

- domain exceptions do not depend on Spring
- domain exceptions do not reference HTTP status
- infrastructure exceptions remain in infrastructure packages
- controllers do not throw persistence exceptions directly
- global handlers exist only in interface packages
- exception classes follow naming conventions

Example rule:

```java
@ArchTest
static final ArchRule domainExceptionsMustNotDependOnSpring =
        noClasses()
                .that()
                .resideInAPackage("..domain..exception..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..");
```

---

# 99. Anti-Patterns

The following practices are prohibited:

- swallowing exceptions
- returning null after an unexpected failure
- catching `Throwable`
- catching broad exceptions without justification
- logging the same exception at every layer
- throwing generic `RuntimeException`
- exposing stack traces to clients
- exposing raw external error payloads
- losing the original cause
- retrying permanent failures
- retrying non-idempotent operations without protection
- using business exceptions for ordinary result states
- treating all failures as HTTP 500
- using exception messages as API contracts
- including secrets in exceptions or logs
- ignoring `InterruptedException`
- hiding mandatory configuration failures
- returning successful responses after critical partial failures

---

# 100. Review Checklist

Before introducing or changing exception handling, verify:

- Is the exception category correct?
- Is the exception name meaningful?
- Is the failure expected or truly exceptional?
- Is the original cause preserved?
- Is translation performed at the correct boundary?
- Is retryability explicit?
- Is the operation safe to retry?
- Is the message free from sensitive information?
- Will the exception be logged only once?
- Is the API error code stable?
- Is the HTTP status semantically correct?
- Is the failure observable through logs and metrics?
- Are tests covering the failure path?
- Is dead-letter handling defined for messaging failures?
- Is fallback behavior semantically safe?

---

# 101. Architecture Rules

Exception handling must:

- preserve domain independence
- translate technology-specific failures at adapter boundaries
- preserve root causes
- expose stable error codes
- protect sensitive information
- distinguish transient and permanent failures
- avoid duplicate logging
- support operational diagnosis
- use consistent Problem Details responses
- provide deterministic messaging failure behavior

---

# 102. Decision Summary

The project adopts:

- explicit domain, application and infrastructure exception categories
- framework-independent domain failures
- boundary-based exception translation
- stable API error codes
- Problem Details error responses
- explicit retryability classification
- cause preservation
- centralized HTTP exception handling
- bounded messaging retries
- dead-letter handling for permanent message failures
- structured and sanitized diagnostics
- single-owner exception logging
- safe generic responses for unexpected failures
- deterministic exception tests
