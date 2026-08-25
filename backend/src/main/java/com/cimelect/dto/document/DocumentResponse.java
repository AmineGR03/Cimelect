package com.cimelect.dto.document;

import com.cimelect.enums.DocumentType;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        Long operationId,
        String operationReference,
        DocumentType type,
        String originalFilename,
        String contentType,
        Long fileSize,
        Instant uploadedAt
) {
}
