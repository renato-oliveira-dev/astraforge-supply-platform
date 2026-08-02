package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ConfirmInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.InventoryReservationOutcomeResult;

public interface ConfirmInventoryReservationUseCase {

    InventoryReservationOutcomeResult confirm(
            ConfirmInventoryReservationCommand command);
}
