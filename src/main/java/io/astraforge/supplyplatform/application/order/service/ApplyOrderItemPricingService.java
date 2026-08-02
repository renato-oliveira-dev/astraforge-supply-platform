package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.ApplyOrderItemPricingUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.entity.OrderItem;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.ItemPricing;
import io.astraforge.supplyplatform.domain.order.valueobject.Money;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.Percentage;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ApplyOrderItemPricingService
        implements ApplyOrderItemPricingUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public ApplyOrderItemPricingService(
            OrderRepository orderRepository,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "Clock must not be null");
    }

    @Override
    public ApplyOrderItemPricingResult applyPricing(
            ApplyOrderItemPricingCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Apply order item pricing command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderItemId orderItemId =
                new OrderItemId(command.orderItemId());
        ItemPricing pricing = pricing(command);
        Instant pricedAt = clock.instant();

        order.applyItemPricing(
                orderItemId,
                pricing,
                new UserId(command.pricedBy()),
                pricedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");
        OrderItem pricedItem = findItem(savedOrder, orderItemId);

        return new ApplyOrderItemPricingResult(
                savedOrder.id().value(),
                orderItemId.value(),
                pricing.total(pricedItem.quantity()).amount(),
                pricing.unitPrice().currency(),
                savedOrder.pricingComplete(),
                savedOrder.status(),
                savedOrder.version(),
                savedOrder.updatedAt());
    }

    private static ItemPricing pricing(
            ApplyOrderItemPricingCommand command
    ) {
        return new ItemPricing(
                new Money(command.unitPrice(), command.currency()),
                new Percentage(command.discountPercentage()),
                new Percentage(command.taxPercentage()));
    }

    private static OrderItem findItem(
            Order order,
            OrderItemId orderItemId
    ) {
        return order.items().stream()
                .filter(item -> item.id().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Priced order item is missing from saved order"));
    }
}
