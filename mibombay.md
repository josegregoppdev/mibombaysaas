MiBombay
DOCUMENTO MAESTRO CONSOLIDADO
Especificación funcional, reglas de negocio, arquitectura y decisiones técnicas
Versión consolidada — 27 de julio de 2026
Documento preparado a partir de los dos documentos maestros aportados. Se consolidaron contenidos coincidentes para evitar redundancias y se priorizaron las reglas más recientes del documento de 27-07-2026.
1. Propósito y visión del proyecto
MiBombay es una plataforma SaaS (Software as a Service) orientada a la gestión de restaurantes. La solución debe permitir que cada empresa administre su operación dentro de un espacio lógico aislado, con usuarios, productos, ingredientes, recetas, inventario, ventas, caja, configuración y demás información propia.
La primera versión prioriza el flujo POS de venta de barra: el cliente llega al establecimiento, realiza su pedido, paga y recibe el pedido. La arquitectura debe permitir incorporar posteriormente mesas, meseros, reservas, cocina, domicilios y otros módulos sin romper el flujo actual.
El sistema será una aplicación web responsive, usable desde computador y teléfono, principalmente por telefono. La facturación será interna mediante documentos PDF; la facturación electrónica oficial queda fuera del alcance actual.
2. Alcance de la primera versión
POS orientado inicialmente a venta de barra.
SaaS multi-tenant con aislamiento por tenantId.
Acceso de los restaurantes mediante subdominios.
Área de SuperAdministrador para administrar la plataforma.
Registro de empresas/restaurantes y creación inicial de usuarios.
Roles iniciales: SUPER_ADMINISTRADOR, ADMINISTRADOR y CAJERO.
Gestión de ingredientes, recetas, productos, adiciones y combos.
Inventario con reglas centralizadas y trazabilidad mediante movimientos.
Compras, proveedores, ventas, devoluciones y caja según las reglas consolidadas.
Generación de documentos internos de venta en PDF.
Interfaz responsive mediante Thymeleaf, HTML, CSS y JavaScript.
3. Arquitectura SaaS multi-tenant
Cada restaurante representa un tenant y posee un tenantId único. Toda entidad propia de una empresa debe estar asociada al tenant correspondiente. Los repositorios, consultas y servicios deben garantizar el aislamiento; no se debe confiar únicamente en filtros de la interfaz.
Los tenants utilizarán subdominios. El mecanismo técnico definitivo para resolver el tenant a partir del subdominio se definirá durante la implementación.
La arquitectura SaaS debe mantenerse suficientemente desacoplada de las reglas específicas del restaurante para que pueda reutilizarse posteriormente como base de otros proyectos SaaS.
4. SuperAdministrador, empresas y usuarios
El SuperAdministrador representa al operador de la plataforma MiBombay. Puede visualizar empresas registradas, consultar su estado, suscripciones, pagos y vencimientos, y activar o desactivar empresas según las reglas del SaaSpor Defecto el sistema tendra un SuperAdministrador ese sera su nombre de usuario y la clave sera Mora.Kristoff_26123009
Al crear un restaurante se inicializa automáticamente su estructura básica y se generan un usuario Administrador y un usuario Cajero. El Administrador configura el restaurante; el Cajero dispone de permisos limitados y no puede administrar libremente ingredientes, recetas, productos o configuraciones críticas.
Roles iniciales conceptuales:
SUPER_ADMINISTRADOR
ADMINISTRADOR
CAJERO
Podrán aparecer posteriormente roles como MESERO, COCINERO o DOMICILIARIO, pero no forman parte de la definición cerrada actual.
5. Seguridad y autenticación
Spring Security para autenticación y autorización.
Login y manejo seguro de contraseñas.
Separación entre SuperAdministrador y usuarios de cada tenant.
Control de acceso por rol y permisos.
Protección de rutas.
Aislamiento por tenant.
Control de acceso según el estado de la empresa o suscripción.
6. Stack tecnológico y arquitectura de aplicación
Java 21.
Spring Boot 4.1.0.
Spring MVC.
Spring Security.
Spring Data JPA / Hibernate.
MySQL y driver JDBC.
Thymeleaf, HTML, CSS y JavaScript.
Maven.
Lombok.
MapStruct 1.6.3.
DTOs.
MVC y arquitectura por capas.
Principios SOLID, Clean Code, bajo acoplamiento y alta cohesión.
La librería visual definitiva para Thymeleaf queda pendiente de confirmación. Bootstrap fue la opción mencionada previamente, pero no debe considerarse una decisión irrevocable.
Flujo conceptual del backend:
Controller → Service → Repository → Entity / Database
Controller: recibe peticiones, valida entrada básica, coordina el flujo HTTP y entrega vistas o respuestas.
Service: contiene reglas de negocio, coordina entidades, ejecuta operaciones transaccionales y aplica validaciones.
Repository: concentra el acceso a datos mediante Spring Data JPA.
Entity: representa persistencia y relaciones del dominio.
DTO: representa los datos de entrada/salida y evita exponer directamente entidades JPA cuando no corresponda.
MapStruct: realiza el mapeo entre Entity y DTO.
Lombok: reduce código repetitivo de getters, setters, constructores y builders.
7. ConfiguracionTenant
Cada restaurante tendrá una única configuración centralizada mediante ConfiguracionTenant. La configuración se crea automáticamente al crear el tenant y lleva tenantId.
La regla mínima es permitirInventarioNegativo.
No se incluyen moneda ni idioma en la primera versión; el sistema se orienta inicialmente a Colombia y español.
Podrán agregarse posteriormente decimales, impuesto por defecto y otras preferencias.
Los servicios deben consultar la configuración y no duplicar reglas como constantes hardcodeadas.
8. Modelo de productos, ingredientes y recetas
El modelo comercial se centra en una única entidad Producto. Su naturaleza se diferencia mediante TipoProducto, evitando entidades duplicadas.
TipoProducto propuesto:
CON_RECETA
SIN_RECETA
ADICIONAL
8.1. Ingrediente
Ingrediente representa materias primas o elementos controlados en inventario que pueden utilizarse en recetas o procesos de producción.
id
tenantId
código
nombre
descripción
categoría
unidad de medida
costo unitario actual
stock actual
stock mínimo
activo
fecha de creación
fecha de actualización
usuario de creación
usuario de actualización
Puede existir un indicador conceptual controlaInventario para distinguir elementos que requieren control. La implementación definitiva se cerrará con el módulo de inventario.
8.2. Unidad de medida y precisión
En la primera versión la unidad de medida se manejará mediante un enum de Java, no como entidad independiente. Cada ingrediente y producto sin receta tendrá una unidad base única y no habrá conversiones automáticas entre unidades.
KILOGRAMO
GRAMO
LITRO
MILILITRO
UNIDAD
PORCION
Las cantidades que requieran precisión deben manejarse con BigDecimal. Por ejemplo, 0,021 kg representa 21 gramos cuando la unidad base es kilogramo. Para dinero y cantidades exactas se evitarán float/double.
8.3. Receta
Receta representa la preparación de un producto y se mantiene independiente de Producto para permitir reutilización. Una receta puede ser utilizada por diferentes productos.
id
tenantId
nombre
descripción
costoProduccion
activa
fecha de creación
fecha de actualización
usuario de creación
usuario de actualización
El precio de venta no pertenece a Receta: pertenece a Producto.
8.4. DetalleReceta
DetalleReceta representa los ingredientes que componen una receta.
id
recetaId
ingredienteId
cantidad
unidad de medida si se requiere explícitamente
costoUnitario
costoTotal
observaciones opcionales
El costo se deriva de cantidad × costoUnitario y el costo de producción de la receta corresponde a la suma de los costos de sus detalles.
8.5. Producto CON_RECETA
Es un producto elaborado a partir de una receta, como una Hamburguesa Clásica. Su costo puede derivarse del costo de producción de la receta y su precio de venta es independiente y pertenece al Producto.
8.6. Producto SIN_RECETA
Es un artículo comprado directamente y revendido sin preparación mediante receta, por ejemplo una gaseosa en lata. Maneja costo de compra, costo interno y precio de venta, además de inventario.
8.7. Producto ADICIONAL
Es un producto que normalmente no aparece en el catálogo principal del cajero, pero puede ofrecerse como complemento. Ejemplos: carne extra, queso extra o papa extra. Puede tener su propia receta.
9. Adiciones
Adición representa una definición o conjunto de opciones adicionales. Se relaciona con productos cuyo TipoProducto sea ADICIONAL mediante DetalleAdicion.
Estructura conceptual: Adición → DetalleAdicion → Producto (ADICIONAL).
DetalleAdicion no apunta directamente a Ingrediente. Esto permite reutilizar la estructura de Producto y, cuando corresponda, la receta propia del producto adicional.
En el POS, el cajero selecciona el producto base y, mediante una acción de Adición, visualiza únicamente los productos adicionales disponibles. Esto mantiene limpia la pantalla principal y permite reportar posteriormente cuántas unidades de cada adición fueron vendidas.
10. Combos
Un Combo agrupa productos que se venden como oferta conjunta. Puede mezclar productos CON_RECETA y SIN_RECETA. Los productos ADICIONALES se gestionan por separado.
Combo: id, tenantId, código, nombre, descripción, precio de venta, activo y auditoría.
DetalleCombo: comboId, productoId y cantidad.
Podrán añadirse posteriormente imagen, costo total calculado, margen, disponibilidad y horarios.
11. Inventario
El inventario se rige por una regla central: ninguna entidad ni servicio distinto de InventarioService puede modificar directamente el stock.
InventarioService es el único responsable de modificar stock y debe registrar el Movimiento correspondiente dentro de la misma transacción.
Ingredientes y productos sin receta tendrán stock mínimo configurable.
Cuando stockActual sea igual o menor a stockMinimo se muestra una alerta de inventario bajo.
La alerta no bloquea ventas ni otras operaciones.
Cada cambio de stock debe dejar trazabilidad.
11.1. Inventario negativo
permitirInventarioNegativo es verdadero por defecto. Si es verdadero, una venta puede confirmarse aun cuando el stock sea insuficiente. Si es falso, se valida disponibilidad al confirmar la venta.
La validación no se realiza durante la construcción del pedido. Si falla al confirmar con inventario negativo deshabilitado, la operación completa se cancela: no se descuenta stock y no se generan movimientos.
11.2. Movimiento de inventario
Movimiento es una entidad de historial y auditoría; no es la entidad que modifica el stock. Registra qué ocurrió con el inventario y conserva stock anterior y stock nuevo.
compra
venta
merma
ajuste
inventario inicial
devolución
Los movimientos confirmados no se editan ni eliminan. Una corrección se registra como un nuevo movimiento de ajuste.
11.3. Costo de inventario
Se utilizará costo promedio ponderado. Al confirmar una compra, InventarioService recalcula el costo promedio considerando existencias y nueva compra y guarda el nuevo costo en el ingrediente o producto sin receta correspondiente.
No se creará una entidad independiente de historial de costos en la primera versión.
La evolución histórica puede reconstruirse desde Compras y CompraDetalle.
El costo interno y el precio de venta son conceptos independientes.
Una variación del costo de compra no cambia automáticamente el precio de venta.
El precio de venta solo cambia mediante una acción explícita de un usuario autorizado.
12. Proveedores y compras
Proveedor representa al proveedor del restaurante.
Compra representa una adquisición realizada a un proveedor.
CompraDetalle representa cada ingrediente o producto adquirido, con cantidad y costo de compra.
Las compras pertenecen a un tenant.
Una compra confirmada puede producir cambios de inventario.
La confirmación de una compra debe pasar por InventarioService.
Los datos históricos deben conservarse para trazabilidad y análisis de costos.
13. Ventas
Venta representa la operación comercial y VentaDetalle los productos incluidos. El precio de venta y el nombre histórico del producto deben congelarse en el detalle para conservar el valor de la operación aunque el catálogo cambie posteriormente.
La venta se maneja mediante estados de negocio, no mediante un booleano Activo/Inactivo como ciclo de vida principal.
En Espera
Confirmada
Anulada
Solo una venta confirmada afecta inventario y caja. Una venta en espera no produce efectos definitivos sobre inventario ni caja.
13.1. Ventas en espera
La venta en espera utiliza la entidad Venta existente; no se crea una entidad independiente. El cajero puede poner una venta en espera, atender otra operación y posteriormente reanudar su propia venta.
El cajero puede consultar y reanudar sus propias ventas en espera.
El administrador puede consultar las ventas en espera del restaurante.
La interfaz tendrá una acción equivalente a Poner en espera y una zona/listado de Ventas en espera.
DTOs posibles: VentaDTO, VentaDetalleDTO y un DTO específico para la vista de ventas en espera.
13.2. Devoluciones
Una devolución corresponde a una venta confirmada cuyo producto debe regresar al inventario antes de determinar su destino físico. VentaService inicia la devolución y solicita a InventarioService la reversión del impacto de inventario.
Los ingredientes y productos sin receta consumidos por la venta devuelta se reintegran según la operación registrada. Si posteriormente no son aptos para volver a venderse, el administrador puede registrarlos como desperdicio/merma mediante InventarioService.
La devolución comercial y la merma son procesos separados.
14. Inventario físico y ajustes
El ajuste manual general se reserva para el proceso de inventario físico. El administrador inicia el proceso, visualiza la cantidad teórica, cuenta físicamente y registra la cantidad real. El sistema compara ambas cantidades y corrige las diferencias mediante movimientos de ajuste auditables.
15. Caja y cierre de caja
CierreCaja será un módulo propio con entidades, DTOs, servicios y controladores.
CierreCaja: tenantId, cajero, administrador, fecha/hora, total esperado, total contado, diferencia y estado.
CierreCajaDenominacion: relación con CierreCaja, denominación y cantidad física contada.
El efectivo se cuenta por denominaciones, no introduciendo manualmente un monto total.
El sistema multiplica denominación × cantidad y calcula automáticamente el efectivo contado.
Se compara efectivo esperado contra efectivo contado.
El resultado puede ser diferencia positiva, diferencia negativa o caja cuadrada.
Medios mínimos: efectivo, datáfono y transferencias.
El cierre registra cajero responsable y administrador que autoriza/confirma.
Un cierre confirmado no se edita; cualquier corrección posterior se registra como ajuste u observación.
Denominaciones colombianas contempladas inicialmente:
Monedas: $50, $100, $200, $500 y $1.000. Billetes: $1.000, $2.000, $5.000, $10.000, $20.000, $50.000 y $100.000.
16. Reportes X y Z
Reporte X es una consulta durante la operación. Puede generarse en cualquier momento y muestra cantidad de transacciones, total monetario y total por medio de pago, como mínimo efectivo, datáfono y transferencias. No constituye el cierre definitivo.
Reporte Z representa el reporte final del día/cierre. Resume transacciones y ventas del periodo, se relaciona con el cierre de caja y permite comprobar totales y diferencias. El formato exacto se definirá durante la implementación.
17. PDF de venta y acceso multiplataforma
MiBombay no será un sistema de facturación electrónica. Podrá generar documentos internos de venta/factura en PDF con productos, cantidades, precios, subtotales, total y datos del restaurante; descuentos e impuestos podrán incorporarse como información interna si se implementan.
La aplicación funcionará mediante navegador web en computadores de escritorio, portátiles y teléfonos. La primera versión no contempla una aplicación móvil nativa.
18. Auditoría y consistencia
Los registros confirmados que representen historial o auditoría no se editan ni eliminan.
Los errores se corrigen mediante nuevas operaciones o movimientos correctivos.
Toda modificación de stock genera trazabilidad.
Inventario y su Movimiento deben ejecutarse dentro de la misma transacción.
El precio de venta y el costo interno son independientes.
Las ventas en espera no generan efectos definitivos hasta su confirmación.
Los cierres confirmados no se modifican.
Las entidades principales deben contemplar, cuando corresponda, fechaCreacion, fechaActualizacion, usuarioCreacion, usuarioActualizacion y activo.
19. DTOs y capa de presentación
Las operaciones expuestas por API o por flujos de presentación utilizarán DTOs para separar el modelo de persistencia de los contratos de entrada/salida. Las entidades no deben exponerse directamente como contrato cuando no corresponda.
VentaDTO
VentaDetalleDTO
VentaEnEsperaDTO
CierreCajaDTO
CierreCajaDenominacionDTO
Los DTOs concretos se definirán según cada flujo. MapStruct será responsable del mapeo entre entidades y DTOs cuando corresponda.
20. Multi-tenant: entidades que deben aislarse
Entre las entidades propias del restaurante que deben respetar tenantId se encuentran CierreCaja, CierreCajaDenominacion, Venta, Compra, Movimiento, Ingrediente, Producto, Cliente, Proveedor y ConfiguracionTenant. Los catálogos verdaderamente globales son la única excepción.
21. Principios de desarrollo
SOLID y Clean Code.
Separación de responsabilidades.
Controllers delgados.
Reglas de negocio en Services.
Repositories enfocados en persistencia.
DTOs para intercambio de información.
MapStruct para conversiones.
Entidades JPA correctamente modeladas.
Relaciones claras y bajo acoplamiento.
Evitar duplicación de lógica.
Enums para estados o tipos cerrados.
BigDecimal para dinero y cantidades que requieran precisión.
Transacciones correctamente definidas.
Manejo adecuado de errores.
Auditoría de información importante.
Pruebas unitarias y de integración.
Seguridad desde el diseño.
22. Flujo general de configuración del restaurante
SuperAdministrador registra la empresa.
El sistema crea el Tenant.
El sistema crea Administrador y Cajero.
El Administrador inicia sesión.
Configura categorías.
Crea ingredientes.
Define unidades de medida disponibles.
Configura costos e inventario inicial.
Crea recetas y sus detalles.
Crea productos CON_RECETA, SIN_RECETA o ADICIONAL.
Crea productos ADICIONALES y las Adiciones.
Relaciona productos ADICIONALES mediante DetalleAdicion.
Crea Combos y los relaciona mediante DetalleCombo.
El restaurante queda preparado para operar el módulo de ventas.
23. Entidades y relaciones conceptuales consolidadas
SaaS: Tenant → Usuarios, configuración y demás información aislada del restaurante.
Receta → DetalleReceta → Ingrediente.
Producto → TipoProducto: CON_RECETA / SIN_RECETA / ADICIONAL.
Producto CON_RECETA → Receta → DetalleReceta → Ingrediente.
Producto SIN_RECETA → costo de compra + precio de venta + inventario.
Producto ADICIONAL → puede tener receta + precio de venta.
Adición → DetalleAdicion → Producto ADICIONAL.
Combo → DetalleCombo → Producto.
Venta → VentaDetalle → Producto/Combo y, cuando corresponda, detalles de adiciones.
CierreCaja → CierreCajaDenominacion.
InventarioService → Stock + Movimiento dentro de la misma transacción.
24. Decisiones de diseño consolidadas
No crear una entidad independiente ProductoSinReceta; usar Producto + TipoProducto.
El precio de venta pertenece a Producto, no a Receta.
La receta es independiente de Producto y reutilizable.
DetalleReceta apunta a Ingrediente.
DetalleAdicion apunta a Producto ADICIONAL.
Los productos ADICIONALES pueden tener receta propia.
Los productos ADICIONALES no se muestran normalmente en el catálogo principal del cajero.
Las unidades de medida se manejan inicialmente mediante enum de Java.
Los costos y cantidades con precisión exacta se manejan con BigDecimal.
La configuración específica del tenant se centraliza en ConfiguracionTenant.
El stock solo cambia mediante InventarioService.
Todo cambio de stock genera un Movimiento.
Los registros históricos confirmados no se editan ni eliminan.
Las ventas en espera utilizan Venta existente y un estado En Espera.
Solo las ventas confirmadas afectan inventario y caja.
El costo de inventario utiliza costo promedio ponderado.
Los cambios de costo no alteran automáticamente el precio de venta.
El sistema es multi-tenant y tenantId es fundamental para el aislamiento.
Spring Security gestiona seguridad y autorización.
La arquitectura usa MVC y capas con DTOs, MapStruct y Spring Data JPA/Hibernate.
La facturación es mediante PDF interno, no electrónica.
25. Fuera del alcance inmediato
Mesas.
Meseros.
Reservas.
Aplicación móvil nativa.
Facturación electrónica e integración DIAN.
Conversión automática entre unidades.
Entidad independiente de historial de costos en la primera versión.
Métodos avanzados de valoración como FIFO/LIFO.
Motor avanzado de devoluciones más allá del flujo definido.
Detalles avanzados de inventario físico aún no definidos.
26. Pendientes de definición para implementación
Permisos concretos por rol para devolución, merma, inventario físico y cierre.
Mensajes detallados de falta de stock cuando el inventario negativo esté deshabilitado.
Diseño visual final del POS.
Estructura exacta de endpoints y contratos DTO.
Reportes finales y filtros.
Flujo detallado de inventario físico y ajustes.
Auditoría técnica y trazabilidad de usuario.
Estrategia concreta de multi-tenancy y resolución por subdominio.
Estructura definitiva de Empresa/Tenant.
Roles y permisos definitivos.
Suscripciones, pagos, vencimientos y suspensión.
Recuperación de contraseña.
Lista definitiva de unidades y categorías.
Modelo definitivo de movimientos y descuentos de recetas/productos.
Reglas de combos y adiciones durante la venta.
Descuentos, impuestos internos y métodos de pago adicionales.
Clientes, mesas, cocina y domicilios.
Backups, despliegue en VPS, dominio, SSL, CI/CD y monitoreo.
Librería visual definitiva para Thymeleaf.
27. Roadmap de implementación
Fase 1 — Base del proyecto
Spring Boot, Maven y Java.
MySQL, Spring Data JPA y Hibernate.
Lombok y MapStruct.
Thymeleaf y MVC.
Spring Security.
Estructura de paquetes.
Fase 2 — SaaS
Tenant/Empresa, usuarios y roles.
Login.
Resolución por subdominio.
Aislamiento tenant.
SuperAdministrador.
Estado de empresa, suscripciones, pagos y activación/desactivación.
Fase 3 — Catálogo
Ingrediente y UnidadMedida.
Categorías.
Receta y DetalleReceta.
Producto y TipoProducto.
Combo y DetalleCombo.
Adición y DetalleAdicion.
Fase 4 — Inventario
Movimientos, entradas, salidas, consumos, mermas y ajustes.
Compras y proveedores.
Costo promedio ponderado.
Inventario físico.
Fase 5 — Ventas y caja
Venta y VentaDetalle.
Ventas en espera.
Adiciones y combos.
Pagos.
Devoluciones.
Caja y cierre.
Fase 6 — Documentos
PDF interno de venta.
Impresión.
Historial.
Fase 7 — Reportes
Ventas, productos y adiciones.
Costos, inventario y rentabilidad.
Consumos.
Reporte X y Reporte Z.
28. Criterio para evolucionar el proyecto
¿Es una regla de negocio?
¿A qué tenant pertenece la información?
¿Qué entidad representa el concepto?
¿La entidad ya existe y puede reutilizarse?
¿Se necesita una nueva relación?
¿Qué rol puede realizar la operación?
¿Qué impacto tiene sobre inventario?
¿Qué impacto tiene sobre costos?
¿Qué impacto tiene sobre ventas?
¿Qué información debe auditarse?
¿Qué DTO necesita?
¿Qué Service debe contener la lógica?
¿Qué Repository necesita?
¿Qué Controller/Vista necesita?
¿Qué pruebas deben agregarse?
No se deben crear entidades únicamente para resolver un caso aislado si una entidad existente puede representar correctamente el concepto.
29. Ejemplo integral del dominio
Ejemplo: Hamburguesa Clásica. Ingredientes: carne de res (KG), mayonesa (KG) y salsa de tomate (KG). La receta Hamburguesa Clásica utiliza, por ejemplo, 0,100 KG de carne, 0,021 KG de mayonesa y 0,021 KG de salsa. El Producto Hamburguesa Clásica es CON_RECETA y utiliza esa receta.
Carne Extra y Queso Extra pueden ser productos ADICIONALES, cada uno con su propia receta y precio de venta. La Adición Extras relaciona ambos mediante DetalleAdicion. En el POS, el cajero selecciona Hamburguesa Clásica, pulsa Adición y selecciona Carne Extra; la hamburguesa mantiene su receta original y la adición se trata como producto adicional.
30. Estado del documento
Estado: BASE CONSOLIDADA / EN CONSTRUCCIÓN. Este documento reúne las decisiones funcionales, de negocio y técnicas disponibles hasta el 27 de julio de 2026. Las reglas más recientes del Documento Maestro de 27-07-2026 se toman como referencia para los puntos que fueron posteriormente definidos con mayor precisión.
La prioridad inmediata es construir una base sólida de SaaS multi-tenant, login, seguridad, tenant, usuarios, roles, ingredientes, recetas, DetalleReceta, productos, TipoProducto, adiciones, DetalleAdicion, combos y DetalleCombo; después se continuará con inventario, ventas, caja, PDF y reportes.
31. Principios finales del proyecto
MiBombay debe desarrollarse como un producto comercial real, no únicamente como proyecto académico.
La seguridad y el aislamiento de tenants son requisitos estructurales.
El dominio debe mantenerse claro y reutilizable.
Las reglas de negocio deben estar centralizadas y ser trazables.
La experiencia de usuario debe priorizar la rapidez del POS de barra.
La arquitectura debe permitir agregar módulos sin romper los existentes.
La base SaaS debe poder reutilizarse posteriormente para otros productos.
Anexo — Criterio de consolidación
Se utilizaron como fuentes los dos documentos aportados por el usuario. El documento de 27-07-2026 contiene las reglas más recientes sobre ventas, inventario, movimientos, costo promedio, devoluciones, caja y reportes; el documento de 25-07-2026 aporta el contexto general del SaaS, stack, modelo inicial de productos, recetas, adiciones, combos y roadmap. Los contenidos coincidentes fueron integrados una sola vez para evitar redundancia.