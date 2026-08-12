package com.josegregoppdev.mibombay.model.supplier;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_supplier_tenant_name", columnList = "tenant_id, name"),
        @Index(name = "idx_supplier_tenant_document_hash", columnList = "tenant_id, document_lookup_hash")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "document_encrypted", nullable = false, length = 500)
    private String documentEncrypted;

    @Column(name = "document_lookup_hash", nullable = false, length = 64)
    private String documentLookupHash;

    @Column(name = "phone_encrypted", length = 500)
    private String phoneEncrypted;

    @Column(name = "phone_lookup_hash", length = 64)
    private String phoneLookupHash;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(name = "contact_name", length = 150)
    private String contactName;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}