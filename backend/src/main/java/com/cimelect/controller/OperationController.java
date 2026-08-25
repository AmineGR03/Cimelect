package com.cimelect.controller;

import com.cimelect.dto.audit.AuditLogResponse;
import com.cimelect.dto.operation.DeleteOperationRequest;
import com.cimelect.dto.operation.OperationRequest;
import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.dto.operation.StatusUpdateRequest;
import com.cimelect.enums.OperationType;
import com.cimelect.service.OperationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operations")
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/import")
    public List<OperationResponse> listImports() {
        return operationService.findByType(OperationType.IMPORT);
    }

    @GetMapping("/export")
    public List<OperationResponse> listExports() {
        return operationService.findByType(OperationType.EXPORT);
    }

    @GetMapping("/{id}")
    public OperationResponse findById(@PathVariable Long id) {
        return operationService.findById(id);
    }

    @GetMapping("/{id}/history")
    public List<AuditLogResponse> history(@PathVariable Long id) {
        return operationService.history(id);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public OperationResponse createImport(@Valid @RequestBody OperationRequest request) {
        return operationService.create(OperationType.IMPORT, request);
    }

    @PostMapping("/export")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public OperationResponse createExport(@Valid @RequestBody OperationRequest request) {
        return operationService.create(OperationType.EXPORT, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public OperationResponse update(@PathVariable Long id, @Valid @RequestBody OperationRequest request) {
        return operationService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT', 'RESPONSABLE')")
    public OperationResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return operationService.updateStatus(id, request.status());
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE')")
    public OperationResponse close(@PathVariable Long id) {
        return operationService.close(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE')")
    public void delete(@PathVariable Long id, @Valid @RequestBody DeleteOperationRequest request) {
        operationService.delete(id, request);
    }
}
