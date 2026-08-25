package com.cimelect.controller;

import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.dto.supplier.PartnerIndicatorsResponse;
import com.cimelect.dto.supplier.SupplierRequest;
import com.cimelect.dto.supplier.SupplierResponse;
import com.cimelect.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> findAll() {
        return supplierService.findAll();
    }

    @GetMapping("/{id}")
    public SupplierResponse findById(@PathVariable Long id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return supplierService.create(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }

    @GetMapping("/{id}/operations")
    public List<OperationResponse> operations(@PathVariable Long id) {
        return supplierService.operations(id);
    }

    @GetMapping("/{id}/indicators")
    public PartnerIndicatorsResponse indicators(@PathVariable Long id) {
        return supplierService.indicators(id);
    }
}
