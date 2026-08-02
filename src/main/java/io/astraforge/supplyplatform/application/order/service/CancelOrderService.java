package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.CancelOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CancellationReason;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class CancelOrderService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public CancelOrderService(
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
    public CancelOrderResult cancel(CancelOrderCommand command) {
        Objects.requireNonNull(
                command,
                "Cancel order command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant cancelledAt = clock.instant();

        order.cancel(
                new CancellationReason(command.reason()),
                new UserId(command.cancelledBy()),
                cancelledAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new CancelOrderResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.cancellationReason().orElseThrow().value(),
                savedOrder.cancelledBy().orElseThrow().value(),
                savedOrder.cancelledAt().orElseThrow(),
                savedOrder.version());
    }
}
