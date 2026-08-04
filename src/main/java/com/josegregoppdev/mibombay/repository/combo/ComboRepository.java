package com.josegregoppdev.mibombay.repository.combo;

import com.josegregoppdev.mibombay.model.combo.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {

    Page<Combo> findByTenantId(String tenantId, Pageable pageable);

    Optional<Combo> findByIdAndTenantId(Long id, String tenantId);

    boolean existsByCodeAndTenantId(String code, String tenantId);

    boolean existsByNameAndTenantId(String name, String tenantId);

    @Query("""
            SELECT c FROM Combo c WHERE c.tenantId = :tenantId
            AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Combo> findByFilters(@Param("tenantId") String tenantId,
                              @Param("name") String name,
                              Pageable pageable);

    @Query("""
            SELECT DISTINCT c FROM Combo c
            LEFT JOIN FETCH c.details d
            LEFT JOIN FETCH d.product p
            WHERE c.tenantId = :tenantId AND c.active = true
            """)
    List<Combo> findAllActiveWithDetailsAndProducts(@Param("tenantId") String tenantId);
}
