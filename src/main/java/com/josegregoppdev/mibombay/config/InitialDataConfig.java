package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.model.recipe.Recipe;
import com.josegregoppdev.mibombay.model.recipe.RecipeDetail;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.recipe.RecipeRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.user.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    @Override
    public void run(String... args) {
        createSuperAdministrator();
        createDemoRestaurant();
    }

    private void createSuperAdministrator() {
        if (!userRepository.existsByEmail("SuperAdministrador@gmail.com")) {
            User superAdmin = User.builder()
                    .tenantId("SUPER_ADMIN")
                    .email("SuperAdministrador@gmail.com")
                    .passwordHash(passwordEncoder.encode("Mora.Kristoff_26123009"))
                    .fullName("SuperAdministrador")
                    .documentHash(passwordEncoder.encode("00000000"))
                    .role(Role.SUPER_ADMIN)
                    .active(true)
                    .mustChangePassword(false)
                    .build();
            userRepository.save(superAdmin);
            log.info("SuperAdministrator created successfully");
        } else {
            log.debug("SuperAdministrator already exists");
        }
    }

    private void createDemoRestaurant() {
        String tenantId;

        if (companyRepository.existsBySubdomain("1")) {
            log.debug("Demo restaurant already exists");
            tenantId = companyRepository.findBySubdomain("1").orElseThrow().getTenantId();
        } else {
            tenantId = "tnt_demo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 28);

            Company company = Company.builder()
                    .tenantId(tenantId)
                    .subdomain("1")
                    .name("Demo Restaurant")
                    .email("demo@mibombay.com")
                    .phone("+57 300 000 0000")
                    .address("Carrera 1 # 1-01")
                    .managerName("Demo Admin")
                    .managerDocumentHash(passwordEncoder.encode("1234567890"))
                    .active(true)
                    .build();
            companyRepository.save(company);

            User admin = User.builder()
                    .tenantId(tenantId)
                    .email("demo@mibombay.com")
                    .passwordHash(passwordEncoder.encode("demo1234"))
                    .fullName("Demo Admin")
                    .phone("+57 300 000 0000")
                    .documentHash(passwordEncoder.encode("1234567890"))
                    .role(Role.ADMIN)
                    .active(true)
                    .mustChangePassword(false)
                    .lastPasswordChange(LocalDateTime.now())
                    .build();
            userRepository.save(admin);

            String cashierPassword = passwordGeneratorService.generateTemporaryPassword();

            User cashier = User.builder()
                    .tenantId(tenantId)
                    .email("demo_cashier@mibombay.com")
                    .passwordHash(passwordEncoder.encode(cashierPassword))
                    .fullName("Demo Cashier")
                    .documentHash(passwordEncoder.encode("1234567890"))
                    .role(Role.CASHIER)
                    .active(true)
                    .mustChangePassword(true)
                    .build();
            userRepository.save(cashier);

            log.info("Demo restaurant created: subdomain='1', admin=demo@mibombay.com / demo1234, cashier=demo_cashier@mibombay.com / {}", cashierPassword);
        }

        createDemoIngredients(tenantId);
        createDemoRecipes(tenantId);
        createDemoProducts(tenantId);
    }

    private void createDemoIngredients(String tenantId) {
        if (ingredientRepository.existsByCodeAndTenantId("CAR-001", tenantId)) {
            log.debug("Demo ingredients already exist");
            return;
        }

        List<Ingredient> ingredients = List.of(
                Ingredient.builder()
                        .tenantId(tenantId).code("CAR-001").name("Ground beef")
                        .category(Category.MEATS).unitOfMeasure(UnitOfMeasure.KILOGRAM)
                        .currentUnitCost(new BigDecimal("18.00")).currentStock(new BigDecimal("30")).minimumStock(new BigDecimal("10")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("PAN-001").name("Hamburger bun")
                        .category(Category.GRAINS).unitOfMeasure(UnitOfMeasure.UNIT)
                        .currentUnitCost(new BigDecimal("1.50")).currentStock(new BigDecimal("100")).minimumStock(new BigDecimal("30")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("PAN-002").name("Hot dog bun")
                        .category(Category.GRAINS).unitOfMeasure(UnitOfMeasure.UNIT)
                        .currentUnitCost(new BigDecimal("1.20")).currentStock(new BigDecimal("80")).minimumStock(new BigDecimal("20")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("LEC-001").name("American cheese slices")
                        .category(Category.DAIRY).unitOfMeasure(UnitOfMeasure.UNIT)
                        .currentUnitCost(new BigDecimal("0.80")).currentStock(new BigDecimal("150")).minimumStock(new BigDecimal("50")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("VER-001").name("Crisp lettuce")
                        .category(Category.VEGETABLES).unitOfMeasure(UnitOfMeasure.KILOGRAM)
                        .currentUnitCost(new BigDecimal("4.00")).currentStock(new BigDecimal("10")).minimumStock(new BigDecimal("5")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("VER-002").name("Tomato")
                        .category(Category.VEGETABLES).unitOfMeasure(UnitOfMeasure.KILOGRAM)
                        .currentUnitCost(new BigDecimal("3.50")).currentStock(new BigDecimal("15")).minimumStock(new BigDecimal("5")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("PAP-001").name("Frozen french fries")
                        .category(Category.OTHERS).unitOfMeasure(UnitOfMeasure.KILOGRAM)
                        .currentUnitCost(new BigDecimal("5.00")).currentStock(new BigDecimal("40")).minimumStock(new BigDecimal("10")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("ACE-001").name("Frying oil")
                        .category(Category.OTHERS).unitOfMeasure(UnitOfMeasure.LITER)
                        .currentUnitCost(new BigDecimal("7.50")).currentStock(new BigDecimal("20")).minimumStock(new BigDecimal("5")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("SAL-001").name("Tomato sauce (ketchup)")
                        .category(Category.CONDIMENTS).unitOfMeasure(UnitOfMeasure.LITER)
                        .currentUnitCost(new BigDecimal("4.00")).currentStock(new BigDecimal("10")).minimumStock(new BigDecimal("3")).active(true).build(),
                Ingredient.builder()
                        .tenantId(tenantId).code("SAL-002").name("Mayonnaise")
                        .category(Category.CONDIMENTS).unitOfMeasure(UnitOfMeasure.LITER)
                        .currentUnitCost(new BigDecimal("5.00")).currentStock(new BigDecimal("10")).minimumStock(new BigDecimal("3")).active(true).build()
        );

        ingredientRepository.saveAll(ingredients);
        log.info("10 demo ingredients created for the restaurant");
    }

    private void createDemoRecipes(String tenantId) {
        if (recipeRepository.existsByCodeAndTenantId("HAM-001", tenantId)) {
            log.debug("Demo recipes already exist");
            return;
        }

        Ingredient groundBeef = ingredientRepository.findByCodeAndTenantId("CAR-001", tenantId).orElseThrow();
        Ingredient hamburgerBun = ingredientRepository.findByCodeAndTenantId("PAN-001", tenantId).orElseThrow();
        Ingredient hotDogBun = ingredientRepository.findByCodeAndTenantId("PAN-002", tenantId).orElseThrow();
        Ingredient cheese = ingredientRepository.findByCodeAndTenantId("LEC-001", tenantId).orElseThrow();
        Ingredient lettuce = ingredientRepository.findByCodeAndTenantId("VER-001", tenantId).orElseThrow();
        Ingredient tomato = ingredientRepository.findByCodeAndTenantId("VER-002", tenantId).orElseThrow();
        Ingredient frenchFries = ingredientRepository.findByCodeAndTenantId("PAP-001", tenantId).orElseThrow();
        Ingredient fryingOil = ingredientRepository.findByCodeAndTenantId("ACE-001", tenantId).orElseThrow();

        Recipe classicHamburger = buildRecipe(tenantId, "HAM-001", "Classic Hamburger",
                "Traditional hamburger with beef patty, cheese, lettuce and tomato",
                List.of(
                        buildDetail(groundBeef, new BigDecimal("0.150"), "Extra point"),
                        buildDetail(hamburgerBun, new BigDecimal("1"), null),
                        buildDetail(cheese, new BigDecimal("2"), null),
                        buildDetail(lettuce, new BigDecimal("0.020"), null),
                        buildDetail(tomato, new BigDecimal("0.030"), null)
                ));

        Recipe hotDog = buildRecipe(tenantId, "HOT-001", "Hot Dog",
                "Classic hot dog with beef sausage, lettuce and tomato",
                List.of(
                        buildDetail(hotDogBun, new BigDecimal("1"), null),
                        buildDetail(groundBeef, new BigDecimal("0.080"), "Ground for sausage"),
                        buildDetail(lettuce, new BigDecimal("0.010"), null),
                        buildDetail(tomato, new BigDecimal("0.020"), null)
                ));

        Recipe frenchFriesRecipe = buildRecipe(tenantId, "FRI-001", "French Fries",
                "Crispy golden french fries",
                List.of(
                        buildDetail(frenchFries, new BigDecimal("0.200"), null),
                        buildDetail(fryingOil, new BigDecimal("0.050"), "For frying")
                ));

        recipeRepository.saveAll(List.of(classicHamburger, hotDog, frenchFriesRecipe));
        log.info("3 demo recipes created: Classic Hamburger, Hot Dog, French Fries");
    }

    private void createDemoProducts(String tenantId) {
        if (productRepository.existsByCodeAndTenantId("PROD-001", tenantId)) {
            log.debug("Demo products already exist");
            return;
        }

        Recipe hamburgerRecipe = recipeRepository.findByCodeAndTenantId("HAM-001", tenantId).orElseThrow();
        Recipe hotDogRecipe = recipeRepository.findByCodeAndTenantId("HOT-001", tenantId).orElseThrow();
        Recipe friesRecipe = recipeRepository.findByCodeAndTenantId("FRI-001", tenantId).orElseThrow();

        Ingredient cheese = ingredientRepository.findByCodeAndTenantId("LEC-001", tenantId).orElseThrow();
        Ingredient lettuce = ingredientRepository.findByCodeAndTenantId("VER-001", tenantId).orElseThrow();

        List<Product> products = List.of(
                // CON_RECETA — 3 products with recipe
                Product.builder()
                        .tenantId(tenantId).code("PROD-001").name("Classic Hamburger")
                        .description("Traditional hamburger with beef patty, cheese, lettuce and tomato")
                        .category(ProductCategory.FOOD).productType(ProductType.CON_RECETA)
                        .sellingPrice(new BigDecimal("12.0000"))
                        .unitCost(hamburgerRecipe.getProductionCost())
                        .recipe(hamburgerRecipe)
                        .active(true).build(),
                Product.builder()
                        .tenantId(tenantId).code("PROD-002").name("Hot Dog")
                        .description("Classic hot dog with beef sausage, lettuce and tomato")
                        .category(ProductCategory.FOOD).productType(ProductType.CON_RECETA)
                        .sellingPrice(new BigDecimal("9.0000"))
                        .unitCost(hotDogRecipe.getProductionCost())
                        .recipe(hotDogRecipe)
                        .active(true).build(),
                Product.builder()
                        .tenantId(tenantId).code("PROD-003").name("French Fries")
                        .description("Crispy golden french fries")
                        .category(ProductCategory.SIDES).productType(ProductType.CON_RECETA)
                        .sellingPrice(new BigDecimal("6.0000"))
                        .unitCost(friesRecipe.getProductionCost())
                        .recipe(friesRecipe)
                        .active(true).build(),

                // SIN_RECETA — 2 products without recipe
                Product.builder()
                        .tenantId(tenantId).code("PROD-004").name("Bottled Cola")
                        .description("350ml bottled cola drink")
                        .category(ProductCategory.DRINKS).productType(ProductType.SIN_RECETA)
                        .sellingPrice(new BigDecimal("3.5000"))
                        .unitCost(new BigDecimal("1.8000"))
                        .active(true).build(),
                Product.builder()
                        .tenantId(tenantId).code("PROD-005").name("Mineral Water")
                        .description("600ml mineral water bottle")
                        .category(ProductCategory.DRINKS).productType(ProductType.SIN_RECETA)
                        .sellingPrice(new BigDecimal("2.5000"))
                        .unitCost(new BigDecimal("1.0000"))
                        .active(true).build(),

                // ADICIONAL — 2 add-on products
                Product.builder()
                        .tenantId(tenantId).code("PROD-006").name("Extra Cheese")
                        .description("Additional cheese slice")
                        .category(ProductCategory.FOOD).productType(ProductType.ADICIONAL)
                        .sellingPrice(new BigDecimal("1.5000"))
                        .unitCost(cheese.getCurrentUnitCost())
                        .active(true).build(),
                Product.builder()
                        .tenantId(tenantId).code("PROD-007").name("Extra Lettuce")
                        .description("Additional fresh lettuce")
                        .category(ProductCategory.FOOD).productType(ProductType.ADICIONAL)
                        .sellingPrice(new BigDecimal("1.0000"))
                        .unitCost(lettuce.getCurrentUnitCost())
                        .active(true).build()
        );

        productRepository.saveAll(products);
        log.info("7 demo products created: 3 with recipe, 2 without recipe, 2 add-ons");
    }

    private Recipe buildRecipe(String tenantId, String code, String name, String description,
                               List<RecipeDetail> details) {
        BigDecimal productionCost = details.stream()
                .map(RecipeDetail::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Recipe recipe = Recipe.builder()
                .tenantId(tenantId)
                .code(code)
                .name(name)
                .description(description)
                .productionCost(productionCost)
                .active(true)
                .details(new ArrayList<>())
                .build();

        for (RecipeDetail detail : details) {
            detail.setRecipe(recipe);
            recipe.getDetails().add(detail);
        }

        return recipe;
    }

    private RecipeDetail buildDetail(Ingredient ingredient, BigDecimal quantity, String notes) {
        BigDecimal totalCost = quantity.multiply(ingredient.getCurrentUnitCost()).setScale(4, RoundingMode.HALF_UP);
        return RecipeDetail.builder()
                .ingredient(ingredient)
                .quantity(quantity)
                .unitOfMeasure(ingredient.getUnitOfMeasure())
                .unitCost(ingredient.getCurrentUnitCost())
                .totalCost(totalCost)
                .notes(notes)
                .build();
    }
}
