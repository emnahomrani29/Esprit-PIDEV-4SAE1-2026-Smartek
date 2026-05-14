package com.smartek.sponsor.dto;

import com.smartek.sponsor.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRegisterRequest {
    private String firstName;
    private String email;
    private String password;
    private String phone;
    private Integer experience;
    private RoleType role;
}

