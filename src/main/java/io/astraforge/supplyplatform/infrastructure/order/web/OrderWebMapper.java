package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;

import java.util.UUID;

final class OrderWebMapper {

    private OrderWebMapper() {
    }

    static CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerId(),
                request.createdBy(),
                request.correlationId());
    }

    static AddOrderItemCommand toCommand(
            UUID orderId,
            AddOrderItemRequest request
    ) {
        return new AddOrderItemCommand(
                orderId,
                request.productId(),
                request.sku(),
                request.productName(),
                request.unitOfMeasure(),
                request.quantity(),
                request.addedBy(),
                request.correlationId());
    }

    static CreateOrderResponse toResponse(CreateOrderResult result) {
        return new CreateOrderResponse(
                result.orderId(),
                result.status(),
                result.version(),
                result.createdAt());
    }

    static AddOrderItemResponse toResponse(AddOrderItemResult result) {
        return new AddOrderItemResponse(
                result.orderId(),
                result.orderItemId(),
                result.productId(),
                result.status(),
                result.itemCount(),
                result.version(),
                result.updatedAt());
    }
}
