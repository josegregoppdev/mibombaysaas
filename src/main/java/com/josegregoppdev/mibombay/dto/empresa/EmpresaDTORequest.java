package com.josegregoppdev.mibombay.dto.empresa;

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
public class EmpresaDTORequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 150)
    private String nombre;

    @NotBlank(message = "El subdominio es obligatorio")
    @Size(min = 3, max = 60, message = "El subdominio debe tener entre 3 y 60 caracteres")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Solo se permiten letras minúsculas, números y guiones")
    private String subdominio;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 150)
    private String correo;

    @Size(max = 30)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String direccion;

    @NotBlank(message = "El nombre del encargado es obligatorio")
    @Size(max = 150)
    private String nombreEncargado;

    @NotBlank(message = "El documento es obligatorio")
    private String documentoEncargado;

    @NotBlank(message = "El email del encargado es obligatorio")
    @Email(message = "El email del encargado no es válido")
    private String emailEncargado;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String passwordEncargado;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmarPasswordEncargado;

    @Size(max = 30)
    private String telefonoEncargado;
}
