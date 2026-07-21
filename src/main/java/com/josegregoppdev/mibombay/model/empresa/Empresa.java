package com.josegregoppdev.mibombay.model.empresa;

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
@Table(name = "empresas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empresa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 40)
    private String tenantId;

    @Column(nullable = false, unique = true, length = 60)
    private String subdominio;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(name = "nombre_encargado", nullable = false, length = 150)
    private String nombreEncargado;

    @Column(name = "documento_encargado_hash", nullable = false, length = 255)
    private String documentoEncargadoHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
