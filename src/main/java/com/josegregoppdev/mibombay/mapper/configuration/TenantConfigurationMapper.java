package com.josegregoppdev.mibombay.mapper.configuration;

import com.josegregoppdev.mibombay.dto.configuration.TenantConfigurationDTO;
import com.josegregoppdev.mibombay.model.configuration.TenantConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantConfigurationMapper {

    TenantConfigurationDTO toDto(TenantConfiguration entity);

    TenantConfiguration toEntity(TenantConfigurationDTO dto);
}
