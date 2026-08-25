package com.cimelect.dto.supplier;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank String companyName,
        @NotBlank String country,
        String contactName,
        String email,
        String phone
) {
}
