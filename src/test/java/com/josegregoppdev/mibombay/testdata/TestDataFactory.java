package com.josegregoppdev.mibombay.testdata;

import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTORequest;
import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTOResponse;
import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;

import java.time.LocalDateTime;

public class TestDataFactory {

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    public static EmpresaDTORequest crearEmpresaDTORequest() {
        EmpresaDTORequest dto = new EmpresaDTORequest();
        dto.setNombre("Mi Restaurante");
        dto.setSubdominio("mi-restaurante");
        dto.setCorreo("contacto@test.com");
        dto.setTelefono("+54 11 1234-5678");
        dto.setDireccion("Av. Siempre Viva 123");
        dto.setNombreEncargado("Juan Pérez");
        dto.setDocumentoEncargado("12345678");
        dto.setEmailEncargado("admin@test.com");
        dto.setPasswordEncargado("Password123!");
        dto.setConfirmarPasswordEncargado("Password123!");
        dto.setTelefonoEncargado("+54 11 9876-5432");
        return dto;
    }

    public static Empresa crearEmpresa() {
        return Empresa.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .subdominio("mi-restaurante")
                .nombre("Mi Restaurante")
                .correo("contacto@test.com")
                .telefono("+54 11 1234-5678")
                .direccion("Av. Siempre Viva 123")
                .nombreEncargado("Juan Pérez")
                .documentoEncargadoHash("$2a$12$hashEjemplo")
                .activo(true)
                .build();
    }

    public static Usuario crearAdmin() {
        return Usuario.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .email("admin@test.com")
                .passwordHash("$2a$12$hashAdmin")
                .nombreCompleto("Juan Pérez")
                .telefono("+54 11 9876-5432")
                .documentoHash("$2a$12$hashDoc")
                .rol(Rol.ADMIN)
                .activo(true)
                .debeCambiarPassword(false)
                .ultimoCambioPassword(LocalDateTime.now())
                .build();
    }

    public static Usuario crearCajero() {
        return Usuario.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .email("admin_cajero@test.com")
                .passwordHash("$2a$12$hashCajero")
                .nombreCompleto("Cajero Mi Restaurante")
                .documentoHash("$2a$12$hashDoc")
                .rol(Rol.CAJERO)
                .activo(true)
                .debeCambiarPassword(true)
                .build();
    }

    public static EmpresaDTOResponse crearEmpresaDTOResponse() {
        return EmpresaDTOResponse.builder()
                .nombreEmpresa("Mi Restaurante")
                .emailCajero("admin_cajero@test.com")
                .passwordCajero("TempPass123!")
                .build();
    }
}
