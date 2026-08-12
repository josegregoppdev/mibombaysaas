package com.josegregoppdev.mibombay.service.inventory;

import com.josegregoppdev.mibombay.dto.configuration.TenantConfigurationDTO;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.combo.ComboDetail;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.movement.Movement;
import com.josegregoppdev.mibombay.model.movement.MovementType;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.model.purchase.Purchase;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.model.sale.Sale;
import com.josegregoppdev.mibombay.model.sale.SaleDetail;
import com.josegregoppdev.mibombay.model.sale.SaleState;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.movement.MovementRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.service.configuration.TenantConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock private IngredientRepository ingredientRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ComboRepository comboRepository;
    @Mock private MovementRepository movementRepository;
    @Mock private TenantConfigurationService tenantConfigurationService;

    @InjectMocks private InventarioService inventarioService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    private TenantConfigurationDTO configAllowNegative(boolean allow) {
        return TenantConfigurationDTO.builder().id(1L).tenantId(TENANT_ID).allowNegativeInventory(allow).build();
    }

    private Sale buildSale(List<SaleDetail> details) {
        return Sale.builder()
                .id(1L).tenantId(TENANT_ID)
                .saleDate(java.time.LocalDateTime.now())
                .state(SaleState.CONFIRMADA)
                .cashier(createCashier())
                .details(details != null ? details : new ArrayList<>())
                .build();
    }

    private SaleDetail productDetail(Product product, BigDecimal qty, String notes) {
        return SaleDetail.builder()
                .productId(product.getId()).productName(product.getName())
                .quantity(qty).notes(notes)
                .build();
    }

    private SaleDetail comboDetail(Combo combo, BigDecimal qty) {
        return SaleDetail.builder()
                .comboId(combo.getId()).comboName(combo.getName())
                .quantity(qty)
                .build();
    }

    @Test
    void consumeForSale_productWithRecipe_decrementsIngredientsAndCreatesMovement() {
        Ingredient beef = createIngredient();
        Product hamburger = createProductWithRecipe();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(hamburger));
        when(ingredientRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(1L)))).thenReturn(List.of(beef));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = buildSale(List.of(productDetail(hamburger, new BigDecimal("2"), null)));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("49.8000").compareTo(beef.getCurrentStock()));
        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement m = captor.getValue();
        assertEquals(MovementType.SALE, m.getType());
        assertEquals(null, m.getProductId());
        assertEquals(1L, m.getIngredientId());
        assertEquals(0, new BigDecimal("50.0000").compareTo(m.getPreviousStock()));
        assertEquals(0, new BigDecimal("49.8000").compareTo(m.getNewStock()));
        assertEquals(1L, m.getReferenceId());
    }

    @Test
    void consumeForSale_productSinReceta_decrementsProductStockAndCreatesMovement() {
        Product cola = createProductSinReceta();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(cola));
        when(productRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(2L)))).thenReturn(List.of(cola));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = buildSale(List.of(productDetail(cola, new BigDecimal("3"), null)));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("97.0000").compareTo(cola.getCurrentStock()));
        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement m = captor.getValue();
        assertEquals(MovementType.SALE, m.getType());
        assertEquals(2L, m.getProductId());
        assertEquals(null, m.getIngredientId());
        assertEquals(0, new BigDecimal("100.0000").compareTo(m.getPreviousStock()));
        assertEquals(0, new BigDecimal("97.0000").compareTo(m.getNewStock()));
    }

    @Test
    void consumeForSale_combo_expandsProductsAndConsumesAll() {
        Ingredient beef = createIngredient();
        Product hamburger = createProductWithRecipe();
        Product cola = createProductSinReceta();

        ComboDetail d1 = ComboDetail.builder().id(1L).product(hamburger).quantity(BigDecimal.ONE).build();
        ComboDetail d2 = ComboDetail.builder().id(2L).product(cola).quantity(BigDecimal.ONE).build();
        Combo combo = createComboWithDetails(d1, d2);

        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(comboRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(combo));
        when(ingredientRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(1L)))).thenReturn(List.of(beef));
        when(productRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(2L)))).thenReturn(List.of(cola));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = buildSale(List.of(comboDetail(combo, new BigDecimal("2"))));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("49.8000").compareTo(beef.getCurrentStock()));
        assertEquals(0, new BigDecimal("98.0000").compareTo(cola.getCurrentStock()));
        verify(movementRepository, times(2)).save(any());
    }

    @Test
    void consumeForSale_excludedIngredient_notConsumed() {
        Ingredient beef = createIngredient();
        Product hamburger = createProductWithRecipe();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(hamburger));

        Sale sale = buildSale(List.of(productDetail(hamburger, BigDecimal.ONE, "Without: Beef")));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("50.0000").compareTo(beef.getCurrentStock()));
        verify(movementRepository, never()).save(any());
    }

    @Test
    void consumeForSale_negativeNotAllowed_throwsAndDoesNotSave() {
        Ingredient beef = createIngredient();
        Product hamburger = createProductWithRecipe();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(false));
        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(hamburger));
        when(ingredientRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(1L)))).thenReturn(List.of(beef));

        Sale sale = buildSale(List.of(productDetail(hamburger, new BigDecimal("1000"), null)));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.consumeForSale(sale));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(movementRepository, never()).save(any());
        assertEquals(0, new BigDecimal("50.0000").compareTo(beef.getCurrentStock()));
    }

    @Test
    void consumeForSale_negativeAllowed_allowsNegativeStock() {
        Ingredient beef = createIngredient();
        Product hamburger = createProductWithRecipe();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(hamburger));
        when(ingredientRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(1L)))).thenReturn(List.of(beef));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = buildSale(List.of(productDetail(hamburger, new BigDecimal("1000"), null)));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("-50.0000").compareTo(beef.getCurrentStock()));
        verify(movementRepository).save(any());
    }

    @Test
    void consumeForSale_adicionalWithRecipe_consumesIngredients() {
        Ingredient cheese = createIngredient();
        Recipe recipe = createRecipe();
        Product addon = createAddonProduct();
        addon.setRecipe(recipe);
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(addon.getId(), TENANT_ID)).thenReturn(Optional.of(addon));
        when(ingredientRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(1L)))).thenReturn(List.of(cheese));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = buildSale(List.of(productDetail(addon, new BigDecimal("2"), null)));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("49.8000").compareTo(cheese.getCurrentStock()));
        verify(movementRepository).save(any());
    }

    @Test
    void consumeForSale_adicionalWithoutRecipe_throws() {
        Product addon = Product.builder()
                .id(6L).tenantId(TENANT_ID).code("ADD-001").name("Extra Cheese")
                .productType(ProductType.ADICIONAL).active(true).build();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(6L, TENANT_ID)).thenReturn(Optional.of(addon));

        Sale sale = buildSale(List.of(productDetail(addon, BigDecimal.ONE, null)));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.consumeForSale(sale));
        assertTrue(ex.getMessage().contains("recipe"));
    }

    @Test
    void consumeForSale_multipleSaleDetails_aggregatesSameIngredient() {
        Ingredient beef = createIngredient();
        Product hamburger = createProductWithRecipe();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(hamburger));
        when(ingredientRepository.findByTenantIdAndIdIn(eq(TENANT_ID), eq(Set.of(1L)))).thenReturn(List.of(beef));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Sale sale = buildSale(List.of(
                productDetail(hamburger, new BigDecimal("3"), null),
                productDetail(hamburger, new BigDecimal("2"), null)
        ));
        inventarioService.consumeForSale(sale);

        assertEquals(0, new BigDecimal("49.5000").compareTo(beef.getCurrentStock()));
        verify(movementRepository).save(any());
    }

    @Test
    void consumeForSale_saleWithNoDetails_doesNothing() {
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        Sale sale = buildSale(new ArrayList<>());
        inventarioService.consumeForSale(sale);
        verify(movementRepository, never()).save(any());
    }

    @Test
    void consumeForSale_productNotFound_throws() {
        Product cola = createProductSinReceta();
        when(tenantConfigurationService.getByTenantId(TENANT_ID)).thenReturn(configAllowNegative(true));
        when(productRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.empty());

        Sale sale = buildSale(List.of(productDetail(cola, BigDecimal.ONE, null)));
        assertThrows(IllegalArgumentException.class, () -> inventarioService.consumeForSale(sale));
    }

    @Test
    void addStockForPurchase_ingredient_incrementsStockUpdatesCostAndCreatesMovement() {
        Ingredient beef = createIngredient();
        Purchase purchase = buildPurchaseForIngredient();
        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(beef));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventarioService.addStockForPurchase(purchase);

        assertEquals(0, new BigDecimal("60.0000").compareTo(beef.getCurrentStock()));
        assertEquals(0, new BigDecimal("14.0000").compareTo(beef.getCurrentUnitCost()));
        assertEquals(0, new BigDecimal("50.0000").compareTo(purchase.getDetails().get(0).getPreviousStock()));
        assertEquals(0, new BigDecimal("15.5000").compareTo(purchase.getDetails().get(0).getPreviousUnitCost()));

        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement m = captor.getValue();
        assertEquals(MovementType.PURCHASE, m.getType());
        assertEquals(null, m.getProductId());
        assertEquals(1L, m.getIngredientId());
        assertEquals(0, new BigDecimal("50.0000").compareTo(m.getPreviousStock()));
        assertEquals(0, new BigDecimal("60.0000").compareTo(m.getNewStock()));
        assertEquals(1L, m.getReferenceId());
        assertEquals(1L, m.getUserId());
    }

    @Test
    void addStockForPurchase_product_incrementsStockUpdatesCostAndCreatesMovement() {
        Product cola = createProductSinReceta();
        Purchase purchase = buildPurchaseForProduct();
        when(productRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(cola));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventarioService.addStockForPurchase(purchase);

        assertEquals(0, new BigDecimal("120.0000").compareTo(cola.getCurrentStock()));
        assertEquals(0, new BigDecimal("0.9000").compareTo(cola.getUnitCost()));
        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement m = captor.getValue();
        assertEquals(MovementType.PURCHASE, m.getType());
        assertEquals(2L, m.getProductId());
        assertEquals(0, new BigDecimal("100.0000").compareTo(m.getPreviousStock()));
        assertEquals(0, new BigDecimal("120.0000").compareTo(m.getNewStock()));
    }

    @Test
    void addStockForPurchase_ingredientNotFound_throws() {
        Purchase purchase = buildPurchaseForIngredient();
        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> inventarioService.addStockForPurchase(purchase));
        verify(movementRepository, never()).save(any());
    }

    @Test
    void revertPurchase_ingredient_decrementsRestoresCostAndCreatesReturnMovement() {
        Ingredient beef = createIngredient();
        beef.setCurrentStock(new BigDecimal("60"));
        beef.setCurrentUnitCost(new BigDecimal("14.0000"));
        Purchase purchase = buildPurchaseForIngredient();
        purchase.getDetails().get(0).setPreviousStock(new BigDecimal("50"));
        purchase.getDetails().get(0).setPreviousUnitCost(new BigDecimal("15.5000"));
        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(beef));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventarioService.revertPurchase(purchase, 2L);

        assertEquals(0, new BigDecimal("50.0000").compareTo(beef.getCurrentStock()));
        assertEquals(0, new BigDecimal("15.5000").compareTo(beef.getCurrentUnitCost()));
        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement m = captor.getValue();
        assertEquals(MovementType.RETURN, m.getType());
        assertEquals(1L, m.getIngredientId());
        assertEquals(0, new BigDecimal("60.0000").compareTo(m.getPreviousStock()));
        assertEquals(0, new BigDecimal("50.0000").compareTo(m.getNewStock()));
        assertEquals(2L, m.getUserId());
    }

    @Test
    void revertPurchase_product_decrementsRestoresCostAndCreatesReturnMovement() {
        Product cola = createProductSinReceta();
        cola.setCurrentStock(new BigDecimal("120"));
        cola.setUnitCost(new BigDecimal("0.9000"));
        Purchase purchase = buildPurchaseForProduct();
        purchase.getDetails().get(0).setPreviousStock(new BigDecimal("100"));
        purchase.getDetails().get(0).setPreviousUnitCost(new BigDecimal("1.8000"));
        when(productRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(cola));
        when(movementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventarioService.revertPurchase(purchase, 2L);

        assertEquals(0, new BigDecimal("100.0000").compareTo(cola.getCurrentStock()));
        assertEquals(0, new BigDecimal("1.8000").compareTo(cola.getUnitCost()));
        ArgumentCaptor<Movement> captor = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(captor.capture());
        Movement m = captor.getValue();
        assertEquals(MovementType.RETURN, m.getType());
        assertEquals(2L, m.getProductId());
    }

    private Purchase buildPurchaseForIngredient() {
        com.josegregoppdev.mibombay.model.purchase.PurchaseDetail detail =
                com.josegregoppdev.mibombay.model.purchase.PurchaseDetail.builder()
                        .id(1L)
                        .ingredientId(1L)
                        .itemName("Beef")
                        .quantity(new BigDecimal("10"))
                        .unitCost(new BigDecimal("14.0000"))
                        .totalCost(new BigDecimal("140.0000"))
                        .build();
        Purchase purchase = Purchase.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .supplierId(2L)
                .supplierName("Proveedor Principal")
                .purchaseDate(java.time.LocalDateTime.now())
                .total(new BigDecimal("140.0000"))
                .userId(1L)
                .active(true)
                .build();
        detail.setPurchase(purchase);
        purchase.setDetails(new ArrayList<>(List.of(detail)));
        return purchase;
    }

    private Purchase buildPurchaseForProduct() {
        com.josegregoppdev.mibombay.model.purchase.PurchaseDetail detail =
                com.josegregoppdev.mibombay.model.purchase.PurchaseDetail.builder()
                        .id(1L)
                        .productId(2L)
                        .itemName("Bottled Cola")
                        .quantity(new BigDecimal("20"))
                        .unitCost(new BigDecimal("0.9000"))
                        .totalCost(new BigDecimal("18.0000"))
                        .build();
        Purchase purchase = Purchase.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .supplierId(2L)
                .supplierName("Proveedor Principal")
                .purchaseDate(java.time.LocalDateTime.now())
                .total(new BigDecimal("18.0000"))
                .userId(1L)
                .active(true)
                .build();
        detail.setPurchase(purchase);
        purchase.setDetails(new ArrayList<>(List.of(detail)));
        return purchase;
    }
}
