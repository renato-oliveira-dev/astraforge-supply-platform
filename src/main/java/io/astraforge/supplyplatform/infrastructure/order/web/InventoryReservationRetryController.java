package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.RetryInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationResult;
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
public final class InventoryReservationRetryController {

    private final RetryInventoryReservationUseCase retryUseCase;

    public InventoryReservationRetryController(
            RetryInventoryReservationUseCase retryUseCase
    ) {
        this.retryUseCase = Objects.requireNonNull(
                retryUseCase,
                "Retry inventory reservation use case must not be null");
    }

    @PostMapping("/retry")
    public ResponseEntity<RetryInventoryReservationResponse> retry(
            @PathVariable UUID orderId,
            @Valid @RequestBody RetryInventoryReservationRequest request
    ) {
        RetryInventoryReservationResult result = retryUseCase.retry(
                InventoryReservationRetryWebMapper.toCommand(
                        orderId,
                        request));

        return ResponseEntity.ok(
                InventoryReservationRetryWebMapper.toResponse(result));
    }
}
