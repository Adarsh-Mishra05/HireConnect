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
public class CandidateProfilePreviewDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String headline;
    private String location;
    private String profilePictureUrl;
    private String resumeUrl;
}