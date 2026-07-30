# ADR-052: Adopt Java 21 / Spring Boot Enterprise Coding and Clean Code Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-052 |
| Title | Adopt Java 21 / Spring Boot Enterprise Coding and Clean Code Standard |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Java 21, Spring Boot, Clean Code, Maintainability, SonarQube |
| Related Work Items | Java Modernization, Clean Code, Sonar, Testing, Architecture Fitness Functions |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The enterprise platform contains multiple Java/Spring Boot services maintained by different teams.

Without explicit implementation standards, services gradually diverge in areas such as:

```text
Dependency Injection

Null Handling

Optional

Records

DTOs

Entities

Exceptions

Logging

Streams

Lambdas

Collections

Immutability

Concurrency

Testing

Naming

Class Design

Method Complexity
```

Java 21 also provides language and runtime capabilities that can simplify code when used appropriately.

The objective is not to mandate every language feature.

The objective is to establish a maintainable, predictable and enterprise-grade Java baseline.

---

# 2. Problem Statement

The organization requires implementation standards covering:

- Java 21
- Spring Boot
- constructor injection
- dependency management
- immutability
- records
- Optional
- null safety
- collections
- defensive copies
- streams
- lambdas
- method references
- switch expressions
- pattern matching
- exceptions
- logging
- DTOs
- entities
- mappers
- enums
- constants
- builders
- utilities
- validation
- concurrency
- Virtual Threads
- class complexity
- method complexity
- SonarQube
- unit tests
- integration tests
- deterministic tests

---

# 3. Decision Drivers

Primary drivers are:

1. maintainability
2. readability
3. correctness
4. Java 21 modernization
5. Sonar compliance
6. testability
7. security
8. predictable architecture
9. reduced accidental complexity
10. consistent engineering practices

---

# 4. Decision

Enterprise Java services MUST follow a common Java 21 / Spring Boot coding baseline.

The target implementation model is:

```text
CLEAR DOMAIN MODEL
       +
EXPLICIT DEPENDENCIES
       +
IMMUTABLE DATA
       +
SMALL RESPONSIBILITIES
       +
CONTROLLED SIDE EFFECTS
       +
CLEAR ERROR HANDLING
       +
DETERMINISTIC TESTS
       +
AUTOMATED QUALITY GATES
       =
MAINTAINABLE JAVA
```

---

# 5. Fundamental Principle

The primary rule is:

```text
Prefer the simplest implementation
that correctly expresses the
business behavior and remains
easy to test, understand and change.
```

---

# 6. Java Baseline

New and modernized services MUST use the approved Java baseline.

Current baseline:

```text
Java 21
```

---

# 7. Language Features

Java 21 language features MAY be used when they improve clarity.

They MUST NOT be introduced merely to demonstrate use of a newer language feature.

---

# 8. Readability Over Novelty

Prefer:

```text
obvious code
```

over:

```text
clever code
```

---

# 9. Spring Boot

Services MUST use a supported Spring Boot version approved by the platform.

---

# 10. Framework Compatibility

Generated or manually implemented code MUST use APIs compatible with the actual Spring Boot/Spring Framework version in the repository.

---

# 11. Dependency Injection

Constructor injection is the standard for mandatory dependencies.

Preferred:

```java
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
```

---

# 12. Field Injection

Field injection is prohibited for normal application dependencies.

Avoid:

```java
@Autowired
private OrderRepository orderRepository;
```

---

# 13. Constructor Injection Benefits

Constructor injection provides:

- explicit dependencies
- immutability
- easier testing
- fail-fast construction
- reduced Spring coupling

---

# 14. Lombok Constructor Injection

Projects using approved Lombok conventions MAY use:

```java
@RequiredArgsConstructor
```

where this does not obscure class design.

---

# 15. Excessive Dependencies

A class with many constructor dependencies is an architectural signal.

---

# 16. Dependency Threshold

Classes approaching approximately:

```text
20 dependencies
```

MUST be reviewed for excessive responsibility.

The number is a review trigger rather than a reason to create artificial dependency containers.

---

# 17. Dependency Bag Anti-Pattern

Do not solve excessive constructor dependencies by introducing:

```java
class OrderDependencies {
    // 20 unrelated dependencies
}
```

solely to reduce constructor parameter count.

---

# 18. Single Responsibility

Classes SHOULD have a coherent responsibility.

---

# 19. God Classes

Large orchestration classes accumulating unrelated behavior SHOULD be decomposed.

---

# 20. Decomposition

Prefer decomposition by responsibility:

```text
Validation

Calculation

Persistence

Integration

Mapping

Workflow

Orchestration
```

rather than arbitrary line count.

---

# 21. Method Size

Methods SHOULD remain small enough that their intent is immediately understandable.

---

# 22. Method Extraction

Extract methods when doing so:

- names a meaningful operation
- reduces complexity
- removes duplication
- improves testability

---

# 23. Meaningless Extraction

Do not extract one-line methods solely to reduce a metric when the resulting abstraction has no semantic value.

---

# 24. Cognitive Complexity

Methods exceeding project Sonar cognitive-complexity limits MUST be refactored or explicitly justified.

---

# 25. Cyclomatic Complexity

High branching complexity SHOULD trigger decomposition or redesign.

---

# 26. Nested Conditionals

Deep nesting SHOULD be avoided.

Prefer guard clauses where they improve readability.

