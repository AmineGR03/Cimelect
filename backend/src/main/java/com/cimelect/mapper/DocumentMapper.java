package com.cimelect.mapper;

import com.cimelect.dto.document.DocumentResponse;
import com.cimelect.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getOperation().getId(),
                document.getOperation().getReference(),
                document.getType(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getUploadedAt()
        );
    }
}
