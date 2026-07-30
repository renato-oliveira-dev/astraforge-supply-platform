# Spring Boot Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Spring Boot Guidelines |
| Status | Draft |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Spring Boot development guidelines adopted by the AstraForge Supply Platform.

It establishes standards for:

- application configuration
- dependency injection
- REST APIs
- validation
- exception handling
- persistence
- transactions
- HTTP integrations
- security
- observability
- testing
- application lifecycle

These guidelines complement the general coding standards and Java guidelines.

---

# 2. Spring Boot Version

The project uses:

```text
Spring Boot 4.1
```

The platform should remain on a supported Spring Boot release line.

Framework upgrades must be:

- planned
- tested
- documented
- validated against security and compatibility requirements

---

# 3. General Principles

Spring Boot should be used as an application framework, not as the domain model.

The framework belongs primarily at architectural boundaries.

The domain layer should remain independent from:

- Spring stereotypes
- persistence annotations
- HTTP concerns
- serialization concerns
- security infrastructure
- messaging infrastructure

---

# 4. Application Structure

The application should be organized by architectural responsibility and business capability.

Recommended structure:

```text
com.enterprise.orders
├── application
├── domain
├── infrastructure
└── configuration
```

Within each layer, organize code by capability whenever practical.

Example:

```text
application
├── order
├── approval
└── cancellation
```

Avoid organizing the entire project only by technical stereotypes such as:

```text
controller
service
repository
model
```

---

# 5. Application Entry Point

The main application class should remain minimal.

Example:

```java
@SpringBootApplication
public class EnterpriseOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                EnterpriseOrderApplication.class,
                args
        );
    }
}
```

Do not place business logic in the application bootstrap class.

---

# 6. Component Scanning

Keep the application class in the root package.

Avoid broad custom component scanning.

Explicit scanning may be used only when modular boundaries require it.

Uncontrolled scanning can:

- increase startup time
- create accidental bean registration
- obscure application structure

---

# 7. Dependency Injection

Use constructor injection exclusively.

Correct:

```java
@Service
public class SubmitOrderService {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    public SubmitOrderService(
            OrderRepository orderRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }
}
```

Avoid:

- field injection
- setter injection for mandatory dependencies
- service locator patterns
- static access to the Spring context

---

# 8. Bean Ownership

Every bean should have a clear owner and responsibility.

Use stereotypes intentionally:

| Annotation | Purpose |
|---|---|
| `@RestController` | HTTP inbound adapter |
| `@Service` | Application service |
| `@Repository` | Persistence adapter |
| `@Component` | Generic infrastructure component |
| `@Configuration` | Bean composition and framework configuration |

Do not annotate domain classes with Spring stereotypes.

---

# 9. Configuration Classes

Configuration classes should be cohesive and focused.

Examples:

```text
KafkaConfiguration

SecurityConfiguration

JacksonConfiguration

HttpClientConfiguration

PersistenceConfiguration
```

Avoid one large configuration class containing unrelated beans.

---

# 10. Configuration Properties

Use type-safe configuration through `@ConfigurationProperties`.

Example:

```java
@ConfigurationProperties(prefix = "integration.inventory")
public record InventoryClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration responseTimeout
) {
}
```

Prefer immutable configuration records.

Validate configuration at startup.

---

# 11. Configuration Validation

Use Bean Validation for configuration properties.

Example:

```java
@ConfigurationProperties(prefix = "integration.payment")
public record PaymentClientProperties(
        @NotNull URI baseUrl,
        @NotNull Duration timeout,
        @Min(1) int maxConnections
) {
}
```

Invalid configuration must prevent application startup.

---

# 12. Externalized Configuration

Environment-specific values must remain outside the application artifact.

Use:

- environment variables
- ConfigMaps
- secret managers
- configuration files
- deployment descriptors

Do not hardcode:

- URLs
- credentials
- timeout values
- environment names
- feature flags

---

# 13. Profiles

Spring profiles should be used sparingly.

Suitable use cases:

- local infrastructure
- test-specific beans
- environment integration differences

Avoid placing business behavior behind profiles.

Prefer configuration values over profile-specific code when possible.

---

# 14. REST Controllers

