package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderIdGenerator;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerId;
import io.astraforge.supplyplatform.domain.order.valueobject.CustomerReference;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class CreateOrderService implements CreateOrderUseCase {

    private static final int MAX_ID_GENERATION_ATTEMPTS = 5;

    private final OrderRepository orderRepository;
    private final OrderIdGenerator orderIdGenerator;
    private final Clock clock;

    public CreateOrderService(
            OrderRepository orderRepository,
            OrderIdGenerator orderIdGenerator,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
        this.orderIdGenerator = Objects.requireNonNull(
                orderIdGenerator,
                "Order ID generator must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "Clock must not be null");
    }

    @Override
    public CreateOrderResult create(CreateOrderCommand command) {
        Objects.requireNonNull(command, "Create order command must not be null");

        OrderId orderId = generateAvailableOrderId();
        Instant createdAt = clock.instant();
        Order order = Order.create(
                orderId,
                new CustomerReference(new CustomerId(command.customerId())),
                new UserId(command.createdBy()),
                createdAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new CreateOrderResult(
                savedOrder.id().value(),
                savedOrder.status(),
                savedOrder.version(),
                savedOrder.createdAt());
    }

    private OrderId generateAvailableOrderId() {
        for (int attempt = 0;
                attempt < MAX_ID_GENERATION_ATTEMPTS;
                attempt++) {
            OrderId candidate = Objects.requireNonNull(
                    orderIdGenerator.nextId(),
                    "Order ID generator must not return null");
            if (!orderRepository.existsById(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique order ID after "
                        + MAX_ID_GENERATION_ATTEMPTS
                        + " attempts");
    }
}
