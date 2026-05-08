package com.hireconnect.profileservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequestDto {

    private String firstName;
    private String lastName;
    private String phone;
    private String headline;
    private String location;
    private String about;
    private String profilePictureUrl;

    private List<SkillRequestDto> skills;
    private List<EducationRequestDto> educations;
    private List<ExperienceRequestDto> experiences;
    private List<SocialLinkRequestDto> socialLinks;
    private ResumeRequestDto resume;
    private RecruiterDetailRequestDto recruiterDetail;
}