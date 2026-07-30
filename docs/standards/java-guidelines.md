# Java Guidelines

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Java Guidelines |
| Status | Draft |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Java development guidelines adopted by the AstraForge Supply Platform.

It establishes standards for:

- Java 21 language features
- object design
- immutability
- exceptions
- collections
- streams
- Optional
- concurrency
- virtual threads
- performance
- code organization

These guidelines complement the general coding standards and apply to all Java modules.

---

# 2. Java Version

The project uses:

```text
Java 21
```

The codebase may use stable Java 21 language features when they improve:

- readability
- maintainability
- safety
- performance

New features should not be adopted only for novelty.

---

# 3. General Principles

Java code should be:

- explicit
- cohesive
- testable
- immutable where practical
- free from unnecessary framework coupling

Prefer simple object-oriented design over excessive abstraction.

---

# 4. Immutability

Prefer immutable objects for:

- commands
- queries
- responses
- domain events
- value objects
- configuration properties

Use:

- final fields
- constructor initialization
- records
- defensive copies

Avoid exposing mutable internal state.

---

# 5. Records

Use records for transparent immutable data carriers.

Suitable examples:

- DTOs
- commands
- queries
- events
- value-oriented results
- projections

Example:

```java
public record CreateOrderCommand(
        UUID customerId,
        List<CreateOrderItemCommand> items
) {
    public CreateOrderCommand {
        items = List.copyOf(items);
    }
}
```

Records must validate invariants in the compact constructor when necessary.

---

# 6. Records in the Domain

Records may represent Value Objects when:

- identity is defined exclusively by their values
- immutability is required
- inheritance is unnecessary
- behavior remains cohesive with the represented concept

Example:

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }
}
```

Do not use records automatically for Aggregate Roots or mutable lifecycle entities.

---

# 7. Sealed Types

Use sealed interfaces and classes when the set of permitted implementations is intentionally closed.

Suitable use cases:

- command results
- payment outcomes
- integration responses
- domain decision types

Example:

```java
public sealed interface PaymentResult
        permits PaymentApproved, PaymentRejected, PaymentPending {
}
```

Sealed hierarchies should communicate a meaningful domain boundary.

---

# 8. Pattern Matching

Use pattern matching when it reduces unsafe casts and improves clarity.

Example:

```java
if (result instanceof PaymentRejected rejected) {
    handleRejection(rejected);
}
```

Avoid long chains of type checks.

When many branches exist, reconsider polymorphism or sealed-type design.

---

# 9. Switch Expressions

Prefer switch expressions for exhaustive mappings.

Example:

```java
return switch (status) {
    case CREATED -> "Created";
    case APPROVED -> "Approved";
    case CANCELLED -> "Cancelled";
};
```

Avoid default branches when all enum constants should be handled explicitly.

This allows compiler errors when new constants are introduced.

---

# 10. Primitive Types

Prefer primitive types when null is not meaningful.

Use wrappers when:

- null represents absence
- required by a framework
- used in generic collections
- required by persistence mapping

Avoid unnecessary boxing in performance-sensitive code.

---

# 11. BigDecimal

Use `BigDecimal` for:

- money
- financial calculations
- precise decimal values

Never create `BigDecimal` from a floating-point literal.

Incorrect:

```java
new BigDecimal(0.1)
```

Correct:

```java
new BigDecimal("0.10")
```

or:

```java
BigDecimal.valueOf(0.1)
```

Monetary scale and rounding mode must be explicit.

---

# 12. Date and Time

Use the `java.time` API.

Preferred types:

| Type | Use |
|---|---|
| `Instant` | Machine timestamp |
| `OffsetDateTime` | Timestamp with UTC offset |
| `ZonedDateTime` | Time-zone-aware business date and time |
| `LocalDate` | Date without time |
| `LocalTime` | Time without date |
| `Duration` | Time-based amount |
| `Period` | Date-based amount |

Avoid:

- `java.util.Date`
- `java.sql.Date`
- manual timestamp arithmetic

Persist timestamps in UTC unless a documented business requirement states otherwise.

---

# 13. UUID

Use UUIDs for technical identifiers where distributed generation is required.

Prefer constants in tests.

Avoid generating random UUIDs inside tests unless randomness is the behavior under test.

Identifier generation should occur at a clear architectural boundary.

---

# 14. Optional

Use `Optional` primarily as a return type when absence is expected.

Good:

```java
Optional<Order> findById(OrderId orderId);
```

Avoid:

- `Optional` fields in entities
- `Optional` method parameters
- `Optional` in records used by serialization frameworks without justification
- calling `get()` without verifying presence

Prefer:

```java
return repository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
```

Use `isPresent()` before `get()` when explicit branching is clearer or required by static analysis rules.

---

# 15. Null Handling

Public APIs should define nullability explicitly through contracts and validation.

Prefer:

- required constructor arguments
- `Objects.requireNonNull`
- Bean Validation at external boundaries
- empty immutable collections
- `Optional` for expected absence

Avoid returning null collections.

---

# 16. Collections

Return immutable collections whenever callers should not modify the result.

Use:

```java
List.copyOf(items)
Set.copyOf(values)
Map.copyOf(entries)
```

Use factory methods for small immutable collections:

```java
List.of(...)
Set.of(...)
Map.of(...)
```

Do not expose mutable collections owned by domain objects.

---

# 17. Collection Selection

Select the collection according to semantics.

| Collection | Use |
|---|---|
| `List` | Ordered elements, duplicates allowed |
| `Set` | Unique elements |
| `Map` | Key-based access |
| `Deque` | Stack or queue behavior |

Do not use `List` by default when uniqueness is a business invariant.

---

# 18. Streams

Use Streams when they improve declarative readability.

Good use cases:

- mapping
- filtering
- grouping
- aggregation
- simple immutable transformations

Example:

```java
return items.stream()
        .filter(OrderItem::isActive)
        .map(OrderItem::toSummary)
        .toList();
