package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ApprovalDecisionResult;
import io.astraforge.supplyplatform.application.order.usecase.RequestOrderReviewCommand;

public interface RequestOrderReviewUseCase {

    ApprovalDecisionResult requestReview(
            RequestOrderReviewCommand command);
}
