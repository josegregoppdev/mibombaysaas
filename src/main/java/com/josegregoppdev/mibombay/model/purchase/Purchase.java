package com.josegregoppdev.mibombay.model.purchase;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases", indexes = {
        @Index(name = "idx_purchase_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_purchase_tenant_date", columnList = "tenant_id,purchase_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 500)
    private String observations;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseDetail> details = new ArrayList<>();
}