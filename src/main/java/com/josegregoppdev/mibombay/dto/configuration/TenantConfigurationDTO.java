package com.josegregoppdev.mibombay.dto.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantConfigurationDTO {

    private Long id;

    private String tenantId;

    private Boolean allowNegativeInventory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
    