package com.josegregoppdev.mibombay.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

    private Long id;

    @NotBlank(message = "The name is required")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s''.,&/-]+$", message = "The name contains disallowed characters")
    private String name;

    @NotBlank(message = "The document is required")
    @Size(max = 20)
    @Pattern(regexp = "^[0-9]+$", message = "The document only allows numbers")
    private String document;

    @Size(max = 20)
    @Pattern(regexp = "^[+]?[\\d\\s()-]{6,20}$", message = "The phone contains disallowed characters")
    private String phone;

    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-Z0-9@._-]*$", message = "The email contains disallowed characters")
    private String email;

    @Size(max = 255)
    private String address;

    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s''.-]*$", message = "The contact name contains disallowed characters")
    private String contactName;

    private Boolean active;

    private Boolean isDefault;

    private String documentMasked;

    private String phoneMasked;
}