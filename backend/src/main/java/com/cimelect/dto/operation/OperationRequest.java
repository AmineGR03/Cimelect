package com.cimelect.dto.operation;

import com.cimelect.enums.OperationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OperationRequest(
        Long supplierId,
        Long customerId,
        String destination,
        LocalDate orderDate,
        LocalDate expectedDate,
        String carrier,
        BigDecimal plannedCost,
        BigDecimal actualCost,
        OperationStatus status,
        @NotEmpty @Valid List<OperationLineRequest> lines
) {
}