---

# 27. Guard Clause

Preferred:

```java
if (order == null) {
    return;
}

process(order);
```

over unnecessary nesting.

---

# 28. Boolean Expressions

Complex boolean expressions SHOULD be extracted into meaningful predicates.

Example:

```java
if (isEligibleForApproval(order, user)) {
    approve(order);
}
```

---

# 29. Ternary Operator

Ternary expressions SHOULD be limited to simple, immediately understandable expressions.

---

# 30. Nested Ternaries

Nested ternary expressions SHOULD NOT be used in production business logic.

---

# 31. Null

Nullability must be intentional.

---

# 32. Null Safety

Code MUST NOT assume non-null values without an established invariant.

---

# 33. Null Boundary

Null should preferably be handled close to the boundary where it enters the application.

---

# 34. Null Return

Methods SHOULD avoid returning `null` collections.

Prefer:

```java
List.of()
```

over:

```java
return null;
```

---

# 35. Optional

`Optional` SHOULD be used primarily to represent an intentionally absent return value.

---

# 36. Good Optional Usage

Example:

```java
Optional<Order> findById(UUID id);
```

---

# 37. Optional Fields

`Optional` SHOULD generally NOT be used as an entity or DTO field.

---

# 38. Optional Parameters

`Optional` SHOULD generally NOT be used as a method parameter.

Prefer explicit overloads or nullable/validated input according to the contract.

---

# 39. Optional.get

Avoid:

```java
optional.get();
```

without establishing presence.

---

# 40. Preferred Optional Access

Use appropriate operations such as:

```java
orElse

orElseGet

orElseThrow

map

flatMap

filter

ifPresent
```

when they improve clarity.

---

# 41. isPresent

When imperative logic is clearer, this is acceptable:

```java
if (optional.isPresent()) {
    var value = optional.get();
}
```

Presence MUST be established before `get()`.

---

# 42. Functional Ceremony

Do not force complex Optional chains when straightforward imperative code is easier to understand.

---

# 43. orElse vs orElseGet

Use `orElseGet` when fallback creation is expensive or should be lazy.

---

# 44. Records

Records SHOULD be considered for immutable data carriers.

Good candidates include:

```text
Request DTOs

Response DTOs

Events

Internal Value Carriers

Configuration Projections
```

---

# 45. Record Semantics

Use a record when the type fundamentally represents data rather than mutable identity/lifecycle behavior.

---

# 46. JPA Entities

JPA entities SHOULD NOT normally be records.

---

# 47. Record Validation

Compact constructors MAY enforce intrinsic record invariants.

---

# 48. Mutable Components in Records

A record is only shallowly immutable.

Mutable components require defensive handling where necessary.

---

# 49. Immutability

Immutable state SHOULD be preferred by default.

---

# 50. final

Fields that do not need reassignment SHOULD be `final`.

---

# 51. Mutable State

Mutable state must have a clear lifecycle and owner.

---

# 52. Defensive Copies

Mutable collections crossing ownership boundaries SHOULD use defensive copies.

---

# 53. List.copyOf

Example:

```java
this.orders = List.copyOf(orders);
```

when null elements and mutation semantics permit it.

---

# 54. Returning Collections

Internal mutable collections SHOULD NOT be exposed directly.

---

# 55. Unmodifiable Is Not Always Immutable

An unmodifiable view over a mutable collection may still change if the underlying collection changes.

Prefer actual copies when ownership isolation is required.

---

# 56. Collections

Use the most appropriate collection interface for the contract.

Prefer:

```java
List<Order>
```

over unnecessarily exposing:

```java
ArrayList<Order>
```

---

# 57. Empty Collections

Prefer standard empty immutable collections:

```java
List.of()

Set.of()

Map.of()
```

when appropriate.

---

# 58. Streams

Streams SHOULD be used when they make collection transformation clearer.

---

# 59. Good Stream Usage

Typical:

```java
orders.stream()
        .filter(Order::isActive)
        .map(Order::getId)
        .toList();
```

---

# 60. Stream Side Effects

Avoid side effects inside:

```java
map

filter

peek
```

---

# 61. peek

`peek` SHOULD primarily support observation/debugging in stream pipelines, not business mutation.

---

# 62. Complex Streams

A stream pipeline that is difficult to understand SHOULD be rewritten or decomposed.

---

# 63. Imperative Code

Imperative loops are fully acceptable when clearer.

---

# 64. Parallel Streams

`parallelStream()` MUST NOT be introduced without workload-specific justification.

---

# 65. Common ForkJoinPool

Remember that parallel streams normally use shared execution infrastructure.

This can create unpredictable resource contention.

---

# 66. Lambdas

Lambdas SHOULD remain concise.

---

# 67. Complex Lambda

A complex multi-step lambda SHOULD normally be extracted to a named method.

---

# 68. Test Lambdas

Test lambdas used for exception assertions SHOULD contain only the invocation relevant to the assertion where required by Sonar.

Example:

```java
assertThatThrownBy(() -> service.execute(request))
        .as("execute should reject an invalid request")
        .isInstanceOf(IllegalArgumentException.class);
```

---

# 69. Method References

Method references SHOULD be used when they are clearer than equivalent lambdas.

Example:

```java
.map(Order::getId)
```

---

# 70. Method Reference Ambiguity

Do not use a method reference when it obscures important behavior or creates overload ambiguity.

