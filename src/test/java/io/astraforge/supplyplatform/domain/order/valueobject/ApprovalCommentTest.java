package io.astraforge.supplyplatform.domain.order.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalCommentTest {

    @Test
    void testConstructorShouldNormalizeComment() {
        ApprovalComment comment =
                new ApprovalComment("  Confirm facility budget.  ");

        assertThat(comment.value())
                .as("normalized approval comment")
                .isEqualTo("Confirm facility budget.");
    }

    @Test
    void testConstructorShouldRejectBlankComment() {
        assertThatThrownBy(() -> new ApprovalComment("   "))
                .as("blank approval comment")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Approval comment must not be blank");
    }

    @Test
    void testConstructorShouldRejectCommentAboveMaximumLength() {
        String value = "x".repeat(1001);

        assertThatThrownBy(() -> new ApprovalComment(value))
                .as("approval comment maximum length")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Approval comment must not exceed 1000 characters");
    }
}