Controllers should remain thin.

Responsibilities:

- receive HTTP requests
- validate input
- invoke application services
- map application results
- return HTTP responses

Controllers should not:

- contain business rules
- access repositories directly
- manage transactions
- orchestrate multiple domain operations
- build complex persistence queries

---

# 15. Controller Example

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = createOrderUseCase.execute(
                request.toCommand()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
```

---

# 16. API Contracts

External API contracts must use dedicated request and response models.

Do not expose:

- JPA entities
- aggregate roots
- internal domain objects
- persistence projections directly

API models should remain stable and version-aware.

---

# 17. Request Validation

Use Bean Validation at the HTTP boundary.

Example:

```java
public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
```

Bean Validation handles structural validation.

Business validation remains in the application or domain layer.

---

# 18. Validation Messages

Validation messages should be:

- clear
- stable
- client-oriented
- externalized when internationalization is required

Avoid exposing implementation details.

---

# 19. Business Validation

Business rules must not be implemented exclusively with annotations.

Examples:

- order transition rules
- approval limits
- customer eligibility
- inventory restrictions

These rules belong in domain objects, domain services or application policies.

---

# 20. API Versioning

Version APIs explicitly when backward compatibility requires it.

Preferred format:

```text
/api/v1/orders
```

Breaking changes require a new major API version.

Do not version APIs for every minor internal change.

---

# 21. HTTP Status Codes

Use status codes consistently.

| Status | Use |
|---|---|
| `200 OK` | Successful retrieval or update |
| `201 Created` | Resource created |
| `202 Accepted` | Asynchronous processing accepted |
| `204 No Content` | Successful operation without body |
| `400 Bad Request` | Invalid request |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Authenticated but unauthorized |
| `404 Not Found` | Resource not found |
| `409 Conflict` | State or concurrency conflict |
| `422 Unprocessable Entity` | Semantically invalid request |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Unexpected failure |
| `503 Service Unavailable` | Temporary dependency or service failure |

---

# 22. Problem Details

Use Spring Problem Details for HTTP error responses.

The platform should follow RFC 9457 semantics.

Example:

```json
{
  "type": "https://enterprise.example/problems/order-not-found",
  "title": "Order not found",
  "status": 404,
  "detail": "Order 8f7c... was not found",
  "instance": "/api/v1/orders/8f7c...",
  "code": "ORDER_NOT_FOUND",
  "traceId": "4fe72..."
}
```

Error responses should include a stable application code.

---

# 23. Global Exception Handling

Use `@RestControllerAdvice` for exception translation.

Example:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFound(
            OrderNotFoundException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatus(
                HttpStatus.NOT_FOUND
        );

        problem.setTitle("Order not found");
        problem.setDetail(exception.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", "ORDER_NOT_FOUND");

        return problem;
    }
}
```

The handler translates exceptions.

It must not contain business rules.

---

# 24. Exception Translation

Exceptions should be translated at architectural boundaries.

Example flow:

```text
Database exception

↓

Persistence adapter exception

↓

Application exception

↓

Problem Detail
```

Never expose raw framework exceptions to API clients.

---

# 25. Transactions

Transaction boundaries belong in the application layer.

Use `@Transactional` on application services or command handlers.

Example:

```java
@Service
public class ApproveOrderService {

    @Transactional
    public void approve(ApproveOrderCommand command) {
        // application orchestration
    }
}
```

Avoid placing transaction boundaries in controllers.

---

# 26. Transaction Scope

Transactions should remain:

- short
- deterministic
- local to one database
- free from slow remote calls

Do not keep a database transaction open while calling:

- external REST APIs
- Amazon SQS
- email services
- object storage

Use Outbox or asynchronous orchestration instead.

---

# 27. Read-Only Transactions

Use read-only transactions for query operations when appropriate.

Example:

```java
@Transactional(readOnly = true)
public OrderDetails findById(OrderId orderId) {
    return queryRepository.findById(orderId);
}
```

Read-only does not replace proper query design.

---

# 28. Propagation

Use default transaction propagation unless a specific requirement exists.

`REQUIRES_NEW` must be justified because it changes atomicity and failure semantics.

Do not use transaction propagation annotations as a workaround for poor orchestration.

---

# 29. Self-Invocation

Spring proxy-based annotations do not apply during self-invocation.

Avoid designs that depend on calling an annotated method from another method in the same class.

Extract the behavior into a dedicated bean when proxy interception is required.

---

# 30. Persistence Adapters

Spring Data repositories belong in the infrastructure layer.

Domain and application layers depend on repository ports.

Example:

```java
public interface OrderRepository {

    Optional<Order> findById(OrderId orderId);

    void save(Order order);
}
```

Infrastructure implementation:

```java
@Repository
public class JpaOrderRepository implements OrderRepository {
}
```

---

# 31. JPA Entities

JPA entities should not be used as external API models.

Keep persistence concerns isolated.

Recommended options:

- dedicated persistence entity
- aggregate mapping adapter
- embeddables for value objects
- explicit mapper

Directly annotating domain objects may be accepted only when the trade-off is deliberate and documented.

---

# 32. Lazy Loading

Avoid relying on lazy loading outside transaction boundaries.

Do not use Open Session in View.

Recommended setting:

```properties
spring.jpa.open-in-view=false
```

Queries should fetch exactly the data required by the use case.

---

# 33. Query Design

Prefer:

- projections
- explicit fetch plans
- pagination
- optimized repository methods
- dedicated query repositories

Avoid:

- `SELECT *`
- uncontrolled entity graph loading
- N+1 queries
- loading full aggregates for simple read models

---

# 34. Pagination

Paginated endpoints should define:

- page
- size
- sort
- maximum page size

Example:

```text
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

Maximum sizes must be enforced to protect resources.

---

# 35. Database Migrations

Use Flyway for all database changes.

Rules:

- never modify an applied migration
- create a new migration for every adjustment
- validate migrations in CI
- keep migrations deterministic
- separate schema and data migrations when appropriate

---

# 36. HTTP Clients

Use a standardized HTTP client configuration.

Preferred options:

- `RestClient` for synchronous integrations
- `WebClient` when reactive composition or streaming is justified

Do not introduce reactive APIs into the application solely because `WebClient` is available.

---

# 37. RestClient

Use `RestClient` for straightforward blocking HTTP integrations.

Example:

```java
@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public InventoryResponse findAvailability(UUID productId) {
        return restClient.get()
                .uri("/inventory/{productId}", productId)
                .retrieve()
                .body(InventoryResponse.class);
    }
}
```

---

# 38. WebClient

Use `WebClient` when required for:

- streaming
- reactive pipelines
- non-blocking high-concurrency workloads
- reactive framework compatibility

Do not call `.block()` repeatedly inside reactive pipelines.

If the application is imperative, use a consistent blocking model.

---

# 39. HTTP Client Configuration

Every integration should define:

- base URL
- connect timeout
- response timeout
- connection pool
- maximum connections
- idle connection lifetime
- retry policy
- circuit breaker
- correlation headers

Configuration must be externalized.

---

# 40. Remote Error Handling

Remote errors must be translated into meaningful application exceptions.

Capture:

- HTTP status
- sanitized response message
- dependency name
- correlation identifier
- elapsed time

Do not expose raw external payloads to clients.

---

# 41. Correlation Headers

Propagate standard identifiers across service calls.

Examples:

```text
traceparent

