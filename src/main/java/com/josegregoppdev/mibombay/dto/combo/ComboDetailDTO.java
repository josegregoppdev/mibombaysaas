package com.josegregoppdev.mibombay.dto.combo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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
public class ComboDetailDTO {

    private Long id;

    @NotNull(message = "The product is required")
    private Long productId;

    private String productName;

    @NotNull(message = "The quantity is required")
    @Positive(message = "The quantity must be greater than zero")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal quantity;

    private BigDecimal unitCost;

    private BigDecimal totalCost;

    @Size(max = 200)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ0-9\\s.,''()/:;\\-&+]*$", message = "The notes contain disallowed characters")
    private String notes;
}
