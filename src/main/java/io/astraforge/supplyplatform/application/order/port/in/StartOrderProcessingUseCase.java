package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingCommand;
import io.astraforge.supplyplatform.application.order.usecase.StartOrderProcessingResult;

public interface StartOrderProcessingUseCase {

    StartOrderProcessingResult startProcessing(
            StartOrderProcessingCommand command);
}
