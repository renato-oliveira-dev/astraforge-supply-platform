# Testing Standards

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Testing Standards |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the testing standards adopted by the Enterprise Order Platform.

It establishes practices for:

- unit testing
- application testing
- controller testing
- persistence testing
- integration testing
- messaging testing
- contract testing
- architecture testing
- concurrency testing
- resilience testing
- performance testing
- test data management
- coverage quality
- deterministic execution

The objective is to ensure that tests provide fast, reliable and meaningful feedback throughout the software delivery lifecycle.

---

# 2. Testing Principles

Tests must be:

- deterministic
- isolated
- readable
- behavior-oriented
- maintainable
- fast at the appropriate level
- independent from execution order
- explicit about intent

Tests should validate observable behavior rather than implementation details.

A test suite must increase confidence, not merely increase coverage percentages.

---

# 3. Testing Strategy

The platform adopts a layered testing strategy.

Recommended order:

1. unit tests
2. slice tests
3. integration tests
4. contract tests
5. end-to-end tests
6. performance and resilience tests

Most tests should remain at the unit and slice levels.

Full end-to-end tests should be limited to critical business journeys.

---

# 4. Test Pyramid

The expected test distribution is:

```text
Many unit tests

Fewer slice and integration tests

Few end-to-end tests
```

A healthy test suite should not depend primarily on full application startup.

Excessive end-to-end testing leads to:

- slow pipelines
- difficult diagnosis
- unstable fixtures
- high maintenance cost
- environment dependence

---

# 5. Test Scope Selection

Use the narrowest test scope that validates the required behavior.

| Scenario | Preferred test type |
|---|---|
| Value Object behavior | Unit test |
| Aggregate invariant | Unit test |
| Application orchestration | Unit test |
| Controller mapping | MVC slice test |
| JPA query | Persistence slice test |
| PostgreSQL behavior | Integration test |
| SQS flow | Integration test |
| External API contract | Contract test |
| Complete business journey | End-to-end test |

Do not start a Spring context when direct class instantiation is sufficient.

---

# 6. Test Frameworks

The standard toolset is:

- JUnit 5
- AssertJ
- Mockito
- Spring Boot Test
- MockMvc
- Testcontainers
- ArchUnit
- Awaitility where justified
- JaCoCo

Additional tools require a documented engineering benefit.

---

# 7. Test Package Structure

Tests should mirror the production package structure.

Example:

```text
src/main/java/com/enterprise/orderplatform/application/order/CreateOrderService.java
```

Corresponding test:

```text
src/test/java/com/enterprise/orderplatform/application/order/CreateOrderServiceTest.java
```

This improves discoverability and package-private access where appropriate.

---

# 8. Test Class Naming

Test classes use the subject under test followed by `Test`.

Examples:

```java
OrderTest

MoneyTest

CreateOrderServiceTest

OrderControllerTest

JpaOrderRepositoryTest
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

# 9. Test Method Naming

Test method names must begin with `test`.

They should communicate:

- operation
- condition
- expected result

Examples:

```java
testApproveShouldChangeStatusWhenOrderIsPending()

testApproveShouldThrowExceptionWhenOrderDoesNotExist()

testCreateShouldPublishEventAfterPersistence()

testFindByIdShouldReturnEmptyWhenOrderDoesNotExist()
```

Avoid:

```java
test1()

shouldWork()

approveTest()

happyPath()
```

---

# 10. Test Structure

Tests should follow Arrange, Act and Assert.

Example:

```java
@Test
void testApproveShouldChangeStatusWhenOrderIsPending() {
    Order order = OrderFixture.pendingApprovalOrder();

    order.approve(TestConstants.APPROVER_ID);

    assertThat(order.status())
            .as("order status should change to approved")
            .isEqualTo(OrderStatus.APPROVED);
}
```

The structure may remain implicit when the test is already clear.

Avoid unnecessary comments such as:

```java
// Arrange
// Act
// Assert
```

when they do not improve readability.

---

# 11. AssertJ

AssertJ is the preferred assertion library.

Every AssertJ assertion chain must include `.as("...")` before the predicate or assertion method.

Correct:

```java
assertThat(order.status())
        .as("order status should be approved")
        .isEqualTo(OrderStatus.APPROVED);
```

Incorrect:

```java
assertThat(order.status())
        .isEqualTo(OrderStatus.APPROVED);
```

Descriptions should explain the expected behavior rather than repeat the syntax.

---

# 12. Assertion Descriptions

Good descriptions:

```java
.as("order should remain pending while supervisor approval is required")

.as("created event should reference the persisted order identifier")

.as("customer identifier should be propagated to the integration request")
```

Avoid:

```java
.as("status")

.as("assert value")

