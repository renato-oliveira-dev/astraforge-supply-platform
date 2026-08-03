package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionCommand;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionResult;

import java.util.UUID;

final class OrderRevisionWebMapper {

    private OrderRevisionWebMapper() {
    }

    static ReopenOrderForRevisionCommand toCommand(
            UUID orderId,
            ReopenOrderForRevisionRequest request
    ) {
        return new ReopenOrderForRevisionCommand(
                orderId,
                request.reopenedBy(),
                request.correlationId());
    }

    static ReopenOrderForRevisionResponse toResponse(
            ReopenOrderForRevisionResult result
    ) {
        return new ReopenOrderForRevisionResponse(
                result.orderId(),
                result.status(),
                result.version(),
                result.reopenedAt());
    }
}
