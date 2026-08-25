package com.cimelect.mapper;

import com.cimelect.dto.operation.OperationLineResponse;
import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.entity.Operation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OperationMapper {

    private final DocumentMapper documentMapper;
    private final ShipmentMapper shipmentMapper;

    public OperationMapper(DocumentMapper documentMapper, ShipmentMapper shipmentMapper) {
        this.documentMapper = documentMapper;
        this.shipmentMapper = shipmentMapper;
    }

    public OperationResponse toResponse(Operation operation) {
        BigDecimal variance = null;
        if (operation.getPlannedCost() != null && operation.getActualCost() != null) {
            variance = operation.getActualCost().subtract(operation.getPlannedCost());
        }
        List<OperationLineResponse> lines = operation.getLines().stream()
                .map(line -> new OperationLineResponse(
                        line.getId(),
                        line.getProduct().getId(),
                        line.getProduct().getName(),
                        line.getQuantity(),
                        line.getUnitPrice()
                ))
                .toList();
        return new OperationResponse(
                operation.getId(),
                operation.getReference(),
                operation.getType(),
                operation.getSupplier() != null ? operation.getSupplier().getId() : null,
                operation.getSupplier() != null ? operation.getSupplier().getCompanyName() : null,
                operation.getCustomer() != null ? operation.getCustomer().getId() : null,
                operation.getCustomer() != null ? operation.getCustomer().getCompanyName() : null,
                operation.getDestination(),
                operation.getOrderDate(),
                operation.getExpectedDate(),
                operation.getActualDate(),
                operation.getCarrier(),
                operation.getPlannedCost(),
                operation.getActualCost(),
                variance,
                operation.getStatus(),
                lines,
                operation.getDocuments().stream().map(documentMapper::toResponse).toList(),
                operation.getShipments().stream().map(shipmentMapper::toResponse).toList(),
                operation.getCreatedAt(),
                operation.getUpdatedAt()
        );
    }
}