.as("test order")
```

Descriptions must provide useful failure context.

---

# 13. Chained Assertions

Prefer cohesive chained assertions.

Example:

```java
assertThat(response)
        .as("created order response should contain the persisted order data")
        .satisfies(result -> {
            assertThat(result.orderId())
                    .as("response should contain the generated order identifier")
                    .isEqualTo(TestConstants.ORDER_ID);

            assertThat(result.status())
                    .as("new order should be returned with created status")
                    .isEqualTo(OrderStatus.CREATED);
        });
```

Avoid creating many disconnected assertions without descriptive context.

---

# 14. AssertAll

JUnit `assertAll` should not be the default when AssertJ provides a clearer object assertion model.

Prefer:

```java
assertThat(order)
        .as("order should contain the expected creation data")
        .extracting(
                Order::customerId,
                Order::status,
                Order::total
        )
        .containsExactly(
                TestConstants.CUSTOMER_ID,
                OrderStatus.CREATED,
                TestConstants.ORDER_TOTAL
        );
```

Use grouped assertions only when they preserve readability.

---

# 15. Exception Assertions

Use AssertJ for exception validation.

Example:

```java
assertThatThrownBy(() -> service.approve(TestConstants.ORDER_ID))
        .as("approval should fail when the order does not exist")
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining(TestConstants.ORDER_ID.toString());
```

The lambda passed to exception assertions should contain one invocation.

Avoid:

```java
assertThatThrownBy(() -> {
    prepareData();
    service.approve(orderId);
});
```

Prepare test data before the assertion.

---

# 16. Exact Exception Types

Use `isExactlyInstanceOf` when subtype acceptance would hide an incorrect exception.

Example:

```java
assertThatThrownBy(order::approve)
        .as("approved orders should reject a second approval")
        .isExactlyInstanceOf(OrderAlreadyApprovedException.class);
```

Use `isInstanceOf` when subtype compatibility is intentional.

---

# 17. Optional Assertions

Do not call `Optional.get()` without first asserting presence.

Correct:

```java
assertThat(result)
        .as("repository should return the persisted order")
        .isPresent();

assertThat(result.orElseThrow())
        .as("persisted order should preserve the original identifier")
        .extracting(Order::id)
        .isEqualTo(TestConstants.ORDER_ID);
```

Prefer AssertJ Optional assertions when possible.

Example:

```java
assertThat(result)
        .as("repository should return the persisted order")
        .contains(expectedOrder);
```

---

# 18. Collection Assertions

Use collection assertions that express intent.

Examples:

```java
assertThat(events)
        .as("order creation should publish one domain event")
        .singleElement()
        .isInstanceOf(OrderCreatedEvent.class);
```

```java
assertThat(orders)
        .as("search result should contain only pending orders")
        .allMatch(order -> order.status() == OrderStatus.PENDING_APPROVAL);
```

Avoid manually iterating only to assert each element.

---

# 19. Predicate Assertions

Predicates should remain readable and contain one logical condition where practical.

Good:

```java
assertThat(orders)
        .as("all returned orders should belong to the requested customer")
        .allMatch(order -> order.customerId().equals(TestConstants.CUSTOMER_ID));
```

Avoid complex lambdas containing multiple branches and method calls.

Extract a named predicate or use object extraction.

---

# 20. Test Constants

Tests must use deterministic values.

Create shared constants for stable identifiers and timestamps.

Example:

```java
public final class TestConstants {

    public static final UUID ORDER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID CUSTOMER_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final Instant CREATED_AT =
            Instant.parse("2026-01-10T12:00:00Z");

    private TestConstants() {
    }
}
```

Avoid:

```java
UUID.randomUUID()

Instant.now()

LocalDate.now()
```

unless randomness or the real clock is explicitly under test.

---

# 21. Clock Control

Time-dependent code must depend on `Clock`.

Production:

```java
public final class OrderFactory {

    private final Clock clock;

    public OrderFactory(Clock clock) {
        this.clock = clock;
    }
}
```

Test:

```java
Clock fixedClock = Clock.fixed(
        TestConstants.CREATED_AT,
        ZoneOffset.UTC
);
```

This prevents flaky time assertions.

---

# 22. Test Fixtures

Use fixture classes to create valid domain objects.

Example:

```java
public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order validOrder() {
        return Order.create(
                TestConstants.ORDER_ID,
                TestConstants.CUSTOMER_ID,
                TestConstants.ORDER_ITEMS,
                TestConstants.CREATED_AT
        );
    }

    public static Order pendingApprovalOrder() {
        Order order = validOrder();
        order.submitForApproval();
        return order;
    }
}
```

Fixtures should create meaningful scenarios, not generic object graphs.

---

# 23. Fixture Naming

Fixture methods should describe the business state.

Good:

```java
validOrder()

