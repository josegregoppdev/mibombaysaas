package com.josegregoppdev.mibombay.service.purchase;

import com.josegregoppdev.mibombay.dto.purchase.PurchaseCartSubmissionDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDetailDTO;
import com.josegregoppdev.mibombay.mapper.purchase.PurchaseMapper;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.purchase.Purchase;
import com.josegregoppdev.mibombay.model.supplier.Supplier;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.purchase.PurchaseRepository;
import com.josegregoppdev.mibombay.repository.supplier.SupplierRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.inventory.InventarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private PurchaseMapper purchaseMapper;
    @Mock private SupplierRepository supplierRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventarioService inventarioService;

    @InjectMocks private PurchaseService purchaseService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void createPurchaseFromCart_ingredientAndProduct_updatesStockAndCreatesPurchase() {
        Supplier supplier = createDefaultSupplier();
        Ingredient beef = createIngredient();
        Product cola = createProductSinReceta();
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(supplier));
        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(beef));
        when(productRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(cola));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(purchaseMapper.toDto(any(Purchase.class))).thenReturn(createPurchaseDTO());

        PurchaseCartSubmissionDTO submission = createPurchaseCartSubmissionDTO();
        purchaseService.createPurchaseFromCart(
                submission.getItems(), TENANT_ID, 1L, submission.getSupplierId(),
                submission.getObservations(), submission.getPurchaseDate());

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        Purchase saved = captor.getValue();
        assertEquals(0, new java.math.BigDecimal("158.0000").setScale(4).compareTo(saved.getTotal()));
        assertEquals(2, saved.getDetails().size());
        assertEquals("Beef", saved.getDetails().get(0).getItemName());
        assertEquals("Proveedor Principal", saved.getSupplierName());
        verify(inventarioService).addStockForPurchase(saved);
    }

    @Test
    void createPurchaseFromCart_emptyCart_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        List.of(), TENANT_ID, 1L, 2L, null, java.time.LocalDate.now()));
        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    void createPurchaseFromCart_nullCart_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        null, TENANT_ID, 1L, 2L, null, java.time.LocalDate.now()));
    }

    @Test
    void createPurchaseFromCart_nullDate_throws() {
        PurchaseCartSubmissionDTO submission = createPurchaseCartSubmissionDTO();
        assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        submission.getItems(), TENANT_ID, 1L, 2L, null, null));
    }

    @Test
    void createPurchaseFromCart_supplierNotFound_throws() {
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.empty());
        PurchaseCartSubmissionDTO submission = createPurchaseCartSubmissionDTO();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        submission.getItems(), TENANT_ID, 1L, submission.getSupplierId(),
                        null, submission.getPurchaseDate()));
        assertTrue(ex.getMessage().contains("Supplier not found"));
    }

    @Test
    void createPurchaseFromCart_ingredientNotFound_throws() {
        Supplier supplier = createDefaultSupplier();
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(supplier));
        when(ingredientRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.empty());

        PurchaseCartSubmissionDTO submission = createPurchaseCartSubmissionDTO();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        submission.getItems(), TENANT_ID, 1L, submission.getSupplierId(),
                        null, submission.getPurchaseDate()));
        assertTrue(ex.getMessage().contains("Ingredient not found"));
    }

    @Test
    void createPurchaseFromCart_inactiveIngredient_throws() {
        Supplier supplier = createDefaultSupplier();
        Ingredient inactive = createInactiveIngredient();
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(supplier));
        when(ingredientRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(inactive));

        PurchaseDetailDTO item = PurchaseDetailDTO.builder()
                .ingredientId(2L).quantity(new java.math.BigDecimal("5"))
                .unitCost(new java.math.BigDecimal("1.0000")).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        List.of(item), TENANT_ID, 1L, 2L, null, java.time.LocalDate.now()));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    void createPurchaseFromCart_itemWithNoReference_throws() {
        Supplier supplier = createDefaultSupplier();
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(supplier));

        PurchaseDetailDTO item = PurchaseDetailDTO.builder()
                .quantity(new java.math.BigDecimal("5"))
                .unitCost(new java.math.BigDecimal("1.0000")).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.createPurchaseFromCart(
                        List.of(item), TENANT_ID, 1L, 2L, null, java.time.LocalDate.now()));
        assertTrue(ex.getMessage().contains("ingredient or a product"));
    }

    @Test
    void cancelPurchase_within24Hours_marksInactiveAndReverts() {
        Purchase purchase = createPurchase();
        when(purchaseRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(purchase));

        purchaseService.cancelPurchase(1L, TENANT_ID, 1L);

        assertFalse(purchase.getActive());
        verify(inventarioService).revertPurchase(purchase, 1L);
        verify(purchaseRepository).save(purchase);
    }

    @Test
    void cancelPurchase_after24Hours_throws() {
        Purchase purchase = createPurchaseWithOldDate();
        when(purchaseRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(purchase));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.cancelPurchase(1L, TENANT_ID, 1L));
        assertTrue(ex.getMessage().contains("24 hours"));
        assertTrue(purchase.getActive());
        verify(inventarioService, never()).revertPurchase(any(), any());
    }

    @Test
    void cancelPurchase_alreadyCancelled_throws() {
        Purchase purchase = createCancelledPurchase();
        when(purchaseRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(purchase));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> purchaseService.cancelPurchase(1L, TENANT_ID, 1L));
        assertTrue(ex.getMessage().contains("already cancelled"));
        verify(inventarioService, never()).revertPurchase(any(), any());
    }

    @Test
    void cancelPurchase_notFound_throws() {
        when(purchaseRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> purchaseService.cancelPurchase(1L, TENANT_ID, 1L));
    }

    @Test
    void getPurchaseById_notFound_throws() {
        when(purchaseRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> purchaseService.getPurchaseById(1L, TENANT_ID));
    }

    @Test
    void getPaginatedPurchases_callsRepositoryWithFilters() {
        when(purchaseRepository.findByFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(createPurchase())));
        when(purchaseMapper.toDto(any(Purchase.class))).thenReturn(createPurchaseDTO());

        var result = purchaseService.getPaginatedPurchases(
                TENANT_ID, "Proveedor", null, null, true, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        verify(purchaseRepository).findByFilters(
                eq(TENANT_ID), eq("Proveedor"), eq(null), eq(null), eq(true), any(Pageable.class));
    }
}