tracestate

X-Correlation-Id

X-Request-Id
```

Do not invent multiple identifiers for the same purpose without justification.

---

# 42. Resilience

Protect remote integrations with:

- timeout
- circuit breaker
- bounded retry
- bulkhead where required
- fallback only when semantically safe

Resilience policies should be configured per dependency.

Do not apply one global policy to all integrations.

---

# 43. Retry Rules

Retry only transient failures.

Suitable examples:

- HTTP 429
- HTTP 502
- HTTP 503
- HTTP 504
- connection reset

Do not retry:

- validation errors
- authorization failures
- business rejections
- non-idempotent operations without protection

---

# 44. Security Configuration

Use Spring Security for authentication and authorization.

The platform should operate as an OAuth2 Resource Server.

Example:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(registry -> registry
                    .requestMatchers("/actuator/health/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(
                    oauth2 -> oauth2.jwt(Customizer.withDefaults())
            )
            .build();
}
```

Security configuration must be explicit.

---

# 45. Method Security

Use method-level authorization for sensitive application operations.

Example:

```java
@PreAuthorize("hasAuthority('order:approve')")
public void approve(ApproveOrderCommand command) {
}
```

Do not rely only on controller path security for critical business actions.

---

# 46. Authorization Rules

Authorization should validate:

