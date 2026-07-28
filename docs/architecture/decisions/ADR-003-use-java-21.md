# ADR-003: Use Java 21

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-003 |
| Title | Use Java 21 |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Application Platform |
| Related Work Items | Initial platform architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform requires a stable, modern and long-term supported runtime for implementing business-critical backend services.

The platform is expected to support:

- long-lived business applications
- high-throughput APIs
- asynchronous processing
- domain-oriented models
- secure integrations
- concurrent workloads
- containerized deployments
- automated quality controls
- gradual architectural evolution
- long-term maintainability

The programming language and runtime influence:

- developer productivity
- application performance
- concurrency design
- framework compatibility
- library availability
- security maintenance
- deployment footprint
- support lifecycle
- operational stability
- modernization capability

The platform requires a Java version that provides a strong long-term support baseline while enabling modern language and runtime capabilities.

---

# 2. Problem Statement

The platform must select a standard Java version that:

- is suitable for enterprise production workloads
- has long-term support
- is compatible with the selected Spring Boot generation
- provides modern language features
- supports efficient concurrency
- receives security updates
- is supported by build and analysis tools
- works consistently in local, CI and production environments
- reduces reliance on legacy patterns
- provides a stable foundation for future platform evolution

The decision must define the mandatory Java version for source code, compilation, testing, runtime execution and container images.

---

# 3. Decision Drivers

The primary decision drivers are:

1. long-term support
2. enterprise ecosystem compatibility
3. security maintenance
4. performance
5. concurrency capabilities
6. developer productivity
7. language expressiveness
8. framework support
9. tooling maturity
10. container compatibility
11. operational stability
12. backward compatibility
13. modernization capability
14. talent availability
15. predictable upgrade strategy

---

# 4. Constraints

The decision must consider:

- Spring Boot is the primary application framework
- Gradle is used for build automation
- PostgreSQL is the primary transactional database
- SQS and Redis may be used
- production workloads run in containers
- CI/CD must use the same Java major version as production
- code quality is enforced through SonarQube and static analysis
- integration tests may use Testcontainers
- legacy Java patterns should not constrain new code
- runtime behavior must remain deterministic across environments
- the platform must avoid unsupported Java releases
- the selected version must be broadly supported by common observability and security tooling

---

# 5. Considered Options

## 5.1 Option A: Java 17

Java 17 is a Long-Term Support release widely used in enterprise applications.

### Advantages

- mature ecosystem
- broad framework compatibility
- extensive production adoption
- stable tooling
- long support horizon
- lower migration effort from older Java versions
- strong container support

### Disadvantages

- lacks later language improvements
- lacks virtual threads as a final production feature
- provides fewer modern APIs than Java 21
- establishes an older baseline for a new platform
- may require an earlier future upgrade

---

## 5.2 Option B: Java 21

Java 21 is a Long-Term Support release providing modern language and runtime capabilities.

### Advantages

- long-term support
- final virtual-thread support
- modern pattern matching
- improved switch expressions and pattern-based code
- record support
- sealed classes
- improved runtime performance
- current enterprise framework support
- strong modernization baseline
- reduced need for custom concurrency infrastructure
- improved code expressiveness
- longer useful lifecycle for a new platform

### Disadvantages

- some older libraries may require upgrades
- teams migrating from legacy Java may require training
- virtual threads may be misused without concurrency discipline
- runtime and build environments must be upgraded consistently
- unsupported legacy application servers may not be compatible

---

## 5.3 Option C: Java 22 or a Later Non-LTS Release

The platform could use the latest available Java release.

### Advantages

- earliest access to new language and runtime features
- potentially improved performance
- faster experimentation
- shorter delay between JVM innovation and adoption

### Disadvantages

- short support lifecycle
- frequent mandatory upgrades
- greater ecosystem compatibility risk
- less predictable production support
- higher operational maintenance
- reduced suitability for long-lived enterprise services
- increased CI and container-image churn

---

## 5.4 Option D: Remain on a Legacy Java Version

The platform could retain Java 8 or Java 11 for compatibility with older systems.

### Advantages

- compatibility with legacy libraries
- reduced immediate migration effort
- broad historical ecosystem support
- familiar development patterns

### Disadvantages

- older language model
- weaker modernization baseline
- increased security and support risk
- limited framework compatibility
- reduced concurrency options
- greater technical debt
- more verbose code
- restricted adoption of current Spring Boot versions
- increased long-term maintenance cost

