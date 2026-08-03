package com.josegregoppdev.mibombay.dto.recipe;

import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RecipeDetailDTO {

    private Long id;

    @NotNull(message = "The ingredient is required")
    private Long ingredientId;

    private String ingredientName;

    @NotNull(message = "The quantity is required")
    @Positive(message = "The quantity must be greater than zero")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal quantity;

    private UnitOfMeasure unitOfMeasure;

    @PositiveOrZero(message = "The unit cost cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal unitCost;

    @PositiveOrZero(message = "The total cost cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal totalCost;

    @Size(max = 200)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,''()/:;\\-&+]*$", message = "The notes contain disallowed characters")
    private String notes;
}
