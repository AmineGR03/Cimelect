package com.cimelect.service;

import com.cimelect.entity.AuditLog;
import com.cimelect.entity.User;
import com.cimelect.enums.AuditAction;
import com.cimelect.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User actor, String entityType, Long entityId, AuditAction action, String details, String justification) {
        auditLogRepository.save(AuditLog.builder()
                .actor(actor)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .justification(justification)
                .build());
    }

    public void log(User actor, String entityType, Long entityId, AuditAction action, String details) {
        log(actor, entityType, entityId, action, details, null);
    }
}
