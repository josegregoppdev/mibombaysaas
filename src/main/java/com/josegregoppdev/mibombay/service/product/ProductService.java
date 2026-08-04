package com.josegregoppdev.mibombay.service.product;

import com.josegregoppdev.mibombay.dto.product.ProductDTO;
import com.josegregoppdev.mibombay.dto.sale.IngredientOptionDTO;
import com.josegregoppdev.mibombay.dto.sale.PosRecipeDataDTO;
import com.josegregoppdev.mibombay.mapper.product.ProductMapper;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.model.recipe.RecipeDetail;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RecipeRepository recipeRepository;
    private final ComboRepository comboRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> getPaginatedProducts(String tenantId, String name,
                                                  ProductCategory category,
                                                  ProductType productType,
                                                  Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? name : null;
        return productRepository.findByFilters(tenantId, nameParam, category, productType, pageable)
                .map(this::mapToDtoWithRecipeInfo);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getPaginatedProducts(String tenantId, Pageable pageable) {
        return getPaginatedProducts(tenantId, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getPaginatedActiveProducts(String tenantId, Pageable pageable) {
        return productRepository.findByTenantIdAndActiveTrue(tenantId, pageable)
                .map(this::mapToDtoWithRecipeInfo);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapToDtoWithRecipeInfo(product);
    }

    @Transactional
    public ProductDTO createNewProduct(ProductDTO dto, String tenantId) {
        if (productRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("A product with that code already exists");
        }
        if (productRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("A product with that name already exists");
        }

        Product product = productMapper.toEntity(dto);
        product.setTenantId(tenantId);
        product.setActive(true);

        linkRecipe(product, dto);
        product = productRepository.save(product);
        return mapToDtoWithRecipeInfo(product);
    }

    @Transactional
    public ProductDTO updateExistingProduct(Long id, ProductDTO dto, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getCode().equals(dto.getCode())
                && productRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("A product with that code already exists");
        }
        if (!product.getName().equals(dto.getName())
                && productRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("A product with that name already exists");
        }

        product.setCode(dto.getCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setProductType(dto.getProductType());
        product.setSellingPrice(dto.getSellingPrice());
        product.setUnitCost(dto.getUnitCost() != null ? dto.getUnitCost() : product.getUnitCost());

        linkRecipe(product, dto);
        product = productRepository.save(product);
        return mapToDtoWithRecipeInfo(product);
    }

    @Transactional
    public void toggleProductActiveStatus(Long id, String tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setActive(!product.getActive());
        productRepository.save(product);
    }

    @Transactional
    public void syncProductCostsForRecipe(Long recipeId, String tenantId, BigDecimal newProductionCost) {
        List<Product> products = productRepository.findByRecipeIdAndTenantId(recipeId, tenantId);
        for (Product product : products) {
            product.setUnitCost(newProductionCost);
            productRepository.save(product);
        }
    }

    @Transactional(readOnly = true)
    public PosRecipeDataDTO getRecipeDataForPos(String tenantId) {
        PosRecipeDataDTO result = PosRecipeDataDTO.builder()
                .productIngredients(new HashMap<>())
                .comboProductIds(new HashMap<>())
                .productNames(new HashMap<>())
                .build();

        List<Product> products = productRepository.findAllActiveWithRecipeAndIngredients(tenantId);
        for (Product product : products) {
            List<IngredientOptionDTO> ingredients = new ArrayList<>();
            if (product.getRecipe() != null && product.getRecipe().getDetails() != null) {
                for (RecipeDetail detail : product.getRecipe().getDetails()) {
                    if (detail.getIngredient() != null) {
                        ingredients.add(IngredientOptionDTO.builder()
                                .id(detail.getIngredient().getId())
                                .name(detail.getIngredient().getName())
                                .build());
                    }
                }
            }
            result.getProductIngredients().put(String.valueOf(product.getId()), ingredients);
            result.getProductNames().put(String.valueOf(product.getId()), product.getName());
        }

        List<Combo> combos = comboRepository.findAllActiveWithDetailsAndProducts(tenantId);
        for (Combo combo : combos) {
            List<String> productIds = new ArrayList<>();
            if (combo.getDetails() != null) {
                combo.getDetails().stream()
                        .filter(d -> d.getProduct() != null)
                        .forEach(d -> productIds.add(String.valueOf(d.getProduct().getId())));
            }
            result.getComboProductIds().put(String.valueOf(combo.getId()), productIds);
        }

        return result;
    }

    private void linkRecipe(Product product, ProductDTO dto) {
        if (product.getProductType() == ProductType.CON_RECETA) {
            if (dto.getRecipeId() == null) {
                throw new IllegalArgumentException("A recipe is required for products with recipe");
            }
            Recipe recipe = recipeRepository.findByIdAndTenantId(dto.getRecipeId(), product.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
            product.setRecipe(recipe);
            product.setUnitCost(recipe.getProductionCost());
        } else if (product.getProductType() == ProductType.ADICIONAL && dto.getRecipeId() != null) {
            Recipe recipe = recipeRepository.findByIdAndTenantId(dto.getRecipeId(), product.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));
            product.setRecipe(recipe);
            product.setUnitCost(recipe.getProductionCost());
        } else {
            product.setRecipe(null);
        }
    }

    private ProductDTO mapToDtoWithRecipeInfo(Product product) {
        ProductDTO dto = productMapper.toDto(product);
        if (product.getRecipe() != null) {
            dto.setRecipeId(product.getRecipe().getId());
            dto.setRecipeName(product.getRecipe().getName());
            dto.setRecipeProductionCost(product.getRecipe().getProductionCost());
        }
        return dto;
    }
}
