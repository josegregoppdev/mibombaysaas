package com.josegregoppdev.mibombay.repository.purchase;

import com.josegregoppdev.mibombay.model.purchase.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByIdAndTenantId(Long id, String tenantId);

    Page<Purchase> findByTenantId(String tenantId, Pageable pageable);

    @Query("""
            SELECT p FROM Purchase p WHERE p.tenantId = :tenantId
            AND (:supplierName IS NULL OR LOWER(p.supplierName) LIKE LOWER(CONCAT('%', :supplierName, '%')))
            AND (:from IS NULL OR p.purchaseDate >= :from)
            AND (:to IS NULL OR p.purchaseDate <= :to)
            AND (:active IS NULL OR p.active = :active)
            """)
    Page<Purchase> findByFilters(@Param("tenantId") String tenantId,
                                 @Param("supplierName") String supplierName,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to,
                                 @Param("active") Boolean active,
                                 Pageable pageable);
}