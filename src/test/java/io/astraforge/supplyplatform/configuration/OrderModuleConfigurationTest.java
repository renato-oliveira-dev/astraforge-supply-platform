package io.astraforge.supplyplatform.configuration;

import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ApplyOrderItemPricingUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ApproveOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CancelOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CompleteOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ConfirmInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.FailInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.in.GetOrderDetailsUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ListOrdersUseCase;
import io.astraforge.supplyplatform.application.order.port.in.PrepareOrderForFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RejectOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RemoveOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ReopenOrderForRevisionUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RequestInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RequestOrderReviewUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RetryInventoryReservationUseCase;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderApprovalUseCase;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderFulfillmentUseCase;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderProcessingUseCase;
import io.astraforge.supplyplatform.application.order.port.in.SubmitOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.UpdateOrderItemQuantityUseCase;
import io.astraforge.supplyplatform.application.order.port.out.OrderIdGenerator;
import io.astraforge.supplyplatform.application.order.port.out.OrderItemIdGenerator;
import io.astraforge.supplyplatform.application.order.port.out.OrderQueryRepository;
import io.astraforge.supplyplatform.application.order.port.out.OrderRepository;
import io.astraforge.supplyplatform.infrastructure.order.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderModuleConfigurationTest {

    @Test
    void testConfigurationShouldExposeCompleteOrderModule() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             OrderModuleConfiguration.class)) {

            assertThat(context.getBean(Clock.class))
                    .as("order module clock")
                    .isNotNull();
            assertThat(context.getBean(OrderRepository.class))
                    .as("order repository port")
                    .isSameAs(context.getBean(
                            InMemoryOrderRepositoryAdapter.class));
            assertThat(context.getBean(OrderQueryRepository.class))
                    .as("order query repository port")
                    .isSameAs(context.getBean(
                            InMemoryOrderRepositoryAdapter.class));
            assertThat(context.getBean(OrderIdGenerator.class))
                    .as("order identifier generator")
                    .isNotNull();
            assertThat(context.getBean(OrderItemIdGenerator.class))
                    .as("order item identifier generator")
                    .isNotNull();
            assertThat(useCaseTypes())
                    .as("configured order use case types")
                    .allSatisfy(type -> assertThat(context.getBean(type))
                            .as("configured use case " + type.getSimpleName())
                            .isNotNull());
        }
    }

    private static List<Class<?>> useCaseTypes() {
        return List.of(
                CreateOrderUseCase.class,
                AddOrderItemUseCase.class,
                UpdateOrderItemQuantityUseCase.class,
                RemoveOrderItemUseCase.class,
                ApplyOrderItemPricingUseCase.class,
                SubmitOrderUseCase.class,
                StartOrderApprovalUseCase.class,
                ApproveOrderUseCase.class,
                RejectOrderUseCase.class,
                RequestOrderReviewUseCase.class,
                ReopenOrderForRevisionUseCase.class,
                CancelOrderUseCase.class,
                StartOrderProcessingUseCase.class,
                RequestInventoryReservationUseCase.class,
                ConfirmInventoryReservationUseCase.class,
                FailInventoryReservationUseCase.class,
                RetryInventoryReservationUseCase.class,
                PrepareOrderForFulfillmentUseCase.class,
                StartOrderFulfillmentUseCase.class,
                CompleteOrderFulfillmentUseCase.class,
                GetOrderDetailsUseCase.class,
                ListOrdersUseCase.class);
    }
}
