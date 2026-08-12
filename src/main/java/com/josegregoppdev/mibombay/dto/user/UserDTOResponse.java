package com.josegregoppdev.mibombay.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTOResponse {

    private Long id;
    private String email;
    private String fullName;
    private String role;
    private Boolean mustChangePassword;
}
