package com.josegregoppdev.mibombay.mapper.recipe;

import com.josegregoppdev.mibombay.dto.recipe.RecipeDTO;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecipeMapper {

    Recipe toEntity(RecipeDTO dto);

    RecipeDTO toDto(Recipe recipe);
}
