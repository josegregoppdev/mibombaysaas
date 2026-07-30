package com.josegregoppdev.mibombay.mapper.ingrediente;

import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngredienteMapper {

    @Mapping(target = "categoria", source = "categoriaId", qualifiedByName = "categoriaFromId")
    Ingrediente toEntity(IngredienteDTO dto);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    IngredienteDTO toDto(Ingrediente ingrediente);

    @Named("categoriaFromId")
    default Categoria categoriaFromId(Long id) {
        if (id == null) return null;
        return Categoria.builder().id(id).build();
    }
}
