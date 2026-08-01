package io.astraforge.supplyplatform.domain.order.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CancellationReasonTest {

    @Test
    void testConstructorShouldNormalizeReason() {
        CancellationReason reason =
                new CancellationReason("  Request was withdrawn.  ");

        assertThat(reason.value())
                .as("normalized cancellation reason")
                .isEqualTo("Request was withdrawn.");
    }

    @Test
    void testConstructorShouldRejectBlankReason() {
        assertThatThrownBy(() -> new CancellationReason("   "))
                .as("blank cancellation reason")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cancellation reason must not be blank");
    }

    @Test
    void testConstructorShouldRejectReasonAboveMaximumLength() {
        String value = "x".repeat(501);

        assertThatThrownBy(() -> new CancellationReason(value))
                .as("cancellation reason maximum length")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Cancellation reason must not exceed 500 characters");
    }
}
