package com.josegregoppdev.mibombay.controller.combo;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.combo.ComboDTO;
import com.josegregoppdev.mibombay.service.combo.ComboService;
import com.josegregoppdev.mibombay.service.product.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createComboDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComboControllerTest {

    @Mock
    private ComboService comboService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ComboController controller;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void list_noFilters_addsAttributesAndReturnsView() {
        Pageable pageable = PageRequest.of(0, 20);
        ComboDTO dto = createComboDTO();
        Page<ComboDTO> page = new PageImpl<>(List.of(dto));
        when(comboService.getPaginatedCombos(eq(TENANT_ID), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Model model = new ExtendedModelMap();
        String view = controller.list(null, pageable, model);

        assertEquals("combo/list", view);
        assertTrue(model.containsAttribute("page"));
        assertTrue(model.containsAttribute("name"));
    }

    @Test
    void list_withFilter_passesParametersToService() {
        Pageable pageable = PageRequest.of(0, 20);
        when(comboService.getPaginatedCombos(eq(TENANT_ID), eq("Hamburger"), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.list("Hamburger", pageable, model);

        assertEquals("combo/list", view);
        assertEquals("Hamburger", model.getAttribute("name"));
    }

    @Test
    void showForm_addsAttributesAndReturnsView() {
        when(productService.getPaginatedActiveProducts(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.showForm(model);

        assertEquals("combo/form", view);
        assertTrue(model.containsAttribute("combo"));
        assertTrue(model.containsAttribute("products"));
    }

    @Test
    void create_success_redirectsWithMessage() {
        ComboDTO dto = createComboDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.create(dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/combo", view);
        verify(comboService).createNewCombo(dto, TENANT_ID);
    }

    @Test
    void create_withValidationError_returnsFormView() {
        ComboDTO dto = createComboDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(productService.getPaginatedActiveProducts(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.create(dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("combo/form", view);
    }

    @Test
    void create_withException_redirectsWithError() {
        ComboDTO dto = createComboDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("A combo with that code already exists"))
                .when(comboService).createNewCombo(dto, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.create(dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/combo/new", view);
    }

    @Test
    void showEditForm_exists_addsAttributesAndReturnsView() {
        ComboDTO dto = createComboDTO();
        when(comboService.getComboById(1L, TENANT_ID)).thenReturn(dto);
        when(productService.getPaginatedActiveProducts(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("combo/form", view);
        assertTrue(model.containsAttribute("combo"));
        assertTrue(model.containsAttribute("products"));
    }

    @Test
    void showEditForm_doesNotExist_redirectsToList() {
        when(comboService.getComboById(99L, TENANT_ID))
                .thenThrow(new IllegalArgumentException("Combo not found"));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(99L, model);

        assertEquals("redirect:/combo", view);
    }

    @Test
    void update_success_redirectsWithMessage() {
        ComboDTO dto = createComboDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.update(1L, dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/combo", view);
        verify(comboService).updateExistingCombo(1L, dto, TENANT_ID);
    }

    @Test
    void update_withValidationError_returnsFormView() {
        ComboDTO dto = createComboDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(productService.getPaginatedActiveProducts(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.update(1L, dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("combo/form", view);
    }

    @Test
    void update_withException_redirectsWithError() {
        ComboDTO dto = createComboDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("A combo with that code already exists"))
                .when(comboService).updateExistingCombo(1L, dto, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.update(1L, dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/combo/1/edit", view);
    }

    @Test
    void toggleStatus_success_redirectsWithMessage() {
        Model model = new ExtendedModelMap();
        String view = controller.toggleStatus(1L, mock(RedirectAttributes.class));

        assertEquals("redirect:/combo", view);
        verify(comboService).toggleComboActiveStatus(1L, TENANT_ID);
    }

    @Test
    void toggleStatus_doesNotExist_redirectsWithError() {
        doThrow(new IllegalArgumentException("Combo not found"))
                .when(comboService).toggleComboActiveStatus(99L, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.toggleStatus(99L, mock(RedirectAttributes.class));

        assertEquals("redirect:/combo", view);
    }
}
