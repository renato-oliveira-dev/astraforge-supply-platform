package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemCommand;
import io.astraforge.supplyplatform.application.order.usecase.RemoveOrderItemResult;

public interface RemoveOrderItemUseCase {

    RemoveOrderItemResult removeItem(RemoveOrderItemCommand command);
}
