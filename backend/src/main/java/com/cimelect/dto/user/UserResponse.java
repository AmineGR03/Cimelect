package com.cimelect.dto.user;

import com.cimelect.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt
) {
}
