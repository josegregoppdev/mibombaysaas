package com.josegregoppdev.mibombay.service.purchase;

import com.josegregoppdev.mibombay.dto.purchase.PurchaseDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDetailDTO;
import com.josegregoppdev.mibombay.mapper.purchase.PurchaseMapper;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.purchase.Purchase;
import com.josegregoppdev.mibombay.model.purchase.PurchaseDetail;
import com.josegregoppdev.mibombay.model.supplier.Supplier;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.purchase.PurchaseRepository;
import com.josegregoppdev.mibombay.repository.supplier.SupplierRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.inventory.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;
    private final SupplierRepository supplierRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventarioService inventarioService;

    @Transactional(readOnly = true)
    public Page<PurchaseDTO> getPaginatedPurchases(String tenantId, String supplierName,
                                                   LocalDate from, LocalDate to, Boolean active, Pageable pageable) {
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;
        Page<PurchaseDTO> result = purchaseRepository.findByFilters(tenantId, supplierName, fromDateTime, toDateTime, active, pageable)
                .map(this::mapToDtoWithDetails);
        resolveUserNames(result.getContent());
        return result;
    }

    @Transactional(readOnly = true)
    public PurchaseDTO getPurchaseById(Long id, String tenantId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));
        PurchaseDTO dto = mapToDtoWithDetails(purchase);
        resolveUserNames(List.of(dto));
        return dto;
    }

    @Transactional
    public PurchaseDTO createPurchaseFromCart(List<PurchaseDetailDTO> cartItems, String tenantId, Long userId,
                                              Long supplierId, String observations, LocalDate purchaseDate) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an empty purchase");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("The purchase date is required");
        }

        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        Purchase purchase = Purchase.builder()
                .tenantId(tenantId)
                .supplierId(supplier.getId())
                .supplierName(supplier.getName())
                .purchaseDate(purchaseDate.atStartOfDay())
                .userId(userId)
                .observations(observations)
                .active(true)
                .details(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseDetailDTO item : cartItems) {
            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setQuantity(item.getQuantity());
            detail.setUnitCost(item.getUnitCost());

            if (item.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findByIdAndTenantId(item.getIngredientId(), tenantId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Ingredient not found: " + item.getIngredientId()));
                if (!Boolean.TRUE.equals(ingredient.getActive())) {
                    throw new IllegalArgumentException(
                            "Ingredient '" + ingredient.getName() + "' is inactive");
                }
                detail.setIngredientId(ingredient.getId());
                detail.setItemName(ingredient.getName());
            } else if (item.getProductId() != null) {
                Product product = productRepository.findByIdAndTenantId(item.getProductId(), tenantId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Product not found: " + item.getProductId()));
                if (!Boolean.TRUE.equals(product.getActive())) {
                    throw new IllegalArgumentException(
                            "Product '" + product.getName() + "' is inactive");
                }
                detail.setProductId(product.getId());
                detail.setItemName(product.getName());
            } else {
                throw new IllegalArgumentException("Each purchase item must reference an ingredient or a product");
            }

            BigDecimal totalCost = detail.getQuantity().multiply(detail.getUnitCost())
                    .setScale(4, RoundingMode.HALF_UP);
            detail.setTotalCost(totalCost);
            purchase.getDetails().add(detail);
            total = total.add(totalCost);
        }

        purchase.setTotal(total);
        purchase = purchaseRepository.save(purchase);

        try {
            inventarioService.addStockForPurchase(purchase);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Could not record stock for the purchase: " + e.getMessage(), e);
        }

        return mapToDtoWithDetails(purchase);
    }

    @Transactional
    public void cancelPurchase(Long purchaseId, String tenantId, Long userId) {
        Purchase purchase = purchaseRepository.findByIdAndTenantId(purchaseId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (!Boolean.TRUE.equals(purchase.getActive())) {
            throw new IllegalArgumentException("The purchase is already cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = purchase.getPurchaseDate().plusDays(1);
        if (now.isAfter(deadline)) {
            throw new IllegalArgumentException(
                    "A purchase can only be cancelled within 24 hours of being registered");
        }

        purchase.setActive(false);
        inventarioService.revertPurchase(purchase, userId);
        purchaseRepository.save(purchase);
    }

    private PurchaseDTO mapToDtoWithDetails(Purchase purchase) {
        PurchaseDTO dto = purchaseMapper.toDto(purchase);
        dto.setDetails(mapDetailsToDto(purchase));
        return dto;
    }

    private List<PurchaseDetailDTO> mapDetailsToDto(Purchase purchase) {
        List<PurchaseDetailDTO> details = new ArrayList<>();
        for (PurchaseDetail detail : purchase.getDetails()) {
            PurchaseDetailDTO detailDto = purchaseMapper.toDetailDto(detail);
            details.add(detailDto);
        }
        return details;
    }

    private void resolveUserNames(List<PurchaseDTO> purchases) {
        Set<Long> userIds = purchases.stream().map(PurchaseDTO::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, String> userNames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
        for (PurchaseDTO dto : purchases) {
            if (dto.getUserId() != null) {
                dto.setUserName(userNames.get(dto.getUserId()));
            }
        }
    }
}