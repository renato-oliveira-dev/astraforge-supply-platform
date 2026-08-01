package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderItemIdGenerator;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.CorrelationId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductId;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductReference;
import io.astraforge.supplyplatform.domain.order.valueobject.ProductSnapshot;
import io.astraforge.supplyplatform.domain.order.valueobject.Quantity;
import io.astraforge.supplyplatform.domain.order.valueobject.UserId;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class AddOrderItemService implements AddOrderItemUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemIdGenerator orderItemIdGenerator;
    private final Clock clock;

    public AddOrderItemService(
            OrderRepository orderRepository,
            OrderItemIdGenerator orderItemIdGenerator,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(
                orderRepository,
                "Order repository must not be null");
        this.orderItemIdGenerator = Objects.requireNonNull(
                orderItemIdGenerator,
                "Order item ID generator must not be null");
        this.clock = Objects.requireNonNull(
                clock,
                "Clock must not be null");
    }

    @Override
    public AddOrderItemResult addItem(AddOrderItemCommand command) {
        Objects.requireNonNull(command, "Add order item command must not be null");

        OrderId orderId = new OrderId(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderItemId orderItemId = Objects.requireNonNull(
                orderItemIdGenerator.nextId(),
                "Order item ID generator must not return null");
        Instant addedAt = clock.instant();

        order.addItem(
                orderItemId,
                productSnapshot(command),
                new Quantity(command.quantity()),
                new UserId(command.addedBy()),
                addedAt,
                new CorrelationId(command.correlationId()));

        Order savedOrder = Objects.requireNonNull(
                orderRepository.save(order),
                "Order repository must return the saved order");

        return new AddOrderItemResult(
                savedOrder.id().value(),
                orderItemId.value(),
                command.productId(),
                savedOrder.status(),
                savedOrder.items().size(),
                savedOrder.version(),
                savedOrder.updatedAt());
    }

    private static ProductSnapshot productSnapshot(
            AddOrderItemCommand command
    ) {
        return new ProductSnapshot(
                new ProductReference(
                        new ProductId(command.productId())),
                command.sku(),
                command.productName(),
                command.unitOfMeasure());
    }
}
