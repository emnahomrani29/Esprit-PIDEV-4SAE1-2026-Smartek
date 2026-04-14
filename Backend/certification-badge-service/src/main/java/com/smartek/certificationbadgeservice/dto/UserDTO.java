package com.smartek.certificationbadgeservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a user fetched from the Auth Service.
 */
@Data
@NoArgsConstructor
public class UserDTO {
    private Long userId;
    private String email;
    private String firstName;
    private String role;
    private String message;
}
