package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.ReopenOrderForRevisionUseCase;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionCommand;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderRevisionControllerTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant REOPENED_AT =
            Instant.parse("2026-08-03T14:10:00Z");

    @Test
    void testReopenShouldDelegateCommandAndReturnOk() {
        CapturingReopenUseCase useCase = new CapturingReopenUseCase();
        OrderRevisionController controller =
                new OrderRevisionController(useCase);

        ResponseEntity<ReopenOrderForRevisionResponse> response =
                controller.reopen(
                        ORDER_ID,
                        new ReopenOrderForRevisionRequest(
                                USER_ID,
                                " correlation-reopen-revision-rest-001 "));

        assertThat(useCase.command())
                .as("reopen revision command delegated by controller")
                .isEqualTo(new ReopenOrderForRevisionCommand(
                        ORDER_ID,
                        USER_ID,
                        "correlation-reopen-revision-rest-001"));
        assertThat(response.getStatusCode().value())
                .as("reopen revision HTTP status")
                .isEqualTo(200);
        assertThat(response.getBody())
                .as("reopen revision response")
                .isEqualTo(new ReopenOrderForRevisionResponse(
                        ORDER_ID,
                        OrderStatus.DRAFT,
                        6,
                        REOPENED_AT));
    }

    @Test
    void testConstructorShouldRejectNullUseCase() {
        assertThatThrownBy(() -> new OrderRevisionController(null))
                .as("null reopen revision use case")
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Reopen order for revision use case must not be null");
    }

    private static final class CapturingReopenUseCase
            implements ReopenOrderForRevisionUseCase {

        private final AtomicReference<ReopenOrderForRevisionCommand> command =
                new AtomicReference<>();

        @Override
        public ReopenOrderForRevisionResult reopen(
                ReopenOrderForRevisionCommand command
        ) {
            this.command.set(command);
            return new ReopenOrderForRevisionResult(
                    ORDER_ID,
                    OrderStatus.DRAFT,
                    6,
                    REOPENED_AT);
        }

        private ReopenOrderForRevisionCommand command() {
            return command.get();
        }
    }
}
