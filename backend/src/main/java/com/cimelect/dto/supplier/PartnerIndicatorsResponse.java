package com.cimelect.dto.supplier;

import java.math.BigDecimal;

public record PartnerIndicatorsResponse(
        Long partnerId,
        String partnerType,
        long operationCount,
        BigDecimal totalAmount,
        long anomalyCount
) {
}
