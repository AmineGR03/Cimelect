package com.cimelect.controller;

import com.cimelect.dto.customer.CustomerRequest;
import com.cimelect.dto.customer.CustomerResponse;
import com.cimelect.dto.operation.OperationResponse;
import com.cimelect.dto.supplier.PartnerIndicatorsResponse;
import com.cimelect.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }

    @GetMapping("/{id}/operations")
    public List<OperationResponse> operations(@PathVariable Long id) {
        return customerService.operations(id);
    }

    @GetMapping("/{id}/indicators")
    public PartnerIndicatorsResponse indicators(@PathVariable Long id) {
        return customerService.indicators(id);
    }
}
