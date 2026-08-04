package io.astraforge.supplyplatform.infrastructure.order.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StartOrderFulfillmentRequest(
        @NotNull UUID startedBy,
        @NotBlank @Size(max = 100) String correlationId
) {
}
