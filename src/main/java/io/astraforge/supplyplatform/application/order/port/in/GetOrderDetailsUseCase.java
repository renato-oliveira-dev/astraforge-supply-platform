package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.GetOrderDetailsQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderDetailsResult;

public interface GetOrderDetailsUseCase {

    OrderDetailsResult getDetails(GetOrderDetailsQuery query);
}
