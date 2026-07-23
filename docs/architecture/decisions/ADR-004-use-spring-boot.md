# ADR-004: Use Spring Boot

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-004 |
| Title | Use Spring Boot |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Application Framework |
| Related Work Items | Initial platform architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform requires a production-ready application framework for implementing backend services using Java 21.

The platform must support:

- REST APIs
- dependency injection
- application configuration
- transaction management
- persistence integration
- validation
- security
- messaging
- caching
- observability
- automated testing
- containerized deployment
- operational health checks
- external-service integration

The application framework must accelerate delivery without becoming the owner of the business model.

The approved architecture requires business behavior to remain isolated from infrastructure concerns.

Therefore, the selected framework must support Clean Architecture and Domain-Driven Design rather than forcing framework-specific concepts into the Domain layer.

---

# 2. Problem Statement

The platform must select a standard application framework that:

- supports Java 21
- provides mature enterprise capabilities
- integrates with the selected infrastructure
- reduces repetitive configuration
- supports production operations
- enables automated testing
- provides a large and maintained ecosystem
- supports secure web applications
- supports dependency inversion
- remains compatible with Clean Architecture
- does not require business logic to depend directly on framework APIs

The decision must define how Spring Boot is used and which architectural boundaries constrain its use.

---

# 3. Decision Drivers

The primary decision drivers are:

1. Java 21 compatibility
2. enterprise maturity
3. production readiness
4. developer productivity
5. ecosystem support
6. security integration
7. persistence integration
8. messaging integration
9. observability
10. testing support
11. configuration management
12. dependency injection
13. operational maintainability
14. community and vendor support
15. compatibility with Clean Architecture
16. compatibility with Domain-Driven Design
17. long-term maintainability
18. container and Kubernetes support

---

# 4. Constraints

The decision must consider:

- Java 21 is the mandatory language and runtime
- Clean Architecture defines dependency direction
- Domain-Driven Design defines business modeling
- PostgreSQL is the primary transactional database
- Flyway manages database migrations
- Kafka may be used for integration events
- Redis may be used for caching
- REST is the primary synchronous interface
- production workloads run in containers
- Kubernetes may orchestrate production workloads
- security uses OAuth 2.0 and OpenID Connect
- domain classes must remain framework-independent
- testing must include unit, integration and architecture tests
- public contracts must remain explicit and versioned

---

# 5. Considered Options

## 5.1 Option A: Spring Boot

Spring Boot provides an opinionated application platform built on the Spring ecosystem.

### Advantages

- strong Java 21 support
- mature dependency-injection model
- broad enterprise adoption
- extensive documentation
- large ecosystem
- Spring MVC support
- Spring Security support
- Spring Data integration
- transaction management
- validation integration
- Kafka integration
- Redis integration
- health and operational endpoints
- strong testing ecosystem
- externalized configuration
- container-friendly runtime
- broad observability integration
- strong developer familiarity

### Disadvantages

- large framework surface
- auto-configuration may hide behavior
- incorrect use may couple business logic to Spring
- dependency graph can become large
- startup and memory footprint may exceed lighter frameworks
- framework upgrades require dependency alignment
- proxy-based behavior may be misunderstood
- transaction and security annotations may be applied incorrectly
- developers may treat framework stereotypes as architecture

---

## 5.2 Option B: Quarkus

Quarkus is a Java framework optimized for cloud-native deployment and fast startup.

### Advantages

- fast startup
- reduced memory footprint
- strong container orientation
- native-image support
- modern developer experience
- Java ecosystem compatibility
- integration with common infrastructure

### Disadvantages

- smaller ecosystem than Spring
- less organizational familiarity
- migration and training cost
- some patterns differ from established Spring practices
- native-image constraints may affect reflection and libraries
- no current business requirement justifies replacing the Spring ecosystem
- operational standards would need to be developed separately

---

## 5.3 Option C: Micronaut

Micronaut provides compile-time dependency injection and cloud-native capabilities.

### Advantages

- fast startup
- reduced reflection
- lower memory consumption
- strong cloud-native support
- compile-time dependency injection
- good HTTP-client support

### Disadvantages

- smaller ecosystem
- lower organizational familiarity
- migration and training effort
- fewer established internal standards
- less extensive integration experience
- no material requirement justifies the additional adoption risk

---

## 5.4 Option D: Jakarta EE

The platform could use Jakarta EE with an application server or compatible runtime.

