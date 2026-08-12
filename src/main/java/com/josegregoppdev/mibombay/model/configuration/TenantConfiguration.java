package com.josegregoppdev.mibombay.model.configuration;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenant_configurations", indexes = {
        @Index(name = "idx_tenant_config_tenant_id", columnList = "tenant_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfiguration extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40, unique = true)
    private String tenantId;

    @Column(name = "allow_negative_inventory", nullable = false)
    @Builder.Default
    private Boolean allowNegativeInventory = true;
}
