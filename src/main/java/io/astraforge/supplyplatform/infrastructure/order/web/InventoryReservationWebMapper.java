package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationResult;

import java.util.UUID;

final class InventoryReservationWebMapper {

    private InventoryReservationWebMapper() {
    }

    static RequestInventoryReservationCommand toRequestCommand(
            UUID orderId,
            RequestInventoryReservationRequest request
    ) {
        return new RequestInventoryReservationCommand(
                orderId,
                request.requestedBy(),
                request.correlationId());
    }

    static RequestInventoryReservationResponse toResponse(
            RequestInventoryReservationResult result
    ) {
        return new RequestInventoryReservationResponse(
                result.orderId(),
                result.status(),
                result.requestedBy(),
                result.requestedAt(),
                result.itemCount(),
                result.version());
    }
}
