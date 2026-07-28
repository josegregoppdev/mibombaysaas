package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import com.josegregoppdev.mibombay.service.usuario.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatosInicialesConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
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
        if (empresaRepository.existsBySubdominio("1")) {
            log.debug("Restaurante demo ya existe");
            return;
        }

        String tenantId = "tnt_demo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 28);

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
}
