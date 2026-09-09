package com.cimelect.dto.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgentRequest(
        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        Long userId,
        
        @NotBlank(message = "Le nom de l'agent est obligatoire")
        String name,
        
        String description,
        
        boolean active
) {
}
