package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.ConfirmInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.in.FailInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.usecase.ConfirmInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.FailInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryReservationOutcomeControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant RECORDED_AT =
            Instant.parse("2026-08-03T14:50:00Z");

    @Test
    void testConfirmShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<InventoryReservationOutcomeResponse> response =
                fixture.controller().confirm(
                        ORDER_ID,
                        new ConfirmInventoryReservationRequest(
                                USER_ID,
                                " correlation-confirm-inventory-rest-001 "));

        assertThat(fixture.confirmUseCase().command())
                .as("confirm inventory command delegated by controller")
                .isEqualTo(new ConfirmInventoryReservationCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-confirm-inventory-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("confirm inventory HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("confirm inventory response")
                .isEqualTo(new InventoryReservationOutcomeResponse(
                        ORDER_ID,
                        OrderStatus.INVENTORY_RESERVED,
                        USER_ID,
                        RECORDED_AT,
                        Optional.empty(),
                        10));
    }

    @Test
    void testFailShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<InventoryReservationOutcomeResponse> response =
                fixture.controller().fail(
                        ORDER_ID,
                        new FailInventoryReservationRequest(
                                " Insufficient stock at eligible facilities. ",
                                USER_ID,
                                " correlation-fail-inventory-rest-001 "));

        assertThat(fixture.failUseCase().command())
                .as("fail inventory command delegated by controller")
                .isEqualTo(new FailInventoryReservationCommand(
                        ORDER_ID,
                        "Insufficient stock at eligible facilities.",
                        USER_ID,
                        "correlation-fail-inventory-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("fail inventory HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("fail inventory response")
                .isEqualTo(new InventoryReservationOutcomeResponse(
                        ORDER_ID,
                        OrderStatus.INVENTORY_FAILED,
                        USER_ID,
                        RECORDED_AT,
                        Optional.of(
                                "Insufficient stock at eligible facilities."),
                        10));
    }

    @Test
    void testConstructorShouldRejectNullDependencies() {
        Fixture fixture = new Fixture();

        var sonarArgument1Value2 = fixture.failUseCase();
        assertThatThrownBy(() -> new InventoryReservationOutcomeController(null, sonarArgument1Value2))
                .as("null confirm inventory use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Confirm inventory reservation use case must not be null");
        var sonarArgument2Value1 = fixture.confirmUseCase();
        assertThatThrownBy(() -> new InventoryReservationOutcomeController(sonarArgument2Value1, null))
                .as("null fail inventory use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Fail inventory reservation use case must not be null");
    }

    private record Fixture(
            CapturingConfirmUseCase confirmUseCase,
            CapturingFailUseCase failUseCase,
            InventoryReservationOutcomeController controller
    ) {

        private Fixture() {
            this(
                    new CapturingConfirmUseCase(),
                    new CapturingFailUseCase());
        }

        private Fixture(
                CapturingConfirmUseCase confirmUseCase,
                CapturingFailUseCase failUseCase
        ) {
            this(
                    confirmUseCase,
                    failUseCase,
                    new InventoryReservationOutcomeController(
                            confirmUseCase,
                            failUseCase));
        }
    }

    private static final class CapturingConfirmUseCase
            implements ConfirmInventoryReservationUseCase {

        private final AtomicReference<ConfirmInventoryReservationCommand>
                command = new AtomicReference<>();

        @Override
        public InventoryReservationOutcomeResult confirm(
                ConfirmInventoryReservationCommand command
        ) {
            this.command.set(command);
            return new InventoryReservationOutcomeResult(
                    ORDER_ID,
                    OrderStatus.INVENTORY_RESERVED,
                    USER_ID,
                    RECORDED_AT,
                    Optional.empty(),
                    10);
        }

        private ConfirmInventoryReservationCommand command() {
            return command.get();
        }
    }

    private static final class CapturingFailUseCase
            implements FailInventoryReservationUseCase {

        private final AtomicReference<FailInventoryReservationCommand> command =
                new AtomicReference<>();

        @Override
        public InventoryReservationOutcomeResult fail(
                FailInventoryReservationCommand command
        ) {
            this.command.set(command);
            return new InventoryReservationOutcomeResult(
                    ORDER_ID,
                    OrderStatus.INVENTORY_FAILED,
                    USER_ID,
                    RECORDED_AT,
                    Optional.of(
                            "Insufficient stock at eligible facilities."),
                    10);
        }

        private FailInventoryReservationCommand command() {
            return command.get();
        }
    }
}
