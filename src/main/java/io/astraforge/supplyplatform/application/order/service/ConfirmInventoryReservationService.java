package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.ConfirmInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ConfirmInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class ConfirmInventoryReservationService
        implements ConfirmInventoryReservationUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public ConfirmInventoryReservationService(
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
    public InventoryReservationOutcomeResult confirm(
            ConfirmInventoryReservationCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Confirm inventory reservation command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant recordedAt = clock.instant();

        order.confirmInventoryReservation(
                new UserId(command.recordedBy()),
                recordedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new InventoryReservationOutcomeResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.inventoryResultRecordedBy()
                        .orElseThrow()
                        .value(),
                savedOrder.inventoryResultRecordedAt().orElseThrow(),
                Optional.empty(),
                savedOrder.version());
    }
}
