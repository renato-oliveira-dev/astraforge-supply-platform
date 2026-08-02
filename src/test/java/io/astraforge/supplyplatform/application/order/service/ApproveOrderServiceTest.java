package io.astraforge.supplyplatform.application.order.service;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.usecase.ApproveOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderApprovalNotAllowedException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.APPROVER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.DECIDED_AT;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.ORDER_ID;
import static io.astraforge.supplyplatform.application.order.service.ApprovalDecisionTestFixture.repositoryWithPendingOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApproveOrderServiceTest {

    @Test
    void testApproveShouldPersistApprovedOrder() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithPendingOrder();
        ApproveOrderService service = new ApproveOrderService(
                repository,
                Clock.fixed(DECIDED_AT, ZoneOffset.UTC));

        ApprovalDecisionResult result = service.approve(command());

        assertThat(result.status())
                .as("approved order status")
                .isEqualTo(OrderStatus.APPROVED);
        assertThat(result.decidedBy())
                .as("approval decision actor")
                .isEqualTo(APPROVER_ID);
        assertThat(result.decidedAt())
                .as("approval decision timestamp")
                .isEqualTo(DECIDED_AT);
        assertThat(result.comment())
                .as("approval comment")
                .isEqualTo(Optional.empty());
        assertThat(result.version())
                .as("approved order version")
                .isEqualTo(5L);
    }

    @Test
    void testApproveShouldRejectUnknownOrder() {
        ApproveOrderService service = new ApproveOrderService(
                new ApprovalDecisionTestFixture.InMemoryOrderRepository(),
                Clock.fixed(DECIDED_AT, ZoneOffset.UTC));

        assertThatThrownBy(() -> service.approve(command()))
                .as("approval for unknown order")
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found: " + ORDER_ID);
    }

    @Test
    void testApproveShouldRejectOrderOutsidePendingApproval() {
        ApprovalDecisionTestFixture.InMemoryOrderRepository repository =
                repositoryWithPendingOrder();
        ApproveOrderService service = new ApproveOrderService(
                repository,
                Clock.fixed(DECIDED_AT, ZoneOffset.UTC));
        service.approve(command());

        assertThatThrownBy(() -> service.approve(command()))
                .as("order cannot be approved twice")
                .isInstanceOf(OrderApprovalNotAllowedException.class)
                .hasMessage(
                        "Only a PENDING_APPROVAL order can receive an approval decision");
    }

    private static ApproveOrderCommand command() {
        return new ApproveOrderCommand(
                ORDER_ID,
                APPROVER_ID,
                "correlation-approve-001");
    }
}
