package io.astraforge.supplyplatform.infrastructure.order.identity;

import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UuidOrderIdGeneratorTest {

    private static final UUID EXPECTED_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001");

    @Test
    void testNextIdShouldWrapGeneratedUuid() {
        UuidOrderIdGenerator generator =
                new UuidOrderIdGenerator(() -> EXPECTED_ID);

        OrderId result = generator.nextId();

        assertThat(result.value())
                .as("generated order identifier")
                .isEqualTo(EXPECTED_ID);
    }

    @Test
    void testConstructorShouldRejectNullSupplier() {
        assertThatThrownBy(() -> new UuidOrderIdGenerator(null))
                .as("null order UUID supplier")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UUID supplier must not be null");
    }

    @Test
    void testNextIdShouldRejectNullGeneratedUuid() {
        UuidOrderIdGenerator generator =
                new UuidOrderIdGenerator(() -> null);

        assertThatThrownBy(generator::nextId)
                .as("null generated order UUID")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UUID supplier must not return null");
    }
}
