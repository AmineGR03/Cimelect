package com.cimelect.dto.customer;

import java.time.Instant;

public record CustomerResponse(
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
