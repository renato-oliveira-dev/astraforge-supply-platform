package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.FailInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;

public interface FailInventoryReservationUseCase {

    InventoryReservationOutcomeResult fail(
            FailInventoryReservationCommand command);
}
