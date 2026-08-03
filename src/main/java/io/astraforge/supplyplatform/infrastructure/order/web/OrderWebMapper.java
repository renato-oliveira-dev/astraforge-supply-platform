package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingResult;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderResult;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderApprovalCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderApprovalResult;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityCommand;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityResult;

import java.util.Currency;
import java.util.UUID;

final class OrderWebMapper {

    private OrderWebMapper() {
    }

    static CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerId(),
                request.createdBy(),
                request.correlationId());
    }

    static AddOrderItemCommand toCommand(
            UUID orderId,
            AddOrderItemRequest request
    ) {
        return new AddOrderItemCommand(
                orderId,
                request.productId(),
                request.sku(),
                request.productName(),
                request.unitOfMeasure(),
                request.quantity(),
                request.addedBy(),
                request.correlationId());
    }

    static UpdateOrderItemQuantityCommand toCommand(
            UUID orderId,
            UUID orderItemId,
            UpdateOrderItemQuantityRequest request
    ) {
        return new UpdateOrderItemQuantityCommand(
                orderId,
                orderItemId,
                request.quantity(),
                request.changedBy(),
                request.correlationId());
    }

    static RemoveOrderItemCommand toCommand(
            UUID orderId,
            UUID orderItemId,
            RemoveOrderItemRequest request
    ) {
        return new RemoveOrderItemCommand(
                orderId,
                orderItemId,
                request.removedBy(),
                request.correlationId());
    }

    static ApplyOrderItemPricingCommand toCommand(
            UUID orderId,
            UUID orderItemId,
            ApplyOrderItemPricingRequest request
    ) {
        return new ApplyOrderItemPricingCommand(
                orderId,
                orderItemId,
                request.unitPrice(),
                Currency.getInstance(request.currency()),
                request.discountPercentage(),
                request.taxPercentage(),
                request.pricedBy(),
                request.correlationId());
    }

    static SubmitOrderCommand toCommand(
            UUID orderId,
            SubmitOrderRequest request
    ) {
        return new SubmitOrderCommand(
                orderId,
                request.submittedBy(),
                request.correlationId());
    }

    static StartOrderApprovalCommand toCommand(
            UUID orderId,
            StartOrderApprovalRequest request
    ) {
        return new StartOrderApprovalCommand(
                orderId,
                request.startedBy(),
                request.correlationId());
    }

    static CreateOrderResponse toResponse(CreateOrderResult result) {
        return new CreateOrderResponse(
                result.orderId(),
                result.status(),
                result.version(),
                result.createdAt());
    }

    static AddOrderItemResponse toResponse(AddOrderItemResult result) {
        return new AddOrderItemResponse(
                result.orderId(),
                result.orderItemId(),
                result.productId(),
                result.status(),
                result.itemCount(),
                result.version(),
                result.updatedAt());
    }

    static UpdateOrderItemQuantityResponse toResponse(
            UpdateOrderItemQuantityResult result
    ) {
        return new UpdateOrderItemQuantityResponse(
                result.orderId(),
                result.orderItemId(),
                result.quantity(),
                result.status(),
                result.version(),
                result.updatedAt());
    }

    static RemoveOrderItemResponse toResponse(
            RemoveOrderItemResult result
    ) {
        return new RemoveOrderItemResponse(
                result.orderId(),
                result.removedOrderItemId(),
                result.status(),
                result.itemCount(),
                result.version(),
                result.updatedAt());
    }

    static ApplyOrderItemPricingResponse toResponse(
            ApplyOrderItemPricingResult result
    ) {
        return new ApplyOrderItemPricingResponse(
                result.orderId(),
                result.orderItemId(),
                result.itemTotal(),
                result.currency(),
                result.pricingComplete(),
                result.status(),
                result.version(),
                result.updatedAt());
    }
    static SubmitOrderResponse toResponse(SubmitOrderResult result) {
        return new SubmitOrderResponse(
                result.orderId(),
                result.status(),
                result.itemCount(),
                result.total(),
                result.currency(),
                result.version(),
                result.submittedAt());
    }
    static StartOrderApprovalResponse toResponse(
            StartOrderApprovalResult result
    ) {
        return new StartOrderApprovalResponse(
                result.orderId(),
                result.status(),
                result.version(),
                result.startedAt());
    }
}
