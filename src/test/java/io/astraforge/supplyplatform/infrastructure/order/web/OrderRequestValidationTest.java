package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestValidationTest {

    private static final UUID USER_ID =
            UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_ID =
            UUID.fromString("40000000-0000-0000-0000-000000000001");

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void testCreateOrderRequestShouldRejectMissingRequiredFields() {
        CreateOrderRequest request = new CreateOrderRequest(
                null,
                null,
                " ");

        Set<ConstraintViolation<CreateOrderRequest>> violations =
                validator.validate(request);

        assertThat(propertyPaths(violations))
                .as("invalid create order request fields")
                .containsExactlyInAnyOrder(
                        "correlationId",
                        "createdBy",
                        "customerId");
    }

    @Test
    void testAddOrderItemRequestShouldRejectInvalidProductContract() {
        AddOrderItemRequest request = new AddOrderItemRequest(
                null,
                " ",
                " ",
                " ",
                BigDecimal.ZERO,
                null,
                " ");

        Set<ConstraintViolation<AddOrderItemRequest>> violations =
                validator.validate(request);

        assertThat(propertyPaths(violations))
                .as("invalid add order item request fields")
                .contains(
                        "addedBy",
                        "correlationId",
                        "productId",
                        "productName",
                        "quantity",
                        "sku",
                        "unitOfMeasure");
    }

    @Test
    void testAddOrderItemRequestShouldRejectExcessQuantityScale() {
        AddOrderItemRequest request = new AddOrderItemRequest(
                PRODUCT_ID,
                "SAFE-HELMET-001",
                "Industrial Safety Helmet",
                "UNIT",
                new BigDecimal("1.0001"),
                USER_ID,
                "correlation-add-item-001");

        assertThat(propertyPaths(validator.validate(request)))
                .as("add item quantity scale violation")
                .containsExactly("quantity");
    }

    @Test
    void testApplyPricingRequestShouldRejectInvalidMonetaryContract() {
        ApplyOrderItemPricingRequest request =
                new ApplyOrderItemPricingRequest(
                        new BigDecimal("-0.01"),
                        "brl",
                        new BigDecimal("100.0001"),
                        new BigDecimal("-0.0001"),
                        null,
                        " ");

        Set<ConstraintViolation<ApplyOrderItemPricingRequest>> violations =
                validator.validate(request);

        assertThat(propertyPaths(violations))
                .as("invalid pricing request fields")
                .contains(
                        "correlationId",
                        "currency",
                        "discountPercentage",
                        "pricedBy",
                        "taxPercentage",
                        "unitPrice");
    }

    @Test
    void testApprovalCommentRequestShouldRejectBlankComment() {
        ApprovalCommentDecisionRequest request =
                new ApprovalCommentDecisionRequest(
                        " ",
                        USER_ID,
                        "correlation-review-001");

        assertThat(propertyPaths(validator.validate(request)))
                .as("blank approval decision comment")
                .containsExactly("comment");
    }

    @Test
    void testCancellationRequestShouldRejectOversizedReason() {
        CancelOrderRequest request = new CancelOrderRequest(
                "x".repeat(501),
                USER_ID,
                "correlation-cancel-001");

        assertThat(propertyPaths(validator.validate(request)))
                .as("oversized cancellation reason")
                .containsExactly("reason");
    }

    @Test
    void testInventoryFailureRequestShouldRejectBlankReason() {
        FailInventoryReservationRequest request =
                new FailInventoryReservationRequest(
                        " ",
                        USER_ID,
                        "correlation-inventory-failure-001");

        assertThat(propertyPaths(validator.validate(request)))
                .as("blank inventory failure reason")
                .containsExactly("reason");
    }

    @Test
    void testQuantityUpdateShouldAcceptValidContract() {
        UpdateOrderItemQuantityRequest request =
                new UpdateOrderItemQuantityRequest(
                        new BigDecimal("10.500"),
                        USER_ID,
                        "correlation-update-quantity-001");

        assertThat(validator.validate(request))
                .as("valid quantity update request")
                .isEmpty();
    }

    @Test
    void testWorkflowRequestsShouldAcceptValidContracts() {
        assertThat(validator.validate(
                new SubmitOrderRequest(
                        USER_ID,
                        "correlation-submit-001")))
                .as("valid submit request")
                .isEmpty();
        assertThat(validator.validate(
                new StartOrderApprovalRequest(
                        USER_ID,
                        "correlation-approval-001")))
                .as("valid start approval request")
                .isEmpty();
        assertThat(validator.validate(
                new StartOrderProcessingRequest(
                        USER_ID,
                        "correlation-processing-001")))
                .as("valid start processing request")
                .isEmpty();
        assertThat(validator.validate(
                new RequestInventoryReservationRequest(
                        USER_ID,
                        "correlation-inventory-001")))
                .as("valid inventory request")
                .isEmpty();
        assertThat(validator.validate(
                new RetryInventoryReservationRequest(
                        USER_ID,
                        "correlation-inventory-retry-001")))
                .as("valid inventory retry request")
                .isEmpty();
    }

    @Test
    void testFulfillmentRequestsShouldAcceptValidContracts() {
        assertThat(validator.validate(
                new PrepareOrderForFulfillmentRequest(
                        USER_ID,
                        "correlation-prepare-001")))
                .as("valid fulfillment preparation request")
                .isEmpty();
        assertThat(validator.validate(
                new StartOrderFulfillmentRequest(
                        USER_ID,
                        "correlation-fulfillment-start-001")))
                .as("valid fulfillment start request")
                .isEmpty();
        assertThat(validator.validate(
                new CompleteOrderFulfillmentRequest(
                        USER_ID,
                        "correlation-fulfillment-complete-001")))
                .as("valid fulfillment completion request")
                .isEmpty();
    }

    private static Set<String> propertyPaths(
            Set<? extends ConstraintViolation<?>> violations
    ) {
        return violations.stream()
                .map(violation -> violation
                        .getPropertyPath()
                        .toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}