```

Avoid streams when:

- control flow is complex
- checked exception handling becomes obscure
- mutation dominates processing
- debugging becomes unnecessarily difficult
- a simple loop is clearer

---

# 19. Stream Side Effects

Stream operations should be free from side effects whenever practical.

Avoid:

```java
items.stream()
        .map(item -> {
            repository.save(item);
            return item;
        })
        .toList();
```

Use explicit iteration for commands and side effects.

---

# 20. Parallel Streams

Parallel streams are not used by default.

Their execution relies on the common ForkJoinPool and may introduce:

- unpredictable contention
- reduced observability
- context propagation problems
- poor behavior with blocking I/O

Use explicit executors or structured concurrency patterns when parallel execution is justified.

---

# 21. Method References

Use method references when they improve readability.

Example:

```java
orders.stream()
        .map(Order::getId)
        .toList();
```

Prefer lambdas when the method reference obscures intent or creates type ambiguity.

---

# 22. Lambdas

Keep lambdas short and focused.

A lambda used in assertions or exception verification should ideally contain a single invocation.

Good:

```java
assertThatThrownBy(() -> service.approve(orderId))
        .as("should reject approval for a missing order")
        .isInstanceOf(OrderNotFoundException.class);
```

Extract complex logic into named methods.

---

# 23. Exceptions

Use exceptions to represent exceptional conditions, not regular control flow.

Exception names must describe the failure.

Examples:

```text
OrderNotFoundException
InventoryReservationException
InvalidOrderTransitionException
ExternalServiceUnavailableException
```

Avoid generic exceptions such as:

```text
RuntimeException
Exception
IllegalStateException
```

for business-specific failures when a meaningful type can be defined.

---

# 24. Checked Exceptions

Checked exceptions should be used sparingly.

They may be appropriate when callers can reasonably recover from the condition.

Infrastructure exceptions should generally be translated before crossing architectural boundaries.

---

# 25. Catch Blocks

Never swallow exceptions.

A caught exception must be:

- handled
- translated
- logged
- or rethrown

When translating, preserve the original cause.

Example:

```java
catch (SQLException exception) {
    throw new OrderPersistenceException(
            "Unable to persist order",
            exception
    );
}
```

Do not both log and rethrow at every layer, as this produces duplicate logs.

Logging ownership should be explicit.

---

# 26. Resource Management

Use try-with-resources for all `AutoCloseable` resources.

Example:

```java
try (InputStream input = file.openStream()) {
    return parser.parse(input);
}
```

Manual resource cleanup should be avoided.

---

# 27. Enums

Use enums for closed, stable sets of values.

Enums may include cohesive behavior.

Example:

```java
public enum OrderStatus {
    CREATED {
        @Override
        public boolean canBeCancelled() {
            return true;
        }
    },
    APPROVED {
        @Override
        public boolean canBeCancelled() {
            return false;
        }
    };

