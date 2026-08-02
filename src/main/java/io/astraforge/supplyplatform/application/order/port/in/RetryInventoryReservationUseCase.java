package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RetryInventoryReservationResult;

public interface RetryInventoryReservationUseCase {

    RetryInventoryReservationResult retry(
            RetryInventoryReservationCommand command);
}
