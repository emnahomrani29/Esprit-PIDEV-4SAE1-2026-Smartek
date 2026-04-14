package com.smartek.offersservice.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String role;
}
