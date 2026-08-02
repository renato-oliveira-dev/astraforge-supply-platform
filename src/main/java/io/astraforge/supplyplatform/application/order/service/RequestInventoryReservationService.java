package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.RequestInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class RequestInventoryReservationService
        implements RequestInventoryReservationUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public RequestInventoryReservationService(
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
    public RequestInventoryReservationResult requestReservation(
            RequestInventoryReservationCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Request inventory reservation command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant requestedAt = clock.instant();

        order.requestInventoryReservation(
                new UserId(command.requestedBy()),
                requestedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new RequestInventoryReservationResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.inventoryRequestedBy()
                        .orElseThrow()
                        .value(),
                savedOrder.inventoryRequestedAt().orElseThrow(),
                savedOrder.items().size(),
                savedOrder.version());
    }
}