---

# 71. switch Expressions

Java switch expressions SHOULD be considered when they improve exhaustive mapping.

Example:

```java
return switch (status) {
    case CREATED -> "Created";
    case CANCELLED -> "Cancelled";
};
```

---

# 72. Enum Exhaustiveness

Avoid unnecessary `default` branches when exhaustive enum handling allows the compiler to detect future missing cases.

---

# 73. Pattern Matching

Java pattern matching MAY be used when it removes redundant casts and improves clarity.

Example:

```java
if (value instanceof Order order) {
    process(order);
}
```

---

# 74. Pattern Matching Switch

Pattern matching for switch MAY be used when it provides a clear type-oriented dispatch model.

---

# 75. Reflection

Reflection SHOULD be minimized.

---

# 76. setAccessible

Avoid:

```java
setAccessible(true)
```

for normal application and test code.

Prefer explicit APIs and testable design.

---

# 77. Enums

Enums SHOULD represent closed, stable sets of domain or technical values.

---

# 78. Stringly Typed Domain

Avoid repeatedly representing known closed concepts through arbitrary strings.

---

# 79. Enum External Value

If an enum has a distinct external representation, model it explicitly.

Example:

```java
public enum OrderStatus {

    CREATED("Criado"),
    CANCELLED("Cancelado");

    private final String externalValue;
}
```

---

# 80. Enum Parsing

External parsing SHOULD be centralized.

---

# 81. Case Conversion

Do not scatter:

```java
equalsIgnoreCase(...)
```

through business code for domain parsing.

---

# 82. Unknown Enum Values

Unknown external values MUST have deliberate handling.

---

# 83. Constants

Repeated semantic constants SHOULD be centralized at the appropriate scope.

---

# 84. Constant Scope

Prefer the narrowest useful scope.

Not every constant belongs in a global `Constants` class.

---

# 85. Magic Numbers

Meaningful thresholds and limits SHOULD be named.

---

# 86. Magic Strings

Repeated protocol/status/configuration strings SHOULD be represented explicitly.

---

# 87. Test Constants

Stable reusable test values SHOULD be placed in appropriate test constants/fixtures.

---

# 88. Random Test Data

Tests SHOULD NOT use randomness when fixed deterministic data provides the same coverage.

---

# 89. UUID.randomUUID

Avoid unnecessary:

```java
UUID.randomUUID()
```

in deterministic unit tests.

Use known constants where identity itself is not under test.

---

# 90. Time

Business logic depending on current time SHOULD use an injectable time abstraction such as `Clock` where deterministic testing requires it.

---

# 91. LocalDateTime.now

Scattered direct calls to:

```java
LocalDateTime.now()
```

SHOULD be avoided in logic requiring deterministic tests.

---

# 92. DTOs

DTOs MUST have a clear boundary purpose.

---

# 93. Request DTO

Request DTOs represent inbound contracts.

---

# 94. Response DTO

Response DTOs represent outbound contracts.

---

# 95. Domain Model

DTOs MUST NOT automatically become the domain model merely to avoid mapping.

---

# 96. Persistence Entity

JPA entities represent persistence concerns.

They SHOULD NOT automatically define REST contracts.

---

# 97. Mapper

Mapping logic SHOULD be explicit and testable.

---

# 98. Mapper Complexity

Complex business decisions MUST NOT be hidden inside generic mapping code.

---

# 99. MapStruct

MapStruct MAY be used for repetitive structural mappings where approved.

---

# 100. Manual Mapping

Manual mapping is acceptable when transformation contains meaningful logic or when it is clearer.

---

# 101. Builder

Builders SHOULD be used when object construction is genuinely complex.

---

# 102. Builder Overuse

Do not create builders for every trivial two-field immutable type.

---

# 103. Entity Builder

Builders for mutable persistence entities require care because they may bypass invariants.

---

# 104. Validation

Validation should occur at the appropriate boundary.

---

# 105. Bean Validation

Bean Validation SHOULD be used for declarative request constraints where appropriate.

Examples:

```text
@NotNull

@NotBlank

@Size

@Positive
```

---

# 106. Domain Validation

Business invariants belong in domain/application logic rather than only in controller annotations.

---

# 107. Duplicate Validation

Avoid implementing the same rule independently in multiple layers unless each layer protects a distinct boundary.

---

# 108. Exception Types

Exceptions SHOULD communicate meaningful failure categories.

---

# 109. Generic Exception

Avoid throwing:

```java
new RuntimeException(...)
```

for known business or integration conditions.

---

# 110. Business Exception

Domain/application failures SHOULD use meaningful exception types.

---

# 111. Integration Exception

Remote-system failures SHOULD preserve enough context for appropriate translation and diagnostics.

---

# 112. Exception Translation

Infrastructure exceptions SHOULD be translated at appropriate boundaries.

---

# 113. Catch Exception

Broad:

```java
catch (Exception ex)
```

SHOULD be avoided unless the boundary genuinely requires handling all failures.

---

# 114. Swallowed Exception

Exceptions MUST NOT be silently swallowed.

---

# 115. Log or Rethrow

Where an exception is caught, handling MUST have a clear purpose.

Depending on the boundary:

```text
Handle

Translate

Rethrow

Log and Handle
```

---

# 116. Duplicate Logging

Avoid logging the same exception at every layer.

---

