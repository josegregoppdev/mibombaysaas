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
- **MapStruct 1.6.3** (mapeo DTO ↔ Entidad, procesador configurado en `pom.xml`)

## Estructura del Proyecto

Organización por capas con subcarpetas por dominio:

```
com.josegregoppdev.mibombay
├── MibombayApplication.java
├── common/                    # utilidades transversales
│   ├── audit/                 # AuditableEntity (timestamps automáticos)
│   ├── tenant/                # TenantContext, TenantFilter
│   └── exception/             # GlobalExceptionHandler
├── model/                     # entidades JPA (sin validaciones, solo JPA)
│   ├── usuario/               # Usuario, Rol
│   └── empresa/               # Empresa
├── dto/                       # DTOs con validaciones
│   ├── empresa/               # EmpresaDTORequest, EmpresaDTOResponse
│   └── usuario/               # UsuarioDTORequest, UsuarioDTOResponse
├── mapper/                    # Mappers MapStruct
│   ├── empresa/               # EmpresaMapper
│   └── usuario/               # UsuarioMapper
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

## Seguridad (OWASP Top 10)

### A01: Broken Access Control
- CSRF habilitado por defecto (Spring Security)
- Logout invalida sesión y borra JSESSIONID
- Máximo 1 sesión por usuario
- DTOs evitan mass assignment (no exponen campos sensibles al binding)
- `@InitBinder` es innecesario porque los DTOs no tienen setters no deseados

### A02: Cryptographic Failures
- `BCryptPasswordEncoder` con strength 12 para passwords
- Documento del encargado hasheado con BCrypt (no reversible)
- Contraseñas temporales generadas con `SecureRandom`

### A03: Injection
- Thymeleaf escapa HTML automáticamente (XSS)
- JPA/Spring Data usa prepared statements (SQL injection)
- `@Valid` en DTOs para validar entrada de datos

### A05: Security Misconfiguration
- Session timeout: 10 minutos de inactividad
- Session fixation: `.sessionFixation().migrateSession()`
- Sesión expirada redirige a `/login?expired=true` con mensaje
- Headers HTTP: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`
- Perfiles de config: dev/prod separados
- `.gitignore` ignora properties con secrets

### A07: Identification & Authentication Failures
- BCrypt para passwords
- Email único global
- Roles: `ADMIN`, `CAJERO` (enum `Rol`)
- Al crear empresa: se crea admin (con password del form) y cajero (password temporal generada)
- Campo `debeCambiarPassword` fuerza cambio de contraseña al primer login del cajero

## Properties

- `application.properties` — base, carga perfil activo + session timeout
- `application-dev.properties` — configuración local (MySQL local, logs)
- `application-prod.properties` — configuración producción (variables de entorno)
- `application.properties.example` — plantilla para GitHub (se sube)

Los archivos de properties reales están en `.gitignore`.

## Tests

- JUnit 5 + Mockito (`spring-boot-starter-test` incluido)
- Test Data Factory en `testdata/TestDataFactory.java` (provee objetos preconstruidos)
- Servicios con lógica: tests unitarios con `@ExtendWith(MockitoExtension.class)`
- Servicios sin dependencias: tests directos (sin mocks)

### Tests actuales

| Clase | Tests |
|---|---|
| `RegistroEmpresaServiceTest` | 4 |
| `PasswordGeneratorServiceTest` | 8 |
| `CustomUserDetailsServiceTest` | 4 |
| Total | 16 |

```bash
./mvnw test
./mvnw test -Dtest=RegistroEmpresaServiceTest
./mvnw test -Dtest=PasswordGeneratorServiceTest
./mvnw test -Dtest=CustomUserDetailsServiceTest
```

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
- **DTOs**: `EmpresaDTORequest`/`EmpresaDTOResponse` para empresa, `UsuarioDTORequest`/`UsuarioDTOResponse` para usuario. El resto simple `@RequestParam` sin DTO
- **MapStruct**: `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)` en interfaces dentro de `mapper/`
- **Entidades**: sin validaciones Javax/Jakarta, solo anotaciones JPA
- **Validaciones**: en los DTOs, no en las entidades
- **Tests**: usar `TestDataFactory` para construir objetos de prueba
- Plantillas Thymeleaf van en `src/main/resources/templates/`
- Assets estáticos van en `src/main/resources/static/`
- Responder y comunicar en español con el usuario
