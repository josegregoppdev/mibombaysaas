package com.josegregoppdev.mibombay.dto.purchase;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetailDTO {

    private Long id;

    private Long ingredientId;

    private Long productId;

    private String itemName;

    @NotNull(message = "The quantity is required")
    @Positive(message = "The quantity must be greater than zero")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal quantity;

    @NotNull(message = "The unit cost is required")
    @Positive(message = "The unit cost must be greater than zero")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal unitCost;

    private BigDecimal totalCost;
}