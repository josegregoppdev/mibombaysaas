package com.josegregoppdev.mibombay.dto.sale;

import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import com.josegregoppdev.mibombay.model.sale.SaleState;
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

    private LocalDateTime saleDate;

    private BigDecimal total;

    private SaleState state;

    private PaymentMethod paymentMethod;

    private Long cashierId;

    private String cashierName;

    private String observations;

    @Builder.Default
    private List<SaleDetailDTO> details = new ArrayList<>();
}
