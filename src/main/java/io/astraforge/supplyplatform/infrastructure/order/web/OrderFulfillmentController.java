package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.CompleteOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.PrepareOrderForFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentResult;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentResult;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentResult;
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
@RequestMapping("/api/v1/orders/{orderId}/fulfillment")
public final class OrderFulfillmentController {

    private final PrepareOrderForFulfillmentUseCase prepareUseCase;
    private final StartOrderFulfillmentUseCase startUseCase;
    private final CompleteOrderFulfillmentUseCase completeUseCase;

    public OrderFulfillmentController(
            PrepareOrderForFulfillmentUseCase prepareUseCase,
            StartOrderFulfillmentUseCase startUseCase,
            CompleteOrderFulfillmentUseCase completeUseCase
    ) {
        this.prepareUseCase = Objects.requireNonNull(
                prepareUseCase,
                "Prepare order for fulfillment use case must not be null");
        this.startUseCase = Objects.requireNonNull(
                startUseCase,
                "Start order fulfillment use case must not be null");
        this.completeUseCase = Objects.requireNonNull(
                completeUseCase,
                "Complete order fulfillment use case must not be null");
    }

    @PostMapping("/preparation")
    public ResponseEntity<PrepareOrderForFulfillmentResponse> prepare(
            @PathVariable UUID orderId,
            @Valid @RequestBody PrepareOrderForFulfillmentRequest request
    ) {
        PrepareOrderForFulfillmentResult result = prepareUseCase.prepare(
                OrderFulfillmentWebMapper.toPrepareCommand(orderId, request));

        return ResponseEntity.ok(
                OrderFulfillmentWebMapper.toResponse(result));
    }

    @PostMapping("/start")
    public ResponseEntity<StartOrderFulfillmentResponse> start(
            @PathVariable UUID orderId,
            @Valid @RequestBody StartOrderFulfillmentRequest request
    ) {
        StartOrderFulfillmentResult result = startUseCase.start(
                OrderFulfillmentWebMapper.toStartCommand(orderId, request));

        return ResponseEntity.ok(
                OrderFulfillmentWebMapper.toResponse(result));
    }

    @PostMapping("/completion")
    public ResponseEntity<CompleteOrderFulfillmentResponse> complete(
            @PathVariable UUID orderId,
            @Valid @RequestBody CompleteOrderFulfillmentRequest request
    ) {
        CompleteOrderFulfillmentResult result = completeUseCase.complete(
                OrderFulfillmentWebMapper.toCompleteCommand(orderId, request));

        return ResponseEntity.ok(
                OrderFulfillmentWebMapper.toResponse(result));
    }
}
