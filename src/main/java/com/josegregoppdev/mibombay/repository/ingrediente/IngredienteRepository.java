package com.josegregoppdev.mibombay.repository.ingrediente;

import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    Page<Ingrediente> findByTenantId(String tenantId, Pageable pageable);

    Page<Ingrediente> findByTenantIdAndActivoTrue(String tenantId, Pageable pageable);

    Optional<Ingrediente> findByIdAndTenantId(Long id, String tenantId);

    boolean existsByCodigoAndTenantId(String codigo, String tenantId);
}
