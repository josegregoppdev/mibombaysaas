package com.josegregoppdev.mibombay.model.purchase;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_details", indexes = {
        @Index(name = "idx_purchase_detail_purchase", columnList = "purchase_id"),
        @Index(name = "idx_purchase_detail_ingredient", columnList = "ingredient_id"),
        @Index(name = "idx_purchase_detail_product", columnList = "product_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetail extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @Column(name = "ingredient_id")
    private Long ingredientId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "previous_stock", precision = 12, scale = 4)
    private BigDecimal previousStock;

    @Column(name = "previous_unit_cost", precision = 12, scale = 4)
    private BigDecimal previousUnitCost;

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