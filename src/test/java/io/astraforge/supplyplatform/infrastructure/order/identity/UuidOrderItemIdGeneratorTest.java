package io.astraforge.supplyplatform.infrastructure.order.identity;

import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UuidOrderItemIdGeneratorTest {

    private static final UUID EXPECTED_ID =
            UUID.fromString(
                    "11000000-0000-0000-0000-000000000001");

    @Test
    void testNextIdShouldWrapGeneratedUuid() {
        UuidOrderItemIdGenerator generator =
                new UuidOrderItemIdGenerator(() -> EXPECTED_ID);

        OrderItemId result = generator.nextId();

        assertThat(result.value())
                .as("generated order item identifier")
                .isEqualTo(EXPECTED_ID);
    }

    @Test
    void testConstructorShouldRejectNullSupplier() {
        assertThatThrownBy(() ->
                new UuidOrderItemIdGenerator(null))
                .as("null order item UUID supplier")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UUID supplier must not be null");
    }

    @Test
    void testNextIdShouldRejectNullGeneratedUuid() {
        UuidOrderItemIdGenerator generator =
                new UuidOrderItemIdGenerator(() -> null);

        assertThatThrownBy(generator::nextId)
                .as("null generated order item UUID")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UUID supplier must not return null");
    }
}
