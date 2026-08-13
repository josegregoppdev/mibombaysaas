package com.josegregoppdev.mibombay.dto.sale;

import jakarta.validation.Valid;
import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CartSubmissionDTO {

    private List<@Valid SaleDetailDTO> items = new ArrayList<>();

    private Long customerId;

    private PaymentMethod paymentMethod;

    private String observations;

    private BigDecimal amountReceived;
}