# 117. Logging Ownership

Prefer logging at the layer with sufficient context and responsibility for the failure.

---

# 118. Logging

Production code MUST use the approved logging framework.

---

# 119. System.out

Production code MUST NOT use:

```java
System.out.println(...)
```

for application logging.

---

# 120. printStackTrace

Production code MUST NOT use:

```java
ex.printStackTrace();
```

---

# 121. Parameterized Logging

Prefer:

```java
log.info("Order {} created for customer {}", orderId, customerId);
```

over unnecessary string concatenation.

---

# 122. Sensitive Logging

Never intentionally log:

```text
Passwords

Access Tokens

Refresh Tokens

Authorization Headers

Private Keys
```

---

# 123. PII

Personal information in logs MUST follow applicable data-governance rules.

---

# 124. Correlation

Relevant distributed operations SHOULD include correlation context.

---

# 125. Error Logs

Logs SHOULD contain enough contextual information to diagnose failures without exposing sensitive data.

---

# 126. toString

Do not assume generated `toString()` output is safe for logs.

---

# 127. Security Sanitization

Sanitization MUST be context-specific.

---

# 128. HTML Escaping

HTML escaping MUST NOT be applied indiscriminately to domain values.

---

# 129. Domain Preservation

Valid business values such as:

```text
Forge & Field
```

MUST remain semantically intact through domain and persistence processing unless the contract explicitly requires transformation.

---

# 130. Output Encoding

Contextual encoding belongs at the output boundary that requires it.

---

# 131. Utility Classes

Utility classes SHOULD contain cohesive stateless operations.

---

# 132. Utility Constructor

A pure utility class SHOULD prevent accidental instantiation.

---

# 133. Utility Dumping Ground

Avoid generic classes such as:

```text
Utils

CommonUtils

Helper
```

containing unrelated operations.

---

# 134. Static Methods

Static methods are appropriate for stateless deterministic behavior without external dependencies.

---

# 135. Static State

Mutable global static state SHOULD be avoided.

---

# 136. Configuration

Configuration SHOULD use typed configuration properties for related settings.

---

# 137. @Value

Repeated unrelated `@Value` injection SHOULD be replaced by cohesive configuration properties where practical.

---

# 138. Configuration Validation

Required configuration SHOULD fail fast.

---

# 139. Defaults

Configuration defaults MUST be deliberate and safe.

---

# 140. Boolean Configuration

Avoid ambiguous configuration semantics such as multiple overlapping flags controlling the same behavior.

---

# 141. Repository

Repository interfaces SHOULD express persistence operations needed by the application.

---

# 142. Repository Business Logic

Business orchestration SHOULD NOT be hidden inside repository implementations.

---

# 143. N+1

Persistence code MUST consider N+1 query behavior.

---

# 144. Fetch Strategy

JPA fetch strategy MUST be deliberate.

---

# 145. EAGER

`FetchType.EAGER` SHOULD NOT be used as a generic fix for lazy-loading problems.

---

# 146. Transactions

Transaction boundaries SHOULD normally be established in the service/application layer.

---

# 147. Transaction Scope

Transactions SHOULD remain as short as practical.

---

# 148. Remote Calls in Transactions

Long remote API calls SHOULD NOT normally occur while holding database transactions open.

---

# 149. readOnly

Read-only transactional semantics MAY be used when appropriate.

---

# 150. Pagination

Potentially large result sets MUST use bounded retrieval/pagination.

---

# 151. Unbounded findAll

Avoid unbounded:

```java
repository.findAll();
```

on tables that may grow substantially.

---

# 152. Batch Processing

Large processing SHOULD use explicit batching.

---

# 153. External API Clients

HTTP clients MUST have bounded timeouts.

---

# 154. Error Mapping

Remote errors SHOULD be translated consistently.

---

# 155. Retry

Retries MUST be bounded and appropriate to idempotency semantics.

---

# 156. Circuit Breaker

Circuit breakers SHOULD follow platform resilience standards.

---

# 157. Fallback

Fallback behavior MUST preserve business correctness.

---

# 158. Silent Fallback

Do not convert every integration failure into:

```text
empty result
```

unless empty result is genuinely the correct domain behavior.

---

# 159. WebClient

Reactive WebClient MAY be used for HTTP integration even in non-reactive applications when consistent with platform infrastructure.

---

# 160. Reactive Complexity

A non-reactive application SHOULD NOT become accidentally reactive merely because WebClient is used.

---

# 161. Blocking

Blocking/reactive boundaries MUST be understood.

---

# 162. Concurrency

Concurrency SHOULD solve a measured or structurally clear performance problem.

---

# 163. Virtual Threads

Java 21 Virtual Threads SHOULD be considered for high-concurrency I/O-bound work.

---

# 164. Virtual Threads Are Not Unlimited Resources

Downstream resources remain bounded:

```text
Database Connections

HTTP Connections

SQS Capacity

External API Limits
```

---

# 165. Bounded Concurrency

Concurrency SHOULD be bounded according to downstream capacity.

---

# 166. Context Propagation

Security and request context MUST be propagated where asynchronous processing requires them.

---

# 167. Shared Mutable State

Concurrent code MUST minimize shared mutable state.

---

# 168. Synchronization

Synchronization strategy MUST be explicit.

---

# 169. Concurrent Collections

Use concurrent collections when their semantics match the requirement.

