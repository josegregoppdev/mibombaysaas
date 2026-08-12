<p align="center">
  <span style="font-size: 2.5rem; font-weight: 700;">Mi<span style="font-style: italic; color: #F4A261;">Bombay</span></span>
</p>

<p align="center">
  <strong>SaaS multi-tenant restaurant management system</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot 4.1.0">
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=flat-square&logo=bootstrap&logoColor=white" alt="Bootstrap 5.3">
  <img src="https://img.shields.io/badge/Tests-181-brightgreen?style=flat-square" alt="181 Tests">
  <img src="https://img.shields.io/badge/License-Proprietary-blue?style=flat-square" alt="License">
</p>

---

## Description

**MiBombay** is a multi-tenant SaaS platform for restaurant management. Restaurant owners create their company, and the system automatically creates an admin and a cashier. Everything works with `tenantId` isolation.

The first version prioritizes the **POS (Point of Sale)** flow: the customer arrives, places an order, pays, and receives the order. The architecture allows adding tables, waiters, reservations, kitchen, deliveries, and other modules later without breaking the current flow.

---

## Features

- **Multi-tenant** — Data isolation per restaurant with `tenantId`
- **Self-registration** — Owners create their company and start using immediately
- **Roles & permissions** — `SUPER_ADMIN`, `ADMIN`, `CASHIER` with Spring Security
- **OWASP Top 10 Security** — BCrypt (strength 12), `@Valid` + `@Pattern`, Thymeleaf XSS, secure sessions, CSRF
- **POS Interface** — JS-managed cart in the dashboard layout with product catalog, search, cart, recipe exclusions, hold/confirm flow
- **Product Management** — CON_RECETA (with recipe), SIN_RECETA (without recipe, with stock), ADICIONAL (add-ons, requires recipe)
- **Recipe Management** — Ingredients + quantities, auto-calculated production cost
- **Recipe exclusions in POS** — Per-sale ingredient unchecks saved in `notes`; global recipes are never modified
- **Add-ons in POS** — `+` button on cart rows opens a search + grid of all products; add-ons are separate lines indented under the parent
- **Combo System** — Group products sold together at a single price; combo editor shows a tree of products and ingredients
- **Mobile-first POS** — Right-side cart drawer with floating action button on phones; full-screen products grid
- **Responsive UI** — Bootstrap 5.3, flexbox dashboard layout, offcanvas sidebar on mobile
- **Inventory on Sale** — `InventarioService` consumes stock and records immutable `Movement` records on every confirmed sale
- **Customer Management** — CRUD with AES-256-GCM encryption for DNI and phone. Default customer `Consumidor Final` auto-created per tenant
- **Supplier Management** — CRUD with AES-256-GCM encryption for document and phone. Masked values in the list; edit form pre-fills decrypted values. Default supplier `Proveedor Principal` auto-created per tenant (protected, only name/address editable)
- **Movement Report** — Read-only inventory movement history at `/movement` with type and date-range filters (ADMIN only)
- **207 unit tests** — JUnit 5 + Mockito with TestDataFactory

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 | Core language |
| **Spring Boot** | 4.1.0 | Main framework |
| **Spring Security** | - | Authentication & authorization |
| **Spring Data JPA** | - | Data persistence |
| **Thymeleaf** | - | Server-side template engine |
| **MySQL** | 8.0+ | Database |
| **Bootstrap** | 5.3 | CSS framework (via CDN) |
| **Bootstrap Icons** | 1.11+ | Icons (via CDN) |
| **Lombok** | - | Boilerplate reduction |
| **MapStruct** | 1.6.3 | DTO ↔ Entity mapping |
| **Maven** | - | Dependency management |

---

## Installation

### Prerequisites

