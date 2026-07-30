package com.josegregoppdev.mibombay.mapper.ingrediente;

import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngredienteMapper {

    Ingrediente toEntity(IngredienteDTO dto);

    IngredienteDTO toDto(Ingrediente ingrediente);
}
