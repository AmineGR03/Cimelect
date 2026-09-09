package com.cimelect.mapper;

import com.cimelect.dto.agent.AgentResponse;
import com.cimelect.entity.Agent;
import com.cimelect.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AgentMapper {

    public AgentResponse toResponse(Agent agent) {
        if (agent == null) {
            return null;
        }
        
        User user = agent.getUser();
        String userName = user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown";
        String userEmail = user != null ? user.getEmail() : "Unknown";
        Long userId = user != null ? user.getId() : null;
        
        User manager = agent.getManager();
        String managerName = manager != null ? manager.getFirstName() + " " + manager.getLastName() : null;
        Long managerId = manager != null ? manager.getId() : null;
        
        return new AgentResponse(
                agent.getId(),
                userId,
                userName,
                userEmail,
                managerId,
                managerName,
                agent.getName(),
                agent.getDescription(),
                agent.isActive(),
                agent.getCreatedAt(),
                agent.getUpdatedAt()
        );
    }
}