---

# 170. Thread.sleep

Production synchronization MUST NOT depend on arbitrary sleeps.

---

# 171. Test Thread.sleep

Tests SHOULD NOT use `Thread.sleep` for ordinary synchronization.

---

# 172. CompletableFuture

`CompletableFuture` SHOULD NOT be introduced solely to make synchronous code appear asynchronous.

---

# 173. Executor Ownership

Custom executors MUST have clear lifecycle and sizing.

---

# 174. Unbounded Executor

Uncontrolled executor creation is prohibited.

---

# 175. Resource Management

Resources MUST use deterministic lifecycle management.

---

# 176. AutoCloseable

Use try-with-resources for `AutoCloseable` resources.

---

# 177. Empty Catch

Empty catch blocks are prohibited.

---

# 178. Comments

Comments SHOULD explain:

```text
WHY
```

when the reason is not evident.

---

# 179. Redundant Comments

Avoid comments that merely restate the code.

---

# 180. TODO

TODO/FIXME comments SHOULD include enough context to be actionable and SHOULD NOT become permanent substitutes for tracked technical debt.

---

# 181. Dead Code

Dead code SHOULD be removed rather than commented out.

---

# 182. Commented-Out Code

Source control is the history.

Do not retain large commented-out implementations.

---

# 183. Naming

Names MUST communicate intent.

---

# 184. Variables

Avoid meaningless names such as:

```text
obj

tmp

data2

x1
```

outside very narrow mathematical/loop contexts.

---

# 185. Methods

Method names SHOULD express behavior.

---

# 186. Boolean Methods

Boolean methods SHOULD read naturally.

Examples:

```text
isActive

hasPermission

canApprove

shouldRetry
```

---

# 187. Classes

Class names SHOULD represent responsibility rather than implementation accident.

---

# 188. Abbreviations

Uncommon abbreviations SHOULD be avoided.

Established domain abbreviations MAY be retained.

---

# 189. Language

Code identifiers SHOULD use the project's established language convention consistently.

---

# 190. Tests

Tests are production-quality code.

---

# 191. JUnit

JUnit 5 is the standard unit-testing framework for modern Java services.

---

# 192. AssertJ

AssertJ is the preferred fluent assertion library where already standardized.

---

# 193. Assertion Description

Assertions MUST include meaningful `.as("...")` descriptions where required by project Sonar/testing conventions.

Preferred:

```java
assertThat(response.status())
        .as("response status should be ACTIVE")
        .isEqualTo(Status.ACTIVE);
```

---

# 194. Assertion Order

`.as("...")` MUST appear before the assertion operation it describes.

---

# 195. Test Names

Tests SHOULD follow the established `test*` convention.

Example:

```java
testFindCustomerShouldReturnCustomerWhenCustomerExists()
```

---

# 196. Test Structure

Tests SHOULD clearly separate:

```text
Arrange

Act

Assert
```

conceptually, without requiring ceremonial comments.

---

# 197. One Behavior

A test SHOULD have one coherent behavioral purpose.

---

# 198. Multiple Assertions

Multiple assertions are acceptable when they validate one coherent result.

---

# 199. Chained Assertions

Assertions SHOULD be chained where this improves clarity and avoids unnecessary duplication.

---

# 200. Varargs

Tests SHOULD avoid unnecessary varargs patterns when project Sonar rules identify them as problematic.

---

# 201. Exception Assertions

Exception tests SHOULD verify meaningful:

- type
- message/code
- relevant state

as appropriate.

---

# 202. Test Constants

Reusable deterministic values SHOULD use `TestConstants` or equivalent fixtures.

---

# 203. Test Isolation

Tests MUST NOT depend on execution order.

---

# 204. Shared Mutable Test State

Shared mutable test state SHOULD be avoided.

---

# 205. Mocking

Mock external collaborators, not the implementation under test.

---

# 206. Over-Mocking

A test requiring extensive mocking of implementation details is a design signal.

---

# 207. Integration Tests

Integration tests SHOULD validate important framework/infrastructure behavior.

---

# 208. Testcontainers

Testcontainers SHOULD be used for representative infrastructure where applicable.

---

# 209. Database Fidelity

Database-specific behavior SHOULD be tested against representative database technology.

---

# 210. H2

H2 MUST NOT be treated as proof of PostgreSQL or Oracle compatibility.

---

# 211. Flaky Tests

Flaky tests are defects.

---

# 212. Retry

Repeatedly retrying flaky tests MUST NOT substitute for fixing them.

---

# 213. Time-Based Tests

Tests involving time SHOULD use deterministic clocks or bounded synchronization.

---

# 214. Sonar

Production and test code MUST satisfy applicable Sonar rules.

---

# 215. Sonar Workaround

Code MUST NOT be made less correct merely to silence Sonar.

---

# 216. Rule Investigation

When a Sonar finding appears:

```text
UNDERSTAND RULE
      |
      v
UNDERSTAND CODE
      |
      v
FIX ROOT CAUSE
```

---

# 217. Suppression

Suppressions MUST be narrow and justified.

---

# 218. NOSONAR

`NOSONAR` SHOULD NOT be used as a routine development technique.

---

# 219. SAST

Generated and manually written code MUST satisfy applicable SAST requirements.

---

# 220. Secure Coding

Security controls MUST be implemented at the correct trust boundary.

---

# 221. Performance

