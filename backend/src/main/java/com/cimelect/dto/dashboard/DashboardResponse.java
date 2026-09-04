package com.cimelect.dto.dashboard;

import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        Map<OperationType, Map<OperationStatus, Long>> operationsInProgressByStatus,
        BigDecimal averageDelayDays,
        BigDecimal averageCost,
        BigDecimal anomalyRate,
        List<VolumePoint> volumeTrend,
        List<CostPoint> costTrend,
        Map<String, BigDecimal> documentCompliance,
        List<AiAlert> activeAiAlerts
) {
    public record VolumePoint(String period, long importCount, long exportCount) {
    }

    public record CostPoint(String period, BigDecimal importCost, BigDecimal exportCost) {
    }

    public record AiAlert(Long shipmentId, String message) {
    }
}
