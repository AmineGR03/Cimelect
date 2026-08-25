package com.cimelect.dto.user;

import com.cimelect.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String password,
        @NotNull Role role,
        Boolean enabled
) {
}
