package com.cimelect.dto.document;

import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;
import jakarta.validation.constraints.NotNull;

public record RequiredDocumentRequest(
        @NotNull OperationType operationType,
        @NotNull DocumentType documentType,
        @NotNull Boolean required
) {
}
