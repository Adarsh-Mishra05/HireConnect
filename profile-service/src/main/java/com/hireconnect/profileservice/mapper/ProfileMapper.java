package com.hireconnect.profileservice.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hireconnect.profileservice.dto.request.EducationRequestDto;
import com.hireconnect.profileservice.dto.request.ExperienceRequestDto;
import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.request.RecruiterDetailRequestDto;
import com.hireconnect.profileservice.dto.request.SkillRequestDto;
import com.hireconnect.profileservice.dto.request.SocialLinkRequestDto;
import com.hireconnect.profileservice.dto.response.EducationResponseDto;
import com.hireconnect.profileservice.dto.response.ExperienceResponseDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.dto.response.RecruiterDetailResponseDto;
import com.hireconnect.profileservice.dto.response.ResumeResponseDto;
import com.hireconnect.profileservice.dto.response.SkillResponseDto;
import com.hireconnect.profileservice.dto.response.SocialLinkResponseDto;
import com.hireconnect.profileservice.entity.Education;
import com.hireconnect.profileservice.entity.Experience;
import com.hireconnect.profileservice.entity.Profile;
import com.hireconnect.profileservice.entity.RecruiterDetail;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.entity.Skill;
import com.hireconnect.profileservice.entity.SocialLink;

@Component
public class ProfileMapper {

    public Profile toProfileEntity(ProfileRequestDto requestDto, Long userId, Role role) {
        if (requestDto == null) {
            return null;
        }

        Profile profile = Profile.builder()
                .userId(userId)
                .role(role)
                .firstName(trimToEmpty(requestDto.getFirstName()))
                .lastName(trimToNull(requestDto.getLastName()))
                .phone(trimToNull(requestDto.getPhone()))
                .headline(trimToNull(requestDto.getHeadline()))
                .location(trimToNull(requestDto.getLocation()))
                .about(trimToNull(requestDto.getAbout()))
                .profilePictureUrl(trimToNull(requestDto.getProfilePictureUrl()))
                .build();

        profile.setSkills(toSkillEntities(requestDto.getSkills(), profile));
        profile.setEducations(toEducationEntities(requestDto.getEducations(), profile));
        profile.setExperiences(toExperienceEntities(requestDto.getExperiences(), profile));
        profile.setSocialLinks(toSocialLinkEntities(requestDto.getSocialLinks(), profile));
        profile.setRecruiterDetail(toRecruiterDetailEntity(requestDto.getRecruiterDetail(), profile));

        return profile;
    }

    public void updateProfileEntity(Profile existingProfile, ProfileRequestDto requestDto) {
        // firstName column is NOT NULL in DB; keep existing value when null is sent.
        if (requestDto.getFirstName() != null) {
            existingProfile.setFirstName(trimToEmpty(requestDto.getFirstName()));
        }

        existingProfile.setLastName(trimToNull(requestDto.getLastName()));
        existingProfile.setPhone(trimToNull(requestDto.getPhone()));
        existingProfile.setHeadline(trimToNull(requestDto.getHeadline()));
        existingProfile.setLocation(trimToNull(requestDto.getLocation()));
        existingProfile.setAbout(trimToNull(requestDto.getAbout()));
        existingProfile.setProfilePictureUrl(trimToNull(requestDto.getProfilePictureUrl()));

        if (existingProfile.getSkills() == null) {
            existingProfile.setSkills(new ArrayList<>());
        }
        existingProfile.getSkills().clear();
        existingProfile.getSkills().addAll(toSkillEntities(requestDto.getSkills(), existingProfile));

        if (existingProfile.getEducations() == null) {
            existingProfile.setEducations(new ArrayList<>());
        }
        existingProfile.getEducations().clear();
        existingProfile.getEducations().addAll(toEducationEntities(requestDto.getEducations(), existingProfile));

        if (existingProfile.getExperiences() == null) {
            existingProfile.setExperiences(new ArrayList<>());
        }
        existingProfile.getExperiences().clear();
        existingProfile.getExperiences().addAll(toExperienceEntities(requestDto.getExperiences(), existingProfile));

        if (existingProfile.getSocialLinks() == null) {
            existingProfile.setSocialLinks(new ArrayList<>());
        }
        existingProfile.getSocialLinks().clear();
        existingProfile.getSocialLinks().addAll(toSocialLinkEntities(requestDto.getSocialLinks(), existingProfile));

        if (requestDto.getRecruiterDetail() == null) {
            existingProfile.setRecruiterDetail(null);
        } else {
            if (existingProfile.getRecruiterDetail() == null) {
                existingProfile.setRecruiterDetail(toRecruiterDetailEntity(requestDto.getRecruiterDetail(), existingProfile));
            } else {
                existingProfile.getRecruiterDetail().setCompanyName(requestDto.getRecruiterDetail().getCompanyName());
                existingProfile.getRecruiterDetail().setWebsite(requestDto.getRecruiterDetail().getWebsite());
                existingProfile.getRecruiterDetail().setCompanyDescription(requestDto.getRecruiterDetail().getCompanyDescription());
                existingProfile.getRecruiterDetail().setDesignation(requestDto.getRecruiterDetail().getDesignation());
            }
        }
    }

