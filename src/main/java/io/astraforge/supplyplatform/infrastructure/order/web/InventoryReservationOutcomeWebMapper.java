package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.ConfirmInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.FailInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;

import java.util.UUID;

final class InventoryReservationOutcomeWebMapper {

    private InventoryReservationOutcomeWebMapper() {
    }

    static ConfirmInventoryReservationCommand toConfirmCommand(
            UUID orderId,
            ConfirmInventoryReservationRequest request
    ) {
        return new ConfirmInventoryReservationCommand(
                orderId,
                request.recordedBy(),
                request.correlationId());
    }

    static FailInventoryReservationCommand toFailCommand(
            UUID orderId,
            FailInventoryReservationRequest request
    ) {
        return new FailInventoryReservationCommand(
                orderId,
                request.reason(),
                request.recordedBy(),
                request.correlationId());
    }

    static InventoryReservationOutcomeResponse toResponse(
            InventoryReservationOutcomeResult result
    ) {
        return new InventoryReservationOutcomeResponse(
                result.orderId(),
                result.status(),
                result.recordedBy(),
                result.recordedAt(),
                result.failureReason(),
                result.version());
    }
}
