package com.josegregoppdev.mibombay.controller.empresa;

import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTORequest;
import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTOResponse;
import com.josegregoppdev.mibombay.service.empresa.RegistroEmpresaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.crearEmpresaDTOResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroEmpresaControllerTest {

    @Mock
    private RegistroEmpresaService registroEmpresaService;

    @InjectMocks
    private RegistroEmpresaController controller;

    @Test
    void mostrarFormulario_retornaVistaRegistro() {
        Model model = new ExtendedModelMap();
        String view = controller.mostrarFormulario(model);
        assertEquals("registro", view);
        assertTrue(model.containsAttribute("empresaRequest"));
    }

    @Test
    void procesarRegistro_exitoso_retornaVistaExito() {
        EmpresaDTOResponse resultado = crearEmpresaDTOResponse();
        when(registroEmpresaService.registrar(any())).thenReturn(resultado);

        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = controller.procesarRegistro(new EmpresaDTORequest(), bindingResult, model);
        assertEquals("registro-exitoso", view);
        assertTrue(model.containsAttribute("resultado"));
    }

    @Test
    void procesarRegistro_conErrorValidacion_retornaVistaRegistro() {
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.procesarRegistro(new EmpresaDTORequest(), bindingResult, model);
        assertEquals("registro", view);
    }

    @Test
    void procesarRegistro_conExcepcion_retornaVistaRegistroConError() {
        when(registroEmpresaService.registrar(any()))
                .thenThrow(new IllegalArgumentException("El subdominio ya está en uso"));

        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = controller.procesarRegistro(new EmpresaDTORequest(), bindingResult, model);
        assertEquals("registro", view);
        assertTrue(model.containsAttribute("error"));
    }
}
