package io.astraforge.supplyplatform.domain.order.aggregate;

import io.astraforge.supplyplatform.domain.order.entity.OrderItem;
import io.astraforge.supplyplatform.domain.order.event.DomainEvent;
import io.astraforge.supplyplatform.domain.order.event.OrderCreated;
import io.astraforge.supplyplatform.domain.order.event.OrderItemAdded;
import io.astraforge.supplyplatform.domain.order.event.OrderItemQuantityChanged;
import io.astraforge.supplyplatform.domain.order.event.OrderItemRemoved;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateOrderItemException;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateProductException;
import io.astraforge.supplyplatform.domain.order.exception.OrderItemNotFoundException;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductReference;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final OrderId ORDER_ID =
            new OrderId(UUID.fromString("10000000-0000-0000-0000-000000000001"));
    private static final CustomerId CUSTOMER_ID =
            new CustomerId(UUID.fromString("20000000-0000-0000-0000-000000000002"));
    private static final UserId USER_ID =
            new UserId(UUID.fromString("30000000-0000-0000-0000-000000000003"));
    private static final ProductId PRODUCT_ID =
            new ProductId(UUID.fromString("40000000-0000-0000-0000-000000000004"));
    private static final ProductId SECOND_PRODUCT_ID =
            new ProductId(UUID.fromString("40000000-0000-0000-0000-000000000014"));
    private static final OrderItemId ITEM_ID =
            new OrderItemId(UUID.fromString("50000000-0000-0000-0000-000000000005"));
    private static final OrderItemId SECOND_ITEM_ID =
            new OrderItemId(UUID.fromString("50000000-0000-0000-0000-000000000015"));
    private static final Instant CREATED_AT = Instant.parse("2026-07-30T20:00:00Z");
    private static final Instant CHANGED_AT = Instant.parse("2026-07-30T20:05:00Z");
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
        assertThat(order.items())
                .as("initial order items")
                .isEmpty();
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
    void testAddItemShouldAddEntityIncrementVersionAndRecordEvent() {
        Order order = createOrderWithoutPendingEvents();
        Quantity quantity = quantity("2.000");

        order.addItem(
                ITEM_ID,
                productSnapshot(PRODUCT_ID, "SKU-001"),
                quantity,
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);

        OrderItem item = order.items().getFirst();
        OrderItemAdded event = (OrderItemAdded) order.domainEvents().getFirst();
        assertThat(item.id())
                .as("added order item identifier")
                .isEqualTo(ITEM_ID);
        assertThat(item.quantity())
                .as("added order item quantity")
                .isEqualTo(quantity);
        assertThat(order.version())
                .as("aggregate version after adding an item")
                .isEqualTo(1L);
        assertThat(order.updatedAt())
                .as("aggregate update timestamp after adding an item")
                .isEqualTo(CHANGED_AT);
        assertThat(event.orderItemId())
                .as("added item identifier in the event")
                .isEqualTo(ITEM_ID);
        assertThat(event.productId())
                .as("added product identifier in the event")
                .isEqualTo(PRODUCT_ID);
        assertThat(event.aggregateVersion())
                .as("aggregate version in the item-added event")
                .isEqualTo(1L);
        assertThat(event.addedBy())
                .as("actor in the item-added event")
                .isEqualTo(USER_ID);
    }

    @Test
    void testAddItemShouldRejectDuplicateItemIdentifier() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);

        assertThatThrownBy(() -> order.addItem(
                ITEM_ID,
                productSnapshot(SECOND_PRODUCT_ID, "SKU-002"),
                quantity("1.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID))
                .as("duplicate order item identifier")
                .isInstanceOf(DuplicateOrderItemException.class)
                .hasMessage("Order item ID already exists in the order");
    }

    @Test
    void testAddItemShouldRejectDuplicateProduct() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);

        assertThatThrownBy(() -> order.addItem(
                SECOND_ITEM_ID,
                productSnapshot(PRODUCT_ID, "SKU-001"),
                quantity("1.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID))
                .as("duplicate product in the order")
                .isInstanceOf(DuplicateProductException.class)
                .hasMessage("Product already exists in the order");
    }

    @Test
    void testUpdateItemQuantityShouldChangeQuantityAndRecordEvent() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);
        order.pullDomainEvents();
        Quantity newQuantity = quantity("5.000");

        order.updateItemQuantity(
                ITEM_ID,
                newQuantity,
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);

        OrderItemQuantityChanged event =
                (OrderItemQuantityChanged) order.domainEvents().getFirst();
        assertThat(order.items().getFirst().quantity())
                .as("updated order item quantity")
                .isEqualTo(newQuantity);
        assertThat(order.version())
                .as("aggregate version after changing quantity")
                .isEqualTo(2L);
        assertThat(event.previousQuantity())
                .as("previous quantity in the event")
                .isEqualTo(quantity("2.000"));
        assertThat(event.newQuantity())
                .as("new quantity in the event")
                .isEqualTo(newQuantity);
        assertThat(event.aggregateVersion())
                .as("aggregate version in the quantity-changed event")
                .isEqualTo(2L);
    }

    @Test
    void testUpdateItemQuantityShouldIgnoreUnchangedQuantity() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);
        order.pullDomainEvents();

        order.updateItemQuantity(
                ITEM_ID,
                quantity("2.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);

        assertThat(order.version())
                .as("aggregate version after an idempotent quantity update")
                .isEqualTo(1L);
        assertThat(order.domainEvents())
                .as("events after an idempotent quantity update")
                .isEmpty();
    }

    @Test
    void testRemoveItemShouldRemoveEntityAndRecordEvent() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);
        order.pullDomainEvents();

        order.removeItem(ITEM_ID, USER_ID, CHANGED_AT, CORRELATION_ID);

        OrderItemRemoved event = (OrderItemRemoved) order.domainEvents().getFirst();
        assertThat(order.items())
                .as("items after removal")
                .isEmpty();
        assertThat(order.version())
                .as("aggregate version after removing an item")
                .isEqualTo(2L);
        assertThat(event.orderItemId())
                .as("removed item identifier in the event")
                .isEqualTo(ITEM_ID);
        assertThat(event.previousQuantity())
                .as("removed item quantity in the event")
                .isEqualTo(quantity("2.000"));
        assertThat(event.aggregateVersion())
                .as("aggregate version in the item-removed event")
                .isEqualTo(2L);
    }

    @Test
    void testItemOperationsShouldRejectUnknownItem() {
        Order order = createOrderWithoutPendingEvents();

        assertThatThrownBy(() -> order.updateItemQuantity(
                ITEM_ID,
                quantity("3.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID))
                .as("quantity change for an unknown order item")
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessage("Order item was not found in the order");
    }

    @Test
    void testItemsShouldReturnImmutableSnapshot() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);
        List<OrderItem> items = order.items();

        assertThatThrownBy(items::clear)
                .as("order item snapshot must be immutable")
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(order.items())
                .as("aggregate items remain registered")
                .hasSize(1);
    }

    @Test
    void testDomainEventsShouldReturnImmutableSnapshot() {
        Order order = createOrder();
        List<DomainEvent> events = order.domainEvents();

        assertThatThrownBy(events::clear)
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

    private static Order createOrderWithoutPendingEvents() {
        Order order = createOrder();
        order.pullDomainEvents();
        return order;
    }

    private static void addItem(Order order, OrderItemId itemId, ProductId productId) {
        order.addItem(
                itemId,
                productSnapshot(productId, "SKU-001"),
                quantity("2.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);
    }

    private static ProductSnapshot productSnapshot(ProductId productId, String sku) {
        return new ProductSnapshot(
                new ProductReference(productId),
                sku,
                "Industrial Pump",
                "UNIT");
    }

    private static Quantity quantity(String value) {
        return new Quantity(new BigDecimal(value));
    }
}
