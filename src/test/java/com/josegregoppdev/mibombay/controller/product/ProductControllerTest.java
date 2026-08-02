package com.josegregoppdev.mibombay.controller.product;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.product.ProductDTO;
import com.josegregoppdev.mibombay.dto.recipe.RecipeDTO;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.service.product.ProductService;
import com.josegregoppdev.mibombay.service.recipe.RecipeService;
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

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createProductDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private ProductController controller;

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
        ProductDTO dto = createProductDTO();
        Page<ProductDTO> page = new PageImpl<>(List.of(dto));
        when(productService.getPaginatedProducts(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Model model = new ExtendedModelMap();
        String view = controller.list(null, null, null, pageable, model);

        assertEquals("product/list", view);
        assertTrue(model.containsAttribute("page"));
        assertTrue(model.containsAttribute("categories"));
        assertTrue(model.containsAttribute("productTypes"));
        assertTrue(model.containsAttribute("name"));
        assertTrue(model.containsAttribute("selectedCategory"));
        assertTrue(model.containsAttribute("selectedProductType"));
    }

    @Test
    void list_withFilters_passesParametersToService() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productService.getPaginatedProducts(eq(TENANT_ID), eq("Hamburger"), eq(ProductCategory.FOOD), eq(ProductType.CON_RECETA), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.list("Hamburger", ProductCategory.FOOD, ProductType.CON_RECETA, pageable, model);

        assertEquals("product/list", view);
        assertEquals("Hamburger", model.getAttribute("name"));
        assertEquals(ProductCategory.FOOD, model.getAttribute("selectedCategory"));
        assertEquals(ProductType.CON_RECETA, model.getAttribute("selectedProductType"));
    }

    @Test
    void showForm_addsAttributesAndReturnsView() {
        when(recipeService.getPaginatedRecipes(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.showForm(model);

        assertEquals("product/form", view);
        assertTrue(model.containsAttribute("product"));
        assertTrue(model.containsAttribute("categories"));
        assertTrue(model.containsAttribute("productTypes"));
        assertTrue(model.containsAttribute("recipes"));
    }

    @Test
    void create_success_redirectsWithMessage() {
        ProductDTO dto = createProductDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.create(dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/product", view);
        verify(productService).createNewProduct(dto, TENANT_ID);
    }

    @Test
    void create_withValidationError_returnsFormView() {
        ProductDTO dto = createProductDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(recipeService.getPaginatedRecipes(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.create(dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("product/form", view);
    }

    @Test
    void create_withException_redirectsWithError() {
        ProductDTO dto = createProductDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("A product with that code already exists"))
                .when(productService).createNewProduct(dto, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.create(dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/product/new", view);
    }

    @Test
    void showEditForm_exists_addsAttributesAndReturnsView() {
        ProductDTO dto = createProductDTO();
        when(productService.getProductById(1L, TENANT_ID)).thenReturn(dto);
        when(recipeService.getPaginatedRecipes(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("product/form", view);
        assertTrue(model.containsAttribute("product"));
        assertTrue(model.containsAttribute("categories"));
        assertTrue(model.containsAttribute("productTypes"));
        assertTrue(model.containsAttribute("recipes"));
    }

    @Test
    void showEditForm_doesNotExist_redirectsToList() {
        when(productService.getProductById(99L, TENANT_ID))
                .thenThrow(new IllegalArgumentException("Product not found"));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(99L, model);

        assertEquals("redirect:/product", view);
    }

    @Test
    void update_success_redirectsWithMessage() {
        ProductDTO dto = createProductDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.update(1L, dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/product", view);
        verify(productService).updateExistingProduct(1L, dto, TENANT_ID);
    }

    @Test
    void update_withValidationError_returnsFormView() {
        ProductDTO dto = createProductDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(recipeService.getPaginatedRecipes(eq(TENANT_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.update(1L, dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("product/form", view);
    }

    @Test
    void update_withException_redirectsWithError() {
        ProductDTO dto = createProductDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("A product with that code already exists"))
                .when(productService).updateExistingProduct(1L, dto, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.update(1L, dto, bindingResult, mock(RedirectAttributes.class), model);

        assertEquals("redirect:/product/1/edit", view);
    }

    @Test
    void toggleStatus_success_redirectsWithMessage() {
        Model model = new ExtendedModelMap();
        String view = controller.toggleStatus(1L, mock(RedirectAttributes.class));

        assertEquals("redirect:/product", view);
        verify(productService).toggleProductActiveStatus(1L, TENANT_ID);
    }

    @Test
    void toggleStatus_doesNotExist_redirectsWithError() {
        doThrow(new IllegalArgumentException("Product not found"))
                .when(productService).toggleProductActiveStatus(99L, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.toggleStatus(99L, mock(RedirectAttributes.class));

        assertEquals("redirect:/product", view);
    }
}
