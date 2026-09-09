package com.cimelect.service;

import com.cimelect.dto.user.ProfileUpdateRequest;
import com.cimelect.dto.user.UserRequest;
import com.cimelect.dto.user.UserResponse;
import com.cimelect.entity.User;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.Role;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.UserMapper;
import com.cimelect.repository.UserRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(get(id));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return userMapper.toResponse(currentUserService.requireUser());
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        User actor = currentUserService.requireUser();
        
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException("Le mot de passe est obligatoire");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email déjà utilisé");
        }
        User created = userRepository.save(User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(request.enabled() == null || request.enabled())
                .build());
        auditService.log(actor, "User", created.getId(), AuditAction.CREATE, "Création compte " + created.getEmail());
        return userMapper.toResponse(created);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User actor = currentUserService.requireUser();
        User user = get(id);
        
        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BusinessException("Email déjà utilisé");
        }
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setRole(request.role());
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        auditService.log(actor, "User", id, AuditAction.UPDATE, "Modification compte " + user.getEmail());
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(ProfileUpdateRequest request) {
        User user = currentUserService.requireUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        auditService.log(user, "User", user.getId(), AuditAction.UPDATE, "Mise à jour du profil");
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User actor = currentUserService.requireUser();
        User user = get(id);
        userRepository.delete(user);
        auditService.log(actor, "User", id, AuditAction.DELETE, "Suppression compte " + user.getEmail());
    }

    private User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
    }
}
