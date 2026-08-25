package com.cimelect.dto.auth;

import com.cimelect.enums.Role;

public record AuthResponse(
        String token,
        String type,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role
) {
    public AuthResponse(String token, Long userId, String email, String firstName, String lastName, Role role) {
        this(token, "Bearer", userId, email, firstName, lastName, role);
    }
}
