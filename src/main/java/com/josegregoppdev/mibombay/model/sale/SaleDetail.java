package com.josegregoppdev.mibombay.model.sale;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "combo_id")
    private Long comboId;

    @Column(name = "combo_name", length = 150)
    private String comboName;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @Column(name = "sale_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal salePrice;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalPrice;

    @Column(length = 200)
    private String notes;
}
