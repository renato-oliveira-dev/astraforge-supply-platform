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
import io.astraforge.supplyplatform.application.order.service.AddOrderItemService;
import io.astraforge.supplyplatform.application.order.service.ApplyOrderItemPricingService;
import io.astraforge.supplyplatform.application.order.service.ApproveOrderService;
import io.astraforge.supplyplatform.application.order.service.CancelOrderService;
import io.astraforge.supplyplatform.application.order.service.CompleteOrderFulfillmentService;
import io.astraforge.supplyplatform.application.order.service.ConfirmInventoryReservationService;
import io.astraforge.supplyplatform.application.order.service.CreateOrderService;
import io.astraforge.supplyplatform.application.order.service.FailInventoryReservationService;
import io.astraforge.supplyplatform.application.order.service.GetOrderDetailsService;
import io.astraforge.supplyplatform.application.order.service.ListOrdersService;
import io.astraforge.supplyplatform.application.order.service.PrepareOrderForFulfillmentService;
import io.astraforge.supplyplatform.application.order.service.RejectOrderService;
import io.astraforge.supplyplatform.application.order.service.RemoveOrderItemService;
import io.astraforge.supplyplatform.application.order.service.ReopenOrderForRevisionService;
import io.astraforge.supplyplatform.application.order.service.RequestInventoryReservationService;
import io.astraforge.supplyplatform.application.order.service.RequestOrderReviewService;
import io.astraforge.supplyplatform.application.order.service.RetryInventoryReservationService;
import io.astraforge.supplyplatform.application.order.service.StartOrderApprovalService;
import io.astraforge.supplyplatform.application.order.service.StartOrderFulfillmentService;
import io.astraforge.supplyplatform.application.order.service.StartOrderProcessingService;
import io.astraforge.supplyplatform.application.order.service.SubmitOrderService;
import io.astraforge.supplyplatform.application.order.service.UpdateOrderItemQuantityService;
import io.astraforge.supplyplatform.infrastructure.order.identity.UuidOrderIdGenerator;
import io.astraforge.supplyplatform.infrastructure.order.identity.UuidOrderItemIdGenerator;
import io.astraforge.supplyplatform.infrastructure.order.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class OrderModuleConfiguration {

    @Bean
    Clock orderClock() {
        return Clock.systemUTC();
    }

    @Bean
    InMemoryOrderRepositoryAdapter inMemoryOrderRepositoryAdapter() {
        return new InMemoryOrderRepositoryAdapter();
    }

    @Bean
    OrderIdGenerator orderIdGenerator() {
        return new UuidOrderIdGenerator();
    }

    @Bean
    OrderItemIdGenerator orderItemIdGenerator() {
        return new UuidOrderItemIdGenerator();
    }

    @Bean
    CreateOrderUseCase createOrderUseCase(
            OrderRepository repository,
            OrderIdGenerator idGenerator,
            Clock orderClock
    ) {
        return new CreateOrderService(repository, idGenerator, orderClock);
    }

    @Bean
    AddOrderItemUseCase addOrderItemUseCase(
            OrderRepository repository,
            OrderItemIdGenerator idGenerator,
            Clock orderClock
    ) {
        return new AddOrderItemService(repository, idGenerator, orderClock);
    }

    @Bean
    UpdateOrderItemQuantityUseCase updateOrderItemQuantityUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new UpdateOrderItemQuantityService(repository, orderClock);
    }

    @Bean
    RemoveOrderItemUseCase removeOrderItemUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new RemoveOrderItemService(repository, orderClock);
    }

    @Bean
    ApplyOrderItemPricingUseCase applyOrderItemPricingUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new ApplyOrderItemPricingService(repository, orderClock);
    }

    @Bean
    SubmitOrderUseCase submitOrderUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new SubmitOrderService(repository, orderClock);
    }

    @Bean
    StartOrderApprovalUseCase startOrderApprovalUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new StartOrderApprovalService(repository, orderClock);
    }

    @Bean
    ApproveOrderUseCase approveOrderUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new ApproveOrderService(repository, orderClock);
    }

    @Bean
    RejectOrderUseCase rejectOrderUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new RejectOrderService(repository, orderClock);
    }

    @Bean
    RequestOrderReviewUseCase requestOrderReviewUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new RequestOrderReviewService(repository, orderClock);
    }

    @Bean
    ReopenOrderForRevisionUseCase reopenOrderForRevisionUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new ReopenOrderForRevisionService(repository, orderClock);
    }

    @Bean
    CancelOrderUseCase cancelOrderUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new CancelOrderService(repository, orderClock);
    }

    @Bean
    StartOrderProcessingUseCase startOrderProcessingUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new StartOrderProcessingService(repository, orderClock);
    }

    @Bean
    RequestInventoryReservationUseCase requestInventoryReservationUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new RequestInventoryReservationService(repository, orderClock);
    }

    @Bean
    ConfirmInventoryReservationUseCase confirmInventoryReservationUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new ConfirmInventoryReservationService(repository, orderClock);
    }

    @Bean
    FailInventoryReservationUseCase failInventoryReservationUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new FailInventoryReservationService(repository, orderClock);
    }

    @Bean
    RetryInventoryReservationUseCase retryInventoryReservationUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new RetryInventoryReservationService(repository, orderClock);
    }

    @Bean
    PrepareOrderForFulfillmentUseCase prepareOrderForFulfillmentUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new PrepareOrderForFulfillmentService(repository, orderClock);
    }

    @Bean
    StartOrderFulfillmentUseCase startOrderFulfillmentUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new StartOrderFulfillmentService(repository, orderClock);
    }

    @Bean
    CompleteOrderFulfillmentUseCase completeOrderFulfillmentUseCase(
            OrderRepository repository,
            Clock orderClock
    ) {
        return new CompleteOrderFulfillmentService(repository, orderClock);
    }

    @Bean
    GetOrderDetailsUseCase getOrderDetailsUseCase(
            OrderRepository repository
    ) {
        return new GetOrderDetailsService(repository);
    }

    @Bean
    ListOrdersUseCase listOrdersUseCase(
            OrderQueryRepository repository
    ) {
        return new ListOrdersService(repository);
    }
}
