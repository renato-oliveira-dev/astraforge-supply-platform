package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class StartOrderFulfillmentService
        implements StartOrderFulfillmentUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public StartOrderFulfillmentService(
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
    public StartOrderFulfillmentResult start(
            StartOrderFulfillmentCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Start order fulfillment command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant startedAt = clock.instant();

        order.startFulfillment(
                new UserId(command.startedBy()),
                startedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new StartOrderFulfillmentResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.fulfillmentStartedBy()
                        .orElseThrow()
                        .value(),
                savedOrder.fulfillmentStartedAt().orElseThrow(),
                savedOrder.version());
    }
}
