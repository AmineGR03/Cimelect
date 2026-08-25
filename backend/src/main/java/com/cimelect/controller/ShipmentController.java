package com.cimelect.controller;

import com.cimelect.dto.shipment.ShipmentRequest;
import com.cimelect.dto.shipment.ShipmentResponse;
import com.cimelect.dto.shipment.ShipmentStatusUpdateRequest;
import com.cimelect.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT', 'RESPONSABLE')")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/in-progress")
    public List<ShipmentResponse> inProgress() {
        return shipmentService.inProgress();
    }

    @GetMapping("/{id}")
    public ShipmentResponse findById(@PathVariable Long id) {
        return shipmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public ShipmentResponse create(@Valid @RequestBody ShipmentRequest request) {
        return shipmentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public ShipmentResponse update(@PathVariable Long id, @Valid @RequestBody ShipmentRequest request) {
        return shipmentService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public ShipmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return shipmentService.updateStatus(id, request.status());
    }
}
