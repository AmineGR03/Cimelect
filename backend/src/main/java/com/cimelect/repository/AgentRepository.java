package com.cimelect.repository;

import com.cimelect.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByUserId(Long userId);

    List<Agent> findByManagerId(Long managerId);

    List<Agent> findByActive(boolean active);

    @Query("SELECT a FROM Agent a WHERE a.manager.id = :managerId ORDER BY a.name")
    List<Agent> findAgentsByManager(@Param("managerId") Long managerId);

    @Query("SELECT a FROM Agent a WHERE a.active = true AND a.manager.id = :managerId")
    List<Agent> findActiveAgentsByManager(@Param("managerId") Long managerId);
}
