package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;

final class OrderWebMapper {

    private OrderWebMapper() {
    }

    static CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerId(),
                request.createdBy(),
                request.correlationId());
    }

    static CreateOrderResponse toResponse(CreateOrderResult result) {
        return new CreateOrderResponse(
                result.orderId(),
                result.status(),
                result.version(),
                result.createdAt());
    }
}
