package com.cimelect.dto.shipment;

import com.cimelect.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ShipmentRequest(
        @NotNull Long operationId,
        String carrier,
        LocalDate departureDate,
        LocalDate expectedArrivalDate,
        LocalDate actualArrivalDate,
        ShipmentStatus status
) {
}
