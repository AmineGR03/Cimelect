package com.cimelect.mapper;

import com.cimelect.dto.audit.AuditLogResponse;
import com.cimelect.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getDetails(),
                log.getJustification(),
                log.getActor() != null ? log.getActor().getId() : null,
                log.getActor() != null ? log.getActor().getEmail() : null,
                log.getCreatedAt()
        );
    }
}