- role or authority
- customer ownership
- tenant
- segment
- region
- workflow level
- business scope

Authentication alone is insufficient.

---

# 47. CSRF

Stateless bearer-token APIs may disable CSRF.

Applications using cookie-based authentication must evaluate and configure CSRF protection properly.

Never disable CSRF by habit without understanding the authentication model.

---

# 48. CORS

Configure CORS explicitly.

Production environments must not use unrestricted wildcard origins when credentials or sensitive APIs are involved.

---

# 49. Jackson Configuration

Centralize serialization configuration.

Define standards for:

- dates
- enums
- unknown properties
- null values
- precision
- case sensitivity

Do not configure `ObjectMapper` independently in multiple classes.

---

# 50. Date Serialization

Use ISO-8601.

Examples:

```text
2026-07-22T18:30:00Z

2026-07-22

18:30:00
```

Do not expose locale-dependent date formats in APIs.

---

# 51. Enum Serialization

External enum values are API contracts.

Do not rename serialized values casually.

When internal and external representations differ, use explicit mapping.

---

# 52. Messaging

SQS consumers/listeners belong in infrastructure adapters.

Listeners should:

- deserialize
- validate the envelope
- check idempotency
- invoke an application use case
- handle acknowledgment according to processing outcome

Business logic must not remain inside listener methods.

---

# 53. Event Publication

Business transactions should not publish directly to SQS; use Transactional Outbox when state and event publication must be reliable.

Use the Transactional Outbox Pattern.

Flow:

```text
Business update

+

Outbox insert

↓

Transaction commit

↓

Outbox dispatcher

↓

Amazon SQS
```

---

# 54. Scheduled Jobs

Scheduled jobs should delegate to application services.

Example:

```java
@Component
public class OutboxDispatcherJob {

    private final DispatchPendingEventsUseCase useCase;

    @Scheduled(fixedDelayString = "${outbox.dispatch.interval}")
    public void dispatch() {
        useCase.execute();
    }
}
```

The scheduler should not contain business logic.

---

# 55. Distributed Job Coordination

When multiple application replicas exist, scheduled jobs must avoid duplicate execution when duplication is unsafe.

Possible strategies:

- database locking
- lease table
- Kubernetes CronJob
- Redis lock when appropriate
- leader election

The chosen mechanism must have timeout and recovery semantics.

---

# 56. Actuator

Enable Spring Boot Actuator for operational visibility.

Recommended endpoints:

```text
health

info

metrics

prometheus
```

Expose only required endpoints.

Administrative endpoints must be protected.

---

# 57. Health Groups

Define liveness and readiness groups.

Example:

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

Liveness should not depend on temporary external dependency failures.

Readiness may reflect the ability to serve traffic.

---

# 58. Metrics

Use Micrometer for application metrics.

Track:

- HTTP latency
- error rate
- throughput
- JVM behavior
- database pool
- SQS consumers
- external integrations
- business operations

Metric names and tags must remain stable.

---

# 59. Metric Cardinality

Avoid high-cardinality metric tags.

Do not use:

- customer ID
- order ID
- request ID
- email address
- arbitrary exception message

High-cardinality dimensions belong in logs and traces.

---

# 60. Logging

Use structured logging.

Include:

- service
- environment
- traceId
- correlationId
- operation
- outcome
- elapsed time

Do not log sensitive payloads.

---

# 61. Logging Ownership

Unexpected exceptions should normally be logged once at the outer application boundary.

Avoid logging and rethrowing the same exception through multiple layers.

Integration adapters may log failures when they add dependency-specific diagnostic context.

---

# 62. Distributed Tracing

Use standard trace propagation.

Instrumentation should cover:

- inbound HTTP
- outbound HTTP
- SQS publication
- SQS consumption
- database access
- scheduled processing

