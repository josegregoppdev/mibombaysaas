package com.josegregoppdev.mibombay.mapper.usuario;

import com.josegregoppdev.mibombay.dto.usuario.UsuarioDTORequest;
import com.josegregoppdev.mibombay.dto.usuario.UsuarioDTOResponse;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    Usuario toEntity(UsuarioDTORequest dto);

    UsuarioDTOResponse toResponse(Usuario usuario);
}
