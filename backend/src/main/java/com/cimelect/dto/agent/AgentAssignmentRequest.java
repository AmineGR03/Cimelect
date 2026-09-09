package com.cimelect.dto.agent;

import jakarta.validation.constraints.NotNull;

public record AgentAssignmentRequest(
        @NotNull(message = "L'ID de l'agent est obligatoire")
        Long agentId,
        
        @NotNull(message = "L'ID du responsable est obligatoire")
        Long managerId,
        
        String reason
) {
}
