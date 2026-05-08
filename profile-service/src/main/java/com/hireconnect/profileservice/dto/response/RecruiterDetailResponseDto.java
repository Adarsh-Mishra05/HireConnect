package com.hireconnect.profileservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterDetailResponseDto {

    private Long id;
    private String companyName;
    private String website;
    private String companyDescription;
    private String designation;
}