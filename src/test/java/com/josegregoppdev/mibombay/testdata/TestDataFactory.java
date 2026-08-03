package com.josegregoppdev.mibombay.testdata;

import com.josegregoppdev.mibombay.dto.company.CompanyDTORequest;
import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.dto.ingredient.IngredientDTO;
import com.josegregoppdev.mibombay.dto.combo.ComboDTO;
import com.josegregoppdev.mibombay.dto.combo.ComboDetailDTO;
import com.josegregoppdev.mibombay.dto.product.ProductDTO;
import com.josegregoppdev.mibombay.dto.recipe.RecipeDTO;
import com.josegregoppdev.mibombay.dto.recipe.RecipeDetailDTO;
import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.combo.ComboDetail;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.model.recipe.RecipeDetail;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    public static CompanyDTORequest createCompanyDTORequest() {
        CompanyDTORequest dto = new CompanyDTORequest();
        dto.setName("My Restaurant");
        dto.setSubdomain("my-restaurant");
        dto.setEmail("contact@test.com");
        dto.setPhone("+54 11 1234-5678");
        dto.setAddress("123 Main St");
        dto.setManagerName("John Doe");
        dto.setManagerDocument("12345678");
        dto.setManagerEmail("admin@test.com");
        dto.setManagerPassword("Password123!");
        dto.setConfirmManagerPassword("Password123!");
        dto.setManagerPhone("+54 11 9876-5432");
        return dto;
    }

    public static Company createCompany() {
        return Company.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .subdomain("my-restaurant")
                .name("My Restaurant")
                .email("contact@test.com")
                .phone("+54 11 1234-5678")
                .address("123 Main St")
                .managerName("John Doe")
                .managerDocumentHash("$2a$12$hashEjemplo")
                .active(true)
                .build();
    }

    public static User createSuperAdmin() {
        return User.builder()
                .id(3L)
                .tenantId("SUPER_ADMIN")
                .email("SuperAdministrador")
                .passwordHash("$2a$12$hashSuperAdmin")
                .fullName("SuperAdministrador")
                .documentHash("$2a$12$hashDoc")
                .role(Role.SUPER_ADMIN)
                .active(true)
                .mustChangePassword(false)
                .lastPasswordChange(LocalDateTime.now())
                .build();
    }

    public static User createAdmin() {
        return createAdminWithTenant(TENANT_ID);
    }

    public static User createAdminWithTenant(String tenantId) {
        return User.builder()
                .id(1L)
                .tenantId(tenantId)
                .email("admin@test.com")
                .passwordHash("$2a$12$hashAdmin")
                .fullName("John Doe")
                .phone("+54 11 9876-5432")
                .documentHash("$2a$12$hashDoc")
                .role(Role.ADMIN)
                .active(true)
                .mustChangePassword(false)
                .lastPasswordChange(LocalDateTime.now())
                .build();
    }

    public static User createCashier() {
        return User.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .email("admin_cashier@test.com")
                .passwordHash("$2a$12$hashCashier")
                .fullName("Cashier My Restaurant")
                .documentHash("$2a$12$hashDoc")
                .role(Role.CASHIER)
                .active(true)
                .mustChangePassword(true)
                .build();
    }

    public static CompanyDTOResponse createCompanyDTOResponse() {
        return CompanyDTOResponse.builder()
                .companyName("My Restaurant")
                .cashierEmail("admin_cashier@test.com")
                .cashierPassword("TempPass123!")
                .build();
    }

    public static IngredientDTO createIngredientDTO() {
        return IngredientDTO.builder()
                .code("CAR-001")
                .name("Beef")
                .category(Category.MEATS)
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .currentUnitCost(new BigDecimal("15.50"))
                .currentStock(new BigDecimal("50"))
                .minimumStock(new BigDecimal("10"))
                .active(true)
                .build();
    }

    public static Ingredient createIngredient() {
        return Ingredient.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .code("CAR-001")
                .name("Beef")
                .category(Category.MEATS)
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .currentUnitCost(new BigDecimal("15.50"))
                .currentStock(new BigDecimal("50"))
                .minimumStock(new BigDecimal("10"))
                .active(true)
                .build();
    }

    public static Ingredient createInactiveIngredient() {
        return Ingredient.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .code("PAN-001")
                .name("Hamburger bun")
                .category(Category.GRAINS)
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .currentUnitCost(new BigDecimal("1.50"))
                .currentStock(new BigDecimal("100"))
                .minimumStock(new BigDecimal("20"))
                .active(false)
                .build();
    }

    public static RecipeDTO createRecipeDTO() {
        RecipeDetailDTO detail = RecipeDetailDTO.builder()
                .ingredientId(1L)
                .quantity(new BigDecimal("0.100"))
                .unitCost(new BigDecimal("15.50"))
                .totalCost(new BigDecimal("1.5500"))
                .build();

        return RecipeDTO.builder()
                .code("HAM-001")
                .name("Classic Hamburger")
                .description("Traditional hamburger with beef patty")
                .productionCost(new BigDecimal("1.5500"))
                .active(true)
                .details(List.of(detail))
                .build();
    }

    public static Recipe createRecipe() {
        RecipeDetail detail = RecipeDetail.builder()
                .id(1L)
                .quantity(new BigDecimal("0.100"))
                .unitOfMeasure(UnitOfMeasure.KILOGRAM)
                .unitCost(new BigDecimal("15.50"))
                .totalCost(new BigDecimal("1.5500"))
                .ingredient(createIngredient())
                .build();

        Recipe recipe = Recipe.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .code("HAM-001")
                .name("Classic Hamburger")
                .description("Traditional hamburger with beef patty")
                .productionCost(new BigDecimal("1.5500"))
                .active(true)
                .build();
        detail.setRecipe(recipe);
        recipe.setDetails(new ArrayList<>(List.of(detail)));
        return recipe;
    }

    public static Recipe createInactiveRecipe() {
        return Recipe.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .code("SOD-001")
                .name("Cola Soda")
                .productionCost(new BigDecimal("2.0000"))
                .active(false)
                .build();
    }

    // -- Product factory methods --

    public static ProductDTO createProductDTO() {
        return ProductDTO.builder()
                .code("PROD-001")
                .name("Classic Hamburger")
                .description("Traditional hamburger with beef patty")
                .category(ProductCategory.FOOD)
                .productType(ProductType.CON_RECETA)
                .sellingPrice(new BigDecimal("8.5000"))
                .unitCost(new BigDecimal("1.5500"))
                .recipeId(1L)
                .active(true)
                .build();
    }

    public static ProductDTO createProductDTOWithoutRecipe() {
        return ProductDTO.builder()
                .code("PROD-002")
                .name("Cola Soda")
                .description("350ml bottled cola")
                .category(ProductCategory.DRINKS)
                .productType(ProductType.SIN_RECETA)
                .sellingPrice(new BigDecimal("2.5000"))
                .unitCost(new BigDecimal("1.2000"))
                .active(true)
                .build();
    }

    public static ProductDTO createAddonProductDTO() {
        return ProductDTO.builder()
                .code("PROD-003")
                .name("Extra Cheese")
                .category(ProductCategory.FOOD)
                .productType(ProductType.ADICIONAL)
                .sellingPrice(new BigDecimal("1.5000"))
                .unitCost(new BigDecimal("0.8000"))
                .active(true)
                .build();
    }

    public static Product createProduct() {
        return Product.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .code("PROD-001")
                .name("Classic Hamburger")
                .description("Traditional hamburger with beef patty")
                .category(ProductCategory.FOOD)
                .productType(ProductType.CON_RECETA)
                .sellingPrice(new BigDecimal("8.5000"))
                .unitCost(new BigDecimal("1.5500"))
                .recipe(createRecipe())
                .active(true)
                .build();
    }

    public static Product createProductWithoutRecipe() {
        return Product.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .code("PROD-002")
                .name("Cola Soda")
                .description("350ml bottled cola")
                .category(ProductCategory.DRINKS)
                .productType(ProductType.SIN_RECETA)
                .sellingPrice(new BigDecimal("2.5000"))
                .unitCost(new BigDecimal("1.2000"))
                .active(true)
                .build();
    }

    public static Product createInactiveProduct() {
        return Product.builder()
                .id(3L)
                .tenantId(TENANT_ID)
                .code("PROD-004")
                .name("Old Product")
                .category(ProductCategory.OTHERS)
                .productType(ProductType.SIN_RECETA)
                .sellingPrice(new BigDecimal("5.0000"))
                .unitCost(new BigDecimal("3.0000"))
                .active(false)
                .build();
    }

    // -- Combo factory methods --

    public static ComboDTO createComboDTO() {
        ComboDetailDTO detail = ComboDetailDTO.builder()
                .productId(1L)
                .quantity(new BigDecimal("1"))
                .unitCost(new BigDecimal("1.5500"))
                .totalCost(new BigDecimal("1.5500"))
                .build();

        return ComboDTO.builder()
                .code("COM-001")
                .name("Hamburger + Fries Combo")
                .description("Classic hamburger with french fries")
                .sellingPrice(new BigDecimal("15.0000"))
                .totalCost(new BigDecimal("1.5500"))
                .active(true)
                .details(List.of(detail))
                .build();
    }

    public static Combo createCombo() {
        Product product = createProduct();

        ComboDetail detail = ComboDetail.builder()
                .id(1L)
                .product(product)
                .quantity(new BigDecimal("1"))
                .unitCost(new BigDecimal("1.5500"))
                .totalCost(new BigDecimal("1.5500"))
                .build();

        Combo combo = Combo.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .code("COM-001")
                .name("Hamburger + Fries Combo")
                .description("Classic hamburger with french fries")
                .sellingPrice(new BigDecimal("15.0000"))
                .totalCost(new BigDecimal("1.5500"))
                .active(true)
                .build();
        detail.setCombo(combo);
        combo.setDetails(new ArrayList<>(List.of(detail)));
        return combo;
    }

    public static Combo createInactiveCombo() {
        return Combo.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .code("COM-002")
                .name("Old Combo")
                .sellingPrice(new BigDecimal("10.0000"))
                .totalCost(new BigDecimal("5.0000"))
                .active(false)
                .build();
    }
}
