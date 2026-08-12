package com.josegregoppdev.mibombay.repository.movement;

import com.josegregoppdev.mibombay.model.movement.Movement;
import com.josegregoppdev.mibombay.model.movement.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    Page<Movement> findByTenantId(String tenantId, Pageable pageable);

    Page<Movement> findByTenantIdAndType(String tenantId, MovementType type, Pageable pageable);

    List<Movement> findByTenantIdAndIngredientId(String tenantId, Long ingredientId);

    List<Movement> findByTenantIdAndProductId(String tenantId, Long productId);

    List<Movement> findByTenantIdAndDateBetween(String tenantId, LocalDateTime from, LocalDateTime to);

    Page<Movement> findByTenantIdAndReferenceId(String tenantId, Long referenceId, Pageable pageable);

    @Query("""
            SELECT m FROM Movement m WHERE m.tenantId = :tenantId
            AND (:type IS NULL OR m.type = :type)
            AND (:from IS NULL OR m.date >= :from)
            AND (:to IS NULL OR m.date <= :to)
            """)
    Page<Movement> findByFilters(@Param("tenantId") String tenantId,
                                 @Param("type") MovementType type,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to,
                                 Pageable pageable);
}
