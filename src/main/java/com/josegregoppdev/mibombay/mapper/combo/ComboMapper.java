package com.josegregoppdev.mibombay.mapper.combo;

import com.josegregoppdev.mibombay.dto.combo.ComboDTO;
import com.josegregoppdev.mibombay.model.combo.Combo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComboMapper {

    Combo toEntity(ComboDTO dto);

    ComboDTO toDto(Combo combo);
}
