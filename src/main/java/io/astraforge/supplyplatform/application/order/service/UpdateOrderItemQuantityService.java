package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.UpdateOrderItemQuantityUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityCommand;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class UpdateOrderItemQuantityService
        implements UpdateOrderItemQuantityUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public UpdateOrderItemQuantityService(
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
    public UpdateOrderItemQuantityResult updateQuantity(
            UpdateOrderItemQuantityCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Update order item quantity command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderItemId orderItemId =
                new OrderItemId(command.orderItemId());
        Instant changedAt = clock.instant();

        order.updateItemQuantity(
                orderItemId,
                new Quantity(command.quantity()),
                new UserId(command.changedBy()),
                changedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        BigDecimalResult quantityResult = quantityOf(
                savedOrder,
                orderItemId);

        return new UpdateOrderItemQuantityResult(
                savedOrder.id().value(),
                orderItemId.value(),
                quantityResult.value(),
                savedOrder.status(),
                savedOrder.version(),
                savedOrder.updatedAt());
    }

    private static BigDecimalResult quantityOf(
            Order order,
            OrderItemId orderItemId
    ) {
        return order.items().stream()
                .filter(item -> item.id().equals(orderItemId))
                .map(item -> new BigDecimalResult(
                        item.quantity().value()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Updated order item is missing from saved order"));
    }

    private record BigDecimalResult(java.math.BigDecimal value) {
    }
}
