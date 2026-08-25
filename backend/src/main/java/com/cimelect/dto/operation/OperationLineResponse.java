package com.cimelect.dto.operation;

import java.math.BigDecimal;

public record OperationLineResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice
) {
}