### Advantages

- standardized enterprise APIs
- vendor-neutral specifications
- mature transaction and persistence standards
- broad historical enterprise usage

### Disadvantages

- application-server operational model may be heavier
- less aligned with the selected containerized deployment approach
- slower developer feedback in some environments
- integration ecosystem is less unified than Spring Boot
- greater configuration variability across runtimes
- lower team alignment for the target architecture

---

## 5.5 Option E: Minimal Custom Framework

The platform could use Java standard APIs and selected libraries without a comprehensive application framework.

### Advantages

- maximum control
- smaller dependency surface
- no framework lock-in
- potentially reduced startup cost
- explicit behavior

### Disadvantages

- significant custom infrastructure code
- repeated configuration
- increased security risk
- increased testing effort
- fragmented dependency choices
- higher maintenance cost
- slower delivery
- inconsistent implementations
- operational features must be built manually
- limited benefit for a business-oriented enterprise platform

---

# 6. Decision

The Enterprise Order Platform will use Spring Boot as its standard application framework.

Spring Boot will provide capabilities for:

- application bootstrap
- dependency injection
- configuration
- REST interfaces
- validation
- transaction management
- security integration
- persistence adapters
- messaging adapters
- caching adapters
- external HTTP clients
- health checks
- operational endpoints
- testing support

Spring Boot must remain concentrated in the outer architectural layers.

The Domain layer must not depend on Spring Boot or Spring Framework APIs.

---

# 7. Rationale

Spring Boot was selected because it provides the strongest balance of:

- enterprise maturity
- ecosystem completeness
- Java 21 support
- team familiarity
- productivity
- security
- infrastructure integration
- testing capability
- operational readiness

The platform requires more than an HTTP framework.

It requires coordinated support for:

- database transactions
- security policies
- messaging
- configuration
- validation
- observability
- production diagnostics
- test infrastructure

Spring Boot provides these capabilities through a cohesive and widely supported ecosystem.

The framework cost is accepted because architectural rules will prevent it from becoming the core business model.

---

# 8. Architectural Position

Spring Boot belongs primarily to the outer layers.

```text
Interface
    Spring MVC
    Bean Validation
    Spring Security integration

Infrastructure
    Spring Data
    Kafka integration
    Redis integration
    HTTP clients
    Configuration
    Observability

Application
    Limited Spring usage where technically justified

Domain
    No Spring dependency
```

The framework supports the architecture.

It does not define the domain model.

---

# 9. Domain Layer Rules

The Domain layer must not use:

- `@Component`
- `@Service`
- `@Repository`
- `@Configuration`
- `@Transactional`
- `@Entity`
- `@Table`
- `@Autowired`
- `ApplicationContext`
- Spring events
- Spring validation annotations
- Spring Security types
- Spring Data abstractions

Domain objects must be created and tested without starting a Spring context.

---

# 10. Application Layer Rules

The Application layer may use limited Spring capabilities where they represent application orchestration rather than domain behavior.

Permitted examples may include:

- `@Service` on use-case implementations
- `@Transactional` on application services
- constructor injection
- Spring-managed executors
- application-event integration where explicitly approved

The Application layer must not depend on:

- Spring Data repository implementations
- JPA entities
- REST request classes
- HTTP response types
- Kafka records
- Redis templates
- servlet APIs
- framework-specific external-client responses

Framework-neutral application services remain preferred where practical.

---

# 11. Interface Layer

The Interface layer may use:

- Spring MVC
- Bean Validation
- Spring Security adapters
- OpenAPI integration
- exception handlers
- request filters
- HTTP-specific converters

Typical components include:

```text
@RestController

@RequestMapping

@ControllerAdvice

Authentication filters

Request DTOs

Response DTOs
```

The Interface layer must translate external requests into application commands and queries.

---

# 12. Infrastructure Layer

The Infrastructure layer contains concrete Spring integrations such as:

- Spring Data repositories
- JPA adapters
- Kafka producers and consumers
- Redis adapters
- HTTP clients
- security configuration
- application properties
- actuator configuration
- metrics configuration
- tracing integration
- scheduler configuration

Infrastructure components implement ports defined by inner layers.

---

# 13. Dependency Injection

Constructor injection is mandatory.

Preferred:

