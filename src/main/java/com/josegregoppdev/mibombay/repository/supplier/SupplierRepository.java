package com.josegregoppdev.mibombay.repository.supplier;

import com.josegregoppdev.mibombay.model.supplier.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByTenantId(String tenantId, Pageable pageable);

    Optional<Supplier> findByIdAndTenantId(Long id, String tenantId);

    Optional<Supplier> findByTenantIdAndIsDefaultTrue(String tenantId);

    List<Supplier> findByTenantIdAndActiveTrueOrderByNameAsc(String tenantId);

    boolean existsByTenantIdAndDocumentLookupHash(String tenantId, String documentLookupHash);

    boolean existsByTenantIdAndDocumentLookupHashAndIdNot(String tenantId, String documentLookupHash, Long id);

    @Query("""
            SELECT s FROM Supplier s WHERE s.tenantId = :tenantId
            AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:document IS NULL OR LOWER(s.documentEncrypted) LIKE LOWER(CONCAT('%', :document, '%')))
            ORDER BY s.name ASC
            """)
    Page<Supplier> findByFilters(@Param("tenantId") String tenantId,
                                 @Param("name") String name,
                                 @Param("document") String document,
                                 Pageable pageable);
}