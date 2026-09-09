package com.cimelect.service;

import com.cimelect.dto.agent.AgentAssignmentRequest;
import com.cimelect.dto.agent.AgentRequest;
import com.cimelect.dto.agent.AgentResponse;
import com.cimelect.entity.Agent;
import com.cimelect.entity.AgentAssignment;
import com.cimelect.entity.User;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.Role;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.AgentMapper;
import com.cimelect.repository.AgentAssignmentRepository;
import com.cimelect.repository.AgentRepository;
import com.cimelect.repository.UserRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;
    private final AgentAssignmentRepository agentAssignmentRepository;
    private final UserRepository userRepository;
    private final AgentMapper agentMapper;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public AgentService(
            AgentRepository agentRepository,
            AgentAssignmentRepository agentAssignmentRepository,
            UserRepository userRepository,
            AgentMapper agentMapper,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.agentRepository = agentRepository;
        this.agentAssignmentRepository = agentAssignmentRepository;
        this.userRepository = userRepository;
        this.agentMapper = agentMapper;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> findAll() {
        return agentRepository.findAll().stream().map(agentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> findByManager(Long managerId) {
        return agentRepository.findAgentsByManager(managerId).stream().map(agentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AgentResponse findById(Long id) {
        return agentMapper.toResponse(get(id));
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> findActiveByManager(Long managerId) {
        return agentRepository.findActiveAgentsByManager(managerId).stream().map(agentMapper::toResponse).toList();
    }

    @Transactional
    public AgentResponse create(AgentRequest request) {
        User actor = currentUserService.requireUser();
        
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        
        if (!user.getRole().equals(Role.AGENT_IMPORT_EXPORT)) {
            throw new BusinessException("Seuls les agents d'import/export peuvent être ajoutés");
        }
        
        Agent created = agentRepository.save(Agent.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .active(request.active())
                .build());
        
        auditService.log(actor, "Agent", created.getId(), AuditAction.CREATE, "Création agent " + created.getName());
        return agentMapper.toResponse(created);
    }

    @Transactional
    public AgentResponse update(Long id, AgentRequest request) {
        User actor = currentUserService.requireUser();
        Agent agent = get(id);
        
        agent.setName(request.name());
        agent.setDescription(request.description());
        agent.setActive(request.active());
        
        Agent updated = agentRepository.save(agent);
        auditService.log(actor, "Agent", updated.getId(), AuditAction.UPDATE, "Mise à jour agent " + updated.getName());
        return agentMapper.toResponse(updated);
    }

    @Transactional
    public void assignToManager(AgentAssignmentRequest request) {
        User actor = currentUserService.requireUser();
        
        // Vérifier que l'utilisateur actuel est un administrateur ou le responsable
        boolean isAdmin = actor.getRole().equals(Role.ADMINISTRATEUR);
        boolean isSelfAssigning = actor.getRole().equals(Role.RESPONSABLE) && actor.getId().equals(request.managerId());
        
        if (!isAdmin && !isSelfAssigning) {
            throw new BusinessException("Vous n'avez pas la permission d'assigner cet agent");
        }
        
        Agent agent = agentRepository.findById(request.agentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent introuvable"));
        
        User manager = userRepository.findById(request.managerId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsable introuvable"));
        
        if (!manager.getRole().equals(Role.RESPONSABLE)) {
            throw new BusinessException("Le gestionnaire doit avoir le rôle de RESPONSABLE");
        }
        
        agent.setManager(manager);
        Agent updated = agentRepository.save(agent);
        
        // Enregistrer l'affectation
        agentAssignmentRepository.save(AgentAssignment.builder()
                .agent(agent)
                .manager(manager)
                .assignedBy(actor)
                .action(AuditAction.AGENT_ASSIGN)
                .reason(request.reason())
                .build());
        
        auditService.log(actor, "Agent", agent.getId(), AuditAction.AGENT_ASSIGN, 
                "Affectation de l'agent " + agent.getName() + " au responsable " + manager.getFirstName() + " " + manager.getLastName());
    }

    @Transactional
    public void unassignFromManager(Long agentId) {
        User actor = currentUserService.requireUser();
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent introuvable"));
        
        if (agent.getManager() == null) {
            throw new BusinessException("L'agent n'est pas actuellement assigné à un responsable");
        }
        
        User manager = agent.getManager();
        agent.setManager(null);
        agentRepository.save(agent);
        
        // Enregistrer la non-affectation
        agentAssignmentRepository.save(AgentAssignment.builder()
                .agent(agent)
                .manager(manager)
                .assignedBy(actor)
                .action(AuditAction.AGENT_UNASSIGN)
                .build());
        
        auditService.log(actor, "Agent", agent.getId(), AuditAction.AGENT_UNASSIGN, 
                "Suppression de l'agent " + agent.getName() + " du responsable " + manager.getFirstName() + " " + manager.getLastName());
    }

    @Transactional
    public void delete(Long id) {
        User actor = currentUserService.requireUser();
        Agent agent = get(id);
        agentRepository.delete(agent);
        auditService.log(actor, "Agent", id, AuditAction.DELETE, "Suppression agent " + agent.getName());
    }

    @Transactional(readOnly = true)
    public List<AgentAssignment> getAgentHistory(Long agentId) {
        return agentAssignmentRepository.findByAgentId(agentId);
    }

    @Transactional(readOnly = true)
    public List<AgentAssignment> getManagerAssignmentHistory(Long managerId) {
        return agentAssignmentRepository.findByManagerId(managerId);
    }

    private Agent get(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent introuvable"));
    }
}