pendingApprovalOrder()

cancelledOrder()

orderWithUnavailableInventory()

customerWithoutCredit()
```

Avoid:

```java
create()

build()

mock()

data()
```

---

# 24. Test Data Builders

Builders may be used when tests require controlled variation.

Example:

```java
OrderTestDataBuilder.anOrder()
        .withCustomerId(TestConstants.CUSTOMER_ID)
        .withStatus(OrderStatus.PENDING_APPROVAL)
        .build();
```

Builders should provide valid defaults.

Tests should override only fields relevant to the scenario.

---

# 25. Object Mother Usage

Object Mother patterns may be used for common valid scenarios.

Avoid a single giant test-data class serving unrelated bounded contexts.

Prefer bounded-context-specific fixtures.

Example:

```text
order/OrderFixture

payment/PaymentFixture

inventory/InventoryFixture
```

---

# 26. Mocking Principles

Mock architectural boundaries, not simple domain objects.

Suitable mocks:

- repositories
- gateways
- publishers
- clocks
- external clients
- application ports

Avoid mocking:

- value objects
- records
- collections
- simple entities
- aggregate behavior
- immutable DTOs

Real domain objects produce more meaningful tests.

---

# 27. Mockito Initialization

Use `@ExtendWith(MockitoExtension.class)` when annotations improve readability.

Example:

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderService service;
}
```

Direct mock creation is also acceptable.

Do not start a Spring context only to inject Mockito mocks.

---

# 28. Exact Mockito Arguments

Prefer exact arguments when values are relevant.

Good:

```java
verify(orderRepository).findById(TestConstants.ORDER_ID);
```

Avoid:

```java
verify(orderRepository).findById(any());
```

Broad matchers can allow regressions to pass unnoticed.

---

# 29. Argument Matchers

When one argument uses a matcher, all arguments in the same invocation must follow Mockito matcher rules.

Use specific matchers where possible.

Example:

```java
verify(eventPublisher).publish(
        eq(TestConstants.ORDER_ID),
        argThat(event -> event instanceof OrderCreatedEvent)
);
```

Do not use `anyString()` or `any()` when exact values are part of the contract.

---

# 30. Argument Captors

Use `ArgumentCaptor` when verifying a constructed object whose fields matter.

Example:

```java
ArgumentCaptor<OrderCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(OrderCreatedEvent.class);

verify(eventPublisher).publish(eventCaptor.capture());

assertThat(eventCaptor.getValue())
        .as("published event should contain the created order data")
        .extracting(
                OrderCreatedEvent::orderId,
                OrderCreatedEvent::customerId
        )
        .containsExactly(
                TestConstants.ORDER_ID,
                TestConstants.CUSTOMER_ID
        );
```

Avoid captors when a direct equality verification is sufficient.

---

# 31. Interaction Verification

Verify only interactions that represent observable behavior or architectural responsibility.

Useful:

```java
verify(orderRepository).save(order);
verify(eventPublisher).publish(expectedEvent);
```

Avoid verifying every internal method call.

Excessive verification couples tests to implementation details.

---

# 32. No More Interactions

`verifyNoMoreInteractions` should be used sparingly.

It is appropriate when additional calls would represent a meaningful defect.

Avoid using it as a universal test pattern because it makes refactoring unnecessarily difficult.

---

# 33. Stubbing Discipline

Create only stubs required by the test.

Unnecessary stubs should be removed.

Strict Mockito behavior helps identify:

- obsolete setup
- incorrect test design
- hidden implementation coupling

Do not globally enable lenient stubbing to silence test problems.

---

# 34. Void Method Stubbing

Use explicit stubbing only when the void method must throw or perform custom behavior.

Example:

```java
doThrow(new KafkaPublicationException("broker unavailable"))
        .when(eventPublisher)
        .publish(any(OrderCreatedEvent.class));
```

Default successful void behavior requires no stubbing.

---

# 35. Partial Mocks

Spies and partial mocks should be avoided.

They often indicate:

- excessive class responsibility
- hidden dependencies
- difficult-to-test design
- inheritance misuse

Refactor the class before introducing a spy whenever practical.

---

# 36. Static Mocking

Static mocking is discouraged.

Static dependencies should be wrapped behind injectable collaborators.

Examples:

- clock
- identifier generator
- environment provider
- security context accessor

Static mocking may be used only for legacy code under controlled refactoring.

---

# 37. Domain Unit Tests

Domain tests should validate:

- aggregate invariants
- state transitions
- value object validation
- domain event generation
- business calculations
- policy decisions

Domain tests must not depend on Spring.

