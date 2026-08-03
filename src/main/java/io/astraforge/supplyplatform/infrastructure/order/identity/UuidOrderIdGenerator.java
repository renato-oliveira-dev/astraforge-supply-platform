package io.astraforge.supplyplatform.infrastructure.order.identity;

import io.astraforge.supplyplatform.application.order.port.out.OrderIdGenerator;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderId;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class UuidOrderIdGenerator implements OrderIdGenerator {

    private final Supplier<UUID> uuidSupplier;

    public UuidOrderIdGenerator() {
        this(UUID::randomUUID);
    }

    UuidOrderIdGenerator(Supplier<UUID> uuidSupplier) {
        this.uuidSupplier = Objects.requireNonNull(
                uuidSupplier,
                "UUID supplier must not be null");
    }

    @Override
    public OrderId nextId() {
        UUID value = Objects.requireNonNull(
                uuidSupplier.get(),
                "UUID supplier must not return null");
        return new OrderId(value);
    }
}
