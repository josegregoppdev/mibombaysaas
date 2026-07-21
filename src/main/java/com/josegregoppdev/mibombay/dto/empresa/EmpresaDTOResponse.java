package com.josegregoppdev.mibombay.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaDTOResponse {

    private String nombreEmpresa;
    private String emailCajero;
    private String passwordCajero;
}