```java
@Service
public class ApproveOrderService implements ApproveOrderUseCase {

    private final OrderRepository orderRepository;
    private final AuthorizationPort authorizationPort;

    public ApproveOrderService(
            OrderRepository orderRepository,
            AuthorizationPort authorizationPort
    ) {
        this.orderRepository = orderRepository;
        this.authorizationPort = authorizationPort;
    }
}
```

Avoid:

```java
@Autowired
private OrderRepository orderRepository;
```

Constructor injection provides:

- explicit dependencies
- immutability
- easier testing
- fail-fast construction
- reduced framework coupling
- simpler static analysis

---

# 14. Optional Dependencies

Application behavior should not depend on nullable injected dependencies.

Optional capabilities must use explicit abstractions.

Preferred:

```java
public interface AuditPort {

    void record(AuditEntry entry);
}
```

A no-operation adapter may be supplied when the capability is disabled.

Avoid injecting `Optional<Dependency>` into core application services unless the optionality is a deliberate part of the design.

---

# 15. Component Scanning

Component scanning must use controlled package boundaries.

The main application class should reside in the common root package.

Example:

```java
@SpringBootApplication
public class OrderPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderPlatformApplication.class, args);
    }
}
```

Broad or unrelated component scanning is prohibited.

Avoid:

```java
@ComponentScan("com")
```

Explicit configuration is preferred when package boundaries are not naturally aligned.

---

# 16. Bean Configuration

Use configuration classes for infrastructure wiring.

Example:

```java
@Configuration
public class OrderApplicationConfiguration {

    @Bean
    ApproveOrderUseCase approveOrderUseCase(
            OrderRepository orderRepository,
            AuthorizationPort authorizationPort
    ) {
        return new ApproveOrderService(
                orderRepository,
                authorizationPort
        );
    }
}
```

Explicit bean configuration may be preferred when:

- keeping the Application layer Spring-independent
- wiring multiple implementations
- applying decorators
- configuring policies
- controlling infrastructure composition

---

# 17. Auto-Configuration

Spring Boot auto-configuration may be used when:

- behavior is understood
- configuration is explicit enough for operations
- the dependency is approved
- defaults are appropriate
- production behavior is validated

Auto-configuration must not be treated as invisible magic.

Critical infrastructure must document:

- enabled configuration
- overridden defaults
- connection settings
- timeout settings
- pool settings
- retry behavior
- security behavior
- health behavior

---

# 18. Configuration Properties

Application configuration must use typed configuration properties.

Preferred:

```java
@ConfigurationProperties(prefix = "orders.approval")
public record OrderApprovalProperties(
        Duration timeout,
        int maximumParallelValidations
) {
}
```

Avoid scattering `@Value` expressions across multiple classes.

Typed configuration provides:

- centralized validation
- type safety
- discoverability
- easier testing
- clearer documentation
- reduced string-based configuration

---

# 19. Configuration Validation

Mandatory properties must be validated during startup.

Example:

```java
@ConfigurationProperties(prefix = "external.inventory")
@Validated
public record InventoryClientProperties(
        @NotBlank String baseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration responseTimeout
) {
}
```

The application should fail fast when required configuration is invalid.

---

# 20. Environment Configuration

Configuration must be externalized.

Sources may include:

- application configuration files
- environment variables
- Kubernetes ConfigMaps
- Kubernetes Secrets
- approved secret-management systems
- runtime arguments

Environment-specific values must not be embedded in source code or packaged artifacts.

---

# 21. Configuration Profiles

Spring profiles may be used for environment-specific infrastructure wiring.

Examples:

```text
local

test

development

staging

production
```

Profiles must not contain business-rule variants.

Avoid behavior such as:

```text
Production approval rules differ because the production profile is active.
```

Business variation must use explicit domain configuration or policy models.

---

# 22. Secret Management

Secrets must not appear in:

- source code
- committed configuration files
- logs
- exception messages
- actuator output
- test fixtures using real credentials

Spring Boot must consume secrets from approved external sources.

Sensitive configuration must use restricted exposure and masked diagnostics.

---

# 23. REST Architecture

Spring MVC will be the default synchronous REST stack.

Controllers must:

- remain thin
- use dedicated request DTOs
- invoke application use cases
- return dedicated response DTOs
- use explicit status codes
- use standardized error responses
- avoid persistence access

REST behavior remains governed by the API design guidelines.

---

# 24. Reactive Programming

Spring WebFlux will not be the default application model.

It may be adopted when justified by:

- end-to-end reactive requirements
- streaming workloads
- non-blocking infrastructure
- measured scalability needs
- specialized gateway behavior

