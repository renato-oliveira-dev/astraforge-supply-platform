package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.CancelOrderUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderResult;
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
@RequestMapping("/api/v1/orders/{orderId}/cancellation")
public final class OrderCancellationController {

    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderCancellationController(
            CancelOrderUseCase cancelOrderUseCase
    ) {
        this.cancelOrderUseCase = Objects.requireNonNull(
                cancelOrderUseCase,
                "Cancel order use case must not be null");
    }

    @PostMapping
    public ResponseEntity<CancelOrderResponse> cancel(
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        CancelOrderResult result = cancelOrderUseCase.cancel(
                OrderCancellationWebMapper.toCommand(
                        orderId,
                        request));

        return ResponseEntity.ok(
                OrderCancellationWebMapper.toResponse(result));
    }
}
