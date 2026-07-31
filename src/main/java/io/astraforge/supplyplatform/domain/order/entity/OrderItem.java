package io.astraforge.supplyplatform.domain.order.entity;

import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;

import java.util.Objects;

public final class OrderItem {

    private final OrderItemId id;
    private final ProductSnapshot productSnapshot;
    private Quantity quantity;

    private OrderItem(OrderItemId id, ProductSnapshot productSnapshot, Quantity quantity) {
        this.id = Objects.requireNonNull(id, "Order item ID must not be null");
        this.productSnapshot = Objects.requireNonNull(
                productSnapshot,
                "Product snapshot must not be null");
        this.quantity = Objects.requireNonNull(quantity, "Quantity must not be null");
    }

    public static OrderItem create(
            OrderItemId id,
            ProductSnapshot productSnapshot,
            Quantity quantity
    ) {
        return new OrderItem(id, productSnapshot, quantity);
    }

    public OrderItemId id() {
        return id;
    }

    public ProductSnapshot productSnapshot() {
        return productSnapshot;
    }

    public ProductId productId() {
        return productSnapshot.productId();
    }

    public Quantity quantity() {
        return quantity;
    }

    public Quantity changeQuantity(Quantity newQuantity) {
        Quantity previousQuantity = quantity;
        quantity = Objects.requireNonNull(newQuantity, "New quantity must not be null");
        return previousQuantity;
    }
}
