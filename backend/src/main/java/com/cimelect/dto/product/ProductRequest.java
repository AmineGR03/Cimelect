package com.cimelect.dto.product;

import jakarta.validation.constraints.NotBlank;

public record ProductRequest(
        String sku,
        @NotBlank String name,
        String description,
        String unit
) {
}
