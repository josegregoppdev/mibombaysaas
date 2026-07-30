package com.josegregoppdev.mibombay.controller.ingrediente;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
import com.josegregoppdev.mibombay.service.ingrediente.IngredienteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.crearIngredienteDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredienteControllerTest {

    @Mock
    private IngredienteService ingredienteService;

    @InjectMocks
    private IngredienteController controller;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @BeforeEach
    void setUp() {
        TenantContext.set(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listar_sinFiltros_agregaAtributosYRetornaVista() {
        Pageable pageable = PageRequest.of(0, 20);
        IngredienteDTO dto = crearIngredienteDTO();
        Page<IngredienteDTO> pagina = new PageImpl<>(List.of(dto));
        when(ingredienteService.obtenerIngredientesPaginados(eq(TENANT_ID), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(pagina);

        Model model = new ExtendedModelMap();
        String view = controller.listar(null, null, null, pageable, model);

        assertEquals("ingrediente/listar", view);
        assertTrue(model.containsAttribute("pagina"));
        assertTrue(model.containsAttribute("categorias"));
        assertTrue(model.containsAttribute("unidadesMedida"));
        assertTrue(model.containsAttribute("nombre"));
        assertTrue(model.containsAttribute("categoriaSeleccionada"));
        assertTrue(model.containsAttribute("unidadMedidaSeleccionada"));
    }

    @Test
    void listar_conFiltros_pasaParametrosAlServicio() {
        Pageable pageable = PageRequest.of(0, 20);
        when(ingredienteService.obtenerIngredientesPaginados(eq(TENANT_ID), eq("Carne"), eq(Categoria.CARNES), eq(UnidadMedida.KILOGRAMO), any(Pageable.class)))
                .thenReturn(Page.empty());

        Model model = new ExtendedModelMap();
        String view = controller.listar("Carne", Categoria.CARNES, UnidadMedida.KILOGRAMO, pageable, model);

        assertEquals("ingrediente/listar", view);
        assertEquals("Carne", model.getAttribute("nombre"));
        assertEquals(Categoria.CARNES, model.getAttribute("categoriaSeleccionada"));
        assertEquals(UnidadMedida.KILOGRAMO, model.getAttribute("unidadMedidaSeleccionada"));
    }

    @Test
    void mostrarFormulario_agregaAtributosYRetornaVista() {
        Model model = new ExtendedModelMap();
        String view = controller.mostrarFormulario(model);

        assertEquals("ingrediente/formulario", view);
        assertTrue(model.containsAttribute("ingrediente"));
        assertTrue(model.containsAttribute("categorias"));
    }

    @Test
    void crear_exitoso_redirigeConMensaje() {
        IngredienteDTO dto = crearIngredienteDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.crear(dto, bindingResult, mock(RedirectAttributes.class));

        assertEquals("redirect:/ingrediente", view);
        verify(ingredienteService).crearNuevoIngrediente(dto, TENANT_ID);
    }

    @Test
    void crear_conErrorValidacion_retornaVistaFormulario() {
        IngredienteDTO dto = crearIngredienteDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        Model model = new ExtendedModelMap();
        String view = controller.crear(dto, bindingResult, mock(RedirectAttributes.class));

        assertEquals("ingrediente/formulario", view);
    }

    @Test
    void crear_conExcepcion_redirigeConError() {
        IngredienteDTO dto = crearIngredienteDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("Ya existe un ingrediente con ese código"))
                .when(ingredienteService).crearNuevoIngrediente(dto, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.crear(dto, bindingResult, mock(RedirectAttributes.class));

        assertEquals("redirect:/ingrediente/nuevo", view);
    }

    @Test
    void mostrarEdicion_existe_agregaAtributosYRetornaVista() {
        IngredienteDTO dto = crearIngredienteDTO();
        when(ingredienteService.obtenerIngredientePorId(1L, TENANT_ID)).thenReturn(dto);

        Model model = new ExtendedModelMap();
        String view = controller.mostrarEdicion(1L, model);

        assertEquals("ingrediente/formulario", view);
        assertTrue(model.containsAttribute("ingrediente"));
        assertTrue(model.containsAttribute("categorias"));
    }

    @Test
    void mostrarEdicion_noExiste_redirigeALista() {
        when(ingredienteService.obtenerIngredientePorId(99L, TENANT_ID))
                .thenThrow(new IllegalArgumentException("Ingrediente no encontrado"));

        Model model = new ExtendedModelMap();
        String view = controller.mostrarEdicion(99L, model);

        assertEquals("redirect:/ingrediente", view);
    }

    @Test
    void actualizar_exitoso_redirigeConMensaje() {
        IngredienteDTO dto = crearIngredienteDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ExtendedModelMap();
        String view = controller.actualizar(1L, dto, bindingResult, mock(RedirectAttributes.class));

        assertEquals("redirect:/ingrediente", view);
        verify(ingredienteService).actualizarIngredienteExistente(1L, dto, TENANT_ID);
    }

    @Test
    void actualizar_conErrorValidacion_retornaVistaFormulario() {
        IngredienteDTO dto = crearIngredienteDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        Model model = new ExtendedModelMap();
        String view = controller.actualizar(1L, dto, bindingResult, mock(RedirectAttributes.class));

        assertEquals("ingrediente/formulario", view);
    }

    @Test
    void actualizar_conExcepcion_redirigeConError() {
        IngredienteDTO dto = crearIngredienteDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        doThrow(new IllegalArgumentException("Ya existe un ingrediente con ese código"))
                .when(ingredienteService).actualizarIngredienteExistente(1L, dto, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.actualizar(1L, dto, bindingResult, mock(RedirectAttributes.class));

        assertEquals("redirect:/ingrediente/1/editar", view);
    }

    @Test
    void toggleEstado_exitoso_redirigeConMensaje() {
        Model model = new ExtendedModelMap();
        String view = controller.toggleEstado(1L, mock(RedirectAttributes.class));

        assertEquals("redirect:/ingrediente", view);
        verify(ingredienteService).cambiarEstadoActivoDelIngrediente(1L, TENANT_ID);
    }

    @Test
    void toggleEstado_noExiste_redirigeConError() {
        doThrow(new IllegalArgumentException("Ingrediente no encontrado"))
                .when(ingredienteService).cambiarEstadoActivoDelIngrediente(99L, TENANT_ID);

        Model model = new ExtendedModelMap();
        String view = controller.toggleEstado(99L, mock(RedirectAttributes.class));

        assertEquals("redirect:/ingrediente", view);
    }
}
