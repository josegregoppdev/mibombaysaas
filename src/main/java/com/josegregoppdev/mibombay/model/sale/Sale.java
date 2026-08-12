package com.josegregoppdev.mibombay.model.sale;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import com.josegregoppdev.mibombay.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Column(name = "sale_date", nullable = false)
    private java.time.LocalDateTime saleDate;

    @Column(nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleState state;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "amount_received", precision = 12, scale = 4)
    private BigDecimal amountReceived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(length = 500)
    private String observations;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleDetail> details = new ArrayList<>();
}
