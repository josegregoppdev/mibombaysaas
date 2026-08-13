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
│   ├── customer/              # Customer (DNI/phone encrypted)
│   ├── supplier/              # Supplier (DNI/phone encrypted)
│   ├── purchase/              # Purchase, PurchaseDetail
│   └── sale/                  # Sale, SaleDetail, SaleState (enum), PaymentMethod (enum)
├── dto/                       # DTOs with validations
│   ├── company/               # CompanyDTORequest, CompanyDTOResponse
│   ├── user/                  # UserDTORequest, UserDTOResponse
│   ├── ingredient/            # IngredientDTO
│   ├── recipe/                # RecipeDTO, RecipeDetailDTO
│   ├── product/               # ProductDTO
│   ├── combo/                 # ComboDTO, ComboDetailDTO
│   ├── customer/              # CustomerDTO
│   ├── supplier/              # SupplierDTO
│   ├── purchase/              # PurchaseDTO, PurchaseDetailDTO, PurchaseCartSubmissionDTO
│   └── sale/                  # SaleDTO, SaleDetailDTO, CartSubmissionDTO, IngredientOptionDTO, PosRecipeDataDTO
├── mapper/                    # MapStruct Mappers
│   ├── company/               # CompanyMapper
│   ├── user/                  # UserMapper
│   ├── ingredient/            # IngredientMapper
│   ├── recipe/                # RecipeMapper
│   ├── product/               # ProductMapper
│   ├── combo/                 # ComboMapper
│   ├── customer/              # CustomerMapper
│   ├── supplier/              # SupplierMapper
│   └── sale/                  # SaleMapper
├── repository/                # Spring Data JPA repositories
│   ├── user/                  # UserRepository
│   ├── company/               # CompanyRepository
│   ├── ingredient/            # IngredientRepository
│   ├── recipe/                # RecipeRepository
│   ├── product/               # ProductRepository
│   ├── combo/                 # ComboRepository
│   ├── customer/              # CustomerRepository
│   ├── supplier/              # SupplierRepository
│   ├── purchase/              # PurchaseRepository, PurchaseDetailRepository
│   └── sale/                  # SaleRepository
├── service/                   # business logic
│   ├── user/                  # CustomUserDetailsService, PasswordGeneratorService
│   ├── company/               # CompanyService
│   ├── ingredient/            # IngredientService
│   ├── recipe/                # RecipeService
│   ├── product/               # ProductService
│   ├── combo/                 # ComboService
│   ├── inventory/             # InventarioService
│   ├── movement/              # MovementService (read-only)
│   ├── customer/              # CustomerService
│   ├── supplier/              # SupplierService
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
│   ├── movement/              # MovementController (inventory movement report)
│   ├── customer/              # CustomerController
│   ├── supplier/              # SupplierController
│   ├── purchase/              # PurchaseController (registers purchases, cancellation)
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
| `CompanyServiceTest` | 9 |
| `PasswordGeneratorServiceTest` | 8 |
| `CustomUserDetailsServiceTest` | 7 |
| `UserServiceTest` | 4 |
| `IngredientServiceTest` | 15 |
| `RecipeServiceTest` | 14 |
| `ProductServiceTest` | 21 |
| `ComboServiceTest` | 15 |
| `InventarioServiceTest` | 16 |
| `MovementServiceTest` | 10 |
| `CustomerServiceTest` | 6 |
| `SupplierServiceTest` | 13 |
| `PurchaseServiceTest` | 16 |
| `LandingControllerTest` | 1 |
| `CompanyRegistrationControllerTest` | 4 |
| `IngredientControllerTest` | 13 |
| `RecipeControllerTest` | 13 |
| `ProductControllerTest` | 13 |
| `ComboControllerTest` | 13 |
| `MovementControllerTest` | 3 |
| `SupplierControllerTest` | 13 |
| `PurchaseControllerTest` | 10 |
| `MibombayApplicationTests` | 1 |
| **Total** | **238** |

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

Layout with left sidebar (`col-md-3 col-lg-2`) + main content (`col-md-9 col-lg-10`). On mobile (<768px) the sidebar is hidden and opens with Offcanvas via hamburger button in the navbar. Sidebar prepared for future sections (tables, menu, orders, reports). The body uses flexbox so the sidebar background reaches the viewport bottom.

### POS design system (`sd-` prefix)

The sale module uses a `sd-` prefixed design system defined inline in `templates/sale/*.html` (kept with each template for locality):
- `sd-pos-page`, `sd-pos-layout`, `sd-pos-products`, `sd-pos-grid`, `sd-pos-item`, `sd-pos-cart` — POS layout primitives
- `sd-pos-cart-row`, `sd-pos-cart-row-controls`, `sd-pos-cart-row-excluded` — cart row structure (with the exclusion badge when an item has excluded ingredients)
- `sd-pos-pago-opt`, `sd-pos-btn-pagar`, `sd-pos-btn-hold` — payment/charge/hold buttons
- `sd-pos-fab`, `sd-pos-cart-backdrop` — mobile floating cart button + dimmed backdrop
- `sd-recipe-modal`, `sd-recipe-tree`, `sd-recipe-tree-root`, `sd-recipe-tree-list`, `sd-recipe-tree-product`, `sd-recipe-tree-leaves`, `sd-recipe-tree-leaf` — the "Edit recipe" modal and its tree (combo → products → ingredients) view
- `sd-page-title`, `sd-list-card`, `sd-info-grid`, `sd-empty-state`, `sd-badge` — shared shell for `history.html`, `on-hold.html`, `detail.html`

