package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentResult;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentResult;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentResult;

import java.util.UUID;

final class OrderFulfillmentWebMapper {

    private OrderFulfillmentWebMapper() {
    }

    static PrepareOrderForFulfillmentCommand toPrepareCommand(
            UUID orderId,
            PrepareOrderForFulfillmentRequest request
    ) {
        return new PrepareOrderForFulfillmentCommand(
                orderId,
                request.preparedBy(),
                request.correlationId());
    }

    static StartOrderFulfillmentCommand toStartCommand(
            UUID orderId,
            StartOrderFulfillmentRequest request
    ) {
        return new StartOrderFulfillmentCommand(
                orderId,
                request.startedBy(),
                request.correlationId());
    }

    static CompleteOrderFulfillmentCommand toCompleteCommand(
            UUID orderId,
            CompleteOrderFulfillmentRequest request
    ) {
        return new CompleteOrderFulfillmentCommand(
                orderId,
                request.completedBy(),
                request.correlationId());
    }

    static PrepareOrderForFulfillmentResponse toResponse(
            PrepareOrderForFulfillmentResult result
    ) {
        return new PrepareOrderForFulfillmentResponse(
                result.orderId(),
                result.status(),
                result.preparedBy(),
                result.preparedAt(),
                result.itemCount(),
                result.version());
    }

    static StartOrderFulfillmentResponse toResponse(
            StartOrderFulfillmentResult result
    ) {
        return new StartOrderFulfillmentResponse(
                result.orderId(),
                result.status(),
                result.startedBy(),
                result.startedAt(),
                result.version());
    }

    static CompleteOrderFulfillmentResponse toResponse(
            CompleteOrderFulfillmentResult result
    ) {
        return new CompleteOrderFulfillmentResponse(
                result.orderId(),
                result.status(),
                result.completedBy(),
                result.completedAt(),
                result.itemCount(),
                result.total(),
                result.currency(),
                result.version());
    }
}
