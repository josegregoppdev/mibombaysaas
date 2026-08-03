package com.josegregoppdev.mibombay.model.combo;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "combos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Combo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComboDetail> details = new ArrayList<>();
}