Example:

```java
@Test
void testCancelShouldRejectAlreadyShippedOrder() {
    Order order = OrderFixture.shippedOrder();

    assertThatThrownBy(order::cancel)
            .as("shipped orders should not be cancellable")
            .isExactlyInstanceOf(OrderCannotBeCancelledException.class);
}
```

---

# 38. Value Object Tests

Value Object tests should validate:

- accepted values
- rejected values
- equality
- immutability
- normalization when required
- serialization contract when externally exposed

Example:

```java
@Test
void testOfShouldRejectNegativeAmount() {
    assertThatThrownBy(
            () -> Money.of(new BigDecimal("-1.00"), Currency.getInstance("BRL"))
    )
            .as("money should reject negative values when the business rule forbids them")
            .isExactlyInstanceOf(IllegalArgumentException.class);
}
```

---

# 39. State Machine Tests

State machines should use parameterized tests for allowed and forbidden transitions.

Example:

```java
@ParameterizedTest
@MethodSource("allowedTransitions")
void testTransitionShouldAcceptAllowedStatusChanges(
        OrderStatus current,
        OrderStatus target
) {
    Order order = OrderFixture.orderWithStatus(current);

    order.transitionTo(target);

    assertThat(order.status())
            .as("order should accept an allowed status transition")
            .isEqualTo(target);
}
```

Each parameterized case should remain identifiable in test reports.

---

# 40. Parameterized Tests

Use parameterized tests when several cases validate the same behavior.

Suitable sources:

- `@ValueSource`
- `@EnumSource`
- `@CsvSource`
- `@MethodSource`

Avoid combining unrelated scenarios into one parameterized test.

Prefer `@MethodSource` for complex domain cases.

---

# 41. Application Service Tests

Application service tests should validate:

- orchestration order where semantically relevant
- repository interaction
- dependency invocation
- transaction-independent behavior
- domain exception propagation
- integration exception translation
- event publication intent

Application tests should use real domain objects and mocked ports.

---

# 42. Command Handler Tests

Command handler tests should verify:

- command validation
- entity retrieval
- domain behavior invocation
- persistence
- event creation
- idempotency
- error propagation

Do not test Spring annotations in pure command-handler tests.

---

# 43. Query Service Tests

Query tests should validate:

- filters
- pagination
- sorting
- projection mapping
- empty results
- access restrictions
- query-port invocation

Avoid loading complete aggregates when testing read-model behavior.

---

# 44. Controller Slice Tests

Use `@WebMvcTest` for REST controller tests.

