package com.hireconnect.auth.dto.response;

import com.hireconnect.auth.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private Role role;
    private String message;
}