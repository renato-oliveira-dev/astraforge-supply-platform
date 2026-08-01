package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.AddOrderItemResult;

public interface AddOrderItemUseCase {

    AddOrderItemResult addItem(AddOrderItemCommand command);
}
