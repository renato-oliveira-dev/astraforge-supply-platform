package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.CompleteOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.PrepareOrderForFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentResult;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentResult;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderFulfillmentControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant PREPARED_AT =
            Instant.parse("2026-08-03T15:10:00Z");
    private static final Instant STARTED_AT =
            Instant.parse("2026-08-03T15:20:00Z");
    private static final Instant COMPLETED_AT =
            Instant.parse("2026-08-03T15:30:00Z");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void testPrepareShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<PrepareOrderForFulfillmentResponse> response =
                fixture.controller().prepare(
                        ORDER_ID,
                        new PrepareOrderForFulfillmentRequest(
                                USER_ID,
                                " correlation-prepare-fulfillment-rest-001 "));

        assertThat(fixture.prepareUseCase().command())
                .as("prepare fulfillment command delegated by controller")
                .isEqualTo(new PrepareOrderForFulfillmentCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-prepare-fulfillment-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("prepare fulfillment HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("prepare fulfillment response")
                .isEqualTo(new PrepareOrderForFulfillmentResponse(
                        ORDER_ID,
                        OrderStatus.READY_FOR_FULFILLMENT,
                        USER_ID,
                        PREPARED_AT,
                        1,
                        12));
    }

    @Test
    void testStartShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<StartOrderFulfillmentResponse> response =
                fixture.controller().start(
                        ORDER_ID,
                        new StartOrderFulfillmentRequest(
                                USER_ID,
                                " correlation-start-fulfillment-rest-001 "));

        assertThat(fixture.startUseCase().command())
                .as("start fulfillment command delegated by controller")
                .isEqualTo(new StartOrderFulfillmentCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-start-fulfillment-rest-001"));
        assertThat(response.getBody())
                .as("start fulfillment response")
                .isEqualTo(new StartOrderFulfillmentResponse(
                        ORDER_ID,
                        OrderStatus.FULFILLMENT_IN_PROGRESS,
                        USER_ID,
                        STARTED_AT,
                        13));
    }

    @Test
    void testCompleteShouldDelegateCommandAndReturnOk() {
        Fixture fixture = new Fixture();

        ResponseEntity<CompleteOrderFulfillmentResponse> response =
                fixture.controller().complete(
                        ORDER_ID,
                        new CompleteOrderFulfillmentRequest(
                                USER_ID,
                                " correlation-complete-fulfillment-rest-001 "));

        assertThat(fixture.completeUseCase().command())
                .as("complete fulfillment command delegated by controller")
                .isEqualTo(new CompleteOrderFulfillmentCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-complete-fulfillment-rest-001"));
        assertThat(response.getBody())
                .as("complete fulfillment response")
                .isEqualTo(new CompleteOrderFulfillmentResponse(
                        ORDER_ID,
                        OrderStatus.COMPLETED,
                        USER_ID,
                        COMPLETED_AT,
                        1,
                        new BigDecimal("216.00"),
                        BRL,
                        14));
    }

    @Test
    void testConstructorShouldRejectNullDependencies() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> new OrderFulfillmentController(
                null,
                fixture.startUseCase(),
                fixture.completeUseCase()))
                .as("null prepare fulfillment use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Prepare order for fulfillment use case must not be null");
        assertThatThrownBy(() -> new OrderFulfillmentController(
                fixture.prepareUseCase(),
                null,
                fixture.completeUseCase()))
                .as("null start fulfillment use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Start order fulfillment use case must not be null");
        assertThatThrownBy(() -> new OrderFulfillmentController(
                fixture.prepareUseCase(),
                fixture.startUseCase(),
                null))
                .as("null complete fulfillment use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Complete order fulfillment use case must not be null");
    }

    private record Fixture(
            CapturingPrepareUseCase prepareUseCase,
            CapturingStartUseCase startUseCase,
            CapturingCompleteUseCase completeUseCase,
            OrderFulfillmentController controller
    ) {

        private Fixture() {
            this(
                    new CapturingPrepareUseCase(),
                    new CapturingStartUseCase(),
                    new CapturingCompleteUseCase());
        }

        private Fixture(
                CapturingPrepareUseCase prepareUseCase,
                CapturingStartUseCase startUseCase,
                CapturingCompleteUseCase completeUseCase
        ) {
            this(
                    prepareUseCase,
                    startUseCase,
                    completeUseCase,
                    new OrderFulfillmentController(
                            prepareUseCase,
                            startUseCase,
                            completeUseCase));
        }
    }

    private static final class CapturingPrepareUseCase
            implements PrepareOrderForFulfillmentUseCase {

        private final AtomicReference<PrepareOrderForFulfillmentCommand>
                command = new AtomicReference<>();

        @Override
        public PrepareOrderForFulfillmentResult prepare(
                PrepareOrderForFulfillmentCommand command
        ) {
            this.command.set(command);
            return new PrepareOrderForFulfillmentResult(
                    ORDER_ID,
                    OrderStatus.READY_FOR_FULFILLMENT,
                    USER_ID,
                    PREPARED_AT,
                    1,
                    12);
        }

        private PrepareOrderForFulfillmentCommand command() {
            return command.get();
        }
    }

    private static final class CapturingStartUseCase
            implements StartOrderFulfillmentUseCase {

        private final AtomicReference<StartOrderFulfillmentCommand> command =
                new AtomicReference<>();

        @Override
        public StartOrderFulfillmentResult start(
                StartOrderFulfillmentCommand command
        ) {
            this.command.set(command);
            return new StartOrderFulfillmentResult(
                    ORDER_ID,
                    OrderStatus.FULFILLMENT_IN_PROGRESS,
                    USER_ID,
                    STARTED_AT,
                    13);
        }

        private StartOrderFulfillmentCommand command() {
            return command.get();
        }
    }

    private static final class CapturingCompleteUseCase
            implements CompleteOrderFulfillmentUseCase {

        private final AtomicReference<CompleteOrderFulfillmentCommand> command =
                new AtomicReference<>();

        @Override
        public CompleteOrderFulfillmentResult complete(
                CompleteOrderFulfillmentCommand command
        ) {
            this.command.set(command);
            return new CompleteOrderFulfillmentResult(
                    ORDER_ID,
                    OrderStatus.COMPLETED,
                    USER_ID,
                    COMPLETED_AT,
                    1,
                    new BigDecimal("216.00"),
                    BRL,
                    14);
        }

        private CompleteOrderFulfillmentCommand command() {
            return command.get();
        }
    }
}
