package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.SubmitOrderResult;

public interface SubmitOrderUseCase {

    SubmitOrderResult submit(SubmitOrderCommand command);
}
