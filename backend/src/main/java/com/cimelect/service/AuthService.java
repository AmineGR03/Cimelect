package com.cimelect.service;

import com.cimelect.dto.auth.AuthResponse;
import com.cimelect.dto.auth.LoginRequest;
import com.cimelect.entity.User;
import com.cimelect.enums.AuditAction;
import com.cimelect.repository.UserRepository;
import com.cimelect.security.CurrentUserService;
import com.cimelect.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            AuditService auditService,
            CurrentUserService currentUserService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.currentUserService = currentUserService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        auditService.log(user, "User", user.getId(), AuditAction.LOGIN, "Connexion");
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }

    public void logout() {
        User user = currentUserService.requireUser();
        auditService.log(user, "User", user.getId(), AuditAction.LOGOUT, "Déconnexion");
    }
}
