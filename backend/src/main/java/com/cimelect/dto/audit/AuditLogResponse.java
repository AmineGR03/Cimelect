package com.cimelect.dto.audit;

import com.cimelect.enums.AuditAction;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String entityType,
        Long entityId,
        AuditAction action,
        String details,
        String justification,
        Long actorId,
        String actorName,
        String actorEmail,
        Instant createdAt
) {
}
