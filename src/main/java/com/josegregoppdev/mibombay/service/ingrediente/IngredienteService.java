package com.josegregoppdev.mibombay.service.ingrediente;

import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.mapper.ingrediente.IngredienteMapper;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
import com.josegregoppdev.mibombay.repository.ingrediente.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final IngredienteMapper ingredienteMapper;

    @Transactional(readOnly = true)
    public Page<IngredienteDTO> obtenerIngredientesPaginados(String tenantId, String nombre,
                                                              Categoria categoria,
                                                              UnidadMedida unidadMedida,
                                                              Pageable pageable) {
        String nombreParam = (nombre != null && !nombre.isBlank()) ? nombre : null;
        return ingredienteRepository.findByFilters(tenantId, nombreParam, categoria, unidadMedida, pageable)
                .map(ingredienteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<IngredienteDTO> obtenerIngredientesPaginados(String tenantId, Pageable pageable) {
        return obtenerIngredientesPaginados(tenantId, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<IngredienteDTO> obtenerIngredientesActivosPaginados(String tenantId, Pageable pageable) {
        return ingredienteRepository.findByTenantIdAndActivoTrue(tenantId, pageable)
                .map(ingredienteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public IngredienteDTO obtenerIngredientePorId(Long id, String tenantId) {
        Ingrediente ingrediente = ingredienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado"));
        return ingredienteMapper.toDto(ingrediente);
    }

    @Transactional
    public IngredienteDTO crearNuevoIngrediente(IngredienteDTO dto, String tenantId) {
        if (ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), tenantId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese código");
        }

        if (ingredienteRepository.existsByNombreAndTenantId(dto.getNombre(), tenantId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese nombre");
        }

        Ingrediente ingrediente = ingredienteMapper.toEntity(dto);
        ingrediente.setTenantId(tenantId);
        ingrediente.setActivo(true);
        ingrediente = ingredienteRepository.save(ingrediente);
        return ingredienteMapper.toDto(ingrediente);
    }

    @Transactional
    public IngredienteDTO actualizarIngredienteExistente(Long id, IngredienteDTO dto, String tenantId) {
        Ingrediente ingrediente = ingredienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado"));

        if (!ingrediente.getCodigo().equals(dto.getCodigo())
                && ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), tenantId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese código");
        }

        if (!ingrediente.getNombre().equals(dto.getNombre())
                && ingredienteRepository.existsByNombreAndTenantId(dto.getNombre(), tenantId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese nombre");
        }

        ingrediente.setCodigo(dto.getCodigo());
        ingrediente.setNombre(dto.getNombre());
        ingrediente.setDescripcion(dto.getDescripcion());
        ingrediente.setCategoria(dto.getCategoria());
        ingrediente.setUnidadMedida(dto.getUnidadMedida());
        ingrediente.setCostoUnitarioActual(dto.getCostoUnitarioActual());
        ingrediente.setStockActual(dto.getStockActual());
        ingrediente.setStockMinimo(dto.getStockMinimo());
        ingrediente = ingredienteRepository.save(ingrediente);
        return ingredienteMapper.toDto(ingrediente);
    }

    @Transactional
    public void cambiarEstadoActivoDelIngrediente(Long id, String tenantId) {
        Ingrediente ingrediente = ingredienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado"));
        ingrediente.setActivo(!ingrediente.getActivo());
        ingredienteRepository.save(ingrediente);
    }
}
