package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.user.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final IngredientRepository ingredientRepository;
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
}