---

## 5.5 Option E: Use Kotlin on the JVM

The platform could adopt Kotlin as the primary JVM language.

### Advantages

- concise syntax
- strong null-safety features
- Java interoperability
- expressive domain modeling
- mature JVM ecosystem access

### Disadvantages

- additional language standard
- increased onboarding requirements
- mixed-language governance complexity
- additional build and tooling considerations
- smaller enterprise talent pool in some environments
- migration complexity for Java-oriented teams
- no clear business need requiring language replacement

---

# 6. Decision

The Enterprise Order Platform will use Java 21 as the mandatory Java version.

Java 21 will be used for:

- source code
- compilation
- unit tests
- integration tests
- architecture tests
- static analysis
- local development
- CI pipelines
- runtime containers
- production execution

The project must compile with:

```text
Java language level: 21

Java bytecode target: 21
```

The build must fail when an incompatible Java runtime or compiler is used.

---

# 7. Rationale

Java 21 was selected because it provides the strongest balance of:

- long-term support
- ecosystem maturity
- modern language capabilities
- concurrency improvements
- enterprise framework compatibility
- security maintenance
- operational predictability

The platform is being designed as a new long-lived system rather than a temporary compatibility layer.

Using Java 21 avoids creating a new platform on an already older baseline and reduces the need for a near-term runtime migration.

Java 21 also supports architectural objectives such as:

- immutable domain models
- explicit result types
- expressive state handling
- controlled concurrency
- reduced boilerplate
- strong type modeling
- clean application boundaries

---

# 8. Required Java Features

The platform may use Java 21 features when they improve readability, correctness or performance.

Approved features include:

- records
- sealed classes
- pattern matching
- switch expressions
- text blocks
- local variable type inference
- virtual threads
- improved collection APIs
- immutable collection factories
- enhanced null-safe modeling through explicit types
- modern date and time APIs

Features must be used intentionally rather than only because they are available.

---

# 9. Records

Records should be used for immutable data carriers.

Appropriate examples include:

- commands
- queries
- responses
- value objects
- integration event envelopes
- configuration projections
- test fixtures
- small immutable results

Example:

```java
public record ApproveOrderCommand(
        OrderId orderId,
        ActorId actorId,
        ApprovalComment comment
) {
}
```

Records should not be used when the type requires:

- complex mutable lifecycle
- inheritance
- framework-driven proxying incompatible with records
- identity-based entity behavior
- unrestricted state evolution

---

# 10. Sealed Types

Sealed types may be used when the set of implementations is intentionally restricted.

Example:

```java
public sealed interface OrderOperationResult
        permits OrderApprovedResult,
                OrderRejectedResult,
                OrderConflictResult {
}
```

Sealed hierarchies are appropriate for:

- explicit result models
- controlled domain outcomes
- state variants
- event families
- command outcomes

They should not be used when third-party or independent extension is expected.

---

# 11. Pattern Matching

Pattern matching may be used to simplify explicit type handling.

Example:

```java
if (result instanceof OrderApprovedResult approvedResult) {
    return mapper.toResponse(approvedResult);
}
```

Pattern matching should improve clarity.

Complex nested type branching should be reviewed for possible polymorphic design.

---

# 12. Switch Expressions

Switch expressions should be preferred when mapping a complete set of known values.

Example:

```java
return switch (status) {
    case CREATED -> OrderAction.SUBMIT;
    case PENDING_APPROVAL -> OrderAction.APPROVE;
    case APPROVED -> OrderAction.FULFILL;
    case CANCELLED -> OrderAction.NONE;
};
```

Switch expressions should:

- cover all valid cases
- avoid silent default branches when an enum is expected to be exhaustive
- return explicit values
- remain small and readable

---

# 13. Virtual Threads

Virtual threads may be used for workloads that are:

- I/O-bound
- request-oriented
- blocking by design
- highly concurrent
- compatible with thread-per-task execution

Appropriate examples include:

- parallel external service validation
- concurrent independent HTTP calls
- high-concurrency request processing
- bounded batch operations with blocking I/O
- report generation involving independent data retrieval

Virtual threads must not be treated as a replacement for:

- timeouts
- bulkheads
- rate limits
- connection-pool sizing
- downstream-capacity planning
- idempotency
- backpressure
- bounded task creation
- cancellation handling

---

# 14. Virtual Thread Rules

When virtual threads are used:

