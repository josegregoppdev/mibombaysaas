package com.josegregoppdev.mibombay.repository.sale;

import com.josegregoppdev.mibombay.model.sale.Sale;
import com.josegregoppdev.mibombay.model.sale.SaleState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByIdAndTenantId(Long id, String tenantId);

    Page<Sale> findByTenantIdAndState(String tenantId, SaleState state, Pageable pageable);

    List<Sale> findByTenantIdAndStateOrderBySaleDateDesc(String tenantId, SaleState state);

    List<Sale> findByTenantIdAndStateAndCashierId(String tenantId, SaleState state, Long cashierId);

    Page<Sale> findByTenantIdAndStateIn(String tenantId, List<SaleState> states, Pageable pageable);

    Page<Sale> findByTenantId(String tenantId, Pageable pageable);

    @Query("""
            SELECT s FROM Sale s WHERE s.tenantId = :tenantId
            AND (:state IS NULL OR s.state = :state)
            ORDER BY s.saleDate DESC
            """)
    Page<Sale> findByFilters(@Param("tenantId") String tenantId,
                             @Param("state") SaleState state,
                             Pageable pageable);
}
