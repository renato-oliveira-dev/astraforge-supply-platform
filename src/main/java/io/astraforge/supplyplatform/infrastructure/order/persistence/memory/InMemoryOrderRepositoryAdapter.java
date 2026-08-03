package io.astraforge.supplyplatform.infrastructure.order.persistence.memory;

import io.astraforge.supplyplatform.application.order.port.out.OrderQueryRepository;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderSummaryResult;
import io.astraforge.supplyplatform.domain.order.aggregate.Order;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderTotals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryOrderRepositoryAdapter
        implements OrderRepository, OrderQueryRepository {

    private static final Comparator<Order> DEFAULT_ORDERING =
            Comparator.comparing(Order::createdAt)
                    .reversed()
                    .thenComparing(order -> order.id().value());

    private final Map<OrderId, Order> orders =
            new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        Order requiredOrder = Objects.requireNonNull(
                order,
                "Order must not be null");
        orders.put(requiredOrder.id(), requiredOrder);
        return requiredOrder;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public boolean existsById(OrderId orderId) {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        return orders.containsKey(orderId);
    }

    @Override
    public OrderPageResult search(ListOrdersQuery query) {
        Objects.requireNonNull(
                query,
                "List orders query must not be null");

        List<Order> matchingOrders = orders.values()
                .stream()
                .filter(order -> matchesCustomer(order, query))
                .filter(order -> matchesStatus(order, query))
                .sorted(DEFAULT_ORDERING)
                .toList();

        return page(matchingOrders, query);
    }

    public void clear() {
        orders.clear();
    }

    public int size() {
        return orders.size();
    }

    private static boolean matchesCustomer(
            Order order,
            ListOrdersQuery query
    ) {
        return query.customerId()
                .map(customerId -> customerId.equals(
                        order.customerReference()
                                .customerId()
                                .value()))
                .orElse(true);
    }

    private static boolean matchesStatus(
            Order order,
            ListOrdersQuery query
    ) {
        return query.status()
                .map(status -> status == order.status())
                .orElse(true);
    }

    private static OrderPageResult page(
            List<Order> matchingOrders,
            ListOrdersQuery query
    ) {
        long totalElements = matchingOrders.size();
        int totalPages = totalPages(totalElements, query.size());
        int fromIndex = pageStart(query, totalElements);

        if (fromIndex >= totalElements) {
            return new OrderPageResult(
                    List.of(),
                    query.page(),
                    query.size(),
                    totalElements,
                    totalPages);
        }

        int toIndex = Math.min(
                fromIndex + query.size(),
                matchingOrders.size());
        List<OrderSummaryResult> content =
                new ArrayList<>(toIndex - fromIndex);
        matchingOrders.subList(fromIndex, toIndex)
                .stream()
                .map(InMemoryOrderRepositoryAdapter::summary)
                .forEach(content::add);

        return new OrderPageResult(
                content,
                query.page(),
                query.size(),
                totalElements,
                totalPages);
    }

    private static int pageStart(
            ListOrdersQuery query,
            long totalElements
    ) {
        long start = (long) query.page() * query.size();
        if (start >= totalElements) {
            return Math.toIntExact(totalElements);
        }
        return Math.toIntExact(start);
    }

    private static int totalPages(long totalElements, int pageSize) {
        if (totalElements == 0) {
            return 0;
        }
        return Math.toIntExact(
                (totalElements + pageSize - 1) / pageSize);
    }

    private static OrderSummaryResult summary(Order order) {
        if (!order.pricingComplete()) {
            return summaryWithoutTotals(order);
        }

        OrderTotals totals = order.totals();
        return new OrderSummaryResult(
                order.id().value(),
                order.customerReference().customerId().value(),
                order.status(),
                order.items().size(),
                true,
                Optional.of(totals.total().amount()),
                Optional.of(totals.total().currency()),
                order.version(),
                order.createdAt(),
                order.updatedAt());
    }

    private static OrderSummaryResult summaryWithoutTotals(
            Order order
    ) {
        return new OrderSummaryResult(
                order.id().value(),
                order.customerReference().customerId().value(),
                order.status(),
                order.items().size(),
                false,
                Optional.empty(),
                Optional.empty(),
                order.version(),
                order.createdAt(),
                order.updatedAt());
    }
}
