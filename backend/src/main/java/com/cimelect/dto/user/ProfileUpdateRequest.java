package com.cimelect.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String password
) {
}
