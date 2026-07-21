package com.josegregoppdev.mibombay.model.usuario;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "email"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 40)
    private String tenantId;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(length = 30)
    private String telefono;

    @Column(name = "documento_hash", nullable = false, length = 255)
    private String documentoHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "debe_cambiar_password", nullable = false)
    @Builder.Default
    private Boolean debeCambiarPassword = false;

    @Column(name = "ultimo_cambio_password")
    private LocalDateTime ultimoCambioPassword;
}