Using WebClient does not require the entire application to become reactive.

Mixing blocking and reactive models without clear boundaries is prohibited.

A broad reactive architecture change requires a separate ADR.

---

# 25. HTTP Clients

External HTTP integrations must use approved Spring clients.

Possible options include:

- `RestClient`
- `WebClient`
- declarative HTTP interfaces where supported and approved

Client design must define:

- base URL
- connection timeout
- response timeout
- connection pool
- authentication
- correlation headers
- error translation
- retry behavior
- circuit breaking
- logging
- metrics

External-client models must remain in Infrastructure.

---

# 26. Client Timeout Rules

Every external HTTP client must define explicit timeouts.

Required considerations:

- connection timeout
- response timeout
- connection acquisition timeout
- read timeout
- write timeout
- total operation timeout

Framework defaults must not be assumed to meet production requirements.

---

# 27. Retry Rules

Automatic retries must be used carefully.

Retries are permitted only when:

- the operation is idempotent
- transient failure is expected
- retry limits are bounded
- backoff is defined
- downstream capacity is considered
- observability exists

Avoid combining multiple retry layers unintentionally.

Examples of duplicate layers include:

- HTTP-client retry
- Resilience4j retry
- Kafka retry
- application retry
- infrastructure retry

---

# 28. Transaction Management

Spring transaction management will be used at application-service boundaries.

Preferred:

```java
@Transactional
public ApproveOrderResult execute(ApproveOrderCommand command) {
    // Application transaction
}
```

Transactions must include only work requiring atomic consistency.

Avoid:

- remote HTTP calls inside long transactions
- waiting on external messaging
- unbounded collection processing
- large file operations
- user interaction
- retry loops inside active transactions

---

# 29. Transactional Proxies

Developers must understand Spring proxy behavior.

A transactional method may not be applied as expected when:

- invoked through self-invocation
- declared with unsupported visibility
- called on an object not managed by Spring
- combined incorrectly with asynchronous execution
- exception handling suppresses rollback behavior

Transaction boundaries must be verified through integration tests.

---

# 30. Transaction Propagation

Default propagation should be used unless a clear requirement justifies another mode.

Non-default propagation such as:

- `REQUIRES_NEW`
- `MANDATORY`
- `NOT_SUPPORTED`
- `NESTED`

requires explicit review.

Incorrect propagation can create:

- partial commits
- hidden transactions
- connection exhaustion
- inconsistent outbox behavior
- difficult rollback semantics

---

# 31. Persistence Integration

Spring Data may be used inside Infrastructure.

Spring Data repositories must not become application or domain ports directly.

Preferred separation:

```text
OrderRepository
    Application port

JpaOrderRepositoryAdapter
    Infrastructure adapter

SpringDataOrderRepository
    Spring Data implementation detail
```

This isolates framework-specific query and persistence behavior.

---

# 32. JPA Usage

JPA and Hibernate may be used for transactional persistence.

Rules include:

- JPA entities remain in Infrastructure
- domain models remain persistence-independent
- lazy-loading behavior must not cross transaction boundaries
- query counts must be monitored
- pagination must be explicit
- locking behavior must be intentional
- N+1 queries must be prevented
- batch behavior must be validated
- entity graphs and fetch joins must be used carefully

---

# 33. Open Session in View

Open Session in View must be disabled.

Recommended configuration:

```yaml
spring:
  jpa:
    open-in-view: false
```

Reasons include:

- preventing hidden lazy loading in controllers
- keeping transaction boundaries explicit
- avoiding unplanned queries during serialization
- reducing persistence leakage into the Interface layer

---

# 34. Flyway Integration

Flyway will manage database schema evolution.

Spring Boot may trigger Flyway during startup according to environment policy.

Mandatory rule:

> An existing or applied migration must never be modified.

Every database change requires a new migration version.

Migration failure must prevent application startup where schema compatibility is required.

---

# 35. Messaging Integration

Spring for Apache Kafka may be used in Infrastructure.

Kafka listeners and producers must remain isolated from domain models.

Consumers must define:

- deserialization behavior
- validation
- idempotency
- retry policy
- dead-letter handling
- observability
- transaction implications
- failure classification

Broker-specific headers must not leak into the Domain layer.

---

# 36. Event Publication

Application transactions requiring reliable event publication must follow the approved Transactional Outbox decision.

Direct broker publication inside a business transaction must not be assumed to be atomic with database persistence.