- concurrency must remain bounded at business or dependency boundaries
- downstream connection pools must be sized intentionally
- task ownership must be explicit
- executor lifecycle must be managed
- security context must be propagated where required
- request context must be propagated where required
- failures must be aggregated correctly
- cancellation behavior must be defined
- tests must avoid timing-based assumptions
- performance claims must be measured

Example:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    var customerFuture = executor.submit(customerValidation);
    var inventoryFuture = executor.submit(inventoryValidation);

    customerFuture.get();
    inventoryFuture.get();
}
```

A shared Spring-managed executor is preferred when lifecycle, context propagation and observability must be centralized.

---

# 15. Concurrency Boundaries

Java 21 makes concurrency easier to express but does not remove concurrency risks.

The platform must still define:

- maximum parallel operations
- ordering requirements
- retry behavior
- timeout behavior
- shared-state ownership
- connection limits
- queue limits
- transaction boundaries
- cancellation behavior
- error aggregation

Unbounded task submission is prohibited for externally driven collections.

---

# 16. Structured Concurrency

Experimental or preview APIs must not be used in production code unless explicitly approved through a separate ADR.

The platform may evaluate structured concurrency when it becomes sufficiently stable and supported by the selected Java baseline.

Until then, task grouping and lifecycle must use approved production APIs.

---

# 17. Preview Features

Java preview features are prohibited in production code by default.

They may be used only for:

- isolated experiments
- proof-of-concept branches
- architectural evaluation
- non-production research

Production adoption requires:

- a dedicated ADR
- framework compatibility validation
- compiler and runtime configuration
- CI support
- operational support
- migration strategy if the feature changes before finalization

---

# 18. Language Style

Java code must favor:

- explicit business naming
- immutability
- constructor validation
- small cohesive methods
- controlled nullability
- explicit result types
- domain-specific value objects
- meaningful exceptions
- deterministic behavior
- side-effect isolation

Modern syntax must not reduce clarity.

---

# 19. Immutability

Immutability is preferred for:

- value objects
- commands
- queries
- integration events
- API responses
- configuration values
- collection views
- test constants

Use:

```java
List.copyOf(items)
```

instead of exposing mutable internal collections.

Mutable state should be limited to objects with a real lifecycle, such as aggregate roots.

---

# 20. Null Handling

The platform must avoid uncontrolled null usage.

Rules:

- validate mandatory values at boundaries
- use explicit nullable annotations where adopted
- use `Optional` primarily for return values
- do not use `Optional` for entity fields by default
- do not use `Optional` as a method parameter by default
- do not return `null` from methods declared to return `Optional`
- do not call `Optional.get()` without proving presence
- use domain-specific absence semantics where relevant

Preferred:

```java
return repository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
```

---

# 21. Optional Usage

Appropriate:

```java
Optional<Order> findById(OrderId orderId);
```

Avoid:

```java
void approve(Optional<OrderId> orderId);
```

Avoid:

```java
private Optional<String> description;
```

Optional should communicate possible absence at an API boundary rather than become a universal null replacement.

---

# 22. Local Variable Type Inference

`var` may be used when the type is obvious from the right-hand side.

Preferred:

```java
var order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
```

Avoid:

```java
var result = process(data);
```

when the inferred type is not clear from context.

`var` must not reduce readability.

---

# 23. Collections

Prefer immutable or unmodifiable collection boundaries.

Use:

```java
List.of(...)

Set.of(...)

Map.of(...)

List.copyOf(...)
```

Be aware that these factories reject null elements.

Mutable collections may be used internally when required by aggregate behavior, but they must not be exposed directly.

---

# 24. Stream API

Streams may be used for:

- transformations
- filtering
- grouping
- aggregation
- declarative collection processing

Streams should not be used when they make control flow difficult to understand.

Avoid:

- deeply nested pipelines
- hidden side effects
- exception-heavy stream logic
- parallel streams without performance evidence
- large pipelines combining unrelated responsibilities

A simple loop is acceptable when clearer.

---

# 25. Parallel Streams

Parallel streams are prohibited by default in application code.

Reasons include:

- use of the common fork-join pool
- difficult context propagation
- unpredictable downstream load
- limited lifecycle control
- poor observability
- interference with unrelated tasks

Explicit executors or controlled concurrency mechanisms must be used instead.

---

# 26. Date and Time

Use the `java.time` API.

Preferred types:

```text
Instant

OffsetDateTime

LocalDate

LocalDateTime

