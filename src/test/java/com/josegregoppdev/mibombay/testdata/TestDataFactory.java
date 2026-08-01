package com.josegregoppdev.mibombay.testdata;

import com.josegregoppdev.mibombay.dto.company.CompanyDTORequest;
import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.dto.ingredient.IngredientDTO;
import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
