package com.josegregoppdev.mibombay.controller.admin;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.crearEmpresa;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private AdminController controller;

    @Test
    void dashboard_retornaVistaConEstadisticas() {
        Empresa empresa1 = crearEmpresa();
        empresa1.setActivo(true);
        Empresa empresa2 = crearEmpresa();
        empresa2.setActivo(true);
        Empresa empresa3 = crearEmpresa();
        empresa3.setActivo(false);

        when(empresaRepository.findAll()).thenReturn(List.of(empresa1, empresa2, empresa3));

        Model model = new ExtendedModelMap();
        String view = controller.dashboard(model);

        assertEquals("admin/dashboard", view);
        assertEquals(3L, model.getAttribute("total"));
        assertEquals(2L, model.getAttribute("activas"));
        assertEquals(1L, model.getAttribute("inactivas"));
    }

    @Test
    void empresas_retornaVistaConLista() {
        when(empresaRepository.findAll()).thenReturn(List.of(crearEmpresa(), crearEmpresa()));

        Model model = new ExtendedModelMap();
        String view = controller.empresas(model);

        assertEquals("admin/empresas", view);
        assertTrue(model.containsAttribute("empresas"));
    }
}