Example:

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;
}
```

Controller tests should validate:

- endpoint mapping
- request deserialization
- validation
- response serialization
- HTTP status
- headers
- Problem Details
- security rules

---

# 45. Controller Test Boundaries

Controller slice tests should mock the application use case, not repositories or external clients.

Correct:

```text
Controller → mocked use case
```

Incorrect:

```text
Controller → mocked repository
```

This preserves architectural boundaries.

---

# 46. Request Validation Tests

Validate each relevant contract rule.

Example:

```java
@Test
void testCreateShouldReturnBadRequestWhenCustomerIdIsMissing()
        throws Exception {

    mockMvc.perform(
                    post("/api/v1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(TestJson.invalidOrderWithoutCustomerId())
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                    .value("REQUEST_VALIDATION_FAILED"))
            .andExpect(jsonPath("$.errors[0].field")
                    .value("customerId"));
}
```

Validation tests should not depend on translated messages unless message text is part of the API contract.

---

# 47. Problem Details Tests

Error response tests should verify:

- status
- type
- title
- stable code
- safe detail
- trace identifier
- validation fields when applicable

Avoid asserting unstable values such as exact timestamps unless controlled.

---

# 48. Security Tests

Controller and integration tests should validate:

- unauthenticated access
- insufficient authority
- allowed authority
- ownership restrictions
- tenant restrictions
- protected actuator endpoints

Use Spring Security test support.

Example:

```java
@WithMockUser(authorities = "order:approve")
```

Do not disable security in all controller tests.

---

# 49. Repository Slice Tests

Use `@DataJpaTest` for repository behavior.

Validate:

- mappings
- constraints
- queries
- projections
- optimistic locking
- pagination
- sorting
- custom converters

Do not use repository tests to validate business rules already covered in domain tests.

---

# 50. PostgreSQL Compatibility

H2 must not be treated as equivalent to PostgreSQL.

Use PostgreSQL Testcontainers when testing:

- JSONB
- native SQL
- case-insensitive behavior
- indexes
- sequences
- locks
- transactions
- database functions
- PostgreSQL-specific types
- constraint behavior

Production-database compatibility is more important than test startup convenience.

---

# 51. Flyway Tests

Integration tests should run Flyway migrations against a clean PostgreSQL container.

Verify:

- migration ordering
- schema creation
- constraints
- seed data where applicable
- application compatibility

Never alter an already applied migration.

Any correction requires a new migration version.

---

# 52. Migration Validation Test

A CI integration stage should:

1. start an empty PostgreSQL instance
2. apply all migrations
3. start the application context
4. execute persistence smoke tests
5. validate the Flyway schema history

This protects both new installations and upgrade paths.

---

# 53. Upgrade Migration Tests

For critical schema changes, test upgrades from a representative previous version.

Example:

```text
V1 to current

last production version to current
```

Validate:

- data preservation
- new constraints
- backfill correctness
- application startup
- rollback or forward-fix strategy

---

# 54. Testcontainers

Use Testcontainers for infrastructure integrations.

Typical infrastructure:

- PostgreSQL Testcontainer
- Redis Testcontainer
- LocalStack when SQS/AWS emulation is justified

Tests must not depend on manually installed local services.

---

# 55. Container Lifecycle

Prefer shared container lifecycle per test class or suite when isolation is preserved.

Avoid starting a new heavy container for every test method.

Container reuse may be enabled locally but must not compromise CI determinism.

---

# 56. Dynamic Properties

Use `@DynamicPropertySource` to supply container configuration.

Example:

```java
@DynamicPropertySource
static void configureProperties(
        DynamicPropertyRegistry registry
) {
    registry.add(
            "spring.datasource.url",
            POSTGRESQL_CONTAINER::getJdbcUrl
    );

    registry.add(
            "spring.datasource.username",
            POSTGRESQL_CONTAINER::getUsername
    );

    registry.add(
            "spring.datasource.password",
            POSTGRESQL_CONTAINER::getPassword
    );
}
```

Do not hardcode container ports.

---

# 57. Integration Test Isolation

Each integration test must have isolated data.

Possible strategies:

- transaction rollback
- explicit cleanup
- schema recreation
- unique deterministic business keys
- dedicated test database

Avoid relying on execution order.

---

# 58. Transactional Tests

`@Transactional` tests may hide production transaction behavior.

Use them carefully.

They are suitable for isolated repository assertions, but less suitable for validating:

- commit-time constraints
- transaction listeners
- outbox publication
- after-commit callbacks
- cross-thread visibility
- asynchronous processing

Use explicit commit behavior when required.

---

# 59. Messaging Tests

Messaging tests should validate:

- serialization
- event envelope
- SQS queue destination
- FIFO MessageGroupId / deduplication ID when applicable
- headers
- consumer deserialization
- idempotency
- retry handling
- dead-letter routing

Do not validate SQS behavior only by mocking the AWS SDK client.

---

# 60. Event Contract Tests

Each published event should have contract tests for:

- event type
- version
- required metadata
- payload schema
- serialized field names
- enum representation
- timestamp format
- backward compatibility

Example event fields:

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

# 61. JSON Serialization Tests

Use the application-configured `ObjectMapper`.

Example:

```java
@JsonTest
class OrderCreatedEventJsonTest {
}
```

Validate exact external contract names, not Java implementation details.

---

# 62. SQS Integration Tests

SQS integration tests SHOULD use LocalStack or another approved SQS-compatible test environment when broker semantics matter.

Verify applicable behavior such as:

```text
SEND / RECEIVE
MESSAGE ATTRIBUTES
FIFO GROUP ORDERING
DUPLICATE / REDELIVERY SAFETY
VISIBILITY TIMEOUT
DLQ / REDRIVE
```

Do not call production AWS services from automated tests.

---

# 63. Outbox Tests

Transactional Outbox tests should validate:

- business data and outbox event persist atomically
- rollback removes both changes
- dispatcher selects only eligible events
- successful publication marks events as sent
- failure increments attempts
- retry scheduling is calculated correctly
- exhausted events are classified correctly
- concurrent dispatchers do not publish the same row twice

---

# 64. Outbox Atomicity Test

A critical integration test should verify that an application failure rolls back both the aggregate and outbox insert.

Example scenario:

```text
save order

insert outbox event

throw exception

commit rejected
```

Expected:

```text
no order persisted

no outbox event persisted
```

---

# 65. Idempotency Tests

Idempotency tests should validate:

- first request is processed
- exact replay returns the previous result
- same key with a different payload is rejected
- concurrent requests do not create duplicates
- completed records remain queryable
- expired keys follow the retention policy

Concurrency must be tested with controlled synchronization.

---

# 66. Contract Testing

Use consumer-driven or provider contract tests for external service boundaries where independent deployment creates compatibility risk.

Contracts should cover:

- paths
- methods
- headers
- request schema
- response schema
- status codes
- error formats
- optional fields
- enum compatibility

Contract tests complement integration tests; they do not replace them.

---

# 67. REST Client Tests

HTTP client tests should validate:

- URL construction
- headers
- token propagation
- timeouts
- successful mapping
- remote error translation
- response sanitization
- retry classification
- correlation propagation

Use a controlled HTTP stub server rather than mocking every client-library method.

---

# 68. External Error Tests

Test relevant status mappings.

Examples:

```text
404 → remote resource not found

409 → remote conflict

422 → remote business rejection

429 → rate limited

503 → dependency unavailable

504 → dependency timeout
```

Verify both exception type and retry classification.

---

# 69. Resilience Tests

Resilience tests should validate:

- timeout behavior
- retry count
- backoff policy
- circuit breaker opening
- half-open recovery
- fallback semantics
- bulkhead rejection
- retry exhaustion

Do not assert only configuration values; test observable behavior.

---

# 70. Retry Tests

Retry tests must avoid real long delays.

Use:

- configurable short test delays
- virtual or controlled clocks where supported
- synchronization mechanisms
- policy-specific test configuration

Do not use `Thread.sleep`.

---

# 71. Circuit Breaker Tests

Circuit breaker tests should validate:

- failures are recorded
- threshold opens the circuit
- calls are rejected while open
- half-open probes are limited
- successful probes close the circuit
- fallback behavior remains semantically safe

Keep tests deterministic through controlled configuration.

---

# 72. Cache Tests

Cache tests should validate:

- hit
- miss
- TTL where relevant
- eviction
- serialization
- fallback
- authoritative source recovery
- stale-value behavior
- optional cache outage

Do not treat the cache as the authoritative source unless explicitly designed that way.

---

# 73. Redis Integration Tests

Use Redis Testcontainers for behavior that depends on:

- serialization
- TTL
- distributed key format
- connection failure handling
- atomic operations
- locks
- eviction behavior

Mock-based cache tests are insufficient for Redis-specific semantics.

---

# 74. Concurrency Tests

Concurrency tests should validate real business risks.

Examples:

- duplicate order creation
- optimistic locking
- inventory over-reservation
- outbox duplicate dispatch
- concurrent idempotency keys
- cache race conditions

Do not add concurrency merely to make tests appear sophisticated.

---

# 75. Virtual Thread Tests

Virtual-thread tests should validate:

- task completion
- exception propagation
- cancellation
- context propagation
- bounded external resource usage
- executor lifecycle

Do not assert internal thread scheduling order.

Virtual threads do not guarantee execution order.

---

# 76. Context Propagation Tests

Asynchronous tests should verify propagation of:

- SecurityContext
- correlation ID
- trace context
- locale where required
- request metadata where intentionally supported

Clear context after execution to avoid leakage between tasks.

---

# 77. No Thread.sleep

`Thread.sleep` is prohibited in tests.

Use:

- Awaitility
- latches
- barriers
- futures
- deterministic polling
- controlled clocks
- explicit synchronization

Example:

```java
await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
                assertThat(repository.findById(TestConstants.ORDER_ID))
                        .as("asynchronous processing should persist the order")
                        .isPresent()
        );
