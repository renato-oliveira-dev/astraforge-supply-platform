package io.astraforge.supplyplatform.infrastructure.order.identity;

import io.astraforge.supplyplatform.application.order.port.out.OrderItemIdGenerator;
import io.astraforge.supplyplatform.domain.order.valueobject.OrderItemId;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class UuidOrderItemIdGenerator
        implements OrderItemIdGenerator {

    private final Supplier<UUID> uuidSupplier;

    public UuidOrderItemIdGenerator() {
        this(UUID::randomUUID);
    }

    UuidOrderItemIdGenerator(Supplier<UUID> uuidSupplier) {
        this.uuidSupplier = Objects.requireNonNull(
                uuidSupplier,
                "UUID supplier must not be null");
    }

    @Override
    public OrderItemId nextId() {
        UUID value = Objects.requireNonNull(
                uuidSupplier.get(),
                "UUID supplier must not return null");
        return new OrderItemId(value);
    }
}
