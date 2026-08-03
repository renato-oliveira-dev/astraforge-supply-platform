package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.PrepareOrderForFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class PrepareOrderForFulfillmentService
        implements PrepareOrderForFulfillmentUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public PrepareOrderForFulfillmentService(
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
    public PrepareOrderForFulfillmentResult prepare(
            PrepareOrderForFulfillmentCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Prepare order for fulfillment command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant preparedAt = clock.instant();

        order.prepareForFulfillment(
                new UserId(command.preparedBy()),
                preparedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new PrepareOrderForFulfillmentResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.fulfillmentPreparedBy()
                        .orElseThrow()
                        .value(),
                savedOrder.fulfillmentPreparedAt().orElseThrow(),
                savedOrder.items().size(),
                savedOrder.version());
    }
}
