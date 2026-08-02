package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.ApproveOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ApproveOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class ApproveOrderService implements ApproveOrderUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public ApproveOrderService(
            OrderRepository orderRepository,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public ApprovalDecisionResult approve(ApproveOrderCommand command) {
        Objects.requireNonNull(
                command,
                "Approve order command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant approvedAt = clock.instant();

        order.approve(
                new UserId(command.approvedBy()),
                approvedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new ApprovalDecisionResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.decisionBy().orElseThrow().value(),
                savedOrder.decisionAt().orElseThrow(),
                Optional.empty(),
                savedOrder.version());
    }
}
