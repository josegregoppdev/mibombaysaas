package com.josegregoppdev.mibombay.mapper.sale;

import com.josegregoppdev.mibombay.dto.sale.SaleDTO;
import com.josegregoppdev.mibombay.model.sale.Sale;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SaleMapper {

    Sale toEntity(SaleDTO dto);

    SaleDTO toDto(Sale sale);
}
