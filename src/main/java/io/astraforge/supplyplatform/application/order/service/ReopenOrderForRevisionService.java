package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.ReopenOrderForRevisionUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionCommand;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ReopenOrderForRevisionService
        implements ReopenOrderForRevisionUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public ReopenOrderForRevisionService(
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
    public ReopenOrderForRevisionResult reopen(
            ReopenOrderForRevisionCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Reopen order for revision command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant reopenedAt = clock.instant();

        order.reopenForRevision(
                new UserId(command.reopenedBy()),
                reopenedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new ReopenOrderForRevisionResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.version(),
                savedOrder.updatedAt());
    }
}
