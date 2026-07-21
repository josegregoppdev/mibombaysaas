package com.josegregoppdev.mibombay.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTOResponse {

    private Long id;
    private String email;
    private String nombreCompleto;
    private String rol;
}
