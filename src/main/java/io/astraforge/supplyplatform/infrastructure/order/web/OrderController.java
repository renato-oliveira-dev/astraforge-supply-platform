package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/orders")
public final class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = Objects.requireNonNull(
                createOrderUseCase,
                "Create order use case must not be null");
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        CreateOrderResult result = createOrderUseCase.create(
                OrderWebMapper.toCommand(request));
        CreateOrderResponse response = OrderWebMapper.toResponse(result);
        URI location = uriBuilder
                .path("/api/v1/orders/{orderId}")
                .buildAndExpand(result.orderId())
                .toUri();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }
}