Performance optimization SHOULD be evidence-based.

---

# 222. Premature Optimization

Avoid increasing complexity without measurable benefit.

---

# 223. Algorithmic Complexity

Obvious algorithmic inefficiencies on large datasets SHOULD be corrected even before production profiling when their impact is structurally clear.

---

# 224. Database Efficiency

Prefer reducing:

```text
N calls
```

to:

```text
1 batch call
```

when semantics and downstream APIs permit it.

---

# 225. Batch APIs

Batch interfaces SHOULD be preferred when they significantly reduce network round trips.

---

# 226. Parallelism

Parallelism SHOULD NOT be used to hide an inefficient N+1 integration pattern when a batch API is available.

---

# 227. Caching

Caching MUST have an explicit:

```text
Purpose

Key

TTL

Invalidation Strategy

Failure Strategy
```

---

# 228. Cache Correctness

Caching MUST NOT sacrifice business correctness.

---

# 229. Fallback Cache

Fallback caching MAY improve resilience but requires explicit consistency semantics.

---

# 230. Comments Around Workarounds

Temporary compatibility workarounds SHOULD document:

```text
Why it exists

What dependency requires it

When it can be removed
```

---

# 231. Deprecated APIs

New code SHOULD NOT introduce deprecated APIs when a supported replacement exists.

---

# 232. Legacy Compatibility

Legacy constraints MAY require less-modern implementation patterns.

The constraint SHOULD be explicit.

---

# 233. Modernization

Do not modernize syntax while accidentally changing business semantics.

---

# 234. Refactoring

Refactoring SHOULD preserve observable behavior unless behavior change is explicitly required.

---

# 235. Small Refactorings

Prefer reviewable, coherent refactorings.

---

# 236. Unrelated Changes

Do not mix unrelated cleanup into critical behavioral changes without good reason.

---

# 237. Formatting

Formatting SHOULD follow the project's automated formatter/style configuration where available.

---

# 238. Import Hygiene

Unused imports MUST be removed.

---

# 239. Static Imports

Static imports SHOULD be used selectively.

Excessive static imports can reduce readability.

---

# 240. Wildcard Imports

Wildcard imports SHOULD be avoided.

---

# 241. package-info.java

Projects requiring package documentation SHOULD maintain `package-info.java` for applicable packages.

---

# 242. Package Documentation

Package documentation SHOULD describe architectural responsibility rather than merely repeat the package name.

---

# 243. Public APIs

Public reusable methods/classes SHOULD have documentation when behavior is not self-evident.

---

# 244. Javadoc

Javadoc SHOULD describe contracts, constraints and non-obvious behavior rather than mechanically restating signatures.

---

# 245. Deprecated Code

Deprecated application APIs SHOULD identify the preferred replacement where possible.

---

# 246. Clean Code Fitness Functions

The following controls SHOULD be automated where practical:

```text
[ ] Java 21 baseline

[ ] Forbidden field injection

[ ] Layer dependency direction

[ ] Package cycles

[ ] Sonar complexity

[ ] Sonar duplication

[ ] Coverage threshold

[ ] Forbidden dependencies

[ ] Secret scanning

[ ] SAST

[ ] Flyway immutability

[ ] Architecture tests

[ ] Unsupported/deprecated dependency checks
```

---

# 247. Code Review Checklist

A material Java change SHOULD be reviewed against:

```text
[ ] Business behavior correct

[ ] Existing architecture preserved

[ ] Responsibilities cohesive

[ ] Dependencies explicit

[ ] Null behavior deliberate

[ ] Collections safely owned

[ ] Exceptions correctly handled

[ ] Logging useful and safe

[ ] No unnecessary abstractions

[ ] No unnecessary reflection

[ ] No accidental N+1

[ ] Transaction scope appropriate

[ ] External calls bounded

[ ] Concurrency justified

[ ] Tests meaningful

[ ] Tests deterministic

[ ] AssertJ conventions followed

[ ] Sonar clean

[ ] SAST clean

[ ] Documentation updated where needed
```

---

# 248. Enterprise Java Gate

A Java/Spring Boot implementation is not considered compliant when an applicable critical condition exists:

```text
[ ] Unsupported Java runtime

[ ] Field injection in new application code

[ ] Secrets hardcoded

[ ] Swallowed exceptions

[ ] Sensitive credentials logged

[ ] Unbounded remote call timeout

[ ] Applied Flyway migration modified

[ ] Critical Sonar/SAST failure

[ ] Business logic dependent on arbitrary Thread.sleep

[ ] Uncontrolled executor creation

[ ] Invalid layer dependency

[ ] Non-deterministic critical tests

[ ] Unresolved critical dependency vulnerability
```

---

# 249. Anti-Patterns

The following are prohibited or strongly discouraged:

