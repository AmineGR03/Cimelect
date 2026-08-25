package com.cimelect.dto.customer;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank String companyName,
        @NotBlank String country,
        String contactName,
        String email,
        String phone
) {
}
