package com.cimelect.dto.shipment;

import com.cimelect.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record ShipmentStatusUpdateRequest(
        @NotNull ShipmentStatus status
) {
}
