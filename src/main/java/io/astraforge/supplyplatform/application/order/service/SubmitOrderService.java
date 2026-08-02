package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.SubmitOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class SubmitOrderService implements SubmitOrderUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public SubmitOrderService(
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
    public SubmitOrderResult submit(SubmitOrderCommand command) {
        Objects.requireNonNull(
                command,
                "Submit order command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant submittedAt = clock.instant();

        order.submit(
                new UserId(command.submittedBy()),
                submittedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");
        OrderTotals totals = savedOrder.totals();

        return new SubmitOrderResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.items().size(),
                totals.total().amount(),
                totals.total().currency(),
                savedOrder.version(),
                savedOrder.submittedAt().orElseThrow());
    }
}
