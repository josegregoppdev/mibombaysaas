package com.josegregoppdev.mibombay.mapper.empresa;

import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTORequest;
import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTOResponse;
import com.josegregoppdev.mibombay.model.empresa.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmpresaMapper {

    Empresa toEntity(EmpresaDTORequest dto);

    EmpresaDTOResponse toResponse(Empresa empresa);
}