Spring application events are not a replacement for durable integration events.

---

# 37. Caching Integration

Spring Cache or direct Redis adapters may be used where appropriate.

Caching must remain an Infrastructure concern.

Cache use must define:

- key ownership
- value format
- expiration
- invalidation
- consistency
- fallback behavior
- metrics
- failure handling
- data sensitivity

Domain behavior must not depend on cache availability.

---

# 38. Security Integration

Spring Security will provide technical security enforcement.

Responsibilities include:

- token validation
- authentication
- authorization filters
- method-level technical authorization where approved
- security headers
- CORS configuration
- endpoint protection
- principal extraction

Business authorization rules must remain explicit in Application or Domain behavior.

---

# 39. Method Security

Method-level security may be used for coarse-grained authorization.

Example:

```java
@PreAuthorize("hasAuthority('order:approve')")
```

It must not replace business authorization such as:

- approval-value limits
- context-specific authority
- customer ownership
- workflow position
- order-type restrictions

Technical and business authorization must remain distinguishable.

---

# 40. Validation

Bean Validation may be used at API and configuration boundaries.

Examples:

- mandatory request fields
- format
- length
- numerical limits
- configuration validity

Domain invariants must still be protected inside the Domain layer.

A valid request DTO does not guarantee a valid domain operation.

---

# 41. Validation Groups

Validation groups should be avoided unless they clearly improve a stable interface contract.

Complex validation-group hierarchies often indicate that request models should be separated by operation.

Preferred:

```text
CreateOrderRequest

UpdateOrderRequest

ApproveOrderRequest
```

rather than one large request with many conditional groups.

---

# 42. Exception Handling

Spring MVC exceptions must be translated through centralized handlers.

Use:

```text
@ControllerAdvice

Problem Details

Stable error codes

Correlation identifiers
```

Controllers must not contain repeated exception-mapping logic.

Infrastructure exceptions must be translated before reaching the public API.

---

# 43. Problem Details

API errors must follow the approved Problem Details standard.

Responses should include appropriate fields such as:

- type
- title
- status
- detail
- instance
- error code
- trace or correlation identifier
- field violations where applicable

Internal stack traces must not be exposed.

---

# 44. Actuator

Spring Boot Actuator may be used for operational endpoints.

Approved capabilities may include:

- health
- readiness
- liveness
- metrics
- info
- configuration diagnostics where safe
- logging-level management where operationally controlled

Sensitive endpoints must be restricted or disabled.

---

# 45. Health Checks

Health checks must distinguish:

- liveness
- readiness
- dependency health
- degraded but operational states

Liveness must not fail merely because an external dependency is temporarily unavailable.

Readiness may reflect whether the application can safely receive traffic.

Health checks must avoid causing excessive load on dependencies.

---

# 46. Metrics

Spring Boot metrics integration may be used through Micrometer.

Metrics should cover:

- request rate
- request latency
- error rate
- database pool usage
- external-client latency
- Kafka processing
- cache behavior
- outbox backlog
- retry activity
- circuit-breaker state
- business-critical operations

Metric labels must avoid unbounded cardinality.

---

# 47. Logging

Spring Boot logging must follow the platform logging standard.

Logs must:

- use structured fields where supported
- include correlation context
- avoid secrets
- avoid sensitive personal data
- avoid duplicate stack traces
- use appropriate levels
- preserve exception causes
- remain useful for operations

Spring-generated logs should be configured to avoid excessive noise.

---

# 48. Tracing

Tracing integration may use Spring-compatible observability tooling.

Trace propagation should cover:

- inbound HTTP
- outbound HTTP
- messaging
- asynchronous execution
- scheduled processing

Trace data must not become a business dependency.

Instrumentation must be validated for Java 21 compatibility and operational overhead.

---

# 49. Scheduled Tasks

Spring scheduling may be used for local recurring work.

Scheduled tasks must define:

- ownership
- locking strategy in multi-instance deployments
- idempotency
- execution timeout
- failure handling
- observability
- concurrency behavior

A simple `@Scheduled` method is insufficient when multiple replicas may execute the same task.

Distributed execution may require:

- leader election
- database locking
- broker-based scheduling
- dedicated job infrastructure

---

# 50. Asynchronous Execution

`@Async` must not be used without explicit configuration.

Every asynchronous executor must define:

