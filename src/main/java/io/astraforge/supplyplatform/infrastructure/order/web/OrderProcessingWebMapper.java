package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingResult;

import java.util.UUID;

final class OrderProcessingWebMapper {

    private OrderProcessingWebMapper() {
    }

    static StartOrderProcessingCommand toCommand(
            UUID orderId,
            StartOrderProcessingRequest request
    ) {
        return new StartOrderProcessingCommand(
                orderId,
                request.startedBy(),
                request.correlationId());
    }

    static StartOrderProcessingResponse toResponse(
            StartOrderProcessingResult result
    ) {
        return new StartOrderProcessingResponse(
                result.orderId(),
                result.status(),
                result.startedBy(),
                result.startedAt(),
                result.version());
    }
}
