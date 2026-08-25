package com.cimelect.controller;

import com.cimelect.dto.document.DocumentResponse;
import com.cimelect.dto.document.RequiredDocumentRequest;
import com.cimelect.dto.document.RequiredDocumentResponse;
import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;
import com.cimelect.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public List<DocumentResponse> findAll() {
        return documentService.findAll();
    }

    @GetMapping("/operations/{operationId}/documents")
    public List<DocumentResponse> byOperation(@PathVariable Long operationId) {
        return documentService.findByOperation(operationId);
    }

    @PostMapping(value = "/operations/{operationId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public DocumentResponse upload(
            @PathVariable Long operationId,
            @RequestParam DocumentType type,
            @RequestParam("file") MultipartFile file
    ) {
        return documentService.upload(operationId, type, file);
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        var document = documentService.get(id);
        Resource resource = documentService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(document.getContentType() != null ? document.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(resource);
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'AGENT_IMPORT_EXPORT')")
    public void delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirmed) {
        documentService.delete(id, confirmed);
    }

    @GetMapping("/document-requirements")
    public List<RequiredDocumentResponse> requirements(@RequestParam(required = false) OperationType type) {
        return documentService.listRequirements(type);
    }

    @PutMapping("/document-requirements")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public RequiredDocumentResponse upsertRequirement(@Valid @RequestBody RequiredDocumentRequest request) {
        return documentService.upsertRequirement(request);
    }
}
