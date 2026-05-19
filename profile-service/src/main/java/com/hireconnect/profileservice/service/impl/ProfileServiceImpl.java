package com.hireconnect.profileservice.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.profileservice.client.ApplicationServiceClient;
import com.hireconnect.profileservice.client.JobServiceClient;
import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Profile;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.exception.ProfileAlreadyExistsException;
import com.hireconnect.profileservice.exception.ProfileNotFoundException;
import com.hireconnect.profileservice.mapper.ProfileMapper;
import com.hireconnect.profileservice.repository.ProfileRepository;
import com.hireconnect.profileservice.repository.ResumeRepository;
import com.hireconnect.profileservice.security.AuthenticatedUser;
import com.hireconnect.profileservice.service.ProfileService;
import com.hireconnect.profileservice.service.ResumeParsingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// [Adarsh Mishra] : Service implementation for user profile and resume management.
// [Adarsh Mishra] : Supports profile CRUD, candidate previews, and secure resume upload/download for recruiters.
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ResumeRepository resumeRepository;
    private final JobServiceClient jobServiceClient;
    private final ApplicationServiceClient applicationServiceClient;
    private final ResumeParsingService resumeParsingService;

    // [Adarsh Mishra] : PROFILE MANAGEMENT SECTION — Handles core profile data operations.

    // [Adarsh Mishra] : Creates a new profile for a candidate or recruiter if it doesn't already exist.
    @Override
    public ProfileResponseDto createProfile(Long userId, Role role, ProfileRequestDto requestDto) {
        log.info("Create profile request received | userId={} | role={}", userId, role);

        if (profileRepository.existsByUserId(userId)) {
            log.warn("Profile already exists | userId={}", userId);
            throw new ProfileAlreadyExistsException("Profile already exists for userId: " + userId);
        }

        Profile profile = profileMapper.toProfileEntity(requestDto, userId, role);
        Profile savedProfile = profileRepository.save(profile);

        log.info("Profile created successfully | userId={} | profileId={}", userId, savedProfile.getId());
        return profileMapper.toProfileResponseDto(savedProfile);
    }

    // [Adarsh Mishra] : Retrieves the authenticated user's profile, auto-creating a blank one if missing.
    @Override
    public ProfileResponseDto getProfileByUserId(AuthenticatedUser user) {
        Long userId = user.getUserId();
        log.info("Fetching profile | userId={} role={}", userId, user.getRole());

        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No profile found for userId={} — auto-creating with role={}", userId, user.getRole());
                    Profile blank = Profile.builder()
                            .userId(userId)
                            .role(user.getRole())
                            .firstName("")
                            .lastName("")
                            .build();
                    return profileRepository.save(blank);
                });

        log.info("Profile fetched successfully | userId={} | profileId={}", userId, profile.getId());
        return profileMapper.toProfileResponseDto(profile);
    }

    @Override
    public ProfileResponseDto getProfileByUserIdInternal(Long userId) {
        log.info("Fetching profile (internal) | userId={}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No profile found for userId={} — auto-creating (internal, defaulting to CANDIDATE)", userId);
                    Profile blank = Profile.builder()
                            .userId(userId)
                            .role(Role.CANDIDATE)
                            .firstName("")
                            .lastName("")
                            .build();
                    return profileRepository.save(blank);
                });

        log.info("Profile fetched (internal) | userId={} | profileId={}", userId, profile.getId());
        return profileMapper.toProfileResponseDto(profile);
    }

    // [Adarsh Mishra] : Updates existing profile details for the authenticated user.
    @Override
    public ProfileResponseDto updateProfile(AuthenticatedUser user, ProfileRequestDto requestDto) {
        Long userId = user.getUserId();
        log.info("Update profile request | userId={}", userId);

        Profile existingProfile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No profile found for userId={} during update — auto-creating with role={}", userId, user.getRole());
                    return profileRepository.save(
                            Profile.builder()
                                    .userId(userId)
                                    .role(user.getRole())
                                    .firstName("")
                                    .lastName("")
                                    .build()
                    );
                });

        profileMapper.updateProfileEntity(existingProfile, requestDto);
        Profile updatedProfile = profileRepository.save(existingProfile);

        log.info("Profile updated successfully | userId={} | profileId={}", userId, updatedProfile.getId());
        return profileMapper.toProfileResponseDto(updatedProfile);
    }

    @Override
    public CandidateProfilePreviewDto getCandidateProfilePreviewByUserId(Long userId) {
        log.info("Fetching candidate preview | userId={}", userId);

        ProfileResponseDto profile = getProfileByUserIdInternal(userId);

        CandidateProfilePreviewDto preview = CandidateProfilePreviewDto.builder()
                .userId(userId)
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .headline(profile.getHeadline())
                .location(profile.getLocation())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .build();

        log.info("Candidate preview built | userId={}", userId);
        return preview;
    }

    // [Adarsh Mishra] : RESUME MANAGEMENT SECTION — Handles file upload and secure download logic.

    // [Adarsh Mishra] : Handles PDF resume uploads and associates them with the user's profile.
    @Override
    public String uploadResume(AuthenticatedUser user, MultipartFile file) {
        log.info("Resume upload request | userId={}", user.getUserId());

        if (file == null || file.isEmpty()) {
            log.warn("Empty resume upload attempt | userId={}", user.getUserId());
            throw new RuntimeException("Resume file is required");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            log.warn("Invalid file type | userId={} | type={}", user.getUserId(), file.getContentType());
            throw new RuntimeException("Only PDF files are allowed");
        }

        try {
            Profile profile = getProfileByUser(user);

            Resume resume = resumeRepository.findByProfile(profile)
                    .orElse(new Resume());

            resume.setProfile(profile);
            resume.setFileName(file.getOriginalFilename());
            resume.setContentType(file.getContentType());
            resume.setFileSize(file.getSize());
            resume.setFileData(file.getBytes());
            resume.setFileUrl("");
            resume.setUploadedAt(LocalDateTime.now());

            ResumeParsingService.ParsedResumeDetails parsed = resumeParsingService.parsePdf(resume.getFileData());
            resume.setExtractedName(parsed.name());
            resume.setExtractedEmail(parsed.email());
            resume.setExtractedPhone(parsed.phone());
            resume.setExtractedSkills(String.join(", ", parsed.skills()));

            if ((profile.getFirstName() == null || profile.getFirstName().isBlank())
                    && parsed.name() != null && !parsed.name().isBlank()) {
                profile.setFirstName(parsed.name().trim());
            }
            if ((profile.getPhone() == null || profile.getPhone().isBlank())
                    && parsed.phone() != null && !parsed.phone().isBlank()) {
                profile.setPhone(parsed.phone().trim());
            }
            profileRepository.save(profile);

            resumeRepository.save(resume);

            log.info("Resume uploaded successfully | userId={}", user.getUserId());
            return "Resume uploaded successfully";

        } catch (Exception e) {
            log.error("Resume upload failed | userId={} | cause={}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to upload resume: " + e.getMessage(), e);
        }
    }

    @Override
    public Resume getMyResume(AuthenticatedUser user) {
        log.info("Fetching own resume | userId={}", user.getUserId());

        Profile profile = getProfileByUser(user);

        return resumeRepository.findByProfile(profile)
                .orElseThrow(() -> {
                    log.error("Resume not found | userId={}", user.getUserId());
                    return new RuntimeException("Resume not found");
                });
    }

    // [Adarsh Mishra] : Allows recruiters to download candidate resumes for jobs they own.
    @Override
    public Resume getResumeForRecruiter(AuthenticatedUser user, Long candidateId, Long jobId) {
        log.info("Recruiter resume access request | recruiterId={} | candidateId={} | jobId={}",
                user.getUserId(), candidateId, jobId);

        if (!"RECRUITER".equalsIgnoreCase(user.getRole().name())) {
            log.warn("Unauthorized access attempt | userId={} | role={}", user.getUserId(), user.getRole());
            throw new RuntimeException("Unauthorized");
        }

        Boolean ownsJob = jobServiceClient.isJobOwnedByRecruiter(jobId, user.getUserId());
        if (Boolean.FALSE.equals(ownsJob)) {
            log.warn("Recruiter does not own job | recruiterId={} | jobId={}", user.getUserId(), jobId);
            throw new RuntimeException("You do not own this job");
        }

        Boolean hasApplied = applicationServiceClient.hasCandidateAppliedToJob(candidateId, jobId);
        if (Boolean.FALSE.equals(hasApplied)) {
            log.warn("Candidate did not apply | candidateId={} | jobId={}", candidateId, jobId);
            throw new RuntimeException("Candidate did not apply");
        }

        Profile profile = profileRepository.findByUserId(candidateId)
                .orElseThrow(() -> {
                    log.error("Candidate profile not found | candidateId={}", candidateId);
                    return new RuntimeException("Profile not found");
                });

        log.info("Resume access granted | recruiterId={} | candidateId={}", user.getUserId(), candidateId);

        return resumeRepository.findByProfile(profile)
                .orElseThrow(() -> {
                    log.error("Resume not found | candidateId={}", candidateId);
                    return new RuntimeException("Resume not found");
                });
    }

    // [Adarsh Mishra] : HELPER METHODS SECTION — Internal utility logic for service operations.

    private Profile getProfileByUser(AuthenticatedUser user) {
        // [Adarsh Mishra] : Auto-creates a blank profile if none exists for the user.
        return profileRepository.findByUserId(user.getUserId())
                .orElseGet(() -> {
                    log.info("No profile found for userId={} \u2014 auto-creating during resume upload", user.getUserId());
                    Profile blank = Profile.builder()
                            .userId(user.getUserId())
                            .role(Role.CANDIDATE)
                            .firstName("")
                            .lastName("")
                            .build();
                    return profileRepository.save(blank);
                });
    }
}
