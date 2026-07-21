<p align="center">
  <span style="font-size: 2.5rem; font-weight: 700;">Mi<span style="font-style: italic; color: #F4A261;">Bombay</span></span>
</p>

<p align="center">
  <strong>Sistema de gestión SaaS multi-tenant para restaurantes</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot 4.1.0">
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=flat-square&logo=bootstrap&logoColor=white" alt="Bootstrap 5.3">
  <img src="https://img.shields.io/badge/License-Proprietary-blue?style=flat-square" alt="License">
</p>

---

## Descripción

**MiBombay** es una plataforma SaaS multi-tenant para la gestión integral de restaurantes. Permite a los dueños de restaurantes crear su empresa, gestionar mesas, menú, pedidos y reportes desde un solo lugar.

Cada restaurante opera como un tenant aislado con su propio `tenantId`, garantizando la separación completa de datos entre empresas.

---

## Características

- **Multi-tenant** — Aislamiento de datos por empresa con `tenantId`
- **Auto-registro** — Los clientes crean su empresa y empiezan a usar el sistema inmediatamente
- **Roles y permisos** — Sistema de roles (`ADMIN`, `CAJERO`) con Spring Security
- **Seguridad OWASP Top 10** — BCrypt (strength 12), `@Valid` + `@Pattern` anti-inyección, Thymeleaf XSS escape, sesiones seguras, CSRF
- **UI moderna** — Bootstrap 5.3 con diseño responsivo mobile-first y paleta personalizada
- **Arquitectura escalable** — Preparado para futuras funcionalidades (mesas, menú, pedidos, reportes)
- **16+ tests unitarios** — JUnit 5 + Mockito con TestDataFactory

---

## Tech Stack

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 21 | Lenguaje base |
| **Spring Boot** | 4.1.0 | Framework principal |
| **Spring Security** | - | Autenticación y autorización |
| **Spring Data JPA** | - | Persistencia de datos |
| **Thymeleaf** | - | Motor de plantillas server-side |
| **MySQL** | 8.0+ | Base de datos |
| **Bootstrap** | 5.3 | Framework CSS (via CDN) |
| **Bootstrap Icons** | 1.11+ | Iconografía (via CDN) |
| **Lombok** | - | Reducción de boilerplate |
| **MapStruct** | 1.6.3 | Mapeo DTO ↔ Entidad |
| **Maven** | - | Gestión de dependencias |

---

## Instalación

### Requisitos previos

- Java 21+
- MySQL 8.0+
- Maven (o usar el wrapper incluido `./mvnw`)

