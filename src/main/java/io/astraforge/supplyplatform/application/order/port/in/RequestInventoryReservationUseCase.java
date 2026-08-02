package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationCommand;
import io.astraforge.supplyplatform.application.order.usecase.RequestInventoryReservationResult;

public interface RequestInventoryReservationUseCase {

    RequestInventoryReservationResult requestReservation(
            RequestInventoryReservationCommand command);
}
