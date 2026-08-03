package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderFulfillmentResult;

public interface StartOrderFulfillmentUseCase {

    StartOrderFulfillmentResult start(
            StartOrderFulfillmentCommand command);
}
