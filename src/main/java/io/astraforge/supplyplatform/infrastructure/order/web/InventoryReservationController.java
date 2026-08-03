package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.RequestInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationResult;
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
public final class InventoryReservationController {

    private final RequestInventoryReservationUseCase requestReservationUseCase;

    public InventoryReservationController(
            RequestInventoryReservationUseCase requestReservationUseCase
    ) {
        this.requestReservationUseCase = Objects.requireNonNull(
                requestReservationUseCase,
                "Request inventory reservation use case must not be null");
    }

    @PostMapping
    public ResponseEntity<RequestInventoryReservationResponse> request(
            @PathVariable UUID orderId,
            @Valid @RequestBody RequestInventoryReservationRequest request
    ) {
        RequestInventoryReservationResult result =
                requestReservationUseCase.requestReservation(
                        InventoryReservationWebMapper.toRequestCommand(
                                orderId,
                                request));

        return ResponseEntity.ok(
                InventoryReservationWebMapper.toResponse(result));
    }
}
