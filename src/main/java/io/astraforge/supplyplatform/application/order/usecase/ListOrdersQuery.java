package io.astraforge.supplyplatform.application.order.usecase;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;

import java.util.Optional;
import java.util.UUID;

public record ListOrdersQuery(
        Optional<UUID> customerId,
        Optional<OrderStatus> status,
        int page,
        int size
) {

    private static final int MAX_PAGE_SIZE = 100;

    public ListOrdersQuery {
        customerId = requireOptional(customerId, "Customer ID filter");
        status = requireOptional(status, "Status filter");
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page index must not be negative");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }
    }

    private static <T> Optional<T> requireOptional(
            Optional<T> value,
            String fieldName
    ) {
        if (value == null) {
            throw new NullPointerException(
                    fieldName + " must not be null");
        }
        return value;
    }
}
