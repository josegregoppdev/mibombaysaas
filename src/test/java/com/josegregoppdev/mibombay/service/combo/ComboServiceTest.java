package com.josegregoppdev.mibombay.service.combo;

import com.josegregoppdev.mibombay.dto.combo.ComboDTO;
import com.josegregoppdev.mibombay.mapper.combo.ComboMapper;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComboServiceTest {

    @Mock
    private ComboRepository comboRepository;

    @Mock
    private ComboMapper comboMapper;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ComboService comboService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void getPaginatedCombos_noFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Combo combo = createCombo();
        Page<Combo> page = new PageImpl<>(List.of(combo));
        when(comboRepository.findByFilters(eq(TENANT_ID), isNull(), eq(pageable)))
                .thenReturn(page);
        when(comboMapper.toDto(combo)).thenReturn(createComboDTO());

        Page<ComboDTO> result = comboService.getPaginatedCombos(TENANT_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("COM-001", result.getContent().get(0).getCode());
    }

    @Test
    void getPaginatedCombos_withFilter_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Combo combo = createCombo();
        Page<Combo> page = new PageImpl<>(List.of(combo));
        when(comboRepository.findByFilters(eq(TENANT_ID), eq("Hamburger"), eq(pageable)))
                .thenReturn(page);
        when(comboMapper.toDto(combo)).thenReturn(createComboDTO());

        Page<ComboDTO> result = comboService.getPaginatedCombos(TENANT_ID, "Hamburger", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getComboById_exists_returnsDTO() {
        Combo combo = createCombo();
        when(comboRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(combo));
        when(comboMapper.toDto(combo)).thenReturn(createComboDTO());

        ComboDTO result = comboService.getComboById(1L, TENANT_ID);

        assertNotNull(result);
        assertEquals("COM-001", result.getCode());
        assertEquals("Hamburger + Fries Combo", result.getName());
        assertNotNull(result.getDetails());
    }

    @Test
    void getComboById_doesNotExist_throwsException() {
        when(comboRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.getComboById(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void createNewCombo_success_returnsDTO() {
        ComboDTO dto = createComboDTO();
        Combo combo = createCombo();
        Product product = createProduct();

        when(comboRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(comboRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(false);
        when(comboMapper.toEntity(dto)).thenReturn(combo);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(comboRepository.save(any())).thenReturn(combo);
        when(comboMapper.toDto(combo)).thenReturn(dto);

        ComboDTO result = comboService.createNewCombo(dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("COM-001", result.getCode());
        assertTrue(result.getActive());

        ArgumentCaptor<Combo> captor = ArgumentCaptor.forClass(Combo.class);
        verify(comboRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertTrue(captor.getValue().getActive());
    }

    @Test
    void createNewCombo_duplicateCode_throwsException() {
        ComboDTO dto = createComboDTO();
        when(comboRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.createNewCombo(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void createNewCombo_duplicateName_throwsException() {
        ComboDTO dto = createComboDTO();
        when(comboRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(comboRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.createNewCombo(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingCombo_success_updatesFields() {
        Combo existing = createCombo();
        ComboDTO dto = createComboDTO();
        dto.setName("Updated Combo");
        dto.setSellingPrice(new BigDecimal("18.0000"));

        when(comboRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(productRepository.findById(1L)).thenReturn(Optional.of(createProduct()));
        when(comboRepository.save(existing)).thenReturn(existing);
        when(comboMapper.toDto(existing)).thenReturn(dto);

        ComboDTO result = comboService.updateExistingCombo(1L, dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("Updated Combo", result.getName());
        assertEquals(new BigDecimal("18.0000"), result.getSellingPrice());
        verify(comboRepository).save(existing);
    }

    @Test
    void updateExistingCombo_duplicateCode_throwsException() {
        Combo existing = createCombo();
        ComboDTO dto = createComboDTO();
        dto.setCode("COM-999");

        when(comboRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(comboRepository.existsByCodeAndTenantId("COM-999", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.updateExistingCombo(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void updateExistingCombo_duplicateName_throwsException() {
        Combo existing = createCombo();
        ComboDTO dto = createComboDTO();
        dto.setName("Cola Combo");

        when(comboRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(comboRepository.existsByNameAndTenantId("Cola Combo", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.updateExistingCombo(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingCombo_doesNotExist_throwsException() {
        ComboDTO dto = createComboDTO();
        when(comboRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.updateExistingCombo(99L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void toggleActiveStatus_activeToInactive_togglesStatus() {
        Combo combo = createCombo();
        assertTrue(combo.getActive());

        when(comboRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(combo));

        comboService.toggleComboActiveStatus(1L, TENANT_ID);

        assertFalse(combo.getActive());
        verify(comboRepository).save(combo);
    }

    @Test
    void toggleActiveStatus_inactiveToActive_togglesStatus() {
        Combo combo = createInactiveCombo();
        assertFalse(combo.getActive());

        when(comboRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(combo));

        comboService.toggleComboActiveStatus(2L, TENANT_ID);

        assertTrue(combo.getActive());
        verify(comboRepository).save(combo);
    }

    @Test
    void toggleActiveStatus_doesNotExist_throwsException() {
        when(comboRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> comboService.toggleComboActiveStatus(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
