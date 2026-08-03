package com.josegregoppdev.mibombay.service.recipe;

import com.josegregoppdev.mibombay.dto.recipe.RecipeDTO;
import com.josegregoppdev.mibombay.dto.recipe.RecipeDetailDTO;
import com.josegregoppdev.mibombay.mapper.recipe.RecipeMapper;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.model.recipe.RecipeDetail;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.recipe.RecipeRepository;
import com.josegregoppdev.mibombay.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final IngredientRepository ingredientRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getPaginatedRecipes(String tenantId, String name, Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? name : null;
        return recipeRepository.findByFilters(tenantId, nameParam, pageable)
                .map(recipeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> getPaginatedRecipes(String tenantId, Pageable pageable) {
        return getPaginatedRecipes(tenantId, null, pageable);
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id, String tenantId) {
        Recipe recipe = recipeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        RecipeDTO dto = recipeMapper.toDto(recipe);
        dto.setDetails(mapDetailsToDto(recipe));
        return dto;
    }

    @Transactional
    public RecipeDTO createNewRecipe(RecipeDTO dto, String tenantId) {
        if (recipeRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("A recipe with that code already exists");
        }
        if (recipeRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("A recipe with that name already exists");
        }

        Recipe recipe = recipeMapper.toEntity(dto);
        recipe.setTenantId(tenantId);
        recipe.setActive(true);
        recipe.setDetails(new ArrayList<>());

        if (dto.getDetails() != null) {
            for (RecipeDetailDTO detailDto : dto.getDetails()) {
                if (detailDto.getIngredientId() == null || detailDto.getQuantity() == null) {
                    continue;
                }
                Ingredient ingredient = ingredientRepository.findById(detailDto.getIngredientId())
                        .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + detailDto.getIngredientId()));

                RecipeDetail detail = new RecipeDetail();
                detail.setRecipe(recipe);
                detail.setIngredient(ingredient);
                detail.setQuantity(detailDto.getQuantity());
                detail.setUnitOfMeasure(ingredient.getUnitOfMeasure());
                detail.setUnitCost(ingredient.getCurrentUnitCost());
                detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                detail.setNotes(detailDto.getNotes());
                recipe.getDetails().add(detail);
            }
        }

        recipe.setProductionCost(calculateProductionCost(recipe.getDetails()));
        recipe = recipeRepository.save(recipe);
        return recipeMapper.toDto(recipe);
    }

    @Transactional
    public RecipeDTO updateExistingRecipe(Long id, RecipeDTO dto, String tenantId) {
        Recipe recipe = recipeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        if (!recipe.getCode().equals(dto.getCode())
                && recipeRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("A recipe with that code already exists");
        }
        if (!recipe.getName().equals(dto.getName())
                && recipeRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("A recipe with that name already exists");
        }

        recipe.setCode(dto.getCode());
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());

        recipe.getDetails().clear();

        if (dto.getDetails() != null) {
            for (RecipeDetailDTO detailDto : dto.getDetails()) {
                if (detailDto.getIngredientId() == null || detailDto.getQuantity() == null) {
                    continue;
                }
                Ingredient ingredient = ingredientRepository.findById(detailDto.getIngredientId())
                        .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + detailDto.getIngredientId()));

                RecipeDetail detail = new RecipeDetail();
                detail.setRecipe(recipe);
                detail.setIngredient(ingredient);
                detail.setQuantity(detailDto.getQuantity());
                detail.setUnitOfMeasure(ingredient.getUnitOfMeasure());
                detail.setUnitCost(ingredient.getCurrentUnitCost());
                detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                detail.setNotes(detailDto.getNotes());
                recipe.getDetails().add(detail);
            }
        }

        recipe.setProductionCost(calculateProductionCost(recipe.getDetails()));
        recipe = recipeRepository.save(recipe);
        productService.syncProductCostsForRecipe(recipe.getId(), tenantId, recipe.getProductionCost());
        return recipeMapper.toDto(recipe);
    }

    @Transactional
    public void toggleRecipeActiveStatus(Long id, String tenantId) {
        Recipe recipe = recipeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
        recipe.setActive(!recipe.getActive());
        recipeRepository.save(recipe);
    }

    private BigDecimal calculateProductionCost(List<RecipeDetail> details) {
        return details.stream()
                .map(RecipeDetail::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<RecipeDetailDTO> mapDetailsToDto(Recipe recipe) {
        List<RecipeDetailDTO> details = new ArrayList<>();
        for (RecipeDetail detail : recipe.getDetails()) {
            RecipeDetailDTO detailDto = new RecipeDetailDTO();
            detailDto.setId(detail.getId());
            detailDto.setIngredientId(detail.getIngredient().getId());
            detailDto.setIngredientName(detail.getIngredient().getName());
            detailDto.setQuantity(detail.getQuantity());
            detailDto.setUnitOfMeasure(detail.getUnitOfMeasure());
            detailDto.setUnitCost(detail.getUnitCost());
            detailDto.setTotalCost(detail.getTotalCost());
            detailDto.setNotes(detail.getNotes());
            details.add(detailDto);
        }
        return details;
    }
}
