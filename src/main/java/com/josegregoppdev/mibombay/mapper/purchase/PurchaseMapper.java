package com.josegregoppdev.mibombay.mapper.purchase;

import com.josegregoppdev.mibombay.dto.purchase.PurchaseDetailDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDTO;
import com.josegregoppdev.mibombay.model.purchase.Purchase;
import com.josegregoppdev.mibombay.model.purchase.PurchaseDetail;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseMapper {

    PurchaseDTO toDto(Purchase purchase);

    PurchaseDetailDTO toDetailDto(PurchaseDetail detail);
}