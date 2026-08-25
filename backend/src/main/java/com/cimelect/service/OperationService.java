package com.cimelect.service;

import com.cimelect.dto.audit.AuditLogResponse;
import com.cimelect.dto.operation.DeleteOperationRequest;
import com.cimelect.dto.operation.OperationLineRequest;
import com.cimelect.dto.operation.OperationRequest;
import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.entity.Operation;
import com.cimelect.entity.OperationLine;
import com.cimelect.entity.Product;
import com.cimelect.entity.User;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.AuditMapper;
import com.cimelect.mapper.OperationMapper;
import com.cimelect.repository.AuditLogRepository;
import com.cimelect.repository.CustomerRepository;
import com.cimelect.repository.DocumentRepository;
import com.cimelect.repository.OperationRepository;
import com.cimelect.repository.ProductRepository;
import com.cimelect.repository.RequiredDocumentRepository;
import com.cimelect.repository.SupplierRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OperationService {

    private final OperationRepository operationRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogRepository auditLogRepository;
    private final OperationMapper operationMapper;
    private final AuditMapper auditMapper;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public OperationService(
            OperationRepository operationRepository,
            SupplierRepository supplierRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            RequiredDocumentRepository requiredDocumentRepository,
            DocumentRepository documentRepository,
            AuditLogRepository auditLogRepository,
            OperationMapper operationMapper,
            AuditMapper auditMapper,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.operationRepository = operationRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.documentRepository = documentRepository;
        this.auditLogRepository = auditLogRepository;
        this.operationMapper = operationMapper;
        this.auditMapper = auditMapper;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> findByType(OperationType type) {
        return operationRepository.findByTypeAndDeletedFalseOrderByCreatedAtDesc(type)
                .stream().map(operationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OperationResponse findById(Long id) {
        return operationMapper.toResponse(get(id));
    }

    @Transactional
    public OperationResponse create(OperationType type, OperationRequest request) {
        validateCreate(type, request);
        User actor = currentUserService.requireUser();
        Operation operation = Operation.builder()
                .reference(nextReference(type))
                .type(type)
                .status(OperationStatus.CREEE)
                .destination(request.destination())
                .orderDate(request.orderDate())
                .expectedDate(request.expectedDate())
                .carrier(request.carrier())
                .plannedCost(request.plannedCost())
                .actualCost(request.actualCost())
                .createdBy(actor)
                .build();
        applyPartner(type, operation, request);
        replaceLines(operation, request.lines());
        Operation saved = operationRepository.save(operation);
        auditService.log(actor, "Operation", saved.getId(), AuditAction.CREATE, "Création " + type + " " + saved.getReference());
        return operationMapper.toResponse(get(saved.getId()));
    }

    @Transactional
    public OperationResponse update(Long id, OperationRequest request) {
        Operation operation = get(id);
        assertNotClosed(operation);
        validateCreate(operation.getType(), request);
        applyPartner(operation.getType(), operation, request);
        operation.setDestination(request.destination());
        operation.setOrderDate(request.orderDate());
        operation.setExpectedDate(request.expectedDate());
        operation.setCarrier(request.carrier());
        operation.setPlannedCost(request.plannedCost());
        if (request.actualCost() != null) {
            operation.setActualCost(request.actualCost());
        }
        replaceLines(operation, request.lines());
        auditService.log(currentUserService.requireUser(), "Operation", id, AuditAction.UPDATE, "Modification " + operation.getReference());
        return operationMapper.toResponse(operation);
    }

    @Transactional
    public OperationResponse updateStatus(Long id, OperationStatus status) {
        Operation operation = get(id);
        assertNotClosed(operation);
        validateTransition(operation.getType(), operation.getStatus(), status);
        if (status == OperationStatus.CLOTUREE) {
            throw new BusinessException("La clôture doit passer par l'action de clôture dédiée");
        }
        applyReceptionRules(operation, status);
        OperationStatus previous = operation.getStatus();
        operation.setStatus(status);
        auditService.log(currentUserService.requireUser(), "Operation", id, AuditAction.STATUS_CHANGE, previous + " -> " + status);
        return operationMapper.toResponse(operation);
    }

    @Transactional
    public OperationResponse close(Long id) {
        Operation operation = get(id);
        assertNotClosed(operation);
        OperationStatus expectedBeforeClose = operation.getType() == OperationType.IMPORT
                ? OperationStatus.RECUE
                : OperationStatus.LIVREE;
        if (operation.getStatus() != expectedBeforeClose) {
            throw new BusinessException("L'opération doit être au statut " + expectedBeforeClose + " avant clôture");
        }
        ensureRequiredDocuments(operation);
        if (operation.getActualCost() == null) {
            throw new BusinessException("Le coût réel doit être renseigné avant clôture");
        }
        operation.setStatus(OperationStatus.CLOTUREE);
        auditService.log(currentUserService.requireUser(), "Operation", id, AuditAction.CLOSE, "Clôture " + operation.getReference());
        return operationMapper.toResponse(operation);
    }

    @Transactional
    public void delete(Long id, DeleteOperationRequest request) {
        Operation operation = get(id);
        operation.setDeleted(true);
        operation.setDeletionJustification(request.justification());
        auditService.log(
                currentUserService.requireUser(),
                "Operation",
                id,
                AuditAction.DELETE,
                "Suppression " + operation.getReference(),
                request.justification()
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> history(Long id) {
        get(id);
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Operation", id)
                .stream().map(auditMapper::toResponse).toList();
    }

    public void applyStatusFromShipment(Operation operation, OperationStatus mapped) {
        if (operation.isClosed() || mapped == null || mapped == operation.getStatus()) {
            return;
        }
        List<OperationStatus> cycle = operation.getType() == OperationType.IMPORT
                ? OperationStatus.importCycle()
                : OperationStatus.exportCycle();
        int current = cycle.indexOf(operation.getStatus());
        int next = cycle.indexOf(mapped);
        if (next > current && mapped != OperationStatus.CLOTUREE) {
            applyReceptionRules(operation, mapped);
            operation.setStatus(mapped);
        }
    }

    private void applyReceptionRules(Operation operation, OperationStatus status) {
        if (operation.getType() == OperationType.IMPORT && status == OperationStatus.RECUE) {
            if (operation.getActualCost() == null) {
                throw new BusinessException("Le coût réel doit être renseigné à la réception (RG04)");
            }
            if (operation.getActualDate() == null) {
                operation.setActualDate(LocalDate.now());
            }
        }
        if (operation.getType() == OperationType.EXPORT && status == OperationStatus.LIVREE && operation.getActualDate() == null) {
            operation.setActualDate(LocalDate.now());
        }
    }

    private void ensureRequiredDocuments(Operation operation) {
        Set<DocumentType> present = documentRepository.findByOperationId(operation.getId()).stream()
                .map(doc -> doc.getType())
                .collect(Collectors.toSet());
        requiredDocumentRepository.findByOperationTypeAndRequiredTrue(operation.getType()).forEach(req -> {
            if (!present.contains(req.getDocumentType())) {
                throw new BusinessException("Document obligatoire manquant: " + req.getDocumentType() + " (RG03/RG08)");
            }
        });
    }

    private void validateCreate(OperationType type, OperationRequest request) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessException("Au moins un produit est obligatoire");
        }
        if (type == OperationType.IMPORT) {
            if (request.supplierId() == null) {
                throw new BusinessException("Le fournisseur est obligatoire");
            }
            if (request.orderDate() == null) {
                throw new BusinessException("La date de commande est obligatoire");
            }
        } else {
            if (request.customerId() == null) {
                throw new BusinessException("Le client est obligatoire");
            }
            if (request.destination() == null || request.destination().isBlank()) {
                throw new BusinessException("La destination est obligatoire");
            }
        }
    }

    private void applyPartner(OperationType type, Operation operation, OperationRequest request) {
        if (type == OperationType.IMPORT) {
            operation.setSupplier(supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur introuvable")));
            operation.setCustomer(null);
        } else {
            operation.setCustomer(customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client introuvable")));
            operation.setSupplier(null);
        }
    }

    private void replaceLines(Operation operation, List<OperationLineRequest> lines) {
        operation.getLines().clear();
        for (OperationLineRequest line : lines) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable: " + line.productId()));
            operation.addLine(OperationLine.builder()
                    .product(product)
                    .quantity(line.quantity())
                    .unitPrice(line.unitPrice())
                    .build());
        }
    }

    private void validateTransition(OperationType type, OperationStatus from, OperationStatus to) {
        List<OperationStatus> cycle = type == OperationType.IMPORT
                ? OperationStatus.importCycle()
                : OperationStatus.exportCycle();
        if (!cycle.contains(to)) {
            throw new BusinessException("Statut " + to + " invalide pour une opération " + type);
        }
        int fromIndex = cycle.indexOf(from);
        int toIndex = cycle.indexOf(to);
        if (toIndex != fromIndex + 1) {
            throw new BusinessException("Transition interdite: " + from + " -> " + to);
        }
    }

    private void assertNotClosed(Operation operation) {
        if (operation.isClosed()) {
            throw new BusinessException("Une opération clôturée ne peut plus être modifiée");
        }
    }

    private String nextReference(OperationType type) {
        String prefix = (type == OperationType.IMPORT ? "IMP" : "EXP") + "-" + Year.now() + "-";
        long count = operationRepository.countByReferencePrefix(prefix) + 1;
        return prefix + String.format("%04d", count);
    }

    private Operation get(Long id) {
        return operationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable"));
    }
}
