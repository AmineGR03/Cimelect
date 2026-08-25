package com.cimelect.dto.shipment;

import com.cimelect.enums.ShipmentStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ShipmentResponse(
        Long id,
        Long operationId,
        String operationReference,
        String carrier,
        LocalDate departureDate,
        LocalDate expectedArrivalDate,
        LocalDate actualArrivalDate,
        ShipmentStatus status,
        boolean aiAnalysisTriggered,
        Instant createdAt
) {
}
