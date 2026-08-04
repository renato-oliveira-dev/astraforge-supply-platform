package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.GetOrderDetailsUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ListOrdersUseCase;
import io.astraforge.supplyplatform.application.order.usecase.GetOrderDetailsQuery;
import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderItemDetailsResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderSummaryResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderQueryControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ORDER_ITEM_ID =
            UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T10:00:00Z");
    private static final Instant UPDATED_AT =
            Instant.parse("2026-08-03T10:05:00Z");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testGetDetailsShouldDelegateQueryAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<OrderDetailsResponse> response =
                fixture.controller().getDetails(ORDER_ID);

        assertThat(fixture.detailsUseCase().query())
                .as("order details query delegated by controller")
                .isEqualTo(new GetOrderDetailsQuery(ORDER_ID));
        assertThat(response.getStatusCode().value())
                .as("order details HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("order details response")
                .isEqualTo(detailsResponse());
        assertThat(response.getBody().items().getFirst().priced())
                .as("order item pricing state")
                .isTrue();
    }

    @Test
    void testListShouldDelegateFiltersAndReturnPage() {
        Fixture fixture = new Fixture();

        ResponseEntity<OrderPageResponse> response =
                fixture.controller().list(
                        CUSTOMER_ID,
                        OrderStatus.SUBMITTED,
                        0,
                        20);

        assertThat(fixture.listUseCase().query())
                .as("order list query delegated by controller")
                .isEqualTo(new ListOrdersQuery(
                        Optional.of(CUSTOMER_ID),
                        Optional.of(OrderStatus.SUBMITTED),
                        0,
                        20));
        assertThat(response.getStatusCode().value())
                .as("order list HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody().content())
                .as("order list response content")
                .hasSize(1);
        assertThat(response.getBody().empty())
                .as("order list response empty state")
                .isFalse();
    }

    @Test
    void testListShouldSupportAbsentFilters() {
        Fixture fixture = new Fixture();

        fixture.controller().list(
                null,
                null,
                OrderQueryController.defaultPage(),
                OrderQueryController.defaultSize());

        assertThat(fixture.listUseCase().query())
                .as("order list query without filters")
                .isEqualTo(new ListOrdersQuery(
                        Optional.empty(),
                        Optional.empty(),
                        0,
                        20));
    }

    @Test
    void testConstructorShouldRejectNullDependencies() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> new OrderQueryController(
                null,
                fixture.listUseCase()))
                .as("null get order details use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Get order details use case must not be null");
        assertThatThrownBy(() -> new OrderQueryController(
                fixture.detailsUseCase(),
                null))
                .as("null list orders use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("List orders use case must not be null");
    }

    private static OrderDetailsResponse detailsResponse() {
        return OrderQueryWebMapper.toResponse(detailsResult());
    }

    private static OrderDetailsResult detailsResult() {
        return new OrderDetailsResult(
                ORDER_ID,
                CUSTOMER_ID,
                OrderStatus.DRAFT,
                USER_ID,
                CREATED_AT,
                UPDATED_AT,
                2,
                List.of(new OrderItemDetailsResult(
                        ORDER_ITEM_ID,
                        PRODUCT_ID,
                        "SAFE-HELMET-001",
                        "Industrial Safety Helmet",
                        "UNIT",
                        new BigDecimal("2.000"),
                        Optional.of(new BigDecimal("100.00")),
                        Optional.of(new BigDecimal("10.0000")),
                        Optional.of(new BigDecimal("20.0000")),
                        Optional.of(new BigDecimal("216.00")),
                        Optional.of(BRL))),
                true,
                Optional.of(new BigDecimal("200.00")),
                Optional.of(new BigDecimal("20.00")),
                Optional.of(new BigDecimal("36.00")),
                Optional.of(new BigDecimal("216.00")),
                Optional.of(BRL));
    }

    private static OrderPageResult pageResult() {
        return new OrderPageResult(
                List.of(new OrderSummaryResult(
                        ORDER_ID,
                        CUSTOMER_ID,
                        OrderStatus.SUBMITTED,
                        1,
                        true,
                        Optional.of(new BigDecimal("216.00")),
                        Optional.of(BRL),
                        3,
                        CREATED_AT,
                        UPDATED_AT)),
                0,
                20,
                1,
                1);
    }

    private record Fixture(
            CapturingDetailsUseCase detailsUseCase,
            CapturingListUseCase listUseCase,
            OrderQueryController controller
    ) {

        private Fixture() {
            this(
                    new CapturingDetailsUseCase(),
                    new CapturingListUseCase());
        }

        private Fixture(
                CapturingDetailsUseCase detailsUseCase,
                CapturingListUseCase listUseCase
        ) {
            this(
                    detailsUseCase,
                    listUseCase,
                    new OrderQueryController(
                            detailsUseCase,
                            listUseCase));
        }
    }

    private static final class CapturingDetailsUseCase
            implements GetOrderDetailsUseCase {

        private final AtomicReference<GetOrderDetailsQuery> query =
                new AtomicReference<>();

        @Override
        public OrderDetailsResult getDetails(GetOrderDetailsQuery query) {
            this.query.set(query);
            return detailsResult();
        }

        private GetOrderDetailsQuery query() {
            return query.get();
        }
    }

    private static final class CapturingListUseCase
            implements ListOrdersUseCase {

        private final AtomicReference<ListOrdersQuery> query =
                new AtomicReference<>();

        @Override
        public OrderPageResult list(ListOrdersQuery query) {
            this.query.set(query);
            return pageResult();
        }

        private ListOrdersQuery query() {
            return query.get();
        }
    }
}
