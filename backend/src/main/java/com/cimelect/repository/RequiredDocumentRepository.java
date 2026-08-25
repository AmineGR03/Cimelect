package com.cimelect.repository;

import com.cimelect.entity.RequiredDocument;
import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequiredDocumentRepository extends JpaRepository<RequiredDocument, Long> {

    List<RequiredDocument> findByOperationTypeAndRequiredTrue(OperationType operationType);

    List<RequiredDocument> findByOperationType(OperationType operationType);

    Optional<RequiredDocument> findByOperationTypeAndDocumentType(OperationType operationType, DocumentType documentType);
}
