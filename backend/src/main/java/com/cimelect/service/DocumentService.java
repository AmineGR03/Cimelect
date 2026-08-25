package com.cimelect.service;

import com.cimelect.config.AppProperties;
import com.cimelect.dto.document.DocumentResponse;
import com.cimelect.dto.document.RequiredDocumentRequest;
import com.cimelect.dto.document.RequiredDocumentResponse;
import com.cimelect.entity.Document;
import com.cimelect.entity.Operation;
import com.cimelect.entity.RequiredDocument;
import com.cimelect.enums.AuditAction;
import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;
import com.cimelect.exception.BusinessException;
import com.cimelect.exception.ResourceNotFoundException;
import com.cimelect.mapper.DocumentMapper;
import com.cimelect.repository.DocumentRepository;
import com.cimelect.repository.OperationRepository;
import com.cimelect.repository.RequiredDocumentRepository;
import com.cimelect.security.CurrentUserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OperationRepository operationRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final DocumentMapper documentMapper;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AppProperties appProperties;

    public DocumentService(
            DocumentRepository documentRepository,
            OperationRepository operationRepository,
            RequiredDocumentRepository requiredDocumentRepository,
            DocumentMapper documentMapper,
            CurrentUserService currentUserService,
            AuditService auditService,
            AppProperties appProperties
    ) {
        this.documentRepository = documentRepository;
        this.operationRepository = operationRepository;
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.documentMapper = documentMapper;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findAll() {
        return documentRepository.findAll().stream().map(documentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findByOperation(Long operationId) {
        return documentRepository.findByOperationId(operationId).stream().map(documentMapper::toResponse).toList();
    }

    @Transactional
    public DocumentResponse upload(Long operationId, DocumentType type, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Fichier obligatoire");
        }
        Operation operation = operationRepository.findByIdAndDeletedFalse(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable"));
        if (operation.isClosed()) {
            throw new BusinessException("Impossible d'ajouter un document à une opération clôturée");
        }
        Path stored = store(file);
        Document document = documentRepository.save(Document.builder()
                .operation(operation)
                .type(type)
                .originalFilename(file.getOriginalFilename())
                .storagePath(stored.toString())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(currentUserService.requireUser())
                .build());
        auditService.log(currentUserService.requireUser(), "Document", document.getId(), AuditAction.DOCUMENT_UPLOAD,
                "Document " + type + " associé à " + operation.getReference());
        return documentMapper.toResponse(document);
    }

    @Transactional(readOnly = true)
    public Resource download(Long id) {
        Document document = get(id);
        return new FileSystemResource(document.getStoragePath());
    }

    @Transactional(readOnly = true)
    public Document get(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable"));
    }

    @Transactional
    public void delete(Long id, boolean confirmed) {
        if (!confirmed) {
            throw new BusinessException("La suppression d'un document nécessite une confirmation explicite (RG18)");
        }
        Document document = get(id);
        documentRepository.delete(document);
        auditService.log(currentUserService.requireUser(), "Document", id, AuditAction.DOCUMENT_DELETE,
                "Suppression " + document.getOriginalFilename(), "Confirmation utilisateur");
    }

    @Transactional(readOnly = true)
    public List<RequiredDocumentResponse> listRequirements(OperationType type) {
        List<RequiredDocument> items = type == null
                ? requiredDocumentRepository.findAll()
                : requiredDocumentRepository.findByOperationType(type);
        return items.stream()
                .map(r -> new RequiredDocumentResponse(r.getId(), r.getOperationType(), r.getDocumentType(), r.isRequired()))
                .toList();
    }

    @Transactional
    public RequiredDocumentResponse upsertRequirement(RequiredDocumentRequest request) {
        RequiredDocument entity = requiredDocumentRepository
                .findByOperationTypeAndDocumentType(request.operationType(), request.documentType())
                .orElseGet(() -> RequiredDocument.builder()
                        .operationType(request.operationType())
                        .documentType(request.documentType())
                        .build());
        entity.setRequired(request.required());
        RequiredDocument saved = requiredDocumentRepository.save(entity);
        return new RequiredDocumentResponse(saved.getId(), saved.getOperationType(), saved.getDocumentType(), saved.isRequired());
    }

    private Path store(MultipartFile file) {
        try {
            Path dir = Paths.get(appProperties.upload().dir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException ex) {
            throw new BusinessException("Impossible d'enregistrer le fichier");
        }
    }
}
