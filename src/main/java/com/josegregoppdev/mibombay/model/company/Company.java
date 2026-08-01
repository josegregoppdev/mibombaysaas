package com.josegregoppdev.mibombay.model.company;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 40)
    private String tenantId;

    @Column(nullable = false, unique = true, length = 60)
    private String subdomain;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "manager_name", nullable = false, length = 150)
    private String managerName;

    @Column(name = "manager_document_hash", nullable = false, length = 255)
    private String managerDocumentHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
