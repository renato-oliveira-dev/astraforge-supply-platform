package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RejectOrderCommand;

public interface RejectOrderUseCase {

    ApprovalDecisionResult reject(RejectOrderCommand command);
}
