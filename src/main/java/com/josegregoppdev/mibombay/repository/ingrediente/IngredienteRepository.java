package com.josegregoppdev.mibombay.repository.ingrediente;

import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.Ingrediente;
import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    Page<Ingrediente> findByTenantId(String tenantId, Pageable pageable);

    Page<Ingrediente> findByTenantIdAndActivoTrue(String tenantId, Pageable pageable);

    Optional<Ingrediente> findByIdAndTenantId(Long id, String tenantId);

    boolean existsByCodigoAndTenantId(String codigo, String tenantId);

    boolean existsByNombreAndTenantId(String nombre, String tenantId);

    @Query("""
            SELECT i FROM Ingrediente i WHERE i.tenantId = :tenantId
            AND (:nombre IS NULL OR LOWER(i.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
            AND (:categoria IS NULL OR i.categoria = :categoria)
            AND (:unidadMedida IS NULL OR i.unidadMedida = :unidadMedida)
            """)
    Page<Ingrediente> findByFilters(@Param("tenantId") String tenantId,
                                    @Param("nombre") String nombre,
                                    @Param("categoria") Categoria categoria,
                                    @Param("unidadMedida") UnidadMedida unidadMedida,
                                    Pageable pageable);
}
