package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.CompleteOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class CompleteOrderFulfillmentService
        implements CompleteOrderFulfillmentUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public CompleteOrderFulfillmentService(
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
    public CompleteOrderFulfillmentResult complete(
            CompleteOrderFulfillmentCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Complete order fulfillment command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant completedAt = clock.instant();

        order.completeFulfillment(
                new UserId(command.completedBy()),
                completedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");
        OrderTotals totals = savedOrder.totals();

        return new CompleteOrderFulfillmentResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.completedBy().orElseThrow().value(),
                savedOrder.completedAt().orElseThrow(),
                savedOrder.items().size(),
                totals.total().amount(),
                totals.total().currency(),
                savedOrder.version());
    }
}
