package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.CancelOrderUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCancellationControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant CANCELLED_AT =
            Instant.parse("2026-08-03T14:20:00Z");

    @Test
    void testCancelShouldDelegateCommandAndReturnOk() {
        CapturingCancelOrderUseCase useCase =
                new CapturingCancelOrderUseCase();
        OrderCancellationController controller =
                new OrderCancellationController(useCase);

        ResponseEntity<CancelOrderResponse> response =
                controller.cancel(
                        ORDER_ID,
                        new CancelOrderRequest(
                                " Request is no longer required. ",
                                USER_ID,
                                " correlation-cancel-order-rest-001 "));

        assertThat(useCase.command())
                .as("cancel order command delegated by controller")
                .isEqualTo(new CancelOrderCommand(
                        ORDER_ID,
                        "Request is no longer required.",
                        USER_ID,
                        "correlation-cancel-order-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("cancel order HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("cancel order response")
                .isEqualTo(new CancelOrderResponse(
                        ORDER_ID,
                        OrderStatus.CANCELLED,
                        "Request is no longer required.",
                        USER_ID,
                        CANCELLED_AT,
                        7));
    }

    @Test
    void testConstructorShouldRejectNullUseCase() {
        assertThatThrownBy(() -> new OrderCancellationController(null))
                .as("null cancel order use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Cancel order use case must not be null");
    }

    private static final class CapturingCancelOrderUseCase
            implements CancelOrderUseCase {

        private final AtomicReference<CancelOrderCommand> command =
                new AtomicReference<>();

        @Override
        public CancelOrderResult cancel(CancelOrderCommand command) {
            this.command.set(command);
            return new CancelOrderResult(
                    ORDER_ID,
                    OrderStatus.CANCELLED,
                    "Request is no longer required.",
                    USER_ID,
                    CANCELLED_AT,
                    7);
        }

        private CancelOrderCommand command() {
            return command.get();
        }
    }
}
