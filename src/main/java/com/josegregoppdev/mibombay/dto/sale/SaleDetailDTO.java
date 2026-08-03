package com.josegregoppdev.mibombay.dto.sale;

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
public class SaleDetailDTO {

    private Long id;

    private Long productId;

    private String productName;

    private Long comboId;

    private String comboName;

    @NotNull(message = "The quantity is required")
    @Positive(message = "The quantity must be greater than zero")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal quantity;

    private BigDecimal salePrice;

    private BigDecimal unitCost;

    private BigDecimal totalPrice;

    private String notes;
}