- executor bean
- thread or virtual-thread strategy
- task limits
- rejection behavior
- context propagation
- exception handling
- lifecycle
- observability

The default executor must not be relied upon for business-critical asynchronous work.

---

# 51. Virtual Threads

Spring-managed virtual-thread support may be used for suitable blocking I/O workloads.

Adoption must still define:

- downstream capacity
- connection-pool sizing
- task limits
- context propagation
- transaction behavior
- cancellation
- observability

Virtual threads do not justify unbounded work submission.

---

# 52. Application Startup

Startup must fail when critical conditions are invalid.

Examples:

- missing mandatory configuration
- incompatible database schema
- invalid security configuration
- duplicate bean ambiguity
- unsupported dependency configuration
- invalid migration state

Non-critical external dependencies should not necessarily block startup unless the application cannot operate safely without them.

---

# 53. Graceful Shutdown

The application must support graceful shutdown.

Shutdown behavior should:

- stop accepting new traffic
- complete or terminate in-flight requests safely
- stop message consumption
- finish bounded work where possible
- release database connections
- close HTTP-client resources
- stop executors
- respect platform termination deadlines

Container termination settings must align with Spring shutdown behavior.

---

# 54. Testing Strategy

Spring Boot tests must use the smallest appropriate test scope.

Preferred hierarchy:

```text
Pure unit tests

Domain tests

Application tests with mocked ports

MVC slice tests

Persistence slice tests

Integration tests

Full application tests
```

Starting a full Spring context for every test is prohibited.

---

# 55. Unit Tests

Pure unit tests should not use Spring.

Appropriate targets include:

- aggregates
- value objects
- domain services
- policies
- mappers
- application services with mocked ports
- utility components

Unit tests must remain fast and deterministic.

---

# 56. Slice Tests

Spring test slices may be used for focused framework integration.

Examples:

- `@WebMvcTest`
- `@DataJpaTest`
- JSON serialization tests
- security-filter tests

Slice tests must not silently become full application tests through excessive imports.

---

# 57. Integration Tests

Full integration tests may use:

```java
@SpringBootTest
```

when validation requires multiple real layers.

Integration tests should use production-compatible dependencies through Testcontainers where applicable.

Mocks should not replace the infrastructure behavior being validated.

---

# 58. Context Caching

Test context reuse should be preserved where practical.

Avoid unnecessary:

- custom test configurations
- dynamic profiles
- dirty contexts
- per-test application startup
- random configuration variants

Excessive Spring context creation significantly increases build time.

---

# 59. Mocking Spring Internals

Tests should not mock Spring framework internals.

Avoid mocking:

- `ApplicationContext`
- transaction managers
- servlet containers
- framework proxies
- Spring Data internals

Test business code directly or use an appropriate Spring test scope.

---

# 60. Architecture Tests

Architecture tests must enforce framework boundaries.

Examples:

- Domain must not depend on Spring
- Domain must not contain Spring annotations
- controllers must not access Spring Data repositories
- JPA entities must remain in Infrastructure
- Application must not depend on Interface
- broker classes must not appear in Domain
- REST DTOs must not be used as domain entities

---

# 61. Dependency Management

Spring Boot dependency management will define compatible versions for supported Spring ecosystem dependencies.

Version overrides must be minimized.

An override requires:

- documented reason
- compatibility validation
- security review where applicable
- removal plan when temporary

Uncoordinated framework dependency versions are prohibited.

---

# 62. Spring Boot Upgrades

Spring Boot upgrades must be planned and tested.

Upgrade activities include:

- release-note review
- dependency-impact review
- deprecated API review
- configuration-property review
- security changes
- serialization changes
- persistence behavior
- test-framework compatibility
- observability-agent compatibility
- container validation
- performance regression testing

Major upgrades may require a dedicated ADR or migration plan.

---

# 63. Dependency Minimization

Only required Spring Boot starters should be included.

Avoid broad starters that introduce unused capabilities.

Every starter affects:

- dependency size
- auto-configuration
- attack surface
- startup
- memory
- upgrade complexity

Unused starters and transitive dependencies should be removed.

---

# 64. Circular Dependencies

Circular bean dependencies are prohibited.

The platform must not enable circular-reference support to hide design problems.

Circular dependencies usually indicate:

- mixed responsibilities
- incorrect layering
- bidirectional orchestration
- oversized services
- hidden domain coupling

They must be resolved through architectural redesign.

---

# 65. Bean Scope

Singleton is the default Spring bean scope.

