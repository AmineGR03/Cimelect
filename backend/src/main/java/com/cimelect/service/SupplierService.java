package com.cimelect.service;

import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.dto.supplier.PartnerIndicatorsResponse;
import com.cimelect.dto.supplier.SupplierRequest;
import com.cimelect.dto.supplier.SupplierResponse;
import com.cimelect.entity.Operation;
import com.cimelect.entity.Supplier;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.OperationStatus;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.OperationMapper;
import com.cimelect.mapper.SupplierMapper;
import com.cimelect.repository.OperationRepository;
import com.cimelect.repository.SupplierRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public SupplierService(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper,
            OperationRepository operationRepository,
            OperationMapper operationMapper,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.operationRepository = operationRepository;
        this.operationMapper = operationMapper;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream().map(supplierMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return supplierMapper.toResponse(get(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier saved = supplierRepository.save(supplierMapper.toEntity(request));
        auditService.log(currentUserService.requireUser(), "Supplier", saved.getId(), AuditAction.CREATE, "Création fournisseur");
        return supplierMapper.toResponse(saved);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = get(id);
        supplierMapper.update(supplier, request);
        auditService.log(currentUserService.requireUser(), "Supplier", id, AuditAction.UPDATE, "Modification fournisseur");
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = get(id);
        boolean hasActive = operationRepository.existsBySupplierIdAndDeletedFalseAndStatusNot(id, OperationStatus.CLOTUREE);
        if (hasActive) {
            throw new BusinessException("Impossible de supprimer un fournisseur lié à une opération active (RG11)");
        }
        supplier.setArchived(true);
        auditService.log(currentUserService.requireUser(), "Supplier", id, AuditAction.ARCHIVE, "Archivage fournisseur");
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> operations(Long id) {
        get(id);
        return operationRepository.findBySupplierIdAndDeletedFalseOrderByCreatedAtDesc(id)
                .stream().map(operationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PartnerIndicatorsResponse indicators(Long id) {
        get(id);
        List<Operation> operations = operationRepository.findBySupplierIdAndDeletedFalseOrderByCreatedAtDesc(id);
        BigDecimal total = operations.stream()
                .map(op -> op.getActualCost() != null ? op.getActualCost() : op.getPlannedCost())
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long anomalies = operations.stream()
                .flatMap(op -> op.getShipments().stream())
                .filter(s -> s.isAiAnalysisTriggered())
                .count();
        return new PartnerIndicatorsResponse(id, "SUPPLIER", operations.size(), total, anomalies);
    }

    private Supplier get(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable"));
    }
}
