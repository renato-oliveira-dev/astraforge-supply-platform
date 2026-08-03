package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.CancelOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderResult;

import java.util.UUID;

final class OrderCancellationWebMapper {

    private OrderCancellationWebMapper() {
    }

    static CancelOrderCommand toCommand(
            UUID orderId,
            CancelOrderRequest request
    ) {
        return new CancelOrderCommand(
                orderId,
                request.reason(),
                request.cancelledBy(),
                request.correlationId());
    }

    static CancelOrderResponse toResponse(CancelOrderResult result) {
        return new CancelOrderResponse(
                result.orderId(),
                result.status(),
                result.reason(),
                result.cancelledBy(),
                result.cancelledAt(),
                result.version());
    }
}