Trace sampling must be configurable.

---

# 63. Graceful Shutdown

Enable graceful shutdown.

Example:

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

The application should:

- stop accepting new traffic
- finish active requests
- stop consumers safely
- close connection pools
- terminate executors

---

# 64. Startup Behavior

Application startup must fail when mandatory configuration is invalid.

Avoid hiding startup failures with silent fallbacks.

Optional dependencies, such as Redis cache, may degrade gracefully only when explicitly designed as optional.

---

# 65. Startup Tasks

Avoid long-running business processing during startup.

Startup initialization should be limited to:

- configuration validation
- migration validation
- required connection initialization
- lightweight cache preparation when justified

Heavy backfills should run as dedicated jobs.

---

# 66. Feature Flags

Feature flags should be:

- externally configured
- auditable
- temporary when possible
- removed after full rollout

Do not spread raw flag checks throughout the codebase.

Encapsulate them behind a policy or feature service.

---

# 67. Caching

Use Spring Cache only when cache semantics are clear.

Define:

- cache key
- TTL
- invalidation strategy
- fallback behavior
- ownership

Do not cache JPA-managed entities.

Cache immutable DTOs or projections instead.

---

# 68. Cache Invalidation

Update the database first.

Evict or refresh the cache only after successful commit.

Avoid cache updates inside transactions that may roll back.

Use transaction synchronization or event-based invalidation when required.

---

# 69. Async Processing

Use `@Async` carefully.

Requirements:

- explicit executor
- context propagation
- exception handling
- lifecycle ownership
- monitoring

Avoid relying on the default executor.

Do not use `@Async` to hide slow or poorly designed request flows.

---

# 70. Virtual Threads

Spring Boot applications may use virtual threads for suitable blocking workloads.

Configuration may include:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Virtual threads do not remove the need for:

- database pool limits
- API concurrency limits
- timeouts
- bulkheads
- rate limiting

Validate framework and library compatibility before enabling globally.

---

# 71. Bean Lifecycle

Avoid complex logic in constructors.

Use constructors only to establish dependencies and invariants.

Use lifecycle callbacks only for infrastructure initialization.

Do not perform remote calls in bean constructors.

---

# 72. Circular Dependencies

Circular dependencies are prohibited.

Do not enable circular bean references as a workaround.

A circular dependency usually indicates:

- mixed responsibilities
- poor layering
- incorrect orchestration
- missing abstraction

---

# 73. Conditional Beans

Use conditional beans for infrastructure variability, not business behavior.

Examples:

- local fake implementation
- cloud provider adapter
- optional cache implementation

Conditions should remain explicit and tested.

---

# 74. AOT and Native Image

AOT or Native Image may be evaluated when startup time and memory usage justify the complexity.

Adoption requires validation of:

- reflection
- serialization
- dynamic proxies
- libraries
- observability agents
- build time
- operational support

Native Image is not a default requirement.

---

# 75. Spring Modulith

Spring Modulith may be adopted when the platform is implemented as a modular monolith.

It may support:

- module boundaries
- architecture verification
- application events
- module documentation
- integration testing

It should not be introduced when the project already uses independently deployed bounded-context services without a clear benefit.

---

# 76. Testing Strategy

Spring tests should use the narrowest useful test scope.

Recommended hierarchy:

1. unit tests without Spring
2. slice tests
3. integration tests
4. end-to-end tests

Do not start the full application context for every test.

---

# 77. Unit Tests

Application and domain tests should instantiate classes directly.

Example:

```java
class ApproveOrderServiceTest {

    private OrderRepository orderRepository;
    private ApproveOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        service = new ApproveOrderService(orderRepository);
    }
}
```

Spring is unnecessary for pure unit tests.

---

# 78. Controller Tests

Use `@WebMvcTest` for MVC slice tests.

Validate:

- request mapping
- validation
- status codes
- serialized response
- security rules
- Problem Details

Mock only the application boundary used by the controller.

---

# 79. Repository Tests

Use `@DataJpaTest` for persistence mappings and query behavior.

Prefer PostgreSQL-compatible integration environments for database-specific behavior.

