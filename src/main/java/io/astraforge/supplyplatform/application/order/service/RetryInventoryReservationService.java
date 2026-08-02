package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.RetryInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class RetryInventoryReservationService
        implements RetryInventoryReservationUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public RetryInventoryReservationService(
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
    public RetryInventoryReservationResult retry(
            RetryInventoryReservationCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Retry inventory reservation command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant requestedAt = clock.instant();

        order.retryInventoryReservation(
                new UserId(command.requestedBy()),
                requestedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new RetryInventoryReservationResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.inventoryRequestedBy()
                        .orElseThrow()
                        .value(),
                savedOrder.inventoryRequestedAt().orElseThrow(),
                savedOrder.version());
    }
}
