package io.astraforge.supplyplatform.infrastructure.order.web;

import io.astraforge.supplyplatform.application.order.port.in.ReopenOrderForRevisionUseCase;
import io.astraforge.supplyplatform.application.order.usecase.ReopenOrderForRevisionResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/revision")
public final class OrderRevisionController {

    private final ReopenOrderForRevisionUseCase reopenOrderForRevisionUseCase;

    public OrderRevisionController(
            ReopenOrderForRevisionUseCase reopenOrderForRevisionUseCase
    ) {
        this.reopenOrderForRevisionUseCase = Objects.requireNonNull(
                reopenOrderForRevisionUseCase,
                "Reopen order for revision use case must not be null");
    }

    @PostMapping("/reopening")
    public ResponseEntity<ReopenOrderForRevisionResponse> reopen(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReopenOrderForRevisionRequest request
    ) {
        ReopenOrderForRevisionResult result =
                reopenOrderForRevisionUseCase.reopen(
                        OrderRevisionWebMapper.toCommand(
                                orderId,
                                request));

        return ResponseEntity.ok(
                OrderRevisionWebMapper.toResponse(result));
    }
}
