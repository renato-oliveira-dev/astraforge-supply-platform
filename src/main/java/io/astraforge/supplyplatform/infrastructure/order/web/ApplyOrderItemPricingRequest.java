package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyOrderItemPricingRequest(
        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal unitPrice,
        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String currency,
        @NotNull
        @DecimalMin(value = "0.0000")
        @DecimalMax(value = "100.0000")
        @Digits(integer = 3, fraction = 4)
        BigDecimal discountPercentage,
        @NotNull
        @DecimalMin(value = "0.0000")
        @DecimalMax(value = "100.0000")
        @Digits(integer = 3, fraction = 4)
        BigDecimal taxPercentage,
        @NotNull UUID pricedBy,
        @NotBlank @Size(max = 100) String correlationId
) {
}
