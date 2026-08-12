package com.josegregoppdev.mibombay.controller.purchase;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseCartSubmissionDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDTO;
import com.josegregoppdev.mibombay.dto.user.UserDTOResponse;
import com.josegregoppdev.mibombay.service.ingredient.IngredientService;
import com.josegregoppdev.mibombay.service.product.ProductService;
import com.josegregoppdev.mibombay.service.purchase.PurchaseService;
import com.josegregoppdev.mibombay.service.supplier.SupplierService;
import com.josegregoppdev.mibombay.service.user.UserService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock private PurchaseService purchaseService;
    @Mock private SupplierService supplierService;
    @Mock private IngredientService ingredientService;
    @Mock private ProductService productService;
    @Mock private UserService userService;

    @InjectMocks private PurchaseController controller;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT_ID);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", "test1234", List.of()));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void list_noFilters_addsAttributesAndReturnsView() {
        Pageable pageable = PageRequest.of(0, 20);
        PurchaseDTO dto = createPurchaseDTO();
        Page<PurchaseDTO> page = new PageImpl<>(List.of(dto));
        when(purchaseService.getPaginatedPurchases(eq(TENANT_ID), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Model model = new ExtendedModelMap();
        String view = controller.list(null, null, null, null, pageable, model);

        assertEquals("purchase/list", view);
        assertTrue(model.containsAttribute("page"));
        assertTrue(model.containsAttribute("supplierName"));
        assertTrue(model.containsAttribute("from"));
        assertTrue(model.containsAttribute("to"));
        assertTrue(model.containsAttribute("active"));
    }

    @Test
    void list_withFilters_passesParametersToService() {
        Pageable pageable = PageRequest.of(0, 20);
        java.time.LocalDate from = java.time.LocalDate.of(2025, 1, 1);
        java.time.LocalDate to = java.time.LocalDate.of(2025, 12, 31);
        when(purchaseService.getPaginatedPurchases(eq(TENANT_ID), eq("San"), eq(from), eq(to), eq(true), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.list("San", from, to, true, pageable, model);

        assertEquals("purchase/list", view);
        assertEquals("San", model.getAttribute("supplierName"));
        assertEquals(from, model.getAttribute("from"));
        assertEquals(to, model.getAttribute("to"));
        assertEquals(true, model.getAttribute("active"));
    }

    @Test
    void showNewForm_addsAttributesAndReturnsView() {
        Model model = new ExtendedModelMap();
        String view = controller.showNewForm(model);

        assertEquals("purchase/new", view);
        assertTrue(model.containsAttribute("submission"));
        assertTrue(model.containsAttribute("suppliers"));
        assertTrue(model.containsAttribute("ingredients"));
        assertTrue(model.containsAttribute("products"));
        verify(supplierService).getAllActiveSuppliersFlat(TENANT_ID);
        verify(ingredientService).getAllActiveIngredientsFlat(TENANT_ID);
        verify(productService).getAllActiveSinRecetaProductsFlat(TENANT_ID);
    }

    @Test
    void create_success_redirectsToDetail() {
        PurchaseCartSubmissionDTO submission = createPurchaseCartSubmissionDTO();
        when(userService.getUserByEmail("admin@test.com")).thenReturn(
                UserDTOResponse.builder().id(1L).build());
        PurchaseDTO dto = createPurchaseDTO();
        when(purchaseService.createPurchaseFromCart(eq(submission.getItems()), eq(TENANT_ID), eq(1L),
                eq(submission.getSupplierId()), eq(submission.getObservations()), eq(submission.getPurchaseDate())))
                .thenReturn(dto);

        String view = controller.create(submission, mock(RedirectAttributes.class));

        assertEquals("redirect:/purchase/1", view);
        verify(purchaseService).createPurchaseFromCart(
                submission.getItems(), TENANT_ID, 1L, submission.getSupplierId(),
                submission.getObservations(), submission.getPurchaseDate());
    }

    @Test
    void create_emptyCart_redirectsWithError() {
        PurchaseCartSubmissionDTO submission = new PurchaseCartSubmissionDTO();
        submission.setItems(List.of());
        submission.setSupplierId(2L);

        String view = controller.create(submission, mock(RedirectAttributes.class));

        assertEquals("redirect:/purchase/new", view);
        verify(purchaseService, never()).createPurchaseFromCart(any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_withException_redirectsWithError() {
        PurchaseCartSubmissionDTO submission = createPurchaseCartSubmissionDTO();
        when(userService.getUserByEmail("admin@test.com")).thenReturn(
                UserDTOResponse.builder().id(1L).build());
        doThrow(new IllegalArgumentException("Supplier not found"))
                .when(purchaseService).createPurchaseFromCart(
                        any(), any(), any(), any(), any(), any());

        String view = controller.create(submission, mock(RedirectAttributes.class));

        assertEquals("redirect:/purchase/new", view);
    }

    @Test
    void view_exists_addsAttributeAndReturnsView() {
        PurchaseDTO dto = createPurchaseDTO();
        when(purchaseService.getPurchaseById(1L, TENANT_ID)).thenReturn(dto);

        Model model = new ExtendedModelMap();
        String view = controller.view(1L, model, mock(RedirectAttributes.class));

        assertEquals("purchase/detail", view);
        assertTrue(model.containsAttribute("purchase"));
    }

    @Test
    void view_doesNotExist_redirectsWithError() {
        when(purchaseService.getPurchaseById(99L, TENANT_ID))
                .thenThrow(new IllegalArgumentException("Purchase not found"));

        Model model = new ExtendedModelMap();
        String view = controller.view(99L, model, mock(RedirectAttributes.class));

        assertEquals("redirect:/purchase", view);
    }

    @Test
    void cancel_success_redirectsWithMessage() {
        when(userService.getUserByEmail("admin@test.com")).thenReturn(
                UserDTOResponse.builder().id(1L).build());
        String view = controller.cancel(1L, mock(RedirectAttributes.class));

        assertEquals("redirect:/purchase/1", view);
        verify(purchaseService).cancelPurchase(1L, TENANT_ID, 1L);
    }

    @Test
    void cancel_withException_redirectsWithError() {
        when(userService.getUserByEmail("admin@test.com")).thenReturn(
                UserDTOResponse.builder().id(1L).build());
        doThrow(new IllegalArgumentException("The purchase is already cancelled"))
                .when(purchaseService).cancelPurchase(99L, TENANT_ID, 1L);

        String view = controller.cancel(99L, mock(RedirectAttributes.class));

        assertEquals("redirect:/purchase/99", view);
    }
}