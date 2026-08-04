package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.RetryInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryReservationRetryControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant REQUESTED_AT =
            Instant.parse("2026-08-03T15:00:00Z");

    @Test
    void testRetryShouldDelegateCommandAndReturnOk() {
        CapturingRetryUseCase useCase = new CapturingRetryUseCase();
        InventoryReservationRetryController controller =
                new InventoryReservationRetryController(useCase);

        ResponseEntity<RetryInventoryReservationResponse> response =
                controller.retry(
                        ORDER_ID,
                        new RetryInventoryReservationRequest(
                                USER_ID,
                                " correlation-retry-inventory-rest-001 "));

        assertThat(useCase.command())
                .as("retry inventory command delegated by controller")
                .isEqualTo(new RetryInventoryReservationCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-retry-inventory-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("retry inventory HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("retry inventory response")
                .isEqualTo(new RetryInventoryReservationResponse(
                        ORDER_ID,
                        OrderStatus.INVENTORY_PENDING,
                        USER_ID,
                        REQUESTED_AT,
                        11));
    }

    @Test
    void testConstructorShouldRejectNullUseCase() {
        assertThatThrownBy(() ->
                new InventoryReservationRetryController(null))
                .as("null retry inventory reservation use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Retry inventory reservation use case must not be null");
    }

    private static final class CapturingRetryUseCase
            implements RetryInventoryReservationUseCase {

        private final AtomicReference<RetryInventoryReservationCommand>
                command = new AtomicReference<>();

        @Override
        public RetryInventoryReservationResult retry(
                RetryInventoryReservationCommand command
        ) {
            this.command.set(command);
            return new RetryInventoryReservationResult(
                    ORDER_ID,
                    OrderStatus.INVENTORY_PENDING,
                    USER_ID,
                    REQUESTED_AT,
                    11);
        }

        private RetryInventoryReservationCommand command() {
            return command.get();
        }
    }
}
