package com.hireconnect.profileservice.dto.response;

import com.hireconnect.profileservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private String headline;
    private String location;
    private String about;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<SkillResponseDto> skills;
    private List<EducationResponseDto> educations;
    private List<ExperienceResponseDto> experiences;
    private List<SocialLinkResponseDto> socialLinks;
    private ResumeResponseDto resume;
    private RecruiterDetailResponseDto recruiterDetail;
}