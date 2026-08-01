package io.astraforge.supplyplatform.domain.order.aggregate;

import io.astraforge.supplyplatform.domain.order.entity.OrderItem;
import io.astraforge.supplyplatform.domain.order.event.DomainEvent;
import io.astraforge.supplyplatform.domain.order.event.OrderApprovalStarted;
import io.astraforge.supplyplatform.domain.order.event.OrderApproved;
import io.astraforge.supplyplatform.domain.order.event.OrderCreated;
import io.astraforge.supplyplatform.domain.order.event.OrderItemAdded;
import io.astraforge.supplyplatform.domain.order.event.OrderItemQuantityChanged;
import io.astraforge.supplyplatform.domain.order.event.OrderItemRemoved;
import io.astraforge.supplyplatform.domain.order.event.OrderItemPriced;
import io.astraforge.supplyplatform.domain.order.event.OrderItemPricingInvalidated;
import io.astraforge.supplyplatform.domain.order.event.OrderRejected;
import io.astraforge.supplyplatform.domain.order.event.OrderReviewRequested;
import io.astraforge.supplyplatform.domain.order.event.OrderSubmitted;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateOrderItemException;
import io.astraforge.supplyplatform.domain.order.exception.DuplicateProductException;
import io.astraforge.supplyplatform.domain.order.exception.OrderItemNotFoundException;
import io.astraforge.supplyplatform.domain.order.exception.OrderApprovalNotAllowedException;
import io.astraforge.supplyplatform.domain.order.exception.OrderCurrencyMismatchException;
import io.astraforge.supplyplatform.domain.order.exception.OrderPricingIncompleteException;
import io.astraforge.supplyplatform.domain.order.exception.OrderSubmissionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.ApprovalComment;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.Money;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;
import io.astraforge.supplyplatform.domain.order.valueobject.Percentage;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductReference;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
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
    private static final UserId APPROVER_ID =
            new UserId(UUID.fromString("30000000-0000-0000-0000-000000000013"));
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


    @Test
    void testApplyItemPricingShouldPriceItemAndCalculateOrderTotals() {
        Order order = createOrderWithoutPendingEvents();
        order.addItem(
                ITEM_ID,
                productSnapshot(PRODUCT_ID, "SKU-001"),
                quantity("2.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);
        order.pullDomainEvents();
        ItemPricing pricing = itemPricing("100.00", "10.0000", "20.0000");
        Instant pricedAt = Instant.parse("2026-07-30T20:10:00Z");

        order.applyItemPricing(ITEM_ID, pricing, USER_ID, pricedAt, CORRELATION_ID);

        OrderItem item = order.items().getFirst();
        OrderTotals totals = order.totals();
        OrderItemPriced event = (OrderItemPriced) order.domainEvents().getFirst();
        assertThat(item.pricing())
                .as("pricing applied to the order item")
                .contains(pricing);
        assertThat(order.pricingComplete())
                .as("order pricing completeness")
                .isTrue();
        assertThat(totals.subtotal().amount())
                .as("order subtotal")
                .isEqualByComparingTo("200.00");
        assertThat(totals.discount().amount())
                .as("order discount")
                .isEqualByComparingTo("20.00");
        assertThat(totals.tax().amount())
                .as("order tax")
                .isEqualByComparingTo("36.00");
        assertThat(totals.total().amount())
                .as("order final total")
                .isEqualByComparingTo("216.00");
        assertThat(event.pricing())
                .as("pricing recorded by the domain event")
                .isEqualTo(pricing);
    }

    @Test
    void testUpdateItemQuantityShouldInvalidateExistingPricing() {
        Order order = createPricedOrder();
        order.pullDomainEvents();
        Instant changedAt = Instant.parse("2026-07-30T20:15:00Z");

        order.updateItemQuantity(
                ITEM_ID,
                quantity("3.000"),
                USER_ID,
                changedAt,
                CORRELATION_ID);

        assertThat(order.items().getFirst().pricing())
                .as("item pricing invalidated after quantity change")
                .isEmpty();
        assertThat(order.pricingComplete())
                .as("order pricing becomes incomplete after quantity change")
                .isFalse();
        assertThat(order.domainEvents())
                .as("quantity change and pricing invalidation events")
                .hasSize(2)
                .anySatisfy(event -> assertThat(event)
                        .as("pricing invalidation event")
                        .isInstanceOf(OrderItemPricingInvalidated.class));
    }

    @Test
    void testTotalsShouldRejectOrderWithUnpricedItem() {
        Order order = createOrderWithoutPendingEvents();
        order.addItem(
                ITEM_ID,
                productSnapshot(PRODUCT_ID, "SKU-001"),
                quantity("1.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);

        assertThatThrownBy(order::totals)
                .as("order totals require complete pricing")
                .isInstanceOf(OrderPricingIncompleteException.class)
                .hasMessage("Order totals require pricing for every order item");
    }

    @Test
    void testApplyItemPricingShouldRejectCurrencyDifferentFromExistingItems() {
        Order order = createOrderWithoutPendingEvents();
        order.addItem(
                ITEM_ID,
                productSnapshot(PRODUCT_ID, "SKU-001"),
                quantity("1.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);
        order.addItem(
                SECOND_ITEM_ID,
                productSnapshot(SECOND_PRODUCT_ID, "SKU-002"),
                quantity("1.000"),
                USER_ID,
                CHANGED_AT.plusSeconds(1),
                CORRELATION_ID);
        order.applyItemPricing(
                ITEM_ID,
                itemPricing("10.00", "0.0000", "0.0000"),
                USER_ID,
                CHANGED_AT.plusSeconds(2),
                CORRELATION_ID);

        assertThatThrownBy(() -> order.applyItemPricing(
                SECOND_ITEM_ID,
                new ItemPricing(
                        new Money(new BigDecimal("12.00"), Currency.getInstance("USD")),
                        percentage("0.0000"),
                        percentage("0.0000")),
                USER_ID,
                CHANGED_AT.plusSeconds(3),
                CORRELATION_ID))
                .as("all priced order items require the same currency")
                .isInstanceOf(OrderCurrencyMismatchException.class)
                .hasMessage("All order items must use the same currency");
    }

    @Test
    void testApplySamePricingShouldBeIdempotent() {
        Order order = createPricedOrder();
        order.pullDomainEvents();
        long versionBefore = order.version();
        ItemPricing existingPricing = order.items().getFirst().pricing().orElseThrow();

        order.applyItemPricing(
                ITEM_ID,
                existingPricing,
                USER_ID,
                CHANGED_AT.plusSeconds(30),
                CORRELATION_ID);

        assertThat(order.version())
                .as("aggregate version remains unchanged for identical pricing")
                .isEqualTo(versionBefore);
        assertThat(order.domainEvents())
                .as("no event is emitted for identical pricing")
                .isEmpty();
    }


    @Test
    void testSubmitShouldTransitionPricedDraftOrderAndRecordEvent() {
        Order order = createPricedOrder();
        order.pullDomainEvents();
        Instant submittedAt = Instant.parse("2026-07-30T20:20:00Z");

        order.submit(USER_ID, submittedAt, CORRELATION_ID);

        OrderSubmitted event = (OrderSubmitted) order.domainEvents().getFirst();
        assertThat(order.status())
                .as("order status after submission")
                .isEqualTo(OrderStatus.SUBMITTED);
        assertThat(order.submittedBy())
                .as("actor who submitted the order")
                .contains(USER_ID);
        assertThat(order.submittedAt())
                .as("order submission timestamp")
                .contains(submittedAt);
        assertThat(order.updatedAt())
                .as("aggregate update timestamp after submission")
                .isEqualTo(submittedAt);
        assertThat(order.version())
                .as("aggregate version after submission")
                .isEqualTo(3L);
        assertThat(event.itemCount())
                .as("submitted item count")
                .isEqualTo(1);
        assertThat(event.totals().total().amount())
                .as("submitted order total snapshot")
                .isEqualByComparingTo("216.00");
        assertThat(event.aggregateVersion())
                .as("aggregate version in the submission event")
                .isEqualTo(3L);
        assertThat(event.submittedBy())
                .as("submission actor in the event")
                .isEqualTo(USER_ID);
        assertThat(event.occurredAt())
                .as("submission occurrence timestamp")
                .isEqualTo(submittedAt);
    }

    @Test
    void testSubmitShouldRejectEmptyOrder() {
        Order order = createOrderWithoutPendingEvents();

        assertThatThrownBy(() -> order.submit(USER_ID, CHANGED_AT, CORRELATION_ID))
                .as("empty order submission")
                .isInstanceOf(OrderSubmissionNotAllowedException.class)
                .hasMessage("An order must contain at least one item before submission");
    }

    @Test
    void testSubmitShouldRejectOrderWithIncompletePricing() {
        Order order = createOrderWithoutPendingEvents();
        addItem(order, ITEM_ID, PRODUCT_ID);

        assertThatThrownBy(() -> order.submit(USER_ID, CHANGED_AT, CORRELATION_ID))
                .as("submission requires pricing for every item")
                .isInstanceOf(OrderSubmissionNotAllowedException.class)
                .hasMessage("Every order item must be priced before submission");
    }

    @Test
    void testSubmitShouldRejectSecondSubmission() {
        Order order = createPricedOrder();
        order.submit(
                USER_ID,
                Instant.parse("2026-07-30T20:20:00Z"),
                CORRELATION_ID);

        assertThatThrownBy(() -> order.submit(
                USER_ID,
                Instant.parse("2026-07-30T20:25:00Z"),
                CORRELATION_ID))
                .as("order cannot be submitted twice")
                .isInstanceOf(OrderSubmissionNotAllowedException.class)
                .hasMessage("Only a DRAFT order can be submitted");
    }

    @Test
    void testSubmittedOrderShouldRejectItemMutation() {
        Order order = createPricedOrder();
        order.submit(
                USER_ID,
                Instant.parse("2026-07-30T20:20:00Z"),
                CORRELATION_ID);

        assertThatThrownBy(() -> order.updateItemQuantity(
                ITEM_ID,
                quantity("3.000"),
                USER_ID,
                Instant.parse("2026-07-30T20:25:00Z"),
                CORRELATION_ID))
                .as("submitted order items are immutable")
                .isInstanceOf(
                        io.astraforge.supplyplatform.domain.order.exception.OrderNotEditableException.class)
                .hasMessage(
                        "Order items can be changed only while the order is in DRAFT status");
    }


    @Test
    void testStartApprovalShouldTransitionSubmittedOrderAndRecordEvent() {
        Order order = createSubmittedOrder();
        order.pullDomainEvents();
        Instant startedAt = Instant.parse("2026-07-30T20:25:00Z");

        order.startApproval(APPROVER_ID, startedAt, CORRELATION_ID);

        OrderApprovalStarted event =
                (OrderApprovalStarted) order.domainEvents().getFirst();
        assertThat(order.status())
                .as("order status after approval starts")
                .isEqualTo(OrderStatus.PENDING_APPROVAL);
        assertThat(order.version())
                .as("aggregate version after approval starts")
                .isEqualTo(4L);
        assertThat(event.startedBy())
                .as("approval start actor")
                .isEqualTo(APPROVER_ID);
        assertThat(event.aggregateVersion())
                .as("aggregate version in approval-started event")
                .isEqualTo(4L);
    }

    @Test
    void testApproveShouldTransitionPendingOrderAndRecordTotalsSnapshot() {
        Order order = createPendingApprovalOrder();
        order.pullDomainEvents();
        Instant approvedAt = Instant.parse("2026-07-30T20:30:00Z");

        order.approve(APPROVER_ID, approvedAt, CORRELATION_ID);

        OrderApproved event = (OrderApproved) order.domainEvents().getFirst();
        assertThat(order.status())
                .as("order status after approval")
                .isEqualTo(OrderStatus.APPROVED);
        assertThat(order.decisionBy())
                .as("approval decision actor")
                .contains(APPROVER_ID);
        assertThat(order.decisionAt())
                .as("approval decision timestamp")
                .contains(approvedAt);
        assertThat(order.decisionComment())
                .as("approved order has no mandatory decision comment")
                .isEmpty();
        assertThat(event.totals().total().amount())
                .as("approved order total snapshot")
                .isEqualByComparingTo("216.00");
        assertThat(event.aggregateVersion())
                .as("aggregate version in approved event")
                .isEqualTo(5L);
    }

    @Test
    void testRejectShouldRequireCommentAndRecordDecision() {
        Order order = createPendingApprovalOrder();
        order.pullDomainEvents();
        ApprovalComment comment =
                new ApprovalComment("Budget allocation is not available.");
        Instant rejectedAt = Instant.parse("2026-07-30T20:30:00Z");

        order.reject(
                comment,
                APPROVER_ID,
                rejectedAt,
                CORRELATION_ID);

        OrderRejected event = (OrderRejected) order.domainEvents().getFirst();
        assertThat(order.status())
                .as("order status after rejection")
                .isEqualTo(OrderStatus.REJECTED);
        assertThat(order.decisionComment())
                .as("rejection explanation")
                .contains(comment);
        assertThat(event.comment())
                .as("rejection comment in domain event")
                .isEqualTo(comment);
        assertThat(event.rejectedBy())
                .as("rejection actor in domain event")
                .isEqualTo(APPROVER_ID);
    }

    @Test
    void testRequestReviewShouldRecordRequiredChanges() {
        Order order = createPendingApprovalOrder();
        order.pullDomainEvents();
        ApprovalComment comment =
                new ApprovalComment("Confirm the requested quantity.");
        Instant requestedAt = Instant.parse("2026-07-30T20:30:00Z");

        order.requestReview(
                comment,
                APPROVER_ID,
                requestedAt,
                CORRELATION_ID);

        OrderReviewRequested event =
                (OrderReviewRequested) order.domainEvents().getFirst();
        assertThat(order.status())
                .as("order status after review request")
                .isEqualTo(OrderStatus.REVIEW_REQUESTED);
        assertThat(order.decisionBy())
                .as("review request actor")
                .contains(APPROVER_ID);
        assertThat(order.decisionComment())
                .as("requested review explanation")
                .contains(comment);
        assertThat(event.comment())
                .as("review comment in domain event")
                .isEqualTo(comment);
    }

    @Test
    void testApprovalShouldRejectInvalidSourceStatus() {
        Order draftOrder = createPricedOrder();

        assertThatThrownBy(() -> draftOrder.startApproval(
                APPROVER_ID,
                Instant.parse("2026-07-30T20:25:00Z"),
                CORRELATION_ID))
                .as("approval cannot start from draft")
                .isInstanceOf(OrderApprovalNotAllowedException.class)
                .hasMessage("Only a SUBMITTED order can start approval");
    }

    @Test
    void testDecisionShouldRejectOrderOutsidePendingApproval() {
        Order submittedOrder = createSubmittedOrder();

        assertThatThrownBy(() -> submittedOrder.approve(
                APPROVER_ID,
                Instant.parse("2026-07-30T20:30:00Z"),
                CORRELATION_ID))
                .as("approval decision requires pending approval status")
                .isInstanceOf(OrderApprovalNotAllowedException.class)
                .hasMessage(
                        "Only a PENDING_APPROVAL order can receive an approval decision");
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


    private static Order createSubmittedOrder() {
        Order order = createPricedOrder();
        order.submit(
                USER_ID,
                Instant.parse("2026-07-30T20:20:00Z"),
                CORRELATION_ID);
        return order;
    }

    private static Order createPendingApprovalOrder() {
        Order order = createSubmittedOrder();
        order.startApproval(
                APPROVER_ID,
                Instant.parse("2026-07-30T20:25:00Z"),
                CORRELATION_ID);
        return order;
    }

    private static Order createPricedOrder() {
        Order order = createOrderWithoutPendingEvents();
        order.addItem(
                ITEM_ID,
                productSnapshot(PRODUCT_ID, "SKU-001"),
                quantity("2.000"),
                USER_ID,
                CHANGED_AT,
                CORRELATION_ID);
        order.applyItemPricing(
                ITEM_ID,
                itemPricing("100.00", "10.0000", "20.0000"),
                USER_ID,
                CHANGED_AT.plusSeconds(1),
                CORRELATION_ID);
        return order;
    }

    private static ItemPricing itemPricing(
            String unitPrice,
            String discount,
            String tax
    ) {
        return new ItemPricing(
                new Money(new BigDecimal(unitPrice), Currency.getInstance("BRL")),
                percentage(discount),
                percentage(tax));
    }

    private static Percentage percentage(String value) {
        return new Percentage(new BigDecimal(value));
    }
}
