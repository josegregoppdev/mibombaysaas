package com.josegregoppdev.mibombay.dto.sale;

import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CartSubmissionDTO {

    private List<SaleDetailDTO> items = new ArrayList<>();

    private PaymentMethod paymentMethod;

    private String observations;
}