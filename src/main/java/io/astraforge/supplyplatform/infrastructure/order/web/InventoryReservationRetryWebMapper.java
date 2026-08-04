package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationResult;

import java.util.UUID;

final class InventoryReservationRetryWebMapper {

    private InventoryReservationRetryWebMapper() {
    }

    static RetryInventoryReservationCommand toCommand(
            UUID orderId,
            RetryInventoryReservationRequest request
    ) {
        return new RetryInventoryReservationCommand(
                orderId,
                request.requestedBy(),
                request.correlationId());
    }

    static RetryInventoryReservationResponse toResponse(
            RetryInventoryReservationResult result
    ) {
        return new RetryInventoryReservationResponse(
                result.orderId(),
                result.status(),
                result.requestedBy(),
                result.requestedAt(),
                result.version());
    }
}
