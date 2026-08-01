package io.astraforge.supplyplatform.domain.order.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryFailureReasonTest {

    @Test
    void testConstructorShouldNormalizeReason() {
        InventoryFailureReason reason =
                new InventoryFailureReason("  Insufficient stock.  ");

        assertThat(reason.value())
                .as("normalized inventory failure reason")
                .isEqualTo("Insufficient stock.");
    }

    @Test
    void testConstructorShouldRejectBlankReason() {
        assertThatThrownBy(() -> new InventoryFailureReason("   "))
                .as("blank inventory failure reason")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inventory failure reason must not be blank");
    }

    @Test
    void testConstructorShouldRejectReasonAboveMaximumLength() {
        String value = "x".repeat(501);

        assertThatThrownBy(() -> new InventoryFailureReason(value))
                .as("inventory failure reason maximum length")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Inventory failure reason must not exceed 500 characters");
    }
}
