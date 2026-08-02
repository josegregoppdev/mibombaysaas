package com.josegregoppdev.mibombay.service.product;

import com.josegregoppdev.mibombay.dto.product.ProductDTO;
import com.josegregoppdev.mibombay.mapper.product.ProductMapper;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private ProductService productService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void getPaginatedProducts_noFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = createProduct();
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(page);
        when(productMapper.toDto(product)).thenReturn(createProductDTO());

        Page<ProductDTO> result = productService.getPaginatedProducts(TENANT_ID, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("PROD-001", result.getContent().get(0).getCode());
    }

    @Test
    void getPaginatedProducts_withFilters_returnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Product product = createProduct();
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByFilters(eq(TENANT_ID), eq("Hamburger"), eq(ProductCategory.FOOD), eq(ProductType.CON_RECETA), eq(pageable)))
                .thenReturn(page);
        when(productMapper.toDto(product)).thenReturn(createProductDTO());

        Page<ProductDTO> result = productService.getPaginatedProducts(
                TENANT_ID, "Hamburger", ProductCategory.FOOD, ProductType.CON_RECETA, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductById_exists_returnsDTO() {
        Product product = createProduct();
        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(createProductDTO());

        ProductDTO result = productService.getProductById(1L, TENANT_ID);

        assertNotNull(result);
        assertEquals("PROD-001", result.getCode());
        assertEquals("Classic Hamburger", result.getName());
        assertNotNull(result.getRecipeName());
    }

    @Test
    void getProductById_doesNotExist_throwsException() {
        when(productRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.getProductById(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void createNewProduct_withRecipe_success() {
        ProductDTO dto = createProductDTO();
        Product product = createProduct();
        Recipe recipe = createRecipe();

        when(productRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(productRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(false);
        when(productMapper.toEntity(dto)).thenReturn(product);
        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(recipe));
        when(productRepository.save(any())).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDTO result = productService.createNewProduct(dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("PROD-001", result.getCode());
        assertTrue(result.getActive());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertTrue(captor.getValue().getActive());
        assertNotNull(captor.getValue().getRecipe());
    }

    @Test
    void createNewProduct_withoutRecipe_success() {
        ProductDTO dto = createProductDTOWithoutRecipe();
        Product product = createProductWithoutRecipe();

        when(productRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(productRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(false);
        when(productMapper.toEntity(dto)).thenReturn(product);
        when(productRepository.save(any())).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDTO result = productService.createNewProduct(dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("PROD-002", result.getCode());
        assertNull(result.getRecipeId());
    }

    @Test
    void createNewProduct_conRecetaWithoutRecipe_throwsException() {
        ProductDTO dto = createProductDTO();
        dto.setRecipeId(null);

        when(productRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(productRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(false);
        Product product = Product.builder()
                .productType(ProductType.CON_RECETA)
                .tenantId(TENANT_ID)
                .build();
        when(productMapper.toEntity(dto)).thenReturn(product);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.createNewProduct(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("recipe"));
    }

    @Test
    void createNewProduct_duplicateCode_throwsException() {
        ProductDTO dto = createProductDTO();
        when(productRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.createNewProduct(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void createNewProduct_duplicateName_throwsException() {
        ProductDTO dto = createProductDTO();
        when(productRepository.existsByCodeAndTenantId(dto.getCode(), TENANT_ID)).thenReturn(false);
        when(productRepository.existsByNameAndTenantId(dto.getName(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.createNewProduct(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingProduct_success_updatesFields() {
        Product existing = createProduct();
        ProductDTO dto = createProductDTO();
        dto.setName("Updated Hamburger");
        dto.setSellingPrice(new BigDecimal("9.0000"));

        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(recipeRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(createRecipe()));
        when(productRepository.save(existing)).thenReturn(existing);
        when(productMapper.toDto(existing)).thenReturn(dto);

        ProductDTO result = productService.updateExistingProduct(1L, dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("Updated Hamburger", result.getName());
        assertEquals(new BigDecimal("9.0000"), result.getSellingPrice());
        verify(productRepository).save(existing);
    }

    @Test
    void updateExistingProduct_duplicateCode_throwsException() {
        Product existing = createProduct();
        ProductDTO dto = createProductDTO();
        dto.setCode("PROD-999");

        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(productRepository.existsByCodeAndTenantId("PROD-999", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.updateExistingProduct(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    void updateExistingProduct_duplicateName_throwsException() {
        Product existing = createProduct();
        ProductDTO dto = createProductDTO();
        dto.setName("Cola Soda");

        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existing));
        when(productRepository.existsByNameAndTenantId("Cola Soda", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.updateExistingProduct(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void updateExistingProduct_doesNotExist_throwsException() {
        ProductDTO dto = createProductDTO();
        when(productRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.updateExistingProduct(99L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void toggleActiveStatus_activeToInactive_togglesStatus() {
        Product product = createProduct();
        assertTrue(product.getActive());

        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(product));

        productService.toggleProductActiveStatus(1L, TENANT_ID);

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    void toggleActiveStatus_inactiveToActive_togglesStatus() {
        Product product = createInactiveProduct();
        assertFalse(product.getActive());

        when(productRepository.findByIdAndTenantId(3L, TENANT_ID)).thenReturn(Optional.of(product));

        productService.toggleProductActiveStatus(3L, TENANT_ID);

        assertTrue(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    void toggleActiveStatus_doesNotExist_throwsException() {
        when(productRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productService.toggleProductActiveStatus(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }
}
