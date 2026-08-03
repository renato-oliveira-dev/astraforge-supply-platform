package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ApplyOrderItemPricingUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RemoveOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.SubmitOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.UpdateOrderItemQuantityUseCase;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingResult;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderResult;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final UpdateOrderItemQuantityUseCase updateQuantityUseCase;
    private final RemoveOrderItemUseCase removeOrderItemUseCase;
    private final ApplyOrderItemPricingUseCase applyPricingUseCase;
    private final SubmitOrderUseCase submitOrderUseCase;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            AddOrderItemUseCase addOrderItemUseCase,
            UpdateOrderItemQuantityUseCase updateQuantityUseCase,
            RemoveOrderItemUseCase removeOrderItemUseCase,
            ApplyOrderItemPricingUseCase applyPricingUseCase,
            SubmitOrderUseCase submitOrderUseCase
    ) {
        this.createOrderUseCase = Objects.requireNonNull(
                createOrderUseCase,
                "Create order use case must not be null");
        this.addOrderItemUseCase = Objects.requireNonNull(
                addOrderItemUseCase,
                "Add order item use case must not be null");
        this.updateQuantityUseCase = Objects.requireNonNull(
                updateQuantityUseCase,
                "Update order item quantity use case must not be null");
        this.removeOrderItemUseCase = Objects.requireNonNull(
                removeOrderItemUseCase,
                "Remove order item use case must not be null");
        this.applyPricingUseCase = Objects.requireNonNull(
                applyPricingUseCase,
                "Apply order item pricing use case must not be null");
        this.submitOrderUseCase = Objects.requireNonNull(
                submitOrderUseCase,
                "Submit order use case must not be null");
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        CreateOrderResult result = createOrderUseCase.create(
                OrderWebMapper.toCommand(request));
        URI location = uriBuilder
                .path("/api/v1/orders/{orderId}")
                .buildAndExpand(result.orderId())
                .toUri();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(OrderWebMapper.toResponse(result));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<AddOrderItemResponse> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddOrderItemRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        AddOrderItemResult result = addOrderItemUseCase.addItem(
                OrderWebMapper.toCommand(orderId, request));
        URI location = uriBuilder
                .path("/api/v1/orders/{orderId}/items/{orderItemId}")
                .buildAndExpand(
                        result.orderId(),
                        result.orderItemId())
                .toUri();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(location)
                .body(OrderWebMapper.toResponse(result));
    }

    @PatchMapping("/{orderId}/items/{orderItemId}/quantity")
    public ResponseEntity<UpdateOrderItemQuantityResponse> updateQuantity(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId,
            @Valid @RequestBody UpdateOrderItemQuantityRequest request
    ) {
        UpdateOrderItemQuantityResult result =
                updateQuantityUseCase.updateQuantity(
                        OrderWebMapper.toCommand(
                                orderId,
                                orderItemId,
                                request));

        return ResponseEntity.ok(OrderWebMapper.toResponse(result));
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<RemoveOrderItemResponse> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId,
            @Valid @RequestBody RemoveOrderItemRequest request
    ) {
        RemoveOrderItemResult result =
                removeOrderItemUseCase.removeItem(
                        OrderWebMapper.toCommand(
                                orderId,
                                orderItemId,
                                request));

        return ResponseEntity.ok(OrderWebMapper.toResponse(result));
    }

    @PatchMapping("/{orderId}/items/{orderItemId}/pricing")
    public ResponseEntity<ApplyOrderItemPricingResponse> applyPricing(
            @PathVariable UUID orderId,
            @PathVariable UUID orderItemId,
            @Valid @RequestBody ApplyOrderItemPricingRequest request
    ) {
        ApplyOrderItemPricingResult result =
                applyPricingUseCase.applyPricing(
                        OrderWebMapper.toCommand(
                                orderId,
                                orderItemId,
                                request));

        return ResponseEntity.ok(OrderWebMapper.toResponse(result));
    }
    @PostMapping("/{orderId}/submission")
    public ResponseEntity<SubmitOrderResponse> submit(
            @PathVariable UUID orderId,
            @Valid @RequestBody SubmitOrderRequest request
    ) {
        SubmitOrderResult result = submitOrderUseCase.submit(
                OrderWebMapper.toCommand(orderId, request));

        return ResponseEntity.ok(OrderWebMapper.toResponse(result));
    }
}
