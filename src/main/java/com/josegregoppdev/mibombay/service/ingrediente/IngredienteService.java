package com.josegregoppdev.mibombay.service.ingrediente;

import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.mapper.ingrediente.IngredienteMapper;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
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
    public Page<IngredienteDTO> listar(String tenantId, Pageable pageable) {
        return ingredienteRepository.findByTenantId(tenantId, pageable)
                .map(ingredienteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<IngredienteDTO> listarActivos(String tenantId, Pageable pageable) {
        return ingredienteRepository.findByTenantIdAndActivoTrue(tenantId, pageable)
                .map(ingredienteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public IngredienteDTO obtenerPorId(Long id, String tenantId) {
        Ingrediente ingrediente = ingredienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado"));
        return ingredienteMapper.toDto(ingrediente);
    }

    @Transactional
    public IngredienteDTO crear(IngredienteDTO dto, String tenantId) {
        if (ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), tenantId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese código");
        }

        Ingrediente ingrediente = ingredienteMapper.toEntity(dto);
        ingrediente.setTenantId(tenantId);
        ingrediente.setActivo(true);
        ingrediente = ingredienteRepository.save(ingrediente);
        return ingredienteMapper.toDto(ingrediente);
    }

    @Transactional
    public IngredienteDTO actualizar(Long id, IngredienteDTO dto, String tenantId) {
        Ingrediente ingrediente = ingredienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado"));

        if (!ingrediente.getCodigo().equals(dto.getCodigo())
                && ingredienteRepository.existsByCodigoAndTenantId(dto.getCodigo(), tenantId)) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese código");
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
    public void desactivar(Long id, String tenantId) {
        Ingrediente ingrediente = ingredienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado"));
        ingrediente.setActivo(false);
        ingredienteRepository.save(ingrediente);
    }
}