```

---

# 78. Awaitility

Use Awaitility only for genuinely asynchronous behavior.

Every wait must define:

- maximum duration
- meaningful assertion
- stable polling strategy when needed

Avoid using Awaitility to hide unpredictable synchronous code.

---

# 79. Latches and Barriers

Use `CountDownLatch`, `CyclicBarrier` or `Phaser` for controlled concurrency scenarios.

Example use cases:

- release two threads at the same point
- hold repository execution before commit
- reproduce concurrent update behavior

Always define timeouts to prevent hanging tests.

---

# 80. Executor Lifecycle

Tests that create executors must close them.

Example:

```java
try (ExecutorService executor =
             Executors.newVirtualThreadPerTaskExecutor()) {
    // test
}
```

Do not leak test threads across the suite.

---

# 81. Performance Tests

Performance tests should define explicit objectives.

Examples:

- maximum latency
- throughput
- error rate
- concurrency
- resource usage
- sustained load duration

Performance tests must not rely on unit-test frameworks alone for production conclusions.

---

# 82. Performance Test Types

The platform may use:

- smoke tests
- load tests
- stress tests
- spike tests
- endurance tests
- capacity tests

Each test must state its purpose and pass criteria.

---

# 83. Performance Baselines

Record baselines for critical flows.

Examples:

```text
create order p95 latency

search orders p95 latency

SQS consumer throughput

outbox dispatch throughput

