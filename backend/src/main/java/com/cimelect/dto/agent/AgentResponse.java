package com.cimelect.dto.agent;

import java.time.Instant;

public record AgentResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        Long managerId,
        String managerName,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
