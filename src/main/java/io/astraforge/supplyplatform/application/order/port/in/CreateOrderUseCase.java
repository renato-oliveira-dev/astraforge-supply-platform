package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.CreateOrderCommand;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;

public interface CreateOrderUseCase {

    CreateOrderResult create(CreateOrderCommand command);
}
