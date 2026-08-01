package com.josegregoppdev.mibombay.dto.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RecipeDTO {

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
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,''():;\\-]*$", message = "The description contains disallowed characters")
    private String description;

    @PositiveOrZero(message = "The production cost cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal productionCost;

    private Boolean active;

    @Valid
    @Builder.Default
    private List<RecipeDetailDTO> details = new ArrayList<>();
}
