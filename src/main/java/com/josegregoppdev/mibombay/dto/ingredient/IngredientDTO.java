package com.josegregoppdev.mibombay.dto.ingredient;

import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDTO {

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

    private Category category;

    @NotNull(message = "The unit of measure is required")
    private UnitOfMeasure unitOfMeasure;

    @PositiveOrZero(message = "The unit cost cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal currentUnitCost;

    @PositiveOrZero(message = "The current stock cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal currentStock;

    @PositiveOrZero(message = "The minimum stock cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal minimumStock;

    private Boolean active;
}
