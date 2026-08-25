package com.cimelect.repository;

import com.cimelect.entity.Operation;
import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.product", "supplier", "customer"})
    List<Operation> findByTypeAndDeletedFalseOrderByCreatedAtDesc(OperationType type);

    @EntityGraph(attributePaths = {"lines", "lines.product", "supplier", "customer"})
    Optional<Operation> findByIdAndDeletedFalse(Long id);

    boolean existsBySupplierIdAndDeletedFalseAndStatusNot(Long supplierId, OperationStatus status);

    boolean existsByCustomerIdAndDeletedFalseAndStatusNot(Long customerId, OperationStatus status);

    @EntityGraph(attributePaths = {"lines", "lines.product", "supplier", "customer"})
    List<Operation> findBySupplierIdAndDeletedFalseOrderByCreatedAtDesc(Long supplierId);

    @EntityGraph(attributePaths = {"lines", "lines.product", "supplier", "customer"})
    List<Operation> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long customerId);

    @Query("select o from Operation o where o.deleted = false and o.status <> com.cimelect.enums.OperationStatus.CLOTUREE")
    List<Operation> findActive();

    long countByTypeAndDeletedFalseAndStatus(OperationType type, OperationStatus status);

    List<Operation> findByDeletedFalse();

    @Query("select count(o) from Operation o where o.reference like concat(:prefix, '%')")
    long countByReferencePrefix(@Param("prefix") String prefix);
}
