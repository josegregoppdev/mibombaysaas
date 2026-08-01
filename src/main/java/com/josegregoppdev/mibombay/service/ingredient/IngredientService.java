package com.josegregoppdev.mibombay.service.ingredient;

import com.josegregoppdev.mibombay.dto.ingredient.IngredientDTO;
import com.josegregoppdev.mibombay.mapper.ingredient.IngredientMapper;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    @Transactional(readOnly = true)
    public Page<IngredientDTO> getPaginatedIngredients(String tenantId, String name,
                                                        Category category,
                                                        UnitOfMeasure unitOfMeasure,
                                                        Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? name : null;
        return ingredientRepository.findByFilters(tenantId, nameParam, category, unitOfMeasure, pageable)
                .map(ingredientMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<IngredientDTO> getPaginatedIngredients(String tenantId, Pageable pageable) {
        return getPaginatedIngredients(tenantId, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<IngredientDTO> getPaginatedActiveIngredients(String tenantId, Pageable pageable) {
        return ingredientRepository.findByTenantIdAndActiveTrue(tenantId, pageable)
                .map(ingredientMapper::toDto);
    }

    @Transactional(readOnly = true)
    public IngredientDTO getIngredientById(Long id, String tenantId) {
        Ingredient ingredient = ingredientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        return ingredientMapper.toDto(ingredient);
    }

    @Transactional
    public IngredientDTO createNewIngredient(IngredientDTO dto, String tenantId) {
        if (ingredientRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("An ingredient with that code already exists");
        }

        if (ingredientRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("An ingredient with that name already exists");
        }

        Ingredient ingredient = ingredientMapper.toEntity(dto);
        ingredient.setTenantId(tenantId);
        ingredient.setActive(true);
        ingredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toDto(ingredient);
    }

    @Transactional
    public IngredientDTO updateExistingIngredient(Long id, IngredientDTO dto, String tenantId) {
        Ingredient ingredient = ingredientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        if (!ingredient.getCode().equals(dto.getCode())
                && ingredientRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("An ingredient with that code already exists");
        }

        if (!ingredient.getName().equals(dto.getName())
                && ingredientRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("An ingredient with that name already exists");
        }

        ingredient.setCode(dto.getCode());
        ingredient.setName(dto.getName());
        ingredient.setDescription(dto.getDescription());
        ingredient.setCategory(dto.getCategory());
        ingredient.setUnitOfMeasure(dto.getUnitOfMeasure());
        ingredient.setCurrentUnitCost(dto.getCurrentUnitCost());
        ingredient.setCurrentStock(dto.getCurrentStock());
        ingredient.setMinimumStock(dto.getMinimumStock());
        ingredient = ingredientRepository.save(ingredient);
        return ingredientMapper.toDto(ingredient);
    }

    @Transactional
    public void toggleIngredientActiveStatus(Long id, String tenantId) {
        Ingredient ingredient = ingredientRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));
        ingredient.setActive(!ingredient.getActive());
        ingredientRepository.save(ingredient);
    }
}
