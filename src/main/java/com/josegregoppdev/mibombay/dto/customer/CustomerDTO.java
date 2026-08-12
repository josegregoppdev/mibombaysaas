package com.josegregoppdev.mibombay.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "The full name is required")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s''.-]+$", message = "The name contains disallowed characters")
    private String fullName;

    @NotBlank(message = "The document is required")
    @Size(max = 20)
    @Pattern(regexp = "^[0-9]+$", message = "The document only allows numbers")
    private String document;

    @Size(max = 20)
    @Pattern(regexp = "^[+]?[\\d\\s()-]{6,20}$", message = "The phone contains disallowed characters")
    private String phone;

    @Size(max = 255)
    private String address;

    private Boolean active;

    private Boolean isDefault;

    private String documentMasked;

    private String phoneMasked;
}
