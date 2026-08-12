package com.josegregoppdev.mibombay.mapper.supplier;

import com.josegregoppdev.mibombay.dto.supplier.SupplierDTO;
import com.josegregoppdev.mibombay.model.supplier.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    SupplierDTO toDto(Supplier entity);

    Supplier toEntity(SupplierDTO dto);
}