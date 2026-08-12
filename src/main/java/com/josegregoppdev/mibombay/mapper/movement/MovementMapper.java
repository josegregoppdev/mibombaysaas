package com.josegregoppdev.mibombay.mapper.movement;

import com.josegregoppdev.mibombay.dto.movement.MovementDTO;
import com.josegregoppdev.mibombay.model.movement.Movement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovementMapper {
    MovementDTO toDto(Movement entity);
    Movement toEntity(MovementDTO dto);
}
