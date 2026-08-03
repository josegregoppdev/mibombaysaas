package com.josegregoppdev.mibombay.service.recipe;

import com.josegregoppdev.mibombay.dto.recipe.RecipeDTO;
import com.josegregoppdev.mibombay.mapper.recipe.RecipeMapper;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.recipe.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private com.josegregoppdev.mibombay.service.product.ProductService productService;

    @InjectMocks
    private RecipeService recipeService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void getPaginatedRecipes_noFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Recipe recipe = createRecipe();
        Page<Recipe> page = new PageImpl<>(List.of(recipe));
        when(recipeRepository.findByFilters(eq(TENANT_ID), isNull(), eq(pageable)))
                .thenReturn(page);
        when(recipeMapper.toDto(recipe)).thenReturn(createRecipeDTO());

        Page<RecipeDTO> result = recipeService.getPaginatedRecipes(TENANT_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("HAM-001", result.getContent().get(0).getCode());
    }

    @Test
    void getPaginatedRecipes_withNameFilter_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Recipe recipe = createRecipe();
        Page<Recipe> page = new PageImpl<>(List.of(recipe));
        when(recipeRepository.findByFilters(eq(TENANT_ID), eq("Classic"), eq(pageable)))
                .thenReturn(page);
        when(recipeMapper.toDto(recipe)).thenReturn(createRecipeDTO());

        Page<RecipeDTO> result = recipeService.getPaginatedRecipes(TENANT_ID, "Classic", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getRecipeById_exists_returnsDTO() {
        Recipe recipe = createRecipe();
        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toDto(recipe)).thenReturn(createRecipeDTO());

        RecipeDTO result = recipeService.getRecipeById(1L, TENANT_ID);

        assertNotNull(result);
        assertEquals("HAM-001", result.getCode());
        assertEquals("Classic Hamburger", result.getName());
    }

    @Test
    void getRecipeById_doesNotExist_throwsException() {
        when(recipeRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.getRecipeById(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void createNewRecipe_success_returnsDTO() {
        RecipeDTO dto = createRecipeDTO();
        Recipe recipe = createRecipe();
        Ingredient ingredient = createIngredient();

        when(recipeRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(recipeRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(false);
        when(recipeMapper.toEntity(dto)).thenReturn(recipe);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(any())).thenReturn(recipe);
        when(recipeMapper.toDto(recipe)).thenReturn(dto);

        RecipeDTO result = recipeService.createNewRecipe(dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("HAM-001", result.getCode());
        assertTrue(result.getActive());

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertTrue(captor.getValue().getActive());
    }

    @Test
    void createNewRecipe_duplicateCode_throwsException() {
        RecipeDTO dto = createRecipeDTO();
        when(recipeRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createNewRecipe(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void createNewRecipe_duplicateName_throwsException() {
        RecipeDTO dto = createRecipeDTO();
        when(recipeRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(recipeRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.createNewRecipe(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingRecipe_success_updatesFields() {
        Recipe existing = createRecipe();
        RecipeDTO dto = createRecipeDTO();
        dto.setName("Premium Hamburger");

        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(createIngredient()));
        when(recipeRepository.save(existing)).thenReturn(existing);
        when(recipeMapper.toDto(existing)).thenReturn(dto);

        RecipeDTO result = recipeService.updateExistingRecipe(1L, dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("Premium Hamburger", result.getName());
        verify(recipeRepository).save(existing);
    }

    @Test
    void updateExistingRecipe_duplicateCode_throwsException() {
        Recipe existing = createRecipe();
        RecipeDTO dto = createRecipeDTO();
        dto.setCode("SOD-001");

        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(recipeRepository.existsByCodeAndTenantId("SOD-001", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.updateExistingRecipe(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void updateExistingRecipe_duplicateName_throwsException() {
        Recipe existing = createRecipe();
        RecipeDTO dto = createRecipeDTO();
        dto.setName("Cola Soda");

        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(recipeRepository.existsByNameAndTenantId("Cola Soda", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.updateExistingRecipe(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingRecipe_doesNotExist_throwsException() {
        RecipeDTO dto = createRecipeDTO();
        when(recipeRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.updateExistingRecipe(99L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void toggleActiveStatus_activeToInactive_togglesStatus() {
        Recipe recipe = createRecipe();
        assertTrue(recipe.getActive());

        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(recipe));

        recipeService.toggleRecipeActiveStatus(1L, TENANT_ID);

        assertFalse(recipe.getActive());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void toggleActiveStatus_inactiveToActive_togglesStatus() {
        Recipe recipe = createInactiveRecipe();
        assertFalse(recipe.getActive());

        when(recipeRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(recipe));

        recipeService.toggleRecipeActiveStatus(2L, TENANT_ID);

        assertTrue(recipe.getActive());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void toggleActiveStatus_doesNotExist_throwsException() {
        when(recipeRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> recipeService.toggleRecipeActiveStatus(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
