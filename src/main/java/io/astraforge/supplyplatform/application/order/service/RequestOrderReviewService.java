package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.RequestOrderReviewUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RequestOrderReviewCommand;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.ApprovalComment;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class RequestOrderReviewService
        implements RequestOrderReviewUseCase {

    private final OrderRepository orderRepository;
    private final Clock clock;

    public RequestOrderReviewService(
            OrderRepository orderRepository,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public ApprovalDecisionResult requestReview(
            RequestOrderReviewCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Request order review command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        Instant requestedAt = clock.instant();

        order.requestReview(
                new ApprovalComment(command.comment()),
                new UserId(command.requestedBy()),
                requestedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new ApprovalDecisionResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.decisionBy().orElseThrow().value(),
                savedOrder.decisionAt().orElseThrow(),
                Optional.of(
                        savedOrder.decisionComment()
                                .orElseThrow()
                                .value()),
                savedOrder.version());
    }
}