Duration

ZoneId
```

Selection depends on business semantics.

Guidance:

- use `Instant` for machine timestamps
- use `OffsetDateTime` when the offset is part of the contract
- use `LocalDate` for date-only business concepts
- use `Duration` for timeout and elapsed-time values
- inject `Clock` when current time affects behavior

Avoid:

```text
java.util.Date

java.sql.Date

Calendar
```

unless required at an isolated integration boundary.

---

# 27. Clock

Business logic that depends on current time must receive a `Clock` or explicit time value.

Example:

```java
public final class OrderExpirationPolicy {

    private final Clock clock;

    public OrderExpirationPolicy(Clock clock) {
        this.clock = clock;
    }

    public boolean isExpired(Instant expiration) {
        return expiration.isBefore(clock.instant());
    }
}
```

This supports deterministic testing.

---

# 28. UUID Usage

UUIDs may be used for distributed identifiers.

Rules:

- identifiers should be wrapped in domain-specific value objects
- random identifiers should be generated at controlled boundaries
- tests should use deterministic constants
- raw UUIDs should not replace meaningful domain types
- parsing failures must be handled explicitly
- database index impact must be considered

Preferred:

```java
public record OrderId(UUID value) {
}
```

Avoid passing raw UUIDs throughout the entire domain model.

---

# 29. Exception Handling

Java exceptions must preserve causes.

Preferred:

```java
catch (SQLException exception) {
    throw new OrderPersistenceException(
            "Failed to persist order",
            exception
    );
}
```

Avoid:

```java
catch (SQLException exception) {
    throw new RuntimeException("Error");
}
```

Exceptions must not be silently ignored.

---

# 30. Checked and Unchecked Exceptions

Domain and application failures will generally use unchecked exceptions.

Checked exceptions may remain at integration boundaries when required by third-party APIs, but should be translated before crossing architectural boundaries.

Use exceptions for exceptional conditions rather than normal branching.

---

# 31. Resource Handling

Use try-with-resources for all closeable resources.

Example:

```java
try (var input = fileSystem.open(path)) {
    return parser.parse(input);
}
```

Resources must not depend on garbage collection for cleanup.

---

# 32. Serialization

Serialization must use explicit contracts.

Rules:

- avoid serializing domain aggregates directly
- use dedicated API and event models
- define date and time formats
- validate unknown-field behavior
- avoid relying on constructor parameter names without compiler support
- test serialization compatibility
- avoid unsafe polymorphic deserialization

Records may be used for immutable serialization models when supported by the serialization framework.

---

# 33. Reflection

Reflection should be minimized.

Acceptable uses include:

- framework integration
- dependency injection
- serialization
- testing tools
- architecture analysis

Custom reflection-based business logic requires strong justification.

Avoid bypassing encapsulation through methods such as:

```java
setAccessible(true)
```

unless isolated, reviewed and technically unavoidable.

---

# 34. Annotations

Annotations should not replace explicit domain behavior.

Framework annotations belong primarily in outer layers.

Examples:

```text
@RestController

@Service

@Repository

@Entity

@Configuration
```

Domain classes should remain free from infrastructure annotations.

---

# 35. Lombok

Lombok will not be required as a platform standard.

Its use should be restricted or avoided when native Java features provide sufficient alternatives.

Preferred alternatives include:

- records
- explicit constructors
- explicit factory methods
- IDE-generated methods
- immutable value objects

Risks of broad Lombok usage include:

- hidden generated behavior
- accidental public setters
- equality mistakes on entities
- reduced source-code transparency
- annotation-processing dependency
- inconsistent team usage

Any Lombok adoption must be defined by a separate decision or coding standard.

---

# 36. Build Configuration

Gradle must enforce Java 21 through a toolchain.

Example:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

The build should not depend on whichever Java version happens to be installed locally.

The toolchain must apply to:

- compilation
- tests
- static analysis
- generated sources
- integration tests

---

# 37. Compiler Configuration

Compiler settings should include:

- UTF-8 source encoding
- parameter metadata when required
- warnings enabled
- deprecation visibility
- reproducible compilation
- no preview features in production builds

Example:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
```

Warnings must be reviewed rather than globally disabled.

---

# 38. Runtime Configuration

The runtime must use a Java 21-compatible JVM distribution.

Allowed distributions should be approved based on:

- support
- security updates
- container availability
- operational compatibility
- licensing
- performance
- observability-agent compatibility