database query p95 latency
```

Compare results against previous stable releases.

---

# 84. Performance Assertions

Avoid overly strict timing assertions in unit tests.

Incorrect:

```java
assertThat(elapsed)
        .as("operation should complete quickly")
        .isLessThan(Duration.ofMillis(10));
```

Such tests are environment-sensitive.

Use dedicated performance environments for meaningful thresholds.

---

# 85. Architecture Tests

Use ArchUnit to enforce:

- domain independence
- allowed layer dependencies
- no package cycles
- controller boundaries
- repository isolation
- adapter implementation rules
- naming conventions
- annotation restrictions

Architecture tests should run in the standard test pipeline.

---

# 86. ArchUnit Example

```java
@ArchTest
static final ArchRule domainMustNotDependOnSpring =
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..");
```

Add `.as("...")` to AssertJ assertions; ArchUnit rules should use clear rule descriptions through their own API.

---

# 87. Package Cycle Test

```java
@ArchTest
static final ArchRule packagesShouldBeFreeOfCycles =
        slices()
                .matching(
                        "com.enterprise.orderplatform.(*).."
                )
                .should()
                .beFreeOfCycles();
```

Cycle rules should match the actual modular structure.

---

# 88. Mutation Testing

Mutation testing may be used for critical domain logic.

Suitable areas:

- financial calculations
- approval policies
- state transitions
- eligibility rules
- idempotency decisions

Mutation score is a quality indicator, not an isolated target.

Do not apply mutation testing indiscriminately to generated or trivial code.

---

# 89. JaCoCo Coverage

JaCoCo is used to measure test coverage.

Default minimum project target:

```text
80%
```

Coverage should be evaluated by:

- module
- package
- critical class
- branch
- line

Critical domain logic may require higher thresholds.

---

# 90. Coverage Quality

Coverage alone does not prove correctness.

A class can have 100% line coverage while failing to validate:

- business outcomes
- exception types
- state changes
- integration contracts
- concurrency behavior

Tests must contain meaningful assertions.

---

# 91. Coverage Exclusions

Exclusions must be minimal and justified.

Potential exclusions:

- generated classes
- framework bootstrap class
- configuration metadata
- generated mappers when independently validated
- trivial immutable DTOs only when coverage adds no value

Do not exclude difficult code merely to satisfy a coverage threshold.

---

# 92. Branch Coverage

Branch coverage is especially important for:

- business rules
- status transitions
- fallback logic
- exception translation
- retry classification
- feature flags
- authorization decisions

Uncovered branches should be reviewed for missing scenarios or dead code.

---

# 93. SonarQube Test Standards

Tests must satisfy the same maintainability expectations as production code.

Required practices include:

- AssertJ `.as("...")` descriptions
- no duplicated test logic
- no ignored exceptions
- no random unstable values
- no unnecessary assertions
- no empty tests
- no disabled tests without justification
- no excessive cognitive complexity
- no resource leaks
- no `Thread.sleep`

---

# 94. Disabled Tests

Disabled tests require:

- documented reason
- issue reference
- planned resolution

Example:

```java
@Disabled("Issue EOP-432: awaiting payment sandbox correction")
```

Do not commit unexplained disabled tests.

Long-term disabled tests should be removed or repaired.

---

# 95. Flaky Tests

Flaky tests are defects.

When a flaky test is identified:

1. stop relying on repeated execution
2. identify the nondeterministic dependency
3. fix synchronization, data or time control
4. add diagnostics
5. restore stable execution

Do not use retries as a permanent solution for flaky tests.

---

# 96. Test Retries

Automatic retries may be used temporarily to diagnose infrastructure instability.

They must not mask:

- race conditions
- shared-state contamination
- time dependence
- random data collisions
- unreliable external environments

A test that passes only after retry remains defective.

---

# 97. Test Ordering

Tests must not depend on method or class execution order.

Avoid:

```java
@TestMethodOrder(...)
```

unless testing an explicitly stateful external workflow where isolation is impossible and the rationale is documented.

Prefer independent setup and cleanup.

---

# 98. Shared State

Tests must not mutate shared static state.

Avoid:

- mutable static collections
- shared random generators with uncontrolled state
- static mocks left open
- global system properties without restoration
- persistent MDC values
- leaked security contexts

Restore any temporary global configuration after the test.

---

# 99. Environment Isolation

Tests must not depend on:

- developer machine paths
- local credentials
- local database installations
- local SQS-compatible infrastructure
- current locale
- current timezone
- execution from an IDE
- previous test artifacts

CI and local execution should produce equivalent results.

---

# 100. Timezone and Locale

Set explicit timezone and locale when behavior depends on them.

Example:

```java
ZoneId zoneId = ZoneId.of("UTC");
Locale locale = Locale.ENGLISH;
```

Do not rely on the workstation default.

---

# 101. File Tests

Temporary files should use JUnit temporary directories.

Example:

```java
@TempDir
Path temporaryDirectory;
```

Clean up resources and validate encoding explicitly.

Use UTF-8 unless the external contract requires another encoding.

---

# 102. Test Resource Files

Test resource names should describe the scenario.

Examples:

```text
valid-order-created-event.json

