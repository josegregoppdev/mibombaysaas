package com.josegregoppdev.mibombay.dto.purchase;

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
public class PurchaseDTO {

    private Long id;

    private Long supplierId;

    private String supplierName;

    private LocalDateTime purchaseDate;

    private String invoiceNumber;

    private BigDecimal total;

    private Long userId;

    private String userName;

    private String observations;

    private Boolean active;

    private List<PurchaseDetailDTO> details = new ArrayList<>();
}