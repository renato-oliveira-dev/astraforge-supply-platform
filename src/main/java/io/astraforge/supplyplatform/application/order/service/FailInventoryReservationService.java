package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.FailInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.FailInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.InventoryFailureReason;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class FailInventoryReservationService
        implements FailInventoryReservationUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public FailInventoryReservationService(
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
    public InventoryReservationOutcomeResult fail(
            FailInventoryReservationCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Fail inventory reservation command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant recordedAt = clock.instant();

        order.failInventoryReservation(
                new InventoryFailureReason(command.reason()),
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
                Optional.of(
                        savedOrder.inventoryFailureReason()
                                .orElseThrow()
                                .value()),
                savedOrder.version());
    }
}
