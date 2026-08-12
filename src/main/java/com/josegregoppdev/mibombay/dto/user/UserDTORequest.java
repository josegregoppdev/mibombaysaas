package com.josegregoppdev.mibombay.dto.user;

import jakarta.validation.Valid;
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
public class UserDTORequest {

    @NotBlank(message = "The email is required")
    @Email(message = "The email is not valid")
    private String email;

    @NotBlank(message = "The password is required")
    @Size(min = 8, message = "The password must be at least 8 characters")
    private String password;

    @NotBlank(message = "The full name is required")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s''.-]+$", message = "The name contains disallowed characters")
    private String fullName;

    @Pattern(regexp = "^[+]?[\\d\\s()-]{6,30}$", message = "The phone contains disallowed characters")
    @Size(max = 30)
    private String phone;

    @NotBlank(message = "The document is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "The document only allows letters, numbers, and hyphens")
    private String document;

    @NotBlank(message = "The role is required")
    @Pattern(regexp = "^(ADMIN|CASHIER)$", message = "The role must be ADMIN or CASHIER")
    private String role;
}
