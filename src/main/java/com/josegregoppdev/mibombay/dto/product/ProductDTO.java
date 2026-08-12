package com.josegregoppdev.mibombay.dto.product;

import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import jakarta.validation.Valid;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

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

    private ProductCategory category;

    @NotNull(message = "The product type is required")
    private ProductType productType;

    @NotNull(message = "The selling price is required")
    @PositiveOrZero(message = "The selling price cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal sellingPrice;

    @PositiveOrZero(message = "The unit cost cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal unitCost;

    @PositiveOrZero(message = "The current stock cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal currentStock;

    @PositiveOrZero(message = "The minimum stock cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal minimumStock;

    private Long recipeId;

    private String recipeName;

    private BigDecimal recipeProductionCost;

    private Boolean active;
}
