package io.astraforge.supplyplatform.domain.order.valueobject;

import io.astraforge.supplyplatform.domain.order.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSnapshotTest {

    private static final ProductId PRODUCT_ID =
            new ProductId(UUID.fromString("40000000-0000-0000-0000-000000000004"));

    @Test
    void testCreateShouldNormalizeRequiredText() {
        ProductSnapshot snapshot = new ProductSnapshot(
                new ProductReference(PRODUCT_ID),
                " SKU-001 ",
                " Industrial Pump ",
                " UNIT ");

        assertThat(snapshot.sku())
                .as("normalized product SKU")
                .isEqualTo("SKU-001");
        assertThat(snapshot.name())
                .as("normalized product name")
                .isEqualTo("Industrial Pump");
        assertThat(snapshot.unitOfMeasure())
                .as("normalized unit of measure")
                .isEqualTo("UNIT");
        assertThat(snapshot.productId())
                .as("product identifier exposed by the snapshot")
                .isEqualTo(PRODUCT_ID);
    }

    @Test
    void testCreateShouldRejectBlankSku() {
        ProductReference reference = new ProductReference(PRODUCT_ID);

        assertThatThrownBy(() -> new ProductSnapshot(reference, " ", "Pump", "UNIT"))
                .as("blank product SKU")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Product SKU must not be blank");
    }

    @Test
    void testCreateShouldRejectNameAboveMaximumLength() {
        ProductReference reference = new ProductReference(PRODUCT_ID);
        String oversizedName = "N".repeat(201);

        assertThatThrownBy(() -> new ProductSnapshot(reference, "SKU", oversizedName, "UNIT"))
                .as("product name above its maximum length")
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Product name must not exceed 200 characters");
    }
}