invalid-order-missing-customer.json

legacy-order-record.txt
```

Avoid:

```text
test1.json

data.json

input.txt
```

---

# 103. Snapshot Testing

Snapshot testing may be used for stable complex contracts.

Suitable examples:

- OpenAPI fragments
- event JSON
- reports
- generated configuration

Snapshots must be reviewed carefully.

Do not accept broad snapshot changes without understanding the contract impact.

---

# 104. API Compatibility Tests

Compatibility tests should protect:

- required response fields
- field names
- enum values
- status codes
- error codes
- event versions
- default behavior
- nullability

Breaking changes require explicit versioning or migration planning.

---

# 105. OpenAPI Tests

Validate generated OpenAPI against the intended API contract.

Check:

- operation IDs
- paths
- request schemas
- response schemas
- security requirements
- Problem Details
- examples
- required fields

The documentation must not drift from runtime behavior.

---

# 106. Security Testing

Security tests should cover:

- broken authentication
- broken authorization
- object-level access control
- tenant isolation
- input validation
- mass assignment
- sensitive data exposure
- insecure defaults
- security headers
- actuator exposure

Automated tests complement, but do not replace, SAST and penetration testing.

---

# 107. SAST and Dependency Scanning

The pipeline should include:

- static analysis
- dependency vulnerability scanning
- secret detection
- container image scanning
- infrastructure manifest scanning

Findings must be triaged by severity and exploitability.

Tests should be added for corrected business or security regressions.

---

# 108. Regression Tests

Every corrected defect should include a regression test when technically feasible.

The test should reproduce the original failure and validate the corrected behavior.

Use a business-oriented test name rather than the issue number alone.

Good:

```java
testCheckoutShouldAcceptSegmentContainingAmpersand()
```

Avoid:

```java
testBug1639()
```

The issue reference may remain in the test documentation or commit.

---

# 109. Production Defect Reproduction

When reproducing a production issue:

- minimize the failing scenario
- sanitize production data
- make the test deterministic
- preserve the relevant boundary condition
- avoid copying full sensitive payloads
- document the root cause

The regression test should remain valuable after the incident is closed.

---

# 110. Test Review Checklist

Before approving a test, verify:

- Does the name explain behavior?
- Does the test validate one cohesive scenario?
- Is the data deterministic?
- Does every AssertJ chain include `.as("...")`?
- Are mocks limited to boundaries?
- Are exact arguments used when relevant?
- Is time controlled?
- Is concurrency deterministic?
- Is `Thread.sleep` absent?
- Is the test independent?
- Is the failure message meaningful?
- Does the test avoid implementation coupling?
- Does it cover the relevant failure path?
- Are resources closed?
- Is the selected test scope appropriate?

---

# 111. Anti-Patterns

The following practices are prohibited:

- tests without assertions
- random UUIDs for normal scenarios
- `Instant.now()` in deterministic tests
- `Thread.sleep`
- broad `any()` matchers without need
- mocking domain value objects
- field injection in tests
- full Spring context for simple unit tests
- test-order dependence
- unexplained disabled tests
- swallowed exceptions
- shared mutable static state
- exact performance timing in unit tests
- coverage-only tests with no meaningful behavior
- changing production logic only to satisfy a weak test
- modifying already applied Flyway migrations
- accepting flaky tests as normal

---

# 112. Architecture Rules

Testing must:

- validate behavior over implementation
- preserve deterministic execution
- use the narrowest effective scope
- isolate external infrastructure
- use realistic integration environments
- protect public contracts
- validate failure paths
- control time and concurrency
- enforce architectural boundaries
- satisfy code-quality standards
- provide meaningful operational confidence

---

# 113. Decision Summary

The project adopts:

- JUnit 5
- AssertJ with mandatory `.as("...")` descriptions
- Mockito for architectural boundaries
- deterministic test constants
- controlled `Clock`
- business-oriented fixtures
- Spring slice tests
- PostgreSQL and Redis Testcontainers plus LocalStack/SQS-compatible infrastructure
- real migration validation with Flyway
- contract tests for external boundaries
- Outbox and idempotency integration tests
- deterministic concurrency testing
- no `Thread.sleep`
- ArchUnit enforcement
- JaCoCo minimum project coverage of 80%
- regression tests for corrected defects
- strict flaky-test remediation
