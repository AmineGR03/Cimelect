package com.cimelect.dto.product;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String unit,
        boolean active
) {
}
