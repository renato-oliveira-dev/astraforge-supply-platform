package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.port.in.ListOrdersUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderQueryRepository;
import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;

import java.util.Objects;

public final class ListOrdersService implements ListOrdersUseCase {

    private final OrderQueryRepository orderQueryRepository;

    public ListOrdersService(OrderQueryRepository orderQueryRepository) {
        this.orderQueryRepository = Objects.requireNonNull(
                orderQueryRepository,
                "Order query repository must not be null");
    }

    @Override
    public OrderPageResult list(ListOrdersQuery query) {
        Objects.requireNonNull(
                query,
                "List orders query must not be null");

        return Objects.requireNonNull(
                orderQueryRepository.search(query),
                "Order query repository must return a page");
    }
}
