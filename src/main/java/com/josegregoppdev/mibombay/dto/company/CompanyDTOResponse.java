package com.josegregoppdev.mibombay.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTOResponse {

    private String companyName;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String subdomain;
    private Boolean active;
    private LocalDateTime createdAt;
    private String cashierEmail;
    private String cashierPassword;
}
