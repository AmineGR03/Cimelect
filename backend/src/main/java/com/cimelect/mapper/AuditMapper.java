package com.cimelect.mapper;

import com.cimelect.dto.audit.AuditLogResponse;
import com.cimelect.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        String actorName = null;
        if (log.getActor() != null) {
            actorName = log.getActor().getFirstName() + " " + log.getActor().getLastName();
        }
        return new AuditLogResponse(
                log.getId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getDetails(),
                log.getJustification(),
                log.getActor() != null ? log.getActor().getId() : null,
                actorName,
                log.getActor() != null ? log.getActor().getEmail() : null,
                log.getCreatedAt()
        );
    }
}