    public ProfileResponseDto toProfileResponseDto(Profile profile) {
        if (profile == null) {
            return null;
        }

        return ProfileResponseDto.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .role(profile.getRole())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phone(profile.getPhone())
                .headline(profile.getHeadline())
                .location(profile.getLocation())
                .about(profile.getAbout())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .skills(toSkillResponseDtos(profile.getSkills()))
                .educations(toEducationResponseDtos(profile.getEducations()))
                .experiences(toExperienceResponseDtos(profile.getExperiences()))
                .socialLinks(toSocialLinkResponseDtos(profile.getSocialLinks()))
                .resume(toResumeResponseDto(profile.getResume()))
                .recruiterDetail(toRecruiterDetailResponseDto(profile.getRecruiterDetail()))
                .build();
    }

    private List<Skill> toSkillEntities(List<SkillRequestDto> skillDtos, Profile profile) {
        List<Skill> skills = new ArrayList<>();
        if (skillDtos == null) {
            return skills;
        }

        for (SkillRequestDto dto : skillDtos) {
            Skill skill = Skill.builder()
                    .name(dto.getName())
                    .level(dto.getLevel())
                    .profile(profile)
                    .build();
            skills.add(skill);
        }
        return skills;
    }

    private List<Education> toEducationEntities(List<EducationRequestDto> educationDtos, Profile profile) {
        List<Education> educations = new ArrayList<>();
        if (educationDtos == null) {
            return educations;
        }

        for (EducationRequestDto dto : educationDtos) {
            Education education = Education.builder()
                    .institution(dto.getInstitution())
                    .degree(dto.getDegree())
                    .fieldOfStudy(dto.getFieldOfStudy())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .grade(dto.getGrade())
                    .description(dto.getDescription())
                    .profile(profile)
                    .build();
            educations.add(education);
        }
        return educations;
    }

    private List<Experience> toExperienceEntities(List<ExperienceRequestDto> experienceDtos, Profile profile) {
        List<Experience> experiences = new ArrayList<>();
        if (experienceDtos == null) {
            return experiences;
        }

        for (ExperienceRequestDto dto : experienceDtos) {
            Experience experience = Experience.builder()
                    .companyName(dto.getCompanyName())
                    .jobTitle(dto.getJobTitle())
                    .employmentType(dto.getEmploymentType())
                    .location(dto.getLocation())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .currentlyWorking(dto.getCurrentlyWorking())
                    .description(dto.getDescription())
                    .profile(profile)
                    .build();
            experiences.add(experience);
        }
        return experiences;
    }

    private List<SocialLink> toSocialLinkEntities(List<SocialLinkRequestDto> socialLinkDtos, Profile profile) {
        List<SocialLink> socialLinks = new ArrayList<>();
        if (socialLinkDtos == null) {
            return socialLinks;
        }

        for (SocialLinkRequestDto dto : socialLinkDtos) {
            SocialLink socialLink = SocialLink.builder()
                    .platform(dto.getPlatform())
                    .url(dto.getUrl())
                    .profile(profile)
                    .build();
            socialLinks.add(socialLink);
        }
        return socialLinks;
    }

