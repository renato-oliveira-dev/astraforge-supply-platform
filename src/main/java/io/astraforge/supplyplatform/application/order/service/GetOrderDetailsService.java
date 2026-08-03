package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.GetOrderDetailsUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.GetOrderDetailsQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderItemDetailsResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.entity.OrderItem;
import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GetOrderDetailsService
        implements GetOrderDetailsUseCase {

    private final OrderRepository orderRepository;

    public GetOrderDetailsService(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
    }

    @Override
    public OrderDetailsResult getDetails(GetOrderDetailsQuery query) {
        Objects.requireNonNull(
                query,
                "Get order details query must not be null");

        OrderId orderId = new OrderId(query.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        List<OrderItemDetailsResult> items = order.items()
                .stream()
                .map(GetOrderDetailsService::itemDetails)
                .toList();

        if (!order.pricingComplete()) {
            return resultWithoutTotals(order, items);
        }

        return resultWithTotals(order, items, order.totals());
    }

    private static OrderDetailsResult resultWithoutTotals(
            Order order,
            List<OrderItemDetailsResult> items
    ) {
        return new OrderDetailsResult(
                order.id().value(),
                order.customerReference().customerId().value(),
                order.status(),
                order.createdBy().value(),
                order.createdAt(),
                order.updatedAt(),
                order.version(),
                items,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    private static OrderDetailsResult resultWithTotals(
            Order order,
            List<OrderItemDetailsResult> items,
            OrderTotals totals
    ) {
        return new OrderDetailsResult(
                order.id().value(),
                order.customerReference().customerId().value(),
                order.status(),
                order.createdBy().value(),
                order.createdAt(),
                order.updatedAt(),
                order.version(),
                items,
                true,
                Optional.of(totals.subtotal().amount()),
                Optional.of(totals.discount().amount()),
                Optional.of(totals.tax().amount()),
                Optional.of(totals.total().amount()),
                Optional.of(totals.total().currency()));
    }

    private static OrderItemDetailsResult itemDetails(OrderItem item) {
        return item.pricing()
                .map(pricing -> pricedItemDetails(item, pricing))
                .orElseGet(() -> unpricedItemDetails(item));
    }

    private static OrderItemDetailsResult unpricedItemDetails(
            OrderItem item
    ) {
        return new OrderItemDetailsResult(
                item.id().value(),
                item.productId().value(),
                item.productSnapshot().sku(),
                item.productSnapshot().name(),
                item.productSnapshot().unitOfMeasure(),
                item.quantity().value(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    private static OrderItemDetailsResult pricedItemDetails(
            OrderItem item,
            ItemPricing pricing
    ) {
        BigDecimal itemTotal = pricing.total(item.quantity()).amount();
        Currency currency = pricing.unitPrice().currency();
        return new OrderItemDetailsResult(
                item.id().value(),
                item.productId().value(),
                item.productSnapshot().sku(),
                item.productSnapshot().name(),
                item.productSnapshot().unitOfMeasure(),
                item.quantity().value(),
                Optional.of(pricing.unitPrice().amount()),
                Optional.of(pricing.discountPercentage().value()),
                Optional.of(pricing.taxPercentage().value()),
                Optional.of(itemTotal),
                Optional.of(currency));
    }
}