The platform must not rely on vendor-specific JVM behavior without documentation.

---

# 39. Container Images

Runtime container images must:

- use Java 21
- use an approved JRE or minimal runtime
- run as a non-root user
- avoid build tools in the runtime layer
- use immutable version references
- receive vulnerability scanning
- expose JVM configuration explicitly
- support container-aware memory behavior

Build and runtime stages should remain separate.

---

# 40. JVM Configuration

JVM configuration must be externally configurable.

Consider:

- heap sizing
- container memory
- garbage collector behavior
- heap dumps
- out-of-memory behavior
- diagnostic options
- timezone
- file encoding
- entropy source
- TLS configuration

Avoid hardcoding environment-specific JVM options inside application artifacts.

---

# 41. Garbage Collection

The default Java 21 garbage collector may be used unless workload evidence justifies another collector.

Collector changes require:

- benchmark evidence
- production-like load testing
- pause-time analysis
- throughput analysis
- memory analysis
- operational review

A different collector must not be selected solely from generic recommendations.

---

# 42. Memory Management

Java 21 does not eliminate memory-management responsibilities.

The platform must still prevent:

- unbounded collections
- retained request payloads
- unbounded caches
- unbounded task queues
- oversized serialization buffers
- unreleased resources
- thread-local leaks
- excessive object allocation in critical paths

Memory-sensitive behavior must be measured.

---

# 43. Native Memory

Operational analysis must consider native memory used by:

- thread stacks
- direct buffers
- class metadata
- code cache
- compression libraries
- TLS
- observability agents
- database drivers

Heap size alone does not represent total container memory usage.

Virtual threads reduce thread-stack costs but do not make concurrency free.

---

# 44. Security Updates

The Java runtime must receive regular security updates.

Patch updates should be:

- monitored
- evaluated
- tested
- applied through controlled dependency and image updates
- tracked through CI/CD
- validated against framework and agent compatibility

Remaining on Java 21 does not mean remaining on one fixed Java 21 patch release.

---

# 45. Version Consistency

The following environments must use compatible Java 21 patch levels:

- developer environments
- CI
- test
- staging
- production
- build containers
- runtime containers

Patch levels do not need to be byte-for-byte identical in every environment, but differences must not be unmanaged.

Production behavior must not depend on a locally installed JVM.

---

# 46. IDE Configuration

Project configuration should communicate Java 21 requirements to supported IDEs.

The repository should include or document:

- Gradle toolchain
- source compatibility
- target compatibility
- recommended IDE version
- annotation-processing policy
- formatting configuration
- test execution commands

IDE configuration must not replace build enforcement.

---

# 47. Framework Compatibility

Libraries and frameworks must explicitly support Java 21.

This includes:

- Spring Boot
- Spring Framework
- Hibernate
- PostgreSQL driver
- SQS client
- Redis client
- Testcontainers
- SonarQube scanner
- static-analysis tools
- code-coverage agents
- observability agents
- mocking frameworks
- container plugins

Unsupported dependencies must be upgraded, replaced or isolated.

---

# 48. Library Selection

New libraries must be evaluated for:

- Java 21 support
- maintenance activity
- security record
- license
- module compatibility
- reflection requirements
- native-image assumptions where relevant
- virtual-thread behavior where relevant
- transitive dependency impact

Legacy compatibility alone is not sufficient justification for introducing an outdated library.

---

# 49. Testing Requirements

Tests must run on Java 21.

This includes:

- unit tests
- integration tests
- architecture tests
- mutation tests where adopted
- performance tests
- contract tests
- security tests

Running tests on an older JVM does not satisfy the platform baseline.

---

# 50. Test Determinism

Java 21 features must be tested deterministically.

Avoid:

- random UUID generation without control
- system time without `Clock`
- `Thread.sleep`
- timing-dependent concurrency assertions
- unbounded asynchronous waiting
- dependence on test execution order
- shared mutable global state

Concurrency tests should use:

- latches
- barriers
- deterministic executors where appropriate
- bounded timeouts
- explicit completion signals

---

# 51. Virtual Thread Testing

Virtual-thread tests must validate:

- task completion
- exception propagation
- cancellation
- context propagation
- bounded external-resource usage
- duplicate handling
- transaction boundaries
- absence of timing assumptions

Tests must not assert implementation details such as the exact runtime thread name unless that behavior is part of a defined contract.

---

# 52. Static Analysis

