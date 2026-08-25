package com.cimelect.mapper;

import com.cimelect.dto.shipment.ShipmentResponse;
import com.cimelect.entity.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOperation().getId(),
                shipment.getOperation().getReference(),
                shipment.getCarrier(),
                shipment.getDepartureDate(),
                shipment.getExpectedArrivalDate(),
                shipment.getActualArrivalDate(),
                shipment.getStatus(),
                shipment.isAiAnalysisTriggered(),
                shipment.getCreatedAt()
        );
    }
}
