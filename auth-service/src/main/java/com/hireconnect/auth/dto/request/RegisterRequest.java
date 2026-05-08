package com.hireconnect.auth.dto.request;

import com.hireconnect.auth.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @Size(max = 120)
    private String fullName;

    @NotNull
    private Role role;

    @NotBlank(message = "OTP is required")
    private String otp;
}
