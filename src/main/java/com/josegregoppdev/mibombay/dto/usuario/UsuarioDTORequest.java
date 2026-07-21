package com.josegregoppdev.mibombay.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTORequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'’.-]+$", message = "El nombre contiene caracteres no permitidos")
    private String nombreCompleto;

    @Pattern(regexp = "^[+]?[\\d\\s()-]{6,30}$", message = "El teléfono contiene caracteres no permitidos")
    @Size(max = 30)
    private String telefono;

    @NotBlank(message = "El documento es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "El documento solo permite letras, números y guiones")
    private String documento;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "^(ADMIN|CAJERO)$", message = "El rol debe ser ADMIN o CAJERO")
    private String rol;
}