- Java 21+
- MySQL 8.0+
- Maven (or use the included wrapper `./mvnw`)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/tu-usuario/mibombay.git
   cd mibombay
   ```

2. **Configure the database**

   Copy the configuration template:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application-dev.properties
   ```

   Edit `application-dev.properties` with your MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/mibombay?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   ```

3. **Compile the project**
   ```bash
   ./mvnw clean compile
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Open in browser**
   
   Navigate to [http://localhost:8080](http://localhost:8080)

---

## Demo Data

The system auto-creates a demo restaurant on startup:

| Role | Email | Password |
|---|---|---|
| Admin | `demo@mibombay.com` | `demo1234` |
| Cashier | `demo_cashier@mibombay.com` | *(temporary, shown once)* |

Subdomain: `1` (access via `1.lvh.me:8080`)

**Demo data includes:**
- 10 ingredients
- 3 recipes (Classic Hamburger, Hot Dog, French Fries)
- 7 products (3 with recipe, 2 without, 2 add-ons)
- 2 combos (Hamburger + Fries + Cola, Hot Dog + Fries + Water)

---

## Modules

### Ingredients
- CRUD with code, name, category, unit of measure, cost, stock
- Active/inactive toggle
- 9 categories (Meats, Dairy, Breads, Vegetables, Fruits, Grains, Condiments, Beverages, Others)

### Recipes
- Recipe with ingredients and quantities
- Auto-calculated production cost
- Ingredient selection with auto-fill cost and unit

### Products
- **CON_RECETA** — Links to a recipe, cost from recipe's production cost
- **SIN_RECETA** — Standalone item with purchase cost (e.g., bottled drinks)
- **ADICIONAL** — Add-on product, may have optional recipe
- **Product categories**: Food, Drinks, Desserts, Sides, Combos, Others
- Product-Recipe cost sync: updating a recipe auto-updates linked products

### Combos
- Groups products sold together at a single price
- Product selection with quantities
- Auto-calculated total cost and margin display

### POS (Point of Sale)
- Embedded in the dashboard layout (navbar + sidebar visible) at `/sale/pos`
- JS-managed cart: add products/combos, change quantity, remove items, clear cart
- **Edit recipe** button per cart item: uncheck ingredients the customer does not want (per-sale, saved in `notes`; global recipe never changes). Combos show a tree (combo → products → ingredients) with real product names; products without a recipe appear with "(no customizations)"
- **Hold Sale** — Save cart as on-hold, resume later
- **Confirm Sale** — Select payment method (Cash, Card, Transfer) and charge
- On-hold sales list (cashier sees own, admin sees all)
- Sales history with detail view
- **Mobile**: cart is a right-side drawer opened by a floating action button; products grid takes the full screen

---

## Project Structure

```
mibombay/
├── src/main/java/com/josegregoppdev/mibombay/
│   ├── common/           # audit/, tenant/, exception/
│   ├── model/            # JPA entities
│   │   ├── ingredient/   # Ingredient, Category, UnitOfMeasure
│   │   ├── recipe/       # Recipe, RecipeDetail
│   │   ├── product/      # Product, ProductType, ProductCategory
│   │   ├── combo/        # Combo, ComboDetail
│   │   ├── customer/     # Customer (DNI/phone encrypted AES-256-GCM)
│   │   ├── supplier/     # Supplier (document/phone encrypted AES-256-GCM)
│   │   └── sale/         # Sale, SaleDetail, SaleState, PaymentMethod
│   ├── dto/              # DTOs with Bean Validation
│   ├── mapper/           # MapStruct interfaces
│   ├── repository/       # Spring Data JPA
│   ├── service/          # Business logic
│   ├── controller/       # Web controllers
│   └── config/           # Security, initial data
├── src/main/resources/
│   ├── templates/        # Thymeleaf templates
│   │   ├── ingredient/   # list.html, form.html
│   │   ├── recipe/       # list.html, form.html
│   │   ├── product/      # list.html, form.html
│   │   ├── combo/        # list.html, form.html
│   │   ├── customer/     # list.html, form.html
│   │   ├── supplier/     # list.html, form.html
│   │   └── sale/         # pos.html, on-hold.html, history.html, detail.html
│   └── static/           # CSS, JS
└── src/test/             # 207 unit tests
```

---

## Multi-Tenant Architecture

- Each restaurant has a unique `tenantId` (format: `tnt_` + UUID)
- `TenantContext` (ThreadLocal) holds the current request tenant
- `TenantFilter` extracts the subdomain from the `Host` header
- All queries filter by `tenantId` for data isolation
- Session cookie works across subdomains (`Domain=lvh.me` for dev)

---

## Security (OWASP Top 10)

| Control | Implementation |
|---|---|
| **A01** Broken Access Control | CSRF, session invalidation, max 1 session, DTO mass assignment prevention |
| **A02** Cryptographic Failures | BCrypt strength 12, SecureRandom for temp passwords |
| **A03** Injection | Thymeleaf XSS escape, JPA prepared statements, `@Pattern` validation |
| **A05** Security Misconfiguration | 10-min session timeout, session fixation mitigation, security headers |
| **A07** Auth Failures | BCrypt passwords, unique email, role-based access, forced password change |

---

## Commands

```bash
# Run dev server
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ProductServiceTest

# Build JAR
./mvnw clean package
```

---

## Roadmap

- [x] SaaS multi-tenant with subdomain resolution
- [x] Authentication & authorization (3 roles)
- [x] OWASP Top 10 security
- [x] Ingredient management
- [x] Recipe management with ingredients
- [x] Product management (3 types)
- [x] Combo system
- [x] POS interface (hold/confirm flow)
- [x] Sales history
- [x] Mobile-first responsive design
- [ ] Tables management
- [x] Adiciones (add-on products via `+` in POS cart rows)
- [ ] Inventory management
- [ ] Supplier & purchase management
- [ ] Cash register & closing
- [ ] PDF receipt generation
- [ ] X and Z reports

---

## License

Proprietary — All rights reserved.

---

## Contact

**Developer**: Jose Gregorio  
**Project**: MiBombay — Restaurant Management System

---

<p align="center">
  <strong>Mi<span style="font-style: italic; color: #F4A261;">Bombay</span></strong> — Seamless management for your restaurant
</p>
