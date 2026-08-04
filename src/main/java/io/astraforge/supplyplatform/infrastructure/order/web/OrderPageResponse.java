package io.astraforge.supplyplatform.infrastructure.order.web;

import java.util.List;
import java.util.Objects;

public record OrderPageResponse(
        List<OrderSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public OrderPageResponse {
        content = List.copyOf(
                Objects.requireNonNull(
                        content,
                        "Page content must not be null"));
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page index must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Page size must be greater than zero");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "Total elements must not be negative");
        }
        if (totalPages < 0) {
            throw new IllegalArgumentException(
                    "Total pages must not be negative");
        }
    }

    public boolean empty() {
        return content.isEmpty();
    }
}
