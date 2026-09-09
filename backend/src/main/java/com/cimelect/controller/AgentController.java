package com.cimelect.controller;

import com.cimelect.dto.agent.AgentAssignmentRequest;
import com.cimelect.dto.agent.AgentRequest;
import com.cimelect.dto.agent.AgentResponse;
import com.cimelect.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public List<AgentResponse> findAll() {
        return agentService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE')")
    public AgentResponse findById(@PathVariable Long id) {
        return agentService.findById(id);
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or (#managerId == authentication.principal.id and hasRole('RESPONSABLE'))")
    public List<AgentResponse> findByManager(@PathVariable Long managerId) {
        return agentService.findByManager(managerId);
    }

    @GetMapping("/manager/{managerId}/active")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or (#managerId == authentication.principal.id and hasRole('RESPONSABLE'))")
    public List<AgentResponse> findActiveByManager(@PathVariable Long managerId) {
        return agentService.findActiveByManager(managerId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse create(@Valid @RequestBody AgentRequest request) {
        return agentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE')")
    public AgentResponse update(@PathVariable Long id, @Valid @RequestBody AgentRequest request) {
        return agentService.update(id, request);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE')")
    @ResponseStatus(HttpStatus.OK)
    public void assignToManager(@Valid @RequestBody AgentAssignmentRequest request) {
        agentService.assignToManager(request);
    }

    @PostMapping("/{id}/unassign")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('RESPONSABLE')")
    @ResponseStatus(HttpStatus.OK)
    public void unassignFromManager(@PathVariable Long id) {
        agentService.unassignFromManager(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        agentService.delete(id);
    }
}
