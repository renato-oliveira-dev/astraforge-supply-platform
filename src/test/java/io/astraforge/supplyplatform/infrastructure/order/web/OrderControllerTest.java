package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001");
    private static final UUID ORDER_ITEM_ID =
            UUID.fromString(
                    "11000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString(
                    "30000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString(
                    "40000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T13:00:00Z");
    private static final Instant UPDATED_AT =
            Instant.parse("2026-08-03T13:05:00Z");

    @Test
    void testCreateShouldDelegateCommandAndReturnCreatedResponse() {
        CapturingCreateOrderUseCase createUseCase =
                new CapturingCreateOrderUseCase();
        OrderController controller = new OrderController(
                createUseCase,
                new CapturingAddOrderItemUseCase());
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER_ID,
                USER_ID,
                " correlation-create-rest-001 ");

        ResponseEntity<CreateOrderResponse> response =
                controller.create(
                        request,
                        UriComponentsBuilder.fromUriString(
                                "https://api.astraforge.test"));

        assertThat(createUseCase.command())
                .as("create order command delegated by controller")
                .isEqualTo(new CreateOrderCommand(
                        CUSTOMER_ID,
                        USER_ID,
                        "correlation-create-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("create order HTTP status")
                .isEqualTo(201);
        assertThat(response.getHeaders().getLocation())
                .as("created order location")
                .hasToString(
                        "https://api.astraforge.test/api/v1/orders/"
                                + ORDER_ID);
        assertThat(response.getBody())
                .as("created order response")
                .isEqualTo(new CreateOrderResponse(
                        ORDER_ID,
                        OrderStatus.DRAFT,
                        0,
                        CREATED_AT));
    }

    @Test
    void testAddItemShouldDelegateCommandAndReturnCreatedResponse() {
        CapturingAddOrderItemUseCase addItemUseCase =
                new CapturingAddOrderItemUseCase();
        OrderController controller = new OrderController(
                new CapturingCreateOrderUseCase(),
                addItemUseCase);
        AddOrderItemRequest request = new AddOrderItemRequest(
                PRODUCT_ID,
                " SAFE-HELMET-001 ",
                " Industrial Safety Helmet ",
                " UNIT ",
                new BigDecimal("2.000"),
                USER_ID,
                " correlation-add-item-rest-001 ");

        ResponseEntity<AddOrderItemResponse> response =
                controller.addItem(
                        ORDER_ID,
                        request,
                        UriComponentsBuilder.fromUriString(
                                "https://api.astraforge.test"));

        assertThat(addItemUseCase.command())
                .as("add order item command delegated by controller")
                .isEqualTo(new AddOrderItemCommand(
                        ORDER_ID,
                        PRODUCT_ID,
                        "SAFE-HELMET-001",
                        "Industrial Safety Helmet",
                        "UNIT",
                        new BigDecimal("2.000"),
                        USER_ID,
                        "correlation-add-item-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("add order item HTTP status")
                .isEqualTo(201);
        assertThat(response.getHeaders().getLocation())
                .as("created order item location")
                .hasToString(
                        "https://api.astraforge.test/api/v1/orders/"
                                + ORDER_ID
                                + "/items/"
                                + ORDER_ITEM_ID);
        assertThat(response.getBody())
                .as("created order item response")
                .isEqualTo(new AddOrderItemResponse(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        PRODUCT_ID,
                        OrderStatus.DRAFT,
                        1,
                        1,
                        UPDATED_AT));
    }

    @Test
    void testConstructorShouldRejectNullCreateUseCase() {
        assertThatThrownBy(() -> new OrderController(
                null,
                new CapturingAddOrderItemUseCase()))
                .as("null create order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Create order use case must not be null");
    }

    @Test
    void testConstructorShouldRejectNullAddItemUseCase() {
        assertThatThrownBy(() -> new OrderController(
                new CapturingCreateOrderUseCase(),
                null))
                .as("null add order item use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Add order item use case must not be null");
    }

    private static final class CapturingCreateOrderUseCase
            implements CreateOrderUseCase {

        private final AtomicReference<CreateOrderCommand> command =
                new AtomicReference<>();

        @Override
        public CreateOrderResult create(CreateOrderCommand command) {
            this.command.set(command);
            return new CreateOrderResult(
                    ORDER_ID,
                    OrderStatus.DRAFT,
                    0,
                    CREATED_AT);
        }

        private CreateOrderCommand command() {
            return command.get();
        }
    }

    private static final class CapturingAddOrderItemUseCase
            implements AddOrderItemUseCase {

        private final AtomicReference<AddOrderItemCommand> command =
                new AtomicReference<>();

        @Override
        public AddOrderItemResult addItem(AddOrderItemCommand command) {
            this.command.set(command);
            return new AddOrderItemResult(
                    ORDER_ID,
                    ORDER_ITEM_ID,
                    PRODUCT_ID,
                    OrderStatus.DRAFT,
                    1,
                    1,
                    UPDATED_AT);
        }

        private AddOrderItemCommand command() {
            return command.get();
        }
    }
}
