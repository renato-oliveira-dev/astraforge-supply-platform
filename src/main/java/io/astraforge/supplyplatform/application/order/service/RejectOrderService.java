package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.RejectOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RejectOrderCommand;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.ApprovalComment;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class RejectOrderService implements RejectOrderUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public RejectOrderService(
            OrderRepository orderRepository,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public ApprovalDecisionResult reject(RejectOrderCommand command) {
        Objects.requireNonNull(
                command,
                "Reject order command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant rejectedAt = clock.instant();

        order.reject(
                new ApprovalComment(command.comment()),
                new UserId(command.rejectedBy()),
                rejectedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return result(savedOrder);
    }

    private static ApprovalDecisionResult result(Order order) {
        return new ApprovalDecisionResult(
                order.id().value(),
                order.status(),
                order.decisionBy().orElseThrow().value(),
                order.decisionAt().orElseThrow(),
                Optional.of(
                        order.decisionComment().orElseThrow().value()),
                order.version());
    }
}
