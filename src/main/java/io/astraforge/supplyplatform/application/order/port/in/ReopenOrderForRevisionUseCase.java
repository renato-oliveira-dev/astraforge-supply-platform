package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionCommand;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionResult;

public interface ReopenOrderForRevisionUseCase {

    ReopenOrderForRevisionResult reopen(
            ReopenOrderForRevisionCommand command);
}
