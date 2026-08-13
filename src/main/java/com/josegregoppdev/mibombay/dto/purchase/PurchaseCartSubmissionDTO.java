package com.josegregoppdev.mibombay.dto.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PurchaseCartSubmissionDTO {

    private List<@Valid PurchaseDetailDTO> items = new ArrayList<>();

    @NotNull(message = "The supplier is required")
    private Long supplierId;

    private String observations;

    @NotBlank(message = "The invoice number is required")
    private String invoiceNumber;

    @NotNull(message = "The purchase date is required")
    private LocalDate purchaseDate;
}