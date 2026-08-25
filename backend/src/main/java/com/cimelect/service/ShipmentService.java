package com.cimelect.service;

import com.cimelect.config.AppProperties;
import com.cimelect.dto.shipment.ShipmentRequest;
import com.cimelect.dto.shipment.ShipmentResponse;
import com.cimelect.entity.Operation;
import com.cimelect.entity.Shipment;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;
import com.cimelect.enums.ShipmentStatus;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.ShipmentMapper;
import com.cimelect.repository.OperationRepository;
import com.cimelect.repository.ShipmentRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OperationRepository operationRepository;
    private final ShipmentMapper shipmentMapper;
    private final OperationService operationService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AppProperties appProperties;

    public ShipmentService(
            ShipmentRepository shipmentRepository,
            OperationRepository operationRepository,
            ShipmentMapper shipmentMapper,
            OperationService operationService,
            CurrentUserService currentUserService,
            AuditService auditService,
            AppProperties appProperties
    ) {
        this.shipmentRepository = shipmentRepository;
        this.operationRepository = operationRepository;
        this.shipmentMapper = shipmentMapper;
        this.operationService = operationService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> inProgress() {
        return shipmentRepository.findByStatusNot(ShipmentStatus.LIVREE).stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShipmentResponse findById(Long id) {
        return shipmentMapper.toResponse(get(id));
    }

    @Transactional
    public ShipmentResponse create(ShipmentRequest request) {
        Operation operation = operationRepository.findByIdAndDeletedFalse(request.operationId())
                .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable"));
        if (operation.isClosed()) {
            throw new BusinessException("Impossible d'ajouter une expédition à une opération clôturée");
        }
        Shipment shipment = Shipment.builder()
                .operation(operation)
                .carrier(request.carrier() != null ? request.carrier() : operation.getCarrier())
                .departureDate(request.departureDate())
                .expectedArrivalDate(request.expectedArrivalDate())
                .actualArrivalDate(request.actualArrivalDate())
                .status(request.status() != null ? request.status() : ShipmentStatus.EN_PREPARATION)
                .build();
        evaluateDelay(shipment);
        Shipment saved = shipmentRepository.save(shipment);
        syncOperation(saved);
        auditService.log(currentUserService.requireUser(), "Shipment", saved.getId(), AuditAction.CREATE,
                "Expédition rattachée à " + operation.getReference());
        return shipmentMapper.toResponse(saved);
    }

    @Transactional
    public ShipmentResponse update(Long id, ShipmentRequest request) {
        Shipment shipment = get(id);
        if (shipment.getOperation().isClosed()) {
            throw new BusinessException("Opération clôturée");
        }
        shipment.setCarrier(request.carrier());
        shipment.setDepartureDate(request.departureDate());
        shipment.setExpectedArrivalDate(request.expectedArrivalDate());
        shipment.setActualArrivalDate(request.actualArrivalDate());
        if (request.status() != null) {
            shipment.setStatus(request.status());
        }
        evaluateDelay(shipment);
        syncOperation(shipment);
        auditService.log(currentUserService.requireUser(), "Shipment", id, AuditAction.UPDATE, "Mise à jour expédition");
        return shipmentMapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse updateStatus(Long id, ShipmentStatus status) {
        Shipment shipment = get(id);
        shipment.setStatus(status);
        evaluateDelay(shipment);
        syncOperation(shipment);
        auditService.log(currentUserService.requireUser(), "Shipment", id, AuditAction.STATUS_CHANGE, "Statut " + status);
        return shipmentMapper.toResponse(shipment);
    }

    private void evaluateDelay(Shipment shipment) {
        if (shipment.getExpectedArrivalDate() != null && shipment.getActualArrivalDate() != null) {
            long delay = ChronoUnit.DAYS.between(shipment.getExpectedArrivalDate(), shipment.getActualArrivalDate());
            int threshold = appProperties.shipment().delayThresholdDays();
            if (delay > threshold && !shipment.isAiAnalysisTriggered()) {
                shipment.setAiAnalysisTriggered(true);
                auditService.log(
                        currentUserService.requireUser(),
                        "Shipment",
                        shipment.getId(),
                        AuditAction.UPDATE,
                        "Écart de délai " + delay + " j > seuil " + threshold + " : analyse IA à déclencher (RG15)"
                );
            }
        }
    }

    private void syncOperation(Shipment shipment) {
        Operation operation = shipment.getOperation();
        OperationStatus mapped = mapStatus(operation.getType(), shipment.getStatus());
        operationService.applyStatusFromShipment(operation, mapped);
    }

    private OperationStatus mapStatus(OperationType type, ShipmentStatus status) {
        return switch (status) {
            case EN_PREPARATION -> type == OperationType.EXPORT ? OperationStatus.PREPARATION : null;
            case EN_TRANSIT -> type == OperationType.IMPORT ? OperationStatus.EN_TRANSIT : OperationStatus.EXPEDIEE;
            case ARRIVEE -> type == OperationType.IMPORT ? OperationStatus.DEDOUANEMENT : OperationStatus.EXPEDIEE;
            case LIVREE -> type == OperationType.IMPORT ? OperationStatus.RECUE : OperationStatus.LIVREE;
        };
    }

    private Shipment get(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expédition introuvable"));
    }
}
