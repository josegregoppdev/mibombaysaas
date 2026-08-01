package com.josegregoppdev.mibombay.dto.company;

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
public class CompanyDTORequest {

    @NotBlank(message = "The company name is required")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,''&-]+$", message = "The name contains disallowed characters")
    private String name;

    @NotBlank(message = "The subdomain is required")
    @Size(min = 3, max = 60, message = "The subdomain must be between 3 and 60 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Only lowercase letters, numbers, and hyphens are allowed")
    private String subdomain;

    @NotBlank(message = "The email is required")
    @Email(message = "The email is not valid")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[+]?[\\d\\s()-]{6,30}$", message = "The phone contains disallowed characters")
    @Size(max = 30)
    private String phone;

    @NotBlank(message = "The address is required")
    @Size(max = 255)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,#°/\\-'''()]+$", message = "The address contains disallowed characters")
    private String address;

    @NotBlank(message = "The manager name is required")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s''.-]+$", message = "The manager name contains disallowed characters")
    private String managerName;

    @NotBlank(message = "The document is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "The document only allows letters, numbers, and hyphens")
    private String managerDocument;

    @NotBlank(message = "The manager email is required")
    @Email(message = "The manager email is not valid")
    private String managerEmail;

    @NotBlank(message = "The password is required")
    @Size(min = 8, message = "The password must be at least 8 characters")
    private String managerPassword;

    @NotBlank(message = "The password confirmation is required")
    private String confirmManagerPassword;

    @Pattern(regexp = "^[+]?[\\d\\s()-]{6,30}$", message = "The phone contains disallowed characters")
    @Size(max = 30)
    private String managerPhone;
}
