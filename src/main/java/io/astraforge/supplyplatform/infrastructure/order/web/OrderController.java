package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public final class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final AddOrderItemUseCase addOrderItemUseCase;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            AddOrderItemUseCase addOrderItemUseCase
    ) {
        this.createOrderUseCase = Objects.requireNonNull(
                createOrderUseCase,
                "Create order use case must not be null");
        this.addOrderItemUseCase = Objects.requireNonNull(
                addOrderItemUseCase,
                "Add order item use case must not be null");
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        CreateOrderResult result = createOrderUseCase.create(
                OrderWebMapper.toCommand(request));
        CreateOrderResponse response =
                OrderWebMapper.toResponse(result);
        URI location = uriBuilder
                .path("/api/v1/orders/{orderId}")
                .buildAndExpand(result.orderId())
                .toUri();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<AddOrderItemResponse> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddOrderItemRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        AddOrderItemResult result = addOrderItemUseCase.addItem(
                OrderWebMapper.toCommand(orderId, request));
        AddOrderItemResponse response =
                OrderWebMapper.toResponse(result);
        URI location = uriBuilder
                .path("/api/v1/orders/{orderId}/items/{orderItemId}")
                .buildAndExpand(
                        result.orderId(),
                        result.orderItemId())
                .toUri();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(response);
    }
}
