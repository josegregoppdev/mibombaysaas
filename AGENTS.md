# AGENTS.md

## Domain

Restaurant management system (MiBombay). Multi-tenant SaaS.

## Build & Run

```bash
./mvnw spring-boot:run          # dev server (needs MySQL configured)
./mvnw test                     # run all tests
./mvnw test -Dtest=ClaseTest    # run one test class
./mvnw clean package            # build jar
```

There are no separate lint/typecheck commands — compilation (`./mvnw compile`) detects type errors.

## Tech Stack

- **Java 21**, **Spring Boot 4.1.0**, Maven
- Spring MVC + Thymeleaf (server-rendered HTML, not REST API by default)
- Spring Security, Spring Data JPA, Bean Validation
- MySQL (driver included, config in `application-dev.properties`)
- Lombok (annotation processor in `pom.xml` — use `@Data`, `@Builder`, etc.)
- **MapStruct 1.6.3** (DTO ↔ Entity mapping, processor in `pom.xml`)

## Project Structure

Layer-based organization with subfolders by domain:

```
com.josegregoppdev.mibombay
├── MibombayApplication.java
├── common/                    # shared utilities
│   ├── audit/                 # AuditableEntity (auto timestamps)
│   ├── tenant/                # TenantContext, TenantFilter
│   └── exception/             # GlobalExceptionHandler
├── model/                     # JPA entities (no validations, JPA only)
│   ├── user/                  # User, Role
│   ├── company/               # Company
│   ├── ingredient/            # Category (enum), Ingredient, UnitOfMeasure (enum)
│   ├── recipe/                # Recipe, RecipeDetail
│   ├── product/               # Product, ProductType (enum), ProductCategory (enum)
│   ├── combo/                 # Combo, ComboDetail
│   └── sale/                  # Sale, SaleDetail, SaleState (enum), PaymentMethod (enum)
├── dto/                       # DTOs with validations
│   ├── company/               # CompanyDTORequest, CompanyDTOResponse
│   ├── user/                  # UserDTORequest, UserDTOResponse
│   ├── ingredient/            # IngredientDTO
│   ├── recipe/                # RecipeDTO, RecipeDetailDTO
│   ├── product/               # ProductDTO
│   ├── combo/                 # ComboDTO, ComboDetailDTO
│   └── sale/                  # SaleDTO, SaleDetailDTO
├── mapper/                    # MapStruct Mappers
│   ├── company/               # CompanyMapper
│   ├── user/                  # UserMapper
│   ├── ingredient/            # IngredientMapper
│   ├── recipe/                # RecipeMapper
│   ├── product/               # ProductMapper
│   ├── combo/                 # ComboMapper
│   └── sale/                  # SaleMapper
├── repository/                # Spring Data JPA repositories
│   ├── user/                  # UserRepository
│   ├── company/               # CompanyRepository
│   ├── ingredient/            # IngredientRepository
│   ├── recipe/                # RecipeRepository
│   ├── product/               # ProductRepository
│   ├── combo/                 # ComboRepository
│   └── sale/                  # SaleRepository
├── service/                   # business logic
│   ├── user/                  # CustomUserDetailsService, PasswordGeneratorService
│   ├── company/               # CompanyRegistrationService
│   ├── ingredient/            # IngredientService
│   ├── recipe/                # RecipeService
│   ├── product/               # ProductService
│   ├── combo/                 # ComboService
│   └── sale/                  # SaleService
├── controller/                # web controllers
│   ├── landing/               # LandingController
│   ├── auth/                  # LoginController, PasswordChangeController
│   ├── company/               # CompanyRegistrationController
│   ├── dashboard/             # DashboardController
│   ├── admin/                 # AdminController
│   ├── ingredient/            # IngredientController
│   ├── recipe/                # RecipeController
│   ├── product/               # ProductController
│   ├── combo/                 # ComboController
│   └── sale/                  # SaleController (POS, on-hold, history)
└── config/                    # Spring configurations
    ├── SecurityConfig.java
    ├── PasswordEncoderConfig.java
    ├── InitialDataConfig.java
    └── LoginSuccessHandler.java
```

## Multi-Tenant Architecture

