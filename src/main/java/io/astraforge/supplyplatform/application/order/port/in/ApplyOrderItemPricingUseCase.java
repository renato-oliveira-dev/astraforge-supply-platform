package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingCommand;
import io.astraforge.supplyplatform.application.order.usecase.ApplyOrderItemPricingResult;

public interface ApplyOrderItemPricingUseCase {

    ApplyOrderItemPricingResult applyPricing(
            ApplyOrderItemPricingCommand command);
}