Other scopes require explicit justification.

Mutable state must not be stored in singleton service beans unless it is:

- thread-safe
- intentionally shared
- operationally controlled
- clearly documented

Request-specific state belongs in method parameters or approved request-scoped components.

---

# 66. Thread Safety

Spring-managed singleton components must be thread-safe.

Avoid mutable fields such as:

```java
private String currentOrderId;
```

inside singleton services.

State should remain local to method execution or reside in properly synchronized infrastructure components.

---

# 67. Proxy Limitations

Framework proxies affect:

- transactions
- method security
- caching
- asynchronous execution
- retry
- circuit breakers

Developers must not assume annotations work during:

- self-invocation
- private method invocation
- direct object construction
- unmanaged execution
- unsupported final-method scenarios depending on proxy type

Critical behavior must be integration-tested.

---

# 68. Native Image

Native-image compilation is not a platform requirement.

It may be evaluated when there is a measurable need for:

- reduced startup
- lower memory
- short-lived workloads
- scale-to-zero behavior
- command-line tools

Adoption requires separate analysis because it affects:

- reflection
- serialization
- proxies
- agents
- dynamic class loading
- build complexity
- debugging

---

# 69. Developer Tools

Development-only Spring tools may be used locally when they do not enter production artifacts.

Examples:

- development reload support
- local Docker Compose integration
- test-specific configuration helpers

Production dependencies must remain separate from development conveniences.

---

# 70. Production Defaults

Spring Boot defaults must be reviewed for production suitability.

Areas requiring explicit review include:

- server timeouts
- request limits
- connection pools
- thread pools
- multipart limits
- error exposure
- actuator exposure
- logging levels
- compression
- graceful shutdown
- proxy headers
- session behavior
- CORS
- database initialization
- Open Session in View

---

# 71. Performance

Framework performance must be evaluated in the context of the full application.

Relevant measures include:

- startup time
- memory
- request throughput
- latency
- connection usage
- serialization cost
- transaction duration
- query count
- external-client concurrency
- message-processing throughput

Framework tuning must be evidence-based.

---

# 72. Security Hardening

Spring Boot applications must apply security hardening such as:

- dependency updates
- minimal endpoint exposure
- secure headers
- restricted actuator access
- explicit CORS
- disabled unnecessary management endpoints
- masked sensitive configuration
- secure cookie behavior where applicable
- input-size limits
- safe serialization
- proper token validation
- least-privilege authorization

---

# 73. Positive Consequences

The decision provides:

- rapid development
- mature enterprise capabilities
- Java 21 support
- broad infrastructure integration
- strong security tooling
- standardized configuration
- strong test support
- production health endpoints
- extensive documentation
- large developer community
- reduced custom framework code
- consistent dependency management
- easier onboarding
- strong container support
- compatibility with existing organizational experience

---

# 74. Negative Consequences

The decision introduces:

- framework dependency
- increased runtime footprint compared with lighter frameworks
- risk of annotation-driven design
- risk of hidden auto-configuration
- proxy-related complexity
- upgrade coordination requirements
- large dependency graphs
- possible startup overhead
- need for strict architectural boundaries
- potential misuse of full application-context tests

These costs are accepted because of the framework's maturity, ecosystem and operational value.

---

# 75. Neutral Consequences

The decision also means:

- Spring Boot becomes the standard outer-layer framework
- application startup behavior depends on managed configuration
- framework upgrades become part of lifecycle maintenance
- Spring-specific knowledge is required for Infrastructure and Interface work
- domain modeling remains independent from the framework
- production behavior must account for proxies and auto-configuration
- framework conventions may be used when they remain compatible with architecture rules

---

# 76. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Domain becomes Spring-coupled | High | Medium | Enforce architecture tests |
| Controllers contain business logic | High | Medium | Require use-case boundaries and code review |
| Auto-configuration hides behavior | Medium | Medium | Document critical configuration and validate startup |
| Excessive full-context testing | Medium | High | Use unit and slice tests |
| Circular dependencies | High | Medium | Prohibit circular references |
| Proxy behavior is misunderstood | High | Medium | Integration-test transactions and security |
| Dependency graph becomes excessive | Medium | Medium | Minimize starters and inspect dependencies |
| Upgrade introduces regressions | High | Medium | Use staged upgrade and regression testing |
| Default settings are unsuitable | High | Medium | Review production configuration explicitly |
| Framework exceptions leak to API | Medium | Medium | Centralize exception translation |
| Blocking and reactive models are mixed | High | Low | Define explicit integration boundaries |
| Unbounded asynchronous work | High | Medium | Configure managed executors and limits |
| Sensitive actuator exposure | High | Low | Restrict management endpoints |
| Transactional annotations fail through self-invocation | High | Medium | Design explicit transactional services and test them |

