package com.josegregoppdev.mibombay.mapper.ingrediente;

import com.josegregoppdev.mibombay.dto.ingrediente.CategoriaDTO;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoriaMapper {

    Categoria toEntity(CategoriaDTO dto);

    CategoriaDTO toDto(Categoria categoria);
}
