package com.josegregoppdev.mibombay.service.empresa;

import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTORequest;
import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTOResponse;
import com.josegregoppdev.mibombay.mapper.empresa.EmpresaMapper;
import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import com.josegregoppdev.mibombay.service.usuario.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final EmpresaMapper empresaMapper;

    @Transactional
    public EmpresaDTOResponse registrar(EmpresaDTORequest dto) {
        validarSubdominio(dto.getSubdominio());
        validarEmailEncargado(dto.getEmailEncargado());
        validarPasswords(dto.getPasswordEncargado(), dto.getConfirmarPasswordEncargado());

        String tenantId = generarTenantId();

        Empresa empresa = empresaMapper.toEntity(dto);
        empresa.setTenantId(tenantId);
        empresa.setActivo(true);
        empresa.setDocumentoEncargadoHash(passwordEncoder.encode(dto.getDocumentoEncargado()));
        empresaRepository.save(empresa);

        Usuario admin = Usuario.builder()
                .tenantId(tenantId)
                .email(dto.getEmailEncargado())
                .passwordHash(passwordEncoder.encode(dto.getPasswordEncargado()))
                .nombreCompleto(dto.getNombreEncargado())
                .telefono(dto.getTelefonoEncargado())
                .documentoHash(empresa.getDocumentoEncargadoHash())
                .rol(Rol.ADMIN)
                .activo(true)
                .debeCambiarPassword(false)
                .ultimoCambioPassword(LocalDateTime.now())
                .build();
        usuarioRepository.save(admin);

        String passwordCajero = passwordGeneratorService.generarPasswordTemporal();
        String emailCajero = generarEmailCajero(dto.getEmailEncargado());

        Usuario cajero = Usuario.builder()
                .tenantId(tenantId)
                .email(emailCajero)
                .passwordHash(passwordEncoder.encode(passwordCajero))
                .nombreCompleto("Cajero " + dto.getNombre())
                .documentoHash(empresa.getDocumentoEncargadoHash())
                .rol(Rol.CAJERO)
                .activo(true)
                .debeCambiarPassword(true)
                .build();
        usuarioRepository.save(cajero);

        return EmpresaDTOResponse.builder()
                .nombreEmpresa(dto.getNombre())
                .emailCajero(emailCajero)
                .passwordCajero(passwordCajero)
                .build();
    }

    private void validarSubdominio(String subdominio) {
        if (empresaRepository.existsBySubdominio(subdominio)) {
            throw new IllegalArgumentException("El subdominio '" + subdominio + "' ya está en uso");
        }
    }

    private void validarEmailEncargado(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email '" + email + "' ya está registrado");
        }
    }

    private void validarPasswords(String password, String confirmarPassword) {
        if (!password.equals(confirmarPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
    }

    private String generarTenantId() {
        return "tnt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    private String generarEmailCajero(String emailAdmin) {
        int atIndex = emailAdmin.indexOf('@');
        if (atIndex == -1) {
            return "cajero_" + emailAdmin;
        }
        return emailAdmin.substring(0, atIndex) + "_cajero" + emailAdmin.substring(atIndex);
    }
}
