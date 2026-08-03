package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.StartOrderProcessingUseCase;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderProcessingControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant STARTED_AT =
            Instant.parse("2026-08-03T14:30:00Z");

    @Test
    void testStartShouldDelegateCommandAndReturnOk() {
        CapturingStartProcessingUseCase useCase =
                new CapturingStartProcessingUseCase();
        OrderProcessingController controller =
                new OrderProcessingController(useCase);

        ResponseEntity<StartOrderProcessingResponse> response =
                controller.start(
                        ORDER_ID,
                        new StartOrderProcessingRequest(
                                USER_ID,
                                " correlation-start-processing-rest-001 "));

        assertThat(useCase.command())
                .as("start processing command delegated by controller")
                .isEqualTo(new StartOrderProcessingCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-start-processing-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("start processing HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("start processing response")
                .isEqualTo(new StartOrderProcessingResponse(
                        ORDER_ID,
                        OrderStatus.PROCESSING,
                        USER_ID,
                        STARTED_AT,
                        8));
    }

    @Test
    void testConstructorShouldRejectNullUseCase() {
        assertThatThrownBy(() -> new OrderProcessingController(null))
                .as("null start processing use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Start order processing use case must not be null");
    }

    private static final class CapturingStartProcessingUseCase
            implements StartOrderProcessingUseCase {

        private final AtomicReference<StartOrderProcessingCommand> command =
                new AtomicReference<>();

        @Override
        public StartOrderProcessingResult startProcessing(
                StartOrderProcessingCommand command
        ) {
            this.command.set(command);
            return new StartOrderProcessingResult(
                    ORDER_ID,
                    OrderStatus.PROCESSING,
                    USER_ID,
                    STARTED_AT,
                    8);
        }

        private StartOrderProcessingCommand command() {
            return command.get();
        }
    }
}