### Pasos

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/mibombay.git
   cd mibombay
   ```

2. **Configurar la base de datos**

   Copia la plantilla de configuración:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application-dev.properties
   ```

   Edita `application-dev.properties` con tus credenciales de MySQL:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/mibombay?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=tu_usuario
   spring.datasource.password=tu_contraseña
   ```

3. **Compilar el proyecto**
   ```bash
   ./mvnw clean compile
   ```

4. **Ejecutar la aplicación**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Abrir en el navegador**
   
   Navega a [http://localhost:8080](http://localhost:8080)

---

## Uso

### Registro de empresa

1. Visita la página principal y haz clic en **"Crear mi empresa"**
2. Completa el formulario con los datos de tu empresa y del encargado
3. El sistema creará automáticamente:
   - Tu empresa con un `tenantId` único
   - Un usuario **ADMIN** (el encargado) con la contraseña que elegiste
   - Un usuario **CAJERO** con contraseña temporal generada automáticamente
4. Guarda las credenciales del cajero (solo se muestran una vez)

### Inicio de sesión

1. Ingresa tu email y contraseña
2. Si es tu primer login como cajero, deberás cambiar la contraseña temporal
3. Accede al dashboard con la información de tu empresa

---

## Estructura del Proyecto

```
mibombay/
├── src/
│   ├── main/
│   │   ├── java/com/josegregoppdev/mibombay/
│   │   │   ├── MibombayApplication.java
│   │   │   ├── common/              # Utilidades transversales
│   │   │   │   ├── audit/           # AuditableEntity
│   │   │   │   ├── tenant/          # TenantContext, TenantFilter
│   │   │   │   └── exception/       # GlobalExceptionHandler
│   │   │   ├── model/               # Entidades JPA
│   │   │   │   ├── usuario/         # Usuario, Rol
│   │   │   │   └── empresa/         # Empresa
│   │   │   ├── repository/          # Repositorios Spring Data JPA
│   │   │   │   ├── usuario/
│   │   │   │   └── empresa/
│   │   │   ├── service/             # Lógica de negocio
│   │   │   │   ├── usuario/         # CustomUserDetailsService, PasswordGeneratorService
│   │   │   │   └── empresa/         # RegistroEmpresaService
│   │   │   ├── controller/          # Controladores web
│   │   │   │   ├── landing/         # LandingController
│   │   │   │   ├── auth/            # LoginController, PasswordChangeController
│   │   │   │   ├── empresa/         # RegistroEmpresaController
│   │   │   │   └── dashboard/       # DashboardController
│   │   │   └── config/              # Configuraciones Spring
│   │   │       ├── SecurityConfig.java
│   │   │       └── PasswordEncoderConfig.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── application.properties.example
│   │       ├── templates/           # Plantillas Thymeleaf
│   │       │   ├── fragments/       # Fragmentos reutilizables
│   │       │   ├── landing.html
│   │       │   ├── login.html
│   │       │   ├── registro.html
│   │       │   ├── registro-exitoso.html
│   │       │   ├── cambiar-password.html
│   │       │   └── dashboard.html
│   │       └── static/
│   │           ├── css/styles.css   # Tokens + overrides de Bootstrap
│   │           └── js/app.js        # Interacciones mínimas
│   └── test/
├── pom.xml
└── README.md
```

---

## Arquitectura Multi-Tenant

El sistema utiliza **aislamiento por `tenantId`** en cada entidad del dominio:

- Cada empresa recibe un `tenantId` único al registrarse (formato: `tnt_` + UUID)
- El campo `subdominio` en `Empresa` está preparado para futuros subdominios
- `TenantContext` (ThreadLocal) mantiene el tenant actual durante el request
- `TenantFilter` limpia el contexto al final de cada request para evitar fugas
- Todas las queries filtran por `tenantId` para garantizar el aislamiento

---

## Seguridad (OWASP Top 10)

### A01 — Broken Access Control
- CSRF habilitado por defecto
- Logout invalida sesión y borra JSESSIONID
- Máximo 1 sesión por usuario
- DTOs evitan mass assignment

### A02 — Cryptographic Failures
- BCryptPasswordEncoder con strength 12
- Documento del encargado hasheado con BCrypt
- Contraseñas temporales generadas con `SecureRandom`

### A03 — Injection
- Thymeleaf escapa HTML automáticamente (XSS)
- JPA/Spring Data usa prepared statements (SQL injection)
- `@Valid` en DTOs con `@Pattern` para bloquear caracteres maliciosos (`< > " &`)

### A05 — Security Misconfiguration
- Session timeout: 10 minutos de inactividad
- Session fixation mitigation: `migrateSession()`
- Headers: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`
- Perfiles de configuración dev/prod separados

### A07 — Identification & Authentication Failures
- BCrypt para passwords
- Email único global
- Roles: `ADMIN` y `CAJERO` (enum `Rol`)
- Cambio de contraseña forzado al primer login del cajero

---

## Comandos útiles

```bash
# Compilar
./mvnw compile

# Ejecutar tests
./mvnw test

# Ejecutar un test específico
./mvnw test -Dtest=NombreClaseTest

# Construir JAR
./mvnw clean package

# Ejecutar la aplicación
./mvnw spring-boot:run
```

---

## Roadmap

- [x] Seguridad OWASP Top 10 (A01-A07)
- [x] DTOs con validación + MapStruct
- [ ] Mobile-First responsive design (Offcanvas sidebar)
- [ ] Gestión de mesas (estados, asignación de meseros)
- [ ] Menú digital (categorías, productos, precios)
- [ ] Sistema de pedidos (toma de pedidos, estados)
- [ ] Reportes y análisis (ventas, productos más vendidos)
- [ ] Múltiples sucursales por empresa
- [ ] Subdominios reales por empresa
- [ ] Recuperación de contraseña por email
- [ ] Autenticación de dos factores (2FA)

---

## Contribución

Este es un proyecto en desarrollo activo. Si deseas contribuir:

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## Licencia

Este proyecto es de código propietario. Todos los derechos reservados.

---

## Contacto

**Desarrollado por**: Jose Gregorio  
**Proyecto**: MiBombay - Sistema de gestión para restaurantes

---

<p align="center">
  <strong>Mi<span style="font-style: italic; color: #F4A261;">Bombay</span></strong> — Gestión sin fricción para tu restaurante
</p>
