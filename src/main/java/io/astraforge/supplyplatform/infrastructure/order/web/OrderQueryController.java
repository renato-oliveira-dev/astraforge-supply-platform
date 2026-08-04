package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.GetOrderDetailsUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ListOrdersUseCase;
import io.astraforge.supplyplatform.application.order.usecase.OrderDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public final class OrderQueryController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final GetOrderDetailsUseCase getOrderDetailsUseCase;
    private final ListOrdersUseCase listOrdersUseCase;

    public OrderQueryController(
            GetOrderDetailsUseCase getOrderDetailsUseCase,
            ListOrdersUseCase listOrdersUseCase
    ) {
        this.getOrderDetailsUseCase = Objects.requireNonNull(
                getOrderDetailsUseCase,
                "Get order details use case must not be null");
        this.listOrdersUseCase = Objects.requireNonNull(
                listOrdersUseCase,
                "List orders use case must not be null");
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsResponse> getDetails(
            @PathVariable UUID orderId
    ) {
        OrderDetailsResult result = getOrderDetailsUseCase.getDetails(
                OrderQueryWebMapper.toDetailsQuery(orderId));

        return ResponseEntity.ok(
                OrderQueryWebMapper.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<OrderPageResponse> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        OrderPageResult result = listOrdersUseCase.list(
                OrderQueryWebMapper.toListQuery(
                        customerId,
                        status,
                        page,
                        size));

        return ResponseEntity.ok(
                OrderQueryWebMapper.toResponse(result));
    }

    static int defaultPage() {
        return DEFAULT_PAGE;
    }

    static int defaultSize() {
        return DEFAULT_SIZE;
    }
}
