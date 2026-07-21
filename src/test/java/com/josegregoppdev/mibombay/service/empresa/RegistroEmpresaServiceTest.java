package com.josegregoppdev.mibombay.service.empresa;

import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTORequest;
import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTOResponse;
import com.josegregoppdev.mibombay.mapper.empresa.EmpresaMapper;
import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import com.josegregoppdev.mibombay.service.usuario.PasswordGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroEmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGeneratorService passwordGeneratorService;

    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private RegistroEmpresaService service;

    @Test
    void registrar_creaAdminYCajeroExitosamente() {
        EmpresaDTORequest dto = crearEmpresaDTORequest();
        Empresa empresa = crearEmpresa();

        when(empresaRepository.existsBySubdominio(dto.getSubdominio())).thenReturn(false);
        when(usuarioRepository.existsByEmail(dto.getEmailEncargado())).thenReturn(false);
        when(empresaMapper.toEntity(dto)).thenReturn(empresa);
        when(empresaRepository.save(any())).thenReturn(empresa);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
        when(passwordGeneratorService.generarPasswordTemporal()).thenReturn("TempPass123!");
        when(usuarioRepository.save(any())).thenReturn(null);

        EmpresaDTOResponse resultado = service.registrar(dto);

        assertNotNull(resultado);
        assertEquals(dto.getNombre(), resultado.getNombreEmpresa());
        assertEquals("admin_cajero@test.com", resultado.getEmailCajero());
        assertEquals("TempPass123!", resultado.getPasswordCajero());
    }

    @Test
    void registrar_subdominioDuplicado_lanzaExcepcion() {
        EmpresaDTORequest dto = crearEmpresaDTORequest();

        when(empresaRepository.existsBySubdominio(dto.getSubdominio())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(dto));
        assertTrue(ex.getMessage().contains("ya está en uso"));
    }

    @Test
    void registrar_emailDuplicado_lanzaExcepcion() {
        EmpresaDTORequest dto = crearEmpresaDTORequest();

        when(empresaRepository.existsBySubdominio(dto.getSubdominio())).thenReturn(false);
        when(usuarioRepository.existsByEmail(dto.getEmailEncargado())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(dto));
        assertTrue(ex.getMessage().contains("ya está registrado"));
    }

    @Test
    void registrar_passwordsNoCoinciden_lanzaExcepcion() {
        EmpresaDTORequest dto = crearEmpresaDTORequest();
        dto.setConfirmarPasswordEncargado("OtraPassword456!");

        when(empresaRepository.existsBySubdominio(dto.getSubdominio())).thenReturn(false);
        when(usuarioRepository.existsByEmail(dto.getEmailEncargado())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(dto));
        assertTrue(ex.getMessage().contains("no coinciden"));
    }
}
