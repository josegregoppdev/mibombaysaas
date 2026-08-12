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
import com.josegregoppdev.mibombay.model.sale.Sale;
import com.josegregoppdev.mibombay.model.sale.SaleDetail;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;

import com.josegregoppdev.mibombay.model.customer.Customer;
import com.josegregoppdev.mibombay.dto.movement.MovementDTO;
import com.josegregoppdev.mibombay.model.movement.Movement;
import com.josegregoppdev.mibombay.model.movement.MovementType;
import com.josegregoppdev.mibombay.dto.supplier.SupplierDTO;
import com.josegregoppdev.mibombay.model.supplier.Supplier;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDetailDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseCartSubmissionDTO;
import com.josegregoppdev.mibombay.model.purchase.Purchase;
import com.josegregoppdev.mibombay.model.purchase.PurchaseDetail;

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

    // -- Add-on product factory methods --

    public static Product createAddonProduct() {
        return Product.builder()
                .id(4L)
                .tenantId(TENANT_ID)
                .code("ADD-001")
                .name("Extra Cheese")
                .category(ProductCategory.FOOD)
                .productType(ProductType.ADICIONAL)
                .sellingPrice(new BigDecimal("1.5000"))
                .unitCost(new BigDecimal("0.8000"))
                .active(true)
                .build();
    }

    public static Product createAddonProduct2() {
        return Product.builder()
                .id(5L)
                .tenantId(TENANT_ID)
                .code("ADD-002")
                .name("Extra Meat")
                .category(ProductCategory.FOOD)
                .productType(ProductType.ADICIONAL)
                .sellingPrice(new BigDecimal("2.5000"))
                .unitCost(new BigDecimal("1.8000"))
                .active(true)
                .build();
    }

    public static Product createProductSinReceta() {
        return Product.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .code("PROD-002")
                .name("Bottled Cola")
                .category(ProductCategory.DRINKS)
                .productType(ProductType.SIN_RECETA)
                .sellingPrice(new BigDecimal("3.5000"))
                .unitCost(new BigDecimal("1.8000"))
                .currentStock(new BigDecimal("100"))
                .minimumStock(new BigDecimal("20"))
                .active(true)
                .build();
    }

    public static Product createProductWithRecipe() {
        Recipe recipe = createRecipe();
        return Product.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .code("PROD-001")
                .name("Classic Hamburger")
                .category(ProductCategory.FOOD)
                .productType(ProductType.CON_RECETA)
                .sellingPrice(new BigDecimal("12.0000"))
                .unitCost(recipe.getProductionCost())
                .recipe(recipe)
                .active(true)
                .build();
    }

    public static Combo createComboWithDetails(ComboDetail... details) {
        Combo combo = Combo.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .code("COM-001")
                .name("Test Combo")
                .sellingPrice(new BigDecimal("15.0000"))
                .totalCost(new BigDecimal("5.0000"))
                .active(true)
                .details(new ArrayList<>())
                .build();
        for (ComboDetail d : details) {
            d.setCombo(combo);
            combo.getDetails().add(d);
        }
        return combo;
    }

    public static Customer createCustomer() {
        return Customer.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .fullName("Juan Perez")
                .documentEncrypted("enc:doc")
                .phoneEncrypted("enc:phone")
                .address("Carrera 1 # 1-01")
                .documentLookupHash("hash-doc")
                .phoneLookupHash("hash-phone")
                .active(true)
                .isDefault(false)
                .build();
    }

    public static Customer createDefaultCustomer() {
        return Customer.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .fullName("Consumidor Final")
                .documentEncrypted("enc:99999999")
                .phoneEncrypted("enc:123456789")
                .address("Cucuta")
                .documentLookupHash("hash-99999999")
                .phoneLookupHash("hash-123456789")
                .active(true)
                .isDefault(true)
                .build();
    }

    // -- Supplier factory methods --

    public static Supplier createSupplier() {
        return Supplier.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .name("Distribuciones San Fernando")
                .documentEncrypted("enc:900000000")
                .phoneEncrypted("enc:3115550000")
                .email("ventas@sanfernando.com")
                .address("Calle 5 # 8-90")
                .contactName("Carlos Ruiz")
                .documentLookupHash("hash-doc")
                .active(true)
                .build();
    }

    public static Supplier createInactiveSupplier() {
        Supplier supplier = createSupplier();
        supplier.setActive(false);
        return supplier;
    }

    public static Supplier createDefaultSupplier() {
        return Supplier.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .name("Proveedor Principal")
                .documentEncrypted("enc:900001111")
                .phoneEncrypted("enc:3110001111")
                .email("proveedor@mibombay.com")
                .address("Cucuta")
                .contactName("Proveedor Principal")
                .documentLookupHash("hash-default-doc")
                .phoneLookupHash("hash-default-phone")
                .isDefault(true)
                .active(true)
                .build();
    }

    public static SupplierDTO createSupplierDTO() {
        return SupplierDTO.builder()
                .id(1L)
                .name("Distribuciones San Fernando")
                .document("900000000")
                .documentMasked("90******00")
                .phone("3115550000")
                .phoneMasked("31******00")
                .email("ventas@sanfernando.com")
                .address("Calle 5 # 8-90")
                .contactName("Carlos Ruiz")
                .active(true)
                .build();
    }

    public static SupplierDTO createNewSupplierDTO() {
        return SupplierDTO.builder()
                .name("Inversiones La Esmeralda")
                .document("800111222")
                .phone("3001234567")
                .email("contacto@esmeralda.com")
                .address("Avenida 3 # 1-20")
                .contactName("Maria Lopez")
                .active(true)
                .build();
    }

    public static Sale createSale(java.util.List<com.josegregoppdev.mibombay.model.sale.SaleDetail> details) {
        return Sale.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .saleDate(LocalDateTime.now())
                .total(new BigDecimal("12.0000"))
                .state(com.josegregoppdev.mibombay.model.sale.SaleState.CONFIRMADA)
                .cashier(createCashier())
                .details(details != null ? details : new ArrayList<>())
                .build();
    }

    // -- Movement factory methods --

    public static Movement createMovement() {
        return Movement.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .type(MovementType.SALE)
                .date(LocalDateTime.now())
                .ingredientId(1L)
                .previousStock(new BigDecimal("50"))
                .newStock(new BigDecimal("49"))
                .quantity(new BigDecimal("1"))
                .referenceId(1L)
                .userId(2L)
                .build();
    }

    public static Movement createMovementForProduct() {
        return Movement.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .type(MovementType.SALE)
                .date(LocalDateTime.now())
                .productId(2L)
                .previousStock(new BigDecimal("100"))
                .newStock(new BigDecimal("99"))
                .quantity(new BigDecimal("1"))
                .referenceId(1L)
                .userId(2L)
                .build();
    }

    public static MovementDTO createMovementDTO() {
        return MovementDTO.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .type(MovementType.SALE)
                .typeDisplayName("Sale")
                .date(LocalDateTime.now())
                .ingredientId(1L)
                .ingredientName("Beef")
                .previousStock(new BigDecimal("50"))
                .newStock(new BigDecimal("49"))
                .quantity(new BigDecimal("1"))
                .referenceId(1L)
                .userId(2L)
                .userName("Cashier My Restaurant")
                .build();
    }

    // -- Purchase factory methods --

    public static PurchaseDetailDTO createPurchaseDetailDTOForIngredient() {
        return PurchaseDetailDTO.builder()
                .ingredientId(1L)
                .quantity(new BigDecimal("10"))
                .unitCost(new BigDecimal("14.0000"))
                .totalCost(new BigDecimal("140.0000"))
                .build();
    }

    public static PurchaseDetailDTO createPurchaseDetailDTOForProduct() {
        return PurchaseDetailDTO.builder()
                .productId(2L)
                .quantity(new BigDecimal("20"))
                .unitCost(new BigDecimal("0.9000"))
                .totalCost(new BigDecimal("18.0000"))
                .build();
    }

    public static PurchaseCartSubmissionDTO createPurchaseCartSubmissionDTO() {
        java.util.List<PurchaseDetailDTO> items = new ArrayList<>();
        items.add(createPurchaseDetailDTOForIngredient());
        items.add(createPurchaseDetailDTOForProduct());
        PurchaseCartSubmissionDTO dto = new PurchaseCartSubmissionDTO();
        dto.setItems(items);
        dto.setSupplierId(2L);
        dto.setPurchaseDate(java.time.LocalDate.now());
        dto.setObservations("Weekly restock");
        return dto;
    }

    public static Purchase createPurchase() {
        PurchaseDetail detail = PurchaseDetail.builder()
                .id(1L)
                .ingredientId(1L)
                .itemName("Beef")
                .quantity(new BigDecimal("10"))
                .unitCost(new BigDecimal("14.0000"))
                .totalCost(new BigDecimal("140.0000"))
                .previousStock(new BigDecimal("50"))
                .previousUnitCost(new BigDecimal("15.5000"))
                .build();

        Purchase purchase = Purchase.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .supplierId(2L)
                .supplierName("Proveedor Principal")
                .purchaseDate(LocalDateTime.now())
                .total(new BigDecimal("140.0000"))
                .userId(1L)
                .observations("Weekly restock")
                .active(true)
                .build();
        detail.setPurchase(purchase);
        purchase.setDetails(new ArrayList<>(List.of(detail)));
        return purchase;
    }

    public static Purchase createCancelledPurchase() {
        Purchase purchase = createPurchase();
        purchase.setActive(false);
        return purchase;
    }

    public static Purchase createPurchaseWithOldDate() {
        Purchase purchase = createPurchase();
        purchase.setPurchaseDate(java.time.LocalDateTime.now().minusHours(30));
        return purchase;
    }

    public static PurchaseDTO createPurchaseDTO() {
        return PurchaseDTO.builder()
                .id(1L)
                .supplierId(2L)
                .supplierName("Proveedor Principal")
                .purchaseDate(LocalDateTime.now())
                .total(new BigDecimal("140.0000"))
                .userId(1L)
                .userName("John Doe")
                .observations("Weekly restock")
                .active(true)
                .build();
    }
}
