package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant CREATED_AT =
            Instant.parse("2026-08-03T13:00:00Z");

    @Test
    void testCreateShouldDelegateCommandAndReturnCreatedResponse() {
        CapturingCreateOrderUseCase useCase =
                new CapturingCreateOrderUseCase();
        OrderController controller = new OrderController(useCase);
        CreateOrderRequest request = new CreateOrderRequest(
                CUSTOMER_ID,
                USER_ID,
                " correlation-create-rest-001 ");

        ResponseEntity<CreateOrderResponse> response =
                controller.create(
                        request,
                        UriComponentsBuilder.fromUriString(
                                "https://api.astraforge.test"));

        assertThat(useCase.command())
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
    void testConstructorShouldRejectNullUseCase() {
        assertThatThrownBy(() -> new OrderController(null))
                .as("null create order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Create order use case must not be null");
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
}