    private RecruiterDetail toRecruiterDetailEntity(RecruiterDetailRequestDto dto, Profile profile) {
        if (dto == null) {
            return null;
        }

        return RecruiterDetail.builder()
                .companyName(dto.getCompanyName())
                .website(dto.getWebsite())
                .companyDescription(dto.getCompanyDescription())
                .designation(dto.getDesignation())
                .profile(profile)
                .build();
    }

    private List<SkillResponseDto> toSkillResponseDtos(List<Skill> skills) {
        List<SkillResponseDto> responseDtos = new ArrayList<>();
        if (skills == null) {
            return responseDtos;
        }

        for (Skill skill : skills) {
            SkillResponseDto dto = SkillResponseDto.builder()
                    .id(skill.getId())
                    .name(skill.getName())
                    .level(skill.getLevel())
                    .build();
            responseDtos.add(dto);
        }
        return responseDtos;
    }

    private List<EducationResponseDto> toEducationResponseDtos(List<Education> educations) {
        List<EducationResponseDto> responseDtos = new ArrayList<>();
        if (educations == null) {
            return responseDtos;
        }

        for (Education education : educations) {
            EducationResponseDto dto = EducationResponseDto.builder()
                    .id(education.getId())
                    .institution(education.getInstitution())
                    .degree(education.getDegree())
                    .fieldOfStudy(education.getFieldOfStudy())
                    .startDate(education.getStartDate())
                    .endDate(education.getEndDate())
                    .grade(education.getGrade())
                    .description(education.getDescription())
                    .build();
            responseDtos.add(dto);
        }
        return responseDtos;
    }

    private List<ExperienceResponseDto> toExperienceResponseDtos(List<Experience> experiences) {
        List<ExperienceResponseDto> responseDtos = new ArrayList<>();
        if (experiences == null) {
            return responseDtos;
        }

        for (Experience experience : experiences) {
            ExperienceResponseDto dto = ExperienceResponseDto.builder()
                    .id(experience.getId())
                    .companyName(experience.getCompanyName())
                    .jobTitle(experience.getJobTitle())
                    .employmentType(experience.getEmploymentType())
                    .location(experience.getLocation())
                    .startDate(experience.getStartDate())
                    .endDate(experience.getEndDate())
                    .currentlyWorking(experience.getCurrentlyWorking())
                    .description(experience.getDescription())
                    .build();
            responseDtos.add(dto);
        }
        return responseDtos;
    }

    private List<SocialLinkResponseDto> toSocialLinkResponseDtos(List<SocialLink> socialLinks) {
        List<SocialLinkResponseDto> responseDtos = new ArrayList<>();
        if (socialLinks == null) {
            return responseDtos;
        }

        for (SocialLink socialLink : socialLinks) {
            SocialLinkResponseDto dto = SocialLinkResponseDto.builder()
                    .id(socialLink.getId())
                    .platform(socialLink.getPlatform())
                    .url(socialLink.getUrl())
                    .build();
            responseDtos.add(dto);
        }
        return responseDtos;
    }

    private ResumeResponseDto toResumeResponseDto(Resume resume) {
        if (resume == null) {
            return null;
        }

        return ResumeResponseDto.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .contentType(resume.getContentType())
                .fileSize(resume.getFileSize())
                .uploadedAt(resume.getUploadedAt())
                .extractedName(resume.getExtractedName())
                .extractedEmail(resume.getExtractedEmail())
                .extractedPhone(resume.getExtractedPhone())
                .extractedSkills(resume.getExtractedSkills())
                .build();
    }

    private RecruiterDetailResponseDto toRecruiterDetailResponseDto(RecruiterDetail recruiterDetail) {
        if (recruiterDetail == null) {
            return null;
        }

        return RecruiterDetailResponseDto.builder()
                .id(recruiterDetail.getId())
                .companyName(recruiterDetail.getCompanyName())
                .website(recruiterDetail.getWebsite())
                .companyDescription(recruiterDetail.getCompanyDescription())
                .designation(recruiterDetail.getDesignation())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
