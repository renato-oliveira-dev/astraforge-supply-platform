package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderProcessingUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class StartOrderProcessingService
        implements StartOrderProcessingUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public StartOrderProcessingService(
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
    public StartOrderProcessingResult startProcessing(
            StartOrderProcessingCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Start order processing command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant startedAt = clock.instant();

        order.startProcessing(
                new UserId(command.startedBy()),
                startedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new StartOrderProcessingResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.processingStartedBy()
                        .orElseThrow()
                        .value(),
                savedOrder.processingStartedAt().orElseThrow(),
                savedOrder.version());
    }
}
