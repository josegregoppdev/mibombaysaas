package com.josegregoppdev.mibombay.mapper.ingredient;

import com.josegregoppdev.mibombay.dto.ingredient.IngredientDTO;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IngredientMapper {

    Ingredient toEntity(IngredientDTO dto);

    IngredientDTO toDto(Ingredient ingredient);
}
