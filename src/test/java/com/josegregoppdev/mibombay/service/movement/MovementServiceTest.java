package com.josegregoppdev.mibombay.service.movement;

import com.josegregoppdev.mibombay.dto.movement.MovementDTO;
import com.josegregoppdev.mibombay.mapper.movement.MovementMapper;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.movement.Movement;
import com.josegregoppdev.mibombay.model.movement.MovementType;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.movement.MovementRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createCashier;
import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createIngredient;
import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createMovement;
import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createMovementForProduct;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private MovementMapper movementMapper;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MovementService movementService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void getMovementsByFilters_enrichesIngredientMovement() {
        Movement movement = createMovement();
        Page<Movement> page = new PageImpl<>(List.of(movement));
        when(movementRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        when(ingredientRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(1L)))
                .thenReturn(List.of(createIngredient()));
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        Page<MovementDTO> result = movementService.getMovementsByFilters(TENANT_ID, null, null, null, PageRequest.of(0, 20));

        MovementDTO dto = result.getContent().get(0);
        assertEquals("Beef", dto.getIngredientName());
        assertEquals("Sale", dto.getTypeDisplayName());
        assertEquals("Cashier My Restaurant", dto.getUserName());
    }

    @Test
    void getMovementsByFilters_enrichesProductMovement() {
        Movement movement = createMovementForProduct();
        Page<Movement> page = new PageImpl<>(List.of(movement));
        when(movementRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        Product product = Product.builder()
                .id(2L).tenantId(TENANT_ID).name("Bottled Cola")
                .build();
        when(productRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(2L)))
                .thenReturn(List.of(product));
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        Page<MovementDTO> result = movementService.getMovementsByFilters(TENANT_ID, null, null, null, PageRequest.of(0, 20));

        MovementDTO dto = result.getContent().get(0);
        assertEquals("Bottled Cola", dto.getProductName());
        assertEquals("Sale", dto.getTypeDisplayName());
    }

    @Test
    void getMovementsByFilters_withType_passesTypeToRepository() {
        when(movementRepository.findByFilters(eq(TENANT_ID), eq(MovementType.PURCHASE), any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        movementService.getMovementsByFilters(TENANT_ID, MovementType.PURCHASE, null, null, PageRequest.of(0, 20));

        verify(movementRepository).findByFilters(TENANT_ID, MovementType.PURCHASE, null, null, PageRequest.of(0, 20));
    }

    @Test
    void getMovementsByFilters_emptyPage_returnsEmptyWithoutEnrichment() {
        when(movementRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<MovementDTO> result = movementService.getMovementsByFilters(TENANT_ID, null, null, null, PageRequest.of(0, 20));

        assertTrue(result.isEmpty());
        verifyNoInteractions(ingredientRepository);
        verifyNoInteractions(productRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getMovementsByFilters_ingredientNotFound_keepsNullName() {
        Movement movement = createMovement();
        Page<Movement> page = new PageImpl<>(List.of(movement));
        when(movementRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        when(ingredientRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(1L)))
                .thenReturn(List.of());
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        Page<MovementDTO> result = movementService.getMovementsByFilters(TENANT_ID, null, null, null, PageRequest.of(0, 20));

        assertNull(result.getContent().get(0).getIngredientName());
    }

    @Test
    void getMovementsByTenant_returnsEnrichedPage() {
        Movement movement = createMovement();
        Page<Movement> page = new PageImpl<>(List.of(movement));
        when(movementRepository.findByTenantId(TENANT_ID, PageRequest.of(0, 20)))
                .thenReturn(page);
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        when(ingredientRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(1L)))
                .thenReturn(List.of(createIngredient()));
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        Page<MovementDTO> result = movementService.getMovementsByTenant(TENANT_ID, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
        assertEquals("Beef", result.getContent().get(0).getIngredientName());
    }

    @Test
    void getHistorialByIngredient_returnsEnrichedList() {
        Movement movement = createMovement();
        when(movementRepository.findByTenantIdAndIngredientId(TENANT_ID, 1L))
                .thenReturn(List.of(movement));
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        when(ingredientRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(1L)))
                .thenReturn(List.of(createIngredient()));
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        List<MovementDTO> result = movementService.getHistorialByIngredient(TENANT_ID, 1L);

        assertEquals(1, result.size());
        assertEquals("Beef", result.get(0).getIngredientName());
    }

    @Test
    void getHistorialByProduct_emptyList_returnsEmptyList() {
        when(movementRepository.findByTenantIdAndProductId(TENANT_ID, 99L))
                .thenReturn(List.of());

        List<MovementDTO> result = movementService.getHistorialByProduct(TENANT_ID, 99L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(ingredientRepository);
        verifyNoInteractions(productRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getMovementsByDateRange_returnsEnrichedList() {
        Movement movement = createMovement();
        LocalDateTime from = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 8, 31, 23, 59);
        when(movementRepository.findByTenantIdAndDateBetween(TENANT_ID, from, to))
                .thenReturn(List.of(movement));
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        when(ingredientRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(1L)))
                .thenReturn(List.of(createIngredient()));
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        List<MovementDTO> result = movementService.getMovementsByDateRange(TENANT_ID, from, to);

        assertEquals(1, result.size());
        assertEquals("Beef", result.get(0).getIngredientName());
    }

    @Test
    void getMovementsByReference_returnsEnrichedPage() {
        Movement movement = createMovement();
        Page<Movement> page = new PageImpl<>(List.of(movement));
        when(movementRepository.findByTenantIdAndReferenceId(TENANT_ID, 1L, PageRequest.of(0, 20)))
                .thenReturn(page);
        when(movementMapper.toDto(any(Movement.class))).thenReturn(new MovementDTO());
        when(ingredientRepository.findByTenantIdAndIdIn(TENANT_ID, Set.of(1L)))
                .thenReturn(List.of(createIngredient()));
        when(userRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(createCashier()));

        Page<MovementDTO> result = movementService.getMovementsByReference(TENANT_ID, 1L, PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
        assertEquals("Beef", result.getContent().get(0).getIngredientName());
    }
}