Static-analysis tools must support Java 21 bytecode and syntax.

The build must fail when:

- analysis cannot understand the Java version
- generated reports are incomplete
- critical findings are introduced
- quality-gate requirements are not met

Tool incompatibility is not a valid reason to disable quality checks.

---

# 53. Code Coverage

Coverage tooling must support Java 21.

JaCoCo or an equivalent tool must be configured with a compatible version.

Coverage requirements remain defined by the testing standards and Definition of Done.

Modern language constructs must not be excluded merely because they require updated analysis tooling.

---

# 54. Observability Agents

Java agents used for:

- tracing
- metrics
- profiling
- security monitoring
- application performance monitoring

must support Java 21.

Agent compatibility must be validated before production deployment.

Agents may affect startup, memory and runtime performance and must be included in realistic testing.

---

# 55. Performance Validation

Java 21 performance claims must be validated using production-representative workloads.

Benchmarks should consider:

- throughput
- P50 latency
- P95 latency
- P99 latency
- memory
- CPU
- garbage collection
- connection-pool usage
- native memory
- startup time
- virtual-thread concurrency behavior

Microbenchmarks must use JMH or an equivalent methodology.

---

# 56. Backward Compatibility

Java 21 source code may use Java 21 capabilities.

The platform will not target older Java bytecode for compatibility with legacy runtimes.

Legacy integrations must occur through:

- network contracts
- files
- messages
- database boundaries where explicitly approved

They must not force the core platform to run on an obsolete Java version.

---

# 57. Migration from Older Java Versions

Migration from older Java versions should be incremental.

Recommended sequence:

1. update build tooling
2. configure Java toolchain
3. update framework versions
4. update incompatible dependencies
5. update tests and static analysis
6. update container images
7. resolve removed or strongly encapsulated APIs
8. validate reflection usage
9. execute the full test suite
10. execute integration tests
11. execute performance tests
12. deploy gradually
13. monitor production behavior
14. adopt new language features incrementally

A version upgrade should not be combined with uncontrolled broad refactoring unless explicitly planned.

---

# 58. Legacy API Removal

Code using obsolete or internal JDK APIs must be replaced.

Examples include:

- internal `sun.*` APIs
- deprecated date and time APIs
- unsupported reflection access
- finalization-dependent cleanup
- obsolete security algorithms
- old concurrency utilities when safer alternatives exist

Use of internal JDK APIs requires explicit approval and migration planning.

---

# 59. Feature Adoption Strategy

The platform will not require immediate use of every Java 21 feature.

Adoption should be prioritized by value.

Recommended order:

1. records for immutable boundary models
2. switch expressions
3. pattern matching
4. sealed result hierarchies
5. immutable collection factories
6. explicit `Clock` usage
7. virtual threads for validated I/O-bound workloads
8. further features based on demonstrated benefit

---

# 60. Coding Standards Relationship

This ADR defines the Java runtime and language baseline.

Detailed implementation conventions remain defined in:

```text
docs/standards/java-guidelines.md

docs/standards/coding-standards.md

docs/standards/testing-standards.md
```

Those standards must remain compatible with this ADR.

---

# 61. Positive Consequences

The decision provides:

- long-term support
- modern language capabilities
- final virtual-thread support
- improved developer productivity
- reduced boilerplate
- stronger immutable modeling
- better expression of domain outcomes
- current framework compatibility
- longer platform lifecycle
- improved concurrency options
- strong security-maintenance baseline
- better container-runtime support
- easier recruitment within the Java ecosystem
- lower pressure for an early major-version upgrade

---

# 62. Negative Consequences

The decision introduces:

- required migration of incompatible libraries
- required developer and CI environment upgrades
- training needs for modern Java features
- risk of feature misuse
- possible incompatibility with legacy application servers
- increased need for concurrency governance
- need to update analysis and coverage tools
- possible changes in runtime behavior compared with older JVMs
- need for controlled patch-version maintenance

These costs are accepted because the platform requires a modern and maintainable baseline.

---

# 63. Neutral Consequences

The decision also means:

- older JVMs cannot execute the application
- legacy systems must integrate through contracts
- language modernization becomes available but not mandatory everywhere
- Java patch updates become part of platform maintenance
- build tools must manage the JDK explicitly
- production troubleshooting must include JVM-specific metrics
- virtual threads become an available option rather than a default solution

---

