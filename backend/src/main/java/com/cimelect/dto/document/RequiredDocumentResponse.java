package com.cimelect.dto.document;

import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;

public record RequiredDocumentResponse(
        Long id,
        OperationType operationType,
        DocumentType documentType,
        boolean required
) {
}