    public abstract boolean canBeCancelled();
}
```

Avoid persistence or API coupling directly inside domain enums when it mixes responsibilities.

---

# 28. Static Utility Classes

Utility classes should be rare.

When justified:

- make the class final
- provide a private constructor
- keep methods stateless
- avoid becoming a dumping ground

Prefer cohesive domain or application services over generic utility classes.

---

# 29. Constructors

Constructors should establish valid objects.

Avoid constructors with excessive parameters.

When many parameters represent one concept, introduce:

- a record
- a Value Object
- a configuration object
- a builder where justified

Do not use builders to hide poor object design.

---

# 30. Dependency Count

Classes with many constructor dependencies indicate excessive responsibility.

A class with more than approximately 10 dependencies requires architectural review.

Classes approaching 20 dependencies should be decomposed.

---

# 31. Visibility

Use the narrowest possible visibility.

Prefer:

- private by default
- package-private for internal collaboration
- public only for intentional contracts

Do not expose implementation details unnecessarily.

---

# 32. Final Classes

Use `final` when inheritance is not part of the design.

Do not use inheritance only to reuse code.

Prefer composition.

---

# 33. Generics

Use generics to improve type safety.

Avoid:

- raw types
- unchecked casts
- overly abstract generic hierarchies
- generic type names that obscure meaning

Common generic names such as `T`, `K`, and `V` are acceptable for small, conventional abstractions.

Use meaningful names in complex contracts.

---

# 34. Concurrency

Shared mutable state should be avoided.

Prefer:

- immutability
- message passing
- database concurrency control
- thread-safe collections when truly required
- explicit executors

Do not synchronize large sections of business logic.

---

# 35. Virtual Threads

Virtual threads may be used for high-concurrency blocking I/O workloads.

Suitable examples:

- independent REST calls
- database operations through blocking drivers
- parallel integration validation
- report generation tasks

Example:

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<Customer> customer = executor.submit(customerClient::getCustomer);
    Future<Inventory> inventory = executor.submit(inventoryClient::getInventory);

    return combine(customer.get(), inventory.get());
}
```

Virtual threads do not eliminate:

- database connection limits
- external API limits
- rate limits
- memory limits
- the need for timeouts

Concurrency must remain bounded by downstream capacity.

---

# 36. Executor Ownership

Executors must have explicit ownership and lifecycle.

Avoid creating a new executor for each request.

Application-managed executors should be:

- named
- monitored
- closed gracefully
- dedicated by workload when isolation is required

---

# 37. ThreadLocal Context

Virtual threads and asynchronous execution may affect context propagation.

Security context, tracing context, correlation identifiers and locale must be propagated explicitly or through supported framework mechanisms.

Do not assume ThreadLocal state is automatically available across executors.

---

# 38. CompletableFuture

Use `CompletableFuture` only when it provides clear orchestration value.

Always use an explicit executor for application workloads.

Avoid:

```java
CompletableFuture.supplyAsync(this::loadData);
```

Prefer:

```java
CompletableFuture.supplyAsync(this::loadData, applicationExecutor);
```

Exceptions and timeouts must be handled explicitly.

---

# 39. Synchronization

