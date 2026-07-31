package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;

public record CorrelationId(String value) {

    private static final int MAX_LENGTH = 100;

    public CorrelationId {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Correlation ID must not be blank");
        }

        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new DomainValidationException("Correlation ID must not exceed 100 characters");
        }
    }
}
