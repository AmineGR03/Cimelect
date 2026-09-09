package com.cimelect.repository;

import com.cimelect.entity.AgentAssignment;
import com.cimelect.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgentAssignmentRepository extends JpaRepository<AgentAssignment, Long> {

    @Query("SELECT aa FROM AgentAssignment aa WHERE aa.agent.id = :agentId ORDER BY aa.createdAt DESC")
    List<AgentAssignment> findByAgentId(@Param("agentId") Long agentId);

    @Query("SELECT aa FROM AgentAssignment aa WHERE aa.manager.id = :managerId ORDER BY aa.createdAt DESC")
    List<AgentAssignment> findByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT aa FROM AgentAssignment aa WHERE aa.assignedBy.id = :userId ORDER BY aa.createdAt DESC")
    List<AgentAssignment> findByAssignedByUserId(@Param("userId") Long userId);

    @Query("SELECT aa FROM AgentAssignment aa WHERE aa.action = :action ORDER BY aa.createdAt DESC")
    List<AgentAssignment> findByAction(@Param("action") AuditAction action);
}
