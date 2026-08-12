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
import com.josegregoppdev.mibombay.model.purchase.PurchaseDetail;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.model.recipe.RecipeDetail;
import com.josegregoppdev.mibombay.model.sale.Sale;
import com.josegregoppdev.mibombay.model.sale.SaleDetail;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.movement.MovementRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.service.configuration.TenantConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * InventarioService is the only service allowed to modify stock.
 * Every stock change is recorded as an immutable Movement in the same transaction.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;
    private final MovementRepository movementRepository;
    private final TenantConfigurationService tenantConfigurationService;

    /**
     * Consumes inventory for a confirmed sale. Decrements ingredient stock for
     * products with a recipe (CON_RECETA, ADICIONAL and combo expansion) and
     * decrements product stock for SIN_RECETA items. Every affected item gets a
     * Movement of type SALE. All in the same transaction (caller rolls back on failure).
     */
    @Transactional
    public void consumeForSale(Sale sale) {
        TenantConfigurationDTO config = tenantConfigurationService.getByTenantId(sale.getTenantId());
        boolean allowNegative = Boolean.TRUE.equals(config.getAllowNegativeInventory());

        Map<Long, BigDecimal> ingredientQuantities = new LinkedHashMap<>();
        Map<Long, BigDecimal> productQuantities = new LinkedHashMap<>();

        for (SaleDetail detail : sale.getDetails()) {
            if (detail.getProductId() != null) {
                Product product = productRepository.findByIdAndTenantId(detail.getProductId(), sale.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + detail.getProductId()));
                addProductConsumption(product, detail.getQuantity(), detail.getNotes(),
                        ingredientQuantities, productQuantities);
            } else if (detail.getComboId() != null) {
                Combo combo = comboRepository.findByIdAndTenantId(detail.getComboId(), sale.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException("Combo not found: " + detail.getComboId()));
                for (ComboDetail comboDetail : combo.getDetails()) {
                    if (comboDetail.getProduct() == null) {
                        continue;
                    }
                    Product product = comboDetail.getProduct();
                    if (!sale.getTenantId().equals(product.getTenantId())) {
                        throw new IllegalArgumentException("Product not found: " + product.getId());
                    }
                    BigDecimal qty = detail.getQuantity().multiply(comboDetail.getQuantity());
                    addProductConsumption(product, qty, detail.getNotes(),
                            ingredientQuantities, productQuantities);
                }
            } else {
                throw new IllegalArgumentException("Each sale item must reference a product or a combo");
            }
        }

        applyIngredientConsumption(sale, ingredientQuantities, allowNegative);
        applyProductConsumption(sale, productQuantities, allowNegative);
    }

    /**
     * Adds inventory for a registered purchase. Increases ingredient/product stock,
     * updates the current unit cost to the purchase price and records a Movement of
     * type PURCHASE for every item. All in the same transaction.
     */
    @Transactional
    public void addStockForPurchase(Purchase purchase) {
        for (PurchaseDetail detail : purchase.getDetails()) {
            if (detail.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findByIdAndTenantId(detail.getIngredientId(), purchase.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Ingredient not found: " + detail.getIngredientId()));
                BigDecimal previousStock = ingredient.getCurrentStock();
                BigDecimal previousUnitCost = ingredient.getCurrentUnitCost();
                BigDecimal newStock = previousStock.add(detail.getQuantity());
                ingredient.setCurrentStock(newStock);
                ingredient.setCurrentUnitCost(detail.getUnitCost());
                ingredientRepository.save(ingredient);
                detail.setPreviousStock(previousStock);
                detail.setPreviousUnitCost(previousUnitCost);
                movementRepository.save(buildPurchaseMovement(purchase, null, ingredient.getId(),
                        previousStock, newStock, detail.getQuantity()));
            } else if (detail.getProductId() != null) {
                Product product = productRepository.findByIdAndTenantId(detail.getProductId(), purchase.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Product not found: " + detail.getProductId()));
                BigDecimal previousStock = product.getCurrentStock();
                BigDecimal previousUnitCost = product.getUnitCost();
                BigDecimal newStock = previousStock.add(detail.getQuantity());
                product.setCurrentStock(newStock);
                product.setUnitCost(detail.getUnitCost());
                productRepository.save(product);
                detail.setPreviousStock(previousStock);
                detail.setPreviousUnitCost(previousUnitCost);
                movementRepository.save(buildPurchaseMovement(purchase, product.getId(), null,
                        previousStock, newStock, detail.getQuantity()));
            } else {
                throw new IllegalArgumentException("Each purchase item must reference an ingredient or a product");
            }
        }
    }

    /**
     * Reverts a cancelled purchase. Decrements ingredient/product stock back to the
     * previous value, restores the previous unit cost and records a Movement of type
     * RETURN for every item. All in the same transaction.
     */
    @Transactional
    public void revertPurchase(Purchase purchase, Long userId) {
        for (PurchaseDetail detail : purchase.getDetails()) {
            if (detail.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findByIdAndTenantId(detail.getIngredientId(), purchase.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Ingredient not found: " + detail.getIngredientId()));
                BigDecimal previousStock = ingredient.getCurrentStock();
                BigDecimal newStock = previousStock.subtract(detail.getQuantity());
                ingredient.setCurrentStock(newStock);
                if (detail.getPreviousUnitCost() != null) {
                    ingredient.setCurrentUnitCost(detail.getPreviousUnitCost());
                }
                ingredientRepository.save(ingredient);
                movementRepository.save(buildReturnMovement(purchase, null, ingredient.getId(),
                        previousStock, newStock, detail.getQuantity(), userId));
            } else if (detail.getProductId() != null) {
                Product product = productRepository.findByIdAndTenantId(detail.getProductId(), purchase.getTenantId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Product not found: " + detail.getProductId()));
                BigDecimal previousStock = product.getCurrentStock();
                BigDecimal newStock = previousStock.subtract(detail.getQuantity());
                product.setCurrentStock(newStock);
                if (detail.getPreviousUnitCost() != null) {
                    product.setUnitCost(detail.getPreviousUnitCost());
                }
                productRepository.save(product);
                movementRepository.save(buildReturnMovement(purchase, product.getId(), null,
                        previousStock, newStock, detail.getQuantity(), userId));
            } else {
                throw new IllegalArgumentException("Each purchase item must reference an ingredient or a product");
            }
        }
    }

    private Movement buildPurchaseMovement(Purchase purchase, Long productId, Long ingredientId,
                                           BigDecimal previousStock, BigDecimal newStock, BigDecimal quantity) {
        return Movement.builder()
                .tenantId(purchase.getTenantId())
                .type(MovementType.PURCHASE)
                .date(purchase.getPurchaseDate() != null ? purchase.getPurchaseDate() : LocalDateTime.now())
                .ingredientId(ingredientId)
                .productId(productId)
                .previousStock(previousStock)
                .newStock(newStock)
                .quantity(quantity)
                .referenceId(purchase.getId())
                .userId(purchase.getUserId())
                .build();
    }

    private Movement buildReturnMovement(Purchase purchase, Long productId, Long ingredientId,
                                         BigDecimal previousStock, BigDecimal newStock, BigDecimal quantity, Long userId) {
        return Movement.builder()
                .tenantId(purchase.getTenantId())
                .type(MovementType.RETURN)
                .date(LocalDateTime.now())
                .ingredientId(ingredientId)
                .productId(productId)
                .previousStock(previousStock)
                .newStock(newStock)
                .quantity(quantity)
                .referenceId(purchase.getId())
                .userId(userId)
                .build();
    }

    private void addProductConsumption(Product product, BigDecimal qty, String notes,
                                       Map<Long, BigDecimal> ingredientQuantities,
                                       Map<Long, BigDecimal> productQuantities) {
        if (product.getProductType() == ProductType.SIN_RECETA) {
            productQuantities.merge(product.getId(), qty, BigDecimal::add);
        } else if (product.getRecipe() != null) {
            addRecipeConsumption(product.getRecipe(), qty, notes, ingredientQuantities);
        } else {
            throw new IllegalArgumentException(
                    "Product '" + product.getName() + "' must have a recipe to control its inventory");
        }
    }

    private void addRecipeConsumption(Recipe recipe, BigDecimal qty, String notes,
                                      Map<Long, BigDecimal> ingredientQuantities) {
        Set<String> excluded = parseExcludedIngredients(notes);
        for (RecipeDetail detail : recipe.getDetails()) {
            Ingredient ingredient = detail.getIngredient();
            if (ingredient == null || excluded.contains(ingredient.getName())) {
                continue;
            }
            BigDecimal amount = detail.getQuantity().multiply(qty);
            ingredientQuantities.merge(ingredient.getId(), amount, BigDecimal::add);
        }
    }

    private void applyIngredientConsumption(Sale sale, Map<Long, BigDecimal> quantities, boolean allowNegative) {
        if (quantities.isEmpty()) {
            return;
        }
        List<Ingredient> ingredients = ingredientRepository.findByTenantIdAndIdIn(sale.getTenantId(), quantities.keySet());
        Map<Long, Ingredient> byId = ingredients.stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        if (byId.size() != quantities.size()) {
            throw new IllegalArgumentException("Some ingredients were not found");
        }

        if (!allowNegative) {
            for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
                Ingredient ingredient = byId.get(entry.getKey());
                if (ingredient.getCurrentStock().compareTo(entry.getValue()) < 0) {
                    throw new IllegalArgumentException(
                            "Insufficient stock of ingredient '" + ingredient.getName() + "'");
                }
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
            Ingredient ingredient = byId.get(entry.getKey());
            BigDecimal previousStock = ingredient.getCurrentStock();
            BigDecimal newStock = previousStock.subtract(entry.getValue());
            ingredient.setCurrentStock(newStock);
            ingredientRepository.save(ingredient);
            movementRepository.save(buildSaleMovement(sale, null, ingredient.getId(),
                    previousStock, newStock, entry.getValue()));
        }
    }

    private void applyProductConsumption(Sale sale, Map<Long, BigDecimal> quantities, boolean allowNegative) {
        if (quantities.isEmpty()) {
            return;
        }
        List<Product> products = productRepository.findByTenantIdAndIdIn(sale.getTenantId(), quantities.keySet());
        Map<Long, Product> byId = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (byId.size() != quantities.size()) {
            throw new IllegalArgumentException("Some products were not found");
        }

        if (!allowNegative) {
            for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
                Product product = byId.get(entry.getKey());
                if (product.getCurrentStock().compareTo(entry.getValue()) < 0) {
                    throw new IllegalArgumentException(
                            "Insufficient stock of product '" + product.getName() + "'");
                }
            }
        }

        for (Map.Entry<Long, BigDecimal> entry : quantities.entrySet()) {
            Product product = byId.get(entry.getKey());
            BigDecimal previousStock = product.getCurrentStock();
            BigDecimal newStock = previousStock.subtract(entry.getValue());
            product.setCurrentStock(newStock);
            productRepository.save(product);
            movementRepository.save(buildSaleMovement(sale, product.getId(), null,
                    previousStock, newStock, entry.getValue()));
        }
    }

    private Movement buildSaleMovement(Sale sale, Long productId, Long ingredientId,
                                       BigDecimal previousStock, BigDecimal newStock, BigDecimal quantity) {
        Long userId = sale.getCashier() != null ? sale.getCashier().getId() : null;
        return Movement.builder()
                .tenantId(sale.getTenantId())
                .type(MovementType.SALE)
                .date(sale.getSaleDate() != null ? sale.getSaleDate() : LocalDateTime.now())
                .ingredientId(ingredientId)
                .productId(productId)
                .previousStock(previousStock)
                .newStock(newStock)
                .quantity(quantity)
                .referenceId(sale.getId())
                .userId(userId)
                .build();
    }

    private Set<String> parseExcludedIngredients(String notes) {
        Set<String> excluded = new HashSet<>();
        if (notes == null || notes.isBlank()) {
            return excluded;
        }
        String text = notes.trim();
        if (text.toLowerCase(Locale.ROOT).startsWith("without:")) {
            String rest = text.substring(text.indexOf(':') + 1);
            for (String part : rest.split(",")) {
                String name = part.trim();
                if (!name.isEmpty()) {
                    excluded.add(name);
                }
            }
        }
        return excluded;
    }
}
