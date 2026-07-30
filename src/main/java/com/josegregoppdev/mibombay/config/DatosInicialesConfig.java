package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.ingrediente.IngredienteRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import com.josegregoppdev.mibombay.service.usuario.PasswordGeneratorService;
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
public class DatosInicialesConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;

    @Override
    public void run(String... args) {
        crearSuperAdministrador();
        crearRestauranteDemo();
    }

    private void crearSuperAdministrador() {
        if (!usuarioRepository.existsByEmail("SuperAdministrador@gmail.com")) {
            Usuario superAdmin = Usuario.builder()
                    .tenantId("SUPER_ADMIN")
                    .email("SuperAdministrador@gmail.com")
                    .passwordHash(passwordEncoder.encode("Mora.Kristoff_26123009"))
                    .nombreCompleto("SuperAdministrador")
                    .documentoHash(passwordEncoder.encode("00000000"))
                    .rol(Rol.SUPER_ADMINISTRADOR)
                    .activo(true)
                    .debeCambiarPassword(false)
                    .build();
            usuarioRepository.save(superAdmin);
            log.info("SuperAdministrador creado correctamente");
        } else {
            log.debug("SuperAdministrador ya existe");
        }
    }

    private void crearRestauranteDemo() {
        String tenantId;

        if (empresaRepository.existsBySubdominio("1")) {
            log.debug("Restaurante demo ya existe");
            tenantId = empresaRepository.findBySubdominio("1").orElseThrow().getTenantId();
        } else {
            tenantId = "tnt_demo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 28);

            Empresa empresa = Empresa.builder()
                    .tenantId(tenantId)
                    .subdominio("1")
                    .nombre("Restaurante Demo")
                    .correo("demo@mibombay.com")
                    .telefono("+57 300 000 0000")
                    .direccion("Carrera 1 # 1-01")
                    .nombreEncargado("Admin Demo")
                    .documentoEncargadoHash(passwordEncoder.encode("1234567890"))
                    .activo(true)
                    .build();
            empresaRepository.save(empresa);

            Usuario admin = Usuario.builder()
                    .tenantId(tenantId)
                    .email("demo@mibombay.com")
                    .passwordHash(passwordEncoder.encode("demo1234"))
                    .nombreCompleto("Admin Demo")
                    .telefono("+57 300 000 0000")
                    .documentoHash(passwordEncoder.encode("1234567890"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .debeCambiarPassword(false)
                    .ultimoCambioPassword(LocalDateTime.now())
                    .build();
            usuarioRepository.save(admin);

            String passwordCajero = passwordGeneratorService.generarPasswordTemporal();

            Usuario cajero = Usuario.builder()
                    .tenantId(tenantId)
                    .email("demo_cajero@mibombay.com")
                    .passwordHash(passwordEncoder.encode(passwordCajero))
                    .nombreCompleto("Cajero Demo")
                    .documentoHash(passwordEncoder.encode("1234567890"))
                    .rol(Rol.CAJERO)
                    .activo(true)
                    .debeCambiarPassword(true)
                    .build();
            usuarioRepository.save(cajero);

            log.info("Restaurante demo creado: subdominio='1', admin=demo@mibombay.com / demo1234, cajero=demo_cajero@mibombay.com / {}", passwordCajero);
        }

        crearIngredientesDemo(tenantId);
    }

    private void crearIngredientesDemo(String tenantId) {
        if (ingredienteRepository.existsByCodigoAndTenantId("CAR-001", tenantId)) {
            log.debug("Ingredientes demo ya existen");
            return;
        }

        List<Ingrediente> ingredientes = List.of(
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("CAR-001").nombre("Carne molida de res")
                        .categoria(Categoria.CARNES).unidadMedida(UnidadMedida.KILOGRAMO)
                        .costoUnitarioActual(new BigDecimal("18.00")).stockActual(new BigDecimal("30")).stockMinimo(new BigDecimal("10")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("PAN-001").nombre("Pan de hamburguesa")
                        .categoria(Categoria.GRANOS).unidadMedida(UnidadMedida.UNIDAD)
                        .costoUnitarioActual(new BigDecimal("1.50")).stockActual(new BigDecimal("100")).stockMinimo(new BigDecimal("30")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("PAN-002").nombre("Pan de perro caliente")
                        .categoria(Categoria.GRANOS).unidadMedida(UnidadMedida.UNIDAD)
                        .costoUnitarioActual(new BigDecimal("1.20")).stockActual(new BigDecimal("80")).stockMinimo(new BigDecimal("20")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("LEC-001").nombre("Queso americano en tajadas")
                        .categoria(Categoria.LACTEOS).unidadMedida(UnidadMedida.UNIDAD)
                        .costoUnitarioActual(new BigDecimal("0.80")).stockActual(new BigDecimal("150")).stockMinimo(new BigDecimal("50")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("VER-001").nombre("Lechuga crespa")
                        .categoria(Categoria.VERDURAS).unidadMedida(UnidadMedida.KILOGRAMO)
                        .costoUnitarioActual(new BigDecimal("4.00")).stockActual(new BigDecimal("10")).stockMinimo(new BigDecimal("5")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("VER-002").nombre("Tomate")
                        .categoria(Categoria.VERDURAS).unidadMedida(UnidadMedida.KILOGRAMO)
                        .costoUnitarioActual(new BigDecimal("3.50")).stockActual(new BigDecimal("15")).stockMinimo(new BigDecimal("5")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("PAP-001").nombre("Papa congelada (francesa)")
                        .categoria(Categoria.OTROS).unidadMedida(UnidadMedida.KILOGRAMO)
                        .costoUnitarioActual(new BigDecimal("5.00")).stockActual(new BigDecimal("40")).stockMinimo(new BigDecimal("10")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("ACE-001").nombre("Aceite para freír")
                        .categoria(Categoria.OTROS).unidadMedida(UnidadMedida.LITRO)
                        .costoUnitarioActual(new BigDecimal("7.50")).stockActual(new BigDecimal("20")).stockMinimo(new BigDecimal("5")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("SAL-001").nombre("Salsa de tomate (kétchup)")
                        .categoria(Categoria.CONDIMENTOS).unidadMedida(UnidadMedida.LITRO)
                        .costoUnitarioActual(new BigDecimal("4.00")).stockActual(new BigDecimal("10")).stockMinimo(new BigDecimal("3")).activo(true).build(),
                Ingrediente.builder()
                        .tenantId(tenantId).codigo("SAL-002").nombre("Mayonesa")
                        .categoria(Categoria.CONDIMENTOS).unidadMedida(UnidadMedida.LITRO)
                        .costoUnitarioActual(new BigDecimal("5.00")).stockActual(new BigDecimal("10")).stockMinimo(new BigDecimal("3")).activo(true).build()
        );

        ingredienteRepository.saveAll(ingredientes);
        log.info("10 ingredientes demo creados para el restaurante");
    }
}
