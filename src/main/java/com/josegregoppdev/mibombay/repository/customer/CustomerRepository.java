package com.josegregoppdev.mibombay.repository.customer;

import com.josegregoppdev.mibombay.model.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByTenantId(String tenantId, Pageable pageable);

    Page<Customer> findByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    Optional<Customer> findByIdAndTenantId(Long id, String tenantId);

    Optional<Customer> findByTenantIdAndDocumentLookupHash(String tenantId, String documentLookupHash);

    Optional<Customer> findByTenantIdAndPhoneLookupHash(String tenantId, String phoneLookupHash);

    Optional<Customer> findByTenantIdAndIsDefaultTrue(String tenantId);

    boolean existsByTenantIdAndDocumentLookupHash(String tenantId, String documentLookupHash);

    boolean existsByTenantIdAndDocumentLookupHashAndIdNot(String tenantId, String documentLookupHash, Long id);

    @Query("""
            SELECT c FROM Customer c WHERE c.tenantId = :tenantId
            AND (:name IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:document IS NULL OR LOWER(c.documentEncrypted) LIKE LOWER(CONCAT('%', :document, '%')))
            ORDER BY c.fullName ASC
            """)
    Page<Customer> findByFilters(@Param("tenantId") String tenantId,
                                 @Param("name") String name,
                                 @Param("document") String document,
                                 Pageable pageable);

    List<Customer> findByTenantIdAndFullNameContainingIgnoreCase(String tenantId, String fullName);
}
