package com.josegregoppdev.mibombay.service.configuration;

import com.josegregoppdev.mibombay.dto.configuration.TenantConfigurationDTO;
import com.josegregoppdev.mibombay.mapper.configuration.TenantConfigurationMapper;
import com.josegregoppdev.mibombay.model.configuration.TenantConfiguration;
import com.josegregoppdev.mibombay.repository.configuration.TenantConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantConfigurationService {

    private final TenantConfigurationRepository tenantConfigurationRepository;
    private final TenantConfigurationMapper tenantConfigurationMapper;

    /**
     * Returns the configuration for a tenant. If it does not exist yet,
     * creates a default one (allowNegativeInventory = true) and returns it.
     * This is the only method the rest of the system should call to read the flag.
     */
    @Transactional
    public TenantConfigurationDTO getByTenantId(String tenantId) {
        return tenantConfigurationRepository.findByTenantId(tenantId)
                .map(tenantConfigurationMapper::toDto)
                .orElseGet(() -> {
                    TenantConfiguration config = TenantConfiguration.builder()
                            .tenantId(tenantId)
                            .allowNegativeInventory(true)
                            .build();
                    return tenantConfigurationMapper.toDto(
                            tenantConfigurationRepository.save(config));
                });
    }

    /**
     * Updates the configuration for a tenant. The configuration must already exist.
     */
    @Transactional
    public TenantConfigurationDTO update(String tenantId, TenantConfigurationDTO dto) {
        TenantConfiguration config = tenantConfigurationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant configuration not found"));
        config.setAllowNegativeInventory(dto.getAllowNegativeInventory());
        return tenantConfigurationMapper.toDto(tenantConfigurationRepository.save(config));
    }
}