- Each company has a unique `tenantId` (format: `tnt_` + UUID)
- `subdomain` field in `Company` identifies the restaurant (e.g., `1`, `mirestaurante`)
- `TenantContext` (ThreadLocal) holds the current request tenant
- `TenantFilter` extracts the subdomain from the `Host` header (works with lvh.me for dev and mibombay.com for prod), finds `Company` by subdomain and sets the `tenantId` in `TenantContext`. No subdomain or `admin`/`www` subdomain means no tenant is set
- `CustomUserDetailsService` validates that the user tenant matches the subdomain tenant
- `TenantFilter` clears the context at the end of each request
- Isolation by `tenantId` in each domain entity
- Session cookie with `Domain=lvh.me` (dev) / `Domain=mibombay.com` (prod) to work across subdomains. Configured in `application-dev.properties` and `application-prod.properties`
- `LoginSuccessHandler`: on login, validates the form subdomain, finds the company and redirects to `{subdomain}.{domain}/dashboard`
- Logout deletes the cookie with and without Domain to ensure browser cleanup

## Security (OWASP Top 10)

### A01: Broken Access Control
- CSRF enabled by default (Spring Security)
- Logout invalidates session and deletes JSESSIONID
- Max 1 session per user
- DTOs prevent mass assignment (no sensitive fields exposed in binding)

### A02: Cryptographic Failures
- `BCryptPasswordEncoder` with strength 12 for passwords
- Manager document hashed with BCrypt (not reversible)
- Temporary passwords generated with `SecureRandom`

### A03: Injection
- Thymeleaf escapes HTML automatically (XSS)
- JPA/Spring Data uses prepared statements (SQL injection)
- `@Valid` in DTOs to validate input data
- `@Pattern` on text fields to block malicious characters (`< > " &`)

### A05: Security Misconfiguration
- Session timeout: 10 minutes of inactivity
- Session fixation: `.sessionFixation().migrateSession()`
- Expired session redirects to `/login?expired=true` with message
- HTTP headers: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`
- Config profiles: dev/prod separate
- `.gitignore` ignores properties with secrets

### A07: Identification & Authentication Failures
- BCrypt for passwords
- Unique global email
- Roles: `SUPER_ADMIN`, `ADMIN`, `CASHIER` (enum `Role`)
- When creating a company: admin is created (with form password) and cashier (generated temporary password)
- `mustChangePassword` field forces password change on cashier first login
- Default SuperAdmin: `SuperAdministrador@gmail.com` / `Mora.Kristoff_26123009` (auto-created on startup)
- SuperAdmin accesses `/admin/dashboard` with company list and active/inactive status
- Demo restaurant auto-created with subdomain `1` — admin: `demo@mibombay.com` / `demo1234`, cashier: `demo_cashier@mibombay.com` (temporary password)

## Properties

- `application.properties` — base, loads active profile + session timeout
- `application-dev.properties` — local config (local MySQL, logs)
- `application-prod.properties` — production config (environment variables)
- `application.properties.example` — template for GitHub (uploaded)

Real properties files are in `.gitignore`.

## Tests

- JUnit 5 + Mockito (`spring-boot-starter-test` included)
- Test Data Factory in `testdata/TestDataFactory.java` (provides pre-built objects)
- Services with logic: unit tests with `@ExtendWith(MockitoExtension.class)`
- Services without dependencies: direct tests (no mocks)

### Current tests

| Class | Tests |
|---|---|
| `CompanyRegistrationServiceTest` | 7 |
| `PasswordGeneratorServiceTest` | 8 |
| `CustomUserDetailsServiceTest` | 7 |
| `IngredientServiceTest` | 15 |
| `RecipeServiceTest` | 14 |
| `ProductServiceTest` | 16 |
| `ComboServiceTest` | 14 |
| `LandingControllerTest` | 1 |
| `LoginControllerTest` | 6 |
| `PasswordChangeControllerTest` | 4 |
| `DashboardControllerTest` | 4 |
| `CompanyRegistrationControllerTest` | 4 |
| `AdminControllerTest` | 2 |
| `IngredientControllerTest` | 13 |
| `RecipeControllerTest` | 13 |
| `ProductControllerTest` | 13 |
| `ComboControllerTest` | 12 |
| `MibombayApplicationTests` | 1 |
| Total | 139 |

```bash
./mvnw test
./mvnw test -Dtest=IngredientServiceTest
./mvnw test -Dtest=IngredientControllerTest
```

## Frontend

- **Bootstrap 5.3** via CDN + **Bootstrap Icons** via CDN
- Thymeleaf templates in `src/main/resources/templates/`
- Custom CSS in `src/main/resources/static/css/styles.css` (tokens only + Bootstrap overrides)
- JS in `src/main/resources/static/js/app.js` (minimal interactions: auto-dismiss alerts, copy to clipboard)

### Color palette (CSS variables)

- `--mb-blue-900: #0B2545` — navbar, main text
- `--mb-blue-700: #13315C` — hover, borders
- `--mb-blue-500: #1E5A96` — primary color
- `--mb-amber-500: #F4A261` — warm accent (CTAs, "Bombay" brand)
- `--mb-success: #2A9D8F` — success messages
- `--mb-error: #E63946` — errors

