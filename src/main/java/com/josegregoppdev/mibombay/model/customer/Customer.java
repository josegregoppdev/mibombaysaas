package com.josegregoppdev.mibombay.model.customer;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_customer_tenant_name", columnList = "tenant_id, full_name"),
        @Index(name = "idx_customer_tenant_document_hash", columnList = "tenant_id, document_encrypted"),
        @Index(name = "idx_customer_tenant_phone_hash", columnList = "tenant_id, phone_encrypted")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "document_encrypted", nullable = false, length = 500)
    private String documentEncrypted;

    @Column(name = "phone_encrypted", length = 500)
    private String phoneEncrypted;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "document_lookup_hash", nullable = false, length = 64)
    private String documentLookupHash;

    @Column(name = "phone_lookup_hash", length = 64)
    private String phoneLookupHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
