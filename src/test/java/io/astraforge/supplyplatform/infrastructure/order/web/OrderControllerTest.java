package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ApplyOrderItemPricingUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RemoveOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.SubmitOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.UpdateOrderItemQuantityUseCase;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingResult;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemResult;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderResult;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityCommand;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderControllerTest {

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
    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T13:00:00Z");
    private static final Instant UPDATED_AT =
            Instant.parse("2026-08-03T13:05:00Z");

    @Test
    void testCreateShouldDelegateCommandAndReturnCreatedResponse() {
        ControllerFixture fixture = new ControllerFixture();
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER_ID,
                USER_ID,
                " correlation-create-rest-001 ");

        ResponseEntity<CreateOrderResponse> response =
                fixture.controller().create(
                        request,
                        UriComponentsBuilder.fromUriString(
                                "https://api.astraforge.test"));

        assertThat(fixture.createUseCase().command())
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
    }

    @Test
    void testAddItemShouldDelegateCommandAndReturnCreatedResponse() {
        ControllerFixture fixture = new ControllerFixture();
        AddOrderItemRequest request = new AddOrderItemRequest(
                PRODUCT_ID,
                " SAFE-HELMET-001 ",
                " Industrial Safety Helmet ",
                " UNIT ",
                new BigDecimal("2.000"),
                USER_ID,
                " correlation-add-item-rest-001 ");

        ResponseEntity<AddOrderItemResponse> response =
                fixture.controller().addItem(
                        ORDER_ID,
                        request,
                        UriComponentsBuilder.fromUriString(
                                "https://api.astraforge.test"));

        assertThat(fixture.addItemUseCase().command())
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
    }

    @Test
    void testUpdateQuantityShouldDelegateCommandAndReturnOk() {
        ControllerFixture fixture = new ControllerFixture();
        UpdateOrderItemQuantityRequest request =
                new UpdateOrderItemQuantityRequest(
                        new BigDecimal("5.000"),
                        USER_ID,
                        " correlation-update-quantity-rest-001 ");

        ResponseEntity<UpdateOrderItemQuantityResponse> response =
                fixture.controller().updateQuantity(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        request);

        assertThat(fixture.updateQuantityUseCase().command())
                .as("update quantity command delegated by controller")
                .isEqualTo(new UpdateOrderItemQuantityCommand(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        new BigDecimal("5.000"),
                        USER_ID,
                        "correlation-update-quantity-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("update quantity HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("update quantity response")
                .isEqualTo(new UpdateOrderItemQuantityResponse(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        new BigDecimal("5.000"),
                        OrderStatus.DRAFT,
                        2,
                        UPDATED_AT));
    }

    @Test
    void testRemoveItemShouldDelegateCommandAndReturnOk() {
        ControllerFixture fixture = new ControllerFixture();
        RemoveOrderItemRequest request = new RemoveOrderItemRequest(
                USER_ID,
                " correlation-remove-item-rest-001 ");

        ResponseEntity<RemoveOrderItemResponse> response =
                fixture.controller().removeItem(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        request);

        assertThat(fixture.removeItemUseCase().command())
                .as("remove item command delegated by controller")
                .isEqualTo(new RemoveOrderItemCommand(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        USER_ID,
                        "correlation-remove-item-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("remove item HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("remove item response")
                .isEqualTo(new RemoveOrderItemResponse(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        OrderStatus.DRAFT,
                        0,
                        2,
                        UPDATED_AT));
    }


    @Test
    void testApplyPricingShouldDelegateCommandAndReturnOk() {
        ControllerFixture fixture = new ControllerFixture();
        ApplyOrderItemPricingRequest request =
                new ApplyOrderItemPricingRequest(
                        new BigDecimal("100.00"),
                        "BRL",
                        new BigDecimal("10.0000"),
                        new BigDecimal("20.0000"),
                        USER_ID,
                        " correlation-price-item-rest-001 ");

        ResponseEntity<ApplyOrderItemPricingResponse> response =
                fixture.controller().applyPricing(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        request);

        assertThat(fixture.applyPricingUseCase().command())
                .as("apply pricing command delegated by controller")
                .isEqualTo(new ApplyOrderItemPricingCommand(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        new BigDecimal("100.00"),
                        BRL,
                        new BigDecimal("10.0000"),
                        new BigDecimal("20.0000"),
                        USER_ID,
                        "correlation-price-item-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("apply pricing HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("apply pricing response")
                .isEqualTo(new ApplyOrderItemPricingResponse(
                        ORDER_ID,
                        ORDER_ITEM_ID,
                        new BigDecimal("216.00"),
                        BRL,
                        true,
                        OrderStatus.DRAFT,
                        2,
                        UPDATED_AT));
    }


    @Test
    void testSubmitShouldDelegateCommandAndReturnOk() {
        ControllerFixture fixture = new ControllerFixture();
        SubmitOrderRequest request = new SubmitOrderRequest(
                USER_ID,
                " correlation-submit-order-rest-001 ");

        ResponseEntity<SubmitOrderResponse> response =
                fixture.controller().submit(ORDER_ID, request);

        assertThat(fixture.submitOrderUseCase().command())
                .as("submit order command delegated by controller")
                .isEqualTo(new SubmitOrderCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-submit-order-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("submit order HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("submit order response")
                .isEqualTo(new SubmitOrderResponse(
                        ORDER_ID,
                        OrderStatus.SUBMITTED,
                        1,
                        new BigDecimal("216.00"),
                        BRL,
                        3,
                        UPDATED_AT));
    }

    @Test
    void testConstructorShouldRejectNullDependencies() {
        ControllerFixture fixture = new ControllerFixture();

        assertThatThrownBy(() -> new OrderController(
                null,
                fixture.addItemUseCase(),
                fixture.updateQuantityUseCase(),
                fixture.removeItemUseCase(),
                fixture.applyPricingUseCase(),
                fixture.submitOrderUseCase()))
                .as("null create order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Create order use case must not be null");
        assertThatThrownBy(() -> new OrderController(
                fixture.createUseCase(),
                null,
                fixture.updateQuantityUseCase(),
                fixture.removeItemUseCase(),
                fixture.applyPricingUseCase(),
                fixture.submitOrderUseCase()))
                .as("null add order item use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Add order item use case must not be null");
        assertThatThrownBy(() -> new OrderController(
                fixture.createUseCase(),
                fixture.addItemUseCase(),
                null,
                fixture.removeItemUseCase(),
                fixture.applyPricingUseCase(),
                fixture.submitOrderUseCase()))
                .as("null update quantity use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Update order item quantity use case must not be null");
        assertThatThrownBy(() -> new OrderController(
                fixture.createUseCase(),
                fixture.addItemUseCase(),
                fixture.updateQuantityUseCase(),
                null,
                fixture.applyPricingUseCase(),
                fixture.submitOrderUseCase()))
                .as("null remove item use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Remove order item use case must not be null");
        assertThatThrownBy(() -> new OrderController(
                fixture.createUseCase(),
                fixture.addItemUseCase(),
                fixture.updateQuantityUseCase(),
                fixture.removeItemUseCase(),
                null))
                .as("null apply pricing use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Apply order item pricing use case must not be null");
        assertThatThrownBy(() -> new OrderController(
                fixture.createUseCase(),
                fixture.addItemUseCase(),
                fixture.updateQuantityUseCase(),
                fixture.removeItemUseCase(),
                fixture.applyPricingUseCase(),
                null))
                .as("null submit order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Submit order use case must not be null");
    }

    private record ControllerFixture(
            CapturingCreateOrderUseCase createUseCase,
            CapturingAddOrderItemUseCase addItemUseCase,
            CapturingUpdateQuantityUseCase updateQuantityUseCase,
            CapturingRemoveOrderItemUseCase removeItemUseCase,
            CapturingApplyPricingUseCase applyPricingUseCase,
            CapturingSubmitOrderUseCase submitOrderUseCase,
            OrderController controller
    ) {

        private ControllerFixture() {
            this(
                    new CapturingCreateOrderUseCase(),
                    new CapturingAddOrderItemUseCase(),
                    new CapturingUpdateQuantityUseCase(),
                    new CapturingRemoveOrderItemUseCase(),
                    new CapturingApplyPricingUseCase(),
                    new CapturingSubmitOrderUseCase());
        }

        private ControllerFixture(
                CapturingCreateOrderUseCase createUseCase,
                CapturingAddOrderItemUseCase addItemUseCase,
                CapturingUpdateQuantityUseCase updateQuantityUseCase,
                CapturingRemoveOrderItemUseCase removeItemUseCase,
                CapturingApplyPricingUseCase applyPricingUseCase,
                CapturingSubmitOrderUseCase submitOrderUseCase
        ) {
            this(
                    createUseCase,
                    addItemUseCase,
                    updateQuantityUseCase,
                    removeItemUseCase,
                    applyPricingUseCase,
                    submitOrderUseCase,
                    new OrderController(
                            createUseCase,
                            addItemUseCase,
                            updateQuantityUseCase,
                            removeItemUseCase,
                            applyPricingUseCase,
                            submitOrderUseCase));
        }
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

    private static final class CapturingUpdateQuantityUseCase
            implements UpdateOrderItemQuantityUseCase {

        private final AtomicReference<UpdateOrderItemQuantityCommand> command =
                new AtomicReference<>();

        @Override
        public UpdateOrderItemQuantityResult updateQuantity(
                UpdateOrderItemQuantityCommand command
        ) {
            this.command.set(command);
            return new UpdateOrderItemQuantityResult(
                    ORDER_ID,
                    ORDER_ITEM_ID,
                    new BigDecimal("5.000"),
                    OrderStatus.DRAFT,
                    2,
                    UPDATED_AT);
        }

        private UpdateOrderItemQuantityCommand command() {
            return command.get();
        }
    }

    private static final class CapturingRemoveOrderItemUseCase
            implements RemoveOrderItemUseCase {

        private final AtomicReference<RemoveOrderItemCommand> command =
                new AtomicReference<>();

        @Override
        public RemoveOrderItemResult removeItem(
                RemoveOrderItemCommand command
        ) {
            this.command.set(command);
            return new RemoveOrderItemResult(
                    ORDER_ID,
                    ORDER_ITEM_ID,
                    OrderStatus.DRAFT,
                    0,
                    2,
                    UPDATED_AT);
        }

        private RemoveOrderItemCommand command() {
            return command.get();
        }
    }


    private static final class CapturingApplyPricingUseCase
            implements ApplyOrderItemPricingUseCase {

        private final AtomicReference<ApplyOrderItemPricingCommand> command =
                new AtomicReference<>();

        @Override
        public ApplyOrderItemPricingResult applyPricing(
                ApplyOrderItemPricingCommand command
        ) {
            this.command.set(command);
            return new ApplyOrderItemPricingResult(
                    ORDER_ID,
                    ORDER_ITEM_ID,
                    new BigDecimal("216.00"),
                    BRL,
                    true,
                    OrderStatus.DRAFT,
                    2,
                    UPDATED_AT);
        }

        private ApplyOrderItemPricingCommand command() {
            return command.get();
        }
    }



    private static final class CapturingSubmitOrderUseCase
            implements SubmitOrderUseCase {

        private final AtomicReference<SubmitOrderCommand> command =
                new AtomicReference<>();

        @Override
        public SubmitOrderResult submit(SubmitOrderCommand command) {
            this.command.set(command);
            return new SubmitOrderResult(
                    ORDER_ID,
                    OrderStatus.SUBMITTED,
                    1,
                    new BigDecimal("216.00"),
                    BRL,
                    3,
                    UPDATED_AT);
        }

        private SubmitOrderCommand command() {
            return command.get();
        }
    }

}