### Typography

- **Display**: Georgia/serif for "Bombay" and headings
- **Body**: system-ui sans-serif
- **Mono**: SF Mono/Monaco for credentials

### Brand signature

"MiBombay" with dual treatment: "Mi" in white sans-serif, "Bombay" in italic amber serif.

### Thymeleaf Fragments

- `fragments/layout.html` — base structure with CDNs
- `fragments/navbar.html` — navbar with conditional authentication (Spring Security)
- `fragments/footer.html` — footer
- `fragments/alerts.html` — flash messages
- `fragments/sidebar.html` — dashboard sidebar (Offcanvas on mobile, inline on desktop)
- `fragments/sidebar-admin.html` — admin panel sidebar (Offcanvas on mobile, inline on desktop)

### Dashboard layout

Layout with left sidebar (`col-md-3 col-lg-2`) + main content (`col-md-9 col-lg-10`). On mobile (<768px) the sidebar is hidden and opens with Offcanvas via hamburger button in the navbar. Sidebar prepared for future sections (tables, menu, orders, reports).

## Conventions

- Use Lombok annotations on entities/DTOs to reduce boilerplate
- **DTOs**: `CompanyDTORequest`/`CompanyDTOResponse` for company, `UserDTORequest`/`UserDTOResponse` for user. The rest use simple `@RequestParam` without DTO
- **MapStruct**: `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)` in interfaces inside `mapper/`
- **Entities**: no Javax/Jakarta validations, JPA annotations only
- **Validations**: in DTOs, not in entities
- **Tests**: use `TestDataFactory` to build test objects
- Thymeleaf templates go in `src/main/resources/templates/`
- Static assets go in `src/main/resources/static/`
- Respond and communicate in English with the user

## Next Module

**Tables** module (tables, rooms, availability management). See `plan-mesas.md` when created.

## Additional Conventions

- **Service method names**: use long descriptive names (`getPaginatedIngredients`, `createNewIngredient`, `toggleIngredientActiveStatus`)
- **Controller helper method**: `private String tenantId() { return TenantContext.get(); }` to avoid direct calls to `TenantContext.get()`
- **Numeric validation**: `@Digits(integer = 8, fraction = 4)` on BigDecimal fields + `max="99999999.9999"` on HTML5 inputs
- **Category as enum**: fixed with 9 values (MEATS, DAIRY, BREADS, VEGETABLES, FRUITS, GRAINS, CONDIMENTS, BEVERAGES, OTHERS) with `displayName`
- **Responsive sidebar**: Offcanvas on mobile (<768px), inline on desktop (≥768px). Hamburger button in authenticated navbar
- **State toggle**: single method `toggleIngredientActiveStatus` that flips `active`. Button always visible in list (red if active, green if inactive)
- **Product types**: `ProductType` enum (CON_RECETA, SIN_RECETA, ADICIONAL) — single Product entity differentiated by type
- **Product-Recipe relationship**: optional `@ManyToOne` — CON_RECETA requires recipe, SIN_RECETA has none, ADICIONAL is optional
- **Product categories**: `ProductCategory` enum (FOOD, DRINKS, DESSERTS, SIDES, COMBOS, OTHERS)
- **Combo**: groups products sold together. `ComboDetail` links to Product with quantity. Total cost = sum of product costs × quantities
- **Sale states**: `SaleState` enum (EN_ESPERA, CONFIRMADA, ANULADA). Confirmed sales are immutable
- **Payment methods**: `PaymentMethod` enum (EFECTIVO, DATAFONO, TRANSFERENCIA)
- **POS**: full-screen interface at `/sale/pos`. Cart stored in HTTP session. Products frozen at sale time (historical snapshot)
- **Recipe-Product cost sync**: when a recipe is updated, all linked products automatically get their `unitCost` updated via `ProductService.syncProductCostsForRecipe()`
