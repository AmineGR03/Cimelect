package com.cimelect.repository;

import com.cimelect.entity.AuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = {"actor"})
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    @EntityGraph(attributePaths = {"actor"})
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