---

# 77. Implementation Guidance

The following rules are mandatory:

1. Spring Boot is used as the application framework
2. Domain code must remain Spring-independent
3. constructor injection is mandatory
4. field injection is prohibited
5. configuration must use typed properties
6. critical configuration must be validated at startup
7. controllers must remain thin
8. application use cases own orchestration
9. Spring Data remains inside Infrastructure
10. JPA entities must not cross infrastructure boundaries
11. Open Session in View must be disabled
12. transactions belong at application-service boundaries
13. external calls must use explicit timeouts
14. asynchronous execution requires managed executors
15. retries must be bounded and idempotency-aware
16. health and actuator endpoints must be secured
17. unit tests should not start Spring
18. full-context tests must be justified
19. architecture tests must enforce framework boundaries
20. circular dependencies are prohibited
21. production defaults must be reviewed explicitly
22. framework dependency versions must remain aligned
23. sensitive information must not be exposed through configuration or diagnostics
24. major deviations require architecture review

---

# 78. Validation

The decision will be validated through:

- architecture tests
- dependency analysis
- code review
- Spring context startup tests
- API integration tests
- persistence integration tests
- security tests
- configuration validation tests
- Testcontainers-based infrastructure tests
- actuator endpoint review
- transaction-boundary tests
- performance tests
- production-readiness review
- vulnerability scanning
- upgrade rehearsal

---

# 79. Success Criteria

The decision is successful when:

- services start consistently across environments
- Domain compiles without Spring dependencies
- controllers delegate to application use cases
- Spring Data remains isolated in Infrastructure
- configuration errors fail fast
- production endpoints are secured
- transaction behavior is predictable
- external clients use explicit operational policies
- integration tests validate framework behavior
- unit tests remain fast and framework-independent
- framework upgrades can be performed predictably
- no circular bean dependencies exist
- observability and health checks support operations
- application code remains compatible with Clean Architecture
- Spring Boot accelerates delivery without owning the business model

---

# 80. Alternatives Rejected

## 80.1 Quarkus

Rejected because the platform already has stronger alignment with Spring and no current operational requirement justifies the ecosystem and migration cost.

---

## 80.2 Micronaut

Rejected because the reduced runtime footprint does not outweigh the lower organizational familiarity and smaller established integration ecosystem for this platform.

---

## 80.3 Jakarta EE

Rejected because Spring Boot better aligns with the selected containerized deployment, development workflow and ecosystem requirements.

---

## 80.4 Minimal Custom Framework

Rejected because it would require significant custom infrastructure, increase security and maintenance risk and reduce delivery speed.

---

# 81. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-007: Adopt Transactional Outbox
- ADR-009: Use Kafka for Integration Events
- ADR-010: Use Redis for Distributed Caching
- ADR-011: Use OAuth 2.0 and OpenID Connect
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Use OpenTelemetry for Distributed Tracing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Use Problem Details for API Errors

---

# 82. References

- Spring Boot reference documentation
- Spring Framework reference documentation
- Spring Security reference documentation
- Spring Data documentation
- Spring for Apache Kafka documentation
- Spring Boot Actuator documentation
- Micrometer documentation
- Enterprise Order Platform Spring Boot guidelines
- Enterprise Order Platform coding standards
- Enterprise Order Platform testing standards
- Enterprise Order Platform security guidelines
- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21

---

# 83. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial application framework baseline |

---

# 84. Decision Summary

The Enterprise Order Platform adopts Spring Boot as its standard application framework.

Spring Boot will provide:

```text
Application bootstrap

Dependency injection

REST interfaces

Configuration

Transactions

Security integration

Persistence integration

Messaging integration

Caching integration

Observability

Testing support
```

Spring Boot must remain concentrated in the outer architectural layers.

The Domain layer must remain independent from Spring.

Application services may use selected Spring capabilities for orchestration and transactions, while Infrastructure contains concrete framework integrations.

This decision establishes Spring Boot as the implementation framework without allowing it to replace the platform's Clean Architecture and Domain-Driven Design foundations.
