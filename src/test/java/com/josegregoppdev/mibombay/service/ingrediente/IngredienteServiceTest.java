package com.josegregoppdev.mibombay.service.ingrediente;

import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.mapper.ingrediente.IngredienteMapper;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
import com.josegregoppdev.mibombay.repository.ingrediente.IngredienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredienteServiceTest {

    @Mock
    private IngredienteRepository ingredienteRepository;

    @Mock
    private IngredienteMapper ingredienteMapper;

    @InjectMocks
    private IngredienteService ingredienteService;

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Test
    void obtenerIngredientesPaginados_sinFiltros_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingrediente ingrediente = crearIngrediente();
        Page<Ingrediente> pagina = new PageImpl<>(List.of(ingrediente));
        when(ingredienteRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(pagina);
        when(ingredienteMapper.toDto(ingrediente)).thenReturn(crearIngredienteDTO());

        Page<IngredienteDTO> resultado = ingredienteService.obtenerIngredientesPaginados(TENANT_ID, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("CAR-001", resultado.getContent().get(0).getCodigo());
    }

    @Test
    void obtenerIngredientesPaginados_conFiltros_retornaPaginaFiltrada() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingrediente ingrediente = crearIngrediente();
        Page<Ingrediente> pagina = new PageImpl<>(List.of(ingrediente));
        when(ingredienteRepository.findByFilters(eq(TENANT_ID), eq("Carne"), eq(Categoria.CARNES), eq(UnidadMedida.KILOGRAMO), eq(pageable)))
                .thenReturn(pagina);
        when(ingredienteMapper.toDto(ingrediente)).thenReturn(crearIngredienteDTO());

        Page<IngredienteDTO> resultado = ingredienteService.obtenerIngredientesPaginados(
                TENANT_ID, "Carne", Categoria.CARNES, UnidadMedida.KILOGRAMO, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void obtenerIngredientesActivosPaginados_retornaSoloActivos() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingrediente ingrediente = crearIngrediente();
        Page<Ingrediente> pagina = new PageImpl<>(List.of(ingrediente));
        when(ingredienteRepository.findByTenantIdAndActivoTrue(TENANT_ID, pageable)).thenReturn(pagina);
        when(ingredienteMapper.toDto(ingrediente)).thenReturn(crearIngredienteDTO());

        Page<IngredienteDTO> resultado = ingredienteService.obtenerIngredientesActivosPaginados(TENANT_ID, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertTrue(resultado.getContent().get(0).getActivo());
    }

    @Test
    void obtenerIngredientePorId_existe_retornaDTO() {
        Ingrediente ingrediente = crearIngrediente();
        when(ingredienteRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(ingrediente));
        when(ingredienteMapper.toDto(ingrediente)).thenReturn(crearIngredienteDTO());

        IngredienteDTO resultado = ingredienteService.obtenerIngredientePorId(1L, TENANT_ID);

        assertNotNull(resultado);
        assertEquals("CAR-001", resultado.getCodigo());
        assertEquals("Carne de res", resultado.getNombre());
    }

    @Test
    void obtenerIngredientePorId_noExiste_lanzaExcepcion() {
        when(ingredienteRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.obtenerIngredientePorId(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void crearNuevoIngrediente_exitoso_retornaDTO() {
        IngredienteDTO dto = crearIngredienteDTO();
        Ingrediente ingrediente = crearIngrediente();

        when(ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), TENANT_ID)).thenReturn(false);
        when(ingredienteRepository.existsByNombreAndTenantId(dto.getNombre(), TENANT_ID)).thenReturn(false);
        when(ingredienteMapper.toEntity(dto)).thenReturn(ingrediente);
        when(ingredienteRepository.save(any())).thenReturn(ingrediente);
        when(ingredienteMapper.toDto(ingrediente)).thenReturn(dto);

        IngredienteDTO resultado = ingredienteService.crearNuevoIngrediente(dto, TENANT_ID);

        assertNotNull(resultado);
        assertEquals("CAR-001", resultado.getCodigo());
        assertTrue(resultado.getActivo());

        ArgumentCaptor<Ingrediente> captor = ArgumentCaptor.forClass(Ingrediente.class);
        verify(ingredienteRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertTrue(captor.getValue().getActivo());
    }

    @Test
    void crearNuevoIngrediente_codigoDuplicado_lanzaExcepcion() {
        IngredienteDTO dto = crearIngredienteDTO();
        when(ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.crearNuevoIngrediente(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("código"));
    }

    @Test
    void crearNuevoIngrediente_nombreDuplicado_lanzaExcepcion() {
        IngredienteDTO dto = crearIngredienteDTO();
        when(ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), TENANT_ID)).thenReturn(false);
        when(ingredienteRepository.existsByNombreAndTenantId(dto.getNombre(), TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.crearNuevoIngrediente(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("nombre"));
    }

    @Test
    void actualizarIngredienteExistente_exitoso_actualizaCampos() {
        Ingrediente existente = crearIngrediente();
        IngredienteDTO dto = crearIngredienteDTO();
        dto.setNombre("Carne molida");
        dto.setCostoUnitarioActual(new BigDecimal("20.00"));

        when(ingredienteRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existente));
        when(ingredienteRepository.save(existente)).thenReturn(existente);
        when(ingredienteMapper.toDto(existente)).thenReturn(dto);

        IngredienteDTO resultado = ingredienteService.actualizarIngredienteExistente(1L, dto, TENANT_ID);

        assertNotNull(resultado);
        assertEquals("Carne molida", resultado.getNombre());
        assertEquals(new BigDecimal("20.00"), resultado.getCostoUnitarioActual());
        verify(ingredienteRepository).save(existente);
    }

    @Test
    void actualizarIngredienteExistente_codigoDuplicado_lanzaExcepcion() {
        Ingrediente existente = crearIngrediente();
        IngredienteDTO dto = crearIngredienteDTO();
        dto.setCodigo("PAN-002");

        when(ingredienteRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existente));
        when(ingredienteRepository.existsByCodigoAndTenantId("PAN-002", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.actualizarIngredienteExistente(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("código"));
    }

    @Test
    void actualizarIngredienteExistente_nombreDuplicado_lanzaExcepcion() {
        Ingrediente existente = crearIngrediente();
        IngredienteDTO dto = crearIngredienteDTO();
        dto.setNombre("Pan de hamburguesa");

        when(ingredienteRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(existente));
        when(ingredienteRepository.existsByNombreAndTenantId("Pan de hamburguesa", TENANT_ID)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.actualizarIngredienteExistente(1L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("nombre"));
    }

    @Test
    void actualizarIngredienteExistente_noExiste_lanzaExcepcion() {
        IngredienteDTO dto = crearIngredienteDTO();
        when(ingredienteRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.actualizarIngredienteExistente(99L, dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void cambiarEstadoActivo_activoAInactivo_cambiaEstado() {
        Ingrediente ingrediente = crearIngrediente();
        assertTrue(ingrediente.getActivo());

        when(ingredienteRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(ingrediente));

        ingredienteService.cambiarEstadoActivoDelIngrediente(1L, TENANT_ID);

        assertFalse(ingrediente.getActivo());
        verify(ingredienteRepository).save(ingrediente);
    }

    @Test
    void cambiarEstadoActivo_inactivoAActivo_cambiaEstado() {
        Ingrediente ingrediente = crearIngredienteInactivo();
        assertFalse(ingrediente.getActivo());

        when(ingredienteRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(ingrediente));

        ingredienteService.cambiarEstadoActivoDelIngrediente(2L, TENANT_ID);

        assertTrue(ingrediente.getActivo());
        verify(ingredienteRepository).save(ingrediente);
    }

    @Test
    void cambiarEstadoActivo_noExiste_lanzaExcepcion() {
        when(ingredienteRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ingredienteService.cambiarEstadoActivoDelIngrediente(99L, TENANT_ID));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }
}
