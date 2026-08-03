package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentCommand;
import io.astraforge.supplyplatform.application.order.usecase.PrepareOrderForFulfillmentResult;

public interface PrepareOrderForFulfillmentUseCase {

    PrepareOrderForFulfillmentResult prepare(
            PrepareOrderForFulfillmentCommand command);
}
