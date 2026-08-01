package com.josegregoppdev.mibombay.repository.company;

import com.josegregoppdev.mibombay.model.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByTenantId(String tenantId);

    Optional<Company> findBySubdomain(String subdomain);

    boolean existsBySubdomain(String subdomain);

    boolean existsByTenantId(String tenantId);
}
