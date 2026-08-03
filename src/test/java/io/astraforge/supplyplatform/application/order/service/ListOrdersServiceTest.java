package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.port.out.OrderQueryRepository;
import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;
import io.astraforge.supplyplatform.application.order.usecase.OrderSummaryResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListOrdersServiceTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T10:00:00Z");
    private static final Instant UPDATED_AT =
            Instant.parse("2026-08-03T10:05:00Z");

    @Test
    void testListShouldDelegateFiltersAndReturnPage() {
        CapturingOrderQueryRepository repository =
                new CapturingOrderQueryRepository(page());
        ListOrdersService service = new ListOrdersService(repository);
        ListOrdersQuery query = new ListOrdersQuery(
                Optional.of(CUSTOMER_ID),
                Optional.of(OrderStatus.SUBMITTED),
                0,
                20);

        OrderPageResult result = service.list(query);

        assertThat(repository.query())
                .as("query delegated to repository")
                .isEqualTo(query);
        assertThat(result.content())
                .as("listed orders")
                .hasSize(1);
        assertThat(result.content().getFirst().orderId())
                .as("listed order identifier")
                .isEqualTo(ORDER_ID);
        assertThat(result.totalElements())
                .as("total listed orders")
                .isEqualTo(1L);
        assertThat(result.totalPages())
                .as("total order pages")
                .isEqualTo(1);
        assertThat(result.empty())
                .as("page empty state")
                .isFalse();
    }

    @Test
    void testListShouldReturnEmptyPage() {
        ListOrdersService service = new ListOrdersService(
                new CapturingOrderQueryRepository(
                        new OrderPageResult(
                                List.of(),
                                0,
                                20,
                                0,
                                0)));

        OrderPageResult result = service.list(
                new ListOrdersQuery(
                        Optional.empty(),
                        Optional.empty(),
                        0,
                        20));

        assertThat(result.empty())
                .as("empty order page")
                .isTrue();
        assertThat(result.content())
                .as("empty order page content")
                .isEmpty();
    }

    @Test
    void testListShouldRejectNullQuery() {
        ListOrdersService service = new ListOrdersService(
                new CapturingOrderQueryRepository(page()));

        assertThatThrownBy(() -> service.list(null))
                .as("null list orders query")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("List orders query must not be null");
    }

    @Test
    void testQueryShouldRejectInvalidPageSize() {
        assertThatThrownBy(() -> new ListOrdersQuery(
                Optional.empty(),
                Optional.empty(),
                0,
                101))
                .as("list orders maximum page size")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Page size must be between 1 and 100");
    }

    @Test
    void testSummaryShouldRequireTotalAndCurrencyTogether() {
        assertThatThrownBy(() -> new OrderSummaryResult(
                ORDER_ID,
                CUSTOMER_ID,
                OrderStatus.DRAFT,
                1,
                false,
                Optional.of(new BigDecimal("100.00")),
                Optional.empty(),
                1,
                CREATED_AT,
                UPDATED_AT))
                .as("order summary monetary consistency")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Order total and currency must be provided together");
    }

    private static OrderPageResult page() {
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

    private static final class CapturingOrderQueryRepository
            implements OrderQueryRepository {

        private final OrderPageResult result;
        private ListOrdersQuery query;

        private CapturingOrderQueryRepository(OrderPageResult result) {
            this.result = result;
        }

        @Override
        public OrderPageResult search(ListOrdersQuery query) {
            this.query = query;
            return result;
        }

        private ListOrdersQuery query() {
            return query;
        }
    }
}
