package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.exception.OrderNotFoundException;
import io.astraforge.supplyplatform.application.order.port.in.AddOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ApplyOrderItemPricingUseCase;
import io.astraforge.supplyplatform.application.order.port.in.CreateOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.GetOrderDetailsUseCase;
import io.astraforge.supplyplatform.application.order.port.in.ListOrdersUseCase;
import io.astraforge.supplyplatform.application.order.port.in.RemoveOrderItemUseCase;
import io.astraforge.supplyplatform.application.order.port.in.StartOrderApprovalUseCase;
import io.astraforge.supplyplatform.application.order.port.in.SubmitOrderUseCase;
import io.astraforge.supplyplatform.application.order.port.in.UpdateOrderItemQuantityUseCase;
import io.astraforge.supplyplatform.application.order.usecase.CreateOrderResult;
import io.astraforge.supplyplatform.domain.order.aggregate.OrderStatus;
import io.astraforge.supplyplatform.domain.order.exception.OrderSubmissionNotAllowedException;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import io.astraforge.supplyplatform.infrastructure.order.web.error.GlobalApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class OrderApiIntegrationTest {

    private static final UUID ORDER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_ID =
            UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final Instant NOW =
            Instant.parse("2026-08-03T23:10:00Z");

    private CreateOrderUseCase createOrderUseCase;
    private SubmitOrderUseCase submitOrderUseCase;
    private GetOrderDetailsUseCase getOrderDetailsUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        createOrderUseCase = mock(CreateOrderUseCase.class);
        submitOrderUseCase = mock(SubmitOrderUseCase.class);
        getOrderDetailsUseCase = mock(GetOrderDetailsUseCase.class);

        OrderController commandController = new OrderController(
                createOrderUseCase,
                mock(AddOrderItemUseCase.class),
                mock(UpdateOrderItemQuantityUseCase.class),
                mock(RemoveOrderItemUseCase.class),
                mock(ApplyOrderItemPricingUseCase.class),
                submitOrderUseCase,
                mock(StartOrderApprovalUseCase.class));

        OrderQueryController queryController = new OrderQueryController(
                getOrderDetailsUseCase,
                mock(ListOrdersUseCase.class));

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = standaloneSetup(commandController, queryController)
                .setControllerAdvice(new GlobalApiExceptionHandler(
                        Clock.fixed(NOW, ZoneOffset.UTC)))
                .setValidator(validator)
                .build();
    }

    @Test
    void testCreateShouldReturnCreatedContract() throws Exception {
        when(createOrderUseCase.create(any()))
                .thenReturn(new CreateOrderResult(
                        ORDER_ID,
                        OrderStatus.DRAFT,
                        0,
                        NOW));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerId": "%s",
                                  "createdBy": "%s",
                                  "correlationId": "correlation-create-001"
                                }
                                """.formatted(CUSTOMER_ID, USER_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/orders/" + ORDER_ID))
                .andExpect(jsonPath("$.orderId")
                        .value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.createdAt")
                        .value(NOW.toString()));
    }

    @Test
    void testCreateShouldReturnStructuredBeanValidationErrors()
            throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerId": null,
                                  "createdBy": null,
                                  "correlationId": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp")
                        .value(NOW.toString()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid request"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/orders"))
                .andExpect(jsonPath("$.fieldErrors.length()")
                        .value(3))
                .andExpect(jsonPath("$.fieldErrors[0].field")
                        .value("correlationId"))
                .andExpect(jsonPath("$.fieldErrors[1].field")
                        .value("createdBy"))
                .andExpect(jsonPath("$.fieldErrors[2].field")
                        .value("customerId"));
    }

    @Test
    void testDetailsShouldReturnStandardizedNotFoundError()
            throws Exception {
        when(getOrderDetailsUseCase.getDetails(any()))
                .thenThrow(new OrderNotFoundException(
                        new OrderId(ORDER_ID)));

        mockMvc.perform(get("/api/v1/orders/{orderId}", ORDER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp")
                        .value(NOW.toString()))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Order not found: " + ORDER_ID))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/orders/" + ORDER_ID))
                .andExpect(jsonPath("$.fieldErrors.length()")
                        .value(0));
    }

    @Test
    void testSubmitShouldReturnDomainStateViolationAs422()
            throws Exception {
        when(submitOrderUseCase.submit(any()))
                .thenThrow(new OrderSubmissionNotAllowedException(
                        "Only a DRAFT order can be submitted"));

        mockMvc.perform(post(
                        "/api/v1/orders/{orderId}/submission",
                        ORDER_ID)
                        .contentType("application/json")
                        .content("""
                                {
                                  "submittedBy": "%s",
                                  "correlationId": "correlation-submit-001"
                                }
                                """.formatted(USER_ID)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error")
                        .value("Unprocessable Content"))
                .andExpect(jsonPath("$.message")
                        .value("Only a DRAFT order can be submitted"))
                .andExpect(jsonPath("$.fieldErrors.length()")
                        .value(0));
    }

    @Test
    void testDetailsShouldReturnBadRequestForMalformedOrderId()
            throws Exception {
        mockMvc.perform(get("/api/v1/orders/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid request"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/orders/not-a-uuid"));
    }
}