The dark blue (`--mb-blue-900` / `#0B2545`) is the dominant tone for the cart, sidebar, and modal — it ties the POS together with the navigation.

### Mobile POS (cart drawer)

On phone (≤767.98px) the cart is hidden off-screen and the products panel takes the full screen. A **floating action button (FAB)** pinned bottom-right (cart icon + amber count badge + total) opens the cart as a **right-side drawer** (`position: fixed; right: 0; width: 88%; max-width: 360px; transform: translateX(100%) → 0` with `0.28s ease`). A semi-transparent backdrop dims the products. Close via the X button in the cart header (hidden on desktop) or by tapping the backdrop. Desktop is unchanged.

### Recipe exclusions in the cart

Each cart item gets an "Edit recipe" button when it has a recipe (products with a recipe, or combos that contain at least one product). Tapping it opens a Bootstrap modal with the recipe's ingredients as checkboxes (all checked = include; uncheck to exclude). For combos, a **tree** is rendered: the combo name as root, each product as a branch (real name, `├─`/`└─` connectors), and each ingredient as a leaf. Products in the combo without a recipe appear as a branch with a "(no customizations)" leaf so the cashier can explain the contents to the customer. Exclusions are saved per-sale in the existing `SaleDetailDTO.notes` field as `"Without: onion, tomato"`. The global `Recipe` entity is never modified. The cart row shows a small "Without: …" badge when exclusions exist.

## Available Skills

The project has specialized skills in `.agents/skills/` that guide development across different areas:

| Skill | Location | Purpose |
|---|---|---|
| **frontend-design** | `.agents/skills/frontend-design/` | Distinctive, intentional visual design. Palette, typography, layout, and signature elements that avoid templated defaults. Use when designing new UI or reshaping existing pages. |
| **thymeleaf-restaurant** | `.agents/skills/thymeleaf-restaurant/` | Restaurant SaaS UI patterns with Spring Boot + Thymeleaf. Covers mobile-first responsive strategy, HTMX for interactivity, Alpine.js for UI state, and restaurant-specific patterns (POS, table maps, order status). |
| **alpinejs** | `.agents/skills/alpinejs/` | Alpine.js best practices and patterns. Use when writing HTML with Alpine directives. Key rule: keep attributes short — extract complex logic to functions. |
| **htmx** | `.agents/skills/htmx/` | HTMX development guidelines for dynamic web apps with minimal JavaScript. Covers request attributes, DOM manipulation, URL management, and integration patterns. |

### When to load each skill

- **frontend-design**: building or redesigning any page (landing, forms, dashboards)
- **thymeleaf-restaurant**: implementing restaurant-specific UI (POS, table management, kitchen display)
- **alpinejs**: when adding client-side interactivity (modals, dropdowns, toggles, form validation)
- **htmx**: when adding partial page updates without full reload (cart operations, live search, polling)

The `thymeleaf-restaurant` skill is the primary guide — it references HTMX and Alpine.js as optional tools for interactivity. The current project uses Bootstrap 5.3 (not Tailwind) as confirmed in AGENTS.md tech stack.

## Conventions

- Use Lombok annotations on entities/DTOs to reduce boilerplate
- **DTOs**: `CompanyDTORequest`/`CompanyDTOResponse` for company, `UserDTORequest`/`UserDTOResponse` for user. The rest use simple `@RequestParam` without DTO
- **Spring MVC + DTOs and thin controllers**: This project uses Spring MVC. The form (Thymeleaf view) ALWAYS works with DTOs, never with JPA entities. Services return DTOs to the controller. The controller calls services, never repositories. **Business logic (validation rules, entity mutations, persistence operations) lives in the service layer**, not in the controller. The controller only does presentation: form binding, input validation that compares form fields, calling the service, handling service exceptions, and returning the view.
- **MapStruct**: `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)` in interfaces inside `mapper/`
- **Entities**: no Javax/Jakarta validations, JPA annotations only
- **Validations**: in DTOs, not in entities
- **Tests**: use `TestDataFactory` to build test objects
- Thymeleaf templates go in `src/main/resources/templates/`
- Static assets go in `src/main/resources/static/`
- Respond and communicate in English with the user

## Next Module

**Tables** module (tables, rooms, availability management). See `plan-mesas.md` when created. Purchases module (Compras) is complete.

## Additional Conventions

