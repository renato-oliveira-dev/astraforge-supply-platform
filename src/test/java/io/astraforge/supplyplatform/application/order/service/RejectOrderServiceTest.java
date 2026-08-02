package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RejectOrderCommand;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.DECIDED_AT;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.repositoryWithPendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RejectOrderServiceTest {

    @Test
    void testRejectShouldPersistRejectedOrderAndComment() {
        RejectOrderService service = new RejectOrderService(
                repositoryWithPendingOrder(),
                Clock.fixed(DECIDED_AT, ZoneOffset.UTC));

        ApprovalDecisionResult result = service.reject(command());

        assertThat(result.status())
                .as("rejected order status")
                .isEqualTo(OrderStatus.REJECTED);
        assertThat(result.comment())
                .as("rejection comment")
                .contains("Budget allocation is unavailable.");
        assertThat(result.version())
                .as("rejected order version")
                .isEqualTo(5L);
    }

    @Test
    void testRejectShouldRejectNullCommand() {
        RejectOrderService service = new RejectOrderService(
                repositoryWithPendingOrder(),
                Clock.fixed(DECIDED_AT, ZoneOffset.UTC));

        assertThatThrownBy(() -> service.reject(null))
                .as("null reject order command")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Reject order command must not be null");
    }

    private static RejectOrderCommand command() {
        return new RejectOrderCommand(
                ORDER_ID,
                " Budget allocation is unavailable. ",
                APPROVER_ID,
                "correlation-reject-001");
    }
}
