package com.josegregoppdev.mibombay.mapper.user;

import com.josegregoppdev.mibombay.dto.user.UserDTORequest;
import com.josegregoppdev.mibombay.dto.user.UserDTOResponse;
import com.josegregoppdev.mibombay.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toEntity(UserDTORequest dto);

    UserDTOResponse toResponse(User user);
}
