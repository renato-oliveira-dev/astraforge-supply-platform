package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.usecase.GetOrderDetailsQuery;
import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderItemDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderSummaryResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.util.Optional;
import java.util.UUID;

final class OrderQueryWebMapper {

    private OrderQueryWebMapper() {
    }

    static GetOrderDetailsQuery toDetailsQuery(UUID orderId) {
        return new GetOrderDetailsQuery(orderId);
    }

    static ListOrdersQuery toListQuery(
            UUID customerId,
            OrderStatus status,
            int page,
            int size
    ) {
        return new ListOrdersQuery(
                Optional.ofNullable(customerId),
                Optional.ofNullable(status),
                page,
                size);
    }

    static OrderDetailsResponse toResponse(OrderDetailsResult result) {
        return new OrderDetailsResponse(
                result.orderId(),
                result.customerId(),
                result.status(),
                result.createdBy(),
                result.createdAt(),
                result.updatedAt(),
                result.version(),
                result.items()
                        .stream()
                        .map(OrderQueryWebMapper::toResponse)
                        .toList(),
                result.pricingComplete(),
                result.subtotal(),
                result.discount(),
                result.tax(),
                result.total(),
                result.currency());
    }

    static OrderPageResponse toResponse(OrderPageResult result) {
        return new OrderPageResponse(
                result.content()
                        .stream()
                        .map(OrderQueryWebMapper::toResponse)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    private static OrderItemDetailsResponse toResponse(
            OrderItemDetailsResult result
    ) {
        return new OrderItemDetailsResponse(
                result.orderItemId(),
                result.productId(),
                result.sku(),
                result.productName(),
                result.unitOfMeasure(),
                result.quantity(),
                result.unitPrice(),
                result.discountPercentage(),
                result.taxPercentage(),
                result.total(),
                result.currency());
    }

    private static OrderSummaryResponse toResponse(
            OrderSummaryResult result
    ) {
        return new OrderSummaryResponse(
                result.orderId(),
                result.customerId(),
                result.status(),
                result.itemCount(),
                result.pricingComplete(),
                result.total(),
                result.currency(),
                result.version(),
                result.createdAt(),
                result.updatedAt());
    }
}
