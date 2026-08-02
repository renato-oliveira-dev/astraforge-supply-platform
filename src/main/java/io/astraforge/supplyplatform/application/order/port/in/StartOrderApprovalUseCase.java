package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.StartOrderApprovalCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderApprovalResult;

public interface StartOrderApprovalUseCase {

    StartOrderApprovalResult startApproval(
            StartOrderApprovalCommand command);
}
