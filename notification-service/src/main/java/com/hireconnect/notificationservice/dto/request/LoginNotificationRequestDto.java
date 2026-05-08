package com.hireconnect.notificationservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginNotificationRequestDto {
    @Email
    @NotBlank
    private String to;

    @NotBlank
    private String userName;

    @NotBlank
    private String loginTime;
}