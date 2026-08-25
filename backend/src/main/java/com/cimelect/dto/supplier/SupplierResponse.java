package com.cimelect.dto.supplier;

import java.time.Instant;

public record SupplierResponse(
        Long id,
        String companyName,
        String country,
        String contactName,
        String email,
        String phone,
        boolean archived,
        Instant createdAt
) {
}
