package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.StartOrderProcessingUseCase;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingResult;
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
@RequestMapping("/api/v1/orders/{orderId}/processing")
public final class OrderProcessingController {

    private final StartOrderProcessingUseCase startOrderProcessingUseCase;

    public OrderProcessingController(
            StartOrderProcessingUseCase startOrderProcessingUseCase
    ) {
        this.startOrderProcessingUseCase = Objects.requireNonNull(
                startOrderProcessingUseCase,
                "Start order processing use case must not be null");
    }

    @PostMapping("/start")
    public ResponseEntity<StartOrderProcessingResponse> start(
            @PathVariable UUID orderId,
            @Valid @RequestBody StartOrderProcessingRequest request
    ) {
        StartOrderProcessingResult result =
                startOrderProcessingUseCase.startProcessing(
                        OrderProcessingWebMapper.toCommand(
                                orderId,
                                request));

        return ResponseEntity.ok(
                OrderProcessingWebMapper.toResponse(result));
    }
}
