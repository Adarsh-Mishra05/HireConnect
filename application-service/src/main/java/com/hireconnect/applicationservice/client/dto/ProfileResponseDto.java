package com.hireconnect.applicationservice.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponseDto {
    private Long userId;
    private String firstName;
    private String phone;
}
