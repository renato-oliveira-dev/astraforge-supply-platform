package io.astraforge.supplyplatform.domain.order.valueobject;

public record ApprovalComment(String value) {

    private static final int MAX_LENGTH = 1000;

    public ApprovalComment {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Approval comment must not be blank");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Approval comment must not exceed 1000 characters");
        }
    }
}
