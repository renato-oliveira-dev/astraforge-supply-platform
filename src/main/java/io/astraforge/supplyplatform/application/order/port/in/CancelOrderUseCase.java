package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.CancelOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CancelOrderResult;

public interface CancelOrderUseCase {

    CancelOrderResult cancel(CancelOrderCommand command);
}
