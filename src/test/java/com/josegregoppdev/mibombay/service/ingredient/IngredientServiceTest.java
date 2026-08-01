package com.josegregoppdev.mibombay.service.ingredient;

import com.josegregoppdev.mibombay.dto.ingredient.IngredientDTO;
import com.josegregoppdev.mibombay.mapper.ingredient.IngredientMapper;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientService ingredientService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void getPaginatedIngredients_noFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingredient ingredient = createIngredient();
        Page<Ingredient> page = new PageImpl<>(List.of(ingredient));
        when(ingredientRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(page);
        when(ingredientMapper.toDto(ingredient)).thenReturn(createIngredientDTO());

        Page<IngredientDTO> result = ingredientService.getPaginatedIngredients(TENANT_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CAR-001", result.getContent().get(0).getCode());
    }

    @Test
    void getPaginatedIngredients_withFilters_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingredient ingredient = createIngredient();
        Page<Ingredient> page = new PageImpl<>(List.of(ingredient));
        when(ingredientRepository.findByFilters(eq(TENANT_ID), eq("Beef"), eq(Category.MEATS), eq(UnitOfMeasure.KILOGRAM), eq(pageable)))
                .thenReturn(page);
        when(ingredientMapper.toDto(ingredient)).thenReturn(createIngredientDTO());

        Page<IngredientDTO> result = ingredientService.getPaginatedIngredients(
                TENANT_ID, "Beef", Category.MEATS, UnitOfMeasure.KILOGRAM, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getPaginatedActiveIngredients_returnsOnlyActive() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingredient ingredient = createIngredient();
        Page<Ingredient> page = new PageImpl<>(List.of(ingredient));
        when(ingredientRepository.findByTenantIdAndActiveTrue(TENANT_ID, pageable)).thenReturn(page);
        when(ingredientMapper.toDto(ingredient)).thenReturn(createIngredientDTO());

        Page<IngredientDTO> result = ingredientService.getPaginatedActiveIngredients(TENANT_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getActive());
    }

    @Test
    void getIngredientById_exists_returnsDTO() {
        Ingredient ingredient = createIngredient();
        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(ingredient));
        when(ingredientMapper.toDto(ingredient)).thenReturn(createIngredientDTO());

        IngredientDTO result = ingredientService.getIngredientById(1L, TENANT_ID);

        assertNotNull(result);
        assertEquals("CAR-001", result.getCode());
        assertEquals("Beef", result.getName());
    }

    @Test
    void getIngredientById_doesNotExist_throwsException() {
        when(ingredientRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.getIngredientById(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void createNewIngredient_success_returnsDTO() {
        IngredientDTO dto = createIngredientDTO();
        Ingredient ingredient = createIngredient();

        when(ingredientRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(ingredientRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(false);
        when(ingredientMapper.toEntity(dto)).thenReturn(ingredient);
        when(ingredientRepository.save(any())).thenReturn(ingredient);
        when(ingredientMapper.toDto(ingredient)).thenReturn(dto);

        IngredientDTO result = ingredientService.createNewIngredient(dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("CAR-001", result.getCode());
        assertTrue(result.getActive());

        ArgumentCaptor<Ingredient> captor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertTrue(captor.getValue().getActive());
    }

    @Test
    void createNewIngredient_duplicateCode_throwsException() {
        IngredientDTO dto = createIngredientDTO();
        when(ingredientRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.createNewIngredient(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void createNewIngredient_duplicateName_throwsException() {
        IngredientDTO dto = createIngredientDTO();
        when(ingredientRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(ingredientRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.createNewIngredient(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingIngredient_success_updatesFields() {
        Ingredient existing = createIngredient();
        IngredientDTO dto = createIngredientDTO();
        dto.setName("Ground beef");
        dto.setCurrentUnitCost(new BigDecimal("20.00"));

        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(existing)).thenReturn(existing);
        when(ingredientMapper.toDto(existing)).thenReturn(dto);

        IngredientDTO result = ingredientService.updateExistingIngredient(1L, dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("Ground beef", result.getName());
        assertEquals(new BigDecimal("20.00"), result.getCurrentUnitCost());
        verify(ingredientRepository).save(existing);
    }

    @Test
    void updateExistingIngredient_duplicateCode_throwsException() {
        Ingredient existing = createIngredient();
        IngredientDTO dto = createIngredientDTO();
        dto.setCode("PAN-002");

        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(ingredientRepository.existsByCodeAndTenantId("PAN-002", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.updateExistingIngredient(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void updateExistingIngredient_duplicateName_throwsException() {
        Ingredient existing = createIngredient();
        IngredientDTO dto = createIngredientDTO();
        dto.setName("Hamburger bun");

        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(ingredientRepository.existsByNameAndTenantId("Hamburger bun", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.updateExistingIngredient(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingIngredient_doesNotExist_throwsException() {
        IngredientDTO dto = createIngredientDTO();
        when(ingredientRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.updateExistingIngredient(99L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void toggleActiveStatus_activeToInactive_togglesStatus() {
        Ingredient ingredient = createIngredient();
        assertTrue(ingredient.getActive());

        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(ingredient));

        ingredientService.toggleIngredientActiveStatus(1L, TENANT_ID);

        assertFalse(ingredient.getActive());
        verify(ingredientRepository).save(ingredient);
    }

    @Test
    void toggleActiveStatus_inactiveToActive_togglesStatus() {
        Ingredient ingredient = createInactiveIngredient();
        assertFalse(ingredient.getActive());

        when(ingredientRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(ingredient));

        ingredientService.toggleIngredientActiveStatus(2L, TENANT_ID);

        assertTrue(ingredient.getActive());
        verify(ingredientRepository).save(ingredient);
    }

    @Test
    void toggleActiveStatus_doesNotExist_throwsException() {
        when(ingredientRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredientService.toggleIngredientActiveStatus(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