Use locking only when shared in-memory state is unavoidable.

Prefer higher-level concurrency primitives:

- `ConcurrentHashMap`
- `AtomicInteger`
- `Semaphore`
- `ReentrantLock`
- immutable snapshots

Document lock ordering when multiple locks exist.

---

# 40. Performance

Do not optimize without evidence.

Performance work should be based on:

- profiling
- production metrics
- benchmarks
- query plans
- load tests

Prioritize algorithmic improvements and I/O reduction over micro-optimizations.

---

# 41. String Handling

Use:

- text blocks for large static text
- `StringBuilder` for repeated mutation in loops
- formatted strings for readable templates

Example:

```java
String message = """
        Order %s cannot transition from %s to %s
        """.formatted(orderId, currentStatus, targetStatus);
```

Avoid concatenating sensitive data into logs.

---

# 42. Text Blocks

Use text blocks for:

- SQL used in tests or infrastructure adapters
- JSON examples
- multiline templates
- documentation-oriented constants

Do not embed large business documents directly in source code.

---

# 43. Serialization

Serialization contracts must be explicit and version-aware.

Do not serialize domain entities directly for external communication.

Use dedicated:

- request DTOs
- response DTOs
- integration event payloads

---

# 44. Reflection

Avoid reflection in business code.

Reflection may be used by frameworks or tightly controlled infrastructure utilities.

Avoid bypassing encapsulation through `setAccessible`.

Prefer supported public APIs and explicit mappings.

---

# 45. Annotations

Annotations should not replace clear design.

Avoid excessive custom annotations that hide business behavior or control flow.

Framework annotations belong primarily at architectural boundaries.

Domain objects should remain framework-independent whenever practical.

---

# 46. Deprecation

Deprecated code must include:

- replacement guidance
- reason for deprecation
- planned removal version when known

Example:

```java
/**
 * @deprecated Use {@link OrderQueryService#findSummary(OrderId)}.
 */
@Deprecated(forRemoval = true, since = "2.0")
```

Deprecated code should not remain indefinitely.

---

# 47. Package Documentation

Every significant package should include a `package-info.java` describing:

- responsibility
- architectural layer
- allowed dependencies
- important design constraints

Package documentation must be updated when responsibilities change.

---

# 48. Source File Organization

Recommended order:

1. package declaration
2. imports
3. class annotation
4. class declaration
5. constants
6. fields
7. constructors
8. public methods
9. package-private methods
10. protected methods
11. private methods
12. nested types

Keep related methods close together.

---

# 49. Import Rules

Avoid wildcard imports.

Remove unused imports.

Use static imports only when they improve clarity, especially in tests.

Excessive static imports should be avoided because they obscure method ownership.

---

# 50. Code Quality Rules

The codebase must remain compliant with:

- compiler warnings
- SonarQube quality rules
- SAST rules
- architecture tests
- formatting rules

Suppressions require a clear technical justification.

Broad suppression annotations are prohibited.

---

# 51. Testing Conventions

Java tests should:

- use descriptive `test*` method names
- use deterministic constants
- avoid random UUIDs unless required
- avoid `Thread.sleep`
- use AssertJ descriptions with `.as("...")`
- verify one behavior per test
- keep assertion lambdas focused on one invocation

Example:

```java
assertThat(result.status())
        .as("order status should be approved")
        .isEqualTo(OrderStatus.APPROVED);
```

---

# 52. Architecture Rules

Java code must:

- preserve domain independence
- avoid framework leakage into the domain
- expose immutable contracts where practical
- use explicit concurrency
- preserve exception causes
- avoid hidden side effects
- favor composition over inheritance

---

# 53. Decision Summary

The project adopts:

- Java 21
- records for immutable data carriers
- sealed types for closed hierarchies
- pattern matching where readable
- `java.time`
- explicit null handling
- immutable collections
- constrained use of Streams
- virtual threads for suitable blocking workloads
- explicit executor ownership
- evidence-based optimization
- SonarQube and SAST compliance
