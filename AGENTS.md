# AGENTS.md

## Dominio

Sistema de gestión para restaurantes (MiBombay). SaaS multi-tenant.

## Build & Run

```bash
./mvnw spring-boot:run          # servidor dev (requiere MySQL configurado)
./mvnw test                     # ejecutar todos los tests
./mvnw test -Dtest=ClaseTest    # ejecutar una sola clase de test
./mvnw clean package            # construir jar
```

No hay comandos separados de lint/typecheck — la compilación (`./mvnw compile`) detecta errores de tipo.

## Tech Stack

- **Java 21**, **Spring Boot 4.1.0**, Maven
- Spring MVC + Thymeleaf (HTML renderizado en servidor, no REST API por defecto)
- Spring Security, Spring Data JPA, Bean Validation
- MySQL (driver incluido, requiere configuración en `application-dev.properties`)
- Lombok (procesador de anotaciones configurado en `pom.xml` — usar `@Data`, `@Builder`, etc.)

## Estructura del Proyecto

Organización por capas con subcarpetas por dominio:

```
com.josegregoppdev.mibombay
├── MibombayApplication.java
├── common/                    # utilidades transversales
│   ├── audit/                 # AuditableEntity (timestamps automáticos)
│   ├── tenant/                # TenantContext, TenantFilter
│   └── exception/             # GlobalExceptionHandler
├── model/                     # entidades JPA
│   ├── usuario/               # Usuario, Rol
│   └── empresa/               # Empresa
├── repository/                # repositorios Spring Data JPA
│   ├── usuario/               # UsuarioRepository
│   └── empresa/               # EmpresaRepository
├── service/                   # lógica de negocio
│   ├── usuario/               # CustomUserDetailsService, PasswordGeneratorService
│   └── empresa/               # RegistroEmpresaService
├── controller/                # controladores web
│   ├── landing/               # LandingController
│   ├── auth/                  # LoginController, PasswordChangeController
│   ├── empresa/               # RegistroEmpresaController
│   └── dashboard/             # DashboardController
└── config/                    # configuraciones Spring
    ├── SecurityConfig.java
    └── PasswordEncoderConfig.java
```

## Arquitectura Multi-Tenant

- Cada empresa tiene un `tenantId` único (formato: `tnt_` + UUID)
- Campo `subdominio` en `Empresa` preparado para futuros subdominios
- `TenantContext` (ThreadLocal) mantiene el tenant actual del request
- `TenantFilter` limpia el contexto al final de cada request
- Aislamiento por `tenantId` en cada entidad del dominio

## Seguridad

- `BCryptPasswordEncoder` con strength 12
- Roles: `ADMIN`, `CAJERO` (enum `Rol`)
- Al crear empresa: se crea admin (con password del form) y cajero (password temporal generada)
- Campo `debeCambiarPassword` fuerza cambio de contraseña al primer login del cajero
- Email único global (no se pide subdominio en login)
- Documento del encargado hasheado con BCrypt

## Properties

- `application.properties` — base, carga perfil activo
- `application-dev.properties` — configuración local (MySQL local, logs)
- `application-prod.properties` — configuración producción (variables de entorno)
- `application.properties.example` — plantilla para GitHub (se sube)

Los archivos de properties reales están en `.gitignore`.

## Frontend

- **Bootstrap 5.3** via CDN + **Bootstrap Icons** via CDN
- Plantillas Thymeleaf en `src/main/resources/templates/`
- CSS custom en `src/main/resources/static/css/styles.css` (solo tokens + overrides de Bootstrap)
- JS en `src/main/resources/static/js/app.js` (interacciones mínimas: auto-dismiss alerts, copiar al portapapeles)

### Paleta de colores (CSS variables)

- `--mb-blue-900: #0B2545` — navbar, texto principal
- `--mb-blue-700: #13315C` — hover, bordes
- `--mb-blue-500: #1E5A96` — color primario
- `--mb-amber-500: #F4A261` — acento cálido (CTAs, brand "Bombay")
- `--mb-success: #2A9D8F` — mensajes de éxito
- `--mb-error: #E63946` — errores

### Tipografía

- **Display**: Georgia/serif para "Bombay" y titulares
- **Body**: system-ui sans-serif
- **Mono**: SF Mono/Monaco para credenciales

### Brand signature

"MiBombay" con tratamiento dual: "Mi" en sans-serif blanco, "Bombay" en serif italic amber.

### Fragments Thymeleaf

- `fragments/layout.html` — estructura base con CDNs
- `fragments/navbar.html` — navbar con autenticación condicional (Spring Security)
- `fragments/footer.html` — footer
- `fragments/alerts.html` — mensajes flash
- `fragments/sidebar.html` — sidebar del dashboard

### Dashboard layout

Layout con sidebar izquierda (`col-md-3`) + contenido principal (`col-md-9`). Sidebar preparada para futuras secciones (mesas, menú, pedidos, reportes).

## Convenciones

- Usar anotaciones Lombok en entidades/DTOs para reducir boilerplate
- Plantillas Thymeleaf van en `src/main/resources/templates/`
- Assets estáticos van en `src/main/resources/static/`
- Responder y comunicar en español con el usuario
