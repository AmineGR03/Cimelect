package com.cimelect.entity;

import com.cimelect.enums.OperationStatus;
import com.cimelect.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String destination;

    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDate actualDate;

    private String carrier;

    @Column(precision = 19, scale = 2)
    private BigDecimal plannedCost;

    @Column(precision = 19, scale = 2)
    private BigDecimal actualCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationLine> lines = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL)
    private List<Shipment> shipments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL)
    private List<Document> documents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    private boolean deleted = false;

    private String deletionJustification;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (status == null) {
            status = OperationStatus.CREEE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isClosed() {
        return status == OperationStatus.CLOTUREE;
    }

    public void addLine(OperationLine line) {
        lines.add(line);
        line.setOperation(this);
    }
}