- field injection
- god classes
- dependency-bag objects hiding excessive dependencies
- deep nested conditionals
- nested ternaries
- returning null collections
- Optional fields in ordinary DTOs/entities
- Optional parameters without strong justification
- unchecked Optional.get
- records used as JPA entities by default
- exposing mutable internal collections
- stream pipelines with hidden side effects
- complex unreadable streams
- parallelStream without justification
- business mutation through peek
- unnecessary reflection
- routine setAccessible(true)
- stringly typed closed domain concepts
- global dumping-ground Constants/Utils classes
- random UUIDs in deterministic tests
- DTO/entity/domain conflation
- generic RuntimeException for known failures
- swallowed exceptions
- duplicate exception logging at every layer
- System.out/printStackTrace
- logging credentials or tokens
- indiscriminate HTML escaping of domain data
- business logic in repositories
- generic EAGER fetching
- remote calls inside long database transactions
- unbounded findAll on large tables
- silent integration fallbacks
- unnecessary CompletableFuture
- uncontrolled executors
- Thread.sleep synchronization
- commented-out dead code
- meaningless comments
- excessive mocking
- meaningless coverage tests
- NOSONAR as routine practice
- performance complexity without evidence
- parallelism used instead of available batch APIs

---

# 250. Positive Consequences

The decision provides:

- consistent Java implementation
- improved readability
- stronger null safety
- better testability
- predictable dependency injection
- increased immutability
- cleaner domain boundaries
- stronger Sonar compliance
- deterministic testing
- safer concurrency
- reduced accidental complexity
- easier code reviews
- easier onboarding
- stronger automated governance

---

# 251. Negative Consequences

The decision introduces:

- refactoring effort for legacy code
- stricter code reviews
- additional architecture tests
- migration from older Java idioms
- occasional explicit mapping code

These costs are accepted because maintainability and predictability are critical for long-lived enterprise services.

---

# 252. Neutral Consequences

The decision also means:

- imperative code remains valid
- not every DTO must be a record
- not every service requires an interface
- not every collection transformation requires streams
- not every I/O operation requires Virtual Threads
- not every abstraction should be shared
- legacy systems may temporarily require different patterns

---

# 253. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Rules become dogmatic | High | Medium | Prefer rationale over ceremony |
| Excessive abstraction | High | Medium | Simplicity principle |
| Overuse of modern Java features | Medium | Medium | Readability review |
| Sonar-driven code distortion | High | Medium | Root-cause fixes |
| Excessive decomposition | Medium | Medium | Cohesive responsibilities |
| Stream overuse | Medium | Medium | Imperative code allowed |
| Parallelism misuse | High | Medium | Capacity analysis |
| Test brittleness | Medium | Medium | Behavioral testing |
| Legacy migration cost | Medium | High | Incremental adoption |
| Too many shared utilities | Medium | Medium | Narrow ownership |

---

# 254. Implementation Guidance

The following rules are mandatory:

1. Modern Java services use the approved Java baseline.
2. Constructor injection is required for mandatory application dependencies.
3. Field injection must not be introduced in new production code.
4. Classes with excessive dependencies must be reviewed for responsibility decomposition.
5. Complexity must remain within applicable Sonar limits.
6. Nested ternaries should not be used for business logic.
7. Nullability must be deliberate.
8. Null collections should not be returned.
9. Optional is primarily a return-value abstraction.
10. Optional.get requires established presence.
11. Records should be considered for immutable data carriers.
12. JPA entities should not normally be records.
13. Mutable collections crossing ownership boundaries require defensive handling.
14. Streams should be used only when clearer than imperative alternatives.
15. Stream operations should avoid hidden side effects.
16. parallelStream requires explicit justification.
17. Reflection and setAccessible should be avoided.
18. Enums should model stable closed value sets.
19. Constants should use the narrowest meaningful scope.
20. Tests should use deterministic data where practical.
21. DTO, domain and persistence responsibilities must remain explicit.
22. Exceptions must not be swallowed.
23. Logging must use approved frameworks and protect sensitive information.
24. Domain values must not be corrupted through generic sanitization.
25. Utility classes must remain cohesive.
26. Configuration should be typed and fail fast where appropriate.
27. Repository code must not become business orchestration.
28. Potentially large queries must remain bounded.
29. External calls require bounded timeouts.
30. Retry and fallback semantics must preserve correctness.
31. Concurrency must be deliberate and bounded by downstream capacity.
32. Virtual Threads must preserve required execution context.
33. Thread.sleep must not be used as ordinary synchronization.
34. Tests must follow established JUnit/AssertJ/Sonar conventions.
35. AssertJ `.as("...")` descriptions must precede relevant assertions where required.
36. Test methods should follow the established `test*` naming convention.
37. Tests should avoid unnecessary random UUIDs.
38. Flaky tests must be fixed rather than normalized through retries.
39. Sonar and SAST findings should be corrected at their root cause.
40. Performance optimizations should be evidence-based.
41. Batch operations should be preferred over N+1 remote calls where supported.
42. Applied Flyway migrations remain immutable.
43. Refactorings should preserve behavior unless behavior change is explicit.
44. Dead/commented-out code should be removed.
45. Architecture fitness functions should automate stable coding invariants where practical.

---

# 255. Validation

This ADR will be validated through:

- compiler
- Gradle
- JUnit 5
- AssertJ
- ArchUnit
- JaCoCo
- SonarQube
- SAST
- dependency scanning
- code review
- architecture fitness functions
- integration tests
- performance tests where applicable

---

# 256. Success Criteria

The decision is successful when:

- Java code becomes easier to understand
- Sonar findings decrease
- test quality increases
- flaky tests decrease
- dependency injection remains explicit
- god classes decrease
- unnecessary abstractions decrease
- null-related defects decrease
- database/integration N+1 patterns decrease
- concurrency remains controlled
- code reviews focus more on behavior than recurring style problems
- architecture fitness functions automatically enforce stable rules

