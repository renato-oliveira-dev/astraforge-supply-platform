package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ApproveOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;

public interface ApproveOrderUseCase {

    ApprovalDecisionResult approve(ApproveOrderCommand command);
}
