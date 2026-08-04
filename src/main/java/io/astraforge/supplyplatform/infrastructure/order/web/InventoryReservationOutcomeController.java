package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.ConfirmInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.in.FailInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/inventory-reservations")
public final class InventoryReservationOutcomeController {

    private final ConfirmInventoryReservationUseCase confirmUseCase;
    private final FailInventoryReservationUseCase failUseCase;

    public InventoryReservationOutcomeController(
            ConfirmInventoryReservationUseCase confirmUseCase,
            FailInventoryReservationUseCase failUseCase
    ) {
        this.confirmUseCase = Objects.requireNonNull(
                confirmUseCase,
                "Confirm inventory reservation use case must not be null");
        this.failUseCase = Objects.requireNonNull(
                failUseCase,
                "Fail inventory reservation use case must not be null");
    }

    @PostMapping("/confirmation")
    public ResponseEntity<InventoryReservationOutcomeResponse> confirm(
            @PathVariable UUID orderId,
            @Valid @RequestBody ConfirmInventoryReservationRequest request
    ) {
        InventoryReservationOutcomeResult result = confirmUseCase.confirm(
                InventoryReservationOutcomeWebMapper.toConfirmCommand(
                        orderId,
                        request));

        return ResponseEntity.ok(
                InventoryReservationOutcomeWebMapper.toResponse(result));
    }

    @PostMapping("/failure")
    public ResponseEntity<InventoryReservationOutcomeResponse> fail(
            @PathVariable UUID orderId,
            @Valid @RequestBody FailInventoryReservationRequest request
    ) {
        InventoryReservationOutcomeResult result = failUseCase.fail(
                InventoryReservationOutcomeWebMapper.toFailCommand(
                        orderId,
                        request));

        return ResponseEntity.ok(
                InventoryReservationOutcomeWebMapper.toResponse(result));
    }
}