---

# 257. Alternatives Rejected

## 257.1 No Enterprise Coding Standard

Rejected because large multi-team environments otherwise accumulate inconsistent implementation practices.

---

## 257.2 SonarQube as the Entire Coding Standard

Rejected because static analysis does not represent all design and architectural concerns.

---

## 257.3 Functional Programming Everywhere

Rejected because streams and functional constructs are tools rather than objectives.

---

## 257.4 Maximum Use of Java 21 Features

Rejected because language novelty is not equivalent to maintainability.

---

## 257.5 Interface for Every Service

Rejected because interfaces without abstraction value create ceremony.

---

## 257.6 Maximum Parallelism

Rejected because downstream resources remain bounded and concurrency introduces complexity.

---

# 258. Related Decisions

This ADR extends and implements:

- ADR-004: Use Spring Boot
- ADR-006: Use Flyway for Database Migrations
- ADR-016: Application Resilience
- ADR-031: Database Performance and Data Access Standards
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-038: Dependency and Software Supply Chain Security Standards
- ADR-041: Architecture Governance and Technical Debt Management Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-048: Engineering Productivity and Developer Experience Standards
- ADR-049: AI-Assisted Software Engineering Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Software Architecture Testing and Automated Fitness Functions

---

# 259. References

- Java 21 Documentation
- Spring Boot Documentation
- Spring Framework Documentation
- Effective Java
- Clean Code
- Refactoring
- JUnit 5
- AssertJ
- ArchUnit
- JaCoCo
- SonarQube
- OWASP
- Testcontainers

---

# 260. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial Java 21 / Spring Boot enterprise coding baseline |

---

# 261. Decision Summary

The Java implementation model becomes:

```text
                  BUSINESS RULE
                       |
                       v
                CLEAR DOMAIN CODE
                       |
          +------------+------------+
          |            |            |
          v            v            v
      IMMUTABLE     EXPLICIT      SMALL
        DATA       DEPENDENCIES   RESPONSIBILITY
          |            |            |
          +------------+------------+
                       |
                       v
                 TESTABLE CODE
                       |
                       v
               QUALITY GATES
```

Dependency injection:

```text
MANDATORY DEPENDENCY
        |
        v
CONSTRUCTOR INJECTION
        |
        v
FINAL FIELD
```

rather than:

```text
HIDDEN FIELD INJECTION
```

Data representation:

```text
DATA CARRIER
    |
    +--> Immutable?
    |
    +--> No entity lifecycle?
    |
    +--> Value semantics?
    |
    v
 CONSIDER RECORD
```

Null handling:

```text
VALUE MAY BE ABSENT
        |
        v
IS IT A RETURN CONTRACT?
     /        \
   YES         NO
    |           |
    v           v
OPTIONAL     EXPLICIT
            CONTRACT
```

Collection ownership:

```text
MUTABLE COLLECTION
       |
       v
CROSSES OWNERSHIP BOUNDARY?
      / \
    YES  NO
     |
     v
DEFENSIVE COPY
```

Streams:

```text
COLLECTION TRANSFORMATION
          |
          v
IS STREAM CLEARER?
       /      \
     YES       NO
      |         |
      v         v
    STREAM     LOOP
```

Both are valid.

Concurrency:

```text
WORKLOAD
   |
   v
IS PARALLELISM NEEDED?
   /          \
 NO           YES
 |             |
 v             v
KEEP       IDENTIFY
SIMPLE     BOTTLENECK
               |
               v
         BOUND CAPACITY
               |
               v
         SELECT MODEL
```

Testing:

```text
ARRANGE
   |
   v
 ACT
   |
   v
ASSERT BEHAVIOR
   |
   v
DETERMINISTIC RESULT
```

with:

```java
assertThat(result)
        .as("result should satisfy the expected business behavior")
        .isEqualTo(expected);
```

Error handling:

```text
EXCEPTION
    |
    v
DO WE HANDLE IT HERE?
   /         \
 YES          NO
  |            |
  v            v
HANDLE /     RETHROW
TRANSLATE
```

not:

```text
CATCH
 |
 v
IGNORE
```

Performance:

```text
N REMOTE CALLS
      |
      v
BATCH API AVAILABLE?
    /       \
  YES        NO
   |          |
   v          v
 BATCH      EVALUATE
           CONTROLLED
           CONCURRENCY
```

The complete enterprise Java equation is:

```text
JAVA 21
   +
SPRING BOOT
   +
EXPLICIT DEPENDENCIES
   +
IMMUTABILITY
   +
CLEAR DOMAIN BOUNDARIES
   +
CONTROLLED COMPLEXITY
   +
CORRECT ERROR HANDLING
   +
SAFE LOGGING
   +
DETERMINISTIC TESTING
   +
SONAR / SAST
   +
MEASURED PERFORMANCE
   +
BOUNDED CONCURRENCY
   =
ENTERPRISE-GRADE JAVA
```

The governing principle is:

```text
Enterprise code does not need
to be complicated to be robust.

Use modern Java where it removes
complexity, not where it adds novelty.

Prefer explicit dependencies,
immutable state, cohesive classes,
clear business rules and
deterministic tests.

Optimize only where the workload
justifies optimization.

Automate stable quality rules.

And whenever a developer must
choose between clever code and
code the next engineer can safely
understand and modify,
choose the latter.
```
