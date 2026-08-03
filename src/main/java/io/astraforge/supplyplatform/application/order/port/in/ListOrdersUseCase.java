package io.astraforge.supplyplatform.application.order.port.in;

import io.astraforge.supplyplatform.application.order.usecase.ListOrdersQuery;
import io.astraforge.supplyplatform.application.order.usecase.OrderPageResult;

public interface ListOrdersUseCase {

    OrderPageResult list(ListOrdersQuery query);
}
