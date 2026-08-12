package com.josegregoppdev.mibombay.model.movement;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movements", indexes = {
        @Index(name = "idx_movement_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_movement_tenant_type_date", columnList = "tenant_id,type,date"),
        @Index(name = "idx_movement_ingredient", columnList = "ingredient_id"),
        @Index(name = "idx_movement_product", columnList = "product_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "previous_stock", precision = 12, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal previousStock = BigDecimal.ZERO;

    @Column(name = "new_stock", precision = 12, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal newStock = BigDecimal.ZERO;

    @Column(precision = 12, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(length = 500)
    private String observations;

    @Column(name = "user_id")
    private Long userId;

    @PrePersist
    @PreUpdate
    private void validateItemReference() {
        boolean hasIngredient = ingredientId != null;
        boolean hasProduct = productId != null;
        if (!(hasIngredient ^ hasProduct)) {
            throw new IllegalStateException(
                "Exactly one of ingredientId or productId must be set (XOR constraint)");
        }
    }
}
