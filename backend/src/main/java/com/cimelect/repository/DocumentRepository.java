package com.cimelect.repository;

import com.cimelect.entity.Document;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {"operation"})
    Optional<Document> findById(Long id);

    @EntityGraph(attributePaths = {"operation"})
    List<Document> findAll();

    @EntityGraph(attributePaths = {"operation"})
    List<Document> findByOperationId(Long operationId);
}
