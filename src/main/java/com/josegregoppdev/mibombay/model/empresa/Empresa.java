package com.josegregoppdev.mibombay.model.empresa;

import com.josegregoppdev.mibombay.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "El subdominio es obligatorio")
    @Size(min = 3, max = 60, message = "El subdominio debe tener entre 3 y 60 caracteres")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Solo se permiten letras minúsculas, números y guiones")
    @Column(nullable = false, unique = true, length = 60)
    private String subdominio;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Column(nullable = false, length = 150)
    private String correo;

    @Size(max = 30)
    @Column(length = 30)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String direccion;

    @NotBlank(message = "El nombre del encargado es obligatorio")
    @Size(max = 150)
    @Column(name = "nombre_encargado", nullable = false, length = 150)
    private String nombreEncargado;

    @Column(name = "documento_encargado_hash", nullable = false, length = 255)
    private String documentoEncargadoHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Transient
    private String documentoEncargado;

    @Transient
    @NotBlank(message = "El email del encargado es obligatorio")
    @Email(message = "El email del encargado no es válido")
    private String emailEncargado;

    @Transient
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String passwordEncargado;

    @Transient
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPasswordEncargado;

    @Transient
    @Size(max = 30)
    private String telefonoEncargado;
}