H2 must not be considered equivalent to PostgreSQL for:

- SQL dialect
- JSONB
- locking
- indexes
- constraints
- transaction behavior

---

# 80. Testcontainers

Use Testcontainers for realistic integration tests involving:

- PostgreSQL
- Amazon SQS
- Redis
- external infrastructure emulators

Container reuse may be configured locally, but CI tests must remain isolated and deterministic.

---

# 81. Full Context Tests

Use `@SpringBootTest` only when validating complete application integration.

Examples:

- security chain
- startup configuration
- multiple adapters
- full use-case execution

Keep the number of full-context tests controlled.

---

# 82. Mocking

Mock architectural dependencies, not internal implementation details.

Avoid excessive mocking of:

- value objects
- collections
- simple domain entities
- framework internals

Prefer real domain objects.

---

# 83. Test Conventions

Tests should:

- use `test*` names
- include AssertJ `.as("...")` descriptions
- avoid random identifiers
- avoid `Thread.sleep`
- avoid broad argument matchers when exact values matter
- verify one behavior per test
- keep exception assertion lambdas to one invocation

---

# 84. Context Caching

Spring test context startup is expensive.

Keep test configurations consistent to maximize context reuse.

Avoid unnecessary use of:

- `@DirtiesContext`
- dynamic profile combinations
- per-test custom bean graphs

---

# 85. API Documentation

Use OpenAPI for public or partner-facing APIs.

Documentation should include:

- endpoints
- authentication
- requests
- responses
- validation
- error models
- examples

Generated documentation must reflect the actual runtime contract.

---

# 86. Package Documentation

Every significant package should include `package-info.java`.

It should describe:

- package responsibility
- architectural layer
- allowed dependencies
- integration boundaries

---

# 87. Dependency Management

Use the Spring Boot dependency management platform.

Avoid overriding managed dependency versions without a documented reason.

Dependency overrides require compatibility and security validation.

---

# 88. Framework Upgrades

Upgrade procedure should include:

- release note review
- deprecation analysis
- automated tests
- integration validation
- performance comparison
- security scan
- rollback plan

Avoid accumulating multiple unsupported major versions.

---

# 89. Deprecated APIs

Do not introduce newly deprecated Spring APIs.

Existing deprecated usage should have:

- replacement plan
- tracked removal
- compatibility tests

---

# 90. Architecture Tests

Use automated architecture rules to verify:

- domain independence
- layer dependencies
- controller isolation
- repository isolation
- naming conventions
- package boundaries

ArchUnit may be used for these validations.

---

# 91. Operational Defaults

Every service should define:

- request timeouts
- graceful shutdown
- health probes
- metrics
- structured logging
- database pool limits
- integration limits
- security configuration

Framework defaults should not be accepted blindly for production-critical behavior.

---

# 92. Production Hardening

Production configuration should:

- disable debug mode
- restrict Actuator exposure
- disable stack traces in client responses
- enforce TLS
- use secure cookies when applicable
- protect administrative endpoints
- use least-privilege credentials
- configure resource limits

---

# 93. Anti-Patterns

The following practices are prohibited:

- field injection
- business logic in controllers
- repositories called directly by controllers
- public JPA entities as API responses
- Open Session in View
- remote calls inside database transactions
- unbounded retries
- default async executors
- circular dependencies
- swallowed exceptions
- unrestricted Actuator exposure
- hardcoded secrets
- unrestricted CORS in production

---

# 94. Architecture Rules

Spring Boot code must:

- preserve domain independence
- use constructor injection
- keep controllers thin
- define explicit transaction boundaries
- isolate persistence
- standardize HTTP clients
- expose operational endpoints securely
- use realistic integration tests
- externalize configuration
- fail safely

---

# 95. Decision Summary

The project adopts:

- Spring Boot 4.1
- constructor injection
- immutable configuration properties
- thin REST controllers
- Bean Validation at boundaries
- Problem Details for API errors
- application-level transaction boundaries
- isolated persistence adapters
- standardized HTTP clients
- OAuth2 Resource Server security
- Actuator and Micrometer
- graceful shutdown
- Testcontainers
- narrow Spring test slices
- automated architecture enforcement
