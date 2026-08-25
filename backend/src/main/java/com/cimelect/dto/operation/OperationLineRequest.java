package com.cimelect.dto.operation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OperationLineRequest(
        @NotNull Long productId,
        @NotNull @Positive BigDecimal quantity,
        BigDecimal unitPrice
) {
}
