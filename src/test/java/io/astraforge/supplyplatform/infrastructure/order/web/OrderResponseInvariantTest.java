package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderResponseInvariantTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant NOW =
            Instant.parse("2026-08-03T23:00:00Z");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testOrderPageResponseShouldDefensivelyCopyContent() {
        java.util.ArrayList<OrderSummaryResponse> content =
                new java.util.ArrayList<>();
        content.add(summary());

        OrderPageResponse response = new OrderPageResponse(
                content,
                0,
                20,
                1,
                1);
        content.clear();

        assertThat(response.content())
                .as("defensively copied order page content")
                .hasSize(1);
        var sonarTarget1 = response.content();
        assertThatThrownBy(sonarTarget1::clear)
                .as("immutable order page content")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testOrderDetailsResponseShouldDefensivelyCopyItems() {
        java.util.ArrayList<OrderItemDetailsResponse> items =
                new java.util.ArrayList<>();
        items.add(item());

        OrderDetailsResponse response = new OrderDetailsResponse(
                ORDER_ID,
                CUSTOMER_ID,
                OrderStatus.DRAFT,
                USER_ID,
                NOW,
                NOW,
                1,
                items,
                true,
                Optional.of(new BigDecimal("100.00")),
                Optional.of(BigDecimal.ZERO),
                Optional.of(BigDecimal.ZERO),
                Optional.of(new BigDecimal("100.00")),
                Optional.of(BRL));
        items.clear();

        assertThat(response.items())
                .as("defensively copied order detail items")
                .hasSize(1);
        var sonarTarget2 = response.items();
        assertThatThrownBy(sonarTarget2::clear)
                .as("immutable order detail items")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testOrderSummaryShouldRequireTotalAndCurrencyTogether() {
        Optional<BigDecimal> sonarArgument3Value6 =
                Optional.of(new BigDecimal("100.00"));
        Optional<Currency> sonarArgument3Value7 = Optional.empty();

        assertThatThrownBy(() -> new OrderSummaryResponse(
                ORDER_ID,
                CUSTOMER_ID,
                OrderStatus.DRAFT,
                1,
                false,
                sonarArgument3Value6,
                sonarArgument3Value7,
                1,
                NOW,
                NOW))
                .as("order summary monetary consistency")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Order total and currency must be provided together");
    }

    @Test
    void testInventoryOutcomeShouldRequireFailureOptionalContainer() {
        assertThatThrownBy(() ->
                new InventoryReservationOutcomeResponse(
                        ORDER_ID,
                        OrderStatus.INVENTORY_RESERVED,
                        USER_ID,
                        NOW,
                        null,
                        1))
                .as("inventory outcome optional container")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Failure reason must not be null");
    }

    @Test
    void testCompletionResponseShouldRejectEmptyOrder() {
        var sonarArgument4Value6 = new BigDecimal("100.00");
        assertThatThrownBy(() -> new CompleteOrderFulfillmentResponse(ORDER_ID, OrderStatus.COMPLETED, USER_ID, NOW, 0, sonarArgument4Value6, BRL, 1))
                .as("completion response item count")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item count must be greater than zero");
    }

    private static OrderSummaryResponse summary() {
        return new OrderSummaryResponse(
                ORDER_ID,
                CUSTOMER_ID,
                OrderStatus.DRAFT,
                1,
                true,
                Optional.of(new BigDecimal("100.00")),
                Optional.of(BRL),
                1,
                NOW,
                NOW);
    }

    private static OrderItemDetailsResponse item() {
        return new OrderItemDetailsResponse(
                UUID.fromString(
                        "11000000-0000-0000-0000-000000000001"),
                UUID.fromString(
                        "40000000-0000-0000-0000-000000000001"),
                "SAFE-HELMET-001",
                "Industrial Safety Helmet",
                "UNIT",
                BigDecimal.ONE,
                Optional.of(new BigDecimal("100.00")),
                Optional.of(BigDecimal.ZERO),
                Optional.of(BigDecimal.ZERO),
                Optional.of(new BigDecimal("100.00")),
                Optional.of(BRL));
    }
}
