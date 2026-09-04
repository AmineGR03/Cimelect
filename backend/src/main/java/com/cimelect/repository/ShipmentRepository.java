package com.cimelect.repository;

import com.cimelect.entity.Shipment;
import com.cimelect.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = {"operation"})
    List<Shipment> findAll();

    @EntityGraph(attributePaths = {"operation"})
    Optional<Shipment> findById(Long id);

    @EntityGraph(attributePaths = {"operation"})
    List<Shipment> findByStatusNot(ShipmentStatus status);

    List<Shipment> findByOperationId(Long operationId);
}