# 64. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Library incompatibility | High | Medium | Validate Java 21 support before adoption |
| CI and runtime version drift | High | Medium | Enforce Gradle toolchains and container baselines |
| Virtual thread overuse | High | Medium | Require bounded concurrency and performance validation |
| Developers misuse modern syntax | Medium | Medium | Enforce coding standards and code review |
| Static-analysis incompatibility | High | Low | Use Java 21-compatible tooling |
| Observability agent incompatibility | High | Low | Validate agents in production-like environments |
| Increased native memory under concurrency | High | Medium | Monitor total process memory and downstream pools |
| Legacy integration pressure | Medium | Medium | Integrate through explicit contracts |
| Preview features enter production | Medium | Low | Disable preview features in production builds |
| Unsupported runtime distribution | High | Low | Approve and standardize JVM distributions |
| Patch updates introduce regressions | Medium | Low | Use staged update and regression testing |
| Runtime behavior differs across environments | High | Medium | Standardize toolchains and container images |

---

# 65. Implementation Guidance

The following rules are mandatory:

1. all modules must compile with Java 21
2. Gradle toolchains must enforce Java 21
3. production containers must run Java 21
4. preview features are disabled by default
5. libraries must support Java 21
6. tests must execute on Java 21
7. static-analysis and coverage tools must support Java 21
8. records should be preferred for suitable immutable data carriers
9. domain entities must not be converted into records solely to reduce boilerplate
10. virtual threads require bounded-resource analysis
11. parallel streams are prohibited by default
12. business time must use injected `Clock` or explicit time
13. internal mutable collections must not be exposed
14. Java runtime patch levels must be maintained
15. unsupported JDK internals are prohibited
16. production code must not depend on preview APIs without a separate ADR
17. performance-sensitive changes require measurement
18. local development must not override the platform Java baseline

---

# 66. Validation

The decision will be validated through:

- Gradle toolchain enforcement
- CI runtime validation
- container-image inspection
- full test execution
- dependency compatibility checks
- static-analysis execution
- code-coverage execution
- Java-version startup logging
- architecture review
- security scanning
- production-like performance testing
- observability-agent validation
- periodic runtime patch review

---

# 67. Success Criteria

The decision is successful when:

- every module compiles with Java 21
- CI uses Java 21 consistently
- runtime images use Java 21
- production starts without compatibility warnings
- all selected frameworks support Java 21
- static analysis understands Java 21 source and bytecode
- code coverage operates correctly
- no unsupported JDK internal APIs are used
- domain models benefit from modern Java features
- concurrency improvements do not overload downstream systems
- Java patch updates can be applied predictably
- no legacy runtime constrains platform evolution
- developers can reproduce production-relevant behavior locally

---

# 68. Alternatives Rejected

## 68.1 Java 17

Rejected as the baseline for a new platform because Java 21 provides a newer Long-Term Support foundation and final virtual-thread support.

Java 17 remains technically viable but would shorten the useful modernization horizon.

---

## 68.2 Non-LTS Java Release

Rejected because the required upgrade frequency and shorter support lifecycle are not appropriate for the platform's operational model.

---

## 68.3 Legacy Java Version

Rejected because it would restrict framework versions, increase technical debt and weaken the long-term security and maintenance posture.

---

## 68.4 Kotlin

Rejected as the primary language because the platform already has strong Java alignment and no business requirement justifies introducing a second primary language.

Kotlin may be reconsidered only through a separate ADR.

---

# 69. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-004: Use Spring Boot
- ADR-013: Use Testcontainers for Integration Testing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-018: Use Conventional Commits

---

# 70. References

- Java 21 Language Specification
- Java 21 API Documentation
- Java Virtual Machine Specification
- Java Platform Module System documentation
- OpenJDK documentation
- Gradle Java Toolchains documentation
- Enterprise Order Platform Java guidelines
- Enterprise Order Platform coding standards
- Enterprise Order Platform testing standards
- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design

---

# 71. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial Java platform baseline |

---

# 72. Decision Summary

The Enterprise Order Platform adopts Java 21 as its mandatory language and runtime baseline.

Java 21 will be used for:

```text
Source compilation

Automated tests

Static analysis

CI execution

Container runtime

Production execution
```

The platform may use modern Java features such as:

```text
Records

Sealed types

Pattern matching

Switch expressions

Virtual threads
```

when they improve correctness, readability or measured performance.

Preview features remain disabled by default.

Gradle toolchains and container images must enforce the Java 21 baseline consistently across all environments.
