package com.cimelect.controller;

import com.cimelect.dto.audit.AuditLogResponse;
import com.cimelect.mapper.AuditMapper;
import com.cimelect.repository.AuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'RESPONSABLE')")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final AuditMapper auditMapper;

    public AuditController(AuditLogRepository auditLogRepository, AuditMapper auditMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditMapper = auditMapper;
    }

    @GetMapping
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(auditMapper::toResponse)
                .toList();
    }
}
