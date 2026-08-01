package io.astraforge.supplyplatform.domain.order.entity;

import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.Money;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.Percentage;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductReference;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemTest {

    private static final OrderItemId ITEM_ID =
            new OrderItemId(UUID.fromString("50000000-0000-0000-0000-000000000005"));
    private static final ProductId PRODUCT_ID =
            new ProductId(UUID.fromString("40000000-0000-0000-0000-000000000004"));

    @Test
    void testCreateShouldPreserveIdentitySnapshotAndQuantity() {
        ProductSnapshot snapshot = productSnapshot();
        Quantity quantity = quantity("2.000");

        OrderItem item = OrderItem.create(ITEM_ID, snapshot, quantity);

        assertThat(item.id())
                .as("order item identifier")
                .isEqualTo(ITEM_ID);
        assertThat(item.productSnapshot())
                .as("immutable product snapshot")
                .isEqualTo(snapshot);
        assertThat(item.productId())
                .as("order item product identifier")
                .isEqualTo(PRODUCT_ID);
        assertThat(item.quantity())
                .as("initial order item quantity")
                .isEqualTo(quantity);
    }

    @Test
    void testChangeQuantityShouldReturnPreviousQuantity() {
        OrderItem item = OrderItem.create(ITEM_ID, productSnapshot(), quantity("2.000"));
        Quantity newQuantity = quantity("5.000");

        Quantity previousQuantity = item.changeQuantity(newQuantity);

        assertThat(previousQuantity)
                .as("quantity before the change")
                .isEqualTo(quantity("2.000"));
        assertThat(item.quantity())
                .as("quantity after the change")
                .isEqualTo(newQuantity);
    }


    @Test
    void testApplyPricingShouldExposePricing() {
        OrderItem item = OrderItem.create(ITEM_ID, productSnapshot(), quantity("2.000"));
        ItemPricing pricing = pricing();

        item.applyPricing(pricing);

        assertThat(item.pricing())
                .as("pricing assigned to the order item")
                .contains(pricing);
    }

    @Test
    void testInvalidatePricingShouldClearExistingPricing() {
        OrderItem item = OrderItem.create(ITEM_ID, productSnapshot(), quantity("2.000"));
        item.applyPricing(pricing());

        boolean invalidated = item.invalidatePricing();

        assertThat(invalidated)
                .as("existing pricing invalidation result")
                .isTrue();
        assertThat(item.pricing())
                .as("pricing removed from the order item")
                .isEmpty();
        assertThat(item.invalidatePricing())
                .as("repeated pricing invalidation result")
                .isFalse();
    }

    private static ProductSnapshot productSnapshot() {
        return new ProductSnapshot(
                new ProductReference(PRODUCT_ID),
                "SKU-001",
                "Industrial Pump",
                "UNIT");
    }

    private static Quantity quantity(String value) {
        return new Quantity(new BigDecimal(value));
    }

    private static ItemPricing pricing() {
        return new ItemPricing(
                new Money(new BigDecimal("10.00"), Currency.getInstance("BRL")),
                new Percentage(new BigDecimal("0.0000")),
                new Percentage(new BigDecimal("0.0000")));
    }
}
