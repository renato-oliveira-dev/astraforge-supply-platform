package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.CompleteOrderFulfillmentResult;

public interface CompleteOrderFulfillmentUseCase {

    CompleteOrderFulfillmentResult complete(
            CompleteOrderFulfillmentCommand command);
}
