package com.josegregoppdev.mibombay.dto.sale;

import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import com.josegregoppdev.mibombay.model.sale.SaleState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDTO {

    private Long id;

    @NotNull(message = "The sale date is required")
    private LocalDateTime saleDate;

    @NotNull(message = "The total is required")
    @PositiveOrZero(message = "The total cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal total;

    @NotNull(message = "The sale state is required")
    private SaleState state;

    private PaymentMethod paymentMethod;

    private Long cashierId;

    private String cashierName;

    private Long customerId;

    private String customerName;

    @Size(max = 500)
    private String observations;

    @PositiveOrZero(message = "The amount received cannot be negative")
    @Digits(integer = 8, fraction = 4, message = "Maximum 8 integer digits and 4 decimals")
    private BigDecimal amountReceived;

    @Builder.Default
    private List<@Valid SaleDetailDTO> details = new ArrayList<>();
}
