package com.cimelect.dto.operation;

import com.cimelect.dto.document.DocumentResponse;
import com.cimelect.dto.shipment.ShipmentResponse;
import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record OperationResponse(
        Long id,
        String reference,
        OperationType type,
        Long supplierId,
        String supplierName,
        Long customerId,
        String customerName,
        String destination,
        LocalDate orderDate,
        LocalDate expectedDate,
        LocalDate actualDate,
        String carrier,
        BigDecimal plannedCost,
        BigDecimal actualCost,
        BigDecimal budgetVariance,
        OperationStatus status,
        List<OperationLineResponse> lines,
        List<DocumentResponse> documents,
        List<ShipmentResponse> shipments,
        Instant createdAt,
        Instant updatedAt
) {
}
