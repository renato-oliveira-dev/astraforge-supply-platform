package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateOrderItemQuantityRequest(
        @NotNull
        @DecimalMin(value = "0.001")
        @Digits(integer = 12, fraction = 3)
        BigDecimal quantity,
        @NotNull UUID changedBy,
        @NotBlank @Size(max = 100) String correlationId
) {
}
