package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.RemoveOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class RemoveOrderItemService
        implements RemoveOrderItemUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public RemoveOrderItemService(
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
    public RemoveOrderItemResult removeItem(
            RemoveOrderItemCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Remove order item command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderItemId orderItemId =
                new OrderItemId(command.orderItemId());
        Instant removedAt = clock.instant();

        order.removeItem(
                orderItemId,
                new UserId(command.removedBy()),
                removedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new RemoveOrderItemResult(
                savedOrder.id().value(),
                orderItemId.value(),
                savedOrder.status(),
                savedOrder.items().size(),
                savedOrder.version(),
                savedOrder.updatedAt());
    }
}
