package com.cimelect.service;

import com.cimelect.dto.customer.CustomerRequest;
import com.cimelect.dto.customer.CustomerResponse;
import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.dto.supplier.PartnerIndicatorsResponse;
import com.cimelect.entity.Customer;
import com.cimelect.entity.Operation;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.OperationStatus;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.CustomerMapper;
import com.cimelect.mapper.OperationMapper;
import com.cimelect.repository.CustomerRepository;
import com.cimelect.repository.OperationRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            OperationRepository operationRepository,
            OperationMapper operationMapper,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.operationRepository = operationRepository;
        this.operationMapper = operationMapper;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(customerMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerMapper.toResponse(get(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer saved = customerRepository.save(customerMapper.toEntity(request));
        auditService.log(currentUserService.requireUser(), "Customer", saved.getId(), AuditAction.CREATE, "Création client");
        return customerMapper.toResponse(saved);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = get(id);
        customerMapper.update(customer, request);
        auditService.log(currentUserService.requireUser(), "Customer", id, AuditAction.UPDATE, "Modification client");
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = get(id);
        boolean hasActive = operationRepository.existsByCustomerIdAndDeletedFalseAndStatusNot(id, OperationStatus.CLOTUREE);
        if (hasActive) {
            throw new BusinessException("Impossible de supprimer un client lié à une opération active (RG11)");
        }
        customer.setArchived(true);
        auditService.log(currentUserService.requireUser(), "Customer", id, AuditAction.ARCHIVE, "Archivage client");
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> operations(Long id) {
        get(id);
        return operationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(id)
                .stream().map(operationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PartnerIndicatorsResponse indicators(Long id) {
        get(id);
        List<Operation> operations = operationRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(id);
        BigDecimal total = operations.stream()
                .map(op -> op.getActualCost() != null ? op.getActualCost() : op.getPlannedCost())
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long anomalies = operations.stream()
                .flatMap(op -> op.getShipments().stream())
                .filter(s -> s.isAiAnalysisTriggered())
                .count();
        return new PartnerIndicatorsResponse(id, "CUSTOMER", operations.size(), total, anomalies);
    }

    private Customer get(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable"));
    }
}