- **Service method names**: use long descriptive names (`getPaginatedIngredients`, `createNewIngredient`, `toggleIngredientActiveStatus`)
- **Controller helper method**: `private String tenantId() { return TenantContext.get(); }` to avoid direct calls to `TenantContext.get()`
- **Numeric validation**: `@Digits(integer = 8, fraction = 4)` on BigDecimal fields + `max="99999999.9999"` on HTML5 inputs
- **Category as enum**: fixed with 9 values (MEATS, DAIRY, BREADS, VEGETABLES, FRUITS, GRAINS, CONDIMENTS, BEVERAGES, OTHERS) with `displayName`
- **Responsive sidebar**: Offcanvas on mobile (<768px), inline on desktop (≥768px). Hamburger button in authenticated navbar
- **State toggle**: single method `toggleIngredientActiveStatus` that flips `active`. Button always visible in list (red if active, green if inactive)
- **Product types**: `ProductType` enum (CON_RECETA, SIN_RECETA, ADICIONAL) — single Product entity differentiated by type
- **Product-Recipe relationship**: CON_RECETA requires recipe, SIN_RECETA has none, ADICIONAL requires recipe (mandatory for inventory control)
- **Product stock**: `Product` has `currentStock` and `minimumStock` (BigDecimal) for SIN_RECETA products
- **Product categories**: `ProductCategory` enum (FOOD, DRINKS, DESSERTS, SIDES, COMBOS, OTHERS)
- **Combo**: groups products sold together. `ComboDetail` links to Product with quantity. Total cost = sum of product costs × quantities
- **Adiciones en POS**: el cajero hace click en `+` de la fila del carrito (solo productos con receta y combos). Se abre un modal con search + grid de TODOS los productos activos (cualquier `ProductType`). Click en un producto → se agrega como add-on indentado bajo el padre. Los add-ons se guardan como `SaleDetail` separados. `parentIdx` es UI-only, no se persiste en DB.
- **Sale states**: `SaleState` enum (EN_ESPERA, CONFIRMADA, ANULADA). Confirmed sales are immutable
- **Payment methods**: `PaymentMethod` enum (EFECTIVO, DATAFONO, TRANSFERENCIA)
- **POS**: embedded in the dashboard layout (navbar + sidebar visible) at `/sale/pos`. **JS-managed cart** (client-side array) — products and quantities are held in a JavaScript array and submitted as a single hidden form on charge/hold. Products frozen at sale time (historical snapshot): on submit, `SaleService.createSaleFromCart` re-reads `sellingPrice`/`unitCost` from the DB and snapshots them into `SaleDetail`. On-hold sales can be resumed by storing the prefilled cart in the HTTP session and reading it back on `/sale/pos`.
- **Recipe exclusions in POS**: per-sale only. Saved in `SaleDetailDTO.notes` as `"Without: ingredient1, ingredient2"`. The global `Recipe`/`RecipeDetail` entities are never modified by the POS.
- **Recipe-Product cost sync**: when a recipe is updated, all linked products automatically get their `unitCost` updated via `ProductService.syncProductCostsForRecipe()`
- **Inventory**: `InventarioService` is the only service allowed to modify stock. Every stock change creates a `Movement` (types `SALE`, `PURCHASE`, `RETURN`) in the same transaction. Consumed by `SaleService.confirmSale()` and `PurchaseService.createPurchaseFromCart()`.
- **Purchases**: registers ingredient/SIN_RECETA purchases from suppliers into stock. `PurchaseService.createPurchaseFromCart` validates items, snapshots supplier name + unit prices, then `InventarioService.addStockForPurchase` increments stock and updates the previous unit cost (snapshot in `PurchaseDetail.previousUnitCost`); total = Σ quantity × unitPrice. Each purchase requires an `invoiceNumber` (supplier invoice, unique per purchase is not enforced but recommended). Cancellation (`cancelPurchase`) only within **24 h** of `purchaseDate` (marked inactive, never deleted) and calls `InventarioService.revertPurchase` which restores previous stock/costs and logs `RETURN` movements.
- **Sale inventory consumption**: Product `CON_RECETA`/`ADICIONAL` → decrements recipe ingredients. Product `SIN_RECETA` → decrements product stock. Combo → expands into products and applies same rules. Exclusions in `"Without: ..."` are not consumed.
- **Negative inventory**: controlled by `TenantConfiguration.allowNegativeInventory`. If false, sale fails when stock insufficient. If true, stock can go negative.
- **Customer**: `Customer` entity with AES-256-GCM encryption for DNI (`documentEncrypted`) and phone (`phoneEncrypted`). Lookup via HMAC-SHA256 hashes (`documentLookupHash`, `phoneLookupHash`). Default customer `Consumidor Final` (DNI: 99999999) auto-created per tenant. Cannot be deactivated.
- **Supplier**: `Supplier` entity with AES-256-GCM encryption for document (`documentEncrypted`) and phone (`phoneEncrypted`), same encryption + lookup-hash scheme as Customer. Default supplier `Proveedor Principal` (NIT: 900001111) auto-created per tenant. Cannot be deactivated; only name/address editable. List shows masked values (`documentMasked`, `phoneMasked`); edit form pre-fills decrypted values for re-encryption on save.
