package com.cimelect.entity;

import com.cimelect.enums.DocumentType;
import com.cimelect.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "required_documents",
        uniqueConstraints = @UniqueConstraint(columnNames = {"operation_type", "document_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequiredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Builder.Default
    private boolean required = true;
}
