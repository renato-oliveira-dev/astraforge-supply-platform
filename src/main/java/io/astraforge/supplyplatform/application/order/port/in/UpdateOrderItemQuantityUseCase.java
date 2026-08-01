package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityCommand;
import io.astraforge.supplyplatform.application.order.usecase.UpdateOrderItemQuantityResult;

public interface UpdateOrderItemQuantityUseCase {

    UpdateOrderItemQuantityResult updateQuantity(
            UpdateOrderItemQuantityCommand command);
}
