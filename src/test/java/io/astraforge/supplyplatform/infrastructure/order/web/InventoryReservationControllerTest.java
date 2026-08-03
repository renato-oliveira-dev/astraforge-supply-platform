package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.RequestInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryReservationControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant REQUESTED_AT =
            Instant.parse("2026-08-03T14:40:00Z");

    @Test
    void testRequestShouldDelegateCommandAndReturnOk() {
        CapturingRequestReservationUseCase useCase =
                new CapturingRequestReservationUseCase();
        InventoryReservationController controller =
                new InventoryReservationController(useCase);

        ResponseEntity<RequestInventoryReservationResponse> response =
                controller.request(
                        ORDER_ID,
                        new RequestInventoryReservationRequest(
                                USER_ID,
                                " correlation-request-inventory-rest-001 "));

        assertThat(useCase.command())
                .as("inventory reservation command delegated by controller")
                .isEqualTo(new RequestInventoryReservationCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-request-inventory-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("inventory reservation HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("inventory reservation response")
                .isEqualTo(new RequestInventoryReservationResponse(
                        ORDER_ID,
                        OrderStatus.INVENTORY_PENDING,
                        USER_ID,
                        REQUESTED_AT,
                        1,
                        9));
    }

    @Test
    void testConstructorShouldRejectNullUseCase() {
        assertThatThrownBy(() -> new InventoryReservationController(null))
                .as("null request inventory reservation use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Request inventory reservation use case must not be null");
    }

    private static final class CapturingRequestReservationUseCase
            implements RequestInventoryReservationUseCase {

        private final AtomicReference<RequestInventoryReservationCommand>
                command = new AtomicReference<>();

        @Override
        public RequestInventoryReservationResult requestReservation(
                RequestInventoryReservationCommand command
        ) {
            this.command.set(command);
            return new RequestInventoryReservationResult(
                    ORDER_ID,
                    OrderStatus.INVENTORY_PENDING,
                    USER_ID,
                    REQUESTED_AT,
                    1,
                    9);
        }

        private RequestInventoryReservationCommand command() {
            return command.get();
        }
    }
}
