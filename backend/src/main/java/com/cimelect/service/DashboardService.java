package com.cimelect.service;

import com.cimelect.dto.dashboard.DashboardResponse;
import com.cimelect.entity.Operation;
import com.cimelect.entity.Shipment;
import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;
import com.cimelect.repository.OperationRepository;
import com.cimelect.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OperationRepository operationRepository;
    private final ShipmentRepository shipmentRepository;

    public DashboardService(OperationRepository operationRepository, ShipmentRepository shipmentRepository) {
        this.operationRepository = operationRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse build() {
        List<Operation> operations = operationRepository.findByDeletedFalse();
        Map<OperationType, Map<OperationStatus, Long>> byStatus = new EnumMap<>(OperationType.class);
        for (OperationType type : OperationType.values()) {
            Map<OperationStatus, Long> counts = new EnumMap<>(OperationStatus.class);
            for (OperationStatus status : OperationStatus.values()) {
                if (status != OperationStatus.CLOTUREE) {
                    counts.put(status, operations.stream()
                            .filter(o -> o.getType() == type && o.getStatus() == status)
                            .count());
                }
            }
            byStatus.put(type, counts);
        }

        List<Long> delays = operations.stream()
                .filter(o -> o.getExpectedDate() != null && o.getActualDate() != null)
                .map(o -> ChronoUnit.DAYS.between(o.getExpectedDate(), o.getActualDate()))
                .toList();
        BigDecimal averageDelay = delays.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(delays.stream().mapToLong(Long::longValue).average().orElse(0))
                .setScale(2, RoundingMode.HALF_UP);

        List<BigDecimal> costs = operations.stream()
                .map(o -> o.getActualCost() != null ? o.getActualCost() : o.getPlannedCost())
                .filter(c -> c != null)
                .toList();
        BigDecimal averageCost = costs.isEmpty()
                ? BigDecimal.ZERO
                : costs.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(costs.size()), 2, RoundingMode.HALF_UP);

        List<Shipment> shipments = shipmentRepository.findAll();
        long anomalies = shipments.stream().filter(Shipment::isAiAnalysisTriggered).count();
        BigDecimal anomalyRate = shipments.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(anomalies * 100.0 / shipments.size()).setScale(2, RoundingMode.HALF_UP);

        Map<YearMonth, List<Operation>> byMonth = operations.stream()
                .collect(Collectors.groupingBy(o -> YearMonth.from(o.getCreatedAt().atZone(java.time.ZoneOffset.UTC))));

        List<DashboardResponse.VolumePoint> volumeTrend = new ArrayList<>();
        List<DashboardResponse.CostPoint> costTrend = new ArrayList<>();
        byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String period = entry.getKey().toString();
                    List<Operation> monthOps = entry.getValue();
                    long imports = monthOps.stream().filter(o -> o.getType() == OperationType.IMPORT).count();
                    long exports = monthOps.stream().filter(o -> o.getType() == OperationType.EXPORT).count();
                    volumeTrend.add(new DashboardResponse.VolumePoint(period, imports, exports));
                    costTrend.add(new DashboardResponse.CostPoint(
                            period,
                            sumCost(monthOps, OperationType.IMPORT),
                            sumCost(monthOps, OperationType.EXPORT)
                    ));
                });

        List<DashboardResponse.AiAlert> alerts = shipments.stream()
                .filter(Shipment::isAiAnalysisTriggered)
                .map(s -> new DashboardResponse.AiAlert(s.getId(), "Écart de délai supérieur au seuil (analyse IA)"))
                .toList();

        return new DashboardResponse(byStatus, averageDelay, averageCost, anomalyRate, volumeTrend, costTrend, alerts);
    }

    private BigDecimal sumCost(List<Operation> operations, OperationType type) {
        return operations.stream()
                .filter(o -> o.getType() == type)
                .map(o -> o.getActualCost() != null ? o.getActualCost() : o.getPlannedCost())
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
