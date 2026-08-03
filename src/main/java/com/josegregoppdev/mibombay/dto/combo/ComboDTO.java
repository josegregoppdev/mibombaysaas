package com.josegregoppdev.mibombay.dto.combo;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboDTO {

    private Long id;

    @NotBlank(message = "The code is required")
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "The code only allows letters, numbers, and hyphens")
    private String code;

    @NotBlank(message = "The name is required")
    @Size(max = 150)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s''.-]+$", message = "The name contains disallowed characters")
    private String name;

    @Size(max = 500)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,''()/:;\\-&+]*$", message = "The description contains disallowed characters")
    private String description;

    @NotNull(message = "The selling price is required")
    @PositiveOrZero(message = "The selling price cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal sellingPrice;

    private BigDecimal totalCost;

    private Boolean active;

    @Builder.Default
    private List<ComboDetailDTO> details = new ArrayList<>();
}
