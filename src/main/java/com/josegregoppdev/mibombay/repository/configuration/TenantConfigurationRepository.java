package com.josegregoppdev.mibombay.repository.configuration;

import com.josegregoppdev.mibombay.model.configuration.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, Long> {

    Optional<TenantConfiguration> findByTenantId(String tenantId);

    boolean existsByTenantId(String tenantId);
}
