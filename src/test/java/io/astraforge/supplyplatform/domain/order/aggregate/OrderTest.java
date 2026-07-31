package io.astraforge.supplyplatform.domain.order.aggregate;

import io.astraforge.supplyplatform.domain.order.event.DomainEvent;
import io.astraforge.supplyplatform.domain.order.event.OrderCreated;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final OrderId ORDER_ID = new OrderId(UUID.fromString("10000000-0000-0000-0000-000000000001"));
    private static final CustomerId CUSTOMER_ID = new CustomerId(UUID.fromString("20000000-0000-0000-0000-000000000002"));
    private static final UserId USER_ID = new UserId(UUID.fromString("30000000-0000-0000-0000-000000000003"));
    private static final Instant CREATED_AT = Instant.parse("2026-07-30T20:00:00Z");
    private static final CorrelationId CORRELATION_ID = new CorrelationId("order-flow-001");

    @Test
    void testCreateShouldInitializeDraftOrderAndRecordEvent() {
        Order order = createOrder();

        assertThat(order.id())
                .as("created order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(order.customerReference())
                .as("created order customer reference")
                .isEqualTo(new CustomerReference(CUSTOMER_ID));
        assertThat(order.status())
                .as("initial order status")
                .isEqualTo(OrderStatus.DRAFT);
        assertThat(order.version())
                .as("initial aggregate version")
                .isZero();
        assertThat(order.createdAt())
                .as("order creation timestamp")
                .isEqualTo(CREATED_AT);
        assertThat(order.updatedAt())
                .as("initial update timestamp")
                .isEqualTo(CREATED_AT);
        assertThat(order.domainEvents())
                .as("creation event collection")
                .singleElement()
                .isInstanceOf(OrderCreated.class);
    }

    @Test
    void testCreateShouldPopulateOrderCreatedEvent() {
        Order order = createOrder();

        OrderCreated event = (OrderCreated) order.domainEvents().getFirst();

        assertThat(event.orderId())
                .as("event order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(event.customerId())
                .as("event customer identifier")
                .isEqualTo(CUSTOMER_ID);
        assertThat(event.createdBy())
                .as("event creator")
                .isEqualTo(USER_ID);
        assertThat(event.occurredAt())
                .as("event occurrence timestamp")
                .isEqualTo(CREATED_AT);
        assertThat(event.correlationId())
                .as("event correlation identifier")
                .isEqualTo(CORRELATION_ID);
        assertThat(event.eventId())
                .as("event identifier")
                .isNotNull();
    }

    @Test
    void testDomainEventsShouldReturnImmutableSnapshot() {
        Order order = createOrder();
        List<DomainEvent> events = order.domainEvents();

        assertThatThrownBy(() -> events.clear())
                .as("domain event snapshot must be immutable")
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(order.domainEvents())
                .as("aggregate events remain registered")
                .hasSize(1);
    }

    @Test
    void testPullDomainEventsShouldReturnAndClearPendingEvents() {
        Order order = createOrder();

        List<DomainEvent> pulledEvents = order.pullDomainEvents();

        assertThat(pulledEvents)
                .as("pulled domain events")
                .hasSize(1);
        assertThat(order.domainEvents())
                .as("pending events after pull")
                .isEmpty();
    }

    private static Order createOrder() {
        return Order.create(
                ORDER_ID,
                new CustomerReference(CUSTOMER_ID),
                USER_ID,
                CREATED_AT,
                CORRELATION_ID);
    }
}
