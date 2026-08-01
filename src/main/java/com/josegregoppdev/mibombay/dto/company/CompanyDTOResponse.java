package com.josegregoppdev.mibombay.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTOResponse {

    private String companyName;
    private String cashierEmail;
    private String cashierPassword;
}
